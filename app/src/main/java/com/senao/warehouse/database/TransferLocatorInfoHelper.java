package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class TransferLocatorInfoHelper extends BasicHelper {
    private String subinventory;
    private String locator;
    private TransferDateCodeInfo[] dateCodeInfo;

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
}
