package com.senao.warehouse.util;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.senao.warehouse.AppController;
import com.senao.warehouse.asynctask.CallWebApi;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.ConditionHelper;
import com.senao.warehouse.handler.WebApiHandler;
import com.senao.warehouse.http.HttpClient;
import com.senao.warehouse.http.ServerResponse;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Util {

    public static final String TAG = Util.class.getSimpleName();

    public static String paddingFront(String str, int length, String charX) {

        int padLength = length - str.length();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < padLength; i++) {
            sb.append(charX);
        }
        return sb.append(str).toString();
    }

    public static boolean testConnection(String host, int port) {
        try {

            HttpClient httpClient = new HttpClient(host, port);

            ServerResponse response = httpClient.doPost("", AppController
                    .getProperties("Connection_Test"), Integer.parseInt(AppController.getProperties("Connection_Test_Timeout")));

            AppController.debug("Server response:" + response.getCode());
            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                AppController.debug("Server connected");
                return true;
            } else {
                AppController.debug("Server connect failed");
                return false;
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            return false;
        }
    }

    public static String formatDateTime(String datetimeString) {
        // 定义日期时间字符串的格式
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);

        try {
            // 解析日期时间字符串为 Date 对象
            Date date = inputFormat.parse(datetimeString);

            // 定义要输出的日期时间格式
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

            // 设置时区
            outputFormat.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));

            // 格式化日期时间并返回
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String paddingRear(String str, int length) {

        int padLength = length - str.length();
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        for (int i = 0; i < padLength; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String getStringResourceByName(String aString, Context context) {
        String packageName = context.getPackageName();
        int resId = context.getResources().getIdentifier(aString, "string",
                packageName);
        if (resId == 0) {
            return aString;
        } else {
            return context.getString(resId);
        }
    }

    public static boolean parseDate(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN);
            Date parsedDate = dateFormat.parse(date);
            Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
            return true;
        } catch (Exception e) { //this generic but you can control another types of exception
            return false;
        }
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",
                Locale.TAIWAN);
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public static String getCurrentDate() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd",
                Locale.TAIWAN);
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public static String getTransferNo() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/MM/dd",
                Locale.TAIWAN);
        Date now = new Date();
        return "S-S " + sdfDate.format(now);
    }

    public static String fmt(double d) {
        if (d == (long) d)
            return String.format("%d", (long) d);
        else
            return String.format("%s", d);
        //return String.valueOf(d);
    }

    public static String fmt(BigDecimal bigDecimal) {
        if (bigDecimal == null)
            return "0";
        if (bigDecimal.doubleValue() == (long) bigDecimal.doubleValue())
            return String.format("%d", (long) bigDecimal.doubleValue());
        else
            return String.format("%s", bigDecimal.doubleValue());
    }

    public static double subtract(double minuend, double subtrahend) {
        return BigDecimal.valueOf(minuend).subtract(BigDecimal.valueOf(subtrahend)).doubleValue();
    }

    public static double add(double augend, double addend) {
        return BigDecimal.valueOf(augend).add(BigDecimal.valueOf(addend)).doubleValue();
    }

    public static double multiply(double mulitplicand, double multiplier) {
        return BigDecimal.valueOf(mulitplicand).multiply(BigDecimal.valueOf(multiplier)).doubleValue();
    }

    public static double divide(double dividend, double divisor) {
        return BigDecimal.valueOf(dividend).divide(BigDecimal.valueOf(divisor)).doubleValue();
    }

    public static String utf8ToBig5(String unicode) {
        System.out.println("UTF-16: " + unicode);
        // unicode 轉成 Big5 編碼
        byte[] big5 = new byte[0];
        try {
            big5 = unicode.getBytes("Big5");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // Big5 編碼 轉回 unicode
        try {
            unicode = new String(big5, "Big5");
            System.out.println("Big5: " + unicode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return unicode;
    }


    public static double getDoubleValue(BigDecimal value) {
        return value == null ? 0 : value.doubleValue();
    }


    public static String getSenaoPartNo(String value) {
        if (value != null) {
            int index = value.indexOf("(");
            if (index >= 0) {
                value = value.substring(0, index);
            }
        }
        return value;
    }

    public static boolean isVendorCodeValid(Activity activity, String vendorCode) {
        try {
            ConditionHelper conditionHelper = new ConditionHelper();
            conditionHelper.setVendorCode(vendorCode);
            BasicHelper result = new CallWebApi(activity, AppController.getProperties("CheckVendorCode"), null).execute(conditionHelper).get();
            if (result != null && result.getIntRetCode() == ReturnCode.OK) {
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    public static boolean isPartNoValid(Activity activity, String partNo) {
        try {
            ConditionHelper conditionHelper = new ConditionHelper();
            conditionHelper.setPartNo(partNo);
            BasicHelper result = new CallWebApi(activity, AppController.getProperties("CheckPartNo"), null).execute(conditionHelper).get();
            if (result != null && result.getIntRetCode() == ReturnCode.OK) {
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    public static boolean isManufacturerValid(String manufacturer) {
        final ConditionHelper conditionHelper = new ConditionHelper();
        conditionHelper.setPartNo(manufacturer);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                WebApiHandler handler = new WebApiHandler();
                handler.callApi(AppController.getProperties("CheckPartNo"), conditionHelper);
            }
        });

        t.start(); // spawn thread

        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }

        return false;
    }

    public static boolean isDateCodeValid(String dateCode) {
        if (!TextUtils.isEmpty(dateCode)) {
            dateCode = dateCode.trim();
            if (dateCode.length() >= 4) {
                dateCode = dateCode.substring(0, 4);
                String year = dateCode.substring(0, 2);
                if (Integer.parseInt(year) > Integer.parseInt(getCurrentYear())) {
                    return false;
                }
                //if (Integer.parseInt(dateCode) <= Integer.parseInt(getCurrentDateCode())) {
                // 一年最多53週
                return Integer.parseInt(dateCode.substring(2, 4)) < 54;
                //}
            }
        }
        return false;
    }

    public static String getCurrentYear() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yy",
                Locale.TAIWAN);
        Date now = new Date();
        return sdfDate.format(now);
    }

    public static String getCurrentDateCode() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyww",
                Locale.TAIWAN);
        Date now = new Date();
        return sdfDate.format(now);
    }

    public boolean isValidLocator(String value) {
        if (TextUtils.isEmpty(value)) {
            return false;
        } else {
            value = value.trim();
            if (TextUtils.isEmpty(value)) {
                return false;
            } else {
                return !(value.equals(Constant.LOCATOR_P022)
                        || value.equals(Constant.LOCATOR_P023)
                        || value.equals(Constant.LOCATOR_PFA18));
            }
        }
    }
}

