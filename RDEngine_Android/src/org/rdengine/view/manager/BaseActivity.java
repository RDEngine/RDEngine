package org.rdengine.view.manager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * 使用RDEngine的Activity
 * 
 * @author yangyu
 */
public class BaseActivity extends FragmentActivity implements ViewController
{

    private ViewManager mViewManager;

    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        ActivityContainer container = new ActivityContainer(this);
        this.setContentView(container);
        mViewManager = new ViewManager(this, container);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    public void showView(Class<?> clazz, ViewParam param)
    {
        if (mViewManager == null)
        {
            throw new Error("Container not set!!!!!!");
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
    public void onBackPressed()
    {
        if (this.backView())
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean getWindowVisibility()
    {
        return false;
    }

    @Override
    public void setWindowVisibility(boolean visibility)
    {}

    @Override
    public void setDefaultLayoutParams()
    {}

    @Override
    public void updateWindowLayoutParams(int x, int y, int w, int h, int inputType, boolean topOnInput)
    {}

}
