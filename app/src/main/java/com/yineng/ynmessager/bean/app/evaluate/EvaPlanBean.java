//***************************************************************
//*    2015-10-10  下午1:43:56
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.bean.app.evaluate;

import java.io.Serializable;

/**
 * @author 胡毅
 * 评教列表中的评教计划属性
 */
public class EvaPlanBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4505490216512264172L;

	/**
	 * 评教活动ID
	 */
	private String id;
	
	/**
	 * 活动开始时间(04/22 10:00)
	 */
	private String beginTimeString;
	
	/**
	 * 活动结束时间(04/24 11:00)
	 */
	private String endTimeString;
	
	/**
	 * 活动名称
	 */
	private String planName;
	
	/**
	 * 被评总人数
	 */
	private int evaluateCount;
	
	/**
	 * 已评总人数
	 */
	private int evaluatedCount;
	
	/**
	 * 活动倒计时(毫秒数)
	 */
	private long restTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBeginTimeString() {
		return beginTimeString;
	}

	public void setBeginTimeString(String beginTimeString) {
		this.beginTimeString = beginTimeString;
	}

	public String getEndTimeString() {
		return endTimeString;
	}

	public void setEndTimeString(String endTimeString) {
		this.endTimeString = endTimeString;
	}

	public String getPlanName() {
		return planName;
	}

	public void setPlanName(String planName) {
		this.planName = planName;
	}

	public int getEvaluateCount() {
		return evaluateCount;
	}

	public void setEvaluateCount(int evaluateCount) {
		this.evaluateCount = evaluateCount;
	}

	public int getEvaluatedCount() {
		return evaluatedCount;
	}

	public void setEvaluatedCount(int evaluatedCount) {
		this.evaluatedCount = evaluatedCount;
	}

	public long getRestTime() {
		return restTime;
	}

	public void setRestTime(long restTime) {
		this.restTime = restTime;
	}
	
}
