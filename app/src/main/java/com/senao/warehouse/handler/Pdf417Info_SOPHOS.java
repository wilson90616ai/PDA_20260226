package com.senao.warehouse.handler;

/*
 * 20250818 Edit By Ann:新增usage_type(SENAO101-202506-0226)
 */

import java.util.List;

public class Pdf417Info_SOPHOS {
    private int dnNo;
    private String palletNo;
    private String partNo;
    private String customerPartNo;
    private String modelName;
    private List<String> snList;
    private List<String> BoxList;
    private int printNo;
    private String errorBuf;
    private String invoiceNo;
    private String workNo;
    private int qty;
    private int boxQty;
    private List<Float> BoxWeight;
    private String customerPO;
    private String customerPartNoDescription;
    private String receiver ;
    private String address ;
    private String contactPhone ;
    private List<Integer> Ship_Qty;
    private String usage_type;

    public String getCustomerPartNoDescription() {
        return customerPartNoDescription;
    }

    public void setCustomerPartNoDescription(String customerPartNoDescription) {
        this.customerPartNoDescription = customerPartNoDescription;
    }

    public int getBoxQty() {
        return boxQty;
    }

    public void setBoxQty(int boxQty) {
        this.boxQty = boxQty;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public String getCustomerPartNo() {
        return customerPartNo;
    }

    public void setCustomerPartNo(String customerPartNo) {
        this.customerPartNo = customerPartNo;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public List<String> getSnList() {
        return snList;
    }

    public void setSnList(List<String> snList) {
        this.snList = snList;
    }

    public int getPrintNo() {
        return printNo;
    }

    public void setPrintNo(int printNo) {
        this.printNo = printNo;
    }

    public int getDnNo() {
        return dnNo;
    }

    public void setDnNo(int dnNo) {
        this.dnNo = dnNo;
    }

    public String getPalletNo() {
        return palletNo;
    }

    public void setPalletNo(String palletNo) {
        this.palletNo = palletNo;
    }

    public String getErrorBuf() {
        return errorBuf;
    }

    public void setErrorBuf(String errorBuf) {
        this.errorBuf = errorBuf;
    }

    public List<Float> getBoxWeight() {
        return BoxWeight;
    }

    public void setBoxWeight(List<Float> boxWeight) {
        this.BoxWeight = boxWeight;
    }

    public String getCustomerPO() {
        return customerPO;
    }

    public void setCustomerPO(String customerPO) {
        this.customerPO = customerPO;
    }

    public List<String> getBoxList() {
        return BoxList;
    }

    public void setBoxList(List<String> boxList) {
        BoxList = boxList;
    }

    public void setWorkNo(String workNo) {
        this.workNo = workNo;
    }

    public String getWorkNo() {
        return workNo;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public List<Integer> getShip_Qty() {
        return Ship_Qty;
    }

    public void setShip_Qty(List<Integer> ship_Qty) {
        Ship_Qty = ship_Qty;
    }

    public String getUsage_Type() {
        return usage_type;
    }

    public void setUsage_Type(String usage_type) {
        this.usage_type = usage_type;
    }

    @Override
    public String toString() {
        return "Pdf417Info_SOPHOS{" +
                "dnNo=" + dnNo +
                ", palletNo='" + palletNo + '\'' +
                ", partNo='" + partNo + '\'' +
                ", customerPartNo='" + customerPartNo + '\'' +
                ", customerPartNoDescription='" + customerPartNoDescription + '\'' +
                ", modelName='" + modelName + '\'' +
                ", snList=" + snList +
                ", printNo=" + printNo +
                ", errorBuf='" + errorBuf + '\'' +
                ", invoiceNo='" + invoiceNo + '\'' +
                ", qty=" + qty +'\'' +
                ", boxQty=" + boxQty +'\'' +
                ", customerPO=" + customerPO +'\'' +
                ", boxWeight=" + BoxWeight +'\'' +
                ", usage_type=" + usage_type +
                '}';
    }
}
