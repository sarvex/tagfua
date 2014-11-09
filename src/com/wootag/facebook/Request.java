/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.wootag.facebook;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wootag.facebook.internal.AttributionIdentifiers;
import com.wootag.facebook.internal.Logger;
import com.wootag.facebook.internal.ServerProtocol;
import com.wootag.facebook.internal.Utility;
import com.wootag.facebook.internal.Validate;
import com.wootag.facebook.model.GraphMultiResult;
import com.wootag.facebook.model.GraphObject;
import com.wootag.facebook.model.GraphObjectList;
import com.wootag.facebook.model.GraphPlace;
import com.wootag.facebook.model.GraphUser;
import com.wootag.facebook.model.OpenGraphAction;
import com.wootag.facebook.model.OpenGraphObject;

/**
 * A single request to be sent to the Facebook Platform through either the <a
 * href="https://developers.facebook.com/docs/reference/api/">Graph API</a> or <a
 * href="https://developers.facebook.com/docs/reference/rest/">REST API</a>. The Request class provides functionality
 * relating to serializing and deserializing requests and responses, making calls in batches (with a single round-trip
 * to the service) and making calls asynchronously. The particular service endpoint that a request targets is determined
 * by either a graph path (see the {@link #setGraphPath(String) setGraphPath} method) or a REST method name (see the
 * {@link #setRestMethod(String) setRestMethod} method); a single request may not target both. A Request can be executed
 * either anonymously or representing an authenticated user. In the former case, no Session needs to be specified, while
 * in the latter, a Session that is in an opened state must be provided. If requests are executed in a batch, a Facebook
 * application ID must be associated with the batch, either by supplying a Session for at least one of the requests in
 * the batch (the first one found in the batch will be used) or by calling the
 * {@link #setDefaultBatchApplicationId(String) setDefaultBatchApplicationId} method. After completion of a request, its
 * Session, if any, will be checked to determine if its Facebook access token needs to be extended; if so, a request to
 * extend it will be issued in the background.
 */
public class Request {

    private static final String REQUESTS2 = "requests";

    private static final String IMAGE = "image";

    /**
     * The maximum number of requests that can be submitted in a single batch. This limit is enforced on the service
     * side by the Facebook platform, not by the Request class.
     */
    public static final int MAXIMUM_BATCH_SIZE = 50;

    public static final String TAG = Request.class.getSimpleName();

    private static final String ME = "me";
    private static final String MY_FRIENDS = "me/friends";
    private static final String MY_PHOTOS = "me/photos";
    private static final String MY_VIDEOS = "me/videos";
    private static final String VIDEOS_SUFFIX = "/videos";
    private static final String SEARCH = "search";
    private static final String MY_FEED = "me/feed";
    private static final String MY_STAGING_RESOURCES = "me/staging_resources";
    private static final String MY_OBJECTS_FORMAT = "me/objects/%s";
    private static final String MY_ACTION_FORMAT = "me/%s";

    private static final String USER_AGENT_BASE = "FBAndroidSDK";
    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";

    // Parameter names/values
    private static final String PICTURE_PARAM = "picture";
    private static final String FORMAT_PARAM = "format";
    private static final String FORMAT_JSON = "json";
    private static final String SDK_PARAM = "sdk";
    private static final String SDK_ANDROID = "android";
    private static final String ACCESS_TOKEN_PARAM = "access_token";
    private static final String BATCH_ENTRY_NAME_PARAM = "name";
    private static final String BATCH_ENTRY_OMIT_RESPONSE_ON_SUCCESS_PARAM = "omit_response_on_success";
    private static final String BATCH_ENTRY_DEPENDS_ON_PARAM = "depends_on";
    private static final String BATCH_APP_ID_PARAM = "batch_app_id";
    private static final String BATCH_RELATIVE_URL_PARAM = "relative_url";
    private static final String BATCH_BODY_PARAM = "body";
    private static final String BATCH_METHOD_PARAM = "method";
    private static final String BATCH_PARAM = "batch";
    private static final String ATTACHMENT_FILENAME_PREFIX = "file";
    private static final String ATTACHED_FILES_PARAM = "attached_files";
    private static final String ISO_8601_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final String STAGING_PARAM = "file";
    private static final String OBJECT_PARAM = "object";

    private static final String MIME_BOUNDARY = "3i2ndDfv2rTHiSisAbouNdArYfORhtTPEefj3q2f";

    private static String defaultBatchApplicationId;

    private static Pattern versionPattern = Pattern.compile("^v\\d+\\.\\d+/.*");

    private Session session;
    private HttpMethod httpMethod;
    private String graphPath;
    private GraphObject graphObject;
    private String restMethod;
    private String batchEntryName;
    private String batchEntryDependsOn;
    private boolean batchEntryOmitResultOnSuccess = true;
    private Bundle parameters;
    private Callback callback;
    private String overriddenURL;
    private Object tag;
    private String version;

    private static volatile String userAgent;

    /**
     * Constructs a request without a session, graph path, or any other parameters.
     */
    public Request() {

        this(null, null, null, null, null);
    }

    /**
     * Constructs a request with a Session to retrieve a particular graph path. A Session need not be provided, in which
     * case the request is sent without an access token and thus is not executed in the context of any particular user.
     * Only certain graph requests can be expected to succeed in this case. If a Session is provided, it must be in an
     * opened state or the request will fail.
     *
     * @param session the Session to use, or null
     * @param graphPath the graph path to retrieve
     */
    public Request(final Session session, final String graphPath) {

        this(session, graphPath, null, null, null);
    }

    /**
     * Constructs a request with a specific Session, graph path, parameters, and HTTP method. A Session need not be
     * provided, in which case the request is sent without an access token and thus is not executed in the context of
     * any particular user. Only certain graph requests can be expected to succeed in this case. If a Session is
     * provided, it must be in an opened state or the request will fail. Depending on the httpMethod parameter, the
     * object at the graph path may be retrieved, created, or deleted.
     *
     * @param session the Session to use, or null
     * @param graphPath the graph path to retrieve, create, or delete
     * @param parameters additional parameters to pass along with the Graph API request; parameters must be Strings,
     *            Numbers, Bitmaps, Dates, or Byte arrays.
     * @param httpMethod the {@link HttpMethod} to use for the request, or null for default (HttpMethod.GET)
     */
    public Request(final Session session, final String graphPath, final Bundle parameters, final HttpMethod httpMethod) {

        this(session, graphPath, parameters, httpMethod, null);
    }

    /**
     * Constructs a request with a specific Session, graph path, parameters, and HTTP method. A Session need not be
     * provided, in which case the request is sent without an access token and thus is not executed in the context of
     * any particular user. Only certain graph requests can be expected to succeed in this case. If a Session is
     * provided, it must be in an opened state or the request will fail. Depending on the httpMethod parameter, the
     * object at the graph path may be retrieved, created, or deleted.
     *
     * @param session the Session to use, or null
     * @param graphPath the graph path to retrieve, create, or delete
     * @param parameters additional parameters to pass along with the Graph API request; parameters must be Strings,
     *            Numbers, Bitmaps, Dates, or Byte arrays.
     * @param httpMethod the {@link HttpMethod} to use for the request, or null for default (HttpMethod.GET)
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     */
    public Request(final Session session, final String graphPath, final Bundle parameters, final HttpMethod httpMethod,
            final Callback callback) {

        this(session, graphPath, parameters, httpMethod, callback, null);
    }

    /**
     * Constructs a request with a specific Session, graph path, parameters, and HTTP method. A Session need not be
     * provided, in which case the request is sent without an access token and thus is not executed in the context of
     * any particular user. Only certain graph requests can be expected to succeed in this case. If a Session is
     * provided, it must be in an opened state or the request will fail. Depending on the httpMethod parameter, the
     * object at the graph path may be retrieved, created, or deleted.
     *
     * @param session the Session to use, or null
     * @param graphPath the graph path to retrieve, create, or delete
     * @param parameters additional parameters to pass along with the Graph API request; parameters must be Strings,
     *            Numbers, Bitmaps, Dates, or Byte arrays.
     * @param httpMethod the {@link HttpMethod} to use for the request, or null for default (HttpMethod.GET)
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @param version the version of the Graph API
     */
    public Request(final Session session, final String graphPath, final Bundle parameters, final HttpMethod httpMethod,
            final Callback callback, final String version) {

        this.session = session;
        this.graphPath = graphPath;
        this.callback = callback;
        this.version = version;

        this.setHttpMethod(httpMethod);

        if (parameters != null) {
            this.parameters = new Bundle(parameters);
        } else {
            this.parameters = new Bundle();
        }

        if (this.version == null) {
            this.version = ServerProtocol.getAPIVersion();
        }
    }

    Request(final Session session, final URL overriddenURL) {

        this.session = session;
        this.overriddenURL = overriddenURL.toString();

        this.setHttpMethod(HttpMethod.GET);

        this.parameters = new Bundle();
    }

    /**
     * Executes a single request on the current thread and returns the response.
     * <p/>
     * This should only be used if you have transitioned off the UI thread.
     *
     * @param request the Request to execute
     * @return the Response object representing the results of the request
     * @throws FacebookException If there was an error in the protocol used to communicate with the service
     */
    public static Response executeAndWait(final Request request) {

        final List<Response> responses = executeBatchAndWait(request);

        if ((responses == null) || (responses.size() != 1)) {
            throw new FacebookException("invalid state: expected a single response");
        }

        return responses.get(0);
    }

    /**
     * Executes requests as a single batch on the current thread and returns the responses.
     * <p/>
     * This should only be used if you have transitioned off the UI thread.
     *
     * @param requests the Requests to execute
     * @return a list of Response objects representing the results of the requests; responses are returned in the same
     *         order as the requests were specified.
     * @throws FacebookException If there was an error in the protocol used to communicate with the service
     */
    public static List<Response> executeBatchAndWait(final Collection<Request> requests) {

        return executeBatchAndWait(new RequestBatch(requests));
    }

    /**
     * Executes requests on the current thread as a single batch and returns the responses.
     * <p/>
     * This should only be used if you have transitioned off the UI thread.
     *
     * @param requests the Requests to execute
     * @return a list of Response objects representing the results of the requests; responses are returned in the same
     *         order as the requests were specified.
     * @throws NullPointerException In case of a null request
     * @throws FacebookException If there was an error in the protocol used to communicate with the service
     */
    public static List<Response> executeBatchAndWait(final Request... requests) {

        Validate.notNull(requests, REQUESTS2);

        return executeBatchAndWait(Arrays.asList(requests));
    }

