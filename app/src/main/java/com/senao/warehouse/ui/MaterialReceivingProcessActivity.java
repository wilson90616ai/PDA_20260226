package com.senao.warehouse.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
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
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.EIQCInspectionStatusHelper;
import com.senao.warehouse.print.BtPrintLabel;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.ITEM_LABEL_QR_CODE_FORMAT;
import com.senao.warehouse.database.InvoiceHelper;
import com.senao.warehouse.database.RECEIVING_TYPE;
import com.senao.warehouse.database.ReceivingInfoHelper;
import com.senao.warehouse.database.VendorInfoHelper;
import com.senao.warehouse.handler.PrintLabelHandler;
import com.senao.warehouse.handler.ReceivingHandler;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.Preferences;
import com.senao.warehouse.util.QrCodeUtil;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.util.Util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class MaterialReceivingProcessActivity extends Activity {
    /*
     * 20251113 Ann Edit:因應神準進料後轉賣給越南神準不換標
     *                   當前Org與QR Code的Org不同，需確認是否存在SSFTP的LOOKUP_VALUE(LOOKUP_CATALOG = 'PDA' AND LOOKUP_CODE = 'RECEIVING_ORG' AND ENABLED = 'Y')，如果存在，則PASS
     *                   當前Vendor與QR Code的Vendor不同，需顯示訊息由User選擇是否來自神準，如果是，則將reelid的13-18碼變更成SSFTP的LOOKUP_VALUE(LOOKUP_CATALOG = 'PDA' AND LOOKUP_CODE = 'RECEIVING_VENDOR_CODE' AND ENABLED = 'Y')
     */

    private static final String TAG = MaterialReceivingProcessActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PRINTER_SETTING = 2;
    private static final int REQUEST_BLUETOOTH_SETTINGS = 3;
    private final String PREFIX_ITEM_NO_PCB = "7016";
    private VendorInfoHelper vendorInfo;
    private String errorInfo;
    private Button btnReturn, btnConfirm, btnCancel;
    private RECEIVING_TYPE receivingType;
    private ReceivingInfoHelper receivingInfo;
    private TextView mConnection, txtVendorInfo, txtVedorSiteCode, txtInvoiceNo, labelPo, txtPo,
            txtControl, txtPartNumber, txtPoQty, lblRecId,
            txtDeliverableQty, txtPredeliverQty, txtReceivedQty,
            txtUnreceivedQty, txtPercentage, txtTempQty, lblXboardQtyDc, lblXboardQty, lblXboardQtyQrCode,
            txtFromSenao, lblDcQrCodeOriginal, txtDcQrCodeOriginal;
    private EditText txtImportSn, txtImportReceiptsQty, txtImportPartNumber,
            txtMultiplier2, txtMultiplicand2, txtAddend2, txtImportQty2, txtImportPartNumberDc, txtImportDc,
            txtImportReelId, txtMultiplier, txtMultiplicand, txtAddend,
            txtImportQty, txtImportInvoiceNo, txtImportLocator, txtImportSingleBoxQty, txtMultiplier5,
            txtMultiplicand5, txtAddend5, txtImportQty8, txtXboardQtyDc, txtXboardQty, txtXboardQtyQrCode, txtDcQrCode, org_edtxt;
    private LinearLayout serialNoLayout, quantityLayout, partNoLayout,
            dcLayout, reelIdLayout, conditionLayout, dcQrCodeLayout, dcQrCodeOriginalLayout, checkorg;
    private RadioGroup rgDC;
    private RadioButton rbQty1, rbQty2, rbQty3, rbQty4, rbReelID, rbPartNo, rbQty5, rbQty8, rbDcQrCode;
    private ReceivingHandler receivingHandler;
    private ProgressDialog dialog;
    private boolean needRefresh = false;
    private TextView lblTitle;
    private int accType;
    private CheckBox cbGenInvoiceNo;
    private PrintLabelHandler print;
    private InvoiceHelper invoiceHelper;
    private String isInspect;//判斷是否免檢

    private String strReelid;//串接Reelid

    private RadioGroup.OnCheckedChangeListener dcRgListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio_dc_qrcode:
                    if (rbDcQrCode.isChecked()) {
                        dcQrCodeLayout.setVisibility(View.VISIBLE);
                        reelIdLayout.setVisibility(View.GONE);
                        dcLayout.setVisibility(View.GONE);
                        txtDcQrCode.requestFocus();

                        if (receivingInfo.getPartNo() != null && receivingInfo.getPartNo().substring(0, 4).equals(PREFIX_ITEM_NO_PCB)) {
                            lblXboardQtyQrCode.setVisibility(View.VISIBLE);
                            txtXboardQtyQrCode.setVisibility(View.VISIBLE);
                        } else {
                            lblXboardQtyQrCode.setVisibility(View.GONE);
                            txtXboardQtyQrCode.setVisibility(View.GONE);
                        }
                    }
                    break;
                case R.id.radio_reel_id:
                    if (rbReelID.isChecked()) {
                        dcQrCodeLayout.setVisibility(View.GONE);
                        reelIdLayout.setVisibility(View.VISIBLE);
                        dcLayout.setVisibility(View.GONE);
                        rbQty2.setChecked(true);
                        txtImportReelId.requestFocus();

                        if (receivingInfo.getPartNo() != null && receivingInfo.getPartNo().substring(0, 4).equals(PREFIX_ITEM_NO_PCB)) {
                            lblXboardQty.setVisibility(View.VISIBLE);
                            txtXboardQty.setVisibility(View.VISIBLE);
                        } else {
                            lblXboardQty.setVisibility(View.GONE);
                            txtXboardQty.setVisibility(View.GONE);
                        }
                    }
                    break;
                case R.id.radio_part_number:
                    if (rbPartNo.isChecked()) {
                        dcQrCodeLayout.setVisibility(View.GONE);
                        reelIdLayout.setVisibility(View.GONE);
                        dcLayout.setVisibility(View.VISIBLE);
                        rbQty8.setChecked(true);
                        txtImportPartNumberDc.requestFocus();

                        if (receivingInfo.getPartNo() != null && receivingInfo.getPartNo().substring(0, 4).equals(PREFIX_ITEM_NO_PCB)) {
                            lblXboardQtyDc.setVisibility(View.VISIBLE);
                            txtXboardQtyDc.setVisibility(View.VISIBLE);
                        } else {
                            lblXboardQtyDc.setVisibility(View.GONE);
                            txtXboardQtyDc.setVisibility(View.GONE);
                        }
                    }
                    break;
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener reelIDListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if (buttonView.equals(rbQty1)) {
                    rbQty2.setChecked(false);
                    txtImportQty.setEnabled(false);
                    txtMultiplier.setEnabled(true);
                    txtMultiplicand.setEnabled(true);
                    txtAddend.setEnabled(true);
                    txtImportReelId.setNextFocusDownId(R.id.edittext_multiplier);
                } else {
                    rbQty1.setChecked(false);
                    txtMultiplier.setEnabled(false);
                    txtMultiplicand.setEnabled(false);
                    txtAddend.setEnabled(false);
                    txtImportQty.setEnabled(true);
                    txtImportReelId.setNextFocusDownId(R.id.edittext_import_qty);
                }
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener dcListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if (buttonView.equals(rbQty5)) {
                    rbQty8.setChecked(false);
                    txtImportQty8.setEnabled(false);
                    txtMultiplier5.setEnabled(true);
                    txtMultiplicand5.setEnabled(true);
                    txtAddend5.setEnabled(true);
                    txtImportDc.setNextFocusDownId(R.id.edittext_multiplier5);
                } else {
                    rbQty5.setChecked(false);
                    txtMultiplier5.setEnabled(false);
                    txtMultiplicand5.setEnabled(false);
                    txtAddend5.setEnabled(false);
                    txtImportQty8.setEnabled(true);
                    txtImportDc.setNextFocusDownId(R.id.edittext_import_qty8);
                }
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener noListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if (buttonView.equals(rbQty3)) {
                    rbQty4.setChecked(false);
                    txtImportQty2.setEnabled(false);
                    txtMultiplier2.setEnabled(true);
                    txtMultiplicand2.setEnabled(true);
                    txtAddend2.setEnabled(true);
                    txtImportPartNumber.setNextFocusDownId(R.id.edittext_multiplier2);
                } else {
                    rbQty3.setChecked(false);
                    txtMultiplier2.setEnabled(false);
                    txtMultiplicand2.setEnabled(false);
                    txtAddend2.setEnabled(false);
                    txtImportQty2.setEnabled(true);
                    txtImportPartNumber.setNextFocusDownId(R.id.edittext_import_qty2);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_receiving_process);
        fetch_SSFTP_Async("sSSFmethod_GetSSFTP_VALUE", "PDA", "RECEIVING_ORG"); //20251113 Ann Edit:先取得當前Org與QR Code不同時，需要Pass的Org有哪些

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            receivingInfo = new Gson().fromJson(extras.getString("RECEIVING_INFO"), ReceivingInfoHelper.class);

            if (receivingInfo == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_receipt_data), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            vendorInfo = new Gson().fromJson(extras.getString("VENDOR_INFO"), VendorInfoHelper.class);

            if (vendorInfo == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_vc_data), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            receivingType = RECEIVING_TYPE.valueOf(receivingInfo.getReceivingType());
            receivingHandler = new ReceivingHandler();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_vc_data), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MaterialReceivingProcessActivity.this);
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

        txtVendorInfo = findViewById(R.id.txt_vendor_info);

        lblRecId = findViewById(R.id.label_rec_id);

        if (TextUtils.isEmpty(vendorInfo.getInvoiceSeq())) {
            lblRecId.setVisibility(View.GONE);
        } else {
            lblRecId.setText(getString(R.string.label_rec_id, vendorInfo.getInvoiceSeq()));
        }

        txtVedorSiteCode = findViewById(R.id.label_vendor_site_code);
        txtInvoiceNo = findViewById(R.id.txt_invoice_no);

        labelPo = findViewById(R.id.label_po);

        txtPo = findViewById(R.id.txt_po);

        txtControl = findViewById(R.id.txt_control);

        txtPartNumber = findViewById(R.id.txt_part_number);

        txtPoQty = findViewById(R.id.txt_po_qty);

        txtDeliverableQty = findViewById(R.id.txt_deliverable_qty);

        txtPredeliverQty = findViewById(R.id.txt_predeliver_qty);

        txtReceivedQty = findViewById(R.id.txt_received_qty);

        txtUnreceivedQty = findViewById(R.id.txt_unreceived_qty);

        txtTempQty = findViewById(R.id.txt_temp_qty);

        serialNoLayout = findViewById(R.id.serialNoLayout);

        txtImportSn = findViewById(R.id.edittext_import_sn);

        txtPercentage = findViewById(R.id.textview_percentage);

        quantityLayout = findViewById(R.id.quantityLayout);

        txtImportSingleBoxQty = findViewById(R.id.edittext_import_single_box_qty);

        txtImportReceiptsQty = findViewById(R.id.edittext_import_receipts_qty);

        partNoLayout = findViewById(R.id.partNoLayout);

        txtImportPartNumber = findViewById(R.id.edittext_import_part_number);

        rbQty3 = findViewById(R.id.radio_button_qty3);

        txtMultiplier2 = findViewById(R.id.edittext_multiplier2);
        txtMultiplier2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down " + String.valueOf(v.getId()));
                    return !checkQty();
                }

                return false;
            }
        });

        txtMultiplicand2 = findViewById(R.id.edittext_multiplicand2);
        txtMultiplicand2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down " + String.valueOf(v.getId()));
                    return !checkQty();
                }

                return false;
            }
        });

        txtAddend2 = findViewById(R.id.edittext_addend2);
        txtAddend2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down " + String.valueOf(v.getId()));

                    if (checkQty()) {
                        hideKeyboard();
                    } else {
                        return true;
                    }
                }

                return false;
            }
        });

        rbQty4 = findViewById(R.id.radio_button_qty4);

        txtImportQty2 = findViewById(R.id.edittext_import_qty2);
        txtImportQty2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down " + String.valueOf(v.getId()));
                    return !checkQty();
                }

                return false;
            }
        });


        lblXboardQtyDc = findViewById(R.id.label_import_x_board_qty_dc);
        txtXboardQtyDc = findViewById(R.id.edittext_import_x_board_qty_dc);

        rbQty3.setOnCheckedChangeListener(noListener);
        rbQty4.setOnCheckedChangeListener(noListener);

        rgDC = findViewById(R.id.radio_group_dc);

        rbDcQrCode = findViewById(R.id.radio_dc_qrcode);
        rbReelID = findViewById(R.id.radio_reel_id);
        rbPartNo = findViewById(R.id.radio_part_number);
        rgDC.setOnCheckedChangeListener(dcRgListener);

        dcLayout = findViewById(R.id.dcLayout);
        txtImportPartNumberDc = findViewById(R.id.edittext_import_part_number_dc);
        txtImportDc = findViewById(R.id.edittext_import_pn_dc);
        txtImportDc.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down " + v.getId());

                    if (checkDc()) {
                        if (receivingType == RECEIVING_TYPE.REC_INVOICE || receivingType == RECEIVING_TYPE.TEMP_INVOICE) {
                        }
                    } else {
                        return true;
                    }
                }

                return false;
            }
        });

        /* 20251113 Ann Edit:來自神準*/
        txtFromSenao = findViewById(R.id.txt_from_senao);

        /* 20251113 Ann Edit:原始QR Code*/
        dcQrCodeOriginalLayout = findViewById(R.id.dcQrCodeOriginalLayout);
        lblDcQrCodeOriginal= findViewById(R.id.label_DcQrCode_Original);
        txtDcQrCodeOriginal= findViewById(R.id.txt_DcQrCode_Original);

        dcQrCodeLayout = findViewById(R.id.dcQrCodeLayout);
        txtDcQrCode = findViewById(R.id.edittext_import_dc_qrcode);
        txtDcQrCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down " + v.getId());
                    EditText et = (EditText) v;

                    if (TextUtils.isEmpty(et.getText())) {
                        return false;
                    } else {
                        txtXboardQtyQrCode.requestFocus();
                        return true;
                    }
                }

                return false;
            }
        });

        lblXboardQtyQrCode = findViewById(R.id.label_import_x_board_qty_qrcode);
        txtXboardQtyQrCode = findViewById(R.id.edittext_import_x_board_qty_qrcode);

        reelIdLayout = findViewById(R.id.reelIdLayout);

        rbQty5 = findViewById(R.id.radio_button_qty5);

        txtMultiplier5 = findViewById(R.id.edittext_multiplier5);
        txtMultiplier5.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down " + String.valueOf(v.getId()));
                    return !checkQty();
                }

                return false;
            }
        });

        txtMultiplicand5 = findViewById(R.id.edittext_multiplicand5);
        txtMultiplicand5.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down " + String.valueOf(v.getId()));
                    return !checkQty();
                }

                return false;
            }
        });

        txtAddend5 = findViewById(R.id.edittext_addend5);
        txtAddend5.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down " + String.valueOf(v.getId()));

                    if (checkQty()) {
                        hideKeyboard();
                    } else {
                        return true;
                    }
                }

                return false;
            }
        });

        rbQty8 = findViewById(R.id.radio_button_qty8);

        txtImportQty8 = findViewById(R.id.edittext_import_qty8);
        txtImportQty8.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down " + String.valueOf(v.getId()));
                    return !checkQty();
                }

                return false;
            }
        });

        rbQty5.setOnCheckedChangeListener(dcListener);
        rbQty8.setOnCheckedChangeListener(dcListener);

        txtImportReelId = findViewById(R.id.edittext_import_reel_id);
        txtImportReelId.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down " + String.valueOf(v.getId()));

                    if (checkReelId()) {
                        if (receivingType == RECEIVING_TYPE.REC_INVOICE || receivingType == RECEIVING_TYPE.TEMP_INVOICE) {
                        }
                    } else {
                        return true;
                    }
                }

                return false;
            }
        });

        rbQty1 = findViewById(R.id.radio_button_qty1);

        txtMultiplier = findViewById(R.id.edittext_multiplier);
        txtMultiplier.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down " + String.valueOf(v.getId()));
                    return !checkQty();
                }

                return false;
            }
        });

        txtMultiplicand = findViewById(R.id.edittext_multiplicand);
        txtMultiplicand.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down " + String.valueOf(v.getId()));
                    return !checkQty();
                }

                return false;
            }
        });

        txtAddend = findViewById(R.id.edittext_addend);
        txtAddend.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down " + String.valueOf(v.getId()));

                    if (checkQty()) {
                        hideKeyboard();
                    } else {
                        return true;
                    }
                }

                return false;
            }
        });

        rbQty2 = findViewById(R.id.radio_button_qty2);

        txtImportQty = findViewById(R.id.edittext_import_qty);
        txtImportQty.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down " + String.valueOf(v.getId()));
                    return !checkQty();
                }

                return false;
            }
        });

        lblXboardQty = findViewById(R.id.label_import_x_board_qty);
        txtXboardQty = findViewById(R.id.edittext_import_x_board_qty);

        rbQty1.setOnCheckedChangeListener(reelIDListener);
        rbQty2.setOnCheckedChangeListener(reelIDListener);

        conditionLayout = findViewById(R.id.conditionLayout);
        cbGenInvoiceNo = findViewById(R.id.checkbox_gen_invoice_no);

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

        txtImportInvoiceNo = findViewById(R.id.edittext_import_invoice_no);
        txtImportInvoiceNo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down " + String.valueOf(v.getId()));
                    return !checkInvoiceNo();
                }

                return false;
            }
        });

        txtImportLocator = findViewById(R.id.edittext_import_locator);

        btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Return", String.valueOf(v.getId()));
                returnPage();
            }

        });

        btnConfirm = findViewById(R.id.button_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Confirm", String.valueOf(v.getId()));

                if(AppController.getReceivingOrg().contains(String.valueOf(AppController.getOrg()))){ //20251113 Ann Edit:因應神準進料後轉賣給越南神準不換標
                    if(! txtDcQrCode.getText().toString().trim().substring(12, 18).equals(vendorInfo.getNum())){
                        new AlertDialog.Builder(MaterialReceivingProcessActivity.this)
                                .setTitle(getString(R.string.btn_ok))
                                .setMessage(getString(R.string.material_from_senao_YN))
                                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dcQrCodeOriginalLayout.setVisibility(View.VISIBLE); //20251113 Ann Edit:顯示 原始QR Code
                                        txtDcQrCodeOriginal.setText(txtDcQrCode.getText().toString().trim()); //20251113 Ann Edit:紀錄 原始QR Code

                                        //20251113 Ann Edit:變更QR Code的值
                                        StringBuilder sb = new StringBuilder(txtDcQrCode.getText().toString().trim());
                                        sb.replace(12, 18, vendorInfo.getNum());
                                        String temp = sb.toString();
                                        int lastAt = temp.lastIndexOf("@");
                                        if (lastAt != -1) {
                                            temp = temp.substring(0, lastAt + 1) + String.valueOf(AppController.getOrg());
                                        }
                                        txtDcQrCode.setText(temp);

                                        //20251113 Ann Edit:來自神準
                                        txtFromSenao.setText("Y");
                                        txtFromSenao.setTextColor(Color.RED);

                                        /*String strPrefix = txtDcQrCode.getText().toString().trim().substring(0, 12);
                                        String strSuffix = txtDcQrCode.getText().toString().trim().substring(18);
                                        int lastAt = strSuffix.lastIndexOf("@");
                                        String strSuffix_Org = "";
                                        if(lastAt != -1){
                                            strSuffix_Org = strSuffix.substring(0, lastAt+1) + AppController.getOrg();
                                        }
                                        txtDcQrCode.setText(strPrefix + vendorInfo.getNum() + strSuffix_Org);*/

                                        if (checkFields()) {
                                            AppController.debug("送出的免檢data:" + strReelid);
                                            new GetEIQCInspectionStatus().execute(0);
                                        }
                                    }
                                })
                                .setNegativeButton(getString(R.string.no), null)
                                .setCancelable(false)
                                .show();

                        return; //等待使用者按 是 / 否
                    }

                    /*
                     * txtDcQrCode.getText().toString().trim().split("@");
                     * 先檢查 AppController.getReceivingOrg().contains(String.valueOf(AppController.getOrg()))，還有 txtDcQrCode 的第13-18碼是否與vendorInfo.getNum()相同
                     * 不同則show出訊息"是否為AAA出貨?"
                     * 如果選擇是，則show出隱藏的layout dcQrCodeOriginalLayout，並將 txtDcQrCode 的值放到layout dcQrCodeOriginalLayout 的 txtDcQrCodeOriginal(lblDcQrCodeOriginal)
                     * 然後再將 txtDcQrCode 變更成別的資料，txtFromSenao 變成紅色的Y
                     */
                }

                if (checkFields()) {
                    AppController.debug("送出的免檢data:"+strReelid);
                    //checkPrinterSetting();
                    new GetEIQCInspectionStatus().execute(0);

                    /*if (conditionLayout.getVisibility() == View.VISIBLE) {
                        if (receivingType == RECEIVING_TYPE.REC_COMBINE || receivingType == RECEIVING_TYPE.TEMP_COMBINE) {
                            AppController.debug("收料作業=>doCheckInvoiceNo()");
                            doCheckInvoiceNo();
                            return;
                        } else {
                            if (receivingInfo.getInvoiceNo() == null || !receivingInfo.getInvoiceNo().equals(txtImportInvoiceNo.getText().toString().trim())) {
                                AppController.debug("收料作業=>updateInvoiceNo()");
                                updateInvoiceNo();
                                return;
                            }
                            if (vendorInfo.getIsRemittance() != null && vendorInfo.getIsRemittance().equals("N")) {
                                AppController.debug("收料作業=>doQuerySeq()");
                                doQuerySeq();
                                return;
                            }
                        }
                    }
                    setView(true);*/
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

        setTitle();
        txtVendorInfo.setText(vendorInfo.getName());
        txtVedorSiteCode.setText(vendorInfo.getSiteCode());

        if (receivingType == RECEIVING_TYPE.REC_COMBINE || receivingType == RECEIVING_TYPE.TEMP_COMBINE
                || receivingType == RECEIVING_TYPE.REC_PO || receivingType == RECEIVING_TYPE.TEMP_PO) {
            receivingInfo.setInputInvoiceNo(vendorInfo.getInvoiceNo());
            receivingInfo.setInvoiceNo(vendorInfo.getInvoiceNo());
            receivingInfo.setLocator(vendorInfo.getLocator());
            txtImportInvoiceNo.setText(vendorInfo.getInvoiceNo());
            txtImportLocator.setText(vendorInfo.getLocator());
            txtInvoiceNo.setText(vendorInfo.getInvoiceNo());
        }

        org_edtxt = findViewById(R.id.org_edtxt);
        checkorg = findViewById(R.id.checkorg);

        if(Constant.ISORG){
            checkorg.setVisibility(View.GONE);
        }else{
            checkorg.setVisibility(View.GONE);
        }

        setSummaryData();
        setView(true);

        if (receivingType == RECEIVING_TYPE.REC_INVOICE || receivingType == RECEIVING_TYPE.TEMP_INVOICE) {
            checkPrinterSetting();
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

    private void doCheckInvoiceNo() {
        receivingInfo.setInputInvoiceNo(txtImportInvoiceNo.getText().toString().trim());
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_updating), true);
        new DoCheckInvoiceNo().execute(0);
    }

    private void updateInvoiceNo() {
        receivingInfo.setInputInvoiceNo(txtImportInvoiceNo.getText().toString().trim());
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_updating), true);
        new UpdateInvoiceNo().execute(0);
    }

    private void setTitle() {
        String subtitle = null;
        switch (receivingType) {
            case REC_COMBINE:
                subtitle = getString(R.string.combine) + " - " + getString(R.string.Receiving);
                break;
            case REC_INVOICE:
                subtitle = getString(R.string.invoice) + " - " + getString(R.string.Receiving);
                break;
            case REC_PO:
                subtitle = getString(R.string.po) + " - " + getString(R.string.Receiving) + "1";
                break;
            case TEMP_COMBINE:
                subtitle = getString(R.string.combine) + " - " + getString(R.string.Pending_inspection);
                break;
            case TEMP_INVOICE:
                subtitle = getString(R.string.invoice) + " - " + getString(R.string.Pending_inspection);
                break;
            case TEMP_PO:
                subtitle = getString(R.string.po) + " - " + getString(R.string.Pending_inspection) + "1";
                break;
            case REC_COMBINE_INVOICE:
                subtitle = getString(R.string.combine_invoice) + " - " + getString(R.string.Receiving);
                break;
            case TEMP_COMBINE_INVOICE:
                subtitle = getString(R.string.combine_invoice) + " - " + getString(R.string.Pending_inspection);
                break;
            case DELETE:

            case DEL_ERROR:

            case DEL_PREDELIVER:
                subtitle = getString(R.string.label_Del);
                break;
        }

        final SpannableString text;

        if (accType == MaterialReceivingActivity.OUTSOURCING) {
            text = new SpannableString(getString(R.string.label_receiving_outsourcing1, AppController.getOrgName(), subtitle));
        } else {
            text = new SpannableString(getString(R.string.label_receiving1, AppController.getOrgName(), subtitle));
        }

        text.setSpan(new RelativeSizeSpan(1.0f), 0, 6,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new RelativeSizeSpan(0.7f), 7+AppController.getOrgName().length(), 7+AppController.getOrgName().length() + subtitle.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        lblTitle.setText(text);
        txtInvoiceNo.setText(receivingInfo.getInvoiceNo());
        txtControl.setText(receivingInfo.getControl());
        txtPartNumber.setText(receivingInfo.getPartNo());

        if (receivingType == RECEIVING_TYPE.REC_PO || receivingType == RECEIVING_TYPE.TEMP_PO) {
            labelPo.setVisibility(View.VISIBLE);
            txtPo.setVisibility(View.VISIBLE);
            txtPo.setText(receivingInfo.getPo());
        } else {
            labelPo.setVisibility(View.GONE);
            txtPo.setVisibility(View.GONE);
        }
    }

    protected void doCheckProcess(boolean updateReelID) {
        receivingInfo.setInputInvoiceNo(txtImportInvoiceNo.getText().toString().trim());
        receivingInfo.setLocator(txtImportLocator.getText().toString().trim());
        receivingInfo.setInputQty(BigDecimal.valueOf(getInputQty()));
        receivingInfo.setUpdateReelID(updateReelID);
        receivingInfo.setXbQty(BigDecimal.valueOf(0));

        if (receivingInfo.getControl().equals("DC")) {
            if (rbDcQrCode.isChecked()) {
                receivingInfo.setReelID(getReelID().substring(0, 28));
                if (txtXboardQtyQrCode.getVisibility() == View.VISIBLE) {
                    receivingInfo.setXbQty(BigDecimal.valueOf(Double.parseDouble(txtXboardQtyQrCode.getText().toString().trim())));
                } else {
                    receivingInfo.setXbQty(BigDecimal.valueOf(0));
                }
            } else if (rbReelID.isChecked()) {
                receivingInfo.setReelID(getReelID().substring(0, 28));

                if (txtXboardQty.getVisibility() == View.VISIBLE) {
                    receivingInfo.setXbQty(BigDecimal.valueOf(Double.parseDouble(txtXboardQty.getText().toString().trim())));
                } else {
                    receivingInfo.setXbQty(BigDecimal.valueOf(0));
                }
            } else {
                receivingInfo.setReelID(getReelID().substring(0, 28));

                if (txtXboardQtyDc.getVisibility() == View.VISIBLE) {
                    receivingInfo.setXbQty(BigDecimal.valueOf(Double.parseDouble(txtXboardQtyDc.getText().toString().trim())));
                } else {
                    receivingInfo.setXbQty(BigDecimal.valueOf(0));
                }
            }
        } else if (receivingInfo.getControl().equals("NO")) {
            receivingInfo.setPartNo(txtImportPartNumber.getText().toString().trim());
        } else {
            receivingInfo.setSingleBoxQty(BigDecimal.valueOf(Double.parseDouble((txtImportSingleBoxQty.getText().toString().trim()))));
            receivingInfo.setSerialNo(this.txtImportSn.getText().toString().trim());
        }

        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_chking), true);
        new DoCheckProcess().execute(0);
    }

    private String getReelID() {
        if (rbDcQrCode.isChecked()) { //dcQrCodeOriginalLayout  lblDcQrCodeOriginal txtDcQrCodeOriginal
            return QrCodeUtil.getValueFromItemLabelQrCode(txtDcQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID);
        } else if (rbReelID.isChecked()) {
            return txtImportReelId.getText().toString().trim();
        } else {
            return getPartNo() + getVendorCode() + getDateCode();
        }
    }

    private String getVendorCode() {
        if (rbDcQrCode.isChecked()) {
            return getReelID().substring(12, 18);
        } else if (rbReelID.isChecked()) {
            return getReelID().substring(12, 18);
        } else {
            return getDateCode().substring(4);
        }
    }

    public String getDateCode() {
        if (rbDcQrCode.isChecked()) {
            return getReelID().substring(18, 28);
        } else if (rbReelID.isChecked()) {
            return getReelID().substring(18, 28);
        } else {
            return txtImportDc.getText().toString().trim();
        }
    }

    public String getPartNo() {
        if (rbDcQrCode.isChecked()) {
            return getReelID().substring(0, 12);
        } else if (rbReelID.isChecked()) {
            return getReelID().substring(0, 12);
        } else {
            return txtImportPartNumberDc.getText().toString().trim();
        }
    }

    protected void doCheckReelID() {
        receivingInfo.setInputInvoiceNo(txtImportInvoiceNo.getText().toString().trim());
        receivingInfo.setLocator(txtImportLocator.getText().toString().trim());
        receivingInfo.setInputQty(BigDecimal.valueOf(getInputQty()));
        receivingInfo.setReelID(getReelID().substring(0, 28));
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_chking), true);
        new DoCheckReelID().execute(0);
    }

    protected void doCheckDateCode() {
        receivingInfo.setInputInvoiceNo(txtImportInvoiceNo.getText().toString().trim());
        receivingInfo.setLocator(txtImportLocator.getText().toString().trim());
        receivingInfo.setInputQty(BigDecimal.valueOf(getInputQty()));
        receivingInfo.setDateCode(getDateCode());
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_chking), true);
        new DoCheckDateCode().execute(0);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtImportSn.getWindowToken(), 0);
    }

    private boolean checkInvoiceNo() {
        if (txtImportInvoiceNo.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_invoice), Toast.LENGTH_LONG).show();
            txtImportInvoiceNo.requestFocus();
            return false;
        } else if (txtImportInvoiceNo.getText().toString().trim().length() < 10) {
            Toast.makeText(getApplicationContext(), getString( R.string.enter_correct_invoice), Toast.LENGTH_LONG).show();
            txtImportInvoiceNo.requestFocus();
            return false;
        }

        return true;
    }

    private boolean checkReelId() {
        String reelID = getReelID();
        strReelid = reelID;

        if (dcQrCodeLayout.getVisibility() == View.VISIBLE) {
            if (TextUtils.isEmpty(reelID)) {
                Toast.makeText(getApplicationContext(), getString(R.string.qr_code_has_error), Toast.LENGTH_LONG).show();
                txtDcQrCode.requestFocus();
                return false;
            } else if (reelID.length() != 34) {
                Toast.makeText(getApplicationContext(), getString(R.string.reelid_incorrect_len_must_be_34), Toast.LENGTH_SHORT).show();
                txtDcQrCode.requestFocus();
                return false;
            } else if (!reelID.substring(0, 12).equals(Util.getSenaoPartNo(receivingInfo.getPartNo()))) {
                Toast.makeText(getApplicationContext(), getString(R.string.sku_incorrect), Toast.LENGTH_SHORT).show();
                txtDcQrCode.requestFocus();
                return false;
            } else if (!reelID.substring(12, 18).equals(vendorInfo.getNum())) {
                Toast.makeText(getApplicationContext(), getString(R.string.vc_incorrect), Toast.LENGTH_SHORT).show();
                txtDcQrCode.requestFocus();
                return false;
            } else if (!Util.isDateCodeValid(reelID.substring(18, 28))) {
                Toast.makeText(getApplicationContext(), getString(R.string.dc_incorrect), Toast.LENGTH_SHORT).show();
                txtDcQrCode.requestFocus();
                return false;
            }
        } else {
            if (TextUtils.isEmpty(reelID)) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_reelid), Toast.LENGTH_LONG).show();
                txtImportReelId.requestFocus();
                return false;
            } else if (reelID.length() != 28 && reelID.length() != 34) {
                Toast.makeText(getApplicationContext(), getString(R.string.reelid_incorrect_len_must_be_34_or_28), Toast.LENGTH_SHORT).show();
                txtImportReelId.requestFocus();
                return false;
            } else if (!reelID.substring(0, 12).equals(Util.getSenaoPartNo(receivingInfo.getPartNo()))) {
                Toast.makeText(getApplicationContext(), getString(R.string.sku_incorrect), Toast.LENGTH_SHORT).show();
                txtImportReelId.requestFocus();
                return false;
            } else if (!reelID.substring(12, 18).equals(vendorInfo.getNum())) {
                Toast.makeText(getApplicationContext(), getString(R.string.vc_incorrect), Toast.LENGTH_SHORT).show();
                txtImportReelId.requestFocus();
                return false;
            } else if (!Util.isDateCodeValid(reelID.substring(18, 28))) {
                Toast.makeText(getApplicationContext(), getString(R.string.dc_incorrect), Toast.LENGTH_SHORT).show();
                txtImportReelId.requestFocus();
                return false;
            }
        }

        return true;
    }

    private boolean checkQty() {
        if (dcQrCodeLayout.getVisibility() == View.VISIBLE) {
            String value = QrCodeUtil.getValueFromItemLabelQrCode(txtDcQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.QTY);

            if (TextUtils.isEmpty(value)) {
                Toast.makeText(getApplicationContext(), getString(R.string.qrcode_qty_error), Toast.LENGTH_SHORT).show();
                txtDcQrCode.requestFocus();
                return false;
            } else if ((receivingType == RECEIVING_TYPE.REC_INVOICE || receivingType == RECEIVING_TYPE.TEMP_INVOICE) && getInputQty() > receivingInfo.getPredeliverQty().doubleValue()) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_greater_than_predeliver), Toast.LENGTH_SHORT).show();
                txtDcQrCode.requestFocus();
                return false;
            } else if (getInputQty() > receivingInfo.getPoQty().subtract(receivingInfo.getReceivedQty()).subtract(receivingInfo.getTempQty()).doubleValue()) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_greater_than_poqty), Toast.LENGTH_SHORT).show();
                txtDcQrCode.requestFocus();
                return false;
            } else if (getInputQty() <= 0) {
                Toast.makeText(getApplicationContext(), getString(R.string.receipts_qty_cant_less_than_or_equals_0), Toast.LENGTH_SHORT).show();
                txtDcQrCode.requestFocus();
                return false;
            }
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            if (rbQty1.isChecked()) {
                if (txtMultiplier.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_multiplier), Toast.LENGTH_SHORT).show();
                    txtMultiplier.requestFocus();
                    return false;
                } else if (txtMultiplicand.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_multiplicand), Toast.LENGTH_SHORT).show();
                    txtMultiplicand.requestFocus();
                    return false;
                } else if (txtAddend.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_add), Toast.LENGTH_SHORT).show();
                    txtAddend.requestFocus();
                    return false;
                } else if ((receivingType == RECEIVING_TYPE.REC_INVOICE || receivingType == RECEIVING_TYPE.TEMP_INVOICE) && getInputQty() > receivingInfo.getPredeliverQty().doubleValue()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cant_greater_than_predeliver), Toast.LENGTH_SHORT).show();
                    txtMultiplier.requestFocus();
                    return false;
                } else if (getInputQty() > receivingInfo.getPoQty().subtract(receivingInfo.getReceivedQty()).subtract(receivingInfo.getTempQty()).doubleValue()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.greater_than_po_qty), Toast.LENGTH_SHORT).show();
                    txtMultiplier.requestFocus();
                    return true;
                } else if (getInputQty() <= 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cant_greater_than_poqty), Toast.LENGTH_SHORT).show();
                    txtMultiplier.requestFocus();
                    return false;
                }
            } else {
                if (txtImportQty.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_qty), Toast.LENGTH_SHORT).show();
                    txtImportQty.requestFocus();
                    return false;
                } else if ((receivingType == RECEIVING_TYPE.REC_INVOICE || receivingType == RECEIVING_TYPE.TEMP_INVOICE) && getInputQty() > receivingInfo.getPredeliverQty().doubleValue()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cant_greater_than_predeliver), Toast.LENGTH_SHORT).show();
                    txtImportQty.requestFocus();
                    return false;
                } else if (getInputQty() > receivingInfo.getPoQty().subtract(receivingInfo.getReceivedQty()).subtract(receivingInfo.getTempQty()).doubleValue()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cant_greater_than_poqty), Toast.LENGTH_SHORT).show();
                    txtImportQty.requestFocus();
                    return false;
                } else if (getInputQty() <= 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cant_greater_than_poqty), Toast.LENGTH_SHORT).show();
                    txtImportQty.requestFocus();
                    return false;
                }
            }
        } else if (dcLayout.getVisibility() == View.VISIBLE) {
            if (rbQty5.isChecked()) {
                if (txtMultiplier5.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_multiplier), Toast.LENGTH_SHORT).show();
                    txtMultiplier5.requestFocus();
                    return false;
                } else if (txtMultiplicand5.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_multiplicand), Toast.LENGTH_SHORT).show();
                    txtMultiplicand5.requestFocus();
                    return false;
                } else if (txtAddend5.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_add), Toast.LENGTH_SHORT).show();
                    txtAddend5.requestFocus();
                    return false;
                } else if ((receivingType == RECEIVING_TYPE.REC_INVOICE || receivingType == RECEIVING_TYPE.TEMP_INVOICE) && getInputQty() > receivingInfo.getPredeliverQty().doubleValue()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cant_greater_than_predeliver), Toast.LENGTH_SHORT).show();
                    txtMultiplier5.requestFocus();
                    return false;
                } else if (getInputQty() > receivingInfo.getPoQty().subtract(receivingInfo.getReceivedQty()).subtract(receivingInfo.getTempQty()).doubleValue()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.greater_than_po_qty), Toast.LENGTH_SHORT).show();
                    txtMultiplier5.requestFocus();
                    return true;
                } else if (getInputQty() <= 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cant_greater_than_poqty), Toast.LENGTH_SHORT).show();
                    txtMultiplier5.requestFocus();
                    return false;
                }
            } else {
                if (txtImportQty8.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_qty), Toast.LENGTH_SHORT).show();
                    txtImportQty8.requestFocus();
                    return false;
                } else if ((receivingType == RECEIVING_TYPE.REC_INVOICE || receivingType == RECEIVING_TYPE.TEMP_INVOICE) && getInputQty() > receivingInfo.getPredeliverQty().doubleValue()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cant_greater_than_predeliver), Toast.LENGTH_SHORT).show();
                    txtImportQty8.requestFocus();
                    return false;
                } else if (getInputQty() > receivingInfo.getPoQty().subtract(receivingInfo.getReceivedQty()).subtract(receivingInfo.getTempQty()).doubleValue()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cant_greater_than_poqty), Toast.LENGTH_SHORT).show();
                    txtImportQty8.requestFocus();
                    return false;
                } else if (getInputQty() <= 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cant_greater_than_poqty), Toast.LENGTH_SHORT).show();
                    txtImportQty8.requestFocus();
                    return false;
                }
            }
        } else if (partNoLayout.getVisibility() == View.VISIBLE) {
            if (rbQty3.isChecked()) {
                if (txtMultiplier2.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_multiplier), Toast.LENGTH_SHORT).show();
                    txtMultiplier2.requestFocus();
                    return false;
                } else if (txtMultiplicand2.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_multiplicand), Toast.LENGTH_SHORT).show();
                    txtMultiplicand2.requestFocus();
                    return false;
                } else if (txtAddend2.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_add), Toast.LENGTH_SHORT).show();
                    txtAddend2.requestFocus();
                    return false;
                } else if ((receivingType == RECEIVING_TYPE.REC_INVOICE || receivingType == RECEIVING_TYPE.TEMP_INVOICE) && getInputQty() > receivingInfo.getPredeliverQty().doubleValue()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cant_greater_than_predeliver), Toast.LENGTH_SHORT).show();
                    txtMultiplier2.requestFocus();
                    return false;
                } else if (getInputQty() > receivingInfo.getPoQty().subtract(receivingInfo.getReceivedQty()).subtract(receivingInfo.getTempQty()).doubleValue()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cant_greater_than_poqty), Toast.LENGTH_SHORT).show();
                    txtMultiplier2.requestFocus();
                    return false;
                } else if (getInputQty() <= 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cant_greater_than_poqty), Toast.LENGTH_SHORT).show();
                    txtMultiplier2.requestFocus();
                    return false;
                }
            } else {
                if (txtImportQty2.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_qty), Toast.LENGTH_SHORT).show();
                    txtImportQty2.requestFocus();
                    return false;
                } else if ((receivingType == RECEIVING_TYPE.REC_INVOICE || receivingType == RECEIVING_TYPE.TEMP_INVOICE) && getInputQty() > receivingInfo.getPredeliverQty().doubleValue()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cant_greater_than_predeliver), Toast.LENGTH_SHORT).show();
                    txtImportQty2.requestFocus();
                    return false;
                } else if (getInputQty() > receivingInfo.getPoQty().subtract(receivingInfo.getReceivedQty()).subtract(receivingInfo.getTempQty()).doubleValue()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cant_greater_than_poqty), Toast.LENGTH_SHORT).show();
                    txtImportQty2.requestFocus();
                    return false;
                } else if (getInputQty() <= 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cant_greater_than_poqty), Toast.LENGTH_SHORT).show();
                    txtImportQty2.requestFocus();
                    return false;
                }
            }
        }

        if (getInputQty() > receivingInfo.getDeliverableQty().doubleValue()) {
            Toast.makeText(getApplicationContext(), getString(R.string.greater_than_deliverable_qty), Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    private boolean checkItemNoDc() {
        if (txtImportPartNumberDc.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_sku), Toast.LENGTH_LONG).show();
            txtImportPartNumberDc.requestFocus();
            return false;
        } else if (!txtImportPartNumberDc.getText().toString().trim().equals(Util.getSenaoPartNo(receivingInfo.getPartNo()))) {
            Toast.makeText(getApplicationContext(), getString(R.string.sku_incorrect), Toast.LENGTH_SHORT).show();
            txtImportPartNumberDc.requestFocus();
            return false;
        }

        return true;
    }

    private boolean checkDc() {
        if (txtImportDc.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_dc), Toast.LENGTH_LONG).show();
            txtImportDc.requestFocus();
            return false;
        } else if (txtImportDc.getText().toString().trim().length() != 10) {
            Toast.makeText(getApplicationContext(), getString(R.string.dc_len_greater_10), Toast.LENGTH_SHORT).show();
            txtImportDc.requestFocus();
            return false;
        } else if (!Util.isDateCodeValid(txtImportDc.getText().toString().trim())) {
            Toast.makeText(getApplicationContext(), getString(R.string.dc_incorrect), Toast.LENGTH_SHORT).show();
            txtImportDc.requestFocus();
            return false;
        }

        return true;
    }

    private boolean checkItemNo() {
        if (txtImportPartNumber.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_sku), Toast.LENGTH_LONG).show();
            txtImportPartNumber.requestFocus();
            return false;
        } else if (!txtImportPartNumber.getText().toString().trim().equals(Util.getSenaoPartNo(receivingInfo.getPartNo()))) {
            Toast.makeText(getApplicationContext(), getString(R.string.sku_incorrect), Toast.LENGTH_SHORT).show();
            txtImportPartNumber.requestFocus();
            return false;
        }

        return true;
    }

    private boolean checkFields() {
        if (conditionLayout.getVisibility() == View.VISIBLE) {
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
        } else if (dcQrCodeLayout.getVisibility() == View.VISIBLE) {
            if (!checkQrCode()) {
                return false;
            }

            if (!checkReelId()) {
                return false;
            }

            if (!checkQty()) {
                return false;
            }

//            if(Constant.ISORG && parseOrg(st)){//判斷有沒有"@" 有的話解析ORG 比對登入ORG
//                Toast.makeText(getApplicationContext(), "與當前ORG不符!", Toast.LENGTH_SHORT).show();
//                return true;
//            }

            if (txtXboardQtyQrCode.getVisibility() == View.VISIBLE && txtXboardQtyQrCode.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_x_board_qty), Toast.LENGTH_SHORT).show();
                txtXboardQtyQrCode.requestFocus();
                return false;
            }
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            if (!checkReelId()) {
                return false;
            }

            if (!checkQty()) {
                return false;
            }

            if (txtXboardQty.getVisibility() == View.VISIBLE && txtXboardQty.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_x_board_qty), Toast.LENGTH_SHORT).show();
                txtXboardQty.requestFocus();
                return false;
            }
        } else if (dcLayout.getVisibility() == View.VISIBLE) {
            if (!checkItemNoDc()) {
                return false;
            }

            if (!checkDc()) {
                return false;
            }

            if (!checkQty()) {
                return false;
            }

            if (txtXboardQtyDc.getVisibility() == View.VISIBLE && txtXboardQtyDc.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_x_board_qty), Toast.LENGTH_SHORT).show();
                txtXboardQtyDc.requestFocus();
                return false;
            }

            strReelid=txtImportPartNumberDc.getText().toString().trim()+txtVendorInfo.getText().toString().substring(0,6)+txtImportDc.getText().toString();
        } else if (partNoLayout.getVisibility() == View.VISIBLE) {
            if (!checkItemNo()) {
                return false;
            }

            if (!checkQty()) {
                return false;
            }
//            strReelid=txtImportPartNumber.getText().toString().trim()+txtVendorInfo.getText().toString().substring(0,6)+;
        } else if (quantityLayout.getVisibility() == View.VISIBLE) {
            if (txtImportSingleBoxQty.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.Plz_Import_Single_Box_Qty), Toast.LENGTH_SHORT).show();
                txtImportSingleBoxQty.requestFocus();
                return false;
            } else if (txtImportReceiptsQty.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.Plz_Import_Receipts_Qty), Toast.LENGTH_SHORT).show();
                txtImportReceiptsQty.requestFocus();
                return false;
            } else if (getInputQty() > receivingInfo.getPoQty().subtract(receivingInfo.getReceivedQty()).subtract(receivingInfo.getTempQty()).doubleValue()) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_greater_than_poqty), Toast.LENGTH_SHORT).show();
                txtImportReceiptsQty.requestFocus();
                return false;
            } else if ((receivingType == RECEIVING_TYPE.REC_INVOICE || receivingType == RECEIVING_TYPE.TEMP_INVOICE) && getInputQty() > receivingInfo.getPredeliverQty().doubleValue()) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_greater_than_predeliver), Toast.LENGTH_SHORT).show();
                txtImportReceiptsQty.requestFocus();
                return false;
            } else if (getInputQty() > receivingInfo.getDeliverableQty().doubleValue()) {
                Toast.makeText(getApplicationContext(), getString(R.string.greater_than_deliverable_qty), Toast.LENGTH_SHORT).show();
                //return false;
            }
        } else {
            if (txtImportSn.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_sn), Toast.LENGTH_SHORT).show();
                txtImportSn.requestFocus();
                return false;
            }
        }

        return true;
    }

    private boolean checkQrCode() {
        String[] values = txtDcQrCode.getText().toString().trim().split("@");

        if (values.length <= ITEM_LABEL_QR_CODE_FORMAT.values().length) { //20260105 Ann Edit:應該要相等，但部分供應商沒有置換標籤增加COO
        //if (values.length == ITEM_LABEL_QR_CODE_FORMAT.values().length) {
        //if (values.length <= ITEM_LABEL_QR_CODE_FORMAT.values().length) {//暫時讓沒輸入org的部分先通過 2022 1/1之後要拿掉 todo
            for (int i = 0; i < values.length; i++) {
                switch (ITEM_LABEL_QR_CODE_FORMAT.values()[i]) {
                    case REEL_ID:
                        if (values[i].trim().length() != 34) {
                            Toast.makeText(getApplicationContext(), getString(R.string.qrcode_has_error_reelid_len_34), Toast.LENGTH_LONG).show();
                            txtDcQrCode.requestFocus();
                            return false;
                        }
                        break;
                    case PO_QTY_LIST:
                        /*if (values[i].trim().isEmpty()) {
                            Toast.makeText(getApplicationContext(), "QR Code有問題，PO不可為空值",
                                    Toast.LENGTH_LONG).show();
                            txtDcQrCode.requestFocus();
                            return false;
                        }*/
                        break;
                    case QTY:
                        if (values[i].trim().isEmpty()) {
                            Toast.makeText(getApplicationContext(), getString(R.string.qrcode_has_error_qty_is_not_null), Toast.LENGTH_LONG).show();
                            txtDcQrCode.requestFocus();
                            return false;
                        }

                        try {
                            int value = Integer.parseInt(values[i].trim());

                            if (value < 1) {
                                Toast.makeText(getApplicationContext(), getString(R.string.qrcode_has_error_qty_is_greater_than_0), Toast.LENGTH_LONG).show();
                                txtDcQrCode.requestFocus();
                            }
                        } catch (NumberFormatException ex) {
                            Toast.makeText(getApplicationContext(), getString(R.string.qrcode_has_error_qty_is_not_resolved), Toast.LENGTH_LONG).show();
                            txtDcQrCode.requestFocus();
                            return false;
                        }
                        break;
                    case ORG:
                        if(Constant.ISORG){
                            try{
                                int num=Integer.parseInt(values[i]);
                            }catch (NumberFormatException e){
                                org_edtxt.requestFocus();
                                Toast.makeText(getApplicationContext(), getString(R.string.org_must_input_num), Toast.LENGTH_SHORT).show();
                                return false;
                            }

                            if (values[i].trim().isEmpty()) {
                                Toast.makeText(getApplicationContext(), getString(R.string.org_is_not_null),Toast.LENGTH_LONG).show();
                                org_edtxt.requestFocus();
                                return false;
//                                return true;//暫時讓沒輸入org的部分先通過 2022 1/1之後要拿掉 todo
                            } else if(AppController.getOrg()!=Integer.parseInt(values[i])){
                                Toast.makeText(getApplicationContext(), getString(R.string.Does_not_match_current_ORG),Toast.LENGTH_LONG).show();
                                org_edtxt.requestFocus();
                                return false;
                            }
                        }else{

                        }
                        break;
                    case EMPTY_PARAM: //20260105 Ann Edit:為收料作業，QRCODE增加COO
                        break;
                    case COO: //20260105 Ann Edit:為收料作業，QRCODE增加COO
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "ITEM_LABEL_QR_CODE_FORMAT",Toast.LENGTH_LONG).show();
                        return false;
                }

            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.qrcode_parsing_length_mismatch_unable_proceed),Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private double getInputQty() {
        if (receivingInfo.getControl().equals("DC")) {
            if (rbDcQrCode.isChecked()) {
                String value = QrCodeUtil.getValueFromItemLabelQrCode(txtDcQrCode.getText().toString().trim(), ITEM_LABEL_QR_CODE_FORMAT.QTY);
                return value == null ? 0 : Double.parseDouble(value);
            } else if (rbReelID.isChecked()) {
                if (rbQty1.isChecked())
                    return BigDecimal.valueOf(Double.parseDouble((txtMultiplier.getText().toString()
                            .trim()))).multiply(BigDecimal.valueOf(Double.parseDouble(txtMultiplicand.getText().toString()
                            .trim()))).add(BigDecimal.valueOf(Double.parseDouble(txtAddend.getText().toString()
                            .trim()))).doubleValue();
                else
                    return BigDecimal.valueOf(Double.parseDouble(txtImportQty.getText().toString().trim())).doubleValue();
            } else {
                if (rbQty5.isChecked())
                    return BigDecimal.valueOf(Double.parseDouble((txtMultiplier5.getText().toString()
                            .trim()))).multiply(BigDecimal.valueOf(Double.parseDouble(txtMultiplicand5.getText().toString()
                            .trim()))).add(BigDecimal.valueOf(Double.parseDouble(txtAddend5.getText().toString()
                            .trim()))).doubleValue();
                else
                    return BigDecimal.valueOf(Double.parseDouble(txtImportQty8.getText().toString().trim())).doubleValue();
            }
        } else if (receivingInfo.getControl().equals("NO")) {
            if (rbQty3.isChecked())
                return BigDecimal.valueOf(Double.parseDouble((txtMultiplier2.getText().toString()
                        .trim()))).multiply(BigDecimal.valueOf(Double.parseDouble(txtMultiplicand2.getText().toString()
                        .trim()))).add(BigDecimal.valueOf(Double.parseDouble(txtAddend2.getText().toString()
                        .trim()))).doubleValue();
            else
                return BigDecimal.valueOf(Double.parseDouble(txtImportQty2.getText().toString().trim())).doubleValue();
        } else {
            return BigDecimal.valueOf(Double.parseDouble(txtImportReceiptsQty.getText().toString().trim())).doubleValue();
        }
    }

    private void setSummaryData() {
        txtPoQty.setText(receivingInfo.getPoQty() == null ? "0" : String.valueOf(receivingInfo.getPoQty()));
        txtDeliverableQty.setText(receivingInfo.getDeliverableQty() == null ? "0" : String.valueOf(receivingInfo.getDeliverableQty()));
        txtPredeliverQty.setText(receivingInfo.getPredeliverQty() == null ? "0" : String.valueOf(receivingInfo.getPredeliverQty()));
        txtReceivedQty.setText(String.valueOf(receivingInfo.getReceivedQty() == null ? "0" : receivingInfo.getReceivedQty()));
        txtUnreceivedQty.setText(String.valueOf(receivingInfo.getUnreceivedQty() == null ? "0" : receivingInfo.getUnreceivedQty()));
        txtTempQty.setText(String.valueOf(receivingInfo.getTempQty() == null ? "0" : receivingInfo.getTempQty()));
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

    protected void cleanData() {
        if (conditionLayout.getVisibility() == View.VISIBLE) {
            cbGenInvoiceNo.setChecked(false);
            txtImportInvoiceNo.setText("");
            txtImportLocator.setText("");
            txtImportInvoiceNo.requestFocus();
        } else if (dcQrCodeLayout.getVisibility() == View.VISIBLE) {
            txtDcQrCode.setText("");
            txtXboardQtyQrCode.setText("");
            txtDcQrCode.requestFocus();
            txtFromSenao.setText("N"); //20251113 Ann Edit:還原預設值
            txtFromSenao.setTextColor(Color.BLACK); //20251113 Ann Edit:還原預設值
            txtDcQrCodeOriginal.setText(""); //20251113 Ann Edit:還原預設值
            dcQrCodeOriginalLayout.setVisibility(View.INVISIBLE); //20251113 Ann Edit:還原預設值
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            txtImportReelId.setText("");
            //rbQty1.setChecked(true);
            rbQty2.setChecked(true);
            txtMultiplier.setText("");
            txtMultiplicand.setText("");
            txtAddend.setText("");
            txtImportQty.setText("");
            txtXboardQty.setText("");
            txtImportReelId.requestFocus();
        } else if (dcLayout.getVisibility() == View.VISIBLE) {
            txtImportPartNumberDc.setText("");
            txtImportDc.setText("");
            //rbQty5.setChecked(true);
            rbQty8.setChecked(true);
            txtMultiplier5.setText("");
            txtMultiplicand5.setText("");
            txtAddend5.setText("");
            txtImportQty8.setText("");
            txtXboardQtyDc.setText("");
            txtImportPartNumberDc.requestFocus();
        } else if (partNoLayout.getVisibility() == View.VISIBLE) {
            txtImportPartNumber.setText("");
            //rbQty3.setChecked(true);
            rbQty4.setChecked(true);
            txtMultiplier2.setText("");
            txtMultiplicand2.setText("");
            txtAddend2.setText("");
            txtImportQty2.setText("");
            txtXboardQty.setText("");
            txtImportPartNumber.requestFocus();
        } else if (quantityLayout.getVisibility() == View.VISIBLE) {
            txtImportReceiptsQty.setText("");
        } else {
            txtImportSn.setText("");
        }
    }

    private void setView(boolean isForward) {
        if (isForward) {
            if (receivingType == RECEIVING_TYPE.REC_PO || receivingType == RECEIVING_TYPE.TEMP_PO) {
                labelPo.setVisibility(View.VISIBLE);
                txtPo.setVisibility(View.VISIBLE);
            } else {
                labelPo.setVisibility(View.INVISIBLE);
                txtPo.setVisibility(View.INVISIBLE);
            }

            if (receivingInfo.getControl().equals("DC")) {
                if (conditionLayout.getVisibility() == View.VISIBLE) {
                    conditionLayout.setVisibility(View.GONE);
                    rgDC.setVisibility(View.VISIBLE);
                    rgDC.check(R.id.radio_dc_qrcode);
                    txtInvoiceNo.setText(txtImportInvoiceNo.getText().toString());
                } else if (rgDC.getVisibility() == View.VISIBLE) {
                    if (isLagerThanDeliverableQty()) {
                        showOverRecvDialog();
                    } else {
                        if (rbDcQrCode.isChecked()) {
                            doCheckReelID();
                        } else if (rbReelID.isChecked()) {
                            doCheckReelID();
                        } else {
                            doCheckDateCode();
                        }
                    }
                } else {
                    if (receivingType == RECEIVING_TYPE.REC_COMBINE || receivingType == RECEIVING_TYPE.TEMP_COMBINE
                            || receivingType == RECEIVING_TYPE.REC_PO || receivingType == RECEIVING_TYPE.TEMP_PO) {
                        conditionLayout.setVisibility(View.GONE);
                        rgDC.setVisibility(View.VISIBLE);
                        rgDC.check(R.id.radio_dc_qrcode);
                    } else {
                        conditionLayout.setVisibility(View.VISIBLE);
                        rgDC.setVisibility(View.GONE);
                        dcQrCodeLayout.setVisibility(View.GONE);
                        reelIdLayout.setVisibility(View.GONE);
                        dcLayout.setVisibility(View.GONE);
                        txtImportInvoiceNo.setText(receivingInfo.getInvoiceNo());
                        txtImportInvoiceNo.selectAll();
                    }
                }
            } else if (receivingInfo.getControl().equals("NO")) {
                if (conditionLayout.getVisibility() == View.VISIBLE) {
                    conditionLayout.setVisibility(View.GONE);
                    partNoLayout.setVisibility(View.VISIBLE);
                    //rbQty3.setChecked(true);
                    rbQty4.setChecked(true);
                    txtInvoiceNo.setText(txtImportInvoiceNo.getText().toString());
                    txtImportPartNumber.requestFocus();
                } else if (partNoLayout.getVisibility() == View.VISIBLE) {
                    if (isLagerThanDeliverableQty()) {
                        showOverRecvDialog();
                    } else {
                        doCheckProcess(false);
                    }
                } else {
                    if (receivingType == RECEIVING_TYPE.REC_COMBINE || receivingType == RECEIVING_TYPE.TEMP_COMBINE
                            || receivingType == RECEIVING_TYPE.REC_PO || receivingType == RECEIVING_TYPE.TEMP_PO) {
                        conditionLayout.setVisibility(View.GONE);
                        partNoLayout.setVisibility(View.VISIBLE);
                        //rbQty3.setChecked(true);
                        rbQty4.setChecked(true);
                        txtInvoiceNo.setText(txtImportInvoiceNo.getText().toString());
                        txtImportPartNumber.requestFocus();
                    } else {
                        conditionLayout.setVisibility(View.VISIBLE);
                        partNoLayout.setVisibility(View.GONE);
                        txtImportInvoiceNo.setText(receivingInfo.getInvoiceNo());
                        txtImportInvoiceNo.selectAll();
                    }
                }
            } else {
                if (conditionLayout.getVisibility() == View.VISIBLE) {
                    conditionLayout.setVisibility(View.GONE);
                    quantityLayout.setVisibility(View.VISIBLE);
                    serialNoLayout.setVisibility(View.GONE);
                    txtInvoiceNo.setText(txtImportInvoiceNo.getText().toString());
                    txtImportSingleBoxQty.requestFocus();
                } else if (quantityLayout.getVisibility() == View.VISIBLE) {
                    conditionLayout.setVisibility(View.GONE);
                    quantityLayout.setVisibility(View.GONE);
                    serialNoLayout.setVisibility(View.VISIBLE);
                    setPercentageView(0);
                    txtImportSn.requestFocus();
                } else if (serialNoLayout.getVisibility() == View.VISIBLE) {
                    if (isLagerThanDeliverableQty()) {
                        showOverRecvDialog();
                    } else {
                        doCheckProcess(false);
                    }
                } else {
                    if (receivingType == RECEIVING_TYPE.REC_COMBINE || receivingType == RECEIVING_TYPE.TEMP_COMBINE
                            || receivingType == RECEIVING_TYPE.REC_PO || receivingType == RECEIVING_TYPE.TEMP_PO) {
                        conditionLayout.setVisibility(View.GONE);
                        quantityLayout.setVisibility(View.VISIBLE);
                        serialNoLayout.setVisibility(View.GONE);
                        txtInvoiceNo.setText(txtImportInvoiceNo.getText().toString());
                        txtImportSingleBoxQty.requestFocus();
                    } else {
                        conditionLayout.setVisibility(View.VISIBLE);
                        txtImportInvoiceNo.setText(receivingInfo.getInvoiceNo());
                        txtImportInvoiceNo.selectAll();
                        quantityLayout.setVisibility(View.GONE);
                        serialNoLayout.setVisibility(View.GONE);
                    }
                }
            }
        } else {
            cleanData();

            if (receivingInfo.getControl().equals("DC")) {
                if (conditionLayout.getVisibility() == View.VISIBLE) {
                    if (needRefresh)
                        setResult(RESULT_OK);

                    finish();
                } else {
                    if (receivingType == RECEIVING_TYPE.REC_COMBINE || receivingType == RECEIVING_TYPE.TEMP_COMBINE
                            || receivingType == RECEIVING_TYPE.REC_PO || receivingType == RECEIVING_TYPE.TEMP_PO) {
                        if (needRefresh)
                            setResult(RESULT_OK);

                        finish();
                    } else {
                        lblRecId.setVisibility(View.GONE);
                        conditionLayout.setVisibility(View.VISIBLE);

                        if (receivingType == RECEIVING_TYPE.REC_COMBINE || receivingType == RECEIVING_TYPE.TEMP_COMBINE
                                || receivingType == RECEIVING_TYPE.REC_PO || receivingType == RECEIVING_TYPE.TEMP_PO)
                            txtInvoiceNo.setText("");
                        rgDC.clearCheck();
                        rgDC.setVisibility(View.GONE);
                        dcQrCodeLayout.setVisibility(View.GONE);
                        reelIdLayout.setVisibility(View.GONE);
                        dcLayout.setVisibility(View.GONE);
                    }
                }
            } else if (receivingInfo.getControl().equals("NO")) {
                if (conditionLayout.getVisibility() == View.VISIBLE) {
                    if (needRefresh)
                        setResult(RESULT_OK);

                    finish();
                } else {
                    if (receivingType == RECEIVING_TYPE.REC_COMBINE || receivingType == RECEIVING_TYPE.TEMP_COMBINE
                            || receivingType == RECEIVING_TYPE.REC_PO || receivingType == RECEIVING_TYPE.TEMP_PO) {
                        if (needRefresh)
                            setResult(RESULT_OK);

                        finish();
                    } else {
                        lblRecId.setVisibility(View.GONE);
                        conditionLayout.setVisibility(View.VISIBLE);
                        if (receivingType == RECEIVING_TYPE.REC_COMBINE || receivingType == RECEIVING_TYPE.TEMP_COMBINE
                                || receivingType == RECEIVING_TYPE.REC_PO || receivingType == RECEIVING_TYPE.TEMP_PO)
                            txtInvoiceNo.setText("");

                        partNoLayout.setVisibility(View.GONE);
                    }
                }
            } else {
                if (conditionLayout.getVisibility() == View.VISIBLE) {
                    if (needRefresh)
                        setResult(RESULT_OK);

                    finish();
                } else if (quantityLayout.getVisibility() == View.VISIBLE) {
                    if (receivingType == RECEIVING_TYPE.REC_COMBINE || receivingType == RECEIVING_TYPE.TEMP_COMBINE
                            || receivingType == RECEIVING_TYPE.REC_PO || receivingType == RECEIVING_TYPE.TEMP_PO) {
                        if (needRefresh)
                            setResult(RESULT_OK);

                        finish();
                    } else {
                        lblRecId.setVisibility(View.GONE);
                        conditionLayout.setVisibility(View.VISIBLE);
                        if (receivingType == RECEIVING_TYPE.REC_COMBINE || receivingType == RECEIVING_TYPE.TEMP_COMBINE
                                || receivingType == RECEIVING_TYPE.REC_PO || receivingType == RECEIVING_TYPE.TEMP_PO)
                            txtInvoiceNo.setText("");

                        quantityLayout.setVisibility(View.GONE);
                        serialNoLayout.setVisibility(View.GONE);
                    }
                } else {
                    conditionLayout.setVisibility(View.GONE);
                    quantityLayout.setVisibility(View.VISIBLE);
                    serialNoLayout.setVisibility(View.GONE);
                }
            }
        }
    }

    private void showOverRecvDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.warn);
        dialog.setMessage(R.string.greater_than_deliverable_qty_Overcharge);
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setCancelable(true);
        dialog.setPositiveButton(getString(R.string.yes),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int arg1) {
                        if (receivingInfo.getControl().equals("DC")) {
                            if (rbDcQrCode.isChecked()) {
                                doCheckReelID();
                            } else if (rbReelID.isChecked()) {
                                doCheckReelID();
                            } else {
                                doCheckDateCode();
                            }
                        } else {
                            doCheckProcess(false);
                        }

                        dialog.dismiss();
                    }

                });

        dialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog,
                                int arg1) {
                dialog.dismiss();
            }

        });

        dialog.show();
    }

    private void setPercentageView(double quantity) {
        txtPercentage.setText(String.format("%s/%s", Util.fmt(quantity), txtImportReceiptsQty.getText().toString()));
    }

    private boolean isLagerThanDeliverableQty() {
        if (getInputQty() > receivingInfo.getDeliverableQty().doubleValue())
            return true;
        else
            return false;
    }

    private void returnPage() {
        cleanData();
        setView(false);
    }

    public void onBackPressed() {
        Log.d(TAG, "onClick Hardware Back Button");
        returnPage();
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

    private class DoCheckProcess extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Do check receiving process from "
                    + AppController.getServerInfo()
                    + AppController.getProperties("DoCheckProcess"));
            publishProgress(getString(R.string.Receiving_processing));
            return receivingHandler.doCheckProcess(receivingInfo);
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
        protected void onPostExecute(BasicHelper result) {
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK || result.getIntRetCode() == ReturnCode.SYSTEM_INFO) {
                    receivingInfo = (ReceivingInfoHelper) result;
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    needRefresh = true;

                    if (result.getIntRetCode() == ReturnCode.SYSTEM_INFO) {
                        Toast.makeText(getApplicationContext(), getString(R.string.warn)+":" + result.getStrErrorBuf(), Toast.LENGTH_LONG).show();
                    }

                    double inputQty = getInputQty();

                    if (receivingInfo.getControl().equals("SN")) {
                        setSummaryData();

                        if (receivingInfo.getSnCheckQty().doubleValue() < inputQty) {
                            setPercentageView(receivingInfo.getSnCheckQty().doubleValue());
                            cleanData();
                        } else if (receivingInfo.getSnCheckQty().doubleValue() == inputQty
                                && inputQty < receivingInfo.getDeliverableQty().subtract(receivingInfo.getReceivedQty()).subtract(receivingInfo.getTempQty()).doubleValue()) {
                            setView(false);
                        } else {
                            setResult(RESULT_OK);
                            finish();
                        }
                    } else {
                        if (receivingType == RECEIVING_TYPE.REC_INVOICE || receivingType == RECEIVING_TYPE.TEMP_INVOICE) {
                            if (receivingInfo.getPredeliverQty().doubleValue() <= 0) {
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                setSummaryData();
                                cleanData();
                            }
                        } else {
                            if (receivingInfo.getDeliverableQty().doubleValue() > 0) {
                                setSummaryData();
                                cleanData();
                            } else {
                                setResult(RESULT_OK);
                                finish();
                            }
                        }
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

    private class DoCheckReelID extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Do check reelidfrom "
                    + AppController.getServerInfo()
                    + AppController.getProperties("DoCheckReelID"));
            publishProgress("檢查ReelID中...");
            return receivingHandler.doCheckReelID(receivingInfo);
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
        protected void onPostExecute(BasicHelper result) {
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    receivingInfo = (ReceivingInfoHelper) result;
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
//                    if ((receivingType == RECEIVING_TYPE.TEMP_INVOICE
//                            || receivingType == RECEIVING_TYPE.REC_INVOICE)
//                            && (!TextUtils.isEmpty(receivingInfo.getSupReelID())
//                            && !receivingInfo.getSupReelID().equals(receivingInfo.getReelID()))) {
//                        showUpdateReelIdDialog();
//                    } else {
//                        doCheckProcess(false);
//                    }
                    doCheckProcess(false);
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else if (result.getIntRetCode() == ReturnCode.SYSTEM_INFO) {
                        mConnection.setText(R.string.server_return_warn);
                        Toast.makeText(getApplicationContext(), result.getStrErrorBuf(), Toast.LENGTH_LONG).show();
//                        if ((receivingType == RECEIVING_TYPE.TEMP_INVOICE
//                                || receivingType == RECEIVING_TYPE.REC_INVOICE)
//                                && (!TextUtils.isEmpty(receivingInfo.getSupReelID())
//                                && !receivingInfo.getSupReelID().equals(receivingInfo.getReelID()))) {
//                            showUpdateReelIdDialog();
//                        } else {
//                            doCheckProcess(false);
//                        }
                        doCheckProcess(false);
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

    private class DoCheckDateCode extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Do check DateCode from "
                    + AppController.getServerInfo()
                    + AppController.getProperties("DoCheckDateCode"));
            publishProgress("檢查Datecode中...");
            return receivingHandler.doCheckDateCode(receivingInfo);
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
        protected void onPostExecute(BasicHelper result) {
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    receivingInfo = (ReceivingInfoHelper) result;
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    doCheckProcess(false);
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else if (result.getIntRetCode() == ReturnCode.SYSTEM_INFO) {
                        mConnection.setText(R.string.server_return_warn);
                        Toast.makeText(getApplicationContext(), result.getStrErrorBuf(), Toast.LENGTH_LONG).show();
                        doCheckProcess(false);
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

    private class DoCheckInvoiceNo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Do check invoice no "
                    + AppController.getServerInfo()
                    + AppController.getProperties("DoCheckInvoiceNo"));
            publishProgress(getString(R.string.Invoice_checking));
            return receivingHandler.doCheckInvoiceNo(receivingInfo);
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
        protected void onPostExecute(BasicHelper result) {
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    receivingInfo = (ReceivingInfoHelper) result;
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    setView(true);
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else if (result.getIntRetCode() == ReturnCode.SYSTEM_INFO) {
                        mConnection.setText(R.string.server_return_warn);
                        Toast.makeText(getApplicationContext(), result.getStrErrorBuf(), Toast.LENGTH_LONG).show();
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

    private class UpdateInvoiceNo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Update Invoice No "
                    + AppController.getServerInfo()
                    + AppController.getProperties("UpdateInvoiceNo"));
            publishProgress("更新發票號碼中...");
            return receivingHandler.updateInvoiceNo(receivingInfo);
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
        protected void onPostExecute(BasicHelper result) {
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    receivingInfo = (ReceivingInfoHelper) result;
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    doQuerySeq();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else if (result.getIntRetCode() == ReturnCode.SYSTEM_INFO) {
                        mConnection.setText(R.string.server_return_warn);
                        Toast.makeText(getApplicationContext(), result.getStrErrorBuf(), Toast.LENGTH_LONG).show();
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
                    lblRecId.setText(getString(R.string.label_rec_id, invoiceHelper.getSeq()));
                    lblRecId.setVisibility(View.VISIBLE);
                    setView(true);
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

    private class GetEIQCInspectionStatus extends AsyncTask<Integer, String, String> {
        @Override
        protected String doInBackground(Integer... params) {
            AppController.debug("GetEIQCInspectionStatus:"
                    //+ AppController.getServerInfo()
                    //+ AppController.getProperties("GetEIQCInspectionStatus"); //20260213 Ann Mark
                    + AppController.getSfcIp() + "/invoke" + AppController.getApi_3() + "?sCode=" + "eIQC_INSPECTION_PLAN__STATUS"); //20260213 Ann Add
            publishProgress(getString(R.string.processing));
            String result = "";
            // 創建 HttpClient 對象
            HttpClient httpClient = new DefaultHttpClient();
            httpClient.getConnectionManager().getSchemeRegistry().register(
                    new Scheme("https", SSLSocketFactory.getSocketFactory(), 443)
            );

            String jason =new Gson().toJson(new EIQCInspectionStatusHelper(strReelid,String.valueOf(AppController.getOrg()),"response"));
            AppController.debug("jason: "+ jason);

            // 創建 HttpClient 對象
            try {
                // 創建 HttpPost 對象並設置 URL
                //HttpPost httpPost = new HttpPost(AppController.getProperties("GetEIQCInspectionStatus"));
                HttpPost httpPost = new HttpPost(AppController.getSfcIp() + "/invoke" + AppController.getApi_3() + "?sCode=" + "eIQC_INSPECTION_PLAN__STATUS");
                // 設置請求的 JSON 數據
                StringEntity stringEntity = new StringEntity(jason);
                httpPost.setEntity(stringEntity);
                // 設置 Content-Type 頭部
                httpPost.addHeader("Accept","application/json; charset=UTF-8");
                httpPost.addHeader("Authorization","Bearer " + LoginActivity.apiToken);
                httpPost.addHeader("Content-Type","application/json; charset=UTF-8");
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
                AppController.debug("GetEIQCInspectionStatus: "+ result);
            } catch (Exception e) {
                e.printStackTrace();
                AppController.debug("GetEIQCInspectionStatus: "+ e.getMessage());
                return e.getMessage();
            }

            httpClient.getConnectionManager().shutdown();
            return result;
        }

        @Override
        protected void onProgressUpdate(String... text) {
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        @Override
        protected void onPostExecute(String result) {
            AppController.debug("GetEIQCInspectionStatus() result json = "  + new Gson().toJson(result));
            isInspect = null;

            if (result != null  ) {
                try {
                    JSONArray jsonArray = new JSONArray(result);

                    if (jsonArray.length() > 0) {
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        isInspect = jsonObject.getString("RSTATUS");
                    } else {
                        AppController.debug("isInspect No data available.");
                    }
                } catch (JSONException e) {
                    AppController.debug("isInspect = "  + e.toString());
                    e.printStackTrace();
                }

                AppController.debug("isInspect = "  + isInspect);
                AppController.debug("免檢.equals(isInspect)="  + ("免檢".equals(isInspect)));
//                                AppController.debug("isInspect = "  + isInspect);

                if("免檢".equals(isInspect)){
                    if (!BtPrintLabel.printEIQCInspect(isInspect,strReelid)) {
                        Toast.makeText(getApplicationContext(), getString(R.string.Label_printing_error_occurred), Toast.LENGTH_LONG).show();
                    }
                }

//                AlertDialog.Builder dialog = new AlertDialog.Builder(
//                        MaterialReceivingProcessActivity.this);
//                dialog.setTitle("Note");
//                dialog.setMessage("此料為:"+isInspect+",是否繼續?");
//                dialog.setIcon(android.R.drawable.ic_dialog_alert);
//                dialog.setCancelable(false);
//                dialog.setNeutralButton(getString(R.string.btn_print), new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        if (!BtPrintLabel.printEIQCInspect(isInspect,strReelid)) {
//                            Toast.makeText(getApplicationContext(), "列印標籤出現問題，請重新列印", Toast.LENGTH_LONG).show();
//
//                        }
//
//
//                    }
//                });
//
//                dialog.setPositiveButton( getString(R.string.btn_ok),
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int arg1) {
//                                if (conditionLayout.getVisibility() == View.VISIBLE) {
//                                    if (receivingType == RECEIVING_TYPE.REC_COMBINE || receivingType == RECEIVING_TYPE.TEMP_COMBINE) {
//                                        AppController.debug("收料作業=>doCheckInvoiceNo()");
//                                        doCheckInvoiceNo();
//                                        return;
//                                    } else {
//                                        if (receivingInfo.getInvoiceNo() == null || !receivingInfo.getInvoiceNo().equals(txtImportInvoiceNo.getText().toString().trim())) {
//                                            AppController.debug("收料作業=>updateInvoiceNo()");
//                                            updateInvoiceNo();
//                                            return;
//                                        }
//                                        if (vendorInfo.getIsRemittance() != null && vendorInfo.getIsRemittance().equals("N")) {
//                                            AppController.debug("收料作業=>doQuerySeq()");
//                                            doQuerySeq();
//                                            return;
//                                        }
//                                    }
//                                }
//                                setView(true);
//                                dialog.dismiss();
//                            }
//                        });
//                dialog.setNegativeButton( getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int arg1) {
//                                dialog.dismiss();
//                            }
//                        });
//                dialog.setCancelable(false);
//                dialog.show();

                final AlertDialog dialog1 = new AlertDialog.Builder(MaterialReceivingProcessActivity.this).setTitle("Note")
                .setMessage(getString(R.string.This_material_is) + isInspect + getString(R.string.would_you_like_to_continue))
                .setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (conditionLayout.getVisibility() == View.VISIBLE) {
                                    if (receivingType == RECEIVING_TYPE.REC_COMBINE || receivingType == RECEIVING_TYPE.TEMP_COMBINE) {
                                        AppController.debug("收料作業=>doCheckInvoiceNo()");
                                        doCheckInvoiceNo();
                                        return;
                                    } else {
                                        if (receivingInfo.getInvoiceNo() == null || !receivingInfo.getInvoiceNo().equals(txtImportInvoiceNo.getText().toString().trim())) {
                                            AppController.debug("收料作業=>updateInvoiceNo()");
                                            updateInvoiceNo();
                                            return;
                                        }
                                        if (vendorInfo.getIsRemittance() != null && vendorInfo.getIsRemittance().equals("N")) {
                                            AppController.debug("收料作業=>doQuerySeq()");
                                            doQuerySeq();
                                            return;
                                        }
                                    }
                                }

                                setView(true);
                            }
                        })
                        .setNeutralButton(getString(R.string.btn_print),null)
                        .setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();

                dialog1.show();

                dialog1.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                         if (!BtPrintLabel.printEIQCInspect(isInspect,strReelid)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.Label_printing_error_occurred), Toast.LENGTH_LONG).show();

                        }

                       return;
                    }
                });

                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
            } else {
                mConnection.setText("GetEIQCInspectionStatus"+getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "GetEIQCInspectionStatus ERROR";
            }
        }
    }

    /* 20251113 Ann Edit:因應神準進料後轉賣給越南神準不換標
     *                  先取得當前Org與QR Code不同時，需要Pass的Org有哪些
     */
    private void fetch_SSFTP_Async(final String API_Name, final String strLOOKUP_CATALOG, final String strLOOKUP_CODE) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                // 呼叫 API
                return getRECEIVING_ORG_FromAPI(API_Name, strLOOKUP_CATALOG, strLOOKUP_CODE);
            }

            @Override
            protected void onPostExecute(String result) {
                // 解析 API 回傳的 JSON
                try {
                    JSONArray arr = new JSONArray(result);
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);

                        if (obj.has("LOOKUP_VALUE")) {
                            if (sb.length() > 0)
                                sb.append("@@");

                            sb.append(obj.getString("LOOKUP_VALUE"));
                        }
                    }

                    // 存入 AppController 全域變數
                    AppController.setReceivingOrg(sb.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }.execute();
    }

    /* 20251113 Ann Edit:因應神準進料後轉賣給越南神準不換標
     *                  先取得當前Org與QR Code不同時，需要Pass的Org有哪些
     */
    private String getRECEIVING_ORG_FromAPI(String API_Name, String strLOOKUP_CATALOG, String strLOOKUP_CODE) {
        String apiUrl = AppController.getSfcIp() + "/invoke" + AppController.getApi_2() + "?sCode=" + API_Name; //取得SSFTP的值
        String result = "";

        HttpClient httpClient = new DefaultHttpClient();
        httpClient.getConnectionManager().getSchemeRegistry().register(
                new Scheme("https", SSLSocketFactory.getSocketFactory(), 443)
        );

        try {
            JSONObject reqJson = new JSONObject();
            reqJson.put("LOOKUP_CATALOG", strLOOKUP_CATALOG);
            reqJson.put("LOOKUP_CODE", strLOOKUP_CODE);

            HttpPost httpPost = new HttpPost(apiUrl);

            StringEntity stringEntity = new StringEntity(reqJson.toString(), "UTF-8");
            httpPost.setEntity(stringEntity);

            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Authorization","Bearer "+LoginActivity.apiToken);
            httpPost.addHeader("Content-Type", "application/json; charset=utf-8");

            HttpResponse response = httpClient.execute(httpPost);

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity, "UTF-8");
                AppController.debug("getRECEIVING_ORG_FromAPI response = " + result);
            }

        } catch (Exception e) {
            e.printStackTrace();
            AppController.debug("getRECEIVING_ORG_FromAPI Exception:: " + e.getMessage());
            return "[]"; // 最安全格式
        } finally {
            httpClient.getConnectionManager().shutdown();
        }

        return result;
    }

}
