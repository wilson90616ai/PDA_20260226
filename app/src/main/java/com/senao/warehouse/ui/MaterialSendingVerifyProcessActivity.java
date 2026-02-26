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
import com.senao.warehouse.database.SendOnHandQtyInfo;
import com.senao.warehouse.database.SendProcessInfoHelper;
import com.senao.warehouse.database.SendingInfoHelper;
import com.senao.warehouse.handler.SendingVerifyHandler;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.util.Util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MaterialSendingVerifyProcessActivity extends Activity {

    private static final String TAG = MaterialSendingVerifyProcessActivity.class.getSimpleName();

    private TextView mConnection;
    private Button btnReturn, btnConfim, btnCancel;
    private ProgressDialog dialog;
    private String errorInfo = "";
    private TextView txtPartNo, txtWoNo, txtLineNumber, txtTotalSendQty, txtSendQty, txtUnsendQty, txtTempQty;
    private LinearLayout lPalletLayout,
            lNoLayout, lBoxLayout,
            lSNLayout, lSnQrCodeLayout;
    private RadioGroup rgSN;
    private RadioButton rbPalletSN, rbBoxSN, rbPalletCN,
            rbBoxCN;
    private EditText txtPalletCN, txtBoxCN,
            txtSnSN, txtBoxSN, txtPalletSN, txtNoPN, txtNoAddend,
            txtNoMultiplier, txtNoMultiplicand, txtNoQty, txtSnQrCode;
    private LayoutType mode = LayoutType.NONE;
    private SendProcessInfoHelper processInfoHelper;
    private boolean needRefresh = false;
    private RadioButton rbNoEquation;
    private RadioButton rbNoQty;
    private SendingVerifyHandler sendingVerifyHandler;
    private SendOnHandQtyInfo onHandQtyInfo;
    private SendingInfoHelper sendInfo;
    private OnKeyListener keyListener = new OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_UP)
                    && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Perform action on key press
                Log.d(TAG, "onKeyListener Enter or Action Down" + String.valueOf(v.getId()));
                hideKeyboard();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_sending_verify_process);
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

            processInfoHelper.setWoNo(sendInfo.getWoNo());
            processInfoHelper.setSubinventory(sendInfo.getSubinventory());
            processInfoHelper.setLocator(sendInfo.getLocator());
            sendingVerifyHandler = new SendingVerifyHandler();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_shipping_info), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mConnection =  findViewById(R.id.label_status);
        mConnection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MaterialSendingVerifyProcessActivity.this);
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

        txtLineNumber = findViewById(R.id.txt_line_number);
        txtPartNo = findViewById(R.id.txt_part_no);
        txtWoNo = findViewById(R.id.txt_wo_no);

        txtTotalSendQty = findViewById(R.id.txt_total_sending_qty);
        txtSendQty = findViewById(R.id.txt_send_qty);
        txtUnsendQty = findViewById(R.id.txt_unsend_qty);
        txtTempQty = findViewById(R.id.txt_temp_qty);

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

                switch (mode) {
                    case NO:
                        if (txtNoPN.getText().toString().trim().equals("")) {
                            Toast.makeText(MaterialSendingVerifyProcessActivity.this, getString(R.string.enter_sku), Toast.LENGTH_SHORT).show();
                            txtNoPN.requestFocus();
                        } else if (!txtNoPN.getText().toString().trim().equals(onHandQtyInfo.getPartNo())) {
                            Toast.makeText(MaterialSendingVerifyProcessActivity.this, getString(R.string.sku_incorrect), Toast.LENGTH_SHORT).show();
                            txtNoPN.requestFocus();
                        } else if (rbNoEquation.isChecked()) {
                            if (txtNoMultiplier.getText().toString().trim().equals("")) {
                                Toast.makeText(MaterialSendingVerifyProcessActivity.this, getString(R.string.enter_multiplier), Toast.LENGTH_SHORT).show();
                                txtNoMultiplier.requestFocus();
                            } else if (txtNoMultiplicand.getText().toString().trim().equals("")) {
                                Toast.makeText(MaterialSendingVerifyProcessActivity.this, getString(R.string.enter_multiplicand), Toast.LENGTH_SHORT).show();
                                txtNoMultiplicand.requestFocus();
                            } else if (txtNoAddend.getText().toString().trim().equals("")) {
                                Toast.makeText(MaterialSendingVerifyProcessActivity.this, getString(R.string.enter_add), Toast.LENGTH_SHORT).show();
                                txtNoAddend.requestFocus();
                            } else if (getInputQty(txtNoMultiplier, txtNoMultiplicand, txtNoAddend) <= 0) {
                                Toast.makeText(MaterialSendingVerifyProcessActivity.this, getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtNoMultiplier.requestFocus();
                            } else if (getInputQty(txtNoMultiplier, txtNoMultiplicand, txtNoAddend) > getAvailableQty()) {
                                Toast.makeText(MaterialSendingVerifyProcessActivity.this, getString(R.string.qty_greater_than_stock)+"(" + getAvailableQty() + ")", Toast.LENGTH_SHORT).show();
                                txtNoMultiplier.requestFocus();
                            } else {
                                hideKeyboard();
                                doSendingProcess();
                            }
                        } else {
                            if (txtNoQty.getText().toString().trim().equals("")) {
                                Toast.makeText(MaterialSendingVerifyProcessActivity.this, getString(R.string.enter_qty), Toast.LENGTH_SHORT).show();
                                txtNoQty.requestFocus();
                            } else if (getInputQty(txtNoQty) <= 0) {
                                Toast.makeText(MaterialSendingVerifyProcessActivity.this, getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtNoQty.requestFocus();
                            } else if (getInputQty(txtNoQty) > getAvailableQty()) {
                                Toast.makeText(MaterialSendingVerifyProcessActivity.this, getString(R.string.qty_greater_than_stock)+"(" + getAvailableQty() + ")", Toast.LENGTH_SHORT).show();
                                txtNoQty.requestFocus();
                            } else {
                                hideKeyboard();
                                doSendingProcess();
                            }
                        }
                        break;
                    case SN_PALLET:
                        if (rbPalletSN.isChecked() && txtPalletSN.getText().toString().trim().equals("")) {
                            Toast.makeText(MaterialSendingVerifyProcessActivity.this, getString(R.string.enter_sn), Toast.LENGTH_SHORT).show();
                            txtPalletSN.requestFocus();
                        } else if (rbPalletCN.isChecked() && txtPalletCN.getText().toString().trim().equals("")) {
                            Toast.makeText(MaterialSendingVerifyProcessActivity.this, getString(R.string.enter_box_no), Toast.LENGTH_SHORT).show();
                            txtPalletCN.requestFocus();
                        } else if (!rbPalletSN.isChecked() && !rbPalletCN.isChecked()) {
                            Toast.makeText(MaterialSendingVerifyProcessActivity.this, getString(R.string.choose_sn_box), Toast.LENGTH_SHORT).show();
                        } else {
                            hideKeyboard();
                            doSendingProcess();
                        }
                        break;
                    case SN_BOX:
                        if (rbBoxSN.isChecked() && txtBoxSN.getText().toString().trim().equals("")) {
                            Toast.makeText(MaterialSendingVerifyProcessActivity.this, getString(R.string.enter_sn), Toast.LENGTH_SHORT).show();
                            txtBoxSN.requestFocus();
                        } else if (rbBoxCN.isChecked() && txtBoxCN.getText().toString().trim().equals("")) {
                            Toast.makeText(MaterialSendingVerifyProcessActivity.this, getString(R.string.enter_box_no), Toast.LENGTH_SHORT).show();
                            txtBoxCN.requestFocus();
                        } else if (!rbBoxSN.isChecked() && !rbBoxCN.isChecked()) {
                            Toast.makeText(MaterialSendingVerifyProcessActivity.this, getString(R.string.choose_sn_box), Toast.LENGTH_SHORT).show();
                        } else {
                            hideKeyboard();
                            doSendingProcess();
                        }
                        break;
                    case SN_SN:
                        if (txtSnSN.getText().toString().trim().equals("")) {
                            Toast.makeText(MaterialSendingVerifyProcessActivity.this, getString(R.string.enter_sn), Toast.LENGTH_SHORT).show();
                            txtSnSN.requestFocus();
                        } else {
                            hideKeyboard();
                            doSendingProcess();
                        }
                        break;
                    case SN_QR_CODE:
                        if (txtSnQrCode.getText().toString().trim().equals("")) {
                            Toast.makeText(MaterialSendingVerifyProcessActivity.this, getString(R.string.enter_qrcode), Toast.LENGTH_SHORT).show();
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

        setView();
    }

    protected double getInputQty(EditText edit) {
        return Double.parseDouble(edit.getText().toString().trim());
    }

    private void setQuantity() {
        BigDecimal quantity = processInfoHelper.getQty();
        //onHandQtyInfo.setSentQty(onHandQtyInfo.getSentQty().add(quantity));
        //onHandQtyInfo.setUnsentQty(onHandQtyInfo.getUnsentQty().subtract(quantity));
        //onHandQtyInfo.setOnHandQty(onHandQtyInfo.getOnHandQty().subtract(quantity));
        onHandQtyInfo.setTempQty(onHandQtyInfo.getTempQty().add(quantity));
    }

    private void setQuantityTitle() {
        txtLineNumber.setText(processInfoHelper.getLineNo());
        txtPartNo.setText(onHandQtyInfo.getPartNo());
        txtWoNo.setText(processInfoHelper.getWoNo());
        txtTotalSendQty.setText(Util.fmt(onHandQtyInfo.getTotalSendingQty().doubleValue()));
        txtSendQty.setText(Util.fmt(onHandQtyInfo.getSentQty().doubleValue()));
        txtUnsendQty.setText(Util.fmt(onHandQtyInfo.getUnsentQty().doubleValue()));
        txtTempQty.setText(Util.fmt(onHandQtyInfo.getTempQty().doubleValue()));
    }

    private double getAvailableQty() {
        //return Math.ceil(onHandQtyInfo.getUnsentQty().doubleValue()) > onHandQtyInfo.getOnHandQty().doubleValue() ? onHandQtyInfo.getOnHandQty().doubleValue() : Math.ceil(onHandQtyInfo.getUnsentQty().doubleValue());
        return onHandQtyInfo.getOnHandQty().subtract(onHandQtyInfo.getTempQty()).doubleValue();
    }

    private double getInputQty(EditText multiplier, EditText multiplicand, EditText addEnd) {
        double mul = Double.parseDouble(multiplier.getText().toString().trim());
        double cand = Double.parseDouble(multiplicand.getText().toString().trim());
        double add = Double.parseDouble(addEnd.getText().toString().trim());
        return BigDecimal.valueOf(mul).multiply(BigDecimal.valueOf(cand)).add(BigDecimal.valueOf(add)).doubleValue();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtNoPN.getWindowToken(), 0);
    }

    private void setView() {
        if (onHandQtyInfo.getControl().equals("SN")) {
            if (rgSN.getCheckedRadioButtonId() == -1)
                rgSN.check(R.id.radio_pallet);
        } else
            showLayout(LayoutType.NO);
    }

    private void cleanData() {
        switch (mode) {
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
                txtPalletCN.setText("");

                if (rbPalletSN.isChecked())
                    txtPalletSN.requestFocus();
                else
                    txtPalletCN.requestFocus();
                break;
            case SN_BOX:
                txtBoxSN.setText("");
                txtBoxCN.setText("");

                if (rbBoxSN.isChecked())
                    txtBoxSN.requestFocus();
                else
                    txtBoxCN.requestFocus();
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

    protected void doSendingProcess() {
        String sn;

        switch (mode) {
            case NO:
                processInfoHelper.setSerialNo(null);
                if (onHandQtyInfo.getControl() != null && onHandQtyInfo.getControl().equals("DC")) {
                    processInfoHelper.setType("4");
                } else {
                    processInfoHelper.setType("5");
                }

                if (rbNoEquation.isChecked())
                    processInfoHelper.setQty(BigDecimal.valueOf(getInputQty(txtNoMultiplier, txtNoMultiplicand, txtNoAddend)));
                else
                    processInfoHelper.setQty(BigDecimal.valueOf(getInputQty(txtNoQty)));
                break;
            case SN_PALLET:
                if (rbPalletSN.isChecked()) {
                    sn = txtPalletSN.getText().toString().trim();
                    processInfoHelper.setSerialNo(sn);
                    processInfoHelper.setType("3");
                } else {
                    sn = txtPalletCN.getText().toString().trim();
                    processInfoHelper.setSerialNo(sn);
                    processInfoHelper.setType("7");
                }

                processInfoHelper.setQty(BigDecimal.valueOf(0));
                break;
            case SN_BOX:
                if (rbBoxSN.isChecked()) {
                    sn = txtBoxSN.getText().toString().trim();
                    processInfoHelper.setSerialNo(sn);
                    processInfoHelper.setType("2");
                } else {
                    sn = txtBoxCN.getText().toString().trim();
                    processInfoHelper.setSerialNo(sn);
                    processInfoHelper.setType("6");
                }

                processInfoHelper.setQty(BigDecimal.valueOf(0));
                break;
            case SN_SN:
                processInfoHelper.setSerialNo(txtSnSN.getText().toString().trim());
                processInfoHelper.setType("1");
                processInfoHelper.setQty(BigDecimal.valueOf(0));
                break;
            case SN_QR_CODE:
                sn = getSN(txtSnQrCode.getText().toString().trim());
                processInfoHelper.setSerialNo(sn);
                processInfoHelper.setType("2");
                processInfoHelper.setQty(BigDecimal.valueOf(0));
            default:
                return;
        }

        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.issue_processing), true);
        new DoSendingProcess().execute(0);
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
        rgSN.setVisibility(View.INVISIBLE);
        lPalletLayout.setVisibility(View.GONE);
        lNoLayout.setVisibility(View.GONE);
        lBoxLayout.setVisibility(View.GONE);
        lSNLayout.setVisibility(View.GONE);
        lSnQrCodeLayout.setVisibility(View.GONE);

        switch (type) {
            case NO:
                lNoLayout.setVisibility(View.VISIBLE);
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

        if (needRefresh) {
            doReturn();
        } else {
            finish();
        }
    }

    private void checkReturnAndSetView() {
        if (onHandQtyInfo.getUnsentQty().subtract(onHandQtyInfo.getTempQty()).doubleValue() <= 0
                || onHandQtyInfo.getOnHandQty().subtract(onHandQtyInfo.getTempQty()).doubleValue() == 0) {
            Toast.makeText(getApplication(), getString(R.string.sku) + onHandQtyInfo.getPartNo() + getString(R.string.chk_ok_exec_debit), Toast.LENGTH_LONG).show();
            doReturn();
        }
    }

    public void onBackPressed() {
        // do something here and don't write super.onBackPressed()
        Log.d(TAG, "onClick Hardware Back Button");
        returnPage();
    }

    private void doReturn() {
        setResult(RESULT_OK);
        finish();
    }

    enum LayoutType {
        NONE, NO, SN_PALLET, SN_BOX, SN_SN, SN_QR_CODE
    }

    private class DoSendingProcess extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            publishProgress(getString(R.string.issue_processing));
            AppController.debug("Do sending check process from " + AppController.getServerInfo()
                    + AppController.getProperties("VerifySaveTemp"));
            return sendingVerifyHandler.doVerifySaveTemp(processInfoHelper);
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
                    processInfoHelper = (SendProcessInfoHelper) result;
                    needRefresh = true;
                    setQuantity();
                    setQuantityTitle();
                    cleanData();
                    Toast.makeText(getApplicationContext(), getString(R.string.process_ok), Toast.LENGTH_SHORT).show();
                    checkReturnAndSetView();
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