    /**
     * Executes requests on the current thread as a single batch and returns the responses.
     * <p/>
     * This should only be used if you have transitioned off the UI thread.
     *
     * @param requests the batch of Requests to execute
     * @return a list of Response objects representing the results of the requests; responses are returned in the same
     *         order as the requests were specified.
     * @throws FacebookException If there was an error in the protocol used to communicate with the service
     * @throws IllegalArgumentException if the passed in RequestBatch is empty
     * @throws NullPointerException if the passed in RequestBatch or any of its contents are null
     */
    public static List<Response> executeBatchAndWait(final RequestBatch requests) {

        Validate.notEmptyAndContainsNoNulls(requests, REQUESTS2);

        HttpURLConnection connection = null;
        try {
            connection = toHttpConnection(requests);
        } catch (final Exception ex) {
            final List<Response> responses = Response.constructErrorResponses(requests.getRequests(), null,
                    new FacebookException(ex));
            runCallbacks(requests, responses);
            return responses;
        }

        return executeConnectionAndWait(connection, requests);
    }

    /**
     * Executes requests as a single batch asynchronously. This function will return immediately, and the requests will
     * be processed on a separate thread. In order to process results of a request, or determine whether a request
     * succeeded or failed, a callback must be specified (see the {@link #setCallback(Callback) setCallback} method).
     * <p/>
     * This should only be called from the UI thread.
     *
     * @param requests the Requests to execute
     * @return a RequestAsyncTask that is executing the request
     * @throws IllegalArgumentException if the passed in collection is empty
     * @throws NullPointerException if the passed in collection or any of its contents are null
     */
    public static RequestAsyncTask executeBatchAsync(final Collection<Request> requests) {

        return executeBatchAsync(new RequestBatch(requests));
    }

    /**
     * Executes requests as a single batch asynchronously. This function will return immediately, and the requests will
     * be processed on a separate thread. In order to process results of a request, or determine whether a request
     * succeeded or failed, a callback must be specified (see the {@link #setCallback(Callback) setCallback} method).
     * <p/>
     * This should only be called from the UI thread.
     *
     * @param requests the Requests to execute
     * @return a RequestAsyncTask that is executing the request
     * @throws NullPointerException If a null request is passed in
     */
    public static RequestAsyncTask executeBatchAsync(final Request... requests) {

        Validate.notNull(requests, REQUESTS2);

        return executeBatchAsync(Arrays.asList(requests));
    }

    /**
     * Executes requests as a single batch asynchronously. This function will return immediately, and the requests will
     * be processed on a separate thread. In order to process results of a request, or determine whether a request
     * succeeded or failed, a callback must be specified (see the {@link #setCallback(Callback) setCallback} method).
     * <p/>
     * This should only be called from the UI thread.
     *
     * @param requests the RequestBatch to execute
     * @return a RequestAsyncTask that is executing the request
     * @throws IllegalArgumentException if the passed in RequestBatch is empty
     * @throws NullPointerException if the passed in RequestBatch or any of its contents are null
     */
    public static RequestAsyncTask executeBatchAsync(final RequestBatch requests) {

        Validate.notEmptyAndContainsNoNulls(requests, REQUESTS2);

        final RequestAsyncTask asyncTask = new RequestAsyncTask(requests);
        asyncTask.executeOnSettingsExecutor();
        return asyncTask;
    }

    /**
     * Executes requests that have already been serialized into an HttpURLConnection. No validation is done that the
     * contents of the connection actually reflect the serialized requests, so it is the caller's responsibility to
     * ensure that it will correctly generate the desired responses.
     * <p/>
     * This should only be called if you have transitioned off the UI thread.
     *
     * @param connection the HttpURLConnection that the requests were serialized into
     * @param requests the requests represented by the HttpURLConnection
     * @return a list of Responses corresponding to the requests
     * @throws FacebookException If there was an error in the protocol used to communicate with the service
     */
    public static List<Response> executeConnectionAndWait(final HttpURLConnection connection,
            final Collection<Request> requests) {

        return executeConnectionAndWait(connection, new RequestBatch(requests));
    }

    /**
     * Executes requests that have already been serialized into an HttpURLConnection. No validation is done that the
     * contents of the connection actually reflect the serialized requests, so it is the caller's responsibility to
     * ensure that it will correctly generate the desired responses.
     * <p/>
     * This should only be called if you have transitioned off the UI thread.
     *
     * @param connection the HttpURLConnection that the requests were serialized into
     * @param requests the RequestBatch represented by the HttpURLConnection
     * @return a list of Responses corresponding to the requests
     * @throws FacebookException If there was an error in the protocol used to communicate with the service
     */
    public static List<Response> executeConnectionAndWait(final HttpURLConnection connection,
            final RequestBatch requests) {

        final List<Response> responses = Response.fromHttpConnection(connection, requests);

        Utility.disconnectQuietly(connection);

        final int numRequests = requests.size();
        if (numRequests != responses.size()) {
            throw new FacebookException(String.format("Received %d responses while expecting %d",
                    Integer.valueOf(responses.size()), Integer.valueOf(numRequests)));
        }

        runCallbacks(requests, responses);

        // See if any of these sessions needs its token to be extended. We do this after issuing the request so as to
        // reduce network contention.
        final HashSet<Session> sessions = new HashSet<Session>();
        for (final Request request : requests) {
            if (request.session != null) {
                sessions.add(request.session);
            }
        }
        for (final Session session : sessions) {
            session.extendAccessTokenIfNeeded();
        }

        return responses;
    }

    /**
     * Asynchronously executes requests that have already been serialized into an HttpURLConnection. No validation is
     * done that the contents of the connection actually reflect the serialized requests, so it is the caller's
     * responsibility to ensure that it will correctly generate the desired responses. This function will return
     * immediately, and the requests will be processed on a separate thread. In order to process results of a request,
     * or determine whether a request succeeded or failed, a callback must be specified (see the
     * {@link #setCallback(Callback) setCallback} method)
     * <p/>
     * This should only be called from the UI thread.
     *
     * @param callbackHandler a Handler that will be used to post calls to the callback for each request; if null, a
     *            Handler will be instantiated on the calling thread
     * @param connection the HttpURLConnection that the requests were serialized into
     * @param requests the requests represented by the HttpURLConnection
     * @return a RequestAsyncTask that is executing the request
     */
    public static RequestAsyncTask executeConnectionAsync(final Handler callbackHandler,
            final HttpURLConnection connection, final RequestBatch requests) {

        Validate.notNull(connection, "connection");

        final RequestAsyncTask asyncTask = new RequestAsyncTask(connection, requests);
        requests.setCallbackHandler(callbackHandler);
        asyncTask.executeOnSettingsExecutor();
        return asyncTask;
    }

    /**
     * Asynchronously executes requests that have already been serialized into an HttpURLConnection. No validation is
     * done that the contents of the connection actually reflect the serialized requests, so it is the caller's
     * responsibility to ensure that it will correctly generate the desired responses. This function will return
     * immediately, and the requests will be processed on a separate thread. In order to process results of a request,
     * or determine whether a request succeeded or failed, a callback must be specified (see the
     * {@link #setCallback(Callback) setCallback} method).
     * <p/>
     * This should only be called from the UI thread.
     *
     * @param connection the HttpURLConnection that the requests were serialized into
     * @param requests the requests represented by the HttpURLConnection
     * @return a RequestAsyncTask that is executing the request
     */
    public static RequestAsyncTask executeConnectionAsync(final HttpURLConnection connection,
            final RequestBatch requests) {

        return executeConnectionAsync(null, connection, requests);
    }

    /**
     * Starts a new Request configured to retrieve a particular graph path.
     * <p/>
     * This should only be called from the UI thread. This method is deprecated. Prefer to call
     * Request.newGraphPathRequest(...).executeAsync();
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param graphPath the graph path to retrieve
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a RequestAsyncTask that is executing the request
     */
    @Deprecated
    public static RequestAsyncTask executeGraphPathRequestAsync(final Session session, final String graphPath,
            final Callback callback) {

        return newGraphPathRequest(session, graphPath, callback).executeAsync();
    }

    /**
     * Starts a new Request configured to retrieve a user's own profile.
     * <p/>
     * This should only be called from the UI thread. This method is deprecated. Prefer to call
     * Request.newMeRequest(...).executeAsync();
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a RequestAsyncTask that is executing the request
     */
    @Deprecated
    public static RequestAsyncTask executeMeRequestAsync(final Session session, final GraphUserCallback callback) {

        return newMeRequest(session, callback).executeAsync();
    }

    /**
     * Starts a new Request configured to retrieve a user's friend list.
     * <p/>
     * This should only be called from the UI thread. This method is deprecated. Prefer to call
     * Request.newMyFriendsRequest(...).executeAsync();
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a RequestAsyncTask that is executing the request
     */
    @Deprecated
    public static RequestAsyncTask executeMyFriendsRequestAsync(final Session session,
            final GraphUserListCallback callback) {

        return newMyFriendsRequest(session, callback).executeAsync();
    }

    /**
     * Starts a new Request that is configured to perform a search for places near a specified location via the Graph
     * API.
     * <p/>
     * This should only be called from the UI thread. This method is deprecated. Prefer to call
     * Request.newPlacesSearchRequest(...).executeAsync();
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param location the location around which to search; only the latitude and longitude components of the location
     *            are meaningful
     * @param radiusInMeters the radius around the location to search, specified in meters
     * @param resultsLimit the maximum number of results to return
     * @param searchText optional text to search for as part of the name or type of an object
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a RequestAsyncTask that is executing the request
     * @throws FacebookException If neither location nor searchText is specified
     */
    @Deprecated
    public static RequestAsyncTask executePlacesSearchRequestAsync(final Session session, final Location location,
            final int radiusInMeters, final int resultsLimit, final String searchText,
            final GraphPlaceListCallback callback) {

        return newPlacesSearchRequest(session, location, radiusInMeters, resultsLimit, searchText, callback)
                .executeAsync();
    }

