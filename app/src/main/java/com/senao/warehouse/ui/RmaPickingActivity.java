package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tscdll.TscWifiActivity;
import com.google.gson.Gson;
import com.senao.warehouse.R;
import com.senao.warehouse.database.COMPANY;
import com.senao.warehouse.database.NewPrintData;
import com.senao.warehouse.database.RmaInfoHelper;
import com.senao.warehouse.handler.NewPrintDataList;
import com.senao.warehouse.handler.OuOrgHelper;
import com.senao.warehouse.print.BtPrintLabel;
import com.senao.warehouse.database.RmaBoxInfoHelper;
import com.senao.warehouse.database.RmaBoxListInfoHelper;
import com.senao.warehouse.database.RmaPalletInfoHelper;
import com.senao.warehouse.handler.PrintLabelHandler;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.Preferences;
import com.senao.warehouse.util.QrCodeUtil;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RmaPickingActivity extends Activity implements View.OnClickListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PRINTER_SETTING = 2;
    private static final int REQUEST_BLUETOOTH_SETTINGS = 3;
    public final String TAG = RmaPickingActivity.class.getSimpleName();
    //private final String CUSTOMER_NO_MERAKI = "5633";
    private final String CUSTOMER_NO_MERAKI = "Meraki";
    private TextView mConnection,lblTitle;
    private String errorInfo;
    private Button btnReturn, btnConfirm, btnCancel, btnInput;
    private RadioGroup rgChecking;
    private RadioButton rbPallet, rbBox;
    private EditText etImportSn, etImportPalletNo;
    private String etImportSn_str;
    private ListView lvSn;
    private List<String> snList = new ArrayList<>();
    private List<String> boxList = new ArrayList<>();
    private ArrayAdapter<String> mAdapter;
    private PrintLabelHandler print;
    private ProgressDialog dialog;
    private RmaPalletInfoHelper palletInfo;
    private int selectedIndex;
    private RmaBoxInfoHelper boxInfo;
    private RmaBoxListInfoHelper boxListInfo;
    private List<RmaBoxInfoHelper> boxInfoList = new ArrayList<>();
    private TextView tvSummary;
    private int type; //0.default,1.RMA,2.LI4
    private Spinner spinnerCompany;
    private ArrayAdapter adapterCompany;
    private List<String> com = new ArrayList<>();
    private String comp="";

    private TscWifiActivity TscEthernetDll;
    private NewPrintDataList PrintDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AppController.getUser() == null) {
            finish();
            return;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_rma_picking);

        btnReturn =  findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);
        btnConfirm =  findViewById(R.id.button_confirm);
        btnConfirm.setOnClickListener(this);
        btnCancel =  findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(this);

        tvSummary =  findViewById(R.id.textViewSummary);
        tvSummary.setText(getString(R.string.rma_picking_summary, 0, 0));

        rgChecking =  findViewById(R.id.radio_group_checking);
        rgChecking.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_pallet_checking) {
                    if (rbPallet.isChecked()) {
                        //btnInput.setVisibility(View.GONE);
                        lvSn.setVisibility(View.GONE);
                        cleanData();
                    }
                } else {
                    if (rbBox.isChecked()) {
                        //btnInput.setVisibility(View.VISIBLE);
                        lvSn.setVisibility(View.VISIBLE);
                        cleanData();
                    }
                }
            }
        });

        rbPallet =  findViewById(R.id.radio_pallet_checking);
        rbBox =  findViewById(R.id.radio_box_checking);

        com.add("Meraki");
        com.add("Fortinet");

        adapterCompany = new ArrayAdapter<>(this, R.layout.spinner_item_for_rma, com);//ORGOU專案 com companies
        spinnerCompany = findViewById(R.id.spinnerCompany);
        adapterCompany.setDropDownViewResource(R.layout.spinner_item);
        spinnerCompany.setAdapter(adapterCompany);

        etImportPalletNo =  findViewById(R.id.edittext_import_pallet);

        etImportSn =  findViewById(R.id.edittext_import_sn);
        etImportSn.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down" + String.valueOf(v.getId()));

                    if (etImportPalletNo.getText().toString().trim().equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.Enter_pallet_no), Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    insertSn();
                    return true;
                }

                return false;
            }
        });

        etImportPalletNo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    etImportSn.requestFocus();
                    return true;
                }

                return false;
            }
        });

        btnInput =  findViewById(R.id.button_input);
        btnInput.setOnClickListener(this);

        lvSn =  findViewById(R.id.listView_sn);
        lvSn.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            // Yes button clicked
                            mAdapter.remove(snList.get(selectedIndex));
                            boxList.remove(selectedIndex);
                            boxInfoList.remove(selectedIndex);
                            setSummary();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            // No button clicked
                            break;
                    }
                }
            };

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedIndex = position;
                AlertDialog.Builder builder = new AlertDialog.Builder(RmaPickingActivity.this);
                builder.setTitle( getString(R.string.btn_ok)).setMessage(getString(R.string.is_del_data))
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show();

                return true;
            }
        });

        mConnection =  findViewById(R.id.label_status);
        mConnection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(RmaPickingActivity.this);
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

        mAdapter = new ArrayAdapter<String>(this, R.layout.simple_list_item, snList);
        lvSn.setAdapter(mAdapter);
        rbBox.setChecked(true);
        lblTitle = findViewById(R.id.ap_title);
        setTitle();
        checkPrinterSetting();
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.title_activity_rma_picking1, AppController.getOrgName()));
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etImportSn.getWindowToken(), 0);
    }

    private Dialog onCreatePrinterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the dialog title
        builder.setTitle(R.string.pick_printers)
                .setItems(R.array.printers_rma, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        AppController.debug("which:"+which);
                        checkComp();

                        if(which<2){//印表機1 2
                            if (rbPallet.isChecked())
                                doPrintLabelByPallet(which + 1);
                            else
                                doPrintLabelByBox(which + 1);

                        }else if(which==2){//WIFI列印
                            if (rbPallet.isChecked()) {
                                //doPrintLabelByPallet02(which + 1); //倉庫沒用過棧板，先注解
                            }else
                                doPrintLabelByBox02(which + 1);
                        }else if(which==3){//Fortinet  不用印

                        }else if(which==4){//Codesoft1
                            if (rbPallet.isChecked()) {
                                //doPrintLabelByPallet04(which + 1); //倉庫沒用過棧板，先注解
                            }else
                                doPrintLabelByBox04(which + 1);
                        }else if(which==5){//Codesoft2
                            if (rbPallet.isChecked()) {
                                //doPrintLabelByPallet05(which + 1); //倉庫沒用過棧板，先注解
                            }else
                                doPrintLabelByBox05(which + 1);
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        }).setCancelable(false);

        return builder.create();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button_return) {
            returnPage();
        } else if (id == R.id.button_confirm) {
            if (checkData()) {
                onCreatePrinterDialog().show();
            }
        } else if (id == R.id.button_cancel) {
            cleanData();
        } else if (id == R.id.button_input) {
            if (etImportPalletNo.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.Enter_pallet_no), Toast.LENGTH_SHORT).show();
                etImportPalletNo.requestFocus();
            } else {
                insertSn();
            }
        }
    }

    private void setSummary() {
        if (rbPallet.isChecked()) {
            tvSummary.setText(getString(R.string.rma_picking_summary, palletInfo.getBoxQty(), palletInfo.getQty()));
        } else {
            int count = 0;

            for (RmaBoxInfoHelper item : boxInfoList) {
                count += item.getQty();
            }

            tvSummary.setText(getString(R.string.rma_picking_summary, boxInfoList.size(), count));
        }
    }

    private void insertSn() {
        etImportSn_str = QrCodeUtil.getSnList(etImportSn.getText().toString().trim());
        String sn = etImportSn_str.trim();

        if (etImportSn.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_be_null), Toast.LENGTH_SHORT).show();
            etImportSn.requestFocus();
        } else {
            if (rbPallet.isChecked()) {
                hideKeyboard();
                doCheckSnByPallet();
            } else {
//                String sn = etImportSn.getText().toString().trim();
                if (snList.contains(sn)) {
                    etImportSn.setText("");
                    etImportSn.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.sn_name) + sn + getString(R.string.repeat), Toast.LENGTH_SHORT).show();
                } else {
                    hideKeyboard();
                    doCheckSnByBox();
                }
            }
        }
    }

    private void doCheckSnByPallet() {
        palletInfo = new RmaPalletInfoHelper();
//        palletInfo.setSn(etImportSn.getText().toString().trim());
        palletInfo.setSn(etImportSn_str);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_chking), true);
        print = new PrintLabelHandler();
        new CheckPalletSnInfo().execute(0);
    }

    private void doCheckSnByBox() {
        boxInfo = new RmaBoxInfoHelper();
//        boxInfo.setSn(etImportSn.getText().toString().trim());
        boxInfo.setSn(etImportSn_str);
        checkComp();
        boxInfo.setComp(comp);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_chking), true);
        print = new PrintLabelHandler();
        new CheckBoxSnInfo().execute(0);
    }

    private void checkComp(){
        if(com.get(spinnerCompany.getSelectedItemPosition()).equals("Meraki")){
            comp="5633";
        }else if(com.get(spinnerCompany.getSelectedItemPosition()).equals("Fortinet")){
            comp="5693";
        }
    }

    private void doPrintLabelByPallet(int printerNo) {
        palletInfo = new RmaPalletInfoHelper();
        palletInfo.setPalletNo(etImportPalletNo.getText().toString().trim());
//        palletInfo.setSn(etImportSn.getText().toString().trim());
        palletInfo.setSn(etImportSn_str);
        palletInfo.setCompany(comp);
        palletInfo.setPrintNo(printerNo);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.processing), true);
        print = new PrintLabelHandler();
        new PrintLabelByPallet().execute(0);
    }

    private void doPrintLabelByBox(int printerNo) {
        boxListInfo = new RmaBoxListInfoHelper();
        boxListInfo.setPalletNo(etImportPalletNo.getText().toString().trim());
        boxListInfo.setBoxInfoList(boxInfoList);
        boxListInfo.setCompany(comp);
        boxListInfo.setPrintNo(printerNo);

        if (type == 1)
            boxListInfo.setRma(true);
        else
            boxListInfo.setRma(false);

        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.processing), true);
        print = new PrintLabelHandler();
        new PrintLabelByBox().execute(0);
    }

    private boolean processBoxData() {
        if (boxList.contains(boxInfo.getBoxNo())) { //在A箱中的A序號跟B箱的B序號本來會同箱，某個智障把他拆開放
            Toast.makeText(getApplicationContext(), getString(R.string.box_repeat)+boxInfo.getBoxNo(), Toast.LENGTH_SHORT).show();
            errorInfo = getString(R.string.box_repeat)+boxInfo.getBoxNo();
            return false;
        }

        int tempType;

        if (boxInfo.getWorkOrderNo().substring(2, 3).equals("7")) {
            tempType = 1;
        } else {
            tempType = 2;
        }

        if (boxList.size() == 0) {
            type = tempType;
        } else if (type != tempType) {
            errorInfo = getString(R.string.wo_type_diff);
            return false;
        }

        boxInfoList.add(boxInfo);
//        snList.add(etImportSn.getText().toString().trim());

        etImportSn_str = QrCodeUtil.getSnList(etImportSn.getText().toString().trim());
        snList.add(etImportSn_str);

        boxList.add(boxInfo.getBoxNo());
        mAdapter.notifyDataSetChanged();
        return true;
    }

    private boolean checkData() {
        if (etImportPalletNo.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.Enter_pallet_no), Toast.LENGTH_SHORT).show();
            etImportPalletNo.requestFocus();
            return false;
        }

        if (rbPallet.isChecked()) {
            if (etImportSn.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(),getString(R.string.sn_is_not_null) , Toast.LENGTH_SHORT).show();
                etImportSn.requestFocus();
                return false;
            }
        } else {
            if (snList.isEmpty()) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_sn), Toast.LENGTH_SHORT).show();
                etImportSn.requestFocus();
                return false;
            }
        }

        return true;
    }

    private void cleanData() {
        etImportPalletNo.setText("");
        etImportSn.setText("");
        snList.clear();
        boxList.clear();
        boxInfoList.clear();
        type = 0;
        mAdapter.notifyDataSetChanged();
        tvSummary.setText(getString(R.string.rma_picking_summary, 0, 0));
        etImportPalletNo.requestFocus();
    }

    private void returnPage() {
        finish();
    }

    public void onBackPressed() {
        // do something here and don't write super.onBackPressed()
        returnPage();
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

    private void printLabel() {
        dialog = ProgressDialog.show(this,
                getString(R.string.holdon), getString(R.string.printingLabel), true);

        if (BtPrintLabel.printRmaPalletLabel(com.get(spinnerCompany.getSelectedItemPosition()),//CUSTOMER_NO_MERAKI
            boxListInfo.getPalletId(),
            boxListInfo.getPalletNo(), boxListInfo.getBoxInfoList().length)) {
            dialog.dismiss();
        } else {
            dialog.dismiss();
            Toast.makeText(getApplicationContext(), getString(R.string.printLabalFailed), Toast.LENGTH_LONG).show();
            showReprintDialog();
        }
    }

    private void showReprintDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.print_chk);
        builder.setMessage(R.string.print_error_);
        builder.setCancelable(false);
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                printLabel();
            }
        });
        builder.create().show();
    }

    private class CheckBoxSnInfo extends AsyncTask<Integer, String, RmaBoxInfoHelper> {
        @Override
        protected RmaBoxInfoHelper doInBackground(Integer... params) {
            AppController.debug("Get Shipment Pallet Serial Number Info from "
                    + AppController.getServerInfo()
                    + AppController.getProperties("CheckRmaBoxSnInfo"));
            publishProgress(getString(R.string.downloading_data));
            return print.doCheckRmaBoxSn(boxInfo);
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
        protected void onPostExecute(RmaBoxInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    boxInfo = result;

                    if (processBoxData()) {
                        mConnection.setText("");
                        mConnection.setTextColor(Color.WHITE);
                        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                        errorInfo = "";
                        setSummary();
                    } else {
                        mConnection.setText(getString(R.string.error));
                        mConnection.setTextColor(Color.RED);
                        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    }

                    etImportSn.setText("");
                    etImportSn.requestFocus();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                        etImportSn.setText("");
                        etImportSn.requestFocus();
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

//    private class CheckPalletSnInfo extends
//            AsyncTask<Integer, String, RmaPalletInfoHelper> {
    private class CheckPalletSnInfo extends AsyncTask<Integer, String, RmaPalletInfoHelper> {
        @Override
        protected RmaPalletInfoHelper doInBackground(Integer... params) {
            AppController.debug("Get Shipment Pallet Serial Number Info from "
                    + AppController.getServerInfo()
                    + AppController.getProperties("CheckRmaPalletSnInfo"));
            publishProgress(getString(R.string.downloading_data));
            return print.doCheckRmaPalletSn(palletInfo);
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
        protected void onPostExecute(RmaPalletInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    palletInfo = result;
                    setSummary();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                        etImportSn.setText("");
                        etImportSn.requestFocus();
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

    private class PrintLabelByPallet extends AsyncTask<Integer, String, RmaPalletInfoHelper> {
        @Override
        protected RmaPalletInfoHelper doInBackground(Integer... params) {
            AppController.debug("Print RMA Label By Pallet from "
                    + AppController.getServerInfo()
                    + AppController.getProperties("PrintLabelByPallet"));
            publishProgress(getString(R.string.downloading_data));
            return print.doPrintLabelByPallet(palletInfo);
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
        protected void onPostExecute(RmaPalletInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    palletInfo = result;
                    cleanData();
                    Toast.makeText(getApplicationContext(), getString(R.string.PDF417_label_for_file_has_been_sent_to_file_server), Toast.LENGTH_LONG).show();
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

    private class PrintLabelByBox extends AsyncTask<Integer, String, RmaBoxListInfoHelper> {
        @Override
        protected RmaBoxListInfoHelper doInBackground(Integer... params) {
            AppController.debug("Print RMA Label By Box  from "
                    + AppController.getServerInfo()
                    + AppController.getProperties("PrintLabelByBox"));
            //boxListInfo
            AppController.debug("boxListInfo="+boxListInfo);
            publishProgress(getString(R.string.downloading_data));
            return print.doPrintLabelByBox(boxListInfo);
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
        protected void onPostExecute(RmaBoxListInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("PrintLabelByBox result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    boxListInfo = result;
                    cleanData();
                    Toast.makeText(getApplicationContext(), getString(R.string.PDF417_label_for_Meraki_file_has_been_sent_to_file_server), Toast.LENGTH_LONG).show();
                    printLabel();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error) + ":" + result.getIntRetCode());
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

    //WiFi-Box
    private void doPrintLabelByBox02(int printerNo) {
        boxListInfo = new RmaBoxListInfoHelper();
        boxListInfo.setPalletNo(etImportPalletNo.getText().toString().trim());
        boxListInfo.setBoxInfoList(boxInfoList);
        boxListInfo.setCompany(comp);
        boxListInfo.setPrintNo(printerNo);

        if (type == 1)
            boxListInfo.setRma(true);
        else
            boxListInfo.setRma(false);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.processing), true);
        print = new PrintLabelHandler();
        new PrintLabelByBox02().execute(0);
    }

    //WiFi-Box
    private class PrintLabelByBox02 extends AsyncTask<Integer, String, RmaBoxListInfoHelper> {
        @Override
        protected RmaBoxListInfoHelper doInBackground(Integer... params) {
            AppController.debug("Print RMA Label Via WiFi By Box from "
                    + AppController.getServerInfo()
                    + AppController.getProperties("PrintLabelByBox01"));
            //boxListInfo
            AppController.debug("boxListInfo="+boxListInfo);
            publishProgress(getString(R.string.downloading_data));
            return print.doPrintLabelByBox01(boxListInfo);
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
        protected void onPostExecute(RmaBoxListInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("PrintLabelByBox result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    boxListInfo = result;
                    printLabel();
                    cleanData();

                    if (comp.equals("5633")){ //Meraki
                        onCreateChooseMerakiPrintBoxDialog().show(); //新版WIFI-Meraki
                    }

                    Toast.makeText(getApplicationContext(), "RMA" + getString(R.string.New_version_uses_WiFi_for_printing), Toast.LENGTH_LONG).show();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error) + ":" + result.getIntRetCode());
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

    //WiFi-Box-Meraki
    private Dialog onCreateChooseMerakiPrintBoxDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer, null);
        final EditText etIP = item.findViewById(R.id.editTextIP);
        String ip = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_IP, AppController.getProperties("MerakiPrinterIP"));
        etIP.setText(ip);
        final EditText etPort = item.findViewById(R.id.editTextPort);
        String port = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_PORT, AppController.getProperties("MerakiPrinterPort"));
        etPort.setText(port);
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("MerakiPrinterQty"));
        etQty.setText(qty);

        //HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();
        List<RmaInfoHelper> rmaInfoList = boxListInfo.getRmaInfoList();

        //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
        int sum = 0;
        /*for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
            Pdf417Info_SOPHOS datatmp = entry.getValue();
            int size = datatmp.getSnList().size();
            int l = (int) Math.ceil(size / 70.0);//60個SN一張
            sum += l;
        }*/
        for (RmaInfoHelper info : rmaInfoList) {//撈出全部資料
            int size = info.getSnInfo().length;
            int l = (int) Math.ceil(size / 70.0);//70個SN一張，共?張
            sum += l; //總張數
        }

        int tol=sum*(Integer.parseInt(qty));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the dialog title
        builder.setTitle("Meraki printer info:"+(tol)+" pcs")//   "Samsara印表機資訊 總共"+(tol)+"張"
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
                        if (TextUtils.isEmpty(etIP.getText().toString())) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_id, Toast.LENGTH_LONG).show();
                            etIP.requestFocus();
                            return;
                        }

                        if (TextUtils.isEmpty(etPort.getText().toString())) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_port, Toast.LENGTH_LONG).show();
                            etPort.requestFocus();
                            return;
                        }

                        if (TextUtils.isEmpty(etQty.getText().toString()) || Integer.parseInt(etQty.getText().toString().trim()) <= 0) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (saveMerakiPrinterInfo(etIP.getText().toString().trim(), etPort.getText().toString().trim(), etQty.getText().toString().trim())) {
                            view.setEnabled(false);

                            //HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();
                            List<RmaInfoHelper> rmaInfoList = boxListInfo.getRmaInfoList();

                            //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
                            int sum = 0;
                            /*for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
                                Pdf417Info_SOPHOS datatmp = entry.getValue();
                                int size = datatmp.getSnList().size();
                                int l = (int) Math.ceil(size / 70.0);//70個SN一張
                                sum += l;
                            }*/
                            for (RmaInfoHelper info : rmaInfoList) {//撈出全部資料
                                int size = info.getSnInfo().length;
                                int l = (int) Math.ceil(size / 70.0);//70個SN一張，共?張
                                sum += l; //總張數
                            }
                            //計算總張數--end

                            //列印資料&設定張數
                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {
                                if (rmaInfoList.size() > 0) {
                                    int page = 0;
                                    //for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {
                                    for (RmaInfoHelper info : rmaInfoList) {
                                        String partNo = info.getPartNo();
                                        //Pdf417Info_SOPHOS value = entry.getValue();
                                        String[] snInfo = info.getSnInfo();

                                        AppController.debug("Server response partNo:" + partNo);
                                        AppController.debug("Server response length:" + snInfo.length);
                                        //AppController.debug("Server response value:" + value);
                                        //AppController.debug("Server response value1:" + value);

                                        List<String> snList = new ArrayList<>();
                                        StringBuilder stringBuilder = new StringBuilder();
                                        int cnt = 0;
                                        int cnt2 = 0;
                                        int MAX_CNT = 70;

                                        for (String sn : snInfo) {
//                                        AppController.debug("Server response value3:" + sn);
                                            stringBuilder.append(sn);
                                            cnt++;
                                            cnt2++;

                                            if (cnt == MAX_CNT) { //每張最大數量
                                                cnt = 0;
                                                snList.add(stringBuilder.toString());
                                                stringBuilder.setLength(0);
                                            } else if (cnt2 == snInfo.length) { //剩餘的數量
                                                snList.add(stringBuilder.toString());
                                            } else {
                                                stringBuilder.append(";");
                                            }
                                        }

                                        for (String value1 : snList) {
                                            AppController.debug("Server response value2:" + value1);
                                            AppController.debug("總頁數:" + sum);
                                            int size = value1.split(";").length;
                                            AppController.debug("總數量:" + size);
                                            page++;
                                            AppController.debug("has sn? :" + !"No Numbers".equals(value1));
                                            AppController.debug("info.getQty() :" + info.getQty());
//                                            測試資料:@575755@NA@0@86 => NA
//                                            @576970@10K@0@86 => 110*120*147

                                            if (printMerakiLabelByBox(etIP.getText().toString().trim(), etPort.getText().toString().trim(), info, size, value1, page, sum,1)) {
                                                Toast.makeText(getApplicationContext(), getString(R.string.print_error), Toast.LENGTH_LONG).show();
                                                alertDialog.dismiss();
                                                view.setEnabled(true);
                                                return;
                                            }
                                        }
                                    }
                                } else {

                                }
                            }

                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private boolean saveMerakiPrinterInfo(String ip, String port, String qty) {
        try {
            Preferences.setString(this, Preferences.MERAKI_PRINTER_IP, ip);
            Preferences.setString(this, Preferences.MERAKI_PRINTER_PORT, port);
            Preferences.setString(this, Preferences.MERAKI_PRINTER_QTY, qty);
            Log.d("savePrinterInfo: ", ip + ":" + port);
            return true;
        } catch (Exception e) {
            Log.e("ERROR", e.toString());
            errorInfo = getString(R.string.save_ip_port_qty_error);
            mConnection.setText(R.string.system_error);
            mConnection.setTextColor(Color.RED);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            return false;
        }
    }

    private boolean printMerakiLabelByBox(String ip, String port, RmaInfoHelper boxInfo, int qty, String qrCode, int page, int allpage, int type) {
        dialog = ProgressDialog.show(this,
                getString(R.string.holdon), getString(R.string.printingLabel), true);

        try {
            if (TscEthernetDll == null) {
                TscEthernetDll = new TscWifiActivity();
            }

            TscEthernetDll.openport(ip, Integer.parseInt(port));
            //TscEthernetDll.setup(100, 72, 3, 10, 0, 3, 0);
            TscEthernetDll.setup(100, 76, 3, 10, 0, 3, 0);
            TscEthernetDll.clearbuffer();
            TscEthernetDll.sendcommand("SET TEAR ON\n");
            int x, y, x_end, y_end;

            x = mm2dot(10);
            y = mm2dot(5);
            String st1 = "Part Number:" + boxInfo.getCustomerPartNo();
            //String st1 = "Part Number:" + "MA-RCKMNT-KIT-1";
            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,1,1,\"" + st1 + "\"\n");

            x = mm2dot(37);
            y = mm2dot(13);
            TscEthernetDll.barcode(x, y, "128", 100, 0, 0, 3, 5,  boxInfo.getCustomerPartNo());

            x = mm2dot(10);
            y = mm2dot(18);
            TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, "QTY:"+qty);

            y = mm2dot(27);
            //TscEthernetDll.sendcommand("PDF417 "+ "50,20,400,200,0,E3,\""+qrCode+"\"\n");//特殊QRcode
            TscEthernetDll.sendcommand("PDF417 "+x+","+y+","+"700,300,0,E4,\""+qrCode+"\"\n");//特殊QRcode

            st1 = "Pallet Number:" + boxInfo.getPalletNo();
            x = mm2dot(10);
            y = mm2dot(66);
            TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,1,1,\"" + st1 + "\"\n");

            x = mm2dot(85);
            y = mm2dot(60);
            TscEthernetDll.printerfont(x, y, "5", 0, 1, 1, (page) + "/" + allpage);
            TscEthernetDll.printlabel(1, 1);
            TscEthernetDll.closeport(5000);
            return false;
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            return true;
        } finally {
            dialog.dismiss();
        }
    }

    private int mm2dot(int mm) {
        // 200 DPI，1點=1/8 mm
        // 300 DPI，1點=1/12 mm
        // 200 DPI: 1 mm = 8 dots
        // 300 DPI: 1 mm = 12 dots
        // Alpha3R 200 DPI
        int factor = 12;
        return mm * factor;
    }

    //Codesoft1-Box
    private void doPrintLabelByBox04(int printerNo) {
        boxListInfo = new RmaBoxListInfoHelper();
        boxListInfo.setPalletNo(etImportPalletNo.getText().toString().trim());
        boxListInfo.setBoxInfoList(boxInfoList);
        boxListInfo.setCompany(comp);
        boxListInfo.setPrintNo(printerNo);

        if (type == 1)
            boxListInfo.setRma(true);
        else
            boxListInfo.setRma(false);

        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.processing), true);
        print = new PrintLabelHandler();
        new PrintLabelByBox04().execute(0);
    }

    //Codesoft1-Box
    private class PrintLabelByBox04 extends AsyncTask<Integer, String, RmaBoxListInfoHelper> {
        @Override
        protected RmaBoxListInfoHelper doInBackground(Integer... params) {
            AppController.debug("Print RMA Label Via Codesoft1 By Box from "
                    + AppController.getServerInfo()
                    + AppController.getProperties("PrintLabelByBox01"));
            //boxListInfo
            AppController.debug("boxListInfo="+boxListInfo);
            publishProgress(getString(R.string.downloading_data));
            return print.doPrintLabelByBox01(boxListInfo);
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
        protected void onPostExecute(RmaBoxListInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("PrintLabelByBox result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    boxListInfo = result;
                    printLabel();
                    cleanData();

                    if (comp.equals("5633")){ //Meraki
                        onCreateChooseCodesoft1MerakiPrintBoxDialog().show(); //新版WIFI-Meraki  onCreateChooseCodesoftMerakiPrinterDialog
                    }

                    Toast.makeText(getApplicationContext(), "RMA" + getString(R.string.print_with_codesoft), Toast.LENGTH_LONG).show();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error) + ":" + result.getIntRetCode());
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

    //Codesoft1-Box-Meraki
    private Dialog onCreateChooseCodesoft1MerakiPrintBoxDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer_new, null);
        final Spinner printerIP = item.findViewById(R.id.printspinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,    //對應的Context
                R.array.printersIP,                             //資料選項內容
                android.R.layout.simple_spinner_item);  //預設Spinner未展開時的View(預設及選取後樣式)
        printerIP.setAdapter(adapter);
        printerIP.setSelection(adapter.getPosition("10.0.189.136"));//TSC TX300_1F_217 10.0.162.209
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("MerakiPrinterQty"));
        etQty.setText(qty);

        //HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();
        List<RmaInfoHelper> rmaInfoList = boxListInfo.getRmaInfoList();

        //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
        int sum = 0;
        /*for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
            Pdf417Info_SOPHOS datatmp = entry.getValue();
            int size = datatmp.getSnList().size();
            int l = (int) Math.ceil(size / 70.0);//60個SN一張
            sum += l;
        }*/
        for (RmaInfoHelper info : rmaInfoList) {//撈出全部資料
            int size = info.getSnInfo().length;
            int l = (int) Math.ceil(size / 70.0);//70個SN一張，共?張
            sum += l; //總張數
        }

        int tol=sum*(Integer.parseInt(qty));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the dialog title
        builder.setTitle("Meraki printer info:"+(tol)+" pcs")//   "Samsara印表機資訊 總共"+(tol)+"張"
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
                        if (TextUtils.isEmpty(etQty.getText().toString()) || Integer.parseInt(etQty.getText().toString().trim()) <= 0) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (true) {
                            view.setEnabled(false);

                            //HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();
                            List<RmaInfoHelper> rmaInfoList = boxListInfo.getRmaInfoList();

                            //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
                            int sum = 0;
                            /*for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
                                Pdf417Info_SOPHOS datatmp = entry.getValue();
                                int size = datatmp.getSnList().size();
                                int l = (int) Math.ceil(size / 70.0);//35個SN一張
                                sum += l;
                            }*/
                            for (RmaInfoHelper info : rmaInfoList) {//撈出全部資料
                                int size = info.getSnInfo().length;
                                int l = (int) Math.ceil(size / 70.0);//70個SN一張，共?張
                                sum += l; //總張數
                            }
                            //計算總張數--end

                            //列印資料&設定張數
//                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {
                            //if (data.size() > 0) {
                            if (rmaInfoList.size() > 0) {
                                int page = 0;
                                PrintDataList = new NewPrintDataList();
                                ArrayList<NewPrintData> labelArray = new ArrayList<NewPrintData>();
                                //for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {
                                for (RmaInfoHelper info : rmaInfoList) {
                                    String partNo = info.getPartNo();
                                    //Pdf417Info_SOPHOS value = entry.getValue();
                                    String[] snInfo = info.getSnInfo();
                                    AppController.debug("Server response partNo:" + partNo);
                                    AppController.debug("Server response length:" + snInfo.length);
                                    //AppController.debug("Server response value:" + value);
                                    //AppController.debug("Server response value1:" + value);

                                    List<String> snList = new ArrayList<>();
                                    StringBuilder stringBuilder = new StringBuilder();
                                    int cnt = 0;
                                    int cnt2 = 0;
                                    int MAX_CNT = 70;

                                    for (String sn : snInfo) {
                                        //AppController.debug("Server response value3:" + sn);
                                        stringBuilder.append(sn);
                                        cnt++;
                                        cnt2++;

                                        if (cnt == MAX_CNT) {//每張最大數量
                                            cnt = 0;
                                            snList.add(stringBuilder.toString());
                                            stringBuilder.setLength(0);
                                        } else if (cnt2 == snInfo.length) {//剩餘的數量
                                            snList.add(stringBuilder.toString());
                                        } else {
                                            stringBuilder.append(";");
                                        }
                                    }
//
                                    for (String value1 : snList) {
                                        AppController.debug("Server response value2:" + value1);
                                        AppController.debug("總頁數:" + sum);
                                        int size = value1.split(";").length;
                                        AppController.debug("總數量:" + size);
                                        page++;
                                        AppController.debug("has sn? :" + !"No Numbers".equals(value1));
                                        AppController.debug("info.getQty() :" + info.getQty());
                                        NewPrintData PrintData= new NewPrintData();
                                        PrintData.setLabelName(AppController.getProperties("MerakiLabelName"));
                                        PrintData.setPrintName(printerIP.getSelectedItem().toString());
                                        HashMap<String, String> var = new HashMap<>();
                                        var.put("PartNumber",info.getCustomerPartNo());
                                        var.put("Pallet Num",boxListInfo.getPalletNo());//boxListInfo.getPalletNo()   info.getPalletNo()
                                        var.put("QTY",Integer.toString(size));
                                        var.put("Label Num",page+"/"+sum);
                                        var.put("SN List",info.getModelName()+";"+Integer.toString(size)+";"+value1);
                                        PrintData.setVariables(var);

//                                        Gson gson = new GsonBuilder().create();
//                                        JsonElement element = gson.toJsonTree(PrintData);
                                        labelArray.add(PrintData);
//                                        測試資料:@575755@NA@0@86 => NA
//                                        @576970@10K@0@86 => 110*120*147
                                    }
                                }

                                PrintDataList.setCount(etQty.getText().toString().trim());
                                PrintDataList.setLabel(labelArray);
                                new PrintWithNewType().execute(0);
                            } else {

                            }
//                            }

                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private class PrintWithNewType extends AsyncTask<Integer, String, String> {
        @Override
        protected String doInBackground(Integer... params) {
            AppController.debug("PrintWithNewType:"
                    + AppController.getProperties("NewCodesoftPrint"));
            publishProgress(getString(R.string.processing));
            String result = "";
            // 創建 HttpClient 對象
            HttpClient httpClient = new DefaultHttpClient();
            httpClient.getConnectionManager().getSchemeRegistry().register(
                    new Scheme("https", SSLSocketFactory.getSocketFactory(), 443)
            );

            String jason =new Gson().toJson(PrintDataList);
            AppController.debug("Print jason: "+ jason);
            // 創建 HttpClient 對象
            try {
                // 創建 HttpPost 對象並設置 URL
                HttpPost httpPost = new HttpPost(AppController.getProperties("NewCodesoftPrint"));
                // 設置請求的 JSON 數據
                StringEntity stringEntity = new StringEntity(jason);
                httpPost.setEntity(stringEntity);
                // 設置 Content-Type 頭部
                httpPost.setHeader("Content-Type", "application/json");
                // 執行 POST 請求
                HttpResponse response = httpClient.execute(httpPost);
                // 獲取響應
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    // 讀取響應內容
                    result = EntityUtils.toString(entity);
                    // 在這裡處理返回的結果
                }

                Thread.sleep(1000);
                AppController.debug("Print result: "+ result);
            } catch (Exception e) {
                e.printStackTrace();
                AppController.debug("Print result Exception: "+ e.getMessage());
                return e.getMessage();
            }

            httpClient.getConnectionManager().shutdown();
            return result;
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("Printing " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.contains("OK")) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                } else {
                    mConnection.setText(getString(R.string.db_return_error));
                    errorInfo = result;
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

    //Codesoft2-Box
    private void doPrintLabelByBox05(int printerNo) {
        boxListInfo = new RmaBoxListInfoHelper();
        boxListInfo.setPalletNo(etImportPalletNo.getText().toString().trim());
        boxListInfo.setBoxInfoList(boxInfoList);
        boxListInfo.setCompany(comp);
        boxListInfo.setPrintNo(printerNo);

        if (type == 1)
            boxListInfo.setRma(true);
        else
            boxListInfo.setRma(false);

        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.processing), true);
        print = new PrintLabelHandler();
        new PrintLabelByBox05().execute(0);
    }

    //Codesoft2-Box
    private class PrintLabelByBox05 extends AsyncTask<Integer, String, RmaBoxListInfoHelper> {
        @Override
        protected RmaBoxListInfoHelper doInBackground(Integer... params) {
            AppController.debug("Print RMA Label Via Codesoft1 By Box from "
                    + AppController.getServerInfo()
                    + AppController.getProperties("PrintLabelByBox01"));
            //boxListInfo
            AppController.debug("boxListInfo="+boxListInfo);
            publishProgress(getString(R.string.downloading_data));
            return print.doPrintLabelByBox01(boxListInfo);
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
        protected void onPostExecute(RmaBoxListInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("PrintLabelByBox result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    boxListInfo = result;
                    printLabel();
                    cleanData();

                    if (comp.equals("5633")){ //Meraki
                        onCreateChooseCodesoft2MerakiPrintBoxDialog().show(); //新版WIFI-Meraki  onCreateChooseCodesoftMerakiPrinterDialog
                    }

                    Toast.makeText(getApplicationContext(), "RMA" + getString(R.string.print_with_codesoft), Toast.LENGTH_LONG).show();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error) + ":" + result.getIntRetCode());
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

    //Codesoft2-Box-Meraki
    private Dialog onCreateChooseCodesoft2MerakiPrintBoxDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer_new, null);
        final Spinner printerIP = item.findViewById(R.id.printspinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,    //對應的Context
                R.array.printersIP,                             //資料選項內容
                android.R.layout.simple_spinner_item);  //預設Spinner未展開時的View(預設及選取後樣式)
        printerIP.setAdapter(adapter);
        printerIP.setSelection(adapter.getPosition("10.0.189.136"));//TSC TX300_1F_217 10.0.162.209
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.SOPHOS_PRINTER_QTY, AppController.getProperties("MerakiPrinterQty"));
        etQty.setText(qty);

        //HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();
        List<RmaInfoHelper> rmaInfoList = boxListInfo.getRmaInfoList();

        //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
        int sum = 0;
        /*for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
            Pdf417Info_SOPHOS datatmp = entry.getValue();
            int size = datatmp.getSnList().size();
            int l = (int) Math.ceil(size / 70.0);//60個SN一張
            sum += l;
        }*/
        for (RmaInfoHelper info : rmaInfoList) {//撈出全部資料
            int size = info.getSnInfo().length;
            int l = (int) Math.ceil(size / 70.0);//70個SN一張，共?張
            sum += l; //總張數
        }

        int tol=sum*(Integer.parseInt(qty));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the dialog title
        builder.setTitle("Meraki printer info:"+(tol)+" pcs") //   "Meraki印表機資訊 總共"+(tol)+"張"
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
                        if (TextUtils.isEmpty(etQty.getText().toString()) || Integer.parseInt(etQty.getText().toString().trim()) <= 0) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (true) {
                            view.setEnabled(false);

                            //HashMap<String, Pdf417Info_SOPHOS> data = palletSnInfo.getHt();
                            List<RmaInfoHelper> rmaInfoList = boxListInfo.getRmaInfoList();

                            //計算總張數 sn超過35個+1張 料號非14或09開頭的不算    No Numbers
                            int sum = 0;
                            /*for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {//撈出全部資料
                                Pdf417Info_SOPHOS datatmp = entry.getValue();
                                int size = datatmp.getSnList().size();
                                int l = (int) Math.ceil(size / 70.0);//35個SN一張
                                sum += l;
                            }*/
                            for (RmaInfoHelper info : rmaInfoList) {//撈出全部資料
                                int size = info.getSnInfo().length;
                                int l = (int) Math.ceil(size / 70.0);//70個SN一張，共?張
                                sum += l; //總張數
                            }
                            //計算總張數--end

                            //列印資料&設定張數
//                            for (int i = 0; i < Integer.parseInt(etQty.getText().toString().trim()); i++) {
                            if (rmaInfoList.size() > 0) {
                                int page = 0;
                                PrintDataList = new NewPrintDataList();
                                ArrayList<NewPrintData> labelArray = new ArrayList<NewPrintData>();

                                //for (Map.Entry<String, Pdf417Info_SOPHOS> entry : data.entrySet()) {
                                for (RmaInfoHelper info : rmaInfoList) {
                                    String partNo = info.getPartNo();
                                    //Pdf417Info_SOPHOS value = entry.getValue();
                                    String[] snInfo = info.getSnInfo();
                                    AppController.debug("Server response partNo:" + partNo);
                                    AppController.debug("Server response length:" + snInfo.length);
                                    //AppController.debug("Server response value:" + value);
                                    //AppController.debug("Server response value1:" + value);
                                    List<String> snList = new ArrayList<>();
                                    StringBuilder stringBuilder = new StringBuilder();
                                    int cnt = 0;
                                    int cnt2 = 0;
                                    int MAX_CNT = 70;

                                    for (String sn : snInfo) {
//                                        AppController.debug("Server response value3:" + sn);
                                        stringBuilder.append(sn);
                                        cnt++;
                                        cnt2++;

                                        if (cnt == MAX_CNT) {//每張最大數量
                                            cnt = 0;
                                            snList.add(stringBuilder.toString());
                                            stringBuilder.setLength(0);
                                        } else if (cnt2 == snInfo.length) {//剩餘的數量
                                            snList.add(stringBuilder.toString());
                                        } else {
                                            stringBuilder.append(";");
                                        }
                                    }
//
                                    for (String value1 : snList) {
                                        AppController.debug("Server response value2:" + value1);
                                        AppController.debug("總頁數:" + sum);
                                        int size = value1.split(";").length;
                                        AppController.debug("總數量:" + size);
                                        page++;
                                        AppController.debug("has sn? :" + !"No Numbers".equals(value1));
                                        AppController.debug("info.getQty() :" + info.getQty());
                                        NewPrintData PrintData = new NewPrintData();
                                        PrintData.setLabelName(AppController.getProperties("MerakiLabelName02"));
                                        PrintData.setPrintName(printerIP.getSelectedItem().toString());
                                        HashMap<String, String> var=new HashMap<>();
                                        var.put("PartNumber",info.getCustomerPartNo());
                                        var.put("Pallet Num",boxListInfo.getPalletNo());
                                        var.put("QTY",Integer.toString(size));
                                        var.put("Label Num",page+"/"+sum);
                                        var.put("SN List",info.getModelName()+";"+Integer.toString(size)+";"+value1);
                                        PrintData.setVariables(var);

//                                        Gson gson = new GsonBuilder().create();
//                                        JsonElement element = gson.toJsonTree(PrintData);
                                        labelArray.add(PrintData);
//                                        測試資料:@575755@NA@0@86 => NA
//                                        @576970@10K@0@86 => 110*120*147
                                    }
                                }

                                PrintDataList.setCount(etQty.getText().toString().trim());
                                PrintDataList.setLabel(labelArray);
                                new PrintWithNewType02().execute(0);
                            } else {

                            }
//                            }

                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private class PrintWithNewType02 extends AsyncTask<Integer, String, String> {
        @Override
        protected String doInBackground(Integer... params) {
            AppController.debug("PrintWithNewType02:"
                    + AppController.getProperties("NewCodesoftPrint02"));
            publishProgress(getString(R.string.processing));
            String result = "";
            // 創建 HttpClient 對象
            HttpClient httpClient = new DefaultHttpClient();
            httpClient.getConnectionManager().getSchemeRegistry().register(
                    new Scheme("https", SSLSocketFactory.getSocketFactory(), 443)
            );

            String jason =new Gson().toJson(PrintDataList);
            AppController.debug("Print02 jason: "+ jason);
            // 創建 HttpClient 對象
            try {
                // 創建 HttpPost 對象並設置 URL
                HttpPost httpPost = new HttpPost(AppController.getProperties("NewCodesoftPrint02"));
                // 設置請求的 JSON 數據
                StringEntity stringEntity = new StringEntity(jason);
                httpPost.setEntity(stringEntity);
                // 設置 Content-Type 頭部
                httpPost.setHeader("Content-Type", "application/json");
                // 執行 POST 請求
                HttpResponse response = httpClient.execute(httpPost);
                // 獲取響應
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    // 讀取響應內容
                    result = EntityUtils.toString(entity);
                    // 在這裡處理返回的結果
                }

                Thread.sleep(1000);
                AppController.debug("Print02 result: "+ result);
            } catch (Exception e) {
                e.printStackTrace();
                AppController.debug("Print02 result Exception: "+ e.getMessage());
                return e.getMessage();
            }

            httpClient.getConnectionManager().shutdown();
            return result;
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("Printing " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.contains("OK")) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                } else {
                    mConnection.setText(getString(R.string.db_return_error));
                    errorInfo = result;
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
