package com.senao.warehouse.database;

import java.math.BigDecimal;

public class TransferDateCodeInfo {
	private String dateCode;
	private BigDecimal qty;
	private BigDecimal pass;

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

	public BigDecimal getPass() {
		return pass;
	}

	public void setPass(BigDecimal pass) {
		this.pass = pass;
	}
}
