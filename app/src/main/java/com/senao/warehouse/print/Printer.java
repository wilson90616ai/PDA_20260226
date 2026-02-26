package com.senao.warehouse.print;


public class Printer {
    private String printname;
    private String IP;
    private String count;

    //Constructor
    public Printer(String printname, String IP, String count) {
        this.printname = printname;
        this.IP = IP;
        this.count = count;
    }

    //Getters and setters
    public String getPrintname() {
        return printname;
    }

    public void setPrintname(String printname) {
        this.printname = printname;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "Printer{" +
                "printname='" + printname + '\'' +
                ", IP='" + IP + '\'' +
                ", count='" + count + '\'' +
                '}';
    }
}
