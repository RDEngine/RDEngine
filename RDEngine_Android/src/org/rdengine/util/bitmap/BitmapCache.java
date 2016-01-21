package org.rdengine.util.bitmap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.rdengine.net.http.RDHttpConnection;
import org.rdengine.net.http.RDHttpRequest;
import org.rdengine.net.http.RDHttpResponse;
import org.rdengine.net.http.ResponseCallback;
import org.rdengine.runtime.RT;
import org.rdengine.util.MD5Util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.widget.ImageView;

/**
 * 6.0图片缓存管理
 * 
 * @author yangyu
 */
public class BitmapCache
{

    private static BitmapCache BitmapCache = null;//

    private static Hashtable<Integer, SoftReference<Bitmap>> Caches = null;// Bitmap缓存池(软引用)

    private static ReferenceQueue<Cache> RefQueue = null;

    // private static ArrayList<Cache> StrongRefs = null;// 强引用索引,防止GC过强的手机对软引用释放的过快

    private static ArrayList<BindView> BindViews = null;// 回调ImageView池,异步setImageBitmap()

    // private static ArrayList<BindView> ReleaseList = null;// 手动释放索引

    private final static int MAX_STRONGREF_SIZE = 10;// 强引用索引数量上限

    private final static int STRONGREF_TIME = 5 * 1000;// 强引用超时

    private final static boolean BITMAP_SAFE_SIZE = true;

    private BitmapCache()
    {
        if (Caches == null)
        {
            Caches = new Hashtable<Integer, SoftReference<Bitmap>>();
        } else
        {
            Enumeration<SoftReference<Bitmap>> enu = Caches.elements();
            while (enu.hasMoreElements())
            {
                if (enu.nextElement().get() != null)
                {
                    enu.nextElement().get().recycle();
                    enu.nextElement().clear();
                }
            }
            Caches.clear();
        }

        // if (StrongRefs == null)
        // {
        // StrongRefs = new ArrayList<Cache>();
        // } else
        // {
        // StrongRefs.clear();
        // }

        if (RefQueue == null)
        {
            RefQueue = new ReferenceQueue<Cache>();
        }

        if (BindViews == null)
        {
            BindViews = new ArrayList<BindView>();
        }
        // if (ReleaseList == null)
        // {
        // ReleaseList = new ArrayList<BindView>();
        // }

    }

    private ResponseCallback preloadCallback = new ResponseCallback()
    {

        public boolean onByteArrayResponse(byte[] result, int httperr, String errmsg, int id, boolean fromcache)
        {
            if (result != null)
            {
                saveBitmapCache(id, result);
            }
            return false;
        }

        @Override
        public boolean onResponse(RDHttpResponse response)
        {
            if (response.getResponseData() != null)
            {
                saveBitmapCache(response.getRequest().getId(), response.getResponseData());
            }
            return false;
        }
    };

