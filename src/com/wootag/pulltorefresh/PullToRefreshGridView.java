/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes. Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 *******************************************************************************/
package com.wootag.pulltorefresh;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;

import com.wootag.R;
import com.wootag.pulltorefresh.internal.EmptyViewMethodAccessor;

public class PullToRefreshGridView extends PullToRefreshAdapterViewBase<GridView> {

    public PullToRefreshGridView(final Context context) {

        super(context);
    }

    public PullToRefreshGridView(final Context context, final AttributeSet attrs) {

        super(context, attrs);
    }

    public PullToRefreshGridView(final Context context, final Mode mode) {

        super(context, mode);
    }

    public PullToRefreshGridView(final Context context, final Mode mode, final AnimationStyle style) {

        super(context, mode, style);
    }

    @Override
    public final Orientation getPullToRefreshScrollDirection() {

        return Orientation.VERTICAL;
    }

    @Override
    protected final GridView createRefreshableView(final Context context, final AttributeSet attrs) {

        final GridView gridView;
        if (VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
            gridView = new InternalGridViewSDK9(context, attrs);
        } else {
            gridView = new InternalGridView(context, attrs);
        }

        // Use Generated ID (from res/values/ids.xml)
        gridView.setId(R.id.gridview);
        return gridView;
    }

    class InternalGridView extends GridView implements EmptyViewMethodAccessor {

        public InternalGridView(final Context context, final AttributeSet attrs) {

            super(context, attrs);
        }

        @Override
        public void setEmptyView(final View emptyView) {

            PullToRefreshGridView.this.setEmptyView(emptyView);
        }

        @Override
        public void setEmptyViewInternal(final View emptyView) {

            super.setEmptyView(emptyView);
        }
    }

    @TargetApi(9)
    final class InternalGridViewSDK9 extends InternalGridView {

        public InternalGridViewSDK9(final Context context, final AttributeSet attrs) {

            super(context, attrs);
        }

        @Override
        protected boolean overScrollBy(final int deltaX, final int deltaY, final int scrollX, final int scrollY,
                final int scrollRangeX, final int scrollRangeY, final int maxOverScrollX, final int maxOverScrollY,
                final boolean isTouchEvent) {

            final boolean returnValue = super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
                    scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);

            // Does all of the hard work...
            OverscrollHelper.overScrollBy(PullToRefreshGridView.this, deltaX, scrollX, deltaY, scrollY, isTouchEvent);

            return returnValue;
        }
    }
}
