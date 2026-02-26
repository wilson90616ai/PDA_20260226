package com.senao.warehouse.database;

import java.math.BigDecimal;

public class AcceptanceDateCodeInfo {
    private String dateCode;
    private BigDecimal qty;
    private BigDecimal inQty;
    private BigDecimal notInQty;
    
	public String getDateCode() {
		return dateCode;
	}
	
	public void setDateCode(String dateCode) {
		this.dateCode = dateCode;
	}
	
	public BigDecimal getQty() {
		return qty;
	}
	
	public void setQty(BigDecimal qty) {
		this.qty = qty;
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
