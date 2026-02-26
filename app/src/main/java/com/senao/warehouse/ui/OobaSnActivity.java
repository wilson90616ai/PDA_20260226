package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.asynctask.AsyncResponse;
import com.senao.warehouse.asynctask.CallWebApi;
import com.senao.warehouse.asynctask.GetApplicantList;
import com.senao.warehouse.asynctask.GetApplicationDeptList;
import com.senao.warehouse.asynctask.GetProcessNoList;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.DocumentInfoHelper;
import com.senao.warehouse.database.TRANSFER_FORM_TYPE;
import com.senao.warehouse.database.TransferFormConditionInfoHelper;
import com.senao.warehouse.database.TransferFormInfoHelper;
import com.senao.warehouse.database.OobaSnHelper;
import com.senao.warehouse.util.ReturnCode;

public class OobaSnActivity extends Activity implements View.OnClickListener {

    private TransferFormConditionInfoHelper transferFormConditionInfoHelper;
    private String errorInfo = "";
    private TextView mConnection,lblTitle;
    private CheckBox cbWhNo, cbPartNoPrefix, cbEzFlowFormNo, cbProcessNo,cbProcessNo_end, cbApplicationDepartment;
    private EditText txtWhNo, txtPartNoPrefix, txtEzFlowFormNo, txtProcessNo,txtProcessNo_end, txtApplicationDepartment, txtApplicationName, itemNoerpcon01;
    private RadioGroup mRadioGroup;
    private RadioButton importall, importnoship, importship;
    private String shipData="1";
    private LinearLayout itemNoerpcon;

