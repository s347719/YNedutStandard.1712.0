package com.yineng.ynmessager.bean.p2psession;

/**
 * 消息中文件发送集
 * 
 * @author 胡毅
 * 
 */
public class MessageFileEntity {
	/**
	 * 消息体中文件编码
	 */
	private String Key;
	/**
	 * 文件名称
	 */
	private String Name;
	/**
	 * 文件大小，以字节为单位
	 */
	private String Size;
	/**
	 * 文件类型
	 */
	private String FileType;
	/**
	 * 文件访问标识
	 */
	private String FileId;
	
	/**
	 * 发送文件时存的本地图片路径
	 */
	private String SdcardPath;

	public String getKey() {
		return Key;
	}

	public void setKey(String key) {
		Key = key;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getSize() {
		return Size;
	}

	public void setSize(String size) {
		Size = size;
	}

	public String getFileType() {
		return FileType;
	}

	public void setFileType(String fileType) {
		FileType = fileType;
	}

	public String getFileId() {
		return FileId;
	}

	public void setFileId(String fileId) {
		FileId = fileId;
	}
	
	public String getSdcardPath() {
		return SdcardPath;
	}

	public void setSdcardPath(String sdcardPath) {
		SdcardPath = sdcardPath;
	}
}
