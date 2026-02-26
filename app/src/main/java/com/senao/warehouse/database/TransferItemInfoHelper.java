package com.senao.warehouse.database;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

public class TransferItemInfoHelper extends BasicHelper {
    private String partNo;
    private String itemDescription;
    private BigDecimal qty;
    private String transSubinventory;
    private String transLocator;
    private BigDecimal transQty;
    private String control;
    private TransferLocatorInfoHelper[] locatorInfo;

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

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
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

    public BigDecimal getTransQty() {
        return transQty;
    }

    public void setTransQty(BigDecimal transQty) {
        this.transQty = transQty;
    }

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
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
}
