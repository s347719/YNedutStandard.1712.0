package com.yineng.ynmessager.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.MainActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.settings.Setting;
import com.yineng.ynmessager.db.SettingsTb;
import com.yineng.ynmessager.db.dao.RecentChatDao;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.AppUtils;
import com.yineng.ynmessager.util.IconBadgerHelper;
import com.yineng.ynmessager.util.SystemUtil;
import com.yineng.ynmessager.util.TimeUtil;

import java.util.ArrayList;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * @author Yutang
 */
public class NoticesManager {
    private static final String TAG = "NoticesManager";
    private static final int ALARM_INTERVAL = 3000;
    private static final long VIBRATE_TIME = 400;
    private static NoticesManager sNoticesManager = null;
    public ArrayList<String> mReceivedUserList = new ArrayList<>(); // 所有接收到的消息的发送人
    public int mUnreadUserCount = 0; // 通知栏中用于显示的未读的联系人数
    public int mUnreadMsgCount = 0; // 通知栏中用于显示的未读的消息数
    private Context mContext;
    private NotificationManager mNM;
    private Bitmap mMSGLargeIcon;// 消息通知的大图标
    private RecentChatDao mRecentChatDao;
    private SoundPool mSoundPool;
    private Vibrator mVibrator;
    private int mStreamId;
    private long mLastReceived = 0L;
    private Resources mContextRes;

    private NoticesManager(Context context) {
        mContext = context;
        mContextRes = context.getResources();
        mRecentChatDao = new RecentChatDao(mContext);
        mNM = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        mMSGLargeIcon = BitmapFactory.decodeResource(mContext.getResources(), mContext.getResources().getIdentifier(AppController.icon_name, "mipmap", mContext.getPackageName()));
        initSoundPool();
    }

    /**
     * 懒汉式单例方法
     *
     * @return
     */
    public static synchronized NoticesManager getInstance(Context context) {
        if (sNoticesManager == null) {
            sNoticesManager = new NoticesManager(context);
        } else if (sNoticesManager.mRecentChatDao.userNo != null) {
            //如果静态变量里的recentDao的用户数据库对象与当前登录userNo不相等，则重新赋值
            if (!sNoticesManager.mRecentChatDao.userNo.equals(LastLoginUserSP.getInstance(context).getUserAccount())) {
                sNoticesManager = null;
                sNoticesManager = new NoticesManager(context);
            }
        }
        return sNoticesManager;
    }

    public void clearMessageTypeNotice() {
        mNM.cancel(20150313);
    }

    /**
     * 未读消息提醒
     */
    public void sendMessageTypeNotice(String account, int chatType) {
        sendMessageTypeNotice(account, chatType, true);
    }

    /**
     * 未读消息提醒
     *
     * @param account
     * @param chatType
     * @param isSetAlarm 该联系人\群\讨论组是否本身有消息设置的提醒；true代表接收并提醒，false代表接收但不提醒
     */
    public void sendMessageTypeNotice(String account, int chatType, boolean isSetAlarm) {
        updateRecentChatList(account, chatType);

        // 是否在上次3秒范围内
        long current = System.currentTimeMillis();
        boolean NotIn3Sec = (current - mLastReceived) > ALARM_INTERVAL;
        if (NotIn3Sec) {
            mLastReceived = current;
        }

        if (NotIn3Sec && shouldAlarm(chatType, isSetAlarm)) {
            playSound(0);
        }

        if (NotIn3Sec && shouldVibrate(chatType, isSetAlarm)) {
            if (AppUtils.isRiginNormalMode()) {//先判断是否是普通模式
                mVibrator.vibrate(VIBRATE_TIME);
            }else {
                AudioManager audioManager = (AudioManager) AppController.getInstance().getSystemService(Context.AUDIO_SERVICE);
                int mode = audioManager.getRingerMode();
                if (mode==AudioManager.RINGER_MODE_VIBRATE){//判断是否是振动模式
                    mVibrator.vibrate(VIBRATE_TIME);
                }
            }
        }

        if (!SystemUtil.isUiRunningFront()) {
            // 如果这次接收到的消息的发送者是新的一个人
            if (!mReceivedUserList.contains(account)) {
                mUnreadUserCount += 1; // 未读联系人数+1
                mReceivedUserList.add(account);
            }
            mUnreadMsgCount += 1; // 未读消息数+1

            showNotify();
        }

        Context context = mContext.getApplicationContext();
        ShortcutBadger.applyCount(context, IconBadgerHelper.count(context)); // 更新桌面图标未读提醒气泡
    }

    private void showNotify() {
        String title = mContextRes.getString(R.string.notification_title);
        String content = mContextRes.getString(R.string.notification_content, mUnreadUserCount, mUnreadMsgCount);
        mNM.cancel(20171212);
        mNM.notify(20171212, createMessageNotification(title, content));
    }

