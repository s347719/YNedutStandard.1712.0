//***************************************************************
//*    2015-9-8  下午12:01:17
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
public class FileDataInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8825098336994916576L;

	private String userCode;
	
	private String basePath;
	
	private String relativePath;
	
	private String serverGroupName = "";
	
	private String serverFilePath = "";
	
	private String fileURLMappingId = "";
	
	private String type = "";
	
	private String description = "";
	
	private long size;

	public String getUserCode() {
		return userCode;
	}

	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public String getServerGroupName() {
		return serverGroupName;
	}

	public void setServerGroupName(String serverGroupName) {
		this.serverGroupName = serverGroupName;
	}

	public String getServerFilePath() {
		return serverFilePath;
	}

	public void setServerFilePath(String serverFilePath) {
		this.serverFilePath = serverFilePath;
	}

	public String getFileURLMappingId() {
		return fileURLMappingId;
	}

	public void setFileURLMappingId(String fileURLMappingId) {
		this.fileURLMappingId = fileURLMappingId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
	
}
