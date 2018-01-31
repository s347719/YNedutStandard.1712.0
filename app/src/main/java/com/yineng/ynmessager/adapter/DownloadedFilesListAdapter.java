package com.yineng.ynmessager.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.yineng.ynmessager.util.SystemUtil;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;
import com.yineng.ynmessager.view.recyclerview.OnItemLongClickListener;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by 舒欢
 * Created time: 2017/10/11
 * Descreption：
 */

public class DownloadedFilesListAdapter extends RecyclerView.Adapter<DownloadedFilesListAdapter.ViewHolder> {

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private ArrayList<DownLoadFile> mData ;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private boolean isSwitchMode = false;

    /**
     * 默认缩略图片宽度
     */
    public int mBitmapwidth;

    /**
     * 默认缩略图高度
     */
    public int mBitmapheight;


    public DownloadedFilesListAdapter(Context context,ArrayList<DownLoadFile> data) {
        mData = data;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        Bitmap mBitmap = BitmapFactory.decodeResource(AppController.getInstance().getResources(), R.mipmap.defalut_image_large);
        mBitmapwidth = mBitmap.getWidth();
        mBitmapheight = mBitmap.getHeight();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = mLayoutInflater.inflate(R.layout.item_downloaded_files_listitem, parent, false);
        final DownloadedFilesListAdapter.ViewHolder holder = new DownloadedFilesListAdapter.ViewHolder(view);
        holder.downlod_view_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSwitchMode){
                    mOnItemClickListener.onItemClick(holder.getLayoutPosition(),holder);
                }else {
                    File item = new File(FileUtil.getFilePath(mData.get(holder.getLayoutPosition()).getFileName()));
                    if (!SystemUtil.execLocalFile(mContext, item)) {
                        ToastUtil.toastAlerMessageCenter(mContext, R.string.downloadedFiles_noAppsForThisFile, 500);
                    }
                }
            }
        });

        holder.downloadFiles_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener!=null){
                    mOnItemClickListener.onItemClick(holder.getLayoutPosition(),holder);
                }
            }
        });

        holder.downlod_view_click.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onItemLongClickListener!=null){
                    onItemLongClickListener.onItemLongClick(holder.getLayoutPosition(),holder);
                }
                return true;
            }
        });

        return holder;
    }

    public void setIsSwitchMode(boolean mode){
        this.isSwitchMode = mode;

    }
    public boolean getSwitchMode(){

        return isSwitchMode;
    }

    public interface OnRecyclerItemLongListener{
        void onItemLongClick(View view,int position);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DownLoadFile item = getItem(position);
        if (mData.size()==1){
            holder.view_time.setVisibility(View.VISIBLE);
            holder.time_tv.setText(TimeUtil.getDateByMillisecond(Long.parseLong(item.getDataTime()),TimeUtil.FORMAT_DATE2));
        }else {
            if (position==0){
                holder.view_time.setVisibility(View.VISIBLE);
                holder.time_tv.setText(TimeUtil.getDateByMillisecond(Long.parseLong(item.getDataTime()),TimeUtil.FORMAT_DATE2));
            }else {
                if (TimeUtil.isSameYearAndMonth(item.getDataTime(), getItem(position - 1).getDataTime())) {
                    holder.view_time.setVisibility(View.GONE);
                } else {
                    holder.view_time.setVisibility(View.VISIBLE);
                    holder.time_tv.setText(TimeUtil.getDateByMillisecond(Long.parseLong(item.getDataTime()), TimeUtil.FORMAT_DATE2));
                }
            }
        }
        if (isSwitchMode){
            holder.downloadFiles_check.setVisibility(View.VISIBLE);
            holder.downloadFiles_more.setVisibility(View.GONE);
            if (item.isCheck()){
                holder.downloadFiles_check.setBackgroundResource(R.mipmap.download_select);
            }else {
                holder.downloadFiles_check.setBackgroundResource(R.mipmap.download_not_select);
            }
        }else {
            holder.downloadFiles_more.setVisibility(View.VISIBLE);
            holder.downloadFiles_check.setVisibility(View.GONE);
        }
        switch (item.getFileType()){
            case 1:
                if (!TextUtils.isEmpty(item.getFileName())){
                    String end  = item.getFileName().substring(item.getFileName().lastIndexOf("."),item.getFileName().length());
                    if (end.endsWith("png")||end.endsWith("jpg")||end.endsWith("jpeg")){
                        holder.downloadedFiles_img_listItem_thumb.setImageBitmap(ImageUtil.getBitmapThumbnail(FileUtil.getFilePath("") + item.getFileName(), mBitmapwidth, mBitmapheight));
                    }else {
                        holder.downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.jpg);
                    }
                }else {
                    holder.downloadedFiles_img_listItem_thumb.setImageResource(R.mipmap.jpg);
                }

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
        holder.downloadedFiles_txt_listItem_fileSize.setText(FileUtil.FormatFileSize(Long.parseLong(item.getSize())));
        holder.downloadedFiles_txt_listItem_timeStamp.setText(TimeUtil.getDateByMillisecond(Long.parseLong(item.getDataTime()),TimeUtil.FORMAT_DATETIME_MM_DD2));
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

    public void setData(ArrayList<DownLoadFile> data) {this.mData = data;}

    public ArrayList<DownLoadFile> getData(){
        return this.mData;
    }
    public DownLoadFile getItem(int position) {return this.mData.get(position);}

    @Override
    public int getItemCount() {
        return this.mData == null ? 0 : this.mData.size();

    }



    public void setOnItemClickListener(OnItemClickListener onItemClickListener)
    {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnLongClickListenet(OnItemLongClickListener onLongClickListener){
        onItemLongClickListener = onLongClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView downloadedFiles_img_listItem_thumb;
        View downloadFiles_more;
        View view_time;
        View downlod_view_click;
        TextView downloadedFiles_txt_listItem_fileName;
        TextView downloadedFiles_txt_listItem_timeStamp;
        TextView downloadedFiles_txt_listItem_fileSize;
        TextView downloadedFiles_txt_listItem_fileSource;
        TextView time_tv;
        ImageView downloadFiles_check;
        ViewHolder(View itemView)
        {
            super(itemView);
            downloadedFiles_img_listItem_thumb = (ImageView) itemView.findViewById(R.id.downloadedFiles_img_listItem_thumb);
            downloadFiles_check = (ImageView) itemView.findViewById(R.id.downloadFiles_check);
            downloadFiles_more = itemView.findViewById(R.id.downloadFiles_more);
            view_time = itemView.findViewById(R.id.view_time);
            downlod_view_click = itemView.findViewById(R.id.downlod_view_click);
            downloadedFiles_txt_listItem_fileName = (TextView) itemView.findViewById(R.id.downloadedFiles_txt_listItem_fileName);
            downloadedFiles_txt_listItem_timeStamp = (TextView) itemView.findViewById(R.id.downloadedFiles_txt_listItem_timeStamp);
            downloadedFiles_txt_listItem_fileSize = (TextView) itemView.findViewById(R.id.downloadedFiles_txt_listItem_fileSize);
            downloadedFiles_txt_listItem_fileSource = (TextView) itemView.findViewById(R.id.downloadedFiles_txt_listItem_fileSource);
            time_tv = (TextView) itemView.findViewById(R.id.time_tv);
        }

    }
}
