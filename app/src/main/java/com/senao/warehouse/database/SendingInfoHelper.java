package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class SendingInfoHelper extends BasicHelper {
    private String lineNo;
    private String mergeNo;
    private String whNo;
    private String woNo;
    private String trxType;
    private String searchText;
    private String itemNo;
    private String subinventory;
    private String locator;
    private String itemPart;
    private SendOnHandQtyInfo[] onHandQtyList;

    public String getItemNo() {
        return itemNo;
    }

    public void setItemNo(String itemNo) {
        this.itemNo = itemNo;
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

    public String getLineNo() {
        return lineNo;
    }

    public void setLineNo(String lineNo) {
        this.lineNo = lineNo;
    }

    public String getMergeNo() {
        return mergeNo;
    }

    public void setMergeNo(String mergeNo) {
        this.mergeNo = mergeNo;
    }

    public String getWhNo() {
        return whNo;
    }

    public void setWhNo(String whNo) {
        this.whNo = whNo;
    }

    public String getWoNo() {
        return woNo;
    }

    public void setWoNo(String woNo) {
        this.woNo = woNo;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public SendOnHandQtyInfo[] getOnHandQtyList() {
        return onHandQtyList;
    }

    public void setOnHandQtyList(List<SendOnHandQtyInfo> list) {
        this.onHandQtyList = new SendOnHandQtyInfo[list.size()];
        Iterator<SendOnHandQtyInfo> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            onHandQtyList[i] = iter.next();
            i++;
        }
    }

    public String getTrxType() {
        return trxType;
    }

    public void setTrxType(String trxType) {
        this.trxType = trxType;
    }

    public String getItemPart() {
        return itemPart;
    }

    public void setItemPart(String itemPart) {
        this.itemPart = itemPart;
    }
}
