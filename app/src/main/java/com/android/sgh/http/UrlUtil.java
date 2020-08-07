package com.android.sgh.http;

public class UrlUtil {
    private static final String KEY = "TdNeHWh98E";
    private static final String BASE_URL = "http://jw.car-boy.com.cn:8008/ShareFileHandler.ashx";
//    public static final String BASE_URL = "http://jw.car-boy.com.cn:6677/ShareFile/ShareFileHandler.ashx";

    public static String getHttpBaseUrl(String action) {
        String result = BASE_URL + "?action=" + action;
        long currentTimeMillis = System.currentTimeMillis() / 1000;
        String sing = MD5Util.string2MD5(KEY + currentTimeMillis);
        result += ("&sign=" + sing);
        result += ("&time=" + currentTimeMillis);
        return result;
    }


    public static String getActivateUrl(String action) {
        String result = BASE_URL + "?action=" + action;
        result += ("&serial=" + "18720573586");
        return result;
    }
}
