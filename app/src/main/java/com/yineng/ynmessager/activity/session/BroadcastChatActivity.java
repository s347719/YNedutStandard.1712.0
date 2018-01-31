package com.yineng.ynmessager.activity.session;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.BroadcastChat;
import com.yineng.ynmessager.bean.RecentChat;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.offline.HistoryMsg;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.dao.BroadcastChatDao;
import com.yineng.ynmessager.db.dao.RecentChatDao;
import com.yineng.ynmessager.manager.NoticesManager;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.smack.MessagePacketListenerImpl;
import com.yineng.ynmessager.smack.ReceiveBroadcastChatCallBack;
import com.yineng.ynmessager.smack.ReceiveReqIQCallBack;
import com.yineng.ynmessager.smack.ReqIQ;
import com.yineng.ynmessager.smack.ReqIQResult;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.NetWorkUtil;
import com.yineng.ynmessager.util.OfflineMessageUtil;
import com.yineng.ynmessager.util.TextUtil;
import com.yineng.ynmessager.util.TimeUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 广播查看界面
 *
 * @author Yutang
 */
public class BroadcastChatActivity extends BaseActivity implements
        OnClickListener, ReceiveBroadcastChatCallBack, DialogInterface.OnClickListener, ReceiveReqIQCallBack {
    public static final String EXTRA_KEY_LIST = "list";
    public static final String EXTRA_KEY_INDEX = "index";
    //获取历史消息
    private final int GET_HISTORY = 7;
    //刷新ui
    private final int REFRESH_UI = 2;
    private TextView mTxtPrevious;
    /**
     * / 显示广播内容的listView
     */
    private ListView mRefreshListView;
    private LinkedList<BroadcastChat> mBroadcastList = new LinkedList<BroadcastChat>();
    private LinearLayout mEmptyView;
    private BroadcastChatDao mBroadcastChatDao;
    // 消息列表操作
    private RecentChatDao mRecentChatDao;
    private BroadcastListviewAdapter mBroadcastListviewAdapter;
    private Context mContext;
    private AlertDialog mDlgDelConfirm;
    private BroadcastChat mChatToDelete = null;
    private PullToRefreshListView mPullToRefreshListView;
    private ContactOrgDao mContactOrgDao;
    //页码
    private int mPage = 0;
    /**
     * 离线消息集合
     */
    private List<BroadcastChat> messageList = new ArrayList<>();
    //历史消息数量
    private final int GET_HISTORY_COUNT = 40;
    /**
     * XMPP连接管理类实例
     */
    protected XmppConnectionManager mXmppConnManager;
    /**
     * 取消搜索框动画
     */
    protected final int UPDATE_LIST_VIEW = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_LIST_VIEW:
                    mRefreshListView.setSelection(0);
                    mBroadcastListviewAdapter.notifyDataSetChanged();
                    break;
                case GET_HISTORY:
                    addHistoryOrUpdateMessageList();
                    break;
                case REFRESH_UI:
                    final int mBeforeMoreCount = mBroadcastListviewAdapter.getCount();
                    refreshUIByPageIndex();
                    // mHandler.sendEmptyMessage(0);
                    final int mAfterMoreCount = mBroadcastListviewAdapter.getCount();
                    final int selectIndex = mAfterMoreCount - mBeforeMoreCount;
                    mPullToRefreshListView.onRefreshComplete();
                    if (selectIndex >= 0) {
                        mRefreshListView.post(new Runnable() {
                            @Override
                            public void run() {
                                hideProgessD();
                                mRefreshListView.setSelection(mBeforeMoreCount);
                            }
                        });
                    }
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_broadcast_layout);
        mXmppConnManager = XmppConnectionManager.getInstance();
        mXmppConnManager.addReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_OFFLINE_USER_LIST, this);
        mContactOrgDao = new ContactOrgDao(mContext);
        initView();
        // 初始状态，从本地获得10条数据，刷新UI
        showProgressD("加载中...", true);
        mHandler.post(mRefreshNextPageRunable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUnreadRecentChatCount();
    }

    /**
     * view初始化
     */
    private void initView() {
        mContext = this;
        mRecentChatDao = new RecentChatDao(mContext);
        mBroadcastChatDao = new BroadcastChatDao(mContext);
        mBroadcastListviewAdapter = new BroadcastListviewAdapter(mContext, mBroadcastList);
        XmppConnectionManager.getInstance().setReceiveBroadcastChatCallBack(this);

        mTxtPrevious = (TextView) findViewById(R.id.broadcastChat_txt_previous);
        mTxtPrevious.setOnClickListener(this);
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pl_broadcast_pull_refresh_list);
        //支持上拉
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        mRefreshListView = mPullToRefreshListView.getRefreshableView();
        mEmptyView = (LinearLayout) findViewById(R.id.ll_session_broadcast_empty);
        mRefreshListView.setEmptyView(mEmptyView);
        mRefreshListView.setAdapter(mBroadcastListviewAdapter);

        // 单击查看记录
        mRefreshListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(mContext,
                        BroadcastChatContentActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(EXTRA_KEY_LIST, mBroadcastList);
                bundle.putInt(EXTRA_KEY_INDEX, position);
                intent.putExtras(bundle);
                startActivityForResult(intent, 0);
            }
        });

        mRefreshListView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mChatToDelete = (BroadcastChat) parent.getItemAtPosition(position);
                mDlgDelConfirm.show();
                return true;
            }
        });

        mDlgDelConfirm = new AlertDialog.Builder(this)
                .setItems(new String[]{getString(R.string.broadcastChat_delete)}, this)
                .create();

        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                mHandler.post(mRefreshNextPageRunable);
            }
        });
    }
    /**
     * 加载下一页的消息
     */
    protected Runnable mRefreshNextPageRunable = new Runnable() {
        @Override
        public void run() {
            //拉取离线消息
            sendHistoryMsgIQ();
            android.os.Message msgMessage = mHandler.obtainMessage();
            msgMessage.what = REFRESH_UI;
            mHandler.sendMessageDelayed(msgMessage, 1000);
        }
    };

    private void refreshUIByPageIndex() {
        LinkedList<BroadcastChat> result = mBroadcastChatDao.queryBroadcastChatPage(mPage, 10);
        if (result.size() > 0) {
            for (BroadcastChat chat : result) {
                if (mBroadcastList.contains(chat)) {
                    continue;
                }
                mBroadcastList.addLast(chat);
            }
            mPage++;
        }
        Collections.sort(mBroadcastList, new ComparatorMessageTimeDESC());
        mBroadcastListviewAdapter.notifyDataSetChanged();
    }

    /**
     * 添加历史消息
     */
    private void addHistoryOrUpdateMessageList() {
        try {
            if (messageList == null) return;
            Collections.sort(messageList, new ComparatorMessageTimeDESC());
            Iterator<BroadcastChat> iterator = messageList.iterator();
            while (iterator.hasNext()) {
                BroadcastChat chat = iterator.next();
                //获取未读广播列表，判断当条广播是否已读
                Set<String> unreadSet = LastLoginUserSP.getInstance(this).getUnreadBroadcastIds();
                for (String unreadId : unreadSet) {
                    if (chat.getPacketId().equals(unreadId)) {
                        chat.setIsRead(0);
                    }
                }
                mBroadcastChatDao.saveOrUpdate(chat);
                iterator.remove();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送获取历史消息iq
     */
    private void sendHistoryMsgIQ() {
        Collections.sort(mBroadcastList, new ComparatorMessageTimeDESC());
        String sendTime;
        try {
            BroadcastChat sendBroadcast = mBroadcastList.get(mBroadcastListviewAdapter.getCount() - 1);
            sendTime = sendBroadcast.getDateTime();
        } catch (Exception e) {
            e.printStackTrace();
            sendTime = "";
        }
        //拉取最老的一条消息
        ReqIQ iq = new ReqIQ();
        iq.setFrom(JIDUtil.getJIDByAccount(LastLoginUserSP.getInstance(this).getUserAccount()));
        iq.setTo("admin@" + mXmppConnManager.getServiceName());
        iq.setAction(2);
        iq.setNameSpace(Const.REQ_IQ_XMLNS_GET_OFFLINE_USER_LIST);
        HistoryMsg historyMsg = new HistoryMsg();
        historyMsg.setChatType(Const.CHAT_TYPE_BROADCAST_RECEIVER);
        historyMsg.setChatId("");
        historyMsg.setSendTime(sendTime);
        historyMsg.setMsgCount(String.valueOf(GET_HISTORY_COUNT));
        String paramsJsonStr = JSON.toJSONString(historyMsg);
        iq.setParamsJson(paramsJsonStr);
        if (NetWorkUtil.isNetworkAvailable(this)) {
            mXmppConnManager.sendPacket(iq);
        }
        L.i("拉取历史消息IQ:" + iq.toXML());
    }


    /**
     * 接收历史回执消息
     *
     * @param packet
     */
    @Override
    public void receivedReqIQResult(ReqIQResult packet) {
        //拉取到历史消息
        if (packet.getNameSpace().equals(Const.REQ_IQ_XMLNS_GET_OFFLINE_USER_LIST)) {
            List<HashMap> messagesJson = JSON.parseArray(packet.getResp(), HashMap.class);
            if (messagesJson.size() > 0) {
                for (int i = 0; i < messagesJson.size(); i++) {
                    String xml = String.valueOf(messagesJson.get(i).get("msg"));
                    org.jivesoftware.smack.packet.Message message = OfflineMessageUtil.parserMessage(xml);
                    // 过滤广播Message Body中的多余json
                    String content = message.getBody();
                    String mFromAccount = JIDUtil.getAccountByJID(message.getFrom().trim());
                    try {
                        JSONObject bodyJson = new JSONObject(content);
                        content = bodyJson.optString("content");
                        content = TextUtil.trimEnd(content).toString();
                        content = Jsoup.parse(content).text();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    User mUser = mContactOrgDao.queryUserInfoByUserNo(mFromAccount);
                    String mUserName;
                    if (mUser != null) {
                        mUserName = mUser.getUserName();
                    } else {
                        mUserName = mFromAccount;
                    }
                    BroadcastChat broadcast = MessagePacketListenerImpl.createBroadcastChatByMessage(message, content, mFromAccount, mUserName);
                    broadcast.setIsRead(1);
                    messageList.add(broadcast);
                }
            }
            android.os.Message msgMessage = mHandler.obtainMessage();
            msgMessage.what = GET_HISTORY;
            mHandler.sendMessage(msgMessage);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDlgDelConfirm.dismiss();
        XmppConnectionManager.getInstance().clearReceiveBroadcastChatCallBack();
        mXmppConnManager.removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_OFFLINE_USER_LIST);
        NoticesManager.getInstance(this).updateRecentChatList(Const.BROADCAST_ID,
                Const.CHAT_TYPE_BROADCAST);
        // 更新最近会话列表
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.broadcastChat_txt_previous:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mChatToDelete == null) {
            return;
        }
        BroadcastChat broadcast = mChatToDelete;
        mBroadcastChatDao.delete(broadcast);
        refreshUIByPageIndex();
        // 查询删除后的最后一条广播，更新到最近会话数据库中
        BroadcastChat lastBroadcast = mBroadcastChatDao.queryLast();
        RecentChat recentBroadcast = mRecentChatDao.queryByChatType(4).get(0);
        if (recentBroadcast != null && lastBroadcast != null) {
            recentBroadcast.setSenderNo(lastBroadcast.getUserNo());
            recentBroadcast.setContent(lastBroadcast.getTitle());
            recentBroadcast.setDateTime(lastBroadcast.getDateTime());
            mRecentChatDao.updateRecentChat(recentBroadcast);
        } else if (lastBroadcast == null) {
            mRecentChatDao.deleteRecentChatByUserNo(recentBroadcast.getUserNo());
        }
    }

    @Override
    public void onReceiveBroadcastChat(BroadcastChat broadcast) {
        if (!mBroadcastList.contains(broadcast)) {
            mBroadcastList.addFirst(broadcast);
        } else {
            mBroadcastList.remove(broadcast);
            mBroadcastList.addFirst(broadcast);
        }
        mHandler.sendEmptyMessage(UPDATE_LIST_VIEW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (data != null) {
                Bundle mBundle = data.getExtras();
                ArrayList<String> reads = mBundle.getStringArrayList("readList");
                if (reads.size() > 0) {
                    for (String readIds : reads) {
                        for (BroadcastChat bc : mBroadcastList) {
                            if (readIds.equals(bc.getPacketId())) {
                                bc.setIsRead(1);
                                break;
                            }
                        }
                    }
                    mBroadcastListviewAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    /**
     * 更新未读记录
     */
    private void updateUnreadRecentChatCount() {
        int unReadNum = mBroadcastChatDao.queryUnReadNum();
        mRecentChatDao.updateUnreadCount(Const.BROADCAST_ID,
                Const.CHAT_TYPE_BROADCAST, unReadNum);// 设置未读记录
        Intent intent = new Intent();
        intent.setAction(Const.ACTION_UPDATE_UNREAD_COUNT);
        this.sendBroadcast(intent);
    }

    private void loadBroadcastByPage() {
        new AsyncTask<Void, Integer, List<BroadcastChat>>() {

            @Override
            protected List<BroadcastChat> doInBackground(Void... params) {
                return mBroadcastChatDao.queryAll();
            }

            // 返回数据
            @Override
            protected void onPostExecute(List<BroadcastChat> result) {
                mBroadcastList.clear();
                mBroadcastList.addAll(result);
                mBroadcastListviewAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    public class BroadcastListviewAdapter extends BaseAdapter {
        private List<BroadcastChat> mList;
        private Context mContext;
        private LayoutInflater inflater = getLayoutInflater();

        public BroadcastListviewAdapter(Context context,
                                        List<BroadcastChat> datalist) {
            mContext = context;
            this.mList = datalist;
        }

        public void setData(List<BroadcastChat> mList) {
            this.mList = mList;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View v, ViewGroup parent) {
            //  Auto-generated method stub
            ViewHolder viewHolder = null;
            BroadcastChat broadcast = mList.get(position);
            if (v == null) {
                viewHolder = new ViewHolder();
                v = inflater.inflate(
                        R.layout.item_session_broadcastchat, parent, false);
                viewHolder.sendername = (TextView) v
                        .findViewById(R.id.broadcastChat_listItem_sender);
                viewHolder.datatime = (TextView) v
                        .findViewById(R.id.broadcastChat_txt_item_timestamp);
                viewHolder.title = (TextView) v
                        .findViewById(R.id.broadcastChat_listItem_title);
                viewHolder.content = (TextView) v
                        .findViewById(R.id.broadcastChat_listItem_content);
                viewHolder.isread = (TextView) v
                        .findViewById(R.id.broadcastChat_listItem_hasRead);
                v.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) v.getTag();
            }
            viewHolder.sendername.setText(getString(R.string.broadcastChat_sender, broadcast.getUserName()));
            Date dateTime = new Date(TimeUtil.getMillisecondByDate(broadcast.getDateTime(), TimeUtil.FORMAT_DATETIME_24_mic));
            viewHolder.datatime.setText(TimeUtil.getTimeRelationFromNow2(mContext, dateTime));
            viewHolder.title.setText(broadcast.getTitle());
            viewHolder.content.setText(broadcast.getMessage());
            if (broadcast.getIsRead() == 0) {
                viewHolder.isread.setVisibility(View.VISIBLE);
            } else {
                viewHolder.isread.setVisibility(View.GONE);
            }
            return v;
        }
    }

    private final class ViewHolder {
        public TextView sendername;
        public TextView title;
        public TextView datatime;
        public TextView isread;
        public TextView content;
    }
    public static class ComparatorMessageTimeDESC implements Comparator<BroadcastChat> {

        @Override
        public int compare(BroadcastChat o1, BroadcastChat o2) {
            Long date1 = TimeUtil.getMillisecondByDate(o1.getDateTime(), TimeUtil.FORMAT_DATETIME_24_mic);
            Long date2 = TimeUtil.getMillisecondByDate(o2.getDateTime(), TimeUtil.FORMAT_DATETIME_24_mic);
            return (int) (date2 - date1);
        }
    }

}
