package test.viewdemo.context;

import org.rdengine.view.manager.BaseActivity;

import test.viewdemo.view.ViewA;
import android.os.Bundle;

public class MainActivity extends BaseActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.showView(ViewA.class, null);
    }

}
