/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.wootag.facebook.widget;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.json.JSONObject;

import com.wootag.R;
import com.wootag.facebook.FacebookException;
import com.wootag.facebook.internal.ImageDownloader;
import com.wootag.facebook.internal.ImageRequest;
import com.wootag.facebook.internal.ImageResponse;
import com.wootag.facebook.model.GraphObject;

class GraphObjectAdapter<T extends GraphObject> extends BaseAdapter implements SectionIndexer {

    private static final int DISPLAY_SECTIONS_THRESHOLD = 1;
    private static final int HEADER_VIEW_TYPE = 0;
    private static final int GRAPH_OBJECT_VIEW_TYPE = 1;
    private static final int ACTIVITY_CIRCLE_VIEW_TYPE = 2;
    private static final int MAX_PREFETCHED_PICTURES = 20;

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String PICTURE = "picture";

    private final Map<String, ImageRequest> pendingRequests = new HashMap<String, ImageRequest>();
    private final LayoutInflater inflater;
    private List<String> sectionKeys = new ArrayList<String>();
    private Map<String, ArrayList<T>> graphObjectsBySection = new HashMap<String, ArrayList<T>>();
    private Map<String, T> graphObjectsById = new HashMap<String, T>();
    private boolean displaySections;
    protected List<String> sortFields;
    private String groupByField;
    private boolean showPicture;
    private boolean showCheckbox;
    private Filter<T> filter;
    private DataNeededListener dataNeededListener;
    private GraphObjectCursor<T> cursor;
    private final Context context;
    private final Map<String, ImageResponse> prefetchedPictureCache = new HashMap<String, ImageResponse>();
    private final List<String> prefetchedProfilePictureIds = new ArrayList<String>();
    private OnErrorListener onErrorListener;

