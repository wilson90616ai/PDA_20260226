package com.senao.warehouse.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.print.BtPrintLabel;
import com.senao.warehouse.database.InvoiceHelper;
import com.senao.warehouse.database.RECEIVING_TYPE;
import com.senao.warehouse.database.VendorInfoHelper;
import com.senao.warehouse.handler.PrintLabelHandler;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.Preferences;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.util.Util;

public class MaterialReceivingInvoiceLocatorActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PRINTER_SETTING = 2;
    private static final int REQUEST_BLUETOOTH_SETTINGS = 3;
    private VendorInfoHelper vendorInfo;
    private int accType;
    private Button btnReturn;
    private Button btnConfirm;
    private Button btnCancel;
    private EditText txtImportInvoiceNo, txtImportLocator;
    private TextView lblTitle, mConnection;
    private String errorInfo;
    private RECEIVING_TYPE receivingType;
    private CheckBox cbGenInvoiceNo;
    private ProgressDialog dialog;
    private InvoiceHelper invoiceHelper;
    private PrintLabelHandler print;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiving_invoice_locator);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            vendorInfo = new Gson().fromJson(extras.getString("VENDOR_INFO"), VendorInfoHelper.class);

            if (vendorInfo == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_vc_data), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            receivingType = RECEIVING_TYPE.valueOf(vendorInfo.getReceivingType());
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_vc_data), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MaterialReceivingInvoiceLocatorActivity.this);
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

        lblTitle = findViewById(R.id.ap_title);

        accType = vendorInfo.isOutSourcing() ? 1 : 0;

        btnReturn = findViewById(R.id.button_return);

        cbGenInvoiceNo = findViewById(R.id.checkbox_gen_invoice_no);
        txtImportInvoiceNo = findViewById(R.id.edittext_import_invoice_no);
        txtImportInvoiceNo.setSelectAllOnFocus(true);
        txtImportLocator = findViewById(R.id.edittext_import_locator);
        txtImportInvoiceNo.setSelectAllOnFocus(true);

        if (vendorInfo.getIsRemittance() != null && vendorInfo.getIsRemittance().equals("N")) {
            cbGenInvoiceNo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        txtImportInvoiceNo.setText(getString(R.string.txt_gen_invoice_no, getInvoiceNo()));
                        int pos = txtImportInvoiceNo.getText().length();
                        txtImportInvoiceNo.setSelection(pos);
                    }
                }
            });
        } else {
            cbGenInvoiceNo.setVisibility(View.GONE);
        }

        btnConfirm = findViewById(R.id.button_confirm);
        btnCancel = findViewById(R.id.button_cancel);

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Return", String.valueOf(v.getId()));
                returnPage();
            }

        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Confirm", String.valueOf(v.getId()));

                if (checkFields()) {
                    if (vendorInfo.getIsRemittance() != null && vendorInfo.getIsRemittance().equals("N")) {
                        doQuerySeq();
                    } else {
                        goToNextPage(receivingType);
                    }
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Cancel", String.valueOf(v.getId()));
                cleanData();
            }
        });

        setTitle();
        checkPrinterSetting();
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

    @SuppressLint("DefaultLocale")
    private String getInvoiceNo() {
        String currentDate = Util.getCurrentDate();
        String invoiceNo = Preferences.getString(this, Preferences.INVOICE_NO);
        int seq;

        if (invoiceNo != null && currentDate.equals(invoiceNo.substring(0, 8))) {
            seq = Integer.valueOf(invoiceNo.substring(8));
            seq++;
        } else {
            seq = 1;
        }

        String newInvoiceNo = String.format("%s%04d", currentDate, seq);
        Preferences.setString(this, Preferences.INVOICE_NO, newInvoiceNo);
        return newInvoiceNo;
    }

    private void doQuerySeq() {
        print = new PrintLabelHandler();
        invoiceHelper = new InvoiceHelper();
        invoiceHelper.setVendorId(vendorInfo.getId());
        invoiceHelper.setInvoice(txtImportInvoiceNo.getText().toString().trim());
        invoiceHelper.setSupplementary(cbGenInvoiceNo.isChecked());
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetSeq().execute(0);
    }

    private void goToNextPage(RECEIVING_TYPE type) {
        Intent intent = new Intent(this, MaterialReceivingListActivity.class);
        vendorInfo.setOutSourcing(accType == 1);
        vendorInfo.setReceivingType(type.name());
        vendorInfo.setInvoiceNo(txtImportInvoiceNo.getText().toString().trim());
        vendorInfo.setLocator(txtImportLocator.getText().toString().trim());

        if (invoiceHelper != null) {
            vendorInfo.setInvoiceSeq(invoiceHelper.getSeq());
        }

        intent.putExtra("VENDOR_INFO", new Gson().toJson(vendorInfo));
        startActivityForResult(intent, type.ordinal());
    }

    private boolean checkFields() {
        if (!checkInvoiceNo()) {
            return false;
        }

        if (txtImportLocator.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_loc), Toast.LENGTH_LONG).show();
            txtImportLocator.requestFocus();
            return false;
        }

        if (txtImportLocator.getText().toString().trim().equals(Constant.LOCATOR_P023)) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_other_loc), Toast.LENGTH_LONG).show();
            txtImportLocator.requestFocus();
            return false;
        }

        return true;
    }

    private void setTitle() {
        String subtitle = null;

        switch (receivingType) {
            case REC_COMBINE:
                subtitle = getString(R.string.combine) + " - " + getString(R.string.Receiving);
                break;
            case TEMP_COMBINE:
                subtitle = getString(R.string.combine) + " - " + getString(R.string.Pending_inspection);
                break;
            case REC_PO:
                subtitle = getString(R.string.po) + " - " + getString(R.string.Receiving);
                break;
            case TEMP_PO:
                subtitle = getString(R.string.po) + " - " + getString(R.string.Pending_inspection);
                break;
        }

        final SpannableString text;

        if (accType == MaterialReceivingActivity.OUTSOURCING) {
            text = new SpannableString(getString(R.string.label_receiving_outsourcing1,AppController.getOrgName(), subtitle));
        } else {
            text = new SpannableString(getString(R.string.label_receiving1,AppController.getOrgName(), subtitle));
        }

        text.setSpan(new RelativeSizeSpan(1.0f), 0, 6,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new RelativeSizeSpan(0.7f), 7+AppController.getOrgName().length(), 7+AppController.getOrgName().length() + subtitle.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private boolean checkInvoiceNo() {
        if (txtImportInvoiceNo.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_invoice), Toast.LENGTH_LONG).show();
            txtImportInvoiceNo.requestFocus();
            return false;
        } else if (txtImportInvoiceNo.getText().toString().trim().length() < 10) {
            Toast.makeText(getApplicationContext(),getString( R.string.enter_correct_invoice), Toast.LENGTH_LONG).show();
            txtImportInvoiceNo.requestFocus();
            return false;
        }

        return true;
    }

    private void returnPage() {
        finish();
    }

    protected void cleanData() {
        cbGenInvoiceNo.setChecked(false);
        txtImportInvoiceNo.setText("");
        txtImportLocator.setText("");
        txtImportInvoiceNo.requestFocus();
    }

    private void printLabel(String seq) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.printingLabel), true);
        errorInfo = "";

        if (!BtPrintLabel.printInvoiceSeq(seq)) {
            errorInfo = getString(R.string.printLabalFailed);
            mConnection.setText(getString(R.string.printer_connect_error));
            mConnection.setTextColor(Color.RED);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        dialog.dismiss();
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
                    printLabel(invoiceHelper.getSeq());
                    goToNextPage(receivingType);
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
