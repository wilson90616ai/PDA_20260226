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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.AcceptanceConditionHelper;
import com.senao.warehouse.database.AcceptanceInfoHelper;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.DeliveryInfoHelper;
import com.senao.warehouse.database.ItemInfoHelper;
import com.senao.warehouse.database.UserInfoHelper;
import com.senao.warehouse.handler.AcceptanceHandler;
import com.senao.warehouse.handler.ShipmentHandler;
import com.senao.warehouse.util.ReturnCode;

import java.util.ArrayList;
import java.util.HashMap;

public class ShipmentActivity extends Activity implements OnClickListener {

    private static final String TAG = ShipmentActivity.class.getSimpleName();
    private ArrayList<HashMap<String, String>> list = new ArrayList<>();
    private SimpleAdapter adapter;
    private TextView mConnection, mEmployeeId, mCustomer, mPlanningShippingDate, mShippingStatus,lblTitle;
    private EditText txtDNNumber, txtItemNo;
    private Button btnReturn, btnDeleteDN;
    private ListView listItem;
    private UserInfoHelper user = AppController.getUser();
    private ShipmentHandler shipment;
    private DeliveryInfoHelper dnInfo;
    private DeliveryInfoHelper delPackingInfo;
    private int delItemInfoPosition;
    private boolean isItemDelete = false;
    private ProgressDialog dialog;
    private String errorInfo = "";
    private Button btnCheckFIFO;
    private LinearLayout llFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_picking_main);

        txtDNNumber = findViewById(R.id.input_data_DN);//DN號碼
        txtDNNumber.setText("");
        txtDNNumber.setSelectAllOnFocus(true);
        txtDNNumber.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down" + v.getId());
                    hideKeyboard(v);
                    String dn = txtDNNumber.getText().toString().trim();

                    if (TextUtils.isEmpty(dn)) {
                        Toast.makeText(ShipmentActivity.this, getString(R.string.dn_num_is_not_null),
                                Toast.LENGTH_SHORT).show();
                        return true;
                    } else {
                        String result = parseDn(dn);

                        if (TextUtils.isEmpty(result)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.cant_resolve_dn), Toast.LENGTH_LONG).show();
                            return true;
                        } else {
                            txtDNNumber.setText(result);
                            doQueryDN();
                        }
                    }
                }

                return false;
            }
        });

        lblTitle = findViewById(R.id.ap_title);
        setTitle();
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String stDn = extras.getString("ERRORDN");
            txtDNNumber.setText(stDn);
            doQueryDN();
        }

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(
                            ShipmentActivity.this);
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

        mEmployeeId = findViewById(R.id.textview_employee_id);
        mCustomer = findViewById(R.id.textview_customer);
        mPlanningShippingDate = findViewById(R.id.textview_planning_shipping_date);
        mShippingStatus = findViewById(R.id.textview_order_status);
        llFilter = findViewById(R.id.llFilter);
        btnReturn = findViewById(R.id.button_return);
        btnDeleteDN = findViewById(R.id.button_delete_dn);
        btnCheckFIFO = findViewById(R.id.button_check_fifo);
        txtItemNo = findViewById(R.id.input_item_no);

        txtItemNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                if (adapter != null) {
                    adapter.getFilter().filter(cs);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        listItem = findViewById(R.id.list_item);
        mEmployeeId.setText(user.getUserName());
        mCustomer.setText("");
        mPlanningShippingDate.setText("");
        mShippingStatus.setText("");
        llFilter.setVisibility(View.GONE);
        listItem.setVisibility(View.GONE);
        btnReturn.setOnClickListener(this);

        btnDeleteDN.setOnClickListener(new View.OnClickListener() {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            // Yes button clicked
                            setDeletePacking();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            // No button clicked
                            break;
                    }
                }
            };

            @Override
            public void onClick(View v) {
                Log.d("onClick Delete DN", String.valueOf(v.getId()));
                isItemDelete = false;
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        ShipmentActivity.this);
                builder.setTitle( getString(R.string.btn_ok)).setMessage(R.string.del_dn)
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show();
            }
        });

        btnDeleteDN.setVisibility(View.INVISIBLE);
        btnCheckFIFO.setOnClickListener(this);
        btnCheckFIFO.setVisibility(View.INVISIBLE);

        if (AppController.getDnInfo() != null) {
            this.dnInfo = AppController.getDnInfo();
            txtDNNumber.setText(String.valueOf(dnInfo.getDeliveryID()));
            doQueryDN();
        }
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.label_picking, AppController.getOrgName()));
        text.setSpan(new RelativeSizeSpan(1f), 0, AppController.getOrgName().length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        text.setSpan(new ForegroundColorSpan(Color.parseColor("#fa160a")), 0, AppController.getOrgName().length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private void hideKeyboard(View edit) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
    }

    private String parseDn(String dn) {
        String result = null;

        if (!TextUtils.isEmpty(dn)) {
            if (dn.contains("@")) {
                String[] list = dn.split("@");

                if (list.length > 1) {
                    result = list[1];
                }
            } else {
                result = dn;
            }
        }

        return result;
    }

    public void setListData() {
        llFilter.setVisibility(View.VISIBLE);
        listItem.setVisibility(View.VISIBLE);
        txtDNNumber.setText(String.valueOf(dnInfo.getDeliveryID()));
        mCustomer.setText(dnInfo.getCustomer());
        mPlanningShippingDate.setText(dnInfo.getShipDate());
        mShippingStatus.setText(dnInfo.getOpStatus());

        if (dnInfo.getOpStatus().toUpperCase().equals("HOLD")) {
            mShippingStatus.setTextColor(Color.RED);
        } else {
            mShippingStatus.setTextColor(Color.BLUE);
        }

        btnDeleteDN.setVisibility(View.VISIBLE);
        btnCheckFIFO.setVisibility(View.VISIBLE);
        txtDNNumber.setEnabled(false);
        list.clear();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtDNNumber.getWindowToken(), 0);

        if (dnInfo.getItemInfo() != null) {
            for (int i = 0; i < dnInfo.getItemInfo().length; i++) {
                HashMap<String, String> item = new HashMap<>();
                item.put("id", dnInfo.getItemInfo()[i].getItemID());
                item.put("quantity", String.valueOf(dnInfo.getItemInfo()[i].getQty()));
                item.put("description", dnInfo.getItemInfo()[i].getDescription());
                item.put("control", dnInfo.getItemInfo()[i].getControl());
                item.put("pass", String.valueOf(dnInfo.getItemInfo()[i].getPass()));
                item.put("wait", String.valueOf(dnInfo.getItemInfo()[i].getWait()));
                list.add(item);
            }
        }

        adapter = new SimpleAdapter(this, list, R.layout.shipment_item,
                new String[]{"id", "quantity", "description", "control", "pass", "wait"},
                new int[]{R.id.txt_shipment_id, R.id.txt_shipment_quantity, R.id.txt_item_description,
                        R.id.txt_shipment_control, R.id.txt_checking_pass,
                        R.id.txt_wait_for_checking}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                HashMap<String, String> items = (HashMap<String, String>) getItem(position);

                if (String.valueOf(items.get("wait")).equals("0")) {
                    view.setBackgroundColor(Color.GRAY);
                    view.setClickable(false);
                } else if(String.valueOf(items.get("qty")).equals("0")){//20220701
                    view.setBackgroundColor(Color.RED);
//                    view.setClickable(false);
                } else {
                    view.setBackgroundDrawable(getResources().getDrawable(
                            R.drawable.custom_item));
                }

                return view;
            }
        };

        listItem.setAdapter(adapter);
        listItem.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                int oriPosition = list.indexOf(parent.getItemAtPosition(position));
                AppController.debug("onClick item " + oriPosition + " ID="  + dnInfo.getItemInfo()[oriPosition].getId());
                AppController.debug("onClick item position ：" + position );
