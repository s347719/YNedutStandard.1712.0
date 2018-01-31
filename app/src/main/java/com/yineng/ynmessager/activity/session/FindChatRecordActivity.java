package com.yineng.ynmessager.activity.session;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.groupsession.GroupChatActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.CommonEvent;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.groupsession.GroupChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.bean.p2psession.MessageFileEntity;
import com.yineng.ynmessager.bean.p2psession.MessageVoiceEntity;
import com.yineng.ynmessager.bean.p2psession.P2PChatMsgEntity;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.P2PChatMsgDao;
import com.yineng.ynmessager.db.dao.DisGroupChatDao;
import com.yineng.ynmessager.db.dao.GroupChatDao;
import com.yineng.ynmessager.imageloader.FileDownLoader;
import com.yineng.ynmessager.imageloader.FileDownLoader.onFileLoaderListener;
import com.yineng.ynmessager.receiver.CommonReceiver;
import com.yineng.ynmessager.receiver.CommonReceiver.IQuitGroupListener;
import com.yineng.ynmessager.receiver.CommonReceiver.updateGroupDataListener;
import com.yineng.ynmessager.service.DownloadService;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.SystemUtil;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.audio.AudioPlayer;
import com.yineng.ynmessager.view.SearchChatRecordEditText;
import com.yineng.ynmessager.view.SearchChatRecordEditText.onCancelSearchAnimationListener;
import com.yineng.ynmessager.view.SearchChatRecordEditText.onResultListItemCLickListener;
import com.yineng.ynmessager.view.face.FaceConversionUtil;
import com.yineng.ynmessager.view.face.gif.AnimatedImageSpan;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindChatRecordActivity extends BaseActivity {
    private static final int PAGE_SIZE = 20;
    /**
     * 显示搜索框动画
     */
    protected final int SHOW_SEARCH_VIEW = 0;
    /**
     * 取消搜索框动画
     */
    protected final int CANCEL_SEARCH_VIEW = 1;

    private final int ANIMATION_DURATION = 120;

    private final int INIT_REFRESH = 0;
    private final int PULL_DOWN_REFRESH = 1;
    private final int PULL_UP_REFRESH = 2;

    private SearchChatRecordEditText mSearchChatRecordEditText;
    private Context mContext;
    private RelativeLayout mRel_seachBox;
    private LinearLayout mChatRecordLayout;
    private int mHighLightPosition = -1;  //搜索聊天记录结果高光显示的列表项

    private Handler mHandler = new Handler() {
        @Override
        @SuppressLint("NewApi")
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SHOW_SEARCH_VIEW:
                    mSearchChatRecordEditText.show();
                    mChatRecordLayout.setY(-mChatEditTextDefaultViewY);
                    break;
                case CANCEL_SEARCH_VIEW:
                    mChatRecordLayout.setY(0);
                    break;

                default:
                    break;
            }
        }
    };
    private String mChatId;
    private int mChatType;
    private PullToRefreshListView mPullLoadMoreLV;
    private ListView mChatMsgLV;
    private int mPullDownPageIndex = 0;
    private int mPullUPPageIndex = 0;
    private P2PChatMsgDao mP2PChatMsgDao;
    private GroupChatDao mGroupChatDao;
    private DisGroupChatDao mDisGroupChatDao;
    private ArrayList<GroupChatMsgEntity> mGroupMessageList = new ArrayList<GroupChatMsgEntity>();
    private ChatMsgAdapter mChatMsgAdapter;
    private boolean mNoMoreMsg = false;
    LinkedList<GroupChatMsgEntity> mMsgList = new LinkedList<GroupChatMsgEntity>();
    private CommonReceiver mCommonReceiver;
    protected boolean isFinishAcitivity = false;
    private AudioPlayer mAudioPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = FindChatRecordActivity.this;
        setContentView(R.layout.activity_session_find_chat_record_layout);
        initData();
        findViews();
        // 初始状态，从本地获得20条数据，刷新UI
        refreshUIByPageIndex(INIT_REFRESH, mPullDownPageIndex);
        mChatMsgLV.setSelection(mChatMsgAdapter.getCount());
    }

    private void findViews() {
        mPullLoadMoreLV = (PullToRefreshListView) findViewById(R.id.ptrl_chat_pull_refresh_list);
        mChatMsgLV = mPullLoadMoreLV.getRefreshableView();
        mChatMsgLV.setAdapter(mChatMsgAdapter);
        findSearchChatRecordView();
    }

    private void initData() {
        Intent mGetIntent = getIntent();
        mChatId = mGetIntent.getStringExtra(GroupChatActivity.CHAT_ID_KEY);
        mChatType = mGetIntent.getIntExtra(GroupChatActivity.CHAT_TYPE_KEY,
                Const.CHAT_TYPE_P2P);
        switch (mChatType) {
            case Const.CHAT_TYPE_P2P:
                mP2PChatMsgDao = new P2PChatMsgDao(mContext);
                mGroupMessageList = mP2PChatMsgDao.getChatMsgEntitiesToFindRecord(mChatId);
                break;
            case Const.CHAT_TYPE_GROUP:
                mGroupChatDao = new GroupChatDao(mContext);
                mGroupMessageList = mGroupChatDao.getChatMsgEntities(mChatId);
                break;
            case Const.CHAT_TYPE_DIS:
                mDisGroupChatDao = new DisGroupChatDao(mContext);
                mGroupMessageList = mDisGroupChatDao.getChatMsgEntities(mChatId);
                break;
            default:
                break;
        }
        mChatMsgAdapter = new ChatMsgAdapter(mContext);
        mAudioPlayer = AudioPlayer.newInstance();
    }

    private void findSearchChatRecordView() {
        mSearchChatRecordEditText = new SearchChatRecordEditText(mContext,
                mChatId, mChatType);
        mRel_seachBox = (RelativeLayout) findViewById(R.id.searchBox);
        mChatRecordLayout = (LinearLayout) findViewById(R.id.ll_find_chat_record_frame);
        initSearchContactViewListener();
    }

    private void initSearchContactViewListener() {
        mRel_seachBox.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View paramView) {
                showDismissSearchView(true);
            }
        });

        mSearchChatRecordEditText
                .setOnCancelSearchAnimationListener(new onCancelSearchAnimationListener() {

                    @Override
                    public void cancelSearchContactAnimation() {
                        showDismissSearchView(false);
                    }
                });

        mSearchChatRecordEditText.setOnResultListItemCLickListener(new onResultListItemCLickListener() {

            @Override
            public void scrollToClickItem(Object clickObject) {
                showDismissSearchView(false);
                int selectIndex = 0;
                mMsgList.clear();
                GroupChatMsgEntity msgEntity = (GroupChatMsgEntity) clickObject;

                //得到点击的item在数据源列表的索引
                int mItemIndex = mGroupMessageList.indexOf(msgEntity);

                //根据item的索引计算出该item应该出现在第几页
                mPullDownPageIndex = mItemIndex / PAGE_SIZE;

                //加载该页数据
                refreshUIByPageIndex(INIT_REFRESH, mPullDownPageIndex);

                //计算出该listview应该选中的位置
                selectIndex = mChatMsgAdapter.getmMsgList().indexOf(msgEntity);
                mHighLightPosition = selectIndex;

//				L.d("mItemIndex == "+mItemIndex+" mPullDownPageIndex == "+mPullDownPageIndex+" selectindex == "+mSelectIndex);
//				L.d("select pos == "+mChatMsgLV.getSelectedItemPosition());
                if (selectIndex > -1) {
                    mChatMsgLV.setSelection(selectIndex);
                }
//				L.e("selected pos == "+mChatMsgLV.getSelectedItemPosition());
                //如果是第一页，则不让他上拉加载更多；否则上下拉都可以
                if (mPullDownPageIndex == 0) {
                    mPullLoadMoreLV.setMode(com.handmark.pulltorefresh.library.PullToRefreshBase.Mode.PULL_DOWN_TO_REFRESH);
                } else {
                    mPullUPPageIndex = mPullDownPageIndex;
                    mPullLoadMoreLV.setMode(com.handmark.pulltorefresh.library.PullToRefreshBase.Mode.BOTH);
                }
            }
        });

        mPullLoadMoreLV.setOnRefreshListener(new OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //如果页数X每页个数小于消息记录总数，说明还有记录
                        if ((mPullDownPageIndex + 1) * PAGE_SIZE < mGroupMessageList.size()) {
                            mPullDownPageIndex++;
                            int mBeforeMoreCount = mChatMsgAdapter.getCount();
                            refreshUIByPageIndex(PULL_DOWN_REFRESH, mPullDownPageIndex);
                            mPullLoadMoreLV.onRefreshComplete();
                            int mAfterMoreCount = mChatMsgAdapter.getCount();
                            int selectIndex = mAfterMoreCount - mBeforeMoreCount;
                            L.e("selectIndex == " + selectIndex);
                            mChatMsgLV.setSelection(selectIndex);
                        } else {
                            mPullLoadMoreLV.onRefreshComplete();
                        }
                    }
                }, 1000);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mPullUPPageIndex > 0) {
                            mPullUPPageIndex--;
                            refreshUIByPageIndex(PULL_UP_REFRESH, mPullUPPageIndex);
                            mPullLoadMoreLV.onRefreshComplete();
                            if (mPullUPPageIndex == 0) {
                                mPullLoadMoreLV.setMode(com.handmark.pulltorefresh.library.PullToRefreshBase.Mode.PULL_DOWN_TO_REFRESH);
                            }
                        }
                    }
                }, 1000);
            }
        });
        addGroupUpdatedListener();
    }

    //下载文件时、更新ui
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BaseChatMsgEntity event) {
        if (event == null || mMsgList == null) {
            return;
        }
        for (GroupChatMsgEntity tempMsgObj : mMsgList) {
            if (event.getPacketId().equals(tempMsgObj.getPacketId())) {
                tempMsgObj.setReceiveProgress(event.getReceiveProgress());
                tempMsgObj.setIsSuccess(event.getIsSuccess());
                break;
            }
        }
        mChatMsgAdapter.notifyDataSetChanged();
    }

    /**
     * 添加讨论组信息更改监听器
     */
    private void addGroupUpdatedListener() {
        mCommonReceiver = new CommonReceiver();
        mCommonReceiver.setUpdateGroupDataListener(new updateGroupDataListener() {

            @Override
            public void updateGroupData(int mGroupType) {
            }
        });
        mCommonReceiver.setIQuitGroupListener(new IQuitGroupListener() {

            @Override
            public void IQuitMyGroup(int mGroupType) {
                if ((mGroupType == Const.CONTACT_GROUP_TYPE && mChatType == Const.CHAT_TYPE_GROUP)
                        || (mGroupType == Const.CONTACT_DISGROUP_TYPE && mChatType == Const.CHAT_TYPE_DIS)) {
                    isFinishAcitivity = true;
                    finish();
                }
            }
        });
        IntentFilter mIntentFilter = new IntentFilter(Const.BROADCAST_ACTION_UPDATE_GROUP);
        mIntentFilter.addAction(Const.BROADCAST_ACTION_I_QUIT_GROUP);
        registerReceiver(mCommonReceiver, mIntentFilter);
    }

    /**
     * 本地分页查询数据，刷新UI
     *
     * @param refreshType
     */
    public void refreshUIByPageIndex(int refreshType, int pageIndex) {
        if (mGroupMessageList.size() > 0) {
            LinkedList<GroupChatMsgEntity> tempChatMsgEntities = new LinkedList<GroupChatMsgEntity>();
            for (int i = pageIndex * PAGE_SIZE; i < mGroupMessageList.size(); i++) {
                tempChatMsgEntities.addFirst(mGroupMessageList.get(i));
                if (tempChatMsgEntities.size() % PAGE_SIZE == 0) {
                    break;
                }
            }
            if (refreshType == PULL_UP_REFRESH) {
                mMsgList.addAll(tempChatMsgEntities);
            } else {
                mMsgList.addAll(0, tempChatMsgEntities);
            }
            notifyAdapterDataSetChanged();
        } else {
            mNoMoreMsg = true;
        }
    }

    /**
     * 修改数据的isShowTime字段（第一条消息显示时间，5分钟内的消息不显示时间），然后刷新UI
     */
    private void notifyAdapterDataSetChanged() {
        List<GroupChatMsgEntity> list = new ArrayList<GroupChatMsgEntity>();
        long preShowTime = 0;
        for (int i = 0; i < mMsgList.size(); i++) {
            GroupChatMsgEntity entity = mMsgList.get(i);
            if (i == 0) {
                entity.setShowTime(true);
                preShowTime = Long.valueOf(entity.getTime().trim());
            } else {
                if (compareTime(preShowTime, Long.valueOf(entity.getTime()))) {
                    preShowTime = Long.valueOf(entity.getTime());
                    entity.setShowTime(true);
                } else {
                    entity.setShowTime(false);
                }
            }
            list.add(entity);
        }
        // list作为临时的数据缓存，避免数据变更后，没有及时通知适配器，出现
        // The content of the adapter has changed but ListView did not receive a
        // notification的错误
        mChatMsgAdapter.setmMsgList(list);
        mChatMsgAdapter.notifyDataSetChanged();
//		refreshUnreadNumUI();

    }

    private static final long TIME_INTERVAL = 60 * 5 * 1000;// 时间在5分钟内的消息不显示时间

    /**
     * Compare the time difference is greater than TIME_INTERVAL minutes
     *
     * @param preTime
     * @param nextTime
     * @return
     */
    public static boolean compareTime(long preTime, long nextTime) {
        return (nextTime - preTime) >= TIME_INTERVAL;
    }

    TranslateAnimation showAnimation = null;
    TranslateAnimation cancelAnimation = null;
    private float mChatEditTextDefaultViewY;
    private AnimationListener showAnimationListener = new AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (isShowSearchEditText) {
                mHandler.sendEmptyMessage(SHOW_SEARCH_VIEW);
            } else {
                mHandler.sendEmptyMessage(CANCEL_SEARCH_VIEW);
            }
        }
    };

    private boolean isShowSearchEditText = false;

    /**
     * 显示/关闭搜素框的动画
     *
     * @param isShow
     */
    public void showDismissSearchView(boolean isShow) {
        isShowSearchEditText = isShow;
        if (isShow) {
            LinearLayout.LayoutParams etParamTest = (LinearLayout.LayoutParams) mRel_seachBox
                    .getLayoutParams();
            mChatEditTextDefaultViewY = mRel_seachBox.getY()
                    - etParamTest.topMargin;
            showAnimation = new TranslateAnimation(0, 0, 0,
                    -mChatEditTextDefaultViewY);
            showAnimation.setDuration(ANIMATION_DURATION);
            showAnimation.setAnimationListener(showAnimationListener);
            mChatRecordLayout.startAnimation(showAnimation);
        } else {
            mSearchChatRecordEditText.dismiss();
            cancelAnimation = new TranslateAnimation(0, 0,
                    0, mChatEditTextDefaultViewY);
            cancelAnimation.setDuration(ANIMATION_DURATION);
            cancelAnimation.setAnimationListener(showAnimationListener);
            mChatRecordLayout.startAnimation(cancelAnimation);
        }
    }

    public void back(View view) {
        finish();
    }

    public class ChatMsgAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private Context context;
        private List<GroupChatMsgEntity> mMsgList = new ArrayList<GroupChatMsgEntity>();

        private float mMinVoiceItemWith;
        private float mMaxVoiceItemWith;

        public ChatMsgAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            this.context = context;
            mMinVoiceItemWith = context.getResources().getDimension(R.dimen.contactPersonInfo_avatarImg_size);
            mMaxVoiceItemWith = context.getResources().getDimension(R.dimen.slidingmenu_offset);
        }

        public List<GroupChatMsgEntity> getmMsgList() {
            return mMsgList;
        }

        public void setmMsgList(List<GroupChatMsgEntity> mMsgList) {
            this.mMsgList = mMsgList;
        }

        @Override
        public int getCount() {
            return mMsgList.size();
        }

        @Override
        public Object getItem(int position) {
            return mMsgList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            GroupChatMsgEntity entity = mMsgList.get(position);
            if (entity.getIsSend() == GroupChatMsgEntity.COM_MSG) {
                return GroupChatMsgEntity.COM_MSG;
            } else {
                return GroupChatMsgEntity.TO_MSG;
            }

        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GroupChatMsgEntity entity = mMsgList.get(position);
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                if (entity.getIsSend() == GroupChatMsgEntity.COM_MSG) {
                    convertView = mInflater.inflate(
                            R.layout.chatting_item_msg_text_left, null);
                    viewHolder.tvSenderName = (TextView) convertView.findViewById(R.id.tv_chat_sender_name);
                    if (mChatType != Const.CHAT_TYPE_P2P) {
                        viewHolder.tvSenderName.setVisibility(View.VISIBLE);
                    }

                    viewHolder.ivVoiceUnreadFlag = (ImageView) convertView.findViewById(R.id.iv_voice_unread_flag);
                    viewHolder.pbVoiceProgressBar = (ProgressBar) convertView.findViewById(R.id.pb_voice_download_bar);
                } else {
                    convertView = mInflater.inflate(
                            R.layout.chatting_item_msg_text_right, null);
                    viewHolder.pbUpLoadProgressBar = (ProgressBar) convertView.findViewById(R.id.pb_file_upload_bar);
                    viewHolder.linResource = (LinearLayout) convertView.findViewById(R.id.lin_source_type);
                }
                viewHolder.pbFileProgressBar = (ProgressBar) convertView.findViewById(R.id.pb_file_download_bar);
                viewHolder.tvFileNotice = (TextView) convertView.findViewById(R.id.tv_file_notice);
                viewHolder.rlFileContent = (RelativeLayout) convertView.findViewById(R.id.rl_filecontent);
                viewHolder.tvFileName = (TextView) convertView.findViewById(R.id.tv_file_name);
                viewHolder.tvFileSize = (TextView) convertView.findViewById(R.id.tv_file_size);
                viewHolder.tv_file_sendstate = (TextView) convertView.findViewById(R.id.tv_file_sendstate);

                //接收语音
                viewHolder.rlVoiceContent = (RelativeLayout) convertView.findViewById(R.id.rl_voiceContent_layout);
                viewHolder.flVoiceContentBg = (FrameLayout) convertView.findViewById(R.id.rl_voice_container_length);
                viewHolder.ivVoiceBg = (ImageView) convertView.findViewById(R.id.iv_voice_bg);
                viewHolder.tvVoiceTime = (TextView) convertView.findViewById(R.id.tv_voice_time_length);

                viewHolder.ivSenderIcon = (CircleImageView) convertView.findViewById(R.id.iv_userhead);
                viewHolder.tvSendTime = (TextView) convertView
                        .findViewById(R.id.tv_sendtime);
                viewHolder.tvContent = (TextView) convertView
                        .findViewById(R.id.tv_chatcontent);
                viewHolder.tvSendStatus = (TextView) convertView
                        .findViewById(R.id.tv_chat_tag);
                viewHolder.mLayout = (RelativeLayout) convertView
                        .findViewById(R.id.chat_item_layout);
                viewHolder.tvSendStatus.setVisibility(View.INVISIBLE);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.tvContent.setVisibility(View.GONE);
            viewHolder.rlFileContent.setVisibility(View.GONE);
            viewHolder.rlVoiceContent.setVisibility(View.GONE);
            if (entity.getIsSend() == GroupChatMsgEntity.COM_MSG) {
                viewHolder.tvSenderName.setText(entity.getSenderName());
            }
            viewHolder.tvSendTime.setVisibility(View.INVISIBLE);
            if (entity.isShowTime()) {
                viewHolder.tvSendTime.setVisibility(View.VISIBLE);
                Date sendTime = new Date(Long.valueOf(entity.getTime()));
                viewHolder.tvSendTime.setText(TimeUtil.getTimeRelationFromNow2(mContext, sendTime));
            }

            viewHolder.tvContent.setTag(entity);
            if (entity.getMessage() != null) {
                MessageBodyEntity body = JSON.parseObject(entity.getMessage(),
                        MessageBodyEntity.class);
//				L.i(mTag, "entity:   " + entity.getMessage());
                switch (entity.getMessageType()) {
                    case P2PChatMsgEntity.FILE:
                        viewHolder.rlFileContent.setVisibility(View.VISIBLE);
                        viewHolder.tvContent.setVisibility(View.GONE);
                        viewHolder.tv_file_sendstate.setVisibility(View.GONE);
                        if (entity.getIsSend() == P2PChatMsgEntity.COM_MSG) {
                            handlerFileReceived(viewHolder, entity, body);
                        } else {
                            handleFileSend(viewHolder, entity, body);
                        }
                        break;
                    case P2PChatMsgEntity.MESSAGE:
                    case P2PChatMsgEntity.IMAGE:
                        viewHolder.tv_file_sendstate.setVisibility(View.GONE);
                        if (viewHolder.rlFileContent != null) {
                            viewHolder.rlFileContent.setVisibility(View.GONE);
                        }
                        viewHolder.tvContent.setVisibility(View.VISIBLE);
                        SpannableString spannableString;
                        if (entity.getSpannableString() != null) {
                            spannableString = entity.getSpannableString();
                        } else {
                            // 对内容做处理
                            spannableString = FaceConversionUtil
                                    .getInstace().handlerContent(mChatId, this.context, viewHolder.tvContent,
                                            body, mHandler, entity.getPacketId(), mChatType);
                            entity.setSpannableString(spannableString);
                        }
                        viewHolder.tvContent.setText(spannableString);
                        break;
                    case BaseChatMsgEntity.AUDIO_FILE://语音
                        viewHolder.tv_file_sendstate.setVisibility(View.GONE);
                        viewHolder.rlVoiceContent.setVisibility(View.VISIBLE);
                        if (entity.getIsSend() == BaseChatMsgEntity.COM_MSG) {//接收
                            handleVoiceReceived(viewHolder, entity, body);
                        } else {//发送
                            handleVoiceSend(viewHolder, entity, body);
                        }
                        break;
                    //显示文件传输给对方，对方接收的状态
                    case BaseChatMsgEntity.FILE_IQ_STATE:
                        viewHolder.mLayout.setVisibility(View.GONE);
                        viewHolder.tv_file_sendstate.setVisibility(View.VISIBLE);
                        viewHolder.tv_file_sendstate.setText(body.getContent());
                        break;
                    default:
                        break;
                }
            }

            // 设置搜索聊天记录时的高光显示
            int highLightBackground;
            if (mHighLightPosition == position) {
                highLightBackground = getResources().getColor(R.color.lightyellow);
            } else {
                highLightBackground = getResources().getColor(R.color.transparent);
            }
            convertView.setBackgroundColor(highLightBackground);

            File userIcon = null;
            //设置头像
            if (mChatType == Const.CHAT_TYPE_P2P && entity.getIsSend() == BaseChatMsgEntity.TO_MSG) {//两人会话且是自己发送的内容
                userIcon = FileUtil.getAvatarByName(LastLoginUserSP.getLoginUserNo(mContext));
            } else {
                userIcon = FileUtil.getAvatarByName(entity.getChatUserNo());
            }
            ContactOrgDao mContactOrgDao = new ContactOrgDao(mContext);
            if (userIcon == null || !userIcon.exists()) {
                if (entity.getIsSend() == BaseChatMsgEntity.COM_MSG) {//接收到消息设置别人的头像
                    int avatar = -1;
                    User user = mContactOrgDao.queryUserInfoByUserNo(entity.getChatUserNo());
                    if (user != null && user.getGender() == 1) {//对方是男性
                        avatar = R.mipmap.session_p2p_men;
                    } else if (user != null && user.getGender() == 2) {//对方是女性
                        avatar = R.mipmap.session_p2p_woman;
                    } else {
                        avatar = R.mipmap.session_no_sex;
                    }
                    viewHolder.ivSenderIcon.setImageResource(avatar);
                } else {//自己发送消息设置自己的头像
                    int avatar = -1;
                    User user = mContactOrgDao.queryUserInfoByUserNo(LastLoginUserSP.getLoginUserNo(mContext));
                    if (user != null && user.getGender() == 1) {//男性
                        avatar = R.mipmap.session_p2p_men;
                    } else if (user != null && user.getGender() == 2) {//女性
                        avatar = R.mipmap.session_p2p_woman;
                    } else {
                        avatar = R.mipmap.session_no_sex;
                    }
                    viewHolder.ivSenderIcon.setImageResource(avatar);
                }
            } else {
                viewHolder.ivSenderIcon.setImageURI(Uri.fromFile(userIcon));
            }

            return convertView;
        }

        class ViewHolder {
            public ProgressBar pbUpLoadProgressBar;
            public ProgressBar pbFileProgressBar;
            public TextView tvFileNotice;
            public TextView tvFileSize;
            public CircleImageView ivSenderIcon;
            public TextView tvFileName;
            public RelativeLayout rlFileContent;
            public TextView tvSendTime;
            public TextView tvContent;
            public TextView tvSendStatus;
            public TextView tv_file_sendstate;
            public RelativeLayout mLayout;
            public TextView tvSenderName;

            public RelativeLayout rlVoiceContent;
            public LinearLayout linResource;
            public FrameLayout flVoiceContentBg;
            public ImageView ivVoiceBg;
            public TextView tvVoiceTime;
            public ImageView ivVoiceUnreadFlag;
            public ProgressBar pbVoiceProgressBar;
        }

        private void handleVoiceSend(ViewHolder viewHolder, BaseChatMsgEntity entity, MessageBodyEntity body) {
            if (body.getVoice() == null) {
                return;
            }

            //根据时长修改消息气泡长度
            int time = body.getVoice().getTime() / 1000;
            ViewGroup.LayoutParams lParams = viewHolder.flVoiceContentBg.getLayoutParams();
            lParams.width = (int) (mMinVoiceItemWith + mMaxVoiceItemWith / 60 * time);
            viewHolder.flVoiceContentBg.setLayoutParams(lParams);
            //

            viewHolder.tvVoiceTime.setText(body.getVoice().getTime() / 1000 + "\"");
            viewHolder.flVoiceContentBg.setTag(entity);
            viewHolder.flVoiceContentBg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final BaseChatMsgEntity entity = (BaseChatMsgEntity) v.getTag();
                    //点击播放音频文件，并把状态改为已读
                    updateDatabaseMsgVoiceStatus(entity, BaseChatMsgEntity.IS_READED);
                    notifyDataSetChanged();
                    //点击播放中的音频，应停止播放
                    MessageBodyEntity body = JSON.parseObject(entity.getMessage(), MessageBodyEntity.class);
                    MessageVoiceEntity downLoadVoiceFileinfo = body.getVoice();
                    String fileName = downLoadVoiceFileinfo.getId();
                    File localFile = FileUtil.getFileByName(fileName);
                    if (localFile.exists()) {
                        playAudio(localFile, v, BaseChatMsgEntity.TO_MSG);
                    }
                }
            });
        }

        private void handleVoiceReceived(ViewHolder viewHolder, BaseChatMsgEntity entity, MessageBodyEntity body) {
            if (body.getVoice() != null) {
                if (entity.getIsSuccess() < BaseChatMsgEntity.DOWNLOAD_NOT_YET) {
                    updateDatabaseMsgStatus(entity, BaseChatMsgEntity.DOWNLOAD_NOT_YET);
                } else {
                    //如果该文件仍是下载中状态，且下载服务的正在下载的文件集合为空(说明服务还未创建)，
                    // 说明该文件已经没有下载了，该把状态置为下载失败的状态 huyi 2016.1.15
                    if (entity.getIsSuccess() == BaseChatMsgEntity.DOWNLOAD_ING) {
                        if (DownloadService.mDownloadMsgBeans == null) {
                            updateDatabaseMsgStatus(entity, BaseChatMsgEntity.DOWNLOAD_FAILED);
                        }
                    }
                }
                viewHolder.pbVoiceProgressBar.setVisibility(View.GONE);
                viewHolder.tvVoiceTime.setVisibility(View.GONE);
                viewHolder.ivVoiceUnreadFlag.setVisibility(View.GONE);
                viewHolder.tvVoiceTime.setBackgroundColor(Color.TRANSPARENT);
                viewHolder.tvVoiceTime.setOnClickListener(null);
                viewHolder.flVoiceContentBg.setOnClickListener(null);

                //根据时长修改消息气泡长度
                int time = body.getVoice().getTime() / 1000;
                ViewGroup.LayoutParams lParams = viewHolder.flVoiceContentBg.getLayoutParams();
                lParams.width = (int) (mMinVoiceItemWith + mMaxVoiceItemWith / 60 * time);
                viewHolder.flVoiceContentBg.setLayoutParams(lParams);
                //

                switch (entity.getIsSuccess()) {
                    case BaseChatMsgEntity.DOWNLOAD_NOT_YET:
                        viewHolder.pbVoiceProgressBar.setVisibility(View.VISIBLE);

                        Intent downLoadIntent = new Intent(mContext, DownloadService.class);
                        downLoadIntent.putExtra("downloadFileType", Const.MESSENGERFILE);
                        downLoadIntent.putExtra("downloadFileBean", entity);
                        mContext.startService(downLoadIntent);
                        break;
                    case BaseChatMsgEntity.DOWNLOAD_SUCCESS:
//                    viewHolder.pbVoiceProgressBar.setVisibility(View.GONE);
                        if (entity.getIsReaded() == BaseChatMsgEntity.IS_NOT_READED) {
                            viewHolder.ivVoiceUnreadFlag.setVisibility(View.VISIBLE);
                        } else {
                            viewHolder.ivVoiceUnreadFlag.setVisibility(View.GONE);
                        }
                        viewHolder.tvVoiceTime.setVisibility(View.VISIBLE);
                        viewHolder.tvVoiceTime.setText(body.getVoice().getTime() / 1000 + "\"");
                        viewHolder.flVoiceContentBg.setTag(entity);
                        viewHolder.flVoiceContentBg.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final BaseChatMsgEntity entity = (BaseChatMsgEntity) v.getTag();
                                //点击播放音频文件，并把状态改为已读
                                updateDatabaseMsgVoiceStatus(entity, BaseChatMsgEntity.IS_READED);
                                notifyDataSetChanged();
                                //点击播放中的音频，应停止播放
                                MessageBodyEntity body = JSON.parseObject(entity.getMessage(), MessageBodyEntity.class);
                                MessageVoiceEntity downLoadVoiceFileinfo = body.getVoice();
                                String fileName = downLoadVoiceFileinfo.getId();
                                File localFile = FileUtil.getFileByName(fileName);
                                if (localFile.exists()) {
                                    playAudio(localFile, v, BaseChatMsgEntity.COM_MSG);
                                }

                            }
                        });
                        break;
                    case BaseChatMsgEntity.DOWNLOAD_FAILED:
