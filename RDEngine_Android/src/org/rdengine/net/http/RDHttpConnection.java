package org.rdengine.net.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.rdengine.log.DLOG;
import org.rdengine.util.StringUtil;

import android.os.Looper;

/**
 * Http
 * 
 * @author yangyu
 */
public class RDHttpConnection
{

    private static final String LOG_TAG = "http";
    public static boolean LOGED = true;
    private static final int GET_BLOCK = 10 * 2014;
    private static final int POST_BLOCK = 10 * 2014;
    private static final int CONN_TIMEOUT = 30 * 1000;

    public static final boolean DEFAULT_READCACHE = false;
    public static final int DEFAULT_CACHETIME = 5 * 60;// 单位:second
    public static final int CACHETIME_MONTH = 60 * 60 * 24 * 30;// 一个月

    public static final int ERR_OK = 0;
    public static final int ERR_NETWORK_NOT_AVAILABLE = -1;
    public static final int ERR_REQUEST_IS_CANCELED = -2;
    public static final int ERR_NO_DOWNLOAD_FILE = -3;
    public static final int ERR_HTTP_EXCEPTION = -4;

    private static RDHttpConnection instance = null;

    private ArrayBlockingQueue<Runnable> httpQueue = new ArrayBlockingQueue<Runnable>(100);
    private ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 5, 10, TimeUnit.SECONDS, httpQueue,
            new ThreadPoolExecutor.CallerRunsPolicy());

    private static HttpMessageSet msgSet = new HttpMessageSet();

    public static void setMessageSet(HttpMessageSet set)
    {
        msgSet = set;
    }

    public static class HttpMessageSet
    {
        public String noNetwork = "no network";
        public String noDownloadFile = "download file handle not found";
        public String OK = "OK";
    }

    public static synchronized RDHttpConnection ins()
    {
        if (instance != null)
        {
            return instance;
        } else
        {
            synchronized (RDHttpConnection.class)
            {
                if (instance == null)
                {
                    instance = new RDHttpConnection();
                }
                return instance;
            }
        }
    }

    private RDHttpConnection()
    {

    }

    // method--------------------------------------------------------------------------

    public synchronized void cancel(RDHttpRequest request)
    {
        if (httpQueue.contains(request))
        {
            httpQueue.remove(request);
        }
    }

    public synchronized void request(RDHttpRequest request)
    {
        Runnable runable = null;
        final RDHttpRequest _request = request;

        if (Looper.myLooper() == Looper.getMainLooper())
        {
            _request.setMainLooper(true);
        } else
        {
            _request.setMainLooper(false);
        }

        runable = new Runnable()
        {
            @Override
            public void run()
            {
                doRequest(_request);
            }
        };
        threadPool.execute(runable);
    }

    public RDHttpResponse requestSync(RDHttpRequest request)
    {
        return doRequest(request);
    }

    // method--------------------------------------------------------------------------

    private RDHttpResponse doRequest(RDHttpRequest request)
    {

        RDHttpResponse response = new RDHttpResponse();
        response.setRequest(request);

        if (LOGED)
        {
            DLOG.d("HTTP", "url:" + request.getUrl());
        }

        if (request.isReadCache() && request.getMethod() == RDHttpRequest.METHOD_GET)
        {
            byte[] cache = RDHttpCache.ins().readCache(request.getKey());
            if (cache != null && cache.length > 0)
            {
                response.setErrcode(ERR_OK);
                response.setErrMsg(msgSet.OK);
                response.setResponseData(cache);
                response.setFromCache(true);
                request.doResponseCallback(response);
                return response;
            }
        }
        // 取消请求
        if (response.getRequest().isCanceled())
        {
            response.setErrcode(ERR_REQUEST_IS_CANCELED);
            return response;
        }

        // 网络不可用
        if (!NetworkState.ins().getNetworkAvailable())
        {
            response.setErrcode(ERR_NETWORK_NOT_AVAILABLE);
            response.setErrMsg(msgSet.noNetwork);
            response.setResponseData(null);
            response.setFromCache(false);
            request.doResponseCallback(response);
            return response;
        }

        HttpURLConnection conn = null;
        InputStream input = null;
        try
        {
            URL url = new URL(request.getUrl());
            conn = (HttpURLConnection) url.openConnection();
            switch (request.getMethod())
            {
            case RDHttpRequest.METHOD_GET :
            case RDHttpRequest.METHOD_GET_DOWNLOAD :
                conn.setRequestMethod("GET");
                conn.setDoOutput(false);
                break;
            case RDHttpRequest.METHOD_POST :
            case RDHttpRequest.METHOD_POST_FILE_MULTIPART :
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                break;
            }

            conn.setDoInput(true);

            conn.setUseCaches(false);
            conn.setConnectTimeout(CONN_TIMEOUT);
            conn.setReadTimeout(CONN_TIMEOUT);
            conn.setInstanceFollowRedirects(true);
            switch (request.getMethod())
            {
            case RDHttpRequest.METHOD_GET :
            case RDHttpRequest.METHOD_GET_DOWNLOAD :
            case RDHttpRequest.METHOD_POST :
                if (request.isZiped())
                {
                    conn.addRequestProperty("Content-Encoding", "gzip");
                    conn.addRequestProperty("Accept-Encoding", "gzip");
                }
                break;
            case RDHttpRequest.METHOD_POST_FILE_MULTIPART :
                request.makeBoundary();
                conn.addRequestProperty("Content-Type", request.MultiPart_ContentType);
                break;
            }

            conn.connect();

            InputStream postIs = null;
            long postLen = 0;

            switch (request.getMethod())
            {
            case RDHttpRequest.METHOD_POST :
                if (request.getPostData() != null)
                {
                    postLen = request.getPostData().length;
                    postIs = new ByteArrayInputStream(request.getPostData());
                }
                break;
            case RDHttpRequest.METHOD_POST_FILE_MULTIPART :
                if (request.getPostFile() != null)
                {
                    if (request.getPostFile().exists())
                    {
                        postLen = request.getPostFile().length();
                        postIs = new FileInputStream(request.getPostFile());
                    }
                }
                break;
            }

            if (request.getMethod() == RDHttpRequest.METHOD_POST
                    || request.getMethod() == RDHttpRequest.METHOD_POST_FILE_MULTIPART)
            {
                OutputStream os = null;
                boolean postOK = false;
                try
                {
                    if (postIs != null)
                    {
                        os = conn.getOutputStream();

                        if (request.getMethod() == RDHttpRequest.METHOD_POST_FILE_MULTIPART
                                && !StringUtil.isEmpty(request.MultiPart_Start))
                        {
                            os.write(request.MultiPart_Start.getBytes());
                        }
                        byte[] buff = new byte[POST_BLOCK];
                        int offset = 0;
                        int len = 0;
                        while (true)
                        {
                            len = postIs.read(buff);
                            if (len == -1)
                            {
                                break;
                            }
                            offset += len;
                            os.write(buff, 0, len);
                            if (postLen <= 0)
                            {
                                response.setPostTotal(1);
                                response.setPostFinished(1);
                            } else
                            {
                                response.setPostTotal(postLen);
                                response.setPostFinished(offset);
                            }
                        }
                        if (request.getMethod() == RDHttpRequest.METHOD_POST_FILE_MULTIPART
                                && !StringUtil.isEmpty(request.MultiPart_End))
                        {
                            os.write(request.MultiPart_End.getBytes());
                        }
                    }
                    postOK = true;
                } catch (Exception ex)
                {
                    postOK = false;
                    response.setErrcode(ERR_HTTP_EXCEPTION);
                    response.setErrMsg(ex.toString());
                    response.setResponseData(null);
                    response.setFromCache(false);
                    request.doResponseCallback(response);
                    ex.printStackTrace();
                } finally
                {
                    if (postIs != null)
                    {
                        postIs.close();
                    }
                    if (os != null)
                    {
                        os.close();
                    }
                }
                if (!postOK)
                {
                    conn.disconnect();
                    return response;
                }
            }

            int code = conn.getResponseCode();

            if (code / 100 != 2)
            {
                DLOG.d("HTTP", "errCode:" + code);
                response.setErrcode(code);
                response.setErrMsg(conn.getResponseMessage());
                response.setResponseData(null);
                response.setFromCache(false);
                request.doResponseCallback(response);
                conn.disconnect();

                return response;
            }
            boolean outputZiped = false;
            if ("gzip".equals(conn.getContentEncoding()))
            {
                outputZiped = true;
                DLOG.d(LOG_TAG, "reviced gzip");
            }
            input = conn.getInputStream();
            if (outputZiped)
            {
                input = new GZIPInputStream(input);
            }
            int total = conn.getContentLength();
            if (total <= 0)
            {
                total = input.available();
            }
            response.setResponseFinished(0);
            response.setResponseTotal(total);

            OutputStream os = null;
            switch (request.getMethod())
            {
            case RDHttpRequest.METHOD_GET :
            case RDHttpRequest.METHOD_POST :
            case RDHttpRequest.METHOD_POST_FILE_MULTIPART :
                os = new ByteArrayOutputStream();
                break;
            case RDHttpRequest.METHOD_GET_DOWNLOAD :
                if (request.getDownloadFile() == null)
                {
                    response.setErrcode(ERR_NO_DOWNLOAD_FILE);
                    response.setErrMsg(msgSet.noDownloadFile);
                    response.setResponseData(null);
                    request.doResponseCallback(response);
                    input.close();
                    input = null;
                    conn.disconnect();
                    return response;
                }
                File _file = request.getDownloadFile();
                if (_file.exists())
                {
                    _file.delete();
                }
                _file.createNewFile();
                os = new FileOutputStream(_file);
                break;
            }

            byte[] buff = new byte[GET_BLOCK];
            int len = 0;
            int offset = 0;
            while (true)
            {
                len = input.read(buff);
                if (len == -1)
                {
                    break;
                }
                os.write(buff, 0, len);
                offset += len;
                if (total <= 0)
                {
                    response.setResponseFinished(offset);
                    response.setResponseTotal(total);
                } else
                {
                    response.setResponseFinished(offset);
                    response.setResponseTotal(total);
                }
            }

            switch (request.getMethod())
            {
            case RDHttpRequest.METHOD_GET :
            case RDHttpRequest.METHOD_POST :
            case RDHttpRequest.METHOD_POST_FILE_MULTIPART :
                byte[] data = ((ByteArrayOutputStream) os).toByteArray();
                if (data != null)
                {
                    if (!StringUtil.isEmpty(request.getKey()) && request.getCacheTime() > 0)
                    {
                        RDHttpCache.ins().addCache(request.getKey(), data, request.getCacheTime());
                    }
                }
                response.setErrcode(ERR_OK);
                response.setErrMsg(msgSet.OK);
                response.setResponseData(data);
                response.setFromCache(false);
                request.doResponseCallback(response);
                break;
            case RDHttpRequest.METHOD_GET_DOWNLOAD :
                response.setErrcode(ERR_OK);
                response.setErrMsg(msgSet.OK);
                response.setResponseData(null);
                response.setFromCache(false);
                request.doResponseCallback(response);
                break;
            }
            os.close();
        } catch (Exception e)
        {
            response.setErrcode(ERR_HTTP_EXCEPTION);
            response.setErrMsg(e.toString());
            response.setResponseData(null);
            response.setFromCache(false);
            request.doResponseCallback(response);
            e.printStackTrace();
        } finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (conn != null)
            {
                conn.disconnect();
            }
        }
        return response;
    }

}
