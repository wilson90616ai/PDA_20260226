package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.adapter.AdapterListener;
import com.senao.warehouse.adapter.ListTransferLocatorFormItemAdapter;
import com.senao.warehouse.asynctask.AsyncResponse;
import com.senao.warehouse.asynctask.CallWebApi;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.TransferFormInfoHelper;
import com.senao.warehouse.database.TransferLocatorFormInfoHelper;
import com.senao.warehouse.database.TransferLocatorFormItemInfoHelper;
import com.senao.warehouse.database.TransferLocatorFormProcessInfoHelper;
import com.senao.warehouse.util.ReturnCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//轉倉/呆料責任歸屬單
public class TransferLocatorFormListActivity extends Activity implements View.OnClickListener {

    private static final String TAG = TransferLocatorFormListActivity.class.getSimpleName();
    private String errorInfo = "";
    private static final int REQUEST_PROCESS = 1;
    private ListTransferLocatorFormItemAdapter mAdapter;
    private List<TransferLocatorFormInfoHelper> dataList = new ArrayList<>();
    private TransferFormInfoHelper conditionInfoHelper;
    private TextView mConnection;
    private EditText txtInputSearchText;
    private boolean isInSearchMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            conditionInfoHelper = new Gson().fromJson(extras.getString("CONDITION_INFO"),
                    TransferFormInfoHelper.class);
            if (conditionInfoHelper == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_condictions),
                        Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_condictions),
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_locator_form_list);
        Button btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(this);

        txtInputSearchText = findViewById(R.id.input_search_text);

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
                        Toast.makeText(TransferLocatorFormListActivity.this,
                                getString(R.string.query_cons_cant_be_null), Toast.LENGTH_SHORT).show();
                        txtInputSearchText.requestFocus();
                    } else {
                        isInSearchMode = true;
                        hideKeyboard();
                        getSearchData(txtInputSearchText.getText().toString().trim());
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
                    setListData(conditionInfoHelper);
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


        ListView listView = findViewById(R.id.list_item);
        mAdapter = new ListTransferLocatorFormItemAdapter(this, dataList, new AdapterListener() {
            @Override
            public void onCallBack(BasicHelper result) {
                TransferLocatorFormItemInfoHelper selectedItemInfo = (TransferLocatorFormItemInfoHelper) result;
                if (selectedItemInfo.getProcessType().equals("DEL")) {
                    deleteItemData(selectedItemInfo);
                } else {
                    goToProcess(selectedItemInfo);
                }
            }
        });
        listView.setAdapter(mAdapter);

        queryEzflowFormList();
    }

    private void getSearchData(String text) {
        if (conditionInfoHelper != null && conditionInfoHelper.getInfoList() != null
                && conditionInfoHelper.getInfoList().length > 0) {
            List<TransferLocatorFormInfoHelper> itemInfoList = new ArrayList<>();
            for (TransferLocatorFormInfoHelper item : conditionInfoHelper.getInfoList()) {
                if (item.getStatus().contains(text)) {
                    itemInfoList.add(item);
                } else {
                    for (TransferLocatorFormItemInfoHelper detailItem : item.getList()) {
                        if (detailItem.getWarehouseClerk().contains(text) || detailItem.getItemNo().contains(text)) {
                            itemInfoList.add(item);
                            break;
                        }
                    }
                }
            }
            TransferFormInfoHelper tempTransferLocatorInfoHelper = new TransferFormInfoHelper();
            tempTransferLocatorInfoHelper.setSearchText(text);
            tempTransferLocatorInfoHelper.setInfoList(itemInfoList);
            setListData(tempTransferLocatorInfoHelper);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.no_data), Toast.LENGTH_LONG).show();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtInputSearchText.getWindowToken(), 0);
    }

    private void goToProcess(TransferLocatorFormItemInfoHelper transferLocatorFormItemInfoHelper) {
        TransferLocatorFormProcessInfoHelper processInfoHelper = new TransferLocatorFormProcessInfoHelper();
        processInfoHelper.setFormSerialNumber(transferLocatorFormItemInfoHelper.getFormSerialNumber());
        processInfoHelper.setItemNo(transferLocatorFormItemInfoHelper.getItemNo());
        processInfoHelper.setControl(transferLocatorFormItemInfoHelper.getControl());
        processInfoHelper.setWarehouseClerk(transferLocatorFormItemInfoHelper.getWarehouseClerk());
        processInfoHelper.setItemDescription(transferLocatorFormItemInfoHelper.getItemDescription());
        processInfoHelper.setDueNumber(transferLocatorFormItemInfoHelper.getDueNumber());
        processInfoHelper.setInNumber(transferLocatorFormItemInfoHelper.getInNumber());
        processInfoHelper.setOutSubinventory(transferLocatorFormItemInfoHelper.getOutSubinventory());
        processInfoHelper.setOutLocator(transferLocatorFormItemInfoHelper.getOutLocator());
        Intent intent = new Intent(this, TransferLocatorFormProcessActivity.class);
        intent.putExtra("SELECTED_ITEM_INFO",
                new Gson().toJson(processInfoHelper));
        startActivityForResult(intent, REQUEST_PROCESS);
    }

    private void deleteItemData(TransferLocatorFormItemInfoHelper selectedItemInfo) {
        CallWebApi task = new CallWebApi(this, AppController.getProperties("DelItemDataFromP023"), new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
                queryEzflowFormList();
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
        task.execute(selectedItemInfo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            queryEzflowFormList();
        }
    }

    private void clearStatus() {
        mConnection.setText("");
        mConnection.setTextColor(Color.WHITE);
        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        errorInfo = "";
    }

    private void queryEzflowFormList() {
        CallWebApi task = new CallWebApi(this, AppController.getProperties("EzFlowDocumentList"), new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                conditionInfoHelper = (TransferFormInfoHelper) result;
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
                setListData(conditionInfoHelper);
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
                setListData(conditionInfoHelper);
            }

        });
        task.execute(conditionInfoHelper);
    }

    private void setListData(final TransferFormInfoHelper tempInfo) {
        dataList.clear();
        if (tempInfo != null && tempInfo.getInfoList() != null) {
            dataList.addAll(Arrays.asList(tempInfo.getInfoList()));
        }

        mAdapter.notifyDataSetChanged();
        if (dataList.size() == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.no_data), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_return) {
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
}