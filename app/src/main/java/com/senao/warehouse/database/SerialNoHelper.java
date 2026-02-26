package com.senao.warehouse.database;

public class SerialNoHelper extends BasicHelper {
	private String partNo;
	private String stockInNo;
	private String serialNo;
	private String boxNo;
	private String palletNo;
	private int qtyInBox;
	private int qtyOnPallet;

	public String getPartNo() {
		return partNo;
	}

	public void setPartNo(String partNo) {
		this.partNo = partNo;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public String getStockInNo() {
		return stockInNo;
	}

	public void setStockInNo(String stockInNo) {
		this.stockInNo = stockInNo;
	}

	public String getPalletNo() {
		return palletNo;
	}

	public void setPalletNo(String palletNo) {
		this.palletNo = palletNo;
	}

	public String getBoxNo() {
		return boxNo;
	}

	public void setBoxNo(String boxNo) {
		this.boxNo = boxNo;
	}

	public int getQtyInBox() {
		return qtyInBox;
	}

	public void setQtyInBox(int qtyInBox) {
		this.qtyInBox = qtyInBox;
	}

	public int getQtyOnPallet() {
		return qtyOnPallet;
	}

	public void setQtyOnPallet(int qtyOnPallet) {
		this.qtyOnPallet = qtyOnPallet;
	}
}
