package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.RECEIVING_TYPE;
import com.senao.warehouse.database.UserInfoHelper;
import com.senao.warehouse.database.VendorInfoHelper;
import com.senao.warehouse.database.VendorInfoListHelper;
import com.senao.warehouse.handler.ReceivingHandler;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.DatabaseHelper;
import com.senao.warehouse.util.ReturnCode;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MaterialReceivingActivity extends Activity {
    /*
     * 20251113 Ann Edit:因應神準進料後轉賣給越南神準不換標
     *                   當前Org與QR Code的Org不同，需確認是否存在SSFTP的LOOKUP_VALUE(LOOKUP_CATALOG = 'PDA' AND LOOKUP_CODE = 'RECEIVING_ORG' AND ENABLED = 'Y')，如果存在，則PASS
     */

    public static final int REGULAR = 0;
    public static final int OUTSOURCING = 1;
    static final String STATE_RECEIVING_TYPE = "RECEIVING_TYPE";
    private static final String TAG = MaterialReceivingActivity.class.getSimpleName();
    private Button btnReturn, btnConfirm, btnCancel;
    private RadioButton rbVendorCode, rbVendorName, rbPn;
    private EditText txtVendorCode, txtVendorName, txtPn;
    private TextView lblTitle, mConnection, mEmployeeId, mAccount;
    private String errorInfo = "";
    private UserInfoHelper user = AppController.getUser();
    private VendorInfoHelper vendorInfo;
    //private List<VendorInfoHelper> vendorInfoList;
    private VendorInfoHelper[] vendorList;
    private LinearLayout checkorg;
    private ProgressDialog dialog;
    private ReceivingHandler receivingHandler;
    private String receivingType;
    private Dialog alertDialog;
    private int accType;

    private CompoundButton.OnCheckedChangeListener rbListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                if (buttonView.equals(rbVendorCode)) {
                    rbVendorName.setChecked(false);
                    rbPn.setChecked(false);
                    txtVendorCode.setEnabled(true);
                    txtVendorName.setText("");
                    txtVendorName.setEnabled(false);
                    txtPn.setText("");
                    txtPn.setEnabled(false);
                    txtVendorCode.requestFocus();
                } else if (buttonView.equals(rbVendorName)) {
                    rbVendorCode.setChecked(false);
                    rbPn.setChecked(false);
                    txtVendorCode.setText("");
                    txtVendorCode.setEnabled(false);
                    txtVendorName.setEnabled(true);
                    txtPn.setText("");
                    txtPn.setEnabled(false);
                    txtVendorName.requestFocus();
                } else {
                    rbVendorCode.setChecked(false);
                    rbVendorName.setChecked(false);
                    txtVendorCode.setText("");
                    txtVendorCode.setEnabled(false);
                    txtVendorName.setText("");
                    txtVendorName.setEnabled(false);
                    txtPn.setEnabled(true);
                    txtPn.requestFocus();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();

            if (extras != null) {
                receivingType = extras.getString(STATE_RECEIVING_TYPE);

                if (TextUtils.isEmpty(receivingType)) {
                    Toast.makeText(getApplicationContext(),getString(R.string.cant_find_method) , Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_method), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            receivingType = savedInstanceState.getString(STATE_RECEIVING_TYPE);
        }

        setContentView(R.layout.activity_material_receiving);

        fetch_SSFTP_Async("sSSFmethod_GetSSFTP_VALUE", "PDA", "RECEIVING_ORG"); //20251113 Ann Edit:先取得當前Org與QR Code不同時，需要Pass的Org有哪些

        lblTitle = findViewById(R.id.ap_title);

        accType = getIntent().getIntExtra("TYPE", 0);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MaterialReceivingActivity.this);
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

        mEmployeeId = findViewById(R.id.textview_employee_id);
        mEmployeeId.setText(String.valueOf(user.getPassword()));

        mAccount = findViewById(R.id.textview_login_account);
        mAccount.setText(user.getUserName());

        rbVendorCode = findViewById(R.id.radio_vendor_code);
        txtVendorCode = findViewById(R.id.edittext_import_vendor_code);
        txtVendorCode.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtVendorCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    Log.d(TAG, "onKeyListener Enter or Action Down" + v.getId());

                    if (txtVendorCode.getText().toString().trim().length() > 0) {
                        hideKeyboard();
                        doCheckVendorInfo();
                    }

                    return true;
                }

                return false;
            }
        });

        rbVendorName = findViewById(R.id.radio_vendor_name);
        txtVendorName = findViewById(R.id.edittext_import_vendor_name);
        txtVendorName.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtVendorName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    Log.d(TAG, "onKeyListener Enter or Action Down" + String.valueOf(v.getId()));

                    if (txtVendorName.getText().toString().trim().length() > 0) {
                        hideKeyboard();
                        doCheckVendorInfo();
                    }

                    return true;
                }

                return false;
            }
        });

        rbPn = findViewById(R.id.radio_pn);
        rbPn.setOnCheckedChangeListener(rbListener);

        txtPn = findViewById(R.id.edittext_import_pn);
        //txtPn.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        txtPn.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtPn.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    Log.d(TAG, "onKeyListener Enter or Action Down" + v.getId());
                    String st = txtPn.getText().toString().trim();

                    if(Constant.ISORG && parseOrg(st)){//判斷有沒有"@" 有的話解析ORG 比對登入ORG
                        Toast.makeText(getApplicationContext(), getString(R.string.Does_not_match_current_ORG), Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    if (txtPn.getText().toString().trim().length() > 0) {
                        txtPn.setText(parsePartNo(txtPn.getText().toString().trim()));
                        txtPn.setSelection(txtPn.getText().length());
                        hideKeyboard();
                        doCheckVendorInfo();
                    }

                    return true;
                }

                return false;
            }
        });

        rbVendorCode.setOnCheckedChangeListener(rbListener);
        rbVendorName.setOnCheckedChangeListener(rbListener);
        rbVendorCode.setChecked(true);

        btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Return", String.valueOf(v.getId()));
                returnPage();
            }

        });

        btnConfirm = findViewById(R.id.button_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Confim", String.valueOf(v.getId()));

                if (rbVendorCode.isChecked()) {
                    if (txtVendorCode.getText().toString().trim().equals("")) {
                        Toast.makeText(MaterialReceivingActivity.this, getString(R.string.enter_vc), Toast.LENGTH_SHORT).show();
                        txtVendorCode.requestFocus();
                        return;
                    }
                } else if (rbVendorName.isChecked()) {
                    if (txtVendorName.getText().toString().trim().equals("")) {
                        Toast.makeText(MaterialReceivingActivity.this, getString(R.string.enter_vendor_name), Toast.LENGTH_SHORT).show();
                        txtVendorName.requestFocus();
                        return;
                    }
                } else {
                    if (txtPn.getText().toString().trim().equals("")) {
                        Toast.makeText(MaterialReceivingActivity.this, getString(R.string.enter_sku), Toast.LENGTH_SHORT).show();
                        txtPn.requestFocus();
                        return;
                    }

                    if (Constant.ISORG && parseOrg(txtPn.getText().toString().trim())) { //判斷有沒有"@" 有的話解析ORG 比對登入ORG
                        Toast.makeText(getApplicationContext(), getString(R.string.Does_not_match_current_ORG), Toast.LENGTH_SHORT).show();
                        return ;
                    }
                }

                doCheckVendorInfo();
            }
        });


        checkorg = findViewById(R.id.checkorg);
        if(Constant.ISORG){
            checkorg.setVisibility(View.GONE);
        }else{
            checkorg.setVisibility(View.GONE);
        }

        btnCancel = findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Cancel", String.valueOf(v.getId()));
                cleanData();
            }
        });

        receivingHandler = new ReceivingHandler();
        setTitle(RECEIVING_TYPE.valueOf(receivingType));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_RECEIVING_TYPE, receivingType);
        super.onSaveInstanceState(outState);
    }

    private String parsePartNo(String trim) {
        if (trim.length() > 12) {
            return trim.substring(0, 12);
        } else {
            return trim;
        }
    }

    private boolean parseOrg(String trim) {
        if (!TextUtils.isEmpty(trim)&&trim.contains("@")) {
            try {
//                if(trim.split("@").length==3){//暫時讓沒輸入org的部分先通過 2022 1/1之後要拿掉 todo
//                    return false;
//                }

                if(!String.valueOf(AppController.getOrg()).equals(trim.split("@")[3])){ //根據材料標籤上的QRcode判斷ORG  格式:ReelId@空白@數量@ORG
                    if(AppController.getReceivingOrg().contains(String.valueOf(AppController.getOrg()))){ //20251113 Ann Edit:因應神準進料後轉賣給越南神準不換標
                        return false;
                    }else{
                        return true; //當前Org與QR Code的Org不同
                    }
                }
            } catch (Exception ex) {
                return false;
            }

            return false;
        } else {
            return false;
        }
    }

    /* 20251113 Ann Edit:因應神準進料後轉賣給越南神準不換標
     *                  先取得當前Org與QR Code不同時，需要Pass的Org有哪些
     */
    private void fetch_SSFTP_Async(final String API_Name, final String strLOOKUP_CATALOG, final String strLOOKUP_CODE) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                // 呼叫 API
                return getRECEIVING_ORG_FromAPI(API_Name, strLOOKUP_CATALOG, strLOOKUP_CODE);
            }

            @Override
            protected void onPostExecute(String result) {
                // 解析 API 回傳的 JSON
                try {
                    JSONArray arr = new JSONArray(result);
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);

                        if (obj.has("LOOKUP_VALUE")) {
                            if (sb.length() > 0)
                                sb.append("@@");

                            sb.append(obj.getString("LOOKUP_VALUE"));
                        }
                    }

                    // 存入 AppController 全域變數
                    AppController.setReceivingOrg(sb.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }.execute();
    }

    /* 20251113 Ann Edit:因應神準進料後轉賣給越南神準不換標
     *                  先取得當前Org與QR Code不同時，需要Pass的Org有哪些
     */
    private String getRECEIVING_ORG_FromAPI(String API_Name, String strLOOKUP_CATALOG, String strLOOKUP_CODE) {
        String apiUrl = AppController.getSfcIp() + "/invoke" + AppController.getApi_2() + "?sCode=" + API_Name; //取得SSFTP的值
        String result = "";

        HttpClient httpClient = new DefaultHttpClient();
        httpClient.getConnectionManager().getSchemeRegistry().register(
                new Scheme("https", SSLSocketFactory.getSocketFactory(), 443)
        );

        try {
            JSONObject reqJson = new JSONObject();
            reqJson.put("LOOKUP_CATALOG", strLOOKUP_CATALOG);
            reqJson.put("LOOKUP_CODE", strLOOKUP_CODE);

            HttpPost httpPost = new HttpPost(apiUrl);

            StringEntity stringEntity = new StringEntity(reqJson.toString(), "UTF-8");
            httpPost.setEntity(stringEntity);

            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Authorization","Bearer "+LoginActivity.apiToken);
            httpPost.addHeader("Content-Type", "application/json; charset=utf-8");

            HttpResponse response = httpClient.execute(httpPost);

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity, "UTF-8");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AppController.debug("getRECEIVING_ORG_FromAPI Exception:: " + e.getMessage());
            return "[]"; // 最安全格式
        } finally {
            httpClient.getConnectionManager().shutdown();
        }

        return result;
    }

    private void setTitle(RECEIVING_TYPE type) {
        SpannableString text;

        if (accType == OUTSOURCING) {
            if (type == RECEIVING_TYPE.RECEIPTS){
                text = new SpannableString(getString(R.string.label_receiving_outsourcing1, AppController.getOrgName(),getString(R.string.Receiving)));
                text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                lblTitle.setText(text);
//                lblTitle.setText(getString(R.string.label_receiving_outsourcing, "收料"));
            }
            else{
                text = new SpannableString(getString(R.string.label_receiving_outsourcing1, AppController.getOrgName(),getString(R.string.Pending_inspection)));
                text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                lblTitle.setText(text);
//                lblTitle.setText(getString(R.string.label_receiving_outsourcing, "暫收"));
            }
        } else {
            if (type == RECEIVING_TYPE.RECEIPTS){
                text = new SpannableString(getString(R.string.label_receiving1, AppController.getOrgName(),getString(R.string.Receiving)));
                text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                lblTitle.setText(text);
//                lblTitle.setText(getString(R.string.label_receiving, "收料"));
            }
            else{
                text = new SpannableString(getString(R.string.label_receiving1, AppController.getOrgName(),getString(R.string.Pending_inspection)));
                text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                lblTitle.setText(text);
//                lblTitle.setText(getString(R.string.label_receiving, "暫收"));
            }
        }
    }

    private void returnPage() {
        hideKeyboard();
        finish();
    }

    private Dialog onCreateVendorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.list, null);
        builder.setView(view).setTitle(getString(R.string.Select_vendor));
        ListView tempListView = view.findViewById(R.id.lv);

        final String ID_TITLE = "TITLE", ID_SUBTITLE = "SUBTITLE";

        String titles[] = new String[vendorList.length];
        String[] subtitles = new String[vendorList.length];

        for (int i = 0; i < vendorList.length; i++) {
            VendorInfoHelper item = vendorList[i];
            titles[i] = item.getName();
            subtitles[i] = item.getSiteCode();
        }

        ArrayList<HashMap<String, String>> myListData = new ArrayList<HashMap<String, String>>();

        for (int i = 0; i < titles.length; ++i) {
            HashMap<String, String> item = new HashMap<String, String>();
            item.put(ID_TITLE, titles[i]);
            item.put(ID_SUBTITLE, subtitles[i]);
            myListData.add(item);
        }

        tempListView.setAdapter(new SimpleAdapter(
                this,
                myListData,
                android.R.layout.simple_list_item_2,
                new String[]{ID_TITLE, ID_SUBTITLE},
                new int[]{android.R.id.text1, android.R.id.text2}));

        tempListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                vendorInfo = vendorList[position];
                vendorInfo.setUserName(user.getUserName());
                vendorInfo.setWareHouseNo(user.getPassword());
                alertDialog.dismiss();
                goToNextPage();
            }
        });

        alertDialog = builder.create();
        return alertDialog;
    }

    private void doSearchVendorList() {
        vendorInfo = new VendorInfoHelper();
        vendorInfo.setOutSourcing(accType == 1);
        vendorInfo.setWareHouseNo(user.getPassword());

        if (rbVendorCode.isChecked())
            vendorInfo.setNum(txtVendorCode.getText().toString().trim());
        else if (rbVendorCode.isChecked())
            vendorInfo.setName(txtVendorName.getText().toString().trim());
        else
            vendorInfo.setPartNo(txtPn.getText().toString().trim());

        vendorInfo.setReceivingType(receivingType);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.downloading_data), true);
        new GetVendorList().execute(0);
    }

    private void goToNextPage() {
        Intent intent = new Intent();
        intent.setClass(this, MaterialReceivingSubModeActivity.class);
        vendorInfo.setOutSourcing(accType == 1);
        vendorInfo.setReceivingType(receivingType);
        intent.putExtra("VENDOR_INFO", new Gson().toJson(vendorInfo));
        startActivityForResult(intent, 0);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtVendorCode.getWindowToken(), 0);
    }

    private void cleanData() {
        rbVendorCode.setChecked(true);
        txtVendorCode.setText("");
        txtVendorName.setText("");
        txtPn.setText("");
        vendorInfo = null;
    }

    public void onBackPressed() {
        Log.d(TAG, "onClick Hardware Back Button");
        returnPage();
    }

    private void doCheckVendorInfo() {
        vendorInfo = new VendorInfoHelper();
        vendorInfo.setOutSourcing(accType == 1);

        if (rbVendorCode.isChecked())
            vendorInfo.setNum(txtVendorCode.getText().toString().trim());
        else if (rbVendorName.isChecked())
            vendorInfo.setName(txtVendorName.getText().toString().trim());
        else{
            vendorInfo.setPartNo(txtPn.getText().toString().trim());
        }

        vendorInfo.setReceivingType(receivingType);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_chking), true);
        new CheckVendorInfo().execute(0);
    }

    private class CheckVendorInfo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check Vendor Info from " + AppController.getServerInfo()
                    + AppController.getProperties("CheckVendorInfo"));
            publishProgress(getString(R.string.downloading_data));
            return receivingHandler.checkVendorInfo(vendorInfo);
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
                    //vendorInfo = (VendorInfoHelper) result;
                    //goToNextPage();
                    VendorInfoListHelper list = (VendorInfoListHelper) result;
                    vendorList = list.getVendorList();
                    onCreateVendorDialog().show();
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

    private class GetVendorList extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Get Vendor List from " + AppController.getServerInfo()
                    + AppController.getProperties("GetVendorList"));
            publishProgress(getString(R.string.downloading_data));
            return receivingHandler.getVendorInfoList(vendorInfo);
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
                    VendorInfoListHelper list = (VendorInfoListHelper) result;
                    vendorList = list.getVendorList();
                    onCreateVendorDialog().show();
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
