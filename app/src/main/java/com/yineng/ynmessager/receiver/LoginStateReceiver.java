package com.yineng.ynmessager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.yineng.ynmessager.bean.login.LoginConfig;
import com.yineng.ynmessager.bean.login.LoginUser;
import com.yineng.ynmessager.db.dao.LoginUserDao;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.TimeUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author yhu
 * Created by yn on 2017/9/28.
 * 判断是否自动退出
 */

public class LoginStateReceiver extends BroadcastReceiver {
    private final String mTag = this.getClass().getSimpleName();
    public static final int TYPE_SERVER_TIME = 0;
    public static final int TYPE_ACCOUNT_ERROR =1;

    public interface LoginStateListener {
        void loginState(boolean isLogOut);
    }


    public LoginStateReceiver() {
        super();
    }

    private static LoginStateListener onLoginStateListener;

    public void setonLoginStateListener(LoginStateListener onLoginStateListener) {
        this.onLoginStateListener = onLoginStateListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int type = intent.getIntExtra("type",3);
        //自动登录时间判断
        if(type == TYPE_SERVER_TIME) {
            Log.i(mTag, "监听到服务器时间");
            String serverTime = intent.getStringExtra("ServiceTime");
            LastLoginUserSP mLastUser = LastLoginUserSP.getInstance(context);
            LoginUserDao mLoginUserDao = new LoginUserDao(context);
            LoginUser uesr = mLoginUserDao.getLoginUserByUserNo(mLastUser.getUserAccount());

            try {
                //判断是否超时
                String lastLoginDateStr = uesr.getLastLoginDate();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date nowDate = format.parse(serverTime);
                Date lastLoginDate = format.parse(lastLoginDateStr);
                int diffDay = TimeUtil.differentDaysByMillisecond(lastLoginDate, nowDate);
                //获取配置文件里的登录天数
                LoginConfig loginConfig = mLastUser.getServiceLoginConfig();
                Log.i(mTag, "服务器限制登录天数:" + loginConfig.getMsgMobileAutoLoginDay() + ";当前登录时间：" + serverTime + ";最后一次登录时间：" + lastLoginDateStr + ";时间天数对比：" + diffDay);
                //如果登录天数大于配置文件里的天数就不让登录
                if (diffDay > loginConfig.getMsgMobileAutoLoginDay()) {
                    Log.i(mTag, "自动登录失败：天数错误");
                    if (onLoginStateListener != null) {
                        onLoginStateListener.loginState(true);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
                if (onLoginStateListener != null) {
                    onLoginStateListener.loginState(false);
                }
            }
            //账号密码错误
        }else if(type == TYPE_ACCOUNT_ERROR){
            if (onLoginStateListener != null) {
                onLoginStateListener.loginState(true);
            }
        }
    }
}
