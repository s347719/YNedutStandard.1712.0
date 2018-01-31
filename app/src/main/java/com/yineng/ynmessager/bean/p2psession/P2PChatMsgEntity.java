package com.yineng.ynmessager.bean.p2psession;

public class P2PChatMsgEntity extends BaseChatMsgEntity
{
    private int gender;// 1-男 2-女 3-保密 用于在查找聊天信息时判断性别


    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

}
