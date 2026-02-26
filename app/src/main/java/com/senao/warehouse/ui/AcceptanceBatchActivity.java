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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.ConditionHelper;
import com.senao.warehouse.handler.ConditionCheckHandler;
import com.senao.warehouse.util.ReturnCode;

public class AcceptanceBatchActivity extends Activity implements View.OnClickListener {
    public static final int REGULAR = 0;
    public static final int OUTSOURCING = 1;
    private EditText txtWhNo, txtPartNo, txtReceiptsNo, txtTrxId, txtSubinventory, txtLocator, txtVendorCode;
    private RadioButton rbPartNo, rbReceiptsNo, rbTrxId, rbVendorCode;
    private Button btnReturn, btnConfirm, btnCancel;
    private TextView mConnection, txtTitle;
    private String errorInfo = "";
    private boolean isCheckChanged;
    private ProgressDialog dialog;
    private ConditionHelper conditionHelper;
    private ConditionCheckHandler handler;
    private boolean isWhNoChecked = true;
    private boolean isPartNoChecked = true;
    private boolean isReceiptsNoChecked = true;
    private boolean isTrxIdChecked = true;
    private boolean isVendorCodeChecked = true;
    private int accType;

    private MyCallBack mCallBack = new MyCallBack() {
        @Override
        public void onTaskComplete() {
            if (checkConditions())
                goToListPage();
        }
    };

    private CompoundButton.OnCheckedChangeListener rbListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                isCheckChanged = true;

