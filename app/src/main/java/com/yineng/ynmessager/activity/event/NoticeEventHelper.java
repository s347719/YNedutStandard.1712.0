//***************************************************************
//*    2015-8-18  上午10:42:52
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.event;

import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import com.yineng.ynmessager.bean.event.NoticeEvent;
import com.yineng.ynmessager.util.L;

import org.apache.commons.lang3.StringUtils;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * @author 贺毅柳
 * 
 */
public class NoticeEventHelper
{
	public static final String TAG = "EventHelper";

	/**
	 * 根据一条Message消息对象得到一个Event事件对象
	 * @param message Message对象
	 * @return 返回null表示解析有问题，该Message表示的不是一个事件
	 */
	public static NoticeEvent from(Message message)
	{
		if(message == null) return null;

		NoticeEvent event = null;
		String body = message.getBody();
		JSONObject jsonBody;
		try
		{
			jsonBody = new JSONObject(body);

			event = new NoticeEvent();
			event.setPacketId(message.getPacketID());
			if (jsonBody.has("sender")) {
				event.setUserNo(jsonBody.getString("sender"));
			}
			event.setUserName("sendName");
			if (jsonBody.has("recevier")) {
				event.setReceiver(jsonBody.getString("recevier"));
			}
			event.setTitle(message.getSubject());
			event.setMessage(jsonBody.getString("content"));
			event.setContent(Html.fromHtml(event.getMessage()).toString());
			if (jsonBody.has("sourceTerminal")) {
				event.setSourceTerminal(jsonBody.getInt("sourceTerminal"));
			}
			event.setMsgType(jsonBody.getString("msgType"));
			//平台通知业务流程修改，huyi 2016.1.11
			String msgId = jsonBody.optString("msgId");
			if (!TextUtils.isEmpty(msgId)) event.setMsgId(msgId);
			String hasAttachment = jsonBody.optString("hasAttachment");
			if (!TextUtils.isEmpty(hasAttachment)) event.setHasAttachment(hasAttachment);
			String hasPic = jsonBody.optString("hasPic");
			if (!TextUtils.isEmpty(hasPic)) event.setHasPic(hasPic);
			String messageType = jsonBody.optString("messageType");
			if (!TextUtils.isEmpty(messageType)) event.setMessageType(messageType);
			convertContent(event);
			String url = jsonBody.optString("url");
			if (!TextUtils.isEmpty(url)) {
				try {
					url = URLEncoder.encode(url, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				event.setUrl(url);
			}//end
		}catch(JSONException e)
		{
			L.e(TAG,e.getMessage(),e);
			return null;
		}

		return event;
	}
	
	
	public static Spanned toHtmlSpan(String text)
	{
		StringBuilder builder = new StringBuilder(text);
		Matcher matcher = android.util.Patterns.WEB_URL.matcher(builder);
		// 先把匹配到的URL，没有http://开头的给补上开头，否则点击超链接没有程序能识别，会报错
		while(matcher.find())
		{
			int start = matcher.start();
			String url = matcher.group();
			if(!url.toLowerCase(Locale.getDefault()).startsWith("http://"))
			{
				builder.insert(start,"http://");
				matcher.reset(builder);
			}
		}
		matcher.reset();

		// 然后把普通的文本URL（不是<a href></a>超链接格式）的URL包裹在<a>标签里
		int findIndex = 0;
		while(matcher.find(findIndex))
		{
			String url = matcher.group();
			int start = matcher.start();
			int end = matcher.end();
			int hrefIndex = (start - 6) < 0 ? 0 : (start - 6);
			String href = builder.substring(hrefIndex,hrefIndex + 5); // 取href标志来判断是否是在<a>标签中
			if(!"href=".equalsIgnoreCase(href))
			{
				String newUrl = "<a href='" + url + "'>" + url.replace("http://","") + "</a>";
				builder.replace(start,end,newUrl);
				matcher.reset(builder);
				findIndex = start + newUrl.length(); // 下次匹配从被修改URL的末尾后开始
			}else
			{
				findIndex = end; // 下次匹配从当前结果的末尾开始
			}
		}
		// 替换所有的换行符为<br />
		String result = builder.toString().replace("\n","<br />");
		return Html.fromHtml(result); // 终于完了
	}
	
	public static void convertContent(@NonNull NoticeEvent event)
	{

        String msg = event.getMessage();

        Document doc = Jsoup.parse(msg);
        Element element = doc.select("a[href]").first();
        if(element == null)
        {
            event.setUrl(StringUtils.EMPTY);
        }else
        {
            String link = element.attr("href");
            try
            {
                link = URLEncoder.encode(link,"utf-8");
            } catch (UnsupportedEncodingException e)
            {
                L.e(TAG,e.getMessage(),e);
            }

            event.setUrl(link);
            event.setMessage(doc.text());
        }

	}

}
