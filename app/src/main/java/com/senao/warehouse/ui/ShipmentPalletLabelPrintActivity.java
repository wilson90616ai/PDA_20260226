package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tscdll.TscWifiActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.asynctask.AsyncResponse;
import com.senao.warehouse.asynctask.CallWebApi;
import com.senao.warehouse.database.ShipmentPalletSnInfoHelperSophos;
import com.senao.warehouse.handler.NewPrintDataList;
import com.senao.warehouse.handler.Pdf417Info_SOPHOS;
import com.senao.warehouse.print.BtPrintLabel;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.ChkDeliveryInfoHelper;
import com.senao.warehouse.database.ExtremePalletInfoHelper;
import com.senao.warehouse.database.ExtremePalletItem;
import com.senao.warehouse.database.PALLET_LABEL_QR_CODE_FORMAT;
import com.senao.warehouse.database.ShipmentPalletInfoHelper;
import com.senao.warehouse.database.ShipmentPalletSnInfoHelper;
import com.senao.warehouse.handler.PrintLabelHandler;
import com.senao.warehouse.handler.ShippingVerifyMainHandler;
import com.senao.warehouse.database.NewPrintData;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.Preferences;
import com.senao.warehouse.util.QrCodeUtil;
import com.senao.warehouse.util.ReturnCode;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShipmentPalletLabelPrintActivity extends Activity {
    /*
     * 20250818 Ann Edit:新增usage_type，判斷是成品還是配件，以此決定要抓什麼作為數量(SENAO101-202506-0226)
     */
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PRINTER_SETTING = 2;
    private static final int REQUEST_BLUETOOTH_SETTINGS = 3;
    private static final int PRINTTYPE_CODESOFT = 1;
    private static final int PRINTTYPE_CODESOFT02 = 2;
    private static final String TAG = ShipmentPalletLabelPrintActivity.class.getSimpleName();
    private final String CUSTOMER_NO_MERAKI = "5633";
    private final String CUSTOMER_NO_CISCO = "226043";
    private final String CUSTOMER_NO_AURADINE = "206018";
    private final String CUSTOMER_NO_TEMBO = "11734";
    private final String CUSTOMER_NO_EXTREME = "8753";
    private final String CUSTOMER_NO_CAMBIUM = "27756";
    private final String CUSTOMER_NO_SOPHOS = "178840";// 178837 178838 178840 179895 178871

    private final String CUSTOMER_NO_SAMSARA = "34760";
    private final String CUSTOMER_NO_VERKADA = "176828";
    private final String[] CUSTOMER_NO_SOPHOS_SET = {"178837", "178838", "178840", "179895", "178871"};

    private TextView mConnection,lblTitle;
    private EditText txtImprotDnNo, txtImportPalletNo, txtImportPalletSize, txtImportQrCode, txtImportQrCodePalletSize;
    private ProgressDialog dialog;
    private String errorInfo = "",orgPrint="";
    private PrintLabelHandler print;
    private ShipmentPalletInfoHelper palletInfo;
    private ShipmentPalletSnInfoHelper palletSnInfo;
    private ShipmentPalletSnInfoHelperSophos palletSnInfoSophos;
    private NewPrintDataList PrintDataList;

    private ExtremePalletInfoHelper extremePalletInfo;
    private TscWifiActivity TscEthernetDll;
    private RadioButton rbQrCode, rbBarcode;
    private LinearLayout qrcodeLayout, barcodeLayout;
    private ChkDeliveryInfoHelper dnInfo;
    private ShippingVerifyMainHandler verifyHandler;

    private String smallTag = "";

    private RadioGroup.OnCheckedChangeListener rgListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio_qrcode:
                    if (rbQrCode.isChecked()) {
                        barcodeLayout.setVisibility(View.GONE);
                        qrcodeLayout.setVisibility(View.VISIBLE);
                        txtImportQrCode.requestFocus();
                    }

                    break;
                case R.id.radio_barcode:
                    if (rbBarcode.isChecked()) {
                        barcodeLayout.setVisibility(View.VISIBLE);
                        qrcodeLayout.setVisibility(View.GONE);
                        txtImprotDnNo.requestFocus();
                    }

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipment_pallet_label_print);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ShipmentPalletLabelPrintActivity.this);
                    dialog.setTitle("Error Msg");
                    dialog.setMessage(errorInfo);
                    dialog.setIcon(android.R.drawable.ic_dialog_alert);
                    dialog.setCancelable(false);

                    dialog.setNegativeButton( getString(R.string.btn_ok),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int arg1) {
                                    dialog.dismiss();
                                }

                            });

                    dialog.show();
                }
            }
        });

        RadioGroup rg = findViewById(R.id.radio_group);
        rbQrCode = findViewById(R.id.radio_qrcode);
        rbBarcode = findViewById(R.id.radio_barcode);
        rg.setOnCheckedChangeListener(rgListener);

        qrcodeLayout = findViewById(R.id.qrcodeLayout);
        txtImportQrCode = findViewById(R.id.edittext_import_qrcode);
        txtImportQrCode.setSelectAllOnFocus(true);

        lblTitle = findViewById(R.id.ap_title);
        setTitle();

        txtImportQrCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    hideKeyboard(v);
                    Log.d(TAG, "onKeyListener Enter or Action Down " + v.getId());
                    EditText et = (EditText) v;

                    if (TextUtils.isEmpty(et.getText())) {
                        Toast.makeText(ShipmentPalletLabelPrintActivity.this, getString(R.string.QR_CODE_is_not_null), Toast.LENGTH_SHORT).show();
                        return true;
                    } else {
                        txtImportQrCodePalletSize.requestFocus();
                    }
                }

                return false;
            }
        });

        txtImportQrCodePalletSize = findViewById(R.id.edittext_qrcode_pallet_size);
        txtImportQrCodePalletSize.setSelectAllOnFocus(true);

        barcodeLayout = findViewById(R.id.barcodeLayout);
        txtImprotDnNo = findViewById(R.id.edittext_dn_no);
        txtImprotDnNo.setSelectAllOnFocus(true);
        txtImprotDnNo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    //hideKeyboard(v);
                    Log.d(TAG, "onKeyListener Enter or Action Down" + v.getId());
                    String dn = txtImprotDnNo.getText().toString().trim();

                    if (TextUtils.isEmpty(dn)) {
                        Toast.makeText(ShipmentPalletLabelPrintActivity.this, getString(R.string.dn_num_is_not_null), Toast.LENGTH_SHORT).show();
                        return true;
                    } else {
                        String result = parseDn(dn);

                        if (TextUtils.isEmpty(result)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.cant_resolve_dn), Toast.LENGTH_LONG).show();
                            return true;
                        } else {
                            txtImprotDnNo.setText(result);
                        }
                    }
                }

                return false;
            }
        });

        txtImportPalletNo = findViewById(R.id.edittext_pallet_no);
        txtImportPalletNo.setSelectAllOnFocus(true);
        txtImportPalletSize = findViewById(R.id.edittext_pallet_size);
        txtImportPalletSize.setSelectAllOnFocus(true);

        Button btnReturn = findViewById(R.id.button_return);
        Button btnConfim = findViewById(R.id.button_confirm);
        Button btnCancel = findViewById(R.id.button_cancel);

        btnReturn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Return", String.valueOf(v.getId()));
                returnPage();
            }

        });

        btnConfim.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Confim", String.valueOf(v.getId()));
                //onCreatePrinterDialog().show();

                if (checkFields()) {
                    doQueryPalletInfo();
                }
            }

        });

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Cancel", String.valueOf(v.getId()));
                cleanData();
            }

        });

        rbQrCode.setChecked(true);
        checkPrinterSetting();
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.title_activity_shipment_pallet_label_print1, AppController.getOrgName()));
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private void hideKeyboard(View edit) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
    }

    private String parseDn(String dn) {
        String result = null;

        if (!TextUtils.isEmpty(dn)) {
            if (dn.contains("@")) {
                String[] list = dn.split("@");

                if (list.length > 1) {
                    result = list[1];
                }
            } else {
                result = dn;
            }
        }

        return result;
    }

    private void printLabel() {
        dialog = ProgressDialog.show(ShipmentPalletLabelPrintActivity.this,
                getString(R.string.holdon), getString(R.string.printingLabel), true);
        orgPrint = QrCodeUtil.getValueFromPalletLabelQrCode(txtImportQrCode.getText().toString(), PALLET_LABEL_QR_CODE_FORMAT.ORG);
        String qrCode = dnInfo.getInvoiceNo() + "@" + palletInfo.getDnNo() + "@" + palletInfo.getPalletNo() + "@" + palletInfo.getBoxQty();

        if(Constant.ISORG){
            qrCode = dnInfo.getInvoiceNo() + "@" + palletInfo.getDnNo() + "@" + palletInfo.getPalletNo() + "@" + palletInfo.getBoxQty() + "@" +orgPrint;//要帶入手WRcode的ORG
        }

        smallTag = qrCode;
//        if (!BtPrintLabel.printShipmentPalletLabel(palletInfo.getMark(),palletInfo.getShippingWay(), palletInfo.getDnNo(),palletInfo.getPalletNo(), palletInfo.getBoxQty(), qrCode)) {
        if (!BtPrintLabel.printShipmentPalletLabel1(palletInfo.getMark(),palletInfo.getShippingWay(), palletInfo.getDnNo(),palletInfo.getPalletNo(), palletInfo.getBoxQty(), qrCode,orgPrint)) {
            errorInfo = getString(R.string.printLabalFailed);
            mConnection.setText(getString(R.string.printer_connect_error));
            mConnection.setTextColor(Color.RED);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        dialog.dismiss();
    }

    private void getInvoiceNo(int dn) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.downloading_data), true);
        dnInfo = new ChkDeliveryInfoHelper();
        dnInfo.setDeliveryID(dn);
        verifyHandler = new ShippingVerifyMainHandler(dnInfo);
        new ChkDNInfo().execute(0);
    }

    private void getExtremeLabelInfo() {
        extremePalletInfo = new ExtremePalletInfoHelper();
        String dn = getDn();
        String palletNo = getPalletNo();
        extremePalletInfo.setDnNo(Integer.parseInt(dn));
        extremePalletInfo.setPalletNo(palletNo);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.processing), true);
        print = new PrintLabelHandler();
        new GetExtremePalletInfo().execute(0);
    }

    private boolean printExtremeLabel(String ip, String port, String qty, ExtremePalletItem extremePalletItem) {
        dialog = ProgressDialog.show(this,
                getString(R.string.holdon), getString(R.string.printingLabel), true);

        try {
            if (TscEthernetDll == null) {
                TscEthernetDll = new TscWifiActivity();
            }

            TscEthernetDll.openport(ip, Integer.parseInt(port));
            TscEthernetDll.setup(100, 150, 3, 10, 0, 3, 0);//(width,heigh,speed,density,sensor,sensor_distance,sensor_offset)
            TscEthernetDll.clearbuffer();//清除被設定過的畫面
            TscEthernetDll.sendcommand("SET TEAR ON\n");//開始作畫
            int x, y;
            x = mm2dot(5);
            y = mm2dot(5);
            TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, "Unique Pack ID : " + extremePalletItem.getUniquePackID());
            y = mm2dot(11);
            TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 3, 3, extremePalletItem.getUniquePackID());
            y = mm2dot(22);
            TscEthernetDll.sendcommand("BAR " + x + "," + y + ",1080,12\n");
            y = mm2dot(25);
            TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, "Trigger information : " + extremePalletItem.getTriggerInfo());
            y = mm2dot(31);
            TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 3, 3, extremePalletItem.getTriggerInfo());
            y = mm2dot(42);
            TscEthernetDll.sendcommand("BAR " + x + "," + y + ",1080,12\n");
            y = mm2dot(45);
            TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, "SKU or Part Number : " + extremePalletItem.getSkuPn());
            y = mm2dot(51);
            TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 3, 3, extremePalletItem.getSkuPn());
            y = mm2dot(62);
            TscEthernetDll.sendcommand("BAR " + x + "," + y + ",1080,12\n");
            y = mm2dot(65);
            TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, "Quantity : " + String.valueOf(extremePalletItem.getQty()));
            y = mm2dot(71);
            TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 3, 3, String.valueOf(extremePalletItem.getQty()));
            y = mm2dot(82);
            TscEthernetDll.sendcommand("BAR " + x + "," + y + ",1080,12\n");
            y = mm2dot(85);
            TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, "Supplier Code : " + extremePalletItem.getSupplierCode());
            y = mm2dot(91);
            TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 3, 3, extremePalletItem.getSupplierCode());
            y = mm2dot(102);
            TscEthernetDll.sendcommand("BAR " + x + "," + y + ",1080,12\n");
            y = mm2dot(105);
            TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, "Extreme Description : ");
            y = mm2dot(110);

            //標楷體 大小42
            if (extremePalletItem.getBrocadeDescription() == null) {
                Toast.makeText(getApplicationContext(), "Not find Extreme Description，please contact MIS", Toast.LENGTH_LONG).show();
                TscEthernetDll.closeport(5000);
                return false;
            }

            byte[] des = extremePalletItem.getBrocadeDescription().getBytes("Big5");

            TscEthernetDll.sendcommand("BLOCK " + x + "," + y + ",1080,210,\"FONT002\",0,1,1,20,0,\"");
            TscEthernetDll.sendcommand(des);
            TscEthernetDll.sendcommand("\"\n");

            y = mm2dot(130);
            TscEthernetDll.sendcommand("BAR " + x + "," + y + ",1080,12\n");
            y = mm2dot(133);
            TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, "Country of Origin : " + extremePalletItem.getCountryOfOrigin());
            y = mm2dot(139);
            TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 3, 3, extremePalletItem.getCountryOfOrigin());
            TscEthernetDll.printlabel(Integer.parseInt(qty), 1);
            TscEthernetDll.closeport(5000);
            return true;
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        } finally {
            dialog.dismiss();
        }
    }

    private boolean printSophosLabel(String ip, String port, ShipmentPalletSnInfoHelper palletSnInfoHelper, int count, String qrCode) {
        dialog = ProgressDialog.show(this,
                getString(R.string.holdon), getString(R.string.printingLabel), true);

        try {
            if (TscEthernetDll == null) {
                TscEthernetDll = new TscWifiActivity();
            }

            TscEthernetDll.openport(ip, Integer.parseInt(port));
            TscEthernetDll.setup(100, 72, 3, 10, 0, 3, 0);
            TscEthernetDll.clearbuffer();
            TscEthernetDll.sendcommand("SET TEAR ON\n");
            int x, y, x_end, y_end;
            x = mm2dot(8);
            y = mm2dot(5);

            TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "Invoice NO. " + palletSnInfoSophos.getInvoiceNo());

            y = mm2dot(15);
            TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "PN    " + palletSnInfoHelper.getCustomerPartNo());


            y = mm2dot(25);
            TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "Pallet No.    " + palletSnInfoHelper.getPalletNo());


            y = mm2dot(35);
            TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "Serial No.");

            //小標籤
            y = mm2dot(50);
            TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, palletSnInfoSophos.getBoxQty() + "CTN");
            y = mm2dot(55);
            TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", smallTag);
            y = mm2dot(68);
            TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, palletSnInfoSophos.getDnNo() + "");


            x = mm2dot(65);
            y = mm2dot(32);
            TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "QTY: " + count);

            //印QRcode1
            x = mm2dot(73);
            y = mm2dot(5);
            TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", palletSnInfoSophos.getInvoiceNo());

            //印QRcode2
            y = mm2dot(15);
            TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", palletSnInfoHelper.getCustomerPartNo());

            //印QRcode3
            x = mm2dot(55);
            y = mm2dot(39);
            TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", qrCode);

            TscEthernetDll.printlabel(1, 1);
            TscEthernetDll.closeport(5000);

//            //TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial30\",0,1,1,\""  + palletSnInfoHelper.getCustomerPartNoDescription() + "\"\n");
//            TscEthernetDll.sendcommand("BLOCK " + x + "," + y + ",1128,132,\"Arial36\",0,1,1,\"" + palletSnInfoHelper.getCustomerPartNoDescription() + "\"\n");
//            //TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, palletSnInfoHelper.getCustomerPartNoDescription());
//            x = mm2dot(4);
//            y = mm2dot(17);
//            TscEthernetDll.barcode(x, y, "39", 120, 0, 0, 3, 6, palletSnInfoHelper.getRemark());
//            y = mm2dot(28);
//            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial42\",0,1,1,\"SKU " + palletSnInfoHelper.getRemark() + "\"\n");
//            //TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "SKU " + palletSnInfoHelper.getRemark());
//            y = mm2dot(37);
//            TscEthernetDll.barcode(x, y, "39", 120, 0, 0, 3, 6, palletSnInfoHelper.getCustomerPartNo());
//            y = mm2dot(48);
//            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial42\",0,1,1,\"P/N " + palletSnInfoHelper.getCustomerPartNo() + "\"\n");
//            //TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "P/N " + palletSnInfoHelper.getCustomerPartNo());
//            y = mm2dot(57);
//            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial56\",0,1,1,\"Quantity-" + count + "\"\n");
//            //TscEthernetDll.printerfont(x, y, "4", 0, 2, 2, "Quantity-" + count);
//            y = mm2dot(66);
//            TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", qrCode);
//            x = mm2dot(58);
//            y = mm2dot(32);
//            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial56\",0,1,1,\"coo Taiwan\"\n");
//            //TscEthernetDll.printerfont(x, y, "4", 0, 2, 2, "coo Taiwan");
//            x = mm2dot(58);
//            y = mm2dot(42);
//            x_end = mm2dot(94);
//            y_end = mm2dot(63);
//            TscEthernetDll.sendcommand("BOX " + x + "," + y + "," + x_end + "," + y_end + ",4\n");
//            TscEthernetDll.printlabel(1, 1);
//            TscEthernetDll.closeport(5000);
            return false;
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            return true;
        } finally {
            dialog.dismiss();
        }
    }

    private boolean printSophosLabel(String ip, String port, Pdf417Info_SOPHOS palletSnInfoHelper, int qty, String qrCode, int page, int allpage) {
        dialog = ProgressDialog.show(this,
                getString(R.string.holdon), getString(R.string.printingLabel), true);

        try {
            if (TscEthernetDll == null) {
                TscEthernetDll = new TscWifiActivity();
            }

            TscEthernetDll.openport(ip, Integer.parseInt(port));
//            TscEthernetDll.setup(100, 72, 3, 10, 0, 3, 0);
            TscEthernetDll.setup(100, 76, 3, 10, 0, 3, 0);
            TscEthernetDll.clearbuffer();
            TscEthernetDll.sendcommand("SET TEAR ON\n");
            int x, y, x_end, y_end;

            //boolean isPak = qrCode.indexOf("NA")==0;
            //boolean isPak1 = qrCode.indexOf("No Numbers")==0;

//"No Numbers".equals(qrCode)

            boolean isPak ="No Numbers".equals(qrCode) &&  palletSnInfoHelper.getPartNo().trim().indexOf("0")==0  ;

            x = mm2dot(8);
            y = mm2dot(5);
            TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "Invoice NO." + palletSnInfoSophos.getInvoiceNo());

            y = mm2dot(15);
            TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "PN    " + palletSnInfoHelper.getCustomerPartNo());

            y = mm2dot(25);
            TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "Pallet No.    " + palletSnInfoHelper.getPalletNo());

            y = mm2dot(35);

            if(AppController.getProperties("Sophos_PN_No_SN").indexOf(palletSnInfoHelper.getPartNo()) >= 0){

            }else{
                TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "Serial No.");
            }
            //y = mm2dot(35); //20250321 Ann(SENAO101-202502-0034):因Sophos要求部分料號不要顯示SERIAL NO.字樣
            //TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "Serial No."); //20250321 Ann(SENAO101-202502-0034):因Sophos要求部分料號不要顯示SERIAL NO.字樣

            y = mm2dot(43);
            TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "Page " + page + "/" + allpage);

            //小標籤
            //barcode(int x, int y, String type, int height, int human_readable, int rotation, int narrow, int wide, String string)
            //x 水平坐標左上角起點 y垂直坐標左上角起點 type條形碼類型 height條形碼高度 human_readable(0 人眼不可識別 1人眼可識別) rotation條形碼旋轉角度
            //narrow 窄bar寬度 wide寬bar寬度 string為要顯示的內容
            y = mm2dot(50);
            TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, palletSnInfoSophos.getBoxQty() + "CTN");
            y = mm2dot(55);
            TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", smallTag);
            y = mm2dot(67);
            TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, palletSnInfoSophos.getDnNo() + "");

//            TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2");

            x = mm2dot(65);
            y = mm2dot(27);

            if(isPak){//如果是配件包，直接取後台算的數量
                TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, "QTY: " + palletSnInfoHelper.getQty());
            }else{
                TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, "QTY: " + qty);
            }

            //印QRcode1
            x = mm2dot(73);
            y = mm2dot(5);
            TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", palletSnInfoSophos.getInvoiceNo());

            //印QRcode2
            y = mm2dot(15);
            TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", palletSnInfoHelper.getCustomerPartNo());

            //印QRcode3
            x = mm2dot(55);
            y = mm2dot(31);

            if(!isPak) {
                TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", qrCode);
            }

            TscEthernetDll.printlabel(1, 1);
            TscEthernetDll.closeport(5000);

