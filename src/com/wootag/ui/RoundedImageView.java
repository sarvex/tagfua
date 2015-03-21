/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundedImageView extends ImageView {

    // private static final Logger LOG = LoggerManager.getLogger();

    public RoundedImageView(final Context context) {

        super(context);
    }

    public RoundedImageView(final Context context, final AttributeSet attrs) {

        super(context, attrs);
    }

    public RoundedImageView(final Context context, final AttributeSet attrs, final int defStyle) {

        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(final Canvas canvas) {

        final Drawable drawable = this.getDrawable();

        if (drawable == null) {
            return;
        }

        if ((this.getWidth() == 0) || (this.getHeight() == 0)) {
            return;
        }
        final Bitmap b = ((BitmapDrawable) drawable).getBitmap();
        final Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);

        final int w = this.getWidth();// h = getHeight();

        final Bitmap roundBitmap = getCroppedBitmap(bitmap, w);
        canvas.drawBitmap(roundBitmap, 0, 0, null);

    }

    public static Bitmap getCroppedBitmap(final Bitmap bmp, final int radius) {

        Bitmap sbmp;
        if ((bmp.getWidth() != radius) || (bmp.getHeight() != radius)) {
            sbmp = Bitmap.createScaledBitmap(bmp, radius, radius, false);
        } else {
            sbmp = bmp;
        }
        final Bitmap output = Bitmap.createBitmap(sbmp.getWidth(), sbmp.getHeight(), Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        // final int color = 0xffa19774;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle((sbmp.getWidth() / 2) + 0.7f, (sbmp.getHeight() / 2) + 0.7f, (sbmp.getWidth() / 2) + 0.1f,
                paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);

        return output;
    }

}
