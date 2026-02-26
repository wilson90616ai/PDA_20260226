package com.senao.warehouse.database;

import com.senao.warehouse.AppController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Locator{
    private String subinventory;
    private String locator;
    private BigDecimal qty;
    private DateCode[] dateCodeList;
    private Map<String,DateCode[]> locator_dateCodeList = new HashMap<>(); //20210804加入，同倉別不同儲位要顯示在一起
    private List<Map<String,DateCode[]>> data = new ArrayList();

    public Map<String, DateCode[]> getLocator_dateCodeList() {
        return locator_dateCodeList;
    }

    public void setLocator_dateCodeList(String locator, List<DateCode> dateCodeList) {
        //this.locator_dateCodeList = new HashMap<>();
        DateCode[] dateCodeList_tmp = new DateCode[dateCodeList.size()];
        Iterator<DateCode> iter = dateCodeList.iterator();

        /*for (Map.Entry<String, DateCode[]> entry : locator_dateCodeList.entrySet()) {

        }*/

        int i = 0;

        while (iter.hasNext()) {
            dateCodeList_tmp[i] = iter.next();
            i++;
        }

        locator_dateCodeList.put(locator, dateCodeList_tmp);
        //locator_dateCodeList = [key:Q0108,value:[DateCode{dateCode='2118314219', qty=60, locator='Q0108'}, DateCode{dateCode='2123314219', qty=100, locator='Q0108'}] ]
        //locator_dateCodeList = [key:Q0107,value:[DateCode{dateCode='2118314219', qty=60, locator='Q0108'}, DateCode{dateCode='2123314219', qty=100, locator='Q0108'}, DateCode{dateCode='2118314219', qty=10, locator='Q0107'}] ]
        for (Map.Entry<String, DateCode[]> entry : locator_dateCodeList.entrySet()) {
            AppController.debug("locator_dateCodeList = [key:" + entry.getKey() + ",value:" + Arrays.toString(entry.getValue())+" ]");
        }
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

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public DateCode[] getDateCodeList() {
        return dateCodeList;
    }

    public void setDateCodeList(List<DateCode> list) {
        this.dateCodeList = new DateCode[list.size()];
        Iterator<DateCode> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            dateCodeList[i] = iter.next();
            i++;
        }
    }

    @Override
    public String toString() {
        return "Locator{" +
                "subinventory='" + subinventory + '\'' +
                ", locator='" + locator + '\'' +
                ", qty=" + qty +
                ", dateCodeList=" + Arrays.toString(dateCodeList) +
                ", locator_dateCodeList=" + locator_dateCodeList +
                '}';
    }
}
