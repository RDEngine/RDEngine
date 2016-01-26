package org.rdengine.util.bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.rdengine.net.http.RDHttpConnection;
import org.rdengine.net.http.RDHttpRequest;
import org.rdengine.net.http.RDHttpResponse;
import org.rdengine.runtime.RT;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

/**
 * 图片缓存管理
 * 
 * @author yangyu
 */
public class BitmapCache
{

    public static String CACHE_PATH = RT.defaultRootPath + "cache/";

    public static final int REFERENCE_LIST_MAX_SIZE = 0;

    private static BitmapCache instance = null;//

    private Hashtable<Integer, SoftReference<Bitmap>> Caches = null;// Bitmap缓存池(软引用)

    private LinkedList<Bitmap> ReferenceList = null;

    private ArrayBlockingQueue<Runnable> taskQuene;
    private ThreadPoolExecutor threadPool;

    private BitmapCache()
    {
        Caches = new Hashtable<Integer, SoftReference<Bitmap>>();
        ReferenceList = new LinkedList<Bitmap>();
        taskQuene = new ArrayBlockingQueue<Runnable>(100);
        threadPool = new ThreadPoolExecutor(1, 10, 5, TimeUnit.SECONDS, taskQuene,
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static BitmapCache ins()
    {
        if (instance != null)
        {
            return instance;
        } else
        {
            synchronized (instance)
            {
                if (instance == null)
                {
                    instance = new BitmapCache();
                }
                return instance;
            }
        }
    }

    /**
     * 直接从缓存池里取Bitmap,如果没有就返回null
     * 
     * @param param
     * @return
     */
    public Bitmap getBitmapFromCache(int key)
    {
        Bitmap bmp = null;
        if (Caches.get(key) != null)
        {
            bmp = Caches.get(key).get();
        }
        if (bmp == null)
        {
            Caches.remove(key);
        }
        return bmp;
    }

    /**
     * 预取图片
     * 
     * @param url
     * @param width
     * @param height
     */
    public void preloadBitmap(BitmapParam param)
    {
        getBitmap(param, null, null);
    }

    public void getBitmap(final BitmapParam param, BitmapLoadCallback callback)
    {
        getBitmap(param, null, callback);
    }

    public void getBitmap(final BitmapParam param, ImageView view)
    {
        getBitmap(param, view, null);
    }

    /**
     * 为ImageView加载图片
     * 
     * @param param
     *            Bitmap参数
     * @param view
     *            目标ImageView
     */
    public void getBitmap(final BitmapParam param, final ImageView view, final BitmapLoadCallback callback)
    {
        Bitmap bitmap = getBitmapFromCache(getCacheKey(param.getUrl(), param.getWidth(), param.getHeight()));
        if (bitmap != null)
        {
            setBitmapToImageView(param, bitmap, view, callback);
            return;
        }
        if (param.getDefaultImage() > 0)
        {
            if (view != null)
                view.setImageResource(param.getDefaultImage());
        }
        Runnable task = new Runnable()
        {
            @Override
            public void run()
            {

                Bitmap bitmap = getLocalCacheBitmap(param);
                if (bitmap == null)
                {
                    bitmap = getNetBitmap(param);
                }

                if (bitmap != null)
                {
                    if (view != null)
                    {
                        setBitmapToImageView(param, bitmap, view, callback);

                    }
                    Caches.put(getCacheKey(param.getUrl(), param.getWidth(), param.getHeight()),
                            new SoftReference<Bitmap>(bitmap));
                    putBitmapToReferenceList(bitmap);
                }
            }
        };
        threadPool.execute(task);
    }

    private Bitmap getLocalCacheBitmap(BitmapParam param)
    {
        int fileKey = getFileKey(param.getUrl());
        File cacheFile = new File(CACHE_PATH + fileKey);
        if (cacheFile.exists())
        {

            BitmapRegionDecoder decoder;
            int sample = 1;
            try
            {
                decoder = BitmapRegionDecoder.newInstance(cacheFile.getAbsolutePath(), true);
                int srcWidth = decoder.getWidth();
                int srcHeight = decoder.getHeight();
                int sampleWidth = 1, sampleHeight = 1;
                if (param.getWidth() > 0)
                {
                    sampleWidth = srcWidth / param.getWidth();
                }
                if (param.getHeight() > 0)
                {
                    sampleHeight = srcHeight / param.getHeight();
                }
                sample = sampleWidth > sampleHeight ? sampleWidth : sampleHeight;
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inSampleSize = sample;
            Bitmap bitmap = BitmapFactory.decodeFile(cacheFile.getAbsolutePath(), op);
            if (bitmap == null)
            {
                cacheFile.delete();
            }
            return bitmap;
        } else
        {
            return null;
        }
    }

    private void setBitmapToImageView(final BitmapParam param, final Bitmap bitmap, final ImageView view,
            final BitmapLoadCallback callback)
    {
        if (view != null)
        {
            new Handler(Looper.getMainLooper()).post(new Runnable()
            {

                @Override
                public void run()
                {
                    view.setImageBitmap(bitmap);
                }
            });
        }
        if (callback != null)
        {
            new Handler(Looper.getMainLooper()).post(new Runnable()
            {

                @Override
                public void run()
                {
                    callback.onBitmapCallback(bitmap, param);
                }
            });
        }
    }

    private Bitmap getNetBitmap(BitmapParam param)
    {
        RDHttpRequest request = new RDHttpRequest(RDHttpRequest.METHOD_GET, param.getFullUrl(), null);
        request.setReadCache(false);
        request.setCacheTime(0);
        RDHttpResponse response = RDHttpConnection.ins().requestSync(request);
        if (response.getErrcode() == 0 && response.getResponseData() != null)
        {
            saveBitmapCache(param, response.getResponseData());
        }

        return getLocalCacheBitmap(param);
    }

    private void saveBitmapCache(BitmapParam param, byte[] data)
    {
        File cacheFile = new File(CACHE_PATH + getFileKey(param.getUrl()));
        if (cacheFile.exists())
        {
            cacheFile.delete();
        }
        try
        {
            cacheFile.createNewFile();
            FileOutputStream os = new FileOutputStream(cacheFile);
            os.write(data);
            os.flush();
            os.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void putBitmapToReferenceList(Bitmap bitmap)
    {
        if (ReferenceList.contains(bitmap))
        {
            ReferenceList.remove(bitmap);

        }
        if (REFERENCE_LIST_MAX_SIZE > 0)
        {
            ReferenceList.addFirst(bitmap);
        }

        while (ReferenceList.size() > REFERENCE_LIST_MAX_SIZE)
        {
            ReferenceList.removeLast();
        }
    }

    public int getCacheKey(String url, int width, int height)
    {
        int hash = 0;
        hash = (url + width + height).hashCode();
        return hash;
    }

    public int getFileKey(String url)
    {
        return url.hashCode();
    }

    public void cancelRequest(RDHttpRequest request)
    {
        RDHttpConnection.ins().cancel(request);
    }

    /**
     * 手动强制释放资源
     * 
     * @param releaseTag
     *            释放资源的Tag
     */
    public void releaseImageViewByTag(String releaseTag)
    {

    }

}
