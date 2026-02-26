package com.senao.warehouse.database;

import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.handler.OobaSnMsHelper;

import java.util.Date;
import java.util.List;

public class OobaSnHelper extends BasicHelper {
	private String cartonId_con; //裝箱號碼
	private String ship_status_con; //出貨狀態
	private String itemNo_con; //料號
	private String itemSn_con; //序號
	private String plan_date_con; //預計出貨日-起
	private String plan_date_con_end; //預計出貨日-結束
	private String mo_con; //製令單號
	private List<OobaSnMsHelper> msHelper;
	private String itemNo_erp_con;

	public String getItemNo_erp_con() {
		return itemNo_erp_con;
	}

	public void setItemNo_erp_con(String itemNo_erp_con) {
		this.itemNo_erp_con = itemNo_erp_con;
	}

	public List<OobaSnMsHelper> getMsHelper() {
		return msHelper;
	}

	public void setMsHelper(List<OobaSnMsHelper> msHelper) {
		this.msHelper = msHelper;
	}

	public String getPlan_date_con_end() {
		return plan_date_con_end;
	}

	public void setPlan_date_con_end(String plan_date_con_end) {
		this.plan_date_con_end = plan_date_con_end;
	}

	public String getCartonId_con() {
		return cartonId_con;
	}

	public void setCartonId_con(String cartonId_con) {
		this.cartonId_con = cartonId_con;
	}

	public String getShip_status_con() {
		return ship_status_con;
	}

	public void setShip_status_con(String ship_status_con) {
		this.ship_status_con = ship_status_con;
	}

	public String getItemNo_con() {
		return itemNo_con;
	}

	public void setItemNo_con(String itemNo_con) {
		this.itemNo_con = itemNo_con;
	}

	public String getItemSn_con() {
		return itemSn_con;
	}

	public void setItemSn_con(String itemSn_con) {
		this.itemSn_con = itemSn_con;
	}

	public String getPlan_date_con() {
		return plan_date_con;
	}

	public void setPlan_date_con(String plan_date_con) {
		this.plan_date_con = plan_date_con;
	}

	public String getMo_con() {
		return mo_con;
	}

	public void setMo_con(String mo_con) {
		this.mo_con = mo_con;
	}
}
