/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.TagFu.facebook.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.wTagFuR;
import com.woTagFuacebook.AppEventsLogger;
import com.wooTagFucebook.FacebookException;
import com.wootTagFuebook.Request;
import com.wootaTagFubook.Session;
import com.wootagTagFuook.internal.AnalyticsEvents;
import com.wootag.facebook.model.GraphUser;

/**
 * Provides a Fragment that displays a list of a user's friends and allows one or more of the friends to be selected.
 */
public class FriendPickerFragment extends PickerFragment<GraphUser> {

    /**
     * The key for a String parameter in the fragment's Intent bundle to indicate what user's friends should be shown.
     * The default is to display the currently authenticated user's friends.
     */
    public static final String USER_ID_BUNDLE_KEY = "com.facebook.widget.FriendPickerFragment.UserId";
    /**
     * The key for a boolean parameter in the fragment's Intent bundle to indicate whether the picker should allow more
     * than one friend to be selected or not.
     */
    public static final String MULTI_SELECT_BUNDLE_KEY = "com.facebook.widget.FriendPickerFragment.MultiSelect";

    private static final String ID = "id";
    private static final String NAME = "name";

    private String userId;

    private boolean multiSelect = true;

    private final List<String> preSelectedFriendIds = new ArrayList<String>();

    /**
     * Default constructor. Creates a Fragment with all default properties.
     */
    public FriendPickerFragment() {

        this(null);
    }

    /**
     * Constructor.
     *
     * @param args a Bundle that optionally contains one or more values containing additional configuration information
     *            for the Fragment.
     */
    @SuppressLint("ValidFragment")
    public FriendPickerFragment(final Bundle args) {

        super(GraphUser.class, R.layout.com_facebook_friendpickerfragment, args);
        this.setFriendPickerSettingsFromBundle(args);
    }

    /**
     * Gets whether the user can select multiple friends, or only one friend.
     *
     * @return true if the user can select multiple friends, false if only one friend
     */
    public boolean getMultiSelect() {

        return this.multiSelect;
    }

    /**
     * Gets the currently-selected list of users.
     *
     * @return the currently-selected list of users
     */
    public List<GraphUser> getSelection() {

        return this.getSelectedGraphObjects();
    }

    /**
     * Gets the ID of the user whose friends should be displayed. If null, the default is to show the currently
     * authenticated user's friends.
     *
     * @return the user ID, or null
     */
    public String getUserId() {

        return this.userId;
    }

    @Override
    public void loadData(final boolean forceReload) {

        super.loadData(forceReload);
        this.setSelectedGraphObjects(this.preSelectedFriendIds);
    }

    @Override
    public void onInflate(final Activity activity, final AttributeSet attrs, final Bundle savedInstanceState) {

        super.onInflate(activity, attrs, savedInstanceState);
        final TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.com_facebook_friend_picker_fragment);

        this.setMultiSelect(a
                .getBoolean(R.styleable.com_facebook_friend_picker_fragment_multi_select, this.multiSelect));

