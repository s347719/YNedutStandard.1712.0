 package com.yineng.ynmessager.activity.dissession;

 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;

 import com.yineng.ynmessager.R;
 import com.yineng.ynmessager.bean.contact.OrganizationTree;

 import java.util.ArrayList;
 import java.util.List;

/** 
 * ClassName:OrgPathAdapter <br/> 
 * Function:  ADD FUNCTION. <br/>
 * Reason:    ADD REASON. <br/>
 * Date:     2015年3月18日 上午10:12:06 <br/> 
 * @author   YINENG 
 * @version   
 * @since    JDK 1.6 
 * @see       
 */
/** 
 * ClassName: OrgPathAdapter <br/> 
 * Function:  ADD FUNCTION. <br/>
 * Reason:  ADD REASON(可选). <br/>
 * date: 2015年3月18日 上午10:12:06 <br/> 
 * 
 * @author YINENG 
 * @version  
 * @since JDK 1.6 
 */
public class OrgPathAdapter extends BaseAdapter {

	private Context context;
	private List<OrganizationTree> list = new ArrayList<OrganizationTree>();

	public OrgPathAdapter(Context context) {
		this.context = context;
	}

	/**
	 * @param context 上下文
	 * @param mTitleOrgList 显示数据
	 */
	public OrgPathAdapter(Context context, ArrayList<OrganizationTree> mTitleOrgList) {
		this.context = context;
		this.list = mTitleOrgList;
	}

	public void setData(List<OrganizationTree> mTitleOrgList) {
		this.list = mTitleOrgList;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.contact_orgtitle_list_item, null);
			holder = new ViewHolder();
			convertView.setTag(holder);
			holder.mPopOrgName = (TextView) convertView
					.findViewById(R.id.tv_contact_title_poplist_item);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mPopOrgName.setText(list.get(position).getOrgName());
//		if (position == list.size() - 1) {
//			holder.mPopOrgName.setTextColor(Color.GRAY);
//		} else {
//			holder.mPopOrgName.setTextColor(Color.parseColor("#21a81b"));
//		}
//		holder.mPopOrgName.setTextColor(Color.parseColor("#21a81b"));
		return convertView;
	}

	class ViewHolder {
		TextView mPopOrgName;
	}
}
  