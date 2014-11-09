/*
 * Copyright (C) 2014 - present : Wootag Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wootag.util;

import java.io.IOException;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

/**
 * FIXME PIG-80 replace this code when pig will be java 6 compliant with "throw new IOException(e);"
 */
public class WrappedIOException {

    private static final Logger LOG = LoggerManager.getLogger();

    public static IOException wrap(final Throwable throwable) {

        return wrap(throwable.getMessage(), throwable);
    }

    public static IOException wrap(final String message, final Throwable throwable) {

        final IOException wrappedException = new IOException(message + " [" + throwable.getMessage() + "]");
        wrappedException.setStackTrace(throwable.getStackTrace());
        wrappedException.initCause(throwable);
        return wrappedException;
    }
}
