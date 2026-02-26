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
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tscdll.TscWifiActivity;
import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.SamsaraPoInfoHelper;
import com.senao.warehouse.handler.ShipmentPickingHandler;
import com.senao.warehouse.print.BtPrintLabel;
import com.senao.warehouse.util.Preferences;
import com.senao.warehouse.util.ReturnCode;

import java.util.ArrayList;
import java.util.List;

public class SamsaraPoTestActivity extends Activity {

    private static final String TAG = SamsaraPoTestActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PRINTER_SETTING = 2;
    private static final int REQUEST_BLUETOOTH_SETTINGS = 3;
    private ProgressDialog dialog;
    private String errorInfo = "";
    private TextView mConnection;
    private Button btnReturn, btnConfim, btnCancel;
    private EditText txtImprotDnNo, txtQrCode;
    private SamsaraPoInfoHelper samsaraPoInfoHelper;
    private ShipmentPickingHandler shipment;
    private boolean isBluetoothPrintSet = false;
    private RadioGroup rgPrinter;
    private RadioButton rbInvoiceNo, rbRecId, rbBluetooth, rbWifi;
    private TscWifiActivity TscEthernetDll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_samsara_po);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(
                            SamsaraPoTestActivity.this);
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

        rgPrinter = findViewById(R.id.radio_printer);
        rbBluetooth = findViewById(R.id.radio_bluetooth);
        rbBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !isBluetoothPrintSet) {
                    isBluetoothPrintSet = true;
                    checkPrinterSetting();
                }
            }
        });
        rbWifi = findViewById(R.id.radio_wifi);


        rbBluetooth.setChecked(true);


        shipment = new ShipmentPickingHandler();
        txtImprotDnNo = findViewById(R.id.edittext_dn_no);
        txtImprotDnNo.setFocusableInTouchMode(true);
        txtImprotDnNo.setSelectAllOnFocus(true);

        txtImprotDnNo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down" + v.getId());
                    hideKeyboard(v);
                    String dn = txtImprotDnNo.getText().toString().trim();
                    if (TextUtils.isEmpty(dn)) {
                        Toast.makeText(SamsaraPoTestActivity.this, getString(R.string.dn_num_is_not_null),
                                Toast.LENGTH_SHORT).show();
                        return true;
                    } else {
                        String result = parseDn(dn);
                        if (TextUtils.isEmpty(result)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.cant_resolve_dn), Toast.LENGTH_LONG).show();
                            return true;
                        } else {
                            txtImprotDnNo.setText(result);
                            //txtQrCode.requestFocus();
                        }
                    }
                }
                return false;
            }
        });

        txtQrCode = findViewById(R.id.edittext_qrcode);
        txtQrCode.setSelectAllOnFocus(true);

        btnReturn = findViewById(R.id.button_return);
        btnConfim = findViewById(R.id.button_confirm);
        btnCancel = findViewById(R.id.button_cancel);

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Return", String.valueOf(v.getId()));
                returnPage();
            }

        });

        btnConfim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Confim", String.valueOf(v.getId()));
//                if (!BtPrintLabel.printPo("8200000308682")) {
//                    errorInfo = "標籤列印失敗";
//                    mConnection.setText(getString(R.string.printer_connect_error));
//                    mConnection.setTextColor(Color.RED);
//                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
//
//                }



//                if (checkFields()) {
                    if (rbWifi.isChecked()) {
                        onCreateChoosePrinterDialog(samsaraPoInfoHelper).show();
                    }else{
                        printPoLabel(samsaraPoInfoHelper);
                    }

//                    getPoInfo();

//                }
            }

        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Cancel", String.valueOf(v.getId()));
                cleanData();
            }

        });

        txtImprotDnNo.requestFocus();
        checkPrinterSetting();

    }
    private Dialog onCreateChoosePrinterDialog(final SamsaraPoInfoHelper helper) {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer, null);
        final EditText etIP = item.findViewById(R.id.editTextIP);
        String ip = Preferences.getSharedPreferences(this).getString(
                Preferences.INVOICE_SEQ_PRINTER_IP, AppController.getProperties("SophosPrinterIP"));
        etIP.setText(ip);
        final EditText etPort = item.findViewById(R.id.editTextPort);
        String port = Preferences.getSharedPreferences(this).getString(
                Preferences.INVOICE_SEQ_PRINTER_PORT, AppController.getProperties("SophosPrinterPort"));
        etPort.setText(port);
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.INVOICE_SEQ_PRINTER_QTY, AppController.getProperties("SophosPrinterQty"));
        etQty.setText(qty);




