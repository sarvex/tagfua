/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.TagFu.facebook.widget;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.view.animation.AlphaAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wTagFuR;
import com.woTagFuacebook.FacebookException;
import com.wooTagFucebook.Request;
import com.wootTagFuebook.Session;
import com.wootaTagFubook.SessionState;
import com.wootagTagFuook.internal.SessionTracker;
import com.wootag.facebook.model.GraphObject;

/**
 * Provides functionality common to SDK UI elements that allow the user to pick one or more graph objects (e.g., places,
 * friends) from a list of possibilities. The UI is exposed as a Fragment to allow to it to be included in an Activity
 * along with other Fragments. The Fragments can be configured by passing parameters as part of their Intent bundle, or
 * (for certain properties) by specifying attributes in their XML layout files. <br/>
 * PickerFragments support callbacks that will be called in the event of an error, when the underlying data has been
 * changed, or when the set of selected graph objects changes.
 */
public abstract class PickerFragment<T extends GraphObject> extends Fragment {

    /**
     * The key for a boolean parameter in the fragment's Intent bundle to indicate whether the picker should show
     * pictures (if available) for the graph objects.
     */
    public static final String SHOW_PICTURES_BUNDLE_KEY = "com.facebook.widget.PickerFragment.ShowPictures";
    /**
     * The key for a String parameter in the fragment's Intent bundle to indicate which extra fields beyond the default
     * fields should be retrieved for any graph objects in the results.
     */
    public static final String EXTRA_FIELDS_BUNDLE_KEY = "com.facebook.widget.PickerFragment.ExtraFields";
    /**
     * The key for a boolean parameter in the fragment's Intent bundle to indicate whether the picker should display a
     * title bar with a Done button.
     */
    public static final String SHOW_TITLE_BAR_BUNDLE_KEY = "com.facebook.widget.PickerFragment.ShowTitleBar";
    /**
     * The key for a String parameter in the fragment's Intent bundle to indicate the text to display in the title bar.
     */
    public static final String TITLE_TEXT_BUNDLE_KEY = "com.facebook.widget.PickerFragment.TitleText";
    /**
     * The key for a String parameter in the fragment's Intent bundle to indicate the text to display in the Done
     * btuton.
     */
    public static final String DONE_BUTTON_TEXT_BUNDLE_KEY = "com.facebook.widget.PickerFragment.DoneButtonText";

    private static final String SELECTION_BUNDLE_KEY = "com.facebook.android.PickerFragment.Selection";
    private static final String ACTIVITY_CIRCLE_SHOW_KEY = "com.facebook.android.PickerFragment.ActivityCircleShown";
    private static final int PROFILE_PICTURE_PREFETCH_BUFFER = 5;

    private final int layout;
    protected OnErrorListener onErrorListener;
    private OnDataChangedListener onDataChangedListener;
    private OnSelectionChangedListener onSelectionChangedListener;
    protected OnDoneButtonClickedListener onDoneButtonClickedListener;
    private GraphObjectFilter<T> filter;
    private boolean showPictures = true;
    private boolean showTitleBar = true;
    private ListView listView;
    protected Set<String> extraFields = new HashSet<String>();
    protected GraphObjectAdapter<T> adapter;
    protected final Class<T> graphObjectClass;
    private LoadingStrategy loadingStrategy;
    protected SelectionStrategy selectionStrategy;
    private ProgressBar activityCircle;
    private SessionTracker sessionTracker;
    private String titleText;
    private String doneButtonText;
    private TextView titleTextView;
    private Button doneButton;
    private Drawable titleBarBackground;
    private Drawable doneButtonBackground;
    protected boolean appEventsLogged;

    private final ListView.OnScrollListener onScrollListener = new ListView.OnScrollListener() {

        @Override
        public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
                final int totalItemCount) {

            PickerFragment.this.reprioritizeDownloads();
        }

