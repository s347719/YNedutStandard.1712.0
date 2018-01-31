package com.yineng.ynmessager.bean.p2psession;

/**
 * 自定义表情
 * 
 * @author 胡毅
 * 
 */
public class MessageCustomAvatarsEntity {
	/**
	 * 消息体中自定义表情编码
	 */
	private String Key;
	/**
	 * 自定义表情名称
	 */
	private String Name;
	/**
	 * 表情文件大小，以字节为单位
	 */
	private String Size;
	/**
	 * 表情类型
	 */
	private String FileType;
	/**
	 * 自定义表情访问标识
	 */
	private String FileId;
	/**
	 * 表情图片源宽度（像素）
	 */
	private String Width;
	/**
	 * 表情图片源高度（像素）
	 */
	private String Height;

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
}
