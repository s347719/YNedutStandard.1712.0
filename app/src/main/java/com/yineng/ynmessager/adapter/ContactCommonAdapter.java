package com.yineng.ynmessager.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.contact.ContactPersonInfoActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.ContactGroupUser;
import com.yineng.ynmessager.bean.contact.OrganizationTree;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactCommonAdapter extends BaseAdapter {
	private static final String TAG = "ContactCommonAdapter";
	private Context mContext;
	private List<Object> nListObjects;
	private ContactOrgDao mContactOrgDao = null;
	private int mShowUserTag = -1;
	private int mShowOrgTag = -1;
	private int mShowGroupTag = -1;
	private int mShowDisGroupTag = -1;
	private LayoutInflater mInflater;
	public static final int[] LOGO_BACKGROUND = {
			R.mipmap.icon_org_1,
			R.mipmap.icon_org_2,
			R.mipmap.icon_org_3,
			R.mipmap.icon_org_4,
			R.mipmap.icon_org_5,
			R.mipmap.icon_org_6,
			R.mipmap.icon_org_7};

	public ContactCommonAdapter(Context context, List<Object> mListObjects) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		this.nListObjects = mListObjects;
		mContactOrgDao = new ContactOrgDao(mContext);
	}

	public void resetViewTag() {
		mShowUserTag = -1;
		mShowOrgTag = -1;
		mShowGroupTag = -1;
		mShowDisGroupTag = -1;
	}

	public void setnListObjects(List<Object> nListObjects) {
		this.nListObjects = nListObjects;
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		Object tempResultObject = nListObjects.get(position);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.contact_orglist_common_item, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.logo = (ImageView) convertView.findViewById(R.id.contactChildOrg_img_listItem_logo);
			viewHolder.tvContactItemTag = (TextView) convertView.findViewById(R.id.tv_contactlist_common_item_tag);
			viewHolder.llContactItemOrg = (RelativeLayout) convertView.findViewById(R.id.ll_contactlist_common_item_org);
			viewHolder.tvContactItemOrgName = (TextView) convertView
					.findViewById(R.id.tv_contactlist_common_item_orgname);
			viewHolder.tvContactItemOrgCount = (TextView) convertView
					.findViewById(R.id.tv_contactlist_common_item_personcount);
			viewHolder.llContactItemPerson = (RelativeLayout) convertView
					.findViewById(R.id.ll_contactlist_common_item_person);
			viewHolder.ivContactItemPersonIcon = (CircleImageView) convertView
					.findViewById(R.id.iv_contactlist_common_item_personicon);
			viewHolder.tvContactItemPersonName = (TextView) convertView
					.findViewById(R.id.tv_contactlist_common_item_personname);
			viewHolder.tvContactItemPostName = (TextView) convertView
					.findViewById(R.id.tv_contactlist_common_item_postname);
			viewHolder.tvState = (TextView) convertView.findViewById(R.id.tv_contactlist_common_item_state);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (tempResultObject instanceof User) {
			if (mShowUserTag == -1 || mShowUserTag == position) {
				viewHolder.tvContactItemTag.setVisibility(View.VISIBLE);
				viewHolder.tvContactItemTag.setText(R.string.contactChildOrg_member);
				mShowUserTag = position;
			} else {
				viewHolder.tvContactItemTag.setVisibility(View.GONE);
			}
			showUserListItem(viewHolder);
			User mUser = (User) tempResultObject;
			viewHolder.tvContactItemPersonName.setText(mUser.getUserName());
			if (mUser.getOrgName() != null && !(mUser.getOrgName().isEmpty())) {
				viewHolder.tvContactItemPostName.setVisibility(View.VISIBLE);
				viewHolder.tvContactItemPostName.setText(mUser.getOrgName());
			} else {
				viewHolder.tvContactItemPostName.setVisibility(View.GONE);
			}

			int img=-1;
			if (mUser.getGender()==1){//男
				img=R.mipmap.session_p2p_men;
			}else if (mUser.getGender()==2){//女
				img=R.mipmap.session_p2p_woman;
			}else {
				img=R.mipmap.session_no_sex;
			}
			//设置头像
			File userIcon = FileUtil.getAvatarByName(mUser.getUserNo());
			if (userIcon == null || !userIcon.exists()) {
				viewHolder.ivContactItemPersonIcon.setImageResource(img);
			} else {
				viewHolder.ivContactItemPersonIcon.setImageURI(Uri.fromFile(userIcon));
			}

			if (mUser.getUserStatus() == 0){ // 根据在线和离线，设置头像透明度
				viewHolder.ivContactItemPersonIcon.setAlpha(0.4f);
				viewHolder.tvState.setHint(R.string.allContact_listItem_offline);
			} else {
				viewHolder.ivContactItemPersonIcon.setAlpha(1.0f);
				viewHolder.tvState.setHint(R.string.allContact_listItem_online);
			}
		} else if (tempResultObject instanceof OrganizationTree) {// 组织结构
			// 随机设置LOGO
			int logoBackground = LOGO_BACKGROUND[(int) (Math.random() * 7)];
			viewHolder.logo.setBackgroundResource(logoBackground);
			if (mShowOrgTag == -1 || mShowOrgTag == position) {
				viewHolder.tvContactItemTag.setVisibility(View.VISIBLE);
				viewHolder.tvContactItemTag.setText(R.string.contactChildOrg_department);
				mShowOrgTag = position;
			} else {
				viewHolder.tvContactItemTag.setVisibility(View.GONE);
			}
			showOrgListItem(viewHolder);
			OrganizationTree tempOrg = (OrganizationTree) tempResultObject;
			viewHolder.tvContactItemOrgName.setText(tempOrg.getOrgName());
			// 某组织机构节点总人数统计
			List<User> userList=new ArrayList<>();
			mContactOrgDao.getOrgUserByOrgIdFromDb(tempOrg,userList);
			int index=0;
			for (User user:userList) {
				if (user.getUserStatus()==0){//离线人数
					index++;
				}
			}
			viewHolder.tvContactItemOrgCount.setText((userList.size()-index)+"/"+userList.size());
		} else if (tempResultObject instanceof ContactGroup) {
			// 随机设置LOGO
			viewHolder.logo.setBackgroundResource(R.mipmap.session_group);
			showOrgListItem(viewHolder);
			List<User> mTempList;
			ContactGroup tempContactGroup = (ContactGroup) tempResultObject;
			if (tempContactGroup.getGroupType() == 8) { // 群组
				if (mShowGroupTag == -1 || mShowGroupTag == position) {
					viewHolder.tvContactItemTag.setVisibility(View.VISIBLE);
					viewHolder.tvContactItemTag.setText(R.string.contactChildOrg_group);
					mShowGroupTag = position;
				} else {
					viewHolder.tvContactItemTag.setVisibility(View.GONE);
				}
				mTempList = mContactOrgDao.queryUsersByGroupName(tempContactGroup.getGroupName(), 8);
				viewHolder.tvContactItemOrgName.setText(tempContactGroup.getNaturalName());
			} else { // 讨论组
				int avatar=-1;
				ContactGroupUser group = mContactOrgDao.getContactGroupUserById(tempContactGroup.getGroupName(), LastLoginUserSP.getLoginUserNo(mContext), Const.CHAT_TYPE_DIS);
				if (group!=null&&group.getRole()==10) {//自己创建的讨论组
					avatar = R.mipmap.session_creat_discus;
				} else {//自己加入的讨论组
					avatar = R.mipmap.session_join_discus;
				}
				viewHolder.logo.setBackgroundResource(avatar);

				if (mShowDisGroupTag == -1 || mShowDisGroupTag == position) {
					viewHolder.tvContactItemTag.setVisibility(View.VISIBLE);
					viewHolder.tvContactItemTag.setText(R.string.contactChildOrg_discussionGroup);
					mShowDisGroupTag = position;
				} else {
					viewHolder.tvContactItemTag.setVisibility(View.GONE);
				}
				mTempList = mContactOrgDao.queryUsersByGroupName(tempContactGroup.getGroupName(), 9);
				if (tempContactGroup.getSubject() != null && !tempContactGroup.getSubject().isEmpty()) {
					viewHolder.tvContactItemOrgName.setText(tempContactGroup.getSubject());
				} else {
					viewHolder.tvContactItemOrgName.setText(tempContactGroup.getNaturalName());
				}
			}

//            int mOrgCount = 0;
//            if (mTempList != null) {
//                mOrgCount = mTempList.size();// 总人数
//            }
//            viewHolder.tvContactItemOrgCount.setText(mContext.getString(R.string.contactGroupOrg_departmentMemberCount,
//                    mOrgCount));
			int mOrgCount = 0;
			if (mTempList != null) {
				mOrgCount = mTempList.size();// 总人数
			}
			int index=0;
			for (User user: mTempList) {
				if (user.getUserStatus()==0){
					index++;
				}
			}
			viewHolder.tvContactItemOrgCount.setText((mOrgCount-index)+"/"+mOrgCount);
		}
		return convertView;
	}

	public void showUserListItem(ViewHolder viewHolder) {
		viewHolder.llContactItemOrg.setVisibility(View.GONE);
		viewHolder.llContactItemPerson.setVisibility(View.VISIBLE);
	}

	public void showOrgListItem(ViewHolder viewHolder) {
		viewHolder.llContactItemOrg.setVisibility(View.VISIBLE);
		viewHolder.llContactItemPerson.setVisibility(View.GONE);
	}

	@Override
	public int getCount() {
		if (nListObjects == null) {
			return 0;
		}
		return nListObjects.size();
	}

	@Override
	public Object getItem(int arg0) {
		if (nListObjects == null) {
			return null;
		}
		return nListObjects.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	class ViewHolder {
		ImageView logo;
		TextView tvContactItemPostName;
		TextView tvContactItemPersonName;
		CircleImageView ivContactItemPersonIcon;
		RelativeLayout llContactItemPerson;
		TextView tvContactItemOrgCount;
		TextView tvContactItemOrgName;
		RelativeLayout llContactItemOrg;
		TextView tvContactItemTag;
		TextView tvState;
	}

	public void startPersonInfoActivity(User mUser) {
		Intent infoIntent = new Intent(mContext, ContactPersonInfoActivity.class);
		infoIntent.putExtra("contactInfo", mUser);
		mContext.startActivity(infoIntent);
	}

	/**
	 * 打开时，界面右进左出
	 */
	public void enterMenuAnimation() {
		((Activity) mContext).overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
	}

	/**
	 * 返回是，界面左进右出
	 */
	public void backMenuAnimation() {
		((Activity) mContext).overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
	}
}