//                AppController.debug("parent.getItemAtPosition(position) ：" + parent.getItemAtPosition(position) );//{id=1102A1076301, control=SN, quantity=24, wait=0, description=FAP-222C (EU) PRODUCT LFP, pass=24}
                AppController.debug("dnInfo.getItemInfo()[oriPosition].getWait() ：" + dnInfo.getItemInfo()[oriPosition].getWait() );//{id=1102A1076301, control=SN, quantity=24, wait=0, description=FAP-222C (EU) PRODUCT LFP, pass=24}

                //可能是未檢數量
                if (dnInfo.getItemInfo()[oriPosition].getWait() > 0) {
                    Intent intent = new Intent(getBaseContext(), ShipmentCartonActivity.class);
                    AppController.setDnInfo(dnInfo);

                    intent.putExtra("ITEM_INFO", new Gson().toJson(dnInfo.getItemInfo()[oriPosition]));
                    startActivityForResult(intent, 0);
                }
            }
        });

        listItem.setOnItemLongClickListener(new OnItemLongClickListener() {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            // Yes button clicked
                            setDeletePacking();
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
                AppController.debug("onLongClick item " + oriPosition + " ID="
                        + dnInfo.getItemInfo()[oriPosition].getId());
                AppController.debug("dnInfo.getItemInfo()[oriPosition].getPass() = " + dnInfo.getItemInfo()[oriPosition].getPass());

                if (dnInfo.getItemInfo()[oriPosition].getPass() > 0) {
                    isItemDelete = true;
                    delItemInfoPosition = oriPosition;
                    AlertDialog.Builder builder = new AlertDialog.Builder(ShipmentActivity.this);
                    builder.setTitle( getString(R.string.btn_ok)).setMessage(R.string.Make_sure_to_del_SKU)
                            .setPositiveButton(getString(R.string.yes), dialogClickListener)
                            .setNegativeButton(getString(R.string.no), dialogClickListener).show();
                }

                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.shipment, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button_return) {
            Log.d("onClick Return", String.valueOf(v.getId()));

            if (txtDNNumber.isEnabled()) {
                finish();
            } else {
                setQueryView();
            }
        } else if (id == R.id.button_check_fifo) {
            Log.d("onClick Check FIFO", String.valueOf(v.getId()));
            doCheckOnHandInfo();
        }
    }

    private void doQueryDN() {
        dnInfo = new DeliveryInfoHelper();
        dnInfo.setDeliveryID(Integer.parseInt(txtDNNumber.getText().toString().trim()));

        shipment = new ShipmentHandler(dnInfo);
        new GetDNInfo().execute(0);
    }

    private void setQueryView() {
        llFilter.setVisibility(View.GONE);
        listItem.setVisibility(View.GONE);
        txtDNNumber.setEnabled(true);
        txtDNNumber.setText("");
        dnInfo = null;
        AppController.setDnInfo(null);
        mCustomer.setText("");
        mPlanningShippingDate.setText("");
        mShippingStatus.setText("");
        delPackingInfo = null;
        btnDeleteDN.setVisibility(View.INVISIBLE);
        btnCheckFIFO.setVisibility(View.INVISIBLE);
        txtDNNumber.requestFocus();
    }

    private void setDeletePacking() {
        AppController.debug("ShipmentActivity setDeletePacking()=>setCustomer() ");
        delPackingInfo = new DeliveryInfoHelper();
        delPackingInfo.setDeliveryID(dnInfo.getDeliveryID());

        if (isItemDelete) {
            ItemInfoHelper item = dnInfo.getItemInfo()[delItemInfoPosition];
            delPackingInfo.setSelectedItemId(item.getId());
            delPackingInfo.setSelectedItemNo(item.getItemID());
        }

        delPackingInfo.setCustomer(dnInfo.getCustomer());
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.del_data), true);
        new DeletePackingInfo().execute(0);
    }

    private void doCheckOnHandInfo() {
        if (dnInfo != null) {
            dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.first_in_first_out), true);
            new CheckOnHandInfo().execute(0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && AppController.getDnInfo() != null) {
            this.dnInfo = AppController.getDnInfo();
            txtDNNumber.setText(String.valueOf(dnInfo.getDeliveryID()));
            doQueryDN();
        }
    }

    private class GetDNInfo extends AsyncTask<Integer, String, DeliveryInfoHelper> {
        @Override
        protected DeliveryInfoHelper doInBackground(Integer... params) {
            AppController.debug("Get DeliveryInfo from " + AppController.getServerInfo()
                    + AppController.getProperties("GetDNInfo"));
            publishProgress(getString(R.string.downloading_data));
            return shipment.getDNInfo();
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
        protected void onPostExecute(DeliveryInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);

            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                dnInfo = result;
                if (dnInfo.getIntRetCode() == ReturnCode.OK) {
                    setListData();
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

                    errorInfo = dnInfo.getStrErrorBuf();
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
                    if (isItemDelete) {
                        doQueryDN();
                    } else {
                        setQueryView();
                    }

                    errorInfo = "";
                    mConnection.setText(result.getStrErrorBuf());
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    dialog.dismiss();
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

    private class CheckOnHandInfo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check OnHand Info to " + AppController.getServerInfo()
                    + AppController.getProperties("CheckOnHandInfo"));
            publishProgress(getString(R.string.first_in_first_out));
            return shipment.checkOnHandInfo();
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
            // execution of result of Long time consuming operation
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    errorInfo = result.getStrErrorBuf();
                    mConnection.setText(getString(R.string.first_in_first_out_ok));
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
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

                Toast.makeText(getApplicationContext(), errorInfo, Toast.LENGTH_LONG).show();
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }
}
