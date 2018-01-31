package com.yineng.ynmessager.bean.p2psession;

/**
 * 消息体中的图片集
 * 
 * @author 胡毅
 * 
 */
public class MessageImageEntity {
	/**
	 * 消息体中图片代码
	 */
	private String Key;
	/**
	 * 图片名称
	 */
	private String Name;
	/**
	 * 图片大小，以字节为单位
	 */
	private String Size;
	/**
	 * 文件类型
	 */
	private String FileType;
	/**
	 * 图片访问标识
	 */
	private String FileId;
	/**
	 * 图片源宽度（像素）
	 */
	private String Width;
	/**
	 * 图片源高度（像素）
	 */
	private String Height;
	/**
	 * 发送图片时存的本地图片路径；为NULL时表明是接收图片,不为NULL时表明是发送图片
	 */
	private String SdcardPath = null;

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

	public String getWidth() {
		return Width;
	}

	public void setWidth(String width) {
		Width = width;
	}

	public String getHeight() {
		return Height;
	}

	public void setHeight(String height) {
		Height = height;
	}

	public String getSdcardPath() {
		return SdcardPath;
	}

	public void setSdcardPath(String sdcardPath) {
		SdcardPath = sdcardPath;
	}
}
