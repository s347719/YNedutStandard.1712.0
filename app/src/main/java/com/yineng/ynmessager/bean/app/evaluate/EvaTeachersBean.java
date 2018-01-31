//***************************************************************
//*    2015-10-20  下午5:00:59
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.bean.app.evaluate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 胡毅
 *
 */
public class EvaTeachersBean {

	/**
	 * 被评人总人数
	 */
	private String totalEvaluate;
	
	/**
	 * 已评人数
	 */
	private String evaedcount;
	
	/**
	 * 被评人列表
	 */
	public List<EvaPersonBean> wjEvaluateVO = new ArrayList<EvaPersonBean>();

	public String getTotalEvaluate() {
		return totalEvaluate;
	}

	public void setTotalEvaluate(String totalEvaluate) {
		this.totalEvaluate = totalEvaluate;
	}

	public String getEvaedcount() {
		return evaedcount;
	}

	public void setEvaedcount(String evaedcount) {
		this.evaedcount = evaedcount;
	}

	public List<EvaPersonBean> getWjEvaluateVO() {
		return wjEvaluateVO;
	}

	public void setWjEvaluateVO(List<EvaPersonBean> wjEvaluateVO) {
		this.wjEvaluateVO = wjEvaluateVO;
	}
}
