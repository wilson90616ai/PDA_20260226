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
import com.senao.warehouse.R;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.WaitCheckQueryConditionHelper;
import com.senao.warehouse.handler.ConditionCheckHandler;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;

public class WaitCheckInquiryActivity extends Activity implements View.OnClickListener {

    private EditText txtWhNo, txtPartNo, txtPartNoPrefix, txtReceiptsNo, txtTrxId;
    private RadioButton rbPartNo, rbPartNoPrefix, rbReceiptsNo, rbTrxId;
    private Button btnReturn, btnConfirm, btnCancel;
    private TextView mConnection;
    private String errorInfo = "";
    private boolean isWhNoChecked = true;
    private boolean isPartNoChecked = true;
    private boolean isReceiptsNoChecked = true;
    private boolean isTrxIdChecked = true;
    private boolean isCheckChanged;
    private ConditionCheckHandler handler;
    private ProgressDialog dialog;
    private WaitCheckQueryConditionHelper conditionHelper;
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
                    txtWhNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    txtWhNo.setNextFocusDownId(R.id.edittext_import_part_number);
                    txtPartNo.setEnabled(true);
                    txtPartNo.requestFocus();
                    if (rbPartNoPrefix.isChecked()) {
                        txtPartNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNo.setNextFocusDownId(R.id.radio_item_part_no_prefix);
                    } else if (rbReceiptsNo.isChecked()) {
                        txtPartNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNo.setNextFocusDownId(R.id.edittext_import_receipt_number);
                    } else if (rbTrxId.isChecked()) {
                        txtPartNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                    } else {
                        txtPartNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtPartNo.setNextFocusDownId(R.id.button_confirm);
                    }
                } else if (buttonView.equals(rbPartNoPrefix)) {
                    txtPartNoPrefix.setEnabled(true);
                    txtPartNoPrefix.requestFocus();
                    if (rbPartNo.isChecked()) {
                        txtPartNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNo.setNextFocusDownId(R.id.radio_item_part_no_prefix);
                    } else {
                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtWhNo.setNextFocusDownId(R.id.radio_item_part_no_prefix);
                    }
                    if (rbReceiptsNo.isChecked()) {
                        txtPartNoPrefix.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNoPrefix.setNextFocusDownId(R.id.edittext_import_receiving_id);
                    } else if (rbTrxId.isChecked()) {
                        txtPartNoPrefix.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNoPrefix.setNextFocusDownId(R.id.edittext_import_receiving_id);
                    } else {
                        txtPartNoPrefix.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtPartNoPrefix.setNextFocusDownId(R.id.button_confirm);
                    }
                } else if (buttonView.equals(rbReceiptsNo)) {
                    txtReceiptsNo.setEnabled(true);
                    txtReceiptsNo.requestFocus();
                    if (rbPartNoPrefix.isChecked()) {
                        txtPartNoPrefix.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNoPrefix.setNextFocusDownId(R.id.edittext_import_receipt_number);
                    } else if (rbPartNo.isChecked()) {
                        txtPartNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNo.setNextFocusDownId(R.id.edittext_import_receipt_number);
                    } else {
                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtWhNo.setNextFocusDownId(R.id.edittext_import_receipt_number);
                    }

                    if (rbTrxId.isChecked()) {
                        txtReceiptsNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtReceiptsNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                    } else {
                        txtReceiptsNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtReceiptsNo.setNextFocusDownId(R.id.button_confirm);
                    }
                } else {
                    txtTrxId.setEnabled(true);
                    txtTrxId.requestFocus();
                    if (rbReceiptsNo.isChecked()) {
                        txtReceiptsNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtReceiptsNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                    } else if (rbPartNoPrefix.isChecked()) {
                        txtPartNoPrefix.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNoPrefix.setNextFocusDownId(R.id.edittext_import_receiving_id);
                    } else if (rbPartNo.isChecked()) {
                        txtPartNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                    } else {
                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtWhNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                    }
                }
            } else {
                if (buttonView.equals(rbPartNo)) {
                    if (rbPartNoPrefix.isChecked()) {
                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtWhNo.setNextFocusDownId(R.id.radio_item_part_no_prefix);
                    } else if (rbReceiptsNo.isChecked()) {
                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtWhNo.setNextFocusDownId(R.id.edittext_import_receipt_number);
                    } else if (rbTrxId.isChecked()) {
                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtWhNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                    } else {
                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtWhNo.setNextFocusDownId(R.id.button_confirm);
                    }
                } else if (buttonView.equals(rbPartNoPrefix)) {
                    if (rbPartNo.isChecked()) {
                        if (rbReceiptsNo.isChecked()) {
                            txtPartNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                            txtPartNo.setNextFocusDownId(R.id.edittext_import_receipt_number);
                        } else if (rbTrxId.isChecked()) {
                            txtPartNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                            txtPartNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                        } else {
                            txtPartNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtPartNo.setNextFocusDownId(R.id.button_confirm);
                        }
                    } else {
                        if (rbReceiptsNo.isChecked()) {
                            txtWhNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                            txtWhNo.setNextFocusDownId(R.id.edittext_import_receipt_number);
                        } else if (rbTrxId.isChecked()) {
                            txtWhNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                            txtWhNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                        } else {
                            txtWhNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtWhNo.setNextFocusDownId(R.id.button_confirm);
                        }
                    }
                } else if (buttonView.equals(rbReceiptsNo)) {
                    if (rbTrxId.isChecked()) {
                        if (rbPartNoPrefix.isChecked()) {
                            txtPartNoPrefix.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                            txtPartNoPrefix.setNextFocusDownId(R.id.edittext_import_receiving_id);
                        } else if (rbPartNo.isChecked()) {
                            txtPartNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                            txtPartNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                        } else {
                            txtWhNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                            txtWhNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                        }
                    } else {
                        if (rbPartNoPrefix.isChecked()) {
                            txtPartNoPrefix.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtPartNoPrefix.setNextFocusDownId(R.id.button_confirm);
                        } else if (rbPartNo.isChecked()) {
                            txtPartNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtPartNo.setNextFocusDownId(R.id.button_confirm);
                        } else {
                            txtWhNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtWhNo.setNextFocusDownId(R.id.button_confirm);
                        }
                    }
                } else {
                    if (rbReceiptsNo.isChecked()) {
                        txtReceiptsNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtReceiptsNo.setNextFocusDownId(R.id.button_confirm);
                    } else if (rbPartNoPrefix.isChecked()) {
                        txtPartNoPrefix.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtPartNoPrefix.setNextFocusDownId(R.id.button_confirm);
                    } else if (rbPartNo.isChecked()) {
                        txtPartNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtPartNo.setNextFocusDownId(R.id.button_confirm);
                    } else {
                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtWhNo.setNextFocusDownId(R.id.button_confirm);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_check_inquiry);

        handler = new ConditionCheckHandler();

        txtWhNo =  findViewById(R.id.edittext_import_wh_number);
        txtPartNo =  findViewById(R.id.edittext_import_part_number);
        txtPartNoPrefix =  findViewById(R.id.edittext_import_item_part_no_prefix);
        txtReceiptsNo =  findViewById(R.id.edittext_import_receipt_number);
        txtTrxId =  findViewById(R.id.edittext_import_receiving_id);

        rbPartNo =  findViewById(R.id.radio_part_number);
        rbPartNoPrefix =  findViewById(R.id.radio_item_part_no_prefix);
        rbReceiptsNo =  findViewById(R.id.radio_receipt_no);
        rbTrxId =  findViewById(R.id.radio_receiving_id);

        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtWhNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (rbPartNo.isChecked()) {
                        txtPartNo.requestFocus();
                    } else if (rbPartNoPrefix.isChecked()) {
                        txtPartNoPrefix.requestFocus();
                    } else if (rbReceiptsNo.isChecked()) {
                        txtReceiptsNo.requestFocus();
                    } else if (rbTrxId.isChecked()) {
                        txtTrxId.requestFocus();
                    }
                    if (!isWhNoChecked) {
                        doCheckWareHouseNo(v.getText().toString().trim(), null);
                    }
                    return true;
                } else if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                    if (!isWhNoChecked) {
                        doCheckWareHouseNo(v.getText().toString().trim(), null);
                    }
                    return true;
                }
                return false;
            }
        });

        txtWhNo.addTextChangedListener(new TextWatcher() {
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
        });

        txtPartNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (rbPartNoPrefix.isChecked()) {
                        txtPartNoPrefix.requestFocus();
                    } else if (rbReceiptsNo.isChecked()) {
                        txtReceiptsNo.requestFocus();
                    } else if (rbTrxId.isChecked()) {
                        txtTrxId.requestFocus();
                    }
                    if (!isPartNoChecked) {
                        doCheckPartNo(v.getText().toString().trim(), null);
                    }
                    return true;
                } else if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                    if (!isPartNoChecked) {
                        doCheckPartNo(v.getText().toString().trim(), null);
                    }
                    return true;
                }
                return false;
            }
        });

        txtPartNo.addTextChangedListener(new TextWatcher() {
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
        });

        txtPartNoPrefix.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (rbReceiptsNo.isChecked()) {
                        txtReceiptsNo.requestFocus();
                    } else if (rbTrxId.isChecked()) {
                        txtTrxId.requestFocus();
                    }
                    return true;
                } else if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });

        txtReceiptsNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (rbTrxId.isChecked()) {
                        txtTrxId.requestFocus();
                    }
                    if (!isReceiptsNoChecked) {
                        doCheckReceiptsNo(v.getText().toString().trim(), null);
                    }
                    return true;
                } else if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                    if (!isReceiptsNoChecked) {
                        doCheckReceiptsNo(v.getText().toString().trim(), null);
                    }
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
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                    if (!isTrxIdChecked) {
                        doCheckTrxId(v.getText().toString().trim(), null);
                    }
                    return true;
                }
                return false;
            }
        });

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

        rbPartNoPrefix.setOnCheckedChangeListener(rbListener);
        rbPartNoPrefix.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (rbPartNoPrefix.isChecked() && !isCheckChanged) {
                    rbPartNoPrefix.setChecked(false);
                    txtPartNoPrefix.setText("");
                    txtPartNoPrefix.setEnabled(false);
                }
                isCheckChanged = false;
            }
        });

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


        btnReturn =  findViewById(R.id.button_return);
        btnConfirm =  findViewById(R.id.button_confirm);
        btnCancel =  findViewById(R.id.button_cancel);
        mConnection =  findViewById(R.id.label_status);

        btnReturn.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        mConnection.setOnClickListener(this);

        cleanData();
    }

    private void cleanData() {
        txtWhNo.setText("");
        txtPartNo.setText("");
        txtPartNo.setEnabled(false);
        rbPartNo.setChecked(false);
        txtPartNoPrefix.setText("");
        txtPartNoPrefix.setEnabled(false);
        rbPartNoPrefix.setChecked(false);
        txtReceiptsNo.setText("");
        txtReceiptsNo.setEnabled(false);
        rbReceiptsNo.setChecked(false);
        txtTrxId.setText("");
        txtTrxId.setEnabled(false);
        rbTrxId.setChecked(false);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtWhNo.getWindowToken(), 0);
    }

    private void doCheckTrxId(String trxId, MyCallBack callBack) {
        hideKeyboard();
        conditionHelper = new WaitCheckQueryConditionHelper();
        conditionHelper.setTrxIdNo(trxId);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.Receipt_id_number_data_checking), true);
        new CheckTrxId(callBack).execute(0);
    }

    private void doCheckReceiptsNo(String receiptsNo, MyCallBack callBack) {
        hideKeyboard();
        conditionHelper = new WaitCheckQueryConditionHelper();
        conditionHelper.setReceiptsNo(receiptsNo);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.Material_receipt_number_data_checking), true);
        new CheckReceiptsNo(callBack).execute(0);
    }

    private void doCheckPartNo(String partNo, MyCallBack callBack) {
        hideKeyboard();
        conditionHelper = new WaitCheckQueryConditionHelper();
        conditionHelper.setPartNo(partNo);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.sku_verifying), true);
        new CheckPartNo(callBack).execute(0);
    }

    private void doCheckWareHouseNo(String warehouseNo, MyCallBack callBack) {
        hideKeyboard();
        conditionHelper = new WaitCheckQueryConditionHelper();
        conditionHelper.setWareHouseNo(warehouseNo);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.wareclerk_verify_data), true);
        new CheckWareHouseNo(callBack).execute(0);
    }

    private boolean checkConditions() {
        if (txtWhNo.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_warehouse_clerk),
                    Toast.LENGTH_SHORT).show();
            txtWhNo.requestFocus();
            return false;
        }
        if (rbPartNo.isChecked() && txtPartNo.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_sku),
                    Toast.LENGTH_SHORT).show();
            txtPartNo.requestFocus();
            return false;
        }
        if (rbPartNoPrefix.isChecked() && txtPartNoPrefix.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_sku),
                    Toast.LENGTH_SHORT).show();
            txtPartNoPrefix.requestFocus();
            return false;
        }
        if (rbReceiptsNo.isChecked() && txtReceiptsNo.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_recived_num),
                    Toast.LENGTH_SHORT).show();
            txtReceiptsNo.requestFocus();
            return false;
        }
        if (rbTrxId.isChecked() && txtTrxId.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_recived_id),
                    Toast.LENGTH_SHORT).show();
            txtTrxId.requestFocus();
            return false;
        }
        if (!isWhNoChecked) {
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
        } else {
            return true;
        }
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
                AlertDialog.Builder dialog = new AlertDialog.Builder(
                        WaitCheckInquiryActivity.this);
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
    }

    private void goToListPage() {

        conditionHelper = new WaitCheckQueryConditionHelper();

        conditionHelper.setWareHouseNo(txtWhNo.getText().toString().trim());

        if (rbPartNo.isChecked())
            conditionHelper.setPartNo(txtPartNo.getText().toString().trim());

        if (rbPartNoPrefix.isChecked())
            conditionHelper.setPartNoPrefix(txtPartNoPrefix.getText().toString().trim());

        if (rbReceiptsNo.isChecked())
            conditionHelper.setReceiptsNo(txtReceiptsNo.getText().toString().trim());

        if (rbTrxId.isChecked())
            conditionHelper.setTrxIdNo(txtTrxId.getText().toString().trim());

        Intent intent = new Intent(this, WaitCheckListActivity.class);
        intent.putExtra("CONDITION_INFO",
                new Gson().toJson(conditionHelper));
        startActivity(intent);
    }

    public interface MyCallBack {
        void onTaskComplete();
    }

    private class CheckTrxId extends
            AsyncTask<Integer, String, BasicHelper> {

        private MyCallBack callBack;

        public CheckTrxId(MyCallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check Trx Id from " + AppController.getServerInfo()
                    + AppController.getProperties("CheckTrxId"));
            publishProgress("收料ID資料核對中...");
            return handler.doCheckTrxId(conditionHelper);
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

            AppController.debug("onPostExecute() result json = "
                    + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    conditionHelper = (WaitCheckQueryConditionHelper) result;
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

    private class CheckReceiptsNo extends
            AsyncTask<Integer, String, BasicHelper> {

        private MyCallBack callBack;

        public CheckReceiptsNo(MyCallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check Receipts No from " + AppController.getServerInfo()
                    + AppController.getProperties("CheckReceiptsNo"));
            publishProgress(getString(R.string.Material_receipt_number_data_checking));
            return handler.doCheckReceiptsNo(conditionHelper);
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

            AppController.debug("onPostExecute() result json = "
                    + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    conditionHelper = (WaitCheckQueryConditionHelper) result;
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

    private class CheckPartNo extends
            AsyncTask<Integer, String, BasicHelper> {

        private MyCallBack callBack;

        public CheckPartNo(MyCallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check Part No from " + AppController.getServerInfo()
                    + AppController.getProperties("CheckPartNo"));
            publishProgress(getString(R.string.sku_verifying));
            return handler.doCheckPartNo(conditionHelper);
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

            AppController.debug("onPostExecute() result json = "
                    + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    conditionHelper = (WaitCheckQueryConditionHelper) result;
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

    private class CheckWareHouseNo extends
            AsyncTask<Integer, String, BasicHelper> {
        private MyCallBack callBack;

        public CheckWareHouseNo(MyCallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check Vendor Info from " + AppController.getServerInfo()
                    + AppController.getProperties("CheckWareHouseNo"));
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
            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }

        @Override
        protected void onPostExecute(BasicHelper result) {
            dialog.dismiss();

            AppController.debug("onPostExecute() result json = "
                    + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    conditionHelper = (WaitCheckQueryConditionHelper) result;
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
