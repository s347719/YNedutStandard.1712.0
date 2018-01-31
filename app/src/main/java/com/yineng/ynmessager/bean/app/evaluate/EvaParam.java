//***************************************************************
//*    2015-10-14  上午9:51:46
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.bean.app.evaluate;

/**
 * @author 胡毅
 * 
 */
public class EvaParam {

    public static final int EVA_QUES_SINGLE_OPTION = 0;//单选题+打分;
    public static final int EVA_QUES_MULT_OPTION = 1;//多选题+打分
    public static final int EVA_QUES_INPUT_SCORE = 2;//直接打分
    public static final int EVA_QUES_JUST_ADVICE = 3;//文本输入;
    public static final int EVA_QUES_SINGLE_ADVICE = 4;//单选+文本输入+打分
    public static final int EVA_QUES_INPUT_SCORE_ADVICE = 5;//打分+文本输入
    /**
	 * 评教列表接口
	 */
	public static String APP_EVALUATE_LOAD_PLANS_API = "third/teachingplan/findTeachingEvaluationPlan.htm?userId={0}&pageNumber={1}&pageSize={2}&access_token={3}";
	/**
	 * 评教获取被评人接口
	 */
	public static String APP_EVALUATE_LOAD_PERSONS_API = "third/teachingplan/findEvaluatedInfo.htm?userId={0}&planId={1}&pageNumber={2}&pageSize={3}&type={4}&access_token={5}";
	/**
	 * 评教获取题目的接口
	 */
	public static String APP_EVALUATE_LOAD_QUESTIONS_API = "third/teachingplan/findWJQuestionnaireTeachingEvaluation.htm?attendEvalUserId={0}&id={1}&evaluAtedUserId={2}&operateType={3}&access_token={4}";
	/**
	 * 评教提交得分的接口
	 */
    public static String APP_EVALUATE_SUBMIT_API =
        "third/teachingplan/saveOrUpdateOnlineEvaluation.htm?access_token={0}";
}
