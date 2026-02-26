package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
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
import com.senao.warehouse.database.ITEM_LABEL_QR_CODE_FORMAT;
import com.senao.warehouse.database.MaterialLabelHelper;
import com.senao.warehouse.database.PnItemInfoHelper;
import com.senao.warehouse.handler.PrintLabelHandler;
import com.senao.warehouse.util.CharUtil;
import com.senao.warehouse.util.Preferences;
import com.senao.warehouse.util.QrCodeUtil;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.util.Util;

public class SmtPrintMaterialLabelActivity extends Activity {

    private TextView mConnection;
    private EditText txtImportQty, txtImportPartNo, txtImportDC, txtImportVC, txtImportPo, txtImportQrCode, txtImportReelId, txtImportReelIdQty, txtImportReelIdPo;
    private Button btnReturn, btnConfim, btnCancel;
    private ProgressDialog dialog;
    private String errorInfo = "";
    private PrintLabelHandler print;
    private PnItemInfoHelper pnInfo;
    private MaterialLabelHelper mlInfo;
    private TscWifiActivity TscEthernetDll;
    private RadioGroup rgDC;
    private RadioButton rbQrCode, rbReelID, rbPartNo;
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
        setContentView(R.layout.activity_smt_print_material_label);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(
                            SmtPrintMaterialLabelActivity.this);
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
        String subtitle = getString(R.string.label_smt_material_label_print_eng);
//        String title = getString(R.string.label_smt_print_material_label, subtitle);
        String title = getString(R.string.label_smt_print_material_label1,AppController.getOrgName(), subtitle);
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

        qrcodeLayout = findViewById(R.id.qrcodeLayout);

        txtImportQrCode = findViewById(R.id.edittext_import_dc_qrcode);
        txtImportQrCode.setSelectAllOnFocus(true);

        reelIdLayout = findViewById(R.id.reelIdLayout);
        txtImportReelId = findViewById(R.id.edittext_reelid);
        txtImportReelId.setFilters(new InputFilter[]{new InputFilter.LengthFilter(34)});
        txtImportReelId.setSelectAllOnFocus(true);
        txtImportReelIdQty = findViewById(R.id.edittext_reelid_qty);
        txtImportReelIdPo = findViewById(R.id.edittext_reelid_po);
        txtImportReelIdPo.setSelectAllOnFocus(true);

        pnLayout = findViewById(R.id.pnLayout);
        txtImportPartNo = findViewById(R.id.edittext_pn);
        // 限制長度為12
        txtImportPartNo
                .setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        txtImportPartNo.setSelectAllOnFocus(true);
        txtImportQty = findViewById(R.id.edittext_qty);
        txtImportQty.setSelectAllOnFocus(true);
        txtImportDC = findViewById(R.id.edittext_dc);
        txtImportDC.setSelectAllOnFocus(true);
        txtImportVC = findViewById(R.id.edittext_vendor_code);
        txtImportVC.setSelectAllOnFocus(true);
        txtImportPo = findViewById(R.id.edittext_pn_po);
        txtImportPo.setSelectAllOnFocus(true);

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
                //onCreateChoosePrinterDialog().show();
                if (checkFields()) {
                    doQueryPN();
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

        rbReelID.setChecked(true);
        print = new PrintLabelHandler();
    }

    private void doQueryPN() {
        pnInfo = new PnItemInfoHelper();
        pnInfo.setPartNo(getPartNo());

        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetPNInfo().execute(0);
    }

    private boolean checkFields() {

        if (pnLayout.getVisibility() == View.VISIBLE) {
            txtImportPartNo.setText(txtImportPartNo.getText().toString().trim());

            txtImportQty.setText(txtImportQty.getText().toString().trim());

            txtImportDC.setText(txtImportDC.getText().toString().trim());

            txtImportVC.setText(txtImportVC.getText().toString().trim());

            txtImportPo.setText(txtImportPo.getText().toString().trim());

            if (TextUtils.isEmpty(txtImportPartNo.getText())) {
                txtImportPartNo.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_sku), Toast.LENGTH_SHORT)
                        .show();
                return false;
            }

            if (txtImportPartNo.getText().length() != 12) {
                txtImportPartNo.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.sku_len_must_be_12),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportQty.getText())) {
                txtImportQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.input_printer_qty), Toast.LENGTH_SHORT)
                        .show();
                return false;
            }

            if (Integer.parseInt(txtImportQty.getText().toString()) < 1) {
                txtImportQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.num_has_greater_0),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportDC.getText())) {
                txtImportDC.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_dc), Toast.LENGTH_SHORT)
                        .show();
                return false;
            }

            if (txtImportDC.getText().length() != 10) {
                txtImportDC.requestFocus();
                Toast.makeText(getApplicationContext(),getString(R.string.dc_len_greater_10),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            if (!Util.isDateCodeValid(getDateCode())) {
                txtImportDC.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.dc_incorrect),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportVC.getText())) {
                txtImportVC.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_vc),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            if (txtImportVC.getText().length() != 6) {
                txtImportVC.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.vc_len_must_be_six),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

