package com.yineng.ynmessager.activity.app;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseFragment;
import com.yineng.ynmessager.activity.MainActivity;
import com.yineng.ynmessager.activity.app.adapter.HomeAppGridAdapter;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.app.Dealt;
import com.yineng.ynmessager.bean.app.NewMyApps;
import com.yineng.ynmessager.bean.app.Publicize;
import com.yineng.ynmessager.bean.settings.Setting;
import com.yineng.ynmessager.db.DealtTb;
import com.yineng.ynmessager.db.NewMyAppsTb;
import com.yineng.ynmessager.db.PublicizeTb;
import com.yineng.ynmessager.db.SettingsTb;
import com.yineng.ynmessager.db.dao.DealtDao;
import com.yineng.ynmessager.db.dao.NewMyAppsDao;
import com.yineng.ynmessager.db.dao.PublicizeDao;
import com.yineng.ynmessager.db.dao.SettingsTbDao;
import com.yineng.ynmessager.util.DensityUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.view.AppsViewPager;
import com.yineng.ynmessager.view.DragGridView;
import com.yineng.ynmessager.view.pullUpDownScrollView.OnDefaultRefreshListener;
import com.yineng.ynmessager.view.PtrClassicRefreshLayout;
import com.yineng.ynmessager.view.pullUpDownScrollView.PtrFrameLayout;
import com.yineng.ynmessager.view.pullUpDownScrollView.ScrollViewFinal;
import com.zhy.http.okhttp.OkHttpUtils;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Created by yn on 2017/7/3.
 * 新的应用首页
 */

public class NewAppFragment extends BaseFragment implements View.OnClickListener, AppSearchFragment.OnDialogFragmentDismiss, ScrollViewFinal.OnScrollListener {

    private View view;
    private AppsViewPager eventViewPager; //待处理事件的viewpager
    private List<Dealt> events = new ArrayList(); //待处理事件
    private EventViewPagerAdapter eventViewPagerAdapter;//待处理事件adapter
    private List<DragGridView> eventGridViews = new ArrayList<>(); //待处理事件gridview
    private EventGridViewAdapter eventGridViewAdapter;
    private LinearLayout dotLinear; //dot小圆点
    private ViewPager notifyViewPager; //通知viewpager
    private NotifyViewPagerAdapter mNotifyViewPagerAdapter; //文字通知adapter
    private LinkedList<Publicize> notifys = new LinkedList<>(); //通知文字集合
    private List<TextView> notifyTxts; //notify通知text
    private LinearLayout notifyContentView, eventContentView;//notify的框架,待办列表的框架
    private LinearLayout loadAppSuccess; //加载成功ui
    private RelativeLayout loadAppFail; //加载失败ui
    private TextView currentTxt, totalTxt; //通知viewpager的页码
    private Button notifyFlagBtn; //判断notify是否可点的button
    private DragGridView recommendGridView; //app展示view
    private HomeAppGridAdapter recommendAdapter; //app gridview的adapter
    public static final int MAX_RECOMMEND_COUNT = 15;//最多显示几个个应用
    private ScheduledExecutorService executorService;//轮播文字服务

    private int curIndex = 0; //当前notify的下标
    private int clickNotifyIndex = 0;//记录点击时的通知下标
    private int curEventIndex = 0;//待办的下标
    public static final int PAGE_SIZE = 4;//每页显示4个待办

    private RelativeLayout searchBox; //搜索框
    private LinearLayout schoolLogoBox;//学校logo
    private PtrClassicRefreshLayout mPtrLayout;//下拉刷新
    private ScrollViewFinal scroll;
    private ImageView schoolLogo;//学校logo
    //app数据库
    private NewMyAppsDao appDao;
    //待办数据库
    private DealtDao dealtDao;
    //文字推广数据库
    private PublicizeDao publicizeDao;
    //用户设置
    private SettingsTbDao settingDao;
    private LinkedList<NewMyApps> apps = new LinkedList<>();//用于保存查询的app
    private LinkedList<NewMyApps> homeApps = new LinkedList<>();//用于获取推荐的app
    //打开外部网页返回fragment回调
    private final int OPEN_URL_CODE = 100;
    private boolean isShowToast = false;//下拉刷新后才可以出现网络连接失败的弹窗
    private boolean isAnimRun = false;//判断动画是否执行
    private boolean isReasum = false;

