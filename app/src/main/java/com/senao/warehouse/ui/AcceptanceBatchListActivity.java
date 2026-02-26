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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.AcceptanceConditionHelper;
import com.senao.warehouse.database.AcceptanceInfoHelper;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.UserInfoHelper;
import com.senao.warehouse.handler.AcceptanceHandler;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AcceptanceBatchListActivity extends Activity implements View.OnClickListener {
    private static final String TAG = AcceptanceBatchListActivity.class.getSimpleName();
    private List<HashMap<String, Object>> data = new ArrayList<>();
    private EditText txtSearchText;
    private TextView mConnection, txtTitle, lblTotalQty, lblSelectedQty;
    private String errorInfo = "";
    private Button btnReturn, btnStockIn, btnAllSel, btnUnSel;
    private boolean isInSearchMode;
    private AcceptanceConditionHelper conditionHelper;
    private UserInfoHelper user = AppController.getUser();
    private AcceptanceHandler acceptanceHandler;
    private AcceptanceConditionHelper tempConditionHelper;
    private AcceptanceConditionHelper processConditionHelper;
    private ProgressDialog dialog;
    private ListView listView;
    private MyAdapter adapter;
    private Listener callback;
    private int dataPosition;
    private boolean isRefreshForStockIn;
    private int accType;
    private int selectedCNT = 0;

    private DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Yes button clicked
                    doStockIn();

                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    break;
            }
        }
    };

    private void clearStatus() {
        mConnection.setText("");
        mConnection.setTextColor(Color.WHITE);
        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        errorInfo = "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            conditionHelper = new Gson().fromJson(extras.getString("CONDITION_INFO"), AcceptanceConditionHelper.class);

            if (conditionHelper == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_acceptance_stock_data), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_acceptance_stock_data), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_acceptance_batch_list);

        txtTitle = findViewById(R.id.ap_title);

        accType = getIntent().getIntExtra("TYPE", 0);
        if (accType == AcceptanceActivity.OUTSOURCING) {
            txtTitle.setText(R.string.label_acceptance_batch_outsourcing);
        }

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(this);

        btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        btnStockIn = findViewById(R.id.button_stock_in);
        btnStockIn.setOnClickListener(this);

        btnAllSel = findViewById(R.id.btn_select_all);
        btnAllSel.setOnClickListener(this);

        btnUnSel = findViewById(R.id.btn_select_invert);
        btnUnSel.setOnClickListener(this);

        lblTotalQty = findViewById(R.id.label_total_qty);
        lblSelectedQty = findViewById(R.id.label_select_qty);

        listView = findViewById(R.id.list_item);

        txtSearchText = findViewById(R.id.input_search_text);
        txtSearchText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    //Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down" + String.valueOf(v.getId()));

                    if (txtSearchText.getText().toString().trim().equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.query_cons_cant_be_null), Toast.LENGTH_SHORT).show();
                        txtSearchText.requestFocus();
                    } else {
                        isInSearchMode = true;
                        hideKeyboard();
                        getSearchData(txtSearchText.getText().toString().trim());
                    }

                    return true;
                }

                return false;
            }
        });
        txtSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (isInSearchMode && TextUtils.isEmpty(s)) {
                    isInSearchMode = false;
                    callback = null;
                    hideKeyboard();
                    clearStatus();
                    setListData(conditionHelper);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        acceptanceHandler = new AcceptanceHandler();
        doQueryAcceptanceInfo();
    }

    private void doQueryAcceptanceInfo() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetAcceptanceInfo().execute(0);
    }

    private void setListData(final AcceptanceConditionHelper tempInfo) {
        listView.setVisibility(View.VISIBLE);
        data.clear();

        try {
            if (tempInfo != null && tempInfo.getInfoList() != null) {
                List<AcceptanceInfoHelper> tempList = new ArrayList<>();

                for (AcceptanceInfoHelper info : tempInfo.getInfoList()) {
                    if (isRefreshForStockIn && Util.getDoubleValue(info.getNotInQty()) == 0) {
                        continue;
                    }

                    info.setWareHouseNo(tempInfo.getWareHouseNo());
                    tempList.add(info);
                    HashMap<String, Object> item = new HashMap<>();
                    item.put("not_in_day", info.getNotInDay());
                    item.put("control", info.getControl());
                    item.put("part_no", info.getPartNo());
                    item.put("uncheck_qty", Util.getDoubleValue(info.getWaitCheckQty()));
                    item.put("check_not_in_qty", Util.getDoubleValue(info.getCheckedNotInQty()));
                    item.put("in_qty", Util.getDoubleValue(info.getInQty()));
                    item.put("not_in_qty", Util.getDoubleValue(info.getNotInQty()));
                    item.put("receipt_no", info.getReceiptNo());
                    data.add(item);
                }

                tempInfo.setInfoList(tempList);

                if (isRefreshForStockIn) {
                    isRefreshForStockIn = false;
                }
            }
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }

        adapter = new MyAdapter(this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                setCheckedQtyTitle();
            }
        });

        lblTotalQty.setText(getString(R.string.label_total_qty, data.size()));
        setCheckedQtyTitle();

        if (data.size() == 0) {
            dataPosition = 0;
            Toast.makeText(getApplicationContext(), getString(R.string.no_data), Toast.LENGTH_LONG).show();
        }
    }

    private void queryData() {
        if (isInSearchMode) {
            callback = new Listener() {
                @Override
                public void onEvent() {
                    getSearchData(txtSearchText.getText().toString().trim());
                }
            };
        } else {
            callback = null;
            isInSearchMode = false;
        }

        doQueryAcceptanceInfo();
    }

    private void getSearchData(String text) {
        if (conditionHelper != null && conditionHelper.getInfoList() != null && conditionHelper.getInfoList().length > 0) {
            List<AcceptanceInfoHelper> itemInfoList = new ArrayList<>();

            for (int i = 0; i < conditionHelper.getInfoList().length; i++) {
                if (conditionHelper.getInfoList()[i].getPartNo().indexOf(text) >= 0) {
                    itemInfoList.add(conditionHelper.getInfoList()[i]);
                }
            }

            if (itemInfoList.size() > 0) {
                tempConditionHelper = new AcceptanceConditionHelper();
                tempConditionHelper.setWareHouseNo(conditionHelper.getWareHouseNo());
                tempConditionHelper.setPartNo(conditionHelper.getPartNo());
                tempConditionHelper.setReceiptsNo(conditionHelper.getReceiptsNo());
                tempConditionHelper.setTrxIdNo(conditionHelper.getTrxIdNo());
                tempConditionHelper.setInfoList(itemInfoList);
                setListData(tempConditionHelper);
                return;
            }
        }

        setListData(null);
    }

    private void doStockIn() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), "執行入庫中...", true);

        List<AcceptanceInfoHelper> list = new ArrayList<>();
        processConditionHelper = new AcceptanceConditionHelper();
        processConditionHelper.setWareHouseNo(conditionHelper.getWareHouseNo());
        processConditionHelper.setSubinventory(conditionHelper.getSubinventory());
        processConditionHelper.setLocator(conditionHelper.getLocator());

        AcceptanceInfoHelper item;
        for (int i = 0; i < data.size(); i++) {
            if (listView.isItemChecked(i)) {
                item =new AcceptanceInfoHelper();

                if (isInSearchMode) {
                    item.setPartNo(tempConditionHelper.getInfoList()[i].getPartNo());
                    item.setReceiptNo(tempConditionHelper.getInfoList()[i].getPartNo());
                } else {
                    item.setPartNo(conditionHelper.getInfoList()[i].getPartNo());
                    item.setReceiptNo(conditionHelper.getInfoList()[i].getPartNo());
                }

                list.add(item);
            }
        }

        processConditionHelper.setInfoList(list);
        new DoStockIn().execute(0);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtSearchText.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_select_all) {
            for (int i = 0; i < data.size(); i++) {
                listView.setItemChecked(i, true);
            }

            setCheckedQtyTitle();
        } else if (id == R.id.btn_select_invert) {
            for (int i = 0; i < data.size(); i++) {
                listView.setItemChecked(i, false);
            }

            setCheckedQtyTitle();
        } else if (id == R.id.button_return) {
            finish();
        } else if (id == R.id.button_stock_in) {
            if (selectedCNT > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle( getString(R.string.btn_ok)).setMessage(getString(R.string.ru_sure_in_stock))
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.chse_warhousing_item), Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.label_status) {
            if (!TextUtils.isEmpty(errorInfo)) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(AcceptanceBatchListActivity.this);
                dialog.setTitle("Error Msg");
                dialog.setMessage(errorInfo);
                dialog.setIcon(android.R.drawable.ic_dialog_alert);
                dialog.setCancelable(false);
                dialog.setNegativeButton( getString(R.string.btn_ok), new DialogInterface.OnClickListener() {

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

    private void setCheckedQtyTitle() {
        int count = 0;

        for (int i = 0; i < data.size(); i++) {
            if (listView.isItemChecked(i)) {
                count++;
            }
        }

        selectedCNT = count;
        lblSelectedQty.setText(getString(R.string.label_select_qty, count));
    }

    interface Listener {
        void onEvent();
    }

    static class ViewHolder {
        public TextView txtNotInDay;
        public TextView txtControl;
        public TextView txtPartNo;
        public TextView txtUncheckQty;
        public TextView txtCheckNotInQty;
        public TextView txtInQty;
        public TextView txtNotInQty;
        public TextView txtReceiptNo;
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
                convertView = mInflater.inflate(R.layout.acceptance_batch_list_item, null);
                holder.txtNotInDay =  convertView.findViewById(R.id.txt_not_in_day);
                holder.txtControl =  convertView.findViewById(R.id.txt_control);
                holder.txtPartNo =  convertView.findViewById(R.id.txt_part_number);
                holder.txtUncheckQty =  convertView.findViewById(R.id.txt_uncheck_qty);
                holder.txtCheckNotInQty =  convertView.findViewById(R.id.txt_check_not_in_qty);
                holder.txtInQty =  convertView.findViewById(R.id.txt_in_qty);
                holder.txtNotInQty =  convertView.findViewById(R.id.txt_not_in_qty);
                holder.txtReceiptNo =  convertView.findViewById(R.id.txt_receipt_no);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            convertView.setVisibility(View.VISIBLE);

            if ((double) data.get(position).get("not_in_qty") == 0) {
                convertView.setBackgroundColor(Color.GRAY);
                convertView.setClickable(false);
            } else {
                convertView.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.custom_item));
            }

            holder.txtNotInDay.setText(AcceptanceBatchListActivity.this.getString(R.string.not_in_day, (int) data.get(position).get("not_in_day")));
            holder.txtControl.setText((String) data.get(position).get("control"));
            holder.txtPartNo.setText((String) data.get(position).get("part_no"));
            holder.txtUncheckQty.setText(Util.fmt((double) data.get(position).get("uncheck_qty")));
            holder.txtCheckNotInQty.setText(Util.fmt((double) data.get(position).get("check_not_in_qty")));
            holder.txtInQty.setText(Util.fmt((double) data.get(position).get("in_qty")));
            holder.txtNotInQty.setText(Util.fmt((double) data.get(position).get("not_in_qty")));
            holder.txtReceiptNo.setText((String) data.get(position).get("receipt_no"));

            return convertView;
        }
    }

    private class GetAcceptanceInfo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            publishProgress(getString(R.string.downloading_data));

            if (accType == AcceptanceActivity.OUTSOURCING) {
                AppController.debug("Get Acceptance Info Osp from " + AppController.getServerInfo() + AppController.getProperties("GetAcceptanceInfoOsp"));
                return acceptanceHandler.getAcceptanceInfoOsp(conditionHelper);
            } else {
                AppController.debug("Get Acceptance Info from " + AppController.getServerInfo() + AppController.getProperties("GetAcceptanceInfo"));

                /*if (isInSearchMode) {
                    return acceptanceHandler.getAcceptanceInfo(tempConditionHelper);
                } else {
                    return acceptanceHandler.getAcceptanceInfo(conditionHelper);
                }*/

                return acceptanceHandler.getAcceptanceInfo(conditionHelper);
            }
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            //Things to be done while execution of long running operation is in
            //progress. For example updating ProgessDialog
        }

        @Override
        protected void onPostExecute(BasicHelper result) {
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                conditionHelper = (AcceptanceConditionHelper) result;

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

                setListData(conditionHelper);

                if (isInSearchMode && callback != null) {
                    callback.onEvent();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                setListData(null);
            }
        }
    }

    private class DoStockIn extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Do Acceptance StockIn Batch from " + AppController.getServerInfo() + AppController.getProperties("AcceptanceDoStockInBatch"));
            publishProgress("執行入庫中...");
            return acceptanceHandler.doStockInBatch(processConditionHelper);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            //Things to be done while execution of long running operation is in
            //progress. For example updating ProgessDialog
        }

        @Override
        protected void onPostExecute(BasicHelper result) {
            //execution of result of Long time consuming operation //
            //finalResult.setText(result);
            //txtResponseArea.setText(result);
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    isRefreshForStockIn = true;
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
}
