package com.yineng.ynmessager.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Base64;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.activity.picker.SendingListener;
import com.yineng.ynmessager.activity.picker.file.SendFileTask;
import com.yineng.ynmessager.activity.picker.image.SendImageTask;
import com.yineng.ynmessager.activity.picker.voice.SendVoiceTask;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.groupsession.GroupChatMsgEntity;
import com.yineng.ynmessager.bean.imgpicker.ImageFile;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.bean.p2psession.P2PChatMsgEntity;
import com.yineng.ynmessager.camera.CameraActivity;
import com.yineng.ynmessager.db.P2PChatMsgDao;
import com.yineng.ynmessager.db.dao.DisGroupChatDao;
import com.yineng.ynmessager.db.dao.GroupChatDao;
import com.yineng.ynmessager.imageloader.FileDownLoader;
import com.yineng.ynmessager.manager.XmppConnectionManager;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * 图片处理工具
 *
 * @author YINENG
 *
 */
public class ImageUtil {
	private Context context;
	public static int mDefaultWidth = 480;
	public static int mDefaultHeight = 800;

	public ImageUtil(Context context) {
		this.context = context;
	}

	public static Bitmap getBitmapFromSource(String name,Context context){

		int resID = context.getResources().getIdentifier(name, "mipmap", context.getApplicationInfo().packageName);
		return BitmapFactory.decodeResource(context.getResources(), resID);
	}



	/**
	 * 判断在h5中返回的地址是否是图片地址
	 * @param url
	 * @return
	 */
	public static boolean CheckIsImageUrl(String url){
		String regex = "^(bmp|jpg|jpeg|png|tiff|gif|pcx|tga|exif|fpx|svg|psd|cdr|pcd|dxf|ufo|eps|ai|raw|wmf)$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(url.toLowerCase());
		return matcher.matches();
	}



