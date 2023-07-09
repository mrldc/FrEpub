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

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.util.AppUtil;

/**
 * 设置字体fragment主页面
 */
public class FontFragment extends Fragment {
    private View mRootView;
    private SeekBar seekBar;

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
        Config config = AppUtil.getSavedConfig(getActivity());
    }

}