//            //TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial30\",0,1,1,\""  + palletSnInfoHelper.getCustomerPartNoDescription() + "\"\n");
//            TscEthernetDll.sendcommand("BLOCK " + x + "," + y + ",1128,132,\"Arial36\",0,1,1,\"" + palletSnInfoHelper.getCustomerPartNoDescription() + "\"\n");
//            //TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, palletSnInfoHelper.getCustomerPartNoDescription());
//            x = mm2dot(4);
//            y = mm2dot(17);
//            TscEthernetDll.barcode(x, y, "39", 120, 0, 0, 3, 6, palletSnInfoHelper.getRemark());
//            y = mm2dot(28);
//            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial42\",0,1,1,\"SKU " + palletSnInfoHelper.getRemark() + "\"\n");
//            //TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "SKU " + palletSnInfoHelper.getRemark());
//            y = mm2dot(37);
//            TscEthernetDll.barcode(x, y, "39", 120, 0, 0, 3, 6, palletSnInfoHelper.getCustomerPartNo());
//            y = mm2dot(48);
//            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial42\",0,1,1,\"P/N " + palletSnInfoHelper.getCustomerPartNo() + "\"\n");
//            //TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "P/N " + palletSnInfoHelper.getCustomerPartNo());
//            y = mm2dot(57);
//            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial56\",0,1,1,\"Quantity-" + count + "\"\n");
//            //TscEthernetDll.printerfont(x, y, "4", 0, 2, 2, "Quantity-" + count);
//            y = mm2dot(66);
//            TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", qrCode);
//            x = mm2dot(58);
//            y = mm2dot(32);
//            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial56\",0,1,1,\"coo Taiwan\"\n");
//            //TscEthernetDll.printerfont(x, y, "4", 0, 2, 2, "coo Taiwan");
//            x = mm2dot(58);
//            y = mm2dot(42);
//            x_end = mm2dot(94);
//            y_end = mm2dot(63);
//            TscEthernetDll.sendcommand("BOX " + x + "," + y + "," + x_end + "," + y_end + ",4\n");
//            TscEthernetDll.printlabel(1, 1);
//            TscEthernetDll.closeport(5000);
            return false;
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            return true;
        } finally {
            dialog.dismiss();
        }
    }

    private boolean printMerakiLabel(String ip, String port, Pdf417Info_SOPHOS palletSnInfoHelper, int qty, String qrCode, int page, int allpage,int type) {
        dialog = ProgressDialog.show(this,
                getString(R.string.holdon), getString(R.string.printingLabel), true);

        try {
            if (TscEthernetDll == null) {
                TscEthernetDll = new TscWifiActivity();
            }

            TscEthernetDll.openport(ip, Integer.parseInt(port));
//            TscEthernetDll.setup(100, 72, 3, 10, 0, 3, 0);
            TscEthernetDll.setup(100, 76, 3, 10, 0, 3, 0);
            TscEthernetDll.clearbuffer();
            TscEthernetDll.sendcommand("SET TEAR ON\n");
            int x, y, x_end, y_end;

//            if(page>1){
//                return true;
//            }

            x = mm2dot(10);
            y = mm2dot(5);
            String st1 = "Part Number:" + palletSnInfoHelper.getCustomerPartNo();
//            String st1 = "Part Number:" + "MA-RCKMNT-KIT-1";
            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,1,1,\"" + st1 + "\"\n");

            x = mm2dot(37);
            y = mm2dot(13);
            TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 3, 5,  palletSnInfoHelper.getCustomerPartNo());

            x = mm2dot(10);
            y = mm2dot(18);
            TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "QTY:"+qty);

            y = mm2dot(27);
//            TscEthernetDll.sendcommand("PDF417 "+ "50,20,400,200,0,E3,\""+qrCode+"\"\n");//特殊QRcode
            TscEthernetDll.sendcommand("PDF417 "+x+","+y+","+"700,300,0,E4,\""+qrCode+"\"\n");//特殊QRcode

            st1 = "Pallet Number:" + palletSnInfoHelper.getPalletNo();
            x = mm2dot(10);
            y = mm2dot(66);
            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,1,1,\"" + st1 + "\"\n");

            x = mm2dot(85);
            y = mm2dot(60);
            TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, (page) + "/" + allpage);

            TscEthernetDll.printlabel(1, 1);
            TscEthernetDll.closeport(5000);

//            TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 5, 5,  palletSnInfoHelper.getCustomerPartNoDescription());

//                y = mm2dot(42);
//                if(palletSnInfoHelper.getSnList()!=null) {
//                    TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "Qty:" + palletSnInfoHelper.getSnList().size());
//                }

//                y = mm2dot(50);
//                TscEthernetDll.sendcommand("PDF417 "+ "50,50,600,600,0,E3,\""+qrCode+"\"\n");

            //小標籤
            //barcode(int x, int y, String type, int height, int human_readable, int rotation, int narrow, int wide, String string)
            //x 水平坐標左上角起點 y垂直坐標左上角起點 type條形碼類型 height條形碼高度 human_readable(0 人眼不可識別 1人眼可識別) rotation條形碼旋轉角度
            //narrow 窄bar寬度 wide寬bar寬度 string為要顯示的內容
//                y = mm2dot(50);
//                TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, palletSnInfoSophos.getBoxQty() + "CTN");

//            TscEthernetDll.sendcommand("PDF417 "+ "50,50,600,600,0,E3,\""+st+"\"\n");

//            //TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial30\",0,1,1,\""  + palletSnInfoHelper.getCustomerPartNoDescription() + "\"\n");
//            TscEthernetDll.sendcommand("BLOCK " + x + "," + y + ",1128,132,\"Arial36\",0,1,1,\"" + palletSnInfoHelper.getCustomerPartNoDescription() + "\"\n");
//            //TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, palletSnInfoHelper.getCustomerPartNoDescription());
//            x = mm2dot(4);
//            y = mm2dot(17);
//            TscEthernetDll.barcode(x, y, "39", 120, 0, 0, 3, 6, palletSnInfoHelper.getRemark());
//            y = mm2dot(28);
//            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial42\",0,1,1,\"SKU " + palletSnInfoHelper.getRemark() + "\"\n");
//            //TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "SKU " + palletSnInfoHelper.getRemark());
//            y = mm2dot(37);
//            TscEthernetDll.barcode(x, y, "39", 120, 0, 0, 3, 6, palletSnInfoHelper.getCustomerPartNo());
//            y = mm2dot(48);
//            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial42\",0,1,1,\"P/N " + palletSnInfoHelper.getCustomerPartNo() + "\"\n");
//            //TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "P/N " + palletSnInfoHelper.getCustomerPartNo());
//            y = mm2dot(57);
//            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial56\",0,1,1,\"Quantity-" + count + "\"\n");
//            //TscEthernetDll.printerfont(x, y, "4", 0, 2, 2, "Quantity-" + count);
//            y = mm2dot(66);
//            TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", qrCode);
//            x = mm2dot(58);
//            y = mm2dot(32);
//            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial56\",0,1,1,\"coo Taiwan\"\n");
//            //TscEthernetDll.printerfont(x, y, "4", 0, 2, 2, "coo Taiwan");
//            x = mm2dot(58);
//            y = mm2dot(42);
//            x_end = mm2dot(94);
//            y_end = mm2dot(63);
//            TscEthernetDll.sendcommand("BOX " + x + "," + y + "," + x_end + "," + y_end + ",4\n");
//            TscEthernetDll.printlabel(1, 1);
//            TscEthernetDll.closeport(5000);
            return false;
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            return true;
        } finally {
            dialog.dismiss();
        }
    }

    private boolean printSamsaraLabel(String ip, String port, Pdf417Info_SOPHOS palletSnInfoHelper, int qty, String qrCode, int page, int allpage,int type) {
        dialog = ProgressDialog.show(this,
                getString(R.string.holdon), getString(R.string.printingLabel), true);

        try {
            if(page==1){
                if (TscEthernetDll == null) {
                    TscEthernetDll = new TscWifiActivity();
                }

                TscEthernetDll.openport(ip, Integer.parseInt(port));
                TscEthernetDll.setup(100, 72, 3, 10, 0, 3, 0);
                TscEthernetDll.clearbuffer();
                TscEthernetDll.sendcommand("SET TEAR ON\n");
                int x, y, x_end, y_end;

                x = mm2dot(8);
                String st1 = "Pallet ID:" + palletSnInfoHelper.getPalletNo();

                y = mm2dot(24);
                TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,2,2,\"" + st1 + "\"\n");

                y = mm2dot(34);
                TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 5, 5,  palletSnInfoHelper.getPalletNo());

                TscEthernetDll.printlabel(1, 1);
                TscEthernetDll.closeport(5000);
            }

            if(false){//page==1
                if (TscEthernetDll == null) {
                    TscEthernetDll = new TscWifiActivity();
                }

                TscEthernetDll.openport(ip, Integer.parseInt(port));
                TscEthernetDll.setup(100, 72, 3, 10, 0, 3, 0);
                TscEthernetDll.clearbuffer();
                TscEthernetDll.sendcommand("SET TEAR ON\n");
                int x, y, x_end, y_end;

                x = mm2dot(8);
                y = mm2dot(5);
                TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "Samsara");

                y = mm2dot(10);
                TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "Product:" + palletSnInfoHelper.getCustomerPartNoDescription());

                y = mm2dot(17);
                TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 5, 5,  palletSnInfoHelper.getCustomerPartNoDescription());

                String st1 = "Pallet ID:" + palletSnInfoHelper.getPalletNo();

                y = mm2dot(27);
                TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,1,1,\"" + st1 + "\"\n");

                y = mm2dot(32);
                TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 5, 5,  palletSnInfoHelper.getPalletNo());

//                y = mm2dot(42);
//                if(palletSnInfoHelper.getSnList()!=null) {
//                    TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "Qty:" + palletSnInfoHelper.getSnList().size());
//                }

//                y = mm2dot(50);
//                TscEthernetDll.sendcommand("PDF417 "+ "50,50,600,600,0,E3,\""+qrCode+"\"\n");

                //小標籤
                //barcode(int x, int y, String type, int height, int human_readable, int rotation, int narrow, int wide, String string)
                //x 水平坐標左上角起點 y垂直坐標左上角起點 type條形碼類型 height條形碼高度 human_readable(0 人眼不可識別 1人眼可識別) rotation條形碼旋轉角度
                //narrow 窄bar寬度 wide寬bar寬度 string為要顯示的內容
//                y = mm2dot(50);
//                TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, palletSnInfoSophos.getBoxQty() + "CTN");

                TscEthernetDll.printlabel(1, 1);

                TscEthernetDll.clearbuffer();
                TscEthernetDll.sendcommand("SET TEAR ON\n");

                TscEthernetDll.sendcommand("PDF417 "+ "50,20,600,600,0,E3,\""+qrCode+"\"\n");

                x = mm2dot(70);
                y = mm2dot(30);
                TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, "Page " + (page) + "/" + allpage+"-"+palletSnInfoHelper.getPalletNo());

                TscEthernetDll.printlabel(1, 1);
                TscEthernetDll.closeport(5000);
            }else if(false){//if(page==2)
                if (TscEthernetDll == null) {
                    TscEthernetDll = new TscWifiActivity();
                }

                TscEthernetDll.openport(ip, Integer.parseInt(port));
                TscEthernetDll.setup(100, 72, 3, 10, 0, 3, 0);
                TscEthernetDll.clearbuffer();
                TscEthernetDll.sendcommand("SET TEAR ON\n");
                int x, y, x_end, y_end;

                TscEthernetDll.sendcommand("PDF417 "+ "50,20,600,600,0,E3,\""+qrCode+"\"\n");

                x = mm2dot(70);
                y = mm2dot(30);
                TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, "Page " + (page) + "/" + allpage+"-"+palletSnInfoHelper.getPalletNo());

                TscEthernetDll.printlabel(1, 1);
                TscEthernetDll.closeport(5000);
            }

//            TscEthernetDll.sendcommand("PDF417 "+ "50,50,600,600,0,E3,\""+st+"\"\n");

//            //TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial30\",0,1,1,\""  + palletSnInfoHelper.getCustomerPartNoDescription() + "\"\n");
//            TscEthernetDll.sendcommand("BLOCK " + x + "," + y + ",1128,132,\"Arial36\",0,1,1,\"" + palletSnInfoHelper.getCustomerPartNoDescription() + "\"\n");
//            //TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, palletSnInfoHelper.getCustomerPartNoDescription());
//            x = mm2dot(4);
//            y = mm2dot(17);
//            TscEthernetDll.barcode(x, y, "39", 120, 0, 0, 3, 6, palletSnInfoHelper.getRemark());
//            y = mm2dot(28);
//            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial42\",0,1,1,\"SKU " + palletSnInfoHelper.getRemark() + "\"\n");
//            //TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "SKU " + palletSnInfoHelper.getRemark());
//            y = mm2dot(37);
//            TscEthernetDll.barcode(x, y, "39", 120, 0, 0, 3, 6, palletSnInfoHelper.getCustomerPartNo());
//            y = mm2dot(48);
//            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial42\",0,1,1,\"P/N " + palletSnInfoHelper.getCustomerPartNo() + "\"\n");
//            //TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "P/N " + palletSnInfoHelper.getCustomerPartNo());
//            y = mm2dot(57);
//            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial56\",0,1,1,\"Quantity-" + count + "\"\n");
//            //TscEthernetDll.printerfont(x, y, "4", 0, 2, 2, "Quantity-" + count);
//            y = mm2dot(66);
//            TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", qrCode);
//            x = mm2dot(58);
//            y = mm2dot(32);
//            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial56\",0,1,1,\"coo Taiwan\"\n");
//            //TscEthernetDll.printerfont(x, y, "4", 0, 2, 2, "coo Taiwan");
//            x = mm2dot(58);
//            y = mm2dot(42);
//            x_end = mm2dot(94);
//            y_end = mm2dot(63);
//            TscEthernetDll.sendcommand("BOX " + x + "," + y + "," + x_end + "," + y_end + ",4\n");
//            TscEthernetDll.printlabel(1, 1);
//            TscEthernetDll.closeport(5000);
            return false;
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            return true;
        } finally {
            dialog.dismiss();
        }
    }

    private boolean printVerkadaLotcodeLabel(String ip, String port, int sum,String st1,String st2,Set<String> lotSet) {
        dialog = ProgressDialog.show(this,
                getString(R.string.holdon), getString(R.string.printingLabel), true);

        try {
            for(String lotCode : lotSet){
                AppController.debug("Lotcode ===>:" + lotCode);
            }

            if(true){//sum==1
                if (TscEthernetDll == null) {
                    TscEthernetDll = new TscWifiActivity();
                }

                TscEthernetDll.openport(ip, Integer.parseInt(port));
                TscEthernetDll.setup(100, 76, 3, 10, 0, 3, 0);
                TscEthernetDll.clearbuffer();
                TscEthernetDll.sendcommand("SET TEAR ON\n");
                int x, y, x_end, y_end;

                x = mm2dot(8);
                y = mm2dot(5);
                TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,3,3,\"" + st1 + "\"\n");

                x = mm2dot(8);
                y = mm2dot(35);
                TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,3,3,\"" + st2 + "\"\n");

                TscEthernetDll.printlabel(1, 1);

                TscEthernetDll.clearbuffer();
                TscEthernetDll.sendcommand("SET TEAR ON\n");

                //小標籤
                //barcode(int x, int y, String type, int height, int human_readable, int rotation, int narrow, int wide, String string)
                //x 水平坐標左上角起點 y垂直坐標左上角起點 type條形碼類型 height條形碼高度 human_readable(0 人眼不可識別 1人眼可識別) rotation條形碼旋轉角度
                //narrow 窄bar寬度 wide寬bar寬度 string為要顯示的內容
//                y = mm2dot(50);
//                TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, palletSnInfoSophos.getBoxQty() + "CTN");

                TscEthernetDll.closeport(5000);
            }

//            TscEthernetDll.sendcommand("PDF417 "+ "50,50,600,600,0,E3,\""+st+"\"\n");
            return false;
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            return true;
        } finally {
            dialog.dismiss();
        }
    }

    private boolean printVerkadaLabel(String ip, String port, Pdf417Info_SOPHOS palletSnInfoHelper, int size, String qrCode, int page, int allpage,Set<String> lotSet) {
        dialog = ProgressDialog.show(this,
                getString(R.string.holdon), getString(R.string.printingLabel), true);

        try {
            for(String lotCode : lotSet){
                AppController.debug("Lotcode ===>:" + lotCode);
            }

            if(page==1){//page%8==1
                if (TscEthernetDll == null) {
                    TscEthernetDll = new TscWifiActivity();
                }

                TscEthernetDll.openport(ip, Integer.parseInt(port));
                TscEthernetDll.setup(100, 76, 3, 10, 0, 3, 0);
                TscEthernetDll.clearbuffer();
                TscEthernetDll.sendcommand("SET TEAR ON\n");
                int x, y, x_end, y_end;

//                int lotSum = lotSet.size()/2;
//                for(String lotCode : lotSet){
//                    AppController.debug("Lotcode ===>:" + lotCode);
//                }

//                x = mm2dot(8);
//                y = mm2dot(5);
//                TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,3,3,\"" + "V1234567F9" + "\"\n");

//                x = mm2dot(8);
//                y = mm2dot(35);
//                TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,3,3,\"" + "V1234567F9" + "\"\n");

//                TscEthernetDll.printlabel(1, 1);

//                TscEthernetDll.clearbuffer();
//                TscEthernetDll.sendcommand("SET TEAR ON\n");

                x = mm2dot(8);
                y = mm2dot(5);
//                TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "Samsara");

//                y = mm2dot(10);
                TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "MODEL:" + palletSnInfoHelper.getCustomerPartNoDescription());

//                y = mm2dot(17);
                y = mm2dot(12);
                TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 5, 5,  palletSnInfoHelper.getCustomerPartNoDescription());

//                y = mm2dot(27);
                y = mm2dot(22);
                TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "P/N:" + palletSnInfoHelper.getCustomerPartNo());
//                TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "P/N:1A-38001-D");

//                y = mm2dot(32);
                y = mm2dot(27);
                TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 5, 5,  palletSnInfoHelper.getCustomerPartNo());
//                TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 5, 5,  "1A-38001-D");

                y = mm2dot(37);
                if(palletSnInfoHelper.getSnList()!=null) {
                    TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "Qty:" + palletSnInfoHelper.getSnList().size());
                }

                y = mm2dot(47);
                TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "S/N:");

//                TscEthernetDll.sendcommand("PDF417 "+ "50,50,600,600,0,E3,\""+qrCode+"\"\n");

                //小標籤
                //barcode(int x, int y, String type, int height, int human_readable, int rotation, int narrow, int wide, String string)
                //x 水平坐標左上角起點 y垂直坐標左上角起點 type條形碼類型 height條形碼高度 human_readable(0 人眼不可識別 1人眼可識別) rotation條形碼旋轉角度
                //narrow 窄bar寬度 wide寬bar寬度 string為要顯示的內容
//                y = mm2dot(50);
//                TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, palletSnInfoSophos.getBoxQty() + "CTN");

                TscEthernetDll.printlabel(1, 1);

                TscEthernetDll.clearbuffer();
                TscEthernetDll.sendcommand("SET TEAR ON\n");

                TscEthernetDll.sendcommand("PDF417 "+ "50,20,600,600,0,E3,\""+qrCode+"\"\n");

                x = mm2dot(70);
                y = mm2dot(30);
                TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, "Page " + (page) + "/" + allpage+"-"+palletSnInfoHelper.getPalletNo());

//                x = mm2dot(60);
                y = mm2dot(25);
                TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, "1 - "+size);

                TscEthernetDll.printlabel(1, 1);
                TscEthernetDll.closeport(5000);
            }else {//if(page==2)  if(page==20)
                if (TscEthernetDll == null) {
                    TscEthernetDll = new TscWifiActivity();
                }

                TscEthernetDll.openport(ip, Integer.parseInt(port));
                TscEthernetDll.setup(100, 76, 3, 10, 0, 3, 0);
                TscEthernetDll.clearbuffer();
                TscEthernetDll.sendcommand("SET TEAR ON\n");
                int x, y, x_end, y_end;

                TscEthernetDll.sendcommand("PDF417 "+ "50,20,600,600,0,E3,\""+qrCode+"\"\n");//特殊QRcode

                x = mm2dot(70);
                y = mm2dot(30);
                TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, "Page " + (page) + "/" + allpage+"-"+palletSnInfoHelper.getPalletNo());
//                x = mm2dot(60);
                y = mm2dot(25);
                int sSnNum=((page-1)*72+1);
                TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, sSnNum+" - "+(sSnNum+size-1));

                //小標籤
                //barcode(int x, int y, String type, int height, int human_readable, int rotation, int narrow, int wide, String string)
                //x 水平坐標左上角起點 y垂直坐標左上角起點 type條形碼類型 height條形碼高度 human_readable(0 人眼不可識別 1人眼可識別) rotation條形碼旋轉角度
                //narrow 窄bar寬度 wide寬bar寬度 string為要顯示的內容
//                y = mm2dot(50);
//                TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, palletSnInfoSophos.getBoxQty() + "CTN");
//                y = mm2dot(55);
//                TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", smallTag);
//                y = mm2dot(67);
//                TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, palletSnInfoSophos.getDnNo() + "");

//            TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2");

//                x = mm2dot(65);
//                y = mm2dot(27);
//
//                TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, "QTY: " + palletSnInfoHelper.getQty());

                TscEthernetDll.printlabel(1, 1);
                TscEthernetDll.closeport(5000);
            }

