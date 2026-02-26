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
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.WaitCheckInfoHelper;
import com.senao.warehouse.database.WaitCheckQueryConditionHelper;
import com.senao.warehouse.handler.WaitCheckHandler;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WaitCheckListActivity extends Activity implements OnClickListener {

    private static final String TAG = WaitCheckListActivity.class.getSimpleName();
    private EditText txtSearchText;
    private TextView mConnection;
    private String errorInfo = "";
    private ListView listView;
    private Button btnReturn;
    private boolean isInSearchMode;
    private WaitCheckQueryConditionHelper conditionHelper;
    private WaitCheckHandler handler;
    private WaitCheckQueryConditionHelper tempConditionHelper;
    private ProgressDialog dialog;
    private List<HashMap<String, Object>> data = new ArrayList<>();
    private MyAdapter adapter;
    private WaitCheckInfoHelper selectedItemInfoHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            conditionHelper = new Gson().fromJson(extras.getString("CONDITION_INFO"),
                    WaitCheckQueryConditionHelper.class);
            if (conditionHelper == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_condictions)+"(N)",
                        Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_condictions)+"(N)",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_wait_check_list);
        btnReturn =  findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        txtSearchText =  findViewById(R.id.input_search_text);
        txtSearchText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down" +
                            String.valueOf(v.getId()));

                    if (txtSearchText.getText().toString().trim().equals("")) {
                        Toast.makeText(WaitCheckListActivity.this,
                                getString(R.string.query_cons_cant_be_null), Toast.LENGTH_SHORT).show();

                        txtSearchText.requestFocus();

                    } else {
                        isInSearchMode = true;
                        hideKeyboard();
                        getSearchData(txtSearchText.getText().toString()
                                .trim());
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
                    hideKeyboard();
                    clearStatus();
                    setListData(conditionHelper);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

        });

        listView =  findViewById(R.id.list_item);
        mConnection =  findViewById(R.id.label_status);

        handler = new WaitCheckHandler();

        doQueryWaitCheckInfo(null);
    }

    private void clearStatus() {
        mConnection.setText("");
        mConnection.setTextColor(Color.WHITE);
        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        errorInfo = "";
    }

    private void doQueryWaitCheckInfo(String searchText) {
        if (!TextUtils.isEmpty(searchText)) {
            tempConditionHelper = new WaitCheckQueryConditionHelper();
            tempConditionHelper.setWareHouseNo(conditionHelper.getWareHouseNo());
            tempConditionHelper.setPartNo(conditionHelper.getPartNo());
            tempConditionHelper.setReceiptsNo(conditionHelper.getReceiptsNo());
            tempConditionHelper.setTrxIdNo(conditionHelper.getTrxIdNo());
            tempConditionHelper.setSearchText(searchText);
        }

        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetWaitCheckInfo().execute(0);
    }

    private void getSearchData(String text) {
        if (conditionHelper != null && conditionHelper.getInfoList() != null
                && conditionHelper.getInfoList().length > 0) {
            List<WaitCheckInfoHelper> itemInfoList = new ArrayList<>();
            for (int i = 0; i < conditionHelper.getInfoList().length; i++) {
                if (conditionHelper.getInfoList()[i].getPartNo().contains(text)) {
                    itemInfoList.add(conditionHelper.getInfoList()[i]);
                }
            }
            if (itemInfoList.size() > 0) {
                tempConditionHelper = new WaitCheckQueryConditionHelper();
                tempConditionHelper.setWareHouseNo(conditionHelper.getWareHouseNo());
                tempConditionHelper.setPartNo(conditionHelper.getPartNo());
                tempConditionHelper.setReceiptsNo(conditionHelper.getReceiptsNo());
                tempConditionHelper.setTrxIdNo(conditionHelper.getTrxIdNo());
                tempConditionHelper.setInfoList(itemInfoList);
                setListData(tempConditionHelper);
            } else {
                doQueryWaitCheckInfo(text);
            }
        } else {
            doQueryWaitCheckInfo(text);
        }
    }

    private void setListData(final WaitCheckQueryConditionHelper tempInfo) {
        listView.setVisibility(View.VISIBLE);
        data.clear();

        if (tempInfo != null && tempInfo.getInfoList() != null) {
            List<WaitCheckInfoHelper> tempList = new ArrayList<>();
            for (WaitCheckInfoHelper info : tempInfo.getInfoList()) {
                tempList.add(info);
                HashMap<String, Object> item = new HashMap<String, Object>();
                item.put("not_in_day", info.getNotInDay());
                item.put("part_no", info.getPartNo());
                item.put("wait_check_qty", info.getUncheckTotalQty().doubleValue());
                item.put("uncheck_qty", info.getReceivedUncheckQty().doubleValue());
                item.put("check_not_in_qty", info.getCheckedNotInQty().doubleValue());
                data.add(item);
            }
            tempInfo.setInfoList(tempList);
        }
        adapter = new MyAdapter(this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                selectedItemInfoHelper = tempInfo.getInfoList()[position];
                AppController.debug("onClick item " + position + " PartNo="
                        + selectedItemInfoHelper.getPartNo());
                if (selectedItemInfoHelper.getCheckedNotInQty().doubleValue() > 0) {
                    Intent intent = new Intent(getBaseContext(),
                            WaitCheckDetailActivity.class);
                    intent.putExtra("ITEM_INFO", new Gson().toJson(selectedItemInfoHelper));
                    startActivity(intent);
                }
            }
        });

        if (data.size() == 0)
            Toast.makeText(getApplicationContext(), getString(R.string.no_data), Toast.LENGTH_LONG).show();
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
        } else if (id == R.id.label_status) {
            if (!TextUtils.isEmpty(errorInfo)) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(
                        WaitCheckListActivity.this);
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

    static class ViewHolder {
        public TextView txtNotInDay;
        public TextView txtPartNo;
        public TextView txtWaitCheckQty;
        public TextView txtUncheckQty;
        public TextView txtCheckNotInQty;
    }

    private class GetWaitCheckInfo extends
            AsyncTask<Integer, String, BasicHelper> {

        @Override
        protected BasicHelper doInBackground(Integer... params) {
            publishProgress(getString(R.string.downloading_data));
            AppController.debug("Get Wait Check Info from " + AppController.getServerInfo()
                    + AppController.getProperties("GetWaitCheckInfo"));
            if (isInSearchMode) {
                return handler.getWaitCheckInfo(tempConditionHelper);
            } else {
                return handler.getWaitCheckInfo(conditionHelper);
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
            AppController.debug("onPostExecute() result json = "
                    + new Gson().toJson(result));

            if (result != null) {
                if (isInSearchMode)
                    tempConditionHelper = (WaitCheckQueryConditionHelper) result;
                else
                    conditionHelper = (WaitCheckQueryConditionHelper) result;
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
                    setListData(tempConditionHelper);
                else
                    setListData(conditionHelper);
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                setListData(null);
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
                convertView = mInflater.inflate(R.layout.wait_check_summary, null);
                holder.txtNotInDay =  convertView.findViewById(R.id.txt_not_in_day);
                holder.txtPartNo =  convertView.findViewById(R.id.txt_part_no);
                holder.txtWaitCheckQty =  convertView.findViewById(R.id.txt_wait_check_qty);
                holder.txtUncheckQty =  convertView.findViewById(R.id.txt_received_uncheck_qty);
                holder.txtCheckNotInQty =  convertView.findViewById(R.id.txt_check_not_in_qty);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            convertView.setVisibility(View.VISIBLE);
            convertView.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.custom_item));
            holder.txtNotInDay.setText(WaitCheckListActivity.this.getString(R.string.not_in_day, (int) data.get(position).get("not_in_day")));
            holder.txtPartNo.setText((String) data.get(position).get("part_no"));
            holder.txtWaitCheckQty.setText(Util.fmt((double) data.get(position).get("wait_check_qty")));
            holder.txtUncheckQty.setText(Util.fmt((double) data.get(position).get("uncheck_qty")));
            holder.txtCheckNotInQty.setText(Util.fmt((double) data.get(position).get("check_not_in_qty")));
            return convertView;
        }


    }
}
