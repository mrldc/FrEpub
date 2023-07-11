package com.folioreader.ui.fragment;

import android.content.ContentResolver;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.callback.FontsCallback;
import com.folioreader.model.event.BookPageEvent;
import com.folioreader.model.event.ReloadDataEvent;
import com.folioreader.ui.adapter.FontAdapter;
import com.folioreader.ui.adapter.FontGridAdapter;
import com.folioreader.util.AppUtil;
import com.folioreader.util.FontFinder;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 设置字体fragment主页面
 */
public class FontFragment extends Fragment implements FontsCallback {
    private View mRootView;
    private SeekBar seekBar;
    private Config config;
    TextView fontTextView;
    LinearLayout ll_font_select;
    RecyclerView fontsRecyclerView;
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

        //初始化横竖屏按钮
        if (config.getScreenOrientation() == 1){
            //竖屏
            mRootView.findViewById(R.id.tv_screen_v).setEnabled(true);
            mRootView.findViewById(R.id.tv_screen_v).setSelected(true);
            mRootView.findViewById(R.id.tv_screen_auto).setSelected(false);
            mRootView.findViewById(R.id.tv_screen_h).setSelected(false);
        }else if(config.getScreenOrientation() == 2){
            mRootView.findViewById(R.id.tv_screen_h).setEnabled(true);
            mRootView.findViewById(R.id.tv_screen_h).setSelected(true);
            mRootView.findViewById(R.id.tv_screen_auto).setSelected(false);
            mRootView.findViewById(R.id.tv_screen_v).setSelected(false);
        }else{
            mRootView.findViewById(R.id.tv_screen_auto).setEnabled(true);
            mRootView.findViewById(R.id.tv_screen_auto).setSelected(true);
            mRootView.findViewById(R.id.tv_screen_h).setSelected(false);
            mRootView.findViewById(R.id.tv_screen_v).setSelected(false);
        }
        mRootView.findViewById(R.id.tv_screen_auto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRootView.findViewById(R.id.tv_screen_auto).setEnabled(true);
                mRootView.findViewById(R.id.tv_screen_auto).setSelected(true);
                mRootView.findViewById(R.id.tv_screen_h).setSelected(false);
                mRootView.findViewById(R.id.tv_screen_v).setSelected(false);
                config.setScreenOrientation(0);
                AppUtil.saveConfig(getActivity(),config);
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
                if(config.getEnableHorizontalColumn()){
                    config.setScreenOrientation(2);
                }else{
                    config.setScreenOrientation(1);
                }
                AppUtil.saveConfig(getActivity(),config);
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
                config.setScreenOrientation(1);
                config.setColumnCount(1);
                AppUtil.saveConfig(getActivity(),config);
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        });

        mRootView.findViewById(R.id.tv_page_open).setEnabled(true);
        mRootView.findViewById(R.id.tv_page_open).setSelected(true);
        //默认配置单双页配置
        if(!config.getEnableHorizontalColumn()){
            mRootView.findViewById(R.id.tv_page_close).setEnabled(true);
            mRootView.findViewById(R.id.tv_page_close).setSelected(true);
            mRootView.findViewById(R.id.tv_page_open).setSelected(false);
        }else{
            mRootView.findViewById(R.id.tv_page_open).setEnabled(true);
            mRootView.findViewById(R.id.tv_page_open).setSelected(true);
            mRootView.findViewById(R.id.tv_page_close).setSelected(false);
        }
        mRootView.findViewById(R.id.tv_page_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRootView.findViewById(R.id.tv_page_open).setEnabled(true);
                mRootView.findViewById(R.id.tv_page_open).setSelected(true);
                mRootView.findViewById(R.id.tv_page_close).setSelected(false);
                //开启双页
                config.setColumnCount(2);
                config.setEnableHorizontalColumn(true);
                AppUtil.saveConfig(getActivity(), config);
                EventBus.getDefault().post(new BookPageEvent());
            }
        });
        mRootView.findViewById(R.id.tv_page_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRootView.findViewById(R.id.tv_page_close).setEnabled(true);
                mRootView.findViewById(R.id.tv_page_close).setSelected(true);
                mRootView.findViewById(R.id.tv_page_open).setSelected(false);
                //开启单页
                config.setColumnCount(1);
                config.setEnableHorizontalColumn(false);
                AppUtil.saveConfig(getActivity(), config);
                EventBus.getDefault().post(new BookPageEvent());
            }
        });
        mRootView.findViewById(R.id.rl_font_bottom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        //字体大小
        SeekBar fontBar = mRootView.findViewById(R.id.seekbar_font);
        fontBar.setMax(4); // 设置最大值为100
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fontBar.setMin(0); // 设置最小值为0
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
        paddingBar.setMax(4); // 设置最大值为100
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            paddingBar.setMin(0); // 设置最小值为0
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
        textSpaceBar.setMax(4); // 设置最大值为100
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

        //字体选择
         ll_font_select = mRootView.findViewById(R.id.ll_font_select);
         fontTextView = mRootView.findViewById(R.id.sp_font);
         fontTextView.setText(FontGridAdapter.getFontShowName(config.getFont()));
         fontTextView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 ll_font_select.setVisibility(View.VISIBLE);
             }
         });

        Map<String, File> userFonts = FontFinder.getUserFonts();
        Map<String, File> systemFonts = new HashMap<>();// FontFinder.getSystemFonts();
        List<String> fontKeyList = new ArrayList<>();
        try {

            fontKeyList.addAll(Arrays.asList(getActivity().getAssets().list("fonts")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fontKeyList.addAll(new ArrayList<>(systemFonts.keySet()));


         fontsRecyclerView = (RecyclerView) mRootView.findViewById(R.id.rv_fonts);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 6);

        // 将布局管理器设置到RecyclerView中
        fontsRecyclerView.setLayoutManager(layoutManager);
        fontsRecyclerView.setAdapter(new FontGridAdapter(fontKeyList,this));
        ImageView iv_font_back = mRootView.findViewById(R.id.iv_font_back);
        iv_font_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_font_select.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void selectFontCallback(String fontName, String fontNameShow) {
        fontTextView.setText(fontNameShow);
        config.setFont(fontName);
        AppUtil.saveConfig(getActivity(),config);
        EventBus.getDefault().post(new ReloadDataEvent());
        ll_font_select.setVisibility(View.INVISIBLE);

    }
}