    private CompoundButton.OnCheckedChangeListener rbListener = new CompoundButton.OnCheckedChangeListener() {





        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                if (buttonView.equals(cbWhNo)) {
                    itemNoerpcon01.setVisibility(View.VISIBLE);
                    itemNoerpcon.setVisibility(View.VISIBLE);
                    itemNoerpcon01.setEnabled(true);

                    txtWhNo.setEnabled(true);
                    txtWhNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    txtWhNo.requestFocus();
                    if (cbPartNoPrefix.isChecked()) {
                        txtWhNo.setNextFocusDownId(R.id.edittext_import_part_number_prefix);
                    } else if (cbEzFlowFormNo.isChecked()) {
                        txtWhNo.setNextFocusDownId(R.id.edittext_import_ezflow_form_no);
                    } else if (cbProcessNo.isChecked()) {
                        txtWhNo.setNextFocusDownId(R.id.edittext_import_process_no);
                    } else if (cbProcessNo_end.isChecked()) {
                        txtWhNo.setNextFocusDownId(R.id.edittext_import_process_no_end);
                    } else if (cbApplicationDepartment.isChecked()) {
                        txtWhNo.setNextFocusDownId(R.id.edittext_import_application_department);
                    } else {
                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtWhNo.setNextFocusDownId(R.id.button_confirm);
                    }
                } else if (buttonView.equals(cbPartNoPrefix)) {
                    itemNoerpcon01.setVisibility(View.VISIBLE);
                    itemNoerpcon.setVisibility(View.VISIBLE);

                    itemNoerpcon01.setEnabled(true);

                    txtPartNoPrefix.setEnabled(true);
                    txtPartNoPrefix.requestFocus();
                    txtPartNoPrefix.setImeOptions(EditorInfo.IME_ACTION_NEXT);

                    if (cbWhNo.isChecked()) {
                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtWhNo.setNextFocusDownId(R.id.edittext_import_part_number_prefix);
                    }

                    if (cbEzFlowFormNo.isChecked()) {
                        txtPartNoPrefix.setNextFocusDownId(R.id.edittext_import_ezflow_form_no);
                    } else if (cbProcessNo.isChecked()) {
                        txtPartNoPrefix.setNextFocusDownId(R.id.edittext_import_process_no);
                    } else if (cbProcessNo_end.isChecked()) {
                        txtPartNoPrefix.setNextFocusDownId(R.id.edittext_import_process_no_end);
                    } else if (cbApplicationDepartment.isChecked()) {
                        txtPartNoPrefix.setNextFocusDownId(R.id.edittext_import_application_department);
                    } else {
                        txtPartNoPrefix.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtPartNoPrefix.setNextFocusDownId(R.id.button_confirm);
                    }
                } else if (buttonView.equals(cbEzFlowFormNo)) {
                    txtEzFlowFormNo.setEnabled(true);
                    txtEzFlowFormNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    txtEzFlowFormNo.requestFocus();
                    txtEzFlowFormNo.setHint(getString(R.string.label_OOBA_data));
                    txtEzFlowFormNo.setHintTextColor(getResources().getColor(R.color.red));

                    if (cbPartNoPrefix.isChecked()) {
                        txtPartNoPrefix.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNoPrefix.setNextFocusDownId(R.id.edittext_import_ezflow_form_no);
                    } else if (cbWhNo.isChecked()) {
                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtWhNo.setNextFocusDownId(R.id.edittext_import_ezflow_form_no);
                    }

                    if (cbProcessNo.isChecked()) {
                        txtEzFlowFormNo.setNextFocusDownId(R.id.edittext_import_process_no);
                    } else if (cbProcessNo_end.isChecked()) {
                        txtEzFlowFormNo.setNextFocusDownId(R.id.edittext_import_process_no_end);
                    } else if (cbApplicationDepartment.isChecked()) {
                        txtEzFlowFormNo.setNextFocusDownId(R.id.edittext_import_application_department);
                    } else {
                        txtEzFlowFormNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtEzFlowFormNo.setNextFocusDownId(R.id.button_confirm);
                    }
                } else if (buttonView.equals(cbProcessNo)) {
                    txtProcessNo.setEnabled(true);
                    txtProcessNo.requestFocus();
                    txtProcessNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    txtProcessNo.setHint(getString(R.string.label_OOBA_data_yyyymmdd));
                    txtProcessNo.setHintTextColor(getResources().getColor(R.color.red));

                    if (cbEzFlowFormNo.isChecked()) {
                        txtEzFlowFormNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtEzFlowFormNo.setNextFocusDownId(R.id.edittext_import_process_no);
                    } else if (cbPartNoPrefix.isChecked()) {
                        txtPartNoPrefix.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNoPrefix.setNextFocusDownId(R.id.edittext_import_process_no);
                    } else if (cbProcessNo_end.isChecked()) {
                        txtProcessNo_end.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtProcessNo_end.setNextFocusDownId(R.id.edittext_import_process_no);
                    } else if (cbWhNo.isChecked()) {
                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtWhNo.setNextFocusDownId(R.id.edittext_import_process_no);
                    }

                    if (cbApplicationDepartment.isChecked()) {
                        txtProcessNo.setNextFocusDownId(R.id.edittext_import_application_department);
                    } else {
                        txtProcessNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtProcessNo.setNextFocusDownId(R.id.button_confirm);
                    }
                } else if (buttonView.equals(cbProcessNo_end)) {
                    txtProcessNo_end.setEnabled(true);
                    txtProcessNo_end.requestFocus();
                    txtProcessNo_end.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    txtProcessNo_end.setHint(getString(R.string.label_OOBA_data_yyyymmdd));
                    txtProcessNo_end.setHintTextColor(getResources().getColor(R.color.red));

                    if (cbEzFlowFormNo.isChecked()) {
                        txtEzFlowFormNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtEzFlowFormNo.setNextFocusDownId(R.id.edittext_import_process_no);
                    } else if (cbPartNoPrefix.isChecked()) {
                        txtPartNoPrefix.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNoPrefix.setNextFocusDownId(R.id.edittext_import_process_no);
                    } else if (cbWhNo.isChecked()) {
                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtWhNo.setNextFocusDownId(R.id.edittext_import_process_no);
                    } else if (cbProcessNo.isChecked()) {
                        txtProcessNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtProcessNo.setNextFocusDownId(R.id.edittext_import_process_no);
                    }

                    if (cbApplicationDepartment.isChecked()) {
                        txtProcessNo_end.setNextFocusDownId(R.id.edittext_import_application_department);
                    } else {
                        txtProcessNo_end.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtProcessNo_end.setNextFocusDownId(R.id.button_confirm);
                    }
                } else if (buttonView.equals(cbApplicationDepartment)) {
                    txtApplicationDepartment.setEnabled(true);
                    txtApplicationDepartment.requestFocus();
                    txtApplicationDepartment.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    txtApplicationDepartment.setHint(getString(R.string.label_OOBA_data));
                    txtApplicationDepartment.setHintTextColor(getResources().getColor(R.color.red));
                    if (cbProcessNo.isChecked()) {
                        txtProcessNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtProcessNo.setNextFocusDownId(R.id.edittext_import_application_department);
                    }

                    if (cbEzFlowFormNo.isChecked()) {
                        txtEzFlowFormNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtEzFlowFormNo.setNextFocusDownId(R.id.edittext_import_application_department);
                    } else if (cbPartNoPrefix.isChecked()) {
                        txtPartNoPrefix.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtPartNoPrefix.setNextFocusDownId(R.id.edittext_import_application_department);
                    } else if (cbWhNo.isChecked()) {
                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtWhNo.setNextFocusDownId(R.id.edittext_import_application_department);
                    } else {
//                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
//                        txtWhNo.setNextFocusDownId(R.id.button_confirm);
                    }

                    if (cbWhNo.isChecked()) {
                        txtApplicationDepartment.setNextFocusDownId(R.id.edittext_import_application_department);
                    } else {
                        txtApplicationDepartment.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtApplicationDepartment.setNextFocusDownId(R.id.button_confirm);
                    }
                } else {
//                    txtApplicationName.setEnabled(true);
//                    txtApplicationName.requestFocus();

//                    if (cbApplicationDepartment.isChecked()) {
//                        txtApplicationDepartment.setImeOptions(EditorInfo.IME_ACTION_NEXT);
//                        txtApplicationDepartment.setNextFocusDownId(R.id.edittext_import_application_name);
//                    }

//                    if (cbProcessNo.isChecked()) {
//                        txtProcessNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
//                        txtProcessNo.setNextFocusDownId(R.id.edittext_import_application_name);
//                    }

//                    if (cbEzFlowFormNo.isChecked()) {
//                        txtEzFlowFormNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
//                        txtEzFlowFormNo.setNextFocusDownId(R.id.edittext_import_application_name);
//                    } else if (cbPartNoPrefix.isChecked()) {
//                        txtPartNoPrefix.setImeOptions(EditorInfo.IME_ACTION_NEXT);
//                        txtPartNoPrefix.setNextFocusDownId(R.id.edittext_import_application_name);
//                    } else if (cbWhNo.isChecked()) {
//                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
//                        txtWhNo.setNextFocusDownId(R.id.edittext_import_application_name);
//                    }
                }
            } else {
                if (buttonView.equals(cbWhNo)) {
                    if(cbPartNoPrefix.isChecked()){
                        itemNoerpcon01.setVisibility(View.VISIBLE);
                        itemNoerpcon.setVisibility(View.VISIBLE);
                        itemNoerpcon01.setEnabled(true);
                    } else{
                        itemNoerpcon01.setVisibility(View.GONE);
                        itemNoerpcon.setVisibility(View.GONE);
                        itemNoerpcon01.setText("");
                        itemNoerpcon01.setEnabled(false);
                    }

                    txtWhNo.setText("");
                    txtWhNo.setEnabled(false);
                } else if (buttonView.equals(cbPartNoPrefix)) {
                    if(cbWhNo.isChecked()){
                        itemNoerpcon01.setVisibility(View.VISIBLE);
                        itemNoerpcon.setVisibility(View.VISIBLE);
                        itemNoerpcon01.setEnabled(true);
                    } else{
                        itemNoerpcon01.setVisibility(View.GONE);
                        itemNoerpcon.setVisibility(View.GONE);
                        itemNoerpcon01.setText("");
                        itemNoerpcon01.setEnabled(false);
                    }

                    txtPartNoPrefix.setText("");
                    txtPartNoPrefix.setEnabled(false);

                    if (cbWhNo.isChecked()) {
                        if (cbEzFlowFormNo.isChecked()) {
                            txtWhNo.setNextFocusDownId(R.id.edittext_import_ezflow_form_no);
                        } else if (cbProcessNo.isChecked()) {
                            txtWhNo.setNextFocusDownId(R.id.edittext_import_process_no);
                        } else if (cbApplicationDepartment.isChecked()) {
                            txtWhNo.setNextFocusDownId(R.id.edittext_import_application_department);
                        } else {
                            txtWhNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtWhNo.setNextFocusDownId(R.id.button_confirm);
                        }
                    }
                } else if (buttonView.equals(cbEzFlowFormNo)) {
                    txtEzFlowFormNo.setText("");
                    txtEzFlowFormNo.setEnabled(false);
                    txtEzFlowFormNo.setHint("");

                    if (cbPartNoPrefix.isChecked()) {
                        if (cbProcessNo.isChecked()) {
                            txtPartNoPrefix.setNextFocusDownId(R.id.edittext_import_process_no);
                        } else if (cbApplicationDepartment.isChecked()) {
                            txtPartNoPrefix.setNextFocusDownId(R.id.edittext_import_application_department);
                        } else {
                            txtPartNoPrefix.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtPartNoPrefix.setNextFocusDownId(R.id.button_confirm);
                        }
                    } else if (cbWhNo.isChecked()) {
                        if (cbProcessNo.isChecked()) {
                            txtWhNo.setNextFocusDownId(R.id.edittext_import_process_no);
                        } else if (cbApplicationDepartment.isChecked()) {
                            txtWhNo.setNextFocusDownId(R.id.edittext_import_application_department);
                        } else {
                            txtWhNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtWhNo.setNextFocusDownId(R.id.button_confirm);
                        }
                    }
                } else if (buttonView.equals(cbProcessNo)) {
                    txtProcessNo.setText("");
                    txtProcessNo.setEnabled(false);
                    txtProcessNo.setHint("");

                    if (cbEzFlowFormNo.isChecked()) {
                        if (cbApplicationDepartment.isChecked()) {
                            txtEzFlowFormNo.setNextFocusDownId(R.id.edittext_import_application_department);
                        } else {
                            txtEzFlowFormNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtEzFlowFormNo.setNextFocusDownId(R.id.button_confirm);
                        }
                    } else if (cbPartNoPrefix.isChecked()) {
                        if (cbApplicationDepartment.isChecked()) {
                            txtPartNoPrefix.setNextFocusDownId(R.id.edittext_import_application_department);
                        } else {
                            txtPartNoPrefix.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtPartNoPrefix.setNextFocusDownId(R.id.button_confirm);
                        }
                    } else if (cbWhNo.isChecked()) {
                        if (cbApplicationDepartment.isChecked()) {
                            txtWhNo.setNextFocusDownId(R.id.edittext_import_application_department);
                        } else {
                            txtWhNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtWhNo.setNextFocusDownId(R.id.button_confirm);
                        }
                    }
                } else if (buttonView.equals(cbProcessNo_end)) {
                    txtProcessNo_end.setText("");
                    txtProcessNo_end.setEnabled(false);
                    txtProcessNo_end.setHint("");

                    if (cbEzFlowFormNo.isChecked()) {
                        if (cbApplicationDepartment.isChecked()) {
                            txtEzFlowFormNo.setNextFocusDownId(R.id.edittext_import_application_department);
                        } else {
                            txtEzFlowFormNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtEzFlowFormNo.setNextFocusDownId(R.id.button_confirm);
                        }
                    } else if (cbPartNoPrefix.isChecked()) {
                        if (cbApplicationDepartment.isChecked()) {
                            txtPartNoPrefix.setNextFocusDownId(R.id.edittext_import_application_department);
                        } else {
                            txtPartNoPrefix.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtPartNoPrefix.setNextFocusDownId(R.id.button_confirm);
                        }
                    } else if (cbWhNo.isChecked()) {
                        if (cbApplicationDepartment.isChecked()) {
                            txtWhNo.setNextFocusDownId(R.id.edittext_import_application_department);
                        } else {
                            txtWhNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtWhNo.setNextFocusDownId(R.id.button_confirm);
                        }
                    }
                } else if (buttonView.equals(cbApplicationDepartment)) {
                    txtApplicationDepartment.setText("");
                    txtApplicationDepartment.setEnabled(false);
                    txtApplicationDepartment.setHint("");

                    if (cbProcessNo.isChecked()) {

                    } else if (cbEzFlowFormNo.isChecked()) {

                    } else if (cbPartNoPrefix.isChecked()) {

                    } else if (cbWhNo.isChecked()) {

                    } else {
//                        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
//                        txtWhNo.setNextFocusDownId(R.id.button_confirm);
                    }
                } else {
//                    txtApplicationName.setText("");
//                    txtApplicationName.setEnabled(false);

                    if (cbProcessNo.isChecked()) {
                        txtProcessNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtProcessNo.setNextFocusDownId(R.id.button_confirm);
                    } else if (cbEzFlowFormNo.isChecked()) {
                        txtEzFlowFormNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtEzFlowFormNo.setNextFocusDownId(R.id.button_confirm);
                    } else if (cbPartNoPrefix.isChecked()) {
                        txtPartNoPrefix.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtPartNoPrefix.setNextFocusDownId(R.id.button_confirm);
                    } else if (cbWhNo.isChecked()) {
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
        setContentView(R.layout.activity_ooba_sn);

        Button btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        cbWhNo = findViewById(R.id.cb_wh_number);
        txtWhNo = findViewById(R.id.edittext_import_wh_number);
        cbWhNo.setOnCheckedChangeListener(rbListener);
        txtWhNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtWhNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (cbPartNoPrefix.isChecked()) {
                        txtPartNoPrefix.requestFocus();
                    } else if (cbEzFlowFormNo.isChecked()) {
                        txtEzFlowFormNo.requestFocus();
                    } else if (cbProcessNo.isChecked()) {
                        txtProcessNo.requestFocus();
                    } else if (cbApplicationDepartment.isChecked()) {
                        txtApplicationDepartment.requestFocus();
                    }
                    return true;
                } else if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });

