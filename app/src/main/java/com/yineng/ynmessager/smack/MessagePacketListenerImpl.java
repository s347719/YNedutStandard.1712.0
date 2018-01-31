package com.yineng.ynmessager.smack;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.activity.event.NoticeEventHelper;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.BroadcastChat;
import com.yineng.ynmessager.bean.RecentChat;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.event.NoticeEvent;
import com.yineng.ynmessager.bean.groupsession.GroupChatMsgEntity;
import com.yineng.ynmessager.bean.offline.MessageRevicerEntity;
import com.yineng.ynmessager.bean.offline.UnRead;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.bean.p2psession.P2PChatMsgEntity;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.NoticeEventTb;
import com.yineng.ynmessager.db.P2PChatMsgDao;
import com.yineng.ynmessager.db.dao.BroadcastChatDao;
import com.yineng.ynmessager.db.dao.DisGroupChatDao;
import com.yineng.ynmessager.db.dao.GroupChatDao;
import com.yineng.ynmessager.db.dao.RecentChatDao;
import com.yineng.ynmessager.manager.NoticesManager;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.IconBadgerHelper;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.SystemUtil;
import com.yineng.ynmessager.util.TextUtil;
import com.yineng.ynmessager.util.TimeUtil;

import org.apache.commons.lang3.StringUtils;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Message消息总的接收处理类
 *
 * @author YINENG
 */
public class MessagePacketListenerImpl implements PacketListener {
    private static final String TAG = "MessagePacketListenerImpl";
    //	private final KeyguardManager mKeyguardManager;
    private Context mContext;
    private LocalBroadcastManager mBroadcastManager;
    private XmppConnectionManager mXmppConnManager;
    //	private NoticesManager mNoticesManager;
    private P2PChatMsgDao mP2pChartMsgDao;
    private GroupChatDao mGroupChatDao;
    private RecentChatDao mRecentChatDao;
    private BroadcastChatDao mBroadcastChatDao;
    private ReceiveMessageCallBack mCallback;
    private ContactOrgDao mContactOrgDao;
    private DisGroupChatDao mDisGroupChatDao;

