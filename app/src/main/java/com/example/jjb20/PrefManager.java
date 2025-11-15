package com.example.jjb20;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PrefManager {
    //앱 전역에서 정보를 저장할 SahrdPreference를 관리할 Manager

    private static final String PREF_NAME = "UserPrefs";
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static final Gson gson = new Gson();

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

    // Long 저장
    public static void put(String key, Long value) {
        editor.putString(key, value != null ? String.valueOf(value) : null);
        editor.apply();
    }

    // Long 불러오기
    public static Long getLong(String key) {
        String value = sharedPreferences.getString(key, null);
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Long 불러오기 (기본값 포함)
    public static Long getLong(String key, Long defaultValue) {
        Long value = getLong(key);
        return value != null ? value : defaultValue;
    }

    // Integer 저장
    public static void put(String key, Integer value) {
        editor.putString(key, value != null ? String.valueOf(value) : null);
        editor.apply();
    }

    // Integer 불러오기
    public static Integer getInt(String key) {
        String value = sharedPreferences.getString(key, null);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Integer 불러오기 (기본값 포함)
    public static Integer getInt(String key, Integer defaultValue) {
        Integer value = getInt(key);
        return value != null ? value : defaultValue;
    }

    // List<String> 저장
    public static void putStringList(String key, List<String> list) {
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
    }

    // List<String> 불러오기
    public static List<String> getStringList(String key) {
        String json = sharedPreferences.getString(key, null);
        if (json == null) {
            return new ArrayList<>();
        }
        try {
            Type type = new TypeToken<List<String>>(){}.getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            return new ArrayList<>();
        }
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

    // 집 등록 관련 데이터 삭제
    public static void clearHouseRegistrationData() {
        remove("house_title");
        remove("house_description");
        remove("house_address");
        remove("house_city");
        remove("house_country");
        remove("house_price");
        remove("house_amenity_codes");
        remove("houseId");  // houseId도 함께 정리
    }
}
