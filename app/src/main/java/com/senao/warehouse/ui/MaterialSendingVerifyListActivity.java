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
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.senao.warehouse.handler.SendingVerifyHandler;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MaterialSendingVerifyListActivity extends Activity implements OnClickListener {

    private static final String TAG = MaterialSendingVerifyListActivity.class.getSimpleName();
    List<HashMap<String, Object>> data = new ArrayList<>();
    private MyAdapter adapter;
    private TextView mConnection;
    private EditText txtInputSearchText;
    private Button btnReturn, btnDebit;
    private ListView listView;
    private SendingVerifyHandler sendingVerifyHandler;
    private SendingInfoHelper sendInfo;
    private SendingInfoHelper tempSendInfo;
    private ProgressDialog dialog;
    private String errorInfo = "";
    private boolean isInSearchMode;
    private SendOnHandQtyInfo selectedItemInfoHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            sendInfo = new Gson().fromJson(extras.getString("CONDITION_INFO"), SendingInfoHelper.class);

            if (sendInfo == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_condictions), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            sendingVerifyHandler = new SendingVerifyHandler();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_condictions), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_material_sending_check_list);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(this);

        btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        listView = findViewById(R.id.list_item);
        listView.setVisibility(View.GONE);

        btnDebit = findViewById(R.id.button_debit);
        btnDebit.setVisibility(View.INVISIBLE);
        btnDebit.setOnClickListener(new OnClickListener() {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            // Yes button clicked
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
                AlertDialog.Builder builder = new AlertDialog.Builder(MaterialSendingVerifyListActivity.this);
                builder.setTitle( getString(R.string.btn_ok)).setMessage(R.string.confirm_exec_debit)
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
                        Toast.makeText(MaterialSendingVerifyListActivity.this, getString(R.string.query_cons_cant_be_null), Toast.LENGTH_SHORT).show();
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

        sendingVerifyHandler = new SendingVerifyHandler();
        doQuerySendInfo(null);
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
            List<SendOnHandQtyInfo> itemInfoList = new ArrayList<>();

            for (int i = 0; i < sendInfo.getOnHandQtyList().length; i++) {
                if (sendInfo.getOnHandQtyList()[i].getPartNo().contains(text)) {
                    itemInfoList.add(sendInfo.getOnHandQtyList()[i]);
                }
            }

            if (itemInfoList.size() > 0) {
                tempSendInfo = new SendingInfoHelper();
                tempSendInfo.setLineNo(sendInfo.getLineNo());
                tempSendInfo.setSubinventory(sendInfo.getSubinventory());
                tempSendInfo.setLocator(sendInfo.getLocator());
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

            for (SendOnHandQtyInfo info : tempInfo.getOnHandQtyList()) {
                if (info.getTempQty() != null && info.getTempQty().doubleValue() > 0) {
                    btnDebit.setVisibility(View.VISIBLE);
                }

                tempList.add(info);
                HashMap<String, Object> item = new HashMap<String, Object>();
                item.put("part_no", info.getPartNo());
                item.put("item_description", info.getItemDescription());
                item.put("control", info.getControl());
                item.put("total_sending_qty",
                        info.getTotalSendingQty().doubleValue());
                item.put("sent_qty", info.getSentQty().doubleValue());
                item.put("unsent_qty", info.getUnsentQty().doubleValue());
                item.put("unchecked_qty", info.getUncheckedQty().doubleValue());
                item.put("onhand_qty", info.getOnHandQty().doubleValue());
                item.put("temp_qty", info.getTempQty().doubleValue());

                if (info.getControl().equals("DC")) {
                    item.put("dc_list", info.getSubinventoryInfoList());
                }

                data.add(item);
            }

            tempInfo.setOnHandQtyList(tempList);
        }

        adapter = new MyAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                selectedItemInfoHelper = tempInfo.getOnHandQtyList()[position];
                AppController.debug("onClick item " + position + " PartNo="
                        + selectedItemInfoHelper.getPartNo());

                if (selectedItemInfoHelper.getOnHandQty().subtract(selectedItemInfoHelper.getTempQty()).doubleValue() > 0) {
                    if (selectedItemInfoHelper.getUnsentQty().subtract(selectedItemInfoHelper.getTempQty()).doubleValue() > 0) {
                        Intent intent = new Intent(getBaseContext(), MaterialSendingVerifyProcessActivity.class);
                        SendProcessInfoHelper processInfoHelper = new SendProcessInfoHelper();
                        processInfoHelper.setLineNo(tempInfo.getLineNo());
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

                if (selectedItemInfoHelper.getTempQty() != null && selectedItemInfoHelper.getTempQty().doubleValue() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            MaterialSendingVerifyListActivity.this);
                    builder.setTitle( getString(R.string.btn_ok)).setMessage(R.string.Delete_chking_data)
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
        }
    }

    private void doDebit(SendingInfoHelper sendingInfo) {
        if (sendingInfo != null) {
            dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.Data_debiting_processing), true);
            new DoDebit().execute(0);
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
            tempSendInfo.setWoNo(sendInfo.getWoNo());
            tempSendInfo.setSubinventory(sendInfo.getSubinventory());
            tempSendInfo.setLocator(sendInfo.getLocator());
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
        } else {
            sendInfo.setItemNo(selectedItemInfoHelper.getPartNo());
        }

        new ClearSendInfo().execute(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            queryData();
        }
    }

    static class ViewHolder {
        public TextView txtPartNo;
        public TextView txtItemDescritpion;
        public TextView txtTotalSendQty;
        public TextView txtSentQty;
        public TextView txtUnSentQty;
        public TextView txtTempQty;
        public TextView txtUncheckedQty;
        public TextView txtOnHandQty;
        public LinearLayout llDcList;
    }

    private class DoDebit extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Do Sending Verify Debit from " + AppController.getServerInfo()
                    + AppController.getProperties("SendingVerifyDebit"));
            publishProgress(getString(R.string.Debit_processing));

            if (isInSearchMode)
                return sendingVerifyHandler.doSendingVerifyDebit(tempSendInfo);
            else
                return sendingVerifyHandler.doSendingVerifyDebit(sendInfo);
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
                    clearStatus();
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

    private class GetSendInfo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            publishProgress(getString(R.string.downloading_data));
            AppController.debug("Get Item On Hand Check Data from " + AppController.getServerInfo()
                    + AppController.getProperties("ItemOnHandVerify"));

            if (isInSearchMode) {
                return sendingVerifyHandler.getItemOnHandCheck(tempSendInfo);
            } else {
                return sendingVerifyHandler.getItemOnHandCheck(sendInfo);
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
            AppController.debug("Delete Temp Verify to " + AppController.getServerInfo()
                    + AppController.getProperties("DeleteTempVerify"));
            publishProgress(getString(R.string.del_data));

            if (isInSearchMode)
                return sendingVerifyHandler.doDeleteTempVerify(tempSendInfo);
            else
                return sendingVerifyHandler.doDeleteTempVerify(sendInfo);
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
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.check_item, null);
                holder.txtPartNo =  convertView.findViewById(R.id.txt_part_number);
                holder.txtItemDescritpion =  convertView.findViewById(R.id.txt_item_description);
                holder.txtTotalSendQty =  convertView.findViewById(R.id.txt_total_sending_qty);
                holder.txtSentQty =  convertView.findViewById(R.id.txt_send_qty);
                holder.txtUnSentQty =  convertView.findViewById(R.id.txt_unsend_qty);
                holder.txtUncheckedQty =  convertView.findViewById(R.id.txt_uncheck_qty);
                holder.txtTempQty =  convertView.findViewById(R.id.txt_temp_qty);
                holder.txtOnHandQty =  convertView.findViewById(R.id.txt_inventory);
                holder.llDcList =  convertView.findViewById(R.id.ll_dc);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            convertView.setVisibility(View.VISIBLE);

            if ((double) data.get(position).get("unsent_qty") <= 0) {
                convertView.setBackgroundColor(Color.DKGRAY);
                convertView.setClickable(false);
            } else if ((double) data.get(position).get("unsent_qty") - (double) data.get(position).get("temp_qty") <= 0) {
                convertView.setBackgroundColor(Color.GRAY);
                convertView.setClickable(false);
            } else {
                convertView.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.custom_item));
            }

            holder.txtPartNo.setText((String) data.get(position).get("part_no"));
            holder.txtItemDescritpion.setText((String) data.get(position).get("item_description"));
            holder.txtTotalSendQty.setText(Util.fmt((double) data.get(position).get("total_sending_qty")));
            holder.txtSentQty.setText(Util.fmt((double) data.get(position).get("sent_qty")));
            holder.txtUnSentQty.setText(Util.fmt((double) data.get(position).get("unsent_qty")));
            holder.txtUncheckedQty.setText(Util.fmt((double) data.get(position).get("unchecked_qty")));
            holder.txtTempQty.setText(Util.fmt((double) data.get(position).get("temp_qty")));
            holder.txtOnHandQty.setText(Util.fmt((double) data.get(position).get("onhand_qty")));
            addView(holder.llDcList, data.get(position).get("dc_list"));
            return convertView;
        }

        private void addView(LinearLayout v, Object o) {
            v.removeAllViews();
            SendSubinventoryInfo[] list = (SendSubinventoryInfo[]) o;

            if (list != null) {
                for (SendSubinventoryInfo item : list) {
                    View child = mInflater.inflate(R.layout.check_dc_item, null);
                    TextView txtDateCode =  child.findViewById(R.id.txt_datecode);
                    txtDateCode.setText(item.getDatecode() + "  x  " + Util.fmt(item.getInventory().doubleValue()));
                    v.addView(child);
                }
            }
        }
    }
}
