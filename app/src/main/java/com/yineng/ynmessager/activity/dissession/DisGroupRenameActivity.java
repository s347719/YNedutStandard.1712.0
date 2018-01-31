package com.yineng.ynmessager.activity.dissession;

import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.receiver.CommonReceiver;
import com.yineng.ynmessager.receiver.CommonReceiver.IQuitGroupListener;
import com.yineng.ynmessager.receiver.CommonReceiver.updateGroupDataListener;
import com.yineng.ynmessager.smack.ReqIQ;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.L;

import java.util.List;

/**
 * 重命名讨论组
 * @author 胡毅
 *
 */
public class DisGroupRenameActivity extends BaseActivity {

	/**
	 * 讨论组实例
	 */
	private ContactGroup mContactGroup;
	/**
	 * 讨论组id
	 */
	private String mGroupId;
	/**
	 * 联系人数据库操作对象
	 */
	private ContactOrgDao mContactOrgDao;
	/**
	 * xmpp管理类
	 */
	private XmppConnectionManager mXmppConnectionManager;
	/**
	 * asmack包id
	 */
	private String mPacketId;
	/**
	 * 编辑框
	 */
	private EditText mRenameDisgroupET;
	private ImageButton mImgb_clearText; // 清空按钮
	private TextView mTxt_textCountLimit; // 字数限制显示
	private TextView mTxt_previous;
	private TextView mTxt_done;
	
	/**
	 * 新名称
	 */
	private String mNewName = "";
	/**
	 * 默认讨论组名称
	 */
	private String mGroupName;

	private Handler mHandler = new Handler(){
		@Override
        public void dispatchMessage(android.os.Message msg) {
			finish();
			overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
		}
	};
	private int mGroupType;
	private CommonReceiver mCommonReceiver;
	protected boolean isFinishAcitivity = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
		setContentView(R.layout.activity_dissession_rename_layout);
		findViews();
		addGroupUpdatedListener();
	}

	private void initData() {
		mXmppConnectionManager = XmppConnectionManager.getInstance();
//		mXmppConnectionManager.addReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_GROUP, this);
		initDisGroupObject();
	}
	
	/**
	 * 初始化该讨论组对象数据
	 */
	private void initDisGroupObject() {
		mContactOrgDao = new ContactOrgDao(this);
		mContactGroup = (ContactGroup) getIntent().getSerializableExtra(
				Const.INTENT_GROUP_EXTRA_NAME);
		mGroupType = getIntent().getIntExtra(Const.INTENT_GROUPTYPE_EXTRA_NAME, 0);
		if (mContactGroup != null) {
			mGroupId = mContactGroup.getGroupName();
		} else {
			mGroupId = (String) getIntent().getCharSequenceExtra(Const.INTENT_GROUPID_EXTRA_NAME);
			mContactGroup = mContactOrgDao.getGroupBeanById(mGroupId, mGroupType);
		}
		if (mContactGroup != null) {
			if (mContactGroup.getSubject() != null && !mContactGroup.getSubject().isEmpty()) {
				mGroupName = mContactGroup.getSubject();
			} else {
				mGroupName = mContactGroup.getNaturalName();
			}
		} else {
			List<User> mUserList = mContactOrgDao.queryUsersByGroupName(mGroupId, Const.CONTACT_DISGROUP_TYPE);
			mGroupName = DisCreateActivity.calculateGroupName(mUserList,null);
		}
	}
	
	private void findViews() {
		//返回按钮
		mTxt_previous = (TextView)findViewById(R.id.disGroupRename_txt_previous);
		mTxt_previous.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
		//完成按钮
		mTxt_done = (TextView)findViewById(R.id.disGroupRename_txt_done);
		mTxt_done.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				mNewName = mRenameDisgroupET.getText().toString().trim();
				if (mNewName.isEmpty()) {
//					ToastUtil.toastAlerMessage(DisGroupRenameActivity.this, "网络异常，请检查网络",Toast.LENGTH_SHORT);
					return;
				} else {
					//转码
					mNewName = TextUtils.htmlEncode(mNewName);
				}
				sendRequestIQPacket(Const.REQ_IQ_XMLNS_GET_GROUP);
			}
		});
		//字数限制文本
		mTxt_textCountLimit = (TextView)findViewById(R.id.disGroupRename_txt_textCountLimit);
		//清空文本按钮
		mImgb_clearText = (ImageButton) findViewById(R.id.disGroupRename_imgb_clearText);
		mImgb_clearText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				mRenameDisgroupET.setText("");
			}
		});
		//编辑框
		mRenameDisgroupET = (EditText) findViewById(R.id.disGroupRename_edt_newName);
		mRenameDisgroupET.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}
			
			@Override
			public void afterTextChanged(Editable s)
			{
				int len = s.length();
				mTxt_textCountLimit.setText(getString(R.string.disGroupRename_textCountLimit,len));
				
				if(len>0)
				{
					mImgb_clearText.setVisibility(View.VISIBLE);
					if (!mTxt_done.isEnabled()) {
						mTxt_done.setEnabled(true);
						mTxt_done.setTextColor(Color.WHITE);
					}
				}else
				{
					mImgb_clearText.setVisibility(View.INVISIBLE);
					if (mTxt_done.isEnabled()) {
						mTxt_done.setEnabled(false);
						mTxt_done.setTextColor(Color.parseColor("#ebeced"));
					}
				}
			}
		});
		
		mRenameDisgroupET.setText(mGroupName);
		mRenameDisgroupET.setSelection(mGroupName.length());
	}
	
	/**
	 * 添加讨论组信息更改监听器
	 */
	private void addGroupUpdatedListener() {
		mCommonReceiver = new CommonReceiver();
		mCommonReceiver.setUpdateGroupDataListener(new updateGroupDataListener() {
			
			@Override
			public void updateGroupData(int mGroupType) {
				if (mGroupType == DisGroupRenameActivity.this.mGroupType) {
					if (!isFinishAcitivity) {
					} else {
						isFinishAcitivity  = false;
					}
				}
			}
		});
		mCommonReceiver.setIQuitGroupListener(new IQuitGroupListener() {
			
			@Override
			public void IQuitMyGroup(int mGroupType) {
				if (mGroupType == DisGroupRenameActivity.this.mGroupType) {
					isFinishAcitivity  = true;
					finish();
				}
			}
		});
		IntentFilter mIntentFilter = new IntentFilter(Const.BROADCAST_ACTION_UPDATE_GROUP);
		mIntentFilter.addAction(Const.BROADCAST_ACTION_QUIT_GROUP);
		mIntentFilter.addAction(Const.BROADCAST_ACTION_I_QUIT_GROUP);
		registerReceiver(mCommonReceiver, mIntentFilter);		
	}
	