        mRadioGroup = findViewById(R.id.radio_group_import);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId)
                {
                    case R.id.importall: //case importpn.getId():
                        AppController.debug("importall");
                        shipData="1";
                        break;

                    case R.id.importnoship: //case importreelid.getId():
                        AppController.debug("importnoship");
                        shipData="2";
                        break;

                    case R.id.importship: //case importrqcode.getId():
                        AppController.debug("importship");
                        shipData="3";
                        break;
                }
            }
        }); //設定單選選項監聽器

        cbPartNoPrefix = findViewById(R.id.cb_part_number_prefix);
        txtPartNoPrefix = findViewById(R.id.edittext_import_part_number_prefix);
        cbPartNoPrefix.setOnCheckedChangeListener(rbListener);
        txtPartNoPrefix.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                EditText et = (EditText) textView;

                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (et.getText().toString().trim().length() > 0) {
                        et.setText(parsePartNo(et.getText().toString().trim()));
                        et.setSelection(et.getText().length());
                    }

                    if (cbEzFlowFormNo.isChecked()) {
                        txtEzFlowFormNo.requestFocus();
                    } else if (cbProcessNo.isChecked()) {
                        txtProcessNo.requestFocus();
                    } else if (cbApplicationDepartment.isChecked()) {
                        txtApplicationDepartment.requestFocus();
                    }

                    return false;
                } else if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    if (et.getText().toString().trim().length() > 0) {
                        et.setText(parsePartNo(et.getText().toString().trim()));
                        et.setSelection(et.getText().length());
                    }

                    hideKeyboard();
                    return false;
                }

                return false;
            }
        });

        cbEzFlowFormNo = findViewById(R.id.cb_ezflow_form_no);
        txtEzFlowFormNo = findViewById(R.id.edittext_import_ezflow_form_no);
        cbEzFlowFormNo.setOnCheckedChangeListener(rbListener);
        //txtEzFlowFormNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtEzFlowFormNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                EditText et = (EditText) v;

                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (et.getText().toString().trim().length() > 0) {
                        et.setText(parseFormSn(et.getText().toString().trim()));
                        et.setSelection(et.getText().length());
                    }

                    if (cbProcessNo.isChecked()) {
                        txtProcessNo.requestFocus();
                    } else if (cbApplicationDepartment.isChecked()) {
                        txtApplicationDepartment.requestFocus();
                    }

                    return false;
                } else if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    if (et.getText().toString().trim().length() > 0) {
                        et.setText(parseFormSn(et.getText().toString().trim()));
                        et.setSelection(et.getText().length());
                    }

                    hideKeyboard();
                    return false;
                }

                return false;
            }
        });

        cbProcessNo_end = findViewById(R.id.cb_process_no_end);
        txtProcessNo_end = findViewById(R.id.edittext_import_process_no_end);
        cbProcessNo_end.setOnCheckedChangeListener(rbListener);
        txtProcessNo_end.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                EditText et = (EditText) v;

                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (et.getText().toString().trim().length() > 0) {
                        et.setText(parseProcessSn(et.getText().toString().trim()));
                        et.setSelection(et.getText().length());
                    }

                    if (cbApplicationDepartment.isChecked()) {
                        txtApplicationDepartment.requestFocus();
                    }

                    return false;
                } else if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    if (et.getText().toString().trim().length() > 0) {
                        et.setText(parseProcessSn(et.getText().toString().trim()));
                        et.setSelection(et.getText().length());
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
                        et.setSelection(et.getText().length());
                    }

                    if (cbApplicationDepartment.isChecked()) {
                        txtApplicationDepartment.requestFocus();
                    }

                    return false;
                } else if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    if (et.getText().toString().trim().length() > 0) {
                        et.setText(parseProcessSn(et.getText().toString().trim()));
                        et.setSelection(et.getText().length());
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

        cbApplicationDepartment = findViewById(R.id.cb_application_department);
        txtApplicationDepartment = findViewById(R.id.edittext_import_application_department);
        cbApplicationDepartment.setOnCheckedChangeListener(rbListener);
        txtApplicationDepartment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    return true;
                } else if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                    return true;
                }

                return false;
            }
        });

