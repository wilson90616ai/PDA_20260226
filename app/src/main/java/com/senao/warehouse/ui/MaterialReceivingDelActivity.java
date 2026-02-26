package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.R;
import com.senao.warehouse.adapter.ListIDeltemAdapter;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.RECEIVING_TYPE;
import com.senao.warehouse.database.ReceivingInfoHelper;
import com.senao.warehouse.database.VendorInfoHelper;
import com.senao.warehouse.handler.ReceivingHandler;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;

import java.util.ArrayList;
import java.util.List;

public class MaterialReceivingDelActivity extends Activity implements View.OnClickListener {

    private static final String TAG = MaterialReceivingDelActivity.class.getSimpleName();
    private ListView listView;
    private Button btnAllSel, btnUnSel, btnDel, btnReturn;
    private ListIDeltemAdapter adapter;
    private List<ReceivingInfoHelper> dataList;
    private EditText txtInputSearchText;
    private TextView lblTitle, mConnection, lblTotalQty, lblSelectedQty, lblDelTitle;
    private String errorInfo;
    private boolean isInSearchMode;
    private ReceivingHandler handler;
    private VendorInfoHelper vendorInfo;
    private VendorInfoHelper tempVendorInfo;
    private VendorInfoHelper processVendorInfo;
    private ProgressDialog dialog;
    private boolean needRefresh;
    private int selectedCNT = 0;
    private RECEIVING_TYPE mReceivingType;
    private String receivingType;
    private int accType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_receiving_del);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            receivingType = extras.getString("RECEIVING_TYPE");

            if (TextUtils.isEmpty(receivingType)) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_method), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            handler = new ReceivingHandler();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_method), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        lblTitle =  findViewById(R.id.ap_title);

        accType = getIntent().getIntExtra("TYPE", 0);

        if (accType == MaterialReceivingActivity.OUTSOURCING) {
            lblTitle.setText(R.string.label_material_receiving_outsourcing);
        }

        lblDelTitle =  findViewById(R.id.label_del_title);

        listView =  findViewById(R.id.list_item);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), "Click position : " + position, Toast.LENGTH_LONG).show();
                setCheckedQtyTitle();
            }
        });

        btnAllSel =  findViewById(R.id.btn_select_all);
        btnAllSel.setOnClickListener(this);

        btnUnSel =  findViewById(R.id.btn_select_invert);
        btnUnSel.setOnClickListener(this);

        txtInputSearchText =  findViewById(R.id.input_search_text);
        lblTotalQty =  findViewById(R.id.label_total_qty);
        lblSelectedQty =  findViewById(R.id.label_select_qty);

        btnDel =  findViewById(R.id.button_del);
        btnDel.setOnClickListener(new View.OnClickListener() {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            // Yes button clicked
                            needRefresh = false;
                            doDelProcess();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            // No button clicked
                            break;
                    }
                }
            };

            @Override
            public void onClick(View v) {
                if (selectedCNT > 0) {
                    Log.d("onClick Del", String.valueOf(v.getId()));
                    AlertDialog.Builder builder = new AlertDialog.Builder(MaterialReceivingDelActivity.this);
                    builder.setTitle( getString(R.string.btn_ok)).setMessage(R.string.Perform_delete)
                            .setPositiveButton(getString(R.string.yes), dialogClickListener)
                            .setNegativeButton(getString(R.string.no), dialogClickListener).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.choose_u_want_to_del), Toast.LENGTH_LONG).show();
                }
            }

        });

        btnReturn =  findViewById(R.id.button_return);

        mConnection =  findViewById(R.id.label_status);

        txtInputSearchText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down" + String.valueOf(v.getId()));

                    if (txtInputSearchText.getText().toString().trim().equals("")) {
                        Toast.makeText(MaterialReceivingDelActivity.this, getString(R.string.query_cons_cant_be_null), Toast.LENGTH_SHORT).show();
                        txtInputSearchText.requestFocus();
                    } else {
                        isInSearchMode = true;
                        hideKeyboard();
                        getSearchData(txtInputSearchText.getText().toString().trim());
                    }

                    return true;
                }

                return false;
            }
        });

        txtInputSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (isInSearchMode && TextUtils.isEmpty(s)) {
                    isInSearchMode = false;
                    hideKeyboard();
                    clearStatus();
                    setListData(vendorInfo);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        btnReturn.setOnClickListener(this);
        mConnection.setOnClickListener(this);

        mReceivingType = RECEIVING_TYPE.valueOf(receivingType);
        setTitle(mReceivingType);
        doQueryReceivingInfoList();
    }

    private void setTitle(RECEIVING_TYPE receivingType) {
        if (receivingType == RECEIVING_TYPE.DEL_ERROR) {
            lblTitle.setText(getString(R.string.label_receiving, getString(R.string.label_del_error)));
            lblDelTitle.setText(getString(R.string.label_del_error));
        } else {
            lblTitle.setText(getString(R.string.label_receiving, getString(R.string.label_del_delivery)));
            lblDelTitle.setText(getString(R.string.label_del_delivery));
        }
    }

    private void setListData(final VendorInfoHelper tempInfo) {
        listView.setVisibility(View.VISIBLE);
        dataList = new ArrayList<>();

        if (tempInfo != null && tempInfo.getRecvList() != null) {
            lblTotalQty.setText(getString(R.string.label_total_qty, tempInfo.getRecvList().length));
            for (ReceivingInfoHelper info : tempInfo.getRecvList()) {
                dataList.add(info);
            }
        }

        lblTotalQty.setText(getString(R.string.label_total_qty, dataList.size()));
        setCheckedQtyTitle();
        adapter = new ListIDeltemAdapter(this, mReceivingType, dataList);
        listView.setAdapter(adapter);

        if (dataList.size() == 0)
            Toast.makeText(getApplicationContext(), getString(R.string.no_data), Toast.LENGTH_LONG).show();
    }

    private void clearStatus() {
        mConnection.setText("");
        mConnection.setTextColor(Color.WHITE);
        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        errorInfo = "";
    }

    private void getSearchData(String text) {
        if (vendorInfo != null && vendorInfo.getRecvList() != null && vendorInfo.getRecvList().length > 0) {
            List<ReceivingInfoHelper> itemInfoList = new ArrayList<>();

            for (ReceivingInfoHelper item : vendorInfo.getRecvList()) {
                if (find(item, text)) {
                    itemInfoList.add(item);
                }
            }

            if (itemInfoList.size() > 0) {
                tempVendorInfo = new VendorInfoHelper();
                tempVendorInfo.setOutSourcing(vendorInfo.isOutSourcing());
                //tempVendorInfo.setNum(vendorInfo.getNum());
                //tempVendorInfo.setName(vendorInfo.getName());
                tempVendorInfo.setReceivingType(vendorInfo.getReceivingType());
                tempVendorInfo.setRecvList(itemInfoList);
                setListData(tempVendorInfo);
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.no_data), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.no_data), Toast.LENGTH_LONG).show();
        }
    }

    private boolean find(ReceivingInfoHelper item, String text) {
        if (item.getPartNo() != null && item.getPartNo().indexOf(text) >= 0) {
            return true;
        } else if (item.getPo() != null && item.getPo().indexOf(text) >= 0) {
            return true;
        } else if (item.getInvoiceNo() != null && item.getInvoiceNo().indexOf(text) >= 0) {
            return true;
        } else if (String.valueOf(item.getVendorId()).indexOf(text) >= 0) {
            return true;
        } else if (item.getVendorName() != null && item.getVendorName().indexOf(text) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    private void setCheckedQtyTitle() {
        int count = 0;

        for (int i = 0; i < dataList.size(); i++) {
            if (listView.isItemChecked(i)) {
                count++;
            }
        }

        selectedCNT = count;
        lblSelectedQty.setText(getString(R.string.label_select_qty, count));
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtInputSearchText.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_select_all) {
            for (int i = 0; i < dataList.size(); i++) {
                listView.setItemChecked(i, true);
            }

            setCheckedQtyTitle();
        } else if (id == R.id.btn_select_invert) {
            for (int i = 0; i < dataList.size(); i++) {
                listView.setItemChecked(i, false);
            }

            setCheckedQtyTitle();
        } else if (id == R.id.button_return) {
            finish();
        } else if (id == R.id.label_status) {
            if (!TextUtils.isEmpty(errorInfo)) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MaterialReceivingDelActivity.this);
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

    private void doQueryReceivingInfoList() {
        vendorInfo = new VendorInfoHelper();
        vendorInfo.setOutSourcing(accType == 1);
        vendorInfo.setReceivingType(receivingType);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetReceivingInfoList().execute(0);
    }

    private void doDelProcess() {
        List<ReceivingInfoHelper> list = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            if (listView.isItemChecked(i)) {
                list.add(dataList.get(i));
            }
        }

        processVendorInfo = new VendorInfoHelper();
        processVendorInfo.setOutSourcing(accType == 1);
        processVendorInfo.setRecvList(list);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.del_data), true);
        new DoDelProcess().execute(0);
    }

    private class GetReceivingInfoList extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Get StockInfo from " + AppController.getServerInfo()
                    + AppController.getProperties("GetReceivingInfoList"));
            publishProgress(getString(R.string.downloading_data));
            return handler.getReceivingInfoList(vendorInfo);
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
                vendorInfo = (VendorInfoHelper) result;

                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
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

                setListData(vendorInfo);
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                setListData(null);
            }
        }
    }

    private class DoDelProcess extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Do receiving process from " + AppController.getServerInfo()
                    + AppController.getProperties("DeleteReceivingData"));
            publishProgress(getString(R.string.del_data));
            return handler.doDelReceivingData(processVendorInfo);
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
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    needRefresh = true;
                    clearStatus();
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
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

                if (needRefresh) {
                    txtInputSearchText.setText("");
                    doQueryReceivingInfoList();
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
