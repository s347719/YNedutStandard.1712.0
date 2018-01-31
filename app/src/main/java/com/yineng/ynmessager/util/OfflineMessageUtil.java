package com.yineng.ynmessager.util;

import com.yineng.ynmessager.app.Const;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by yhu on 2017/12/1.
 * 解析xmpp message消息
 */

public class OfflineMessageUtil {


    /**
     * 解析最后一条历史消息
     *
     * @param xml
     * @return
     */
    public static Message parserLastMessage(String xml) {
        Message message = new Message();
        try {
            InputStream inputStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inputStream, "UTF-8"); // 设置数据源编码
            int eventType = parser.getEventType(); // 获取事件类型
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG: // 开始读取某个标签
                        if ("message".equals(parser.getName())) {
                            String mFrom = parser.getAttributeValue("", "from");
                            String mTo = parser.getAttributeValue("", "to");
                            message.setPacketID(parser.getAttributeValue("", "id"));
                            message.setFrom(mFrom);
                            message.setTo(mTo);
                            if (parser.getAttributeValue("", "type").equals("groupchat")) {
                                message.setType(Message.Type.groupchat);
                                message.setFrom(mTo + "/" + JIDUtil.getAccountByJID(mFrom));
                                message.setTo(mFrom);
                            } else {
                                message.setType(Message.Type.chat);
                            }

                        } else if ("body".equals(parser.getName())) {
                            message.setBody(parser.nextText());
                        } else if (parser.getName().equals("sendTime")) {
                            message.setSendTime(parser.nextText());
                        } else if (parser.getName().equals("subject")) {
                            message.setSubject(parser.nextText());
                        } else if (parser.getName().equals("recipient")) {
                            final String recipientMsg = parser.nextText();
                            message.addExtension(new PacketExtension() {
                                @Override
                                public String getElementName() {
                                    return "recipient";
                                }

                                @Override
                                public String getNamespace() {
                                    return Const.BROADCAST_ID;
                                }

                                @Override
                                public String toXML() {
                                    return "<recipient xmlns=\"com:yineng:broadcast\">" + recipientMsg + " </recipient>";
                                }
                            });
                        }

                        break;
                    case XmlPullParser.END_TAG:
                        System.out.println("end name:" + parser.getName());
                        break;
                }
                eventType = parser.next();
            }
            inputStream.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return message;
    }

    /**
     * 解析历史消息
     *
     * @param xml
     * @return
     */
    public static Message parserMessage(String xml) {
        Message message = new Message();
        try {
            InputStream inputStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inputStream, "UTF-8"); // 设置数据源编码
            int eventType = parser.getEventType(); // 获取事件类型
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG: // 开始读取某个标签
                        if ("message".equals(parser.getName())) {
                            String mFrom = parser.getAttributeValue("", "from");
                            String mTo = parser.getAttributeValue("", "to");
                            message.setPacketID(parser.getAttributeValue("", "id"));
                            message.setFrom(mFrom);
                            message.setTo(mTo);
                            if (parser.getAttributeValue("", "type").equals("groupchat")) {
                                message.setType(Message.Type.groupchat);
                            } else {
                                message.setType(Message.Type.chat);
                            }

                        } else if ("body".equals(parser.getName())) {
                            message.setBody(parser.nextText());
                        } else if (parser.getName().equals("sendTime")) {
                            message.setSendTime(parser.nextText());
                        } else if (parser.getName().equals("subject")) {
                            message.setSubject(parser.nextText());
                        } else if (parser.getName().equals("recipient")) {
                            final String recipientMsg = parser.nextText();
                            message.addExtension(new PacketExtension() {
                                @Override
                                public String getElementName() {
                                    return "recipient";
                                }

                                @Override
                                public String getNamespace() {
                                    return Const.BROADCAST_ID;
                                }

                                @Override
                                public String toXML() {
                                    return "<recipient xmlns=\"com:yineng:broadcast\">" + recipientMsg + " </recipient>";
                                }
                            });
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        System.out.println("end name:" + parser.getName());
                        break;
                }
                eventType = parser.next();
            }
            inputStream.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return message;
    }
}
