package com.yineng.ynmessager.view.face;


import android.support.v4.app.Fragment;
import android.view.View;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.util.FileUtil;

import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatRecordFragment extends BaseVoiceFragment {

    @Override
    public void initListener() {
        mRecordVoice.setImageResource(R.mipmap.chat_voice_record);
        mRecordVoice.setTag(R.mipmap.chat_voice_record);
        setTips(R.string.chat_press_record_voice);

        mRecordVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Integer integer = (Integer) mRecordVoice.getTag();
                integer = integer == null ? 0 : integer;
                switch (integer) {
                    case R.mipmap.chat_voice_record:
                        if(mVoiceRecorder.isStart()) {
                            break;
                        }

                        mRecordVoice.setImageResource(R.mipmap.chat_voice_record_stop);
                        mRecordVoice.setTag(R.mipmap.chat_voice_record_stop);
                        startRecordVoice();
                        disableQuickClick(v);
                        break;
                    case R.mipmap.chat_voice_record_stop:
                        if(mVoiceRecorder.isStart()) {
                            showPreviewDialog();
                        }
                        disableQuickClick(v);
                        break;
                    default:
                        mRecordVoice.setImageResource(R.mipmap.chat_voice_record);
                        mRecordVoice.setTag(R.mipmap.chat_voice_record);
                        break;
                }
            }

            private void disableQuickClick(final View v)
            {
                v.setEnabled(false);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run()
                    {
                        v.setEnabled(true);
                    }
                },mRecordingMinLimit);
            }

        });
    }

    @Override
    public void onStop()
    {
        super.onStop();

        if(mVoiceRecorder.isStart()) {
            showPreviewDialog();
        }
    }

    private void startRecordVoice() {
        count = 0;
        updateRecordTime(count);
        mHandler.sendEmptyMessageDelayed(UPDATE_TIME,1000);
        mVoiceRecorder.getRecordingConfig().outputPath = FileUtil.getFileByName(UUID.randomUUID().toString());
        mVoiceRecorder.start();
        if (viewPagerScrollListener != null) {
            viewPagerScrollListener.onViewPagerScroll(false);
        }
    }

    @Override
    public void showPreviewDialog() {
        mVoiceRecorder.stop();
        super.showPreviewDialog();
        mRecordVoice.setImageResource(R.mipmap.chat_voice_record);
        mRecordVoice.setTag(R.mipmap.chat_voice_record);
        setTips(R.string.chat_press_record_voice);
    }
}
