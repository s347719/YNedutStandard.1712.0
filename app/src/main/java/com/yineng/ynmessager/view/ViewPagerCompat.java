//***************************************************************
//*    2015-6-24  上午10:46:59
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 主要是在原有ViewPager的基础上增添了一个设置是否能被手动滑动的功能，用于兼容一些滑动手势冲突的问题<br>
 * 默认是跟原有ViewPage一样可以手动滑动<br>
 * 通过{@link #setScrollable(boolean)}设置
 * 
 * @author 贺毅柳
 * 
 */
public class ViewPagerCompat extends ViewPager
{
	private boolean mScrollable = true;

	public ViewPagerCompat(Context context)
	{
		super(context);
	}

	public ViewPagerCompat(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	/**
	 * 设置ViewPager是否能被手动滑动
	 * 
	 * @param scrollable
	 *            true或者false
	 */
	public void setScrollable(boolean scrollable)
	{
		mScrollable = scrollable;
	}

	/**
	 * 返回ViewPager当前是否能被手动滑动
	 * 
	 * @return true表示可以，否则为false
	 */
	public boolean isScrollable()
	{
		return mScrollable;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		if(mScrollable)
		{
			return super.onTouchEvent(ev);
		}else
		{
			return false;
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		if(mScrollable)
		{
			return super.onInterceptTouchEvent(ev);
		}else
		{
			return false;
		}
	}
}
