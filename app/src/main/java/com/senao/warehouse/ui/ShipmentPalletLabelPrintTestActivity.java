package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.print.PrintHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tscdll.TscWifiActivity;
import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.asynctask.AsyncResponse;
import com.senao.warehouse.asynctask.CallWebApi;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.ChkDeliveryInfoHelper;
import com.senao.warehouse.database.ExtremePalletInfoHelper;
import com.senao.warehouse.database.ExtremePalletItem;
import com.senao.warehouse.database.PALLET_LABEL_QR_CODE_FORMAT;
import com.senao.warehouse.database.ShipmentPalletInfoHelper;
import com.senao.warehouse.database.ShipmentPalletSnInfoHelper;
import com.senao.warehouse.database.ShipmentPalletSnInfoHelperSophos;
import com.senao.warehouse.handler.Pdf417Info_SOPHOS;
import com.senao.warehouse.handler.PrintLabelHandler;
import com.senao.warehouse.handler.ShippingVerifyMainHandler;
import com.senao.warehouse.print.BtPrintLabel;
import com.senao.warehouse.util.Preferences;
import com.senao.warehouse.util.QrCodeUtil;
import com.senao.warehouse.util.ReturnCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShipmentPalletLabelPrintTestActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PRINTER_SETTING = 2;
    private static final int REQUEST_BLUETOOTH_SETTINGS = 3;
    private static final String TAG = ShipmentPalletLabelPrintTestActivity.class.getSimpleName();
    private final String CUSTOMER_NO_MERAKI = "5633";
    private final String CUSTOMER_NO_TEMBO = "11734";
    private final String CUSTOMER_NO_EXTREME = "8753";
    private final String CUSTOMER_NO_SOPHOS = "178840";// 178837 178838 178840 179895 178871

    private final String[] CUSTOMER_NO_SOPHOS_SET = {"178837", "178838", "178840", "179895", "178871"};


    private TextView mConnection;
    private EditText txtImprotDnNo, txtImportPalletNo, txtImportPalletSize, txtImportQrCode, txtImportQrCodePalletSize;
    private ProgressDialog dialog;
    private String errorInfo = "";
    private PrintLabelHandler print;
    private ShipmentPalletInfoHelper palletInfo;
    private ShipmentPalletSnInfoHelper palletSnInfo;
    private ShipmentPalletSnInfoHelperSophos palletSnInfoSophos;

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
                    AlertDialog.Builder dialog = new AlertDialog.Builder(
                            ShipmentPalletLabelPrintTestActivity.this);
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



        txtImportQrCodePalletSize = findViewById(R.id.edittext_qrcode_pallet_size);
        txtImportQrCodePalletSize.setSelectAllOnFocus(true);

        barcodeLayout = findViewById(R.id.barcodeLayout);
        txtImprotDnNo = findViewById(R.id.edittext_dn_no);
        txtImprotDnNo.setSelectAllOnFocus(true);
        txtImprotDnNo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_UP)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    //hideKeyboard(v);
                    Log.d(TAG, "onKeyListener Enter or Action Down" + v.getId());
                    String dn = txtImprotDnNo.getText().toString().trim();
                    if (TextUtils.isEmpty(dn)) {
                        Toast.makeText(ShipmentPalletLabelPrintTestActivity.this, getString(R.string.dn_num_is_not_null),
                                Toast.LENGTH_SHORT).show();
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
//                onCreateChooseSophosPrinterDialog().show();
                onCreateChooseSamsaraPrinterDialog().show();
//                    doQueryPalletInfo();

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
        dialog = ProgressDialog.show(ShipmentPalletLabelPrintTestActivity.this,
                getString(R.string.holdon), getString(R.string.printingLabel), true);

        String qrCode = dnInfo.getInvoiceNo() + "@" + palletInfo.getDnNo() + "@" + palletInfo.getPalletNo() + "@" + palletInfo.getBoxQty();
        smallTag = qrCode;
        if (!BtPrintLabel.printShipmentPalletLabel(palletInfo.getMark(),
                palletInfo.getShippingWay(), palletInfo.getDnNo(),
                palletInfo.getPalletNo(), palletInfo.getBoxQty(), qrCode)) {
            errorInfo = getString(R.string.printLabalFailed);
            mConnection.setText(getString(R.string.printer_connect_error));
            mConnection.setTextColor(Color.RED);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        dialog.dismiss();
    }

    private void getInvoiceNo(int dn) {
        dialog = ProgressDialog.show(getApplicationContext(),getString(R.string.holdon), getString(R.string.downloading_data), true);
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
            TscEthernetDll.setup(100, 150, 3, 10, 0, 3, 0);
            TscEthernetDll.clearbuffer();
            TscEthernetDll.sendcommand("SET TEAR ON\n");
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

    private boolean printSophosLabel(String ip, String port) {
        dialog = ProgressDialog.show(this,
                getString(R.string.holdon), getString(R.string.printingLabel), true);
        AppController.debug("======>!B" );
        try {
            if (TscEthernetDll == null) {
                TscEthernetDll = new TscWifiActivity();
            }
            TscEthernetDll.openport(ip, Integer.parseInt(port));
            TscEthernetDll.setup(100, 72, 3, 10, 0, 3, 0);
            TscEthernetDll.clearbuffer();
            TscEthernetDll.sendcommand("SET TEAR ON\n");
            int x, y, x_end, y_end;

            //boolean isPak = qrCode.indexOf("NA")==0;
            //boolean isPak1 = qrCode.indexOf("No Numbers")==0;

//"No Numbers".equals(qrCode)

//            boolean isPak ="No Numbers".equals(qrCode) &&  palletSnInfoHelper.getPartNo().trim().indexOf("0")==0  ;




//            TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "Invoice NO. ");

            String st = "SerialABC123001.SerialABC123002.SerialABC123003.SerialABC123004.SerialABC123005.SerialABC123006.SerialABC123007.SerialABC123008.SerialABC123009.SerialABC123010.SerialABC123011.SerialABC123012.SerialABC123013.SerialABC123014.SerialABC123015.SerialABC123016.SerialABC123017.SerialABC123018.SerialABC123019.SerialABC123020.SerialABC123021.SerialABC123022.SerialABC123023.SerialABC123024.SerialABC123025.SerialABC123026.SerialABC123027.SerialABC123028.SerialABC123029.SerialABC123030.SerialABC123031.SerialABC123032.SerialABC123033.SerialABC123034.SerialABC123035.";
            String st1 ="WHFG01";

            x = mm2dot(8);
            y = mm2dot(5);
            TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", st1);

            st1 ="100236";
            x = mm2dot(30);
            TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", st1);
//
//
//            x = mm2dot(30);
//            y = mm2dot(27);
//            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial42\",0,1,1,\"SKU "  + "\"\n");
//            TscEthernetDll.sendcommand("PDF417 "+ x+","+y+","+"400,200,0,E3,\"Error correction level:4\"\n");
//            TscEthernetDll.sendcommand("PDF417 "+ "50,50,600,600,0,E3,\""+st+"\"\n");
//            TscEthernetDll.sendcommand("PDF417 "+ x+","+y+","+"400,200,0,"+"[E3]"+","+"\"content\"");
//            TscEthernetDll.sendcommand("PDF417 "+"50,50,400,200,0,"+"Without Options");


//            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"Arial42\",0,1,1,\"SKU " + palletSnInfoHelper.getRemark() + "\"\n");
//
//            if(isPak){//如果是配件包，直接取後台算的數量
//                TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, "QTY: " + palletSnInfoHelper.getQty());
//            }else{
//                TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, "QTY: " + qty);
//            }


            //印QRcode1
//            x = mm2dot(73);
//            y = mm2dot(5);
//            TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", palletSnInfoSophos.getInvoiceNo());

            //印QRcode2
//            y = mm2dot(15);
//            TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", palletSnInfoHelper.getCustomerPartNo());

            //印QRcode3
//            x = mm2dot(55);
//            y = mm2dot(31);
//            if(!isPak) {
//                TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", qrCode);
//            }

            TscEthernetDll.printlabel(1, 1);
            TscEthernetDll.closeport(5000);


            AppController.debug("======>!C" );

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
            Toast.makeText(getApplicationContext(), "錯誤訊息:"+ex.getMessage(), Toast.LENGTH_LONG).show();
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
                Toast.makeText(getApplicationContext(), getString(R.string.enter_dn),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(palletNoStartWithDn)) {
                txtImportPalletNo.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_pallet_nu),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            if (palletNoStartWithDn.length() <= dn.length()) {
                txtImportPalletNo.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.pallet_nu_uncorrect),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            if (palletNoStartWithDn.indexOf(dn) != 0) {
                txtImportPalletNo.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.pallet_nu_uncorrect),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            txtImprotDnNo.setText(dn);

            txtImportPalletNo.setText(palletNoStartWithDn);
        } else {

            String palletNo = getPalletNo();
            if (TextUtils.isEmpty(txtImportQrCode.getText().toString().trim())) {
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_qrcode),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(dn)) {
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.qrcode_dn_is_null),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(palletNo)) {
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.qrcode_pallet_is_null),
                        Toast.LENGTH_SHORT).show();
                return false;
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
                        doPrintPalletSnInfo(which + 1);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {


            }
        }).setCancelable(false);
        return builder.create();
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
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {


            }
        }).setCancelable(false);
        return builder.create();
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
                button.setOnClickListener(new OnClickListener() {

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
                                    {
                                        alertDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), getString(R.string.print_error), Toast.LENGTH_LONG).show();
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
        String qty = "1";
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
                button.setOnClickListener(new OnClickListener() {

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


                                            AppController.debug("======>!A" );
                                            AppController.debug("======>!A==>"+etIP.getText().toString().trim()+"==>"+etPort.getText().toString().trim() );


                                            if (printSophosLabel(etIP.getText().toString().trim(), etPort.getText().toString().trim())) {

                                                Toast.makeText(getApplicationContext(), getString(R.string.print_error), Toast.LENGTH_LONG).show();
                                                alertDialog.dismiss();
                                                view.setEnabled(true);
                                                return;

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

//        Model:AD32-HW
//        PN: 1A-48001-A



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
        builder.setTitle("印表機資訊")//R.string.label_samsara_printer_info+
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


//                            HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();

                            //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
//                            int sum = 0;
//                            for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
//                                Pdf417Info_SOPHOS datatmp = entry.getValue();
//                                int size = datatmp.getSnList().size();
//                                int l = (int) Math.ceil(size / 60.0);//35個SN一張
//                                sum += l;
//                            }
                            //計算總張數--end


                            if (printSamsaraLabel(etIP.getText().toString().trim(), etPort.getText().toString().trim(), null, 0, txtImportQrCode.getText().toString(), 1, 1,1)) {
                                {
                                    Toast.makeText(getApplicationContext(), getString(R.string.print_error), Toast.LENGTH_LONG).show();
                                    alertDialog.dismiss();
                                    view.setEnabled(true);
                                    return;
                                }
                            }




                            //列印資料&設定張數
//                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {
//                                if (data.size() > 0) {
//                                    int page = 0;
//                                    for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {
//                                        String partNo = entry.getKey();
//                                        Pdf417Info_SOPHOS value = entry.getValue();
//
//                                        AppController.debug("Server response partNo:" + partNo);
//                                        AppController.debug("Server response size:" + value.getSnList().size());
////                                        AppController.debug("Server response value:" + value);
//
//
////                                    AppController.debug("Server response value1:" + value);
//
//                                        List<String> snList = new ArrayList<>();
//                                        StringBuilder stringBuilder = new StringBuilder();
//                                        int cnt = 0;
//                                        int cnt2 = 0;
//                                        int MAX_CNT = 60;
//
//                                        for (String sn : value.getSnList()) {
////                                        AppController.debug("Server response value3:" + sn);
//                                            stringBuilder.append(sn);
//                                            cnt++;
//                                            cnt2++;
//                                            if (cnt == MAX_CNT) {//每張最大數量
//                                                cnt = 0;
//                                                snList.add(stringBuilder.toString());
//                                                stringBuilder.setLength(0);
//                                            } else if (cnt2 == value.getSnList().size()) {//剩餘的數量
//                                                snList.add(stringBuilder.toString());
//                                            } else {
//                                                stringBuilder.append(",");
//                                            }
//                                        }
////
//                                        for (String value1 : snList) {
//                                            AppController.debug("Server response value2:" + value1);
//                                            AppController.debug("總頁數:" + sum);
//                                            int size = value1.split(",").length;
//                                            AppController.debug("總數量:" + size);
//                                            page++;
//
//                                            AppController.debug("has sn? :" + !"No Numbers".equals(value1));
//                                            AppController.debug("value.getQty() :" + value.getQty());
//
//                                            if (printSamsaraLabel(etIP.getText().toString().trim(), etPort.getText().toString().trim(), value, size, value1, page, sum,1)) {
//                                                {
//                                                    Toast.makeText(getApplicationContext(), getString(R.string.print_error), Toast.LENGTH_LONG).show();
//                                                    alertDialog.dismiss();
//                                                    view.setEnabled(true);
//                                                    return;
//                                                }
//                                            }
//
//
//                                        }
//
//
//                                    }
//                                } else {
//
//                                }
//                            }


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
//
//                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {
//                                for (String value : snList) {
//                                    int size = value.split("\r\n").length;
//                                    String[] date = value.split("\r\n");
//
//                                    int l = (int)Math.ceil(size/35.0);//35個SN一張
//
//
//                                    if (printSophosLabel(etIP.getText().toString().trim(), etPort.getText().toString().trim(), palletSnInfo, size, value)) {
//                                        {
//                                            Toast.makeText(getApplicationContext(), getString(R.string.print_error), Toast.LENGTH_LONG).show();
//                                            alertDialog.dismiss();
//                                            view.setEnabled(true);
//                                            return;
//                                        }
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
    private boolean printSamsaraLabel(String ip, String port, Pdf417Info_SOPHOS palletSnInfoHelper, int qty, String qrCode, int page, int allpage,int type) {
        dialog = ProgressDialog.show(this,
                getString(R.string.holdon), getString(R.string.printingLabel), true);

        try {


            if(page==1){//page%8==1

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
//                TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,1,1,\"" + "V123sssssss4567F9" + "\"\n");
                TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", qrCode);


                x = mm2dot(8);
                y = mm2dot(15);
//                TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "Samsara");

//                TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,3,3,\"" + "V1234567F9" + "\"\n");


                x = mm2dot(8);
                y = mm2dot(45);


//                TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"FONT002\",0,3,3,\"" + "測試字體" + "\"\n");

//                y = mm2dot(10);
//                TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "Product:" + palletSnInfoHelper.getCustomerPartNoDescription());
//
//                y = mm2dot(17);
//                TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 5, 5,  palletSnInfoHelper.getCustomerPartNoDescription());
//
//
//                y = mm2dot(27);
//                TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "Pallet ID:" + palletSnInfoHelper.getPalletNo());
//
//                y = mm2dot(32);
//                TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 5, 5,  palletSnInfoHelper.getPalletNo());
//
//
//
//
//                y = mm2dot(42);
//                if(palletSnInfoHelper.getSnList()!=null) {
//                    TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "Qty:" + palletSnInfoHelper.getSnList().size());
//                }
//
//
//                y = mm2dot(50);
//
//
//                TscEthernetDll.printlabel(1, 1);
//
//
//                TscEthernetDll.clearbuffer();
//                TscEthernetDll.sendcommand("SET TEAR ON\n");
//
//
//                TscEthernetDll.sendcommand("PDF417 "+ "50,20,600,600,0,E3,\""+qrCode+"\"\n");
//
//
//                x = mm2dot(70);
//                y = mm2dot(30);
//                TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, "Page " + (page) + "/" + allpage+"-"+palletSnInfoHelper.getPalletNo());



                TscEthernetDll.printlabel(1, 1);

                TscEthernetDll.closeport(5000);
            }else if(page==2){//if(page==2)

//                if (TscEthernetDll == null) {
//                    TscEthernetDll = new TscWifiActivity();
//                }
//                TscEthernetDll.openport(ip, Integer.parseInt(port));
//                TscEthernetDll.setup(100, 72, 3, 10, 0, 3, 0);
//                TscEthernetDll.clearbuffer();
//                TscEthernetDll.sendcommand("SET TEAR ON\n");
//                int x, y, x_end, y_end;
//
//
//                TscEthernetDll.sendcommand("PDF417 "+ "50,20,600,600,0,E3,\""+qrCode+"\"\n");
//
//                x = mm2dot(70);
//                y = mm2dot(30);
//                TscEthernetDll.printerfont(x, y, "4", 0, 1, 1, "Page " + (page) + "/" + allpage+"-"+palletSnInfoHelper.getPalletNo());
//
//
//                TscEthernetDll.printlabel(1, 1);
//                TscEthernetDll.closeport(5000);
            }










            return false;
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            return true;
        } finally {
            dialog.dismiss();
        }
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

    private void getPalletSnInfoByDnPallet() {
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
                palletSnInfo = (ShipmentPalletSnInfoHelper) result;
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";

                AppController.debug("palletSnInfo = " + palletSnInfo);
                AppController.debug("palletSnInfo = " + palletSnInfo.getHt().size());

//                if (palletSnInfo.getSnInfo() != null && palletSnInfo.getSnInfo().length > 0) {
                if (palletSnInfo.getHt() != null && palletSnInfo.getHt().size() > 0) {
                    onCreateChooseSophosPrinterDialog().show();
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

    private class GetPrintSnInfo extends
            AsyncTask<Integer, String, ShipmentPalletSnInfoHelper> {

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
            AppController.debug("onPostExecute() result json = "
                    + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    palletSnInfo = result;
                    Toast.makeText(getApplicationContext(), "PDF417 Label for Meraki檔案已傳送至File Server", Toast.LENGTH_LONG).show();
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

    private class GetPrintSnInfo_SOPHOS extends
            AsyncTask<Integer, String, ShipmentPalletSnInfoHelperSophos> {

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
            AppController.debug("onPostExecute() result json = "
                    + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    palletSnInfoSophos = result;
                    Toast.makeText(getApplicationContext(), "PDF417 Label for Meraki檔案已傳送至File Server", Toast.LENGTH_LONG).show();
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

    private class GetPrintInfo extends
            AsyncTask<Integer, String, ShipmentPalletInfoHelper> {

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
            AppController.debug("onPostExecute() result json = "
                    + new Gson().toJson(result));

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

    private class PrintPalletLabel extends
            AsyncTask<Integer, String, ShipmentPalletSnInfoHelper> {

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
            AppController.debug("onPostExecute() result json = "
                    + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    palletSnInfo = result;
                    Toast.makeText(getApplicationContext(), "Pallet Label for Tembo檔案已傳送至File Server", Toast.LENGTH_LONG).show();
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


    private class GetExtremePalletInfo extends
            AsyncTask<Integer, String, ExtremePalletInfoHelper> {

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
            AppController.debug("onPostExecute() result json = "
                    + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    extremePalletInfo = result;
                    if (extremePalletInfo.getInfoList() != null && extremePalletInfo.getInfoList().length > 0) {
                        onCreateChoosePrinterDialog().show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Extreme pallet info not found，please contact MIS", Toast.LENGTH_LONG).show();//無Extreme棧板資訊，please contact MIS
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
            AppController.debug("onPostExecute() result json = "
                    + new Gson().toJson(result));
            dialog.dismiss();
            if (result != null) {
                dnInfo = result;
                errorInfo = "";
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
//                printLabel();

//                AppController.debug("palletInfo.getCustomerNo()" + palletInfo.getCustomerNo());
//                if (palletInfo.getCustomerNo().equals(CUSTOMER_NO_MERAKI))
//                    onCreatePrinterDialog().show();
//                if (palletInfo.getCustomerNo().equals(CUSTOMER_NO_TEMBO))
//                    onCreateChooseFileServerDialog().show();
//                if (palletInfo.getCustomerNo().equals(CUSTOMER_NO_EXTREME))
//                    getExtremeLabelInfo();
//
//                //新的SOPHOS
//                for (String data : CUSTOMER_NO_SOPHOS_SET) {
//                    if (palletInfo.getCustomerNo().equals(data)) {
//                        //onCreatePrinterDialog_SOPHOS().show();//把打印標籤存在Servlet後台，再呼叫標籤程式列印
                  getPalletSnInfoByDnPallet();//PDA直接執行列印
//                    }
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
