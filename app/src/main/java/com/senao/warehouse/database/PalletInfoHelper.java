package com.senao.warehouse.database;

public class PalletInfoHelper extends BasicHelper {
    private int itemID;
    private int airQty;
    private int seaQty;
    private String airLWH;
    private String seaLWH;

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public int getAirQty() {
        return airQty;
    }

    public void setAirQty(int airQty) {
        this.airQty = airQty;
    }

    public int getSeaQty() {
        return seaQty;
    }

    public void setSeaQty(int seaQty) {
        this.seaQty = seaQty;
    }

    public String getAirLWH() {
        return airLWH;
    }

    public void setAirLWH(String airLWH) {
        this.airLWH = airLWH;
    }

    public String getSeaLWH() {
        return seaLWH;
    }

    public void setSeaLWH(String seaLWH) {
        this.seaLWH = seaLWH;
    }
}
