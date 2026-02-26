package com.senao.warehouse.database;

public class DcNoItemInfoHelper extends BasicHelper {
	private int deliveryID;
	private int itemID;
	private String palletNo;
	private String cartonNo;
	private int shipQty;
	private double boxWeight;
	private String no;
	private String rev;
	private String origin;
	private String lotcode;

	public String getLotcode() {
		return lotcode;
	}

	public void setLotcode(String lotcode) {
		this.lotcode = lotcode;
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

	public int getShipQty() {
		return shipQty;
	}

	public void setShipQty(int shipQty) {
		this.shipQty = shipQty;
	}

	public double getBoxWeight() {
		return boxWeight;
	}

	public void setBoxWeight(double boxWeight) {
		this.boxWeight = boxWeight;
	}

	public String getNo() {
		return no;
	}

	public void setNo(String no) {
		this.no = no;
	}

	public String getRev() {
		return rev;
	}

	public void setRev(String rev) {
		this.rev = rev;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}
}