    /**
     * Starts a new Request configured to make a call to the Facebook REST API.
     * <p/>
     * This should only be called from the UI thread. This method is deprecated. Prefer to call
     * Request.newRestRequest(...).executeAsync();
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param restMethod the method in the Facebook REST API to execute
     * @param parameters additional parameters to pass along with the Graph API request; parameters must be Strings,
     *            Numbers, Bitmaps, Dates, or Byte arrays.
     * @param httpMethod the HTTP method to use for the request; must be one of GET, POST, or DELETE
     * @return a RequestAsyncTask that is executing the request
     */
    @Deprecated
    public static RequestAsyncTask executeRestRequestAsync(final Session session, final String restMethod,
            final Bundle parameters, final HttpMethod httpMethod) {

        return newRestRequest(session, restMethod, parameters, httpMethod).executeAsync();
    }

    /**
     * Starts a new Request configured to post a status update to a user's feed.
     * <p/>
     * This should only be called from the UI thread. This method is deprecated. Prefer to call
     * Request.newStatusUpdateRequest(...).executeAsync();
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param message the text of the status update
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a RequestAsyncTask that is executing the request
     */
    @Deprecated
    public static RequestAsyncTask executeStatusUpdateRequestAsync(final Session session, final String message,
            final Callback callback) {

        return newStatusUpdateRequest(session, message, callback).executeAsync();
    }

    /**
     * Starts a new Request configured to upload a photo to the user's default photo album.
     * <p/>
     * This should only be called from the UI thread. This method is deprecated. Prefer to call
     * Request.newUploadPhotoRequest(...).executeAsync();
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param image the image to upload
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a RequestAsyncTask that is executing the request
     */
    @Deprecated
    public static RequestAsyncTask executeUploadPhotoRequestAsync(final Session session, final Bitmap image,
            final Callback callback) {

        return newUploadPhotoRequest(session, image, callback).executeAsync();
    }

    /**
     * Starts a new Request configured to upload a photo to the user's default photo album. The photo will be read from
     * the specified stream.
     * <p/>
     * This should only be called from the UI thread. This method is deprecated. Prefer to call
     * Request.newUploadPhotoRequest(...).executeAsync();
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param file the file containing the photo to upload
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a RequestAsyncTask that is executing the request
     */
    @Deprecated
    public static RequestAsyncTask executeUploadPhotoRequestAsync(final Session session, final File file,
            final Callback callback) throws FileNotFoundException {

        return newUploadPhotoRequest(session, file, callback).executeAsync();
    }

    /**
     * Gets the default Facebook application ID that will be used to submit batched requests if none of those requests
     * specifies a Session. Batched requests require an application ID, so either at least one request in a batch must
     * specify a Session or the application ID must be specified explicitly.
     *
     * @return the Facebook application ID to use for batched requests if none can be determined
     */
    public static final String getDefaultBatchApplicationId() {

        return Request.defaultBatchApplicationId;
    }

    /**
     * Creates a new Request configured to retrieve an App User ID for the app's Facebook user. Callers will send this
     * ID back to their own servers, collect up a set to create a Facebook Custom Audience with, and then use the
     * resultant Custom Audience to target ads.
     * <p/>
     * The GraphObject in the response will include an "custom_audience_third_party_id" property, with the value being
     * the ID retrieved. This ID is an encrypted encoding of the Facebook user's ID and the invoking Facebook app ID.
     * Multiple calls with the same user will return different IDs, thus these IDs cannot be used to correlate behavior
     * across devices or applications, and are only meaningful when sent back to Facebook for creating Custom Audiences.
     * <p/>
     * The ID retrieved represents the Facebook user identified in the following way: if the specified session (or
     * activeSession if the specified session is `null`) is open, the ID will represent the user associated with the
     * activeSession; otherwise the ID will represent the user logged into the native Facebook app on the device. A
     * `null` ID will be provided into the callback if a) there is no native Facebook app, b) no one is logged into it,
     * or c) the app has previously called {@link Settings#setLimitEventAndDataUsage(android.content.Context, boolean)}
     * with `true` for this user.
     *
     * @param session the Session to issue the Request on, or null; if non-null, the session must be in an opened state.
     *            If there is no logged-in Facebook user, null is the expected choice.
     * @param context the Application context from which the app ID will be pulled, and from which the 'attribution ID'
     *            for the Facebook user is determined. If there has been no app ID set, an exception will be thrown.
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions. The GraphObject in the Response will contain a "custom_audience_third_party_id" property
     *            that represents the user as described above.
     * @return a Request that is ready to execute
     */
    public static Request newCustomAudienceThirdPartyIdRequest(final Session session, final Context context,
            final Callback callback) {

        return newCustomAudienceThirdPartyIdRequest(session, context, null, callback);
    }

    /**
     * Creates a new Request configured to retrieve an App User ID for the app's Facebook user. Callers will send this
     * ID back to their own servers, collect up a set to create a Facebook Custom Audience with, and then use the
     * resultant Custom Audience to target ads.
     * <p/>
     * The GraphObject in the response will include an "custom_audience_third_party_id" property, with the value being
     * the ID retrieved. This ID is an encrypted encoding of the Facebook user's ID and the invoking Facebook app ID.
     * Multiple calls with the same user will return different IDs, thus these IDs cannot be used to correlate behavior
     * across devices or applications, and are only meaningful when sent back to Facebook for creating Custom Audiences.
     * <p/>
     * The ID retrieved represents the Facebook user identified in the following way: if the specified session (or
     * activeSession if the specified session is `null`) is open, the ID will represent the user associated with the
     * activeSession; otherwise the ID will represent the user logged into the native Facebook app on the device. A
     * `null` ID will be provided into the callback if a) there is no native Facebook app, b) no one is logged into it,
     * or c) the app has previously called {@link Settings#setLimitEventAndDataUsage(android.content.Context, boolean)}
     * ;} with `true` for this user.
     *
     * @param session the Session to issue the Request on, or null; if non-null, the session must be in an opened state.
     *            If there is no logged-in Facebook user, null is the expected choice.
     * @param context the Application context from which the app ID will be pulled, and from which the 'attribution ID'
     *            for the Facebook user is determined. If there has been no app ID set, an exception will be thrown.
     * @param applicationId explicitly specified Facebook App ID. If null, and there's a valid session, then the
     *            application ID from the session will be used, otherwise the application ID from metadata will be used.
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions. The GraphObject in the Response will contain a "custom_audience_third_party_id" property
     *            that represents the user as described above.
     * @return a Request that is ready to execute
     */
    public static Request newCustomAudienceThirdPartyIdRequest(Session session, final Context context,
            String applicationId, final Callback callback) {

        // if provided session or activeSession is opened, use it.
        if (session == null) {
            session = Session.getActiveSession();
        }

        if ((session != null) && !session.isOpened()) {
            session = null;
        }

        if (applicationId == null) {
            if (session != null) {
                applicationId = session.getApplicationId();
            } else {
                applicationId = Utility.getMetadataApplicationId(context);
            }
        }

        if (applicationId == null) {
            throw new FacebookException("Facebook App ID cannot be determined");
        }

        final String endpoint = applicationId + "/custom_audience_third_party_id";
        final AttributionIdentifiers attributionIdentifiers = AttributionIdentifiers.getAttributionIdentifiers(context);
        final Bundle parameters = new Bundle();

        if (session == null) {
            // Only use the attributionID if we don't have an open session. If we do have an open session, then
            // the user token will be used to identify the user, and is more reliable than the attributionID.
            final String udid = attributionIdentifiers.getAttributionId() != null ? attributionIdentifiers
                    .getAttributionId() : attributionIdentifiers.getAndroidAdvertiserId();
            if (attributionIdentifiers.getAttributionId() != null) {
                parameters.putString("udid", udid);
            }
        }

        // Server will choose to not provide the App User ID in the event that event usage has been limited for
        // this user for this app.
        if (Settings.getLimitEventAndDataUsage(context) || attributionIdentifiers.isTrackingLimited()) {
            parameters.putString("limit_event_usage", "1");
        }

        return new Request(session, endpoint, parameters, HttpMethod.GET, callback);
    }

    /**
     * Creates a new Request configured to delete a resource through the Graph API.
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param id the id of the object to delete
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a Request that is ready to execute
     */
    public static Request newDeleteObjectRequest(final Session session, final String id, final Callback callback) {

        return new Request(session, id, null, HttpMethod.DELETE, callback);
    }

    /**
     * Creates a new Request configured to retrieve a particular graph path.
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param graphPath the graph path to retrieve
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a Request that is ready to execute
     */
    public static Request newGraphPathRequest(final Session session, final String graphPath, final Callback callback) {

        return new Request(session, graphPath, null, null, callback);
    }

    /**
     * Creates a new Request configured to retrieve a user's own profile.
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a Request that is ready to execute
     */
    public static Request newMeRequest(final Session session, final GraphUserCallback callback) {

        final Callback wrapper = new Callback() {

            @Override
            public void onCompleted(final Response response) {

                if (callback != null) {
                    callback.onCompleted(response.getGraphObjectAs(GraphUser.class), response);
                }
            }
        };
        return new Request(session, ME, null, null, wrapper);
    }

    /**
     * Creates a new Request configured to retrieve a user's friend list.
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a Request that is ready to execute
     */
    public static Request newMyFriendsRequest(final Session session, final GraphUserListCallback callback) {

        final Callback wrapper = new Callback() {

            @Override
            public void onCompleted(final Response response) {

                if (callback != null) {
                    callback.onCompleted(typedListFromResponse(response, GraphUser.class), response);
                }
            }
        };
        return new Request(session, MY_FRIENDS, null, null, wrapper);
    }

    /**
     * Creates a new Request that is configured to perform a search for places near a specified location via the Graph
     * API. At least one of location or searchText must be specified.
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param location the location around which to search; only the latitude and longitude components of the location
     *            are meaningful
     * @param radiusInMeters the radius around the location to search, specified in meters; this is ignored if no
     *            location is specified
     * @param resultsLimit the maximum number of results to return
     * @param searchText optional text to search for as part of the name or type of an object
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a Request that is ready to execute
     * @throws FacebookException If neither location nor searchText is specified
     */
    public static Request newPlacesSearchRequest(final Session session, final Location location,
            final int radiusInMeters, final int resultsLimit, final String searchText,
            final GraphPlaceListCallback callback) {

        if ((location == null) && Utility.isNullOrEmpty(searchText)) {
            throw new FacebookException("Either location or searchText must be specified.");
        }

        final Bundle parameters = new Bundle(5);
        parameters.putString("type", "place");
        parameters.putInt("limit", resultsLimit);
        if (location != null) {
            parameters.putString(
                    "center",
                    String.format(Locale.US, "%f,%f", Double.valueOf(location.getLatitude()),
                            Double.valueOf(location.getLongitude())));
            parameters.putInt("distance", radiusInMeters);
        }
        if (!Utility.isNullOrEmpty(searchText)) {
            parameters.putString("q", searchText);
        }

        final Callback wrapper = new Callback() {

            @Override
            public void onCompleted(final Response response) {

                if (callback != null) {
                    callback.onCompleted(typedListFromResponse(response, GraphPlace.class), response);
                }
            }
        };

        return new Request(session, SEARCH, parameters, HttpMethod.GET, wrapper);
    }

