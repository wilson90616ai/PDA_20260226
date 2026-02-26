package com.senao.warehouse.database;

import java.math.BigDecimal;

public class ReceivingInfoHelper extends BasicHelper {
    private String receivingType;
    private String invoiceNo;
    private String inputInvoiceNo;
    private String control;
    private int partId;
    private String partNo;
    private String partDesc;
    private BigDecimal poQty;
    private BigDecimal deliverableQty;
    private BigDecimal predeliverQty;
    private BigDecimal receivedQty;
    private BigDecimal unreceivedQty;
    private BigDecimal tempQty;
    private String reason;
    private String po;
    private String locator;
    private BigDecimal inputQty;
    private String serialNo;
    private String reelID;
    private BigDecimal snCheckQty;
    private BigDecimal singleBoxQty;
    private int poHeaderId;
    private int poLineId;
    private int lineLocationId;
    private int shipmentNo;
    private int poNo;
    private int vendorId;
    private String vendorNo;
    private String vendorName;
    private String vendorSiteCode;
    private int vendorSiteId;
    private String userName;
    private int lineNo;
    private String dateCode;
    private boolean isOutSourcing;
    private String wareHouseNo;
    private String supReelID;
    private boolean updateReelID;
    private BigDecimal xbQty;
    private String itemLoc;
    private String itemLocDesc;
    private String carton;
    private Integer boxWeight;

    public int getShipmentNo() {
        return shipmentNo;
    }

    public void setShipmentNo(int shipmentNo) {
        this.shipmentNo = shipmentNo;
    }

    public int getPoHeaderId() {
        return poHeaderId;
    }

    public void setPoHeaderId(int poHeaderId) {
        this.poHeaderId = poHeaderId;
    }

    public int getPoLineId() {
        return poLineId;
    }

    public void setPoLineId(int poLineId) {
        this.poLineId = poLineId;
    }

    public int getLineLocationId() {
        return lineLocationId;
    }

