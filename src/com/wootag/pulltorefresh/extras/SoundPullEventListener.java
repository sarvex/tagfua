/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes. Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 *******************************************************************************/
package com.TagFu.pulltorefresh.extras;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.View;

import com.wTagFupulltorefresh.PullToRefreshBase;
import com.woTagFuulltorefresh.PullToRefreshBase.Mode;
import com.wootag.pulltorefresh.PullToRefreshBase.State;

public class SoundPullEventListener<V extends View> implements PullToRefreshBase.OnPullEventListener<V> {

    private final Context mContext;
    private final Map<State, Integer> mSoundMap;

    private MediaPlayer mCurrentMediaPlayer;

    /**
     * Constructor
     *
     * @param context - Context
     */
    public SoundPullEventListener(final Context context) {

        this.mContext = context;
        this.mSoundMap = new HashMap<State, Integer>();
    }

    /**
     * Set the Sounds to be played when a Pull Event happens. You specify which sound plays for which events by calling
     * this method multiple times for each event.
     * <p/>
     * If you've already set a sound for a certain event, and add another sound for that event, only the new sound will
     * be played.
     *
     * @param event - The event for which the sound will be played.
     * @param resId - Resource Id of the sound file to be played (e.g. <var>R.raw.pull_sound</var>)
     */
    public void addSoundEvent(final State event, final int resId) {

        this.mSoundMap.put(event, Integer.valueOf(resId));
    }

    /**
     * Clears all of the previously set sounds and events.
     */
    public void clearSounds() {

        this.mSoundMap.clear();
    }

    /**
     * Gets the current (or last) MediaPlayer instance.
     */
    public MediaPlayer getCurrentMediaPlayer() {

        return this.mCurrentMediaPlayer;
    }

    @Override
    public final void onPullEvent(final PullToRefreshBase<V> refreshView, final State event, final Mode direction) {

        final Integer soundResIdObj = this.mSoundMap.get(event);
        if (null != soundResIdObj) {
            this.playSound(soundResIdObj.intValue());
        }
    }

    private void playSound(final int resId) {

        // Stop current player, if there's one playing
        if (null != this.mCurrentMediaPlayer) {
            this.mCurrentMediaPlayer.stop();
            this.mCurrentMediaPlayer.release();
        }

        this.mCurrentMediaPlayer = MediaPlayer.create(this.mContext, resId);
        if (null != this.mCurrentMediaPlayer) {
            this.mCurrentMediaPlayer.start();
        }
    }

}
