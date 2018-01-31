package com.yineng.ynmessager.activity.picker.picture;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.picker.image.ImageViewerActivity;
import com.yineng.ynmessager.bean.imgpicker.ImageFile;
import com.yineng.ynmessager.bean.imgpicker.ImageFolder;
import com.yineng.ynmessager.util.L;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends BaseActivity
        implements RadioGroup.OnCheckedChangeListener, ViewPager.OnPageChangeListener, View.OnClickListener
{
    private RadioGroup mRadGSwitchBar;
    private ViewPager mPagerContainer;
    private Button mBtnPreview;
    private Button mBtnDone;
    private List<ImageFolder> mGalleryDataList = null;
    private ImageFolder mDcim;
    private AlbumFragment mAlbumFragment;
    private FoldersFragment mFoldersFragment;
    public static final int ACTIVITY_REQUEST_CODE = 1;
    /** 用户取消发送图片操作，返回聊天界面 */
    public static final int RESULT_USER_CANCELED = 2;
    /** 用户完成选择图片操作，返回聊天界面 */
    public static final int RESULT_DONE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        initViews();
    }

    private void initViews()
    {
        mRadGSwitchBar = (RadioGroup) findViewById(R.id.gallery_radG_switchBar);
        mPagerContainer = (ViewPager) findViewById(R.id.gallery_pager_container);
        mBtnPreview = (Button) findViewById(R.id.gallery_btn_preview);
        mBtnDone = (Button) findViewById(R.id.gallery_btn_done);

        initGalleryData();

        mAlbumFragment = new AlbumFragment();
        mFoldersFragment = new FoldersFragment();
        //设置AlbumFragment 中要显示的系统相册数据
        mAlbumFragment.setAlbumImageFolder(mDcim);
        //设置给FoldersFragment中的子Fragment设置文件夹数据
        Bundle folders = new Bundle(1);
        folders.putParcelableArrayList("Folders", new ArrayList<>(mGalleryDataList));
        mFoldersFragment.setArguments(folders);

        ContainerAdapter containerAdapter = new ContainerAdapter(getSupportFragmentManager(),
                new Fragment[]{mAlbumFragment, mFoldersFragment});
        mPagerContainer.setAdapter(containerAdapter);
        mPagerContainer.addOnPageChangeListener(this);
        mRadGSwitchBar.setOnCheckedChangeListener(this);
        mBtnPreview.setOnClickListener(this);
        mBtnDone.setOnClickListener(this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        ImageLoader.getInstance().stop();
    }

    /**
     * 初始化相册图片数据
     */
    private void initGalleryData()
    {
        L.i(mTag, "init GalleryData start...");
        mGalleryDataList = GalleryHelper.list(getContentResolver());

        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        for (ImageFolder folder : mGalleryDataList)
        {
            if (folder.getDirectory().getPath().contains(dcim.getPath()))
            {
                //我日！！这里必须复制新的ImageFolder对象出来，避免引用到mGalleryDataList中的同一个对象引发问题
                mDcim = ImageFolder.from(folder);
                break;
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId)
    {
        switch (checkedId)
        {
            case R.id.gallery_rad_switchAlbum:
                mPagerContainer.setCurrentItem(0);
                break;
            case R.id.gallery_rad_switchOtherFolders:
                mPagerContainer.setCurrentItem(1);
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
        switch (position)
        {
            case 0:
                mRadGSwitchBar.check(R.id.gallery_rad_switchAlbum);
                break;
            case 1:
                mRadGSwitchBar.check(R.id.gallery_rad_switchOtherFolders);
                break;
            default:
                break;
        }

        //每次切换Pager都清空已勾选的图片
        for (ImageFolder imageFolder : mGalleryDataList)
        {
            List<ImageFile> imageFiles = imageFolder.getImages();
            for (ImageFile image : imageFiles)
            {
                if (image != null) {
                    image.setSelected(false);
                }
            }
        }

        //刷新内容显示
        mAlbumFragment.refreshContent();
        mFoldersFragment.refreshContent();
    }

    @Override
    public void onPageScrollStateChanged(int state)
    {

    }

    /**
     * 预览、完成按钮的点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v)
    {
        int currentPage = mPagerContainer.getCurrentItem();

        ArrayList<ImageFile> imageFiles = null;
        if (currentPage == 0)
        {
            imageFiles = new ArrayList<>(mAlbumFragment.getCheckedImageFiles());
        } else
        {
            FolderListFragment folderListFragment = (FolderListFragment) mFoldersFragment
                    .getChildFragmentManager()
                    .findFragmentByTag(FolderListFragment.FRAGMENT_TAG);
            imageFiles = new ArrayList<>(folderListFragment.getCheckedImageFiles());
        }

        if (imageFiles.isEmpty()) {
            return;
        }

        switch (v.getId())
        {
            case R.id.gallery_btn_preview:
                Intent prevIntent = new Intent(this, ImageViewerActivity.class);
                prevIntent.putExtra("ImageFileList", imageFiles);
                startActivity(prevIntent);
                break;
            case R.id.gallery_btn_done:
                Intent doneIntent = new Intent();
                doneIntent.putExtra("selectedImages", imageFiles);
                setResult(Activity.RESULT_OK, doneIntent);
                finish();
                break;
            default:
                break;
        }
    }


    class ContainerAdapter extends FragmentPagerAdapter
    {
        private Fragment[] fragments;

        public ContainerAdapter(FragmentManager fm, Fragment[] fragments)
        {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position)
        {
            return fragments[position];
        }

        @Override
        public int getCount()
        {
            return fragments == null ? 0 : fragments.length;
        }
    }
}
