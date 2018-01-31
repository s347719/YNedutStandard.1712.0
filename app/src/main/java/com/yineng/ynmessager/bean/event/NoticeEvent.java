package com.yineng.ynmessager.bean.event;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

/**
 * Created by 贺毅柳 on 2015/11/16 17:00.
 */
public class NoticeEvent implements Parcelable
{
    private int id;
    private String title;
    private String packetId;
    private String userNo;
    private String userName;
    private String receiver;
    private String message;
    private String content;
    private String msgType;
    /**
     * 相对地址URL
     */
    private String url;
    private Date timeStamp;
    private boolean read;
    /**
     * 来源终端(0:YNedut8 1:企业信息化)
     */
    private int sourceTerminal;
    /**
     * 是否有附件
     */
    private String hasAttachment;
    /**
     * 是否有图片
     */
    private String hasPic;
    /**
     * 消息类型(0:告知类系统消息 1:通知公告 2：messenger消息)
     */
    private String messageType;
    /**
     * V8的消息通知ID
     */
    private String msgId;
    public static final String ACTION_REFRESH = "events.notice.refresh"; // 事件应该被刷新的广播Action
    public static final String ACTION_REFRESH_UNREAD = "events.notice.refresh.unread"; // 事件应该被刷新的广播Action
    public static final String ACTION_TUISONG_EVENT= "events.ynmessager.notice.tuisong"; // 阿里推送的广播
    public static final String SYSTEM_MSG = "0";
    public static final String NOTICE_MSG = "1";
    public static final String BROADCAST_MSG = "2";
    // 其他属性
    private boolean checked = false;

    public static final Parcelable.Creator<NoticeEvent> CREATOR = new Parcelable.Creator<NoticeEvent>()
    {
        @Override
        public NoticeEvent createFromParcel(Parcel in)
        {
            return new NoticeEvent(in);
        }

        @Override
        public NoticeEvent[] newArray(int size)
        {
            return new NoticeEvent[size];
        }
    };

    public NoticeEvent() {}

    private NoticeEvent(Parcel in)
    {
        this.id = in.readInt();
        this.title = in.readString();
        this.packetId = in.readString();
        this.userNo = in.readString();
        this.userName = in.readString();
        this.receiver = in.readString();
        this.message = in.readString();
        this.content = in.readString();
        this.url = in.readString();
        this.timeStamp = (Date) in.readSerializable();
        this.read = in.readByte() != 0;
        this.sourceTerminal = in.readInt();
        this.url = in.readString();
        this.hasAttachment = in.readString();
        this.hasPic = in.readString();
        this.messageType = in.readString();
        this.msgId = in.readString();
        this.msgType = in.readString();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(this.id);
        dest.writeString(this.title);
        dest.writeString(this.packetId);
        dest.writeString(this.userNo);
        dest.writeString(this.userName);
        dest.writeString(this.receiver);
        dest.writeString(this.message);
        dest.writeString(this.content);
        dest.writeString(this.url);
        dest.writeSerializable(this.timeStamp);
        dest.writeByte((byte) (this.read ? 1 : 0));
        dest.writeInt(this.sourceTerminal);
        dest.writeString(this.url);
        dest.writeString(this.hasAttachment);
        dest.writeString(this.hasPic);
        dest.writeString(this.messageType);
        dest.writeString(this.msgId);
        dest.writeString(this.msgType);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getPacketId()
    {
        return packetId;
    }

    public void setPacketId(String packetId)
    {
        this.packetId = packetId;
    }

    public String getUserNo()
    {
        return userNo;
    }

    public void setUserNo(String userNo)
    {
        this.userNo = userNo;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getReceiver()
    {
        return receiver;
    }

    public void setReceiver(String receiver)
    {
        this.receiver = receiver;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public Date getTimeStamp()
    {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public boolean isRead()
    {
        return read;
    }

    public void setRead(boolean read)
    {
        this.read = read;
    }

    public int getSourceTerminal()
    {
        return sourceTerminal;
    }

    public void setSourceTerminal(int sourceTerminal)
    {
        this.sourceTerminal = sourceTerminal;
    }

    public boolean isChecked()
    {
        return checked;
    }

    public void setChecked(boolean checked)
    {
        this.checked = checked;
    }

    @Override
    public boolean equals(Object o)
    {
        boolean equals = false;

        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }

        if (o instanceof NoticeEvent)
        {
            NoticeEvent notice = (NoticeEvent) o;
            equals = new EqualsBuilder()
                    .append(this.id, notice.getId())
                    .isEquals();
        }
        return equals;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
                .append(this.id)
                .toHashCode();
    }

    public String getHasAttachment() {
        return hasAttachment;
    }

    public void setHasAttachment(String hasAttachment) {
        this.hasAttachment = hasAttachment;
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

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }
}
