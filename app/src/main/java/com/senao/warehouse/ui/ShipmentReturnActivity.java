package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
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
import com.senao.warehouse.database.ReturnInfoHelper;
import com.senao.warehouse.handler.ShipmentPickingHandler;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.util.Util;

import java.util.ArrayList;
import java.util.List;

public class ShipmentReturnActivity extends Activity implements View.OnClickListener {

    private EditText etDn, etCartonNoDcNo, etCartonNo, etSn, etQrCode;
    private RadioButton rbPallet, rbBox, rbDcNo, rbImportSn, rbImportCn, rbImportQrCode;
    private TextView lblCartonNoDcNo, mConnection,lblTitle;
    private TextView txtItemNo, txtItemDesc, txtReqQty, txtPackQty, txtUnPackQty, txtDelQty, txtPalletNo;
    private String errorInfo;
    private ProgressDialog dialog;
    private ReturnInfoHelper returnInfo;
    private ShipmentPickingHandler handler;
    private LinearLayout llSn;

    private RadioGroup.OnCheckedChangeListener listener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio_pallet:
                    Log.d("onClick Radio Pallet", String.valueOf(checkedId));
                    llSn.setVisibility(View.VISIBLE);
                    lblCartonNoDcNo.setVisibility(View.GONE);
                    etCartonNoDcNo.setVisibility(View.GONE);
                    rbImportSn.setChecked(true);
                    break;
                case R.id.radio_box:
                    Log.d("onClick Radio Box", String.valueOf(checkedId));
                    llSn.setVisibility(View.VISIBLE);
                    lblCartonNoDcNo.setVisibility(View.GONE);
                    etCartonNoDcNo.setVisibility(View.GONE);
                    rbImportSn.setChecked(true);
                    break;
                case R.id.radio_dc_no:
                    Log.d("onClick Radio DC NO", String.valueOf(checkedId));
                    lblCartonNoDcNo.setVisibility(View.VISIBLE);
                    etCartonNoDcNo.setVisibility(View.VISIBLE);
                    llSn.setVisibility(View.GONE);
                    break;
            }

            cleanData();
        }
    };

    private RadioButton.OnCheckedChangeListener rbListener = new RadioButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (b) {
                int checkedId = compoundButton.getId();

                switch (checkedId) {
                    case R.id.radiobutton_import_sn:
                        Log.d("onClick Radio Import SN", String.valueOf(checkedId));
                        etSn.setEnabled(true);
                        etSn.requestFocus();
                        rbImportCn.setChecked(false);
                        etCartonNo.setText("");
                        etCartonNo.setEnabled(false);
                        rbImportQrCode.setChecked(false);
                        etQrCode.setText("");
                        etQrCode.setEnabled(false);
                        break;
                    case R.id.radiobutton_import_cn:
                        Log.d("onClick Radio Import CN", String.valueOf(checkedId));
                        etCartonNo.setEnabled(true);
                        etCartonNo.requestFocus();
                        rbImportSn.setChecked(false);
                        etSn.setText("");
                        etSn.setEnabled(false);
                        rbImportQrCode.setChecked(false);
                        etQrCode.setText("");
                        etQrCode.setEnabled(false);
                        break;
                    case R.id.radiobutton_import_qrcode:
                        Log.d("onClick Radio Import QR", String.valueOf(checkedId));
                        etQrCode.setEnabled(true);
                        etQrCode.requestFocus();
                        rbImportSn.setChecked(false);
                        etSn.setText("");
                        etSn.setEnabled(false);
                        rbImportCn.setChecked(false);
                        etCartonNo.setText("");
                        etCartonNo.setEnabled(false);
                        break;
                }
            }
        }
    };

    private TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
            final boolean isEnterEvent = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
            final boolean isEnterUpEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_UP;
            final boolean isEnterDownEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_DOWN;

            if (actionId == EditorInfo.IME_ACTION_NEXT || isEnterUpEvent) {
                // Do your action here
                //hideKeyboard(textView);
                EditText et = (EditText) textView;

                switch (et.getId()) {
                    case R.id.edittext_import_dn:
                        if (et.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.dn_is_not_null), Toast.LENGTH_SHORT).show();
                            return true;
                        } else {
                            String result = parseDn(et.getText().toString().trim());
                            if (TextUtils.isEmpty(result)) {
                                Toast.makeText(getApplicationContext(), getString(R.string.cant_resolve_dn), Toast.LENGTH_LONG).show();
                                return true;
                            } else {
                                et.setText(result);
                                checkDn(result);
                            }
                        }
                        break;
                    case R.id.edittext_import_cn_dc_no:
                        if (et.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_box_nu), Toast.LENGTH_SHORT).show();
                            return true;
                        } else {
                            checkValue("B", etDn.getText().toString().trim(), null, et.getText().toString().trim());
                        }
                        break;
                    case R.id.edittext_import_sn:
                        if (et.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.sn_is_not_null), Toast.LENGTH_SHORT).show();
                            return true;
                        } else {
                            if (rbPallet.isChecked()) {
                                checkValue("P", etDn.getText().toString().trim(), et.getText().toString().trim(), null);
                            } else {
                                checkValue("B", etDn.getText().toString().trim(), et.getText().toString().trim(), null);
                            }
                        }
                        break;
                    case R.id.edittext_import_cn:
                        if (et.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_box_nu), Toast.LENGTH_SHORT).show();
                            return true;
                        } else {
                            if (rbPallet.isChecked()) {
                                checkValue("P", etDn.getText().toString().trim(), null, et.getText().toString().trim());
                            } else {
                                checkValue("B", etDn.getText().toString().trim(), null, et.getText().toString().trim());
                            }
                        }
                        break;
                    case R.id.edittext_import_qrcode:
                        if (et.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.QR_CODE_is_not_null), Toast.LENGTH_SHORT).show();
                            return true;
                        } else {
                            if (rbPallet.isChecked()) {
                                checkValue("P", etDn.getText().toString().trim(), getSN(et.getText().toString().trim()), null);
                            } else {
                                checkValue("B", etDn.getText().toString().trim(), getSN(et.getText().toString().trim()), null);
                            }
                        }
                        break;
                    default:
                        break;
                }

                return false;
            } else if (isEnterDownEvent) {
                // Capture this event to receive ACTION_UP
                return true;
            } else {
                // We do not care on other actions
                return false;
            }
        }
    };

    private void hideKeyboard(View edit) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
    }

    private String parseDn(String dn) {
        String result = null;

        if (!TextUtils.isEmpty(dn)) {
            if (dn.contains("@")) {
                String[] list = dn.split("@");

                if (list.length > 1) {
                    result = list[1];
                }
            } else {
                result = dn;
            }
        }

        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipment_return);

        Button btnPick = findViewById(R.id.button_return);
        btnPick.setOnClickListener(this);

        txtItemNo = findViewById(R.id.txt_shipment_id);
        txtReqQty = findViewById(R.id.txt_shipment_quantity);
        txtItemDesc = findViewById(R.id.txt_item_description);
        txtPackQty = findViewById(R.id.txt_checking_pass);
        txtUnPackQty = findViewById(R.id.txt_wait_for_checking);
        txtDelQty = findViewById(R.id.txt_prechecking_quantity);
        txtPalletNo = findViewById(R.id.txt_pallet_number);

        rbDcNo = findViewById(R.id.radio_dc_no);
        rbPallet = findViewById(R.id.radio_pallet);
        rbBox = findViewById(R.id.radio_box);
        RadioGroup rbGroup = findViewById(R.id.radio_group);
        rbGroup.setOnCheckedChangeListener(listener);

        etDn = findViewById(R.id.edittext_import_dn);
        etDn.setOnEditorActionListener(editorActionListener);
        lblCartonNoDcNo = findViewById(R.id.label_import_cn_dc_no);
        etCartonNoDcNo = findViewById(R.id.edittext_import_cn_dc_no);
        etCartonNoDcNo.setOnEditorActionListener(editorActionListener);

        llSn = findViewById(R.id.snLayout);

        rbImportSn = findViewById(R.id.radiobutton_import_sn);
        rbImportSn.setOnCheckedChangeListener(rbListener);
        etSn = findViewById(R.id.edittext_import_sn);
        etSn.setOnEditorActionListener(editorActionListener);

        rbImportCn = findViewById(R.id.radiobutton_import_cn);
        rbImportCn.setOnCheckedChangeListener(rbListener);
        etCartonNo = findViewById(R.id.edittext_import_cn);
        etCartonNo.setOnEditorActionListener(editorActionListener);

        rbImportQrCode = findViewById(R.id.radiobutton_import_qrcode);
        rbImportQrCode.setOnCheckedChangeListener(rbListener);
        etQrCode = findViewById(R.id.edittext_import_qrcode);
        etQrCode.setOnEditorActionListener(editorActionListener);

        Button btnConfirm = findViewById(R.id.button_confirm);
        btnConfirm.setOnClickListener(this);

        Button btnCancel = findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(this);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(this);

        lblTitle = findViewById(R.id.ap_title);
        setTitle();
        rbBox.setChecked(true);
        handler = new ShipmentPickingHandler();
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.label_shipment_return1, AppController.getOrgName()));
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etDn.getWindowToken(), 0);
    }

    private void setSummary(ReturnInfoHelper info) {
        if (info == null) {
            txtItemNo.setText("");
            txtItemDesc.setText("");
            txtReqQty.setText("");
            txtPackQty.setText("");
            txtUnPackQty.setText("");
            txtDelQty.setText("");
            txtPalletNo.setText("");
            etCartonNo.setText("");
            etCartonNoDcNo.setText("");
        } else {
            txtItemNo.setText(info.getItemNo());
            txtItemDesc.setText(info.getItemDesc());
            txtReqQty.setText(Util.fmt(info.getReqQty()));
            txtPackQty.setText(Util.fmt(info.getPackQty()));
            txtUnPackQty.setText(Util.fmt(info.getUnPackQty()));
            txtDelQty.setText(Util.fmt(info.getDelQty()));
            txtPalletNo.setText(info.getPalletNo());
            etCartonNo.setText(info.getCartonNo());
            etCartonNoDcNo.setText(info.getCartonNo());
        }
    }

    private void cleanData() {
        etDn.setText("");
        etCartonNoDcNo.setText("");
        etSn.setText("");
        etCartonNo.setText("");
        etQrCode.setText("");
        etDn.requestFocus();
        returnInfo = null;
        setSummary(returnInfo);
    }

    private boolean checkCondition() {
        if (etDn.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_dn), Toast.LENGTH_LONG).show();
            etDn.requestFocus();
            return false;
        }

        if (rbBox.isChecked() || rbPallet.isChecked()) {
            if (rbImportSn.isChecked()) {
                if (etSn.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_sn), Toast.LENGTH_LONG).show();
                    etSn.requestFocus();
                    return false;
                }
            } else if (rbImportCn.isChecked()) {
                if (etCartonNo.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_box_nu), Toast.LENGTH_LONG).show();
                    etCartonNo.requestFocus();
                    return false;
                }
            } else {
                if (etQrCode.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_qrcode), Toast.LENGTH_LONG).show();
                    etQrCode.requestFocus();
                    return false;
                }
            }
        } else {
            if (etCartonNoDcNo.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_box_nu), Toast.LENGTH_LONG).show();
                etCartonNoDcNo.requestFocus();
                return false;
            }
        }

        if (returnInfo == null || (TextUtils.isEmpty(returnInfo.getCartonNo()) && TextUtils.isEmpty(returnInfo.getPalletNo()))) {
            Toast.makeText(getApplicationContext(), getString(R.string.return_info_is_incorrect), Toast.LENGTH_LONG).show();

            if (rbPallet.isChecked() || rbBox.isChecked()) {
                if (rbImportSn.isChecked()) {
                    etSn.requestFocus();
                } else if (rbImportCn.isChecked()) {
                    etCartonNo.requestFocus();
                } else {
                    etQrCode.requestFocus();
                }
            } else {
                etCartonNoDcNo.requestFocus();
            }
        }

        return true;
    }

    private void checkDn(String dn) {
        hideKeyboard();
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_chking), true);
        returnInfo = new ReturnInfoHelper();
        returnInfo.setDeliveryId(dn);

        if (rbPallet.isChecked()) {
            returnInfo.setType("P");
        } else if (rbBox.isChecked()) {
            returnInfo.setType("B");
        } else {
            returnInfo.setType("B");
        }

        new CheckDn().execute(0);
    }

    //type: 1.棧板 2.箱號 3.其它
    private void checkValue(String type, String dn, String sn, String cn) {
        hideKeyboard();
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_chking), true);
        returnInfo = new ReturnInfoHelper();
        returnInfo.setDeliveryId(dn);
        returnInfo.setType(type);
        returnInfo.setSn(sn);
        returnInfo.setCartonNo(cn);
        new CheckValue().execute(0);
    }

    private void returnPicking() {
        hideKeyboard();
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.processing), true);
        new ReturnPicking().execute(0);
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

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.button_return) {
            finish();
        } else if (id == R.id.button_confirm) {
            hideKeyboard();

            if (checkCondition()) {
                returnPicking();
            }
        } else if (id == R.id.button_cancel) {
            cleanData();
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
        }
    }

    private class CheckDn extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Return Picking Check Dn" + AppController.getServerInfo()
                    + AppController.getProperties("ReturnCheckDn"));
            publishProgress(getString(R.string.data_chking));
            return handler.checkDn(returnInfo);
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
                    errorInfo = "";
                    mConnection.setText(result.getStrErrorBuf());
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));

                    if (rbPallet.isChecked() || rbBox.isChecked()) {
                        if (rbImportSn.isChecked()) {
                            etSn.requestFocus();
                        } else if (rbImportCn.isChecked()) {
                            etCartonNo.requestFocus();
                        } else {
                            etQrCode.requestFocus();
                        }
                    } else {
                        etCartonNoDcNo.requestFocus();
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
                    etDn.requestFocus();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class CheckValue extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Return Picking Check Value " + AppController.getServerInfo()
                    + AppController.getProperties("ReturnCheckValue"));
            publishProgress(getString(R.string.data_chking));
            return handler.checkValue(returnInfo);
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
                    errorInfo = "";
                    returnInfo = (ReturnInfoHelper) result;
                    mConnection.setText(result.getStrErrorBuf());
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    setSummary(returnInfo);
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));

                    if (rbPallet.isChecked() || rbBox.isChecked()) {
                        if (rbImportSn.isChecked()) {
                            etSn.requestFocus();
                        } else if (rbImportCn.isChecked()) {
                            etCartonNo.requestFocus();
                        } else {
                            etQrCode.requestFocus();
                        }
                    } else {
                        etCartonNoDcNo.requestFocus();
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

    private class ReturnPicking extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Return Picking " + AppController.getServerInfo()
                    + AppController.getProperties("ReturnPicking"));
            publishProgress(getString(R.string.processing));
            return handler.returnPicking(returnInfo);
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
                    errorInfo = "";
                    mConnection.setText(result.getStrErrorBuf());
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    cleanData();
                    Toast.makeText(getApplicationContext(), getString(R.string.Picking_data_has_been_deleted), Toast.LENGTH_LONG).show();
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
