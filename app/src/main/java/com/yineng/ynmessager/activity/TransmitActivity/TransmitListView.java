package com.yineng.ynmessager.activity.TransmitActivity;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * 让listview中的内容能全部展示出来
 * Created by 舒欢
 * Created time: 2017/4/19
 */
public class TransmitListView extends ListView {

    public TransmitListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TransmitListView(Context context) {
        super(context);
    }

    public TransmitListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int expandSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}
