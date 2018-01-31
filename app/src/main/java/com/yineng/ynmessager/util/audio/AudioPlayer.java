package com.yineng.ynmessager.util.audio;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yineng.ynmessager.util.L;

import java.io.File;
import java.io.IOException;

/**
 * Created by 贺毅柳 on 2016/4/26 10:18.
 */
public class AudioPlayer
{
    private static final String TAG = "AudioPlayer";

    private MediaPlayer mMediaPlayer;

    private AudioPlayer()
    {
        mMediaPlayer = new MediaPlayer();
    }

    /**
     * create a new instance of AudioPlayer
     *
     * @return
     */
    @NonNull
    public static AudioPlayer newInstance()
    {
        return new AudioPlayer();
    }

    /**
     * start playing, the current playing will be interrupted if it's playing
     *
     * @param config a non-null playback file object you want
     */
    public void start(@NonNull PlayingConfig config)
    {
        checkState();
        stop();

        try
        {
            mMediaPlayer.setDataSource(config.path.getPath());
            mMediaPlayer.setOnCompletionListener(config.onCompletionListener);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e)
        {
            L.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * interrupt current playing
     */
    public void stop()
    {
        checkState();

        mMediaPlayer.stop();
        mMediaPlayer.reset();
    }

    public boolean isPlaying()
    {
        checkState();

        return mMediaPlayer.isPlaying();
    }

    private void checkState()
    {
        if (mMediaPlayer == null)
        {
            throw new IllegalStateException("unable to do operations now! (have you called release() method?)");
        }
    }

    /**
     * release resources and this object will be unavailable after you invoke this method
     */
    public void release()
    {
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    public static class PlayingConfig
    {
        @NonNull public File path;
        @Nullable public MediaPlayer.OnCompletionListener onCompletionListener;

        public PlayingConfig(@NonNull File path)
        {
            this.path = path;
            this.onCompletionListener = null;
        }

    }

    /**
     * Gets the duration of the file.
     *
     * @param context
     * @param file
     * @return
     */
    public static int getDuration(@NonNull Context context, @NonNull File file)
    {
        final MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.fromFile(file));
        int duration = mediaPlayer.getDuration();
        mediaPlayer.release();
        return duration;
    }
}
