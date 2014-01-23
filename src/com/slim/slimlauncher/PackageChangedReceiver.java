package com.slim.slimlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.slim.slimlauncher.settings.SettingsProvider;
import com.slim.slimlauncher.compat.UserHandleCompat;

public class PackageChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        final String packageName = intent.getData().getSchemeSpecificPart();

        if (packageName == null || packageName.length() == 0) {
            // they sent us a bad intent
            return;
        }
        // in rare cases the receiver races with the application to set up LauncherAppState
        LauncherAppState.setApplicationContext(context.getApplicationContext());
        LauncherAppState app = LauncherAppState.getInstance();
        app.getIconCache().remove(packageName, UserHandleCompat.myUserHandle());
        WidgetPreviewLoader.removePackageFromDb(app.getWidgetPreviewCacheDb(), packageName);

        String iconPackName = SettingsProvider.getString(context,
                SettingsProvider.KEY_ICON_PACK, "");
        if (iconPackName.equals(packageName)) {
            app.getIconCache().flush();
            app.getModel().forceReload();
        }
    }
}
