package com.yineng.ynmessager.view.pullUpDownScrollView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;


public class DefaultLoadMoreView extends RelativeLayout implements ILoadMoreView {

    private TextView mTvMessage;
    private ProgressBar mPbLoading;
    private View view;
    public DefaultLoadMoreView(Context context) {
        super(context);
        init(context);
    }

    public DefaultLoadMoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DefaultLoadMoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.loading_view_final_footer_default, this);;
        mPbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        mTvMessage = (TextView) findViewById(R.id.tv_loading_msg);
        view = findViewById(R.id.view);
    }

    @Override
    public void showNormal() {
        view.setVisibility(GONE);
        mPbLoading.setVisibility(View.GONE);
        mTvMessage.setText(R.string.loading_view_click_loading_more);
    }

    @Override
    public void showNoMore() {
        view.setVisibility(VISIBLE);
        mPbLoading.setVisibility(View.GONE);
        mTvMessage.setText(R.string.nomore_loading);
    }

    @Override
    public void showLoading() {
        view.setVisibility(VISIBLE);
        mPbLoading.setVisibility(View.VISIBLE);
        mTvMessage.setText(R.string.listview_loading);
    }

    @Override
    public void showFail() {
        view.setVisibility(VISIBLE);
        mPbLoading.setVisibility(View.GONE);
        mTvMessage.setText(R.string.loading_view_net_error);
    }

    @Override
    public View getFooterView() {
        return this;
    }

}
