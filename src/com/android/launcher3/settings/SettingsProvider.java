package com.android.launcher3.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by gmillz on 9/7/14.
 */
public class SettingsProvider {

    public static final String KEY_SHOW_SEARCH_BAR = "show_search_bar";

    public static SharedPreferences get(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferences.Editor editor(Context context) {
        return get(context).edit();
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        return get(context).getBoolean(key, defValue);
    }
}
