package com.yineng.ynmessager.activity.picker.picture;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.imgpicker.ImageFolder;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;
import com.yineng.ynmessager.view.recyclerview.OnItemLongClickListener;

import java.util.List;

/**
 * Created by 贺毅柳 on 2015/12/10 14:12.
 */
public class FolderListAdapter extends RecyclerView.Adapter<FolderListAdapter.ViewHolder>
{
    private LayoutInflater mInflater;
    private List<ImageFolder> mData;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mDisplayImageOptions;
    private OnItemClickListener<ViewHolder> mOnItemClickListener;
    private OnItemLongClickListener<ViewHolder> mOnItemLongClickListener;

    FolderListAdapter(Context context)
    {
        mInflater = LayoutInflater.from(context);
        mImageLoader = ImageLoader.getInstance();
        mDisplayImageOptions = new DisplayImageOptions.Builder().cloneFrom(AppController.getInstance().mImageLoaderDisplayOptions)
                .displayer(new FadeInBitmapDisplayer(400))
                .build();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = mInflater.inflate(R.layout.item_gallery_folderlist, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(holder.getLayoutPosition(), holder);
                }
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                if (mOnItemClickListener != null) {
                    return mOnItemLongClickListener.onItemLongClick(holder.getLayoutPosition(), holder);
                }

                return false;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        ImageFolder imageFolder = getItem(position);

        mImageLoader.displayImage("file://" + imageFolder.getImages().get(1), holder.cover, mDisplayImageOptions);
        holder.name.setText(imageFolder.getDirectory().getName());
    }

    @Override
    public int getItemCount()
    {
        return mData == null ? 0 : mData.size();
    }

    public ImageFolder getItem(int position)
    {
        return mData == null ? null : mData.get(position);
    }

    public void setData(List<ImageFolder> data)
    {
        mData = data;
    }

    public void setOnItemClickListener(OnItemClickListener<ViewHolder> onItemClickListener)
    {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<ViewHolder> onItemLongClickListener)
    {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView cover;
        TextView name;

        public ViewHolder(View itemView)
        {
            super(itemView);
            cover = (ImageView) itemView.findViewById(R.id.gallery_img_folderListItem_cover);
            name = (TextView) itemView.findViewById(R.id.gallery_txt_folderListItem_name);
        }
    }

}
