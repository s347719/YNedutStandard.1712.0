package com.yineng.ynmessager.view.xrecyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;

public class LoadingMoreFooter extends LinearLayout {

    public final static int STATE_LOADING = 0;
    public final static int STATE_COMPLETE = 1;
    public final static int STATE_NOMORE = 2;
    public final static int STATE_ERROR = 3;
    static final Interpolator ANIMATION_INTERPOLATOR = new LinearInterpolator();
    static final int ROTATION_ANIMATION_DURATION = 1200;
    private TextView mText;
    private String loadingHint;
    private String noMoreHint;
    private String loadingDoneHint;
    private LinearLayout mContainer;
    private ImageView progress_image;
    private FrameLayout progress_view;
    private Animation mRotateAnimation;
    private int state=-1;

	public LoadingMoreFooter(Context context) {
		super(context);
		initView();
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public LoadingMoreFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

    public void setLoadingHint(String hint) {
        loadingHint = hint;
    }

    public void setNoMoreHint(String hint) {
        noMoreHint = hint;
    }

    public void setLoadingDoneHint(String hint) {
        loadingDoneHint = hint;
    }

    public void initView(){

        mContainer = (LinearLayout) LayoutInflater.from(getContext()).inflate(
                R.layout.xrecycleview_footer, null);

        mRotateAnimation = new RotateAnimation(0, 720, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        mRotateAnimation.setInterpolator(ANIMATION_INTERPOLATOR);
        mRotateAnimation.setDuration(ROTATION_ANIMATION_DURATION);
        mRotateAnimation.setRepeatCount(Animation.INFINITE);
        mRotateAnimation.setRepeatMode(Animation.RESTART);

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 0);
        this.setLayoutParams(lp);
        this.setPadding(0, 0, 0, 0);
        addView(mContainer,new LayoutParams(LayoutParams.MATCH_PARENT, 80));

        progress_image = (ImageView)findViewById(R.id.progress_image);
        progress_image.setImageDrawable(getContext().getResources().getDrawable(R.drawable.default_ptr_rotate));
        mText = (TextView) findViewById(R.id.mText);
        progress_view = (FrameLayout) findViewById(R.id.progress_view);
        mText.setText("正在加载...");
        loadingHint = (String)getContext().getText(R.string.listview_loading);
        noMoreHint = (String)getContext().getText(R.string.nomore_loading);
        loadingDoneHint = (String)getContext().getText(R.string.loading_done);

    }


    public void  setState(int state) {
        this.state = state;
        switch(state) {
            case STATE_LOADING:
                progress_view.setVisibility(View.VISIBLE);
                progress_image.startAnimation(mRotateAnimation);
                mText.setText(loadingHint);
                this.setVisibility(View.VISIBLE);
                    break;
            case STATE_COMPLETE:
                mText.setText(loadingDoneHint);
                this.setVisibility(View.GONE);
                break;
            case STATE_NOMORE:
                mText.setText(noMoreHint);
                progress_image.clearAnimation();
                progress_view.setVisibility(View.GONE);
                this.setVisibility(View.VISIBLE);
                break;
            case STATE_ERROR:
                mText.setText("加载错误");
                progress_image.clearAnimation();
                progress_view.setVisibility(View.GONE);
                this.setVisibility(View.VISIBLE);
                break;
        }
    }

    public int getState() {
        return state;
    }
}
