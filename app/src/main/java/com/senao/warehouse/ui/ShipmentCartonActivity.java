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
import com.senao.warehouse.database.CartonInfoHelper;
import com.senao.warehouse.database.ItemInfoHelper;
import com.senao.warehouse.handler.ShipmentCartonHandler;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;

public class ShipmentCartonActivity extends Activity {

    private TextView mConnection, mPartNo,lblTitle;
    private Button btnReturn, btnConfim, btnCancel;
    private EditText txtPackQty, txtCartonWeight, txtCartonCuft, txtCartonSize;
    private ItemInfoHelper item;
    private CartonInfoHelper carton;
    private ShipmentCartonHandler shipment;
    // private boolean isUpdateSuccess;
    private ProgressDialog dialog;
    private String errorInfo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_picking_carton_data);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            item = new Gson().fromJson(extras.getString("ITEM_INFO"), ItemInfoHelper.class);

            if (item == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_single_box), Toast.LENGTH_SHORT).show();
                finish();
                return;
            } else {
                shipment = new ShipmentCartonHandler(item);
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_single_box), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mConnection =  findViewById(R.id.label_status);
        mConnection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(
                            ShipmentCartonActivity.this);
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
        txtPackQty = findViewById(R.id.edittext_pack_qty);
        txtCartonWeight = findViewById(R.id.edittext_carton_weight);
        txtCartonCuft = findViewById(R.id.edittext_carton_cuft);
        txtCartonSize = findViewById(R.id.edittext_carton_size);
        txtPackQty.setSelectAllOnFocus(true);
        txtCartonWeight.setSelectAllOnFocus(true);
        txtCartonCuft.setSelectAllOnFocus(true);
        txtCartonSize.setSelectAllOnFocus(true);

        mPartNo.setText(getResources().getString(R.string.label_part_no) + " " + item.getItemID());

        btnReturn = findViewById(R.id.button_return);
        btnConfim = findViewById(R.id.button_confirm);
        btnCancel = findViewById(R.id.button_cancel);

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Return", String.valueOf(v.getId()));
                returnPage();
            }

        });

        btnConfim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppController.debug("onClick Confim " + String.valueOf(v.getId()));
                if (checkCartonInfo()) {
                    if (!compareCartonInfo()) {
                        updateCartonInfo();
                        new SetItemCartonInfo().execute(0);
                        dialog = ProgressDialog.show(
                                ShipmentCartonActivity.this, getString(R.string.holdon), getString(R.string.data_uploading),
                                true);
                    } else {
                        toNextPage(carton.getBoxQty());
                    }
                }
            }

        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Cancel", String.valueOf(v.getId()));

                if (checkCartonInfo()) {
                    // if (compareCartonInfo()) {
                    // isUpdateSuccess = true;
                    toNextPage(carton.getBoxQty());
                    /*
                     * } else { Toast.makeText(ShipmentCartonActivity.this,
                     * "資料已異動！請先按確定更新資料。", Toast.LENGTH_SHORT).show();
                     * btnConfim.requestFocus(); }
                     */
                }
            }

        });

        lblTitle = findViewById(R.id.ap_title);
        setTitle();
        new GetItemCartonInfo().execute(0);
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.label_picking, AppController.getOrgName()));
        text.setSpan(new RelativeSizeSpan(1f), 7, 7 + AppController.getOrgName().length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private void setCartonInfo() {
        txtPackQty.setText(String.valueOf(carton.getBoxQty()));
        txtCartonWeight.setText(String.valueOf(carton.getBoxWeight()));
        txtCartonCuft.setText(String.valueOf(carton.getVolume()));
        txtCartonSize.setText(carton.getBoxLWH());
    }

    private boolean checkCartonInfo() {
        if (txtPackQty.getText().toString().trim().equals("")) {
            Toast.makeText(ShipmentCartonActivity.this, getString(R.string.single_box_qty_is_not_null), Toast.LENGTH_SHORT).show();
            txtPackQty.requestFocus();
        } else if (txtCartonWeight.getText().toString().trim().equals("")) {
            Toast.makeText(ShipmentCartonActivity.this,getString( R.string.single_box_weight_is_not_null), Toast.LENGTH_SHORT).show();
            txtCartonWeight.requestFocus();
        } else if (txtCartonCuft.getText().toString().trim().equals("")) {
            Toast.makeText(ShipmentCartonActivity.this, getString(R.string.single_box_volume_is_not_null), Toast.LENGTH_SHORT).show();
            txtCartonCuft.requestFocus();
        } else if (txtCartonSize.getText().toString().trim().equals("")) {
            Toast.makeText(ShipmentCartonActivity.this,getString(R.string.single_box_size_is_not_null) , Toast.LENGTH_SHORT).show();
            txtCartonSize.requestFocus();
        } else {
            return true;
        }

        return false;
    }

    private void toNextPage(int intPackQty) {
        Intent intent;
        intent = new Intent(getBaseContext(), ShipmentPalletActivity.class);

//        if (item.getControl().equals("DC") || item.getControl().equals("NO")) {
//            intent = new Intent(getBaseContext(), ShipmentCreatePalletActivity.class);
//        } else {
//            intent = new Intent(getBaseContext(), ShipmentPickingActivity.class);
//        }

        intent.putExtra("ITEM_INFO", new Gson().toJson(item));
        intent.putExtra("PACK_QTY", intPackQty);
        startActivityForResult(intent, 0);
    }

    private boolean compareCartonInfo() {
        if (carton.getBoxQty() == Integer.parseInt(txtPackQty.getText().toString().trim())
                && carton.getBoxWeight() == Double.parseDouble(txtCartonWeight.getText().toString().trim())
                && carton.getVolume() == Double.parseDouble(txtCartonCuft.getText().toString().trim())) {
            if (carton.getBoxLWH() != null) {
                if (carton.getBoxLWH().equals(txtCartonSize.getText().toString().trim())) {
                    // isUpdateSuccess = true;
                    return true;
                }
            }
        }

        return false;
    }

    private void updateCartonInfo() {
        carton.setBoxQty(Integer.parseInt(txtPackQty.getText().toString().trim()));
        carton.setBoxWeight(Double.parseDouble(txtCartonWeight.getText().toString().trim()));
        carton.setVolume(Double.parseDouble(txtCartonCuft.getText().toString().trim()));
        carton.setBoxLWH(txtCartonSize.getText().toString().trim());
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

    private class GetItemCartonInfo extends AsyncTask<Integer, String, CartonInfoHelper> {
        @Override
        protected CartonInfoHelper doInBackground(Integer... params) {
            AppController.debug("Get CartonInfo from " + AppController.getServerInfo()
                    + AppController.getProperties("GetCartonInfo"));
            publishProgress(getString(R.string.downloading_data));
            return shipment.getCartonInfo();
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
        protected void onPostExecute(CartonInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);

            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                carton = result;

                if (carton.getIntRetCode() == ReturnCode.OK) {
                    setCartonInfo();
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

                    errorInfo = carton.getStrErrorBuf();
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

    private class SetItemCartonInfo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Send CartonInfo to " + AppController.getServerInfo()
                    + AppController.getProperties("GetCartonInfo"));
            publishProgress(getString(R.string.data_uploading));
            return shipment.setCartonInfo(carton);
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
                    toNextPage(Integer.parseInt(txtPackQty.getText().toString().trim()));
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
