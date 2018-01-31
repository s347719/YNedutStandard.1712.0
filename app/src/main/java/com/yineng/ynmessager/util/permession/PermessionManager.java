/*
 * Copyright (C) 2016 Facishare Technology Co., Ltd. All Rights Reserved.
 */
package com.yineng.ynmessager.util.permession;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Description:
 *
 * @author zhaozp
 * @since 2016-10-17
 */

public class PermessionManager {
    private static final String TAG = "PermessionManager";

    private static volatile PermessionManager instance;

    public static PermessionManager getInstance() {
        if (instance == null) {
            synchronized (PermessionManager.class) {
                if (instance == null) {
                    instance = new PermessionManager();
                }
            }
        }
        return instance;
    }


    public boolean checkPermission(Context context,int op) {
        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.checkIsMiuiRom()) {
                return miuiPermissionCheck(context,op);
            } else if (RomUtils.checkIsMeizuRom()) {
                return meizuPermissionCheck(context,op);
            } else if (RomUtils.checkIsHuaweiRom()) {
                return huaweiPermissionCheck(context,op);
            } else if (RomUtils.checkIs360Rom()) {
                return qikuPermissionCheck(context,op);
            }
        }
        return commonROMPermissionCheck(context, op);
    }

    private boolean huaweiPermissionCheck(Context context,int op) {
        return HuaweiUtils.checkCameraOpenPermession(context,op);
    }

    private boolean miuiPermissionCheck(Context context,int op) {
        return MiuiUtils.checkCameraOpenPermession(context,op);
    }

    private boolean meizuPermissionCheck(Context context,int op) {
        return MeizuUtils.checkCameraOpenPermession(context,op);
    }

    private boolean qikuPermissionCheck(Context context,int op) {
        return QikuUtils.checkCameraOpenPermession(context,op);
    }

    private boolean commonROMPermissionCheck(Context context,int op) {
        //最新发现魅族6.0的系统这种方式不好用，天杀的，只有你是奇葩，没办法，单独适配一下
        if (RomUtils.checkIsMeizuRom()) {
            return meizuPermissionCheck(context,op);
        } else {
            Boolean result = true;
            if (Build.VERSION.SDK_INT >= 23) {
                try {
                    Class clazz = Settings.class;
                    Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                    result = (Boolean) canDrawOverlays.invoke(null, context);
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
            return result;
        }
    }

    public void applyPermission(Context context) {
        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.checkIsMiuiRom()) {
                miuiROMPermissionApply(context);
            } else if (RomUtils.checkIsMeizuRom()) {
                meizuROMPermissionApply(context);
            } else if (RomUtils.checkIsHuaweiRom()) {
                huaweiROMPermissionApply(context);
            } else if (RomUtils.checkIs360Rom()) {
                ROM360PermissionApply(context);
            }else {
                goToDetailApplication(context);
            }
        }
        commonROMPermissionApply(context);
    }
    //在无法做出判断情况下统一跳转到设置详情页
    private void goToDetailApplication(Context context) {
        Intent localIntent = new Intent();
        localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        context.startActivity(localIntent);
    }

    private void ROM360PermissionApply(final Context context) {
                    QikuUtils.applyPermission(context);
    }

    private void huaweiROMPermissionApply(final Context context) {
                    HuaweiUtils.applyPermission(context);
    }

    private void meizuROMPermissionApply(final Context context) {
                    MeizuUtils.applyPermission(context);
    }

    private void miuiROMPermissionApply(final Context context) {
        MiuiUtils.applyMiuiPermission(context);
    }

    /**
     * 通用 rom 权限申请
     */
    private void commonROMPermissionApply(final Context context) {
        //这里也一样，魅族系统需要单独适配
        if (RomUtils.checkIsMeizuRom()) {
            meizuROMPermissionApply(context);
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                Class clazz = Settings.class;
                Field field ;
                try {
                    field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION");
                    Intent intent = new Intent(field.get(null).toString());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    context.startActivity(intent);
                } catch (Exception e) {
                    goToDetailApplication(context);
                }
            }
        }
    }

}
