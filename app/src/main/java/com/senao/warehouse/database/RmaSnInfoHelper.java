package com.senao.warehouse.database;

public class RmaSnInfoHelper extends  RmaInfoHelper {
    private String boxNo;
    private int type;
    private String sn;

    public String getBoxNo() {
        return boxNo;
    }

    public void setBoxNo(String boxNo) {
        this.boxNo = boxNo;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }
}
