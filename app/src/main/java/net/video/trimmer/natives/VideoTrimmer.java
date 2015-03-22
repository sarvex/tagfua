/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package net.video.trimmer.natives;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class VideoTrimmer {

    private static final Logger LOG = LoggerManager.getLogger();

    public static native int trim(String inputFile, String outputFile, int start, int duration);

    public int trim_(final String inputFileName, final String outFileName, final int start, final int duration) {

        return trim(inputFileName, outFileName, start, duration);
    }
}