    public void setLineLocationId(int lineLocationId) {
        this.lineLocationId = lineLocationId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getReceivingType() {
        return receivingType;
    }

    public void setReceivingType(String receivingType) {
        this.receivingType = receivingType;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public BigDecimal getPoQty() {
        return poQty;
    }

    public void setPoQty(BigDecimal poQty) {
        this.poQty = poQty;
    }

    public BigDecimal getDeliverableQty() {
        return deliverableQty;
    }

    public void setDeliverableQty(BigDecimal deliverableQty) {
        this.deliverableQty = deliverableQty;
    }

    public BigDecimal getPredeliverQty() {
        return predeliverQty;
    }

    public void setPredeliverQty(BigDecimal predeliverQty) {
        this.predeliverQty = predeliverQty;
    }

    public BigDecimal getReceivedQty() {
        return receivedQty;
    }

    public void setReceivedQty(BigDecimal receivedQty) {
        this.receivedQty = receivedQty;
    }

    public BigDecimal getUnreceivedQty() {
        return unreceivedQty;
    }

    public void setUnreceivedQty(BigDecimal unreceivedQty) {
        this.unreceivedQty = unreceivedQty;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getLocator() {
        return locator;
    }

    public void setLocator(String locator) {
        this.locator = locator;
    }

    public BigDecimal getInputQty() {
        return inputQty;
    }

    public void setInputQty(BigDecimal inputQty) {
        this.inputQty = inputQty;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getReelID() {
        return reelID;
    }

    public void setReelID(String reelID) {
        this.reelID = reelID;
    }

    public BigDecimal getSnCheckQty() {
        return snCheckQty;
    }

    public void setSnCheckQty(BigDecimal snCheckQty) {
        this.snCheckQty = snCheckQty;
    }

    public BigDecimal getSingleBoxQty() {
        return singleBoxQty;
    }

    public void setSingleBoxQty(BigDecimal singleBoxQty) {
        this.singleBoxQty = singleBoxQty;
    }

    public String getInputInvoiceNo() {
        return inputInvoiceNo;
    }

    public void setInputInvoiceNo(String inputInvoiceNo) {
        this.inputInvoiceNo = inputInvoiceNo;
    }

    public BigDecimal getTempQty() {
        return tempQty;
    }

    public void setTempQty(BigDecimal tempQty) {
        this.tempQty = tempQty;
    }

    public int getPartId() {
        return partId;
    }

    public void setPartId(int partId) {
        this.partId = partId;
    }

    public String getPo() {
        return po;
    }

    public void setPo(String po) {
        this.po = po;
    }

    public int getPoNo() {
        return poNo;
    }

    public void setPoNo(int poNo) {
        this.poNo = poNo;
    }

    public int getVendorId() {
        return vendorId;
    }

    public void setVendorId(int vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorSiteCode() {
        return vendorSiteCode;
    }

    public void setVendorSiteCode(String vendorSiteCode) {
        this.vendorSiteCode = vendorSiteCode;
    }

    public int getVendorSiteId() {
        return vendorSiteId;
    }

    public void setVendorSiteId(int vendorSiteId) {
        this.vendorSiteId = vendorSiteId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getLineNo() {
        return lineNo;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    public String getDateCode() {
        return dateCode;
    }

    public void setDateCode(String dateCode) {
        this.dateCode = dateCode;
    }

    public boolean isOutSourcing() {
        return isOutSourcing;
    }

    public void setOutSourcing(boolean isOutSourcing) {
        this.isOutSourcing = isOutSourcing;
    }

    public String getWareHouseNo() {
        return wareHouseNo;
    }

    public void setWareHouseNo(String wareHouseNo) {
        this.wareHouseNo = wareHouseNo;
    }

    public String getSupReelID() {
        return supReelID;
    }

    public void setSupReelID(String supReelID) {
        this.supReelID = supReelID;
    }

    public boolean isUpdateReelID() {
        return updateReelID;
    }

    public void setUpdateReelID(boolean updateReelID) {
        this.updateReelID = updateReelID;
    }

    public BigDecimal getXbQty() {
        return xbQty;
    }

    public void setXbQty(BigDecimal xbQty) {
        this.xbQty = xbQty;
    }

    public String getVendorNo() {
        return vendorNo;
    }

    public void setVendorNo(String vendorNo) {
        this.vendorNo = vendorNo;
    }

    public String getPartDesc() {
        return partDesc;
    }

    public void setPartDesc(String partDesc) {
        this.partDesc = partDesc;
    }

    public String getItemLoc() {
        return itemLoc;
    }

    public void setItemLoc(String itemLoc) {
        this.itemLoc = itemLoc;
    }

    public String getItemLocDesc() {
        return itemLocDesc;
    }

    public void setItemLocDesc(String itemLocDesc) {
        this.itemLocDesc = itemLocDesc;
    }

    public void setCarton(String carton) {
        this.carton = carton;
    }

    public String getCarton() {
        return carton;
    }

    public Integer getBoxWeight() {
        return boxWeight;
    }

    public void setBoxWeight(Integer boxWeight) {
        this.boxWeight = boxWeight;
    }

    @Override
    public String toString() {
        return "ReceivingInfoHelper{" +
                "receivingType='" + receivingType + '\'' +
                ", invoiceNo='" + invoiceNo + '\'' +
                ", inputInvoiceNo='" + inputInvoiceNo + '\'' +
                ", control='" + control + '\'' +
                ", partId=" + partId +
                ", partNo='" + partNo + '\'' +
                ", partDesc='" + partDesc + '\'' +
                ", poQty=" + poQty +
                ", deliverableQty=" + deliverableQty +
                ", predeliverQty=" + predeliverQty +
                ", receivedQty=" + receivedQty +
                ", unreceivedQty=" + unreceivedQty +
                ", tempQty=" + tempQty +
                ", reason='" + reason + '\'' +
                ", po='" + po + '\'' +
                ", locator='" + locator + '\'' +
                ", inputQty=" + inputQty +
                ", serialNo='" + serialNo + '\'' +
                ", reelID='" + reelID + '\'' +
                ", snCheckQty=" + snCheckQty +
                ", singleBoxQty=" + singleBoxQty +
                ", poHeaderId=" + poHeaderId +
                ", poLineId=" + poLineId +
                ", lineLocationId=" + lineLocationId +
                ", shipmentNo=" + shipmentNo +
                ", poNo=" + poNo +
                ", vendorId=" + vendorId +
                ", vendorNo='" + vendorNo + '\'' +
                ", vendorName='" + vendorName + '\'' +
                ", vendorSiteCode='" + vendorSiteCode + '\'' +
                ", vendorSiteId=" + vendorSiteId +
                ", userName='" + userName + '\'' +
                ", lineNo=" + lineNo +
                ", dateCode='" + dateCode + '\'' +
                ", isOutSourcing=" + isOutSourcing +
                ", wareHouseNo='" + wareHouseNo + '\'' +
                ", supReelID='" + supReelID + '\'' +
                ", updateReelID=" + updateReelID +
                ", xbQty=" + xbQty +
                ", itemLoc='" + itemLoc + '\'' +
                ", itemLocDesc='" + itemLocDesc + '\'' +
                '}';
    }
}
