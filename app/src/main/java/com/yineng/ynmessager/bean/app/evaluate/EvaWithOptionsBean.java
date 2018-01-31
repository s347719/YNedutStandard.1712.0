//***************************************************************
//*    2015-9-25  上午11:32:19
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.bean.app.evaluate;

import java.util.List;

/**
 * 带选项的题目(包含单选+打分、多选+打分、单选+打分+文本输入 三种类型的题目)
 * @author 胡毅
 *
 */
public class EvaWithOptionsBean extends BaseEvaluateBean {

	/**
	 * 选项
	 */
	private List<BaseEvaluateOptionsBean> options;
	
	/**
	 * 意见建议,在单选+文本输入的题目中出现
	 */
	private String suggestion;
	
	/**
	 * 最大分值
	 */
	private String maxScore;
	
	/**
	 * 最小分值
	 */
	private String minScore;

	public List<BaseEvaluateOptionsBean> getOptions() {
		return options;
	}

	public void setOptions(List<BaseEvaluateOptionsBean> options) {
		this.options = options;
	}

	public String getSuggestion() {
		return suggestion;
	}

	public void setSuggestion(String suggestion) {
		this.suggestion = suggestion;
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
	
}
