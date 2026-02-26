package com.senao.warehouse.database;

public class ExtremePalletItem {
    private String uniquePackID;
    private String triggerInfo;
    private String skuPn;
    private int qty;
    private String SupplierCode;
    private String brocadeDescription;
    private String countryOfOrigin;

    public String getUniquePackID() {
        return uniquePackID;
    }

    public void setUniquePackID(String uniquePackID) {
        this.uniquePackID = uniquePackID;
    }

    public String getTriggerInfo() {
        return triggerInfo;
    }

    public void setTriggerInfo(String triggerInfo) {
        this.triggerInfo = triggerInfo;
    }

    public String getSkuPn() {
        return skuPn;
    }

    public void setSkuPn(String skuPn) {
        this.skuPn = skuPn;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getSupplierCode() {
        return SupplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        SupplierCode = supplierCode;
    }

    public String getBrocadeDescription() {
        return brocadeDescription;
    }

    public void setBrocadeDescription(String brocadeDescription) {
        this.brocadeDescription = brocadeDescription;
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public void setCountryOfOrigin(String countryOfOrigin) {
        this.countryOfOrigin = countryOfOrigin;
    }
}
