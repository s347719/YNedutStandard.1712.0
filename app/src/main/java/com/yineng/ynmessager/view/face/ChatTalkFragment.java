package com.yineng.ynmessager.view.face;

import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.p2psession.BaseChatActivity;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.SystemUtil;
import com.yineng.ynmessager.util.audio.AudioPlayer;
import com.yineng.ynmessager.view.dialog.ChatVoiceDialog;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatTalkFragment extends BaseVoiceFragment implements View.OnTouchListener{
    private ChatVoiceDialog mConfirmDialog;
    private long mRecordingStartTime;
    /**
     * 是否录音超时
     */
    private boolean isTimeOut = false;

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mConfirmDialog = new ChatVoiceDialog(mParentActivity, mVoiceRecorder);
  }

  @Override
    public void initListener() {
//        mRecordVoice.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                mRecordVoice.setOnTouchListener(ChatTalkFragment.this);
//                return true;
//            }
//        });
        mRecordVoice.setOnTouchListener(ChatTalkFragment.this);
    }

    /**
     * 录音超时后的操作
     */
    @Override
    public void showPreviewDialog() {
        isTimeOut = true;
        super.showPreviewDialog();
        showPreviewAndDeleteView(false);
    }

    @Override
    public boolean onTouch(final View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //如果在录音了，就不再重复调用开始录音，避免崩溃
                if(mVoiceRecorder.isStart()) {
                    break;
                }

                showPreviewAndDeleteView(true);
                mVoiceRecorder.getRecordingConfig().outputPath = FileUtil.getFileByName(UUID.randomUUID().toString());
                mVoiceRecorder.start();

                mRecordingStartTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                initViewRect();
                isInPreviewRect = mPreviewVoiceRect.contains(event.getRawX(),event.getRawY());
                isInDeleteRect = mDeleteVoiceRect.contains(event.getRawX(),event.getRawY());
                if (isInPreviewRect) {
                    mPreviewVoice.setImageResource(R.mipmap.chat_voice_preview_selected);
                    setTips(R.string.chat_release_preview_voice);
                } else {
                    mPreviewVoice.setImageResource(R.mipmap.chat_voice_preview);
                }
                if (isInDeleteRect) {
                    mDeleteVoice.setImageResource(R.mipmap.chat_voice_delete_selected);
                    setTips(R.string.chat_release_cancel_send_voice);
                } else {
                    mDeleteVoice.setImageResource(R.mipmap.chat_voice_delete);
                }
                updateRecordTime(count);
                break;
            case MotionEvent.ACTION_UP:
                if(!mVoiceRecorder.isStart()) {
                    break;
                }

                //判断按键时间是否过短
                long current = System.currentTimeMillis();
                if (current - mRecordingStartTime < mRecordingMinLimit)
                {
                    //显示提示文字
                    Toast.makeText(v.getContext(), R.string.voiceChatMsg_durationTooShort, Toast.LENGTH_SHORT).show();
                    //稍过一会儿再停止录音，不然马上停止可能崩溃
                    mHandler.postDelayed(new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            //禁用录音按钮
                            v.setEnabled(false);

                            //停止录音
                            mVoiceRecorder.stop();
                            showPreviewAndDeleteView(false);

                            //稍后才能恢复按钮可用，不然如果又立即点按钮开始录音，可能硬件还正在停止中，造成崩溃
                            mHandler.postDelayed(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    v.setEnabled(true);
                                }
                            }, 2500);

                        }
                    }, 600);
                    break;
                }

                mVoiceRecorder.stop();
                showPreviewAndDeleteView(false);
                //录音过程中滑动到左侧的试听图标处，显示“松手试听”文本，松手后不直接发送录制的语音，打开试听界面，
                // 可试听录制的语音（点击播放图标，播放语音），可发送语音（点击“发送”按钮），可取消（点击“取消”则删除录制的语音）
                if (isInPreviewRect) {
                    mTalkDialog.show(count);
                    break;
                }
                //录音过程中滑动到右侧的取消图标处，显示“松手取消发送”文本，松手后删除录制的语音，不发送语音信息
                if (isInDeleteRect) {
                    break;
                }
                //是否超时，超时就不发送语音消息了。没有超时就直接发送;直接松开语音图标，发送录制的语音给对方
                if (isTimeOut) {
                    isTimeOut = false;
                    break;
                }
                //界面是否正在前台显示
                if (SystemUtil.isUiRunningFront()) {
                  //正常情况：直接发送
                  ((BaseChatActivity) getActivity()).sendVoiceMsg(
                      mVoiceRecorder.getRecordingConfig().outputPath);
                } else {
                  //录音过程中，界面退到了后台，比如按home键、来电话等，则弹出试听界面
                  int durationSec = AudioPlayer.getDuration(mParentActivity.getApplicationContext(),
                      mVoiceRecorder.getRecordingConfig().outputPath);
                  durationSec = durationSec / 1000;

                  mConfirmDialog.show(durationSec);
                }
                break;
            default:
                break;
        }
        return true;
    }

  @Override public void onDestroyView() {
    super.onDestroyView();

    mConfirmDialog.dismiss();
  }

  /**
     * 初始化三个按钮的位置
     */
    private void initViewRect() {
        mPreviewVoiceRect = calcViewScreenLocation(mPreviewVoice);
        mDeleteVoiceRect = calcViewScreenLocation(mDeleteVoice);
        mRecordVoiceRect = calcViewScreenLocation(mRecordVoice);
    }

    /**
     * 显示试听和删除按钮
     * @param show 是否显示
     */
    @Override
    public void showPreviewAndDeleteView(boolean show) {
        mPreviewVoice.setImageResource(R.mipmap.chat_voice_preview);
        mDeleteVoice.setImageResource(R.mipmap.chat_voice_delete);
        if (show) {
            count = 0;
            isInPreviewRect = false;
            isInDeleteRect = false;
            mPreviewVoice.setVisibility(View.VISIBLE);
            mDeleteVoice.setVisibility(View.VISIBLE);
            updateRecordTime(count);
            mHandler.sendEmptyMessageDelayed(UPDATE_TIME,1000);
            if (viewPagerScrollListener != null) {
                viewPagerScrollListener.onViewPagerScroll(false);
            }
        } else {
            mVoiceTips.setText(R.string.chat_pin_record_voice);
            mPreviewVoice.setVisibility(View.GONE);
            mDeleteVoice.setVisibility(View.GONE);
            mHandler.removeMessages(UPDATE_TIME);
            if (viewPagerScrollListener != null) {
                viewPagerScrollListener.onViewPagerScroll(true);
            }
        }
    }

    /**
     * 计算指定的 View 在屏幕中的坐标。
     */
    public static RectF calcViewScreenLocation(View view) {
        int[] location = new int[2];
        // 获取控件在屏幕中的位置，返回的数组分别为控件左顶点的 x、y 的值
        view.getLocationOnScreen(location);
        return new RectF(location[0], location[1], location[0] + view.getWidth(),
                location[1] + view.getHeight());
    }

}
