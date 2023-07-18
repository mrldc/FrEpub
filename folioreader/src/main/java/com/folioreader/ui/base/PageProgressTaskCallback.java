package com.folioreader.ui.base;

import com.folioreader.model.db.PageProgress;

import java.util.List;

/**
 * @author gautam chibde on 12/6/17.
 */

public interface PageProgressTaskCallback extends BaseMvpView {
    void onCompletePageCalculate(List<PageProgress> pageProgressList);
}
