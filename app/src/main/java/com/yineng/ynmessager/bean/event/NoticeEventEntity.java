package com.yineng.ynmessager.bean.event;

/**
 * Created by 舒欢
 * Created time: 2017/7/6
 */

public class NoticeEventEntity {

    /**
     * readStatus : 0
     * hasAttachment :
     * senderId : a5517023-9e77-47d7-8a9a-10decfe3636c
     * msgContent : 长沙市信息职业技术学校部门发布了关于“石文灿收通知公告了”的通知，请知晓！
     * hasPic : 0
     * messageType : 1
     * subject : 通知公告发布提醒！
     * msgId : a5517023-9e77-47d7-8a9a-10decfe3636c
     * sendTime : 2017-07-05 12:23:12
     */


    /**
     * 阅读状态 0:未阅读 1：已阅读
     */
    private String readStatus;
    /**
     * 0 是否有附件
     */
    private String hasAttachment;
    /**
     * 发送人
     */
    private String senderId;
    /**
     *  消息内容
     */
    private String msgContent;
    /**
     * 是否有图片，0:无 1:有
     */
    private String hasPic;
    /**
     *  消息类型(0:系统消息 1:通知公告 2：messenger消息)
     */
    private String messageType;
    /**
     *   消息标题
     */
    private String subject;
    /**
     * 消息ID
     */
    private String msgId;
    /**
     * 发送时间 格式:yyyy-MM-dd HH:mm:ss
     */
    private String sendTime;

    public String getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(String readStatus) {
        this.readStatus = readStatus;
    }

    public String getHasAttachment() {
        return hasAttachment;
    }

    public void setHasAttachment(String hasAttachment) {
        this.hasAttachment = hasAttachment;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    public String getHasPic() {
        return hasPic;
    }

    public void setHasPic(String hasPic) {
        this.hasPic = hasPic;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }
}
