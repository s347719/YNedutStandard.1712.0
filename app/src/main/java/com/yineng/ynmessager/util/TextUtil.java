//***************************************************************
//*    2015-6-19  上午10:56:41
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.util;

import android.text.TextUtils;

import com.yineng.ynmessager.bean.app.DownLoadFile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 贺毅柳
 */
public class TextUtil {


    /**
     * 用于去掉字符串结尾的空白字符
     *
     * @param str 你的字符串；如果str为null或者长度为0，则直接返回原str对象
     * @return 去掉str结尾所有空白字符后的结果
     */
    public static CharSequence trimEnd(CharSequence str) {
        // 如果为null或者长度为0的话，直接返回原CharSequence
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        // 取最后一个字符
        char end = str.charAt(str.length() - 1);
        // 判断最后一个字符是否为空白
        if (TextUtils.isEmpty(String.valueOf(end).trim())) {
            // 从str中去掉最后一个字符，进行递归
            str = str.subSequence(0, str.length() - 1);
            return trimEnd(str);
        } else {
            // 返回现在的str
            return str;
        }
    }

    /**
     * 根据文件名字判断文件类型
     *
     * @param name
     * @return
     */
    public static int matchTheFileType(String name) {

        if (TextUtils.isEmpty(name)) {
            return DownLoadFile.FILETYPE_OTHER;
        }
        String end = name.substring(name.lastIndexOf(".") + 1, name.length());
        if (PattenUtil.imageCheck(end)) {
            return DownLoadFile.FILETYPE_IMG;
        } else if (PattenUtil.movieCheck(end)) {
            return DownLoadFile.FILETYPE_MOIVE;
        } else if (PattenUtil.voiceCheck(end)) {
            return DownLoadFile.FILETYPE_VOICE;
        } else if (PattenUtil.docCheck(end)) {
            return DownLoadFile.FILETYPE_DOC;
        } else if (PattenUtil.excelCheck(end)) {
            return DownLoadFile.FILETYPE_EXCEL;
        } else if (PattenUtil.pdfCheck(end)) {
            return DownLoadFile.FILETYPE_PDF;
        } else if (PattenUtil.pptCheck(end)) {
            return DownLoadFile.FILETYPE_PPT;
        } else if (PattenUtil.zipCheck(end)) {
            return DownLoadFile.FILETYPE_ZIP;
        } else {
            return DownLoadFile.FILETYPE_OTHER;
        }
    }

    /**
     * 去除字符串中的空格、回车、换行符、制表符
     *
     * @param str
     * @return
     */
    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    /**
     * 去除url里面最后一个斜杠
     * @param resultUrl
     * @return
     */
    public static String replaceSlash(String resultUrl) {
        if (resultUrl.length()>0) {
            // 如果最后一个符号是“/”就要去除
            if (resultUrl.substring(resultUrl.length() - 1, resultUrl.length()).equals("/")) {
                resultUrl = resultUrl.substring(0, resultUrl.length() - 1);
            } else {
                return resultUrl;
            }
            return replaceSlash(resultUrl);
        }else {
            return "";
        }
    }
}
