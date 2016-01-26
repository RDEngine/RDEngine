package org.rdengine.util.bitmap;

import org.rdengine.runtime.RT;

/**
 * BitmapCache请求参数描述
 * 
 * @author yangyu
 */
public class RDBitmapParam
{
    public final static String LOCAL_PREFIX = "localfile:";

    public final static int MODE_NORMAL = 0;// 不等比缩放
    public final static int MODE_CHECK = 1;// 等比缩放

    private String Url = null;
    private int DefaultImage = 0;
    private String ReleaseTag = null;
    private int Width = 0;
    private int Height = 0;
    private int mode = 0;// 剪裁模式 0：不等比缩放， 1：等比缩放

    public RDBitmapParam()
    {}

    public RDBitmapParam(String url)
    {
        this.Url = url;
    }

    public void setPXSize(int width, int height)
    {
        Width = width;
        Height = height;
    }

    public void setDPSize(int width, int height)
    {
        Width = (int) (width / RT.application.getResources().getDisplayMetrics().density + 0.5f);
        Height = (int) (height / RT.application.getResources().getDisplayMetrics().density + 0.5f);
    }

    public void setUrl(String url)
    {
        this.Url = url;
    }

    public String getUrl()
    {
        return Url;
    }

    public String getFullUrl()
    {
        return productPicUrl(mode, Url, Width, Height);
    }

    public void setDefaultImage(int resid)
    {
        this.DefaultImage = resid;
    }

    public int getDefaultImage()
    {
        return this.DefaultImage;
    }

    public int getMode()
    {
        return mode;
    }

    public void setMode(int mode)
    {
        this.mode = mode;
    }

    /**
     * 设置释放Tag,标识了Tag的ImageView会被索引,当调用BitmapCache.releaseImageViewByTag(tag);时,会对设置了相同Tag的ImageView做setImageBitmap(null);
     * 
     * @param tag
     */
    public void setReleasedTag(String tag)
    {
        ReleaseTag = tag;
    }

    public String getReleaseTag()
    {
        return ReleaseTag;
    }

    public int getWidth()
    {
        return Width;
    }

    public int getHeight()
    {
        return Height;
    }

    /**
     * 拼接图片剪裁参数 ?imageView2/1/w/200/h/200
     * 
     * @param url
     * @param width
     * @param height
     * @return
     */
    public static String productPicUrl(int mode, String url, int width, int height)
    {
        // if (width <= 0 || height <= 0)
        // {
        // return url;
        // }
        // StringBuilder c = new StringBuilder().append(url);
        // if (mode == MODE_NORMAL)
        // {
        // c.append("?imageView2/1").append("/w/" + width).append("/h/" + height);
        // } else
        // {
        // c.append("?imageView2/0").append("/w/" + width);
        // }
        // DLOG.d("image", "url:" + c.toString());
        // return c.toString();
        return url;
    }
}