    public boolean shouldAlarm(int chatType, boolean isSetAlarm) {
        boolean flag = false;
        // 获取当前时间是否在设置的免打扰时间段内
        if (AppController.getInstance().mUserSetting == null) {
            //用于解决app被系统回收后，后台重启接收消息时没有声音提醒的bug
            AppController.getInstance().mUserSetting = new SettingsTb(mContext).obtainSettingFromDb();
        }
        Setting setting = AppController.getInstance().mUserSetting;

        boolean isInTimeScope = TimeUtil.isCurrentInTimeScope(setting.getDistractionFree_begin_h(),
                setting.getDistractionFree_begin_m(), setting.getDistractionFree_end_h(),
                setting.getDistractionFree_end_m());
        //获取系统音量判断系统是否静音
        AudioManager mAudioManager = (AudioManager) mContext.getSystemService(mContext.AUDIO_SERVICE);
        boolean isSilentMode = mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT;
        // 首先如果开了声音
        flag = setting.getAudio() != 0;
        // 如果免打扰没开，或者免打扰开了但不在时间段内
        boolean isDistractionFree = setting.getDistractionFree() != 0;
        flag = flag && (!isDistractionFree || !isInTimeScope);

        flag = flag && isSetAlarm && !isSilentMode;  // 是否开启了声音并且系统未静音
        if (chatType != Const.CHAT_TYPE_P2P) //会话类型是群、讨论组会话
        {
            // 群\讨论组是否本身开启了消息提醒
            boolean isGroupAlarm = setting.getAudio_group() != 0;
            flag = flag && isGroupAlarm;
        }

        return flag;
    }

    public boolean shouldVibrate(int chatType, boolean isSetVibrate) {
        boolean flag = false;
        // 获取当前时间是否在设置的免打扰时间段内
        Setting setting = AppController.getInstance().mUserSetting;
        if (setting == null) {
            return false;
        }

        boolean isInTimeScope = TimeUtil.isCurrentInTimeScope(setting.getDistractionFree_begin_h(),
                setting.getDistractionFree_begin_m(), setting.getDistractionFree_end_h(),
                setting.getDistractionFree_end_m());
        // 如果开了振动
        flag = setting.getVibrate() != 0;
        // 如果免打扰没开，或者免打扰开了但不在时间段内
        boolean isDistractionFree = setting.getDistractionFree() != 0;
        flag = flag && (!isDistractionFree || (isDistractionFree && !isInTimeScope));
        // 最后还要判断该联系人\群\讨论组是否本身开启了消息提醒
        boolean isGroupVibrate = setting.getVibrate_group() != 0;
        flag = flag && isGroupVibrate && isSetVibrate;

        return flag;
    }

    /**
     * 刷新最近会话
     *
     * @param account
     * @param chattype
     */
    public void updateRecentChatList(String account, int chattype) {
        Intent intent = new Intent();
        intent.setAction(Const.ACTION_UPDATE_UNREAD_COUNT);
        intent.putExtra("userno", account);
        intent.putExtra("chattype", chattype);
        mContext.sendBroadcast(intent);
    }

    private Notification createMessageNotification(String title, String contenttext) {
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pintent = PendingIntent.getActivity(mContext, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(mContext).setContentTitle(title)
                .setContentText(contenttext)
                .setSmallIcon(mContext.getResources().getIdentifier(AppController.icon_name, "mipmap", mContext.getPackageName()))
                .setLargeIcon(mMSGLargeIcon)
                .setAutoCancel(true)
                .setContentIntent(pintent)
                .build();
        // 消息栏通知没有声音和震动
        // notification.defaults |= Notification.DEFAULT_SOUND;
        // notification.defaults |= Notification.DEFAULT_VIBRATE;
        return notification;
    }

    // 初始化声音池的方法
    public void initSoundPool() {
        mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0); // 创建SoundPool对象
        mStreamId = mSoundPool.load(mContext, R.raw.office, 1);
    }

    // 播放声音的方法
    public void playSound(int loop) { // 获取AudioManager引用
        /*
		 * AudioManager am = (AudioManager) mContext
		 * .getSystemService(Context.AUDIO_SERVICE); // 获取当前音量 float
		 * streamVolumeCurrent = am .getStreamVolume(AudioManager.STREAM_MUSIC);
		 * // 获取系统最大音量 float streamVolumeMax = am
		 * .getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 计算得到播放音量 float
		 * volume = streamVolumeCurrent / streamVolumeMax; //
		 * 调用SoundPool的play方法来播放声音文件 sp.play(currStreamId, volume, volume, 1,
		 * loop, 1.0f);
		 */
        mSoundPool.play(mStreamId, 0.8f, 0.8f, 1, loop, 1.0f);
    }

    /**
     * 销毁当前的NoticeManager对象实例
     */
    public void destoryInstance() {
        synchronized (this) {
            if (mMSGLargeIcon != null) {
                mMSGLargeIcon.recycle();
            }
            if (mSoundPool != null) {
                mSoundPool.release();
            }
            sNoticesManager = null;
        }
    }

}
