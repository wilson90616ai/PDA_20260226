package com.senao.warehouse.database;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class AcceptanceConditionHelper extends ConditionHelper {
    private AcceptanceInfoHelper[] infoList;

    public AcceptanceInfoHelper[] getInfoList() {
        return infoList;
    }

    public void setInfoList(List<AcceptanceInfoHelper> list) {
        this.infoList = new AcceptanceInfoHelper[list.size()];
        Iterator<AcceptanceInfoHelper> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            infoList[i] = iter.next();
            i++;
        }
    }

    @Override
    public String toString() {
        return "AcceptanceConditionHelper{" +
                "infoList=" + Arrays.toString(infoList) +
                '}';
    }
}
