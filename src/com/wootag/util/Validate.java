package com.wootag.util;

import java.util.regex.Pattern;

import com.wootag.Constant;

public final class Validate {

    public static boolean email(final String email) {

        return Pattern.compile(Constant.EMAIL_PATTERN).matcher(email).matches();
    }

}
