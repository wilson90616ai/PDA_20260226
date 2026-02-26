package com.senao.warehouse.database;

import java.math.BigDecimal;

public class TempTransferProcessInfoHelper extends BasicHelper {
    private String partNo;
    private String itemDescription;
    private String subinventory;
    private String locator;
    private String transSubinventory;
    private String transLocator;
    private BigDecimal inventory;
    private BigDecimal transQty;
    private String dateCode;
    private String userID;

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
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

    public String getTransSubinventory() {
        return transSubinventory;
    }

    public void setTransSubinventory(String transSubinventory) {
        this.transSubinventory = transSubinventory;
    }

    public String getTransLocator() {
        return transLocator;
    }

    public void setTransLocator(String transLocator) {
        this.transLocator = transLocator;
    }

    public BigDecimal getInventory() {
        return inventory;
    }

    public void setInventory(BigDecimal inventory) {
        this.inventory = inventory;
    }

    public BigDecimal getTransQty() {
        return transQty;
    }

    public void setTransQty(BigDecimal transQty) {
        this.transQty = transQty;
    }

    public String getDateCode() {
        return dateCode;
    }

    public void setDateCode(String dateCode) {
        this.dateCode = dateCode;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
