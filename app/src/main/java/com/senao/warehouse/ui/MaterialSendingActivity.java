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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.SendingInfoHelper;
import com.senao.warehouse.handler.ConditionCheckHandler;
import com.senao.warehouse.handler.SendingHandler;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;

public class MaterialSendingActivity extends Activity {

    public static final int QUERY_TYPE_SEND = 1;
    public static final int QUERY_TYPE_TRANSFER = 2;
    public static final int QUERY_TYPE_LACK = 3;
    private static final String TAG = MaterialSendingActivity.class.getSimpleName();
    private Button btnReturn, btnConfirm, btnCancel;
    private CheckBox cbLineNumber, cbMergeNumber, cbWhNumber, cbWorkOrderNo, cbItemPart;
    private EditText txtLineNumber, txtWorkOrderNo, txtWarehouseNo,
            txtMergeNumber, txtItemPart;
    private TextView mConnection,lblTitle;
    private String errorInfo = "";
    private ProgressDialog dialog;
    private SendingInfoHelper sendingInfo;
    private SendingHandler sendingHandler;
    private ConditionCheckHandler checkHandler;
    private boolean isCheckFinished = true;
    private boolean isMergeNoChecked = true;
    private boolean isWHNoChecked = true;
    private boolean isWONoChecked = true;
    private boolean isItemPartChecked = true;
    private int sendType;

    private CompoundButton.OnCheckedChangeListener rbListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                if (buttonView.equals(cbLineNumber)) {
                    txtLineNumber.setEnabled(true);
                    txtLineNumber.requestFocus();
                    txtLineNumber.setImeOptions(EditorInfo.IME_ACTION_NEXT);

                    if (cbMergeNumber.isChecked()) {
                        txtLineNumber.setNextFocusDownId(R.id.edittext_import_merge_number);
                    } else if (cbWhNumber.isChecked()) {
                        txtLineNumber.setNextFocusDownId(R.id.edittext_import_wh_number);
                    } else if (cbWorkOrderNo.isChecked()) {
                        txtLineNumber.setNextFocusDownId(R.id.edittext_import_wo_number);
                    } else if (cbItemPart.isChecked()) {
                        txtLineNumber.setNextFocusDownId(R.id.edittext_import_item_part);
                    } else {
                        txtLineNumber.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtLineNumber.setNextFocusDownId(R.id.button_confirm);
                    }
                } else if (buttonView.equals(cbMergeNumber)) {
                    txtMergeNumber.setEnabled(true);
                    txtMergeNumber.requestFocus();
                    txtMergeNumber.setImeOptions(EditorInfo.IME_ACTION_NEXT);

                    if (cbLineNumber.isChecked()) {
                        txtLineNumber.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtLineNumber.setNextFocusDownId(R.id.edittext_import_merge_number);
                    }

                    if (cbWhNumber.isChecked()) {
                        txtMergeNumber.setNextFocusDownId(R.id.edittext_import_wh_number);
                    } else if (cbWorkOrderNo.isChecked()) {
                        txtMergeNumber.setNextFocusDownId(R.id.edittext_import_wo_number);
                    } else if (cbItemPart.isChecked()) {
                        txtMergeNumber.setNextFocusDownId(R.id.edittext_import_item_part);
                    } else {
                        txtMergeNumber.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtMergeNumber.setNextFocusDownId(R.id.button_confirm);
                    }
                } else if (buttonView.equals(cbWhNumber)) {
                    txtWarehouseNo.setEnabled(true);
                    txtWarehouseNo.requestFocus();
                    txtWarehouseNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);

                    if (cbLineNumber.isChecked() && !cbMergeNumber.isChecked()) {
                        txtLineNumber.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtLineNumber.setNextFocusDownId(R.id.edittext_import_wh_number);
                    }

                    if (cbMergeNumber.isChecked()) {
                        txtMergeNumber.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtMergeNumber.setNextFocusDownId(R.id.edittext_import_wh_number);
                    }

