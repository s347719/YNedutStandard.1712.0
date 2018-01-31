package com.yineng.ynmessager.view.pullUpDownScrollView;

import android.view.View;

import com.yineng.ynmessager.view.pullUpDownScrollView.OnDefaultRefreshListener;
import com.yineng.ynmessager.view.pullUpDownScrollView.PtrFrameLayout;

public interface OnRefreshListener {

    /**
     * Check can do refresh or not. For example the content is empty or the first child is in view.
     * <p/>
     * {@link OnDefaultRefreshListener#checkContentCanBePulledDown}
     */
    public boolean checkCanDoRefresh(final PtrFrameLayout frame, final View content, final View header);

    /**
     * When refresh begin
     *
     * @param frame
     */
    public void onRefreshBegin(final PtrFrameLayout frame);
}