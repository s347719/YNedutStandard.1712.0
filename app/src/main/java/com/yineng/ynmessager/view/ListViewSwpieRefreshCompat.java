//***************************************************************
//*    2015-11-3  下午5:38:42
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
import android.view.View;
import android.widget.ListView;

/**
 * 主要用于解决ListView在SwipeRefreshLayout中时调用 {@link #setEmptyView(View)} ，其EmptyView与下拉刷新冲突的问题
 * @author 贺毅柳
 * 
 */
public class ListViewSwpieRefreshCompat extends ListView
{

	public ListViewSwpieRefreshCompat(Context context)
	{
		super(context);
	}

	public ListViewSwpieRefreshCompat(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	public void setVisibility(int visibility)
	{
		if(visibility != View.GONE || getCount() != 0)
		{
			super.setVisibility(visibility);
		}
	}
}
