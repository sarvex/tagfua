/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.TagFu.facebook;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wTagFufacebook.internal.AttributionIdentifiers;
import com.woTagFuacebook.internal.Logger;
import com.wooTagFucebook.internal.Utility;
import com.wootTagFuebook.internal.Validate;
import com.wootag.facebook.model.GraphObject;

/**
 * <p>
 * The AppEventsLogger class allows the developer to log various types of events back to Facebook. In order to log
 * events, the app must create an instance of this class via a {@link #newLogger newLogger} method, and then call the
 * various "log" methods off of that.
 * </p>
 * <p>
 * This client-side event logging is then available through Facebook App Insights and for use with Facebook Ads
 * conversion tracking and optimization.
 * </p>
 * <p>
 * The AppEventsLogger class has a few related roles:
 * <ul>
 * <li>Logging predefined and application-defined events to Facebook App Insights with a numeric value to sum across a
 * large number of events, and an optional set of key/value parameters that define "segments" for this event (e.g.,
 * 'purchaserStatus' : 'frequent', or 'gamerLevel' : 'intermediate'). These events may also be used for ads conversion
 * tracking, optimization, and other ads related targeting in the future.</li>
 * <li>Methods that control the way in which events are flushed out to the Facebook servers.</li>
 * </ul>
 * Here are some important characteristics of the logging mechanism provided by AppEventsLogger:
 * <ul>
 * <li>Events are not sent immediately when logged. They're cached and flushed out to the Facebook servers in a number
 * of situations:
 * <ul>
 * <li>when an event count threshold is passed (currently 100 logged events).</li>
 * <li>when a time threshold is passed (currently 60 seconds).</li>
 * <li>when an app has gone to background and is then brought back to the foreground.</li>
 * </ul>
 * <li>Events will be accumulated when the app is in a disconnected state, and sent when the connection is restored and
 * one of the above 'flush' conditions are met.</li>
 * <li>The AppEventsLogger class is intended to be used from the thread it was created on. Multiple AppEventsLoggers may
 * be created on other threads if desired.</li>
 * <li>The developer can call the setFlushBehavior method to force the flushing of events to only occur on an explicit
 * call to the `flush` method.</li>
 * <li>The developer can turn on console debug output for event logging and flushing to the server
 * Settings.addLoggingBehavior(LoggingBehavior.APP_EVENTS);</li>
 * </ul>
 * Some things to note when logging events:
 * <ul>
 * <li>There is a limit on the number of unique event names an app can use, on the order of 300.</li>
 * <li>There is a limit to the number of unique parameter names in the provided parameters that can be used per event,
 * on the order of 25. This is not just for an individual call, but for all invocations for that eventName.</li>
 * <li>Event names and parameter names (the keys in the NSDictionary) must be between 2 and 40 characters, and must
 * consist of alphanumeric characters, _, -, or spaces.</li>
 * <li>The length of each parameter value can be no more than on the order of 100 characters.</li>
 * </ul>
 */
public class AppEventsLogger {

    // Enums

    // Constants
    protected static final String TAG = AppEventsLogger.class.getCanonicalName();

    private static final int NUM_LOG_EVENTS_TO_TRY_TO_FLUSH_AFTER = 100;

    private static final int FLUSH_PERIOD_IN_SECONDS = 60;

    private static final int APP_SUPPORTS_ATTRIBUTION_ID_RECHECK_PERIOD_IN_SECONDS = 60 * 60 * 24;

    private static final int APP_ACTIVATE_SUPPRESSION_PERIOD_IN_SECONDS = 5 * 60;
    // Instance member variables
    private final Context context;
    private final AccessTokenAppIdPair accessTokenAppId;
    protected static Map<AccessTokenAppIdPair, SessionEventsState> stateMap = new ConcurrentHashMap<AccessTokenAppIdPair, SessionEventsState>();

    private static Timer flushTimer;
    private static Timer supportsAttributionRecheckTimer;

    private static FlushBehavior flushBehavior = FlushBehavior.AUTO;
    private static boolean requestInFlight;
    private static Context applicationContext;
    protected static Object staticLock = new Object();
    private static String hashedDeviceAndAppId;
    private static Map<String, Date> mapEventsToSuppressionTime = new HashMap<String, Date>();
    @SuppressWarnings("serial")
    private static Map<String, EventSuppression> mapEventNameToSuppress = new HashMap<String, EventSuppression>() {

        {
            this.put(AppEventsConstants.EVENT_NAME_ACTIVATED_APP, new EventSuppression(
                    APP_ACTIVATE_SUPPRESSION_PERIOD_IN_SECONDS,
                    SuppressionTimeoutBehavior.RESET_TIMEOUT_WHEN_LOG_ATTEMPTED));
        }
    };
    /**
     * The action used to indicate that a flush of app events has occurred. This should be used as an action in an
     * IntentFilter and BroadcastReceiver registered with the {@link android.support.v4.content.LocalBroadcastManager}.
     */
    public static final String ACTION_APP_EVENTS_FLUSHED = "com.facebook.sdk.APP_EVENTS_FLUSHED";
    public static final String APP_EVENTS_EXTRA_NUM_EVENTS_FLUSHED = "com.facebook.sdk.APP_EVENTS_NUM_EVENTS_FLUSHED";
    public static final String APP_EVENTS_EXTRA_FLUSH_RESULT = "com.facebook.sdk.APP_EVENTS_FLUSH_RESULT";

