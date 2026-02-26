package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class StockInNoHelper extends BasicHelper {
    private String no;
    private String workOrderNo;
    private ItemInfoHelper itemInfo;
    private String searchText;
    private DateCodeInfo[] dateDodeInfo;
    private boolean isSSG; //是否為出新加坡的貨

    public DateCodeInfo[] getDateCodeInfo() {
        return dateDodeInfo;
    }

    public void setDateCodeInfo(List<DateCodeInfo> list) {
        this.dateDodeInfo = new DateCodeInfo[list.size()];
        Iterator<DateCodeInfo> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            dateDodeInfo[i] = iter.next();
            i++;
        }
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public ItemInfoHelper getItemInfo() {
        return itemInfo;
    }

    public void setItemInfo(ItemInfoHelper itemInfo) {
        this.itemInfo = itemInfo;
    }

    public String getWorkOrderNo() {
        return workOrderNo;
    }

    public void setWorkOrderNo(String workOrderNo) {
        this.workOrderNo = workOrderNo;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public boolean isSSG() {
        return isSSG;
    }

    public void setSSG(boolean isSSG) {
        this.isSSG = isSSG;
    }
}
