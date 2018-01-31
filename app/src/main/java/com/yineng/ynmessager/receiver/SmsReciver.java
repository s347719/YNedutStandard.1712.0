package com.yineng.ynmessager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yn on 2017/9/21.
 */

public class SmsReciver extends BroadcastReceiver {

    private final String TAG = SmsReciver.class.getSimpleName();

    public interface OnSmsPhoneCodeListener {
        void onReciverPhoneCode(String code);
    }


    public SmsReciver() {
        super();
    }

    private static OnSmsPhoneCodeListener onSmsPhoneCodeListener;

    public void setOnSmsPhoneCodeListener(OnSmsPhoneCodeListener onSmsPhoneCodeListener) {
        this.onSmsPhoneCodeListener = onSmsPhoneCodeListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras()==null){
            return;
        }
        Object[] pdus = (Object[]) intent.getExtras().get("pdus");
        if (pdus!=null) {
            for (Object pdu : pdus) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                String sender = smsMessage.getDisplayOriginatingAddress();
                String content = smsMessage.getMessageBody();
                long date = smsMessage.getTimestampMillis();
                Date timeDate = new Date(date);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time = simpleDateFormat.format(timeDate);

                Log.i(TAG, "onReceive: 短信来自:" + sender);
                Log.i(TAG, "onReceive: 短信内容:" + content);
                Log.i(TAG, "onReceive: 短信时间:" + time);
                //1.判断是否是依能的短信
                if (content.contains("YNedut数字校园")) {
                    //2.截取到验证码
                    int codeStart = content.indexOf("短信动态码") + 6;
                    String code = content.substring(codeStart, codeStart + 6);
                    Log.e(TAG, "短信验证码:" + code);
                    if (onSmsPhoneCodeListener != null) {
                        onSmsPhoneCodeListener.onReciverPhoneCode(code);
                    }
                }
            }
        }
    }
}