	/**
	 * 图片转成string
	 *
	 * @param bitmap
	 * @return
	 */
	public static String BitmapToBase64Str(Bitmap bitmap)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] appicon = baos.toByteArray();// 转为byte数组
		return Base64.encodeToString(appicon, Base64.DEFAULT);

	}

	/**
	 * 自定义相机点击OK上传图片方法
	 * @param imagepath  必须是经过压缩和缩放的已经保存好的图片路劲
	 * @return  经过base64编码返回字符串
	 */
	public static String ImagetoBase64String(String imagepath){

		Bitmap bm = BitmapFactory.decodeFile(imagepath);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 因为传过来的图片路径已经是经过压缩和缩放的所以在此处不缩放，比例为100
		byte[] b = baos.toByteArray();

		return Base64.encodeToString(b, Base64.DEFAULT);
	}

	/**
	 * 获得图片宽度
	 *
	 *            R.mipmap.jimg001
	 * @return
	 */
	public static int getImageWidth(String path) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);
		opts.inSampleSize = 1;
		opts.inJustDecodeBounds = false;
		int width = opts.outWidth;
		return width;
	}

	/**
	 *
	 * 获得图片高
	 *
	 *  R.mipmap.jimg001
	 * @return
	 */
	public static int getImageHeight(String path) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);
		opts.inSampleSize = 1;
		opts.inJustDecodeBounds = false;
		int height = opts.outHeight;
		return height;
	}


	/*
     * 获取Bitmap的缩略图
     */
	public static Bitmap getBitmapThumbnail(String path, int width, int height) {
		Bitmap bitmap = null;

		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);
		opts.inSampleSize = Math.max((int) (opts.outHeight / (float) height),
				(int) (opts.outWidth / (float) width));
		opts.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(path, opts);
		return bitmap;
	}

	/*
     * 获取Bitmap的缩略图
     */
	public static Bitmap getBitmapThumbnail(Bitmap bmp, int width, int height) {
		Bitmap bitmap = null;
		if (bmp != null) {
			int bmpWidth = bmp.getWidth();
			int bmpHeight = bmp.getHeight();
			if (width != 0 && height != 0) {
				Matrix matrix = new Matrix();
				float scaleWidth = ((float) width / bmpWidth);
				float scaleHeight = ((float) height / bmpHeight);
				matrix.postScale(scaleWidth, scaleHeight);
				bitmap = Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeight,
						matrix, true);
			} else {
				bitmap = bmp;
			}
		}
		return bitmap;
	}

	/**
	 *
	 * 对图片进行缩放，降低图片尺寸
	 *
	 * @param bm
	 *            值为1则表示原图大小 2表示放大一倍， 0.5表示缩小一倍。缩小会失帧
	 * @return
	 */
	public static Bitmap scaleToFit(Bitmap bm, Context context) {
		String tag = "scaleToFit";
		Bitmap bmResult = null;
		float width = bm.getWidth();
		float height = bm.getHeight();
		float screenWidth = 480;
		float screenHeight = 800;

		float Xscale = 1;
		float Yscale = 1;
		if (screenWidth < width) {
			Xscale = screenWidth / width;
		}

		if (screenHeight < height) {
			Yscale = screenHeight / height;
		}

		Matrix matrix = new Matrix();

		if (Xscale > Yscale) {
			matrix.postScale(Yscale, Yscale);
		} else {
			matrix.postScale(Xscale, Xscale);
		}
		bmResult = Bitmap.createBitmap(bm, 0, 0, (int) width, (int) height,
				matrix, true);
		return comp(bmResult, context);
	}


	/**
	 * 保存图片为PNG
	 *
	 * @param bitmap
	 */
	public static String savePNG_After(Bitmap bitmap, String path) {
		File file = new File(path);
		try {
			FileOutputStream out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return path;
	}

	/**
	 * 保存图片为JPEG
	 *
	 * @param bitmap
	 * @param path
	 */
	public static void saveJPGE_After(Bitmap bitmap, String path) {
		File file = new File(path);
		try {
			FileOutputStream out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * byte[] 转 bitmap
	 *
	 * @param b
	 * @return
	 */
	public static Bitmap bytesToBimap(byte[] b) {
		if (b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;
		}
	}

	/**
	 * bitmap 转 byte[]
	 *
	 * @param bm
	 * @return
	 */
	public static byte[] bitmapToBytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		return baos.toByteArray();
	}

	/**
	 * 把bitmap转换成String
	 *
	 * @param filePath
	 * @return
	 */
	public static String bitmapToString(String filePath) {

		Bitmap bm = getSmallBitmap(filePath);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 40, baos);
		byte[] b = baos.toByteArray();

		return Base64.encodeToString(b, Base64.DEFAULT);

	}

	/**
	 * 计算图片的缩放值
	 *
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
											int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	/**
	 * 根据资源ID加载一个指定大小的图片
	 * @param res
	 * @param resId
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
														 int reqWidth, int reqHeight) {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	/**
	 * 获得缩略图
	 * 图片大小480*800
	 */
	public static Bitmap getSmallBitmap(String filePath) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, 480, 800);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		//在读取时加上图片的Config参数，可以跟有效减少加载的内存，从而跟有效阻止抛out of Memory异常
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		return BitmapFactory.decodeFile(filePath, options);
	}

	/**
	 * 获得缩略图
	 * 图片大小240*320
	 */
	public static Bitmap getThumbBitmap(String filePath) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, 240, 320);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(filePath, options);
	}

	/**
	 * 获得缩略图
	 *
	 * @param
	 * @return
	 */
	public static Bitmap getSmallBitmap(String filePath, Context context) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		int width = ScreenUtils.getScreenWidth(context)/7;
		int height = ScreenUtils.getScreenHeight(context)/7;
