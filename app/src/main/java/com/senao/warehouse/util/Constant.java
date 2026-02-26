package com.senao.warehouse.util;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class Constant {

    public static final String SUB_307 = "307";
    public static final String LOCATOR_P023 = "P023";
    public static final String LOCATOR_P022 = "P022";
    public static final String LOCATOR_PFA18 = "PFA18";
    public static List<String> GET_NOT_ALLOWED_SUBS = new ArrayList<>();
    public static int lan=0;


    public static final Boolean ISORG = true;//1.判斷是不是使用ORG版本 之後要拿掉 把原版的刪掉2.可以先上正式區的  //TODO
    public static final Boolean ISORG2 = true;//1.不可以先上正式區的 要等測試正式區有資料
    public static final Boolean ISQRORG = true;//1.QRcode標籤切換


    public static final Boolean ISOUORG = true;//建立公司別與工廠別

//    if(Constant.ISORG){
//        checkorg.setVisibility(View.VISIBLE);
//    }else{
//        checkorg.setVisibility(View.GONE);
//    }

//    edittext_import_subinventory


}

//查無此倉別
//    Sending URI http://10.0.204.170:18080/WarehouseWeb/stock/chksubinventory.do
//        2022-09-19 16:39:03.980 21434-21497/com.senao.warehouse D/com.senao.warehouse: Service status =200
//        2022-09-19 16:39:03.980 21434-21497/com.senao.warehouse D/com.senao.warehouse: Response entity ={"Code":"OK","Message":"CHECK SUBINVENTORY","Data":"{\"partNo\":\"0912A0224301\",\"subinventory\":\"I311\",\"locator\":\"I31105\",\"strErrorBuf\":\"無此倉別，請確認?\",\"intRetCode\":2,\"org\":86,\"ou\":82}"}