package org.rdengine.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.util.Log;

/**
 * 截图工具
 * 
 * @author CCCMAX
 */
public class ScreenShotUtil
{

    /**
     * 判断当前手机是否有ROOT权限
     * 
     * @return
     */
    public static boolean isRoot()
    {
        boolean bool = false;
        try
        {
            if ((!new File("/system/bin/su").exists()) && (!new File("/system/sbin/su").exists())
                    && (!new File("/system/xbin/su").exists()))
            {
                bool = false;
            } else
            {
                bool = true;
            }
        } catch (Exception e)
        {
        }
        return bool;
    }

    /**
     * 截图保存图片
     * 
     * @param path
     *            截图保存的路径
     * @return 0 成功 -1路径错误 -2截图命令异常(权限) -3找不到图片文件
     */
    public static int screenshot(String path)
    {
        // path = "/sdcard/test.png";//图片路径

        if (path == null || path.length() == 0)
            return -1;// 路径错误

        int err = 0;

        File imgfile = new File(path);// 已存在 则删除旧文件
        if (imgfile.exists())
            imgfile.delete();

        DataOutputStream os = null;
        try
        {
            Process process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("screencap -p " + path + "\n exit\n");
            os.flush();
            err = process.waitFor();// 0 成功 1 失败
        } catch (Exception e)
        {
            Log.e("cccmax", "截图异常");
            e.printStackTrace();
            err = -2;// 截图命令异常
        } finally
        {
            try
            {
                os.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        if (err == 0)
        {
            if (!new File(path).exists())// 判断文件是否存在
            {
                err = -3;// 返回0成功 但是找不到图片
            }
        }
        return err;
    }
    
    
    private static Context mContext;
    
    public static int getDrawableId(String name)
    {
        if(mContext == null || name  == null)
        {
            return 0;
        }
        
        return mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName());
        
    }
    
    public static int getLayoutId(String name)
    {
        if(mContext == null || name  == null)
        {
            return 0;
        }
        
        return mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName());
    }
    
    
    public static int getStringId(String name)
    {
        if(mContext == null || name  == null)
        {
            return 0;
        }
        
        return mContext.getResources().getIdentifier(name, "string", mContext.getPackageName());
    }
    
}
