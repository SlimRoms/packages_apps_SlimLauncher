/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class Stats {
    private static final String TAG = "Launcher3/Stats";

    private static final String STATS_FILE_NAME = "stats.log";
    private static final int STATS_VERSION = 1;
    private static final int INITIAL_STATS_SIZE = 100;

    ArrayList<String> mIntents;
    ArrayList<Integer> mHistogram;

    /**
     * Implemented by containers to provide a launch source for a given child.
     */
    public interface LaunchSourceProvider {
        void fillInLaunchSourceData(Bundle sourceData);
    }

    /**
     * Helpers to add the source to a launch intent.
     */
    public static class LaunchSourceUtils {
        /**
         * Create a default bundle for LaunchSourceProviders to fill in their data.
         */
        public static Bundle createSourceData() {
            Bundle sourceData = new Bundle();
            sourceData.putString(SOURCE_EXTRA_CONTAINER, CONTAINER_HOMESCREEN);
            // Have default container/sub container pages
            sourceData.putInt(SOURCE_EXTRA_CONTAINER_PAGE, 0);
            sourceData.putInt(SOURCE_EXTRA_SUB_CONTAINER_PAGE, 0);
            return sourceData;
        }

        /**
         * Finds the next launch source provider in the parents of the view hierarchy and populates
         * the source data from that provider.
         */
        public static void populateSourceDataFromAncestorProvider(View v, Bundle sourceData) {
            if (v == null) {
                return;
            }

            Stats.LaunchSourceProvider provider = null;
            ViewParent parent = v.getParent();
            while (parent != null && parent instanceof View) {
                if (parent instanceof Stats.LaunchSourceProvider) {
                    provider = (Stats.LaunchSourceProvider) parent;
                    break;
                }
                parent = parent.getParent();
            }

            if (provider != null) {
                provider.fillInLaunchSourceData(sourceData);
            } else if (LauncherAppState.isDogfoodBuild()) {
                throw new RuntimeException("Expected LaunchSourceProvider");
            }
        }
    }

    private static final boolean DEBUG_BROADCASTS = false;

    public static final String ACTION_LAUNCH = "com.android.launcher3.action.LAUNCH";
    public static final String EXTRA_INTENT = "intent";
    public static final String EXTRA_CONTAINER = "container";
    public static final String EXTRA_SCREEN = "screen";
    public static final String EXTRA_CELLX = "cellX";
    public static final String EXTRA_CELLY = "cellY";
    public static final String EXTRA_SOURCE = "source";

    public static final String SOURCE_EXTRA_CONTAINER = "container";
    public static final String SOURCE_EXTRA_CONTAINER_PAGE = "container_page";
    public static final String SOURCE_EXTRA_SUB_CONTAINER = "sub_container";
    public static final String SOURCE_EXTRA_SUB_CONTAINER_PAGE = "sub_container_page";

    public static final String CONTAINER_SEARCH_BOX = "search_box";
    public static final String CONTAINER_ALL_APPS = "all_apps";
    public static final String CONTAINER_HOMESCREEN = "homescreen"; // aka. Workspace
    public static final String CONTAINER_HOTSEAT = "hotseat";

    public static final String SUB_CONTAINER_FOLDER = "folder";
    public static final String SUB_CONTAINER_ALL_APPS_A_Z = "a-z";
    public static final String SUB_CONTAINER_ALL_APPS_PREDICTION = "prediction";
    public static final String SUB_CONTAINER_ALL_APPS_SEARCH = "search";

    private final Launcher mLauncher;
    private final String mLaunchBroadcastPermission;

    public Stats(Launcher launcher) {
        mLauncher = launcher;
        mLaunchBroadcastPermission =
                launcher.getResources().getString(R.string.receive_launch_broadcasts_permission);

        loadStats();

        if (DEBUG_BROADCASTS) {
            launcher.registerReceiver(
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            Log.v("Stats", "got broadcast: " + intent + " for launched intent: "
                                    + intent.getStringExtra(EXTRA_INTENT));
                        }
                    },
                    new IntentFilter(ACTION_LAUNCH),
                    mLaunchBroadcastPermission,
                    null
            );
        }
    }

    public void incrementLaunch(String intentStr) {
        int pos = mIntents.indexOf(intentStr);
        if (pos < 0) {
            mIntents.add(intentStr);
            mHistogram.add(1);
        } else {
            mHistogram.set(pos, mHistogram.get(pos) + 1);
        }
    }

    public int launchCount(Intent intent) {
        intent = new Intent(intent);
        intent.setSourceBounds(null);

        final String flat = intent.toUri(0);
        int pos = mIntents.indexOf(flat);
        if (pos < 0) {
            return 0;
        } else {
            return mHistogram.get(pos);
        }
    }

    public void recordLaunch(View v, Intent intent, ShortcutInfo shortcut) {
        intent = new Intent(intent);
        intent.setSourceBounds(null);

        final String flat = intent.toUri(0);
        Intent broadcastIntent = new Intent(ACTION_LAUNCH).putExtra(EXTRA_INTENT, flat);
        if (shortcut != null) {
            broadcastIntent.putExtra(EXTRA_CONTAINER, shortcut.container)
                    .putExtra(EXTRA_SCREEN, shortcut.screenId)
                    .putExtra(EXTRA_CELLX, shortcut.cellX)
                    .putExtra(EXTRA_CELLY, shortcut.cellY);
        }

        Bundle sourceExtras = LaunchSourceUtils.createSourceData();
        LaunchSourceUtils.populateSourceDataFromAncestorProvider(v, sourceExtras);
        broadcastIntent.putExtra(EXTRA_SOURCE, sourceExtras);
        mLauncher.sendBroadcast(broadcastIntent, mLaunchBroadcastPermission);

        incrementLaunch(flat);
        saveStats();
    }

    private void saveStats() {
        DataOutputStream stats = null;
        try {
            stats = new DataOutputStream(mLauncher.openFileOutput(STATS_FILE_NAME + ".tmp", Context.MODE_PRIVATE));
            stats.writeInt(STATS_VERSION);
            final int N = mHistogram.size();
            stats.writeInt(N);
            for (int i=0; i<N; i++) {
                stats.writeUTF(mIntents.get(i));
                stats.writeInt(mHistogram.get(i));
            }
            stats.close();
            stats = null;
            mLauncher.getFileStreamPath(STATS_FILE_NAME + ".tmp")
                    .renameTo(mLauncher.getFileStreamPath(STATS_FILE_NAME));
        } catch (FileNotFoundException e) {
            Log.e(TAG, "unable to create stats data: " + e);
        } catch (IOException e) {
            Log.e(TAG, "unable to write to stats data: " + e);
        } finally {
            if (stats != null) {
                try {
                    stats.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private void loadStats() {
        mIntents = new ArrayList<>(INITIAL_STATS_SIZE);
        mHistogram = new ArrayList<>(INITIAL_STATS_SIZE);
        DataInputStream stats = null;
        try {
            stats = new DataInputStream(mLauncher.openFileInput(STATS_FILE_NAME));
            final int version = stats.readInt();
            if (version == STATS_VERSION) {
                final int N = stats.readInt();
                for (int i=0; i<N; i++) {
                    final String pkg = stats.readUTF();
                    final int count = stats.readInt();
                    mIntents.add(pkg);
                    mHistogram.add(count);
                }
            }
        } catch (IOException e) {
            // more of a problem

        } finally {
            if (stats != null) {
                try {
                    stats.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
}
