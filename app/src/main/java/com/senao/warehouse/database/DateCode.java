package com.senao.warehouse.database;

import java.math.BigDecimal;

public class DateCode {
    private String dateCode;
    private BigDecimal qty;
    private String locator; //為了辨別該datecode的locator

    public String getLocator() {
        return locator;
    }

    public void setLocator(String locator) {
        this.locator = locator;
    }

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

    @Override
    public String toString() {
        return "DateCode{" +
                "dateCode='" + dateCode + '\'' +
                ", qty=" + qty +
                ", locator='" + locator + '\'' +
                '}';
    }
}
