package org.slim.launcher;

import com.android.launcher3.DeviceProfile;

import org.slim.launcher.settings.SettingsProvider;

public class SlimDeviceProfile {

    public int workspacePaddingTop;

    @SuppressWarnings("unused")
    public SlimDeviceProfile(SlimLauncher slimLauncher) {
    }

    public void updateFromPreferences() {


        //Rect searchBarBounds = profile.getSearchBarBounds(false);

        workspacePaddingTop = 0;
    }
}
