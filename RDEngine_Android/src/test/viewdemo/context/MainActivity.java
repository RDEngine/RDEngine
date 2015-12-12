package test.viewdemo.context;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

import org.rdengine.view.manager.BaseActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends BaseActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // this.showView(ViewA.class, null);
        testMD5.start();
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
