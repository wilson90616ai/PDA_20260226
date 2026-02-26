package com.senao.warehouse.ui;

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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.R;
import com.senao.warehouse.print.BtPrintLabel;
import com.senao.warehouse.database.PoInfoHelper;
import com.senao.warehouse.database.TYPE_FLAG;
import com.senao.warehouse.handler.ShipmentPickingHandler;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;

import java.util.ArrayList;
import java.util.List;

public class ShipmentQueryPoActivity extends Activity implements TextView.OnEditorActionListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PRINTER_SETTING = 2;
    private static final int REQUEST_BLUETOOTH_SETTINGS = 3;
    private ProgressDialog dialog;
    private String errorInfo = "";
    private TextView mConnection, txtPo, txtOe,lblTitle;
    private Button btnReturn, btnConfim, btnCancel;
    private EditText txtValue, txtQrCode;
    private PoInfoHelper poInfoHelper;
    private ShipmentPickingHandler shipment;
    private RadioGroup rbGroup;
    private RadioButton rbCartonNo, rbSn, rbSnQrCode;

    private RadioGroup.OnCheckedChangeListener listener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            mConnection.setText("");
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));

            switch (checkedId) {
                case R.id.radio_box:
                    Log.d("onClick Radio Box", String.valueOf(checkedId));
                    txtValue.setVisibility(View.VISIBLE);
                    txtQrCode.setVisibility(View.GONE);
                    txtValue.requestFocus();
                    break;
                case R.id.radio_sn:
                    Log.d("onClick Radio SN", String.valueOf(checkedId));
                    txtValue.setVisibility(View.VISIBLE);
                    txtQrCode.setVisibility(View.GONE);
                    txtValue.requestFocus();
                    break;
                case R.id.radio_sn_qrcode:
                    Log.d("onClick Radio QR Code", String.valueOf(checkedId));
                    txtValue.setVisibility(View.GONE);
                    txtQrCode.setVisibility(View.VISIBLE);
                    txtQrCode.requestFocus();
                    break;
            }

            cleanData();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipment_query_po);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(
                            ShipmentQueryPoActivity.this);
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

        rbGroup = findViewById(R.id.radio_group);
        rbCartonNo = findViewById(R.id.radio_box);
        rbSn = findViewById(R.id.radio_sn);
        rbSnQrCode = findViewById(R.id.radio_sn_qrcode);
        rbGroup.setOnCheckedChangeListener(listener);

        txtValue = findViewById(R.id.edittext_value);
        txtValue.setOnEditorActionListener(this);
        txtQrCode = findViewById(R.id.edittext_qrcode);
        txtQrCode.setOnEditorActionListener(this);

        txtPo = findViewById(R.id.txt_po);
        txtOe = findViewById(R.id.txt_oe);

        shipment = new ShipmentPickingHandler();
        lblTitle = findViewById(R.id.ap_title);
        setTitle();
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
                if (checkFields()) {
                    getPoInfo();
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

        rbSn.setChecked(true);
        checkPrinterSetting();
    }

    private void returnPage() {
        finish();
    }

    public void onBackPressed() {
        // do something here and don't write super.onBackPressed()
        returnPage();
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.title_activity_shippment_query_po1, AppController.getOrgName()));
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private boolean checkFields() {
        String value = txtValue.getText().toString().trim();

        if (rbCartonNo.isChecked()) {
            if (TextUtils.isEmpty(value)) {
                txtValue.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_box_nu), Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (rbSn.isChecked()) {
            if (TextUtils.isEmpty(value)) {
                txtValue.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_sn), Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (rbSnQrCode.isChecked()) {

            if (TextUtils.isEmpty(txtQrCode.getText().toString().trim())) {
                txtQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_qrcode), Toast.LENGTH_SHORT).show();
                return false;
            }

            String sn = getSN(txtQrCode.getText().toString().trim());

            if (TextUtils.isEmpty(sn)) {
                txtQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.qrcode_format_incorrect), Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            rbGroup.requestFocus();
            Toast.makeText(getApplicationContext(), getString(R.string.plz_enter_query_condictions), Toast.LENGTH_SHORT).show();
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
        txtValue.setText("");
        txtQrCode.setText("");
        txtPo.setText(getString(R.string.txt_po, ""));
        txtOe.setText(getString(R.string.txt_oe, ""));
    }

    private void setResult() {
        txtPo.setText(getString(R.string.txt_po, poInfoHelper.getPo()));
        txtOe.setText(getString(R.string.txt_oe, poInfoHelper.getOe()));
    }

    private void getPoInfo() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        poInfoHelper = new PoInfoHelper();

        if (rbCartonNo.isChecked()) {
            poInfoHelper.setTypeFlag(TYPE_FLAG.CTN.ordinal());
            poInfoHelper.setValue(txtValue.getText().toString().trim());
        } else if (rbSn.isChecked()) {
            poInfoHelper.setTypeFlag(TYPE_FLAG.SN.ordinal());
            poInfoHelper.setValue(txtValue.getText().toString().trim());
        } else {
            poInfoHelper.setTypeFlag(TYPE_FLAG.SN_BOX.ordinal());
            poInfoHelper.setValue(getSN(txtQrCode.getText().toString().trim()));
        }

        new GetPoInfo().execute(0);
    }

    private void printPoLabel() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.printingLabel), true);
        errorInfo = "";

        if (!BtPrintLabel.printPoAndOe(poInfoHelper.getPo(), poInfoHelper.getOe())) {
            errorInfo = getString(R.string.printLabalFailed);
            mConnection.setText(getString(R.string.printer_connect_error));
            mConnection.setTextColor(Color.RED);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

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

    private void showAskDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.Print));
        dialog.setMessage(getString(R.string.print_oe_po_question, poInfoHelper.getOe(), poInfoHelper.getPo()));
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setCancelable(false);
        dialog.setNegativeButton(getString(R.string.no),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int arg1) {
                        dialog.dismiss();
                    }

                });

        dialog.setPositiveButton(getString(R.string.yes),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int arg1) {
                        dialog.dismiss();
                        printPoLabel();
                    }
                });

        dialog.show();
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        final boolean isEnterEvent = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
        final boolean isEnterUpEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_UP;
        final boolean isEnterDownEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_DOWN;

        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || isEnterUpEvent) {
            // Do your action here
            EditText et = (EditText) textView;

            switch (et.getId()) {
                case R.id.edittext_value:
                    if (checkFields()) {
                        getPoInfo();
                    }
                    break;
                case R.id.radio_sn_qrcode:
                    if (checkFields()) {
                        getPoInfo();
                    }
                    break;
                default:
                    break;
            }

            return false;
        } else if (isEnterDownEvent) {
            // Capture this event to receive ACTION_UP
            return true;
        } else {
            // We do not care on other actions
            return false;
        }
    }

    private class GetPoInfo extends AsyncTask<Integer, String, PoInfoHelper> {
        @Override
        protected PoInfoHelper doInBackground(Integer... params) {
            AppController.debug("Get Po Info from " + AppController.getServerInfo()
                    + AppController.getProperties("PoInfo"));
            publishProgress(getString(R.string.data_reading));
            return shipment.getPoInfo(poInfoHelper);
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
        protected void onPostExecute(PoInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    poInfoHelper = result;
                    mConnection.setText(R.string.get_data_success);
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    dialog.dismiss();
                    errorInfo = "";

                    if (poInfoHelper.getPo() != null && poInfoHelper.getOe() != null) {
                        setResult();
                        showAskDialog();
                    } else {
                        Toast.makeText(getApplicationContext(), "can't find Po，please contact MIS", Toast.LENGTH_LONG).show();
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
