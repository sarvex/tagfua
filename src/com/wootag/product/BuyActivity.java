/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.wTagFuproduct;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFuonstant;
import com.wooTagFu
import com.wootTagFu.ErrorResponse;
import com.wootaTagFuTagInfo;
import com.wootagTagFu.Backend;
import com.wootag.TagFulerts;
import com.wootag.uTagFuinManager;
import com.wootag.util.Util;

public class BuyActivity extends Activity {

    private static final Logger LOG = LoggerManager.getLogger();

    private static final Pattern pattern;
    private EditText address;
    private ImageButton cancel;
    private TagInfo currentTag;
    private ImageButton done;
    private EditText email;
    private TextView heading;
    private Matcher matcher;
    private EditText message;
    private EditText mobileNumber;
    private EditText name;
    private String ownerId;

    static {
        pattern = Pattern.compile(Constant.EMAIL_PATTERN);
    }

    public boolean validate(final String email) {

        this.matcher = pattern.matcher(email);
        return this.matcher.matches();
    }

    private void sendProductBuyRequest() throws JSONException {

        final JSONObject json = new JSONObject();

        json.put(Constant.BOUGHT_VIDEO_ID, this.currentTag.getServerVideoId());
        json.put(Constant.BUYER_ID, this.ownerId);
        json.put(Constant.SELLER_ID, MainManager.getInstance().getUserId());
        json.put(Constant.TAG_ID, this.currentTag.getServertagId());

        if (this.address.getText() != null) {
            json.put(Constant.BUYER_ADDRESS, this.address.getText().toString());
        }

        if (this.message.getText() != null) {
            json.put(Constant.BUYER_MESSAGE, this.message.getText().toString());
        }

        if (this.mobileNumber.getText() != null) {
            json.put(Constant.BUYER_MOBILE, this.mobileNumber.getText().toString());
        }

        if (this.name.getText() != null) {
            json.put(Constant.BUYER_NAME, this.name.getText().toString());
        }

        json.put(Constant.REQUEST_TIME, Util.getCurrentDate(Constant.DATE_FORMAT));

        final String request = json.toString();

        new checkoutAsync(request, BuyActivity.this).execute();
    }

    protected void isValidRequest() throws JSONException {

        if ((this.name.getText() != null) && (this.name.getText().toString().trim().length() > 0)) {
            if (((this.mobileNumber.getText() != null) && (this.mobileNumber.getText().toString().trim().length() > 0))
                    || (this.validate(this.email.getText().toString()))) {
                this.sendProductBuyRequest();
            } else {
                Alerts.showInfoOnly(Constant.EMAIL_MOBILE_MISSING, BuyActivity.this);
            }
        } else {
            Alerts.showInfoOnly(Constant.NAME_MISSING, BuyActivity.this);
        }

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.checkout_product);

        final Bundle bundle = this.getIntent().getExtras();

        if (bundle != null) {
            if (bundle.containsKey(Constant.TAG)) {
                this.currentTag = (TagInfo) bundle.getSerializable(Constant.TAG);
            }
            if (bundle.containsKey(Constant.OWNER_ID)) {
                this.ownerId = bundle.getString(Constant.OWNER_ID);
            }
        }

        this.heading = (TextView) this.findViewById(R.id.headerInfo);
        this.heading.setText(Constant.HI + MainManager.getInstance().getUserName() + Constant.THANKS_FOR_YOUR_INTEREST);
        this.name = (EditText) this.findViewById(R.id.buyerName);
        this.address = (EditText) this.findViewById(R.id.address);
        this.email = (EditText) this.findViewById(R.id.email);
        this.mobileNumber = (EditText) this.findViewById(R.id.mobilenumber);
        this.message = (EditText) this.findViewById(R.id.message);

        this.done = (ImageButton) this.findViewById(R.id.donecheckout);
        this.done.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                try {
                    BuyActivity.this.isValidRequest();
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            }
        });

        this.cancel = (ImageButton) this.findViewById(R.id.cancelcheckout);
        this.cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                BuyActivity.this.finish();
            }
        });
    }

    private class checkoutAsync extends AsyncTask<Void, Void, Void> {

        private final Context context;
        private ProgressDialog progressDialog;
        private final String request;
        private Object response;

        public checkoutAsync(final String request, final Context context) {

            this.request = request;
            this.context = context;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            try {
                this.response = Backend.buyProduct(this.context, this.request);
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            if (this.progressDialog != null) {
                this.progressDialog.dismiss();
            }

            if (this.response != null) {
                if (this.response instanceof Boolean) {
                    BuyActivity.this.finish();
                } else if (this.response instanceof ErrorResponse) {
                    Alerts.showInfoOnly(Constant.EMPTY + ((ErrorResponse) this.response).getMessage(), this.context);
                }
            } else {
                Alerts.showInfoOnly(Constant.NETWORK_PROBLEM, this.context);
            }

        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            this.progressDialog = ProgressDialog.show(this.context, Constant.EMPTY, Constant.EMPTY, true);
            this.progressDialog.setContentView(((LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar, null, false));
            this.progressDialog.setCancelable(false);
            this.progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.show();
        }
    }
}
