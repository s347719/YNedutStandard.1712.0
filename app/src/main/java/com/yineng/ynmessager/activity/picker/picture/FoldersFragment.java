package com.yineng.ynmessager.activity.picker.picture;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yineng.ynmessager.R;

/**
 * Created by 贺毅柳 on 2015/12/4 11:42.
 */
public class FoldersFragment extends SubGalleryFragment
{
    private FolderListFragment mFolderListFragment;
    private AlbumFragment mAlbumFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_gallery_folders, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        mFolderListFragment = new FolderListFragment();
        mAlbumFragment = new AlbumFragment();

        mFolderListFragment.setArguments(getArguments()); //把数据参数传递给实际显示的FolderListFragment

        //加载FolderListFragment
        getChildFragmentManager().beginTransaction()
                .add(R.id.gallery_frm_foldersContent, mFolderListFragment, FolderListFragment.FRAGMENT_TAG)
                .add(R.id.gallery_frm_foldersContent, mAlbumFragment, AlbumFragment.FRAGMENT_TAG)
                .hide(mAlbumFragment) //先隐藏AlbumFragment
                .commit();
    }

    void refreshContent()
    {
        mAlbumFragment.refreshContent();
    }
}
