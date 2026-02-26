package com.senao.warehouse.util;

import android.widget.Toast;

import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.CONTAINER_LABEL_QR_CODE_FORMAT;
import com.senao.warehouse.database.ITEM_LABEL_QR_CODE_FORMAT;
import com.senao.warehouse.database.PALLET_LABEL_QR_CODE_FORMAT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QrCodeUtil {

    public static String getValueFromItemLabelQrCode(String text, ITEM_LABEL_QR_CODE_FORMAT type) {

        if(Constant.ISQRORG){
            String[] values = text.trim().split("@");
            if (values.length <= ITEM_LABEL_QR_CODE_FORMAT.values().length) {
                for (int i = 0; i < values.length; i++) {
                    if (i == type.ordinal()) {
                        return values[i].trim();
                    }
                }
            }
            return "";
        }else{
            String[] values = text.trim().split("@");
            //if (values.length == ITEM_LABEL_QR_CODE_FORMAT.values().length) {
            if (values.length <= ITEM_LABEL_QR_CODE_FORMAT.values().length) { //20260105 Ann Edit:應該要相等，但部分供應商沒有置換標籤增加COO
                for (int i = 0; i < values.length; i++) {
                    if (i == type.ordinal()) {
                        return values[i].trim();
                    }
                }
            }
            return "";
        }


    }

    public static String getValueFromPalletLabelQrCode(String text, PALLET_LABEL_QR_CODE_FORMAT type) {

        if(Constant.ISQRORG){
            String[] values = text.trim().split("@");
            if (values.length <= PALLET_LABEL_QR_CODE_FORMAT.values().length) {
                for (int i = 0; i < values.length; i++) {
                    if (i == type.ordinal()) {
                        return values[i].trim();
                    }
                }
            }
            return "";
        }else{
            String[] values = text.trim().split("@");
            if (values.length == PALLET_LABEL_QR_CODE_FORMAT.values().length) {
                for (int i = 0; i < values.length; i++) {
                    if (i == type.ordinal()) {
                        return values[i].trim();
                    }
                }
            }
            return "";
        }
    }

    public static String getValueFromContainerLabelQrCode(String text, CONTAINER_LABEL_QR_CODE_FORMAT type) {
        String[] values = text.trim().split("@");
        if (values.length == CONTAINER_LABEL_QR_CODE_FORMAT.values().length) {
            for (int i = 0; i < values.length; i++) {
                if (i == type.ordinal()) {
                    return values[i].trim();
                }
            }
        }
        return "";
    }



    public static String getSnList(String qrCode) {
        AppController.debug("getSnList" +qrCode);
        String[] snList;
        if (qrCode.indexOf("\r\n") > 0) {
            snList = qrCode.split("\r\n");
        } else if (qrCode.indexOf("\r") > 0) {
            snList = qrCode.split("\r");
        } else if (qrCode.indexOf("\t") > 0) {
            snList = qrCode.split("\t");
        } else if (qrCode.indexOf("\n") > 0) {
            snList = qrCode.split("\n");
        } else if (qrCode.indexOf("[CR][LF]") > 0) {
            snList = qrCode.split("[CR][LF]");
        } else if (qrCode.indexOf("[CR]") > 0) {
            snList = qrCode.split("\\[CR]");
        } else if (qrCode.indexOf(";") > 0) {
            snList = qrCode.split(";");
        } else if (qrCode.indexOf(":") > 0) {
            snList = qrCode.split(":");
        } else if (qrCode.indexOf(",") > 0) {
            snList = qrCode.split(",");
        } else if (qrCode.indexOf(" ") > 0) {
            snList = qrCode.split(" ");
        } else {
            snList = new String[1];
            snList[0] = qrCode;
        }

        return snList[0];


    }

}