    private ResponseCallback responseCallback = new ResponseCallback()
    {

        @Override
        public boolean onResponse(RDHttpResponse response)
        {
            if (response.getErrcode() == 0 && response.getResponseData() != null)
            {
                BindView bind = null;
                Bitmap bitmap = null;
                synchronized (BindViews)
                {
                    for (int i = 0; i < BindViews.size(); i++)
                    {
                        bind = BindViews.get(i);
                        if (bind.getKey() == response.getRequest().getId())
                        {
                            boolean safe_save = false;
                            try
                            {
                                float sample = 1.0f;
                                try
                                {
                                    if (bind.getWidth() > 0 && bind.getHeight() > 0)
                                    {
                                        BitmapRegionDecoder decoder = BitmapRegionDecoder
                                                .newInstance(response.getResponseData(), 0,
                                                        response.getResponseData().length, false);
                                        int oWidth = decoder.getWidth();
                                        int oHeight = decoder.getHeight();
                                        float sw = (float) oWidth / bind.getWidth();
                                        float sh = (float) oHeight / bind.getHeight();
                                        if (sw > sh)
                                        {
                                            sample = sh;
                                        } else
                                        {
                                            sample = sw;
                                        }
                                    }

                                } catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                                Options op = new Options();
                                op.inSampleSize = (int) sample;
                                bitmap = BitmapFactory.decodeByteArray(response.getResponseData(), 0,
                                        response.getResponseData().length, op);

                            } catch (Throwable ex)
                            {
                                if (BITMAP_SAFE_SIZE)
                                {
                                    int max_w = RT.application.getResources().getDisplayMetrics().widthPixels;
                                    int max_h = RT.application.getResources().getDisplayMetrics().heightPixels;
                                    BitmapSize bs = new BitmapSize(max_w, max_h);
                                    bitmap = BitmapDecoder.decodeSampledBitmapFromByteArray(response.getResponseData(),
                                            bs, Bitmap.Config.ARGB_8888);
                                    safe_save = true;
                                }
                            }
                            if (bitmap != null)
                            {
                                if (response.getResponseData() != null && response.getResponseData() instanceof byte[])
                                {
                                    if (safe_save)
                                        saveBitmapCache(response.getRequest().getId(), Bitmap2Bytes(bitmap));
                                    else saveBitmapCache(response.getRequest().getId(), response.getResponseData());
                                }

                                Caches.put(response.getRequest().getId(), new SoftReference<Bitmap>(bitmap));
                                addStrongRef(response.getRequest().getId(), bitmap);
                                if (bind.getImageView() != null)
                                {
                                    addReleaseTagList(response.getRequest().getId(), bind.getImageView(),
                                            bind.getReleaseTag());
                                    if (bind.getIsRound())
                                    {
                                        bitmap = toRoundBitmap(bitmap);
                                    }
                                    if (bind.getCallback() != null)
                                    {
                                        bind.getCallback().callback(bitmap, response.getRequest().getId());
                                    }
                                    bind.getImageView().setImageBitmap(bitmap);

                                }
                            } else
                            {
                                if (bind.getCallback() != null)
                                {
                                    bind.getCallback().callback(null, response.getRequest().getId());
                                }
                            }
                            BindViews.remove(i);
                            i--;
                        }
                    }
                }
            } else
            {
                BindView bind = null;

                synchronized (BindViews)
                {
                    for (int i = 0; i < BindViews.size(); i++)
                    {
                        bind = BindViews.get(i);
                        if (bind.getKey() == response.getRequest().getId())
                        {
                            if (bind.getCallback() != null)
                            {
                                bind.getCallback().callback(null, response.getRequest().getId());
                            }
                            BindViews.remove(i);
                            i--;
                        }
                    }
                }
            }
            return false;
        }
    };