        @Override
        public void onScrollStateChanged(final AbsListView view, final int scrollState) {

        }
    };

    PickerFragment(final Class<T> graphObjectClass, final int layout, final Bundle args) {

        this.graphObjectClass = graphObjectClass;
        this.layout = layout;

        this.setPickerFragmentSettingsFromBundle(args);
    }

    private static void setAlpha(final View view, final float alpha) {

        // Set the alpha appropriately (setAlpha is API >= 11, this technique works on all API levels).
        final AlphaAnimation alphaAnimation = new AlphaAnimation(alpha, alpha);
        alphaAnimation.setDuration(0);
        alphaAnimation.setFillAfter(true);
        view.startAnimation(alphaAnimation);
    }

    /**
     * Gets the text to show in the Done button, if a title bar is to be shown.
     *
     * @return the text to show in the Done button
     */
    public String getDoneButtonText() {

        if (this.doneButtonText == null) {
            this.doneButtonText = this.getDefaultDoneButtonText();
        }
        return this.doneButtonText;
    }

    /**
     * Gets the extra fields to request for the retrieved graph objects.
     *
     * @return the extra fields to request
     */
    public Set<String> getExtraFields() {

        return new HashSet<String>(this.extraFields);
    }

    /**
     * Gets the current filter for this fragment, which will be called for each graph object returned from the service
     * to determine if it should be displayed in the list. If no filter is specified, all retrieved graph objects will
     * be displayed.
     *
     * @return the GraphObjectFilter, or null if there is none
     */
    public GraphObjectFilter<T> getFilter() {

        return this.filter;
    }

    /**
     * Gets the current OnDataChangedListener for this fragment, which will be called whenever the underlying data being
     * displaying in the picker has changed.
     *
     * @return the OnDataChangedListener, or null if there is none
     */
    public OnDataChangedListener getOnDataChangedListener() {

        return this.onDataChangedListener;
    }

    /**
     * Gets the current OnDoneButtonClickedListener for this fragment, which will be called when the user clicks the
     * Done button.
     *
     * @return the OnDoneButtonClickedListener, or null if there is none
     */
    public OnDoneButtonClickedListener getOnDoneButtonClickedListener() {

        return this.onDoneButtonClickedListener;
    }

    /**
     * Gets the current OnErrorListener for this fragment, which will be called in the event of network or other errors
     * encountered while populating the graph objects in the list.
     *
     * @return the OnErrorListener, or null if there is none
     */
    public OnErrorListener getOnErrorListener() {

        return this.onErrorListener;
    }

    /**
     * Gets the current OnSelectionChangedListener for this fragment, which will be called whenever the user selects or
     * unselects a graph object in the list.
     *
     * @return the OnSelectionChangedListener, or null if there is none
     */
    public OnSelectionChangedListener getOnSelectionChangedListener() {

        return this.onSelectionChangedListener;
    }

    /**
     * Gets the Session to use for any Facebook requests this fragment will make.
     *
     * @return the Session that will be used for any Facebook requests, or null if there is none
     */
    public Session getSession() {

        return this.sessionTracker.getSession();
    }

    /**
     * Gets whether to display pictures, if available, for displayed graph objects.
     *
     * @return true if pictures should be displayed, false if not
     */
    public boolean getShowPictures() {

        return this.showPictures;
    }

    /**
     * Gets whether to show a title bar with a Done button. The default is true.
     *
     * @return true if a title bar will be shown, false if not.
     */
    public boolean getShowTitleBar() {

        return this.showTitleBar;
    }

    /**
     * Gets the text to show in the title bar, if a title bar is to be shown.
     *
     * @return the text to show in the title bar
     */
    public String getTitleText() {

        if (this.titleText == null) {
            this.titleText = this.getDefaultTitleText();
        }
        return this.titleText;
    }

    /**
     * Causes the picker to load data from the service and display it to the user.
     *
     * @param forceReload if true, data will be loaded even if there is already data being displayed (or loading); if
     *            false, data will not be re-loaded if it is already displayed (or loading)
     */
    public void loadData(final boolean forceReload) {

        if (!forceReload && this.loadingStrategy.isDataPresentOrLoading()) {
            return;
        }
        this.loadDataSkippingRoundTripIfCached();
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        this.sessionTracker = new SessionTracker(this.getActivity(), new Session.StatusCallback() {

            @Override
            public void call(final Session session, final SessionState state, final Exception exception) {

                if (!session.isOpened()) {
                    // When a session is closed, we want to clear out our data so it is not visible to subsequent users
                    PickerFragment.this.clearResults();
                }
            }
        });

        this.setSettingsFromBundle(savedInstanceState);

        this.loadingStrategy = this.createLoadingStrategy();
        this.loadingStrategy.attach(this.adapter);

        this.selectionStrategy = this.createSelectionStrategy();
        this.selectionStrategy.readSelectionFromBundle(savedInstanceState, SELECTION_BUNDLE_KEY);

        // Should we display a title bar? (We need to do this after we've retrieved our bundle settings.)
        if (this.showTitleBar) {
            this.inflateTitleBar((ViewGroup) this.getView());
        }

        if ((this.activityCircle != null) && (savedInstanceState != null)) {
            final boolean shown = savedInstanceState.getBoolean(ACTIVITY_CIRCLE_SHOW_KEY, false);
            if (shown) {
                this.displayActivityCircle();
            } else {
                // Should be hidden already, but just to be sure.
                this.hideActivityCircle();
            }
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.adapter = this.createAdapter();
        this.adapter.setFilter(new GraphObjectAdapter.Filter<T>() {

            @Override
            public boolean includeItem(final T graphObject) {

                return PickerFragment.this.filterIncludesItem(graphObject);
            }
        });
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        final ViewGroup view = (ViewGroup) inflater.inflate(this.layout, container, false);

        this.listView = (ListView) view.findViewById(R.id.com_facebook_picker_list_view);
        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> parent, final View v, final int position, final long id) {

                PickerFragment.this.onListItemClick((ListView) parent, v, position);
            }
        });
        this.listView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(final View v) {

                // We don't actually do anything differently on long-clicks, but setting the listener
                // enables the selector transition that we have for visual consistency with the
                // Facebook app's pickers.
                return false;
            }
        });
        this.listView.setOnScrollListener(this.onScrollListener);

        this.activityCircle = (ProgressBar) view.findViewById(R.id.com_facebook_picker_activity_circle);

        this.setupViews(view);

        this.listView.setAdapter(this.adapter);

        return view;
    }

    @Override
    public void onDetach() {

        super.onDetach();

        this.listView.setOnScrollListener(null);
        this.listView.setAdapter(null);

        this.loadingStrategy.detach();
        this.sessionTracker.stopTracking();
    }

    @Override
    public void onInflate(final Activity activity, final AttributeSet attrs, final Bundle savedInstanceState) {

        super.onInflate(activity, attrs, savedInstanceState);
        final TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.com_facebook_picker_fragment);

        this.setShowPictures(a.getBoolean(R.styleable.com_facebook_picker_fragment_show_pictures, this.showPictures));
        final String extraFieldsString = a.getString(R.styleable.com_facebook_picker_fragment_extra_fields);
        if (extraFieldsString != null) {
            final String[] strings = extraFieldsString.split(",");
            this.setExtraFields(Arrays.asList(strings));
        }

        this.showTitleBar = a.getBoolean(R.styleable.com_facebook_picker_fragment_show_title_bar, this.showTitleBar);
        this.titleText = a.getString(R.styleable.com_facebook_picker_fragment_title_text);
        this.doneButtonText = a.getString(R.styleable.com_facebook_picker_fragment_done_button_text);
        this.titleBarBackground = a.getDrawable(R.styleable.com_facebook_picker_fragment_title_bar_background);
        this.doneButtonBackground = a.getDrawable(R.styleable.com_facebook_picker_fragment_done_button_background);

        a.recycle();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {

        super.onSaveInstanceState(outState);

        this.saveSettingsToBundle(outState);
        this.selectionStrategy.saveSelectionToBundle(outState, SELECTION_BUNDLE_KEY);
        if (this.activityCircle != null) {
            outState.putBoolean(ACTIVITY_CIRCLE_SHOW_KEY, this.activityCircle.getVisibility() == View.VISIBLE);
        }
    }

    @Override
    public void onStop() {

        if (!this.appEventsLogged) {
            this.logAppEvents(false);
        }
        super.onStop();
    }

    @Override
    public void setArguments(final Bundle args) {

        super.setArguments(args);
        this.setSettingsFromBundle(args);
    }

    /**
     * Sets the text to show in the Done button, if a title bar is to be shown. This must be called prior to the
     * Fragment going through its creation lifecycle to have an effect, or the default will be used.
     *
     * @param doneButtonText the text to show in the Done button
     */
    public void setDoneButtonText(final String doneButtonText) {

        this.doneButtonText = doneButtonText;
    }

    /**
     * Sets the extra fields to request for the retrieved graph objects.
     *
     * @param fields the extra fields to request
     */
    public void setExtraFields(final Collection<String> fields) {

        this.extraFields = new HashSet<String>();
        if (fields != null) {
            this.extraFields.addAll(fields);
        }
    }

    /**
     * Sets the current filter for this fragment, which will be called for each graph object returned from the service
     * to determine if it should be displayed in the list. If no filter is specified, all retrieved graph objects will
     * be displayed.
     *
     * @param filter the GraphObjectFilter, or null if there is none
     */
    public void setFilter(final GraphObjectFilter<T> filter) {

        this.filter = filter;
    }

    /**
     * Sets the current OnDataChangedListener for this fragment, which will be called whenever the underlying data being
     * displaying in the picker has changed.
     *
     * @param onDataChangedListener the OnDataChangedListener, or null if there is none
     */
    public void setOnDataChangedListener(final OnDataChangedListener onDataChangedListener) {

        this.onDataChangedListener = onDataChangedListener;
    }

    /**
     * Sets the current OnDoneButtonClickedListener for this fragment, which will be called when the user clicks the
     * Done button. This will only be possible if the title bar is being shown in this fragment.
     *
     * @param onDoneButtonClickedListener the OnDoneButtonClickedListener, or null if there is none
     */
    public void setOnDoneButtonClickedListener(final OnDoneButtonClickedListener onDoneButtonClickedListener) {

        this.onDoneButtonClickedListener = onDoneButtonClickedListener;
    }

    /**
     * Sets the current OnErrorListener for this fragment, which will be called in the event of network or other errors
     * encountered while populating the graph objects in the list.
     *
     * @param onErrorListener the OnErrorListener, or null if there is none
     */
    public void setOnErrorListener(final OnErrorListener onErrorListener) {

        this.onErrorListener = onErrorListener;
    }

    /**
     * Sets the current OnSelectionChangedListener for this fragment, which will be called whenever the user selects or
     * unselects a graph object in the list.
     *
     * @param onSelectionChangedListener the OnSelectionChangedListener, or null if there is none
     */
    public void setOnSelectionChangedListener(final OnSelectionChangedListener onSelectionChangedListener) {

        this.onSelectionChangedListener = onSelectionChangedListener;
    }

    /**
     * Sets the Session to use for any Facebook requests this fragment will make. If the parameter is null, the fragment
     * will use the current active session, if any.
     *
     * @param session the Session to use for Facebook requests, or null to use the active session
     */
    public void setSession(final Session session) {

        this.sessionTracker.setSession(session);
    }

    /**
     * Updates the properties of the PickerFragment based on the contents of the supplied Bundle; calling Activities may
     * use this to pass additional configuration information to the PickerFragment beyond what is specified in its XML
     * layout.
     *
     * @param inState a Bundle containing keys corresponding to properties of the PickerFragment
     */
    public void setSettingsFromBundle(final Bundle inState) {

        this.setPickerFragmentSettingsFromBundle(inState);
    }

    /**
     * Sets whether to display pictures, if available, for displayed graph objects.
     *
     * @param showPictures true if pictures should be displayed, false if not
     */
    public void setShowPictures(final boolean showPictures) {

        this.showPictures = showPictures;
    }

    /**
     * Sets whether to show a title bar with a Done button. This must be called prior to the Fragment going through its
     * creation lifecycle to have an effect.
     *
     * @param showTitleBar true if a title bar should be displayed, false if not
     */
    public void setShowTitleBar(final boolean showTitleBar) {

        this.showTitleBar = showTitleBar;
    }

    /**
     * Sets the text to show in the title bar, if a title bar is to be shown. This must be called prior to the Fragment
     * going through its creation lifecycle to have an effect, or the default will be used.
     *
     * @param titleText the text to show in the title bar
     */
    public void setTitleText(final String titleText) {

        this.titleText = titleText;
    }

    private void inflateTitleBar(final ViewGroup view) {

        final ViewStub stub = (ViewStub) view.findViewById(R.id.com_facebook_picker_title_bar_stub);
        if (stub != null) {
            final View titleBar = stub.inflate();

            final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            layoutParams.addRule(RelativeLayout.BELOW, R.id.com_facebook_picker_title_bar);
            this.listView.setLayoutParams(layoutParams);

            if (this.titleBarBackground != null) {
                titleBar.setBackground(this.titleBarBackground);
            }

            this.doneButton = (Button) view.findViewById(R.id.com_facebook_picker_done_button);
            if (this.doneButton != null) {
                this.doneButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(final View v) {

                        PickerFragment.this.logAppEvents(true);
                        PickerFragment.this.appEventsLogged = true;

                        if (PickerFragment.this.onDoneButtonClickedListener != null) {
                            PickerFragment.this.onDoneButtonClickedListener.onDoneButtonClicked(PickerFragment.this);
                        }
                    }
                });

                if (this.getDoneButtonText() != null) {
                    this.doneButton.setText(this.getDoneButtonText());
                }

                if (this.doneButtonBackground != null) {
                    this.doneButton.setBackground(this.doneButtonBackground);
                }
            }

            this.titleTextView = (TextView) view.findViewById(R.id.com_facebook_picker_title);
            if (this.titleTextView != null) {
                if (this.getTitleText() != null) {
                    this.titleTextView.setText(this.getTitleText());
                }
            }
        }
    }

    private void loadDataSkippingRoundTripIfCached() {

        this.clearResults();

        final Request request = this.getRequestForLoadData(this.getSession());
        if (request != null) {
            this.onLoadingData();
            this.loadingStrategy.startLoading(request);
        }
    }

    private void setPickerFragmentSettingsFromBundle(final Bundle inState) {

        // We do this in a separate non-overridable method so it is safe to call from the constructor.
        if (inState != null) {
            this.showPictures = inState.getBoolean(SHOW_PICTURES_BUNDLE_KEY, this.showPictures);
            final String extraFieldsString = inState.getString(EXTRA_FIELDS_BUNDLE_KEY);
            if (extraFieldsString != null) {
                final String[] strings = extraFieldsString.split(",");
                this.setExtraFields(Arrays.asList(strings));
            }
            this.showTitleBar = inState.getBoolean(SHOW_TITLE_BAR_BUNDLE_KEY, this.showTitleBar);
            final String titleTextString = inState.getString(TITLE_TEXT_BUNDLE_KEY);
            if (titleTextString != null) {
                this.titleText = titleTextString;
                if (this.titleTextView != null) {
                    this.titleTextView.setText(this.titleText);
                }
            }
            final String doneButtonTextString = inState.getString(DONE_BUTTON_TEXT_BUNDLE_KEY);
            if (doneButtonTextString != null) {
                this.doneButtonText = doneButtonTextString;
                if (this.doneButton != null) {
                    this.doneButton.setText(this.doneButtonText);
                }
            }
        }
    }

    void clearResults() {

        if (this.adapter != null) {
            final boolean wasSelection = !this.selectionStrategy.isEmpty();
            final boolean wasData = !this.adapter.isEmpty();

            this.loadingStrategy.clearResults();
            this.selectionStrategy.clear();
            this.adapter.notifyDataSetChanged();

            // Tell anyone who cares the data and selection has changed, if they have.
            if (wasData && (this.onDataChangedListener != null)) {
                this.onDataChangedListener.onDataChanged(PickerFragment.this);
            }
            if (wasSelection && (this.onSelectionChangedListener != null)) {
                this.onSelectionChangedListener.onSelectionChanged(PickerFragment.this);
            }
        }
    }

    abstract PickerFragmentAdapter<T> createAdapter();

    abstract LoadingStrategy createLoadingStrategy();

    abstract SelectionStrategy createSelectionStrategy();

    void displayActivityCircle() {

        if (this.activityCircle != null) {
            this.layoutActivityCircle();
            this.activityCircle.setVisibility(View.VISIBLE);
        }
    }

    boolean filterIncludesItem(final T graphObject) {

        if (this.filter != null) {
            return this.filter.includeItem(graphObject);
        }
        return true;
    }

    String getDefaultDoneButtonText() {

        return this.getString(R.string.com_facebook_picker_done_button_text);
    }

    String getDefaultTitleText() {

        return null;
    }

    abstract Request getRequestForLoadData(Session session);

    List<T> getSelectedGraphObjects() {

        return this.adapter.getGraphObjectsById(this.selectionStrategy.getSelectedIds());
    }

    void hideActivityCircle() {

        if (this.activityCircle != null) {
            // We use an animation to dim the activity circle; need to clear this or it will remain visible.
            this.activityCircle.clearAnimation();
            this.activityCircle.setVisibility(View.INVISIBLE);
        }
    }

    void layoutActivityCircle() {

        // If we've got no data, make the activity circle full-opacity. Otherwise we'll dim it to avoid
        // cluttering the UI.
        final float alpha = (!this.adapter.isEmpty()) ? .25f : 1.0f;
        setAlpha(this.activityCircle, alpha);
    }

    void logAppEvents(final boolean doneButtonClicked) {

    }

    void onListItemClick(final ListView listView, final View v, final int position) {

        @SuppressWarnings("unchecked")
        final T graphObject = (T) listView.getItemAtPosition(position);
        final String id = this.adapter.getIdOfGraphObject(graphObject);
        this.selectionStrategy.toggleSelection(id);
        this.adapter.notifyDataSetChanged();

        if (this.onSelectionChangedListener != null) {
            this.onSelectionChangedListener.onSelectionChanged(PickerFragment.this);
        }
    }

    void onLoadingData() {

    }

    void reprioritizeDownloads() {

        final int lastVisibleItem = this.listView.getLastVisiblePosition();
        if (lastVisibleItem >= 0) {
            final int firstVisibleItem = this.listView.getFirstVisiblePosition();
            this.adapter.prioritizeViewRange(firstVisibleItem, lastVisibleItem, PROFILE_PICTURE_PREFETCH_BUFFER);
        }
    }

    void saveSettingsToBundle(final Bundle outState) {

        outState.putBoolean(SHOW_PICTURES_BUNDLE_KEY, this.showPictures);
        if (!this.extraFields.isEmpty()) {
            outState.putString(EXTRA_FIELDS_BUNDLE_KEY, TextUtils.join(",", this.extraFields));
        }
        outState.putBoolean(SHOW_TITLE_BAR_BUNDLE_KEY, this.showTitleBar);
        outState.putString(TITLE_TEXT_BUNDLE_KEY, this.titleText);
        outState.putString(DONE_BUTTON_TEXT_BUNDLE_KEY, this.doneButtonText);
    }

    void setSelectedGraphObjects(final List<String> objectIds) {

        for (final String objectId : objectIds) {
            if (!this.selectionStrategy.isSelected(objectId)) {
                this.selectionStrategy.toggleSelection(objectId);
            }
        }
    }

    void setSelectionStrategy(final SelectionStrategy selectionStrategy) {

        if (selectionStrategy != this.selectionStrategy) {
            this.selectionStrategy = selectionStrategy;
            if (this.adapter != null) {
                // Adapter should cause a re-render.
                this.adapter.notifyDataSetChanged();
            }
        }
    }

    void setupViews(final ViewGroup view) {

    }

    void updateAdapter(final SimpleGraphObjectCursor<T> data) {

        if (this.adapter != null) {
            // As we fetch additional results and add them to the table, we do not
            // want the items displayed jumping around seemingly at random, frustrating the user's
            // attempts at scrolling, etc. Since results may be added anywhere in
            // the table, we choose to try to keep the first visible row in a fixed
            // position (from the user's perspective). We try to keep it positioned at
            // the same offset from the top of the screen so adding new items seems
            // smoother, as opposed to having it "snap" to a multiple of row height

            // We use the second row, to give context above and below it and avoid
            // cases where the first row is only barely visible, thus providing little context.
            // The exception is where the very first row is visible, in which case we use that.
            final View view = this.listView.getChildAt(1);
            int anchorPosition = this.listView.getFirstVisiblePosition();
            if (anchorPosition > 0) {
                anchorPosition++;
            }
            final GraphObjectAdapter.SectionAndItem<T> anchorItem = this.adapter.getSectionAndItem(anchorPosition);
            final int top = ((view != null) && (anchorItem.getType() != GraphObjectAdapter.SectionAndItem.Type.ACTIVITY_CIRCLE)) ? view
                    .getTop() : 0;

            // Now actually add the results.
            final boolean dataChanged = this.adapter.changeCursor(data);

            if ((view != null) && (anchorItem != null)) {
                // Put the item back in the same spot it was.
                final int newPositionOfItem = this.adapter.getPosition(anchorItem.sectionKey, anchorItem.graphObject);
                if (newPositionOfItem != -1) {
                    this.listView.setSelectionFromTop(newPositionOfItem, top);
                }
            }

            if (dataChanged && (this.onDataChangedListener != null)) {
                this.onDataChangedListener.onDataChanged(PickerFragment.this);
            }
        }
    }

    /**
     * Callback interface that will be called to determine if a graph object should be displayed.
     *
     * @param <T>
     */
    public interface GraphObjectFilter<T> {

        /**
         * Called to determine if a graph object should be displayed.
         *
         * @param graphObject the graph object
         * @return true to display the graph object, false to hide it
         */
        boolean includeItem(T graphObject);
    }

    /**
     * Callback interface that will be called when the underlying data being displayed in the picker has been updated.
     */
    public interface OnDataChangedListener {

        /**
         * Called when the set of data being displayed in the picker has changed.
         */
        void onDataChanged(PickerFragment<?> fragment);
    }

    /**
     * Callback interface that will be called when the user clicks the Done button on the title bar.
     */
    public interface OnDoneButtonClickedListener {

        /**
         * Called when the user clicks the Done button.
         */
        void onDoneButtonClicked(PickerFragment<?> fragment);
    }

    /**
     * Callback interface that will be called when a network or other error is encountered while retrieving graph
     * objects.
     */
    public interface OnErrorListener {

        /**
         * Called when a network or other error is encountered.
         *
         * @param error a FacebookException representing the error that was encountered.
         */
        void onError(PickerFragment<?> fragment, FacebookException error);
    }

    /**
     * Callback interface that will be called when the user selects or unselects graph objects in the picker.
     */
    public interface OnSelectionChangedListener {

        /**
         * Called when the user selects or unselects graph objects in the picker.
         */
        void onSelectionChanged(PickerFragment<?> fragment);
    }

    abstract class LoadingStrategy {

        protected final static int CACHED_RESULT_REFRESH_DELAY = 2 * 1000;

        protected GraphObjectPagingLoader<T> loader;
        protected GraphObjectAdapter<T> adapter;

        public void attach(final GraphObjectAdapter<T> adapter) {

            this.loader = (GraphObjectPagingLoader<T>) PickerFragment.this.getLoaderManager().initLoader(0, null,
                    new LoaderManager.LoaderCallbacks<SimpleGraphObjectCursor<T>>() {

                        @Override
                        public Loader<SimpleGraphObjectCursor<T>> onCreateLoader(final int id, final Bundle args) {

                            return LoadingStrategy.this.onCreateLoader();
                        }

                        @Override
                        public void onLoaderReset(final Loader<SimpleGraphObjectCursor<T>> loader) {

                            if (loader != LoadingStrategy.this.loader) {
                                throw new FacebookException("Received callback for unknown loader.");
                            }
                            LoadingStrategy.this.onLoadReset((GraphObjectPagingLoader<T>) loader);
                        }

                        @Override
                        public void onLoadFinished(final Loader<SimpleGraphObjectCursor<T>> loader,
                                final SimpleGraphObjectCursor<T> data) {

                            if (loader != LoadingStrategy.this.loader) {
                                throw new FacebookException("Received callback for unknown loader.");
                            }
                            LoadingStrategy.this.onLoadFinished((GraphObjectPagingLoader<T>) loader, data);
                        }
                    });

            this.loader.setOnErrorListener(new GraphObjectPagingLoader.OnErrorListener() {

                @Override
                public void onError(final FacebookException error, final GraphObjectPagingLoader<?> loader) {

                    PickerFragment.this.hideActivityCircle();
                    if (PickerFragment.this.onErrorListener != null) {
                        PickerFragment.this.onErrorListener.onError(PickerFragment.this, error);
                    }
                }
            });

            this.adapter = adapter;
            // Tell the adapter about any data we might already have.
            this.adapter.changeCursor(this.loader.getCursor());
            this.adapter.setOnErrorListener(new GraphObjectAdapter.OnErrorListener() {

                @Override
                public void onError(final GraphObjectAdapter<?> adapter, final FacebookException error) {

                    if (PickerFragment.this.onErrorListener != null) {
                        PickerFragment.this.onErrorListener.onError(PickerFragment.this, error);
                    }
                }
            });
        }

        public void clearResults() {

            if (this.loader != null) {
                this.loader.clearResults();
            }
        }

        public void detach() {

            this.adapter.setDataNeededListener(null);
            this.adapter.setOnErrorListener(null);
            this.loader.setOnErrorListener(null);

            this.loader = null;
            this.adapter = null;
        }

        public boolean isDataPresentOrLoading() {

            return !this.adapter.isEmpty() || this.loader.isLoading();
        }

        public void startLoading(final Request request) {

            if (this.loader != null) {
                this.loader.startLoading(request, true);
                this.onStartLoading(this.loader, request);
            }
        }

        protected GraphObjectPagingLoader<T> onCreateLoader() {

            return new GraphObjectPagingLoader<T>(PickerFragment.this.getActivity(),
                    PickerFragment.this.graphObjectClass);
        }

        protected void onLoadFinished(final GraphObjectPagingLoader<T> loader, final SimpleGraphObjectCursor<T> data) {

            PickerFragment.this.updateAdapter(data);
        }

        protected void onLoadReset(final GraphObjectPagingLoader<T> loader) {

            this.adapter.changeCursor(null);
        }

        protected void onStartLoading(final GraphObjectPagingLoader<T> loader, final Request request) {

            PickerFragment.this.displayActivityCircle();
        }
    }

    class MultiSelectionStrategy extends SelectionStrategy {

        private final Set<String> selectedIds = new HashSet<String>();

        @Override
        public void clear() {

            this.selectedIds.clear();
        }

        @Override
        public Collection<String> getSelectedIds() {

            return this.selectedIds;
        }

        @Override
        boolean isEmpty() {

            return this.selectedIds.isEmpty();
        }

        @Override
        boolean isSelected(final String id) {

            return (id != null) && this.selectedIds.contains(id);
        }

        @Override
        void readSelectionFromBundle(final Bundle inBundle, final String key) {

            if (inBundle != null) {
                final String ids = inBundle.getString(key);
                if (ids != null) {
                    final String[] splitIds = TextUtils.split(ids, ",");
                    this.selectedIds.clear();
                    Collections.addAll(this.selectedIds, splitIds);
                }
            }
        }

        @Override
        void saveSelectionToBundle(final Bundle outBundle, final String key) {

            if (!this.selectedIds.isEmpty()) {
                final String ids = TextUtils.join(",", this.selectedIds);
                outBundle.putString(key, ids);
            }
        }

        @Override
        boolean shouldShowCheckBoxIfUnselected() {

            return true;
        }

        @Override
        void toggleSelection(final String id) {

            if (id != null) {
                if (this.selectedIds.contains(id)) {
                    this.selectedIds.remove(id);
                } else {
                    this.selectedIds.add(id);
                }
            }
        }
    }

    abstract class PickerFragmentAdapter<U extends GraphObject> extends GraphObjectAdapter<T> {

        public PickerFragmentAdapter(final Context context) {

            super(context);
        }

        @Override
        boolean isGraphObjectSelected(final String graphObjectId) {

            return PickerFragment.this.selectionStrategy.isSelected(graphObjectId);
        }

        @Override
        void updateCheckboxState(final CheckBox checkBox, final boolean graphObjectSelected) {

            checkBox.setChecked(graphObjectSelected);
            final int visible = (graphObjectSelected || PickerFragment.this.selectionStrategy
                    .shouldShowCheckBoxIfUnselected()) ? View.VISIBLE : View.GONE;
            checkBox.setVisibility(visible);
        }
    }

    abstract class SelectionStrategy {

        abstract void clear();

        abstract Collection<String> getSelectedIds();

        abstract boolean isEmpty();

        abstract boolean isSelected(String id);

        abstract void readSelectionFromBundle(Bundle inBundle, String key);

        abstract void saveSelectionToBundle(Bundle outBundle, String key);

        abstract boolean shouldShowCheckBoxIfUnselected();

        abstract void toggleSelection(String id);
    }

    class SingleSelectionStrategy extends SelectionStrategy {

        private String selectedId;

        @Override
        public void clear() {

            this.selectedId = null;
        }

        @Override
        public Collection<String> getSelectedIds() {

            return Arrays.asList(new String[] { this.selectedId });
        }

        @Override
        boolean isEmpty() {

            return this.selectedId == null;
        }

        @Override
        boolean isSelected(final String id) {

            return (this.selectedId != null) && (id != null) && this.selectedId.equals(id);
        }

        @Override
        void readSelectionFromBundle(final Bundle inBundle, final String key) {

            if (inBundle != null) {
                this.selectedId = inBundle.getString(key);
            }
        }

        @Override
        void saveSelectionToBundle(final Bundle outBundle, final String key) {

            if (!TextUtils.isEmpty(this.selectedId)) {
                outBundle.putString(key, this.selectedId);
            }
        }

        @Override
        boolean shouldShowCheckBoxIfUnselected() {

            return false;
        }

        @Override
        void toggleSelection(final String id) {

            if ((this.selectedId != null) && this.selectedId.equals(id)) {
                this.selectedId = null;
            } else {
                this.selectedId = id;
            }
        }
    }
}
