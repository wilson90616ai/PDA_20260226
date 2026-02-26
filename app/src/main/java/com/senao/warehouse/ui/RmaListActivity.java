package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.R;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.RmaBoxInfoHelper;
import com.senao.warehouse.database.RmaBoxListInfoHelper;
import com.senao.warehouse.handler.RmaHandler;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RmaListActivity extends Activity implements View.OnClickListener {

    private ListView listView;
    private String palletId;
    private ProgressDialog dialog;
    private RmaHandler handler;
    private TextView tvSummary, mConnection;
    private String errorInfo;
    private RmaBoxListInfoHelper boxListInfo;
    private RmaBoxListInfoHelper selectItemInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rma_list);

        palletId = getIntent().getStringExtra("PALLET_ID");

        if (palletId == null) {
            finish();
            return;
        }

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(this);

        Button btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        tvSummary = findViewById(R.id.textViewSummary);
        tvSummary.setText(getString(R.string.rma_picking_summary, 0, 0));

        listView = findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final HashMap<String, String> item = (HashMap<String, String>) parent.getItemAtPosition(position);
                String cartonNo = item.get("title").replace("CARTON NO: ", "");
                doQueryCartonInfo(cartonNo);
            }
        });

        handler = new RmaHandler();
        doQueryPalletInfo();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.button_return) {
            finish();
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

    private void doQueryPalletInfo() {
        boxListInfo = new RmaBoxListInfoHelper();
        boxListInfo.setPalletId(palletId);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetPalletInfo().execute(0);
    }

    private void doQueryCartonInfo(String cartonNo) {
        selectItemInfo = new RmaBoxListInfoHelper();
        selectItemInfo.setPalletId(palletId);
        RmaBoxInfoHelper item = new RmaBoxInfoHelper();
        item.setBoxNo(cartonNo);
        List<RmaBoxInfoHelper> list =new ArrayList<>();
        list.add(item);
        selectItemInfo.setBoxInfoList(list);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetCartonInfo().execute(0);
    }

    private void setListData(final RmaBoxListInfoHelper tempInfo) {
        List<HashMap<String, String>> list = new ArrayList<>();

        if (tempInfo != null && tempInfo.getBoxInfoList() != null && tempInfo.getBoxInfoList().length > 0) {
            //使用List存入HashMap，用來顯示ListView上面的文字。
            for (int i = 0; i < tempInfo.getBoxInfoList().length; i++) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("title", "CARTON NO: " + tempInfo.getBoxInfoList()[i].getBoxNo());
                hashMap.put("text", "QTY: " + tempInfo.getBoxInfoList()[i].getQty());
                //把title , text存入HashMap之中
                list.add(hashMap);
                //把HashMap存入list之中
            }

            ListAdapter listAdapter = new SimpleAdapter(
                    this,
                    list,
                    R.layout.simple_list_item_2,
                    new String[]{"title", "text"},
                    new int[]{R.id.text1, R.id.text2});
            // 5個參數 : context , List , layout , key1 & key2 , text1 & text2

            listView.setAdapter(listAdapter);
            setSummary(tempInfo);
        }

        if (list.size() == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.no_data), Toast.LENGTH_LONG).show();
            listView.setVisibility(View.GONE);
            tvSummary.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.VISIBLE);
            tvSummary.setVisibility(View.VISIBLE);
        }
    }

    private void setSummary(final RmaBoxListInfoHelper tempInfo) {
        int count = 0;

        for (RmaBoxInfoHelper item : tempInfo.getBoxInfoList()) {
            count += item.getQty();
        }

        tvSummary.setText(getString(R.string.rma_picking_summary, tempInfo.getBoxInfoList().length, count));
    }

    private void showListDialog(final String[] snList) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        //builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle(R.string.sn_list);
        //final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
        //for (String item : snList) {
        //    arrayAdapter.add(item);
        //}

        builderSingle.setItems(snList,null);

        builderSingle.setNeutralButton( getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //builderSingle.setAdapter(arrayAdapter,null);

        builderSingle.show();
    }

    private class GetPalletInfo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            publishProgress(getString(R.string.downloading_data));
            AppController.debug("Get Rma Pallet Info from " + AppController.getServerInfo()
                    + AppController.getProperties("GetRmaPalletInfo"));
            return handler.getPalletInfo(boxListInfo);
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
                boxListInfo = (RmaBoxListInfoHelper) result;

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
                setListData(boxListInfo);
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                setListData(null);
            }
        }
    }

    private class GetCartonInfo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            publishProgress(getString(R.string.downloading_data));
            AppController.debug("Get Rma Carton Info from " + AppController.getServerInfo()
                    + AppController.getProperties("GetRmaCartonInfo"));
            return handler.getRmaCartonInfo(selectItemInfo);
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
                    selectItemInfo = (RmaBoxListInfoHelper) result;
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    showListDialog(selectItemInfo.getBoxInfoList()[0].getSnList());
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
