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
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.R;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.ItemInfoHelper;
import com.senao.warehouse.database.PalletInfoHelper;
import com.senao.warehouse.handler.ShipmentPalletHandler;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;

public class ShipmentPalletActivity extends Activity {

    private TextView mConnection, mPartNo,lblTitle;
    private Button btnReturn, btnConfim, btnCancel;
    private EditText txtAirPltQty, txtAirPltSize, txtSeaPltQty, txtSeaPltSize;
    private ItemInfoHelper item;
    private PalletInfoHelper palletInfo;
    private ShipmentPalletHandler shipment;
    // private boolean isUpdateSuccess;
    private ProgressDialog dialog;
    private String errorInfo = "";
    private int intPackQty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_picking_pallet_data);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            item = new Gson().fromJson(extras.getString("ITEM_INFO"), ItemInfoHelper.class);

            if (item == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_sku), Toast.LENGTH_SHORT).show();
                finish();
                return;
            } else {
                shipment = new ShipmentPalletHandler(item);
            }

            intPackQty = extras.getInt("PACK_QTY", 0);

            if (intPackQty == 0) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_box), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_pallet), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mConnection =  findViewById(R.id.label_status);
        mConnection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ShipmentPalletActivity.this);
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

        mPartNo = findViewById(R.id.label_part_no);
        txtAirPltQty = findViewById(R.id.edittext_air_plt_qty);
        txtAirPltSize = findViewById(R.id.edittext_air_plt_size);
        txtSeaPltQty = findViewById(R.id.edittext_sea_plt_qty);
        txtSeaPltSize = findViewById(R.id.edittext_sea_plt_size);
        txtAirPltQty.setSelectAllOnFocus(true);
        txtAirPltSize.setSelectAllOnFocus(true);
        txtSeaPltQty.setSelectAllOnFocus(true);
        txtSeaPltSize.setSelectAllOnFocus(true);

        mPartNo.setText(getResources().getString(R.string.label_part_no) + " " + item.getItemID());

        btnReturn = findViewById(R.id.button_return);
        btnConfim = findViewById(R.id.button_confirm);
        btnCancel = findViewById(R.id.button_cancel);

        btnReturn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Return", String.valueOf(v.getId()));
                returnPage();
            }

        });

        btnConfim.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AppController.debug("onClick Confirm " + String.valueOf(v.getId()));

                if (checkPalletInfo()) {
                    if (!comparePalletInfo()) {
                        updatePalletInfo();
                        new SetItemPalletInfo().execute(0);
                        dialog = ProgressDialog.show(
                                ShipmentPalletActivity.this, getString(R.string.holdon), getString(R.string.data_uploading),
                                true);
                    } else {
                        toNextPage();
                    }
                }
            }

        });

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Cancel", String.valueOf(v.getId()));

                if (checkPalletInfo()) {
                    toNextPage();
                }
            }

        });

        lblTitle = findViewById(R.id.ap_title);
        setTitle();
        new GetItemPalletInfo().execute(0);
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.label_picking, AppController.getOrgName()));
        text.setSpan(new RelativeSizeSpan(1f), 7, 7 + AppController.getOrgName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private void setPalletInfo() {
        txtAirPltQty.setText(String.valueOf(palletInfo.getAirQty()));
        txtAirPltSize.setText(palletInfo.getAirLWH());
        txtSeaPltQty.setText(String.valueOf(palletInfo.getSeaQty()));
        txtSeaPltSize.setText(palletInfo.getSeaLWH());
    }

    private boolean checkPalletInfo() {
        if (txtAirPltQty.getText().toString().trim().equals("")) {
            Toast.makeText(ShipmentPalletActivity.this, getString(R.string.air_pallet_qty_is_not_null), Toast.LENGTH_SHORT).show();
            txtAirPltQty.requestFocus();
        } else if (txtAirPltSize.getText().toString().trim().equals("")) {
            Toast.makeText(ShipmentPalletActivity.this, getString(R.string.air_pallet_size_is_not_null), Toast.LENGTH_SHORT).show();
            txtAirPltSize.requestFocus();
        } else if (txtSeaPltQty.getText().toString().trim().equals("")) {
            Toast.makeText(ShipmentPalletActivity.this, getString(R.string.shipping_pallet_qty_is_not_null), Toast.LENGTH_SHORT).show();
            txtSeaPltQty.requestFocus();
        } else if (txtSeaPltSize.getText().toString().trim().equals("")) {
            Toast.makeText(ShipmentPalletActivity.this, getString(R.string.shipping_pallet_size_is_not_null), Toast.LENGTH_SHORT).show();
            txtSeaPltSize.requestFocus();
        } else {
            return true;
        }

        return false;
    }

    private void toNextPage() {
        Intent intent;

        if (item.getControl().equals("DC") || item.getControl().equals("NO")) {
            intent = new Intent(getBaseContext(), ShipmentCreatePalletActivity.class);
        } else {
            intent = new Intent(getBaseContext(), ShipmentPoActivity.class);
        }

        intent.putExtra("ITEM_INFO", new Gson().toJson(item));
        intent.putExtra("PACK_QTY", intPackQty);
        startActivityForResult(intent, 0);
    }

    private boolean comparePalletInfo() {
        if (palletInfo.getAirQty() == Integer.parseInt(txtAirPltQty.getText().toString().trim())
                && palletInfo.getAirLWH() != null
                && palletInfo.getAirLWH().equals(txtAirPltSize.getText().toString().trim())
                && palletInfo.getSeaQty() == Integer.parseInt(txtSeaPltQty.getText().toString().trim())
                && palletInfo.getSeaLWH() != null
                && palletInfo.getSeaLWH().equals(txtSeaPltSize.getText().toString().trim())) {
            return true;
        }

        return false;
    }

    private void updatePalletInfo() {
        palletInfo.setAirQty(Integer.parseInt(txtAirPltQty.getText().toString().trim()));
        palletInfo.setAirLWH(txtAirPltSize.getText().toString().trim());
        palletInfo.setSeaQty(Integer.parseInt(txtSeaPltQty.getText().toString().trim()));
        palletInfo.setSeaLWH(txtSeaPltSize.getText().toString().trim());
    }

    public void onBackPressed() {
        // do something here and don't write super.onBackPressed()
        Log.d("onClick Back Button", "");
        returnPage();
    }

    private void returnPage() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        } else if (resultCode == RESULT_FIRST_USER) {
            setResult(RESULT_OK);
        }
    }

    private class GetItemPalletInfo extends AsyncTask<Integer, String, PalletInfoHelper> {
        @Override
        protected PalletInfoHelper doInBackground(Integer... params) {
            AppController.debug("Get Pallet Info from " + AppController.getServerInfo()
                    + AppController.getProperties("GetPalletInfo"));
            publishProgress(getString(R.string.downloading_data));
            return shipment.getPalletInfo();
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
        protected void onPostExecute(PalletInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);

            AppController.debug("GetItemPalletInfo result json = " + new Gson().toJson(result));

            if (result != null) {
                palletInfo = result;

                if (palletInfo.getIntRetCode() == ReturnCode.OK) {
                    setPalletInfo();
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

                    errorInfo = palletInfo.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
            }
        }
    }

    private class SetItemPalletInfo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Send PalletInfo to " + AppController.getServerInfo()
                    + AppController.getProperties("SetPalletInfo"));
            publishProgress(getString(R.string.data_uploading));
            return shipment.setPalletInfo(palletInfo);
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

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText(getString(R.string.data_updated_successfully));
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    // isUpdateSuccess = true;
                    errorInfo = "";
                    dialog.dismiss();
                    toNextPage();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    // isUpdateSuccess = false;
                    dialog.dismiss();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                // isUpdateSuccess = false;
                errorInfo = "";
                dialog.dismiss();
            }
        }
    }
}
