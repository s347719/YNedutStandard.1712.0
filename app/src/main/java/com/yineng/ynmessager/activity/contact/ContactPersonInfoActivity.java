package com.yineng.ynmessager.activity.contact;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Intents;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.MainActivity;
import com.yineng.ynmessager.activity.p2psession.P2PChatActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.contact.OrganizationTree;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.receiver.CommonReceiver;
import com.yineng.ynmessager.receiver.CommonReceiver.onServiceNoticeListener;
import com.yineng.ynmessager.smack.ReceiveReqIQCallBack;
import com.yineng.ynmessager.smack.ReqIQ;
import com.yineng.ynmessager.smack.ReqIQResult;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.ToastUtil;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 个人详情页面
 *
 * @author 胡毅
 */
public class ContactPersonInfoActivity extends BaseActivity implements OnClickListener, ReceiveReqIQCallBack {

	private User mContactInfo;
	private TextView mPersonInfoNameTV;
	private TextView mPersonInfoGenderTV;
	private Context mContext;
	private XmppConnectionManager mXmppConnectionManager;
	private ContactOrgDao mContactOrgDao;
	private TextView mPersonInfoMainOrgTV;
	private TextView mPersonInfoMainJobTV;
	private TextView mPersonInfoTelNumberTV;
	private TextView mPersonInfoEmailTV;
	private TextView mPersonInfoPostTV;
	private LinearLayout mPersonInfoLayoutLL;
	//点击生成的弹出框
	private PopupWindow mPopupWindow;
	private View mPhonePopView;

