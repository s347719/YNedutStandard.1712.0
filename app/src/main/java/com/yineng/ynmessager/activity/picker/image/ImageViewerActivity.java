package com.yineng.ynmessager.activity.picker.image;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.ortiz.touch.HackyViewPager;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.app.DownLoadFile;
import com.yineng.ynmessager.bean.groupsession.GroupChatMsgEntity;
import com.yineng.ynmessager.bean.imgpicker.ImageFile;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.bean.p2psession.P2PChatMsgEntity;
import com.yineng.ynmessager.db.DownLoadFileTb;
import com.yineng.ynmessager.db.P2PChatMsgDao;
import com.yineng.ynmessager.db.dao.DisGroupChatDao;
import com.yineng.ynmessager.db.dao.GroupChatDao;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.view.photoview.PhotoViewAttacher;

import java.util.ArrayList;
import java.util.List;

/**
 * Intent Extra 参数：
 * <ul>
 * <li><b>ImageFileList</b>: 必需，<code>ArrayList&lt;ImageFile&gt;</code>类型</li>
 * <li><b>DefPosition</b>：可选，<code>int</code>类型，默认<code>0</code></li>
 * <li><b>IsShowOptionsMenu</b>：可选，<code>boolean</code>类型，默认<code>true</code></li>
 * <li><b>ImageScaleType</b>：可选，<code>{@link ImageScaleType}</code>类型，默认<code>{@link ImageScaleType#NONE_SAFE}</code></li>
 * </ul>
 *
 * @author 贺毅柳
 */
public class ImageViewerActivity extends BaseActivity implements OnPageChangeListener
{
    private static ViewerAdapter mViewerAdapter;
    /**
     *
     存放多张图片预览的时候packetId的数据链，只有在聊天内容跳转过来才会有
     */
    private ArrayList<String> packetIdList = new ArrayList<>();
    ArrayList<ImageFile> mImageFileList;
    private int type =-1;
    private String path;

    /**
     * 消息数据库工具
     */
    private P2PChatMsgDao mP2PChatMsgDao;

