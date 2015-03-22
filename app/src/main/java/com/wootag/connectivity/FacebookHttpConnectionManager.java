/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.TagFu.connectivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import android.content.Context;
import android.net.ParseException;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONStringer;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.Constant;
import com.TagFu.R;
import com.TagFu.dto.FacebookUser;
import com.TagFu.dto.Friend;
import com.TagFu.dto.Playback;
import com.TagFu.util.Config;

public class FacebookHttpConnectionManager {

    private static final String TagFu = "TagFu";

    private static final String BLANK = "";

    private static final Logger LOG = LoggerManager.getLogger();

    private static final String ACCESS_TOKEN = "?access_token=";
    private static final String ACTIONS = "actions";
    private static final String CAPTION = "caption";
    private static final String CHARSET = "charset";
    private static final String FIELDS_ID_FIRST_NAME_LAST_NAME_LOCATION = "&fields=id,first_name,last_name,location";
    private static final String HTTPS_GRAPH_FACEBOOK = "https://graph.facebook.com/";
    private static final String HTTPS_GRAPH_FACEBOOK_COM_ME = "https://graph.facebook.com/me";
    private static final String HTTP_ENTITY_MAY_NOT_BE_NULL = "HTTP entity may not be null";
    private static final String HTTP_PROTOCOL_CONTENT_CHARSET = "http.protocol.content-charset";
    private static final String HTTP_SOCKET_TIMEOUT = "http.socket.timeout";
    private static final String LINK = "link";
    private static final String LINKS_ACCESS_TOKEN = "/links?access_token=";
    private static final String MESSAGE = "message";
    private static final String NAME = "name";
    private static final String PICTURE = "picture";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String SHARED_VIA_TagFu_APP = "Shared via TagFu app";
    private static final String SOURCE = "source";
    private static final String HTTP_ENTITY_TOO_LARGE_TO_BE_BUFFERED_IN_MEMORY = "HTTP entity too large to be buffered in memory";
    private static final String HTTPS_GRAPH_FACEBOOK_COM_ME_FRIENDS_ACCESS_TOKEN = "https://graph.facebook.com/me/friends?access_token=";
    private static final String TagFu_VIDEO_STORAGE = "http://TagFuvideostorage.s3.amazonaws.com//221ec86aace048e4613213654f8dbfd6-480x800.mp4";

    private final Parser parser;
    private final Context context;

    public FacebookHttpConnectionManager(final Context context) {

        this.context = context;
        this.parser = new Parser(this.context);
    }

    public static String getContentCharSet(final HttpEntity entity) throws ParseException {

        if (entity == null) {
            throw new IllegalArgumentException(HTTP_ENTITY_MAY_NOT_BE_NULL);
        }
        String charset = null;
        if (entity.getContentType() != null) {
            final HeaderElement values[] = entity.getContentType().getElements();
            if (values.length > 0) {
                final NameValuePair param = values[0].getParameterByName(CHARSET);
                if (param != null) {
                    charset = param.getValue();
                }
            }
        }
        return charset;
    }

    /**
     * Posting link to Facebook friend wall by using open graph
     */
    public static void shareLink(final String id, final Playback video, final String accessToken) {

        String url = null;

        final HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(HTTP_SOCKET_TIMEOUT, Integer.valueOf(60 * 1000));
        client.getParams().setParameter(HTTP_PROTOCOL_CONTENT_CHARSET, Constant.UTF_8);
        client.getParams().setParameter(PICTURE, Constant.UTF_8);
        client.getParams().setParameter(CAPTION, TagFu);
        client.getParams().setParameter(NAME, TagFu_VIDEO_STORAGE);
        client.getParams().setParameter(SOURCE, TagFu_VIDEO_STORAGE);
        client.getParams().setParameter(MESSAGE, SHARED_VIA_TagFu_APP);
        client.getParams().setParameter(REDIRECT_URI, TagFu_VIDEO_STORAGE);

        JSONStringer actions = null;
        try {
            actions = new JSONStringer().object().key(NAME).value(R.string.TagFu).key(LINK)
                    .value(TagFu_VIDEO_STORAGE).endObject();
        } catch (final JSONException exception) {
            LOG.e(exception);
        }

        client.getParams().setParameter(ACTIONS, actions.toString());

        url = HTTPS_GRAPH_FACEBOOK + id + LINKS_ACCESS_TOKEN + accessToken;
        final HttpPost grt = new HttpPost(url);
        HttpResponse response = null;
        try {
            response = client.execute(grt);
        } catch (final ClientProtocolException exception) {
            LOG.e(exception);
        } catch (final IOException exception) {
            LOG.e(exception);
        }
        final HttpEntity entity = response.getEntity();
        final String responseString = getResponseBody(entity);
    }

