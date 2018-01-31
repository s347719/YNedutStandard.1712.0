package com.yineng.ynmessager.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.util.AppUtils;
import com.yineng.ynmessager.util.L;

import org.apache.commons.lang3.StringUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yn on 2017/5/18.
 */

public class GuardService extends Service {

    private boolean isLogout=false;//用户注销
    /**
     * 用户注销广播接收器
     */
    private BroadcastReceiver mUserLogoutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (StringUtils.equals(Const.BROADCAST_ACTION_USER_LOGOUT, action)) {
                isLogout=true;
                stopSelf();
            }
        }
    };

    /**
     * 定位服务死亡广播接收器
     */
    private BroadcastReceiver locateDeathReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (StringUtils.equals(Const.LOCATE_ACTION_DEATH, action)) {
                startService(new Intent(context,LocateService.class));
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        /**用户注销*/
        IntentFilter startIntentFilter = new IntentFilter();
        startIntentFilter.addAction(Const.BROADCAST_ACTION_USER_LOGOUT);
        registerReceiver(mUserLogoutReceiver, startIntentFilter);
        /**定位服务死亡*/
        IntentFilter locateDeathFilter = new IntentFilter();
        startIntentFilter.addAction(Const.LOCATE_ACTION_DEATH);
        registerReceiver(locateDeathReceiver, locateDeathFilter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.e("msg","守护服务启动了");
        if (task != null) {
            task.cancel();
        }
        startTimer();
        timer.schedule(task, 2000, 60 * 1000);//延迟检测防止同时出现两条定位服务
        return Service.START_STICKY;
    }

    private Timer timer = new Timer();
    private TimerTask task;

    /**
     * 定时器请求定位
     */
    private void startTimer() {
        task = new TimerTask() {
            @Override
            public void run() {
                if (!AppUtils.isServiceWork(GuardService.this, LocateService.class.getName())) {//不存在该服务启动定位服务
                    L.e("msg","定位服务不存在了！！");
                    startService(new Intent(GuardService.this, LocateService.class));
                }else {
                    L.e("msg","定位服务还在！！");
                }
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        L.e("msg","守护服务销毁了----onDestroy");
        unregisterReceiver(mUserLogoutReceiver);
        unregisterReceiver(locateDeathReceiver);
        if (task != null) {
            task.cancel();
        }
        if (!isLogout) {
            sendBroadcast(new Intent(Const.GUARD_ACTION_DEATH));//发送守护死亡广播
        }else {
            L.e("msg","用户注销了----守护服务销毁了");
        }
    }
}