//            TscEthernetDll.sendcommand("PDF417 "+ "50,50,600,600,0,E3,\""+st+"\"\n");
            return false;
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            return true;
        } finally {
            dialog.dismiss();
        }
    }

    private boolean printSophosLabel_bk(String ip, String port, ShipmentPalletSnInfoHelper palletSnInfoHelper, int count, String qrCode) {
        dialog = ProgressDialog.show(this,
                getString(R.string.holdon), getString(R.string.printingLabel), true);

        try {
            if (TscEthernetDll == null) {
                TscEthernetDll = new TscWifiActivity();
            }

            TscEthernetDll.openport(ip, Integer.parseInt(port));
            TscEthernetDll.setup(100, 150, 3, 10, 0, 3, 0);
            TscEthernetDll.clearbuffer();
            TscEthernetDll.sendcommand("SET TEAR ON\n");
            int x, y, x_end, y_end;
            x = mm2dot(2);
            y = mm2dot(3);
            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"ARIAL13\",0,1,1,\"" + palletSnInfoHelper.getCustomerPartNoDescription() + "\"\n");
            //TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, palletSnInfoHelper.getCustomerPartNoDescription());
            x = mm2dot(4);
            y = mm2dot(12);
            TscEthernetDll.barcode(x, y, "128", 120, 0, 0, 3, 3, palletSnInfoHelper.getRemark());
            y = mm2dot(23);
            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"ARIAL16\",0,1,1,\"SKU " + palletSnInfoHelper.getRemark() + "\"\n");
            //TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "SKU " + palletSnInfoHelper.getRemark());
            y = mm2dot(32);
            TscEthernetDll.barcode(x, y, "128", 120, 0, 0, 3, 3, palletSnInfoHelper.getCustomerPartNo());
            y = mm2dot(43);
            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"ARIAL16\",0,1,1,\"P/N " + palletSnInfoHelper.getCustomerPartNo() + "\"\n");
            //TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "P/N " + palletSnInfoHelper.getCustomerPartNo());
            y = mm2dot(52);
            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"ARIAL20\",0,1,1,\"Quantity-" + count + "\"\n");
            //TscEthernetDll.printerfont(x, y, "4", 0, 2, 2, "Quantity-" + count);
            y = mm2dot(61);
            TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", qrCode);
            x = mm2dot(55);
            y = mm2dot(27);
            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"ARIAL20\",0,1,1,\"coo Taiwan\"\n");
            //TscEthernetDll.printerfont(x, y, "4", 0, 2, 2, "coo Taiwan");
            x = mm2dot(59);
            y = mm2dot(37);
            x_end = mm2dot(94);
            y_end = mm2dot(58);
            TscEthernetDll.sendcommand("BOX " + x + "," + y + "," + x_end + "," + y_end + ",4\n");
            TscEthernetDll.printlabel(1, 1);
            TscEthernetDll.closeport(5000);
            return false;
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            return true;
        } finally {
            dialog.dismiss();
        }
    }

    private boolean printExtremeLabelOrig(String ip, String port, String qty, ExtremePalletItem extremePalletItem) {
        dialog = ProgressDialog.show(this,
                getString(R.string.holdon), getString(R.string.printingLabel), true);

        try {
            if (TscEthernetDll == null) {
                TscEthernetDll = new TscWifiActivity();
            }

            TscEthernetDll.openport(ip, Integer.parseInt(port));
            TscEthernetDll.setup(100, 150, 3, 10, 0, 3, 0);
            TscEthernetDll.clearbuffer();
            TscEthernetDll.sendcommand("SET TEAR ON\n");
            int x, y;
            x = mm2dot(5);
            y = mm2dot(5);
            TscEthernetDll.printerfont(x, y, "2", 0, 2, 2, "Unique Pack ID : " + extremePalletItem.getUniquePackID());
            y = mm2dot(11);
            TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 5, 5, extremePalletItem.getUniquePackID());
            y = mm2dot(22);
            TscEthernetDll.sendcommand("BAR " + x + "," + y + ",1080,12\n");
            y = mm2dot(25);
            TscEthernetDll.printerfont(x, y, "2", 0, 2, 2, "Trigger information : " + extremePalletItem.getTriggerInfo());
            y = mm2dot(31);
            TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 5, 5, extremePalletItem.getTriggerInfo());
            y = mm2dot(42);
            TscEthernetDll.sendcommand("BAR " + x + "," + y + ",1080,12\n");
            y = mm2dot(45);
            TscEthernetDll.printerfont(x, y, "2", 0, 2, 2, "SKU or Part Number : " + extremePalletItem.getSkuPn());
            y = mm2dot(51);
            TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 5, 5, extremePalletItem.getSkuPn());
            y = mm2dot(62);
            TscEthernetDll.sendcommand("BAR " + x + "," + y + ",1080,12\n");
            y = mm2dot(65);
            TscEthernetDll.printerfont(x, y, "2", 0, 2, 2, "Quantity : " + String.valueOf(extremePalletItem.getQty()));
            y = mm2dot(71);
            TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 5, 5, String.valueOf(extremePalletItem.getQty()));
            y = mm2dot(82);
            TscEthernetDll.sendcommand("BAR " + x + "," + y + ",1080,12\n");
            y = mm2dot(85);
            TscEthernetDll.printerfont(x, y, "2", 0, 2, 2, "Supplier Code : " + extremePalletItem.getSupplierCode());
            y = mm2dot(91);
            TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 5, 5, extremePalletItem.getSupplierCode());
            y = mm2dot(102);
            TscEthernetDll.sendcommand("BAR " + x + "," + y + ",1080,12\n");
            y = mm2dot(105);
            TscEthernetDll.printerfont(x, y, "2", 0, 2, 2, "Extreme Description : ");
            y = mm2dot(110);

            //標楷體 大小42
            if (extremePalletItem.getBrocadeDescription() == null) {
                Toast.makeText(getApplicationContext(), "Not find Extreme Description，please contact MIS", Toast.LENGTH_LONG).show();
                TscEthernetDll.closeport(5000);
                return false;
            }

            byte[] des = extremePalletItem.getBrocadeDescription().getBytes("Big5");

            TscEthernetDll.sendcommand("BLOCK " + x + "," + y + ",1080,210,\"FONT002\",0,1,1,20,0,\"");
            TscEthernetDll.sendcommand(des);
            TscEthernetDll.sendcommand("\"\n");

            y = mm2dot(130);
            TscEthernetDll.sendcommand("BAR " + x + "," + y + ",1080,12\n");
            y = mm2dot(133);
            TscEthernetDll.printerfont(x, y, "2", 0, 2, 2, "Country of Origin : " + extremePalletItem.getCountryOfOrigin());
            y = mm2dot(139);
            TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 5, 5, extremePalletItem.getCountryOfOrigin());
            TscEthernetDll.printlabel(Integer.parseInt(qty), 1);
            TscEthernetDll.closeport(5000);
            return true;
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        } finally {
            dialog.dismiss();
        }
    }

    private String getDn() {
        if (rbBarcode.isChecked()) {
            return txtImprotDnNo.getText().toString().trim();
        } else {
            return QrCodeUtil.getValueFromPalletLabelQrCode(txtImportQrCode.getText().toString(), PALLET_LABEL_QR_CODE_FORMAT.DN);
        }
    }

    private String getPalletSize() {
        if (rbBarcode.isChecked()) {
            return txtImportPalletSize.getText().toString().trim();
        } else {
            return txtImportQrCodePalletSize.getText().toString().trim();
        }
    }

    private String getPalletNoStartWithDn() {
        if (rbBarcode.isChecked()) {
            return txtImportPalletNo.getText().toString().trim();
        } else {
            return QrCodeUtil.getValueFromPalletLabelQrCode(txtImportQrCode.getText().toString(), PALLET_LABEL_QR_CODE_FORMAT.DN)
                    + QrCodeUtil.getValueFromPalletLabelQrCode(txtImportQrCode.getText().toString(), PALLET_LABEL_QR_CODE_FORMAT.P_NO);
        }
    }

    private String getPalletNo() {
        if (rbBarcode.isChecked()) {
            return txtImportPalletNo.getText().toString().trim().substring(getDn().length());
        } else {
            return QrCodeUtil.getValueFromPalletLabelQrCode(txtImportQrCode.getText().toString(), PALLET_LABEL_QR_CODE_FORMAT.P_NO);
        }
    }

    private boolean checkFields() {
        String dn = getDn();
        String palletNoStartWithDn = getPalletNoStartWithDn();

        if (rbBarcode.isChecked()) {
            if (TextUtils.isEmpty(dn)) {
                txtImprotDnNo.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_dn), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(palletNoStartWithDn)) {
                txtImportPalletNo.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_pallet_nu), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (palletNoStartWithDn.length() <= dn.length()) {
                txtImportPalletNo.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.pallet_nu_uncorrect), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (palletNoStartWithDn.indexOf(dn) != 0) {
                txtImportPalletNo.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.pallet_nu_uncorrect), Toast.LENGTH_SHORT).show();
                return false;
            }

            txtImprotDnNo.setText(dn);
            txtImportPalletNo.setText(palletNoStartWithDn);
        } else {
            String palletNo = getPalletNo();

            if (TextUtils.isEmpty(txtImportQrCode.getText().toString().trim())) {
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_qrcode), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(dn)) {
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.qrcode_dn_is_null), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(palletNo)) {
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.qrcode_pallet_is_null), Toast.LENGTH_SHORT).show();
                return false;
            }

            if(Constant.ISORG){
                String orgPrint = QrCodeUtil.getValueFromPalletLabelQrCode(txtImportQrCode.getText().toString(), PALLET_LABEL_QR_CODE_FORMAT.ORG);

                try{
                    int num=Integer.parseInt(orgPrint);
                }catch (NumberFormatException e){
                    txtImportQrCode.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.org_must_input_num), Toast.LENGTH_SHORT).show();
                    return false;
                }

//                if(!AppController.getOrgName().equals(orgPrint)){
//                    txtImportQrCode.requestFocus();
//                    Toast.makeText(getApplicationContext(), "與當前ORG不符!", Toast.LENGTH_SHORT).show();
//                    return false;
//                }
            }

            txtImportQrCode.setText(txtImportQrCode.getText().toString().trim());
        }

        return true;
    }

    private void checkPrinterSetting() {
        if (!BtPrintLabel.isPrintNameSet(this)) {
            Intent intent = new Intent(this, PrinterSettingActivity.class);
            startActivityForResult(intent, REQUEST_PRINTER_SETTING);
            Toast.makeText(getApplicationContext(), getString(R.string.set_printer_name), Toast.LENGTH_LONG).show();
            return;
        }

        if (!BtPrintLabel.isBtEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Toast.makeText(getApplicationContext(), getString(R.string.open_bt), Toast.LENGTH_LONG).show();
            return;
        }

        if (!BtPrintLabel.instance(getApplicationContext())) {
            Intent intentOpenBluetoothSettings = new Intent();
            intentOpenBluetoothSettings.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivityForResult(intentOpenBluetoothSettings, REQUEST_BLUETOOTH_SETTINGS);
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_printer), Toast.LENGTH_LONG).show();
            return;
        }

        if (BtPrintLabel.connect()) {
            Toast.makeText(getApplicationContext(), getString(R.string.bt_connect_ok), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.not_connect_btprinter), Toast.LENGTH_LONG).show();
        }
    }

    private void cleanData() {
        if (rbBarcode.isChecked()) {
            txtImprotDnNo.setText("");
            txtImportPalletNo.setText("");
            txtImportPalletSize.setText("");
            txtImprotDnNo.requestFocus();
        } else {
            txtImportQrCode.setText("");
            txtImportQrCodePalletSize.setText("");
            txtImportQrCode.requestFocus();
        }
    }

    private void doQueryPalletInfo() {
        palletInfo = new ShipmentPalletInfoHelper();
        String dn = getDn();
        String palletNo = getPalletNo();

        palletInfo.setDnNo(Integer.parseInt(dn));
        palletInfo.setPalletNo(palletNo);
        palletInfo.setPalletSize(getPalletSize());
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.downloading_data), true);
        print = new PrintLabelHandler();
        new GetPrintInfo().execute(0);
    }

    private void doPrintPalletSnInfo(int printerNo) {
        palletSnInfo = new ShipmentPalletSnInfoHelper();
        String dn = getDn();
        String palletNo = getPalletNo();

        palletSnInfo.setDnNo(Integer.parseInt(dn));
        palletSnInfo.setPalletNo(palletNo);
        palletSnInfo.setPrintNo(printerNo);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.processing), true);
        print = new PrintLabelHandler();
        new GetPrintSnInfo().execute(0);
    }

    private Dialog onCreatePrinterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle(R.string.pick_printers)
                .setItems(R.array.printers, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        AppController.debug("which:" + which);

                        if(which==2){
                            doPrintPalletSnInfo01();//新版走WIFI
                        }else if(which==3||which==4){//新版走Codesoft
                            doPrintPalletSnInfoMeraki(which);
                        }else{
                            doPrintPalletSnInfo(which + 1);//舊版列印
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        }).setCancelable(false);

        return builder.create();
    }

    private Dialog onCreateAuradinePrinterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle(R.string.pick_printers)
                .setItems(R.array.printer_type, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        AppController.debug("which:" + which);

                        if(which==0){
                            //doPrintPalletSnInfo01_Auradine();//新版走WIFI
                        }else if(which==1||which==2){//新版走Codesoft
                            doPrintPalletSnInfoAuradine(which);
                        }else{

                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        }).setCancelable(false);

        return builder.create();
    }

    private Dialog onCreateVerkadaPrinterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle(R.string.pick_printers)
                .setItems(R.array.printer_type, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        AppController.debug("which:" + which);
                        //新版走Codesoft which==1
                        //新版走wifi which==0
                        getPalletSnInfoByDnPallet_Verkada(which);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        }).setCancelable(false);

        return builder.create();
    }

    private Dialog onCreateSophosPrinterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle(R.string.pick_printers)
                .setItems(R.array.printer_type, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        AppController.debug("which:" + which);
                        //新版走Codesoft which==1
                        //新版走wifi which==0
                        getPalletSnInfoByDnPallet(which);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        }).setCancelable(false);

        return builder.create();
    }

    private void doPrintPalletSnInfoMeraki(int type) {//新版codeSoft Cambium
        palletSnInfo = new ShipmentPalletSnInfoHelper();
        String dn = getDn();
        String palletNo = getPalletNo();

        palletSnInfo.setDnNo(Integer.parseInt(dn));
        palletSnInfo.setPalletNo(palletNo);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.processing), true);
        print = new PrintLabelHandler();

        if (type==3) {
            new GetPrintSnInfoMeraki().execute(0);
        } else if(type==4) {
            new GetPrintSnInfoMeraki02().execute(0);
        }
    }

    private void doPrintPalletSnInfoAuradine(int type) {//新版codeSoft Cambium
        palletSnInfo = new ShipmentPalletSnInfoHelper();
        String dn = getDn();
        String palletNo = getPalletNo();

        palletSnInfo.setDnNo(Integer.parseInt(dn));
        palletSnInfo.setPalletNo(palletNo);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.processing), true);
        print = new PrintLabelHandler();

        if (type==1) {
            new GetPrintSnInfoAuradine().execute(0);
        } else if(type==2) {
            new GetPrintSnInfoAuradine02().execute(0);
        }
    }

    private class GetPrintSnInfoMeraki extends AsyncTask<Integer, String, ShipmentPalletSnInfoHelper> {
        @Override
        protected ShipmentPalletSnInfoHelper doInBackground(Integer... params) {
            AppController.debug("GetPrintSnInfo01:" + AppController.getServerInfo()
                    + AppController.getProperties("PrintShipmentPalletSnInfo01"));
            publishProgress(getString(R.string.processing));
            return print.printShipmentPalletSnInfo01(palletSnInfo);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(ShipmentPalletSnInfoHelper result) {
            dialog.dismiss();
            AppController.debug("GetPrintSnInfo01 result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    palletSnInfo = result;

                    //AppController.debug("GetPrintSnInfo01 getCustomerPartNo = " + palletSnInfo.getCustomerPartNo());
                    //AppController.debug("GetPrintSnInfo01 getCustomerNo = " + palletSnInfo.getCustomerNo());

                    onCreateChooseCodesoftMerakiPrinterDialog().show();
                    Toast.makeText(getApplicationContext(), "print with codesoft", Toast.LENGTH_LONG).show();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private Dialog onCreateChooseCodesoftMerakiPrinterDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer_new, null);
        final Spinner printerIP = item.findViewById(R.id.printspinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,    //對應的Context
                R.array.printersIP,                             //資料選項內容
                android.R.layout.simple_spinner_item);  //預設Spinner未展開時的View(預設及選取後樣式)
        printerIP.setAdapter(adapter);
        printerIP.setSelection(adapter.getPosition("10.0.189.136"));//TSC TX300_1F_217 10.0.162.209
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("MerakiPrinterQty"));
        etQty.setText(qty);

        HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();

        //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
        int sum = 0;
        for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
            Pdf417Info_SOPHOS datatmp = entry.getValue();
//            AppController.debug("GetPrintSnInfo01 getCustomerPartNo = " + datatmp.getCustomerPartNo());
            int size = datatmp.getSnList().size();
            int l = (int) Math.ceil(size / 70.0);//60個SN一張
            sum += l;
        }

        int tol=sum*(Integer.parseInt(qty));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle("Meraki printer info:"+(tol)+" pcs")//   "Samsara印表機資訊 總共"+(tol)+"張"
                .setView(item)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(etQty.getText().toString()) || Integer.parseInt(etQty.getText().toString().trim()) <= 0) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (true) {
                            view.setEnabled(false);
                            HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();

                            //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
                            int sum = 0;
                            for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
                                Pdf417Info_SOPHOS datatmp = entry.getValue();
                                int size = datatmp.getSnList().size();
                                int l = (int) Math.ceil(size / 70.0);//35個SN一張
                                sum += l;
                            }
                            //計算總張數--end

                            //列印資料&設定張數
//                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {ME240124-28@597117@2E@0@86
                            if (data.size() > 0) {
                                int page = 0;
                                PrintDataList=new NewPrintDataList();
                                ArrayList<NewPrintData> labelArray = new ArrayList<NewPrintData>();

                                for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {
                                    String partNo = entry.getKey();
                                    Pdf417Info_SOPHOS value = entry.getValue();

                                    AppController.debug("Server response partNo:" + partNo);
                                    AppController.debug("Server response size:" + value.getSnList().size());

                                    boolean isTAN=false;

                                    if(!TextUtils.isEmpty(value.getCustomerPartNo()) && (value.getCustomerPartNo().startsWith("A90")||value.getCustomerPartNo().startsWith("A50")||value.getCustomerPartNo().startsWith("A40"))){
                                        isTAN=true;
                                    }

                                    AppController.debug("GetPrintSnInfo01 getCustomerPartNo = " + value.getCustomerPartNo()+" ,isTAN:"+isTAN);
//                                    AppController.debug("Server response value:" + value);
//                                    AppController.debug("Server response value1:" + value);

                                    List<String> snList = new ArrayList<>();
                                    StringBuilder stringBuilder = new StringBuilder();
                                    int cnt = 0;
                                    int cnt2 = 0;
                                    int MAX_CNT = 70;

                                    for (String sn : value.getSnList()) {
//                                        AppController.debug("Server response value3:" + sn);
                                        stringBuilder.append(sn);
                                        cnt++;
                                        cnt2++;

                                        if (cnt == MAX_CNT) {//每張最大數量
                                            cnt = 0;
                                            snList.add(stringBuilder.toString());
                                            stringBuilder.setLength(0);
                                        } else if (cnt2 == value.getSnList().size()) {//剩餘的數量
                                            snList.add(stringBuilder.toString());
                                        } else {
                                            stringBuilder.append(";");
                                        }
                                    }

                                    for (String value1 : snList) {
                                        AppController.debug("Server response value2:" + value1);
                                        AppController.debug("總頁數:" + sum);
                                        int size = value1.split(";").length;
                                        AppController.debug("總數量:" + size);
                                        page++;

                                        AppController.debug("has sn? :" + !"No Numbers".equals(value1));
                                        AppController.debug("value.getQty() :" + value.getQty());

                                        NewPrintData PrintData= new NewPrintData();

                                        if(isTAN){
                                            PrintData.setLabelName(AppController.getProperties("MerakiLabelName"));
                                        }else{
                                            PrintData.setLabelName(AppController.getProperties("MerakiLabelName_TAN"));
                                        }

                                        PrintData.setPrintName(printerIP.getSelectedItem().toString());
                                        HashMap<String, String> var=new HashMap<>();
                                        var.put("PartNumber",value.getCustomerPartNo());
                                        var.put("Pallet Num",value.getPalletNo());
                                        //var.put("QTY",Integer.toString(size));
                                        if(value.getUsage_Type().equals("NonSN")){ //20250818 Ann Edit:新增usage_type，判斷是成品還是配件，以此決定要抓什麼作為數量(SENAO101-202506-0226)
                                            int sumShip_Qty = 0;
                                            for (Integer iShip_Qty : value.getShip_Qty()) {
                                                if (iShip_Qty != null) {
                                                    sumShip_Qty += iShip_Qty;
                                                }
                                            }
                                            var.put("QTY", String.valueOf(sumShip_Qty));
                                        }else if(value.getUsage_Type().equals("Y")){
                                            var.put("QTY",Integer.toString(size));
                                        }else{
                                            var.put("QTY","ERROR");
                                        }
                                        var.put("Label Num",page+"/"+sum);
                                        var.put("SN List",value.getModelName()+";"+Integer.toString(size)+";"+value1);
                                        PrintData.setVariables(var);

//                                            Gson gson = new GsonBuilder().create();
//                                            JsonElement element = gson.toJsonTree(PrintData);
                                        labelArray.add(PrintData);
//                                            測試資料:@575755@NA@0@86 => NA
//                                            @576970@10K@0@86 => 110*120*147
                                    }
                                }

                                PrintDataList.setCount(etQty.getText().toString().trim());
                                PrintDataList.setLabel(labelArray);
                                new PrintWithNewType().execute(0);
                            } else {

                            }
//                            }

                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private class GetPrintSnInfoMeraki02 extends AsyncTask<Integer, String, ShipmentPalletSnInfoHelper> {
        @Override
        protected ShipmentPalletSnInfoHelper doInBackground(Integer... params) {
            AppController.debug("GetPrintSnInfo01:"
                    + AppController.getServerInfo()
                    + AppController.getProperties("PrintShipmentPalletSnInfo01"));
            publishProgress(getString(R.string.processing));
            return print.printShipmentPalletSnInfo01(palletSnInfo);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(ShipmentPalletSnInfoHelper result) {
            dialog.dismiss();
            AppController.debug("GetPrintSnInfo01 result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    palletSnInfo = result;
                    onCreateChooseCodesoftMerakiPrinterDialog02().show();
                    Toast.makeText(getApplicationContext(), getString(R.string.print_with_codesoft), Toast.LENGTH_LONG).show();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private Dialog onCreateChooseCodesoftMerakiPrinterDialog02() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer_new, null);
        final Spinner printerIP = item.findViewById(R.id.printspinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,    //對應的Context
                R.array.printersIP,                             //資料選項內容
                android.R.layout.simple_spinner_item);  //預設Spinner未展開時的View(預設及選取後樣式)
        printerIP.setAdapter(adapter);
        printerIP.setSelection(adapter.getPosition("10.0.189.136"));//TSC TX300_1F_217 10.0.162.209
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("MerakiPrinterQty"));
        etQty.setText(qty);

        HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();

        //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
        int sum = 0;

        for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
            Pdf417Info_SOPHOS datatmp = entry.getValue();
            int size = datatmp.getSnList().size();
            int l = (int) Math.ceil(size / 70.0);//60個SN一張
            sum += l;
        }

        int tol=sum*(Integer.parseInt(qty));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle("Meraki printer info:"+(tol)+" pcs")//   "Samsara印表機資訊 總共"+(tol)+"張"
                .setView(item)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(etQty.getText().toString()) || Integer.parseInt(etQty.getText().toString().trim()) <= 0) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (true) {
                            view.setEnabled(false);
                            HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();

                            //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
                            int sum = 0;

                            for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
                                Pdf417Info_SOPHOS datatmp = entry.getValue();
                                int size = datatmp.getSnList().size();
                                int l = (int) Math.ceil(size / 70.0);//35個SN一張
                                sum += l;
                            }
                            //計算總張數--end

                            //列印資料&設定張數