    /**
     * Constructor is private, newLogger() methods should be used to build an instance.
     */
    private AppEventsLogger(final Context context, String applicationId, Session session) {

        Validate.notNull(context, "context");
        this.context = context;

        if (session == null) {
            session = Session.getActiveSession();
        }

        if (session != null) {
            this.accessTokenAppId = new AccessTokenAppIdPair(session);
        } else {
            if (applicationId == null) {
                applicationId = Utility.getMetadataApplicationId(context);
            }
            this.accessTokenAppId = new AccessTokenAppIdPair(null, applicationId);
        }

        synchronized (staticLock) {

            if (hashedDeviceAndAppId == null) {
                hashedDeviceAndAppId = Utility.getHashedDeviceAndAppID(context, applicationId);
            }

            if (applicationContext == null) {
                applicationContext = context.getApplicationContext();
            }
        }

        initializeTimersIfNeeded();
    }

    /**
     * Notifies the events system that the app has launched & logs an activatedApp event. Should be called whenever your
     * app becomes active, typically in the onResume() method of each long-running Activity of your app. Use this method
     * if your application ID is stored in application metadata, otherwise see
     * {@link AppEventsLogger#activateApp(android.content.Context, String)}.
     *
     * @param context Used to access the applicationId and the attributionId for non-authenticated users.
     */
    public static void activateApp(final Context context) {

        activateApp(context, Utility.getMetadataApplicationId(context));
    }

    /**
     * Notifies the events system that the app has launched & logs an activatedApp event. Should be called whenever your
     * app becomes active, typically in the onResume() method of each long-running Activity of your app.
     *
     * @param context Used to access the attributionId for non-authenticated users.
     * @param applicationId The specific applicationId to report the activation for.
     */
    @SuppressWarnings("deprecation")
    public static void activateApp(final Context context, final String applicationId) {

        if ((context == null) || (applicationId == null)) {
            throw new IllegalArgumentException("Both context and applicationId must be non-null");
        }

        // activateApp supercedes publishInstall in the public API, so we need to explicitly invoke it, since the server
        // can't reliably infer install state for all conditions of an app activate.
        Settings.publishInstallAsync(context, applicationId);

        final AppEventsLogger logger = new AppEventsLogger(context, applicationId, null);
        logger.logEvent(AppEventsConstants.EVENT_NAME_ACTIVATED_APP);
    }

    /**
     * Access the behavior that AppEventsLogger uses to determine when to flush logged events to the server. This
     * setting applies to all instances of AppEventsLogger.
     *
     * @return specified flush behavior.
     */
    public static FlushBehavior getFlushBehavior() {

        synchronized (staticLock) {
            return flushBehavior;
        }
    }

    /**
     * Build an AppEventsLogger instance to log events through. The Facebook app that these events are targeted at comes
     * from this application's metadata. The application ID used to log events will be determined from the app ID
     * specified in the package metadata.
     *
     * @param context Used to access the applicationId and the attributionId for non-authenticated users.
     * @return AppEventsLogger instance to invoke log* methods on.
     */
    public static AppEventsLogger newLogger(final Context context) {

        return new AppEventsLogger(context, null, null);
    }

    /**
     * Build an AppEventsLogger instance to log events through.
     *
     * @param context Used to access the attributionId for non-authenticated users.
     * @param session Explicitly specified Session to log events against. If null, the activeSession will be used if
     *            it's open, otherwise the logging will happen against the default app ID specified via the app ID
     *            specified in the package metadata.
     * @return AppEventsLogger instance to invoke log* methods on.
     */
    public static AppEventsLogger newLogger(final Context context, final Session session) {

        return new AppEventsLogger(context, null, session);
    }

    /**
     * Build an AppEventsLogger instance to log events that are attributed to the application but not to any particular
     * Session.
     *
     * @param context Used to access the attributionId for non-authenticated users.
     * @param applicationId Explicitly specified Facebook applicationId to log events against. If null, the default app
     *            ID specified in the package metadata will be used.
     * @return AppEventsLogger instance to invoke log* methods on.
     */
    public static AppEventsLogger newLogger(final Context context, final String applicationId) {

        return new AppEventsLogger(context, applicationId, null);
    }

    /**
     * Build an AppEventsLogger instance to log events through.
     *
     * @param context Used to access the attributionId for non-authenticated users.
     * @param applicationId Explicitly specified Facebook applicationId to log events against. If null, the default app
     *            ID specified in the package metadata will be used.
     * @param session Explicitly specified Session to log events against. If null, the activeSession will be used if
     *            it's open, otherwise the logging will happen against the specified app ID.
     * @return AppEventsLogger instance to invoke log* methods on.
     */
    public static AppEventsLogger newLogger(final Context context, final String applicationId, final Session session) {

        return new AppEventsLogger(context, applicationId, session);
    }

    /**
     * Call this when the consuming Activity/Fragment receives an onStop() callback in order to persist any outstanding
     * events to disk, so they may be flushed at a later time. The next flush (explicit or not) will check for any
     * outstanding events and, if present, include them in that flush. Note that this call may trigger an I/O operation
     * on the calling thread. Explicit use of this method is not necessary if the consumer is making use of
     * {@link UiLifecycleHelper}, which will take care of making the call in its own onStop() callback.
     */
    public static void onContextStop() {

        PersistedEvents.persistEvents(applicationContext, stateMap);
    }

    /**
     * Set the behavior that this AppEventsLogger uses to determine when to flush logged events to the server. This
     * setting applies to all instances of AppEventsLogger.
     *
     * @param flushBehavior the desired behavior.
     */
    public static void setFlushBehavior(final FlushBehavior flushBehavior) {

        synchronized (staticLock) {
            AppEventsLogger.flushBehavior = flushBehavior;
        }
    }

    private static int accumulatePersistedEvents() {

        final PersistedEvents persistedEvents = PersistedEvents.readAndClearStore(applicationContext);

        int result = 0;
        for (final AccessTokenAppIdPair accessTokenAppId : persistedEvents.keySet()) {
            final SessionEventsState sessionEventsState = getSessionEventsState(applicationContext, accessTokenAppId);

            final List<AppEvent> events = persistedEvents.getEvents(accessTokenAppId);
            sessionEventsState.accumulatePersistedEvents(events);
            result += events.size();
        }

        return result;
    }

