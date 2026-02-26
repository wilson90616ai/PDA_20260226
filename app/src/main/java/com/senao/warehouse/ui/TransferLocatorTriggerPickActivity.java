package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.adapter.ListTransferItemAdapter;
import com.senao.warehouse.asynctask.AsyncResponse;
import com.senao.warehouse.asynctask.GetOnHandInventoryList;
import com.senao.warehouse.asynctask.TriggerTransferLocator;
import com.senao.warehouse.print.BtPrintLabel;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.TransferInfoHelper;
import com.senao.warehouse.database.TransferItemInfoHelper;
import com.senao.warehouse.util.ReturnCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransferLocatorTriggerPickActivity extends Activity implements View.OnClickListener {

    private static final String TAG = TransferLocatorTriggerPickActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PRINTER_SETTING = 2;
    private static final int REQUEST_BLUETOOTH_SETTINGS = 3;
    private ListView listView;
    private List<TransferItemInfoHelper> dataList = new ArrayList<>();
    private EditText txtInputSearchText;
    private TextView mConnection, lblTotalQty, lblSelectedQty;
    private String errorInfo;
    private boolean isInSearchMode;
    private TransferInfoHelper transferInfoHelper;
    private TransferInfoHelper processTransferInfoHelper;
    private int selectedCNT = 0;
    private ListTransferItemAdapter mAdapter;
    private Button btnUnSel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_locator_trigger_pick);

        TextView lblTitle = findViewById(R.id.ap_title);
        final SpannableString text;
        String subtitle = getString(R.string.label_detail_pick);
        String title = getString(R.string.label_transfer_locator_trigger_main, subtitle);
        text = new SpannableString(title);
        text.setSpan(new RelativeSizeSpan(1.0f), 0, title.length() - subtitle.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new RelativeSizeSpan(0.8f), title.length() - subtitle.length(), title.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);

        Button btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        listView = findViewById(R.id.list_item);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setCheckedQtyTitle();
            }
        });

        mAdapter = new ListTransferItemAdapter(this, dataList);
        listView.setAdapter(mAdapter);

        Button btnAllSel = findViewById(R.id.btn_select_all);
        btnAllSel.setOnClickListener(this);
        btnUnSel = findViewById(R.id.btn_select_invert);
        btnUnSel.setOnClickListener(this);

        txtInputSearchText = findViewById(R.id.input_search_text);
        lblTotalQty = findViewById(R.id.label_total_qty);
        lblSelectedQty = findViewById(R.id.label_select_qty);

        Button btnTrigger = findViewById(R.id.button_trigger);
        btnTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedCNT > 150) {
                    Toast.makeText(getApplicationContext(), "不可超過150筆", Toast.LENGTH_LONG).show();
                } else if (selectedCNT > 0) {
                    Log.d("onClick Stock In", String.valueOf(v.getId()));
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            TransferLocatorTriggerPickActivity.this);
                    builder.setTitle( getString(R.string.btn_ok)).setMessage("確定觸發?")
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    triggerProcess();
                                }
                            })
                            .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).setCancelable(false).show();
                } else {
                    Toast.makeText(getApplicationContext(), "請先選擇要觸發的項目", Toast.LENGTH_LONG).show();
                }
            }

        });

        mConnection = findViewById(R.id.label_status);

        txtInputSearchText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_UP)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down" +
                            String.valueOf(v.getId()));

                    if (txtInputSearchText.getText().toString().trim().equals("")) {
                        Toast.makeText(TransferLocatorTriggerPickActivity.this,
                                getString(R.string.query_cons_cant_be_null), Toast.LENGTH_SHORT).show();
                        txtInputSearchText.requestFocus();
                    } else {
                        String partNo = parsePartNo(txtInputSearchText.getText().toString().trim());
                        txtInputSearchText.setText(partNo);
                        txtInputSearchText.setSelection(txtInputSearchText.getText().length());
                        isInSearchMode = true;
                        hideKeyboard();
                        getSearchData(partNo);
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
                    clearStatus();
                    setListData(transferInfoHelper);
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

        btnReturn.setOnClickListener(this);
        mConnection.setOnClickListener(this);

        lblTotalQty.setText(getString(R.string.label_total_qty, 0));
        lblSelectedQty.setText(getString(R.string.label_select_qty, 0));

        queryOnHandInventoryList();
    }

    private void checkPrinterSetting() {
        if (!BtPrintLabel.isPrintNameSet(this)) {
            Intent intent = new Intent(this, PrinterSettingActivity.class);
            startActivityForResult(intent, REQUEST_PRINTER_SETTING);
            Toast.makeText(getApplicationContext(), getString(R.string.set_printer_name), Toast.LENGTH_LONG).show();
            return;
        }

        if (!BtPrintLabel.isBtEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Toast.makeText(getApplicationContext(), getString(R.string.open_bt), Toast.LENGTH_LONG).show();
            return;
        }

        if (!BtPrintLabel.instance(getApplicationContext())) {
            Intent intentOpenBluetoothSettings = new Intent();
            intentOpenBluetoothSettings.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivityForResult(intentOpenBluetoothSettings, REQUEST_BLUETOOTH_SETTINGS);
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_printer), Toast.LENGTH_LONG).show();
            return;
        }

        if (BtPrintLabel.connect()) {
            Toast.makeText(getApplicationContext(), getString(R.string.bt_connect_ok), Toast.LENGTH_LONG).show();
            printLabel(processTransferInfoHelper.getFormSn(), processTransferInfoHelper.getProcessSn());
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.not_connect_btprinter), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ENABLE_BT || requestCode == REQUEST_PRINTER_SETTING) {
                checkPrinterSetting();
            }
        } else {
            if (requestCode == REQUEST_BLUETOOTH_SETTINGS) {
                if (BtPrintLabel.instance(getApplicationContext()) && BtPrintLabel.connect()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.bt_connect_ok), Toast.LENGTH_LONG).show();
                    printLabel(processTransferInfoHelper.getFormSn(), processTransferInfoHelper.getProcessSn());
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.not_connect_btprinter), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private String parsePartNo(String trim) {
        if (trim.length() > 12) {
            return trim.substring(0, 12);
        } else {
            return trim;
        }
    }

    private void setListData(final TransferInfoHelper tempInfo) {
        btnUnSel.callOnClick();
        listView.setVisibility(View.VISIBLE);
        dataList.clear();
        if (tempInfo != null && tempInfo.getInfoList() != null) {
            lblTotalQty.setText(getString(R.string.label_total_qty, tempInfo.getInfoList().length));
            dataList.addAll(Arrays.asList(tempInfo.getInfoList()));
        }

        lblTotalQty.setText(getString(R.string.label_total_qty, dataList.size()));
        setCheckedQtyTitle();
        mAdapter.notifyDataSetChanged();
        if (dataList.size() == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.no_data), Toast.LENGTH_LONG).show();
        }
    }

    private void clearStatus() {
        mConnection.setText("");
        mConnection.setTextColor(Color.WHITE);
        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        errorInfo = "";
    }

    private void getSearchData(String text) {
        if (transferInfoHelper != null && transferInfoHelper.getInfoList() != null
                && transferInfoHelper.getInfoList().length > 0) {
            List<TransferItemInfoHelper> itemInfoList = new ArrayList<>();
            for (TransferItemInfoHelper item : transferInfoHelper.getInfoList()) {
                if (item.getPartNo().contains(text)) {
                    itemInfoList.add(item);
                }
            }
            TransferInfoHelper tempTransferLocatorInfoHelper = new TransferInfoHelper();
            tempTransferLocatorInfoHelper.setSearchText(text);
            tempTransferLocatorInfoHelper.setInfoList(itemInfoList);
            setListData(tempTransferLocatorInfoHelper);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.no_data), Toast.LENGTH_LONG).show();
        }
    }

    private void setCheckedQtyTitle() {
        int count = 0;
        for (int i = 0; i < dataList.size(); i++) {
            if (listView.isItemChecked(i)) {
                count++;
            }
        }
        selectedCNT = count;
        lblSelectedQty.setText(getString(R.string.label_select_qty, count));
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtInputSearchText.getWindowToken(), 0);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_select_all) {
            for (int i = 0; i < dataList.size(); i++) {
                listView.setItemChecked(i, true);
            }
            setCheckedQtyTitle();
        } else if (id == R.id.btn_select_invert) {
            for (int i = 0; i < dataList.size(); i++) {
                listView.setItemChecked(i, false);
            }
            setCheckedQtyTitle();
        } else if (id == R.id.button_return) {
            finish();
        } else if (id == R.id.label_status) {
            showStatus();
        }
    }

    private void showStatus() {
        if (!TextUtils.isEmpty(errorInfo)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
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

    private void queryOnHandInventoryList() {
        transferInfoHelper = new TransferInfoHelper();
        GetOnHandInventoryList task = new GetOnHandInventoryList(this, new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                transferInfoHelper = (TransferInfoHelper) result;
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
                if (isInSearchMode) {
                    getSearchData(txtInputSearchText.getText().toString().trim());
                } else {
                    setListData(transferInfoHelper);
                }
            }

            @Override
            public void onFailure() {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                setListData(null);
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
                setListData(transferInfoHelper);
            }

        });
        task.execute(transferInfoHelper);
    }

    private void triggerProcess() {
        List<TransferItemInfoHelper> list = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            if (listView.isItemChecked(i)) {
                list.add(dataList.get(i));
            }
        }
        processTransferInfoHelper = new TransferInfoHelper();
        processTransferInfoHelper.setUserId(AppController.getUser().getPassword());
        processTransferInfoHelper.setInfoList(list);

        TriggerTransferLocator task = new TriggerTransferLocator(this, new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                processTransferInfoHelper = (TransferInfoHelper) result;
                clearStatus();
                queryOnHandInventoryList();
                checkPrinterSetting();
            }

            @Override
            public void onFailure() {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
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

        });
        task.execute(processTransferInfoHelper);
    }

    private void printLabel(String formSn, String processSn) {
        ProgressDialog dialog = ProgressDialog.show(this, getString(R.string.holdon),
                getString(R.string.printingLabel), true);
        errorInfo = "";
        if (!BtPrintLabel.printEzflowFormLabel(formSn, processSn)) {
            errorInfo = getString(R.string.printLabalFailed);
            mConnection.setText(getString(R.string.printer_connect_error));
            mConnection.setTextColor(Color.RED);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }
        dialog.dismiss();
    }
}