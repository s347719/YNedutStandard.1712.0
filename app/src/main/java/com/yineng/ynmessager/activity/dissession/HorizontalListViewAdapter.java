package com.yineng.ynmessager.activity.dissession;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.contact.User;

import java.util.ArrayList;
import java.util.List;

public class HorizontalListViewAdapter extends BaseAdapter implements OnClickListener{
	private List<User> mMemberList = new ArrayList<User>();
	private LayoutInflater inflater;
	private onRemoveSelectedMemgerListener mOnRemoveSelectedMemgerListener;
	private boolean isAddActivity = false;
	public HorizontalListViewAdapter(Context context) {
		inflater = LayoutInflater.from(context);
	}

	public void setData(List<User> list) {
		this.mMemberList = list;
	}

	/**
	 * 设置底部画廊监听器
	 * @param isAddActivity 是否是添加联系人界面
	 * @param tempListener 删除已选中成员的监听器
	 */
	public void setAdapterAttr(boolean isAddActivity,onRemoveSelectedMemgerListener tempListener) {
		this.isAddActivity = isAddActivity;
		this.mOnRemoveSelectedMemgerListener = tempListener;
	}
	
	@Override
	public int getCount() {
		return mMemberList.size();
	}

	@Override
	public Object getItem(int position) {
		return mMemberList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHold viewHold = null;
		User mUser = mMemberList.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(
					R.layout.dis_create_gallary_item, parent,false);
			viewHold = new ViewHold();
			viewHold.mMemberItemLayout = (LinearLayout) convertView
			.findViewById(R.id.ll_dis_create_gallary_item);
			viewHold.mMemberIconIV = (ImageView) convertView
					.findViewById(R.id.iv_dis_create_gallary_item_head);
			viewHold.mMemberNameTV = (TextView) convertView
					.findViewById(R.id.tv_dis_create_gallary_item_name);
			convertView.setTag(viewHold);
		} else {
			viewHold = (ViewHold) convertView.getTag();
		}
//		L.e("adapter user name == "+mUser.getUserName());
//		viewHold.mMemberNameTV.setText(mUser.getUserName());
		if (isAddActivity) {
			viewHold.mMemberIconIV.setTag(mUser);
			viewHold.mMemberIconIV.setOnClickListener(HorizontalListViewAdapter.this);
		}
		return convertView;
	}

	class ViewHold {
		public LinearLayout mMemberItemLayout;
		ImageView mMemberIconIV;
		TextView mMemberNameTV;
	}
	
	public interface onRemoveSelectedMemgerListener {
		void onRemoveSelected(User mUser);
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View paramView) {
		if (mOnRemoveSelectedMemgerListener != null) {
			User mUser = (User) paramView.getTag();
			mOnRemoveSelectedMemgerListener.onRemoveSelected(mUser);
		}
	}
}
