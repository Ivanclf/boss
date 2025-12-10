package com.boss.bosscommon.util;

import cn.hutool.core.util.StrUtil;

public class RegexUtil {

    /**
     * 校验电话号是否正确
     * @param phone
     * @return
     */
    public static boolean isPhoneValid(String phone) { return match(phone, "^1[3-9]\\d{9}$");}

    // 校验是否不符合正则格式
    private static boolean match(String str, String regex){
        if (StrUtil.isBlank(str)) {
            return false;
        }
        return str.matches(regex);
    }
}
