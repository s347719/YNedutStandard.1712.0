package com.yineng.ynmessager.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.settings.DownloadedFilesActivity;
import com.yineng.ynmessager.activity.settings.SettingActivity;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.settings.Setting;
import com.yineng.ynmessager.db.SettingsTb;
import com.yineng.ynmessager.util.FileUtil;
import de.hdodenhof.circleimageview.CircleImageView;
import java.io.File;

public class MenuFragmentLeft extends BaseFragment implements OnClickListener
{
	private Button mBtn_darkMode;
	private Button mBtn_mySettings;
	private Button mBtn_downloadedFilesManagement;
	private TextView mTxt_username;
	private CircleImageView mIv_userAvatar;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.main_menu_left_layout,container,false);
	}
	
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view,savedInstanceState);
		Setting setting = mApplication.mUserSetting;
		mIv_userAvatar = (CircleImageView) view.findViewById(R.id.main_img_avatar);
		mBtn_darkMode = (Button)view.findViewById(R.id.main_btn_darkMode);
		mBtn_mySettings = (Button)view.findViewById(R.id.main_btn_mySettings);
		mBtn_downloadedFilesManagement = (Button)view.findViewById(R.id.main_btn_downloadedFilesManagement);
		mTxt_username = (TextView)view.findViewById(R.id.main_txt_username);

		int darkModeTextRes = setting.getDarkMode() == 0 ? R.string.main_darkMode : R.string.main_normalMode;
		mBtn_darkMode.setText(darkModeTextRes);
		mBtn_darkMode.setOnClickListener(this);
		mBtn_mySettings.setOnClickListener(this);
		mBtn_downloadedFilesManagement.setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		User self = mApplication.mSelfUser;
		if(self != null)
		{
			mTxt_username.setText(self.getUserName());
			//设置头像
			File userIcon = FileUtil.getAvatarByName(self.getUserNo());
			if (userIcon == null || !userIcon.exists()) {
				mIv_userAvatar.setImageResource(R.mipmap.user_center_avatar);
			} else {
				mIv_userAvatar.setImageURI(Uri.fromFile(userIcon));
			}
		} else {
			mIv_userAvatar.setImageResource(R.mipmap.user_center_avatar);
		}

	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
			case R.id.main_btn_darkMode:
				Setting setting = mApplication.mUserSetting;
				setting.setDarkMode(setting.getDarkMode() == 0 ? 1 : 0);
				new SettingsTb(getApplicationContext()).update(setting);
				switchTheme();
				break;
			case R.id.main_btn_mySettings:
				startActivity(new Intent(mParentActivity,SettingActivity.class));
				break;
			case R.id.main_btn_downloadedFilesManagement:
                startActivity(new Intent(mParentActivity, DownloadedFilesActivity.class));
                //startActivity(new Intent(mParentActivity, InternshipListActivity.class));
                break;
        }
	}

	/**
	 * 切换主题（重新启动MainActivity来加载Style）
	 */
	private void switchTheme()
	{
		Activity parent = getActivity();
		Intent intent = parent.getIntent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		parent.finish();
		parent.overridePendingTransition(0,0);
		startActivity(intent);
	}

}
