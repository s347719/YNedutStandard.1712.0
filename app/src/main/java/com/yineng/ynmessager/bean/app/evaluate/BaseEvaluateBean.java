//***************************************************************
//*    2015-9-22  下午4:20:17
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.bean.app.evaluate;

/**
 * 答题-题目数据结构模型
 * @author 胡毅
 * @des 题目类型：0：单选题+打分  1：多选题+打分  2： 直接打分 3：文本输入  4：单选+文本输入+打分 5：打分+文本输入
 */
public class BaseEvaluateBean {
	
	/**
	 * 题目id
	 */
	private String id;
	
	/**
	 * 该题所属的大题
	 */
	private String topicName;
	
	/**
	 * 题目问题，如：你觉得改老师要授课过程中，是否有违规行为?（2~10分）
	 */
	private String question;
	
	/**
	 * 每题的得分
	 */
	private float score = 0.0F;
	
	/**
	 * 题目类型：0：单选题+打分  1：多选题+打分  2： 直接打分 3：文本输入  4：单选+文本输入+打分 5：打分+文本输入
	 */
	private int type = -1;

    /**
     * 是否是必评题目
     */
    private boolean required;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
