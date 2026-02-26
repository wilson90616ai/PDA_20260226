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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.TransferProcessInfoHelper;
import com.senao.warehouse.handler.OrgHandler;
import com.senao.warehouse.handler.OrgHelper;
import com.senao.warehouse.handler.TransferHandler;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.ReturnCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//轉倉轉儲位作業
public class TransferSubinventoryLocatorActivity extends Activity implements View.OnClickListener {

    private EditText txtPn, txtSubinventoryFrom, txtLocatorFrom, txtSubinventoryTo, txtLocatorTo,txtReference;
    private Button btnReturn, btnConfirm, btnCancel;
    private TextView mConnection,lblTitle;
    private String errorInfo = "";
    private TransferProcessInfoHelper processInfoHelper;
    private ProgressDialog dialog;
    private TransferHandler transferHandler;
    private static final String[] NOT_ALLOWED_SUBS = {"304","310","318","322","326"};
    private List<String> LIST_NOT_ALLOWED_SUBS = new ArrayList<>();
    private OrgHandler orgHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_subinventory_loctor);
        btnReturn =  findViewById(R.id.button_return);

        txtPn =  findViewById(R.id.edittext_pn);
        txtPn.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                final boolean isEnterEvent = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
                final boolean isEnterUpEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_UP;
                final boolean isEnterDownEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_DOWN;

                // Capture this event to receive ACTION_UP
                // We do not care on other actions
                if (actionId == EditorInfo.IME_ACTION_NEXT || isEnterUpEvent) {
                    // Do your action here
                    EditText et = (EditText) textView;

                    if (et.getText().toString().trim().equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.enter_sku), Toast.LENGTH_SHORT).show();
                        return true;
                    } else {
                        txtPn.setText(parsePartNo(txtPn.getText().toString().trim()));
                        txtPn.setSelection(txtPn.getText().length());
                        return false;
                    }
                } else return isEnterDownEvent;
            }
        });

        txtSubinventoryFrom =  findViewById(R.id.edittext_subinventory_from);
        txtSubinventoryTo =  findViewById(R.id.edittext_subinventory_to);

        txtLocatorFrom =  findViewById(R.id.edittext_locator_from);
        txtLocatorTo =  findViewById(R.id.edittext_locator_to);

        txtReference =  findViewById(R.id.edittext_reference);

        btnConfirm =  findViewById(R.id.button_confirm);
        btnConfirm.setOnClickListener(this);

        btnCancel =  findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(this);

        mConnection =  findViewById(R.id.label_status);
        mConnection.setOnClickListener(this);
        btnReturn.setOnClickListener(this);

        transferHandler = new TransferHandler();

        lblTitle = findViewById(R.id.ap_title);
        setTitle();

        if(Constant.ISORG){
            getSubs();//ORGOU專案
        }
    }

    private void getSubs() {
        orgHandler = new OrgHandler();
        new GetNotAllowedSubs02().execute(0);
    }

    private class GetNotAllowedSubs02 extends AsyncTask<Integer, String, OrgHelper> {
        @Override
        protected OrgHelper doInBackground(Integer... params) {
            AppController.debug("Get Not Allowed Subs " + AppController.getServerInfo()
                    + AppController.getProperties("GetNotAllowedSubs02"));
//            publishProgress("資料下載中...");
            return orgHandler.getNotAllowedSubs02(new OrgHelper());
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("GetOrgInfo onProgressUpdate() " + text[0]);
//            mConnection.setText(text[0]);
//            mConnection.setTextColor(Color.WHITE);
//            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        @Override
        protected void onPostExecute(OrgHelper result) {
            // execution of result of Long time consuming operation //ORGOU專案
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            AppController.debug("TransferSubinventoryLocatorActivity GetNotAllowedSubs02 result json = "  + new Gson().toJson(result));

            if (TextUtils.isEmpty(result.getStrErrorBuf())) {
                LIST_NOT_ALLOWED_SUBS.clear();
                LIST_NOT_ALLOWED_SUBS = result.getSubs();
            } else {
                mConnection.setText(getString(R.string.Controlled_subinventory) + getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.title_activity_transfer_subinventory_locator1, AppController.getOrgName()));
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

    private boolean checkCondition() {
        if (txtPn.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_sku), Toast.LENGTH_LONG).show();
            txtPn.requestFocus();
            return false;
        }

        if (txtSubinventoryFrom.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(),  getString(R.string.import_subinventory_from), Toast.LENGTH_LONG).show();//"請輸入轉出倉別"
            txtSubinventoryFrom.requestFocus();
            return false;
        }

//        if (Arrays.asList(NOT_ALLOWED_SUBS).contains(txtSubinventoryFrom.getText().toString().trim())) {
        if (LIST_NOT_ALLOWED_SUBS.contains(txtSubinventoryFrom.getText().toString().trim())) {
            Toast.makeText(getApplicationContext(), getString(R.string.sub_controlled_enter_correct_sub)+"(Out)", Toast.LENGTH_SHORT).show();//"此倉別為管制倉別，請填寫正確的轉出倉別"
            txtSubinventoryFrom.requestFocus();
            return false;
        }

        if (txtLocatorFrom.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.import_locator_from), Toast.LENGTH_LONG).show();
            txtLocatorFrom.requestFocus();
            return false;
        }

        if (txtSubinventoryTo.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.import_subinventory_to), Toast.LENGTH_LONG).show();
            txtSubinventoryTo.requestFocus();
            return false;
        }

