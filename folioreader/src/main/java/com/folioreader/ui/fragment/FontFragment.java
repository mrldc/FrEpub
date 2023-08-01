package com.folioreader.ui.fragment;

import android.content.ContentResolver;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import com.folioreader.ui.base.HtmlUtil;
import com.folioreader.util.AppUtil;
import com.folioreader.util.FontFinder;
import com.folioreader.util.Utils;
import com.folioreader.util.UtilsKtKt;
import com.litao.slider.NiftySlider;
import com.litao.slider.effect.ITEffect;

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

    String LOG_TAG = FolioPageFragment.class.getSimpleName();
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

        int activeTrackColor =
                Utils.setColorAlpha(ContextCompat.getColor(requireContext(), R.color.we_read_thumb_color), 1f);
        int inactiveTrackColor =
                Utils.setColorAlpha(ContextCompat.getColor(requireContext(), R.color.we_read_theme_color), 0.1f);
        int iconTintColor =
                Utils.setColorAlpha(ContextCompat.getColor(requireContext(), R.color.we_read_theme_color), 0.7f);

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
                    config.setColumnCount(2);
                }else{
                    config.setColumnCount(1);
                }
                config.setScreenOrientation(2);
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
        final NiftySlider fontBar = mRootView.findViewById(R.id.seekbar_font);
        fontBar.setValue(config.getFontSize(),false);
        fontBar.setThumbText(HtmlUtil.getFontSize(config.getFontSize())+"");
        fontBar.setValueFrom(0);
        fontBar.setValueTo(4);
        ITEffect effect = new ITEffect(fontBar);
        effect.setStartIcon(R.drawable.ic_font2);
        effect.setEndIcon(R.drawable.ic_font2);
        effect.setStartIconSize(Utils.dpToPx(10));
        effect.setEndIconSize(Utils.dpToPx(15));
        effect.setStartPadding(Utils.dpToPx(12));
        effect.setEndPadding(Utils.dpToPx(12));
        effect.setStartTintList(ColorStateList.valueOf(iconTintColor));
        effect.setEndTintList(ColorStateList.valueOf(iconTintColor));
        fontBar.setTrackTintList(ColorStateList.valueOf(activeTrackColor));
        fontBar.setTrackInactiveTintList(ColorStateList.valueOf(inactiveTrackColor));

        fontBar.setEffect(effect);

        fontBar.setOnIntValueChangeListener(new NiftySlider.OnIntValueChangeListener() {
            @Override
            public void onValueChange(@NonNull NiftySlider niftySlider, int progress, boolean fromUser) {
                if(fromUser){
                    config.setFontSize(progress);
                    AppUtil.saveConfig(getActivity(), config);
                    fontBar.setThumbText(HtmlUtil.getFontSize(config.getFontSize())+"");
                    EventBus.getDefault().post(new ReloadDataEvent());
                }

            }
        });



        //边距
        NiftySlider paddingBar = mRootView.findViewById(R.id.seekbar_margins);
        paddingBar.setValue(config.getBodyPadding(),false);
        paddingBar.setValueFrom(0);
        paddingBar.setValueTo(4);
        ITEffect paddingEffect = new ITEffect(paddingBar);
        paddingEffect.setStartText("小");
        paddingEffect.setEndText("大");
        paddingEffect.setStartTextSize(Utils.dpToPx(12));
        paddingEffect.setEndTextSize(Utils.dpToPx(12));
        paddingEffect.setStartPadding(Utils.dpToPx(12));
        paddingEffect.setEndPadding(Utils.dpToPx(12));
        paddingEffect.setStartTintList(ColorStateList.valueOf(iconTintColor));
        paddingEffect.setEndTintList(ColorStateList.valueOf(iconTintColor));
        paddingBar.setTrackTintList(ColorStateList.valueOf(activeTrackColor));
        paddingBar.setTrackInactiveTintList(ColorStateList.valueOf(inactiveTrackColor));

        paddingBar.setEffect(paddingEffect);

        paddingBar.setOnIntValueChangeListener(new NiftySlider.OnIntValueChangeListener() {
            @Override
            public void onValueChange(@NonNull NiftySlider niftySlider, int progress, boolean fromUser) {
                if(fromUser){
                    config.setBodyPadding(progress);
                    AppUtil.saveConfig(getActivity(), config);
                    EventBus.getDefault().post(new ReloadDataEvent());
                }

            }
        });

        //行距
        NiftySlider textSpaceBar = mRootView.findViewById(R.id.seekbar_space);
        textSpaceBar.setValue(config.getTextSpace(),false);
        textSpaceBar.setValueFrom(0);
        textSpaceBar.setValueTo(4);
        ITEffect textSpaceEffect = new ITEffect(textSpaceBar);
        textSpaceEffect.setStartText("紧");
        textSpaceEffect.setEndText("松");
        textSpaceEffect.setStartTextSize(Utils.dpToPx(12));
        textSpaceEffect.setEndTextSize(Utils.dpToPx(12));
        textSpaceEffect.setStartPadding(Utils.dpToPx(12));
        textSpaceEffect.setEndPadding(Utils.dpToPx(12));
        textSpaceEffect.setStartTintList(ColorStateList.valueOf(iconTintColor));
        textSpaceEffect.setEndTintList(ColorStateList.valueOf(iconTintColor));
        textSpaceBar.setTrackTintList(ColorStateList.valueOf(activeTrackColor));
        textSpaceBar.setTrackInactiveTintList(ColorStateList.valueOf(inactiveTrackColor));

        textSpaceBar.setEffect(textSpaceEffect);

        textSpaceBar.setOnIntValueChangeListener(new NiftySlider.OnIntValueChangeListener() {
            @Override
            public void onValueChange(@NonNull NiftySlider niftySlider, int progress, boolean fromUser) {
                if(fromUser){
                    config.setTextSpace(progress);
                    AppUtil.saveConfig(getActivity(), config);
                    EventBus.getDefault().post(new ReloadDataEvent());
                }

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
            fontKeyList.add("系统字体");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fontKeyList.addAll(new ArrayList<>(systemFonts.keySet()));


         fontsRecyclerView = (RecyclerView) mRootView.findViewById(R.id.rv_fonts);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 4);

        // 将布局管理器设置到RecyclerView中
        fontsRecyclerView.setLayoutManager(layoutManager);
        fontsRecyclerView.setAdapter(new FontGridAdapter(fontKeyList,this));
        ImageView iv_font_back = mRootView.findViewById(R.id.iv_font_back);
        iv_font_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_font_select.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void selectFontCallback(String fontName, String fontNameShow) {
        fontTextView.setText(fontNameShow);
        config.setFont(fontName);
        AppUtil.saveConfig(getActivity(),config);
        EventBus.getDefault().post(new ReloadDataEvent());
        ll_font_select.setVisibility(View.GONE);


    }
}