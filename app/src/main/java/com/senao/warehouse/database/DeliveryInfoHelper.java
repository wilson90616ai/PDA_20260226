package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class DeliveryInfoHelper extends BasicHelper {
    private int deliveryID;
    private String customer;
    private String shipDate;
    private String opStatus;
    private int selectedItemId;
    private String selectedItemNo;
    private int selectedOeId;
    private ItemInfoHelper[] itemInfo;

    public ItemInfoHelper[] getItemInfo() {
        return itemInfo;
    }

    public void setItemInfo(List<ItemInfoHelper> list) {
        this.itemInfo = new ItemInfoHelper[list.size()];
        Iterator<ItemInfoHelper> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            itemInfo[i] = iter.next();
            i++;
        }
    }

    public int getDeliveryID() {
        return deliveryID;
    }

    public void setDeliveryID(int deliveryID) {
        this.deliveryID = deliveryID;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getShipDate() {
        return shipDate;
    }

    public void setShipDate(String shipDate) {
        this.shipDate = shipDate;
    }

    public String getOpStatus() {
        return opStatus;
    }

    public void setOpStatus(String opStatus) {
        this.opStatus = opStatus;
    }

    public int getSelectedItemId() {
        return selectedItemId;
    }

    public void setSelectedItemId(int selectedItemId) {
        this.selectedItemId = selectedItemId;
    }

    public String getSelectedItemNo() {
        return selectedItemNo;
    }

    public void setSelectedItemNo(String selectedItemNo) {
        this.selectedItemNo = selectedItemNo;
    }

    public int getSelectedOeId() {
        return selectedOeId;
    }

    public void setSelectedOeId(int selectedOeId) {
        this.selectedOeId = selectedOeId;
    }
}