//        if (Arrays.asList(NOT_ALLOWED_SUBS).contains(txtSubinventoryTo.getText().toString().trim())) {
        if (LIST_NOT_ALLOWED_SUBS.contains(txtSubinventoryTo.getText().toString().trim())) {
            Toast.makeText(getApplicationContext(),getString(R.string.sub_controlled_enter_correct_sub)+"(IN)" ,
                    Toast.LENGTH_SHORT).show();//"此倉別為管制倉別，請填寫正確的轉入倉別"
            txtSubinventoryTo.requestFocus();
            return false;
        }

        if (txtLocatorTo.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.import_locator_to), Toast.LENGTH_LONG).show();
            txtLocatorTo.requestFocus();
            return false;
        }

        if (txtSubinventoryTo.getText().toString().trim().equals(txtSubinventoryFrom.getText().toString().trim())
                && txtLocatorTo.getText().toString().trim().equals(txtLocatorFrom.getText().toString().trim())) {
            Toast.makeText(getApplicationContext(), getString(R.string.out_and_in_loc_not_same), Toast.LENGTH_LONG).show(); //轉出的儲位不可與轉入的相同
            txtLocatorTo.requestFocus();
            return false;
        }

        if (txtReference.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.Enter_remarks), Toast.LENGTH_LONG).show();
            txtReference.requestFocus();
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

        if (imm != null) {
            imm.hideSoftInputFromWindow(txtPn.getWindowToken(), 0);
        }
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
                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.dismiss();
                            }
                        });

                dialog.show();
            }
        } else if (id == R.id.button_cancel) {
            clearCondition();
        } else if (id == R.id.button_confirm) {
            doQueryTransferInfo();
        }
    }

    private void clearCondition() {
        txtPn.setText("");
        txtSubinventoryFrom.setText("");
        txtLocatorFrom.setText("");
        txtSubinventoryTo.setText("");
        txtLocatorTo.setText("");
        txtReference.setText("");
    }

    private void doQueryTransferInfo() {
        if (checkCondition()) {
            processInfoHelper = new TransferProcessInfoHelper();
            processInfoHelper.setPartNo(txtPn.getText().toString().trim());
            processInfoHelper.setSubinventory(txtSubinventoryFrom.getText().toString().trim());
            processInfoHelper.setLocator(txtLocatorFrom.getText().toString().trim());
            processInfoHelper.setTransSubinventory(txtSubinventoryTo.getText().toString().trim());
            processInfoHelper.setTransLocator(txtLocatorTo.getText().toString().trim());
            processInfoHelper.setReference(txtReference.getText().toString().trim());
            dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
            new GetTransferInfo().execute(0);
        }
    }

    private void goToProcess() {
        Intent intent = new Intent(this, TransferSubinventoryLocatorProcessActivity.class);
        intent.putExtra("CONDITION_INFO", new Gson().toJson(processInfoHelper));
        startActivity(intent);
    }

    private class GetTransferInfo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            publishProgress(getString(R.string.downloading_data));
            AppController.debug("Get Tansfer Data from " + AppController.getServerInfo()
                    + AppController.getProperties("TransferInfo"));
            return transferHandler.getTransferInfo(processInfoHelper);
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
                    processInfoHelper = (TransferProcessInfoHelper) result;
                    goToProcess();
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
