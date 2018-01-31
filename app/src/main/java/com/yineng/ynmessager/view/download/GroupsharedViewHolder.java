package com.yineng.ynmessager.view.download;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.AppController;

/**
 * @author by 舒欢
 *         Created time: 2017/11/10
 *         Descreption：
 */

public class GroupsharedViewHolder extends RecyclerView.ViewHolder{


    public ImageView thumb;
    public Button taskActionBtn;
    public TextView fileName;
    public  TextView fileSize;
    public TextView owner;
    public TextView timestamp;
    public ProgressBar downloadBar;
    public TextView status_tv;

    public GroupsharedViewHolder(View itemView) {
        super(itemView);
        thumb = (ImageView) itemView.findViewById(R.id.groupSharedFiles_img_listItem_thumb);
        taskActionBtn = (Button) itemView.findViewById(R.id.groupSharedFiles_btn_listItem_openUp);
        fileName = (TextView) itemView.findViewById(R.id.groupSharedFiles_txt_listItem_fileName);
        fileSize = (TextView) itemView.findViewById(R.id.groupSharedFiles_txt_listItem_fileSize);
        owner = (TextView) itemView.findViewById(R.id.groupSharedFiles_txt_listItem_owner);
        timestamp = (TextView) itemView.findViewById(R.id.groupSharedFiles_txt_listItem_timestamp);
        downloadBar = (ProgressBar) itemView.findViewById(R.id.groupSharedFiles_prg_listItem_downloadBar);
        status_tv = (TextView) itemView.findViewById(R.id.groupSharedFiles_txt_listItem_status);
    }

    private View findViewById(final int id) {
        return itemView.findViewById(id);
    }


    /**
     * viewHolder position
     */
    public int position;
    /**
     * download id
     */
    public int id;

    public void update(final int id, final int position) {
        this.id = id;
        this.position = position;
    }


    public void updateDownloaded() {
        downloadBar.setVisibility(View.GONE);
        downloadBar.setMax(1);
        downloadBar.setProgress(1);
        status_tv.setText(R.string.tasks_manager_demo_status_completed);
        taskActionBtn.setText(R.string.scan);
    }

    public void updateNotDownloaded(final int status, final long sofar, final long total) {
        downloadBar.setVisibility(View.VISIBLE);
        if (sofar > 0 && total > 0) {
            final float percent = sofar
                    / (float) total;
            downloadBar.setMax(100);
            downloadBar.setProgress((int) (percent * 100));
        } else {
            downloadBar.setMax(1);
            downloadBar.setProgress(0);
        }

        switch (status) {
            case FileDownloadStatus.error:
                status_tv.setText(R.string.tasks_manager_demo_status_error);
                break;
            case FileDownloadStatus.paused:
                status_tv.setText(R.string.tasks_manager_demo_status_paused);
                break;
            default:
                status_tv.setText(R.string.tasks_manager_demo_status_not_downloaded);
                break;
        }
        taskActionBtn.setText(R.string.start);
    }

    public void updateDownloading(final int status, final long sofar, final long total) {
        final float percent = sofar / (float) total;
        if (downloadBar.getVisibility()!=View.VISIBLE){
            downloadBar.setVisibility(View.VISIBLE);
        }
        downloadBar.setMax(100);
        downloadBar.setProgress((int) (percent * 100));

        switch (status) {
            case FileDownloadStatus.pending:
                status_tv.setText(R.string.tasks_manager_demo_status_pending);
                break;
            case FileDownloadStatus.started:
                status_tv.setText(R.string.tasks_manager_demo_status_started);
                break;
            case FileDownloadStatus.connected:
                status_tv.setText(R.string.tasks_manager_demo_status_connected);
                break;
            case FileDownloadStatus.progress:
                status_tv.setText(R.string.tasks_manager_demo_status_progress);
                break;
            default:
                status_tv.setText(AppController.getInstance().getString(
                        R.string.tasks_manager_demo_status_downloading, status));
                break;
        }

        taskActionBtn.setText(R.string.pause);
    }

}
