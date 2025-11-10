package com.example.jjb20;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    //앱 전역에서 정보를 저장할 SahrdPreference를 관리할 Manager

    private static final String PREF_NAME = "UserPrefs";
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    //초기화 앱 실행시
    public static void init(Context context){
        if (sharedPreferences == null){
            sharedPreferences = context.getApplicationContext()
                    .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
    }

    // 저장
    public static void put(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    // 불러오기
    public static String get(String key) {
        return sharedPreferences.getString(key, null);
    }

    // 불러오기 (기본값 포함)
    public static String get(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    // 삭제
    public static void remove(String key) {
        editor.remove(key);
        editor.apply();
    }

    // 전체 삭제 (로그아웃용)
    public static void clear() {
        editor.clear();
        editor.apply();
    }
}