//            if (TextUtils.isEmpty(txtImportPo.getText())) {
//                txtImportPo.requestFocus();
//                Toast.makeText(getApplicationContext(), "請輸入PO", Toast.LENGTH_SHORT)
//                        .show();
//                return false;
//            }

        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {


            txtImportReelId.setText(txtImportReelId.getText().toString().trim());

            txtImportReelIdQty.setText(txtImportReelIdQty.getText().toString().trim());

            txtImportReelIdPo.setText(txtImportReelIdPo.getText().toString().trim());

            if (txtImportReelId.getText().length() != 28
                    && txtImportReelId.getText().length() != 34) {
                txtImportReelId.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.reelid_len_must_be_28_24),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            if (!Util.isDateCodeValid(getDateCode())) {
                txtImportReelId.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.dc_incorrect),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportReelIdQty.getText())) {
                txtImportReelIdQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.input_printer_qty), Toast.LENGTH_SHORT)
                        .show();
                return false;
            }

            if (Integer.parseInt(txtImportReelIdQty.getText().toString()) < 1) {
                txtImportReelIdQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.num_has_greater_0),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

//            if (TextUtils.isEmpty(txtImportReelIdPo.getText())) {
//                txtImportReelIdPo.requestFocus();
//                Toast.makeText(getApplicationContext(), "請輸入PO", Toast.LENGTH_SHORT)
//                        .show();
//                return false;
//            }

        } else {

            txtImportQrCode.setText(txtImportQrCode.getText().toString().trim());

            if (TextUtils.isEmpty(txtImportQrCode.getText())) {
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_qrcode), Toast.LENGTH_SHORT)
                        .show();
                return false;
            }

            String reelId = QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID);
            if (reelId.length() != 34) {
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.reelid_incorrect_len_must_be_34),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            if (!Util.isDateCodeValid(getDateCode())) {
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.dc_incorrect),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            String qty = QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.QTY);
            if (TextUtils.isEmpty(qty)) {
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.qr_qty_is_not_null), Toast.LENGTH_SHORT)
                        .show();
                return false;
            }

            if (Integer.parseInt(qty) < 1) {
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.num_has_greater_0),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

        }

        return true;
    }

    private void cleanData() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            txtImportPartNo.setText("");
            txtImportQty.setText("");
            txtImportDC.setText("");
            txtImportVC.setText("");
            txtImportPo.setText("");
            txtImportPartNo.requestFocus();
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            txtImportReelId.setText("");
            txtImportReelIdQty.setText("");
            txtImportReelIdPo.setText("");
            txtImportReelId.requestFocus();
        } else {
            txtImportQrCode.setText("");
            txtImportQrCode.requestFocus();
        }
    }


    private Dialog onCreateChoosePrinterDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer, null);
        final EditText etIP = item.findViewById(R.id.editTextIP);
        String ip = Preferences.getSharedPreferences(this).getString(
                Preferences.MATERIAL_PRINTER_IP, AppController.getProperties("SmtPrinterIP"));
        etIP.setText(ip);
        final EditText etPort = item.findViewById(R.id.editTextPort);
        String port = Preferences.getSharedPreferences(this).getString(
                Preferences.MATERIAL_PRINTER_PORT, AppController.getProperties("SmtPrinterPort"));
        etPort.setText(port);
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.MATERIAL_PRINTER_QTY, AppController.getProperties("SmtPrinterQty"));
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
                            dialog = ProgressDialog.show(SmtPrintMaterialLabelActivity.this,
                                    getString(R.string.holdon), getString(R.string.printingLabel), true);
                            if (!printOpen(etIP.getText().toString().trim(), etPort.getText().toString().trim())) {
                                Toast.makeText(getApplicationContext(), getString(R.string.cant_conect_printer), Toast.LENGTH_LONG).show();
                                view.setEnabled(true);
                                dialog.dismiss();
                                return;
                            }
                            //printTest();
                            int printTimes = Integer.valueOf(etQty.getText().toString());
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
                                if (!printLabel(reelId, getPartNo(), pnInfo.getPartDescription(), qty
                                        , getDateCode(), getVendorCode(), po, qrCode)) {
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
        if (!printLabel(reelId, partNo, description
                , qty, dateCode, vendorCode, po, qrCode)) {
            errorInfo = getString(R.string.printLabalFailed);
            mConnection.setText(getString(R.string.printer_connect_error));
            mConnection.setTextColor(Color.RED);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }
    }

    private boolean printLabel(String reelId, String partNo, String Description, String qty,
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
            x = mm2dot(55);
            y = mm2dot(45);
            TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "SMT Packing");
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
            Preferences.setString(this, Preferences.MATERIAL_PRINTER_IP, ip);

            Preferences.setString(this, Preferences.MATERIAL_PRINTER_PORT, port);

            Preferences.setString(this, Preferences.MATERIAL_PRINTER_QTY, qty);

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

    private class GetPNInfo extends
            AsyncTask<Integer, String, PnItemInfoHelper> {

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
            AppController.debug("onPostExecute() result json = "
                    + new Gson().toJson(result));

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
                        onCreateChoosePrinterDialog().show();
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

    private class GetReelIdList extends
            AsyncTask<Integer, String, MaterialLabelHelper> {

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
            AppController.debug("onPostExecute() result json = "
                    + new Gson().toJson(result));

            if (result != null) {
                mlInfo = result;
                if (mlInfo.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    onCreateChoosePrinterDialog().show();
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