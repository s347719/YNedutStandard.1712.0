//***************************************************************
//*    2015-4-21  上午11:21:38
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.settings;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.update.UpdateCheckUtil;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.address.URLs;

import java.util.Hashtable;

/**
 * 关于界面
 *
 * @author 贺毅柳
 * @category Activity
 */
public class AboutActivity extends BaseActivity implements OnClickListener, UpdateCheckUtil.checkVersionListener {
    private TextView mTxt_previous; // 左上角的返回按钮
    private TextView about_logo_version, about_version, about_apk_download_address,
            about_copy_apk_download_address, about_service_address, about_copy_service_address, about_apk_qrcode, about_service_qrcode;
    private ImageView newVersionFlag;
    private AlertDialog serviceDialog, apkDialog;
    private LayoutInflater inflater;
    private RelativeLayout rl_update_version;
    private UpdateCheckUtil updateCheckUtil;
    private ImageView icon_logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        serviceDialog = new AlertDialog.Builder(this).create();
        apkDialog = new AlertDialog.Builder(this).create();
        inflater = LayoutInflater.from(this);
        updateCheckUtil = UpdateCheckUtil.getInstance();
        updateCheckUtil.setContext(this);
        initViews();
        //检测版本
        checkVersion();
    }

    /**
     * 检测版本
     */
    private void checkVersion() {
        updateCheckUtil.setOnCheckVersionListener(this);
        updateCheckUtil.setShowDialog(false,false);
        updateCheckUtil.checkAppVersion(this);
    }


    /**
     * 初始化界面控件
     */
    private void initViews() {
        rl_update_version = (RelativeLayout) findViewById(R.id.rl_update_version);
        icon_logo = (ImageView) findViewById(R.id.icon_logo);
        mTxt_previous = (TextView) findViewById(R.id.about_txt_previous);
        about_apk_qrcode = (TextView) findViewById(R.id.about_apk_qrcode);
        about_service_qrcode = (TextView) findViewById(R.id.about_service_qrcode);
        about_logo_version = (TextView) findViewById(R.id.about_logo_version);
        about_version = (TextView) findViewById(R.id.about_version);
        about_apk_download_address = (TextView) findViewById(R.id.about_apk_download_address);
        about_copy_apk_download_address = (TextView) findViewById(R.id.about_copy_apk_download_address);
        about_service_address = (TextView) findViewById(R.id.about_service_address);
        about_copy_service_address = (TextView) findViewById(R.id.about_copy_service_address);
        newVersionFlag = (ImageView) findViewById(R.id.about_new_version_flag);

        about_logo_version.setText("Messenger " + UpdateCheckUtil.formatNewVersionToShow(getPackageVersionName()));
//        about_version.setText("");
        about_copy_apk_download_address.setOnClickListener(this);
        about_copy_service_address.setOnClickListener(this);
        mTxt_previous.setOnClickListener(this);
        about_apk_qrcode.setOnClickListener(this);
        about_apk_download_address.setOnClickListener(this);
        about_service_qrcode.setOnClickListener(this);
        about_service_address.setOnClickListener(this);
        rl_update_version.setOnClickListener(this);

        about_apk_download_address.setText(URLs.YNEDUT_DOWNLOAD_SHARE);
        about_service_address.setText(LastLoginUserSP.getInstance(this).getUserServicesAddress());
        int drawAbleID = getResources().getIdentifier(AppController.getInstance().icon_name, "mipmap", getPackageName());
        icon_logo.setImageResource(drawAbleID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                showApkQRcodeDialog(URLs.YNEDUT_DOWNLOAD_SHARE);
                showServiceQRcodeDialog(getString(R.string.qeCodeSignName) + LastLoginUserSP.getInstance(AboutActivity.this).getUserServicesAddress());
            }
        }).start();
    }

    private void showApkQRcodeDialog(String url) {
        Window window = apkDialog.getWindow();
        window.setGravity(Gravity.CENTER);
        window.setWindowAnimations(R.style.dialog_style);
        View view = inflater.inflate(R.layout.layout_qrcode_dialog, null);
        TextView textView = (TextView) view.findViewById(R.id.saomaio_qr);
        textView.setText("扫一扫下载安装包");
        ImageView imageView = (ImageView) view.findViewById(R.id.about_QRcode_img);
        imageView.setImageBitmap(createQRImage(url));
        apkDialog.setCanceledOnTouchOutside(true);
        apkDialog.setCancelable(true);
        apkDialog.setView(view);
    }

    private void showServiceQRcodeDialog(String url) {
        Window window = serviceDialog.getWindow();
        window.setGravity(Gravity.CENTER);
        window.setWindowAnimations(R.style.dialog_style);
        View view = inflater.inflate(R.layout.layout_qrcode_dialog, null);
        TextView textView = (TextView) view.findViewById(R.id.saomaio_qr);
        textView.setText("扫一扫获取服务器地址");
        ImageView imageView = (ImageView) view.findViewById(R.id.about_QRcode_img);
        imageView.setImageBitmap(createQRImage(url));
        serviceDialog.setCanceledOnTouchOutside(true);
        serviceDialog.setCancelable(true);
        serviceDialog.setView(view);
    }

    /**
     * 要转换的地址或字符串,可以是中文
     */
    public Bitmap createQRImage(String url) {
        try {
            //判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1) {
                return null;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, 500, 500, hints);
            int[] pixels = new int[500 * 500];
            //下面这里按照二维码的算法，逐个生成二维码的图片，
            //两个for循环是图片横列扫描的结果
            for (int y = 0; y < 500; y++) {
                for (int x = 0; x < 500; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * 500 + x] = 0xff000000;
                    } else {
                        pixels[y * 500 + x] = 0xffffffff;
                    }
                }
            }
            //生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, 500, 0, 0, 500, 500);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.about_copy_apk_download_address://复制apk下载地址
                copyAddress(about_apk_download_address);
                break;
            case R.id.about_copy_service_address://复制登录服务器地址
                copyAddress(about_service_address);
                break;
            case R.id.about_txt_previous:
                finish();
                break;
            case R.id.about_apk_qrcode://点击apk二维码弹出dialog
                apkDialog.show();
                break;
            case R.id.about_apk_download_address://点击apk二维码弹出dialog
                apkDialog.show();
                break;
            case R.id.about_service_address://点击service二维码弹出dialog
                serviceDialog.show();
                break;
            case R.id.about_service_qrcode://点击service二维码弹出dialog
                serviceDialog.show();
                break;
            case R.id.rl_update_version://检测版本
                updateCheckUtil.setOnCheckVersionListener(mCheckAppListener);
                updateCheckUtil.setShowDialog(true,false);
                updateCheckUtil.checkAppVersion(this, true);
                showProgressD("正在检测新版本...", false);
                break;
        }

    }

    /**
     * 复制到剪切板
     */
    private void copyAddress(TextView textView) {
        String copyString = textView.getText().toString().trim();
        ClipboardManager mClipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        mClipboard.setPrimaryClip(ClipData.newPlainText(this.getClass().getSimpleName(), copyString));
        Toast.makeText(this, R.string.common_copyToClipboard, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (serviceDialog != null) {
            serviceDialog.dismiss();
        }

        if (apkDialog != null) {
            apkDialog.dismiss();
        }
    }

    /**
     * 得到本应用程序的VersionName
     */
    private String getPackageVersionName() {
        String versionName = "";
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
        }
        return versionName;
    }

    @Override
    public void onCheckVerResult(int checkResult, boolean isHandCheck) {
        Log.e("yhu",checkResult+":111");
        hideProgessD();
        if (checkResult == 0) {
            newVersionFlag.setVisibility(View.VISIBLE);
//            String version =UpdateCheckUtil.getInstance().getmUpdateInfo().getResult().getNewesProVersionCode();
//            about_version.setText(UpdateCheckUtil.formatNewVersionToShow(version));
        }
    }

    @Override
    public void onHandleUpdateVer(int handleType, boolean isHandCheck) {
        Log.e("yhu",handleType+":222");
        hideProgessD();

    }
}
