package test.viewdemo.view;

import org.rdengine.android.R;
import org.rdengine.view.manager.BaseView;

import android.content.Context;
import android.view.MotionEvent;

public class BubbleView extends BaseView
{

    public BubbleView(Context context)
    {
        super(context);
        setContentView(R.layout.bubble);

        this.getController().updateWindowLayoutParams(0, 0, 128, 128, INPUT_TYPE_TOUCH_NOKEY, true);
    }

    @Override
    public String getTag()
    {
        return null;
    }

    @Override
    public void onShow()
    {
        super.onShow();
        this.getController().updateWindowLayoutParams(floatX, floatY, 128, 128, INPUT_TYPE_TOUCH_NOKEY, true);
    }

    @Override
    public void onHide()
    {
        super.onHide();
        this.getController().setDefaultLayoutParams();
    }

    int floatX = 0, floatY = 0;
    int keyDownX = 0, keyDownY = 0;
    long keyDownTime = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {

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
            if (Math.abs(x - keyDownX) < 10 & Math.abs(y - keyDownY) < 10)
            {

            } else
            {
                floatX = x - 64;
                floatY = y - 64;
                this.getController().updateWindowLayoutParams(floatX, floatY, 128, 128, INPUT_TYPE_TOUCH_NOKEY, true);
            }
            break;
        case MotionEvent.ACTION_UP :
            if (System.currentTimeMillis() - keyDownTime < 500)
            {
                if (Math.abs(x - keyDownX) < 10 & Math.abs(y - keyDownY) < 10)
                {

                    onClick();
                }
            }
            break;
        }
        return true;
    }

    private void onClick()
    {
        this.getController().showView(ViewA.class, null);
    }

}
