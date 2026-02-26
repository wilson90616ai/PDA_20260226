package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.WaitCheckDetailInfoHelper;
import com.senao.warehouse.database.WaitCheckInfoHelper;
import com.senao.warehouse.handler.WaitCheckHandler;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WaitCheckDetailActivity extends Activity implements View.OnClickListener {

    private ListView listView;
    private TextView mConnection, lblVenderInfo, lblPartNo, lblTotalQty, lblUncheckQty, lblNotInQty;
    private String errorInfo = "";
    private Button btnReturn;
    private ProgressDialog dialog;
    private List<HashMap<String, Object>> data = new ArrayList<>();
    private MyAdapter adapter;
    private WaitCheckHandler handler;
    private WaitCheckInfoHelper itemInfoHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            itemInfoHelper = new Gson().fromJson(extras.getString("ITEM_INFO"),
                    WaitCheckInfoHelper.class);
            if (itemInfoHelper == null) {
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

        setContentView(R.layout.activity_wait_check_detail);
        btnReturn =  findViewById(R.id.button_return);
        lblVenderInfo =  findViewById(R.id.txt_vendor_info);
        lblPartNo =  findViewById(R.id.txt_pn);
        lblTotalQty =  findViewById(R.id.txt_total_qty);
        lblUncheckQty =  findViewById(R.id.txt_uncheck_qty);
        lblNotInQty =  findViewById(R.id.txt_not_in_qty);
        mConnection =  findViewById(R.id.label_status);

        btnReturn.setOnClickListener(this);
        mConnection.setOnClickListener(this);

        listView =  findViewById(R.id.list_item);

        handler = new WaitCheckHandler();

        setSummary();

        doQueryWaitCheckInfo();
    }

    private void doQueryWaitCheckInfo() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetWaitCheckDetail().execute(0);
    }

    private void setSummary() {
        lblVenderInfo.setText(getString(R.string.label_vendor_info, itemInfoHelper.getVendorNo(), itemInfoHelper.getVendorName()));
        lblPartNo.setText(itemInfoHelper.getPartNo());
        lblTotalQty.setText(Util.fmt(itemInfoHelper.getUncheckTotalQty().doubleValue()));
        lblUncheckQty.setText(Util.fmt(itemInfoHelper.getReceivedUncheckQty().doubleValue()));
        lblNotInQty.setText(Util.fmt(itemInfoHelper.getCheckedNotInQty().doubleValue()));
    }

    private void setListData(final WaitCheckInfoHelper tempInfo) {
        listView.setVisibility(View.VISIBLE);
        data.clear();

        if (tempInfo != null && tempInfo.getDetailList() != null) {
            List<WaitCheckDetailInfoHelper> tempList = new ArrayList<>();
            for (WaitCheckDetailInfoHelper info : tempInfo.getDetailList()) {
                tempList.add(info);
                HashMap<String, Object> item = new HashMap<String, Object>();
                item.put("not_in_day", info.getNotInDay());
                item.put("locator", info.getLocator());
                item.put("datecode", info.getDateCode());
                item.put("check_not_in_qty", info.getCheckedNotInQty().doubleValue());
                data.add(item);
            }
            tempInfo.setDetailList(tempList);
        }
        adapter = new MyAdapter(this);
        listView.setAdapter(adapter);
        if (data.size() == 0)
            Toast.makeText(getApplicationContext(), getString(R.string.no_data), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_return) {
            finish();
        } else if (id == R.id.label_status) {
            if (!TextUtils.isEmpty(errorInfo)) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(
                        WaitCheckDetailActivity.this);
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
        public TextView txtLocator;
        public TextView lblDateCode;
        public TextView txtDateCode;
        public TextView txtCheckNotInQty;
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
                convertView = mInflater.inflate(R.layout.wait_check_detail_item, null);
                holder.txtNotInDay =  convertView.findViewById(R.id.txt_not_in_day);
                holder.txtLocator =  convertView.findViewById(R.id.txt_locator);
                holder.lblDateCode =  convertView.findViewById(R.id.label_dc);
                holder.txtDateCode =  convertView.findViewById(R.id.txt_dc);
                holder.txtCheckNotInQty =  convertView.findViewById(R.id.txt_check_not_in_qty);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            convertView.setVisibility(View.VISIBLE);
            convertView.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.custom_item));
            holder.txtNotInDay.setText(WaitCheckDetailActivity.this.getString(R.string.not_in_day, (int) data.get(position).get("not_in_day")));
            holder.txtLocator.setText((String) data.get(position).get("locator"));
            if (itemInfoHelper.getControl().equals("DC")) {
                holder.lblDateCode.setVisibility(View.VISIBLE);
                holder.txtDateCode.setText((String) data.get(position).get("datecode"));
            } else {
                holder.lblDateCode.setVisibility(View.INVISIBLE);
                holder.txtDateCode.setText("");
            }
            holder.txtCheckNotInQty.setText(Util.fmt((double) data.get(position).get("check_not_in_qty")));
            return convertView;
        }


    }

    private class GetWaitCheckDetail extends
            AsyncTask<Integer, String, BasicHelper> {

        @Override
        protected BasicHelper doInBackground(Integer... params) {
            publishProgress(getString(R.string.downloading_data));
            AppController.debug("Get Wait Check Info from " + AppController.getServerInfo()
                    + AppController.getProperties("GetWaitCheckInfo"));
            return handler.getWaitCheckDetail(itemInfoHelper);
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
                itemInfoHelper = (WaitCheckInfoHelper) result;
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
                setListData(itemInfoHelper);
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                setListData(null);
            }
        }

    }
}
