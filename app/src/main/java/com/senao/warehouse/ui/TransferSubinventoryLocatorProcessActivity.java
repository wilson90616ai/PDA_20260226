package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.ITEM_LABEL_QR_CODE_FORMAT;
import com.senao.warehouse.database.TransferDateCodeInfo;
import com.senao.warehouse.database.TransferProcessInfoHelper;
import com.senao.warehouse.database.TransferSnInfo;
import com.senao.warehouse.handler.TransferHandler;
import com.senao.warehouse.util.QrCodeUtil;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.util.Util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TransferSubinventoryLocatorProcessActivity extends Activity {

    private static final String TAG = TransferSubinventoryLocatorProcessActivity.class.getSimpleName();

    List<HashMap<String, Object>> data = new ArrayList<>();
    private TextView mConnection;
    private Button btnReturn, btnConfim, btnCancel, btnDeduct;
    private ProgressDialog dialog;
    private String errorInfo = "";
    private TextView txtTransferNo, txtPartNo, txtInventory, txtTransferQty;
    private LinearLayout lPalletLayout,
            lNoLayout, lReelIDLayout, lPNLayout, lBoxLayout,
            lSNLayout, lSnQrCodeLayout, lDcQrCodeLayout;
    private RadioGroup rgDC, rgSN;
    private RadioButton rbDcQrCode, rbReelID, rbPartNo, rbPalletSN, rbBoxSN, rbPalletCN,
            rbBoxCN;
    private EditText txtReelID, txtPalletCN, txtBoxCN,
            txtSnSN, txtBoxSN, txtPalletSN, txtNoPN, txtNoAddend,
            txtNoMultiplier, txtNoMultiplicand, txtReelIdMultiplicand,
            txtReelIdAddend, txtReelIdMultiplier,
            txtPnPN, txtPnDC, txtPnMultiplier, txtPnMultiplicand, txtPnAddend, txtDcQrCode, txtSnQrCode;
    private LayoutType mode = LayoutType.NONE;
    private RadioButton rbPnEquation;
    private RadioButton rbPnQty;
    private EditText txtPnQty;
    private RadioButton rbReelIdEquation;
    private RadioButton rbReelIdQty;
    private EditText txtReelIdQty;
    private RadioButton rbNoEquation;
    private RadioButton rbNoQty;
    private EditText txtNoQty;
    private TransferHandler transferHandler;
    private TransferProcessInfoHelper processInfoHelper;
    private List<TransferSnInfo> snInfoList;
    private ListView listView;
    private DcAdapter dcAdapter;
    private SnAdapter snAdapter;

    private OnKeyListener keyListener = new OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Perform action on key press
                Log.d(TAG, "onKeyListener Enter or Action Down" + String.valueOf(v.getId()));

                if (v.getId() == R.id.edittext_import_pn_pn) {
                    txtPnDC.selectAll();
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
                case R.id.radio_sn_qrcode:
                    Log.d("onClick QR Code", String.valueOf(checkedId));
                    showLayout(LayoutType.SN_QR_CODE);
                    break;
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener dcListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio_dc_qrcode:
                    if (rbDcQrCode.isChecked())
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
        setContentView(R.layout.activity_transfer_subinventory_locator_process);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            processInfoHelper = new Gson().fromJson(extras.getString("CONDITION_INFO"), TransferProcessInfoHelper.class);

            if (processInfoHelper == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_data), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            transferHandler = new TransferHandler();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_data), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (processInfoHelper.getControl().equals("SN")) {
            snInfoList = new ArrayList<>();
        }

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(TransferSubinventoryLocatorProcessActivity.this);
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

        txtTransferNo = findViewById(R.id.txt_subinventory_transfer_no);
        txtPartNo = findViewById(R.id.txt_part_no);
        txtInventory = findViewById(R.id.txt_total_inventory_qty);
        txtTransferQty = findViewById(R.id.txt_total_transfer_subinventory_qty);

        listView = findViewById(R.id.listView);

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

        lDcQrCodeLayout = findViewById(R.id.dcQrCodeLayout);
        txtDcQrCode = findViewById(R.id.edittext_import_dc_qrcode);

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

        lSnQrCodeLayout = findViewById(R.id.snQrCodeLayout);
        txtSnQrCode = findViewById(R.id.edittext_import_sn_qrcode);

        btnReturn = findViewById(R.id.button_return);
        btnConfim = findViewById(R.id.button_confirm);
        btnCancel = findViewById(R.id.button_cancel);
        btnDeduct = findViewById(R.id.button_deduct);

        rgDC = findViewById(R.id.radio_group_dc);
        rbDcQrCode = findViewById(R.id.radio_dc_qrcode);
        rbReelID = findViewById(R.id.radio_reel_id);
        rbPartNo = findViewById(R.id.radio_part_number);
        rgDC.setOnCheckedChangeListener(dcListener);

        rgSN = findViewById(R.id.radio_group_sn);
        rgSN.setOnCheckedChangeListener(snListener);

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

                switch (mode) {
                    case DC_PN:
                        if (getPartNo().equals("")) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_sku), Toast.LENGTH_SHORT).show();
                            txtPnPN.requestFocus();
                        } else if (!getPartNo().equals(processInfoHelper.getPartNo())) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.sku_incorrect), Toast.LENGTH_SHORT).show();
                            txtPnPN.requestFocus();
                        } else if (getDateCode().equals("")) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_dc), Toast.LENGTH_SHORT).show();
                            txtPnDC.requestFocus();
                        } else if (!isDatecodeExist(getDateCode().trim())) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.dc_not_in_this_sub_loc), Toast.LENGTH_SHORT).show();
                            txtPnDC.requestFocus();
                        } else if (rbPnEquation.isChecked()) {
                            if (txtPnMultiplier.getText().toString().trim().equals("")) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_multiplier), Toast.LENGTH_SHORT).show();
                                txtPnMultiplier.requestFocus();
                            } else if (txtPnMultiplicand.getText().toString().trim().equals("")) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_multiplicand), Toast.LENGTH_SHORT).show();
                                txtPnMultiplicand.requestFocus();
                            } else if (txtPnAddend.getText().toString().trim().equals("")) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_add), Toast.LENGTH_SHORT).show();
                                txtPnAddend.requestFocus();
                            } else if (getInputQty(txtPnMultiplier, txtPnMultiplicand, txtPnAddend) <= 0) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtPnMultiplier.requestFocus();
                            } else if (getInputQty(txtPnMultiplier, txtPnMultiplicand, txtPnAddend) > getAvailableQty()) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.qty_greater_than_stock)+"(" + getAvailableQty() + ")", Toast.LENGTH_SHORT).show();
                                txtPnMultiplier.requestFocus();
                            } else {
                                hideKeyboard();
                                doSendingProcess();
                            }
                        } else {
                            if (txtPnQty.getText().toString().trim().equals("")) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_qty), Toast.LENGTH_SHORT).show();
                                txtPnQty.requestFocus();
                            } else if (getInputQty(txtPnQty) <= 0) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtPnQty.requestFocus();
                            } else if (getInputQty(txtPnQty) > getAvailableQty()) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.qty_greater_than_stock)+"(" + getAvailableQty() + ")", Toast.LENGTH_SHORT).show();
                                txtPnQty.requestFocus();
                            } else {
                                hideKeyboard();
                                doSendingProcess();
                            }
                        }

                        break;
                    case DC_QR_CODE:
                        if (txtDcQrCode.getText().toString().trim().equals("")) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_qrcode), Toast.LENGTH_SHORT).show();
                            txtDcQrCode.requestFocus();
                        } else if (getReelId().length() != 34) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.reelid_incorrect_len_must_be_34), Toast.LENGTH_SHORT).show();
                            txtDcQrCode.requestFocus();
                        } else if (!getPartNo().equals(processInfoHelper.getPartNo())) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.sku_incorrect), Toast.LENGTH_SHORT).show();
                            txtDcQrCode.requestFocus();
                        } else if (!isDatecodeExist(getDateCode())) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.dc_not_in_this_sub_loc), Toast.LENGTH_SHORT).show();
                            txtDcQrCode.requestFocus();
                        } else {
                            if (getInputQty(txtDcQrCode) <= 0) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtDcQrCode.requestFocus();
                            } else if (getInputQty(txtDcQrCode) > getAvailableQty()) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.qty_greater_than_stock)+"(" + getAvailableQty() + ")", Toast.LENGTH_SHORT).show();
                                txtDcQrCode.requestFocus();
                            } else {
                                hideKeyboard();
                                doSendingProcess();
                            }
                        }

                        break;
                    case DC_REEL_ID:
                        if (getReelId().equals("")) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_reelid), Toast.LENGTH_SHORT).show();
                            txtReelID.requestFocus();
                        } else if (txtReelID.getText().toString().trim().length() != 28 && txtReelID.getText().toString().trim().length() != 34) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.reelid_incorrect_len_must_be_34_or_28), Toast.LENGTH_SHORT).show();
                            txtReelID.requestFocus();
                        } else if (!getPartNo().equals(processInfoHelper.getPartNo())) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.sku_incorrect), Toast.LENGTH_SHORT).show();
                            txtReelID.requestFocus();
                        } else if (!isDatecodeExist(getDateCode())) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.dc_not_in_this_sub_loc), Toast.LENGTH_SHORT).show();
                            txtPnDC.requestFocus();
                        } else if (rbReelIdEquation.isChecked()) {
                            if (txtReelIdMultiplier.getText().toString().trim().equals("")) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_multiplier), Toast.LENGTH_SHORT).show();
                                txtReelIdMultiplier.requestFocus();
                            } else if (txtReelIdMultiplicand.getText().toString().trim().equals("")) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_multiplicand), Toast.LENGTH_SHORT).show();
                                txtReelIdMultiplicand.requestFocus();
                            } else if (txtReelIdAddend.getText().toString().trim().equals("")) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_add), Toast.LENGTH_SHORT).show();
                                txtReelIdAddend.requestFocus();
                            } else if (getInputQty(txtReelIdMultiplier, txtReelIdMultiplicand, txtReelIdAddend) <= 0) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtReelIdMultiplier.requestFocus();
                            } else if (getInputQty(txtReelIdMultiplier, txtReelIdMultiplicand, txtReelIdAddend) > getAvailableQty()) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.qty_greater_than_stock)+"(" + getAvailableQty() + ")", Toast.LENGTH_SHORT).show();
                                txtReelIdMultiplier.requestFocus();
                            } else {
                                hideKeyboard();
                                doSendingProcess();
                            }
                        } else {
                            if (txtReelIdQty.getText().toString().trim().equals("")) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_qty), Toast.LENGTH_SHORT).show();
                                txtReelIdQty.requestFocus();
                            } else if (getInputQty(txtReelIdQty) <= 0) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtReelIdQty.requestFocus();
                            } else if (getInputQty(txtReelIdQty) > getAvailableQty()) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.qty_greater_than_stock)+"(" + getAvailableQty() + ")", Toast.LENGTH_SHORT).show();
                                txtReelIdQty.requestFocus();
                            } else {
                                hideKeyboard();
                                doSendingProcess();
                            }
                        }

                        break;
                    case NO:
                        if (txtNoPN.getText().toString().trim().equals("")) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_sku), Toast.LENGTH_SHORT).show();
                            txtNoPN.requestFocus();
                        } else if (!txtNoPN.getText().toString().trim().equals(processInfoHelper.getPartNo())) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.sku_incorrect), Toast.LENGTH_SHORT).show();
                            txtNoPN.requestFocus();
                        } else if (rbNoEquation.isChecked()) {
                            if (txtNoMultiplier.getText().toString().trim().equals("")) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_multiplier), Toast.LENGTH_SHORT).show();
                                txtNoMultiplier.requestFocus();
                            } else if (txtNoMultiplicand.getText().toString().trim().equals("")) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_multiplicand), Toast.LENGTH_SHORT).show();
                                txtNoMultiplicand.requestFocus();
                            } else if (txtNoAddend.getText().toString().trim().equals("")) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_add), Toast.LENGTH_SHORT).show();
                                txtNoAddend.requestFocus();
                            } else if (getInputQty(txtNoMultiplier, txtNoMultiplicand, txtNoAddend) <= 0) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtNoMultiplier.requestFocus();
                            } else if (getInputQty(txtNoMultiplier, txtNoMultiplicand, txtNoAddend) > getAvailableQty()) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.qty_greater_than_stock)+"(" + getAvailableQty() + ")", Toast.LENGTH_SHORT).show();
                                txtNoMultiplier.requestFocus();
                            } else {
                                hideKeyboard();
                                doSendingProcess();
                            }
                        } else {
                            if (txtNoQty.getText().toString().trim().equals("")) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_qty), Toast.LENGTH_SHORT).show();
                                txtNoQty.requestFocus();
                            } else if (getInputQty(txtNoQty) <= 0) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtNoQty.requestFocus();
                            } else if (getInputQty(txtNoQty) > getAvailableQty()) {
                                Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.qty_greater_than_stock)+"(" + getAvailableQty() + ")", Toast.LENGTH_SHORT).show();
                                txtNoQty.requestFocus();
                            } else {
                                hideKeyboard();
                                doSendingProcess();
                            }
                        }

                        break;
                    case SN_PALLET:
                        if (rbPalletSN.isChecked() && txtPalletSN.getText().toString().trim().equals("")) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_sn), Toast.LENGTH_SHORT).show();
                            txtPalletSN.requestFocus();
                        } else if (rbPalletCN.isChecked() && txtPalletCN.getText().toString().trim().equals("")) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_box_no), Toast.LENGTH_SHORT).show();
                            txtPalletCN.requestFocus();
                        } else if (!rbPalletSN.isChecked() && !rbPalletCN.isChecked()) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.choose_sn_box), Toast.LENGTH_SHORT).show();
                        } else {
                            hideKeyboard();
                            doSendingProcess();
                        }

                        break;
                    case SN_BOX:
                        if (rbBoxSN.isChecked() && txtBoxSN.getText().toString().trim().equals("")) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_sn), Toast.LENGTH_SHORT).show();
                            txtBoxSN.requestFocus();
                        } else if (rbBoxCN.isChecked() && txtBoxCN.getText().toString().trim().equals("")) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_box_no), Toast.LENGTH_SHORT).show();
                            txtBoxCN.requestFocus();
                        } else if (!rbBoxSN.isChecked() && !rbBoxCN.isChecked()) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.choose_sn_box), Toast.LENGTH_SHORT).show();
                        } else {
                            hideKeyboard();
                            doSendingProcess();
                        }

                        break;
                    case SN_SN:
                        if (txtSnSN.getText().toString().trim().equals("")) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_sn), Toast.LENGTH_SHORT).show();
                            txtSnSN.requestFocus();
                        } else {
                            hideKeyboard();
                            doSendingProcess();
                        }

                        break;
                    case SN_QR_CODE:
                        if (txtSnQrCode.getText().toString().trim().equals("")) {
                            Toast.makeText(TransferSubinventoryLocatorProcessActivity.this, getString(R.string.enter_qrcode), Toast.LENGTH_SHORT).show();
                            txtSnQrCode.requestFocus();
                        } else {
                            hideKeyboard();
                            doSendingProcess();
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

        btnDeduct.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Deduct", String.valueOf(v.getId()));
                doDeduct();
            }
        });

        setView();
        setQuantityTitle();
        setListData();
    }


    private void setListData() {
        listView.setVisibility(View.GONE);
        data.clear();

        if (processInfoHelper.getControl().equals("DC")) {
            if (processInfoHelper != null && processInfoHelper.getDateCodeInfo() != null && processInfoHelper.getDateCodeInfo().length > 0) {
                listView.setVisibility(View.VISIBLE);

                for (TransferDateCodeInfo info : processInfoHelper.getDateCodeInfo()) {
                    HashMap<String, Object> item = new HashMap<>();
                    item.put("datecode", info.getDateCode());
                    item.put("qty", info.getQty());
                    item.put("pass", info.getPass());
                    data.add(item);
                }

                dcAdapter = new DcAdapter(this);
                listView.setAdapter(dcAdapter);
            }
        } else if (processInfoHelper.getControl().equals("SN")) {
            if (processInfoHelper != null && processInfoHelper.getSnInfo() != null && processInfoHelper.getSnInfo().length > 0) {
                listView.setVisibility(View.VISIBLE);

                for (TransferSnInfo info : processInfoHelper.getSnInfo()) {
                    HashMap<String, Object> item = new HashMap<>();

                    if (info.getType() == 1) {
                        item.put("type", getString(R.string.label_sn_check));
                        item.put("value", getString(R.string.label_sn_value, info.getValue()));
                    } else if (info.getType() == 2) {
                        item.put("type", getString(R.string.label_box_check));
                        item.put("value", getString(R.string.label_sn_value, info.getValue()));
                    } else if (info.getType() == 3) {
                        item.put("type", getString(R.string.label_pallet_check));
                        item.put("value", getString(R.string.label_sn_value, info.getValue()));
                    } else if (info.getType() == 6) {
                        item.put("type", getString(R.string.label_box_check));
                        item.put("value", getString(R.string.label_box_value, info.getValue()));
                    } else {
                        item.put("type", getString(R.string.label_pallet_check));
                        item.put("value", getString(R.string.label_box_value, info.getValue()));
                    }

                    item.put("qty", info.getQty());
                    data.add(item);
                }

                snAdapter = new SnAdapter(this);
                listView.setAdapter(snAdapter);
            }
        }
    }

    private void doDeduct() {
        if (processInfoHelper != null && processInfoHelper.getTransQty() != null && processInfoHelper.getTransQty().doubleValue() > 0) {
            dialog = ProgressDialog.show(this, getString(R.string.holdon),
                    getString(R.string.Data_debiting_processing), true);

            if (processInfoHelper.getControl().equals("SN")) {
                processInfoHelper.setSnInfo(snInfoList);
            }

            new DoDeduct().execute(0);
        }
    }

    private boolean isDatecodeExist(String dateCode) {
        boolean available = false;

        if (processInfoHelper.getDateCodeInfo() != null && processInfoHelper.getDateCodeInfo().length > 0) {
            for (TransferDateCodeInfo info : processInfoHelper.getDateCodeInfo()) {
                if (info.getDateCode().equals(dateCode)) {
                    available = true;
                    break;
                }
            }
        }

        return available;
    }

    private void showAskDialog(String dc) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle(getString(R.string.warn));
        dialog.setMessage(getString(R.string.find_older_dc)
                + dc
                + getString(R.string.continue_with_warehouse_and_location_transfer));
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setPositiveButton(getString(R.string.button_confirm_setting),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int arg1) {
                        dialog.dismiss();
                        doSendingProcess();
                    }
                });

        dialog.setNegativeButton(R.string.btn_cancel,
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
            return getReelId().substring(18,28);
        else
            return txtPnDC.getText().toString().trim();
    }

    private void setQuantityTitle() {
        txtTransferNo.setText(Util.getTransferNo());
        txtPartNo.setText(processInfoHelper.getPartNo());
        txtInventory.setText(Util.fmt(processInfoHelper.getInventory()));
        txtTransferQty.setText(Util.fmt(processInfoHelper.getTransQty()));
    }

    private double getAvailableQty() {
        BigDecimal quantity = BigDecimal.valueOf(0);

        if (mode == LayoutType.DC_REEL_ID || mode == LayoutType.DC_PN || mode == LayoutType.DC_QR_CODE) {
            if (processInfoHelper.getDateCodeInfo() != null && processInfoHelper.getDateCodeInfo().length > 0) {
                String dateCode = getDateCode();

                for (TransferDateCodeInfo info : processInfoHelper.getDateCodeInfo()) {
                    if (info.getPass() == null) {
                        if (info.getDateCode().equals(dateCode)) {
                            quantity = quantity.add(info.getQty());
                        }
                    } else {
                        if (info.getDateCode().equals(dateCode)
                                && info.getQty().subtract(info.getPass()).doubleValue() > 0) {
                            quantity = quantity.add(info.getQty().subtract(info.getPass()));
                        }
                    }
                }
            }
        } else {
            if (processInfoHelper.getTransQty() == null) {
                quantity = quantity.add(processInfoHelper.getInventory());
            } else {
                quantity = quantity.add(processInfoHelper.getInventory().subtract(processInfoHelper.getTransQty()));
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

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtSnQrCode.getWindowToken(), 0);
    }

    private void setView() {
        if (processInfoHelper.getControl().equals("SN")) {
            if (rgSN.getCheckedRadioButtonId() == -1)
                rgSN.check(R.id.radio_pallet);
        } else if (processInfoHelper.getControl().equals("DC")) {
            if (rgDC.getCheckedRadioButtonId() == -1)
                rgDC.check(R.id.radio_dc_qrcode);
        } else
            showLayout(LayoutType.NO);
    }

    private void cleanData() {
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
            case SN_QR_CODE:
                txtSnQrCode.setText("");
                txtSnQrCode.requestFocus();
                break;
            default:
                break;
        }
    }

    private String getOlderDC(String inputDC) {
        if (processInfoHelper != null && processInfoHelper.getDateCodeInfo() != null && processInfoHelper.getDateCodeInfo().length > 0) {
            for (TransferDateCodeInfo info : processInfoHelper.getDateCodeInfo()) {
                if (info.getPass() == null) {
                    if (info.getQty().doubleValue() > 0 && compareDatecode(info.getDateCode(), inputDC)) {
                        return info.getDateCode();
                    }
                } else {
                    if (info.getQty().subtract(info.getPass()).doubleValue() > 0 && compareDatecode(info.getDateCode(), inputDC)) {
                        return info.getDateCode();
                    }
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

    protected void doSendingProcess() {
        String sn = null;

        switch (mode) {
            case DC_PN:
                processInfoHelper.setDateCode(getDateCode());
                if (rbPnEquation.isChecked())
                    processInfoHelper.setQty(BigDecimal.valueOf(getInputQty(txtPnMultiplier, txtPnMultiplicand, txtPnAddend)));
                else
                    processInfoHelper.setQty(BigDecimal.valueOf(getInputQty(txtPnQty)));
                break;
            case DC_QR_CODE:
                processInfoHelper.setReelID(getReelId().substring(0, 28));
                processInfoHelper.setDateCode(getDateCode());
                processInfoHelper.setQty(BigDecimal.valueOf(getInputQty(txtDcQrCode)));
                break;
            case DC_REEL_ID:
                processInfoHelper.setReelID(getReelId().substring(0, 28));
                processInfoHelper.setDateCode(getDateCode());
                if (rbReelIdEquation.isChecked())
                    processInfoHelper.setQty(BigDecimal.valueOf(getInputQty(txtReelIdMultiplier, txtReelIdMultiplicand, txtReelIdAddend)));
                else
                    processInfoHelper.setQty(BigDecimal.valueOf(getInputQty(txtReelIdQty)));
                break;
            case NO:
                if (rbNoEquation.isChecked())
                    processInfoHelper.setQty(BigDecimal.valueOf(getInputQty(txtNoMultiplier, txtNoMultiplicand, txtNoAddend)));
                else
                    processInfoHelper.setQty(BigDecimal.valueOf(getInputQty(txtNoQty)));
                break;
            case SN_PALLET:
                if (rbPalletSN.isChecked()) {
                    sn = txtPalletSN.getText().toString().trim();
                    processInfoHelper.setSerialNo(sn);
                    processInfoHelper.setType(3);
                } else {
                    sn = txtPalletCN.getText().toString().trim();
                    processInfoHelper.setSerialNo(sn);
                    processInfoHelper.setType(7);
                }
                break;
            case SN_BOX:
                if (rbBoxSN.isChecked()) {
                    sn = txtBoxSN.getText().toString().trim();
                    processInfoHelper.setSerialNo(sn);
                    processInfoHelper.setType(2);
                } else {
                    sn = txtBoxCN.getText().toString().trim();
                    processInfoHelper.setSerialNo(sn);
                    processInfoHelper.setType(6);
                }
                break;
            case SN_SN:
                sn = txtSnSN.getText().toString().trim();
                processInfoHelper.setSerialNo(txtSnSN.getText().toString().trim());
                processInfoHelper.setType(1);
                break;
            case SN_QR_CODE:
                sn = getSN(txtSnQrCode.getText().toString().trim());
                processInfoHelper.setSerialNo(sn);
                processInfoHelper.setType(2);
            default:
                return;
        }

        if (processInfoHelper.getControl().equals("SN")) {
            if (checkExistInList(sn)) {
                Toast.makeText(getApplicationContext(), getString(R.string.sn_box_has_finished), Toast.LENGTH_LONG).show();
            } else {
                dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_querying), true);
                new DoCheckSn().execute(0);
            }
        } else {
            setQuantity();
            setQuantityTitle();
            setListData();
            cleanData();
            Toast.makeText(getApplicationContext(), getString(R.string.process_ok), Toast.LENGTH_SHORT).show();
            checkQty();
        }
    }

    private boolean checkExistInList(String value) {
        if (!TextUtils.isEmpty(value) && snInfoList != null && snInfoList.size() > 0) {
            for (TransferSnInfo item : snInfoList) {
                if (item.getValue().equals(value)) {
                    return true;
                }
            }
        }

        return false;
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

    private void showLayout(LayoutType type) {
        mode = type;
        rgDC.setVisibility(View.INVISIBLE);
        rgSN.setVisibility(View.INVISIBLE);
        lPalletLayout.setVisibility(View.GONE);
        lNoLayout.setVisibility(View.GONE);
        lDcQrCodeLayout.setVisibility(View.GONE);
        lReelIDLayout.setVisibility(View.GONE);
        lPNLayout.setVisibility(View.GONE);
        lBoxLayout.setVisibility(View.GONE);
        lSNLayout.setVisibility(View.GONE);
        lSnQrCodeLayout.setVisibility(View.GONE);

        switch (type) {
            case DC_PN:
                rgDC.setVisibility(View.VISIBLE);
                lPNLayout.setVisibility(View.VISIBLE);
                //rbPnEquation.setChecked(true);
                rbPnQty.setChecked(true);
                txtPnPN.requestFocus();
                break;
            case DC_QR_CODE:
                rgDC.setVisibility(View.VISIBLE);
                lDcQrCodeLayout.setVisibility(View.VISIBLE);
                rbDcQrCode.setChecked(true);
                txtDcQrCode.requestFocus();
                break;
            case DC_REEL_ID:
                rgDC.setVisibility(View.VISIBLE);
                lReelIDLayout.setVisibility(View.VISIBLE);
                //rbReelIdEquation.setChecked(true);
                rbReelIdQty.setChecked(true);
                txtReelID.requestFocus();
                break;
            case NO:
                lNoLayout.setVisibility(View.VISIBLE);
                //rbNoEquation.setChecked(true);
                rbNoQty.setChecked(true);
                txtNoPN.requestFocus();
                break;
            case SN_PALLET:
                rgSN.setVisibility(View.VISIBLE);
                lPalletLayout.setVisibility(View.VISIBLE);
                rbPalletSN.setChecked(true);
                break;
            case SN_BOX:
                rgSN.setVisibility(View.VISIBLE);
                lBoxLayout.setVisibility(View.VISIBLE);
                rbBoxSN.setChecked(true);
                break;
            case SN_SN:
                rgSN.setVisibility(View.VISIBLE);
                lSNLayout.setVisibility(View.VISIBLE);
                txtSnSN.requestFocus();
                break;
            case SN_QR_CODE:
                rgSN.setVisibility(View.VISIBLE);
                lSnQrCodeLayout.setVisibility(View.VISIBLE);
                txtSnQrCode.requestFocus();
                break;
            default:
                break;
        }
    }

    private void returnPage() {
        hideKeyboard();

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
            case SN_QR_CODE:
                rgSN.clearCheck();
                txtSnQrCode.setText("");
                break;
            default:
                break;
        }

        mConnection.setText("");
        mConnection.setTextColor(Color.WHITE);
        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        errorInfo = "";
        setQuantityTitle();
        finish();
    }

    private void setQuantity() {
        BigDecimal quantity = processInfoHelper.getQty();

        if (processInfoHelper.getTransQty() == null) {
            processInfoHelper.setTransQty(BigDecimal.valueOf(quantity.doubleValue()));
        } else {
            processInfoHelper.setTransQty(processInfoHelper.getTransQty().add(quantity));
        }

        txtTransferQty.setText(Util.fmt(processInfoHelper.getTransQty()));

        if (processInfoHelper.getDateCodeInfo() != null && processInfoHelper.getDateCodeInfo().length > 0) {
            for (TransferDateCodeInfo info : processInfoHelper.getDateCodeInfo()) {
                if (info.getDateCode().equals(processInfoHelper.getDateCode())) {
                    if (info.getPass() == null) {
                        info.setPass(BigDecimal.valueOf(quantity.doubleValue()));
                    } else {
                        info.setPass(info.getPass().add(quantity));
                    }
                    break;
                }
            }
        }
    }

    private void checkQty() {
        BigDecimal quantity;

        if (processInfoHelper.getTransQty() == null) {
            quantity = BigDecimal.valueOf(0);
        } else {
            quantity = processInfoHelper.getTransQty();
        }

        if (processInfoHelper.getInventory().subtract(quantity).doubleValue() <= 0) {
            Toast.makeText(getApplication(),
                    getString(R.string.sku) + processInfoHelper.getPartNo() + getString(R.string.Processed_OK_plz_perform_debit),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void onBackPressed() {
        // do something here and don't write super.onBackPressed()
        Log.d(TAG, "onClick Hardware Back Button");
        returnPage();
    }

    private void doReturn() {
        finish();
    }

    private boolean checkSnInfo() {
        BigDecimal quantity = processInfoHelper.getQty();

        if (processInfoHelper.getTransQty() == null) {
            if (processInfoHelper.getInventory().subtract(quantity).doubleValue() < 0) {
                return false;
            }
        } else {
            if (processInfoHelper.getInventory().subtract(processInfoHelper.getTransQty()).subtract(quantity).doubleValue() < 0) {
                return false;
            }
        }

        return true;
    }

    private void insertIntoList() {
        if (snInfoList == null) {
            snInfoList = new ArrayList<>();
        }

        TransferSnInfo item = new TransferSnInfo();
        item.setValue(processInfoHelper.getSerialNo());
        item.setType(processInfoHelper.getType());
        item.setQty(processInfoHelper.getQty());
        snInfoList.add(item);
        processInfoHelper.setSnInfo(snInfoList);
    }

    enum LayoutType {
        NONE, DC_PN, DC_REEL_ID, NO, SN_PALLET, SN_BOX, SN_SN, SN_QR_CODE, DC_QR_CODE
    }

    static class DcViewHolder {
        public TextView txtDateCode;
        public TextView txtPass;
    }

    static class SnViewHolder {
        public TextView txtType;
        public TextView txtValue;
        public TextView txtQty;
    }

    public class DcAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        private DcAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DcViewHolder holder;

            if (convertView == null) {
                holder = new DcViewHolder();
                convertView = mInflater.inflate(R.layout.dc_item, null);
                holder.txtDateCode = convertView.findViewById(R.id.txt_datecode);
                holder.txtPass = convertView.findViewById(R.id.txt_pass);
                convertView.setTag(holder);
            } else {
                holder = (DcViewHolder) convertView.getTag();
            }

            convertView.setVisibility(View.VISIBLE);
            holder.txtDateCode.setText(getString(R.string.label_datecode_qty, data.get(position).get("datecode"), Util.fmt((BigDecimal) data.get(position).get("qty"))));
            holder.txtPass.setText(getString(R.string.label_datecode_pass, Util.fmt((BigDecimal) data.get(position).get("pass"))));
            return convertView;
        }
    }


    public class SnAdapter extends BaseAdapter {
        private LayoutInflater mInflater = null;

        private SnAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SnViewHolder holder;

            if (convertView == null) {
                holder = new SnViewHolder();
                convertView = mInflater.inflate(R.layout.sn_item, null);
                holder.txtType = convertView.findViewById(R.id.txt_type);
                holder.txtValue = convertView.findViewById(R.id.txt_value);
                holder.txtQty = convertView.findViewById(R.id.txt_qty);
                convertView.setTag(holder);
            } else {
                holder = (SnViewHolder) convertView.getTag();
            }

            convertView.setVisibility(View.VISIBLE);
            holder.txtType.setText((String) data.get(position).get("type"));
            holder.txtValue.setText((String) data.get(position).get("value"));
            holder.txtQty.setText(Util.fmt((BigDecimal) data.get(position).get("qty")));
            return convertView;
        }
    }

    private class DoDeduct extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            publishProgress(getString(R.string.Debit_processing));

            if (processInfoHelper.getControl().equals("SN")) {
                AppController.debug("Do transfer process from " + AppController.getServerInfo()
                        + AppController.getProperties("TransferItemSn"));
                return transferHandler.doTransferItemSn(processInfoHelper);
            } else {
                AppController.debug("Do transfer process from " + AppController.getServerInfo()
                        + AppController.getProperties("TransferItemNoDc"));
                return transferHandler.doTransferItemNoDc(processInfoHelper);
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
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                processInfoHelper = (TransferProcessInfoHelper) result;

                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    Toast.makeText(getApplicationContext(), getString(R.string.Deduction_completed), Toast.LENGTH_SHORT).show();
                    doReturn();
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
//                if (processInfoHelper.getInventory().doubleValue() > 0) {
//                    setQuantityTitle();
//                    setListData();
//                    cleanData();
//                } else {
//                    doReturn();
//                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class DoCheckSn extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            publishProgress(getString(R.string.data_querying));
            AppController.debug("Check SN INFO from " + AppController.getServerInfo()
                    + AppController.getProperties("CheckSn"));
            return transferHandler.doCheckSn(processInfoHelper);
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
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    processInfoHelper = (TransferProcessInfoHelper) result;

                    if (checkSnInfo()) {
                        insertIntoList();
                        setQuantity();
                        setQuantityTitle();
                        setListData();
                        cleanData();
                        Toast.makeText(getApplicationContext(), getString(R.string.process_ok), Toast.LENGTH_SHORT).show();
                        checkQty();
                    } else {
                        Toast.makeText(getApplication(), getString(R.string.sku) + processInfoHelper.getPartNo() + getString(R.string.cant_transfer_qty_over_stock), Toast.LENGTH_LONG).show();
                    }
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
