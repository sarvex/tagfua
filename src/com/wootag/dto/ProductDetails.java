/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.dto;

import java.io.Serializable;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class ProductDetails implements Serializable {

    private static final Logger LOG = LoggerManager.getLogger();
    private static final long serialVersionUID = 1707799599163703478L;

    private String currencyCategory;
    private String productCategory;
    private String productDescription;
    private String productLink;
    private String productName;
    private String productPrice;
    private String sold;

    public String getCurrencyCategory() {

        return this.currencyCategory;
    }

    public String getProductCategory() {

        return this.productCategory;
    }

    public String getProductDescription() {

        return this.productDescription;
    }

    public String getProductLink() {

        return this.productLink;
    }

    public String getProductName() {

        return this.productName;
    }

    public String getProductPrice() {

        return this.productPrice;
    }

    public String getSold() {

        return this.sold;
    }

    public void setCurrencyCategory(final String currencyCategory) {

        this.currencyCategory = currencyCategory;
    }

    public void setProductCategory(final String productCategory) {

        this.productCategory = productCategory;
    }

    public void setProductDescription(final String productDescription) {

        this.productDescription = productDescription;
    }

    public void setProductLink(final String productLink) {

        this.productLink = productLink;
    }

    public void setProductName(final String productName) {

        this.productName = productName;
    }

    public void setProductPrice(final String productPrice) {

        this.productPrice = productPrice;
    }

    public void setSold(final String sold) {

        this.sold = sold;
    }

}
