package com.yineng.ynmessager.activity.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;
import android.view.LayoutInflater;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.yineng.ynmessager.activity.app.evaluate.EvaluatePlanActivity;
import com.yineng.ynmessager.activity.app.internship.InternshipListActivity;
import com.yineng.ynmessager.activity.event.EventActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.app.Dealt;
import com.yineng.ynmessager.bean.app.NewAppContent;
import com.yineng.ynmessager.bean.app.NewMyApps;
import com.yineng.ynmessager.bean.app.Publicize;
import com.yineng.ynmessager.bean.app.Submenu;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.login.LoginUser;
import com.yineng.ynmessager.db.dao.DealtDao;
import com.yineng.ynmessager.db.dao.LoginUserDao;
import com.yineng.ynmessager.db.dao.NewMyAppsDao;
import com.yineng.ynmessager.db.dao.PublicizeDao;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.CryptUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.NetWorkUtil;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.util.V8TokenManager;
import com.yineng.ynmessager.util.address.URLs;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by Yhu on 2017/7/19.
 *
 * @author yhu
 *         通过api查询app
 *         以及APP跳转的工具类
 *         排序规则：
 *         1.第一次进入app时查询数据库，如果数据库为空则添加服务器过来的推荐应用，并且拼接未推荐应用，总数量小于规定数量
 *         并且第一次insert时最后修改时间是我手机本地时间，这样本地应用最后修改时间是大于服务器最后修改时间的
 *         2.判断用户是否勾选同步服务器选项：
 *         1).未同步服务器：
 *         未同步服务器刷新时，保留原有的排序，只做新增以及名称等信息的一些修改。主要保留了用户自己的排序设置。
 *         2).同步服务器：
 *         一、更新、新增；同步服务器的情况，首先update数据库，但是保留本地的排序号。如果本地没有的应用则新增。
 *         二、提取；把服务器中的推荐应用加入到一个集合中(mergeWeb)，再把本地推荐应用加入到另一个集合中(mergeLocal)
 *         三、合并:
 *         1.拼接；创建一个集合resultApps,通过top_seq排序把mergeWeb中的应用重新排序后加入到resultApps。
 *         本地应用mergeLocal同理，但是排序号要跟在mergeWeb后面加入到resultApps中。
 *         比如，服务器过来: A、B、C 本地: C、A、B、D、E 合并后的结果: A、B、C、C、A、B、D、E 排序号从1开始依次递增。
 *         2.去重；判断服务器和本地时间，如果哪个较新就保留哪个。比如，服务器过来: A、B、C ；本地: C、A、B、D 本地的C如果比
 *         服务器的时间新，就删除服务器的C，本地的A、B也要新于服务器时间。结果:C、A、B、D （不管怎么样，遵循保留新的规则）
 *         3.显示、隐藏；如果在我本地删除了一个服务器推荐应用，依旧要与服务器对比最后修改时间。如果服务器推荐应用时间较新，依旧显示
 *         出这个用户已删除的应用，反之继续隐藏。比如，服务器推荐:A、B、C ；本地：A、C  用户在本地删除了B 并且服务器没有对
 *         B做出修改，所以本地的B 时间新所以要保留用户设置，结果：A、C 反之亦然。
 *         4.更新时间；所有应用的最后时间保留本地最新时间。
 *         5.重新排序；将集合resultApps 按照集合里的顺序依次重新排序。
 *         3.删除；如果服务器过来的应用没有，而本地有就删除。
 */

public class CheckMyApps {

    private static final String TAG = "CheckMyApps";

    private Context context;
    /**
     * Application
     */
    private final AppController mApplication = AppController.getInstance();

    String token = "";
    private User user;
    private LayoutInflater mLayoutInflater = null;

    private CheckMyApps(Context context) {
        this.context = context;
        user = new User();
        SharedPreferences userInfo = context.getSharedPreferences("userPost", 0);
        user.setUserNo(userInfo.getString("userNo", ""));
        user.setUserName(LastLoginUserSP.getInstance(context).getUserName());
        user.setUserType(LastLoginUserSP.getInstance(context).getUserType());
        token = V8TokenManager.obtain();
        mLayoutInflater = LayoutInflater.from(context);
    }

