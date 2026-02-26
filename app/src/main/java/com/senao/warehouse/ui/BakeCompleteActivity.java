package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.BakeHelper;
import com.senao.warehouse.database.ITEM_LABEL_QR_CODE_FORMAT;
import com.senao.warehouse.print.BtPrintLabel;
import com.senao.warehouse.util.AsyncTaskCompleteListener;
import com.senao.warehouse.util.MyAsyncTask;
import com.senao.warehouse.util.QrCodeUtil;
import com.senao.warehouse.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class BakeCompleteActivity extends Activity  implements AsyncTaskCompleteListener<String> {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PRINTER_SETTING = 2;
    private static final int REQUEST_BLUETOOTH_SETTINGS = 3;
    private TextView mConnection,bake_result,lblTitle;
    private String errorInfo = "";
    private Button btnReturn, btnConfim, btnCancel;
    private EditText txtImportReelId;
    private RadioButton bake_t1, bake_t2 ;
    private RadioButton bake_pcb, bake_bag,bake_other,bake_reel ;
    private RadioGroup temperature,type;
    private ListView datalist;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> dataList_arr;
    private BakeHelper bakeHelper;
    private AsyncTaskCompleteListener completeListener, mailListener, printListener, queryListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_bake);

        bakeHelper = new BakeHelper();
        bakeHelper.setORGANIZATION_ID(AppController.getOrg());

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(BakeCompleteActivity.this);
                    dialog.setTitle("Error Msg"); //烘烤補標
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
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Return", String.valueOf(v.getId()));
                returnPage();
            }

        });

        btnCancel = findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Cancel", String.valueOf(v.getId()));
                cleanData();
            }

        });

        type= findViewById(R.id.radio_group_dc);
        temperature= findViewById(R.id.radio_group_dc2);

        //errorInfo="test";
        btnConfim = findViewById(R.id.button_confirm);
        btnConfim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(txtImportReelId.getText().toString())) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_reelid), Toast.LENGTH_SHORT).show();
                    txtImportReelId.requestFocus();
                    return;
                } else if(txtImportReelId.getText().toString().length()<34) {
                    Toast.makeText(getApplicationContext(), getString(R.string.reelid_incorrect_len_must_be_34), Toast.LENGTH_SHORT).show();
                    return;
                }

                //bakeHelper.setITEM(txtImportReelId.getText().toString());
                //bakeHelper.setREELID(txtImportReelId.getText().toString());
                bakeHelper.setREELID( QrCodeUtil.getValueFromItemLabelQrCode(txtImportReelId.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID));
                bakeHelper.setITEM(QrCodeUtil.getValueFromItemLabelQrCode(txtImportReelId.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID));

                //String url = AppController.getProperties("wmsportal2") + "WMS_BAKING_REELID_SEELCT"; //查烘烤等級及規則
                String url = AppController.getSfcIp() + "/invoke" + AppController.getApi_1() + "?sCode=" + "WMS_BAKING_REELID_SEELCT"; //查烘烤等級及規則，20260213 Ann Edit
                //apiHandler.startBake(bakeHelper,"WMS_BAKING_LEVEL");

                String jsonPayload = new Gson().toJson(bakeHelper);
                AppController.debug(jsonPayload);

                new MyAsyncTask(BakeCompleteActivity.this).execute(url, jsonPayload);
            }

        });

        completeListener = new AsyncTaskCompleteListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onTaskComplete(Object result) {
                AppController.debug("completeListener:"+result);

                try {
                    JSONArray jsonArray = new JSONArray((String) result);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    if ("ok".equals(jsonObject.get("result"))) {
                        dataList_arr.add(bakeHelper.getREELID()+"已完成");
                        adapter.notifyDataSetChanged();
                        Toast.makeText(BakeCompleteActivity.this, "Bake Complete!printing...", Toast.LENGTH_SHORT).show();

                        //開始列印
                        //String url = AppController.getProperties("wmsportal2") + "WMS_BAKING_REELID_SEELCT"; //查烘烤等級及規則
                        String url = AppController.getSfcIp() + "/invoke" + AppController.getApi_1() + "?sCode=" + "WMS_BAKING_REELID_SEELCT"; //查烘烤等級及規則，20260213 Ann Edit
                        //apiHandler.startBake(bakeHelper,"WMS_BAKING_LEVEL");
                        String jsonPayload = new Gson().toJson(bakeHelper);
                        AppController.debug(jsonPayload);
                        new MyAsyncTask(queryListener).execute(url, jsonPayload);
                    } else {
                        Toast.makeText(BakeCompleteActivity.this, "Error:"+getString(R.string.Unable_update_completion_time), Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    Toast.makeText(BakeCompleteActivity.this, "Error Exception:"+getString(R.string.Unable_update_completion_time), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        };

        printListener= new AsyncTaskCompleteListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onTaskComplete(Object result) {
                AppController.debug("printListener:"+result);
            }
        };

        queryListener= new AsyncTaskCompleteListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onTaskComplete(Object result) {
                AppController.debug("printListener:"+result);

                if (result != null) {
                    //AppController.debug("onTaskComplete result:"+result);

                    try {
                        JSONArray jsonArray = new JSONArray((String) result);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        //742A131110000000002201000000A00100
                        //7016A148903j1010332128101033047900

                        String STARTTIME = jsonObject.getString("STARTTIME");
                        String ENDTIME = jsonObject.getString("ENDTIME");
                        String NH = jsonObject.getString("NH");
                        String BAKING_TIME = jsonObject.getString("BAKING_TIME");
                        //bakeHelper.setMSL(msl);
                        //bakeHelper.setBAKING_TIME(bakingTime);
                        //bakeHelper.setTemperature(temperature);

                        if (!TextUtils.isEmpty(STARTTIME)&&!"null".equals(STARTTIME)) {
                            STARTTIME = Util.formatDateTime(STARTTIME);
                        }

                        if (!TextUtils.isEmpty(ENDTIME) && !"null".equals(ENDTIME)) {
                            ENDTIME = Util.formatDateTime(ENDTIME);bakeHelper.setSTARTDATE(STARTTIME);
                            bakeHelper.setENDDATE(ENDTIME);
                            bakeHelper.setNH(NH);
                            printLabel();
                            Toast.makeText(getApplicationContext(), "Print OK!", Toast.LENGTH_SHORT).show();
                        } else {
                            mConnection.setText(getString(R.string.Baking_not_yet_completed));
                            //mConnection.setTextColor(Color.WHITE);
                            //mConnection.setBackgroundColor(Color.RED);
                            errorInfo = getString(R.string.Baking_not_yet_completed);
                            Toast.makeText(BakeCompleteActivity.this, getString(R.string.Baking_not_yet_completed), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        errorInfo = getString(R.string.label_No_bake_data)+e;
                        Toast.makeText(BakeCompleteActivity.this, getString(R.string.label_No_bake_data), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    } catch ( Exception e) {
                        //Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        AppController.debug(e.getMessage());
                        AppController.debug(e.getStackTrace().toString());
                        errorInfo = "Exception:"+e;
                    }
                } else {
                    //请求失败，处理错误
                    Toast.makeText(BakeCompleteActivity.this, getString(R.string.label_No_bake_data), Toast.LENGTH_SHORT).show();
                }
            }
        };

        mailListener = new AsyncTaskCompleteListener() {
            @Override
            public void onTaskComplete(Object result) {
                AppController.debug("mailListener:"+result);
            }
        };

        mConnection.setText("");
        mConnection.setTextColor(Color.WHITE);
        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));

        errorInfo = "";

        txtImportReelId = findViewById(R.id.edittext_reelid);
        txtImportReelId.setSelectAllOnFocus(true);

        bake_result = findViewById(R.id.bake_result);

        datalist = findViewById(R.id.datalist);
        dataList_arr = new ArrayList<>();
        //dataList_arr.add("test011111111111111");
        //dataList_arr.add("test022222222222222");
        //dataList_arr.add("test033333333333333");
        //dataList_arr.add("test011111111111111");
        //dataList_arr.add("test022222222222222");
        //dataList_arr.add("test033333333333333");
        //dataList_arr.add("test011111111111111");
        //dataList_arr.add("test022222222222222");
        //dataList_arr.add("test033333333333333");

        //初始化 ArrayAdapter
        adapter = new ArrayAdapter<>(this, R.layout.bake_list_item, dataList_arr);
        //adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList_arr);

        //設定Adapter
        datalist.setAdapter(adapter);

        //設定ListView的長按事件
        registerForContextMenu(datalist);

        lblTitle = findViewById(R.id.ap_title);
        setTitle();
        checkPrinterSetting();
    }

    private void printLabel() {
        if (!BtPrintLabel.printBakeReprintLabel1(bakeHelper)) {
            errorInfo = getString(R.string.printLabalFailed);
            mConnection.setText(getString(R.string.printer_connect_error));
            mConnection.setTextColor(Color.RED);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }
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
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.not_connect_btprinter), Toast.LENGTH_LONG).show();
        }
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.title_activity_complete_bake, AppController.getOrgName()));
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    @Override
    public void onTaskComplete(String result) {
        if (result != null) {
            AppController.debug("onTaskComplete result:"+result);

            try {
                JSONArray jsonArray = new JSONArray(result);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String STARTTIME = jsonObject.getString("STARTTIME");
                String ENDTIME = jsonObject.getString("ENDTIME");
                String NH = jsonObject.getString("NH");
                String OH = jsonObject.getString("OH");
                String BAKING_TIME = jsonObject.getString("BAKING_TIME");

                float remaining = Float.parseFloat(BAKING_TIME)-Float.parseFloat(NH);
                DecimalFormat df = new DecimalFormat("#.##");
                String formattedRemaining = df.format(remaining);

                if (!TextUtils.isEmpty(ENDTIME)&&!"null".equals(ENDTIME)) {
                    mConnection.setText(getString(R.string.No_ongoing_baking_data));
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.RED);
                    errorInfo=bakeHelper.getREELID();
                } else {
                    if (Float.parseFloat(OH)>0.0) { //超過時間
                        bakeHelper.setDECISION("");

                        //String url = AppController.getProperties("wmsportal2") + "WMS_BAKING_ENDTIME_UPDATE"; //更新烘烤結束時間
                        String url = AppController.getSfcIp() + "/invoke" + AppController.getApi_1() + "?sCode=" + "WMS_BAKING_ENDTIME_UPDATE"; //更新烘烤結束時間，20260213 Ann Edit
                        //apiHandler.startBake(bakeHelper,"WMS_BAKING_LEVEL");

                        String jsonPayload = new Gson().toJson(bakeHelper);
                        AppController.debug(jsonPayload);
                        new MyAsyncTask(completeListener).execute(url, jsonPayload);
                    } else {
                        //7016A148903j1010332128101033047900

                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(BakeCompleteActivity.this);
                        dialogBuilder.setTitle("Note");
                        dialogBuilder.setMessage(getString(R.string.Still_requires) + formattedRemaining + getString(R.string.force_end_baking_process_and_send_notification));
                        dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setCancelable(false);

                        final EditText et = new EditText(BakeCompleteActivity.this);
                        et.setHint(getString(R.string.input_who_dicision));
                        dialogBuilder.setView(et);
                        dialogBuilder.setPositiveButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }


                        });

                        dialogBuilder.setNegativeButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!TextUtils.isEmpty(et.getText().toString())) {
                                    bakeHelper.setDECISION(et.getText().toString());
                                    //String url = AppController.getProperties("wmsportal2") + "WMS_BAKING_ENDTIME_UPDATE"; //更新烘烤結束時間
                                    String url = AppController.getSfcIp() + "/invoke" + AppController.getApi_1() + "?sCode=" + "WMS_BAKING_ENDTIME_UPDATE"; //更新烘烤結束時間，20260213 Ann Edit
                                    //apiHandler.startBake(bakeHelper,"WMS_BAKING_LEVEL");

                                    String jsonPayload = new Gson().toJson(bakeHelper);
                                    AppController.debug(jsonPayload);

                                    new MyAsyncTask(completeListener).execute(url, jsonPayload);

                                    //url = AppController.getProperties("wmsportal2") + "WMS_BAKING_NOTENOUGHTIME_MAIL"; //發mail
                                    url = AppController.getSfcIp() + "/invoke" + AppController.getApi_1() + "?sCode=" + "WMS_BAKING_NOTENOUGHTIME_MAIL"; //發mail，20260213 Ann Edit

                                    new MyAsyncTask(mailListener).execute(url, jsonPayload);
                                }
                            }
                        });

                        final AlertDialog alertDialog = dialogBuilder.create();
                        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                final Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

                                if (positiveButton != null) {
                                    positiveButton.setEnabled(!TextUtils.isEmpty(et.getText().toString()));
                                }

                                et.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                    }

                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                                        if (positiveButton != null) {
                                            positiveButton.setEnabled(!TextUtils.isEmpty(s));
                                        }
                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {

                                    }
                                });
                            }
                        });

                        alertDialog.show();
                    }
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.label_No_bake_data), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }catch ( Exception e) {
                Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                AppController.debug(e.getMessage());
                AppController.debug(e.getStackTrace().toString());
            }
        } else {
            //请求失败，处理错误
            Toast.makeText(BakeCompleteActivity.this, getString(R.string.label_No_bake_data), Toast.LENGTH_SHORT).show();
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
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.not_connect_btprinter), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //取得被長按的ListView的位置
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;

        switch (item.getItemId()) {
            case R.id.delete_item:
                //刪除選中的項目
                dataList_arr.remove(position);
                //更新ListView
                adapter.notifyDataSetChanged();
                Toast.makeText(this, getString(R.string.Item_has_been_deleted), Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void returnPage() {
        finish();
    }

    private void cleanData() {
        txtImportReelId.setText("");
        txtImportReelId.requestFocus();

        mConnection.setText("");
        mConnection.setTextColor(Color.WHITE);
        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        errorInfo = "";
    }

    private boolean checkFields() {
        if(TextUtils.isEmpty(txtImportReelId.getText().toString().trim())){
            txtImportReelId.requestFocus();
            Toast.makeText(getApplicationContext(), getString(R.string.enter_data), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
 }
