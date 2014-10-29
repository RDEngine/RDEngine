package org.rdengine.view.manager;

import java.lang.reflect.Constructor;
import java.util.Stack;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * View栈管理
 * 
 * @author yangyu
 */
public class ViewManager
{
    private Context mContext;
    private ViewGroup mContainer;
    private Stack<BaseView> mViewStack;

    private ViewManager(Context context)
    {
        if (!(context instanceof ViewController))
        {
            throw new Error("context is not a Activity implements ViewController");
        }
        mContext = context;
        mViewStack = new Stack<BaseView>();
        mViewStack.clear();
    }

    public ViewManager(Context context, ViewGroup container)
    {
        this(context);
        mContainer = container;
    }

    /**
     * 插到栈顶
     * 
     * @param view
     */
    public void addViewToTop(BaseView view)
    {
        if (!mViewStack.empty())
        {
            BaseView currentView = mViewStack.peek();
            currentView.onHide();
            currentView.setVisibility(View.GONE);
        }
        mViewStack.push(view);
        mContainer.addView(view);
        view.setVisibility(View.VISIBLE);
        view.onShow();
    }

    /**
     * 显示新View
     * 
     * @param clazz
     * @param param
     */
    public void showView(Class<?> clazz, ViewParam param)
    {
        BaseView view = createView(clazz, param);
        addViewToTop(view);
    }

    /**
     * 自然返回
     * 
     * @return
     */
    public boolean backView()
    {
        if (mViewStack.size() > 1)
        {
            BaseView currentView = mViewStack.pop();
            mContainer.removeView(currentView);
            currentView.onHide();
            currentView.onReleaseResource();

            BaseView showView = mViewStack.peek();
            showView.onShow();
            showView.setVisibility(View.VISIBLE);
            return false;
        } else
        {
            return true;
        }
    }

    public void killViewAt(int index)
    {
        BaseView view = mViewStack.get(index);
        mViewStack.remove(index);
        mContainer.removeView(view);

    }

    public int getViewSize()
    {
        return mViewStack.size();
    }

    public void killAllHistoryView()
    {
        BaseView currentView = mViewStack.peek();
        mViewStack.clear();
        mContainer.removeAllViews();

        mViewStack.push(currentView);
        mContainer.addView(currentView);
    }

    public BaseView getViewAt(int index)
    {
        BaseView view = mViewStack.get(index);
        return view;
    }

    public void killAllSameView(BaseView view)
    {
        BaseView v = null;
        for (int i = 0; i < mViewStack.size(); i++)
        {
            v = mViewStack.get(i);

            if (view.getClass().getName().equals(v.getClass().getName()))
            {
                if (v != view)
                {
                    mViewStack.remove(i);
                    mContainer.removeView(v);
                    i--;
                }
            }
        }
    }

    /**
     * 创建BaseView
     * 
     * @param clazz
     * @param param
     * @return
     */
    public BaseView createView(Class<?> clazz, ViewParam param)
    {
        BaseView view = null;
        if (view == null)
        {
            try
            {
                Constructor constructor = clazz.getDeclaredConstructor(Context.class);
                constructor.setAccessible(true);
                view = (BaseView) constructor.newInstance(mContext);
                view.mViewParam = param;
                view.init();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return view;
    }
}
