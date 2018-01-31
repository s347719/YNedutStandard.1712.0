//***************************************************************
//*    2015-5-29  上午10:03:49
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;

import com.yineng.ynmessager.app.AppController;

import java.io.File;
import java.util.List;

/**
 * @author 贺毅柳
 */
public class SystemUtil
{
    public static final String TAG = "SystemUtil";

    /**
     * 判断当前UI是否前台运行
     *
     * @return true表示UI正在前台展示运行中；否则返回false
     */
    public static boolean isUiRunningFront()
    {
        boolean isAppRunning = AppController.getInstance().isShownOnScreen;
        return isAppRunning;
    }


    /**
     *判断当前应用程序处于前台还是后台
     */
    public static boolean isApplicationBroughtToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;

    }




    //隐藏键盘
    public static void hideSoftInputView(Context context) {
        InputMethodManager manager = ((InputMethodManager) context
                .getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (((Activity) context).getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (((Activity) context).getCurrentFocus() != null) {
                manager.hideSoftInputFromWindow(((Activity) context)
                                .getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * 获取当前设备的屏幕显示宽度
     *
     * @param context 不解释
     * @return 屏幕显示宽度（px）
     */
    public static int getScreenWidth(Context context)
    {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    /**
     * 获取当前设备的屏幕显示高度
     *
     * @param context 不解释
     * @return 屏幕显示高度（px）
     */
    public static int getScreenHeight(Context context)
    {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }

    /**
     * dp转换为px
     *
     * @param context 不解释
     * @param dpValue 要转换的dp值
     * @return 对应的px
     */
    public static int dp2px(Context context, float dpValue)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px转换为dp
     *
     * @param context 不解释
     * @param pxValue 要转换的px值
     * @return 对应的dp
     */
    public static int px2dp(Context context, float pxValue)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 判断在触摸屏幕时，是否超出了指定控件的范围
     *
     * @param v     View控件；为null则返回false
     * @param event MotionEvent用于判断
     * @return true表示超出了控件范围，否则为false
     */
    public static boolean isTouchOutside(View v, MotionEvent event)
    {
        if (v != null)
        {
            int[] leftTop = new int[2];
            // 获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            float touchX = event.getX();
            float touchY = event.getY();
            return !(touchX > left && touchX < right && touchY > top && touchY < bottom);
        }
        return false;
    }

    /**
     * 给系统发送一个Intent来打开（执行）指定的File对象
     *
     * @param context 将使用这个Context对象来发送Intent（startActivity）
     * @param file
     * @return 打开成功返回true，否则返回false
     */
    public static boolean execLocalFile(Context context, File file)
    {
        boolean flag;

        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        Intent newIntent = new Intent(Intent.ACTION_VIEW);

        String ext = FileUtil.fileExt(file.getPath());
        if (ext != null)
        {
            ext = ext.substring(1);
        } else
        {
            ext = "";
        }
        String mimeType = myMime.getMimeTypeFromExtension(ext);
        newIntent.setDataAndType(Uri.fromFile(file), mimeType)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try
        {
            context.startActivity(newIntent);
            flag = true;
        } catch (ActivityNotFoundException e)
        {
            flag = false;
            L.w(TAG, "no apps found for opening this file", e);
        }

        return flag;
    }

    public static boolean getGpsState(Context context)
    {
        ContentResolver resolver = context.getContentResolver();
        boolean open = Settings.Secure.isLocationProviderEnabled(resolver,
                LocationManager.GPS_PROVIDER);
        return open;
    }

    /**
     * @param context
     * @return 获取客户端的imei值
     */
    public static String getImei(Context context)
    {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        return tm.getDeviceId();
    }

    /**
     * @return 获取手机操作系统的版本号
     */
    public static int getSystemVersion()
    {
        return android.os.Build.VERSION.SDK_INT;
    }

    /**
     * @param context
     * @return获取 app 的版本号
     */
    public static int geVersionCode(Context context)
    {
        int verCode = -1;
        try
        {
            verCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return verCode;
    }

    /**
     * @param context
     * @return 获取 app 的版本名称
     */
    public static String getVersionName(Context context)
    {
        String verName = "";
        try
        {
            verName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return verName;
    }

}
