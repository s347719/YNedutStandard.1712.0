//***************************************************************
//*    2015-10-19  下午2:00:22
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.bean.app.evaluate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 胡毅
 * 
 */
public class EvaSubmitBean {

	public List<EvaAnswerBean> scoreRecordDetailVOList = new ArrayList<EvaAnswerBean>();

	private String wjTeachingEvaluationPlanId;

	private String attendEvalUserId;

	private String evaluAtedUserId;

	private float totalScore;

	private String operateType;
	// private String wjEvaluatedPersonAndAttendEvaluationPersonScoreRecordId;
	private String userType;

	public List<EvaAnswerBean> getScoreRecordDetailVOList() {
		return scoreRecordDetailVOList;
	}

	public void setScoreRecordDetailVOList(
			List<EvaAnswerBean> scoreRecordDetailVOList) {
		this.scoreRecordDetailVOList = scoreRecordDetailVOList;
	}

	public String getWjTeachingEvaluationPlanId() {
		return wjTeachingEvaluationPlanId;
	}

	public void setWjTeachingEvaluationPlanId(String wjTeachingEvaluationPlanId) {
		this.wjTeachingEvaluationPlanId = wjTeachingEvaluationPlanId;
	}

	public String getAttendEvalUserId() {
		return attendEvalUserId;
	}

	public void setAttendEvalUserId(String attendEvalUserId) {
		this.attendEvalUserId = attendEvalUserId;
	}

	public String getEvaluAtedUserId() {
		return evaluAtedUserId;
	}

	public void setEvaluAtedUserId(String evaluAtedUserId) {
		this.evaluAtedUserId = evaluAtedUserId;
	}

	public float getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(float totalScore) {
		this.totalScore = totalScore;
	}

	public String getOperateType() {
		return operateType;
	}

	public void setOperateType(String operateType) {
		this.operateType = operateType;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

}