//                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {
                            if (data.size() > 0) {
                                int page = 0;
                                PrintDataList=new NewPrintDataList();
                                ArrayList<NewPrintData> labelArray = new ArrayList<NewPrintData>();

                                for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {
                                    String partNo = entry.getKey();
                                    Pdf417Info_SOPHOS value = entry.getValue();

                                    boolean isTAN = false;

                                    if(!TextUtils.isEmpty(value.getCustomerPartNo()) && (value.getCustomerPartNo().startsWith("A90")||value.getCustomerPartNo().startsWith("A50")||value.getCustomerPartNo().startsWith("A40"))){
                                        isTAN = true;
                                    }

                                    AppController.debug("Server response partNo:" + partNo);
                                    AppController.debug("Server response size:" + value.getSnList().size());
//                                    AppController.debug("Server response value:" + value);
//                                    AppController.debug("Server response value1:" + value);

                                    List<String> snList = new ArrayList<>();
                                    StringBuilder stringBuilder = new StringBuilder();
                                    int cnt = 0;
                                    int cnt2 = 0;
                                    int MAX_CNT = 70;

                                    for (String sn : value.getSnList()) {
//                                        AppController.debug("Server response value3:" + sn);
                                        stringBuilder.append(sn);
                                        cnt++;
                                        cnt2++;

                                        if (cnt == MAX_CNT) {//每張最大數量
                                            cnt = 0;
                                            snList.add(stringBuilder.toString());
                                            stringBuilder.setLength(0);
                                        } else if (cnt2 == value.getSnList().size()) {//剩餘的數量
                                            snList.add(stringBuilder.toString());
                                        } else {
                                            stringBuilder.append(";");
                                        }
                                    }

                                    for (String value1 : snList) {
                                        AppController.debug("Server response value2:" + value1);
                                        AppController.debug("總頁數:" + sum);
                                        int size = value1.split(";").length;
                                        AppController.debug("總數量:" + size);
                                        page++;
                                        AppController.debug("has sn? :" + !"No Numbers".equals(value1));
                                        AppController.debug("value.getQty() :" + value.getQty());
                                        NewPrintData PrintData= new NewPrintData();

                                        PrintData.setLabelName(AppController.getProperties("MerakiLabelName02"));

                                        if(isTAN){
                                            PrintData.setLabelName(AppController.getProperties("MerakiLabelName02"));
                                        }else{
                                            PrintData.setLabelName(AppController.getProperties("MerakiLabelName02_TAN"));
                                        }

                                        PrintData.setPrintName(printerIP.getSelectedItem().toString());
                                        HashMap<String, String> var=new HashMap<>();
                                        var.put("PartNumber",value.getCustomerPartNo());
                                        var.put("Pallet Num",value.getPalletNo());
                                        //var.put("QTY",Integer.toString(size));
                                        if(value.getUsage_Type().equals("NonSN")){ //20250818 Ann Edit:新增usage_type，判斷是成品還是配件，以此決定要抓什麼作為數量(SENAO101-202506-0226)
                                            int sumShip_Qty = 0;
                                            for (Integer iShip_Qty : value.getShip_Qty()) {
                                                if (iShip_Qty != null) {
                                                    sumShip_Qty += iShip_Qty;
                                                }
                                            }
                                            var.put("QTY", String.valueOf(sumShip_Qty));
                                        }else if(value.getUsage_Type().equals("Y")){
                                            var.put("QTY",Integer.toString(size));
                                        }else{
                                            var.put("QTY","ERROR");
                                        }
                                        var.put("Label Num",page+"/"+sum);
                                        var.put("SN List",value.getModelName()+";"+Integer.toString(size)+";"+value1);
                                        PrintData.setVariables(var);

//                                        Gson gson = new GsonBuilder().create();
//                                        JsonElement element = gson.toJsonTree(PrintData);
                                        labelArray.add(PrintData);
//                                        測試資料:@575755@NA@0@86 => NA
//                                        @576970@10K@0@86 => 110*120*147
                                    }
                                }

                                PrintDataList.setCount(etQty.getText().toString().trim());
                                PrintDataList.setLabel(labelArray);
                                new PrintWithNewType02().execute(0);
                            } else {

                            }

                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private class GetPrintSnInfoAuradine extends AsyncTask<Integer, String, ShipmentPalletSnInfoHelper> {
        @Override
        protected ShipmentPalletSnInfoHelper doInBackground(Integer... params) {
            AppController.debug("GetPrintSnInfoAuradine:"
                    + AppController.getServerInfo()
                    + AppController.getProperties("PrintShipmentPalletSnInfo01"));
            publishProgress(getString(R.string.processing));
            return print.printShipmentPalletSnInfo01(palletSnInfo);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(ShipmentPalletSnInfoHelper result) {
            dialog.dismiss();
            AppController.debug("GetPrintSnInfoAuradine result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    palletSnInfo = result;

//                    AppController.debug("GetPrintSnInfo01 getCustomerPartNo = " + palletSnInfo.getCustomerPartNo());
//                    AppController.debug("GetPrintSnInfo01 getCustomerNo = " + palletSnInfo.getCustomerNo());

                    onCreateChooseCodesoftAuradinePrinterDialog().show();
                    Toast.makeText(getApplicationContext(), getString(R.string.print_with_codesoft), Toast.LENGTH_LONG).show();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private Dialog onCreateChooseCodesoftAuradinePrinterDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer_new, null);
        final Spinner printerIP = item.findViewById(R.id.printspinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,    //對應的Context
                R.array.printersIP,                             //資料選項內容
                android.R.layout.simple_spinner_item);  //預設Spinner未展開時的View(預設及選取後樣式)
        printerIP.setAdapter(adapter);
        printerIP.setSelection(adapter.getPosition("10.0.189.136"));//TSC TX300_1F_217 10.0.162.209
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("MerakiPrinterQty"));
        etQty.setText(qty);

        HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();

        //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
        int sum = 0;

        for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
            Pdf417Info_SOPHOS datatmp = entry.getValue();

//            AppController.debug("GetPrintSnInfo01 getCustomerPartNo = " + datatmp.getCustomerPartNo());
            int size = datatmp.getSnList().size();
            int l = (int) Math.ceil(size / 70.0);//60個SN一張
            sum += l;
        }

        int tol=sum*(Integer.parseInt(qty));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle("Auradine printer info:"+(tol)+" pcs")//   "Samsara印表機資訊 總共"+(tol)+"張"
                .setView(item)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(etQty.getText().toString()) || Integer.parseInt(etQty.getText().toString().trim()) <= 0) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (true) {
                            view.setEnabled(false);
                            HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();

                            //寫一個用CustomerPN當作是SKU的城市
                            HashMap<String, Pdf417Info_SOPHOS> Auradine = new HashMap<>();

                            /**
                             * 總共會用到的資料
                             * 1.getSnList
                             * 2.getCustomerPartNo = > Key
                             * 3.getPartNo
                             * 4.getPalletNo
                             * 5.getQty
                             * 6.getDnNo
                             *
                             *
                             */

                            for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
                                Pdf417Info_SOPHOS datatmp = entry.getValue();
                                String senaoPN = entry.getKey();
//                                String newCustomerPN = datatmp.getCustomerPartNo();
                                String newCustomerPN = datatmp.getModelName();

                                if(Auradine.containsKey(newCustomerPN)){
                                    // 如果 Auradine 中已包含此 CustomerPN，累加相關信息
                                    Pdf417Info_SOPHOS existingData = Auradine.get(newCustomerPN);

                                    // 累加 SN 列表
                                    existingData.getSnList().addAll(datatmp.getSnList());

                                    // 更新數量
                                    existingData.setQty(existingData.getQty() + datatmp.getQty());

                                    // 可以根據需求添加其他累加或更新邏輯，例如：
                                    // existingData.setPalletNo(existingData.getPalletNo() + "," + datatmp.getPalletNo());
                                }else{
                                    // 如果 Auradine 中不包含此 CustomerPN，直接將該數據加入
                                    Pdf417Info_SOPHOS newData = new Pdf417Info_SOPHOS();
                                    newData.setCustomerPartNo(newCustomerPN);
                                    newData.setSnList(new ArrayList<>(datatmp.getSnList())); //
                                    newData.setQty(datatmp.getQty());
                                    newData.setPartNo(datatmp.getPartNo());
                                    newData.setPalletNo(datatmp.getPalletNo());
                                    newData.setDnNo(datatmp.getDnNo());

                                    // 將新數據放入 Auradine
                                    Auradine.put(newCustomerPN, newData);
                                }
                            }

                            // 測試打印 Auradine 中的數據
                            for (Map.Entry<String, Pdf417Info_SOPHOS> entry : Auradine.entrySet()) {
                                String customerPN = entry.getKey();
                                Pdf417Info_SOPHOS info = entry.getValue();

                                AppController.debug("=====CustomerPN: " + customerPN);
                                AppController.debug("=====Details: " + info.toString());
                            }

                            data = Auradine;

                            //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
                            int sum = 0;

                            for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
                                Pdf417Info_SOPHOS datatmp = entry.getValue();


                                int size = datatmp.getSnList().size();
                                int l = (int) Math.ceil(size / 18.0);//
                                sum += l;
                            }
                            //計算總張數--end

                            //列印資料&設定張數
//                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {ME240124-28@597117@2E@0@86
                            if (data.size() > 0) {
                                int page = 0;
                                PrintDataList=new NewPrintDataList();
                                ArrayList<NewPrintData> labelArray = new ArrayList<NewPrintData>();

                                for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {
                                    String partNo = entry.getKey();
                                    Pdf417Info_SOPHOS value = entry.getValue();

                                    AppController.debug("Server response partNo:" + partNo);
                                    AppController.debug("Server response size:" + value.getSnList().size());
                                    AppController.debug("getModelName : "+value.getModelName()+", getPartNo = " + value.getPartNo()+" ,getPalletNo:"+value.getPalletNo());//getPartNo = 1102A1270300 ,getPalletNo:2E

//                                    AppController.debug("Server response value:" + value);
//                                    AppController.debug("Server response value1:" + value);

                                    List<String> snList = new ArrayList<>();
                                    StringBuilder stringBuilder = new StringBuilder();
                                    int cnt = 0;
                                    int cnt2 = 0;
                                    int MAX_CNT = 18;

                                    for (String sn : value.getSnList()) {
//                                        AppController.debug("Server response value3:" + sn);
                                        stringBuilder.append(sn);
                                        cnt++;
                                        cnt2++;

                                        if (cnt == MAX_CNT) {//每張最大數量
                                            cnt = 0;
                                            snList.add(stringBuilder.toString());
                                            stringBuilder.setLength(0);
                                        } else if (cnt2 == value.getSnList().size()) {//剩餘的數量
                                            snList.add(stringBuilder.toString());
                                        } else {
                                            stringBuilder.append(";");
                                        }
                                    }
//
                                    AppController.debug("總頁數:" + sum);
                                    for (String value1 : snList) {
                                        AppController.debug("SN 格式:" + value1);
                                        int size = value1.split(";").length;
                                        AppController.debug("總數量:" + size);
                                        page++;
                                        AppController.debug("has sn? :" + !"No Numbers".equals(value1));
                                        AppController.debug("value.getQty() :" + value.getQty());
                                        NewPrintData PrintData= new NewPrintData();

                                        PrintData.setLabelName(AppController.getProperties("AuradineLabelName"));

                                        PrintData.setPrintName(printerIP.getSelectedItem().toString());
                                        HashMap<String, String> var=new HashMap<>();
//                                        var.put("SKU",value.getCustomerPartNo());//SKU
//                                        var.put("SKU",value.getModelName());//SKU
                                        var.put("SKU",partNo);//SKU
                                        var.put("ID",value.getDnNo()+value.getPalletNo());//PalletID 5197972E
                                        var.put("QTY",Integer.toString(size));
//                                        var.put("Label Num",page+"/"+sum);
                                        var.put("SNLIST",value1);//SN1;SN2;SN3

                                        AppController.debug("SKU:" + value.getCustomerPartNo());
                                        AppController.debug("ID:" + value.getDnNo()+value.getPalletNo());
                                        AppController.debug("QTY:" + Integer.toString(size));
                                        AppController.debug("SNLIST:" + value1);

                                        PrintData.setVariables(var);
                                        labelArray.add(PrintData);
//                                        測試資料:@575755@NA@0@86 => NA
//                                        @576970@10K@0@86 => 110*120*147

//                                        if(page>=2){
//                                            break;
//                                        }
                                    }
                                }

                                PrintDataList.setCount(etQty.getText().toString().trim());
                                PrintDataList.setLabel(labelArray);
                                new PrintWithNewType().execute(0);//先註解 要打開 SNLIST
                            } else {

                            }
//                            }

                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private class GetPrintSnInfoAuradine02 extends AsyncTask<Integer, String, ShipmentPalletSnInfoHelper> {
        @Override
        protected ShipmentPalletSnInfoHelper doInBackground(Integer... params) {
            AppController.debug("GetPrintSnInfoAuradine:"
                    + AppController.getServerInfo()
                    + AppController.getProperties("PrintShipmentPalletSnInfo01"));
            publishProgress(getString(R.string.processing));
            return print.printShipmentPalletSnInfo01(palletSnInfo);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(ShipmentPalletSnInfoHelper result) {
            dialog.dismiss();
            AppController.debug("GetPrintSnInfoAuradine result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    palletSnInfo = result;

//                    AppController.debug("GetPrintSnInfo01 getCustomerPartNo = " + palletSnInfo.getCustomerPartNo());
//                    AppController.debug("GetPrintSnInfo01 getCustomerNo = " + palletSnInfo.getCustomerNo());

                    onCreateChooseCodesoftAuradinePrinterDialog02().show();
                    Toast.makeText(getApplicationContext(), getString(R.string.print_with_codesoft), Toast.LENGTH_LONG).show();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private Dialog onCreateChooseCodesoftAuradinePrinterDialog02() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer_new, null);
        final Spinner printerIP = item.findViewById(R.id.printspinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,    //對應的Context
                R.array.printersIP,                             //資料選項內容
                android.R.layout.simple_spinner_item);  //預設Spinner未展開時的View(預設及選取後樣式)
        printerIP.setAdapter(adapter);
        printerIP.setSelection(adapter.getPosition("10.0.189.136"));//TSC TX300_1F_217 10.0.162.209
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("MerakiPrinterQty"));
        etQty.setText(qty);

        HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();

        //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
        int sum = 0;

        for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
            Pdf417Info_SOPHOS datatmp = entry.getValue();

//            AppController.debug("GetPrintSnInfo01 getCustomerPartNo = " + datatmp.getCustomerPartNo());
            int size = datatmp.getSnList().size();
            int l = (int) Math.ceil(size / 70.0);//60個SN一張
            sum += l;
        }

        int tol=sum*(Integer.parseInt(qty));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle("Auradine printer info:"+(tol)+" pcs")//   "Samsara印表機資訊 總共"+(tol)+"張"
                .setView(item)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(etQty.getText().toString()) || Integer.parseInt(etQty.getText().toString().trim()) <= 0) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (true) {
                            view.setEnabled(false);
                            HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();

                            //寫一個用CustomerPN當作是SKU的城市
                            HashMap<String, Pdf417Info_SOPHOS> Auradine = new HashMap<>();

                            /**
                             * 總共會用到的資料
                             * 1.getSnList
                             * 2.getCustomerPartNo = > Key
                             * 3.getPartNo
                             * 4.getPalletNo
                             * 5.getQty
                             * 6.getDnNo
                             *
                             *pn=modelName?
                             */

                            for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
                                Pdf417Info_SOPHOS datatmp = entry.getValue();
                                String senaoPN = entry.getKey();
//                                String newCustomerPN = datatmp.getCustomerPartNo();
                                String newCustomerPN = datatmp.getModelName();

                                if(Auradine.containsKey(newCustomerPN)){
                                    // 如果 Auradine 中已包含此 CustomerPN，累加相關信息
                                    Pdf417Info_SOPHOS existingData = Auradine.get(newCustomerPN);

                                    // 累加 SN 列表
                                    existingData.getSnList().addAll(datatmp.getSnList());

                                    // 更新數量
                                    existingData.setQty(existingData.getQty() + datatmp.getQty());

                                    // 可以根據需求添加其他累加或更新邏輯，例如：
                                    // existingData.setPalletNo(existingData.getPalletNo() + "," + datatmp.getPalletNo());
                                }else{
                                    // 如果 Auradine 中不包含此 CustomerPN，直接將該數據加入
                                    Pdf417Info_SOPHOS newData = new Pdf417Info_SOPHOS();
                                    newData.setCustomerPartNo(newCustomerPN);
                                    newData.setSnList(new ArrayList<>(datatmp.getSnList())); //
                                    newData.setQty(datatmp.getQty());
                                    newData.setPartNo(datatmp.getPartNo());
                                    newData.setPalletNo(datatmp.getPalletNo());
                                    newData.setDnNo(datatmp.getDnNo());

                                    // 將新數據放入 Auradine
                                    Auradine.put(newCustomerPN, newData);
                                }
                            }

                            // 測試打印 Auradine 中的數據
                            for (Map.Entry<String, Pdf417Info_SOPHOS> entry : Auradine.entrySet()) {
                                String customerPN = entry.getKey();
                                Pdf417Info_SOPHOS info = entry.getValue();
                                AppController.debug("=====CustomerPN: " + customerPN);
                                AppController.debug("=====Details: " + info.toString());
                            }

                            data = Auradine;

                            //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
                            int sum = 0;

                            for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
                                Pdf417Info_SOPHOS datatmp = entry.getValue();

                                int size = datatmp.getSnList().size();
                                int l = (int) Math.ceil(size / 18.0);//
                                sum += l;
                            }
                            //計算總張數--end

                            //列印資料&設定張數
//                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {ME240124-28@597117@2E@0@86
                            if (data.size() > 0) {
                                int page = 0;
                                PrintDataList=new NewPrintDataList();
                                ArrayList<NewPrintData> labelArray = new ArrayList<NewPrintData>();

                                for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {
                                    String partNo = entry.getKey();
                                    Pdf417Info_SOPHOS value = entry.getValue();
                                    AppController.debug("Server response partNo:" + partNo);
                                    AppController.debug("Server response size:" + value.getSnList().size());
                                    AppController.debug("getModelName : "+value.getModelName()+", getPartNo = " + value.getPartNo()+" ,getPalletNo:"+value.getPalletNo());//getPartNo = 1102A1270300 ,getPalletNo:2E

//                                    AppController.debug("Server response value:" + value);
//                                    AppController.debug("Server response value1:" + value);

                                    List<String> snList = new ArrayList<>();
                                    StringBuilder stringBuilder = new StringBuilder();
                                    int cnt = 0;
                                    int cnt2 = 0;
                                    int MAX_CNT = 18;

                                    for (String sn : value.getSnList()) {
//                                        AppController.debug("Server response value3:" + sn);
                                        stringBuilder.append(sn);
                                        cnt++;
                                        cnt2++;

                                        if (cnt == MAX_CNT) {//每張最大數量
                                            cnt = 0;
                                            snList.add(stringBuilder.toString());
                                            stringBuilder.setLength(0);
                                        } else if (cnt2 == value.getSnList().size()) {//剩餘的數量
                                            snList.add(stringBuilder.toString());
                                        } else {
                                            stringBuilder.append(";");
                                        }
                                    }
//
                                    for (String value1 : snList) {
                                        AppController.debug("SN 格式:" + value1);
                                        AppController.debug("總頁數:" + sum);
                                        int size = value1.split(";").length;
                                        AppController.debug("總數量:" + size);
                                        page++;
                                        AppController.debug("has sn? :" + !"No Numbers".equals(value1));
                                        AppController.debug("value.getQty() :" + value.getQty());

                                        NewPrintData PrintData= new NewPrintData();

                                        PrintData.setLabelName(AppController.getProperties("AuradineLabelName02"));

                                        PrintData.setPrintName(printerIP.getSelectedItem().toString());
                                        HashMap<String, String> var=new HashMap<>();
//                                        var.put("SKU",value.getCustomerPartNo());//SKU
//                                        var.put("SKU",value.getModelName());//SKU
                                        var.put("SKU",partNo);//SKU
                                        var.put("ID",value.getDnNo()+value.getPalletNo());//PalletID 5197972E
                                        var.put("QTY",Integer.toString(size));
                                        var.put("Label Num",page+"/"+sum);
                                        var.put("SNLIST",value1);//SN1;SN2;SN3
                                        PrintData.setVariables(var);

//                                        Gson gson = new GsonBuilder().create();
//                                        JsonElement element = gson.toJsonTree(PrintData);
                                        labelArray.add(PrintData);
//                                        測試資料:@575755@NA@0@86 => NA
//                                        @576970@10K@0@86 => 110*120*147
                                    }
                                }

                                PrintDataList.setCount(etQty.getText().toString().trim());
                                PrintDataList.setLabel(labelArray);
                                new PrintWithNewType02().execute(0);//先註解 要打開
                            } else {

                            } //AD241118-07@640465@2A@18@286