    /**
     * Creates a new Request configured to publish an Open Graph action.
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param openGraphAction the Open Graph object to create; must not be null, and must have a non-empty 'type'
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a Request that is ready to execute
     */
    public static Request newPostOpenGraphActionRequest(final Session session, final OpenGraphAction openGraphAction,
            final Callback callback) {

        if (openGraphAction == null) {
            throw new FacebookException("openGraphAction cannot be null");
        }
        if (Utility.isNullOrEmpty(openGraphAction.getType())) {
            throw new FacebookException("openGraphAction must have non-null 'type' property");
        }

        final String path = String.format(MY_ACTION_FORMAT, openGraphAction.getType());
        return newPostRequest(session, path, openGraphAction, callback);
    }

    /**
     * Creates a new Request configured to create a user owned Open Graph object.
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param openGraphObject the Open Graph object to create; must not be null, and must have a non-empty type and
     *            title
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a Request that is ready to execute
     */
    public static Request newPostOpenGraphObjectRequest(final Session session, final OpenGraphObject openGraphObject,
            final Callback callback) {

        if (openGraphObject == null) {
            throw new FacebookException("openGraphObject cannot be null");
        }
        if (Utility.isNullOrEmpty(openGraphObject.getType())) {
            throw new FacebookException("openGraphObject must have non-null 'type' property");
        }
        if (Utility.isNullOrEmpty(openGraphObject.getTitle())) {
            throw new FacebookException("openGraphObject must have non-null 'title' property");
        }

        final String path = String.format(MY_OBJECTS_FORMAT, openGraphObject.getType());
        final Bundle bundle = new Bundle();
        bundle.putString(OBJECT_PARAM, openGraphObject.getInnerJSONObject().toString());
        return new Request(session, path, bundle, HttpMethod.POST, callback);
    }

    /**
     * Creates a new Request configured to create a user owned Open Graph object.
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param type the fully-specified Open Graph object type (e.g., my_app_namespace:my_object_name); must not be null
     * @param title the title of the Open Graph object; must not be null
     * @param imageUrl the link to an image to be associated with the Open Graph object; may be null
     * @param url the url to be associated with the Open Graph object; may be null
     * @param description the description to be associated with the object; may be null
     * @param objectProperties any additional type-specific properties for the Open Graph object; may be null
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions; may be null
     * @return a Request that is ready to execute
     */
    public static Request newPostOpenGraphObjectRequest(final Session session, final String type, final String title,
            final String imageUrl, final String url, final String description, final GraphObject objectProperties,
            final Callback callback) {

        final OpenGraphObject openGraphObject = OpenGraphObject.Factory.createForPost(OpenGraphObject.class, type,
                title, imageUrl, url, description);
        if (objectProperties != null) {
            openGraphObject.setData(objectProperties);
        }

        return newPostOpenGraphObjectRequest(session, openGraphObject, callback);
    }

    /**
     * Creates a new Request configured to post a GraphObject to a particular graph path, to either create or update the
     * object at that path.
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param graphPath the graph path to retrieve, create, or delete
     * @param graphObject the GraphObject to create or update
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a Request that is ready to execute
     */
    public static Request newPostRequest(final Session session, final String graphPath, final GraphObject graphObject,
            final Callback callback) {

        final Request request = new Request(session, graphPath, null, HttpMethod.POST, callback);
        request.setGraphObject(graphObject);
        return request;
    }

    /**
     * Creates a new Request configured to make a call to the Facebook REST API.
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param restMethod the method in the Facebook REST API to execute
     * @param parameters additional parameters to pass along with the Graph API request; parameters must be Strings,
     *            Numbers, Bitmaps, Dates, or Byte arrays.
     * @param httpMethod the HTTP method to use for the request; must be one of GET, POST, or DELETE
     * @return a Request that is ready to execute
     */
    public static Request newRestRequest(final Session session, final String restMethod, final Bundle parameters,
            final HttpMethod httpMethod) {

        final Request request = new Request(session, null, parameters, httpMethod);
        request.setRestMethod(restMethod);
        return request;
    }

    /**
     * Creates a new Request configured to post a status update to a user's feed.
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param message the text of the status update
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a Request that is ready to execute
     */
    public static Request newStatusUpdateRequest(final Session session, final String message, final Callback callback) {

        return newStatusUpdateRequest(session, message, (String) null, null, callback);
    }

    /**
     * Creates a new Request configured to post a status update to a user's feed.
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param message the text of the status update
     * @param place an optional place to associate with the post
     * @param tags an optional list of users to tag in the post
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a Request that is ready to execute
     */
    public static Request newStatusUpdateRequest(final Session session, final String message, final GraphPlace place,
            final List<GraphUser> tags, final Callback callback) {

        List<String> tagIds = null;
        if (tags != null) {
            tagIds = new ArrayList<String>(tags.size());
            for (final GraphUser tag : tags) {
                tagIds.add(tag.getId());
            }
        }
        final String placeId = place == null ? null : place.getId();
        return newStatusUpdateRequest(session, message, placeId, tagIds, callback);
    }

    /**
     * Creates a new Request configured to update a user owned Open Graph object.
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param openGraphObject the Open Graph object to update, which must have a valid 'id' property
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a Request that is ready to execute
     */
    public static Request newUpdateOpenGraphObjectRequest(final Session session, final OpenGraphObject openGraphObject,
            final Callback callback) {

        if (openGraphObject == null) {
            throw new FacebookException("openGraphObject cannot be null");
        }

        final String path = openGraphObject.getId();
        if (path == null) {
            throw new FacebookException("openGraphObject must have an id");
        }

        final Bundle bundle = new Bundle();
        bundle.putString(OBJECT_PARAM, openGraphObject.getInnerJSONObject().toString());
        return new Request(session, path, bundle, HttpMethod.POST, callback);
    }

    /**
     * Creates a new Request configured to update a user owned Open Graph object.
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param id the id of the Open Graph object
     * @param title the title of the Open Graph object
     * @param imageUrl the link to an image to be associated with the Open Graph object
     * @param url the url to be associated with the Open Graph object
     * @param description the description to be associated with the object
     * @param objectProperties any additional type-specific properties for the Open Graph object
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a Request that is ready to execute
     */
    public static Request newUpdateOpenGraphObjectRequest(final Session session, final String id, final String title,
            final String imageUrl, final String url, final String description, final GraphObject objectProperties,
            final Callback callback) {

        final OpenGraphObject openGraphObject = OpenGraphObject.Factory.createForPost(OpenGraphObject.class, null,
                title, imageUrl, url, description);
        openGraphObject.setId(id);
        openGraphObject.setData(objectProperties);

        return newUpdateOpenGraphObjectRequest(session, openGraphObject, callback);
    }

    /**
     * Creates a new Request configured to upload a photo to the user's default photo album.
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param image the image to upload
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a Request that is ready to execute
     */
    public static Request newUploadPhotoRequest(final Session session, final Bitmap image, final Callback callback) {

        final Bundle parameters = new Bundle(1);
        parameters.putParcelable(PICTURE_PARAM, image);

        return new Request(session, MY_PHOTOS, parameters, HttpMethod.POST, callback);
    }

    /**
     * Creates a new Request configured to upload a photo to the user's default photo album. The photo will be read from
     * the specified stream.
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param file the file containing the photo to upload
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a Request that is ready to execute
     */
    public static Request newUploadPhotoRequest(final Session session, final File file, final Callback callback)
            throws FileNotFoundException {

        final ParcelFileDescriptor descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        final Bundle parameters = new Bundle(1);
        parameters.putParcelable(PICTURE_PARAM, descriptor);

        return new Request(session, MY_PHOTOS, parameters, HttpMethod.POST, callback);
    }

    /**
     * Creates a new Request configured to upload an image to create a staging resource. Staging resources allow you to
     * post binary data such as images, in preparation for a post of an Open Graph object or action which references the
     * image. The URI returned when uploading a staging resource may be passed as the image property for an Open Graph
     * object or action.
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param image the image to upload
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a Request that is ready to execute
     */
    public static Request newUploadStagingResourceWithImageRequest(final Session session, final Bitmap image,
            final Callback callback) {

        final Bundle parameters = new Bundle(1);
        parameters.putParcelable(STAGING_PARAM, image);

        return new Request(session, MY_STAGING_RESOURCES, parameters, HttpMethod.POST, callback);
    }

    /**
     * Creates a new Request configured to upload an image to create a staging resource. Staging resources allow you to
     * post binary data such as images, in preparation for a post of an Open Graph object or action which references the
     * image. The URI returned when uploading a staging resource may be passed as the image property for an Open Graph
     * object or action.
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param file the file containing the image to upload
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a Request that is ready to execute
     */
    public static Request newUploadStagingResourceWithImageRequest(final Session session, final File file,
            final Callback callback) throws FileNotFoundException {

        final ParcelFileDescriptor descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        final ParcelFileDescriptorWithMimeType descriptorWithMimeType = new ParcelFileDescriptorWithMimeType(
                descriptor, "image/png");
        final Bundle parameters = new Bundle(1);
        parameters.putParcelable(STAGING_PARAM, descriptorWithMimeType);

        return new Request(session, MY_STAGING_RESOURCES, parameters, HttpMethod.POST, callback);
    }

    /**
     * Creates a new Request configured to upload a photo to the user's default photo album. The photo will be read from
     * the specified file descriptor.
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param file the file to upload
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a Request that is ready to execute
     */
    public static Request newUploadVideoRequest(final Session session, final File file, final Callback callback)
            throws FileNotFoundException {

        final ParcelFileDescriptor descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        final Bundle parameters = new Bundle(1);
        parameters.putParcelable(file.getName(), descriptor);

        return new Request(session, MY_VIDEOS, parameters, HttpMethod.POST, callback);
    }

