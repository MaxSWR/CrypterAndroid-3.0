package com.cryptandroid.max.crypterandroid20;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by MAX on 30.05.2018.
 */

public class Settings {
    public static final String TAG = "MAX__";
    public enum MODE {CRYPT, DECRYPT};
    /**
     *
     * @return HashMap настроек
     */
    public static HashMap<String, Object> getSettings(Activity activity) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        HashMap result = new HashMap<String, Object>();
        boolean speed_mode = sp.getBoolean("speed_mode", false);
        boolean deleteAfterAction = sp.getBoolean("deleteAfterAction", false);
        boolean ownerpath = sp.getBoolean("ownerpath", false);
        boolean hidepath = sp.getBoolean("hidepath", false);
        boolean defaultm = sp.getBoolean("defaultm", false);
        String[] modeArray = activity.getResources().getStringArray(R.array.mode);
        String modeString = sp.getString("mode", modeArray[0]);
        boolean modeEach = modeString.equals("1") ? true : false;
        String[] safetyArray = activity.getResources().getStringArray(R.array.key_length);
        String safety = sp.getString("safety", safetyArray[0]);
        String name = sp.getString("name", "max");
        String password = sp.getString("password", "max");
        String defFolder = sp.getString("defaultFolder", "/sdcard/cryptoAndroid");

        result.put("speed_mode", speed_mode);
        result.put("deleteAfterAction", deleteAfterAction);
        result.put("ownerpath", ownerpath);
        result.put("hidepath", hidepath);
        result.put("defaultm", defaultm);
        result.put("modeEach", modeEach);
        result.put("safety", safety);
        result.put("name", name);
        result.put("password", password);
        result.put("defaultFolder", defFolder);

        return result;
    }

    public static void debug(String str) {
        Log.d(TAG, str);
    }
}
