package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
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
import com.senao.warehouse.util.AsyncTaskCompleteListener;
import com.senao.warehouse.util.MyAsyncTask;
import com.senao.warehouse.util.QrCodeUtil;
import com.senao.warehouse.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class BakeQueryActivity extends Activity  implements AsyncTaskCompleteListener<String> {
    private TextView mConnection, bake_result, passorfail, lblTitle;
    private String errorInfo = "";
    private Button btnReturn, btnConfim, btnCancel;
    private EditText txtImportReelId;
    private RadioButton bake_t1, bake_t2;
    private RadioButton bake_pcb, bake_bag, bake_other, bake_reel;
    private RadioGroup temperature, type;
    private ListView datalist;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> dataList_arr;
    private BakeHelper bakeHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_bake);

        bakeHelper = new BakeHelper();
        bakeHelper.setORGANIZATION_ID(AppController.getOrg());

        lblTitle = findViewById(R.id.ap_title);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(BakeQueryActivity.this);
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

        type = findViewById(R.id.radio_group_dc);
        temperature = findViewById(R.id.radio_group_dc2);

        passorfail = findViewById(R.id.passorfail);
        //errorInfo = "test";

        btnConfim = findViewById(R.id.button_confirm);
        btnConfim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //bakeHelper.setITEM(txtImportReelId.getText().toString());
                //bakeHelper.setREELID(txtImportReelId.getText().toString());
                bakeHelper.setREELID( QrCodeUtil.getValueFromItemLabelQrCode(txtImportReelId.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID));
                bakeHelper.setITEM(QrCodeUtil.getValueFromItemLabelQrCode(txtImportReelId.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID));

                //String url = AppController.getProperties("wmsportal2") + "WMS_BAKING_REELID_SEELCT"; //查烘烤等級及規則
                String url = AppController.getSfcIp() + "/invoke" + AppController.getApi_1() + "?sCode=" + "WMS_BAKING_REELID_SEELCT"; //查烘烤等級及規則，20260213 Ann Edit
                //apiHandler.startBake(bakeHelper,"WMS_BAKING_LEVEL");

                String jsonPayload = new Gson().toJson(bakeHelper);
                AppController.debug(jsonPayload);
                new MyAsyncTask(BakeQueryActivity.this).execute(url, jsonPayload);
            }

        });

        txtImportReelId = findViewById(R.id.edittext_reelid);
        txtImportReelId.setSelectAllOnFocus(true);

        setTitle();
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.title_activity_query_bake, AppController.getOrgName()));
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
                //7016A148903j1010332128101033047900
                String STARTTIME = jsonObject.getString("STARTTIME");
                String ENDTIME = jsonObject.getString("ENDTIME");
                String NH = jsonObject.getString("NH");
                String BAKING_TIME = jsonObject.getString("BAKING_TIME");
                //bakeHelper.setMSL(msl);
                //bakeHelper.setBAKING_TIME(bakingTime);
                //bakeHelper.setTemperature(temperature);

                SpannableStringBuilder builder = new SpannableStringBuilder();

                if(!TextUtils.isEmpty(STARTTIME)&&!"null".equals(STARTTIME)){
                    STARTTIME = Util.formatDateTime(STARTTIME);
                }

                builder.append(getString(R.string.Start_Date) + "：").append("\n");

                int start = builder.length();
                builder.append(STARTTIME);
                builder.setSpan(new ForegroundColorSpan(0xFF0000FF), start, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //设置字体颜色为蓝色
                builder.setSpan(new RelativeSizeSpan(1.5f), start, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //设置字体大小增大
                builder.append("\n");
                builder.append(getString(R.string.End_Date) + "：").append("\n");

                passorfail.setVisibility(View.VISIBLE);

                float remaining = Float.parseFloat(BAKING_TIME)-Float.parseFloat(NH);
                DecimalFormat df = new DecimalFormat("#.##");
                String formattedRemaining = df.format(remaining);

                if(!TextUtils.isEmpty(ENDTIME)&&!"null".equals(ENDTIME)){
                    ENDTIME = Util.formatDateTime(ENDTIME);
                    start = builder.length();
                    builder.append(ENDTIME);
                    builder.setSpan(new ForegroundColorSpan(0xFF0000FF), start, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //设置字体颜色为蓝色
                    builder.setSpan(new RelativeSizeSpan(1.5f), start, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //设置字体大小增大
                    builder.append("\n");
                }else{
                    start = builder.length();
                    builder.append(getString(R.string.Still_in_baking_standard_baking_time_is) + ":" + BAKING_TIME + " hr").append("\n");
                    builder.setSpan(new ForegroundColorSpan(0xFF0000FF), start, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //设置字体颜色为蓝色
                    builder.setSpan(new RelativeSizeSpan(1.5f), start, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //设置字体大小增大
                    builder.append("\n");

                    if(remaining<=0.00){
                        start = builder.length();
                        //builder.append(formattedRemaining+" ").append("hr");
                        builder.append(getString(R.string.Baking_time_reached_but_not_yet_completed)).append("\n");
                        builder.setSpan(new ForegroundColorSpan(0xFFFF0000), start, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //设置字体颜色为蓝色
                        builder.setSpan(new RelativeSizeSpan(1.5f), start, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //设置字体大小增大
                        builder.append("\n");
                    }else{
                        builder.append(getString(R.string.Remaining_Time) + "：").append("\n");
                        start = builder.length();
                        builder.append(formattedRemaining+" ").append("hr");
                        builder.setSpan(new ForegroundColorSpan(0xFFFF0000), start, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //设置字体颜色为蓝色
                        builder.setSpan(new RelativeSizeSpan(1.5f), start, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //设置字体大小增大
                        builder.append("\n");
                    }
                }

                if(remaining<0){
                    passorfail.setText("PASS");
                    passorfail.setBackgroundColor(getResources().getColor(R.color.green));
                }else {
                    passorfail.setText("FAIL");
                    passorfail.setBackgroundColor(getResources().getColor(R.color.red));
                }

                ((TextView)findViewById(R.id.bake_result)).setText(builder);
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
            Toast.makeText(BakeQueryActivity.this, getString(R.string.label_No_bake_data), Toast.LENGTH_SHORT).show();
        }
    }

    private void returnPage() {
        finish();
    }

    private void cleanData() {
        txtImportReelId.setText("");
        //bake_result.setText("");
        ((TextView)findViewById(R.id.bake_result)).setText("");
        txtImportReelId.requestFocus();
        passorfail.setVisibility(View.GONE);
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
