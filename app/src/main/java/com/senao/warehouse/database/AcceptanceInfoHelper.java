package com.senao.warehouse.database;

import java.math.BigDecimal;

public class AcceptanceInfoHelper extends BasicHelper {
    private String wareHouseNo;
    private int notInDay;
    private String control;
    private String partNo;
    private BigDecimal waitCheckQty;
    private BigDecimal checkedNotInQty;
    private BigDecimal inQty;
    private BigDecimal notInQty;
    private String receiptNo;
    private String trxId;
    private BigDecimal checkedNotInXbQty;
    private BigDecimal inXbQty;
    private BigDecimal notInXbQty;
    private String itemLoc;
    private String itemLocDesc;

    public BigDecimal getCheckedNotInXbQty() {
        return checkedNotInXbQty;
    }

    public void setCheckedNotInXbQty(BigDecimal checkedNotInXbQty) {
        this.checkedNotInXbQty = checkedNotInXbQty;
    }

    public BigDecimal getInXbQty() {
        return inXbQty;
    }

    public void setInXbQty(BigDecimal inXbQty) {
        this.inXbQty = inXbQty;
    }

    public BigDecimal getNotInXbQty() {
        return notInXbQty;
    }

    public void setNotInXbQty(BigDecimal notInXbQty) {
        this.notInXbQty = notInXbQty;
    }

    public int getNotInDay() {
        return notInDay;
    }

    public void setNotInDay(int notInDay) {
        this.notInDay = notInDay;
    }

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public String getReceiptNo() {
        return receiptNo;
    }

    public void setReceiptNo(String receiptNo) {
        this.receiptNo = receiptNo;
    }

    public BigDecimal getWaitCheckQty() {
        return waitCheckQty;
    }

    public void setWaitCheckQty(BigDecimal waitCheckQty) {
        this.waitCheckQty = waitCheckQty;
    }

    public BigDecimal getCheckedNotInQty() {
        return checkedNotInQty;
    }

    public void setCheckedNotInQty(BigDecimal checkedNotInQty) {
        this.checkedNotInQty = checkedNotInQty;
    }

    public BigDecimal getInQty() {
        return inQty;
    }

    public void setInQty(BigDecimal inQty) {
        this.inQty = inQty;
    }

    public BigDecimal getNotInQty() {
        return notInQty;
    }

    public void setNotInQty(BigDecimal notInQty) {
        this.notInQty = notInQty;
    }

    public String getWareHouseNo() {
        return wareHouseNo;
    }

    public void setWareHouseNo(String wareHouseNo) {
        this.wareHouseNo = wareHouseNo;
    }

    public String getTrxId() {
        return trxId;
    }

    public void setTrxId(String trxId) {
        this.trxId = trxId;
    }

    public String getItemLoc() {
        return itemLoc;
    }

    public void setItemLoc(String itemLoc) {
        this.itemLoc = itemLoc;
    }

    public String getItemLocDesc() {
        return itemLocDesc;
    }

    public void setItemLocDesc(String itemLocDesc) {
        this.itemLocDesc = itemLocDesc;
    }

    @Override
    public String toString() {
        return "AcceptanceInfoHelper{" +
                "wareHouseNo='" + wareHouseNo + '\'' +
                ", notInDay=" + notInDay +
                ", control='" + control + '\'' +
                ", partNo='" + partNo + '\'' +
                ", waitCheckQty=" + waitCheckQty +
                ", checkedNotInQty=" + checkedNotInQty +
                ", inQty=" + inQty +
                ", notInQty=" + notInQty +
                ", receiptNo='" + receiptNo + '\'' +
                ", trxId='" + trxId + '\'' +
                ", checkedNotInXbQty=" + checkedNotInXbQty +
                ", inXbQty=" + inXbQty +
                ", notInXbQty=" + notInXbQty +
                ", itemLoc='" + itemLoc + '\'' +
                ", itemLocDesc='" + itemLocDesc + '\'' +
                '}';
    }
}
