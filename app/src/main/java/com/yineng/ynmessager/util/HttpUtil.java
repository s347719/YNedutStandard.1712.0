package com.yineng.ynmessager.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Map;


/**
 * @author YuTang
 */
public class HttpUtil {
	private static final String TAG = "HttpUtil";
	private static final String RESPONSE_ERROR = "{'response' : 'error','msg' : {'code' : 500,'msg' : '数据请求失败'}}";

	/**
	 * @param urlstr
	 *            urlstr,url字串
	 * @param map
	 *            <String, String>params 参数以map形式封装起来
	 * @return 在get方式访问网络时把url和map参数 组合成url?param1=XXX&param2=XXX的字串类型
	 */
	public static String createGetUrl(final String urlstr,
			final Map<String, String> map) {
		StringBuilder sb = new StringBuilder(urlstr);
		int len = 0;
		int mapsize = 0;
		if (map != null && map.size() > 0) {
			sb.append("?");
			mapsize = map.size();
			for (String key : map.keySet()) {
				try {
					len++;
					sb.append(key);
					sb.append("=");
					sb.append(URLEncoder.encode(map.get(key).trim(), "utf-8"));
					if (len < mapsize) {
						sb.append("&");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		Log.v(TAG, "createGetUrl:url-->" + sb.toString());
		return sb.toString().trim();
	}

	/**
	 * @param context
	 *            如果isdefparam = false，context 可以为 null
	 * @param url
	 *            ,url字串
	 * @param params
	 *            参数以map形式封装起来
	 * @param isdefparam
	 *            是否设置默认参数
	 * @return 通过url用HttpURLConnection 以 get短连接方式向服务端请求数据，返回json Stirng 包括异常的
	 *         json Stirng
	 * @throws IOException
	 */
	public static String getHttpURLConnRequest(Context context,
			final String url, final Map<String, String> params,
			boolean isdefparam) throws IOException {
		return getHttpURLConnRequestWithTimeout(context, url, params,
				isdefparam, 60000);
	}

	/**
	 * @param context
	 * @return Map<String,String> 接口 得到默认的参数对象接口
	 */
	public static Map<String, String> getDefaultParams(Context context) {

		Map<String, String> params = new Hashtable<String, String>();

		return params;
	}

	/**
	 * @param context
	 *            如果isdefparam = false，context 可以为 null
	 * @param url
	 *            ,url字串
	 * @param params
	 *            参数以map形式封装起来
	 * @param isdefparam
	 *            是否设置默认参数
	 * @param timeoutmillis
	 *            连接超时时间
	 * @return 通过url用HttpURLConnection 以 get短连接方式向服务端请求数据，返回json Stirng 包括异常的
	 *         json Stirng
	 * @throws IOException
	 */
	public static String getHttpURLConnRequestWithTimeout(Context context,
			final String url, final Map<String, String> params,
			boolean isdefparam, int timeoutmillis) {
		final String urlstr = createGetUrl(url, params);
		StringBuffer strbu = new StringBuffer();
		try {
			URL postUrl = new URL(urlstr);
			HttpURLConnection connection = (HttpURLConnection) postUrl
					.openConnection();
			// connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(timeoutmillis);// 连接超时间为1分钟
			connection.setReadTimeout(5 * 1000);// 访问连接超时间为1分钟
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			if (isdefparam && context != null) {// 设置默认参数在http header里面
				Map<String, String> map = getDefaultParams(context);
				for (String key : map.keySet()) {
					connection.setRequestProperty(key, map.get(key));
				}

			}
			connection.connect();
			Log.v(TAG,
					"getHttpURLConnRequestWithTimeout-->"
							+ connection.getURL());
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));

			String line = null;
			while ((line = reader.readLine()) != null) {
				strbu.append(line);// 读取服务端的数据流
			}

			reader.close();
			connection.disconnect();
		} catch (MalformedURLException e) {
			throw new Error(e);

		} catch (ProtocolException e) {
			throw new Error(e);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return strbu.toString();
	}


	/**
	 * 截取url最后一个斜杠
	 * @param url
	 * @return
	 */
	public static String  getServiceUrl(String url){
		String result = "";
		if(url.substring(url.length()-1,url.length()).equals("/")){
			result=url.substring(0,url.length()-1);
		}else{
			result = url;
		}
		return result;
	}
}
