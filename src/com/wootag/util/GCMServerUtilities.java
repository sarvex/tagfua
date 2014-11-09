/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import android.content.Context;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.google.android.gcm.GCMRegistrar;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.Constant;
import com.wootag.R;

public final class GCMServerUtilities {

    private static final String APP_NAME = "appName";
    private static final String IMEI = "imei";
    private static final String REG_ID = "regId";
    private static final String BRACKET_CLOSE = ")";
    private static final String REGISTERING_DEVICE_REG_ID = "registering device (regId = ";
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final int MAX_ATTEMPTS = 5;

    private static final Random RANDOM = new Random();

    private static final Logger LOG = LoggerManager.getLogger();

    private GCMServerUtilities() {

    }

    /**
     * Register this account/device pair within the server.
     */
    public static void register(final Context context, final String appName, final String imei, final String regId) {

        LOG.i(REGISTERING_DEVICE_REG_ID + regId + BRACKET_CLOSE);
        final String serverUrl = Constant.SERVER_URL;

        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(REG_ID, regId));
        params.add(new BasicNameValuePair(IMEI, imei));
        params.add(new BasicNameValuePair(APP_NAME, appName));

        long backoff = BACKOFF_MILLI_SECONDS + RANDOM.nextInt(1000);
        // Once GCM returns a registration id, we need to register on our server
        // As the server might be down, we will retry it a couple
        // times.
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            LOG.d("Attempt #" + i + " to register");
            try {
                final HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 60 * 1000);
                HttpConnectionParams.setSoTimeout(httpParams, 60 * 1000);
                final HttpClient httpClient = new DefaultHttpClient(httpParams);
                final HttpPost request = new HttpPost(serverUrl);
                request.setEntity(new UrlEncodedFormEntity(params));

                httpClient.execute(request);

                GCMRegistrar.setRegisteredOnServer(context, true);
                LOG.d(context.getString(R.string.server_registered));
                return;
            } catch (final IOException e) {
                LOG.e("Failed to register on attempt " + i + ":" + e);
                if (i == MAX_ATTEMPTS) {
                    break;
                }
                try {
                    LOG.d("Sleeping for " + backoff + " ms before retry");
                    Thread.sleep(backoff);
                } catch (final InterruptedException e1) {
                    // Activity finished before we complete - exit.
                    LOG.d("Thread interrupted: abort remaining retries!");
                    Thread.currentThread().interrupt();
                    return;
                }
                // increase backoff exponentially
                backoff *= 2;
            } catch (final Exception e) {
                // Here we are simplifying and retrying on any error; in a real
                // application, it should retry only on unrecoverable errors
                // (like HTTP error code 503).
                LOG.e("Failed to register on attempt " + i + ":" + e);
                if (i == MAX_ATTEMPTS) {
                    break;
                }
                try {
                    LOG.d("Sleeping for " + backoff + " ms before retry");
                    Thread.sleep(backoff);
                } catch (final InterruptedException e1) {
                    // Activity finished before we complete - exit.
                    LOG.d("Thread interrupted: abort remaining retries!");
                    Thread.currentThread().interrupt();
                    return;
                }
                // increase backoff exponentially
                backoff *= 2;
            }
        }
        LOG.d(context.getString(R.string.server_register_error, Integer.valueOf(MAX_ATTEMPTS)));
    }

    /**
     * Unregister this account/device pair within the server.
     */
    public static void unregister(final Context context, final String regId) {

        LOG.i("unregistering device (regId = " + regId + BRACKET_CLOSE);
        final String serverUrl = Constant.SERVER_URL + "/unregister";
        final Map<String, String> params = new HashMap<String, String>();
        params.put(REG_ID, regId);
        try {
            post(serverUrl, params);
            GCMRegistrar.setRegisteredOnServer(context, false);
            final String message = context.getString(R.string.server_unregistered);
            LOG.d(message);
            // Utils.displayMessage(context, message);
        } catch (final IOException e) {
            // At this point the device is unregistered from GCM, but still
            // registered in the server.
            // We could try to unregister again, but it is not necessary:
            // if the server tries to send a message to the device, it will get
            // a "NotRegistered" error message and should unregister the device.
            final String message = context.getString(R.string.server_unregister_error, e.getMessage());
            LOG.d(message);
        }
    }

    /**
     * Issue a POST request to the server.
     *
     * @param endpoint POST address.
     * @param params request parameters.
     * @throws IOException propagated from POST.
     */
    private static void post(final String endpoint, final Map<String, String> params) throws IOException {

        URL url;
        try {
            url = new URL(endpoint);
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        final StringBuilder bodyBuilder = new StringBuilder();
        final Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            final Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=').append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        final String body = bodyBuilder.toString();
        LOG.v("Posting '" + body + "' to " + url);
        final byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        OutputStream out = null;
        try {
            LOG.e("URL", "> " + url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");

            out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // handle the response
            final int status = conn.getResponseCode();
            if (status != 200) {
                throw new IOException("Post failed with error code " + status);
            }
        } catch (final IOException exception) {
            LOG.e(exception);
        } finally {
            if (conn != null) {
                conn.disconnect();
                conn = null;
            }

            if (out != null) {
                try {
                    out.close();
                    out = null;
                } catch (final IOException exception) {
                    LOG.e(exception);
                }
            }
        }
    }
}
