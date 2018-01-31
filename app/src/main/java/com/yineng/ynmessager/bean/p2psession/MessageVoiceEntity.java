package com.yineng.ynmessager.bean.p2psession;

/**
 * Created by huyi on 2016/5/4.
 */
public class MessageVoiceEntity {

    private String id;
    private int time;
    private boolean sentSuccess;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public boolean isSentSuccess()
    {
        return sentSuccess;
    }

    public void setSentSuccess(boolean sentSuccess)
    {
        this.sentSuccess = sentSuccess;
    }
}
