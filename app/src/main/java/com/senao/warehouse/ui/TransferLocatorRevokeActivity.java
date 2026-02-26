package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.senao.warehouse.R;
import com.senao.warehouse.asynctask.AsyncResponse;
import com.senao.warehouse.asynctask.GetApplicantList;
import com.senao.warehouse.asynctask.GetApplicationDeptList;
import com.senao.warehouse.asynctask.GetEzflowDocNoList;
import com.senao.warehouse.asynctask.GetProcessNoList;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.DocumentInfoHelper;
import com.senao.warehouse.database.RevokeInfoHelper;
import com.senao.warehouse.database.TRANSFER_FORM_TYPE;
import com.senao.warehouse.database.TransferFormConditionInfoHelper;
import com.senao.warehouse.util.ReturnCode;

public class TransferLocatorRevokeActivity extends Activity implements View.OnClickListener {

    private static final String TAG = TransferLocatorRevokeActivity.class.getSimpleName();
    TransferFormConditionInfoHelper transferFormConditionInfoHelper;
    private String errorInfo = "";
    private TextView mConnection;
    private CheckBox cbEzFlowFormNo, cbProcessNo, cbApplicationDepartment, cbApplicationName;
    private EditText txtEzFlowFormNo, txtProcessNo, txtApplicationDepartment, txtApplicationName;
    private CompoundButton.OnCheckedChangeListener rbListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                if (buttonView.equals(cbEzFlowFormNo)) {
                    txtEzFlowFormNo.setEnabled(true);
                    txtEzFlowFormNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    txtEzFlowFormNo.requestFocus();
                    if (cbProcessNo.isChecked()) {
                        txtEzFlowFormNo.setNextFocusDownId(R.id.edittext_import_process_no);
                    } else if (cbApplicationDepartment.isChecked()) {
                        txtEzFlowFormNo.setNextFocusDownId(R.id.edittext_import_application_department);
                    } else if (cbApplicationName.isChecked()) {
                        txtEzFlowFormNo.setNextFocusDownId(R.id.edittext_import_application_name);
                    } else {
                        txtEzFlowFormNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtEzFlowFormNo.setNextFocusDownId(R.id.button_confirm);
                    }
                } else if (buttonView.equals(cbProcessNo)) {
                    txtProcessNo.setEnabled(true);
                    txtProcessNo.requestFocus();
                    txtProcessNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    if (cbEzFlowFormNo.isChecked()) {
                        txtEzFlowFormNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtEzFlowFormNo.setNextFocusDownId(R.id.edittext_import_process_no);
                    }

                    if (cbApplicationDepartment.isChecked()) {
                        txtProcessNo.setNextFocusDownId(R.id.edittext_import_application_department);
                    } else if (cbApplicationName.isChecked()) {
                        txtProcessNo.setNextFocusDownId(R.id.edittext_import_application_name);
                    } else {
                        txtProcessNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtProcessNo.setNextFocusDownId(R.id.button_confirm);
                    }
                } else if (buttonView.equals(cbApplicationDepartment)) {
                    txtApplicationDepartment.setEnabled(true);
                    txtApplicationDepartment.requestFocus();
                    txtApplicationDepartment.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    if (cbEzFlowFormNo.isChecked() && !cbProcessNo.isChecked()) {
                        txtEzFlowFormNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtEzFlowFormNo.setNextFocusDownId(R.id.edittext_import_application_department);
                    }

                    if (cbProcessNo.isChecked()) {
                        txtProcessNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtProcessNo.setNextFocusDownId(R.id.edittext_import_application_department);
                    }

                    if (cbApplicationName.isChecked()) {
                        txtApplicationDepartment.setNextFocusDownId(R.id.edittext_import_application_name);
                    } else {
                        txtApplicationDepartment.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtApplicationDepartment.setNextFocusDownId(R.id.button_confirm);
                    }
                } else {
                    txtApplicationName.setEnabled(true);
                    txtApplicationName.requestFocus();

                    if (cbEzFlowFormNo.isChecked() && !cbProcessNo.isChecked() && !cbApplicationDepartment.isChecked()) {
                        txtEzFlowFormNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtEzFlowFormNo.setNextFocusDownId(R.id.edittext_import_application_name);
                    }

                    if (cbProcessNo.isChecked() && !cbApplicationDepartment.isChecked()) {
                        txtProcessNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtProcessNo.setNextFocusDownId(R.id.edittext_import_application_name);
                    }

                    if (cbApplicationDepartment.isChecked()) {
                        txtApplicationDepartment.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtApplicationDepartment.setNextFocusDownId(R.id.edittext_import_application_name);
                    }
                }
            } else {
                if (buttonView.equals(cbEzFlowFormNo)) {
                    txtEzFlowFormNo.setText("");
                    txtEzFlowFormNo.setEnabled(false);
                } else if (buttonView.equals(cbProcessNo)) {
                    txtProcessNo.setText("");
                    txtProcessNo.setEnabled(false);

                    if (cbEzFlowFormNo.isChecked()) {
                        if (cbApplicationDepartment.isChecked()) {
                            txtEzFlowFormNo.setNextFocusDownId(R.id.edittext_import_application_department);
                        } else if (cbApplicationName.isChecked()) {
                            txtEzFlowFormNo.setNextFocusDownId(R.id.edittext_import_application_name);
                        } else {
                            txtEzFlowFormNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtEzFlowFormNo.setNextFocusDownId(R.id.button_confirm);
                        }
                    }

                } else if (buttonView.equals(cbApplicationDepartment)) {
                    txtApplicationDepartment.setText("");
                    txtApplicationDepartment.setEnabled(false);

                    if (cbEzFlowFormNo.isChecked() && !cbProcessNo.isChecked()) {
                        if (cbApplicationName.isChecked()) {
                            txtEzFlowFormNo.setNextFocusDownId(R.id.edittext_import_application_name);
                        } else {
                            txtEzFlowFormNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtEzFlowFormNo.setNextFocusDownId(R.id.button_confirm);
                        }
                    }

                    if (cbProcessNo.isChecked()) {
                        if (cbApplicationName.isChecked()) {
                            txtProcessNo.setNextFocusDownId(R.id.edittext_import_application_name);
                        } else {
                            txtProcessNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtProcessNo.setNextFocusDownId(R.id.button_confirm);
                        }
                    }
                } else {
                    txtApplicationName.setText("");
                    txtApplicationName.setEnabled(false);

                    if (cbEzFlowFormNo.isChecked() && !cbProcessNo.isChecked() && !cbApplicationDepartment.isChecked()) {
                        txtEzFlowFormNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtEzFlowFormNo.setNextFocusDownId(R.id.button_confirm);
                    }

                    if (cbProcessNo.isChecked() && !cbApplicationDepartment.isChecked()) {
                        txtProcessNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtProcessNo.setNextFocusDownId(R.id.button_confirm);
                    }

                    if (cbApplicationDepartment.isChecked()) {
                        txtApplicationDepartment.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtApplicationDepartment.setNextFocusDownId(R.id.button_confirm);
                    }

                    if (cbApplicationName.isChecked()) {
                        txtApplicationName.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtApplicationName.setNextFocusDownId(R.id.button_confirm);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_locator_revoke);

        Button btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        cbEzFlowFormNo = findViewById(R.id.cb_ezflow_form_no);
        txtEzFlowFormNo = findViewById(R.id.edittext_import_ezflow_form_no);
        cbEzFlowFormNo.setOnCheckedChangeListener(rbListener);
        txtEzFlowFormNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtEzFlowFormNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    EditText et = (EditText) v;
                    if (et.getText().toString().trim().length() > 0) {
                        et.setText(parseFormSn(et.getText().toString().trim()));
                    }
                    if (cbProcessNo.isChecked()) {
                        txtProcessNo.requestFocus();
                    } else if (cbApplicationDepartment.isChecked()) {
                        txtApplicationDepartment.requestFocus();
                    } else if (cbApplicationName.isChecked()) {
                        txtApplicationName.requestFocus();
                    }
                    return false;
                } else if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    Log.d(TAG, "onKeyListener Enter or Action Down" +
                            v.getId());
                    EditText et = (EditText) v;
                    if (et.getText().toString().trim().length() > 0) {
                        et.setText(parseFormSn(et.getText().toString().trim()));
                    }
                    hideKeyboard();
                    return false;
                }
                return false;
            }
        });

        cbProcessNo = findViewById(R.id.cb_process_no);
        txtProcessNo = findViewById(R.id.edittext_import_process_no);
        cbProcessNo.setOnCheckedChangeListener(rbListener);

        txtProcessNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                EditText et = (EditText) v;
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (et.getText().toString().trim().length() > 0) {
                        et.setText(parseProcessSn(et.getText().toString().trim()));
                    }
                    if (cbApplicationDepartment.isChecked()) {
                        txtApplicationDepartment.requestFocus();
                    } else if (cbApplicationName.isChecked()) {
                        txtApplicationName.requestFocus();
                    }
                    return false;
                } else if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    Log.d(TAG, "onKeyListener Enter or Action Down" +
                            v.getId());
                    if (et.getText().toString().trim().length() > 0) {
                        et.setText(parseProcessSn(et.getText().toString().trim()));
                    }
                    hideKeyboard();
                    return false;
                }
                return false;
            }
        });

        Button btnEzflowFormNo = findViewById(R.id.btn_ezflow_form_no);
        btnEzflowFormNo.setOnClickListener(this);

        Button btnProcessNo = findViewById(R.id.btn_process_no);
        btnProcessNo.setOnClickListener(this);

        Button btnApplicationDept = findViewById(R.id.btn_application_dept);
        btnApplicationDept.setOnClickListener(this);

        Button btnApplicant = findViewById(R.id.btn_application_applicant);
        btnApplicant.setOnClickListener(this);

        cbApplicationDepartment = findViewById(R.id.cb_application_department);
        txtApplicationDepartment = findViewById(R.id.edittext_import_application_department);
        cbApplicationDepartment.setOnCheckedChangeListener(rbListener);
        txtApplicationDepartment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (cbApplicationName.isChecked()) {
                        txtApplicationName.requestFocus();
                    }
                    return true;
                } else if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });

        cbApplicationName = findViewById(R.id.cb_application_name);
        txtApplicationName = findViewById(R.id.edittext_import_application_name);
        cbApplicationName.setOnCheckedChangeListener(rbListener);

        Button btnConfirm = findViewById(R.id.button_confirm);
        btnConfirm.setOnClickListener(this);

        Button btnCancel = findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(this);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(this);

        cleanData();
    }

    private String parseFormSn(String trim) {
        if (trim.contains("@")) {
            try {
                return trim.split("@")[0];
            } catch (Exception ex) {
                return trim;
            }
        } else {
            return trim;
        }
    }

    private String parseProcessSn(String trim) {
        if (trim.contains("@")) {
            try {
                return trim.split("@")[1];
            } catch (Exception ex) {
                return trim;
            }
        } else {
            return trim;
        }
    }

    private boolean checkInputConditions() {
        if (cbEzFlowFormNo.isChecked()) {
            if (txtEzFlowFormNo.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), "請輸入表單單號",
                        Toast.LENGTH_SHORT).show();
                txtEzFlowFormNo.requestFocus();
                return false;
            }
        } else if (cbProcessNo.isChecked()) {
            if (txtProcessNo.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), "請輸入流程序號",
                        Toast.LENGTH_SHORT).show();
                txtProcessNo.requestFocus();
                return false;
            }
        } else if (cbApplicationDepartment.isChecked()) {
            if (txtApplicationDepartment.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), "請輸入申請部門",
                        Toast.LENGTH_SHORT).show();
                txtApplicationDepartment.requestFocus();
                return false;
            }
        } else if (cbApplicationName.isChecked()) {
            if (txtApplicationName.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), "請輸入申請人",
                        Toast.LENGTH_SHORT).show();
                txtApplicationName.requestFocus();
                return false;
            }
        }

        return true;
    }

    private void cleanData() {
        cbEzFlowFormNo.setChecked(true);
        txtEzFlowFormNo.setText("");
        txtEzFlowFormNo.setEnabled(true);
        cbProcessNo.setChecked(false);
        txtProcessNo.setText("");
        txtProcessNo.setEnabled(false);
        cbApplicationDepartment.setChecked(false);
        txtApplicationDepartment.setText("");
        txtApplicationDepartment.setEnabled(false);
        cbApplicationName.setChecked(false);
        txtApplicationName.setText("");
        txtApplicationName.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_return) {
            returnPage();
        } else if (id == R.id.button_confirm) {
            if (checkInputConditions()) {
                goToList();
            }
        } else if (id == R.id.button_cancel) {
            cleanData();
        } else if (id == R.id.label_status) {
            showStatus();
        } else if (id == R.id.btn_ezflow_form_no) {
            if (cbEzFlowFormNo.isChecked()) {
                getDocNoList();
            }
        } else if (id == R.id.btn_process_no) {
            if (cbProcessNo.isChecked()) {
                getProcessNoList();
            }
        } else if (id == R.id.btn_application_dept) {
            if (cbApplicationDepartment.isChecked()) {
                getApplicationDeptList();
            }
        } else if (id == R.id.btn_application_applicant) {
            if (cbApplicationName.isChecked()) {
                getApplicantList();
            }
        }
    }

    private void getDocNoList() {
        transferFormConditionInfoHelper = new TransferFormConditionInfoHelper();
        transferFormConditionInfoHelper.setSearchText(txtEzFlowFormNo.getText().toString().trim());
        transferFormConditionInfoHelper.setType(TRANSFER_FORM_TYPE.TO_P023.name());
        GetEzflowDocNoList task = new GetEzflowDocNoList(this, new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                transferFormConditionInfoHelper = (TransferFormConditionInfoHelper) result;
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
                if (transferFormConditionInfoHelper.getList() == null || transferFormConditionInfoHelper.getList().length == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cant_query_form_no), Toast.LENGTH_LONG).show();
                } else {
                    String[] list = new String[transferFormConditionInfoHelper.getList().length];
                    int i = 0;
                    for (DocumentInfoHelper documentInfoHelper : transferFormConditionInfoHelper.getList()) {
                        list[i] = getString(R.string.list_item_doc_no, documentInfoHelper.getDocumentNo(),
                                documentInfoHelper.getApplicationDepartmentId(), documentInfoHelper.getApplicationDepartment(),
                                documentInfoHelper.getApplicantId(), documentInfoHelper.getApplicant());
                        i++;
                    }
                    showListDialog(getString(R.string.select_form_no), list, txtEzFlowFormNo);
                }
            }

            @Override
            public void onFailure() {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }

            @Override
            public void onError(BasicHelper result) {
                if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                    mConnection.setText(getString(R.string.connect_error));
                } else {
                    mConnection.setText(getString(R.string.db_return_error));
                }
                errorInfo = result.getStrErrorBuf();
                mConnection.setTextColor(Color.RED);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            }

        });
        task.execute(transferFormConditionInfoHelper);
    }

    private void getProcessNoList() {
        transferFormConditionInfoHelper = new TransferFormConditionInfoHelper();
        transferFormConditionInfoHelper.setSearchText(txtProcessNo.getText().toString().trim());
        transferFormConditionInfoHelper.setType(TRANSFER_FORM_TYPE.TO_P023.name());
        GetProcessNoList task = new GetProcessNoList(this, new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                transferFormConditionInfoHelper = (TransferFormConditionInfoHelper) result;
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
                if (transferFormConditionInfoHelper.getList() == null || transferFormConditionInfoHelper.getList().length == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.cant_query_seq), Toast.LENGTH_LONG).show();
                } else {
                    String[] list = new String[transferFormConditionInfoHelper.getList().length];
                    int i = 0;
                    for (DocumentInfoHelper documentInfoHelper : transferFormConditionInfoHelper.getList()) {
                        list[i] = getString(R.string.list_item_processs_no, documentInfoHelper.getProcessNo(),
                                documentInfoHelper.getApplicationDepartmentId(), documentInfoHelper.getApplicationDepartment(),
                                documentInfoHelper.getApplicantId(), documentInfoHelper.getApplicant());
                        i++;
                    }
                    showListDialog(getString(R.string.select_seq), list, txtProcessNo);
                }
            }

            @Override
            public void onFailure() {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }

            @Override
            public void onError(BasicHelper result) {
                if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                    mConnection.setText(getString(R.string.connect_error));
                } else {
                    mConnection.setText(getString(R.string.db_return_error));
                }
                errorInfo = result.getStrErrorBuf();
                mConnection.setTextColor(Color.RED);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            }

        });
        task.execute(transferFormConditionInfoHelper);
    }

    private void getApplicationDeptList() {
        transferFormConditionInfoHelper = new TransferFormConditionInfoHelper();
        transferFormConditionInfoHelper.setSearchText(txtApplicationDepartment.getText().toString().trim());
        transferFormConditionInfoHelper.setType(TRANSFER_FORM_TYPE.TO_P023.name());
        GetApplicationDeptList task = new GetApplicationDeptList(this, new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                transferFormConditionInfoHelper = (TransferFormConditionInfoHelper) result;
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
                if (transferFormConditionInfoHelper.getList() == null || transferFormConditionInfoHelper.getList().length == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.select_no_department), Toast.LENGTH_LONG).show();
                } else {
                    String[] list = new String[transferFormConditionInfoHelper.getList().length];
                    int i = 0;
                    for (DocumentInfoHelper documentInfoHelper : transferFormConditionInfoHelper.getList()) {
                        list[i] = getString(R.string.list_item_apply_dept, documentInfoHelper.getApplicationDepartmentId(), documentInfoHelper.getApplicationDepartment());
                        i++;
                    }
                    showListDialog(getString(R.string.Select_department), list, txtApplicationDepartment);
                }
            }

            @Override
            public void onFailure() {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }

            @Override
            public void onError(BasicHelper result) {
                if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                    mConnection.setText(getString(R.string.connect_error));
                } else {
                    mConnection.setText(getString(R.string.db_return_error));
                }
                errorInfo = result.getStrErrorBuf();
                mConnection.setTextColor(Color.RED);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            }

        });
        task.execute(transferFormConditionInfoHelper);
    }

    private void getApplicantList() {
        transferFormConditionInfoHelper = new TransferFormConditionInfoHelper();
        transferFormConditionInfoHelper.setSearchText(txtApplicationName.getText().toString().trim());
        transferFormConditionInfoHelper.setType(TRANSFER_FORM_TYPE.TO_P023.name());
        GetApplicantList task = new GetApplicantList(this, new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                transferFormConditionInfoHelper = (TransferFormConditionInfoHelper) result;
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
                if (transferFormConditionInfoHelper.getList() == null || transferFormConditionInfoHelper.getList().length == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.Select_no_applicant), Toast.LENGTH_LONG).show();
                } else {
                    String[] list = new String[transferFormConditionInfoHelper.getList().length];
                    int i = 0;
                    for (DocumentInfoHelper documentInfoHelper : transferFormConditionInfoHelper.getList()) {
                        list[i] = getString(R.string.list_item_applicant, documentInfoHelper.getApplicantId(), documentInfoHelper.getApplicant());
                        i++;
                    }
                    showListDialog(getString(R.string.Select_the_applicant), list, txtApplicationName);
                }
            }

            @Override
            public void onFailure() {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }

            @Override
            public void onError(BasicHelper result) {
                if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                    mConnection.setText(getString(R.string.connect_error));
                } else {
                    mConnection.setText(getString(R.string.db_return_error));
                }
                errorInfo = result.getStrErrorBuf();
                mConnection.setTextColor(Color.RED);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            }

        });
        task.execute(transferFormConditionInfoHelper);
    }

    private void showListDialog(final String title, final String[] list, final EditText editText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setItems(list, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (editText.getId()) {
                            case R.id.edittext_import_ezflow_form_no:
                                editText.setText(transferFormConditionInfoHelper.getList()[which].getDocumentNo());
                                break;
                            case R.id.edittext_import_process_no:
                                editText.setText(transferFormConditionInfoHelper.getList()[which].getProcessNo());
                                break;
                            case R.id.edittext_import_application_department:
                                editText.setText(transferFormConditionInfoHelper.getList()[which].getApplicationDepartmentId());
                                break;
                            case R.id.edittext_import_application_name:
                                editText.setText(transferFormConditionInfoHelper.getList()[which].getApplicantId());
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.show();
    }

    private void goToList() {
        RevokeInfoHelper conditionHelper = new RevokeInfoHelper();

        if (cbEzFlowFormNo.isChecked())
            conditionHelper.setDocumentNo(txtEzFlowFormNo.getText().toString().trim());
        else
            conditionHelper.setDocumentNo(null);

        if (cbProcessNo.isChecked())
            conditionHelper.setProcessNo(txtProcessNo.getText().toString().trim());
        else
            conditionHelper.setProcessNo(null);

        if (cbApplicationDepartment.isChecked())
            conditionHelper.setApplicationDepartmentId(txtApplicationDepartment.getText().toString().trim());
        else
            conditionHelper.setApplicationDepartmentId(null);

        if (cbApplicationName.isChecked())
            conditionHelper.setApplicantId(txtApplicationName.getText().toString().trim());
        else
            conditionHelper.setApplicantId(null);

        Intent intent = new Intent(this, TransferLocatorRevokeListActivity.class);
        intent.putExtra("CONDITION_INFO",
                new Gson().toJson(conditionHelper));
        startActivity(intent);
    }

    private void returnPage() {
        hideKeyboard();
        finish();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtEzFlowFormNo.getWindowToken(), 0);
    }

    private void showStatus() {
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
    }
}