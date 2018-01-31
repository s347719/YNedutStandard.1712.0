//***************************************************************
//*    2015-10-12  下午5:51:38
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.bean.app.evaluate;

import java.io.Serializable;

/**
 * @author 胡毅
 * 被评人对象
 */
public class EvaPersonBean implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1852459565574685931L;

	/**
	 * 被评人ID
	 */
	private String id;
	
	/**
	 * 被评人姓名
	 */
	private String evaluateName;
	
	/**
	 * 评教状态：0 待评  1 已评
	 */
	private int evaluateStatus;
	
	/**
	 * 头像地址，暂不实现
	 */
	private String headImgUrl;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEvaluateName() {
		return evaluateName;
	}

	public void setEvaluateName(String evaluateName) {
		this.evaluateName = evaluateName;
	}

	public int getEvaluateStatus() {
		return evaluateStatus;
	}

	public void setEvaluateStatus(int evaluateStatus) {
		this.evaluateStatus = evaluateStatus;
	}

	public String getHeadImgUrl() {
		return headImgUrl;
	}

	public void setHeadImgUrl(String headImgUrl) {
		this.headImgUrl = headImgUrl;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof EvaPersonBean) {
			EvaPersonBean tempObj = (EvaPersonBean) o;
			return this.id.equals(tempObj.getId());
		}
		return super.equals(o);
	}
}
