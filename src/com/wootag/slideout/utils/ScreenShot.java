package com.wootag.slideout.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.view.View;

public class ScreenShot {

    private final View view;

    /** Create snapshot handler that captures the root of the whole activity. */
    public ScreenShot(final Activity activity) {

        final View contentView = activity.findViewById(android.R.id.content);
        this.view = contentView.getRootView();
    }

    /** Create snapshot handler that captures the view with target id of the activity. */
    public ScreenShot(final Activity activity, final int id) {

        this.view = activity.findViewById(id);
    }

    /** Create snapshots based on the view and its children. */
    public ScreenShot(final View root) {

        this.view = root;
    }

    /** Take a snapshot of the view. */
    public Bitmap snap() {

        final Bitmap bitmap = Bitmap.createBitmap(this.view.getWidth(), this.view.getHeight(), Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        this.view.draw(canvas);
        return bitmap;
    }
}
