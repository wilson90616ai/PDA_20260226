package com.senao.warehouse.print;

/**
 * Created by 102069 on 2016/5/13.
 * for 70mm*50mm 標籤
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.example.tscdll.TSCActivity;
import com.senao.warehouse.AppController;
import com.senao.warehouse.database.BakeHelper;
import com.senao.warehouse.database.MerakiPoInfoHelper;
import com.senao.warehouse.util.CharUtil;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.Preferences;

import java.util.Set;

public class BtPrintLabel {
    static String printerName = null;
    static boolean enableRotatePrint = false;
    static boolean isConnected = false;
    private static BluetoothDevice btPrinter = null;

    private final static BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (device != null && btPrinter != null && device.getAddress().equals(btPrinter.getAddress())) {
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    //Toast.makeText(context, "BluetoothDevice.ACTION_FOUND", Toast.LENGTH_SHORT).show();
                } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                    //Toast.makeText(context, "BluetoothDevice.ACTION_ACL_CONNECTED", Toast.LENGTH_SHORT).show();
                    isConnected = true;
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    //Toast.makeText(context, "BluetoothDevice.ACTION_DISCOVERY_FINISHED", Toast.LENGTH_SHORT).show();
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                    //Toast.makeText(context, "BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED", Toast.LENGTH_SHORT).show();
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    //Toast.makeText(context, "與藍芽印表機斷線", Toast.LENGTH_SHORT).show();
                    isConnected = false;
                }
            } else {
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    //Toast.makeText(context, "與藍芽印表機斷線", Toast.LENGTH_SHORT).show();

                    if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                        isConnected = false;
                    }
                }
            }
        }
    };

    private static BtPrintLabel pl;
    private static TSCActivity tscDll = null;

    public BtPrintLabel() {
        if (tscDll == null)
            tscDll = new TSCActivity();

        Log.d("BTPrnter", "construct PrintLabel()");
    }

    private BtPrintLabel(Context context) {
        Log.d("BTPrnter", "construct PrintLabel()");
    }

    private BtPrintLabel(String printerName) {
        Log.d("BTPrnter", "construct PrintLabel()");
    }

    public static boolean instance(Context context) {
        printerName = Preferences.getSharedPreferences(context).getString(Preferences.PRINTER_NAME, null);
        Log.d("BTPrnter", "Get Prtiner Name:" + printerName);

        enableRotatePrint = Preferences.getSharedPreferences(context).getBoolean(Preferences.ROTATE_ENABLED, false);
        Log.d("BTPrnter", "Get ROTATE_ENABLED:" + enableRotatePrint);

        if (TextUtils.isEmpty(printerName)) {
            return false;
        } else {
            if (pl == null) {
                pl = new BtPrintLabel();
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
                filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
                filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

                context.registerReceiver(mReceiver, filter);
            }

            boolean result = pl.discover(printerName);
            if (!result) {
                isConnected = false;
            }

            return result;
        }
    }

    public static boolean isPrintNameSet(Context context) {
        printerName = Preferences.getSharedPreferences(context).getString(Preferences.PRINTER_NAME, null);
        Log.d("BTPrnter", "Get Prtiner Name:" + printerName);

        if (TextUtils.isEmpty(printerName)) {
            isConnected = false;
            return false;
        }

        return true;
    }

    public static void setPrinterInfo(String printerNameNew, boolean enableRotatePrintNew) {
        if (printerName != null && !printerName.equals(printerNameNew)) {
            disconnect();
        }

        printerName = printerNameNew;
        enableRotatePrint = enableRotatePrintNew;
    }

    public static boolean isBtEnabled() {
        BluetoothAdapter mBtAdapter;
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            isConnected = false;
            return false;
        }

        boolean result = mBtAdapter.isEnabled();
        if (!result) {
            isConnected = false;
        }

        return result;
    }

    public static boolean printPo(String po) {
        try {
            //connect();
            sendSetup();
            int x, y;

            /*x = mm2dot(3);
            y = mm2dot(20);//20
            tscDll.printerfont(x, y, "3", 0, 2, 2, "PO:"+po);*/

            x = mm2dot(3);
            y = mm2dot(15);
            tscDll.printerfont(x, y, "3", 0, 2, 2, "PO:");
            y = mm2dot(25);
            tscDll.printerfont(x, y, "3", 0, 2, 2, po);

            tscDll.printlabel(1, 1);
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter", e.toString());
            return false;
        }
    }

    public static boolean printTESTPO() {
        try {
            //connect();
            sendSetup();

            /*tscDll.openport(btPrinter.getAddress());
            tscDll.setup(70, 50, 3, 7, 0, 3, 0);
            tscDll.sendcommand("DIRECTION 0\n");
            tscDll.clearbuffer();*/

            int x, y;

            /*x = mm2dot(3);
            y = mm2dot(20);//20
            tscDll.printerfont(x, y, "3", 0, 2, 2, "PO:"+po);*/

            x = mm2dot(3);
            y = mm2dot(3);
            tscDll.printerfont(x, y, "1", 0, 2, 2, "PO:");

            x = mm2dot(13);
            tscDll.barcode(x, y, "128", 30, 2, 0, 2, 5, "S0000-67821");

            //tscDll.printerfont(x, y, "2", 0, 2, 2, "DN123456");
            x = mm2dot(3);
            y = mm2dot(15);
            tscDll.printerfont(x, y, "1", 0, 2, 2, "COO:");

            x = mm2dot(13);
            tscDll.barcode(x, y, "128", 30, 2, 0, 2, 5, "TW");

            x = mm2dot(3);
            y = mm2dot(25);
            tscDll.printerfont(x, y, "1", 0, 2, 2, "QTY:");

            x = mm2dot(13);
            tscDll.barcode(x, y, "128", 30, 2, 0, 2, 5, "10");

            x = mm2dot(3);
            y = mm2dot(35);
            tscDll.printerfont(x, y, "1", 0, 2, 2, "G.W.:");

            x = mm2dot(16);
            tscDll.barcode(x, y, "128", 30, 2, 0, 2, 5, "12.16");

            /*x = mm2dot(3);
            y = mm2dot(15);
            tscDll.printerfont(x, y, "3", 0, 2, 2, "PO:");
            y = mm2dot(25);
            tscDll.printerfont(x, y, "3", 0, 2, 2, "S000067821");*/

            tscDll.printlabel(1, 1);
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter", e.toString());
            return false;
        }
    }

    public static boolean printMerakiPO(String PO,String COO,String QTY,String GW) {
        try {
            //connect();
            sendSetup();

            /*tscDll.openport(btPrinter.getAddress());
            tscDll.setup(70, 50, 3, 7, 0, 3, 0);
            tscDll.sendcommand("DIRECTION 0\n");
            tscDll.clearbuffer();*/

            int x, y;

            /*x = mm2dot(3);
            y = mm2dot(20);//20
            tscDll.printerfont(x, y, "3", 0, 2, 2, "PO:"+po);*/

            x = mm2dot(3);
            y = mm2dot(3);
            tscDll.printerfont(x, y, "1", 0, 2, 2, "PO:");

            x = mm2dot(13);
            tscDll.barcode(x, y, "128", 30, 2, 0, 2, 5, PO);

            //tscDll.printerfont(x, y, "2", 0, 2, 2, "DN123456");
            x = mm2dot(3);
            y = mm2dot(15);
            tscDll.printerfont(x, y, "1", 0, 2, 2, "COO:");

            x = mm2dot(13);
            tscDll.barcode(x, y, "128", 30, 2, 0, 2, 5, COO);

            x = mm2dot(3);
            y = mm2dot(25);
            tscDll.printerfont(x, y, "1", 0, 2, 2, "QTY:");

            x = mm2dot(13);
            tscDll.barcode(x, y, "128", 30, 2, 0, 2, 5, QTY);

            x = mm2dot(3);
            y = mm2dot(35);
            tscDll.printerfont(x, y, "1", 0, 2, 2, "G.W.:");

            x = mm2dot(16);
            tscDll.barcode(x, y, "128", 30, 2, 0, 2, 5, GW);

            /*x = mm2dot(3);
            y = mm2dot(39);*/
            y = (int)39.5*8;
            /*tscDll.printerfont(x, y, "1", 0, 1, 1, "KG:");
            tscDll.sendcommand("TEXT " + x + "," + y + ",\"1\",0,1,1,\"" + "KG:" + "\"\n");*/

            //y = mm2dot(45);
            tscDll.sendcommand("TEXT " + x + "," + y + ",\"2\",0,1,1,\"" + "KG:" + "\"\n");
            //tscDll.sendcommand("TEXT " + x + "," + y + ",\"2\",0,0.8,0.8,\"" + "KG:" + "\"\n");

            /*x = mm2dot(3);
            y = mm2dot(15);
            tscDll.printerfont(x, y, "3", 0, 2, 2, "PO:");
            y = mm2dot(25);
            tscDll.printerfont(x, y, "3", 0, 2, 2, "S000067821");*/

            tscDll.printlabel(1, 1);
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter", e.toString());
            return false;
        }
    }

    //public static boolean printMerakiPO(MerakiPoInfoHelper result) {
    //try {
    ////connect();
    //sendSetup();

            /*tscDll.openport(btPrinter.getAddress());
            tscDll.setup(70, 50, 3, 7, 0, 3, 0);
            tscDll.sendcommand("DIRECTION 0\n");
            tscDll.clearbuffer();*/

    //int x, y;

            /*x = mm2dot(3);
            y = mm2dot(20);//20
            tscDll.printerfont(x, y, "3", 0, 2, 2, "PO:"+po);*/

    //x = mm2dot(3);
    //y = mm2dot(3);
    //tscDll.printerfont(x, y, "1", 0, 2, 2, "PO:");

    //x = mm2dot(13);
    //tscDll.barcode(x, y, "128", 30, 2, 0, 2, 5, PO);

    ////tscDll.printerfont(x, y, "2", 0, 2, 2, "DN123456");
    //x = mm2dot(3);
    //y = mm2dot(15);
    //tscDll.printerfont(x, y, "1", 0, 2, 2, "COO:");

    //x = mm2dot(13);
    //tscDll.barcode(x, y, "128", 30, 2, 0, 2, 5, "TW");

    //x = mm2dot(3);
    //y = mm2dot(25);
    //tscDll.printerfont(x, y, "1", 0, 2, 2, "QTY:");

    //x = mm2dot(13);
    //tscDll.barcode(x, y, "128", 30, 2, 0, 2, 5, result.get);

    //x = mm2dot(3);
    //y = mm2dot(35);
    //tscDll.printerfont(x, y, "1", 0, 2, 2, "G.W.:");

    //x = mm2dot(16);
    //tscDll.barcode(x, y, "128", 30, 2, 0, 2, 5, GW);

            /*x = mm2dot(3);
            y = mm2dot(15);
            tscDll.printerfont(x, y, "3", 0, 2, 2, "PO:");
            y = mm2dot(25);
            tscDll.printerfont(x, y, "3", 0, 2, 2, "S000067821");*/

    //tscDll.printlabel(1, 1);
    //return true;
    //} catch (Exception e) {
    //Log.d("BTPrnter", e.toString());
    //return false;
    //}
    //}

    public static boolean printPoAndOe(String po, String oe) {
        try {
            //connect();
            sendSetup();

            int x, y;

            x = mm2dot(3);
            y = mm2dot(5);
            //tscDll.printerfont(x, y, "3", 0, 2, 2, "PO:" + po);
            tscDll.sendcommand("BLOCK " + x + "," + y + ",560,240,\"3\",0,2,2,\"" + "PO:" + po + "\"\n");
            y = mm2dot(30);
            //tscDll.printerfont(x, y, "3", 0, 2, 2, "OE:" + oe);
            tscDll.sendcommand("BLOCK " + x + "," + y + ",560,160,\"3\",0,2,2,\"" + "OE:" + oe + "\"\n");

            tscDll.printlabel(1, 1);
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter", e.toString());
            return false;
        }
    }

    //20260122 Ann Add:COO
    public static boolean printMaterialLabel(String reelId, String partNo, String Description, String qty,
                                             String dateCode, String vendorCode, String po, String qrCode, String coo) {
        try {
            sendSetup();
            tscDll.sendcommand("CODEPAGE UTF-8\n");

            int x, y;
            Description = "Description:  " + Description;
            byte[] des = null;

            if (CharUtil.isChineseOrGreek(Description)) {
                des = new byte[1024];
                des = Description.getBytes("Big5");
            }

            if (!TextUtils.isEmpty(reelId)) {
                x = mm2dot(1);
                y = mm2dot(3);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Reel ID:");
                x = mm2dot(9);
                y = mm2dot(1);
                tscDll.barcode(x, y, "128", 30, 1, 0, 1, 1, reelId);
            }

            x = mm2dot(1);
            y = mm2dot(10);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "P/N:");
            x = mm2dot(8);
            y = mm2dot(8);
            tscDll.barcode(x, y, "128", 30, 0, 0, 2, 2, partNo);
            x = mm2dot(9);
            y = mm2dot(12);
            tscDll.printerfont(x, y, "0", 0, 12, 12, partNo);
            x = mm2dot(1);
            y = mm2dot(16);
            if (des == null) {
                tscDll.printerfont(x, y, "0", 0, 7, 7, Description);
            } else {
                tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
                tscDll.sendcommand(des);
                tscDll.sendcommand("\"\n");
            }
            y = mm2dot(20);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "Quantity:");
            x = mm2dot(13);
            y = mm2dot(19);
            tscDll.barcode(x, y, "128", 30, 0, 0, 2, 2, qty);
            x = mm2dot(13);
            y = mm2dot(23);
            tscDll.printerfont(x, y, "0", 0, 12, 12, qty);

            if (!TextUtils.isEmpty(dateCode)) {
                x = mm2dot(1);
                y = mm2dot(29);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Date Code:");
                x = mm2dot(13);
                y = mm2dot(27);
                tscDll.barcode(x, y, "128", 30, 1, 0, 2, 2, dateCode);
            }

            if (!TextUtils.isEmpty(vendorCode)) {
                x = mm2dot(1);
                y = mm2dot(36);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Vendor Code:");
                x = mm2dot(14);
                y = mm2dot(34);
                tscDll.barcode(x, y, "128", 30, 1, 0, 2, 2, vendorCode);
            }

            if (!TextUtils.isEmpty(po) && !po.toUpperCase().equals("NULL")) {
                x = mm2dot(1);
                y = mm2dot(43);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "PO NO:");
                x = mm2dot(9);
                y = mm2dot(41);
                tscDll.barcode(x, y, "128", 30, 1, 0, 2, 2, po);
            }

            //COO 20260122 Ann Add
            if(!coo.isEmpty()){
                x = mm2dot(40);
                y = mm2dot(20);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "COO:");
                x = mm2dot(45);
                y = mm2dot(19);
                tscDll.barcode(x, y, "128", 30, 0, 0, 2, 2, coo);
                x = mm2dot(45);
                y = mm2dot(23);
                tscDll.printerfont(x, y, "0", 0, 12, 12, coo);
            }

            if (!TextUtils.isEmpty(qrCode)) {
                /*x = mm2dot(40);
                y = mm2dot(19);*/
                x = mm2dot(45);
                y = mm2dot(27); //20260122 Ann Edit:QRCode從23下移至27
                tscDll.qrcode(x, y, "M", "4", "A", "0", "M2", "S7", qrCode);
                x = mm2dot(55);
            }

            y = mm2dot(46);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "W/H Packing");

            tscDll.printlabel(1, 1);
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter", e.toString());
            return false;
        }
    }

    //20260114 Ann Add:COO
    public static boolean printMaterialLabel3(String reelId, String partNo, String Description, String qty,
                                              String dateCode, String vendorCode, String po, String qrCode, String orgPrint, String coo) {
        try {
            sendSetup();
            tscDll.sendcommand("CODEPAGE UTF-8\n");

            int x, y;

            Description = "Description:  " + Description;
            byte[] des = null;
            if (CharUtil.isChineseOrGreek(Description)) {
                des = new byte[1024];
                des = Description.getBytes("Big5");
            }

            if (!TextUtils.isEmpty(reelId)) {
                x = mm2dot(1);
                y = mm2dot(3);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Reel ID:");
                x = mm2dot(9);
                y = mm2dot(1);
                tscDll.barcode(x, y, "128", 30, 1, 0, 1, 1, reelId);
            }

            x = mm2dot(1);
            y = mm2dot(10);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "P/N:");
            x = mm2dot(8);
            y = mm2dot(8);
            tscDll.barcode(x, y, "128", 30, 0, 0, 2, 2, partNo);
            x = mm2dot(9);
            y = mm2dot(12);
            tscDll.printerfont(x, y, "0", 0, 12, 12, partNo);
            x = mm2dot(1);
            y = mm2dot(16);
            if (des == null) {
                tscDll.printerfont(x, y, "0", 0, 7, 7, Description);
            } else {
                tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
                tscDll.sendcommand(des);
                tscDll.sendcommand("\"\n");
            }
            y = mm2dot(20);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "Quantity:");
            x = mm2dot(13);
            y = mm2dot(19);
            tscDll.barcode(x, y, "128", 30, 0, 0, 2, 2, qty);
            x = mm2dot(13);
            y = mm2dot(23);
            tscDll.printerfont(x, y, "0", 0, 12, 12, qty);

            if (!TextUtils.isEmpty(dateCode)) {
                x = mm2dot(1);
                y = mm2dot(29);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Date Code:");
                x = mm2dot(13);
                y = mm2dot(27);
                tscDll.barcode(x, y, "128", 30, 1, 0, 2, 2, dateCode);
            }

            if (!TextUtils.isEmpty(vendorCode)) {
                x = mm2dot(1);
                y = mm2dot(36);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Vendor Code:");
                x = mm2dot(14);
                y = mm2dot(34);
                tscDll.barcode(x, y, "128", 30, 1, 0, 2, 2, vendorCode);
            }

            if (!TextUtils.isEmpty(po) && !po.toUpperCase().equals("NULL")) {
                x = mm2dot(1);
                y = mm2dot(43);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "PO NO:");
                x = mm2dot(9);
                y = mm2dot(41);
                tscDll.barcode(x, y, "128", 30, 1, 0, 2, 2, po);
            }

            if(Constant.ISORG){
                /*x = mm2dot(50);
                y = mm2dot(7);

                if(!TextUtils.isEmpty(orgPrint)){
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,1,1,\"" + orgPrint + "\"\n");
                }else{
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,1,1,\"" + AppController.getOrgName() + "\"\n");
                }*/

                x = mm2dot(43);
                y = mm2dot(8);

                if(!TextUtils.isEmpty(orgPrint)){
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"3\",0,2,2,\"" + AppController.getOrgName(Integer.parseInt(orgPrint)) + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(9);
                    tscDll.barcode(x, y, "128", 30, 0, 0, 1, 1, orgPrint);
                }else{
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"3\",0,2,2,\"" + AppController.getOrgName() + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(9);
                    tscDll.barcode(x, y, "128", 30, 0, 0, 1, 1, AppController.getOrg()+"");
                }
            }

            //COO 20260114 Ann Add
            if(!coo.isEmpty()){
                x = mm2dot(40);
                y = mm2dot(20);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "COO:");
                x = mm2dot(45);
                y = mm2dot(19);
                tscDll.barcode(x, y, "128", 30, 0, 0, 2, 2, coo);
                x = mm2dot(45);
                y = mm2dot(23);
                tscDll.printerfont(x, y, "0", 0, 12, 12, coo);
            }

            if (!TextUtils.isEmpty(qrCode)) {
                /*x = mm2dot(40);
                y = mm2dot(19);*/
                x = mm2dot(45);
                y = mm2dot(27); //20260114 Ann Edit:QRCode從23下移至27
                tscDll.qrcode(x, y, "M", "4", "A", "0", "M2", "S7", qrCode);
                x = mm2dot(55);
            }

            y = mm2dot(46);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "W/H Packing");

            tscDll.printlabel(1, 1);
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter", e.toString());
            return false;
        }
    }

    //20260108 Ann Add:COO
    public static boolean printMaterialLabel2(String reelId, String partNo, String Description, String qty,
                                              String dateCode, String vendorCode, String po, String qrCode, String orgPrint, String coo) {
        try {
            sendSetup();
            tscDll.sendcommand("CODEPAGE UTF-8\n");

            int x, y;

            Description = "Description: " + Description;
            byte[] des = null;
            if (CharUtil.isChineseOrGreek(Description)) {
                des = new byte[1024];
                des = Description.getBytes("Big5");
            }

            if (!TextUtils.isEmpty(reelId)) {
                x = mm2dot(1);
                y = mm2dot(3);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Reel ID:");
                x = mm2dot(9);
                y = mm2dot(1);
                tscDll.barcode(x, y, "128", 30, 1, 0, 1, 1, reelId);
            }

            x = mm2dot(1);
            y = mm2dot(10);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "P/N:");
            x = mm2dot(8);
            y = mm2dot(8);
            tscDll.barcode(x, y, "128", 30, 0, 0, 2, 2, partNo);
            x = mm2dot(9);
            y = mm2dot(12);
            tscDll.printerfont(x, y, "0", 0, 12, 12, partNo);
            x = mm2dot(1);
            y = mm2dot(16);
            if (des == null) {
                tscDll.printerfont(x, y, "0", 0, 7, 7, Description);
            } else {
                tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
                tscDll.sendcommand(des);
                tscDll.sendcommand("\"\n");
            }
            y = mm2dot(20);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "Quantity:");
            x = mm2dot(13);
            y = mm2dot(19);
            tscDll.barcode(x, y, "128", 30, 0, 0, 2, 2, qty);
            x = mm2dot(13);
            y = mm2dot(23);
            tscDll.printerfont(x, y, "0", 0, 12, 12, qty);

            if (!TextUtils.isEmpty(dateCode)) {
                x = mm2dot(1);
                y = mm2dot(29);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Date Code:");
                x = mm2dot(13);
                y = mm2dot(27);
                tscDll.barcode(x, y, "128", 30, 1, 0, 2, 2, dateCode);
            }

            if (!TextUtils.isEmpty(vendorCode)) {
                x = mm2dot(1);
                y = mm2dot(36);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Vendor Code:");
                x = mm2dot(14);
                y = mm2dot(34);
                tscDll.barcode(x, y, "128", 30, 1, 0, 2, 2, vendorCode);
            }

            if (!TextUtils.isEmpty(po) && !po.toUpperCase().equals("NULL")) {
                x = mm2dot(1);
                y = mm2dot(43);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "PO NO:");
                x = mm2dot(9);
                y = mm2dot(41);
                tscDll.barcode(x, y, "128", 30, 1, 0, 2, 2, po);
            }

            if(Constant.ISORG){
                x = mm2dot(43);
                y = mm2dot(8);

                if(!TextUtils.isEmpty(orgPrint)){
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"3\",0,2,2,\"" + AppController.getOrgName(Integer.parseInt(orgPrint)) + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(9);
                    tscDll.barcode(x, y, "128", 30, 0, 0, 1, 1, orgPrint);
                }else{
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"3\",0,2,2,\"" + AppController.getOrgName() + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(9);
                    tscDll.barcode(x, y, "128", 30, 0, 0, 1, 1, AppController.getOrg()+"");
                }
                //TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,3,3,\"" + st1 + "\"\n");
            }

            //COO 20260108 Ann Add
            if(!coo.isEmpty()){
                x = mm2dot(40);
                y = mm2dot(20);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "COO:");
                x = mm2dot(45);
                y = mm2dot(19);
                tscDll.barcode(x, y, "128", 30, 0, 0, 2, 2, coo);
                x = mm2dot(45);
                y = mm2dot(23);
                tscDll.printerfont(x, y, "0", 0, 12, 12, coo);
            }

            if (!TextUtils.isEmpty(qrCode)) {
                /*x = mm2dot(40);
                y = mm2dot(19);*/
                x = mm2dot(45);
                y = mm2dot(27); //20260108 Ann Edit:QRCode從23下移至27
                tscDll.qrcode(x, y, "M", "4", "A", "0", "M2", "S7", qrCode);
                x = mm2dot(55);
            }

            y = mm2dot(46);

            tscDll.printlabel(1, 1);
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter", e.toString());
            return false;
        }
    }

    public static boolean printCustomerMaterialLabel(String reelId, String partNo, String Description, String qty,
                                                     String dateCode, String vendorCode, String po, String qrCode, String customerPartNo) {
        try {
            sendSetup();
            tscDll.sendcommand("CODEPAGE UTF-8\n");

            int x, y;

            Description = "Description:  " + Description;
            byte[] des = null;
            if (CharUtil.isChineseOrGreek(Description)) {
                des = new byte[1024];
                des = Description.getBytes("Big5");
            }

            if (!TextUtils.isEmpty(reelId)) {
                x = mm2dot(1);
                y = mm2dot(3);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Reel ID:");
                x = mm2dot(9);
                y = mm2dot(1);
                tscDll.barcode(x, y, "128", 30, 1, 0, 1, 1, reelId);
            }

            x = mm2dot(1);
            y = mm2dot(10);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "P/N:");
            x = mm2dot(8);
            y = mm2dot(8);
            tscDll.barcode(x, y, "128", 30, 0, 0, 2, 2, partNo);
            x = mm2dot(9);
            y = mm2dot(12);
            tscDll.printerfont(x, y, "0", 0, 12, 12, partNo);
            x = mm2dot(1);
            y = mm2dot(16);
            if (des == null) {
                tscDll.printerfont(x, y, "0", 0, 7, 7, Description);
            } else {
                tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
                tscDll.sendcommand(des);
                tscDll.sendcommand("\"\n");
            }
            y = mm2dot(20);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "Quantity:");
            x = mm2dot(13);
            y = mm2dot(19);
            tscDll.barcode(x, y, "128", 30, 0, 0, 2, 2, qty);
            x = mm2dot(13);
            y = mm2dot(23);
            tscDll.printerfont(x, y, "0", 0, 12, 12, qty);

            if (!TextUtils.isEmpty(dateCode)) {
                x = mm2dot(1);
                y = mm2dot(29);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Date Code:");
                x = mm2dot(13);
                y = mm2dot(27);
                tscDll.barcode(x, y, "128", 30, 1, 0, 2, 2, dateCode);
            }

            if (!TextUtils.isEmpty(vendorCode)) {
                x = mm2dot(1);
                y = mm2dot(36);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Vendor Code:");
                x = mm2dot(14);
                y = mm2dot(34);
                tscDll.barcode(x, y, "128", 30, 1, 0, 2, 2, vendorCode);
            }

            if (!TextUtils.isEmpty(po) && !po.toUpperCase().equals("NULL")) {
                x = mm2dot(1);
                y = mm2dot(43);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "PO NO:");
                x = mm2dot(9);
                y = mm2dot(41);
                tscDll.barcode(x, y, "128", 30, 1, 0, 2, 2, po);
            }

            if (!TextUtils.isEmpty(customerPartNo)) {
                x = mm2dot(1);
                y = mm2dot(43);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Customer P/N:");
                x = mm2dot(19);
                y = mm2dot(41);
                tscDll.barcode(x, y, "128", 30, 1, 0, 2, 5, customerPartNo);
                //tscDll.barcode(x, y, "128", 30, 1, 0, 2, 2, customerPartNo);
            }

            if (!TextUtils.isEmpty(qrCode)) {
                /*x = mm2dot(40);
                y = mm2dot(19);*/
                x = mm2dot(45);
                y = mm2dot(23);
                tscDll.qrcode(x, y, "M", "4", "A", "0", "M2", "S7", qrCode);
                x = mm2dot(55);
            }

            y = mm2dot(46);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "W/H Packing");

            tscDll.printlabel(1, 1);
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter", e.toString());
            return false;
        }
    }

    public static boolean printCustomerMaterialLabel1(String reelId, String partNo, String Description, String qty,
                                                      String dateCode, String vendorCode, String po, String qrCode, String customerPartNo, String orgPrint) {
        try {
            sendSetup();
            tscDll.sendcommand("CODEPAGE UTF-8\n");

            int x, y;

            Description = "Description:  " + Description;
            byte[] des = null;
            if (CharUtil.isChineseOrGreek(Description)) {
                des = new byte[1024];
                des = Description.getBytes("Big5");
            }

            if (!TextUtils.isEmpty(reelId)) {
                x = mm2dot(1);
                y = mm2dot(3);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Reel ID:");
                x = mm2dot(9);
                y = mm2dot(1);
                tscDll.barcode(x, y, "128", 30, 1, 0, 1, 1, reelId);
            }

            x = mm2dot(1);
            y = mm2dot(10);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "P/N:");
            x = mm2dot(8);
            y = mm2dot(8);
            //tscDll.barcode(x, y, "128", 30, 0, 0, 2, 2, partNo);
            tscDll.barcode(x, y, "128", 30, 0, 0, 1, 1, partNo);
            x = mm2dot(9);
            y = mm2dot(12);
            tscDll.printerfont(x, y, "0", 0, 12, 12, partNo);
            x = mm2dot(1);
            y = mm2dot(16);
            if (des == null) {
                tscDll.printerfont(x, y, "0", 0, 7, 7, Description);
            } else {
                tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
                tscDll.sendcommand(des);
                tscDll.sendcommand("\"\n");
            }
            y = mm2dot(20);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "Quantity:");
            x = mm2dot(13);
            y = mm2dot(19);
            tscDll.barcode(x, y, "128", 30, 0, 0, 2, 2, qty);
            x = mm2dot(13);
            y = mm2dot(23);
            tscDll.printerfont(x, y, "0", 0, 12, 12, qty);

            if (!TextUtils.isEmpty(dateCode)) {
                x = mm2dot(1);
                y = mm2dot(29);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Date Code:");
                x = mm2dot(13);
                y = mm2dot(27);
                tscDll.barcode(x, y, "128", 30, 1, 0, 2, 2, dateCode);
            }

            if (!TextUtils.isEmpty(vendorCode)) {
                x = mm2dot(1);
                y = mm2dot(36);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Vendor Code:");
                x = mm2dot(14);
                y = mm2dot(34);
                tscDll.barcode(x, y, "128", 30, 1, 0, 2, 2, vendorCode);
            }

            if (!TextUtils.isEmpty(po) && !po.toUpperCase().equals("NULL")) {
                x = mm2dot(1);
                y = mm2dot(43);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "PO NO:");
                x = mm2dot(9);
                y = mm2dot(41);
                tscDll.barcode(x, y, "128", 30, 1, 0, 2, 2, po);
            }

            if (!TextUtils.isEmpty(customerPartNo)) {
                x = mm2dot(1);
                y = mm2dot(43);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Customer P/N:");
                x = mm2dot(19);
                y = mm2dot(41);
                tscDll.barcode(x, y, "128", 30, 1, 0, 2, 5, customerPartNo);
                //tscDll.barcode(x, y, "128", 30, 1, 0, 2, 2, customerPartNo);
            }

            if(Constant.ISORG){
                /*x = mm2dot(50);
                y = mm2dot(7);

                if(!TextUtils.isEmpty(orgPrint)){
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,1,1,\"" + orgPrint + "\"\n");
                }else{
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,1,1,\"" + AppController.getOrgName() + "\"\n");
                }*/

                x = mm2dot(43);
                y = mm2dot(8);

                if(!TextUtils.isEmpty(orgPrint)){
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"3\",0,2,2,\"" + AppController.getOrgName(Integer.parseInt(orgPrint)) + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(9);
                    tscDll.barcode(x, y, "128", 30, 0, 0, 1, 1, orgPrint);
                }else{
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"3\",0,2,2,\"" + AppController.getOrgName() + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(9);
                    tscDll.barcode(x, y, "128", 30, 0, 0, 1, 1, AppController.getOrg()+"");
                }
            }

            if (!TextUtils.isEmpty(qrCode)) {
                /*x = mm2dot(40);
                y = mm2dot(19);*/
                x = mm2dot(45);
                y = mm2dot(23);
                tscDll.qrcode(x, y, "M", "4", "A", "0", "M2", "S7", qrCode);
                x = mm2dot(55);
            }

            y = mm2dot(46);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "W/H Packing");

            tscDll.printlabel(1, 1);
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter", e.toString());
            return false;
        }
    }

    //客供材料進貨標籤列印作業
    public static boolean printCustomerMaterialReceiptsLabel(String reelId, String partNo, String Description, String qty,
                                                             String dateCode, String vendorCode, String po, String qrCode, String customerPartNo, String orgPrint) {
        try {
            sendSetup();
            tscDll.sendcommand("CODEPAGE UTF-8\n");

            int x, y;

            Description = "Description:  " + Description;
            byte[] des = null;
            if (CharUtil.isChineseOrGreek(Description)) {
                des = new byte[1024];
                des = Description.getBytes("Big5");
            }

            if (!TextUtils.isEmpty(reelId)) {
                x = mm2dot(1);
                y = mm2dot(3);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Reel ID:");
                x = mm2dot(9);
                y = mm2dot(1);
                tscDll.barcode(x, y, "128", 30, 1, 0, 1, 1, reelId);
            }

            x = mm2dot(1);
            y = mm2dot(10);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "P/N:");
            x = mm2dot(8);
            y = mm2dot(8);
            //tscDll.barcode(x, y, "128", 30, 0, 0, 2, 2, partNo);
            tscDll.barcode(x, y, "128", 30, 0, 0, 1, 1, partNo);
            //tscDll.sendcommand("BARCODE " + x + "," + y + ",\"FONT001\",0,2,2,\"");
            x = mm2dot(9);
            y = mm2dot(12);
            tscDll.printerfont(x, y, "0", 0, 12, 12, partNo);
            x = mm2dot(1);
            y = mm2dot(16);
            if (des == null) {
                tscDll.printerfont(x, y, "0", 0, 7, 7, Description);
            } else {
                tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
                tscDll.sendcommand(des);
                tscDll.sendcommand("\"\n");
            }
            y = mm2dot(20);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "Quantity:");
            x = mm2dot(13);
            y = mm2dot(19);
            tscDll.barcode(x, y, "128", 30, 0, 0, 2, 2, qty);
            x = mm2dot(13);
            y = mm2dot(23);
            tscDll.printerfont(x, y, "0", 0, 12, 12, qty);

            if (!TextUtils.isEmpty(dateCode)) {
                x = mm2dot(1);
                y = mm2dot(29);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Date Code:");
                x = mm2dot(13);
                y = mm2dot(27);
                tscDll.barcode(x, y, "128", 30, 1, 0, 2, 2, dateCode);
            }

            if (!TextUtils.isEmpty(vendorCode)) {
                x = mm2dot(1);
                y = mm2dot(36);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Vendor Code:");
                x = mm2dot(14);
                y = mm2dot(34);
                tscDll.barcode(x, y, "128", 30, 1, 0, 2, 2, vendorCode);
            }

            if (!TextUtils.isEmpty(po) && !po.toUpperCase().equals("NULL")) {
                x = mm2dot(1);
                y = mm2dot(43);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "PO NO:");
                x = mm2dot(9);
                y = mm2dot(41);
                tscDll.barcode(x, y, "128", 30, 1, 0, 2, 2, po);
            }

            if (!TextUtils.isEmpty(customerPartNo)) {
                x = mm2dot(1);
                y = mm2dot(43);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Customer P/N:");
                x = mm2dot(19);
                y = mm2dot(41);
                tscDll.barcode(x, y, "128", 30, 1, 0, 2, 5, customerPartNo);
                /*tscDll.barcode(x, y, "128", 30, 1, 0, 2, 2, customerPartNo);
                x = mm2dot(19);
                y = mm2dot(44);
                tscDll.printerfont(x, y, "0", 0, 7, 7, customerPartNo);*/
            }

            if(Constant.ISORG){
                /*x = mm2dot(50);
                y = mm2dot(7);

                if(!TextUtils.isEmpty(orgPrint)){
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,1,1,\"" + orgPrint + "\"\n");
                }else{
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,1,1,\"" + AppController.getOrgName() + "\"\n");
                }*/

                x = mm2dot(43);
                y = mm2dot(8);

                if(!TextUtils.isEmpty(orgPrint)){
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"3\",0,2,2,\"" + AppController.getOrgName(Integer.parseInt(orgPrint)) + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(9);
                    tscDll.barcode(x, y, "128", 30, 0, 0, 1, 1, orgPrint);
                }else{
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"3\",0,2,2,\"" + AppController.getOrgName() + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(9);
                    tscDll.barcode(x, y, "128", 30, 0, 0, 1, 1, AppController.getOrg()+"");
                }
            }

            if (!TextUtils.isEmpty(qrCode)) {
                /*x = mm2dot(40);
                y = mm2dot(19);*/
                x = mm2dot(45);
                y = mm2dot(23);
                tscDll.qrcode(x, y, "M", "4", "A", "0", "M2", "S7", qrCode);
                x = mm2dot(55);
            }

            tscDll.printlabel(1, 1);
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter", e.toString());
            return false;
        }
    }

    //製令標籤補標作業
    //20260120 Ann Add:COO
    public static boolean printWorkOrderLabel(String lineNo, String mergeNo, String reelId, String partNo, String description, String qty,
                                              String dateCode, String vendorCode, String po, String remark, String qrCode, String orgPrint, String coo) {
        try {
            sendSetup();
            tscDll.sendcommand("CODEPAGE UTF-8\n");

            int x, y;

            description = "Description: " + description;
            byte[] des = null;
            if (CharUtil.isChineseOrGreek(description)) {
                des = description.getBytes("Big5");
            }

            byte[] rem = null;
            if (CharUtil.isChineseOrGreek(remark)) {
                rem = remark.getBytes("Big5");
            }

            x = mm2dot(1);
            y = mm2dot(1);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "Line: " + lineNo);
            x = mm2dot(36);
            y = mm2dot(1);
            if (TextUtils.isEmpty(mergeNo))
                mergeNo = "";
            tscDll.printerfont(x, y, "0", 0, 7, 7, "Merge No: " + mergeNo);

            if (!TextUtils.isEmpty(reelId)) {
                x = mm2dot(1);
                y = mm2dot(7);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Reel ID:");
                x = mm2dot(9);
                y = mm2dot(4);
                tscDll.barcode(x, y, "128", 20, 1, 0, 1, 1, reelId);
            }

            x = mm2dot(1);
            y = mm2dot(12);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "P/N:");
            x = mm2dot(8);
            y = mm2dot(10);
            tscDll.barcode(x, y, "128", 20, 0, 0, 2, 5, partNo);
            x = mm2dot(9);
            y = mm2dot(13);
            tscDll.printerfont(x, y, "0", 0, 12, 12, partNo);
            x = mm2dot(1);
            y = mm2dot(17);
            if (des == null) {
                tscDll.printerfont(x, y, "0", 0, 7, 7, description);
            } else {
                tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
                tscDll.sendcommand(des);
                tscDll.sendcommand("\"\n");
            }
            y = mm2dot(22);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "Quantity:");
            x = mm2dot(13);
            y = mm2dot(20);
            tscDll.barcode(x, y, "128", 20, 0, 0, 2, 2, qty);
            x = mm2dot(13);
            y = mm2dot(23);
            tscDll.printerfont(x, y, "0", 0, 12, 12, qty);
            x = mm2dot(1);
            y = mm2dot(29);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "Date Code:");

            if (!TextUtils.isEmpty(dateCode)) {
                x = mm2dot(13);
                y = mm2dot(27);
                tscDll.barcode(x, y, "128", 20, 1, 0, 2, 2, dateCode);
            }

            x = mm2dot(1);
            y = mm2dot(35);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "Vendor Code:");

            if (!TextUtils.isEmpty(vendorCode)) {
                x = mm2dot(15);
                y = mm2dot(33);
                tscDll.barcode(x, y, "128", 20, 1, 0, 2, 2, vendorCode);
            }

            if (!TextUtils.isEmpty(po) && !po.toUpperCase().equals("NULL")) {
                x = mm2dot(1);
                y = mm2dot(41);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "PO NO:");
                x = mm2dot(9);
                y = mm2dot(39);
                tscDll.barcode(x, y, "128", 20, 1, 0, 2, 2, po);
            }

            x = mm2dot(3);
            y = mm2dot(45);
            if (rem == null) {
                tscDll.sendcommand("BLOCK " + x + "," + y + ",512,42,\"0\",0,7,7,\"" + remark + "\"\n");
            } else {
                tscDll.sendcommand("BLOCK " + x + "," + y + ",512,42,\"FONT001\",0,2,2,\"");
                tscDll.sendcommand(rem);
                tscDll.sendcommand("\"\n");
            }

            /*x = mm2dot(40);
            y = mm2dot(21);*/
            if(Constant.ISORG){
                /*x = mm2dot(47);
                y = mm2dot(43);

                if(!TextUtils.isEmpty(orgPrint)){
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,1,1,\"" + orgPrint + "\"\n");
                }else{
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,1,1,\"" + AppController.getOrgName() + "\"\n");
                }*/

                x = mm2dot(43);
                y = mm2dot(9);

                if(!TextUtils.isEmpty(orgPrint)){
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"3\",0,2,2,\"" + AppController.getOrgName(Integer.parseInt(orgPrint)) + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(9);
                    tscDll.barcode(x, y, "128", 30, 0, 0, 1, 1, orgPrint);
                }else{
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"3\",0,2,2,\"" + AppController.getOrgName() + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(9);
                    tscDll.barcode(x, y, "128", 30, 0, 0, 1, 1, AppController.getOrg()+"");
                }
            }

            //COO 20260120 Ann Add
            if(!coo.isEmpty()){
                x = mm2dot(40);
                y = mm2dot(20);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "COO:");
                x = mm2dot(45);
                y = mm2dot(19);
                tscDll.barcode(x, y, "128", 30, 0, 0, 2, 2, coo);
                x = mm2dot(45);
                y = mm2dot(23);
                tscDll.printerfont(x, y, "0", 0, 12, 12, coo);
            }

            x = mm2dot(45);
            y = mm2dot(27); //20260120 Ann Edit:QRCode從23下移至27
            tscDll.qrcode(x, y, "M", "4", "A", "0", "M2", "S7", qrCode);

            tscDll.printlabel(1, 1);
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter", e.toString());
            return false;
        }
    }

    //製令欠料
    //20260122 Ann Add:COO
    public static boolean printWorkOrderLabel(String lineNo, String workOrderNo, String reelId, String partNo, String description, String qty,
                                              String dateCode, String vendorCode, String po, String qrCode, String coo) {
        try {
            sendSetup();
            tscDll.sendcommand("CODEPAGE UTF-8\n");

            int x, y;

            description = "Description: " + description;
            byte[] des = null;
            if (CharUtil.isChineseOrGreek(description)) {
                des = description.getBytes("Big5");
            }

            x = mm2dot(1);
            y = mm2dot(1);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "Line: " + lineNo);
            x = mm2dot(36);
            y = mm2dot(1);
            if (TextUtils.isEmpty(workOrderNo))
                workOrderNo = "";
            tscDll.printerfont(x, y, "0", 0, 7, 7, "W/O: " + workOrderNo);

            if (!TextUtils.isEmpty(reelId)) {
                x = mm2dot(1);
                y = mm2dot(7);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "Reel ID:");
                x = mm2dot(9);
                y = mm2dot(4);
                tscDll.barcode(x, y, "128", 20, 1, 0, 1, 1, reelId);
            }

            x = mm2dot(1);
            y = mm2dot(12);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "P/N:");
            x = mm2dot(8);
            y = mm2dot(10);
            tscDll.barcode(x, y, "128", 20, 0, 0, 2, 5, partNo);
            x = mm2dot(9);
            y = mm2dot(13);
            tscDll.printerfont(x, y, "0", 0, 12, 12, partNo);
            x = mm2dot(1);
            y = mm2dot(17);
            if (des == null) {
                tscDll.printerfont(x, y, "0", 0, 7, 7, description);
            } else {
                tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
                tscDll.sendcommand(des);
                tscDll.sendcommand("\"\n");
            }
            y = mm2dot(22);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "Quantity:");
            x = mm2dot(13);
            y = mm2dot(20);
            tscDll.barcode(x, y, "128", 20, 0, 0, 2, 2, qty);
            x = mm2dot(13);
            y = mm2dot(23);
            tscDll.printerfont(x, y, "0", 0, 12, 12, qty);
            x = mm2dot(1);
            y = mm2dot(29);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "Date Code:");

            if (!TextUtils.isEmpty(dateCode)) {
                x = mm2dot(13);
                y = mm2dot(27);
                tscDll.barcode(x, y, "128", 20, 1, 0, 2, 2, dateCode);
            }

            x = mm2dot(1);
            y = mm2dot(35);
            tscDll.printerfont(x, y, "0", 0, 7, 7, "Vendor Code:");

            if (!TextUtils.isEmpty(vendorCode)) {
                x = mm2dot(15);
                y = mm2dot(33);
                tscDll.barcode(x, y, "128", 20, 1, 0, 2, 2, vendorCode);
            }

            if (!TextUtils.isEmpty(po) && !po.toUpperCase().equals("NULL")) {
                x = mm2dot(1);
                y = mm2dot(41);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "PO NO:");
                x = mm2dot(9);
                y = mm2dot(39);
                tscDll.barcode(x, y, "128", 20, 1, 0, 2, 2, po);
            }

            //COO 20260122 Ann Add
            if(!coo.isEmpty()){
                x = mm2dot(40);
                y = mm2dot(20);
                tscDll.printerfont(x, y, "0", 0, 7, 7, "COO:");
                x = mm2dot(45);
                y = mm2dot(19);
                tscDll.barcode(x, y, "128", 30, 0, 0, 2, 2, coo);
                x = mm2dot(45);
                y = mm2dot(23);
                tscDll.printerfont(x, y, "0", 0, 12, 12, coo);
            }

            if (!TextUtils.isEmpty(qrCode)) {
                /*x = mm2dot(40);
                y = mm2dot(21);*/
                x = mm2dot(45);
                y = mm2dot(27); //20260122 Ann Edit:QRCode從23下移至27
                tscDll.qrcode(x, y, "M", "4", "A", "0", "M2", "S7", qrCode);
            }

            tscDll.printlabel(1, 1);
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter", e.toString());
            return false;
        }
    }

    public static boolean printShipmentPalletLabel(String strMark, String strShippingWay, int intDnNumber, String strPalletNumber,
                                                   int intBoxQty, String qrCode) {
        try {
            sendSetup();
            tscDll.sendcommand("CODEPAGE UTF-8\n");

            int x, y;

            byte[] mark = null;
            if (CharUtil.isChineseOrGreek(strMark)) {
                mark = strMark.getBytes("Big5");
            }

            byte[] shippingWay = null;
            if (CharUtil.isChineseOrGreek(strShippingWay)) {
                shippingWay = strShippingWay.getBytes("Big5");
            }

            x = mm2dot(1);
            y = mm2dot(1);
            if (mark == null) {
                tscDll.printerfont(x, y, "0", 0, 18, 18, TextUtils.isEmpty(strMark) ? "" : strMark);
            } else {
                tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,4,4,\"");
                tscDll.sendcommand(mark);
                tscDll.sendcommand("\"\n");
            }

            y = mm2dot(10);
            if (shippingWay == null) {
                tscDll.printerfont(x, y, "0", 0, 18, 18, TextUtils.isEmpty(strShippingWay) ? "" : strShippingWay);
            } else {
                tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,4,4,\"");
                tscDll.sendcommand(shippingWay);
                tscDll.sendcommand("\"\n");
            }

            y = mm2dot(19);
            tscDll.printerfont(x, y, "0", 0, 18, 18, String.valueOf(intDnNumber));
            y = mm2dot(27);
            tscDll.printerfont(x, y, "0", 0, 18, 18, "Pallet No:");
            y = mm2dot(35);
            tscDll.printerfont(x, y, "0", 0, 18, 18, strPalletNumber);
            y = mm2dot(43);
            if (intBoxQty > 0) {
                tscDll.printerfont(x, y, "0", 0, 18, 18, intBoxQty + " CTN");
            } else {
                tscDll.printerfont(x, y, "0", 0, 15, 15, "     CTN");
            }

            x = mm2dot(43);
            y = mm2dot(19);
            tscDll.qrcode(x, y, "M", "4", "A", "0", "M2", "S7", qrCode);

            tscDll.printlabel(1, 1);
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter", e.toString());
            return false;
        }
    }

    public static boolean printShipmentPalletLabel1(String strMark, String strShippingWay, int intDnNumber, String strPalletNumber,
                                                    int intBoxQty, String qrCode, String orgPrint) {
        try {
            sendSetup();
            tscDll.sendcommand("CODEPAGE UTF-8\n");
            int x, y;

            byte[] mark = null;
            if (CharUtil.isChineseOrGreek(strMark)) {
                mark = strMark.getBytes("Big5");
            }

            Log.d("BTPrnter", "3");
            byte[] shippingWay = null;
            if (CharUtil.isChineseOrGreek(strShippingWay)) {
                shippingWay = strShippingWay.getBytes("Big5");
            }

            Log.d("BTPrnter", "4");
            x = mm2dot(1);
            y = mm2dot(1);
            if (mark == null) {
                tscDll.printerfont(x, y, "0", 0, 18, 18, TextUtils.isEmpty(strMark) ? "" : strMark);
            } else {
                tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,4,4,\"");
                tscDll.sendcommand(mark);
                tscDll.sendcommand("\"\n");
            }

            y = mm2dot(10);
            if (shippingWay == null) {
                tscDll.printerfont(x, y, "0", 0, 18, 18, TextUtils.isEmpty(strShippingWay) ? "" : strShippingWay);
            } else {
                tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,4,4,\"");
                tscDll.sendcommand(shippingWay);
                tscDll.sendcommand("\"\n");
            }

            y = mm2dot(19);
            tscDll.printerfont(x, y, "0", 0, 18, 18, String.valueOf(intDnNumber));
            y = mm2dot(27);
            tscDll.printerfont(x, y, "0", 0, 18, 18, "Pallet No:");
            y = mm2dot(35);
            tscDll.printerfont(x, y, "0", 0, 18, 18, strPalletNumber);
            y = mm2dot(43);
            if (intBoxQty > 0) {
                tscDll.printerfont(x, y, "0", 0, 18, 18, intBoxQty + " CTN");
            } else {
                tscDll.printerfont(x, y, "0", 0, 15, 15, "     CTN");
            }

            if(Constant.ISORG){ //出貨棧板標籤列印
                x = mm2dot(47);
                y = mm2dot(43);

                if(!TextUtils.isEmpty(orgPrint)){
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,1,1,\"" + AppController.getOrgName(Integer.parseInt(orgPrint)) + "\"\n");
                }else{
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,1,1,\"" + AppController.getOrgName() + "\"\n");
                }

                /*x = mm2dot(47);
                y = mm2dot(43);

                if(!TextUtils.isEmpty(orgPrint)){
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"3\",0,2,2,\"" + AppController.getOrgName(Integer.parseInt(orgPrint)) + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(9);
                    tscDll.barcode(x, y, "128", 30, 0, 0, 1, 1, orgPrint);
                }else{
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"3\",0,2,2,\"" + AppController.getOrgName() + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(9);
                    tscDll.barcode(x, y, "128", 30, 0, 0, 1, 1, AppController.getOrg()+"");
                }*/
            }

            x = mm2dot(43);
            y = mm2dot(19);
            tscDll.qrcode(x, y, "M", "4", "A", "0", "M2", "S7", qrCode);

            tscDll.printlabel(1, 1);
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter1", "printShipmentPalletLabel1 "+e.toString());
            return false;
        }
    }

    public static boolean printShipmentPalletLabel2(String strMark, String strShippingWay, int intDnNumber, String strPalletNumber,
                                                    int intBoxQty, String qrCode, String orgPrint) {
        try {
            if (tscDll == null) {
                tscDll = new TSCActivity();
            }

            //sendSetup();
            /* parameter e: 字串型別，設定使用感應器類別
             0 表示使用垂直間距感測器(gap sensor)
             1 表示使用黑標感測器(black mark sensor)*/
            tscDll.setup(70, 50, 3, 7, 0, 3, 0);//EG230106-01@579639@a@0@86
            //tscDll.sendcommand("DIRECTION 0\n");
            tscDll.clearbuffer();

            //tscDll.sendcommand("CODEPAGE UTF-8\n");
            int x, y;

            byte[] mark = null;
            if (CharUtil.isChineseOrGreek(strMark)) {
                mark = strMark.getBytes("Big5");
            }

            Log.d("BTPrnter", "3");
            byte[] shippingWay = null;
            if (CharUtil.isChineseOrGreek(strShippingWay)) {
                shippingWay = strShippingWay.getBytes("Big5");
            }

            Log.d("BTPrnter", "4");
            x = mm2dot(1);
            y = mm2dot(1);
            if (mark == null) {
                tscDll.printerfont(x, y, "0", 0, 18, 18, TextUtils.isEmpty(strMark) ? "" : strMark);
            } else {
                tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,4,4,\"");
                tscDll.sendcommand(mark);
                tscDll.sendcommand("\"\n");
            }

            y = mm2dot(10);
            if (shippingWay == null) {
                tscDll.printerfont(x, y, "0", 0, 18, 18, TextUtils.isEmpty(strShippingWay) ? "" : strShippingWay);
            } else {
                tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,4,4,\"");
                tscDll.sendcommand(shippingWay);
                tscDll.sendcommand("\"\n");
            }

            y = mm2dot(19);
            tscDll.printerfont(x, y, "0", 0, 18, 18, String.valueOf(intDnNumber));
            y = mm2dot(27);
            tscDll.printerfont(x, y, "0", 0, 18, 18, "Pallet No:");
            y = mm2dot(35);
            tscDll.printerfont(x, y, "0", 0, 18, 18, strPalletNumber);
            y = mm2dot(43);
            if (intBoxQty > 0) {
                tscDll.printerfont(x, y, "0", 0, 18, 18, intBoxQty + " CTN");
            } else {
                tscDll.printerfont(x, y, "0", 0, 15, 15, "     CTN");
            }

            if(Constant.ISORG){ //出貨棧板標籤列印
                x = mm2dot(47);
                y = mm2dot(43);

                if(!TextUtils.isEmpty(orgPrint)){
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,1,1,\"" + orgPrint + "\"\n");
                }else{
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,1,1,\"" + AppController.getOrgName() + "\"\n");
                }

                /*x = mm2dot(47);
                y = mm2dot(43);

                if(!TextUtils.isEmpty(orgPrint)){
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"3\",0,2,2,\"" + AppController.getOrgName(Integer.parseInt(orgPrint)) + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(9);
                    tscDll.barcode(x, y, "128", 30, 0, 0, 1, 1, orgPrint);
                }else{
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"3\",0,2,2,\"" + AppController.getOrgName() + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(9);
                    tscDll.barcode(x, y, "128", 30, 0, 0, 1, 1, AppController.getOrg()+"");
                }*/
            }

            x = mm2dot(43);
            y = mm2dot(19);
            tscDll.qrcode(x, y, "M", "4", "A", "0", "M2", "S7", qrCode);

            tscDll.printlabel(1, 1);
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter1", "printShipmentPalletLabel1 "+e.toString());
            return false;
        }
    }

    public static boolean printShipmentPalletLabelOld(String strMark, String strShippingWay, int intDnNumber, String strPalletNumber, int intBoxQty) {
        try {
            /*if (!connect())
                return false;*/

            sendSetup();
            tscDll.sendcommand("CODEPAGE UTF-8\n");

            int x, y;

            byte[] mark = null;
            if (CharUtil.isChineseOrGreek(strMark)) {
                mark = strMark.getBytes("Big5");
            }

            byte[] shippingWay = null;
            if (CharUtil.isChineseOrGreek(strShippingWay)) {
                shippingWay = strShippingWay.getBytes("Big5");
            }

            x = mm2dot(2);
            y = mm2dot(2);
            if (mark == null) {
                tscDll.printerfont(x, y, "0", 0, 20, 20, TextUtils.isEmpty(strMark) ? "" : strMark);
            } else {
                tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,4,4,\"");
                tscDll.sendcommand(mark);
                tscDll.sendcommand("\"\n");
            }

            y = mm2dot(9);
            if (shippingWay == null) {
                tscDll.printerfont(x, y, "0", 0, 20, 20, TextUtils.isEmpty(strShippingWay) ? "" : strShippingWay);
            } else {
                tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,4,4,\"");
                tscDll.sendcommand(shippingWay);
                tscDll.sendcommand("\"\n");
            }

            y = mm2dot(16);
            tscDll.barcode(x, y, "128", 40, 2, 0, 2, 2, String.valueOf(intDnNumber));
            y = mm2dot(25);
            tscDll.printerfont(x, y, "0", 0, 20, 20, "P/NO:");
            x = mm2dot(21);
            y = mm2dot(25);
            tscDll.printerfont(x, y, "0", 0, 20, 20, strPalletNumber);
            x = mm2dot(2);
            y = mm2dot(32);
            tscDll.printerfont(x, y, "0", 0, 20, 20, intBoxQty + " CTN");
            y = mm2dot(39);
            tscDll.barcode(x, y, "128", 40, 2, 0, 2, 2, intDnNumber + strPalletNumber);

            tscDll.printlabel(1, 1);
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter", e.toString());
            return false;
        }
    }

    public static boolean printRmaPalletLabel(String strMark, String strPalletId, String strPalletNumber, int intBoxQty) {
        try {
            /*if (!connect())
                return false;*/

            sendSetup();
            tscDll.sendcommand("CODEPAGE UTF-8\n");

            int x, y;

            byte[] mark = null;
            if (CharUtil.isChineseOrGreek(strMark)) {
                mark = strMark.getBytes("Big5");
            }

            x = mm2dot(3);
            y = mm2dot(3);
            if (mark == null) {
                tscDll.printerfont(x, y, "0", 0, 20, 20, TextUtils.isEmpty(strMark) ? "" : strMark);
            } else {
                tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,4,4,\"");
                tscDll.sendcommand(mark);
                tscDll.sendcommand("\"\n");
            }

            y = mm2dot(17);
            tscDll.printerfont(x, y, "0", 0, 20, 20, "P/ID:");
            x = mm2dot(28);
            y = mm2dot(15);
            tscDll.barcode(x, y, "128", 80, 2, 0, 2, 2, strPalletId);
            x = mm2dot(3);
            y = mm2dot(32);
            tscDll.printerfont(x, y, "0", 0, 20, 20, "P/NO:");
            x = mm2dot(28);
            y = mm2dot(32);
            tscDll.printerfont(x, y, "0", 0, 20, 20, strPalletNumber);
            x = mm2dot(3);
            y = mm2dot(42);
            tscDll.printerfont(x, y, "0", 0, 20, 20, intBoxQty + " CTN");

            tscDll.printlabel(1, 1);
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter", e.toString());
            return false;
        }
    }

    public static boolean printPalletReceived(String currentTimeStamp, String palletNo) {
        try {
            /*if (!connect())
                return false;*/

            sendSetup();

            int x, y;

            x = mm2dot(1);
            y = mm2dot(3);
            tscDll.printerfont(x, y, "0", 0, 17, 17, currentTimeStamp);
            y = mm2dot(23);
            tscDll.printerfont(x, y, "0", 0, 17, 17, "P/NO:");
            x = mm2dot(15);
            y = mm2dot(23);
            tscDll.printerfont(x, y, "0", 0, 17, 17, palletNo);
            x = mm2dot(17);
            y = mm2dot(40);
            tscDll.printerfont(x, y, "0", 0, 17, 17, "(Received)");

            tscDll.printlabel(1, 1);
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter", e.toString());
            return false;
        }
    }

    private static int mm2dot(int mm) {
        /*200 DPI，1點=1/8 mm
        300 DPI，1點=1/12 mm
        200 DPI: 1 mm = 8 dots
        300 DPI: 1 mm = 12 dots
        Alpha3R 200 DPI*/
        int factor = 8;

        return mm * factor;
    }

    public static boolean isPrinterAvailable() {
        try {
            if (tscDll.status().equals("Ready"))
                return true;
            else
                return false;
        } catch (Exception ex) {
            //Log.d("BTPrnter", ex.getMessage() == null ? "無法錯誤訊息" : ex.getMessage());
            return false;
        }
    }

    public static boolean connect() {
        Log.d("BTPrnter", "connect()");
        try {
            if (isConnected) {
                return true;
            } else {
                String returnCode = tscDll.openport(btPrinter.getAddress());
                if (!TextUtils.isEmpty(returnCode) && returnCode.equals("1")) {
                    isConnected = true;
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception ex) {
            //Log.d("BTPrnter", ex.toString());
            isConnected = false;
            return false;
        }
    }

    public static boolean disconnect() {
        Log.d("BTPrnter", "disconnect()");
        try {
            if (isConnected) {
                String returnCode = tscDll.closeport();
                if (!TextUtils.isEmpty(returnCode) && returnCode.equals("1")) {
                    isConnected = false;
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } catch (Exception e) {
            isConnected = false;
            return false;
        }
    }

    private static void sendSetup() {
        /*parameter e: 字串型別，設定使用感應器類別
        0 表示使用垂直間距感測器(gap sensor)
        1 表示使用黑標感測器(black mark sensor)*/
        tscDll.setup(70, 50, 3, 7, 0, 3, 0);
        tscDll.sendcommand("DIRECTION 0\n");
        tscDll.clearbuffer();
    }

    private static void sendSetup2() {
        /*parameter e: 字串型別，設定使用感應器類別
        0 表示使用垂直間距感測器(gap sensor)
        1 表示使用黑標感測器(black mark sensor)*/
        tscDll.setup(60, 10, 3, 7, 0, 3, 0);
        tscDll.sendcommand("DIRECTION 0\n");
        tscDll.clearbuffer();
    }

    public static boolean printInvoiceSeq(String seq) {
        try {
            //connect();
            sendSetup2();

            int x, y;

            x = mm2dot(5);
            y = mm2dot(0);
            tscDll.printerfont(x, y, "0", 0, 5, 5, "|");
            y = mm2dot(2);
            tscDll.printerfont(x, y, "0", 0, 5, 5, "|");
            y = mm2dot(4);
            tscDll.printerfont(x, y, "0", 0, 5, 5, "|");
            y = mm2dot(6);
            tscDll.printerfont(x, y, "0", 0, 5, 5, "|");
            y = mm2dot(8);
            tscDll.printerfont(x, y, "0", 0, 5, 5, "|");

            x = mm2dot(8);
            y = mm2dot(3);
            tscDll.printerfont(x, y, "0", 0, 15, 15, "Rec ID:");
            x = mm2dot(24);
            y = mm2dot(1);
            tscDll.barcode(x, y, "128", 25, 0, 0, 2, 5, seq);
            x = mm2dot(24);
            y = mm2dot(5);
            tscDll.printerfont(x, y, "0", 0, 12, 12, seq);

            tscDll.printlabel(1, 1);
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter", e.toString());
            return false;
        }
    }

    public static boolean printEzflowFormLabel(String formSn, String processSn) {
        try {
            sendSetup();
            tscDll.sendcommand("CODEPAGE UTF-8\n");

            int x, y;

            String ezFormSn = "表單單號:" + formSn;
            byte[] bytesEzFormSn = null;
            if (CharUtil.isChineseOrGreek(ezFormSn)) {
                bytesEzFormSn = ezFormSn.getBytes("Big5");
            }

            String ezProcessSn = "流程序號:" + processSn;
            byte[] bytesEzProcessSn = null;
            if (CharUtil.isChineseOrGreek(ezProcessSn)) {
                bytesEzProcessSn = ezProcessSn.getBytes("Big5");
            }

            x = mm2dot(1);
            y = mm2dot(3);
            if (bytesEzFormSn == null) {
                tscDll.printerfont(x, y, "0", 0, 7, 7, formSn);
            } else {
                tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
                tscDll.sendcommand(bytesEzFormSn);
                tscDll.sendcommand("\"\n");
            }

            //tscDll.printerfont(x, y, "0", 0, 10, 10, "表單單號:" + formSn);
            y = mm2dot(7);
            tscDll.barcode(x, y, "128", 50, 0, 0, 2, 2, formSn);
            y = mm2dot(14);
            tscDll.printerfont(x, y, "0", 0, 12, 12, formSn);

            y = mm2dot(25);
            if (bytesEzProcessSn == null) {
                tscDll.printerfont(x, y, "0", 0, 7, 7, processSn);
            } else {
                tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
                tscDll.sendcommand(bytesEzProcessSn);
                tscDll.sendcommand("\"\n");
            }

            //tscDll.printerfont(x, y, "0", 0, 10, 10, "流程序號:" + processSn);
            y = mm2dot(29);
            tscDll.barcode(x, y, "128", 50, 0, 0, 2, 2, processSn);
            y = mm2dot(36);
            tscDll.printerfont(x, y, "0", 0, 12, 12, processSn);
            x = mm2dot(49);
            y = mm2dot(33);
            String qrCode = formSn + "@" + processSn;
            tscDll.qrcode(x, y, "M", "4", "A", "0", "M2", "S7", qrCode);

            tscDll.printlabel(1, 1);
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter", e.toString());
            return false;
        }
    }

    public boolean discover(String printerName) {
        BluetoothAdapter mBtAdapter;
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        //Get a set of currently paired devices
        if (mBtAdapter != null) {
            Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

            //If there are paired devices, add each one to the ArrayAdapter
            if (pairedDevices != null && pairedDevices.size() > 0) {
                for (BluetoothDevice btdevice : pairedDevices) {
                    Log.d("BTPrnter", btdevice.getName() + ":" + btdevice.getAddress());

                    if (btdevice.getName().equals(printerName)) {
                        btPrinter = btdevice;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean printTest7(boolean rotate) {
        try {
            tscDll.openport(btPrinter.getAddress());
            tscDll.setup(70, 50, 3, 7, 0, 3, 0);
            tscDll.sendcommand("DIRECTION 0\n");
            tscDll.clearbuffer();

            int x, y;

            x = mm2dot(5);
            y = mm2dot(5);

            tscDll.printerfont(x, y, "5", 0, 1, 1, "coo Taiwan");

            y = mm2dot(15);
            tscDll.sendcommand("TEXT " + x + "," + y + ",\"Arial36\",0,1,1,\"SKU XY8BTCHUS\"\n");

            tscDll.printlabel(1, 1);
            tscDll.closeport();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public boolean printTest(boolean rotate) {
        try {
            tscDll.openport(btPrinter.getAddress());
            tscDll.setup(70, 50, 3, 7, 0, 3, 0);
            tscDll.sendcommand("DIRECTION 0\n");
            tscDll.clearbuffer();

            int x, y;

            x = mm2dot(5);
            y = mm2dot(5);
            tscDll.printerfont(x, y, "2", 0, 2, 2, "DN123456");

            y = mm2dot(20);
            tscDll.printerfont(x, y, "2", 0, 2, 2, "P12345678901");

            y = mm2dot(35);
            tscDll.barcode(x, y, "128", 60, 2, 0, 2, 5, "DN123456P12345678901");

            tscDll.printlabel(1, 1);
            tscDll.closeport();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static boolean printEIQCInspect(String status, String reelId) {
        try {
            if (tscDll == null) {
                tscDll = new TSCActivity();
            }

            AppController.debug("1 = "  + reelId);

            tscDll.openport(btPrinter.getAddress());
            //tscDll.setup(70, 50, 3, 7, 0, 3, 0);
            tscDll.setup(60, 10, 3, 7, 0, 3, 0);

            //tscDll.sendcommand("CODEPAGE UTF-8\n");

            tscDll.sendcommand("DIRECTION 0\n");
            tscDll.clearbuffer();

            AppController.debug("2 = "  + reelId);

            int x, y;

            //String strMark = status;
            status="免檢";

            byte[] mark = null;
            if (CharUtil.isChineseOrGreek(status)) {
                mark = status.getBytes("Big5");
            }

            AppController.debug("3 = "  + reelId);

            x = mm2dot(2);
            y = mm2dot(2);
            //tscDll.printerfont(x, y, "2", 0, 7, 7, "NO:");
            tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,4,4,\"");
            tscDll.sendcommand(mark);
            tscDll.sendcommand("\"\n");

            x = mm2dot(19);
            y = mm2dot(2);
            //tscDll.printerfont(x, y, "0", 0, 7, 7, "A"+reelId);
            tscDll.printerfont(x, y, "0", 0, 7, 27, reelId);

            /*x = mm2dot(19);
            y = mm2dot(11);
            tscDll.printerfont(x, y, "0", 0, 3, 13, "B"+reelId);

            x = mm2dot(19);
            y = mm2dot(21);
            //tscDll.printerfont(x, y, "2", 0, 7, 7, "C"+reelId);
            tscDll.printerfont(x, y, "0", 0, 8, 19, "C"+reelId);

            x = mm2dot(19);
            y = mm2dot(31);
            tscDll.printerfont(x, y, "0", 0, 7, 17, "D"+reelId);
            tscDll.barcode(x, y, "128", 30, 1, 0, 1, 1, reelId);*/

            AppController.debug("5 = "  + reelId);
            //tscDll.sendcommand("TEXT " + x + "," + y + ",\"Arial36\",0,1,1,\"SKU XY8BTCHUS\"\n");

            tscDll.printlabel(1, 1);
            tscDll.closeport();
            AppController.debug("6 = "  + reelId);
            return true;
        } catch (Exception e) {
            AppController.debug("BTPrnter:"+ e.toString());
            return false;
        }
    }

    public static boolean printCustomPrint(String data, int x, int y) {
        try {
            tscDll.openport(btPrinter.getAddress());
            //tscDll.setup(70, 50, 3, 7, 0, 3, 0);
            tscDll.setup(x, y, 3, 7, 0, 3, 0);
            tscDll.clearbuffer();
            tscDll.sendcommand("CODEPAGE UTF-8\n");

            String strMark = "免檢";

            byte[] mark = null;
            if (CharUtil.isChineseOrGreek(strMark)) {
                mark = strMark.getBytes("Big5");
            }

            x = mm2dot(2);
            y = mm2dot(1);
            //tscDll.printerfont(x, y, "2", 0, 7, 7, "NO:");
            tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,4,4,\"");
            tscDll.sendcommand(mark);
            tscDll.sendcommand("\"\n");

            x = mm2dot(19);
            y = mm2dot(1);
            tscDll.printerfont(x, y, "0", 0, 7, 27, data);
            //tscDll.sendcommand("TEXT " + x + "," + y + ",\"Arial36\",0,1,1,\"SKU XY8BTCHUS\"\n");

            tscDll.printlabel(1, 1);
            tscDll.closeport();
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter", e.toString());
            return false;
        }
    }

    public static boolean printBakeReprintLabel1(BakeHelper bakeHelper) {
        try {
            tscDll.setup(50, 20, 3, 7, 0, 3, 0);
            tscDll.sendcommand("DIRECTION 0\n");
            tscDll.clearbuffer();

            tscDll.sendcommand("CODEPAGE UTF-8\n");
            int x, y;

            byte[] mark = null;

            x = mm2dot(1);
            y = mm2dot(1);

            //tscDll.printerfont(x, y, "0", 0, 18, 18, "PN:");
            //tscDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,1,1,\"" + "PN:" + "\"\n");
            tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
            tscDll.sendcommand("PN:");
            tscDll.sendcommand("\"\n");

            x = mm2dot(13);
            //tscDll.printerfont(x, y, "0", 0, 18, 18, bakeHelper.getSKU());
            tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
            tscDll.sendcommand(bakeHelper.getSKU());
            tscDll.sendcommand("\"\n");

            x = mm2dot(1);
            y = mm2dot(6);
            //tscDll.printerfont(x, y, "0", 0, 18, 18, "BSD:");//Baking start date:
            tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
            tscDll.sendcommand("start:");
            tscDll.sendcommand("\"\n");

            x = mm2dot(14); //22
            //tscDll.printerfont(x, y, "0", 0, 18, 18, bakeHelper.getSTARTDATE());
            tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
            tscDll.sendcommand(bakeHelper.getSTARTDATE());
            tscDll.sendcommand("\"\n");

            y = mm2dot(11);
            x = mm2dot(1);
            //tscDll.printerfont(x, y, "0", 0, 18, 18, "BED:");//Baking end date:
            tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
            tscDll.sendcommand("end:");
            tscDll.sendcommand("\"\n");

            x = mm2dot(10);
            //tscDll.printerfont(x, y, "0", 0, 18, 18, bakeHelper.getENDDATE());
            tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
            tscDll.sendcommand(bakeHelper.getENDDATE());
            tscDll.sendcommand("\"\n");

            y = mm2dot(16);
            x = mm2dot(1);
            //tscDll.printerfont(x, y, "0", 0, 18, 18, bakeHelper.getNH()+"hr");
            tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
            tscDll.sendcommand(bakeHelper.getNH()+"hr");
            tscDll.sendcommand("\"\n");

            x = mm2dot(26);
            //tscDll.printerfont(x, y, "0", 0, 18, 18, "sign:");
            tscDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
            tscDll.sendcommand("sign:");
            tscDll.sendcommand("\"\n");

            /*y = mm2dot(43);
            if (intBoxQty > 0) {
                tscDll.printerfont(x, y, "0", 0, 18, 18, intBoxQty + " CTN");
            } else {
                tscDll.printerfont(x, y, "0", 0, 15, 15, "     CTN");
            }*/

            if(Constant.ISORG){ //出貨棧板標籤列印
                /*x = mm2dot(47);
                y = mm2dot(43);

                if(!TextUtils.isEmpty(orgPrint)){
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,1,1,\"" + AppController.getOrgName(Integer.parseInt(orgPrint)) + "\"\n");
                }else{
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,1,1,\"" + AppController.getOrgName() + "\"\n");
                }

                x = mm2dot(47);
                y = mm2dot(43);

                if(!TextUtils.isEmpty(orgPrint)){
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"3\",0,2,2,\"" + AppController.getOrgName(Integer.parseInt(orgPrint)) + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(9);
                    tscDll.barcode(x, y, "128", 30, 0, 0, 1, 1, orgPrint);
                }else{
                    tscDll.sendcommand("TEXT " + x + "," + y + ",\"3\",0,2,2,\"" + AppController.getOrgName() + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(9);
                    tscDll.barcode(x, y, "128", 30, 0, 0, 1, 1, AppController.getOrg()+"");
                }*/
            }

            x = mm2dot(43);
            y = mm2dot(19);
            //tscDll.qrcode(x, y, "M", "4", "A", "0", "M2", "S7", qrCode);

            tscDll.printlabel(1, 1);
            return true;
        } catch (Exception e) {
            Log.d("BTPrnter1", "printShipmentPalletLabel1 "+e.toString());
            return false;
        }
    }
}
