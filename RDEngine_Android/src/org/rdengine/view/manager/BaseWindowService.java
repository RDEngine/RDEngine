package org.rdengine.view.manager;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
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

    private static int TYPE_TOPONINPUT = LayoutParams.TYPE_SYSTEM_ERROR;
    static
    {
        // MIUI V6之前 气泡用TYPE_SYSTEM_ERROR 会有问题 不刷新 不重绘
        // int miuiversion = PhoneUtil.getMIUIVersion();
        // if (miuiversion >= 0 && miuiversion <= 5)
        {
            TYPE_TOPONINPUT = LayoutParams.TYPE_PHONE;
            // DLOG.e("BaseWindowService", "MIUIversion=V" + miuiversion + "  TYPE_TOPONINPUT=LayoutParams.TYPE_PHONE");
        }
    }

    protected ViewManager mViewManager;

    @Override
    public void onCreate()
    {
        Log.d("service", "onCreate");
        super.onCreate();
        winMgr = (WindowManager) this.getSystemService("window");
        initDefaultParams();
        initContainer();
        changeScreenSize();

    }

    @Override
    public void onDestroy()
    {
        Log.d("service", "onDestroy");
        super.onDestroy();
        winMgr.removeView(container);

    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private WindowContainer container;
    protected WindowManager winMgr;

    @SuppressLint("NewApi")
    private void initContainer()
    {
        Log.d("service", "createContainer");
        container = new WindowContainer(this);
        container.setVisibility(View.GONE);
        container.setFocusable(true);
        isDefaultParams = true;
        winMgr.addView(container, defaultParams);
        mViewManager = new ViewManager(this, container);
    }

    private void initDefaultParams()
    {
        if (defaultParams == null)
        {
            defaultParams = new LayoutParams();
            changeLayoutParams(defaultParams, 0, 0, screenWidth, screenHeight, BaseView.INPUT_TYPE_TOUCH_KEY, false);
        }
        if (currentParams == null)
        {
            currentParams = new LayoutParams();
            currentParams.copyFrom(defaultParams);
        }
    }

    private LayoutParams defaultParams;
    private LayoutParams currentParams;
    private boolean isDefaultParams = false;

    public Rect getDefaultPortraitRect()
    {
        return new Rect(0, 0, screenWidth, screenHeight);
    }

    public Rect getDefaultLandscapeRect()
    {
        return new Rect(0, 0, screenWidth, screenHeight);
    }

    private int screenWidth = 0, screenHeight = 0;

    private void changeScreenSize()
    {
        DisplayMetrics dm = new DisplayMetrics();
        winMgr.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        int rotation = winMgr.getDefaultDisplay().getRotation();
        Rect rect = null;
        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)
        {
            rect = getDefaultPortraitRect();
        } else
        {
            rect = getDefaultLandscapeRect();
        }
        changeLayoutParams(defaultParams, rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top,
                BaseView.INPUT_TYPE_TOUCH_KEY, false);
        if (isDefaultParams)
        {
            int lastType = currentParams.type;
            currentParams.copyFrom(defaultParams);
            if (lastType == currentParams.type)
            {
                winMgr.updateViewLayout(container, currentParams);
            } else
            {
                winMgr.removeView(container);
                winMgr.addView(container, currentParams);
            }
        }
        Log.d("service", "changeScreenSize:width:" + screenWidth + ",height=" + screenHeight);
    }

    private void changeLayoutParams(LayoutParams params, int x, int y, int w, int h, int inputType, boolean topOnInput)
    {
        if (topOnInput)
        {
            params.type = TYPE_TOPONINPUT;
        } else
        {
            params.type = LayoutParams.TYPE_PHONE;
        }
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.format = PixelFormat.RGBA_8888;
        switch (inputType)
        {
        case BaseView.INPUT_TYPE_TOUCH_KEY :
            params.flags = LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            break;
        case BaseView.INPUT_TYPE_NOTOUCH_NOKEY :
            params.flags = LayoutParams.FLAG_NOT_TOUCHABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | LayoutParams.FLAG_NOT_FOCUSABLE;
            break;
        case BaseView.INPUT_TYPE_TOUCH_NOKEY :
            params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
            break;
        case BaseView.INPUT_TYPE_NOTOUCH_KEY :
            params.flags = LayoutParams.FLAG_NOT_TOUCHABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL;
            break;
        case BaseView.INPUT_TYPE_TOUCH_KEY_THROUGH :
            params.flags = LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | LayoutParams.FLAG_NOT_TOUCH_MODAL;
            break;
        }
        params.flags |= LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        params.x = x;
        params.y = y;
        params.width = w;
        params.height = h;
    }

    public int getScreenWidth()
    {
        return screenWidth;
    }

    public int getScreenHeight()
    {
        return screenHeight;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        changeScreenSize();
    }

    private boolean isPaused = false;

    public boolean isPaused()
    {
        return isPaused;
    }

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
            if (container.getChildCount() > 0)
            {
                container.setVisibility(View.VISIBLE);
            }
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
        isDefaultParams = true;
        int lastType = currentParams.type;
        currentParams.copyFrom(defaultParams);
        if (lastType == currentParams.type)
        {
            winMgr.updateViewLayout(container, currentParams);
        } else
        {
            winMgr.removeView(container);
            winMgr.addView(container, currentParams);
        }
    }

    @Override
    public void updateWindowLayoutParams(int x, int y, int w, int h, int inputType, boolean topOnInput)
    {
        isDefaultParams = false;
        if (currentParams == null)
        {
            currentParams = new LayoutParams();

            currentParams.gravity = Gravity.LEFT | Gravity.TOP;
            currentParams.format = PixelFormat.RGBA_8888;
        }
        int lastType = currentParams.type;
        if (topOnInput)
        {
            currentParams.type = TYPE_TOPONINPUT;
        } else
        {
            currentParams.type = LayoutParams.TYPE_PHONE;
        }
        switch (inputType)
        {
        case BaseView.INPUT_TYPE_TOUCH_KEY :
            currentParams.flags = LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
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
        case BaseView.INPUT_TYPE_TOUCH_KEY_THROUGH :
            currentParams.flags = LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | LayoutParams.FLAG_NOT_TOUCH_MODAL;
            break;
        case BaseView.INPUT_TYPE_TOUCH_NOKEY_THROUGH :
            currentParams.flags = LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | LayoutParams.FLAG_NOT_FOCUSABLE;
            break;
        }
        currentParams.flags |= LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        currentParams.x = x;
        currentParams.y = y;
        currentParams.width = w;
        currentParams.height = h;
        if (lastType == currentParams.type)
        {
            winMgr.updateViewLayout(container, currentParams);
        } else
        {
            winMgr.removeView(container);
            winMgr.addView(container, currentParams);
        }

    }

    @Override
    public BaseView getTopView()
    {
        return mViewManager.getViewAt(mViewManager.getViewSize() - 1);

    }

    @Override
    public void swipeviewOnDismiss(BaseView sbv)
    {
        mViewManager.swipeviewOnDismiss(sbv);
    }

    @Override
    public void moveToTop(BaseView view)
    {
        mViewManager.moveToTop(view);
    }

    public void moveToBottom(BaseView view)
    {
        mViewManager.moveToBottom(view);
    }

    // Controller-----------------------------------------------------------------------------------------
}
