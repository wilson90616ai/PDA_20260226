package com.senao.warehouse.database;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

public class TransferLocatorFormInfoHelper  extends  BasicHelper {
    private String status;
    private String workItemName;
    private String type;
    private BigDecimal totalNumber;
    private BigDecimal totalIn;
    private String formSerialNumber;
    private String processSerialNumber;
    private TransferLocatorFormItemInfoHelper[] list;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWorkItemName() {
        return workItemName;
    }

    public void setWorkItemName(String workItemName) {
        this.workItemName = workItemName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getTotalNumber() {
        return totalNumber;
    }

    public void setTotalNumber(BigDecimal totalNumber) {
        this.totalNumber = totalNumber;
    }

    public BigDecimal getTotalIn() {
        return totalIn;
    }

    public void setTotalIn(BigDecimal totalIn) {
        this.totalIn = totalIn;
    }

    public String getFormSerialNumber() {
        return formSerialNumber;
    }

    public void setFormSerialNumber(String formSerialNumber) {
        this.formSerialNumber = formSerialNumber;
    }

    public TransferLocatorFormItemInfoHelper[] getList() {
        return list;
    }

    public void setList(List<TransferLocatorFormItemInfoHelper> list) {
        this.list = new TransferLocatorFormItemInfoHelper[list.size()];
        Iterator<TransferLocatorFormItemInfoHelper> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            this.list[i] = iter.next();
            i++;
        }
    }

    public String getProcessSerialNumber() {
        return processSerialNumber;
    }

    public void setProcessSerialNumber(String processSerialNumber) {
        this.processSerialNumber = processSerialNumber;
    }
}
