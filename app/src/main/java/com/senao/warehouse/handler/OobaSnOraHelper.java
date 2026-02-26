package com.senao.warehouse.handler;

import com.senao.warehouse.database.BasicHelper;

public class OobaSnOraHelper extends BasicHelper {
	//Oracle
	private String salesDate="No data"; //retrun 銷貨日DATE
	private int shippingDN; //出貨DN NUM
	private String shippingCustom="No data"; //出貨客戶var
	private String shippingBoxNum="No data"; //出貨箱號var
	private String shippingVerNum="No data"; //出貨版號num
	private int shippingOE; //出貨OE num
	private String shippingPo="No data"; //出貨PO var
	private String p_status="No data"; //Oracle 狀態
	private String errorMsg;
	private int errorCode;
	private String P_SERIAL_NO; //SN
	private String P_box_NO; //箱號
	private String P_item_NO; //料號

	public String getP_item_NO() {
		return P_item_NO;
	}

	public void setP_item_NO(String p_item_NO) {
		P_item_NO = p_item_NO;
	}

	public String getP_SERIAL_NO() {
		return P_SERIAL_NO;
	}

	public void setP_SERIAL_NO(String p_SERIAL_NO) {
		P_SERIAL_NO = p_SERIAL_NO;
	}

	public String getP_box_NO() {
		return P_box_NO;
	}

	public void setP_box_NO(String p_box_NO) {
		P_box_NO = p_box_NO;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getSalesDate() {
		return salesDate;
	}

	public void setSalesDate(String salesDate) {
		this.salesDate = salesDate;
	}

	public int getShippingDN() {
		return shippingDN;
	}

	public void setShippingDN(int shippingDN) {
		this.shippingDN = shippingDN;
	}

	public String getShippingCustom() {
		return shippingCustom;
	}

	public void setShippingCustom(String shippingCustom) {
		this.shippingCustom = shippingCustom;
	}

	public String getShippingBoxNum() {
		return shippingBoxNum;
	}

	public void setShippingBoxNum(String shippingBoxNum) {
		this.shippingBoxNum = shippingBoxNum;
	}

	public String getShippingVerNum() {
		return shippingVerNum;
	}

	public void setShippingVerNum(String shippingVerNum) {
		this.shippingVerNum = shippingVerNum;
	}

	public int getShippingOE() {
		return shippingOE;
	}

	public void setShippingOE(int shippingOE) {
		this.shippingOE = shippingOE;
	}

	public String getShippingPo() {
		return shippingPo;
	}

	public void setShippingPo(String shippingPo) {
		this.shippingPo = shippingPo;
	}

	public String getP_status() {
		return p_status;
	}

	public void setP_status(String p_status) {
		this.p_status = p_status;
	}

	@Override
	public String toString() {
		return "OobaSnOraHelper [salesDate=" + salesDate +
				", shippingDN=" + shippingDN +
				", shippingCustom=" + shippingCustom +
				", shippingBoxNum=" + shippingBoxNum +
				", shippingVerNum=" + shippingVerNum +
				", shippingOE=" + shippingOE +
				", shippingPo=" + shippingPo +
				", p_status=" + p_status +
				"]";
	}
}
