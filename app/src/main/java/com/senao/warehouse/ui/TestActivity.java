package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tscdll.TscWifiActivity;
import com.senao.warehouse.R;
import com.senao.warehouse.print.BtPrintLabel;
import com.senao.warehouse.database.ExtremePalletItem;
import com.senao.warehouse.util.Preferences;
import com.senao.warehouse.AppController;

public class TestActivity extends Activity {

    EditText etIP, etPort, etWidth, etHeight;
    Button btnOK,btnOK2;
    TscWifiActivity TscEthernetDll = new TscWifiActivity();
    private ProgressDialog dialog;
    private ExtremePalletItem extremePalletInfo;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PRINTER_SETTING = 2;
    private static final int REQUEST_BLUETOOTH_SETTINGS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

//        etIP =  findViewById(R.id.etIP);
//        etPort =  findViewById(R.id.etPort);
//        etWidth =  findViewById(R.id.etWidth);
//        etHeight =  findViewById(R.id.etHeight);
        btnOK =  findViewById(R.id.btnOK);
        btnOK2 =  findViewById(R.id.btnOK2);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

//                    TscEthernetDll.openport(etIP.getText().toString().trim(), Integer.parseInt(etPort.getText().toString().trim()));
//
//                    TscEthernetDll.setup(Integer.parseInt(etWidth.getText().toString().trim()), Integer.parseInt(etHeight.getText().toString().trim()), 4, 4, 0, 0, 0);
//                    TscEthernetDll.clearbuffer();
//                    TscEthernetDll.sendcommand("SET TEAR ON\n");
//                    TscEthernetDll.sendcommand("SET COUNTER @1 1\n");
//                    TscEthernetDll.sendcommand("@1 = \"0001\"\n");
//                    TscEthernetDll.sendcommand("TEXT 100,300,\"3\",0,1,1,@1\n");
//                    TscEthernetDll.barcode(100, 100, "128", 100, 1, 0, 3, 3, "123456789");
//                    TscEthernetDll.printerfont(100, 250, "3", 0, 1, 1, "987654321");
//                    TscEthernetDll.printlabel(2, 1);
//
//                    TscEthernetDll.closeport(5000);

                    setupData();
                    onCreateChoosePrinterDialog().show();

                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

//        setupData();
//        onCreateChoosePrinterDialog().show();

