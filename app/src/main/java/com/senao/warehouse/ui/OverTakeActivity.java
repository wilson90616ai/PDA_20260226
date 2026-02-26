package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
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
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.OverTakeHelper;
import com.senao.warehouse.handler.OverTakeHandler;
import com.senao.warehouse.util.ReturnCode;

public class OverTakeActivity extends Activity implements View.OnClickListener{

    private RadioGroup mRadioGroup;
    private RadioButton importpn, importreelid, importrqcode;
    private TextView mConnection,lblTitle;
    private EditText edtvItemNo;
    private String errorInfo = "";
    OverTakeHelper helper;
    OverTakeHandler overTakeHandler;

    private ProgressDialog dialog;

    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.overtake);

        importpn = (RadioButton) findViewById(R.id.importpn);
        importreelid = (RadioButton) findViewById(R.id.importreelid);
        importrqcode = (RadioButton) findViewById(R.id.importrqcode);

        mRadioGroup = findViewById(R.id.radio_group_import);
        mRadioGroup.check(R.id.importpn); //設定 mRadioButton0 選項為選取狀態
        mRadioGroup.setOnCheckedChangeListener(radGrpRegionOnCheckedChange); //設定單選選項監聽器

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(this);

        btnConfirm =  findViewById(R.id.button_confirm);
        btnConfirm.setOnClickListener(this);

        findViewById(R.id.button_cancel).setOnClickListener(this);
        findViewById(R.id.button_return).setOnClickListener(this);

        edtvItemNo =  findViewById(R.id.edittext_subinventory11);

        helper = new OverTakeHelper();
        overTakeHandler = new OverTakeHandler();
        //clearStatus();
        lblTitle = findViewById(R.id.ap_title);
        setTitle();
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.title_activity_overtake1, AppController.getOrgName()));
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private void clearStatus() {
        mConnection.setText("");
        mConnection.setTextColor(Color.WHITE);
        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        errorInfo = "";
    }

    private RadioGroup.OnCheckedChangeListener radGrpRegionOnCheckedChange = new RadioGroup.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.importpn: //case importpn.getId():
                    AppController.debug("importpn");
                    break;

                case R.id.importreelid: //case importreelid.getId():
                    AppController.debug("importreelid");
                    break;

                case R.id.importrqcode: //case importrqcode.getId():
                    AppController.debug("importrqcode");
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button_return) {
            finish();
//        }
//        else if (id == R.id.button_stock_in) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle( getString(R.string.btn_ok)).setMessage(getString(R.string.ru_sure_in_stock))
//                    .setPositiveButton(getString(R.string.yes), dialogClickListener)
//                    .setNegativeButton(getString(R.string.no), dialogClickListener).show();
        } else if (id == R.id.label_status) {
            if (!TextUtils.isEmpty(errorInfo)) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(OverTakeActivity.this);
                dialog.setTitle("Error Msg");
                dialog.setMessage(errorInfo);
                dialog.setIcon(android.R.drawable.ic_dialog_alert);
                dialog.setCancelable(false);
                dialog.setNegativeButton(getString(R.string.btn_ok),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int arg1) {
                                dialog.dismiss();
                            }
                        });

                dialog.show();
            }
        } else if(id == R.id.button_confirm){
            if(TextUtils.isEmpty(edtvItemNo.getText().toString())){
                Toast.makeText(getApplicationContext(), getString(R.string.enter_sku), Toast.LENGTH_SHORT).show();
                return;
            }else if(edtvItemNo.getText().toString().length()<12){
                Toast.makeText(getApplicationContext(), getString(R.string.sku_len_can_not_less_than_12), Toast.LENGTH_SHORT).show();
                return;
            }

            helper.setItemNo(edtvItemNo.getText().toString().substring(0, 12));//edtvItemNo.getText().toString().substring(0, 12);  "6520A4293010"
            doGetOverTakeInfo();
        }else if (id == R.id.button_cancel) {
            edtvItemNo.setText("");
            ((TextView)findViewById(R.id.show_result)).setText("");
        }
    }

    private void doGetOverTakeInfo() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new OverTakeActivity.GetOverTakeInfo().execute(0);
    }

    private class GetOverTakeInfo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Get OverTake Info to " + AppController.getServerInfo()
                    + AppController.getProperties("GetOvertakeData"));
            publishProgress(getString(R.string.data_reading));
            return overTakeHandler.getOvertakeData(helper);
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
                    OverTakeHelper resulthelper = (OverTakeHelper)result;

                    //String txt1="嗨嗨嗨<font color='#FF0000'>憨</font>嗨<br>"+resulthelper.getCouldReceiveDate()+resulthelper.getItemNo()+resulthelper.getCouldReceiveNum()+resulthelper.getLastNAME()+resulthelper.getWSDESC()+resulthelper.getWSNAME()+resulthelper.getControlDay();
                    //String txt2="<font color='#0000FF'></font>";//this.helper.getItemNo

                    String 超交="<font color='#FF0000'>"+getString(R.string.over_delivery)+"</font>"+"&nbsp;&nbsp;&nbsp;"+getString(R.string.total_receivable)+"：<font color='#FF0000'>"+resulthelper.getCouldReceiveNum()+"</font><br>";
                    String 可收="<font color='#0000FF'>"+getString(R.string.acceptable_for_receipt)+"</font>"+"&nbsp;&nbsp;&nbsp;"+getString(R.string.total_receivable)+"： <font color='#0000FF'>"+resulthelper.getCouldReceiveNum()+"</font><br>";

                    String line1=getString(R.string.state)+"："+(resulthelper.getCouldReceiveNum()==0?超交:可收);
                    String line2=getString(R.string.Demand_range)+"：<font color='#0000FF'>"+resulthelper.getCouldReceiveDate()+"</font><br>"+getString(R.string.Days_of_control)+"：<font color='#0000FF'>"+resulthelper.getControlDay() +"</font><br>";
                    String line3=getString(R.string.Buyer)+"：<font color='#0000FF'>"+resulthelper.getLastNAME()+"</font><br>";
                    String line4=getString(R.string.Warehouse_clerk)+"：<font color='#0000FF'>"+resulthelper.getWSNAME()+" "+resulthelper.getWSDESC()+"</font><br>";
                    String line5=getString(R.string.SKU)+"：<font color='#0000FF'>"+helper.getItemNo()+"</font><br>";
                    String line6=getString(R.string.Description)+"：<font color='#0000FF'>"+resulthelper.getItemNo()+"</font><br>";
                    String lineAll=line1+line2+line3+line4+line5+line6;

                    if(TextUtils.isEmpty(resulthelper.getCouldReceiveDate())&&TextUtils.isEmpty(resulthelper.getLastNAME())){
                        ((TextView)findViewById(R.id.show_result)).setText("");
                        mConnection.setText(getString(R.string.db_return_error));
                        errorInfo = getString(R.string.sku)+helper.getItemNo()+getString(R.string.no_receivable_quantity);
                        mConnection.setTextColor(Color.RED);
                        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                        return;
                    }

                    CharSequence charSequence;

                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
                        charSequence=Html.fromHtml(lineAll, Html.FROM_HTML_MODE_LEGACY);
                    }else{
                        charSequence= Html.fromHtml(lineAll);
                    }

                    ((TextView)findViewById(R.id.show_result)).setText(charSequence);

                    errorInfo = "";
                    mConnection.setText(result.getStrErrorBuf());
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    //queryData();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    ((TextView)findViewById(R.id.show_result)).setText("");
                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                }
            } else {
                ((TextView)findViewById(R.id.show_result)).setText("");
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }
}
