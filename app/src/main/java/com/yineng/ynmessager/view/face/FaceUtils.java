package com.yineng.ynmessager.view.face;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class FaceUtils {
	/**
	 * 读取表情配置文件
	 * 
	 * @param context
	 * @return
	 */
	public static List<String> getEmojiFile(Context context) {
		try {
			List<String> list = new ArrayList<String>();

			String[] faces = context.getAssets().list("face/gif");
			//将Assets中的表情名称转为字符串一一添加进list  去除之前的81个gif图不添加进去
			for (int i = 3; i < faces.length-77; i++) {
				list.add(faces[i]);
			}
			list.remove("11.gif");
			return list;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


//	/**
//	 * 读取表情配置文件
//	 * 
//	 * @param context
//	 * @return
//	 */
//	public static List<String> getEmojiFile(Context context) {
//		List<String> list = new ArrayList<String>();
//		for (int i = 0; i <= 80; i++) {
//			list.add(i+"");
//		}
//		return list;
//	}
}