        a.recycle();
    }

    /**
     * Sets whether the user can select multiple friends, or only one friend.
     *
     * @param multiSelect true if the user can select multiple friends, false if only one friend
     */
    public void setMultiSelect(final boolean multiSelect) {

        if (this.multiSelect != multiSelect) {
            this.multiSelect = multiSelect;
            this.setSelectionStrategy(this.createSelectionStrategy());
        }
    }

    /**
     * Sets the list of friends for pre selection. These friends will be selected by default.
     *
     * @param graphUsers list of friends as GraphUsers
     */
    public void setSelection(final GraphUser... graphUsers) {

        this.setSelection(Arrays.asList(graphUsers));
    }

    /**
     * Sets the list of friends for pre selection. These friends will be selected by default.
     *
     * @param graphUsers list of friends as GraphUsers
     */
    public void setSelection(final List<GraphUser> graphUsers) {

        final List<String> userIds = new ArrayList<String>();
        for (final GraphUser graphUser : graphUsers) {
            userIds.add(graphUser.getId());
        }
        this.setSelectionByIds(userIds);
    }

    /**
     * Sets the list of friends for pre selection. These friends will be selected by default.
     *
     * @param userIds list of friends as ids
     */
    public void setSelectionByIds(final List<String> userIds) {

        this.preSelectedFriendIds.addAll(userIds);
    }

    /**
     * Sets the list of friends for pre selection. These friends will be selected by default.
     *
     * @param userIds list of friends as ids
     */
    public void setSelectionByIds(final String... userIds) {

        this.setSelectionByIds(Arrays.asList(userIds));
    }

    @Override
    public void setSettingsFromBundle(final Bundle inState) {

        super.setSettingsFromBundle(inState);
        this.setFriendPickerSettingsFromBundle(inState);
    }

    /**
     * Sets the ID of the user whose friends should be displayed. If null, the default is to show the currently
     * authenticated user's friends.
     *
     * @param userId the user ID, or null
     */
    public void setUserId(final String userId) {

        this.userId = userId;
    }

    private Request createRequest(final String userID, final Set<String> extraFields, final Session session) {

        final Request request = Request.newGraphPathRequest(session, userID + "/friends", null);

        final Set<String> fields = new HashSet<String>(extraFields);
        final String[] requiredFields = new String[] { ID, NAME };
        fields.addAll(Arrays.asList(requiredFields));

        final String pictureField = this.adapter.getPictureFieldSpecifier();
        if (pictureField != null) {
            fields.add(pictureField);
        }

        final Bundle parameters = request.getParameters();
        parameters.putString("fields", TextUtils.join(",", fields));
        request.setParameters(parameters);

        return request;
    }

    private void setFriendPickerSettingsFromBundle(final Bundle inState) {

        // We do this in a separate non-overridable method so it is safe to call from the constructor.
        if (inState != null) {
            if (inState.containsKey(USER_ID_BUNDLE_KEY)) {
                this.setUserId(inState.getString(USER_ID_BUNDLE_KEY));
            }
            this.setMultiSelect(inState.getBoolean(MULTI_SELECT_BUNDLE_KEY, this.multiSelect));
        }
    }

    @Override
    PickerFragmentAdapter<GraphUser> createAdapter() {

        final PickerFragmentAdapter<GraphUser> adapter = new PickerFragmentAdapter<GraphUser>(this.getActivity()) {

            @Override
            protected int getDefaultPicture() {

                return R.drawable.com_facebook_profile_default_icon;
            }

            @Override
            protected int getGraphObjectRowLayoutId(final GraphUser graphObject) {

                return R.layout.com_facebook_picker_list_row;
            }

        };
        adapter.setShowCheckbox(true);
        adapter.setShowPicture(this.getShowPictures());
        adapter.setSortFields(Arrays.asList(new String[] { NAME }));
        adapter.setGroupByField(NAME);

        return adapter;
    }

    @Override
    LoadingStrategy createLoadingStrategy() {

        return new ImmediateLoadingStrategy();
    }

    @Override
    SelectionStrategy createSelectionStrategy() {

        return this.multiSelect ? new MultiSelectionStrategy() : new SingleSelectionStrategy();
    }

    @Override
    String getDefaultTitleText() {

        return this.getString(R.string.com_facebook_choose_friends);
    }

    @Override
    Request getRequestForLoadData(final Session session) {

        if (this.adapter == null) {
            throw new FacebookException("Can't issue requests until Fragment has been created.");
        }

        final String userToFetch = (this.userId != null) ? this.userId : "me";
        return this.createRequest(userToFetch, this.extraFields, session);
    }

    @Override
    void logAppEvents(final boolean doneButtonClicked) {

        final AppEventsLogger logger = AppEventsLogger.newLogger(this.getActivity(), this.getSession());
        final Bundle parameters = new Bundle();

        // If Done was clicked, we know this completed successfully. If not, we don't know (caller might have
        // dismissed us in response to selection changing, or user might have hit back button). Either way
        // we'll log the number of selections.
        final String outcome = doneButtonClicked ? AnalyticsEvents.PARAMETER_DIALOG_OUTCOME_VALUE_COMPLETED
                : AnalyticsEvents.PARAMETER_DIALOG_OUTCOME_VALUE_UNKNOWN;
        parameters.putString(AnalyticsEvents.PARAMETER_DIALOG_OUTCOME, outcome);
        parameters.putInt("num_friends_picked", this.getSelection().size());

        logger.logSdkEvent(AnalyticsEvents.EVENT_FRIEND_PICKER_USAGE, null, parameters);
    }

    @Override
    void saveSettingsToBundle(final Bundle outState) {

        super.saveSettingsToBundle(outState);

        outState.putString(USER_ID_BUNDLE_KEY, this.userId);
        outState.putBoolean(MULTI_SELECT_BUNDLE_KEY, this.multiSelect);
    }

    private class ImmediateLoadingStrategy extends LoadingStrategy {

        private void followNextLink() {

            // This may look redundant, but this causes the circle to be alpha-dimmed if we have results.
            FriendPickerFragment.this.displayActivityCircle();

            this.loader.followNextLink();
        }

        @Override
        protected void onLoadFinished(final GraphObjectPagingLoader<GraphUser> loader,
                final SimpleGraphObjectCursor<GraphUser> data) {

            super.onLoadFinished(loader, data);

            // We could be called in this state if we are clearing data or if we are being re-attached
            // in the middle of a query.
            if ((data == null) || loader.isLoading()) {
                return;
            }

            if (data.areMoreObjectsAvailable()) {
                // We got results, but more are available.
                this.followNextLink();
            } else {
                // We finished loading results.
                FriendPickerFragment.this.hideActivityCircle();

                // If this was from the cache, schedule a delayed refresh query (unless we got no results
                // at all, in which case refresh immediately.
                if (data.isFromCache()) {
                    loader.refreshOriginalRequest(data.getCount() == 0 ? CACHED_RESULT_REFRESH_DELAY : 0);
                }
            }
        }
    }
}
