package com.senao.warehouse.database;

import com.senao.warehouse.AppController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class RmaBoxListInfoHelper extends  BasicHelper {
    private RmaBoxInfoHelper[] boxInfoList;
    private int printNo;
    private String palletNo;
    private String palletId;
    private boolean isRma;
    private String company;
    private List<RmaInfoHelper> rmaInfoList = new ArrayList<>();
    private HashMap<String, RmaInfoHelper> ht;

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPalletNo() {
        return palletNo;
    }

    public void setPalletNo(String palletNo) {
        this.palletNo = palletNo;
    }

    public String getPalletId() {
        AppController.debug("palletId = "+palletId);
        return palletId;
    }

    public void setPalletId(String palletId) {
        this.palletId = palletId;
    }

    public RmaBoxInfoHelper[] getBoxInfoList() {
        return boxInfoList;
    }

    public void setBoxInfoList(List<RmaBoxInfoHelper> list) {
        this.boxInfoList = new RmaBoxInfoHelper[list.size()];
        Iterator<RmaBoxInfoHelper> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            boxInfoList[i] = iter.next();
            i++;
        }
    }

    public int getPrintNo() {
        return printNo;
    }

    public void setPrintNo(int printNo) {
        this.printNo = printNo;
    }

    public boolean isRma() {
        return isRma;
    }

    public void setRma(boolean rma) {
        isRma = rma;
    }

    public List<RmaInfoHelper> getRmaInfoList() {
        return rmaInfoList;
    }

    public void setRmaInfoList(List<RmaInfoHelper> rmaInfoList) {
        this.rmaInfoList = rmaInfoList;
    }

    @Override
    public String toString() {
        return "RmaBoxListInfoHelper{" +
                "boxInfoList=" + Arrays.toString(boxInfoList) +
                ", printNo=" + printNo +
                ", palletNo='" + palletNo + '\'' +
                ", palletId='" + palletId + '\'' +
                ", isRma=" + isRma +
                ", boxInfoList=" + Arrays.toString(boxInfoList) +
                ", rmaInfoList=" + rmaInfoList +
                '}';
    }
}
