package org.slim.launcher;

import android.graphics.Rect;

import com.android.launcher3.DeviceProfile;
import org.slim.launcher.settings.SettingsProvider;

public class SlimDeviceProfile {

    public int workspacePaddingTop;

    @SuppressWarnings("unused")
    public SlimDeviceProfile(SlimLauncher slimLauncher) {
    }

    public void updateFromPreferences() {

        boolean showSearchBar = SettingsProvider.getBoolean(SlimLauncher.getInstance(),
                SettingsProvider.KEY_SHOW_SEARCH_BAR, true);

        DeviceProfile profile = SlimLauncher.getInstance().getDeviceProfile();

        //Rect searchBarBounds = profile.getSearchBarBounds(false);

        workspacePaddingTop = 0;
    }
}
