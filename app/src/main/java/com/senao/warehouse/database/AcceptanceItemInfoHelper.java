package com.senao.warehouse.database;

import java.math.BigDecimal;

public class AcceptanceItemInfoHelper extends BasicHelper {
    private int id;
    private String itemID;
    private BigDecimal qty;
    private String control;
    private BigDecimal inQty;
    private BigDecimal notInQty;
    
	public int getId() {
		return id;
	}
	
	public void setId(int iD) {
		id = iD;
	}
	
	public String getItemID() {
		return itemID;
	}
	
	public void setItemID(String itemID) {
		this.itemID = itemID;
	}
	
	public BigDecimal getQty() {
		return qty;
	}
	
	public void setQty(BigDecimal qty) {
		this.qty = qty;
	}
	
	public String getControl() {
		return control;
	}
	
	public void setControl(String control) {
		this.control = control;
	}
	
	public BigDecimal getInQty() {
		return inQty;
	}
	
	public void setInQty(BigDecimal inQty) {
		this.inQty = inQty;
	}
	
	public BigDecimal getNotInQty() {
		return notInQty;
	}
	
	public void setNotInQty(BigDecimal notInQty) {
		this.notInQty = notInQty;
	}
}
