package org.rdengine.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.rdengine.log.DLOG;
import org.rdengine.runtime.RT;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

public class DMUtil
{
    public static boolean isMount()
    {
        return new File(RT.mLocalExternalPath).canWrite();
    }

    /** 计算字符串中的中文数量 英文和数字算半个 */
    public static int calculateWordNumber(String in)
    {
        if (StringUtil.isEmpty(in))
        {
            return 0;
        }
        int cnLength = 0;
        int enLength = 0;
        String a;
        for (int i = 0; i < in.length(); i++)
        {
            // String a = in.substring(i, i + 1);
            a = String.valueOf(in.charAt(i));
            byte[] b = a.getBytes();
            if (b.length < 2)
            {
                enLength++;
            } else
            {
                cnLength++;
            }
        }
        return cnLength + (enLength + 1) / 2;
    }

    public static boolean isRoot()
    {
        boolean isRoot = false;
        String sys = System.getenv("PATH");
        ArrayList<String> commands = new ArrayList<String>();
        String[] path = sys.split(":");
        for (int i = 0; i < path.length; i++)
        {
            String commod = "ls -l " + path[i] + "/su";
            commands.add(commod);
        }
        ArrayList<String> res = run("/system/bin/sh", commands);
        String response = "";
        for (int i = 0; i < res.size(); i++)
        {
            response += res.get(i);
        }

        String root = "-rwsr-sr-x root     root";
        if (response.contains(root))
        {
            isRoot = true;
        }

        return isRoot;

    }

    public static ArrayList<String> run(String shell, ArrayList<String> commands)
    {
        ArrayList<String> output = new ArrayList<String>();
        Process process = null;
        try
        {
            process = Runtime.getRuntime().exec(shell);

            BufferedOutputStream shellInput = new BufferedOutputStream(process.getOutputStream());
            BufferedReader shellOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            for (String command : commands)
            {
                shellInput.write((command + " 2>&1\n").getBytes());
            }

            shellInput.write("exit\n".getBytes());
            shellInput.flush();

            String line;
            while ((line = shellOutput.readLine()) != null)
            {
                output.add(line);
            }

            process.waitFor();
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } finally
        {
            process.destroy();
        }

        return output;
    }

    /**
     * 获得进程名
     * 
     * @param context
     * @param pid
     * @return
     */
    public static String getAppNameByPID(Context context, int pid)
    {
        try
        {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

            for (RunningAppProcessInfo processInfo : manager.getRunningAppProcesses())
            {
                if (processInfo.pid == pid)
                {
                    return processInfo.processName;
                }
            }
            return "";
        } catch (Exception e)
        {
            return "";
        }
    }

    /**
     * 判断指定包名的进程是否运行
     * 
     * @param context
     * @param packageName
     *            指定包名
     * @return 是否运行
     */
    public static boolean isRunning(Context context, String packageName)
    {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> infos = am.getRunningAppProcesses();
        for (RunningAppProcessInfo rapi : infos)
        {
            if (rapi.processName.equals(packageName))
                return true;
        }
        return false;
    }

    public static boolean isServiceRunning(Context mContext, String className)
    {

        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(1000);

        if (!(serviceList.size() > 0))
        {
            return false;
        }

        for (int i = 0; i < serviceList.size(); i++)
        {
            DLOG.d("service", serviceList.get(i).service.getClassName());
            if (serviceList.get(i).service.getClassName().equals(className))
            {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

}
