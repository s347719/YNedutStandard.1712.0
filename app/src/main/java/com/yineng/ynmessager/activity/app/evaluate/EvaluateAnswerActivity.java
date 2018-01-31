//***************************************************************
//*    2015-9-22  下午5:08:06
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.activity.app.evaluate;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.app.AppBaseActivity;
import com.yineng.ynmessager.bean.app.evaluate.BaseEvaluateBean;
import com.yineng.ynmessager.bean.app.evaluate.BaseEvaluateOptionsBean;
import com.yineng.ynmessager.bean.app.evaluate.EvaAnswerBean;
import com.yineng.ynmessager.bean.app.evaluate.EvaNoOptionsBean;
import com.yineng.ynmessager.bean.app.evaluate.EvaParam;
import com.yineng.ynmessager.bean.app.evaluate.EvaPersonBean;
import com.yineng.ynmessager.bean.app.evaluate.EvaSubmitBean;
import com.yineng.ynmessager.bean.app.evaluate.EvaWithOptionsBean;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.view.dialog.EvaluateSubmitScoreDialog;
import com.yineng.ynmessager.view.dialog.EvaluateSubmitScoreDialog.OnSubmitScoreListener;
import com.yineng.ynmessager.view.dialog.SubmitDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 胡毅 答题的ACTIVITY
 */
