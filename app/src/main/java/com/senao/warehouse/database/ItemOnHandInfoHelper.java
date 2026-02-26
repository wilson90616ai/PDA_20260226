package com.senao.warehouse.database;

import com.senao.warehouse.AppController;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ItemOnHandInfoHelper extends BasicHelper {
    private int type; //0.全部 1.待驗 2.庫存
    private String itemNo;
    private String subinventory;
    private String locator;
    private ItemOnHand[] list;
    private ItemOnHandRecord[] rowList;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getItemNo() {
        return itemNo;
    }

    public void setItemNo(String itemNo) {
        this.itemNo = itemNo;
    }

    public String getSubinventory() {
        return subinventory;
    }

    public void setSubinventory(String subinventory) {
        this.subinventory = subinventory;
    }

    public String getLocator() {
        return locator;
    }

    public void setLocator(String locator) {
        this.locator = locator;
    }

    public ItemOnHand[] getList() {
        return list;
    }

    public void setList(List<ItemOnHand> list) {
        this.list = new ItemOnHand[list.size()];
        Iterator<ItemOnHand> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            this.list[i] = iter.next();
            i++;
        }
    }

    public ItemOnHandRecord[] getRowList() {
        return rowList;
    }

    public void setRowList(List<ItemOnHandRecord> rowList) {
        this.rowList = new ItemOnHandRecord[rowList.size()];
        Iterator<ItemOnHandRecord> iter = rowList.iterator();
        int i = 0;

        while (iter.hasNext()) {
            this.rowList[i] = iter.next();
            AppController.debug("conditionHelper rowList = " + this.rowList[i]);
            i++;
        }
    }

    @Override
    public String toString() {
        /*StringBuilder sb = new StringBuilder();
        sb.append("ItemOnHandInfoHelper{" +
                "type=" + type +
                ", itemNo='" + itemNo + '\'' +
                ", subinventory='" + subinventory + '\'' +
                ", locator='" + locator + '\'' +
                ", list=" + Arrays.toString(list)+
                 ", rowList="
                );

        for(ItemOnHandRecord item:rowList){
            sb.append(item+", ");
        }

        return sb.toString();*/

        return "ItemOnHandInfoHelper{" +
                "type=" + type +
                ", itemNo='" + itemNo + '\'' +
                ", subinventory='" + subinventory + '\'' +
                ", locator='" + locator + '\'' +
                ", list=" + Arrays.toString(list) +
                ", rowList=" + Arrays.toString(rowList) +
                '}';
    }
}
