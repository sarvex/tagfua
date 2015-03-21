/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wootag.util;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.WindowManager;

/**
 * @author sarvex
 */
public final class Alerts {

    private Alerts() {

    }

    /**
     * shows one dialog with positive button if clicks on ok button it will finish the current activity
     */

    public static void showAlert(final String heading, final String message, final Context context) {

        new AlertDialog.Builder(context).setTitle(heading).setMessage(message).setCancelable(false)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {

                        dialog.dismiss();
                        ((Activity) context).finish();
                    }
                }).create().show();
    }

    /**
     * shows one dialog with positive button
     */

    public static void showAlertOnly(final String heading, final String message, final Context context) {

        new AlertDialog.Builder(context).setTitle(heading).setMessage(message).setCancelable(false)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {

                        dialog.dismiss();
                        ((Activity) context).finish();
                    }
                }).create().show();
    }

    public static void showErrorOnly(final String message, final Context context) {

        Alerts.showAlertOnly("Error", message, context);

    }

    public static void showException(final String message, final Context context) {

        Alerts.showAlert("EXCEPTION", message, context);
    }

    public static void showExceptionOnly(final String message, final Context context) {

        Alerts.showAlertOnly("EXCEPTION", message, context);
    }

    public static void showInfo(final String message, final Context context) {

        Alerts.showAlert("INFO", message, context);
    }

    public static void showInfoOnly(final String message, final Context context) {

        Alerts.showAlertOnly("INFO", message, context);
    }

    public static void showServiceAlert(final String heading, final String message, final Context context) {

        final AlertDialog alert = new AlertDialog.Builder(context).setTitle(heading).setMessage(message)
                .setCancelable(false).setNegativeButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {

                        dialog.dismiss();
                    }
                }).create();

        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();
    }

    /**
     * shows video discard dialog if user clicks on ok button video will be deleted
     */

    public static void videoDisacrdAlert(final String heading, final String message, final Context context,
            final String path, final Dialog dialog) {

        new AlertDialog.Builder(context).setTitle(heading).setMessage(message).setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {

                        final File file = new File(path);
                        file.delete();
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        ((Activity) context).finish();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {

                        dialog.dismiss();
                    }
                }).create().show();
    }
}
