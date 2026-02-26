package com.senao.warehouse.database;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ItemOnHand {
    private int itemId;
    private String itemNo;
    private String itemDescription; //這是品名啦........
    private String itemControl;
    private BigDecimal qty;
    private ItemOnHandType[] typeList;

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemNo() {
        return itemNo;
    }

    public void setItemNo(String itemNo) {
        this.itemNo = itemNo;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getItemControl() {
        return itemControl;
    }

    public void setItemControl(String itemControl) {
        this.itemControl = itemControl;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public ItemOnHandType[] getTypeList() {
        return typeList;
    }

    public void setTypeList(List<ItemOnHandType> list) {
        this.typeList = new ItemOnHandType[list.size()];
        Iterator<ItemOnHandType> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            this.typeList[i] = iter.next();
            i++;
        }
    }

    @Override
    public String toString() {
        return "ItemOnHand{" +
                "itemId=" + itemId +
                ", itemNo='" + itemNo + '\'' +
                ", itemDescription='" + itemDescription + '\'' +
                ", itemControl='" + itemControl + '\'' +
                ", qty=" + qty +
                ", typeList=" + Arrays.toString(typeList) +
                '}';
    }
}
