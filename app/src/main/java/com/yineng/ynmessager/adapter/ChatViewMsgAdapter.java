package com.yineng.ynmessager.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.contact.ContactPersonInfoActivity;
import com.yineng.ynmessager.activity.p2psession.BaseChatActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.groupsession.GroupChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.bean.p2psession.MessageFileEntity;
import com.yineng.ynmessager.bean.p2psession.MessageVoiceEntity;
import com.yineng.ynmessager.bean.p2psession.P2PChatMsgEntity;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.P2PChatMsgDao;
import com.yineng.ynmessager.db.dao.DisGroupChatDao;
import com.yineng.ynmessager.db.dao.GroupChatDao;
import com.yineng.ynmessager.service.DownloadService;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.ImageUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.SystemUtil;
import com.yineng.ynmessager.util.TextUtil;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.view.face.FaceConversionUtil;
import com.yineng.ynmessager.view.face.gif.AnimatedImageSpan;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 *
 */
public class ChatViewMsgAdapter extends BaseAdapter {
    public static final String TAG = "ChatViewMsgAdapter";

    //会话类型1为2人会话，2为讨论组会话，3为群会话，4为广播
    private final int mChatType;
    private float mMinVoiceItemWith;
    private float mMaxVoiceItemWith;
    private List<Object> coll = new ArrayList<>();
    private LayoutInflater mInflater;
    private Context mContext;
    private ContactOrgDao mContactOrgDao;

    private String mChatUserNum;
    /**
     * 消息数据库工具
     */
    private P2PChatMsgDao mP2PChatMsgDao;

    private GroupChatDao mGroupChatDao;

    private DisGroupChatDao mDisGroupChatDao;

