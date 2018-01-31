package com.yineng.ynmessager.view.recyclerview.decoration;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by 贺毅柳 on 2015/12/17 11:33.
 */
public class LinearVerSpacingItemDecoration extends RecyclerView.ItemDecoration
{
    private int mSpace;
    private boolean mApplyToLeftRight = false;

    /**
     * 设置
     * @param space
     */
    public LinearVerSpacingItemDecoration(int space)
    {
        this(space, false);
    }

    public LinearVerSpacingItemDecoration(int space, boolean applyToLeftRight)
    {
        mSpace = space;
        mApplyToLeftRight = applyToLeftRight;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state)
    {
        if (mApplyToLeftRight)
        {
            outRect.left = mSpace;
            outRect.right = mSpace;
        }

        outRect.bottom = mSpace;
        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildLayoutPosition(view) == 0)
        {
            outRect.top = mSpace;
        }
    }
}
