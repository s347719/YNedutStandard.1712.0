package com.yineng.ynmessager.activity.app.internship.report;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.app.Internship.AttachFile;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;

import java.util.List;

/**
 * 附件adapter
 * Created by yineng on 2015/12/28.
 */
public class ReportAttachFileAdapter extends RecyclerView.Adapter<ReportAttachFileAdapter.FileHolder>{

    private Context mContext;
    private List<AttachFile> mFiles;
    private OnItemClickListener mOnItemClickListener;

    private int position;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public ReportAttachFileAdapter(Context context) {
        mContext = context;
    }

    public void setAttachFiles(List<AttachFile> mFiles) {
        this.mFiles = mFiles;
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @Override
    public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_dgsx_attach_file, parent, false);
        final FileHolder holder = new FileHolder(itemView);
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
        itemView.setOnLongClickListener(new View.OnLongClickListener(){

            @Override
            public boolean onLongClick(View v) {
                setPosition(holder.getLayoutPosition());
                return false;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(FileHolder holder, int position) {
        AttachFile attachFile = mFiles.get(position);
        holder.mTvAttachFileName.setText(attachFile.getmFileName());
        holder.mTVAttachFileSize.setText(attachFile.getmFileSize());
        holder.mTVDownloadPercent.setTextColor(Color.parseColor("#12b5b0"));
        holder.mTVDownloadPercent.setVisibility(View.GONE);
        holder.mPbDownloadView.setProgress(0);
        switch (attachFile.getmStatus()) {
            case BaseChatMsgEntity.DOWNLOAD_ING:
                int downloadPercent = attachFile.getmDownloadProgress();
                holder.mPbDownloadView.setProgress(downloadPercent);
                holder.mTVDownloadPercent.setVisibility(View.VISIBLE);
                holder.mTVDownloadPercent.setText(downloadPercent+"%");
                break;
            case BaseChatMsgEntity.DOWNLOAD_SUCCESS:
                holder.mPbDownloadView.setProgress(100);
                break;
            case BaseChatMsgEntity.DOWNLOAD_FAILED:
                holder.mTVDownloadPercent.setVisibility(View.VISIBLE);
                holder.mTVDownloadPercent.setText("下载失败");
                holder.mTVDownloadPercent.setTextColor(Color.RED);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mFiles != null ? mFiles.size() : 0;
    }

    public int getItemPosition(AttachFile sharedFile)
    {
        if (mFiles == null || mFiles.size() <= 0) {
            return -1;
        }
        return mFiles.indexOf(sharedFile);
    }

    public AttachFile getItem(int position) {
        if (mFiles == null || mFiles.size() <= 0) {
            return null;
        }
        return mFiles.get(position);
    }

    public void setItem(int pos,AttachFile tempFile) {
        mFiles.set(pos,tempFile);
    }

    public class FileHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{

        ProgressBar mPbDownloadView;
        ImageView mIvThumbImgView;
        TextView mTvAudioDurtion;
        TextView mTvAttachFileName;
        TextView mTVAttachFileSize;
        TextView mTVDownloadPercent;

        public FileHolder(View itemView) {
            super(itemView);
            mPbDownloadView = (ProgressBar) itemView.findViewById(R.id.pb_attachfile_download);
            mTvAttachFileName = (TextView) itemView.findViewById(R.id.tv_attachfile_name);
            mTVAttachFileSize = (TextView) itemView.findViewById(R.id.tv_attachfile_size);
            mTVDownloadPercent = (TextView) itemView.findViewById(R.id.tv_attachfile_download_percent);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("附件操作");
            menu.add(Menu.NONE, 0,0,"下载");
            menu.add(Menu.NONE, 1,1,"重命名");
            menu.add(Menu.NONE, 2,2,"删除");
        }
    }
}
