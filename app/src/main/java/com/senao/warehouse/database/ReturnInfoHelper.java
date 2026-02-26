package com.senao.warehouse.database;

import java.math.BigDecimal;

public class ReturnInfoHelper extends BasicHelper {
    private String deliveryId;
    private String sn;
    private String cartonNo;
    private String palletNo;
    private String itemNo;
    private String itemDesc;
    private BigDecimal reqQty;
    private BigDecimal packQty;
    private BigDecimal unPackQty;
    private BigDecimal delQty;
    private String type;

    public String getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(String deliveryId) {
        this.deliveryId = deliveryId;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getCartonNo() {
        return cartonNo;
    }

    public void setCartonNo(String cartonNo) {
        this.cartonNo = cartonNo;
    }

    public String getPalletNo() {
        return palletNo;
    }

    public void setPalletNo(String palletNo) {
        this.palletNo = palletNo;
    }

    public String getItemNo() {
        return itemNo;
    }

    public void setItemNo(String itemNo) {
        this.itemNo = itemNo;
    }

    public BigDecimal getReqQty() {
        return reqQty;
    }

    public void setReqQty(BigDecimal reqQty) {
        this.reqQty = reqQty;
    }

    public BigDecimal getPackQty() {
        return packQty;
    }

    public void setPackQty(BigDecimal packQty) {
        this.packQty = packQty;
    }

    public BigDecimal getUnPackQty() {
        return unPackQty;
    }

    public void setUnPackQty(BigDecimal unPackQty) {
        this.unPackQty = unPackQty;
    }

    public BigDecimal getDelQty() {
        return delQty;
    }

    public void setDelQty(BigDecimal delQty) {
        this.delQty = delQty;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }
}
