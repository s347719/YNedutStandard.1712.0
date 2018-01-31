package com.yineng.ynmessager.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.settings.AboutActivity;
import com.yineng.ynmessager.activity.settings.AdditionalFunctionActivity;
import com.yineng.ynmessager.activity.settings.DownloadedFilesActivity;
import com.yineng.ynmessager.activity.settings.PerferSettingActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.service.DownloadService;
import com.yineng.ynmessager.service.XmppConnService;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.ToastUtil;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by yn on 2017/6/12.
 */

public class MyFragment extends BaseFragment implements View.OnClickListener {

    private CircleImageView header;
    private TextView name, post, department, position, sendCard, view_cancle_message;
    private RelativeLayout rl_setting, rl_function, rl_file, rl_about, rl_exit, view_send_message, view_copy_message;
    private LinearLayout ll_my_fragment;

    private ContactOrgDao mContactOrgDao;
    private User user;
    private LastLoginUserSP lastLoginUserSP;
    private AlertDialog mLogoutConfirmDialog;
    private PopupWindow mPopupWindow;
    private View mPhonePopView, v_popwin_bg, contentView;

    private String userNo;//用户编号
    private String userName;//用户姓名
    private int userType;//用户类型
    private int gender;//用户性别
    private String orgName;//用户主组织机构
    private String userPost; //用户职务
    private String email;//用户邮箱
    private String telephone;//用户电话

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.main_my_layout, container, false);
        SharedPreferences userInfo = getActivity().getSharedPreferences("userPost", 0);
        userNo = userInfo.getString("userNo", "");
        userName = LastLoginUserSP.getInstance(getActivity()).getUserName();
        userType = LastLoginUserSP.getInstance(getActivity()).getUserType();
        gender = userInfo.getInt("gender", -1);
        orgName = userInfo.getString("orgName", "");
        userPost = userInfo.getString("post", "");
        email = userInfo.getString("email", "");
        telephone = userInfo.getString("telephone", "");

        return contentView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContactOrgDao = new ContactOrgDao(getActivity());
        user = mContactOrgDao.queryUserInfoByUserNo(LastLoginUserSP.getLoginUserNo(getActivity()));
        lastLoginUserSP = LastLoginUserSP.getInstance(getActivity());

        ll_my_fragment = (LinearLayout) view.findViewById(R.id.ll_my_fragment);
        header = (CircleImageView) view.findViewById(R.id.my_center_img);
        name = (TextView) view.findViewById(R.id.my_center_name);
        post = (TextView) view.findViewById(R.id.my_center_post);
        department = (TextView) view.findViewById(R.id.my_center_department);
        position = (TextView) view.findViewById(R.id.my_center_position);

        sendCard = (TextView) view.findViewById(R.id.my_center_send_card);
        rl_setting = (RelativeLayout) view.findViewById(R.id.my_center_setting);
        rl_function = (RelativeLayout) view.findViewById(R.id.my_center_function);
        rl_file = (RelativeLayout) view.findViewById(R.id.my_center_file);
        rl_about = (RelativeLayout) view.findViewById(R.id.my_center_about);
        rl_exit = (RelativeLayout) view.findViewById(R.id.my_center_exit);

        sendCard.setOnClickListener(this);
        rl_setting.setOnClickListener(this);
        rl_function.setOnClickListener(this);
        rl_file.setOnClickListener(this);
        rl_about.setOnClickListener(this);
        rl_exit.setOnClickListener(this);

        mPhonePopView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_my_popwindow, null);
        view_send_message = (RelativeLayout) mPhonePopView.findViewById(R.id.view_send_message);
        view_copy_message = (RelativeLayout) mPhonePopView.findViewById(R.id.view_copy_message);
        view_cancle_message = (TextView) mPhonePopView.findViewById(R.id.view_cancle_message);
        v_popwin_bg = mPhonePopView.findViewById(R.id.v_popwin_bg);
        view_cancle_message.setOnClickListener(this);
        view_send_message.setOnClickListener(this);
        view_copy_message.setOnClickListener(this);
        v_popwin_bg.setOnClickListener(this);

        mLogoutConfirmDialog = new AlertDialog.Builder(getActivity()).create();
        initInfo();
    }

    /**
     * 初始化用户信息
     */
    private void initInfo() {
        //姓名
        name.setText(userName);
        //判断职务是否为空，如果为空就显示老师或者学生等
        if (userType == 1) {//教师
            if (!StringUtils.isEmpty(userPost) && !userPost.equals("无")) {
                post.setText(userPost);
            } else {
                post.setText("教职工");
            }
        } else {//学生
            if (!StringUtils.isEmpty(userPost)) {
                post.setText(userPost);
            } else {
                post.setText("学生");
            }
        }

        if (!StringUtils.isEmpty(orgName)) {
            department.setText(orgName);
        }

//      设置头像
        File userIcon = FileUtil.getAvatarByName(userNo);
        if (userIcon == null || !userIcon.exists()) {
            if (gender == 1) {//男
                header.setImageResource(R.mipmap.session_p2p_men);
            } else if (gender == 2) {//女
                header.setImageResource(R.mipmap.session_p2p_woman);
            } else {
                header.setImageResource(R.mipmap.session_no_sex);
            }
        } else {
            header.setImageURI(Uri.fromFile(userIcon));
        }

//        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_center_send_card://发送名片
                if (!StringUtils.isEmpty(userNo)) {
                    showPhonePopWindow();
                }
                break;
            case R.id.v_popwin_bg://点击popwindow以外的地方
                mPopupWindow.dismiss();
                break;
            case R.id.view_send_message://点击发送名片
                sendContactCard();
                break;
            case R.id.view_cancle_message://点击取消发送名片弹出框
                mPopupWindow.dismiss();
                break;
            case R.id.view_copy_message://点击复制名片

                //判断当前是男是女
                String sex;
                if (gender == 1) {
                    sex = "男";
                } else if (gender == 2) {
                    sex = "女";
                } else {
                    sex = "保密";
                }
                //判断当前机构是否有名字
                String org = "";
                String work = "";
                if (userType == 1) {//教师
                    if (!StringUtils.isEmpty(userPost) && !userPost.equals("无")) {
                        org = "  职称：" + userPost + "\n";
                    } else {
                        org = "  职称：" + "教职工" + "\n";
                    }
                    work = "  部门：" + orgName + "\n";
                } else {//学生
                    if (!StringUtils.isEmpty(userPost)) {
                        org = "  职称：" + userPost + "\n";
                    } else {
                        org = "  职称：" + "学生" + "\n";
                    }
                    work = "  班级：" + orgName + "\n";
                }
                //生成复制名片的字符串
                String copyString = "  姓名：" + userName + "\n"
                        + "  性别：" + sex + "\n"
                        + org
                        + work
                        + "  手机：" + telephone + "\n"
                        + "  Email：" + email + "\n";
                ClipboardManager mClipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                mClipboard.setPrimaryClip(ClipData.newPlainText(this.getClass().getSimpleName(), copyString));
                Toast.makeText(getActivity(), R.string.common_copyToClipboard, Toast.LENGTH_SHORT).show();
                mPopupWindow.dismiss();
                break;
            case R.id.my_center_setting://偏好设置
                startActivity(new Intent(getActivity(), PerferSettingActivity.class));
                break;
            case R.id.my_center_function://辅助功能
                startActivity(new Intent(getActivity(), AdditionalFunctionActivity.class));
                break;
            case R.id.my_center_file://我接收的文件
                startActivity(new Intent(getActivity(), DownloadedFilesActivity.class));
                break;
            case R.id.my_center_about://关于
                startActivity(new Intent(getActivity(), AboutActivity.class));
                break;
            case R.id.my_center_exit://退出登录
                backLogout();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLogoutConfirmDialog.dismiss();
    }

    /**
     * 退出登录
     */
    private void backLogout() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.myfragment_alert_dialog, null);
        LinearLayout cancel = (LinearLayout) view.findViewById(R.id.cancel);
        LinearLayout sure = (LinearLayout) view.findViewById(R.id.sure);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLogoutConfirmDialog.dismiss();
            }
        });
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLogoutConfirmDialog.dismiss();
                L.e("msg", "退出登录成功");
                LastLoginUserSP.getInstance(getActivity()).saveIsLogin(false);
