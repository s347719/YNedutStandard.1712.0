package com.yineng.ynmessager.activity.session;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.BroadcastChat;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.offline.MessageRevicerEntity;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.dao.BroadcastChatDao;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.NetWorkUtil;
import com.yineng.ynmessager.util.TimeUtil;

import org.jivesoftware.smack.packet.Message;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Yutang
 */
public class BroadcastChatContentActivity extends BaseActivity implements OnClickListener {
    private ArrayList<BroadcastChat> mBroadcastList;
    private int mCurrentIndex;
    private TextView mTxt_previous;

    private TextView mTxt_sender;
    private TextView mTxt_timestamp;
    private TextView mTitleTV;
    private TextView mTxt_content;
    private ImageButton mImgb_pagePrevious;
    private ImageButton mImgb_pageNext;
    private BroadcastChatDao mBroadcastChatDao;
    private ScrollView mScr_scrollContent;
    private ArrayList<String> packetIdSet = new ArrayList<>();
    /**
     * XMPP连接管理类实例
     */
    private XmppConnectionManager mXmppConnManager;
    private User myUserInfo;
    private ContactOrgDao mContactOrgDao;
    private String myUserAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_broadcast_content_layout);
        init();
        findViews();
        updateView();
    }

    private void init() {
        mBroadcastChatDao = new BroadcastChatDao(this);
        Intent intent = getIntent();
        if (intent != null) {
            mBroadcastList = (ArrayList<BroadcastChat>) intent
                    .getSerializableExtra(BroadcastChatActivity.EXTRA_KEY_LIST);
            mContactOrgDao = new ContactOrgDao(getApplicationContext());
            mXmppConnManager = XmppConnectionManager.getInstance();
            myUserAccount = LastLoginUserSP.getInstance(this).getUserAccount();
            myUserInfo = mContactOrgDao.queryUserInfoByUserNo(myUserAccount);

            if (mBroadcastList == null || mBroadcastList.size() == 0) {
                finish();
                return;
            }
            mCurrentIndex = intent.getIntExtra(BroadcastChatActivity.EXTRA_KEY_INDEX, 0) - 1;
            //发送消息回执
            sendReveived(mCurrentIndex);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.broadcastChatContent_txt_previous:
                backTo();
                break;
            case R.id.broadcastChat_imgb_pagePrevious:
                if (mCurrentIndex > 0) {
                    mCurrentIndex--;
                    updateView();
                    sendReveived(mCurrentIndex);
                }
                break;
            case R.id.broadcastChat_imgb_pageNext:
                if (mCurrentIndex < mBroadcastList.size() - 1) {
                    mCurrentIndex++;
                    updateView();
                    sendReveived(mCurrentIndex);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        backTo();
    }

    private void findViews() {
        mTxt_previous = (TextView) findViewById(R.id.broadcastChatContent_txt_previous);
        mTitleTV = (TextView) findViewById(R.id.broadcastChatContent_txt_title);
        mTxt_content = (TextView) findViewById(R.id.broadcastChatContent_txt_content);
        mImgb_pagePrevious = (ImageButton) findViewById(R.id.broadcastChat_imgb_pagePrevious);
        mImgb_pageNext = (ImageButton) findViewById(R.id.broadcastChat_imgb_pageNext);
        mScr_scrollContent = (ScrollView) findViewById(R.id.broadcastChat_scr_scrollContent);
        mTxt_sender = (TextView) findViewById(R.id.broadcastChatContent_txt_sender);
        mTxt_timestamp = (TextView) findViewById(R.id.broadcastChatContent_txt_timestamp);

        mTxt_previous.setOnClickListener(this);
        mImgb_pagePrevious.setOnClickListener(this);
        mImgb_pageNext.setOnClickListener(this);
    }

    public void backTo() {
        Intent resultIntent = new Intent();
        Bundle mBundle = new Bundle();
        mBundle.putStringArrayList("readList", packetIdSet);
        resultIntent.putExtras(mBundle);
        setResult(RESULT_OK, resultIntent);
        this.finish();
    }

    /**
     * 发送回执消息
     */
    private void sendReveived(int mCurrentIndex) {
        if (mBroadcastList.size() > 0) {
            BroadcastChat bc = mBroadcastList.get(mCurrentIndex);
            if (bc == null || bc.getIsRead() == 1) return;

            Message msg = new Message();
            //封装回执消息
            MessageRevicerEntity revicerEntity = new MessageRevicerEntity(bc.getPacketId());
            revicerEntity.setType(Const.CHAT_TYPE_BROADCAST_RECEIVER);
            msg.setFrom(JIDUtil.getJIDByAccount(myUserInfo != null ? myUserInfo.getUserNo() : myUserAccount));
            msg.setTo("admin@" + mXmppConnManager.getServiceName());
            msg.setPacketID(msg.getPacketID());
            msg.setType(Message.Type.headline);
            msg.addExtension(revicerEntity);
            //网络可用的时候才能让xmpp发送msg，否则将发送的消息改为发送失败的状态
            if (NetWorkUtil.isNetworkAvailable(this)) {
                mXmppConnManager.sendPacket(msg);
            }
            Log.i(mTag, "发送消息回执:" + msg.toXML());
        }
    }

    private void updateView() {
        BroadcastChat bc = mBroadcastList.get(mCurrentIndex);
        Set<String> unreadSet = LastLoginUserSP.getInstance(this).getUnreadBroadcastIds();
        Iterator<String> unreadsIter = unreadSet.iterator();
        while (unreadsIter.hasNext()){
            String unreadId = unreadsIter.next();
            if (unreadId.equals(bc.getPacketId())) {
                unreadsIter.remove();
            }
        }
        LastLoginUserSP.getInstance(this).saveUnreadBroadcastIds(unreadSet);
        //如果已读就把更新未读列表
        if (bc != null) {
            mTxt_sender.setText(getString(R.string.broadcastChatContent_sender, bc.getUserName()));
            mTxt_timestamp.setText(getString(R.string.broadcastChatContent_sendTime, TimeUtil.getDateByDateStr(bc.getDateTime(), "yyyy-MM-dd HH:mm:ss")));
            mTitleTV.setText(bc.getTitle());

            String content = bc.getMessage();
            mTxt_content.setText(content);
            if (bc.getIsRead() == 0) {
                mBroadcastChatDao.updateIsReadById(bc.getPacketId(), 1);// 标记已读
                packetIdSet.add(bc.getPacketId());
            }
        }
        if (mCurrentIndex <= 0) {
            mImgb_pagePrevious.setEnabled(false);
            mImgb_pagePrevious.setAlpha(0.4f);
        } else {
            mImgb_pagePrevious.setEnabled(true);
            mImgb_pagePrevious.setAlpha(1.0f);
        }

        if (mCurrentIndex >= mBroadcastList.size() - 1) {
            mImgb_pageNext.setEnabled(false);
            mImgb_pageNext.setAlpha(0.4f);
        } else {
            mImgb_pageNext.setEnabled(true);
            mImgb_pageNext.setAlpha(1.0f);
        }

        mScr_scrollContent.smoothScrollTo(0, 0);
    }

}