    public static CheckMyApps checkMyApps = null;

    public synchronized static CheckMyApps getInstance(Context context) {
        if (checkMyApps == null) {
            synchronized (CheckMyApps.class) {
                if (checkMyApps == null) {
                    checkMyApps = new CheckMyApps(context);
                }
            }
        }
        return checkMyApps;
    }

    /**
     * app更新完成回调
     */
    interface OnCheckFinishListener {
        /**
         * app检测完成回调
         *
         * @param state   是否成功 1：成功 0：失败
         * @param isFirst 是否第一次登录 true 是；false 否
         */
        void checkMyAppFinishListener(int state, boolean isFirst);

        /**
         * notify检测完成回调
         *
         * @param state 是否成功 1：成功 0：失败
         */
        void checkNotifyFinishListener(int state);

        /**
         * dealt检测完成回调
         *
         * @param state 是否成功 1：成功 0：失败
         */
        void checkDealtFinishListener(int state);
    }

    /**
     * 判断smesis id是否为空
     */
    public interface OnLoginStateListener {
        /**
         * 异常账号
         *
         * @param isLoginOut 是否退出当前账号
         */
        void abnormalAccount(boolean isLoginOut);
    }

    private OnLoginStateListener onLoginStateListener;


    private OnCheckFinishListener onCheckFinishListener;

    public void setOnCheckFinishListener(OnCheckFinishListener onCheckFinishListener) {
        this.onCheckFinishListener = onCheckFinishListener;
    }

    public void setOnLoginStateListener(OnLoginStateListener onLoginStateListener) {
        this.onLoginStateListener = onLoginStateListener;
    }

    public static CheckMyApps newInstance(Context context) {
        CheckMyApps fragment = new CheckMyApps(context);
        return fragment;
    }

