package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class TransferInfoHelper extends BasicHelper {
    private TransferItemInfoHelper[] infoList;
    private String userId;
    private String searchText;
    private int BatchId;
    private String formSn;
    private String processSn;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getBatchId() {
        return BatchId;
    }

    public void setBatchId(int batchId) {
        BatchId = batchId;
    }

    public TransferItemInfoHelper[] getInfoList() {
        return infoList;
    }

    public void setInfoList(List<TransferItemInfoHelper> list) {
        this.infoList = new TransferItemInfoHelper[list.size()];
        Iterator<TransferItemInfoHelper> iterator = list.iterator();
        int i = 0;

        while (iterator.hasNext()) {
            infoList[i] = iterator.next();
            i++;
        }
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getFormSn() {
        return formSn;
    }

    public void setFormSn(String formSn) {
        this.formSn = formSn;
    }

    public String getProcessSn() {
        return processSn;
    }

    public void setProcessSn(String processSn) {
        this.processSn = processSn;
    }
}
