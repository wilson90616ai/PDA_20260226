package com.senao.warehouse.database;


import java.util.Iterator;
import java.util.List;

public class RmaInfoHelper extends BasicHelper {
    private String partNo;
    private String palletNo;
    private int qty;
    private int printNo;
    private String customerPartNo;
    private String modelName;
    private String[] snInfo;

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
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

    public int getPrintNo() {
        return printNo;
    }

    public void setPrintNo(int printNo) {
        this.printNo = printNo;
    }

    public String getCustomerPartNo() {
        return customerPartNo;
    }

    public void setCustomerPartNo(String customerPartNo) {
        this.customerPartNo = customerPartNo;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
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
}
