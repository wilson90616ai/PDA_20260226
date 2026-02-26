package com.senao.warehouse.database;

import java.math.BigDecimal;

public class SendSubinventoryInfo {
    private String subinventory;
    private String locator;
    private BigDecimal inventory;
    private String datecode;

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

    public BigDecimal getInventory() {
        return inventory;
    }

    public void setInventory(BigDecimal inventory) {
        this.inventory = inventory;
    }

    public String getDatecode() {
        return datecode;
    }

    public void setDatecode(String datecode) {
        this.datecode = datecode;
    }
}
