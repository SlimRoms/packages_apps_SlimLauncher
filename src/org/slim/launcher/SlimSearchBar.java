package org.slim.launcher;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.RelativeLayout;

import com.android.launcher3.R;

import org.slim.launcher.settings.SettingsProvider;

public class SlimSearchBar extends RelativeLayout implements View.OnClickListener {

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

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        findViewById(R.id.search_button_container).setOnClickListener(this);
        findViewById(R.id.voice_button_container).setOnClickListener(this);
        setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        Log.d("TEST", "onClick");
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        if (v.getId() == R.id.voice_button_container) {
            startVoice(v);
        } else {
            mSlimLauncher.onSearchRequested();
        }
    }

    public void startVoice(View v) {
        Context context = getContext();
        try {
            final SearchManager searchManager =
                    (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
            ComponentName activityName = searchManager.getGlobalSearchActivity();
            Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (activityName != null) {
                intent.setPackage(activityName.getPackageName());
            }
            mSlimLauncher.startActivity(v, intent, "onClickVoiceButton");
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mSlimLauncher.startActivitySafely(v, intent, "onClickVoiceButton");
        }
    }
}