//                            }

                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private void doPrintPalletSnInfo2() {//新版codeSoft Cambium
        palletSnInfo = new ShipmentPalletSnInfoHelper();
        String dn = getDn();
        String palletNo = getPalletNo();
        palletSnInfo.setDnNo(Integer.parseInt(dn));
        palletSnInfo.setPalletNo(palletNo);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.processing), true);
        print = new PrintLabelHandler();
        new GetPrintSnInfo2().execute(0);
    }

    private class GetPrintSnInfo2 extends AsyncTask<Integer, String, ShipmentPalletSnInfoHelper> {
        @Override
        protected ShipmentPalletSnInfoHelper doInBackground(Integer... params) {
            AppController.debug("GetPrintSnInfo01:"
                    + AppController.getServerInfo()
                    + AppController.getProperties("PrintShipmentPalletSnInfo01"));
            publishProgress(getString(R.string.processing));
            return print.printShipmentPalletSnInfo01(palletSnInfo);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(ShipmentPalletSnInfoHelper result) {
            dialog.dismiss();
            AppController.debug("GetPrintSnInfo01 result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    palletSnInfo = result;
//                    onCreateChooseCambiumDialog().show();//原來的
                    onCreatePrinterCambiumDialog().show();
                    Toast.makeText(getApplicationContext(), getString(R.string.print_with_new_type), Toast.LENGTH_LONG).show();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private Dialog onCreatePrinterCambiumDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle(R.string.pick_printers)
                .setItems(R.array.printer_type02, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if (which==0) {//CODESOFT列印
                            onCreateChooseCambiumDialog().show();
                        }else if (which==1){//CODESOFT2列印
                            onCreateChooseCambiumDialog02().show();
                        }

                        AppController.debug("which:" + which);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        }).setCancelable(false);
        return builder.create();
    }

    private Dialog onCreateChooseCambiumDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer_new, null);
        final Spinner printerIP = item.findViewById(R.id.printspinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,    //對應的Context
                R.array.printersIP,                             //資料選項內容
                android.R.layout.simple_spinner_item);  //預設Spinner未展開時的View(預設及選取後樣式)
        printerIP.setAdapter(adapter);
        printerIP.setSelection(adapter.getPosition("10.0.189.136"));//TSC TX300_1F_216
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("MerakiPrinterQty"));
        etQty.setText(qty);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle("Cambium printer info: 1 pcs")
                .setView(item)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(etQty.getText().toString())) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (saveMerakiPrinterInfo(printerIP.getSelectedItem().toString(), "9100", etQty.getText().toString().trim())) {
                            view.setEnabled(false);
                            PrintDataList=new NewPrintDataList();
                            ArrayList<NewPrintData> labelArray = new ArrayList<NewPrintData>();

                            for (Map.Entry<String, Pdf417Info_SOPHOS> entry : palletSnInfo.getHt().entrySet()){
                                Pdf417Info_SOPHOS datatmp = entry.getValue();
                                int length = datatmp.getSnList().size();
                                int qty =length;

                                NewPrintData PrintData= new NewPrintData();
                                PrintData.setLabelName(AppController.getProperties("CambiumLabelName"));
                                PrintData.setPrintName(printerIP.getSelectedItem().toString());
                                HashMap<String, String> data=new HashMap<>();
                                data.put("CustomPartNum",datatmp.getModelName());//Model = PN
                                List<Pair<String, Float>> BoxList=new ArrayList();
                                List<Pair<String, Integer>> ShipQTY=new ArrayList();
                                int size=datatmp.getBoxList().size();

                                for (int i = 0; i < size; i++) {
                                    BoxList.add(new Pair<>(datatmp.getBoxList().get(i), datatmp.getBoxWeight().get(i)));
                                    ShipQTY.add(new Pair<>(datatmp.getBoxList().get(i), datatmp.getShip_Qty().get(i)));
                                }

                                BoxList=new ArrayList<>(new HashSet<>(BoxList));
                                ShipQTY=new ArrayList<>(new HashSet<>(ShipQTY));

                                data.put("BoxQTY",String.valueOf(BoxList.size()));
                                data.put("SuppInovice","1284276+"+datatmp.getWorkNo());
                                data.put("PONumber",datatmp.getCustomerPO());
                                String Address="Cambium networks Ltd. \\n";
                                String wrappedAddress;

                                String address = datatmp.getAddress();
                                int maxLength = 45;
                                List<String> parts = new ArrayList<>();

                                while (address.length() > maxLength) {
                                    int endIndex = address.lastIndexOf(" ", maxLength);

                                    if (endIndex <= 0) {
                                        // 如果无法找到空格，则直接截取最大长度
                                        parts.add(address.substring(0, maxLength));
                                        address = address.substring(maxLength);
                                    } else {
                                        // 找到最后一个空格并截取至空格前一个字符
                                        parts.add(address.substring(0, endIndex));
                                        address = address.substring(endIndex + 1);
                                    }
                                }

                                if (!address.isEmpty()) {
                                    parts.add(address);
                                }

                                wrappedAddress=TextUtils.join("\\n", parts);
//                              wrappedAddress = TextUtils.join("\n", TextUtils.split(datatmp.getAddress(), "(?<=\\G.{40})"));
                                data.put("ShipAddress",Address+
                                        wrappedAddress+"\\n"+
                                        datatmp.getReceiver()+"\\n"+
                                        datatmp.getContactPhone());
                                double sum=15;//pallet 15 kg

                                for (Pair<String, Float> boxWeight : BoxList){
                                    sum+=boxWeight.second;
                                }

                                double QTY=0;

                                for (Pair<String, Integer> boxQty : ShipQTY){
                                    QTY+=boxQty.second;
                                }

                                data.put("QTY", String.format("%.0f",QTY));//Qty
                                data.put("KG",String.format("%.2f",sum));
                                data.put("LB",String.format("%.2f",sum*2.04));
                                PrintData.setVariables(data);
//                                Gson gson = new GsonBuilder().create();
//                                JsonElement element = gson.toJsonTree(PrintData);
                                labelArray.add(PrintData);
                            }

                            PrintDataList.setCount(etQty.getText().toString().trim());
                            PrintDataList.setLabel(labelArray);
                            new PrintWithNewType().execute(0);
//                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private Dialog onCreateChooseCambiumDialog02() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer_new, null);
        final Spinner printerIP = item.findViewById(R.id.printspinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,    //對應的Context
                R.array.printersIP,                             //資料選項內容
                android.R.layout.simple_spinner_item);  //預設Spinner未展開時的View(預設及選取後樣式)
        printerIP.setAdapter(adapter);
        printerIP.setSelection(adapter.getPosition("10.0.189.136"));//TSC TX300_1F_216
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("MerakiPrinterQty"));
        etQty.setText(qty);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle("Cambium printer info: 1 pcs")
                .setView(item)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(etQty.getText().toString())) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (saveMerakiPrinterInfo(printerIP.getSelectedItem().toString(), "9100", etQty.getText().toString().trim())) {
                            view.setEnabled(false);
                            PrintDataList=new NewPrintDataList();
                            ArrayList<NewPrintData> labelArray = new ArrayList<NewPrintData>();

                            for (Map.Entry<String, Pdf417Info_SOPHOS> entry : palletSnInfo.getHt().entrySet()){
                                Pdf417Info_SOPHOS datatmp = entry.getValue();
                                int length = datatmp.getSnList().size();
                                int qty =length;

                                NewPrintData PrintData= new NewPrintData();
                                PrintData.setLabelName(AppController.getProperties("CambiumLabelName02"));
                                PrintData.setPrintName(printerIP.getSelectedItem().toString());
                                HashMap<String, String> data=new HashMap<>();
                                data.put("CustomPartNum",datatmp.getModelName());//Model = PN
                                List<Pair<String, Float>> BoxList=new ArrayList();
                                List<Pair<String, Integer>> ShipQTY=new ArrayList();
                                int size=datatmp.getBoxList().size();

                                for (int i = 0; i < size; i++) {
                                    BoxList.add(new Pair<>(datatmp.getBoxList().get(i), datatmp.getBoxWeight().get(i)));
                                    ShipQTY.add(new Pair<>(datatmp.getBoxList().get(i), datatmp.getShip_Qty().get(i)));
                                }

                                BoxList=new ArrayList<>(new HashSet<>(BoxList));
                                ShipQTY=new ArrayList<>(new HashSet<>(ShipQTY));

                                data.put("BoxQTY",String.valueOf(BoxList.size()));
                                data.put("SuppInovice","1284276+"+datatmp.getWorkNo());
                                data.put("PONumber",datatmp.getCustomerPO());
                                String Address="Cambium networks Ltd. \\n";
                                String wrappedAddress;

                                String address = datatmp.getAddress();
                                int maxLength = 45;
                                List<String> parts = new ArrayList<>();

                                while (address.length() > maxLength) {
                                    int endIndex = address.lastIndexOf(" ", maxLength);

                                    if (endIndex <= 0) {
                                        // 如果无法找到空格，则直接截取最大长度
                                        parts.add(address.substring(0, maxLength));
                                        address = address.substring(maxLength);
                                    } else {
                                        // 找到最后一个空格并截取至空格前一个字符
                                        parts.add(address.substring(0, endIndex));
                                        address = address.substring(endIndex + 1);
                                    }
                                }

                                if (!address.isEmpty()) {
                                    parts.add(address);
                                }

                                wrappedAddress=TextUtils.join("\\n", parts);
//                                wrappedAddress = TextUtils.join("\n", TextUtils.split(datatmp.getAddress(), "(?<=\\G.{40})"));
                                data.put("ShipAddress",Address+
                                        wrappedAddress+"\\n"+
                                        datatmp.getReceiver()+"\\n"+
                                        datatmp.getContactPhone());
                                double sum=15; //pallet 15 kg

                                for (Pair<String, Float> boxWeight : BoxList){
                                    sum+=boxWeight.second;
                                }

                                double QTY=0;

                                for (Pair<String, Integer> boxQty : ShipQTY){
                                    QTY+=boxQty.second;
                                }

                                data.put("QTY", String.format("%.0f",QTY));//Qty
                                data.put("KG",String.format("%.2f",sum));
                                data.put("LB",String.format("%.2f",sum*2.04));
                                PrintData.setVariables(data);
//                                Gson gson = new GsonBuilder().create();
//                                JsonElement element = gson.toJsonTree(PrintData);
                                labelArray.add(PrintData);
                            }

                            PrintDataList.setCount(etQty.getText().toString().trim());
                            PrintDataList.setLabel(labelArray);
                            new PrintWithNewType02().execute(0);
//                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private class PrintWithNewType extends AsyncTask<Integer, String, String> {
        @Override
        protected String doInBackground(Integer... params) {
            AppController.debug("PrintWithNewType:"
                    + AppController.getProperties("NewCodesoftPrint"));
            publishProgress(getString(R.string.processing));
            String result = "";
            // 創建 HttpClient 對象
            HttpClient httpClient = new DefaultHttpClient();
            httpClient.getConnectionManager().getSchemeRegistry().register(
                    new Scheme("https", SSLSocketFactory.getSocketFactory(), 443)
            );

            String jason =new Gson().toJson(PrintDataList);
            AppController.debug("Print jason: "+ jason);

            // 創建 HttpClient 對象
            try {
                // 創建 HttpPost 對象並設置 URL
                HttpPost httpPost = new HttpPost(AppController.getProperties("NewCodesoftPrint"));
                // 設置請求的 JSON 數據
                StringEntity stringEntity = new StringEntity(jason);
                httpPost.setEntity(stringEntity);
                // 設置 Content-Type 頭部
                httpPost.setHeader("Content-Type", "application/json");
                // 執行 POST 請求
                HttpResponse response = httpClient.execute(httpPost);
                // 獲取響應
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    // 讀取響應內容
                    result = EntityUtils.toString(entity);
                    // 在這裡處理返回的結果
                }

                Thread.sleep(1000);
                AppController.debug("Print result: "+ result);
            } catch (Exception e) {
                e.printStackTrace();
                AppController.debug("Print result Exception: "+ e.getMessage());
                return e.getMessage();
            }

            httpClient.getConnectionManager().shutdown();
            return result;
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("Printing " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.contains("OK")) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                } else {
                    mConnection.setText(getString(R.string.db_return_error));
                    errorInfo = result;
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class PrintWithNewType02 extends AsyncTask<Integer, String, String> {
        @Override
        protected String doInBackground(Integer... params) {
            AppController.debug("PrintWithNewType02:"
                    + AppController.getProperties("NewCodesoftPrint02"));
            publishProgress(getString(R.string.processing));
            String result = "";
            // 創建 HttpClient 對象
            HttpClient httpClient = new DefaultHttpClient();
            httpClient.getConnectionManager().getSchemeRegistry().register(
                    new Scheme("https", SSLSocketFactory.getSocketFactory(), 443)
            );

            String jason =new Gson().toJson(PrintDataList);
            AppController.debug("Print02 jason: "+ jason);

            // 創建 HttpClient 對象
            try {
                // 創建 HttpPost 對象並設置 URL
                HttpPost httpPost = new HttpPost(AppController.getProperties("NewCodesoftPrint02"));
                // 設置請求的 JSON 數據
                StringEntity stringEntity = new StringEntity(jason);
                httpPost.setEntity(stringEntity);
                // 設置 Content-Type 頭部
                httpPost.setHeader("Content-Type", "application/json");
                // 執行 POST 請求
                HttpResponse response = httpClient.execute(httpPost);
                // 獲取響應
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    // 讀取響應內容
                    result = EntityUtils.toString(entity);
                    // 在這裡處理返回的結果
                }

                Thread.sleep(1000);
                AppController.debug("Print02 result: "+ result);
            } catch (Exception e) {
                e.printStackTrace();
                AppController.debug("Print02 result Exception: "+ e.getMessage());
                return e.getMessage();
            }

            httpClient.getConnectionManager().shutdown();
            return result;
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("Printing " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.contains("OK")) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                } else {
                    mConnection.setText(getString(R.string.db_return_error));
                    errorInfo = result;
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private void doPrintPalletSnInfo01() {//新版走WIFI
        palletSnInfo = new ShipmentPalletSnInfoHelper();
        String dn = getDn();
        String palletNo = getPalletNo();
        palletSnInfo.setDnNo(Integer.parseInt(dn));
        palletSnInfo.setPalletNo(palletNo);
//        palletSnInfo.setPrintNo(printerNo);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.processing), true);
        print = new PrintLabelHandler();
        new GetPrintSnInfo01().execute(0);
    }

    private class GetPrintSnInfo01 extends AsyncTask<Integer, String, ShipmentPalletSnInfoHelper> {
        @Override
        protected ShipmentPalletSnInfoHelper doInBackground(Integer... params) {
            AppController.debug("GetPrintSnInfo01:"
                    + AppController.getServerInfo()
                    + AppController.getProperties("PrintShipmentPalletSnInfo01"));
            publishProgress(getString(R.string.processing));
            return print.printShipmentPalletSnInfo01(palletSnInfo);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        @Override
        protected void onPostExecute(ShipmentPalletSnInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("GetPrintSnInfo01 result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    palletSnInfo = result;
                    onCreateChooseMerakiPrinterDialog().show();//新版走WIFI
                    Toast.makeText(getApplicationContext(), getString(R.string.New_version_uses_WiFi_for_printing), Toast.LENGTH_LONG).show();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private Dialog onCreateChooseMerakiPrinterDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer, null);
        final EditText etIP = item.findViewById(R.id.editTextIP);
        String ip = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_IP, AppController.getProperties("MerakiPrinterIP"));
        etIP.setText(ip);
        final EditText etPort = item.findViewById(R.id.editTextPort);
        String port = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_PORT, AppController.getProperties("MerakiPrinterPort"));
        etPort.setText(port);
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("MerakiPrinterQty"));
        etQty.setText(qty);

        HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();

        //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
        int sum = 0;

        for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
            Pdf417Info_SOPHOS datatmp = entry.getValue();
            int size = datatmp.getSnList().size();
            int l = (int) Math.ceil(size / 70.0);//60個SN一張
            sum += l;
        }

        int tol=sum*(Integer.parseInt(qty));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle("Meraki printer info:"+(tol)+" pcs")//   "Samsara印表機資訊 總共"+(tol)+"張"
                .setView(item)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(etIP.getText().toString())) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_id, Toast.LENGTH_LONG).show();
                            etIP.requestFocus();
                            return;
                        }

                        if (TextUtils.isEmpty(etPort.getText().toString())) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_port, Toast.LENGTH_LONG).show();
                            etPort.requestFocus();
                            return;
                        }

                        if (TextUtils.isEmpty(etQty.getText().toString()) || Integer.parseInt(etQty.getText().toString().trim()) <= 0) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (saveMerakiPrinterInfo(etIP.getText().toString().trim(), etPort.getText().toString().trim(), etQty.getText().toString().trim())) {
                            view.setEnabled(false);
                            HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();

                            //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
                            int sum = 0;

                            for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
                                Pdf417Info_SOPHOS datatmp = entry.getValue();
                                int size = datatmp.getSnList().size();
                                int l = (int) Math.ceil(size / 70.0);//35個SN一張
                                sum += l;
                            }
                            //計算總張數--end

                            //列印資料&設定張數
                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {
                                if (data.size() > 0) {
                                    int page = 0;

                                    for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {
                                        String partNo = entry.getKey();
                                        Pdf417Info_SOPHOS value = entry.getValue();
                                        AppController.debug("Server response partNo:" + partNo);
                                        AppController.debug("Server response size:" + value.getSnList().size());

//                                        AppController.debug("Server response value:" + value);
//                                        AppController.debug("Server response value1:" + value);

                                        List<String> snList = new ArrayList<>();
                                        StringBuilder stringBuilder = new StringBuilder();
                                        int cnt = 0;
                                        int cnt2 = 0;
                                        int MAX_CNT = 70;

                                        for (String sn : value.getSnList()) {
//                                        AppController.debug("Server response value3:" + sn);
                                            stringBuilder.append(sn);
                                            cnt++;
                                            cnt2++;

                                            if (cnt == MAX_CNT) { //每張最大數量
                                                cnt = 0;
                                                snList.add(stringBuilder.toString());
                                                stringBuilder.setLength(0);
                                            } else if (cnt2 == value.getSnList().size()) { //剩餘的數量
                                                snList.add(stringBuilder.toString());
                                            } else {
                                                stringBuilder.append(";");
                                            }
                                        }

                                        for (String value1 : snList) {
                                            AppController.debug("Server response value2:" + value1);
                                            AppController.debug("總頁數:" + sum);
                                            int size = value1.split(";").length;
                                            AppController.debug("總數量:" + size);
                                            page++;
                                            AppController.debug("has sn? :" + !"No Numbers".equals(value1));
                                            AppController.debug("value.getQty() :" + value.getQty());
//                                            測試資料:@575755@NA@0@86 => NA
//                                            @576970@10K@0@86 => 110*120*147

                                            if (printMerakiLabel(etIP.getText().toString().trim(), etPort.getText().toString().trim(), value, size, value1, page, sum,1)) {
                                                Toast.makeText(getApplicationContext(), getString(R.string.print_error), Toast.LENGTH_LONG).show();
                                                alertDialog.dismiss();
                                                view.setEnabled(true);
                                                return;
                                            }
                                        }
                                    }
                                } else {

                                }
                            }

                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private void returnPage() {
        finish();
    }

    public void onBackPressed() {
        // do something here and don't write super.onBackPressed()
        returnPage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ENABLE_BT || requestCode == REQUEST_PRINTER_SETTING) {
                checkPrinterSetting();
            }
        } else {
            if (requestCode == REQUEST_BLUETOOTH_SETTINGS) {
                if (BtPrintLabel.instance(getApplicationContext()) && BtPrintLabel.connect()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.bt_connect_ok), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.not_connect_btprinter), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private Dialog onCreateChooseFileServerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle(R.string.pick_file_server)
                .setItems(R.array.file_servers, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        doPrintPalletLabel(which + 1);

                        if(which==2){
                            //doPrintPalletSnInfo01();//新版走WIFI(Meraki)
                        }else if(which==3||which==4){//新版走Codesoft
                            //doPrintPalletSnInfoMeraki(which);

                        }else{
                            doPrintPalletLabel(which + 1);
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        }).setCancelable(false);

        return builder.create();
    }

    private Dialog onCreateChoosePrinterCodesoftDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer_new, null);
        final Spinner printerIP = item.findViewById(R.id.printspinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,    //對應的Context
                R.array.printersIP,                             //資料選項內容
                android.R.layout.simple_spinner_item);  //預設Spinner未展開時的View(預設及選取後樣式)
        printerIP.setAdapter(adapter);
        printerIP.setSelection(adapter.getPosition("10.0.189.136"));//TSC TX300_1F_216
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("MerakiPrinterQty"));
        etQty.setText(qty);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle(R.string.label_extrem_printer_info)
                .setView(item)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(etQty.getText().toString())) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (true) {
                            PrintDataList=new NewPrintDataList();
                            ArrayList<NewPrintData> labelArray = new ArrayList<NewPrintData>();
                            view.setEnabled(false);

                            for (ExtremePalletItem item : extremePalletInfo.getInfoList()) {
                                if (TextUtils.isEmpty(item.getCountryOfOrigin())) {
                                    item.setCountryOfOrigin("TW");
                                }

                                NewPrintData PrintData= new NewPrintData();
                                PrintData.setLabelName(AppController.getProperties("ExtremeLabelName"));
                                PrintData.setPrintName(printerIP.getSelectedItem().toString());

                                HashMap<String, String> var=new HashMap<>();
                                var.put("UniquePackID",item.getUniquePackID());
                                var.put("TriggerInfo",item.getTriggerInfo());
                                var.put("SkuPn",item.getSkuPn());
                                var.put("Qty",String.valueOf(item.getQty()));
                                var.put("SupplierCode",item.getSupplierCode());

                                if (item.getBrocadeDescription() == null) {
                                    Toast.makeText(getApplicationContext(), "Not find Extreme Description，please contact MIS", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                var.put("BrocadeDescription",item.getBrocadeDescription());
                                var.put("CountryOfOrigin",item.getCountryOfOrigin());
                                PrintData.setVariables(var);

//                                Gson gson = new GsonBuilder().create();
//                                JsonElement element = gson.toJsonTree(PrintData);
                                labelArray.add(PrintData);
                            }

                            PrintDataList.setCount(etQty.getText().toString().trim());
                            PrintDataList.setLabel(labelArray);
                            new PrintWithNewType().execute(0);
                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private Dialog onCreateChoosePrinterCodesoftDialog02() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer_new, null);
        final Spinner printerIP = item.findViewById(R.id.printspinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,    //對應的Context
                R.array.printersIP,                             //資料選項內容
                android.R.layout.simple_spinner_item);  //預設Spinner未展開時的View(預設及選取後樣式)
        printerIP.setAdapter(adapter);
        printerIP.setSelection(adapter.getPosition("10.0.189.136"));//TSC TX300_1F_216
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("MerakiPrinterQty"));
        etQty.setText(qty);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle(R.string.label_extrem_printer_info)
                .setView(item)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(etQty.getText().toString())) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (true) {
                            PrintDataList=new NewPrintDataList();
                            ArrayList<NewPrintData> labelArray = new ArrayList<NewPrintData>();
                            view.setEnabled(false);

                            for (ExtremePalletItem item : extremePalletInfo.getInfoList()) {
                                if (TextUtils.isEmpty(item.getCountryOfOrigin())) {
                                    item.setCountryOfOrigin("TW");
                                }

                                NewPrintData PrintData= new NewPrintData();
                                PrintData.setLabelName(AppController.getProperties("ExtremeLabelName02"));
                                PrintData.setPrintName(printerIP.getSelectedItem().toString());

                                HashMap<String, String> var=new HashMap<>();
                                var.put("UniquePackID",item.getUniquePackID());
                                var.put("TriggerInfo",item.getTriggerInfo());
                                var.put("SkuPn",item.getSkuPn());
                                var.put("Qty",String.valueOf(item.getQty()));
                                var.put("SupplierCode",item.getSupplierCode());

                                if (item.getBrocadeDescription() == null) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.Not_find_Extreme_description), Toast.LENGTH_LONG).show();
                                    return;

                                }

                                var.put("BrocadeDescription",item.getBrocadeDescription());
                                var.put("CountryOfOrigin",item.getCountryOfOrigin());
                                PrintData.setVariables(var);

