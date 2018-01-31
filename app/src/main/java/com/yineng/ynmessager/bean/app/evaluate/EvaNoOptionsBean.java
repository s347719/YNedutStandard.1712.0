//***************************************************************
//*    2015-9-25  上午11:19:14
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.bean.app.evaluate;

/**
 * 不带选项的题目 (包含直接打分、文本输入、打分+文本输入 三种类型的题目)
 * @author 胡毅
 *
 */
public class EvaNoOptionsBean extends BaseEvaluateBean {

	/**
	 * 最大分值
	 */
	private String maxScore;
	
	/**
	 * 最小分值
	 */
	private String minScore;
	
	/**
	 * 意见建议
	 */
	private String suggestion;

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

	public String getSuggestion() {
		return suggestion;
	}

	public void setSuggestion(String suggestion) {
		this.suggestion = suggestion;
	}
}
