package com.yineng.ynmessager.view.recyclerview;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by 舒欢
 * Created time: 2017/8/16
 * Descreption：解决在recycleview 中嵌套recycleview的布局管理器
 */

public class FullyLinearLayoutManager extends LinearLayoutManager {

    private static final String TAG = FullyLinearLayoutManager.class.getSimpleName();

    public FullyLinearLayoutManager(Context context) {
        super(context);
    }

    public FullyLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    private int[] mMeasuredDimension = new int[2];

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state,
                          int widthSpec, int heightSpec) {

        final int widthMode = View.MeasureSpec.getMode(widthSpec);
        final int heightMode = View.MeasureSpec.getMode(heightSpec);
        final int widthSize = View.MeasureSpec.getSize(widthSpec);
        final int heightSize = View.MeasureSpec.getSize(heightSpec);


        int width = 0;
        int height = 0;
        for (int i = 0; i < getItemCount(); i++) {
            measureScrapChild(recycler, i,
                    View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                    mMeasuredDimension);

            if (getOrientation() == HORIZONTAL) {
                width = width + mMeasuredDimension[0];
                if (i == 0) {
                    height = mMeasuredDimension[1];
                }
            } else {
                height = height + mMeasuredDimension[1];
                if (i == 0) {
                    width = mMeasuredDimension[0];
                }
            }
        }
        switch (widthMode) {
            case View.MeasureSpec.EXACTLY:
                width = widthSize;
            case View.MeasureSpec.AT_MOST:
            case View.MeasureSpec.UNSPECIFIED:
        }

        switch (heightMode) {
            case View.MeasureSpec.EXACTLY:
                height = heightSize;
            case View.MeasureSpec.AT_MOST:
            case View.MeasureSpec.UNSPECIFIED:
        }

        setMeasuredDimension(width, height);
    }

    private void measureScrapChild(RecyclerView.Recycler recycler, int position, int widthSpec,
                                   int heightSpec, int[] measuredDimension) {
        try {
            View view = recycler.getViewForPosition(0);//fix 动态添加时报IndexOutOfBoundsException

            if (view != null) {
                RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();

                int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                        getPaddingLeft() + getPaddingRight(), p.width);

                int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                        getPaddingTop() + getPaddingBottom(), p.height);

                view.measure(childWidthSpec, childHeightSpec);
                measuredDimension[0] = view.getMeasuredWidth() + p.leftMargin + p.rightMargin;
                measuredDimension[1] = view.getMeasuredHeight() + p.bottomMargin + p.topMargin;
                recycler.recycleView(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }


    /**
     * 然后设置：
     FullyLinearLayoutManager fullyLinearLayoutManager = new FullyLinearLayoutManager(getActivity());
     fullyLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
     /**
     * 当平滑滚动启用时，滚动条把手的位置和大小基于可见条目的可见像素数来计算。
     * 该处里假定所有列表条目具有相同的高度。如果你使用条目高度不同的类表， 滚动条会在用户滚动过程中改变大小。
     * 为了避免这种情况，应该禁用该特性。 当平滑滚动被禁用后，滚动条把手的大小和位置只是基于适配器中的条目数， 以及适配器中的可见条目来确定。
     * 这样可以为使用可变高条目列表的用户， 提供稳定的滚动条。
     /
     fullyLinearLayoutManager.setSmoothScrollbarEnabled(true);
     //TODO 开启自动测绘
     fullyLinearLayoutManager.setAutoMeasureEnabled(true);
     recyclerView.setLayoutManager(fullyLinearLayoutManager);
     //设置不允许嵌套滑动，因为嵌套滑动的话会导致错乱
     recyclerView.setNestedScrollingEnabled(false);
     recyclerView.setAdapter(mAdapter);

     作者：小火火520
     链接：http://www.jianshu.com/p/d0465dec3566
     來源：简书
     著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
     */

}
