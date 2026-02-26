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
import com.senao.warehouse.database.SendOnHandQtyInfo;
import com.senao.warehouse.database.SendingInfoHelper;
import com.senao.warehouse.database.UserInfoHelper;
import com.senao.warehouse.handler.SendingHandler;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MaterialLackListActivity extends Activity implements OnClickListener {
    private static final String TAG = MaterialLackListActivity.class.getSimpleName();
    List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    private MyAdapter adapter;
    private TextView mConnection, mEmployeeId, mAccount,lblTitle;
    private EditText txtInputSearchText;
    private Button btnReturn;
    private ListView listView;
    private UserInfoHelper user = AppController.getUser();
    private SendingHandler sendingHandler;
    private SendingInfoHelper sendInfo;
    private SendingInfoHelper tempSendInfo;
    private ProgressDialog dialog;
    private String errorInfo = "";
    private boolean isInSearchMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
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

        setContentView(R.layout.activity_material_lack_list);

        mConnection =  findViewById(R.id.label_status);
        mConnection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MaterialLackListActivity.this);
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

        btnReturn =  findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        listView =  findViewById(R.id.list_item);
        listView.setVisibility(View.GONE);

        mAccount =  findViewById(R.id.textview_login_account);
        mAccount.setText(user.getUserName());

        mEmployeeId =  findViewById(R.id.textview_employee_id);
        mEmployeeId.setText(String.valueOf(user.getPassword()));

        txtInputSearchText =  findViewById(R.id.input_search_text);
        txtInputSearchText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    //Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down" + String.valueOf(v.getId()));

                    if (txtInputSearchText.getText().toString().trim().equals("")) {
                        Toast.makeText(MaterialLackListActivity.this, getString(R.string.query_cons_cant_be_null), Toast.LENGTH_SHORT).show();
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
                    doQueryLackSendInfo();
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
        lblTitle = findViewById(R.id.ap_title);
        setTitle();
        doQueryLackSendInfo();
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.title_activity_material_lack_query1, AppController.getOrgName()));
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
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
                Toast.makeText(getApplicationContext(), getString(R.string.No_data_found), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.No_data_found), Toast.LENGTH_LONG).show();
        }
    }

    private void setListData(final SendingInfoHelper sendInfo) {
        listView.setVisibility(View.VISIBLE);
        data.clear();

        if (sendInfo != null && sendInfo.getOnHandQtyList() != null) {
            List<SendOnHandQtyInfo> tempList = new ArrayList<>();

            for (SendOnHandQtyInfo info : sendInfo.getOnHandQtyList()) {
                tempList.add(info);
                HashMap<String, Object> item = new HashMap<String, Object>();
                item.put("part_no", info.getPartNo());
                item.put("control", info.getControl());
                item.put("total_sending_qty", info.getTotalSendingQty().doubleValue());
                item.put("sent_qty", info.getSentQty().doubleValue());
                item.put("unsent_qty", info.getUnsentQty().doubleValue());
                item.put("unchecked_qty", info.getUncheckedQty().doubleValue());
                item.put("onhand_qty", info.getOnHandQty().doubleValue());
                data.add(item);
            }

            sendInfo.setOnHandQtyList(tempList);
        }

        adapter = new MyAdapter(this);
        listView.setAdapter(adapter);

        if (data.size() == 0)
            Toast.makeText(getApplicationContext(), getString(R.string.no_data), Toast.LENGTH_LONG);
    }

    @Override
    public void onClick(View v) {
        Log.d("onClick Return", String.valueOf(v.getId()));

        if (v.getId() == R.id.button_return) {
            returnPage();
        }
    }

    public void onBackPressed() {
        //do something here and don't write super.onBackPressed()
        Log.d(TAG, "onClick Hardware Back Button");
        returnPage();
    }

    private void returnPage() {
        finish();
    }

    private void doQueryLackSendInfo() {
        tempSendInfo = new SendingInfoHelper();
        tempSendInfo.setLineNo(sendInfo.getLineNo());
        tempSendInfo.setMergeNo(sendInfo.getMergeNo());
        tempSendInfo.setWhNo(sendInfo.getWhNo());
        tempSendInfo.setWoNo(sendInfo.getWoNo());
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetItemNoOhQuery().execute(0);
    }

    private class GetItemNoOhQuery extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Get Item No Oh Query from " + AppController.getServerInfo() + AppController.getProperties("ItemNoOhQuery"));
            publishProgress(getString(R.string.downloading_data));
            return sendingHandler.geItemNoOhQuery(tempSendInfo);
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
                tempSendInfo = (SendingInfoHelper) result;
                if (TextUtils.isEmpty(tempSendInfo.getSearchText())) {
                    sendInfo = tempSendInfo;
                }

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

                setListData(tempSendInfo);
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                setListData(null);
            }
        }
    }

    static class ViewHolder {
        public TextView txtPartNo;
        public TextView txtTotalSendQty;
        public TextView txtSentQty;
        public TextView txtUnSentQty;
        public TextView txtUncheckedQty;
        public TextView txtOnHandQty;
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
                convertView = mInflater.inflate(R.layout.lack_summary, null);
                holder.txtPartNo =  convertView.findViewById(R.id.txt_part_no);
                holder.txtTotalSendQty =  convertView.findViewById(R.id.txt_total_sending_qty);
                holder.txtSentQty =  convertView.findViewById(R.id.txt_send_qty);
                holder.txtUnSentQty =  convertView.findViewById(R.id.txt_unsend_qty);
                holder.txtUncheckedQty =  convertView.findViewById(R.id.txt_uncheck_qty);
                holder.txtOnHandQty =  convertView.findViewById(R.id.txt_inventory);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (String.valueOf(data.get(position).get("unsent_qty")).equals("0")) {
                convertView.setBackgroundColor(Color.GRAY);
                convertView.setClickable(false);
            } else {
                convertView.setBackgroundDrawable(getResources().getDrawable(R.drawable.custom_item));
            }

            holder.txtPartNo.setText((String) data.get(position).get("part_no"));
            holder.txtTotalSendQty.setText(Util.fmt((double) data.get(position).get("total_sending_qty")));
            holder.txtSentQty.setText(Util.fmt((double) data.get(position).get("sent_qty")));
            holder.txtUnSentQty.setText(Util.fmt((double) data.get(position).get("unsent_qty")));
            holder.txtUncheckedQty.setText(Util.fmt((double) data.get(position).get("unchecked_qty")));
            holder.txtOnHandQty.setText(Util.fmt((double) data.get(position).get("onhand_qty")));
            return convertView;
        }
    }
}
