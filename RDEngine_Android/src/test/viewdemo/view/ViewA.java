package test.viewdemo.view;

import org.rdengine.android.R;
import org.rdengine.runtime.event.EventManager;
import org.rdengine.runtime.event.EventTag;
import org.rdengine.view.manager.BaseView;

import test.viewdemo.context.MainService;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ViewA extends BaseView implements OnClickListener
{

    public ViewA(Context context)
    {
        super(context);
        setContentView(R.layout.viewa);
    }

    static int number = 0;
    Button buttonA, buttonB, buttonC, buttonDelAll, buttonDelSame, buttonShowPanel, buttonStartService,
            buttonStopService;

    @Override
    public void init()
    {
        TextView textview = (TextView) findViewById(R.id.text);
        textview.setText("this is " + number);
        number++;
        buttonA = (Button) findViewById(R.id.btna);
        buttonA.setOnClickListener(this);
        buttonB = (Button) findViewById(R.id.btnb);
        buttonB.setOnClickListener(this);
        buttonC = (Button) findViewById(R.id.btnc);
        buttonC.setOnClickListener(this);
        buttonDelAll = (Button) findViewById(R.id.btndelall);
        buttonDelAll.setOnClickListener(this);
        buttonDelSame = (Button) findViewById(R.id.btndelsame);
        buttonDelSame.setOnClickListener(this);
        buttonShowPanel = (Button) findViewById(R.id.btnshowpanel);
        buttonShowPanel.setOnClickListener(this);
        buttonStartService = (Button) findViewById(R.id.btnstartservice);
        buttonStartService.setOnClickListener(this);
        buttonStopService = (Button) findViewById(R.id.btnstopservice);
        buttonStopService.setOnClickListener(this);

    }

    @Override
    public String getTag()
    {
        return null;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
        case R.id.btna :
            getController().showView(ViewA.class, null);
            break;
        case R.id.btnb :
            getController().showView(ViewB.class, null);
            break;
        case R.id.btnc :
            getController().showView(ViewC.class, null);
            break;
        case R.id.btndelall :
            getController().killAllHistoryView();
            break;
        case R.id.btndelsame :
            getController().killAllSameView(this);
            break;
        case R.id.btnshowpanel :
            EventManager.ins().sendEvent(EventTag.FLOAT_WINDOW_SHOW_PANEL, 0, 0, null);
            break;
        case R.id.btnstartservice :
        {
            Intent intent = new Intent(this.getContext(), MainService.class);
            this.getContext().startService(intent);
        }
            break;
        case R.id.btnstopservice :
        {
            Intent intent = new Intent(this.getContext(), MainService.class);
            this.getContext().stopService(intent);
        }
            break;

        }
    }

    //
    // @Override
    // public boolean onKeyDown(int keyCode, KeyEvent event)
    // {
    // Log.d("keydown", "" + keyCode);
    // if (keyCode == KeyEvent.KEYCODE_BACK)
    // {
    // if (getController().backView())
    // {
    // return false;
    // } else
    // {
    // return true;
    // }
    // }
    // return false;
    // }

}
