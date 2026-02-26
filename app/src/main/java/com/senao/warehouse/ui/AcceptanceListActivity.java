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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
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
import android.widget.LinearLayout;
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

public class AcceptanceListActivity extends Activity implements View.OnClickListener {
    private static final String TAG = AcceptanceListActivity.class.getSimpleName();
    private List<HashMap<String, Object>> data = new ArrayList<>();
    private EditText txtSearchText;
    private TextView mConnection, lblAccount, lblEmpID, txtTitle;
    private String errorInfo = "";
    private Button btnReturn, btnStockIn;
    private boolean isInSearchMode;
    private AcceptanceConditionHelper conditionHelper;
    private UserInfoHelper user = AppController.getUser();
    private AcceptanceHandler acceptanceHandler;
    private AcceptanceConditionHelper tempConditionHelper;
    private ProgressDialog dialog;
    private AcceptanceInfoHelper selectedItemInfoHelper;
    private ListView listView;
    private MyAdapter adapter;
    private Listener callback;
    private int dataPosition;
    private boolean isRefreshForStockIn;
    private int accType;

    private DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    doStockIn();
                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
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

        setContentView(R.layout.activity_acceptance_list);

        txtTitle = findViewById(R.id.ap_title);

        accType = getIntent().getIntExtra("TYPE", 0);
        final SpannableString text;
        if (accType == AcceptanceActivity.OUTSOURCING) {
            text = new SpannableString(getString(R.string.label_acceptance_outsourcing1, AppController.getOrgName()));
            text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            txtTitle.setText(text);
        }else {
            text = new SpannableString(getString(R.string.label_acceptance1, AppController.getOrgName()));
            text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            txtTitle.setText(text);
        }

        /*if (accType == AcceptanceActivity.OUTSOURCING) {
            txtTitle.setText(R.string.label_acceptance_outsourcing);
        }*/

        lblAccount = findViewById(R.id.textview_login_account);
        lblAccount.setText(user.getUserName());

        lblEmpID = findViewById(R.id.textview_employee_id);
        lblEmpID.setText(String.valueOf(user.getPassword()));

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(this);

        btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        btnStockIn = findViewById(R.id.button_stock_in);
        btnStockIn.setOnClickListener(this);
        btnStockIn.setEnabled(false);

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
                        txtSearchText.setText(parsePartNo(txtSearchText.getText().toString().trim()));
                        txtSearchText.setSelection(txtSearchText.getText().length());
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

    private String parsePartNo(String trim) {
        if (trim.length() > 12) {
            return trim.substring(0, 12);
        } else {
            return trim;
        }
    }

