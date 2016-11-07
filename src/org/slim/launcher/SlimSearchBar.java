package org.slim.launcher;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.speech.RecognizerIntent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.launcher3.R;

import org.slim.launcher.settings.SettingsProvider;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SlimSearchBar extends RelativeLayout implements View.OnClickListener {

    private SlimLauncher mSlimLauncher;

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

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        View search = findViewById(R.id.search_button_container);
        if (search != null) {
            search.setOnClickListener(this);
        }
        View voice = findViewById(R.id.voice_button_container);
        if (voice != null) {
            voice.setOnClickListener(this);
        }
        setOnClickListener(this);
    }

    public void updateSearchBarVisibility() {
        mShowSearchBar = SettingsProvider.getBoolean(mSlimLauncher,
                SettingsProvider.KEY_SHOW_SEARCH_BAR, true);

        setVisibility(mShowSearchBar ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getContext().registerReceiver(mDateReceiver, filter);
        updateDate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(mDateReceiver);
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

    private void updateDate() {
        TextView date1 = (TextView) findViewById(R.id.date1);
        TextView date2 = (TextView) findViewById(R.id.date2);
        DateFormat d1 = new SimpleDateFormat("MMMM d", Locale.getDefault());
        DateFormat d2 = new SimpleDateFormat("EEEE, yyyy", Locale.getDefault());
        if (date1 != null) {
            date1.setText(d1.format(Calendar.getInstance().getTime()));
        }
        if (date2 != null) {
            date2.setText(d2.format(Calendar.getInstance().getTime()));
        }
    }

    private BroadcastReceiver mDateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIMEZONE_CHANGED)
                    || action.equals(Intent.ACTION_DATE_CHANGED)) {
                updateDate();
            }
        }
    };
}