    public int getKeyByParam(BitmapParam param)
    {
        int key = 0;
        if (param.isFromRes())
        {
            key = getHashKey(param.getResId());
        } else
        {
            key = getHashKey(param.getUrl(), param.getBitmapWidth(), param.getBitmapHeight());
        }
        return key;
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
     * 直接获取一张图，bitmap在回调里，只供widget和通知栏使用
     * 
     * @param param
     */
    public void getBitmap(final BitmapParam param)
    {
        String url = param.getUrl();
        int width = param.getBitmapWidth();
        int height = param.getBitmapHeight();
        int mode = param.getMode();
        final boolean isRound = param.getIsRound();

        if (url == null || url.length() == 0)
        {
            if (param.getCallback() != null)
            {
                param.getCallback().callback(null, param.getKey());
            }
            return;
        }
        int key = getHashKey(url, width, height);

        Bitmap bmp = null;
        if (Caches.get(key) != null)
        {
            bmp = Caches.get(key).get();
        }
        if (bmp == null || bmp.isRecycled())
        {
            Caches.remove(key);
        } else
        {
            addStrongRef(key, bmp);
            if (isRound)
            {
                bmp = toRoundBitmap(bmp);
            }
            if (param.getCallback() != null)
            {
                param.getCallback().callback(bmp, key);
            }
            return;
        }
        synchronized (BindViews)
        {
            bmp = readLocalBitmapCache(key, width, height);
            if (bmp == null || bmp.isRecycled())
            {
                ResponseCallback otherListener = new ResponseCallback()
                {

                    @Override
                    public boolean onResponse(RDHttpResponse response)
                    {
                        if (response.getErrcode() == 0 && response.getResponseData() != null)
                        {
                            Bitmap bitmap = null;
                            boolean safe_save = false;
                            try
                            {
                                bitmap = BitmapFactory.decodeByteArray(response.getResponseData(), 0,
                                        response.getResponseData().length);
                            } catch (Throwable e)
                            {
                                if (BITMAP_SAFE_SIZE)
                                {
                                    int max_w = RT.application.getResources().getDisplayMetrics().widthPixels;
                                    int max_h = RT.application.getResources().getDisplayMetrics().heightPixels;
                                    BitmapSize bs = new BitmapSize(max_w, max_h);
                                    bitmap = BitmapDecoder.decodeSampledBitmapFromByteArray(response.getResponseData(),
                                            bs, Bitmap.Config.ARGB_8888);
                                    safe_save = true;
                                }
                            }

                            if (bitmap != null)
                            {
                                if (!safe_save)
                                {
                                    saveBitmapCache(response.getRequest().getId(), response.getResponseData());
                                } else
                                {
                                    saveBitmapCache(response.getRequest().getId(), Bitmap2Bytes(bitmap));
                                }

                                Caches.put(response.getRequest().getId(), new SoftReference<Bitmap>(bitmap));
                                addStrongRef(response.getRequest().getId(), bitmap);
                                if (isRound)
                                {
                                    bitmap = toRoundBitmap(bitmap);
                                }

                                if (param.getCallback() != null)
                                {
                                    param.getCallback().callback(bitmap, response.getRequest().getId());
                                }
                            }
                        } else
                        {
                            if (param.getCallback() != null)
                            {
                                param.getCallback().callback(null, response.getRequest().getId());
                            }
                        }
                        return false;
                    }

                };

                RDHttpRequest request = new RDHttpRequest(RDHttpRequest.METHOD_GET, BitmapParam.productPicUrl(mode,
                        url, width, height), null);
                request.setReadCache(false);
                request.setId(key);
                request.setCacheTime(0);
                request.setResponseCallBack(otherListener);
                RDHttpConnection.ins().request(request);
            } else
            {
                addStrongRef(key, bmp);
                if (isRound)
                {
                    bmp = toRoundBitmap(bmp);
                }

                if (param.getCallback() != null)
                {
                    param.getCallback().callback(bmp, key);
                }

                return;
            }

        }
    }

    /**
     * 为ImageView加载图片
     * 
     * @param param
     *            Bitmap参数
     * @param view
     *            目标ImageView
     */
    public void getBitmap(BitmapParam param, ImageView view)
    {
        if (param.isFromRes())
        {
            getBitmap(param.getResId(), param.getContext(), view, param.getReleaseTag());
        } else
        {
            // Log.d("bitmapcache", "w:" + param.getBitmapWidth() + " h:" + param.getBitmapHeight());

            getBitmap(param.getUrl(), param.getBitmapWidth(), param.getBitmapHeight(), view, param.getDefaultImage(),
                    param.getReleaseTag(), param.getIsRound(), param.getMode(), param.getCallback());
        }
    }

    /**
     * 为ImageView加载图片
     * 
     * @param resid
     *            资源id
     * @param context
     *            资源context
     * @param view
     *            目标ImageView
     * @param releaseTag
     *            手动释放Tag
     */
    public void getBitmap(int resid, Context context, ImageView view, String releaseTag)
    {
        if (view == null || context == null)
        {
            return;
        }
        int key = getHashKey(resid);
        Bitmap bmp = Caches.get(key).get();
        if (bmp == null)
        {
            Caches.remove(key);
        } else
        {
            addStrongRef(key, bmp);
            view.setImageBitmap(bmp);
            return;
        }
        bmp = BitmapFactory.decodeResource(context.getResources(), resid);
        if (bmp == null)
        {

        } else
        {
            addStrongRef(key, bmp);
            view.setImageBitmap(bmp);
            addReleaseTagList(key, view, releaseTag);
            Caches.put(key, new SoftReference<Bitmap>(bmp));
        }
    }

    /**
     * 为ImageView加载图片
     * 
     * @param url
     *            Bitmap Url
     * @param width
     *            Bitmap宽px
     * @param height
     *            Bitmap高px
     * @param view
     *            目标ImageView
     * @param defaultImage
     *            默认图片Resource Id
     * @param releaseTag
     *            手动释放Tag
     */
    public void getBitmap(String url, int width, int height, ImageView view, int defaultImage, String releaseTag,
            boolean isRound, int mode, BitmapCallback callback)
    {
        if (url == null || url.length() == 0 || view == null)
        {
            if (defaultImage > 0)
            {
                view.setImageResource(defaultImage);
            }
            return;
        }
        int key = getHashKey(url, width, height);

        Bitmap bmp = null;
        if (Caches.get(key) != null)
        {
            bmp = Caches.get(key).get();
        }
        if (bmp == null || bmp.isRecycled())
        {
            Caches.remove(key);
        } else
        {
            addStrongRef(key, bmp);
            addReleaseTagList(key, view, releaseTag);
            if (isRound)
            {
                bmp = toRoundBitmap(bmp);
            }
            if (callback != null)
            {
                callback.callback(bmp, key);
            }
            view.setImageBitmap(bmp);
            return;
        }
        BindView bind = null;
        synchronized (BindViews)
        {
            for (int i = 0; i < BindViews.size(); i++)
            {
                bind = BindViews.get(i);
                if (bind.getImageView() == view)
                {
                    BindViews.remove(i);
                }
            }

            bmp = readLocalBitmapCache(key, width, height);
            if (bmp == null || bmp.isRecycled())
            {
                BindView bv = new BindView(key, view, releaseTag, isRound, callback);
                bv.setWidth(width);
                bv.setHeight(height);
                BindViews.add(bv);
                if (defaultImage > 0)
                {
                    view.setImageResource(defaultImage);
                }
                // Log.d("bitmapcache", "net:" + url + " w:" + width + " h:" + height);

                RDHttpRequest request = new RDHttpRequest(RDHttpRequest.METHOD_GET, BitmapParam.productPicUrl(mode,
                        url, width, height), null);
                request.setReadCache(false);
                request.setId(key);
                request.setCacheTime(0);
                request.setResponseCallBack(responseCallback);
                RDHttpConnection.ins().request(request);

            } else
            {
                addStrongRef(key, bmp);
                addReleaseTagList(key, view, releaseTag);
                if (isRound)
                {
                    bmp = toRoundBitmap(bmp);
                }
                if (callback != null)
                {
                    callback.callback(bmp, key);
                }
                view.setImageBitmap(bmp);
            }

        }

    }

    /**
     * 预取图片
     * 
     * @param url
     * @param width
     * @param height
     */
    public void preloadBitmap(String url, int width, int height)
    {
        int key = getHashKey(url, width, height);
        RDHttpRequest request = new RDHttpRequest();
        request.setUrl(url, null);
        request.setReadCache(false);
        request.setCacheTime(0);
        request.setId(key);
        request.setResponseCallBack(preloadCallback);
        RDHttpConnection.ins().request(request);
    }

    public int getHashKey(String url, int width, int height)
    {
        int hash = 0;
        hash = (url + width + height).hashCode();
        return hash;
    }

    public int getHashKey(int resid)
    {
        int hash = 0;
        hash = ("res:" + resid).hashCode();
        return hash;
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
        // if (ReleaseList != null)
        // {
        // BindView bv = null;
        // for (int i = 0; i < ReleaseList.size(); i++)
        // {
        // bv = ReleaseList.get(i);
        // if (releaseTag.equals(bv.getReleaseTag()))
        // {
        // if (bv.getImageView() != null)
        // {
        // bv.getImageView().setImageBitmap(null);
        // }
        // ReleaseList.remove(i);
        // i--;
        // }
        //
        // if (bv.getImageView() == null)
        // {
        // ReleaseList.remove(i);
        // i--;
        // }
        // }
        // }
    }

    // no use
    private void clear()
    {
        // if (StrongRefs != null)
        // {
        // StrongRefs.clear();
        // }
        if (Caches != null)
        {
            Caches.clear();
        }

    }

    // no use
    private void freeMemmory()
    {
        // ArrayList<Integer> sKeys = new ArrayList<Integer>();
        // for (int i = 0; i < StrongRefs.size(); i++)
        // {
        // sKeys.add(StrongRefs.get(i).getKey());
        // }
        // Enumeration<Integer> keys = Caches.keys();
        // int key = 0;
        // while (keys.hasMoreElements())
        // {
        // key = keys.nextElement();
        // if (!sKeys.contains(key))
        // {
        // Caches.remove(key);
        // }
        // }

    }

    private void cleanCaches()
    {
        Enumeration<Integer> keys = Caches.keys();
        int key = 0;
        while (keys.hasMoreElements())
        {
            key = keys.nextElement();
            if (Caches.get(key).get() == null)
            {
                Caches.remove(key);
            }
        }
    }

    private void refreshReference()
    {
        Cache cache = null;
        // synchronized (StrongRefs)
        // {
        // while (StrongRefs.size() > MAX_STRONGREF_SIZE)
        // {
        // StrongRefs.remove(0);
        // }
        // for (int i = 0; i < StrongRefs.size(); i++)
        // {
        // cache = StrongRefs.get(i);
        // if (cache.updateTime < System.currentTimeMillis() - STRONGREF_TIME)
        // {
        // StrongRefs.remove(i);
        // i--;
        // } else
        // {
        // break;
        // }
        // }
        // }
        cleanCaches();
    }

    private void addStrongRef(int key, Bitmap bitmap)
    {
        // Cache cache = null;
        // for (int i = 0; i < StrongRefs.size(); i++)
        // {
        // cache = StrongRefs.get(i);
        // if (cache.getBitmap() == bitmap)
        // {
        // StrongRefs.remove(i);
        // cache.updateTime = System.currentTimeMillis();
        // StrongRefs.add(cache);
        // return;
        // }
        // }
        //
        // cache = new Cache(key, bitmap, System.currentTimeMillis());
        // StrongRefs.add(cache);

        refreshReference();
    }

    public static String Cache_Path = "";

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    private Bitmap readLocalBitmapCache(int key, int width, int height)
    {
        String filename = Cache_Path.concat("/").concat(MD5Util.getMd5((Integer.toString(key)).getBytes()));
        File file = new File(filename);

        if (file.exists())
        {
            Bitmap bmp = null;
            try
            {
                float sample = 1f;
                try
                {
                    if (width > 0 && height > 0)
                    {
                        BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(filename, false);
                        int oWidth = decoder.getWidth();
                        int oHeight = decoder.getHeight();
                        float sw = (float) oWidth / width;
                        float sh = (float) oHeight / height;
                        if (sw > sh)
                        {
                            sample = sh;
                        } else
                        {
                            sample = sw;
                        }
                    }

                } catch (Throwable e)
                {
                    e.printStackTrace();
                }
                Options op = new Options();
                op.inSampleSize = (int) sample;
                bmp = BitmapFactory.decodeFile(filename, op);
            } catch (Throwable e)
            {
                if (BITMAP_SAFE_SIZE)
                {
                    int max_w = RT.application.getResources().getDisplayMetrics().widthPixels;
                    int max_h = RT.application.getResources().getDisplayMetrics().heightPixels;
                    BitmapSize bs = new BitmapSize(max_w, max_h);
                    bmp = BitmapDecoder.decodeSampledBitmapFromFile(filename, bs, Bitmap.Config.ARGB_8888);
                }
            }

            if (bmp == null)
            {
                file.delete();
                return null;
            }

            file.setLastModified(System.currentTimeMillis());
            Caches.put(key, new SoftReference<Bitmap>(bmp));
            return bmp;
        } else
        {
            return null;
        }

    }

    private void saveBitmapCache(int key, byte[] data)
    {
        String filename = Cache_Path.concat("/").concat(MD5Util.getMd5((Integer.toString(key)).getBytes()));
        File file = new File(filename);
        if (file.exists())
        {
            file.delete();
        }
        try
        {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private void addReleaseTagList(int key, ImageView view, String releaseTag)
    {
        // if (releaseTag != null && releaseTag.length() > 0)
        // {
        // BindView bv = null;
        // boolean hasAdded = false;
        // for (int i = 0; i < ReleaseList.size(); i++)
        // {
        // bv = ReleaseList.get(i);
        // if (bv.getImageView() == null)
        // {
        // ReleaseList.remove(i);
        // i--;
        // continue;
        // } else
        // {
        // if (view == bv.getImageView())
        // {
        // bv.ReleaseTag = releaseTag;
        // hasAdded = true;
        // break;
        // }
        // }
        // }
        // // 增加释放TAG,可以手工释放
        // if (!hasAdded)
        // {
        // bv = new BindView(key, view, releaseTag);
        // ReleaseList.add(bv);
        // }
        // }
    }

    public static BitmapCache ins()
    {
        if (BitmapCache == null)
        {
            BitmapCache = new BitmapCache();
        }
        return BitmapCache;
    }

    public class Cache
    {
        private Bitmap bitmap = null;
        private long updateTime = 0;
        private int key = 0;

        public Cache(int key, Bitmap bmp, long time)
        {
            this.key = key;
            updateTime = time;
            bitmap = bmp;
        }

        public long getUpdateTime()
        {
            return updateTime;
        }

        public Bitmap getBitmap()
        {
            return bitmap;
        }

        public int getKey()
        {
            return key;
        }
    }

    private class BindView
    {
        private int Key = 0;
        private WeakReference<ImageView> ImageView = null;
        private String ReleaseTag = null;
        private boolean IsRound = false;
        private int Width = 0;
        private int Height = 0;

        public int getWidth()
        {
            return Width;
        }

        public void setWidth(int width)
        {
            Width = width;
        }

        public int getHeight()
        {
            return Height;
        }

        public void setHeight(int height)
        {
            Height = height;
        }

        private BitmapCallback Callback = null;

        public BindView(int key, ImageView view)
        {
            Key = key;
            ImageView = new WeakReference<ImageView>(view);
        }

        public BindView(int key, ImageView view, String releaseTag)
        {
            Key = key;
            ImageView = new WeakReference<ImageView>(view);
            ReleaseTag = releaseTag;
        }

        public BindView(int key, ImageView view, String releaseTag, boolean isRound, BitmapCallback callback)
        {
            Key = key;
            ImageView = new WeakReference<ImageView>(view);
            ReleaseTag = releaseTag;
            this.IsRound = isRound;
            this.Callback = callback;
        }

        public int getKey()
        {
            return Key;
        }

        public ImageView getImageView()
        {
            return ImageView.get();
        }

        public String getReleaseTag()
        {
            return ReleaseTag;
        }

        public boolean getIsRound()
        {
            return IsRound;
        }

        public void setIsRound(boolean isRound)
        {
            IsRound = isRound;
        }

        public BitmapCallback getCallback()
        {
            return Callback;
        }

        public void setCallback(BitmapCallback callback)
        {
            Callback = callback;
        }

    }

    public static Bitmap toRoundBitmap(Bitmap bitmap)
    {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height)
        {
            roundPx = width / 2;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else
        {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff000000;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);

        paint.setAntiAlias(true);

        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        return output;
    }

    public byte[] Bitmap2Bytes(Bitmap bm)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
