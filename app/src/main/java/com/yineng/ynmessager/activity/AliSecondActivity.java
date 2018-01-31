package com.yineng.ynmessager.activity;

import com.alibaba.sdk.android.push.AndroidPopupActivity;
import com.yineng.ynmessager.util.L;

import java.util.Map;

/**
 * Created by 舒欢
 * Created time: 2017/8/2
 * Descreption：阿里推送点击弹框进入的activity
 * 收到普通推送通道弹出的通知，点击后跳转到这里
 * 辅助弹窗通道弹出通知，点击后跳转到这里
 * https://help.aliyun.com/document_detail/30067.html
 */

public class AliSecondActivity extends AndroidPopupActivity {

    final String TAG = "AliSecondActivity";
    @Override
    protected void onSysNoticeOpened(String s, String s1, Map<String, String> map) {
        L.d(TAG, "Receive XiaoMi notification, title: " + s + ", content: " + s1 + ", extraMap: " + map);
    }
}
