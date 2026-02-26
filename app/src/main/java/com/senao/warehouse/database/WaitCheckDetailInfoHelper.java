package com.senao.warehouse.database;

import java.math.BigDecimal;

/**
 * Created by 102069 on 2016/8/22.
 */

public class WaitCheckDetailInfoHelper {
    private int notInDay;
    private String subinventory;
    private String locator;
    private String dateCode;
    private BigDecimal checkedNotInQty;

    public BigDecimal getCheckedNotInQty() {
        return checkedNotInQty;
    }

    public void setCheckedNotInQty(BigDecimal checkedNotInQty) {
        this.checkedNotInQty = checkedNotInQty;
    }

    public int getNotInDay() {
        return notInDay;
    }

    public void setNotInDay(int notInDay) {
        this.notInDay = notInDay;
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

    public String getDateCode() {
        return dateCode;
    }

    public void setDateCode(String dateCode) {
        this.dateCode = dateCode;
    }
}
