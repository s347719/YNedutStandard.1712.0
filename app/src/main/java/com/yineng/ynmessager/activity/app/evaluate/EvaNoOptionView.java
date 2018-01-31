//***************************************************************
//*    2015-10-9  上午11:08:56
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.activity.app.evaluate;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.app.evaluate.EvaNoOptionsBean;
import com.yineng.ynmessager.bean.app.evaluate.EvaParam;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.ToastUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.lang3.StringUtils;

/**
 * @author 胡毅
 */
public class EvaNoOptionView extends LinearLayout implements OnClickListener {
    private static final String TAG = "EvaNoOptionView";
    private static final BigDecimal OPERATION_STEP = new BigDecimal("0.1");
    private static final float NUMBER_SETP = 0.1f;
    /**
     * 没有选项的题目实例
     */
    private EvaNoOptionsBean mEvaNoOptionsBean;
    private LinearLayout mOptionsRootContainerLL;
    private TextView mQuesContentViewTV;
    private RelativeLayout mAdjustScoreContainViewRl;
    private ImageView mMinuteScoreViewIV;
    private ImageView mPlusScoreViewIV;
    private EditText mGetScoreViewET;
    private EditText mSuggestViewET;
    private Context mContext;
    /**
     * 当前题目的索引
     */
    private int mQuesIndex;
    private float mMinScore;
    private float mMaxScore;
    /**
     * 意见或建议的文本监听
     */
    private TextWatcher mSuggestEditChangeListener = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            L.v("EvaNoOptionView 意见 afterTextChanged == " + s.toString());
            if (s.length() > 0) {
                //文本输入题目,当编辑框有文本输入时，直接满分
                if (mEvaNoOptionsBean.getType() == EvaParam.EVA_QUES_JUST_ADVICE) {
                    mEvaNoOptionsBean.setScore(mMaxScore);
                }
            } else {
                //文本输入题目,当编辑框没有文本输入时，记0分
                if (mEvaNoOptionsBean.getType() == EvaParam.EVA_QUES_JUST_ADVICE) {
                    setQuestionScore(0);
                }
            }
            mEvaNoOptionsBean.setSuggestion(s.toString());
        }
    };
    private View mScoreDivierView;

