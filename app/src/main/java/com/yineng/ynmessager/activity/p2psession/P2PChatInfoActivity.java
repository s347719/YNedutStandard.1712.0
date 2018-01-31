package com.yineng.ynmessager.activity.p2psession;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.contact.ContactPersonInfoActivity;
import com.yineng.ynmessager.activity.dissession.DisAddActivity;
import com.yineng.ynmessager.activity.dissession.DisGroupPersonList;
import com.yineng.ynmessager.activity.groupsession.GroupChatActivity;
import com.yineng.ynmessager.activity.session.FindChatRecordActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.ClientInitConfig;
import com.yineng.ynmessager.bean.contact.OrganizationTree;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class P2PChatInfoActivity extends BaseActivity implements OnClickListener
{

	private String mChatId;
	private Context mContext;
	private ContactOrgDao mContactOrgDao;
	private List<User> mUserList = new LinkedList<User>();
	private TextView mTxt_nickname;
	private TextView mTxt_chatLogSearch;
	private TextView mTxt_previous;
	private RelativeLayout mRel_createNewDiscussBtn;
	private RelativeLayout mRel_userDetailsBtn;
	private User mCurrentUesr; // 对方的User对象
	private CircleImageView mIv_userAvatar;
	private TextView mTxt_postname;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_person_info_layout);
		initViews();
	}

	public void initViews()
	{
		mContext = P2PChatInfoActivity.this;
		mChatId = getIntent().getStringExtra(GroupChatActivity.CHAT_ID_KEY);
		mContactOrgDao = new ContactOrgDao(mContext);
		mIv_userAvatar = (CircleImageView) findViewById(R.id.p2pChatInfo_userInfoArea_avatar);
		mTxt_nickname = (TextView)findViewById(R.id.p2pChatInfo_nickname);
		mTxt_postname = (TextView)findViewById(R.id.p2pChatInfo_post);
		mTxt_chatLogSearch = (TextView)findViewById(R.id.p2pChatInfo_txt_chatLogSearch);
		mTxt_chatLogSearch.setOnClickListener(this);
		mTxt_previous = (TextView)findViewById(R.id.p2pChatInfo_previous);
		mTxt_previous.setOnClickListener(this);
		mRel_createNewDiscussBtn = (RelativeLayout)findViewById(R.id.p2pChatInfo_rel_createNewDiscussBtn);
		mRel_createNewDiscussBtn.setOnClickListener(this);
		mRel_userDetailsBtn = (RelativeLayout)findViewById(R.id.p2pChatInfo_rel_userDetailsBtn);
		mRel_userDetailsBtn.setOnClickListener(this);

		if(mChatId != null)
		{
			mCurrentUesr = mContactOrgDao.queryUserInfoByUserNo(mChatId);
			if(mCurrentUesr != null)
			{
				mTxt_nickname.setText(mCurrentUesr.getUserName());
				OrganizationTree mOrg = mContactOrgDao.queryUserRelationByUserNo(mCurrentUesr.getUserNo());
				mTxt_postname.setText(mOrg != null ? mOrg.getOrgName() : "");
				mUserList.add(mCurrentUesr);
			}
			//设置头像
			File userIcon = FileUtil.getAvatarByName(mChatId);
			if (userIcon == null || !userIcon.exists()) {
				mIv_userAvatar.setImageResource(R.mipmap.user);
			} else {
				mIv_userAvatar.setImageURI(Uri.fromFile(userIcon));
			}
		}

	}

	/**
	 * 打开个人资料页
	 * 
	 * @param mUser
	 */
	public void startPersonInfoActivity(User mUser)
	{
		Intent infoIntent = new Intent(mContext,ContactPersonInfoActivity.class);
//		OrganizationTree mParentOrg = mContactOrgDao.queryUserRelationByUserNo(mUser.getUserNo());
//		infoIntent.putExtra("parentOrg",mParentOrg);
		infoIntent.putExtra("contactInfo",mUser);
		mContext.startActivity(infoIntent);
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
			case R.id.p2pChatInfo_previous:
				finish();
				break;
			case R.id.p2pChatInfo_txt_chatLogSearch:
				Intent chatRecordintent = new Intent(this,FindChatRecordActivity.class);
				chatRecordintent.putExtra(GroupChatActivity.CHAT_ID_KEY,mChatId);
				chatRecordintent.putExtra(GroupChatActivity.CHAT_TYPE_KEY,Const.CHAT_TYPE_P2P);
				startActivity(chatRecordintent);
				break;
			case R.id.p2pChatInfo_rel_createNewDiscussBtn:
				ClientInitConfig mClientInitConfig = mContactOrgDao.getClientInitInfo();
				int sumDisGroups = mContactOrgDao.queryGroupSumCreatedByMe(mApplication.mLoginUser.getUserNo());
                L.i(mTag, String.format("我创建的讨论组已有%d个", sumDisGroups));
				if(mClientInitConfig != null)
				{
					int maxDisGroups = Integer.parseInt(mClientInitConfig.getMax_disdisgroup_can_create());
					if(sumDisGroups >= maxDisGroups)
					{
						ToastUtil.toastAlerMessage(mContext,getString(R.string.p2pChatInfo_myDiscussCountOutOfMax,maxDisGroups),
								Toast.LENGTH_SHORT);
						return;
					}
				}
				ArrayList<User> tempUsers = new ArrayList<User>();
				if(mApplication.mSelfUser != null)
				{
					//把我添加到列表里
					tempUsers.add(mApplication.mSelfUser);
				}
				//把聊天对象添加到列表里
				tempUsers.add(mUserList.get(0));
				final Intent intent = new Intent(this,DisAddActivity.class);
				intent.putExtra("disGroupAddedUser",tempUsers);
				intent.putExtra(DisGroupPersonList.GROUP_TYPE,Const.CONTACT_DISGROUP_TYPE);
				startActivity(intent);
				break;
			case R.id.p2pChatInfo_rel_userDetailsBtn:
				final Intent userDetailsIntent = new Intent(this,ContactPersonInfoActivity.class);
				userDetailsIntent.putExtra("contactInfo",mCurrentUesr);
				startActivity(userDetailsIntent);
				break;
		}
	}
}
