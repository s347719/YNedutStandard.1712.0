package com.yineng.ynmessager.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.bean.p2psession.MessageFileEntity;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.ImageUtil;
import com.yineng.ynmessager.util.TextUtil;
import com.yineng.ynmessager.view.face.FaceConversionUtil;

import static android.view.View.GONE;

/**
 * Created by 舒欢
 * Created time: 2017/4/20
 */
public class TransmitDialog extends Dialog implements
        View.OnClickListener {

    private MessageBodyEntity bodyEntity;
    private OnclickListener onclick;
    private String name;
    private int isSend;//0:是发送 1:不是发送（即接收）用于转发图片的判断
    ImageView transFile;
    public TransmitDialog(Context context, int theme, OnclickListener onclick , MessageBodyEntity entity, String target,int isSend) {
        super(context, theme);
        this.onclick = onclick;
        this.bodyEntity = entity;
        this.name = target;
        this.isSend = isSend;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transmit_dialog_layout);
        setCanceledOnTouchOutside(false);
        View view_image = findViewById(R.id.view_image);
        ImageView imageView = (ImageView) findViewById(R.id.transimage);
        TextView image_text = (TextView) findViewById(R.id.image_text);
        TextView file_name = (TextView) findViewById(R.id.file_name);
        View view_file = findViewById(R.id.view_file);
        transFile =(ImageView)findViewById(R.id.transFile);
        TextView file_size = (TextView) findViewById(R.id.file_size);
        TextView trans_text = (TextView) findViewById(R.id.trans_text);
        if (bodyEntity.getImages().size()>0){
            view_image.setVisibility(View.VISIBLE);
            view_file.setVisibility(GONE);
            trans_text.setVisibility(GONE);
            //imagepath是判断当前图片是否下载过，如果下载过就直接从本地取，如果不是那么就去网络上下载
            String imagepath = bodyEntity.getImages().get(0).getSdcardPath();
            String content = bodyEntity.getContent();
            //有可能是文字在前也有可能是文字在后，在此处判断字符长度的时候要加上头尾两部分存在的可能性
            String info = content.substring(0,content.indexOf("<"))+content.substring(content.lastIndexOf(">")+1,content.length());
            if (info.length()>0){
                //<img key="3b29c48f-0ee0-45e7-8783-acee9facd3be">我技术是  是内容的模板，去掉<　>里面的内容
                imageView.setVisibility(GONE);
                image_text.setVisibility(View.VISIBLE);
                content = content.substring(0,content.indexOf("<"))+"【图片】"+content.substring(content.lastIndexOf(">")+1,content.length());
                image_text.setText(content);
            }else {
                imageView.setVisibility(View.VISIBLE);
                image_text.setVisibility(GONE);
                if (imagepath != null && isSend==0) {
                    Bitmap cacheBit = ImageUtil.getBitmapThumbnail(imagepath, FaceConversionUtil.getInstace().mBitmapwidth, FaceConversionUtil.getInstace().mBitmapheight);
                    imageView.setImageBitmap(cacheBit);
                } else {
                    String token = FileUtil.getSendFileToken();
                    String fileId = bodyEntity.getImages().get(0).getFileId();
                    String mFileUrl = Const.PROTOCOL + AppController.getInstance().CONFIG_INSIDE_FILE_TRANSLATE_IP + Const.DOWNLOAD_FILE_URL + "?token=" + token + "&fileId=" + fileId;
                    ImageLoader.getInstance().displayImage(mFileUrl, imageView);
                }
            }
        }else if(bodyEntity.getFiles().size()>0){
            view_image.setVisibility(GONE);
            view_file.setVisibility(View.VISIBLE);
            trans_text.setVisibility(GONE);
            MessageFileEntity mFileInfo = bodyEntity.getFiles().get(0);
            showImageType(mFileInfo.getName());
            long mFileInfoSize = Long.parseLong(mFileInfo.getSize());
            String fileSizeStr = FileUtil.FormatFileSize(mFileInfoSize);
            file_name.setText(mFileInfo.getName());
            file_size.setText(fileSizeStr);
        }else {
            trans_text.setVisibility(View.VISIBLE);
            view_image.setVisibility(GONE);
            view_file.setVisibility(GONE);
            trans_text.setText(bodyEntity.getContent());
        }
        findViewById(R.id.cancle).setOnClickListener(this);
        findViewById(R.id.ok).setOnClickListener(this);
        TextView textView = (TextView) findViewById(R.id.text_name);
        textView.setText(name);

    }

    private void showImageType(String name) {

        switch (TextUtil.matchTheFileType(name)){
            case 1:
                transFile.setImageResource(R.mipmap.jpg);
                break;
            case 2:
                transFile.setImageResource(R.mipmap.video);
                break;
            case 3:
                transFile.setImageResource(R.mipmap.music);
                break;
            case 4:
                transFile.setImageResource(R.mipmap.word);
                break;
            case 5:
                transFile.setImageResource(R.mipmap.excel);
                break;
            case 6:
                transFile.setImageResource(R.mipmap.pdf);
                break;
            case 7:
                transFile.setImageResource(R.mipmap.ppt);
                break;
            case 8:
                transFile.setImageResource(R.mipmap.rar);
                break;
            case 9:
                transFile.setImageResource(R.mipmap.file);
                break;
            default:
                transFile.setImageResource(R.mipmap.file);
                break;

        }

    }
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.cancle:
                cancel();
                break;
            case R.id.ok:
                onclick.clicklistener(v);
                break;
        }
    }

    //外部接口接收点击确定事件
    public interface OnclickListener{

        void clicklistener(View view);
    }

}