    //注销掉业务逻辑代码
//    private final AppController mApplication = AppController.getInstance();
//    private FrameLayout top_view;//顶部包括搜索框的对象
//    private CommonTabLayout tablayout; //业务逻辑中展示选择项
//    private int statusBarHeight;// 手机状态栏高度
//    private int[] icon = {R.mipmap.ic_launcher}; //仅做填充数据使用
//    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
//    private LinearLayout float_view,float_stop;// 滑动悬浮的视图和停止视图
//    private EmptySupportRecyclerView business_recycle;// 业务逻辑的recycleview
//    private View emptyView; // 业务逻辑为空的时候显示视图
//    private int pageSize = 5;//业务逻辑中每页获取大小
//    private int pageNum = 0;// 获取的第几页 从0开始
//    private String mobileModuleId = "";//默认的查询模块Id开始的时候为空
//    private boolean isLast = false;//业务逻辑返回数据是否是最后一页
//    private ArrayList<NewAppContentItem> dataList = new ArrayList<>();
//    private AppBusinessAdapter appBusinessAdapter;// 业务适配器


    public void setResume(boolean reasum) {
        isReasum = reasum;
    }

    private static NewAppFragment newAppFragment = null;

    public static NewAppFragment getInstance() {
        if (newAppFragment == null) {
            synchronized (NewAppFragment.class) {
                if (newAppFragment == null) {
                    newAppFragment = new NewAppFragment();
                }
            }
        }
        return newAppFragment;
    }
    /**
     * scrollviewFinal滑动事件
     */
    @Override
    public void onScroll(int scrollY) {
        L.i("距离：   ",scrollY+""+"     改变布局距离：   "+loadAppSuccess.getBottom()+""+"    listview：高度"+ scroll.getHeight()+"");
        //注销掉业务逻辑代码
        // 滑动控件悬停
//        if(scrollY >= loadAppSuccess.getBottom()-top_view.getHeight()-statusBarHeight){
//            if (tablayout.getParent()!=float_stop) {
//                float_view.removeView(tablayout);
//                float_stop.addView(tablayout);
//            }
//        }else{
//            if (tablayout.getParent()!=float_view) {
//                float_stop.removeView(tablayout);
//                float_view.addView(tablayout);
//            }
//        }
    }

    /**
     * 获取新的app数量
     */
    public interface OnNewAppListener {
        void onNewAppNotify(boolean isNew);
    }

    private OnNewAppListener onNewAppListener;

    public void setOnNewAppListener(OnNewAppListener mOnNewAppListener) {
        this.onNewAppListener = mOnNewAppListener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        newAppFragment = this;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDao = new NewMyAppsTb(getActivity());
        dealtDao = new DealtTb(getActivity());
        publicizeDao = new PublicizeTb(getActivity());
        settingDao = new SettingsTb(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main_app_new_layout, container, false);
        //注销掉业务逻辑代码
//        statusBarHeight = getStatusBarHeight();
        initData();
        initView(view);
        return view;
    }

