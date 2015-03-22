package com.TagFu.pulltorefresh;

import java.util.HashSet;
import java.util.Set;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import com.TagFu.pulltorefresh.internal.LoadingLayout;

public class LoadingLayoutProxy implements ILoadingLayout {

    private final Set<LoadingLayout> mLoadingLayouts;

    LoadingLayoutProxy() {

        this.mLoadingLayouts = new HashSet<LoadingLayout>();
    }

    /**
     * This allows you to add extra LoadingLayout instances to this proxy. This is only necessary if you keep your own
     * instances, and want to have them included in any
     * {@link PullToRefreshBase#createLoadingLayoutProxy(boolean, boolean) createLoadingLayoutProxy(...)} calls.
     *
     * @param layout - LoadingLayout to have included.
     */
    public void addLayout(final LoadingLayout layout) {

        if (null != layout) {
            this.mLoadingLayouts.add(layout);
        }
    }

    @Override
    public void setLastUpdatedLabel(final CharSequence label) {

        for (final LoadingLayout layout : this.mLoadingLayouts) {
            layout.setLastUpdatedLabel(label);
        }
    }

    @Override
    public void setLoadingDrawable(final Drawable drawable) {

        for (final LoadingLayout layout : this.mLoadingLayouts) {
            layout.setLoadingDrawable(drawable);
        }
    }

    @Override
    public void setPullLabel(final CharSequence label) {

        for (final LoadingLayout layout : this.mLoadingLayouts) {
            layout.setPullLabel(label);
        }
    }

    @Override
    public void setRefreshingLabel(final CharSequence refreshingLabel) {

        for (final LoadingLayout layout : this.mLoadingLayouts) {
            layout.setRefreshingLabel(refreshingLabel);
        }
    }

    @Override
    public void setReleaseLabel(final CharSequence label) {

        for (final LoadingLayout layout : this.mLoadingLayouts) {
            layout.setReleaseLabel(label);
        }
    }

    @Override
    public void setTextTypeface(final Typeface tf) {

        for (final LoadingLayout layout : this.mLoadingLayouts) {
            layout.setTextTypeface(tf);
        }
    }
}
