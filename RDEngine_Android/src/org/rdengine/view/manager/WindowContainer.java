package org.rdengine.view.manager;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;

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
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        Log.d("dispatchKeyEvent", "" + event.getAction());
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
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
    public String getTag()
    {
        return null;
    }

}