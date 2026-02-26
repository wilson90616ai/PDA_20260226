package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.print.BtPrintLabel;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.MaterialLabelHelper;
import com.senao.warehouse.database.PnItemInfoHelper;
import com.senao.warehouse.database.SendOnHandQtyInfo;
import com.senao.warehouse.database.SendProcessInfoHelper;
import com.senao.warehouse.database.SendSubinventoryInfo;
import com.senao.warehouse.database.SubinventoryInfoHelper;
import com.senao.warehouse.handler.PrintLabelHandler;
import com.senao.warehouse.handler.SendingHandler;
import com.senao.warehouse.handler.StockInHandler;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.util.Util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
//製令發料轉倉作業
public class MaterialSendingSubinventoryTransferActivity extends Activity {

    static final Collection<String> EXCLUDE_SUBINVENTORY;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PRINTER_SETTING = 2;
    private static final int REQUEST_BLUETOOTH_SETTINGS = 3;
    private static final String TAG = MaterialSendingSubinventoryTransferActivity.class.getSimpleName();

    //排除310,318倉
    static {
        EXCLUDE_SUBINVENTORY = new ArrayList<>(2);
        EXCLUDE_SUBINVENTORY.add("304");
        EXCLUDE_SUBINVENTORY.add("320");
        EXCLUDE_SUBINVENTORY.add("Stage");
        EXCLUDE_SUBINVENTORY.add("310");
        EXCLUDE_SUBINVENTORY.add("318");
        EXCLUDE_SUBINVENTORY.add("322");
        EXCLUDE_SUBINVENTORY.add("326");
    }

    private TextView mConnection;
    private Button btnReturn, btnConfim, btnCancel;
    private ProgressDialog dialog;
    private String errorInfo = "";
    private TextView txtPartNo, txtLineNumber, txtTransferQty, txtTransferNo, labelMergeWoNumber, txtMergeWoNumber;
    private LinearLayout lQuestionLayout, lStockLayout;
    private EditText txtSubinventory, txtLocator;
    private LayoutType mode = LayoutType.NONE;
    private SubinventoryInfoHelper subinventoryHelper;
    private SendProcessInfoHelper processInfoHelper;
    private SendingHandler sendingHandler;
    private PrintLabelHandler print;
    private SendOnHandQtyInfo onHandQtyInfo;
    private PnItemInfoHelper pnInfo;
    private StockInHandler stockInHandler;
    private boolean isDoTransfer = false;
    private MaterialLabelHelper mlInfo;
    private MaterialLabelHelper subMlInfo;
    private String coo = ""; //20260122 Ann Add

    private OnKeyListener keyListener = new OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Perform action on key press
                Log.d(TAG, "onKeyListener Enter or Action Down" + String.valueOf(v.getId()));

