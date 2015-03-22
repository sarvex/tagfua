/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved Unauthorized copying of this file, via any medium
 * is strictly prohibited Proprietary and confidential
 */
package com.TagFu;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.TagFu.dto.ProductDetails;
import com.TagFu.util.Alerts;
import com.TagFu.util.Config;

public class ProductDetailsActivity extends Activity {

    private static final Logger LOG = LoggerManager.getLogger();

    protected EditText productName;
    private EditText productDescription;
    private EditText price;
    private Spinner productCategorySpinner;
    private Spinner currencySpinner;
    private ImageButton done;
    private ImageButton cancel;
    protected ProductDetails product;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.product_details);

        this.productName = (EditText) this.findViewById(R.id.productname);
        this.productDescription = (EditText) this.findViewById(R.id.productdescription);
        this.price = (EditText) this.findViewById(R.id.productprice);
        this.productCategorySpinner = (Spinner) this.findViewById(R.id.categoryspinner);
        this.currencySpinner = (Spinner) this.findViewById(R.id.currencyspinner);
        this.done = (ImageButton) this.findViewById(R.id.doneProductDetails);
        this.cancel = (ImageButton) this.findViewById(R.id.cancelProductDetails);
        if ((Config.getProductDetails() != null) && (Config.getProductDetails().getProductName() != null)) {
            this.productName.setText(Config.getProductDetails().getProductName());
        }
        if ((Config.getProductDetails() != null) && (Config.getProductDetails().getProductDescription() != null)) {
            this.productDescription.setText(Config.getProductDetails().getProductDescription());
        }
        if ((Config.getProductDetails() != null) && (Config.getProductDetails().getProductPrice() != null)) {
            this.price.setText(Config.getProductDetails().getProductPrice());
        }
        if ((Config.getProductDetails() != null) && (Config.getProductDetails().getCurrencyCategory() != null)
                && !Config.getProductDetails().getCurrencyCategory().trim().equalsIgnoreCase("")) {
            final ArrayAdapter<String> myAdap = (ArrayAdapter<String>) this.currencySpinner.getAdapter(); // cast to an
            // ArrayAdapter
            final int spinnerPosition = myAdap.getPosition(Config.getProductDetails().getCurrencyCategory());
            this.currencySpinner.setSelection(spinnerPosition);
        }
        if ((Config.getProductDetails() != null) && (Config.getProductDetails().getProductCategory() != null)
                && !Config.getProductDetails().getProductCategory().trim().equalsIgnoreCase("")) {
            final ArrayAdapter<String> myAdap = (ArrayAdapter<String>) this.productCategorySpinner.getAdapter(); // cast
                                                                                                                 // to
                                                                                                                 // an
            // ArrayAdapter
            final int spinnerPosition = myAdap.getPosition(Config.getProductDetails().getProductCategory());
            this.productCategorySpinner.setSelection(spinnerPosition);
        }
        this.cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                Config.setProductDetails(null);
                ProductDetailsActivity.this.finish();
            }
        });
        this.done.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View ignored) {

                if ((ProductDetailsActivity.this.productName.getText() != null)
                        && (ProductDetailsActivity.this.productName.getText().toString().length() > 0)) {
                    ProductDetailsActivity.this.setProductDeatils();
                    Config.setProductDetails(ProductDetailsActivity.this.product);
                    ProductDetailsActivity.this.finish();
                } else {
                    Alerts.showAlertOnly("Alert", "Product name should not be empty", ProductDetailsActivity.this);
                }
            }
        });
    }

    void setProductDeatils() {

        this.product = new ProductDetails();
        if (this.productName.getText() != null) {
            this.product.setProductName(this.productName.getText().toString());
        }
        if (this.price.getText() != null) {
            this.product.setProductPrice(this.price.getText().toString());
        }
        this.product.setProductCategory(String.valueOf(this.productCategorySpinner.getSelectedItem()));
        this.product.setCurrencyCategory(String.valueOf(this.currencySpinner.getSelectedItem()));
        if (this.productDescription.getText() != null) {
            this.product.setProductDescription(this.productDescription.getText().toString());
        }
    }

}
