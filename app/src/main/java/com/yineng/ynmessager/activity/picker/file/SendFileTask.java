//***************************************************************
//*    2015-8-20  下午2:52:19
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright? 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.picker.file;

import android.os.AsyncTask;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.activity.picker.SendingListener;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.bean.p2psession.MessageFileEntity;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.UploadResponseHandler;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

/**
 * @author 贺毅柳
 *
 */
public class SendFileTask extends AsyncTask<Set<File>, Integer, Void>
{
	private AppController mApplication;
	private String mReceiverUser;
	private XmppConnectionManager mXmppManager;
	private SendingListener mSendFileListener;
	private int mChatType;
	/**
	 * 重新发送的ID
	 */
	private String reSendPacketId = null;

	public SendFileTask(XmppConnectionManager xmppManager, int chatType, String receiver)
	{
		if (mApplication==null){
			mApplication = AppController.getInstance();
		}
		if (mXmppManager==null){
		  	mXmppManager = xmppManager;
		}
		mChatType = chatType;
		mReceiverUser = receiver;
	}

	@Override
	protected Void doInBackground(Set<File>... params)
	{
		Set<File> files = params[0];

		int length = files.size();
		MessageFileEntity[] fileEntities = new MessageFileEntity[length];
		MessageBodyEntity[] bodies = new MessageBodyEntity[length];
		Message[] msgs = new Message[length];

		Iterator<File> iterator = files.iterator();
		for(int i = 0;iterator.hasNext();++i)
		{
			File file = iterator.next();

			bodies[i] = new MessageBodyEntity();
			// 文件集合
			fileEntities[i] = new MessageFileEntity();
			//保存文件本地路径
			fileEntities[i].setSdcardPath(file.getPath());
			fileEntities[i].setSize(String.valueOf(file.length()));
			fileEntities[i].setName(file.getName());

			bodies[i].getFiles().add(fileEntities[i]);
			bodies[i].setContent("<file key=\"\">");
			bodies[i].setSendName(mApplication.mSelfUser.getUserName());
			bodies[i].setMsgType(mChatType);

			msgs[i] = new Message();
			if (reSendPacketId != null && !reSendPacketId.isEmpty()) {
				msgs[i].setPacketID(reSendPacketId);
			}
			msgs[i].setBody(JSON.toJSONString(bodies[i]));
			msgs[i].setFrom(JIDUtil.getJIDByAccount(mApplication.mSelfUser.getUserNo()));
			msgs[i].setType(mChatType == Const.CHAT_TYPE_P2P ? Type.chat : Type.groupchat);
			msgs[i].setTo(mChatType == Const.CHAT_TYPE_P2P ? JIDUtil.getSendToMsgAccount(mReceiverUser) : JIDUtil.getGroupJIDByAccount(mReceiverUser));
			msgs[i].setSubject(bodies[i].getContent());

			if(mSendFileListener != null) {
                mSendFileListener.onBeforeEachSend(BaseChatMsgEntity.FILE, msgs[i], mChatType);
            }
		}

		iterator = files.iterator();
		for(int i = 0;iterator.hasNext();++i)
		{
			File file = iterator.next();

			String filePath = file.getPath();
			String uploadJson = "";
			uploadJson = FileUtil.formatSendFileJsonByFilePath(mReceiverUser,mChatType == Const.CHAT_TYPE_P2P ? "1" : "2",filePath,"0");
			String[] uploadResult = FileUtil.uploadFile(uploadJson,filePath,new UploadResponseHandler());
			L.e("uploadResult == "+uploadResult);
			if(uploadResult == null) {
				mSendFileListener.onFailedSend(BaseChatMsgEntity.FILE,msgs[i],mChatType);
				continue;
			} else if(uploadResult[0] == null) {
				mSendFileListener.onFailedSend(BaseChatMsgEntity.FILE,msgs[i],mChatType);
				continue;
			} else if (uploadResult[0].equals(UploadResponseHandler.mFailedSend)) {
				L.e("uploadResult == "+UploadResponseHandler.mFailedSend);
				mSendFileListener.onFailedSend(BaseChatMsgEntity.FILE,msgs[i],mChatType);
				continue;
			}

			fileEntities[i].setKey(uploadResult[0]);
			fileEntities[i].setFileId(uploadResult[0]);
			fileEntities[i].setFileType(uploadResult[2]);

			bodies[i].getFiles().clear();
			bodies[i].getFiles().add(fileEntities[i]);
			bodies[i].setContent("<file key=\"" + uploadResult[0] + "\">");
			bodies[i].setResource(MessageBodyEntity.SOURCE_PHONE);

			msgs[i].setBody(null);
			msgs[i].setBody(JSON.toJSONString(bodies[i]));

			if(mSendFileListener != null) {
                mSendFileListener.onEachDone(BaseChatMsgEntity.FILE, msgs[i], mChatType);
            }
			boolean isScuess = mXmppManager.sendPacket(msgs[i]);
			//当前发送包因为各种原因失败了返回失败结果
			if (!isScuess){
				mSendFileListener.onFailedSend(BaseChatMsgEntity.FILE,msgs[i],mChatType);
			}
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void result)
	{
		super.onPostExecute(result);
		if(mSendFileListener != null) {
            mSendFileListener.onAllDone();
        }
		mSendFileListener = null;
		System.gc();
	}

	public void setSendFileListener(SendingListener sendFileListener)
	{
		mSendFileListener = sendFileListener;
	}

	/**
	 * 设置要重新发送的包ID
	 * @param reSendPacketId
	 */
	public void setResendChatBeanPacketId(String reSendPacketId) {
		this.reSendPacketId = reSendPacketId;
	}

}