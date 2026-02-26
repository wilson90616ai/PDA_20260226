package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.BakeHelper;
import com.senao.warehouse.database.ITEM_LABEL_QR_CODE_FORMAT;
import com.senao.warehouse.handler.APIHandler;
import com.senao.warehouse.util.AsyncTaskCompleteListener;
import com.senao.warehouse.util.MyAsyncTask;
import com.senao.warehouse.util.OkHttpUtil;
import com.senao.warehouse.util.QrCodeUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class BakeStartActivity extends Activity  implements AsyncTaskCompleteListener<String> {
    private TextView mConnection, bake_result, lblTitle;
    private String errorInfo = "";
    private Button btnReturn, btnConfim, btnCancel, button_start;
    private EditText txtImportReelId, edittext_bakeloc;
    private RadioGroup temperature, type;
    private BakeHelper bakeHelper;
    private APIHandler apiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_bake);

        bakeHelper = new BakeHelper();
        bakeHelper.setORGANIZATION_ID(AppController.getOrg());

        apiHandler = new APIHandler();

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(BakeStartActivity.this);
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

        type= findViewById(R.id.radio_group_dc);
        temperature= findViewById(R.id.radio_group_dc2);

        button_start = findViewById(R.id.button_start);
        //button_start.setVisibility(View.GONE);
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String url = AppController.getProperties("wmsportal2") + "WMS_BAKING_STIME_INSEERT"; //寫入烘烤開始
                String url = AppController.getSfcIp() + "/invoke" + AppController.getApi_1() + "?sCode=" + "WMS_BAKING_STIME_INSEERT"; //寫入烘烤開始，20260213 Ann Edit
                //apiHandler.startBake(bakeHelper,"WMS_BAKING_LEVEL");
                //7016A148903j1010332128101033047900@@500@86
                //7016A148903j101033212810103304TEST@@500@86
                //bakeHelper=null;
                String jsonPayload = new Gson().toJson(bakeHelper);
                AppController.debug("jsonPayload:"+jsonPayload);

                OkHttpUtil.postRequest(url, jsonPayload, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        AppController.debug(e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            AppController.debug(responseData);
                            JSONArray jsonArray = null;
                            JSONObject jsonObject = null;
                            String result = "";

                            try {
                                jsonArray = new JSONArray(responseData);
                                jsonObject = jsonArray.getJSONObject(0);
                                result=jsonObject.getString("result");

                                if ("ok".equals(jsonObject.getString("result"))) {
                                    runOnUiThread(new Runnable() { //執行後要改變UI須要在此處理
                                        @Override
                                        public void run() {
                                            Toast.makeText(BakeStartActivity.this, getString(R.string.Successfully_recorded_baking_start_time), Toast.LENGTH_SHORT).show();
                                            btnConfim.setVisibility(View.VISIBLE);
                                            button_start.setVisibility(View.GONE);
                                            cleanData();
                                        }
                                    });
                                } else {
                                    runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(BakeStartActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            //Toast.makeText(BakeStartActivity.this, "成功寫入烘烤開始時間", Toast.LENGTH_SHORT).show();
                            //处理响应数据
                        } else {
                            AppController.debug(response.toString());
                            //处理请求失败的情况
                        }
                    }
                });

                //btnConfim.setVisibility(View.VISIBLE);
                //button_start.setVisibility(View.GONE);
            }
        });
        button_start.setVisibility(View.GONE);

        btnConfim = findViewById(R.id.button_confirm);
        btnConfim.setVisibility(View.VISIBLE);
        btnConfim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (checkFields())
                    //doQueryPN();

                if (TextUtils.isEmpty(txtImportReelId.getText().toString())) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_reelid), Toast.LENGTH_SHORT).show();
                    txtImportReelId.requestFocus();
                    return;
                } else if (txtImportReelId.getText().toString().length() < 34) {
                    Toast.makeText(getApplicationContext(), getString(R.string.reelid_incorrect_len_must_be_34), Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(edittext_bakeloc.getText().toString())) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_bakeloc), Toast.LENGTH_SHORT).show();
                    edittext_bakeloc.requestFocus();
                    return;
                }

                //edittext_bakeloc.requestFocus();
                int selectedRadioButtonId = type.getCheckedRadioButtonId();

                if (selectedRadioButtonId != -1) {
                    RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
                    //现在您可以使用selectedRadioButton来执行您想要的操作
                    //例如：selectedRadioButton.getText() 获取选中的RadioButton的文本
                    AppController.debug("selectedRadioButtonId:" + selectedRadioButtonId);

                    if (selectedRadioButtonId == R.id.bake_pcb) {
                        //AppController.debug("PCB 被選中 ");
                        bakeHelper.setTID("1");
                    } else if (selectedRadioButtonId == R.id.bake_bag) {
                        bakeHelper.setTID("2");
                    } else if (selectedRadioButtonId == R.id.bake_other) {
                        bakeHelper.setTID("3");
                    } else if (selectedRadioButtonId == R.id.bake_reel) {
                        bakeHelper.setTID("4");
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.plz_input_package), Toast.LENGTH_SHORT).show();
                }

                selectedRadioButtonId = temperature.getCheckedRadioButtonId();

                if (selectedRadioButtonId != -1) {
                    RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
                    AppController.debug("selectedRadioButtonId:"+selectedRadioButtonId);
                } else {

                }

                //bakeHelper.setITEM(txtImportReelId.getText().toString());
                //bakeHelper.setREELID(txtImportReelId.getText().toString());
                bakeHelper.setREELID( QrCodeUtil.getValueFromItemLabelQrCode(txtImportReelId.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID));
                bakeHelper.setITEM(QrCodeUtil.getValueFromItemLabelQrCode(txtImportReelId.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID));
                bakeHelper.setBakeloc(edittext_bakeloc.getText().toString());

                if(TextUtils.isEmpty(QrCodeUtil.getValueFromItemLabelQrCode(txtImportReelId.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.ORG))){
                    //bakeHelper.setORGANIZATION_ID(AppController.getOrg());
                    Toast.makeText(getApplicationContext(), getString(R.string.org_is_not_null), Toast.LENGTH_SHORT).show();
                    txtImportReelId.requestFocus();
                    return;
                }else if(TextUtils.isEmpty(QrCodeUtil.getValueFromItemLabelQrCode(txtImportReelId.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.QTY))){
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_qty),Toast.LENGTH_SHORT).show();
                    txtImportReelId.requestFocus();
                    return;
                }

                bakeHelper.setORGANIZATION_ID(Integer.parseInt(QrCodeUtil.getValueFromItemLabelQrCode(txtImportReelId.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.ORG)));
                bakeHelper.setQTY(QrCodeUtil.getValueFromItemLabelQrCode(txtImportReelId.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.QTY));

                //String url = AppController.getProperties("wmsportal2") + "WMS_BAKING_LEVEL"; //查烘烤等級及規則
                String url = AppController.getSfcIp() + "/invoke" + AppController.getApi_1() + "?sCode=" + "WMS_BAKING_LEVEL"; //查烘烤等級及規則，20260213 Ann Edit
                //apiHandler.startBake(bakeHelper,"WMS_BAKING_LEVEL");
                String jsonPayload = new Gson().toJson(bakeHelper);
                AppController.debug(jsonPayload);
                new MyAsyncTask(BakeStartActivity.this).execute(url, jsonPayload);

                /*OkHttpUtil.postRequest(url, jsonPayload, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        AppController.debug(e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            AppController.debug(responseData);
                            //处理响应数据
                        } else {
                            AppController.debug(response.toString());
                            //处理请求失败的情况
                        }
                    }
                });

                String response = HttpUtil.postData(AppController.getProperties("wmsportal2") + "WMS_BAKING_LEVEL", jsonPayload);
                AppController.debug(response);*/
            }
        });

        txtImportReelId = findViewById(R.id.edittext_reelid);
        txtImportReelId.setSelectAllOnFocus(true);

        edittext_bakeloc = findViewById(R.id.edittext_bakeloc);
        edittext_bakeloc.requestFocus();

        bake_result = findViewById(R.id.bake_result);

        lblTitle = findViewById(R.id.ap_title);
        setTitle();
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.title_activity_start_bake, AppController.getOrgName()));
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
                //7016A148903j1010332128101033047900@@500@86
                String msl = jsonObject.getString("MSL");
                String bakingTime = jsonObject.getString("BAKING_TIME");
                String temperature = jsonObject.getString("TEMP");

                bakeHelper.setMSL(msl);
                bakeHelper.setBAKING_TIME(bakingTime);
                bakeHelper.setTemperature(temperature);

                SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append("MSL Level：");
                builder.append(msl);
                builder.append("\n");

                builder.append(getString(R.string.label_Bake_Time));
                builder.append("\n");

                int start = builder.length();
                builder.append(bakingTime).append("hr").append("\n");
                builder.setSpan(new ForegroundColorSpan(0xFF0000FF), start, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //设置字体颜色为蓝色
                builder.setSpan(new RelativeSizeSpan(1.5f), start, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //设置字体大小增大

                builder.append(getString(R.string.label_Bake_Temperature));
                builder.append("\n");

                start = builder.length();
                builder.append(temperature);
                builder.setSpan(new ForegroundColorSpan(0xFF0000FF), start, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //设置字体颜色为蓝色
                builder.setSpan(new RelativeSizeSpan(1.5f), start, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //设置字体大小增大

                ((TextView)findViewById(R.id.bake_result)).setText(builder);

                btnConfim.setVisibility(View.GONE);
                button_start.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), getString(R.string.label_No_bake_data), Toast.LENGTH_SHORT).show();
            }
        } else {
            //请求失败，处理错误 5730A03165000000002404000000A00100@@500@86
            Toast.makeText(getApplicationContext(), getString(R.string.label_No_bake_data), Toast.LENGTH_SHORT).show();
        }
    }

    private void returnPage() {
        finish();
    }

    private void cleanData() {
        txtImportReelId.setText("");
        //txtImportReelId.requestFocus();
        edittext_bakeloc.setText("");
        edittext_bakeloc.requestFocus();
        bake_result.setText("MSL Level:");
        btnConfim.setVisibility(View.VISIBLE);
        button_start.setVisibility(View.GONE);
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
