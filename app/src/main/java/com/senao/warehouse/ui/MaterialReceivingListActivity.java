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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.RECEIVING_TYPE;
import com.senao.warehouse.database.ReceivingInfoHelper;
import com.senao.warehouse.database.UserInfoHelper;
import com.senao.warehouse.database.VendorInfoHelper;
import com.senao.warehouse.handler.ReceivingHandler;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MaterialReceivingListActivity extends Activity {

    private static final String TAG = MaterialReceivingListActivity.class.getSimpleName();
    private List<HashMap<String, Object>> data = new ArrayList<>();
    private TextView lblTitle, mConnection, txtVendorInfo, txtVendorSiteCode, lblRecId,org_txt;
    private EditText txtSearchText;
    private boolean isInSearchMode;
    private Button btnReturn, btnReceiving;
    private ListView listItem;
    private String errorInfo;
    private RECEIVING_TYPE receivingType;
    private VendorInfoHelper vendorInfo;
    private VendorInfoHelper tempVendorInfo;
    private ReceivingInfoHelper receivingInfo;
    private List<ReceivingInfoHelper> receivingInfoList;
    private UserInfoHelper user = AppController.getUser();
    private ProgressDialog dialog;
    private ReceivingHandler receivingHandler;
    private int accType;
    private CallBackListener callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_receiving_list);
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

        vendorInfo.setUserName(AppController.getUser().getUserName());
        lblTitle = findViewById(R.id.ap_title);
        accType = vendorInfo.isOutSourcing() ? 1 : 0;

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MaterialReceivingListActivity.this);
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

        btnReturn = findViewById(R.id.button_return);

        txtSearchText = findViewById(R.id.input_search_text);
        txtSearchText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down" + String.valueOf(v.getId()));

                    if (txtSearchText.getText().toString().trim().equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.query_cons_cant_be_null), Toast.LENGTH_SHORT).show();
                        txtSearchText.requestFocus();
                    } else {
                        isInSearchMode = true;
                        hideKeyboard();
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
                    //callback = null;
                    hideKeyboard();
                    clearStatus();
                    setListData(vendorInfo);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        txtVendorInfo = findViewById(R.id.txt_vendor_info);

        lblRecId = findViewById(R.id.label_rec_id);

        if (TextUtils.isEmpty(vendorInfo.getInvoiceSeq())) {
            lblRecId.setVisibility(View.GONE);
        } else {
            lblRecId.setText(getString(R.string.label_rec_id, vendorInfo.getInvoiceSeq()));
        }

        txtVendorSiteCode = findViewById(R.id.label_vendor_site_code);
        org_txt=findViewById(R.id.org_txt);
        btnReceiving = findViewById(R.id.button_receiving);

        if (receivingType.toString().contains("TEMP")) {
            btnReceiving.setVisibility(View.GONE);
        }

        btnReceiving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Receiving", String.valueOf(v.getId()));
                doReceiving();
            }
        });

        listItem = findViewById(R.id.list_item);

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Return", String.valueOf(v.getId()));
                returnPage();
            }
        });

        setORG();
        //txtVendorInfo.setText(getString(R.string.label_vendor_info,
        //        vendorInfo.getNum(), vendorInfo.getName()));
        txtVendorInfo.setText(vendorInfo.getName());

        setTitle();
        doGetReceivingList();
    }


    private void setORG() {
        if(Constant.ISORG && AppController.getOrgName()!=null){
            AppController.debug("setORG() true");
//            final SpannableString text;
//            text = new SpannableString(vendorInfo.getSiteCode()+" "+AppController.getOrgName());
//            text.setSpan(new RelativeSizeSpan(1f), vendorInfo.getSiteCode().length(), vendorInfo.getSiteCode().length() + AppController.getOrgName().length(),
//                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            text.setSpan(new ForegroundColorSpan(Color.parseColor("#fa160a")), vendorInfo.getSiteCode().length(), vendorInfo.getSiteCode().length()+1 + AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            txtVendorSiteCode.setText(text);
            org_txt.setText(AppController.getOrgName());
            txtVendorSiteCode.setText(vendorInfo.getSiteCode());
        }else{
            AppController.debug("setORG() false");
            txtVendorSiteCode.setText(vendorInfo.getSiteCode());
        }
    }

    private void clearStatus() {
        mConnection.setText("");
        mConnection.setTextColor(Color.WHITE);
        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        errorInfo = "";
    }

    private void getSearchData(String text) {
        if (vendorInfo != null && vendorInfo.getRecvList() != null && vendorInfo.getRecvList().length > 0) {
            List<ReceivingInfoHelper> itemInfoList = new ArrayList<>();
            boolean isPart = false;
            boolean isInvoice = false;
            int partId = 0;

            for (int i = 0; i < vendorInfo.getRecvList().length; i++) {
                if ((vendorInfo.getRecvList()[i].getPartNo() != null && Util.getSenaoPartNo(vendorInfo.getRecvList()[i].getPartNo()).equals(text))) {
                    isPart = true;
                    partId = vendorInfo.getRecvList()[i].getPartId();
                    itemInfoList.add(vendorInfo.getRecvList()[i]);
                } else if (vendorInfo.getRecvList()[i].getInvoiceNo() != null && vendorInfo.getRecvList()[i].getInvoiceNo().equals(text)) {
                    isInvoice = true;
                    itemInfoList.add(vendorInfo.getRecvList()[i]);
                } else if (vendorInfo.getRecvList()[i].getPo() != null && vendorInfo.getRecvList()[i].getPo().contains(text)) {
                    itemInfoList.add(vendorInfo.getRecvList()[i]);
                }
            }

            if (itemInfoList.size() > 0) {
                tempVendorInfo = new VendorInfoHelper();
                tempVendorInfo.setOutSourcing(vendorInfo.isOutSourcing());
                tempVendorInfo.setUserName(vendorInfo.getUserName());
                tempVendorInfo.setWareHouseNo(vendorInfo.getWareHouseNo());
                tempVendorInfo.setInvoiceNo(vendorInfo.getInvoiceNo());

                if (isPart) {
                    tempVendorInfo.setPartNo(text);
                    tempVendorInfo.setPartID(partId);
                } else if (isInvoice) {
                    tempVendorInfo.setInvoiceNo(text);
                }

                tempVendorInfo.setId(vendorInfo.getId());
                tempVendorInfo.setNum(vendorInfo.getNum());
                tempVendorInfo.setName(vendorInfo.getName());
                tempVendorInfo.setSiteID(vendorInfo.getSiteID());
                tempVendorInfo.setSiteCode(vendorInfo.getSiteCode());
                tempVendorInfo.setLocator(vendorInfo.getLocator());
                tempVendorInfo.setReceivingType(vendorInfo.getReceivingType());
                tempVendorInfo.setRecvList(itemInfoList);

                setListData(tempVendorInfo);
                return;
            }
        }

        setListData(null);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtSearchText.getWindowToken(), 0);
    }

    private void setTitle() {
        String subtitle = null;

        switch (receivingType) {
            case REC_COMBINE:
                subtitle = getString(R.string.combine) + " - " + getString(R.string.Receiving);
                break;
            case REC_INVOICE:
                subtitle = getString(R.string.invoice) + " - " + getString(R.string.Receiving);
                break;
            case REC_PO:
                subtitle = getString(R.string.po) + " - " + getString(R.string.Receiving);
                break;
            case TEMP_COMBINE:
                subtitle = getString(R.string.combine) + " - " + getString(R.string.Pending_inspection);
                break;
            case TEMP_INVOICE:
                subtitle = getString(R.string.invoice) + " - " + getString(R.string.Pending_inspection);;
                break;
            case TEMP_PO:
                subtitle = getString(R.string.po) + " - " + getString(R.string.Pending_inspection);
                break;
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
//            text = new SpannableString(getString(R.string.label_receiving_outsourcing, subtitle));
        } else {
            text = new SpannableString(getString(R.string.label_receiving1,AppController.getOrgName(), subtitle));
//            text = new SpannableString(getString(R.string.label_receiving, subtitle));
        }

        text.setSpan(new RelativeSizeSpan(1.0f), 0, 6,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new RelativeSizeSpan(0.7f), 7+AppController.getOrgName().length(), 7+AppController.getOrgName().length() + subtitle.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private void goToProcess(VendorInfoHelper info) {
        Intent intent = new Intent(this, MaterialReceivingProcessActivity.class);
        receivingInfo.setOutSourcing(accType == 1);
        receivingInfo.setReceivingType(receivingType.toString());
        intent.putExtra("VENDOR_INFO", new Gson().toJson(info.getImportInfo(info)));
        intent.putExtra("RECEIVING_INFO", new Gson().toJson(receivingInfo));
        startActivityForResult(intent, 0);
    }

    protected void doReceiving() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.retrive), true);
        new DoReceivingProcess().execute(0);
    }

    private void doGetReceivingList() {
        vendorInfo.setReceivingType(receivingType.toString());
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.downloading_data), true);
        new GetReceivingInfoList().execute(0);
    }

    private void queryData() {
        if (isInSearchMode) {
            callback = new CallBackListener() {
                @Override
                public void onEvent() {
                    getSearchData(txtSearchText.getText().toString().trim());
                }
            };
        } else {
            callback = null;
        }

        doGetReceivingList();
    }

    private void setListData(final VendorInfoHelper info) {
        if (info == null || info.getRecvList() == null) {
            receivingInfoList = new ArrayList<>();
        } else {
            receivingInfoList = Arrays.asList(info.getRecvList());
        }

        btnReceiving.setEnabled(false);
        data.clear();

        try {
            for (int i = 0; i < receivingInfoList.size(); i++) {
                HashMap<String, Object> item = new HashMap<>();
                item.put("invoice_no", receivingInfoList.get(i).getInvoiceNo());
                item.put("po", receivingInfoList.get(i).getPo());
                item.put("control", receivingInfoList.get(i).getControl());
                item.put("part_number", receivingInfoList.get(i).getPartNo());
                item.put("po_qty", Util.getDoubleValue(receivingInfoList.get(i).getPoQty()));
                item.put("deliverable_qty", Util.getDoubleValue(receivingInfoList.get(i).getDeliverableQty()));
                item.put("predeliver_qty", Util.getDoubleValue(receivingInfoList.get(i).getPredeliverQty()));
                item.put("received_qty", Util.getDoubleValue(receivingInfoList.get(i).getReceivedQty()));
                item.put("unreceived_qty", Util.getDoubleValue(receivingInfoList.get(i).getUnreceivedQty()));
                item.put("temp_qty", Util.getDoubleValue(receivingInfoList.get(i).getTempQty()));
                item.put("item_loc", receivingInfoList.get(i).getItemLoc());
                item.put("item_loc_desc", receivingInfoList.get(i).getItemLocDesc());

                if (!btnReceiving.isEnabled() && Util.getDoubleValue(receivingInfoList.get(i).getReceivedQty()) > 0) {
                    btnReceiving.setEnabled(true);
                }

                data.add(item);
            }
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }

        ListAdapter adapter = new MyAdapter(this);
        listItem.setAdapter(adapter);
        listItem.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                AppController.debug("onClick item " + position);
                receivingInfo = receivingInfoList.get(position);
                receivingInfo.setUserName(user.getUserName());
                receivingInfo.setWareHouseNo(user.getPassword());

                if (receivingType == RECEIVING_TYPE.REC_INVOICE || receivingType == RECEIVING_TYPE.TEMP_INVOICE) {
                    if (Util.getDoubleValue(receivingInfo.getPredeliverQty()) > 0) {
                        goToProcess(info);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.Insufficient_pre_delivery_qty), Toast.LENGTH_SHORT).show();
                    }
                } else {
//                    if (Util.getDoubleValue(receivingInfo.getDeliverableQty()) > 0) {
//                        goToProcess(info);
//                    }
                    if (!((Util.getDoubleValue(receivingInfo.getReceivedQty()) > 0 || (Util.getDoubleValue(receivingInfo.getTempQty()) > 0))
                            && Util.getDoubleValue(receivingInfo.getUnreceivedQty()) == 0)) {
                        goToProcess(info);
                    }
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
                            doClearReceivingInfo();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            // No button clicked
                            break;
                    }
                }
            };

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AppController.debug("onLongClick item " + position);
                receivingInfo = receivingInfoList.get(position);
                receivingInfo.setUserName(info.getUserName());
                receivingInfo.setWareHouseNo(info.getWareHouseNo());

                if ((receivingType.toString().contains("REC") && Util.getDoubleValue(receivingInfo.getReceivedQty()) > 0) ||
                        (receivingType.toString().contains("TEMP") && Util.getDoubleValue(receivingInfo.getTempQty()) > 0)) {
                    String message = getString(R.string.ru_del_data);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MaterialReceivingListActivity.this);
                    builder.setTitle( getString(R.string.btn_ok)).setMessage(message)
                            .setPositiveButton(getString(R.string.yes), dialogClickListener)
                            .setNegativeButton(getString(R.string.no), dialogClickListener).show();
                }

                return true;
            }
        });

        if (data.size() == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.no_data), Toast.LENGTH_LONG).show();
        }
    }

    private void doClearReceivingInfo() {
        receivingInfo.setReceivingType(receivingType.toString());
        receivingInfo.setOutSourcing(accType == 1);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.del_data), true);
        new ClearReceivingInfo().execute(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            queryData();
        }
    }

    private void returnPage() {
        finish();
    }

    public void onBackPressed() {
        returnPage();
    }

    interface CallBackListener {
        void onEvent();
    }

    static class ViewHolder {
        public TextView lblItemLoc;
        public TextView txtItemLoc;
        public TextView lblItemLocDesc;
        public TextView txtItemLocDesc;
        public TextView lblInvoiceNo;
        public TextView txtInvoiceNo;
        public TextView lblPoNo;
        public TextView txtPoNo;
        public TextView txtControl;
        public TextView txtPartNo;
        public TextView txtPartName;
        public TextView txtPoQty;
        public TextView txtDeliverableQty;
        public TextView txtPredeliverQty;
        public TextView txtReceivedQty;
        public TextView txtUnreceivedQty;
        public TextView txtTempQty;
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
                convertView = mInflater.inflate(R.layout.receiving_item, null);
                holder.lblItemLoc = convertView.findViewById(R.id.label_item_loc);
                holder.txtItemLoc = convertView.findViewById(R.id.txt_item_loc);
                holder.lblItemLocDesc = convertView.findViewById(R.id.label_item_loc_desc);
                holder.txtItemLocDesc = convertView.findViewById(R.id.txt_item_loc_desc);
                holder.lblInvoiceNo = convertView.findViewById(R.id.label_invoice_no);
                holder.txtInvoiceNo = convertView.findViewById(R.id.txt_invoice_no);
                holder.lblPoNo = convertView.findViewById(R.id.label_po);
                holder.txtPoNo = convertView.findViewById(R.id.txt_po);
                holder.txtControl = convertView.findViewById(R.id.txt_control);
                holder.txtPartNo = convertView.findViewById(R.id.txt_part_number);
                holder.txtPoQty = convertView.findViewById(R.id.txt_po_qty);
                holder.txtDeliverableQty = convertView.findViewById(R.id.txt_deliverable_qty);
                holder.txtPredeliverQty = convertView.findViewById(R.id.txt_predeliver_qty);
                holder.txtReceivedQty = convertView.findViewById(R.id.txt_received_qty);
                holder.txtUnreceivedQty = convertView.findViewById(R.id.txt_unreceived_qty);
                holder.txtTempQty = convertView.findViewById(R.id.txt_temp_qty);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            //convertView.setVisibility(View.VISIBLE);

            if ((receivingType == RECEIVING_TYPE.REC_INVOICE || receivingType == RECEIVING_TYPE.TEMP_INVOICE) &&
                    ((double) data.get(position).get("received_qty") > 0 || (double) data.get(position).get("temp_qty") > 0)
                    && (double) data.get(position).get("predeliver_qty") == 0) {
                convertView.setBackgroundColor(Color.GRAY);
            } else if ((double) data.get(position).get("po_qty") < (double) data.get(position).get("deliverable_qty")) {
                convertView.setBackgroundColor(Color.RED);
            } else if (((double) data.get(position).get("received_qty") > 0 || (double) data.get(position).get("temp_qty") > 0)
                    && (double) data.get(position).get("unreceived_qty") == 0) {
                convertView.setBackgroundColor(Color.GRAY);
                //convertView.setClickable(false);
            } else if ((double) data.get(position).get("unreceived_qty") < 0) {
                convertView.setBackgroundColor(Color.RED);
                //convertView.setClickable(false);
            } else {
                convertView.setBackgroundDrawable(getResources().getDrawable(R.drawable.custom_item));
                //convertView.setClickable(true);
            }

            if (receivingType == RECEIVING_TYPE.REC_COMBINE || receivingType == RECEIVING_TYPE.TEMP_COMBINE) {
                holder.lblInvoiceNo.setVisibility(View.GONE);
                holder.txtInvoiceNo.setVisibility(View.GONE);
                holder.lblPoNo.setVisibility(View.GONE);
                holder.txtPoNo.setVisibility(View.GONE);
            } else if (receivingType == RECEIVING_TYPE.REC_INVOICE || receivingType == RECEIVING_TYPE.TEMP_INVOICE) {
                holder.lblInvoiceNo.setVisibility(View.VISIBLE);
                holder.txtInvoiceNo.setVisibility(View.VISIBLE);
                holder.lblPoNo.setVisibility(View.GONE);
                holder.txtPoNo.setVisibility(View.GONE);
            } else {
                holder.lblInvoiceNo.setVisibility(View.VISIBLE);
                holder.txtInvoiceNo.setVisibility(View.VISIBLE);
                holder.lblPoNo.setVisibility(View.VISIBLE);
                holder.txtPoNo.setVisibility(View.VISIBLE);
            }

            holder.txtItemLoc.setText((String) data.get(position).get("item_loc"));
            holder.txtItemLocDesc.setText((String) data.get(position).get("item_loc_desc"));
            holder.txtInvoiceNo.setText((String) data.get(position).get("invoice_no"));
            holder.txtPoNo.setText((String) data.get(position).get("po"));
            holder.txtControl.setText((String) data.get(position).get("control"));
            holder.txtPartNo.setText((String) data.get(position).get("part_number"));
            holder.txtPoQty.setText(Util.fmt((double) data.get(position).get("po_qty")));
            holder.txtDeliverableQty.setText(Util.fmt((double) data.get(position).get("deliverable_qty")));
            holder.txtPredeliverQty.setText(Util.fmt((double) data.get(position).get("predeliver_qty")));
            holder.txtReceivedQty.setText(Util.fmt((double) data.get(position).get("received_qty")));
            holder.txtUnreceivedQty.setText(Util.fmt((double) data.get(position).get("unreceived_qty")));
            holder.txtTempQty.setText(Util.fmt((double) data.get(position).get("temp_qty")));
            return convertView;
        }
    }

    private class GetReceivingInfoList extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Get Receiving Info List  from " + AppController.getServerInfo()
                    + AppController.getProperties("GetReceivingInfoList"));
            publishProgress(getString(R.string.downloading_data));
            return receivingHandler.getReceivingInfoList(vendorInfo);
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

    private class ClearReceivingInfo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Clear Receiving Info from " + AppController.getServerInfo()
                    + AppController.getProperties("ClearReceivingInfo"));
            publishProgress(getString(R.string.del_data));
            return receivingHandler.doClearReceivingInfo(receivingInfo);
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

    private class DoReceivingProcess extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Do receiving process from " + AppController.getServerInfo()
                    + AppController.getProperties("DoReceivingProcess"));
            publishProgress(getString(R.string.Receiving_processing));

            if (isInSearchMode) {
                return receivingHandler.doReceivingProcess(tempVendorInfo);
            } else {
                vendorInfo.setPartID(0);
                return receivingHandler.doReceivingProcess(vendorInfo);
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
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
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
