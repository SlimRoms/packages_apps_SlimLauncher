package org.slim.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import com.android.launcher3.IconProvider;
import com.android.launcher3.compat.LauncherActivityInfoCompat;

import org.slim.launcher.settings.SettingsProvider;
import org.slim.launcher.util.SlimUtils;

public class SlimIconProvider extends IconProvider {

    private Context mContext;
    private IconPackHelper mIconPackHelper;
    private String mCurrentIconPack;

    public SlimIconProvider(Context context) {
        super();
        mContext = context;
        mIconPackHelper = new IconPackHelper(context);
        loadIconPack();
    }

    @Override
    public void updateSystemStateString() {
        loadIconPack();
    }
    @Override
    public String getIconSystemState(String packageName) {
        return mCurrentIconPack;
    }

    @Override
    public Drawable getIcon(LauncherActivityInfoCompat info, int iconDpi) {
        if (!mIconPackHelper.isIconPackLoaded()) {
            return info.getIcon(iconDpi);
        }
        int resourceId = mIconPackHelper.getResourceIdForActivityIcon(info);
        if (resourceId != 0) {
            return mIconPackHelper.getIconPackResources().getDrawableForDensity(resourceId,
                    iconDpi, mContext.getTheme());
        } else {
            return new BitmapDrawable(mContext.getResources(),
                    SlimUtils.createIconBitmap(info.getIcon(iconDpi), mContext, mIconPackHelper));
        }
    }

    public void loadIconPack() {
        if (mIconPackHelper == null) return;
        mIconPackHelper.unloadIconPack();
        mCurrentIconPack = SettingsProvider.getString(mContext,
                SettingsProvider.KEY_ICON_PACK, "default");
        if (!mCurrentIconPack.equals("default") &&
                !mIconPackHelper.loadIconPack(mCurrentIconPack)) {
            SettingsProvider.putString(mContext, SettingsProvider.KEY_ICON_PACK, "");
        }
    }
}
