package com.senao.warehouse.database;

import java.util.Date;

public class OverTakeHelper extends BasicHelper {
    //RCV_OPEN_DAY number 控管天數
    //ITEM_DESC varchar2 料號
    //LAST_NAME varchar2 採購員
    //LEAD_TIME date 可收日期
    //PO_QUANTITY number 可收總數
    //WS_NAME varchar2
    //WS_DESC varchar2

    private int controlDay; //控管天數
    private String itemNo; //料號
    private String lastNAME; //採購員
    private String couldReceiveDate; //可收日期
    private int couldReceiveNum; //可收總數
    private String WSNAME; //
    private String WSDESC; //

    public String getLastNAME() {
        return lastNAME;
    }

    public void setLastNAME(String lastNAME) {
        this.lastNAME = lastNAME;
    }

    public String getItemNo() {
        return itemNo;
    }

    public void setItemNo(String itemNo) {
        this.itemNo = itemNo;
    }

    public int  getControlDay() {
        return controlDay;
    }

    public void setControlDay(int  controlDay) {
        this.controlDay = controlDay;
    }

    public String getCouldReceiveDate() {
        return couldReceiveDate;
    }

    public void setCouldReceiveDate(String couldReceiveDate) {
        this.couldReceiveDate = couldReceiveDate;
    }

    public int getCouldReceiveNum() {
        return couldReceiveNum;
    }

    public void setCouldReceiveNum(int couldReceiveNum) {
        this.couldReceiveNum = couldReceiveNum;
    }

    public String getWSNAME() {
        return WSNAME;
    }

    public void setWSNAME(String wSNAME) {
        WSNAME = wSNAME;
    }

    public String getWSDESC() {
        return WSDESC;
    }

    public void setWSDESC(String wSDESC) {
        WSDESC = wSDESC;
    }
}