    /**
     * 点击失败的消息重发
     */
    private OnClickListener msgReclickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (msgResendListener != null) {
                if (mChatType == Const.CHAT_TYPE_P2P && (v.getTag()) instanceof P2PChatMsgEntity) {
                    P2PChatMsgEntity p2pEntity = (P2PChatMsgEntity) v.getTag();
                    p2pEntity.setIsSuccess(BaseChatMsgEntity.SEND_ING);
                    msgResendListener.onResend(p2pEntity);
                } else {
                    GroupChatMsgEntity groupEntity = (GroupChatMsgEntity) v.getTag();
                    groupEntity.setIsSuccess(BaseChatMsgEntity.SEND_ING);
                    msgResendListener.onResend(groupEntity);
                }
            }
        }
    };

    public interface msgResendListener {
        void onResend(Object obj);
    }

    private msgResendListener msgResendListener;

    public void setOnMsgResendListener(msgResendListener onMsgResendListener) {
        msgResendListener = onMsgResendListener;
    }

    /**
     * 进入联系人详细资料页
     */
    private OnClickListener contactInfoViewListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            //  Auto-generated method stub
            BaseChatMsgEntity entity = (BaseChatMsgEntity) v.getTag();
            User mUserInfo;
            if (entity.getIsSend() == BaseChatMsgEntity.COM_MSG) {
                mUserInfo = mContactOrgDao.queryUserInfoByUserNo(entity.getChatUserNo());
            } else {
                mUserInfo = AppController.getInstance().mSelfUser;
            }
            if (mUserInfo == null) {
                ToastUtil.toastAlerMessageBottom(mContext, "暂无此人,请手动更新联系人.", 800);
                return;
            }
            startPersonInfoActivity(mUserInfo);
        }
    };

    public ChatViewMsgAdapter(Context context, int chatType, String chatNum) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mChatUserNum = chatNum;
        mContactOrgDao = new ContactOrgDao(mContext);
        mChatType = chatType;
        switch (chatType) {
            case Const.CHAT_TYPE_P2P:
                mP2PChatMsgDao = new P2PChatMsgDao(mContext);
                break;
            case Const.CHAT_TYPE_GROUP:
                mGroupChatDao = new GroupChatDao(mContext);
                break;
            case Const.CHAT_TYPE_DIS:
                mDisGroupChatDao = new DisGroupChatDao(mContext);
                break;
            default:
                break;
        }
        mMinVoiceItemWith = context.getResources().getDimension(R.dimen.contactPersonInfo_avatarImg_size);
        mMaxVoiceItemWith = context.getResources().getDimension(R.dimen.slidingmenu_offset);
    }

    public void setData(List<Object> coll) {
        this.coll = coll;
    }

    public List<Object> getData() {
        return coll;
    }

    @Override
    public int getCount() {
        return coll.size();
    }

    @Override
    public Object getItem(int position) {
        return coll.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        BaseChatMsgEntity entity = (BaseChatMsgEntity) coll.get(position);
        if (entity.getIsSend() == BaseChatMsgEntity.COM_MSG) {
            return BaseChatMsgEntity.COM_MSG;
        } else {
            return BaseChatMsgEntity.TO_MSG;
        }

    }


    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final BaseChatMsgEntity entity = (BaseChatMsgEntity) coll.get(position);
        MessageBodyEntity body;
        if (entity.getMessage() != null) {
            body = JSON.parseObject(entity.getMessage(), MessageBodyEntity.class);
        } else {
            convertView = mInflater.inflate(R.layout.chatting_item_msg_text_left, parent, false);
            return convertView;
        }
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            if (entity.getIsSend() == BaseChatMsgEntity.COM_MSG) {
                convertView = mInflater.inflate(R.layout.chatting_item_msg_text_left, parent, false);
                viewHolder.ivVoiceUnreadFlag = (ImageView) convertView.findViewById(R.id.iv_voice_unread_flag);
                viewHolder.item_session_online_type = (ImageView) convertView.findViewById(R.id.item_session_online_type);
            } else {
                convertView = mInflater.inflate(R.layout.chatting_item_msg_text_right, parent, false);
                viewHolder.pbFileUpLoadPgBar = (ProgressBar) convertView.findViewById(R.id.pb_file_upload_bar);
                viewHolder.linResource = (LinearLayout) convertView.findViewById(R.id.lin_source_type);
            }
            viewHolder.pbVoiceProgressBar = (ProgressBar) convertView.findViewById(R.id.pb_voice_download_bar);
            viewHolder.pbFileLoadPgBar = (ProgressBar) convertView.findViewById(R.id.pb_file_download_bar);
            viewHolder.tvFileNotice = (TextView) convertView.findViewById(R.id.tv_file_notice);
            viewHolder.tv_file_sendstate = (TextView) convertView.findViewById(R.id.tv_file_sendstate);
            viewHolder.rlFileContent = (RelativeLayout) convertView.findViewById(R.id.rl_filecontent);
            viewHolder.tvFileName = (TextView) convertView.findViewById(R.id.tv_file_name);
            viewHolder.tvFileSize = (TextView) convertView.findViewById(R.id.tv_file_size);
            viewHolder.iv_file_thumb = (ImageView) convertView.findViewById(R.id.iv_file_thumb);
            //接收/发送语音
            viewHolder.rlVoiceContent = (RelativeLayout) convertView.findViewById(R.id.rl_voiceContent_layout);
            viewHolder.flVoiceContentBg = (FrameLayout) convertView.findViewById(R.id.rl_voice_container_length);
            viewHolder.ivVoiceBg = (ImageView) convertView.findViewById(R.id.iv_voice_bg);
            viewHolder.tvVoiceTime = (TextView) convertView.findViewById(R.id.tv_voice_time_length);
            viewHolder.tvSenderName = (TextView) convertView.findViewById(R.id.tv_chat_sender_name);
            viewHolder.ivSenderIcon = (CircleImageView) convertView.findViewById(R.id.iv_userhead);
            viewHolder.tvSendTime = (TextView) convertView.findViewById(R.id.tv_sendtime);
            viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_chatcontent);
            viewHolder.tvSendStatus = (TextView) convertView.findViewById(R.id.tv_chat_tag);
            viewHolder.mLayout = (RelativeLayout) convertView.findViewById(R.id.chat_item_layout);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvContent.setVisibility(View.GONE);
        viewHolder.rlFileContent.setVisibility(View.GONE);
        viewHolder.rlVoiceContent.setVisibility(View.GONE);

        switch (entity.getMessageType()) {
            case BaseChatMsgEntity.FILE:
                viewHolder.mLayout.setVisibility(View.VISIBLE);
                viewHolder.tv_file_sendstate.setVisibility(View.GONE);
                viewHolder.rlFileContent.setVisibility(View.VISIBLE);
                viewHolder.tvContent.setVisibility(View.GONE);
                if (entity.getIsSend() == BaseChatMsgEntity.COM_MSG) {
                    handlerFileReceived(viewHolder, entity, body);
                } else {
                    handleFileSend(viewHolder, entity, body);
                }
                break;
            case BaseChatMsgEntity.MESSAGE:
            case BaseChatMsgEntity.IMAGE:
                viewHolder.mLayout.setVisibility(View.VISIBLE);
                viewHolder.tv_file_sendstate.setVisibility(View.GONE);
                if (viewHolder.rlFileContent != null) {
                    viewHolder.rlFileContent.setVisibility(View.GONE);
                }
                viewHolder.tvContent.setVisibility(View.VISIBLE);
                SpannableString spannableString;
                // L.e("getview == "+viewHolder.tvSendTime.getTag());
                if (entity.getSpannableString() != null) {
                    spannableString = entity.getSpannableString();
                    destoryTempGifMemory(spannableString, false);
                }
//				MessageBodyEntity body = JSON.parseObject(entity.getMessage(),MessageBodyEntity.class);
                // 对内容做处理
                spannableString = FaceConversionUtil.getInstace().handlerContent(mChatUserNum, mContext, viewHolder.tvContent,
                        body, mHandler, entity.getPacketId(), mChatType);
                entity.setSpannableString(spannableString);
                viewHolder.tvContent.setText(spannableString);
                break;
            case BaseChatMsgEntity.AUDIO_FILE://语音
                viewHolder.mLayout.setVisibility(View.VISIBLE);
                viewHolder.tv_file_sendstate.setVisibility(View.GONE);
                viewHolder.rlVoiceContent.setVisibility(View.VISIBLE);
                if (entity.getIsSend() == BaseChatMsgEntity.COM_MSG) {//接收
                    handleVoiceReceived(viewHolder, entity, body);
                } else {//发送
                    handleVoiceSend(viewHolder, entity, body);
                }
                break;
            //显示文件传输给对方，对方接收的状态
            case BaseChatMsgEntity.FILE_IQ_STATE:
                viewHolder.mLayout.setVisibility(View.GONE);
                viewHolder.tv_file_sendstate.setVisibility(View.VISIBLE);
                viewHolder.tv_file_sendstate.setText(body.getContent());
                break;
            default:
                break;
        }

        if (entity.isShowTime()) {
            viewHolder.tvSendTime.setVisibility(View.VISIBLE);
            Date sendTime = new Date(Long.valueOf(entity.getTime()));
            viewHolder.tvSendTime.setText(TimeUtil.getTimeRelationFromNow2(mContext, sendTime));
        } else {
            viewHolder.tvSendTime.setVisibility(View.GONE);
        }

        //entity.getMessageType() 不为12  不显示文件接收状态视图 此时才做其他项数据的处理
        if (entity.getMessageType() != BaseChatMsgEntity.FILE_IQ_STATE) {
            viewHolder.tvSendStatus.setVisibility(View.GONE);
            //如果是接收多人会话的消息时，要显示其发送人的名称
            if (entity.getIsSend() == BaseChatMsgEntity.COM_MSG) {
                if (mChatType != Const.CHAT_TYPE_P2P) {
                    viewHolder.tvSenderName.setVisibility(View.VISIBLE);
                    viewHolder.tvSenderName.setText(body.getSendName());
                }
            } else {
                switch (entity.getIsSuccess()) {
                    case BaseChatMsgEntity.SEND_SUCCESS:
                        viewHolder.pbFileUpLoadPgBar.setVisibility(View.GONE);
                        break;
                    case BaseChatMsgEntity.SEND_FAILED:
                        viewHolder.pbFileUpLoadPgBar.setVisibility(View.GONE);
                        viewHolder.tvSendStatus.setBackgroundResource(R.mipmap.chat_resend_icon);
                        viewHolder.tvSendStatus.setVisibility(View.VISIBLE);
                        viewHolder.tvSendStatus.setTag(coll.get(position));
                        viewHolder.tvSendStatus.setOnClickListener(msgReclickListener);
                        break;
                    case BaseChatMsgEntity.SEND_ING:
                        viewHolder.pbFileUpLoadPgBar.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }


            File userIcon = null;
            //设置头像
            if (mChatType == Const.CHAT_TYPE_P2P && entity.getIsSend() == BaseChatMsgEntity.TO_MSG) {//两人会话且是自己发送的内容
                userIcon = FileUtil.getAvatarByName(LastLoginUserSP.getLoginUserNo(mContext));
            } else {
                userIcon = FileUtil.getAvatarByName(entity.getChatUserNo());
            }
            User user;
            if(entity.getIsSend() == BaseChatMsgEntity.COM_MSG){
                user = mContactOrgDao.queryUserInfoByUserNo(entity.getChatUserNo());
            }else {
                user = mContactOrgDao.queryUserInfoByUserNo(LastLoginUserSP.getLoginUserNo(mContext));
            }
            if (!userIcon.exists()) {
                if (entity.getIsSend() == BaseChatMsgEntity.COM_MSG) {//接收到消息设置别人的头像
                    int avatar = -1;
                    if (user != null && user.getGender() == 1) {//对方是男性
                        avatar = R.mipmap.session_p2p_men;
                    } else if (user != null && user.getGender() == 2) {//对方是女性
                        avatar = R.mipmap.session_p2p_woman;
                    } else {
                        avatar = R.mipmap.session_p2p_men;
                    }
                    viewHolder.ivSenderIcon.setImageResource(avatar);
                } else {//自己发送消息设置自己的头像
                    int avatar = -1;
                    if (user != null && user.getGender() == 1) {//男性
                        avatar = R.mipmap.session_p2p_men;
                    } else if (user != null && user.getGender() == 2) {//女性
                        avatar = R.mipmap.session_p2p_woman;
                    } else {
                        avatar = R.mipmap.session_p2p_men;
                    }
                    viewHolder.ivSenderIcon.setImageResource(avatar);
                }
            } else {
                if (user.getUserStatus()==0){
                    viewHolder.ivSenderIcon.setImageBitmap(ImageUtil.convertToBlackWhite(BitmapFactory.decodeFile(userIcon.getAbsolutePath())));
                } else {
                    viewHolder.ivSenderIcon.setImageURI(Uri.fromFile(userIcon));
                }
            }

            viewHolder.ivSenderIcon.setTag(entity);
            viewHolder.ivSenderIcon.setOnClickListener(contactInfoViewListener);
        }
        return convertView;
    }

    private void handleVoiceSend(ViewHolder viewHolder, BaseChatMsgEntity entity, MessageBodyEntity body) {
        if (body.getVoice() == null) return;
        //根据时长修改消息气泡长度
        int time = body.getVoice().getTime() / 1000;
        ViewGroup.LayoutParams lParams = viewHolder.flVoiceContentBg.getLayoutParams();
        lParams.width = (int) (mMinVoiceItemWith + mMaxVoiceItemWith / 60 * time);
        viewHolder.flVoiceContentBg.setLayoutParams(lParams);
        MessageVoiceEntity downLoadVoiceFileinfo = body.getVoice();
        String fileName = downLoadVoiceFileinfo.getId();
        File localFile = FileUtil.getFileByName(fileName);
        if (localFile.exists() && entity.getIsSuccess() == BaseChatMsgEntity.DOWNLOAD_NOT_YET) {
            entity.setIsSuccess(BaseChatMsgEntity.DOWNLOAD_SUCCESS);
        }
        switch (entity.getIsSuccess()) {
            case BaseChatMsgEntity.DOWNLOAD_SUCCESS:
                viewHolder.pbVoiceProgressBar.setVisibility(View.GONE);
                viewHolder.tvVoiceTime.setVisibility(View.VISIBLE);
                break;
            case BaseChatMsgEntity.DOWNLOAD_NOT_YET:
                viewHolder.pbVoiceProgressBar.setVisibility(View.VISIBLE);
                viewHolder.tvVoiceTime.setVisibility(View.GONE);
                Intent downLoadIntent = new Intent(mContext, DownloadService.class);
                downLoadIntent.putExtra("downloadFileType", Const.MESSENGERFILE);
                downLoadIntent.putExtra("downloadFileBean", entity);
                mContext.startService(downLoadIntent);
                break;
            case BaseChatMsgEntity.DOWNLOAD_FAILED:
                viewHolder.tvVoiceTime.setBackgroundResource(R.mipmap.chat_resend_icon);
                viewHolder.tvVoiceTime.setText("");
                viewHolder.tvVoiceTime.setVisibility(View.VISIBLE);
                viewHolder.tvVoiceTime.setTag(entity);
                viewHolder.tvVoiceTime.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final BaseChatMsgEntity entity = (BaseChatMsgEntity) v.getTag();
                        Intent downLoadIntent = new Intent(mContext, DownloadService.class);
                        downLoadIntent.putExtra("downloadFileType", Const.MESSENGERFILE);
                        downLoadIntent.putExtra("downloadFileBean", entity);
                        mContext.startService(downLoadIntent);
                    }
                });
                break;
            default:
                break;
        }
        viewHolder.tvVoiceTime.setText(body.getVoice().getTime() / 1000 + "\"");
        viewHolder.flVoiceContentBg.setTag(entity);
        viewHolder.flVoiceContentBg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final BaseChatMsgEntity entity = (BaseChatMsgEntity) v.getTag();
                //点击播放音频文件，并把状态改为已读
                updateDatabaseMsgVoiceStatus(entity, BaseChatMsgEntity.IS_READED);
                notifyDataSetChanged();
                //点击播放中的音频，应停止播放
                MessageBodyEntity body = JSON.parseObject(entity.getMessage(), MessageBodyEntity.class);
                MessageVoiceEntity downLoadVoiceFileinfo = body.getVoice();
                String fileName = downLoadVoiceFileinfo.getId();
                File localFile = FileUtil.getFileByName(fileName);
                if (localFile.exists()) {
                    ((BaseChatActivity) mContext).playAudio(localFile, v, BaseChatMsgEntity.TO_MSG);
                }
            }
        });
    }

    private void handleVoiceReceived(ViewHolder viewHolder, BaseChatMsgEntity entity, MessageBodyEntity body) {
        if (body.getVoice() != null) {
            if (entity.getIsSuccess() < BaseChatMsgEntity.DOWNLOAD_NOT_YET) {
                updateDatabaseMsgStatus(entity, BaseChatMsgEntity.DOWNLOAD_NOT_YET);
            } else {
                //如果该文件仍是下载中状态，且下载服务的正在下载的文件集合为空(说明服务还未创建)，
                // 说明该文件已经没有下载了，该把状态置为下载失败的状态 huyi 2016.1.15
                if (entity.getIsSuccess() == BaseChatMsgEntity.DOWNLOAD_ING) {
                    if (DownloadService.mDownloadMsgBeans == null) {
                        updateDatabaseMsgStatus(entity, BaseChatMsgEntity.DOWNLOAD_FAILED);
                    }
                }
            }
            viewHolder.pbVoiceProgressBar.setVisibility(View.GONE);
            viewHolder.tvVoiceTime.setVisibility(View.GONE);
            viewHolder.ivVoiceUnreadFlag.setVisibility(View.GONE);
            viewHolder.tvVoiceTime.setBackgroundColor(Color.TRANSPARENT);
            viewHolder.tvVoiceTime.setOnClickListener(null);
            viewHolder.flVoiceContentBg.setOnClickListener(null);
            //根据时长修改消息气泡长度
            int time = body.getVoice().getTime() / 1000;
            ViewGroup.LayoutParams lParams = viewHolder.flVoiceContentBg.getLayoutParams();
            lParams.width = (int) (mMinVoiceItemWith + mMaxVoiceItemWith / 60 * time);
            viewHolder.flVoiceContentBg.setLayoutParams(lParams);

            switch (entity.getIsSuccess()) {
                case BaseChatMsgEntity.DOWNLOAD_NOT_YET:
                    viewHolder.pbVoiceProgressBar.setVisibility(View.VISIBLE);
                    Intent downLoadIntent = new Intent(mContext, DownloadService.class);
                    downLoadIntent.putExtra("downloadFileType", Const.MESSENGERFILE);
                    downLoadIntent.putExtra("downloadFileBean", entity);
                    mContext.startService(downLoadIntent);
                    break;
                case BaseChatMsgEntity.DOWNLOAD_SUCCESS:
                    viewHolder.pbVoiceProgressBar.setVisibility(View.GONE);
                    if (entity.getIsReaded() == BaseChatMsgEntity.IS_NOT_READED) {
                        viewHolder.ivVoiceUnreadFlag.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.ivVoiceUnreadFlag.setVisibility(View.GONE);
                    }
                    viewHolder.tvVoiceTime.setVisibility(View.VISIBLE);
                    viewHolder.tvVoiceTime.setText(body.getVoice().getTime() / 1000 + "\"");
                    viewHolder.flVoiceContentBg.setTag(entity);
                    viewHolder.flVoiceContentBg.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final BaseChatMsgEntity entity = (BaseChatMsgEntity) v.getTag();
                            //点击播放音频文件，并把状态改为已读
                            updateDatabaseMsgVoiceStatus(entity, BaseChatMsgEntity.IS_READED);
                            notifyDataSetChanged();
                            //点击播放中的音频，应停止播放
                            MessageBodyEntity body = JSON.parseObject(entity.getMessage(), MessageBodyEntity.class);
                            MessageVoiceEntity downLoadVoiceFileinfo = body.getVoice();
                            String fileName = downLoadVoiceFileinfo.getId();
                            File localFile = FileUtil.getFileByName(fileName);

                            if (localFile.exists()) {
                                ((BaseChatActivity) mContext).playAudio(localFile, v, BaseChatMsgEntity.COM_MSG);
                            }
                        }
                    });
                    break;
                case BaseChatMsgEntity.DOWNLOAD_FAILED:
