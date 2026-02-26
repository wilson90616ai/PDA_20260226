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
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
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
import com.senao.warehouse.database.ITEM_LABEL_QR_CODE_FORMAT;
import com.senao.warehouse.database.MaterialLabelHelper;
import com.senao.warehouse.database.PnItemInfoHelper;
import com.senao.warehouse.handler.PrintLabelHandler;
import com.senao.warehouse.print.BtPrintLabel;
import com.senao.warehouse.util.CharUtil;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.Preferences;
import com.senao.warehouse.util.QrCodeUtil;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.util.Util;

public class WoLabelPrintActivity extends Activity {

    private static final String TAG = WoLabelPrintActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PRINTER_SETTING = 2;
    private static final int REQUEST_BLUETOOTH_SETTINGS = 3;
    private TextView mConnection,lblTitle;
    private Button btnReturn, btnConfim, btnCancel;
    private ProgressDialog dialog;
    private String errorInfo = "",orgPrint="";
    private PrintLabelHandler print;
    private PnItemInfoHelper pnInfo;
    private MaterialLabelHelper mlInfo;
    private EditText txtImportLine, txtImportMergeNo, txtImportQty, txtImportPartNo, txtImportDC, txtImportVC,
            txtImportPo, txtImportRemark, txtImportReelIdLine, txtImportReelIdMergeNo, txtImportReelId,
            txtImportReelIdQty, txtImportReelIdRemark, txtImportReelIdPo, txtImportQrCodeLine, txtImportQrCodeMergeNo,
            txtImportQrCode, txtImportQrCodeRemark;
    private EditText txtImportCOO, txtImportReelIdCOO; //20260120 Ann Add
    private RadioGroup rgDC, rgPrinter;
    private RadioButton rbQrCode, rbReelID, rbPartNo, rbBluetooth, rbWifi;
    private LinearLayout qrcodeLayout, pnLayout, reelIdLayout;
    private boolean isBluetoothPrintSet = false;
    private TscWifiActivity TscEthernetDll;

    private RadioGroup.OnCheckedChangeListener dcListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio_dc_qrcode:
                    if (rbQrCode.isChecked()) {
                        pnLayout.setVisibility(View.GONE);
                        reelIdLayout.setVisibility(View.GONE);
                        qrcodeLayout.setVisibility(View.VISIBLE);
                        rbQrCode.requestFocus();
                    }
                    break;
                case R.id.radio_reel_id:
                    if (rbReelID.isChecked()) {
                        pnLayout.setVisibility(View.GONE);
                        reelIdLayout.setVisibility(View.VISIBLE);
                        qrcodeLayout.setVisibility(View.GONE);
                        rbReelID.requestFocus();
                    }
                    break;
                case R.id.radio_part_number:
                    if (rbPartNo.isChecked()) {
                        pnLayout.setVisibility(View.VISIBLE);
                        reelIdLayout.setVisibility(View.GONE);
                        qrcodeLayout.setVisibility(View.GONE);
                        rbPartNo.requestFocus();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wo_label_print);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(WoLabelPrintActivity.this);
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

        rgDC = findViewById(R.id.radio_group_dc);
        rbQrCode = findViewById(R.id.radio_dc_qrcode);
        rbReelID = findViewById(R.id.radio_reel_id);
        rbPartNo = findViewById(R.id.radio_part_number);
        rgDC.setOnCheckedChangeListener(dcListener);

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

        qrcodeLayout = findViewById(R.id.qrcodeLayout);
        txtImportQrCodeLine = findViewById(R.id.edittext_qrcode_line);
        txtImportQrCodeLine.setSelectAllOnFocus(true);
        txtImportQrCodeMergeNo = findViewById(R.id.edittext_qrcode_merge_no);
        txtImportQrCodeMergeNo.setSelectAllOnFocus(true);
        txtImportQrCode = findViewById(R.id.edittext_import_dc_qrcode);
        txtImportQrCode.setSelectAllOnFocus(true);
        txtImportQrCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down " + v.getId());
                    EditText et = (EditText) v;