//                                Gson gson = new GsonBuilder().create();
//                                JsonElement element = gson.toJsonTree(PrintData);

                                labelArray.add(PrintData);
                            }

                            PrintDataList.setCount(etQty.getText().toString().trim());
                            PrintDataList.setLabel(labelArray);
                            new PrintWithNewType02().execute(0);
                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private Dialog onCreateChoosePrinterDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer, null);
        final EditText etIP = item.findViewById(R.id.editTextIP);
        String ip = Preferences.getSharedPreferences(this).getString(
                Preferences.EXTREME_PRINTER_IP, AppController.getProperties("ExtremePrinterIP"));
        etIP.setText(ip);
        final EditText etPort = item.findViewById(R.id.editTextPort);
        String port = Preferences.getSharedPreferences(this).getString(
                Preferences.EXTREME_PRINTER_PORT, AppController.getProperties("ExtremePrinterPort"));
        etPort.setText(port);
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.EXTREME_PRINTER_QTY, AppController.getProperties("ExtremePrinterQty"));
        etQty.setText(qty);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle(R.string.label_extrem_printer_info)
                .setView(item)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(etIP.getText().toString())) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_id, Toast.LENGTH_LONG).show();
                            etIP.requestFocus();
                            return;
                        }

                        if (TextUtils.isEmpty(etPort.getText().toString())) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_port, Toast.LENGTH_LONG).show();
                            etPort.requestFocus();
                            return;
                        }

                        if (TextUtils.isEmpty(etQty.getText().toString())) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (savePrinterInfo(etIP.getText().toString().trim(), etPort.getText().toString().trim(), etQty.getText().toString().trim())) {
                            view.setEnabled(false);

                            for (ExtremePalletItem item : extremePalletInfo.getInfoList()) {
                                if (TextUtils.isEmpty(item.getCountryOfOrigin())) {
                                    item.setCountryOfOrigin("TW");
                                }

                                if (!printExtremeLabel(etIP.getText().toString().trim(), etPort.getText().toString().trim(), etQty.getText().toString().trim(), item)) {
                                    alertDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), getString(R.string.print_error), Toast.LENGTH_LONG).show();
                                    view.setEnabled(true);
                                    return;
                                }
                            }

                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private Dialog onCreateChooseSamsaraPrinterDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer, null);
        final EditText etIP = item.findViewById(R.id.editTextIP);
        String ip = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_IP, AppController.getProperties("SophosPrinterIP"));
        etIP.setText(ip);
        final EditText etPort = item.findViewById(R.id.editTextPort);
        String port = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_PORT, AppController.getProperties("SophosPrinterPort"));
        etPort.setText(port);
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("SophosPrinterQty"));
        etQty.setText(qty);

        HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();

        //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
        int sum = 0;

        for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
            Pdf417Info_SOPHOS datatmp = entry.getValue();
            int size = datatmp.getSnList().size();
            int l = (int) Math.ceil(size / 60.0);//35個SN一張
            sum += l;
        }

        int tol=sum*(Integer.parseInt(qty));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle(getString(R.string.Samsara_printer_info_total) + (1) + getString(R.string.printer_info_total_pages)) //"Samsara印表機資訊 總共"+(tol)+"張"
                .setView(item)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(etIP.getText().toString())) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_id, Toast.LENGTH_LONG).show();
                            etIP.requestFocus();
                            return;
                        }

                        if (TextUtils.isEmpty(etPort.getText().toString())) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_port, Toast.LENGTH_LONG).show();
                            etPort.requestFocus();
                            return;
                        }

                        if (TextUtils.isEmpty(etQty.getText().toString()) || Integer.parseInt(etQty.getText().toString().trim()) <= 0) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (saveSophosPrinterInfo(etIP.getText().toString().trim(), etPort.getText().toString().trim(), etQty.getText().toString().trim())) {
                            view.setEnabled(false);
                            HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();

                            //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
                            int sum = 0;

                            for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
                                Pdf417Info_SOPHOS datatmp = entry.getValue();
                                int size = datatmp.getSnList().size();
                                int l = (int) Math.ceil(size / 60.0);//35個SN一張
                                sum += l;
                            }
                            //計算總張數--end

                            //列印資料&設定張數
                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {
                                if (data.size() > 0) {
                                    int page = 0;

                                    for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {
                                        String partNo = entry.getKey();
                                        Pdf417Info_SOPHOS value = entry.getValue();
                                        AppController.debug("Server response partNo:" + partNo);
                                        AppController.debug("Server response size:" + value.getSnList().size());

//                                        AppController.debug("Server response value:" + value);
//                                        AppController.debug("Server response value1:" + value);

                                        List<String> snList = new ArrayList<>();
                                        StringBuilder stringBuilder = new StringBuilder();
                                        int cnt = 0;
                                        int cnt2 = 0;
                                        int MAX_CNT = 60;

                                        for (String sn : value.getSnList()) {
//                                            AppController.debug("Server response value3:" + sn);
                                            stringBuilder.append(sn);
                                            cnt++;
                                            cnt2++;

                                            if (cnt == MAX_CNT) { //每張最大數量
                                                cnt = 0;
                                                snList.add(stringBuilder.toString());
                                                stringBuilder.setLength(0);
                                            } else if (cnt2 == value.getSnList().size()) { //剩餘的數量
                                                snList.add(stringBuilder.toString());
                                            } else {
                                                stringBuilder.append(",");
                                            }
                                        }

                                        for (String value1 : snList) {
                                            AppController.debug("Server response value2:" + value1);
                                            AppController.debug("總頁數:" + sum);
                                            int size = value1.split(",").length;
                                            AppController.debug("總數量:" + size);
                                            page++;
                                            AppController.debug("has sn? :" + !"No Numbers".equals(value1));
                                            AppController.debug("value.getQty() :" + value.getQty());

                                            if (printSamsaraLabel(etIP.getText().toString().trim(), etPort.getText().toString().trim(), value, size, value1, page, sum,1)) {
                                                Toast.makeText(getApplicationContext(), getString(R.string.print_error), Toast.LENGTH_LONG).show();
                                                alertDialog.dismiss();
                                                view.setEnabled(true);
                                                return;
                                            }
                                        }
                                    }
                                } else {

                                }
                            }

//                            for (String sn : palletSnInfo.getSnInfo()) {
//                                stringBuilder.append(sn);
//                                cnt++;
//                                cnt2++;

//                                if (cnt == MAX_CNT) {
//                                    cnt = 0;
//                                    snList.add(stringBuilder.toString());
//                                    stringBuilder.setLength(0);
//                                } else if (cnt2 == palletSnInfo.getSnInfo().length) {
//                                    snList.add(stringBuilder.toString());
//                                } else {
//                                    stringBuilder.append("\r\n");
//                                }
//                            }

//                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {
//                                for (String value : snList) {
//                                    int size = value.split("\r\n").length;
//                                    String[] date = value.split("\r\n");
//                                    int l = (int)Math.ceil(size/35.0);//35個SN一張

//                                    if (printSophosLabel(etIP.getText().toString().trim(), etPort.getText().toString().trim(), palletSnInfo, size, value)) {
//                                        Toast.makeText(getApplicationContext(), getString(R.string.print_error), Toast.LENGTH_LONG).show();
//                                        alertDialog.dismiss();
//                                        view.setEnabled(true);
//                                        return;
//                                    }
//                                }
//                            }

                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private Dialog onCreateChooseVerkadaCodesoftLotcodePrinterDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer_new, null);
        final Spinner printerIP = item.findViewById(R.id.printspinner);
        ArrayAdapter <CharSequence> adapter = ArrayAdapter.createFromResource(this,    //對應的Context
                R.array.printersIP,                             //資料選項內容
                android.R.layout.simple_spinner_item);  //預設Spinner未展開時的View(預設及選取後樣式)
        printerIP.setAdapter(adapter);
        printerIP.setSelection(adapter.getPosition("10.0.162.209"));//TSC TX300_1F_217
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("SophosPrinterQty"));
        etQty.setText(qty);

        final Set<String> lotSet = palletSnInfo.getLotCode();

        for(String lotCode : lotSet){
            AppController.debug("Lotcode ===>:" + lotCode);
        }

        final int tol=(lotSet.size()/2+1)*(Integer.parseInt(qty));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle(getString(R.string.Verkada_LotCode_printer_info_total) + (tol) + getString(R.string.printer_info_total_pages))//R.string.label_samsara_printer_info+
                .setView(item)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(etQty.getText().toString()) || Integer.parseInt(etQty.getText().toString().trim()) <= 0) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (true) {
                            view.setEnabled(false);
                            HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();
//                            Set<String> lotSet = palletSnInfo.getLotCode();

                            List<String> list = new ArrayList<>();

                            for(String lotCode : lotSet){
                                AppController.debug("Lotcode ===>:" + lotCode);
                                list.add(lotCode);
                            }

                            int lotSum = lotSet.size()/2+1;
                            PrintDataList=new NewPrintDataList();
                            ArrayList<NewPrintData> labelArray = new ArrayList<NewPrintData>();
                            //列印資料&設定張數
//                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {
                            AppController.debug("etQty ===>:" + Integer.parseInt(etQty.getText().toString().trim()));

                            for(int j = 0; j < list.size();j+=2){
                                String st1 = list.get(j);
                                AppController.debug("Lotcode00 ===>:" + st1);
                                String st2 = "";

                                if(j+1 < list.size()){
                                    st2 = list.get(j+1);
                                    AppController.debug("Lotcode11 ===>:" + st2);
                                }

                                NewPrintData PrintData= new NewPrintData();
                                PrintData.setLabelName(AppController.getProperties("VercadaLotcodeLabelName"));
                                PrintData.setPrintName(printerIP.getSelectedItem().toString());
                                HashMap<String, String> Var=new HashMap<>();
                                Var.put("Lotcode1",st1);
                                Var.put("Lotcode2",st2);
                                PrintData.setVariables(Var);
//                                Gson gson = new GsonBuilder().create();
//                                JsonElement element = gson.toJsonTree(PrintData);
                                labelArray.add(PrintData);
                            }

//                            }

                            PrintDataList.setCount(etQty.getText().toString().trim());
                            PrintDataList.setLabel(labelArray);
                            new PrintWithNewType().execute(0);
                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private Dialog onCreateChooseVerkadaCodesoftLotcodePrinterDialog02() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer_new, null);
        final Spinner printerIP = item.findViewById(R.id.printspinner);
        ArrayAdapter <CharSequence> adapter = ArrayAdapter.createFromResource(this,    //對應的Context
                R.array.printersIP,                             //資料選項內容
                android.R.layout.simple_spinner_item);  //預設Spinner未展開時的View(預設及選取後樣式)
        printerIP.setAdapter(adapter);
        printerIP.setSelection(adapter.getPosition("10.0.162.209"));//TSC TX300_1F_217
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("SophosPrinterQty"));
        etQty.setText(qty);
        final Set<String> lotSet = palletSnInfo.getLotCode();

        for(String lotCode : lotSet){
            AppController.debug("Lotcode ===>:" + lotCode);
        }

        final int tol=(lotSet.size()/2+1)*(Integer.parseInt(qty));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the dialog title
        builder.setTitle(getString(R.string.Verkada_LotCode_printer_info_total) + (tol) + getString(R.string.printer_info_total_pages))//R.string.label_samsara_printer_info+
                .setView(item)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(etQty.getText().toString()) || Integer.parseInt(etQty.getText().toString().trim()) <= 0) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (true) {
                            view.setEnabled(false);
                            HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();
//                            Set<String> lotSet = palletSnInfo.getLotCode();

                            List<String> list = new ArrayList<>();

                            for(String lotCode : lotSet){
                                AppController.debug("Lotcode ===>:" + lotCode);
                                list.add(lotCode);
                            }

                            int lotSum = lotSet.size()/2+1;
                            PrintDataList=new NewPrintDataList();
                            ArrayList<NewPrintData> labelArray = new ArrayList<NewPrintData>();
                            //列印資料&設定張數
//                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {
                            AppController.debug("etQty ===>:" + Integer.parseInt(etQty.getText().toString().trim()));

                            for(int j = 0; j < list.size();j+=2){
                                String st1 = list.get(j);
                                AppController.debug("Lotcode00 ===>:" + st1);
                                String st2 = "";

                                if(j+1 < list.size()){
                                    st2 = list.get(j+1);
                                    AppController.debug("Lotcode11 ===>:" + st2);
                                }

                                NewPrintData PrintData= new NewPrintData();
                                PrintData.setLabelName(AppController.getProperties("VercadaLotcodeLabelName02"));
                                PrintData.setPrintName(printerIP.getSelectedItem().toString());
                                HashMap<String, String> Var=new HashMap<>();
                                Var.put("Lotcode1",st1);
                                Var.put("Lotcode2",st2);
                                PrintData.setVariables(Var);
//                                Gson gson = new GsonBuilder().create();
//                                JsonElement element = gson.toJsonTree(PrintData);
                                labelArray.add(PrintData);
                            }
//                            }

                            PrintDataList.setCount(etQty.getText().toString().trim());
                            PrintDataList.setLabel(labelArray);
//                            new PrintWithNewType().execute(0);
                            new PrintWithNewType02().execute(0);
                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private Dialog onCreateChooseVerkadaLotcodePrinterDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer, null);
        final EditText etIP = item.findViewById(R.id.editTextIP);
        String ip = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_IP, AppController.getProperties("SophosPrinterIP"));
        etIP.setText(ip);
        final EditText etPort = item.findViewById(R.id.editTextPort);
        String port = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_PORT, AppController.getProperties("SophosPrinterPort"));
        etPort.setText(port);
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("SophosPrinterQty"));
        etQty.setText(qty);
        final Set<String> lotSet = palletSnInfo.getLotCode();

        for(String lotCode : lotSet){
            AppController.debug("Lotcode ===>:" + lotCode);
        }

        final int tol=(lotSet.size()/2+1)*(Integer.parseInt(qty));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the dialog title
        builder.setTitle(getString(R.string.Verkada_LotCode_printer_info_total) + (tol) + getString(R.string.printer_info_total_pages))//R.string.label_samsara_printer_info+
                .setView(item)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(etIP.getText().toString())) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_id, Toast.LENGTH_LONG).show();
                            etIP.requestFocus();
                            return;
                        }

                        if (TextUtils.isEmpty(etPort.getText().toString())) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_port, Toast.LENGTH_LONG).show();
                            etPort.requestFocus();
                            return;
                        }

                        if (TextUtils.isEmpty(etQty.getText().toString()) || Integer.parseInt(etQty.getText().toString().trim()) <= 0) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (saveSophosPrinterInfo(etIP.getText().toString().trim(), etPort.getText().toString().trim(), etQty.getText().toString().trim())) {
                            view.setEnabled(false);
                            HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();
//                            Set<String> lotSet = palletSnInfo.getLotCode();

                            List<String> list = new ArrayList<>();

                            for(String lotCode : lotSet){
                                AppController.debug("Lotcode ===>:" + lotCode);
                                list.add(lotCode);
                            }

                            int lotSum = lotSet.size()/2+1;

                            //列印資料&設定張數
                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {
//                                String[] st = (String[]) lotSet.toArray();

//                                for(String lotCode : lotSet){
//                                    AppController.debug("Lotcode ===>:" + lotCode);
//                                }

                                for(int j = 0; j < list.size();j+=2){
                                    String st1 = list.get(j);
                                    AppController.debug("Lotcode00 ===>:" + st1);
                                    String st2 = "";

                                    if(j+1 < list.size()){
                                        st2 = list.get(j+1);
                                        AppController.debug("Lotcode11 ===>:" + st2);
                                    }

                                    if (printVerkadaLotcodeLabel(etIP.getText().toString().trim(), etPort.getText().toString().trim(), lotSum,st1,st2,lotSet)) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.print_error), Toast.LENGTH_LONG).show();
                                        alertDialog.dismiss();
                                        view.setEnabled(true);
                                        return;
                                    }
                                }
                            }

                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private Dialog onCreateChooseVerkadaCodesoftPrinterDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer_new, null);
        final Spinner printerIP = item.findViewById(R.id.printspinner);
        ArrayAdapter <CharSequence> adapter = ArrayAdapter.createFromResource(this,    //對應的Context
                R.array.printersIP,                             //資料選項內容
                android.R.layout.simple_spinner_item);  //預設Spinner未展開時的View(預設及選取後樣式)
        printerIP.setAdapter(adapter);
        printerIP.setSelection(adapter.getPosition("10.0.189.136"));//TSC TX300_1F_217 10.0.162.209
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("SophosPrinterQty"));
        etQty.setText(qty);
        HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();
        final double everyPageSize=72.0;//72個SN一張
        final int everyPageSize1=72;//72個SN一張
        int sum = 0;

        for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
            Pdf417Info_SOPHOS datatmp = entry.getValue();
            int size = datatmp.getSnList().size();
            int l = (int) Math.ceil(size / everyPageSize);//60個SN一張
            sum += l;
        }

        AppController.debug("印出張數 sum:" + sum);
        int tol=sum*(Integer.parseInt(qty));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the dialog title
        builder.setTitle(getString(R.string.Verkada_Model_SN_printer_info_total) + (tol) + getString(R.string.printer_info_total_pages))//R.string.label_samsara_printer_info+
                .setView(item)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(etQty.getText().toString()) || Integer.parseInt(etQty.getText().toString().trim()) <= 0) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (true) {
                            view.setEnabled(false);
                            HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();
                            Set<String> lotSet = palletSnInfo.getLotCode();

                            for(String lotCode : lotSet){
                                AppController.debug("Lotcode ===>:" + lotCode);
                            }

                            //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
                            int sum = 0;

                            for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
                                Pdf417Info_SOPHOS datatmp = entry.getValue();
                                int size = datatmp.getSnList().size();
                                int l = (int) Math.ceil(size / everyPageSize);//72個SN一張
                                sum += l;
                            }
                            //計算總張數--end

                            //列印資料&設定張數
                            PrintDataList=new NewPrintDataList();
                            ArrayList<NewPrintData> labelArray = new ArrayList<NewPrintData>();
//                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {

                            if (data.size() > 0) {
                                int page = 0;

                                for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//根據每個料號做處理 第一個String 是料號
                                    String partNo = entry.getKey();
                                    Pdf417Info_SOPHOS value = entry.getValue();
                                    AppController.debug("Server response partNo:" + partNo);
                                    AppController.debug("Server response size:" + value.getSnList().size());
//                                        AppController.debug("Server response value:" + value);
                                    List<String> snList = new ArrayList<>();
                                    StringBuilder stringBuilder = new StringBuilder();
                                    int cnt = 0;
                                    int cnt2 = 0;
                                    int MAX_CNT = everyPageSize1;

                                    for (String sn : value.getSnList()) {//整理每個料號裡面的SN
//                                        AppController.debug("Server response value3:" + sn);
                                        stringBuilder.append(sn);
                                        cnt++;
                                        cnt2++;

                                        if (cnt == MAX_CNT) {//每張最大數量
                                            cnt = 0;
                                            snList.add(stringBuilder.toString());
                                            stringBuilder.setLength(0);
                                        } else if (cnt2 == value.getSnList().size()) {//剩餘的數量
                                            snList.add(stringBuilder.toString());
                                        } else {
                                            stringBuilder.append(",");
                                        }
                                    }

                                    AppController.debug("總頁數:" + sum);

                                    for (String value1 : snList) {
                                        AppController.debug("Server response value2:" + value1);
                                        int size = value1.split(",").length;
                                        AppController.debug("每張標籤數量:" + size);
                                        page++;
                                        NewPrintData PrintData;

                                        if(page==1){
                                            PrintData= new NewPrintData();
                                            PrintData.setLabelName(AppController.getProperties("VercadaSNLabelName"));
                                            PrintData.setPrintName(printerIP.getSelectedItem().toString());
                                            HashMap<String, String> Var=new HashMap<>();
                                            Var.put("ModelNum",value.getCustomerPartNoDescription());
                                            Var.put("PNum",value.getCustomerPartNo());
                                            Var.put("QTY",String.valueOf(value.getSnList().size()));
                                            PrintData.setVariables(Var);
//                                                Gson gson = new GsonBuilder().create();
//                                                JsonElement element = gson.toJsonTree(PrintData);
                                            labelArray.add(PrintData);
                                        }

                                        AppController.debug("has sn? :" + !"No Numbers".equals(value1));
                                        AppController.debug("value.getQty() :" + value.getQty()+" ,value.getSnList().size()="+value.getSnList().size());
                                        PrintData= new NewPrintData();
                                        PrintData.setLabelName(AppController.getProperties("VercadaSNListLabelName"));
//                                        PrintData.setLabelName(AppController.getProperties("VercadaSNLabelName"));
                                        PrintData.setPrintName(printerIP.getSelectedItem().toString());
                                        HashMap<String, String> Var=new HashMap<>();
                                        Var.put("SNum",value.getModelName()+";"+Integer.toString(size)+";"+value1);
                                        Var.put("PagePalletNum",(page) + "/" + sum+"-"+value.getPalletNo());

                                        if (page==1)
                                            Var.put("SNIndex","1 - "+size);
                                        else{
                                            int sSnNum=((page-1)*72+1);
                                            Var.put("SNIndex",sSnNum+" - "+(sSnNum+size-1));
                                        }

                                        PrintData.setVariables(Var);
//                                        Gson gson = new GsonBuilder().create();
//                                        JsonElement element = gson.toJsonTree(PrintData);
                                        labelArray.add(PrintData);
                                    }
                                }
                            } else {

                            }
