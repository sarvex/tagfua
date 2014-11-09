/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wootag.Constant;
import com.wootag.R;
import com.wootag.SignInFragment;
import com.wootag.VideoPlayerApp;
import com.wootag.WootagTabActivity;
import com.wootag.dto.TagInfo;

public class Util {

    private static final Logger LOG = LoggerManager.getLogger();

    public static void clearAllNotifications(final Context context) {

        final NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

    }

    public static void clearImageCache(final Context context) {

    }

    public static String decodeBase64(final String encodedString) {

        final byte[] byteData = Base64.decode(encodedString, Base64.NO_WRAP);
        String decodedString = null;
        try {
            decodedString = new String(byteData, Constant.UTF_8);
        } catch (final UnsupportedEncodingException exception) {
            LOG.e(exception);
        }
        return decodedString;
    }

    /**
     * Remove all child directories of given file directory.
     *
     * @param spannable
     */
    public static void deleteRecursive(final File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (final File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

    public static void displayMessage(final Context context, final String message) {

        // send broadcast local
        LOG.i("reiceived message notification");
    }

    /**
     * split the spannable string based on tag"[" and replace the image with tag.
     *
     * @param spannable
     */
    public static void emotifySpannable(final Spannable spannable) {

        final int length = spannable.length();
        int tagLength = 0;
        boolean inTag = false;
        int tagStartPosition = 0;
        StringBuilder buffer = new StringBuilder();
        int position = 0;

        while (position < length) {
            final String subString = spannable.subSequence(position, position + 1).toString();
            if (!inTag && subString.equals(Constant.BRACKET_OPEN)) {
                buffer = new StringBuilder();
                tagStartPosition = position;
                inTag = true;
                tagLength = 0;
            }

            if (inTag) {
                buffer.append(subString);
                tagLength++;

                // Have we reached end of the tag?
                if (subString.equals(Constant.BRACKET_CLOSE)) {
                    inTag = false;

                    final String tag = buffer.toString();
                    final int tagEnd = tagStartPosition + tagLength;
                    LOG.e("SeeAllComments", "Tag: " + tag + ", started at: " + tagStartPosition + ", finished at "
                            + tagEnd + ", length: " + tagLength);

                    String emoticonName = Constant.EMPTY;
                    emoticonName = tag.substring(tag.indexOf('[') + 1, tag.indexOf(']'));
                    LOG.e(emoticonName);

                    final Drawable drawable = VideoPlayerApp
                            .getAppContext()
                            .getResources()
                            .getDrawable(
                                    VideoPlayerApp
                                            .getAppContext()
                                            .getResources()
                                            .getIdentifier(emoticonName, Constant.DRAWABLE,
                                                    VideoPlayerApp.getAppContext().getPackageName()));
                    drawable.setBounds(0, 0, 34, 34);
                    final ImageSpan imageSpan = new ImageSpan(drawable, DynamicDrawableSpan.ALIGN_BASELINE);
                    spannable.setSpan(imageSpan, tagStartPosition, tagEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            position++;
        }
    }

    /**
     * encode the given string.
     *
     * @param String which is need to be encode
     */
    public static String encodedBase64(final String currentString) {

        byte[] commentData = null;
        try {
            commentData = currentString.trim().getBytes(Constant.UTF_8);
        } catch (final UnsupportedEncodingException exception) {
            LOG.e(exception);
        }
        return Base64.encodeToString(commentData, Base64.NO_WRAP);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void generateNotification(final Context context, final String message, final String title,
            final String subtilte, final String ticker, final String vibrate, final String sound) {

        Intent notificationIntent;
        PendingIntent intent;

        if (!Strings.isNullOrEmpty(MainManager.getInstance().getUserId())) {
            Config.setUserID(MainManager.getInstance().getUserId());
            notificationIntent = new Intent(context, WootagTabActivity.class);
            notificationIntent.putExtra(Constant.FROM, Constant.BACKGROUND);

            // set intent so it does not start a new activity
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        } else {
            notificationIntent = new Intent(context, SignInFragment.class);
            notificationIntent.putExtra(Constant.FROM, Constant.BACKGROUND);

            // set intent so it does not start a new activity
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        }

        final int id = getRandomTransactionId(10, 200);
        final Notification notification = new Notification.Builder(context).setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL).setContentText(message).setContentTitle(title)
                .setSmallIcon(R.drawable.app_icon).setTicker(ticker).setContentIntent(intent).build();
        final NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);

    }

    public static List<String> getAllActivities() {

        final List<String> activitites = new ArrayList<String>();
        activitites.add(Constant.BROWSE_ACTIVITY);
        activitites.add(Constant.DISCOVER_MORE_PEOPLE_ACTIVITY);
        activitites.add(Constant.LIKED_ACTIVITY);
        activitites.add(Constant.MORE_VIDEOS_ACTIVITY);
        activitites.add(Constant.MY_VIDEO_ACTIVITY);
        activitites.add(Constant.NEW_MY_PAGE_ACTIVITY);
        activitites.add(Constant.OTHER_USER_ACTIVITY);
        activitites.add(Constant.PLAYER_ACTIVITY);
        activitites.add(Constant.SHARE_ACTIVITY);
        activitites.add(Constant.SUGGESTED_USER_ACTIVITY);
        activitites.add(Constant.UPLOADING_FILE_QUEUE_ACTIVITY);
        activitites.add(Constant.USERS_LIST_ACTIVITY);
        activitites.add(Constant.VIDEO_DETAILS_ACTIVITY);
        activitites.add(Constant.WEB_VIEW_ACTIVITY);
        activitites.add(Constant.SEE_ALL_COMMENTS_ACTIVITY);
        activitites.add(Constant.NOTIFICATIONS_ACTIVITY);

        return activitites;

    }

    public static String getApplicationName(final Context context) {

        final int stringId = context.getApplicationInfo().labelRes;
        return context.getString(stringId);
    }

    public static String getCalendarTime(final Date date) {

        if (date == null) {
            return Constant.EMPTY;
        }

        String message = null;

        final Calendar cal = Calendar.getInstance();
        final Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date);

        final long delta = (cal.getTimeInMillis() - cal2.getTimeInMillis()) / 1000;
        if (delta < 0) {
            message = Constant._0_SECONDS_AGO;

        } else if (delta < (1 * Constant.MINUTE)) {
            message = (delta == 1) ? Constant._1_SECOND_AGO : delta + Constant.SECONDS_AGO;

        } else if (delta < (2 * Constant.MINUTE)) {
            message = Constant.A_MINUTE_AGO;

        } else if (delta < (45 * Constant.MINUTE)) {
            message = ((delta / Constant.MINUTE) == 1) ? Constant._1_MINUTE_AGO : (delta / Constant.MINUTE)
                    + Constant.MINUTES_AGO;

        } else if (delta < (90 * Constant.MINUTE)) {
            message = Constant.AN_HOUR_AGO;

        } else if (delta < (24 * Constant.HOUR)) {
            message = ((delta / Constant.HOUR) == 1) ? Constant._1_HOUR_AGO : (delta / Constant.HOUR)
                    + Constant.HOURS_AGO;

        } else if (delta < (48 * Constant.HOUR)) {
            message = Constant.YESTERDAY;

        } else if (delta < (30 * Constant.DAY)) {
            message = ((delta / Constant.DAY) == 1) ? Constant._1_DAY_AGO : (delta / Constant.DAY) + Constant.DAYS_AGO;

        } else if (delta < (12 * Constant.MONTH)) {
            message = ((delta / Constant.MONTH) <= 1) ? Constant._1_MONTH_AGO : (delta / Constant.MONTH)
                    + Constant.MONTHS_AGO;

        } else {
            message = ((delta / Constant.YEAR) <= 1) ? Constant._1_YEAR_AGO : (delta / Constant.YEAR)
                    + Constant.YEARS_AGO;

        }
        return message;
    }

    /**
     * returns the specified color resource id.
     *
     * @param context ,color string
     */
    public static int getColorForMarkerBitmap(final Context cntxt, final String markerColor) {

        final Resources res = cntxt.getResources();
        int newColor = 0;
        if (Constant.RED.equalsIgnoreCase(markerColor)) {
            newColor = res.getColor(R.color.redcolor);
        } else if (Constant.GREEN.equalsIgnoreCase(markerColor)) {
            newColor = res.getColor(R.color.green);
        } else if (Constant.YELLOW.equalsIgnoreCase(markerColor)) {
            newColor = res.getColor(R.color.yellow);
        } else if (Constant.SKYBLUE.equalsIgnoreCase(markerColor)) {
            newColor = res.getColor(R.color.blue);
        } else if (Constant.BLACK.equalsIgnoreCase(markerColor)) {
            newColor = res.getColor(R.color.black);
        } else if (Constant.WHITE.equalsIgnoreCase(markerColor)) {
            newColor = res.getColor(R.color.white);
        } else if (Constant.LAVENDER.equalsIgnoreCase(markerColor)) {
            newColor = res.getColor(R.color.lavender);
        } else {
            newColor = res.getColor(R.color.yellow);
        }
        return newColor;
    }

    /**
     * returns the imageview with given color.
     *
     * @param context ,
     * @param imageview ,
     * @param color
     */
    public static ImageView getColorImage(final Context cntxt, final ImageView view, final String markerColor) {

        final Resources res = cntxt.getResources();
        int newColor = 0;
        if (Constant.RED.equalsIgnoreCase(markerColor)) {
            newColor = res.getColor(R.color.redcolor);

        } else if (Constant.GREEN.equalsIgnoreCase(markerColor)) {
            newColor = res.getColor(R.color.green);

        } else if (Constant.YELLOW.equalsIgnoreCase(markerColor)) {
            newColor = res.getColor(R.color.yellow);

        } else if (Constant.SKYBLUE.equalsIgnoreCase(markerColor)) {
            newColor = res.getColor(R.color.blue);

        } else if (Constant.BLACK.equalsIgnoreCase(markerColor)) {
            newColor = res.getColor(R.color.black);

        } else if (Constant.WHITE.equalsIgnoreCase(markerColor)) {
            newColor = res.getColor(R.color.white);

        } else if (Constant.LAVENDER.equalsIgnoreCase(markerColor)) {
            newColor = res.getColor(R.color.lavender);

        } else {
            newColor = res.getColor(R.color.yellow);
        }

        view.setColorFilter(newColor, Mode.SRC_ATOP);
        return view;
    }

    public static String getCurrentDate() {

        return Util.getCurrentDate(Constant.DD_MMM_YYYY);
    }

    public static String getCurrentDate(final String format) {

        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return dateFormat.format(calendar.getTime());

    }

    public static String getCurrentSQLiteDateTime() {

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(calendar.getTimeInMillis()
                - calendar.getTimeZone().getOffset(calendar.getTimeInMillis()));
        return new SimpleDateFormat(Constant.SQLITE_DATE_TIME_FORMAT_PATTERN, Locale.getDefault()).format(calendar
                .getTime());
    }

    public static String getCurrentTimeStamp() {

        return DateFormat.format(Constant.YYYYMMDDHHMMSS, new Date()).toString();
    }

    public static String getDateTimeStamp() {

        return new SimpleDateFormat(Constant.DATE_TIME_STAMP_PATTERN, Locale.getDefault()).format(new Date());
    }

    public static String getImei(final Context context) {

        return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    }

    public static String getLocalDateFromUTC(final String date) {

        String localTime = Constant.EMPTY;
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constant.YYYY_MM_DD_T_HH_MM_SS,
                Locale.getDefault());
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(Constant.UTC));
        Date fDate = null;
        try {
            fDate = simpleDateFormat.parse(date);
        } catch (final ParseException exception) {
            LOG.e(exception);
        }
        localTime = Util.getLocalTimeFromGMT(fDate.toString());
        return localTime;
    }

    public static String getLocalTime(final String sqliteDateTime) {

        if (sqliteDateTime == null) {
            return null;
        }

        Date sqliteDateTimeObject = null;

        try {
            sqliteDateTimeObject = new SimpleDateFormat(Constant.SQLITE_DATE_TIME_FORMAT_PATTERN, Locale.getDefault())
                    .parse(sqliteDateTime);
        } catch (final ParseException exception) {
            LOG.e(exception);
        }

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(sqliteDateTimeObject.getTime()
                + calendar.getTimeZone().getOffset(sqliteDateTimeObject.getTime()));
        return getCalendarTime(calendar.getTime());
    }

    public static String getLocalTimeFromGMT(final String time) {

        String localTimeString = Constant.EMPTY;
        final SimpleDateFormat inputFormat = new SimpleDateFormat(Constant.EEE_MMM_DD_HH_MM_SS_GMT_ZZZZZ_YYYY,
                Locale.getDefault());

        Date date = null;
        try {
            date = inputFormat.parse(time);
        } catch (final ParseException exception) {
            LOG.e(exception);
        }

        inputFormat.setTimeZone(TimeZone.getTimeZone(Constant.ETC_UTC));
        final SimpleDateFormat outputFormat = new SimpleDateFormat(Constant.DD_MMM_YYYY, Locale.getDefault());
        // // Adjust locale and zone appropriately
        localTimeString = outputFormat.format(date);

        return localTimeString;
    }

    public static long getLongFromTime(final String time) {// seconds

        return (long) (Float.parseFloat(time) * (1000));
    }

    /**
     * Returns marker type based on given width and height of the tag
     *
     * @param tag coordinate x,tag coordinate y ,width of current screen,height of the current screen
     */
    public static int getMarkerType(final float tagx, final float tagy, final int widthX, final int heightY) {

        int markerType = 0;
        if ((tagx <= widthX) && (tagy < heightY)) {
            markerType = 1;

        } else if ((tagx > widthX) && (tagy < heightY)) {
            markerType = 2;

        } else if ((tagx <= widthX) && (tagy >= heightY)) {
            markerType = 3;

        } else if ((tagx > widthX) && (tagy >= heightY)) {
            markerType = 4;

        } else {
            markerType = 1;
        }
        return markerType;
    }

    public static String getMD5Hash(final byte[] partdata) {

        return getMD5Hash(new ByteArrayInputStream(partdata));
    }

    public static String getMD5Hash(final InputStream inputStream) {

        try {
            // Compute hash
            final MessageDigest digester = MessageDigest.getInstance("MD5");
            final byte[] bytes = new byte[8192];
            int byteCount;

            while ((byteCount = inputStream.read(bytes)) > 0) {
                digester.update(bytes, 0, byteCount);
            }
            final byte[] digest = digester.digest();
            // Create Hex String
            final StringBuffer hexString = new StringBuffer();
            for (final byte element : digest) {
                final String hex = Integer.toHexString(0xFF & element);
                if (hex.length() == 2) {
                    hexString.append(hex);
                } else if (hex.length() == 1) {
                    // pad it with 0
                    hexString.append(R.string._0);
                    hexString.append(hex);
                } else {
                    LOG.e("Expected 2 hex characters. But got " + hex.length() + " characters.");
                }
            }

            return hexString.toString();
        } catch (final IOException e) {
            LOG.e("Error while reading input stream for computing MD5 hash: " + e.toString());

            return null;
        } catch (final NoSuchAlgorithmException e) {
            LOG.e("MD5 algorithm not present (extremely unlikely): " + e.toString());

            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    LOG.e("Error while closing input stream after computing MD5 hash: " + e.toString());
                }
            }
        }
    }

    public static String getMD5Hash(final String filePath) {

        try {
            final FileInputStream inputStream = new FileInputStream(filePath);
            return getMD5Hash(inputStream);
        } catch (final FileNotFoundException e) {
            LOG.e("Error reading file at path " + filePath + ": " + e.toString());

            return null;
        }
    }

    public static int getNetworkType(final Context context) {

        final ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            final int netType = networkInfo.getType();
            if (netType == ConnectivityManager.TYPE_WIFI) {
                return ConnectivityManager.TYPE_WIFI;
            } else if (netType == ConnectivityManager.TYPE_MOBILE) {
                return ConnectivityManager.TYPE_MOBILE;
            } else {
                return -1;
            }
        }
        return -1;
    }

    public static boolean getNotificationCalendarTime(final Date date) {

        if (date == null) {
            return true;
        }

        final Calendar cal = Calendar.getInstance();

        final Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date);

        final long delta = (cal.getTimeInMillis() - cal2.getTimeInMillis()) / 1000;
        if (delta < 0) {
            return true;

        } else if (delta < (1 * Constant.MINUTE)) {
            return true;

        } else if (delta < (2 * Constant.MINUTE)) {
            return true;

        } else if (delta < (45 * Constant.MINUTE)) {
            return true;

        } else if (delta < (90 * Constant.MINUTE)) {
            return true;

        } else if (delta < (24 * Constant.HOUR)) {

            return true;

        } else if (delta < (48 * Constant.HOUR)) {
            return true;

        } else if (delta < (30 * Constant.DAY)) {
            final int days = (int) (delta / Constant.DAY);
            return (days <= 7);
        } else if (delta < (12 * Constant.MONTH)) {
            return false;

        } else {
            return false;

        }

    }

    public static boolean getNotificationLocalTime(final String sqliteDateTime) {

        if (sqliteDateTime == null) {
            return true;
        }

        Date sqliteDateTimeObject = null;

        try {
            sqliteDateTimeObject = new SimpleDateFormat(Constant.SQLITE_DATE_TIME_FORMAT_PATTERN, Locale.getDefault())
                    .parse(sqliteDateTime);
        } catch (final ParseException exception) {
            LOG.e(exception);
        }

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(sqliteDateTimeObject.getTime()
                + calendar.getTimeZone().getOffset(sqliteDateTimeObject.getTime()));

        return getNotificationCalendarTime(calendar.getTime());
    }

    public static String getPlayerTimeString(final long millis) {

        final StringBuffer buf = new StringBuffer();
        final int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        final int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);
        buf.append(String.format(Constant._02D, Integer.valueOf(minutes))).append(':')
                .append(String.format(Constant._02D, Integer.valueOf(seconds)));

        return buf.toString();
    }

    public static int getRandomTransactionId(final int min, final int max) {

        return new Random().nextInt(max - min) + min;
    }

    public static float getTagCoordinatesX(final TagInfo tag, final int width) {

        LOG.i("co-ordinate x from server " + tag.getTagX());
        final float clienttagx = ((width * tag.getTagX()) / 100);
        LOG.i("co-ordinate x caluculated  " + clienttagx);
        return clienttagx;
    }

    public static float getTagCoordinatesY(final TagInfo tag, final int height) {

        LOG.i("co-ordinate Y from server " + tag.getTagY());
        final float clienttagy = ((height * tag.getTagY()) / 100);// / tag.getScreenWidth();
        LOG.i("co-ordinate Y from server " + clienttagy);
        return clienttagy;
    }

    public static String getTimeString(final long millis) {

        final StringBuffer buf = new StringBuffer();
        final float minutes = millis / (1000);// * 60
        buf.append(minutes);
        return buf.toString();
    }

    public static String getTZLocalTime(final String sqliteDateTime) {

        if (sqliteDateTime == null) {
            return null;
        }

        Date sqliteDateTimeObject = null;

        try {
            sqliteDateTimeObject = new SimpleDateFormat(Constant.SQLITE_DATE_TIME_FORMAT_PATTERN, Locale.getDefault())
                    .parse(sqliteDateTime);
        } catch (final ParseException exception) {
            LOG.e(exception);
        }

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(sqliteDateTimeObject.getTime()
                + calendar.getTimeZone().getOffset(sqliteDateTimeObject.getTime()));
        return getCalendarTime(sqliteDateTimeObject);
    }

    public static Bitmap getVideoFrame(final String path) {

        final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Bitmap bitmap = null;
        retriever.setDataSource(path);
        bitmap = retriever.getFrameAtTime(1);
        retriever.release();
        return bitmap;
    }

    public static Bitmap getVideoFrame(final String path, long time) {

        final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        final Bitmap bitmap = null;
        time = time * 1000000;
        retriever.setDataSource(path);
        retriever.release();
        return bitmap;
    }

    /**
     * Returns the boolean flag true if app is in foreground state.
     *
     * @param application context object
     */
    public static boolean isAppForground(final Context context) {

        final ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningAppProcessInfo> l = mActivityManager.getRunningAppProcesses();
        final Iterator<RunningAppProcessInfo> i = l.iterator();
        while (i.hasNext()) {
            final RunningAppProcessInfo info = i.next();
            if ((info.uid == context.getApplicationInfo().uid)
                    && (info.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isConnected(final Context applicationContext) {

        final NetworkInfo networkInfo = ((ConnectivityManager) applicationContext
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return (networkInfo != null) && networkInfo.isConnectedOrConnecting();
    }

    public static boolean isConnectedToWifi(final Context applicationContext) {

        final ConnectivityManager connectivityManager = (ConnectivityManager) applicationContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return (networkInfo != null) && networkInfo.isConnectedOrConnecting()
                && (networkInfo.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public static boolean isFeedFragmentForeground(final Context context) {

        boolean notificationActivity = false;

        if (Config.getCurrentTabIndex() == 0) {
            notificationActivity = true;
        }
        return notificationActivity;

    }

    public static boolean isMatchedBday(final String date, final int type) {

        boolean matched = false;
        SimpleDateFormat curFormater = null;

        switch (type) {
        case 1:
            curFormater = new SimpleDateFormat(Constant.YYYY_MM_DD, Locale.getDefault());
            break;
        case 2:
            curFormater = new SimpleDateFormat(Constant.DD_MM_YYYY, Locale.getDefault());
            break;
        default:
            curFormater = new SimpleDateFormat(Constant.DD_MM_YYYY, Locale.getDefault());
            break;
        }

        Date dateObj = null;
        try {
            dateObj = curFormater.parse(date);
        } catch (final ParseException exception) {
            LOG.e(exception);
        }

        final Calendar cal = Calendar.getInstance();
        cal.setTime(dateObj);
        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat dateFormat = new SimpleDateFormat(Constant.DD_MM, Locale.getDefault());
        final String formattedDate = dateFormat.format(calendar.getTime());
        final String birthday = dateFormat.format(cal.getTime());
        if (formattedDate.equalsIgnoreCase(birthday)) {
            matched = true;
        }

        return matched;
    }

    public static boolean isNotificationFragmentForeground(final Context context) {

        boolean notificationActivity = false;

        if (Config.getCurrentTabIndex() == 3) {
            notificationActivity = true;
        }
        return notificationActivity;

    }

    /**
     * Return true if sd card available.
     *
     * @param context object ,boolean flag to show toast or not.
     */
    public static boolean isSDCardValid(final Context context, final boolean showToast) {

        final String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }

        if (Environment.MEDIA_REMOVED.equals(state)) {
            if (showToast) {
                Toast.makeText(context, context.getText(R.string.sdcard_not_present_toast_message), Toast.LENGTH_LONG)
                        .show();
            }

            return false;
        }

        if (Environment.MEDIA_UNMOUNTED.equals(state)) {
            if (showToast) {
                Toast.makeText(context, context.getText(R.string.sdcard_not_mounted_toast_message), Toast.LENGTH_LONG)
                        .show();
            }

            return false;
        }

        if (showToast) {
            Toast.makeText(context,
                    Constant.THE_SD_CARD_IN_THE_DEVICE_IS_IN + state + Constant.STATE_AND_CANNOT_BE_USED,
                    Toast.LENGTH_LONG).show();
        }

        return false;
    }

    /**
     * Return boolean true if specified service is running.
     *
     * @param application context ,service class name
     */
    public static boolean isServiceRunning(final Context applicationContext, final String serviceClassName) {

        final ActivityManager activityManager = (ActivityManager) applicationContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (final RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName) && runningServiceInfo.started) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the string from json file.
     *
     * @param json file path
     */
    public static String jsontoString(final String path) {

        String responseString = null;
        InputStream inputStream = Util.class.getClassLoader().getResourceAsStream(path);
        try {
            final StringBuffer stringBuffer = new StringBuffer();
            int character;
            while ((character = inputStream.read()) != -1) {
                stringBuffer.append((char) character);
            }
            responseString = stringBuffer.toString();
        } catch (final IOException exception) {
            LOG.e(exception);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                    inputStream = null;
                } catch (final IOException exception) {
                    LOG.e(exception);
                }
            }
        }

        return responseString;
    }

    public static boolean longsEqual(final int value1, final int value2) {

        if ((value1 == 0) && (value2 == 0)) {
            return true;
        }

        if ((value1 == 0) || (value2 == 0)) {
            return false;
        }

        if ((value1 != 0) && (value2 != 0)) {
            return value1 == value2;
        }

        return false;
    }

    public static void putNullOrValue(final ContentValues contentValues, final String key, final Boolean value) {

        if (value == null) {
            contentValues.putNull(key);
        } else {
            contentValues.put(key, value.toString());
        }
    }

    public static void putNullOrValue(final ContentValues contentValues, final String key, final Integer value) {

        if (value == null) {
            contentValues.putNull(key);
        } else {
            contentValues.put(key, value);
        }
    }

    public static void putNullOrValue(final ContentValues contentValues, final String key, final Long value) {

        if (value == null) {
            contentValues.putNull(key);
        } else {
            contentValues.put(key, value);
        }
    }

    public static void putNullOrValue(final ContentValues contentValues, final String key, final String value) {

        if (value == null) {
            contentValues.putNull(key);
        } else {
            contentValues.put(key, value);
        }
    }

    /**
     * Sets the given color to image view.
     *
     * @param imageview ,color
     */
    public static void setColorView(final ImageView colorView, final String markerColor) {

        if (Constant.RED.equalsIgnoreCase(markerColor)) {
            colorView.setBackgroundResource(R.drawable.red_color_view);

        } else if (Constant.GREEN.equalsIgnoreCase(markerColor)) {
            colorView.setBackgroundResource(R.drawable.green_color_view);

        } else if (Constant.YELLOW.equalsIgnoreCase(markerColor)) {
            colorView.setBackgroundResource(R.drawable.yellow_color_view);

        } else if (Constant.SKYBLUE.equalsIgnoreCase(markerColor)) {
            colorView.setBackgroundResource(R.drawable.blue_color_view);

        } else if (Constant.BLACK.equalsIgnoreCase(markerColor)) {
            colorView.setBackgroundResource(R.drawable.black_color_view);

        } else if (Constant.WHITE.equalsIgnoreCase(markerColor)) {
            colorView.setBackgroundResource(R.drawable.white_color_view);

        } else if (Constant.LAVENDER.equalsIgnoreCase(markerColor)) {
            colorView.setBackgroundResource(R.drawable.lavender_color_view);

        } else {
            colorView.setBackgroundResource(R.drawable.blue_color_view);
        }
    }

    public static ImageView setMarkerImage(final Context context, ImageView view, final TagInfo tag, final float tagx,
            final float tagy, final int widthX, final int heightY) {

        final String markerColor = tag.getColor();
        if ((tagx <= widthX) && (tagy < heightY)) {
            view.setImageResource(R.drawable.bluefirst);

        } else if ((tagx > widthX) && (tagy < heightY)) {
            view.setImageResource(R.drawable.bluesecond);

        } else if ((tagx <= widthX) && (tagy >= heightY)) {
            view.setImageResource(R.drawable.bluethird);

        } else if ((tagx > widthX) && (tagy >= heightY)) {
            view.setImageResource(R.drawable.bluefourth);

        } else {
            view.setImageResource(R.drawable.bluefourth);
        }

        view = getColorImage(context, view, markerColor);
        return view;
    }

    public static void tagUpdate(final TagInfo tag, final int tagIndex, final boolean show) {

        final TagInfo updateTag = new TagInfo();
        updateTag.setName(tag.getName());
        updateTag.setTagId(tag.getTagId());
        if (tag.getLink() != null) {
            updateTag.setLink(tag.getLink());
        }

        updateTag.setDisplayTime(tag.getDisplayTime());
        updateTag.setColor(tag.getColor());
        if (tag.getFbId() != null) {
            updateTag.setFbId(tag.getFbId());
        }
        if (tag.getTwId() != null) {
            updateTag.setTwId(tag.getTwId());
        }
        if (tag.getWooTagId() != null) {
            updateTag.setWooTagId(tag.getWooTagId());
        }
        if (tag.getgPlusId() != null) {
            updateTag.setgPlusId(tag.getgPlusId());
        }
        updateTag.setVideoPlaybackTime(tag.getVideoPlaybackTime());
        updateTag.setTagX(tag.getTagX());
        updateTag.setTagY(tag.getTagY());
        updateTag.setTagTimeOutFrame((tag.getVideoPlaybackTime() + (1000 * Integer.parseInt(tag.getDisplayTime()))));

        VideoPlayerApp.tagInfo.remove(tagIndex);
        VideoPlayerApp.tagInfo.add(tagIndex, updateTag);
    }
}
