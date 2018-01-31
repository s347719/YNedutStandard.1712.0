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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.event.DoneEvent;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;
import com.yineng.ynmessager.view.xrecyclerview.XRecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author 胡毅
 * 
 */
public class DemandFragment extends BaseEventFragment implements OnItemClickListener<DemandListAdapter.ViewHolder> {

	private DemandListAdapter mAdapter;
	//页码
	private int mCurrentPage = 0;

	/**
	 * 申请总计数
	 */
	private String totalElements;
	private int Hint= R.string.empty_hint_demand;
	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mAdapter = new DemandListAdapter(getApplicationContext());
		mAdapter.setOnItemClickListener(this);
		mAdapter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkDemandReviewData(v);
			}
		});
		mRcy_list.setAdapter(mAdapter);
		mRcy_list.setHasFixedSize(true);
	}
	private void checkDemandReviewData(View v) {
		int pos = (int) v.getTag();
		DoneEvent item = mAdapter.getItem(pos);
		if (item.isExpand()){
			item.setExpand(false);
		}else {
			item.setExpand(true);
		}
		refreshItem(item);

	}
	private void refreshItem(DoneEvent demandEventObj){
		int pos = getItemPostion(demandEventObj);
		if (pos < 0) {
			return;
		}
		setItem(pos,demandEventObj);
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mAdapter.setData(mData);
				mAdapter.notifyDataSetChanged();

			}
		});
	}
	List<DoneEvent> mData;
	private int getItemPostion(DoneEvent demandEvent) {
		mData = mAdapter.getData();
		if (mData == null || mData.size() <= 0) {
			return -1;
		}
		return mData.indexOf(demandEvent);
	}

	public void setItem(int pos,DoneEvent demandEvent) {
		mData.set(pos,demandEvent);
	}

	/**
	 * 获取第一页的申请数据
	 */
	public void refreshDemandList(boolean aotuRfresh,boolean isTop)
	{
		refreshDemandList(aotuRfresh,isTop,false, 0);
	}

	/**
	 * 请求第page页的数据
	 * @param isForceRefresh 是否从网络获取token
	 * @param page 页码
	 */
	private void refreshDemandList(boolean aotuRfresh,boolean isTop,boolean isForceRefresh, final int page) {
		loadEventDataByPageIndex(aotuRfresh,isTop,EVENT_DEMAND_TYPE, isForceRefresh, page);
	}

	@Override
	public void onRefresh() {
		refreshDemandList(false,true);
	}

	@Override
	public void onLoadMore() {
		refreshDemandList(false,false,false, mCurrentPage + 1);
	}
	@Override
	public void onRetry(XRecyclerView view) {
		refreshDemandList(true,true);
	}
	@Override
	public void onTaskGetJson(String responseString, int page) {
		List<DoneEvent> dataList = null;
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
                    refreshEventCount(totalElements, EVENT_DEMAND_TYPE);
                }
				int size = content.length();
				if (size > 0) {
                    dataList = new ArrayList<>(size);
                }
				dataList = com.alibaba.fastjson.JSONArray.parseArray(content,DoneEvent.class);
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
				List<DoneEvent> data = mAdapter.getData();
				data.addAll(dataList);
				Collections.sort(data,comparator);
            }
		} else
		{
			if(dataList!=null) {
				Collections.sort(dataList, comparator);
			}
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
	public void onItemClick(int position, DemandListAdapter.ViewHolder viewHolder) {
		DoneEvent mClickDoneEvent = mAdapter.getItem(position-1);

		if (mClickDoneEvent.getStatus()==0){
			if (mClickDoneEvent.getIsOnlyPcModify()){
				ToastUtil.toastAlerMessageCenter(getActivity(),"此申请只能在电脑上查看或修改",500);
			}else {
				startWebActivity(mClickDoneEvent.getFormSource(),mClickDoneEvent.getId(),mClickDoneEvent.getModifyUrl(),0,mClickDoneEvent.getName());
			}
		}else {
			if (mClickDoneEvent.getIsOnlyPcView()){
				ToastUtil.toastAlerMessageCenter(getActivity(),"此申请只能在电脑上查看或修改",500);
			}else {
				startWebActivity(mClickDoneEvent.getFormSource(),mClickDoneEvent.getId(),mClickDoneEvent.getViewUrl(),1,mClickDoneEvent.getName());
			}

		}
	}

	private Comparator<DoneEvent> comparator = new Comparator<DoneEvent>() {
		@Override
		public int compare(DoneEvent o1, DoneEvent o2) {
			return o1.getStatus()-o2.getStatus();
		}
	} ;
}
