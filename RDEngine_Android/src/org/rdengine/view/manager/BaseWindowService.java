package org.rdengine.view.manager;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * 浮窗服务
 * 
 * @author yangyu
 */
public class BaseWindowService extends Service implements ViewController
{

    ViewManager mViewManager;
    ScanTopActivityThread scanThread;

    @Override
    public void onCreate()
    {
        Log.d("service", "onCreate");
        super.onCreate();
        winMgr = (WindowManager) this.getSystemService("window");
        createDefaultLayoutParams(1080 - 480, 1920 - 800, 400, 800, BaseView.INPUT_TYPE_TOUCH_KEY);
        createContainer();
        scanThread = new ScanTopActivityThread(this);
        scanThread.start();
    }

    @Override
    public void onDestroy()
    {
        Log.d("service", "onDestroy");
        super.onDestroy();
        winMgr.removeView(container);
        scanThread.stopScan();

    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private WindowContainer container;
    WindowManager winMgr;

    private void createContainer()
    {
        Log.d("service", "createContainer");
        container = new WindowContainer(this);
        container.setVisibility(View.GONE);
        container.setFocusable(true);
        currentParams = new LayoutParams();
        currentParams.copyFrom(defaultParams);
        winMgr.addView(container, defaultParams);
        mViewManager = new ViewManager(this, container);
    }

    private LayoutParams defaultParams;
    private LayoutParams currentParams;

    private void createDefaultLayoutParams(int x, int y, int w, int h, int inputType)
    {
        defaultParams = new WindowManager.LayoutParams();
        defaultParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
        defaultParams.gravity = Gravity.LEFT | Gravity.TOP;
        defaultParams.format = PixelFormat.RGBA_8888;
        switch (inputType)
        {
        case BaseView.INPUT_TYPE_TOUCH_KEY :
            defaultParams.flags = LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | LayoutParams.FLAG_NOT_TOUCH_MODAL;
            break;
        case BaseView.INPUT_TYPE_NOTOUCH_NOKEY :
            defaultParams.flags = LayoutParams.FLAG_NOT_TOUCHABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | LayoutParams.FLAG_NOT_FOCUSABLE;
            break;
        case BaseView.INPUT_TYPE_TOUCH_NOKEY :
            defaultParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
            break;
        case BaseView.INPUT_TYPE_NOTOUCH_KEY :
            defaultParams.flags = LayoutParams.FLAG_NOT_TOUCHABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL;
            break;
        }
        defaultParams.flags |= LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        defaultParams.x = x;
        defaultParams.y = y;
        defaultParams.width = w;
        defaultParams.height = h;
    }

    private void updateLayoutParams(LayoutParams params, boolean changeType)
    {
        if (changeType)
        {
            currentParams = params;
            winMgr.removeView(container);
            winMgr.addView(container, currentParams);
        } else
        {
            currentParams = params;
            winMgr.updateViewLayout(container, currentParams);
        }
        Log.d("service", "updateLayout:x=" + currentParams.x + ",y=" + currentParams.y + ",w=" + currentParams.width
                + ",h=" + currentParams.height);
    }

    private boolean isPaused = false;

    public void Pause()
    {
        if (isPaused)
        {
            return;
        }
        isPaused = true;

        if (container != null)
        {
            Log.d("service", "Pause");
            container.setVisibility(View.GONE);
        }
    }

    public void Resume()
    {
        if (!isPaused)
        {
            return;
        }
        isPaused = false;
        if (container != null)
        {
            Log.d("service", "Resume");
            container.setVisibility(View.VISIBLE);
        }
    }

    // Controller-----------------------------------------------------------------------------------------

    @Override
    public void showView(Class<?> clazz, ViewParam param)
    {
        if (mViewManager == null)
        {
            throw new Error("Container not set!!!!!!");
        }

        if (container != null)
        {
            container.setVisibility(View.VISIBLE);
        }

        mViewManager.showView(clazz, param);
    }

    @Override
    public boolean backView()
    {
        return mViewManager.backView();
    }

    @Override
    public void killViewAt(int index)
    {
        mViewManager.killViewAt(index);
    }

    @Override
    public int getViewSize()
    {
        return mViewManager.getViewSize();
    }

    @Override
    public void killAllHistoryView()
    {
        mViewManager.killAllHistoryView();
    }

    @Override
    public BaseView getViewAt(int index)
    {
        return mViewManager.getViewAt(index);
    }

    @Override
    public void killAllSameView(BaseView view)
    {
        mViewManager.killAllSameView(view);
    }

    @Override
    public boolean getWindowVisibility()
    {
        if (container == null)
        {
            return false;
        }
        return (container.getVisibility() == View.VISIBLE);
    }

    @Override
    public void setWindowVisibility(boolean visibility)
    {
        if (container != null)
        {
            if (visibility)
            {
                container.setVisibility(View.VISIBLE);
            } else
            {
                container.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void setDefaultLayoutParams()
    {
        LayoutParams param = new LayoutParams();
        param.copyFrom(defaultParams);
        updateLayoutParams(param, param.type != currentParams.type);
    }

    @Override
    public void updateWindowLayoutParams(int x, int y, int w, int h, int inputType, boolean topOnInput)
    {
        if (currentParams == null)
        {
            currentParams = new LayoutParams();

            currentParams.gravity = Gravity.LEFT | Gravity.TOP;
            currentParams.format = PixelFormat.RGBA_8888;
        }
        int lastType = currentParams.type;
        if (topOnInput)
        {
            currentParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
        } else
        {
            currentParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
        }

        switch (inputType)
        {
        case BaseView.INPUT_TYPE_TOUCH_KEY :
            currentParams.flags = LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | LayoutParams.FLAG_NOT_TOUCH_MODAL;
            break;
        case BaseView.INPUT_TYPE_NOTOUCH_NOKEY :
            currentParams.flags = LayoutParams.FLAG_NOT_TOUCHABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | LayoutParams.FLAG_NOT_FOCUSABLE;
            break;
        case BaseView.INPUT_TYPE_TOUCH_NOKEY :
            currentParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
            break;
        case BaseView.INPUT_TYPE_NOTOUCH_KEY :
            currentParams.flags = LayoutParams.FLAG_NOT_TOUCHABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL;
            break;
        }
        currentParams.flags |= LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        currentParams.x = x;
        currentParams.y = y;
        currentParams.width = w;
        currentParams.height = h;

        updateLayoutParams(currentParams, lastType != currentParams.type);
    }

    // Controller-----------------------------------------------------------------------------------------
}
