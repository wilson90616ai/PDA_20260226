package com.senao.warehouse.database;

public class ShipmentPalletInfoHelper extends BasicHelper {
	private int dnNo;
	private String palletNo;
	private String mark;
	private String shippingWay;
	private int boxQty;
	private String customerNo;
	private String containerNo;
	private String palletSize;

	public String getContainerNo() {
		return containerNo;
	}

	public void setContainerNo(String containerNo) {
		this.containerNo = containerNo;
	}
	
	public int getDnNo() {
		return dnNo;
	}

	public void setDnNo(int dnNo) {
		this.dnNo = dnNo;
	}

	public String getPalletNo() {
		return palletNo;
	}

	public void setPalletNo(String palletNo) {
		this.palletNo = palletNo;
	}

	public String getMark() {
		return mark;
	}

	public void setMark(String mark) {
		this.mark = mark;
	}

	public String getShippingWay() {
		return shippingWay;
	}

	public void setShippingWay(String shippingWay) {
		this.shippingWay = shippingWay;
	}

	public int getBoxQty() {
		return boxQty;
	}

	public void setBoxQty(int boxQty) {
		this.boxQty = boxQty;
	}

	public String getCustomerNo() {
		return customerNo;
	}

	public void setCustomerNo(String customerNo) {
		this.customerNo = customerNo;
	}

	public String getPalletSize() {
		return palletSize;
	}

	public void setPalletSize(String palletSize) {
		this.palletSize = palletSize;
	}

	@Override
	public String toString() {
		return "ShipmentPalletInfoHelper{" +
				"dnNo=" + dnNo +
				", palletNo='" + palletNo + '\'' +
				", mark='" + mark + '\'' +
				", shippingWay='" + shippingWay + '\'' +
				", boxQty=" + boxQty +
				", customerNo='" + customerNo + '\'' +
				", containerNo='" + containerNo + '\'' +
				", palletSize='" + palletSize + '\'' +
				'}';
	}
}
