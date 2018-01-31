package com.yineng.ynmessager.activity.TransmitActivity;

import java.io.Serializable;

/**
 * 转发图片创建的实体类对象，因为BaseChatMsgEntity中引用了Android。text.spanstring无法序列化无法在Intent中传递
 *
 * Created by 舒欢
 * Created time: 2017/4/24
 */
public class TransMsgEntity implements Serializable {


    private String message;

    private int isSuccess;// 0发送成功,1发送失败，2发送中; 如果messageType == 2，则该标志标识下载过程的状态

    private int isSend;// 0:是发送 1:不是发送（即接收）

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(int isSuccess) {
        this.isSuccess = isSuccess;
    }

    public int getIsSend() {
        return isSend;
    }

    public void setIsSend(int isSend) {
        this.isSend = isSend;
    }
}