    public GraphObjectAdapter(final Context context) {

        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    static int compareGraphObjects(final GraphObject a, final GraphObject graphObject,
            final Collection<String> sortFields, final Collator collator) {

        for (final String sortField : sortFields) {
            final String sa = (String) a.getProperty(sortField);
            final String sb = (String) graphObject.getProperty(sortField);

            if ((sa != null) && (sb != null)) {
                final int result = collator.compare(sa, sb);
                if (result != 0) {
                    return result;
                }
            } else if (!((sa == null) && (sb == null))) {
                return (sa == null) ? -1 : 1;
            }
        }
        return 0;
    }

    @Override
    public boolean areAllItemsEnabled() {

        return this.displaySections;
    }

    public boolean changeCursor(final GraphObjectCursor<T> cursor) {

        if (this.cursor == cursor) {
            return false;
        }
        if (this.cursor != null) {
            this.cursor.close();
        }
        this.cursor = cursor;

        this.rebuildAndNotify();
        return true;
    }

    @Override
    public int getCount() {

        if (this.sectionKeys.size() == 0) {
            return 0;
        }

        // If we are not displaying sections, we don't display a header; otherwise, we have one header per item in
        // addition to the actual items.
        int count = (this.displaySections) ? this.sectionKeys.size() : 0;
        for (final List<T> section : this.graphObjectsBySection.values()) {
            count += section.size();
        }

        // If we should show a cell with an activity circle indicating more data is coming, add it to the count.
        if (this.shouldShowActivityCircleCell()) {
            ++count;
        }

        return count;
    }

    public GraphObjectCursor<T> getCursor() {

        return this.cursor;
    }

    public DataNeededListener getDataNeededListener() {

        return this.dataNeededListener;
    }

    public List<T> getGraphObjectsById(final Collection<String> ids) {

        final Set<String> idSet = new HashSet<String>();
        idSet.addAll(ids);

        final ArrayList<T> result = new ArrayList<T>(idSet.size());
        for (final String id : idSet) {
            final T graphObject = this.graphObjectsById.get(id);
            if (graphObject != null) {
                result.add(graphObject);
            }
        }

        return result;
    }

    public String getGroupByField() {

        return this.groupByField;
    }

    @Override
    public Object getItem(final int position) {

        final SectionAndItem<T> sectionAndItem = this.getSectionAndItem(position);
        return (sectionAndItem.getType() == SectionAndItem.Type.GRAPH_OBJECT) ? sectionAndItem.graphObject : null;
    }

    @Override
    public long getItemId(final int position) {

        // We assume IDs that can be converted to longs. If this is not the case for certain types of
        // GraphObjects, subclasses should override this to return, e.g., position, and override hasStableIds
        // to return false.
        final SectionAndItem<T> sectionAndItem = this.getSectionAndItem(position);
        if ((sectionAndItem != null) && (sectionAndItem.graphObject != null)) {
            final String id = this.getIdOfGraphObject(sectionAndItem.graphObject);
            if (id != null) {
                return Long.parseLong(id);
            }
        }
        return 0;
    }

    @Override
    public int getItemViewType(final int position) {

        final SectionAndItem<T> sectionAndItem = this.getSectionAndItem(position);
        switch (sectionAndItem.getType()) {
        case SECTION_HEADER:
            return HEADER_VIEW_TYPE;
        case GRAPH_OBJECT:
            return GRAPH_OBJECT_VIEW_TYPE;
        case ACTIVITY_CIRCLE:
            return ACTIVITY_CIRCLE_VIEW_TYPE;
        default:
            throw new FacebookException("Unexpected type of section and item.");
        }
    }

    public OnErrorListener getOnErrorListener() {

        return this.onErrorListener;
    }

    @Override
    public int getPositionForSection(int section) {

        if (this.displaySections) {
            section = Math.max(0, Math.min(section, this.sectionKeys.size() - 1));
            if (section < this.sectionKeys.size()) {
                return this.getPosition(this.sectionKeys.get(section), null);
            }
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(final int position) {

        final SectionAndItem<T> sectionAndItem = this.getSectionAndItem(position);
        if ((sectionAndItem != null) && (sectionAndItem.getType() != SectionAndItem.Type.ACTIVITY_CIRCLE)) {
            return Math.max(0,
                    Math.min(this.sectionKeys.indexOf(sectionAndItem.sectionKey), this.sectionKeys.size() - 1));
        }
        return 0;
    }

    @Override
    public Object[] getSections() {

        if (this.displaySections) {
            return this.sectionKeys.toArray();
        }
        return new Object[0];
    }

    public boolean getShowCheckbox() {

        return this.showCheckbox;
    }

    public boolean getShowPicture() {

        return this.showPicture;
    }

    public List<String> getSortFields() {

        return this.sortFields;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {

        final SectionAndItem<T> sectionAndItem = this.getSectionAndItem(position);

        switch (sectionAndItem.getType()) {
        case SECTION_HEADER:
            return this.getSectionHeaderView(sectionAndItem.sectionKey, convertView, parent);
        case GRAPH_OBJECT:
            return this.getGraphObjectView(sectionAndItem.graphObject, convertView, parent);
        case ACTIVITY_CIRCLE:
            this.dataNeededListener.onDataNeeded();
            return this.getActivityCircleView(convertView, parent);
        default:
            throw new FacebookException("Unexpected type of section and item.");
        }
    }

    @Override
    public int getViewTypeCount() {

        return 3;
    }

    @Override
    public boolean hasStableIds() {

        return true;
    }

    @Override
    public boolean isEmpty() {

        // We'll never populate sectionKeys unless we have at least one object.
        return this.sectionKeys.size() == 0;
    }

    @Override
    public boolean isEnabled(final int position) {

        final SectionAndItem<T> sectionAndItem = this.getSectionAndItem(position);
        return sectionAndItem.getType() == SectionAndItem.Type.GRAPH_OBJECT;
    }

    public void prioritizeViewRange(final int firstVisibleItem, final int lastVisibleItem, final int prefetchBuffer) {

        if ((lastVisibleItem < firstVisibleItem) || (this.sectionKeys.size() == 0)) {
            return;
        }

        // We want to prioritize requests for items which are visible but do not have pictures
        // loaded yet. We also want to pre-fetch pictures for items which are not yet visible
        // but are within a buffer on either side of the visible items, on the assumption that
        // they will be visible soon. For these latter items, we'll store the images in memory
        // in the hopes we can immediately populate their image view when needed.

        // Prioritize the requests in reverse order since each call to prioritizeRequest will just
        // move it to the front of the queue. And we want the earliest ones in the range to be at
        // the front of the queue, so all else being equal, the list will appear to populate from
        // the top down.
        for (int i = lastVisibleItem; i >= 0; i--) {
            final SectionAndItem<T> sectionAndItem = this.getSectionAndItem(i);
            if (sectionAndItem.graphObject != null) {
                final String id = this.getIdOfGraphObject(sectionAndItem.graphObject);
                final ImageRequest request = this.pendingRequests.get(id);
                if (request != null) {
                    ImageDownloader.prioritizeRequest(request);
                }
            }
        }

        // For items which are not visible, but within the buffer on either side, we want to
        // fetch those items and store them in a small in-memory cache of bitmaps.
        final int start = Math.max(0, firstVisibleItem - prefetchBuffer);
        final int end = Math.min(lastVisibleItem + prefetchBuffer, this.getCount() - 1);
        final ArrayList<T> graphObjectsToPrefetchPicturesFor = new ArrayList<T>();
        // Add the IDs before and after the visible range.
        for (int i = start; i < firstVisibleItem; ++i) {
            final SectionAndItem<T> sectionAndItem = this.getSectionAndItem(i);
            if (sectionAndItem.graphObject != null) {
                graphObjectsToPrefetchPicturesFor.add(sectionAndItem.graphObject);
            }
        }
        for (int i = lastVisibleItem + 1; i <= end; ++i) {
            final SectionAndItem<T> sectionAndItem = this.getSectionAndItem(i);
            if (sectionAndItem.graphObject != null) {
                graphObjectsToPrefetchPicturesFor.add(sectionAndItem.graphObject);
            }
        }
        for (final T graphObject : graphObjectsToPrefetchPicturesFor) {
            final URI uri = this.getPictureUriOfGraphObject(graphObject);
            final String id = this.getIdOfGraphObject(graphObject);

            // This URL already have been requested for pre-fetching, but we want to act in an LRU manner, so move
            // it to the end of the list regardless.
            final boolean alreadyPrefetching = this.prefetchedProfilePictureIds.remove(id);
            this.prefetchedProfilePictureIds.add(id);

            // If we've already requested it for pre-fetching, no need to do so again.
            if (!alreadyPrefetching) {
                this.downloadProfilePicture(id, uri, null);
            }
        }
    }

    public void rebuildAndNotify() {

        this.rebuildSections();
        this.notifyDataSetChanged();
    }

    public void setDataNeededListener(final DataNeededListener dataNeededListener) {

        this.dataNeededListener = dataNeededListener;
    }

    public void setGroupByField(final String groupByField) {

        this.groupByField = groupByField;
    }

    public void setOnErrorListener(final OnErrorListener onErrorListener) {

        this.onErrorListener = onErrorListener;
    }

    public void setShowCheckbox(final boolean showCheckbox) {

        this.showCheckbox = showCheckbox;
    }

    public void setShowPicture(final boolean showPicture) {

        this.showPicture = showPicture;
    }

    public void setSortFields(final List<String> sortFields) {

        this.sortFields = sortFields;
    }

    private void callOnErrorListener(Exception exception) {

        if (this.onErrorListener != null) {
            if (!(exception instanceof FacebookException)) {
                exception = new FacebookException(exception);
            }
            this.onErrorListener.onError(this, (FacebookException) exception);
        }
    }

    private void downloadProfilePicture(final String profileId, final URI pictureURI, final ImageView imageView) {

        if (pictureURI == null) {
            return;
        }

        // If we don't have an imageView, we are pre-fetching this image to store in-memory because we
        // think the user might scroll to its corresponding list row. If we do have an imageView, we
        // only want to queue a download if the view's tag isn't already set to the URL (which would mean
        // it's already got the correct picture).
        final boolean prefetching = imageView == null;
        if (prefetching || !pictureURI.equals(imageView.getTag())) {
            if (!prefetching) {
                // Setting the tag to the profile ID indicates that we're currently downloading the
                // picture for this profile; we'll set it to the actual picture URL when complete.
                imageView.setTag(profileId);
                imageView.setImageResource(this.getDefaultPicture());
            }

            final ImageRequest.Builder builder = new ImageRequest.Builder(this.context.getApplicationContext(),
                    pictureURI).setCallerTag(this).setCallback(new ImageRequest.Callback() {

                @Override
                public void onCompleted(final ImageResponse response) {

                    GraphObjectAdapter.this.processImageResponse(response, profileId, imageView);
                }
            });

            final ImageRequest newRequest = builder.build();
            this.pendingRequests.put(profileId, newRequest);

            ImageDownloader.downloadAsync(newRequest);
        }
    }

    private View getActivityCircleView(final View convertView, final ViewGroup parent) {

        View result = convertView;

        if (result == null) {
            result = this.inflater.inflate(R.layout.com_facebook_picker_activity_circle_row, null);
        }
        final ProgressBar activityCircle = (ProgressBar) result
                .findViewById(R.id.com_facebook_picker_row_activity_circle);
        activityCircle.setVisibility(View.VISIBLE);

        return result;
    }

    private void rebuildSections() {

        this.sectionKeys = new ArrayList<String>();
        this.graphObjectsBySection = new HashMap<String, ArrayList<T>>();
        this.graphObjectsById = new HashMap<String, T>();
        this.displaySections = false;

        if ((this.cursor == null) || (this.cursor.getCount() == 0)) {
            return;
        }

        int objectsAdded = 0;
        this.cursor.moveToFirst();
        do {
            final T graphObject = this.cursor.getGraphObject();

            if (!this.filterIncludesItem(graphObject)) {
                continue;
            }

            objectsAdded++;

            final String sectionKeyOfItem = this.getSectionKeyOfGraphObject(graphObject);
            if (!this.graphObjectsBySection.containsKey(sectionKeyOfItem)) {
                this.sectionKeys.add(sectionKeyOfItem);
                this.graphObjectsBySection.put(sectionKeyOfItem, new ArrayList<T>());
            }
            final List<T> section = this.graphObjectsBySection.get(sectionKeyOfItem);
            section.add(graphObject);

            this.graphObjectsById.put(this.getIdOfGraphObject(graphObject), graphObject);
        } while (this.cursor.moveToNext());

        if (this.sortFields != null) {
            final Collator collator = Collator.getInstance();
            for (final List<T> section : this.graphObjectsBySection.values()) {
                Collections.sort(section, new Comparator<GraphObject>() {

                    @Override
                    public int compare(final GraphObject a, final GraphObject graphObject) {

                        return compareGraphObjects(a, graphObject, GraphObjectAdapter.this.sortFields, collator);
                    }
                });
            }
        }

        Collections.sort(this.sectionKeys, Collator.getInstance());

        this.displaySections = (this.sectionKeys.size() > 1) && (objectsAdded > DISPLAY_SECTIONS_THRESHOLD);
    }

    private boolean shouldShowActivityCircleCell() {

        // We show the "more data" activity circle cell if we have a listener to request more data,
        // we are expecting more data, and we have some data already (i.e., not on a fresh query).
        return (this.cursor != null) && this.cursor.areMoreObjectsAvailable() && (this.dataNeededListener != null)
                && !this.isEmpty();
    }

    protected View createGraphObjectView(final T graphObject) {

        final View result = this.inflater.inflate(this.getGraphObjectRowLayoutId(graphObject), null);

        final ViewStub checkboxStub = (ViewStub) result.findViewById(R.id.com_facebook_picker_checkbox_stub);
        if (checkboxStub != null) {
            if (!this.getShowCheckbox()) {
                checkboxStub.setVisibility(View.GONE);
            } else {
                final CheckBox checkBox = (CheckBox) checkboxStub.inflate();
                this.updateCheckboxState(checkBox, false);
            }
        }

        final ViewStub profilePicStub = (ViewStub) result.findViewById(R.id.com_facebook_picker_profile_pic_stub);
        if (!this.getShowPicture()) {
            profilePicStub.setVisibility(View.GONE);
        } else {
            final ImageView imageView = (ImageView) profilePicStub.inflate();
            imageView.setVisibility(View.VISIBLE);
        }

        return result;
    }

    protected int getDefaultPicture() {

        return R.drawable.com_facebook_profile_default_icon;
    }

    protected int getGraphObjectRowLayoutId(final T graphObject) {

        return R.layout.com_facebook_picker_list_row;
    }

    protected View getGraphObjectView(final T graphObject, final View convertView, final ViewGroup parent) {

        View result = convertView;

        if (result == null) {
            result = this.createGraphObjectView(graphObject);
        }

        this.populateGraphObjectView(result, graphObject);
        return result;
    }

    protected URI getPictureUriOfGraphObject(final T graphObject) {

        String uri = null;
        final Object o = graphObject.getProperty(PICTURE);
        if (o instanceof String) {
            uri = (String) o;
        } else if (o instanceof JSONObject) {
            final ItemPicture itemPicture = GraphObject.Factory.create((JSONObject) o).cast(ItemPicture.class);
            final ItemPictureData data = itemPicture.getData();
            if (data != null) {
                uri = data.getUrl();
            }
        }

        if (uri != null) {
            try {
                return new URI(uri);
            } catch (final URISyntaxException e) {
            }
        }
        return null;
    }

    protected View getSectionHeaderView(final String sectionHeader, final View convertView, final ViewGroup parent) {

        TextView result = (TextView) convertView;

        if (result == null) {
            result = (TextView) this.inflater.inflate(R.layout.com_facebook_picker_list_section_header, null);
        }

        result.setText(sectionHeader);

        return result;
    }

    protected String getSectionKeyOfGraphObject(final T graphObject) {

        String result = null;

        if (this.groupByField != null) {
            result = (String) graphObject.getProperty(this.groupByField);
            if ((result != null) && (result.length() > 0)) {
                result = result.substring(0, 1).toUpperCase(Locale.getDefault());
            }
        }

        return (result != null) ? result : "";
    }

    protected CharSequence getSubTitleOfGraphObject(final T graphObject) {

        return null;
    }

    protected CharSequence getTitleOfGraphObject(final T graphObject) {

        return (String) graphObject.getProperty(NAME);
    }

    protected void populateGraphObjectView(final View view, final T graphObject) {

        final String id = this.getIdOfGraphObject(graphObject);
        view.setTag(id);

        final CharSequence title = this.getTitleOfGraphObject(graphObject);
        final TextView titleView = (TextView) view.findViewById(R.id.com_facebook_picker_title);
        if (titleView != null) {
            titleView.setText(title, TextView.BufferType.SPANNABLE);
        }

        final CharSequence subtitle = this.getSubTitleOfGraphObject(graphObject);
        final TextView subtitleView = (TextView) view.findViewById(R.id.picker_subtitle);
        if (subtitleView != null) {
            if (subtitle != null) {
                subtitleView.setText(subtitle, TextView.BufferType.SPANNABLE);
                subtitleView.setVisibility(View.VISIBLE);
            } else {
                subtitleView.setVisibility(View.GONE);
            }
        }

        if (this.getShowCheckbox()) {
            final CheckBox checkBox = (CheckBox) view.findViewById(R.id.com_facebook_picker_checkbox);
            this.updateCheckboxState(checkBox, this.isGraphObjectSelected(id));
        }

        if (this.getShowPicture()) {
            final URI pictureURI = this.getPictureUriOfGraphObject(graphObject);

            if (pictureURI != null) {
                final ImageView profilePic = (ImageView) view.findViewById(R.id.com_facebook_picker_image);

                // See if we have already pre-fetched this; if not, download it.
                if (this.prefetchedPictureCache.containsKey(id)) {
                    final ImageResponse response = this.prefetchedPictureCache.get(id);
                    profilePic.setImageBitmap(response.getBitmap());
                    profilePic.setTag(response.getRequest().getImageUri());
                } else {
                    this.downloadProfilePicture(id, pictureURI, profilePic);
                }
            }
        }
    }

    boolean filterIncludesItem(final T graphObject) {

        return (this.filter == null) || this.filter.includeItem(graphObject);
    }

    Filter<T> getFilter() {

        return this.filter;
    }

    /**
     * @throws FacebookException if the GraphObject doesn't have an ID.
     */
    String getIdOfGraphObject(final T graphObject) {

        if (graphObject.asMap().containsKey(ID)) {
            final Object obj = graphObject.getProperty(ID);
            if (obj instanceof String) {
                return (String) obj;
            }
        }
        throw new FacebookException("Received an object without an ID.");
    }

    String getPictureFieldSpecifier() {

        // How big is our image?
        final View view = this.createGraphObjectView(null);
        final ImageView picture = (ImageView) view.findViewById(R.id.com_facebook_picker_image);
        if (picture == null) {
            return null;
        }

        // Note: these dimensions are in pixels, not dips
        final ViewGroup.LayoutParams layoutParams = picture.getLayoutParams();
        return String.format(Locale.US, "picture.height(%d).width(%d)", Integer.valueOf(layoutParams.height),
                Integer.valueOf(layoutParams.width));
    }

    int getPosition(final String sectionKey, final T graphObject) {

        int position = 0;
        boolean found = false;

        // First find the section key and increment position one for each header we will render;
        // increment by the size of each section prior to the one we want.
        for (final String key : this.sectionKeys) {
            if (this.displaySections) {
                position++;
            }
            if (key.equals(sectionKey)) {
                found = true;
                break;
            }
            position += this.graphObjectsBySection.get(key).size();
        }

        if (!found) {
            return -1;
        } else if (graphObject == null) {
            // null represents the header for a section; we counted this header in position earlier,
            // so subtract it back out.
            return position - (this.displaySections ? 1 : 0);
        }

        // Now find index of this item within that section.
        for (final T t : this.graphObjectsBySection.get(sectionKey)) {
            if (GraphObject.Factory.hasSameId(t, graphObject)) {
                return position;
            }
            position++;
        }
        return -1;
    }

    SectionAndItem<T> getSectionAndItem(int position) {

        if (this.sectionKeys.size() == 0) {
            return null;
        }
        String sectionKey = null;
        T graphObject = null;

        if (!this.displaySections) {
            sectionKey = this.sectionKeys.get(0);
            final List<T> section = this.graphObjectsBySection.get(sectionKey);
            if ((position >= 0) && (position < section.size())) {
                graphObject = this.graphObjectsBySection.get(sectionKey).get(position);
            } else {
                // We are off the end; we must be adding an activity circle to indicate more data is coming.
                assert (this.dataNeededListener != null) && this.cursor.areMoreObjectsAvailable();
                // We return null for both to indicate this.
                return new SectionAndItem<T>(null, null);
            }
        } else {
            // Count through the sections; the "0" position in each section is the header. We decrement
            // position each time we skip forward a certain number of elements, including the header.
            for (final String key : this.sectionKeys) {
                // Decrement if we skip over the header
                if (position-- == 0) {
                    sectionKey = key;
                    break;
                }

                final List<T> section = this.graphObjectsBySection.get(key);
                if (position < section.size()) {
                    // The position is somewhere in this section. Get the corresponding graph object.
                    sectionKey = key;
                    graphObject = section.get(position);
                    break;
                }
                // Decrement by as many items as we skipped over
                position -= section.size();
            }
        }
        if (sectionKey != null) {
            // Note: graphObject will be null if this represents a section header.
            return new SectionAndItem<T>(sectionKey, graphObject);
        }
        throw new IndexOutOfBoundsException("position");
    }

    boolean isGraphObjectSelected(final String graphObjectId) {

        return false;
    }

    void processImageResponse(final ImageResponse response, final String graphObjectId, final ImageView imageView) {

        this.pendingRequests.remove(graphObjectId);
        if (response.getError() != null) {
            this.callOnErrorListener(response.getError());
        }

        if (imageView == null) {
            // This was a pre-fetch request.
            if (response.getBitmap() != null) {
                // Is the cache too big?
                if (this.prefetchedPictureCache.size() >= MAX_PREFETCHED_PICTURES) {
                    // Find the oldest one and remove it.
                    final String oldestId = this.prefetchedProfilePictureIds.remove(0);
                    this.prefetchedPictureCache.remove(oldestId);
                }
                this.prefetchedPictureCache.put(graphObjectId, response);
            }
        } else if (graphObjectId.equals(imageView.getTag())) {
            final Exception error = response.getError();
            final Bitmap bitmap = response.getBitmap();
            if ((error == null) && (bitmap != null)) {
                imageView.setImageBitmap(bitmap);
                imageView.setTag(response.getRequest().getImageUri());
            }
        }
    }

    void setFilter(final Filter<T> filter) {

        this.filter = filter;
    }

    void updateCheckboxState(final CheckBox checkBox, final boolean graphObjectSelected) {

        // Default is no-op
    }

    public interface DataNeededListener {

        void onDataNeeded();
    }

    public interface OnErrorListener {

        void onError(GraphObjectAdapter<?> adapter, FacebookException error);
    }

    public static class SectionAndItem<T extends GraphObject> {

        public String sectionKey;
        public T graphObject;

        public SectionAndItem(final String sectionKey, final T graphObject) {

            this.sectionKey = sectionKey;
            this.graphObject = graphObject;
        }

        public Type getType() {

            if (this.sectionKey == null) {
                return Type.ACTIVITY_CIRCLE;
            } else if (this.graphObject == null) {
                return Type.SECTION_HEADER;
            } else {
                return Type.GRAPH_OBJECT;
            }
        }

        public enum Type {
            GRAPH_OBJECT, SECTION_HEADER, ACTIVITY_CIRCLE
        }
    }

    // Graph object type to navigate the JSON that sometimes comes back instead of a URL string
    private interface ItemPicture extends GraphObject {

        ItemPictureData getData();
    }

    // Graph object type to navigate the JSON that sometimes comes back instead of a URL string
    private interface ItemPictureData extends GraphObject {

        String getUrl();
    }

    interface Filter<T> {

        boolean includeItem(T graphObject);
    }
}
