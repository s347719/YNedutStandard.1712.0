package com.yineng.ynmessager.util;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.yineng.ynmessager.activity.picker.SendingListener;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.imageloader.FileDownLoader;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.apache.http.Header;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FileUtil {

	public static final String TAG = "FileUtil";
	public static final String mAppUpdateFilePath= "appupdate";
	public static final String mImgPath = "img";
	public static final String mFilePath = "file";
	public static final String mAvatarPath = "useravatar";
	public static final String mCachePath = "cache";
	public static final int SIZETYPE_B = 1;// 获取文件大小单位为B的double值
	public static final int SIZETYPE_KB = 2;// 获取文件大小单位为KB的double值
	public static final int SIZETYPE_MB = 3;// 获取文件大小单位为MB的double值
	public static final int SIZETYPE_GB = 4;// 获取文件大小单位为GB的double值
	public static final HashMap<String, String> mFileTypes = new HashMap<String, String>();

	static {
		// images
		mFileTypes.put("FFD8FF", "jpg");
		mFileTypes.put("89504E47", "png");
		mFileTypes.put("47494638", "gif");
		mFileTypes.put("49492A00", "tif");
		mFileTypes.put("424D", "bmp");
		//
		mFileTypes.put("41433130", "dwg"); // CAD
		mFileTypes.put("38425053", "psd");
		mFileTypes.put("7B5C727466", "rtf"); // 日记本
		mFileTypes.put("3C3F786D6C", "xml");
		mFileTypes.put("68746D6C3E", "html");
		mFileTypes.put("44656C69766572792D646174653A", "eml"); // 邮件
		mFileTypes.put("D0CF11E0", "doc");
		mFileTypes.put("CFAD12FEC5FD746F", "dbx");
		mFileTypes.put("5374616E64617264204A", "mdb");
		mFileTypes.put("252150532D41646F6265", "ps");
		mFileTypes.put("255044462D312E", "pdf");
		mFileTypes.put("504B0304", "zip");
		mFileTypes.put("52617221", "rar");
		mFileTypes.put("57415645", "wav");
		mFileTypes.put("41564920", "avi");
		mFileTypes.put("2E524D46", "rm");
		mFileTypes.put("000001BA", "mpg");
		mFileTypes.put("000001B3", "mpg");
		mFileTypes.put("6D6F6F76", "mov");
		mFileTypes.put("3026B2758E66CF11", "asf");
		mFileTypes.put("4D546864", "mid");
		mFileTypes.put("1F8B08", "gz");
		mFileTypes.put("", "");
		mFileTypes.put("", "");
	}

	/**
	 * 保存Bitmap到指定目录
	 * 来自于会话中的图片
	 *
	 * @param fileName 文件名
	 * @param bitmap
	 * @throws IOException
	 */
	public static void saveBitmap(String fileName, Bitmap bitmap, String path) {
		File downloadFolder = new File(path);//生成文件路径
		if (bitmap == null) {
			return;
		}
		if (TextUtils.isEmpty(fileName)){
			return;
		}
		File file =  new File(mkdirFile(downloadFolder),fileName+".jpg");//生成文件路径
		if (!isFileExists(path,fileName)){
			/**
			 * 如果不存在此文件则保存在downloadFolder路径下的file名文件中
			 */
			try {
				file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
				fos.flush();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static File mkdirFile(File downloadFolder) {
		try {
			if (!downloadFolder.exists()) {
				downloadFolder.mkdirs();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return downloadFolder;
	}

	/**
	 * 判断某目录下文件是否存在
	 *
	 * @param fileName 文件名
	 * @return
	 */
	public static boolean isFileExists(String downimagefolder,String fileName) {
		return new File( new File(downimagefolder), fileName).exists();
	}


	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	public static String getPath(final Context context, final Uri uri) {

		// check here to KITKAT or new version
		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/"
							+ split[1];
				}
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(context, contentUri, selection,
						selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {

			// Return the remote address
			if (isGooglePhotosUri(uri)) {
				return uri.getLastPathSegment();
			}

			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri
				.getAuthority());
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @param selection
	 *            (Optional) Filter used in the query.
	 * @param selectionArgs
	 *            (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri,
									   String selection, String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection,
					selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}


	/**
	 * 创建目录
	 *
	 * @param dir
	 *            目录
	 */
	public static void mkdir(String dir) {
		try {
			String dirTemp = dir;
			File dirPath = new File(dirTemp);
			if (!dirPath.exists()) {
				dirPath.mkdirs();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 新建文件
	 *
	 * @param fileName
	 *            String 包含路径的文件名 如:E:\phsftp\src\123.txt
	 * @param content
	 *            String 文件内容
	 *
	 */
	public static void createNewFile(String fileName, InputStream content) {
		try {
			String fileNameTemp = fileName;
			File filePath = new File(fileNameTemp);
			if (!filePath.exists()) {
				filePath.createNewFile();
			}
			FileWriter fw = new FileWriter(filePath);
			PrintWriter pw = new PrintWriter(fw);
			InputStream strContent = content;
			pw.println(strContent);
			pw.flush();
			pw.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 删除文件
	 *
	 * @param filePath
	 *            包含路径的文件名
	 */
	public static void delFile(String filePath) {
		try {
			java.io.File delFile = new java.io.File(filePath);
			delFile.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除文件夹
	 *
	 * @param folderPath
	 *            文件夹路径
	 */
	public static void delFolder(String folderPath) {
		try {
			// 删除文件夹里面所有内容
			delAllFile(folderPath);
			String filePath = folderPath;
			java.io.File myFilePath = new java.io.File(filePath);
			// 删除空文件夹
			myFilePath.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除文件夹里面的所有文件
	 *
	 * @param path
	 *            文件夹路径
	 */
	public static void delAllFile(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}
		String[] childFiles = file.list();
		if (childFiles == null) {
			return;
		}
		File temp = null;
		for (int i = 0; i < childFiles.length; i++) {
			// File.separator与系统有关的默认名称分隔符
			// 在UNIX系统上，此字段的值为'/'；在Microsoft Windows系统上，它为 '\'。
			if (path.endsWith(File.separator)) {
				temp = new File(path + childFiles[i]);
			} else {
				temp = new File(path + File.separator + childFiles[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + childFiles[i]);// 先删除文件夹里面的文件
				delFolder(path + "/" + childFiles[i]);// 再删除空文件夹
			}
		}
	}

	/**
	 * 复制单个文件
	 *
	 * @param srcFile
	 *            源文件路径（包含文件名）
	 * @param dstFile
	 *            目标路径（包含文件名）
	 * @return true：复制成功，否则表示出现异常
	 */
	public static boolean copyFile(String srcFile, String dstFile)
	{
		if(TextUtils.isEmpty(srcFile) || TextUtils.isEmpty(dstFile)) {
			return false;
		}
		return copyFile(new File(srcFile),new File(dstFile));
	}

	/**
	 * 复制单个文件
	 *
	 * @param srcFile
	 *            源文件路径（包含文件名）
	 * @param dstFile
	 *            目标路径（包含文件名）
	 * @return true：复制成功，否则表示出现异常
	 */
	public static boolean copyFile(File srcFile, File dstFile)
	{
		boolean result = false;
		if(srcFile == null || !srcFile.exists() || dstFile == null) {
			return result;
		}

		FileInputStream inStream = null;
		FileOutputStream outStream = null;
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		try
		{
			inStream = new FileInputStream(srcFile);
			outStream = new FileOutputStream(dstFile);
			inChannel = inStream.getChannel();
			outChannel = outStream.getChannel();
			long transferred = outChannel.transferFrom(inChannel,0,inChannel.size());
			if(transferred > 0) {
				result = true;
			}
		}catch(FileNotFoundException e)
		{
			L.e(TAG,e.getMessage(),e);
			result = false;
		}catch(IOException e)
		{
			L.e(TAG,e.getMessage(),e);
			result = false;
		}finally
		{
			try
			{
				if(inChannel != null) {
					inChannel.close();
				}
				if(inStream != null) {
					inStream.close();
				}
			}catch(IOException e)
			{
				L.e(TAG,e.getMessage(),e);
			}
			try
			{
				if(outChannel != null) {
					outChannel.close();
				}
				if(outStream != null) {
					outStream.close();
				}
			}catch(IOException e)
			{
				L.e(TAG,e.getMessage(),e);
			}
		}
		return result;
	}

	/**
	 * 复制文件夹
	 *
	 * @param oldPath
	 *            String 源文件夹路径 如：E:/phsftp/src
	 * @param newPath
	 *            String 目标文件夹路径 如：E:/phsftp/dest
	 * @return boolean
	 */
	public static void copyFolder(String oldPath, String newPath) {
		try {
			// 如果文件夹不存在 则新建文件夹
			mkdir(newPath);
			File file = new File(oldPath);
			String[] files = file.list();
			File temp = null;
			for (int i = 0; i < files.length; i++) {
				if (oldPath.endsWith(File.separator)) {
					temp = new File(oldPath + files[i]);
				} else {
					temp = new File(oldPath + File.separator + files[i]);
				}

				if (temp.isFile()) {
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath
							+ "/" + (temp.getName()).toString());
					byte[] buffer = new byte[1024 * 2];
					int len;
					while ((len = input.read(buffer)) != -1) {
						output.write(buffer, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory()) {// 如果是子文件夹
					copyFolder(oldPath + "/" + files[i], newPath + "/"
							+ files[i]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 移动文件到指定目录
	 *
	 * @param oldPath
	 *            包含路径的文件名 如：E:/phsftp/src/ljq.txt
	 * @param newPath
	 *            目标文件目录 如：E:/phsftp/dest
	 */
	public static void moveFile(String oldPath, String newPath) {
		copyFile(oldPath, newPath);
		delFile(oldPath);
	}

	/**
	 * 移动文件到指定目录，不会删除文件夹
	 *
	 * @param oldPath
	 *            源文件目录 如：E:/phsftp/src
	 * @param newPath
	 *            目标文件目录 如：E:/phsftp/dest
	 */
	public static void moveFiles(String oldPath, String newPath) {
		copyFolder(oldPath, newPath);
		delAllFile(oldPath);
	}

	/**
	 * 移动文件到指定目录，会删除文件夹
	 *
	 * @param oldPath
	 *            源文件目录 如：E:/phsftp/src
	 * @param newPath
	 *            目标文件目录 如：E:/phsftp/dest
	 */
	public static void moveFolder(String oldPath, String newPath) {
		copyFolder(oldPath, newPath);
		delFolder(oldPath);
	}

	/**
	 * 读取数据
	 *
	 * @param inSream
	 * @param charsetName
	 * @return
	 * @throws Exception
	 */
	public static String readData(InputStream inSream, String charsetName)
			throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		while ((len = inSream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		byte[] data = outStream.toByteArray();
		outStream.close();
		inSream.close();
		return new String(data, charsetName);
	}

	/**
	 * 一行一行读取文件，适合字符读取，若读取中文字符时会出现乱码
	 *
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static Set<String> readFile(String path) throws Exception {
		Set<String> datas = new HashSet<String>();
		FileReader fr = new FileReader(path);
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		while ((line = br.readLine()) != null) {
			datas.add(line);
		}
		br.close();
		fr.close();
		return datas;
	}

	/**
	 * 获取文件指定文件的指定单位的大小
	 *
	 * @param filePath
	 *            文件路径
	 * @param sizeType
	 *            获取大小的类型1为B、2为KB、3为MB、4为GB
	 * @return double值的大小
	 */
	public static double getFileOrFilesSize(String filePath, int sizeType) {
		File file = new File(filePath);
		long blockSize = 0;
		try {
			if (file.isDirectory()) {
				blockSize = getFileSizes(file);
			} else {
				blockSize = getFileSize(file);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("获取文件大小", "获取失败!");
		}
		return FormatFileSize(blockSize, sizeType);
	}

	/**
	 * 调用此方法自动计算指定文件或指定文件夹的大小
	 *
	 * @param filePath
	 *            文件路径
	 * @return 计算好的带B、KB、MB、GB的字符串
	 */
	public static String getAutoFileOrFilesSize(String filePath) {
		File file = new File(filePath);
		long blockSize = 0;
		try {
			if (file.isDirectory()) {
				blockSize = getFileSizes(file);
			} else {
				blockSize = getFileSize(file);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("获取文件大小", "获取失败!");
		}
		return FormatFileSize(blockSize);
	}

	/**
	 * 获取指定文件大小
	 *
	 * @param file
	 * @return
	 * @throws Exception
	 */
	private static long getFileSize(File file) throws Exception {
		long size = 0;
		if (file.exists()) {
			FileInputStream fis = null;
			fis = new FileInputStream(file);
			size = fis.available();
			fis.close();
		} else {
			file.createNewFile();
			Log.e("获取文件大小", "文件不存在!");
		}
		return size;
	}

	/**
	 * 获取指定文件夹大小（包括文件夹下的所有文件）
	 *
	 * @param f
	 * @return
	 * @throws Exception
	 */
	private static long getFileSizes(File f) throws Exception {
		if (f == null) {
			return 0;
		}
		long size = 0;
		File flist[] = f.listFiles();
		if (flist == null) {
			return 0;
		}
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getFileSizes(flist[i]);
			} else {
				size = size + getFileSize(flist[i]);
			}
		}
		return size;
	}

	/**
	 * 转换文件大小
	 *
	 * @param fileS
	 * @return
	 */
	public static String FormatFileSize(long fileS) {
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		String wrongSize = "0B";
		if (fileS == 0) {
			return wrongSize;
		}
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "KB";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "MB";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "GB";
		}
		return fileSizeString;
	}

	/**
	 * 转换文件大小,指定转换的类型
	 *
	 * @param fileS
	 * @param sizeType
	 * @return
	 */
	public static double FormatFileSize(long fileS, int sizeType) {
		DecimalFormat df = new DecimalFormat("#.00");
		double fileSizeLong = 0;
		switch (sizeType) {
			case SIZETYPE_B:
				fileSizeLong = Double.valueOf(df.format((double) fileS));
				break;
			case SIZETYPE_KB:
				fileSizeLong = Double.valueOf(df.format((double) fileS / 1024));
				break;
			case SIZETYPE_MB:
				fileSizeLong = Double.valueOf(df.format((double) fileS / 1048576));
				break;
			case SIZETYPE_GB:
				fileSizeLong = Double.valueOf(df
						.format((double) fileS / 1073741824));
				break;
			default:
				break;
		}
		return fileSizeLong;
	}

	/**
	 * @param isAppUpdateDir 是否是app升级文件夹
	 * @return 获取本地文件夹路径,如果不存在文件夹直接创建文件夹
	 */
	public static String getUserSDPath(boolean isAppUpdateDir,String fileFolderName) {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
			String fileDir;
			if (isAppUpdateDir) {
				fileDir = sdDir.toString() + "/YNedutDownloads/appupdate";
			} else {
				String loginAccount = LastLoginUserSP.getInstance(AppController.getInstance()).getUserLoginAccount();
				if (!TextUtils.isEmpty(loginAccount)) {
					if (fileFolderName != null) {
						fileDir = sdDir.toString() + "/YNedutDownloads/"+loginAccount+"/"+fileFolderName;
					} else {
						fileDir = sdDir.toString() + "/YNedutDownloads/"+loginAccount;
					}
				} else {
					fileDir = sdDir.toString() + "/YNedutDownloads/common";
				}
			}
			File fiDir = new File(fileDir);

			try {
				if (fiDir.exists() && fiDir.isDirectory()) {
					return fiDir.toString();
				} else {
					fiDir.mkdirs();
					return fiDir.toString();
				}
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
		} else {
			return "";
		}

	}
	/**
	 * 根据帐号删除的时候返回下载文件的路径
	 * @param account
	 * @return
	 */
	public static String getFilePathByUserAccount(String account){
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
			String fileDir;
			fileDir = sdDir.toString() + "/YNedutDownloads/"+account;
			return fileDir;
		} else {
			return "";
		}

	}

	/**
	 * 获取已下载文件所在sd卡的路径
	 * @param fileName
	 * @return
	 */
	public static String getFilePath(String fileName) {
		String filePath = getUserSDPath(false, mFilePath);
		if (fileName != null) {
			filePath = filePath + File.separator + fileName;
		}
		return filePath;
	}

	/**
	 * 获取某文件夹下的文件的的绝对路径
	 * @param folderName 文件夹名
	 * @param fileName 文件名
	 * @return 文件的的绝对路径
	 */
	public static String getFileAbsolutePath(String folderName, String fileName) {
		boolean isUpdateFilePath = false;
		if (mAppUpdateFilePath.equals(folderName)) {
			isUpdateFilePath = true;
		}
		String filePath = getUserSDPath(isUpdateFilePath,folderName);
		if (!TextUtils.isEmpty(filePath)) {
			filePath = filePath + File.separator + fileName;
		}
		return filePath;
	}

	/**
	 * 文件是否存在
	 * @param fileName
	 * @return
	 */
	public static boolean isFileExist(String fileName) {
		return getFileByName(fileName).exists();
	}

	/**
	 * 根据名称获取某文件
	 * @param fileName 文件名
	 * @return 文件
	 */
	public static File getFileByName(String fileName) {
		String filePath = getFilePath(fileName);
		return new File(filePath);
	}

	/**
	 * 获取图片所在sd卡的路径
	 * @param fileName
	 * @return
	 */
	public static String getImgPath(String fileName) {
		String imgPath = getUserSDPath(false, mImgPath);
		if (fileName != null) {
			imgPath = imgPath +"/"+fileName;
		}
		return imgPath;
	}

	/**
	 * 获取用户头像文件
	 * @param userNo 文件名，以userno命名
	 * @return 返回用户文件
	 */
	public static File getAvatarByName(String userNo) {
		String filePath = getUserSDPath(false, mAvatarPath);
		if (userNo != null) {
			filePath = filePath + File.separator + userNo;
		}
		return new File(filePath);
	}

	/**
	 * 获取登录用户头像文件
	 * @param userNo 文件名，以userno命名
	 * @return 返回用户文件
	 */
	public static File getLoginAvatarByName(String userNo) {
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) {
			String sdDir = Environment.getExternalStorageDirectory().toString();// 获取跟目录
			String filePath = sdDir + "/ynmessager/" + userNo + File.separator + mAvatarPath;
			if (userNo != null) {
				filePath = filePath + File.separator + userNo;
			}
			return new File(filePath);
		}
		return null;
	}

	/**
	 * 上传文件
	 * @param json 格式化的json：eg.  {'token':'6B9050586868D200F159CFAC6556BC50EC58CC5B17970FA0E7E3C45185DC91EA49AFBC3A93F9D86F486FEDE8BA216A14626EB8FFC6775350',
	 * 'licensee':'zouyanxia','useType':'1','fileTypeList':[{'fieldName':'20150724_200613.jpg','fileType':'1'}]}
	 * @param filePath 文件本地路径
	 * @return 	strings[0] ："fileId" 文件ID ;
	 * 			strings[1] ："fileName" 文件名称;
	 *  		strings[2] ："fileType" 文件类型;
	 */
	public static String[] uploadFile(String json,String filePath) {

		final File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			return null;
		}
		/**
		 * 第一部分
		 */
//		String url = Const.PROTOCOL+"10.6.0.49:9090"+"/plugins/orgmanager/ynmessenger/upload";
//		String url = Const.PROTOCOL+"10.6.0.33:9090"+"/plugins/orgmanager/ynmessenger/upload";
		final String[] returnStrings = new String[3];
		String url = Const.PROTOCOL+AppController.getInstance().CONFIG_INSIDE_FILE_TRANSLATE_IP+Const.UPLOAD_FILE_URL;
		L.v(url);

		SyncHttpClient httpClient = new SyncHttpClient();
		RequestParams params = new RequestParams();
		try {
			params.put("json",json);
			params.put(UUID.randomUUID().toString(),file);

			httpClient.post(url, params, new AsyncHttpResponseHandler() {

				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
					if (responseBody == null) {
						return;
					}
					JSONArray response;
					try
					{
						response = new JSONArray(new String(responseBody));
						JSONObject obj = response.getJSONObject(0);
						returnStrings[0] = obj.getString("fileId");
						returnStrings[1] = obj.getString("fileName");
						returnStrings[2] = obj.getString("fileType");
					}catch(JSONException e)
					{
						e.printStackTrace();
					}
					L.v("上传成功 == "+statusCode);
				}

				@Override
				public void onFailure(int statusCode, Header[] headers,
									  byte[] responseBody, Throwable error) {
					L.v("上传失败 == "+statusCode);
				}

				@Override
				public void onProgress(long bytesWritten, long totalSize) {
//					super.onProgress(bytesWritten, totalSize);
					//				(totalSize > 0) ? (bytesWritten * 1.0 / totalSize) * 100 : -1)
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnStrings;
	}

	/**
	 * 根据文件名格式化json
	 * @param receiveUserNo 有权查看者,可以是用户编号、群（讨论组）号、还可以为all
	 * @param userType 1:P2P文件;2:群文件;3:群共享文件;4:升级包;5:头像
	 * @param fileName 文件名
	 * @param fileType 0:普通文件,1:图片,2:音频,3:视频,4:office文档
	 * @return 格式化json
	 */
	public static String formatSendFileJsonByFileName(String receiveUserNo,String userType,String fileName,String fileType) {
		String token = getSendFileToken();
		StringBuilder jsonBuilder = new StringBuilder();
		jsonBuilder.append("{'token':'"+token+"',");
		jsonBuilder.append("'licensee':'"+receiveUserNo+"',");
		jsonBuilder.append("'useType':'"+userType+"',");
		jsonBuilder.append("'fileTypeList':[{'fieldName':'"+fileName+"','fileType':'"+fileType+"'}]}");
		return jsonBuilder.toString();
	}

	/**
	 * 根据文件路径格式化json
	 * @param receiveUserNo 有权查看者,可以是用户编号、群（讨论组）号、还可以为all
	 * @param userType 1:P2P文件;2:群文件;3:群共享文件;4:升级包;5:头像
	 * @param filePath 文件路径
	 * @param fileType 0:普通文件,1:图片,2:音频,3:视频,4:office文档
	 * @return 格式化json
	 */
	public static String formatSendFileJsonByFilePath (String receiveUserNo,String userType,String filePath,String fileType)
	{
		String token = getSendFileToken();
		if (filePath == null || filePath.isEmpty()) {
			return null;
		}
		String fileName;
		if (filePath.contains("\\")) {
			fileName = filePath.substring(filePath.lastIndexOf("\\"));
		} else {
			fileName = filePath;
		}

		StringBuilder jsonBuilder = new StringBuilder();
		jsonBuilder.append("{'token':'"+token+"',");
		jsonBuilder.append("'licensee':'"+receiveUserNo+"',");
		jsonBuilder.append("'useType':'"+userType+"',");
		jsonBuilder.append("'fileTypeList':[{'fieldName':'"+fileName+"','fileType':'"+fileType+"'}]}");
		return jsonBuilder.toString();
	}

	/**
	 * 获取文件收发的token
	 * @return token
	 */
	public static String getSendFileToken() {
		if (AppController.getInstance().mLoginUser == null) {
			return null;
		}
		String token = "{'userNo':'"+AppController.getInstance().mLoginUser.getUserNo()+"','passwd':'" + AppController.getInstance().mLoginUser.getPassWord()+ "','cversion':'100'}";
		token = DESUtil.encrypt(token, Const.DES_KEY);
		return token;
	}

	/**
	 * 格式化获取用户头像的参数 JSON字符串
	 * @param userNoList 用户唯一标识
	 * @param headType 1为原图,2为100*100,3为50*50
	 * @param serverTime
	 * @return
	 */
	public static String formatGetUserAvatarParamJson(List<User> userNoList,String headType,String serverTime) {
		StringBuffer userNoListStr = new StringBuffer();
		if (userNoList == null || userNoList.size() == 0) {
			userNoListStr.append("[]");
		} else {
			userNoListStr.append("[");
			for (User tempUser:userNoList) {
				userNoListStr.append("\"");
				userNoListStr.append(tempUser.getUserNo());
				userNoListStr.append("\",");
			}
			userNoListStr.deleteCharAt(userNoListStr.lastIndexOf(","));
			userNoListStr.append("]");
		}
		StringBuilder jsonBuilder = new StringBuilder();
		jsonBuilder.append("{\"userNoList\":"+userNoListStr.toString()+",");
		jsonBuilder.append("\"headType\":" + headType + ",");
		jsonBuilder.append("\"token\":\"" + getSendFileToken() + "\",");
		jsonBuilder.append("\"serverTime\":\""+serverTime+"\"}");
		L.e("avatar json == "+jsonBuilder.toString());
		return jsonBuilder.toString();
	}

	/**
	 * @param filePath
	 * @param message
	 * @param messageBodyEntity
	 * @param mSendFileListener
	 * @return
	 */
	public static String[] uploadFile(String json, String filePath,
									  final Message message, final MessageBodyEntity messageBodyEntity, final SendingListener mSendFileListener) {
		final File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			return null;
		}
		/**
		 * 第一部分
		 */
//		String url = Const.PROTOCOL+"10.6.0.49:9090"+"/plugins/orgmanager/ynmessenger/upload";
//		String url = Const.PROTOCOL+"10.6.0.33:9090"+"/plugins/orgmanager/ynmessenger/upload";
		final String[] returnStrings = new String[3];
		String url = Const.PROTOCOL+AppController.getInstance().CONFIG_INSIDE_FILE_TRANSLATE_IP+Const.UPLOAD_FILE_URL;
		L.v(url);

		SyncHttpClient httpClient = new SyncHttpClient();
		RequestParams params = new RequestParams();
		try {
			params.put("json",json);
			params.put(UUID.randomUUID().toString(),file);

			httpClient.post(url, params, new AsyncHttpResponseHandler() {

				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
					if (responseBody == null) {
						return;
					}
					JSONArray response;
					try {
						response = new JSONArray(new String(responseBody));
						JSONObject obj = response.getJSONObject(0);
						returnStrings[0] = obj.getString("fileId");
						returnStrings[1] = obj.getString("fileName");
						returnStrings[2] = obj.getString("fileType");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					L.v("上传成功 == "+statusCode);
				}

				@Override
				public void onFailure(int statusCode, Header[] headers,
									  byte[] responseBody, Throwable error) {
					L.v("上传失败 == "+statusCode);
				}

				@Override
				public void onProgress(long bytesWritten, long totalSize) {
//					int progress = (int) ((totalSize > 0) ? (bytesWritten * 1.0 / totalSize) * 100 : -1);
//					if (progress == mProgress) {
//						return;
//					}
//					mProgress = progress;
//					if (progress % 4 == 0) {
//						mSendFileListener.onProgressUpdateSend(message,messageBodyEntity,progress);
//					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnStrings;
	}

	/**
	 * @param uploadJson
	 * @param filePath
	 * @param uploadResponseHandler
	 * @return
	 */
	public static String[] uploadFile(String uploadJson, String filePath,
									  UploadResponseHandler uploadResponseHandler) {
		return uploadFile(uploadJson,UUID.randomUUID().toString(),filePath,uploadResponseHandler);
	}

	public static String[] uploadFile(String uploadJson, String uuid, String filePath,
									  UploadResponseHandler uploadResponseHandler)
	{
		final File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			return null;
		}
		/**
		 * 第一部分
		 */
		//		String url = Const.PROTOCOL+"10.6.0.49:9090"+"/plugins/orgmanager/ynmessenger/upload";
		//		String url = Const.PROTOCOL+"10.6.0.33:9090"+"/plugins/orgmanager/ynmessenger/upload";
		String url = Const.PROTOCOL+AppController.getInstance().CONFIG_INSIDE_FILE_TRANSLATE_IP+Const.UPLOAD_FILE_URL;
		L.v(url);
		L.v(uploadJson);
		SyncHttpClient httpClient = new SyncHttpClient();
		httpClient.setTimeout(6000);
		httpClient.setMaxRetriesAndTimeout(0,6000);
		RequestParams params = new RequestParams();
		try {
			params.put("json",uploadJson);
			params.put(uuid,file);
			httpClient.post(url, params, uploadResponseHandler);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return uploadResponseHandler.returnStrings;
	}

	/**
	 * 下载头像压缩包
	 */
	public static void downloadAvatarZipFile(Context context,List<User> userNoList,String headType) {
		String serverTime;
		if (context == null) {
			serverTime = "0";
		} else {
			serverTime = LastLoginUserSP.getAvatarServerTime(context);
		}
		String paramStr = formatGetUserAvatarParamJson(userNoList,headType,serverTime);
		downloadFile(context, Const.DOWNLOAD_AVATAR_URL, paramStr, "UserAvatar.zip");
	}

	/**
	 * 下载文件
	 * @param context
	 * @param url 文件url
	 * @param param url参数
	 * @param fileName 文件名
	 */
	public static void downloadFile(final Context context, String url, String param, final String fileName) {
		String userId = null;
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		if (context!=null) {
			userId = LastLoginUserSP.getLoginUserNo(context);
		}
		getFileTransUrl(context);
		String downloadUrl = Const.PROTOCOL+AppController.getInstance().CONFIG_INSIDE_FILE_TRANSLATE_IP+url;
		RequestParams params = new RequestParams();
		params.put("params", param);
		L.v("url== " + AsyncHttpClient.getUrlWithQueryString(true, downloadUrl, params));
		//异步请求用户头像
		asyncHttpClient.get(downloadUrl, params, new AsyncHttpResponseHandler() {

			private FileOutputStream os;
			private String downloadFilePath;
			private String userNo;

			public AsyncHttpResponseHandler setUserNo(String userNo) {
				this.userNo = userNo;
				return this;
			}

			@Override
			public void onSuccess(int statusCode, final Header[] headers,
								  byte[] responseBody) {
				if (responseBody == null) {
					return;
				}
				try {
					downloadFilePath = FileUtil.getFileAbsolutePath(mCachePath, fileName);
					final File mFile = new File(downloadFilePath);
					os = new FileOutputStream(mFile);
					os.write(responseBody);
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if ("UserAvatar.zip".equals(fileName)) {
					//解压压缩包
					FileDownLoader.getInstance().getThreadPool().execute(new Runnable() {
						@Override
						public void run() {
							FileUtil.upZipFile(new File(downloadFilePath), FileUtil.getUserSDPath(false, mAvatarPath));
							//保存用户ID头像的serverTime
							if (!TextUtils.isEmpty(userNo) && context != null) {
								for (Header header : headers) {
									if ("serverTime".equals(header.getName())) {
										LastLoginUserSP.setAvatarServerTime(context, userNo, header.getValue());
										break;
									}
								}
							}
						}
					});
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
								  byte[] responseBody, Throwable error) {
				if (responseBody == null) {
					return;
				}
				String onFailure = new String(responseBody);
				L.e("download file onFailure " + onFailure);
			}

			@Override
			public void onFinish() {
				super.onFinish();
			}

			@Override
			public void onProgress(long bytesWritten, long totalSize) {
				super.onProgress(bytesWritten, totalSize);
			}
		}.setUserNo(userId));
	}

	/**
	 * 获取openfire文件传输的地址
	 * @param context
	 * @return
	 */
	public static String getFileTransUrl(Context context) {
		//确认文件传输地址
		if (TextUtils.isEmpty(AppController.getInstance().CONFIG_INSIDE_FILE_TRANSLATE_IP)) {
			AppController.getInstance().CONFIG_INSIDE_FILE_TRANSLATE_IP = LastLoginUserSP.getUserLoginServiceUrl(context);
			if (AppController.getInstance().CONFIG_INSIDE_FILE_TRANSLATE_IP.contains(":")) {
				String[] addrs = AppController.getInstance().CONFIG_INSIDE_FILE_TRANSLATE_IP.split(":");
				AppController.getInstance().CONFIG_INSIDE_FILE_TRANSLATE_IP = addrs[0]+":9090";
			} else {
				AppController.getInstance().CONFIG_INSIDE_FILE_TRANSLATE_IP = AppController.getInstance().CONFIG_INSIDE_FILE_TRANSLATE_IP +":9090";
			}
		}
		return AppController.getInstance().CONFIG_INSIDE_FILE_TRANSLATE_IP;
	}

	/**
	 * 获得文件的后缀名
	 * @param url 文件的地址
	 * @return 返回小写形式的后缀名字符串
	 */
	public static String fileExt(String url) {
		if (url.indexOf("?") > -1) {
			url = url.substring(0, url.indexOf("?"));
		}
		if (url.lastIndexOf(".") == -1) {
			return null;
		} else {
			String ext = url.substring(url.lastIndexOf("."));
			if (ext.indexOf("%") > -1) {
				ext = ext.substring(0, ext.indexOf("%"));
			}
			if (ext.indexOf("/") > -1) {
				ext = ext.substring(0, ext.indexOf("/"));
			}
			return ext.toLowerCase();
		}
	}

	public static String getFileType(String filePath) {
		return mFileTypes.get(getFileHeader(filePath));
	}

	// 获取文件头信息
	public static String getFileHeader(String filePath) {
		FileInputStream is = null;
		String value = null;
		try {
			is = new FileInputStream(filePath);
			byte[] b = new byte[4];
			is.read(b, 0, b.length);
			value = bytesToHexString(b);
		} catch (Exception e) {
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
		return value;
	}

	private static String bytesToHexString(byte[] src) {
		StringBuilder builder = new StringBuilder();
		if (src == null || src.length <= 0) {
			return null;
		}
		String hv;
		for (int i = 0; i < src.length; i++) {
			hv = Integer.toHexString(src[i] & 0xFF).toUpperCase();
			if (hv.length() < 2) {
				builder.append(0);
			}
			builder.append(hv);
		}
		return builder.toString();
	}

	/**
	 * 解压缩一个文件
	 *
	 * @param zipFile
	 *            压缩文件
	 * @param folderPath
	 *            解压缩的目标目录
	 * @throws IOException
	 *             当解压缩过程出错时抛出
	 */

	public static void upZipFile(File zipFile, String folderPath) {
		try {
			File desDir = new File(folderPath);
			if (!desDir.exists()) {
				desDir.mkdirs();
			}
			ZipFile zf = new ZipFile(zipFile);
			for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements(); ) {
				ZipEntry entry = ((ZipEntry) entries.nextElement());
				InputStream in = zf.getInputStream(entry);
				String str = folderPath;
				// str = new String(str.getBytes("8859_1"), "GB2312");
				String fileName = URLEncoder.encode(entry.getName(), "UTF-8");
				//去掉文件后缀名
				if (fileName.contains(".")) {
					int dot = fileName.lastIndexOf('.');
					if ((dot >-1) && (dot < (fileName.length()))) {
						fileName = fileName.substring(0, dot);
					}
				}
				File desFile = new File(str, fileName);

				if (!desFile.exists()) {
					File fileParentDir = desFile.getParentFile();
					if (!fileParentDir.exists()) {
						fileParentDir.mkdirs();
					}
				}

				OutputStream out = new FileOutputStream(desFile);
				byte buffer[] = new byte[1024 * 1024];
				int realLength = in.read(buffer);
				while (realLength != -1) {
					out.write(buffer, 0, realLength);
					realLength = in.read(buffer);
				}
				out.close();
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解压缩一个文件
	 *
	 */
	public static void Unzip(String zipFile, String targetDir) {
		int BUFFER = 4096; // 这里缓冲区我们使用4KB，
		String strEntry; // 保存每个zip的条目名称
		try {
			BufferedOutputStream dest = null; // 缓冲输出流
			FileInputStream fis = new FileInputStream(zipFile);
			ZipInputStream zis = new ZipInputStream(
					new BufferedInputStream(fis));
			ZipEntry entry; // 每个zip条目的实例
			while ((entry = zis.getNextEntry()) != null) {
				try {
					int count;
					byte data[] = new byte[BUFFER];
					strEntry = entry.getName();
					File entryFile = new File(targetDir + strEntry);
					File entryDir = new File(entryFile.getParent());
					if (!entryDir.exists()) {
						entryDir.mkdirs();
					}
					FileOutputStream fos = new FileOutputStream(entryFile);
					dest = new BufferedOutputStream(fos, BUFFER);
					while ((count = zis.read(data, 0, BUFFER)) != -1) {

						dest.write(data, 0, count);
					}
					dest.flush();
					dest.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			zis.close();
		} catch (Exception cwj) {
			cwj.printStackTrace();
		}
	}

	public static boolean externalMemoryAvailable()
	{
		return android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	public static long getAvailableInternalMemorySize()
	{
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return  availableBlocks * blockSize;
	}

	public static long getTotalInternalMemorySize()
	{
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	public static long getAvailableExternalMemorySize()
	{
		if (externalMemoryAvailable())
		{
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;
		} else
		{
			return -1L;
		}
	}

	public static long getTotalExternalMemorySize()
	{
		if (externalMemoryAvailable())
		{
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			return totalBlocks * blockSize;
		} else
		{
			return -1L;
		}
	}

	public static String formatSize(long size)
	{
		String suffix = null;

		if (size >= 1024)
		{
			suffix = "KB";
			size /= 1024;
			if (size >= 1024)
			{
				suffix = "MB";
				size /= 1024;
			}
		}

		StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

		int commaOffset = resultBuffer.length() - 3;
		while (commaOffset > 0)
		{
			resultBuffer.insert(commaOffset, ',');
			commaOffset -= 3;
		}

		if (suffix != null) {
			resultBuffer.append(suffix);
		}
		return resultBuffer.toString();
	}

	/**
	 * 图片是否存在
	 */
	public boolean isBitmapExists(String fileName) {
		String bitmapPath = getImgPath(fileName);
		return new File(bitmapPath).exists();
	}

	/**
	 * @param fileName
	 * @return
	 */
	public long getBitmapSize(String fileName) {
		String bitmapPath = getImgPath(fileName);
		return new File(bitmapPath).length();
	}

	/**
	 * @param fileName
	 * @return
	 */
	public Bitmap getBitmap(String fileName) {
		String bitmapPath = getImgPath(fileName);
		return BitmapFactory.decodeFile(bitmapPath);
	}
}
