package test.viewdemo.view;

import org.rdengine.android.R;
import org.rdengine.view.manager.BaseView;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ViewC extends BaseView implements OnClickListener
{

    public ViewC(Context context)
    {
        super(context);
        setContentView(R.layout.viewc);
    }

    static int number = 0;
    Button buttonA, buttonB, buttonC, buttonDelAll, buttonDelSame;

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
        }
    }
    //
    // @Override
    // public boolean onKeyDown(int keyCode, KeyEvent event)
    // {
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
