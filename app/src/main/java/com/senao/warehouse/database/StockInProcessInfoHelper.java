package com.senao.warehouse.database;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class StockInProcessInfoHelper extends BasicHelper {
    private String partNo;
    private String subinventory;
    private String locator;
    private String applyNo;
    private String dateCode1; //original
    private String dateCode2; //modified
    private int qty;
    private String reelID;
    private String serialNoStart;
    private String serialNoEnd;
    private String boxNo;
    private String workOrderNo;
    private String palletNo;
    private String[] snList;

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

    public String getApplyNo() {
        return applyNo;
    }

    public void setApplyNo(String applyNo) {
        this.applyNo = applyNo;
    }

    public String getDateCode1() {
        return dateCode1;
    }

    public void setDateCode1(String dateCode1) {
        this.dateCode1 = dateCode1;
    }

    public String getDateCode2() {
        return dateCode2;
    }

    public void setDateCode2(String dateCode2) {
        this.dateCode2 = dateCode2;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
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

    public String[] getSnList() {
        return snList;
    }

    public void setSnList(List<String> list) {
        this.snList = new String[list.size()];
        Iterator<String> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            snList[i] = iter.next();
            i++;
        }
    }

    @Override
    public String toString() {
        return "StockInProcessInfoHelper{" +
                "partNo='" + partNo + '\'' +
                ", subinventory='" + subinventory + '\'' +
                ", locator='" + locator + '\'' +
                ", applyNo='" + applyNo + '\'' +
                ", dateCode1='" + dateCode1 + '\'' +
                ", dateCode2='" + dateCode2 + '\'' +
                ", qty=" + qty +
                ", reelID='" + reelID + '\'' +
                ", serialNoStart='" + serialNoStart + '\'' +
                ", serialNoEnd='" + serialNoEnd + '\'' +
                ", boxNo='" + boxNo + '\'' +
                ", workOrderNo='" + workOrderNo + '\'' +
                ", palletNo='" + palletNo + '\'' +
                ", snList=" + Arrays.toString(snList) +
                '}';
    }
}
