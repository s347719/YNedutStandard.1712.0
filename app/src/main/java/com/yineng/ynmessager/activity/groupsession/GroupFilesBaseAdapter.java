//***************************************************************
//*    2015-9-9  下午5:29:08
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.groupsession;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.groupsession.GroupSharedFilesActivity.ListContentHead;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.app.DownLoadFile;
import com.yineng.ynmessager.bean.groupsession.SharedFile;
import com.yineng.ynmessager.db.DownLoadFileTb;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.SystemUtil;
import com.yineng.ynmessager.util.TextUtil;
import com.yineng.ynmessager.view.download.GroupsharedViewHolder;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.File;

/**
 * @author 贺毅柳
 */
public class GroupFilesBaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public static final String TAG = "GroupFilesBaseAdapter";
    private Context mContext;
    private LayoutInflater mInflater;
    private OnItemLongClickListener mOnItemLongClickListener;
    private static final int VIEW_TYPE_HEAD = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private DownLoadFileTb downLoadFileTb;
    private AppController appController =  AppController.getInstance();
    private String groupId;

    GroupFilesBaseAdapter(Context context, String groupId)
    {
        mContext = context;
        this.groupId = groupId;
        mInflater = LayoutInflater.from(context);
        downLoadFileTb = new DownLoadFileTb(mContext);
    }

    @Override
    public int getItemCount()
    {
        return TasksManager.getImpl().getTaskCounts();
    }


    @Override
    public int getItemViewType(int position)
    {
        return TasksManager.getImpl().get(position) instanceof SharedFile ? VIEW_TYPE_ITEM : VIEW_TYPE_HEAD;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        switch (holder.getItemViewType())
        {
            case VIEW_TYPE_HEAD:
                onBindHeadViewHolder((HeadViewHolder) holder, position);
                break;
            case VIEW_TYPE_ITEM:
                onBindItemViewHolder((GroupsharedViewHolder) holder, position);
                break;
            default:
                break;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view;
        RecyclerView.ViewHolder holder = null;
        switch (viewType)
        {
            case VIEW_TYPE_HEAD:
                view = mInflater.inflate(R.layout.item_group_shared_files_title, parent, false);
                holder = new HeadViewHolder(view);
                break;
            case VIEW_TYPE_ITEM:
                view = mInflater.inflate(R.layout.item_group_shared_files, parent, false);
                holder = new GroupsharedViewHolder(view);
                final RecyclerView.ViewHolder finalHolder = holder;
                holder.itemView.setOnLongClickListener(new OnLongClickListener()
                {

                    @Override
                    public boolean onLongClick(View v) {
                        return mOnItemLongClickListener != null && mOnItemLongClickListener.onLongClick(v, finalHolder.getLayoutPosition() - 1);
                    }
                });
                ((GroupsharedViewHolder)holder).taskActionBtn.setOnClickListener(taskActionOnClickListener);
                break;
            default:
                break;
        }
        return holder;
    }

    private void onBindHeadViewHolder(HeadViewHolder holder, int position)
    {
        ListContentHead head = (ListContentHead) TasksManager.getImpl().get(position);
        holder.title.setText(head.getTitle());
    }

    private void onBindItemViewHolder(GroupsharedViewHolder holder, int position)
    {


        final SharedFile model = (SharedFile)TasksManager.getImpl().get(position);

        String url =  Const.PROTOCOL + appController.CONFIG_INSIDE_FILE_TRANSLATE_IP + Const.GROUP_FILE_DOWNLOAD_URL +"?fileId="+ model.getId()+"&token="+ FileUtil.getSendFileToken();
        BaseDownloadTask task = FileDownloader.getImpl().create(url)
                .setPath(FileUtil.getFilePath(model.getName()));

        holder.update(task.getId(),position);
        showImageType(holder.thumb,model.getName());
        holder.fileName.setText(model.getName());
        holder.fileSize.setText(FileUtil.FormatFileSize(model.getSize()));
        holder.owner.setText(mContext.getString(R.string.groupSharedFiles_owner, model.getOwnerName()));
        holder.timestamp.setText(DateFormatUtils.format(model.getUploadTime().getTime(), "MM-dd HH:mm"));
        holder.taskActionBtn.setTag(holder);

        TasksManager.getImpl()
                .updateViewHolder(holder.id, holder);

        holder.taskActionBtn.setEnabled(true);


        if (TasksManager.getImpl().isReady()) {
            final int status = TasksManager.getImpl().getStatus(holder.id,FileUtil.getFilePath(model.getName()));
            if (status == FileDownloadStatus.pending || status == FileDownloadStatus.started ||
                    status == FileDownloadStatus.connected) {
                // start task, but file not created yet
                holder.updateDownloading(status, TasksManager.getImpl().getSoFar(Integer.parseInt(model.getId()))
                        , TasksManager.getImpl().getTotal(Integer.parseInt(model.getId())));
            } else if (!new File(FileUtil.getFilePath(model.getName())).exists()) {
                // not exist file
                holder.updateNotDownloaded(status, 0, 0);
            } else if (TasksManager.getImpl().isDownloaded(status)) {
                // already downloaded and exist
                holder.updateDownloaded();
            } else if (status == FileDownloadStatus.progress) {
                // downloading
                holder.updateDownloading(status, TasksManager.getImpl().getSoFar(model.getUpTime())
                        , TasksManager.getImpl().getTotal(model.getUpTime()));
            } else {
                // not start
                holder.updateNotDownloaded(status, TasksManager.getImpl().getSoFar(model.getUpTime())
                        , TasksManager.getImpl().getTotal(model.getUpTime()));
            }
        } else {
            holder.status_tv.setText(R.string.tasks_manager_demo_status_loading);
            holder.taskActionBtn.setEnabled(false);
        }

    }
    /**
     * 设置当前文件所属类型
     * @param fileType  控件
     * @param name 名字
     */
    private void showImageType(ImageView fileType, String name) {

        switch (TextUtil.matchTheFileType(name)){
            case 1:
                fileType.setImageResource(R.mipmap.jpg);
                break;
            case 2:
                fileType.setImageResource(R.mipmap.video);
                break;
            case 3:
                fileType.setImageResource(R.mipmap.music);
                break;
            case 4:
                fileType.setImageResource(R.mipmap.word);
                break;
            case 5:
                fileType.setImageResource(R.mipmap.excel);
                break;
            case 6:
                fileType.setImageResource(R.mipmap.pdf);
                break;
            case 7:
                fileType.setImageResource(R.mipmap.ppt);
                break;
            case 8:
                fileType.setImageResource(R.mipmap.rar);
                break;
            case 9:
                fileType.setImageResource(R.mipmap.file);
                break;
            default:
                fileType.setImageResource(R.mipmap.file);
                break;

        }
    }


    static class HeadViewHolder extends RecyclerView.ViewHolder
    {
        TextView title;

        HeadViewHolder(View view)
        {
            super(view);
            title = (TextView) view.findViewById(R.id.groupSharedFiles_txt_listItem_title);
        }
    }

    private FileDownloadListener taskDownloadListener = new FileDownloadSampleListener() {

        private GroupsharedViewHolder checkCurrentHolder(final BaseDownloadTask task) {
            final GroupsharedViewHolder tag = (GroupsharedViewHolder) task.getTag();
            if (tag.id!=task.getId()) {
                return null;
            }

            return tag;
        }

        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.pending(task, soFarBytes, totalBytes);
            final GroupsharedViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }

            tag.updateDownloading(FileDownloadStatus.pending, soFarBytes
                    , totalBytes);
            tag.status_tv.setText(R.string.tasks_manager_demo_status_pending);
        }

        @Override
        protected void started(BaseDownloadTask task) {
            super.started(task);
            final GroupsharedViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }

            tag.status_tv.setText(R.string.tasks_manager_demo_status_started);
        }

        @Override
        protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
            super.connected(task, etag, isContinue, soFarBytes, totalBytes);
            final GroupsharedViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }

            tag.updateDownloading(FileDownloadStatus.connected, soFarBytes
                    , totalBytes);
            tag.status_tv.setText(R.string.tasks_manager_demo_status_connected);
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.progress(task, soFarBytes, totalBytes);
            final GroupsharedViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }

            tag.updateDownloading(FileDownloadStatus.progress, soFarBytes
                    , totalBytes);
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            super.error(task, e);
            final GroupsharedViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }

            tag.updateNotDownloaded(FileDownloadStatus.error, task.getLargeFileSoFarBytes()
                    , task.getLargeFileTotalBytes());
            TasksManager.getImpl().removeTaskForViewHolder(task.getId());
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.paused(task, soFarBytes, totalBytes);
            final GroupsharedViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }

            tag.updateNotDownloaded(FileDownloadStatus.paused, soFarBytes, totalBytes);
            tag.status_tv.setText(R.string.tasks_manager_demo_status_paused);
            TasksManager.getImpl().removeTaskForViewHolder(task.getId());
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            super.completed(task);
            final GroupsharedViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }
            // 保存下载的文件信息到数据库
            SharedFile sharedFile = (SharedFile)TasksManager.getImpl().get(tag.position);
            String sharedFileName = sharedFile.getName();
            DownLoadFile downLoadFile = new DownLoadFile();
            downLoadFile.setFileName(sharedFileName);
            downLoadFile.setFileSource(DownLoadFile.FILE_SOURCE_SHARED_GROUP);
            downLoadFile.setPacketId(sharedFile.getId());
            downLoadFile.setFileId(groupId);
            downLoadFile.setSendUserNo(sharedFile.getOwner());
            downLoadFile.setIsSend(DownLoadFile.IS_UNKOWN);
            downLoadFile.setFileType(TextUtil.matchTheFileType(sharedFileName));
            downLoadFile.setDataTime(String.valueOf(System.currentTimeMillis()));
            downLoadFile.setSize(String.valueOf(sharedFile.getSize()));
            downLoadFileTb.saveOrUpdate(downLoadFile);

            tag.updateDownloaded();
            TasksManager.getImpl().removeTaskForViewHolder(task.getId());
        }
    };
    private View.OnClickListener taskActionOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() == null) {
                return;
            }

            GroupsharedViewHolder holder = (GroupsharedViewHolder) v.getTag();

            CharSequence action = ((TextView) v).getText();
            if (action.equals(v.getResources().getString(R.string.pause))) {
                // to pause
                FileDownloader.getImpl().pause(holder.id);
            } else if (action.equals(v.getResources().getString(R.string.start))) {
                // to start
                // to start
                final SharedFile model = (SharedFile)TasksManager.getImpl().get(holder.position);
                String url =  Const.PROTOCOL + appController.CONFIG_INSIDE_FILE_TRANSLATE_IP + Const.GROUP_FILE_DOWNLOAD_URL +"?fileId="+ model.getId()+"&token="+ FileUtil.getSendFileToken();
                final BaseDownloadTask task = FileDownloader.getImpl().create(url)
                        .setPath(FileUtil.getFilePath(model.getName()))
                        .setCallbackProgressTimes(100)
                        .setListener(taskDownloadListener);

                TasksManager.getImpl()
                        .addTaskForViewHolder(task);

                TasksManager.getImpl()
                        .updateViewHolder(holder.id, holder);

                task.start();
            } else if (action.equals(v.getResources().getString(R.string.scan))) {
                // to delete
                File file = new File(FileUtil.getFilePath(((SharedFile)TasksManager.getImpl().get(holder.position)).getName()));
                if (!file.exists()){
                    Toast.makeText(mContext, R.string.groupSharedFiles_fileNotExist, Toast.LENGTH_SHORT).show();
                }
                if (!SystemUtil.execLocalFile(mContext, file))
                {
                    Toast.makeText(mContext, R.string.groupSharedFiles_noAppsForThisFile, Toast.LENGTH_LONG).show();
                }
//                holder.taskActionBtn.setEnabled(true);
//                holder.updateNotDownloaded(FileDownloadStatus.INVALID_STATUS, 0, 0);
            }
        }
    };



    interface OnItemLongClickListener
    {
        boolean onLongClick(View v, int position);
    }

    /**
     * @param onItemLongClickListener the mOnItemLongClickListener to set
     */
    void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener)
    {
        this.mOnItemLongClickListener = onItemLongClickListener;
    }

}