        btnOK2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkPrinterSetting())
                    printPoLabel("123456");
            }
        });
    }

    private void setupData() {
        extremePalletInfo = new ExtremePalletItem();
        extremePalletInfo.setUniquePackID("J20180100090");
        extremePalletInfo.setTriggerInfo("OT00086491");
        extremePalletInfo.setSkuPn("XEN-R000294");
        extremePalletInfo.setQty(2);
        extremePalletInfo.setSupplierCode("FJZ-N-FXH");
        extremePalletInfo.setBrocadeDescription("FRU,2 POST MID MOUNT KIT/FLUSH MOUNT KIT");
        extremePalletInfo.setCountryOfOrigin("TW");
    }

    private Dialog onCreateChoosePrinterDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer, null);
        final EditText etIP =  item.findViewById(R.id.editTextIP);
        String ip = Preferences.getSharedPreferences(this).getString(
                Preferences.EXTREME_PRINTER_IP, AppController.getProperties("ExtremePrinterIP"));
        etIP.setText(ip);
        final EditText etPort =  item.findViewById(R.id.editTextPort);
        String port = Preferences.getSharedPreferences(this).getString(
                Preferences.EXTREME_PRINTER_PORT, AppController.getProperties("ExtremePrinterPort"));
        etPort.setText(port);
        final EditText etQty =  item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.EXTREME_PRINTER_QTY, AppController.getProperties("ExtremePrinterQty"));
        etQty.setText(qty);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle(R.string.label_printer_info)
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
                            if(printExtremeLabel(etIP.getText().toString().trim(), etPort.getText().toString().trim(), etQty.getText().toString().trim())) {
                                alertDialog.dismiss();
                            }
                        }
                    }
                });
            }
        });
        return alertDialog;
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

    private boolean savePrinterInfo(String ip, String port, String qty) {

        try {
            Preferences.setString(this, Preferences.EXTREME_PRINTER_IP, ip);

            Preferences.setString(this, Preferences.EXTREME_PRINTER_PORT, port);

            Preferences.setString(this, Preferences.EXTREME_PRINTER_QTY, qty);

            Log.d("savePrinterInfo: ", ip + ":" + port);

            return true;
        } catch (Exception e) {
            Log.e("ERROR", e.toString());
            return false;
        }
    }

    private boolean printExtremeLabel(String ip, String port, String qty) {
        dialog = ProgressDialog.show(this,
                getString(R.string.holdon), getString(R.string.printingLabel), true);

        try {
            if (TscEthernetDll == null) {
                TscEthernetDll = new TscWifiActivity();
            }

            TscEthernetDll.openport(ip, Integer.parseInt(port));
            TscEthernetDll.setup(100, 150, 4, 4, 0, 3, 0);
            TscEthernetDll.clearbuffer();
            TscEthernetDll.sendcommand("SET TEAR ON\n");
            int x,y;
            x = mm2dot(5);
            y = mm2dot(8);
            TscEthernetDll.printerfont(x, y, "2", 0, 2, 2, "Unique Pack ID : " + extremePalletInfo.getUniquePackID());
            y = mm2dot(14);
            TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 5, 5, extremePalletInfo.getUniquePackID());
            y = mm2dot(25);
            TscEthernetDll.sendcommand("BAR " + x + "," + y + ",1080,12\n");
            y = mm2dot(28);
            TscEthernetDll.printerfont(x, y, "2", 0, 2, 2, "Trigger information : " + extremePalletInfo.getTriggerInfo());
            y = mm2dot(34);
            TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 5, 5, extremePalletInfo.getTriggerInfo());
            y = mm2dot(45);
            TscEthernetDll.sendcommand("BAR " + x + "," + y + ",1080,12\n");
            y = mm2dot(48);
            TscEthernetDll.printerfont(x, y, "2", 0, 2, 2, "SKU or Part Number : " + extremePalletInfo.getSkuPn());
            y = mm2dot(54);
            TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 5, 5, extremePalletInfo.getSkuPn());
            y = mm2dot(65);
            TscEthernetDll.sendcommand("BAR " + x + "," + y + ",1080,12\n");
            y = mm2dot(68);
            TscEthernetDll.printerfont(x, y, "2", 0, 2, 2, "Quantity : " + String.valueOf(extremePalletInfo.getQty()));
            y = mm2dot(74);
            TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 5, 5, String.valueOf(extremePalletInfo.getQty()));
            y = mm2dot(85);
            TscEthernetDll.sendcommand("BAR " + x + "," + y + ",1080,12\n");
            y = mm2dot(88);
            TscEthernetDll.printerfont(x, y, "2", 0, 2, 2, "Supplier Code : " + extremePalletInfo.getSupplierCode());
            y = mm2dot(94);
            TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 5, 5, extremePalletInfo.getSupplierCode());
            y = mm2dot(105);
            TscEthernetDll.sendcommand("BAR " + x + "," + y + ",1080,12\n");
            y = mm2dot(108);
            TscEthernetDll.printerfont(x, y, "2", 0, 2, 2, "Brocade Description : ");
            y = mm2dot(114);
            TscEthernetDll.sendcommand("BLOCK " + x + "," + y + ",1080,192,\"5\",0,1,1,20,0,\"" + extremePalletInfo.getBrocadeDescription() + "\"\n");
            y = mm2dot(128);
            TscEthernetDll.sendcommand("BAR " + x + "," + y + ",1080,12\n");
            y = mm2dot(131);
            TscEthernetDll.printerfont(x, y, "2", 0, 2, 2, "Country of Origin : " + extremePalletInfo.getCountryOfOrigin());
            y = mm2dot(138);
            TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 5, 5, extremePalletInfo.getCountryOfOrigin());
            TscEthernetDll.printlabel(Integer.parseInt(qty), 1);
            TscEthernetDll.closeport(5000);
            return true;
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage(),Toast.LENGTH_LONG).show();
            return false;
        }
        finally {
            dialog.dismiss();
        }
    }

    private boolean checkPrinterSetting() {
        if (!BtPrintLabel.isPrintNameSet(this)) {
            Intent intent = new Intent(this, PrinterSettingActivity.class);
            startActivityForResult(intent, REQUEST_PRINTER_SETTING);
            Toast.makeText(getApplicationContext(), getString(R.string.set_printer_name), Toast.LENGTH_LONG).show();
            return false;
        }

        if (!BtPrintLabel.isBtEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Toast.makeText(getApplicationContext(), getString(R.string.open_bt), Toast.LENGTH_LONG).show();
            return false;
        }

        if (!BtPrintLabel.instance(getApplicationContext())) {
            Intent intentOpenBluetoothSettings = new Intent();
            intentOpenBluetoothSettings.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivityForResult(intentOpenBluetoothSettings, REQUEST_BLUETOOTH_SETTINGS);
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_printer), Toast.LENGTH_LONG).show();
            return false;
        }

        if (BtPrintLabel.connect()) {
            Toast.makeText(getApplicationContext(), getString(R.string.bt_connect_ok), Toast.LENGTH_LONG).show();
            return true;
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.not_connect_btprinter), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void printPoLabel(String po) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon),
                getString(R.string.printingLabel), true);


        if (!BtPrintLabel.printPo(po)) {
            Toast.makeText(getApplicationContext(),getString(R.string.printLabalFailed),Toast.LENGTH_LONG).show();
        }

        dialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ENABLE_BT || requestCode == REQUEST_PRINTER_SETTING) {
                if(checkPrinterSetting())
                    printPoLabel("123456");
            }
        } else {
            if (requestCode == REQUEST_ENABLE_BT || requestCode == REQUEST_BLUETOOTH_SETTINGS) {
                if (BtPrintLabel.instance(getApplicationContext()) && BtPrintLabel.connect()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.bt_connect_ok), Toast.LENGTH_LONG).show();
                    printPoLabel("123456");
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.not_connect_btprinter), Toast.LENGTH_LONG).show();
                }

            }
        }
    }
}
