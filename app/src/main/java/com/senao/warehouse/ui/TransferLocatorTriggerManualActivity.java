package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.asynctask.AsyncResponse;
import com.senao.warehouse.asynctask.GetItemOnHandInventoryList;
import com.senao.warehouse.asynctask.ProcessTempTransferInfo;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.TempTransferProcessInfoHelper;
import com.senao.warehouse.database.TransferDateCodeInfo;
import com.senao.warehouse.database.TransferItemInfoHelper;
import com.senao.warehouse.database.TransferLocatorInfoHelper;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.ReturnCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TransferLocatorTriggerManualActivity extends Activity implements View.OnClickListener {

    private static final String TAG = TransferLocatorTriggerManualActivity.class.getSimpleName();
    private static final int REQUEST_DETAIL = 1;
    private EditText txtImportPartNo;
    private TextView mConnection, labelItemDescription, labelItemInventorySummary;
    private String errorInfo = "";
    private List<HashMap<String, Object>> data = new ArrayList<>();
    private MyAdapter mAdapter;
    private TransferItemInfoHelper transferItemInfoHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_locator_trigger_manual);

        TextView lblTitle = findViewById(R.id.ap_title);
        final SpannableString text;
        String subtitle = getString(R.string.label_manual_trigger);
        String title = getString(R.string.label_transfer_locator_trigger_main, subtitle);
        text = new SpannableString(title);
        text.setSpan(new RelativeSizeSpan(1.0f), 0, title.length() - subtitle.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new RelativeSizeSpan(0.8f), title.length() - subtitle.length(), title.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);

        Button btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        txtImportPartNo = findViewById(R.id.edittext_import_part_number);
        txtImportPartNo.setSelectAllOnFocus(true);
        txtImportPartNo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_UP)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down" + String.valueOf(v.getId()));
                    hideKeyboard();
                    String partNo = parsePartNo(txtImportPartNo.getText().toString().trim());
                    if (TextUtils.isEmpty(partNo)) {
                        Toast.makeText(getApplicationContext(), getString(R.string.enter_sku), Toast.LENGTH_SHORT).show();//"請先輸入料號"
                    } else {
                        txtImportPartNo.setText(partNo);
                        txtImportPartNo.setSelection(txtImportPartNo.getText().length());
                        queryItemOnHandInventoryList(partNo);
                    }
                    return true;
                }
                return false;
            }
        });

        txtImportPartNo.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    clearCondition();
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

        labelItemDescription = findViewById(R.id.label_item_description);
        labelItemInventorySummary = findViewById(R.id.label_item_inventory_summary);

        ListView listView = findViewById(R.id.listView);
        mAdapter = new MyAdapter(this);
        listView.setAdapter(mAdapter);

        Button btnCancel = findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(this);

        Button btnDetail = findViewById(R.id.button_detail);
        btnDetail.setOnClickListener(this);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(this);

        labelItemDescription.setText(getString(R.string.label_item_description_2, ""));
        labelItemInventorySummary.setText(getString(R.string.label_item_inventory_summary, 0, 0));
    }

    private String parsePartNo(String trim) {
        if (trim.length() > 12) {
            return trim.substring(0, 12);
        } else {
            return trim;
        }
    }

    private void queryItemOnHandInventoryList(String partNo) {
        transferItemInfoHelper = new TransferItemInfoHelper();
        transferItemInfoHelper.setPartNo(partNo);
        GetItemOnHandInventoryList task = new GetItemOnHandInventoryList(this, new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                transferItemInfoHelper = (TransferItemInfoHelper) result;
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
                setListData(transferItemInfoHelper);
                hideKeyboard();
            }

            @Override
            public void onError(BasicHelper result) {
                if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                    mConnection.setText(getString(R.string.connect_error));
                } else {
                    mConnection.setText(getString(R.string.db_return_error));
                }
                errorInfo = result.getStrErrorBuf();
                mConnection.setTextColor(Color.RED);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                setListData(null);
            }

            @Override
            public void onFailure() {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        });
        task.execute(transferItemInfoHelper);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_return) {
            returnPage();
        } else if (id == R.id.button_cancel) {
            clearCondition();
        } else if (id == R.id.button_detail) {
            goToDetail();
        } else if (id == R.id.label_status) {
            showErrorMsg();
        }
    }

    private Dialog onCreateTransferDialog(final String partNo, final String itemDescription, final TransferLocatorInfoHelper locatorInfoHelper, final String dc) {
        int index = 0;
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_transfer, null);
        final TextView tvPartNo = item.findViewById(R.id.tv_part_no);
        final TextView tvPartDesc = item.findViewById(R.id.tv_part_desc);
        final TextView tvSubinventory = item.findViewById(R.id.tv_subinventory);
        final TextView tvLocator = item.findViewById(R.id.tv_locator);
        final TextView tvOnHandDc = item.findViewById(R.id.tv_on_hand_dc);
        final TextView tvOnHandQty = item.findViewById(R.id.tv_on_hand_qty);
        final EditText etTransferQty = item.findViewById(R.id.edittext_import_transfer_qty);
        for (int i = 0; i < locatorInfoHelper.getDateCodeInfo().length; i++) {
            if (locatorInfoHelper.getDateCodeInfo()[i].getDateCode().equals(dc)) {
                index = i;
                break;
            }
        }
        tvPartNo.setText(partNo);
        tvPartDesc.setText(itemDescription);
        tvSubinventory.setText(locatorInfoHelper.getSubinventory());
        tvLocator.setText(locatorInfoHelper.getLocator());
        tvOnHandDc.setText(locatorInfoHelper.getDateCodeInfo()[index].getDateCode());
        tvOnHandQty.setText(String.valueOf(Math.round(locatorInfoHelper.getDateCodeInfo()[index].getQty().doubleValue())));
        if ((int) Math.round(locatorInfoHelper.getDateCodeInfo()[index].getPass().doubleValue()) > 0) {
            etTransferQty.setText(String.valueOf(Math.round(locatorInfoHelper.getDateCodeInfo()[index].getPass().doubleValue())));
        } else {
            etTransferQty.setText(String.valueOf(Math.round(locatorInfoHelper.getDateCodeInfo()[index].getQty().doubleValue())));
        }
        etTransferQty.setSelectAllOnFocus(true);
        etTransferQty.requestFocus();
        final int finalIndex = index;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle(R.string.label_transfer_data).setView(item).setCancelable(false);
        builder.setPositiveButton(getString(R.string.button_confirm_setting), null);
        builder.setNegativeButton(R.string.choose_sku, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hideKeyboard();
            }
        });
        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        hideKeyboard();
                        if (TextUtils.isEmpty(etTransferQty.getText().toString())) {
                            Toast.makeText(getApplicationContext(), R.string.msg_input_transfer_qty, Toast.LENGTH_LONG).show();
                            etTransferQty.requestFocus();
                            return;
                        }
                        int transferQty;
                        try {
                            transferQty = Integer.parseInt(etTransferQty.getText().toString());
                            if (transferQty > locatorInfoHelper.getDateCodeInfo()[finalIndex].getQty().doubleValue()) {
                                Toast.makeText(getApplicationContext(), R.string.msg_input_transfer_qty_larger_than_original, Toast.LENGTH_LONG).show();
                                etTransferQty.requestFocus();
                                return;
                            }
                            if (transferQty <= 0) {
                                Toast.makeText(getApplicationContext(), R.string.msg_input_transfer_qty_larger_than_one, Toast.LENGTH_LONG).show();
                                etTransferQty.requestFocus();
                                return;
                            }
                            locatorInfoHelper.getDateCodeInfo()[finalIndex].setPass(BigDecimal.valueOf(Double.parseDouble(etTransferQty.getText().toString())));
                            hideKeyboard();
                            dialog.dismiss();
                            processTransfer(partNo, itemDescription, locatorInfoHelper.getSubinventory(), locatorInfoHelper.getLocator(), locatorInfoHelper.getDateCodeInfo()[finalIndex].getDateCode(), Double.parseDouble(etTransferQty.getText().toString()));
                        } catch (Exception ex) {
                            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        return dialog;
    }

    private void processTransfer(String partNo, String itemDescription, String subinventory, String locator, String datecode, double transferQty) {
        TempTransferProcessInfoHelper helper = new TempTransferProcessInfoHelper();
        helper.setPartNo(partNo);
        helper.setItemDescription(itemDescription);
        helper.setSubinventory(subinventory);
        helper.setLocator(locator);
        //helper.setTransSubinventory("307");
        //helper.setTransLocator("PFA17");
        //helper.setTransLocator("P023");
        helper.setDateCode(datecode);
        helper.setTransQty(BigDecimal.valueOf(transferQty));
        helper.setUserID(AppController.getUser().getPassword());

        ProcessTempTransferInfo task = new ProcessTempTransferInfo(this, new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
                queryItemOnHandInventoryList(txtImportPartNo.getText().toString());
            }

            @Override
            public void onError(BasicHelper result) {
                if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                    mConnection.setText(getString(R.string.connect_error));
                } else {
                    mConnection.setText(getString(R.string.db_return_error));
                }
                errorInfo = result.getStrErrorBuf();
                mConnection.setTextColor(Color.RED);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            }

            @Override
            public void onFailure() {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        });
        task.execute(helper);
    }

    private void setListData(final TransferItemInfoHelper tempInfo) {
        data.clear();

        if (tempInfo == null) {
            labelItemDescription.setText(getString(R.string.label_item_description_2, ""));
            labelItemInventorySummary.setText(getString(R.string.label_item_inventory_summary, 0, 0));
        } else {
            labelItemDescription.setText(getString(R.string.label_item_description_2, tempInfo.getItemDescription()));
            labelItemInventorySummary.setText(getString(R.string.label_item_inventory_summary, tempInfo.getQty() == null ? 0 : Math.round(tempInfo.getQty().doubleValue()), tempInfo.getTransQty() == null ? 0 : Math.round(tempInfo.getTransQty().doubleValue())));
            if (tempInfo.getLocatorInfo() != null) {
                for (TransferLocatorInfoHelper locatorInfoHelper : tempInfo.getLocatorInfo()) {
                    HashMap<String, Object> item = new HashMap<>();
                    item.put("part_no", tempInfo.getPartNo());
                    item.put("item_desc", tempInfo.getItemDescription());
                    item.put("locator_info", locatorInfoHelper);
                    data.add(item);
                }
            }
        }

        mAdapter.notifyDataSetChanged();

        if (data.size() == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.no_data), Toast.LENGTH_LONG).show();
        }
    }

    private void showErrorMsg() {
        if (!TextUtils.isEmpty(errorInfo)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(
                    this);
            dialog.setTitle("Error Msg");
            dialog.setMessage(errorInfo);
            dialog.setIcon(android.R.drawable.ic_dialog_alert);
            dialog.setCancelable(false);
            dialog.setNegativeButton( getString(R.string.btn_ok), null);
            dialog.show();
        }
    }

    private void goToDetail() {
        Intent intent = new Intent(this, TransferLocatorTriggerManualDetailActivity.class);
        startActivityForResult(intent, REQUEST_DETAIL);
    }

    private void returnPage() {
        hideKeyboard();
        finish();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(txtImportPartNo.getWindowToken(), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (!this.data.isEmpty() && !TextUtils.isEmpty(txtImportPartNo.getText().toString().trim())) {
                queryItemOnHandInventoryList(txtImportPartNo.getText().toString().trim());
            }
        }
    }

    private void clearCondition() {
        if (!TextUtils.isEmpty(txtImportPartNo.getText())) {
            txtImportPartNo.setText("");
        }
        txtImportPartNo.requestFocus();
        data.clear();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        labelItemDescription.setText(getString(R.string.label_item_description_2, ""));
        labelItemInventorySummary.setText(getString(R.string.label_item_inventory_summary, 0, 0));
    }

    static class ViewHolder {
        public TextView txtSubinventory;
        public TextView txtLocator;
        public LinearLayout llDcList;
    }

    public class MyAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

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
                convertView = mInflater.inflate(R.layout.locator_inventory_item, null);
                holder.txtSubinventory = convertView.findViewById(R.id.txt_subinventory);
                holder.txtLocator = convertView.findViewById(R.id.txt_locator);
                holder.llDcList = convertView.findViewById(R.id.layout_dc);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            convertView.setVisibility(View.VISIBLE);
            TransferLocatorInfoHelper locatorInfo = (TransferLocatorInfoHelper) data.get(position).get("locator_info");
            String partNo = (String) data.get(position).get("part_no");
            String itemDescription = (String) data.get(position).get("item_desc");
            if (locatorInfo == null) {
                holder.txtSubinventory.setText("");
                holder.txtLocator.setText("");
                holder.llDcList.removeAllViews();
            } else {
                holder.txtSubinventory.setText(locatorInfo.getSubinventory());
                holder.txtLocator.setText(locatorInfo.getLocator());
                addView(holder.llDcList, partNo, itemDescription, locatorInfo);
            }
            return convertView;
        }

        private void addView(LinearLayout v, final String partNo, final String itemDescription, final TransferLocatorInfoHelper locatorInfo) {
            v.removeAllViews();
            if (locatorInfo != null && locatorInfo.getDateCodeInfo() != null && locatorInfo.getDateCodeInfo().length > 0) {
                for (final TransferDateCodeInfo item : locatorInfo.getDateCodeInfo()) {
                    LinearLayout layoutContainer = (LinearLayout) mInflater.inflate(R.layout.locator_dc_item, null);
                    layoutContainer.setBackgroundDrawable(getResources().getDrawable(
                            R.drawable.custom_bg));
                    layoutContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (locatorInfo.getSubinventory().equals(Constant.SUB_307) &&
                                    !(locatorInfo.getLocator().equals(Constant.LOCATOR_P022)
                                            || locatorInfo.getLocator().equals(Constant.LOCATOR_P023)
                                            || (locatorInfo.getLocator().equals(Constant.LOCATOR_PFA18)))) {
                                onCreateTransferDialog(partNo, itemDescription, locatorInfo, item.getDateCode()).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "不可選此儲位", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    TextView tvOnHandQty = layoutContainer.findViewById(R.id.tv_on_hand_qty);
                    tvOnHandQty.setText(getString(R.string.label_dc_inventory, item.getDateCode(), (int) Math.round(item.getQty().doubleValue())));
                    TextView tvTransferQty = layoutContainer.findViewById(R.id.tv_transfer_qty);
                    tvTransferQty.setText(getString(R.string.tv_transfer_qty, String.valueOf(Math.round(item.getPass().doubleValue()))));
                    v.addView(layoutContainer);
                }
            }
        }

    }
}