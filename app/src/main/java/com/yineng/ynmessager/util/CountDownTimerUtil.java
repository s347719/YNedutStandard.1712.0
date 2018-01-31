package com.yineng.ynmessager.util;

import android.os.CountDownTimer;
import android.widget.TextView;

/**
 * Created by yhu on 2017/6/16.
 */

public class CountDownTimerUtil extends CountDownTimer {

    private TextView textView;

    /**
     * 结束时的数字
     */
    private int finishTime = 1;

    /**
     * 显示的字符
     */
    private String showTxt = "s";

    public void setShowTxt(String showTxt) {
        this.showTxt = showTxt;
    }

    /**
     * 设置结束时的文字
     *
     * @param finishTime
     */
    public void setFinishTime(int finishTime) {
        this.finishTime = finishTime;
    }

    public interface OnCountDownTimerFinish {
        /**
         * 结束时的回调
         */
        void onTimeFinish();
    }

    private OnCountDownTimerFinish onCountDownTimerFinish;

    public void setOnCountDownTimerFinish(OnCountDownTimerFinish onCountDownTimerFinish) {
        this.onCountDownTimerFinish = onCountDownTimerFinish;
    }

    /**
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks.
     */
    public CountDownTimerUtil(long millisInFuture, long countDownInterval, TextView countTextView) {
        super(millisInFuture, countDownInterval);
        this.textView = countTextView;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (textView != null) {
            textView.setText(millisUntilFinished / 1000 + showTxt);
        }
    }

    @Override
    public void onFinish() {
        if (textView != null) {
            textView.setText(finishTime + showTxt);
        }

        if (onCountDownTimerFinish != null) {
            onCountDownTimerFinish.onTimeFinish();
        }
    }
}
