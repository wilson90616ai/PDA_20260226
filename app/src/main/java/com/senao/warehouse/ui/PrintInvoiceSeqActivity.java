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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.senao.warehouse.database.InvoiceHelper;
import com.senao.warehouse.handler.PrintLabelHandler;
import com.senao.warehouse.print.BtPrintLabel;
import com.senao.warehouse.util.CharUtil;
import com.senao.warehouse.util.Preferences;
import com.senao.warehouse.util.ReturnCode;

public class PrintInvoiceSeqActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PRINTER_SETTING = 2;
    private static final int REQUEST_BLUETOOTH_SETTINGS = 3;
    private static final String TAG = PrintInvoiceSeqActivity.class.getSimpleName();
    private TextView mConnection, lblVendorName,lblTitle;
    private ProgressDialog dialog;
    private String errorInfo = "";
    private PrintLabelHandler print;
    private InvoiceHelper invoiceHelper;
    private EditText txtImportInvoiceNo, txtImportRecId;
    private RadioGroup rgPrinter;
    private RadioButton rbInvoiceNo, rbRecId, rbBluetooth, rbWifi;
    private Button btnReturn, btnConfim, btnCancel;
    private CheckBox cbGenInvoiceNo;
    private boolean isBluetoothPrintSet = false;
    private TscWifiActivity TscEthernetDll;

    private CompoundButton.OnCheckedChangeListener rbListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                invoiceHelper = null;
                lblVendorName.setVisibility(View.GONE);
                if (buttonView.equals(rbInvoiceNo)) {
                    rbRecId.setChecked(false);
                    txtImportRecId.setText("");
                    txtImportRecId.setEnabled(false);
                    txtImportInvoiceNo.setEnabled(true);
                    txtImportInvoiceNo.requestFocus();
                } else {
                    rbInvoiceNo.setChecked(false);
                    txtImportInvoiceNo.setText("");
                    txtImportInvoiceNo.setEnabled(false);
                    txtImportRecId.setEnabled(true);
                    txtImportRecId.requestFocus();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_invoice_seq);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(PrintInvoiceSeqActivity.this);
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

        cbGenInvoiceNo = findViewById(R.id.checkbox_gen_invoice_no);
        rbInvoiceNo = findViewById(R.id.radio_invoice_no);
        txtImportInvoiceNo = findViewById(R.id.edittext_invoice_no);
        txtImportInvoiceNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtImportInvoiceNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    Log.d(TAG, "onKeyListener Enter or Action Down" + v.getId());

                    if (txtImportInvoiceNo.getText().toString().trim().length() > 0) {
                        hideKeyboard();
                        doQuerySeq(false);
                    }

                    return true;
                }

                return false;
            }
        });

        rbRecId = findViewById(R.id.radio_invoice_seq);
        txtImportRecId = findViewById(R.id.edittext_rec_id);
        txtImportRecId.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtImportRecId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    Log.d(TAG, "onKeyListener Enter or Action Down" + v.getId());

                    if (txtImportRecId.getText().toString().trim().length() > 0) {
                        hideKeyboard();
                        doQuerySeq(false);
                    }

                    return true;
                }

                return false;
            }
        });

        lblVendorName = findViewById(R.id.label_vendor_name);
        lblVendorName.setVisibility(View.GONE);

        rbInvoiceNo.setOnCheckedChangeListener(rbListener);
        rbRecId.setOnCheckedChangeListener(rbListener);

        rbInvoiceNo.setChecked(true);

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

        btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Return", String.valueOf(v.getId()));
                returnPage();
            }

        });

        btnConfim = findViewById(R.id.button_confirm);
        btnConfim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Confim", String.valueOf(v.getId()));
                //printTest();

                if (checkFields()) {
                    if (invoiceHelper != null && invoiceHelper.getIntRetCode() == ReturnCode.OK) {
                        if (rbInvoiceNo.isChecked() && txtImportInvoiceNo.getText().toString().trim().equals(invoiceHelper.getInvoice())
                                && !TextUtils.isEmpty(invoiceHelper.getSeq())
                                && !TextUtils.isEmpty(invoiceHelper.getVendorName())) {
                            if (rbWifi.isChecked()) {
                                onCreateChoosePrinterDialog().show();
                            } else {
                                printLabelByBluetooth(invoiceHelper.getSeq());
                            }

                            return;
                        }
                        if (rbRecId.isChecked() && txtImportRecId.getText().toString().trim().equals(invoiceHelper.getSeq())
                                && !TextUtils.isEmpty(invoiceHelper.getVendorName())) {
                            if (rbWifi.isChecked()) {
                                onCreateChoosePrinterDialog().show();
                            } else {
                                printLabelByBluetooth(invoiceHelper.getSeq());
                            }

                            return;
                        }
                    }

                    doQuerySeq(true);
                }
            }

        });

        btnCancel = findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Cancel", String.valueOf(v.getId()));
                cleanData();
            }

        });

        print = new PrintLabelHandler();
        rbBluetooth.setChecked(true);
        lblTitle = findViewById(R.id.ap_title);
        setTitle();
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.label_print_invoice_seq1, AppController.getOrgName()));
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private void printTest() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.printingLabel), true);
        errorInfo = "";
        String seq = "NN190627-15AW";

        if (!BtPrintLabel.printInvoiceSeq(seq)) {
            errorInfo = getString(R.string.printLabalFailed) + " PrintInoiceSeq1";
            mConnection.setText(getString(R.string.printer_connect_error));
            mConnection.setTextColor(Color.RED);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        dialog.dismiss();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtImportInvoiceNo.getWindowToken(), 0);
    }

    private void doQuerySeq(boolean byBtn) {
        invoiceHelper = new InvoiceHelper();

        if (rbInvoiceNo.isChecked()) {
            invoiceHelper.setInvoice(txtImportInvoiceNo.getText().toString().trim());
        } else {
            invoiceHelper.setSeq(txtImportRecId.getText().toString().trim());
        }

        invoiceHelper.setSupplementary(cbGenInvoiceNo.isChecked());
        invoiceHelper.setByBtn(byBtn);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new QuerySeq().execute(0);
    }

    private void printLabelByBluetooth(String seq) {
        dialog = ProgressDialog.show(PrintInvoiceSeqActivity.this, getString(R.string.holdon), getString(R.string.printingLabel), true);
        errorInfo = "";

        if (!BtPrintLabel.printInvoiceSeq(seq)) {
            errorInfo = getString(R.string.printLabalFailed) + " PrintInoiceSeq1";
            mConnection.setText(getString(R.string.printer_connect_error));
            mConnection.setTextColor(Color.RED);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        dialog.dismiss();
    }

    private boolean printLabelByWifi(String seq) {
        try {
            TscEthernetDll.clearbuffer();
            TscEthernetDll.sendcommand("SET TEAR ON\n");
            TscEthernetDll.sendcommand("CODEPAGE UTF-8\n");
            int x, y;

            x = mm2dot(5);
            y = mm2dot(0);
            TscEthernetDll.printerfont(x, y, "0", 0, 5, 5, "|");
            y = mm2dot(2);
            TscEthernetDll.printerfont(x, y, "0", 0, 5, 5, "|");
            y = mm2dot(4);
            TscEthernetDll.printerfont(x, y, "0", 0, 5, 5, "|");
            y = mm2dot(6);
            TscEthernetDll.printerfont(x, y, "0", 0, 5, 5, "|");
            y = mm2dot(8);
            TscEthernetDll.printerfont(x, y, "0", 0, 5, 5, "|");

            x = mm2dot(8);
            y = mm2dot(3);
            TscEthernetDll.printerfont(x, y, "0", 0, 15, 15, "Rec ID:");
            x = mm2dot(24);
            y = mm2dot(1);
            TscEthernetDll.barcode(x, y, "128", 25, 0, 0, 2, 5, seq);
            x = mm2dot(24);
            y = mm2dot(5);
            TscEthernetDll.printerfont(x, y, "0", 0, 12, 12, seq);

            TscEthernetDll.printlabel(1, 1);
            return true;
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean checkFields() {
        if (rbInvoiceNo.isChecked()) {
            String invoiceNo = txtImportInvoiceNo.getText().toString().trim();
            if (TextUtils.isEmpty(invoiceNo)) {
                txtImportInvoiceNo.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_invoice), Toast.LENGTH_SHORT).show();
                return false;
            }

            txtImportInvoiceNo.setText(invoiceNo);
        } else {
            String invoiceSeq = txtImportRecId.getText().toString().trim();
            if (TextUtils.isEmpty(invoiceSeq)) {
                txtImportRecId.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.label_import_invoice_seq), Toast.LENGTH_SHORT).show(); //"請輸入發票流水號"
                return false;
            }

            txtImportRecId.setText(invoiceSeq);
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
        cbGenInvoiceNo.setChecked(false);
        txtImportInvoiceNo.setText("");
        txtImportRecId.setText("");
        lblVendorName.setVisibility(View.GONE);
        rbInvoiceNo.setChecked(true);
        invoiceHelper = null;
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

    private void setSupplementary() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_setting), true);
        new SetSupplementary().execute(0);
    }

    private void getSeq() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetSeq().execute(0);
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

    private void printClose() {
        try {
            TscEthernetDll.closeport(5000);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean printLabelByBluetooth(String reelId, String partNo, String Description, String qty,
                                          String dateCode, String vendorCode, String po, String qrCode) {
        try {
            TscEthernetDll.clearbuffer();
            TscEthernetDll.sendcommand("SET TEAR ON\n");
            TscEthernetDll.sendcommand("CODEPAGE UTF-8\n");
            Description = "Description:  " + Description;
            byte[] des = null;
            if (CharUtil.isChineseOrGreek(Description)) {
                des = Description.getBytes("Big5");
            }
            int x, y;
            x = mm2dot(1);
            y = mm2dot(3);
            TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "Reel ID:");
            x = mm2dot(9);
            y = mm2dot(2);
            TscEthernetDll.barcode(x, y, "128", 30, 1, 0, 2, 2, reelId);
            x = mm2dot(1);
            y = mm2dot(10);
            TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "P/N:");
            x = mm2dot(8);
            y = mm2dot(8);
            TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 2, partNo);
            x = mm2dot(8);
            y = mm2dot(11);
            TscEthernetDll.printerfont(x, y, "0", 0, 12, 12, partNo);
            x = mm2dot(1);
            y = mm2dot(16);
            if (des == null) {
                TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, Description);
            } else {
                TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
                TscEthernetDll.sendcommand(des);
                TscEthernetDll.sendcommand("\"\n");
            }
            y = mm2dot(22);
            TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "Quantity:");
            x = mm2dot(13);
            y = mm2dot(20);
            TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 2, qty);
            x = mm2dot(13);
            y = mm2dot(23);
            TscEthernetDll.printerfont(x, y, "0", 0, 12, 12, qty);
            x = mm2dot(1);
            y = mm2dot(29);
            TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "Date Code:");
            x = mm2dot(13);
            y = mm2dot(27);
            TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 2, dateCode);
            x = mm2dot(13);
            y = mm2dot(30);
            TscEthernetDll.printerfont(x, y, "0", 0, 12, 12, dateCode);
            x = mm2dot(1);
            y = mm2dot(36);
            TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "Vendor Code:");
            x = mm2dot(14);
            y = mm2dot(34);
            TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 2, vendorCode);
            x = mm2dot(14);
            y = mm2dot(37);
            TscEthernetDll.printerfont(x, y, "0", 0, 12, 12, vendorCode);
            if (!TextUtils.isEmpty(po) && !po.toUpperCase().equals("NULL")) {
                x = mm2dot(1);
                y = mm2dot(43);
                TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "PO NO:");
                x = mm2dot(9);
                y = mm2dot(41);
                TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 2, po);
                x = mm2dot(9);
                y = mm2dot(44);
                TscEthernetDll.printerfont(x, y, "0", 0, 12, 12, po);
            }
