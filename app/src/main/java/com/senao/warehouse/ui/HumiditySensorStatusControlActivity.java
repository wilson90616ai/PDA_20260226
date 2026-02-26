package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.BakeHelper;
import com.senao.warehouse.database.ExtremePalletItem;
import com.senao.warehouse.database.ITEM_LABEL_QR_CODE_FORMAT;
import com.senao.warehouse.database.NewPrintData;
import com.senao.warehouse.print.DataStructure;
import com.senao.warehouse.print.Printer;
import com.senao.warehouse.print.Variables;
import com.senao.warehouse.util.AsyncTaskCompleteListener;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.MyAsyncTask;
import com.senao.warehouse.util.OkHttpUtil;
import com.senao.warehouse.util.Preferences;
import com.senao.warehouse.util.QrCodeUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HumiditySensorStatusControlActivity extends Activity implements AsyncTaskCompleteListener<String> {
    private TextView mConnection, lblTitle, floor_life;
    private String errorInfo = "";
    private Button btnReturn, btnConfim, btnCancel;
    private ProgressDialog dialog;
    private RadioGroup rgDC;
    private RadioButton rbQrCode, rbReelID, rbWifi;
    private LinearLayout qrcodeLayout, reelIdLayout, import_floor_life_lin;
    private EditText edittext_msl, label_import_pallet_et, edittext_floorlife, edittext_import_qrcode, txtImportReelId;
    BakeHelper bakeHelper;

    private RadioGroup.OnCheckedChangeListener dcListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.radio_qrcode:
                if (rbQrCode.isChecked()) {
                    //pnLayout.setVisibility(View.GONE);
                    reelIdLayout.setVisibility(View.GONE);
                    qrcodeLayout.setVisibility(View.VISIBLE);
                    edittext_import_qrcode.requestFocus();
                }
                break;
            case R.id.radio_reel_id:
                if (rbReelID.isChecked()) {
                    //pnLayout.setVisibility(View.GONE);
                    reelIdLayout.setVisibility(View.VISIBLE);
                    qrcodeLayout.setVisibility(View.GONE);
                    //setListSummary(poWithQtyListReelId, labelListSummaryReelId);
                    txtImportReelId.requestFocus();
                }
                break;
        }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_humidity_sensor );

        lblTitle = findViewById(R.id.ap_title);
        setTitle();

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(HumiditySensorStatusControlActivity.this);
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

        bakeHelper = new BakeHelper();

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

        rbWifi = findViewById(R.id.radio_wifi);
        rbWifi.setChecked(true);

        rgDC = findViewById(R.id.radio_group);
        rgDC.setOnCheckedChangeListener(dcListener);

        rbQrCode = findViewById(R.id.radio_qrcode);
        rbReelID = findViewById(R.id.radio_reel_id);

        qrcodeLayout = findViewById(R.id.qrcodeLayout);

        edittext_import_qrcode = findViewById(R.id.edittext_import_qrcode);
        edittext_import_qrcode.setSelectAllOnFocus(true);

        //檢查?
        /*txtImportQrCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    EditText et = (EditText) v;

                    if (TextUtils.isEmpty(et.getText())) {
                        return false;
                    } else {
                        txtImportQrCodePackQty.requestFocus();
                        return true;
                    }
                }

                return false;
            }
        });*/

        reelIdLayout = findViewById(R.id.reelIdLayout);
        edittext_msl = findViewById(R.id.edittext_msl);
        floor_life = findViewById(R.id.floor_life);
        label_import_pallet_et = findViewById(R.id.label_import_pallet_et);

        txtImportReelId = findViewById(R.id.edittext_reelid);
        txtImportReelId.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                if(TextUtils.isEmpty(txtImportReelId.getText().toString())||"".equals(txtImportReelId.getText().toString())){
                    Toast.makeText(HumiditySensorStatusControlActivity.this, getString(R.string.enter_reelid), Toast.LENGTH_LONG).show();
                    return false;
                }else if(txtImportReelId.getText().toString().length()<34){
                    Toast.makeText(getApplicationContext(), getString(R.string.reelid_incorrect_len_must_be_34), Toast.LENGTH_SHORT).show();
                    return false;
                }

                bakeHelper.setREELID(txtImportReelId.getText().toString());
                bakeHelper.setITEM(txtImportReelId.getText().toString());
                bakeHelper.setORGANIZATION_ID(AppController.getOrg());

                //String url = AppController.getProperties("wmsportal2") + "WMS_MSL_SELECT";
                String url = AppController.getSfcIp() + "/invoke" + AppController.getApi_1() + "?sCode=" + "WMS_MSL_SELECT"; //20260213 Ann Edit
                //String url = "http://10.0.200.226:5000/printbakinglabel";
                //apiHandler.startBake(bakeHelper,"WMS_BAKING_LEVEL"); //7016A148903j1010332128101033047900

                String jsonPayload = new Gson().toJson(bakeHelper);
                AppController.debug(jsonPayload);
                AppController.debug(url);

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
                            AppController.debug("Success:"+responseData);

                            try {
                                JSONArray jsonArray = new JSONArray(responseData);
                                JSONObject jsonObject = jsonArray.getJSONObject(0);

                                final String msl = jsonObject.getString("MSL");
                                final String IPC = jsonObject.getString("IPC");
                                final String HOURS = jsonObject.getString("HOURS");

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                    edittext_msl.setText(msl);
                                    bakeHelper.setMSL(msl);
                                    bakeHelper.setIPC(IPC);
                                    bakeHelper.setHOURS(HOURS);

                                    String text = "Floor Life: " + IPC;
                                    SpannableString spannableString = new SpannableString(text);

                                    //找到 "168" 的起始位置和結束位置
                                    int start = text.indexOf(IPC);
                                    int end = start + IPC.length();

                                    //設置 "168" 的文字顏色為紅色
                                    spannableString.setSpan(new ForegroundColorSpan(Color.RED), start, end, 0);

                                    //將 SpannableString 設置到 TextView 中
                                    floor_life.setText(spannableString);

                                    label_import_pallet_et.requestFocus();

                                    if("6".equals(msl)){
                                        import_floor_life_lin.setVisibility(View.VISIBLE);
                                    }

                                    //Floor Life: 168 hour  Toast.makeText(HumiditySensorStatusControlActivity.this, "成功寫入烘烤開始時間", Toast.LENGTH_SHORT).show();
                                    //btnConfim.setVisibility(View.VISIBLE);
                                    //button_start.setVisibility(View.GONE);
                                    //cleanData(); //7016A148903j1010332128101033047900
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                                //floor_life.setText("No data");
                                mConnection.setText("Error");
                                mConnection.setTextColor(Color.RED); //7016A323700X1090132413109013A00100@@10@86
                                //mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                                errorInfo = "Error:"+e.toString();
                            }

                            //Toast.makeText(BakeStartActivity.this, "成功寫入烘烤開始時間", Toast.LENGTH_SHORT).show();
                            //处理响应数据
                        } else {
                            AppController.debug("error:"+response.toString());
                            //floor_life.setText("Failed");
                            //mConnection.setText("Failed");
                            //mConnection.setTextColor(Color.WHITE);
                            //mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                            errorInfo = "error:"+response.toString();
                            //处理请求失败的情况
                        }
                    }
                });

                return true;
            }

            return false;
            }
        });

        btnConfim = findViewById(R.id.button_confirm);
        btnConfim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(reelIdLayout.getVisibility()==View.VISIBLE){
                    if(TextUtils.isEmpty(label_import_pallet_et.getText().toString())){
                        Toast.makeText(HumiditySensorStatusControlActivity.this, getString(R.string.import_original_pack_qty), Toast.LENGTH_SHORT).show();
                        label_import_pallet_et.requestFocus();
                        return;
                    }else if(TextUtils.isEmpty(edittext_msl.getText().toString())){//edittext_msl
                        Toast.makeText(HumiditySensorStatusControlActivity.this, getString(R.string.import_msl), Toast.LENGTH_SHORT).show();
                        edittext_floorlife.requestFocus();
                        return;
                    }else if("6".equals(bakeHelper.getMSL())){
                        if(TextUtils.isEmpty(edittext_floorlife.getText().toString())){
                            Toast.makeText(HumiditySensorStatusControlActivity.this, getString(R.string.import_floor_life), Toast.LENGTH_SHORT).show();
                            edittext_floorlife.requestFocus();
                            return;
                        }

                        bakeHelper.setHOURS(edittext_floorlife.getText().toString());
                    }else if(txtImportReelId.getText().toString().length()<34){
                        Toast.makeText(getApplicationContext(), getString(R.string.reelid_incorrect_len_must_be_34), Toast.LENGTH_SHORT).show();
                        return ;
                    }

                    if(TextUtils.isEmpty(QrCodeUtil.getValueFromItemLabelQrCode(txtImportReelId.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID))){
                        bakeHelper.setREELID( txtImportReelId.getText().toString());
                        bakeHelper.setITEM( txtImportReelId.getText().toString());
                    }else{
                        bakeHelper.setREELID( QrCodeUtil.getValueFromItemLabelQrCode(txtImportReelId.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID));
                        bakeHelper.setITEM(QrCodeUtil.getValueFromItemLabelQrCode(txtImportReelId.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID));
                    }

                    bakeHelper.setORI_PACK_QTY(label_import_pallet_et.getText().toString());

                    if (rbWifi.isChecked()) {
                        onCreateChoosePrinterDialog().show();
                    } else {
                        //printLabel();
                    }
                }else if(qrcodeLayout.getVisibility()==View.VISIBLE){
                    //edittext_import_qrcode.getText().toString()

                    if(TextUtils.isEmpty(edittext_import_qrcode.getText().toString())){
                        Toast.makeText(HumiditySensorStatusControlActivity.this, getString(R.string.enter_qrcode),
                                Toast.LENGTH_SHORT).show();
                        edittext_import_qrcode.requestFocus();
                        return;
                    }else if(edittext_import_qrcode.getText().toString().length()<33){
                        Toast.makeText(getApplicationContext(), getString(R.string.reelid_incorrect_len_must_be_34),
                                Toast.LENGTH_SHORT).show();
                        return  ;
                    }

                    //bakeHelper.setREELID( edittext_import_qrcode.getText().toString());
                    //bakeHelper.setITEM( edittext_import_qrcode.getText().toString());

                    if(TextUtils.isEmpty(QrCodeUtil.getValueFromItemLabelQrCode(edittext_import_qrcode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID))){
                        bakeHelper.setREELID( edittext_import_qrcode.getText().toString());
                        bakeHelper.setITEM( edittext_import_qrcode.getText().toString());
                    }else{
                        bakeHelper.setREELID( QrCodeUtil.getValueFromItemLabelQrCode(edittext_import_qrcode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID));
                        bakeHelper.setITEM(QrCodeUtil.getValueFromItemLabelQrCode(edittext_import_qrcode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID));
                    }

                    if(TextUtils.isEmpty(QrCodeUtil.getValueFromItemLabelQrCode(edittext_import_qrcode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.ORG))){
                        bakeHelper.setORGANIZATION_ID(AppController.getOrg());
                    }else {
                        bakeHelper.setORGANIZATION_ID(Integer.parseInt(QrCodeUtil.getValueFromItemLabelQrCode(edittext_import_qrcode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.ORG)));
                    }

                    //bakeHelper.setORGANIZATION_ID(Integer.parseInt(QrCodeUtil.getValueFromItemLabelQrCode(edittext_import_qrcode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.ORG)));
                    bakeHelper.setORI_PACK_QTY(QrCodeUtil.getValueFromItemLabelQrCode(edittext_import_qrcode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.QTY));

                    //String url = AppController.getProperties("wmsportal2") + "WMS_MSL_SELECT";
                    String url = AppController.getSfcIp() + "/invoke" + AppController.getApi_1() + "?sCode=" + "WMS_MSL_SELECT"; //20260213 Ann Edit
                    //apiHandler.startBake(bakeHelper,"WMS_BAKING_LEVEL"); //7016A148903j1010332128101033047900

                    String jsonPayload = new Gson().toJson(bakeHelper);
                    AppController.debug(jsonPayload);
                    AppController.debug(url);

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
                                AppController.debug("Success:"+responseData);

                                try {
                                    JSONArray jsonArray = new JSONArray(responseData);
                                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                                    final String msl = jsonObject.getString("MSL");
                                    final String IPC = jsonObject.getString("IPC");
                                    final String HOURS = jsonObject.getString("HOURS");

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //edittext_msl.setText(msl);
                                            bakeHelper.setMSL(msl);
                                            bakeHelper.setIPC(IPC);

                                            if("6".equals(msl)){
                                                bakeHelper.setHOURS("0");
                                            }else{
                                                bakeHelper.setHOURS(HOURS);
                                            }

                                            if (rbWifi.isChecked()) {
                                                onCreateChoosePrinterDialog().show();
                                            } else {

                                            }

                                            /*String text = "Floor Life: " + IPC;
                                            SpannableString spannableString = new SpannableString(text);

                                            //找到 "168" 的起始位置和結束位置
                                            int start = text.indexOf(IPC);
                                            int end = start + IPC.length();

                                            //設置 "168" 的文字顏色為紅色
                                            spannableString.setSpan(new ForegroundColorSpan(Color.RED), start, end, 0);

                                            //將 SpannableString 設置到 TextView 中
                                            floor_life.setText(spannableString);

                                            label_import_pallet_et.requestFocus();

                                            if("6".equals(msl)){
                                                import_floor_life_lin.setVisibility(View.VISIBLE);
                                            }

                                            Floor Life: 168 hour  Toast.makeText(HumiditySensorStatusControlActivity.this, "成功寫入烘烤開始時間", Toast.LENGTH_SHORT).show();
                                            btnConfim.setVisibility(View.VISIBLE);
                                            button_start.setVisibility(View.GONE);
                                            cleanData(); //7016A148903j1010332128101033047900*/
                                        }
                                    });
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    //floor_life.setText("No data");
                                    //mConnection.setText("No data");
                                    //mConnection.setTextColor(Color.WHITE);
                                    //mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                                    mConnection.setText("Error");
                                    mConnection.setTextColor(Color.RED);
                                    //mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                                    errorInfo = "Error:"+e.toString();
                                }

                                //Toast.makeText(BakeStartActivity.this, "成功寫入烘烤開始時間", Toast.LENGTH_SHORT).show();
                                // 处理响应数据
                            } else {
                                AppController.debug("error:"+response.toString());
                                //floor_life.setText("Failed");
                                //mConnection.setText("Failed");
                                //mConnection.setTextColor(Color.WHITE);
                                //mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                                errorInfo = "error:"+response.toString();
                                //处理请求失败的情况
                            }
                        }
                    });
                }
            }
        });

        import_floor_life_lin = findViewById(R.id.import_floor_life_lin);
        edittext_floorlife = findViewById(R.id.edittext_floorlife);
        import_floor_life_lin.setVisibility(View.GONE);
    }

    private Dialog onCreateChoosePrinterDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer_new, null);
        final Spinner printerIP = item.findViewById(R.id.printspinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, //對應的Context
                R.array.printersIP, //資料選項內容
                android.R.layout.simple_spinner_item); //預設Spinner未展開時的View(預設及選取後樣式)
        printerIP.setAdapter(adapter);
        printerIP.setSelection(adapter.getPosition("10.0.189.136"));
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(Preferences.MATERIAL_PRINTER_QTY, AppController.getProperties("MaterialPrinterQty"));
        etQty.setText(qty);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Set the dialog title
        builder.setTitle(R.string.label_printer_info)
                .setView(item)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                    if (TextUtils.isEmpty(etQty.getText().toString())) {
                        Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                        etQty.requestFocus();
                        return;
                    }

                    alertDialog.dismiss();
                    dialog = ProgressDialog.show(HumiditySensorStatusControlActivity.this, getString(R.string.holdon), getString(R.string.printingLabel), true);

                    int printTimes = Integer.parseInt(etQty.getText().toString());
                    /*for (int j = 0; j < printTimes; j++) {

                    }

                    HashMap<String, String> printInfo=new HashMap<>();
                    NewPrintData PrintData= new NewPrintData();
                    printInfo.put("printname" ,"iDPRT iT4S");
                    printInfo.put("IP" ,printerIP.getSelectedItem().toString());
                    printInfo.put("count" ,etQty.getText().toString().trim());
                    HashMap<String, String> var=new HashMap<>();
                    var.put("HOURS",bakeHelper.getHOURS());
                    var.put("ORI_PACK_QTY",bakeHelper.getORI_PACK_QTY());
                    var.put("REELID",bakeHelper.getREELID());
                    var.put("MSL",bakeHelper.getMSL());

                    PrintData.setVariables(var);

                    String jsonPayload = new Gson().toJson(printInfo);
                    AppController.debug(jsonPayload);*/

                    final Printer printer1 = new Printer("iDPRT iT4S", printerIP.getSelectedItem().toString(), etQty.getText().toString().trim())  ;
                    final Variables variables1 = new Variables(bakeHelper.getHOURS(), bakeHelper.getORI_PACK_QTY(), bakeHelper.getREELID(), bakeHelper.getMSL(),bakeHelper.getORGANIZATION_ID());

                    /*AppController.debug(printer1.toString());
                    Object dataStructure = new Object() {
                        Printer printer = printer1;
                        Variables variables = variables1;
                    }; //7016A148903j1010332128101033047900*/

                    DataStructure dataStructure = new DataStructure(printer1, variables1);

                    Gson gson = new Gson();
                    String json = gson.toJson(dataStructure);
                    AppController.debug(json); //742A131110000000002201000000A00200@@100@86

                    //String url = AppController.getProperties("wmsportal2") + "WMS_BAKING_PRINT";
                    String url = "http://10.0.200.226:5000/printbakinglabel";
                    new MyAsyncTask(HumiditySensorStatusControlActivity.this).execute(url, json);
                    //new PrintWithNewType().execute(0);
                    }
                });
            }
        });

        return alertDialog;
    }

    private void cleanData() {
        if (reelIdLayout.getVisibility() == View.VISIBLE) {
            txtImportReelId.setText("");
            floor_life.setText("");
            label_import_pallet_et.setText("");
            edittext_msl.setText("");
            txtImportReelId.requestFocus();
            import_floor_life_lin.setVisibility(View.GONE);
            edittext_floorlife.setText("");
        } else {
            edittext_import_qrcode.setText("");
            edittext_import_qrcode.requestFocus();
        }

        mConnection.setText("");
        mConnection.setTextColor(Color.WHITE);
        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        errorInfo = "";
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.title_humidity_sensor, AppController.getOrgName()));
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private void returnPage() {
        finish();
    }

    public void onBackPressed() {
        //do something here and don't write super.onBackPressed()
        returnPage();
    }

    @Override
    public void onTaskComplete(String result) {
        dialog.dismiss();

        if(result!=null && !TextUtils.isEmpty(result)){
            try {
                //JSONArray jsonArray = new JSONArray(result); //{"result":"OK"} 7016A148903j1010332128101033047900
                JSONObject jsonObject = new JSONObject(result);
                String print_result = jsonObject.getString("result");

                if("OK".equals(print_result)){
                    AppController.debug("OK");
                    Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                }else{
                    mConnection.setText("Failed");
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "Print Failed";
                    AppController.debug("NG1");
                    //Toast.makeText(getApplicationContext(), "NG1", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                AppController.debug("NG2");
                Toast.makeText(getApplicationContext(), "NG2", Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(getApplicationContext(), "NG3", Toast.LENGTH_LONG).show();
        }
    }
}