    private static FlushStatistics buildAndExecuteRequests(final FlushReason reason,
            final Set<AccessTokenAppIdPair> keysToFlush) {

        final FlushStatistics flushResults = new FlushStatistics();

        final boolean limitEventUsage = Settings.getLimitEventAndDataUsage(applicationContext);

        final List<Request> requestsToExecute = new ArrayList<Request>();
        for (final AccessTokenAppIdPair accessTokenAppId : keysToFlush) {
            final SessionEventsState sessionEventsState = getSessionEventsState(accessTokenAppId);
            if (sessionEventsState == null) {
                continue;
            }

            final Request request = buildRequestForSession(accessTokenAppId, sessionEventsState, limitEventUsage,
                    flushResults);
            if (request != null) {
                requestsToExecute.add(request);
            }
        }

        if (requestsToExecute.size() > 0) {
            Logger.log(LoggingBehavior.APP_EVENTS, TAG, "Flushing %d events due to %s.",
                    Integer.valueOf(flushResults.numEvents), reason.toString());

            for (final Request request : requestsToExecute) {
                // Execute the request synchronously. Callbacks will take care of handling errors and updating
                // our final overall result.
                request.executeAndWait();
            }
            return flushResults;
        }

        return null;
    }

    private static Request buildRequestForSession(final AccessTokenAppIdPair accessTokenAppId,
            final SessionEventsState sessionEventsState, final boolean limitEventUsage, final FlushStatistics flushState) {

        final String applicationId = accessTokenAppId.getApplicationId();

        final Utility.FetchedAppSettings fetchedAppSettings = Utility.queryAppSettings(applicationId, false);

        final Request postRequest = Request.newPostRequest(null, String.format("%s/activities", applicationId), null,
                null);

        Bundle requestParameters = postRequest.getParameters();
        if (requestParameters == null) {
            requestParameters = new Bundle();
        }
        requestParameters.putString("access_token", accessTokenAppId.getAccessToken());
        postRequest.setParameters(requestParameters);

        final int numEvents = sessionEventsState
                .populateRequest(postRequest, fetchedAppSettings.supportsImplicitLogging(),
                        fetchedAppSettings.supportsAttribution(), limitEventUsage);
        if (numEvents == 0) {
            return null;
        }

        flushState.numEvents += numEvents;

        postRequest.setCallback(new Request.Callback() {

            @Override
            public void onCompleted(final Response response) {

                handleResponse(accessTokenAppId, postRequest, response, sessionEventsState, flushState);
            }
        });

        return postRequest;
    }

    private static void flush(final FlushReason reason) {

        Settings.getExecutor().execute(new Runnable() {

            @Override
            public void run() {

                flushAndWait(reason);
            }
        });
    }

    private static void flushIfNecessary() {

        synchronized (staticLock) {
            if (getFlushBehavior() != FlushBehavior.EXPLICIT_ONLY) {
                if (getAccumulatedEventCount() > NUM_LOG_EVENTS_TO_TRY_TO_FLUSH_AFTER) {
                    flush(FlushReason.EVENT_THRESHOLD);
                }
            }
        }
    }

    private static int getAccumulatedEventCount() {

        synchronized (staticLock) {

            int result = 0;
            for (final SessionEventsState state : stateMap.values()) {
                result += state.getAccumulatedEventCount();
            }
            return result;
        }
    }

    private static SessionEventsState getSessionEventsState(final AccessTokenAppIdPair accessTokenAppId) {

        synchronized (staticLock) {
            return stateMap.get(accessTokenAppId);
        }
    }

    // Creates a new SessionEventsState if not already in the map.
    private static SessionEventsState getSessionEventsState(final Context context,
            final AccessTokenAppIdPair accessTokenAppId) {

        synchronized (staticLock) {
            SessionEventsState state = stateMap.get(accessTokenAppId);
            if (state == null) {
                // Retrieve attributionId, but we will only send it if attribution is supported for the app.
                final AttributionIdentifiers attributionIdentifiers = AttributionIdentifiers
                        .getAttributionIdentifiers(context);

                state = new SessionEventsState(attributionIdentifiers, context.getPackageName(), hashedDeviceAndAppId);
                stateMap.put(accessTokenAppId, state);
            }
            return state;
        }
    }

    private static void initializeTimersIfNeeded() {

        synchronized (staticLock) {
            if (flushTimer != null) {
                return;
            }
            flushTimer = new Timer();
            supportsAttributionRecheckTimer = new Timer();
        }

        flushTimer.schedule(new TimerTask() {

            @Override
            public void run() {

                if (getFlushBehavior() != FlushBehavior.EXPLICIT_ONLY) {
                    flushAndWait(FlushReason.TIMER);
                }
            }
        }, 0, // start immediately
                FLUSH_PERIOD_IN_SECONDS * 1000);

        supportsAttributionRecheckTimer.schedule(new TimerTask() {

            @Override
            public void run() {

                final Set<String> applicationIds = new HashSet<String>();
                synchronized (staticLock) {
                    for (final AccessTokenAppIdPair accessTokenAppId : stateMap.keySet()) {
                        applicationIds.add(accessTokenAppId.getApplicationId());
                    }
                }
                for (final String applicationId : applicationIds) {
                    Utility.queryAppSettings(applicationId, true);
                }
            }
        }, 0, // start immediately
                APP_SUPPORTS_ATTRIBUTION_ID_RECHECK_PERIOD_IN_SECONDS * 1000);
    }

    private static void logEvent(final Context context, final AppEvent event,
            final AccessTokenAppIdPair accessTokenAppId) {

        if (shouldSuppressEvent(event)) {
            return;
        }

        final SessionEventsState state = getSessionEventsState(context, accessTokenAppId);
        state.addEvent(event);

        flushIfNecessary();
    }

