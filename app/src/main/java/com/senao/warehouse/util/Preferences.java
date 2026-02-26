package com.senao.warehouse.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Tooth Monster
 */
public final class Preferences {
    /**
     *
     */
    public final static String PREFERENCE_NAME = "warehouseapp";
    public final static String SERVER_IP_API = "server_ip_api";
    public final static String SERVER_IP_FORMAL = "server_ip_formal";
    public final static String SERVER_PORT_FORMAL = "server_port_formal";
    public final static String SERVER_IP_TEST = "server_ip_test";
    public final static String SERVER_PORT_TEST = "server_port_test";
    public final static String SERVER_IP_OTHER = "server_ip_other";
    public final static String SERVER_PORT_OTHER = "server_port_other";
    public final static String SERVER_TYPE = "server_type";
    public final static String PRINTER_NAME = "printer";
    public final static String ROTATE_ENABLED = "enable_rotate";
    public final static String EXTREME_PRINTER_IP = "extreme_printer_ip";
    public final static String EXTREME_PRINTER_PORT = "extreme_printer_port";
    public final static String EXTREME_PRINTER_QTY = "extreme_printer_qty";
    public final static String MATERIAL_PRINTER_IP = "material_printer_ip";
    public final static String MATERIAL_PRINTER_PORT = "material_printer_port";
    public final static String MATERIAL_PRINTER_QTY = "material_printer_qty";
    public final static String CUSTOMER_MATERIAL_PRINTER_IP = "customer_material_printer_ip";
    public final static String CUSTOMER_MATERIAL_PRINTER_PORT = "customer_material_printer_port";
    public final static String CUSTOMER_MATERIAL_PRINTER_QTY = "customer_material_printer_qty";
    public final static String COMPANY = "company";
    public final static String FACTORY = "factory";
    public final static String ORG = "org";
    public final static String ORGNAME = "orgname";
    public final static String OU = "ou";

    public final static String SOPHOS_PRINTER_IP = "sophos_printer_ip";
    public final static String SOPHOS_PRINTER_PORT = "sophos_printer_port";
    public final static String SOPHOS_PRINTER_QTY = "sophos_printer_qty";

    public final static String MERAKI_PRINTER_IP = "meraki_printer_ip";
    public final static String MERAKI_PRINTER_PORT = "meraki_printer_port";
    public final static String MERAKI_PRINTER_QTY = "meraki_printer_qty";

    public final static String MATERIAL_REPRINT_PRINTER_IP = "material_reprint_printer_ip";
    public final static String MATERIAL_REPRINT_PRINTER_PORT = "material_reprint_printer_port";
    public final static String MATERIAL_REPRINT_PRINTER_QTY = "material_reprint_printer_qty";

    public final static String WO_LABEL_PRINTER_IP = "wo_label_printer_ip";
    public final static String WO_LABEL_PRINTER_PORT = "wo_label_printer_port";
    public final static String WO_LABEL_PRINTER_QTY = "wo_label_printer_qty";

    public final static String INVOICE_SEQ_PRINTER_IP = "invoice_seq_printer_ip";
    public final static String INVOICE_SEQ_PRINTER_PORT = "invoice_seq_printer_port";
    public final static String INVOICE_SEQ_PRINTER_QTY = "invoice_seq_printer_qty";

    public final static String INVOICE_NO = "invoice_no";

    public final static String SMT_PRINTER_IP = "smt_printer_ip";
    public final static String SMT_PRINTER_PORT = "smt_printer_port";
    public final static String SMT_PRINTER_QTY = "smt_printer_qty";

    public final static String SPLIT_MATERIAL_PRINTER_IP = "split_material_printer_ip";
    public final static String SPLIT_MATERIAL_PRINTER_PORT = "split_printer_port";
    public final static String SPLIT_MATERIAL_PRINTER_QTY = "split_printer_qty";

    public final static String USER = "user";
    public final static String PWD = "password";
    public final static String PALLET_YN = "pallet_yn";

    /*
     *
     */
    private Preferences() {
    }

    /**
     * @param context
     * @return
     */
    public static SharedPreferences getSharedPreferences(Context context) {
        SharedPreferences s = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        return s;
    }

    /**
     * @param context
     * @param preferenceKey
     * @return
     */
    public static boolean getBoolean(Context context, String preferenceKey) {
        boolean b = false;
        SharedPreferences preferences = getSharedPreferences(context);
        b = preferences.getBoolean(preferenceKey, false);
        return b;
    }

    public static String getString(Context context, String preferenceKey) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getString(preferenceKey, null);
    }

    /**
     * @param context
     * @param preferenceKey
     * @param value
     */
    public static void setBoolean(Context context, String preferenceKey,
                                  boolean value) {
        SharedPreferences preferences = getSharedPreferences(context);
        preferences.edit().putBoolean(preferenceKey, value).apply();
    }

    /**
     * @param context
     * @param preferenceKey
     * @param value
     */
    public static void setString(Context context, String preferenceKey,
                                 String value) {
        SharedPreferences preferences = getSharedPreferences(context);
        preferences.edit().putString(preferenceKey, value).apply();
    }

    /*
     * @param context
     *
     * @param preferenceKey
     *
     * @param value
     */
    public static void setInt(Context context, String preferenceKey, int values) {
        SharedPreferences preferences = getSharedPreferences(context);
        preferences.edit().putInt(preferenceKey, values).apply();
    }

    /**
     * @param context
     * @param preferenceKey
     * @return
     */
    public static int getInt(Context context, String preferenceKey) {
        int b = 0;
        SharedPreferences preferences = getSharedPreferences(context);
        b = preferences.getInt(preferenceKey, 0);
        return b;
    }

}