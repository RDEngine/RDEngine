package org.rdengine.android.test;

import org.rdengine.log.DLOG;
import org.rdengine.net.http.RDHttpConnection;
import org.rdengine.net.http.RDHttpParams;
import org.rdengine.net.http.RDHttpRequest;
import org.rdengine.net.http.RDHttpResponse;
import org.rdengine.net.http.RDResponseCallback;

import android.test.AndroidTestCase;

public class TestHttpCase extends AndroidTestCase
{

    private static final String SERVER_URL = "http://114.66.8.198:9876/dgnews/";

    public void testHttp()
    {
        RDHttpParams params = new RDHttpParams();
        try
        {
            params.put("userId", "");

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        // callback.onResponse(getJson("channelData"), 0, "ok", 0, false);
        RDHttpRequest request = new RDHttpRequest(RDHttpRequest.METHOD_GET, SERVER_URL + "drios/channelData", params);
        RDResponseCallback callback = new RDResponseCallback()
        {

            @Override
            public boolean onResponse(RDHttpResponse response)
            {
                if (response.getErrcode() == 0)
                {
                    DLOG.d("request", response.getResponseJson().toString());
                }
                return false;
            }
        };
        request.setResponseCallBack(callback);
        RDHttpConnection.ins().request(request);

        try
        {
            Thread.sleep(10000);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
