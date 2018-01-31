package com.yineng.ynmessager.app.update;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.util.AppUtils;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.NetWorkUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.view.download.DownloadVersionViewHolder;

import java.io.File;
import java.lang.ref.WeakReference;


public class DownloadVersionActivity extends BaseActivity {
	private Context mContext;
//	private int mCurrentStatus;// 0；没有下载任务，1：正在下载，2，暂停，3，下载完成
	// UI references.
	private ImageView mPauseBT;
	private ImageView mAbortBT;
	private TextView mDownTitleTV;
	private TextView mDownPercsentTV;
	private TextView mVersionInfoTV;
	private ProgressBar mDownProgressPB;
	private int downloadId2;
//	private DownloadDao mDownloadDao;
	private ProductInfo mProductInfo;
	/**
	 * apk下载的位置
	 */
	private String mDownloadFilePath;
	private ImageView icon_logo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_downloadversion);
		mContext = this;
		init();
		// 注册按钮监听器
		initListener();
		//开启下载线程
		startDownloadThread();
	}

	private void init() {
		Intent intent = this.getIntent();
		icon_logo = (ImageView) findViewById(R.id.icon_logo);
		int drawableId = mContext.getResources().getIdentifier(AppController.getInstance().icon_name, "mipmap", mContext.getPackageName());
		icon_logo.setImageResource(drawableId);
		mProductInfo = (ProductInfo) intent.getSerializableExtra(Const.UPDATE_INFO);
		mPauseBT = (ImageView) this.findViewById(R.id.iv_downloadversion_pause);
		mAbortBT = (ImageView) this.findViewById(R.id.iv_downloadversion_abort);
		mDownTitleTV = (TextView) this
				.findViewById(R.id.tv_downloadversion_versiontitle);
		mDownPercsentTV = (TextView) this
				.findViewById(R.id.tv_downloadversion_percsent);
		mVersionInfoTV = (TextView) this
				.findViewById(R.id.tv_downloadversion_sumary);
		mDownProgressPB = (ProgressBar) this
				.findViewById(R.id.pb_downloadversion_progress);
		if (mProductInfo != null) {
			mVersionInfoTV.setText(mProductInfo.getChangeNote());
			mDownTitleTV.setText("新版本：" + UpdateCheckUtil.formatNewVersionToShow(mProductInfo.getNewesProVersionCode()));
		}
		mPauseBT.setImageResource(R.mipmap.update_pause);
	}

	private void initListener() {
		mPauseBT.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mCurrentStatus == PAUSE) {
					if (!NetWorkUtil.isNetworkAvailable(mContext)) {
						ToastUtil.toastAlerMessage(mContext,
								mContext.getString(R.string.network_is_error),
								2000);
						return;
					}
					mPauseBT.setImageResource(R.mipmap.update_pause);
					mCurrentStatus = START;
					startDownloadThread();
				} else {
					mPauseBT.setImageResource(R.mipmap.update_start);
					mCurrentStatus = PAUSE;
					FileDownloader.getImpl().pause(downloadId2);
				}
			}
		});

		mAbortBT.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mPauseBT.setImageResource(R.mipmap.update_start);
				mCurrentStatus = PAUSE;
				showCancelUpdateDialog();
			}
		});

		mDownProgressPB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDownProgressPB.getProgress() == mProductInfo.getFileSize()) {
					doDownloadedSuccess(mDownloadFilePath);
				}
			}
		});
	}

	/**
	 * 开始下载
	 */
	private void startDownloadThread() {
		final DownloadVersionViewHolder tag;
		final String url;
		final String path;
		String path1;

		final FileDataInfo updateFileDataInfo = mProductInfo != null ? mProductInfo.getFileDataMetaTempVO() : null;
		if (updateFileDataInfo == null) {
			return;
		}
		url = Const.UPDATE_DOWNLOAD_APP_URL+"?fastDFSId="+updateFileDataInfo.getFileURLMappingId();
		L.e("强制更新下载地址： "+url);

		path1 = "Messenger"+AppUtils.getVersionName(DownloadVersionActivity.this)+".apk";
		FileDataInfo updateFileDataInfo2 = mProductInfo.getFileDataMetaTempVO();
		String basePath = updateFileDataInfo2.getBasePath();
		if(basePath != null && basePath.contains("/"))
		{
			int index = basePath.lastIndexOf("/");
			path1 = basePath.substring(index + 1);
		}
		path = path1;
		mDownloadFilePath = FileUtil.getUserSDPath(true,null) +File.separator+path;
		tag = new DownloadVersionViewHolder(new WeakReference<>(this), mDownProgressPB, mDownPercsentTV,null , path);
		downloadId2 = FileDownloader.getImpl()
				.create(url)
				.setPath(mDownloadFilePath)
				.setCallbackProgressTimes(300)
				.setMinIntervalUpdateSpeed(400)
				.setTag(tag)
				.setListener(new FileDownloadSampleListener() {

					@Override
					protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
						super.pending(task, soFarBytes, totalBytes);
						((DownloadVersionViewHolder) task.getTag()).updatePending(task);
					}

					@Override
					protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
						super.progress(task, soFarBytes, totalBytes);
						((DownloadVersionViewHolder) task.getTag()).updateProgress(soFarBytes, totalBytes,
								task.getSpeed());
					}

					@Override
					protected void error(BaseDownloadTask task, Throwable e) {
						super.error(task, e);
						((DownloadVersionViewHolder) task.getTag()).updateError(e, task.getSpeed());
						ToastUtil.toastAlerMessageBottom(DownloadVersionActivity.this, "连接异常，下载失败.",
								Toast.LENGTH_SHORT);
					}

					@Override
					protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
						super.connected(task, etag, isContinue, soFarBytes, totalBytes);
						((DownloadVersionViewHolder) task.getTag()).updateConnected(etag, path);
					}

					@Override
					protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
						super.paused(task, soFarBytes, totalBytes);
						((DownloadVersionViewHolder) task.getTag()).updatePaused(task.getSpeed());
					}

					@Override
					protected void completed(BaseDownloadTask task) {
						super.completed(task);
						((DownloadVersionViewHolder) task.getTag()).updateCompleted(task);
						L.i("安装apk");
						if(mDownloadFilePath.contains(".apk"))
						{
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setDataAndType(Uri.fromFile(new File(mDownloadFilePath)),"application/vnd.android.package-archive");
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
						}
					}

					@Override
					protected void warn(BaseDownloadTask task) {
						super.warn(task);
						((DownloadVersionViewHolder) task.getTag()).updateWarn();
					}
				}).start();
	}
	
	private void showCancelUpdateDialog() {
		if (mDownProgressPB.getProgress() == mProductInfo.getFileSize()) {
			logout(true);
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this).setMessage("您确定要取消更新么？").setCancelable(
					false);

			builder.setPositiveButton(getString(R.string.splash_bttext_continue_update),
					new DialogInterface.OnClickListener() {

						@Override
                        public void onClick(DialogInterface dailog, int which) {
							if (!NetWorkUtil.isNetworkAvailable(mContext)) {
								ToastUtil.toastAlerMessage(mContext,
										mContext.getString(R.string.network_is_error),
										2000);
								return;
							}
							mPauseBT.setImageResource(R.mipmap.update_pause);
//							FileManager.setmCurrentStatus(FileManager.RESTARTING);
							mCurrentStatus = RESTARTING;
						}

					});

			builder.setNegativeButton(getString(R.string.splash_bttext_cancel_update),
					new DialogInterface.OnClickListener() {

						@Override
                        public void onClick(DialogInterface dailog, int which) {
//							FileManager.setmCurrentStatus(FileManager.CANCEL);
							mCurrentStatus = CANCEL;
							logout(true);
						}

					});

			builder.create().show();
		}
	}
	
	@Override
	public void onContextMenuClosed(Menu menu) {
		super.onContextMenuClosed(menu);
	}
	
	/**
	 * 当点击标题栏的返回按扭时执行
	 * 
	 * @Title: back
	 * @Description: 方法描述
	 * @param view
	 */
	public void back(View view) {
		showCancelUpdateDialog();
	}
	
	@Override
	public void onBackPressed() {
		showCancelUpdateDialog();
	}
	
	@Override
    public void onResume() {
		super.onResume();
	}

	@Override
    public void onPause() {
		super.onPause();
	}
	


}
