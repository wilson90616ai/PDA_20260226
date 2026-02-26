package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class WaitCheckQueryConditionHelper extends ConditionHelper {
    private WaitCheckInfoHelper[] infoList;

    public WaitCheckInfoHelper[] getInfoList() {
        return infoList;
    }

    public void setInfoList(List<WaitCheckInfoHelper> list) {
        this.infoList = new WaitCheckInfoHelper[list.size()];
        Iterator<WaitCheckInfoHelper> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            infoList[i] = iter.next();
            i++;
        }
    }
}
