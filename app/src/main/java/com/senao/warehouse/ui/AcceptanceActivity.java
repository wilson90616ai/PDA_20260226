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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.ConditionHelper;
import com.senao.warehouse.handler.ConditionCheckHandler;
import com.senao.warehouse.util.ReturnCode;

public class AcceptanceActivity extends Activity implements View.OnClickListener {
    public static final int REGULAR = 0;
    public static final int OUTSOURCING = 1;
    private EditText txtWhNo, txtPartNo, txtReceiptsNo, txtTrxId;
    private CheckBox cbWhNo, cbPartNo, cbReceiptsNo, cbTrxId;
    private Button btnReturn, btnConfirm, btnCancel;
    private TextView mConnection, txtTitle;
    private String errorInfo = "";
    private ProgressDialog dialog;
    private ConditionHelper conditionHelper;
    private ConditionCheckHandler handler;
    private boolean isWhNoChecked = true;
    private boolean isPartNoChecked = true;
    private boolean isReceiptsNoChecked = true;
    private boolean isTrxIdChecked = true;
    private int accType;
    private ConditionCheckHandler checkHandler;

    private MyCallBack mCallBack = new MyCallBack() {
        @Override
        public void onTaskComplete() {
            if (checkConditions())
                //goToList();
                doCheckAccMonth();
        }
    };

    private CompoundButton.OnCheckedChangeListener rbListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                if (buttonView.equals(cbWhNo)) {
                    txtWhNo.setEnabled(true);
                    txtWhNo.requestFocus();
                    txtWhNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);

