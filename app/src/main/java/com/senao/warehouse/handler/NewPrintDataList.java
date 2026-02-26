package com.senao.warehouse.handler;

import com.senao.warehouse.database.NewPrintData;

import java.util.ArrayList;

public class NewPrintDataList {
    private ArrayList<NewPrintData> label;
    private String count;

    public void setLabel(ArrayList<NewPrintData> label){this.label=label;}

    public ArrayList<NewPrintData> getLabel(){return label;}

    public void setCount(String count){this.count=count;}

    public String getCount(){return count;}
}
