package com.yineng.ynmessager.activity.app;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.app.NewMyApps;
import com.yineng.ynmessager.db.NewMyAppsTb;
import com.yineng.ynmessager.db.dao.NewMyAppsDao;
import com.yineng.ynmessager.util.InputUtil;
import com.yineng.ynmessager.util.L;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yn on 2017/7/7.
 */

public class AppSearchFragment extends DialogFragment implements View.OnClickListener, TextWatcher, AdapterView.OnItemClickListener {

    private TextView dismissBtn;
    private ListView searchListView, recentList;
    private EditText searchEdit;
    private NewMyAppsDao dao;
    private SearchAdapter adapter, recentAdapter;
    private List<NewMyApps> apps = new ArrayList<>();
    private LinkedList<NewMyApps> recentApps = new LinkedList<>();
    private RelativeLayout searchBox;
    private LinearLayout recentLin;
    private View emptyView; //显示空的页面
    private final int RECENT_COUNT = 8; //首页应用最多显示几个
    private View dialogView;

    interface OnDialogFragmentDismiss {
        void onDismiss();
    }

    private OnDialogFragmentDismiss onDialogFragmentDismiss;

    public void setOnDialogFragmentDismiss(OnDialogFragmentDismiss onDialogFragmentDismiss) {
        this.onDialogFragmentDismiss = onDialogFragmentDismiss;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        dao = new NewMyAppsTb(getActivity());
        dialogView = inflater.inflate(R.layout.fragment_search_app, container, false);
        return dialogView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        dismissBtn = (TextView) view.findViewById(R.id.dismiss_btn);
        searchListView = (ListView) view.findViewById(R.id.app_search_list);
        searchEdit = (EditText) view.findViewById(R.id.app_search_edit);
        recentList = (ListView) view.findViewById(R.id.app_recent_list);
        recentLin = (LinearLayout) view.findViewById(R.id.search_app_recent_linear);
        emptyView = view.findViewById(R.id.emptyview);
        searchBox = (RelativeLayout) view.findViewById(R.id.appSearchBox);
        searchBox.setOnClickListener(this);

        searchEdit.addTextChangedListener(this);
        dismissBtn.setOnClickListener(this);
        searchListView.setOnItemClickListener(this);
        recentList.setOnItemClickListener(onItemClickListener);

        adapter = new SearchAdapter();
        searchListView.setAdapter(adapter);

        LinkedList<NewMyApps> app = dao.queryOrderByDate();
        for (int i = 0; i < app.size(); i++) {
            if (i < RECENT_COUNT && !StringUtils.isEmpty(app.get(i).getLastUseDate())) {
                recentApps.add(app.get(i));
            }
        }
        recentAdapter = new SearchAdapter(recentApps);
        recentList.setAdapter(recentAdapter);

        //延迟弹出键盘
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                searchEdit.setFocusable(true);
                searchEdit.setFocusableInTouchMode(true);
                searchEdit.requestFocus();
                InputUtil.ShowKeyboard(searchEdit);
            }
        }, 300);
    }


    /**
     * 点击app跳转
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        hideKeyBoard();
        NewMyApps app = apps.get(position);
        //app跳转
        boolean isMenu = !StringUtils.isEmpty(app.getSubmenu());
        CheckMyApps.getInstance(getActivity()).JumpApp(app, isMenu);
    }

    /**
     * 最近应用点击跳转
     */
    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            NewMyApps app = recentApps.get(position);
            //app跳转
            boolean isMenu = !StringUtils.isEmpty(app.getSubmenu());
            CheckMyApps.getInstance(getActivity()).JumpApp(app, isMenu);
        }
    };


    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawable(null);

            Window window = getDialog().getWindow();
            WindowManager.LayoutParams windowParams = window.getAttributes();
            windowParams.dimAmount = 0.0f;

            window.setAttributes(windowParams);
        }
    }

    /*****************************************edittext 监听事件******************************/

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String name = s.toString();
        if (StringUtils.isEmpty(name)) {
            return;
        }

        apps = dao.queryByLike(name);
        //隐藏最近
        recentLin.setVisibility(View.GONE);
        searchListView.setEmptyView(emptyView);
        adapter.setData(apps);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /*****************************************edittext 监听事件 end******************************/


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dismiss_btn:
                hideKeyBoard();
                dismiss();
                break;
            case R.id.appSearchBox:
                searchEdit.clearFocus();
                searchEdit.setFocusable(true);
                break;
        }
    }

    /**
     * 隐藏软键盘
     */
    private void hideKeyBoard(){
        //隐藏软键盘
        searchEdit.setText("");
        searchEdit.clearFocus();
        InputUtil.HideKeyboard(searchEdit);
    }

    class SearchAdapter extends BaseAdapter {

        private List<NewMyApps> apps = new ArrayList<>();

        public SearchAdapter() {

        }

        public SearchAdapter(List<NewMyApps> apps) {
            this.apps = apps;
        }

        public void setData(List<NewMyApps> apps) {
            this.apps = apps;
            notifyDataSetChanged();
        }

        class ViewHolder {
            ImageView icon;
            TextView name;

            public ViewHolder(View v) {
                icon = (ImageView) v.findViewById(R.id.search_app_icon);
                name = (TextView) v.findViewById(R.id.search_app_title);
            }
        }

        @Override
        public int getCount() {
            return apps.size();
        }

        @Override
        public NewMyApps getItem(int position) {
            return apps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mViewHolder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_search_app, parent, false);
                mViewHolder = new ViewHolder(convertView);
                convertView.setTag(mViewHolder);
            } else {
                mViewHolder = (ViewHolder) convertView.getTag();
            }

            ImageView icon = mViewHolder.icon;
            TextView title = mViewHolder.name;

            title.setText(apps.get(position).getName());
            //根据菜单ID来查找对应的资源图片
            int drawAbleID = getActivity().getResources().getIdentifier(apps.get(position).getIcon(), "mipmap", getActivity().getPackageName());
            if (drawAbleID != 0) {
                try {
                    icon.setImageResource(drawAbleID);
                } catch (Resources.NotFoundException e) {
                    L.e(this.getClass(), e.getMessage());
                    NewMyApps.randomIcon(apps.get(position), icon);
                }
            } else {
                NewMyApps.randomIcon(apps.get(position), icon);
            }

            return convertView;
        }
    }

    /**
     * dismiss监听
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (onDialogFragmentDismiss != null) {
            onDialogFragmentDismiss.onDismiss();
        }
    }
}