                    if (cbWorkOrderNo.isChecked()) {
                        txtWarehouseNo.setNextFocusDownId(R.id.edittext_import_wo_number);
                    } else if (cbItemPart.isChecked()) {
                        txtWarehouseNo.setNextFocusDownId(R.id.edittext_import_item_part);
                    } else {
                        txtWarehouseNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtWarehouseNo.setNextFocusDownId(R.id.button_confirm);
                    }
                } else if (buttonView.equals(cbWorkOrderNo)) {
                    txtWorkOrderNo.setEnabled(true);
                    txtWorkOrderNo.requestFocus();

                    if (cbLineNumber.isChecked() && !cbMergeNumber.isChecked() && !cbWhNumber.isChecked()) {
                        txtLineNumber.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtLineNumber.setNextFocusDownId(R.id.edittext_import_wo_number);
                    }

                    if (cbMergeNumber.isChecked() && !cbWhNumber.isChecked()) {
                        txtMergeNumber.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtMergeNumber.setNextFocusDownId(R.id.edittext_import_wo_number);
                    }

                    if (cbWhNumber.isChecked()) {
                        txtWarehouseNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtWarehouseNo.setNextFocusDownId(R.id.edittext_import_wo_number);
                    }

                    if (cbItemPart.isChecked()) {
                        txtWorkOrderNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtWorkOrderNo.setNextFocusDownId(R.id.edittext_import_item_part);
                    } else {
                        txtWorkOrderNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtWorkOrderNo.setNextFocusDownId(R.id.button_confirm);
                    }
                } else {
                    txtItemPart.setEnabled(true);
                    txtItemPart.requestFocus();

                    if (cbLineNumber.isChecked() && !cbMergeNumber.isChecked() && !cbWhNumber.isChecked() && !cbWorkOrderNo.isChecked()) {
                        txtLineNumber.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtLineNumber.setNextFocusDownId(R.id.edittext_import_item_part);
                    }

                    if (cbMergeNumber.isChecked() && !cbWhNumber.isChecked() && !cbWorkOrderNo.isChecked()) {
                        txtMergeNumber.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtMergeNumber.setNextFocusDownId(R.id.edittext_import_item_part);
                    }

                    if (cbWhNumber.isChecked() && !cbWorkOrderNo.isChecked()) {
                        txtWarehouseNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtWarehouseNo.setNextFocusDownId(R.id.edittext_import_item_part);
                    }

                    if (cbWorkOrderNo.isChecked()) {
                        txtWorkOrderNo.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        txtWorkOrderNo.setNextFocusDownId(R.id.edittext_import_item_part);
                    }
                }
            } else {
                if (buttonView.equals(cbLineNumber)) {
                    txtLineNumber.setText("");
                    txtLineNumber.setEnabled(false);
                } else if (buttonView.equals(cbMergeNumber)) {
                    txtMergeNumber.setText("");
                    txtMergeNumber.setEnabled(false);

                    if (cbLineNumber.isChecked()) {
                        if (cbWhNumber.isChecked()) {
                            txtLineNumber.setNextFocusDownId(R.id.edittext_import_wh_number);
                        } else if (cbWorkOrderNo.isChecked()) {
                            txtLineNumber.setNextFocusDownId(R.id.edittext_import_wo_number);
                        } else if (cbItemPart.isChecked()) {
                            txtLineNumber.setNextFocusDownId(R.id.edittext_import_item_part);
                        } else {
                            txtLineNumber.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtLineNumber.setNextFocusDownId(R.id.button_confirm);
                        }
                    }
                } else if (buttonView.equals(cbWhNumber)) {
                    txtWarehouseNo.setText("");
                    txtWarehouseNo.setEnabled(false);

                    if (cbLineNumber.isChecked() && !cbMergeNumber.isChecked()) {
                        if (cbWorkOrderNo.isChecked()) {
                            txtLineNumber.setNextFocusDownId(R.id.edittext_import_wo_number);
                        } else if (cbItemPart.isChecked()) {
                            txtLineNumber.setNextFocusDownId(R.id.edittext_import_item_part);
                        } else {
                            txtLineNumber.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtLineNumber.setNextFocusDownId(R.id.button_confirm);
                        }
                    }

                    if (cbMergeNumber.isChecked()) {
                        if (cbWorkOrderNo.isChecked()) {
                            txtMergeNumber.setNextFocusDownId(R.id.edittext_import_wo_number);
                        } else if (cbItemPart.isChecked()) {
                            txtMergeNumber.setNextFocusDownId(R.id.edittext_import_item_part);
                        } else {
                            txtMergeNumber.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtMergeNumber.setNextFocusDownId(R.id.button_confirm);
                        }
                    }
                } else if (buttonView.equals(cbWorkOrderNo)) {
                    txtWorkOrderNo.setText("");
                    txtWorkOrderNo.setEnabled(false);

                    if (cbLineNumber.isChecked() && !cbMergeNumber.isChecked() && !cbWhNumber.isChecked()) {
                        if (cbItemPart.isChecked()) {
                            txtLineNumber.setNextFocusDownId(R.id.edittext_import_item_part);
                        } else {
                            txtLineNumber.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtLineNumber.setNextFocusDownId(R.id.button_confirm);
                        }
                    }

                    if (cbMergeNumber.isChecked() && !cbWhNumber.isChecked()) {
                        if (cbItemPart.isChecked()) {
                            txtMergeNumber.setNextFocusDownId(R.id.edittext_import_item_part);
                        } else {
                            txtMergeNumber.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtMergeNumber.setNextFocusDownId(R.id.button_confirm);
                        }
                    }

                    if (cbWhNumber.isChecked()) {
                        if (cbItemPart.isChecked()) {
                            txtWarehouseNo.setNextFocusDownId(R.id.edittext_import_item_part);
                        } else {
                            txtWarehouseNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            txtWarehouseNo.setNextFocusDownId(R.id.button_confirm);
                        }
                    }
                } else {
                    txtItemPart.setText("");
                    txtItemPart.setEnabled(false);

                    if (cbLineNumber.isChecked() && !cbMergeNumber.isChecked() && !cbWhNumber.isChecked() && !cbWorkOrderNo.isChecked()) {
                        txtLineNumber.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtLineNumber.setNextFocusDownId(R.id.button_confirm);
                    }

                    if (cbMergeNumber.isChecked() && !cbWhNumber.isChecked() && !cbWorkOrderNo.isChecked()) {
                        txtMergeNumber.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtMergeNumber.setNextFocusDownId(R.id.button_confirm);
                    }

                    if (cbWhNumber.isChecked() && !cbWorkOrderNo.isChecked()) {
                        txtWarehouseNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtWarehouseNo.setNextFocusDownId(R.id.button_confirm);
                    }

                    if (cbItemPart.isChecked()) {
                        txtItemPart.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        txtItemPart.setNextFocusDownId(R.id.button_confirm);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            sendType = extras.getInt("TYPE");

            if (sendType < QUERY_TYPE_SEND) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_shipping_info), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            sendingHandler = new SendingHandler();
            checkHandler = new ConditionCheckHandler();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.label_fill_in_all_required_fields), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_material_sending);
        TextView title = findViewById(R.id.ap_title);
        final SpannableString text;

        if (sendType == QUERY_TYPE_SEND){
            text = new SpannableString(getString(R.string.title_activity_material_sending_debit1, AppController.getOrgName()));
            text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            title.setText(text);
//            title.setText(R.string.title_activity_material_sending_debit);
        } else if (sendType == QUERY_TYPE_TRANSFER){
            text = new SpannableString(getString(R.string.title_activity_material_sending_transfer1, AppController.getOrgName()));
            text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            title.setText(text);
//            title.setText(R.string.title_activity_material_sending_transfer);
        } else{
            text = new SpannableString(getString(R.string.title_activity_material_lack_query1, AppController.getOrgName()));
            text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            title.setText(text);
//            title.setText(R.string.title_activity_material_lack_query);
        }

