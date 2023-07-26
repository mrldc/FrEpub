package com.folioreader.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.FolioReader;
import com.folioreader.R;
import com.folioreader.model.event.ChangeBackgroundEvent;
import com.folioreader.model.event.ReloadDataEvent;
import com.folioreader.ui.activity.FolioActivityCallback;
import com.folioreader.util.AppUtil;
import com.folioreader.util.Utils;
import com.litao.slider.NiftySlider;
import com.litao.slider.effect.ITEffect;

import org.greenrobot.eventbus.EventBus;

/**
 * 设置亮度fragment主页面
 */
public class LightFragment extends Fragment {
    private View mRootView;
    private NiftySlider seekBar;
    private FolioActivityCallback callback;

    private View backgroundView1;
    private View backgroundView2;
    private View backgroundView3;
    private View backgroundView4;
    public LightFragment(FolioActivityCallback callback){
        this.callback = callback;
    }
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
        backgroundView1 = mRootView.findViewById(R.id.light_background_select01);
        backgroundView2 = mRootView.findViewById(R.id.light_background_select02);
        backgroundView3 = mRootView.findViewById(R.id.light_background_select03);
        backgroundView4 = mRootView.findViewById(R.id.light_background_select04);


        Config config = AppUtil.getSavedConfig(getActivity());
        int activeTrackColor =
                Utils.setColorAlpha(ContextCompat.getColor(requireContext(), R.color.we_read_thumb_color), 1f);
        int inactiveTrackColor =
                Utils.setColorAlpha(ContextCompat.getColor(requireContext(), R.color.we_read_theme_color), 0.1f);
        int iconTintColor =
                Utils.setColorAlpha(ContextCompat.getColor(requireContext(), R.color.we_read_theme_color), 0.7f);
        seekBar =  mRootView.findViewById(R.id.seekbar_light);
        // #FFFFFF
        mRootView.findViewById(R.id.iv_light_1).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                callback.selectBackground(2);
                changeBackgroundSelect(false,true,false,false);
              //  EventBus.getDefault().post(new ChangeBackgroundEvent(getActivity().getResources().getString(R.color.background)));
            }
        });
        // #EDEAE4
        mRootView.findViewById(R.id.iv_light_2).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                callback.selectBackground(1);

                changeBackgroundSelect(true,false,false,false);
             //   EventBus.getDefault().post(new ChangeBackgroundEvent(getActivity().getResources().getString(R.color.background02)));
            }
        });
        //#E9F4E3
        mRootView.findViewById(R.id.iv_light_3).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                callback.selectBackground(3);
                changeBackgroundSelect(false,false,true,false);
             //   EventBus.getDefault().post(new ChangeBackgroundEvent(getActivity().getResources().getString(R.color.background03)));
            }
        });
        // #E3F4F3
        mRootView.findViewById(R.id.iv_light_4).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                callback.selectBackground(4);
                changeBackgroundSelect(false,false,false,true);
            //    EventBus.getDefault().post(new ChangeBackgroundEvent(getActivity().getResources().getString(R.color.background04)));

            }
        });

        //设置滚动条

        seekBar.setValue(config.getLight(),false);

        ITEffect effect = new ITEffect(seekBar);
        effect.setStartIcon(R.drawable.icon_brightness_down);
        effect.setEndIcon(R.drawable.icon_brightness_up);
        effect.setStartIconSize(Utils.dpToPx(10));
        effect.setEndIconSize(Utils.dpToPx(15));
        effect.setStartPadding(Utils.dpToPx(12));
        effect.setEndPadding(Utils.dpToPx(12));
        effect.setStartTintList(ColorStateList.valueOf(iconTintColor));
        effect.setEndTintList(ColorStateList.valueOf(iconTintColor));
        seekBar.setTrackTintList(ColorStateList.valueOf(activeTrackColor));
        seekBar.setTrackInactiveTintList(ColorStateList.valueOf(inactiveTrackColor));

        seekBar.setEffect(effect);

        seekBar.setOnIntValueChangeListener(new NiftySlider.OnIntValueChangeListener() {
            @Override
            public void onValueChange(@NonNull NiftySlider niftySlider, int progress, boolean fromUser) {
                if (fromUser) {//以免太暗
                    config.setLight(progress);
                    AppUtil.saveConfig(getActivity(), config);

                    WindowManager.LayoutParams layoutParams = getActivity().getWindow().getAttributes();
                    layoutParams.screenBrightness = (float) progress/255 ;//因为这个值是[0, 1]范围的
                    getActivity().getWindow().setAttributes(layoutParams);
                  /*  if (ContextCompat.checkSelfPermission(
                            getActivity(), Manifest.permission.WRITE_SETTINGS
                    ) != PackageManager.PERMISSION_GRANTED
                    ){
                        Toast.makeText(getActivity(),"请前往设置->应用权限 打开系统设置权限",Toast.LENGTH_LONG).show();
                    }else{
                        ContentResolver contentResolver = getActivity().getContentResolver();
                        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, progress);
                        contentResolver.notifyChange(Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),null);
                    }*/

                }

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
    private void changeBackgroundSelect(boolean select01,boolean select02,boolean select03,boolean select04){
        if(select01){
            backgroundView1.setVisibility(View.VISIBLE);
            backgroundView2.setVisibility(View.GONE);
            backgroundView3.setVisibility(View.GONE);
            backgroundView4.setVisibility(View.GONE);
        }else if(select02){
            backgroundView1.setVisibility(View.GONE);
            backgroundView2.setVisibility(View.VISIBLE);
            backgroundView3.setVisibility(View.GONE);
            backgroundView4.setVisibility(View.GONE);
        }else if(select03){
            backgroundView1.setVisibility(View.GONE);
            backgroundView2.setVisibility(View.GONE);
            backgroundView3.setVisibility(View.VISIBLE);
            backgroundView4.setVisibility(View.GONE);
        }else if(select04){
            backgroundView1.setVisibility(View.GONE);
            backgroundView2.setVisibility(View.GONE);
            backgroundView3.setVisibility(View.GONE);
            backgroundView4.setVisibility(View.VISIBLE);
        }
    }
}