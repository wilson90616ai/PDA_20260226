package com.senao.warehouse.database;

public class PnItemInfoHelper extends BasicHelper {
	private String partNo;
	private String partDescription;
	private String customerPartNo;

	public String getPartNo() {
		return partNo;
	}

	public void setPartNo(String partNo) {
		this.partNo = partNo;
	}

	public String getPartDescription() {
		return partDescription;
	}

	public void setPartDescription(String partDescription) {
		this.partDescription = partDescription;
	}

	public String getCustomerPartNo() {
		return customerPartNo;
	}

	public void setCustomerPartNo(String customerPartNo) {
		this.customerPartNo = customerPartNo;
	}
}