//                getActivity().stopService(new Intent(getActivity(), LocateService.class));//退出登录后停止定位服务
                LastLoginUserSP.getInstance(getActivity()).saveUserAccount("");
                logout();
            }
        });
        mLogoutConfirmDialog.setView(view);
        mLogoutConfirmDialog.show();
    }


    /**
     * 发送名片
     */
    public void sendContactCard() {
        if (user != null) {
            //判断当前是男是女
            String sex;
            if (gender == 1) {
                sex = "男";
            } else if (gender == 2) {
                sex = "女";
            } else {
                sex = "保密";
            }
            //判断当前机构是否有名字
            String org = "";
            String work = "";
            if (userType == 1) {//教师
                if (!StringUtils.isEmpty(userPost) && !userPost.equals("无")) {
                    org = "  职称：" + userPost + "\n";
                } else {
                    org = "  职称：" + "教职工" + "\n";
                }
                work = "  部门：" + orgName + "\n";
            } else {//学生
                if (!StringUtils.isEmpty(userPost)) {
                    org = "  职称：" + userPost + "\n";
                } else {
                    org = "  职称：" + "学生" + "\n";
                }
                work = "  班级：" + orgName + "\n";
            }
            //生成复制名片的字符串
            String copyString = "  姓名：" + userName + "\n"
                    + "  性别：" + sex + "\n"
                    + org
                    + work
                    + "  手机：" + telephone + "\n"
                    + "  Email：" + email + "\n";
            Uri smsToUri = Uri.parse("smsto:");
            Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
            intent.putExtra("sms_body", copyString);
            startActivity(intent);
        } else {
            ToastUtil.toastAlerMessage(getActivity(), "没有该用户信息", 1000);
        }
    }

    /**
     * 打开弹出框
     */
    public void showPhonePopWindow() {
        mPopupWindow = new PopupWindow(mPhonePopView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x64ffffff));//必须设置背景才能响应物理返回键
        mPopupWindow.showAtLocation(ll_my_fragment, Gravity.BOTTOM, 0, 0);
        mPopupWindow.setAnimationStyle(R.style.AnimBottom);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.showAsDropDown(ll_my_fragment);
