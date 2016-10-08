/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.Manifest;
import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.gallery3d.common.BitmapCropTask;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.launcher3.util.Thunk;
import com.android.photos.BitmapRegionTileSource;
import com.android.photos.BitmapRegionTileSource.BitmapSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class WallpaperPickerActivity extends WallpaperCropActivity {
    static final String TAG = "WallpaperPickerActivity";

    public static final int IMAGE_PICK = 5;
    public static final int PICK_WALLPAPER_THIRD_PARTY_ACTIVITY = 6;
    /** An Intent extra used when opening the wallpaper picker from the workspace overlay. */
    public static final String EXTRA_WALLPAPER_OFFSET = "com.android.launcher3.WALLPAPER_OFFSET";
    private static final String TEMP_WALLPAPER_TILES = "TEMP_WALLPAPER_TILES";
    private static final String SELECTED_INDEX = "SELECTED_INDEX";
    private static final int FLAG_POST_DELAY_MILLIS = 200;

    @Thunk View mSelectedTile;
    @Thunk boolean mIgnoreNextTap;
    @Thunk OnClickListener mThumbnailOnClickListener;

    @Thunk LinearLayout mWallpapersView;
    @Thunk HorizontalScrollView mWallpaperScrollContainer;
    @Thunk View mWallpaperStrip;

    @Thunk ActionMode.Callback mActionModeCallback;
    @Thunk ActionMode mActionMode;

    @Thunk View.OnLongClickListener mLongClickListener;

    ArrayList<Uri> mTempWallpaperTiles = new ArrayList<Uri>();
    private SavedWallpaperImages mSavedImages;
    @Thunk int mSelectedIndex = -1;
    private float mWallpaperParallaxOffset;

    public static abstract class WallpaperTileInfo {
        protected View mView;
        public Drawable mThumb;

        public void setView(View v) {
            mView = v;
        }
        public void onClick(WallpaperPickerActivity a) {}
        public void onSave(WallpaperPickerActivity a) {}
        public void onDelete(WallpaperPickerActivity a) {}
        public boolean isSelectable() { return false; }
        public boolean isNamelessWallpaper() { return false; }
        public void onIndexUpdated(CharSequence label) {
            if (isNamelessWallpaper()) {
                mView.setContentDescription(label);
            }
        }
    }

    public static class PickImageInfo extends WallpaperTileInfo {
        @Override
        public void onClick(WallpaperPickerActivity a) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            a.startActivityForResultSafely(intent, IMAGE_PICK);
        }
    }

    public static class UriWallpaperInfo extends WallpaperTileInfo {
        private Uri mUri;
        public UriWallpaperInfo(Uri uri) {
            mUri = uri;
        }
        @Override
        public void onClick(final WallpaperPickerActivity a) {
            a.setWallpaperButtonEnabled(false);
            final BitmapRegionTileSource.UriBitmapSource bitmapSource =
                    new BitmapRegionTileSource.UriBitmapSource(a.getContext(), mUri);
            a.setCropViewTileSource(bitmapSource, true, false, null, new Runnable() {

                @Override
                public void run() {
                    if (bitmapSource.getLoadingState() == BitmapSource.State.LOADED) {
                        a.selectTile(mView);
                        a.setWallpaperButtonEnabled(true);
                    } else {
                        ViewGroup parent = (ViewGroup) mView.getParent();
                        if (parent != null) {
                            parent.removeView(mView);
                            Toast.makeText(a.getContext(), R.string.image_load_fail,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
        @Override
        public void onSave(final WallpaperPickerActivity a) {
            boolean finishActivityWhenDone = true;
            BitmapCropTask.OnBitmapCroppedHandler h = new BitmapCropTask.OnBitmapCroppedHandler() {
                @Override
                public void onBitmapCropped(byte[] imageBytes, Rect hint) {
                    Bitmap thumb = null;
                    Point thumbSize = getDefaultThumbnailSize(a.getResources());
                    if (imageBytes != null) {
                        // rotation is set to 0 since imageBytes has already been correctly rotated
                        thumb = createThumbnail(
                                thumbSize, null, null, imageBytes, null, 0, 0, true);
                        a.getSavedImages().writeImage(thumb, imageBytes);
                    } else {
                        try {
                            // Generate thumb
                            Point size = getDefaultThumbnailSize(a.getResources());
                            Rect finalCropped = new Rect();
                            Utils.getMaxCropRect(hint.width(), hint.height(), size.x, size.y, false)
                                    .roundOut(finalCropped);
                            finalCropped.offset(hint.left, hint.top);

                            InputStream in = a.getContentResolver().openInputStream(mUri);
                            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(in, true);

                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = finalCropped.width() / size.x;
                            thumb = decoder.decodeRegion(finalCropped, options);
                            decoder.recycle();
                            Utils.closeSilently(in);
                            if (thumb != null) {
                                thumb = Bitmap.createScaledBitmap(thumb, size.x, size.y, true);
                            }
                        } catch (IOException e) { }
                        PointF center = a.mCropView.getCenter();

                        Float[] extras = new Float[] {
                                a.mCropView.getScale(),
                                center.x,
                                center.y
                        };
                        a.getSavedImages().writeImage(thumb, mUri, extras);
                    }
                }
            };
            boolean shouldFadeOutOnFinish = a.getWallpaperParallaxOffset() == 0f;
            a.cropImageAndSetWallpaper(mUri, h, finishActivityWhenDone, shouldFadeOutOnFinish);
        }
        @Override
        public boolean isSelectable() {
            return true;
        }
        @Override
        public boolean isNamelessWallpaper() {
            return true;
        }
    }

    public static class FileWallpaperInfo extends WallpaperTileInfo {
        protected File mFile;

        public FileWallpaperInfo(File target, Drawable thumb) {
            mFile = target;
            mThumb = thumb;
        }
        @Override
        public void onClick(final WallpaperPickerActivity a) {
            a.setWallpaperButtonEnabled(false);
            final BitmapRegionTileSource.UriBitmapSource bitmapSource =
                    new BitmapRegionTileSource.UriBitmapSource(a.getContext(), Uri.fromFile(mFile));
            a.setCropViewTileSource(bitmapSource, false, true, getCropViewScaleAndOffsetProvider(),
                    new Runnable() {

                @Override
                public void run() {
                    if (bitmapSource.getLoadingState() == BitmapSource.State.LOADED) {
                        a.setWallpaperButtonEnabled(true);
                    }
                }
            });
        }

        protected CropViewScaleAndOffsetProvider getCropViewScaleAndOffsetProvider() {
            return null;
        }

        @Override
        public void onSave(WallpaperPickerActivity a) {
            boolean shouldFadeOutOnFinish = a.getWallpaperParallaxOffset() == 0f;
            a.setWallpaper(Uri.fromFile(mFile), true, shouldFadeOutOnFinish);
        }
        @Override
        public boolean isSelectable() {
            return true;
        }
        @Override
        public boolean isNamelessWallpaper() {
            return true;
        }
    }

    public static class ResourceWallpaperInfo extends WallpaperTileInfo {
        private Resources mResources;
        private int mResId;

        public ResourceWallpaperInfo(Resources res, int resId, Drawable thumb) {
            mResources = res;
            mResId = resId;
            mThumb = thumb;
        }
        @Override
        public void onClick(final WallpaperPickerActivity a) {
            a.setWallpaperButtonEnabled(false);
            final BitmapRegionTileSource.ResourceBitmapSource bitmapSource =
                    new BitmapRegionTileSource.ResourceBitmapSource(mResources, mResId);
            a.setCropViewTileSource(bitmapSource, false, false, new CropViewScaleAndOffsetProvider() {

                @Override
                public float getScale(Point wallpaperSize, RectF crop) {
                    return wallpaperSize.x /crop.width();
                }

                @Override
                public float getParallaxOffset() {
                    return a.getWallpaperParallaxOffset();
                }
            }, new Runnable() {

                @Override
                public void run() {
                    if (bitmapSource.getLoadingState() == BitmapSource.State.LOADED) {
                        a.setWallpaperButtonEnabled(true);
                    }
                }
            });
        }
        @Override
        public void onSave(WallpaperPickerActivity a) {
            boolean finishActivityWhenDone = true;
            boolean shouldFadeOutOnFinish = true;
            a.cropImageAndSetWallpaper(mResources, mResId, finishActivityWhenDone,
                    shouldFadeOutOnFinish);
        }
        @Override
        public boolean isSelectable() {
            return true;
        }
        @Override
        public boolean isNamelessWallpaper() {
            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static class DefaultWallpaperInfo extends WallpaperTileInfo {
        public DefaultWallpaperInfo(Drawable thumb) {
            mThumb = thumb;
        }
        @Override
        public void onClick(WallpaperPickerActivity a) {
            CropView c = a.getCropView();
            Drawable defaultWallpaper = WallpaperManager.getInstance(a.getContext())
                    .getBuiltInDrawable(c.getWidth(), c.getHeight(), false, 0.5f, 0.5f);
            if (defaultWallpaper == null) {
                Log.w(TAG, "Null default wallpaper encountered.");
                c.setTileSource(null, null);
                return;
            }

            LoadRequest req = new LoadRequest();
            req.moveToLeft = false;
            req.touchEnabled = false;
            req.scaleAndOffsetProvider = new CropViewScaleAndOffsetProvider();
            req.result = new DrawableTileSource(a.getContext(),
                    defaultWallpaper, DrawableTileSource.MAX_PREVIEW_SIZE);
            a.onLoadRequestComplete(req, true);
        }
        @Override
        public void onSave(final WallpaperPickerActivity a) {
            if (!Utilities.ATLEAST_N) {
                try {
                    WallpaperManager.getInstance(a.getContext()).clear();
                    a.setResult(Activity.RESULT_OK);
                } catch (IOException e) {
                    Log.e(TAG, "Setting wallpaper to default threw exception", e);
                } catch (SecurityException e) {
                    Log.w(TAG, "Setting wallpaper to default threw exception", e);
                    // In this case, clearing worked; the exception was thrown afterwards.
                    a.setResult(Activity.RESULT_OK);
                }
                a.finish();
            } else {
                BitmapCropTask.OnEndCropHandler onEndCropHandler
                        = new BitmapCropTask.OnEndCropHandler() {
                    @Override
                    public void run(boolean cropSucceeded) {
                        if (cropSucceeded) {
                            a.setResult(Activity.RESULT_OK);
                        }
                        a.finish();
                    }
                };
                BitmapCropTask setWallpaperTask = getDefaultWallpaperCropTask(a, onEndCropHandler);

                NycWallpaperUtils.executeCropTaskAfterPrompt(a, setWallpaperTask,
                        a.getOnDialogCancelListener());
            }
        }

        @NonNull
        private BitmapCropTask getDefaultWallpaperCropTask(final WallpaperPickerActivity a,
                final BitmapCropTask.OnEndCropHandler onEndCropHandler) {
            return new BitmapCropTask(a, null, null, -1, -1, -1,
                    true, false, onEndCropHandler) {
                @Override
                protected Boolean doInBackground(Integer... params) {
                    int whichWallpaper = params[0];
                    boolean succeeded = true;
                    try {
                        if (whichWallpaper == WallpaperManager.FLAG_LOCK) {
                            Bitmap defaultWallpaper = ((BitmapDrawable) WallpaperManager
                                    .getInstance(a.getApplicationContext()).getBuiltInDrawable())
                                    .getBitmap();
                            ByteArrayOutputStream tmpOut = new ByteArrayOutputStream(2048);
                            if (defaultWallpaper.compress(Bitmap.CompressFormat.PNG, 100,
                                    tmpOut)) {
                                byte[] outByteArray = tmpOut.toByteArray();
                                NycWallpaperUtils.setStream(a.getApplicationContext(),
                                        new ByteArrayInputStream(outByteArray), null, true,
                                        WallpaperManager.FLAG_LOCK);
                            }
                        } else {
                            NycWallpaperUtils.clear(a, whichWallpaper);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Setting wallpaper to default threw exception", e);
                        succeeded = false;
                    } catch (SecurityException e) {
                        Log.w(TAG, "Setting wallpaper to default threw exception", e);
                        // In this case, clearing worked; the exception was thrown afterwards.
                        succeeded = true;
                    }
                    return succeeded;
                }
            };
        }

        @Override
        public boolean isSelectable() {
            return true;
        }
        @Override
        public boolean isNamelessWallpaper() {
            return true;
        }
    }

    /**
     * shows the system wallpaper behind the window and hides the {@link
     * #mCropView} if visible
     * @param visible should the system wallpaper be shown
     */
    protected void setSystemWallpaperVisiblity(final boolean visible) {
        // hide our own wallpaper preview if necessary
        if(!visible) {
            mCropView.setVisibility(View.VISIBLE);
        } else {
            changeWallpaperFlags(visible);
        }
        // the change of the flag must be delayed in order to avoid flickering,
        // a simple post / double post does not suffice here
        mCropView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!visible) {
                    changeWallpaperFlags(visible);
                } else {
                    mCropView.setVisibility(View.INVISIBLE);
                }
            }
        }, FLAG_POST_DELAY_MILLIS);
    }

    @Thunk void changeWallpaperFlags(boolean visible) {
        int desiredWallpaperFlag = visible ? WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER : 0;
        int currentWallpaperFlag = getWindow().getAttributes().flags
                & WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
        if (desiredWallpaperFlag != currentWallpaperFlag) {
            getWindow().setFlags(desiredWallpaperFlag,
                    WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        }
    }

    @Override
    protected void onLoadRequestComplete(LoadRequest req, boolean success) {
        super.onLoadRequestComplete(req, success);
        if (success) {
            setSystemWallpaperVisiblity(false);
        }
    }

    // called by onCreate; this is subclassed to overwrite WallpaperCropActivity
    protected void init() {
        setContentView(R.layout.wallpaper_picker);

        mCropView = (CropView) findViewById(R.id.cropView);
        mCropView.setVisibility(View.INVISIBLE);

        mProgressView = findViewById(R.id.loading);
        mWallpaperScrollContainer = (HorizontalScrollView) findViewById(R.id.wallpaper_scroll_container);
        mWallpaperStrip = findViewById(R.id.wallpaper_strip);
        mCropView.setTouchCallback(new CropView.TouchCallback() {
            ViewPropertyAnimator mAnim;
            @Override
            public void onTouchDown() {
                if (mAnim != null) {
                    mAnim.cancel();
                }
                if (mWallpaperStrip.getAlpha() == 1f) {
                    mIgnoreNextTap = true;
                }
                mAnim = mWallpaperStrip.animate();
                mAnim.alpha(0f)
                    .setDuration(150)
                    .withEndAction(new Runnable() {
                        public void run() {
                            mWallpaperStrip.setVisibility(View.INVISIBLE);
                        }
                    });
                mAnim.setInterpolator(new AccelerateInterpolator(0.75f));
                mAnim.start();
            }
            @Override
            public void onTouchUp() {
                mIgnoreNextTap = false;
            }
            @Override
            public void onTap() {
                boolean ignoreTap = mIgnoreNextTap;
                mIgnoreNextTap = false;
                if (!ignoreTap) {
                    if (mAnim != null) {
                        mAnim.cancel();
                    }
                    mWallpaperStrip.setVisibility(View.VISIBLE);
                    mAnim = mWallpaperStrip.animate();
                    mAnim.alpha(1f)
                         .setDuration(150)
                         .setInterpolator(new DecelerateInterpolator(0.75f));
                    mAnim.start();
                }
            }
        });

        mThumbnailOnClickListener = new OnClickListener() {
            public void onClick(View v) {
                if (mActionMode != null) {
                    // When CAB is up, clicking toggles the item instead
                    if (v.isLongClickable()) {
                        mLongClickListener.onLongClick(v);
                    }
                    return;
                }
                WallpaperTileInfo info = (WallpaperTileInfo) v.getTag();
                if (info.isSelectable() && v.getVisibility() == View.VISIBLE) {
                    selectTile(v);
                    setWallpaperButtonEnabled(true);
                }
                info.onClick(WallpaperPickerActivity.this);
            }
        };
        mLongClickListener = new View.OnLongClickListener() {
            // Called when the user long-clicks on someView
            public boolean onLongClick(View view) {
                CheckableFrameLayout c = (CheckableFrameLayout) view;
                c.toggle();

                if (mActionMode != null) {
                    mActionMode.invalidate();
                } else {
                    // Start the CAB using the ActionMode.Callback defined below
                    mActionMode = startActionMode(mActionModeCallback);
                    int childCount = mWallpapersView.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        mWallpapersView.getChildAt(i).setSelected(false);
                    }
                }
                return true;
            }
        };

        mWallpaperParallaxOffset = getIntent().getFloatExtra(EXTRA_WALLPAPER_OFFSET, 0);

        // Populate the built-in wallpapers
        ArrayList<WallpaperTileInfo> wallpapers = findBundledWallpapers();
        mWallpapersView = (LinearLayout) findViewById(R.id.wallpaper_list);
        SimpleWallpapersAdapter ia = new SimpleWallpapersAdapter(getContext(), wallpapers);
        populateWallpapersFromAdapter(mWallpapersView, ia, false);

        // Populate the saved wallpapers
        mSavedImages = new SavedWallpaperImages(getContext());
        mSavedImages.loadThumbnailsAndImageIdList();
        populateWallpapersFromAdapter(mWallpapersView, mSavedImages, true);

        // Populate the live wallpapers
        final LinearLayout liveWallpapersView =
                (LinearLayout) findViewById(R.id.live_wallpaper_list);
        final LiveWallpaperListAdapter a = new LiveWallpaperListAdapter(getContext());
        a.registerDataSetObserver(new DataSetObserver() {
            public void onChanged() {
                liveWallpapersView.removeAllViews();
                populateWallpapersFromAdapter(liveWallpapersView, a, false);
                initializeScrollForRtl();
                updateTileIndices();
            }
        });

        // Populate the third-party wallpaper pickers
        final LinearLayout thirdPartyWallpapersView =
                (LinearLayout) findViewById(R.id.third_party_wallpaper_list);
        final ThirdPartyWallpaperPickerListAdapter ta =
                new ThirdPartyWallpaperPickerListAdapter(getContext());
        populateWallpapersFromAdapter(thirdPartyWallpapersView, ta, false);

        // Add a tile for the Gallery
        LinearLayout masterWallpaperList = (LinearLayout) findViewById(R.id.master_wallpaper_list);
        FrameLayout pickImageTile = (FrameLayout) getLayoutInflater().
                inflate(R.layout.wallpaper_picker_image_picker_item, masterWallpaperList, false);
        masterWallpaperList.addView(pickImageTile, 0);

        // Make its background the last photo taken on external storage
        Bitmap lastPhoto = getThumbnailOfLastPhoto();
        if (lastPhoto != null) {
            ImageView galleryThumbnailBg =
                    (ImageView) pickImageTile.findViewById(R.id.wallpaper_image);
            galleryThumbnailBg.setImageBitmap(lastPhoto);
            int colorOverlay = getResources().getColor(R.color.wallpaper_picker_translucent_gray);
            galleryThumbnailBg.setColorFilter(colorOverlay, PorterDuff.Mode.SRC_ATOP);
        }

        PickImageInfo pickImageInfo = new PickImageInfo();
        pickImageTile.setTag(pickImageInfo);
        pickImageInfo.setView(pickImageTile);
        pickImageTile.setOnClickListener(mThumbnailOnClickListener);

        // Select the first item; wait for a layout pass so that we initialize the dimensions of
        // cropView or the defaultWallpaperView first
        mCropView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if ((right - left) > 0 && (bottom - top) > 0) {
                    if (mSelectedIndex >= 0 && mSelectedIndex < mWallpapersView.getChildCount()) {
                        mThumbnailOnClickListener.onClick(
                                mWallpapersView.getChildAt(mSelectedIndex));
                        setSystemWallpaperVisiblity(false);
                    }
                    v.removeOnLayoutChangeListener(this);
                }
            }
        });

        updateTileIndices();

        // Update the scroll for RTL
        initializeScrollForRtl();

        // Create smooth layout transitions for when items are deleted
        final LayoutTransition transitioner = new LayoutTransition();
        transitioner.setDuration(200);
        transitioner.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
        transitioner.setAnimator(LayoutTransition.DISAPPEARING, null);
        mWallpapersView.setLayoutTransition(transitioner);

        // Action bar
        // Show the custom action bar view
        final ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.actionbar_set_wallpaper);
        actionBar.getCustomView().setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Ensure that a tile is selected and loaded.
                        if (mSelectedTile != null && mCropView.getTileSource() != null) {
                            // Prevent user from selecting any new tile.
                            mWallpaperStrip.setVisibility(View.GONE);
                            actionBar.hide();

                            WallpaperTileInfo info = (WallpaperTileInfo) mSelectedTile.getTag();
                            info.onSave(WallpaperPickerActivity.this);
                        } else {
                            // This case shouldn't be possible, since "Set wallpaper" is disabled
                            // until user clicks on a title.
                            Log.w(TAG, "\"Set wallpaper\" was clicked when no tile was selected");
                        }
                    }
                });
        mSetWallpaperButton = findViewById(R.id.set_wallpaper_button);

        // CAB for deleting items
        mActionModeCallback = new ActionMode.Callback() {
            // Called when the action mode is created; startActionMode() was called
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate a menu resource providing context menu items
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.cab_delete_wallpapers, menu);
                return true;
            }

            private int numCheckedItems() {
                int childCount = mWallpapersView.getChildCount();
                int numCheckedItems = 0;
                for (int i = 0; i < childCount; i++) {
                    CheckableFrameLayout c = (CheckableFrameLayout) mWallpapersView.getChildAt(i);
                    if (c.isChecked()) {
                        numCheckedItems++;
                    }
                }
                return numCheckedItems;
            }

            // Called each time the action mode is shown. Always called after onCreateActionMode,
            // but may be called multiple times if the mode is invalidated.
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                int numCheckedItems = numCheckedItems();
                if (numCheckedItems == 0) {
                    mode.finish();
                    return true;
                } else {
                    mode.setTitle(getResources().getQuantityString(
                            R.plurals.number_of_items_selected, numCheckedItems, numCheckedItems));
                    return true;
                }
            }

            // Called when the user selects a contextual menu item
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_delete) {
                    int childCount = mWallpapersView.getChildCount();
                    ArrayList<View> viewsToRemove = new ArrayList<View>();
                    boolean selectedTileRemoved = false;
                    for (int i = 0; i < childCount; i++) {
                        CheckableFrameLayout c =
                                (CheckableFrameLayout) mWallpapersView.getChildAt(i);
                        if (c.isChecked()) {
                            WallpaperTileInfo info = (WallpaperTileInfo) c.getTag();
                            info.onDelete(WallpaperPickerActivity.this);
                            viewsToRemove.add(c);
                            if (i == mSelectedIndex) {
                                selectedTileRemoved = true;
                            }
                        }
                    }
                    for (View v : viewsToRemove) {
                        mWallpapersView.removeView(v);
                    }
                    if (selectedTileRemoved) {
                        mSelectedIndex = -1;
                        mSelectedTile = null;
                        setSystemWallpaperVisiblity(true);
                    }
                    updateTileIndices();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                } else {
                    return false;
                }
            }

            // Called when the user exits the action mode
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                int childCount = mWallpapersView.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    CheckableFrameLayout c = (CheckableFrameLayout) mWallpapersView.getChildAt(i);
                    c.setChecked(false);
                }
                if (mSelectedTile != null) {
                    mSelectedTile.setSelected(true);
                }
                mActionMode = null;
            }
        };
    }

    public void setWallpaperButtonEnabled(boolean enabled) {
        mSetWallpaperButton.setEnabled(enabled);
    }

    public float getWallpaperParallaxOffset() {
        return mWallpaperParallaxOffset;
    }

    @Thunk void selectTile(View v) {
        if (mSelectedTile != null) {
            mSelectedTile.setSelected(false);
            mSelectedTile = null;
        }
        mSelectedTile = v;
        v.setSelected(true);
        mSelectedIndex = mWallpapersView.indexOfChild(v);
        // TODO: Remove this once the accessibility framework and
        // services have better support for selection state.
        v.announceForAccessibility(
                getContext().getString(R.string.announce_selection, v.getContentDescription()));
    }

    @Thunk void initializeScrollForRtl() {
        if (Utilities.isRtl(getResources())) {
            final ViewTreeObserver observer = mWallpaperScrollContainer.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    LinearLayout masterWallpaperList =
                            (LinearLayout) findViewById(R.id.master_wallpaper_list);
                    mWallpaperScrollContainer.scrollTo(masterWallpaperList.getWidth(), 0);
                    mWallpaperScrollContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
    }

    protected Bitmap getThumbnailOfLastPhoto() {
        boolean canReadExternalStorage = getActivity().checkPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE, Process.myPid(), Process.myUid()) ==
                PackageManager.PERMISSION_GRANTED;

        if (!canReadExternalStorage) {
            // MediaStore.Images.Media.EXTERNAL_CONTENT_URI requires
            // the READ_EXTERNAL_STORAGE permission
            return null;
        }

        Cursor cursor = MediaStore.Images.Media.query(getContext().getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.DATE_TAKEN},
                null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC LIMIT 1");

        Bitmap thumb = null;
        if (cursor != null) {
            if (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                thumb = MediaStore.Images.Thumbnails.getThumbnail(getContext().getContentResolver(),
                        id, MediaStore.Images.Thumbnails.MINI_KIND, null);
            }
            cursor.close();
        }
        return thumb;
    }

    public void onStop() {
        super.onStop();
        mWallpaperStrip = findViewById(R.id.wallpaper_strip);
        if (mWallpaperStrip != null && mWallpaperStrip.getAlpha() < 1f) {
            mWallpaperStrip.setAlpha(1f);
            mWallpaperStrip.setVisibility(View.VISIBLE);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(TEMP_WALLPAPER_TILES, mTempWallpaperTiles);
        outState.putInt(SELECTED_INDEX, mSelectedIndex);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        ArrayList<Uri> uris = savedInstanceState.getParcelableArrayList(TEMP_WALLPAPER_TILES);
        for (Uri uri : uris) {
            addTemporaryWallpaperTile(uri, true);
        }
        mSelectedIndex = savedInstanceState.getInt(SELECTED_INDEX, -1);
    }

    @Thunk void populateWallpapersFromAdapter(ViewGroup parent, BaseAdapter adapter,
            boolean addLongPressHandler) {
        for (int i = 0; i < adapter.getCount(); i++) {
            FrameLayout thumbnail = (FrameLayout) adapter.getView(i, null, parent);
            parent.addView(thumbnail, i);
            WallpaperTileInfo info = (WallpaperTileInfo) adapter.getItem(i);
            thumbnail.setTag(info);
            info.setView(thumbnail);
            if (addLongPressHandler) {
                addLongPressHandler(thumbnail);
            }
            thumbnail.setOnClickListener(mThumbnailOnClickListener);
        }
    }

    @Thunk void updateTileIndices() {
        LinearLayout masterWallpaperList = (LinearLayout) findViewById(R.id.master_wallpaper_list);
        final int childCount = masterWallpaperList.getChildCount();
        final Resources res = getResources();

        // Do two passes; the first pass gets the total number of tiles
        int numTiles = 0;
        for (int passNum = 0; passNum < 2; passNum++) {
            int tileIndex = 0;
            for (int i = 0; i < childCount; i++) {
                View child = masterWallpaperList.getChildAt(i);
                LinearLayout subList;

                int subListStart;
                int subListEnd;
                if (child.getTag() instanceof WallpaperTileInfo) {
                    subList = masterWallpaperList;
                    subListStart = i;
                    subListEnd = i + 1;
                } else { // if (child instanceof LinearLayout) {
                    subList = (LinearLayout) child;
                    subListStart = 0;
                    subListEnd = subList.getChildCount();
                }

                for (int j = subListStart; j < subListEnd; j++) {
                    WallpaperTileInfo info = (WallpaperTileInfo) subList.getChildAt(j).getTag();
                    if (info.isNamelessWallpaper()) {
                        if (passNum == 0) {
                            numTiles++;
                        } else {
                            CharSequence label = res.getString(
                                    R.string.wallpaper_accessibility_name, ++tileIndex, numTiles);
                            info.onIndexUpdated(label);
                        }
                    }
                }
            }
        }
    }

    @Thunk static Point getDefaultThumbnailSize(Resources res) {
        return new Point(res.getDimensionPixelSize(R.dimen.wallpaperThumbnailWidth),
                res.getDimensionPixelSize(R.dimen.wallpaperThumbnailHeight));

    }

    @Thunk static Bitmap createThumbnail(Point size, Context context, Uri uri, byte[] imageBytes,
            Resources res, int resId, int rotation, boolean leftAligned) {
        int width = size.x;
        int height = size.y;

        BitmapCropTask cropTask;
        if (uri != null) {
            cropTask = new BitmapCropTask(
                    context, uri, null, rotation, width, height, false, true, null);
        } else if (imageBytes != null) {
            cropTask = new BitmapCropTask(
                    imageBytes, null, rotation, width, height, false, true, null);
        }  else {
            cropTask = new BitmapCropTask(
                    context, res, resId, null, rotation, width, height, false, true, null);
        }
        Point bounds = cropTask.getImageBounds();
        if (bounds == null || bounds.x == 0 || bounds.y == 0) {
            return null;
        }

        Matrix rotateMatrix = new Matrix();
        rotateMatrix.setRotate(rotation);
        float[] rotatedBounds = new float[] { bounds.x, bounds.y };
        rotateMatrix.mapPoints(rotatedBounds);
        rotatedBounds[0] = Math.abs(rotatedBounds[0]);
        rotatedBounds[1] = Math.abs(rotatedBounds[1]);

        RectF cropRect = Utils.getMaxCropRect(
                (int) rotatedBounds[0], (int) rotatedBounds[1], width, height, leftAligned);
        cropTask.setCropBounds(cropRect);

        if (cropTask.cropBitmap(WallpaperManager.FLAG_SYSTEM)) {
            return cropTask.getCroppedBitmap();
        } else {
            return null;
        }
    }

    private void addTemporaryWallpaperTile(final Uri uri, boolean fromRestore) {
        // Add a tile for the image picked from Gallery, reusing the existing tile if there is one.
        FrameLayout existingImageThumbnail = null;
        int indexOfExistingTile = 0;
        for (; indexOfExistingTile < mWallpapersView.getChildCount(); indexOfExistingTile++) {
            FrameLayout thumbnail = (FrameLayout) mWallpapersView.getChildAt(indexOfExistingTile);
            Object tag = thumbnail.getTag();
            if (tag instanceof UriWallpaperInfo && ((UriWallpaperInfo) tag).mUri.equals(uri)) {
                existingImageThumbnail = thumbnail;
                break;
            }
        }
        final FrameLayout pickedImageThumbnail;
        if (existingImageThumbnail != null) {
            pickedImageThumbnail = existingImageThumbnail;
            // Always move the existing wallpaper to the front so user can see it without scrolling.
            mWallpapersView.removeViewAt(indexOfExistingTile);
            mWallpapersView.addView(existingImageThumbnail, 0);
        } else {
            // This is the first time this temporary wallpaper has been added
            pickedImageThumbnail = (FrameLayout) getLayoutInflater()
                    .inflate(R.layout.wallpaper_picker_item, mWallpapersView, false);
            pickedImageThumbnail.setVisibility(View.GONE);
            mWallpapersView.addView(pickedImageThumbnail, 0);
            mTempWallpaperTiles.add(uri);
        }

        // Load the thumbnail
        final ImageView image = (ImageView) pickedImageThumbnail.findViewById(R.id.wallpaper_image);
        final Point defaultSize = getDefaultThumbnailSize(this.getResources());
        final Context context = getContext();
        new AsyncTask<Void, Bitmap, Bitmap>() {
            protected Bitmap doInBackground(Void...args) {
                try {
                    int rotation = BitmapUtils.getRotationFromExif(context, uri);
                    return createThumbnail(defaultSize, context, uri, null, null, 0, rotation,
                            false);
                } catch (SecurityException securityException) {
                    if (isActivityDestroyed()) {
                        // Temporarily granted permissions are revoked when the activity
                        // finishes, potentially resulting in a SecurityException here.
                        // Even though {@link #isDestroyed} might also return true in different
                        // situations where the configuration changes, we are fine with
                        // catching these cases here as well.
                        cancel(false);
                    } else {
                        // otherwise it had a different cause and we throw it further
                        throw securityException;
                    }
                    return null;
                }
            }
            protected void onPostExecute(Bitmap thumb) {
                if (!isCancelled() && thumb != null) {
                    image.setImageBitmap(thumb);
                    Drawable thumbDrawable = image.getDrawable();
                    thumbDrawable.setDither(true);
                    pickedImageThumbnail.setVisibility(View.VISIBLE);
                } else {
                    Log.e(TAG, "Error loading thumbnail for uri=" + uri);
                }
            }
        }.execute();

        UriWallpaperInfo info = new UriWallpaperInfo(uri);
        pickedImageThumbnail.setTag(info);
        info.setView(pickedImageThumbnail);
        addLongPressHandler(pickedImageThumbnail);
        updateTileIndices();
        pickedImageThumbnail.setOnClickListener(mThumbnailOnClickListener);
        if (!fromRestore) {
            mThumbnailOnClickListener.onClick(pickedImageThumbnail);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                addTemporaryWallpaperTile(uri, false);
            }
        } else if (requestCode == PICK_WALLPAPER_THIRD_PARTY_ACTIVITY
                && resultCode == Activity.RESULT_OK) {
            // Something was set on the third-party activity.
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    private void addLongPressHandler(View v) {
        v.setOnLongClickListener(mLongClickListener);

        // Enable stylus button to also trigger long click.
        final StylusEventHelper stylusEventHelper = new StylusEventHelper(v);
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return stylusEventHelper.checkAndPerformStylusEvent(event);
            }
        });
    }

    private ArrayList<WallpaperTileInfo> findBundledWallpapers() {
        final PackageManager pm = getContext().getPackageManager();
        final ArrayList<WallpaperTileInfo> bundled = new ArrayList<WallpaperTileInfo>(24);

        List<Partner> partners = Partner.getAllPartners(pm);
        boolean hideDefault = false;
        if (partners != null) {
            for (Partner partner : partners) {
                final Resources partnerRes = partner.getResources();
                final int resId = partnerRes.getIdentifier(Partner.RES_WALLPAPERS, "array",
                        partner.getPackageName());
                if (resId != 0) {
                    addWallpapers(bundled, partnerRes, partner.getPackageName(), resId);
                }

                // Add system wallpapers
                File systemDir = partner.getWallpaperDirectory();
                if (systemDir != null && systemDir.isDirectory()) {
                    for (File file : systemDir.listFiles()) {
                        if (!file.isFile()) {
                            continue;
                        }
                        String name = file.getName();
                        int dotPos = name.lastIndexOf('.');
                        String extension = "";
                        if (dotPos >= -1) {
                            extension = name.substring(dotPos);
                            name = name.substring(0, dotPos);
                        }

                        if (name.endsWith("_small")) {
                            // it is a thumbnail
                            continue;
                        }

                        File thumbnail = new File(systemDir, name + "_small" + extension);
                        Bitmap thumb = BitmapFactory.decodeFile(thumbnail.getAbsolutePath());
                        if (thumb != null) {
                            bundled.add(new FileWallpaperInfo(file, new BitmapDrawable(thumb)));
                        }
                    }
                }
                if (partner.hideDefaultWallpaper()) {
                    hideDefault = true;
                }
            }
        }

        Pair<ApplicationInfo, Integer> r = getWallpaperArrayResourceId();
        if (r != null) {
            try {
                Resources wallpaperRes = getContext().getPackageManager()
                        .getResourcesForApplication(r.first);
                addWallpapers(bundled, wallpaperRes, r.first.packageName, r.second);
            } catch (PackageManager.NameNotFoundException e) {
            }
        }

        if (!hideDefault) {
            // Add an entry for the default wallpaper (stored in system resources)
            WallpaperTileInfo defaultWallpaperInfo = Utilities.ATLEAST_KITKAT
                    ? getDefaultWallpaper() : getPreKKDefaultWallpaperInfo();
            if (defaultWallpaperInfo != null) {
                bundled.add(0, defaultWallpaperInfo);
            }
        }
        return bundled;
    }

    private boolean writeImageToFileAsJpeg(File f, Bitmap b) {
        try {
            f.createNewFile();
            FileOutputStream thumbFileStream =
                    getContext().openFileOutput(f.getName(), Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.JPEG, 95, thumbFileStream);
            thumbFileStream.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error while writing bitmap to file " + e);
            f.delete();
        }
        return false;
    }

    private File getDefaultThumbFile() {
        return new File(getContext().getFilesDir(), Build.VERSION.SDK_INT
                + "_" + LauncherFiles.DEFAULT_WALLPAPER_THUMBNAIL);
    }

    private boolean saveDefaultWallpaperThumb(Bitmap b) {
        // Delete old thumbnails.
        new File(getContext().getFilesDir(), LauncherFiles.DEFAULT_WALLPAPER_THUMBNAIL_OLD).delete();
        new File(getContext().getFilesDir(), LauncherFiles.DEFAULT_WALLPAPER_THUMBNAIL).delete();

        for (int i = Build.VERSION_CODES.JELLY_BEAN; i < Build.VERSION.SDK_INT; i++) {
            new File(getContext().getFilesDir(), i + "_"
                    + LauncherFiles.DEFAULT_WALLPAPER_THUMBNAIL).delete();
        }
        return writeImageToFileAsJpeg(getDefaultThumbFile(), b);
    }

    private ResourceWallpaperInfo getPreKKDefaultWallpaperInfo() {
        Resources sysRes = Resources.getSystem();
        int resId = sysRes.getIdentifier("default_wallpaper", "drawable", "android");

        File defaultThumbFile = getDefaultThumbFile();
        Bitmap thumb = null;
        boolean defaultWallpaperExists = false;
        if (defaultThumbFile.exists()) {
            thumb = BitmapFactory.decodeFile(defaultThumbFile.getAbsolutePath());
            defaultWallpaperExists = true;
        } else {
            Resources res = getResources();
            Point defaultThumbSize = getDefaultThumbnailSize(res);
            int rotation = BitmapUtils.getRotationFromExif(res, resId);
            thumb = createThumbnail(
                    defaultThumbSize, getContext(), null, null, sysRes, resId, rotation, false);
            if (thumb != null) {
                defaultWallpaperExists = saveDefaultWallpaperThumb(thumb);
            }
        }
        if (defaultWallpaperExists) {
            return new ResourceWallpaperInfo(sysRes, resId, new BitmapDrawable(thumb));
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private DefaultWallpaperInfo getDefaultWallpaper() {
        File defaultThumbFile = getDefaultThumbFile();
        Bitmap thumb = null;
        boolean defaultWallpaperExists = false;
        if (defaultThumbFile.exists()) {
            thumb = BitmapFactory.decodeFile(defaultThumbFile.getAbsolutePath());
            defaultWallpaperExists = true;
        } else {
            Resources res = getResources();
            Point defaultThumbSize = getDefaultThumbnailSize(res);
            Drawable wallpaperDrawable = WallpaperManager.getInstance(getContext()).getBuiltInDrawable(
                    defaultThumbSize.x, defaultThumbSize.y, true, 0.5f, 0.5f);
            if (wallpaperDrawable != null) {
                thumb = Bitmap.createBitmap(
                        defaultThumbSize.x, defaultThumbSize.y, Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(thumb);
                wallpaperDrawable.setBounds(0, 0, defaultThumbSize.x, defaultThumbSize.y);
                wallpaperDrawable.draw(c);
                c.setBitmap(null);
            }
            if (thumb != null) {
                defaultWallpaperExists = saveDefaultWallpaperThumb(thumb);
            }
        }
        if (defaultWallpaperExists) {
            return new DefaultWallpaperInfo(new BitmapDrawable(thumb));
        }
        return null;
    }

    public Pair<ApplicationInfo, Integer> getWallpaperArrayResourceId() {
        // Context.getPackageName() may return the "original" package name,
        // com.android.launcher3; Resources needs the real package name,
        // com.android.launcher3. So we ask Resources for what it thinks the
        // package name should be.
        final String packageName = getResources().getResourcePackageName(R.array.wallpapers);
        try {
            ApplicationInfo info = getContext().getPackageManager().getApplicationInfo(packageName, 0);
            return new Pair<ApplicationInfo, Integer>(info, R.array.wallpapers);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private void addWallpapers(ArrayList<WallpaperTileInfo> known, Resources res,
            String packageName, int listResId) {
        final String[] extras = res.getStringArray(listResId);
        for (String extra : extras) {
            int resId = res.getIdentifier(extra, "drawable", packageName);
            if (resId != 0) {
                final int thumbRes = res.getIdentifier(extra + "_small", "drawable", packageName);

                if (thumbRes != 0) {
                    ResourceWallpaperInfo wallpaperInfo =
                            new ResourceWallpaperInfo(res, resId, res.getDrawable(thumbRes));
                    known.add(wallpaperInfo);
                    // Log.d(TAG, "add: [" + packageName + "]: " + extra + " (" + res + ")");
                }
            } else {
                Log.e(TAG, "Couldn't find wallpaper " + extra);
            }
        }
    }

    public CropView getCropView() {
        return mCropView;
    }

    public SavedWallpaperImages getSavedImages() {
        return mSavedImages;
    }

    private static class SimpleWallpapersAdapter extends ArrayAdapter<WallpaperTileInfo> {
        private final LayoutInflater mLayoutInflater;

        SimpleWallpapersAdapter(Context context, ArrayList<WallpaperTileInfo> wallpapers) {
            super(context, R.layout.wallpaper_picker_item, wallpapers);
            mLayoutInflater = LayoutInflater.from(context);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Drawable thumb = getItem(position).mThumb;
            if (thumb == null) {
                Log.e(TAG, "Error decoding thumbnail for wallpaper #" + position);
            }
            return createImageTileView(mLayoutInflater, convertView, parent, thumb);
        }
    }

    public static View createImageTileView(LayoutInflater layoutInflater,
            View convertView, ViewGroup parent, Drawable thumb) {
        View view;

        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.wallpaper_picker_item, parent, false);
        } else {
            view = convertView;
        }

        ImageView image = (ImageView) view.findViewById(R.id.wallpaper_image);

        if (thumb != null) {
            image.setImageDrawable(thumb);
            thumb.setDither(true);
        }

        return view;
    }

    public void startActivityForResultSafely(Intent intent, int requestCode) {
        Utilities.startActivityForResultSafely(getActivity(), intent, requestCode);
    }

    @Override
    public boolean enableRotation() {
        return true;
    }
}
