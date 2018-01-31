package com.yineng.ynmessager.bean.p2psession;

import java.util.LinkedList;
import java.util.List;

/**
 * smack的Message中的body类
 * 
 * @author 胡毅
 * 
 */
public class MessageBodyEntity {

	/**
	 * pc资源
	 */
	public static final String SOURCE_PC = "Msg_PC";
	/**
	 * 移动端资源
	 */
	public static final String SOURCE_PHONE = "Msg_Phone";
	/**
	 * 消息内容
	 */
	private String Content;
	/**
	 * 会话类型1为2人会话，2为讨论组会话，3为群会话，4为广播
	 */
	private int MsgType;
	/**
	 * 发送人的昵称
	 */
	private String SendName;
	/**
	 * 资源类型，PC:Msg_PC 手机:Msg_Phone
	 */
	private String resource ;
	/**
	 * 消息体中的图片集
	 */
	private List<MessageImageEntity> Images = new LinkedList<MessageImageEntity>();
	/**
	 * 消息体中自定义表情
	 */
	private List<MessageCustomAvatarsEntity> CustomAvatars = new LinkedList<MessageCustomAvatarsEntity>();
	/**
	 * 消息中文件发送集
	 */
	private List<MessageFileEntity> Files = new LinkedList<MessageFileEntity>();
	/**
	 * 字体
	 */
	private MessageStyleEntity Style = new MessageStyleEntity();

	private MessageVoiceEntity voice = new MessageVoiceEntity();
	
//	private int Progress = 0;

	public String getContent() {
		return Content;
	}

	public void setContent(String content) {
		Content = content;
	}

	public int getMsgType() {
		return MsgType;
	}

	public void setMsgType(int msgType) {
		MsgType = msgType;
	}

	public List<MessageImageEntity> getImages() {
		return Images;
	}

	public void setImages(List<MessageImageEntity> images) {
		Images = images;
	}

	public List<MessageCustomAvatarsEntity> getCustomAvatars() {
		return CustomAvatars;
	}

	public void setCustomAvatars(List<MessageCustomAvatarsEntity> customAvatars) {
		CustomAvatars = customAvatars;
	}

	public List<MessageFileEntity> getFiles() {
		return Files;
	}

	public void setFiles(List<MessageFileEntity> files) {
		Files = files;
	}

	public MessageStyleEntity getStyle() {
		return Style;
	}

	public void setStyle(MessageStyleEntity style) {
		Style = style;
	}

	public String getSendName() {
		return SendName;
	}

	public void setSendName(String sendName) {
		SendName = sendName;
	}

	public MessageVoiceEntity getVoice() {
		return voice;
	}

	public void setVoice(MessageVoiceEntity voice) {
		this.voice = voice;
	}
	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}
}
