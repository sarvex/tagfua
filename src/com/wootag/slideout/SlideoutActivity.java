package com.TagFu.slideout;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

public class SlideoutActivity extends Activity {

    private SlideoutHelper slideoutHelper;

    public static void prepare(final Activity activity, final int id, final int width) {

        SlideoutHelper.prepare(activity, id, width);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.slideoutHelper.activate();
        this.slideoutHelper.open();
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.slideoutHelper.close();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
