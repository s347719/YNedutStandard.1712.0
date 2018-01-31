//***************************************************************
//*    2015-10-9  上午11:40:49
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
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.text.style.BackgroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.app.evaluate.BaseEvaluateOptionsBean;
import com.yineng.ynmessager.bean.app.evaluate.EvaParam;
import com.yineng.ynmessager.bean.app.evaluate.EvaWithOptionsBean;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.view.MyRadioGroup;
import com.yineng.ynmessager.view.MyRadioGroup.OnCheckedChangeListener;
import com.yineng.ynmessager.view.OptionMultCheckBX;
import com.yineng.ynmessager.view.OptionMultCheckBX.optionCheckListener;
import com.yineng.ynmessager.view.OptionSingleRadioBT;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * @author 胡毅
 */
public class EvaWithOptionView extends LinearLayout implements OnClickListener {
    private static final String TAG = "EvaWithOptionView";
    private static final BigDecimal OPERATION_STEP = new BigDecimal("0.1");
    /**
     * 带选项的题目实例
     */
    private EvaWithOptionsBean mEvaWithOptionsBean;
    private TextView mQuesContentViewTV;
    private RelativeLayout mAdjustScoreContainViewRl;
    private EditText mGetScoreViewET;
    private EditText mSuggestViewET;
    private ImageView mMinuteScoreViewIV;
    private ImageView mPlusScoreViewIV;


    /**
     * 当前题目的索引
     */
    private int mQuesIndex;
    private View rootView;
    /**
     * 选项布局容器
     */
    private LinearLayout mOptionsContainerLL;
    private MyRadioGroup mSingleOptionRG;
    /**
     * 选项
     */
    private List<BaseEvaluateOptionsBean> mOptionList;

    /**
     * 可编辑或输入的最小分数
     */
    private float mMinScore = 0.0F;

    /**
     * 可编辑或输入的最大分数
     */
    private float mMaxScore = 0.0F;

    private NumberKeyListener mScoreEditNumberListener = new NumberKeyListener() {
        @Override
        public int getInputType() {
            return InputType.TYPE_CLASS_NUMBER;
        }

        @Override
        protected char[] getAcceptedChars() {
            char[] numbers = new char[]{'0', '1', '2', '3', '4', '5', '6',
                    '7', '8', '9'};
            return numbers;
        }
    };

