package com.yineng.ynmessager.util;

import android.util.Log;

import org.apache.commons.lang3.ClassUtils;

/**
 * @author YINENG
 */
public class L
{
    public static boolean isDebug = true;// 是否需要打印bug，可以在application的onCreate函数里面初始化
    private static final String TAG = "YINENG";

    /**
     * 返回Class的标签
     *
     * @param c
     * @return
     */
    public static String getClassTag(Class<?> c)
    {
        return ClassUtils.getSimpleName(c);
    }

    // 下面四个是默认tag的函数
    public static void i(String msg)
    {
        if (isDebug) {
            Log.i(TAG, msg);
        }
    }

    public static void d(String msg)
    {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }

    public static void e(String msg)
    {
        if (isDebug) {
            Log.e(TAG, msg);
        }
    }

    public static void v(String msg)
    {
        if (isDebug) {
            Log.v(TAG, msg);
        }
    }

    // 下面是传入类名打印log
    public static void i(Class<?> c, String msg)
    {
        if (isDebug) {
            Log.i(getClassTag(c), msg);
        }
    }

    public static void i(String tag, String msg, Object... args) {
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        if (isDebug) {
            Log.i(tag, msg);
        }
    }
    public static void d(Class<?> c, String msg)
    {
        if (isDebug) {
            Log.d(getClassTag(c), msg);
        }
    }

    public static void d(String tag, String msg, Object... args) {
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        if (isDebug) {
            Log.d(tag, msg);
        }
    }

    public static void w(Class<?> c, String msg)
    {
        if (isDebug) {
            Log.w(getClassTag(c), msg);
        }
    }

    public static void e(Class<?> c, String msg)
    {
        if (isDebug) {
            Log.e(getClassTag(c), msg);
        }
    }

    public static void v(Class<?> c, String msg)
    {
        if (isDebug) {
            Log.v(getClassTag(c), msg);
        }
    }

    public static void i(Class<?> c, String msg, Throwable tr)
    {
        if (isDebug) {
            Log.i(getClassTag(c), msg, tr);
        }
    }

    public static void d(Class<?> c, String msg, Throwable tr)
    {
        if (isDebug) {
            Log.d(getClassTag(c), msg, tr);
        }
    }

    public static void w(Class<?> c, String msg, Throwable tr)
    {
        if (isDebug) {
            Log.w(getClassTag(c), msg, tr);
        }
    }

    public static void e(Class<?> c, String msg, Throwable tr)
    {
        if (isDebug) {
            Log.e(getClassTag(c), msg, tr);
        }
    }

    public static void v(Class<?> c, String msg, Throwable tr)
    {
        if (isDebug) {
            Log.v(getClassTag(c), msg, tr);
        }
    }

    public static void v(String tag, String msg, Object... args) {
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        if (isDebug) {
            Log.v(tag, msg);
        }
    }
    // 下面是传入自定义tag的函数
    public static void i(String tag, String msg)
    {
        if (isDebug) {
            Log.i(tag, msg);
        }
    }

    public static void d(String tag, String msg)
    {
        if (isDebug) {
            Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg)
    {
        if (isDebug) {
            Log.e(tag, msg);
        }
    }

    public static void v(String tag, String msg)
    {
        if (isDebug) {
            Log.v(tag, msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr)
    {
        if (isDebug) {
            Log.i(tag, msg, tr);
        }
    }

    public static void d(String tag, String msg, Throwable tr)
    {
        if (isDebug) {
            Log.d(tag, msg, tr);
        }
    }

    public static void e(String tag, String msg, Throwable tr)
    {
        if (isDebug) {
            Log.e(tag, msg, tr);
        }
    }

    public static void v(String tag, String msg, Throwable tr)
    {
        if (isDebug) {
            Log.v(tag, msg, tr);
        }
    }

    public static void w(String tag, String msg)
    {
        if (isDebug)
        {
            Log.w(tag, msg);
        }
    }

    public static void w(String tag, Throwable tr)
    {
        if (isDebug)
        {
            Log.w(tag, tr);
        }
    }

    public static void w(String tag, String msg, Throwable tr)
    {
        if (isDebug)
        {
            Log.w(tag, msg, tr);
        }
    }
}
