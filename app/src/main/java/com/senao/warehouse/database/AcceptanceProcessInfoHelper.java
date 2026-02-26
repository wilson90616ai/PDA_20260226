package com.senao.warehouse.database;

import java.math.BigDecimal;

public class AcceptanceProcessInfoHelper extends BasicHelper {
    private String partNo;
    private String subinventory;
    private String locator;
    private String receiptNo;
    private String dateCode;
    private BigDecimal qty;
    private String reelID;
    private String serialNoStart;
    private String serialNoEnd;
    private String boxNo;
    private String workOrderNo;
    private String palletNo;
    private BigDecimal xbQty;

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
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

    public String getReceiptNo() {
        return receiptNo;
    }

    public void setReceiptNo(String receiptNo) {
        this.receiptNo = receiptNo;
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

    public String getReelID() {
        return reelID;
    }

    public void setReelID(String reelID) {
        this.reelID = reelID;
    }

    public String getSerialNoStart() {
        return serialNoStart;
    }

    public void setSerialNoStart(String serialNoStart) {
        this.serialNoStart = serialNoStart;
    }

    public String getSerialNoEnd() {
        return serialNoEnd;
    }

    public void setSerialNoEnd(String serialNoEnd) {
        this.serialNoEnd = serialNoEnd;
    }

    public String getBoxNo() {
        return boxNo;
    }

    public void setBoxNo(String boxNo) {
        this.boxNo = boxNo;
    }

    public String getWorkOrderNo() {
        return workOrderNo;
    }

    public void setWorkOrderNo(String workOrderNo) {
        this.workOrderNo = workOrderNo;
    }

    public String getPalletNo() {
        return palletNo;
    }

    public void setPalletNo(String palletNo) {
        this.palletNo = palletNo;
    }

    public BigDecimal getXbQty() {
        return xbQty;
    }

    public void setXbQty(BigDecimal xbQty) {
        this.xbQty = xbQty;
    }
}
