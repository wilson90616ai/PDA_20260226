package com.senao.warehouse.database;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ItemOnHandType {
    private String type;
    private BigDecimal qty;
    private Locator[] locatorList;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public Locator[] getLocatorList() {
        return locatorList;
    }

    public void setLocatorList(List<Locator> list) {
        this.locatorList = new Locator[list.size()];
        Iterator<Locator> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            this.locatorList[i] = iter.next();
            i++;
        }
    }

    @Override
    public String toString() {
        return "ItemOnHandType{" +
                "type='" + type + '\'' +
                ", qty=" + qty +
                ", locatorList=" + Arrays.toString(locatorList) +
                '}';
    }
}
