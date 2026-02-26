package com.senao.warehouse.handler;

public class OobaSnMsHelper {
	//MSSQL
	private String q_no; //檢驗單號
	private String mo; //製令單號
	private int qty; //套數
	private String item_desc="No data"; //品名號
	private String plan_date="No data"; //預計出貨日
	private String q_date="No data"; //檢驗日期
	private String mfg_date="No data"; //生產日期
	private String stock_date="No data"; //入庫日期
	private String ship_date="No data"; //出貨日期
	private String sc001="No data"; //入庫單號
	private String mf036="No data"; //PO號碼
	private String mo017="No data"; //棧板號碼
	private String state="No data"; //狀態
	private OobaSnOraHelper oraHelper;
	private String itemNo="No data"; //料號
	private String itemSn="No data"; //序號
	private String MO012="No data"; //箱號
	private String MO012C; //箱子數量
	private String MO017C; //棧板數量
	private String sndata; //箱子裡面的序號

	public String getMO012() {
		return MO012;
	}

	public void setMO012(String mO012) {
		MO012 = mO012;
	}

	public String getMO012C() {
		return MO012C;
	}

	public void setMO012C(String mO012C) {
		MO012C = mO012C;
	}

	public String getMO017C() {
		return MO017C;
	}

	public void setMO017C(String mO017C) {
		MO017C = mO017C;
	}

	public String getSndata() {
		return sndata;
	}

	public void setSndata(String sndata) {
		this.sndata = sndata;
	}
	

	public String getItemNo() {
		return itemNo;
	}

	public void setItemNo(String itemNo) {
		this.itemNo = itemNo;
	}

	public String getItemSn() {
		return itemSn;
	}

	public void setItemSn(String itemSn) {
		this.itemSn = itemSn;
	}

	public OobaSnOraHelper getOraHelper() {
		return oraHelper;
	}

	public void setOraHelper(OobaSnOraHelper oraHelper) { this.oraHelper = oraHelper; }

	public String getQ_no() {
		return q_no;
	}

	public void setQ_no(String q_no) {
		this.q_no = q_no;
	}

	public String getMo() {
		return mo;
	}

	public void setMo(String mo) {
		this.mo = mo;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public String getItem_desc() {
		return item_desc;
	}

	public void setItem_desc(String item_desc) {
		this.item_desc = item_desc;
	}

	public String getPlan_date() {
		return plan_date;
	}

	public void setPlan_date(String plan_date) {
		this.plan_date = plan_date;
	}

	public String getQ_date() {
		return q_date;
	}

	public void setQ_date(String q_date) {
		this.q_date = q_date;
	}

	public String getMfg_date() {
		return mfg_date;
	}

	public void setMfg_date(String mfg_date) {
		this.mfg_date = mfg_date;
	}

	public String getStock_date() {
		return stock_date;
	}

	public void setStock_date(String stock_date) {
		this.stock_date = stock_date;
	}

	public String getShip_date() {
		return ship_date;
	}

	public void setShip_date(String ship_date) {
		this.ship_date = ship_date;
	}

	public String getSc001() {
		return sc001;
	}

	public void setSc001(String sc001) {
		this.sc001 = sc001;
	}

	public String getMf036() {
		return mf036;
	}

	public void setMf036(String mf036) {
		this.mf036 = mf036;
	}

	public String getMo017() {
		return mo017;
	}

	public void setMo017(String mo017) {
		this.mo017 = mo017;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
