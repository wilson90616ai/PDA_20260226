package com.senao.warehouse.database;

import java.math.BigDecimal;

public class TransferSnInfo {
    private int type;
    private String value;
    private BigDecimal qty;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }
}
