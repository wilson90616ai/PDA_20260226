package com.senao.warehouse.database;

import java.util.List;

public class SubinventoryInfoHelper extends BasicHelper {
	private String partNo;
	private String subinventory;
	private String locator;
	private List<String> subinventories;

	public List<String> getSubinventories() {
		return subinventories;
	}

	public void setSubinventories(List<String> subinventories) {
		this.subinventories = subinventories;
	}

	public String getSubinventory() {
		return subinventory;
	}
	
	public void setSubinventory(String subinventory) {
		this.subinventory = subinventory;
	}
	
	public String getLocator() {
		return locator;
	}
	
	public void setLocator(String locator) {
		this.locator = locator;
	}

	public String getPartNo() {
		return partNo;
	}

	public void setPartNo(String partNo) {
		this.partNo = partNo;
	}
}
