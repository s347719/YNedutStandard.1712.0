//***************************************************************
//*    2015-4-20  下午2:08:03
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.bean.offline;


/**
 * 离线消息
 *
 * @author yhu
 */
public class OfflineMsgList {

    /**
     * 用户id或组id
     */
    private String chatId;
    /**
     * 会话类型 1:2人会话 2:讨论组或者群 3:广播
     */
    private String chatType;
    /**
     * 唯一标示
     */
    private String id;
    /**
     * 最后一条消息
     */
    private String lastMsg;
    /**
     * 最后消息时间
     */
    private double lastMsgDate;
    /**
     * 会话列表拥有人
     */
    private String owner;
    /**
     * 未读数量
     */
    private int unreadCount;
    /**
     * 广播未读id
     */
    private String unreadMsgIds;

    public OfflineMsgList() {
    }

    public OfflineMsgList(String chatId, String chatType, String id, String lastMsg, double lastMsgDate, String owner, int unreadCount, String unreadMsgIds) {
        this.chatId = chatId;
        this.chatType = chatType;
        this.id = id;
        this.lastMsg = lastMsg;
        this.lastMsgDate = lastMsgDate;
        this.owner = owner;
        this.unreadCount = unreadCount;
        this.unreadMsgIds = unreadMsgIds;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getChatType() {
        return chatType;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public double getLastMsgDate() {
        return lastMsgDate;
    }

    public void setLastMsgDate(double lastMsgDate) {
        this.lastMsgDate = lastMsgDate;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getUnreadMsgIds() {
        return unreadMsgIds;
    }

    public void setUnreadMsgIds(String unreadMsgIds) {
        this.unreadMsgIds = unreadMsgIds;
    }

    @Override
    public String toString() {
        return "OfflineMsg{" +
                "chatId='" + chatId + '\'' +
                ", chatType='" + chatType + '\'' +
                ", id='" + id + '\'' +
                ", lastMsg='" + lastMsg + '\'' +
                ", lastMsgDate=" + lastMsgDate +
                ", owner='" + owner + '\'' +
                ", unreadCount=" + unreadCount +
                ", unreadMsgIds='" + unreadMsgIds + '\'' +
                '}';
    }
}
