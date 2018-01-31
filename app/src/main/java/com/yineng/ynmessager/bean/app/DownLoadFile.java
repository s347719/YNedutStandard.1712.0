package com.yineng.ynmessager.bean.app;

/**
 * Created by 舒欢
 * Created time: 2017/9/7
 * Descreption：
 */

public class DownLoadFile {


    public static int FILE_SOURCE_PERSON = 0;
    public static int FILE_SOURCE_DIS = 1;
    public static int FILE_SOURCE_GROUP = 2;
    public static int FILE_SOURCE_SHARED_GROUP = 3;
    public static int FILE_SOURCE_PROMISE = 4;
    public static int FILE_SOURCE_OA = 5;

    public static int IS_SEND = 0;
    public static int IS_RECEIVE = 1;
    public static int IS_UNKOWN =2;

    public static int FILETYPE_IMG = 1;
    public static int FILETYPE_MOIVE = 2;
    public static int FILETYPE_VOICE = 3;
    public static int FILETYPE_DOC = 4;
    public static int FILETYPE_EXCEL = 5;
    public static int FILETYPE_PDF = 6;
    public static int FILETYPE_PPT = 7;
    public static int FILETYPE_ZIP = 8;
    public static int FILETYPE_OTHER = 9;

    /**
     * String fileName    图片的文件名  ：即数据包中返回的 packetId 后缀是 .jpg
     * String fileId  当前会话的ID
     * int fileSource  当前保存图片会话类型   0-个人会话  1-讨论组   2-群  3-来自群共享 4-流程审批  5-OA申请
     * String dataTime   图片保存时间（取数据保存的时间）
     * int isSend     图片是发送的还是接收的    0:是发送 1:不是发送（即接收） 2:既不是发送也不是接收(从openfire下载)
     * int fileType   下载文件类型在这里都是图片为 1    1-图片  2-视频  3-音频 4-doc  5-excel 6-pdf  7-ppt 8- 压缩包 9=-其他
     * String sendUserNo  发送者帐号：userNo
     * String size   文件的大小
     * "packetId";  // 下载文件在聊天记录中的packetId 唯一标记
     */
    private String fileName;
    private String fileId;
    private int fileSource;
    private String dataTime;
    private int isSend;
    private int fileType;
    private String sendUserNo;
    private String packetId;
    private String size;

    private boolean isCheck = false;
    private String tag;

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getPacketId() {
        return packetId;
    }

    public void setPacketId(String packetId) {
        this.packetId = packetId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public int getFileSource() {
        return fileSource;
    }

    public void setFileSource(int fileSource) {
        this.fileSource = fileSource;
    }

    public String getDataTime() {
        return dataTime;
    }

    public void setDataTime(String dataTime) {
        this.dataTime = dataTime;
    }

    public int getIsSend() {
        return isSend;
    }

    public void setIsSend(int isSend) {
        this.isSend = isSend;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public String getSendUserNo() {
        return sendUserNo;
    }

    public void setSendUserNo(String sendUserNo) {
        this.sendUserNo = sendUserNo;
    }
}
