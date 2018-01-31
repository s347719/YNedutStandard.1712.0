//***************************************************************
//*    2015-9-22  下午4:58:47
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.bean.app.evaluate;

/**
 * 答案选项类
 * @author 胡毅
 * 
 */
public class BaseEvaluateOptionsBean {
	/**
	 * 每个选项的内容
	 */
	private String content;

	/**
	 * 每个选项的最大分值
	 */
	private String maxScore;

	/**
	 * 每个选项的最小分值
	 */
	private String minScore;
	
	private boolean isSelected;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(String maxScore) {
		this.maxScore = maxScore;
	}

	public String getMinScore() {
		return minScore;
	}

	public void setMinScore(String minScore) {
		this.minScore = minScore;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
}
