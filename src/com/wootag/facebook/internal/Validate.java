/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.wootag.facebook.internal;

import java.util.Collection;

/**
 * com.facebook.internal is solely for the use of other packages within the Facebook SDK for Android. Use of any of the
 * classes in this package is unsupported, and they may be modified or removed without warning at any time.
 */
public final class Validate {

    public static void containsNoNullOrEmpty(final Collection<String> container, final String name) {

        Validate.notNull(container, name);
        for (final String item : container) {
            if (item == null) {
                throw new NullPointerException("Container '" + name + "' cannot contain null values");
            }
            if (item.length() == 0) {
                throw new IllegalArgumentException("Container '" + name + "' cannot contain empty values");
            }
        }
    }

    public static <T> void containsNoNulls(final Collection<T> container, final String name) {

        Validate.notNull(container, name);
        for (final T item : container) {
            if (item == null) {
                throw new NullPointerException("Container '" + name + "' cannot contain null values");
            }
        }
    }

    public static <T> void notEmpty(final Collection<T> container, final String name) {

        if (container.isEmpty()) {
            throw new IllegalArgumentException("Container '" + name + "' cannot be empty");
        }
    }

    public static <T> void notEmptyAndContainsNoNulls(final Collection<T> container, final String name) {

        Validate.containsNoNulls(container, name);
        Validate.notEmpty(container, name);
    }

    public static void notNull(final Object arg, final String name) {

        if (arg == null) {
            throw new NullPointerException("Argument '" + name + "' cannot be null");
        }
    }

    public static void notNullOrEmpty(final String arg, final String name) {

        if (Utility.isNullOrEmpty(arg)) {
            throw new IllegalArgumentException("Argument '" + name + "' cannot be null or empty");
        }
    }

    public static void oneOf(final Object arg, final String name, final Object... values) {

        for (final Object value : values) {
            if (value != null) {
                if (value.equals(arg)) {
                    return;
                }
            } else {
                if (arg == null) {
                    return;
                }
            }
        }
        throw new IllegalArgumentException("Argument '" + name + "' was not one of the allowed values");
    }
}
