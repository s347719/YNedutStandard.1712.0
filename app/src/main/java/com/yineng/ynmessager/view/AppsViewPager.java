package com.yineng.ynmessager.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

/**
 * @author by 舒欢
 *         Created time: 2017/11/30
 *         Descreption：
 */

public class AppsViewPager extends ViewPager {
    private ViewGroup parent;

    public AppsViewPager(Context context) {
        super(context);
        parent= (ViewGroup) getParent();
    }

    public AppsViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        parent= (ViewGroup) getParent();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (parent != null) {
                    //禁止上一层的View 不处理该事件,屏蔽父组件的事件
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (parent != null) {
                    //拦截
                    parent.requestDisallowInterceptTouchEvent(false);
                }
                break;

            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

}
