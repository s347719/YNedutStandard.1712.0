package com.yineng.ynmessager.activity.TransmitActivity;

import android.content.Context;
import android.net.Uri;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.RecentChat;
import com.yineng.ynmessager.bean.contact.ContactGroupUser;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.view.face.FaceConversionUtil;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 转发页面中最近聊天信息的适配器
 * Created by 舒欢
 * Created time: 2017/4/18
 */
public class TransmitSessionAdapter extends BaseAdapter {


    private LinkedList<RecentChat> mSessionDatas;
    private Context mContext;
    private ContactOrgDao mContactOrgDao;

    public TransmitSessionAdapter( Context context)
    {
        mContext = context;
        mContactOrgDao = new ContactOrgDao(context);
    }


    public LinkedList<RecentChat> getmSessionDatas()
    {
        return mSessionDatas;
    }


    public void setmSessionDatas(LinkedList<RecentChat> mSessionDatas)
    {
        this.mSessionDatas = mSessionDatas;
    }


    @Override
    public int getCount() {
        return mSessionDatas!=null ? mSessionDatas.size():0;
    }

    @Override
    public Object getItem(int position) {
        return mSessionDatas.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        RecentChat recentChat = mSessionDatas.get(position);
        int chatType = recentChat.getChatType();
        if(convertView == null)
        {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.transmitsession_layout,null);
            viewHolder = new ViewHolder();
            viewHolder.mSessionPhoto = (CircleImageView)convertView.findViewById(R.id.iv_main_session_item_headicon);
            viewHolder.mSessionDateTime = (TextView)convertView.findViewById(R.id.tv_main_session_item_datetime);
            viewHolder.mSessionContent = (TextView)convertView.findViewById(R.id.tv_main_session_item_content);
            viewHolder.mSessionTitle = (TextView)convertView.findViewById(R.id.tv_main_session_item_title);
            convertView.setTag(viewHolder);
        }else
        {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        int avatar ;
        //当前是两人会话
        if (chatType== Const.CHAT_TYPE_P2P){
            //设置头像
            File userIcon = FileUtil.getAvatarByName(recentChat.getUserNo());
            if (userIcon == null || !userIcon.exists()) {
                User user = mContactOrgDao.queryUserInfoByUserNo(recentChat.getUserNo());
                if (user!=null&&user.getGender() == 1){
                    avatar = R.mipmap.session_p2p_men;
                }else if (user!=null&&user.getGender() == 2){
                    avatar = R.mipmap.session_p2p_woman;
                }
                else {
                    avatar = R.mipmap.session_no_sex;
                }
                viewHolder.mSessionPhoto.setImageResource(avatar);
            } else {
                viewHolder.mSessionPhoto.setImageURI(Uri.fromFile(userIcon));
            }

        }else if (chatType == Const.CHAT_TYPE_GROUP){
            //当前是群组.
            avatar = R.mipmap.session_group;
            viewHolder.mSessionPhoto.setImageResource(avatar);
        }
        else if (chatType == Const.CHAT_TYPE_DIS){
            //当前是讨论组
            ContactGroupUser group = mContactOrgDao.getContactGroupUserById(recentChat.getUserNo(), LastLoginUserSP.getLoginUserNo(mContext), Const.CHAT_TYPE_DIS);
            if (group!=null&&group.getRole()==10) {//自己创建的讨论组
                avatar = R.mipmap.session_creat_discus;
            } else {//自己加入的讨论组
                avatar = R.mipmap.session_join_discus;
            }
            viewHolder.mSessionPhoto.setImageResource(avatar);
        }


    // 获取会话中最后一次消息的聊天时间
        if (!TextUtils.isEmpty(recentChat.getDateTime())) {
            Date date = TimeUtil.convertStringDate(recentChat.getDateTime());
            // 转换成与当前时间的关系文字
            String relative = TimeUtil.getTimeRelationFromNow2(mContext,date);
            viewHolder.mSessionDateTime.setText(relative);
        } else {
            viewHolder.mSessionDateTime.setText("");
        }

        // 设置显示草稿还是消息内容
        String draft = recentChat.getDraft();
        String mContentStr;
        if(TextUtils.isEmpty(draft)) // 如果没有草稿
        {
            if((recentChat.getChatType() == Const.CHAT_TYPE_DIS || recentChat.getChatType() == Const.CHAT_TYPE_GROUP)
                    && recentChat.getSenderName() != null)
            {
                mContentStr = recentChat.getSenderName() + ": " + recentChat.getContent();
            }else
            {
                mContentStr = recentChat.getContent();
            }
        }else
        // 如果有草稿
        {
            mContentStr = RecentChat.DRAFT_PREFIX + draft;
        }

        // 添加表情
        SpannableString spannableString;
        if(recentChat.getSpannableString() != null)
        {
            spannableString = recentChat.getSpannableString();
        }else
        {
            // 对内容做处理
            spannableString = FaceConversionUtil.getInstace().handlerRecentChatContent(mContext,viewHolder.mSessionContent, mContentStr);
            recentChat.setSpannableString(spannableString);
        }
        viewHolder.mSessionContent.setText(spannableString);

        viewHolder.mSessionTitle.setText(recentChat.getTitle());



        return convertView;
    }


    private final class ViewHolder
    {
        CircleImageView mSessionPhoto;
        TextView mSessionDateTime;
        TextView mSessionTitle;
        TextView mSessionContent;
    }


}