//        txtApplicationName = findViewById(R.id.edittext_import_application_name);

        Button btnConfirm = findViewById(R.id.button_confirm);
        btnConfirm.setOnClickListener(this);

        Button btnCancel = findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(this);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(this);

        itemNoerpcon = findViewById(R.id.itemNoerpcon);
        itemNoerpcon01 = findViewById(R.id.itemNoerpcon01);

        lblTitle = findViewById(R.id.ap_title);
        final SpannableString text;
        String title = getString(R.string.n_ooba, AppController.getOrgName());
        text = new SpannableString(title);
        text.setSpan(new RelativeSizeSpan(1.0f), 0, title.length() - 11,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new RelativeSizeSpan(0.7f), title.length() - 11, title.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);

        cleanData();
    }

    private String parsePartNo(String trim) {
        if (trim.length() > 12) {
            return trim.substring(0, 12);
        } else {
            return trim;
        }
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
        if (cbWhNo.isChecked()) {
            if (txtWhNo.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_sn),
                        Toast.LENGTH_SHORT).show();
                txtWhNo.requestFocus();
                return false;
            }
        } else if (cbPartNoPrefix.isChecked()) {
            if (txtPartNoPrefix.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_box_nu),
                        Toast.LENGTH_SHORT).show();
                txtPartNoPrefix.requestFocus();
                return false;
            }
        } else if (cbEzFlowFormNo.isChecked()) {
            if (txtEzFlowFormNo.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_sku),
                        Toast.LENGTH_SHORT).show();
                txtEzFlowFormNo.requestFocus();
                return false;
            }
        } else if (cbProcessNo.isChecked()) {
            if (txtProcessNo.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_estimated_shipping_date1),
                        Toast.LENGTH_SHORT).show();
                txtProcessNo.requestFocus();
                return false;
            }
        } else if (cbProcessNo_end.isChecked()) {
            if (txtProcessNo_end.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_estimated_shipping_date2),
                        Toast.LENGTH_SHORT).show();
                cbProcessNo_end.requestFocus();
                return false;
            }
        } else if (cbApplicationDepartment.isChecked()) {
            if (txtApplicationDepartment.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_wo),
                        Toast.LENGTH_SHORT).show();
                txtApplicationDepartment.requestFocus();
                return false;
            }
        } else if (!cbApplicationDepartment.isChecked()&&!cbProcessNo.isChecked()&&!cbEzFlowFormNo.isChecked()&&!cbPartNoPrefix.isChecked()&&!cbWhNo.isChecked()&&!cbProcessNo_end.isChecked()) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_condictions),
                        Toast.LENGTH_SHORT).show();
                return false;
        }

        return true;
    }

    private void cleanData() {
        cbWhNo.setChecked(false);
        txtWhNo.setText("");
        txtWhNo.setEnabled(false);
        cbPartNoPrefix.setChecked(false);
        txtPartNoPrefix.setText("");
        txtPartNoPrefix.setEnabled(false);
        cbEzFlowFormNo.setChecked(false);
        txtEzFlowFormNo.setText("");
        txtEzFlowFormNo.setEnabled(false);
        cbProcessNo.setChecked(false);
        cbProcessNo_end.setChecked(false);
        txtProcessNo.setText("");
        txtProcessNo.setEnabled(false);
        txtProcessNo_end.setText("");
        txtProcessNo_end.setEnabled(false);
        cbApplicationDepartment.setChecked(false);
        txtApplicationDepartment.setText("");
        txtApplicationDepartment.setEnabled(false);

        txtEzFlowFormNo.requestFocus();
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
        }
