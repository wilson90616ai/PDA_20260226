package com.senao.warehouse.database;

public class CartonInfoHelper extends BasicHelper {
	private int itemID;
	private int boxQty;
	private double boxWeight;
	private double volume;
	private String boxLWH;
	
	public int getItemID() {
		return itemID;
	}
	public void setItemID(int itemID) {
		this.itemID = itemID;
	}
	public int getBoxQty() {
		return boxQty;
	}
	public void setBoxQty(int boxQty) {
		this.boxQty = boxQty;
	}
	public double getBoxWeight() {
		return boxWeight;
	}
	public void setBoxWeight(double boxWeight) {
		this.boxWeight = boxWeight;
	}
	public double getVolume() {
		return volume;
	}
	public void setVolume(double volume) {
		this.volume = volume;
	}
	public String getBoxLWH() {
		return boxLWH;
	}
	public void setBoxLWH(String boxLWH) {
		this.boxLWH = boxLWH;
	}
}
