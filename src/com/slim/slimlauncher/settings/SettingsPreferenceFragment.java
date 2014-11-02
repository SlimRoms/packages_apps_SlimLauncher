package com.slim.slimlauncher.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import com.slim.slimlauncher.DeviceProfile;
import com.slim.slimlauncher.DynamicGrid;
import com.slim.slimlauncher.LauncherAppState;

public class SettingsPreferenceFragment extends PreferenceFragment {

    DynamicGrid mGrid;
    DeviceProfile mProfile;

    Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGrid = LauncherAppState.getInstance().getDynamicGrid();
        if (mGrid != null) {
            mProfile = mGrid.getDeviceProfile();
            mProfile.updateFromPreferences(getActivity());
        }

        mContext = getActivity();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
