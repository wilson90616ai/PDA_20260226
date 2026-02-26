package com.senao.warehouse.database;

import java.util.HashMap;

public class NewPrintData  {
    private String LabelName;
    private String PrintName;
    private HashMap<String, String> variables;
    private String printname;
    private String IP;
    private String count;

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

    public void setVariables(HashMap<String, String> variables) {
        this.variables = variables;
    }

    public HashMap<String, String> getVariables() {
        return variables;
    }

    public void setLabelName(String LabelName){this.LabelName=LabelName;}

    public String getLabelName(){return LabelName;}

    public void setPrintName(String PrintName){this.PrintName=PrintName;}

    public String getPrintName(){return PrintName;}
}
