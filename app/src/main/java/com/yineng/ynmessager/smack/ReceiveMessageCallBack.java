package com.yineng.ynmessager.smack;

import com.yineng.ynmessager.bean.groupsession.GroupChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.P2PChatMsgEntity;

public interface ReceiveMessageCallBack {
	void  receivedMessage(P2PChatMsgEntity msg);
	void receivedMessage(GroupChatMsgEntity msg);
}
