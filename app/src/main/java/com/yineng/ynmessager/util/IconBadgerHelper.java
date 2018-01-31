package com.yineng.ynmessager.util;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.db.dao.RecentChatDao;
import com.yineng.ynmessager.service.BadgeIntentService;

/**
 * <p>桌面图标未读提示数量计算的帮助类</p>
 * Created by 贺毅柳 on 2015/11/24 17:22.
 */
public class IconBadgerHelper
{
    public static int count(Context context)
    {
        RecentChatDao recentChatDao = new RecentChatDao(context);
        int count = 0;
        count += recentChatDao.getUnReadMsgCount();
        count += AppController.getInstance().UnReedNoticeNum;
        if (count > 99)
        {
            count = 99;
        }
        return count;
    }

    public static void showIconBadger(Context context){
        if (Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")) {
            context.startService(
                    new Intent(context, BadgeIntentService.class).putExtra("badgeCount", IconBadgerHelper.count(context))
            );
        }
    }

}
