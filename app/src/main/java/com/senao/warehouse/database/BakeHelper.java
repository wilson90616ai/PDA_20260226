package com.senao.warehouse.database;

import android.text.TextUtils;

public class BakeHelper extends ApiAuthHelper{
    private String REELID;
    private String ITEM ; //同REELID
    private String TID; //Package

    private String temperature;
    private String MSL; //施敏等擊
    private String BAKING_TIME; //最低烘烤多久

    private String DECISION;
    private String IPC; //允許最常暴露時間
    private String HOURS; //允許最常暴露時間

    private String STARTDATE;
    private String ENDDATE;
    private String ORI_PACK_QTY; //原包裝數量

    //NH(烘烤多久小時)，OH(烘烤超過多少小時)
    private String NH;
    private String SKU;
    private int ORGANIZATION_ID=0 ;

    private String QTY; //烘烤數量

    private String bakeloc;

    public String getBakeloc() {
        return bakeloc;
    }

    public void setBakeloc(String bakeloc) {
        this.bakeloc = bakeloc;
    }

    public String getQTY() {
        return QTY;
    }

    public void setQTY(String QTY) {
        this.QTY = QTY;
    }

    public String getSKU() {
        if(TextUtils.isEmpty(SKU))
            return REELID.substring(0,12);
        else
            return SKU;
    }

    public void setSKU(String SKU) {
        this.SKU = SKU;
    }

    public String getNH() {
        return NH;
    }

    public void setNH(String NH) {
        this.NH = NH;
    }

    public String getSTARTDATE() {
        return STARTDATE;
    }

    public void setSTARTDATE(String STARTDATE) {
        this.STARTDATE = STARTDATE;
    }

    public String getENDDATE() {
        return ENDDATE;
    }

    public void setENDDATE(String ENDDATE) {
        this.ENDDATE = ENDDATE;
    }

    public String getORI_PACK_QTY() {
        return ORI_PACK_QTY;
    }

    public void setORI_PACK_QTY(String ORI_PACK_QTY) {
        this.ORI_PACK_QTY = ORI_PACK_QTY;
    }

    public String getHOURS() {
        return HOURS;
    }

    public void setHOURS(String HOURS) {
        this.HOURS = HOURS;
    }

    public String getIPC() {
        return IPC;
    }

    public void setIPC(String IPC) {
        this.IPC = IPC;
    }

    public String getDECISION() {
        return DECISION;
    }

    public void setDECISION(String DECISION) {
        this.DECISION = DECISION;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getMSL() {
        return MSL;
    }

    public void setMSL(String MSL) {
        this.MSL = MSL;
    }

    public String getBAKING_TIME() {
        return BAKING_TIME;
    }

    public void setBAKING_TIME(String BAKING_TIME) {
        this.BAKING_TIME = BAKING_TIME;
    }

    public int getORGANIZATION_ID() {
        if(ORGANIZATION_ID==0){
            return super.getOrg();
        }else {
            return ORGANIZATION_ID;
        }
    }

    public void setORGANIZATION_ID(int ORGANIZATION_ID) {
        this.ORGANIZATION_ID = ORGANIZATION_ID;
    }

    public BakeHelper(){
        //ORGANIZATION_ID = super.getOrg();
    }

    public String getREELID() {
        return REELID;
    }

    public void setREELID(String REELID) {
        this.REELID = REELID;
    }

    public String getITEM() {
        return ITEM;
    }

    public void setITEM(String ITEM) {
        this.ITEM = ITEM;
    }

    public String getTID() {
        return TID;
    }

    public void setTID(String TID) {
        this.TID = TID;
    }

    @Override
    public String toString() {
        return "BakeHelper{" +
                "REELID='" + REELID + '\'' +
                ", ITEM='" + ITEM + '\'' +
                ", TID='" + TID + '\'' +
                ", temperature='" + temperature + '\'' +
                ", MSL='" + MSL + '\'' +
                ", BAKING_TIME='" + BAKING_TIME + '\'' +
                ", ORGANIZATION_ID=" + ORGANIZATION_ID +
                '}';
    }
}
