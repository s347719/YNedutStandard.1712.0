//***************************************************************
//*    2015-5-25  上午10:48:33
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：sumknot@foxmail.com
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.event;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.event.TodoEvent;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;
import com.yineng.ynmessager.view.xrecyclerview.XRecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 胡毅
 * 
 */
public class DoneFragment extends BaseEventFragment implements OnItemClickListener<DoneListAdapter.ViewHolder>
{
	//已办的列表适配器
	private DoneListAdapter mAdapter;
	//页码
	private int mCurrentPage = 0;

	/**
	 * 已办总计数
	 */
	private String totalElements;
	private int Hint= R.string.empty_hint_done;
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mAdapter = new DoneListAdapter(getApplicationContext());
		mAdapter.setOnItemClickListener(this);
		mRcy_list.setAdapter(mAdapter);
	}

	/**
	 * 获取第一页的已办数据
	 */
	public void refreshDoneList(boolean aotuRfresh,boolean isTop)
	{
		refreshDoneList(aotuRfresh,isTop,false, 0);
	}

	/**
	 * 请求第page页的数据
	 * @param isForceRefresh 是否从网络获取token
	 * @param page 页码
	 */
	private void refreshDoneList(boolean aotuRfresh,boolean isTop,boolean isForceRefresh, final int page) {
		loadEventDataByPageIndex(aotuRfresh,isTop,EVENT_DONE_TYPE, isForceRefresh, page);
	}

	@Override
	public void onRefresh() {
		refreshDoneList(false,true);
	}

	@Override
	public void onLoadMore() {
		refreshDoneList(false,false,false, mCurrentPage + 1);
	}
	@Override
	public void onRetry(XRecyclerView view) {
		refreshDoneList(true,true);
	}
	@Override
	public void onTaskGetJson(String responseString, int page) {
		List<TodoEvent> dataList = null;
		try
		{
			JSONObject jsonResp = new JSONObject(responseString);
			if ("0".equals(jsonResp.getString("status"))) {
				JSONObject result = jsonResp.getJSONObject("result");
				String content = result.getString("content");
				//解析已办总数量
				totalElements = result.optString("totalElements");
				//刷新已办总数量
				if (isAdded()) {
                    refreshEventCount(totalElements, EVENT_DONE_TYPE);
                }
				if (!TextUtils.isEmpty(content)) {
                    dataList = new ArrayList<>();
                }
				dataList = com.alibaba.fastjson.JSONArray.parseArray(content,TodoEvent.class);
			}

		} catch (JSONException e)
		{
			L.w(mTag, e.getMessage(), e);
			if (dataList != null) {
                dataList.clear();
            }
		}
		if (page > 0)
		{
			if (dataList != null) {
				mAdapter.getData().addAll(dataList);
			}
		} else
		{
			mAdapter.setData(dataList);
		}
		mAdapter.notifyDataSetChanged();

		if (dataList != null && dataList.size() > 0) {
			mCurrentPage = page;
			mRcy_list.setNoMore(false);
		}else {
			mRcy_list.setNoMore(true);
		}
	}

	@Override
	public void onTaskFinish(boolean isTop) {
		if (mAdapter.getItemCount() > 0) {
			if (isTop){
				mRcy_list.refreshComplete();
			}else {
				mRcy_list.loadMoreComplete();
			}
		} else {
			if (isTop){
				mRcy_list.refreshComplete();
			}else {
				mRcy_list.loadMoreComplete();
			}
			mRcy_list.setEmptyView(mEmptyView,Hint);
		}
	}


	@Override
	public void onItemClick(int position, DoneListAdapter.ViewHolder viewHolder) {
		TodoEvent mClickDoneEvent = mAdapter.getItem(position-1);
		if (!(mClickDoneEvent.isOnlyPcView())){
			startWebActivity(mClickDoneEvent.getFormSource(),mClickDoneEvent.getId(),mClickDoneEvent.getViewUrl(),1,mClickDoneEvent.getName());
		}else {
            ToastUtil.toastAlerMessageCenter(getActivity(),"此申请只能在电脑上查看",500);
        }
	}
}
