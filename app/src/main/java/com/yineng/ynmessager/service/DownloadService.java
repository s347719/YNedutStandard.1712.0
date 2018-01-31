package com.yineng.ynmessager.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.alibaba.fastjson.JSON;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.app.DownLoadFile;
import com.yineng.ynmessager.bean.app.Internship.AttachFile;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.bean.p2psession.MessageFileEntity;
import com.yineng.ynmessager.bean.p2psession.MessageVoiceEntity;
import com.yineng.ynmessager.db.DownLoadFileTb;
import com.yineng.ynmessager.db.P2PChatMsgDao;
import com.yineng.ynmessager.db.dao.DisGroupChatDao;
import com.yineng.ynmessager.db.dao.GroupChatDao;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.TextUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import org.apache.http.Header;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Request;

public class DownloadService extends Service
{
	private static final int PROGRESS_PULISH_INTERVAL = 1000;
	/**
	 * 当前正在下载的会话消息中的文件
	 */
	public static Set<BaseChatMsgEntity> mDownloadMsgBeans = null;
	private static P2PChatMsgDao mP2pChatMsgDao;
	private static DisGroupChatDao mDisGroupChatDao;
	private static GroupChatDao mGroupChatDao;
	public String tag = "DownloadService";
	public Context mContext;
	public AsyncHttpClient mAsyncHttpClient = new AsyncHttpClient();
	//下载文件工具类
	private DownLoadFileTb downLoadFileTb;
	/**
	 * 更改数据库中该条记录的状态
	 */
	public static void updateDatabaseMsgStatus(BaseChatMsgEntity failedMsg, int downloadStatus) {
		failedMsg.setIsSuccess(downloadStatus);
		MessageBodyEntity body = JSON.parseObject(failedMsg.getMessage(), MessageBodyEntity.class);
		if (body == null) {
			return;
		}
		switch (body.getMsgType()) {
			case Const.CHAT_VOICE_TYPE_P2P:
			case Const.CHAT_TYPE_P2P:
				if (mP2pChatMsgDao == null) {
					return;
				}
				mP2pChatMsgDao.updateMsgSendStatus(failedMsg.getPacketId(), downloadStatus);
				break;
			case Const.CHAT_VOICE_TYPE_GROUP:
			case Const.CHAT_TYPE_GROUP:
				if (mGroupChatDao == null) {
					return;
				}
				mGroupChatDao.updateMsgSendStatus(failedMsg.getPacketId(), downloadStatus);
				break;
			case Const.CHAT_VOICE_TYPE_DISGROUP:
			case Const.CHAT_TYPE_DIS:
				if (mDisGroupChatDao == null) {
					return;
				}
				mDisGroupChatDao.updateMsgSendStatus(failedMsg.getPacketId(), downloadStatus);
				break;
			default:
				break;
		}
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		L.e(tag, "onCreate");
		mContext = DownloadService.this;
		mP2pChatMsgDao = new P2PChatMsgDao(mContext);
		mGroupChatDao = new GroupChatDao(mContext);
		mDisGroupChatDao = new DisGroupChatDao(mContext);
		downLoadFileTb = new DownLoadFileTb(mContext);
		mDownloadMsgBeans = new HashSet<BaseChatMsgEntity>();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (intent == null) {
			return START_STICKY;
		}
		int fileType = intent.getIntExtra("downloadFileType",-1);
		switch (fileType) {
			case Const.MESSENGERFILE:
				downloadMessengerFile(intent);
				break;
			case Const.APPDGSXFILE:
				downloadDgsxFile(intent);
				break;
			default:
				break;
		}
		return START_STICKY;
	}

