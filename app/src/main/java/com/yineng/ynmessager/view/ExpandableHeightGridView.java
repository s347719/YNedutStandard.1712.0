//***************************************************************
//*    2015-9-25  上午11:56:51
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.GridView;

/**
 * @author 贺毅柳
 * 
 */
/**
 * 该GridView实现了可以嵌套在ScrollView中，并且高度不会出现问题（可以直接全部展开，并且不能被滑动），
 * 外层的整个ScrollView可以正常滑动，通过 {@link #setExpanded(boolean)}方法设置
 * 
 * @author 贺毅柳
 */
public class ExpandableHeightGridView extends GridView
{
	private boolean expanded = false;

	public ExpandableHeightGridView(Context context)
	{
		super(context);
	}

	public ExpandableHeightGridView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public ExpandableHeightGridView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public boolean isExpanded()
	{
		return expanded;
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		// HACK! TAKE THAT ANDROID!
		if(isExpanded())
		{
			// Calculate entire height by providing a very large height hint.
			// View.MEASURED_SIZE_MASK represents the largest height possible.
			int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK,MeasureSpec.AT_MOST);
			super.onMeasure(widthMeasureSpec,expandSpec);

			ViewGroup.LayoutParams params = getLayoutParams();
			params.height = getMeasuredHeight();
		}else
		{
			super.onMeasure(widthMeasureSpec,heightMeasureSpec);
		}
	}

	/**
	 * 设置是否直接展开显示GridView中的所有内容
	 * 
	 * @param expanded
	 */
	public void setExpanded(boolean expanded)
	{
		this.expanded = expanded;
	}

}