public class EvaluateAnswerActivity extends AppBaseActivity implements
        OnClickListener {

    private List<BaseEvaluateBean> mQuesObjects = new ArrayList<>();
    private TextView mTopicViewTV;
    private TextView mTopIndexViewTV;
    private TextView mEvaluatePreQuesTV;
    private TextView mEvaluateNextQuesTV;
    private ScrollView mEvaluateQuesViewSV;
    private View mRootContentView;
    private int mQuesIndex = 0;
    private EvaNoOptionView mEvaNoOptionView;
    private EvaWithOptionView mEvaWithOptionView;
    private EvaluateSubmitScoreDialog mEvaSubmitDialog;

    private float mMyAnswerScore = 0;

    private int mMyAnswerCount = 0;
    private String mPlanId;
    private EvaPersonBean mEvaPersonObject;
    private String mQuesUrl;
    private SubmitDialog mEvaSubmitStatus;
    private boolean mSubmitScoreIng = false;
    private AlertDialog mFinishActivityDialog; // 退出界面确认对话框

    private EvaSubmitBean mEvaSubmitBean = new EvaSubmitBean();

    private Handler mHandler = new Handler();
    /**
     * 提交成绩后，自动关闭当前界面
     */
    private Runnable mDismissFinishRunable = new Runnable() {

        @Override
        public void run() {
            mEvaSubmitStatus.dismiss();
            mEvaPersonObject.setEvaluateStatus(1);
            Intent intent = new Intent();
            intent.putExtra("evaedUser", mEvaPersonObject);
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    /**
     * 强制ScrollView滚动到顶部
     */
    private Runnable mScrollTopRunable = new Runnable() {
        @Override
        public void run() {
            mEvaluateQuesViewSV.fullScroll(ScrollView.FOCUS_UP);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRootContentView = setAppContentView(R.layout.activity_app_evaluate_answer);
        initData();
        initView();
        initViewListener();
    }


    /**
     *
     */
    private void initData() {
        mPlanId = getIntent().getStringExtra("planId");
        mEvaPersonObject = (EvaPersonBean) getIntent().getSerializableExtra("evaUser");
    }


    public void initView() {
        setTitleName(mEvaPersonObject != null ? mEvaPersonObject.getEvaluateName() : "被评人姓名");
//		setTitleRightVisible();
        mTopicViewTV = (TextView) findViewById(R.id.app_evaluate_ques_topic);
        mTopIndexViewTV = (TextView) findViewById(R.id.app_evaluate_ques_index);
        mEvaluatePreQuesTV = (TextView) findViewById(R.id.app_evaluate_pre_ques);
        mEvaluateNextQuesTV = (TextView) findViewById(R.id.app_evaluate_next_ques);
        mEvaluateQuesViewSV = (ScrollView) findViewById(R.id.app_evaluate_ques_view);
        mEvaSubmitDialog = new EvaluateSubmitScoreDialog(EvaluateAnswerActivity.this);
        mEvaSubmitStatus = new SubmitDialog(EvaluateAnswerActivity.this);
        AlertDialog.Builder builder = new Builder(this);
        builder.setTitle(R.string.common_id_past_title);
        builder.setMessage(R.string.eva_back_dialog_msg);
        builder.setPositiveButton(R.string.main_confirm, new Dialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton(R.string.setting_cancel, null);
        mFinishActivityDialog = builder.create();

    }

    public void initViewListener() {
        mEvaluatePreQuesTV.setOnClickListener(this);
        mEvaluateNextQuesTV.setOnClickListener(this);
        mEvaSubmitDialog.setOnSubmitScoreListener(new OnSubmitScoreListener() {

            @Override
            public void onSubmitScore(View v) {
                mEvaSubmitDialog.dismiss();
                //提交成绩
                submitMyScoreTask();
            }
        });
        mEvaluateQuesViewSV.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mEvaluateQuesViewSV.requestFocus();
                return false;
            }
        });
        loadServiceEvaQuestions();
    }

    /**
     * 加载服务器题目
     */
    private void loadServiceEvaQuestions() {
        mQuesUrl = mApplication.CONFIG_YNEDUT_V8_URL + EvaParam.APP_EVALUATE_LOAD_QUESTIONS_API;
        String evaluAtedUserId = mEvaPersonObject != null ? mEvaPersonObject.getId() : "";
        mQuesUrl = MessageFormat.format(mQuesUrl, mLastUserSP.getUserAccount(), mPlanId, evaluAtedUserId, 0, mApplication.mAppTokenStr);
        L.v("eva mQuesUrl == " + mQuesUrl);
//		loadDataTask(mQuesUrl, true);
        loadServiceData(mQuesUrl);
    }


    @Override
    public void titleRightClick(View v) {
        if (!mEvaSubmitDialog.isShowing()) {
            showEvaSubmitDialog();
        }
    }

    /**
     * 显示提交对话框
     */
    private void showEvaSubmitDialog() {
        int requiredNotCompletedCount = 0;
        for (BaseEvaluateBean firstQuesObj : mQuesObjects) {
//            if (baseEvaluateBean.isRequired() && baseEvaluateBean.getScore() == 0.0F) {
//                ++requiredNotCompletedCount;
//            }
            EvaWithOptionsBean withOptionQues = null;
            EvaNoOptionsBean noOptionQuesObj = null;
            if (firstQuesObj.isRequired() && firstQuesObj.getScore() == 0.0F) {
                if (firstQuesObj instanceof EvaWithOptionsBean) {
                    withOptionQues = (EvaWithOptionsBean) firstQuesObj;
                    L.e("MSG", "1是否必评" + withOptionQues.isRequired() + ",分数" + withOptionQues.getScore());
                    if (withOptionQues.getMaxScore().equals("0") && withOptionQues.getSuggestion().length() > 0) {

                    } else {
                        ++requiredNotCompletedCount;
                        break;
                    }
                } else if (firstQuesObj instanceof EvaNoOptionsBean) {
                    noOptionQuesObj = (EvaNoOptionsBean) firstQuesObj;
                    L.e("MSG", "2是否必评" + noOptionQuesObj.isRequired() + ",分数" + noOptionQuesObj.getScore()
                            + ",意见" + noOptionQuesObj.getSuggestion() + ",最大分数" + noOptionQuesObj.getMaxScore());
                    if (noOptionQuesObj.getMaxScore().equals("0") && noOptionQuesObj.getSuggestion().length() > 0) {

                    } else {
                        ++requiredNotCompletedCount;
                        break;
                    }
                }
            }
        }
        if (requiredNotCompletedCount != 0) {
            Toast.makeText(EvaluateAnswerActivity.this, String.format("你有%d个必评题未评，请评完后再提交", requiredNotCompletedCount),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mMyAnswerScore = 0;
        mMyAnswerCount = 0;
        //清空提交对象列表
        mEvaSubmitBean.scoreRecordDetailVOList.clear();
        for (Object quesObj : mQuesObjects) {
            if (quesObj instanceof EvaNoOptionsBean) {
                EvaNoOptionsBean noOptionQuesObj = (EvaNoOptionsBean) quesObj;
                //累计分数
                if (noOptionQuesObj.getScore() != 0) {
                    mMyAnswerScore = mMyAnswerScore + noOptionQuesObj.getScore();
                    mMyAnswerCount++;
                }
            } else {
                EvaWithOptionsBean withOptionQues = (EvaWithOptionsBean) quesObj;
                //判断是否大于最小分数
                try {
                    float mMinScore = Float.parseFloat(withOptionQues.getMinScore());
                    if (withOptionQues.getScore() < mMinScore) {
                        ToastUtil.toastAlerMessageCenter(this, "不能小于" + mMinScore + "分", 400);
                        return;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return;
                }
                //累计分数
                if (withOptionQues.getScore() != 0) {
                    mMyAnswerScore = mMyAnswerScore + withOptionQues.getScore();
                    mMyAnswerCount++;
                }
            }
            addSubmitJsonArrayData(quesObj);
        }
        mEvaSubmitDialog.setDialogData(mMyAnswerScore, mMyAnswerCount, mQuesObjects.size());
        mEvaSubmitDialog.show();
    }


    @Override
    public void onPreTask() {
        if (mSubmitScoreIng) {//提交成绩
            mEvaSubmitStatus.show();
        } else {
            showLoadingView();
        }
    }

    @Override
    public Object onDoingTask(String resultJson) {
        if (mSubmitScoreIng) {
            try {
                JSONObject resultJsonObject = new JSONObject(resultJson);
                int status = resultJsonObject.optInt("status");
                if (status != 1) { //成功
                    mEvaSubmitStatus.showSubmitSuccessView();
                    mHandler.postDelayed(mDismissFinishRunable, 1000);
                } else {//失败
                    mEvaSubmitStatus.dismiss();
                    ToastUtil.toastAlerMessageBottom(EvaluateAnswerActivity.this, "提交失败", 700);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mEvaSubmitStatus.dismiss();
                ToastUtil.toastAlerMessageBottom(EvaluateAnswerActivity.this, "服务器返回的JSON异常", 700);
            }
            return null;
        }
        List<BaseEvaluateBean> quesObjects = new ArrayList<>();
        try {
//			resultJson = getResources().getString(R.string.test_eva);
            JSONArray jsArray = new JSONArray(resultJson);
            for (int i = 0; i < jsArray.length(); i++) {
                JSONObject quesJsObject = jsArray.getJSONObject(i);
                int type = quesJsObject.getInt("type");
                String id = quesJsObject.getString("id");
                String topicName = quesJsObject.getString("topicName");
                String question = quesJsObject.getString("question");
                String maxScore = quesJsObject.getString("maxScore");
                String minScore = quesJsObject.getString("minScore");
                boolean required = quesJsObject.getInt("must") != 0;

                switch (type) {
                    case 2:
                    case 3:
                    case 5:
                        EvaNoOptionsBean noOptionsBean = new EvaNoOptionsBean();
                        noOptionsBean.setId(id);
                        noOptionsBean.setTopicName(topicName);
                        noOptionsBean.setQuestion(question);
                        noOptionsBean.setMaxScore(maxScore);
                        noOptionsBean.setMinScore(minScore);
                        noOptionsBean.setType(type);
                        noOptionsBean.setRequired(required);
                        quesObjects.add(noOptionsBean);
                        break;
                    case 0:
                    case 1:
                    case 4:
                        EvaWithOptionsBean withOptionsBean = new EvaWithOptionsBean();
                        withOptionsBean.setId(id);
                        withOptionsBean.setTopicName(topicName);
                        withOptionsBean.setQuestion(question);
                        withOptionsBean.setType(type);
                        withOptionsBean.setMaxScore(maxScore);
                        withOptionsBean.setMinScore(minScore);
                        withOptionsBean.setRequired(required);
                        List<BaseEvaluateOptionsBean> options = new ArrayList<BaseEvaluateOptionsBean>();
                        JSONArray optionArray = quesJsObject
                                .getJSONArray("options");
                        for (int j = 0; j < optionArray.length(); j++) {
                            JSONObject optionObject = optionArray.getJSONObject(j);
                            String quesContent = optionObject.getString("content");
                            String optionMaxScore = optionObject
                                    .getString("maxScore");
                            String optionMinScore = optionObject
                                    .getString("minScore");
                            BaseEvaluateOptionsBean optionItem = new BaseEvaluateOptionsBean();
                            optionItem.setContent(quesContent);
                            optionItem.setMaxScore(optionMaxScore);
                            optionItem.setMinScore(optionMinScore);
                            options.add(optionItem);
                        }
                        withOptionsBean.setOptions(options);
                        quesObjects.add(withOptionsBean);
                        break;

                    default:
                        break;
                }
            }
        } catch (Exception e) {
            ToastUtil.toastAlerMessageBottom(EvaluateAnswerActivity.this, "JSON格式错误", 800);
        }
        this.mQuesObjects.addAll(quesObjects);
        return quesObjects;
    }

    @Override
    public void onDoneTask(Object result) {
        if (mSubmitScoreIng) {
            return;
        }
        //		mQuesObjects = (List<Object>) result;
        L.v("mQuesObjects == " + mQuesObjects.size());
//		dismissContentStatusView();
        if (mQuesObjects.size() > 0) {
            // 设置默认的Fragment
            setTitleRightVisible();
            setQuestionFragment(mQuesIndex);
        } else {
            showEmptyView("暂无题目!");
        }
    }

    @Override
    public void onFailedTask(int urlQuesStatusCode) {
        switch (urlQuesStatusCode) {
            case 2:
                //如果是提交时，因为其他原因失败
                if (mSubmitScoreIng) {
                    mEvaSubmitStatus.dismiss();
                    ToastUtil.toastAlerMessageBottom(EvaluateAnswerActivity.this, "提交失败，请稍后重试!", 700);
                    return;
                }
                showContentFailedView();
                break;
            case 3:
                //如果是提交时，因为其他原因失败
                if (mSubmitScoreIng) {
                    mEvaSubmitStatus.dismiss();
                    ToastUtil.toastAlerMessageBottom(EvaluateAnswerActivity.this, "网络异常，请检查重试!", 700);
                    return;
                }
                showContentFailedView("网络异常，请检查重试!");
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 上一题
            case R.id.app_evaluate_pre_ques:
                if (mQuesIndex == 0) {
                    return;
                }
                mQuesIndex--;
                setQuestionFragment(mQuesIndex);
                break;
            // 下一题
            case R.id.app_evaluate_next_ques:
                if (mQuesIndex == mQuesObjects.size() - 1) {
                    Toast.makeText(this, "已经是最后一题！", Toast.LENGTH_SHORT).show();
                    break;
                }

                BaseEvaluateBean firstQuesObj = mQuesObjects.get(mQuesIndex);
                EvaWithOptionsBean withOptionQues = null;
                EvaNoOptionsBean noOptionQuesObj = null;
                if (firstQuesObj.isRequired() && firstQuesObj.getScore() == 0.0F) {
                    if (firstQuesObj instanceof EvaWithOptionsBean) {
                        withOptionQues = (EvaWithOptionsBean) firstQuesObj;
                        L.e("MSG", "1是否必评" + withOptionQues.isRequired() + ",分数" + withOptionQues.getScore());
                        if (withOptionQues.getMaxScore().equals("0") && withOptionQues.getSuggestion().length() > 0) {

                        } else {
                            Toast.makeText(this, "此题为必评题，请为此题打分", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    } else if (firstQuesObj instanceof EvaNoOptionsBean) {
                        noOptionQuesObj = (EvaNoOptionsBean) firstQuesObj;
                        L.e("MSG", "2是否必评" + noOptionQuesObj.isRequired() + ",分数" + noOptionQuesObj.getScore()
                                + ",意见" + noOptionQuesObj.getSuggestion() + ",最大分数" + noOptionQuesObj.getMaxScore());
                        if (noOptionQuesObj.getMaxScore().equals("0") && noOptionQuesObj.getSuggestion().length() > 0) {
                        } else {
                            Toast.makeText(this, "此题为必评题，请为此题打分", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }

                setQuestionFragment(++mQuesIndex);
                break;
            default:
                break;
        }
    }

    /**
     * 切换题目
     *
     * @param i
     */
    private void setQuestionFragment(int i) {
        if (mQuesObjects.size() > 0) {
            //给题目序号增加颜色效果
            SpannableString indexStr = drawQuestionIndexColor((i + 1) + "/" + mQuesObjects.size());
            mTopIndexViewTV.setText(indexStr);

            BaseEvaluateBean firstQuesObj = mQuesObjects.get(i);
            mEvaluateQuesViewSV.removeAllViews();

            String topicName = firstQuesObj.getTopicName();
            if (firstQuesObj instanceof EvaNoOptionsBean) {
                EvaNoOptionsBean noOptionQuesObj = (EvaNoOptionsBean) firstQuesObj;
                mTopicViewTV.setText(topicName);
                if (mEvaNoOptionView == null) {
                    mEvaNoOptionView = new EvaNoOptionView(this);
                }
                mEvaluateQuesViewSV.addView(mEvaNoOptionView);
                mEvaNoOptionView.refreshView(noOptionQuesObj);
            } else if (firstQuesObj instanceof EvaWithOptionsBean) {
                EvaWithOptionsBean withOptionQues = (EvaWithOptionsBean) firstQuesObj;
                mTopicViewTV.setText(topicName);
                if (mEvaWithOptionView == null) {
                    mEvaWithOptionView = new EvaWithOptionView(this);
                }
                mEvaluateQuesViewSV.addView(mEvaWithOptionView);
                mEvaWithOptionView.refreshView(withOptionQues);
            }
            //在主线程加载完新题目时，在消息队列里面更新scrollView的位置
            mHandler.post(mScrollTopRunable);
        }
    }

    /**
     * 给字符串增加颜色
     *
     * @param indexStr 题目序号字符串
     * @return
     */
    public SpannableString drawQuestionIndexColor(String indexStr) {
        SpannableString spanString = new SpannableString(indexStr);
        spanString.setSpan(
                new ForegroundColorSpan(Color.parseColor("#12b5b0")), 0,
                indexStr.indexOf("/"), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spanString;
    }

    @Override
    public void backView() {
        if (isTitleRightVisible()) {
            mFinishActivityDialog.show();
        } else {
            finish();
        }
    }


//	@Override
//	public void onScoreChange(int index) {
//		if (index != mQuesIndex) {
//			return;
//		}
    //		Object nowQuesObj = mQuesObjects.get(mQuesIndex);
//		if (nowQuesObj instanceof EvaNoOptionsBean) {
    //			mQuesObjects.set(mQuesIndex, mInputScoreFgment.getmEvaNoOptionsBean());
//		} else if (nowQuesObj instanceof EvaWithOptionsBean) {
    //			mQuesObjects.set(mQuesIndex, mOptionScoreFgment.getmEvaWithOptionsBean());
//		}
//	}

    /**
     * 提交成绩
     */
    private void submitMyScoreTask() {

        mSubmitScoreIng = true;
        String submitUrl = mApplication.CONFIG_YNEDUT_V8_URL + EvaParam.APP_EVALUATE_SUBMIT_API;
        String jsonStr = formatSubmitJson();
        submitUrl = MessageFormat.format(submitUrl, mApplication.mAppTokenStr);
        L.v("eva submitUrl == " + submitUrl);
        Map<String,String> params = new HashMap<>();
        params.put("JsonData", jsonStr);
        OKHttpCustomUtils.get(submitUrl,params,jsonObjectCallBack);

    }


    /**
     * 格式化json字符串
     */
    private String formatSubmitJson() {
        mEvaSubmitBean.setWjTeachingEvaluationPlanId(mPlanId);
        mEvaSubmitBean.setAttendEvalUserId(mLastUserSP.getUserAccount());
        mEvaSubmitBean.setEvaluAtedUserId(mEvaPersonObject != null ? mEvaPersonObject.getId() : "");
        mEvaSubmitBean.setTotalScore(mMyAnswerScore);
        mEvaSubmitBean.setOperateType("0");
        mEvaSubmitBean.setUserType(String.valueOf(LastLoginUserSP.getUserType(getApplicationContext())));
        return JSON.toJSONString(mEvaSubmitBean);
    }


    /**
     * 给用于提交的json中的list添加数据
     *
     * @param quesObj
     */
    private void addSubmitJsonArrayData(Object quesObj) {
        EvaAnswerBean tempEvaAnswerBean = new EvaAnswerBean();
        //给json序列化对象添加数据
        tempEvaAnswerBean.setAttendUserId(mLastUserSP.getUserAccount());
        if (quesObj instanceof EvaNoOptionsBean) {
            EvaNoOptionsBean noOptionQuesObj = (EvaNoOptionsBean) quesObj;
            //给json序列化对象添加数据
            tempEvaAnswerBean.setTopicId(noOptionQuesObj.getId());
            tempEvaAnswerBean.setScore(noOptionQuesObj.getScore() + "");
            tempEvaAnswerBean.setAdvice(noOptionQuesObj.getSuggestion() != null ? noOptionQuesObj.getSuggestion() : "");
        } else {
            EvaWithOptionsBean withOptionQues = (EvaWithOptionsBean) quesObj;
            //给json序列化对象添加数据
            tempEvaAnswerBean.setTopicId(withOptionQues.getId());
            tempEvaAnswerBean.setScore(withOptionQues.getScore() + "");
            tempEvaAnswerBean.setAdvice(withOptionQues.getSuggestion() != null ? withOptionQues.getSuggestion() : "");
        }
        mEvaSubmitBean.scoreRecordDetailVOList.add(tempEvaAnswerBean);
    }

    @Override
    public void onReloadClick(View v) {
        loadServiceEvaQuestions();
    }

}
