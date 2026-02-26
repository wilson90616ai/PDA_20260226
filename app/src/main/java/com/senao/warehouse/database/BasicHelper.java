package com.senao.warehouse.database;


import com.senao.warehouse.AppController;

public class BasicHelper {
    private String strErrorBuf;
    private int intRetCode;
    private int org;
    private String orgName;
    private int ou;

    public BasicHelper() {
        setOrg(AppController.getOrg());
        setOrgName(AppController.getOrgName());
        setOu(AppController.getOu());
    }

    public String getStrErrorBuf() {
        return strErrorBuf;
    }

    public void setStrErrorBuf(String strErrorBuf) {
        this.strErrorBuf = strErrorBuf;
    }

    public int getIntRetCode() {
        return intRetCode;
    }

    public void setIntRetCode(int intRetCode) {
        this.intRetCode = intRetCode;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public int getOrg() {
        return org;
    }

    public void setOrg(int org) {
        this.org = org;
    }

    public int getOu() {
        return ou;
    }

    public void setOu(int ou) {
        this.ou = ou;
    }

    @Override
    public String toString() {
        return "BasicHelper{" +
                "strErrorBuf='" + strErrorBuf + '\'' +
                ", intRetCode=" + intRetCode +
                ", org=" + org +
                ", ou=" + ou +
                '}';
    }
}
