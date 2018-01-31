//***************************************************************
//*    2015-8-14  下午4:40:05
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.picker.image;

import android.os.AsyncTask;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.activity.picker.SendingListener;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.imgpicker.ImageFile;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.bean.p2psession.MessageImageEntity;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.UploadResponseHandler;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;

import java.util.List;

/**
 * @author 贺毅柳
 *
 */
public class SendImageTask extends AsyncTask<List<ImageFile>, Integer, Void>
{
	private String mTag = this.getClass().getSimpleName();
	private AppController mApplication;
	private String mReceiverUser;
	private XmppConnectionManager mXmppManager;
	private SendingListener mSendImageListener;
	/**
	 * 重新发送的ID
	 */
	private String reSendPacketId = null;
	private int mChatType;

	public SendImageTask(XmppConnectionManager xmppManager, int chatType, String receiver)
	{
		mApplication = AppController.getInstance();
		mXmppManager = xmppManager;
		mChatType = chatType;
		mReceiverUser = receiver;
	}


	@Override
	protected Void doInBackground(List<ImageFile>... params)
	{
		String myUserNo = mApplication.mSelfUser.getUserNo();

		List<ImageFile> imageFiles = params[0];
		int length = imageFiles.size();
		MessageImageEntity[] tempImageBeans = new MessageImageEntity[length];
		MessageBodyEntity[] bodies = new MessageBodyEntity[length];
		Message[] msgs = new Message[length];

		for(int i = 0; i < length; ++i)
		{
			ImageFile image = imageFiles.get(i);
			String imagePath = image.getPath();

			// 图片集合
			tempImageBeans[i] = new MessageImageEntity();
			tempImageBeans[i].setSdcardPath(imagePath);
			tempImageBeans[i].setSize(String.valueOf(image.length()));

			// 封装body
			bodies[i] = new MessageBodyEntity();
			bodies[i].getImages().add(tempImageBeans[i]);
			bodies[i].setSendName(mApplication.mSelfUser.getUserName());
			bodies[i].setMsgType(mChatType);
			bodies[i].setContent("<img key=\"\">");

			msgs[i] = new Message();
//			Object tempResendChatBean = image.getResendChatBean();
//			//如果不为空，则说明是重新发送之前发送失败的图片  ADD by hy on 8.29
//			if (tempResendChatBean != null) {
//				String msgPackId = null;
//				if (tempResendChatBean instanceof P2PChatMsgEntity) {
//					msgPackId = ((P2PChatMsgEntity)tempResendChatBean).getPacketId();
//				} else if (tempResendChatBean instanceof GroupChatMsgEntity) {
//					msgPackId = ((GroupChatMsgEntity)tempResendChatBean).getPacketId();
//				}
//				if (msgPackId != null && !msgPackId.isEmpty()) {
//					msgs[i].setPacketID(msgPackId);0-
//				}
//			}
			//如果不为空，则说明是重新发送之前发送失败的图片  ADD by hy on 8.29
			if (reSendPacketId != null && !reSendPacketId.isEmpty()) {
				msgs[i].setPacketID(reSendPacketId);
			}
			msgs[i].setBody(JSON.toJSONString(bodies[i]));
			msgs[i].setFrom(JIDUtil.getJIDByAccount(myUserNo));
			msgs[i].setType(mChatType == Const.CHAT_TYPE_P2P ? Type.chat : Type.groupchat);
			msgs[i].setTo(mChatType == Const.CHAT_TYPE_P2P ? JIDUtil.getJIDByAccount(mReceiverUser) : JIDUtil
					.getGroupJIDByAccount(mReceiverUser));
			msgs[i].setSubject(bodies[i].getContent());

			if(mSendImageListener != null) {
                mSendImageListener.onBeforeEachSend(BaseChatMsgEntity.IMAGE, msgs[i], mChatType);
            }
		}

		for(int i = 0; i < length; ++i)
		{
			ImageFile image = imageFiles.get(i);
			String imagePath = image.getPath();

			String uploadJson = "";
			uploadJson = FileUtil.formatSendFileJsonByFilePath(mReceiverUser,mChatType == Const.CHAT_TYPE_P2P ? "1" : "2",imagePath,"1");
//			String[] uploadResult = FileUtil.uploadFile(uploadJson,imagePath);
//			String[] uploadResult = FileUtil.uploadFile(uploadJson,imagePath,msgs[i],bodies[i],mSendImageListener);
//			String[] uploadResult = FileUtil.uploadFileByHttpMine(uploadJson,imagePath);
			String[] uploadResult = FileUtil.uploadFile(uploadJson,imagePath,new UploadResponseHandler());
			if(uploadResult == null) {
				mSendImageListener.onFailedSend(BaseChatMsgEntity.IMAGE,msgs[i],mChatType);
				continue;
			} else if(uploadResult[0] == null) {
				mSendImageListener.onFailedSend(BaseChatMsgEntity.IMAGE,msgs[i],mChatType);
				continue;
			} else if (uploadResult[0].equals(UploadResponseHandler.mFailedSend)) {
				mSendImageListener.onFailedSend(BaseChatMsgEntity.IMAGE,msgs[i],mChatType);
				continue;
			}

			tempImageBeans[i].setKey(uploadResult[0]);
			tempImageBeans[i].setFileId(uploadResult[0]);
			tempImageBeans[i].setName(uploadResult[1]);
			tempImageBeans[i].setFileType(uploadResult[2]);

			// 清除掉List里面未包含上传成功返回的fileId而只含有sdcardPath的tempImageBean，重新添加完整信息
			bodies[i].getImages().clear();
			bodies[i].getImages().add(tempImageBeans[i]);
			bodies[i].setContent("<img key=\"" + uploadResult[0] + "\">");
			bodies[i].setResource(MessageBodyEntity.SOURCE_PHONE);

			msgs[i].setBody(null);
			msgs[i].setBody(JSON.toJSONString(bodies[i]));

			if(mSendImageListener != null) {
                mSendImageListener.onEachDone(BaseChatMsgEntity.IMAGE, msgs[i], mChatType);
            }

			boolean isSuccess =mXmppManager.sendPacket(msgs[i]);
			L.i(mTag,"发送图片"+msgs[i].toXML()+"发送状态"+isSuccess);
			//当前发送包因为各种原因失败了返回失败结果
			if (!isSuccess){
				mSendImageListener.onFailedSend(BaseChatMsgEntity.IMAGE,msgs[i],mChatType);
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

	/**
	 * 设置要重新发送的包ID
	 * @param reSendPacketId
	 */
	public void setResendChatBeanPacketId(String reSendPacketId) {
		this.reSendPacketId = reSendPacketId;
	}
}