    private void doQueryAcceptanceInfo() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetAcceptanceInfo().execute(0);
    }

    private void setListData(final AcceptanceConditionHelper tempInfo) {
        listView.setVisibility(View.VISIBLE);
        btnStockIn.setEnabled(false);
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
                    item.put("trx_id", info.getTrxId());
                    item.put("xb_in_qty", Util.getDoubleValue(info.getInXbQty()));
                    item.put("xb_not_in_qty", Util.getDoubleValue(info.getNotInXbQty()));
                    item.put("item_loc", info.getItemLoc());
                    item.put("item_loc_desc", info.getItemLocDesc());

                    if (!btnStockIn.isEnabled() && Util.getDoubleValue(info.getNotInQty()) == 0) {
                        btnStockIn.setEnabled(true);
                    }

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
                dataPosition = position;
                selectedItemInfoHelper = tempInfo.getInfoList()[position];
                AppController.debug("onClick item " + position + " PartNo=" + selectedItemInfoHelper.getPartNo());

                if (Util.getDoubleValue(selectedItemInfoHelper.getNotInQty()) > 0) {
                    goToProcess();
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            doClearAcceptanceInfo();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItemInfoHelper = tempInfo.getInfoList()[position];
                AppController.debug("onLongClick item " + position + " PartNo=" + selectedItemInfoHelper.getPartNo());
                AlertDialog.Builder builder = new AlertDialog.Builder(AcceptanceListActivity.this);
                String msg = getString(R.string.Make_sure_delete_inbound_data);
                builder.setTitle(getString(R.string.btn_ok)).setMessage(msg)
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show();
                return true;
            }
        });

        listView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                listView.smoothScrollToPosition(dataPosition);
            }
        });

        if (data.size() == 0) {
            dataPosition = 0;
            Toast.makeText(getApplicationContext(), getString(R.string.no_data), Toast.LENGTH_LONG).show();
        }
    }

    private void doClearAcceptanceInfo() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.del_data), true);
        new ClearAcceptanceInfo().execute(0);
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

    private void goToProcess() {
        Intent intent = new Intent(this, AcceptanceProcessActivity.class);
        intent.putExtra("CONDITION_INFO", new Gson().toJson(conditionHelper));
        intent.putExtra("ITEM_INFO", new Gson().toJson(selectedItemInfoHelper));
        intent.putExtra("TYPE", accType);
        startActivityForResult(intent, 0);
    }

    private void doStockIn() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.stock_proccessing), true);
        new DoStockIn().execute(0);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtSearchText.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button_return) {
            finish();
        } else if (id == R.id.button_stock_in) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.btn_ok)).setMessage(getString(R.string.ru_sure_in_stock))
                    .setPositiveButton(getString(R.string.yes), dialogClickListener)
                    .setNegativeButton(getString(R.string.no), dialogClickListener).show();
        } else if (id == R.id.label_status) {
            if (!TextUtils.isEmpty(errorInfo)) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(AcceptanceListActivity.this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            queryData();
        }
    }

    interface Listener {
        void onEvent();
    }

    static class ViewHolder {
        public TextView txtNotInDay;
        public TextView lblItemLoc;
        public TextView txtItemLoc;
        public TextView lblItemLocDesc;
        public TextView txtItemLocDesc;
        public TextView txtControl;
        public TextView txtPartNo;
        public TextView txtUncheckQty;
        public TextView txtCheckNotInQty;
        public TextView txtInQty;
        public TextView txtNotInQty;
        public TextView txtReceiptNo;
        public TextView txtTrxId;
        public LinearLayout llXb;
        public TextView txtInXbQty;
        public TextView txtNotInXbQty;
    }

    public class MyAdapter extends BaseAdapter {
        private final String PREFIX_ITEM_NO_PCB = "7016";

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
                convertView = mInflater.inflate(R.layout.acceptance_list_item, null);
                holder.txtNotInDay = convertView.findViewById(R.id.txt_not_in_day);
                holder.lblItemLoc = convertView.findViewById(R.id.label_item_loc);
                holder.txtItemLoc = convertView.findViewById(R.id.txt_item_loc);
                holder.lblItemLocDesc = convertView.findViewById(R.id.label_item_loc_desc);
                holder.txtItemLocDesc = convertView.findViewById(R.id.txt_item_loc_desc);
                holder.txtControl = convertView.findViewById(R.id.txt_control);
                holder.txtPartNo = convertView.findViewById(R.id.txt_part_number);
                holder.txtUncheckQty = convertView.findViewById(R.id.txt_uncheck_qty);
                holder.txtCheckNotInQty = convertView.findViewById(R.id.txt_check_not_in_qty);
                holder.txtInQty = convertView.findViewById(R.id.txt_in_qty);
                holder.txtNotInQty = convertView.findViewById(R.id.txt_not_in_qty);
                holder.txtReceiptNo = convertView.findViewById(R.id.txt_receipt_no);
                holder.txtTrxId = convertView.findViewById(R.id.txt__trx_id);
                holder.llXb = convertView.findViewById(R.id.ll_xb);
                holder.txtInXbQty = convertView.findViewById(R.id.txt_in_xb_qty);
                holder.txtNotInXbQty = convertView.findViewById(R.id.txt_not_in_xb_qty);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            convertView.setVisibility(View.VISIBLE);

            if ((double) data.get(position).get("not_in_qty") == 0) {
                convertView.setBackgroundColor(Color.GRAY);
                convertView.setClickable(false);
            } else {
                convertView.setBackgroundDrawable(getResources().getDrawable(R.drawable.custom_item));
            }

            holder.txtNotInDay.setText(AcceptanceListActivity.this.getString(R.string.not_in_day, (int) data.get(position).get("not_in_day")));
            holder.txtItemLoc.setText((String) data.get(position).get("item_loc"));
            holder.txtItemLocDesc.setText((String) data.get(position).get("item_loc_desc"));
            holder.txtControl.setText((String) data.get(position).get("control"));
            holder.txtPartNo.setText((String) data.get(position).get("part_no"));
            holder.txtUncheckQty.setText(Util.fmt((double) data.get(position).get("uncheck_qty")));
            holder.txtCheckNotInQty.setText(Util.fmt((double) data.get(position).get("check_not_in_qty")));
            holder.txtInQty.setText(Util.fmt((double) data.get(position).get("in_qty")));
            holder.txtNotInQty.setText(Util.fmt((double) data.get(position).get("not_in_qty")));
            holder.txtReceiptNo.setText((String) data.get(position).get("receipt_no"));
            holder.txtTrxId.setText((String) data.get(position).get("trx_id"));

            if (holder.txtPartNo.getText().toString().trim().substring(0, 4).equals(PREFIX_ITEM_NO_PCB)) {
                holder.llXb.setVisibility(View.VISIBLE);
                holder.txtInXbQty.setText(Util.fmt((double) data.get(position).get("xb_in_qty")));
                holder.txtNotInXbQty.setText(Util.fmt((double) data.get(position).get("xb_not_in_qty")));
            } else {
                holder.llXb.setVisibility(View.GONE);
                holder.txtInXbQty.setText("");
                holder.txtNotInXbQty.setText("");
            }

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

    private class ClearAcceptanceInfo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Delete Acceptance Info to " + AppController.getServerInfo() + AppController.getProperties("ClearAcceptanceInfo"));
            publishProgress(getString(R.string.del_data));
            return acceptanceHandler.doDeleteTempInfo(selectedItemInfoHelper);
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

    private class DoStockIn extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Do Acceptance StockIn from " + AppController.getServerInfo() + AppController.getProperties("AcceptanceDoStockIn"));
            publishProgress("執行入庫中...");
            return acceptanceHandler.doStockIn(conditionHelper);
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
