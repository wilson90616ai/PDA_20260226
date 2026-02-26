package com.senao.warehouse.database;

public class InvoiceHelper extends BasicHelper {
    private int vendorId;
    private String invoice;
    private String seq;
    private boolean isSupplementary;
    private String vendorName;
    private boolean byBtn;

    public String getInvoice() {
        return invoice;
    }

    public void setInvoice(String invoice) {
        this.invoice = invoice;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public boolean isSupplementary() {
        return isSupplementary;
    }

    public void setSupplementary(boolean supplementary) {
        isSupplementary = supplementary;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public boolean isByBtn() {
        return byBtn;
    }

    public void setByBtn(boolean byBtn) {
        this.byBtn = byBtn;
    }

    public int getVendorId() {
        return vendorId;
    }

    public void setVendorId(int vendorId) {
        this.vendorId = vendorId;
    }

    @Override
    public String toString() {
        return "InvoiceHelper{" +
                "vendorId=" + vendorId +
                ", invoice='" + invoice + '\'' +
                ", seq='" + seq + '\'' +
                ", isSupplementary=" + isSupplementary +
                ", vendorName='" + vendorName + '\'' +
                ", byBtn=" + byBtn +
                '}';
    }
}
