//***************************************************************
//*    2015-6-19  下午3:07:02
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.app.update;

import java.io.Serializable;

/**
 * @author 胡毅
 * 
 */
public class ProductInfo implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -456018441526756181L;

	private String publishTime;
	
	private long fileSize;
	
	/**
	 * 更新说明
	 */
	private String changeNote;
	
	private FileDataInfo fileDataMetaTempVO;
	
	/**
	 * 是否必须更新 
	 * 0否 1是
	 */
	private int isMustUpdate;

	/**
	 * 是否提示更新 1提示  0不提示
	 */
	private int remindUpdate;
	
	private String attachmentId;
	
	private String updateTimes;
	
	private String newesProVersionCode;

	public String getPublishTime() {
		return publishTime;
	}

	public int getRemindUpdate() {
		return remindUpdate;
	}

	public void setRemindUpdate(int remindUpdate) {
		this.remindUpdate = remindUpdate;
	}

	public void setPublishTime(String publishTime) {
		this.publishTime = publishTime;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getChangeNote() {
		return changeNote;
	}

	public void setChangeNote(String changeNote) {
		this.changeNote = changeNote;
	}

	public FileDataInfo getFileDataMetaTempVO() {
		return fileDataMetaTempVO;
	}

	public void setFileDataMetaTempVO(FileDataInfo fileDataMetaTempVO) {
		this.fileDataMetaTempVO = fileDataMetaTempVO;
	}

	public int getIsMustUpdate() {
		return isMustUpdate;
	}

	public void setIsMustUpdate(int isMustUpdate) {
		this.isMustUpdate = isMustUpdate;
	}

	public String getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(String attachmentId) {
		this.attachmentId = attachmentId;
	}

	public String getUpdateTimes() {
		return updateTimes;
	}

	public void setUpdateTimes(String updateTimes) {
		this.updateTimes = updateTimes;
	}

	public String getNewesProVersionCode() {
		return newesProVersionCode;
	}

	public void setNewesProVersionCode(String newesProVersionCode) {
		this.newesProVersionCode = newesProVersionCode;
	}
	
}