    /**
     * 初始化UI
     *
     * @param view
     */
    private void initView(View view) {
        notifyFlagBtn = (Button) view.findViewById(R.id.app_notify_flag);
        schoolLogo = (ImageView) view.findViewById(R.id.app_school_logo);
        eventViewPager = (AppsViewPager) view.findViewById(R.id.app_event_viewpager);
        dotLinear = (LinearLayout) view.findViewById(R.id.dot_linear);
        notifyViewPager = (ViewPager) view.findViewById(R.id.notify_txt_viewPager);
        currentTxt = (TextView) view.findViewById(R.id.current_page_txt);
        totalTxt = (TextView) view.findViewById(R.id.total_page_txt);
        recommendGridView = (DragGridView) view.findViewById(R.id.app_recommend_gridview);
        searchBox = (RelativeLayout) view.findViewById(R.id.appSearchBox);
        schoolLogoBox = (LinearLayout) view.findViewById(R.id.school_logo_box);
        mPtrLayout = (PtrClassicRefreshLayout) view.findViewById(R.id.ptr_layout);
        mPtrLayout.disableWhenHorizontalMove(true);
        scroll = (ScrollViewFinal) view.findViewById(R.id.scroll);
        scroll.setOnScrollListener(this);
        notifyContentView = (LinearLayout) view.findViewById(R.id.app_notify_content);
        eventContentView = (LinearLayout) view.findViewById(R.id.event_content_linear);
        loadAppFail = (RelativeLayout) view.findViewById(R.id.app_load_fail_view);
        loadAppSuccess = (LinearLayout) view.findViewById(R.id.app_load_success_view);


        eventViewPagerAdapter = new EventViewPagerAdapter();
        mNotifyViewPagerAdapter = new NotifyViewPagerAdapter();
        eventViewPager.setAdapter(eventViewPagerAdapter);
        eventViewPager.addOnPageChangeListener(eventViewPagerChange);
        notifyViewPager.setAdapter(mNotifyViewPagerAdapter);
        notifyViewPager.addOnPageChangeListener(notifyPagerChange);
        recommendGridView.setOnItemClickListener(recommendItemClick);
        searchBox.setOnClickListener(this);
        //注销掉业务逻辑代码
//        top_view = (FrameLayout) view.findViewById(R.id.top_view);
//        //用于在滑动的时候悬停控件时使用的
//        float_view= (LinearLayout) view.findViewById(R.id.float_view);
//        float_stop = (LinearLayout) view.findViewById(R.id.float_stop);
//        tablayout= (CommonTabLayout) view.findViewById(R.id.tablayout);
//        business_recycle = (EmptySupportRecyclerView) view.findViewById(R.id.business_recycle);
//        business_recycle.setEmptyTextHint("应用业务数据返回错误");
//        emptyView = view.findViewById(R.id.text_empty);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
//        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        business_recycle.setLayoutManager(layoutManager);
//        appBusinessAdapter = new AppBusinessAdapter(getActivity());
//        appBusinessAdapter.setData(dataList);
//        business_recycle.setAdapter(appBusinessAdapter);
//
//        tablayout.setOnTabSelectListener(new OnTabSelectListener() {
//            @Override
//            public void onTabSelect(int position) {
//                // TODO 点击 tablayout 切换
//                mobileModuleId = mTabEntities.get(position).getId();
//                pageNum= 0;
//                getAppBusiness(pageNum);
//            }
//
//            @Override
//            public void onTabReselect(int position) {
//
//            }
//        });

        mPtrLayout.setOnRefreshListener(new OnDefaultRefreshListener() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                // 下拉刷新
//                pageNum=0;
                getData(0);
            }
        });
        //注销掉业务逻辑代码