                    if (TextUtils.isEmpty(et.getText())) {
                        return false;
                    } else {
                        txtImportQrCodeRemark.requestFocus();
                        return true;
                    }
                }

                return false;
            }
        });

        txtImportQrCodeRemark = findViewById(R.id.edittext_qrcode_remark);
        txtImportQrCodeRemark.setSelectAllOnFocus(true);
        txtImportQrCodeRemark.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // if enter is pressed start calculating
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    // get EditText text
                    String text = ((EditText) v).getText().toString();
                    String findStr = "\n";
                    int lastIndex = 0;
                    int count = 0;

                    while (lastIndex != -1) {
                        lastIndex = text.indexOf(findStr, lastIndex);

                        if (lastIndex != -1) {
                            count++;
                            lastIndex += findStr.length();
                        }
                    }
                    // find how many rows it cointains
                    //int editTextRowCount = text.split("\\n").length;
                    // user has input more than limited - lets do something
                    // about that
                    if (count >= 2) {
                        // find the last break
                        int lastBreakIndex = text.lastIndexOf(findStr);
                        // compose new text
                        String newText = text.substring(0, lastBreakIndex);
                        // add new text - delete old one and append new one
                        // (append because I want the cursor to be at the end)
                        ((EditText) v).setText("");
                        ((EditText) v).append(newText);
                        hideKeyboard((EditText) v);
                    }
                }

                return false;
            }
        });

        reelIdLayout = findViewById(R.id.reelIdLayout);
        txtImportReelIdLine = findViewById(R.id.edittext_reelid_line);
        txtImportReelIdLine.setSelectAllOnFocus(true);
        txtImportReelIdMergeNo = findViewById(R.id.edittext_reelid_merge_no);
        txtImportReelIdMergeNo.setSelectAllOnFocus(true);
        txtImportReelId = findViewById(R.id.edittext_reelid);
        // 限制長度為34
        txtImportReelId.setFilters(new InputFilter[]{new InputFilter.LengthFilter(34)});
        txtImportReelId.setSelectAllOnFocus(true);
        txtImportReelIdQty = findViewById(R.id.edittext_reelid_qty);
        txtImportReelIdQty.setSelectAllOnFocus(true);
        txtImportReelIdPo = findViewById(R.id.edittext_reelid_po);
        txtImportReelIdPo.setSelectAllOnFocus(true);
        txtImportReelIdCOO = findViewById(R.id.edittext_reelid_coo);
        txtImportReelIdCOO.setSelectAllOnFocus(true);
        txtImportReelIdRemark = findViewById(R.id.edittext_reelid_remark);
        txtImportReelIdRemark.setSelectAllOnFocus(true);
        txtImportReelIdRemark.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // if enter is pressed start calculating
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    // get EditText text
                    String text = ((EditText) v).getText().toString();
                    String findStr = "\n";
                    int lastIndex = 0;
                    int count = 0;

                    while (lastIndex != -1) {
                        lastIndex = text.indexOf(findStr, lastIndex);

                        if (lastIndex != -1) {
                            count++;
                            lastIndex += findStr.length();
                        }
                    }
                    // find how many rows it cointains
                    // int editTextRowCount = text.split("\\n").length;
                    // user has input more than limited - lets do something
                    // about that
                    if (count >= 2) {
                        // find the last break
                        int lastBreakIndex = text.lastIndexOf(findStr);
                        // compose new text
                        String newText = text.substring(0, lastBreakIndex);
                        // add new text - delete old one and append new one
                        // (append because I want the cursor to be at the end)
                        ((EditText) v).setText("");
                        ((EditText) v).append(newText);
                        hideKeyboard((EditText) v);
                    }
                }

                return false;
            }
        });

        pnLayout = findViewById(R.id.pnLayout);

        txtImportLine = findViewById(R.id.edittext_line);
        txtImportLine.setSelectAllOnFocus(true);
        txtImportMergeNo = findViewById(R.id.edittext_merge_no);
        txtImportMergeNo.setSelectAllOnFocus(true);
        txtImportPartNo = findViewById(R.id.edittext_pn);
        // 限制長度為12
        txtImportPartNo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        txtImportPartNo.setSelectAllOnFocus(true);
        txtImportQty = findViewById(R.id.edittext_qty);
        txtImportQty.setSelectAllOnFocus(true);
        txtImportDC = findViewById(R.id.edittext_dc);
        txtImportDC.setSelectAllOnFocus(true);
        txtImportVC = findViewById(R.id.edittext_vendor_code);
        txtImportVC.setSelectAllOnFocus(true);
        txtImportPo = findViewById(R.id.edittext_pn_po);
        txtImportPo.setSelectAllOnFocus(true);
        txtImportCOO = findViewById(R.id.edittext_coo);
        txtImportCOO.setSelectAllOnFocus(true);
        txtImportRemark = findViewById(R.id.edittext_remark);
        txtImportRemark.setSelectAllOnFocus(true);
        txtImportRemark.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // if enter is pressed start calculating
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    // get EditText text
                    String text = ((EditText) v).getText().toString();
                    String findStr = "\n";
                    int lastIndex = 0;
                    int count = 0;

                    while (lastIndex != -1) {
                        lastIndex = text.indexOf(findStr, lastIndex);

                        if (lastIndex != -1) {
                            count++;
                            lastIndex += findStr.length();
                        }
                    }
                    // find how many rows it cointains
                    // int editTextRowCount = text.split("\\n").length;
                    // user has input more than limited - lets do something
                    // about that
                    if (count >= 2) {
                        // find the last break
                        int lastBreakIndex = text.lastIndexOf(findStr);
                        // compose new text
                        String newText = text.substring(0, lastBreakIndex);
                        // add new text - delete old one and append new one
                        // (append because I want the cursor to be at the end)
                        ((EditText) v).setText("");
                        ((EditText) v).append(newText);
                        hideKeyboard((EditText) v);
                    }
                }

                return false;
            }
        });

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

                if (checkFields()) {
                    //if (TextUtils.isEmpty(getPo())) {
                    //showAskDialog();
                    //} else {
                    doQueryPN();
                    //}
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

        rbReelID.setChecked(true);
        rbBluetooth.setChecked(true);
        lblTitle = findViewById(R.id.ap_title);
        setTitle();
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.label_wo_label_print1, AppController.getOrgName()));
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private void showAskDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle(getString(R.string.warn));
        dialog.setMessage(getString(R.string.PO_is_empty_do_you_want_to_continue_printing));
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setPositiveButton(getString(R.string.button_confirm_setting),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int arg1) {
                        dialog.dismiss();
                        doQueryPN();
                    }
                });

        dialog.setNegativeButton(R.string.choose_sku,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int arg1) {
                        dialog.dismiss();
                    }
                });

        dialog.show();
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

    private void hideKeyboard(EditText v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void doQueryPN() {
        pnInfo = new PnItemInfoHelper();
        pnInfo.setPartNo(getPartNo());
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        print = new PrintLabelHandler();
        new GetPNInfo().execute(0);
    }

    private void getReelIdList() {
        mlInfo = new MaterialLabelHelper();
        mlInfo.setPartNo(getPartNo());
        mlInfo.setVendorCode(getVendorCode());
        mlInfo.setDateCode(getDateCode());
        mlInfo.setLabelSize("S");
        mlInfo.setLabelCount(1);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetReelIdList().execute(0);
    }

    private void printLabel() {
        dialog = ProgressDialog.show(WoLabelPrintActivity.this, getString(R.string.holdon), getString(R.string.printingLabel), true);
        errorInfo = "";
        String poWithQty;
        String reelId;
        String po = getPo();
        String qty = getQty();

        if (rbPartNo.isChecked() || (rbReelID.isChecked() && getReelId().length() == 28)) {
            if (TextUtils.isEmpty(po)) {
                poWithQty = "";
            } else {
                poWithQty = po + "," + qty;
            }

            reelId = mlInfo.getReelIds()[0];
        } else if (rbReelID.isChecked()) {
            if (TextUtils.isEmpty(po)) {
                poWithQty = "";
            } else {
                poWithQty = po + "," + qty;
            }

            reelId = getReelId();
        } else {
            poWithQty = getPoWithQtyList();
            reelId = getReelId();
        }

        String qrCode = reelId + "@" + poWithQty + "@" + qty;
        String coo = getCOO(); //20260120 Ann Add

        if(Constant.ISORG){
            if(!TextUtils.isEmpty(orgPrint)){
                if(coo.isEmpty()) { //20260120 Ann Add:coo
                    qrCode = reelId + "@" + poWithQty + "@" + qty + "@" + orgPrint;
                }else{
                    qrCode = reelId + "@" + poWithQty + "@" + qty + "@" + orgPrint + "@@" + coo;
                }
            }else{
                if(coo.isEmpty()) { //20260120 Ann Add:coo
                    qrCode = reelId + "@" + poWithQty + "@" + qty + "@" + AppController.getOrg();
                }else{
                    qrCode = reelId + "@" + poWithQty + "@" + qty + "@" + AppController.getOrg() + "@@" + coo;
                }
            }
        }

        //if (!BtPrintLabel.printWorkOrderLabel(getLine(), getMergeNo(), reelId, getPartNo(), pnInfo.getPartDescription(), qty, getDateCode(), getVendorCode(), po, getRemark(), qrCode, orgPrint)) {
        if (!BtPrintLabel.printWorkOrderLabel(getLine(), getMergeNo(), reelId, getPartNo(), pnInfo.getPartDescription(), qty, getDateCode(), getVendorCode(), po, getRemark(), qrCode, orgPrint, coo)) { //20260120 Ann Add:coo
            errorInfo = getString(R.string.printLabalFailed);
            mConnection.setText(getString(R.string.printer_connect_error));
            mConnection.setTextColor(Color.RED);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        dialog.dismiss();
    }

    private boolean checkFields() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            orgPrint = "";
            txtImportLine.setText(txtImportLine.getText().toString().trim());
            txtImportMergeNo.setText(txtImportMergeNo.getText().toString().trim());
            txtImportPartNo.setText(txtImportPartNo.getText().toString().trim());
            txtImportQty.setText(txtImportQty.getText().toString().trim());
            txtImportDC.setText(txtImportDC.getText().toString().trim());
            txtImportVC.setText(txtImportVC.getText().toString().trim());
            txtImportPo.setText(txtImportPo.getText().toString().trim());
            txtImportRemark.setText(txtImportRemark.getText().toString().trim());

            if (TextUtils.isEmpty(txtImportLine.getText())) {
                txtImportLine.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_line), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportPartNo.getText())) {
                txtImportPartNo.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_sku), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (txtImportPartNo.getText().length() != 12) {
                txtImportPartNo.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.sku_len_must_be_12), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportQty.getText())) {
                txtImportQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.input_printer_qty), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (Integer.parseInt(txtImportQty.getText().toString()) < 1) {
                txtImportQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.num_has_greater_0), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportDC.getText()) && TextUtils.isEmpty(txtImportVC.getText())) {
                //do nothing
            } else {
                if (TextUtils.isEmpty(txtImportDC.getText())) {
                    txtImportDC.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_dc), Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (txtImportDC.getText().length() != 10) {
                    txtImportDC.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.dc_len_greater_10), Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (!Util.isDateCodeValid(getDateCode())) {
                    txtImportDC.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.dc_incorrect), Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (TextUtils.isEmpty(txtImportVC.getText())) {
                    txtImportVC.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_vc), Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (txtImportVC.getText().length() != 6) {
                    txtImportVC.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.vc_len_must_be_six), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }

            /*if (TextUtils.isEmpty(txtImportPo.getText())) {
                txtImportPo.requestFocus();
                Toast.makeText(getApplicationContext(), "請輸入PO", Toast.LENGTH_SHORT).show();
                return false;
            }*/
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            orgPrint = "";
            txtImportReelIdLine.setText(txtImportReelIdLine.getText().toString().trim());
            txtImportReelIdMergeNo.setText(txtImportReelIdMergeNo.getText().toString().trim());
            txtImportReelId.setText(txtImportReelId.getText().toString().trim());
            txtImportReelIdQty.setText(txtImportReelIdQty.getText().toString().trim());
            txtImportReelIdPo.setText(txtImportReelIdPo.getText().toString().trim());
            txtImportReelIdRemark.setText(txtImportReelIdRemark.getText().toString().trim());

            if (TextUtils.isEmpty(txtImportReelIdLine.getText())) {
                txtImportReelIdLine.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_line), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportReelId.getText())) {
                txtImportReelId.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_reelid), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (txtImportReelId.getText().length() != 28 && txtImportReelId.getText().length() != 34) {
                txtImportReelId.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.reelid_len_must_be_28_24), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (!Util.isDateCodeValid(getDateCode())) {
                txtImportReelId.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.dc_incorrect), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportReelIdQty.getText())) {
                txtImportReelIdQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.input_printer_qty), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (Integer.parseInt(txtImportReelIdQty.getText().toString()) < 1) {
                txtImportReelIdQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.num_has_greater_0), Toast.LENGTH_SHORT).show();
                return false;
            }

            /*if (TextUtils.isEmpty(txtImportReelIdPo.getText())) {
                txtImportReelIdPo.requestFocus();
                Toast.makeText(getApplicationContext(), "請輸入PO", Toast.LENGTH_SHORT).show();
                return false;
            }*/
        } else {
            txtImportQrCodeLine.setText(txtImportQrCodeLine.getText().toString().trim());
            txtImportQrCodeMergeNo.setText(txtImportQrCodeMergeNo.getText().toString().trim());
            txtImportQrCode.setText(txtImportQrCode.getText().toString().trim());
            txtImportQrCodeRemark.setText(txtImportQrCodeRemark.getText().toString().trim());

            if (TextUtils.isEmpty(txtImportQrCodeLine.getText())) {
                txtImportQrCodeLine.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_line), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportQrCode.getText())) {
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_qrcode), Toast.LENGTH_SHORT).show();
                return false;
            }

            String reelId = QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID);
            if (reelId.length() != 34) {
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.reelid_incorrect_len_must_be_34), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (!Util.isDateCodeValid(getDateCode())) {
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.dc_incorrect), Toast.LENGTH_SHORT).show();
                return false;
            }

            String qty = QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.QTY);
            if (TextUtils.isEmpty(qty)) {
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.qr_qty_is_not_null), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (Integer.parseInt(qty) < 1) {
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.num_has_greater_0), Toast.LENGTH_SHORT).show();
                return false;
            }
            orgPrint = QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.ORG);
            try{
                int num=Integer.parseInt(orgPrint);
            }catch (NumberFormatException e){
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.org_must_input_num), Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    public String getReelId() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportPartNo.getText().toString().trim() + txtImportVC.getText().toString().trim() + txtImportDC.getText().toString().trim();
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            return txtImportReelId.getText().toString().trim();
        } else {
            return QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID);
        }
    }

    public String getPartNo() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportPartNo.getText().toString().trim();
        } else {
            return getReelId().substring(0, 12);
        }
    }

    public String getDateCode() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportDC.getText().toString().trim();
        } else {
            return getReelId().substring(18, 28);
        }
    }

    public String getQty() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportQty.getText().toString().trim();
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            return txtImportReelIdQty.getText().toString().trim();
        } else {
            return QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.QTY);
        }
    }

    //20260120 Ann Add
    public String getCOO(){
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportCOO.getText().toString().trim();
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            return txtImportReelIdCOO.getText().toString().trim();
        } else {
            return QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.COO);
        }
    }

    public String getLine() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportLine.getText().toString().trim();
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            return txtImportReelIdLine.getText().toString().trim();
        } else {
            return txtImportQrCodeLine.getText().toString().trim();
        }
    }

    public String getMergeNo() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportMergeNo.getText().toString().trim();
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            return txtImportReelIdMergeNo.getText().toString().trim();
        } else {
            return txtImportQrCodeMergeNo.getText().toString().trim();
        }
    }

    public String getRemark() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportRemark.getText().toString().trim();
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            return txtImportReelIdRemark.getText().toString().trim();
        } else {
            return txtImportQrCodeRemark.getText().toString().trim();
        }
    }

    public String getPoWithQtyList() {
        if (qrcodeLayout.getVisibility() == View.VISIBLE) {
            return QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.PO_QTY_LIST);
        } else {
            return "";
        }
    }

    public String getPo() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportPo.getText().toString().trim();
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            return txtImportReelIdPo.getText().toString().trim();
        } else {
            return QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.PO_QTY_LIST).split(",")[0];
        }
    }

    public String getVendorCode() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportVC.getText().toString().trim();
        } else {
            return getReelId().substring(12, 18);
        }
    }

    private void cleanData() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            txtImportLine.setText("");
            txtImportMergeNo.setText("");
            txtImportPartNo.setText("");
            txtImportQty.setText("");
            txtImportDC.setText("");
            txtImportVC.setText("");
            txtImportPo.setText("");
            txtImportCOO.setText(""); //20260120 Ann Add
            txtImportRemark.setText("");
            txtImportLine.requestFocus();
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            txtImportReelIdLine.setText("");
            txtImportReelIdMergeNo.setText("");
            txtImportReelId.setText("");
            txtImportReelIdQty.setText("");
            txtImportReelIdPo.setText("");
            txtImportReelIdCOO.setText(""); //20260120 Ann Add
            txtImportReelIdRemark.setText("");
            txtImportReelIdLine.requestFocus();
        } else {
            txtImportQrCodeLine.setText("");
            txtImportQrCodeMergeNo.setText("");
            txtImportQrCode.setText("");
            txtImportQrCodeRemark.setText("");
            txtImportQrCodeLine.requestFocus();
        }
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

    private boolean savePrinterInfo(String ip, String port, String qty) {
        try {
            Preferences.setString(this, Preferences.WO_LABEL_PRINTER_IP, ip);
            Preferences.setString(this, Preferences.WO_LABEL_PRINTER_PORT, port);
            Preferences.setString(this, Preferences.WO_LABEL_PRINTER_QTY, qty);
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
            printClose();
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

    //20260120 Ann Add:COO
    private boolean printLabel(String lineNo, String mergeNo, String reelId, String partNo, String description, String qty,
                               String dateCode, String vendorCode, String po, String remark, String qrCode, String coo) {
        try {
            TscEthernetDll.clearbuffer();
            TscEthernetDll.sendcommand("SET TEAR ON\n");
            TscEthernetDll.sendcommand("CODEPAGE UTF-8\n");

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
            TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "Line: " + lineNo);
            x = mm2dot(36);
            y = mm2dot(1);
            if (TextUtils.isEmpty(mergeNo))
                mergeNo = "";
            TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "Merge No: " + mergeNo);
            if (!TextUtils.isEmpty(reelId)) {
                x = mm2dot(1);
                y = mm2dot(7);
                TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "Reel ID:");
                x = mm2dot(9);
                y = mm2dot(4);
                TscEthernetDll.barcode(x, y, "128", 30, 1, 0, 2, 2, reelId);
            }
            x = mm2dot(1);
            y = mm2dot(12);
            TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "P/N:");
            x = mm2dot(8);
            y = mm2dot(10);
            TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 5, partNo);
            x = mm2dot(9);
            y = mm2dot(13);
            TscEthernetDll.printerfont(x, y, "0", 0, 12, 12, partNo);
            x = mm2dot(1);
            y = mm2dot(17);
            if (des == null) {
                TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, description);
            } else {
                TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
                TscEthernetDll.sendcommand(des);
                TscEthernetDll.sendcommand("\"\n");
            }
            y = mm2dot(22);
            TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "Quantity:");
            x = mm2dot(13);
            y = mm2dot(20);
            TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 3, qty);
            x = mm2dot(13);
            y = mm2dot(23);
            TscEthernetDll.printerfont(x, y, "0", 0, 12, 12, qty);
            x = mm2dot(1);
            y = mm2dot(29);
            TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "Date Code:");
            if (!TextUtils.isEmpty(dateCode)) {
                x = mm2dot(13);
                y = mm2dot(27);
                TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 3, dateCode);
                x = mm2dot(13);
                y = mm2dot(30);
                TscEthernetDll.printerfont(x, y, "0", 0, 10, 10, dateCode);
            }
            x = mm2dot(1);
            y = mm2dot(35);
            TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "Vendor Code:");
            if (!TextUtils.isEmpty(vendorCode)) {
                x = mm2dot(15);
                y = mm2dot(33);
                TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 2, vendorCode);
                x = mm2dot(15);
                y = mm2dot(36);
                TscEthernetDll.printerfont(x, y, "0", 0, 9, 9, vendorCode);
            }
            if (!TextUtils.isEmpty(po) && !po.toUpperCase().equals("NULL")) {
                x = mm2dot(1);
                y = mm2dot(41);
                TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "PO NO:");
                x = mm2dot(9);
                y = mm2dot(39);
                TscEthernetDll.barcode(x, y, "128", 30, 1, 0, 2, 2, po);
            }
            x = mm2dot(3);
            y = mm2dot(45);
            if (rem == null) {
                TscEthernetDll.sendcommand("BLOCK " + x + "," + y + ",512,42,\"0\",0,7,7,\"" + remark + "\"\n");
            } else {
                TscEthernetDll.sendcommand("BLOCK " + x + "," + y + ",512,42,\"FONT001\",0,2,2,\"");
                TscEthernetDll.sendcommand(rem);
                TscEthernetDll.sendcommand("\"\n");
            }
            //x = mm2dot(40);
            //y = mm2dot(21);

            if(Constant.ISORG){
                /*x = mm2dot(50);
                y = mm2dot(7);

                if(!TextUtils.isEmpty(orgPrint)){
                    TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,2,2,\"" + orgPrint + "\"\n");
                }else{
                    TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,2,2,\"" + AppController.getOrgName() + "\"\n");
                }
                TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,3,3,\"" + st1 + "\"\n");*/

                x = mm2dot(43);
                y = mm2dot(11);

                if(!TextUtils.isEmpty(orgPrint)){
                    TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"4\",0,2,2,\"" + AppController.getOrgName(Integer.parseInt(orgPrint)) + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(11);
                    TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 2, orgPrint);
                }else{
                    TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"4\",0,2,2,\"" + AppController.getOrgName() + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(11);
                    TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 2, AppController.getOrg()+"");
                }
            }

            //COO 20260120 Ann Add
            if(!coo.isEmpty()){
                x = mm2dot(40);
                y = mm2dot(20);
                TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "COO:");
                x = mm2dot(45);
                y = mm2dot(19);
                TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 2, coo);
                x = mm2dot(45);
                y = mm2dot(23);
                TscEthernetDll.printerfont(x, y, "0", 0, 12, 12, coo);
            }

            x = mm2dot(45);
            y = mm2dot(27); //20260120 Ann Edit:QRCode從23下移至27
            TscEthernetDll.qrcode(x, y, "M", "4", "A", "0", "M2", "S7", qrCode);
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
                Preferences.WO_LABEL_PRINTER_IP, AppController.getProperties("WoLabelPrinterIP"));
        etIP.setText(ip);
        final EditText etPort = item.findViewById(R.id.editTextPort);
        String port = Preferences.getSharedPreferences(this).getString(
                Preferences.WO_LABEL_PRINTER_PORT, AppController.getProperties("WoLabelPrinterPort"));
        etPort.setText(port);
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.WO_LABEL_PRINTER_QTY, AppController.getProperties("WoLabelPrinterQty"));
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
                            dialog = ProgressDialog.show(WoLabelPrintActivity.this, getString(R.string.holdon), getString(R.string.printingLabel), true);

                            if (!printOpen(etIP.getText().toString().trim(), etPort.getText().toString().trim())) {
                                Toast.makeText(getApplicationContext(), getString(R.string.cant_conect_printer), Toast.LENGTH_LONG).show();
                                view.setEnabled(true);
                                dialog.dismiss();
                                return;
                            }

                            int printTimes = Integer.parseInt(etQty.getText().toString());
                            for (int j = 0; j < printTimes; j++) {
                                String poWithQty;
                                String reelId;
                                String po = getPo();
                                String qty = getQty();

                                if (rbPartNo.isChecked() || (rbReelID.isChecked() && getReelId().length() == 28)) {
                                    if (TextUtils.isEmpty(po)) {
                                        poWithQty = "";
                                    } else {
                                        poWithQty = po + "," + qty;
                                    }

                                    reelId = mlInfo.getReelIds()[0];
                                } else if (rbReelID.isChecked()) {
                                    if (TextUtils.isEmpty(po)) {
                                        poWithQty = "";
                                    } else {
                                        poWithQty = po + "," + qty;
                                    }

                                    reelId = getReelId();
                                } else {
                                    poWithQty = getPoWithQtyList();
                                    reelId = getReelId();
                                }

                                String qrCode = reelId + "@" + poWithQty + "@" + qty;
                                String coo = getCOO(); //20260120 Ann Add

                                if(Constant.ISORG){
                                    if(!TextUtils.isEmpty(orgPrint)){
                                        if(coo.isEmpty()) { //20260120 Ann Add:coo
                                            qrCode = reelId + "@" + poWithQty + "@" + qty + "@" + orgPrint;
                                        }else{
                                            qrCode = reelId + "@" + poWithQty + "@" + qty + "@" + orgPrint + "@@" + coo;
                                        }
                                    }else{
                                        if(coo.isEmpty()) { //20260120 Ann Add:coo
                                            qrCode = reelId + "@" + poWithQty + "@" + qty + "@" + AppController.getOrg();
                                        }else{
                                            qrCode = reelId + "@" + poWithQty + "@" + qty + "@" + AppController.getOrg() + "@@" + coo;
                                        }
                                    }
                                }

                                if (!printLabel(getLine(), getMergeNo(), reelId, getPartNo(), pnInfo.getPartDescription(), qty, getDateCode(), getVendorCode(), po, getRemark(), qrCode, coo)) {
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

    private class GetPNInfo extends AsyncTask<Integer, String, PnItemInfoHelper> {
        @Override
        protected PnItemInfoHelper doInBackground(Integer... params) {
            AppController.debug("Get PNInfo from " + AppController.getServerInfo()
                    + AppController.getProperties("GetPNInfo"));
            publishProgress(getString(R.string.downloading_data));
            return print.getPNInfo(pnInfo);
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
        protected void onPostExecute(PnItemInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                pnInfo = result;
                if (pnInfo.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    //orgPrint = QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.ORG);

                    if (rbPartNo.isChecked() || (rbReelID.isChecked() && txtImportReelId.length() == 28)) {
                        getReelIdList();
                    } else {
                        if (rbWifi.isChecked()) {
                            onCreateChoosePrinterDialog().show();
                        } else {
                            printLabel();
                        }
                    }
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = pnInfo.getStrErrorBuf();
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

    private class GetReelIdList extends AsyncTask<Integer, String, MaterialLabelHelper> {
        @Override
        protected MaterialLabelHelper doInBackground(Integer... params) {
            AppController.debug("Get Reel ID List from " + AppController.getServerInfo()
                    + AppController.getProperties("GetReelIdList"));
            publishProgress(getString(R.string.downloading_data));
            return print.getReelIdList(mlInfo);
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
        protected void onPostExecute(MaterialLabelHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                mlInfo = result;
                if (mlInfo.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";

                    if (rbWifi.isChecked()) {
                        onCreateChoosePrinterDialog().show();
                    } else {
                        printLabel();
                    }
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = mlInfo.getStrErrorBuf();
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
