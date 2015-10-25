package com.android.launcher3;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class IconPickerActivity extends Activity {

    public static final String SELECTED_RESOURCE_EXTRA = "selected_resource";
    public static final String SELECTED_BITMAP_EXTRA = "bitmap";

    private int mIconSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String pkgName = getIntent().getStringExtra("package");

        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        mIconSize = activityManager.getLauncherLargeIconSize();
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        float dpWidth = metrics.widthPixels / metrics.density;
        int columns = Math.round(dpWidth / Utilities.dpiFromPx(mIconSize, metrics));

        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setBackgroundColor(0x77000000);

        final GridLayoutManager layoutManager = new GridLayoutManager(this, columns);
        recyclerView.setLayoutManager(layoutManager);

        final ImageAdapter adapter = new ImageAdapter(this, pkgName);
        recyclerView.setAdapter(adapter);

        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.isHeader(position) ? layoutManager.getSpanCount() : 1;
            }
        });

        PackageManager pm = getPackageManager();
        ApplicationInfo ai = null;
        try {
            ai = pm.getApplicationInfo(pkgName, 0);
        } catch (NameNotFoundException e) {
            // ignore
        }
        setTitle(ai != null ? pm.getApplicationLabel(ai) : getTitle());
        setContentView(recyclerView);
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public ImageViewHolder(View view) {
            super(view);
            imageView = (ImageView) view;
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        public HeaderViewHolder(View view) {
            super(view);
            textView = (TextView) view;
        }
    }

    public static class Item {
        String title;
        boolean isHeader = false;
        boolean isIcon = false;
        WeakReference<Drawable> drawable;
        int resource_id;
    }

    public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Context mContext;
        private Resources mResources;
        private ArrayList<Item> mItems = new ArrayList<>();
        private String mIconPackageName;

        public final int ITEM_VIEW_TYPE_HEADER = 0;
        public final int ITEM_VIEW_TYPE_ITEM = 1;

        public class FetchDrawable extends AsyncTask<Integer, Void, Drawable> {
            WeakReference<ImageView> mImageView;

            FetchDrawable(ImageView imgView) {
                mImageView = new WeakReference<>(imgView);
            }

            @Override
            protected Drawable doInBackground(Integer... position) {
                Item info = mItems.get(position[0]);
                int itemId = info.resource_id;
                Drawable d = mResources.getDrawable(itemId);
                info.drawable = new WeakReference<>(d);
                return d;
            }

            @Override
            public void onPostExecute(Drawable result) {
                if (mImageView.get() != null) {
                    mImageView.get().setImageDrawable(result);
                }
            }
        }

        public ImageAdapter(Context c, String pkgName) {
            mContext = c;
            mIconPackageName = pkgName;
            mItems = IconPackHelper.getCustomIconPackResources(c, pkgName);
            if (mItems != null && mItems.size() > 0) {
                try {
                    ArrayList<Item> itemsToRemove = new ArrayList<>();
                    mResources = c.getPackageManager().getResourcesForApplication(pkgName);
                    for (Item i : mItems) {
                        int id = mResources.getIdentifier(i.title, "drawable", pkgName);
                        if (id != 0) {
                            i.resource_id = id;
                        } else {
                            if (i.isIcon) {
                                itemsToRemove.add(i);
                            }
                        }
                    }
                    for (Item i : itemsToRemove) {
                        mItems.remove(i);
                    }
                } catch (NameNotFoundException e) {
                    // ignore
                }
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == ITEM_VIEW_TYPE_ITEM) {
                ImageView imageView = new ImageView(parent.getContext());
                imageView.setLayoutParams(new GridLayoutManager.LayoutParams(mIconSize, mIconSize));
                imageView.setPadding(10, 10, 10, 10);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                return new ImageViewHolder(imageView);
            } else if (viewType == ITEM_VIEW_TYPE_HEADER) {
                TextView textView = (TextView) View.inflate(mContext, R.layout.header_view, null);
                return new HeaderViewHolder(textView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            if (mItems.get(position).isHeader) {
                HeaderViewHolder vHolder = (HeaderViewHolder) holder;
                vHolder.textView.setText(mItems.get(position).title);
            } else {
                ImageViewHolder vHolder = (ImageViewHolder) holder;

                FetchDrawable req = new FetchDrawable(vHolder.imageView);
                vHolder.imageView.setTag(req);
                req.execute(position);

                vHolder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent in = new Intent();
                        Item i = mItems.get(position);
                        in.putExtra(SELECTED_RESOURCE_EXTRA,
                                mIconPackageName + "|" + i.title);
                        in.putExtra(SELECTED_BITMAP_EXTRA,
                                ((BitmapDrawable) i.drawable.get()).getBitmap());
                        setResult(Activity.RESULT_OK, in);
                        finish();
                    }
                });
            }
        }

        @Override
        public int getItemViewType(int position) {
            return isHeader(position) ? ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_ITEM;
        }

        public boolean isHeader(int position) {
            return mItems.get(position).isHeader;
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }
}
