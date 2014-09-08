package com.android.launcher3.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by gmillz on 9/7/14.
 */
public class SettingsProvider {

    private static final String SETTINGS_KEY = "com.android.launcher3_preferences";

    public static final String KEY_SHOW_SEARCH_BAR = "show_search_bar";
    public static final String KEY_HOMESCREEN_GRID = "homescreen_grid";
    public static final String KEY_ICON_SIZE = "icon_size";
    public static final String KEY_DOCK_ICONS = "dock_icon_count";

    public static SharedPreferences get(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferences.Editor put(Context context) {
        return get(context).edit();
    }

    public static int getCellCountX(Context context, String key, int def) {
        String[] values = get(context).getString(key, "0|" + def).split("\\|");
        try {
            return Integer.parseInt(values[1]);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static void putCellCountX(Context context, String key, int value) {
        String[] values = get(context).getString(key, "0|0").split("\\|");
        put(context).putString(key, values[0] + "|" + value);
    }

    public static int getCellCountY(Context context, String key, int def) {
        String[] values = get(context).getString(key, def + "|0").split("\\|");
        try {
            return Integer.parseInt(values[0]);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static void putCellCountY(Context context, String key, int value) {
        String[] values = get(context).getString(key, "0|0").split("\\|");
        put(context).putString(key, value + "|" + values[1]);
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        return get(context).getBoolean(key, defValue);
    }

    public static int getInt(Context context, String key, int defValue) {
        return get(context).getInt(key, defValue);
    }

    public static void putInt(Context context, String key, int value) {
        put(context).putInt(key, value);
    }
}
