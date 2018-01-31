//***************************************************************
//*    2015-8-31  下午4:08:49
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.p2psession;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.os.AsyncTaskCompat;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.dissession.DisChatActivity;
import com.yineng.ynmessager.activity.groupsession.GroupChatActivity;
import com.yineng.ynmessager.activity.picker.SendingListener;
import com.yineng.ynmessager.activity.picker.file.FolderViewerActivity;
import com.yineng.ynmessager.activity.picker.file.SendFileTask;
import com.yineng.ynmessager.activity.picker.image.SendImageTask;
import com.yineng.ynmessager.activity.picker.picture.GalleryActivity;
import com.yineng.ynmessager.activity.picker.voice.SendVoiceTask;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.imgpicker.ImageFile;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.util.ImageUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.audio.AudioPlayer;
import com.yineng.ynmessager.view.face.FaceRelativeLayout;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import id.zelory.compressor.Compressor;

/**
 * 三大聊天界面的之父
 *
 * @author 贺毅柳
 */
public abstract class BaseChatActivity extends BaseActivity
        implements FaceRelativeLayout.OtherButtonsClickListener
{
    // ----------------------------------------------------------
    /**
     * XMPP连接管理类实例
     */
    protected XmppConnectionManager mXmppConnManager;

    /**
     * 当前对方的聊天帐号（该属性在子类实现中被初始化）
     */
    public String mChatUserNum;

    /**
     * 拍照后返回的照片路径
     */
    protected String mCameraPhotoPath;

    /**
     * 图片、文件发送过程中的方法回调接口实例
     */
    protected SendingListener mSendingListener;

    /**
     * 语音过程中的方法回调接口实例
     */
    protected SendingListener mVoiceSendingListener;

    /**
     * context menu的显示状态
     */
    private boolean isContextMenuShowing = false;
    private AudioPlayer mAudioPlayer;

    /**
     * 正在播放的音频
     */
    private File mFilePlaying;
    /**
     * 正在播放的音频的视图
     */
    private View playingVoiceAnimView;
    // ----------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mXmppConnManager = XmppConnectionManager.getInstance();
        mSendingListener = initSendingListener();
        mVoiceSendingListener = initVoiceSendingListener();
        mAudioPlayer = AudioPlayer.newInstance();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        menu.setHeaderTitle(R.string.p2pChatActivity_contextMenuTitle);
        menu.add(Menu.NONE, 0, Menu.NONE, R.string.p2pChatActivity_copyChatMsg);
        menu.add(Menu.NONE, 1, Menu.NONE, R.string.p2pChatActivity_deleteChatMsg);
        menu.add(Menu.NONE,2,1,R.string.p2pChatActivity_transmitImage);

        isContextMenuShowing = true;
        L.i(mTag, "ContextMenu created");
    }

    @Override
    public void onContextMenuClosed(Menu menu)
    {
        super.onContextMenuClosed(menu);

        isContextMenuShowing = false;
        L.i(mTag, "ContextMenu closed");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // 根据当前的Activity来设定chatType参数值
        int chatType = Const.CHAT_TYPE_P2P;
        if (this instanceof GroupChatActivity) {
            chatType = Const.CHAT_TYPE_GROUP;
        } else if (this instanceof DisChatActivity) {
            chatType = Const.CHAT_TYPE_DIS;
        }

        switch (requestCode)
        {
            case GalleryActivity.ACTIVITY_REQUEST_CODE: // 图库选择返回
                if (resultCode == Activity.RESULT_OK)
                {
                    // 发送图片
                    List<ImageFile> imageFileList = (ArrayList<ImageFile>) data.getSerializableExtra("selectedImages");
                        Compressor compressor = new Compressor.Builder(getApplicationContext())
                                .setMaxWidth(1280)
                                .setMaxHeight(1280)
                                .build();

                        List<ImageFile> compressedImageFileList = new ArrayList<>();
                        for (ImageFile image : imageFileList) {
                            File compressed = compressor.compressToFile(image);
                            compressedImageFileList.add(ImageFile.fromFile(compressed));
                        }

                        imageFileList = compressedImageFileList;

                    // 启动发送图片的AsyncTask
                    SendImageTask sendImageTask = new SendImageTask(mXmppConnManager, chatType, mChatUserNum);
                    sendImageTask.setSendImageListener(mSendingListener);
                    AsyncTaskCompat.executeParallel(sendImageTask, imageFileList);
                }
                break;
            case FolderViewerActivity.ACTIVITY_REQUEST_CODE: // 文件选择返回
                if (resultCode == RESULT_OK)
                {
                    HashSet<File> selectedFiles = (HashSet<File>) data.getSerializableExtra("SelectedFiles");
                    SendFileTask sendFileTask = new SendFileTask(mXmppConnManager, chatType, mChatUserNum);
                    sendFileTask.setSendFileListener(mSendingListener);
                    AsyncTaskCompat.executeParallel(sendFileTask, selectedFiles);
                }
                break;
            case Const.REQUEST_TAKE_PHOTO: // 相机拍照返回
                String photoPath = ImageUtil.getimage(mCameraPhotoPath, this);
                if (!StringUtils.isEmpty(photoPath))
                {
                    // 发送图片
                    ArrayList<ImageFile> imageFileList = new ArrayList<>();
                    imageFileList.add(new ImageFile(mCameraPhotoPath));
                    // 启动发送图片的AsyncTask
                    SendImageTask sendImageTask = new SendImageTask(mXmppConnManager, chatType, mChatUserNum);
                    sendImageTask.setSendImageListener(mSendingListener);
                    AsyncTaskCompat.executeParallel(sendImageTask, imageFileList);
                }
                break;
        }
    }

    public void sendVoiceMsg(File voiceFile)
    {
        final int chatType;
        if (this instanceof P2PChatActivity)
        {
            chatType = Const.CHAT_TYPE_P2P;
        } else if (this instanceof GroupChatActivity)
        {
            chatType = Const.CHAT_TYPE_GROUP;
        } else
        {
            chatType = Const.CHAT_TYPE_DIS;
        }

        Set<File> voiceFiles = new HashSet<>();
        voiceFiles.add(voiceFile);

        SendVoiceTask sendVoiceTask = new SendVoiceTask(XmppConnectionManager.getInstance(), chatType, mChatUserNum);
        sendVoiceTask.setSendFileListener(mVoiceSendingListener);
        AsyncTaskCompat.executeParallel(sendVoiceTask, voiceFiles);
    }

    public void playAudio(@NonNull File voiceFile) {
        if(mAudioPlayer.isPlaying() && ObjectUtils.equals(mFilePlaying,voiceFile))
        {
            mAudioPlayer.stop();
        }else
        {
            AudioPlayer.PlayingConfig config = new AudioPlayer.PlayingConfig(voiceFile);
            mAudioPlayer.start(config);
        }

        mFilePlaying = voiceFile;
    }

    /**
     * 播放音频文件
     * @param voiceFile 音频文件
     * @param v 对应的语音消息视图
     * @param sendType 消息来源：0发送 1接收
     */
    public void playAudio(@NonNull File voiceFile, View v, final int sendType) {
        if (playingVoiceAnimView != null) {
            if ((int)playingVoiceAnimView.getTag() == BaseChatMsgEntity.COM_MSG) {
                playingVoiceAnimView.setBackgroundResource(R.drawable.chat_voice_left_anim3);
            } else {
                playingVoiceAnimView.setBackgroundResource(R.drawable.chat_voice_right_anim3);
            }
            playingVoiceAnimView = null;
        }
        playingVoiceAnimView = v.findViewById(R.id.iv_voice_bg);
        playingVoiceAnimView.setTag(sendType);
        if(mAudioPlayer.isPlaying() && ObjectUtils.equals(mFilePlaying,voiceFile))
        {
            if (sendType == BaseChatMsgEntity.COM_MSG) {
                playingVoiceAnimView.setBackgroundResource(R.drawable.chat_voice_left_anim3);
            } else {
                playingVoiceAnimView.setBackgroundResource(R.drawable.chat_voice_right_anim3);
            }
            mAudioPlayer.stop();
        }else
        {
            if (sendType == BaseChatMsgEntity.COM_MSG) {
                playingVoiceAnimView.setBackgroundResource(R.drawable.chat_play_voice_left_msg_animation);
            } else {
                playingVoiceAnimView.setBackgroundResource(R.drawable.chat_play_voice_right_msg_animation);
            }
            AnimationDrawable playVoiceAnimation = (AnimationDrawable) playingVoiceAnimView.getBackground();
            playVoiceAnimation.start();

            AudioPlayer.PlayingConfig config = new AudioPlayer.PlayingConfig(voiceFile);
            config.onCompletionListener = new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (sendType == BaseChatMsgEntity.COM_MSG) {
                        playingVoiceAnimView.setBackgroundResource(R.drawable.chat_voice_left_anim3);
                    } else {
                        playingVoiceAnimView.setBackgroundResource(R.drawable.chat_voice_right_anim3);
                    }
                }
            };
            mAudioPlayer.start(config);
        }

        mFilePlaying = voiceFile;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAudioPlayer.release();
    }

    @Override
    public void onSendImageClick()
    {
        startActivityForResult(new Intent(this, GalleryActivity.class), GalleryActivity.ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onSendFileClick()
    {
        startActivityForResult(new Intent(this, FolderViewerActivity.class), FolderViewerActivity.ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onSendCameraImageClick()
    {
        mCameraPhotoPath = ImageUtil.takePhoto(this);
    }

    /**
     * 初始化 {@link SendingListener} 实例并返回
     *
     * @return
     */
    protected abstract SendingListener initSendingListener();

    /**
     * 初始化 {@link SendingListener} 语音发送回调实例并返回
     *
     * @return
     */
    protected abstract SendingListener initVoiceSendingListener();

    /**
     * 返回Context Menu 的显示状态
     * @return
     */
    public final boolean isContextMenuShowing()
    {
        return isContextMenuShowing;
    }
}