//                            }

                            PrintDataList.setCount(etQty.getText().toString().trim());
                            PrintDataList.setLabel(labelArray);
                            new PrintWithNewType().execute(0);
                            Toast.makeText(getApplicationContext(), getString(R.string.Print_Start), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private Dialog onCreateChooseVerkadaCodesoftPrinterDialog02() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer_new, null);
        final Spinner printerIP = item.findViewById(R.id.printspinner);
        ArrayAdapter <CharSequence> adapter = ArrayAdapter.createFromResource(this,    //對應的Context
                R.array.printersIP,                             //資料選項內容
                android.R.layout.simple_spinner_item);  //預設Spinner未展開時的View(預設及選取後樣式)
        printerIP.setAdapter(adapter);
        printerIP.setSelection(adapter.getPosition("10.0.189.136"));//TSC TX300_1F_217 10.0.162.209
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("SophosPrinterQty"));
        etQty.setText(qty);
        HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();
        final double everyPageSize=72.0;//72個SN一張
        final int everyPageSize1=72;//72個SN一張
        int sum = 0;

        for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
            Pdf417Info_SOPHOS datatmp = entry.getValue();
            int size = datatmp.getSnList().size();
            int l = (int) Math.ceil(size / everyPageSize);//60個SN一張
            sum += l;
        }

        AppController.debug("印出張數 sum:" + sum);
        int tol=sum*(Integer.parseInt(qty));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the dialog title
        builder.setTitle(getString(R.string.Verkada_Model_SN_printer_info_total) + (tol) + getString(R.string.printer_info_total_pages))//R.string.label_samsara_printer_info+
                .setView(item)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(etQty.getText().toString()) || Integer.parseInt(etQty.getText().toString().trim()) <= 0) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (true) {
                            view.setEnabled(false);
                            HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();
                            Set<String> lotSet = palletSnInfo.getLotCode();

                            for(String lotCode : lotSet){
                                AppController.debug("Lotcode ===>:" + lotCode);
                            }

                            //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
                            int sum = 0;

                            for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
                                Pdf417Info_SOPHOS datatmp = entry.getValue();
                                int size = datatmp.getSnList().size();
                                int l = (int) Math.ceil(size / everyPageSize);//72個SN一張
                                sum += l;
                            }
                            //計算總張數--end

                            //列印資料&設定張數
                            PrintDataList=new NewPrintDataList();
                            ArrayList<NewPrintData> labelArray = new ArrayList<NewPrintData>();
//                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {

                            if (data.size() > 0) {
                                int page = 0;

                                for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//根據每個料號做處理 第一個String 是料號
                                    String partNo = entry.getKey();
                                    Pdf417Info_SOPHOS value = entry.getValue();
                                    AppController.debug("Server response partNo:" + partNo);
                                    AppController.debug("Server response size:" + value.getSnList().size());
//                                    AppController.debug("Server response value:" + value);
                                    List<String> snList = new ArrayList<>();
                                    StringBuilder stringBuilder = new StringBuilder();
                                    int cnt = 0;
                                    int cnt2 = 0;
                                    int MAX_CNT = everyPageSize1;

                                    for (String sn : value.getSnList()) {//整理每個料號裡面的SN
//                                        AppController.debug("Server response value3:" + sn);
                                        stringBuilder.append(sn);
                                        cnt++;
                                        cnt2++;

                                        if (cnt == MAX_CNT) {//每張最大數量
                                            cnt = 0;
                                            snList.add(stringBuilder.toString());
                                            stringBuilder.setLength(0);
                                        } else if (cnt2 == value.getSnList().size()) {//剩餘的數量
                                            snList.add(stringBuilder.toString());
                                        } else {
                                            stringBuilder.append(",");
                                        }
                                    }

                                    AppController.debug("總頁數:" + sum);

                                    for (String value1 : snList) {
                                        AppController.debug("Server response value2:" + value1);
                                        int size = value1.split(",").length;
                                        AppController.debug("每張標籤數量:" + size);
                                        page++;
                                        NewPrintData PrintData;

                                        if(page==1){
                                            PrintData= new NewPrintData();
                                            PrintData.setLabelName(AppController.getProperties("VercadaSNLabelName02"));
                                            PrintData.setPrintName(printerIP.getSelectedItem().toString());
                                            HashMap<String, String> Var=new HashMap<>();
                                            Var.put("ModelNum",value.getCustomerPartNoDescription());
                                            Var.put("PNum",value.getCustomerPartNo());
                                            Var.put("QTY",String.valueOf(value.getSnList().size()));
                                            PrintData.setVariables(Var);
//                                                Gson gson = new GsonBuilder().create();
//                                                JsonElement element = gson.toJsonTree(PrintData);
                                            labelArray.add(PrintData);
                                        }

                                        AppController.debug("has sn? :" + !"No Numbers".equals(value1));
                                        AppController.debug("value.getQty() :" + value.getQty()+" ,value.getSnList().size()="+value.getSnList().size());
                                        PrintData= new NewPrintData();
                                        PrintData.setLabelName(AppController.getProperties("VercadaSNListLabelName02"));
//                                        PrintData.setLabelName(AppController.getProperties("VercadaSNLabelName02"));
                                        PrintData.setPrintName(printerIP.getSelectedItem().toString());
                                        HashMap<String, String> Var=new HashMap<>();
                                        Var.put("SNum",value.getModelName()+";"+Integer.toString(size)+";"+value1);
                                        Var.put("PagePalletNum",(page) + "/" + sum+"-"+value.getPalletNo());

                                        if (page==1)
                                            Var.put("SNIndex","1 - "+size);
                                        else{
                                            int sSnNum=((page-1)*72+1);
                                            Var.put("SNIndex",sSnNum+" - "+(sSnNum+size-1));
                                        }

                                        PrintData.setVariables(Var);
//                                        Gson gson = new GsonBuilder().create();
//                                        JsonElement element = gson.toJsonTree(PrintData);
                                        labelArray.add(PrintData);
                                    }
                                }
                            } else {

                            }
//                            }

                            PrintDataList.setCount(etQty.getText().toString().trim());
                            PrintDataList.setLabel(labelArray);
                            //ew PrintWithNewType().execute(0);
                            new PrintWithNewType02().execute(0);
                            Toast.makeText(getApplicationContext(), getString(R.string.Print_Start), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private Dialog onCreateChooseVerkadaPrinterDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer, null);
        final EditText etIP = item.findViewById(R.id.editTextIP);
        String ip = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_IP, AppController.getProperties("SophosPrinterIP"));
        etIP.setText(ip);
        final EditText etPort = item.findViewById(R.id.editTextPort);
        String port = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_PORT, AppController.getProperties("SophosPrinterPort"));
        etPort.setText(port);
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("SophosPrinterQty"));
        etQty.setText(qty);
        HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();
        final double everyPageSize=72.0;//72個SN一張
        final int everyPageSize1=72;//72個SN一張
        int sum = 0;

        for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
            Pdf417Info_SOPHOS datatmp = entry.getValue();
//            List<String> snL = new ArrayList<String>();
//            for(int i=0;i<360;i++){
//                snL.add("xxx");
//            }
//            datatmp.setSnList(snL);
//            AppController.debug("===>:" + datatmp.getCustomerPartNoDescription());
            int size = datatmp.getSnList().size();
            int l = (int) Math.ceil(size / everyPageSize);//60個SN一張
            sum += l;
        }

        AppController.debug("印出張數 sum:" + sum);
        int tol=sum*(Integer.parseInt(qty));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle(getString(R.string.Verkada_Model_SN_printer_info_total) + (tol) + getString(R.string.printer_info_total_pages))//R.string.label_samsara_printer_info+
                .setView(item)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(etIP.getText().toString())) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_id, Toast.LENGTH_LONG).show();
                            etIP.requestFocus();
                            return;
                        }

                        if (TextUtils.isEmpty(etPort.getText().toString())) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_port, Toast.LENGTH_LONG).show();
                            etPort.requestFocus();
                            return;
                        }

                        if (TextUtils.isEmpty(etQty.getText().toString()) || Integer.parseInt(etQty.getText().toString().trim()) <= 0) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (saveSophosPrinterInfo(etIP.getText().toString().trim(), etPort.getText().toString().trim(), etQty.getText().toString().trim())) {
                            view.setEnabled(false);
                            HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();
                            Set<String> lotSet = palletSnInfo.getLotCode();

                            for(String lotCode : lotSet){
                                AppController.debug("Lotcode ===>:" + lotCode);
                            }

                            //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
                            int sum = 0;

                            for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
                                Pdf417Info_SOPHOS datatmp = entry.getValue();
                                int size = datatmp.getSnList().size();
                                int l = (int) Math.ceil(size / everyPageSize);//72個SN一張
                                sum += l;
                            }
                            //計算總張數--end

                            //列印資料&設定張數
                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {
                                if (data.size() > 0) {
                                    int page = 0;

                                    for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//根據每個料號做處理 第一個String 是料號
                                        String partNo = entry.getKey();
                                        Pdf417Info_SOPHOS value = entry.getValue();
                                        AppController.debug("Server response partNo:" + partNo);
                                        AppController.debug("Server response size:" + value.getSnList().size());
//                                        AppController.debug("Server response value:" + value);
                                        List<String> snList = new ArrayList<>();
                                        StringBuilder stringBuilder = new StringBuilder();
                                        int cnt = 0;
                                        int cnt2 = 0;
                                        int MAX_CNT = everyPageSize1;

                                        for (String sn : value.getSnList()) {//整理每個料號裡面的SN
//                                        AppController.debug("Server response value3:" + sn);
                                            stringBuilder.append(sn);
                                            cnt++;
                                            cnt2++;

                                            if (cnt == MAX_CNT) {//每張最大數量
                                                cnt = 0;
                                                snList.add(stringBuilder.toString());
                                                stringBuilder.setLength(0);
                                            } else if (cnt2 == value.getSnList().size()) {//剩餘的數量
                                                snList.add(stringBuilder.toString());
                                            } else {
                                                stringBuilder.append(",");
                                            }
                                        }

                                        AppController.debug("總頁數:" + sum);

                                        for (String value1 : snList) {
                                            AppController.debug("Server response value2:" + value1);
                                            int size = value1.split(",").length;
                                            AppController.debug("每張標籤數量:" + size);
                                            page++;
                                            AppController.debug("has sn? :" + !"No Numbers".equals(value1));
                                            AppController.debug("value.getQty() :" + value.getQty()+" ,value.getSnList().size()="+value.getSnList().size());

                                            if (printVerkadaLabel(etIP.getText().toString().trim(), etPort.getText().toString().trim(), value, size, value1, page, sum,lotSet)) {
                                                Toast.makeText(getApplicationContext(), getString(R.string.print_error), Toast.LENGTH_LONG).show();
                                                alertDialog.dismiss();
                                                view.setEnabled(true);
                                                return;
                                            }
                                        }
                                    }
                                } else {

                                }
                            }

//                            for (String sn : palletSnInfo.getSnInfo()) {
//                                stringBuilder.append(sn);
//                                cnt++;
//                                cnt2++;

//                                if (cnt == MAX_CNT) {
//                                    cnt = 0;
//                                    snList.add(stringBuilder.toString());
//                                    stringBuilder.setLength(0);
//                                } else if (cnt2 == palletSnInfo.getSnInfo().length) {
//                                    snList.add(stringBuilder.toString());
//                                } else {
//                                    stringBuilder.append("\r\n");
//                                }
//                            }

//                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {
//                                for (String value : snList) {
//                                    int size = value.split("\r\n").length;
//                                    String[] date = value.split("\r\n");
//                                    int l = (int)Math.ceil(size/35.0);//35個SN一張

//                                    if (printSophosLabel(etIP.getText().toString().trim(), etPort.getText().toString().trim(), palletSnInfo, size, value)) {
//                                        Toast.makeText(getApplicationContext(), getString(R.string.print_error), Toast.LENGTH_LONG).show();
//                                        alertDialog.dismiss();
//                                        view.setEnabled(true);
//                                        return;
//                                    }
//                                }
//                            }

                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private Dialog onCreateChooseSophosPrinterCodesoftDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer_new, null);
        final Spinner printerIP = item.findViewById(R.id.printspinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,    //對應的Context
                R.array.printersIP,                             //資料選項內容
                android.R.layout.simple_spinner_item);  //預設Spinner未展開時的View(預設及選取後樣式)
        printerIP.setAdapter(adapter);
        printerIP.setSelection(adapter.getPosition("10.0.162.209"));//TSC TX300_1F_217
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("MerakiPrinterQty"));
        etQty.setText(qty);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the dialog title
        builder.setTitle(R.string.label_sophos_printer_info)
                .setView(item)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(etQty.getText().toString()) || Integer.parseInt(etQty.getText().toString().trim()) <= 0) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (true) {
                            view.setEnabled(false);
                            HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();

                            //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
                            int sum = 0;

                            for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
                                Pdf417Info_SOPHOS datatmp = entry.getValue();
                                if (datatmp.getPartNo().trim().indexOf("1") == 0 || datatmp.getPartNo().trim().indexOf("0") == 0) {//料號必須是09或14開頭
                                    int size = datatmp.getSnList().size();
                                    int l = (int) Math.ceil(size / 35.0);//35個SN一張
                                    sum += l;
                                }
                            }
                            //計算總張數--end

                            //列印資料&設定張數
//                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {
                            if (data.size() > 0) {
                                int page = 0;
                                PrintDataList=new NewPrintDataList();
                                ArrayList<NewPrintData> labelArray = new ArrayList<NewPrintData>();

                                for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {
                                    String partNo = entry.getKey();
                                    Pdf417Info_SOPHOS value = entry.getValue();
                                    AppController.debug("Server response partNo:" + partNo);

                                    if (partNo.trim().indexOf("1") == 0 || partNo.trim().indexOf("0") == 0) {
                                        List<String> snList = new ArrayList<>();
                                        StringBuilder stringBuilder = new StringBuilder();
                                        int cnt = 0;
                                        int cnt2 = 0;
                                        int MAX_CNT = 35;

                                        for (String sn : value.getSnList()) {
//                                        AppController.debug("Server response value3:" + sn);
                                            stringBuilder.append(sn);
                                            cnt++;
                                            cnt2++;

                                            if (cnt == MAX_CNT) {
                                                cnt = 0;
                                                snList.add(stringBuilder.toString()+".");
                                                stringBuilder.setLength(0);
                                            } else if (cnt2 == value.getSnList().size()) {
                                                snList.add(stringBuilder.toString()+".");
                                            } else {
                                                stringBuilder.append(".");
//                                                stringBuilder.append("\r\n");
                                            }
                                        }

                                        for (String value1 : snList) {
                                            AppController.debug("Server response value2:" + value1);
                                            AppController.debug("總頁數:" + sum);
                                            int size = value1.split("\\.").length;
//                                            int size = value1.split("\r\n").length;
                                            AppController.debug("總數量:" + size);
                                            page++;
                                            AppController.debug("has sn? :" + !"No Numbers".equals(value1));
                                            AppController.debug("value.getQty() :" + value.getQty());
                                            HashMap<String, String> printInfo=new HashMap<>();
                                            NewPrintData PrintData= new NewPrintData();

                                            if(AppController.getProperties("Sophos_PN_No_SN").indexOf(value.getPartNo()) >= 0){
                                                PrintData.setLabelName(AppController.getProperties("SophosLabelName_No_SN"));
                                            }else{
                                                PrintData.setLabelName(AppController.getProperties("SophosLabelName"));
                                            }
                                            //PrintData.setLabelName(AppController.getProperties("SophosLabelName")); //20250321 Ann(SENAO101-202502-0034):因Sophos要求部分料號不要顯示SERIAL NO.字樣
                                            PrintData.setPrintName(printerIP.getSelectedItem().toString());

                                            HashMap<String, String> var=new HashMap<>();
                                            var.put("InvoiceNo",palletSnInfoSophos.getInvoiceNo());
                                            var.put("PN",value.getCustomerPartNo());
                                            var.put("PALLET",value.getPalletNo());
                                            var.put("PAGE",page+"/"+ sum);
                                            boolean isPak ="No Numbers".equals(value1) &&  value.getPartNo().trim().indexOf("0")==0  ;

                                            if(!isPak) {
                                                var.put("SerialNo",value1);
                                                var.put("QTY",String.valueOf(size));
                                            }else{
                                                var.put("QTY",String.valueOf(value.getQty()));
                                                var.put("SerialNo","");
                                            }

                                            var.put("smallTag",smallTag);
                                            var.put("BOXQTY",String.valueOf(palletSnInfoSophos.getBoxQty())+"CTN");
                                            var.put("DN",String.valueOf(palletSnInfoSophos.getDnNo()));
                                            PrintData.setVariables(var);
                                            Gson gson = new GsonBuilder().create();
                                            JsonElement element = gson.toJsonTree(PrintData);
                                            labelArray.add(PrintData);
                                        }
                                    }
                                }

                                PrintDataList.setCount(etQty.getText().toString().trim());
                                PrintDataList.setLabel(labelArray);
                                new PrintWithNewType().execute(0);
                            } else {

                            }
//                            }

                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private Dialog onCreateChooseSophosPrinterCodesoftDialog02() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer_new, null);
        final Spinner printerIP = item.findViewById(R.id.printspinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,    //對應的Context
                R.array.printersIP,                             //資料選項內容
                android.R.layout.simple_spinner_item);  //預設Spinner未展開時的View(預設及選取後樣式)
        printerIP.setAdapter(adapter);
        printerIP.setSelection(adapter.getPosition("10.0.162.209"));//TSC TX300_1F_217
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("MerakiPrinterQty"));
        etQty.setText(qty);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the dialog title
        builder.setTitle(R.string.label_sophos_printer_info)
                .setView(item)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(etQty.getText().toString()) || Integer.parseInt(etQty.getText().toString().trim()) <= 0) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (true) {
                            view.setEnabled(false);
                            HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();

                            //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
                            int sum = 0;

                            for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
                                Pdf417Info_SOPHOS datatmp = entry.getValue();
                                if (datatmp.getPartNo().trim().indexOf("1") == 0 || datatmp.getPartNo().trim().indexOf("0") == 0) {//料號必須是09或14開頭
                                    int size = datatmp.getSnList().size();
                                    int l = (int) Math.ceil(size / 35.0);//35個SN一張
                                    sum += l;
                                }
                            }
                            //計算總張數--end

                            //列印資料&設定張數
//                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {
                            if (data.size() > 0) {
                                int page = 0;
                                PrintDataList=new NewPrintDataList();
                                ArrayList<NewPrintData> labelArray = new ArrayList<NewPrintData>();

                                for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {
                                    String partNo = entry.getKey();
                                    Pdf417Info_SOPHOS value = entry.getValue();
                                    AppController.debug("Server response partNo:" + partNo);

                                    if (partNo.trim().indexOf("1") == 0 || partNo.trim().indexOf("0") == 0) {
                                        List<String> snList = new ArrayList<>();
                                        StringBuilder stringBuilder = new StringBuilder();
                                        int cnt = 0;
                                        int cnt2 = 0;
                                        int MAX_CNT = 35;

                                        for (String sn : value.getSnList()) {
//                                        AppController.debug("Server response value3:" + sn);
                                            stringBuilder.append(sn);
                                            cnt++;
                                            cnt2++;

                                            if (cnt == MAX_CNT) {
                                                cnt = 0;
                                                snList.add(stringBuilder.toString()+".");
                                                stringBuilder.setLength(0);
                                            } else if (cnt2 == value.getSnList().size()) {
                                                snList.add(stringBuilder.toString()+".");
                                            } else {
                                                stringBuilder.append(".");
//                                                stringBuilder.append("\r\n");
                                            }
                                        }

                                        for (String value1 : snList) {
                                            AppController.debug("Server response value2:" + value1);
                                            AppController.debug("總頁數:" + sum);
                                            int size = value1.split("\\.").length;
//                                            int size = value1.split("\r\n").length;
                                            AppController.debug("總數量:" + size);
                                            page++;
                                            AppController.debug("has sn? :" + !"No Numbers".equals(value1));
                                            AppController.debug("value.getQty() :" + value.getQty());
                                            HashMap<String, String> printInfo=new HashMap<>();
                                            NewPrintData PrintData= new NewPrintData();

                                            if(AppController.getProperties("Sophos_PN_No_SN").indexOf(value.getPartNo()) >= 0){
                                                PrintData.setLabelName(AppController.getProperties("SophosLabelName02_No_SN"));
                                            }else{
                                                PrintData.setLabelName(AppController.getProperties("SophosLabelName02"));
                                            }
                                            //PrintData.setLabelName(AppController.getProperties("SophosLabelName02")); //20250321 Ann(SENAO101-202502-0034):因Sophos要求部分料號不要顯示SERIAL NO.字樣

                                            PrintData.setPrintName(printerIP.getSelectedItem().toString());
                                            HashMap<String, String> var=new HashMap<>();
                                            var.put("InvoiceNo",palletSnInfoSophos.getInvoiceNo());
                                            var.put("PN",value.getCustomerPartNo());
                                            var.put("PALLET",value.getPalletNo());
                                            var.put("PAGE",page+"/"+ sum);
                                            boolean isPak ="No Numbers".equals(value1) &&  value.getPartNo().trim().indexOf("0")==0;

                                            if(!isPak) {
                                                var.put("SerialNo",value1);
                                                var.put("QTY",String.valueOf(size));
                                            }else{
                                                var.put("QTY",String.valueOf(value.getQty()));
                                                var.put("SerialNo","");
                                            }

                                            var.put("smallTag",smallTag);
                                            var.put("BOXQTY",String.valueOf(palletSnInfoSophos.getBoxQty())+"CTN");
                                            var.put("DN",String.valueOf(palletSnInfoSophos.getDnNo()));
                                            PrintData.setVariables(var);
                                            Gson gson = new GsonBuilder().create();
                                            JsonElement element = gson.toJsonTree(PrintData);
                                            labelArray.add(PrintData);
                                        }
                                    }
                                }

                                PrintDataList.setCount(etQty.getText().toString().trim());
                                PrintDataList.setLabel(labelArray);
                                new PrintWithNewType02().execute(0);
                            } else {

                            }