                if (buttonView.equals(rbPartNo)) {
                    txtLocator.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    txtLocator.setNextFocusDownId(R.id.edittext_import_part_number);
                    txtPartNo.setEnabled(true);
                    txtPartNo.requestFocus();

                    if (rbReceiptsNo.isChecked()) {
                        txtPartNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNo.setNextFocusDownId(R.id.edittext_import_receipt_number);
                    } else if (rbTrxId.isChecked()) {
                        txtPartNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                    } else if (rbVendorCode.isChecked()) {
                        txtPartNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNo.setNextFocusDownId(R.id.edittext_import_vendor_code);
                    } else {
                        txtPartNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtPartNo.setNextFocusDownId(R.id.button_confirm);
                    }
                } else if (buttonView.equals(rbReceiptsNo)) {
                    txtReceiptsNo.setEnabled(true);
                    txtReceiptsNo.requestFocus();

                    if (rbPartNo.isChecked()) {
                        txtPartNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNo.setNextFocusDownId(R.id.edittext_import_receipt_number);
                    } else {
                        txtLocator.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtLocator.setNextFocusDownId(R.id.edittext_import_receipt_number);
                    }

                    if (rbTrxId.isChecked()) {
                        txtReceiptsNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtReceiptsNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                    } else if (rbVendorCode.isChecked()) {
                        txtReceiptsNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtReceiptsNo.setNextFocusDownId(R.id.edittext_import_vendor_code);
                    } else {
                        txtReceiptsNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtReceiptsNo.setNextFocusDownId(R.id.button_confirm);
                    }
                } else if (buttonView.equals(rbTrxId)) {
                    txtTrxId.setEnabled(true);
                    txtTrxId.requestFocus();

                    if (rbReceiptsNo.isChecked()) {
                        txtReceiptsNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtReceiptsNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                    } else if (rbPartNo.isChecked()) {
                        txtPartNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                    } else {
                        txtLocator.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtLocator.setNextFocusDownId(R.id.edittext_import_receiving_id);
                    }

                    if (rbVendorCode.isChecked()) {
                        txtTrxId.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtTrxId.setNextFocusDownId(R.id.edittext_import_vendor_code);
                    } else {
                        txtTrxId.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtTrxId.setNextFocusDownId(R.id.button_confirm);
                    }
                } else {
                    txtVendorCode.setEnabled(true);
                    txtVendorCode.requestFocus();

                    if (rbTrxId.isChecked()) {
                        txtTrxId.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtTrxId.setNextFocusDownId(R.id.edittext_import_vendor_code);
                    } else if (rbReceiptsNo.isChecked()) {
                        txtReceiptsNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtReceiptsNo.setNextFocusDownId(R.id.edittext_import_vendor_code);
                    } else if (rbPartNo.isChecked()) {
                        txtPartNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNo.setNextFocusDownId(R.id.edittext_import_vendor_code);
                    } else {
                        txtLocator.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtLocator.setNextFocusDownId(R.id.edittext_import_vendor_code);
                    }
                }
            } else {
                if (buttonView.equals(rbPartNo)) {
                    if (rbReceiptsNo.isChecked()) {
                        txtLocator.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtLocator.setNextFocusDownId(R.id.edittext_import_receipt_number);
                    } else if (rbTrxId.isChecked()) {
                        txtLocator.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtLocator.setNextFocusDownId(R.id.edittext_import_receiving_id);
                    } else if (rbVendorCode.isChecked()) {
                        txtLocator.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtLocator.setNextFocusDownId(R.id.edittext_import_vendor_code);
                    } else {
                        txtLocator.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtLocator.setNextFocusDownId(R.id.button_confirm);
                    }
                } else if (buttonView.equals(rbReceiptsNo)) {
                    if (rbPartNo.isChecked()) {
                        if (rbTrxId.isChecked()) {
                            txtPartNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                            txtPartNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                        } else if (rbVendorCode.isChecked()) {
                            txtPartNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                            txtPartNo.setNextFocusDownId(R.id.edittext_import_vendor_code);
                        } else {
                            txtPartNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtPartNo.setNextFocusDownId(R.id.button_confirm);
                        }
                    } else {
                        if (rbTrxId.isChecked()) {
                            txtLocator.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                            txtLocator.setNextFocusDownId(R.id.edittext_import_receiving_id);
                        } else if (rbVendorCode.isChecked()) {
                            txtLocator.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                            txtLocator.setNextFocusDownId(R.id.edittext_import_vendor_code);
                        } else {
                            txtLocator.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtLocator.setNextFocusDownId(R.id.button_confirm);
                        }
                    }
                } else if (buttonView.equals(rbTrxId)) {
                    if (rbReceiptsNo.isChecked()) {
                        if (rbVendorCode.isChecked()) {
                            txtReceiptsNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                            txtReceiptsNo.setNextFocusDownId(R.id.edittext_import_vendor_code);
                        } else {
                            txtReceiptsNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtReceiptsNo.setNextFocusDownId(R.id.button_confirm);
                        }
                    } else if (rbPartNo.isChecked()) {
                        if (rbVendorCode.isChecked()) {
                            txtPartNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                            txtPartNo.setNextFocusDownId(R.id.edittext_import_vendor_code);
                        } else {
                            txtPartNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtPartNo.setNextFocusDownId(R.id.button_confirm);
                        }
                    } else {
                        if (rbVendorCode.isChecked()) {
                            txtLocator.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                            txtLocator.setNextFocusDownId(R.id.edittext_import_vendor_code);
                        } else {
                            txtLocator.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtLocator.setNextFocusDownId(R.id.button_confirm);
                        }
                    }
                } else {
                    if (rbTrxId.isChecked()) {
                        txtTrxId.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtTrxId.setNextFocusDownId(R.id.button_confirm);
                    } else if (rbReceiptsNo.isChecked()) {
                        txtReceiptsNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtReceiptsNo.setNextFocusDownId(R.id.button_confirm);
                    } else if (rbPartNo.isChecked()) {
                        txtPartNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtPartNo.setNextFocusDownId(R.id.button_confirm);
                    } else {
                        txtLocator.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtLocator.setNextFocusDownId(R.id.button_confirm);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceptance_batch);

        txtTitle = findViewById(R.id.ap_title);

        accType = getIntent().getIntExtra("TYPE", 0);
        if (accType == OUTSOURCING) {
            txtTitle.setText(R.string.label_acceptance_batch_outsourcing);
        }

        txtWhNo = findViewById(R.id.edittext_import_wh_number);
        /*txtWhNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString().trim()))
                    isWhNoChecked = true;
                else
                    isWhNoChecked = false;
            }
        });*/

        txtSubinventory = findViewById(R.id.edittext_import_subinventory);

        txtLocator = findViewById(R.id.edittext_import_locator);
        txtLocator.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtLocator.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (rbPartNo.isChecked()) {
                        txtPartNo.requestFocus();
                    } else if (rbReceiptsNo.isChecked()) {
                        txtReceiptsNo.requestFocus();
                    } else if (rbTrxId.isChecked()) {
                        txtTrxId.requestFocus();
                    } else if (rbTrxId.isChecked()) {
                        txtTrxId.requestFocus();
                    }else if (rbVendorCode.isChecked()) {
                        txtVendorCode.requestFocus();
                    }

                    //20170428
                    /*if (!isWhNoChecked) {
                        doCheckWareHouseNo(v.getText().toString().trim(), null);
                    }*/

                    return true;
                } else if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();

                    //20170428
                    /*if (!isWhNoChecked) {
                        doCheckWareHouseNo(v.getText().toString().trim(), null);
                    }*/

                    return true;
                }

                return false;
            }
        });

        txtPartNo = findViewById(R.id.edittext_import_part_number);
        txtPartNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (rbReceiptsNo.isChecked()) {
                        txtReceiptsNo.requestFocus();
                    } else if (rbTrxId.isChecked()) {
                        txtTrxId.requestFocus();
                    } else if (rbVendorCode.isChecked()) {
                        txtVendorCode.requestFocus();
                    }

                    /*if (!isPartNoChecked) {
                        doCheckPartNo(v.getText().toString().trim(), null);
                    }*/

                    return true;
                } else if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();

                    /*if (!isPartNoChecked) {
                        doCheckPartNo(v.getText().toString().trim(), null);
                    }*/

                    return true;
                }

                return false;
            }
        });

        /*txtPartNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString().trim()))
                    isPartNoChecked = true;
                else
                    isPartNoChecked = false;
            }
        });*/

        txtReceiptsNo = findViewById(R.id.edittext_import_receipt_number);
        txtReceiptsNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (rbTrxId.isChecked()) {
                        txtTrxId.requestFocus();
                    } else if (rbVendorCode.isChecked()) {
                        txtVendorCode.requestFocus();
                    }

                    /*if (!isReceiptsNoChecked) {
                        doCheckReceiptsNo(v.getText().toString().trim(), null);
                    }*/

                    return true;
                } else if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();

                    /*if (!isReceiptsNoChecked) {
                        doCheckReceiptsNo(v.getText().toString().trim(), null);
                    }*/

                    return true;
                }

                return false;
            }
        });

        txtReceiptsNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString().trim()))
                    isReceiptsNoChecked = true;
                else
                    isReceiptsNoChecked = false;
            }
        });

        txtTrxId = findViewById(R.id.edittext_import_receiving_id);
        txtTrxId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString().trim()))
                    isTrxIdChecked = true;
                else
                    isTrxIdChecked = false;
            }
        });

        txtTrxId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (rbVendorCode.isChecked()) {
                        txtVendorCode.requestFocus();
                    }

                    /*if (!isTrxIdChecked) {
                        doCheckTrxId(v.getText().toString().trim(), null);
                    }*/

                    return true;
                } else if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();

                    /*if (!isTrxIdChecked) {
                        doCheckTrxId(v.getText().toString().trim(), null);
                    }*/

                    return true;
                }

                return false;
            }
        });

        txtVendorCode = findViewById(R.id.edittext_import_vendor_code);
        txtVendorCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString().trim()))
                    isVendorCodeChecked = true;
                else
                    isVendorCodeChecked = false;
            }
        });

        txtVendorCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();

                    /*if (!isVendorCodeChecked) {
                        doCheckVendorCode(v.getText().toString().trim(), null);
                    }*/

                    return true;
                }

                return false;
            }
        });

        rbPartNo = findViewById(R.id.radio_part_number);
        rbPartNo.setOnCheckedChangeListener(rbListener);
        rbPartNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rbPartNo.isChecked() && !isCheckChanged) {
                    rbPartNo.setChecked(false);
                    txtPartNo.setText("");
                    txtPartNo.setEnabled(false);
                }

                isCheckChanged = false;
            }
        });

        rbReceiptsNo = findViewById(R.id.radio_receipt_no);
        rbReceiptsNo.setOnCheckedChangeListener(rbListener);
        rbReceiptsNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rbReceiptsNo.isChecked() && !isCheckChanged) {
                    rbReceiptsNo.setChecked(false);
                    txtReceiptsNo.setText("");
                    txtReceiptsNo.setEnabled(false);
                }

                isCheckChanged = false;
            }
        });

        rbTrxId = findViewById(R.id.radio_receiving_id);
        rbTrxId.setOnCheckedChangeListener(rbListener);
        rbTrxId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rbTrxId.isChecked() && !isCheckChanged) {
                    rbTrxId.setChecked(false);
                    txtTrxId.setText("");
                    txtTrxId.setEnabled(false);
                }

                isCheckChanged = false;
            }
        });

        rbVendorCode = findViewById(R.id.radio_vendor_code);
        rbVendorCode.setOnCheckedChangeListener(rbListener);
        rbVendorCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rbVendorCode.isChecked() && !isCheckChanged) {
                    rbVendorCode.setChecked(false);
                    txtVendorCode.setText("");
                    txtVendorCode.setEnabled(false);
                }

                isCheckChanged = false;
            }
        });

        btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        btnConfirm = findViewById(R.id.button_confirm);
        btnConfirm.setOnClickListener(this);

        btnCancel = findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(this);

        mConnection = findViewById(R.id.label_status);

        handler = new ConditionCheckHandler();

        cleanData();
    }

    private void doCheckTrxId(String trxId, MyCallBack callBack) {
        hideKeyboard();
        conditionHelper = new ConditionHelper();
        conditionHelper.setTrxIdNo(trxId);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.Receipt_id_number_data_checking), true);
        new CheckTrxId(callBack).execute(0);
    }

    private void doCheckVendorCode(String vendorCode, MyCallBack callBack) {
        hideKeyboard();
        conditionHelper = new ConditionHelper();
        conditionHelper.setVendorCode(vendorCode);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.Receipt_id_number_data_checking), true);
        new CheckVendorCode(callBack).execute(0);
    }

    private void doCheckReceiptsNo(String receiptsNo, MyCallBack callBack) {
        hideKeyboard();
        conditionHelper = new ConditionHelper();
        conditionHelper.setReceiptsNo(receiptsNo);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.Material_receipt_number_data_checking), true);
        new CheckReceiptsNo(callBack).execute(0);
    }

    private void doCheckPartNo(String partNo, MyCallBack callBack) {
        hideKeyboard();
        conditionHelper = new ConditionHelper();
        conditionHelper.setPartNo(partNo);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.sku_verifying), true);
        new CheckPartNo(callBack).execute(0);
    }

    private void cleanData() {
        txtWhNo.setText("");
        txtSubinventory.setText("");
        txtLocator.setText("");
        txtPartNo.setText("");
        txtPartNo.setEnabled(false);
        rbPartNo.setChecked(false);
        txtReceiptsNo.setText("");
        txtReceiptsNo.setEnabled(false);
        rbReceiptsNo.setChecked(false);
        txtTrxId.setText("");
        txtTrxId.setEnabled(false);
        rbTrxId.setChecked(false);
        txtVendorCode.setText("");
        txtVendorCode.setEnabled(false);
        rbVendorCode.setChecked(false);
    }

    private boolean checkConditions() {
        if (txtWhNo.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_warehouse_clerk), Toast.LENGTH_SHORT).show();
            txtWhNo.requestFocus();
            return false;
        }

        if (txtSubinventory.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_sub), Toast.LENGTH_SHORT).show();
            txtSubinventory.requestFocus();
            return false;
        }

        if (txtLocator.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_loc), Toast.LENGTH_SHORT).show();
            txtLocator.requestFocus();
            return false;
        }

        if (rbPartNo.isChecked() && txtPartNo.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_sku), Toast.LENGTH_SHORT).show();
            txtPartNo.requestFocus();
            return false;
        }

        if (rbReceiptsNo.isChecked() && txtReceiptsNo.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_recived_num), Toast.LENGTH_SHORT).show();
            txtReceiptsNo.requestFocus();
            return false;
        }

        if (rbTrxId.isChecked() && txtTrxId.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_recived_id), Toast.LENGTH_SHORT).show();
            txtTrxId.requestFocus();
            return false;
        }

        if (rbVendorCode.isChecked() && txtVendorCode.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_vc), Toast.LENGTH_SHORT).show();
            txtVendorCode.requestFocus();
            return false;
        }

        //20170428
        /*if (!isWhNoChecked) {
            doCheckWareHouseNo(txtWhNo.getText().toString().trim(), mCallBack);
            return false;
        } else if (!isPartNoChecked) {
            doCheckPartNo(txtPartNo.getText().toString().trim(), mCallBack);
            return false;
        } else if (!isReceiptsNoChecked) {
            doCheckReceiptsNo(txtReceiptsNo.getText().toString().trim(), mCallBack);
            return false;
        } else if (!isTrxIdChecked) {
            doCheckTrxId(txtTrxId.getText().toString().trim(), mCallBack);
            return false;
        } else if (!isVendorCodeChecked) {
            doCheckVendorCode(txtVendorCode.getText().toString().trim(), mCallBack);
            return false;
        } else {
            return true;
        }*/

        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button_return) {
            finish();
        } else if (id == R.id.button_confirm) {
            if (checkConditions())
                goToListPage();
        } else if (id == R.id.button_cancel) {
            cleanData();
        } else if (id == R.id.label_status) {
            if (!TextUtils.isEmpty(errorInfo)) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(AcceptanceBatchActivity.this);
                dialog.setTitle("Error Msg");
                dialog.setMessage(errorInfo);
                dialog.setIcon(android.R.drawable.ic_dialog_alert);
                dialog.setCancelable(false);
                dialog.setNegativeButton( getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.dismiss();
                            }
                        });
                dialog.show();
            }
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtWhNo.getWindowToken(), 0);
    }

    private void doCheckWareHouseNo(String warehouseNo, MyCallBack callBack) {
        hideKeyboard();
        conditionHelper = new ConditionHelper();
        conditionHelper.setWareHouseNo(warehouseNo);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.wareclerk_verify_data), true);
        new CheckWareHouseNo(callBack).execute(0);
    }

    private void goToListPage() {
        conditionHelper = new ConditionHelper();
        conditionHelper.setWareHouseNo(txtWhNo.getText().toString().trim());
        conditionHelper.setSubinventory(txtSubinventory.getText().toString().trim());
        conditionHelper.setLocator(txtLocator.getText().toString().trim());

        if (rbPartNo.isChecked())
            conditionHelper.setPartNo(txtPartNo.getText().toString().trim());
        else
            conditionHelper.setPartNo(null);

        if (rbReceiptsNo.isChecked())
            conditionHelper.setReceiptsNo(txtReceiptsNo.getText().toString().trim());
        else
            conditionHelper.setReceiptsNo(null);

        if (rbTrxId.isChecked())
            conditionHelper.setTrxIdNo(txtTrxId.getText().toString().trim());
        else
            conditionHelper.setTrxIdNo(null);

        if (rbVendorCode.isChecked())
            conditionHelper.setVendorCode(txtVendorCode.getText().toString().trim());
        else
            conditionHelper.setVendorCode(null);

        Intent intent = new Intent(this, AcceptanceBatchListActivity.class);
        intent.putExtra("CONDITION_INFO", new Gson().toJson(conditionHelper));
        intent.putExtra("TYPE", accType);
        startActivity(intent);
    }

    public interface MyCallBack {
        void onTaskComplete();
    }

    private class CheckTrxId extends AsyncTask<Integer, String, BasicHelper> {
        private MyCallBack callBack;

        public CheckTrxId(MyCallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check Trx Id from " + AppController.getServerInfo() + AppController.getProperties("CheckTrxId"));
            publishProgress("收料ID資料核對中...");
            return handler.doCheckTrxId(conditionHelper);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            //Things to be done while execution of long running operation is in
            //progress. For example updating ProgessDialog
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
                    conditionHelper = (ConditionHelper) result;
                    isTrxIdChecked = true;

                    if (callBack != null)
                        callBack.onTaskComplete();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    txtTrxId.requestFocus();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                txtTrxId.requestFocus();
            }
        }
    }


    private class CheckVendorCode extends AsyncTask<Integer, String, BasicHelper> {
        private MyCallBack callBack;

        public CheckVendorCode(MyCallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check Vendor code from " + AppController.getServerInfo() + AppController.getProperties("CheckVendorCode"));
            publishProgress(getString(R.string.vc_data_chking));
            return handler.doCheckVendorCode(conditionHelper);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            //Things to be done while execution of long running operation is in
            //progress. For example updating ProgessDialog
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
                    conditionHelper = (ConditionHelper) result;
                    isVendorCodeChecked = true;

                    if (callBack != null)
                        callBack.onTaskComplete();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    txtVendorCode.requestFocus();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                txtVendorCode.requestFocus();
            }
        }
    }

    private class CheckReceiptsNo extends AsyncTask<Integer, String, BasicHelper> {
        private MyCallBack callBack;

        public CheckReceiptsNo(MyCallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check Receipts No from " + AppController.getServerInfo() + AppController.getProperties("CheckReceiptsNo"));
            publishProgress(getString(R.string.Material_receipt_number_data_checking));
            return handler.doCheckReceiptsNo(conditionHelper);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            //Things to be done while execution of long running operation is in
            //progress. For example updating ProgessDialog
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
                    conditionHelper = (ConditionHelper) result;
                    isReceiptsNoChecked = true;

                    if (callBack != null)
                        callBack.onTaskComplete();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    txtReceiptsNo.requestFocus();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                txtReceiptsNo.requestFocus();
            }
        }
    }

    private class CheckPartNo extends AsyncTask<Integer, String, BasicHelper> {
        private MyCallBack callBack;

        public CheckPartNo(MyCallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check Part No from " + AppController.getServerInfo() + AppController.getProperties("CheckPartNo"));
            publishProgress(getString(R.string.sku_verifying));
            return handler.doCheckPartNo(conditionHelper);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            //Things to be done while execution of long running operation is in
            //progress. For example updating ProgessDialog
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
                    conditionHelper = (ConditionHelper) result;
                    isPartNoChecked = true;

                    if (callBack != null)
                        callBack.onTaskComplete();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    txtPartNo.requestFocus();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                txtPartNo.requestFocus();
            }
        }
    }

    private class CheckWareHouseNo extends AsyncTask<Integer, String, BasicHelper> {
        private MyCallBack callBack;

        public CheckWareHouseNo(MyCallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check Vendor Info from " + AppController.getServerInfo() + AppController.getProperties("CheckWareHouseNo"));
            publishProgress(getString(R.string.wareclerk_verify_data));

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return handler.doCheckWareHouseNo(conditionHelper);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            //Things to be done while execution of long running operation is in
            //progress. For example updating ProgessDialog
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
                    conditionHelper = (ConditionHelper) result;
                    isWhNoChecked = true;

                    if (callBack != null) {
                        callBack.onTaskComplete();
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
                    txtWhNo.requestFocus();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                txtWhNo.requestFocus();
            }
        }
    }
}
