package com.yineng.ynmessager.activity.event;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.event.DoneEvent;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;

import java.util.List;


/**
 * Created by 胡毅 on 2016/03/18 15:31.
 */
class DemandListAdapter extends RecyclerView.Adapter<DemandListAdapter.ViewHolder> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<DoneEvent> mData;
    private OnItemClickListener mOnItemClickListener;
    private View.OnClickListener onClickListener;

    DemandListAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.item_main_event_demand_listitem, parent, false);
        View demand_top = view.findViewById(R.id.demand_top);
        final ViewHolder holder = new ViewHolder(view);
        demand_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(holder.getLayoutPosition(), holder);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final DoneEvent demandEventObj = getItem(position);
        if (demandEventObj == null) {
            return;
        }
        holder.demand_name.setText(demandEventObj.getName());
        //TODO 暂时去取消灰色逻辑，因为需求不明确
//        if (demandEventObj.getIsOnlyPcView()){
//            holder.demand_name.setTextColor(mContext.getResources().getColor(R.color.common_text_color));
//            holder.demand_hole_view.setBackgroundColor(mContext.getResources().getColor(R.color.whitesmoke));
//        }else {
        holder.demand_name.setTextColor(mContext.getResources().getColor(R.color.common_black_color));
        holder.demand_hole_view.setBackgroundColor(mContext.getResources().getColor(R.color.white));
