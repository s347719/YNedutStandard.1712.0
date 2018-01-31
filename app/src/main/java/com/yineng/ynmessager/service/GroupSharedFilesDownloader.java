//***************************************************************
//*    2015-9-14  下午2:30:38
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.app.DownLoadFile;
import com.yineng.ynmessager.bean.groupsession.SharedFile;
import com.yineng.ynmessager.db.DownLoadFileTb;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.TextUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;

import java.io.File;

/**
 * @author 贺毅柳 群共享文件下载的Service
 */
public class GroupSharedFilesDownloader extends IntentService
{
    public static final String TAG = "GroupSharedFilesDownloader";
    public static final String INTENT_ACTION = "DownloaderBroadcastReceiver";
    public static final String INTENT_ACTION_CANCEL = "DownloaderBroadcastReceiver.cancel";
    public static final int STATE_START = 1;
    public static final int STATE_PROGRESS = 2;
    public static final int STATE_FINISH = 0;
    public static final int STATE_FINISH_SUCCESS = 3;
    public static final int STATE_FINISH_FAILURE = 4;
    private static final int PROGRESS_PULISH_INTERVAL = 400;
    private static final int DOWNLOAD_MAX_RETRY = 0; // 下载最大重试次数（不需要重试）
    private static final int DOWNLOAD_TIMEOUT = 8000; // 下载超时
    public static SharedFile sHandling = null;
    private final SyncHttpClient mHttpClient = new SyncHttpClient();
    private Context mContext;
    private Handler mHandler;
    private LocalBroadcastManager mBroadcastManager;
    private long mLastPulishProgress = 0;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override public void onReceive(Context context, Intent intent) {
            mHttpClient.cancelAllRequests(true);
        }
    };
    //下载文件工具类
    private DownLoadFileTb downLoadFileTb;

    public GroupSharedFilesDownloader()
    {
        super(TAG);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        mContext = getBaseContext();
        if (downLoadFileTb==null){
            downLoadFileTb = new DownLoadFileTb(mContext);
        }
        mHandler = new Handler(getMainLooper());
        mHttpClient.setMaxRetriesAndTimeout(DOWNLOAD_MAX_RETRY, DOWNLOAD_TIMEOUT);
        mBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        mBroadcastManager.registerReceiver(mBroadcastReceiver, new IntentFilter(INTENT_ACTION_CANCEL));
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        L.d(TAG, "Service has done and going to be destoried");

        sHandling = null;
        mBroadcastManager.unregisterReceiver(mBroadcastReceiver);
        mHttpClient.cancelAllRequests(true);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        String ip = AppController.getInstance().CONFIG_INSIDE_FILE_TRANSLATE_IP;

        final String url = Const.PROTOCOL + ip + Const.GROUP_FILE_DOWNLOAD_URL;
        L.i(TAG, "preparing to download file from " + url);

        final SharedFile sharedFile = intent.getParcelableExtra("SharedFile");
        final long size = sharedFile.getSize();
        final String groupId = intent.getStringExtra("GroupId");

        sHandling = sharedFile;

        final Intent broadcastIntent = new Intent(INTENT_ACTION);
        broadcastIntent.putExtra("SharedFile", sharedFile);
        broadcastIntent.putExtra("GroupId", groupId);

        // 下载文件
        RequestParams params = new RequestParams();
        params.put("fileId", sharedFile.getId());
        params.put("token", FileUtil.getSendFileToken());
        mHttpClient.get(url, params, new FileAsyncHttpResponseHandler(mContext)
        {

            @Override
            public void onStart()
            {
                super.onStart();

                broadcastIntent.putExtra("State", STATE_START);
                mBroadcastManager.sendBroadcast(broadcastIntent);

                mHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(mContext, getString(R.string.groupSharedFiles_downloadStart, sharedFile.getName()), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file)
            {
                String sharedFileName = sharedFile.getName();
                L.w(TAG, "execDownload onFailure -> file : " + sharedFileName, throwable);
                file.delete();

                broadcastIntent.putExtra("State", STATE_FINISH_FAILURE);
                mBroadcastManager.sendBroadcast(broadcastIntent);

                final String strNotice;
                String errMsg = (throwable == null ? null : throwable.getMessage());
                if (StringUtils.contains(errMsg, "ENOSPC")) // 存储空间不足，下载中断
                {
                    strNotice = getString(R.string.groupSharedFiles_downloadFailed_ENOSPC);
                } else if (StringUtils.contains(errMsg, "ETIMEDOUT") ||
                        StringUtils.contains(errMsg, "ENETUNREACH")) // 网络异常，请检查网络设置
                {
                    strNotice = getString(R.string.groupSharedFiles_downloadFailed_ETIMEDOUT);
                } else // 默认提示 “XXX文件 下载中断”
                {
                    strNotice = getString(R.string.groupSharedFiles_downloadFailed, sharedFile.getName());
                }

                mHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(mContext, strNotice, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file)
            {
                String sharedFileName = sharedFile.getName();
                L.d(TAG, "execDownload onSuccess -> file name: " + sharedFileName);
                File destFile = new File(FileUtil.getFilePath(sharedFileName));
                FileUtil.copyFile(file, destFile);
                file.delete();
                // 保存下载的文件信息到数据库
                DownLoadFile downLoadFile = new DownLoadFile();
                downLoadFile.setFileName(sharedFileName);
                downLoadFile.setFileSource(DownLoadFile.FILE_SOURCE_SHARED_GROUP);
                downLoadFile.setPacketId(sharedFile.getId());
                downLoadFile.setFileId(groupId);
                downLoadFile.setSendUserNo(sharedFile.getOwner());
                downLoadFile.setIsSend(DownLoadFile.IS_UNKOWN);
                downLoadFile.setFileType(TextUtil.matchTheFileType(sharedFileName));
                downLoadFile.setDataTime(String.valueOf(System.currentTimeMillis()));
                downLoadFile.setSize(String.valueOf(size));
                downLoadFileTb.saveOrUpdate(downLoadFile);

                broadcastIntent.putExtra("State", STATE_FINISH_SUCCESS);
                mBroadcastManager.sendBroadcast(broadcastIntent);

                mHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(mContext, getString(R.string.groupSharedFiles_downloadSuccess, sharedFile.getName()), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize)
            {
                long now = System.currentTimeMillis();
                if (now - mLastPulishProgress > PROGRESS_PULISH_INTERVAL)
                {
                    broadcastIntent.putExtra("State", STATE_PROGRESS);

                    int progressPercent = (int) ((double) bytesWritten / (double) size * 100);
                    if (progressPercent < 1) {
                        progressPercent = 1;
                    }

                    sharedFile.setDownloadProgress(progressPercent);
                    mBroadcastManager.sendBroadcast(broadcastIntent);

                    mLastPulishProgress = now;
                }
            }

            @Override
            public void onFinish()
            {
                super.onFinish();
                sHandling = null;
                mLastPulishProgress = 0;

                broadcastIntent.putExtra("State", STATE_FINISH);
                mBroadcastManager.sendBroadcast(broadcastIntent);
            }

            @Override
            public void onCancel()
            {
                super.onCancel();

                L.d(TAG, "cancel downloading");
            }
        });
    }
}
