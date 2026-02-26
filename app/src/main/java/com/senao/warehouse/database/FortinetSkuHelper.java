package com.senao.warehouse.database;

public class FortinetSkuHelper {
    private String SKU;
    private String chk;

    public FortinetSkuHelper(String SKU, String chk) {
        this.SKU = SKU;
        this.chk = chk;
    }

    public String getSKU() {
        return SKU;
    }

    public void setSKU(String SKU) {
        this.SKU = SKU;
    }

    public String getChk() {
        return chk;
    }

    public void setChk(String chk) {
        this.chk = chk;
    }
}
