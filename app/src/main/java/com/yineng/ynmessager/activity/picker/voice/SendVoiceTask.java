package com.yineng.ynmessager.activity.picker.voice;

import android.content.Context;
import android.os.AsyncTask;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.activity.picker.SendingListener;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.bean.p2psession.MessageVoiceEntity;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.UploadResponseHandler;
import com.yineng.ynmessager.util.audio.AudioPlayer;

import org.jivesoftware.smack.packet.Message;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by 贺毅柳 on 2016/5/5 17:03.
 */
public class SendVoiceTask extends AsyncTask<Set<File>, Integer, Void>
{
    private AppController mApplication;
    private String mReceiverUser;
    private XmppConnectionManager mXmppManager;
    private SendingListener mSendVoiceListener;
    private int mChatType;
    /**
     * 重新发送的ID
     */
    private String reSendPacketId = null;

    public SendVoiceTask(XmppConnectionManager xmppManager, int chatType, String receiver)
    {
        mApplication = AppController.getInstance();
        mXmppManager = xmppManager;
        mChatType = chatType;
        mReceiverUser = receiver;
    }

    @Override
    protected Void doInBackground(Set<File>... params)
    {
        Set<File> files = params[0];

        int length = files.size();
        MessageVoiceEntity[] voiceEntities = new MessageVoiceEntity[length];
        MessageBodyEntity[] bodies = new MessageBodyEntity[length];
        Message[] msgs = new Message[length];

        //根据ChatType值来判断msgType
        final int msgType;
        if(mChatType == Const.CHAT_TYPE_P2P)
        {
            msgType = Const.CHAT_VOICE_TYPE_P2P;
        }else if(mChatType == Const.CHAT_TYPE_GROUP)
        {
            msgType = Const.CHAT_VOICE_TYPE_GROUP;
        }else
        {
            msgType = Const.CHAT_VOICE_TYPE_DISGROUP;
        }

        Iterator<File> iterator = files.iterator();
        Context context = AppController.getInstance().getApplicationContext();
        for (int i = 0; iterator.hasNext(); ++i)
        {
            File file = iterator.next();

            bodies[i] = new MessageBodyEntity();
            // 文件集合
            voiceEntities[i] = new MessageVoiceEntity();
            voiceEntities[i].setId(file.getName());
            voiceEntities[i].setTime(AudioPlayer.getDuration(context, file));
            voiceEntities[i].setSentSuccess(false);

            bodies[i].setVoice(voiceEntities[i]);
            bodies[i].setContent("[语音]");
            bodies[i].setSendName(mApplication.mSelfUser.getUserName());
            bodies[i].setMsgType(msgType);

            msgs[i] = new Message();
            if (reSendPacketId != null && !reSendPacketId.isEmpty())
            {
                msgs[i].setPacketID(reSendPacketId);
            }
            msgs[i].setBody(JSON.toJSONString(bodies[i]));
            msgs[i].setFrom(JIDUtil.getJIDByAccount(mApplication.mSelfUser.getUserNo()));
            msgs[i].setType(mChatType == Const.CHAT_TYPE_P2P ? Message.Type.chat : Message.Type.groupchat);
            msgs[i].setTo(mChatType == Const.CHAT_TYPE_P2P ? JIDUtil.getSendToMsgAccount(mReceiverUser) : JIDUtil.getGroupJIDByAccount(mReceiverUser));
            msgs[i].setSubject(bodies[i].getContent());

            if (mSendVoiceListener != null) {
                mSendVoiceListener.onBeforeEachSend(BaseChatMsgEntity.AUDIO_FILE, msgs[i], mChatType);
            }
        }

        iterator = files.iterator();
        for (int i = 0; iterator.hasNext(); ++i)
        {
            File file = iterator.next();

            String filePath = file.getPath();
            String uploadJson = FileUtil.formatSendFileJsonByFilePath(mReceiverUser, mChatType == Const.CHAT_TYPE_P2P ? "1" : "2", filePath, "0");
            String[] uploadResult = FileUtil.uploadFile(uploadJson, voiceEntities[i].getId(), filePath, new UploadResponseHandler());
            if (uploadResult == null)
            {
                mSendVoiceListener.onFailedSend(BaseChatMsgEntity.AUDIO_FILE, msgs[i],mChatType);
                continue;
            } else if (uploadResult[0] == null && mSendVoiceListener != null)
            {
                mSendVoiceListener.onFailedSend(BaseChatMsgEntity.AUDIO_FILE, msgs[i],mChatType);
                continue;
            } else if (UploadResponseHandler.mFailedSend.equals(uploadResult[0]) && mSendVoiceListener !=null)
            {
                L.e("uploadResult == " + UploadResponseHandler.mFailedSend);
                mSendVoiceListener.onFailedSend(BaseChatMsgEntity.AUDIO_FILE, msgs[i],mChatType);
                continue;
            }
            bodies[i].setResource(MessageBodyEntity.SOURCE_PHONE);
            voiceEntities[i].setSentSuccess(true);
            msgs[i].setBody(null);
            msgs[i].setBody(JSON.toJSONString(bodies[i]));

            if (mSendVoiceListener != null) {
                mSendVoiceListener.onEachDone(BaseChatMsgEntity.AUDIO_FILE, msgs[i], mChatType);
            }

            mXmppManager.sendPacket(msgs[i]);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result)
    {
        super.onPostExecute(result);
        if (mSendVoiceListener != null) {
            mSendVoiceListener.onAllDone();
        }
        mSendVoiceListener = null;
        System.gc();
    }

    public void setSendFileListener(SendingListener sendFileListener)
    {
        mSendVoiceListener = sendFileListener;
    }

    /**
     * 设置要重新发送的包ID
     *
     * @param reSendPacketId
     */
    public void setResendChatBeanPacketId(String reSendPacketId)
    {
        this.reSendPacketId = reSendPacketId;
    }

}