//	private TextView mGetScoreTagTV;

    public EvaNoOptionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public EvaNoOptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public EvaNoOptionView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.fragment_evaluate_answer_question, this);
        mContext = context;
        initView();
    }

    public void setEvaNoOptionsBean(EvaNoOptionsBean mEvaNoOptionsBean) {
        this.mEvaNoOptionsBean = mEvaNoOptionsBean;
    }

    public void setEvaNoOptionsBean(int index, EvaNoOptionsBean mEvaNoOptionsBean) {
        this.mQuesIndex = index;
        this.mEvaNoOptionsBean = mEvaNoOptionsBean;
    }

    /**
     *
     */
    private void initView() {
        findViews();
        initViewListener();
    }

    /**
     *
     */
    private void findViews() {
        mOptionsRootContainerLL = (LinearLayout) findViewById(R.id.app_evaluate_ques_root);
        mQuesContentViewTV = (TextView) findViewById(R.id.app_evaluate_ques_content);
        mScoreDivierView = findViewById(R.id.app_evaluate_common_divider);
        mAdjustScoreContainViewRl = (RelativeLayout) findViewById(R.id.app_evaluate_adjust_score_view);
        mMinuteScoreViewIV = (ImageView) findViewById(R.id.app_evaluate_ques_minute_score);
        mPlusScoreViewIV = (ImageView) findViewById(R.id.app_evaluate_ques_plus_score);
        mGetScoreViewET = (EditText) findViewById(R.id.app_evaluate_ques_get_point);
//		mGetScoreTagTV = (TextView) findViewById(R.id.app_evaluate_ques_score_tag);
        mSuggestViewET = (EditText) findViewById(R.id.app_evaluate_ques_suggestion);
    }

    /**
     * 数字监听判断是否大于最大范围
     */
    private TextWatcher mGetScoreEditChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //第一个字符不能为小数点
            if (s.toString().isEmpty()|| (s.length()==1&&s.toString().equals("."))) return;
            count = 0;
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i)=='.'){
                    count=count+1;
                }
            }
            if (count>1){
                ToastUtil.toastAlerMessage(getContext(),"只允许一个小数点"+mMaxScore,300);
            }
            float num = Float.parseFloat(s.toString());
            if (start >= 0) {//从一输入就开始判断，
                if (mMinScore != -1 && mMaxScore != -1) {
                    try {
                        //判断当前edittext中的数字(可能一开始Edittext中有数字)是否大于max
                        if (num > mMaxScore) {
                            s = String.valueOf(mMaxScore);//如果大于max，则内容为max
                            mGetScoreViewET.setText(s);
                            mGetScoreViewET.setSelection(s.length());
                            mEvaNoOptionsBean.setScore(mMaxScore);
                            ToastUtil.toastAlerMessage(getContext(),"分数不能大于"+mMaxScore,300);
                        } else {
                            mEvaNoOptionsBean.setScore(num);
                        }
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "==" + e.toString());
                    }
                    //edittext中的数字在max和min之间，则不做处理，正常显示即可。
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };


    /**
     * 初始化监听器
     */
    private void initViewListener() {
        // 加减分按钮的监听
        mMinuteScoreViewIV.setOnClickListener(this);
        mPlusScoreViewIV.setOnClickListener(this);
//		mGetScoreTagTV.setOnClickListener(this);
        mGetScoreViewET.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        mGetScoreViewET.addTextChangedListener(mGetScoreEditChangeListener);
        mGetScoreViewET.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String text = mGetScoreViewET.getText().toString();
                String roundedNumTxt = null;
                float roundedNum = 0f;
                try {
                    roundedNumTxt = new BigDecimal(text)
                            .setScale(1, RoundingMode.FLOOR)
                            .toString();
                    roundedNum = Float.parseFloat(roundedNumTxt);
                } catch (Exception e) {
                    e.printStackTrace();
                    roundedNum = 0.0f;
                }
                if (!hasFocus) {
                    if (roundedNum < mMinScore) {
                        roundedNum = mMinScore;
                    } else if (roundedNum > mMaxScore) {
                        roundedNum = mMaxScore;
                    } else {
                        try {
                            roundedNum = Float.parseFloat(roundedNumTxt);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mGetScoreViewET.setText(String.valueOf(roundedNum));
                }
            }
        });

        mOptionsRootContainerLL.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mOptionsRootContainerLL.requestFocus();
                return false;
            }
        });
        // mSuggestViewET.removeTextChangedListener(mSuggestEditChangeListener);
        mSuggestViewET.addTextChangedListener(mSuggestEditChangeListener);
    }

    @Override
    public void onClick(View v) {
        if (StringUtils.isEmpty(mGetScoreViewET.getText())) {
            mGetScoreViewET.setText(String.valueOf(mMinScore));
        }
        float score = Float.parseFloat(mGetScoreViewET.getText().toString());
        switch (v.getId()) {
            case R.id.app_evaluate_ques_minute_score:
                if (score <= mMinScore) {
                    ToastUtil.toastAlerMessageCenter(mContext, "不能小于" + mMinScore + "分", 500);
                } else {
                    mGetScoreViewET.setText(
                            new BigDecimal(
                                    String.valueOf(score))
                                    .subtract(OPERATION_STEP)
                                    .toString());
                }
                break;
            case R.id.app_evaluate_ques_plus_score:
                if (score >= mMaxScore) {
                    ToastUtil.toastAlerMessageCenter(mContext, "不能大于" + mMaxScore + "分", 500);
                } else {
                    mGetScoreViewET.setText(
                            new BigDecimal(
                                    String.valueOf(score))
                                    .add(OPERATION_STEP)
                                    .toString());
                }
                break;
            default:
                break;
        }
    }

    /**
     * 设置该题分数
     */
    public void setQuestionScore(float score) {
//		if (mEvaNoOptionsBean.getScore() == 0) {
//			//设置这道题目默认的分数值为最大分数
//			score = Integer.valueOf(mEvaNoOptionsBean.getMaxScore());
//		} else if (score > Integer.valueOf(mEvaNoOptionsBean.getMaxScore())) {
//			score = Integer.valueOf(mEvaNoOptionsBean.getMaxScore());
//		} else if (score < Integer.valueOf(mEvaNoOptionsBean.getMinScore())) {
//			score = Integer.valueOf(mEvaNoOptionsBean.getMinScore());
//		}
        mEvaNoOptionsBean.setScore(score);
        mGetScoreViewET.setText(String.valueOf(score));
    }

    /**
     * 刷新当前界面
     *
     * @param noOptionQuesObj
     */
    public void refreshView(EvaNoOptionsBean noOptionQuesObj) {
        this.mEvaNoOptionsBean = noOptionQuesObj;
        mMaxScore = Float.valueOf(mEvaNoOptionsBean.getMaxScore());
        mMinScore = Float.valueOf(mEvaNoOptionsBean.getMinScore());

        switch (mEvaNoOptionsBean.getType()) {
            case EvaParam.EVA_QUES_INPUT_SCORE://直接打分
                mAdjustScoreContainViewRl.setVisibility(View.VISIBLE);
                mSuggestViewET.setVisibility(View.GONE);
                mScoreDivierView.setVisibility(View.VISIBLE);
                break;
            case EvaParam.EVA_QUES_JUST_ADVICE://文本输入
                mAdjustScoreContainViewRl.setVisibility(View.GONE);
                mSuggestViewET.setVisibility(View.VISIBLE);
                mScoreDivierView.setVisibility(View.GONE);
                break;
            case EvaParam.EVA_QUES_INPUT_SCORE_ADVICE://打分+文本输入
                mAdjustScoreContainViewRl.setVisibility(View.VISIBLE);
                mSuggestViewET.setVisibility(View.VISIBLE);
                mScoreDivierView.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
        initSuggestView();
    }

    /**
     *
     */
    private void initSuggestView() {
        // 问题内容
        String question = mEvaNoOptionsBean.getQuestion();
        boolean required = mEvaNoOptionsBean.isRequired();

        if (required) {
            question = "必评  " + question;
            Spannable spannable = new SpannableString(question);
            spannable.setSpan(new TextAppearanceSpan(null, Typeface.NORMAL, 0,
                            new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.WHITE}), null), 0, 2,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new BackgroundColorSpan(
                            ResourcesCompat.getColor(getResources(), R.color.orange, getContext().getTheme())), 0, 2,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            mQuesContentViewTV.setText(spannable);
        } else {
            mQuesContentViewTV.setText(question);
        }

        L.v("mEvaNoOptionsBean.getSuggestion() == " + mEvaNoOptionsBean.getSuggestion());
        // 意见或建议
        mSuggestViewET.setText(mEvaNoOptionsBean.getSuggestion() == null ? ""
                : mEvaNoOptionsBean.getSuggestion());

        // 设置默认分数
//		setQuestionScore(mEvaNoOptionsBean.getScore());
        // 设置默认分数
        float score = mEvaNoOptionsBean.getScore();
        if (score == 0.0F) {
            mGetScoreViewET.setText(StringUtils.EMPTY);
        } else {
            mGetScoreViewET.setText(String.valueOf(score));
        }

        L.v("mEvaNoOptionsBean.getScore() == " + mEvaNoOptionsBean.getScore());
    }


}