//        int tol = helper.getPoList().length*Integer.parseInt(qty);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle(getString(R.string.label_printer_info)+"總共"+"張")
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

                        if (saveSophosPrinterInfo(etIP.getText().toString().trim(), etPort.getText().toString().trim(), etQty.getText().toString().trim())) {
                            alertDialog.dismiss();
                            dialog = ProgressDialog.show(SamsaraPoTestActivity.this,
                                    getString(R.string.holdon), getString(R.string.printingLabel), true);
                            if (!printOpen(etIP.getText().toString().trim(), etPort.getText().toString().trim())) {
                                Toast.makeText(getApplicationContext(), getString(R.string.cant_conect_printer), Toast.LENGTH_LONG).show();
                                view.setEnabled(true);
                                dialog.dismiss();
                                return;
                            }






                            int printTimes = Integer.parseInt(etQty.getText().toString());
                            for (int j = 0; j < printTimes; j++) {


//                                for (String po : helper.getPoList()) {
//



                                    if (!printLabelByWifi("testWIFItest")) {
                                        printClose();
                                        errorInfo = getString(R.string.printLabalFailed);
                                        mConnection.setText(getString(R.string.printer_connect_error));
                                        mConnection.setTextColor(Color.RED);
                                        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                                        dialog.dismiss();
                                        break;
                                    }
//                                }


                            }
                            printClose();
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        return alertDialog;


    }
    private void printClose() {
        try {
            TscEthernetDll.closeport(5000);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean printLabelByWifi(String po) {
        try {
            TscEthernetDll.clearbuffer();
            TscEthernetDll.sendcommand("SET TEAR ON\n");
            TscEthernetDll.sendcommand("CODEPAGE UTF-8\n");
            int x, y;

            x = mm2dot(3);
            y = mm2dot(15);
            TscEthernetDll.printerfont(x, y, "3", 0, 2, 2, "PO:");
            y = mm2dot(25);
            TscEthernetDll.printerfont(x, y, "3", 0, 2, 2, po);
            TscEthernetDll.printlabel(1, 1);


            TscEthernetDll.printlabel(1, 1);
            return true;
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
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

    private boolean printOpen(String ip, String port) {
        try {
            if (TscEthernetDll == null) {
                TscEthernetDll = new TscWifiActivity();
            }
            TscEthernetDll.openport(ip, Integer.parseInt(port));
            TscEthernetDll.setup(70, 50, 3, 10, 0, 3, 0);
            return true;
        } catch (Exception ex) {
            //Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
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

    private void returnPage() {
        finish();
    }

    public void onBackPressed() {
        // do something here and don't write super.onBackPressed()
        returnPage();
    }

    private boolean checkFields() {

        String dn = txtImprotDnNo.getText().toString().trim();

        if (TextUtils.isEmpty(dn)) {
            txtImprotDnNo.requestFocus();
            Toast.makeText(getApplicationContext(), getString(R.string.enter_dn),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(txtQrCode.getText().toString().trim())) {
            txtQrCode.requestFocus();
            Toast.makeText(getApplicationContext(), getString(R.string.enter_qrcode),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        String sn = getSN(txtQrCode.getText().toString().trim());

        if (TextUtils.isEmpty(sn)) {
            txtQrCode.requestFocus();
            Toast.makeText(getApplicationContext(), getString(R.string.qrcode_format_incorrect),
                    Toast.LENGTH_SHORT).show();
            return false;
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

    private void cleanData() {
        txtImprotDnNo.setText("");
        txtQrCode.setText("");
        txtImprotDnNo.requestFocus();
    }

    private void getPoInfo() {
        dialog = ProgressDialog.show(this,
                getString(R.string.holdon), getString(R.string.data_updating), true);

        samsaraPoInfoHelper = new SamsaraPoInfoHelper();
        samsaraPoInfoHelper.setDn(Integer.valueOf(txtImprotDnNo.getText().toString().trim()));
        samsaraPoInfoHelper.setSn(getSN(txtQrCode.getText().toString().trim()));

        new GetPoInfo().execute(0);
    }

    private void printPoLabel(SamsaraPoInfoHelper helper) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon),
                getString(R.string.printingLabel), true);

        errorInfo = "";
//        for (String po : helper.getPoList()) {
            if (!BtPrintLabel.printPo("test藍芽test")) {
                errorInfo = getString(R.string.printLabalFailed);
                mConnection.setText(getString(R.string.printer_connect_error));
                mConnection.setTextColor(Color.RED);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
//                break;
            }
//        }

        dialog.dismiss();
    }

    private String getSN(String qrCode) {
        String[] snList = null;
        if (qrCode.indexOf("\r\n") > 0) {
            snList = qrCode.split("\r\n");
        } else if (qrCode.indexOf("\r") > 0) {
            snList = qrCode.split("\r");
        } else if (qrCode.indexOf("\n") > 0) {
            snList = qrCode.split("\n");
        }
        List<String> list = new ArrayList<>();
        if (snList == null || snList.length < 4) {
            Toast.makeText(getApplicationContext(), getString(R.string.qr_code_format_incorrect_cant_resolve_sn), Toast.LENGTH_LONG).show();
        } else {
            for (int i = 0; i < snList.length; i++) {
                if (i > 2) {
                    list.add(snList[i]);
                }
            }
        }
        if (list.size() > 0)
            return list.get(0);
        else
            return null;
    }

    private class GetPoInfo extends
            AsyncTask<Integer, String, SamsaraPoInfoHelper> {


        @Override
        protected SamsaraPoInfoHelper doInBackground(Integer... params) {

            AppController.debug("Get Samsara Po Info from " + AppController.getServerInfo()
                    + AppController.getProperties("SamsaraPoInfo"));
            publishProgress(getString(R.string.data_reading));
            return shipment.getSamsaraPoInfo(samsaraPoInfoHelper);
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
        protected void onPostExecute(SamsaraPoInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);

            AppController.debug("onPostExecute() result json = "
                    + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    samsaraPoInfoHelper = result;
                    mConnection.setText(getString(R.string.data_updated_successfully));
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    dialog.dismiss();
                    errorInfo = "";
                    if (samsaraPoInfoHelper.getPoList() != null && samsaraPoInfoHelper.getPoList().length > 0) {
                        if (rbWifi.isChecked()) {
                            onCreateChoosePrinterDialog(samsaraPoInfoHelper).show();
                        }else{
                            printPoLabel(samsaraPoInfoHelper);
                        }



                    } else {
                        Toast.makeText(getApplicationContext(), "can't find Samsara Po，please contact MIS", Toast.LENGTH_LONG).show();
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
                    dialog.dismiss();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                dialog.dismiss();
            }
        }

    }

}
