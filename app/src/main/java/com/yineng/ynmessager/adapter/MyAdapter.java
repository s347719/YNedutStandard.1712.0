package com.yineng.ynmessager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.login.LoginConfig;
import com.yineng.ynmessager.bean.login.LoginUser;
import com.yineng.ynmessager.db.dao.LoginUserDao;

import java.util.Iterator;
import java.util.List;

/**
 * Created by yn on 2017/4/19.
 */
public class MyAdapter extends BaseAdapter {

    private Context context;
    private List<LoginUser> mData;
    private ViewHolder holder;
    private OnClicklistener onClicklistener;

    public MyAdapter(Context context, List<LoginUser> mData, OnClicklistener onClicklistener) {
        this.context = context;
        this.mData = mData;
        //过滤从未用帐号密码登陆过的帐号
        Iterator<LoginUser> iterator = this.mData.iterator();
        while(iterator.hasNext()){
            LoginUser user = iterator.next();
            if(user.getIsUserAccountType()==0){
               iterator.remove();
            }
        }
        this.onClicklistener=onClicklistener;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_login_seclet_account, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.item_username.setText(mData.get(position).getAccount());
        holder.item_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClicklistener.OnClicklistener(mData.get(position).getAccount(),mData.get(position));
            }
        });
        return view;
    }

    class ViewHolder {
        LinearLayout item_bg;
        TextView item_username;
        ImageView item_delete;

        public ViewHolder(View view) {
            item_bg = (LinearLayout) view.findViewById(R.id.item_bg);
            item_username = (TextView) view.findViewById(R.id.item_user_name);
            item_delete = (ImageView) view.findViewById(R.id.item_delete_history);
        }
    }

    public interface OnClicklistener {
        void OnClicklistener(String account, LoginUser user);
    }
}
