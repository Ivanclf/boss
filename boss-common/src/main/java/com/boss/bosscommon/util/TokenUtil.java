package com.boss.bosscommon.util;

import java.net.http.HttpRequest;

public class TokenUtil {

    public static String getToken(HttpRequest httpRequest) {
        return httpRequest.headers().map().get("authorization").getFirst();
    }
}
