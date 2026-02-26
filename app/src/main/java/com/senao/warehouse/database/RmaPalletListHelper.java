package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class RmaPalletListHelper extends BasicHelper {
    private RmaPalletInfoHelper[] list;

    public RmaPalletInfoHelper[] getList() {
        return list;
    }

    public void setList(List<RmaPalletInfoHelper> list) {
        this.list = new RmaPalletInfoHelper[list.size()];
        Iterator<RmaPalletInfoHelper> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            this.list[i] = iter.next();
            i++;
        }
    }
}
