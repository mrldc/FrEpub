package com.folioreader.ui.fragment;

import android.content.ContentResolver;
import android.content.pm.ActivityInfo;
import android.os.Build;
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

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.model.event.ReloadDataEvent;
import com.folioreader.util.AppUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * 设置字体fragment主页面
 */
public class FontFragment extends Fragment {
    private View mRootView;
    private SeekBar seekBar;
    private Config config;
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.view_font, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.i("Bookmark fragment", "onViewCreated: inside onViewCreated ");
        super.onViewCreated(view, savedInstanceState);
        config = AppUtil.getSavedConfig(getActivity());
        mRootView.findViewById(R.id.tv_screen_auto).setEnabled(true);
        mRootView.findViewById(R.id.tv_screen_auto).setSelected(true);
        mRootView.findViewById(R.id.tv_screen_auto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRootView.findViewById(R.id.tv_screen_auto).setEnabled(true);
                mRootView.findViewById(R.id.tv_screen_auto).setSelected(true);
                mRootView.findViewById(R.id.tv_screen_h).setSelected(false);
                mRootView.findViewById(R.id.tv_screen_v).setSelected(false);
                //设置不固定
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);


            }
        });

        mRootView.findViewById(R.id.tv_screen_h).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRootView.findViewById(R.id.tv_screen_h).setEnabled(true);
                mRootView.findViewById(R.id.tv_screen_h).setSelected(true);
                mRootView.findViewById(R.id.tv_screen_auto).setSelected(false);
                mRootView.findViewById(R.id.tv_screen_v).setSelected(false);
                //设置横屏
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        });
        mRootView.findViewById(R.id.tv_screen_v).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRootView.findViewById(R.id.tv_screen_v).setEnabled(true);
                mRootView.findViewById(R.id.tv_screen_v).setSelected(true);
                mRootView.findViewById(R.id.tv_screen_auto).setSelected(false);
                mRootView.findViewById(R.id.tv_screen_h).setSelected(false);
                //设置竖屏
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        });

        mRootView.findViewById(R.id.tv_page_open).setEnabled(true);
        mRootView.findViewById(R.id.tv_page_open).setSelected(true);
        mRootView.findViewById(R.id.tv_page_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRootView.findViewById(R.id.tv_page_open).setEnabled(true);
                mRootView.findViewById(R.id.tv_page_open).setSelected(true);
                mRootView.findViewById(R.id.tv_page_close).setSelected(false);
            }
        });
        mRootView.findViewById(R.id.tv_page_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRootView.findViewById(R.id.tv_page_close).setEnabled(true);
                mRootView.findViewById(R.id.tv_page_close).setSelected(true);
                mRootView.findViewById(R.id.tv_page_open).setSelected(false);
            }
        });
        mRootView.findViewById(R.id.rl_font_bottom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        //字体大小
        SeekBar fontBar = mRootView.findViewById(R.id.seekbar_font);
        fontBar.setMax(80); // 设置最大值为100
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fontBar.setMin(10); // 设置最小值为0
        }
        fontBar.setProgress(config.getFontSize());
        fontBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    config.setFontSize(progress);
                    AppUtil.saveConfig(getActivity(), config);
                    EventBus.getDefault().post(new ReloadDataEvent());
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });
        //边距
        SeekBar paddingBar = mRootView.findViewById(R.id.seekbar_margins);
        paddingBar.setMax(100); // 设置最大值为100
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            paddingBar.setMin(5); // 设置最小值为0
        }
        paddingBar.setProgress(config.getBodyPadding());
        paddingBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    config.setBodyPadding(progress);
                    AppUtil.saveConfig(getActivity(), config);
                    EventBus.getDefault().post(new ReloadDataEvent());
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });
        //行距
        SeekBar textSpaceBar = mRootView.findViewById(R.id.seekbar_space);
        textSpaceBar.setMax(100); // 设置最大值为100
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            textSpaceBar.setMin(5); // 设置最小值为0
        }
        textSpaceBar.setProgress(config.getTextSpace());
        textSpaceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    config.setTextSpace(progress);
                    AppUtil.saveConfig(getActivity(), config);
                    EventBus.getDefault().post(new ReloadDataEvent());
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });
    }

}