                if (v.getId() == R.id.edittext_import_subinventory) {
                    txtLocator.selectAll();
                } else {
                    hideKeyboard();
                }
            }

            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_sending_subinventory_transfer);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            onHandQtyInfo = new Gson().fromJson(extras.getString("ON_HAND_QTY_INFO"), SendOnHandQtyInfo.class);
            if (onHandQtyInfo == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_stock), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            processInfoHelper = new Gson().fromJson(extras.getString("PROCESS_INFO"), SendProcessInfoHelper.class);
            if (processInfoHelper == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_debit_data), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            processInfoHelper.setTransQty(processInfoHelper.getQty().subtract(onHandQtyInfo.getUnsentQty()));
            processInfoHelper.setQty(onHandQtyInfo.getUnsentQty());

            coo = getIntent().getStringExtra("COO"); //20260122 Ann Add

            sendingHandler = new SendingHandler();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_stock), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MaterialSendingSubinventoryTransferActivity.this);
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

        txtLineNumber = findViewById(R.id.txt_line_number);
        txtPartNo = findViewById(R.id.txt_part_no);
        labelMergeWoNumber = findViewById(R.id.label_merge_wo_number);
        txtMergeWoNumber = findViewById(R.id.txt_merge_wo_number);
        txtTransferQty = findViewById(R.id.txt_subinventory_transfer_qty);
        txtTransferNo = findViewById(R.id.txt_subinventory_transfer_no);

        lStockLayout = findViewById(R.id.stockLayout);
        txtSubinventory = findViewById(R.id.edittext_import_subinventory);
        txtLocator = findViewById(R.id.edittext_locator);

        txtSubinventory.setOnKeyListener(keyListener);

        lQuestionLayout = findViewById(R.id.questionLayout);

        btnReturn = findViewById(R.id.button_return);
        btnConfim = findViewById(R.id.button_confirm);
        btnCancel = findViewById(R.id.button_cancel);

        setQuantityTitle();

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Return", String.valueOf(v.getId()));
                returnPage();
            }
        });

        btnConfim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Confim", String.valueOf(v.getId()));

                if (lQuestionLayout.getVisibility() == View.VISIBLE) {
                    showLayout(LayoutType.STOCK_INFO);
                } else {
                    if (txtSubinventory.getText().toString().trim().equals("")) {
                        Toast.makeText(MaterialSendingSubinventoryTransferActivity.this, getString(R.string.enter_sub), Toast.LENGTH_SHORT).show();
                        txtSubinventory.requestFocus();
                    } else if (EXCLUDE_SUBINVENTORY.contains(txtSubinventory.getText().toString().trim())) {
                        Toast.makeText(getApplicationContext(), getString(R.string.this_sub_is_controlled_cant_used), Toast.LENGTH_SHORT).show();
                        txtSubinventory.requestFocus();
                    } else if (txtLocator.getText().toString().trim().equals("")) {
                        Toast.makeText(MaterialSendingSubinventoryTransferActivity.this, getString(R.string.enter_loc), Toast.LENGTH_SHORT).show();
                        txtLocator.requestFocus();
                    } else if (txtLocator.getText().toString().trim().equals(Constant.LOCATOR_P023)) {
                        Toast.makeText(MaterialSendingSubinventoryTransferActivity.this, getString(R.string.enter_other_loc), Toast.LENGTH_SHORT).show();
                        txtLocator.requestFocus();
                    } else if (processInfoHelper.getLocator().equals(txtLocator.getText().toString().trim())) {
                        Toast.makeText(MaterialSendingSubinventoryTransferActivity.this, getString(R.string.target_loc_cant_be_the_same_as_source_loc), Toast.LENGTH_SHORT).show();
                        txtLocator.requestFocus();
                    } else {
                        hideKeyboard();
                        isDoTransfer = true;
                        doCheckSubinventoryAndLocator(txtSubinventory.getText().toString().trim(), txtLocator.getText().toString().trim());
                    }
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Cancel", String.valueOf(v.getId()));

                if (lQuestionLayout.getVisibility() == View.VISIBLE) {
                    isDoTransfer = false;
                    doSendingProcess();
                } else {
                    cleanData();
                }
            }
        });

        showLayout(LayoutType.QUESTION);
        doQueryPN();
    }

    private void setQuantity() {
        BigDecimal quantity = processInfoHelper.getQty();
        onHandQtyInfo.setSentQty(onHandQtyInfo.getSentQty().add(quantity));
        onHandQtyInfo.setUnsentQty(onHandQtyInfo.getUnsentQty().subtract(quantity));

        for (SendSubinventoryInfo info : onHandQtyInfo.getSubinventoryInfoList()) {
            if (info.getSubinventory().equals(processInfoHelper.getSubinventory())
                    && info.getLocator().equals(processInfoHelper.getLocator())
                    && info.getInventory().doubleValue() > 0) {
                if (info.getInventory().doubleValue() >= quantity.doubleValue()) {
                    info.setInventory(info.getInventory().subtract(quantity));
                    quantity = BigDecimal.valueOf(0);
                } else {
                    quantity = quantity.subtract(info.getInventory());
                    info.setInventory(BigDecimal.valueOf(0));
                }

                if (quantity.doubleValue() <= 0)
                    break;
            }
        }
    }

    private void setQuantityForTransfer() {
        BigDecimal quantity = processInfoHelper.getTransQty();

        for (SendSubinventoryInfo info : onHandQtyInfo.getSubinventoryInfoList()) {
            if (info.getSubinventory().equals(processInfoHelper.getSubinventory())
                    && info.getLocator().equals(processInfoHelper.getLocator())
                    && info.getInventory().doubleValue() > 0) {
                if (info.getInventory().doubleValue() >= quantity.doubleValue()) {
                    info.setInventory(info.getInventory().subtract(quantity));
                    quantity = BigDecimal.valueOf(0);
                } else {
                    quantity = quantity.subtract(info.getInventory());
                    info.setInventory(BigDecimal.valueOf(0));
                }

                if (quantity.doubleValue() <= 0)
                    break;
            }
        }

        boolean exist = false;
        List<SendSubinventoryInfo> list = new ArrayList<>();

        for (SendSubinventoryInfo info : onHandQtyInfo.getSubinventoryInfoList()) {
            list.add(info);

            if (info.getSubinventory().equals(processInfoHelper.getTransSubinventory())
                    && info.getLocator().equals(processInfoHelper.getTransLocator())) {
                info.setInventory(info.getInventory().add(processInfoHelper.getTransQty()));
                exist = true;
            }
        }

        if (!exist) {
            SendSubinventoryInfo info = new SendSubinventoryInfo();
            info.setSubinventory(processInfoHelper.getTransSubinventory());
            info.setLocator(processInfoHelper.getTransLocator());
            info.setDatecode(processInfoHelper.getDateCode());
            info.setInventory(processInfoHelper.getTransQty());
            list.add(info);
            onHandQtyInfo.setSubinventoryInfoList(list);
        }
    }

    private void setQuantityTitle() {
        txtLineNumber.setText(processInfoHelper.getLineNo());
        labelMergeWoNumber.setVisibility(View.VISIBLE);
        txtMergeWoNumber.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(processInfoHelper.getMergeNo())) {
            labelMergeWoNumber.setText(R.string.label_merge_number);
            txtMergeWoNumber.setText(processInfoHelper.getMergeNo());
        } else if (!TextUtils.isEmpty(processInfoHelper.getWoNo())) {
            labelMergeWoNumber.setText(R.string.label_workorder_no);
            txtMergeWoNumber.setText(processInfoHelper.getWoNo());
        } else {
            labelMergeWoNumber.setVisibility(View.INVISIBLE);
            txtMergeWoNumber.setVisibility(View.INVISIBLE);
        }

        txtPartNo.setText(onHandQtyInfo.getPartNo());
        txtTransferQty.setText(Util.fmt(processInfoHelper.getTransQty().doubleValue()));
        txtTransferNo.setText(Util.getTransferNo());
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtSubinventory.getWindowToken(), 0);
    }

    private void cleanData() {
        txtSubinventory.setText("");
        txtLocator.setText("");
        txtSubinventory.requestFocus();
    }

    protected void doCheckSubinventoryAndLocator(String subinventory, String locator) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.chk_sub_locate), true);
        subinventoryHelper = new SubinventoryInfoHelper();
        subinventoryHelper.setPartNo(onHandQtyInfo.getPartNo());
        subinventoryHelper.setSubinventory(subinventory);
        subinventoryHelper.setLocator(locator);
        stockInHandler = new StockInHandler();
        new CheckSubInventory().execute(0);
    }

    protected void doSendingProcess() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.issue_processing), true);
        new DoSendingProcess().execute(0);
    }

    private void doTransfer() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.transfer_processing), true);
        processInfoHelper.setTransSubinventory(txtSubinventory.getText().toString().trim());
        processInfoHelper.setTransLocator(txtLocator.getText().toString().trim());
        new DoTransferProcess().execute(0);
    }

    private void getReelIdList(String partNo, String vendorCode, String dateCode) {
        mlInfo = new MaterialLabelHelper();
        mlInfo.setPartNo(partNo);
        mlInfo.setVendorCode(vendorCode);

        if (dateCode.length() == 4) {
            dateCode = dateCode + vendorCode;
        }
        if (dateCode.length() == 6) {
            dateCode = dateCode.substring(2) + vendorCode;
        }

        mlInfo.setDateCode(dateCode);
        mlInfo.setLabelSize("S");
        mlInfo.setLabelCount(1);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetReelIdList().execute(0);
    }

    private void getReelIdSubList(String partNo, String vendorCode, String dateCode, String seq) {
        subMlInfo = new MaterialLabelHelper();
        subMlInfo.setPartNo(partNo);
        subMlInfo.setVendorCode(vendorCode);

        if (dateCode.length() == 4) {
            dateCode = dateCode + vendorCode;
        }
        if (dateCode.length() == 6) {
            dateCode = dateCode.substring(2) + vendorCode;
        }

        subMlInfo.setDateCode(dateCode);
        subMlInfo.setSeqNo(seq);
        subMlInfo.setLabelCount(1);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetReelIdSubList().execute(0);
    }

    private void printWorkOrderLabel(double quantity) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.printingLabel), true);
        errorInfo = "";
        String reelID;
        String vendorCode;
        String qrCode;
        String po;
        String qty = Util.fmt(quantity);

        if (TextUtils.isEmpty(processInfoHelper.getNewReelID())) {
            if (TextUtils.isEmpty(processInfoHelper.getDateCode())) {
                reelID = "";
                vendorCode = "";
                qrCode = "";
                po = "";
            } else {
                reelID = mlInfo.getReelIds()[0];
                vendorCode = processInfoHelper.getVendorCode();
                po = "";
                qrCode = reelID + "@" + "@" + qty;
            }
        } else {
            reelID = processInfoHelper.getNewReelID();
            vendorCode = processInfoHelper.getVendorCode();

            if (TextUtils.isEmpty(processInfoHelper.getPo())) {
                po = "";

                if(coo.isEmpty()) { //20260122 Ann Add:coo
                    qrCode = reelID + "@" + "@" + qty;
                }else{
                    qrCode = reelID + "@" + "@" + qty + "@@@" + coo;
                }
            } else {
                po = processInfoHelper.getPo();

                if(coo.isEmpty()) { //20260122 Ann Add:coo
                    qrCode = reelID + "@" + processInfoHelper.getPoWithQty() + "@" + qty;
                }else{
                    qrCode = reelID + "@" + processInfoHelper.getPoWithQty() + "@" + qty + "@@@" + coo;
                }
            }
        }

        //if (!BtPrintLabel.printWorkOrderLabel(processInfoHelper.getLineNo(), processInfoHelper.getWoNo(), reelID, processInfoHelper.getPartNo(), pnInfo.getPartDescription(), qty, processInfoHelper.getDateCode(), vendorCode, po, qrCode)) {
        if (!BtPrintLabel.printWorkOrderLabel(processInfoHelper.getLineNo(), processInfoHelper.getWoNo(), reelID, processInfoHelper.getPartNo(), pnInfo.getPartDescription(), qty, processInfoHelper.getDateCode(), vendorCode, po, qrCode, coo)) { //20260122 Ann Add:coo
            errorInfo = getString(R.string.printLabalFailed);
            mConnection.setText(getString(R.string.printer_connect_error));
            mConnection.setTextColor(Color.RED);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        dialog.dismiss();
    }

    private void printInventoryLabel() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.printingLabel), true);
        errorInfo = "";
        String reelID;
        String vendorCode;
        String qrCode;
        String po;
        String qty = Util.fmt(processInfoHelper.getTransQty().doubleValue());

        if (TextUtils.isEmpty(processInfoHelper.getNewReelID())) {
            if (TextUtils.isEmpty(processInfoHelper.getDateCode())) {
                reelID = "";
                vendorCode = "";
                po = "";
                qrCode = "";
            } else {
                reelID = subMlInfo.getReelIds()[0];
                vendorCode = processInfoHelper.getVendorCode();
                po = "";
                qrCode = reelID + "@" + "@" + qty;
            }
        } else {
            reelID = subMlInfo.getReelIds()[0];
            vendorCode = processInfoHelper.getVendorCode();

            if (TextUtils.isEmpty(processInfoHelper.getPo())) {
                po = "";

                if(coo.isEmpty()) { //20260122 Ann Add:coo
                    qrCode = reelID + "@" + "@" + qty;
                }else{
                    qrCode = reelID + "@" + "@" + qty + "@@@" + coo;
                }
            } else {
                po = processInfoHelper.getPo();

                if(coo.isEmpty()) { //20260122 Ann Add:coo
                    qrCode = reelID + "@" + processInfoHelper.getPoWithQty() + "@" + qty;
                }else{
                    qrCode = reelID + "@" + processInfoHelper.getPoWithQty() + "@" + qty + "@@@" + coo;
                }
            }
        }

        //if (!BtPrintLabel.printMaterialLabel(reelID, processInfoHelper.getPartNo(), pnInfo.getPartDescription(), qty, processInfoHelper.getDateCode(), vendorCode, po, qrCode)) {
        if (!BtPrintLabel.printMaterialLabel(reelID, processInfoHelper.getPartNo(), pnInfo.getPartDescription(), qty, processInfoHelper.getDateCode(), vendorCode, po, qrCode, coo)) { //20260122 Ann Add:coo
            errorInfo = getString(R.string.printLabalFailed);
            mConnection.setText(getString(R.string.printer_connect_error));
            mConnection.setTextColor(Color.RED);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        dialog.dismiss();
    }

    private void doQueryPN() {
        pnInfo = new PnItemInfoHelper();
        pnInfo.setPartNo(onHandQtyInfo.getPartNo());
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        print = new PrintLabelHandler();
        new GetPNInfo().execute(0);
    }

    private void showLayout(LayoutType type) {
        mode = type;
        lStockLayout.setVisibility(View.GONE);
        lQuestionLayout.setVisibility(View.GONE);

        switch (type) {
            case STOCK_INFO:
                lStockLayout.setVisibility(View.VISIBLE);
                break;
            case QUESTION:
                lQuestionLayout.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private void returnPage() {
        hideKeyboard();

        switch (mode) {
            case STOCK_INFO:
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
                showLayout(LayoutType.QUESTION);
                break;
            case QUESTION:
                finish();
                break;
            default:
                break;
        }
    }

    public void onBackPressed() {
        // do something here and don't write super.onBackPressed()
        Log.d(TAG, "onClick Hardware Back Button");
        returnPage();
    }

    private void doReturn() {
        Intent resultData = new Intent();
        resultData.putExtra("ON_HAND_QTY_INFO", new Gson().toJson(onHandQtyInfo));
        setResult(RESULT_OK, resultData);
        finish();
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

    enum LayoutType {
        NONE, STOCK_INFO, QUESTION, DC_PN, DC_REEL_ID, NO,
    }

    private class CheckSubInventory extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check Subinventory from " + AppController.getServerInfo()
                    + AppController.getProperties("CheckSubinventory"));
            publishProgress(getString(R.string.downloading_data));
            return stockInHandler.checkSubInventory(subinventoryHelper);
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
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    doSendingProcess();
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

    private class DoSendingProcess extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Do sending process from " + AppController.getServerInfo()
                    + AppController.getProperties("IssueItemNoDc"));
            publishProgress(getString(R.string.issue_processing));
            return sendingHandler.doIssueItemNoDc(processInfoHelper);
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
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    processInfoHelper = (SendProcessInfoHelper) result;
                    Toast.makeText(getApplicationContext(), getString(R.string.debit_ok), Toast.LENGTH_SHORT).show();
                    setQuantity();

                    if (isDoTransfer) {
                        doTransfer();
                    } else {
                        if (TextUtils.isEmpty(processInfoHelper.getNewReelID())) {
                            if (TextUtils.isEmpty(processInfoHelper.getDateCode())) {
                                printInventoryLabel();
                                printWorkOrderLabel(processInfoHelper.getQty().doubleValue());
                                doReturn();
                            } else {
                                if (TextUtils.isEmpty(processInfoHelper.getVendorCode())) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.print_error_msg), Toast.LENGTH_SHORT).show();
                                } else {
                                    getReelIdList(processInfoHelper.getPartNo(), processInfoHelper.getVendorCode(), processInfoHelper.getDateCode());
                                }
                            }
                        } else {
                            getReelIdSubList(processInfoHelper.getPartNo(), processInfoHelper.getVendorCode(), processInfoHelper.getDateCode(), processInfoHelper.getNewReelID().substring(28, 32));
                        }
                    }
                } else {
                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    Toast.makeText(getApplicationContext(), errorInfo, Toast.LENGTH_SHORT).show();

                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class DoTransferProcess extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Do sending process from " + AppController.getServerInfo()
                    + AppController.getProperties("ItemNoTrans"));
            publishProgress(getString(R.string.transfer_processing));
            return sendingHandler.doItemNoTrans(processInfoHelper);
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
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    processInfoHelper = (SendProcessInfoHelper) result;
                    Toast.makeText(getApplicationContext(), getString(R.string.transfer_ok), Toast.LENGTH_SHORT).show();
                    setQuantityForTransfer();

                    if (!TextUtils.isEmpty(processInfoHelper.getNewReelID()) || TextUtils.isEmpty(processInfoHelper.getDateCode())) {
                        printWorkOrderLabel(processInfoHelper.getQty().add(processInfoHelper.getTransQty()).doubleValue());
                        doReturn();
                    } else {
                        if (TextUtils.isEmpty(processInfoHelper.getVendorCode())) {
                            Toast.makeText(getApplicationContext(), getString(R.string.print_error_msg), Toast.LENGTH_SHORT).show();
                            doReturn();
                        } else {
                            getReelIdList(processInfoHelper.getPartNo(), processInfoHelper.getVendorCode(), processInfoHelper.getDateCode());
                        }
                    }
                } else {
                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    Toast.makeText(getApplicationContext(), errorInfo, Toast.LENGTH_SHORT).show();

                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class GetPNInfo extends AsyncTask<Integer, String, PnItemInfoHelper> {
        @Override
        protected PnItemInfoHelper doInBackground(Integer... params) {
            AppController.debug("Get PNInfo from " + AppController.getServerInfo()
                    + AppController.getProperties("GetPNInfo"));
            publishProgress(getString(R.string.downloading_data));
            return print.getPNInfo(pnInfo);
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
        protected void onPostExecute(PnItemInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                pnInfo = result;

                if (pnInfo.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    checkPrinterSetting();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = pnInfo.getStrErrorBuf();
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

    private class GetReelIdList extends AsyncTask<Integer, String, MaterialLabelHelper> {
        @Override
        protected MaterialLabelHelper doInBackground(Integer... params) {
            AppController.debug("Get Reel ID List from " + AppController.getServerInfo()
                    + AppController.getProperties("GetReelIdList"));
            publishProgress(getString(R.string.downloading_data));
            return print.getReelIdList(mlInfo);
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
        protected void onPostExecute(MaterialLabelHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                mlInfo = result;

                if (mlInfo.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";

                    if (isDoTransfer) {
                        printWorkOrderLabel(processInfoHelper.getQty().add(processInfoHelper.getTransQty()).doubleValue());
                        doReturn();
                    } else {
                        getReelIdSubList(processInfoHelper.getPartNo(), processInfoHelper.getVendorCode(), processInfoHelper.getDateCode(), mlInfo.getReelIds()[0].substring(28, 32));
                    }
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = mlInfo.getStrErrorBuf();
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

    private class GetReelIdSubList extends AsyncTask<Integer, String, MaterialLabelHelper> {
        @Override
        protected MaterialLabelHelper doInBackground(Integer... params) {
            AppController.debug("Get Reel ID Sub List from " + AppController.getServerInfo()
                    + AppController.getProperties("GetReelIdSubList"));
            publishProgress(getString(R.string.downloading_data));
            return print.getReelIdSubList(subMlInfo);
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
        protected void onPostExecute(MaterialLabelHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                subMlInfo = result;

                if (subMlInfo.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    printInventoryLabel();
                    printWorkOrderLabel(processInfoHelper.getQty().doubleValue());
                    doReturn();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = subMlInfo.getStrErrorBuf();
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