//	@Override
//	public void receivedReqIQResult(ReqIQResult packet) {
//		// 接收到回执信息，判断退出操作是否成功
//		L.i(mTAG, "收到回执信息");
//		if (mPacketId.equals(packet.getPacketID())) {
//			if (packet.getCode() == 200) {
//				mContactGroup.setSubject(mNewName);
////				mContactOrgDao.insertOneContactGroupData(mContactGroup, Const.CONTACT_DISGROUP_TYPE);
//				Intent updateViewIntent = new Intent(Const.BROADCAST_ACTION_UPDATE_GROUP);
//				updateViewIntent.putExtra(Const.INTENT_GROUPTYPE_EXTRA_NAME, mGroupType);
//				sendBroadcast(updateViewIntent);
//			} else {
//			}
//		}
//	}
	
	/**
	 * 发送IQ请求
	 * @param nameSpace
	 *            命名空间
	 */
	private void sendRequestIQPacket(String nameSpace) {
		String paramStr = getParamsJson();
		ReqIQ iq = new ReqIQ();
		mPacketId = iq.getPacketID();
		iq.setNameSpace(nameSpace);
		iq.setParamsJson(paramStr);
		iq.setType(org.jivesoftware.smack.packet.IQ.Type.SET);
		iq.setAction(Const.INTERFACE_ACTION_GROUP_RENAME);
		iq.setFrom(JIDUtil.getJIDByAccount(AppController.getInstance().mSelfUser.getUserNo()));
		iq.setTo("admin@"+mXmppConnectionManager.getServiceName());
		L.i("RenameDisGroup", "iq xml ->" + iq.toXML());
//		mXmppConnectionManager.sendPacket(iq);
		boolean success = sendIqPacketCommon(iq);
		if (success) {
			mHandler.sendEmptyMessageDelayed(0, 500);
		}
	}
	
	/**
	 * 拼接字符串 
	 * @return  eg： {"subject":"请大家下午2点半开会","groupName":"leklddkdfd"}
	 */
	private String getParamsJson() {
//		mNewName = mRenameDisgroupET.getText().toString().trim();
		StringBuilder mBuilder = new StringBuilder();
		if (mGroupType == Const.CONTACT_DISGROUP_TYPE) {
			mBuilder.append("{\"subject\":\"");
		} else {
			mBuilder.append("{\"subject\":\"");
		}
		
		mBuilder.append(mNewName);
		mBuilder.append("\",\"groupName\":\"");
		mBuilder.append(mContactGroup.getGroupName());
		mBuilder.append("\"}");
		return mBuilder.toString();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mCommonReceiver);
	}

}
