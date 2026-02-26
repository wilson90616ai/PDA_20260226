package com.senao.warehouse.database;

import java.util.Set;

public class SnItemInfoHelper extends BasicHelper {
	private int deliveryID;
	private int itemID;
	private String itemNo;
	private int typeFlag;
	private String oldPallet;
	private String oldCarton;
	private String palletNo;
	private String cartonNo;
	private double boxWeight;
	private String serialNo;
	private String customer;
	private String rev;
	private String po;
	private String oe;
	private int oeID;
	private String lotcode;
	private String total_sn;

	public String getTotal_sn() {
		return total_sn;
	}

	public void setTotal_sn(String total_sn) {
		this.total_sn = total_sn;
	}

	public String getLotcode() {
		return lotcode;
	}

	public void setLotcode(String lotcode) {
		this.lotcode = lotcode;
	}

	public String getPo() {
		return po;
	}

	public void setPo(String po) {
		this.po = po;
	}

	public String getOe() {
		return oe;
	}

	public void setOe(String oe) {
		this.oe = oe;
	}

	public int getTypeFlag() {
		return typeFlag;
	}

	public void setTypeFlag(int typeFlag) {
		this.typeFlag = typeFlag;
	}

	public String getOldPallet() {
		return oldPallet;
	}

	public void setOldPallet(String oldPallet) {
		this.oldPallet = oldPallet;
	}

	public String getOldCarton() {
		return oldCarton;
	}

	public void setOldCarton(String oldCarton) {
		this.oldCarton = oldCarton;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public int getDeliveryID() {
		return deliveryID;
	}

	public void setDeliveryID(int deliveryID) {
		this.deliveryID = deliveryID;
	}

	public int getItemID() {
		return itemID;
	}

	public void setItemID(int itemID) {
		this.itemID = itemID;
	}

	public String getPalletNo() {
		return palletNo;
	}

	public void setPalletNo(String palletNo) {
		this.palletNo = palletNo;
	}

	public String getCartonNo() {
		return cartonNo;
	}

	public void setCartonNo(String cartonNo) {
		this.cartonNo = cartonNo;
	}

	public double getBoxWeight() {
		return boxWeight;
	}

	public void setBoxWeight(double boxWeight) {
		this.boxWeight = boxWeight;
	}

	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	public String getItemNo() {
		return itemNo;
	}

	public void setItemNo(String itemNo) {
		this.itemNo = itemNo;
	}

	public String getRev() {
		return rev;
	}

	public void setRev(String rev) {
		this.rev = rev;
	}

	public int getOeID() {
		return oeID;
	}

	public void setOeID(int oeID) {
		this.oeID = oeID;
	}
}
