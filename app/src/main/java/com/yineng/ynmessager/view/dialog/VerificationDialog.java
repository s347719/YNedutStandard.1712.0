package com.yineng.ynmessager.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.sdk.android.ams.common.util.StringUtil;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.LoginActivity;
import com.yineng.ynmessager.util.CodeUtil;
import com.yineng.ynmessager.util.InputUtil;
import com.yineng.ynmessager.util.ToastUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yhu on 2017/9/11.
 * 验证码弹窗
 */

public class VerificationDialog extends Dialog implements View.OnClickListener, TextWatcher {

    private Context mContext;
    private ImageView verification_code_img;
    private TextView first_edit, second_edit, third_edit, four_edit;
    private TextView dialog_cancel;
    private EditText dialog_edit;
    private String verCode = "";
    private LinkedList<TextView> textViews = new LinkedList<>();

    public interface VerificationListener {
        void verificationSuccess();
    }

    private VerificationListener mVerificationListener;

    public void setmVerificationListener(VerificationListener mVerificationListener) {
        this.mVerificationListener = mVerificationListener;
    }

    public VerificationDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public VerificationDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dialog_verification);
        setCanceledOnTouchOutside(false);
        initView();
    }

    /**
     * 初始化弹窗
     */
    private void initView() {
        verification_code_img = (ImageView) findViewById(R.id.verification_code_img);
        first_edit = (TextView) findViewById(R.id.first_edit);
        second_edit = (TextView) findViewById(R.id.second_edit);
        third_edit = (TextView) findViewById(R.id.third_edit);
        four_edit = (TextView) findViewById(R.id.four_edit);
        dialog_edit = (EditText) findViewById(R.id.dialog_edit);
        dialog_cancel = (TextView) findViewById(R.id.dialog_cancel);


        verification_code_img.setOnClickListener(this);
        dialog_edit.addTextChangedListener(this);
        dialog_edit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        //禁止复制粘贴
        dialog_edit.setLongClickable(false);

        dialog_edit.setCustomSelectionActionModeCallback(banLongSelected);

        textViews.add(first_edit);
        textViews.add(second_edit);
        textViews.add(third_edit);
        textViews.add(four_edit);

        for (TextView textView:textViews){
            textView.setLongClickable(false);
            textView.setCustomSelectionActionModeCallback(banLongSelected);
        }

        //关闭弹窗
        dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //隐藏键盘
                InputUtil.HideKeyboard(dialog_edit);
                dismiss();
            }
        });
        //设置textview样式
        for (final TextView textView : textViews) {
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showKeyBoard();
                }
            });
            textView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (StringUtil.isEmpty(textView.getText().toString())) {
                        textView.setBackgroundResource(R.drawable.login_activity_verification_press_background);
                    } else {
                        textView.setBackgroundResource(R.drawable.login_activity_verification_nor_background);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

        showKeyBoard();
        setVerificationCodeImg();
    }

    private void showKeyBoard() {
        //延迟弹出键盘
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog_edit.setFocusable(true);
                dialog_edit.setFocusableInTouchMode(true);
                dialog_edit.requestFocus();
                InputUtil.ShowKeyboard(dialog_edit);
            }
        }, 100);
    }

    //禁止edittext复制粘贴
    private  ActionMode.Callback banLongSelected = new ActionMode.Callback() {

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }


        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }
    };

    /**
     * 设置验证码
     */
    private void setVerificationCodeImg() {
        CodeUtil codeUtil = CodeUtil.getInstance();
        Bitmap bitmap = codeUtil.createBitmap();
        verCode = codeUtil.getCode();
        verification_code_img.setImageBitmap(bitmap);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.verification_code_img:
                setVerificationCodeImg();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    private String lastCode = "";

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //禁止输入空格和回车
        if (s.toString().contains(" ")) {
            String[] str = s.toString().split(" ");
            String str1 = "";
            for (int i = 0; i < str.length; i++) {
                str1 += str[i];
            }
            dialog_edit.setText(str1);
            dialog_edit.setSelection(start);
        }
        //1.判断现在输入框到哪一个了
        String inputTxt = dialog_edit.getText().toString();
        int position = inputTxt.length();
        if (lastCode.length() > inputTxt.length()) {
            //删除字符
            cleanTextViewStyle(position);
        } else if (inputTxt.length() > lastCode.length()) {
            //新增字符
            fillTextViewStyle(position);
        }
        lastCode = inputTxt;
    }


    @Override
    public void afterTextChanged(Editable s) {

    }

    /**
     * 更改样式
     *
     * @param position
     */

    private void fillTextViewStyle(int position) {
        String code = dialog_edit.getText().toString();
        switch (position) {
            case 1:
                first_edit.setText(code.substring(0, 1));
                break;
            case 2:
                first_edit.setText(code.substring(0, 1));
                second_edit.setText(code.substring(1, 2));
                break;
            case 3:
                first_edit.setText(code.substring(0, 1));
                second_edit.setText(code.substring(1, 2));
                third_edit.setText(code.substring(2, 3));
                break;
            case 4:
                first_edit.setText(code.substring(0, 1));
                second_edit.setText(code.substring(1, 2));
                third_edit.setText(code.substring(2, 3));
                finishInput();
                break;
        }
    }

    private void cleanTextViewStyle(int position) {
        switch (position) {
            case 2:
                third_edit.setText("");
                break;
            case 1:
                second_edit.setText("");
                break;
            case 0:
                first_edit.setText("");
                break;
        }
    }

    /**
     * 判断text获取焦点
     */
    private void finishInput() {
        String resultTxt = dialog_edit.getText().toString();
        four_edit.setBackgroundResource(R.drawable.login_activity_verification_nor_background);
        four_edit.setText(dialog_edit.getText().toString().substring(3, 4));
        if (!CodeUtil.ignoreCaseEquals(resultTxt, verCode)) {
            ToastUtil.toastAlerMessageiconTop(mContext, getLayoutInflater(),
                    mContext.getResources().getString(R.string.login_img_verification_error), 500);
            //延迟清空输入框
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    for (TextView textView : textViews) {
                        textView.setText("");
                        dialog_edit.setText("");
                        lastCode = "";
                    }
                    setVerificationCodeImg();
                }
            }, 300);

        } else {
            //延迟退出
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mVerificationListener != null) {
                        mVerificationListener.verificationSuccess();
                    }
                    //隐藏键盘
                    InputUtil.HideKeyboard(dialog_edit);
                    dismiss();
                }
            }, 100);

        }
    }


}
