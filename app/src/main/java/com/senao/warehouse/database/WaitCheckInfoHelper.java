package com.senao.warehouse.database;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

public class WaitCheckInfoHelper extends BasicHelper {
    private int notInDay;
    private String control;
    private String partNo;
    private BigDecimal uncheckTotalQty;
    private BigDecimal receivedUncheckQty;
    private BigDecimal checkedNotInQty;
    private String vendorNo;
    private String vendorName;
    private WaitCheckDetailInfoHelper[] detailList;

    public WaitCheckDetailInfoHelper[] getDetailList() {
        return detailList;
    }

    public void setDetailList(List<WaitCheckDetailInfoHelper> list) {
        this.detailList = new WaitCheckDetailInfoHelper[list.size()];
        Iterator<WaitCheckDetailInfoHelper> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            detailList[i] = iter.next();
            i++;
        }
    }

    public int getNotInDay() {
        return notInDay;
    }

    public void setNotInDay(int notInDay) {
        this.notInDay = notInDay;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public BigDecimal getUncheckTotalQty() {
        return uncheckTotalQty;
    }

    public void setUncheckTotalQty(BigDecimal uncheckTotalQty) {
        this.uncheckTotalQty = uncheckTotalQty;
    }

    public BigDecimal getReceivedUncheckQty() {
        return receivedUncheckQty;
    }

    public void setReceivedUncheckQty(BigDecimal receivedUncheckQty) {
        this.receivedUncheckQty = receivedUncheckQty;
    }

    public BigDecimal getCheckedNotInQty() {
        return checkedNotInQty;
    }

    public void setCheckedNotInQty(BigDecimal checkedNotInQty) {
        this.checkedNotInQty = checkedNotInQty;
    }

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
    }

    public String getVendorNo() {
        return vendorNo;
    }

    public void setVendorNo(String vendorNo) {
        this.vendorNo = vendorNo;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }
}