    /**
     * 检测推广文字信息
     *
     * @param dao
     */
    public void checkNotifyApi(final PublicizeDao dao) {
        String url = URLs.POPULARIZE_PICTURE_PUSH;
        Map<String, String> params = new HashMap<>();
        String v8Version = "V1.0";
        String number = "M0003";
        int total = 6;
        token = V8TokenManager.obtain();
        params.put("version", v8Version);
        params.put("access_token", token);
        params.put("number", number);
        params.put("personStart", user.getUserType() + "");
        params.put("total", total + "");
        Log.i("yhu", "获取推广文字信息:" + url + "?" + params.toString());
        OkHttpUtils.get()
                .url(url)
                .tag(this)
                .params(params)
                .build()
                .execute(new StringCallback() {

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        Log.i("yhu", "获取推广文字信息失败：" + e.getMessage());
                        if (onCheckFinishListener != null) {
                            onCheckFinishListener.checkNotifyFinishListener(0);
                        }
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        NewAppContent<Publicize> appContent = getResult(response, Publicize.class);
                        if (appContent == null) {
                            if (onCheckFinishListener != null) {
                                onCheckFinishListener.checkNotifyFinishListener(0);
                            }
                            return;
                        }
                        List<Publicize> webPubs = appContent.getResult();
                        LinkedList<Publicize> localPubs = dao.queryOrderBy();

                        if (localPubs.size() <= 0) {
                            for (Publicize publicize : webPubs) {
                                //设置未读
                                publicize.setIsRead(0);
                                dao.insert(publicize);
                            }
                        }

                        //修改,如果相同的条目，是否已读保留本地
                        for (Publicize webPub : webPubs) {
                            for (Publicize localPub : localPubs) {
                                if (webPub.equals(localPub)) {
                                    webPub.setIsRead(localPub.getIsRead());
                                }
                            }
                            dao.update(webPub);
                        }

                        //新的条目，增加
                        for (Publicize publicize : getNewItem(localPubs, webPubs)) {
                            publicize.setIsRead(0);
                            dao.insert(publicize);
                        }

                        //没有的条目，删除
                        for (Publicize publicize : getDeleteItem(localPubs, webPubs)) {
                            dao.delete(publicize);
                        }

                        if (onCheckFinishListener != null) {
                            onCheckFinishListener.checkNotifyFinishListener(1);
                        }
                    }
                });
    }

    /**
     * 检测待办
     *
     * @param dao
     */
    public void checkDealtApi(final DealtDao dao) {
        String url = URLs.GET_USER_DEALT;
        String version = "V1.0";
        token = V8TokenManager.obtain();
        Map<String, String> params = new HashMap<>();
        params.put("version", version);
        params.put("access_token", token);
        params.put("userId", user.getUserNo());
        params.put("userType", user.getUserType() + "");

        Log.i("yhu", "获取待办：" + url + "?" + params.toString());
        OkHttpUtils.get()
                .url(url)
                .tag(this)
                .params(params)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e("yhu", "获取待办列表失败：" + e.getMessage());
                        if (onCheckFinishListener != null) {
                            onCheckFinishListener.checkDealtFinishListener(0);
                        }
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        NewAppContent<Dealt> appContent = getResult(response, Dealt.class);
                        if (appContent == null) {
                            if (onCheckFinishListener != null) {
                                onCheckFinishListener.checkDealtFinishListener(0);
                            }
                            return;
                        }
                        List<Dealt> dealts = appContent.getResult();
                        LinkedList<Dealt> localDealts = dao.queryOrderBySequence();
                        if (localDealts == null) {
                            if (onCheckFinishListener != null) {
                                onCheckFinishListener.checkDealtFinishListener(0);
                            }
                            return;
                        }
                        if (localDealts.size() == 0) {
                            dao.insert(dealts);
                        }

                        //更新待办，每次都更新，没有用户设置
                        dao.update(dealts);
                        //获取新增的待办
                        for (Dealt newDealt : getNewItem(localDealts, dealts)) {
                            dao.insert(newDealt);
                        }

                        //获取删除的待办
                        for (Dealt deleteDealt : getDeleteItem(localDealts, dealts)) {
                            dao.delete(deleteDealt);
                        }

                        if (onCheckFinishListener != null) {
                            onCheckFinishListener.checkDealtFinishListener(1);
                        }
                    }
                });
    }

    /**
     * 检测是否有新的app
     *
     * @param dao
     * @param type 1：根据服务器排序，0：根据用户自己排序
     */
    public void checkAppsApi(final NewMyAppsDao dao, final int type) {
        String url = URLs.GET_USER_APPS;
        String version = "V2.0";
        Map<String, String> params = new HashMap<>();
        token = V8TokenManager.obtain();
        params.put("version", version);
        params.put("access_token", token);
        params.put("userId", user.getUserNo());
        params.put("userType", user.getUserType() + "");

        Log.i("yhu", "获取app列表Api:" + url + "?" + params.toString());
        OkHttpUtils.get()
                .url(url)
                .tag(this)
                .params(params)
                .build()
                .execute(new StringCallback() {
                    //判断是否第一次加载
                    boolean isFirst = false;

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.i("yhu", "获取App列表错误信息:" + e.getMessage());
                        if (onCheckFinishListener != null) {
                            onCheckFinishListener.checkMyAppFinishListener(0, isFirst);
                        }
                    }

                    @Override
                    public void onResponse(String jsonStr, int id) {
                        List<NewMyApps> apps = parseJSON(jsonStr);
                        if (apps == null) {
                            if (onCheckFinishListener != null) {
                                onCheckFinishListener.checkMyAppFinishListener(0, false);
                            }
                            return;
                        }
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        LinkedList<NewMyApps> oldApps = dao.queryOrderByTopSeq();
                        LinkedList<NewMyApps> mergeLocalApps = new LinkedList<>();
                        LinkedList<NewMyApps> mergeWebApps = new LinkedList<>();
                        //通过seq排序
                        Collections.sort(apps, new AppComparatorBySeq());
                        //本地数据空就新增
                        if (oldApps == null || oldApps.size() <= 0) {
                            isFirst = true;
                            LinkedList<NewMyApps> recommendApp = new LinkedList<>();
                            for (NewMyApps app : apps) {
                                app.setUpdateTime(df.format(new Date()));
                                app.setUserDelete(NewMyAppsDao.USER_DELETE_FALSE);
                                app.setIsNew(0);
                                dao.insert(apps);
                            }
                            //取出所有的云端推荐应用
                            int recommendNum = 0;
                            for (NewMyApps app : apps) {
                                if (app.getIsRecommend()) {
                                    recommendApp.add(app);
                                }
                            }
                            //把推荐应用再排序设置排序号
                            Collections.sort(recommendApp, new AppComparatorByTopSeq());
                            for (NewMyApps seqApp : recommendApp) {
                                recommendNum++;
                                seqApp.setTopSequence(recommendNum);
                            }
                            //第一次本地数据是空的，必须显示11个应用在首页，云端推荐+云端非推荐拼成11个
                            int addNum = recommendNum;
                            for (NewMyApps app : apps) {
                                if (!app.getIsRecommend() && addNum <= NewAppFragment.MAX_RECOMMEND_COUNT) {
                                    addNum++;
                                    app.setIsRecommend(true);
                                    app.setUpdateTime(df.format(new Date()));
                                    app.setTopSequence(addNum);
                                    app.setUserDelete(NewMyAppsDao.USER_DELETE_FALSE);
                                    recommendApp.add(app);
                                }
                            }
                            //截取最大数目，保证小于等于15个推荐应用
                            for (int i = 0; i < recommendApp.size(); i++) {
                                if (i >= NewAppFragment.MAX_RECOMMEND_COUNT) {
                                    recommendApp.get(i).setIsRecommend(false);
                                }
                            }
                            dao.update(recommendApp);
                        }
                        switch (type) {
                            //同步服务器
                            case 1:
                                List<NewMyApps> newApps = parseJSON(jsonStr);
                                if (newApps == null) {
                                    if (onCheckFinishListener != null) {
                                        onCheckFinishListener.checkMyAppFinishListener(0, false);
                                    }
                                    return;
                                }
                                //拿出服务器app中推荐应用
                                for (NewMyApps webApp : newApps) {
                                    if (webApp.getIsRecommend()) {
                                        mergeWebApps.add(webApp);
                                    }
                                }
                                //拿出本地app的推荐应用
                                for (NewMyApps localApp : oldApps) {
                                    if (localApp.getIsRecommend()) {
                                        mergeLocalApps.add(localApp);
                                    }
                                }

                                //服务器app对比本地app
                                for (NewMyApps newApp : getNewItem(oldApps, apps)) {
                                    //服务器app不存在就插入一条
                                    mergeWebApps.add(newApp);
                                    newApp.setIsNew(1);
                                    dao.insert(newApp);
                                }
                                Collections.sort(mergeWebApps, new AppComparatorByTopSeq());
                                Collections.sort(mergeLocalApps, new AppComparatorByTopSeq());
                                //去重
                                List<NewMyApps> resultApps = removeDuplicateWithOrder(mergeWebApps, mergeLocalApps, oldApps);
                                for (NewMyApps webApp : apps) {
                                    for (NewMyApps resultApp : resultApps) {
                                        if (webApp.equals(resultApp)) {
                                            webApp.setTopSequence(resultApp.getTopSequence());
                                            webApp.setUpdateTime(resultApp.getUpdateTime());
                                            webApp.setUserDelete(resultApp.getUserDelete());
                                            webApp.setIsRecommend(resultApp.getIsRecommend());
                                        }
                                    }
                                }

                                //保留新应用的设置
                                for (NewMyApps webApp : apps) {
                                    for (NewMyApps oldApp : oldApps) {
                                        if (webApp.equals(oldApp)) {
                                            webApp.setIsNew(oldApp.getIsNew());
                                            webApp.setLastUseDate(oldApp.getLastUseDate());
                                        }
                                    }
                                }

                                Collections.sort(apps, new AppComparatorByTopSeq());
                                //截取app数量，如果大于多少个就设置未非推荐
                                int appCount = 0;
                                for (NewMyApps app : apps) {
                                    if (app.getIsRecommend()) {
                                        appCount++;
                                        if (appCount > NewAppFragment.MAX_RECOMMEND_COUNT) {
                                            app.setIsRecommend(false);
                                        }
                                    }
                                }
                                dao.update(apps);
                                break;
                            //保留用户设置
                            case 0:
                                oldApps = dao.queryOrderBySeq();
                                for (NewMyApps newApp : getNewItem(oldApps, apps)) {
                                    //服务器app不存在就插入一条
                                    //不存在加添加，但是不推荐
                                    newApp.setIsRecommend(false);
                                    newApp.setIsNew(1);
                                    dao.insert(newApp);
                                }
                                //排序保留合并结果，但是其他内容根据web服务器的来更新
                                for (NewMyApps webApp : apps) {
                                    for (NewMyApps localApp : oldApps) {
                                        if (webApp.equals(localApp)) {
                                            webApp.setTopSequence(localApp.getTopSequence());
                                            webApp.setIsRecommend(localApp.getIsRecommend());
                                            webApp.setIsNew(localApp.getIsNew());
                                            webApp.setUpdateTime(localApp.getUpdateTime());
                                            webApp.setLastUseDate(localApp.getLastUseDate());
                                            webApp.setUserDelete(localApp.getUserDelete());
                                        }
                                    }
                                }
                                dao.update(apps);
                                break;
                            default:
                                break;
                        }

                        //不管是否是用户本地设置还是同步服务器，如果服务器中的app存在就delete
                        //本地对比服务器应用
                        for (NewMyApps localApp : getDeleteItem(oldApps, apps)) {
                            dao.delete(localApp.getId());
                        }

                        if (onCheckFinishListener != null) {
                            onCheckFinishListener.checkMyAppFinishListener(1, false);
                        }
                    }

                });
    }

    /**
     * 去重
     *
     * @param webAppReco
     * @param localReco
     */
    private LinkedList<NewMyApps> removeDuplicateWithOrder(LinkedList<NewMyApps> webAppReco, LinkedList<NewMyApps> localReco, LinkedList<NewMyApps> localApps) {
        LinkedList<NewMyApps> resultApps = new LinkedList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int index = 0;
        for (NewMyApps app : webAppReco) {
            index++;
            app.setTopSequence(index);
            resultApps.add(app);
        }
        for (NewMyApps app : localReco) {
            index++;
            app.setTopSequence(index);
            resultApps.add(app);
        }

        //通过seq排序
        Collections.sort(resultApps, new AppComparatorByTopSeq());
        for (NewMyApps webApp : webAppReco) {
            for (NewMyApps localApp : localReco) {
                //如果相等，就判断保留哪个应用
                if (webApp.equals(localApp)) {
                    String webDateStr = webApp.getUpdateTime();
                    String localDateStr = localApp.getUpdateTime();
                    //对比时间前后，如果看哪个是最新的就保留哪个
                    if (TimeUtil.compareDate(localDateStr, webDateStr, sdf) == -1) {
                        //webDate 的时间较新
                        resultApps.remove(resultApps.lastIndexOf(localApp));
                        webApp.setUserDelete(NewMyAppsDao.USER_DELETE_FALSE);
                    } else if (TimeUtil.compareDate(localDateStr, webDateStr, sdf) == 1) {
                        //localDate 的时间较新
                        resultApps.remove(resultApps.indexOf(webApp));
                        localApp.setUserDelete(NewMyAppsDao.USER_DELETE_FALSE);
                    } else {
                        //时间相同
                        resultApps.remove(resultApps.indexOf(webApp));
                        localApp.setUserDelete(NewMyAppsDao.USER_DELETE_FALSE);
                    }
                }
            }
        }

        //用户删除的app需要剔除
        for (NewMyApps oldApp : localApps) {
            for (NewMyApps newApp : resultApps) {
                if (oldApp.equals(newApp)) {
                    String webDateStr = newApp.getUpdateTime();
                    String localDateStr = oldApp.getUpdateTime();
                    //如果是用户已删除的
                    if (oldApp.getUserDelete() == NewMyAppsDao.USER_DELETE_TRUE) {
                        if (TimeUtil.compareDate(localDateStr, webDateStr, sdf) == 1) {
                            //localDate 的时间较新
                            newApp.setIsRecommend(false);
                            newApp.setUserDelete(oldApp.getUserDelete());
                            newApp.setUpdateTime(oldApp.getUpdateTime());
                            newApp.setSequence(oldApp.getSequence());
                        } else {
                            //时间相同或web时间新
                            newApp.setUserDelete(NewMyAppsDao.USER_DELETE_FALSE);
                        }
                        //用户没有选择删除，但是本地没有推荐的应用则应该删除
                    } else if (oldApp.getIsRecommend() == false) {
                        if (TimeUtil.compareDate(localDateStr, webDateStr, sdf) == 1) {
                            //localDate 的时间较新
                            newApp.setIsRecommend(false);
                            newApp.setUserDelete(oldApp.getUserDelete());
                            newApp.setUpdateTime(oldApp.getUpdateTime());
                            newApp.setSequence(oldApp.getSequence());
                        } else {
                            //时间相同或web时间新
                            newApp.setUserDelete(NewMyAppsDao.USER_DELETE_FALSE);
                        }
                    }
                }
            }
        }

        int seq = 0;
        for (NewMyApps app : resultApps) {
            if (app.getIsRecommend()) {
                seq++;
                app.setTopSequence(seq);
            }
            //设置最新本地时间
            app.setUpdateTime(sdf.format(new Date()));
        }
        return resultApps;
    }


    /**
     * 解析json获取app
     *
     * @param jsonStr
     * @return
     */
    private List<NewMyApps> parseJSON(String jsonStr) {
        List<NewMyApps> apps = null;
        try {
            NewAppContent<NewMyApps> appContent = getResult(jsonStr, NewMyApps.class);
            if (appContent!=null && appContent.getResult()!=null) {
                apps = new ArrayList<>(appContent.getResult());
            }else {
                apps= new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return apps;
    }

    /**
     * JSON 通过泛型获取不同对象
     *
     * @param jsonString
     * @param clz
     * @param <T>
     * @return
     */
    @Nullable
    private <T> NewAppContent<T> getResult(String jsonString, Class<T> clz) {
        try {
            NewAppContent<T> page = JSON.parseObject(jsonString,
                    new TypeReference<NewAppContent<T>>() {
                    });
            //泛型类型调用paseObject的时候，使用parseObject可以转换Class，
            // 但是后边传TypeReference或者Type就解析不出泛型类型了，需要再转换一次
            List<T> list = JSONArray.parseArray(page.getResult().toString(), clz);
            page.setResult(list);
            return page;
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 本地对比服务器，获取服务器中没有的条目，也就是要删除的条目
     *
     * @param localList
     * @param webList
     * @param <T>
     * @return
     */
    private <T> LinkedList<T> getDeleteItem(LinkedList<T> localList, List<T> webList) {
        LinkedList<T> curList = new LinkedList<>(localList);
        curList.removeAll(webList);
        return curList;
    }

    /**
     * 服务器对比本地，获取本地没有的条目，也就是新的条目
     *
     * @param localList
     * @param webList
     * @param <T>
     * @return
     */
    private <T> List<T> getNewItem(LinkedList<T> localList, List<T> webList) {
        LinkedList<T> curList = new LinkedList<>(webList);
        curList.removeAll(localList);
        return curList;
    }

    /**
     * 应用跳转工具方法
     *
     * @param app
     */
    public void JumpApp(final NewMyApps app, final boolean initMenu) {
        LoginUserDao loginUserDao = new LoginUserDao(context);
        final LoginUser loginUser = loginUserDao.getLoginUserByUserNo(this.user.getUserNo());
        if (!NetWorkUtil.isNetworkAvailable(context)) {
            ToastUtil.toastAlerMessageiconTop(context, mLayoutInflater, "网络连接异常,请检查网络设置", 500);
        } else {
            @SuppressLint("StaticFieldLeak")
            AsyncTask<Void, Void, String> tokenTask =
                    new AsyncTask<Void, Void, String>() {

                @Override
                protected String doInBackground(Void... voids) {
                    return V8TokenManager.obtain();
                }

                @Override
                protected void onPostExecute(String token) {
                    if (StringUtils.isEmpty(token)) {
                        ToastUtil.toastAlerMessageiconTop(context, mLayoutInflater, "系统繁忙,请稍后重试", 500);
                        return;
                    }
                    Intent intent = new Intent();
                    if (StringUtils.isEmpty(app.getUrl())) {
                        app.setUrl("");
                    }
                    Log.e(TAG + ":yhu", "跳转用户名称:" + user.getUserName());
                    switch (app.getSource()) {
                        //Ynedut应用
                        case NewMyApps.TYPE_YNEDUT_APP:
                            //判断是否原生应用
                            switch (app.getUrl()) {
                                //评教
                                case "native_pingjiao":
                                    intent.setClass(context, EvaluatePlanActivity.class);
                                    intent.putExtra("AppUserType", user.getUserType());
                                    context.startActivity(intent);
                                    break;
                                //顶岗实习
                                case "native_dinggang":
                                    intent.setClass(context, InternshipListActivity.class);
                                    intent.putExtra("AppUserType", user.getUserType());
                                    context.startActivity(intent);
                                    break;
                                //待办
                                case "native_daiban":
                                    intent.setClass(context, EventActivity.class);
                                    context.startActivity(intent);
                                    break;
                                default:
                                    //判断是否是远程h5
                                    //http://2313.ynedut.cn/ynedut/third/auth/forwardPage.htm?version=V1.0&access_token=249b0fb0-c21f-4662-a43c-49e9638e4672&openId=9de81def-6b64-400c-8c3f-530a080e3fad&urlStr=/mobile/tuition/index.html
                                    //[remote]/mobile/tuition/index.html
                                    String remoteFlag = "[remote]";
                                    if (app.getUrl().contains(remoteFlag)) {
                                        String urlStr = app.getUrl().replace("[remote]", "");
                                        StringBuffer sb = new StringBuffer();
                                        sb.append(URLs.YNEDUT_PAY_APP)
                                                .append("?version=").append("V1.0")
                                                .append("&access_token=").append(token)
                                                .append("&openId=").append(user.getUserNo())
                                                .append("&urlStr=").append(urlStr);
                                        loadWebUrl(sb.toString(), app.getName(), app.getId(), false, false);
                                        Log.i("yhu", "支付跳转地址:" + sb.toString());
                                    } else {
                                        //cordova应用
                                        openCordovaUrl(app, initMenu, NewMyApps.TYPE_YNEDUT_APP, token);
                                    }
                                    break;
                            }
                            break;
                        //Smesis应用
                        case NewMyApps.TYPE_SMESIS_APP:
                            //cordova应用
                            openCordovaUrl(app, initMenu, NewMyApps.TYPE_SMESIS_APP, token);
                            break;
                        //第三方应用
                        case NewMyApps.TYPE_THIRD_APP:
                            String thirdUrl = URLs.YNEDUT_THIRD_APP + "?id=" + app.getId() + "&client=1" + "&userId=" + user.getUserNo() + "&access_token=" + mApplication.mAppTokenStr;
                            L.e("MSG", "当前URL：" + thirdUrl);
                            loadWebUrl(thirdUrl, app.getName(), app.getId(), false, false);
                            break;
                        //报表
                        case NewMyApps.TYPE_REPORT_APP:
                            StringBuilder sb = new StringBuilder();
                            //http://10.6.0.196/wr/appReport/view.html?fr_username=刘平&fr_password=e9ba823389393a9e5c7988ca21ea0d99&reportlet=/yn_local/app_demo.cpt&op=h5&userId=8e1ab776-8d18-4d84-b79e-b5abd1cc33a5
                            sb.append(URLs.YNEDUT_REPORT_APP)
                                    .append("?fr_username=").append(loginUser.getAccount())
                                    .append("&fr_password=").append(LastLoginUserSP.getInstance(context).getUserPassword())

                                    .append("&").append(app.getUrl())
                                    .append("&userId=").append(loginUser.getUserNo());
                            Log.i(TAG, "报表跳转地址：" + sb.toString());
                            loadWebUrl(sb.toString(), app.getName(), app.getId(), false, false);
                            break;
                        //OA
                        case NewMyApps.TYPE_OA_APP:
                            String url = URLs.YNEDUT_OA_APP;
                            String resultUrl = url + "?version=V2.0" + "&username=" + CryptUtil.encode(loginUser.getAccount()) + "&password=" + LastLoginUserSP.getInstance(context).getUserPassword() + "&linked=ms&method=getMyTodoTask";
                            Log.i(TAG, "OA跳转地址：" + resultUrl);
                            loadWebUrl(resultUrl, app.getName(), "", initMenu, true);
                            break;
                        default:
                            break;
                    }
                }
            };
            AsyncTaskCompat.executeParallel(tokenTask);
        }

    }

    /**
     * 显示V8网页
     *
     * @param url
     * @param name
     * @param appId
     */
    private void loadWebUrl(String url, String name, String appId, boolean withCordova, boolean isOa) {
        if (withCordova) {
            //do nothing
        } else {
            Intent intent = new Intent();
            intent.setClass(context, X5WebAppActivity.class);
            intent.putExtra(X5WebAppActivity.IS_OA, isOa);
            intent.putExtra("url", url);
            intent.putExtra("Name", name);
            intent.putExtra("AppId", appId);
            context.startActivity(intent);
        }
    }


    /**
     * APP list通过seq排序
     */
    public static class AppComparatorByTopSeq implements Comparator<NewMyApps> {
        @Override
        public int compare(NewMyApps o1, NewMyApps o2) {
            return o1.getTopSequence() - o2.getTopSequence();
        }
    }

    /**
     * APP list通过seq排序
     */
    public static class AppComparatorBySeq implements Comparator<NewMyApps> {
        @Override
        public int compare(NewMyApps o1, NewMyApps o2) {
            int seq1 = o1.getSequence();
            int seq2 = o2.getSequence();
            return seq1 - seq2;
        }
    }

    /**
     * 打开cordova网页工具类
     *
     * @param token
     */
    private void openCordovaUrl(NewMyApps app, boolean initMenu, int source, String token) {
        if (StringUtils.isEmpty(token)) {
            ToastUtil.toastAlerMessageBottom(context, "服务器连接异常", 500);
            return;
        }
        String originUrl = StringUtils.chop(mApplication.CONFIG_YNEDUT_V8_URL);
        String appUrl = app.getUrl();
        try {
            originUrl = URLEncoder.encode(originUrl, "utf-8");
            //判断是否有子菜单
            if (!StringUtils.isEmpty(appUrl) || !initMenu) {
                appUrl = URLEncoder.encode(appUrl, "utf-8");
            } else {
                List<Submenu> submenus = JSON.parseArray(app.getSubmenu(), Submenu.class);
                appUrl = URLEncoder.encode(submenus.get(0).getRoute(), "utf-8");
            }
        } catch (Exception e) {
            L.e(TAG, e.getMessage(), e);
        }
        String resultUrl = "";
        switch (source) {
            case NewMyApps.TYPE_YNEDUT_APP:
                StringBuilder urlArg = new StringBuilder();
                urlArg.append("file:///android_asset/www/index.html#/")
                        .append(appUrl)
                        .append("?access_token=").append(token)
                        .append("&userId=").append(user.getUserNo())
                        .append("&userType=").append(user.getUserType())
                        .append("&originUrl=").append(originUrl);
                resultUrl = urlArg.toString();
                break;
            case NewMyApps.TYPE_SMESIS_APP:
                String smesisUserId = LastLoginUserSP.getInstance(context).getSmesisUserId();
                if (StringUtils.isEmpty(smesisUserId)) {
                    if (onLoginStateListener != null) {
                        onLoginStateListener.abnormalAccount(true);
                    }
                    return;
                }
                String V8_HOST_SERVICE_HOST = mApplication.CONFIG_YNEDUT_V8_SERVICE_HOST;
                String smesisUrl = V8_HOST_SERVICE_HOST.substring(0, StringUtils.removeEnd(V8_HOST_SERVICE_HOST, "/").lastIndexOf("/")) + "/smesis";
                StringBuilder smesisArg = new StringBuilder("file:///android_asset/smesis/index.html#/").append(appUrl)
                        .append("?access_token=").append(token)
                        .append("&platformSysUserId=").append(LastLoginUserSP.getInstance(context).getSmesisUserId())
                        .append("&originUrl=").append(smesisUrl);
                resultUrl = smesisArg.toString();
                break;
            default:
                break;
        }
        L.i(TAG, "跳转url:" + resultUrl);
        Intent intent = new Intent();
        intent.setClass(context, CordovaWebActivity.class);
        intent.putExtra("url", resultUrl);
        intent.putExtra("title", app.getName());
        intent.putExtra("initMenu", initMenu);
        intent.putExtra("id", app.getId());
        context.startActivity(intent);
    }

    public void destoryCheckMyApp() {
        checkMyApps = null;
    }
}
