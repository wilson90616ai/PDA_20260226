package com.senao.warehouse.database;

/**
 * Created by 102069 on 2016/10/7.
 */

public class ConditionHelper extends BasicHelper {
    private String wareHouseNo;
    private String partNo;
    private String partNoPrefix;
    private String receiptsNo;
    private String trxIdNo;
    private String vendorCode;
    private String searchText;
    private String subinventory;
    private String locator;
    private String manufacturer;

    public String getWareHouseNo() {
        return wareHouseNo;
    }

    public void setWareHouseNo(String wareHouseNo) {
        this.wareHouseNo = wareHouseNo;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public String getPartNoPrefix() {
        return partNoPrefix;
    }

    public void setPartNoPrefix(String partNoPrefix) {
        this.partNoPrefix = partNoPrefix;
    }

    public String getReceiptsNo() {
        return receiptsNo;
    }

    public void setReceiptsNo(String receiptsNo) {
        this.receiptsNo = receiptsNo;
    }

    public String getTrxIdNo() {
        return trxIdNo;
    }

    public void setTrxIdNo(String trxIdNo) {
        this.trxIdNo = trxIdNo;
    }

    public void setSearchText(String searchText) {
        this.searchText =searchText;
    }

    public String getSearchText() {
        return searchText;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getLocator() {
        return locator;
    }

    public void setLocator(String locator) {
        this.locator = locator;
    }

    public String getSubinventory() {
        return subinventory;
    }

    public void setSubinventory(String subinventory) {
        this.subinventory = subinventory;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    @Override
    public String toString() {
        return "ConditionHelper{" +
                "wareHouseNo='" + wareHouseNo + '\'' +
                ", partNo='" + partNo + '\'' +
                ", partNoPrefix='" + partNoPrefix + '\'' +
                ", receiptsNo='" + receiptsNo + '\'' +
                ", trxIdNo='" + trxIdNo + '\'' +
                ", vendorCode='" + vendorCode + '\'' +
                ", searchText='" + searchText + '\'' +
                ", subinventory='" + subinventory + '\'' +
                ", locator='" + locator + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                '}';
    }
}
