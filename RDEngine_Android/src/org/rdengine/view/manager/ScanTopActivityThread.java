package org.rdengine.view.manager;

import java.util.HashMap;
import java.util.List;

import org.rdengine.runtime.event.EventManager;
import org.rdengine.runtime.event.EventTag;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.util.Log;

/**
 * 检测当前最上层Activity的线程
 * 
 * @author yangyu
 */
public class ScanTopActivityThread extends Thread
{

    private Context mContext;

    public ScanTopActivityThread(Context context)
    {
        mContext = context;
        isListening = true;
    }

    boolean isListening = false;

    private int lastAPKNameHashCode = 0;

    @Override
    public void run()
    {
        String apkname = "";
        int apknamehashcode = 0;
        long starttime = 0;
        while (isListening)
        {
            try
            {
                Thread.sleep(100);
                starttime = System.currentTimeMillis();
                ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
                if (runningTaskInfos != null)
                {
                    apkname = (runningTaskInfos.get(0).topActivity).getPackageName();
                    if (apkname == null)
                    {
                        continue;
                    }
                    apknamehashcode = apkname.hashCode();
                    if (apknamehashcode != lastAPKNameHashCode)
                    {
                        Log.d("service", "apkname:" + apkname + ":" + (System.currentTimeMillis() - starttime));
                        if (isDesktop(apknamehashcode))
                        {
                            EventManager.ins().sendEvent(EventTag.SCAN_ACTIVITY_DESKTOP, 0, 0, apkname);
                        } else
                        {
                            EventManager.ins().sendEvent(EventTag.SCAN_ACTIVITY_APP, 0, 0, apkname);
                        }
                        lastAPKNameHashCode = apknamehashcode;
                    }
                }
            } catch (Exception ex)
            {
                Log.d("service", ex.toString());
            }
        }
    }

    public void stopScan()
    {
        this.isListening = false;
    }

    private static HashMap<Integer, Integer> DeskTopTable = new HashMap<Integer, Integer>();
    static
    {
        DeskTopTable.put("com.miui.home".hashCode(), 0);
    }

    private boolean isDesktop(int hashcode)
    {
        if (DeskTopTable.containsKey(hashcode))
        {
            return true;
        } else
        {
            return false;
        }
    }
}
