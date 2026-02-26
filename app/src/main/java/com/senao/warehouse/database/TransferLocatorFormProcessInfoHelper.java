package com.senao.warehouse.database;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

public class TransferLocatorFormProcessInfoHelper extends BasicHelper {
    private String formSerialNumber;
    private String outSubinventory;
    private String outLocator;
    private String status;
    private String warehouseClerk;
    private String itemNo;
    private String itemDescription;
    private String control;
    private String inSubinventory;
    private String inLocator;
    private BigDecimal dueNumber;
    private BigDecimal inNumber;
    private BigDecimal onHand;
    private String reelID;
    private String serialNo;
    private String boxNo;
    private int type;
    private BigDecimal qty;
    private String dateCode;
    private String userId;
    private TransferDateCodeInfo[] dateCodeInfo;
    private TransferSnInfo[] snInfo;

    public String getOutSubinventory() {
        return outSubinventory;
    }

    public void setOutSubinventory(String outSubinventory) {
        this.outSubinventory = outSubinventory;
    }

    public String getOutLocator() {
        return outLocator;
    }

    public void setOutLocator(String outLocator) {
        this.outLocator = outLocator;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWarehouseClerk() {
        return warehouseClerk;
    }

    public void setWarehouseClerk(String warehouseClerk) {
        this.warehouseClerk = warehouseClerk;
    }

    public String getItemNo() {
        return itemNo;
    }

    public void setItemNo(String itemNo) {
        this.itemNo = itemNo;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public BigDecimal getDueNumber() {
        return dueNumber;
    }

    public void setDueNumber(BigDecimal dueNumber) {
        this.dueNumber = dueNumber;
    }

    public BigDecimal getInNumber() {
        return inNumber;
    }

    public void setInNumber(BigDecimal inNumber) {
        this.inNumber = inNumber;
    }

    public BigDecimal getOnHand() {
        return onHand;
    }

    public void setOnHand(BigDecimal onHand) {
        this.onHand = onHand;
    }

    public String getFormSerialNumber() {
        return formSerialNumber;
    }

    public void setFormSerialNumber(String formSerialNumber) {
        this.formSerialNumber = formSerialNumber;
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

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
    }

    public String getInSubinventory() {
        return inSubinventory;
    }

    public void setInSubinventory(String inSubinventory) {
        this.inSubinventory = inSubinventory;
    }

    public String getInLocator() {
        return inLocator;
    }

    public void setInLocator(String inLocator) {
        this.inLocator = inLocator;
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

    public String getDateCode() {
        return dateCode;
    }

    public void setDateCode(String dateCode) {
        this.dateCode = dateCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
