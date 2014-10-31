package org.rdengine.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

public class HttpClientUtil
{

    static DefaultHttpClient httpClient;

    private static void setHttpClient(Context context)
    {
        // httpClient = new DefaultHttpClient();
        httpClient = createHttpClient();
        NetType netType = NetworkControl.getNetworkType(context);
        try
        {

            if (netType != null && netType.getType() == NetType.NET_TYPE_MOBILE_WAP)
            {
                HttpHost proxy = new HttpHost(netType.getProxy(), netType.getPort());
                httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
            }

            // Proxy proxy = null;
            // SocketAddress sa = new InetSocketAddress(netType.getProxy(),
            // netType.getPort());
            // proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, sa);
            // // HttpHost proxy = new
            // HttpHost(android.net.Proxy.getDefaultHost(),
            // android.net.Proxy.getDefaultPort(), "http");
            // httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
            // proxy);
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String get(String url, String encoding, Context context) throws Exception
    {
        HttpGet httpGet = new HttpGet(url);
        setHttpClient(context);
        HttpResponse res = httpClient.execute(httpGet);
        return getContent(res, encoding);
    }

    public static String get(String url, String encoding, DefaultHttpClient client) throws Exception
    {
        HttpGet httpGet = new HttpGet(url);
        HttpResponse res = client.execute(httpGet);
        return getContent(res, encoding);
    }

    public static String post(String url, StringEntity se, String host, String referer, String encoding, Context context)
            throws Exception
    {
        setHttpClient(context);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(se);
        httpPost.setHeader("Host", host);
        httpPost.setHeader("Referer", referer);
        httpPost.setHeader("Accept", "*/*");
        httpPost.setHeader("Accept-Language", "zh-cn");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setHeader("UA-CPU", "x86");
        httpPost.setHeader(
                "User-Agent",
                "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1) ; .NET CLR 2.0.50727; InfoPath.2; CIBA)");
        httpPost.setHeader("Connection", "close");
        HttpResponse response = httpClient.execute(httpPost);

        return getContent(response, encoding);
    }

    public static String httpPost(String url, String queryString, String encoding, Context context) throws Exception
    {
        setHttpClient(context);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(queryString));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.getParams().setParameter("http.socket.timeout", Integer.valueOf(20000));
        httpPost.setHeader("Connection", "close");
        HttpResponse response = httpClient.execute(httpPost);

        return getContent(response, encoding);
    }

    public static String getContent(HttpResponse res, String encoding) throws Exception
    {
        HttpEntity ent = res.getEntity();
        String result = "";
        try
        {
            InputStream is = ent.getContent();
            StringBuilder s;
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String str = "";// in.readLine();
            s = new StringBuilder("");
            while ((str = in.readLine()) != null)
            {
                s.append(str);
            }
            // //LOG.d("oauth", ">>>  " + s.toString());
            result = new String(s.toString().getBytes(), encoding);
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ent.consumeContent();
        return result;
    }

    public static InputStream getStream(String url) throws Exception
    {
        HttpGet httpGet = new HttpGet(url);
        HttpResponse res = httpClient.execute(httpGet);
        return res.getEntity().getContent();
    }

    public static InputStream getStream(String url, DefaultHttpClient client) throws Exception
    {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(
                "User-Agent",
                "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1) ; .NET CLR 2.0.50727; InfoPath.2; CIBA)");
        httpGet.setHeader("Referer", "http://reg.126.com/regmail126/userRegist.do?action=fillinfo");
        // httpGet.setHeader("Accept", "*/*");
        // httpGet.setHeader("Accept-Language", "zh-cn");
        // httpGet.setHeader("Accept-Encoding", "gzip, deflate");
        httpGet.setHeader("Connection", "close");
        HttpResponse res = client.execute(httpGet);
        return res.getEntity().getContent();
    }

    /**
     * Create a thread-safe client. This client does not do redirecting, to allow us to capture correct "error" codes.
     * 
     * @return HttpClient
     */
    public static final DefaultHttpClient createHttpClient()
    {
        try
        {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e)
        {
            return new DefaultHttpClient();
        }
    }

    public static void main(String args[]) throws Exception
    {
        @SuppressWarnings("unused")
        String urlString = "http://www.webxml.com.cn/WebServices/IpAddressSearchWebService.asmx/getGeoIPContext";
        // String value = HttpClientUtil.get(urlString, "utf-8");
        // System.out.println(value);
    }

    public static class NetworkControl
    {

        public static boolean isNetworkAvailable(Context context)
        {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            return info != null;
        }

        public static NetType getNetworkType(Context context)
        {
            NetType netType = new NetType();

            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info == null)
                return netType;

            String type = info.getTypeName();
            if (type.equalsIgnoreCase("WIFI"))
            {
                netType.setType(NetType.NET_TYPE_WIFI);
            } else if (type.equalsIgnoreCase("MOBILE"))
            {
                // GPRS
                netType.setType(NetType.NET_TYPE_MOBILE_NET);
                String proxyHost = android.net.Proxy.getDefaultHost();
                if (!TextUtils.isEmpty(proxyHost))
                {
                    // WAP
                    netType.setType(NetType.NET_TYPE_MOBILE_WAP);
                    netType.setProxy(proxyHost);
                    netType.setPort(android.net.Proxy.getDefaultPort());
                }
            }
            return netType;
        }
    }

    public static class NetType
    {
        public static final int NET_TYPE_UNAVAILABLE = 0;
        public static final int NET_TYPE_WIFI = 1;
        public static final int NET_TYPE_MOBILE_NET = 2;
        public static final int NET_TYPE_MOBILE_WAP = 3;

        private int type = NET_TYPE_UNAVAILABLE;
        private String proxy = null;
        private int port = 0;

        public int getType()
        {
            return type;
        }

        public void setType(int type)
        {
            this.type = type;
        }

        public String getProxy()
        {
            return proxy;
        }

        public void setProxy(String proxy)
        {
            this.proxy = proxy;
        }

        public int getPort()
        {
            return port;
        }

        public void setPort(int port)
        {
            this.port = port;
        }
    }

    public static class SSLSocketFactoryEx extends SSLSocketFactory
    {

        SSLContext sslContext = SSLContext.getInstance("TLS");

        public SSLSocketFactoryEx(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException,
                KeyStoreException, UnrecoverableKeyException
        {
            super(truststore);

            TrustManager tm = new X509TrustManager()
            {
                public java.security.cert.X509Certificate[] getAcceptedIssuers()
                {
                    return null;
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
                        throws java.security.cert.CertificateException
                {}

                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                        throws java.security.cert.CertificateException
                {}
            };
            sslContext.init(null, new TrustManager[]
            { tm }, null);
        }

        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
                UnknownHostException
        {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        public Socket createSocket() throws IOException
        {
            return sslContext.getSocketFactory().createSocket();
        }
    }
}
