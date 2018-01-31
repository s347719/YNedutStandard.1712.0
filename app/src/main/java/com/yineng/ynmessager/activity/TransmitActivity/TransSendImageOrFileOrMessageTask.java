package com.yineng.ynmessager.activity.TransmitActivity;

import android.os.AsyncTask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yineng.ynmessager.activity.picker.SendingListener;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.imgpicker.ImageFile;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.bean.p2psession.MessageImageEntity;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.L;

import org.jivesoftware.smack.packet.Message;

import java.io.File;
import java.util.List;

/**
 * 转发图片过程中服务端已经存在才使用到的异步线程
 * Created by 舒欢
 * Created time: 2017/4/24
 */
public class TransSendImageOrFileOrMessageTask extends AsyncTask<List<ImageFile>, Integer, Void> {

    private String mTag = this.getClass().getSimpleName();
    private AppController mApplication;
    private String mReceiverUser;
    private XmppConnectionManager mXmppManager;
    private SendingListener mSendImageListener;
    private int mChatType;
    private String msg;
    public TransSendImageOrFileOrMessageTask(XmppConnectionManager xmppManager, int chatType, String receiver, String mess)
    {
        mApplication = AppController.getInstance();
        mXmppManager = xmppManager;
        mChatType = chatType;
        mReceiverUser = receiver;
        msg = mess;
    }


    @Override
    protected Void doInBackground(List<ImageFile>... params)
    {

        String myUserNo = mApplication.mSelfUser.getUserNo();
        Message message = new Message();
        MessageBodyEntity bodyEntity  = JSONObject.parseObject(msg,MessageBodyEntity.class);
        //因为在ynedut中转发图片会根据sdCardPath来作为判断是否从本地读取图片的依据
        // 然而图片地址是Android自己上传的，所以我们在这里将所有转发的图片的地址置空从而让图片能在被转发后读取出来
        if (bodyEntity.getImages().size()>0){
            for (int i = 0; i < bodyEntity.getImages().size(); i++) {
                bodyEntity.getImages().get(i).setSdcardPath(null);
            }
        }
        //将message中的信息修改成当前信息
        bodyEntity.setMsgType(mChatType);
        bodyEntity.setSendName(mApplication.mSelfUser.getUserName());
        //转发设置成为手机端
        bodyEntity.setResource(MessageBodyEntity.SOURCE_PHONE);
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
            if (bodyEntity.getImages().size() > 0){
                //存储在数据库中是图片类型
                mSendImageListener.onBeforeEachSend(BaseChatMsgEntity.IMAGE,message,mChatType);
            }
            else if (bodyEntity.getFiles().size() > 0){
                //存储在数据库中是文件类型
                mSendImageListener.onBeforeEachSend(BaseChatMsgEntity.FILE,message,mChatType);
            }else {
                mSendImageListener.onBeforeEachSend(BaseChatMsgEntity.MESSAGE,message,mChatType);
            }
        }
        boolean isSuccess = mXmppManager.sendPacket(message);
        L.i(mTag,"转发信息："+message.toXML()+"发送状态"+isSuccess);
        //当前发送包因为各种原因失败了返回失败结果
        if (!isSuccess){
            if (bodyEntity.getImages().size() > 0){
                mSendImageListener.onFailedSend(BaseChatMsgEntity.IMAGE,message,mChatType);
            }else if (bodyEntity.getFiles().size() > 0){
                mSendImageListener.onFailedSend(BaseChatMsgEntity.FILE,message,mChatType);
            }else {
                mSendImageListener.onBeforeEachSend(BaseChatMsgEntity.MESSAGE,message,mChatType);
            }
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

    public void setSendImageListener(SendingListener sendImageListener)
    {
        mSendImageListener = sendImageListener;
    }
}
