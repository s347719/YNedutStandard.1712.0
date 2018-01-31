package com.yineng.ynmessager.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.app.DownLoadFile;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.ImageUtil;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 舒欢
 * Created time: 2017/10/11
 * Descreption：
 */

public class DownloadedFilesSrarchAdapter extends RecyclerView.Adapter<DownloadedFilesSrarchAdapter.ViewHolder> {


    private OnItemClickListener mOnItemClickListener;
    private List<DownLoadFile> mData ;
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    /**
     * 默认缩略图片宽度
     */
    public int mBitmapwidth;

    /**
     * 默认缩略图高度
     */
    public int mBitmapheight;

    public DownloadedFilesSrarchAdapter(Context context, List<DownLoadFile> data) {
        mData = data;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        Bitmap mBitmap = BitmapFactory.decodeResource(AppController.getInstance().getResources(), R.mipmap.defalut_image_large);
        mBitmapwidth = mBitmap.getWidth();
        mBitmapheight = mBitmap.getHeight();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = mLayoutInflater.inflate(R.layout.item_downloaded_files_search_item, parent, false);
        final DownloadedFilesSrarchAdapter.ViewHolder holder = new DownloadedFilesSrarchAdapter.ViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (mOnItemClickListener!=null){
                        mOnItemClickListener.onItemClick(holder.getLayoutPosition(),holder);
                    }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DownLoadFile item = getItem(position);
        switch (item.getFileType()){
            case 1:
                holder.downloadedFiles_img_listItem_thumb.setImageBitmap(ImageUtil.getBitmapThumbnail(FileUtil.getFilePath("") + item.getFileName(), mBitmapwidth, mBitmapheight));
                break;
            case 2:
                holder.downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.video);
                break;
            case 3:
                holder.downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.music);
                break;
            case 4:
                holder.downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.word);
                break;
            case 5:
                holder.downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.excel);
                break;
            case 6:
                holder.downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.pdf);
                break;
            case 7:
                holder.downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.ppt);
                break;
            case 8:
                holder.downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.rar);
                break;
            case 9:
                holder.downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.file);
                break;
            default:
                holder.downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.file);
                break;
        }
        holder.downloadedFiles_txt_listItem_fileName.setText(item.getFileName());
        holder.downloadedFiles_txt_listItem_fileSize.setText(FileUtil.formatSize(Long.parseLong(item.getSize())));
        holder.downloadedFiles_txt_listItem_timeStamp.setText(TimeUtil.getDateByDateStr(item.getDataTime(),TimeUtil.FORMAT_DATETIME_MM_DD2));
        // 012 代表聊天 4 流程审批  5  OA申请
        switch (item.getFileSource()){
            case 0:
            case 1:
            case 2:
                holder.downloadedFiles_txt_listItem_fileSource.setText(R.string.downloadFiles_select_source_adapterChat);
                break;
            case 3:
                holder.downloadedFiles_txt_listItem_fileSource.setText(R.string.downloadFiles_select_source_groupshare);
                break;
            case 4:
                holder.downloadedFiles_txt_listItem_fileSource.setText(R.string.downloadFiles_select_source_promise);
                break;
            case 5:
                holder.downloadedFiles_txt_listItem_fileSource.setText(R.string.downloadFiles_select_source_oa);
                break;
            default:
                holder.downloadedFiles_txt_listItem_fileSource.setText(R.string.downloadFiles_select_source_unkown);
                break;
        }
    }

    public void setData(List<DownLoadFile> data) {this.mData = data;}

    public DownLoadFile getItem(int position) {return this.mData.get(position);}

    @Override
    public int getItemCount() {
        return this.mData == null ? 0 : this.mData.size();

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener)
    {
        mOnItemClickListener = onItemClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {

        ImageView downloadedFiles_img_listItem_thumb;
        View downloadFiles_more;
        View view_time;
        TextView downloadedFiles_txt_listItem_fileName;
        TextView downloadedFiles_txt_listItem_timeStamp;
        TextView downloadedFiles_txt_listItem_fileSize;
        TextView downloadedFiles_txt_listItem_fileSource;
        ViewHolder(View itemView)
        {
            super(itemView);
            downloadedFiles_img_listItem_thumb = (ImageView) itemView.findViewById(R.id.downloadedFiles_img_listItem_thumb);
            downloadFiles_more = itemView.findViewById(R.id.downloadFiles_more);
            view_time = itemView.findViewById(R.id.view_time);
            downloadedFiles_txt_listItem_fileName = (TextView) itemView.findViewById(R.id.downloadedFiles_txt_listItem_fileName);
            downloadedFiles_txt_listItem_timeStamp = (TextView) itemView.findViewById(R.id.downloadedFiles_txt_listItem_timeStamp);
            downloadedFiles_txt_listItem_fileSize = (TextView) itemView.findViewById(R.id.downloadedFiles_txt_listItem_fileSize);
            downloadedFiles_txt_listItem_fileSource = (TextView) itemView.findViewById(R.id.downloadedFiles_txt_listItem_fileSource);
        }
    }
}