    /**
     * Sets the default application ID that will be used to submit batched requests if none of those requests specifies
     * a Session. Batched requests require an application ID, so either at least one request in a batch must specify a
     * Session or the application ID must be specified explicitly.
     *
     * @param applicationId the Facebook application ID to use for batched requests if none can be determined
     */
    public static final void setDefaultBatchApplicationId(final String applicationId) {

        defaultBatchApplicationId = applicationId;
    }

    /**
     * Serializes one or more requests but does not execute them. The resulting HttpURLConnection can be executed
     * explicitly by the caller.
     *
     * @param requests one or more Requests to serialize
     * @return an HttpURLConnection which is ready to execute
     * @throws FacebookException If any of the requests in the batch are badly constructed or if there are problems
     *             contacting the service
     * @throws IllegalArgumentException if the passed in collection is empty
     * @throws NullPointerException if the passed in collection or any of its contents are null
     */
    public static HttpURLConnection toHttpConnection(final Collection<Request> requests) {

        Validate.notEmptyAndContainsNoNulls(requests, REQUESTS2);

        return toHttpConnection(new RequestBatch(requests));
    }

    /**
     * Serializes one or more requests but does not execute them. The resulting HttpURLConnection can be executed
     * explicitly by the caller.
     *
     * @param requests one or more Requests to serialize
     * @return an HttpURLConnection which is ready to execute
     * @throws FacebookException If any of the requests in the batch are badly constructed or if there are problems
     *             contacting the service
     * @throws IllegalArgumentException if the passed in array is zero-length
     * @throws NullPointerException if the passed in array or any of its contents are null
     */
    public static HttpURLConnection toHttpConnection(final Request... requests) {

        return toHttpConnection(Arrays.asList(requests));
    }

    /**
     * Serializes one or more requests but does not execute them. The resulting HttpURLConnection can be executed
     * explicitly by the caller.
     *
     * @param requests a RequestBatch to serialize
     * @return an HttpURLConnection which is ready to execute
     * @throws FacebookException If any of the requests in the batch are badly constructed or if there are problems
     *             contacting the service
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection toHttpConnection(final RequestBatch requests) {

        for (final Request request : requests) {
            request.validate();
        }

        URL url = null;
        try {
            if (requests.size() == 1) {
                // Single request case.
                final Request request = requests.get(0);
                // In the non-batch case, the URL we use really is the same one returned by getUrlForSingleRequest.
                url = new URL(request.getUrlForSingleRequest());
            } else {
                // Batch case -- URL is just the graph API base, individual request URLs are serialized
                // as relative_url parameters within each batch entry.
                url = new URL(ServerProtocol.getGraphUrlBase());
            }
        } catch (final MalformedURLException e) {
            throw new FacebookException("could not construct URL for request", e);
        }

        HttpURLConnection connection;
        try {
            connection = createConnection(url);

            serializeToUrlConnection(requests, connection);
        } catch (final IOException e) {
            throw new FacebookException("could not construct request body", e);
        } catch (final JSONException e) {
            throw new FacebookException("could not construct request body", e);
        }

        return connection;
    }

    private static String getBatchAppId(final RequestBatch batch) {

        if (!Utility.isNullOrEmpty(batch.getBatchApplicationId())) {
            return batch.getBatchApplicationId();
        }

        for (final Request request : batch) {
            final Session session = request.session;
            if (session != null) {
                return session.getApplicationId();
            }
        }
        return Request.defaultBatchApplicationId;
    }

    private static String getMimeContentType() {

        return String.format("multipart/form-data; boundary=%s", MIME_BOUNDARY);
    }

    private static String getUserAgent() {

        if (userAgent == null) {
            userAgent = String.format("%s.%s", USER_AGENT_BASE, FacebookSdkVersion.BUILD);
        }

        return userAgent;
    }

    private static boolean hasOnProgressCallbacks(final RequestBatch requests) {

        for (final RequestBatch.Callback callback : requests.getCallbacks()) {
            if (callback instanceof RequestBatch.OnProgressCallback) {
                return true;
            }
        }

        for (final Request request : requests) {
            if (request.getCallback() instanceof OnProgressCallback) {
                return true;
            }
        }

        return false;
    }

    private static boolean isSupportedAttachmentType(final Object value) {

        return (value instanceof Bitmap) || (value instanceof byte[]) || (value instanceof ParcelFileDescriptor)
                || (value instanceof ParcelFileDescriptorWithMimeType);
    }

    private static boolean isSupportedParameterType(final Object value) {

        return (value instanceof String) || (value instanceof Boolean) || (value instanceof Number)
                || (value instanceof Date);
    }

    /**
     * Creates a new Request configured to post a status update to a user's feed.
     *
     * @param session the Session to use, or null; if non-null, the session must be in an opened state
     * @param message the text of the status update
     * @param placeId an optional place id to associate with the post
     * @param tagIds an optional list of user ids to tag in the post
     * @param callback a callback that will be called when the request is completed to handle success or error
     *            conditions
     * @return a Request that is ready to execute
     */
    private static Request newStatusUpdateRequest(final Session session, final String message, final String placeId,
            final List<String> tagIds, final Callback callback) {

        final Bundle parameters = new Bundle();
        parameters.putString("message", message);

        if (placeId != null) {
            parameters.putString("place", placeId);
        }

        if ((tagIds != null) && (tagIds.size() > 0)) {
            final String tags = TextUtils.join(",", tagIds);
            parameters.putString("tags", tags);
        }

        return new Request(session, MY_FEED, parameters, HttpMethod.POST, callback);
    }

    private static String parameterToString(final Object value) {

        if (value instanceof String) {
            return (String) value;
        } else if ((value instanceof Boolean) || (value instanceof Number)) {
            return value.toString();
        } else if (value instanceof Date) {
            final SimpleDateFormat iso8601DateFormat = new SimpleDateFormat(ISO_8601_FORMAT_STRING, Locale.US);
            return iso8601DateFormat.format(value);
        }
        throw new IllegalArgumentException("Unsupported parameter type.");
    }

    private static void processGraphObject(final GraphObject graphObject, final String path,
            final KeyValueSerializer serializer) throws IOException {

        // In general, graph objects are passed by reference (ID/URL). But if this is an OG Action,
        // we need to pass the entire values of the contents of the 'image' property, as they
        // contain important metadata beyond just a URL. We don't have a 100% foolproof way of knowing
        // if we are posting an OG Action, given that batched requests can have parameter substitution,
        // but passing the OG Action type as a substituted parameter is unlikely.
        // It looks like an OG Action if it's posted to me/namespace:action[?other=stuff].
        boolean isOGAction = false;
        if (path.startsWith("me/") || path.startsWith("/me/")) {
            final int colonLocation = path.indexOf(":");
            final int questionMarkLocation = path.indexOf("?");
            isOGAction = (colonLocation > 3)
                    && ((questionMarkLocation == -1) || (colonLocation < questionMarkLocation));
        }

        final Set<Entry<String, Object>> entries = graphObject.asMap().entrySet();
        for (final Entry<String, Object> entry : entries) {
            final boolean passByValue = isOGAction && IMAGE.equalsIgnoreCase(entry.getKey());
            processGraphObjectProperty(entry.getKey(), entry.getValue(), serializer, passByValue);
        }
    }