    /**
     * 得分输入框的文本变化监听器
     */
    private TextWatcher mGetScoreEditChangeListener = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            //第一个字符不能为小数点
            if (s.toString().isEmpty()||s.length()==1&&s.toString().equals(".")) return;
            float num = Float.parseFloat(s.toString());
            if (start >= 0) {//从一输入就开始判断，
                if (mMinScore != -1 && mMaxScore != -1) {
                    try {
                        //判断当前edittext中的数字(可能一开始Edittext中有数字)是否大于max
                        if (num > mMaxScore) {
                            s = String.valueOf(mMaxScore);//如果大于max，则内容为max
                            mGetScoreViewET.setText(s);
                            mGetScoreViewET.setSelection(s.length());
                            mEvaWithOptionsBean.setScore(mMaxScore);
                            ToastUtil.toastAlerMessage(getContext(),"分数不能大于"+mMaxScore,300);
                        } else {
                            mEvaWithOptionsBean.setScore(num);
                        }
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "==" + e.toString());
                    }
                    //edittext中的数字在max和min之间，则不做处理，正常显示即可。
                    return;
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    private LinearLayout mOptionsRootContainerLL;
    /**
     * 多选题的选择项视图
     */
    private LinearLayout mMultOptionsContainerLL;
    /**
     * 意见或建议的文本监听
     */
    private TextWatcher mSuggestEditChangeListener = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            L.v("mEvaWithOptionsBean mSuggestViewET afterTextChanged == " + s.toString());
            mEvaWithOptionsBean.setSuggestion(s.toString());
        }
    };
    private Context mContext;
    /**
     * 是否是初始化已有选择的单选题目分数
     */
    private boolean isInitSingleCheckedScore = false;
    /**
     * 单选选项选中监听器
     */
    private OnCheckedChangeListener mSingleOptionCheckListener = new MyRadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(MyRadioGroup group, int checkedId) {
            L.v("checkedId == " + checkedId + " " + group.getTag());
            if (checkedId == -1) {
                return;
            }
            mGetScoreViewET.clearFocus();
            // 消除之前被选中的选项状态
            if (group.getTag() != null) {
                int checkedBefore = (Integer) group.getTag();
                updateRadioGroupSelector(group, checkedBefore, false);

            }
            // 更新被选中的选项状态
//			group.check(checkedId);
            group.setTag(checkedId);
            updateRadioGroupSelector(group, checkedId, true);

            updateSingleOptionScoreArea(checkedId);

        }
    };
    /**
     * 是否是初始化已有选择的多选题目分数
     */
    private boolean isInitMultCheckedScore = false;
    /**
     * 多选题的选项选择监听器
     */
    private optionCheckListener mMultOptionCheckListener = new optionCheckListener() {

        @Override
        public void onChecked(CompoundButton buttonView, boolean isChecked) {
            int checkedId = buttonView.getId();
            BaseEvaluateOptionsBean tempOption = mOptionList.get(checkedId);
            if (tempOption == null) {
                return;
            }
            tempOption.setSelected(isChecked);
            updateMultOptionScoreArea();
        }
    };
    //	private TextView mGetScoreTagTV;

    public EvaWithOptionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EvaWithOptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EvaWithOptionView(Context context) {
        super(context);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.fragment_evaluate_answer_question, this);
        initView();
    }

    /**
     * 设置该界面的对象数据
     */
    public void setmEvaNoOptionsBean(EvaWithOptionsBean mEvaWithOptionsBean) {
        this.mEvaWithOptionsBean = mEvaWithOptionsBean;
    }

    protected void updateRadioGroupSelector(MyRadioGroup group,
                                            int checkedId, boolean isChecked) {
        RadioButton optionBt = (RadioButton) group.findViewById(checkedId);
        View optionContainer = (View) optionBt.getParent();
        ImageView mSelectIV = (ImageView) optionContainer
                .findViewById(R.id.item_app_evaluate_option_bt);
        mSelectIV.setSelected(isChecked);
        optionBt.setChecked(isChecked);
        mOptionList.get(checkedId).setSelected(isChecked);
//		if (mGetScoreViewET.getKeyListener() == null) {
//			mGetScoreViewET.setKeyListener(mScoreEditNumberListener);
//		}
    }

    /**
     * 设置该界面的对象数据
     *
     * @param index
     * @param mEvaWithOptionsBean
     */
    public void setmEvaNoOptionsBean(int index,
                                     EvaWithOptionsBean mEvaWithOptionsBean) {
        this.mQuesIndex = index;
        this.mEvaWithOptionsBean = mEvaWithOptionsBean;
        // refreshView();
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
        mOptionsContainerLL = (LinearLayout) findViewById(R.id.app_evluate_options_container);
        mOptionsContainerLL.setVisibility(VISIBLE);
        mSingleOptionRG = (MyRadioGroup) findViewById(R.id.app_evaluate_single_options_rg);
        mMultOptionsContainerLL = (LinearLayout) findViewById(R.id.app_evaluate_mult_options_ll);
        mAdjustScoreContainViewRl = (RelativeLayout) findViewById(R.id.app_evaluate_adjust_score_view);
        mMinuteScoreViewIV = (ImageView) findViewById(R.id.app_evaluate_ques_minute_score);
        mPlusScoreViewIV = (ImageView) findViewById(R.id.app_evaluate_ques_plus_score);
        mGetScoreViewET = (EditText) findViewById(R.id.app_evaluate_ques_get_point);
//		mGetScoreTagTV = (TextView) findViewById(R.id.app_evaluate_ques_score_tag);
        mSuggestViewET = (EditText) findViewById(R.id.app_evaluate_ques_suggestion);

    }

    /**
     *
     */
    private void initViewListener() {
        mMinuteScoreViewIV.setOnClickListener(this);
        mPlusScoreViewIV.setOnClickListener(this);
        mAdjustScoreContainViewRl.setOnClickListener(this);
//		mGetScoreViewET.setKeyListener(null);
        mGetScoreViewET.setOnClickListener(this);
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
                    mEvaWithOptionsBean.setScore(roundedNum);
                }
            }
        });
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
            case R.id.app_evaluate_adjust_score_view:
