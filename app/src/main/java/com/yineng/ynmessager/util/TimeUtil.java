package com.yineng.ynmessager.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Time;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

/**
 * @author YUTANG
 */
public class TimeUtil {
    /**
     * 一天的秒数
     */
    public static final long DAY_SEC = 60 * 60 * 24;

    /**
     * 一天的毫秒数
     */
    public static final long DAY_MILLIS_SEC = 1000 * DAY_SEC;

    /**
     * "MM-dd HH:mm" 24小时制
     */
    public static final String FORMAT_DATETIME_MM_DD = "MM-dd HH:mm";

    /**
     * "MM月dd日 HH:mm" 24小时制
     */
    public static final String FORMAT_DATETIME_MM_DD2= "MM月dd日 HH:mm";

    /**
     * "yyyy-MM-dd hh:mm:ss" 12小时制
     */
    public static final String FORMAT_DATETIME_12 = "yyyy-MM-dd hh:mm:ss";

    /**
     * "yyyy-MM-dd HH:mm:ss" 24小时制
     */
    public static final String FORMAT_DATETIME_24 = "yyyy-MM-dd HH:mm:ss";

    /**
     * "yyyy-MM-dd HH:mm:ss.SSS" 24小时制,含有毫秒
     */
    public static final String FORMAT_DATETIME_24_mic = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * "yyyy-MM-dd"
     */

    public static final String FORMAT_DATE1 = "yyyy-MM-dd";

    /**
     * "yyyy-MM"
     */
    public static final String FORMAT_DATE2 = "yyyy-MM";

    /**
     * "yyyy年MM月"
     */
    public static final String FORMAT_DATE3 = "yyyy年MM月";

    /**
     * "yyyy年"
     */
    public static final String FORMAT_DATE4 = "yyyy年";


    public static final String DAY = "HH:mm:ss";


    /**
     * "MM - dd"
     */
    public static final String FORMAT_DATA_DAY = "MM-dd";

    public static final String[] WEEK_DAYS = AppController.getInstance().getResources()
            .getStringArray(R.array.common_weekDays);

    //计算当前是否在配置指定的时间范围内
    public static boolean isInTimeScope(String start, String end, String now) {
        long startAt = TimeUtil.getMillisecondByDate(start,TimeUtil.FORMAT_DATETIME_24);
        long endAt = TimeUtil.getMillisecondByDate(end,TimeUtil.FORMAT_DATETIME_24);
        long nowAt = TimeUtil.getMillisecondByDate(now,TimeUtil.FORMAT_DATETIME_24);
        L.e("MSG", "定位配置开始时间long：" + startAt + "定位配置结束时间long：" + endAt + "现在时间long：" + nowAt);
        return nowAt >= startAt && nowAt < endAt;
    }

    /**
     * 功能：将当前日期的年进行重新设置。
     * Jul 31, 2013 2:42:36 PM
     * @param year 某一年
     * @return 设置后的日期
     */
    public Date setYearNew(int year){
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.set(Calendar.YEAR, year);
        return new Date(c.getTimeInMillis());
    }

    /**
     * 获取当前年份
     * @return
     */
    public static int getCurrentYear(){
        Calendar instance = Calendar.getInstance();
        return instance.get(Calendar.YEAR);
    }



    /**
     * 获取当前月份
     * @return
     */
    public int getCurrentMonth(){
        Calendar instance = Calendar.getInstance();
        return instance.get(Calendar.MONTH);
    }

    /**
     * @param datetimeformate "yyyy-MM-dd" "yyyy-MM-dd hh:mm:ss"
     * @return 根据传入的时间格式，返回时间字符串
     */
    @SuppressLint("SimpleDateFormat")
    public static String getCurrenDateTime(String datetimeformate) {
        String currentTimestr = null;
        SimpleDateFormat sDateFormat = new SimpleDateFormat(datetimeformate);
        Date curDateTime = new Date(System.currentTimeMillis());
        currentTimestr = sDateFormat.format(curDateTime);
        return currentTimestr;
    }

