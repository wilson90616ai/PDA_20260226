package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class RmaBoxInfoHelper extends  RmaInfoHelper {
    private String boxNo;
    private String workOrderNo;
    private String sn;
    private String[] snList;
    private String comp;

    public String getComp() {
        return comp;
    }

    public void setComp(String comp) {
        this.comp = comp;
    }

    public String[] getSnList() {
        return snList;
    }

    public void setSnList(List<String> snList) {
        this.snList = new String[snList.size()];
        Iterator<String> iter = snList.iterator();
        int i = 0;

        while (iter.hasNext()) {
            this.snList[i] = iter.next();
            i++;
        }
    }

    public String getWorkOrderNo() {
        return workOrderNo;
    }

    public void setWorkOrderNo(String workOrderNo) {
        this.workOrderNo = workOrderNo;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getBoxNo() {
        return boxNo;
    }

    public void setBoxNo(String boxNo) {
        this.boxNo = boxNo;
    }
}
