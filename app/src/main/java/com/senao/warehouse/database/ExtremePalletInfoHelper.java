package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class ExtremePalletInfoHelper extends BasicHelper {
    private int dnNo;
    private String palletNo;
    private ExtremePalletItem[] infoList;

    public int getDnNo() {
        return dnNo;
    }

    public void setDnNo(int dnNo) {
        this.dnNo = dnNo;
    }

    public String getPalletNo() {
        return palletNo;
    }

    public void setPalletNo(String palletNo) {
        this.palletNo = palletNo;
    }

    public ExtremePalletItem[] getInfoList() {
        return infoList;
    }

    public void setInfoList(List<ExtremePalletItem> list) {
        this.infoList = new ExtremePalletItem[list.size()];
        Iterator<ExtremePalletItem> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            infoList[i] = iter.next();
            i++;
        }
    }
}
