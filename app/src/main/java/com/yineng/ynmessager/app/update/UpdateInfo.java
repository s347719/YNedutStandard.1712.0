//***************************************************************
//*    2015-9-8  上午11:56:47
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.app.update;

import java.io.Serializable;

/**
 * @author 胡毅
 *
 */
public class UpdateInfo implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -197570894840246039L;

	/** 异常提示信息. */
	private String message="操作成功";

	/**
	 * 状态 001 ： 操作失败！ 002 ： 没有最新版本信息！ 0 ：操作成功！默认状态
	 */
	private String status="0"; 
	
	private ProductInfo result;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public ProductInfo getResult() {
		return result;
	}

	public void setResult(ProductInfo result) {
		this.result = result;
	}
	
}
