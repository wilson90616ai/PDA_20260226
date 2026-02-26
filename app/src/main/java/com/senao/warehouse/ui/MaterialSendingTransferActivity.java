package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.ITEM_LABEL_QR_CODE_FORMAT;
import com.senao.warehouse.database.PnItemInfoHelper;
import com.senao.warehouse.database.SendOnHandQtyInfo;
import com.senao.warehouse.database.SendProcessInfoHelper;
import com.senao.warehouse.database.SendSubinventoryInfo;
import com.senao.warehouse.database.SendingInfoHelper;
import com.senao.warehouse.database.SubinventoryInfoHelper;
import com.senao.warehouse.handler.PrintLabelHandler;
import com.senao.warehouse.handler.SendingHandler;
import com.senao.warehouse.handler.StockInHandler;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.QrCodeUtil;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.util.Util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MaterialSendingTransferActivity extends Activity {

    static final Collection<String> EXCLUDE_SUBINVENTORY;
    private static final String TAG = MaterialSendingTransferActivity.class.getSimpleName();

    //排除310,318倉
    static {
        EXCLUDE_SUBINVENTORY = new ArrayList<>(2);
        EXCLUDE_SUBINVENTORY.add("304");
        EXCLUDE_SUBINVENTORY.add("320");
        EXCLUDE_SUBINVENTORY.add("Stage");
        EXCLUDE_SUBINVENTORY.add("310");
        EXCLUDE_SUBINVENTORY.add("318");
        EXCLUDE_SUBINVENTORY.add("322");
        EXCLUDE_SUBINVENTORY.add("326");
    }

    private TextView mConnection;
    private Button btnReturn, btnConfim, btnCancel;
    private ProgressDialog dialog;
    private String errorInfo = "";
    private TextView txtPartNo;
    private LinearLayout lPalletLayout, lNoLayout, lReelIDLayout, lStockLayout, lPNLayout, lBoxLayout,
            lSNLayout, lDcQrCodeLayout;
    private RadioGroup rgDC, rgSN;
    private RadioButton rbQrCode, rbReelID, rbPartNo, rbPalletSN, rbBoxSN, rbPalletCN,
            rbBoxCN;
    private EditText txtSubinventoryFrom, txtReelID, txtPalletCN, txtBoxCN,
            txtSnSN, txtBoxSN, txtPalletSN, txtNoPN, txtNoAddend,
            txtNoMultiplier, txtNoMultiplicand, txtReelIdMultiplicand, txtDcQrCode,
            txtReelIdAddend, txtReelIdMultiplier, txtLocatorFrom, txtSubinventoryTo, txtLocatorTo,
            txtPnPN, txtPnDC, txtPnMultiplier, txtPnMultiplicand, txtPnAddend;
    private LayoutType mode = LayoutType.NONE;
    private SendProcessInfoHelper processInfoHelper;
    private boolean needRefresh = false;
    private RadioButton rbPnEquation;
    private RadioButton rbPnQty;
    private EditText txtPnQty;
    private RadioButton rbReelIdEquation;
    private RadioButton rbReelIdQty;
    private EditText txtReelIdQty;
    private RadioButton rbNoEquation;
    private RadioButton rbNoQty;
    private EditText txtNoQty;
    private TextView txtMergeWoNumber;
    private TextView txtLineNumber;
    private TextView txtTotalSendQty;
    private TextView txtSendQty;
    private TextView txtUnsendQty;
    private TextView labelMergeWoNumber;
    private SendingHandler sendingHandler;
    private SendOnHandQtyInfo onHandQtyInfo;
    private SubinventoryInfoHelper subinventoryHelper;
    private StockInHandler stockInHandler;
    private LinearLayout lTransferQty;
    private LinearLayout lTransferNo;
    private TextView txtTransferQty;
    private TextView txtTransferNo;
    private BigDecimal totalTransferQty = BigDecimal.valueOf(0);
    private PnItemInfoHelper pnInfo;
    private PrintLabelHandler print;
    private Button btnDebit;
    private SendingInfoHelper sendInfo;
    private boolean needDeibt;

    private OnKeyListener keyListener = new OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Perform action on key press
                Log.d(TAG, "onKeyListener Enter or Action Down" + String.valueOf(v.getId()));
                int id = v.getId();

                if (id == R.id.edittext_import_pn_pn) {
                    txtPnDC.selectAll();
                } else if (id == R.id.edittext_import_subinventory_from) {
                    txtLocatorFrom.selectAll();
                } else {
                    hideKeyboard();
                }
            }

            return false;
        }

    };

    private CompoundButton.OnCheckedChangeListener noEquationListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbNoQty.setChecked(false);
                txtNoMultiplier.setEnabled(true);
                txtNoMultiplicand.setEnabled(true);
                txtNoAddend.setEnabled(true);
                txtNoMultiplier.requestFocus();
                txtNoQty.setText("");
                txtNoQty.setEnabled(false);
                txtNoPN.setNextFocusDownId(R.id.edittext_no_multiplier);
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener noQtyListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbNoEquation.setChecked(false);
                txtNoMultiplier.setText("");
                txtNoMultiplicand.setText("");
                txtNoAddend.setText("");
                txtNoMultiplier.setEnabled(false);
                txtNoMultiplicand.setEnabled(false);
                txtNoAddend.setEnabled(false);
                txtNoQty.setEnabled(true);
                txtNoQty.requestFocus();
                txtNoPN.setNextFocusDownId(R.id.edittext_no_qty);
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener reelIdEquationListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbReelIdQty.setChecked(false);
                txtReelIdMultiplier.setEnabled(true);
                txtReelIdMultiplicand.setEnabled(true);
                txtReelIdAddend.setEnabled(true);
                txtReelIdMultiplier.requestFocus();
                txtReelIdQty.setText("");
                txtReelIdQty.setEnabled(false);
                txtReelID.setNextFocusDownId(R.id.edittext_reelid_multiplier);
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener reelIdQtyListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbReelIdEquation.setChecked(false);
                txtReelIdMultiplier.setText("");
                txtReelIdMultiplicand.setText("");
                txtReelIdAddend.setText("");
                txtReelIdMultiplier.setEnabled(false);
                txtReelIdMultiplicand.setEnabled(false);
                txtReelIdAddend.setEnabled(false);
                txtReelIdQty.setEnabled(true);
                txtReelIdQty.requestFocus();
                txtReelID.setNextFocusDownId(R.id.edittext_reelid_qty);
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener pnEquationListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbPnQty.setChecked(false);
                txtPnMultiplier.setEnabled(true);
                txtPnMultiplicand.setEnabled(true);
                txtPnAddend.setEnabled(true);
                txtPnMultiplier.requestFocus();
                txtPnQty.setText("");
                txtPnQty.setEnabled(false);
                txtPnDC.setNextFocusDownId(R.id.edittext_pn_multiplier);
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener pnQtyListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbPnEquation.setChecked(false);
                txtPnMultiplier.setText("");
                txtPnMultiplicand.setText("");
                txtPnAddend.setText("");
                txtPnMultiplier.setEnabled(false);
                txtPnMultiplicand.setEnabled(false);
                txtPnAddend.setEnabled(false);
                txtPnQty.setEnabled(true);
                txtPnQty.requestFocus();
                txtPnDC.setNextFocusDownId(R.id.edittext_pn_qty);
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener boxSnListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbBoxCN.setChecked(false);
                txtBoxSN.setEnabled(true);
                txtBoxCN.setText("");
                txtBoxCN.setEnabled(false);
                txtBoxSN.requestFocus();
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener boxCnListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbBoxSN.setChecked(false);
                txtBoxSN.setText("");
                txtBoxSN.setEnabled(false);
                txtBoxCN.setEnabled(true);
                txtBoxCN.requestFocus();
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener palletSnListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbPalletCN.setChecked(false);
                txtPalletSN.setEnabled(true);
                txtPalletCN.setText("");
                txtPalletCN.setEnabled(false);
                txtPalletSN.requestFocus();
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener palletCnListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbPalletSN.setChecked(false);
                txtPalletSN.setText("");
                txtPalletSN.setEnabled(false);
                txtPalletCN.setEnabled(true);
                txtPalletCN.requestFocus();
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener snListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio_pallet:
                    Log.d("onClick Radio Pallet", String.valueOf(checkedId));
                    showLayout(LayoutType.SN_PALLET);
                    break;
                case R.id.radio_box:
                    Log.d("onClick Box", String.valueOf(checkedId));
                    showLayout(LayoutType.SN_BOX);
                    break;
                case R.id.radio_sn:
                    Log.d("onClick Serial Number", String.valueOf(checkedId));
                    showLayout(LayoutType.SN_SN);
                    break;
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener dcListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio_dc_qrcode:
                    if (rbQrCode.isChecked())
                        showLayout(LayoutType.DC_QR_CODE);
                    break;
                case R.id.radio_reel_id:
                    if (rbReelID.isChecked())
                        showLayout(LayoutType.DC_REEL_ID);
                    break;
                case R.id.radio_part_number:
                    if (rbPartNo.isChecked()) {
                        showLayout(LayoutType.DC_PN);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_sending_transfer);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            sendInfo = new Gson().fromJson(extras.getString("CONDITION_INFO"), SendingInfoHelper.class);
            if (sendInfo == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_shipping_info), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            processInfoHelper = new Gson().fromJson(extras.getString("PROCESS_INFO"), SendProcessInfoHelper.class);
            if (processInfoHelper == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_shipping_info), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            onHandQtyInfo = new Gson().fromJson(extras.getString("ON_HAND_QTY_INFO"), SendOnHandQtyInfo.class);
            if (onHandQtyInfo == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_material_stock_data), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            sendingHandler = new SendingHandler();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_shipping_info), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MaterialSendingTransferActivity.this);
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

        labelMergeWoNumber = findViewById(R.id.label_merge_number);
        txtMergeWoNumber = findViewById(R.id.txt_merge_number);
        txtLineNumber = findViewById(R.id.txt_line_number);
        txtPartNo = findViewById(R.id.txt_part_no);

        txtTotalSendQty = findViewById(R.id.txt_total_sending_qty);
        txtSendQty = findViewById(R.id.txt_send_qty);
        txtUnsendQty = findViewById(R.id.txt_unsend_qty);

        lTransferQty = findViewById(R.id.ll_transfer_qty);
        lTransferNo = findViewById(R.id.ll_transfer_no);
        txtTransferQty = findViewById(R.id.txt_subinventory_transfer_qty);
        txtTransferNo = findViewById(R.id.txt_subinventory_transfer_no);

        lStockLayout = findViewById(R.id.stockLayout);
        txtSubinventoryFrom = findViewById(R.id.edittext_import_subinventory_from);
        txtLocatorFrom = findViewById(R.id.edittext_locator_from);
        txtSubinventoryTo = findViewById(R.id.edittext_import_subinventory_to);
        txtLocatorTo = findViewById(R.id.edittext_locator_to);

        txtSubinventoryFrom.setOnKeyListener(keyListener);

        lDcQrCodeLayout = findViewById(R.id.dcQrCodeLayout);
        txtDcQrCode = findViewById(R.id.edittext_import_dc_qrcode);

        lPNLayout = findViewById(R.id.pnLayout);
        txtPnPN = findViewById(R.id.edittext_import_pn_pn);
        txtPnDC = findViewById(R.id.edittext_import_pn_dc);
        rbPnEquation = findViewById(R.id.radioButtonPnEquation);
        rbPnEquation.setOnCheckedChangeListener(pnEquationListener);
        txtPnMultiplier = findViewById(R.id.edittext_pn_multiplier);
        txtPnMultiplicand = findViewById(R.id.edittext_pn_multiplicand);
        txtPnAddend = findViewById(R.id.edittext_pn_addend);
        txtPnAddend.setOnKeyListener(keyListener);
        rbPnQty = findViewById(R.id.radioButtonPnQty);
        rbPnQty.setOnCheckedChangeListener(pnQtyListener);
        txtPnQty = findViewById(R.id.edittext_pn_qty);

        txtPnPN.setOnKeyListener(keyListener);

        lReelIDLayout = findViewById(R.id.reelIdLayout);
        txtReelID = findViewById(R.id.edittext_import_reel_id);
        rbReelIdEquation = findViewById(R.id.radioButtonReelIdEquation);
        rbReelIdEquation.setOnCheckedChangeListener(reelIdEquationListener);
        txtReelIdMultiplier = findViewById(R.id.edittext_reelid_multiplier);
        txtReelIdMultiplicand = findViewById(R.id.edittext_reelid_multiplicand);
        txtReelIdAddend = findViewById(R.id.edittext_reelid_addend);
        txtReelIdAddend.setOnKeyListener(keyListener);
        rbReelIdQty = findViewById(R.id.radioButtonReelIdQty);
        rbReelIdQty.setOnCheckedChangeListener(reelIdQtyListener);
        txtReelIdQty = findViewById(R.id.edittext_reelid_qty);

        lNoLayout = findViewById(R.id.noLayout);
        txtNoPN = findViewById(R.id.edittext_import_no_pn);
        rbNoEquation = findViewById(R.id.radioButtonNoEquation);
        rbNoEquation.setOnCheckedChangeListener(noEquationListener);
        txtNoMultiplier = findViewById(R.id.edittext_no_multiplier);
        txtNoMultiplicand = findViewById(R.id.edittext_no_multiplicand);
        txtNoAddend = findViewById(R.id.edittext_no_addend);
        txtNoAddend.setOnKeyListener(keyListener);
        rbNoQty = findViewById(R.id.radioButtonNoQty);
        rbNoQty.setOnCheckedChangeListener(noQtyListener);
        txtNoQty = findViewById(R.id.edittext_no_qty);

        lPalletLayout = findViewById(R.id.palletLayout);
        rbPalletSN = findViewById(R.id.radio_pallet_serial_no);
        rbPalletSN.setOnCheckedChangeListener(palletSnListener);
        txtPalletSN = findViewById(R.id.edittext_import_pallet_sn);
        txtPalletSN.setImeOptions(EditorInfo.IME_ACTION_DONE);

        rbPalletCN = findViewById(R.id.radio_pallet_cn);
        rbPalletCN.setOnCheckedChangeListener(palletCnListener);
        txtPalletCN = findViewById(R.id.edittext_import_pallet_cn);

        lBoxLayout = findViewById(R.id.boxLayout);
        rbBoxSN = findViewById(R.id.radio_box_serial_no);
        rbBoxSN.setOnCheckedChangeListener(boxSnListener);
        txtBoxSN = findViewById(R.id.edittext_import_box_sn);
        txtBoxSN.setImeOptions(EditorInfo.IME_ACTION_DONE);

        rbBoxCN = findViewById(R.id.radio_box_cn);
        rbBoxCN.setOnCheckedChangeListener(boxCnListener);
        txtBoxCN = findViewById(R.id.edittext_import_box_cn);

        lSNLayout = findViewById(R.id.snLayout);
        txtSnSN = findViewById(R.id.edittext_import_sn3);

        btnReturn = findViewById(R.id.button_return);
        btnConfim = findViewById(R.id.button_confirm);
        btnCancel = findViewById(R.id.button_cancel);
        btnDebit = findViewById(R.id.button_debit);

        if (onHandQtyInfo.getControl().equals("SN"))
            btnDebit.setText(R.string.button_debit);
        else
            btnDebit.setText(R.string.button_complete);

        btnDebit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onHandQtyInfo.getControl().equals("SN")) {
                    doDebit();
                } else {
                    if (needRefresh) {
                        doReturn();
                    } else {
                        finish();
                    }
                }
            }
        });

        rgDC = findViewById(R.id.radio_group_dc);
        rbQrCode = findViewById(R.id.radio_dc_qrcode);
        rbReelID = findViewById(R.id.radio_reel_id);
        rbPartNo = findViewById(R.id.radio_part_number);
        rgDC.setOnCheckedChangeListener(dcListener);

        rgSN = findViewById(R.id.radio_group_sn);
        rgSN.setOnCheckedChangeListener(snListener);

        setQuantityTitle();

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

                if (!checkSubAndLocator())
                    return;
                switch (mode) {
                    case DC_PN:
                        if (getPartNo().equals("")) {
                            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_sku), Toast.LENGTH_SHORT).show();
                            txtPnPN.requestFocus();
                        } else if (!getPartNo().equals(onHandQtyInfo.getPartNo())) {
                            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.sku_incorrect), Toast.LENGTH_SHORT).show();
                            txtPnPN.requestFocus();
                        } else if (getDateCode().equals("")) {
                            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_dc), Toast.LENGTH_SHORT).show();
                            txtPnDC.requestFocus();
                        } else if (!isDatecodeExist(getDateCode())) {
                            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.dc_not_in_this_sub_loc), Toast.LENGTH_SHORT).show();
                            txtPnDC.requestFocus();
                        } else if (rbPnEquation.isChecked()) {
                            if (txtPnMultiplier.getText().toString().trim().equals("")) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_multiplier), Toast.LENGTH_SHORT).show();
                                txtPnMultiplier.requestFocus();
                            } else if (txtPnMultiplicand.getText().toString().trim().equals("")) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_multiplicand), Toast.LENGTH_SHORT).show();
                                txtPnMultiplicand.requestFocus();
                            } else if (txtPnAddend.getText().toString().trim().equals("")) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_add), Toast.LENGTH_SHORT).show();
                                txtPnAddend.requestFocus();
                            } else if (getInputQty(txtPnMultiplier, txtPnMultiplicand, txtPnAddend) <= 0) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtPnMultiplier.requestFocus();
                            } else if (getInputQty(txtPnMultiplier, txtPnMultiplicand, txtPnAddend) > getAvailableQty()) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.qty_greater_than_stock)+"(" + getAvailableQty() + ")", Toast.LENGTH_SHORT).show();
                                txtPnMultiplier.requestFocus();
                            } else {
                                hideKeyboard();
                                doCompareAndSendingTansfer(getDateCode());
                            }
                        } else {
                            if (txtPnQty.getText().toString().trim().equals("")) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_qty), Toast.LENGTH_SHORT).show();
                                txtPnQty.requestFocus();
                            } else if (getInputQty(txtPnQty) <= 0) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtPnQty.requestFocus();
                            } else if (getInputQty(txtPnQty) > getAvailableQty()) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.qty_greater_than_stock)+"(" + getAvailableQty() + ")", Toast.LENGTH_SHORT).show();
                                txtPnQty.requestFocus();
                            } else {
                                hideKeyboard();
                                doCompareAndSendingTansfer(getDateCode());
                            }
                        }

                        break;
                    case DC_QR_CODE:
                        if (txtDcQrCode.getText().toString().trim().equals("")) {
                            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_qrcode), Toast.LENGTH_SHORT).show();
                            txtDcQrCode.requestFocus();
                        } else if (getReelId().length() != 34) {
                            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.reelid_incorrect_len_must_be_34), Toast.LENGTH_SHORT).show();
                            txtDcQrCode.requestFocus();
                        } else if (!getPartNo().equals(onHandQtyInfo.getPartNo())) {
                            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.sku_incorrect), Toast.LENGTH_SHORT).show();
                            txtDcQrCode.requestFocus();
                        } else if (!isDatecodeExist(getDateCode())) {
                            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.dc_not_in_this_sub_loc), Toast.LENGTH_SHORT).show();
                            txtDcQrCode.requestFocus();
                        } else {
                            if (getInputQty(txtDcQrCode) <= 0) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtDcQrCode.requestFocus();
                            } else if (getInputQty(txtDcQrCode) > getAvailableQty()) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.qty_greater_than_stock)+"(" + getAvailableQty() + ")", Toast.LENGTH_SHORT).show();
                                txtDcQrCode.requestFocus();
                            } else {
                                hideKeyboard();
                                doCompareAndSendingTansfer(getDateCode());
                            }
                        }

                        break;
                    case DC_REEL_ID:
                        if (getReelId().equals("")) {
                            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_reelid), Toast.LENGTH_SHORT).show();
                            txtReelID.requestFocus();
                        } else if (getReelId().length() != 28 && getReelId().length() != 34) {
                            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.reelid_incorrect_len_must_be_34_or_28), Toast.LENGTH_SHORT).show();
                            txtReelID.requestFocus();
                        } else if (!getPartNo().equals(onHandQtyInfo.getPartNo())) {
                            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.sku_incorrect), Toast.LENGTH_SHORT).show();
                            txtReelID.requestFocus();
                        } else if (!isDatecodeExist(getDateCode())) {
                            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.dc_not_in_this_sub_loc), Toast.LENGTH_SHORT).show();
                            txtPnDC.requestFocus();
                        } else if (rbReelIdEquation.isChecked()) {
                            if (txtReelIdMultiplier.getText().toString().trim().equals("")) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_multiplier), Toast.LENGTH_SHORT).show();
                                txtReelIdMultiplier.requestFocus();
                            } else if (txtReelIdMultiplicand.getText().toString().trim().equals("")) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_multiplicand), Toast.LENGTH_SHORT).show();
                                txtReelIdMultiplicand.requestFocus();
                            } else if (txtReelIdAddend.getText().toString().trim().equals("")) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_add), Toast.LENGTH_SHORT).show();
                                txtReelIdAddend.requestFocus();
                            } else if (getInputQty(txtReelIdMultiplier, txtReelIdMultiplicand, txtReelIdAddend) <= 0) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtReelIdMultiplier.requestFocus();
                            } else if (getInputQty(txtReelIdMultiplier, txtReelIdMultiplicand, txtReelIdAddend) > getAvailableQty()) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.qty_greater_than_stock)+"(" + getAvailableQty() + ")", Toast.LENGTH_SHORT).show();
                                txtReelIdMultiplier.requestFocus();
                            } else {
                                hideKeyboard();
                                doCompareAndSendingTansfer(getDateCode());
                            }
                        } else {
                            if (txtReelIdQty.getText().toString().trim().equals("")) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_qty), Toast.LENGTH_SHORT).show();
                                txtReelIdQty.requestFocus();
                            } else if (getInputQty(txtReelIdQty) <= 0) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtReelIdQty.requestFocus();
                            } else if (getInputQty(txtReelIdQty) > getAvailableQty()) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.qty_greater_than_stock)+"(" + getAvailableQty() + ")", Toast.LENGTH_SHORT).show();
                                txtReelIdQty.requestFocus();
                            } else {
                                hideKeyboard();
                                doCompareAndSendingTansfer(getDateCode());
                            }
                        }

                        break;
                    case NO:
                        if (txtNoPN.getText().toString().trim().equals("")) {
                            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_sku), Toast.LENGTH_SHORT).show();
                            txtNoPN.requestFocus();
                        } else if (!txtNoPN.getText().toString().trim().equals(onHandQtyInfo.getPartNo())) {
                            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.sku_incorrect), Toast.LENGTH_SHORT).show();
                            txtNoPN.requestFocus();
                        } else if (rbNoEquation.isChecked()) {
                            if (txtNoMultiplier.getText().toString().trim().equals("")) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_multiplier), Toast.LENGTH_SHORT).show();
                                txtNoMultiplier.requestFocus();
                            } else if (txtNoMultiplicand.getText().toString().trim().equals("")) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_multiplicand), Toast.LENGTH_SHORT).show();
                                txtNoMultiplicand.requestFocus();
                            } else if (txtNoAddend.getText().toString().trim().equals("")) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_add), Toast.LENGTH_SHORT).show();
                                txtNoAddend.requestFocus();
                            } else if (getInputQty(txtNoMultiplier, txtNoMultiplicand, txtNoAddend) <= 0) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtNoMultiplier.requestFocus();
                            } else if (getInputQty(txtNoMultiplier, txtNoMultiplicand, txtNoAddend) > getAvailableQty()) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.qty_greater_than_stock)+"(" + getAvailableQty() + ")", Toast.LENGTH_SHORT).show();
                                txtNoMultiplier.requestFocus();
                            } else {
                                hideKeyboard();
                                //if (checkPrinterSetting()) {
                                doTransfer();
                                //}
                            }
                        } else {
                            if (txtNoQty.getText().toString().trim().equals("")) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_qty), Toast.LENGTH_SHORT).show();
                                txtNoQty.requestFocus();
                            } else if (getInputQty(txtNoQty) <= 0) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtNoQty.requestFocus();
                            } else if (getInputQty(txtNoQty) > getAvailableQty()) {
                                Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.qty_greater_than_stock)+"(" + getAvailableQty() + ")", Toast.LENGTH_SHORT).show();
                                txtNoQty.requestFocus();
                            } else {
                                hideKeyboard();
                                // if (checkPrinterSetting()) {
                                doTransfer();
                                //}
                            }
                        }

                        break;
                    case SN_PALLET:
                        if (rbPalletSN.isChecked() && txtPalletSN.getText().toString().trim().equals("")) {
                            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_sn), Toast.LENGTH_SHORT).show();
                            txtPalletSN.requestFocus();
                        } else if (rbPalletCN.isChecked() && txtPalletCN.getText().toString().trim().equals("")) {
                            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_box_no), Toast.LENGTH_SHORT).show();
                            txtPalletCN.requestFocus();
                        } else if (!rbPalletSN.isChecked() && !rbPalletCN.isChecked()) {
                            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.choose_sn_box), Toast.LENGTH_SHORT).show();
                        } else {
                            hideKeyboard();
                            //if (checkPrinterSetting()) {
                            doTransfer();
                            //}
                        }

                        break;
                    case SN_BOX:
                        if (rbBoxSN.isChecked() && txtBoxSN.getText().toString().trim().equals("")) {
                            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_sn), Toast.LENGTH_SHORT).show();
                            txtBoxSN.requestFocus();
                        } else if (rbBoxCN.isChecked() && txtBoxCN.getText().toString().trim().equals("")) {
                            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_box_no), Toast.LENGTH_SHORT).show();
                            txtBoxCN.requestFocus();
                        } else if (!rbBoxSN.isChecked() && !rbBoxCN.isChecked()) {
                            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.choose_sn_box), Toast.LENGTH_SHORT).show();
                        } else {
                            hideKeyboard();
                            //if (checkPrinterSetting()) {
                            doTransfer();
                            //}
                        }

                        break;
                    case SN_SN:
                        if (txtSnSN.getText().toString().trim().equals("")) {
                            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_sn), Toast.LENGTH_SHORT).show();
                            txtSnSN.requestFocus();
                        } else {
                            hideKeyboard();
                            //if (checkPrinterSetting()) {
                            doTransfer();
                            //}
                        }

                        break;
                    default:
                        break;
                }
            }
        });

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Cancel", String.valueOf(v.getId()));
                cleanData();
            }
        });

        setView();
        setInventoryData();
        doQueryPN();
    }

    protected double getInputQty(EditText edit) {
        if (mode == LayoutType.DC_QR_CODE)
            return Double.parseDouble(QrCodeUtil.getValueFromItemLabelQrCode(edit.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.QTY));
        else
            return Double.parseDouble(edit.getText().toString().trim());
    }

    public String getReelId() {
        if (mode == LayoutType.DC_QR_CODE)
            return QrCodeUtil.getValueFromItemLabelQrCode(txtDcQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID);
        else if (mode == LayoutType.DC_REEL_ID)
            return txtReelID.getText().toString().trim();
        else
            return "";
    }

    public String getPartNo() {
        if (mode == LayoutType.DC_QR_CODE || mode == LayoutType.DC_REEL_ID)
            return getReelId().substring(0, 12);
        else
            return txtPnPN.getText().toString().trim();
    }

    public String getDateCode() {
        if (mode == LayoutType.DC_QR_CODE || mode == LayoutType.DC_REEL_ID)
            return getReelId().substring(18, 28);
        else
            return txtPnDC.getText().toString().trim();
    }

    private boolean checkSubAndLocator() {
        if (txtSubinventoryFrom.getText().toString().trim().equals("")) {
            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.import_subinventory_from), Toast.LENGTH_SHORT).show(); //"請填寫輸入轉出倉別"
            txtSubinventoryFrom.requestFocus();
            return false;
        } else if (EXCLUDE_SUBINVENTORY.contains(txtSubinventoryFrom.getText().toString().trim())) {
            Toast.makeText(MaterialSendingTransferActivity.this, txtSubinventoryFrom.getText().toString().trim() + getString(R.string.sub_controlled_enter_correct_sub), Toast.LENGTH_SHORT).show(); //"為不可出之倉別，請重新輸入"
            txtSubinventoryFrom.setText("");
            txtSubinventoryFrom.requestFocus();
            return false;
        } else if (txtLocatorFrom.getText().toString().trim().equals("")) {
            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.import_locator_from), Toast.LENGTH_SHORT).show();
            txtLocatorFrom.requestFocus();
            return false;
        } else if (txtLocatorFrom.getText().toString().trim().equals(Constant.LOCATOR_P023)) {
            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_other_loc), Toast.LENGTH_SHORT).show();
            txtLocatorFrom.requestFocus();
            return false;
        } else {
            hideKeyboard();

            if (!doCheckSubinventoryAndLocator(txtSubinventoryFrom.getText().toString().trim(), txtLocatorFrom.getText().toString().trim())) {
                txtSubinventoryFrom.requestFocus();
                return false;
            }
        }

        if (txtSubinventoryTo.getText().toString().trim().equals("")) {
            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.import_subinventory_to), Toast.LENGTH_SHORT).show();
            txtSubinventoryTo.requestFocus();
            return false;
        } else if (txtLocatorTo.getText().toString().trim().equals("")) {
            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.import_locator_to), Toast.LENGTH_SHORT).show();
            txtLocatorTo.requestFocus();
            return false;
        } else if (txtLocatorTo.getText().toString().trim().equals(Constant.LOCATOR_P023)) {
            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.enter_other_loc)+"(in)", Toast.LENGTH_SHORT).show();//"請填寫輸入其它轉入的儲位"
            txtLocatorTo.requestFocus();
            return false;
        } else if (processInfoHelper.getLocator().equals(txtLocatorTo.getText().toString().trim())) {
            Toast.makeText(MaterialSendingTransferActivity.this, getString(R.string.target_loc_cant_be_the_same_as_source_loc), Toast.LENGTH_SHORT).show();
            txtLocatorTo.requestFocus();
            return false;
        }

        return true;
    }

    private double getAvailableQty() {
        BigDecimal quantity = BigDecimal.valueOf(0);

        if (onHandQtyInfo.getSubinventoryInfoList() != null && onHandQtyInfo.getSubinventoryInfoList().length > 0) {
            if (mode == LayoutType.DC_REEL_ID || mode == LayoutType.DC_PN || mode == LayoutType.DC_QR_CODE) {
                String dateCode = getDateCode();

                for (SendSubinventoryInfo info : onHandQtyInfo.getSubinventoryInfoList()) {
                    if (info.getSubinventory().equals(processInfoHelper.getSubinventory())
                            && info.getLocator().equals(processInfoHelper.getLocator())
                            && info.getDatecode().equals(dateCode)
                            && info.getInventory().doubleValue() > 0) {
                        quantity = quantity.add(info.getInventory());
                    }
                }
            } else {
                for (SendSubinventoryInfo info : onHandQtyInfo.getSubinventoryInfoList()) {
                    if (info.getSubinventory().equals(processInfoHelper.getSubinventory())
                            && info.getLocator().equals(processInfoHelper.getLocator())
                            && info.getInventory().doubleValue() > 0) {
                        quantity = quantity.add(info.getInventory());
                    }
                }
            }
        }

        return quantity.doubleValue();
    }

    private double getInputQty(EditText multiplier, EditText multiplicand, EditText addEnd) {
        double mul = Double.parseDouble(multiplier.getText().toString().trim());
        double cand = Double.parseDouble(multiplicand.getText().toString().trim());
        double add = Double.parseDouble(addEnd.getText().toString().trim());
        return BigDecimal.valueOf(mul).multiply(BigDecimal.valueOf(cand)).add(BigDecimal.valueOf(add)).doubleValue();
    }

    private void doDebit() {
        if (sendInfo != null && onHandQtyInfo != null) {
            dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.Data_debiting_processing), true);
            sendInfo.setItemNo(onHandQtyInfo.getPartNo());
            sendInfo.setTrxType("T");
            new DoDebit().execute(0);
        }
    }

    private boolean isDatecodeExist(String dateCode) {
        boolean available = false;

        if (onHandQtyInfo.getSubinventoryInfoList() != null && onHandQtyInfo.getSubinventoryInfoList().length > 0) {
            for (SendSubinventoryInfo info : onHandQtyInfo.getSubinventoryInfoList()) {
                if (info.getSubinventory().equals(processInfoHelper.getSubinventory())
                        && info.getLocator().equals(processInfoHelper.getLocator())
                        && info.getDatecode().equals(dateCode)) {
                    available = true;
                    break;
                }
            }
        }

        return available;
    }

    private void doCompareAndSendingTansfer(String dc) {
        String olderDC = getOlderDC(dc);

        if (TextUtils.isEmpty(olderDC)) {
            //if (checkPrinterSetting()) {
            doTransfer();
            //}
        } else {
            showAskDialog(olderDC);
        }
    }

    private void showAskDialog(String dc) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle(getString(R.string.warn));
        dialog.setMessage(getString(R.string.find_older_dc)
                + dc
                + getString(R.string.do_u_want_to_transfer));
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setPositiveButton(getString(R.string.button_confirm_setting),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int arg1) {
                        dialog.dismiss();
                        //if (checkPrinterSetting()) {
                        doTransfer();
                        //}
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

    private void setQuantityTitle() {
        txtLineNumber.setText(processInfoHelper.getLineNo());
        labelMergeWoNumber.setVisibility(View.VISIBLE);
        txtMergeWoNumber.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(processInfoHelper.getMergeNo())) {
            labelMergeWoNumber.setText(R.string.label_merge_number);
            txtMergeWoNumber.setText(processInfoHelper.getMergeNo());
        } else if (!TextUtils.isEmpty(processInfoHelper.getWoNo())) {
            labelMergeWoNumber.setText(R.string.label_workorder_no);
            txtMergeWoNumber.setText(processInfoHelper.getWoNo());
        } else {
            labelMergeWoNumber.setVisibility(View.INVISIBLE);
            txtMergeWoNumber.setVisibility(View.INVISIBLE);
        }

        txtPartNo.setText(onHandQtyInfo.getPartNo());
        txtTotalSendQty.setText(Util.fmt(onHandQtyInfo.getTotalSendingQty().doubleValue()));
        txtSendQty.setText(Util.fmt(onHandQtyInfo.getSentQty().doubleValue()));
        txtUnsendQty.setText(Util.fmt(onHandQtyInfo.getUnsentQty().doubleValue()));

        txtTransferQty.setText(Util.fmt(totalTransferQty.doubleValue()));
        txtTransferNo.setText(Util.getTransferNo());
    }

    private void setInventoryData() {
        for (SendSubinventoryInfo info : onHandQtyInfo.getSubinventoryInfoList()) {
            if (!EXCLUDE_SUBINVENTORY.contains(info.getSubinventory()) && info.getInventory().doubleValue() > 0) {
                txtSubinventoryFrom.setText(info.getSubinventory());
                txtLocatorFrom.setText(info.getLocator());
                txtSubinventoryFrom.requestFocus();
                txtSubinventoryFrom.selectAll();
                break;
            }
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtSubinventoryFrom.getWindowToken(), 0);
    }

    private void setView() {
        if (onHandQtyInfo.getControl().equals("SN")) {
            if (rgSN.getCheckedRadioButtonId() == -1)
                rgSN.check(R.id.radio_pallet);
        } else if (onHandQtyInfo.getControl().equals("DC")) {
            if (rgDC.getCheckedRadioButtonId() == -1)
                rgDC.check(R.id.radio_dc_qrcode);
        } else
            showLayout(LayoutType.NO);
    }

    private void cleanData() {
        //txtSubinventoryTo.setText("");
        //txtLocatorTo.setText("");
        //txtSubinventoryFrom.setText("");
        //txtLocatorFrom.setText("");
        //txtSubinventoryFrom.requestFocus();
        switch (mode) {
            case DC_PN:
                txtPnPN.setText("");
                txtPnDC.setText("");
                txtPnMultiplier.setText("");
                txtPnMultiplicand.setText("");
                txtPnAddend.setText("");
                txtPnQty.setText("");
                txtPnPN.requestFocus();
                break;
            case DC_QR_CODE:
                txtDcQrCode.setText("");
                txtDcQrCode.requestFocus();
                break;
            case DC_REEL_ID:
                txtReelID.setText("");
                txtReelIdMultiplier.setText("");
                txtReelIdMultiplicand.setText("");
                txtReelIdAddend.setText("");
                txtReelIdQty.setText("");
                txtReelID.requestFocus();
                break;
            case NO:
                txtNoPN.setText("");
                txtNoMultiplier.setText("");
                txtNoMultiplicand.setText("");
                txtNoAddend.setText("");
                txtNoQty.setText("");
                txtNoPN.requestFocus();
                break;
            case SN_PALLET:
                txtPalletSN.setText("");
                txtPalletSN.requestFocus();
                break;
            case SN_BOX:
                txtBoxSN.setText("");
                txtBoxCN.setText("");
                txtBoxSN.requestFocus();
                break;
            case SN_SN:
                txtSnSN.setText("");
                txtSnSN.requestFocus();
                break;
            default:
                break;
        }
    }

    private String getOlderDC(String inputDC) {
        if (onHandQtyInfo != null && onHandQtyInfo.getSubinventoryInfoList() != null && onHandQtyInfo.getSubinventoryInfoList().length > 0) {
            for (SendSubinventoryInfo info : onHandQtyInfo.getSubinventoryInfoList()) {
                if (!EXCLUDE_SUBINVENTORY.contains(info.getSubinventory()) && info.getInventory().doubleValue() > 0 && compareDatecode(info.getDatecode(), inputDC)) {
                    return info.getDatecode();
                }
            }
        }

        return null;
    }

    private boolean compareDatecode(String datecode1, String datecode2) {
        if (TextUtils.isEmpty(datecode1) || TextUtils.isEmpty(datecode2)) {
            return false;
        } else {
            int code1 = Integer.parseInt(datecode1.substring(0, 4));
            int code2 = Integer.parseInt(datecode2.substring(0, 4));
            if (code1 < code2)
                return true;
            else
                return false;
        }
    }

    protected boolean doCheckSubinventoryAndLocator(String subinventory, String locator) {
        if (onHandQtyInfo.getSubinventoryInfoList() != null && onHandQtyInfo.getSubinventoryInfoList().length > 0) {
            for (SendSubinventoryInfo info : onHandQtyInfo.getSubinventoryInfoList()) {
                if (info.getSubinventory().equals(txtSubinventoryFrom.getText().toString().trim())
                        && info.getLocator().equals(txtLocatorFrom.getText().toString().trim())
                        && info.getInventory().doubleValue() > 0) {
                    processInfoHelper.setSubinventory(info.getSubinventory());
                    processInfoHelper.setLocator(info.getLocator());
                    return true;
                }
            }
        }

        Toast.makeText(getApplicationContext(), getString(R.string.sub_lot_incorrect), Toast.LENGTH_LONG).show();
        txtSubinventoryFrom.requestFocus();
        return false;
    }

    protected void doCheckTransferSubinventoryAndLocator(String subinventory, String locator) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.chk_sub_locate), true);
        subinventoryHelper = new SubinventoryInfoHelper();
        subinventoryHelper.setPartNo(onHandQtyInfo.getPartNo());
        subinventoryHelper.setSubinventory(subinventory);
        subinventoryHelper.setLocator(locator);
        stockInHandler = new StockInHandler();
        new CheckSubInventory().execute(0);
    }

    private void showLayout(LayoutType type) {
        mode = type;
        lTransferQty.setVisibility(View.GONE);
        lTransferNo.setVisibility(View.GONE);
        rgDC.setVisibility(View.INVISIBLE);
        rgSN.setVisibility(View.INVISIBLE);
        lPalletLayout.setVisibility(View.GONE);
        lNoLayout.setVisibility(View.GONE);
        lDcQrCodeLayout.setVisibility(View.GONE);
        lReelIDLayout.setVisibility(View.GONE);
        lStockLayout.setVisibility(View.GONE);
        lPNLayout.setVisibility(View.GONE);
        lBoxLayout.setVisibility(View.GONE);
        lSNLayout.setVisibility(View.GONE);
        btnDebit.setVisibility(View.GONE);
        lTransferNo.setVisibility(View.VISIBLE);
        lStockLayout.setVisibility(View.VISIBLE);

        switch (type) {
            case DC_PN:
                lTransferQty.setVisibility(View.VISIBLE);
                rgDC.setVisibility(View.VISIBLE);
                lPNLayout.setVisibility(View.VISIBLE);
                btnDebit.setVisibility(View.VISIBLE);
                rbPnEquation.setChecked(true);
                txtPnPN.requestFocus();
                break;
            case DC_QR_CODE:
                lTransferQty.setVisibility(View.VISIBLE);
                rgDC.setVisibility(View.VISIBLE);
                lDcQrCodeLayout.setVisibility(View.VISIBLE);
                rbQrCode.setChecked(true);
                txtDcQrCode.requestFocus();
                break;
            case DC_REEL_ID:
                lTransferQty.setVisibility(View.VISIBLE);
                rgDC.setVisibility(View.VISIBLE);
                lReelIDLayout.setVisibility(View.VISIBLE);
                btnDebit.setVisibility(View.VISIBLE);
                rbReelIdEquation.setChecked(true);
                txtReelID.requestFocus();
                break;
            case NO:
                lTransferQty.setVisibility(View.VISIBLE);
                lNoLayout.setVisibility(View.VISIBLE);
                btnDebit.setVisibility(View.VISIBLE);
                rbNoEquation.setChecked(true);
                txtNoPN.requestFocus();
                break;
            case SN_PALLET:
                lTransferQty.setVisibility(View.VISIBLE);
                rgSN.setVisibility(View.VISIBLE);
                lPalletLayout.setVisibility(View.VISIBLE);
                btnDebit.setVisibility(View.VISIBLE);
                rbPalletSN.setChecked(true);
                break;
            case SN_BOX:
                lTransferQty.setVisibility(View.VISIBLE);
                rgSN.setVisibility(View.VISIBLE);
                lBoxLayout.setVisibility(View.VISIBLE);
                btnDebit.setVisibility(View.VISIBLE);
                rbBoxSN.setChecked(true);
                break;
            case SN_SN:
                lTransferQty.setVisibility(View.VISIBLE);
                rgSN.setVisibility(View.VISIBLE);
                lSNLayout.setVisibility(View.VISIBLE);
                btnDebit.setVisibility(View.VISIBLE);
                txtSnSN.requestFocus();
                break;
            default:
                break;
        }
    }

    private void showNeedDiebtDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Warning Msg");
        dialog.setMessage(getString(R.string.perform_debit));
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

    private void returnPage() {
        hideKeyboard();

        if (onHandQtyInfo.getControl().equals("SN") && needDeibt) {
            showNeedDiebtDialog();
            return;
        }

        switch (mode) {
            case DC_PN:
                rgDC.clearCheck();
                txtPnPN.setText("");
                txtPnDC.setText("");
                rbPnEquation.setChecked(false);
                txtPnMultiplier.setText("");
                txtPnMultiplicand.setText("");
                txtPnAddend.setText("");
                rbPnQty.setChecked(false);
                txtPnQty.setText("");
                break;
            case DC_QR_CODE:
                rgDC.clearCheck();
                txtDcQrCode.setText("");
                break;
            case DC_REEL_ID:
                rgDC.clearCheck();
                rbReelIdEquation.setChecked(false);
                txtReelID.setText("");
                txtReelIdMultiplier.setText("");
                txtReelIdMultiplicand.setText("");
                txtReelIdAddend.setText("");
                rbReelIdQty.setChecked(false);
                txtReelIdQty.setText("");
                break;
            case NO:
                txtNoPN.setText("");
                rbNoEquation.setChecked(false);
                txtNoMultiplier.setText("");
                txtNoMultiplicand.setText("");
                txtNoAddend.setText("");
                rbNoQty.setChecked(false);
                txtNoQty.setText("");
                break;
            case SN_PALLET:
                rgSN.clearCheck();
                rbPalletSN.setChecked(false);
                txtPalletSN.setText("");
                rbPalletCN.setChecked(false);
                txtPalletCN.setText("");
                break;
            case SN_BOX:
                rgSN.clearCheck();
                rbBoxSN.setChecked(false);
                txtBoxSN.setText("");
                rbBoxCN.setChecked(false);
                txtBoxCN.setText("");
                break;
            case SN_SN:
                rgSN.clearCheck();
                txtSnSN.setText("");
                break;
            default:
                break;
        }

        mConnection.setText("");
        mConnection.setTextColor(Color.WHITE);
        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        errorInfo = "";
        setQuantityTitle();

        if (needRefresh) {
            doReturn();
        } else {
            finish();
        }
    }

    private void doQueryPN() {
        pnInfo = new PnItemInfoHelper();
        pnInfo.setPartNo(onHandQtyInfo.getPartNo());
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        print = new PrintLabelHandler();
        new GetPNInfo().execute(0);
    }

    private void doTransfer() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.transfer_processing), true);
        String sn;

        switch (mode) {
            case DC_PN:
                processInfoHelper.setDateCode(getDateCode());
                processInfoHelper.setQty(BigDecimal.valueOf(0));

                if (rbPnEquation.isChecked())
                    processInfoHelper.setTransQty(BigDecimal.valueOf(getInputQty(txtPnMultiplier, txtPnMultiplicand, txtPnAddend)));
                else
                    processInfoHelper.setTransQty(BigDecimal.valueOf(getInputQty(txtPnQty)));

                break;
            case DC_QR_CODE:
                processInfoHelper.setReelID(getReelId().substring(0, 28));
                processInfoHelper.setDateCode(getDateCode());
                processInfoHelper.setQty(BigDecimal.valueOf(0));
                processInfoHelper.setTransQty(BigDecimal.valueOf(getInputQty(txtDcQrCode)));
                break;
            case DC_REEL_ID:
                processInfoHelper.setReelID(getReelId().substring(0, 28));
                processInfoHelper.setDateCode(getDateCode());
                processInfoHelper.setQty(BigDecimal.valueOf(0));

                if (rbReelIdEquation.isChecked())
                    processInfoHelper.setTransQty(BigDecimal.valueOf(getInputQty(txtReelIdMultiplier, txtReelIdMultiplicand, txtReelIdAddend)));
                else
                    processInfoHelper.setTransQty(BigDecimal.valueOf(getInputQty(txtReelIdQty)));

                break;
            case NO:
                processInfoHelper.setQty(BigDecimal.valueOf(0));

                if (rbNoEquation.isChecked())
                    processInfoHelper.setTransQty(BigDecimal.valueOf(getInputQty(txtNoMultiplier, txtNoMultiplicand, txtNoAddend)));
                else
                    processInfoHelper.setTransQty(BigDecimal.valueOf(getInputQty(txtNoQty)));

                break;
            case SN_PALLET:
                sn = txtPalletSN.getText().toString().trim();
                processInfoHelper.setSerialNo(sn);
                processInfoHelper.setBoxNo(txtPalletCN.getText().toString().trim());
                processInfoHelper.setType("PLT");
                processInfoHelper.setTrxType("T");
                processInfoHelper.setQty(BigDecimal.valueOf(0));
                processInfoHelper.setTransQty(BigDecimal.valueOf(0));
                break;
            case SN_BOX:
                sn = txtBoxSN.getText().toString().trim();
                processInfoHelper.setSerialNo(sn);
                processInfoHelper.setBoxNo(txtBoxCN.getText().toString().trim());
                processInfoHelper.setType("BOX");
                processInfoHelper.setTrxType("T");
                processInfoHelper.setQty(BigDecimal.valueOf(0));
                processInfoHelper.setTransQty(BigDecimal.valueOf(0));
                break;
            case SN_SN:
                processInfoHelper.setSerialNo(txtSnSN.getText().toString().trim());
                processInfoHelper.setType("SN");
                processInfoHelper.setTrxType("T");
                processInfoHelper.setQty(BigDecimal.valueOf(0));
                processInfoHelper.setTransQty(BigDecimal.valueOf(0));
                break;
            default:
                return;
        }

        processInfoHelper.setTransSubinventory(txtSubinventoryTo.getText().toString().trim());
        processInfoHelper.setTransLocator(txtLocatorTo.getText().toString().trim());
        new DoTransferProcess().execute(0);
    }

    private void setQuantityForTransfer() {
        BigDecimal quantity = processInfoHelper.getTransQty();

        for (SendSubinventoryInfo info : onHandQtyInfo.getSubinventoryInfoList()) {
            if (info.getSubinventory().equals(processInfoHelper.getSubinventory())
                    && info.getLocator().equals(processInfoHelper.getLocator())
                    && info.getInventory().doubleValue() > 0) {
                if (info.getInventory().doubleValue() >= quantity.doubleValue()) {
                    info.setInventory(info.getInventory().subtract(quantity));
                    quantity = BigDecimal.valueOf(0);
                } else {
                    quantity = quantity.subtract(info.getInventory());
                    info.setInventory(BigDecimal.valueOf(0));
                }

                if (quantity.doubleValue() <= 0)
                    break;
            }
        }

        boolean exist = false;
        List<SendSubinventoryInfo> list = new ArrayList<>();

        for (SendSubinventoryInfo info : onHandQtyInfo.getSubinventoryInfoList()) {
            list.add(info);

            if (info.getSubinventory().equals(processInfoHelper.getTransSubinventory())
                    && info.getLocator().equals(processInfoHelper.getTransLocator())) {
                info.setInventory(info.getInventory().add(processInfoHelper.getTransQty()));
                exist = true;
            }
        }

        if (exist) {
            list = null;
        } else {
            SendSubinventoryInfo info = new SendSubinventoryInfo();
            info.setSubinventory(processInfoHelper.getTransSubinventory());
            info.setLocator(processInfoHelper.getTransLocator());
            info.setDatecode(processInfoHelper.getDateCode());
            info.setInventory(processInfoHelper.getTransQty());
            list.add(info);
            onHandQtyInfo.setSubinventoryInfoList(list);
        }
    }

    public void onBackPressed() {
        // do something here and don't write super.onBackPressed()
        Log.d(TAG, "onClick Hardware Back Button");
        returnPage();
    }

    private void doReturn() {
        Intent resultData = new Intent();
        resultData.putExtra("ON_HAND_QTY_INFO", new Gson().toJson(onHandQtyInfo));
        setResult(RESULT_OK, resultData);
        finish();
    }

    enum LayoutType {
        NONE, DC_PN, DC_REEL_ID, NO, SN_PALLET, SN_BOX, SN_SN, DC_QR_CODE
    }

    private class DoDebit extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Do Debit from " + AppController.getServerInfo()
                    + AppController.getProperties("DipWipSn"));
            publishProgress(getString(R.string.Debit_processing));
            return sendingHandler.doDisWipSn(sendInfo);
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
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    needDeibt = false;
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
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

    private class CheckSubInventory extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check Subinventory from " + AppController.getServerInfo()
                    + AppController.getProperties("GetStockNo"));
            publishProgress(getString(R.string.downloading_data));
            return stockInHandler.checkSubInventory(subinventoryHelper);
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
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    setView();
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

    private class DoTransferProcess extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            publishProgress(getString(R.string.transfer_processing));

            if (onHandQtyInfo.getControl().equals("SN")) {
                AppController.debug("Do sending process from " + AppController.getServerInfo()
                        + AppController.getProperties("IssueItemSn"));
                return sendingHandler.doIssueItemSn(processInfoHelper);
            } else {
                AppController.debug("Do sending process from " + AppController.getServerInfo()
                        + AppController.getProperties("ItemNoTrans"));
                return sendingHandler.doItemNoTrans(processInfoHelper);
            }
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
                    needRefresh = true;
                    needDeibt = true;
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    processInfoHelper = (SendProcessInfoHelper) result;
                    cleanData();
                    Toast.makeText(getApplicationContext(), getString(R.string.transfer_ok), Toast.LENGTH_SHORT).show();
                    totalTransferQty = totalTransferQty.add(processInfoHelper.getTransQty());
                    setQuantityForTransfer();
                    setQuantityTitle();
                } else {
                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    Toast.makeText(getApplicationContext(), errorInfo, Toast.LENGTH_SHORT).show();

                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }
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
