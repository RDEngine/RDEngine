package org.rdengine.view.manager;

import org.rdengine.log.DLOG;

import test.viewdemo.view.BubbleView;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * Window浮窗的VIew容器
 * 
 * @author yangyu
 */
class WindowContainer extends BaseView
{

    public WindowContainer(Context context)
    {
        super(context);
    }

    @Override
    public void init()
    {
        super.init();

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        // DLOG.d("cccmax", event.toString());
        // BaseView bv = getController().getTopView();
        // if (bv != null && BubbleView.class.isInstance(bv)) // 判断当前页面是气泡
        // {
        // boolean bbss = event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN
        // && event.getAction() == KeyEvent.ACTION_DOWN
        // && PreferenceHelper.ins().getBooleanShareData(FloatScreenshotView.IS_VOLUME, false);
        // if(!bbss) //不是音量键截屏 就释放这次事件
        // return false;
        // }
        Log.d("dispatchKeyEvent", "" + event.getAction());

        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            onKeyDown(event.getKeyCode(), event);
            return true;
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            if (getController().getTopView() instanceof BubbleView)
            {
                return false;
            }
            if (getController().backView())
            {
                // WindowManager winMgr = (WindowManager) getContext().getSystemService("window");
                // winMgr.removeView(this);
                this.setVisibility(GONE);
                return false;
            } else
            {
                return false;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
        {
            // EventManager.ins().sendEvent(EventTag.FLOAT_SCREENSHOT_VOLUME, 0, 0, null);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public String getTag()
    {
        return null;
    }

    int keyDownX = 0, keyDownY = 0;
    long keyDownTime = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        DLOG.d("floatontouch", event.toString());
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch (event.getAction())
        {
        case MotionEvent.ACTION_DOWN :
            keyDownX = x;
            keyDownY = y;
            keyDownTime = System.currentTimeMillis();
            break;
        case MotionEvent.ACTION_MOVE :
            break;
        case MotionEvent.ACTION_UP :
            if (System.currentTimeMillis() - keyDownTime < 500)
            {
                if (Math.abs(x - keyDownX) < 10 & Math.abs(y - keyDownY) < 10)
                {
                    // 点击
                    showbubble();
                }
            }
            break;
        // case MotionEvent.ACTION_OUTSIDE :
        // getController().showView(BubbleView.class, null);
        // break;
        }
        return true;
    }

    public void showbubble()
    {
        boolean hasbubble = false;
        View bb = null;
        for (int i = 0; i < getController().getViewSize(); i++)
        {
            BaseView child = getController().getViewAt(i);
            if (BubbleView.class.isInstance(child))
            {
                hasbubble = true;
                bb = child;
                break;
            }
        }
        if (hasbubble)
        {
            getController().moveToTop((BaseView) bb);
        } else
        {
            getController().showView(BubbleView.class, null);
        }
    }

}