//***************************************************************
//*    2015-5-20  上午11:47:56
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.PopupWindow;

import com.yineng.ynmessager.R;

/**
 * 群讨论组消息提醒 设置界面中点击列表项时弹出的Popupwindow
 * 
 * @author 贺毅柳
 * 
 */
public class ConfirmNotifyModeWindow extends PopupWindow
{
	private AdapterView<?> mParent;
	private int mListPosition;

	public ConfirmNotifyModeWindow(Context context)
	{
		// 填充布局
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.groupmsgnotifysetting_confirmwindow,null,false);
		// 初始化Popupwindow
		setContentView(view);
		setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
		setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		setFocusable(true);
		setOutsideTouchable(false);
		setBackgroundDrawable(context.getResources().getDrawable(android.R.color.transparent));
		setAnimationStyle(R.style.Anim_Popupwindow);
	}

	/**
	 * 设置PopupWindow当前关联的AdapterView
	 * 
	 * @param parent
	 *            AdapterView
	 */
	public void setListAdapterView(AdapterView<?> parent)
	{
		mParent = parent;
	}

	/**
	 * 获取PopupWindow当前关联的AdapterView
	 * 
	 * @return AdapterView
	 */
	public AdapterView<?> getListAdapterView()
	{
		return mParent;
	}

	/**
	 * 设置Popupwindow当前关联的Adapter的position
	 * 
	 * @param position
	 */
	public void setListPosition(int position)
	{
		mListPosition = position;
	}

	/**
	 * 获取Popupwindow当前关联的Adapter的position
	 * 
	 * @return position
	 */
	public int getListPosition()
	{
		return mListPosition;
	}

}
