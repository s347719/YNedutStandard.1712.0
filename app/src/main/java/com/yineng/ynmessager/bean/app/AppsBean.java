//***************************************************************
//*    2015-6-26  下午3:38:12
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.bean.app;

/**
 * @author 胡毅
 *  应用实体类
 */
public class AppsBean {

	/**
	 * 当前应用id
	 */
	private String id;
	/**
	 * 当前应用名称
	 */
	private String name;
	/**
	 * 当前应用所属的应用id
	 */
	private String parentId;
	/**
	 * 当前应用所属的应用名
	 */
	private String parentName;
	private String belong;
	/**
	 * 当前应用链接地址
	 */
	private String reqUrl;
	
	private int showTag = 0;
	
	public AppsBean() {
		super();
	}

	public AppsBean(String id, String name, String parentId, String parentName,
			String belong, String reqUrl) {
		super();
		this.id = id;
		this.name = name;
		this.parentId = parentId;
		this.parentName = parentName;
		this.belong = belong;
		this.reqUrl = reqUrl;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public String getBelong() {
		return belong;
	}

	public void setBelong(String belong) {
		this.belong = belong;
	}

	public String getReqUrl() {
		return reqUrl;
	}

	public void setReqUrl(String reqUrl) {
		this.reqUrl = reqUrl;
	}

	/**
	 * @param i
	 */
	public void setShowTag(int i) {
		showTag = i;
	}
	
	public int getShowTag() {
		return showTag;
	}

}
