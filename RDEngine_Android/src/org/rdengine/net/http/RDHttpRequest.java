package org.rdengine.net.http;

import java.io.File;

import android.os.Handler;
import android.os.Looper;

public class RDHttpRequest
{

    public static final int METHOD_GET = 1;
    public static final int METHOD_POST = 2;
    public static final int METHOD_POST_FILE_MULTIPART = 3;
    public static final int METHOD_GET_DOWNLOAD = 4;

    public static final int DEFAULT_CACHETIME = 5 * 60;// 单位:second

    private int Id = 0;
    private int Method = 1;
    private int CacheTime = DEFAULT_CACHETIME;
    private String Url = null;
    private byte[] PostData = null;
    private File PostFile = null;
    private File downloadFile = null;
    private boolean Ziped = false;
    private String Key = null;
    public ResponseCallback responseCallBack = null;
    public ProcessCallback processCallback = null;

    public void setCacheTime(int cacheTime)
    {
        CacheTime = cacheTime;
    }

    public int getCacheTime()
    {
        return CacheTime;
    }

    public int getId()
    {
        return Id;
    }

    public void setId(int id)
    {
        Id = id;
    }

    public int getMethod()
    {
        return Method;
    }

    public void setMethod(int method)
    {
        Method = method;
    }

    public String getUrl()
    {
        return Url;
    }

    public void setUrl(String url, RDHttpParams getParam)
    {
        Key = RDHttpCache.genCacheKey(url, getParam);
        Url = fullUrl(url, getParam);
    }

    public void setUrl(String url)
    {
        Key = RDHttpCache.genCacheKey(url, null);
        Url = url;
    }

    public byte[] getPostData()
    {
        return PostData;
    }

    public void setPostData(byte[] postData)
    {

        PostData = postData;
    }

    public File getPostFile()
    {
        return PostFile;
    }

    public void setPostFile(File postFile)
    {
        PostFile = postFile;
    }

    public File getDownloadFile()
    {
        return downloadFile;
    }

    public void setDownloadFile(File file)
    {
        downloadFile = file;
    }

    public boolean isZiped()
    {
        return Ziped;
    }

    public void setZiped(boolean ziped)
    {
        Ziped = ziped;
    }

    public void setResponseCallBack(ResponseCallback callBack)
    {
        responseCallBack = callBack;
    }

    public void setProcessCallback(ProcessCallback callBack)
    {
        processCallback = callBack;
    }

    public void doResponseCallback(RDHttpResponse response)
    {
        if (responseCallBack != null)
        {
            if (isMainLooper())
            {
                Handler handler = new Handler(Looper.getMainLooper());
                final RDHttpResponse _response = response;
                handler.post(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        responseCallBack.onResponse(_response);
                    }
                });
            } else
            {
                responseCallBack.onResponse(response);
            }
        }
    }

    public String getKey()
    {
        return Key;
    }

    private boolean isCanceled = false;

    public boolean isCanceled()
    {
        return isCanceled;
    }

    public void cancel()
    {
        this.isCanceled = true;
    }

    /**
     * call from main UI looper
     */
    private boolean isMainLooper = false;

    public boolean isMainLooper()
    {
        return isMainLooper;
    }

    public void setMainLooper(boolean isMainLooper)
    {
        this.isMainLooper = isMainLooper;
    }

    private String fullUrl(String url, RDHttpParams param)
    {
        if (param == null)
        {
            return url;
        }
        if (url.indexOf("?") > 0)
        {
            return url + "&" + param.toUrlString();
        } else
        {
            return url + "?" + param.toUrlString();
        }
    }

    public RDHttpRequest()
    {

    }

    public RDHttpRequest(int method, String url, RDHttpParams urlParams)
    {
        this(method, url, urlParams, null, null, null);
    }

    public RDHttpRequest(int method, String url, RDHttpParams urlParams, ResponseCallback callback)
    {
        this(method, url, urlParams, null, null, callback);
    }

    public RDHttpRequest(int method, String url, RDHttpParams urlParams, byte[] post)
    {
        this(method, url, urlParams, post, null, null);
    }

    public RDHttpRequest(int method, String url, RDHttpParams urlParams, byte[] post, ResponseCallback callback)
    {
        this(method, url, urlParams, post, null, callback);
    }

    public RDHttpRequest(int method, String url, RDHttpParams urlParams, File post)
    {
        this(method, url, urlParams, null, post, null);
    }

    public RDHttpRequest(int method, String url, RDHttpParams urlParams, File post, ResponseCallback callback)
    {
        this(method, url, urlParams, null, post, callback);
    }

    public RDHttpRequest(int method, String url, RDHttpParams urlParams, byte[] postdata, File post,
            ResponseCallback callback)
    {
        setMethod(method);
        setUrl(url, urlParams);
        setPostData(postdata);
        setPostFile(post);
        setResponseCallBack(callback);
    }

    public String MultiPart_Start = "";
    public String MultiPart_End = "";
    public String MultiPart_ContentType = "";

    public void makeBoundary()
    {
        if (getMethod() != METHOD_POST_FILE_MULTIPART || PostFile == null)
        {
            return;
        }
        String boundary = "---------" + System.currentTimeMillis();
        MultiPart_ContentType = "multipart/form-data; boundary=" + boundary;
        MultiPart_Start = "--" + boundary;
        MultiPart_Start += "\r\n";
        MultiPart_Start += "Content-Disposition: form-data; name=\"file\"; filename=\"" + PostFile.getPath() + "\"";
        MultiPart_Start += "\r\n";
        MultiPart_Start += "Content-Type: application/octet-stream";
        MultiPart_Start += "\r\n";
        MultiPart_Start += "\r\n";

        MultiPart_End = "\r\n--" + boundary + "--\r\n";
    }

}
