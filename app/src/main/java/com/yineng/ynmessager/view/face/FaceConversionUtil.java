package com.yineng.ynmessager.view.face;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.p2psession.BaseChatActivity;
import com.yineng.ynmessager.activity.picker.image.ImageViewerActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.ChatEmoji;
import com.yineng.ynmessager.bean.groupsession.GroupChatMsgEntity;
import com.yineng.ynmessager.bean.imgpicker.ImageFile;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.bean.p2psession.MessageImageEntity;
import com.yineng.ynmessager.db.P2PChatMsgDao;
import com.yineng.ynmessager.db.dao.DisGroupChatDao;
import com.yineng.ynmessager.db.dao.GroupChatDao;
import com.yineng.ynmessager.receiver.NetWorkTypeUtils;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.ImageUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.address.URLs;
import com.yineng.ynmessager.view.ClickableImageSpan;
import com.yineng.ynmessager.view.ClickableMovementMethod;
import com.yineng.ynmessager.view.face.gif.AnimatedGifDrawable;
import com.yineng.ynmessager.view.face.gif.AnimatedImageSpan;
import com.yineng.ynmessager.view.face.gif.GifDecoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public final class FaceConversionUtil {
    public static final String TAG = "FaceConversionUtil";

    /**
     * 每一页表情的个数
     */
    private final int mPageSize = 20;

    private static FaceConversionUtil mFaceConversionUtil;

    /**
     * 保存于内存中的表情HashMap
     */
    private HashMap<String, String> mEmojiMap = new HashMap<String, String>();

    /**
     * 保存于内存中的表情集合
     */
    private List<ChatEmoji> mEmojiList = new ArrayList<ChatEmoji>();

    /**
     * 表情分页的结果集合
     */
    public List<List<ChatEmoji>> mEmojiLists = new ArrayList<List<ChatEmoji>>();

    private Rect mFaceImgSize; // 表情图片的大小

    private ArrayList<GroupChatMsgEntity> mGroupMessageList;
    private AppController mApplication;

    // 初始化全局默认通用的DisplayImageOptions
    DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true) // 图片一律要在内存中缓存
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
            .build();// 构建完成

    /**
     * 默认缩略图片宽度
     */
    public int mBitmapwidth;

    /**
     * 默认缩略图高度
     */
    public int mBitmapheight;

    private Context mContext;
    /**
     * 消息数据库工具
     */
    private P2PChatMsgDao mP2PChatMsgDao;

    private GroupChatDao mGroupChatDao;

    private DisGroupChatDao mDisGroupChatDao;

    private FaceConversionUtil() {
        mApplication = AppController.getInstance();
        // 初始化获取表情图片的大小
        int faceSize = mApplication.getResources().getDimensionPixelSize(R.dimen.common_face_imageSize);
        mFaceImgSize = new Rect(0, 0, faceSize, faceSize);
        Bitmap mBitmap = BitmapFactory.decodeResource(mApplication.getResources(), R.mipmap.defalut_image_large);
        mBitmapwidth = mBitmap.getWidth();
        mBitmapheight = mBitmap.getHeight();
        mP2PChatMsgDao = new P2PChatMsgDao(mContext);
        mGroupChatDao = new GroupChatDao(mContext);
        mDisGroupChatDao = new DisGroupChatDao(mContext);
    }

    public static synchronized FaceConversionUtil getInstace() {
        if (mFaceConversionUtil == null) {
            mFaceConversionUtil = new FaceConversionUtil();
        }
        return mFaceConversionUtil;
    }

    /**
     * 得到一个SpanableString对象，通过传入的字符串,并进行正则判断
     *
     * @param context
     * @param str
     * @return
     */
    public SpannableString getExpressionString(Context context, String str) {

        SpannableString spannableString = new SpannableString(str);
        // 正则表达式比配字符串里是否含有表情，如： 我好[开心]啊
//		String zhengze = "\\[[^\\]]+\\]";
        String zhengze = "(\\[\\/\\d{1,2}\\])";
        // 通过传入的正则表达式来生成一个pattern
        Pattern sinaPatten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);
        try {
            dealExpression(context, spannableString, sinaPatten, 0);
        } catch (Exception e) {
            Log.e("dealExpression", e.getMessage());
        }
        return spannableString;
    }

    /**
     * 添加表情
     *
     * @param context
     * @param imgId
     * @param spannableString
     * @return
     */
    public SpannableString addFace(Context context, int imgId,
                                   String spannableString) {
        if (TextUtils.isEmpty(spannableString)) {
            return null;
        }
//		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
//				imgId);
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(context.getAssets().open("face/gif/" + imgId + ".gif"));
        } catch (IOException e) {
            L.e(TAG, e.getMessage(), e);
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, mFaceImgSize.right, mFaceImgSize.bottom, false);
        ImageSpan imageSpan = new ImageSpan(context, bitmap);
        SpannableString spannable = new SpannableString(spannableString);
        spannable.setSpan(imageSpan, 0, spannableString.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    /**
     * 对spanableString进行正则判断，如果符合要求，则以表情图片代替
     *
     * @param context
     * @param spannableString
     * @param patten
     * @param start
     * @throws Exception
     */
    private void dealExpression(Context context,
                                SpannableString spannableString, Pattern patten, int start)
            throws Exception {
        Matcher matcher = patten.matcher(spannableString);
        while (matcher.find()) {
            String key = matcher.group();
            // 返回第一个字符的索引的文本匹配整个正则表达式,ture 则继续递归
            if (matcher.start() < start) {
                continue;
            }
            String value = mEmojiMap.get(key);
            if (TextUtils.isEmpty(value)) {
                continue;
            }
            Bitmap bitmap = BitmapFactory.decodeStream(context.getAssets().open("face/gif/" + value));
            bitmap = Bitmap.createScaledBitmap(bitmap, mFaceImgSize.right, mFaceImgSize.bottom, false);
            // 通过图片资源id来得到bitmap，用一个ImageSpan来包装
            ImageSpan imageSpan = new ImageSpan(context, bitmap);
            // 计算该图片名字的长度，也就是要替换的字符串的长度
            int end = matcher.start() + key.length();
            // 将该图片替换字符串中规定的位置中
            spannableString.setSpan(imageSpan, matcher.start(), end,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            if (end < spannableString.length()) {
                // 如果整个字符串还未验证完，则继续。。
                dealExpression(context, spannableString, patten, end);
            }
            break;

        }
    }

    public void getFileText(Context context) {
        parseData(FaceUtils.getEmojiFile(context), context);
    }

    /**
     * 解析字符
     *
     * @param data
     */
    private void parseData(List<String> data, Context context) {
        if (data == null) {
            return;
        }
        mEmojiMap.clear();
        mEmojiList.clear();
        mEmojiLists.clear();
        ChatEmoji emojEentry;
        try {
            for (String str : data) {

                String[] fileTexts = str.split("\\.");
                String faceId = "[/" + fileTexts[0] + "]";//eg:[/18]
                String fileName = fileTexts[0];//eg:18
                mEmojiMap.put(faceId, str); //key:[/18] value:18.gif
                emojEentry = new ChatEmoji();
                emojEentry.setId(Integer.parseInt(fileName));
                emojEentry.setCharacter(faceId);
                emojEentry.setFaceName(fileName);
                mEmojiList.add(emojEentry);
            }
            int pageCount = (int) Math.ceil(mEmojiList.size() / 20 + 0.1);

            for (int i = 0; i < pageCount; i++) {
                mEmojiLists.add(getData(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取分页数据
     *
     * @param page
     * @return
     */
    private List<ChatEmoji> getData(int page) {
        int startIndex = page * mPageSize;
        int endIndex = startIndex + mPageSize;

        if (endIndex > mEmojiList.size()) {
            endIndex = mEmojiList.size();
        }
        // 不这么写，会在viewpager加载中报集合操作异常，我也不知道为什么
        List<ChatEmoji> list = new ArrayList<ChatEmoji>();
        list.addAll(mEmojiList.subList(startIndex, endIndex));
        if (list.size() < mPageSize) {
            for (int i = list.size(); i < mPageSize; i++) {
                ChatEmoji object = new ChatEmoji();
                list.add(object);
            }
        }
        if (list.size() == mPageSize) {
            ChatEmoji object = new ChatEmoji();
            object.setId(R.drawable.face_del_icon);
            list.add(object);
        }
        return list;
    }

    /**
     * 处理消息文本
     *
     * @param mContext
     * @param gifTextView
     * @param body
     * @param mHandler
     * @param packetId    每个图片所属的packetId 是唯一的
     * @param type        会话类型 //会话类型1为2人会话，2为讨论组会话，3为群会话，4为广播
     * @return
     */
    public SpannableString handlerContent(String chatNum, Context mContext, TextView gifTextView, MessageBodyEntity body, Handler mHandler, String packetId, int type) {
        this.mContext = mContext;
        SpannableString ss = null;
        String regex = Const.Regex.FACE + "|" + Const.Regex.IMG;
        Pattern p = Pattern.compile(regex);
        if (body == null || body.getContent() == null) {
            return ss;
        }
        Matcher m = p.matcher(body.getContent());
        if (handlerCount(body.getContent()) < 10) {
            ss = handlerFaceImgContent(chatNum, true, m, body, gifTextView, mHandler, packetId, type);
        } else {
            ss = handlerFaceImgContent(chatNum, false, m, body, gifTextView, mHandler, packetId, type);
        }

        return ss;
    }

    /**
     * @param m
     * @param body
     * @param mHandler
     * @return
     */
    private SpannableString handlerFaceImgContent(String chatId, boolean show, Matcher m,
                                                  MessageBodyEntity body, final TextView textView, final Handler mHandler, String packetId, int type) {
        final SpannableString sb = new SpannableString(body.getContent());
        List<MessageImageEntity> mImageList = body.getImages();
        if (mImageList == null) return sb;
        //如果有图片集，则获取第一个图片实例的sdCardPath属性，如果该属性为null，说明是接收图片；否则是发送图片
        String sendImgPath = mImageList.size() > 0 ? mImageList.get(0).getSdcardPath() : null;
        String fieldId = mImageList.size() > 0 ? mImageList.get(0).getFileId() : null;
        while (m.find()) {
            String tempText = m.group();

            if (tempText.startsWith("[")) {
                // 表情
                InputStream is = null;
                try {
                    String num = tempText.substring("[/".length(),
                            tempText.length() - "]".length());
                    String gif = "face/gif/" + num + ".gif";
                    /**
                     * 如果open这里不抛异常说明存在gif，则显示对应的gif 否则说明gif找不到，则显示png
                     * */
                    is = mContext.getAssets().open(gif);

                    if (show) {
                        /** 显示gif **/
                        AnimatedGifDrawable mAnimatedGifDrawable = new AnimatedGifDrawable(
                                is, new AnimatedGifDrawable.UpdateListener() {
                            @Override
                            public void update() {
                                textView.postInvalidate();
                            }
                        }, mFaceImgSize);
                        AnimatedImageSpan animatedImageSpan = new AnimatedImageSpan(
                                mAnimatedGifDrawable);
                        sb.setSpan(animatedImageSpan, m.start(), m.end(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        /** 显示静态pic **/
                        GifDecoder gifDecoder = new GifDecoder();
                        gifDecoder.read(is);
//						Bitmap bitmap = BitmapFactory.decodeStream(is);
//						bitmap = Bitmap.createScaledBitmap(bitmap,
//								mFaceImgSize.right, mFaceImgSize.bottom, false);
                        Bitmap bitmap = gifDecoder.getFrame(0);
                        bitmap = Bitmap.createScaledBitmap(bitmap,
                                mFaceImgSize.right, mFaceImgSize.bottom, false);
                        ImageSpan imageSpan = new ImageSpan(mContext, bitmap);
                        sb.setSpan(imageSpan, m.start(), m.end(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } catch (Exception e) {
                    L.e(TAG, e.getMessage(), e);
                } finally {
                    // 关闭打开的图片资源InputStream
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException e) {
                        L.e(TAG, e.getMessage(), e);
                    }
                }
            } else {// 图片
                //发送图片
                if (sendImgPath != null
                        && mApplication.mSelfUser != null
                        && body.getSendName().equals(mApplication.mSelfUser.getUserName())) {
                    showSdcardImgBySend(chatId, body, sendImgPath, m, sb, textView, packetId, type, mHandler);
                } else {//接收图片
                    showReceiveImg(chatId, body, m, sb, textView, mHandler, packetId, type);
                }
            }
        }
        return sb;
    }

    /**
     * 聊天窗口中显示接收图片的方法
     *
     * @param m
     * @param sb
     * @param textView
     * @param mHandler
     */
    private void showReceiveImg(final String chatId, final MessageBodyEntity body, Matcher m, final SpannableString sb, final TextView textView, final Handler mHandler, final String packetId, final int type) {
        FileInputStream is = null;
        try {
            String tempText = m.group();
            String token = FileUtil.getSendFileToken();
            final String fileId = tempText.substring("<img key=\"".length(),
                    tempText.length() - "\">".length());
            int[] matchIndex = {m.start(), m.end()};

            if (token == null
                    || TextUtils.isEmpty(mApplication.CONFIG_INSIDE_FILE_TRANSLATE_IP)) {
                ImageSpan breakSpan = new ImageSpan(mContext,
                        R.mipmap.defalut_image_break,
                        DynamicDrawableSpan.ALIGN_BOTTOM);
                sb.setSpan(breakSpan, m.start(), m.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                return;
            }
            final String mImgUrl = Const.PROTOCOL
                    + mApplication.CONFIG_INSIDE_FILE_TRANSLATE_IP
                    + Const.DOWNLOAD_FILE_URL + "?token=" + token
                    + "&fileId=" + fileId;
            final String cacheImgPath = ImageLoader.getInstance().getDiskCache().get(mImgUrl).getPath();
            File cacheImg = new File(cacheImgPath);
            L.e("mImgUrl == " + mImgUrl);
            if (cacheImg.exists()) {
                Bitmap downloadedPic = ImageUtil.getBitmapThumbnail(cacheImgPath, mBitmapwidth, mBitmapheight);
                if (downloadedPic == null) {
                    ImageSpan breakSpan = new ImageSpan(mContext,
                            R.mipmap.defalut_image_break,
                            DynamicDrawableSpan.ALIGN_BOTTOM);
                    sb.setSpan(breakSpan, m.start(), m.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    textView.setText(sb);
//					cacheImg.delete();
                    return;
                }
                String fileType = FileUtil.getFileType(cacheImg.getPath());
                if (fileType != null && fileType.equals("gif")) {
                    /** 显示gif **/
                    is = new FileInputStream(cacheImg);
                    /** 显示gif **/
                    AnimatedGifDrawable mAnimatedGifDrawable = new AnimatedGifDrawable(
                            is, new AnimatedGifDrawable.UpdateListener() {
                        @Override
                        public void update() {
                            textView.postInvalidate();
                        }
                    }, new Rect(0, 0, downloadedPic.getWidth(), downloadedPic.getHeight()));
                    AnimatedImageSpan animatedImageSpan = new AnimatedImageSpan(
                            mAnimatedGifDrawable);
                    sb.setSpan(animatedImageSpan, m.start(), m.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    /** 显示图片 **/
                    ClickableImageSpan clickableImageSpan = new ClickableImageSpan(
                            mContext, downloadedPic) {

                        @Override
                        public void onClick(View view) {
                            // 如果Activity中ContextMenu正在显示就不触发点击后续事件
                            // 比如长按图片的时候，会弹出ContextMenu，但是手松开后又会触发该点击事件
                            Activity parent = unwrapContext(view.getContext());
                            if (parent == null) {
                                return;
                            }
                            if (parent instanceof BaseChatActivity) {
                                if (((BaseChatActivity) parent).isContextMenuShowing()) {
                                    L.d(getClass(), "due to the context menu is showing, onClick event will not be performed");
                                    return;
                                }
                            }

                            int[] index = (int[]) view.getTag();
                            L.v("ClickableImageSpan : onClick == " + index[0] + " " + index[1]);
                            //点击对方发送的图片到图片浏览器
                            startViewImageActivity(type, chatId, body);

                        }

                        // http://stackoverflow.com/questions/8276634/android-get-hosting-activity-from-a-view
                        private Activity unwrapContext(Context context) {
                            while (context instanceof ContextWrapper) {
                                if (context instanceof Activity) {
                                    return (Activity) context;
                                }
                                context = ((ContextWrapper) context).getBaseContext();
                            }
                            return null;
                        }
                    };
                    sb.setSpan(clickableImageSpan, m.start(), m.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    textView.setMovementMethod(ClickableMovementMethod
                            .getInstance());
                }
                textView.setText(sb);
            } else {
                L.e("cacheImg.不存在,就去下载 == " + (sb.getSpans(0, sb.length() - 1, ImageSpan.class).length));
                ImageSpan imageSpan = new ImageSpan(mContext,
                        R.mipmap.defalut_image_large,
                        DynamicDrawableSpan.ALIGN_BOTTOM);
                sb.setSpan(imageSpan, m.start(), m.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (!TextUtils.isEmpty(mApplication.CONFIG_INSIDE_FILE_TRANSLATE_IP)) {
                    String networkType = NetWorkTypeUtils.getNetTypeForView(mContext);
                    // 如果不是wifi并且设置中设置了移动网络下不接受图片时，不自动接收pic,设图片点击事件
                    if (!networkType.equals("wifi") && mApplication.mUserSetting
                            .getAlwaysAutoReceiveImg() == 0) {
                        ClickableImageSpan clickableImageSpan = new ClickableImageSpan(
                                mContext, R.mipmap.defalut_image_large,
                                DynamicDrawableSpan.ALIGN_BOTTOM) {

                            @Override
                            public void onClick(View view) {
                                //  Auto-generated method stub
                                int[] index = (int[]) view.getTag();
                                L.e("加载图片 == " + index[0] + " " + index[1]);
                                loadImage(mImgUrl, sb, textView, index,
                                        mHandler, packetId);
                            }
                        };
                        sb.setSpan(clickableImageSpan, m.start(), m.end(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        textView
                                .setMovementMethod(ClickableMovementMethod
                                        .getInstance());
                    } else {
                        // 直接加载图片
                        loadImage(mImgUrl, sb, textView, matchIndex,
                                mHandler, packetId);
                    }
                } else {
                    ImageSpan breakSpan = new ImageSpan(mContext,
                            R.mipmap.defalut_image_break,
                            DynamicDrawableSpan.ALIGN_BOTTOM);
                    sb.setSpan(breakSpan, m.start(), m.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭打开的图片资源InputStream
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                L.e(TAG, e.getMessage(), e);
            }
        }
    }

    /**
     * 聊天窗口中显示发送图片的方法
     *
     * @param sendImgPath
     * @param m
     * @param textView
     * @param sb
     */
    private void showSdcardImgBySend(final String chatId, final MessageBodyEntity body, final String sendImgPath, Matcher m, SpannableString sb, final TextView textView, final String packetId, final int type, Handler mHandler) {
        FileInputStream is = null;
        try {
            File cacheImg = new File(sendImgPath);
            if (cacheImg.exists()) {
                Bitmap downloadedPic = ImageUtil.getBitmapThumbnail(sendImgPath, mBitmapwidth, mBitmapheight);
                String fileType = FileUtil.getFileType(cacheImg.getPath());
                if (fileType != null && fileType.equals("gif")) {
                    is = new FileInputStream(cacheImg);
                    /** 显示gif **/
                    AnimatedGifDrawable mAnimatedGifDrawable = new AnimatedGifDrawable(
                            is, new AnimatedGifDrawable.UpdateListener() {
                        @Override
                        public void update() {
                            textView.postInvalidate();
                        }
                    }, new Rect(0, 0, downloadedPic.getWidth(), downloadedPic.getHeight()));
                    AnimatedImageSpan animatedImageSpan = new AnimatedImageSpan(
                            mAnimatedGifDrawable);
                    sb.setSpan(animatedImageSpan, m.start(), m.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    //保存会话中发送的图片到指定指定文件夹 cacheImg.getPath()是独一无二的自己发送的图片路径
                    FileUtil.saveBitmap(packetId, downloadedPic, Const.DOWNLOAD_FILE_CACHE);
                    ClickableImageSpan clickableImageSpan = new ClickableImageSpan(
                            mContext, downloadedPic) {

                        @Override
                        public void onClick(View view) {
                            // 如果Activity中ContextMenu正在显示就不触发点击后续事件
                            // 比如长按图片的时候，会弹出ContextMenu，但是手松开后又会触发该点击事件
                            Activity parent = unwrapContext(view.getContext());
                            if (parent == null) {
                                return;
                            }
                            if (parent instanceof BaseChatActivity) {
                                if (((BaseChatActivity) parent).isContextMenuShowing()) {
                                    L.d(getClass(), "due to the context menu is showing, onClick event will not be performed");
                                    return;
                                }
                            }

                            int[] index = (int[]) view.getTag();
                            L.v("ClickableImageSpan : onClick == " + index[0] + " " + index[1] + "644");
                            // 点击跳转到图片浏览器
                            startViewImageActivity(type, chatId, body);
                        }

                        // http://stackoverflow.com/questions/8276634/android-get-hosting-activity-from-a-view
                        private Activity unwrapContext(Context context) {
                            while (context instanceof ContextWrapper) {
                                if (context instanceof Activity) {
                                    return (Activity) context;
                                }
                                context = ((ContextWrapper) context).getBaseContext();
                            }
                            return null;
                        }

                    };
                    sb.setSpan(clickableImageSpan, m.start(), m.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    textView.setText(sb);
                    textView.setMovementMethod(ClickableMovementMethod
                            .getInstance());
                }

            } else { //显示烂图
                showReceiveImg(chatId, body, m, sb, textView, mHandler, packetId, type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭打开的图片资源InputStream
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                L.e(TAG, e.getMessage(), e);
            }
        }
    }

    /**
     * 跳转图片预览
     *
     * @param type
     * @param chatId
     * @param body
     */
    private void startViewImageActivity(int type, String chatId, MessageBodyEntity body) {
//根据当前类型来查询数据库
        mGroupMessageList = new ArrayList<>();
        if (type == Const.CHAT_TYPE_P2P) {
            mGroupMessageList = mP2PChatMsgDao.getChatMsgEntitiesToFindRecord(chatId);
        } else if (type == Const.CHAT_TYPE_GROUP) {
            mGroupMessageList = mGroupChatDao.getChatMsgEntities(chatId);
        } else if (type == Const.CHAT_TYPE_DIS) {
            mGroupMessageList = mDisGroupChatDao.getChatMsgEntities(chatId);
        }
        String token = FileUtil.getSendFileToken();
        ArrayList<ImageFile> images = new ArrayList<>();
        ArrayList<String> packetIdList = new ArrayList<>();
        for (GroupChatMsgEntity groupChatMsgEntity : mGroupMessageList) {
            MessageBodyEntity body2 = JSON.parseObject(groupChatMsgEntity.getMessage(), MessageBodyEntity.class);
            if (body2.getImages().size() > 0) {
                for (int i = 0; i < body2.getImages().size(); i++) {
                    String mImgUrl = URLs.PROTOCOL
                            + mApplication.CONFIG_INSIDE_FILE_TRANSLATE_IP
                            + URLs.DOWNLOAD_FILE_URL + "?token=" + token
                            + "&fileId=" + body2.getImages().get(i).getFileId();
                    images.add(new ImageFile(mImgUrl));
                    packetIdList.add(groupChatMsgEntity.getPacketId());
                }
            }
        }
        Collections.reverse(images);
        Collections.reverse(packetIdList);
//        当前点击的图片
        String path = Const.PROTOCOL
                + mApplication.CONFIG_INSIDE_FILE_TRANSLATE_IP
                + URLs.DOWNLOAD_FILE_URL + "?token=" + token
                + "&fileId=" + body.getImages().get(0).getFileId();
        //判断当前图片数据是我们在数据库查询出来数据中的位置
        int dex = images.indexOf(new ImageFile(path));
        Intent intent = new Intent(mContext, ImageViewerActivity.class);
        intent.putExtra("DefPosition", dex);
        intent.putExtra("ImageFileList", images);
        intent.putExtra("packetId", packetIdList);
        intent.putExtra("type", type);
        intent.putExtra("downLoad", true);
        mContext.startActivity(intent);
    }

    /**
     * 处理最近会话表情消息文本
     *
     * @param gifTextView
     * @param sb
     * @param m
     */
    private void showGifFace(boolean show, final Matcher m, final SpannableString sb, final TextView gifTextView, final Handler mHandler) {
        while (m.find()) {
            String tempText = m.group();

            if (tempText.startsWith("[")) {//表情
                InputStream is = null;
                try {
                    String num = tempText.substring("[/".length(), tempText.length() - "]".length());
                    String gif = "face/gif/" + num + ".gif";
                    /**
                     * 如果open这里不抛异常说明存在gif，则显示对应的gif
                     * 否则说明gif找不到，则显示png
                     * */
                    is = mContext.getAssets().open(gif);

                    if (show) {/**显示gif**/
                        AnimatedGifDrawable mAnimatedGifDrawable = new AnimatedGifDrawable(is, new AnimatedGifDrawable.UpdateListener() {
                            @Override
                            public void update() {
                                gifTextView.postInvalidate();
                            }
                        }, mFaceImgSize);
                        AnimatedImageSpan animatedImageSpan = new AnimatedImageSpan(mAnimatedGifDrawable);
                        sb.setSpan(animatedImageSpan, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {/**显示静态pic**/
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        bitmap = Bitmap.createScaledBitmap(bitmap, mFaceImgSize.right, mFaceImgSize.bottom, false);
                        ImageSpan imageSpan = new ImageSpan(mContext, bitmap);
                        sb.setSpan(imageSpan, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } catch (Exception e) {
                    L.e(TAG, e.getMessage(), e);
                } finally {
                    //关闭打开的图片资源InputStream
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException e) {
                        L.e(TAG, e.getMessage(), e);
                    }
                }
            } else {
                //图片
                String token = FileUtil.getSendFileToken();
                final String fileId = tempText.substring("<img key=\"".length(), tempText.length() - "\">".length());
                int[] matchIndex = {m.start(), m.end()};

                if (token == null || mApplication.CONFIG_INSIDE_FILE_TRANSLATE_IP == null) {
                    return;
                }

                ImageLoader imageLoader = ImageLoader.getInstance();
                final String mImgUrl = Const.PROTOCOL + mApplication.CONFIG_INSIDE_FILE_TRANSLATE_IP + Const.DOWNLOAD_FILE_URL + "?token=" + token + "&fileId=" + fileId;
                L.v("mImgUrl == " + mImgUrl);
                L.v("path == " + imageLoader.getDiskCache().get(mImgUrl).getPath());
                String cacheImgPath = imageLoader.getDiskCache().get(mImgUrl).getPath();
                File cacheImg = new File(cacheImgPath);
                if (cacheImg.exists()) {
                    L.v("cacheImg.exists()");
                    Bitmap downloadedPic = imageLoader.loadImageSync(Uri.fromFile(new File(cacheImgPath)).toString());
                    downloadedPic = ThumbnailUtils.extractThumbnail(downloadedPic, mBitmapwidth, mBitmapheight);
                    ClickableImageSpan clickableImageSpan = new ClickableImageSpan(mContext, downloadedPic) {

                        @Override
                        public void onClick(View view) {
                            //  Auto-generated method stub
                            L.v("进入 ");
                        }
                    };
                    sb.setSpan(clickableImageSpan, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    gifTextView.setText(sb);
                    gifTextView.setMovementMethod(ClickableMovementMethod.getInstance());
                } else {
                    L.e("cacheImg.不存在");
                    ImageSpan imageSpan = new ImageSpan(mContext, R.mipmap.defalut_image_large, DynamicDrawableSpan.ALIGN_BOTTOM);
                    sb.setSpan(imageSpan, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    if (!mApplication.CONFIG_INSIDE_FILE_TRANSLATE_IP.isEmpty()) {
                        L.v("sb == " + sb);
                        if (mApplication.mUserSetting.getAlwaysAutoReceiveImg() == 0) {//不自动接收,设图片点击事件
                            ClickableImageSpan clickableImageSpan = new ClickableImageSpan(mContext, R.mipmap.defalut_image_large, DynamicDrawableSpan.ALIGN_BOTTOM) {

                                @Override
                                public void onClick(View view) {
                                    //  Auto-generated method stub
                                    int[] index = (int[]) view.getTag();
                                    L.v("加载图片 == " + index[0] + " " + index[1]);
                                    loadImage(mImgUrl, sb, gifTextView, index, mHandler, null);
                                }
                            };
                            sb.setSpan(clickableImageSpan, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            gifTextView.setMovementMethod(ClickableMovementMethod.getInstance());
                        } else {//直接加载图片
                            loadImage(mImgUrl, sb, gifTextView, matchIndex, mHandler, null);
                        }
                    }
                }
            }
        }
    }


    /**
     * @param mHandler
     */
    protected synchronized void loadImage(String mImgUrl, final SpannableString ss, final TextView gifTextView, final int[] mIndex, final Handler mHandler, final String packetId) {

        ImageLoader.getInstance().loadImage(mImgUrl, imageOptions, new ImageLoadingListener() {

            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view,
                                        FailReason failReason) {
                L.e("onLoadingFailed failReason == " + failReason.getType());
                if (failReason.getCause() != null) {
                    failReason.getCause().printStackTrace();
                }
                mHandler.sendEmptyMessage(0);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap bitmap) {
                L.e("onLoadingComplete ");
                //将图片保存到指定文件夹中
                if (!TextUtils.isEmpty(packetId)) {
                    FileUtil.saveBitmap(packetId, bitmap, Const.DOWNLOAD_FILE_CACHE);
                }
                mHandler.sendEmptyMessage(0);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
    }

    /**
     * 处理最近会话消息列表文本
     *
     * @return
     */
    public SpannableString handlerRecentChatContent(Context mContext, TextView gifTextView, String content) {
        this.mContext = mContext;
        if (content == null) {
            return new SpannableString("");
        }
        content = content.replaceAll(Const.Regex.IMG, "[图片]");
        content = content.replaceAll(Const.Regex.FILE, "[文件]");
        SpannableString ss = new SpannableString(content);
        String regex = Const.Regex.FACE;
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        if (handlerCount(content) < 10) {
            showGifFace(true, m, ss, gifTextView, null);
        } else {
            showGifFace(false, m, ss, gifTextView, null);
        }
        return ss;
    }

    // 统计gif个数
    private int handlerCount(String content) {
        int number = 0;
        String regex = "(\\[\\/\\d{1,2}\\])";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        while (m.find()) {
            number++;
        }
        return number;
    }

}