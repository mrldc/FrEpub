package com.folioreader.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * @author： libaixing
 * time：2023/7/12 16:27
 * description：输入法
 * updateUser：
 * updateDate：2023/7/12 16:27
 */
public class InputMethodUtils {
    /**
     * 关闭输入法
     * */
    public static void close(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager manager = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager == null) {
            return;
        }
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * 开关输入法
     */
    public static void toggle(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager manager = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager == null) {
            return;
        }
        manager.toggleSoftInput(0, 0);
    }


    public static void show(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager manager = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager == null) {
            return;
        }
        manager.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }
}
