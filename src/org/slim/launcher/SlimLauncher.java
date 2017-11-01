package org.slim.launcher;

import android.os.Bundle;

import com.android.launcher3.Launcher;
import com.google.android.libraries.launcherclient.LauncherClient;

public class SlimLauncher extends Launcher {

    private static SlimLauncher sLauncher;

    private LauncherClient mLauncherClient;

    public static SlimLauncher getInstance() {
        return sLauncher;
    }


    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        sLauncher = this;

        mLauncherClient = new LauncherClient(this, getPackageName(), true);
        setLauncherOverlay(new LauncherOverlay() {
            @Override
            public void onScrollInteractionBegin() {
                mLauncherClient.startMove();
            }

            @Override
            public void onScrollInteractionEnd() {
                mLauncherClient.endMove();
            }

            @Override
            public void onScrollChange(float progress, boolean rtl) {
                mLauncherClient.updateMove(progress);
            }

            @Override
            public void setOverlayCallbacks(LauncherOverlayCallbacks callbacks) {

            }
        });
    }

    public LauncherClient getClient() {
        return mLauncherClient;
    }

    @Override
    public void onResume() {
        super.onResume();
        mLauncherClient.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mLauncherClient.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mLauncherClient.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mLauncherClient.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sLauncher = null;
        mLauncherClient.onDestroy();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mLauncherClient.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mLauncherClient.onDetachedFromWindow();
    }
}
