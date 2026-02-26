package com.senao.warehouse.database;

public class ItemInfoHelper extends BasicHelper {
	private int id;
	private String itemID;
	private int qty;
	private String control;
	private int pass; //數量
	private int wait; //可能是未檢數量
	private String description;
	private String searchText;
	private String CustmerId;
	private String CustmerName;

	public String getCustmerName() {
		return CustmerName;
	}

	public void setCustmerName(String custmerName) {
		CustmerName = custmerName;
	}

	public String getCustmerId() {
		return CustmerId;
	}

	public void setCustmerId(String custmerId) {
		CustmerId = custmerId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public String getControl() {
		return control;
	}

	public void setControl(String control) {
		this.control = control;
	}

	public int getPass() {
		return pass;
	}

	public void setPass(int pass) {
		this.pass = pass;
	}

	public int getWait() {
		return wait;
	}

	public void setWait(int wait) {
		this.wait = wait;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	@Override
	public String toString() {
		return "ItemInfoHelper{" +
				"id=" + id +
				", itemID='" + itemID + '\'' +
				", qty=" + qty +
				", control='" + control + '\'' +
				", pass=" + pass +
				", wait=" + wait +
				", description='" + description + '\'' +
				", searchText='" + searchText + '\'' +
				", CustmerId='" + CustmerId + '\'' +
				", CustmerName='" + CustmerName + '\'' +
				'}';
	}
}
