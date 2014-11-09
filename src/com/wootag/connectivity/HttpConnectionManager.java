/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.connectivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.json.JSONException;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class HttpConnectionManager {

    private static final String ACCEPT = "Accept";
    private static final String AMPERSAND = "&";
    private static final String APPLICATION_JSON = "application/json";
    private static final String BLANK = "";
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String EQUALS = "=";
    private static final String HTTP = "http";
    private static final String NO_CACHE = "no-cache";
    private static final String QUESTION = "?";
    private static final String URL_ENCODED = "application/x-www-form-urlencoded";
    private static final int CONNECTION_TIMEOUT = 120 * 1000;
    private static final int HTTP_PORT = 80;
    private static final int MAX_HTTP_CONNECTIONS = 10;
    private static final int SO_TIMEOUT = 120 * 1000;
    private static final String UTF_8 = "UTF-8";

    private static final Logger LOG = LoggerManager.getLogger();

    private static CookieStore cookieJar;

    private final Context context;
    private final HttpClient httpClient;

    private String proxyHost;
    private int proxyPort;

    public HttpConnectionManager(final Context context) {

        this.context = context;
        this.configureProxy();

        // Create and initialize HTTP parameters
        final HttpParams params = new BasicHttpParams();
        ConnManagerParams.setMaxTotalConnections(params, MAX_HTTP_CONNECTIONS);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

        // Create and initialize scheme registry
        final SchemeRegistry schemeRegistry = new SchemeRegistry();
        final Scheme scheme = schemeRegistry
                .register(new Scheme(HTTP, PlainSocketFactory.getSocketFactory(), HTTP_PORT));
        LOG.i("scheme register " + scheme);
        HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, SO_TIMEOUT);
        HttpConnectionParams.setTcpNoDelay(params, true);

        if (this.proxyPort != 0) {
            ConnRouteParams.setDefaultProxy(params, new HttpHost(this.proxyHost, this.proxyPort));
        }

        // Create an HttpClient with the ThreadSafeClientConnManager. This connection manager must be used if more than
        // one thread will be using the HttpClient.
        final ClientConnectionManager collectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);
        this.httpClient = new DefaultHttpClient(collectionManager, params);
    }

    private static CookieStore getCookieJar() {

        if (cookieJar == null) {
            cookieJar = new BasicCookieStore();
        }
        return cookieJar;
    }

    /**
     * Returns HTTP response string .
     *
     * @param url
     * @param header map
     * @param url map
     */
    public String httpGet(String url, final Map<String, String> headerMap, final Map<String, String> urlParamMap) {

        String result = null;
        this.configureProxy();
        if (this.proxyPort != 0) {
            ConnRouteParams.setDefaultProxy(this.httpClient.getParams(), new HttpHost(this.proxyHost, this.proxyPort));
        }

        String urlParams = QUESTION;
        if (urlParamMap != null) {
            for (final Entry<String, String> entry : urlParamMap.entrySet()) {
                try {
                    urlParams += URLEncoder.encode(entry.getKey() == null ? BLANK : entry.getKey(), UTF_8) + EQUALS
                            + URLEncoder.encode(entry.getValue() == null ? BLANK : entry.getValue(), UTF_8) + AMPERSAND;
                } catch (final UnsupportedEncodingException exception) {
                    LOG.e(exception);
                }
            }
        }

        urlParams = urlParams.substring(0, urlParams.length() - 1);
        url += urlParams;
        LOG.i("sending url  :" + url);
        final HttpGet request = new HttpGet(url);
        request.setHeader(CACHE_CONTROL, NO_CACHE);
        if (headerMap != null) {
            try {
                for (final Entry<String, String> entry : headerMap.entrySet()) {
                    request.addHeader(URLEncoder.encode(entry.getKey() == null ? BLANK : entry.getKey(), UTF_8),
                            URLEncoder.encode(entry.getValue() == null ? BLANK : entry.getValue(), UTF_8));
                }
            } catch (final UnsupportedEncodingException exception) {
                LOG.e(exception);
            }
        }

        // Creating a local HTTP context
        final BasicHttpContext localContext = new BasicHttpContext();

        // Bind custom cookie store to the local context
        localContext.setAttribute(ClientContext.COOKIE_STORE, getCookieJar());
        HttpResponse response = null;
        InputStream reader = null;
        try {
            response = this.httpClient.execute(request, localContext);
            final StatusLine status = response.getStatusLine();
            LOG.i("Request returned status: " + status);
            reader = response.getEntity().getContent();
            result = this.getStringFromInputStream(reader);

        } catch (final ClientProtocolException exception) {
            LOG.e(exception);
        } catch (final IOException exception) {
            LOG.e(exception);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    reader = null;
                } catch (final IOException exception) {

                }
            }
        }

        LOG.i("Response: " + result);
        return result;
    }

    /**
     * Returns http reponse string.
     *
     * @param url
     * @param json request
     * @param header map
     * @param url map
     */
    public String httpPOST(String url, final String json, final Map<String, String> headerMap,
            final Map<String, String> urlParamMap) {

        String result = null;
        this.configureProxy();
        if (this.proxyPort != 0) {
            ConnRouteParams.setDefaultProxy(this.httpClient.getParams(), new HttpHost(this.proxyHost, this.proxyPort));
        }
        String urlParams = QUESTION;
        if (urlParamMap != null) {
            try {
                for (final Entry<String, String> entry : urlParamMap.entrySet()) {
                    urlParams += (URLEncoder.encode(entry.getKey() == null ? BLANK : entry.getKey(), UTF_8) + EQUALS
                            + URLEncoder.encode(entry.getValue() == null ? BLANK : entry.getValue(), UTF_8) + AMPERSAND);
                }
            } catch (final UnsupportedEncodingException exception) {
                LOG.e(exception);
            }
        }
        urlParams = urlParams.substring(0, urlParams.length() - 1);
        url += urlParams;

        LOG.i("sending url  :" + url);
        LOG.i("sending json  :" + json);
        final HttpPost request = new HttpPost(url);
        request.setHeader(ACCEPT, APPLICATION_JSON);
        // request.setHeader("Content-Type", "application/jsonrequest");
        request.setHeader(CACHE_CONTROL, NO_CACHE);
        if (headerMap != null) {
            try {
                for (final Entry<String, String> entry : headerMap.entrySet()) {
                    request.addHeader(URLEncoder.encode(entry.getKey() == null ? BLANK : entry.getKey(), UTF_8),
                            URLEncoder.encode(entry.getValue() == null ? BLANK : entry.getValue(), UTF_8));
                }
            } catch (final UnsupportedEncodingException exception) {
                LOG.e(exception);
            }
        }

        final ByteArrayEntity entityByteArrayEntity = new ByteArrayEntity(json.getBytes());
        entityByteArrayEntity.setContentType(APPLICATION_JSON);
        request.setEntity(entityByteArrayEntity);

        // Creating a local HTTP context
        final BasicHttpContext localContext = new BasicHttpContext();
        // Bind custom cookie store to the local context
        localContext.setAttribute(ClientContext.COOKIE_STORE, getCookieJar());

        HttpResponse response = null;
        InputStream reader = null;
        try {
            response = this.httpClient.execute(request, localContext);
            LOG.i("Request returned status: " + response.getStatusLine());
            reader = response.getEntity().getContent();

            result = this.getStringFromInputStream(reader);
            LOG.i("Response: " + result);
        } catch (final ClientProtocolException exception) {
            LOG.e(exception);
        } catch (final IOException exception) {
            LOG.e(exception);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    reader = null;
                } catch (final IOException exception) {
                    LOG.e(exception);
                }
            }
        }

        return result;
    }

    /**
     * Returns published video id .
     *
     * @param url
     * @param buffer object to post
     * @param header map
     * @param url map
     * @throws JSONException
     */
    public long httpPOSTData(String url, final byte[] buffer, final Map<String, String> headerMap,
            final Map<String, String> urlParamMap) throws JSONException {

        long videoId = 0;
        this.configureProxy();
        if (this.proxyPort != 0) {
            ConnRouteParams.setDefaultProxy(this.httpClient.getParams(), new HttpHost(this.proxyHost, this.proxyPort));
        }

        String urlParams = QUESTION;
        if (urlParamMap != null) {
            try {
                for (final Entry<String, String> entry : urlParamMap.entrySet()) {
                    urlParams += (URLEncoder.encode(entry.getKey() == null ? BLANK : entry.getKey(), UTF_8) + EQUALS
                            + URLEncoder.encode(entry.getValue() == null ? BLANK : entry.getValue(), UTF_8) + AMPERSAND);
                }
            } catch (final UnsupportedEncodingException exception) {
                LOG.e(exception);
            }
        }
        urlParams = urlParams.substring(0, urlParams.length() - 1);
        url += urlParams;
        LOG.i("sending url  :" + url);
        final HttpPost request = new HttpPost(url);
        final ByteArrayEntity entityByteArrayEntity = new ByteArrayEntity(buffer);
        request.setEntity(entityByteArrayEntity);
        request.setHeader(CONTENT_TYPE, URL_ENCODED);// application/x-www-form-urlencoded

        // Creating a local HTTP context
        final BasicHttpContext localContext = new BasicHttpContext();
        // Bind custom cookie store to the local context
        localContext.setAttribute(ClientContext.COOKIE_STORE, getCookieJar());

        String result = null;
        InputStream reader = null;
        try {
            final HttpResponse response = this.httpClient.execute(request, localContext);
            LOG.i("Request returned status: " + response.getStatusLine());

            reader = response.getEntity().getContent();
            result = this.getStringFromInputStream(reader);
            LOG.i("Response: " + result);

        } catch (final ClientProtocolException exception) {
            LOG.e(exception);
        } catch (final IOException exception) {
            LOG.e(exception);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    reader = null;

                } catch (final IOException exception) {
                    LOG.e(exception);
                }
            }
        }

        videoId = Parser.parseResponseJson(result);
        return videoId;
    }

    private void configureProxy() {

        // ANDROID BUG 24227 4.2 Regression
        // final ApnSettings apnSettings = new ApnSettings(this.context);
        // final ApnDto defaultApn = apnSettings.getDefaultApn();
        // if ((defaultApn != null) && (Utils.getNetworkType(this.context) == ConnectivityManager.TYPE_MOBILE)) {
        // this.proxyHost = defaultApn.getProxy();
        // this.proxyPort = defaultApn.getPort();
        // LOG.i("proxy" + this.proxyHost + "port" + this.proxyPort);
        // }
    }

    private String getStringFromInputStream(final InputStream inputStream) throws IOException {

        final byte[] bytes = new byte[1024];
        int size;
        final StringBuilder returnString = new StringBuilder();
        while ((size = inputStream.read(bytes)) != -1) {
            returnString.append(new String(bytes, 0, size));

        }
        return returnString.toString();

    }
}
