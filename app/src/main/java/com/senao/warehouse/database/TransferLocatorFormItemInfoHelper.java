package com.senao.warehouse.database;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

public class TransferLocatorFormItemInfoHelper extends BasicHelper {
    private String formSerialNumber;
    private String outSubinventory;
    private String outLocator;
    private String status;
    private String warehouseClerk;
    private String itemNo;
    private String itemDescription;
    private String control;
    private BigDecimal dueNumber;
    private BigDecimal inNumber;
    private BigDecimal onHand;
    private String processType;
    private TransferLocatorInfoHelper[] locatorInfo;

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

    public TransferLocatorInfoHelper[] getLocatorInfo() {
        return locatorInfo;
    }

    public void setLocatorInfo(List<TransferLocatorInfoHelper> list) {
        this.locatorInfo = new TransferLocatorInfoHelper[list.size()];
        Iterator<TransferLocatorInfoHelper> iterator = list.iterator();
        int i = 0;

        while (iterator.hasNext()) {
            locatorInfo[i] = iterator.next();
            i++;
        }
    }

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
    }

    public String getFormSerialNumber() {
        return formSerialNumber;
    }

    public void setFormSerialNumber(String formSerialNumber) {
        this.formSerialNumber = formSerialNumber;
    }

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }
}
