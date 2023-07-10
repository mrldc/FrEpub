package com.folioreader.ui.fragment;

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.FolioReader;
import com.folioreader.R;
import com.folioreader.util.AppUtil;

/**
 * 设置亮度fragment主页面
 */
public class LightFragment extends Fragment {
    private View mRootView;
    private SeekBar seekBar;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.view_light, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.i("Bookmark fragment", "onViewCreated: inside onViewCreated ");
        super.onViewCreated(view, savedInstanceState);
        Config config = AppUtil.getSavedConfig(getActivity());
        seekBar = (SeekBar) mRootView.findViewById(R.id.seekbar_light);
        // #FFFFFF
        mRootView.findViewById(R.id.iv_light_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        // #EDEAE4
        mRootView.findViewById(R.id.iv_light_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        //#E9F4E3
        mRootView.findViewById(R.id.iv_light_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        // #E3F4F3
        mRootView.findViewById(R.id.iv_light_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //设置滚动条
        seekBar.setProgress(getCurrentBrightValue());
        seekBar.setMax(255);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * 拖动中数值的时候
             * @param fromUser 是否是由用户操作的
             */
            @Override

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (progress > 3 && fromUser) {//以免太暗
                    WindowManager.LayoutParams layoutParams = getActivity().getWindow().getAttributes();
                    layoutParams.screenBrightness = (float) progress / 255;//因为这个值是[0, 1]范围的
                    getActivity().getWindow().setAttributes(layoutParams);
                }
            }

            /**
             * 当按下的时候
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                System.out.println("com.example.screenBrightnessTest.MyActivity.onStartTrackingTouch");

            }

            /**
             *当松开的时候
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println("com.example.screenBrightnessTest.MyActivity.onStopTrackingTouch");
            }
        });
    }


    /**

     * 仅当系统的亮度模式是非自动模式的情况下，获取当前屏幕亮度值[0, 255].

     * 如果是自动模式，那么该方法获得的值不正确。

     */

    private int getCurrentBrightValue() {

        int nowBrightnessValue = 0;
        ContentResolver resolver = getActivity().getContentResolver();
        try {
            nowBrightnessValue = android.provider.Settings.System.getInt(resolver,
                    Settings.System.SCREEN_BRIGHTNESS, 255);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nowBrightnessValue;
    }
}