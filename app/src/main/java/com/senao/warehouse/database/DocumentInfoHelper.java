package com.senao.warehouse.database;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

public class DocumentInfoHelper extends BasicHelper {
    private String documentNo;
    private String processNo;
    private String applicationDepartmentId;
    private String applicationDepartment;
    private String applicant;
    private String applicantId;
    private BigDecimal qty;
    private BigDecimal processQty;
    private String status;
    private String workItemName; //目前關卡
    private String userId;
    private ItemInfoHelper[] list;

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

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public BigDecimal getProcessQty() {
        return processQty;
    }

    public void setProcessQty(BigDecimal processQty) {
        this.processQty = processQty;
    }

    public ItemInfoHelper[] getList() {
        return list;
    }

    public void setList(List<ItemInfoHelper> list) {
        this.list = new ItemInfoHelper[list.size()];
        Iterator<ItemInfoHelper> iter = list.iterator();
        int i = 0;

        while (iter.hasNext()) {
            this.list[i] = iter.next();
            i++;
        }
    }

    public String getApplicationDepartmentId() {
        return applicationDepartmentId;
    }

    public void setApplicationDepartmentId(String applicationDepartmentId) {
        this.applicationDepartmentId = applicationDepartmentId;
    }

    public String getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
