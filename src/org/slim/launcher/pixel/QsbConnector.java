package org.slim.launcher.pixel;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.android.launcher3.R;

public class QsbConnector extends View {
    private static final Property bu = new C0284i(Integer.class, "overlayAlpha");
    private int bv;
    private ObjectAnimator bw;
    private final int bx;
    private final BroadcastReceiver by;

    public QsbConnector(Context context) {
        this(context, null);
    }

    public QsbConnector(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public QsbConnector(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        by = new C0285j(this);
        bv = 0;
        bx = getResources().getColor(R.color.qsb_background) & 16777215;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        be();
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        getContext().registerReceiver(by, filter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            getContext().unregisterReceiver(by);
        } catch (IllegalArgumentException ignored) {
            // Not supposed to happen but we'll ignore it
        }
    }

    private void be() {
        Drawable drawable = null;
        try {
            Resources resourcesForApplication = getContext().getPackageManager().getResourcesForApplication("com.google.android.googlequicksearchbox");
            int identifier = resourcesForApplication.getIdentifier("bg_pixel_qsb_connector", "drawable", "com.google.android.googlequicksearchbox");
            if (identifier != 0) {
                drawable = resourcesForApplication.getDrawable(identifier, getContext().getTheme());
            }
        } catch (Throwable e) {
            Log.d("QsbConnector", "Error loading connector background", e);
        }
        if (drawable == null) {
            setBackgroundResource(R.color.qsb_connector);
        } else {
            setBackground(drawable);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bv > 0) {
            canvas.drawColor(ColorUtils.setAlphaComponent(bx, bv));
        }
    }

    @Override
    protected boolean onSetAlpha(int i) {
        if (i == 0) {
            bd();
        }
        return super.onSetAlpha(i);
    }

    public void bc(boolean z) {
        if (z) {
            bd();
            bf(255);
            bw = ObjectAnimator.ofInt(this, bu, 0);
            bw.setInterpolator(new AccelerateDecelerateInterpolator());
            bw.start();
            return;
        }
        bf(0);
    }

    private void bd() {
        if (bw != null) {
            bw.end();
            bw = null;
        }
    }

    private void bf(int i) {
        if (bv != i) {
            bv = i;
            invalidate();
        }
    }

    final class C0285j extends BroadcastReceiver {
        final /* synthetic */ QsbConnector co;

        C0285j(QsbConnector qsbConnector) {
            co = qsbConnector;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            co.be();
        }
    }

    final static class C0284i extends Property {
        C0284i(Class cls, String str) {
            super(cls, str);
        }

        @Override
        public /* bridge */ /* synthetic */ Object get(Object obj) {
            return bR((QsbConnector) obj);
        }

        public Integer bR(QsbConnector qsbConnector) {
            return qsbConnector.bv;
        }

        @Override
        public /* bridge */ /* synthetic */ void set(Object obj, Object obj2) {
            bS((QsbConnector) obj, (Integer) obj2);
        }

        public void bS(QsbConnector qsbConnector, Integer num) {
            qsbConnector.bf(num);
        }
    }
}