package com.yineng.ynmessager.util;

import android.content.Context;
import android.view.LayoutInflater;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.Const;

/**
 * Created by yn on 2017/9/26.
 */

public class AppToastUtils {

    private static AppToastUtils instance;

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private AppToastUtils(Context mContext) {
        this.mContext = mContext;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public static synchronized AppToastUtils getInstance(Context mContext) {
        if (instance == null) {
            instance = new AppToastUtils(mContext);
        }
        return instance;
    }

    /**
     * 短信验证码登录方式错误码提示
     * @param code
     */
    public void toastForPhoneTop(String code) {
        switch (code) {
            case Const.PHONE_SEND_SUCCESS:
                ToastUtil.toastAlerMessageBottom(mContext,mContext.getString(R.string.phone_send_success),500);
                break;
            case Const.PHONE_TYPE_NO_OPEN:
                ToastUtil.toastAlerMessageiconTop(mContext,mLayoutInflater,mContext.getString(R.string.phone_type_no_open),500);
                break;
            case Const.PHONE_NO_FOUND:
                ToastUtil.toastAlerMessageiconTop(mContext,mLayoutInflater,mContext.getString(R.string.phone_no_found),500);
                break;
            case Const.PHONE_NO_ACCOUNT:
                ToastUtil.toastAlerMessageiconTop(mContext,mLayoutInflater,mContext.getString(R.string.phone_no_account),500);
                break;
            case Const.PHONE_ACCOUNT_LOCK:
                ToastUtil.toastAlerMessageiconTop(mContext,mLayoutInflater,mContext.getString(R.string.phone_account_lock),500);
                break;
            case Const.PHONE_ACCOUNT_OUT_TIME:
                ToastUtil.toastAlerMessageiconTop(mContext,mLayoutInflater,mContext.getString(R.string.phone_account_out_time),500);
                break;
            case Const.PHONE_ACCOUNT_NO_FOUND_OR_OUT_TIME:
                ToastUtil.toastAlerMessageiconTop(mContext,mLayoutInflater,mContext.getString(R.string.phone_account_no_found_or_out_time),500);
                break;
            case Const.PHONE_SEND_CODE_ERROR:
                ToastUtil.toastAlerMessageiconTop(mContext,mLayoutInflater,mContext.getString(R.string.phone_send_code_error),500);
                break;
            case Const.PHONE_NO_PREPAID:
                ToastUtil.toastAlerMessageiconTop(mContext,mLayoutInflater,mContext.getString(R.string.phone_no_prepaid),500);
                break;
            case Const.PHONE_GET_CODE_OFTEN:
                ToastUtil.toastAlerMessageiconTop(mContext,mLayoutInflater,mContext.getString(R.string.phone_get_code_often),500);
                break;
            case Const.PHONE_GET_CODE_OFTEN_2:
                ToastUtil.toastAlerMessageiconTop(mContext,mLayoutInflater,mContext.getString(R.string.phone_get_code_often2),500);
                break;
            case Const.PHONE_NO_EMPTY:
                ToastUtil.toastAlerMessageiconTop(mContext,mLayoutInflater,mContext.getString(R.string.phone_no_empty),500);
                break;
            case Const.PHONE_CODE_ERROR_OR_OUT_TIME:
                ToastUtil.toastAlerMessageiconTop(mContext,mLayoutInflater,mContext.getString(R.string.phone_code_error_or_out_time),500);
                break;
            case Const.PHONE_CODE_LOSE:
                ToastUtil.toastAlerMessageiconTop(mContext,mLayoutInflater,mContext.getString(R.string.phone_code_lose),500);
                break;

        }
    }
}