    private static String getResponseBody(final HttpEntity entity) {

        if (entity == null) {
            throw new IllegalArgumentException(HTTP_ENTITY_MAY_NOT_BE_NULL);
        }

        InputStream instream = null;
        try {
            instream = entity.getContent();
        } catch (final IOException exception) {
            LOG.e(exception);
        }

        if (instream == null) {
            return BLANK;
        }

        if (entity.getContentLength() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(HTTP_ENTITY_TOO_LARGE_TO_BE_BUFFERED_IN_MEMORY);
        }

        String charset = getContentCharSet(entity);
        if (charset == null) {
            charset = HTTP.DEFAULT_CONTENT_CHARSET;
        }

        final StringBuilder buffer = new StringBuilder();
        Reader reader = null;
        try {
            reader = new InputStreamReader(instream, charset);

            final char[] tmp = new char[1024];
            int l;
            while ((l = reader.read(tmp)) != -1) {
                buffer.append(tmp, 0, l);
            }
        } catch (final UnsupportedEncodingException exception) {
            LOG.e(exception);
        } catch (final IOException exception) {
            LOG.e(exception);
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (final IOException exception) {
                    LOG.e(exception);
                }
            }
        }

        return buffer.toString();
    }

    /**
     * Returns the own feed .
     *
     * @throws JSONException
     */
    public FacebookUser getFacebookFeed() throws JSONException {

        final HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(HTTP_SOCKET_TIMEOUT, Integer.valueOf(60 * 1000));
        client.getParams().setParameter(HTTP_PROTOCOL_CONTENT_CHARSET, Constant.UTF_8);
        final String sendUrl = HTTPS_GRAPH_FACEBOOK_COM_ME + ACCESS_TOKEN + Config.getFacebookAccessToken();
        final HttpGet grt = new HttpGet(sendUrl);

        HttpResponse response = null;
        try {
            response = client.execute(grt);
        } catch (final ClientProtocolException exception) {
            LOG.e(exception);
        } catch (final IOException exception) {
            LOG.e(exception);
        }

        final HttpEntity entity = response.getEntity();
        final String responseString = getResponseBody(entity);
        return Parser.parseFriendInfo(this.context, responseString);
    }

    /**
     * Returns the facebook friend profile inforamtion.
     *
     * @param fbuser id
     * @throws JSONException
     */
    public FacebookUser getFacebookFriendInfo(final String friendid) throws JSONException {

        final HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(HTTP_SOCKET_TIMEOUT, Integer.valueOf(60 * 1000));
        client.getParams().setParameter(HTTP_PROTOCOL_CONTENT_CHARSET, Constant.UTF_8);
        final String sendUrl = HTTPS_GRAPH_FACEBOOK + friendid + ACCESS_TOKEN + Config.getFacebookAccessToken();
        final HttpGet grt = new HttpGet(sendUrl);

        HttpResponse response = null;
        try {
            response = client.execute(grt);
        } catch (final ClientProtocolException exception) {
            LOG.e(exception);
        } catch (final IOException exception) {
            LOG.e(exception);
        }

        final HttpEntity entity = response.getEntity();
        final String responseString = getResponseBody(entity);
        return Parser.parseFriendInfo(this.context, responseString);
    }

    /**
     * Returns the list of facebook friends.
     *
     * @throws JSONException
     */
    public List<Friend> getFacebookFriendsList() throws JSONException {

        String url = null;
        final HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(HTTP_SOCKET_TIMEOUT, Integer.valueOf(60 * 1000));
        client.getParams().setParameter(HTTP_PROTOCOL_CONTENT_CHARSET, Constant.UTF_8);
        url = HTTPS_GRAPH_FACEBOOK_COM_ME_FRIENDS_ACCESS_TOKEN + Config.getFacebookAccessToken()
                + FIELDS_ID_FIRST_NAME_LAST_NAME_LOCATION;
        final HttpGet grt = new HttpGet(url);

        HttpResponse response = null;
        try {
            response = client.execute(grt);
        } catch (final ClientProtocolException exception) {
            LOG.e(exception);
        } catch (final IOException exception) {
            LOG.e(exception);
        }

        final HttpEntity entity = response.getEntity();
        final String responseString = getResponseBody(entity);

        return Parser.parseFriendList(this.context, responseString);
    }
}
