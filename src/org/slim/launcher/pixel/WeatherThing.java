package org.slim.launcher.pixel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.android.launcher3.Alarm;
import com.android.launcher3.OnAlarmListener;

import java.util.ArrayList;

public class WeatherThing extends BroadcastReceiver implements OnAlarmListener {
    static long INITIAL_LOAD_TIMEOUT = 5000;
    private static WeatherThing instance;
    private final AppWidgetManagerHelper appWidgetManagerHelper;
    private final Alarm mAlarm;
    private final Context mContext;
    private final ArrayList<OnWeatherInfoListener> mListeners;
    private WeatherInfo weatherInfo;
    private boolean weatherInfoLoaded = false;

    WeatherThing (Context context) {
        mContext = context;
        mListeners = new ArrayList<>();
        appWidgetManagerHelper = new AppWidgetManagerHelper(mContext);
        mAlarm = new Alarm();
        mAlarm.setOnAlarmListener(this);
        onReceive(null, null);
        mAlarm.setAlarm(INITIAL_LOAD_TIMEOUT);
        aU(weatherInfo);

        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_DATA_CLEARED);

        context.registerReceiver(this, filter);
    }

    public static WeatherThing getInstance (Context context) {
        if (instance == null) {
            instance = new WeatherThing(context.getApplicationContext());
        }
        return instance;
    }

    private void aU (WeatherInfo gsa) {
        Bundle appWidgetOptions = appWidgetManagerHelper.getAppWidgetOptions();
        if (appWidgetOptions != null) {
            WeatherInfo gsa2 = new WeatherInfo(appWidgetOptions);
            if (gsa2.mRemoteViews != null && gsa2.gsaVersion == gsa.gsaVersion &&
                    gsa2.gsaUpdateTime == gsa.gsaUpdateTime && gsa2.validity() > 0) {
                this.weatherInfo = gsa2;
                onNewWeatherInfo();
            }
        }
    }

    @Override
    public void onReceive (Context context, Intent intent) {
        weatherInfo = new WeatherInfo(mContext, null);
        mContext.sendBroadcast(new Intent("com.google.android.apps.gsa.weatherwidget.ENABLE_UPDATE")
                .setPackage("com.google.android.googlequicksearchbox"));
    }

    private void onNewWeatherInfo () {
        weatherInfoLoaded = true;
        for (OnWeatherInfoListener listener : mListeners) {
            listener.onWeatherInfo(weatherInfo);
        }
        mAlarm.cancelAlarm();
        if (weatherInfo.mRemoteViews != null) {
            mAlarm.setAlarm(weatherInfo.validity());
        }
    }

    public void aT (RemoteViews remoteViews) {
        if (remoteViews != null) {
            weatherInfo = new WeatherInfo(mContext, remoteViews);
            onNewWeatherInfo();
            appWidgetManagerHelper.updateAppWidgetOptions(weatherInfo.toBundle());
        }
    }

    @Override
    public void onAlarm (Alarm alarm) {
        if (weatherInfo.mRemoteViews != null || !weatherInfoLoaded) {
            weatherInfo = new WeatherInfo(mContext, null);
            onNewWeatherInfo();
        }
    }

    public WeatherInfo getWeatherInfoAndAddListener (OnWeatherInfoListener onWeatherInfoListener) {
        mListeners.add(onWeatherInfoListener);
        return weatherInfoLoaded ? weatherInfo : null;
    }

    public void removeListener (OnWeatherInfoListener onWeatherInfoListener) {
        mListeners.remove(onWeatherInfoListener);
    }
}