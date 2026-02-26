package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class SamsaraPoInfoHelper extends BasicHelper {
    private int dn;
    private String partNo;
    private String sn;
    private String[] poList;

    public String[] getPoList() {
        return poList;
    }

    public void setPoList(List<String> list) {
        this.poList = new String[list.size()];
        Iterator<String> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            poList[i] = iter.next();
            i++;
        }
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public int getDn() {
        return dn;
    }

    public void setDn(int dn) {
        this.dn = dn;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }
}
