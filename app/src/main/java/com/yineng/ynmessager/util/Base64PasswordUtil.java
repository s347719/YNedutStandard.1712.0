package com.yineng.ynmessager.util;


import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

/**
 * Created by yn on 2017/9/28.
 */

public class Base64PasswordUtil {
    private static final int INDEX_NAME = 3;

    private static final String SPECIC_NAME = "d5a6J5oqaQO+8ge+8iCbv";


    private static final String UTF_8 = "UTF-8";


    /**
     * 编码密码
     *
     * @param data
     * @return
     */
    public static String encode(String data) {
        String result = encodeData(data);
        StringBuffer sb = new StringBuffer(result);
        sb.insert(INDEX_NAME, SPECIC_NAME);
        return sb.toString();
    }

    /**
     * 解密密码
     *
     * @param data
     * @return
     */
    public static String decode(String data) {
        String base64Password = decodeData(data);
        StringBuffer sb = new StringBuffer(base64Password);
        sb.delete(INDEX_NAME, SPECIC_NAME.length() + INDEX_NAME);
        return sb.toString();
    }

    /**
     * 对给定的字符串进行base64解码操作
     */
    public static String decodeData(String inputData) {
        try {
            if (null == inputData) {
                return null;
            }
            return new String(Base64.decodeBase64(inputData.getBytes(UTF_8)), UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 对给定的字符串进行base64加密操作
     */
    public static String encodeData(String inputData) {
        try {
            if (null == inputData) {
                return null;
            }
            return new String(Base64.encodeBase64(inputData.getBytes(UTF_8)), UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

}
