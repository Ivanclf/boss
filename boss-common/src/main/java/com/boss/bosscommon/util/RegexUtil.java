package com.boss.bosscommon.util;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class RegexUtil {

    private static final Pattern PHONE_PATTERN;

    static  {
        PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    }

    /**
     * 校验电话号是否正确
     * @param phone
     * @return
     */
    public static boolean isPhoneValid(String phone) {
        if(!StringUtils.hasText(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }
}
