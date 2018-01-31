//***************************************************************
//*    2015-6-26  下午3:44:57
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yineng.ynmessager.bean.app.AppsBean;
import com.yineng.ynmessager.db.UserAccountDB;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 胡毅 主页-应用模块数据库操作类
 */
/**
 * @author 胡毅
 *
 */
public class AppsDao {

	private final String mAppTableName = "Apps";
	private SQLiteDatabase mDB = null;

	public AppsDao(Context context) {
		mDB = UserAccountDB.getInstance(context,
				LastLoginUserSP.getInstance(context).getUserAccount())
				.getReadableDatabase();
	}

	public synchronized void saveAllAppsData(List<AppsBean> mList) {
		removeAllAppsData();
		mDB.beginTransaction();
		for (AppsBean mAppsBean : mList) {
			inserOneAppData(mAppsBean);
		}
		mDB.setTransactionSuccessful();
		mDB.endTransaction();
	}

	/**
	 * 插入一条App数据
	 * 
	 * @param mAppsBean
	 */
	private void inserOneAppData(AppsBean mAppsBean) {
		ContentValues appTreeContentValues = new ContentValues();
		appTreeContentValues.put("id", mAppsBean.getId());
		appTreeContentValues.put("name", mAppsBean.getName());
		appTreeContentValues.put("parentId", mAppsBean.getParentId());
		appTreeContentValues.put("parentName", mAppsBean.getParentName());
		appTreeContentValues.put("belong", mAppsBean.getBelong());
		appTreeContentValues.put("reqUrl", mAppsBean.getReqUrl());
		mDB.insert(mAppTableName, null, appTreeContentValues);
	}
	
	/**
	 * 获取根应用对象数据
	 * @return
	 */
	public ArrayList<AppsBean> queryAppRootBeans() {
		ArrayList<AppsBean> mList = null;
		Cursor mCursor = mDB.query(mAppTableName, null, "parentId = 0", null,
				null, null, null);
		if (mCursor != null && mCursor.getCount() > 0) {
			mList = new ArrayList<AppsBean>();
			while (mCursor.moveToNext()) {
				String id = mCursor.getString(mCursor.getColumnIndex("id"));
				String name = mCursor.getString(mCursor.getColumnIndex("name"));
				String parentId = mCursor.getString(mCursor.getColumnIndex("parentId"));
				String parentName = mCursor.getString(mCursor.getColumnIndex("parentName"));
				String belong = mCursor.getString(mCursor.getColumnIndex("belong"));
				String reqUrl = mCursor.getString(mCursor.getColumnIndex("reqUrl"));
				AppsBean tempAppsBean = new AppsBean(id, name, parentId, parentName, belong, 
						reqUrl);
				mList.add(tempAppsBean);
			}
		}
		if (mCursor != null) {
			mCursor.close();
		}
		return mList;
	}
	
	/**
	 * 根据父应用id获取当前系统下的应用对象数据
	 * @return
	 */
	public ArrayList<AppsBean> queryAppBeansByParentId(String mParentId) {
		ArrayList<AppsBean> mList = null;
		Cursor mCursor = mDB.query(mAppTableName, null, "parentId = ?", new String[]{mParentId},
				null, null, null);
		if (mCursor != null && mCursor.getCount() > 0) {
			mList = new ArrayList<AppsBean>();
			while (mCursor.moveToNext()) {
				String id = mCursor.getString(mCursor.getColumnIndex("id"));
				String name = mCursor.getString(mCursor.getColumnIndex("name"));
				String parentId = mCursor.getString(mCursor.getColumnIndex("parentId"));
				String parentName = mCursor.getString(mCursor.getColumnIndex("parentName"));
				String belong = mCursor.getString(mCursor.getColumnIndex("belong"));
				String reqUrl = mCursor.getString(mCursor.getColumnIndex("reqUrl"));
				AppsBean tempAppsBean = new AppsBean(id, name, parentId, parentName, belong, reqUrl);
				mList.add(tempAppsBean);
			}
		}
		if (mCursor != null) {
			mCursor.close();
		}
		return mList;
	}

	/**
	 * 查询二三级数据
	 * @return
	 */
	public ArrayList<AppsBean> queryAllChildrenAppBeans() {
		ArrayList<AppsBean> mList = null;
		ArrayList<AppsBean> mFirstMenusList = queryAppRootBeans();
		if (mFirstMenusList != null && mFirstMenusList.size() > 0) {
			mList = new ArrayList<AppsBean>();
			for (AppsBean appsBean : mFirstMenusList) {
				ArrayList<AppsBean> mSecondMenuBeans = queryAppBeansByParentId(appsBean.getId());
				if (mSecondMenuBeans != null) {
					for (AppsBean secondBean : mSecondMenuBeans) {
						ArrayList<AppsBean> mThirdMenuBeans = queryAppBeansByParentId(secondBean.getId());
						if (mThirdMenuBeans != null && mThirdMenuBeans.size() > 0) {
							mThirdMenuBeans.get(0).setShowTag(1);
							mList.addAll(mThirdMenuBeans);
						}
					}
				}
			}
		}
		return mList;
	}
	
	/**
	 * 删除应用表中所有数据
	 */
	public void removeAllAppsData() {
		mDB.delete(mAppTableName, null, null);
	}

}