//        }
        holder.demand_time.setText(mContext.getResources().getString(R.string.event_demand_review_reviewTime, demandEventObj.getCreateTime()));
        String curChecker = demandEventObj.getCurChecker();
        if (demandEventObj.getStatus() == 1 && (!TextUtils.isEmpty(curChecker) || !"null".equals(curChecker) || curChecker == null)) {
            holder.demand_curcheck.setVisibility(View.VISIBLE);
            holder.demand_curcheck.setText(mContext.getResources().getString(R.string.event_demand_review_curperson, curChecker));
        } else {
            holder.demand_curcheck.setVisibility(View.GONE);
        }
        String time = "null".equals(demandEventObj.getReviewTime()) || demandEventObj.getReviewTime() == null ? "无" : demandEventObj.getReviewTime();
        String preChecker = demandEventObj.getPreChecker();
        if (!TextUtils.isEmpty(preChecker)) {
            holder.has_review_person.setVisibility(View.VISIBLE);
            holder.demend_review_show_view.setVisibility(View.VISIBLE);
            holder.demand_precheck.setVisibility(View.VISIBLE);
            holder.demand_precheck.setText(mContext.getResources().getString(R.string.event_demand_review_preperson, demandEventObj.getPreChecker()) + "(" + time + ")");
        } else {
            holder.has_review_person.setVisibility(View.GONE);
            holder.demend_review_show_view.setVisibility(View.GONE);
            holder.demand_precheck.setVisibility(View.GONE);
        }

        int drawable;
        switch (demandEventObj.getStatus()) {
            case -1:
                drawable = R.mipmap.oa_send_request_back;
                if (demandEventObj.getIsOnlyPcView()) {
                    holder.has_review_person.setVisibility(View.VISIBLE);
                    holder.demand_only_hint_computer.setVisibility(View.VISIBLE);
                    holder.demand_computer_text_hint.setText("只能在电脑上查看");
                } else {
                    holder.demand_only_hint_computer.setVisibility(View.GONE);
                    if (!TextUtils.isEmpty(preChecker)) {
                        holder.demend_review_show_view.setVisibility(View.VISIBLE);
                    } else {
                        holder.has_review_person.setVisibility(View.GONE);
                        holder.demend_review_show_view.setVisibility(View.GONE);
                    }
                }
                break;
            case 0:
                drawable = R.mipmap.oa_send_request_repulse;
                if (demandEventObj.getIsOnlyPcModify()) {
                    holder.has_review_person.setVisibility(View.VISIBLE);
                    holder.demand_only_hint_computer.setVisibility(View.VISIBLE);
                    holder.demand_computer_text_hint.setText("只能在电脑上修改");
                } else {
                    holder.demand_only_hint_computer.setVisibility(View.GONE);
                    if (!TextUtils.isEmpty(preChecker)) {
                        holder.demend_review_show_view.setVisibility(View.VISIBLE);
                    } else {
                        holder.has_review_person.setVisibility(View.GONE);
                        holder.demend_review_show_view.setVisibility(View.GONE);
                    }
                }
                break;
            case 1:
                drawable = R.mipmap.oa_send_request_checking;
                if (demandEventObj.getIsOnlyPcView()) {
                    holder.has_review_person.setVisibility(View.VISIBLE);
                    holder.demand_only_hint_computer.setVisibility(View.VISIBLE);
                    holder.demand_computer_text_hint.setText("只能在电脑上查看");
                } else {
                    holder.demand_only_hint_computer.setVisibility(View.GONE);
                    if (!TextUtils.isEmpty(preChecker)) {
                        holder.demend_review_show_view.setVisibility(View.VISIBLE);
                    } else {
                        holder.has_review_person.setVisibility(View.GONE);
                        holder.demend_review_show_view.setVisibility(View.GONE);
                    }
                }
                break;
            case 2:
                drawable = R.mipmap.oa_send_request_pass;
                if (demandEventObj.getIsOnlyPcView()) {
                    holder.has_review_person.setVisibility(View.VISIBLE);
                    holder.demand_only_hint_computer.setVisibility(View.VISIBLE);
                    holder.demand_computer_text_hint.setText("只能在电脑上查看");
                } else {
                    holder.demand_only_hint_computer.setVisibility(View.GONE);
                    if (!TextUtils.isEmpty(preChecker)) {
                        holder.demend_review_show_view.setVisibility(View.VISIBLE);
                    } else {
                        holder.has_review_person.setVisibility(View.GONE);
                        holder.demend_review_show_view.setVisibility(View.GONE);
                    }
                }
                break;
            default:
                drawable = R.mipmap.oa_send_request_repulse;
                holder.demand_only_hint_computer.setVisibility(View.VISIBLE);
                holder.demand_computer_text_hint.setText("");
                break;

        }
        holder.demand_status.setImageResource(drawable);
        holder.review_image.setImageResource(R.mipmap.event_review_down);

        if (demandEventObj.isExpand()) {
            holder.demand_review_view.setVisibility(View.VISIBLE);
            String reviewAdvice = demandEventObj.getReviewAdvice();
            String advice = "null".equals(reviewAdvice) || reviewAdvice == null || TextUtils.isEmpty(reviewAdvice) ? "无" : demandEventObj.getReviewAdvice();
            holder.demand_review_text.setText(advice);
            holder.review_image.setImageResource(R.mipmap.event_review_up);
        } else {
            holder.demand_review_view.setVisibility(View.GONE);
            holder.review_image.setImageResource(R.mipmap.event_review_down);
        }
        holder.demend_review_show_view.setTag(position);
        holder.demend_review_show_view.setOnClickListener(onClickListener);

    }


    public void setItem(int pos, DoneEvent demandEvent) {
        mData.set(pos, demandEvent);
    }

    public DoneEvent getItem(int position) {
        return mData.get(position);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public List<DoneEvent> getData() {
        return mData;
    }

    public void setData(List<DoneEvent> data) {
        mData = data;
    }

    public OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView demand_name;
        TextView demand_time;
        TextView demand_curcheck;
        TextView demand_precheck;
        ImageView demand_status;
        View demend_review_show_view;
        ImageView review_image;
        View demand_only_hint_computer;
        TextView demand_computer_text_hint;
        View demand_review_view;
        TextView demand_review_text;
        View demand_hole_view;
        View has_review_person;

        public ViewHolder(View itemView) {
            super(itemView);
            demand_name = (TextView) itemView.findViewById(R.id.demand_name);
            demand_time = (TextView) itemView.findViewById(R.id.demand_time);
            demand_curcheck = (TextView) itemView.findViewById(R.id.demand_curcheck);
            demand_precheck = (TextView) itemView.findViewById(R.id.demand_precheck);
            demand_status = (ImageView) itemView.findViewById(R.id.demand_status);
            demend_review_show_view = itemView.findViewById(R.id.demend_review_show_view);
            review_image = (ImageView) itemView.findViewById(R.id.demand_review_image);
            demand_only_hint_computer = itemView.findViewById(R.id.demand_only_hint_computer);
            demand_computer_text_hint = (TextView) itemView.findViewById(R.id.demand_computer_text_hint);
            demand_review_view = itemView.findViewById(R.id.demand_review_view);
            demand_hole_view = itemView.findViewById(R.id.demand_hole_view);
            has_review_person = itemView.findViewById(R.id.has_review_person);
            demand_review_text = (TextView) itemView.findViewById(R.id.demand_review_text);

        }
    }
}
