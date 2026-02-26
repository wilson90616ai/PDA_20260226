package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class OobaListHelper extends BasicHelper {
    private int itemId;
    private String itemNo;
    private String ooba;
    private OobaItem[] oobaList;

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemNo() {
        return itemNo;
    }

    public void setItemNo(String itemNo) {
        this.itemNo = itemNo;
    }

    public OobaItem[] getOobaList() {
        return oobaList;
    }

    public void setOobaList(List<OobaItem> list) {
        this.oobaList = new OobaItem[list.size()];
        Iterator<OobaItem> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            oobaList[i] = iter.next();
            i++;
        }
    }

    public String getOoba() {
        return ooba;
    }

    public void setOoba(String ooba) {
        this.ooba = ooba;
    }
}
