package com.yineng.ynmessager.util.address;

import android.content.Context;
import android.util.Log;

import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.TextUtil;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * V8地址解析类，解析注解
 *
 * @author yhu
 *         注解地址
 *         {@link V8AddressAnnotation}
 */
public class V8ContextAddress {

    public static V8ContextAddress v8Address = null;
    public static final String mTag = "V8ContextAddress";

    private V8ContextAddress(Context context, Class<?> clazz) throws MalformedURLException {
        check(clazz, context);
    }

    public synchronized static V8ContextAddress bind(Context context, Class<?> clazz) throws MalformedURLException {
        if (v8Address == null) {
            synchronized (V8ContextAddress.class) {
                if (v8Address == null) {
                    v8Address = new V8ContextAddress(context, clazz);
                }
            }
        }
        return v8Address;
    }

    private void check(Class<?> clazz, Context context) throws MalformedURLException {
        Class<?> obj = clazz;
        //获取v8地址,去除最后一个"/"还有空格这些东西
        String v8Url = TextUtil.replaceSlash((TextUtil.replaceBlank(LastLoginUserSP.getInstance(context).getUserServicesAddress())));
        Field lastHost = null;
        // 上一次的数据
        String lastValue = "";
        try {
            lastHost = obj.getField("HOST");
            lastValue = lastHost.get(obj).toString();
        } catch (NoSuchFieldException | SecurityException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        List<Field> list = Arrays.asList(obj.getDeclaredFields());
        for (int i = 0; i < list.size(); i++) {
            Field field = list.get(i);
            // 是否使用V8Address注解
            if (field.isAnnotationPresent(V8AddressAnnotation.class)) {
                // 获得所有的注解
                for (Annotation anno : field.getDeclaredAnnotations()) {
                    try {
                        V8AddressAnnotation.ContextType contextType = ((V8AddressAnnotation) anno).v8ContextType();
                        String resultUrl = "";
                        String host;
                        //判断是否包含https
                        if (v8Url.contains("https")) {
                            host = "https://";
                        } else {
                            host = "http://";
                        }
                        //取出URL中的host
                        host += new URL(v8Url).getAuthority();
                        //拼接
                        switch (contextType) {
                            case YNEDUT:
                                //YNEDUT表示用户输入平台地址的二级域名
                                resultUrl = host + v8Url.replaceAll(host, "");
                                break;
                            case WR:
                                resultUrl = host + "/wr";
                                break;
                            case SMESIS:
                                resultUrl = host + "/smesis";
                                break;
                            case BPMX:
                                resultUrl = host + "/bpmx";
                                break;
                        }
                        //获取当前静态变量的值
                        String value = field.get(obj).toString();
                        //判断上次的host是否为空，如果为空表示第一次运行，可以直接赋值
                        if (StringUtils.isEmpty(lastValue)) {
                            resultUrl += value;
                        } else {
                            switch (contextType) {
                                case WR:
                                    String wrUrl = value.substring(value.lastIndexOf("/wr"), value.length()).replace("/wr", "");
                                    resultUrl += wrUrl;
                                    break;
                                case SMESIS:
                                    String smesisUrl = value.substring(value.lastIndexOf("/smesis"), value.length()).replace("/smesis", "");
                                    resultUrl += smesisUrl;
                                    break;
                                case BPMX:
                                    String bpmxUrl = value.substring(value.lastIndexOf("/bpmx"), value.length()).replace("/bpmx", "");
                                    resultUrl += bpmxUrl;
                                    break;
                                default:
                                    //YNEDUT
                                    //如果不为空表示已经赋值过，这去除初始值，并且替换
                                    // 请求地址
                                    String requestUrl = value.replace(lastValue, "");
                                    resultUrl += requestUrl;
                                    break;
                            }
                        }
                        field.set(obj, "");
                        // 设置数值
                        field.set(obj, resultUrl);
                        Log.i(mTag, "赋值：" + field.get(obj).toString());
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        try {
            lastHost.set(obj, v8Url);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