//            x = mm2dot(36);
//            y = mm2dot(20);
            x = mm2dot(45);
            y = mm2dot(23);
            TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", qrCode);
            x = mm2dot(48);
            y = mm2dot(46);
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

    private Dialog onCreateChoosePrinterDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer, null);
        final EditText etIP = item.findViewById(R.id.editTextIP);
        String ip = Preferences.getSharedPreferences(this).getString(
                Preferences.INVOICE_SEQ_PRINTER_IP, AppController.getProperties("InvoiceSeqPrinterIP"));
        etIP.setText(ip);
        final EditText etPort = item.findViewById(R.id.editTextPort);
        String port = Preferences.getSharedPreferences(this).getString(
                Preferences.INVOICE_SEQ_PRINTER_PORT, AppController.getProperties("InvoiceSeqPrinterPort"));
        etPort.setText(port);
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.INVOICE_SEQ_PRINTER_QTY, AppController.getProperties("InvoiceSeqPrinterQty"));
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
                            alertDialog.dismiss();
                            dialog = ProgressDialog.show(PrintInvoiceSeqActivity.this, getString(R.string.holdon), getString(R.string.printingLabel), true);

                            if (!printOpen(etIP.getText().toString().trim(), etPort.getText().toString().trim())) {
                                Toast.makeText(getApplicationContext(), getString(R.string.cant_conect_printer), Toast.LENGTH_LONG).show();
                                view.setEnabled(true);
                                dialog.dismiss();
                                return;
                            }

                            int printTimes = Integer.parseInt(etQty.getText().toString());
                            for (int j = 0; j < printTimes; j++) {
                                if (!printLabelByWifi(invoiceHelper.getSeq())) {
                                    printClose();
                                    Toast.makeText(getApplicationContext(), getString(R.string.print_error), Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                    return;
                                }
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

    private boolean savePrinterInfo(String ip, String port, String qty) {
        try {
            Preferences.setString(this, Preferences.INVOICE_SEQ_PRINTER_IP, ip);
            Preferences.setString(this, Preferences.INVOICE_SEQ_PRINTER_PORT, port);
            Preferences.setString(this, Preferences.INVOICE_SEQ_PRINTER_QTY, qty);
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

    private class QuerySeq extends AsyncTask<Integer, String, InvoiceHelper> {
        @Override
        protected InvoiceHelper doInBackground(Integer... params) {
            AppController.debug("Query Invoice Seq from " + AppController.getServerInfo()
                    + AppController.getProperties("QryInvoiceSeq"));
            publishProgress(getString(R.string.downloading_data));
            return print.qryInvoiceSeq(invoiceHelper);
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
        protected void onPostExecute(InvoiceHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                invoiceHelper = result;
                if (invoiceHelper.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    lblVendorName.setVisibility(View.VISIBLE);
                    lblVendorName.setText(getString(R.string.label_vendor_no_name, invoiceHelper.getVendorName()));

                    if (rbInvoiceNo.isChecked()) {
                        txtImportRecId.setText(invoiceHelper.getSeq());
                    } else {
                        txtImportInvoiceNo.setText(invoiceHelper.getInvoice());
                    }

                    if (invoiceHelper.isByBtn()) {
                        if (cbGenInvoiceNo.isChecked()) {
                            if (TextUtils.isEmpty(invoiceHelper.getSeq())) {
                                getSeq();
                            } else {
                                setSupplementary();
                            }
                        } else {
                            if (TextUtils.isEmpty(invoiceHelper.getSeq())) {
                                getSeq();
                            } else {
                                if (rbWifi.isChecked()) {
                                    onCreateChoosePrinterDialog().show();
                                } else {
                                    printLabelByBluetooth(invoiceHelper.getSeq());
                                }
                            }
                        }
                    }
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = invoiceHelper.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    lblVendorName.setVisibility(View.GONE);
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                lblVendorName.setVisibility(View.GONE);
            }
        }
    }

    private class SetSupplementary extends AsyncTask<Integer, String, InvoiceHelper> {
        @Override
        protected InvoiceHelper doInBackground(Integer... params) {
            AppController.debug("Set Supplementary from " + AppController.getServerInfo()
                    + AppController.getProperties("GetInvoiceSeq"));
            publishProgress(getString(R.string.data_setting));
            return print.getInvoiceSeq(invoiceHelper);
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
        protected void onPostExecute(InvoiceHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                invoiceHelper = result;
                if (invoiceHelper.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";

                    if (rbWifi.isChecked()) {
                        onCreateChoosePrinterDialog().show();
                    } else {
                        printLabelByBluetooth(invoiceHelper.getSeq());
                    }
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = invoiceHelper.getStrErrorBuf();
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

    private class GetSeq extends AsyncTask<Integer, String, InvoiceHelper> {
        @Override
        protected InvoiceHelper doInBackground(Integer... params) {
            AppController.debug("Get Invoice Seq from " + AppController.getServerInfo()
                    + AppController.getProperties("GetInvoiceSeq"));
            publishProgress(getString(R.string.downloading_data));
            return print.getInvoiceSeq(invoiceHelper);
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
        protected void onPostExecute(InvoiceHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                invoiceHelper = result;
                if (invoiceHelper.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    txtImportRecId.setText(invoiceHelper.getSeq());

                    if (rbWifi.isChecked()) {
                        onCreateChoosePrinterDialog().show();
                    } else {
                        printLabelByBluetooth(invoiceHelper.getSeq());
                    }
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = invoiceHelper.getStrErrorBuf();
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
}
