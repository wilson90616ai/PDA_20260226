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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.R;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.DeliveryInfoHelper;
import com.senao.warehouse.database.ItemInfoHelper;
import com.senao.warehouse.database.ItemPoInfoHelper;
import com.senao.warehouse.database.OeInfo;
import com.senao.warehouse.handler.ShipmentPickingHandler;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;

import java.util.ArrayList;
import java.util.HashMap;

public class ShipmentPoActivity extends Activity {

    public final String TAG = ShipmentPoActivity.class.getSimpleName();
    private TextView mConnection, mOrderStatus, mPartNo, mCheckingQty,lblTitle;
    private Button btnReturn, btnYes, btnNo;
    private RadioButton rbNoPo, rbPo;
    private RadioGroup rbGroup;
    private ItemInfoHelper itemInfo;
    private int intPackQty;
    private String errorInfo = "";
    private ShipmentPickingHandler shipment;
    private DeliveryInfoHelper delPackingInfo;
    private ListView listView;
    private ProgressDialog dialog;
    private ItemPoInfoHelper itemPoInfoHelper;
    private ArrayList<HashMap<String, String>> list = new ArrayList<>();
    private OeInfo selectedOeInfo;
    private final String CUSTOMER_VERKADA = "VERKADA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.debug("Customer Name getCustomer() :" +getCustomer());

        if (AppController.getUser() == null) {
            finish();
            return;
        }

        setContentView(R.layout.activity_shipping_po);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            itemInfo = new Gson().fromJson(extras.getString("ITEM_INFO"),ItemInfoHelper.class);

