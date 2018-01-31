//***************************************************************
//*    2015-8-31  下午4:19:45
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.bean.p2psession;


import android.text.SpannableString;

import java.io.Serializable;

/**
 * @author 贺毅柳
 */
public class BaseChatMsgEntity implements Serializable{

    // 已读和未读标识
    public static final int IS_READED = 1;
    public static final int IS_NOT_READED = 0;

    // 发送成功或失败的标识
    public static final int SEND_SUCCESS = 0;
    public static final int SEND_FAILED = 1;
    public static final int SEND_ING = 2;

    // 下载文件成功、失败或正在进行的标识
    public static final int DOWNLOAD_NOT_YET = 10;
    public static final int DOWNLOAD_ING = 11;
    public static final int DOWNLOAD_SUCCESS = 12;
    public static final int DOWNLOAD_FAILED = 13;

    // 消息发送类型
    public static final int COM_MSG = 1;
    public static final int TO_MSG = 0;

    // 0:普通消息 1:图片 2：文件 3:语音文件
    public static final int MESSAGE = 0;
    public static final int IMAGE = 1;
    public static final int FILE = 2;
    public static final int AUDIO_FILE = 3;
    public static final int FILE_IQ_STATE = 12;

    // 0:是发送 1:不是发送（即接收）
    public static final int SEND = 0;
    public static final int RECEIVE = 0;

    private String packetId;// 数据包id

    private String chatUserNo;    // 发送：id为自己  接收：id为别人

    private int messageType;// 发送消息类型 0:普通消息 1:图片 2：文件 3:语音文件  12:文件接收状态的回执代表已下载
    private int chatType; //1 两人会话; 2 群会话; 3 讨论组会话
    private String message;
    private String time;// 发送时间
    private int isSend;// 0:是发送 1:不是发送（即接收）
    private int isSuccess;// 0发送成功,1发送失败，2发送中; 如果messageType == 2，则该标志标识下载过程的状态
    private int isReaded;// 1:已读/0:未读
    private boolean isShowTime;// UI显示时，指定是否显示时间
    private String fileId;// 文件id,映射FileAttr表中的主键
    private int receiveProgress = 0;//文件下载时已获取到的大小
    private long receivedBytes = 0;//文件下载时已获取到的大小
    /**
     * 用于界面显示的文本
     */
    private SpannableString spannableString;

    public String getPacketId() {
        return packetId;
    }

    public void setPacketId(String packetId) {
        this.packetId = packetId;
    }

    public int getIsSend() {
        return isSend;
    }

    public void setIsSend(int isSend) {
        this.isSend = isSend;
    }

    public String getChatUserNo() {
        return chatUserNo;
    }

    public void setChatUserNo(String chatUserNo) {
        this.chatUserNo = chatUserNo;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(int isSuccess) {
        this.isSuccess = isSuccess;
    }

    public int getIsReaded() {
        return isReaded;
    }

    public void setIsReaded(int isReaded) {
        this.isReaded = isReaded;
    }

    public boolean isShowTime() {
        return isShowTime;
    }

    public void setShowTime(boolean isShowTime) {
        this.isShowTime = isShowTime;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public SpannableString getSpannableString() {
        return spannableString;
    }

    public void setSpannableString(SpannableString spannableString) {
        this.spannableString = spannableString;
    }

    public int getReceiveProgress() {
        return receiveProgress;
    }

    public void setReceiveProgress(int receiveProgress) {
        this.receiveProgress = receiveProgress;
    }

    public int getChatType() {
        return chatType;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }


    public long getReceivedBytes() {
        return receivedBytes;
    }

    public void setReceivedBytes(long receivedBytes) {
        this.receivedBytes = receivedBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BaseChatMsgEntity) {
            BaseChatMsgEntity mChatMsgEntity = (BaseChatMsgEntity) o;
            return mChatMsgEntity.packetId.equals(packetId);
        }
        return super.equals(o);
    }

}
