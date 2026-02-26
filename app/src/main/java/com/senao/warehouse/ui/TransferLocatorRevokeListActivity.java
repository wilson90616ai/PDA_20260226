package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.adapter.ListRevokeItemAdapter;
import com.senao.warehouse.asynctask.AsyncResponse;
import com.senao.warehouse.asynctask.GetEzFlowDocumentListForRevoke;
import com.senao.warehouse.asynctask.RevokeEzflowDoc;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.DocumentInfoHelper;
import com.senao.warehouse.database.ItemInfoHelper;
import com.senao.warehouse.database.RevokeInfoHelper;
import com.senao.warehouse.util.ReturnCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransferLocatorRevokeListActivity extends Activity implements View.OnClickListener {

    private String errorInfo = "";
    private ListRevokeItemAdapter mAdapter;
    private List<DocumentInfoHelper> dataList = new ArrayList<>();
    private DocumentInfoHelper selectedItemInfoHelper;
    private RevokeInfoHelper revokeInfoHelper;
    private TextView mConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            revokeInfoHelper = new Gson().fromJson(extras.getString("CONDITION_INFO"),
                    RevokeInfoHelper.class);
            if (revokeInfoHelper == null) {
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
        setContentView(R.layout.activity_transfer_locator_revoke_list);
        Button btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(this);

        ListView listView = findViewById(R.id.listView);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItemInfoHelper = dataList.get(position);
                if (selectedItemInfoHelper.getWorkItemName().equals(getString(R.string.Direct_supervisor))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            TransferLocatorRevokeListActivity.this);
                    String msg = getString(R.string.Make_sure_to_revoke_this_form);
                    builder.setTitle( getString(R.string.btn_ok)).setMessage(msg)
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    revoke();
                                }
                            })
                            .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).setCancelable(false).show();
                } else {
                    showListDialog(selectedItemInfoHelper);
                }
                return true;
            }
        });

        mAdapter = new ListRevokeItemAdapter(this, dataList);
        listView.setAdapter(mAdapter);

        queryRevokeList();
    }

    private void showListDialog(final DocumentInfoHelper helper) {

        String[] itemList = new String[helper.getList().length];
        int i = 0;
        for (ItemInfoHelper item : helper.getList()) {
            itemList[i] = item.getItemID() + " * " + item.getQty() + "\n" + item.getDescription();
            i++;
        }

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(android.R.drawable.ic_dialog_alert);
        builderSingle.setTitle("Error Msg");
        builderSingle.setCancelable(false);
        LayoutInflater factory = LayoutInflater.from(this);
        View content = factory.inflate(R.layout.dialog_msg_list, null);

        ListView lv = content.findViewById(R.id.list);
        lv.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, itemList));

        TextView tvMsg = content.findViewById(R.id.msg);
        tvMsg.setText(R.string.error_msg_revoke);

        builderSingle.setView(content);

        builderSingle.setNeutralButton( getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.show();
    }

    private void revoke() {
        RevokeEzflowDoc task = new RevokeEzflowDoc(this, new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
                queryRevokeList();
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
        selectedItemInfoHelper.setUserId(AppController.getUser().getPassword());
        task.execute(selectedItemInfoHelper);
    }

    private void queryRevokeList() {
        GetEzFlowDocumentListForRevoke task = new GetEzFlowDocumentListForRevoke(this, new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                revokeInfoHelper = (RevokeInfoHelper) result;
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
                setListData(revokeInfoHelper);
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
                setListData(revokeInfoHelper);
            }

        });
        task.execute(revokeInfoHelper);
    }

    private void setListData(final RevokeInfoHelper tempInfo) {
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