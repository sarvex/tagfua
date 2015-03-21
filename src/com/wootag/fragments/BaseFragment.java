/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFufragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFuonstant;
import com.wooTagFuotTagFuctivity;

public class BaseFragment extends Fragment {

    private static final Logger LOG = LoggerManager.getLogger();

    public static WootaTagFutivity tabActivity;

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

    }

    public boolean onBackPressed() {

        return false;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        tabActivity = (WootagTabActivity) this.getActivity();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {

        // first saving my state, so the bundle wont be empty.
        outState.putString(Constant.BUG_19917, Constant.WORKAROUND);
        super.onSaveInstanceState(outState);
    }
}