	private TextView mTxt_previous;
	/**
	 * 已更新个人信息的用户
	 */
	private List<User> mUsersList = new ArrayList<User>();
	private CommonReceiver mCommonReceiver;
	//展示学生和教师的区别字段
	private TextView left_org;
	/**
	 * 发起会话的布局
	 */
	private LinearLayout mCreateChatLayoutLL;
	private CircleImageView mPersonInfoAvatar;
	private boolean isClickRefresh = false;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
        public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case 0:
					if (mContactInfo != null) {
						int userType = mContactInfo.getUserType();
						if (userType == 1){
							//教师帐号
							left_org.setText(getString(R.string.main_department));
							if(mContactInfo.getPost()!=null){
								mPersonInfoPostTV.setText(mContactInfo.getPost());
							}else {
								mPersonInfoPostTV.setText(getString(R.string.main_teacher));
							}
						}else if (userType==2){
							//学生帐号
							left_org.setText(getString(R.string.main_class));
							if (mContactInfo.getPost()!=null){
								mPersonInfoPostTV.setText(mContactInfo.getPost());
							}else {
								mPersonInfoPostTV.setText(getString(R.string.main_student));
							}
						}
						String orgName = mContactInfo.getOrgName();
						if (StringUtils.isEmpty(orgName)) {
							OrganizationTree mOrg = mContactOrgDao.queryUserRelationByUserNo(mContactInfo.getUserNo());
							mPersonInfoMainJobTV.setText(mOrg != null ? mOrg.getOrgName() : "");
						} else {
							mPersonInfoMainJobTV.setText(orgName);
						}
						mPersonInfoNameTV.setText(mContactInfo.getUserName());
						mPersonInfoEmailTV.setText(mContactInfo.getEmail());

						if (mContactInfo.getGender() == 1) {
							mPersonInfoGenderTV.setText("男");
						} else if (mContactInfo.getGender()==2){
							mPersonInfoGenderTV.setText("女");
						}else {
							mPersonInfoGenderTV.setText("保密");
						}

						String telephone = mContactInfo.getTelephone();
						mPersonInfoTelNumberTV.setText(telephone);
					}
					if (isClickRefresh) {
						isClickRefresh = false;
						ToastUtil.toastAlerMessageCenter(mContext, "已更新个人信息", 1000);
					}
					//刷新用户头像
					refreshUserIcon();
					break;
				case 1:
					// 下载该用户的头像
					FileUtil.downloadAvatarZipFile(null, mUsersList, "3");
					break;
				default:
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = ContactPersonInfoActivity.this;
		setContentView(R.layout.activity_contact_personinfo);
		initData();
		findViews();
		addServiceNoticeListener();
	}

	private void initData() {
		mXmppConnectionManager = XmppConnectionManager.getInstance();
		mXmppConnectionManager.addReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_PERSON_DETAIL,
				ContactPersonInfoActivity.this);
		mContactOrgDao = new ContactOrgDao(mContext);
		Intent dataIntent = getIntent();
		mContactInfo = dataIntent.getParcelableExtra("contactInfo");
		mHandler.sendEmptyMessage(0);
		//		mContactOrgInfo = (OrganizationTree)dataIntent.getSerializableExtra("parentOrg");
		connectSendPacket(0, Const.REQ_IQ_XMLNS_GET_PERSON_DETAIL, true);
	}

	private void findViews() {
		mTxt_previous = (TextView) findViewById(R.id.contactPersonInfo_previous);
		mTxt_previous.setOnClickListener(this);
		mPersonInfoLayoutLL = (LinearLayout) findViewById(R.id.ll_contact_personinfo_layout);
		mPersonInfoAvatar = (CircleImageView) findViewById(R.id.contactPersonInfo_avatar);
		mPersonInfoNameTV = (TextView) findViewById(R.id.contactPersonInfo_nickname);
		mPersonInfoGenderTV = (TextView) findViewById(R.id.contactPersonInfo_gender);
		mPersonInfoPostTV = (TextView) findViewById(R.id.contactPersonInfo_employeeId);
		mPersonInfoMainOrgTV = (TextView) findViewById(R.id.tv_contact_personinfo_mainOrg);
		mPersonInfoMainJobTV = (TextView) findViewById(R.id.tv_contact_personInfo_mainJob);
		mPersonInfoTelNumberTV = (TextView) findViewById(R.id.tv_contact_personInfo_telNumber);
		mPersonInfoEmailTV = (TextView) findViewById(R.id.tv_contact_personInfo_email);
		left_org = (TextView) findViewById(R.id.left_org);

		mCreateChatLayoutLL = (LinearLayout) findViewById(R.id.ll_contact_personinfo_creatChat);
		if (mContactInfo.getUserNo().equals(mApplication.mSelfUser.getUserNo())) {
			mCreateChatLayoutLL.setEnabled(false);
		}
		refreshUserIcon();
	}

	//刷新头像
	public void refreshUserIcon() {
		int avatar=-1;
		File userIcon = FileUtil.getAvatarByName(mContactInfo.getUserNo());
		if (mContactInfo.getGender()==1){//男
			avatar=R.mipmap.session_p2p_men;
		}else if (mContactInfo.getGender()==2){//女
			avatar=R.mipmap.session_p2p_woman;
		}else {
			avatar = R.mipmap.session_no_sex;
		}
		if (userIcon == null || !userIcon.exists()) {
			mPersonInfoAvatar.setImageResource(avatar);
		} else {
			mPersonInfoAvatar.setImageURI(Uri.fromFile(userIcon));
		}
	}

	/**
	 * 注册服务器信息变更的监听
	 */
	private void addServiceNoticeListener() {
		mCommonReceiver = new CommonReceiver();
		mCommonReceiver.setOnServiceNoticeListener(new onServiceNoticeListener() {

			@Override
			public void onServiceNoticed() {
				connectSendPacket(0, Const.REQ_IQ_XMLNS_GET_PERSON_DETAIL, true);
			}
		});
		IntentFilter mIntentFilter = new IntentFilter(Const.BROADCAST_ACTION_SERVICE_NOTICE);
		registerReceiver(mCommonReceiver, mIntentFilter);
	}

	public void onClickListener(View v) {
		switch (v.getId()) {
			case R.id.ll_contact_personinfo_creatChat:// /进入会话界面
				Intent chatIntent = new Intent(mContext, P2PChatActivity.class);
				chatIntent.putExtra(Const.INTENT_USER_EXTRA_NAME, mContactInfo);
				startActivity(chatIntent);
				BaseActivity.deleteContactActivitys();
				finish();
				LocalBroadcastManager.getInstance(getApplicationContext())
						.sendBroadcast(new Intent(MainActivity.ACTION_RETURN_TO_MAIN_RECENT).putExtra("index", 0));
				break;
			case R.id.ll_contact_personinfo_sendcard:// 点击发送名片弹出框
				if(mContactInfo!=null){
					mPhonePopView = LayoutInflater.from(mContext).inflate(R.layout.contact_personinfo_send_personinfo, null);
					showPhonePopWindow();
				}
				break;

			case R.id.view_send_message://点击发送名片
				sendContactCard();
				break;

			case R.id.view_cancle_message://点击取消发送名片弹出框
				mPopupWindow.dismiss();
				break;
			case R.id.view_copy_message://点击复制名片
				//判断当前是男是女
				String sex;
				if (mContactInfo.getGender() == 1) {
					sex = "男";
				} else if (mContactInfo.getGender()==2){
					sex = "女";
				}else {
					sex ="保密";
				}
				//判断当前机构是否有名字
				String org ="";
				String work ="";
				if(mContactInfo.getUserType()==1){//教师
					if(mContactInfo.getPost().length()>0){
						org = "  职称："+mContactInfo.getPost()+"\n";
					}else {org = "  职称："+"教职工"+"\n";}
					work = "  部门："+mContactInfo.getOrgName()+"\n";
				}else {//学生
					if (mContactInfo.getPost().length()>0){org= "  职称："+mContactInfo.getPost()+"\n";}
					else {org= "  职称："+"学生"+"\n";}
					work = "  班级："+mContactInfo.getOrgName()+"\n";
				}
				//生成复制名片的字符串
				String copyString = "  姓名："+mContactInfo.getUserName()+"\n"
						+"  性别："+sex+"\n"
						+ org
						+ work
						+"  手机："+(mContactInfo.getTelephone()!=null?mContactInfo.getEmail():"")+"\n"
						+"  Email："+(mContactInfo.getEmail()!=null ?mContactInfo.getEmail():"" )+"\n";
				ClipboardManager mClipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
				mClipboard.setPrimaryClip(ClipData.newPlainText(this.getClass().getSimpleName(),copyString));
				showToast(R.string.common_copyToClipboard);
				mPopupWindow.dismiss();
				break;

			case R.id.ll_contact_personinfo_phonenumber:// 点击电话，打开弹出框
				break;
			case R.id.ll_contact_personinfo_telenumber:// 点击手机，打开弹出框
				if (!mPersonInfoTelNumberTV.getText().toString().isEmpty()) {
					mPhonePopView = LayoutInflater.from(mContext).inflate(R.layout.contact_personinfo_phone_popview, null);
					showPhonePopWindow();
				}
				break;
			case R.id.ll_contact_personinfo_email://点击电子邮箱
				if (!mPersonInfoEmailTV.getText().toString().isEmpty()){
					mPhonePopView = LayoutInflater.from(mContext).inflate(R.layout.contact_persioninifo_phone_email,null);
					showPhonePopWindow();
				}
				break;
			case R.id.tv_personinfo_email_send://发送email
				sendEmailtoUser();
				mPopupWindow.dismiss();
				break;
			case R.id.tv_personinfo_pop_email_copy://复制邮箱
				copyEmail();
				mPopupWindow.dismiss();
				break;
			case R.id.tv_personinfo_pop_email_cancle://取消
				mPopupWindow.dismiss();
				break;
			case R.id.tv_personinfo_pop_add_contact:// 弹出框之添加号码到联系人
				openContactAppForAddContact();
				mPopupWindow.dismiss();
				break;
			case R.id.tv_personinfo_pop_call_contact:// 弹出框之打电话
				callContactPhone();
				mPopupWindow.dismiss();
				break;
			case R.id.tv_personinfo_pop_callcopy_contact:// 弹出框之复制电话号码
				copyTelephoneNum();
				mPopupWindow.dismiss();
				break;
			case R.id.tv_personinfo_pop_sent_sms:// 弹出框之发短信
				sendContactMsg();
				mPopupWindow.dismiss();
				break;
			case R.id.tv_personinfo_pop_cancle_pop:// 取消
				mPopupWindow.dismiss();
				break;
			case R.id.bt_contact_personinfo_refresh:
				mUsersList.clear();
				isClickRefresh = true;
				connectSendPacket(0, Const.REQ_IQ_XMLNS_GET_PERSON_DETAIL, false);
				break;
			default:
				break;
		}
	}


	/**
	 * 发送IQ请求获取该联系人详细信息
	 */
	private void connectSendPacket(int action, String nameSpace, boolean isFirst) {
		ReqIQ iq = new ReqIQ();
		iq.setNameSpace(nameSpace);
		iq.setParamsJson("{\"userNoList\":[\"" + mContactInfo.getUserNo() + "\"]}");
		// iq.setFrom("admin" + "@" + mXmppConnectionManager.getServiceName());
		L.d("ContactPersonInfoActivity", "iq xml ->" + iq.toXML());
		//		mXmppConnectionManager.sendPacket(iq);
		boolean success = sendIqPacketCommon(iq);
	}

	/**
	 * IQ消息回执
	 */
	@Override
	public void receivedReqIQResult(ReqIQResult packet) {
		String nameSpace = packet.getNameSpace();
		if (nameSpace.equals(Const.REQ_IQ_XMLNS_GET_PERSON_DETAIL)) {
			L.v("ContactPersonInfoActivity", "iq xml ->" + packet.toXML());
			if (packet.getCode() == 200) {
				// mContactInfo = JSON.parseObject(packet.getResp(),User.class);
				try {
					JSONArray tempJsonArray = new JSONArray(packet.getResp());
					if (tempJsonArray.length() > 0) {
						for (int i = 0; i < tempJsonArray.length(); i++) {
							User tempUser = new User();
							JSONObject mUserObject = tempJsonArray.optJSONObject(i);
							tempUser.setCreateTime(mUserObject.optInt("createTime"));
							tempUser.setGender(mUserObject.optInt("gender"));
							tempUser.setUserType(mUserObject.optInt("userType"));
							tempUser.setDayOfBirth(mUserObject.optString("dayOfBirth"));
							tempUser.setEmail(mUserObject.optString("email"));
							tempUser.setHeadUrl(mUserObject.optString("headUrl"));
							tempUser.setJoinTime(mUserObject.optString("joinTime"));
							tempUser.setOrgName(mUserObject.optString("mainOrg"));
							tempUser.setPost(mUserObject.optString("post"));
							tempUser.setSigature(mUserObject.optString("signature"));
							tempUser.setUserName(mUserObject.optString("userName"));
							tempUser.setUserNo(mUserObject.optString("userNo"));

							//优先显示公号，无公号则显示个人联系电话
							String[] telephone = StringUtils.split(mUserObject.optString("telephone"), ',');
							if (telephone.length > 0) {
								if (telephone.length > 1) {
									tempUser.setTelephone(telephone[1]);
								} else {
									tempUser.setTelephone(telephone[0]);
								}
							}

							mUsersList.add(tempUser);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (mUsersList.size() > 0) {
					if (isClickRefresh) {
						mHandler.sendEmptyMessage(1);
					}
					mContactInfo = mUsersList.get(0);
					mContactOrgDao.insertUpdateOneUserData(mContactInfo);
				}
				mHandler.sendEmptyMessage(0);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mXmppConnectionManager != null) {
			mXmppConnectionManager.removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_PERSON_DETAIL);
		}
		unregisterReceiverSafe(mCommonReceiver);
	}

	/**
	 * 复制邮箱
	 */
	private void copyEmail() {
		if (mContactInfo!=null && !mContactInfo.getEmail().isEmpty()) {
			ClipboardManager mClipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
			mClipboard.setPrimaryClip(ClipData.newPlainText(this.getClass().getSimpleName(),mContactInfo.getEmail()));
			showToast(R.string.common_copyToClipboard);
		}

	}

	/**
	 * 打开弹出框
	 */
	public void showPhonePopWindow() {
		mPopupWindow = new PopupWindow(mPhonePopView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
		mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#b0000000")));
		mPopupWindow.showAtLocation(mPersonInfoLayoutLL, Gravity.BOTTOM, 0, 0);
		mPopupWindow.setAnimationStyle(R.style.AnimBottom);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setFocusable(true);
		mPopupWindow.showAsDropDown(mPersonInfoLayoutLL);
//        mPopupWindow.update();
	}

	/**
	 * 发送邮件
	 *
	 */
	public void sendEmailtoUser(){
		if (mContactInfo!=null && !mContactInfo.getEmail().isEmpty()) {
			Intent data = new Intent(Intent.ACTION_SENDTO);
			if (data.resolveActivity(getPackageManager())!=null) {
				data.setData(Uri.parse(mContactInfo.getEmail()));
				data.putExtra(Intent.EXTRA_SUBJECT, "标题");
				data.putExtra(Intent.EXTRA_TEXT, "请填写内容");
				startActivity(data);
			}else {
				ToastUtil.toastAlerMessageBottom(ContactPersonInfoActivity.this,getString(R.string.down_email_app),1000);
			}
		}
	}

	/**
	 * 添加号码到联系人
	 */
	public void openContactAppForAddContact() {
		if (mContactInfo != null && !mContactInfo.getTelephone().isEmpty()) {
			Intent intent = new Intent(Intent.ACTION_INSERT,
					Uri.withAppendedPath(Uri.parse("content://com.android.contacts"), "contacts"));
			intent.putExtra(Intents.Insert.NAME, mContactInfo.getUserName());
			intent.putExtra(Intents.Insert.PHONE, mContactInfo.getTelephone());
			startActivity(intent);
		}
	}

	public void copyTelephoneNum(){
		if (mContactInfo != null && !mContactInfo.getTelephone().isEmpty()) {
			ClipboardManager mClipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
			mClipboard.setPrimaryClip(ClipData.newPlainText(this.getClass().getSimpleName(),mContactInfo.getTelephone()));
			showToast(R.string.common_copyToClipboard);
		}
	}
	/**
	 * 拨打号码
	 */
	public void callContactPhone() {
		if (mContactInfo != null && !mContactInfo.getTelephone().isEmpty()) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_CALL);
			intent.setData(Uri.parse("tel:" + mContactInfo.getTelephone()));
			startActivity(intent);
		}
	}

	/**
	 * 发送消息
	 */
	public void sendContactMsg() {
		if (mContactInfo != null && !mContactInfo.getTelephone().isEmpty()) {
			Uri smsToUri = Uri.parse("smsto:" + mContactInfo.getTelephone());
			Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
			startActivity(intent);
		}
	}

	/**
	 * 发送名片
	 */
	public void sendContactCard() {
		if (mContactInfo != null) {
			Uri smsToUri = Uri.parse("smsto:");
			Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
			intent.putExtra("sms_body", "姓名: "
					+ mContactInfo.getUserName()
					+ ", 手机: "
					+ (mContactInfo.getTelephone()!=null? mContactInfo.getTelephone() :"")
					+ ", 邮箱: "
					+ (mContactInfo.getEmail()!=null? mContactInfo.getEmail() :""));
			startActivity(intent);
		} else {
			ToastUtil.toastAlerMessage(mContext, "没有该用户信息", 1000);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
			case R.id.contactPersonInfo_previous:
				finish();
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
				break;
		}
	}
}