    private GroupChatDao mGroupChatDao;
    private DisGroupChatDao mDisGroupChatDao;
    private DownLoadFileTb downLoadFileTb;
    private Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        initViews();
    }

    private void initViews()
    {
        Intent intent = getIntent();
        //      保存的图片地址
        path = FileUtil.getUserSDPath(false, FileUtil.mFilePath);
        mImageFileList = (ArrayList<ImageFile>) intent.getSerializableExtra("ImageFileList");
        packetIdList = (ArrayList<String>) intent.getSerializableExtra("packetId");
        boolean downLoad = intent.getBooleanExtra("downLoad", false);
        type = intent.getIntExtra("type",-1);
        int defPosition = intent.getIntExtra("DefPosition", 0);
        defPosition = defPosition > (mImageFileList.size() - 1) ? 0 : defPosition;
        if (downLoadFileTb==null){
            downLoadFileTb = new DownLoadFileTb(this);
        }
        switch (type) {
            case Const.CHAT_TYPE_P2P:
                mP2PChatMsgDao = new P2PChatMsgDao(this);
                break;
            case Const.CHAT_TYPE_GROUP:
                mGroupChatDao = new GroupChatDao(this);
                break;
            case Const.CHAT_TYPE_DIS:
                mDisGroupChatDao = new DisGroupChatDao(this);
                break;
            default:
                break;
        }
        ImageView imageDownload = (ImageView) findViewById(R.id.image_download);
        if (downLoad){
            imageDownload.setVisibility(View.VISIBLE);
        }else {
            imageDownload.setVisibility(View.GONE);
        }
        imageDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageDetailFragment fragment = mViewerAdapter.getCurrentFragment();
                int pos = mViewerAdapter.getPos();
                    BitmapDrawable drawable = (BitmapDrawable) fragment.mImageView.getDrawable();
                    if ( drawable!=null)
                    {
                        bitmap = drawable.getBitmap();
                        saveDownLoadFile(packetIdList.get(pos));
                        FileUtil.saveBitmap(packetIdList.get(pos),bitmap,path);
                        ToastUtil.toastAlerMessageCenter(ImageViewerActivity.this,"已保存至SD卡"+path,500);

                    }else {
                        ToastUtil.toastAlerMessageCenter(ImageViewerActivity.this,"保存失败",500);
                    }
            }
        });
        HackyViewPager viewPager = (HackyViewPager) findViewById(R.id.imageViewer_pager_viewer);
        viewPager.setOffscreenPageLimit(6);
        mViewerAdapter = new ViewerAdapter(getSupportFragmentManager());
        mViewerAdapter.setData(mImageFileList,packetIdList);
        viewPager.setAdapter(mViewerAdapter);
        viewPager.setCurrentItem(defPosition);
        viewPager.addOnPageChangeListener(this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        ImageLoader.getInstance().stop();
        packetIdList.clear();
        mImageFileList.clear();
    }

    private class ViewerAdapter extends FragmentStatePagerAdapter
    {
        List<ImageFile> data;
        List<String> packetList;
        FragmentManager tManager;
        ImageDetailFragment currentFragment;
        int pos;
        ViewerAdapter(FragmentManager fm)
        {
            super(fm);
            tManager = fm;
        }

        void setData(List<ImageFile> data,List<String> pack)
        {
            this.data = data;
            this.packetList = pack;
        }
        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }
        @Override
        public Fragment getItem(int position) {
            ImageFile imageFile = data.get(position);
            String imageUri = imageFile.getPath().startsWith("http:") ? imageFile.getPath().replace("http:/","http://") :"file://" + imageFile.getPath();
            L.i(mTag,"点击查看的图片地址："+imageUri);
            ImageDetailFragment fragment = ImageDetailFragment.newInstance(imageUri,type);
            fragment.setClick(new PhotoViewAttacher.OnPhotoTapListener() {

                @Override
                public void onPhotoTap(View view, float x, float y) {
                    finish();
                }
            });
            return fragment;

        }

        //保存当前的碎片和当前的位置
        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            currentFragment = (ImageDetailFragment) object;
            pos = position;
            super.setPrimaryItem(container, position, object);
        }
        public int getPos(){
            return pos;
        }

        private ImageDetailFragment getCurrentFragment(){
            return currentFragment;
        }
        //保证如果有刷新操作会刷新
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup arg0, int arg1) {

            return super.instantiateItem(arg0, arg1);

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            FragmentTransaction trans = tManager.beginTransaction();
            trans.remove((Fragment) object);
            trans.commit();
            super.destroyItem(container, position, object);
        }

    }

    /**
     *保存下载图片信息到信息表中
     */
    private void saveDownLoadFile(@NonNull String packetId)
    {
        DownLoadFile downLoadFile = new DownLoadFile();
        switch (type) {
            case Const.CHAT_TYPE_P2P:
                P2PChatMsgEntity chatMsgEntity = mP2PChatMsgDao.queryInfoByPacketId(packetId);
                MessageBodyEntity messageBodyEntity = JSON.parseObject(chatMsgEntity.getMessage(), MessageBodyEntity.class);
                String name = messageBodyEntity.getImages().get(0).getSdcardPath();
                if (!TextUtils.isEmpty(name)){
                    int lastIndexOf = name.lastIndexOf("/");
                    downLoadFile.setFileName( name.substring(lastIndexOf+1,name.length()));
                }else {
                    downLoadFile.setFileName(packetId+".jpg");
                }
                downLoadFile.setFileName(packetId+".jpg");
                downLoadFile.setFileSource(type);
                downLoadFile.setPacketId(packetId);
                downLoadFile.setFileId(chatMsgEntity.getChatUserNo());
                downLoadFile.setSendUserNo(chatMsgEntity.getIsSend()==0 ? LastLoginUserSP.getLoginUserNo(this) :chatMsgEntity.getChatUserNo());
                downLoadFile.setIsSend(chatMsgEntity.getIsSend());
                downLoadFile.setFileType(DownLoadFile.FILETYPE_IMG);
                downLoadFile.setDataTime(String.valueOf(System.currentTimeMillis()));
                downLoadFile.setSize(messageBodyEntity.getImages().get(0).getSize());
                downLoadFileTb.saveOrUpdate(downLoadFile);
                break;
            case Const.CHAT_TYPE_GROUP:
                GroupChatMsgEntity groupChatMsgEntity = mGroupChatDao.queryInfoByPacketId(packetId);
                downLoadFile.setFileName(packetId+".jpg");
                downLoadFile.setFileSource(type);
                downLoadFile.setPacketId(packetId);
                downLoadFile.setFileId(groupChatMsgEntity.getGroupId());
                downLoadFile.setSendUserNo(groupChatMsgEntity.getChatUserNo());
                downLoadFile.setIsSend(groupChatMsgEntity.getIsSend());
                downLoadFile.setFileType(DownLoadFile.FILETYPE_IMG);
                downLoadFile.setDataTime(String.valueOf(System.currentTimeMillis()));
                downLoadFile.setSize(JSON.parseObject(groupChatMsgEntity.getMessage(), MessageBodyEntity.class).getImages().get(0).getSize());
                downLoadFileTb.saveOrUpdate(downLoadFile);
                break;
            case Const.CHAT_TYPE_DIS:
                GroupChatMsgEntity disChatMsgEntity1 = mDisGroupChatDao.queryInfoByPacketId(packetId);
                downLoadFile.setFileName(packetId+".jpg");
                downLoadFile.setFileSource(type);
                downLoadFile.setPacketId(packetId);
                downLoadFile.setFileId(disChatMsgEntity1.getGroupId());
                downLoadFile.setSendUserNo(disChatMsgEntity1.getChatUserNo());
                downLoadFile.setIsSend(disChatMsgEntity1.getIsSend());
                downLoadFile.setFileType(DownLoadFile.FILETYPE_IMG);
                downLoadFile.setDataTime(String.valueOf(System.currentTimeMillis()));
                downLoadFile.setSize( JSON.parseObject(disChatMsgEntity1.getMessage(), MessageBodyEntity.class).getImages().get(0).getSize());
                downLoadFileTb.saveOrUpdate(downLoadFile);
                break;
            default:
                break;
        }

    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {

    }

    @Override
    public void onPageSelected(int position)
    {
    }

    @Override
    public void onPageScrollStateChanged(int state)
    {

    }
}
