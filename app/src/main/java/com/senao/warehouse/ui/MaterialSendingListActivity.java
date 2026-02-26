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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.SendOnHandQtyInfo;
import com.senao.warehouse.database.SendProcessInfoHelper;
import com.senao.warehouse.database.SendSubinventoryInfo;
import com.senao.warehouse.database.SendingInfoHelper;
import com.senao.warehouse.database.UserInfoHelper;
import com.senao.warehouse.handler.SendingHandler;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.util.Util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MaterialSendingListActivity extends Activity implements OnClickListener {

    private static final String TAG = MaterialSendingListActivity.class.getSimpleName();
    List<HashMap<String, Object>> data = new ArrayList<>();
    private MyAdapter adapter;
    private TextView mConnection, mEmployeeId, mAccount,lblTitle;
    private EditText txtInputSearchText;
    private Button btnReturn, btnDebit;
    private ListView listView;
    private UserInfoHelper user = AppController.getUser();
    private SendingHandler sendingHandler;
    private SendingInfoHelper sendInfo;
    private SendingInfoHelper tempSendInfo;
    private ProgressDialog dialog;
    private String errorInfo = "";
    private boolean isInSearchMode;
    private SendOnHandQtyInfo selectedItemInfoHelper;
    private boolean needRefresh;
    private int sendType;
    private int dataPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            sendType = extras.getInt("TYPE");

            if (sendType < MaterialSendingActivity.QUERY_TYPE_SEND) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_shipping_info), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            sendInfo = new Gson().fromJson(extras.getString("CONDITION_INFO"), SendingInfoHelper.class);

            if (sendInfo == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_shipping_info), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            sendingHandler = new SendingHandler();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_shipping_info), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_material_sending_list);
        TextView title = findViewById(R.id.ap_title);
        final SpannableString text;

        if (sendType == MaterialSendingActivity.QUERY_TYPE_SEND) {
            text = new SpannableString(getString(R.string.title_activity_material_sending_debit1, AppController.getOrgName()));
            text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            title.setText(R.string.title_activity_material_sending_debit);
            title.setText(text);
            sendInfo.setTrxType("I");
        } else {
            text = new SpannableString(getString(R.string.title_activity_material_sending_transfer1, AppController.getOrgName()));
            text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            title.setText(R.string.title_activity_material_sending_transfer);
            title.setText(text);
            sendInfo.setTrxType("T");
        }

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MaterialSendingListActivity.this);
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

        listView = findViewById(R.id.list_item);
        listView.setVisibility(View.GONE);

        mAccount = findViewById(R.id.textview_login_account);
        mAccount.setText(user.getUserName());

        mEmployeeId = findViewById(R.id.textview_employee_id);
        mEmployeeId.setText(String.valueOf(user.getPassword()));

        btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        btnDebit = findViewById(R.id.button_debit);
        btnDebit.setVisibility(View.INVISIBLE);
        btnDebit.setOnClickListener(new View.OnClickListener() {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            // Yes button clicked
                            needRefresh = false;

                            if (isInSearchMode)
                                doDebit(tempSendInfo);
                            else
                                doDebit(sendInfo);

                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            // No button clicked
                            break;
                    }
                }
            };

            @Override
            public void onClick(View v) {
                Log.d("onClick Stock In", String.valueOf(v.getId()));
                AlertDialog.Builder builder = new AlertDialog.Builder(MaterialSendingListActivity.this);
                builder.setTitle(getString(R.string.btn_ok)).setMessage(R.string.confirm_exec_debit)
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show();
            }
        });

        txtInputSearchText = findViewById(R.id.input_search_text);
        txtInputSearchText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down" + String.valueOf(v.getId()));

                    if (txtInputSearchText.getText().toString().trim().equals("")) {
                        Toast.makeText(MaterialSendingListActivity.this, getString(R.string.query_cons_cant_be_null), Toast.LENGTH_SHORT).show();
                        txtInputSearchText.requestFocus();
                    } else {
                        isInSearchMode = true;
                        hideKeyboard();
                        txtInputSearchText.setText(parsePartNo(txtInputSearchText.getText().toString().trim()));
                        txtInputSearchText.setSelection(txtInputSearchText.getText().length());
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
                    setListData(sendInfo);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        sendingHandler = new SendingHandler();
//        setTitle();
        doQuerySendInfo(null);
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.label_picking, AppController.getOrgName()));
        text.setSpan(new RelativeSizeSpan(1f), 0, AppController.getOrgName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        text.setSpan(new ForegroundColorSpan(Color.parseColor("#fa160a")), 0, AppController.getOrgName().length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private String parsePartNo(String trim) {
        if (trim.length() >= 28) {
            return trim.substring(0, 12);
        } else {
            return trim;
        }
    }

    private void clearStatus() {
        mConnection.setText("");
        mConnection.setTextColor(Color.WHITE);
        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        errorInfo = "";
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtInputSearchText.getWindowToken(), 0);
    }

    private void getSearchData(String text) {
        if (sendInfo != null && sendInfo.getOnHandQtyList() != null && sendInfo.getOnHandQtyList().length > 0) {
            List<SendOnHandQtyInfo> itemInfoList = new ArrayList<SendOnHandQtyInfo>();

            for (int i = 0; i < sendInfo.getOnHandQtyList().length; i++) {
                if (sendInfo.getOnHandQtyList()[i].getPartNo().indexOf(text) >= 0) {
                    itemInfoList.add(sendInfo.getOnHandQtyList()[i]);
                }
            }

            if (itemInfoList.size() > 0) {
                tempSendInfo = new SendingInfoHelper();
                tempSendInfo.setLineNo(sendInfo.getLineNo());
                tempSendInfo.setMergeNo(sendInfo.getMergeNo());
                tempSendInfo.setWhNo(sendInfo.getMergeNo());
                tempSendInfo.setWoNo(sendInfo.getWoNo());
                tempSendInfo.setOnHandQtyList(itemInfoList);
                setListData(tempSendInfo);
            } else {
                doQuerySendInfo(text);
            }
        } else {
            doQuerySendInfo(text);
        }
    }

    private void setListData(final SendingInfoHelper tempInfo) {
        listView.setVisibility(View.VISIBLE);
        btnDebit.setVisibility(View.INVISIBLE);
        data.clear();

        if (tempInfo != null && tempInfo.getOnHandQtyList() != null) {
            List<SendOnHandQtyInfo> tempList = new ArrayList<>();
            double totalInventory;

            for (SendOnHandQtyInfo info : tempInfo.getOnHandQtyList()) {
                //if (info.getUnsentQty().doubleValue() > 0) {
                if (info.isCanDebit())
                    btnDebit.setVisibility(View.VISIBLE);

                tempList.add(info);
                HashMap<String, Object> item = new HashMap<String, Object>();
                item.put("part_no", info.getPartNo());
                item.put("control", info.getControl());
                item.put("can_debit", info.isCanDebit());
                item.put("total_sending_qty", info.getTotalSendingQty().doubleValue());
                item.put("sent_qty", info.getSentQty().doubleValue());
                item.put("unsent_qty", info.getUnsentQty().doubleValue());
                item.put("unchecked_qty", info.getUncheckedQty().doubleValue());
                totalInventory = 0;

                if (info.getSubinventoryInfoList() != null) {
                    for (SendSubinventoryInfo subinventoryInfo : info.getSubinventoryInfoList()) {
                        totalInventory += subinventoryInfo.getInventory().doubleValue();
                    }
                }

                info.setOnHandQty(BigDecimal.valueOf(totalInventory));
                item.put("total_inventory", totalInventory);
                item.put("subinventory_list", info.getSubinventoryInfoList());
                data.add(item);
                // }
            }

            tempInfo.setOnHandQtyList(tempList);
        }

        adapter = new MyAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                dataPosition = position;
                selectedItemInfoHelper = tempInfo.getOnHandQtyList()[position];
                AppController.debug("onClick item " + position + " PartNo="
                        + selectedItemInfoHelper.getPartNo());

                if (selectedItemInfoHelper.getOnHandQty().doubleValue() > 0) {
                    if (selectedItemInfoHelper.getUnsentQty().doubleValue() > 0) {
                        Intent intent = null;

                        if (sendType == MaterialSendingActivity.QUERY_TYPE_SEND)
                            intent = new Intent(getBaseContext(), MaterialSendingProcessActivity.class);
                        else
                            intent = new Intent(getBaseContext(), MaterialSendingTransferActivity.class);

                        SendProcessInfoHelper processInfoHelper = new SendProcessInfoHelper();
                        processInfoHelper.setLineNo(tempInfo.getLineNo());
                        processInfoHelper.setMergeNo(tempInfo.getMergeNo());
                        processInfoHelper.setWhNo(tempInfo.getWhNo());
                        processInfoHelper.setWoNo(tempInfo.getWoNo());
                        processInfoHelper.setPartNo(selectedItemInfoHelper.getPartNo());
                        intent.putExtra("PROCESS_INFO", new Gson().toJson(processInfoHelper));
                        intent.putExtra("ON_HAND_QTY_INFO", new Gson().toJson(selectedItemInfoHelper));
                        intent.putExtra("CONDITION_INFO", new Gson().toJson(tempInfo));
                        startActivityForResult(intent, 0);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.Insufficient_stock_Qty), Toast.LENGTH_LONG).show();
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            // Yes button clicked
                            doClearSendInfo();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            // No button clicked
                            break;
                    }
                }
            };

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItemInfoHelper = tempInfo.getOnHandQtyList()[position];
                AppController.debug("onLongClick item " + position + " PartNo="
                        + selectedItemInfoHelper.getPartNo());

                if (selectedItemInfoHelper.getControl().equals("SN") && selectedItemInfoHelper.isCanDebit()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MaterialSendingListActivity.this);
                    String msg = null;

                    if (sendType == MaterialSendingActivity.QUERY_TYPE_SEND)
                        msg = getString(R.string.r_u_sure_to_del_data);
                    else
                        msg = getString(R.string.r_u_sure_to_del_transfer_sub_data);

                    builder.setTitle( getString(R.string.btn_ok)).setMessage(msg)
                            .setPositiveButton(getString(R.string.yes), dialogClickListener)
                            .setNegativeButton(getString(R.string.no), dialogClickListener).show();
                }

                return true;
            }
        });

        if (data.size() == 0)
            Toast.makeText(getApplicationContext(), getString(R.string.no_data), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        Log.d("onClick Return", String.valueOf(v.getId()));

        if (v.getId() == R.id.button_return) {
            returnPage();
        }
    }

    private void doDebit(SendingInfoHelper sendingInfo) {
        if (sendingInfo != null) {
            for (SendOnHandQtyInfo item : sendingInfo.getOnHandQtyList()) {
                if (item.getControl().equals("SN") && item.isCanDebit() && item.getUnsentQty().doubleValue() >= 0) {
                    dialog = ProgressDialog.show(this, getString(R.string.holdon),
                            getString(R.string.Data_debiting_processing), true);
                    selectedItemInfoHelper = item;

                    if (isInSearchMode) {
                        tempSendInfo.setItemNo(item.getPartNo());

                        if (sendType == MaterialSendingActivity.QUERY_TYPE_SEND)
                            tempSendInfo.setTrxType("I");
                        else
                            tempSendInfo.setTrxType("T");
                    } else {
                        sendInfo.setItemNo(item.getPartNo());

                        if (sendType == MaterialSendingActivity.QUERY_TYPE_SEND)
                            sendInfo.setTrxType("I");
                        else
                            sendInfo.setTrxType("T");
                    }

                    new DoDebit().execute(0);
                    return;
                }
            }

            if (needRefresh)
                queryData();
        }
    }

    private void doNextDebit() {
        if (selectedItemInfoHelper != null) {
            selectedItemInfoHelper.setCanDebit(false);

            if (isInSearchMode)
                doDebit(tempSendInfo);
            else
                doDebit(sendInfo);
        }
    }

    public void onBackPressed() {
        // do something here and don't write super.onBackPressed()
        Log.d(TAG, "onClick Hardware Back Button");
        returnPage();
    }

    private void returnPage() {
        finish();
    }

    private void doQuerySendInfo(String searchText) {
        if (!TextUtils.isEmpty(searchText)) {
            tempSendInfo = new SendingInfoHelper();
            tempSendInfo.setLineNo(sendInfo.getLineNo());
            tempSendInfo.setMergeNo(sendInfo.getMergeNo());
            tempSendInfo.setWhNo(sendInfo.getWhNo());
            tempSendInfo.setWoNo(sendInfo.getWoNo());
            tempSendInfo.setTrxType(sendInfo.getTrxType());
            tempSendInfo.setSearchText(searchText);
        }

        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetSendInfo().execute(0);
    }

    private void queryData() {
        if (isInSearchMode) {
            doQuerySendInfo(txtInputSearchText.getText().toString().trim());
        } else {
            doQuerySendInfo(null);
        }
    }

    private void doClearSendInfo() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.del_data), true);

        if (isInSearchMode) {
            tempSendInfo.setItemNo(selectedItemInfoHelper.getPartNo());

            if (sendType == MaterialSendingActivity.QUERY_TYPE_SEND)
                tempSendInfo.setTrxType("I");
            else
                tempSendInfo.setTrxType("T");
        } else {
            sendInfo.setItemNo(selectedItemInfoHelper.getPartNo());

            if (sendType == MaterialSendingActivity.QUERY_TYPE_SEND)
                sendInfo.setTrxType("I");
            else
                sendInfo.setTrxType("T");
        }

        new ClearSendInfo().execute(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (selectedItemInfoHelper != null) {
                selectedItemInfoHelper = new Gson().fromJson(data.getStringExtra("ON_HAND_QTY_INFO"), SendOnHandQtyInfo.class);

                if (isInSearchMode) {
                    tempSendInfo.getOnHandQtyList()[dataPosition] = selectedItemInfoHelper;
                    boolean found = false;
                    int index = 0;

                    for (int i = 0; i < sendInfo.getOnHandQtyList().length; i++) {
                        if (sendInfo.getOnHandQtyList()[i].getPartNo().equals(selectedItemInfoHelper.getPartNo())) {
                            found = true;
                            index = i;
                            break;
                        }
                    }

                    if (found)
                        sendInfo.getOnHandQtyList()[index] = selectedItemInfoHelper;

                    setListData(tempSendInfo);
                } else {
                    sendInfo.getOnHandQtyList()[dataPosition] = selectedItemInfoHelper;
                    setListData(sendInfo);
                }

                listView.smoothScrollToPosition(dataPosition);
            }
        }
    }

    static class ViewHolder {
        public TextView txtPartNo;
        public TextView txtTotalSendQty;
        public TextView txtSentQty;
        public TextView txtUnSentQty;
        public TextView txtUncheckedQty;
        public TextView txtTotalInventory;
        public LinearLayout llStockList;
    }

    private class DoDebit extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Do Debit from " + AppController.getServerInfo()
                    + AppController.getProperties("DipWipSn"));
            publishProgress(getString(R.string.Debit_processing));//"扣帳處理中..."

            if (isInSearchMode)
                return sendingHandler.doDisWipSn(tempSendInfo);
            else
                return sendingHandler.doDisWipSn(sendInfo);
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
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    needRefresh = true;
                    clearStatus();
                    doNextDebit();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));

                    if (needRefresh)
                        queryData();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";

                if (needRefresh)
                    queryData();
            }
        }
    }

    private class GetSendInfo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            publishProgress(getString(R.string.downloading_data));

            if (isInSearchMode) {
                AppController.debug("Get Item On Hand Qty from " + AppController.getServerInfo()
                        + AppController.getProperties("ItemOnHandQty"));
                return sendingHandler.getItemOnHandQty(tempSendInfo);
            } else {
                AppController.debug("Get Inv On Hand Qty from " + AppController.getServerInfo()
                        + AppController.getProperties("InvOnHandQty"));
                return sendingHandler.getInvOnHandQty(sendInfo);
            }
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
                if (isInSearchMode)
                    tempSendInfo = (SendingInfoHelper) result;
                else
                    sendInfo = (SendingInfoHelper) result;

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

                if (isInSearchMode)
                    setListData(tempSendInfo);
                else
                    setListData(sendInfo);
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                setListData(null);
            }
        }
    }

    private class ClearSendInfo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Delete SendInfo to " + AppController.getServerInfo()
                    + AppController.getProperties("DeleteTempSn"));
            publishProgress(getString(R.string.del_data));

            if (isInSearchMode)
                return sendingHandler.doDeleteTempSn(tempSendInfo);
            else
                return sendingHandler.doDeleteTempSn(sendInfo);
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
                    queryData();
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

    public class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater = null;

        private MyAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.sending_summary, null);
                holder.txtPartNo = convertView.findViewById(R.id.txt_part_no);
                holder.txtTotalSendQty = convertView.findViewById(R.id.txt_total_sending_qty);
                holder.txtSentQty = convertView.findViewById(R.id.txt_send_qty);
                holder.txtUnSentQty = convertView.findViewById(R.id.txt_unsend_qty);
                holder.txtUncheckedQty = convertView.findViewById(R.id.txt_uncheck_qty);
                holder.txtTotalInventory = convertView.findViewById(R.id.txt_total_inventory);
                holder.llStockList = convertView.findViewById(R.id.ll_stock_list);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            convertView.setVisibility(View.VISIBLE);

            if ((double) data.get(position).get("unsent_qty") == 0) {
                convertView.setBackgroundColor(Color.GRAY);
            } else if ((boolean) (data.get(position).get("can_debit"))) {
                convertView.setBackgroundColor(Color.LTGRAY);
            } else {
                convertView.setBackgroundDrawable(getResources().getDrawable(R.drawable.custom_item));
            }

            if ((double) data.get(position).get("unchecked_qty") == 0 && (double) data.get(position).get("total_inventory") == 0) {
                convertView.setBackgroundColor(Color.RED);
            }

            holder.txtPartNo.setText((String) data.get(position).get("part_no"));
            holder.txtTotalSendQty.setText(Util.fmt((double) data.get(position).get("total_sending_qty")));
            holder.txtSentQty.setText(Util.fmt((double) data.get(position).get("sent_qty")));
            holder.txtUnSentQty.setText(Util.fmt((double) data.get(position).get("unsent_qty")));
            holder.txtUncheckedQty.setText(Util.fmt((double) data.get(position).get("unchecked_qty")));
            holder.txtTotalInventory.setText(Util.fmt((double) data.get(position).get("total_inventory")));
            addView(holder.llStockList, data.get(position).get("subinventory_list"));
            return convertView;
        }

        private void addView(LinearLayout v, Object o) {
            v.removeAllViews();
            SendSubinventoryInfo[] list = (SendSubinventoryInfo[]) o;

            for (SendSubinventoryInfo item : list) {
                View child = mInflater.inflate(R.layout.sending_item, null);
                TextView txtSubinventory = child.findViewById(R.id.txt_subinventory);
                TextView txtLocator = child.findViewById(R.id.txt_locator);
                TextView txtInventory = child.findViewById(R.id.txt_inventory);
                TextView labelDateCode = child.findViewById(R.id.label_datecode);
                TextView txtDateCode = child.findViewById(R.id.txt_datecode);
                LinearLayout llDc = child.findViewById(R.id.ll_dc);
                txtSubinventory.setText(item.getSubinventory());
                txtLocator.setText(item.getLocator());
                txtInventory.setText(Util.fmt(item.getInventory().doubleValue()));

                if (TextUtils.isEmpty(item.getDatecode()))
                    llDc.setVisibility(View.GONE);
                else
                    txtDateCode.setText(item.getDatecode());

                v.addView(child);
            }
        }
    }
}