//        mPopupWindow.update();//7.0版本有毒
    }

    /**
     * 注销帐号
     */
    private void logout() {
        AppController.getInstance().mLoginUser = null;
        AppController.getInstance().mSelfUser = null;
        // 清空密码
        LastLoginUserSP lastUser = LastLoginUserSP.getInstance(getActivity());
        lastUser.saveUserPassword("");
        getActivity().sendBroadcast(new Intent(Const.BROADCAST_ACTION_USER_LOGOUT));//发送用户注销广播
        //停止聊天窗口里下载文件的服务
        stopDownLoadService();
        // 下线，停止xmpp服务
        closeXmppService();

        //退出的时候同时解绑阿里推送帐号
        PushServiceFactory.getCloudPushService().unbindAccount(
                new CommonCallback() {
                    @Override
                    public void onSuccess(String response) {
                        L.i(mTag, "解绑阿里帐号成功");
                    }

                    @Override
                    public void onFailed(String errorCode, String errorMessage) {
                        L.i(mTag, "解绑阿里帐号失败");
                    }
                }
        );

        //关闭用户数据库连接
//        UserAccountDB.setNullInstance();

        //是否彻底退出并关闭进程
        BaseActivity.exit(false);

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }

    /**
     * 停止xmpp的服务
     */
    protected void closeXmppService() {
        XmppConnectionManager.getInstance().doExitThread();
        Intent serviceIntent = new Intent(getActivity(), XmppConnService.class);
        getActivity().stopService(serviceIntent);
    }

    /**
     * 停止聊天窗口里下载文件的服务
     */
    protected void stopDownLoadService() {
        Intent downLoadIntent = new Intent(getActivity(), DownloadService.class);
        L.v("DownloadService.mDownloadMsgBeans == " + DownloadService.mDownloadMsgBeans);
        if (DownloadService.mDownloadMsgBeans != null && DownloadService.mDownloadMsgBeans.size() > 0) {
            List<BaseChatMsgEntity> msgBeans = new ArrayList<BaseChatMsgEntity>(DownloadService.mDownloadMsgBeans);
            DownloadService.mDownloadMsgBeans.clear();
            for (BaseChatMsgEntity failedMsg : msgBeans) {
                DownloadService.updateDatabaseMsgStatus(failedMsg, BaseChatMsgEntity.DOWNLOAD_FAILED);
            }
        }
        getActivity().stopService(downLoadIntent);
    }
}
