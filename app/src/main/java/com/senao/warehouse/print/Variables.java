package com.senao.warehouse.print;

import com.senao.warehouse.database.BasicHelper;

public class Variables extends BasicHelper {
    private String HOURS;
    private String ORI_PACK_QTY;
    private String REELID;
    private String MSL;
    private int ORGANIZATION_ID;

    //Constructor
    public Variables(String HOURS, String ORI_PACK_QTY, String REELID, String MSL,int ORGANIZATION_ID) {
        this.HOURS = HOURS;
        this.ORI_PACK_QTY = ORI_PACK_QTY;
        this.REELID = REELID;
        this.MSL = MSL;
        this.ORGANIZATION_ID = ORGANIZATION_ID;
    }

    public int getORGANIZATION_ID() {
        return ORGANIZATION_ID;
    }

    public void setORGANIZATION_ID(int ORGANIZATION_ID) {
        this.ORGANIZATION_ID = ORGANIZATION_ID;
    }

    //Getters and setters
    public String getHOURS() {
        return HOURS;
    }

    public void setHOURS(String HOURS) {
        this.HOURS = HOURS;
    }

    public String getORI_PACK_QTY() {
        return ORI_PACK_QTY;
    }

    public void setORI_PACK_QTY(String ORI_PACK_QTY) {
        this.ORI_PACK_QTY = ORI_PACK_QTY;
    }

    public String getREELID() {
        return REELID;
    }

    public void setREELID(String REELID) {
        this.REELID = REELID;
    }

    public String getMSL() {
        return MSL;
    }

    public void setMSL(String MSL) {
        this.MSL = MSL;
    }

    @Override
    public String toString() {
        return "Variables{" +
                "HOURS='" + HOURS + '\'' +
                ", ORI_PACK_QTY='" + ORI_PACK_QTY + '\'' +
                ", REELID='" + REELID + '\'' +
                ", MSL='" + MSL + '\'' +
                '}';
    }
}