    /**
     * 将Data 格式转换成String
     */
    public  static  String getStringFromDate(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FORMAT_DATETIME_24_mic);
        return  simpleDateFormat.format(date);
    }

    /**
     * 返回系统毫秒值
     *
     * @return
     */
    public static String getCurrentMillisecond() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 根据传入的时间，返回是星期几
     *
     * @param str
     * @return
     * @Title: getWeekIndexByDate
     */
    @SuppressLint("SimpleDateFormat")
    public static int getWeekIndexByDate(String str) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        int weeks = -1;
        try {
            date = dateFormatter.parse(str);
            dateFormatter.applyPattern("w");
            weeks = Integer.valueOf(dateFormatter.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return weeks;
    }

    /**
     * 根据传入的时间字符串，返回毫秒值
     *
     * @param date
     * @param dateFormat
     * @return
     * @Title: getMillisecondByDate
     * @Description:
     */
    public static long getMillisecondByDate(String date, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        try {
            long millionSeconds = sdf.parse(date).getTime();
            return millionSeconds;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1L;
    }

    /**
     * 根据传入的long值判断2个数据是否是同年同月
     * @param mills1
     * @param mills2
     * @return
     */
    public static boolean isSameYearAndMonth(String mills1,String mills2){
        Calendar e = Calendar.getInstance();
        int year1 = -1;
        int month1 = -1;
        int year2 = -1;
        int month2 = -1;
        if (mills1.length()>0) {
            e.setTime(new Date(Long.parseLong(mills1)));
            year1 = e.get(Calendar.YEAR);
            month1 = e.get(Calendar.MONTH);
        }
        if (mills2.length()>0){
            e.setTime(new Date(Long.parseLong(mills2)));
            year2 = e.get(Calendar.YEAR);
            month2 = e.get(Calendar.MONTH);
        }
        if (year1==year2){
            if (month1==month2){
                return true;
            }else {
                return  false;
            }
        }else {
            return false;
        }
    }



    /**
     * 根据毫秒值和时间格式，返回时间字符串"yyyy-MM-dd""yyyy-MM-dd hh:mm:ss""
     *
     * @param dateFormat
     * @return
     */
    public static String getDateByMillisecond(long millionSeconds, String dateFormat) {
        String currentTimestr = null;
        SimpleDateFormat sDateFormat = new SimpleDateFormat(dateFormat);
        Date curDateTime = new Date(millionSeconds);
        currentTimestr = sDateFormat.format(curDateTime);
        return currentTimestr;
    }

    /**
     * 根据时间字符串和时间格式，返回时间字符串"yyyy-MM-dd""yyyy-MM-dd hh:mm:ss""
     *
     * @param dateFormat
     * @return
     */
    public static String getDateByDateStr(String dateStr, String dateFormat) {
        String currentTimestr = null;
        SimpleDateFormat sDateFormat = new SimpleDateFormat(dateFormat);
        long millionSeconds = getMillisecondByDate(dateStr, FORMAT_DATETIME_24);
        Date curDateTime = new Date(millionSeconds);
        currentTimestr = sDateFormat.format(curDateTime);
        return currentTimestr;
    }

    /**
     * 根据日期，返回星期几
     *
     * @return
     * @Title: getwhatDayByDate
     */
    public static String getWeekdayByDate(String date) {
        long milliseconds = getMillisecondByDate(date, "yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(milliseconds));
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return WEEK_DAYS[w];
    }

    /**
     * 根据日期，返回是第几月份
     *
     * @param date
     */
    public static int getMonthByDate(String date) {
        long milliseconds = getMillisecondByDate(date, "yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(milliseconds));
        return (cal.get(Calendar.MONTH) + 1);
    }

    /**
     * 当前系统时间是星期几
     *
     * @return
     * @Title: getCurrentWeek
     */
    public static String getCurrentWeekday() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return WEEK_DAYS[w];
    }

    /**
     * 将 7:5这样的时间格式转换成 07:05 的格式
     *
     * @param hour   小时，比如 7
     * @param minute 分钟，比如 5
     * @return 格式化后的时间格式字符串，比如07:05<br>
     * 数组第0个是hour，第1个是minute
     */
    public static String[] betterTimeDisplay(int hour, int minute) {
        String[] betterTime = new String[2];
        betterTime[0] = hour >= 10 ? String.valueOf(hour) : "0" + hour;
        betterTime[1] = minute >= 10 ? String.valueOf(minute) : "0" + minute;
        return betterTime;
    }

    /**
     * 将 6这样的月份转换成  06
     * @return
     */
    public static String betterMonthTimeDisplay(int month){

        return month >=10 ? String.valueOf(month) : "0"+month;
    }

    /**
     * 判断当前系统时间是否在指定时间的范围内
     *
     * @param beginHour 开始小时，例如22
     * @param beginMin  开始小时的分钟数，例如30
     * @param endHour   结束小时，例如 8
     * @param endMin    结束小时的分钟数，例如0
     * @return true表示在范围内，否则false
     */
    public static boolean isCurrentInTimeScope(int beginHour, int beginMin, int endHour, int endMin) {
        boolean result = false;
        final long aDayInMillis = 1000 * 60 * 60 * 24;
        final long currentTimeMillis = System.currentTimeMillis();

        Time now = new Time();
        now.set(currentTimeMillis);

        Time startTime = new Time();
        startTime.set(currentTimeMillis);
        startTime.hour = beginHour;
        startTime.minute = beginMin;

        Time endTime = new Time();
        endTime.set(currentTimeMillis);
        endTime.hour = endHour;
        endTime.minute = endMin;

        if (!startTime.before(endTime)) // 跨天的特殊情况（比如22:00-8:00）
        {
            startTime.set(startTime.toMillis(true) - aDayInMillis);
            result = !now.before(startTime) && !now.after(endTime); // startTime <= now <= endTime

            Time startTimeInThisDay = new Time();
            startTimeInThisDay.set(startTime.toMillis(true) + aDayInMillis);
            if (!now.before(startTimeInThisDay)) {
                result = true;
            }
        } else //普通情况(比如 8:00 - 14:00)
        {
            result = !now.before(startTime) && !now.after(endTime); // startTime <= now <= endTime
        }
        return result;
    }

    /**
     * 判断Date的时间关系字符串（传入的Date对象与当前时间的关系）
     *
     * @param context 你懂的
     * @param date    想要判断的Date对象
     * @return 返回时间关系字符串，比如：
     * <ul>
     * <li>当天，会直接返回具体时间，比如“19:48”</li>
     * <li>昨天，会返回“昨天”</li>
     * <li>前天到前一周内，会返回其所在的星期，比如“星期三”</li>
     * <li>超出前一周范围，返回其日期，比如“2015-5-8”</li>
     * </ul>
     */
    public static String getTimeRelationFromNow(Context context, Date date) {
        String relative = "";
        if (date == null) {
            return relative;
        }
        long today = date.getTime();
        long weekFromToday[] = {today, today + DateUtils.DAY_IN_MILLIS, today + DateUtils.DAY_IN_MILLIS * 2,
                today + DateUtils.DAY_IN_MILLIS * 3, today + DateUtils.DAY_IN_MILLIS * 4,
                today + DateUtils.DAY_IN_MILLIS * 5, today + DateUtils.DAY_IN_MILLIS * 6,
                today + DateUtils.DAY_IN_MILLIS * 7};

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        if (DateUtils.isToday(weekFromToday[0])) // 判断是否为今天
        {
            String timeDisplay[] = betterTimeDisplay(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
            relative = timeDisplay[0] + ":" + timeDisplay[1];
        } else if (DateUtils.isToday(weekFromToday[1])) // 是否为昨天
        {
            relative = context.getString(R.string.session_yesterday);
        } else if (DateUtils.isToday(weekFromToday[2]) || DateUtils.isToday(weekFromToday[3])
                || DateUtils.isToday(weekFromToday[4]) || DateUtils.isToday(weekFromToday[5])
                || DateUtils.isToday(weekFromToday[6]) || DateUtils.isToday(weekFromToday[7])) // 是否为前天到上一周内的时间范围
        {
            // 这里判断周是上周的还是这周的
            // if(calendar.get(Calendar.WEEK_OF_YEAR) !=
            // Calendar.getInstance().get(Calendar.WEEK_OF_YEAR))
            // {
            // relative += "上周";
            // }
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            switch (dayOfWeek) {
                case 1:
                    relative += WEEK_DAYS[0];
                    break;
                case 2:
                    relative += WEEK_DAYS[1];
                    break;
                case 3:
                    relative += WEEK_DAYS[2];
                    break;
                case 4:
                    relative += WEEK_DAYS[3];
                    break;
                case 5:
                    relative += WEEK_DAYS[4];
                    break;
                case 6:
                    relative += WEEK_DAYS[5];
                    break;
                case 7:
                    relative += WEEK_DAYS[6];
                    break;
                default:
                    relative = "";
                    break;
            }
        } else
        // 超出一周以外的
        {
            relative = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-"
                    + calendar.get(Calendar.DAY_OF_MONTH);
        }
        return relative;
    }

    // TODO: 2016/1/13 什么时候考虑把 getTimeRelationFromNow(2) 这两个方法重写了

    /**
     * 判断Date的时间关系字符串【版本二】（传入的Date对象与当前时间的关系）
     *
     * @param context 你懂的
     * @param date    想要判断的Date对象
     * @return 返回时间关系字符串，比如：
     * <ul>
     * <li>当天，会直接返回具体时间，比如“19:48”</li>
     * <li>昨天，会返回“昨天”加具体时间，比如“昨天 19:48”</li>
     * <li>前天到前一周内，会返回其所在的星期加具体时间，比如“星期三 19:48”</li>
     * <li>超出前一周范围，返回其日期加具体时间，比如“2015-5-8 19:48”</li>
     * </ul>
     */
    public static String getTimeRelationFromNow2(Context context, Date date) {
        String relative = "";
        if (date == null) {
            return relative;
        }
        long today = date.getTime();
        long weekFromToday[] = {today, today + DateUtils.DAY_IN_MILLIS, today + DateUtils.DAY_IN_MILLIS * 2,
                today + DateUtils.DAY_IN_MILLIS * 3, today + DateUtils.DAY_IN_MILLIS * 4,
                today + DateUtils.DAY_IN_MILLIS * 5, today + DateUtils.DAY_IN_MILLIS * 6,
                today + DateUtils.DAY_IN_MILLIS * 7};

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String timeDisplay[] = betterTimeDisplay(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        if (DateUtils.isToday(weekFromToday[0])) // 判断是否为今天
        {
            relative = timeDisplay[0] + ":" + timeDisplay[1];
        } else if (DateUtils.isToday(weekFromToday[1])) // 是否为昨天
        {
            relative = context.getString(R.string.session_yesterday);
            relative += " " + timeDisplay[0] + ":" + timeDisplay[1];
        } else if (DateUtils.isToday(weekFromToday[2]) || DateUtils.isToday(weekFromToday[3])
                || DateUtils.isToday(weekFromToday[4]) || DateUtils.isToday(weekFromToday[5])
                || DateUtils.isToday(weekFromToday[6]) || DateUtils.isToday(weekFromToday[7])) // 是否为前天到上一周内的时间范围
        {
            // 这里判断周是上周的还是这周的
            // if(calendar.get(Calendar.WEEK_OF_YEAR) !=
            // Calendar.getInstance().get(Calendar.WEEK_OF_YEAR))
            // {
            // relative += "上周";
            // }
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            switch (dayOfWeek) {
                case 1:
                    relative += WEEK_DAYS[0];
                    break;
                case 2:
                    relative += WEEK_DAYS[1];
                    break;
                case 3:
                    relative += WEEK_DAYS[2];
                    break;
                case 4:
                    relative += WEEK_DAYS[3];
                    break;
                case 5:
                    relative += WEEK_DAYS[4];
                    break;
                case 6:
                    relative += WEEK_DAYS[5];
                    break;
                case 7:
                    relative += WEEK_DAYS[6];
                    break;
            }
            relative += " " + timeDisplay[0] + ":" + timeDisplay[1];
        } else
        // 超出一周以外的
        {
            relative = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-"
                    + calendar.get(Calendar.DAY_OF_MONTH);
            relative += " " + timeDisplay[0] + ":" + timeDisplay[1];
        }
        return relative;
    }

    public static boolean isInOneWeek(Date date) {
        boolean flag = false;
        for (int i = 1; i <= 7; ++i) {
            flag = DateUtils.isToday(date.getTime() + (i * DAY_MILLIS_SEC));
            if (flag) {
                break;
            }
        }
        return flag;
    }

    public static boolean isSameWeek(Date date1, Date date2) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date1);
        int date1_year = calendar.get(Calendar.YEAR);
        int date1_week = calendar.get(Calendar.WEEK_OF_YEAR);

        calendar.clear();
        calendar.setTime(date2);
        int date2_year = calendar.get(Calendar.YEAR);
        int date2_week = calendar.get(Calendar.WEEK_OF_YEAR);

        return date1_year == date2_year && date1_week == date2_week;
    }

    public static boolean isSameMonth(Date date1, Date date2) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date1);
        int date1_year = calendar.get(Calendar.YEAR);
        int date1_month = calendar.get(Calendar.MONTH);

        calendar.clear();
        calendar.setTime(date2);
        int date2_year = calendar.get(Calendar.YEAR);
        int date2_month = calendar.get(Calendar.MONTH);

        return date1_year == date2_year && date1_month == date2_month;
    }

    public static String getRestTimeCountDownDay(long restTime) {// 根据毫秒差计算时间差
        // 毫秒
        long ssec = restTime % 1000;
        // 秒
        long sec = (restTime / 1000) % 60;
        // 分钟
        long min = (restTime / 1000 / 60) % 60;
        // 小时
        long hour = (restTime / 1000 / 60 / 60) % 24;
        // 天
        long day = restTime / 1000 / 60 / 60 / 24;

        if (day > 0) {
            return day + "天" + hour + "小时" + min + "分" + sec + "秒";
        }

        if (hour > 0) {
            return hour + "小时" + min + "分" + sec + "秒";
        }

        if (min > 0) {
            return min + "分" + sec + "秒";
        }

        return sec + "秒";
    }

    /**
     * 将日期时间字符串转换为Date对象
     *
     * @param date 要转换的日期时间字符串
     * @return 想要获得的Date对象
     */
    public static Date convertStringDate(String date) {
        SimpleDateFormat mDateFormat = new SimpleDateFormat(TimeUtil.FORMAT_DATETIME_24);
        Date d = null;
        // 2015-05-08 09:56:58
        try {
            d = mDateFormat.parse(date);
        } catch (ParseException e) {
            L.e("", e.getMessage(), e);
        }
        return d;
    }

    /**
     * 对比时间前后
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int compareDate(String date1, String date2, SimpleDateFormat dateFormat) {
        DateFormat df = dateFormat;
        try {
            Date dt1 = df.parse(date1);
            Date dt2 = df.parse(date2);
            if (dt1.getTime() > dt2.getTime()) {
                return 1;
            } else if (dt1.getTime() < dt2.getTime()) {
                return -1;
            } else {
                return 0;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    /**
     * 通过时间秒毫秒数判断两个时间的间隔
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int differentDaysByMillisecond(Date date1, Date date2) {
        int days = (int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24));
        return days;
    }

    /**
     * 判断差多少分钟
     * @param date1
     * @param date2
     * @return
     */
    public static int differentDaysByMin(Date date1, Date date2) {
        int between = (int) (date2.getTime() - date1.getTime()) / 1000;//除以1000是为了转换成秒
        int min = between / 60;
        return min;
    }

    /**
     * 比对聊天时间先后
     *
     * @param <T>
     */
    public static class ComparatorMessageTimeDESC<T> implements Comparator<T> {
        @Override
        public int compare(T o1, T o2) {
            BaseChatMsgEntity baseChatMsgEntity1 = (BaseChatMsgEntity) o1;
            BaseChatMsgEntity baseChatMsgEntity2 = (BaseChatMsgEntity) o2;
            Long time1 = Long.parseLong(baseChatMsgEntity1.getTime());
            Long time2 = Long.parseLong(baseChatMsgEntity2.getTime());
            if (time1 > time2) {
                return 1;
            }
            if (time1 == time2) {
                return 0;
            }
            return -1;
        }
    }

}