//        scroll.setOnLoadMoreListener(new OnLoadMoreListener() {
//            @Override
//            public void loadMore() {
//                // 上拉加载
//                pageNum+=1;
//                getAppBusiness(pageNum);
//            }
//        });
        scroll.setNoLoadMoreHideView(false);
        notifyFlagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = notifys.get(curIndex).getUrl();
                if (!StringUtils.isEmpty(url)) {
                    Intent it = new Intent(getActivity(), X5WebAppActivity.class);
                    it.putExtra("url", url);
                    startActivityForResult(it, OPEN_URL_CODE);
                }
            }
        });
        //判断是否有定制logo
        hasCustomLogo();
        recommendGridView.setAdapter(recommendAdapter);
        setNotifyTextView();
        setEventGridView();
    }


    /**
     * 初始化数据
     */
    public void initData() {
        events = dealtDao.queryOrderBySequence();
        notifys = publicizeDao.queryOrderBy();
        apps = appDao.queryOrderBySeq();

        //通知新APP数量
        notifyNewAppOrNewDealt();
        //推荐应用
        recommendAdapter = new HomeAppGridAdapter(apps, getActivity(), true);
        homeApps = recommendAdapter.getApps();
        recommendAdapter.notifyDataSetChanged();
    }

    /**
     * 判断是否加载成功
     */
    public boolean loadSuccessOrFail() {
        boolean isSuccess = false;
        if (apps.size() > 0) {
            isSuccess = true;
        }
        if (isSuccess) {
            loadAppFail.setVisibility(View.GONE);
            loadAppSuccess.setVisibility(View.VISIBLE);
        } else {
            loadAppFail.setVisibility(View.VISIBLE);
            loadAppSuccess.setVisibility(View.GONE);
        }
        return isSuccess;
    }

    /**
     * 获取待办事项的gridview
     */
    private void setEventGridView() {
        //如果没有待办列表就隐藏
        if (events.size() > 0) {
            showNotifyAnimation(eventContentView);
        } else {
            hideNotifyAnimation(eventContentView);
            return;
        }

        //待办事项gridview 添加
        eventGridViews.clear();
        final int pageCount = (int) Math.ceil(events.size() / 4.0f);
        Drawable drawable = getResources().getDrawable(R.drawable.gridview_list_selected);
        for (int i = 0; i < pageCount; i++) {
            DragGridView gv = new DragGridView(getActivity());
            eventGridViewAdapter = new EventGridViewAdapter(i);
            gv.setGravity(Gravity.CENTER);
            gv.setClickable(true);
            gv.setFocusable(true);
            gv.setSelector(drawable);
            gv.setNumColumns(4);
            gv.setAdapter(eventGridViewAdapter);
            gv.setOnItemClickListener(eventOnItemClick);
            eventGridViews.add(gv);
        }
        setDotLayout();
    }

    /**
     * 设置指示器
     */
    private void setDotLayout() {
        if (dotLinear == null) {
            return;
        }
        dotLinear.removeAllViews();
        for (int i = 0; i < eventGridViews.size(); i++) {
            Button bt = new Button(getActivity());
            LinearLayout.LayoutParams btParams = new LinearLayout.LayoutParams(DensityUtil.dip2px(getActivity(), 6), DensityUtil.dip2px(getActivity(), 6));
            btParams.setMargins(DensityUtil.dip2px(getActivity(), 2), DensityUtil.dip2px(getActivity(), 5), DensityUtil.dip2px(getActivity(), 5), DensityUtil.dip2px(getActivity(), 5));
            bt.setLayoutParams(btParams);
            if (i == 0) {
                bt.setBackgroundResource(R.drawable.icon_dot_selector);
            } else {
                bt.setBackgroundResource(R.drawable.icon_dot_normal);
            }
            dotLinear.setGravity(Gravity.CENTER);
            dotLinear.addView(bt);
        }
        eventViewPagerAdapter.notifyDataSetChanged();
    }

    /**
     * 设置notify的样式
     */
    private void setNotifyTextView() {
        notifyTxts = new ArrayList<>();
        if (notifys.size() > 0) {
            currentTxt.setText("1");
            setNotifyFlagShowOrHide(notifys.get(0).getUrl());
        }
        //如果没有通知时则隐藏notify的view
        if (notifys.size() > 0) {
            showNotifyAnimation(notifyContentView);
        } else {
            hideNotifyAnimation(notifyContentView);
            return;
        }

        curIndex = 0;
        totalTxt.setText(notifys.size() + "");
        for (int i = 0; i < notifys.size(); i++) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            TextView notifyTxt = new TextView(getActivity());
            notifyTxt.setGravity(Gravity.CENTER_VERTICAL);
            notifyTxt.setLayoutParams(params);
            notifyTxt.setSingleLine(true);
            notifyTxt.setEllipsize(TextUtils.TruncateAt.valueOf("END"));
            notifyTxt.setClickable(true);
            notifyTxt.setOnClickListener(notifyTxtClickListener);
            notifyTxts.add(notifyTxt);
        }
        if (notifys.size() >= 2) {
            if (executorService != null) {
                executorService.shutdownNow();
            }
            startAutoScoll();
        }
    }

    /**
     * 如果有定制的logo
     */
    private void hasCustomLogo() {
        //ic_launcher
        String imgName = AppController.getInstance().icon_name;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) searchBox.getLayoutParams();
        //有定制logo
        if (!imgName.equals("ic_launcher")) {
            int drawAbleID = getResources().getIdentifier(AppController.getInstance().icon_name, "mipmap", getActivity().getPackageName());
            schoolLogo.setImageResource(drawAbleID);
            schoolLogoBox.setVisibility(View.VISIBLE);
            params.setMargins(DensityUtil.dip2px(getActivity(), 80), DensityUtil.dip2px(getActivity(), 3), DensityUtil.dip2px(getActivity(), 9), DensityUtil.dip2px(getActivity(), 0));
            searchBox.setLayoutParams(params);
            isAnimRun = true;
        }
    }

    /**
     * 判断通知文字的详情表示是否显示
     *
     * @param url
     */
    private void setNotifyFlagShowOrHide(String url) {
        //如果没有url就不显示详情，反之亦然
        if (StringUtils.isEmpty(url)) {
            notifyFlagBtn.setText("");
            notifyFlagBtn.setBackgroundDrawable(null);
            notifyFlagBtn.setBackgroundColor(Color.parseColor("#00000000"));
        } else {
            notifyFlagBtn.setText("详情");
            notifyFlagBtn.setBackgroundResource(R.drawable.app_notify_flag);
        }
    }

    /**
     * 开始轮播notify
     */
    private void startAutoScoll() {
        if (notifys.size() == 0) {
            return;
        }
        executorService = Executors.newScheduledThreadPool(1);
        //每隔四秒换文字
        executorService.scheduleWithFixedDelay(new PagerTaks(), notifys.size(), 4, TimeUnit.SECONDS);
    }

    /**
     * notifyViewPager自动滚动runnable
     */
    private class PagerTaks implements Runnable {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int count = notifys.size();
                    notifyViewPager.setCurrentItem((curIndex + 1) % count);
                }
            });
        }
    }
    /**************************************动画****************************************************/

    /**
     * 搜索窗取消的监听
     */
    @Override
    public void onDismiss() {
        dismissDialog();
    }

    /**
     * 设置view的margin
     */
    class ViewWrapper {
        private View view;

        public ViewWrapper(View view) {
            this.view = view;
        }

        public void setMarginLeft(int marginLeft) {
            RelativeLayout.MarginLayoutParams marginLayoutParams = (RelativeLayout.MarginLayoutParams) view
                    .getLayoutParams();
            marginLayoutParams.leftMargin = marginLeft;
            view.setLayoutParams(marginLayoutParams);
        }

        public int getMarginLeft() {
            RelativeLayout.MarginLayoutParams marginLayoutParams = (RelativeLayout.MarginLayoutParams) view
                    .getLayoutParams();
            return marginLayoutParams.leftMargin;
        }
    }

    /**
     * 显示notify view
     *
     * @param notifyContentView
     */

    private void showNotifyAnimation(final LinearLayout notifyContentView) {
        //如果已经是显示的就不在走动画
        if (notifyContentView.getVisibility() == View.VISIBLE) {
            return;
        }
        notifyContentView.setVisibility(View.VISIBLE);
        ObjectAnimator objAnimScaleX = ObjectAnimator.ofFloat(notifyContentView, "ScaleX", 0, 1);
        ObjectAnimator objAnimScaleY = ObjectAnimator.ofFloat(notifyContentView, "ScaleY", 0, 1);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(objAnimScaleX).with(objAnimScaleY);
        animSet.setDuration(300);
        animSet.start();
    }

    /**
     * 隐藏notify view
     *
     * @param notifyContentView
     */
    private void hideNotifyAnimation(final LinearLayout notifyContentView) {
        if (notifyContentView.getVisibility() == View.GONE) {
            return;
        }
        if (executorService != null) {
            executorService.shutdownNow();
        }
        ObjectAnimator objAnimScaleX = ObjectAnimator.ofFloat(notifyContentView, "scaleX", 0);
        ObjectAnimator objAnimScaleY = ObjectAnimator.ofFloat(notifyContentView, "scaleY", 0);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(objAnimScaleX).with(objAnimScaleY);
        animSet.setDuration(300);
        animSet.start();
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                notifyContentView.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 显示搜索dialog
     */
    private void showDialog() {
        if(isAnimRun) {
            //动画logo跑上去
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(schoolLogoBox, "y", -200);
            ViewWrapper view = new ViewWrapper(searchBox);
            ObjectAnimator objectAnimatorSearchBox = ObjectAnimator.ofInt(view, "marginLeft", DensityUtil.dip2px(getActivity(), 9));
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(objectAnimator).with(objectAnimatorSearchBox);
            animatorSet.setDuration(300);
            animatorSet.start();
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    //打开fragment
                    AppSearchFragment dialogFragment = new AppSearchFragment();//搜索弹窗
                    dialogFragment.setOnDialogFragmentDismiss(NewAppFragment.this);
                    dialogFragment.show(getActivity().getFragmentManager(), "searchDialog");
                }
            });
        }else{
            //打开fragment
            AppSearchFragment dialogFragment = new AppSearchFragment();//搜索弹窗
            dialogFragment.setOnDialogFragmentDismiss(NewAppFragment.this);
            dialogFragment.show(getActivity().getFragmentManager(), "searchDialog");
        }
    }

    /**
     * dialog 取消的动画
     */
    private void dismissDialog() {
        if (!isAnimRun) {
            return;
        }
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(schoolLogoBox, "y", 0);
        final ViewWrapper view = new ViewWrapper(searchBox);
        ObjectAnimator objectAnimatorSearchBox = ObjectAnimator.ofInt(view, "marginLeft", DensityUtil.dip2px(getActivity(), 80));
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(objectAnimator).with(objectAnimatorSearchBox);
        animatorSet.setDuration(300);
        animatorSet.start();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setMarginLeft(DensityUtil.dip2px(getActivity(), 80));
            }
        });
    }

    /***************************************监听事件************************************************/
    /**
     * 通知文字点击事件
     */
    private View.OnClickListener notifyTxtClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            clickNotifyIndex = curIndex;
            Publicize publicize = notifys.get(clickNotifyIndex);
            if (!StringUtils.isEmpty(publicize.getUrl())) {
                Intent it = new Intent(getActivity(), X5WebAppActivity.class);
                it.putExtra("url", publicize.getUrl());
                startActivityForResult(it, OPEN_URL_CODE);
            }
            //TODO 文字已读未读功能取消
