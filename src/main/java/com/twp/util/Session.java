package com.twp.util;

public class Session {
    public static String accessToken = null;
    public static String userId = null;
    
    public static void clear() {
        accessToken = null;
        userId = null;
    }
}
