package com.folioreader.ui.activity;

import android.graphics.Rect;

import com.folioreader.Config;
import com.folioreader.model.DisplayUnit;
import com.folioreader.model.db.Book;
import com.folioreader.model.locators.ReadLocator;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public interface FolioActivityCallback {

    int getCurrentChapterIndex();

    ReadLocator getEntryReadLocator();

    boolean goToChapter(@Nullable String href, @Nullable String cfi);
    boolean goToChapter(String href);

    Config.Direction getDirection();

    void onDirectionChange(Config.Direction newDirection);

    void storeLastReadLocator(ReadLocator lastReadLocator);

    void toggleSystemUI();
    void toggleSystemUI(Boolean showUI);
    void setDayMode();

    void setNightMode();

    int getTopDistraction(final DisplayUnit unit);

    int getBottomDistraction(final DisplayUnit unit);

    Rect getViewportRect(final DisplayUnit unit);

    WeakReference<FolioActivity> getActivity();

    String getStreamerUrl();

    /**更新进度条**/
    void updateProgressUi(double percent);

    Book getReadRecord();
    void updateReadRecord(Book book);
    //控制显示哪一个tab
    void tabController(Boolean directory ,Boolean write  ,Boolean light  , Boolean font  );
    /**选择背景图片**/
    void selectBackground(Integer index);

    void setStopScroll(Boolean stopScroll);
}
