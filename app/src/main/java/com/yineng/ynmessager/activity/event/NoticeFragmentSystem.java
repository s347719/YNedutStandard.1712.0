//***************************************************************
//*    2015-6-24  上午10:23:10
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseFragment;
import com.yineng.ynmessager.activity.session.PlatformNoticeActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.CommonEvent;
import com.yineng.ynmessager.bean.event.NoticeEvent;
import com.yineng.ynmessager.bean.event.NoticeEventEntity;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.okHttp.callback.JSONObjectCallBack;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.V8TokenManager;
import com.yineng.ynmessager.util.address.URLs;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;
import com.yineng.ynmessager.view.xrecyclerview.XRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;


/**
 * @author
 */
public class NoticeFragmentSystem extends BaseFragment implements OnItemClickListener {
    private XRecyclerView mRecyclerView;
    private View mEmptyView;
    private NoticeListAdapter mAdapter;
    private LocalBroadcastManager mBroadcastManager;
    private PlatformNoticeActivity mParentFragment;
    private BroadcastReceiver mNoticeEventRefreshBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (NoticeEvent.ACTION_REFRESH.equals(intent.getAction())) {
                mRecyclerView.refreshComplete();
                mRecyclerView.loadMoreComplete();
                setClear();
                mRecyclerView.refresh();
            }
        }
    };
    // 在当前页面使用cordova去连接网页
    private int messageType = 0;//初始化当前所属类型的时候为1代表系统消息
    private AppController appController;
    //获取数据默认从第一页开始获取
    private int pageNum = 0;
    //根据数据获取的值判断是否是最后一页
    private boolean lastPage = false;
    private ArrayList<NoticeEvent> dataList = new ArrayList<>();
    private String receiver;
    private int empty_hint;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_event_notice, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = getContext();
        empty_hint = R.string.main_eventNoticeEmpty;
        appController = AppController.getInstance();
        receiver = LastLoginUserSP.getLoginName(mParentFragment);
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
        mBroadcastManager.registerReceiver(mNoticeEventRefreshBroadcastReceiver, new IntentFilter(NoticeEvent.ACTION_REFRESH));
        mRecyclerView = (XRecyclerView) view.findViewById(R.id.recyclerview);
        mEmptyView = view.findViewById(R.id.text_empty);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setOnRetryListener(new XRecyclerView.OnRetryListener() {
            @Override
            public void onRetry(XRecyclerView view) {
                getNoticeData(messageType, pageNum);
            }
        });
        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                setClear();
                getNoticeData(messageType, pageNum);
            }

            @Override
            public void onLoadMore() {
                if (!lastPage) {
                    pageNum += 1;
                    getNoticeData(messageType, pageNum);
                } else {
                    mRecyclerView.setNoMore(true);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        mAdapter = new NoticeListAdapter(dataList, mParentActivity);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.refresh();

    }

    /**
     * 联网获取数据
     *
     * @param messType
     */
    private void getNoticeData(final int messType, final int pageNum) {
        Map<String,String> params = new HashMap<>();
        if (messType == 2) {
            params.put("msgType", "");
        } else {
            params.put("msgType", messType + "");
        }

        params.put("pageNumber", pageNum + "");
        params.put("pageSize", 10 + "");
        params.put("userId", LastLoginUserSP.getLoginUserNo(getContext()));
        params.put("access_token", appController.mAppTokenStr);
        OKHttpCustomUtils.get(URLs.GET_NOTICE_URL_INFO, params, getActivity(), new JSONObjectCallBack() {
            @Override
            public void onResponse(JSONObject resObj, int id) {
                try {
                    int status = resObj.optInt("status");
                    if (status == 0) {
                        if (pageNum == 0) {
                            dataList.clear();
                        }
                        JSONObject resultObj = resObj.getJSONObject("result");
                        lastPage = resultObj.optBoolean("lastPage");
                        String result = resultObj.optString("content");
                        List<NoticeEventEntity> noticeEventEntities = JSON.parseArray(result, NoticeEventEntity.class);
                        //为了解决三星手机在数据不满屏的情况下下拉刷新ANR的情况
                        if (noticeEventEntities.size() > 0) {
                            for (int i = 0; i < noticeEventEntities.size(); i++) {
                                NoticeEvent event = new NoticeEvent();
                                NoticeEventEntity eventEntity = noticeEventEntities.get(i);
                                event.setUserNo(eventEntity.getSenderId());
                                event.setUserName("sendName");
                                event.setReceiver(receiver);
                                event.setTitle(eventEntity.getSubject());
                                event.setMessage(eventEntity.getMsgContent());
                                event.setContent(eventEntity.getMsgContent());
                                event.setMsgId(eventEntity.getMsgId());
                                event.setHasAttachment(eventEntity.getHasAttachment());
                                event.setTimeStamp(TimeUtil.convertStringDate(eventEntity.getSendTime()));
                                event.setHasPic(eventEntity.getHasPic());
                                event.setMessageType(eventEntity.getMessageType());
                                event.setRead(!eventEntity.getReadStatus().equals("0"));
                                dataList.add(event);
                            }
                            //判断是上拉加载还是下拉刷新
                            if (pageNum == 0) {
                                mRecyclerView.refreshComplete();
                            } else {
                                mRecyclerView.loadMoreComplete();
                            }
                            mAdapter.setData(dataList);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            if (pageNum == 0) {
                                mRecyclerView.setEmptyView(mEmptyView, empty_hint);
                                mRecyclerView.refreshComplete();
                            } else {
                                mRecyclerView.loadMoreError();
                            }
                        }
                        //判断是否是最后一页显示没有数据提示
                        if (lastPage) {
                            mRecyclerView.setNoMore(true);
                        }
                    } else {
                        if (pageNum == 0) {
                            mRecyclerView.setEmptyView(mEmptyView, empty_hint);
                            mRecyclerView.refreshComplete();
                        } else {
                            mRecyclerView.loadMoreError();
                        }
                    }
                } catch (JSONException e) {
                    L.e(mTag, e.getMessage(), e);
                    if (pageNum == 0) {
                        mRecyclerView.setEmptyView(mEmptyView, empty_hint);
                        mRecyclerView.refreshComplete();
                    } else {
                        mRecyclerView.loadMoreError();
                    }
                }
            }

            @Override
            public void onError(Call call, Exception e, int i) {
                super.onError(call, e, i);
                mRecyclerView.setEmptyView(mEmptyView, empty_hint);
                if (pageNum == 0) {
                    if (dataList.size() > 0) {
                        dataList.clear();
                    }
                    mRecyclerView.refreshComplete();
                } else {
                    mRecyclerView.loadMoreError();
                }
            }
        });
    }

    //由于把通知修改倒主页消息列表的平台通知中去，所以注释掉事件模块的引用代码，huyi 2015.1.7
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mParentFragment = ((PlatformNoticeActivity) mParentActivity);
    }


    // 注册EventBus 必须要实现的方法，不可删除
    //在其他页面有相同msgId消息被刷新的时候在这里也需要刷新
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CommonEvent event) {
        switch (event.getWhat()) {
            case Const.SCAN_NOTICEEVENT:
                String msgId = (String) event.getObj();
                for (int i = 0; i < dataList.size(); i++) {
                    if (dataList.get(i).getMsgId().equals(msgId)) {
                        dataList.get(i).setRead(true);
                        mAdapter.notifyDataSetChanged();
                        break;
                    }
                }
                break;
        }
    }

    /**
     * 还原数据
     */
    private void setClear() {
        pageNum = 0;
        lastPage = false;
    }

    @Override
    public void onResume() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            if (dataList.size() == 0) {
                mRecyclerView.refresh();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBroadcastManager.unregisterReceiver(mNoticeEventRefreshBroadcastReceiver);
    }

    /**
     * 访问服务器接口
     */
    private void refreshNoticeReadStatus(NoticeEvent noticeEvent, int position, boolean requestV8) {
        if (!noticeEvent.isRead()) {
            noticeEvent.setRead(true);
            EventBus.getDefault().post(new CommonEvent(Const.SCAN_NOTICEEVENT, noticeEvent.getMsgId()));
            mAdapter.notifyItemChanged(position);
            if (!requestV8) {
                return;
            }
            String url = URLs.NOTICE_UPDATE_STATUS_URL;
            Map<String,String> map  = new HashMap<>();
            String contentParam = formatNoticeStatusJson(noticeEvent);
            map.put("access_token",V8TokenManager.sToken);
            map.put("content",contentParam);
            //给url赋上参数
            OKHttpCustomUtils.post(url, map, new JSONObjectCallBack() {
                @Override
                public void onResponse(JSONObject response, int id) {

                }


            });

        }
    }

    /**
     * 格式化更新通知为已读的json作为参数给服务器
     *
     * @param noticeEvent
     * @return
     */
    private String formatNoticeStatusJson(NoticeEvent noticeEvent) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\"readerId\":\"" + LastLoginUserSP.getLoginUserNo(getContext()) + "\",");
        jsonBuilder.append("\"msgArr\":[{");
        jsonBuilder.append("\"msgId\":[\"" + noticeEvent.getMsgId() + "\"],");
        jsonBuilder.append("\"messageType\":" + noticeEvent.getMessageType());
        jsonBuilder.append("}]}");
        return jsonBuilder.toString();
    }

    @Override
    public void onItemClick(int position, RecyclerView.ViewHolder viewHolder) {
        final NoticeEvent event = mAdapter.getItem(position - 1);

        //更新系统消息为已读状态
        refreshNoticeReadStatus(event, position, true);
    }

}
