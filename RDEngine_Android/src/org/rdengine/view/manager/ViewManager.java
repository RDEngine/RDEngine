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
        view.onLoadResource();
        view.onShow();

        if (mViewStack.size() > 2)
        {
            mViewStack.get(mViewStack.size() - 2).onReleaseResource();
        }
    }

    /**
     * 显示新View
     * 
     * @param clazz
     * @param param
     */
    public void showView(Class<?> clazz, ViewParam param)
    {
        BaseView view = createView(clazz, param, mContext);
        addViewToTop(view);
        view.refresh();
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
            BaseView currentView = mViewStack.peek();

            currentView = mViewStack.pop();
            mContainer.removeView(currentView);
            currentView.onHide();
            currentView.onReleaseResource();

            BaseView showView = mViewStack.peek();
            if (!showView.getShown())
            {
                showView.onLoadResource();
                showView.onShow();
            }
            showView.setVisibility(View.VISIBLE);

            return false;
        } else
        {
            return true;
        }
    }

    /** 滑动返回的view 结束了滑动 开始销毁 */
    public void swipeviewOnDismiss(BaseView sbv)
    {
        if (sbv != null)
        {
            try
            {
                // mContainer.removeView(sbv);
                // sbv.onHide();
                // sbv.onReleaseResource();

                BaseView currentView = mViewStack.pop();
                mContainer.removeView(currentView);
                currentView.onHide();
                currentView.onReleaseResource();
                currentView.closeInputMethod();

                BaseView showView = mViewStack.peek();
                if (!showView.getShown())
                {
                    showView.onLoadResource();
                    showView.onShow();
                }
                showView.setVisibility(View.VISIBLE);

            } catch (Exception e)
            {
                e.printStackTrace();
            }
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
    public static BaseView createView(Class<?> clazz, ViewParam param, Context context)
    {
        BaseView view = null;
        if (view == null)
        {
            try
            {
                Constructor constructor = clazz.getDeclaredConstructor(Context.class);
                constructor.setAccessible(true);
                view = (BaseView) constructor.newInstance(context);
                view.mViewParam = param;
                view.init();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return view;
    }

    /**
     * 将一个view移动到最顶层
     * 
     * @param view
     */
    public void moveToTop(BaseView view)
    {
        if (mViewStack != null && mViewStack.size() > 1 && view != null)
        {
            mViewStack.remove(view);

            BaseView currentView = mViewStack.peek();
            currentView.onHide();
            currentView.setVisibility(View.GONE);

            mViewStack.push(view);
            boolean hasview = false;
            for (int i = 0; i < mContainer.getChildCount(); i++)
            {
                View child = mContainer.getChildAt(i);
                if (view.equals(child))
                {
                    hasview = true;
                    break;
                }
            }
            if (hasview)
            {
                view.bringToFront();
            } else
            {
                mContainer.addView(view);
            }
            view.setVisibility(View.VISIBLE);
            view.onLoadResource();
            view.onShow();

            if (mViewStack.size() > 2)
            {
                mViewStack.get(mViewStack.size() - 2).onReleaseResource();
            }
        }
    }

    public void moveToBottom(BaseView view)
    {
        if (mViewStack != null && mViewStack.size() > 1 && view != null)
        {
            mViewStack.remove(view);
            mViewStack.insertElementAt(view, 0);
            view.onHide();
            view.setVisibility(View.GONE);

            BaseView currentView = mViewStack.peek();
            currentView.setVisibility(View.VISIBLE);
            currentView.onLoadResource();
            currentView.onShow();
            currentView.bringToFront();

            if (mViewStack.size() > 2)
            {
                mViewStack.get(mViewStack.size() - 2).onReleaseResource();
            }
        }
    }
}