//        if (sendType == QUERY_TYPE_SEND)
//            title.setText(R.string.title_activity_material_sending_debit);
//        else if (sendType == QUERY_TYPE_TRANSFER)
//            title.setText(R.string.title_activity_material_sending_transfer);
//        else
//            title.setText(R.string.title_activity_material_lack_query);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MaterialSendingActivity.this);
                    dialog.setTitle("Error Msg");
                    dialog.setMessage(errorInfo);
                    dialog.setIcon(android.R.drawable.ic_dialog_alert);
                    dialog.setCancelable(false);

                    dialog.setNegativeButton(getString(R.string.btn_ok),
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

        cbLineNumber = findViewById(R.id.cb_line_number);
        cbLineNumber.setOnCheckedChangeListener(rbListener);
        txtLineNumber = findViewById(R.id.edittext_import_line_number);
        txtLineNumber.setNextFocusDownId(R.id.edittext_import_wh_number);

        cbMergeNumber = findViewById(R.id.cb_merge_number);
        txtMergeNumber = findViewById(R.id.edittext_import_merge_number);
        txtMergeNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String MergeNo = ((EditText) v).getText().toString().trim();
                if (!hasFocus && !isMergeNoChecked) {
                    doCheckMergeNo(MergeNo);
                }
            }
        });

        txtMergeNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString().trim()))
                    isMergeNoChecked = true;
                else
                    isMergeNoChecked = false;
            }
        });

        cbMergeNumber.setOnCheckedChangeListener(rbListener);
        txtWarehouseNo = findViewById(R.id.edittext_import_wh_number);
        txtWarehouseNo.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtWarehouseNo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String WarehouseNo = ((EditText) v).getText().toString().trim();

                if (!hasFocus && !isWHNoChecked) {
                    doCheckWareHouseNo(WarehouseNo);
                }
            }
        });

        cbWhNumber = findViewById(R.id.cb_wh_number);
        cbWhNumber.setOnCheckedChangeListener(rbListener);
        txtWarehouseNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString().trim()))
                    isWHNoChecked = true;
                else
                    isWHNoChecked = false;
            }
        });

        txtWarehouseNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (cbWorkOrderNo.isChecked()) {
                        txtWorkOrderNo.requestFocus();
                    } else if (cbItemPart.isChecked()) {
                        txtItemPart.requestFocus();
                    }

                    return true;
                }

                return false;
            }
        });

        cbWorkOrderNo = findViewById(R.id.cb_wo_no);
        txtWorkOrderNo = findViewById(R.id.edittext_import_wo_number);
        txtWorkOrderNo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String WorkOrderNo = ((EditText) v).getText().toString().trim();

                if (!hasFocus && !isWONoChecked) {
                    doCheckWorkOrderNo(WorkOrderNo);
                }
            }
        });

        txtWorkOrderNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isWONoChecked = TextUtils.isEmpty(s.toString().trim());
            }
        });

        cbWorkOrderNo.setOnCheckedChangeListener(rbListener);

        cbItemPart = findViewById(R.id.cb_item_part);
        txtItemPart = findViewById(R.id.edittext_import_item_part);
        txtItemPart.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtItemPart.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String itemNo = ((EditText) v).getText().toString().trim();

                if (!hasFocus && !isItemPartChecked) {
                    doCheckItemPart(itemNo);
                }
            }
        });

        txtItemPart.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isItemPartChecked = TextUtils.isEmpty(s.toString().trim());
            }
        });

        txtItemPart.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                final boolean isEnterEvent = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
                final boolean isEnterUpEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_UP;
                final boolean isEnterDownEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_DOWN;

                // Capture this event to receive ACTION_UP
                // We do not care on other actions
                if (actionId == EditorInfo.IME_ACTION_DONE || isEnterUpEvent) {
                    // Do your action here
                    EditText et = (EditText) textView;

                    if (et.getText().toString().trim().equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.enter_sku), Toast.LENGTH_SHORT).show();
                        return true;
                    } else {
                        txtItemPart.setText(parsePartNo(txtItemPart.getText().toString().trim()));
                        txtItemPart.setSelection(txtItemPart.getText().length());
                        return false;
                    }
                } else {
                    return isEnterDownEvent;
                }
            }
        });

        cbItemPart.setOnCheckedChangeListener(rbListener);

        btnReturn = findViewById(R.id.button_return);
        btnConfirm = findViewById(R.id.button_confirm);
        btnCancel = findViewById(R.id.button_cancel);

        btnReturn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Return", String.valueOf(v.getId()));
                returnPage();
            }
        });

        btnConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick Confim " + String.valueOf(v.getId()));

                if (false) {
                    isCheckFinished = true;
                    sendingInfo = new SendingInfoHelper();
                    sendingInfo.setLineNo("002");
                    //sendingInfo.setMergeNo("16010704");
                    sendingInfo.setWhNo("LA1");
                    //sendingInfo.setWhNo("LF1");
                    //sendingInfo.setWoNo("LI1-15C12200");
                    //sendingInfo.setItemPart("1272E00");
                    //sendingInfo.setItemNo("");
                    Intent intent = null;

                    if (sendType == MaterialSendingActivity.QUERY_TYPE_LACK)
                        intent = new Intent(getBaseContext(), MaterialLackListActivity.class);
                    else
                        intent = new Intent(getBaseContext(), MaterialSendingListActivity.class);

                    intent.putExtra("TYPE", sendType);
                    intent.putExtra("CONDITION_INFO", new Gson().toJson(sendingInfo));
                    startActivityForResult(intent, 0);
                    return;
                }

                isCheckFinished = false;
                checkInputConditions();
            }
        });

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick Cancel" + String.valueOf(v.getId()));
                cleanData();
            }
        });

        lblTitle = findViewById(R.id.ap_title);
        setTitle();
        cleanData();
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.label_material_sending1, AppController.getOrgName()));
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private String parsePartNo(String trim) {
        if (trim.length() > 12) {
            return trim.substring(0, 12);
        } else {
            return trim;
        }
    }

    private void checkInputConditions() {
        if (cbLineNumber.isChecked()) {
            if (txtLineNumber.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_line), Toast.LENGTH_SHORT).show();
                txtLineNumber.requestFocus();
                return;
            }
        } else if (cbMergeNumber.isChecked()) {
            if (txtMergeNumber.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(),  getString(R.string.label_import_merge_no), Toast.LENGTH_SHORT).show(); //"請填寫輸入合併碼"
                txtMergeNumber.requestFocus();
                return;
            }
        } else if (cbWhNumber.isChecked()) {
            if (txtWarehouseNo.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_warehouse_clerk), Toast.LENGTH_SHORT).show();
                txtWarehouseNo.requestFocus();
                return;
            }
        } else if (cbWorkOrderNo.isChecked()) {
            if (txtWorkOrderNo.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_wo), Toast.LENGTH_SHORT).show();
                txtWorkOrderNo.requestFocus();
                return;
            }
        } else if (cbItemPart.isChecked()) {
            if (txtItemPart.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_sku), Toast.LENGTH_SHORT).show();
                txtItemPart.requestFocus();
                return;
            }
        }

        AppController.debug("isMergeNoChecked = " + isMergeNoChecked);
        AppController.debug("isWHNoChecked = " + isWHNoChecked);
        AppController.debug("isWONoChecked = " + isWONoChecked);
        AppController.debug("isItemPartChecked = " + isItemPartChecked);
        AppController.debug("sendType = " + sendType);

        if (!isMergeNoChecked)
            doCheckMergeNo(txtMergeNumber.getText().toString().trim());
        else if (!isWHNoChecked)
            doCheckWareHouseNo(txtWarehouseNo.getText().toString().trim());//1
        else if (!isWONoChecked)
            doCheckWorkOrderNo(txtWorkOrderNo.getText().toString().trim());
        else if (!isItemPartChecked)
            doCheckItemPart(txtItemPart.getText().toString().trim());
        else if (sendType == MaterialSendingActivity.QUERY_TYPE_LACK)
            goToList();
        else
            doCheckAccMonth();
        //else
        //   goToList();
    }

    private void doCheckWorkOrderNo(String workOrderNo) {
        hideKeyboard();
        sendingInfo = new SendingInfoHelper();
        sendingInfo.setWoNo(workOrderNo);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.wo_chk), true);
        new CheckWorkOrderNo().execute(0);
    }

    private void doCheckAccMonth() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.verify_data), true);
        new CheckAccountingMonth().execute(0);
    }

    private void doCheckItemPart(String itemNo) {
        hideKeyboard();
        isItemPartChecked = true;

        if (!isCheckFinished)
            checkInputConditions();
    }

    private void doCheckWareHouseNo(String warehouseNo) {
        hideKeyboard();
        sendingInfo = new SendingInfoHelper();
        sendingInfo.setWhNo(warehouseNo);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.wareclerk_verify_data), true);
        new CheckWareHouseNo().execute(0);
    }

    private void doCheckMergeNo(String mergeNo) {
        hideKeyboard();
        sendingInfo = new SendingInfoHelper();
        sendingInfo.setMergeNo(mergeNo);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.Merge_code_data_verification), true);
        new CheckMergeNo().execute(0);
    }

    private void doCheckWipLineNo(String lineNo) {
        hideKeyboard();
        sendingInfo = new SendingInfoHelper();
        sendingInfo.setLineNo(lineNo);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.Checking_line), true);
        new CheckWipLineNo().execute(0);
    }

    private void returnPage() {
        hideKeyboard();
        finish();
    }

    private void goToList() {
        isCheckFinished = true;
        sendingInfo = new SendingInfoHelper();

        if (cbLineNumber.isChecked()) {
            sendingInfo.setLineNo(txtLineNumber.getText().toString().trim());
        }

        if (cbMergeNumber.isChecked()) {
            sendingInfo.setMergeNo(txtMergeNumber.getText().toString().trim());
        }

        if (cbWhNumber.isChecked()) {
            sendingInfo.setWhNo(txtWarehouseNo.getText().toString().trim());
        }

        if (cbWorkOrderNo.isChecked()) {
            sendingInfo.setWoNo(txtWorkOrderNo.getText().toString().trim());
        }

        if (cbItemPart.isChecked()) {
            sendingInfo.setItemPart(txtItemPart.getText().toString().trim());
        }

        Intent intent;

        if (sendType == MaterialSendingActivity.QUERY_TYPE_LACK)
            intent = new Intent(getBaseContext(), MaterialLackListActivity.class);
        else
            intent = new Intent(getBaseContext(), MaterialSendingListActivity.class);

        intent.putExtra("TYPE", sendType);
        intent.putExtra("CONDITION_INFO", new Gson().toJson(sendingInfo));
        startActivityForResult(intent, 0);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtLineNumber.getWindowToken(), 0);
    }

    private void cleanData() {
        cbLineNumber.setChecked(false);
        txtLineNumber.setText("");
        txtLineNumber.setEnabled(false);
        cbMergeNumber.setChecked(false);
        txtMergeNumber.setText("");
        txtMergeNumber.setEnabled(false);
        cbWorkOrderNo.setChecked(false);
        txtWorkOrderNo.setText("");
        txtWorkOrderNo.setEnabled(false);
        cbItemPart.setChecked(false);
        txtItemPart.setText("");
        txtItemPart.setEnabled(false);
        cbWhNumber.setChecked(true);
        txtWarehouseNo.setText("");
        txtWarehouseNo.setEnabled(true);
        txtWarehouseNo.requestFocus();
    }

    public void onBackPressed() {
        Log.d(TAG, "onClick Hardware Back Button");
        returnPage();
    }

    private class CheckWorkOrderNo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check Vendor Info from " + AppController.getServerInfo()
                    + AppController.getProperties("CheckWipJobNo"));
            publishProgress(getString(R.string.wo_chk));
            return sendingHandler.doCheckWipJobNo(sendingInfo);
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
                    sendingInfo = (SendingInfoHelper) result;
                    isWONoChecked = true;
                    if (!isCheckFinished)
                        checkInputConditions();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    txtWorkOrderNo.requestFocus();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                txtWorkOrderNo.requestFocus();
            }
        }
    }

    private class CheckWareHouseNo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check Vendor Info from " + AppController.getServerInfo()
                    + AppController.getProperties("CheckInvWhNo"));
            publishProgress(getString(R.string.wareclerk_verify_data));
            return sendingHandler.doCheckInvWhNo(sendingInfo);
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
                    sendingInfo = (SendingInfoHelper) result;
                    isWHNoChecked = true;

                    if (!isCheckFinished)
                        checkInputConditions();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    txtWarehouseNo.requestFocus();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                txtWarehouseNo.requestFocus();
            }
        }
    }

    private class CheckMergeNo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check Vendor Info from " + AppController.getServerInfo()
                    + AppController.getProperties("CheckWipMergeNo"));
            publishProgress(getString(R.string.Merge_code_data_verification));
            return sendingHandler.doCheckWipMergeNo(sendingInfo);
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
                    sendingInfo = (SendingInfoHelper) result;
                    isMergeNoChecked = true;

                    if (!isCheckFinished)
                        checkInputConditions();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    txtMergeNumber.requestFocus();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                txtMergeNumber.requestFocus();
            }
        }
    }

    private class CheckWipLineNo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check Vendor Info from " + AppController.getServerInfo()
                    + AppController.getProperties("CheckVendorInfo"));
            publishProgress(getString(R.string.Checking_line));
            return sendingHandler.doCheckWipProdLine(sendingInfo);
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
                    sendingInfo = (SendingInfoHelper) result;

                    if (!isCheckFinished)
                        checkInputConditions();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    txtLineNumber.requestFocus();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                txtLineNumber.requestFocus();
            }
        }
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
