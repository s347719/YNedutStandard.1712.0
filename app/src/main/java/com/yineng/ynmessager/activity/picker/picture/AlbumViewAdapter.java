package com.yineng.ynmessager.activity.picker.picture;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.imgpicker.ImageFile;
import com.yineng.ynmessager.bean.imgpicker.ImageFolder;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;

import java.util.List;

/**
 * Created by 贺毅柳 on 2015/12/7 10:59.
 */
public class AlbumViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private LayoutInflater mInflater;
    private ImageFolder mAlbumFolder;
    private List<ImageFile> mImageFileList = null;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mDisplayImageOptions;
    private OnItemClickListener<RecyclerView.ViewHolder> mOnItemClickListener;
    static final int VIEW_TYPE_BACK = 0;
    static final int VIEW_TYPE_IMAGE = 1;

    public AlbumViewAdapter(Context context)
    {
        mInflater = LayoutInflater.from(context);
        mImageLoader = ImageLoader.getInstance();
        mDisplayImageOptions = new DisplayImageOptions.Builder().cloneFrom(AppController.getInstance().mImageLoaderDisplayOptions)
                .displayer(new FadeInBitmapDisplayer(400))
                .build();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        final RecyclerView.ViewHolder holder;
        View itemView;
        if (viewType == VIEW_TYPE_BACK)
        {
            itemView = mInflater.inflate(R.layout.item_gallery_ablumbackview, parent, false);
            holder = new BackViewHolder(itemView);
        } else
        {
            itemView = mInflater.inflate(R.layout.item_gallery_ablumview, parent, false);
            holder = new ImageViewHolder(itemView);
            ((ImageViewHolder) holder).checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    ImageFile imageFile = getItem(holder.getLayoutPosition());
                    imageFile.setSelected(isChecked);
                }
            });
        }

        itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(holder.getLayoutPosition(), holder);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder _holder, int position)
    {
        int viewType = getItemViewType(position);

        if (viewType == VIEW_TYPE_BACK)
        {
            BackViewHolder holder = (BackViewHolder) _holder;

            holder.foldName.setText(mAlbumFolder.getDirectory().getName());
        } else if (viewType == VIEW_TYPE_IMAGE)
        {
            ImageFile item = getItem(position);
            ImageViewHolder holder = (ImageViewHolder) _holder;

            holder.checkbox.setChecked(item.isSelected());
            mImageLoader.displayImage("file://" + item.getPath(), holder.picture, mDisplayImageOptions);
        }
    }

    @Override
    public int getItemCount()
    {
        return mImageFileList == null ? 0 : mImageFileList.size();
    }

    @Override
    public int getItemViewType(int position)
    {
        return getItem(position) == null ? VIEW_TYPE_BACK : VIEW_TYPE_IMAGE;
    }

    public void setData(ImageFolder imageFolder)
    {
        mAlbumFolder = imageFolder;
        mImageFileList = imageFolder == null ? null : imageFolder.getImages();
    }

    public ImageFile getItem(int position)
    {
        return mImageFileList.size()==0 ? mImageFileList.get(position) : mImageFileList.get(mImageFileList.size()-position-1);
    }

    public void setOnItemClickListener(OnItemClickListener<RecyclerView.ViewHolder> onItemClickListener)
    {
        mOnItemClickListener = onItemClickListener;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder
    {
        ImageView picture;
        CheckBox checkbox;

        public ImageViewHolder(View itemView)
        {
            super(itemView);
            picture = (ImageView) itemView.findViewById(R.id.gallery_img_albumItem_picture);
            checkbox = (CheckBox) itemView.findViewById(R.id.gallery_img_albumItem_checkBox);
        }
    }

    static class BackViewHolder extends RecyclerView.ViewHolder
    {
        TextView foldName;

        public BackViewHolder(View itemView)
        {
            super(itemView);
            foldName = (TextView) itemView.findViewById(R.id.gallery_txt_albumItem_backToParentFolder_name);
        }
    }
}
