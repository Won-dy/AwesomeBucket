package com.example.awesomebucket;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPreferences {

    private static SharedPreferences getPreferences(String filename, Context context) {
        return context.getSharedPreferences(filename, Context.MODE_PRIVATE);
    }

    // 로그인 한 사용자 번호 저장
    public static void setLoginUserId(Context context, String filename, String key, Long value) {
        SharedPreferences sharedPref = getPreferences(filename, context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    // 로그인 한 사용자 번호 검색
    public static Long getLoginUserId(Context context, String filename, String key) {
        SharedPreferences sharedPref = getPreferences(filename, context);
        return sharedPref.getLong(key, -1);
    }

    // 파일 내 모든 환경 설정 값 제거
    public static void clearAll(Context context, String filename) {
        SharedPreferences sharedPref = getPreferences(filename, context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
    }

    // 파일 내 특정 환경 설정 값 제거
    public static void removeOne(Context context, String filename, String key) {
        SharedPreferences sharedPref = getPreferences(filename, context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(key);
        editor.apply();
    }

}
