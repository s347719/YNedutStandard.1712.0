package com.yineng.ynmessager.util.audio;

import android.media.MediaRecorder;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.yineng.ynmessager.util.L;

import java.io.File;
import java.io.IOException;

/**
 * Created by 贺毅柳 on 2016/4/21 17:01.
 */
public class VoiceRecorder
{
    private static final String TAG = "VoiceRecorder";
    private MediaRecorder mRecorder = null;
    private RecordingConfig mRecordingConfig = null;
    private boolean mIsStart = false;

    /**
     * create a new Instance
     * @return
     */
    @NonNull
    public static VoiceRecorder newInstance()
    {
        return new VoiceRecorder();
    }

    private VoiceRecorder()
    {
        mRecorder = new MediaRecorder();
    }

    /**
     * initialize some objects, must be called first before other operations
     *
     * @param recordingConfig
     */
    public void init(@NonNull RecordingConfig recordingConfig)
    {
        if (mRecordingConfig != null)
        {
            throw new UnsupportedOperationException("Do not repeat init() method before calling release()");
        }
        mRecordingConfig = recordingConfig;
    }

    /**
     * release resources and this object will be unavailable after you invoke this method. Recording will be stopped if it's still running
     */
    public void release()
    {
        mRecorder.release();
        mRecorder = null;
        mRecordingConfig = null;
        mIsStart = false;
    }

    /**
     * start recording<br/>
     * <b>NOTE: must not be called if recorder has already started</b>
     */
    public void start()
    {
        if (mRecordingConfig == null)
        {
            throw new IllegalStateException("has NOT been initialized yet");
        }

        mRecorder.setAudioSource(mRecordingConfig.audioSource);
        mRecorder.setOutputFormat(mRecordingConfig.outputFormat);
        mRecorder.setAudioEncoder(mRecordingConfig.audioEncoder);
        mRecorder.setOutputFile(mRecordingConfig.outputPath.getPath());
        mRecorder.setMaxDuration(mRecordingConfig.maxDuration);
        mRecorder.setOnInfoListener(mRecordingConfig.onInfoListener);

        try
        {
            mRecorder.prepare();
        } catch (IOException e)
        {
            L.e(TAG, e.getMessage(), e);
        }

        mRecorder.start();
        mIsStart = true;
    }

    /**
     * stop recording<br/>
     * <b>NOTE: must be called after recorder get started</b>
     */
    public void stop()
    {
        if (mRecordingConfig == null)
        {
            throw new IllegalStateException("has NOT been initialized yet");
        }

        mRecorder.stop();
        mRecorder.reset();

        mIsStart = false;
    }

    public boolean isStart()
    {
        return mIsStart;
    }

    public RecordingConfig getRecordingConfig()
    {
        return mRecordingConfig;
    }

    /**
     * Config info class for {@link VoiceRecorder}
     */
    public static final class RecordingConfig
    {
        /**
         * a value comes from {@link android.media.MediaRecorder.AudioSource}<br/>
         * default value: {@linkplain android.media.MediaRecorder.AudioSource#MIC MIC}
         */
        public int audioSource;
        /**
         * a value comes from {@link android.media.MediaRecorder.OutputFormat}<br/>
         * default value: {@linkplain android.media.MediaRecorder.OutputFormat#MPEG_4 MPEG_4}
         */
        public int outputFormat;
        /**
         * a value comes from {@link android.media.MediaRecorder.AudioEncoder}<br/>
         * default value: {@linkplain android.media.MediaRecorder.AudioEncoder#AAC AAC}
         */
        public int audioEncoder;
        /**
         * a path that contains filename to store the recorded audio file<br/>
         * default value: external SD card path with filename <b>"audiorecordtest.mp3"</b>
         */
        @NonNull
        public File outputPath;
        /**
         * see {@link MediaRecorder#setMaxDuration(int)}<br/>
         * default value: -1, means disable the duration limit
         */
        public int maxDuration;
        /**
         * see {@link android.media.MediaRecorder.OnInfoListener}<br/>
         * and see also {@link MediaRecorder#setMaxDuration(int)}<br/>
         * default value: null
         */
        public MediaRecorder.OnInfoListener onInfoListener;

        public RecordingConfig()
        {
            audioSource = MediaRecorder.AudioSource.MIC;
            outputFormat = MediaRecorder.OutputFormat.MPEG_4;
            audioEncoder = MediaRecorder.AudioEncoder.AAC;
            outputPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "audiorecordtest.mp3");
            maxDuration = -1;
            onInfoListener = null;
        }
    }

}
