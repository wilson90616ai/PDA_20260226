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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
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
import com.senao.warehouse.database.SendingInfoHelper;
import com.senao.warehouse.handler.ConditionCheckHandler;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.ReturnCode;

import java.util.ArrayList;
import java.util.Collection;

public class MaterialSendingVerifyActivity extends Activity implements View.OnClickListener {

    static final Collection<String> EXCLUDE_SUBINVENTORY;

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

    private EditText txtLineNumber, txtSubinventory, txtLocator, txtWoNo;
    private CheckBox cbLineNumber, cbWoNumber;
    private Button btnReturn, btnConfirm, btnCancel;
    private TextView mConnection,lblTitle;
    private String errorInfo = "";
    private ProgressDialog dialog;
    private ConditionCheckHandler checkHandler;

    private CompoundButton.OnCheckedChangeListener rbListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                if (buttonView.equals(cbLineNumber)) {
                    txtLineNumber.setEnabled(true);
                    txtLineNumber.requestFocus();
                    txtLineNumber.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    txtLineNumber.setNextFocusDownId(R.id.edittext_import_subinventory);
                } else {
                    txtWoNo.setEnabled(true);
                    txtWoNo.requestFocus();
                    txtLocator.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    txtLocator.setNextFocusDownId(R.id.edittext_import_wo_number);
                }
            } else {
                if (buttonView.equals(cbLineNumber)) {
                    txtLineNumber.setText("");
                    txtLineNumber.setEnabled(false);
                } else {
                    txtWoNo.setText("");
                    txtWoNo.setEnabled(false);
                    txtLocator.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    txtLocator.setNextFocusDownId(R.id.button_confirm);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_sending_verify);

        btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        cbLineNumber = findViewById(R.id.cb_line_number);
        cbLineNumber.setOnCheckedChangeListener(rbListener);
        txtLineNumber = findViewById(R.id.edittext_import_line_number);

        txtSubinventory = findViewById(R.id.edittext_import_subinventory);
        txtLocator = findViewById(R.id.edittext_import_locator);

        cbWoNumber = findViewById(R.id.cb_wo_number);
        cbWoNumber.setOnCheckedChangeListener(rbListener);
        txtWoNo = findViewById(R.id.edittext_import_wo_number);

        btnConfirm = findViewById(R.id.button_confirm);
        btnConfirm.setOnClickListener(this);

        btnCancel = findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(this);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(this);

        checkHandler = new ConditionCheckHandler();

        lblTitle = findViewById(R.id.ap_title);

        setTitle();
        clearCondition();
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.title_activity_material_sending_check1, AppController.getOrgName()));
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private boolean checkCondition() {
        if (cbLineNumber.isChecked() && txtLineNumber.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_line), Toast.LENGTH_LONG).show();
            txtLineNumber.requestFocus();
            return false;
        }

        if (txtSubinventory.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_sub), Toast.LENGTH_LONG).show();
            txtSubinventory.requestFocus();
            return false;
        }

        if (EXCLUDE_SUBINVENTORY.contains(txtSubinventory.getText().toString().trim())) {
            Toast.makeText(getApplicationContext(), getString(R.string.sub_controlled_enter_correct_sub), Toast.LENGTH_LONG).show();
            txtSubinventory.requestFocus();
            return false;
        }

        if (txtLocator.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_loc), Toast.LENGTH_LONG).show();
            txtLocator.requestFocus();
            return false;
        }

        if (txtLocator.getText().toString().trim().equals(Constant.LOCATOR_P023)) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_other_loc), Toast.LENGTH_LONG).show();
            txtLocator.requestFocus();
            return false;
        }

        if (cbWoNumber.isChecked() && txtWoNo.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_wo), Toast.LENGTH_LONG).show();
            txtWoNo.requestFocus();
            return false;
        }

        return true;
    }

    private void returnPage() {
        hideKeyboard();
        finish();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtLineNumber.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button_return) {
            returnPage();
        } else if (id == R.id.label_status) {
            if (!TextUtils.isEmpty(errorInfo)) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
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
        } else if (id == R.id.button_cancel) {
            clearCondition();
        } else if (id == R.id.button_confirm) {
            if (checkCondition()) {
                //Toast.makeText(getApplicationContext(), "next page", Toast.LENGTH_LONG);
                doCheckAccMonth();
                //goToList();
            }
        }
    }

    private void goToList() {
        Intent intent = new Intent(this, MaterialSendingVerifyListActivity.class);
        SendingInfoHelper sendingInfo = new SendingInfoHelper();

        if (cbLineNumber.isChecked()) {
            sendingInfo.setLineNo(txtLineNumber.getText().toString().trim());
        }

        sendingInfo.setSubinventory(txtSubinventory.getText().toString().trim());
        sendingInfo.setLocator(txtLocator.getText().toString().trim());

        if (cbWoNumber.isChecked()) {
            sendingInfo.setWoNo(txtWoNo.getText().toString().trim());
        }

        intent.putExtra("CONDITION_INFO", new Gson().toJson(sendingInfo));
        startActivityForResult(intent, 0);
    }

    private void clearCondition() {
        cbLineNumber.setChecked(false);
        txtLineNumber.setText("");
        txtLineNumber.setEnabled(false);

        txtSubinventory.setText("");
        txtSubinventory.setEnabled(true);

        txtLocator.setText("");
        txtLocator.setEnabled(true);

        cbWoNumber.setChecked(false);
        txtWoNo.setText("");
        txtWoNo.setEnabled(false);

        cbLineNumber.setChecked(true);
    }

    private void doCheckAccMonth() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.verify_data), true);
        new CheckAccountingMonth().execute(0);
    }

    private class CheckAccountingMonth extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check Accounting Month from " + AppController.getServerInfo()
                    + AppController.getProperties("CheckInvAccountingMonth"));
            publishProgress(getString(R.string.Month_accounting));
            return checkHandler.doCheckAccountingMonth();
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
