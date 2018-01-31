//***************************************************************
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.settings;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.bean.settings.Setting;
import com.yineng.ynmessager.db.SettingsTb;
import com.yineng.ynmessager.db.dao.SettingsTbDao;

/**
 * 浏览设置界面
 * 
 * @author 贺毅柳
 * @category Activity
 */
public class ViewSettingActivity extends BaseActivity implements OnClickListener, OnCheckedChangeListener
{
	private TextView mTxt_previous; // 左上角返回按钮
	private RelativeLayout mRel_fontSize; // 字体大小按钮
	private RelativeLayout mRel_backgroundSkin;
	private SwitchCompat mSwt_autoRecImg;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_setting);

		initViews();
	}

	/**
	 * 初始化界面控件
	 */
	private void initViews()
	{
		mTxt_previous = (TextView)findViewById(R.id.viewSetting_txt_previous);
		mTxt_previous.setOnClickListener(this);
		mRel_fontSize = (RelativeLayout)findViewById(R.id.viewSetting_rel_fontSize);
		mRel_fontSize.setOnClickListener(this);
		mRel_backgroundSkin = (RelativeLayout)findViewById(R.id.viewSetting_rel_backgroundSkin);
		mRel_backgroundSkin.setOnClickListener(this);
		mSwt_autoRecImg = (SwitchCompat)findViewById(R.id.viewSetting_swt_autoRecImg);
		mSwt_autoRecImg.setOnCheckedChangeListener(this);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		mSwt_autoRecImg.setChecked(mApplication.mUserSetting.getAlwaysAutoReceiveImg() != 0);
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
			case R.id.viewSetting_txt_previous: // 返回
				finish();
				break;
			case R.id.viewSetting_rel_fontSize: // 字体大小
				// startActivity(new
				// Intent(this,FontSizeSettingActivity.class));
				showToast("此功能开发中，敬请期待……");
				break;
			case R.id.viewSetting_rel_backgroundSkin: // 背景图片
				showToast("此功能开发中，敬请期待……");
				break;
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		int id = buttonView.getId();
		switch(id)
		{
			case R.id.viewSetting_swt_autoRecImg:
				SettingsTbDao settingsTbDao = new SettingsTb(getApplicationContext());
				// 改变Application中的Settings对象
				Setting setting = mApplication.mUserSetting;
				setting.setAlwaysAutoReceiveImg(isChecked ? 1 : 0);
				// 然后更新到数据库
				settingsTbDao.update(setting);
				// 再从数据库中加载到Application的Setting
				mApplication.mUserSetting = settingsTbDao.obtainSettingFromDb();
				break;
		}
	}
}
