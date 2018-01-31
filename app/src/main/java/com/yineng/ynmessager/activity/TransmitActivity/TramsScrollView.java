package com.yineng.ynmessager.activity.TransmitActivity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 *
 * Created by 舒欢
 * Created time: 2017/4/19
 */
public class TramsScrollView extends ScrollView {

    private float xDistance, yDistance, xLast, yLast;

    private ScrollViewListener scrollViewListener = null;

    public TramsScrollView(Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDistance = yDistance = 0f;
                xLast = ev.getX();
                yLast = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float curX = ev.getX();
                final float curY = ev.getY();

                xDistance += Math.abs(curX - xLast);
                yDistance += Math.abs(curY - yLast);
                xLast = curX;
                yLast = curY;

                if (xDistance > yDistance) {
                    return false;
                }
        }
        boolean onIntercept = super.onInterceptTouchEvent(ev);

        return onIntercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean touchEvent = super.onTouchEvent(ev);
        return touchEvent;
    }

    public TramsScrollView(Context context, AttributeSet attrs,
                        int defStyle) {
        super(context, attrs, defStyle);
    }

    public TramsScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {

        super.onScrollChanged(x, y, oldx, oldy);
        if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }
    public interface ScrollViewListener {
        void onScrollChanged(TramsScrollView scrollView, int x, int y, int oldx, int oldy);
    }

}
