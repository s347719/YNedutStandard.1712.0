package com.yineng.ynmessager.view.download;

import android.widget.ProgressBar;
import android.widget.TextView;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.yineng.ynmessager.app.update.DownloadVersionActivity;
import com.yineng.ynmessager.util.FileUtil;

import java.lang.ref.WeakReference;

/**
 * @author by 舒欢
 *         Created time: 2017/11/9
 *         Descreption：
 */

public class DownloadVersionViewHolder {

    private ProgressBar pb;
    private TextView detailTv;
    private TextView speedTv;
    private TextView filenameTv;
    private String fileName;

    private WeakReference<DownloadVersionActivity> weakReferenceContext;

    public DownloadVersionViewHolder(WeakReference<DownloadVersionActivity> weakReferenceContext,
                                     final ProgressBar pb, final TextView detailTv, final TextView speedTv,
                                     final String name) {
        this.weakReferenceContext = weakReferenceContext;
        this.pb = pb;
        this.detailTv = detailTv;
        this.fileName = name;
        this.speedTv = speedTv;
    }


    public void setFilenameTv(TextView filenameTv) {
        this.filenameTv = filenameTv;
    }

    private void updateSpeed(int speed) {
        if (speedTv!=null) {
            speedTv.setText(speed+""+"kb/s");
        }
    }

    public void updateProgress(final int sofar, final int total, final int speed) {
        if (total == -1) {
            // chunked transfer encoding data
            pb.setIndeterminate(true);
        } else {
            pb.setMax(total);
            pb.setProgress(sofar);
        }

        updateSpeed(speed);

        if (detailTv != null) {
            detailTv.setText(FileUtil.FormatFileSize(sofar)+"/"+FileUtil.FormatFileSize(total));
        }
    }

    public void updatePending(BaseDownloadTask task) {
        if (filenameTv != null) {
            filenameTv.setText(fileName);
        }
    }

    public void updatePaused(final int speed) {
        updateSpeed(speed);
        pb.setIndeterminate(false);
    }

    public void updateConnected(String etag, String filename) {
        if (filenameTv != null) {
            filenameTv.setText(filename);
        }
    }

    public void updateWarn() {
        pb.setIndeterminate(false);
    }

    public void updateError(final Throwable ex, final int speed) {
        updateSpeed(speed);
        pb.setIndeterminate(false);
        ex.printStackTrace();
    }

    public void updateCompleted(final BaseDownloadTask task) {

        if (detailTv != null) {
            detailTv.setText(task.getSmallFileSoFarBytes()+"/"+task.getSmallFileTotalBytes());
        }

        updateSpeed(task.getSpeed());
        pb.setIndeterminate(false);
        pb.setMax(task.getSmallFileTotalBytes());
        pb.setProgress(task.getSmallFileSoFarBytes());
    }

}
