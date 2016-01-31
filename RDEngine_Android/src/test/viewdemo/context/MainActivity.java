package test.viewdemo.context;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

import org.rdengine.android.R;
import org.rdengine.log.DLOG;
import org.rdengine.net.http.RDHttpConnection;
import org.rdengine.net.http.RDHttpParams;
import org.rdengine.net.http.RDHttpRequest;
import org.rdengine.net.http.RDHttpResponse;
import org.rdengine.net.http.RDResponseCallback;
import org.rdengine.view.manager.BaseActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends BaseActivity implements View.OnClickListener
{
    Button btn_sync;
    Button btn_aync;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // this.showView(ViewA.class, null);
        // testMD5.start();

        setContentView(R.layout.main);

        btn_sync = (Button) findViewById(R.id.btn_sync);
        btn_aync = (Button) findViewById(R.id.btn_aync);
        btn_sync.setOnClickListener(this);
        btn_aync.setOnClickListener(this);

    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
        case R.id.btn_sync :
            new Thread()
            {
                public void run()
                {
                    testHttpSync();
                }
            }.start();
            break;
        case R.id.btn_aync :
            testHttpAync();
            break;
        }
    }

    private static final String SERVER_URL = "http://114.66.8.198:9876/dgnews/";

    public void testHttpSync()
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

        RDHttpResponse response = RDHttpConnection.ins().requestSync(request);

        DLOG.d("request", response.getResponseJson().toString());
        btn_sync.setText("同步(完成)");

    }

    public void testHttpAync()
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
                    btn_aync.setText("异步(完成)");
                    DLOG.d("request", response.getResponseJson().toString());
                }
                return false;
            }
        };
        request.setResponseCallBack(callback);
        RDHttpConnection.ins().request(request);

    }

    Thread testMD5 = new Thread()
    {
        public void run()
        {
            for (int i = 1; i < 10; i++)
            {
                File file = new File("/sdcard/Download/test.apk");
                long startTime = System.currentTimeMillis();

                String md5 = getMd5(file);
                Log.d("md5", (System.currentTimeMillis() - startTime) + ":" + md5);
            }
        }
    };

    final static int bufferSize = 512 * 1024;

    public static String getMd5(File file)
    {
        String md5String = null;
        if (!file.exists())
        {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[bufferSize];
        int len;
        try
        {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, bufferSize)) != -1)
            {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        md5String = bigInt.toString(16);
        return md5String;

    }

}
