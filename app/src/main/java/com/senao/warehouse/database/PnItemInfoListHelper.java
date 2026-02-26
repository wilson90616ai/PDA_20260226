package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class PnItemInfoListHelper extends BasicHelper {
    private PnItemInfoHelper[] pnList;

    public PnItemInfoHelper[] getPnList() {
        return pnList;
    }

    public void setPnList(List<PnItemInfoHelper> list) {
        this.pnList = new PnItemInfoHelper[list.size()];
        Iterator<PnItemInfoHelper> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            pnList[i] = iter.next();
            i++;
        }
    }
}
