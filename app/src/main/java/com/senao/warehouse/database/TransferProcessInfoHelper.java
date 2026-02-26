package com.senao.warehouse.database;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

public class TransferProcessInfoHelper extends BasicHelper {
    private String lineNo;
    private String whNo;
    private String partNo;
    private String itemDescription;
    private String subinventory;
    private String locator;
    private BigDecimal qty;
    private String reelID;
    private String serialNo;
    private String boxNo;
    private int type;
    private String transSubinventory;
    private String transLocator;
    private BigDecimal inventory;
    private BigDecimal transQty;
    private String dateCode;
    private String transferNo;
    private String control;
    private String reference;
    private TransferDateCodeInfo[] dateCodeInfo;
    private TransferSnInfo[] snInfo;

    public TransferProcessInfoHelper() {

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

    public String getWhNo() {
        return whNo;
    }

    public void setWhNo(String whNo) {
        this.whNo = whNo;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public BigDecimal getTransQty() {
        return transQty;
    }

    public void setTransQty(BigDecimal transQty) {
        this.transQty = transQty;
    }

    public TransferDateCodeInfo[] getDateCodeInfo() {
        return dateCodeInfo;
    }

    public void setDateCodeInfo(List<TransferDateCodeInfo> list) {
        this.dateCodeInfo = new TransferDateCodeInfo[list.size()];
        Iterator<TransferDateCodeInfo> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            dateCodeInfo[i] = iter.next();
            i++;
        }
    }

    public BigDecimal getInventory() {
        return inventory;
    }

    public void setInventory(BigDecimal inventory) {
        this.inventory = inventory;
    }

    public String getDateCode() {
        return dateCode;
    }

    public void setDateCode(String dateCode) {
        this.dateCode = dateCode;
    }

    public String getTransferNo() {
        return transferNo;
    }

    public void setTransferNo(String transferNo) {
        this.transferNo = transferNo;
    }

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
    }

    public TransferSnInfo[] getSnInfo() {
        return snInfo;
    }

    public void setSnInfo(List<TransferSnInfo> list) {
        this.snInfo = new TransferSnInfo[list.size()];
        Iterator<TransferSnInfo> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            snInfo[i] = iter.next();
            i++;
        }
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }
}
