package com.yineng.ynmessager.activity.app;

import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.MainActivity;
import com.yineng.ynmessager.activity.app.adapter.GroupListAdapter;
import com.yineng.ynmessager.activity.app.adapter.GroupListAdapter.OnUpdateGroupDateApp;
import com.yineng.ynmessager.activity.app.adapter.HomeAppGridAdapter;
import com.yineng.ynmessager.activity.app.adapter.RecentAppGridAdapter;
import com.yineng.ynmessager.bean.app.NewMyApps;
import com.yineng.ynmessager.bean.settings.Setting;
import com.yineng.ynmessager.db.NewMyAppsTb;
import com.yineng.ynmessager.db.SettingsTb;
import com.yineng.ynmessager.db.dao.NewMyAppsDao;
import com.yineng.ynmessager.db.dao.SettingsTbDao;
import com.yineng.ynmessager.util.DensityUtil;
import com.yineng.ynmessager.view.DragGridView;
import com.yineng.ynmessager.view.NoScrollListView;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static com.yineng.ynmessager.activity.app.adapter.GroupAppAdapter.OnCheckedChangeListener;
import static com.yineng.ynmessager.activity.app.adapter.HomeAppGridAdapter.OnAdapterClickListener;

/**
 * Created by yn on 2017/7/19.
 * 更多app界面
 */