                    if (cbPartNo.isChecked()) {
                        txtWhNo.setNextFocusDownId(R.id.edittext_import_part_number);
                    } else if (cbReceiptsNo.isChecked()) {
                        txtWhNo.setNextFocusDownId(R.id.edittext_import_receipt_number);
                    } else if (cbTrxId.isChecked()) {
                        txtWhNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                    } else {
                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtWhNo.setNextFocusDownId(R.id.button_confirm);
                    }
                } else if (buttonView.equals(cbPartNo)) {
                    txtPartNo.setEnabled(true);
                    txtPartNo.requestFocus();
                    txtPartNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);

                    if (cbWhNo.isChecked()) {
                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtWhNo.setNextFocusDownId(R.id.edittext_import_part_number);
                    }

                    if (cbReceiptsNo.isChecked()) {
                        txtPartNo.setNextFocusDownId(R.id.edittext_import_receipt_number);
                    } else if (cbTrxId.isChecked()) {
                        txtPartNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                    } else {
                        txtPartNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtPartNo.setNextFocusDownId(R.id.button_confirm);
                    }
                } else if (buttonView.equals(cbReceiptsNo)) {
                    txtReceiptsNo.setEnabled(true);
                    txtReceiptsNo.requestFocus();
                    txtReceiptsNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);

                    if (cbWhNo.isChecked() && !cbPartNo.isChecked()) {
                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtWhNo.setNextFocusDownId(R.id.edittext_import_receipt_number);
                    }

                    if (cbPartNo.isChecked()) {
                        txtPartNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNo.setNextFocusDownId(R.id.edittext_import_receipt_number);
                    }

                    if (cbTrxId.isChecked()) {
                        txtReceiptsNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                    } else {
                        txtReceiptsNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtReceiptsNo.setNextFocusDownId(R.id.button_confirm);
                    }
                } else {
                    txtTrxId.setEnabled(true);
                    txtTrxId.requestFocus();

                    if (cbWhNo.isChecked() && !cbPartNo.isChecked() && !cbReceiptsNo.isChecked()) {
                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtWhNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                    }

                    if (cbPartNo.isChecked() && !cbReceiptsNo.isChecked()) {
                        txtPartNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                    }

                    if (cbReceiptsNo.isChecked()) {
                        txtReceiptsNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtReceiptsNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                    }
                }
            } else {
                if (buttonView.equals(cbWhNo)) {
                    txtWhNo.setText("");
                    txtWhNo.setEnabled(false);
                } else if (buttonView.equals(cbPartNo)) {
                    txtPartNo.setText("");
                    txtPartNo.setEnabled(false);

                    if (cbWhNo.isChecked()) {
                        if (cbReceiptsNo.isChecked()) {
                            txtWhNo.setNextFocusDownId(R.id.edittext_import_receipt_number);
                        } else if (cbTrxId.isChecked()) {
                            txtWhNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                        } else {
                            txtWhNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtWhNo.setNextFocusDownId(R.id.button_confirm);
                        }
                    }
                } else if (buttonView.equals(cbReceiptsNo)) {
                    txtReceiptsNo.setText("");
                    txtReceiptsNo.setEnabled(false);

                    if (cbWhNo.isChecked() && !cbPartNo.isChecked()) {
                        if (cbTrxId.isChecked()) {
                            txtWhNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                        } else {
                            txtWhNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtWhNo.setNextFocusDownId(R.id.button_confirm);
                        }
                    }

                    if (cbPartNo.isChecked()) {
                        if (cbTrxId.isChecked()) {
                            txtPartNo.setNextFocusDownId(R.id.edittext_import_receiving_id);
                        } else {
                            txtPartNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtPartNo.setNextFocusDownId(R.id.button_confirm);
                        }
                    }
                } else {
                    txtTrxId.setText("");
                    txtTrxId.setEnabled(false);

                    if (cbWhNo.isChecked() && !cbPartNo.isChecked() && !cbReceiptsNo.isChecked()) {
                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtWhNo.setNextFocusDownId(R.id.button_confirm);
                    }

                    if (cbPartNo.isChecked() && !cbReceiptsNo.isChecked()) {
                        txtPartNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtPartNo.setNextFocusDownId(R.id.button_confirm);
                    }

                    if (cbReceiptsNo.isChecked()) {
                        txtReceiptsNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtReceiptsNo.setNextFocusDownId(R.id.button_confirm);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceptance);

        txtTitle = findViewById(R.id.ap_title);
        accType = getIntent().getIntExtra("TYPE", 0);
        final SpannableString text;

        if (accType == OUTSOURCING) {
            text = new SpannableString(getString(R.string.label_acceptance_outsourcing1, AppController.getOrgName()));
            text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            txtTitle.setText(text);
        }else {
            text = new SpannableString(getString(R.string.label_acceptance1, AppController.getOrgName()));
            text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            txtTitle.setText(text);
        }

        /*if (accType == OUTSOURCING) {
            txtTitle.setText(R.string.label_acceptance_outsourcing);
        }*/

        cbWhNo = findViewById(R.id.cb_wh_number);
        cbWhNo.setOnCheckedChangeListener(rbListener);

        txtWhNo = findViewById(R.id.edittext_import_wh_number);
        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtWhNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (cbPartNo.isChecked()) {
                        txtPartNo.requestFocus();
                    } else if (cbReceiptsNo.isChecked()) {
                        txtReceiptsNo.requestFocus();
                    } else if (cbTrxId.isChecked()) {
                        txtTrxId.requestFocus();
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

        txtPartNo = findViewById(R.id.edittext_import_part_number);
        txtPartNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                final boolean isEnterEvent = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
                final boolean isEnterUpEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_UP;
                final boolean isEnterDownEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_DOWN;

                //Capture this event to receive ACTION_UP
                //We do not care on other actions
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    txtPartNo.setText(parsePartNo(txtPartNo.getText().toString().trim()));
                    txtPartNo.setSelection(txtPartNo.getText().length());

                    if (cbReceiptsNo.isChecked()) {
                        txtReceiptsNo.requestFocus();
                    } else if (cbTrxId.isChecked()) {
                        txtTrxId.requestFocus();
                    }

                    hideKeyboard();
                    return true;
                } else if (actionId == EditorInfo.IME_ACTION_DONE || isEnterUpEvent) {
                    //Do your action here
                    txtPartNo.setText(parsePartNo(txtPartNo.getText().toString().trim()));
                    txtPartNo.setSelection(txtPartNo.getText().length());
                    hideKeyboard();
                    return false;
                } else return isEnterDownEvent;
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
                    if (cbTrxId.isChecked()) {
                        txtTrxId.requestFocus();
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
                isReceiptsNoChecked = TextUtils.isEmpty(s.toString().trim());
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
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();

                    /*if (!isTrxIdChecked) {
                        doCheckTrxId(v.getText().toString().trim(), null);
                    }*/

                    return true;
                }

                return false;
            }
        });

        cbPartNo = findViewById(R.id.cb_part_number);
        cbPartNo.setOnCheckedChangeListener(rbListener);

        cbReceiptsNo = findViewById(R.id.cb_receipt_no);
        cbReceiptsNo.setOnCheckedChangeListener(rbListener);

        cbTrxId = findViewById(R.id.cb_receiving_id);
        cbTrxId.setOnCheckedChangeListener(rbListener);

        btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);
        btnConfirm = findViewById(R.id.button_confirm);
        btnConfirm.setOnClickListener(this);
        btnCancel = findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(this);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(this);

        handler = new ConditionCheckHandler();

        cleanData();

        checkHandler = new ConditionCheckHandler();
    }

    private String parsePartNo(String trim) {
        if (trim.length() > 12) {
            return trim.substring(0, 12);
        } else {
            return trim;
        }
    }

    private void doCheckTrxId(String trxId, MyCallBack callBack) {
        hideKeyboard();
        conditionHelper = new ConditionHelper();
        conditionHelper.setTrxIdNo(trxId);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.Receipt_id_number_data_checking), true);
        new CheckTrxId(callBack).execute(0);
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
        cbPartNo.setChecked(false);
        txtPartNo.setText("");
        txtPartNo.setEnabled(false);
        cbReceiptsNo.setChecked(false);
        txtReceiptsNo.setText("");
        txtReceiptsNo.setEnabled(false);
        cbTrxId.setChecked(false);
        txtTrxId.setText("");
        txtTrxId.setEnabled(false);
        cbWhNo.setChecked(true);
        txtWhNo.setText("");
        txtWhNo.setEnabled(true);
        txtWhNo.requestFocus();
    }

    private boolean checkConditions() {
        if (cbWhNo.isChecked() && txtWhNo.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_warehouse_clerk), Toast.LENGTH_SHORT).show();
            txtWhNo.requestFocus();
            return false;
        }

        if (cbPartNo.isChecked() && txtPartNo.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_sku), Toast.LENGTH_SHORT).show();
            txtPartNo.requestFocus();
            return false;
        }

        if (cbReceiptsNo.isChecked() && txtReceiptsNo.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_recived_num), Toast.LENGTH_SHORT).show();
            txtReceiptsNo.requestFocus();
            return false;
        }

        if (cbTrxId.isChecked() && txtTrxId.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_recived_id), Toast.LENGTH_SHORT).show();
            txtTrxId.requestFocus();
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
                //goToList();
                doCheckAccMonth();
        } else if (id == R.id.button_cancel) {
            cleanData();
        } else if (id == R.id.label_status) {
            if (!TextUtils.isEmpty(errorInfo)) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(AcceptanceActivity.this);
                dialog.setTitle("Error Msg");
                dialog.setMessage(errorInfo);
                dialog.setIcon(android.R.drawable.ic_dialog_alert);
                dialog.setCancelable(false);
                dialog.setNegativeButton( getString(R.string.btn_ok),
                        new DialogInterface.OnClickListener() {
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

    private void goToList() {
        conditionHelper = new ConditionHelper();

        if (cbWhNo.isChecked())
            conditionHelper.setWareHouseNo(txtWhNo.getText().toString().trim());
        else
            conditionHelper.setWareHouseNo(null);

        if (cbPartNo.isChecked())
            conditionHelper.setPartNo(txtPartNo.getText().toString().trim());
        else
            conditionHelper.setPartNo(null);

        if (cbReceiptsNo.isChecked())
            conditionHelper.setReceiptsNo(txtReceiptsNo.getText().toString().trim());
        else
            conditionHelper.setReceiptsNo(null);

        if (cbTrxId.isChecked())
            conditionHelper.setTrxIdNo(txtTrxId.getText().toString().trim());
        else
            conditionHelper.setTrxIdNo(null);

        Intent intent = new Intent(this, AcceptanceListActivity.class);
        intent.putExtra("CONDITION_INFO", new Gson().toJson(conditionHelper));
        intent.putExtra("TYPE", accType);
        startActivity(intent);
    }

    private void doCheckAccMonth() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.verify_data), true);
        new CheckAccountingMonth().execute(0);
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
            publishProgress(getString(R.string.Receipt_id_number_data_checking));
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

    private class CheckAccountingMonth extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check Accounting Month from " + AppController.getServerInfo() + AppController.getProperties("CheckInvAccountingMonth"));
            publishProgress(getString(R.string.Month_accounting));
            return checkHandler.doCheckAccountingMonth();
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
                    goToList();
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
}
