package test.viewdemo.context;

import org.rdengine.runtime.event.EventListener;
import org.rdengine.runtime.event.EventManager;
import org.rdengine.runtime.event.EventTag;
import org.rdengine.view.manager.BaseWindowService;

import test.viewdemo.view.BubbleView;
import android.graphics.Rect;

public class MainService extends BaseWindowService
{

    @Override
    public void onCreate()
    {
        super.onCreate();
        EventManager.ins().registListener(EventTag.FLOAT_WINDOW_SHOW_PANEL, listener);
        EventManager.ins().registListener(EventTag.SCAN_ACTIVITY_DESKTOP, listener);
        EventManager.ins().registListener(EventTag.SCAN_ACTIVITY_APP, listener);

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        EventManager.ins().removeListener(EventTag.FLOAT_WINDOW_SHOW_PANEL, listener);
        EventManager.ins().removeListener(EventTag.SCAN_ACTIVITY_DESKTOP, listener);
        EventManager.ins().removeListener(EventTag.SCAN_ACTIVITY_APP, listener);
    }

    EventListener listener = new EventListener()
    {

        @Override
        public void handleMessage(int what, int arg1, int arg2, Object dataobj)
        {
            switch (what)
            {
            case EventTag.FLOAT_WINDOW_SHOW_PANEL :
                MainService.this.showView(BubbleView.class, null);
                break;
            case EventTag.SCAN_ACTIVITY_DESKTOP :
                // Pause();
                break;
            case EventTag.SCAN_ACTIVITY_APP :
                // Resume();
                break;
            }
        }
    };

    public Rect getDefaultPortraitRect()
    {
        return new Rect(100, 100, getScreenWidth() - 200, getScreenHeight() - 200);
    };

    @Override
    public Rect getDefaultLandscapeRect()
    {
        return new Rect(100, 50, 800, getScreenHeight() - 100);
    }
}