//			if (mSingleOptionRG.getCheckedRadioButtonId() == -1) {
//				ToastUtil.toastAlerMessageBottom(mContext, "请先选择选项", 1000);
//				return;
//			}
//			if (mEvaWithOptionsBean.getScore() == 0) {
//				ToastUtil.toastAlerMessageBottom(mContext, "请先选择选项", 1000);
//				return;
//			}
                break;
            case R.id.app_evaluate_ques_get_point:
//			if (mSingleOptionRG.getCheckedRadioButtonId() == -1) {
//				ToastUtil.toastAlerMessageBottom(mContext, "请先选择选项", 1000);
//				return;
//			}
//			if (mEvaWithOptionsBean.getScore() == 0) {
//				ToastUtil.toastAlerMessageBottom(mContext, "请先选择选项", 1000);
//				return;
//			}
                break;
            default:
                break;
        }
    }

    /**
     * 刷新当前界面
     */
    public void refreshView(EvaWithOptionsBean withOptionQuesObj) {
        this.mEvaWithOptionsBean = withOptionQuesObj;
        resetViews();
        initCommonView();
        switch (mEvaWithOptionsBean.getType()) {
            case EvaParam.EVA_QUES_SINGLE_OPTION://单选题+打分
                mSingleOptionRG.setVisibility(View.VISIBLE);
                mAdjustScoreContainViewRl.setVisibility(View.VISIBLE);
                mSuggestViewET.setVisibility(View.GONE);
                initSingleOptionRgView();
                break;
            case EvaParam.EVA_QUES_MULT_OPTION://多选题+打分
                mSingleOptionRG.setVisibility(View.GONE);
                mAdjustScoreContainViewRl.setVisibility(View.VISIBLE);
                mSuggestViewET.setVisibility(View.GONE);
                mMultOptionsContainerLL.setVisibility(View.VISIBLE);
                initMultOptionCbView();
                break;
            case EvaParam.EVA_QUES_SINGLE_ADVICE://单选+文本输入+打分
                mSingleOptionRG.setVisibility(View.VISIBLE);
                mAdjustScoreContainViewRl.setVisibility(View.VISIBLE);
                mSuggestViewET.setVisibility(View.VISIBLE);
                initSingleOptionRgView();
                // 意见或建议
                mSuggestViewET.setText(mEvaWithOptionsBean.getSuggestion() == null ? "" : mEvaWithOptionsBean.getSuggestion());
                break;
            default:
                break;
        }
    }

    /**
     * 重置View
     */
    private void resetViews() {
        mMinScore = 0.0F;
        mMaxScore = 0.0F;
        mSingleOptionRG.removeAllViews();
//		mSingleOptionRG.clearCheck();
        mSingleOptionRG.setTag(null);
        mMultOptionsContainerLL.removeAllViews();
        mGetScoreViewET.clearFocus();
    }

    /**
     *
     */
    private void initCommonView() {
        mOptionList = mEvaWithOptionsBean.getOptions();
        // 问题内容
        String question = mEvaWithOptionsBean.getQuestion();
        boolean required = mEvaWithOptionsBean.isRequired();

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

        if (mEvaWithOptionsBean.getMaxScore().isEmpty() || mEvaWithOptionsBean.getMinScore().isEmpty()) {
            ToastUtil.toastAlerMessageBottom(mContext, "题目没有分数区间", 800);
            return;
        }
        //现在不根据题目的最大和最小分值来确定能填的分值范围
/*		mMaxScore = Float.valueOf(mEvaWithOptionsBean.getMaxScore());
        mMinScore = Float.valueOf(mEvaWithOptionsBean.getMinScore());*/

//		if (mEvaWithOptionsBean.getScore() == 0) {
//			int defaultScore = Integer.valueOf(mEvaWithOptionsBean.getMaxScore());
//			mEvaWithOptionsBean.setScore(defaultScore);
//		}
        mMaxScore = Float.valueOf(mEvaWithOptionsBean.getMaxScore());
        mMinScore = Float.valueOf(mEvaWithOptionsBean.getMinScore());

        // 设置默认分数
        float score = mEvaWithOptionsBean.getScore();
        if (score == 0.0F) {
            mGetScoreViewET.setText(StringUtils.EMPTY);
        } else {
            mGetScoreViewET.setText(String.valueOf(score));
        }
//		setQuestionScore(mEvaWithOptionsBean.getScore());
    }

    /**
     * 初始化单选选项界面和选项监听器
     */
    private void initSingleOptionRgView() {
        if (mOptionList != null) {
            for (int i = 0; i < mOptionList.size(); i++) {
                BaseEvaluateOptionsBean tempOption = mOptionList.get(i);
                //初始化每一个选项内容
                OptionSingleRadioBT singleRadioBT = initEachOneSingleOptionView(i, tempOption);
                mSingleOptionRG.addView(singleRadioBT);
            }
            initEachOneSingleOptionViewListener();
            for (int i = 0; i < mOptionList.size(); i++) {
                BaseEvaluateOptionsBean tempOption = mOptionList.get(i);
                if (tempOption.isSelected()) {
//					mSingleOptionRG.check(i);
                    isInitSingleCheckedScore = true;
                    OptionSingleRadioBT selectRB = (OptionSingleRadioBT) mSingleOptionRG.getChildAt(i);
                    selectRB.mSelectRB.setChecked(true);
                }
            }
        }
    }

    /**
     * 初始化每一个单选选项内容
     *
     * @param i
     * @param tempOption
     * @return
     */
    private OptionSingleRadioBT initEachOneSingleOptionView(int i, BaseEvaluateOptionsBean tempOption) {
        final OptionSingleRadioBT singleRadioBT = new OptionSingleRadioBT(mContext);
        singleRadioBT.mSelectRB.setId(i);
        SpannableStringBuilder spanBuilder = initEachOptionContent(tempOption);
        singleRadioBT.mSelectRB.setText(spanBuilder);
        return singleRadioBT;
    }

    /**
     * 初始化单选选项监听器
     */
    private void initEachOneSingleOptionViewListener() {
        mSingleOptionRG.setOnCheckedChangeListener(mSingleOptionCheckListener);
    }

    /**
     * 初始化多选题的选择项视图
     */
    private void initMultOptionCbView() {
        if (mOptionList != null) {
            isInitMultCheckedScore = true;
            for (int i = 0; i < mOptionList.size(); i++) {
                BaseEvaluateOptionsBean tempOption = mOptionList.get(i);
                //初始化每一个选项内容
                OptionMultCheckBX multCheckBT = initEachOneMultOptionView(i, tempOption);
                initEachOneMultOptionViewListener(multCheckBT);
                mMultOptionsContainerLL.addView(multCheckBT);
                if (tempOption.isSelected()) {
                    multCheckBT.setChecked(true);
                }
            }
            isInitMultCheckedScore = false;
        }
    }

    /**
     * 初始化每一个多选选项的视图
     *
     * @param i          索引，id
     * @param tempOption 选项数据对象
     * @return 一个选项的视图
     */
    private OptionMultCheckBX initEachOneMultOptionView(int i, BaseEvaluateOptionsBean tempOption) {
        final OptionMultCheckBX multOptionBT = new OptionMultCheckBX(mContext);
        multOptionBT.mCheckCB.setId(i);
        SpannableStringBuilder spanBuilder = initEachOptionContent(tempOption);
        multOptionBT.mCheckCB.setText(spanBuilder);
        return multOptionBT;
    }

    /**
     * 初始化每个多选按钮的监听器
     *
     * @param multCheckBT
     */
    private void initEachOneMultOptionViewListener(OptionMultCheckBX multCheckBT) {
        multCheckBT.setOnOptionCheckListener(mMultOptionCheckListener);
    }

    /**
     * 初始化每个选项的文本显示格式
     *
     * @param tempOption 选项数据对象
     * @return 格式化后的选项文本
     */
    public SpannableStringBuilder initEachOptionContent(BaseEvaluateOptionsBean tempOption) {
        //初始化选项内容格式
        ColorStateList redColors = ColorStateList.valueOf(Color
                .parseColor("#a0a0a0"));
        SpannableStringBuilder spanBuilder = new SpannableStringBuilder();
        spanBuilder.append(tempOption.getContent());
        spanBuilder.append("\n");
        spanBuilder.append(tempOption.getMinScore() + "分~");
        spanBuilder.append(tempOption.getMaxScore() + "分");
        // style 为0 即是正常的，还有Typeface.BOLD(粗体) Typeface.ITALIC(斜体)等
        // size 为0 即采用原始的正常的 size大小
        int contentSize = (int) getResources().getDimension(
                R.dimen.common_listItem_textSize);
        TextAppearanceSpan textStyle = new TextAppearanceSpan(null, 0, contentSize - 3,
                redColors, null);
        spanBuilder.setSpan(textStyle, tempOption.getContent().length(), spanBuilder
                .length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        return spanBuilder;
    }

    /**
     * 更新单选分数区间
     *
     * @param checkedId
     */
    protected void updateSingleOptionScoreArea(int checkedId) {
        BaseEvaluateOptionsBean optionScore = mOptionList.get(checkedId);
        float maxScore = Float.valueOf(optionScore.getMaxScore());
        float minScore = Float.valueOf(optionScore.getMinScore());

        mMaxScore = maxScore;
        mMinScore = minScore;
        mEvaWithOptionsBean.setMinScore(optionScore.getMinScore());


        //当选中选项时，不更新最大最小值区间
//		mMinScore = Integer.valueOf(optionScore.getMinScore());
        if (isInitSingleCheckedScore) {
            isInitSingleCheckedScore = false;
            return;
        }
        //当用户选中选项时，将选项的最高分作为默认值，变更此输入框的分数
        mGetScoreViewET.setText(String.valueOf(maxScore));
//		setQuestionScore(maxScore);
//		if (mMinScore <= mEvaWithOptionsBean.getScore() && mEvaWithOptionsBean.getScore() <= maxScore) {
//			setQuestionScore(mEvaWithOptionsBean.getScore());
//		} else {
//			setQuestionScore(maxScore);
//		}
    }

    /**
     * 更新多选分数区间
     */
    protected void updateMultOptionScoreArea() {
        float maxScore = 0.0F;
        float minScore = 0.0F;

        for (BaseEvaluateOptionsBean checkOption : mOptionList) {
            if (checkOption.isSelected()) {
                maxScore = new BigDecimal(checkOption.getMaxScore())
                        .add(new BigDecimal(String.valueOf(maxScore)))
                        .floatValue();

                minScore = new BigDecimal(checkOption.getMinScore())
                        .add(new BigDecimal(String.valueOf(minScore)))
                        .floatValue();
            }
        }

        mMaxScore = maxScore;
        mMinScore = minScore;
        mEvaWithOptionsBean.setMinScore(String.valueOf(minScore));

        L.i("isInitMultCheckedScore == " + isInitMultCheckedScore);
        if (isInitMultCheckedScore) {
            return;
        }
        if (maxScore == 0) {
            mEvaWithOptionsBean.setScore(0.0F);
            mGetScoreViewET.setText(StringUtils.EMPTY);
        } else {
            mGetScoreViewET.setText(String.valueOf(maxScore));
        }

//		setQuestionScore(maxScore);
//		if (mMinScore <= mEvaWithOptionsBean.getScore()
//				&& mEvaWithOptionsBean.getScore() <= maxScore) {
//			setQuestionScore(mEvaWithOptionsBean.getScore());
//		} else {
//			setQuestionScore(maxScore);
//		}
    }
}