            if (itemInfo == null || AppController.getDnInfo() == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_sku), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            intPackQty = extras.getInt("PACK_QTY");
            shipment = new ShipmentPickingHandler();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_sku), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (getCustomer().equals(CUSTOMER_VERKADA)) {
            AppController.debug("S========>>>>>1");
        }

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ShipmentPoActivity.this);
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

        mOrderStatus = findViewById(R.id.textview_order_status);
        mPartNo = findViewById(R.id.label_part_no);
        mCheckingQty = findViewById(R.id.label_checking_quantity);

        rbNoPo = findViewById(R.id.radio_none_po);
        rbPo = findViewById(R.id.radio_po);
        rbGroup = findViewById(R.id.radio_group);

        btnReturn = findViewById(R.id.button_return);
        btnYes = findViewById(R.id.button_yes);
        btnNo = findViewById(R.id.button_no);

        mOrderStatus.setText(AppController.getDnInfo().getOpStatus());

        if (AppController.getDnInfo().getOpStatus().toUpperCase().equals("HOLD")) {
            mOrderStatus.setTextColor(Color.RED);
        } else {
            mOrderStatus.setTextColor(Color.BLUE);
        }

        mPartNo.setText(getString(R.string.label_part_no_2, itemInfo.getItemID()));
        mCheckingQty.setText(getResources().getString(R.string.label_picking_qty, itemInfo.getQty(), itemInfo.getPass(), itemInfo.getWait()));

        btnReturn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Return", String.valueOf(v.getId()));
                returnPage();
            }

        });

        listView = findViewById(R.id.listView);
        listView.setVisibility(View.GONE);

        btnYes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Yes", String.valueOf(v.getId()));
                toNextPage();
            }

        });

        btnNo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick No", String.valueOf(v.getId()));
                returnPage();
            }

        });

        rbGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));

                switch (checkedId) {
                    case R.id.radio_none_po:
                        Log.d("onClick Radio None Po", String.valueOf(checkedId));
                        listView.setVisibility(View.GONE);
                        btnYes.setVisibility(View.VISIBLE);
                        btnNo.setVisibility(View.VISIBLE);
                        break;
                    case R.id.radio_po:
                        Log.d("onClick Po", String.valueOf(checkedId));
                        btnYes.setVisibility(View.GONE);
                        btnNo.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                        queryOePo();
                        break;
                }
            }
        });

        lblTitle = findViewById(R.id.ap_title);
        setTitle();
        rbNoPo.setChecked(true);
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.label_picking, AppController.getOrgName()));
        text.setSpan(new RelativeSizeSpan(1f), 7, 7 + AppController.getOrgName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private String getCustomer() {
        return AppController.getDnInfo().getCustomer().split(" ")[0].toUpperCase();
    }

    private void queryOePo() {
        itemPoInfoHelper = new ItemPoInfoHelper();
        itemPoInfoHelper.setDeliveryID(AppController.getDnInfo().getDeliveryID());
        itemPoInfoHelper.setId(itemInfo.getId());
        itemPoInfoHelper.setItemID(itemInfo.getItemID());
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetOePoInfo().execute(0);
    }

    private void toNextPage() {
        Intent intent = new Intent(getBaseContext(), ShipmentPickingActivity.class);
        intent.putExtra("ITEM_INFO", new Gson().toJson(itemInfo));
        intent.putExtra("PACK_QTY", intPackQty);
        startActivityForResult(intent, 0);
    }

    private void returnPage() {
        finish();
    }

    public void onBackPressed() {
        // do something here and don't write super.onBackPressed()
        returnPage();
    }

    public void setListData() {
        listView.setVisibility(View.VISIBLE);
        list.clear();

        if (itemPoInfoHelper.getOeInfos() != null) {
            int qty = 0;
            int pass = 0;
            int wait = 0;

            for (int i = 0; i < itemPoInfoHelper.getOeInfos().length; i++) {
                OeInfo oeInfo = itemPoInfoHelper.getOeInfos()[i];
                qty += oeInfo.getQty();
                pass += oeInfo.getPass();
                wait += oeInfo.getWait();
                HashMap<String, String> item = new HashMap<>();
                item.put("oe_po", getString(R.string.label_oe_po, oeInfo.getOe(), oeInfo.getPo()));
                item.put("pick_qty", getString(R.string.label_pick_qty, String.valueOf(oeInfo.getQty()), String.valueOf(oeInfo.getPass()), String.valueOf(oeInfo.getWait())));
                list.add(item);
            }

            itemInfo.setQty(qty);
            itemInfo.setPass(pass);
            itemInfo.setWait(wait);
            mCheckingQty.setText(getResources().getString(R.string.label_picking_qty, itemInfo.getQty(), itemInfo.getPass(), itemInfo.getWait()));
        }

        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.shipment_po_item,
                new String[]{"oe_po", "pick_qty"},
                new int[]{R.id.txt_oe_po, R.id.txt_pick_qty}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                OeInfo item = itemPoInfoHelper.getOeInfos()[position];

                if (String.valueOf(item.getWait()).equals("0")) {
                    view.setBackgroundColor(Color.GRAY);
                    view.setClickable(false);
                } else {
                    view.setBackgroundDrawable(getResources().getDrawable(R.drawable.custom_item));
                }

                return view;
            }
        };

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                int oriPosition = list.indexOf(parent.getItemAtPosition(position));
                AppController.debug("onClick itemInfo " + oriPosition + " OE=" + itemPoInfoHelper.getOeInfos()[oriPosition].getOe());

                if (itemPoInfoHelper.getOeInfos()[oriPosition].getWait() > 0) {
                    Intent intent = new Intent(getBaseContext(), ShipmentPickingActivity.class);
                    intent.putExtra("OE_INFO",
                            new Gson().toJson(itemPoInfoHelper.getOeInfos()[oriPosition]));
                    intent.putExtra("ITEM_INFO", new Gson().toJson(itemInfo));
                    intent.putExtra("PACK_QTY", intPackQty);
                    startActivityForResult(intent, 0);
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
                            deletePacking();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            // No button clicked
                            break;
                    }
                }
            };

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int oriPosition = list.indexOf(parent.getItemAtPosition(position));
                AppController.debug("onLongClick itemInfo " + oriPosition + " ID=" + itemPoInfoHelper.getOeInfos()[oriPosition].getOe());

                if (itemPoInfoHelper.getOeInfos()[oriPosition].getPass() > 0) {
                    selectedOeInfo = itemPoInfoHelper.getOeInfos()[oriPosition];
                    AlertDialog.Builder builder = new AlertDialog.Builder(ShipmentPoActivity.this);
                    builder.setTitle( getString(R.string.btn_ok)).setMessage(R.string.del_oe)
                            .setPositiveButton(getString(R.string.yes), dialogClickListener)
                            .setNegativeButton(getString(R.string.no), dialogClickListener).show();
                }

                return true;
            }
        });
    }

    private void deletePacking() {
        delPackingInfo = new DeliveryInfoHelper();
        delPackingInfo.setDeliveryID(itemPoInfoHelper.getDeliveryID());
        delPackingInfo.setSelectedItemId(itemPoInfoHelper.getId());
        delPackingInfo.setSelectedItemNo(itemPoInfoHelper.getItemID());
        delPackingInfo.setSelectedOeId(selectedOeInfo.getId());
        delPackingInfo.setCustomer(AppController.getDnInfo().getCustomer());
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.del_data), true);
        new DeletePackingInfo().execute(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK || resultCode == RESULT_FIRST_USER) {
            if (rbPo.isChecked()) {
                selectedOeInfo = new Gson().fromJson(data.getStringExtra("OE_INFO"), OeInfo.class);

                for (OeInfo item : itemPoInfoHelper.getOeInfos()) {
                    if (item.getId() == selectedOeInfo.getId() && item.getPo() == selectedOeInfo.getPo()) {
                        item.setQty(selectedOeInfo.getQty());
                        item.setPass(selectedOeInfo.getPass());
                        item.setWait(selectedOeInfo.getWait());
                        break;
                    }
                }

                setListData();
            } else {
                itemInfo = new Gson().fromJson(data.getStringExtra("ITEM_INFO"), ItemInfoHelper.class);
                mCheckingQty.setText(getResources().getString(R.string.label_picking_qty, itemInfo.getQty(), itemInfo.getPass(), itemInfo.getWait()));
            }

            setResult(RESULT_OK);

            if (itemInfo.getWait() == 0) {
                finish();
            }
        }
    }

    private class GetOePoInfo extends AsyncTask<Integer, String, ItemPoInfoHelper> {
        @Override
        protected ItemPoInfoHelper doInBackground(Integer... params) {
            AppController.debug("Get OE PO Info from " + AppController.getServerInfo()
                    + AppController.getProperties("GetOePoInfo"));
            publishProgress(getString(R.string.downloading_data));
            return shipment.getOePoInfo(itemPoInfoHelper);
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
        protected void onPostExecute(ItemPoInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    itemPoInfoHelper = result;
                    setListData();
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

    private class DeletePackingInfo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Delete PackingInfo to " + AppController.getServerInfo()
                    + AppController.getProperties("DelPackingInfo"));
            publishProgress(getString(R.string.del_data));
            return shipment.delPackingInfo(delPackingInfo);
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
                    errorInfo = "";
                    mConnection.setText(result.getStrErrorBuf());
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    dialog.dismiss();
                    setResult(RESULT_OK);
                    queryOePo();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    dialog.dismiss();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                dialog.dismiss();
            }
        }
    }
}
