package com.slim.slimlauncher.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.view.MenuItem;

import com.slim.slimlauncher.DeviceProfile;
import com.slim.slimlauncher.DynamicGrid;
import com.slim.slimlauncher.LauncherAppState;
import com.slim.slimlauncher.R;

public class SettingsPreferenceFragment extends PreferenceFragment {

    DynamicGrid mGrid;
    DeviceProfile mProfile;

    Context mContext;

    CharSequence mTitle;

    boolean showHomeAsUp = true;

    public void onCreate(Bundle savedInstanceState, CharSequence title) {
        mTitle = title;
        super.onCreate(savedInstanceState);
        onCreate();
    }

    public void onCreate(Bundle savedInstanceState, CharSequence title, boolean displayHomeAsUp) {
        mTitle = title;
        showHomeAsUp = displayHomeAsUp;
        super.onCreate(savedInstanceState);
        onCreate();
    }

    public void onCreate(Bundle savedInstanceState, boolean displayHomeAsUp) {
        showHomeAsUp = displayHomeAsUp;
        super.onCreate(savedInstanceState);
        onCreate();
    }

    public void onResume(CharSequence title) {
        super.onResume();
        mTitle = title;
        setHeaderTitle();
    }

    private void onCreate() {

        setHeaderTitle();

        mGrid = LauncherAppState.getInstance().getDynamicGrid();
        if (mGrid != null) {
            mProfile = mGrid.getDeviceProfile();
            mProfile.updateFromPreferences(getActivity());
        }

        mContext = getActivity();

        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().beginTransaction().remove(this).commit();
                getFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setHeaderTitle() {
        if (TextUtils.isEmpty(mTitle)) {
            mTitle = getString(R.string.slim_application_name);
        }
        if (getActivity() != null) {
            if (getActivity().getActionBar() != null) {
                getActivity().getActionBar().setTitle(mTitle);
                getActivity().getActionBar().setDisplayHomeAsUpEnabled(showHomeAsUp);
            }
        }
    }
}
