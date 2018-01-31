package com.yineng.ynmessager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.contact.ContactGroup;

/**
 * 公共监听器，主要监听网络变更，讨论组信息变更及讨论组创建
 * @author 胡毅
 *
 */
public class CommonReceiver extends BroadcastReceiver {
	private final String TAG = "CommonReceiver";
	/**
	 * 讨论组信息变更监听器
	 */
	private updateGroupDataListener mUpdateGroupDataListener;
	/**
	 * 创建讨论组监听器
	 */
	private groupCreatedListener mGroupCreatedListener;
	/**
	 * 我退出讨论组的监听器
	 */
	private IQuitGroupListener mIQuitGroupListener;
	/**
	 * 网络状态变更监听器
	 */
	private netWorkChangedListener mNetWorkChangedListener;
	/**
	 * 身份验证过期
	 */
	private IdPastListener IdPastListener;
	
	/**
	 * 下线通知
	 */
	private onLoginedOtherListener mOnLoginedOtherListener;
	
	private onServiceNoticeListener mOnServiceNoticeListener;
	
	/**
	 * 从登录界面进入主界面的监听
	 */
	private onStartMainListener mOnMainActivityListener;
	
	private onEvaStatusRefreshListener mOnEvaStatusRefreshListener;
	
	public static final String BROADCAST_ACTION_REFRESH_EVA_STATUS = "broadcast_refresh_eva";
	/**
	 * 网络信息
	 */
	public static String mNetWorkTypeStr = "";
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		int mGroupType = intent.getIntExtra(Const.INTENT_GROUPTYPE_EXTRA_NAME, 0);
		// 网络变更
		if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
			String netType = NetWorkTypeUtils.getNetTypeForView(context);
			if (!mNetWorkTypeStr.equals(netType)) {
				mNetWorkTypeStr = netType;
				if (mNetWorkChangedListener != null) {
					mNetWorkChangedListener.netWorkChanged(netType);
				}
			}
		}
		
		//身份过期
		if (action.equals(Const.BROADCAST_ACTION_ID_PAST)) {
			if (IdPastListener != null) {
				IdPastListener.idPasted();
			}
		} else if (action.equals(Const.BROADCAST_ACTION_LOGINED_OTHER)) {
			if (mOnLoginedOtherListener != null) {
				mOnLoginedOtherListener.onLoginedOther();
			}
		}
				
		//用户重命名讨论组，添加成员到讨论组，被添加人员自动创建该讨论组的广播
		if (action.equals(Const.BROADCAST_ACTION_UPDATE_GROUP)) {
			if (mUpdateGroupDataListener != null) {
				mUpdateGroupDataListener.updateGroupData(mGroupType);
			}
		} 
		//用户创建讨论组,群组
		else if (action.equals(Const.BROADCAST_ACTION_CREATE_GROUP)) {
			if (mGroupCreatedListener != null) {
				ContactGroup tempGroup = (ContactGroup) intent.getSerializableExtra(Const.INTENT_GROUP_EXTRA_NAME);
				mGroupCreatedListener.groupCreated(tempGroup);
			}
		} 
		//其他用户退出讨论组,群组
		else if (action.equals(Const.BROADCAST_ACTION_QUIT_GROUP)) {
			if (mUpdateGroupDataListener != null) {
				mUpdateGroupDataListener.updateGroupData(mGroupType);
			}
		} 
		//我退出该讨论组;别人把我T出讨论组或群组
		else if (action.equals(Const.BROADCAST_ACTION_I_QUIT_GROUP)) {
			if (mIQuitGroupListener != null) {
				mIQuitGroupListener.IQuitMyGroup(mGroupType);
			}
			if (mUpdateGroupDataListener != null) {
				mUpdateGroupDataListener.updateGroupData(mGroupType);
			}
		}
		else if (action.equals(Const.BROADCAST_ACTION_SERVICE_NOTICE)) {
			if (mOnServiceNoticeListener != null) {
				mOnServiceNoticeListener.onServiceNoticed();
			}
		} else if (action.equals(Const.BROADCAST_ACTION_START_MAINACTIVITY)) {
			if (mOnMainActivityListener != null) {
				mOnMainActivityListener.onStartMainActivity();
			}
		} else if (action.equals(BROADCAST_ACTION_REFRESH_EVA_STATUS)) {
			if (mOnEvaStatusRefreshListener != null) {
				String planId = intent.getStringExtra("planId");
				mOnEvaStatusRefreshListener.onRefreshEvaStatus(planId);
			}
		}
	}
	
	public interface updateGroupDataListener{
		void updateGroupData(int mGroupType);
	}
	
	public interface groupCreatedListener{
		void groupCreated(ContactGroup tempGroup);
	}
	
	public interface IQuitGroupListener{
		void IQuitMyGroup(int mGroupType);
	}
	
	public void setUpdateGroupDataListener(
			updateGroupDataListener updateGroupDataListener) {
		this.mUpdateGroupDataListener = updateGroupDataListener;
	}

	public void setGroupCreatedListener(groupCreatedListener mGroupCreatedListener) {
		this.mGroupCreatedListener = mGroupCreatedListener;
	}

	public void setIQuitGroupListener(IQuitGroupListener mIQuitGroupListener) {
		this.mIQuitGroupListener = mIQuitGroupListener;
	}

	public interface netWorkChangedListener{
		void netWorkChanged(String netWorkInfo);
	}
	
	public void setNetWorkChangedListener(netWorkChangedListener mNetWorkChangedListener) {
		this.mNetWorkChangedListener = mNetWorkChangedListener;
	}
	
	public interface IdPastListener{
		void idPasted();
	}
	
	public void setIdPastListener(IdPastListener mIdPastListener) {
		this.IdPastListener = mIdPastListener;
	}
	
	public interface onLoginedOtherListener{
		void onLoginedOther();
	}
	
	public void setOnLoginedOtherListener(onLoginedOtherListener mOnLoginedOtherListener) {
		this.mOnLoginedOtherListener = mOnLoginedOtherListener;
	}
	
	public interface onServiceNoticeListener{
		void onServiceNoticed();
	}

	public void setOnServiceNoticeListener(
			onServiceNoticeListener mOnServiceNoticeListener) {
		this.mOnServiceNoticeListener = mOnServiceNoticeListener;
	}
	
	public interface onStartMainListener {
		void onStartMainActivity();
	}
	
	/**
	 * 设置跳转到主界面的监听器
	 * @param mainListener
	 */
	public void setStartMainActivityListener(onStartMainListener mainListener) {
		this.mOnMainActivityListener = mainListener;
	}
	
	public interface onEvaStatusRefreshListener {

		/**
		 * @param planId
		 */
		void onRefreshEvaStatus(String planId);
	}
	
	public void setOnEvaStatusRefreshListener(onEvaStatusRefreshListener mOnEvaStatusRefreshListener) {
		this.mOnEvaStatusRefreshListener = mOnEvaStatusRefreshListener;
	}
	
	
}
