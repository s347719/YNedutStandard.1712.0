//***************************************************************
//*    2015-8-4  下午5:29:51
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.text.SpannableString;
import android.widget.TextView;

import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.L;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;

/**
 * @author 胡毅
 * 
 */
public class FileDownLoader {
	/**
	 * 缓存Image的类，当存储Image的大小大于LruCache设定的值，系统自动释放内存
	 */
	private LruCache<String, Bitmap> mMemoryCache;
	/**
	 * 操作文件相关类对象的引用
	 */
	private FileUtil fileUtils;
	/**
	 * 下载Image的线程池
	 */
	private ExecutorService mImageThreadPool = null;
	
	private final int FILE_DOWNLOAD_ING = 0;
	
	private final int FILE_DOWNLOADED = 1;
	
	private final int FILE_DOWNLOAD_FAIL = 2;
	
	private static FileDownLoader mFileDownLoader;
	
	public static synchronized FileDownLoader getInstance() {
		if (mFileDownLoader == null) {
			mFileDownLoader = new FileDownLoader();
		}
		return mFileDownLoader;
	}

	/**
	 * 获取线程池的方法，因为涉及到并发的问题，我们加上同步锁
	 * 
	 * @return
	 */
	public ExecutorService getThreadPool() {
		if (mImageThreadPool == null) {
			synchronized (ExecutorService.class) {
				if (mImageThreadPool == null) {
					// 为了下载图片更加的流畅，我们用了10个线程来下载图片
					mImageThreadPool = Executors.newFixedThreadPool(10);
				}
			}
		}

		return mImageThreadPool;

	}

	/**
	 * 添加Bitmap到内存缓存
	 * 
	 * @param key
	 * @param bitmap
	 */
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null && bitmap != null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	/**
	 * 从内存缓存中获取一个Bitmap
	 * 
	 * @param key
	 * @return
	 */
	public Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}




	/**
	 * 加载文件,如果文件已经缓存在本地，则直接返回本地文件路径;否则就通过线程池去网络下载文件
	 * @param url 文件url
	 * @param fileSize 文件大小
	 * @param fileName 文件名
	 * @param listener 下载监听器
	 */
	public void loadFile(final String url, final String fileSize,final String fileName, final onFileLoaderListener listener) {
		if (fileName == null) {
			return;
		}
		final String filePath = FileUtil.getFilePath(fileName);
		File downloadedFile = new File(filePath);
		long mFileInfoSize = Long.parseLong(fileSize);
		//如果文件存在且已下载部分大小与消息中的大小一致
		if (downloadedFile.exists() && downloadedFile.length() == mFileInfoSize) {
			listener.onFileLoaded(null, filePath);
		} else {
			//通过handler回调更新UI，或处理业务逻辑
			final Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					switch (msg.what) {
					case FILE_DOWNLOAD_ING:
						int progress = msg.arg1;
						int total = msg.arg2;
						int fileLength = (Integer) msg.obj;
						listener.onFileLoading(progress, total, fileLength);
						break;
					case FILE_DOWNLOADED:
						listener.onFileLoaded(null,filePath);
						break;
					case FILE_DOWNLOAD_FAIL:
						listener.onFileLoadFail();
						break;
					default:
						break;
					}
				}
			};
			getThreadPool().execute(new Runnable() {

				@Override
				public void run() {
					downloadFileFromNet(url,fileSize,filePath,listener,handler);
				}
			});
		}
		return;
	}
	
	/**
	 * 下载文件
	 * @param fileUrl 文件url
	 * @param fileSize 文件大小
	 * @param filePath 文件路径
	 * @param listener 文件下载监听器
	 * @param handler
	 * @return 下载后的文件路径
	 */
	public String downloadFileFromNet(String fileUrl, String fileSize, String filePath, onFileLoaderListener listener, Handler handler) {
		InputStream input = null;
		OutputStream output = null;
		HttpURLConnection connection = null;
		try {
			URL url = new URL(fileUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(8000);
			connection.connect();
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				L.e("Server returned HTTP " + connection.getResponseCode()+ " " + connection.getResponseMessage());
				handler.sendEmptyMessage(FILE_DOWNLOAD_FAIL);
				return null;
			}
			// this will be useful to display download percentage
			// might be -1: server did not report the length
			int fileLength = connection.getContentLength();
			if (fileLength == -1) {
				fileLength = Integer.parseInt(fileSize);
			}
			// download the file
			input = connection.getInputStream();
			output = new FileOutputStream(filePath);
			byte data[] = new byte[Const.DOWN_UP_1MB];
			long total = 0;
			int count;
			int tempProgress = 0;
			while ((count = input.read(data)) != -1) {
				total += count;
				output.write(data, 0, count);
				// publishing the progress....
				if (fileLength > 0) {
					// only if total length is known
					int progress = (int) (total * 100 / fileLength);
					if (listener != null) {
						if (tempProgress == progress) {
							continue;
						}
						tempProgress = progress;
						L.i("progress == "+progress);
						if (progress%10 == 0) {
							Message progressMsg = Message.obtain();
							progressMsg.what = FILE_DOWNLOAD_ING;
							progressMsg.arg1 = progress;
							progressMsg.arg2 = (int) total;
							progressMsg.obj = fileLength;
							handler.sendMessage(progressMsg);
						}
						if (progress == 100) {
							handler.sendEmptyMessage(FILE_DOWNLOADED);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			handler.sendEmptyMessage(FILE_DOWNLOAD_FAIL);
			return null;
		} finally {
			try {
				if (output != null) {
                    output.close();
                }
				if (input != null) {
                    input.close();
                }
			} catch (IOException ignored) {
			}
			if (connection != null) {
                connection.disconnect();
            }
		}
		return filePath;

	}

	/**
	 * 取消正在下载的任务
	 */
	public synchronized void cancelTask() {
		if (mImageThreadPool != null) {
			mImageThreadPool.shutdownNow();
			mImageThreadPool = null;
		}
	}

	/**
	 * 异步下载图片的回调接口
	 * 
	 * @author len
	 * 
	 */
	public interface onImageLoaderListener {
		void onImageLoader(Bitmap bitmap, String url);
	}

	/**
	 * 文件下载监听
	 * @author 胡毅
	 *
	 */
	public interface onFileLoaderListener {
		/**
		 * 正在下载文件
		 * @param progress 当前进度
		 * @param loadedLenghth 已加载长度
		 * @param fileLength 总长度
		 */
		void onFileLoading(int progress, int loadedLenghth,int fileLength);
		/**
		 * 文件已下载
		 * @param bitmap
		 * @param path 文件路径
		 */
		void onFileLoaded(File bitmap, String path);
		/**
		 * 文件下载失败
		 */
		void onFileLoadFail();
	}
	
}
