package org.rdengine.util;

import java.util.ArrayList;
import java.util.List;

import android.R;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class PackageUtil
{
    /***
     * @param context
     * @return installed packageNames
     */
    public static List<String> getInstalledAppPackageName(Context context)
    {
        List<String> appNames = new ArrayList<String>();
        List<PackageInfo> packageInfos = context.getPackageManager()
                .getInstalledPackages(PackageManager.GET_ACTIVITIES);
        for (PackageInfo packageInfo : packageInfos)
        {
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
            {
                String packageName = packageInfo.packageName;
                appNames.add(packageName);
            }

        }
        return appNames;
    }

	public static boolean isInstallApk(Context context, String packageName)
	{
		if (StringUtil.isEmpty(packageName))
		{
			return false;
		}
		PackageInfo packageInfo;
        try
        {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (NameNotFoundException e)
        {
            packageInfo = null;
            e.printStackTrace();
        }
        if (packageInfo == null)
        {
            return false;
        } else
        {
            return true;
        }
    }

    public static int getVersionCode(Context context)// 获取版本号(内部识别号)
    {
        try
        {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionCode;
        } catch (NameNotFoundException e)
        {
            e.printStackTrace();
            return 0;
        }
    }
    
    public static int getVersionCode(Context context, String packageName)// 获取版本号(内部识别号)
    {
    	try
    	{
    		PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);
    		return pi.versionCode;
    	} catch (NameNotFoundException e)
    	{
    		e.printStackTrace();
    		return 0;
    	}
    }

    public static String getVersionName(Context context)// 获取版本号
    {
        try
        {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionName;
        } catch (NameNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
