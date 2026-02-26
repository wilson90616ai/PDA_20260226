package com.senao.warehouse.database;

public class PackingQtyInfoHelper extends BasicHelper {
	private int deliveryID;
	private int itemID;
	private int qty;
	private int pickQty;
	private int nonPickQty;
	private int oeID;

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

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public int getPickQty() {
		return pickQty;
	}

	public void setPickQty(int pickQty) {
		this.pickQty = pickQty;
	}

	public int getNonPickQty() {
		return nonPickQty;
	}

	public void setNonPickQty(int nonPickQty) {
		this.nonPickQty = nonPickQty;
	}

	public int getOeID() {
		return oeID;
	}

	public void setOeID(int oeID) {
		this.oeID = oeID;
	}
}