//                    viewHolder.pbVoiceProgressBar.setVisibility(View.GONE);
                    viewHolder.tvVoiceTime.setBackgroundResource(R.mipmap.chat_resend_icon);
                    viewHolder.tvVoiceTime.setText("");
                    viewHolder.tvVoiceTime.setVisibility(View.VISIBLE);
                    viewHolder.tvVoiceTime.setTag(entity);
                    viewHolder.tvVoiceTime.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final BaseChatMsgEntity entity = (BaseChatMsgEntity) v.getTag();
                            Intent downLoadIntent = new Intent(mContext, DownloadService.class);
                            downLoadIntent.putExtra("downloadFileType", Const.MESSENGERFILE);
                            downLoadIntent.putExtra("downloadFileBean", entity);
                            mContext.startService(downLoadIntent);
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 处理文件接收的方法
     *
     * @param viewHolder 布局对象
     * @param entity     消息实体
     * @param body       消息内容
     */
    private void handlerFileReceived(ViewHolder viewHolder, BaseChatMsgEntity entity, MessageBodyEntity body) {
        if (body.getFiles().size() > 0) {
            MessageFileEntity mFileInfo = body.getFiles().get(0);
            viewHolder.tvFileName.setText(mFileInfo.getName());
            L.e("MSG", "接收文件名：" + mFileInfo.getName());
            switch (TextUtil.matchTheFileType(mFileInfo.getName())) {
                case 1:
                    viewHolder.iv_file_thumb.setImageResource(R.mipmap.jpg);
                    break;
                case 2:
                    viewHolder.iv_file_thumb.setImageResource(R.mipmap.video);
                    break;
                case 3:
                    viewHolder.iv_file_thumb.setImageResource(R.mipmap.music);
                    break;
                case 4:
                    viewHolder.iv_file_thumb.setImageResource(R.mipmap.word);
                    break;
                case 5:
                    viewHolder.iv_file_thumb.setImageResource(R.mipmap.excel);
                    break;
                case 6:
                    viewHolder.iv_file_thumb.setImageResource(R.mipmap.pdf);
                    break;
                case 7:
                    viewHolder.iv_file_thumb.setImageResource(R.mipmap.ppt);
                    break;
                case 8:
                    viewHolder.iv_file_thumb.setImageResource(R.mipmap.rar);
                    break;
                case 9:
                    viewHolder.iv_file_thumb.setImageResource(R.mipmap.file);
                    break;
                default:
                    viewHolder.iv_file_thumb.setImageResource(R.mipmap.file);
                    break;
            }
            long mFileInfoSize = Long.parseLong(mFileInfo.getSize());
            String fileSizeStr = FileUtil.FormatFileSize(mFileInfoSize);
            viewHolder.tvFileSize.setText(fileSizeStr);
            viewHolder.pbFileLoadPgBar.setVisibility(View.GONE);

            if (entity.getIsSuccess() < BaseChatMsgEntity.DOWNLOAD_NOT_YET) {
                updateDatabaseMsgStatus(entity, BaseChatMsgEntity.DOWNLOAD_NOT_YET);
            } else {
                //如果该文件仍是下载中状态，且下载服务的正在下载的文件集合为空(说明服务还未创建)，
                // 说明该文件已经没有下载了，该把状态置为下载失败的状态 huyi 2016.1.15
                if (entity.getIsSuccess() == BaseChatMsgEntity.DOWNLOAD_ING) {
                    if (DownloadService.mDownloadMsgBeans == null) {
                        updateDatabaseMsgStatus(entity, BaseChatMsgEntity.DOWNLOAD_FAILED);
                    }
                }
            }

            switch (entity.getIsSuccess()) {
                case BaseChatMsgEntity.DOWNLOAD_NOT_YET:
                    viewHolder.tvFileNotice.setText("下载");
                    viewHolder.tvFileNotice.setTextColor(0xFF12b5b0);
                    viewHolder.tvFileSize.setText(fileSizeStr);
                    break;
                case BaseChatMsgEntity.DOWNLOAD_SUCCESS:
                    viewHolder.tvFileNotice.setText("打开");
                    viewHolder.tvFileNotice.setTextColor(0xFF12b5b0);
                    viewHolder.tvFileSize.setText(fileSizeStr);
                    break;
                case BaseChatMsgEntity.DOWNLOAD_FAILED:
                    viewHolder.tvFileNotice.setText("下载失败,请重试");
                    viewHolder.tvFileNotice.setTextColor(Color.RED);
                    viewHolder.tvFileSize.setText(fileSizeStr);
                    break;
                case BaseChatMsgEntity.DOWNLOAD_ING:
                    viewHolder.pbFileLoadPgBar.setVisibility(View.VISIBLE);
                    if (mFileInfoSize != 0) {
                        viewHolder.pbFileLoadPgBar.setProgress(entity.getReceiveProgress());
                        String showProgress = FileUtil.FormatFileSize((mFileInfoSize / 100) * entity.getReceiveProgress()) + "/" + fileSizeStr;
                        viewHolder.tvFileSize.setText(showProgress);
                        viewHolder.tvFileNotice.setText("");
                        viewHolder.tvFileNotice.setTextColor(0xFF12b5b0);
                        L.e("BaseChatMsgEntity.DOWNLOAD_ING " + entity.getReceiveProgress());
                    }
                    break;
                default:
                    break;
            }
            viewHolder.rlFileContent.setTag(entity);
            viewHolder.rlFileContent.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View paramView) {
                    final BaseChatMsgEntity entity = (BaseChatMsgEntity) paramView.getTag();
                    switch (entity.getIsSuccess()) {
                        case BaseChatMsgEntity.DOWNLOAD_FAILED:
                        case BaseChatMsgEntity.DOWNLOAD_NOT_YET:
                            Intent downLoadIntent = new Intent(mContext, DownloadService.class);
                            downLoadIntent.putExtra("downloadFileType", Const.MESSENGERFILE);
                            downLoadIntent.putExtra("downloadFileBean", entity);
                            mContext.startService(downLoadIntent);
                            break;
                        case BaseChatMsgEntity.DOWNLOAD_SUCCESS:
                            L.e("BaseChatMsgEntity.DOWNLOAD_SUCCESS ");
                            MessageBodyEntity body = JSON.parseObject(entity.getMessage(), MessageBodyEntity.class);
                            MessageFileEntity downLoadFileinfo = body.getFiles().get(0);
                            String fileName = downLoadFileinfo.getName();
                            File localFile = FileUtil.getFileByName(fileName);
                            if (!SystemUtil.execLocalFile(mContext, localFile)) {
                                Toast.makeText(mContext, R.string.groupSharedFiles_noAppsForThisFile, Toast.LENGTH_LONG).show();
                            }
                            break;
                        default:
                            break;
                    }
                }
            });
        }
    }

    private void handleFileSend(ViewHolder viewHolder, BaseChatMsgEntity entity, MessageBodyEntity body) {
        if (body.getFiles().size() == 0) {
            return;
        }

        MessageFileEntity fileInfo = body.getFiles().get(0);
        viewHolder.tvFileName.setText(fileInfo.getName());
        L.e("MSG", "发送文件名：" + fileInfo.getName());
        switch (TextUtil.matchTheFileType(fileInfo.getName())) {
            case 1:
                viewHolder.iv_file_thumb.setImageResource(R.mipmap.jpg);
                break;
            case 2:
                viewHolder.iv_file_thumb.setImageResource(R.mipmap.video);
                break;
            case 3:
                viewHolder.iv_file_thumb.setImageResource(R.mipmap.music);
                break;
            case 4:
                viewHolder.iv_file_thumb.setImageResource(R.mipmap.word);
                break;
            case 5:
                viewHolder.iv_file_thumb.setImageResource(R.mipmap.excel);
                break;
            case 6:
                viewHolder.iv_file_thumb.setImageResource(R.mipmap.pdf);
                break;
            case 7:
                viewHolder.iv_file_thumb.setImageResource(R.mipmap.ppt);
                break;
            case 8:
                viewHolder.iv_file_thumb.setImageResource(R.mipmap.rar);
                break;
            case 9:
                viewHolder.iv_file_thumb.setImageResource(R.mipmap.file);
                break;
            default:
                viewHolder.iv_file_thumb.setImageResource(R.mipmap.file);
                break;
        }
        long mFileInfoSize = Long.parseLong(fileInfo.getSize());
        String fileSizeStr = FileUtil.FormatFileSize(mFileInfoSize);
        viewHolder.tvFileSize.setText(fileSizeStr);
        //判断文件终端
        if (!StringUtils.isEmpty(body.getResource()) && body.getResource().equals(MessageBodyEntity.SOURCE_PC)) {
            viewHolder.linResource.setVisibility(View.VISIBLE);
        } else {
            viewHolder.linResource.setVisibility(View.GONE);
        }

        MessageFileEntity downLoadFileinfo = body.getFiles().get(0);
        String sdFilePath = downLoadFileinfo.getSdcardPath();
        String downFilePath = FileUtil.getFileByName(downLoadFileinfo.getName()).getPath();
        //判断打开地址是本地地址还是下载地址
        File localFile = null;
        if (!StringUtils.isEmpty(downFilePath)) {
            localFile = new File(downFilePath);
            if (!localFile.exists()) {
                if (!StringUtils.isEmpty(sdFilePath)) {
                    localFile = new File(sdFilePath);
                }
            }
        }
        if (localFile != null && localFile.exists() && entity.getIsSuccess() == BaseChatMsgEntity.DOWNLOAD_NOT_YET) {
            entity.setIsSuccess(BaseChatMsgEntity.DOWNLOAD_SUCCESS);
        } else if (!localFile.exists()&&entity.getIsSuccess()!=BaseChatMsgEntity.DOWNLOAD_ING) {
            entity.setIsSuccess(BaseChatMsgEntity.DOWNLOAD_NOT_YET);
        }

        switch (entity.getIsSuccess()) {
            case BaseChatMsgEntity.DOWNLOAD_NOT_YET:
                viewHolder.pbFileLoadPgBar.setVisibility(View.GONE);
                viewHolder.tvFileNotice.setText("下载");
                viewHolder.tvFileNotice.setTextColor(0xFF6299e9);
                viewHolder.tvFileSize.setText(fileSizeStr);
                break;
            case BaseChatMsgEntity.DOWNLOAD_SUCCESS:
                viewHolder.pbFileLoadPgBar.setVisibility(View.GONE);
                viewHolder.tvFileNotice.setText("打开");
                viewHolder.tvFileNotice.setTextColor(0xFF6299e9);
                viewHolder.tvFileSize.setText(fileSizeStr);
                break;
            case BaseChatMsgEntity.SEND_SUCCESS:
                viewHolder.pbFileLoadPgBar.setVisibility(View.GONE);
                viewHolder.tvFileNotice.setText("已发送");
                viewHolder.tvFileNotice.setTextColor(mContext.getResources().getColor(R.color.common_hint_text_color));
                viewHolder.tvFileSize.setText(fileSizeStr);
                break;
            case BaseChatMsgEntity.SEND_FAILED:
                viewHolder.pbFileLoadPgBar.setVisibility(View.GONE);
                viewHolder.tvFileNotice.setText("发送失败,请重试");
                viewHolder.tvFileNotice.setTextColor(Color.RED);
                viewHolder.tvFileSize.setText(fileSizeStr);
                break;
            case BaseChatMsgEntity.DOWNLOAD_FAILED:
                viewHolder.pbFileLoadPgBar.setVisibility(View.GONE);
                viewHolder.tvFileNotice.setText("下载失败,请重试");
                viewHolder.tvFileNotice.setTextColor(Color.RED);
                viewHolder.tvFileSize.setText(fileSizeStr);
                break;
            case BaseChatMsgEntity.SEND_ING:
                viewHolder.tvFileNotice.setText("发送中");
                viewHolder.tvFileNotice.setTextColor(mContext.getResources().getColor(R.color.common_hint_text_color));
                break;
            case BaseChatMsgEntity.DOWNLOAD_ING:
                viewHolder.pbFileLoadPgBar.setVisibility(View.VISIBLE);
                if (mFileInfoSize != 0) {
                    viewHolder.pbFileLoadPgBar.setProgress(entity.getReceiveProgress());
                    String downloadedfileSizeStr = FileUtil.FormatFileSize(entity.getReceivedBytes());
                    viewHolder.tvFileSize.setText(downloadedfileSizeStr + "/" + fileSizeStr);
                    viewHolder.tvFileNotice.setText("");
                    viewHolder.tvFileNotice.setTextColor(0xFF6299e9);
                }
                break;
        }

        //点击打开文件
        viewHolder.rlFileContent.setTag(entity);
        viewHolder.rlFileContent.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View paramView) {
                BaseChatMsgEntity entity = (BaseChatMsgEntity) paramView.getTag();
                MessageBodyEntity body = JSON.parseObject(entity.getMessage(), MessageBodyEntity.class);
                MessageFileEntity downLoadFileinfo = body.getFiles().get(0);
                String sdFilePath = downLoadFileinfo.getSdcardPath();
                String downFilePath = FileUtil.getFileByName(downLoadFileinfo.getName()).getPath();
                //判断打开地址是本地地址还是下载地址
                File localFile = null;
                if (!StringUtils.isEmpty(downFilePath)) {
                    localFile = new File(downFilePath);
                    if (!localFile.exists()) {
                        if (!StringUtils.isEmpty(sdFilePath)) {
                            localFile = new File(sdFilePath);
                        }
                    }
                }
                //判断有没有这个文件，如果没有就判断是否下载
                if (localFile.exists()) {
                    if (!SystemUtil.execLocalFile(mContext, localFile)) {
                        Toast.makeText(mContext, R.string.groupSharedFiles_noAppsForThisFile, Toast.LENGTH_LONG).show();
                    }
                } else if (entity.getIsSuccess() == BaseChatMsgEntity.DOWNLOAD_NOT_YET || entity.getIsSuccess() == BaseChatMsgEntity.DOWNLOAD_FAILED) {
                    Intent downLoadIntent = new Intent(mContext, DownloadService.class);
                    downLoadIntent.putExtra("downloadFileType", Const.MESSENGERFILE);
                    downLoadIntent.putExtra("downloadFileBean", entity);
                    mContext.startService(downLoadIntent);
                }
            }
        });
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            //  Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    notifyDataSetChanged();
                    break;

                default:
                    break;
            }
        }
    };

    /**
     * 退出界面时
     * 回收bitmap暂用的内存空间
     *
     * @param recycleBmp 是否销毁
     */
    public void destroyGifValue(boolean recycleBmp) {
        List<Object> mAdapterList = getData();
        if (mAdapterList != null) {
            SpannableString tempSpan;
            for (Object chatObj : mAdapterList) {
                if (chatObj instanceof P2PChatMsgEntity) {
                    P2PChatMsgEntity p2pEntity = (P2PChatMsgEntity) chatObj;
                    tempSpan = p2pEntity.getSpannableString();
                } else {
                    GroupChatMsgEntity groupEntity = (GroupChatMsgEntity) chatObj;
                    tempSpan = groupEntity.getSpannableString();
                }
                if (tempSpan != null) {
                    destoryTempGifMemory(tempSpan, recycleBmp);
                }
            }
        }
    }

    /**
     * 销毁gif内存
     */
    public void destoryTempGifMemory(SpannableString spannableString, boolean recycleBmp) {
        AnimatedImageSpan[] tem = spannableString.getSpans(0, spannableString.length() - 1, AnimatedImageSpan.class);
        for (AnimatedImageSpan animatedImageSpan : tem) {
            animatedImageSpan.recycleBitmaps(recycleBmp);
        }
        spannableString.removeSpan(tem);
    }

    class ViewHolder {
        ProgressBar pbFileUpLoadPgBar;
        ProgressBar pbFileLoadPgBar;
        TextView tvFileNotice;
        TextView tvFileSize;
        TextView tvFileName;
        RelativeLayout rlFileContent;
        TextView tvSendTime;
        TextView tv_file_sendstate;
        TextView tvContent;
        TextView tvSendStatus;
        RelativeLayout mLayout;
        CircleImageView ivSenderIcon;
        TextView tvSenderName;
        LinearLayout linResource;

        RelativeLayout rlVoiceContent;
        FrameLayout flVoiceContentBg;
        ImageView ivVoiceBg, iv_file_thumb;
        TextView tvVoiceTime;
        ImageView ivVoiceUnreadFlag;
        ProgressBar pbVoiceProgressBar;
        ImageView item_session_online_type;
    }

    /**
     * 打开个人资料页
     *
     * @param mUser 用户
     */
    private void startPersonInfoActivity(User mUser) {
        Intent infoIntent = new Intent(mContext, ContactPersonInfoActivity.class);
        infoIntent.putExtra("contactInfo", mUser);
        mContext.startActivity(infoIntent);
    }

    /**
     * 更改数据库中该条记录的状态
     *
     * @param entity 记录
     */
    private void updateDatabaseMsgStatus(BaseChatMsgEntity entity, int downloadStatus) {
        entity.setIsSuccess(downloadStatus);
        switch (mChatType) {
            case Const.CHAT_TYPE_P2P:
                mP2PChatMsgDao.updateMsgSendStatus(entity.getPacketId(), downloadStatus);
                break;
            case Const.CHAT_TYPE_GROUP:
                mGroupChatDao.updateMsgSendStatus(entity.getPacketId(), downloadStatus);
                break;
            case Const.CHAT_TYPE_DIS:
                mDisGroupChatDao.updateMsgSendStatus(entity.getPacketId(), downloadStatus);
                break;
            default:
                break;
        }
    }

    private void updateDatabaseMsgVoiceStatus(BaseChatMsgEntity entity, int isReaded) {
        entity.setIsReaded(isReaded);
        switch (mChatType) {
            case Const.CHAT_TYPE_P2P:
                mP2PChatMsgDao.updateMsgVoiceStatus(entity.getPacketId(), isReaded);
                break;
            case Const.CHAT_TYPE_GROUP:
                mGroupChatDao.updateMsgVoiceStatus(entity.getPacketId(), isReaded);
                break;
            case Const.CHAT_TYPE_DIS:
                mDisGroupChatDao.updateMsgVoiceStatus(entity.getPacketId(), isReaded);
                break;
            default:
                break;
        }
    }
}