public class AppsMoreAcrivity extends BaseActivity implements OnUpdateGroupDateApp, OnAdapterClickListener, OnCheckedChangeListener, RecentAppGridAdapter.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private NewMyAppsDao appDao;
    private SettingsTbDao settingDao;

    private LinkedList<NewMyApps> apps = new LinkedList<>(); //通过排序查询所有的app
    private LinkedList<NewMyApps> recentApps = new LinkedList<>();//最近的app
    private LinkedList<NewMyApps> deleteApps = new LinkedList<>();//通过删除的app
    private HomeAppGridAdapter mHomeAdapter;    //首页的adapter
    private RecentAppGridAdapter mRecentAdapter;    //最近app的adapter
    private GroupListAdapter listAdapter;       //分组显示的adapter
    private NoScrollListView groupListView;     //listview 禁止滑动
    private DragGridView mGridView, recentGridView;   //拖拽的gridview
    private DragGridView editGridView;//用于编辑显示的gridview
    private TextView appSeatView;//搜索框占位
    private LinearLayout editLin, hidnLin;//编辑时显示和隐藏的布局
    private CheckBox checkService;
    private ScrollView appEditScrollView; //编辑栏的scroll
    private boolean isEdit = false;
    private TextView changeStateBtn; //编辑按钮
    private RelativeLayout searchBox;//搜索框
    private RelativeLayout appBackBtn; //返回键
    private Setting setting;//用户设置
    private boolean isShowAlert = true; //是否开启弹窗
    private AlertDialog dialog; //提示窗
    private CheckMyApps checkMyApps;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_apps);
        appDao = new NewMyAppsTb(this);
        settingDao = new SettingsTb(this);
        checkMyApps = CheckMyApps.getInstance(this);
        initView();
        queryDatas();
    }

    private void initView() {
        mGridView = (DragGridView) findViewById(R.id.home_gridview);
        appEditScrollView = (ScrollView) findViewById(R.id.app_edit_scrollview);
        checkService = (CheckBox) findViewById(R.id.check_service);
        groupListView = (NoScrollListView) findViewById(R.id.app_group_list);
        recentGridView = (DragGridView) findViewById(R.id.recent_gridview);
        changeStateBtn = (TextView) findViewById(R.id.change_state_btn);
        searchBox = (RelativeLayout) findViewById(R.id.appSearchBox);
        appBackBtn = (RelativeLayout) findViewById(R.id.app_back);
        editGridView = (DragGridView) findViewById(R.id.home_edit_gridview);
        editLin = (LinearLayout) findViewById(R.id.edit_linear_show);
        hidnLin = (LinearLayout) findViewById(R.id.edit_linear_hide);
        dialog = new android.app.AlertDialog.Builder(this).create();
        setting = settingDao.obtainSettingFromDb();
        appSeatView = (TextView) findViewById(R.id.search_app_seat);

        //获得焦点置顶scroll
        hidnLin.setFocusable(true);
        hidnLin.setFocusableInTouchMode(true);
        hidnLin.requestFocus();

        //设置checkbox图片大小
        Drawable drawable = this.getResources().getDrawable(R.drawable.app_sync_selected);
        drawable.setBounds(0, 0, DensityUtil.dip2px(this, 20), DensityUtil.dip2px(this, 20));
        checkService.setCompoundDrawables(drawable, null, null, null);

        //是否开启弹窗
        if (setting.getIsRecommendAppDialog() == 0) {
            isShowAlert = false;
        }
        boolean isSync = false;
        //不同步
        if (setting.getIsRecommendApp() == 1) {
            isSync = true;
        }
        checkService.setChecked(isSync);
        changeStateBtn.setOnClickListener(this);
        searchBox.setOnClickListener(this);
        appBackBtn.setOnClickListener(this);
        checkMyApps.setOnLoginStateListener(onLoginStateListener);


        //隐藏置顶的lin，显示下方的lin
        editLin.setVisibility(View.GONE);
        hidnLin.setVisibility(View.VISIBLE);

        mGridView.setOnItemClickListener(homeOnItemClick);
        recentGridView.setOnItemClickListener(recentOnItemClick);
        checkService.setOnCheckedChangeListener(this);
    }

    /**
     * 获取数据
     */
    private void queryDatas() {
        //根据时间查询
        recentApps = appDao.queryOrderByDate();
        //根据seq查询
        apps = appDao.queryOrderBySeq();
        mHomeAdapter = new HomeAppGridAdapter(apps, this);
        mRecentAdapter = new RecentAppGridAdapter(this, recentApps);
        mHomeAdapter.setOnDeleteClickListener(this);
        mGridView.setAdapter(mHomeAdapter);
        editGridView.setAdapter(mHomeAdapter);
        recentGridView.setAdapter(mRecentAdapter);
        mRecentAdapter.setOnCheckedChangeListener(this);

        refreshListAdapter();
    }

    /**
     * 刷新listAdapter
     */
    private void refreshListAdapter() {
        //分组显示
        List<HashMap<String, Object>> groupList = appDao.queryGroupByGroup();
        List<HashMap<String, List<NewMyApps>>> groupApp = new ArrayList<>();
        for (int i = 0; i < groupList.size(); i++) {
            HashMap<String, List<NewMyApps>> appMap = new HashMap<>();
            List<NewMyApps> apps = appDao.queryByGroupId(String.valueOf(groupList.get(i).get("typeId")));
            appMap.put("apps", apps);
            groupApp.add(appMap);
        }
        listAdapter = new GroupListAdapter(groupList, groupApp, this);
        groupListView.setAdapter(listAdapter);
        listAdapter.setOnUpdateGroupDateApp(this);
        listAdapter.setOnCheckedChangeListener(this);
    }

    /**
     * 刷新主页面显示adapter
     */
    private void refreshRecommAdapter() {
        //根据seq查询
        apps = appDao.queryOrderBySeq();
        mHomeAdapter.setNewItem(apps);
    }


    /**
     * 删除首页应用
     */
    @Override
    public void onDeleteListener(List<NewMyApps> apps, int position) {
        for (NewMyApps app : apps) {
            //删除首页应用不是真的删除，而是把seq改为0
            app.setIsRecommend(false);
            //设置用户删除
            app.setUserDelete(NewMyAppsDao.USER_DELETE_TRUE);
            if (checkService.isChecked()) {
                //更新最后日期
                Date updateDate = new Date();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                app.setUpdateTime(df.format(updateDate));
            }
            //设置删除的id，让分组取消checked
            listAdapter.setDeleteAppId(app.getId());
            deleteApps.add(app);
//            mRecentAdapter.addApp(app);
//            mRecentAdapter.setDeleteAppId(app.getId());
        }
    }

    /**
     * 弹出是否更新服务器设置窗口
     */
    private int isRecommendShowDialog = 1;

    private void showAccountDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_recommend_dialog, null);
        LinearLayout cancel = (LinearLayout) view.findViewById(R.id.cancel);
        LinearLayout sure = (LinearLayout) view.findViewById(R.id.sure);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.delete_local_info);
        //设置checkbox图片大小
        Drawable drawable = this.getResources().getDrawable(R.drawable.app_sync_selected);
        drawable.setBounds(0, 0, DensityUtil.dip2px(this, 20), DensityUtil.dip2px(this, 20));
        checkBox.setCompoundDrawables(drawable, null, null, null);


        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isRecommendShowDialog = 1;
                if (isChecked) {
                    isRecommendShowDialog = 0;
                }

            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkService.setChecked(false);
                dialog.dismiss();
            }
        });
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setting.setIsRecommendAppDialog(isRecommendShowDialog);
                settingDao.update(setting);
                refershRecommend(true);
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        dialog.setView(view);
        dialog.show();
    }

    /**
     * 判断用户状态是否异常
     */
    private CheckMyApps.OnLoginStateListener onLoginStateListener = new CheckMyApps.OnLoginStateListener() {
        @Override
        public void abnormalAccount(boolean isLoginOut) {
            if(isLoginOut){
                initIdPastDialog("");
            }
        }
    };

    /**
     * 拖拽监听事件，更新updateTime
     *
     * @param app
     */
    @Override
    public void onDragListener(NewMyApps app) {
//        if(checkService.isChecked()) {
//            //更新最后日期
//            Date updateDate = new Date();
//            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            app.setUpdateTime(df.format(updateDate));
//        }
//        appDao.update(app);
    }

    /**
     * 推荐应用点击事件
     */
    private AdapterView.OnItemClickListener homeOnItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (!isEdit) {
                NewMyApps app = mHomeAdapter.getApps().get(position);
                //如果是新应用就修改成旧
                if (app.getIsNew() == 1) {
                    app.setIsNew(0);
                }
                updateLastUseDate(app);
                //更新ui
                refreshListAdapter();
                refreshRecommAdapter();
            }
        }
    };

    /**
     * 最近应用点击事件
     */
    private AdapterView.OnItemClickListener recentOnItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            NewMyApps app = mRecentAdapter.getApps().get(position);
            //如果是新应用就修改成旧
            if (app.getIsNew() == 1) {
                app.setIsNew(0);
            }
            updateLastUseDate(app);
        }
    };

    /**
     * 点击分类列表事件
     *
     * @param app
     */
    @Override
    public void onUpdateGroupDate(NewMyApps app) {
        //如果是新应用就修改成旧
        if (app.getIsNew() == 1) {
            app.setIsNew(0);
        }
        updateLastUseDate(app);
        //更新ui
        refreshListAdapter();
        refreshRecommAdapter();
    }

    /**
     * 更新最后显示时间
     */
    private void updateLastUseDate(NewMyApps app) {
        if (!isEdit) {
            //更新最后日期
            Date lastUseDate = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            app.setLastUseDate(df.format(lastUseDate));
            appDao.update(app);
            //app跳转
            boolean isMenu = !StringUtils.isEmpty(app.getSubmenu());
            CheckMyApps.getInstance(AppsMoreAcrivity.this).JumpApp(app, isMenu);
        }
    }


    /**
     * 添加应用
     *
     * @param position
     * @param view
     */
    @Override
    public void checkedChangeListener(int position, View view, NewMyApps addApp) {
        int recommendNum = 0;
        boolean isexisted = false;
        //计算当前首页有多少张图片
        for (NewMyApps app : mHomeAdapter.getApps()) {
            if (app.getIsRecommend()) {
                recommendNum++;
            }
            if (app.getId().equals(addApp.getId())) {
                isexisted = true;
            }
        }

        //最大数量不超过15
        if (recommendNum == NewAppFragment.MAX_RECOMMEND_COUNT) {
            //判断是否是推荐
            if (!addApp.getIsRecommend()) {
                Toast.makeText(this, "首页最多添加15个应用", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        updateNewItem(addApp, isexisted);
    }

    /**
     * 添加或者删除一个新的条目
     *
     * @param addApp
     */
    private void updateNewItem(NewMyApps addApp, boolean isexisted) {
        if (checkService.isChecked()) {
            //更新最后日期
            Date updateDate = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            addApp.setUpdateTime(df.format(updateDate));
        }
        //不存在就添加，存在就删除
        if (!isexisted) {
            //增加一个新的条目
            mHomeAdapter.addNewItem(addApp);
            mRecentAdapter.deleteApp(addApp);
            listAdapter.setAddAppId(addApp.getId());
            addApp.setIsRecommend(true);
            addApp.setUserDelete(NewMyAppsDao.USER_DELETE_FALSE);
            apps.add(addApp);
        } else {
            mHomeAdapter.deleteItem(addApp);
            listAdapter.setDeleteAppId(addApp.getId());
            addApp.setIsRecommend(false);
            addApp.setUserDelete(NewMyAppsDao.USER_DELETE_TRUE);
            deleteApps.add(addApp);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        //重新刷新最近
        recentApps = appDao.queryOrderByDate();
        mRecentAdapter.setNewItem(recentApps);
    }

    /**
     * 同步服务器
     *
     * @param buttonView
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //是否开启弹窗
        if (setting.getIsRecommendAppDialog() == 0) {
            isShowAlert = false;
        } else {
            isShowAlert = true;
        }
        if (isShowAlert && isChecked) {
            showAccountDialog();
            return;
        }
        refershRecommend(isChecked);
    }

    private void refershRecommend(boolean isChecked) {
        int isRecommendApp = 0;
        if (isChecked) {
            isRecommendApp = 1;
        }
        setting.setIsRecommendApp(isRecommendApp);
        settingDao.update(setting);
        if (isChecked) {
            checkMyApps.checkAppsApi(appDao, 1);
            checkMyApps.setOnCheckFinishListener(new CheckMyApps.OnCheckFinishListener() {
                @Override
                public void checkMyAppFinishListener(int state, boolean isFirst) {
                    if (state == 1) {
                        LinkedList<NewMyApps> apps = appDao.queryOrderBySeq();
                        mHomeAdapter.setNewItem(apps);
                    }
                }

                @Override
                public void checkNotifyFinishListener(int state) {

                }

                @Override
                public void checkDealtFinishListener(int state) {

                }

            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.appSearchBox:
                //打开fragment
                AppSearchFragment dialogFragment = new AppSearchFragment();//搜索弹窗
                dialogFragment.show(this.getFragmentManager(), "searchDialog");
                break;
            case R.id.change_state_btn:
                setIsEdit(false);
                break;
            case R.id.app_back:
                if (isEdit) {
                    setIsEdit(true);
                } else {
                    finish();
                }
                break;
        }

    }

    /**
     * 设置是否编辑
     *
     * @param isBack 是否退出
     */
    private void setIsEdit(boolean isBack) {
        if (isEdit) {
            isEdit = false;
            changeStateBtn.setText("设置");
            //如果不是退出按钮就保存用户设置
            if (!isBack) {
                //修改首页app
                if (deleteApps != null) {
                    appDao.update(deleteApps);
                }
                appDao.update(mHomeAdapter.getApps());
                mHomeAdapter.notifyDataSetChanged();
            } else {
                //重新刷新推荐列表
                //根据seq查询
                apps = appDao.queryOrderBySeq();
                mHomeAdapter = new HomeAppGridAdapter(apps, this);
                mHomeAdapter.setOnDeleteClickListener(this);
                mGridView.setAdapter(mHomeAdapter);
                editGridView.setAdapter(mHomeAdapter);
                refreshListAdapter();
            }
            //隐藏置顶的lin，显示下方的lin
            editLin.setVisibility(View.GONE);
            hidnLin.setVisibility(View.VISIBLE);
            //显示搜索框
            searchBox.setVisibility(View.VISIBLE);
            appSeatView.setVisibility(View.GONE);
        } else {
            isEdit = true;
            changeStateBtn.setText("完成");
            editLin.setVisibility(View.VISIBLE);
            hidnLin.setVisibility(View.GONE);
            //隐藏搜索框
            searchBox.setVisibility(View.GONE);
            appSeatView.setVisibility(View.VISIBLE);
        }
        mGridView.isEdit(isEdit);
        editGridView.isEdit(isEdit);
        mRecentAdapter.setEdit(isEdit);
        listAdapter.setEdit(isEdit);
        mHomeAdapter.setEditState(isEdit);
        appEditScrollView.post(new Runnable() {
            @Override
            public void run() {
                appEditScrollView.smoothScrollTo(0, 0);
            }
        });
    }
}