    /**
     * Invoke this method, rather than throwing an Exception, for situations where user/server input might reasonably
     * cause this to occur, and thus don't want an exception thrown at production time, but do want logging
     * notification.
     */
    private static void notifyDeveloperError(final String message) {

        Logger.log(LoggingBehavior.DEVELOPER_ERRORS, "AppEvents", message);
    }

    // This will also update the timestamp based on specified behavior.
    private static boolean shouldSuppressEvent(final AppEvent event) {

        final EventSuppression suppressionInfo = mapEventNameToSuppress.get(event.getName());
        if (suppressionInfo == null) {
            return false;
        }

        final Date timestamp = mapEventsToSuppressionTime.get(event.getName());
        boolean suppressed;
        if (timestamp == null) {
            suppressed = false;
        } else {
            final long delta = new Date().getTime() - timestamp.getTime();
            suppressed = delta < (suppressionInfo.getTimeoutPeriod() * 1000);
        }

        // Update the time if we're not suppressed, OR if we are suppressed but the behavior is to reset even on
        // suppressed events.
        if (!suppressed
                || (suppressionInfo.getBehavior() == SuppressionTimeoutBehavior.RESET_TIMEOUT_WHEN_LOG_ATTEMPTED)) {
            mapEventsToSuppressionTime.put(event.getName(), new Date());
        }

        return suppressed;
    }

    static void eagerFlush() {

        if (getFlushBehavior() != FlushBehavior.EXPLICIT_ONLY) {
            flush(FlushReason.EAGER_FLUSHING_EVENT);
        }
    }

    //
    // Private implementation
    //

