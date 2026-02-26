package com.senao.warehouse.handler;

import com.senao.warehouse.database.BasicHelper;

public class OuOrgHelper {
    private int ouId;
    private String ouName;
    private int orgId;
    private String orgEName;

    public int getOuId() {
        return ouId;
    }

    public void setOuId(int ouId) {
        this.ouId = ouId;
    }

    public String getOuName() {
        return ouName;
    }

    public void setOuName(String ouName) {
        this.ouName = ouName;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public String getOrgEName() {
        return orgEName;
    }

    public void setOrgEName(String orgName) {
        this.orgEName = orgName;
    }
}
