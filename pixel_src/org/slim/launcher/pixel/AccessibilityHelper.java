package org.slim.launcher.pixel;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;

import org.slim.launcher.SlimLauncher;

public class AccessibilityHelper extends AccessibilityDelegate {
    public static String getShowGoogleAppText (Context context) {
        try {
            Resources res = context.getPackageManager()
                    .getResourcesForApplication("com.google.android.googlequicksearchbox");
            int id = res.getIdentifier("title_google_home_screen", "string",
                    "com.google.android.googlequicksearchbox");
            if (id != 0) {
                if (TextUtils.isEmpty(res.getString(id))) {
                    return context.getString(R.string.title_show_google_app, res.getString(id));
                }
            }
        } catch (NameNotFoundException e) {
        }
        return context.getString(R.string.title_show_google_app_default);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo (View view,
                                                   AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
        if (SlimLauncher.getInstance().getClient().isConnected()) {
            accessibilityNodeInfo.addAction(new AccessibilityAction(R.string.title_show_google_app,
                    AccessibilityHelper.getShowGoogleAppText(view.getContext())));
        }
    }

    @Override
    public boolean performAccessibilityAction (View view, int i, Bundle bundle) {
        if (i != R.string.title_show_google_app) {
            return super.performAccessibilityAction(view, i, bundle);
        }
        SlimLauncher.getInstance().getClient().openOverlay(true);
        return true;
    }
}