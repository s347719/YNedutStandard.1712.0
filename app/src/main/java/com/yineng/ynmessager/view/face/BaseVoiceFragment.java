package com.yineng.ynmessager.view.face;

import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseFragment;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.util.audio.VoiceRecorder;
import com.yineng.ynmessager.view.dialog.ChatVoiceDialog;
import java.lang.ref.WeakReference;

/**
 */
public abstract class BaseVoiceFragment extends BaseFragment
{
    public static final int UPDATE_TIME = 0;
    public ImageView mPreviewVoice;
    public ImageView mDeleteVoice;
    public ImageView mRecordVoice;
    public TextView mVoiceTips;
    public RectF mPreviewVoiceRect;
    public RectF mDeleteVoiceRect;
    public RectF mRecordVoiceRect;
    public ChatVoiceDialog mTalkDialog;
    public boolean isInPreviewRect;
    public boolean isInDeleteRect;
    protected final long mRecordingMinLimit = 1500;
    public int count = 0;
    protected VoiceChatHandler mHandler;
    protected boolean mIsStopped;

    protected static class VoiceChatHandler extends Handler{
        private final WeakReference<BaseVoiceFragment> host;

        private VoiceChatHandler(BaseVoiceFragment host)
        {
            this.host = new WeakReference<>(host);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseVoiceFragment _host = this.host.get();
            if(_host == null) {
                return;
            }

            switch (msg.what) {
                case UPDATE_TIME:
                    if (_host.count < Const.VOICE_MAX_TIME) {
                        _host.count++;
                        _host.updateRecordTime(_host.count);
                        if (_host.count == Const.VOICE_MAX_TIME) {//刚好一分钟
                            _host.showPreviewDialog();
                            break;
                        }
                        sendEmptyMessageDelayed(UPDATE_TIME,1000);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public VoiceRecorder mVoiceRecorder;

    public void updateRecordTime(int count) {
        if (!isInPreviewRect && !isInDeleteRect) {
            if (count > 9) {
                setTips("00:" + count);
                if (count >= 59) {
                    setTips("01:00");
                }
            } else {
                setTips("00:0" + count);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_talk, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mHandler = new VoiceChatHandler(this);

        mVoiceRecorder = VoiceRecorder.newInstance();
        VoiceRecorder.RecordingConfig recordingConfig = new VoiceRecorder.RecordingConfig();
        mVoiceRecorder.init(recordingConfig);

        initView(view);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        mIsStopped = false;
    }

    @Override
    public void onStop()
    {
        super.onStop();

        mIsStopped = true;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        if(mVoiceRecorder.isStart()) {
            mVoiceRecorder.stop();
        }
        mVoiceRecorder.release();
        mTalkDialog.dismiss();
    }

    private void initView(View view) {
        mVoiceTips = (TextView) view.findViewById(R.id.tv_chat_voice_tips);
        mPreviewVoice = (ImageView) view.findViewById(R.id.iv_chat_voice_preview);
        mDeleteVoice = (ImageView) view.findViewById(R.id.iv_chat_voice_delete);
        mRecordVoice = (ImageView) view.findViewById(R.id.iv_chat_voice_record);
        mTalkDialog = new ChatVoiceDialog(mParentActivity, mVoiceRecorder);
        initListener();
    }

    public abstract void initListener();

    /**
     * 显示试听和删除按钮
     * @param show 是否显示
     */
    public void showPreviewAndDeleteView(boolean show) {}

    public void showPreviewDialog() {
        mTalkDialog.show(count);
        resetTimeCounter();
    }

    public void resetTimeCounter(){
        count = 0;
        mHandler.removeMessages(UPDATE_TIME);
        if (viewPagerScrollListener != null) {
            viewPagerScrollListener.onViewPagerScroll(true);
        }
    }

    public void setTips(int resId) {
        mVoiceTips.setText(resId);
    }

    public void setTips(String time) {
        mVoiceTips.setText(time);
    }

    public interface viewPagerScrollable {
        void onViewPagerScroll(boolean scrollable);
    }

    public viewPagerScrollable viewPagerScrollListener;

    public void setViewPagerScrollListener(viewPagerScrollable viewPagerScrollListener) {
        this.viewPagerScrollListener = viewPagerScrollListener;
    }
}
