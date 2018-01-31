//***************************************************************
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.bean.settings.Setting;
import com.yineng.ynmessager.db.SettingsTb;
import com.yineng.ynmessager.db.dao.SettingsTbDao;
import com.yineng.ynmessager.util.TimeUtil;

/**
 * 消息提醒设置界面
 * 
 * @author 贺毅柳
 * @category Activity
 */
public class MsgNotifySettingActivity extends BaseActivity implements OnClickListener
{
	private TextView mImgb_previous; // 左上角返回按钮
	private AppCompatCheckBox mChk_distractionFree; // [免打扰]功能开关的勾选框
	private RelativeLayout mRel_timeSetting; // 免打扰时间段设置按钮
	private TextView mTxt_timeSetting; // 免打扰时段设置显示文本
	private TextView mTxt_timeSettingDisplay; // 免打扰时间段显示用的TextView
	private RelativeLayout mRel_audio; // 打开[声音]设置界面的按钮
	private RelativeLayout mRel_groupMsgNotifySetting; // 群讨论组消息设置按钮
	private AppCompatCheckBox mChk_notifyWhenExit;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_msg_notify_setting);

		initViews();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		// 加载用户设置
		Setting setting = mApplication.mUserSetting;
		// 用户是否已经开启免打扰
		boolean isDistractionFree = setting.getDistractionFree() != 0;
		mChk_distractionFree.setChecked(isDistractionFree);
		mRel_timeSetting.setClickable(isDistractionFree);
		mTxt_timeSetting.setEnabled(isDistractionFree);
		mTxt_timeSettingDisplay.setEnabled(isDistractionFree);
		// 显示用户的免打扰时间段设置
		int hourBegin = setting.getDistractionFree_begin_h();
		int minBegin = setting.getDistractionFree_begin_m();
		int hourEnd = setting.getDistractionFree_end_h();
		int minEnd = setting.getDistractionFree_end_m();
		String[] betterDisplayBegin = TimeUtil.betterTimeDisplay(hourBegin,minBegin);
		String[] betterDisplayEnd = TimeUtil.betterTimeDisplay(hourEnd,minEnd);
		mTxt_timeSettingDisplay.setText(getString(R.string.msgNotifySetting_timeDisplay,betterDisplayBegin[0],
				betterDisplayBegin[1],betterDisplayEnd[0],betterDisplayEnd[1]));
		// 加载是否退出仍然接收消息提醒
		mChk_notifyWhenExit.setChecked(setting.getReceiveWhenExit() != 0);
	}

	/**
	 * 初始化界面控件
	 */
	private void initViews()
	{
		mImgb_previous = (TextView)findViewById(R.id.msgNotifySetting_imgb_previous);
		mImgb_previous.setOnClickListener(this);
		mChk_distractionFree = (AppCompatCheckBox)findViewById(R.id.msgNotifySetting_chk_distractionFree);
		mChk_distractionFree.setOnClickListener(new DistractionFree_OnClickListener());
		mRel_timeSetting = (RelativeLayout)findViewById(R.id.msgNotifySetting_rel_timeSetting);
		mRel_timeSetting.setOnClickListener(this);
		mTxt_timeSetting = (TextView)findViewById(R.id.msgNotifySetting_txt_timeSetting);
		mTxt_timeSettingDisplay = (TextView)findViewById(R.id.msgNotifySetting_txt_timeSettingDisplay);
		mRel_audio = (RelativeLayout)findViewById(R.id.msgNotifySetting_rel_audio);
		mRel_audio.setOnClickListener(this);
		mRel_groupMsgNotifySetting = (RelativeLayout)findViewById(R.id.msgNotifySetting_rel_groupMsgNotifySetting);
		mRel_groupMsgNotifySetting.setOnClickListener(this);
		mChk_notifyWhenExit = (AppCompatCheckBox)findViewById(R.id.msgNotifySetting_chk_notifyWhenExit);
		mChk_notifyWhenExit.setOnClickListener(new NotifyWhenExit_OnClickListener());
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
			case R.id.msgNotifySetting_imgb_previous:
				finish();
				break;
			case R.id.msgNotifySetting_rel_timeSetting:
				startActivity(new Intent(this,DistractionFreeSettingActivity.class));
				break;
			case R.id.msgNotifySetting_rel_audio:
				startActivity(new Intent(this,AudioSettingActivity.class));
				break;
			case R.id.msgNotifySetting_rel_groupMsgNotifySetting:
				startActivity(new Intent(this,GroupMsgNotifySettingActivity.class));
				break;
		}
	}

	/**
	 * 免打扰开关勾选框的监听器
	 * 
	 * @author 贺毅柳
	 * @category Listener
	 */
	private class DistractionFree_OnClickListener implements OnClickListener
	{

		@Override
		public void onClick(View v)
		{
			CheckBox checkbox = (CheckBox)v;
			boolean isChecked = checkbox.isChecked();
			// 先修改Application中的Setting对象
			Setting setting = mApplication.mUserSetting;
			setting.setDistractionFree(isChecked ? 1 : 0);
			// 将Setting对象更新到数据库
			SettingsTbDao dao = new SettingsTb(getApplicationContext());
			dao.update(setting);
			// 重写将Applicaton中的Setting对象从数据库中更新
			mApplication.mUserSetting = dao.obtainSettingFromDb();

			mRel_timeSetting.setClickable(isChecked);
			mTxt_timeSetting.setEnabled(isChecked);
			mTxt_timeSettingDisplay.setEnabled(isChecked);
		}
	}

	/**
	 * 退出后是否接受消息提醒的CheckBox勾选监听器
	 * 
	 * @author 贺毅柳
	 * 
	 */
	private class NotifyWhenExit_OnClickListener implements OnClickListener
	{

		@Override
		public void onClick(View v)
		{
			CheckBox checkbox = (CheckBox)v;
			boolean isChecked = checkbox.isChecked();
			// 先修改Application中的Setting对象
			Setting setting = mApplication.mUserSetting;
			setting.setReceiveWhenExit(isChecked ? 1 : 0);
			// 将Setting对象更新到数据库
			SettingsTbDao dao = new SettingsTb(getApplicationContext());
			dao.update(setting);
			// 重写将Applicaton中的Setting对象从数据库中更新
			mApplication.mUserSetting = dao.obtainSettingFromDb();
		}

	}

}
