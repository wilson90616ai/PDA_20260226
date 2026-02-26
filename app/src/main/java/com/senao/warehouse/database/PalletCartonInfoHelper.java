package com.senao.warehouse.database;

public class PalletCartonInfoHelper extends BasicHelper {
	private int deliveryID;
	private int palletTotalQty;
	private int cartonTotalQty;
	private int palletNonCheckQty;
	private int cartonNonCheckQty;

	public int getDeliveryID() {
		return deliveryID;
	}

	public void setDeliveryID(int deliveryID) {
		this.deliveryID = deliveryID;
	}

	public int getPalletTotalQty() {
		return palletTotalQty;
	}

	public void setPalletTotalQty(int palletTotalQty) {
		this.palletTotalQty = palletTotalQty;
	}

	public int getCartonTotalQty() {
		return cartonTotalQty;
	}

	public void setCartonTotalQty(int cartonTotalQty) {
		this.cartonTotalQty = cartonTotalQty;
	}

	public int getPalletNonCheckQty() {
		return palletNonCheckQty;
	}

	public void setPalletNonCheckQty(int palletNonCheckQty) {
		this.palletNonCheckQty = palletNonCheckQty;
	}

	public int getCartonNonCheckQty() {
		return cartonNonCheckQty;
	}

	public void getIntCartonNonCheckQty(int intCartonNonCheckQty) {
		this.cartonNonCheckQty = intCartonNonCheckQty;
	}

	@Override
	public String toString() {
		return "PalletCartonInfoHelper{" +
				"deliveryID=" + deliveryID +
				", palletTotalQty=" + palletTotalQty +
				", cartonTotalQty=" + cartonTotalQty +
				", palletNonCheckQty=" + palletNonCheckQty +
				", cartonNonCheckQty=" + cartonNonCheckQty +
				'}';
	}
}
