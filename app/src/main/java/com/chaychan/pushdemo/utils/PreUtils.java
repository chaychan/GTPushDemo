package com.chaychan.pushdemo.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreUtils {

    public static final String CONFIG_FILE_NAME = "config";

    public static void putBoolean(Context context,String key, boolean value){
        SharedPreferences sp = context.getSharedPreferences(CONFIG_FILE_NAME,Context.MODE_PRIVATE);
        sp.edit().putBoolean(key,value).commit();
    }

    public static boolean getBoolean(Context context,String key, boolean defValue){
        SharedPreferences sp = context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(key,defValue);
    }

    public static void putString(Context context,String key, String value){
        SharedPreferences sp = context.getSharedPreferences(CONFIG_FILE_NAME,Context.MODE_PRIVATE);
        sp.edit().putString(key,value).commit();
    }

    public static String getString(Context context,String key, String defValue){
        SharedPreferences sp = context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(key,defValue);
    }
}