//                    viewHolder.pbVoiceProgressBar.setVisibility(View.GONE);
                        viewHolder.tvVoiceTime.setBackgroundResource(R.mipmap.chat_resend_icon);
                        viewHolder.tvVoiceTime.setText("");
                        viewHolder.tvVoiceTime.setVisibility(View.VISIBLE);
                        viewHolder.tvVoiceTime.setTag(entity);
                        viewHolder.tvVoiceTime.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final BaseChatMsgEntity entity = (BaseChatMsgEntity) v.getTag();
                                Intent downLoadIntent = new Intent(mContext, DownloadService.class);
                                downLoadIntent.putExtra("downloadFileType", Const.MESSENGERFILE);
                                downLoadIntent.putExtra("downloadFileBean", entity);
                                mContext.startService(downLoadIntent);
                            }
                        });
                        break;
                    default:
                        break;
                }
            }
        }

        /**
         * 处理文件接收的方法
         *
         * @param viewHolder
         * @param entity
         * @param body
         */
        private void handlerFileReceived(ViewHolder viewHolder, GroupChatMsgEntity entity, MessageBodyEntity body) {
            if (body.getFiles().size() > 0) {
                MessageFileEntity mFileInfo = body.getFiles().get(0);
                viewHolder.tvFileName.setText(mFileInfo.getName());
                long mFileInfoSize = Long.parseLong(mFileInfo.getSize());
                String fileSizeStr = FileUtil.FormatFileSize(mFileInfoSize);
                viewHolder.tvFileSize.setText(fileSizeStr);
                viewHolder.pbFileProgressBar.setVisibility(View.GONE);

                if (entity.getIsSuccess() < BaseChatMsgEntity.DOWNLOAD_NOT_YET) {
                    updateDatabaseMsgStatus(entity, BaseChatMsgEntity.DOWNLOAD_NOT_YET);
                } else {
                    //如果该文件仍是下载中状态，且下载服务的正在下载的文件集合为空(说明服务还未创建)，
                    // 说明该文件已经没有下载了，该把状态置为下载失败的状态 huyi 2016.1.15
                    if (entity.getIsSuccess() == BaseChatMsgEntity.DOWNLOAD_ING) {
                        if (DownloadService.mDownloadMsgBeans == null) {
                            updateDatabaseMsgStatus(entity, BaseChatMsgEntity.DOWNLOAD_FAILED);
                        }
                    }
                }

                switch (entity.getIsSuccess()) {
                    case P2PChatMsgEntity.DOWNLOAD_NOT_YET:
                        viewHolder.tvFileNotice.setText("下载");
                        viewHolder.tvFileNotice.setTextColor(0xFF12b5b0);
                        viewHolder.tvFileSize.setText(fileSizeStr);
                        break;
                    case P2PChatMsgEntity.DOWNLOAD_SUCCESS:
                        viewHolder.tvFileNotice.setText("打开");
                        viewHolder.tvFileNotice.setTextColor(0xFF12b5b0);
                        viewHolder.tvFileSize.setText(fileSizeStr);
                        break;
                    case P2PChatMsgEntity.DOWNLOAD_FAILED:
                        viewHolder.tvFileNotice.setText("下载失败,请重试");
                        viewHolder.tvFileNotice.setTextColor(Color.RED);
                        viewHolder.tvFileSize.setText(fileSizeStr);
                        break;
                    case P2PChatMsgEntity.DOWNLOAD_ING:
                        viewHolder.pbFileProgressBar.setVisibility(View.VISIBLE);
                        if (mFileInfoSize != 0) {
                            viewHolder.pbFileProgressBar.setProgress(entity.getReceiveProgress());
                            String showProgress = FileUtil.FormatFileSize((mFileInfoSize / 100) * entity.getReceiveProgress()) + "/" + fileSizeStr;
                            viewHolder.tvFileSize.setText(showProgress);
                            viewHolder.tvFileNotice.setText("");
                            viewHolder.tvFileNotice.setTextColor(0xFF12b5b0);
                            L.e("BaseChatMsgEntity.DOWNLOAD_ING " + entity.getReceiveProgress());
                        }
                        break;
                    default:
                        break;
                }
                viewHolder.rlFileContent.setTag(entity);
                viewHolder.rlFileContent.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(final View paramView) {
                        final BaseChatMsgEntity entity = (BaseChatMsgEntity) paramView.getTag();
                        switch (entity.getIsSuccess()) {
                            case BaseChatMsgEntity.DOWNLOAD_FAILED:
                            case BaseChatMsgEntity.DOWNLOAD_NOT_YET:
                                Intent downLoadIntent = new Intent(mContext, DownloadService.class);
                                downLoadIntent.putExtra("downloadFileType", Const.MESSENGERFILE);
                                downLoadIntent.putExtra("downloadFileBean", entity);
                                mContext.startService(downLoadIntent);
                                break;
                            case BaseChatMsgEntity.DOWNLOAD_SUCCESS:
                                L.e("BaseChatMsgEntity.DOWNLOAD_SUCCESS ");
                                MessageBodyEntity body = JSON.parseObject(entity.getMessage(), MessageBodyEntity.class);
                                MessageFileEntity downLoadFileinfo = body.getFiles().get(0);
                                String fileName = downLoadFileinfo.getName();
                                File localFile = FileUtil.getFileByName(fileName);
                                if (!SystemUtil.execLocalFile(mContext, localFile)) {
                                    Toast.makeText(mContext, R.string.groupSharedFiles_noAppsForThisFile, Toast.LENGTH_LONG).show();
                                }
                                break;
                            default:
                                break;
                        }
                    }
                });
            }
        }

        private void handleFileSend(final ViewHolder viewHolder, GroupChatMsgEntity entity, MessageBodyEntity body) {
            if (body.getFiles().size() == 0) return;
            MessageFileEntity mFileInfo = body.getFiles().get(0);
            viewHolder.tvFileName.setText(mFileInfo.getName());
            long mFileInfoSize = Long.parseLong(mFileInfo.getSize());
            String fileSizeStr = FileUtil.FormatFileSize(mFileInfoSize);
            viewHolder.tvFileSize.setText(fileSizeStr);
            //判断打开地址是本地地址还是下载地址
            MessageFileEntity downLoadFileinfo = body.getFiles().get(0);
            String sdFilePath = downLoadFileinfo.getSdcardPath();
            String downFilePath = FileUtil.getFileByName(downLoadFileinfo.getName()).getPath();
            File localFile = null;
            if (!StringUtils.isEmpty(downFilePath)) {
                localFile = new File(downFilePath);
                if (!localFile.exists()) {
                    if (!StringUtils.isEmpty(sdFilePath)) {
                        localFile = new File(sdFilePath);
                    }
                }
            }
            if (localFile != null && localFile.exists() && entity.getIsSuccess() == BaseChatMsgEntity.DOWNLOAD_NOT_YET) {
                entity.setIsSuccess(BaseChatMsgEntity.DOWNLOAD_SUCCESS);
            }  else if (!localFile.exists()&&entity.getIsSuccess()!=BaseChatMsgEntity.DOWNLOAD_ING) {
                entity.setIsSuccess(BaseChatMsgEntity.DOWNLOAD_NOT_YET);
            }

            //判断文件终端
            if (!StringUtils.isEmpty(body.getResource()) && body.getResource().equals(MessageBodyEntity.SOURCE_PC)) {
                viewHolder.linResource.setVisibility(View.VISIBLE);
            } else {
                viewHolder.linResource.setVisibility(View.GONE);
            }

            switch (entity.getIsSuccess()) {
                case P2PChatMsgEntity.DOWNLOAD_NOT_YET:
                    viewHolder.tvFileNotice.setText("下载");
                    viewHolder.tvFileNotice.setTextColor(0xFF6299e9);
                    viewHolder.tvFileSize.setText(fileSizeStr);
                    break;
                case P2PChatMsgEntity.DOWNLOAD_SUCCESS:
                    viewHolder.pbFileProgressBar.setVisibility(View.GONE);
                    viewHolder.tvFileNotice.setTextColor(0xFF6299e9);
                    viewHolder.tvFileNotice.setText("打开");
                    break;
                case BaseChatMsgEntity.DOWNLOAD_FAILED:
                    viewHolder.pbFileProgressBar.setVisibility(View.GONE);
                    viewHolder.tvFileNotice.setText("下载失败,请重试");
                    viewHolder.tvFileNotice.setTextColor(Color.RED);
                    viewHolder.tvFileSize.setText(fileSizeStr);
                    break;
                case BaseChatMsgEntity.DOWNLOAD_ING:
                    viewHolder.pbFileProgressBar.setVisibility(View.VISIBLE);
                    if (mFileInfoSize != 0) {
                        int progress = (int) (entity.getReceivedBytes() * 100 / mFileInfoSize);
                        viewHolder.pbFileProgressBar.setProgress(progress);
                        String downloadedfileSizeStr = FileUtil.FormatFileSize(entity.getReceivedBytes());
                        viewHolder.tvFileSize.setText(downloadedfileSizeStr + "/" + fileSizeStr);
                        viewHolder.tvFileNotice.setText("");
                        viewHolder.tvFileNotice.setTextColor(0xFF6299e9);
                        L.e("BaseChatMsgEntity.DOWNLOAD_ING " + progress);
                    }
                    break;
                default:
                    break;
            }
            //点击打开文件
            viewHolder.rlFileContent.setTag(entity);
            viewHolder.rlFileContent.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final GroupChatMsgEntity entity = (GroupChatMsgEntity) v.getTag();
                    MessageBodyEntity body = JSON.parseObject(entity.getMessage(), MessageBodyEntity.class);
                    MessageFileEntity downLoadFileinfo = body.getFiles().get(0);
                    String sdFilePath = downLoadFileinfo.getSdcardPath();
                    String downFilePath = FileUtil.getFileByName(downLoadFileinfo.getName()).getPath();
                    //判断打开地址是本地地址还是下载地址
                    File localFile = null;
                    if (!StringUtils.isEmpty(downFilePath)) {
                        localFile = new File(downFilePath);
                        if (!localFile.exists()) {
                            if (!StringUtils.isEmpty(sdFilePath)) {
                                localFile = new File(sdFilePath);
                            }
                        }
                    }
                    //判断有没有这个文件，如果没有就判断是否下载
                    if (localFile.exists()) {
                        if (!SystemUtil.execLocalFile(mContext, localFile)) {
                            Toast.makeText(mContext, R.string.groupSharedFiles_noAppsForThisFile, Toast.LENGTH_LONG).show();
                        }
                    } else if (entity.getIsSuccess() == BaseChatMsgEntity.DOWNLOAD_NOT_YET || entity.getIsSuccess() == BaseChatMsgEntity.DOWNLOAD_FAILED) {
                        entity.setIsSuccess(P2PChatMsgEntity.DOWNLOAD_ING);
                        Intent downLoadIntent = new Intent(mContext, DownloadService.class);
                        downLoadIntent.putExtra("downloadFileType", Const.MESSENGERFILE);
                        downLoadIntent.putExtra("downloadFileBean", entity);
                        mContext.startService(downLoadIntent);
                    }
                }
            });
        }

        private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                //  Auto-generated method stub
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        notifyDataSetChanged();
                        break;

                    default:
                        break;
                }
            }
        };

        /**
         * 更改数据库中该条记录的状态
         *
         * @param entity
         */
        private void updateDatabaseMsgStatus(BaseChatMsgEntity entity, int downloadStatus) {
            entity.setIsSuccess(downloadStatus);
            switch (mChatType) {
                case Const.CHAT_TYPE_P2P:
                    mP2PChatMsgDao.updateMsgSendStatus(entity.getPacketId(), downloadStatus);
                    break;
                case Const.CHAT_TYPE_GROUP:
                    mGroupChatDao.updateMsgSendStatus(entity.getPacketId(), downloadStatus);
                    break;
                case Const.CHAT_TYPE_DIS:
                    mDisGroupChatDao.updateMsgSendStatus(entity.getPacketId(), downloadStatus);
                    break;
                default:
                    break;
            }
        }

        private void updateDatabaseMsgVoiceStatus(BaseChatMsgEntity entity, int isReaded) {
            entity.setIsReaded(isReaded);
            switch (mChatType) {
                case Const.CHAT_TYPE_P2P:
                    mP2PChatMsgDao.updateMsgVoiceStatus(entity.getPacketId(), isReaded);
                    break;
                case Const.CHAT_TYPE_GROUP:
                    mGroupChatDao.updateMsgVoiceStatus(entity.getPacketId(), isReaded);
                    break;
                case Const.CHAT_TYPE_DIS:
                    mDisGroupChatDao.updateMsgVoiceStatus(entity.getPacketId(), isReaded);
                    break;
                default:
                    break;
            }
        }
    }

    public void playAudio(@NonNull File voiceFile) {
        AudioPlayer.PlayingConfig config = new AudioPlayer.PlayingConfig(voiceFile);
        mAudioPlayer.start(config);
    }

    /**
     * 正在播放的音频
     */
    private File mFilePlaying;
    /**
     * 正在播放的音频的视图
     */
    private View playingVoiceAnimView;

    /**
     * 播放音频文件
     *
     * @param voiceFile 音频文件
     * @param v         对应的语音消息视图
     * @param sendType  消息来源：0发送 1接收
     */
    public void playAudio(@NonNull File voiceFile, View v, final int sendType) {
        if (playingVoiceAnimView != null) {
            if ((int) playingVoiceAnimView.getTag() == BaseChatMsgEntity.COM_MSG) {
                playingVoiceAnimView.setBackgroundResource(R.drawable.chat_voice_left_anim3);
            } else {
                playingVoiceAnimView.setBackgroundResource(R.drawable.chat_voice_right_anim3);
            }
            playingVoiceAnimView = null;
        }
        playingVoiceAnimView = v.findViewById(R.id.iv_voice_bg);
        playingVoiceAnimView.setTag(sendType);
        if (mAudioPlayer.isPlaying() && ObjectUtils.equals(mFilePlaying, voiceFile)) {
            if (sendType == BaseChatMsgEntity.COM_MSG) {
                playingVoiceAnimView.setBackgroundResource(R.drawable.chat_voice_left_anim3);
            } else {
                playingVoiceAnimView.setBackgroundResource(R.drawable.chat_voice_right_anim3);
            }
            mAudioPlayer.stop();
        } else {
            if (sendType == BaseChatMsgEntity.COM_MSG) {
                playingVoiceAnimView.setBackgroundResource(R.drawable.chat_play_voice_left_msg_animation);
            } else {
                playingVoiceAnimView.setBackgroundResource(R.drawable.chat_play_voice_right_msg_animation);
            }
            AnimationDrawable playVoiceAnimation = (AnimationDrawable) playingVoiceAnimView.getBackground();
            playVoiceAnimation.start();

            AudioPlayer.PlayingConfig config = new AudioPlayer.PlayingConfig(voiceFile);
            config.onCompletionListener = new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (sendType == BaseChatMsgEntity.COM_MSG) {
                        playingVoiceAnimView.setBackgroundResource(R.drawable.chat_voice_left_anim3);
                    } else {
                        playingVoiceAnimView.setBackgroundResource(R.drawable.chat_voice_right_anim3);
                    }
                }
            };
            mAudioPlayer.start(config);
        }

        mFilePlaying = voiceFile;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAudioPlayer.release();
        /**销毁gif引用**/
        destroyGifValue();
        unregisterReceiver(mCommonReceiver);
        System.gc();
    }

    /**
     * 回收bitmap暂用的内存空间
     */
    private void destroyGifValue() {
        List<GroupChatMsgEntity> mAdapterList = mChatMsgAdapter.getmMsgList();
        if (mAdapterList != null) {
            for (GroupChatMsgEntity groupChatMsgEntity : mAdapterList) {
                SpannableString tempSpan = groupChatMsgEntity.getSpannableString();
                if (tempSpan != null) {
                    AnimatedImageSpan[] tem = tempSpan.getSpans(0, tempSpan.length() - 1, AnimatedImageSpan.class);
                    for (AnimatedImageSpan animatedImageSpan : tem) {
                        animatedImageSpan.recycleBitmaps(true);
//						animatedImageSpan = null;
                    }
                    tempSpan.removeSpan(tem);
                }
            }
        }
    }
}
