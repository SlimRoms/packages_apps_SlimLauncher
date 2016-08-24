package org.slim.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import org.slim.launcher.settings.SettingsProvider;

public class SlimSearchBar extends RelativeLayout {

    private SlimLauncher mSlimLauncher;
    private View mSearchBar;

    private boolean mShowSearchBar;

    public SlimSearchBar(Context context) {
        super(context);
    }

    public SlimSearchBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setLauncher(SlimLauncher slimLauncher) {
        mSlimLauncher = slimLauncher;
    }

    public void updateSetting(String key) {
        if (key.equals(SettingsProvider.KEY_SHOW_SEARCH_BAR)) {
            updateSearchBarVisibility();
        }
    }

    public void updateSearchBarVisibility() {
        mShowSearchBar = SettingsProvider.getBoolean(mSlimLauncher,
                SettingsProvider.KEY_SHOW_SEARCH_BAR, true);

        setVisibility(mShowSearchBar ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setAlpha(float alpha) {
        if (!mShowSearchBar) {
            alpha = 0f;
        }
        super.setAlpha(alpha);
    }
}
