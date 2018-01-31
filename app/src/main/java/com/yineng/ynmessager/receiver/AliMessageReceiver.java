package com.yineng.ynmessager.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.alibaba.sdk.android.push.MessageReceiver;
import com.alibaba.sdk.android.push.notification.CPushMessage;
import com.yineng.ynmessager.activity.Splash.SplashActivity;
import com.yineng.ynmessager.activity.session.PlatformNoticeActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.event.NoticeEvent;
import com.yineng.ynmessager.util.IconBadgerHelper;
import com.yineng.ynmessager.util.SystemUtil;

import java.util.Map;

/**
 * Created by 舒欢
 * Created time: 2017/8/2
 * Descreption：
 */

public class AliMessageReceiver extends MessageReceiver {

    // 消息接收部分的LOG_TAG
    public static final String REC_TAG = "AliMessageReceiver";

    /**
     * 推送通知的回调方法
     * @param context
     * @param title
     * @param summary
     * @param extraMap
     */
    @Override
    public void onNotification(Context context, String title, String summary, Map<String, String> extraMap) {
        // TODO 处理推送通知
        if ( null != extraMap ) {
            for (Map.Entry<String, String> entry : extraMap.entrySet()) {
                Log.i(REC_TAG,"@Get diy param : Key=" + entry.getKey() + " , Value=" + entry.getValue());
            }
        } else {
            Log.i(REC_TAG,"@收到通知 && 自定义消息为空");
        }
        LocalBroadcastManager mBroadcastManager = LocalBroadcastManager.getInstance(context);
        mBroadcastManager.sendBroadcast(new Intent(NoticeEvent.ACTION_REFRESH_UNREAD));
//        IconBadgerHelper.showIconBadger(context);//刷新气泡
    }

    @Override
    protected void onNotificationReceivedInApp(Context context, String title, String summary, Map<String, String> extraMap, int openType, String openActivity, String openUrl) {
        Log.i(REC_TAG,"onNotificationReceivedInApp ： " + " : " + title + " : " + summary + "  " + extraMap + " : " + openType + " : " + openActivity + " : " + openUrl);
    }

    /**
     * 推送消息的回调方法
     *
     * @param context
     * @param cPushMessage
     */
    @Override
    public void onMessage(Context context, CPushMessage cPushMessage) {
        try {
            Log.i(REC_TAG,"收到一条推送消息 ： " + cPushMessage.getTitle());
        } catch (Exception e) {
            Log.i(REC_TAG, e.toString());
        }
    }

    /**
     * 从通知栏打开通知的扩展处理
     * @param context
     * @param title
     * @param summary
     * @param extraMap
     */
    @Override
    public void onNotificationOpened(Context context, String title, String summary, String extraMap) {
        Log.i(REC_TAG,"onNotificationOpened ： " + " : " + title + " : " + summary + " : " + extraMap);
        if (SystemUtil.isUiRunningFront()&& !SystemUtil.isApplicationBroughtToBackground(context)){
            Log.i(REC_TAG,"true");
            Intent intent = new Intent(context, PlatformNoticeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP| Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        else {
            Log.i(REC_TAG,"false");
            //此时当前APP不在前台进程，重启这个APP
            Intent mainIntent = new Intent(context, SplashActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            AppController.getInstance().isAliTuisong = true;
            context.startActivity(mainIntent);
        }

    }


    @Override
    public void onNotificationRemoved(Context context, String messageId) {
        Log.i(REC_TAG, "onNotificationRemoved ： " + messageId);
    }


    @Override
    protected void onNotificationClickedWithNoAction(Context context, String title, String summary, String extraMap) {
        Log.i(REC_TAG,"onNotificationClickedWithNoAction ： " + " : " + title + " : " + summary + " : " + extraMap);
    }
}
