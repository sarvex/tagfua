package com.TagFu.cache;

public class CacheTransactionException extends Exception {

    private static final long serialVersionUID = 1L;

    public CacheTransactionException() {

    }

    public CacheTransactionException(final String alert) {

        super(alert);
    }
}
