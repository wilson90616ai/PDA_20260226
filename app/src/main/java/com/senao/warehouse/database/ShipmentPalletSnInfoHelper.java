package com.senao.warehouse.database;

import com.senao.warehouse.handler.Pdf417Info_SOPHOS;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ShipmentPalletSnInfoHelper extends BasicHelper {
    private String searchText;
    private int dnNo;
    private String partNo;
    private String palletNo;
    private String modelName;
    private int qty;
    private String[] snInfo;
    private String customerPartNo;
    private String customerPartNoDescription;
    private String remark;
    private int printNo;
    private HashMap<String, Pdf417Info_SOPHOS> ht;
    private Set lotCode;
    private String customerNo;

    public Set getLotCode() {
        return lotCode;
    }

    public void setLotCode(Set lotCode) {
        this.lotCode = lotCode;
    }

    public String getCustomerNo() {
        return customerNo;
    }

    public void setCustomerNo(String customerNo) {
        this.customerNo = customerNo;
    }

    public HashMap<String, Pdf417Info_SOPHOS> getHt() {
        return ht;
    }

    public void setHt(HashMap<String, Pdf417Info_SOPHOS> ht) {
        this.ht = ht;
    }

    public int getDnNo() {
        return dnNo;
    }

    public void setDnNo(int dnNo) {
        this.dnNo = dnNo;
    }

    public String getPalletNo() {
        return palletNo;
    }

    public void setPalletNo(String palletNo) {
        this.palletNo = palletNo;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String[] getSnInfo() {
        return snInfo;
    }

    public void setSnInfo(List<String> list) {
        this.snInfo = new String[list.size()];
        Iterator<String> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            snInfo[i] = iter.next();
            i++;
        }
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getCustomerPartNo() {
        return customerPartNo;
    }

    public void setCustomerPartNo(String customerPartNo) {
        this.customerPartNo = customerPartNo;
    }

    public String getCustomerPartNoDescription() {
        return customerPartNoDescription;
    }

    public void setCustomerPartNoDescription(String customerPartNoDescription) {
        this.customerPartNoDescription = customerPartNoDescription;
    }

    public int getPrintNo() {
        return printNo;
    }

    public void setPrintNo(int printNo) {
        this.printNo = printNo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
