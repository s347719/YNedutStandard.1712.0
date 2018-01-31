package com.yineng.ynmessager.activity.picker.content;

import android.os.AsyncTask;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.activity.picker.SendingListener;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.L;

import org.jivesoftware.smack.packet.Message;

/**
 * Created by 舒欢
 * Created time: 2017/6/15
 */

public class SendMessageTask extends AsyncTask<String, Integer, Void> {
    private AppController mApplication;
    private String mReceiverUser;
    private XmppConnectionManager mXmppManager;
    private SendingListener mSendImageListener;
    /**
     * 重新发送的ID
     */
    private String reSendPacketId = null;
    private int mChatType;

    public SendMessageTask(XmppConnectionManager xmppManager, int chatType, String receiver) {

        mApplication = AppController.getInstance();
        mXmppManager = xmppManager;
        mChatType = chatType;
        mReceiverUser = receiver;
    }

    @Override
    protected Void doInBackground(String... params) {
        String content = params[0];
        String myUserNo = mApplication.mSelfUser.getUserNo();
        Message message = new Message();
        MessageBodyEntity bodyEntity  = new MessageBodyEntity();
        //将message中的信息修改成当前信息
        bodyEntity.setContent(content);
        bodyEntity.setMsgType(mChatType);
        bodyEntity.setSendName(mApplication.mSelfUser.getUserName());
        message.setBody(JSON.toJSONString(bodyEntity));
        //设置发送消息的发送人
        message.setFrom(JIDUtil.getJIDByAccount(myUserNo));
        message.setType(mChatType == Const.CHAT_TYPE_P2P ? Message.Type.chat : Message.Type.groupchat);
        //设置发送消息的接收对象
        message.setTo(mChatType == Const.CHAT_TYPE_P2P ? JIDUtil.getJIDByAccount(mReceiverUser) : JIDUtil
                .getGroupJIDByAccount(mReceiverUser));
        message.setSubject(bodyEntity.getContent());
        // 因为这里和SendImageTask 不同的地方在于必须要上传图片文件，综合mSendImgeListener 接口中的方法只执行onBeforeEachSend方法保存当前的信息
        if(mSendImageListener != null) {
            mSendImageListener.onBeforeEachSend(BaseChatMsgEntity.MESSAGE,message,mChatType);
        }
        boolean isSuccess = mXmppManager.sendPacket(message);
        //当前发送包因为各种原因失败了返回失败结果
        if (!isSuccess){
            mSendImageListener.onFailedSend(BaseChatMsgEntity.MESSAGE,message,mChatType);
        }
        return null;
    }
    @Override
    protected void onPostExecute(Void result)
    {
        super.onPostExecute(result);
        if(mSendImageListener != null) {
            mSendImageListener.onAllDone();
        }
        mSendImageListener = null;
        System.gc();
    }

    public void setSendMessageListener(SendingListener sendImageListener)
    {
        mSendImageListener = sendImageListener;
    }
}