    public MessagePacketListenerImpl(Context context) {
        this.mContext = context;
        this.mBroadcastManager = LocalBroadcastManager.getInstance(context);
        mXmppConnManager = XmppConnectionManager.getInstance();
//		mNoticesManager = NoticesManager.getInstance(context);
        mP2pChartMsgDao = new P2PChatMsgDao(mContext);
        mGroupChatDao = new GroupChatDao(mContext);
        mDisGroupChatDao = new DisGroupChatDao(mContext);
        mRecentChatDao = new RecentChatDao(mContext);
        mBroadcastChatDao = new BroadcastChatDao(mContext);
        mContactOrgDao = new ContactOrgDao(mContext);
        //test
//		mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    }

    /**
     * 将文本中的HTML转义字符替换成（可能有bug）
     *
     * @param htmlbody
     * @return
     */
    public static String formatHtmlBodyToJson(String htmlbody) {
        if (htmlbody == null) {
            return "";
        }
        String s = "";
        s = htmlbody.replace("&amp;", "&");
        s = s.replace("&lt;", "<");
        s = s.replace("&gt;", ">");
        s = s.replace("&nbsp;", " ");
        s = s.replace("&#39;", "\'");
        s = s.replace("&quot;", "\"");
        return s;
    }

    /**
     * 根据消息生成广播
     *
     * @throws JSONException
     */
    public static BroadcastChat createBroadcastChatByMessage(Message message, String msgContent, String senderNo,
                                                             String senderName) {
        BroadcastChat broadcast = new BroadcastChat();
        broadcast.setPacketId(message.getPacketID());
        broadcast.setTitle(message.getSubject() != null ? message.getSubject() : "");
        broadcast.setMessage(msgContent);
        broadcast.setDateTime(message.getSendTime());
        broadcast.setUserNo(senderNo);
        broadcast.setUserName(senderName);
        broadcast.setIsSend(0);
        return broadcast;
    }

    /**
     * 根据body生成最近会话内容
     *
     * @param body 解压缩后的json字符串
     * @return
     */
    public static MessageBodyEntity getContentByBody(String body) {
        String content = null;
        MessageBodyEntity bodyEntity = JSON.parseObject(body, MessageBodyEntity.class);
        L.v(TAG, "MessagePacketListenerImpl :getContentByBody->body" + body);
        try {
            if (bodyEntity != null) {
                switch (bodyEntity.getMsgType()) {
                    case Const.CHAT_TYPE_P2P:
                        if (bodyEntity.getImages() != null && bodyEntity.getImages().size() > 0) {
                            content = "[图片...]";
                        } else {
                            content = bodyEntity.getContent();
                        }
                        break;
                    case Const.CHAT_TYPE_DIS:
                    case Const.CHAT_TYPE_GROUP:
                    case Const.CHAT_TYPE_BROADCAST:
                    case Const.CHAT_TYPE_NOTICE:
                        if (bodyEntity.getImages() != null && bodyEntity.getImages().size() > 0) {
                            if (bodyEntity.getSendName() != null) {
                                content = bodyEntity.getSendName() + "  " + "[图片...]";
                            } else {
                                content = "[图片...]";
                            }

                        } else {
                            if (bodyEntity.getSendName() != null) {
                                content = bodyEntity.getContent();
                            } else {
                                content = bodyEntity.getContent();
                            }
                        }
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        L.v(TAG, "MessagePacketListenerImpl :getContentByBody->content:" + content);
        return bodyEntity;
        // return content == null ? " " : content;
    }

    @Override
    public void processPacket(Packet arg0) {
        Message message = (Message) arg0;
        L.v(TAG, "MessagePacketListenerImpl :toxml   " + message.toXML());
        RecentChat recentChat = new RecentChat();
        String mFromAccount = JIDUtil.getAccountByJID(message.getFrom().trim());
        String mToAccount = JIDUtil.getAccountByJID(message.getTo().trim());
        Message.Type type = message.getType();
        String subject = message.getSubject();
        String jsonBody = null;
        boolean isAlarm = true;
        MessageBodyEntity tempBodyEntity = null;
        if (mFromAccount != null) {
            recentChat.setSenderNo(mFromAccount);
            recentChat.setUserNo(mFromAccount);
            // 修改讨论组名称
            if (message.getBody() == null && subject != null) {
                updateGroupSubject(mFromAccount, message);
                return;
            }
            jsonBody = formatHtmlBodyToJson(message.getBody());
            L.v(TAG, jsonBody);
            //如果是已读消息回执
            if (message.getExtension(Const.MESSAGE_READED_RECEIVER) != null) {
                MessageRevicerEntity revicerEntity = (MessageRevicerEntity) message.getExtension(Const.MESSAGE_READED_RECEIVER);
                //3为广播通知
                if (revicerEntity.getType() == 3) {
                    BroadcastChat broadcastChat = mBroadcastChatDao.queryBroadcastById(revicerEntity.getId());
                    broadcastChat.setIsRead(1);
                    mBroadcastChatDao.saveOrUpdate(broadcastChat);
                    RecentChat unReadChat = mRecentChatDao.isChatExist(Const.BROADCAST_ID, Const.CHAT_TYPE_BROADCAST);
                    unReadChat.setUnReadCount(mBroadcastChatDao.queryUnReadNum());
                    mRecentChatDao.updateRecentChat(unReadChat);
                    //未读消息列表
                    Set<String> unreadIdSet = LastLoginUserSP.getInstance(mContext).getUnreadBroadcastIds();
                    Iterator<String> idsIterator = unreadIdSet.iterator();
                    while (idsIterator.hasNext()) {
                        String id = idsIterator.next();
                        if (id.equals(broadcastChat.getPacketId())) {
                            idsIterator.remove();
                        }
                    }
                    LastLoginUserSP.getInstance(mContext).saveUnreadBroadcastIds(unreadIdSet);
                    NoticesManager.getInstance(mContext).sendMessageTypeNotice(Const.BROADCAST_ID, unReadChat.getChatType(), false);// 发送消息提醒
                    mXmppConnManager.getReceiveBroadcastChatCallBack().onReceiveBroadcastChat(broadcastChat);// 回调
                } else {
                    //判断消息类型
                    int msgType = 0;
                    if (revicerEntity.getId().startsWith("dis")) {
                        msgType = Const.CHAT_TYPE_DIS;
                    } else if (revicerEntity.getId().startsWith("group")) {
                        msgType = Const.CHAT_TYPE_GROUP;
                    } else {
                        msgType = Const.CHAT_TYPE_P2P;
                    }
                    RecentChat unReadChat = mRecentChatDao.isChatExist(revicerEntity.getId(), msgType);
                    unReadChat.setUnReadCount(0);
                    mRecentChatDao.updateRecentChat(unReadChat);
                    NoticesManager.getInstance(mContext).sendMessageTypeNotice(revicerEntity.getId(), unReadChat.getChatType(), false);// 发送消息提醒
                }
                return;
            }
            //如果不是广播
            tempBodyEntity = getContentByBody(jsonBody);
            if (message.getExtension(Const.BROADCAST_ID) == null) {
                if (tempBodyEntity != null) {
                    recentChat.setContent(tempBodyEntity.getContent());
                    recentChat.setSenderName(tempBodyEntity.getSendName());
                }
            }
            //如果有未读消息数就表示这是本地发送的未读消息列表
            int unReaderNum = 1;
            String[] mUnreadMsgIds = new String[]{};
            Set<String> mUnreadMsgSet = new HashSet<>();
            UnRead unRead = null;
            //查询当前未读消息条数
            PacketExtension unReadExtension = message.getExtension("unread", Const.BROADCAST_ACTION_UNREADER_MSG_NUM);
            if (unReadExtension != null) {
                unRead = (UnRead) unReadExtension;
                unReaderNum = unRead.getNum();
                mUnreadMsgIds = unRead.getUnreadMsgIds().split(",");
                for (int i = 0; i < mUnreadMsgIds.length; i++) {
                    if (!StringUtils.isEmpty(mUnreadMsgIds[i])) {
                        mUnreadMsgSet.add(mUnreadMsgIds[i]);
                    }
                }
            }
            recentChat.setDateTime(message.getSendTime());
            // 广播或者个人回话
            // broadcast
            if (type == Message.Type.chat) {
                if (message.getExtension(Const.BROADCAST_ID) != null) {
                    L.v(TAG, "MessagePacketListenerImpl broadcast: " + "sendtime:" + message.getSendTime() + " subject:"
                            + message.getSubject());

                    NoticeEvent event;
                    recentChat.setUserNo(Const.BROADCAST_ID);
                    recentChat.setChatType(Const.CHAT_TYPE_BROADCAST);// 设置广播类型
                    recentChat.setTitle(Const.BROADCAST_NAME);
                    //广播的标题作为最近会话的内容
                    if (!TextUtils.isEmpty(subject))
                        recentChat.setContent(subject);
                    // 过滤广播Message Body中的多余json
                    String content = message.getBody();
                    try {
                        JSONObject bodyJson = new JSONObject(content);
                        content = bodyJson.optString("content");
                        if (!TextUtils.isEmpty(content)) {//说明是广播 处理平台通知type为2的情况 huyi 2016.1.13
                            content = TextUtil.trimEnd(content).toString(); // 去掉末尾的一个多余的换行符
                            if (bodyJson.optString("messageType") != null) //V8发送的广播通知该字段为不为空
                            {
                                //如果messageType为2时，去掉content里面的<a>标签，只保留文本内容
                                content = Jsoup.parse(content).text();
                            }
                        } else {
                            JSONArray msgIdList = bodyJson.optJSONArray("msgIdList");
                            if (msgIdList != null && msgIdList.length() > 0) {
                                NoticeEventTb noticeEventTb = new NoticeEventTb(mContext);
                                for (int i = 0; i < msgIdList.length(); i++) {
                                    noticeEventTb.updateNoticeReadStatus(msgIdList.optString(i));
                                }
                                //通知更新计数
                                mBroadcastManager.sendBroadcast(new Intent(NoticeEvent.ACTION_REFRESH));
                                mBroadcastManager.sendBroadcast(new Intent(NoticeEvent.ACTION_REFRESH_UNREAD));
                                ShortcutBadger.applyCount(mContext, IconBadgerHelper.count(mContext));
                            }
                            return;
                        }
                    } catch (JSONException e) {
                        L.e(TAG, e.getMessage(), e);
                    }//end

                    User mUser = mContactOrgDao.queryUserInfoByUserNo(mFromAccount);
                    String mUserName;
                    if (mUser != null) {
                        mUserName = mUser.getUserName();
                    } else {
                        if(tempBodyEntity!=null) {
                            mUserName = tempBodyEntity.getSendName();
                        }else{
                            mUserName = mFromAccount;
                        }
                    }
                    BroadcastChat broadcast = createBroadcastChatByMessage(message, content, mFromAccount, mUserName);
                    //广播未读消息数组
                    if (mUnreadMsgIds.length > 0 && unRead != null) {
                        LastLoginUserSP.getInstance(mContext).saveUnreadBroadcastIds(mUnreadMsgSet);
                    } else {
                        //新接收的消息把它加到未读列表中
                        Set<String> unreadIdSet = LastLoginUserSP.getInstance(mContext).getUnreadBroadcastIds();
                        if(unreadIdSet == null){
                            unreadIdSet = new HashSet<>();
                        }
                        unreadIdSet.add(message.getPacketID());
                        LastLoginUserSP.getInstance(mContext).saveUnreadBroadcastIds(unreadIdSet);
                    }
                    // 事件广播且该广播不是平台通知类型的消息
                    if ((event = NoticeEventHelper.from(message)) != null &&
                            StringUtils.isEmpty(event.getMessageType())) {
                        recentChat.setUnReadCount(unReaderNum);
                        // 发送消息提醒之前保存最近会话
                        if (unRead != null) {
                            // 广播显示界面没有打开，提醒
                            broadcast.setIsRead(1);// 标记已读
                            for (int i = 0; i < mUnreadMsgIds.length; i++) {
                                //匹配未读id，如果存在表示未读
                                if (message.getPacketID().equals(mUnreadMsgIds[i])) {
                                    broadcast.setIsRead(0);
                                }
                            }
                            mRecentChatDao.saveRecentChatSetUnreadNum(recentChat);
                        } else {
                            broadcast.setIsRead(0);
                            mRecentChatDao.saveRecentChat(recentChat);
                        }
                        mBroadcastChatDao.saveOrUpdate(broadcast);
                        NoticesManager.getInstance(mContext).sendMessageTypeNotice(Const.BROADCAST_ID, recentChat.getChatType());// 发送消息提醒
                    }
                    if (mXmppConnManager.getReceiveBroadcastChatCallBack() != null) {
                        mXmppConnManager.getReceiveBroadcastChatCallBack().onReceiveBroadcastChat(broadcast);// 回调
                    }
                    // p2pchat
                } else {
                    //如果是pc发送的文件P2P点单传输就过滤掉
                    if (tempBodyEntity.getMsgType() == Const.CHAT_TYPE_P2P_FILE) {
                        return;
                    }
                    if (tempBodyEntity.getMsgType() == Const.CHAT_VOICE_TYPE_P2P) {
                        recentChat.setContent("[语音]");
                    }
                    recentChat.setChatType(Const.CHAT_TYPE_P2P);// 设置会话类型
                    String showAccount = mFromAccount;
                    //判断如果是历史消息是当前账号发送的FromAccount=Account,就把标题换成toAccount
                    if (mFromAccount.equals(LastLoginUserSP.getInstance(mContext).getUserAccount())) {
                        showAccount = mToAccount;
                        recentChat.setUserNo(showAccount);
                    }
                    //正常接收消息to是自己
                    User chatUserInfo = mContactOrgDao.queryUserInfoByUserNo(mToAccount);
                    if (mToAccount.equals(LastLoginUserSP.getInstance(mContext).getUserAccount())) {
                        chatUserInfo = mContactOrgDao.queryUserInfoByUserNo(mFromAccount);
                    }
                    recentChat.setTitle(chatUserInfo.getUserName());
                    P2PChatMsgEntity msg = (P2PChatMsgEntity) generateMsgObj(showAccount, Const.CHAT_TYPE_P2P, tempBodyEntity, jsonBody, message, null);
                    if (msg == null) {
                        return;
                    }
                    msg.setTime(String.valueOf(TimeUtil.getMillisecondByDate(message.getSendTime(), TimeUtil.FORMAT_DATETIME_24_mic)));
                    if (mFromAccount.equals(LastLoginUserSP.getInstance(mContext).getUserAccount())) {
                        //如果你是消息from和当前账号一样，表示这是自己发送的消息
                        isAlarm = false;
                        msg.setIsSend(0);
                        unReaderNum = 0;
                    }
                    // 如果个人会话窗口打开，mCallback!= null
                    if ((mCallback = mXmppConnManager.getReceiveMessageCallBack(showAccount)) != null && SystemUtil.isUiRunningFront()) {// 根据发送方帐号转发消息
                        mCallback.receivedMessage(msg);

                        recentChat.setUnReadCount(0);
                        mRecentChatDao.saveRecentChat(recentChat);// 发送消息提醒之前保存最近会话
                        NoticesManager.getInstance(mContext).updateRecentChatList(showAccount, recentChat.getChatType());// 更新最近会话列表
                    }// 未打开会话窗，保存到个人记录，发送消息提醒
                    else {
                        recentChat.setUnReadCount(unReaderNum);
                        // 发送消息提醒之前保存最近会话
                        if (unRead != null) {
                            mRecentChatDao.saveRecentChatSetUnreadNum(recentChat);
//                            handleDraft(recentChat.getUserNo(),Const.CHAT_TYPE_P2P);
                        } else {
                            mRecentChatDao.saveRecentChat(recentChat);
                        }
                        NoticesManager.getInstance(mContext).sendMessageTypeNotice(showAccount, recentChat.getChatType(), isAlarm);// 发送消息提醒
                    }
                    mP2pChartMsgDao.saveMsg(msg);// 保存消息到个人会话记录
                }
            }// 群或讨论组消息
            else if (type == Message.Type.groupchat) {
                String mFromGroupMemberAccount = JIDUtil.getResouceNameByJID(message.getFrom().trim());
                L.e("mFromGroupMemberAccount == " + mFromGroupMemberAccount);
                L.e("LastSPAccount == " + LastLoginUserSP.getInstance(mContext).getUserAccount());
                // 从数据库表中获取当前接收消息的群\讨论组详细信息
                ContactGroup contactGroup = mContactOrgDao.queryGroupOrDiscussByGroupName(mFromAccount);

                if (tempBodyEntity.getMsgType() == Const.CHAT_VOICE_TYPE_DISGROUP ||
                        tempBodyEntity.getMsgType() == Const.CHAT_VOICE_TYPE_GROUP) {
                    recentChat.setContent("[语音]");
                }
                // 讨论组消息
                if (mFromAccount.startsWith("dis")) {
                    recentChat.setChatType(Const.CHAT_TYPE_DIS);// 设置会话类型
                    String title = mRecentChatDao.getUserNameByUserId(mFromAccount, Const.CHAT_TYPE_DIS);
                    //未查到当前讨论组
                    if (title.equals("error")) {
                        return;
                    }
                    recentChat.setTitle(title);
                    // 发送者ID
                    recentChat.setSenderNo(mFromGroupMemberAccount);
                    GroupChatMsgEntity msg = (GroupChatMsgEntity) generateMsgObj(mFromAccount, Const.CHAT_TYPE_DIS, tempBodyEntity, jsonBody, message, mFromGroupMemberAccount);
                    if (msg == null) {
                        return;
                    }
                    if (mFromGroupMemberAccount.equals(LastLoginUserSP.getInstance(mContext).getUserAccount())) {
                        msg.setIsSend(0);
                        unReaderNum = 0;
                        isAlarm = false;
                    }
                    // 如果该讨论组会话窗口打开，mCallback!= null
                    if ((mCallback = mXmppConnManager.getReceiveMessageCallBack(mFromAccount)) != null) {// 根据讨论组帐号转发消息
                        // 回调接口分发消息到会话界面
                        mCallback.receivedMessage(msg);
                        recentChat.setUnReadCount(0);
                        mRecentChatDao.saveRecentChat(recentChat);// 发送消息提醒之前保存最近会话
                        NoticesManager.getInstance(mContext).updateRecentChatList(mFromAccount, recentChat.getChatType());// 更新最近会话列表
                    }// 未打开该讨论组会话窗，保存到讨论组记录，发送消息提醒
                    else {
                        recentChat.setUnReadCount(unReaderNum);
                        // 发送消息提醒之前保存最近会话
                        if (unRead != null) {
                            mRecentChatDao.saveRecentChatSetUnreadNum(recentChat);
                        } else {
                            mRecentChatDao.saveRecentChat(recentChat);
                        }
                        if (contactGroup != null) {
                            NoticesManager.getInstance(mContext).sendMessageTypeNotice(mFromAccount, recentChat.getChatType(),
                                    contactGroup.getNotifyMode() != 0 && isAlarm);// 发送消息提醒
                        } else {
                            // 发送消息提醒
                            NoticesManager.getInstance(mContext).sendMessageTypeNotice(mFromAccount, recentChat.getChatType(), isAlarm);
                        }

                    }
                    mDisGroupChatDao.saveGroupChatMsg(msg);
                }
                // 群组消息
                else if (mFromAccount.startsWith("group")) {
                    recentChat.setChatType(Const.CHAT_TYPE_GROUP);// 设置会话类型
                    String title = mRecentChatDao.getUserNameByUserId(mFromAccount, Const.CHAT_TYPE_GROUP);
                    //未查到当前群组
                    if (title.equals("error")) {
                        return;
                    }
                    recentChat.setTitle(title);
                    // 发送者ID
                    recentChat.setSenderNo(mFromGroupMemberAccount);
                    GroupChatMsgEntity msg = (GroupChatMsgEntity) generateMsgObj(mFromAccount, Const.CHAT_TYPE_GROUP, tempBodyEntity, jsonBody, message, mFromGroupMemberAccount);
                    if (msg == null) {
                        return;
                    }
                    if (mFromGroupMemberAccount.equals(LastLoginUserSP.getInstance(mContext).getUserAccount())) {
                        msg.setIsSend(0);
                        unReaderNum = 0;
                        isAlarm = false;
                    }
                    // 如果该群组会话窗口打开，mCallback!= null
                    if ((mCallback = mXmppConnManager.getReceiveMessageCallBack(mFromAccount)) != null) {// 根据群组帐号转发消息
                        mCallback.receivedMessage(msg);
                        recentChat.setUnReadCount(0);
                        mRecentChatDao.saveRecentChat(recentChat);// 发送消息提醒之前保存最近会话
                        NoticesManager.getInstance(mContext).updateRecentChatList(mFromAccount, recentChat.getChatType());// 更新最近会话列表
                    }// 未打开该群组会话窗，保存到讨论组记录，发送消息提醒
                    else {
                        recentChat.setUnReadCount(unReaderNum);
                        // 发送消息提醒之前保存最近会话
                        if (unRead != null) {
                            mRecentChatDao.saveRecentChatSetUnreadNum(recentChat);
                        } else {
                            mRecentChatDao.saveRecentChat(recentChat);
                        }
                        // 第一次安装登陆时contactGroup会未null，如果刚登陆时有消息来则默认声音提醒
                        if (contactGroup != null) {
                            NoticesManager.getInstance(mContext).sendMessageTypeNotice(mFromAccount, recentChat.getChatType(),
                                    contactGroup.getNotifyMode() != 0 && isAlarm);// 发送消息提醒
                        } else {
                            NoticesManager.getInstance(mContext).sendMessageTypeNotice(mFromAccount, recentChat.getChatType(), isAlarm);// 发送消息提醒
                        }
                    }
                    mGroupChatDao.saveGroupChatMsg(msg);
                }
            }
        }
    }

    /**
     * 生成ChatMsgEntity对象
     *
     * @param mFromAccount            单人会话：对方的userno  多人会话：讨论、群组id
     * @param chatType                1 两人会话; 2 群会话; 3 讨论组会话
     * @param tempBodyEntity          body的对象数据
     * @param jsonBody                message的body数据
     * @param message                 xmpp的message对象
     * @param mFromGroupMemberAccount 多人会话中该条消息的发送者
     * @return
     */
    public static Object generateMsgObj(String mFromAccount, int chatType, MessageBodyEntity tempBodyEntity, String jsonBody, Message message, String mFromGroupMemberAccount) {
        Object msg = null;
        switch (chatType) {
            case Const.CHAT_TYPE_P2P:
                msg = new P2PChatMsgEntity();
                ((BaseChatMsgEntity) msg).setChatUserNo(mFromAccount);
                break;
            case Const.CHAT_TYPE_GROUP:
            case Const.CHAT_TYPE_DIS:
                msg = new GroupChatMsgEntity();
                ((GroupChatMsgEntity) msg).setGroupId(mFromAccount);
                ((BaseChatMsgEntity) msg).setChatUserNo(mFromGroupMemberAccount);
                if (tempBodyEntity != null) {
                    // 发送者名称
                    ((GroupChatMsgEntity) msg).setSenderName(tempBodyEntity.getSendName());
                }
                break;
            default:
                break;
        }
        ((BaseChatMsgEntity) msg).setChatType(chatType);
        ((BaseChatMsgEntity) msg).setIsReaded(BaseChatMsgEntity.IS_NOT_READED);
        ((BaseChatMsgEntity) msg).setIsSend(BaseChatMsgEntity.COM_MSG);
        if (tempBodyEntity.getFiles() != null && tempBodyEntity.getFiles().size() > 0) {//文件消息
            ((BaseChatMsgEntity) msg).setMessageType(BaseChatMsgEntity.FILE);
            ((BaseChatMsgEntity) msg).setIsSuccess(BaseChatMsgEntity.DOWNLOAD_NOT_YET);
        } else if (tempBodyEntity.getMsgType() == Const.CHAT_VOICE_TYPE_P2P ||
                tempBodyEntity.getMsgType() == Const.CHAT_VOICE_TYPE_DISGROUP ||
                tempBodyEntity.getMsgType() == Const.CHAT_VOICE_TYPE_GROUP) {
            ((BaseChatMsgEntity) msg).setMessageType(BaseChatMsgEntity.AUDIO_FILE);//语音消息
            ((BaseChatMsgEntity) msg).setIsSuccess(BaseChatMsgEntity.DOWNLOAD_NOT_YET);
        } else if (tempBodyEntity.getMsgType() == BaseChatMsgEntity.FILE_IQ_STATE) {
            ((BaseChatMsgEntity) msg).setMessageType(BaseChatMsgEntity.FILE_IQ_STATE);
            ((BaseChatMsgEntity) msg).setIsSuccess(BaseChatMsgEntity.SEND_SUCCESS);
        } else {
            ((BaseChatMsgEntity) msg).setMessageType(BaseChatMsgEntity.MESSAGE);//文本、表情、图片消息
            ((BaseChatMsgEntity) msg).setIsSuccess(BaseChatMsgEntity.SEND_SUCCESS);
        }
        ((BaseChatMsgEntity) msg).setMessage(jsonBody);
        ((BaseChatMsgEntity) msg).setPacketId(message.getPacketID());
        if (message.getSendTime() != null) {
            long time = TimeUtil.getMillisecondByDate(message.getSendTime(), TimeUtil.FORMAT_DATETIME_24);
            ((BaseChatMsgEntity) msg).setTime(String.valueOf(time));
        } else {
            ((BaseChatMsgEntity) msg).setTime(String.valueOf(System.currentTimeMillis()));
        }
        return msg;
    }

    /**
     * 收到群、讨论组名称被修改的msg
     *
     * @param mGroupId
     * @param message
     */
    private void updateGroupSubject(String mGroupId, Message message) {
        RecentChat mRecentChat;
        int mGroupType;
        // 更新联系人中讨论组列表的名字
        if (mGroupId.startsWith("dis")) {
            mGroupType = Const.CONTACT_DISGROUP_TYPE;
            mRecentChat = mRecentChatDao.isChatExist(mGroupId, Const.CHAT_TYPE_DIS);
        } else {
            mGroupType = Const.CONTACT_GROUP_TYPE;
            mRecentChat = mRecentChatDao.isChatExist(mGroupId, Const.CHAT_TYPE_GROUP);
        }
        // 更改群组、讨论组名称
        mContactOrgDao.updateGroupSubject(mGroupId, message.getSubject(), mGroupType);

        Intent updateViewIntent = new Intent(Const.BROADCAST_ACTION_UPDATE_GROUP);
        updateViewIntent.putExtra(Const.INTENT_GROUPTYPE_EXTRA_NAME, mGroupType);
        mContext.sendBroadcast(updateViewIntent);

        // 更新会话列表中讨论组名称
        if (mRecentChat != null) {
            mRecentChat.setTitle(message.getSubject());
            mRecentChatDao.updateRecentChat(mRecentChat);// 发送消息提醒之前保存最近会话
            NoticesManager.getInstance(mContext).updateRecentChatList(mGroupId, mRecentChat.getChatType());// 更新最近会话列表
        }
    }
}