	/**
	 * 下载messenger的文件
	 * @param intent
	 */
	private void downloadMessengerFile(Intent intent) {
		BaseChatMsgEntity downloadMsgBean = (BaseChatMsgEntity) intent.getSerializableExtra("downloadFileBean");
		if (downloadMsgBean != null) {
			MessageBodyEntity body = JSON.parseObject(downloadMsgBean.getMessage(), MessageBodyEntity.class);
			if (body != null) {
				if (body.getFiles() != null && body.getFiles().size() > 0) {
					MessageFileEntity downLoadFileinfo = body.getFiles().get(0);
					String fileId = downLoadFileinfo.getFileId();
					String fileName = downLoadFileinfo.getName();
					String token = FileUtil.getSendFileToken();
					String mFileUrl = Const.PROTOCOL+ AppController.getInstance().CONFIG_INSIDE_FILE_TRANSLATE_IP+Const.DOWNLOAD_FILE_URL+"?token="+token+"&fileId="+fileId ;
					FileDownloadResponseHandler downloadResponHandler = new FileDownloadResponseHandler(mContext);
					downloadResponHandler.setDownloadFileBean(downloadMsgBean, fileName,BaseChatMsgEntity.FILE);
					mAsyncHttpClient.get(mFileUrl, null, downloadResponHandler);
				} else if (body.getVoice() != null) {
					MessageVoiceEntity voiceFileinfo = body.getVoice();
					String fileId = voiceFileinfo.getId();
					String fileName = voiceFileinfo.getId();
					String token = FileUtil.getSendFileToken();
					String mFileUrl = Const.PROTOCOL+ AppController.getInstance().CONFIG_INSIDE_FILE_TRANSLATE_IP+Const.DOWNLOAD_FILE_URL+"?token="+token+"&fileId="+fileId ;
					FileDownloadResponseHandler downloadResponHandler = new FileDownloadResponseHandler(mContext);
					downloadResponHandler.setDownloadFileBean(downloadMsgBean, fileName,BaseChatMsgEntity.AUDIO_FILE);
					mAsyncHttpClient.get(mFileUrl, null, downloadResponHandler);
				}

			}
		}
	}
	/**
	 * 下载顶岗实习的文件
	 * @param intent
	 */
	private void downloadDgsxFile(Intent intent) {
		AttachFile downloadBean = intent.getParcelableExtra("downloadFileBean");
		String mFileUrl = "http://eww.yineng.com.cn/d/file/support/download/2015-11-06/07686945444ad8b836e945009771f979.exe";
		FileDownloadResponseHandler downloadResponHandler = new FileDownloadResponseHandler(mContext);
		downloadResponHandler.setDownloadFileBean(downloadBean,downloadBean.getmFileName());
		mAsyncHttpClient.get(mFileUrl, null, downloadResponHandler);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		L.e(tag, "onDestroy");
		//当服务被系统杀掉的时候，执行ondestory，这个时候再置正在下载的文件为失败状态
		if (mDownloadMsgBeans != null && mDownloadMsgBeans.size() > 0) {
			List<BaseChatMsgEntity> msgBeans = new ArrayList<BaseChatMsgEntity>(DownloadService.mDownloadMsgBeans);
			mDownloadMsgBeans.clear();
			for (BaseChatMsgEntity failedMsg :msgBeans) {
				updateDatabaseMsgStatus(failedMsg, BaseChatMsgEntity.DOWNLOAD_FAILED);
			}
		}
		mP2pChatMsgDao = null;
		mGroupChatDao = null;
		mDisGroupChatDao = null;
		mDownloadMsgBeans = null;
	}


	public class FileDownloadResponseHandler extends FileAsyncHttpResponseHandler {
		public String fileName;
		public BaseChatMsgEntity mDownloadMsgBean;

		//顶岗实习附件下载
		public AttachFile mAttachFile;

		private long mLastPulishProgress = 0;

		private Object mDownloadObject;


		private int type;//下载的文件是文件还是语音

		public FileDownloadResponseHandler(Context context) {
			super(context);
		}

		@Override
		public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
			L.e(tag, "execDownload onFailure -> file : " + fileName);
			if (mDownloadObject instanceof BaseChatMsgEntity) {
				updateDatabaseMsgStatus(mDownloadMsgBean,BaseChatMsgEntity.DOWNLOAD_FAILED);
				if (mDownloadMsgBeans != null && mDownloadMsgBeans.size()>0) {
					mDownloadMsgBeans.remove(mDownloadMsgBean);
				}
				//发送广播更新UI
				mDownloadMsgBean.setIsSuccess(BaseChatMsgEntity.DOWNLOAD_FAILED);
				EventBus.getDefault().post(mDownloadMsgBean);
			} else if (mDownloadObject instanceof AttachFile) {
				//发送广播更新UI
				mAttachFile.setmStatus(BaseChatMsgEntity.DOWNLOAD_FAILED);
				EventBus.getDefault().post(mAttachFile);
			}
		}

		public void setDownloadFileBean(BaseChatMsgEntity downloadMsgBean,String destFileName,int type) {
			mDownloadObject = downloadMsgBean;
			this.type = type;
			this.fileName = destFileName;
		}
		//  下载的附件对象。此版本已经没有被用到 所以在  AttachFile  类型的时候不需要保存下载信息
		public void setDownloadFileBean(AttachFile downloadMsgBean, String fileName) {
			mDownloadObject = downloadMsgBean;
			this.fileName = fileName;
		}

