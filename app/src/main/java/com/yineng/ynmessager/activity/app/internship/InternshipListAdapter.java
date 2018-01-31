package com.yineng.ynmessager.activity.app.internship;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.app.Internship.InternshipAct;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.L;
import java.util.List;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * Created by 贺毅柳 on 2015/12/14 16:46.
 */
public class InternshipListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int VIEW_TYPE_ITEM = 0;
    public static final int VIEW_TYPE_LOADING = 1;
    private static final String TAG = "InternshipListAdapter";
    // The minimum amount of items to have below your current scroll position before loading more.
    private static final int VISIBLE_THRESHOLD = 1;
    private Context mContext;
    private LayoutInflater mInflater;
    private List<InternshipAct> mData;
    private ItemViewHolder.onChildClickListener mChildOnClickListener;
    private int mLastVisibleItem, totalItemCount;
    private boolean mLoading;
    private OnLoadMoreListener mOnLoadMoreListener;
    private int mUserType;

    InternshipListAdapter(Context context, RecyclerView recyclerView) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mUserType = LastLoginUserSP.getUserType(context);

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = linearLayoutManager.getItemCount();
                    mLastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!mLoading && totalItemCount <= (mLastVisibleItem + VISIBLE_THRESHOLD)) {
                        L.d(TAG, "the end of this list has been reached");
                        // End has been reached
                        // Do something
                        if (mOnLoadMoreListener != null) {
                            mOnLoadMoreListener.onLoadMore();
                        }
                        mLoading = true;
                    }
                }
            });
        }
    }

    @Override public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override public int getItemViewType(int position) {
        return getItem(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View itemView = mInflater.inflate(R.layout.item_internship_act_list, parent, false);
            final ItemViewHolder holder = new ItemViewHolder(itemView);

            holder.dailyReport.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (mChildOnClickListener != null) {
                        int position = holder.getLayoutPosition();
                        mChildOnClickListener.onDaily(position, getItem(position));
                    }
                }
            });

            holder.weeklyReport.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (mChildOnClickListener != null) {
                        int position = holder.getLayoutPosition();
                        mChildOnClickListener.onWeekly(position, getItem(position));
                    }
                }
            });

            holder.monthlyReport.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (mChildOnClickListener != null) {
                        int position = holder.getLayoutPosition();
                        mChildOnClickListener.onMonthly(position, getItem(position));
                    }
                }
            });
            return holder;
        } else if (viewType == VIEW_TYPE_LOADING) {
            View loadingView = mInflater.inflate(R.layout.item_internship_act_list_loading, parent, false);
            return new LoadingViewHolder(loadingView);
        }

        return null;
    }

    @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder _itemHolder = (ItemViewHolder) holder;
            InternshipAct act = getItem(position);

            Context _context = mContext;

            String startTime = DateFormatUtils.format(act.getStartDate(), "yyyy/MM/dd");
            String endTime = DateFormatUtils.format(act.getEndDate(), "yyyy/MM/dd");
            _itemHolder.timeLimit.setText(_context.getString(R.string.internshipList_timeLimit, startTime, endTime));
            _itemHolder.title.setText(act.getTitle());

            //指导老师填报时不显示“指导老师”和“实习企业”
            if (mUserType == 1) {
                _itemHolder.teacher.setVisibility(View.GONE);
                _itemHolder.company.setVisibility(View.GONE);
                _itemHolder.line1.setVisibility(View.GONE);
            } else {
                _itemHolder.teacher.setVisibility(View.VISIBLE);
                _itemHolder.company.setVisibility(View.VISIBLE);
                _itemHolder.line1.setVisibility(View.INVISIBLE);

                _itemHolder.teacher.setText(_context.getString(R.string.internshipList_teacher, act.getTeacher()));
                _itemHolder.company.setText(_context.getString(R.string.internshipList_company, act.getCompany()));
            }

            boolean hasDaily = act.isHasDaily();
            boolean hasWeekly = act.isHasWeekly();
            boolean hasMonthly = act.isHasMonthly();
            boolean enableReport = hasDaily || hasWeekly || hasMonthly;

            _itemHolder.dailyReport.setVisibility(hasDaily ? View.VISIBLE : View.INVISIBLE);
            _itemHolder.weeklyReport.setVisibility(hasWeekly ? View.VISIBLE : View.INVISIBLE);
            _itemHolder.monthlyReport.setVisibility(hasMonthly ? View.VISIBLE : View.INVISIBLE);
            _itemHolder.noneToReport.setVisibility(!enableReport ? View.VISIBLE : View.INVISIBLE);
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder _loadingHolder = (LoadingViewHolder) holder;
            _loadingHolder.progressBar.setIndeterminate(true);
        }
    }

    List<InternshipAct> getData() {
        return mData;
    }

    void setData(List<InternshipAct> data) {
        mData = data;
    }

    InternshipAct getItem(int position) {
        return mData.get(position);
    }

    void setOnItemChildClickListener(ItemViewHolder.onChildClickListener childOnClickListener) {
        mChildOnClickListener = childOnClickListener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.mOnLoadMoreListener = onLoadMoreListener;
    }

    public void setLoaded() {
        this.mLoading = false;
    }

    interface OnLoadMoreListener {
        void onLoadMore();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView timeLimit;
        TextView title;
        TextView teacher;
        TextView company;
        TextView monthlyReport;
        TextView weeklyReport;
        TextView dailyReport;
        TextView noneToReport;
        View line1;

        ItemViewHolder(View itemView) {
            super(itemView);
            timeLimit = (TextView) itemView.findViewById(R.id.internshipList_txt_contentList_item_timeLimit);
            title = (TextView) itemView.findViewById(R.id.internshipList_txt_contentList_item_title);
            teacher = (TextView) itemView.findViewById(R.id.internshipList_txt_contentList_item_teacher);
            company = (TextView) itemView.findViewById(R.id.internshipList_txt_contentList_item_company);
            monthlyReport = (TextView) itemView.findViewById(R.id.internshipList_txt_contentList_item_monthlyReport);
            weeklyReport = (TextView) itemView.findViewById(R.id.internshipList_txt_contentList_item_weeklyReport);
            dailyReport = (TextView) itemView.findViewById(R.id.internshipList_txt_contentList_item_dailyReport);
            noneToReport = (TextView) itemView.findViewById(R.id.internshipList_txt_contentList_item_noneToReport);
            line1 = itemView.findViewById(R.id.internshipList_view_contentList_item_line1);
        }

        interface onChildClickListener {
            void onDaily(int position, InternshipAct internshipAct);

            void onWeekly(int position, InternshipAct internshipAct);

            void onMonthly(int position, InternshipAct internshipAct);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        LoadingViewHolder(View itemView) {
            super(itemView);

            progressBar = (ProgressBar) itemView.findViewById(R.id.internshipList_pro_contentList_loading);
        }
    }
}