//		L.e("height == "+height+" width == "+width);
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, width, height);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(filePath, options);
	}

	/**
	 *
	 * 对图片进行质量压缩，让图片小于100kb
	 *
	 * @param image
	 * @param srcPath
	 * @return
	 */
	public static String compressImage(Bitmap image, String srcPath) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中

		int options = 100;
		while (baos.toByteArray().length / 1024 > 150) { // 循环判断如果压缩后图片是否大于150kb,大于继续压缩

			baos.reset();// 重置baos即清空baos

			options -= 10;// 每次都减少10

			image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
			System.gc();
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中

		image = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片

		int degree = readPictureDegree(srcPath);
		if (degree != 0) {// 旋转照片角度
			image = rotateBitmap(image, degree);
		}

		try {
//			FileUtil.delFile(srcPath); 是否需要删除源文件  待定
			FileOutputStream os = new FileOutputStream(srcPath);
			os.write(bitmapToBytes(image));
			os.flush();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return srcPath;
	}

	/**
	 *
	 * 按比例，缩小图片
	 *
	 * @param srcPath
	 * @return
	 */
	public static String getimage(String srcPath, Context context) {

		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = false;
		newOpts.inPurgeable = true;
		newOpts.inInputShareable = true;
		// Do not compress
		newOpts.inSampleSize = 1;
		newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		L.e("newOpts.outWidth == "+w+" newOpts.outHeight == "+h);
		// 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
		int hh = mDefaultHeight;
		int ww = mDefaultWidth;
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;// be=1表示不缩放
		if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
			be = newOpts.outWidth / ww;
		} else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
			be = newOpts.outHeight / hh;
		}
		if (be <= 0) {
            be = 1;
        }
		newOpts.inSampleSize = be;// 设置缩放比例
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		if (bitmap == null) {
			return null;
		}
		compressImage(bitmap,srcPath);// 压缩好比例大小后再进行质量压缩
		if (!bitmap.isRecycled()) {
			bitmap.recycle();
		}
		return srcPath;

	}

	/**
	 * 自定义相机获取比例压缩的图片
	 * @param imagePath
	 * @return
	 */
	public static Bitmap getScaleImage(String imagePath){

		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = false;
		newOpts.inPurgeable = true;
		newOpts.inInputShareable = true;
		// Do not compress
		newOpts.inSampleSize = 1;
		newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
		Bitmap bitmap = BitmapFactory.decodeFile(imagePath, newOpts);

		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		// 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
		float hh = 800f;// 这里设置高度为800f
		float ww = 480f;// 这里设置宽度为480f
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;// be=1表示不缩放
		if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0) {
            be = 1;
        }
		newOpts.inSampleSize = be;// 设置缩放比例
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeFile(imagePath, newOpts);
		return  bitmap;

	}

	/**
	 *
	 * 对图片进行质量压缩，让图片小于100kb
	 *
	 *
	 * @param
	 * @param image
	 * @return
	 */
	public static Bitmap getQuaitlyImage(Bitmap image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 90, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中

		int options = 90;
		while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于150kb,大于继续压缩

			baos.reset();// 重置baos即清空baos

			options -= 10;// 每次都减少10

			image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
			System.gc();
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中

		image = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片



		return image;
	}

	/**
	 * 保存添加过水印的图片
	 * @param scaleImage
	 * @param imagePath
	 */
	public static void saveWaterBit(Bitmap scaleImage, String imagePath){

		int degree = readPictureDegree(imagePath);
		if (degree != 0) {// 旋转照片角度
			scaleImage = rotateBitmap(scaleImage, degree);
		}

		try {
			FileUtil.delFile(imagePath);// 删除原路径旧的文件
			FileOutputStream os = new FileOutputStream(imagePath);//将新压缩的图片存在原来的路径下
			os.write(bitmapToBytes(scaleImage));
			os.flush();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	/**
	 *
	 * 图片按比例压缩
	 *
	 * @param image
	 * @return
	 */
	public static Bitmap comp(Bitmap image, Context context) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		if (baos.toByteArray().length / 1024 > 200) {// 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
			baos.reset();// 重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, 50, baos);// 这里压缩20%，把压缩后的数据存放到baos中
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		// 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为

		int hh = 800;
		int ww = 480;
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;// be=1表示不缩放
		if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
			be = newOpts.outWidth / ww;
		} else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
			be = newOpts.outHeight / hh;
		}
		if (be <= 0) {
            be = 1;
        }
		newOpts.inSampleSize = be;// 设置缩放比例
		newOpts.inPreferredConfig = Config.RGB_565;// 降低图片从ARGB888到RGB565
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		isBm = new ByteArrayInputStream(baos.toByteArray());
		bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		return bitmap;// 压缩好比例大小后再进行质量压缩
	}


	/**
	 * 添加到图库
	 */
	public static void galleryAddPic(Context context, String path) {
		Intent mediaScanIntent = new Intent(
				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		File f = new File(path);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		context.sendBroadcast(mediaScanIntent);
	}

	/**
	 * 拍照
	 * @return
	 */
	public static String takePhoto(Activity activity) {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		try {
			String state = Environment.getExternalStorageState(); // 判断是否存在sd卡
			if (state.equals(Environment.MEDIA_MOUNTED)) { // 直接调用系统的照相机
				// 指定存放拍摄照片的位置
				File photoFile = createImageFile();
				takePictureIntent
						.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
				activity.startActivityForResult(takePictureIntent, Const.REQUEST_TAKE_PHOTO);
				return photoFile.getAbsolutePath();
			} else {
				Toast.makeText(activity, "请检查手机是否有SD卡", Toast.LENGTH_LONG).show();
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 把程序拍摄的照片放到 SD卡的 Pictures目录中 sheguantong 文件夹中
	 * 照片的命名规则为：ynmsg__20130125_173729.jpg
	 *
	 * @return
	 * @throws IOException
	 */
	public static File createImageFile() throws IOException {

		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String timeStamp = format.format(new Date());
		String imageFileName = "ynmsg_" + timeStamp + ".jpg";

		File image = new File(FileUtil.getImgPath(null), imageFileName);

		return image;
	}

	/**
	 * 压缩图片，处理某些手机拍照角度旋转的问题
	 * @param filePath 图片路径
	 * @param quality 图片质量
	 * @return
	 * @throws FileNotFoundException
	 */
	public static String saveCompressImage(String filePath,int quality) {

		Bitmap bm = getSmallBitmap(filePath);
		if (bm == null) {
			return null;
		}
		int degree = readPictureDegree(filePath);
		if (degree != 0) {// 旋转照片角度
			bm = rotateBitmap(bm, degree);
		}

		File outputFile = new File(filePath);

		try {
			FileOutputStream out = new FileOutputStream(outputFile);
			if (bm.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outputFile.getPath();
	}

	/**
	 * 计算图片旋转角度
	 * @param path
	 * @return
	 */
	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * 旋转图片
	 * @param bitmap
	 * @param degress
	 * @return
	 */
	public static Bitmap rotateBitmap(Bitmap bitmap,int degress) {
		if (bitmap != null) {
			int bmpWidth = bitmap.getWidth();
			int bmpHeight = bitmap.getHeight();
			Matrix matrix = new Matrix();
			matrix.postRotate(degress);
			L.e("bmpWidth == "+bmpWidth+" bmpHeight == "+bmpHeight);
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bmpWidth, bmpHeight,
					matrix, true);
			return bitmap;
		}
		return bitmap;
	}

	/**
	 * 重新发送发送失败的图片
	 * @param mChatUserNum
	 * @param mSendingListener
	 */
	public static void reSendFailedImgChatMsgBean(Context mContext,
												  Object mChatMsgEntity, int chatType,
												  String mChatUserNum, SendingListener mSendingListener) {
		Message msg = new Message();
		MessageBodyEntity body = null;
		switch (chatType) {
			case Const.CHAT_TYPE_P2P:
				body = JSON.parseObject(((P2PChatMsgEntity)mChatMsgEntity).getMessage(),MessageBodyEntity.class);
				break;
			case Const.CHAT_TYPE_GROUP:
				body = JSON.parseObject(((GroupChatMsgEntity)mChatMsgEntity).getMessage(),MessageBodyEntity.class);
				break;
			case Const.CHAT_TYPE_DIS:
				body = JSON.parseObject(((GroupChatMsgEntity)mChatMsgEntity).getMessage(),MessageBodyEntity.class);
				break;
			default:
				break;
		}
		if (body != null && body.getContent() != null) {
			String fileId = null;
			int msgType = 0;
			//图片
			if (body.getImages().size() > 0) {
				msgType = P2PChatMsgEntity.IMAGE;
				fileId = body.getImages().get(0).getFileId();
			} else if (body.getFiles().size() > 0) {
				//文件
				msgType = P2PChatMsgEntity.FILE;
				fileId = body.getFiles().get(0).getFileId();
			} else if (body.getVoice() != null)
			{
				//语音
				msgType = P2PChatMsgEntity.AUDIO_FILE;
				fileId = body.getVoice().getId();
			}

			//说明图片没有上传成功,需启动线程重新上传
			if (fileId == null || fileId.isEmpty() ||
					//因为发送语音消息的时候，上传之前就已经生成了文件ID，所有这里不能通过服务器返回ID来判断是否发送成功
					//只能用如下的方法判断
					(msgType == P2PChatMsgEntity.AUDIO_FILE && !body.getVoice().isSentSuccess())) {

				String reSendPacketId;
				if (chatType == Const.CHAT_TYPE_P2P) {
					reSendPacketId = ((P2PChatMsgEntity) mChatMsgEntity).getPacketId();
				} else {
					reSendPacketId = ((GroupChatMsgEntity) mChatMsgEntity).getPacketId();
				}

				switch (msgType) {
					case P2PChatMsgEntity.IMAGE:
						// 发送图片
						ArrayList<ImageFile> imageFileList = new ArrayList<ImageFile>();
						ImageFile reSendImageFile = new ImageFile(body.getImages().get(0).getSdcardPath());
						imageFileList.add(reSendImageFile);

						// 启动发送图片的AsyncTask
						SendImageTask sendImageTask = new SendImageTask(XmppConnectionManager.getInstance(),chatType,mChatUserNum);
						sendImageTask.setSendImageListener(mSendingListener);
						sendImageTask.setResendChatBeanPacketId(reSendPacketId);
						sendImageTask.executeOnExecutor(FileDownLoader.getInstance().getThreadPool(), imageFileList);
						break;
					case P2PChatMsgEntity.FILE:
						HashSet<File> selectedFiles = new HashSet<File>();
						File file = new File(body.getFiles().get(0).getSdcardPath());
						selectedFiles.add(file);

						SendFileTask sendFileTask = new SendFileTask(XmppConnectionManager.getInstance(), chatType, mChatUserNum);
						sendFileTask.setSendFileListener(mSendingListener);
						sendFileTask.setResendChatBeanPacketId(reSendPacketId);
						sendFileTask.executeOnExecutor(FileDownLoader.getInstance().getThreadPool(), selectedFiles);
						break;
					case P2PChatMsgEntity.AUDIO_FILE:
						Set<File> files = new HashSet<>();
						files.add(FileUtil.getFileByName(fileId));

						SendVoiceTask sendVoiceTask = new SendVoiceTask(XmppConnectionManager.getInstance(),chatType,mChatUserNum);
						sendVoiceTask.setResendChatBeanPacketId(reSendPacketId);
						AsyncTaskCompat.executeParallel(sendVoiceTask,files);
						break;
					default:
						break;
				}
			} else {
				//图片已经上传成功，只需重新发送message消息即可
				switch (chatType) {
					case Const.CHAT_TYPE_P2P:
						msg.setBody(((P2PChatMsgEntity) mChatMsgEntity).getMessage());
						msg.setType(Type.chat);
						msg.setTo(JIDUtil.getJIDByAccount(mChatUserNum));
						msg.setPacketID(((P2PChatMsgEntity) mChatMsgEntity).getPacketId());

						P2PChatMsgDao mP2PChatMsgDao = new P2PChatMsgDao(mContext);
						mP2PChatMsgDao.saveOrUpdate((P2PChatMsgEntity) mChatMsgEntity);
						break;
					case Const.CHAT_TYPE_GROUP:
						msg.setBody(((GroupChatMsgEntity) mChatMsgEntity).getMessage());
						msg.setType(Type.groupchat);
						msg.setTo(JIDUtil.getGroupJIDByAccount(mChatUserNum));
						msg.setPacketID(((GroupChatMsgEntity) mChatMsgEntity).getPacketId());

						GroupChatDao mGroupChatDao = new GroupChatDao(mContext);
						mGroupChatDao.saveOrUpdate((GroupChatMsgEntity) mChatMsgEntity);
						break;
					case Const.CHAT_TYPE_DIS:
						msg.setBody(((GroupChatMsgEntity) mChatMsgEntity).getMessage());
						msg.setType(Type.groupchat);
						msg.setTo(JIDUtil.getGroupJIDByAccount(mChatUserNum));
						msg.setPacketID(((GroupChatMsgEntity) mChatMsgEntity).getPacketId());

						DisGroupChatDao mDisGroupChatDao = new DisGroupChatDao(mContext);
						mDisGroupChatDao.saveOrUpdate((GroupChatMsgEntity) mChatMsgEntity);
						break;
					default:
						break;
				}
				msg.setFrom(JIDUtil.getJIDByAccount(AppController.getInstance().mSelfUser.getUserNo()));
				XmppConnectionManager.getInstance().sendPacket(msg);
			}
		}
	}


	/**
	 * 将彩色图转换为纯黑白二色
	 *
	 * @return 返回转换好的位图
	 */
	public static Bitmap convertToBlackWhite(Bitmap bmp) {
		int width = bmp.getWidth(); // 获取位图的宽
		int height = bmp.getHeight(); // 获取位图的高
		int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组

		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		int alpha = 0xFF << 24;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int grey = pixels[width * i + j];

				// 分离三原色
				int red = ((grey & 0x00FF0000) >> 16);
				int green = ((grey & 0x0000FF00) >> 8);
				int blue = (grey & 0x000000FF);

				// 转化成灰度像素
				grey = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
				grey = alpha | (grey << 16) | (grey << 8) | grey;
				pixels[width * i + j] = grey;
			}
		}

		// 新建图片
		Bitmap newBmp = Bitmap.createBitmap(width, height, Config.RGB_565);
		// 设置图片数据
		newBmp.setPixels(pixels, 0, width, 0, 0, width, height);

		Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(newBmp, 380, 460);

		return resizeBmp;
	}

}
