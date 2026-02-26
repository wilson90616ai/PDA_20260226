package com.senao.warehouse.database;

import java.math.BigDecimal;

public class SendProcessInfoHelper extends BasicHelper {
    private String lineNo;
    private String mergeNo;
    private String whNo;
    private String woNo;
    private String partNo;
    private String subinventory;
    private String locator;
    private String dateCode;
    private BigDecimal qty;
    private String reelID;
    private String serialNo;
    private String boxNo;
    private String type;
    private String transSubinventory;
    private String transLocator;
    private BigDecimal transQty;
    private String trxType;
    private String po;
    private String newReelID;
    private String vendorCode;
    private String poWithQty;

    public String getPo() {
        return po;
    }

    public void setPo(String po) {
        this.po = po;
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

    public String getLineNo() {
        return lineNo;
    }

    public void setLineNo(String lineNo) {
        this.lineNo = lineNo;
    }

    public String getMergeNo() {
        return mergeNo;
    }

    public void setMergeNo(String mergeNo) {
        this.mergeNo = mergeNo;
    }

    public String getWhNo() {
        return whNo;
    }

    public void setWhNo(String whNo) {
        this.whNo = whNo;
    }

    public String getWoNo() {
        return woNo;
    }

    public void setWoNo(String woNo) {
        this.woNo = woNo;
    }

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

    public String getDateCode() {
        return dateCode;
    }

    public void setDateCode(String dateCode) {
        this.dateCode = dateCode;
    }

    public String getReelID() {
        return reelID;
    }

    public void setReelID(String reelID) {
        this.reelID = reelID;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getBoxNo() {
        return boxNo;
    }

    public void setBoxNo(String boxNo) {
        this.boxNo = boxNo;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getTransQty() {
        return transQty;
    }

    public void setTransQty(BigDecimal transQty) {
        this.transQty = transQty;
    }

    public String getTrxType() {
        return trxType;
    }

    public void setTrxType(String trxType) {
        this.trxType = trxType;
    }

    public String getNewReelID() {
        return newReelID;
    }

    public void setNewReelID(String newReelID) {
        this.newReelID = newReelID;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getPoWithQty() {
        return poWithQty;
    }

    public void setPoWithQty(String poWithQty) {
        this.poWithQty = poWithQty;
    }
}
