package com.yineng.ynmessager.view.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.p2psession.BaseChatActivity;
import com.yineng.ynmessager.util.audio.AudioPlayer;
import com.yineng.ynmessager.util.audio.VoiceRecorder;

/**
 * Created by yineng on 2016/4/27.
 */
public class ChatVoiceDialog extends AlertDialog implements View.OnClickListener{

    private TextView mSendVoiceTV;
    private TextView mCancelSendVoiceTV;
    private TextView mPreviewVoiceTV;
    private ImageView mPlayButton;
    private BaseChatActivity mParentActivity;
    private VoiceRecorder mVoiceRecorder;
    private AudioPlayer mAudioPlayer;

    public ChatVoiceDialog(@NonNull Context context, @NonNull VoiceRecorder voiceRecorder) {
        super(context, R.style.ChatVoiceDialogTheme);
        mParentActivity = (BaseChatActivity) context;
        mVoiceRecorder = voiceRecorder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindow();
        setContentView(R.layout.fragment_chat_preview_record);
        findViews();
        initListener();
    }

    private void initWindow() {
        setCanceledOnTouchOutside(false);

        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog)
            {
                mAudioPlayer = AudioPlayer.newInstance();
            }
        });

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                mAudioPlayer.release();
            }
        });
    }

    private void findViews() {
        mPreviewVoiceTV = (TextView) findViewById(R.id.tv_chat_preview_voice_tips);
        mSendVoiceTV = (TextView) findViewById(R.id.tv_chat_preview_send);
        mCancelSendVoiceTV = (TextView) findViewById(R.id.tv_chat_preview_cancel_send);
        mPlayButton = (ImageView) findViewById(R.id.iv_chat_preview_voice_play_stop);
    }

    private void initListener() {
        mSendVoiceTV.setOnClickListener(this);
        mCancelSendVoiceTV.setOnClickListener(this);
        mPlayButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_chat_preview_send:
                mParentActivity.sendVoiceMsg(mVoiceRecorder.getRecordingConfig().outputPath);
                dismiss();
                break;
            case R.id.tv_chat_preview_cancel_send:
                dismiss();
                break;
            case R.id.iv_chat_preview_voice_play_stop:
                //如果没有试听时，此时背景为播放图片，点击后就播放，替换成停止图片
                //如果正在试听，点击停止图片，则停止播放，替换成播放图片
                //如果试听结束，则将停止图片替换为播放图片
                if (mAudioPlayer.isPlaying()) {//点击了正在播放的音频，停止播放
                    mPlayButton.setImageResource(R.mipmap.chat_voice_preview_record);
                    mAudioPlayer.stop();
                } else {//点击了未播放的音频，播放音频
                    mPlayButton.setImageResource(R.mipmap.chat_voice_record_stop);
                    AudioPlayer.PlayingConfig tempPlayConfig = new AudioPlayer.PlayingConfig(mVoiceRecorder.getRecordingConfig().outputPath);
                    tempPlayConfig.onCompletionListener = new MediaPlayer.OnCompletionListener () {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            //如果播放完了，就替换播放的图片
                            mPlayButton.setImageResource(R.mipmap.chat_voice_preview_record);
                        }
                    };
                    mAudioPlayer.start(tempPlayConfig);
                }
                break;
            default:
                break;
        }
    }

    public void show(int count) {
        super.show();
        if (count > 9) {
            if (count >= 59) {
                mPreviewVoiceTV.setText("01:00");
            } else {
                mPreviewVoiceTV.setText("00:" + count);
            }
        } else {
            mPreviewVoiceTV.setText("00:0" + count);
        }
        mPlayButton.setImageResource(R.mipmap.chat_voice_preview_record);
    }
}
