package com.folioreader.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by arthur on 06/10/16.
 */
public class ScreenUtils {

    private Context ctx;
    private DisplayMetrics metrics;

    public ScreenUtils(Context ctx) {
        this.ctx = ctx;
        WindowManager wm = (WindowManager) ctx
                .getSystemService(Context.WINDOW_SERVICE);

        Display display = wm.getDefaultDisplay();
        metrics = new DisplayMetrics();
        display.getMetrics(metrics);

    }

    public int getHeight() {
        return metrics.heightPixels;
    }

    public int getWidth() {
        return metrics.widthPixels;
    }

    public int getRealHeight() {
        return (int)(((float)metrics.heightPixels) / metrics.densityDpi *160);
    }

    public int getRealWidth() {
        return (int)(((float)metrics.widthPixels) / metrics.densityDpi *160);
    }

    public int getDensity() {
        return metrics.densityDpi;
    }

    public int getScale(int picWidth) {
        Display display
                = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        int width = display.getWidth();
        Double val = new Double(width) / new Double(picWidth);
        val = val * 100d;
        return val.intValue();
    }
}
