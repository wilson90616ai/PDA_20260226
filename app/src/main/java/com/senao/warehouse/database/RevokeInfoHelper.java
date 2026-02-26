package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class RevokeInfoHelper extends BasicHelper{
    private DocumentInfoHelper[] infoList;
    private String documentNo;
    private String processNo;
    private String applicationDepartmentId;
    private String applicantId;

    public DocumentInfoHelper[] getInfoList() {
        return infoList;
    }

    public void setInfoList(List<DocumentInfoHelper> list) {
        this.infoList = new DocumentInfoHelper[list.size()];
        Iterator<DocumentInfoHelper> iterator = list.iterator();
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
}