		@Override
		public void onStart() {
			if (mDownloadObject instanceof BaseChatMsgEntity) {
				mDownloadMsgBean = (BaseChatMsgEntity) mDownloadObject;
				updateDatabaseMsgStatus(mDownloadMsgBean,BaseChatMsgEntity.DOWNLOAD_ING);
				if (mDownloadMsgBeans != null) {
					mDownloadMsgBeans.add(mDownloadMsgBean);
				}
				//发送广播更新UI
				EventBus.getDefault().post(mDownloadMsgBean);
			} else if (mDownloadObject instanceof AttachFile) {
				mAttachFile = (AttachFile) mDownloadObject;
				mAttachFile.setmStatus(BaseChatMsgEntity.DOWNLOAD_ING);
			}
		}



		@Override
		public void onProgress(long bytesWritten, long totalSize) {
			long now = System.currentTimeMillis();
			if(now - mLastPulishProgress > PROGRESS_PULISH_INTERVAL)
			{
				int progress = (int) ((bytesWritten * 1.0 / totalSize) * 100);
				if (mDownloadObject instanceof BaseChatMsgEntity) {
					mDownloadMsgBean.setReceiveProgress(progress);
					mDownloadMsgBean.setReceivedBytes(bytesWritten);
					if (mDownloadMsgBean.getMessageType() == BaseChatMsgEntity.AUDIO_FILE) {
						return;
					}
					//发送广播更新UI
					mDownloadMsgBean.setIsSuccess(BaseChatMsgEntity.DOWNLOAD_ING);
					EventBus.getDefault().post(mDownloadMsgBean);
				} else if (mDownloadObject instanceof AttachFile) {
					L.e("progress = "+progress);
					//发送广播更新UI
					mAttachFile.setmDownloadProgress(progress);
					EventBus.getDefault().post(mAttachFile);
				}
				mLastPulishProgress = now;
			}
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers, File file) {
			File destFile = null;
			if (mDownloadObject instanceof BaseChatMsgEntity) {
				destFile = new File(FileUtil.getFilePath(fileName));
				updateDatabaseMsgStatus(mDownloadMsgBean,BaseChatMsgEntity.DOWNLOAD_SUCCESS);
				if (mDownloadMsgBeans != null && mDownloadMsgBeans.size()>0) {
					mDownloadMsgBeans.remove(mDownloadMsgBean);
				}
				// 保存下载的文件信息到数据库
				MessageBodyEntity messageBodyEntity = JSON.parseObject(((BaseChatMsgEntity) mDownloadObject).getMessage(), MessageBodyEntity.class);
				if(messageBodyEntity.getFiles()!=null){
					DownLoadFile downLoadFile = new DownLoadFile();
					downLoadFile.setFileName(fileName);
					if (messageBodyEntity.getMsgType()==1){
						downLoadFile.setFileSource(DownLoadFile.FILE_SOURCE_PERSON);
					}else if (messageBodyEntity.getMsgType()==3){
						downLoadFile.setFileSource(DownLoadFile.FILE_SOURCE_DIS);
					}
					downLoadFile.setPacketId(((BaseChatMsgEntity) mDownloadObject).getPacketId());
					downLoadFile.setFileId(((BaseChatMsgEntity) mDownloadObject).getChatUserNo());
					downLoadFile.setSendUserNo(((BaseChatMsgEntity) mDownloadObject).getIsSend()==0 ? LastLoginUserSP.getLoginUserNo(mContext):((BaseChatMsgEntity) mDownloadObject).getChatUserNo());
					downLoadFile.setIsSend(DownLoadFile.IS_RECEIVE);
					downLoadFile.setFileType(TextUtil.matchTheFileType(fileName));
					downLoadFile.setDataTime(String.valueOf(System.currentTimeMillis()));
					downLoadFile.setSize(messageBodyEntity.getFiles().size()>0 ? messageBodyEntity.getFiles().get(0).getSize(): "0" );
					downLoadFileTb.saveOrUpdate(downLoadFile);
					//发送广播更新UI 将状态置为下载完成
					mDownloadMsgBean.setIsSuccess(BaseChatMsgEntity.DOWNLOAD_SUCCESS);
				}
				EventBus.getDefault().post(mDownloadMsgBean);
			} else if (mDownloadObject instanceof AttachFile) {
				destFile = new File(FileUtil.getFilePath(fileName));
				mAttachFile.setmStatus(BaseChatMsgEntity.DOWNLOAD_SUCCESS);
				//发送广播更新UI
				mAttachFile.setmDownloadProgress(100);
				//发送广播更新UI
				EventBus.getDefault().post(mAttachFile);
			}
			if (destFile == null) {
				return;
			}
			FileUtil.copyFile(file, destFile);
			file.delete();

		}

	}


}
