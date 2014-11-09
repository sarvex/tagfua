/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.wootag.facebook.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;

import org.json.JSONException;
import org.json.JSONObject;

class JsonUtil {

    private static final String UNCHECKED = "unchecked";

    static void jsonObjectClear(final JSONObject jsonObject) {

        @SuppressWarnings(UNCHECKED)
        final Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            keys.next();
            keys.remove();
        }
    }

    static boolean jsonObjectContainsValue(final JSONObject jsonObject, final Object value) {

        @SuppressWarnings(UNCHECKED)
        final Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            final Object thisValue = jsonObject.opt(keys.next());
            if ((thisValue != null) && thisValue.equals(value)) {
                return true;
            }
        }
        return false;
    }

    static Set<Map.Entry<String, Object>> jsonObjectEntrySet(final JSONObject jsonObject) {

        final HashSet<Map.Entry<String, Object>> result = new HashSet<Map.Entry<String, Object>>();

        @SuppressWarnings(UNCHECKED)
        final Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            final Object value = jsonObject.opt(key);
            result.add(new JSONObjectEntry(key, value));
        }

        return result;
    }

    static Set<String> jsonObjectKeySet(final JSONObject jsonObject) {

        final HashSet<String> result = new HashSet<String>();

        @SuppressWarnings(UNCHECKED)
        final Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            result.add(keys.next());
        }

        return result;
    }

    static void jsonObjectPutAll(final JSONObject jsonObject, final Map<String, Object> map) {

        final Set<Map.Entry<String, Object>> entrySet = map.entrySet();
        for (final Map.Entry<String, Object> entry : entrySet) {
            try {
                jsonObject.putOpt(entry.getKey(), entry.getValue());
            } catch (final JSONException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    static Collection<Object> jsonObjectValues(final JSONObject jsonObject) {

        final ArrayList<Object> result = new ArrayList<Object>();

        @SuppressWarnings(UNCHECKED)
        final Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            result.add(jsonObject.opt(keys.next()));
        }

        return result;
    }

    private final static class JSONObjectEntry implements Map.Entry<String, Object> {

        private final String key;
        private final Object value;

        JSONObjectEntry(final String key, final Object value) {

            this.key = key;
            this.value = value;
        }

        @SuppressLint("FieldGetter")
        @Override
        public String getKey() {

            return this.key;
        }

        @Override
        public Object getValue() {

            return this.value;
        }

        @Override
        public Object setValue(final Object object) {

            throw new UnsupportedOperationException("JSONObjectEntry is immutable");
        }

    }
}
