package com.yineng.ynmessager.bean;

import java.util.ArrayList;
import java.util.List;


/**
 * 初始化信息配置
 * @author 胡毅
 *
 */
public class ClientInitConfig {
	private String disgroup_max_user; // 讨论组的最大人数
	private String group_max_user; // 群的最大人数
	private String max_disdisgroup_can_create; // 可创建的最大讨论组个数
	private String max_group_can_create; // 可创建的最大群个数
	private int org_update_type; // 组织机构数据更新方式，如果为0则表示全量更新，1则表示增量更新
	private Object rightList;
	private String servertime; //获取到组织机构后得到的服务器时间
	private List<AddressBean> addressList = new ArrayList<AddressBean>();
	
	public ClientInitConfig() {
		super();
	}

	public ClientInitConfig(String disgroup_max_user, String group_max_user,
			String max_disdisgroup_can_create, String max_group_can_create,
			int org_update_type) {
		super();
		this.disgroup_max_user = disgroup_max_user;
		this.group_max_user = group_max_user;
		this.max_disdisgroup_can_create = max_disdisgroup_can_create;
		this.max_group_can_create = max_group_can_create;
		this.org_update_type = org_update_type;
	}

	public String getDisgroup_max_user() {
		return disgroup_max_user;
	}

	public void setDisgroup_max_user(String disgroup_max_user) {
		this.disgroup_max_user = disgroup_max_user;
	}

	public String getGroup_max_user() {
		return group_max_user;
	}

	public void setGroup_max_user(String group_max_user) {
		this.group_max_user = group_max_user;
	}

	public String getMax_disdisgroup_can_create() {
		return max_disdisgroup_can_create;
	}

	public void setMax_disdisgroup_can_create(String max_disdisgroup_can_create) {
		this.max_disdisgroup_can_create = max_disdisgroup_can_create;
	}

	public String getMax_group_can_create() {
		return max_group_can_create;
	}

	public void setMax_group_can_create(String max_group_can_create) {
		this.max_group_can_create = max_group_can_create;
	}

	public int getOrg_update_type() {
		return org_update_type;
	}

	public void setOrg_update_type(int org_update_type) {
		this.org_update_type = org_update_type;
	}

	public Object getRightList() {
		return rightList;
	}

	public void setRightList(Object rightList) {
		this.rightList = rightList;
	}

	public String getServertime() {
		return servertime;
	}

	public void setServertime(String servertime) {
		this.servertime = servertime;
	}
	
	public List<AddressBean> getAddressList() {
		return addressList;
	}

	public void setAddressList(List<AddressBean> addressList) {
		this.addressList = addressList;
	}

	public class AddressBean {
		private String ipaddress;
		private int key;
		private int port;
		private String remark;
		private String server;
		private int type;// type ==3 代表是云地端内外网整合参数

		public String getIpaddress() {
			return ipaddress;
		}

		public void setIpaddress(String ipaddress) {
			this.ipaddress = ipaddress;
		}

		public int getKey() {
			return key;
		}

		public void setKey(int key) {
			this.key = key;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public String getRemark() {
			return remark;
		}

		public void setRemark(String remark) {
			this.remark = remark;
		}

		public String getServer() {
			return server;
		}

		public void setServer(String server) {
			this.server = server;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

	}
	
}
