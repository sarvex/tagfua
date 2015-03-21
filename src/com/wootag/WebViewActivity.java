/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wootag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class WebViewActivity extends Activity {

    public static WebViewActivity webViewActivity;

    protected static final Logger LOG = LoggerManager.getLogger();
    private WebViewActivity context;
    private TextView heading;
    private String headingText = "";
    private String link;
    private Button menu;
    private View search;
    private WebView webview;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        webViewActivity = this;
        this.context = this;
        this.setContentView(R.layout.activity_web_view);
        final Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("link")) {
                this.link = bundle.getString("link");
            }
            if (bundle.containsKey("heading")) {
                this.headingText = bundle.getString("heading");
            }
        }
        this.webview = (WebView) this.findViewById(R.id.webview);

        this.heading = (TextView) this.findViewById(R.id.heading);
        this.heading.setText(this.headingText);
        this.menu = (Button) this.findViewById(R.id.menu);
        this.search = this.findViewById(R.id.settings);
        this.search.setVisibility(View.GONE);
        this.menu.setVisibility(View.GONE);
        final Button back = (Button) this.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        this.loadLink(this.link, this.webview);
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                WebViewActivity.this.finish();
            }
        });

    }

    /** this will load the given link in webview */
    private void loadLink(final String link, final WebView mWebView) {

        final WebSettings settings = mWebView.getSettings();
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setSupportZoom(true);

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        final ProgressDialog pd = ProgressDialog.show(this.context, "", "", true);
        final View view = ((LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.progress_bar, null, false);
        pd.setContentView(view);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(final WebView view, final String url) {

                LOG.i("Finished loading URL: " + url);

                if (pd.isShowing()) {
                    pd.dismiss();
                }
            }

            @Override
            public void onReceivedError(final WebView view, final int errorCode, final String description,
                    final String failingUrl) {

                LOG.e("Error: " + description);
                Toast.makeText(WebViewActivity.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
                alertDialog.setTitle("Error");
                alertDialog.setMessage(description);
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {

                    }
                });
                alertDialog.show();
            }

            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {

                LOG.i("Processing webview url click...");
                view.loadUrl(url);
                return true;
            }
        });
        mWebView.loadUrl(link);
    }
}
