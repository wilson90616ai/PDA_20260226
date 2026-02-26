package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class ItemPoInfoHelper extends BasicHelper {
    private int deliveryID;
    private int id;
    private String itemID;
    private OeInfo[] oeInfos;

    public int getDeliveryID() {
        return deliveryID;
    }

    public void setDeliveryID(int deliveryID) {
        this.deliveryID = deliveryID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public OeInfo[] getOeInfos() {
        return oeInfos;
    }

    public void setOeInfos(List<OeInfo> list) {
        this.oeInfos = new OeInfo[list.size()];
        Iterator<OeInfo> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            oeInfos[i] = iter.next();
            i++;
        }
    }
}
