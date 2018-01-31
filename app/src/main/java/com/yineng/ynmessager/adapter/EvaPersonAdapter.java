//***************************************************************
//*    2015-10-13  上午11:52:12
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.app.evaluate.EvaPersonBean;
import com.yineng.ynmessager.bean.contact.ContactOrg;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author 胡毅
 * 
 */
public class EvaPersonAdapter extends BaseAdapter {

	private Context mContext;
	private List<EvaPersonBean> mEvaPersonList = new ArrayList<EvaPersonBean>();

	public EvaPersonAdapter(Context context) {
		mContext = context;
	}

	public EvaPersonAdapter(Context context, List<EvaPersonBean> tempEvaPersonList) {
		mContext = context;
		mEvaPersonList = tempEvaPersonList;
	}

	public void setEvaPersonList(List<EvaPersonBean> mEvaPersonList) {
		this.mEvaPersonList = mEvaPersonList;
	}

	public List<EvaPersonBean> getEvaPersonList() {
		return mEvaPersonList;
	}

	@Override
	public int getCount() {
		return mEvaPersonList.size() == 0 ? 0 : mEvaPersonList.size();
	}

	@Override
	public Object getItem(int position) {
		return mEvaPersonList == null ? null : mEvaPersonList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PersonViewHolder mPersonViewHolder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_evaluate_choose_person_griditem, parent,
					false);
			mPersonViewHolder = new PersonViewHolder();
			mPersonViewHolder.mPersonHeadIV = (CircleImageView) convertView
					.findViewById(R.id.app_evaluate_choose_person_iv);
			mPersonViewHolder.mPersonStatusTV = (TextView) convertView
					.findViewById(R.id.app_evaluate_choose_eva_status);
			mPersonViewHolder.mPersonNameTV = (TextView) convertView
					.findViewById(R.id.app_evaluate_choose_person_name);
			convertView.setTag(mPersonViewHolder);
		} else {
			mPersonViewHolder = (PersonViewHolder) convertView.getTag();
		}
		EvaPersonBean itemEvaPersonBean = mEvaPersonList.get(position);
		mPersonViewHolder.mPersonNameTV.setText(itemEvaPersonBean
				.getEvaluateName());
		mPersonViewHolder.mPersonHeadIV.setAlpha(1f);
		mPersonViewHolder.mPersonStatusTV.setVisibility(View.GONE);
		if (itemEvaPersonBean.getEvaluateStatus() == 1) {
			mPersonViewHolder.mPersonHeadIV.setAlpha(0.5f);
			mPersonViewHolder.mPersonStatusTV.setVisibility(View.VISIBLE);
		}
		//设置头像
		File userIcon = FileUtil.getAvatarByName(itemEvaPersonBean.getId());
		if (userIcon == null || !userIcon.exists()) {

			mPersonViewHolder.mPersonHeadIV.setImageResource(R.mipmap.app_evaluate_user_head_icon);
		} else {
			mPersonViewHolder.mPersonHeadIV.setImageURI(Uri.fromFile(userIcon));
		}
		// mPlanViewHolder.mPlanEvaCountTV.setText(getString(R.string.evaluateCount,
		// itemEvaPlanBean.getEvaluateCount()));
		// mPlanViewHolder.mPlanEvaedCountTV.setText(getString(R.string.evaluatedCount,
		// itemEvaPlanBean.getEvaluatedCount()));
		// mPlanViewHolder.mPlanDurationTV.setText(itemEvaPlanBean.getBeginTime()+" ~ "+itemEvaPlanBean.getEndTime());
		// mPlanViewHolder.mPlanCountDownTV.setText(itemEvaPlanBean.getRestTime());
		return convertView;
	}

	public class PersonViewHolder {
		public CircleImageView mPersonHeadIV;
		public TextView mPersonStatusTV;
		public TextView mPersonNameTV;
	}

	/**
	 * 
	 */
	public void clearData() {
		mEvaPersonList.clear();
	}
}