//            else {
//                publicize.setIsRead(1);
//                publicizeDao.update(publicize);
            int index = curIndex;
            //刷新notify
            notifyViewPager.setCurrentItem(index);
//            }
        }
    };


    /**
     * 打开外部网页修改文字为已读
     *
     * @param intent
     * @param requestCode
     */
    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        if (requestCode == OPEN_URL_CODE) {
            Publicize publicize = notifys.get(clickNotifyIndex);
            publicize.setIsRead(1);
            publicizeDao.update(publicize);
            int index = curIndex;
            //刷新notify
            mNotifyViewPagerAdapter = new NotifyViewPagerAdapter();
            notifyViewPager.setAdapter(mNotifyViewPagerAdapter);
            notifyViewPager.setCurrentItem(index);
        }
    }

    /**
     * 待办点击事件
     */
    private AdapterView.OnItemClickListener eventOnItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //获取当前分页下的待办对象
            int i = curEventIndex * PAGE_SIZE;
            int end = i + PAGE_SIZE;
            List<Dealt> pageEvents = new ArrayList<>();
            while ((i < events.size()) && (i < end)) {
                pageEvents.add(events.get(i));
                i++;
            }
            //待办和app的id是相同的
            NewMyApps app = appDao.queryById(pageEvents.get(position).getId());
            if (app != null) {
                //app跳转
                boolean isMenu = !StringUtils.isEmpty(app.getSubmenu());
                CheckMyApps.getInstance(getActivity()).JumpApp(app, isMenu);
            }
        }
    };
    /**
     * viewpager eventViewPager切换事件
     */
    private ViewPager.OnPageChangeListener eventViewPagerChange = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            curEventIndex = position;
            //设置dot的样式
            int viewCount = dotLinear.getChildCount();
            Button currentBt = (Button) dotLinear.getChildAt(position);
            if (currentBt == null) {
                return;
            }
            for (int i = 0; i < viewCount; i++) {
                (dotLinear.getChildAt(i)).setBackgroundResource(R.drawable.icon_dot_normal);
            }
            currentBt.setBackgroundResource(R.drawable.icon_dot_selector);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    /**
     * notify pagerListener
     */
    private ViewPager.OnPageChangeListener notifyPagerChange = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            //设置当前页码
            curIndex = position;
            currentTxt.setText((curIndex + 1) + "");
            String url = notifys.get(curIndex).getUrl();
            //如果没有url就不显示详情，反之亦然
            setNotifyFlagShowOrHide(url);
            if (executorService != null) {
                executorService.shutdownNow();
            }
            startAutoScoll();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    /**
     * app展示页点击事件
     */
    private AdapterView.OnItemClickListener recommendItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //更多应用
            if (position + 1 == recommendAdapter.getCount()) {
                Intent it = new Intent(getActivity(), AppsMoreAcrivity.class);
                startActivity(it);
                return;
            }
            NewMyApps app = homeApps.get(position);
            //如果是新应用就修改成旧
            if (app.getIsNew() == 1) {
                app.setIsNew(0);
            }
            //修改最后使用时间
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            app.setLastUseDate(df.format(new Date()));
            appDao.update(app);
            //app跳转
            boolean isMenu = !StringUtils.isEmpty(app.getSubmenu());
            CheckMyApps.getInstance(getActivity()).JumpApp(app, isMenu);
        }
    };

    /**
     * 点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.appSearchBox:
                showDialog();
                break;
        }
    }

    /**
     * 下拉刷新
     */
    private void getData(int pageNu) {
        //检查app更新
        CheckMyApps checkMyApps = CheckMyApps.getInstance(getActivity());
        Setting setting = settingDao.obtainSettingFromDb();
        boolean isSync = false;
        //不同步
        if (setting.getIsRecommendApp() == 1) {
            isSync = true;
        }
        if (isSync) {
            checkMyApps.checkAppsApi(appDao, 1);
        } else {
            checkMyApps.checkAppsApi(appDao, 0);
        }
        checkMyApps.checkDealtApi(dealtDao);
        checkMyApps.checkNotifyApi(publicizeDao);
        checkMyApps.setOnCheckFinishListener(checkFinishListener);
        checkMyApps.setOnLoginStateListener(onLoginStateListener);
        isShowToast = true;
        //注销掉业务逻辑代码
//        getAppBusiness(pageNu);

    }
    //注销掉业务逻辑代码
    /**
     * 获取应用中业务逻辑
     */
