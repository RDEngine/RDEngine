package org.rdengine.view.manager;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

public abstract class BaseView extends FrameLayout
{

    protected ViewParam mViewParam;

    private ViewController controller;

    public BaseView(Context context)
    {
        super(context);
        if (!(context instanceof ViewController))
        {
            throw new Error("context is not a Activity implements ViewController");
        }
        controller = (ViewController) context;
        setClickable(true);
    }

    public BaseView(Context context, ViewParam param)
    {
        this(context);
        mViewParam = param;
    }

    public void init()
    {
        refresh();
    }

    public void refresh()
    {

    }

    protected LayoutInflater mInflater;

    protected void setContentView(int layoutId)
    {
        mInflater = LayoutInflater.from(getContext());
        mInflater.inflate(layoutId, this, true);
    }

    /**
     * 获取当前View的Controller,用于控制View流程
     * 
     * @return
     */
    public ViewController getController()
    {
        return controller;
    }

    /**
     * 销毁当前界面
     */
    protected void dismissCurrentView()
    {
        closeInputMethod();
        controller.backView();

    }

    private BaseView parentView;

    public BaseView getParentView()
    {
        return parentView;
    }

    public void setParentView(BaseView parentView)
    {
        this.parentView = parentView;
    }

    public void hideChildrenView()
    {

    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
    }

    /**
     * @return true:默认执行Back,false:拦截Back事件
     */
    public boolean OnBack()
    {
        return true;
    }

    private boolean isShown = false;

    public void onShow()
    {
        if (!isShown)
        {
            isShown = true;
            Log.i("BaseView", "onShow:" + this.getClass().getName());
        }
    }

    public void onHide()
    {
        if (isShown)
        {
            isShown = false;
            Log.i("BaseView", "onHide:" + this.getClass().getName());

            hideChildrenView();
        }
    }

    public void onLoadResource()
    {
        Log.i("BaseView", "onLoadResource:" + this.getClass().getName());
    }

    public void onReleaseResource()
    {
        Log.i("BaseView", "onReleaseResource:" + this.getClass().getName());
    }

    public abstract String getTag();

    public void closeInputMethod()
    {
        try
        {
            if (getContext() instanceof BaseActivity)
            {
                BaseActivity activity = (BaseActivity) getContext();
                InputMethodManager inputMethodManager = (InputMethodManager) activity
                        .getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e)
        {
        }
    }

    public static final int INPUT_TYPE_TOUCH_KEY = 1;
    public static final int INPUT_TYPE_TOUCH_NOKEY = 2;
    public static final int INPUT_TYPE_NOTOUCH_KEY = 3;
    public static final int INPUT_TYPE_NOTOUCH_NOKEY = 4;

}
