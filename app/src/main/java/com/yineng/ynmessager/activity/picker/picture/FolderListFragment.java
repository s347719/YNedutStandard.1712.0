package com.yineng.ynmessager.activity.picker.picture;

import android.content.Context;
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
import com.yineng.ynmessager.activity.BaseFragment;
import com.yineng.ynmessager.bean.imgpicker.ImageFile;
import com.yineng.ynmessager.bean.imgpicker.ImageFolder;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;
import com.yineng.ynmessager.view.recyclerview.decoration.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by 贺毅柳 on 2015/12/8 17:30.
 */
public class FolderListFragment extends BaseFragment implements OnItemClickListener<FolderListAdapter.ViewHolder>
{
    public static final String FRAGMENT_TAG = "FolderListFragment";
    private RecyclerView mRcy_folderListContent;
    private FolderListAdapter mFolderAdapter;
    private FragmentManager mFragmentManager;
    private AlbumFragment mAlbumFragment;
    private List<ImageFolder> mImageFolderList;
    public static final int COLUMN_COUNT = 2; // 网格每行列数

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_gallery_folderlist, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        mRcy_folderListContent = (RecyclerView) view.findViewById(R.id.gallery_rcy_folderListContent);

        Context context = getContext();
        mFragmentManager = getFragmentManager();

        mAlbumFragment = (AlbumFragment) mFragmentManager.findFragmentByTag(AlbumFragment.FRAGMENT_TAG);

        mImageFolderList = getArguments().getParcelableArrayList("Folders");
        if (mImageFolderList != null) {
            addBackItem(mImageFolderList);
        }

        mFolderAdapter = new FolderListAdapter(context);
        mFolderAdapter.setData(mImageFolderList);
        mFolderAdapter.setOnItemClickListener(this);
        mRcy_folderListContent.setLayoutManager(new GridLayoutManager(context, COLUMN_COUNT));
        RecyclerView.ItemDecoration itemDecoration = new GridSpacingItemDecoration(COLUMN_COUNT,
                context.getResources().getDimensionPixelSize(R.dimen.gallery_folderList_item_img_spacing),
                true);
        mRcy_folderListContent.addItemDecoration(itemDecoration);
        mRcy_folderListContent.setHasFixedSize(true);
        mRcy_folderListContent.setAdapter(mFolderAdapter);
    }

    @Override
    public void onItemClick(int position, FolderListAdapter.ViewHolder viewHolder)
    {
        mAlbumFragment.setAlbumImageFolder(mFolderAdapter.getItem(position));

        mFragmentManager.beginTransaction()
                .show(mAlbumFragment)
                .hide(this)
                .commit();
    }

    /**
     * 给每个ImageFolder中添加一个返回按键，用null表示
     *
     * @param imageFolderList
     */
    private void addBackItem(List<ImageFolder> imageFolderList)
    {
        for (ImageFolder imageFolder : imageFolderList)
        {
            imageFolder.getImages().add(0, null);
        }
    }

    @NonNull
    List<ImageFile> getCheckedImageFiles()
    {
        if (mImageFolderList == null) {
            return Collections.emptyList();
        }

        ArrayList<ImageFile> checkedList = new ArrayList<>();
        for (ImageFolder folder : mImageFolderList)
        {
            List<ImageFile> images = folder.getImages();
            for (ImageFile img : images)
            {
                if (img != null && img.isSelected()) {
                    checkedList.add(img);
                }
            }
        }
        return checkedList;
    }

}
