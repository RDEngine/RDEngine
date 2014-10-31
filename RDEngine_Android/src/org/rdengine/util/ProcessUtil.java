package org.rdengine.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import org.rdengine.log.DLOG;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

public class ProcessUtil
{
	public static void killAllProcess(Context context)
	{
		// 拿到这个包管理器
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		// 拿到所有正在运行的进程信息
		List<RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
		// 进行遍历，然后杀死它们
		for (RunningAppProcessInfo runningAppProcessInfo : list)
		{
			if ("com.mofang.mgassistant".equals(runningAppProcessInfo.processName))
			{

			} else
			{
				activityManager.killBackgroundProcesses(runningAppProcessInfo.processName);
			}
		}
	}

	public static String getProcessCount(Context context)
	{
		// 拿到这个包管理器
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		// 拿到所有正在运行的进程信息
		List<RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
		return list.size() + "";
	}

	public static long getAvailMemory(Context context)
	{
		// 拿到这个包管理器
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		// new一个内存的对象
		MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		// 拿到现在系统里面的内存信息
		activityManager.getMemoryInfo(memoryInfo);
		return memoryInfo.availMem;
		// return dataSizeFormat(memoryInfo.availMem);
	}

	public static long getTotalMemory(Context context)
	{
		String str1 = "/proc/meminfo";// 系统内存信息文件
		String str2;
		String[] arrayOfString;
		long initial_memory = 0;

		try
		{
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
			str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

			arrayOfString = str2.split("\\s+");
			for (String num : arrayOfString)
			{
				DLOG.i(str2, num + "/t");
			}

			initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
			localBufferedReader.close();

		} catch (IOException e)
		{
		}
		// return Formatter.formatFileSize(context, initial_memory);// Byte转换为KB或者MB，内存大小规格化
		return initial_memory;
	}

	public static String dataSizeFormat(long size)
	{
		DecimalFormat formater = new DecimalFormat("####.00");
		if (size < 1024)
		{
			return size + "byte";
		} else if (size < (1 << 20)) // 左移20位，相当于1024 * 1024
		{
			float kSize = size >> 10; // 右移10位，相当于除以1024
			return formater.format(kSize) + "KB";
		} else if (size < (1 << 30)) // 左移30位，相当于1024 * 1024 * 1024
		{
			float mSize = size >> 20; // 右移20位，相当于除以1024再除以1024
			return formater.format(mSize) + "MB";
		} else if (size < (1 << 40))
		{
			float gSize = size >> 30;
			return formater.format(gSize) + "GB";
		} else
		{
			return "size : error";
		}
	}

	public static String getSizeFromKB(long kSize)
	{
		return dataSizeFormat(kSize << 10);
	}

}
