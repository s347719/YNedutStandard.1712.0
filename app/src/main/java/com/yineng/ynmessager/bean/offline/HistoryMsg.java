package com.yineng.ynmessager.bean.offline;

/**
 * Created by yhu on 2017/12/1.
 * 历史消息
 */

public class HistoryMsg {

    /**
     * 会话类型 1:两人会话;2:群组或者讨论组;3:广播
     */
    public int chatType;
    /**
     * 会话id
     */
    public String chatId;
    /**
     * 拉去消息数量，如果是正数表示当前时间之前的消息。反之亦然
     */
    public String msgCount;
    /**
     * 消息时间戳
     */
    public String sendTime;

    public HistoryMsg() {
    }

    public HistoryMsg(int chatType, String chatId, String msgCount, String sendTime) {
        this.chatType = chatType;
        this.chatId = chatId;
        this.msgCount = msgCount;
        this.sendTime = sendTime;
    }

    public int getChatType() {
        return chatType;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getMsgCount() {
        return msgCount;
    }

    public void setMsgCount(String msgCount) {
        this.msgCount = msgCount;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }
}