//                            }

                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private Dialog onCreateChooseSophosPrinterDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer, null);
        final EditText etIP = item.findViewById(R.id.editTextIP);
        String ip = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_IP, AppController.getProperties("SophosPrinterIP"));
        etIP.setText(ip);
        final EditText etPort = item.findViewById(R.id.editTextPort);
        String port = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_PORT, AppController.getProperties("SophosPrinterPort"));
        etPort.setText(port);
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("SophosPrinterQty"));
        etQty.setText(qty);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the dialog title
        builder.setTitle(R.string.label_sophos_printer_info)
                .setView(item)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(etIP.getText().toString())) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_id, Toast.LENGTH_LONG).show();
                            etIP.requestFocus();
                            return;
                        }

                        if (TextUtils.isEmpty(etPort.getText().toString())) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_port, Toast.LENGTH_LONG).show();
                            etPort.requestFocus();
                            return;
                        }

                        if (TextUtils.isEmpty(etQty.getText().toString()) || Integer.parseInt(etQty.getText().toString().trim()) <= 0) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (saveSophosPrinterInfo(etIP.getText().toString().trim(), etPort.getText().toString().trim(), etQty.getText().toString().trim())) {
                            view.setEnabled(false);
                            HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();

                            //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
                            int sum = 0;

                            for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
                                Pdf417Info_SOPHOS datatmp = entry.getValue();
                                if (datatmp.getPartNo().trim().indexOf("1") == 0 || datatmp.getPartNo().trim().indexOf("0") == 0) {//料號必須是09或14開頭
                                    int size = datatmp.getSnList().size();
                                    int l = (int) Math.ceil(size / 35.0);//35個SN一張
                                    sum += l;
                                }
                            }
                            //計算總張數--end

                            //列印資料&設定張數
                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {
                                if (data.size() > 0) {
                                    int page = 0;

                                    for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {
                                        String partNo = entry.getKey();
                                        Pdf417Info_SOPHOS value = entry.getValue();
                                        AppController.debug("Server response partNo:" + partNo);

                                        if (partNo.trim().indexOf("1") == 0 || partNo.trim().indexOf("0") == 0) {
//                                        AppController.debug("Server response value1:" + value);
                                            List<String> snList = new ArrayList<>();
                                            StringBuilder stringBuilder = new StringBuilder();
                                            int cnt = 0;
                                            int cnt2 = 0;
                                            int MAX_CNT = 35;

                                            for (String sn : value.getSnList()) {
//                                            AppController.debug("Server response value3:" + sn);
                                                stringBuilder.append(sn);
                                                cnt++;
                                                cnt2++;

                                                if (cnt == MAX_CNT) {
                                                    cnt = 0;
                                                    snList.add(stringBuilder.toString()+".");
                                                    stringBuilder.setLength(0);
                                                } else if (cnt2 == value.getSnList().size()) {
                                                    snList.add(stringBuilder.toString()+".");
                                                } else {
                                                    stringBuilder.append(".");
//                                                    stringBuilder.append("\r\n");
                                                }
                                            }

                                            for (String value1 : snList) {
                                                AppController.debug("Server response value2:" + value1);
                                                AppController.debug("總頁數:" + sum);
                                                int size = value1.split("\\.").length;
//                                                int size = value1.split("\r\n").length;
                                                AppController.debug("總數量:" + size);
                                                page++;
                                                AppController.debug("has sn? :" + !"No Numbers".equals(value1));
                                                AppController.debug("value.getQty() :" + value.getQty());

                                                if (printSophosLabel(etIP.getText().toString().trim(), etPort.getText().toString().trim(), value, size, value1, page, sum)) {
                                                    Toast.makeText(getApplicationContext(), getString(R.string.print_error), Toast.LENGTH_LONG).show();
                                                    alertDialog.dismiss();
                                                    view.setEnabled(true);
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                } else {

                                }
                            }

//                            for (String sn : palletSnInfo.getSnInfo()) {
//                                stringBuilder.append(sn);
//                                cnt++;
//                                cnt2++;
//                                if (cnt == MAX_CNT) {
//                                    cnt = 0;
//                                    snList.add(stringBuilder.toString());
//                                    stringBuilder.setLength(0);
//                                } else if (cnt2 == palletSnInfo.getSnInfo().length) {
//                                    snList.add(stringBuilder.toString());
//                                } else {
//                                    stringBuilder.append("\r\n");
//                                }
//                            }

//                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {
//                                for (String value : snList) {
//                                    int size = value.split("\r\n").length;
//                                    String[] date = value.split("\r\n");
//                                    int l = (int)Math.ceil(size/35.0);//35個SN一張

//                                    if (printSophosLabel(etIP.getText().toString().trim(), etPort.getText().toString().trim(), palletSnInfo, size, value)) {
//                                          Toast.makeText(getApplicationContext(), getString(R.string.print_error), Toast.LENGTH_LONG).show();
//                                          alertDialog.dismiss();
//                                          view.setEnabled(true);
//                                          return;
//                                    }
//                                }
//                            }

                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private boolean savePrinterInfo(String ip, String port, String qty) {
        try {
            Preferences.setString(this, Preferences.EXTREME_PRINTER_IP, ip);
            Preferences.setString(this, Preferences.EXTREME_PRINTER_PORT, port);
            Preferences.setString(this, Preferences.EXTREME_PRINTER_QTY, qty);
            Log.d("savePrinterInfo: ", ip + ":" + port);
            return true;
        } catch (Exception e) {
            Log.e("ERROR", e.toString());
            errorInfo = getString(R.string.save_ip_port_qty_error);
            mConnection.setText(R.string.system_error);
            mConnection.setTextColor(Color.RED);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            return false;
        }
    }

    private boolean saveSophosPrinterInfo(String ip, String port, String qty) {
        try {
            Preferences.setString(this, Preferences.SOPHOS_PRINTER_IP, ip);
            Preferences.setString(this, Preferences.SOPHOS_PRINTER_PORT, port);
            Preferences.setString(this, Preferences.SOPHOS_PRINTER_QTY, qty);
            Log.d("savePrinterInfo: ", ip + ":" + port);
            return true;
        } catch (Exception e) {
            Log.e("ERROR", e.toString());
            errorInfo = getString(R.string.save_ip_port_qty_error);
            mConnection.setText(R.string.system_error);
            mConnection.setTextColor(Color.RED);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            return false;
        }
    }

    private boolean saveMerakiPrinterInfo(String ip, String port, String qty) {
        try {
            Preferences.setString(this, Preferences.MERAKI_PRINTER_IP, ip);
            Preferences.setString(this, Preferences.MERAKI_PRINTER_PORT, port);
            Preferences.setString(this, Preferences.MERAKI_PRINTER_QTY, qty);
            Log.d("savePrinterInfo: ", ip + ":" + port);
            return true;
        } catch (Exception e) {
            Log.e("ERROR", e.toString());
            errorInfo = getString(R.string.save_ip_port_qty_error);
            mConnection.setText(R.string.system_error);
            mConnection.setTextColor(Color.RED);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            return false;
        }
    }

    private int mm2dot(int mm) {
        // 200 DPI，1點=1/8 mm
        // 300 DPI，1點=1/12 mm
        // 200 DPI: 1 mm = 8 dots
        // 300 DPI: 1 mm = 12 dots
        // Alpha3R 200 DPI
        int factor = 12;
        return mm * factor;
    }

    private Dialog onCreatePrinterDialog_SOPHOS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the dialog title
        builder.setTitle(R.string.pick_printers)
                .setItems(R.array.printers, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        doPrintPalletSnInfo_SOPHOS(which + 1);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        }).setCancelable(false);

        return builder.create();
    }

    private void doPrintPalletSnInfo_SOPHOS(int printerNo) {
        palletSnInfoSophos = new ShipmentPalletSnInfoHelperSophos();
        palletSnInfoSophos.setBoxQty(palletInfo.getBoxQty());
        palletSnInfo = new ShipmentPalletSnInfoHelper();

        String dn = getDn();
        String palletNo = getPalletNo();//拿到PN

        palletSnInfoSophos.setDnNo(Integer.parseInt(dn));
        palletSnInfoSophos.setPalletNo(palletNo);
        palletSnInfoSophos.setPrintNo(printerNo);
        palletSnInfoSophos.setInvoiceNo(dnInfo.getInvoiceNo());

        palletSnInfo.setDnNo(Integer.parseInt(dn));
        palletSnInfo.setPalletNo(palletNo);
        palletSnInfo.setPrintNo(printerNo);
        //dialog = ProgressDialog.show(this, getString(R.string.holdon), activity.getString(R.string.processing), true);
        print = new PrintLabelHandler();

//        AppController.debug("dnInfo.getInvoiceNo() = "+dnInfo.getInvoiceNo());//拿到發票資料Incoice No
//        AppController.debug("palletSnInfo.getQty() = "+palletSnInfo.getQty());//拿到數量QTY
//        AppController.debug("palletSnInfo.getPalletNo() = "+palletSnInfo.getPalletNo());//拿到PN
//        AppController.debug("dnInfo.getInvoiceNo() = "+palletSnInfo.getSnInfo());//疑似SN  Serial No

        AppController.debug("dnInfo.getInvoiceNo() = " + palletSnInfoSophos.getInvoiceNo());//拿到發票資料Incoice No
        AppController.debug("palletSnInfo.getQty() = " + palletSnInfoSophos.getQty());//拿到數量QTY
        AppController.debug("palletSnInfo.getPalletNo() = " + palletSnInfoSophos.getPalletNo());//拿到PN
        AppController.debug("dnInfo.getInvoiceNo() = " + palletSnInfoSophos.getSnInfo());//疑似SN  Serial No
        new GetPrintSnInfo_SOPHOS().execute(0);
    }

    private void getPalletSnInfoByDnPallet_Verkada(final int printType) {
        palletSnInfoSophos = new ShipmentPalletSnInfoHelperSophos();
        palletSnInfoSophos.setBoxQty(palletInfo.getBoxQty());
        palletSnInfo = new ShipmentPalletSnInfoHelper();

        String dn = getDn();
        String palletNo = getPalletNo();

        palletSnInfo.setDnNo(Integer.parseInt(dn));
        palletSnInfo.setPalletNo(palletNo);
        palletSnInfo.setCustomerNo(palletInfo.getCustomerNo());

        palletSnInfoSophos.setDnNo(Integer.parseInt(dn));
        palletSnInfoSophos.setPalletNo(palletNo);
        palletSnInfoSophos.setInvoiceNo(dnInfo.getInvoiceNo());

        //重寫一個

        //有可能是直接傳到印表機的 但畫面?
        CallWebApi task = new CallWebApi(this, AppController.getProperties("GetPalletSnInfoByDnPallet_SAMSARA"), new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                palletSnInfo = (ShipmentPalletSnInfoHelper) result;
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";

                AppController.debug("palletSnInfo = " + palletSnInfo);
                AppController.debug("palletSnInfo = " + palletSnInfo.getHt().size());

//                if (palletSnInfo.getSnInfo() != null && palletSnInfo.getSnInfo().length > 0) {
                if (palletSnInfo.getHt() != null && palletSnInfo.getHt().size() > 0) {
                    if (palletInfo.getCustomerNo().equals(CUSTOMER_NO_VERKADA)) {
                        if (printType == PRINTTYPE_CODESOFT) {//codesoft1
                            onCreateChooseVerkadaCodesoftPrinterDialog().show();
                            Set<String> lotSet = palletSnInfo.getLotCode();

                            if (lotSet.size()!=0) {
                                onCreateChooseVerkadaCodesoftLotcodePrinterDialog().show();
                            }
                        } else if(printType == 2) {//codesoft2
                            onCreateChooseVerkadaCodesoftPrinterDialog02().show();
                            Set<String> lotSet = palletSnInfo.getLotCode();

                            if (lotSet.size()!=0) {
                                onCreateChooseVerkadaCodesoftLotcodePrinterDialog02().show();
                            }
                        } else {
                            onCreateChooseVerkadaPrinterDialog().show();
                            Set<String> lotSet = palletSnInfo.getLotCode();

                            if (lotSet.size()!=0) {
                                onCreateChooseVerkadaLotcodePrinterDialog().show();
                            }
                        }
                    }

                    if (palletInfo.getCustomerNo().equals(CUSTOMER_NO_SAMSARA)) {
                        onCreateChooseSamsaraPrinterDialog().show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_pallet_id_mis), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure() {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }

            @Override
            public void onError(BasicHelper result) {
                if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                    mConnection.setText(getString(R.string.connect_error));
                } else {
                    mConnection.setText(getString(R.string.db_return_error));
                }

                errorInfo = result.getStrErrorBuf();
                mConnection.setTextColor(Color.RED);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            }
        });

        task.execute(palletSnInfo);
    }

    private void getPalletSnInfoByDnPallet(final int printType) {
        AppController.debug("getPalletSnInfoByDnPallet()");
        palletSnInfoSophos = new ShipmentPalletSnInfoHelperSophos();
        palletSnInfoSophos.setBoxQty(palletInfo.getBoxQty());
        palletSnInfo = new ShipmentPalletSnInfoHelper();

        String dn = getDn();
        String palletNo = getPalletNo();

        palletSnInfo.setDnNo(Integer.parseInt(dn));
        palletSnInfo.setPalletNo(palletNo);
        palletSnInfo.setCustomerNo(palletInfo.getCustomerNo());

        palletSnInfoSophos.setDnNo(Integer.parseInt(dn));
        palletSnInfoSophos.setPalletNo(palletNo);
        palletSnInfoSophos.setInvoiceNo(dnInfo.getInvoiceNo());

        //重寫一個

        //有可能是直接傳到印表機的 但畫面?
        CallWebApi task = new CallWebApi(this, AppController.getProperties("GetPalletSnInfoByDnPallet"), new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                AppController.debug("SOPHOS列印標籤成功 ");
                palletSnInfo = (ShipmentPalletSnInfoHelper) result;
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
                AppController.debug("palletSnInfo = " + palletSnInfo);
                AppController.debug("palletSnInfo = " + palletSnInfo.getHt().size());

//                if (palletSnInfo.getSnInfo() != null && palletSnInfo.getSnInfo().length > 0) {
                if (palletSnInfo.getHt() != null && palletSnInfo.getHt().size() > 0) {
                    if(printType==PRINTTYPE_CODESOFT){//codesoft
                        onCreateChooseSophosPrinterCodesoftDialog().show();
                    }else if(printType==PRINTTYPE_CODESOFT02){//codesoft2
                        onCreateChooseSophosPrinterCodesoftDialog02().show();
                    } else{
                        onCreateChooseSophosPrinterDialog().show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_pallet_id_mis), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure() {
                AppController.debug("SOPHOS列印標籤錯誤1 ");
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }

            @Override
            public void onError(BasicHelper result) {
                AppController.debug("SOPHOS列印標籤錯誤2 ");
                if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                    mConnection.setText(getString(R.string.connect_error));
                } else {
                    mConnection.setText(getString(R.string.db_return_error));
                }

                errorInfo = result.getStrErrorBuf();
                mConnection.setTextColor(Color.RED);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            }
        });

        task.execute(palletSnInfo);
    }

    private void doPrintPalletLabel(int printerNo) {
        palletSnInfo = new ShipmentPalletSnInfoHelper();

        String dn = getDn();
        String palletNo = getPalletNo();

        palletSnInfo.setDnNo(Integer.parseInt(dn));
        palletSnInfo.setPalletNo(palletNo);
        palletSnInfo.setPrintNo(printerNo);

        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.downloading_data), true);
        print = new PrintLabelHandler();
        new PrintPalletLabel().execute(0);
    }

    private class GetPrintSnInfo extends AsyncTask<Integer, String, ShipmentPalletSnInfoHelper> {
        @Override
        protected ShipmentPalletSnInfoHelper doInBackground(Integer... params) {
            AppController.debug("Do Print Shipment Pallet Serial Number Info from "
                    + AppController.getServerInfo()
                    + AppController.getProperties("PrintShipmentPalletSnInfo"));
            publishProgress(getString(R.string.processing));
            return print.printShipmentPalletSnInfo(palletSnInfo);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }

        @Override
        protected void onPostExecute(ShipmentPalletSnInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    palletSnInfo = result;
                    Toast.makeText(getApplicationContext(), getString(R.string.PDF417_label_for_Meraki_file_has_been_sent_to_file_server), Toast.LENGTH_LONG).show();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class GetPrintSnInfo_SOPHOS extends AsyncTask<Integer, String, ShipmentPalletSnInfoHelperSophos> {
        @Override
        protected ShipmentPalletSnInfoHelperSophos doInBackground(Integer... params) {
            AppController.debug("Do Print Shipment Pallet Serial Number Info from "
                    + AppController.getServerInfo()
                    + AppController.getProperties("GetPalletSnInfoByDnPallet_SOPHOS"));
            publishProgress(getString(R.string.processing));
//            return print.printShipmentPalletSnInfo_SOPHOS(palletSnInfo);
            return print.printShipmentPalletSnInfo_SOPHOS(palletSnInfoSophos);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }

        @Override
        protected void onPostExecute(ShipmentPalletSnInfoHelperSophos result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    palletSnInfoSophos = result;
                    Toast.makeText(getApplicationContext(), getString(R.string.PDF417_label_for_Meraki_file_has_been_sent_to_file_server), Toast.LENGTH_LONG).show();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class GetPrintInfo extends AsyncTask<Integer, String, ShipmentPalletInfoHelper> {
        @Override
        protected ShipmentPalletInfoHelper doInBackground(Integer... params) {
            AppController.debug("Get Shipment Pallet Info from " + AppController.getServerInfo()
                    + AppController.getProperties("GetShipmentPalletInfo"));
            publishProgress(getString(R.string.downloading_data));
            return print.getShipmentPalletInfo(palletInfo);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }

        @Override
        protected void onPostExecute(ShipmentPalletInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    palletInfo = result;
                    getInvoiceNo(palletInfo.getDnNo());
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class PrintPalletLabel extends AsyncTask<Integer, String, ShipmentPalletSnInfoHelper> {
        @Override
        protected ShipmentPalletSnInfoHelper doInBackground(Integer... params) {
            AppController.debug("PrintTemboPalletLabel Info from " + AppController.getServerInfo()
                    + AppController.getProperties("PrintTemboPalletLabel"));
            publishProgress(getString(R.string.downloading_data));
            return print.doPrintPalletLabel(palletSnInfo);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }

        @Override
        protected void onPostExecute(ShipmentPalletSnInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    palletSnInfo = result;
                    Toast.makeText(getApplicationContext(), getString(R.string.Pallet_label_for_Tembo_file_has_been_sent_to_file_server), Toast.LENGTH_LONG).show();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private Dialog onCreatePrinterExtremeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle(R.string.pick_printers)
                .setItems(R.array.printer_type, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if (which==PRINTTYPE_CODESOFT) {
                            onCreateChoosePrinterCodesoftDialog().show();
                        }else if (which==PRINTTYPE_CODESOFT02){//CODESOFT02列印
                            onCreateChoosePrinterCodesoftDialog02().show();
                        }else if (which==0){//WIFI列印
                            onCreateChoosePrinterDialog().show();
                        }

                        AppController.debug("which:" + which);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        }).setCancelable(false);

        return builder.create();
    }

    private class GetExtremePalletInfo extends AsyncTask<Integer, String, ExtremePalletInfoHelper> {
        @Override
        protected ExtremePalletInfoHelper doInBackground(Integer... params) {
            AppController.debug("Get Extreme Pallet Info from " + AppController.getServerInfo()
                    + AppController.getProperties("ExtremePalletInfo"));
            publishProgress(getString(R.string.downloading_data));
            return print.getExtremePalletInfo(extremePalletInfo);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }

        @Override
        protected void onPostExecute(ExtremePalletInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    extremePalletInfo = result;

                    if (extremePalletInfo.getInfoList() != null && extremePalletInfo.getInfoList().length > 0) {
                        onCreatePrinterExtremeDialog().show();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.Extreme_pallet_info_not_found), Toast.LENGTH_LONG).show();//無Extreme棧板資訊，請洽MIS
                    }
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    public class ChkDNInfo extends AsyncTask<Integer, String, ChkDeliveryInfoHelper> {
        @Override
        protected ChkDeliveryInfoHelper doInBackground(Integer... params) {
            AppController.debug("Get DeliveryInfo from " + AppController.getServerInfo()
                    + AppController.getProperties("ChkDNInfo"));
            publishProgress(getString(R.string.downloading_data));
            return verifyHandler.getDNInfo();
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }

        @Override
        protected void onPostExecute(ChkDeliveryInfoHelper result) {
            // execution of result of Long time consuming operation //
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                dnInfo = result;
//                errorInfo = "";
                errorInfo = dnInfo.getStrErrorBuf();
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                printLabel();//TODO
                AppController.debug("palletInfo.getCustomerNo() = " + palletInfo.getCustomerNo());

                if (palletInfo.getCustomerNo().equals(CUSTOMER_NO_MERAKI) || palletInfo.getCustomerNo().equals(CUSTOMER_NO_CISCO)) {
                    onCreatePrinterDialog().show();//原本Meraki
                }

                if (palletInfo.getCustomerNo().equals(CUSTOMER_NO_AURADINE)) {
                    onCreateAuradinePrinterDialog().show();//Auradine
                }

                if (palletInfo.getCustomerNo().equals(CUSTOMER_NO_TEMBO)) {
                    onCreateChooseFileServerDialog().show();
                }

                if (palletInfo.getCustomerNo().equals(CUSTOMER_NO_EXTREME)) {//這個好
                    getExtremeLabelInfo();
                }

//                if (palletInfo.getCustomerNo().equals(CUSTOMER_NO_SAMSARA))
//                    getPalletSnInfoByDnPallet_SAMSARA();

                if (palletInfo.getCustomerNo().equals(CUSTOMER_NO_VERKADA)) {
                    onCreateVerkadaPrinterDialog().show();
                }

                if (palletInfo.getCustomerNo().equals(CUSTOMER_NO_CAMBIUM)) {
                    doPrintPalletSnInfo2();
                }

                //新的SOPHOS
                for (String data : CUSTOMER_NO_SOPHOS_SET) {
                    if (palletInfo.getCustomerNo().equals(data)) {
                        onCreateSophosPrinterDialog().show();
                        //onCreatePrinterDialog_SOPHOS().show();//把打印標籤存在Servlet後台，再呼叫標籤程式列印
//                        getPalletSnInfoByDnPallet();//PDA直接執行列印
                    }
                }

                //原來的SOPHOS
//                if (palletInfo.getCustomerNo().equals(CUSTOMER_NO_SOPHOS)){
//                    onCreatePrinterDialog_SOPHOS().show();
//                    //getPalletSnInfoByDnPallet();
//                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }
}
