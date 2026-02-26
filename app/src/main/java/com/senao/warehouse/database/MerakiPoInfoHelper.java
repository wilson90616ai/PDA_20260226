package com.senao.warehouse.database;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MerakiPoInfoHelper extends BasicHelper{
    private int dn;
    private String partNo;
    private String sn;
    private String carton_no;
    private String carton_weight;
    private String[] poList;
    private Map<String, Integer> poCount = new HashMap<String, Integer>() ;

    public Map<String, Integer> getPoCount() {
        return poCount;
    }

    public void setPoCount(Map<String, Integer> poCount) {
        this.poCount = poCount;
    }

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

    public String getCarton_weight() {
        return carton_weight;
    }

    public void setCarton_weight(String carton_weight) {
        this.carton_weight = carton_weight;
    }

    public String getCarton_no() {
        return carton_no;
    }

    public void setCarton_no(String carton_no) {
        this.carton_no = carton_no;
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
