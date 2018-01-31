package com.yineng.ynmessager.bean.groupsession;

import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;



public class GroupChatMsgEntity extends BaseChatMsgEntity
{

	private String groupId;// 群ID;

	private String senderName;    //该条消息的发送者的名称

	private int gender;// 1-男 2-女 3-保密 用于在查找聊天信息时判断性别

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}
}
