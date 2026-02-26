package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class MaterialLabelHelper extends BasicHelper {
    private String partNo;
    private String vendorCode;
    private String dateCode;
    private String labelSize; //L, M, S
    private int labelCount;
    private String seqNo;
    private String[] reelIds;

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getDateCode() {
        return dateCode;
    }

    public void setDateCode(String dateCode) {
        this.dateCode = dateCode;
    }

    public String getLabelSize() {
        return labelSize;
    }

    public void setLabelSize(String labelSize) {
        this.labelSize = labelSize;
    }

    public int getLabelCount() {
        return labelCount;
    }

    public void setLabelCount(int labelCount) {
        this.labelCount = labelCount;
    }

    public String[] getReelIds() {
        return reelIds;
    }

    public void setReelIds(List<String> reelIds) {
        this.reelIds = new String[reelIds.size()];
        Iterator<String> iter = reelIds.iterator();
        int i = 0;

        while (iter.hasNext()) {
            this.reelIds[i] = iter.next();
            i++;
        }
    }

    /* @return the seqNo
     */
    public String getSeqNo() {
        return seqNo;
    }

    /* @param seqNo the seqNo to set
     */
    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }
}