//    private void getAppBusiness(final int page) {
//        // TODO
//        Map<String,String> params = new HashMap<>();
//        params.put("userId", LastLoginUserSP.getLoginUserNo(getContext()));
//        params.put("access_token", mApplication.mAppTokenStr);
//        params.put("pageNumber", page+"");
//        params.put("pageSize", pageSize+"");
//        params.put("mobileModuleId", mobileModuleId+"");
//        String url = AsyncHttpClient.getUrlWithQueryString(false,
//                mApplication.CONFIG_YNEDUT_V8_URL + URLs.APP_BUSINESS,
//                params);
//        OKHttpCustomUtils.get(url, params, new StringCallback() {
//            @Override
//            public void onError(Call call, Exception e, int id) {
//                if (page==0){
//                    business_recycle.setEmptyView(emptyView);
//                    if(dataList.size()>0){
//                        dataList.clear();
//                    }
//                    mPtrLayout.onRefreshComplete();
//                }else {
//                    scroll.onLoadMoreComplete();
//                }
//            }
//
//            @Override
//            public void onResponse(String response, int id) {
//                try {
//                    JSONObject resObj = new JSONObject(responseString);
//                    int status = resObj.optInt("status");
//                    if (status==0){
//                        NewAppResult result = JSON.parseObject(resObj.optString("result"), NewAppResult.class);
//                        mTabEntities.clear();
//                        int curren = 0;//在第一次取数据的时候根据active判断所属数据时modeluelist中的第几个并设置tablayout显示
//                        if (result.getModuleInfoList()!=null) {
//                            for (int i = 0; i < result.getModuleInfoList().size(); i++) {
//                                NewAppModuleItem item = result.getModuleInfoList().get(i);
//                                mTabEntities.add(new TabEntity(item.getName(), icon[0], icon[0], item.getId()));
//                                if (item.isActive()) {
//                                    curren = i;
//                                }
//                            }
//                            tablayout.setTabData(mTabEntities);
//                            tablayout.setCurrentTab(curren);
//                        }
//                        if (result.getPageContent()!=null) {
//                            isLast = result.getPageContent().isLast();
//                            if (isLast) {
//                                scroll.setHasLoadMore(false);
//                            } else {
//                                scroll.setHasLoadMore(true);
//                            }
//                            dataList.addAll(result.getPageContent().getContent());
//                        }
//                    }
//                    else {
//                        if (page==0){
//                            business_recycle.setEmptyView(emptyView);
//                        }else {
//                            scroll.showFailUI();
//                        }
//                    }
//                } catch (JSONException e) {
//                    L.e(mTag, e.getMessage(), e);
//                    if (page==0){
//                        business_recycle.setEmptyView(emptyView);
//                    }else {
//                        scroll.onLoadMoreComplete();
//                    }
//                }
//            }
//
//            @Override
//            public void onAfter(int id) {
//                if (page==0){
//                    mPtrLayout.onRefreshComplete();
//                }else {
//                    scroll.onLoadMoreComplete();
//                }
//                appBusinessAdapter.notifyDataSetChanged();
//            }
//        });
//    }


    public CheckMyApps.OnCheckFinishListener checkFinishListener = new CheckMyApps.OnCheckFinishListener() {
        /**
         * 推广文字接口回调完成后监听事件
         */
        @Override
        public void checkNotifyFinishListener(int state) {
            if (state == 1) {
                try {
                    notifys.clear();
                    notifys = publicizeDao.queryOrderBy();
                    setNotifyTextView();
                    mNotifyViewPagerAdapter = new NotifyViewPagerAdapter();
                    notifyViewPager.setAdapter(mNotifyViewPagerAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mPtrLayout.onRefreshComplete();
        }

        /**
         * 待办接口回调完成后监听事件
         */
        @Override
        public void checkDealtFinishListener(int state) {
            if (state == 1) {
                events.clear();
            }
            events = dealtDao.queryOrderBySequence();
            notifyNewAppOrNewDealt();
            setEventGridView();
            eventViewPager.setCurrentItem(curEventIndex);
        }


        /**
         * app列表接口回调完成后监听事件
         */
        @Override
        public void checkMyAppFinishListener(int state, boolean isFirst) {
            if (state == 1) {
                apps.clear();
                apps = appDao.queryOrderBySeq();
                recommendAdapter.setNewItem(apps);
            } else {
                if (isFirst) {
                    loadSuccessOrFail();
                }
                if (isShowToast) {
                    ToastUtil.toastAlerMessage(getActivity(), "服务器连接错误", 300);
                }
            }
        }
    };

    /**
     * 判断用户状态是否异常
     */
    private CheckMyApps.OnLoginStateListener onLoginStateListener = new CheckMyApps.OnLoginStateListener() {
        @Override
        public void abnormalAccount(boolean isLoginOut) {
            if(isLoginOut){
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity. initIdPastDialog("");
            }
        }
    };

    /**
     * 判断是否有新应用和新的待办并且通知MainFragment判断是否显示标识
     */
    private void notifyNewAppOrNewDealt() {
        int newAppNum = 0;
        int dealtsCount = 0;
        //获取未办理的待办数量
        if (events.size()>0) {
            for (Dealt dealt : events) {
                if (Integer.parseInt(dealt.getCount()) > 0) {
                    dealtsCount++;
                }
            }
        }

        //获取新的app数量
        if (apps.size()>0) {
            for (NewMyApps app : apps) {
                if (app.getIsNew() == 1) {
                    newAppNum++;
                }
            }
        }
        if (onNewAppListener != null) {
            if (newAppNum > 0 || dealtsCount > 0) {
                onNewAppListener.onNewAppNotify(true);
            } else if (newAppNum == 0 && dealtsCount == 0) {
                onNewAppListener.onNewAppNotify(false);
            }
        }
    }


    /***************************************Adapter************************************************/
    /**
     * 事件列表gridview
     */
    class EventGridViewAdapter extends BaseAdapter {

        private List<Dealt> pageEvents;

        EventGridViewAdapter(int page) {
            int i = page * PAGE_SIZE;
            int end = i + PAGE_SIZE;
            pageEvents = new ArrayList<>();
            while ((i < events.size()) && (i < end)) {
                pageEvents.add(events.get(i));
                i++;
            }
        }

        @Override
        public int getCount() {
            return pageEvents.size();
        }

        @Override
        public Object getItem(int position) {
            return pageEvents.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            TextView titleTxt, numTxt;

            ViewHolder(View v) {
                titleTxt = (TextView) v.findViewById(R.id.item_event_title_txt);
                numTxt = (TextView) v.findViewById(R.id.item_event_num_txt);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mViewHolder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_event_grid_item, parent, false);
                mViewHolder = new ViewHolder(convertView);
                convertView.setTag(mViewHolder);
            } else {
                mViewHolder = (ViewHolder) convertView.getTag();
            }

            if (Integer.parseInt(pageEvents.get(position).getCount()) <= 0) {
                mViewHolder.numTxt.setTextColor(getResources().getColor(R.color.common_text_color));
            } else {
                mViewHolder.numTxt.setTextColor(getResources().getColor(R.color.event_selected_text_color));
            }

            mViewHolder.titleTxt.setText(pageEvents.get(position).getName());
            mViewHolder.numTxt.setText(pageEvents.get(position).getCount());
            return convertView;
        }
    }

    /**
     * viewPager adapter event的viewpager
     */
    class EventViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return eventGridViews.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(eventGridViews.get(position));
            return eventGridViews.get(position);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

    }

    /**
     * notify的veiwPagerAdapter
     */
    class NotifyViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return notifys.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            String title = notifys.get(position).getTitle();
            //TODO 文字已读未读功能取消
//            int read = notifys.get(position).getIsRead();
//            //如果已经阅读
//            if (read == 1) {
//                //文字变细
//                TextPaint paint = notifyTxts.get(position).getPaint();
//                paint.setFakeBoldText(false);
//            } else {
//                //文字加粗
//                TextPaint paint = notifyTxts.get(position).getPaint();
//                paint.setFakeBoldText(true);
//            }
            notifyTxts.get(position).setTextColor(getResources().getColor(R.color.actionBar_bg));
            notifyTxts.get(position).setText(title);
            container.addView(notifyTxts.get(position));
            return notifyTxts.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }


    /**
     * 第二次进入fragment刷新
     */
    @Override
    public void onResume() {
        super.onResume();

        if (isReasum) {
            apps = appDao.queryOrderByTopSeq();
            recommendAdapter.setNewItem(apps);

            mPtrLayout.autoRefresh();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dealtDao != null) {
            dealtDao.closeDB();
        }
        if (appDao != null) {
            appDao.closeDB();
        }
        if (publicizeDao != null) {
            publicizeDao.closeDB();
        }
        OkHttpUtils.getInstance().cancelTag(this);
        //销毁单例
        CheckMyApps.getInstance(getActivity()).destoryCheckMyApp();
    }
}
