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

import org.rdengine.log.DLOG;
import org.rdengine.net.http.RDHttpConnection;
import org.rdengine.net.http.RDHttpRequest;
import org.rdengine.net.http.RDHttpResponse;
import org.rdengine.util.MD5Util;
import org.rdengine.util.StringUtil;

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
public class RDBitmapCache
{

    private static final boolean LOGED = true;

    public static String CACHE_PATH = "";

    public static final int REFERENCE_LIST_MAX_SIZE = 0;

    private static RDBitmapCache instance = null;//

    private Hashtable<Integer, SoftReference<Bitmap>> Caches = null;// Bitmap缓存池(软引用)
    private Hashtable<ImageView, RDBitmapParam> Binds = null;

    private LinkedList<Bitmap> ReferenceList = null;

    private ArrayBlockingQueue<Runnable> taskQuene;
    private ThreadPoolExecutor threadPool;

    private RDBitmapCache()
    {
        Caches = new Hashtable<Integer, SoftReference<Bitmap>>();
        Binds = new Hashtable<ImageView, RDBitmapParam>();
        ReferenceList = new LinkedList<Bitmap>();
        taskQuene = new ArrayBlockingQueue<Runnable>(100);
        threadPool = new ThreadPoolExecutor(1, 5, 30, TimeUnit.SECONDS, taskQuene,
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static RDBitmapCache ins()
    {
        if (instance != null)
        {
            return instance;
        } else
        {
            synchronized (RDBitmapCache.class)
            {
                if (instance == null)
                {
                    instance = new RDBitmapCache();
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
    public void preloadBitmap(RDBitmapParam param)
    {
        getBitmap(param, null, null);
    }

    public void getBitmap(RDBitmapParam param)
    {
        getBitmap(param, null, null);
    }

    public void getBitmap(RDBitmapParam param, RDBitmapLoadCallback callback)
    {
        getBitmap(param, null, callback);
    }

    public void getBitmap(RDBitmapParam param, ImageView view)
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
    public void getBitmap(final RDBitmapParam param, final ImageView view, final RDBitmapLoadCallback callback)
    {
        if (LOGED)
            DLOG.d("BitmapCache", "" + Thread.currentThread().getId() + "getBitmap:" + param.getFullUrl());

        if (StringUtil.isEmpty(param.getUrl()) || (!param.getFullUrl().trim().toLowerCase().startsWith("http://")
                && !param.getFullUrl().trim().toLowerCase().startsWith("ftp://")))
        {
            if (view != null)
            {
                view.setImageResource(param.getDefaultImage());
            }
            return;
        }
        if (view != null)
        {
            Binds.put(view, param);
        }
        Bitmap bitmap = getBitmapFromCache(getCacheKey(param.getFullUrl(), param.getWidth(), param.getHeight()));
        if (bitmap != null)
        {
            bindViewAndCallback(param, bitmap, view, callback);
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
                bindViewAndCallback(param, bitmap, view, callback);
                if (bitmap != null)
                {
                    Caches.put(getCacheKey(param.getFullUrl(), param.getWidth(), param.getHeight()),
                            new SoftReference<Bitmap>(bitmap));
                    putBitmapToReferenceList(bitmap);
                }
            }
        };
        threadPool.execute(task);
    }

    public Bitmap getLocalCacheBitmap(RDBitmapParam param)
    {

        File cacheFile = new File(getBitmapCachePath(param));
        if (cacheFile.exists())
        {

            BitmapRegionDecoder decoder;
            float sample = 1;
            try
            {
                decoder = BitmapRegionDecoder.newInstance(cacheFile.getAbsolutePath(), true);
                float srcWidth = decoder.getWidth();
                float srcHeight = decoder.getHeight();
                float sampleWidth = 1, sampleHeight = 1;
                if (param.getWidth() > 0)
                {
                    sampleWidth = srcWidth / param.getWidth();
                }
                if (param.getHeight() > 0)
                {
                    sampleHeight = srcHeight / param.getHeight();
                }
                decoder.recycle();
                sample = sampleWidth > sampleHeight ? sampleWidth : sampleHeight;
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inSampleSize = (int) sample;
            if (LOGED)
                DLOG.d("BitmapCache", "" + Thread.currentThread().getId() + "getLocalCacheBitmap:" + sample + ":"
                        + getBitmapCachePath(param));
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

    private void bindViewAndCallback(final RDBitmapParam param, final Bitmap bitmap, final ImageView view,
            final RDBitmapLoadCallback callback)
    {
        if (LOGED)
            DLOG.d("BitmapCache", "" + Thread.currentThread().getId() + "setImageBitmap:" + bitmap);
        if (view != null)
        {
            new Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    if (Binds.get(view) == param)
                    {
                        Binds.remove(view);
                        if (bitmap != null)
                        {
                            view.setImageBitmap(bitmap);
                        }
                    }
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
                    if (callback != null)
                    {
                        callback.onBitmapCallback(bitmap, param);
                    }
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

    private Bitmap getNetBitmap(RDBitmapParam param)
    {
        if (LOGED)
            DLOG.d("BitmapCache", "" + Thread.currentThread().getId() + "getNetBitmap:" + param.getFullUrl());
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

    private void saveBitmapCache(RDBitmapParam param, byte[] data)
    {
        if (LOGED)
            DLOG.d("BitmapCache", "" + Thread.currentThread().getId() + "saveBitmapCache:" + getBitmapCachePath(param));
        File cacheFile = new File(getBitmapCachePath(param));
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

    public String getBitmapCachePath(RDBitmapParam param)
    {
        return CACHE_PATH + getFileKey(param.getFullUrl());
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

    public String getFileKey(String url)
    {
        return MD5Util.getMd5(url.getBytes());
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
