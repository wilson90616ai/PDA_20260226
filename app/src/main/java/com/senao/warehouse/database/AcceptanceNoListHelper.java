package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class AcceptanceNoListHelper extends BasicHelper {
    private String partNo;
    private AcceptanceNoHelper[] noList;

    public AcceptanceNoHelper[] getNoList() {
        return noList;
    }

    public void setNoList(List<AcceptanceNoHelper> list) {
        this.noList = new AcceptanceNoHelper[list.size()];
        Iterator<AcceptanceNoHelper> iter = list.iterator();
        int i = 0;
        while (iter.hasNext()) {
            noList[i] = iter.next();
            i++;
        }
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }
}
