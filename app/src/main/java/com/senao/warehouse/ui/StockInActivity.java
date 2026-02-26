package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.asynctask.AsyncResponse;
import com.senao.warehouse.asynctask.ClearStockInfo;
import com.senao.warehouse.asynctask.GetStockInApplyInfo;
import com.senao.warehouse.asynctask.StockIn;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.ItemInfoHelper;
import com.senao.warehouse.database.StockInInfoHelper;
import com.senao.warehouse.database.UserInfoHelper;
import com.senao.warehouse.util.ReturnCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockInActivity extends Activity implements OnClickListener {

    private static final String TAG = StockInActivity.class.getSimpleName();
    ArrayList<HashMap<String, Object>> list = new ArrayList<>();
    private SimpleAdapter adapter;
    private TextView mConnection, mEmployeeId, mAccount,lblTitle;
    private EditText txtInputSnItemNo;
    private Button btnStockIn;
    private ListView listItem;
    private UserInfoHelper user = AppController.getUser();
    private StockInInfoHelper stockInfo;
    private StockInInfoHelper tempStockInfo;
    private StockInInfoHelper selectedStockInfo;
    private ItemInfoHelper selectedItemInfoHelper;
    private String errorInfo = "";
    private boolean isInSearchMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_in);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(this);

        mEmployeeId = findViewById(R.id.textview_employee_id);
        mAccount = findViewById(R.id.textview_login_account);

        Button btnReturn = findViewById(R.id.button_return);
        btnStockIn = findViewById(R.id.button_stock_in);

        txtInputSnItemNo = findViewById(R.id.input_sn_item_no);
        txtInputSnItemNo.setFocusableInTouchMode(true);

        listItem = findViewById(R.id.list_item);

        mAccount.setText(user.getUserName());
        mEmployeeId.setText(String.valueOf(user.getPassword()));

        listItem.setVisibility(View.GONE);

        btnReturn.setOnClickListener(this);

        btnStockIn.setOnClickListener(this);

        txtInputSnItemNo.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down" + String.valueOf(v.getId()));

                    if (txtInputSnItemNo.getText().toString().trim().equals("")) {
                        Toast.makeText(StockInActivity.this, getString(R.string.query_cons_cant_be_null), Toast.LENGTH_SHORT).show();
                        txtInputSnItemNo.requestFocus();
                    } else {
                        isInSearchMode = true;
                        hideKeyboard();
                        txtInputSnItemNo.setText(parsePartNo(txtInputSnItemNo.getText().toString().trim()));
                        txtInputSnItemNo.setSelection(txtInputSnItemNo.getText().length());
                        getSearchData(txtInputSnItemNo.getText().toString().trim());
                    }

                    return true;
                }

                return false;
            }
        });

        txtInputSnItemNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (isInSearchMode && TextUtils.isEmpty(s)) {
                    isInSearchMode = false;
                    hideKeyboard();
                    refreshData();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

        });

        adapter = new SimpleAdapter(this, list, R.layout.stock_in_item,
                new String[]{"id", "quantity", "description", "control", "pass", "wait"},
                new int[]{R.id.txt_shipment_id, R.id.txt_shipment_quantity, R.id.txt_item_description,
                        R.id.txt_shipment_control, R.id.txt_stocking_pass,
                        R.id.txt_wait_for_stocking}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Map<String, Object> items = (HashMap<String, Object>) getItem(position);

                if (String.valueOf(items.get("wait")).equals("0")) {
                    view.setBackgroundColor(Color.GRAY);
                    view.setClickable(false);
                } else {
                    view.setBackgroundDrawable(getResources().getDrawable(R.drawable.custom_item));
                }

                return view;
            }
        };

        listItem.setAdapter(adapter);
        listItem.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ItemInfoHelper item = selectedStockInfo.getItemInfo()[position];
                AppController.debug("onClick item " + position + " ID=" + item.getId());

                if (item.getWait() > 0) {
                    Intent intent = new Intent(getBaseContext(),StockInProcessActivity.class);
                    intent.putExtra("ITEM_INFO", new Gson().toJson(item));
                    intent.putExtra("SEARCH_TEXT", selectedStockInfo.getSearchText());
                    startActivityForResult(intent, 0);
                }
            }
        });

        listItem.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                ItemInfoHelper item = selectedStockInfo.getItemInfo()[position];
                AppController.debug("onLongClick item " + position + " ID=" + item.getId());

                if (item.getPass() > 0) {
                    selectedItemInfoHelper = item;
                    showDeleteDialog();
                }

                return true;
            }
        });

        // btnStockIn.setVisibility(View.INVISIBLE);
        btnStockIn.setEnabled(false);
        lblTitle = findViewById(R.id.ap_title);
        setTitle();
        txtInputSnItemNo.requestFocus();
        doQueryStockInApplyInfo(null);
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.label_stock_in1, AppController.getOrgName()));
        text.setSpan(new RelativeSizeSpan(1f), 0, AppController.getOrgName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        text.setSpan(new ForegroundColorSpan(Color.parseColor("#fa160a")), 0, AppController.getOrgName().length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private String parsePartNo(String trim) {
        if (trim.length() >= 28) {
            return trim.substring(0, 12);
        } else {
            return trim;
        }
    }

    private void showStockInConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle( getString(R.string.btn_ok)).setMessage(getString(R.string.ru_sure_in_stock));
        builder.setPositiveButton(getString(R.string.yes),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                        doStockIn();
                    }
                });
        builder.setNegativeButton(getString(R.string.no),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                        btnStockIn.setEnabled(true);
                    }
                });

        builder.show();
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
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.dismiss();
                        }
                    });

            dialog.show();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtInputSnItemNo.getWindowToken(), 0);
    }

    private void getSearchData(String text) {
        if (stockInfo != null && stockInfo.getItemInfo() != null && stockInfo.getItemInfo().length > 0) {
            List<ItemInfoHelper> itemInfoList = new ArrayList<>();

            for (int i = 0; i < stockInfo.getItemInfo().length; i++) {
                ItemInfoHelper item = stockInfo.getItemInfo()[i];

                if (item.getItemID().contains(text)) {
                    item.setSearchText(text);
                    itemInfoList.add(item);
                }
            }

            if (itemInfoList.size() > 0) {
                tempStockInfo = new StockInInfoHelper();
                tempStockInfo.setItemInfo(itemInfoList);
                tempStockInfo.setSearchText(text);
                setListData(tempStockInfo);
            } else {
                doQueryStockInApplyInfo(text);
            }
        } else {
            doQueryStockInApplyInfo(text);
        }
    }

    private void setListData(final StockInInfoHelper infoHelper) {
        selectedStockInfo = infoHelper;
        listItem.setVisibility(View.VISIBLE);
        btnStockIn.setEnabled(false);
        list.clear();

        if (infoHelper != null && infoHelper.getItemInfo() != null) {
            for (int i = 0; i < infoHelper.getItemInfo().length; i++) {
                HashMap<String, Object> item = new HashMap<>();
                item.put("id", infoHelper.getItemInfo()[i].getItemID());
                item.put("quantity", infoHelper.getItemInfo()[i].getQty());
                item.put("description", infoHelper.getItemInfo()[i].getDescription());
                item.put("control", infoHelper.getItemInfo()[i].getControl());
                item.put("pass", infoHelper.getItemInfo()[i].getPass());
                item.put("wait", infoHelper.getItemInfo()[i].getWait());

                if (!btnStockIn.isEnabled()
                        && infoHelper.getItemInfo()[i].getPass() >= infoHelper.getItemInfo()[i].getQty()) {
                    btnStockIn.setEnabled(true);
                }

                list.add(item);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle( getString(R.string.btn_ok)).setMessage(R.string.Make_sure_delete_inbound_data);
        builder.setPositiveButton(getString(R.string.yes),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                        doClearStockInInfo();
                    }
                });
        builder.setNegativeButton(getString(R.string.no),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                    }
                });

        builder.show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button_return) {
            Log.d("onClick Return", String.valueOf(v.getId()));
            returnPage();
        } else if (id == R.id.label_status) {
            showStatus();
        } else if (id == R.id.button_stock_in) {
            Log.d("onClick Stock In", String.valueOf(v.getId()));
            btnStockIn.setEnabled(false);
            showStockInConfirmDialog();
        }
    }

    public void onBackPressed() {
        // do something here and don't write super.onBackPressed()
        Log.d(TAG, "onClick Hardware Back Button");
        returnPage();
    }

    private void returnPage() {
        finish();
    }

    private void refreshData() {
        if (isInSearchMode) {
            doQueryStockInApplyInfo(txtInputSnItemNo.getText().toString().trim());
        } else {
            doQueryStockInApplyInfo(null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            refreshData();
        }
    }

    private void doStockIn() {
        StockIn task = new StockIn(this, new AsyncResponse<BasicHelper>() { //call SSFI914P31(?,?,?,?,?)
            @Override
            public void onSuccess(BasicHelper result) {
                btnStockIn.setEnabled(true);
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
                refreshData();
            }

            @Override
            public void onFailure() {
                btnStockIn.setEnabled(true);
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }

            @Override
            public void onError(BasicHelper result) {
                btnStockIn.setEnabled(true);

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

        if (isInSearchMode) {
            task.execute(tempStockInfo);
        } else {
            task.execute(stockInfo);
        }
    }

    private void doQueryStockInApplyInfo(String searchText) {
        if (TextUtils.isEmpty(searchText)) {
            stockInfo = new StockInInfoHelper();
        } else {
            tempStockInfo = new StockInInfoHelper();
            tempStockInfo.setSearchText(searchText);
        }

        GetStockInApplyInfo task = new GetStockInApplyInfo(this, new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                tempStockInfo = (StockInInfoHelper) result;
                AppController.debug(tempStockInfo.toString());

                if (TextUtils.isEmpty(tempStockInfo.getSearchText())) {
                    stockInfo = tempStockInfo;
                }

                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
                setListData(tempStockInfo);
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
                setListData(tempStockInfo);
            }
        });

        if (isInSearchMode) {
            task.execute(tempStockInfo);
        } else {
            task.execute(stockInfo);
        }
    }

    private void doClearStockInInfo() {
        ClearStockInfo task = new ClearStockInfo(this, new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                errorInfo = "";
                mConnection.setText(result.getStrErrorBuf());
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                refreshData();
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

        task.execute(selectedItemInfoHelper);
    }
}
