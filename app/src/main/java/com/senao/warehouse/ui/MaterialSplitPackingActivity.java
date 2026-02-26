package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
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
import com.senao.warehouse.print.BtPrintLabel;
import com.senao.warehouse.database.ITEM_LABEL_QR_CODE_FORMAT;
import com.senao.warehouse.database.MaterialLabelHelper;
import com.senao.warehouse.database.PnItemInfoHelper;
import com.senao.warehouse.handler.PrintLabelHandler;
import com.senao.warehouse.util.CharUtil;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.Preferences;
import com.senao.warehouse.util.QrCodeUtil;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.util.Util;

public class MaterialSplitPackingActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PRINTER_SETTING = 2;
    private static final int REQUEST_BLUETOOTH_SETTINGS = 3;
    private static final String TAG = MaterialSplitPackingActivity.class.getSimpleName();
    private TextView mConnection;
    private EditText txtImportPartNo, txtImportDC, txtImportVC, txtImportOriginalQty, txtImportSplitQty,
            txtImportPo, txtImportQrCode, txtImportReelId, txtImportReelIdOriginalQty, txtImportReelIdSplitQty,
            txtImportReelIdPo, txtImportQrCodeSplitQty;
    private EditText txtImportCOO, txtImportReelIdCOO; //20260116 Ann Add
    private Button btnReturn, btnConfim, btnCancel;
    private ProgressDialog dialog;
    private String errorInfo = "",orgPrint="";
    private PrintLabelHandler print;
    private PnItemInfoHelper pnInfo;
    private MaterialLabelHelper mlInfo;
    private MaterialLabelHelper subMlInfo;
    private TscWifiActivity TscEthernetDll;
    private RadioGroup rgDC, rgPrinter;
    private RadioButton rbQrCode, rbReelID, rbPartNo, rbBluetooth, rbWifi;
    private LinearLayout qrcodeLayout, pnLayout, reelIdLayout;
    private TextView lblTitle;

    private RadioGroup.OnCheckedChangeListener dcListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio_dc_qrcode:
                    if (rbQrCode.isChecked()) {
                        pnLayout.setVisibility(View.GONE);
                        reelIdLayout.setVisibility(View.GONE);
                        qrcodeLayout.setVisibility(View.VISIBLE);
                        txtImportQrCode.requestFocus();
                    }
                    break;
                case R.id.radio_reel_id:
                    if (rbReelID.isChecked()) {
                        pnLayout.setVisibility(View.GONE);
                        reelIdLayout.setVisibility(View.VISIBLE);
                        qrcodeLayout.setVisibility(View.GONE);
                        txtImportReelId.requestFocus();
                    }
                    break;
                case R.id.radio_part_number:
                    if (rbPartNo.isChecked()) {
                        pnLayout.setVisibility(View.VISIBLE);
                        reelIdLayout.setVisibility(View.GONE);
                        qrcodeLayout.setVisibility(View.GONE);
                        txtImportPartNo.requestFocus();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_split_packing);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MaterialSplitPackingActivity.this);
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
        final SpannableString text;
        String subtitle = getString(R.string.label_material_split_packing_eng);
        String title = getString(R.string.label_material_split_packing1, AppController.getOrgName(),subtitle);
        text = new SpannableString(title);
        text.setSpan(new RelativeSizeSpan(1.0f), 0, title.length() - subtitle.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new RelativeSizeSpan(0.7f), title.length() - subtitle.length(), title.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);

        rgDC = findViewById(R.id.radio_group_dc);
        rbQrCode = findViewById(R.id.radio_dc_qrcode);
        rbReelID = findViewById(R.id.radio_reel_id);
        rbPartNo = findViewById(R.id.radio_part_number);
        rgDC.setOnCheckedChangeListener(dcListener);

        rgPrinter = findViewById(R.id.radio_printer);
        rbBluetooth = findViewById(R.id.radio_bluetooth);
        rbWifi = findViewById(R.id.radio_wifi);

        qrcodeLayout = findViewById(R.id.qrcodeLayout);

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
                        txtImportQrCodeSplitQty.requestFocus();
                        return true;
                    }
                }

                return false;
            }
        });

        txtImportQrCodeSplitQty = findViewById(R.id.edittext_qrcode_split_qty);
        txtImportQrCodeSplitQty.setSelectAllOnFocus(true);

        reelIdLayout = findViewById(R.id.reelIdLayout);
        txtImportReelId = findViewById(R.id.edittext_reelid);
        txtImportReelId.setFilters(new InputFilter[]{new InputFilter.LengthFilter(34)});
        txtImportReelId.setSelectAllOnFocus(true);
        txtImportReelIdOriginalQty = findViewById(R.id.edittext_reelid_original_qty);
        txtImportReelIdOriginalQty.setSelectAllOnFocus(true);
        txtImportReelIdPo = findViewById(R.id.edittext_reelid_po);
        txtImportReelIdPo.setSelectAllOnFocus(true);
        txtImportReelIdSplitQty = findViewById(R.id.edittext_reelid_split_qty);
        txtImportReelIdSplitQty.setSelectAllOnFocus(true);
        txtImportReelIdCOO = findViewById(R.id.edittext_reelid_coo);
        txtImportReelIdCOO.setSelectAllOnFocus(true);

        pnLayout = findViewById(R.id.pnLayout);
        txtImportPartNo = findViewById(R.id.edittext_pn);
        // 限制長度為12
        txtImportPartNo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        txtImportPartNo.setSelectAllOnFocus(true);
        txtImportDC = findViewById(R.id.edittext_dc);
        txtImportDC.setSelectAllOnFocus(true);
        txtImportVC = findViewById(R.id.edittext_vendor_code);
        txtImportVC.setSelectAllOnFocus(true);
        txtImportOriginalQty = findViewById(R.id.edittext_original_qty);
        txtImportOriginalQty.setSelectAllOnFocus(true);
        txtImportSplitQty = findViewById(R.id.edittext_split_qty);
        txtImportSplitQty.setSelectAllOnFocus(true);
        txtImportPo = findViewById(R.id.edittext_pn_po);
        txtImportPo.setSelectAllOnFocus(true);
        txtImportCOO = findViewById(R.id.edittext_coo);
        txtImportCOO.setSelectAllOnFocus(true);

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
                if (checkFields())
                    doQueryPN();
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

        rbBluetooth.setChecked(true);
        rbReelID.setChecked(true);
        print = new PrintLabelHandler();
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

    private void doQueryPN() {
        pnInfo = new PnItemInfoHelper();
        pnInfo.setPartNo(getPartNo());
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetPNInfo().execute(0);
    }

    private boolean checkFields() {
        if (rgPrinter.getCheckedRadioButtonId() == -1) {
            rbBluetooth.requestFocus();
            Toast.makeText(getApplicationContext(), getString(R.string.choose_printer), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (pnLayout.getVisibility() == View.VISIBLE) {
            orgPrint = "";
            txtImportPartNo.setText(txtImportPartNo.getText().toString().trim());
            txtImportDC.setText(txtImportDC.getText().toString().trim());
            txtImportVC.setText(txtImportVC.getText().toString().trim());
            txtImportOriginalQty.setText(txtImportOriginalQty.getText().toString().trim());
            txtImportSplitQty.setText(txtImportSplitQty.getText().toString().trim());
            txtImportPo.setText(txtImportPo.getText().toString().trim());

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

            if (TextUtils.isEmpty(txtImportOriginalQty.getText())) {
                txtImportOriginalQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_origin_qty), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (Integer.parseInt(txtImportOriginalQty.getText().toString()) < 1) {
                txtImportOriginalQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.original_qty_must_more_than_0), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportSplitQty.getText())) {
                txtImportSplitQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_split_qty), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (Integer.parseInt(txtImportSplitQty.getText().toString()) < 1) {
                txtImportSplitQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.split_qty_more_than_0), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (Integer.parseInt(txtImportSplitQty.getText().toString()) > Integer.parseInt(txtImportOriginalQty.getText().toString())) {
                txtImportSplitQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString( R.string.spilt_qty_cant_more_than_original_qty), Toast.LENGTH_SHORT).show();
                return false;
            }

            /*if (TextUtils.isEmpty(txtImportPo.getText())) {
                txtImportPo.requestFocus();
                Toast.makeText(getApplicationContext(), "請輸入PO", Toast.LENGTH_SHORT).show();
                return false;
            }*/
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            orgPrint = "";
            txtImportReelId.setText(txtImportReelId.getText().toString().trim());
            txtImportReelIdOriginalQty.setText(txtImportReelIdOriginalQty.getText().toString().trim());
            txtImportReelIdSplitQty.setText(txtImportReelIdSplitQty.getText().toString().trim());
            txtImportReelIdPo.setText(txtImportReelIdPo.getText().toString().trim());

            if (txtImportReelId.getText().length() != 28
                    && txtImportReelId.getText().length() != 34) {
                txtImportReelId.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.reelid_len_must_be_28_24), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (!Util.isDateCodeValid(getDateCode())) {
                txtImportReelId.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.dc_incorrect), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportReelIdOriginalQty.getText())) {
                txtImportReelIdOriginalQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_origin_qty), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (Integer.parseInt(txtImportReelIdOriginalQty.getText().toString()) < 1) {
                txtImportReelIdOriginalQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.original_qty_must_more_than_0), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportReelIdSplitQty.getText())) {
                txtImportReelIdSplitQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_split_qty), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (Integer.parseInt(txtImportReelIdSplitQty.getText().toString()) < 1) {
                txtImportReelIdSplitQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.split_qty_more_than_0), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (Integer.parseInt(txtImportReelIdSplitQty.getText().toString()) > Integer.parseInt(txtImportReelIdOriginalQty.getText().toString())) {
                txtImportReelIdSplitQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString( R.string.spilt_qty_cant_more_than_original_qty), Toast.LENGTH_SHORT).show();
                return false;
            }

            /*if (TextUtils.isEmpty(txtImportReelIdPo.getText())) {
                txtImportReelIdPo.requestFocus();
                Toast.makeText(getApplicationContext(), "請輸入PO", Toast.LENGTH_SHORT).show();
                return false;
            }*/
        } else {
            txtImportQrCode.setText(txtImportQrCode.getText().toString().trim());
            txtImportQrCodeSplitQty.setText(txtImportQrCodeSplitQty.getText().toString().trim());
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

            if (TextUtils.isEmpty(txtImportQrCodeSplitQty.getText())) {
                txtImportQrCodeSplitQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_split_qty), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (Integer.parseInt(txtImportQrCodeSplitQty.getText().toString()) < 1) {
                txtImportQrCodeSplitQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.split_qty_more_than_0), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (Integer.parseInt(txtImportQrCodeSplitQty.getText().toString()) > Integer.parseInt(qty)) {
                txtImportQrCodeSplitQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString( R.string.spilt_qty_cant_more_than_original_qty), Toast.LENGTH_SHORT).show();
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

    private void cleanData() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            txtImportPartNo.setText("");
            txtImportDC.setText("");
            txtImportVC.setText("");
            txtImportOriginalQty.setText("");
            txtImportSplitQty.setText("");
            txtImportPo.setText("");
            txtImportCOO.setText(""); //20260116 Ann Add
            txtImportPartNo.requestFocus();
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            txtImportReelId.setText("");
            txtImportReelIdOriginalQty.setText("");
            txtImportReelIdSplitQty.setText("");
            txtImportReelIdPo.setText("");
            txtImportReelIdCOO.setText(""); //20260116 Ann Add
            txtImportReelId.requestFocus();
        } else {
            txtImportQrCode.setText("");
            txtImportQrCodeSplitQty.setText("");
            txtImportQrCode.requestFocus();
        }
    }

    private Dialog onCreateChoosePrinterDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer, null);
        final EditText etIP = item.findViewById(R.id.editTextIP);
        String ip = Preferences.getSharedPreferences(this).getString(
                Preferences.SPLIT_MATERIAL_PRINTER_IP, AppController.getProperties("SplitMaterialPrinterIP"));
        etIP.setText(ip);
        final EditText etPort = item.findViewById(R.id.editTextPort);
        String port = Preferences.getSharedPreferences(this).getString(
                Preferences.SPLIT_MATERIAL_PRINTER_PORT, AppController.getProperties("SplitMaterialPort"));
        etPort.setText(port);
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SPLIT_MATERIAL_PRINTER_QTY, AppController.getProperties("SplitMaterialPrinterQty"));
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
                            dialog = ProgressDialog.show(MaterialSplitPackingActivity.this,
                                    getString(R.string.holdon), getString(R.string.printingLabel), true);

                            if (!printOpen(etIP.getText().toString().trim(), etPort.getText().toString().trim())) {
                                Toast.makeText(getApplicationContext(), getString(R.string.cant_conect_printer), Toast.LENGTH_LONG).show();
                                view.setEnabled(true);
                                dialog.dismiss();
                                return;
                            }

                            int printTimes = Integer.parseInt(etQty.getText().toString());
                            for (int j = 0; j < printTimes; j++) {
                                int originalQty = Integer.parseInt(getQty());
                                int splitQty = Integer.parseInt(getSplitQty());
                                int newQty = originalQty - splitQty;
                                String reelId;
                                String po = getPo();
                                String poWithQty;
                                String poWithSplitQty;

                                if (rbPartNo.isChecked() || (rbReelID.isChecked() && getReelId().length() == 28)) {
                                    reelId = mlInfo.getReelIds()[0];

                                    if (TextUtils.isEmpty(po)) {
                                        poWithQty = "";
                                        poWithSplitQty = "";
                                    } else {
                                        poWithQty = po + "," + newQty;
                                        poWithSplitQty = po + "," + splitQty;
                                    }
                                } else if (rbReelID.isChecked()) {
                                    reelId = getReelId();

                                    if (TextUtils.isEmpty(po)) {
                                        poWithQty = "";
                                        poWithSplitQty = "";
                                    } else {
                                        poWithQty = po + "," + newQty;
                                        poWithSplitQty = po + "," + splitQty;
                                    }
                                } else {
                                    reelId = getReelId();
                                    poWithQty = getPoWithQtyList();
                                    poWithSplitQty = getPoWithQtyList();
                                }

                                String qrCode = reelId + "@" + poWithQty + "@" + newQty;
                                String coo = getCOO(); //20260116 Ann Add

                                if(Constant.ISORG){
                                    if(!TextUtils.isEmpty(orgPrint)){
                                        if(coo.isEmpty()){ //20260116 Ann Add:coo
                                            qrCode = reelId + "@" + poWithQty + "@" + newQty + "@" + orgPrint;
                                        }else{
                                            qrCode = reelId + "@" + poWithQty + "@" + newQty + "@" + orgPrint + "@@" + coo;
                                        }
                                    }else{
                                        if(coo.isEmpty()){ //20260116 Ann Add:coo
                                            qrCode = reelId + "@" + poWithQty + "@" + newQty + "@" + AppController.getOrg();
                                        }else{
                                            qrCode = reelId + "@" + poWithQty + "@" + newQty + "@" + AppController.getOrg() + "@@" + coo;
                                        }
                                    }
                                }

                                if (!printLabel(reelId, getPartNo(), pnInfo.getPartDescription(), String.valueOf(newQty), getDateCode(), getVendorCode(), po, qrCode, coo)) {
                                    printClose();
                                    Toast.makeText(getApplicationContext(), getString(R.string.reprinting_due_to_issue_with_new_label), Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                    return;
                                }

                                qrCode = subMlInfo.getReelIds()[0] + "@" + poWithSplitQty + "@" + splitQty;

                                if(Constant.ISORG){
                                    if(!TextUtils.isEmpty(orgPrint)){
                                        if(coo.isEmpty()){ //20260116 Ann Add:coo
                                            qrCode = subMlInfo.getReelIds()[0] + "@" + poWithSplitQty + "@" + splitQty + "@" + orgPrint;
                                        }else{
                                            qrCode = subMlInfo.getReelIds()[0] + "@" + poWithSplitQty + "@" + splitQty + "@" + orgPrint + "@@" + coo;
                                        }
                                    }else{
                                        if(coo.isEmpty()){ //20260116 Ann Add:coo
                                            qrCode = subMlInfo.getReelIds()[0] + "@" + poWithSplitQty + "@" + splitQty + "@" + AppController.getOrg();
                                        }else{
                                            qrCode = subMlInfo.getReelIds()[0] + "@" + poWithSplitQty + "@" + splitQty + "@" + AppController.getOrg() + "@@" + coo;
                                        }
                                    }
                                }

                                if (!printLabel(subMlInfo.getReelIds()[0], getPartNo(), pnInfo.getPartDescription(), String.valueOf(splitQty), getDateCode(), getVendorCode(), po, qrCode, coo)) {
                                    printClose();
                                    Toast.makeText(getApplicationContext(), getString(R.string.reprinting_due_to_issue_with_sub_package_label), Toast.LENGTH_LONG).show();
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

    private void printLabel() {
        dialog = ProgressDialog.show(MaterialSplitPackingActivity.this,
                getString(R.string.holdon), getString(R.string.printingLabel), true);
        int originalQty = Integer.parseInt(getQty());
        int splitQty = Integer.parseInt(getSplitQty());
        int newQty = originalQty - splitQty;
        String reelId;
        String po = getPo();
        String poWithQty;
        String poWithSplitQty;

        if (rbPartNo.isChecked() || (rbReelID.isChecked() && getReelId().length() == 28)) {
            reelId = mlInfo.getReelIds()[0];

            if (TextUtils.isEmpty(po)) {
                poWithQty = "";
                poWithSplitQty = "";
            } else {
                poWithQty = po + "," + newQty;
                poWithSplitQty = po + "," + splitQty;
            }
        } else if (rbReelID.isChecked()) {
            reelId = getReelId();

            if (TextUtils.isEmpty(po)) {
                poWithQty = "";
                poWithSplitQty = "";
            } else {
                poWithQty = po + "," + newQty;
                poWithSplitQty = po + "," + splitQty;
            }
        } else {
            reelId = getReelId();
            poWithQty = getPoWithQtyList();
            poWithSplitQty = getPoWithQtyList();
        }

        String qrCode = reelId + "@" + poWithQty + "@" + newQty;
        String coo = getCOO(); //20260116 Ann Add

        if(Constant.ISORG){
            if(!TextUtils.isEmpty(orgPrint)){
                if(coo.isEmpty()){ //20260116 Ann Add:coo
                    qrCode = reelId + "@" + poWithQty + "@" + newQty + "@" + orgPrint;
                }else{
                    qrCode = reelId + "@" + poWithQty + "@" + newQty + "@" + orgPrint + "@@" + coo;
                }
            }else{
                if(coo.isEmpty()) { //20260116 Ann Add:coo
                    qrCode = reelId + "@" + poWithQty + "@" + newQty + "@" + AppController.getOrg();
                }else{
                    qrCode = reelId + "@" + poWithQty + "@" + newQty + "@" + AppController.getOrg() + "@@" + coo;
                }
            }
        }

        //if (!BtPrintLabel.printMaterialLabel3(reelId, getPartNo(), pnInfo.getPartDescription(), String.valueOf(newQty), getDateCode(), getVendorCode(), po, qrCode, orgPrint)) {
        if (!BtPrintLabel.printMaterialLabel3(reelId, getPartNo(), pnInfo.getPartDescription(), String.valueOf(newQty), getDateCode(), getVendorCode(), po, qrCode, orgPrint, coo)) { //20260116 Ann Add:coo
            Toast.makeText(getApplicationContext(), getString(R.string.reprinting_due_to_issue_with_sub_package_label), Toast.LENGTH_LONG).show();
            dialog.dismiss();
            return;
        }

        qrCode = subMlInfo.getReelIds()[0] + "@" + poWithSplitQty + "@" + splitQty;

        if(Constant.ISORG){
            if(!TextUtils.isEmpty(orgPrint)){
                if(coo.isEmpty()) { //20260116 Ann Add:coo
                    qrCode = subMlInfo.getReelIds()[0] + "@" + poWithSplitQty + "@" + splitQty + "@" + orgPrint;
                }else{
                    qrCode = subMlInfo.getReelIds()[0] + "@" + poWithSplitQty + "@" + splitQty + "@" + orgPrint + "@@" + coo;
                }
            }else{
                if(coo.isEmpty()) { //20260116 Ann Add:coo
                    qrCode = subMlInfo.getReelIds()[0] + "@" + poWithSplitQty + "@" + splitQty + "@" + AppController.getOrg();
                }else{
                    qrCode = subMlInfo.getReelIds()[0] + "@" + poWithSplitQty + "@" + splitQty + "@" + AppController.getOrg() + "@@" + coo;
                }
            }
        }

        //if (!BtPrintLabel.printMaterialLabel3(subMlInfo.getReelIds()[0], getPartNo(), pnInfo.getPartDescription(), String.valueOf(splitQty), getDateCode(), getVendorCode(), po, qrCode, orgPrint)) {
        if (!BtPrintLabel.printMaterialLabel3(subMlInfo.getReelIds()[0], getPartNo(), pnInfo.getPartDescription(), String.valueOf(splitQty), getDateCode(), getVendorCode(), po, qrCode, orgPrint, coo)) { //20260116 Ann Add:coo
            Toast.makeText(getApplicationContext(), getString(R.string.reprinting_due_to_issue_with_sub_package_label), Toast.LENGTH_LONG).show();
            dialog.dismiss();
            return;
        }

        dialog.dismiss();
        Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
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

    //20260116 Ann Add
    public String getCOO(){
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportCOO.getText().toString().trim();
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            return txtImportReelIdCOO.getText().toString().trim();
        } else {
            return QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.COO);
        }
    }

    public String getQty() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportOriginalQty.getText().toString().trim();
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            return txtImportReelIdOriginalQty.getText().toString().trim();
        } else {
            return QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.QTY);
        }
    }

    public String getSplitQty() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportSplitQty.getText().toString().trim();
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            return txtImportReelIdSplitQty.getText().toString().trim();
        } else {
            return txtImportQrCodeSplitQty.getText().toString().trim();
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

    public String getPoWithQtyList() {
        if (qrcodeLayout.getVisibility() == View.VISIBLE) {
            return QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.PO_QTY_LIST);
        } else {
            return "";
        }
    }

    public String getVendorCode() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportVC.getText().toString().trim();
        } else {
            return getReelId().substring(12, 18);
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

    private void printTest() {
        errorInfo = "";

        String reelId = "72E0348004001111201922111120000100";
        String partNo = "72E034800400";
        String description = "R384RF LFP";
        String qty = "999";
        String dateCode = "1922111120";
        String vendorCode = "111120";
        String po = "999999-99";
        String qrCode = reelId + "@" + po + "," + qty + "@" + qty;
        String coo = "TWN";

        if (!printLabel(reelId, partNo, description, qty, dateCode, vendorCode, po, qrCode, coo)) {
            errorInfo = getString(R.string.printLabalFailed);
            mConnection.setText(getString(R.string.printer_connect_error));
            mConnection.setTextColor(Color.RED);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }
    }

    //20260116 Ann Add:COO
    private boolean printLabel(String reelId, String partNo, String Description, String qty,
                               String dateCode, String vendorCode, String po, String qrCode, String coo) {
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
            //x = mm2dot(36);
            //y = mm2dot(20);

            if(Constant.ISORG){
                /*x = mm2dot(50);
                y = mm2dot(7);

                if(!TextUtils.isEmpty(orgPrint)){
                    TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,2,2,\"" + orgPrint + "\"\n");
                }else{
                    TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,2,2,\"" + AppController.getOrgName() + "\"\n");
                }*/
                x = mm2dot(43);
                y = mm2dot(8);

                if(!TextUtils.isEmpty(orgPrint)){
                    TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"4\",0,2,2,\"" + AppController.getOrgName(Integer.parseInt(orgPrint)) + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(9);
                    TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 2, orgPrint);
                }else{
                    TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"4\",0,2,2,\"" + AppController.getOrgName() + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(9);
                    TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 2, AppController.getOrg()+"");
                }
            }

            //COO 20260116 Ann Add
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
            y = mm2dot(27); //20260116 Ann Edit:QRCode從23下移至27
            TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", qrCode);
            x = mm2dot(55);
            y = mm2dot(45);
            TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "W/H Packing");
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

    private boolean savePrinterInfo(String ip, String port, String qty) {
        try {
            Preferences.setString(this, Preferences.SPLIT_MATERIAL_PRINTER_IP, ip);
            Preferences.setString(this, Preferences.SPLIT_MATERIAL_PRINTER_PORT, port);
            Preferences.setString(this, Preferences.SPLIT_MATERIAL_PRINTER_QTY, qty);
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

    private void returnPage() {
        finish();
    }

    public void onBackPressed() {
        // do something here and don't write super.onBackPressed()
        returnPage();
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

    private void getReelIdSubList(String seq) {
        subMlInfo = new MaterialLabelHelper();
        subMlInfo.setPartNo(getPartNo());
        subMlInfo.setVendorCode(getVendorCode());
        subMlInfo.setDateCode(getDateCode());
        subMlInfo.setSeqNo(seq);
        subMlInfo.setLabelCount(1);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetReelIdSubList().execute(0);
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

                    if (rbPartNo.isChecked() || (rbReelID.isChecked() && txtImportReelId.length() == 28)) {
                        getReelIdList();
                    } else {
                        getReelIdSubList(getReelId().substring(28, 32));
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
                    getReelIdSubList(mlInfo.getReelIds()[0].substring(28, 32));
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

    private class GetReelIdSubList extends AsyncTask<Integer, String, MaterialLabelHelper> {
        @Override
        protected MaterialLabelHelper doInBackground(Integer... params) {
            AppController.debug("Get Reel ID Sub List from " + AppController.getServerInfo()
                    + AppController.getProperties("GetReelIdSubList"));
            publishProgress(getString(R.string.downloading_data));
            return print.getReelIdSubList(subMlInfo);
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
                subMlInfo = result;
                if (subMlInfo.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
//                    orgPrint = QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.ORG);

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

                    errorInfo = subMlInfo.getStrErrorBuf();
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
