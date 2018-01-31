
package com.yineng.ynmessager.smack;

import android.content.Context;

import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.groupsession.GroupChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.P2PChatMsgEntity;
import com.yineng.ynmessager.db.P2PChatMsgDao;
import com.yineng.ynmessager.db.dao.DisGroupChatDao;
import com.yineng.ynmessager.db.dao.GroupChatDao;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.TimeUtil;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

/**
 * @author Yutang
 * Date 2014-12-29 所有IQ消息总的监听接口实例
 *
 */
public class IQPacketListenerImpl implements PacketListener {
	private XmppConnectionManager mXmppConnManager;
	/**
	 * 两人会话数据库操作对象
	 */
	private P2PChatMsgDao mP2PChatMsgDao;
	/**
	 * 讨论组会话数据库操作对象
	 */
	private DisGroupChatDao mDisGroupChatDao;
	/**
	 * 群组会话数据库操作对象
	 */
	private GroupChatDao mGroupChatDao;
	
	public IQPacketListenerImpl(Context mContext) {
		mXmppConnManager = XmppConnectionManager.getInstance();
		mP2PChatMsgDao = new P2PChatMsgDao(mContext);
		mDisGroupChatDao = new DisGroupChatDao(mContext);
		mGroupChatDao = new GroupChatDao(mContext);
	}
	

	@Override
	public void processPacket(Packet arg0) {
		//
		L.v("IQPacketListenerImpl",
				"IQPacketListenerImpl receive packet xml ->:"
						+ arg0.toXML());
		final ReqIQResult response = (ReqIQResult) arg0;
		L.v("type == "+response.getTypeStr());
		final ReceiveReqIQCallBack callback;
		if ((callback = mXmppConnManager.getReceiveReqIQCallBack(response
				.getNameSpace())) != null) {//根据命名空间转发IQ消息
			if (/*response.getNameSpace().equals(Const.REQ_IQ_XMLNS_CLIENT_INIT) 
					|| */response.getNameSpace().equals(Const.REQ_IQ_XMLNS_GET_ORG) 
					|| response.getNameSpace().equals(Const.REQ_IQ_XMLNS_GET_GROUP)
					|| response.getNameSpace().equals(Const.REQ_IQ_XMLNS_GET_STATUS)) {
				
//				FileDownLoader.getInstance().getThreadPool().execute(new Runnable() {
//					
//					@Override
//					public void run() {
//						callback.receivedReqIQResult(response);
//					}
//				});
				new Thread(){
					@Override
                    public void run() {
						callback.receivedReqIQResult(response);
					}
				}.start();
			} else {
				callback.receivedReqIQResult(response);
			}
			
		}else{
			//如果是message回执IQ 
			if (response.getNameSpace().equals(Const.REQ_IQ_XMLNS_MSG_RESULT)) {
				if (BaseActivity.mChatMsgBeanMap.containsKey(response.getPacketID())) {
					Object unResultChatMstEntity = BaseActivity.mChatMsgBeanMap.get(response.getPacketID());
					if (unResultChatMstEntity instanceof P2PChatMsgEntity) {
						P2PChatMsgEntity unResultP2PChatMsg = (P2PChatMsgEntity) unResultChatMstEntity;
						unResultP2PChatMsg.setIsSuccess(GroupChatMsgEntity.SEND_SUCCESS);
						//更新发送消息收到回执后的发送成功时间
						long time = TimeUtil.getMillisecondByDate(response.getSendTime(), TimeUtil.FORMAT_DATETIME_24_mic);
						unResultP2PChatMsg.setTime(String.valueOf(time));
						mP2PChatMsgDao.saveOrUpdate(unResultP2PChatMsg);
					} else {
						GroupChatMsgEntity unResultGroupChatMsg = (GroupChatMsgEntity) unResultChatMstEntity;
						unResultGroupChatMsg.setIsSuccess(GroupChatMsgEntity.SEND_SUCCESS);
						//更新发送消息收到回执后的发送成功时间
						long time = TimeUtil.getMillisecondByDate(response.getSendTime(), TimeUtil.FORMAT_DATETIME_24_mic);
						unResultGroupChatMsg.setTime(String.valueOf(time));
						if (unResultGroupChatMsg.getGroupId().startsWith("dis")) {
							mDisGroupChatDao.saveOrUpdate(unResultGroupChatMsg);
						} else {
							mGroupChatDao.saveOrUpdate(unResultGroupChatMsg);
						}
					}
					BaseActivity.mChatMsgBeanMap.remove(response.getPacketID());
				}
				
			}
		}
	}
}
