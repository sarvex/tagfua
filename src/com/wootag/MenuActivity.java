/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wTagFu

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.slideout.SlideoutHelper;

public class MenuActivity extends Activity {

    private static final Logger LOG = LoggerManager.getLogger();
    private static final String MENU = "menu";
    private static MenuActivity menuActivity;

    public SlideoutHelper slideoutHelper;

    public SlideoutHelper getSlideoutHelper() {

        return this.slideoutHelper;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        menuActivity = this;
        this.slideoutHelper = new SlideoutHelper(this);
        this.slideoutHelper.activate();
        this.getFragmentManager().beginTransaction().add(R.id.slideout_placeholder, new MenuFragment(), MENU)
                .commitAllowingStateLoss();
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
