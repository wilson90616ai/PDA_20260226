package com.senao.warehouse.database;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

public class SendOnHandQtyInfo extends  BasicHelper{
    private String partNo;
    private String itemDescription;
    private String control;
    private BigDecimal totalSendingQty;
    private BigDecimal sentQty;
    private BigDecimal unsentQty;
    private BigDecimal uncheckedQty;
    private boolean canDebit;
    private BigDecimal onHandQty;
    private SendSubinventoryInfo[] subinventoryInfoList;
    private BigDecimal tempQty;

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public BigDecimal getTotalSendingQty() {
        return totalSendingQty;
    }

    public void setTotalSendingQty(BigDecimal totalSendingQty) {
        this.totalSendingQty = totalSendingQty;
    }

    public BigDecimal getSentQty() {
        return sentQty;
    }

    public void setSentQty(BigDecimal sentQty) {
        this.sentQty = sentQty;
    }

    public BigDecimal getUnsentQty() {
        return unsentQty;
    }

    public void setUnsentQty(BigDecimal unsentQty) {
        this.unsentQty = unsentQty;
    }

    public BigDecimal getUncheckedQty() {
        return uncheckedQty;
    }

    public void setUncheckedQty(BigDecimal uncheckedQty) {
        this.uncheckedQty = uncheckedQty;
    }

    public SendSubinventoryInfo[] getSubinventoryInfoList() {
        return subinventoryInfoList;
    }

    public void setSubinventoryInfoList(List<SendSubinventoryInfo> list) {
        this.subinventoryInfoList = new SendSubinventoryInfo[list.size()];
        Iterator<SendSubinventoryInfo> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            subinventoryInfoList[i] = iter.next();
            i++;
        }
    }

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
    }

    public boolean isCanDebit() {
        return canDebit;
    }

    public void setCanDebit(boolean canDebit) {
        this.canDebit = canDebit;
    }

    public BigDecimal getOnHandQty() {
        return onHandQty;
    }

    public void setOnHandQty(BigDecimal onHandQty) {
        this.onHandQty = onHandQty;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public BigDecimal getTempQty() {
        return tempQty;
    }

    public void setTempQty(BigDecimal tempQty) {
        this.tempQty = tempQty;
    }
}
