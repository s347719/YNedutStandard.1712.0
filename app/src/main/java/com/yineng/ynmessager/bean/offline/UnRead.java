package com.yineng.ynmessager.bean.offline;

import com.yineng.ynmessager.app.Const;

import org.jivesoftware.smack.packet.PacketExtension;

/**
 * Created by yhu on 2017/12/1.
 * 离线列表未读消息条目
 */

public class UnRead implements PacketExtension {
    public static final String NAME = "unread";

    private int num;
    private String unreadMsgIds;

    public String getUnreadMsgIds() {
        return unreadMsgIds;
    }

    public void setUnreadMsgIds(String unreadMsgIds) {
        this.unreadMsgIds = unreadMsgIds;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public UnRead() {
    }

    @Override
    public String getElementName() {
        return NAME;
    }

    @Override
    public String getNamespace() {
        return Const.BROADCAST_ACTION_UNREADER_MSG_NUM;
    }

    @Override
    public String toXML() {
        return "<unread xmlns=\"" + Const.BROADCAST_ACTION_UNREADER_MSG_NUM + "\">"
                + "<unreadCount>" + num + "</unreadCount> "
                + "<unreadMsgIds>" + unreadMsgIds + "</unreadMsgIds>"
                + "</unread>";
    }
}
