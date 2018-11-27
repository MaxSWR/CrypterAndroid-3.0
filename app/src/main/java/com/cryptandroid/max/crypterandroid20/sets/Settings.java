package com.cryptandroid.max.crypterandroid20.sets;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import com.cryptandroid.max.crypterandroid20.R;
import java.util.HashMap;

public class Settings {
    public static final String TAG = "MAX__";
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
        String[] algnameArray = activity.getResources().getStringArray(R.array.alg_nameValues);
        String algname = sp.getString("algname", algnameArray[0]);
        String name = sp.getString("name", HardSettings.DEFAULT_NAME);
        String password = sp.getString("password", HardSettings.DEFAULT_PASSWORD);
        String defFolder = sp.getString("defaultFolder", HardSettings.DEFAULT_PATH);

        result.put("speed_mode", speed_mode);
        result.put("deleteAfterAction", deleteAfterAction);
        result.put("ownerpath", ownerpath);
        result.put("hidepath", hidepath);
        result.put("defaultm", defaultm);
        result.put("modeEach", modeEach);
        result.put("algname", algname);
        result.put("name", name);
        result.put("password", password);
        result.put("defaultFolder", defFolder);

        return result;
    }

    public static void debug(String str) {
        Log.d(TAG, str);
    }
}