    static void flushAndWait(final FlushReason reason) {

        Set<AccessTokenAppIdPair> keysToFlush;
        synchronized (staticLock) {
            if (requestInFlight) {
                return;
            }
            requestInFlight = true;
            keysToFlush = new HashSet<AccessTokenAppIdPair>(stateMap.keySet());
        }

        accumulatePersistedEvents();

        FlushStatistics flushResults = null;
        try {
            flushResults = buildAndExecuteRequests(reason, keysToFlush);
        } catch (final Exception e) {
            Log.d(TAG, "Caught unexpected exception while flushing: " + e.toString());
        }

        synchronized (staticLock) {
            requestInFlight = false;
        }

        if (flushResults != null) {
            final Intent intent = new Intent(ACTION_APP_EVENTS_FLUSHED);
            intent.putExtra(APP_EVENTS_EXTRA_NUM_EVENTS_FLUSHED, flushResults.numEvents);
            intent.putExtra(APP_EVENTS_EXTRA_FLUSH_RESULT, flushResults.result);
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent);
        }
    }

    static void handleResponse(final AccessTokenAppIdPair accessTokenAppId, final Request request,
            final Response response, final SessionEventsState sessionEventsState, final FlushStatistics flushState) {

        final FacebookRequestError error = response.getError();
        String resultDescription = "Success";

        FlushResult flushResult = FlushResult.SUCCESS;

        if (error != null) {
            final int NO_CONNECTIVITY_ERROR_CODE = -1;
            if (error.getErrorCode() == NO_CONNECTIVITY_ERROR_CODE) {
                resultDescription = "Failed: No Connectivity";
                flushResult = FlushResult.NO_CONNECTIVITY;
            } else {
                resultDescription = String.format("Failed:\n  Response: %s\n  Error %s", response.toString(),
                        error.toString());
                flushResult = FlushResult.SERVER_ERROR;
            }
        }

        if (Settings.isLoggingBehaviorEnabled(LoggingBehavior.APP_EVENTS)) {
            final String eventsJsonString = (String) request.getTag();
            String prettyPrintedEvents;

            try {
                final JSONArray jsonArray = new JSONArray(eventsJsonString);
                prettyPrintedEvents = jsonArray.toString(2);
            } catch (final JSONException exc) {
                prettyPrintedEvents = "<Can't encode events for debug logging>";
            }

            Logger.log(LoggingBehavior.APP_EVENTS, TAG, "Flush completed\nParams: %s\n  Result: %s\n  Events JSON: %s",
                    request.getGraphObject().toString(), resultDescription, prettyPrintedEvents);
        }

        sessionEventsState.clearInFlightAndStats(error != null);

        if (flushResult == FlushResult.NO_CONNECTIVITY) {
            // We may call this for multiple requests in a batch, which is slightly inefficient since in principle
            // we could call it once for all failed requests, but the impact is likely to be minimal.
            // We don't call this for other server errors, because if an event failed because it was malformed, etc.,
            // continually retrying it will cause subsequent events to not be logged either.
            PersistedEvents.persistEvents(applicationContext, accessTokenAppId, sessionEventsState);
        }

        if (flushResult != FlushResult.SUCCESS) {
            // We assume that connectivity issues are more significant to report than server issues.
            if (flushState.result != FlushResult.NO_CONNECTIVITY) {
                flushState.result = flushResult;
            }
        }
    }

    /**
     * Explicitly flush any stored events to the server. Implicit flushes may happen depending on the value of
     * getFlushBehavior. This method allows for explicit, app invoked flushing.
     */
    public void flush() {

        flush(FlushReason.EXPLICIT);
    }

    /**
     * Returns the app ID this logger was configured to log to.
     *
     * @return the Facebook app ID
     */
    public String getApplicationId() {

        return this.accessTokenAppId.getApplicationId();
    }

    /**
     * Log an app event with the specified name.
     *
     * @param eventName eventName used to denote the event. Choose amongst the EVENT_NAME_* constants in
     *            {@link AppEventsConstants} when possible. Or create your own if none of the EVENT_NAME_* constants are
     *            applicable. Event names should be 40 characters or less, alphanumeric, and can include spaces,
     *            underscores or hyphens, but mustn't have a space or hyphen as the first character. Any given app
     *            should have no more than ~300 distinct event names.
     */
    public void logEvent(final String eventName) {

        this.logEvent(eventName, null);
    }

    /**
     * Log an app event with the specified name and set of parameters.
     *
     * @param eventName eventName used to denote the event. Choose amongst the EVENT_NAME_* constants in
     *            {@link AppEventsConstants} when possible. Or create your own if none of the EVENT_NAME_* constants are
     *            applicable. Event names should be 40 characters or less, alphanumeric, and can include spaces,
     *            underscores or hyphens, but mustn't have a space or hyphen as the first character. Any given app
     *            should have no more than ~300 distinct event names.
     * @param parameters A Bundle of parameters to log with the event. Insights will allow looking at the logs of these
     *            events via different parameter values. You can log on the order of 10 parameters with each distinct
     *            eventName. It's advisable to keep the number of unique values provided for each parameter in the, at
     *            most, thousands. As an example, don't attempt to provide a unique parameter value for each unique user
     *            in your app. You won't get meaningful aggregate reporting on so many parameter values. The values in
     *            the bundles should be Strings or numeric values.
     */
    public void logEvent(final String eventName, final Bundle parameters) {

        this.logEvent(eventName, null, parameters, false);
    }

    /**
     * Log an app event with the specified name and the supplied value.
     *
     * @param eventName eventName used to denote the event. Choose amongst the EVENT_NAME_* constants in
     *            {@link AppEventsConstants} when possible. Or create your own if none of the EVENT_NAME_* constants are
     *            applicable. Event names should be 40 characters or less, alphanumeric, and can include spaces,
     *            underscores or hyphens, but mustn't have a space or hyphen as the first character. Any given app
     *            should have no more than ~300 distinct event names. * @param eventName
     * @param valueToSum a value to associate with the event which will be summed up in Insights for across all
     *            instances of the event, so that average values can be determined, etc.
     */
    public void logEvent(final String eventName, final double valueToSum) {

        this.logEvent(eventName, valueToSum, null);
    }

    /**
     * Log an app event with the specified name, supplied value, and set of parameters.
     *
     * @param eventName eventName used to denote the event. Choose amongst the EVENT_NAME_* constants in
     *            {@link AppEventsConstants} when possible. Or create your own if none of the EVENT_NAME_* constants are
     *            applicable. Event names should be 40 characters or less, alphanumeric, and can include spaces,
     *            underscores or hyphens, but mustn't have a space or hyphen as the first character. Any given app
     *            should have no more than ~300 distinct event names.
     * @param valueToSum a value to associate with the event which will be summed up in Insights for across all
     *            instances of the event, so that average values can be determined, etc.
     * @param parameters A Bundle of parameters to log with the event. Insights will allow looking at the logs of these
     *            events via different parameter values. You can log on the order of 10 parameters with each distinct
     *            eventName. It's advisable to keep the number of unique values provided for each parameter in the, at
     *            most, thousands. As an example, don't attempt to provide a unique parameter value for each unique user
     *            in your app. You won't get meaningful aggregate reporting on so many parameter values. The values in
     *            the bundles should be Strings or numeric values.
     */
    public void logEvent(final String eventName, final double valueToSum, final Bundle parameters) {

        this.logEvent(eventName, Double.valueOf(valueToSum), parameters, false);
    }

    /**
     * Logs a purchase event with Facebook, in the specified amount and with the specified currency.
     *
     * @param purchaseAmount Amount of purchase, in the currency specified by the 'currency' parameter. This value will
     *            be rounded to the thousandths place (e.g., 12.34567 becomes 12.346).
     * @param currency Currency used to specify the amount.
     */
    public void logPurchase(final BigDecimal purchaseAmount, final Currency currency) {

        this.logPurchase(purchaseAmount, currency, null);
    }

    /**
     * Logs a purchase event with Facebook, in the specified amount and with the specified currency. Additional detail
     * about the purchase can be passed in through the parameters bundle.
     *
     * @param purchaseAmount Amount of purchase, in the currency specified by the 'currency' parameter. This value will
     *            be rounded to the thousandths place (e.g., 12.34567 becomes 12.346).
     * @param currency Currency used to specify the amount.
     * @param parameters Arbitrary additional information for describing this event. Should have no more than 10
     *            entries, and keys should be mostly consistent from one purchase event to the next.
     */
    public void logPurchase(final BigDecimal purchaseAmount, final Currency currency, Bundle parameters) {

        if (purchaseAmount == null) {
            notifyDeveloperError("purchaseAmount cannot be null");
            return;
        } else if (currency == null) {
            notifyDeveloperError("currency cannot be null");
            return;
        }

        if (parameters == null) {
            parameters = new Bundle();
        }
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currency.getCurrencyCode());

        this.logEvent(AppEventsConstants.EVENT_NAME_PURCHASED, purchaseAmount.doubleValue(), parameters);
        eagerFlush();
    }

    /**
     * This method is intended only for internal use by the Facebook SDK and other use is unsupported.
     */
    public void logSdkEvent(final String eventName, final Double valueToSum, final Bundle parameters) {

        this.logEvent(eventName, valueToSum, parameters, true);
    }

    private void logEvent(final String eventName, final Double valueToSum, final Bundle parameters,
            final boolean isImplicitlyLogged) {

        final AppEvent event = new AppEvent(eventName, valueToSum, parameters, isImplicitlyLogged);
        logEvent(this.context, event, this.accessTokenAppId);
    }

    boolean isValidForSession(final Session session) {

        final AccessTokenAppIdPair other = new AccessTokenAppIdPair(session);
        return this.accessTokenAppId.equals(other);
    }

    /**
     * Controls when an AppEventsLogger sends log events to the server
     */
    public enum FlushBehavior {
        /**
         * Flush automatically: periodically (once a minute or after every 100 events), and always at app reactivation.
         * This is the default value.
         */
        AUTO,

        /**
         * Only flush when AppEventsLogger.flush() is explicitly invoked.
         */
        EXPLICIT_ONLY,
    }

    // Rather than retaining Sessions, we extract the information we need and track app events by
    // application ID and access token (which may be null for Session-less calls). This avoids needing to
    // worry about Session lifecycle and also allows us to coalesce app events from different Sessions
    // that have the same access token/app ID.
    private static class AccessTokenAppIdPair implements Serializable {

        private static final long serialVersionUID = 1L;
        private final String accessToken;
        private final String applicationId;

        AccessTokenAppIdPair(final Session session) {

            this(session.getAccessToken(), session.getApplicationId());
        }

        AccessTokenAppIdPair(final String accessToken, final String applicationId) {

            this.accessToken = Utility.isNullOrEmpty(accessToken) ? null : accessToken;
            this.applicationId = applicationId;
        }

        @Override
        public boolean equals(final Object o) {

            if (!(o instanceof AccessTokenAppIdPair)) {
                return false;
            }
            final AccessTokenAppIdPair p = (AccessTokenAppIdPair) o;
            return Utility.areObjectsEqual(p.accessToken, this.accessToken)
                    && Utility.areObjectsEqual(p.applicationId, this.applicationId);
        }

        @Override
        public int hashCode() {

            return (this.accessToken == null ? 0 : this.accessToken.hashCode())
                    ^ (this.applicationId == null ? 0 : this.applicationId.hashCode());
        }

        private Object writeReplace() {

            return new SerializationProxyV1(this.accessToken, this.applicationId);
        }

        String getAccessToken() {

            return this.accessToken;
        }

        String getApplicationId() {

            return this.applicationId;
        }

        private static class SerializationProxyV1 implements Serializable {

            private static final long serialVersionUID = -2488473066578201069L;
            private final String accessToken;
            private final String appId;

            SerializationProxyV1(final String accessToken, final String appId) {

                this.accessToken = accessToken;
                this.appId = appId;
            }

            private Object readResolve() {

                return new AccessTokenAppIdPair(this.accessToken, this.appId);
            }
        }
    }

    private static class EventSuppression {

        // Timeout period in seconds
        private final int timeoutPeriod;
        private final SuppressionTimeoutBehavior behavior;

        EventSuppression(final int timeoutPeriod, final SuppressionTimeoutBehavior behavior) {

            this.timeoutPeriod = timeoutPeriod;
            this.behavior = behavior;
        }

        SuppressionTimeoutBehavior getBehavior() {

            return this.behavior;
        }

        int getTimeoutPeriod() {

            return this.timeoutPeriod;
        }
    }

    private enum FlushReason {
        EXPLICIT, TIMER, SESSION_CHANGE, PERSISTED_EVENTS, EVENT_THRESHOLD, EAGER_FLUSHING_EVENT,
    }

    private enum FlushResult {
        SUCCESS, SERVER_ERROR, NO_CONNECTIVITY, UNKNOWN_ERROR
    }

    private static class FlushStatistics {

        public int numEvents;
        public FlushResult result = FlushResult.SUCCESS;
    }

    private enum SuppressionTimeoutBehavior {
        // Successfully logging an event will reset the timeout period (i.e., events will log no more than every N
        // seconds).
        RESET_TIMEOUT_WHEN_LOG_SUCCESSFUL,
        // Attempting to log an event, even if it is suppressed, will reset the timeout period (i.e., events will not
        // be logged until they have been "silent" for at least N seconds).
        RESET_TIMEOUT_WHEN_LOG_ATTEMPTED,
    }

    //
    // Deprecated Stuff
    //

    static class AppEvent implements Serializable {

        private static final String JSON_ENCODING_FOR_APP_EVENT_FAILED_S = "JSON encoding for app event failed: '%s'";

        private static final String APP_EVENTS = "AppEvents";

        private static final String _EVENT_NAME = "_eventName";

        private static final String S_IMPLICIT_B_JSON_S = "\"%s\", implicit: %b, json: %s";

        private static final long serialVersionUID = 1L;

        private JSONObject jsonObject;
        private final boolean isImplicit;
        private static final HashSet<String> validatedIdentifiers = new HashSet<String>();
        private String name;

        public AppEvent(final String eventName, final Double valueToSum, final Bundle parameters,
                final boolean isImplicitlyLogged) {

            this.validateIdentifier(eventName);

            this.name = eventName;

            this.isImplicit = isImplicitlyLogged;
            this.jsonObject = new JSONObject();

            try {

                this.jsonObject.put(_EVENT_NAME, eventName);
                this.jsonObject.put("_logTime", System.currentTimeMillis() / 1000);

                if (valueToSum != null) {
                    this.jsonObject.put("_valueToSum", valueToSum.doubleValue());
                }

                if (this.isImplicit) {
                    this.jsonObject.put("_implicitlyLogged", "1");
                }

                final String appVersion = Settings.getAppVersion();
                if (appVersion != null) {
                    this.jsonObject.put("_appVersion", appVersion);
                }

                if (parameters != null) {
                    for (final String key : parameters.keySet()) {

                        this.validateIdentifier(key);

                        final Object value = parameters.get(key);
                        if (!(value instanceof String) && !(value instanceof Number)) {
                            throw new FacebookException(String.format(
                                    "Parameter value '%s' for key '%s' should be a string or a numeric type.", value,
                                    key));
                        }

                        this.jsonObject.put(key, value.toString());
                    }
                }

                if (!this.isImplicit) {
                    Logger.log(LoggingBehavior.APP_EVENTS, APP_EVENTS, "Created app event '%s'",
                            this.jsonObject.toString());
                }
            } catch (final JSONException jsonException) {

                // If any of the above failed, just consider this an illegal event.
                Logger.log(LoggingBehavior.APP_EVENTS, APP_EVENTS, JSON_ENCODING_FOR_APP_EVENT_FAILED_S,
                        jsonException.toString());
                this.jsonObject = null;

            }
        }

        protected AppEvent(final String jsonString, final boolean isImplicit) throws JSONException {

            this.jsonObject = new JSONObject(jsonString);
            this.isImplicit = isImplicit;
        }

        public boolean getIsImplicit() {

            return this.isImplicit;
        }

        public JSONObject getJSONObject() {

            return this.jsonObject;
        }

        public String getName() {

            return this.name;
        }

        @Override
        public String toString() {

            return String.format(S_IMPLICIT_B_JSON_S, this.jsonObject.optString(_EVENT_NAME),
                    Boolean.valueOf(this.isImplicit), this.jsonObject.toString());
        }

        // throw exception if not valid.
        private void validateIdentifier(String identifier) {

            // Identifier should be 40 chars or less, and only have 0-9A-Za-z, underscore, hyphen, and space (but no
            // hyphen or space in the first position).
            final String regex = "^[0-9a-zA-Z_]+[0-9a-zA-Z _-]*$";

            final int MAX_IDENTIFIER_LENGTH = 40;
            if ((identifier == null) || (identifier.length() == 0) || (identifier.length() > MAX_IDENTIFIER_LENGTH)) {
                if (identifier == null) {
                    identifier = "<None Provided>";
                }
                throw new FacebookException(String.format("Identifier '%s' must be less than %d characters",
                        identifier, Integer.valueOf(MAX_IDENTIFIER_LENGTH)));
            }

            boolean alreadyValidated = false;
            synchronized (validatedIdentifiers) {
                alreadyValidated = validatedIdentifiers.contains(identifier);
            }

            if (!alreadyValidated) {
                if (identifier.matches(regex)) {
                    synchronized (validatedIdentifiers) {
                        validatedIdentifiers.add(identifier);
                    }
                } else {
                    throw new FacebookException(String.format(
                            "Skipping event named '%s' due to illegal name - must be under 40 chars "
                                    + "and alphanumeric, _, - or space, and not start with a space or hyphen.",
                            identifier));
                }
            }

        }

        private Object writeReplace() {

            return new SerializationProxyV1(this.jsonObject.toString(), this.isImplicit);
        }

        private static class SerializationProxyV1 implements Serializable {

            private static final long serialVersionUID = -2488473066578201069L;
            private final String jsonString;
            private final boolean isImplicit;

            SerializationProxyV1(final String jsonString, final boolean isImplicit) {

                this.jsonString = jsonString;
                this.isImplicit = isImplicit;
            }

            private Object readResolve() throws JSONException {

                return new AppEvent(this.jsonString, this.isImplicit);
            }
        }
    }

    // Read/write operations are thread-safe/atomic across all instances of PersistedEvents, but modifications
    // to any individual instance are not thread-safe.
    static class PersistedEvents {

        static final String PERSISTED_EVENTS_FILENAME = "AppEventsLogger.persistedevents";

        private static Object staticLock = new Object();

        private final Context context;
        private Map<AccessTokenAppIdPair, List<AppEvent>> persistedEvents = new HashMap<AccessTokenAppIdPair, List<AppEvent>>();

        private PersistedEvents(final Context context) {

            this.context = context;
        }

        public static void persistEvents(final Context context, final AccessTokenAppIdPair accessTokenAppId,
                final SessionEventsState eventsToPersist) {

            final Map<AccessTokenAppIdPair, SessionEventsState> map = new HashMap<AccessTokenAppIdPair, SessionEventsState>();
            map.put(accessTokenAppId, eventsToPersist);
            persistEvents(context, map);
        }

        public static void persistEvents(final Context context,
                final Map<AccessTokenAppIdPair, SessionEventsState> eventsToPersist) {

            synchronized (staticLock) {
                // Note that we don't track which instance of AppEventsLogger added a particular event to
                // SessionEventsState; when a particular Context is being destroyed, we'll persist all accumulated
                // events. More sophisticated tracking could be done to try to reduce unnecessary persisting of events,
                // but the overall number of events is not expected to be large.
                final PersistedEvents persistedEvents = readAndClearStore(context);

                for (final Map.Entry<AccessTokenAppIdPair, SessionEventsState> entry : eventsToPersist.entrySet()) {
                    final List<AppEvent> events = entry.getValue().getEventsToPersist();
                    if (events.size() == 0) {
                        continue;
                    }

                    persistedEvents.addEvents(entry.getKey(), events);
                }

                persistedEvents.write();
            }
        }

        public static PersistedEvents readAndClearStore(final Context context) {

            synchronized (staticLock) {
                final PersistedEvents persistedEvents = new PersistedEvents(context);

                persistedEvents.readAndClearStore();

                return persistedEvents;
            }
        }

        public void addEvents(final AccessTokenAppIdPair accessTokenAppId, final List<AppEvent> eventsToPersist) {

            if (!this.persistedEvents.containsKey(accessTokenAppId)) {
                this.persistedEvents.put(accessTokenAppId, new ArrayList<AppEvent>());
            }
            this.persistedEvents.get(accessTokenAppId).addAll(eventsToPersist);
        }

        public List<AppEvent> getEvents(final AccessTokenAppIdPair accessTokenAppId) {

            return this.persistedEvents.get(accessTokenAppId);
        }

        public Set<AccessTokenAppIdPair> keySet() {

            return this.persistedEvents.keySet();
        }

        private void readAndClearStore() {

            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new BufferedInputStream(
                        this.context.openFileInput(PERSISTED_EVENTS_FILENAME)));

                @SuppressWarnings("unchecked")
                final HashMap<AccessTokenAppIdPair, List<AppEvent>> obj = (HashMap<AccessTokenAppIdPair, List<AppEvent>>) ois
                        .readObject();

                // Note: We delete the store before we store the events; this means we'd prefer to lose some
                // events in the case of exception rather than potentially log them twice.
                this.context.getFileStreamPath(PERSISTED_EVENTS_FILENAME).delete();
                this.persistedEvents = obj;
            } catch (final FileNotFoundException e) {
                // Expected if we never persisted any events.
            } catch (final Exception e) {
                Log.d(TAG, "Got unexpected exception: " + e.toString());
            } finally {
                Utility.closeQuietly(ois);
            }
        }

        private void write() {

            ObjectOutputStream oos = null;
            try {
                oos = new ObjectOutputStream(new BufferedOutputStream(this.context.openFileOutput(
                        PERSISTED_EVENTS_FILENAME, 0)));
                oos.writeObject(this.persistedEvents);
            } catch (final Exception e) {
                Log.d(TAG, "Got unexpected exception: " + e.toString());
            } finally {
                Utility.closeQuietly(oos);
            }
        }
    }

    static class SessionEventsState {

        private List<AppEvent> accumulatedEvents = new ArrayList<AppEvent>();
        private final List<AppEvent> inFlightEvents = new ArrayList<AppEvent>();
        private int numSkippedEventsDueToFullBuffer;
        private final AttributionIdentifiers attributionIdentifiers;
        private final String packageName;
        private final String hashedDeviceAndAppId;

        public static final String EVENT_COUNT_KEY = "event_count";
        public static final String ENCODED_EVENTS_KEY = "encoded_events";
        public static final String NUM_SKIPPED_KEY = "num_skipped";

        private static final int MAX_ACCUMULATED_LOG_EVENTS = 1000;

        public SessionEventsState(final AttributionIdentifiers identifiers, final String packageName,
                final String hashedDeviceAndAppId) {

            this.attributionIdentifiers = identifiers;
            this.packageName = packageName;
            this.hashedDeviceAndAppId = hashedDeviceAndAppId;
        }

        public synchronized void accumulatePersistedEvents(final List<AppEvent> events) {

            // We won't skip events due to a full buffer, since we already accumulated them once and persisted
            // them. But they will count against the buffer size when further events are accumulated.
            this.accumulatedEvents.addAll(events);
        }

        // Synchronize here and in other methods on this class, because could be coming in from different
        // AppEventsLoggers on different threads pointing at the same session.
        public synchronized void addEvent(final AppEvent event) {

            if ((this.accumulatedEvents.size() + this.inFlightEvents.size()) >= SessionEventsState.MAX_ACCUMULATED_LOG_EVENTS) {
                this.numSkippedEventsDueToFullBuffer++;
            } else {
                this.accumulatedEvents.add(event);
            }
        }

        public synchronized void clearInFlightAndStats(final boolean moveToAccumulated) {

            if (moveToAccumulated) {
                this.accumulatedEvents.addAll(this.inFlightEvents);
            }
            this.inFlightEvents.clear();
            this.numSkippedEventsDueToFullBuffer = 0;
        }

        public synchronized int getAccumulatedEventCount() {

            return this.accumulatedEvents.size();
        }

        public synchronized List<AppEvent> getEventsToPersist() {

            // We will only persist accumulated events, not ones currently in-flight. This means if an in-flight
            // request fails, those requests will not be persisted and thus might be lost if the process terminates
            // while the flush is in progress.
            final List<AppEvent> result = this.accumulatedEvents;
            this.accumulatedEvents = new ArrayList<AppEvent>();
            return result;
        }

        public int populateRequest(final Request request, final boolean includeImplicitEvents,
                final boolean includeAttribution, final boolean limitEventUsage) {

            int numSkipped;
            JSONArray jsonArray;
            synchronized (this) {
                numSkipped = this.numSkippedEventsDueToFullBuffer;

                // move all accumulated events to inFlight.
                this.inFlightEvents.addAll(this.accumulatedEvents);
                this.accumulatedEvents.clear();

                jsonArray = new JSONArray();
                for (final AppEvent event : this.inFlightEvents) {
                    if (includeImplicitEvents || !event.getIsImplicit()) {
                        jsonArray.put(event.getJSONObject());
                    }
                }

                if (jsonArray.length() == 0) {
                    return 0;
                }
            }

            this.populateRequest(request, numSkipped, jsonArray, includeAttribution, limitEventUsage);
            return jsonArray.length();
        }

        private byte[] getStringAsByteArray(final String jsonString) {

            byte[] jsonUtf8 = null;
            try {
                jsonUtf8 = jsonString.getBytes("UTF-8");
            } catch (final UnsupportedEncodingException e) {
                // Utility.logd("Encoding exception: ", e);
            }
            return jsonUtf8;
        }

        private void populateRequest(final Request request, final int numSkipped, final JSONArray events,
                final boolean includeAttribution, final boolean limitEventUsage) {

            final GraphObject publishParams = GraphObject.Factory.create();
            publishParams.setProperty("event", "CUSTOM_APP_EVENTS");

            if (this.numSkippedEventsDueToFullBuffer > 0) {
                publishParams.setProperty("num_skipped_events", Integer.valueOf(numSkipped));
            }

            if (includeAttribution) {
                Utility.setAppEventAttributionParameters(publishParams, this.attributionIdentifiers,
                        this.hashedDeviceAndAppId, limitEventUsage);
            }

            publishParams.setProperty("application_package_name", this.packageName);

            request.setGraphObject(publishParams);

            Bundle requestParameters = request.getParameters();
            if (requestParameters == null) {
                requestParameters = new Bundle();
            }

            final String jsonString = events.toString();
            if (jsonString != null) {
                requestParameters.putByteArray("custom_events_file", this.getStringAsByteArray(jsonString));
                request.setTag(jsonString);
            }
            request.setParameters(requestParameters);
        }
    }
}
