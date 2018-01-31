package com.yineng.ynmessager.bean.offline;

import com.yineng.ynmessager.app.Const;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.EmbeddedExtensionProvider;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;

import java.util.List;
import java.util.Map;

/**
 * Created by yhu on 2017/11/29.
 * 消息回执
 */

public class MessageRevicerEntity extends DeliveryReceipt {

    /**
     * 标签名称
     */
    private String elementName = "received";

    private int type;


    public MessageRevicerEntity(String id, int type) {
        super(id);
        this.type = type;
    }

    public MessageRevicerEntity(String id) {
        super(id);
    }


    @Override
    public String getElementName() {
        return elementName;
    }

    @Override
    public String getNamespace() {
        return Const.MESSAGE_READED_RECEIVER;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * 返回扩展的xml字符串
     * 此字符串作为message元素的子元素
     */
    @Override
    public String toXML() {
        return "<received xmlns='urn:xmpp:receipts' id='" + this.getId() + "'" + " type='" + this.getType() + "'" + "/>";
    }

    public static class Provider extends EmbeddedExtensionProvider {
        public Provider() {
        }

        protected PacketExtension createReturnExtension(String currentElement, String currentNamespace, Map<String, String> attributeMap, List<? extends PacketExtension> content) {
            return new MessageRevicerEntity((String) attributeMap.get("id"), Integer.parseInt(attributeMap.get("type")));
        }
    }

}
