package com.yineng.ynmessager.activity.picker.picture;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.picker.image.ImageViewerActivity;
import com.yineng.ynmessager.bean.imgpicker.ImageFile;
import com.yineng.ynmessager.bean.imgpicker.ImageFolder;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;
import com.yineng.ynmessager.view.recyclerview.decoration.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by 贺毅柳 on 2015/12/4 11:42.
 */
public class AlbumFragment extends SubGalleryFragment implements OnItemClickListener<RecyclerView.ViewHolder>
{
    public static final String FRAGMENT_TAG = "AlbumFragment";
    private ImageFolder mImageFolder = null;
    private RecyclerView mRcyContent;
    private AlbumViewAdapter mAlbumAdapter;
    private FragmentManager mFragmentManager;
    private FolderListFragment mFolderListFragment;
    // 网格每行列数
    public static final int COLUMN_COUNT = 3;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_gallery_album, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        mRcyContent = (RecyclerView) view.findViewById(R.id.gallery_rcy_albumContent);

        Context context = getContext();
        mFragmentManager = getFragmentManager();

        mFolderListFragment = (FolderListFragment) mFragmentManager.findFragmentByTag(FolderListFragment.FRAGMENT_TAG);

        mAlbumAdapter = new AlbumViewAdapter(context);
        mAlbumAdapter.setData(mImageFolder);
        mAlbumAdapter.setOnItemClickListener(this);
        mRcyContent.setLayoutManager(new GridLayoutManager(context, COLUMN_COUNT));
        RecyclerView.ItemDecoration itemDecoration = new GridSpacingItemDecoration(COLUMN_COUNT,
                context.getResources().getDimensionPixelSize(R.dimen.gallery_album_item_spacing),
                true);
        mRcyContent.addItemDecoration(itemDecoration);
        mRcyContent.setAdapter(mAlbumAdapter);
    }

    void setAlbumImageFolder(@Nullable ImageFolder imageFolder)
    {
        if (mAlbumAdapter != null)
        {
            mAlbumAdapter.setData(imageFolder);
            mAlbumAdapter.notifyDataSetChanged();
        }
        mImageFolder = imageFolder;
    }

    /**
     * 刷新Fragment页面内容
     */
    void refreshContent()
    {
        //当前如果是被隐藏的状态的话,就不用更新界面了，避免尝试重绘界面，浪费CPU和内存
        boolean isVisible = isVisible();
        if(mAlbumAdapter != null && isVisible) {
            mAlbumAdapter.notifyDataSetChanged();
        }
        L.i(mTag,toString() + " isVisible when refreshContent() -> " + isVisible);
    }

    /**
     * 获取已勾选的图片
     *
     * @return
     */
    @NonNull
    List<ImageFile> getCheckedImageFiles()
    {
        if (mImageFolder == null) {
            return Collections.emptyList();
        }

        ArrayList<ImageFile> checkedList = new ArrayList<>();
        List<ImageFile> imageFiles = mImageFolder.getImages();
        for (ImageFile img : imageFiles)
        {
            if (img.isSelected()) {
                checkedList.add(img);
            }
        }
        return checkedList;
    }

    @Override
    public void onItemClick(int position, RecyclerView.ViewHolder viewHolder)
    {
        int viewType = mAlbumAdapter.getItemViewType(position);
        if (viewType == AlbumViewAdapter.VIEW_TYPE_BACK)
        {
            //点击返回相册目录的按钮
            mFragmentManager.beginTransaction()
                    .show(mFolderListFragment)
                    .hide(this)
                    .commit();
        } else if (viewType == AlbumViewAdapter.VIEW_TYPE_IMAGE)
        {
            //点击图片
            int defPosition = position;
            ArrayList<ImageFile> imageFileList = new ArrayList<>(mImageFolder.getImages());
            // 如果是在相册文件夹中触发的点击，
            // 那么图片列表中第一个是null表示的返回按钮，必须去掉，
            // 然后默认显示的position减1
            if(imageFileList.get(0) == null)
            {
                imageFileList.remove(0);
//                --defPosition;
            }
            Collections.reverse(imageFileList);
            // 启动ImageViewerActivity
            Intent intent = new Intent(mParentActivity, ImageViewerActivity.class);
            intent.putExtra("ImageFileList", imageFileList);
            intent.putExtra("DefPosition",defPosition);
            startActivity(intent);
        }
    }
}
