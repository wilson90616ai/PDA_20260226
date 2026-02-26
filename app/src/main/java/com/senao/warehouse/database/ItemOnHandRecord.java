package com.senao.warehouse.database;

import java.math.BigDecimal;

public class ItemOnHandRecord {
    private int grp;
    private String type;
    private String itemCtrl;
    private int itemId;
    private String itemNo;
    private String description;
    private String subinventoryCode;
    private String lot;
    private String locator;
    private BigDecimal qty;

    public int getGrp() {
        return grp;
    }

    public void setGrp(int grp) {
        this.grp = grp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getItemCtrl() {
        return itemCtrl;
    }

    public void setItemCtrl(String itemCtrl) {
        this.itemCtrl = itemCtrl;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemNo() {
        return itemNo;
    }

    public void setItemNo(String itemNo) {
        this.itemNo = itemNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubinventoryCode() {
        return subinventoryCode;
    }

    public void setSubinventoryCode(String subinventoryCode) {
        this.subinventoryCode = subinventoryCode;
    }

    public String getLot() {
        return lot;
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

    public String getLocator() {
        return locator;
    }

    public void setLocator(String locator) {
        this.locator = locator;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    @Override
    public String toString() {
        return "ItemOnHandRecord{" +
                "grp=" + grp +
                ", type='" + type + '\'' +
                ", itemCtrl='" + itemCtrl + '\'' +
                ", itemId=" + itemId +
                ", itemNo='" + itemNo + '\'' +
                ", description='" + description + '\'' +
                ", subinventoryCode='" + subinventoryCode + '\'' +
                ", lot='" + lot + '\'' +
                ", locator='" + locator + '\'' +
                ", qty=" + qty +
                '}';
    }
}
