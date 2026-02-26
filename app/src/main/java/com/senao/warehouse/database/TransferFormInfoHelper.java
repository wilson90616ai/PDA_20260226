package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class TransferFormInfoHelper extends BasicHelper {
    private TransferLocatorFormInfoHelper[] infoList;
    private String warehouseNo;
    private String partNoPrefix;
    private String documentNo;
    private String processNo;
    private String applicationDepartment;
    private String applicant;
    private String searchText;

    public TransferLocatorFormInfoHelper[] getInfoList() {
        return infoList;
    }

    public void setInfoList(List<TransferLocatorFormInfoHelper> list) {
        this.infoList = new TransferLocatorFormInfoHelper[list.size()];
        Iterator<TransferLocatorFormInfoHelper> iterator = list.iterator();
        int i = 0;

        while (iterator.hasNext()) {
            infoList[i] = iterator.next();
            i++;
        }
    }

    public String getDocumentNo() {
        return documentNo;
    }

    public void setDocumentNo(String documentNo) {
        this.documentNo = documentNo;
    }

    public String getProcessNo() {
        return processNo;
    }

    public void setProcessNo(String processNo) {
        this.processNo = processNo;
    }

    public String getApplicationDepartment() {
        return applicationDepartment;
    }

    public void setApplicationDepartment(String applicationDepartment) {
        this.applicationDepartment = applicationDepartment;
    }

    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(String applicant) {
        this.applicant = applicant;
    }

    public String getWarehouseNo() {
        return warehouseNo;
    }

    public void setWarehouseNo(String warehouseNo) {
        this.warehouseNo = warehouseNo;
    }

    public String getPartNoPrefix() {
        return partNoPrefix;
    }

    public void setPartNoPrefix(String partNoPrefix) {
        this.partNoPrefix = partNoPrefix;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
}
