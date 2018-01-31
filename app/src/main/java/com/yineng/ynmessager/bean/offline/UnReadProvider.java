package com.yineng.ynmessager.bean.offline;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

/**
 * Created by yhu on 2017/12/1.
 * 解析未读消息
 */

public class UnReadProvider implements PacketExtensionProvider {

    @Override
    public PacketExtension parseExtension(XmlPullParser xmlPullParser) throws Exception {
        UnRead unRead = new UnRead();
        int eventType = xmlPullParser.getEventType(); // 获取事件类型
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (xmlPullParser.getName().equals("unreadCount")) {
                        try {
                            unRead.setNum(Integer.parseInt(xmlPullParser.nextText()));
                        } catch (Exception e) {
                            e.printStackTrace();
                            unRead.setNum(0);
                        }
                    } else if (xmlPullParser.getName().equals("unreadMsgIds")) {
                        unRead.setUnreadMsgIds(xmlPullParser.nextText());
                    }
                    break;
            }
            eventType = xmlPullParser.next();
        }
        return unRead;
    }
}