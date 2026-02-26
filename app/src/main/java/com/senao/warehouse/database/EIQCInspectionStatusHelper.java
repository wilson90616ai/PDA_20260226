package com.senao.warehouse.database;

public class EIQCInspectionStatusHelper extends ApiAuthHelper{
    private String PREELID;
    private String PORGANIZATION_ID;
    private String RSTATUS;

    public EIQCInspectionStatusHelper() {
    }

    public EIQCInspectionStatusHelper(String username, String password, String token, String PREELID, String PORGANIZATION_ID, String RSTATUS) {
        super(username, password, token);
        this.PREELID = PREELID;
        this.PORGANIZATION_ID = PORGANIZATION_ID;
        this.RSTATUS = RSTATUS;
    }

    public EIQCInspectionStatusHelper(String PREELID, String PORGANIZATION_ID, String RSTATUS) {
        this.PREELID = PREELID;
        this.PORGANIZATION_ID = PORGANIZATION_ID;
        this.RSTATUS = RSTATUS;
    }

    public String getPREELID() {
        return PREELID;
    }

    public void setPREELID(String PREELID) {
        this.PREELID = PREELID;
    }

    public String getPORGANIZATION_ID() {
        return PORGANIZATION_ID;
    }

    public void setPORGANIZATION_ID(String PORGANIZATION_ID) {
        this.PORGANIZATION_ID = PORGANIZATION_ID;
    }

    public String getRSTATUS() {
        return RSTATUS;
    }

    public void setRSTATUS(String RSTATUS) {
        this.RSTATUS = RSTATUS;
    }
}