//        else if (id == R.id.btn_ezflow_form_no) {
//            if (cbEzFlowFormNo.isChecked()) {
//                getDocNoList();
//            }
//        } else if (id == R.id.btn_process_no) {
//            if (cbProcessNo.isChecked()) {
//                getProcessNoList();
//            }
//        } else if (id == R.id.btn_application_dept) {
//            if (cbApplicationDepartment.isChecked()) {
//                getApplicationDeptList();
//            }
//        }
    }

    private void getDocNoList() {
        transferFormConditionInfoHelper = new TransferFormConditionInfoHelper();
        transferFormConditionInfoHelper.setSearchText(txtEzFlowFormNo.getText().toString().trim());
        transferFormConditionInfoHelper.setType(TRANSFER_FORM_TYPE.FROM_P023.name());
        CallWebApi task = new CallWebApi(this, AppController.getProperties("GetEzflowDocNoList"), new AsyncResponse<BasicHelper>() {
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
        transferFormConditionInfoHelper.setType(TRANSFER_FORM_TYPE.FROM_P023.name());
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
        transferFormConditionInfoHelper.setType(TRANSFER_FORM_TYPE.FROM_P023.name());
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
        transferFormConditionInfoHelper.setType(TRANSFER_FORM_TYPE.FROM_P023.name());
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

                   // showListDialog("選擇申請人", list, txtApplicationName);
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
//                            case R.id.edittext_import_application_name:
//                                editText.setText(transferFormConditionInfoHelper.getList()[which].getApplicantId());
//                                break;
                            default:
                                break;
                        }
                    }
                });

        builder.show();
    }

    private void goToList() {
        OobaSnHelper oobaSnHelper = new OobaSnHelper();

        TransferFormInfoHelper transferFormInfoHelper = new TransferFormInfoHelper();
        //2CB8ED2CCE80 LI1-22117200

//        oobaSnHelper.setItemSn_con("all");

//        oobaSnHelper.setCartonId_con("all");
//        oobaSnHelper.setItemNo_con("all");
//        oobaSnHelper.setPlan_date_con("all");
//        oobaSnHelper.setMo_con("LI1-22117200");

//        oobaSnHelper.setShip_status_con("1");

        if (cbWhNo.isChecked()) {
            oobaSnHelper.setItemSn_con(txtWhNo.getText().toString().trim());
        } else {
            oobaSnHelper.setItemSn_con("all");
        }

        if (cbPartNoPrefix.isChecked()) {
            oobaSnHelper.setCartonId_con(txtPartNoPrefix.getText().toString().trim());
        } else {
            oobaSnHelper.setCartonId_con("all");
        }

        if (cbEzFlowFormNo.isChecked()) {
            oobaSnHelper.setItemNo_con(txtEzFlowFormNo.getText().toString().trim());
        } else {
            oobaSnHelper.setItemNo_con("all");
        }

        if (cbProcessNo.isChecked()) {
            oobaSnHelper.setPlan_date_con(txtProcessNo.getText().toString().trim());
        } else {
            oobaSnHelper.setPlan_date_con("all");
        }

        if (cbProcessNo_end.isChecked()) {
            oobaSnHelper.setPlan_date_con_end(txtProcessNo_end.getText().toString().trim());
        } else {
            oobaSnHelper.setPlan_date_con_end("all");
        }

        if (cbApplicationDepartment.isChecked()) {
            oobaSnHelper.setMo_con(txtApplicationDepartment.getText().toString().trim());
        } else {
            oobaSnHelper.setMo_con("all");
        }

        if(itemNoerpcon01.isEnabled()&&TextUtils.isEmpty(itemNoerpcon01.getText().toString().trim())) {
            oobaSnHelper.setItemNo_erp_con(itemNoerpcon01.getText().toString().trim());
        } else {
            oobaSnHelper.setItemNo_erp_con(null);
        }

        oobaSnHelper.setShip_status_con(shipData);

//        if (cbWhNo.isChecked())
//            transferFormInfoHelper.setWarehouseNo(txtWhNo.getText().toString().trim());
//        else
//            transferFormInfoHelper.setWarehouseNo(null);

//        if (cbPartNoPrefix.isChecked())
//            transferFormInfoHelper.setPartNoPrefix(txtPartNoPrefix.getText().toString().trim());
//        else
//            transferFormInfoHelper.setPartNoPrefix(null);

//        if (cbEzFlowFormNo.isChecked())
//            transferFormInfoHelper.setDocumentNo(txtEzFlowFormNo.getText().toString().trim());
//        else
//            transferFormInfoHelper.setDocumentNo(null);

//        if (cbProcessNo.isChecked())
//            transferFormInfoHelper.setProcessNo(txtProcessNo.getText().toString().trim());
//        else
//            transferFormInfoHelper.setProcessNo(null);

//        if (cbApplicationDepartment.isChecked())
//            transferFormInfoHelper.setApplicationDepartment(txtApplicationDepartment.getText().toString().trim());
//        else
//            transferFormInfoHelper.setApplicationDepartment(null);

//        Intent intent = new Intent(this, TransferLocatorFormListActivity.class); OobaSnListActivity
        Intent intent = new Intent(this, OobaSnListActivity.class);
        intent.putExtra("CONDITION_INFO",   new Gson().toJson(oobaSnHelper));
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
