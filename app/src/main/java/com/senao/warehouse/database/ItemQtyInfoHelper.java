package com.senao.warehouse.database;

public class ItemQtyInfoHelper extends BasicHelper {
	private int itemID;
	private String customer;
	private int typeFlag;
	private String serialNo;
	private int snOnHand;
	private String snBoxPltNo;
	private int boxQty;
	private int deliveryID;
	private String itemNo;
	private String po;
	private String oe;
	private int oeID;
	private String SubLoc; //儲位

	public String getSubLoc() {
		return SubLoc;
	}

	public void setSubLoc(String subLoc) {
		SubLoc = subLoc;
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

	public int getItemID() {
		return itemID;
	}

	public void setItemID(int itemID) {
		this.itemID = itemID;
	}

	public int getTypeFlag() {
		return typeFlag;
	}

	public void setTypeFlag(int typeFlag) {
		this.typeFlag = typeFlag;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public int getSnOnHand() {
		return snOnHand;
	}

	public void setSnOnHand(int snOnHand) {
		this.snOnHand = snOnHand;
	}

	public String getSnBoxPltNo() {
		return snBoxPltNo;
	}

	public void setSnBoxPltNo(String snBoxPltNo) {
		this.snBoxPltNo = snBoxPltNo;
	}

	public int getBoxQty() {
		return boxQty;
	}

	public void setBoxQty(int boxQty) {
		this.boxQty = boxQty;
	}

	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	public int getDeliveryID() {
		return deliveryID;
	}

	public void setDeliveryID(int deliveryID) {
		this.deliveryID = deliveryID;
	}

	public String getItemNo() {
		return itemNo;
	}

	public void setItemNo(String itemNo) {
		this.itemNo = itemNo;
	}

	public int getOeID() {
		return oeID;
	}

	public void setOeID(int oeID) {
		this.oeID = oeID;
	}

	@Override
	public String toString() {
		return "ItemQtyInfoHelper{" +
				"itemID=" + itemID +
				", customer='" + customer + '\'' +
				", typeFlag=" + typeFlag +
				", serialNo='" + serialNo + '\'' +
				", snOnHand=" + snOnHand +
				", snBoxPltNo='" + snBoxPltNo + '\'' +
				", boxQty=" + boxQty +
				", deliveryID=" + deliveryID +
				", itemNo='" + itemNo + '\'' +
				", po='" + po + '\'' +
				", oe='" + oe + '\'' +
				", oeID=" + oeID +
				'}';
	}
}
