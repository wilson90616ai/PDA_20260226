package com.senao.warehouse.database;

import android.util.Log;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class VendorInfoHelper extends BasicHelper implements Cloneable {
    private String num;
    private String name;
    private int id;
    private String siteCode;
    private int siteID;
    private String userName;
    private String partNo;
    private int partID;
    private String receivingType;
    private boolean isOutSourcing;
    private String wareHouseNo;
    private String invoiceNo;
    private PnItemInfoHelper[] pnList;
    private ReceivingInfoHelper[] recvList;
    private String[] invoiceList;
    private String locator;
    private int count; //成功筆數
    private String isRemittance;
    private String invoiceSeq;

    public ReceivingInfoHelper[] getRecvList() {
        return recvList;
    }

    public void setRecvList(List<ReceivingInfoHelper> list) {
        if (list != null && list.size() > 0) {
            this.recvList = new ReceivingInfoHelper[list.size()];
            Iterator<ReceivingInfoHelper> iter = list.iterator();
            int i = 0;

            while (iter.hasNext()) {
                recvList[i] = iter.next();
                i++;
            }
        } else {
            this.recvList = null;
        }
    }

    public VendorInfoHelper getImportInfo(VendorInfoHelper info) {
        VendorInfoHelper copy = null;

        try {
            copy = (VendorInfoHelper) info.clone();
            copy.setInvoiceList(null);
            copy.setRecvList(null);
            copy.setPnList(null);
        } catch (Exception ex) {
            Log.e(this.getClass().getSimpleName(), ex.getMessage());
        }

        return copy;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    public int getSiteID() {
        return siteID;
    }

    public void setSiteID(int siteID) {
        this.siteID = siteID;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PnItemInfoHelper[] getPnList() {
        return pnList;
    }

    public void setPnList(List<PnItemInfoHelper> list) {
        if (list != null && list.size() > 0) {
            this.pnList = new PnItemInfoHelper[list.size()];
            Iterator<PnItemInfoHelper> iter = list.iterator();
            int i = 0;

            while (iter.hasNext()) {
                pnList[i] = iter.next();
                i++;
            }
        } else {
            this.pnList = null;
        }
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public String getReceivingType() {
        return receivingType;
    }

    public void setReceivingType(String receivingType) {
        this.receivingType = receivingType;
    }

    public int getPartID() {
        return partID;
    }

    public void setPartID(int partID) {
        this.partID = partID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isOutSourcing() {
        return isOutSourcing;
    }

    public void setOutSourcing(boolean outSourcing) {
        isOutSourcing = outSourcing;
    }

    public String getWareHouseNo() {
        return wareHouseNo;
    }

    public void setWareHouseNo(String wareHouseNo) {
        this.wareHouseNo = wareHouseNo;
    }

    public String[] getInvoiceList() {
        return invoiceList;
    }

    public void setInvoiceList(List<String> list) {
        if (list != null && list.size() > 0) {
            this.invoiceList = new String[list.size()];
            Iterator<String> iter = list.iterator();
            int i = 0;

            while (iter.hasNext()) {
                invoiceList[i] = iter.next();
                i++;
            }
        } else {
            this.invoiceList = null;
        }
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getLocator() {
        return locator;
    }

    public void setLocator(String locator) {
        this.locator = locator;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getIsRemittance() {
        return isRemittance;
    }

    public void setIsRemittance(String isRemittance) {
        this.isRemittance = isRemittance;
    }

    public String getInvoiceSeq() {
        return invoiceSeq;
    }

    public void setInvoiceSeq(String invoiceSeq) {
        this.invoiceSeq = invoiceSeq;
    }

    @Override
    public String toString() {
        return "VendorInfoHelper{" +
                "num='" + num + '\'' +
                ", name='" + name + '\'' +
                ", id=" + id +
                ", siteCode='" + siteCode + '\'' +
                ", siteID=" + siteID +
                ", userName='" + userName + '\'' +
                ", partNo='" + partNo + '\'' +
                ", partID=" + partID +
                ", receivingType='" + receivingType + '\'' +
                ", isOutSourcing=" + isOutSourcing +
                ", wareHouseNo='" + wareHouseNo + '\'' +
                ", invoiceNo='" + invoiceNo + '\'' +
                ", pnList=" + Arrays.toString(pnList) +
                ", recvList=" + Arrays.toString(recvList) +
                ", invoiceList=" + Arrays.toString(invoiceList) +
                ", locator='" + locator + '\'' +
                ", count=" + count +
                ", isRemittance='" + isRemittance + '\'' +
                ", invoiceSeq='" + invoiceSeq + '\'' +
                '}';
    }
}
