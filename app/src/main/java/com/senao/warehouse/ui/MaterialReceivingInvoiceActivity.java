package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.R;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.RECEIVING_TYPE;
import com.senao.warehouse.database.VendorInfoHelper;
import com.senao.warehouse.handler.ReceivingHandler;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MaterialReceivingInvoiceActivity extends Activity {

    private ListView listView;
    private VendorInfoHelper vendorInfo;
    private RECEIVING_TYPE receivingType;
    private ReceivingHandler receivingHandler;
    private TextView lblTitle, mConnection;
    private int accType;
    private String errorInfo;
    private Button btnReturn,btnConfirm,btnCancel;
    private List<String> listData;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_receiving_invoice);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            vendorInfo = new Gson().fromJson(extras.getString("VENDOR_INFO"), VendorInfoHelper.class);

            if (vendorInfo == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_vc_data), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            receivingType = RECEIVING_TYPE.valueOf(vendorInfo.getReceivingType());
            receivingHandler = new ReceivingHandler();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_vc_data), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        lblTitle =  findViewById(R.id.ap_title);

        accType = vendorInfo.isOutSourcing() ? 1 : 0;

        mConnection =  findViewById(R.id.label_status);
        mConnection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MaterialReceivingInvoiceActivity.this);
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
        btnConfirm =  findViewById(R.id.button_confirm);
        btnCancel =  findViewById(R.id.button_cancel);

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Return", String.valueOf(v.getId()));
                returnPage();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNextPage(receivingType);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Cancel", String.valueOf(v.getId()));
                cleanData();
            }
        });

        listView =  findViewById(R.id.list);
        setTitle();
        doQueryInvoiceData();
    }

    private void doQueryInvoiceData() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetReceivingInvoiceNoList().execute(0);
    }

    private void cleanData() {
        for ( int i=0; i < listView.getAdapter().getCount(); i++) {
            listView.setItemChecked(i, false);
        }
    }

    private void setTitle() {
        String subtitle = null;

        switch (receivingType) {
            case REC_COMBINE_INVOICE:
                subtitle = getString(R.string.combine_invoice) + " - " + getString(R.string.Receiving);
                break;
            case TEMP_COMBINE_INVOICE:
                subtitle = getString(R.string.combine_invoice) + " - " + getString(R.string.Pending_inspection);
                break;
        }

        final SpannableString text;

        if (accType == MaterialReceivingActivity.OUTSOURCING) {
            text = new SpannableString(getString(R.string.label_receiving_outsourcing1,AppController.getOrgName(), subtitle));
        } else {
            text = new SpannableString(getString(R.string.label_receiving1,AppController.getOrgName(), subtitle));
        }

        text.setSpan(new RelativeSizeSpan(1.0f), 0, 6,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new RelativeSizeSpan(0.5f), 7+AppController.getOrgName().length(), 7+AppController.getOrgName().length() + subtitle.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private void goToNextPage(RECEIVING_TYPE type) {
        Intent intent = new Intent(this, MaterialReceivingListActivity.class);
        vendorInfo.setOutSourcing(accType == 1);
        vendorInfo.setReceivingType(type.name());
        intent.putExtra("VENDOR_INFO", new Gson().toJson(vendorInfo));
        startActivityForResult(intent, type.ordinal());
    }

    private void returnPage() {
        finish();
    }

    public void onBackPressed() {
        returnPage();
    }

    private class GetReceivingInvoiceNoList extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Get Receiving Info List  from " + AppController.getServerInfo()
                    + AppController.getProperties("GetReceivingInvoiceNoList"));
            publishProgress(getString(R.string.downloading_data));
            return receivingHandler.getReceivingInvoiceNoList(vendorInfo);
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

    private void setListData(final VendorInfoHelper vendorInfo) {
        if (vendorInfo == null || vendorInfo.getInvoiceList() == null) {
            listData = new ArrayList<>();
        } else {
            listData = Arrays.asList(vendorInfo.getInvoiceList());
        }

        listView.setAdapter(new ArrayAdapter<>(this, R.layout.simple_list_item_multiple_choice, listData));

        for ( int i=0; i < listView.getAdapter().getCount(); i++) {
            listView.setItemChecked(i, true);
        }

        if (listData.size() == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.no_data), Toast.LENGTH_LONG).show();
        }
    }
}
