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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.R;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.RmaPalletInfoHelper;
import com.senao.warehouse.database.RmaPalletListHelper;
import com.senao.warehouse.handler.RmaHandler;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
//RMA出貨確認作業
public class RmaVerifyActivity extends Activity implements View.OnClickListener {

    private static final String TAG = RmaVerifyActivity.class.getSimpleName();
    private ListView listView;
    private TextView mConnection,lblTitle;
    private String errorInfo;
    private ProgressDialog dialog;
    private RmaPalletListHelper info;
    private RmaHandler handler;
    private RmaPalletInfoHelper selectedItemInfoHelper;
    private EditText txtPalletId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rma_verify);

        Button btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        txtPalletId = findViewById(R.id.edittext_import_pallet_id);
        txtPalletId.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down" + String.valueOf(v.getId()));

                    if (txtPalletId.getText().toString().trim().equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.enter_pallet_id), Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    insertPalletId();
                    return true;
                }

                return false;
            }
        });

        txtPalletId.requestFocus();
        Button btnInput = findViewById(R.id.button_input);
        btnInput.setOnClickListener(this);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(this);

        listView = findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final HashMap<String, String> item = (HashMap<String, String>) parent.getItemAtPosition(position);
                goToList(item.get("title"));
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            // Yes button clicked
                            doClearPalletInfo();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            // No button clicked
                            break;
                    }
                }
            };

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final HashMap<String, String> item = (HashMap<String, String>) parent.getItemAtPosition(position);
                selectedItemInfoHelper= new RmaPalletInfoHelper();
                selectedItemInfoHelper.setPalletId(item.get("title"));
                AppController.debug("onLongClick item " + position + " Pallet ID=" + selectedItemInfoHelper.getPalletId());
                AlertDialog.Builder builder = new AlertDialog.Builder(RmaVerifyActivity.this);
                String msg = getString(R.string.Make_sure_to_delete_the_pickup_data);
                builder.setTitle( getString(R.string.btn_ok)).setMessage(msg)
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show();
                return true;
            }
        });

        lblTitle = findViewById(R.id.ap_title);
        setTitle();
        handler = new RmaHandler();
        doQueryPalletList();
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.title_activity_rma_verify1, AppController.getOrgName()));
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private void insertPalletId() {
        if (txtPalletId.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_be_null), Toast.LENGTH_SHORT).show();
            txtPalletId.requestFocus();
        } else {
            hideKeyboard();
            doConfirmShipDate();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtPalletId.getWindowToken(), 0);
    }

    private void doClearPalletInfo() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.del_data), true);
        new ClearPalletInfo().execute(0);
    }

    private void doConfirmShipDate() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.processing), true);
        selectedItemInfoHelper = new RmaPalletInfoHelper();
        selectedItemInfoHelper.setPalletId(txtPalletId.getText().toString().trim());
        new ConfirmShipDate().execute(0);
    }

    private void goToList(String palletId) {
        Intent intent = new Intent(this, RmaListActivity.class);
        intent.putExtra("PALLET_ID", palletId);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.button_return) {
            finish();
        } else if (id == R.id.button_input) {
            insertPalletId();
        } else if (id == R.id.label_status) {
            if (!TextUtils.isEmpty(errorInfo)) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("Error Msg");
                dialog.setMessage(errorInfo);
                dialog.setIcon(android.R.drawable.ic_dialog_alert);
                dialog.setCancelable(false);
                dialog.setNegativeButton( getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                    }

                });

                dialog.show();
            }
        }
    }

    private void setListData(final RmaPalletListHelper tempInfo) {
        List<HashMap<String, String>> list = new ArrayList<>();

        if (tempInfo != null && tempInfo.getList() != null && tempInfo.getList().length > 0) {
            //使用List存入HashMap，用來顯示ListView上面的文字。
            for (int i = 0; i < tempInfo.getList().length; i++) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("title", tempInfo.getList()[i].getPalletId());
                hashMap.put("text", "CTN: " + tempInfo.getList()[i].getBoxQty() + "  P/NO: " + tempInfo.getList()[i].getPalletNo());
                //把title , text存入HashMap之中
                list.add(hashMap);
                //把HashMap存入list之中
            }

            ListAdapter listAdapter = new SimpleAdapter(
                    this,
                    list,
                    R.layout.simple_list_item_2,
                    new String[]{"title", "text"},
                    new int[]{R.id.text1, R.id.text2});
            // 5個參數 : context , List , layout , key1 & key2 , text1 & text2

            listView.setAdapter(listAdapter);
        }

        if (list.size() == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.no_data), Toast.LENGTH_LONG).show();
            listView.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.VISIBLE);
        }
    }

    private void doQueryPalletList() {
        info = new RmaPalletListHelper();
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetPalletList().execute(0);
    }

    private class GetPalletList extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            publishProgress(getString(R.string.downloading_data));
            AppController.debug("Get Rma Pallet List from " + AppController.getServerInfo()
                    + AppController.getProperties("GetRmaPalletList"));
            return handler.getPalletList(info);
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
                info = (RmaPalletListHelper) result;

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

                setListData(info);
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                setListData(null);
            }
        }
    }

    private class ClearPalletInfo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Delete Pallet Info to " + AppController.getServerInfo()
                    + AppController.getProperties("ClearPalletInfo"));
            publishProgress(getString(R.string.del_data));
            return handler.doDeleteTempInfo(selectedItemInfoHelper);
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
                    doQueryPalletList();
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

    private class ConfirmShipDate extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Confirm Ship Date " + AppController.getServerInfo()
                    + AppController.getProperties("ConfirmShipDate"));
            publishProgress(getString(R.string.processing));
            return handler.doConfirmShipDate(selectedItemInfoHelper);
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
                txtPalletId.setText("");

                if (result.getIntRetCode() == ReturnCode.OK) {
                    errorInfo = "";
                    mConnection.setText(result.getStrErrorBuf());
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    doQueryPalletList();
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
