package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.adapter.AdapterListener;
import com.senao.warehouse.adapter.OobaSnListAdapter;
import com.senao.warehouse.asynctask.AsyncResponse;
import com.senao.warehouse.asynctask.CallWebApi;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.OobaSnHelper;
import com.senao.warehouse.handler.OobaSnMsHelper;
import com.senao.warehouse.util.ReturnCode;

import java.util.ArrayList;
import java.util.List;

public class OobaSnListActivity extends Activity  implements View.OnClickListener{

    private OobaSnHelper conditionInfoHelper = null;
    private TextView mConnection,lblTitle;
    private String errorInfo = "";
    private OobaSnListAdapter mAdapter;
    private List<OobaSnMsHelper> dataList = new ArrayList<>(); //List<OobaSnMsHelper>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            conditionInfoHelper = new Gson().fromJson(extras.getString("CONDITION_INFO"), OobaSnHelper.class);
            if (conditionInfoHelper == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_condictions), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_condictions), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ooba_sn_list);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(this);
        clearStatus();

        Button btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        ListView listView = findViewById(R.id.list_item);
        mAdapter = new OobaSnListAdapter(this, dataList, new AdapterListener() {
            @Override
            public void onCallBack(BasicHelper result) {
                OobaSnHelper selectedItemInfo = (OobaSnHelper) result;
//                if (selectedItemInfo.getProcessType().equals("DEL")) {
//                    deleteItemData(selectedItemInfo);
//                } else {
//                    goToProcess(selectedItemInfo);
//                }
            }
        } );
        listView.setAdapter(mAdapter);

        lblTitle = findViewById(R.id.ap_title);
        final SpannableString text;
        String title = getString(R.string.search_result1, AppController.getOrgName());
        text = new SpannableString(title);
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);

        queryOobaSnList();
    }

    private void returnPage() {
        hideKeyboard();
        finish();
    }

    private void hideKeyboard() {
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(txtEzFlowFormNo.getWindowToken(), 0);
    }

    private void queryOobaSnList() {
        CallWebApi task = new CallWebApi(this, AppController.getProperties("GetOobaSN"), new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                conditionInfoHelper = (OobaSnHelper) result;
                AppController.debug("conditionInfoHelper = "
                        + new Gson().toJson(result));

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
//                setListData(conditionInfoHelper);
            }
        });

        task.execute(conditionInfoHelper);
    }

    private void setListData(OobaSnHelper tempInfo) {
        dataList.clear();

        if (tempInfo != null && tempInfo.getMsHelper() != null) { //&& tempInfo.getInfoList() != null
//            dataList.addAll(Arrays.asList(tempInfo.getInfoList()));
            dataList.addAll(tempInfo.getMsHelper());
        }

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

    @Override
    public void onClick(View v) {
        AppController.debug("");
        int id = v.getId();

        if (id == R.id.button_return) {
            AppController.debug("button_return");
            returnPage();
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
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.dismiss();
                        }
                    });
            dialog.show();
        }
    }
}
