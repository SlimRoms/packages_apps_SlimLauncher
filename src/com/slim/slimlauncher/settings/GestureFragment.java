package com.slim.slimlauncher.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.slim.slimlauncher.R;
import com.slim.slimlauncher.util.AppHelper;
import com.slim.slimlauncher.util.ShortcutPickHelper;

public class GestureFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    Context mContext;

    ShortcutPickHelper mPicker;

    ListPreference mGestureUp;
    ListPreference mGestureDown;
    ListPreference mGesturePinch;
    ListPreference mGestureSpread;
    ListPreference mGestureDoubleTap;

    String mGesture;
    ListPreference mGestureCurrent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        addPreferencesFromResource(R.xml.gesture_fragment);

        mGestureUp = (ListPreference) findPreference(SettingsProvider.UP_GESTURE_ACTION);
        mGestureDown = (ListPreference) findPreference(SettingsProvider.DOWN_GESTURE_ACTION);
        mGesturePinch = (ListPreference) findPreference(SettingsProvider.PINCH_GESTURE_ACTION);
        mGestureSpread = (ListPreference) findPreference(SettingsProvider.SPREAD_GESTURE_ACTION);
        mGestureDoubleTap = (ListPreference)
                findPreference(SettingsProvider.DOUBLE_TAP_GESTURE_ACTION);

        mGestureUp.setOnPreferenceChangeListener(this);
        mGestureDown.setOnPreferenceChangeListener(this);
        mGesturePinch.setOnPreferenceChangeListener(this);
        mGestureSpread.setOnPreferenceChangeListener(this);
        mGestureDoubleTap.setOnPreferenceChangeListener(this);

        mPicker = new ShortcutPickHelper(getActivity(), new ShortcutPickHelper.OnPickListener() {
            @Override
            public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
                if (uri == null) {
                    return;
                }
                if (mGesture != null) {
                    SettingsProvider.putString(mContext, mGesture + "_custom", uri);
                    mGesture = null;
                }
                if (mGestureCurrent != null) {
                    updateSummary(mGestureCurrent, "custom");
                    mGestureCurrent = null;
                }
            }
        });

        updatePrefs();
    }

    private void updatePrefs() {
        updateSummary(mGestureUp, mGestureUp.getValue());
        updateSummary(mGestureDown, mGestureDown.getValue());
        updateSummary(mGesturePinch, mGesturePinch.getValue());
        updateSummary(mGestureSpread, mGestureSpread.getValue());
        updateSummary(mGestureDoubleTap, mGestureDoubleTap.getValue());
    }

    private void updateSummary(ListPreference pref, String value) {
        if (value.equals("custom")) {
            pref.setSummary(AppHelper.getFriendlyNameForUri(
                    getActivity(), getActivity().getPackageManager(),
                    SettingsProvider.getString(getActivity(), pref.getKey() + "_custom", "")));
        } else {
            CharSequence[] array = pref.getEntryValues();
            for (int i = 0; i < array.length; i++) {
                if (array[i].equals(value)) {
                    pref.setSummary(pref.getEntries()[i]);
                    return;
                }
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mGestureUp || preference == mGestureDown ||
                preference == mGesturePinch || preference == mGestureSpread ||
                preference == mGestureDoubleTap) {
            if (newValue.equals("custom")) {
                mGesture = preference.getKey();
                mGestureCurrent = (ListPreference) preference;
                mPicker.pickShortcut(null, null, getId());
                return true;
            }
            updateSummary((ListPreference) preference, (String) newValue);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != Activity.RESULT_CANCELED
                && resultCode != Activity.RESULT_CANCELED) {
            mPicker.onActivityResult(requestCode, resultCode, data);
        }
    }
}
