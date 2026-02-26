package com.senao.warehouse.database;

public class ConnectionDetails {
    private String name;
    private String ip;
    private String port;
    private String sfcIp;
    private String api_1;
    private String api_2;
    private String api_3;
    private String erpIp;

    public ConnectionDetails(String name, String ip, String port, String sfcIp, String api_1, String api_2, String api_3, String erpIp) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.sfcIp = sfcIp;
        this.api_1 = api_1;
        this.api_2 = api_2;
        this.api_3 = api_3;
        this.erpIp = erpIp;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    public String getSfcIp() {
        return sfcIp;
    }

    public String getApi_1() {
        return api_1;
    }

    public String getApi_2() {
        return api_2;
    }

    public String getApi_3() {
        return api_3;
    }

    public String getErpIp() {
        return erpIp;
    }

    @Override
    public String toString() {
        return "ConnectionDetails{" +
                "name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                ", sfcIp='" + sfcIp + '\'' +
                ", api_1='" + api_1 + '\'' +
                ", api_2='" + api_2 + '\'' +
                ", api_3='" + api_3 + '\'' +
                ", erpIp='" + erpIp + '\'' +
                '}';
    }
}
