/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.TagFu.facebook.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.database.CursorIndexOutOfBoundsException;

import com.TagFu.facebook.model.GraphObject;

class SimpleGraphObjectCursor<T extends GraphObject> implements GraphObjectCursor<T> {

    private int pos = -1;
    private boolean closed;
    private List<T> graphObjects = new ArrayList<T>();
    private boolean moreObjectsAvailable;
    private boolean fromCache;

    SimpleGraphObjectCursor() {

    }

    SimpleGraphObjectCursor(final SimpleGraphObjectCursor<T> other) {

        this.pos = other.pos;
        this.closed = other.closed;
        this.graphObjects = new ArrayList<T>();
        this.graphObjects.addAll(other.graphObjects);
        this.fromCache = other.fromCache;

        // We do not copy observers.
    }

    public void addGraphObjects(final Collection<T> graphObjects, final boolean fromCache) {

        this.graphObjects.addAll(graphObjects);
        // We consider this cached if ANY results were from the cache.
        this.fromCache |= fromCache;
    }

    @Override
    public boolean areMoreObjectsAvailable() {

        return this.moreObjectsAvailable;
    }

    @Override
    public void close() {

        this.closed = true;
    }

    @Override
    public int getCount() {

        return this.graphObjects.size();
    }

    @Override
    public T getGraphObject() {

        if (this.pos < 0) {
            throw new CursorIndexOutOfBoundsException("Before first object.");
        }
        if (this.pos >= this.graphObjects.size()) {
            throw new CursorIndexOutOfBoundsException("After last object.");
        }
        return this.graphObjects.get(this.pos);
    }

    @Override
    public int getPosition() {

        return this.pos;
    }

    @Override
    public boolean isAfterLast() {

        final int count = this.getCount();
        return (count == 0) || (this.pos == count);
    }

    @Override
    public boolean isBeforeFirst() {

        return (this.getCount() == 0) || (this.pos == -1);
    }

    @Override
    public boolean isClosed() {

        return this.closed;
    }

    @Override
    public boolean isFirst() {

        return (this.pos == 0) && (this.getCount() != 0);
    }

    @Override
    public boolean isFromCache() {

        return this.fromCache;
    }

    @Override
    public boolean isLast() {

        final int count = this.getCount();
        return (this.pos == (count - 1)) && (count != 0);
    }

    @Override
    public boolean move(final int offset) {

        return this.moveToPosition(this.pos + offset);
    }

    @Override
    public boolean moveToFirst() {

        return this.moveToPosition(0);
    }

    @Override
    public boolean moveToLast() {

        return this.moveToPosition(this.getCount() - 1);
    }

    @Override
    public boolean moveToNext() {

        return this.moveToPosition(this.pos + 1);
    }

    @Override
    public boolean moveToPosition(final int position) {

        final int count = this.getCount();
        if (position >= count) {
            this.pos = count;
            return false;
        }

        if (position < 0) {
            this.pos = -1;
            return false;
        }

        this.pos = position;
        return true;
    }

    @Override
    public boolean moveToPrevious() {

        return this.moveToPosition(this.pos - 1);
    }

    public void setFromCache(final boolean fromCache) {

        this.fromCache = fromCache;
    }

    public void setMoreObjectsAvailable(final boolean moreObjectsAvailable) {

        this.moreObjectsAvailable = moreObjectsAvailable;
    }

}