    private static void processGraphObjectProperty(final String key, Object value, final KeyValueSerializer serializer,
            final boolean passByValue) throws IOException {

        Class<?> valueClass = value.getClass();
        if (GraphObject.class.isAssignableFrom(valueClass)) {
            value = ((GraphObject) value).getInnerJSONObject();
            valueClass = value.getClass();
        } else if (GraphObjectList.class.isAssignableFrom(valueClass)) {
            value = ((GraphObjectList<?>) value).getInnerJSONArray();
            valueClass = value.getClass();
        }

        if (JSONObject.class.isAssignableFrom(valueClass)) {
            final JSONObject jsonObject = (JSONObject) value;
            if (passByValue) {
                // We need to pass all properties of this object in key[propertyName] format.
                @SuppressWarnings("unchecked")
                final Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    final String propertyName = keys.next();
                    final String subKey = String.format(Locale.getDefault(), "%s[%s]", key, propertyName);
                    processGraphObjectProperty(subKey, jsonObject.opt(propertyName), serializer, passByValue);
                }
            } else {
                // Normal case is passing objects by reference, so just pass the ID or URL, if any, as the value
                // for "key"
                if (jsonObject.has("id")) {
                    processGraphObjectProperty(key, jsonObject.optString("id"), serializer, passByValue);
                } else if (jsonObject.has("url")) {
                    processGraphObjectProperty(key, jsonObject.optString("url"), serializer, passByValue);
                }
            }
        } else if (JSONArray.class.isAssignableFrom(valueClass)) {
            final JSONArray jsonArray = (JSONArray) value;
            final int length = jsonArray.length();
            for (int i = 0; i < length; ++i) {
                final String subKey = String.format(Locale.getDefault(), "%s[%d]", key, Integer.valueOf(i));
                processGraphObjectProperty(subKey, jsonArray.opt(i), serializer, passByValue);
            }
        } else if (String.class.isAssignableFrom(valueClass) || Number.class.isAssignableFrom(valueClass)
                || Boolean.class.isAssignableFrom(valueClass)) {
            serializer.writeString(key, value.toString());
        } else if (Date.class.isAssignableFrom(valueClass)) {
            final Date date = (Date) value;
            // The "Events Timezone" platform migration affects what date/time formats Facebook accepts and returns.
            // Apps created after 8/1/12 (or apps that have explicitly enabled the migration) should send/receive
            // dates in ISO-8601 format. Pre-migration apps can send as Unix timestamps. Since the future is ISO-8601,
            // that is what we support here. Apps that need pre-migration behavior can explicitly send these as
            // integer timestamps rather than Dates.
            final SimpleDateFormat iso8601DateFormat = new SimpleDateFormat(ISO_8601_FORMAT_STRING, Locale.US);
            serializer.writeString(key, iso8601DateFormat.format(date));
        }
    }

    private static void processRequest(final RequestBatch requests, final Logger logger, final int numRequests,
            final URL url, final OutputStream outputStream) throws IOException, JSONException {

        final Serializer serializer = new Serializer(outputStream, logger);

        if (numRequests == 1) {
            final Request request = requests.get(0);

            final Map<String, Attachment> attachments = new HashMap<String, Attachment>();
            for (final String key : request.parameters.keySet()) {
                final Object value = request.parameters.get(key);
                if (isSupportedAttachmentType(value)) {
                    attachments.put(key, new Attachment(request, value));
                }
            }

            if (logger != null) {
                logger.append("  Parameters:\n");
            }
            serializeParameters(request.parameters, serializer, request);

            if (logger != null) {
                logger.append("  Attachments:\n");
            }
            serializeAttachments(attachments, serializer);

            if (request.graphObject != null) {
                processGraphObject(request.graphObject, url.getPath(), serializer);
            }
        } else {
            final String batchAppID = getBatchAppId(requests);
            if (Utility.isNullOrEmpty(batchAppID)) {
                throw new FacebookException("At least one request in a batch must have an open Session, or a "
                        + "default app ID must be specified.");
            }

            serializer.writeString(BATCH_APP_ID_PARAM, batchAppID);

            // We write out all the requests as JSON, remembering which file attachments they have, then
            // write out the attachments.
            final Map<String, Attachment> attachments = new HashMap<String, Attachment>();
            serializeRequestsAsJSON(serializer, requests, attachments);

            if (logger != null) {
                logger.append("  Attachments:\n");
            }
            serializeAttachments(attachments, serializer);
        }
    }

    private static void serializeAttachments(final Map<String, Attachment> attachments, final Serializer serializer)
            throws IOException {

        final Set<String> keys = attachments.keySet();

        for (final String key : keys) {
            final Attachment attachment = attachments.get(key);
            if (isSupportedAttachmentType(attachment.getValue())) {
                serializer.writeObject(key, attachment.getValue(), attachment.getRequest());
            }
        }
    }

    private static void serializeParameters(final Bundle bundle, final Serializer serializer, final Request request)
            throws IOException {

        final Set<String> keys = bundle.keySet();

        for (final String key : keys) {
            final Object value = bundle.get(key);
            if (isSupportedParameterType(value)) {
                serializer.writeObject(key, value, request);
            }
        }
    }

    private static void serializeRequestsAsJSON(final Serializer serializer, final Collection<Request> requests,
            final Map<String, Attachment> attachments) throws JSONException, IOException {

        final JSONArray batch = new JSONArray();
        for (final Request request : requests) {
            request.serializeToBatch(batch, attachments);
        }

        serializer.writeRequestsAsJson(BATCH_PARAM, batch, requests);
    }

    static HttpURLConnection createConnection(final URL url) throws IOException {

        HttpURLConnection connection;
        connection = (HttpURLConnection) url.openConnection();

        connection.setRequestProperty(USER_AGENT_HEADER, getUserAgent());
        connection.setRequestProperty(CONTENT_TYPE_HEADER, getMimeContentType());
        connection.setRequestProperty(ACCEPT_LANGUAGE_HEADER, Locale.getDefault().toString());

        connection.setChunkedStreamingMode(0);
        return connection;
    }

    static void runCallbacks(final RequestBatch requests, final List<Response> responses) {

        final int numRequests = requests.size();

        // Compile the list of callbacks to call and then run them either on this thread or via the Handler we received
        final ArrayList<Pair<Callback, Response>> callbacks = new ArrayList<Pair<Callback, Response>>();
        for (int i = 0; i < numRequests; ++i) {
            final Request request = requests.get(i);
            if (request.callback != null) {
                callbacks.add(new Pair<Callback, Response>(request.callback, responses.get(i)));
            }
        }

        if (callbacks.size() > 0) {
            final Runnable runnable = new Runnable() {

                @Override
                public void run() {

                    for (final Pair<Callback, Response> pair : callbacks) {
                        pair.first.onCompleted(pair.second);
                    }

                    final List<RequestBatch.Callback> batchCallbacks = requests.getCallbacks();
                    for (final RequestBatch.Callback batchCallback : batchCallbacks) {
                        batchCallback.onBatchCompleted(requests);
                    }
                }
            };

            final Handler callbackHandler = requests.getCallbackHandler();
            if (callbackHandler == null) {
                // Run on this thread.
                runnable.run();
            } else {
                // Post to the handler.
                callbackHandler.post(runnable);
            }
        }
    }

    final static void serializeToUrlConnection(final RequestBatch requests, final HttpURLConnection connection)
            throws IOException, JSONException {

        final Logger logger = new Logger(LoggingBehavior.REQUESTS, "Request");

        final int numRequests = requests.size();

        final HttpMethod connectionHttpMethod = (numRequests == 1) ? requests.get(0).httpMethod : HttpMethod.POST;
        connection.setRequestMethod(connectionHttpMethod.name());

        final URL url = connection.getURL();
        logger.append("Request:\n");
        logger.appendKeyValue("Id", requests.getId());
        logger.appendKeyValue("URL", url);
        logger.appendKeyValue("Method", connection.getRequestMethod());
        logger.appendKeyValue("User-Agent", connection.getRequestProperty("User-Agent"));
        logger.appendKeyValue("Content-Type", connection.getRequestProperty("Content-Type"));

        connection.setConnectTimeout(requests.getTimeout());
        connection.setReadTimeout(requests.getTimeout());

        // If we have a single non-POST request, don't try to serialize anything or HttpURLConnection will
        // turn it into a POST.
        final boolean isPost = (connectionHttpMethod == HttpMethod.POST);
        if (!isPost) {
            logger.log();
            return;
        }

        connection.setDoOutput(true);

        OutputStream outputStream = null;
        try {
            if (hasOnProgressCallbacks(requests)) {
                ProgressNoopOutputStream countingStream = null;
                countingStream = new ProgressNoopOutputStream(requests.getCallbackHandler());
                processRequest(requests, null, numRequests, url, countingStream);

                final int max = countingStream.getMaxProgress();
                final Map<Request, RequestProgress> progressMap = countingStream.getProgressMap();

                final BufferedOutputStream buffered = new BufferedOutputStream(connection.getOutputStream());
                outputStream = new ProgressOutputStream(buffered, requests, progressMap, max);
            } else {
                outputStream = new BufferedOutputStream(connection.getOutputStream());
            }

            processRequest(requests, logger, numRequests, url, outputStream);
        } finally {
            outputStream.close();
        }

        logger.log();
    }

    static <T extends GraphObject> List<T> typedListFromResponse(final Response response, final Class<T> clazz) {

        final GraphMultiResult multiResult = response.getGraphObjectAs(GraphMultiResult.class);
        if (multiResult == null) {
            return null;
        }

        final GraphObjectList<GraphObject> data = multiResult.getData();
        if (data == null) {
            return null;
        }

        return data.castToListOf(clazz);
    }

    /**
     * Executes this request and returns the response.
     * <p/>
     * This should only be called if you have transitioned off the UI thread.
     *
     * @return the Response object representing the results of the request
     * @throws FacebookException If there was an error in the protocol used to communicate with the service
     * @throws IllegalArgumentException
     */
    public final Response executeAndWait() {

        return Request.executeAndWait(this);
    }

    /**
     * Executes this request and returns the response.
     * <p/>
     * This should only be called from the UI thread.
     *
     * @return a RequestAsyncTask that is executing the request
     * @throws IllegalArgumentException
     */
    public final RequestAsyncTask executeAsync() {

        return Request.executeBatchAsync(this);
    }

    /**
     * Returns the name of the request that this request entry explicitly depends on in a batched request.
     *
     * @return the name of this request's dependency, or null if none has been specified
     */
    public final String getBatchEntryDependsOn() {

        return this.batchEntryDependsOn;
    }

    /**
     * Returns the name of this request's entry in a batched request.
     *
     * @return the name of this request's batch entry, or null if none has been specified
     */
    public final String getBatchEntryName() {

        return this.batchEntryName;
    }

    /**
     * Returns whether or not this batch entry will return a response if it is successful. Only applies if another
     * request entry in the batch specifies this entry as a dependency.
     *
     * @return the name of this request's dependency, or null if none has been specified
     */
    public final boolean getBatchEntryOmitResultOnSuccess() {

        return this.batchEntryOmitResultOnSuccess;
    }

    /**
     * Returns the callback which will be called when the request finishes.
     *
     * @return the callback
     */
    public final Callback getCallback() {

        return this.callback;
    }

    /**
     * Returns the GraphObject, if any, associated with this request.
     *
     * @return the GraphObject associated with this requeset, or null if there is none
     */
    public final GraphObject getGraphObject() {

        return this.graphObject;
    }

    /**
     * Returns the graph path of this request, if any.
     *
     * @return the graph path of this request, or null if there is none
     */
    public final String getGraphPath() {

        return this.graphPath;
    }

    /**
     * Returns the {@link HttpMethod} to use for this request.
     *
     * @return the HttpMethod
     */
    public final HttpMethod getHttpMethod() {

        return this.httpMethod;
    }

    /**
     * Returns the parameters for this request.
     *
     * @return the parameters
     */
    public final Bundle getParameters() {

        return this.parameters;
    }

    /**
     * Returns the REST method to call for this request.
     *
     * @return the REST method
     */
    public final String getRestMethod() {

        return this.restMethod;
    }

    /**
     * Returns the Session associated with this request.
     *
     * @return the Session associated with this request, or null if none has been specified
     */
    public final Session getSession() {

        return this.session;
    }

    /**
     * Gets the tag on the request; this is an application-defined object that can be used to distinguish between
     * different requests. Its value has no effect on the execution of the request.
     *
     * @return an object that serves as a tag, or null
     */
    public final Object getTag() {

        return this.tag;
    }

    /**
     * Returns the version of the API that this request will use. By default this is the current API at the time the SDK
     * is released.
     *
     * @return the version that this request will use
     */
    public final String getVersion() {

        return this.version;
    }

    /**
     * Sets the name of the request entry that this request explicitly depends on in a batched request. This value is
     * only used if this request is submitted as part of a batched request. It can be used to specified dependencies
     * between requests. See <a href="https://developers.facebook.com/docs/reference/api/batch/">Batch Requests</a> in
     * the Graph API documentation for more details.
     *
     * @param batchEntryDependsOn the name of the request entry that this entry depends on in a batched request
     */
    public final void setBatchEntryDependsOn(final String batchEntryDependsOn) {

        this.batchEntryDependsOn = batchEntryDependsOn;
    }

    /**
     * Sets the name of this request's entry in a batched request. This value is only used if this request is submitted
     * as part of a batched request. It can be used to specified dependencies between requests. See <a
     * href="https://developers.facebook.com/docs/reference/api/batch/">Batch Requests</a> in the Graph API
     * documentation for more details.
     *
     * @param batchEntryName the name of this request's entry in a batched request, which must be unique within a
     *            particular batch of requests
     */
    public final void setBatchEntryName(final String batchEntryName) {

        this.batchEntryName = batchEntryName;
    }

    /**
     * Sets whether or not this batch entry will return a response if it is successful. Only applies if another request
     * entry in the batch specifies this entry as a dependency. See <a
     * href="https://developers.facebook.com/docs/reference/api/batch/">Batch Requests</a> in the Graph API
     * documentation for more details.
     *
     * @param batchEntryOmitResultOnSuccess the name of the request entry that this entry depends on in a batched
     *            request
     */
    public final void setBatchEntryOmitResultOnSuccess(final boolean batchEntryOmitResultOnSuccess) {

        this.batchEntryOmitResultOnSuccess = batchEntryOmitResultOnSuccess;
    }

    /**
     * Sets the callback which will be called when the request finishes.
     *
     * @param callback the callback
     */
    public final void setCallback(final Callback callback) {

        this.callback = callback;
    }

    /**
     * Sets the GraphObject associated with this request. This is meaningful only for POST requests.
     *
     * @param graphObject the GraphObject to upload along with this request
     */
    public final void setGraphObject(final GraphObject graphObject) {

        this.graphObject = graphObject;
    }

    /**
     * Sets the graph path of this request. A graph path may not be set if a REST method has been specified.
     *
     * @param graphPath the graph path for this request
     */
    public final void setGraphPath(final String graphPath) {

        this.graphPath = graphPath;
    }

    /**
     * Sets the {@link HttpMethod} to use for this request.
     *
     * @param httpMethod the HttpMethod, or null for the default (HttpMethod.GET).
     */
    public final void setHttpMethod(final HttpMethod httpMethod) {

        if ((this.overriddenURL != null) && (httpMethod != HttpMethod.GET)) {
            throw new FacebookException("Can't change HTTP method on request with overridden URL.");
        }
        this.httpMethod = (httpMethod != null) ? httpMethod : HttpMethod.GET;
    }

    /**
     * Sets the parameters for this request.
     *
     * @param parameters the parameters
     */
    public final void setParameters(final Bundle parameters) {

        this.parameters = parameters;
    }

    /**
     * Sets the REST method to call for this request. A REST method may not be set if a graph path has been specified.
     *
     * @param restMethod the REST method to call
     */
    public final void setRestMethod(final String restMethod) {

        this.restMethod = restMethod;
    }

    /**
     * Sets the Session to use for this request. The Session does not need to be opened at the time it is specified, but
     * it must be opened by the time the request is executed.
     *
     * @param session the Session to use for this request
     */
    public final void setSession(final Session session) {

        this.session = session;
    }

    /**
     * Sets the tag on the request; this is an application-defined object that can be used to distinguish between
     * different requests. Its value has no effect on the execution of the request.
     *
     * @param tag an object to serve as a tag, or null
     */
    public final void setTag(final Object tag) {

        this.tag = tag;
    }

    /**
     * Set the version to use for this request. By default the version will be the current API at the time the SDK is
     * released. Only use this if you need to explicitly override.
     *
     * @param version The version to use. Should look like "v2.0"
     */
    public final void setVersion(final String version) {

        this.version = version;
    }

    /**
     * Returns a string representation of this Request, useful for debugging.
     *
     * @return the debugging information
     */
    @Override
    public String toString() {

        return new StringBuilder().append("{Request: ").append(" session: ").append(this.session)
                .append(", graphPath: ").append(this.graphPath).append(", graphObject: ").append(this.graphObject)
                .append(", restMethod: ").append(this.restMethod).append(", httpMethod: ").append(this.httpMethod)
                .append(", parameters: ").append(this.parameters).append("}").toString();
    }

    private void addCommonParameters() {

        if (this.session != null) {
            if (!this.session.isOpened()) {
                throw new FacebookException("Session provided to a Request in un-opened state.");
            } else if (!this.parameters.containsKey(ACCESS_TOKEN_PARAM)) {
                final String accessToken = this.session.getAccessToken();
                Logger.registerAccessToken(accessToken);
                this.parameters.putString(ACCESS_TOKEN_PARAM, accessToken);
            }
        } else if (!this.parameters.containsKey(ACCESS_TOKEN_PARAM)) {
            final String appID = Settings.getApplicationId();
            final String clientToken = Settings.getClientToken();
            if (!Utility.isNullOrEmpty(appID) && !Utility.isNullOrEmpty(clientToken)) {
                final String accessToken = appID + "|" + clientToken;
                this.parameters.putString(ACCESS_TOKEN_PARAM, accessToken);
            } else {
                Log.d(TAG,
                        "Warning: Sessionless Request needs token but missing either application ID or client token.");
            }
        }
        this.parameters.putString(SDK_PARAM, SDK_ANDROID);
        this.parameters.putString(FORMAT_PARAM, FORMAT_JSON);
    }

    private String appendParametersToBaseUrl(final String baseUrl) {

        final Uri.Builder uriBuilder = new Uri.Builder().encodedPath(baseUrl);

        final Set<String> keys = this.parameters.keySet();
        for (final String key : keys) {
            Object value = this.parameters.get(key);

            if (value == null) {
                value = "";
            }

            if (isSupportedParameterType(value)) {
                value = parameterToString(value);
            } else {
                if (this.httpMethod == HttpMethod.GET) {
                    throw new IllegalArgumentException(String.format("Unsupported parameter type for GET request: %s",
                            value.getClass().getSimpleName()));
                }
                continue;
            }

            uriBuilder.appendQueryParameter(key, value.toString());
        }

        return uriBuilder.toString();
    }

    private String getGraphPathWithVersion() {

        final Matcher matcher = versionPattern.matcher(this.graphPath);
        if (matcher.matches()) {
            return this.graphPath;
        }
        return String.format("%s/%s", this.version, this.graphPath);
    }

    private String getRestPathWithVersion() {

        final Matcher matcher = versionPattern.matcher(this.restMethod);
        if (matcher.matches()) {
            return this.restMethod;
        }
        return String.format("%s/%s/%s", this.version, ServerProtocol.REST_METHOD_BASE, this.restMethod);
    }

    private void serializeToBatch(final JSONArray batch, final Map<String, Attachment> attachments)
            throws JSONException, IOException {

        final JSONObject batchEntry = new JSONObject();

        if (this.batchEntryName != null) {
            batchEntry.put(BATCH_ENTRY_NAME_PARAM, this.batchEntryName);
            batchEntry.put(BATCH_ENTRY_OMIT_RESPONSE_ON_SUCCESS_PARAM, this.batchEntryOmitResultOnSuccess);
        }
        if (this.batchEntryDependsOn != null) {
            batchEntry.put(BATCH_ENTRY_DEPENDS_ON_PARAM, this.batchEntryDependsOn);
        }

        final String relativeURL = this.getUrlForBatchedRequest();
        batchEntry.put(BATCH_RELATIVE_URL_PARAM, relativeURL);
        batchEntry.put(BATCH_METHOD_PARAM, this.httpMethod);
        if (this.session != null) {
            final String accessToken = this.session.getAccessToken();
            Logger.registerAccessToken(accessToken);
        }

        // Find all of our attachments. Remember their names and put them in the attachment map.
        final ArrayList<String> attachmentNames = new ArrayList<String>();
        final Set<String> keys = this.parameters.keySet();
        for (final String key : keys) {
            final Object value = this.parameters.get(key);
            if (isSupportedAttachmentType(value)) {
                // Make the name unique across this entire batch.
                final String name = String.format(Locale.getDefault(), "%s%d", ATTACHMENT_FILENAME_PREFIX,
                        attachments.size());
                attachmentNames.add(name);
                attachments.put(name, new Attachment(this, value));
            }
        }

        if (!attachmentNames.isEmpty()) {
            final String attachmentNamesString = TextUtils.join(",", attachmentNames);
            batchEntry.put(ATTACHED_FILES_PARAM, attachmentNamesString);
        }

        if (this.graphObject != null) {
            // Serialize the graph object into the "body" parameter.
            final ArrayList<String> keysAndValues = new ArrayList<String>();
            processGraphObject(this.graphObject, relativeURL, new KeyValueSerializer() {

                @Override
                public void writeString(final String key, final String value) throws IOException {

                    keysAndValues.add(String.format("%s=%s", key, URLEncoder.encode(value, "UTF-8")));
                }
            });
            final String bodyValue = TextUtils.join("&", keysAndValues);
            batchEntry.put(BATCH_BODY_PARAM, bodyValue);
        }

        batch.put(batchEntry);
    }

    private void validate() {

        if ((this.graphPath != null) && (this.restMethod != null)) {
            throw new IllegalArgumentException("Only one of a graph path or REST method may be specified per request.");
        }
    }

    final String getUrlForBatchedRequest() {

        if (this.overriddenURL != null) {
            throw new FacebookException("Can't override URL for a batch request");
        }

        String baseUrl;
        if (this.restMethod != null) {
            baseUrl = this.getRestPathWithVersion();
        } else {
            baseUrl = this.getGraphPathWithVersion();
        }

        this.addCommonParameters();
        return this.appendParametersToBaseUrl(baseUrl);
    }

    final String getUrlForSingleRequest() {

        if (this.overriddenURL != null) {
            return this.overriddenURL;
        }

        String baseUrl;
        if (this.restMethod != null) {
            baseUrl = String.format("%s/%s", ServerProtocol.getRestUrlBase(), this.getRestPathWithVersion());
        } else {
            String graphBaseUrlBase;
            if ((this.getHttpMethod() == HttpMethod.POST) && (this.graphPath != null)
                    && this.graphPath.endsWith(VIDEOS_SUFFIX)) {
                graphBaseUrlBase = ServerProtocol.getGraphVideoUrlBase();
            } else {
                graphBaseUrlBase = ServerProtocol.getGraphUrlBase();
            }
            baseUrl = String.format("%s/%s", graphBaseUrlBase, this.getGraphPathWithVersion());
        }

        this.addCommonParameters();
        return this.appendParametersToBaseUrl(baseUrl);
    }

    /**
     * Specifies the interface that consumers of the Request class can implement in order to be notified when a
     * particular request completes, either successfully or with an error.
     */
    public interface Callback {

        /**
         * The method that will be called when a request completes.
         *
         * @param response the Response of this request, which may include error information if the request was
         *            unsuccessful
         */
        void onCompleted(Response response);
    }

    /**
     * Specifies the interface that consumers of
     * {@link Request#executePlacesSearchRequestAsync(Session, android.location.Location, int, int, String, com.wootag.facebook.Request.GraphPlaceListCallback)}
     * can use to be notified when the request completes, either successfully or with an error.
     */
    public interface GraphPlaceListCallback {

        /**
         * The method that will be called when the request completes.
         *
         * @param places the list of GraphObjects representing the returned places, or null
         * @param response the Response of this request, which may include error information if the request was
         *            unsuccessful
         */
        void onCompleted(List<GraphPlace> places, Response response);
    }

    /**
     * Specifies the interface that consumers of
     * {@link Request#executeMeRequestAsync(Session, com.wootag.facebook.Request.GraphUserCallback)} can use to be
     * notified when the request completes, either successfully or with an error.
     */
    public interface GraphUserCallback {

        /**
         * The method that will be called when the request completes.
         *
         * @param user the GraphObject representing the returned user, or null
         * @param response the Response of this request, which may include error information if the request was
         *            unsuccessful
         */
        void onCompleted(GraphUser user, Response response);
    }

    /**
     * Specifies the interface that consumers of
     * {@link Request#executeMyFriendsRequestAsync(Session, com.wootag.facebook.Request.GraphUserListCallback)} can use
     * to be notified when the request completes, either successfully or with an error.
     */
    public interface GraphUserListCallback {

        /**
         * The method that will be called when the request completes.
         *
         * @param users the list of GraphObjects representing the returned friends, or null
         * @param response the Response of this request, which may include error information if the request was
         *            unsuccessful
         */
        void onCompleted(List<GraphUser> users, Response response);
    }

    /**
     * Specifies the interface that consumers of the Request class can implement in order to be notified when a progress
     * is made on a particular request. The frequency of the callbacks can be controlled using
     * {@link com.wootag.facebook.Settings#setOnProgressThreshold(long)}
     */
    public interface OnProgressCallback extends Callback {

        /**
         * The method that will be called when progress is made.
         *
         * @param current the current value of the progress of the request.
         * @param max the maximum value (target) value that the progress will have.
         */
        void onProgress(long current, long max);
    }

    private static class Attachment {

        private final Request request;
        private final Object value;

        public Attachment(final Request request, final Object value) {

            this.request = request;
            this.value = value;
        }

        public Request getRequest() {

            return this.request;
        }

        public Object getValue() {

            return this.value;
        }
    }

    private interface KeyValueSerializer {

        void writeString(String key, String value) throws IOException;
    }

    private static class ParcelFileDescriptorWithMimeType implements Parcelable {

        private final String mimeType;
        private final ParcelFileDescriptor fileDescriptor;

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<ParcelFileDescriptorWithMimeType> CREATOR = new Parcelable.Creator<ParcelFileDescriptorWithMimeType>() {

            @Override
            public ParcelFileDescriptorWithMimeType createFromParcel(final Parcel in) {

                return new ParcelFileDescriptorWithMimeType(in);
            }

            @Override
            public ParcelFileDescriptorWithMimeType[] newArray(final int size) {

                return new ParcelFileDescriptorWithMimeType[size];
            }
        };

        public ParcelFileDescriptorWithMimeType(final ParcelFileDescriptor fileDescriptor, final String mimeType) {

            this.mimeType = mimeType;
            this.fileDescriptor = fileDescriptor;
        }

        private ParcelFileDescriptorWithMimeType(final Parcel in) {

            this.mimeType = in.readString();
            this.fileDescriptor = in.readFileDescriptor();
        }

        @Override
        public int describeContents() {

            return CONTENTS_FILE_DESCRIPTOR;
        }

        public ParcelFileDescriptor getFileDescriptor() {

            return this.fileDescriptor;
        }

        public String getMimeType() {

            return this.mimeType;
        }

        @Override
        public void writeToParcel(final Parcel out, final int flags) {

            out.writeString(this.mimeType);
            out.writeFileDescriptor(this.fileDescriptor.getFileDescriptor());
        }
    }

    private static class Serializer implements KeyValueSerializer {

        private static final String FOUR_SPACES = "    ";
        private final OutputStream outputStream;
        private final Logger logger;
        private boolean firstWrite = true;

        public Serializer(final OutputStream outputStream, final Logger logger) {

            this.outputStream = outputStream;
            this.logger = logger;
        }

        public void write(final String format, final Object... args) throws IOException {

            if (this.firstWrite) {
                // Prepend all of our output with a boundary string.
                this.outputStream.write("--".getBytes());
                this.outputStream.write(MIME_BOUNDARY.getBytes());
                this.outputStream.write("\r\n".getBytes());
                this.firstWrite = false;
            }
            this.outputStream.write(String.format(format, args).getBytes());
        }

        public void writeBitmap(final String key, final Bitmap bitmap) throws IOException {

            this.writeContentDisposition(key, key, "image/png");
            // Note: quality parameter is ignored for PNG
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, this.outputStream);
            this.writeLine("");
            this.writeRecordBoundary();
            if (this.logger != null) {
                this.logger.appendKeyValue(FOUR_SPACES + key, "<Image>");
            }
        }

        public void writeBytes(final String key, final byte[] bytes) throws IOException {

            this.writeContentDisposition(key, key, "content/unknown");
            this.outputStream.write(bytes);
            this.writeLine("");
            this.writeRecordBoundary();
            if (this.logger != null) {
                this.logger.appendKeyValue(FOUR_SPACES + key, String.format("<Data: %d>", bytes.length));
            }
        }

        public void writeContentDisposition(final String name, final String filename, final String contentType)
                throws IOException {

            this.write("Content-Disposition: form-data; name=\"%s\"", name);
            if (filename != null) {
                this.write("; filename=\"%s\"", filename);
            }
            this.writeLine(""); // newline after Content-Disposition
            if (contentType != null) {
                this.writeLine("%s: %s", CONTENT_TYPE_HEADER, contentType);
            }
            this.writeLine(""); // blank line before content
        }

        public void writeFile(final String key, final ParcelFileDescriptor descriptor, String mimeType)
                throws IOException {

            if (mimeType == null) {
                mimeType = "content/unknown";
            }
            this.writeContentDisposition(key, key, mimeType);

            int totalBytes = 0;

            if (this.outputStream instanceof ProgressNoopOutputStream) {
                // If we are only counting bytes then skip reading the file
                ((ProgressNoopOutputStream) this.outputStream).addProgress(descriptor.getStatSize());
            } else {
                ParcelFileDescriptor.AutoCloseInputStream inputStream = null;
                BufferedInputStream bufferedInputStream = null;
                try {
                    inputStream = new ParcelFileDescriptor.AutoCloseInputStream(descriptor);
                    bufferedInputStream = new BufferedInputStream(inputStream);

                    final byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                        this.outputStream.write(buffer, 0, bytesRead);
                        totalBytes += bytesRead;
                    }
                } finally {
                    if (bufferedInputStream != null) {
                        bufferedInputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            }
            this.writeLine("");
            this.writeRecordBoundary();
            if (this.logger != null) {
                this.logger.appendKeyValue(FOUR_SPACES + key, String.format("<Data: %d>", totalBytes));
            }
        }

        public void writeFile(final String key, final ParcelFileDescriptorWithMimeType descriptorWithMimeType)
                throws IOException {

            this.writeFile(key, descriptorWithMimeType.getFileDescriptor(), descriptorWithMimeType.getMimeType());
        }

        public void writeLine(final String format, final Object... args) throws IOException {

            this.write(format, args);
            this.write("\r\n");
        }

        public void writeObject(final String key, final Object value, final Request request) throws IOException {

            if (this.outputStream instanceof RequestOutputStream) {
                ((RequestOutputStream) this.outputStream).setCurrentRequest(request);
            }

            if (isSupportedParameterType(value)) {
                this.writeString(key, parameterToString(value));
            } else if (value instanceof Bitmap) {
                this.writeBitmap(key, (Bitmap) value);
            } else if (value instanceof byte[]) {
                this.writeBytes(key, (byte[]) value);
            } else if (value instanceof ParcelFileDescriptor) {
                this.writeFile(key, (ParcelFileDescriptor) value, null);
            } else if (value instanceof ParcelFileDescriptorWithMimeType) {
                this.writeFile(key, (ParcelFileDescriptorWithMimeType) value);
            } else {
                throw new IllegalArgumentException("value is not a supported type: String, Bitmap, byte[]");
            }
        }

        public void writeRecordBoundary() throws IOException {

            this.writeLine("--%s", MIME_BOUNDARY);
        }

        public void writeRequestsAsJson(final String key, final JSONArray requestJsonArray,
                final Collection<Request> requests) throws IOException, JSONException {

            if (!(this.outputStream instanceof RequestOutputStream)) {
                this.writeString(key, requestJsonArray.toString());
                return;
            }

            final RequestOutputStream requestOutputStream = (RequestOutputStream) this.outputStream;
            this.writeContentDisposition(key, null, null);
            this.write("[");
            int i = 0;
            for (final Request request : requests) {
                final JSONObject requestJson = requestJsonArray.getJSONObject(i);
                requestOutputStream.setCurrentRequest(request);
                if (i > 0) {
                    this.write(",%s", requestJson.toString());
                } else {
                    this.write("%s", requestJson.toString());
                }
                i++;
            }
            this.write("]");
            if (this.logger != null) {
                this.logger.appendKeyValue(FOUR_SPACES + key, requestJsonArray.toString());
            }
        }

        @Override
        public void writeString(final String key, final String value) throws IOException {

            this.writeContentDisposition(key, null, null);
            this.writeLine("%s", value);
            this.writeRecordBoundary();
            if (this.logger != null) {
                this.logger.appendKeyValue(FOUR_SPACES + key, value);
            }
        }

    }
}
