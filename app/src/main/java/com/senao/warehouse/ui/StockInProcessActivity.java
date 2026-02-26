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
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import com.senao.warehouse.apiservice.ApiClient.ApiResponse;
import com.senao.warehouse.apiservice.ApiManager.DataRequest;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
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
import com.senao.warehouse.apiservice.ApiClient;
import com.senao.warehouse.apiservice.ApiManager;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.DateCodeInfo;
import com.senao.warehouse.database.FortinetSkuHelper;
import com.senao.warehouse.database.ITEM_LABEL_QR_CODE_FORMAT;
import com.senao.warehouse.database.ItemInfoHelper;
import com.senao.warehouse.database.SerialNoHelper;
import com.senao.warehouse.database.StockInNoHelper;
import com.senao.warehouse.database.StockInNoListHelper;
import com.senao.warehouse.database.StockInProcessInfoHelper;
import com.senao.warehouse.database.SubinventoryInfoHelper;
import com.senao.warehouse.handler.FortinetHandler;
import com.senao.warehouse.handler.StockInHandler;
import com.senao.warehouse.print.BtPrintLabel;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.QrCodeUtil;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StockInProcessActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PRINTER_SETTING = 2;
    private static final int REQUEST_BLUETOOTH_SETTINGS = 3;
    private static final String TAG = StockInProcessActivity.class.getSimpleName();
//    private static String[] NOT_ALLOWED_SUBS = {"304", "320", "Stage", "310", "318", "322", "326", "327"};
    private TextView mConnection,lblTitle;

    private Button btnReturn, btnConfim, btnCancel, btnYes, btnNo;
    private ItemInfoHelper item;
    private ProgressDialog dialog;
    private String errorInfo = "";
    private TextView txtPartNo, txtStockQty, txtPreStockQty, txtWarehousingNo;
    private LinearLayout lPalletLayout,
            lNoLayout, lReelIDLayout, lStockLayout, lQuestionLayout,
            lReceiptNoLayout, lPNLayout, lBoxLayout, lSNLayout, lSnQRCodeLayout, lDcQrCodeLayout;
    private RadioGroup rgDC, rgSN;
    private RadioButton rbReelID, rbPartNo, rbPalletSN, rbBoxSN, rbPalletCN,
            rbBoxCN, rbPallet, rbBox, rbSN, rbSnQRCode, rbDcQrCode;
    private EditText txtSubinventory, txtReelID, txtPalletCN, txtBoxCN,
            txtSNStart, txtSNEnd, txtBoxSN, txtPalletSN, txtNoPN, txtNoAddend,
            txtNoMultiplier, txtNoMultiplicand, txtReelIdMultiplicand,
            txtReelIdAddend, txtReelIdMultiplier, txtLocator, txtReceiptNo,
            txtPnPN, txtPnDC, txtPnMultiplier, txtPnMultiplicand, txtPnAddend, txtSnQrCode, txtDcQrCode;
    private LayoutType mode = LayoutType.NONE;
    private boolean isMerge = true;
    private StockInNoListHelper noListHelper;
    private StockInHandler stockInHandler;
    private SubinventoryInfoHelper subinventoryHelper;
    private StockInNoHelper noHelper;
    private SerialNoHelper serialNoHelper;
    private StockInProcessInfoHelper processInfoHelper;
    private boolean needRefresh = false;
    private RadioButton rbPnEquation;
    private RadioButton rbPnQty;
    private EditText txtPnQty;
    private RadioButton rbReelIdEquation;
    private RadioButton rbReelIdQty;
    private EditText txtReelIdQty;
    private RadioButton rbNoEquation;
    private RadioButton rbNoQty;
    private EditText txtNoQty;
    private String searchText;
    private List<String> palletNoList = new ArrayList<>();
    private Spinner spinnerSubs,spinner_customer,stockLayout_spinner_customer;
    private ArrayAdapter adapterSubs,adapter_costomer;//倉別
    private List<String> sublist = new ArrayList<>();
    private List<String> ErrorSn = new ArrayList<>();
    private List<String> EmptySn = new ArrayList<>();
    private List<String> OkSn = new ArrayList<>();

    private List<FortinetSkuHelper> fortinetSkuHelpers = new ArrayList<>();

    private OnKeyListener keyListener = new OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Perform action on key press
                Log.d(TAG, String.valueOf(v.getId()) + "onKeyListener Enter or Action Down");
                int id = v.getId();

                if (id == R.id.edittext_import_pallet_sn) {
                    if (txtPalletSN.getText().toString().trim().equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.enter_sn), Toast.LENGTH_SHORT).show();
                        txtPalletSN.requestFocus();
                    } else {
                        hideKeyboard();
                        doCheckPallet(txtPalletSN.getText().toString().trim());
                    }
                } else if (id == R.id.edittext_import_pallet_cn) {
                    if (txtPalletCN.getText().toString().trim().equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.enter_box_no), Toast.LENGTH_SHORT).show();
                        txtPalletCN.requestFocus();
                    } else {
                        hideKeyboard();
                        doCheckPalletBoxNo(txtPalletCN.getText().toString().trim());
                    }
                } else if (id == R.id.edittext_import_box_sn) {
                    if (txtBoxSN.getText().toString().trim().equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.enter_sn), Toast.LENGTH_SHORT).show();
                        txtBoxSN.requestFocus();
                    } else {
                        hideKeyboard();
                        doCheckSnBox(txtBoxSN.getText().toString().trim());
                    }
                } else if (id == R.id.edittext_import_box_cn) {
                    if (txtBoxCN.getText().toString().trim().equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.enter_box_no), Toast.LENGTH_SHORT).show();
                        txtBoxCN.requestFocus();
                    } else {
                        hideKeyboard();
                        doCheckBoxNo(txtBoxCN.getText().toString().trim());
                    }
                } else if (id == R.id.edittext_import_pn_pn) {
                    txtPnDC.selectAll();
                } else if (id == R.id.edittext_import_subinventory) {
                    txtLocator.selectAll();
                } else {
                    hideKeyboard();
                }
            }

            return false;
        }

    };

    private CompoundButton.OnCheckedChangeListener noEquationListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbNoQty.setChecked(false);
                txtNoMultiplier.setEnabled(true);
                txtNoMultiplicand.setEnabled(true);
                txtNoAddend.setEnabled(true);
                txtNoMultiplier.requestFocus();
                txtNoQty.setText("");
                txtNoQty.setEnabled(false);
                txtNoPN.setNextFocusDownId(R.id.edittext_no_multiplier);
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener noQtyListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbNoEquation.setChecked(false);
                txtNoMultiplier.setText("");
                txtNoMultiplicand.setText("");
                txtNoAddend.setText("");
                txtNoMultiplier.setEnabled(false);
                txtNoMultiplicand.setEnabled(false);
                txtNoAddend.setEnabled(false);
                txtNoQty.setEnabled(true);
                txtNoQty.requestFocus();
                txtNoPN.setNextFocusDownId(R.id.edittext_no_qty);
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener reelIdEquationListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbReelIdQty.setChecked(false);
                txtReelIdMultiplier.setEnabled(true);
                txtReelIdMultiplicand.setEnabled(true);
                txtReelIdAddend.setEnabled(true);
                txtReelIdMultiplier.requestFocus();
                txtReelIdQty.setText("");
                txtReelIdQty.setEnabled(false);
                txtReelID.setNextFocusDownId(R.id.edittext_reelid_multiplier);
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener reelIdQtyListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbReelIdEquation.setChecked(false);
                txtReelIdMultiplier.setText("");
                txtReelIdMultiplicand.setText("");
                txtReelIdAddend.setText("");
                txtReelIdMultiplier.setEnabled(false);
                txtReelIdMultiplicand.setEnabled(false);
                txtReelIdAddend.setEnabled(false);
                txtReelIdQty.setEnabled(true);
                txtReelIdQty.requestFocus();
                txtReelID.setNextFocusDownId(R.id.edittext_reelid_qty);
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener pnEquationListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbPnQty.setChecked(false);
                txtPnMultiplier.setEnabled(true);
                txtPnMultiplicand.setEnabled(true);
                txtPnAddend.setEnabled(true);
                txtPnMultiplier.requestFocus();
                txtPnQty.setText("");
                txtPnQty.setEnabled(false);
                txtPnDC.setNextFocusDownId(R.id.edittext_pn_multiplier);
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener pnQtyListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbPnEquation.setChecked(false);
                txtPnMultiplier.setText("");
                txtPnMultiplicand.setText("");
                txtPnAddend.setText("");
                txtPnMultiplier.setEnabled(false);
                txtPnMultiplicand.setEnabled(false);
                txtPnAddend.setEnabled(false);
                txtPnQty.setEnabled(true);
                txtPnQty.requestFocus();
                txtPnDC.setNextFocusDownId(R.id.edittext_pn_qty);
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener boxSnListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbBoxCN.setChecked(false);
                txtBoxSN.setEnabled(true);
                txtBoxCN.setText("");
                txtBoxCN.setEnabled(false);
                txtBoxSN.requestFocus();
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener boxCnListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbBoxSN.setChecked(false);
                txtBoxSN.setText("");
                txtBoxSN.setEnabled(false);
                txtBoxCN.setEnabled(true);
                txtBoxCN.requestFocus();
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener palletSnListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbPalletCN.setChecked(false);
                txtPalletSN.setEnabled(true);
                txtPalletCN.setText("");
                txtPalletCN.setEnabled(false);
                txtPalletSN.requestFocus();
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener palletCnListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbPalletSN.setChecked(false);
                txtPalletSN.setText("");
                txtPalletSN.setEnabled(false);
                txtPalletCN.setEnabled(true);
                txtPalletCN.requestFocus();
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener snListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio_pallet:
                    Log.d("onClick Radio Pallet", String.valueOf(checkedId));
                    showLayout(LayoutType.SN_PALLET);
                    break;
                case R.id.radio_box:
                    Log.d("onClick Box", String.valueOf(checkedId));
                    showLayout(LayoutType.SN_BOX);
                    break;
                case R.id.radio_sn:
                    Log.d("onClick Serial Number", String.valueOf(checkedId));
                    showLayout(LayoutType.SN_SN);
                    break;
                case R.id.radio_sn_qrcode:
                    Log.d("onClick QR Code", String.valueOf(checkedId));
                    showLayout(LayoutType.SN_QR_CODE);
                    break;
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener dcListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio_dc_qrcode:
                    if (rbDcQrCode.isChecked())
                        showLayout(LayoutType.DC_QR_CODE);
                    break;
                case R.id.radio_reel_id:
                    if (rbReelID.isChecked())
                        showLayout(LayoutType.DC_REEL_ID);
                    break;
                case R.id.radio_part_number:
                    if (rbPartNo.isChecked()) {
                        showLayout(LayoutType.DC_PN);
                        setDateCode();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_in_process);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            item = new Gson().fromJson(extras.getString("ITEM_INFO"), ItemInfoHelper.class);

            if (item == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_storage), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            searchText = extras.getString("SEARCH_TEXT");
            stockInHandler = new StockInHandler();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_storage), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

//        new FortinetQcApprovalTask().execute("PU234FTF23002068");//PU234FTF23002068

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(StockInProcessActivity.this);
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

        AppController.debug("item:"+item);

        stockLayout_spinner_customer = findViewById(R.id.stockLayout_spinner_customer);
        adapter_costomer = new ArrayAdapter(this, R.layout.spinner_item, getResources().getStringArray(R.array.customer));
        adapter_costomer.setDropDownViewResource(R.layout.spinner_item);
        stockLayout_spinner_customer.setAdapter(adapter_costomer);
        stockLayout_spinner_customer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppController.debug("stockLayout_spinner_customer:"+parent.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(StockInProcessActivity.this,"onNothingSelected"/*這行可直接取得選中內容*/,Toast.LENGTH_SHORT).show();
            }
        });

        spinner_customer = findViewById(R.id.spinner_customer);
        adapter_costomer = new ArrayAdapter(this, R.layout.spinner_item, getResources().getStringArray(R.array.customer));
        adapter_costomer.setDropDownViewResource(R.layout.spinner_item);
        spinner_customer.setAdapter(adapter_costomer);
        spinner_customer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppController.debug("spinner_customer:"+parent.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(StockInProcessActivity.this,"onNothingSelected"/*這行可直接取得選中內容*/,Toast.LENGTH_SHORT).show();
            }
        });

        spinnerSubs = findViewById(R.id.spinnerSubs);
        adapterSubs= new ArrayAdapter<>(this, R.layout.spinner_item, getResources().getStringArray(R.array.subs));
        adapterSubs.setDropDownViewResource(R.layout.spinner_item);
        spinnerSubs.setAdapter(adapterSubs);
        spinnerSubs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppController.debug(parent.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(StockInProcessActivity.this,"onNothingSelected"/*這行可直接取得選中內容*/,Toast.LENGTH_SHORT).show();
            }
        });

//        if(Constant.ISORG){
//            NOT_ALLOWED_SUBS = Constant.GET_NOT_ALLOWED_SUBS.toArray(new String[Constant.GET_NOT_ALLOWED_SUBS.size()]);
//        }

        txtPartNo = findViewById(R.id.label_part_no);
        txtStockQty = findViewById(R.id.textview_stock_quantity);
        txtPreStockQty = findViewById(R.id.label_prestocking_quantity);
        txtWarehousingNo = findViewById(R.id.textview_warehouse_number);

        lStockLayout = findViewById(R.id.stockLayout);
        txtSubinventory = findViewById(R.id.edittext_import_subinventory);
        txtSubinventory.setOnKeyListener(keyListener);
        txtLocator = findViewById(R.id.edittext_locator);
        //txtLocator.setSelectAllOnFocus(true);
        txtLocator.setOnKeyListener(keyListener);

        lQuestionLayout = findViewById(R.id.questionLayout);

        lReceiptNoLayout = findViewById(R.id.receiptNoLayout);
        txtReceiptNo = findViewById(R.id.edittext_receipt_no);

        lPNLayout = findViewById(R.id.pnLayout);
        txtPnPN = findViewById(R.id.edittext_import_pn_pn);
        txtPnDC = findViewById(R.id.edittext_import_pn_dc);
        rbPnEquation = findViewById(R.id.radioButtonPnEquation);
        rbPnEquation.setOnCheckedChangeListener(pnEquationListener);
        txtPnMultiplier = findViewById(R.id.edittext_pn_multiplier);
        txtPnMultiplicand = findViewById(R.id.edittext_pn_multiplicand);
        txtPnAddend = findViewById(R.id.edittext_pn_addend);
        txtPnAddend.setOnKeyListener(keyListener);
        rbPnQty = findViewById(R.id.radioButtonPnQty);
        rbPnQty.setOnCheckedChangeListener(pnQtyListener);
        txtPnQty = findViewById(R.id.edittext_pn_qty);

        txtPnPN.setOnKeyListener(keyListener);

        lDcQrCodeLayout = findViewById(R.id.dcQrCodeLayout);
        txtDcQrCode = findViewById(R.id.edittext_import_dc_qrcode);

        lReelIDLayout = findViewById(R.id.reelIdLayout);
        txtReelID = findViewById(R.id.edittext_import_reel_id);
        rbReelIdEquation = findViewById(R.id.radioButtonReelIdEquation);
        rbReelIdEquation.setOnCheckedChangeListener(reelIdEquationListener);
        txtReelIdMultiplier = findViewById(R.id.edittext_reelid_multiplier);
        txtReelIdMultiplicand = findViewById(R.id.edittext_reelid_multiplicand);
        txtReelIdAddend = findViewById(R.id.edittext_reelid_addend);
        txtReelIdAddend.setOnKeyListener(keyListener);
        rbReelIdQty = findViewById(R.id.radioButtonReelIdQty);
        rbReelIdQty.setOnCheckedChangeListener(reelIdQtyListener);
        txtReelIdQty = findViewById(R.id.edittext_reelid_qty);

        lNoLayout = findViewById(R.id.noLayout);
        txtNoPN = findViewById(R.id.edittext_import_no_pn);
        rbNoEquation = findViewById(R.id.radioButtonNoEquation);
        rbNoEquation.setOnCheckedChangeListener(noEquationListener);
        txtNoMultiplier = findViewById(R.id.edittext_no_multiplier);
        txtNoMultiplicand = findViewById(R.id.edittext_no_multiplicand);
        txtNoAddend = findViewById(R.id.edittext_no_addend);
        txtNoAddend.setOnKeyListener(keyListener);
        rbNoQty = findViewById(R.id.radioButtonNoQty);
        rbNoQty.setOnCheckedChangeListener(noQtyListener);
        txtNoQty = findViewById(R.id.edittext_no_qty);

        lPalletLayout = findViewById(R.id.palletLayout);
        rbPalletSN = findViewById(R.id.radio_pallet_serial_no);
        rbPalletSN.setOnCheckedChangeListener(palletSnListener);
        txtPalletSN = findViewById(R.id.edittext_import_pallet_sn);
        txtPalletSN.setOnKeyListener(keyListener);

        rbPalletCN = findViewById(R.id.radio_pallet_cn);
        rbPalletCN.setOnCheckedChangeListener(palletCnListener);
        txtPalletCN = findViewById(R.id.edittext_import_pallet_cn);
        txtPalletCN.setOnKeyListener(keyListener);

        lBoxLayout = findViewById(R.id.boxLayout);
        rbBoxSN = findViewById(R.id.radio_box_serial_no);
        rbBoxSN.setOnCheckedChangeListener(boxSnListener);
        txtBoxSN = findViewById(R.id.edittext_import_box_sn);
        txtBoxSN.setOnKeyListener(keyListener);

        rbBoxCN = findViewById(R.id.radio_box_cn);
        rbBoxCN.setOnCheckedChangeListener(boxCnListener);
        txtBoxCN = findViewById(R.id.edittext_import_box_cn);
        txtBoxCN.setOnKeyListener(keyListener);

        lSNLayout = findViewById(R.id.snLayout);
        txtSNStart = findViewById(R.id.edittext_import_sn_start);
        txtSNEnd = findViewById(R.id.edittext_import_sn_end);

        lSnQRCodeLayout = findViewById(R.id.snQrCodeLayout);
        txtSnQrCode = findViewById(R.id.edittext_import_sn_qrcode);

        btnReturn = findViewById(R.id.button_return);
        btnConfim = findViewById(R.id.button_confirm);
        btnCancel = findViewById(R.id.button_cancel);
        btnYes = findViewById(R.id.button_yes);
        btnNo = findViewById(R.id.button_no);

        rgDC = findViewById(R.id.radio_group_dc);
        rbDcQrCode = findViewById(R.id.radio_dc_qrcode);
        rbReelID = findViewById(R.id.radio_reel_id);
        rbPartNo = findViewById(R.id.radio_part_number);
        rgDC.setOnCheckedChangeListener(dcListener);

        rgSN = findViewById(R.id.radio_group_sn);
        rbPallet = findViewById(R.id.radio_pallet);
        rbBox = findViewById(R.id.radio_box);
        rbSN = findViewById(R.id.radio_sn);
        rbSnQRCode = findViewById(R.id.radio_sn_qrcode);
        rgSN.setOnCheckedChangeListener(snListener);

        processInfoHelper = new StockInProcessInfoHelper();

        lblTitle = findViewById(R.id.ap_title);
        setTitle();

        txtPartNo.setText(getResources().getString(R.string.label_part_no) + " " + item.getItemID());
        setQuantityTitle();
        txtWarehousingNo.setText("");

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
                switch (mode) {
                    case STOCK_INFO:
//                        if (Constant.ISORG2){
//                            if (txtSubinventory.getText().toString().trim().equals("")) {
//                                Toast.makeText(getApplicationContext(), "請填寫輸入倉別!",
//                                        Toast.LENGTH_SHORT).show();
//                                txtSubinventory.requestFocus();
//                                return;
//                            }

//                            if(!TextUtils.isEmpty(AppController.getOrgName())){
//                                if(!AppController.getOrgName().substring(0,1).equals(txtSubinventory.getText().toString().trim().substring(0,1))||
//                                        !AppController.getOrgName().substring(0,1).equals(txtLocator.getText().toString().trim().substring(0,1))
//                                ){
//                                    Toast.makeText(getApplicationContext(), "此倉別儲位不在"+AppController.getOrgName(),Toast.LENGTH_SHORT).show();
//                                    txtSubinventory.requestFocus();
//                                    return;
//                                }
//                            }else{
//                                Toast.makeText(getApplicationContext(), "找不到ORG!",
//                                        Toast.LENGTH_SHORT).show();
//                                txtSubinventory.requestFocus();
//                                return;
//                            }
//                        }

                        hideKeyboard();
                        doCheckSubinventoryAndLocator(spinnerSubs.getSelectedItem().toString().trim(), txtLocator.getText().toString().trim());

                        //原版
//                        if (txtSubinventory.getText().toString().trim().equals("")) {
//                            Toast.makeText(getApplicationContext(), getString(R.string.enter_sub),
//                                    Toast.LENGTH_SHORT).show();
//                            txtSubinventory.requestFocus();
//                        } else if (Arrays.asList(NOT_ALLOWED_SUBS).contains(txtSubinventory.getText().toString().trim())) {
//                            Toast.makeText(getApplicationContext(), getString(R.string.this_sub_is_controlled_cant_used),
//                                    Toast.LENGTH_SHORT).show();
//                            txtSubinventory.requestFocus();
//                        } else if (txtLocator.getText().toString().trim()
//                                .equals("")) {
//                            Toast.makeText(getApplicationContext(), getString(R.string.enter_loc),
//                                    Toast.LENGTH_SHORT).show();
//                            txtLocator.requestFocus();
//                        } else if (txtLocator.getText().toString().trim()
//                                .equals(Constant.LOCATOR_P023)) {
//                            Toast.makeText(getApplicationContext(), "請填寫它儲位",
//                                    Toast.LENGTH_SHORT).show();
//                            txtLocator.requestFocus();
//                        } else {
//                            hideKeyboard();
//                            doCheckSubinventoryAndLocator(txtSubinventory.getText().toString().trim(),
//                                    txtLocator.getText().toString().trim());
//                        }
                        break;
                    case RECEIPT_NO:
                        if (txtReceiptNo.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_stock_form), Toast.LENGTH_SHORT).show();
                            txtReceiptNo.requestFocus();
                        } else {
                            hideKeyboard();
                            doQueryStockInNo();
                        }
                        break;
                    case DC_PN:
                        if (getPartNo().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_sku), Toast.LENGTH_SHORT).show();
                            txtPnPN.requestFocus();
                        } else if (!getPartNo().equals(item.getItemID())) {
                            Toast.makeText(getApplicationContext(), getString(R.string.sku_incorrect), Toast.LENGTH_SHORT).show();
                            txtPnPN.requestFocus();
                        } else if (getDateCode().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_dc), Toast.LENGTH_SHORT).show();
                            txtPnDC.requestFocus();
                        } else if (rbPnEquation.isChecked()) {
                            if (txtPnMultiplier.getText().toString().trim().equals("")) {
                                Toast.makeText(getApplicationContext(), getString(R.string.enter_multiplier), Toast.LENGTH_SHORT).show();
                                txtPnMultiplier.requestFocus();
                            } else if (txtPnMultiplicand.getText().toString().trim().equals("")) {
                                Toast.makeText(getApplicationContext(), getString(R.string.enter_multiplicand), Toast.LENGTH_SHORT).show();
                                txtPnMultiplicand.requestFocus();
                            } else if (txtPnAddend.getText().toString().trim().equals("")) {
                                Toast.makeText(getApplicationContext(), getString(R.string.enter_add), Toast.LENGTH_SHORT).show();
                                txtPnAddend.requestFocus();
                            } else if (getInputQty(txtPnMultiplier, txtPnMultiplicand, txtPnAddend) > getAvailableQty()) {
                                Toast.makeText(getApplicationContext(), getString(R.string.qty_greater_than_stock_qty), Toast.LENGTH_SHORT).show();
                                txtPnMultiplier.requestFocus();
                            } else if (getInputQty(txtPnMultiplier, txtPnMultiplicand, txtPnAddend) <= 0) {
                                Toast.makeText(getApplicationContext(), getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtPnMultiplier.requestFocus();
                            } else {
                                hideKeyboard();
                                doStockInProcess();
                            }
                        } else {
                            if (txtPnQty.getText().toString().trim().equals("")) {
                                Toast.makeText(getApplicationContext(), getString(R.string.enter_qty), Toast.LENGTH_SHORT).show();
                                txtPnQty.requestFocus();
                            } else if (getInputQty(txtPnQty) > getAvailableQty()) {
                                Toast.makeText(getApplicationContext(), getString(R.string.qty_greater_than_stock_qty), Toast.LENGTH_SHORT).show();
                                txtPnQty.requestFocus();
                            } else if (getInputQty(txtPnQty) <= 0) {
                                Toast.makeText(getApplicationContext(), getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtPnQty.requestFocus();
                            } else {
                                hideKeyboard();
                                doStockInProcess();
                            }
                        }
                        break;
                    case DC_QR_CODE:
                        if (getReelId().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_qrcode), Toast.LENGTH_SHORT).show();
                            txtDcQrCode.requestFocus();
                        } else if (getReelId().length() != 34) {
                            Toast.makeText(getApplicationContext(), getString(R.string.reelid_incorrect_len_must_be_34), Toast.LENGTH_SHORT).show();
                            txtDcQrCode.requestFocus();
                        } else if (!getPartNo().equals(item.getItemID())) {
                            Toast.makeText(getApplicationContext(), getString(R.string.sku_incorrect), Toast.LENGTH_SHORT).show();
                            txtDcQrCode.requestFocus();
                        } else {
                            if (getInputQty(txtDcQrCode) > getAvailableQty()) {
                                Toast.makeText(getApplicationContext(), getString(R.string.qty_greater_than_stock_qty), Toast.LENGTH_SHORT).show();
                                txtDcQrCode.requestFocus();
                            } else if (getInputQty(txtDcQrCode) <= 0) {
                                Toast.makeText(getApplicationContext(), getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtDcQrCode.requestFocus();
                            } else {
                                hideKeyboard();
                                doStockInProcess();
                            }
                        }
                        break;
                    case DC_REEL_ID:
                        if (getReelId().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_reelid), Toast.LENGTH_SHORT).show();
                            txtReelID.requestFocus();
                        } else if (getReelId().length() != 28 && getReelId().length() != 34) {
                            Toast.makeText(getApplicationContext(), getString(R.string.reelid_incorrect_len_must_be_34_or_28), Toast.LENGTH_SHORT).show();
                            txtReelID.requestFocus();
                        } else if (!getPartNo().equals(item.getItemID())) {
                            Toast.makeText(getApplicationContext(), getString(R.string.sku_incorrect), Toast.LENGTH_SHORT).show();
                            txtReelID.requestFocus();
                        } else if (rbReelIdEquation.isChecked()) {
                            if (txtReelIdMultiplier.getText().toString().trim().equals("")) {
                                Toast.makeText(getApplicationContext(), getString(R.string.enter_multiplier), Toast.LENGTH_SHORT).show();
                                txtReelIdMultiplier.requestFocus();
                            } else if (txtReelIdMultiplicand.getText().toString().trim().equals("")) {
                                Toast.makeText(getApplicationContext(), getString(R.string.enter_multiplicand), Toast.LENGTH_SHORT).show();
                                txtReelIdMultiplicand.requestFocus();
                            } else if (txtReelIdAddend.getText().toString().trim().equals("")) {
                                Toast.makeText(getApplicationContext(), getString(R.string.enter_add), Toast.LENGTH_SHORT).show();
                                txtReelIdAddend.requestFocus();
                            } else if (getInputQty(txtReelIdMultiplier, txtReelIdMultiplicand, txtReelIdAddend) > getAvailableQty()) {
                                Toast.makeText(getApplicationContext(), getString(R.string.qty_greater_than_stock_qty), Toast.LENGTH_SHORT).show();
                                txtReelIdMultiplier.requestFocus();
                            } else if (getInputQty(txtReelIdMultiplier, txtReelIdMultiplicand, txtReelIdAddend) <= 0) {
                                Toast.makeText(getApplicationContext(), getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtReelIdMultiplier.requestFocus();
                            } else {
                                hideKeyboard();
                                doStockInProcess();
                            }
                        } else {
                            if (txtReelIdQty.getText().toString().trim().equals("")) {
                                Toast.makeText(getApplicationContext(), getString(R.string.enter_qty), Toast.LENGTH_SHORT).show();
                                txtReelIdQty.requestFocus();
                            } else if (getInputQty(txtReelIdQty) > getAvailableQty()) {
                                Toast.makeText(getApplicationContext(), getString(R.string.qty_greater_than_stock_qty), Toast.LENGTH_SHORT).show();
                                txtReelIdQty.requestFocus();
                            } else if (getInputQty(txtReelIdQty) <= 0) {
                                Toast.makeText(getApplicationContext(), getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtReelIdQty.requestFocus();
                            } else {
                                hideKeyboard();
                                doStockInProcess();
                            }
                        }
                        break;
                    case NO:
                        if (txtNoPN.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_sku), Toast.LENGTH_SHORT).show();
                            txtNoPN.requestFocus();
                        } else if (!txtNoPN.getText().toString().trim().equals(item.getItemID())) {
                            Toast.makeText(getApplicationContext(), getString(R.string.sku_incorrect), Toast.LENGTH_SHORT).show();
                            txtNoPN.requestFocus();
                        } else if (rbNoEquation.isChecked()) {
                            if (txtNoMultiplier.getText().toString().trim().equals("")) {
                                Toast.makeText(getApplicationContext(), getString(R.string.enter_multiplier), Toast.LENGTH_SHORT).show();
                                txtNoMultiplier.requestFocus();
                            } else if (txtNoMultiplicand.getText().toString().trim().equals("")) {
                                Toast.makeText(getApplicationContext(), getString(R.string.enter_multiplicand), Toast.LENGTH_SHORT).show();
                                txtNoMultiplicand.requestFocus();
                            } else if (txtNoAddend.getText().toString().trim().equals("")) {
                                Toast.makeText(getApplicationContext(), getString(R.string.enter_add), Toast.LENGTH_SHORT).show();
                                txtNoAddend.requestFocus();
                            } else if (getInputQty(txtNoMultiplier, txtNoMultiplicand, txtNoAddend) > getAvailableQty()) {
                                Toast.makeText(getApplicationContext(), getString(R.string.qty_greater_than_stock_qty), Toast.LENGTH_SHORT).show();
                                txtNoMultiplier.requestFocus();
                            } else if (getInputQty(txtNoMultiplier, txtNoMultiplicand, txtNoAddend) <= 0) {
                                Toast.makeText(getApplicationContext(), getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtNoMultiplier.requestFocus();
                            } else {
                                hideKeyboard();
                                doStockInProcess();
                            }
                        } else {
                            if (txtNoQty.getText().toString().trim().equals("")) {
                                Toast.makeText(getApplicationContext(), getString(R.string.enter_qty), Toast.LENGTH_SHORT).show();
                                txtNoQty.requestFocus();
                            } else if (getInputQty(txtNoQty) > getAvailableQty()) {
                                Toast.makeText(getApplicationContext(), getString(R.string.qty_greater_than_stock_qty), Toast.LENGTH_SHORT).show();
                                txtNoQty.requestFocus();
                            } else if (getInputQty(txtNoQty) <= 0) {
                                Toast.makeText(getApplicationContext(), getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                                txtNoQty.requestFocus();
                            } else {
                                hideKeyboard();
                                doStockInProcess();
                            }
                        }
                        break;
                    case SN_PALLET:
                        if (rbPalletSN.isChecked() && txtPalletSN.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_sn), Toast.LENGTH_SHORT).show();
                            txtPalletSN.requestFocus();
                        } else if (rbPalletCN.isChecked() && txtPalletCN.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_box_no), Toast.LENGTH_SHORT).show();
                            txtPalletCN.requestFocus();
                        } else if (!rbPalletSN.isChecked() && !rbPalletCN.isChecked()) {
                            Toast.makeText(getApplicationContext(), getString(R.string.choose_sn_box), Toast.LENGTH_SHORT).show();
                        } else {
                            hideKeyboard();
                            doStockInProcess();
                        }
                        break;
                    case SN_BOX:
                        if (rbBoxSN.isChecked() && txtBoxSN.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_sn), Toast.LENGTH_SHORT).show();
                            txtBoxSN.requestFocus();
                        } else if (rbBoxCN.isChecked() && txtBoxCN.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_box_no), Toast.LENGTH_SHORT).show();
                            txtBoxCN.requestFocus();
                        } else if (!rbBoxSN.isChecked() && !rbBoxCN.isChecked()) {
                            Toast.makeText(getApplicationContext(), getString(R.string.choose_sn_box), Toast.LENGTH_SHORT).show();
                        } else {
                            hideKeyboard();
                            doStockInProcess();
                        }
                        break;
                    case SN_SN:
                        if (txtSNStart.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_sn_start), Toast.LENGTH_SHORT).show();
                            txtSNStart.requestFocus();
                        } else if (txtSNEnd.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_sn_end), Toast.LENGTH_SHORT).show();
                            txtSNEnd.requestFocus();
                        } else {
                            hideKeyboard();
                            doStockInProcess();
                        }
                        break;
                    case SN_QR_CODE:
                        if (txtSnQrCode.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_qrcode), Toast.LENGTH_SHORT).show();
                            txtSnQrCode.requestFocus();
                        } else {
                            hideKeyboard();
                            doStockInProcess();
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Cancel", String.valueOf(v.getId()));
                cleanData();
            }
        });

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Yes", String.valueOf(v.getId()));
                // isMerge = true;
                doQueryStockNoList();
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick No", String.valueOf(v.getId()));
                // isMerge = false;
                showLayout(LayoutType.RECEIPT_NO);
            }
        });

        showLayout(LayoutType.STOCK_INFO);
        doQuerySubInventory();
        getFortinetControl();
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.label_stock_in1, AppController.getOrgName()));
        text.setSpan(new RelativeSizeSpan(1f), 0, AppController.getOrgName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        text.setSpan(new ForegroundColorSpan(Color.parseColor("#fa160a")), 0, AppController.getOrgName().length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    protected int getInputQty(EditText edit) {
        if (mode == LayoutType.DC_QR_CODE)
            return Integer.parseInt(QrCodeUtil.getValueFromItemLabelQrCode(edit.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.QTY));
        else
            return Integer.parseInt(edit.getText().toString().trim());
    }

    public String getReelId() {
        if (mode == LayoutType.DC_QR_CODE)
            return QrCodeUtil.getValueFromItemLabelQrCode(txtDcQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID);
        else if (mode == LayoutType.DC_REEL_ID)
            return txtReelID.getText().toString().trim();
        else
            return "";
    }

    public String getPartNo() {
        if (mode == LayoutType.DC_QR_CODE || mode == LayoutType.DC_REEL_ID)
            return getReelId().substring(0, 12);
        else
            return txtPnPN.getText().toString().trim();
    }

    public String getDateCode() {
        if (mode == LayoutType.DC_QR_CODE || mode == LayoutType.DC_REEL_ID)
            return getReelId().substring(18, 28);
        else
            return txtPnDC.getText().toString().trim();
    }

    private void setQuantityTitle() {
        if (isMerge) {
            if (item != null)
                txtStockQty.setText(getResources().getString(
                        R.string.label_stock_quantity, item.getQty(),
                        item.getPass(), item.getWait()));
            else
                txtStockQty.setText(getResources().getString(
                        R.string.label_stock_quantity, 0, 0, 0));
        } else {
            if (noHelper != null && noHelper.getItemInfo() != null)
                txtStockQty.setText(getResources().getString(
                        R.string.label_stock_quantity,
                        noHelper.getItemInfo().getQty(),
                        noHelper.getItemInfo().getPass(),
                        noHelper.getItemInfo().getWait()));
            else
                txtStockQty.setText(getResources().getString(
                        R.string.label_stock_quantity, 0, 0, 0));
        }
    }

    protected int getAvailableQty() {
        if (mode == LayoutType.DC_REEL_ID || mode == LayoutType.DC_PN || mode == LayoutType.DC_QR_CODE) {
            String dateCode = getDateCode();
            int quantity = 0;

            if (isMerge) {
                if (noListHelper != null) {
                    for (StockInNoHelper no : noListHelper.getStockInNoList()) {
                        for (DateCodeInfo info : no.getDateCodeInfo()) {
                            if (info.getDateCode().equals(dateCode) && info.getWait() > 0) {
                                quantity += info.getWait();
                            }
                        }
                    }
                }

                return (quantity == 0) ? item.getWait() : quantity;
            } else {
                if (noHelper != null) {
                    for (DateCodeInfo info : noHelper.getDateCodeInfo()) {
                        if (info.getDateCode().equals(dateCode) && info.getWait() > 0) {
                            quantity += info.getWait();
                        }
                    }
                }

                return (quantity == 0) ? noHelper.getItemInfo().getWait() : quantity;
            }
        } /*else if (mode == LayoutType.DC_PN) {
            if (isMerge) {
                if (noListHelper != null) {
                    for (StockInNoHelper no : noListHelper.getStockInNoList()) {
                        for (DateCodeInfo info : no.getDateCodeInfo()) {
                            if (info.getWait() > 0) {
                                return info.getWait();
                            }
                        }
                    }
                }

                return item.getWait();
            } else {
                if (noHelper != null) {
                    for (DateCodeInfo info : noHelper.getDateCodeInfo()) {
                        if (info.getWait() > 0) {
                            return info.getWait();
                        }
                    }
                }

                return noHelper.getItemInfo().getWait();
            }
        }*/ else {
            if (isMerge) {
                return item.getWait();
            } else
                return noHelper.getItemInfo().getWait();
        }
    }

    private int getInputQty(EditText multiplier, EditText multiplicand, EditText addEnd) {
        return Integer.parseInt(multiplier.getText().toString().trim())
                * Integer.parseInt(multiplicand.getText().toString().trim())
                + Integer.parseInt(addEnd.getText().toString().trim());
    }

    protected void doQueryStockInNo() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.chk_Storage_application_number), true);
        noHelper = new StockInNoHelper();
        noHelper.setNo(txtReceiptNo.getText().toString().trim());
        noHelper.setSearchText(searchText);
        ItemInfoHelper itemInfo = new ItemInfoHelper();
        itemInfo.setItemID(item.getItemID());
        noHelper.setItemInfo(itemInfo);
        new GetStockInNo().execute(0);
    }

    public void setInventoryData(SubinventoryInfoHelper item) {
        if(Constant.ISORG2){
//            txtSubinventory.setText(AppController.getOrgName().substring(0,1)+item.getSubinventory());
//            txtLocator.setText(AppController.getOrgName().substring(0,1)+item.getLocator());

            adapterSubs= new ArrayAdapter<>(this, R.layout.spinner_item, item.getSubinventories());
            spinnerSubs.setAdapter(adapterSubs);
            adapterSubs.notifyDataSetChanged();
        }else{
            txtSubinventory.setText(item.getSubinventory());
            txtLocator.setText(item.getLocator());
            txtLocator.requestFocus();
            txtLocator.selectAll();
        }
    }

    private void doQuerySubInventory() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.reading_sub_lot), true);
        subinventoryHelper = new SubinventoryInfoHelper();
        subinventoryHelper.setPartNo(item.getItemID());
        new GetSubInventory().execute(0);
    }

    private void doQueryStockNoList() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.read_in_stock_data), true);
        noListHelper = new StockInNoListHelper();
        noListHelper.setPartNo(item.getItemID());
        noListHelper.setSearchText(searchText);
        new GetStockInNoList().execute(0);
    }

    private void doCheckSN(String sn) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.sn_checking), true);
        txtPreStockQty.setText("");
        serialNoHelper = new SerialNoHelper();
        serialNoHelper.setPartNo(item.getItemID());

        if (!isMerge)
            serialNoHelper.setStockInNo(noHelper.getNo());

        serialNoHelper.setSerialNo(sn);
        new CheckSN().execute(0);
    }

    private void doCheckPalletBoxNo(String boxNo) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.chk_box_no), true);
        txtPreStockQty.setText("");
        serialNoHelper = new SerialNoHelper();
        serialNoHelper.setPartNo(item.getItemID());

        if (!isMerge)
            serialNoHelper.setStockInNo(noHelper.getNo());

        serialNoHelper.setBoxNo(boxNo);
        new CheckPalletBoxNo().execute(0);
    }

    private void doCheckPallet(String sn) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.sn_checking), true);
        txtPreStockQty.setText("");
        serialNoHelper = new SerialNoHelper();
        serialNoHelper.setPartNo(item.getItemID());

        if (!isMerge)
            serialNoHelper.setStockInNo(noHelper.getNo());

        serialNoHelper.setSerialNo(sn);
        new CheckPallet().execute(0);
    }

    private void doCheckSnBox(String sn) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.sn_checking), true);
        txtPreStockQty.setText("");
        serialNoHelper = new SerialNoHelper();
        serialNoHelper.setPartNo(item.getItemID());

        if (!isMerge)
            serialNoHelper.setStockInNo(noHelper.getNo());

        serialNoHelper.setSerialNo(sn);
        new CheckSnBox().execute(0);
    }

    private void doCheckBoxNo(String boxNo) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.chk_box_no), true);
        serialNoHelper = new SerialNoHelper();
        serialNoHelper.setPartNo(item.getItemID());

        if (!isMerge)
            serialNoHelper.setStockInNo(noHelper.getNo());

        serialNoHelper.setBoxNo(boxNo);
        new CheckBoxNo().execute(0);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtSubinventory.getWindowToken(), 0);
    }

    private void setStockNoData() {
        StringBuilder applyNo = new StringBuilder();

        if (isMerge) {
            StockInNoHelper[] stockList = noListHelper.getStockInNoList();

            for (StockInNoHelper item : stockList) {
                if (item.isSSG()) {
                    applyNo.append(item.getNo()).append("(SSG)").append(" ");
                } else {
                    applyNo.append(item.getNo()).append(" ");
                }
            }

            applyNo = new StringBuilder(applyNo.toString().trim());
            applyNo = new StringBuilder(applyNo.toString().replace(" ", "\n"));
        } else {
            if (noHelper.isSSG()) {
                applyNo = new StringBuilder(txtReceiptNo.getText().toString() + "(SSG)");
            } else {
                applyNo = new StringBuilder(txtReceiptNo.getText().toString());
            }
        }

        txtWarehousingNo.setText(applyNo.toString());
    }

    private void setView() {
        if (item.getControl().equals("SN")) {
            updateRadioButtonsVisibility();

            if (rgSN.getCheckedRadioButtonId() == -1){
//                rgSN.check(R.id.radio_pallet);
                rgSN.check(R.id.radio_sn_qrcode);
            }
        } else if (item.getControl().equals("DC")) {
            if (rgDC.getCheckedRadioButtonId() == -1)
                rgDC.check(R.id.radio_part_number);
        } else
            showLayout(LayoutType.NO);
    }

    private void cleanData() {
        switch (mode) {
            case STOCK_INFO:
                txtSubinventory.setText("");
                txtLocator.setText("");
                txtSubinventory.requestFocus();
                break;
            case RECEIPT_NO:
                txtReceiptNo.setText("");
                txtReceiptNo.requestFocus();
                break;
            case DC_PN:
                txtPnPN.setText("");
                txtPnDC.setText("");
                txtPnMultiplier.setText("");
                txtPnMultiplicand.setText("");
                txtPnAddend.setText("");
                txtPnQty.setText("");
                txtPnPN.requestFocus();
                break;
            case DC_QR_CODE:
                txtDcQrCode.setText("");
                txtDcQrCode.requestFocus();
                break;
            case DC_REEL_ID:
                txtReelID.setText("");
                txtReelIdMultiplier.setText("");
                txtReelIdMultiplicand.setText("");
                txtReelIdAddend.setText("");
                txtReelIdQty.setText("");
                txtReelID.requestFocus();
                break;
            case NO:
                txtNoPN.setText("");
                txtNoMultiplier.setText("");
                txtNoMultiplicand.setText("");
                txtNoAddend.setText("");
                txtNoQty.setText("");
                txtNoPN.requestFocus();
                break;
            case SN_PALLET:
                txtPalletSN.setText("");
                txtPalletSN.requestFocus();
                break;
            case SN_BOX:
                txtBoxSN.setText("");
                txtBoxCN.setText("");
                txtBoxSN.requestFocus();
                break;
            case SN_SN:
                txtSNStart.setText("");
                txtSNEnd.setText("");
                txtSNStart.requestFocus();
                break;
            case SN_QR_CODE:
                txtSnQrCode.setText("");
                txtSnQrCode.requestFocus();
                break;
            default:
                break;
        }
    }

    protected void doCheckSubinventoryAndLocator(String subinventory, String locator) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.chk_sub_locate), true);
        subinventoryHelper = new SubinventoryInfoHelper();
        subinventoryHelper.setPartNo(item.getItemID());
        subinventoryHelper.setSubinventory(subinventory);
        subinventoryHelper.setLocator(locator);
        new CheckSubInventory().execute(0);
    }

    protected void doStockInProcess() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.stock_proccessing), true);
//        processInfoHelper = new StockInProcessInfoHelper();
        processInfoHelper.setPartNo(item.getItemID());
        processInfoHelper.setApplyNo(getApplyNo());
        processInfoHelper.setWorkOrderNo(getWorkOrderNo());
//        if (isSSFG()) {
//            processInfoHelper.setSubinventory("311");
//            processInfoHelper.setLocator("SG01");
//        } else {
//            processInfoHelper.setSubinventory(txtSubinventory.getText().toString().trim());

        if(TextUtils.isEmpty(processInfoHelper.getSubinventory())){
            if(TextUtils.isEmpty(spinnerSubs.getSelectedItem().toString().trim())){
                Toast.makeText(getApplicationContext(), "Subinventory is empty", Toast.LENGTH_SHORT).show();
                return;
            }else{
                processInfoHelper.setSubinventory(spinnerSubs.getSelectedItem().toString().trim());
            }
        }
        if(TextUtils.isEmpty(processInfoHelper.getLocator())){
            if(TextUtils.isEmpty(txtLocator.getText().toString().trim())){
                Toast.makeText(getApplicationContext(), "Locator is empty", Toast.LENGTH_SHORT).show();
                return;
            }else{
                processInfoHelper.setLocator(txtLocator.getText().toString().trim());
            }
        }

//        if(TextUtils.isEmpty(spinnerSubs.getSelectedItem().toString().trim())||TextUtils.isEmpty(txtLocator.getText().toString().trim())){
//            Toast.makeText(getApplicationContext(), "Locator is empty2", Toast.LENGTH_SHORT).show();
//            return;
//        }


//        processInfoHelper.setSubinventory(spinnerSubs.getSelectedItem().toString().trim());
//        processInfoHelper.setLocator(txtLocator.getText().toString().trim());


//        }
        AppController.debug("mode = "+mode+", processInfoHelper = "+processInfoHelper);
        String dateCode;
        String sn;

        switch (mode) {
            case DC_PN:
                processInfoHelper.setDateCode1(getDateCodeFromHelper());
                processInfoHelper.setDateCode2(getDateCode());
                if (rbPnEquation.isChecked())
                    processInfoHelper.setQty(getInputQty(txtPnMultiplier, txtPnMultiplicand, txtPnAddend));
                else
                    processInfoHelper.setQty(getInputQty(txtPnQty));
                break;
            case DC_QR_CODE:
                processInfoHelper.setReelID(getReelId().substring(0, 28));
                dateCode = getDateCode();
                processInfoHelper.setDateCode1(dateCode);
                processInfoHelper.setDateCode2(dateCode);
                processInfoHelper.setQty(getInputQty(txtDcQrCode));
                break;
            case DC_REEL_ID:
                processInfoHelper.setReelID(getReelId().substring(0, 28));
                dateCode = getDateCode();
                processInfoHelper.setDateCode1(dateCode);
                processInfoHelper.setDateCode2(dateCode);
                if (rbReelIdEquation.isChecked())
                    processInfoHelper.setQty(getInputQty(txtReelIdMultiplier, txtReelIdMultiplicand, txtReelIdAddend));
                else
                    processInfoHelper.setQty(getInputQty(txtReelIdQty));
                break;
            case NO:
                if (rbNoEquation.isChecked())
                    processInfoHelper.setQty(getInputQty(txtNoMultiplier, txtNoMultiplicand, txtNoAddend));
                else
                    processInfoHelper.setQty(getInputQty(txtNoQty));
                break;
            case SN_PALLET:
                sn = txtPalletSN.getText().toString().trim();
                processInfoHelper.setSerialNoStart(sn);
                processInfoHelper.setSerialNoEnd(sn);
                processInfoHelper.setBoxNo(txtPalletCN.getText().toString().trim());
                break;
            case SN_BOX:
                sn = txtBoxSN.getText().toString().trim();
                processInfoHelper.setSerialNoStart(sn);
                processInfoHelper.setSerialNoEnd(sn);
                processInfoHelper.setBoxNo(txtBoxCN.getText().toString().trim());
                break;
            case SN_SN:
                processInfoHelper.setSerialNoStart(txtSNStart.getText().toString().trim());
                processInfoHelper.setSerialNoEnd(txtSNEnd.getText().toString().trim());
                break;
            case SN_QR_CODE:

                if (spinner_customer.getSelectedItem().toString().trim().equals("Fortinet")){
                    processInfoHelper.setSnList(getSnList(txtSnQrCode.getText().toString().trim()));
                    FortinetQcApprovalTask approvalTask = new FortinetQcApprovalTask();
                    ErrorSn = new ArrayList<>();
                    EmptySn = new ArrayList<>();

                    try {
                        String snList_putput = "";

                        for(String sn_D : processInfoHelper.getSnList()){
                            snList_putput+=sn_D+",";
                        }

                        snList_putput = snList_putput.substring(0,snList_putput.length()-1);
                        approvalTask.execute(snList_putput);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else if(spinner_customer.getSelectedItem().toString().trim().equals("Samsara")){
                    processInfoHelper.setSnList(getSnListbySamsara(txtSnQrCode.getText().toString().trim()));
                    new DoStockInProcess().execute(0);
                }else if(spinner_customer.getSelectedItem().toString().trim().equals("Extreme")){
                    processInfoHelper.setSnList(getSnListbyExtreme(txtSnQrCode.getText().toString().trim()));
                    new DoStockInProcess().execute(0);
                }else if(spinner_customer.getSelectedItem().toString().trim().equals("Engenius")){
                    processInfoHelper.setSnList(getSnListbyEngenius(txtSnQrCode.getText().toString().trim()));
                    new DoStockInProcess().execute(0);
                }else if(spinner_customer.getSelectedItem().toString().trim().equals("Seasonic")){
                    processInfoHelper.setSnList(getSnListbySeasonic(txtSnQrCode.getText().toString().trim()));
                    new DoStockInProcess().execute(0);
                }else if(spinner_customer.getSelectedItem().toString().trim().equals("Verkada")){
                    processInfoHelper.setSnList(getSnListbyVerkada(txtSnQrCode.getText().toString().trim()));
                    new DoStockInProcess().execute(0);
                }else{
                    processInfoHelper.setSnList(getSnListbyOthers(txtSnQrCode.getText().toString().trim()));
                    new DoStockInProcess().execute(0);
                }

//                if(ErrorSn.size()>0){ //代表有序號不符合規範
//                    list = new ArrayList<>();
//                    Toast.makeText(getApplicationContext(), "請檢查SN是否過QC站:"+ErrorSn.get(0), Toast.LENGTH_LONG).show();
//                }

                break;
            default:
                return;
        }

       if (mode != LayoutType.SN_QR_CODE){
            new DoStockInProcess().execute(0);
       }
    }

    private List<String> getSnList(String qrCode) {
        //parse HW-CM11
        String[] snList = null;

        if (qrCode.indexOf("\r\n") > 0) {
            snList = qrCode.split("\r\n");
        } else if (qrCode.indexOf("\r") > 0) {
            snList = qrCode.split("\r");
        } else if (qrCode.indexOf("\n") > 0) {
            snList = qrCode.split("\n");
        } else if (qrCode.indexOf("[CR][LF]") > 0) {
            snList = qrCode.split("[CR][LF]");
        } else if (qrCode.indexOf("[CR]") > 0) {
            snList = qrCode.split("\\[CR\\]");
        } else if (qrCode.indexOf("[LF]") > 0) {
            snList = qrCode.split("\\[LF\\]");
        } else if (qrCode.indexOf(",") > 0) { // 測試 之後拿掉
            snList = qrCode.split(",");
        } else if (qrCode.indexOf("\u2028") > 0) { // 行分隔符
            snList = qrCode.split("\\u2028");
        } else if (qrCode.indexOf("\u2029") > 0) { // 段落分隔符
            snList = qrCode.split("\\u2029");
        } else if (qrCode.indexOf("\f") > 0) { // 換頁符
            snList = qrCode.split("\f");
        }else  { // 換頁符
//            snList = qrCode.trim();
        }

        List<String> list = new ArrayList<>();

        if (snList == null) {
//            Toast.makeText(getApplicationContext(), getString(R.string.qr_code_format_incorrect_cant_resolve_sn), Toast.LENGTH_LONG).show();
            snList = qrCode.split("\\r\\n|\\r|\\n|\\[CR\\]\\[LF\\]|\\[CR\\]|\\[LF\\]|\\u2028|\\u2029|\\f|,");

            if (snList == null || snList.length == 0) {
                Toast.makeText(getApplicationContext(), getString(R.string.qr_code_format_incorrect_cant_resolve_sn), Toast.LENGTH_LONG).show();
                //snList = qrCode.split("\\r\\n|\\r|\\n|\\[CR\\]\\[LF\\]|\\[CR\\]|\\[LF\\]|\\u2028|\\u2029|\\f|,");
            } else {
                for (int i = 0; i < snList.length; i++) {
                    list.add(snList[i]);
                }
            }
        } else {
//            for (int i = 0; i < snList.length; i++) {
//                list.add(snList[i]);
//            }

            for (String sn : snList) {
                list.add(sn.trim());
            }
        }

        return list;
    }

    private List<String> getSnListbyOthers(String qrCode) {
        //parse HW-CM11
        String[] snList = null;

        if (qrCode.indexOf("\r\n") > 0) {
            snList = qrCode.split("\r\n");
        } else if (qrCode.indexOf("\r") > 0) {
            snList = qrCode.split("\r");
        } else if (qrCode.indexOf("\n") > 0) {
            snList = qrCode.split("\n");
        } else if (qrCode.indexOf("[CR][LF]") > 0) {
            snList = qrCode.split("[CR][LF]");
        } else if (qrCode.indexOf("[CR]") > 0) {
            snList = qrCode.split("\\[CR]");
        } else if (qrCode.indexOf("[LF]") > 0) {
            snList = qrCode.split("\\[LF]");
        } else if (qrCode.indexOf(",") > 0) { //測試 之後拿掉
            snList = qrCode.split(",");
        }

        List<String> list = new ArrayList<>();

        if (snList == null || snList.length < 4) {
            Toast.makeText(getApplicationContext(), getString(R.string.qr_code_format_incorrect_cant_resolve_sn), Toast.LENGTH_LONG).show();
        } else {
            for (int i = 0; i < snList.length; i++) {
                if (i > 2) {
                    list.add(snList[i]);
                }
            }
        }

//        if (snList == null) {
//            Toast.makeText(getApplicationContext(), getString(R.string.qr_code_format_incorrect_cant_resolve_sn), Toast.LENGTH_LONG).show();
//        } else {
//            for (int i = 0; i < snList.length; i++) {
//                list.add(snList[i]);
//            }
//        }

        return list;
    }

    private List<String> getSnListbyExtreme(String qrCode) {
        //parse HW-CM11
        String[] snList = null;

        if (qrCode.indexOf("\r\n") > 0) {
            snList = qrCode.split("\r\n");
        } else if (qrCode.indexOf("\r") > 0) {
            snList = qrCode.split("\r");
        } else if (qrCode.indexOf("\n") > 0) {
            snList = qrCode.split("\n");
        } else if (qrCode.indexOf("[CR][LF]") > 0) {
            snList = qrCode.split("[CR][LF]");
        } else if (qrCode.indexOf("[CR]") > 0) {
            snList = qrCode.split("\\[CR]");
        } else if (qrCode.indexOf("[LF]") > 0) {
            snList = qrCode.split("\\[LF]");
        } else if (qrCode.indexOf(",") > 0) { //測試 之後拿掉
            snList = qrCode.split(",");
        }

        List<String> list = new ArrayList<>();

        try {
            list.add(snList[0].split(":")[1]);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), getString(R.string.qr_code_format_incorrect_cant_resolve_sn), Toast.LENGTH_LONG).show();
        }

        return list;
    }

    private List<String> getSnListbyEngenius(String qrCode) {
        //parse HW-CM11
        String[] snList = null;

        if (qrCode.indexOf("\r\n") > 0) {
            snList = qrCode.split("\r\n");
        } else if (qrCode.indexOf("\r") > 0) {
            snList = qrCode.split("\r");
        } else if (qrCode.indexOf("\n") > 0) {
            snList = qrCode.split("\n");
        } else if (qrCode.indexOf("[CR][LF]") > 0) {
            snList = qrCode.split("[CR][LF]");
        } else if (qrCode.indexOf("[CR]") > 0) {
            snList = qrCode.split("\\[CR]");
        } else if (qrCode.indexOf("[LF]") > 0) {
            snList = qrCode.split("\\[LF]");
        } else if (qrCode.indexOf(",") > 0) { //測試 之後拿掉
            snList = qrCode.split(",");
        }

        List<String> list = new ArrayList<>();

        try {
            for (String s : snList) {
                list.add(s.split(";")[0]);
            }
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), getString(R.string.qr_code_format_incorrect_cant_resolve_sn), Toast.LENGTH_LONG).show();
        }

        return list;
    }

    private List<String> getSnListbySeasonic(String qrCode) {
        //parse HW-CM11
        String[] snList = null;

        if (qrCode.indexOf("\r\n") > 0) {
            snList = qrCode.split("\r\n");
        } else if (qrCode.indexOf("\r") > 0) {
            snList = qrCode.split("\r");
        } else if (qrCode.indexOf("\n") > 0) {
            snList = qrCode.split("\n");
        } else if (qrCode.indexOf("[CR][LF]") > 0) {
            snList = qrCode.split("[CR][LF]");
        } else if (qrCode.indexOf("[CR]") > 0) {
            snList = qrCode.split("\\[CR]");
        } else if (qrCode.indexOf("[LF]") > 0) {
            snList = qrCode.split("\\[LF]");
        } else if (qrCode.indexOf(",") > 0) { //測試 之後拿掉
            snList = qrCode.split(",");
        }

        List<String> list = new ArrayList<>();
        boolean startGetSn = false;

        for (String s : snList) {
            if (s.contains("Seria Nr.")) {
                startGetSn = true;
            } else {
                if (startGetSn) {
                    if (s.contains("C/No")) {
                        break;
                    } else {
                        list.add(s);
                    }
                }
            }
        }

        return list;
    }

    private List<String> getSnListbyVerkada(String qrCode) {
        //parse HW-CM11
        String[] snList = null;

        if (qrCode.indexOf("\r\n") > 0) {
            snList = qrCode.split("\r\n");
        } else if (qrCode.indexOf("\r") > 0) {
            snList = qrCode.split("\r");
        } else if (qrCode.indexOf("\n") > 0) {
            snList = qrCode.split("\n");
        } else if (qrCode.indexOf("[CR][LF]") > 0) {
            snList = qrCode.split("[CR][LF]");
        } else if (qrCode.indexOf("[CR]") > 0) {
            snList = qrCode.split("\\[CR]");
        } else if (qrCode.indexOf("[LF]") > 0) {
            snList = qrCode.split("\\[LF]");
        } else if (qrCode.indexOf(",") > 0) { //測試 之後拿掉
            snList = qrCode.split(",");
        }

        List<String> list = new ArrayList<>();
        snList = snList[0].split(",");

        try {
            list.addAll(Arrays.asList(snList));
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), getString(R.string.qr_code_format_incorrect_cant_resolve_sn), Toast.LENGTH_LONG).show();
        }

        return list;
    }

    private List<String> getSnListbySamsara(String qrCode) {
        //parse HW-CM11
        String[] snList = null;

        if (qrCode.indexOf("\r\n") > 0) {
            snList = qrCode.split("\r\n");
        } else if (qrCode.indexOf("\r") > 0) {
            snList = qrCode.split("\r");
        } else if (qrCode.indexOf("\n") > 0) {
            snList = qrCode.split("\n");
        } else if (qrCode.indexOf("[CR][LF]") > 0) {
            snList = qrCode.split("[CR][LF]");
        } else if (qrCode.indexOf("[CR]") > 0) {
            snList = qrCode.split("\\[CR]");
        } else if (qrCode.indexOf("[LF]") > 0) {
            snList = qrCode.split("\\[LF]");
        } else if (qrCode.indexOf(",") > 0) { //測試 之後拿掉
            snList = qrCode.split(",");
        }

        List<String> list = new ArrayList<>();

        if (snList == null || snList.length < 4) {
            Toast.makeText(getApplicationContext(), getString(R.string.qr_code_format_incorrect_cant_resolve_sn), Toast.LENGTH_LONG).show();
        } else {
            for (int i = 0; i < snList.length; i++) {
                if (i > 2) {
                    list.add(snList[i]);
                }
            }
        }

//        if (snList == null) {
//            Toast.makeText(getApplicationContext(), getString(R.string.qr_code_format_incorrect_cant_resolve_sn), Toast.LENGTH_LONG).show();
//        } else {
//            for (int i = 0; i < snList.length; i++) {
//                list.add(snList[i]);
//            }
//        }

        return list;
    }

    private class FortinetQcApprovalTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
//            publishProgress(getString(R.string.processing));
            return new FortinetHandler().getFortinetQcApproval(params[0]);
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
        protected void  onPostExecute(String result) {
            dialog.dismiss();
            AppController.debug("FortinetQcApprovalTask = " + result);

            if (result != null) {
                try {
                    JSONArray jsonArray = new JSONArray(result);

                    if (processInfoHelper.getSnList().length!=jsonArray.length()){//傳入的參數不等於回傳的參數，代表有序號不是Fortinet的所以回傳空值
                        mConnection.setText("Length of Error");
                        mConnection.setTextColor(Color.RED);
                        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                        errorInfo = getString(R.string.sn_not_in_system);
                    }else if (jsonArray.length() > 0) {
//                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        for(int i = 0;i<jsonArray.length();i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i );

                            String approver = jsonObject.getString("approver");
                            String bom = jsonObject.getString("bom");
                            String test_station = jsonObject.getString("test_station");
                            String next_station = jsonObject.getString("next_station");
                            String status = jsonObject.getString("status");
                            String sn = jsonObject.getString("sn");

                            AppController.debug("Approver: " + approver+" ,status: " + status+" ,sn: " + sn);
//                        AppController.debug("BOM: " + bom);
//                        AppController.debug("Test Station: " + test_station);
//                        AppController.debug("Next Station: " + next_station);
//                        AppController.debug("status: " + status);

                            if ((!TextUtils.isEmpty(approver) && "Approved".equals(status)) || "Force Approved".equals(status)) {//狀態是"status": "Approved"，且要有 "approver": "vincent.chen"才給過
                                OkSn.add(sn);
                            }else{
                                ErrorSn.add(sn);
                            }
                        }

                        if(ErrorSn.size()>0){
                            String snList="";

                            for(String sn_D : ErrorSn){
                                snList+=sn_D+",";
                            }

                            mConnection.setText("List of ErrorSn");
                            mConnection.setTextColor(Color.RED);
                            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                            errorInfo = getString(R.string.sn_has_not_passed_customer_QC_approval) + snList.substring(0,snList.length()-1);

                        }else{
                            new DoStockInProcess().execute(0);
                        }
                    } else {
                        mConnection.setText("Length of Error");
                        mConnection.setTextColor(Color.RED);
                        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                        errorInfo = "Could not find SN in Fortinet's System";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    AppController.debug("JSONException: " + e);
                    mConnection.setText("Fortinet data error(not Json)");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = e.getMessage();
                }

//                mConnection.setText("");
//                mConnection.setTextColor(Color.WHITE);
//                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
//                errorInfo = "";
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "FortinetQcApproval ERROR(result is null)";
            }
        }
    }

    private String getDateCodeFromHelper() {
        if (isMerge) {
            if (noListHelper != null) {
                for (StockInNoHelper no : noListHelper.getStockInNoList()) {
                    for (DateCodeInfo info : no.getDateCodeInfo()) {
                        if (info.getWait() > 0) {
                            return info.getDateCode();
                        }
                    }
                }
            }
        } else {
            if (noHelper != null) {
                for (DateCodeInfo info : noHelper.getDateCodeInfo()) {
                    if (info.getWait() > 0) {
                        return info.getDateCode();
                    }
                }
            }
        }

        return null;
    }

    private String getApplyNo() {
        if (isMerge) {
            if (noListHelper != null) {
                for (StockInNoHelper no : noListHelper.getStockInNoList()) {
                    if (no.getDateCodeInfo() == null || no.getDateCodeInfo().length == 0) {
                        break;
                    } else {
                        for (DateCodeInfo info : no.getDateCodeInfo()) {
                            if (info.getWait() > 0) {
                                return no.getNo();
                            }
                        }
                    }
                }
            }
        } else {
            if (noHelper != null) {
                if (noHelper.getDateCodeInfo() == null || noHelper.getDateCodeInfo().length == 0) {
                    return noHelper.getNo();
                } else {
                    for (DateCodeInfo info : noHelper.getDateCodeInfo()) {
                        if (info.getWait() > 0) {
                            return noHelper.getNo();
                        }
                    }
                }
            }
        }

        return null;
    }

    private boolean isSSFG() {
        if (isMerge) {
            if (noListHelper != null && noListHelper.getStockInNoList() != null) {
                boolean allSSG = true;

                for (StockInNoHelper no : noListHelper.getStockInNoList()) {
                    if (!no.isSSG()) {
                        allSSG = false;
                        break;
                    }
                }

                return allSSG;
            }
        } else {
            if (noHelper != null) {
                return noHelper.isSSG();
            }
        }

        return false;
    }

    private String getWorkOrderNo() {
        if (isMerge) {
            if (noListHelper != null) {
                for (StockInNoHelper no : noListHelper.getStockInNoList()) {
                    if (no.getDateCodeInfo() == null || no.getDateCodeInfo().length == 0) {
                        break;
                    } else {
                        for (DateCodeInfo info : no.getDateCodeInfo()) {
                            if (info.getWait() > 0) {
                                return no.getWorkOrderNo();
                            }
                        }
                    }
                }
            }
        } else {
            if (noHelper != null) {
                if (noHelper.getDateCodeInfo() == null || noHelper.getDateCodeInfo().length == 0) {
                    return noHelper.getWorkOrderNo();
                } else {
                    for (DateCodeInfo info : noHelper.getDateCodeInfo()) {
                        if (info.getWait() > 0) {
                            return noHelper.getWorkOrderNo();
                        }
                    }
                }
            }
        }

        return null;
    }

    private void doQueryDateCode() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.read_datecode), true);
        new GetDateCode().execute(0);
    }

    private void showLayout(LayoutType type) {
        mode = type;
        btnYes.setVisibility(View.GONE);
        btnNo.setVisibility(View.GONE);
        rgDC.setVisibility(View.INVISIBLE);
        rgSN.setVisibility(View.INVISIBLE);
        lPalletLayout.setVisibility(View.GONE);
        lNoLayout.setVisibility(View.GONE);
        lDcQrCodeLayout.setVisibility(View.GONE);
        lReelIDLayout.setVisibility(View.GONE);
        lStockLayout.setVisibility(View.GONE);
        lQuestionLayout.setVisibility(View.GONE);
        lReceiptNoLayout.setVisibility(View.GONE);
        lPNLayout.setVisibility(View.GONE);
        lBoxLayout.setVisibility(View.GONE);
        lSNLayout.setVisibility(View.GONE);
        lSnQRCodeLayout.setVisibility(View.GONE);

        AppController.debug("mode:"+type);

        switch (type) {
            case STOCK_INFO:
                lStockLayout.setVisibility(View.VISIBLE);
                break;
            case QUESTION:
                lQuestionLayout.setVisibility(View.VISIBLE);
                btnYes.setVisibility(View.VISIBLE);
                btnNo.setVisibility(View.VISIBLE);
                break;
            case RECEIPT_NO:
                lReceiptNoLayout.setVisibility(View.VISIBLE);
                txtReceiptNo.requestFocus();
                break;
            case DC_PN:
                rgDC.setVisibility(View.VISIBLE);
                lPNLayout.setVisibility(View.VISIBLE);
                //rbPnEquation.setChecked(true);
                rbPnQty.setChecked(true);
                txtPnPN.requestFocus();
                break;
            case DC_QR_CODE:
                rgDC.setVisibility(View.VISIBLE);
                lDcQrCodeLayout.setVisibility(View.VISIBLE);
                txtDcQrCode.requestFocus();
                break;
            case DC_REEL_ID:
                rgDC.setVisibility(View.VISIBLE);
                lReelIDLayout.setVisibility(View.VISIBLE);
                //rbReelIdEquation.setChecked(true);
                rbReelIdQty.setChecked(true);
                txtReelID.requestFocus();
                break;
            case NO:
                lNoLayout.setVisibility(View.VISIBLE);
                //rbNoEquation.setChecked(true);
                rbNoQty.setChecked(true);
                txtNoPN.requestFocus();
                break;
            case SN_PALLET:
                rgSN.setVisibility(View.VISIBLE);
                lPalletLayout.setVisibility(View.VISIBLE);
                rbPalletSN.setChecked(true);
                break;
            case SN_BOX:
                rgSN.setVisibility(View.VISIBLE);
                lBoxLayout.setVisibility(View.VISIBLE);
                rbBoxSN.setChecked(true);
                break;
            case SN_SN:
                rgSN.setVisibility(View.VISIBLE);
                lSNLayout.setVisibility(View.VISIBLE);
                txtSNStart.requestFocus();
                break;
            case SN_QR_CODE:
                rgSN.setVisibility(View.VISIBLE);
                lSnQRCodeLayout.setVisibility(View.VISIBLE);
                txtSnQrCode.requestFocus();
                break;
            default:
                break;
        }
    }

    private void setDateCode() {
        this.txtPnDC.setText(getDateCodeFromHelper());
    }

    private void returnPage() {
        hideKeyboard();

        switch (mode) {
            case STOCK_INFO:
                if (needRefresh)
                    setResult(RESULT_OK);

                finish();
                break;
            case QUESTION:
                showLayout(LayoutType.STOCK_INFO);
                noListHelper = null;
                break;
            case RECEIPT_NO:
                txtReceiptNo.setText("");
                showLayout(LayoutType.QUESTION);
                noHelper = null;
                break;
            case DC_PN:
                txtWarehousingNo.setText("");
                rgDC.clearCheck();
                txtPnPN.setText("");
                txtPnDC.setText("");
                rbPnEquation.setChecked(false);
                txtPnMultiplier.setText("");
                txtPnMultiplicand.setText("");
                txtPnAddend.setText("");
                rbPnQty.setChecked(false);
                txtPnQty.setText("");

                if (isMerge)
                    showLayout(LayoutType.QUESTION);
                else
                    showLayout(LayoutType.RECEIPT_NO);

                break;
            case DC_QR_CODE:
                txtWarehousingNo.setText("");
                rgDC.clearCheck();
                txtDcQrCode.setText("");

                if (isMerge)
                    showLayout(LayoutType.QUESTION);
                else
                    showLayout(LayoutType.RECEIPT_NO);

                break;
            case DC_REEL_ID:
                txtWarehousingNo.setText("");
                rgDC.clearCheck();
                rbReelIdEquation.setChecked(false);
                txtReelID.setText("");
                txtReelIdMultiplier.setText("");
                txtReelIdMultiplicand.setText("");
                txtReelIdAddend.setText("");
                rbReelIdQty.setChecked(false);
                txtReelIdQty.setText("");

                if (isMerge)
                    showLayout(LayoutType.QUESTION);
                else
                    showLayout(LayoutType.RECEIPT_NO);

                break;
            case NO:
                txtWarehousingNo.setText("");
                txtNoPN.setText("");
                rbNoEquation.setChecked(false);
                txtNoMultiplier.setText("");
                txtNoMultiplicand.setText("");
                txtNoAddend.setText("");
                rbNoQty.setChecked(false);
                txtNoQty.setText("");

                if (isMerge)
                    showLayout(LayoutType.QUESTION);
                else
                    showLayout(LayoutType.RECEIPT_NO);

                break;
            case SN_PALLET:
                rgSN.clearCheck();
                rbPalletSN.setChecked(false);
                txtPalletSN.setText("");
                rbPalletCN.setChecked(false);
                txtPalletCN.setText("");
                txtWarehousingNo.setText("");
                txtPreStockQty.setText(getString(R.string.label_prestocking_quantity));

                if (isMerge)
                    showLayout(LayoutType.QUESTION);
                else
                    showLayout(LayoutType.RECEIPT_NO);

                break;
            case SN_BOX:
                txtWarehousingNo.setText("");
                rgSN.clearCheck();
                rbBoxSN.setChecked(false);
                txtBoxSN.setText("");
                rbBoxCN.setChecked(false);
                txtBoxCN.setText("");
                txtPreStockQty.setText(getString(R.string.label_prestocking_quantity));

                if (isMerge)
                    showLayout(LayoutType.QUESTION);
                else
                    showLayout(LayoutType.RECEIPT_NO);

                break;
            case SN_SN:
                txtWarehousingNo.setText("");
                rgSN.clearCheck();
                txtSNStart.setText("");
                txtSNEnd.setText("");

                if (isMerge)
                    showLayout(LayoutType.QUESTION);
                else
                    showLayout(LayoutType.RECEIPT_NO);

                break;
            case SN_QR_CODE:
                txtWarehousingNo.setText("");
                rgSN.clearCheck();
                txtSnQrCode.setText("");

                if (isMerge)
                    showLayout(LayoutType.QUESTION);
                else
                    showLayout(LayoutType.RECEIPT_NO);

                break;
            default:
                break;
        }

        mConnection.setText("");
        mConnection.setTextColor(Color.WHITE);
        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        errorInfo = "";

        if (!isMerge) {
            isMerge = true;
            setQuantityTitle();
        }
    }

    private void printPalletLabel(String palletNo) {
        if (!TextUtils.isEmpty(palletNo)) {
            String[] AryPallet = palletNo.split(",");

            for (String no : AryPallet) {
                if (!palletNoList.contains(no)) {
                    palletNoList.add(no);
                    dialog = ProgressDialog.show(StockInProcessActivity.this,
                            getString(R.string.holdon), getString(R.string.printingLabel), true);
                    errorInfo = "";

                    if (!BtPrintLabel.printPalletReceived(Util.getCurrentTimeStamp(), no)) {
                        errorInfo = getString(R.string.printLabalFailed);
                        mConnection.setText(getString(R.string.printer_connect_error));
                        mConnection.setTextColor(Color.RED);
                        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                        dialog.dismiss();
                        break;
                    }

                    dialog.dismiss();
                }
            }
        }
    }

    private void refreshQuantity() {
        if (isMerge) {
            item.setQty(0);
            item.setPass(0);
            item.setWait(0);

            for (StockInNoHelper stockInNo : noListHelper.getStockInNoList()) {
                item.setQty(item.getQty() + stockInNo.getItemInfo().getQty());
                item.setPass(item.getPass() + stockInNo.getItemInfo().getPass());
                item.setWait(item.getWait() + stockInNo.getItemInfo().getWait());
            }
        }
    }

    private void setQuantity() {
        int quantity = processInfoHelper.getQty();
        item.setPass(item.getPass() + quantity);
        item.setWait(item.getWait() - quantity);

        if (!isMerge) {
            noHelper.getItemInfo().setPass(noHelper.getItemInfo().getPass() + quantity);
            noHelper.getItemInfo().setWait(noHelper.getItemInfo().getWait() - quantity);
        }

        if (mode == LayoutType.DC_PN) {
            if (isMerge) {
                for (StockInNoHelper stockInNo : noListHelper.getStockInNoList()) {
                    for (DateCodeInfo dateCode : stockInNo.getDateCodeInfo()) {
                        if (dateCode.getDateCode().equals(processInfoHelper.getDateCode1()) && dateCode.getWait() > 0 && quantity > 0) {
                            if (!processInfoHelper.getDateCode1().equals(processInfoHelper.getDateCode2()))
                                dateCode.setDateCode(processInfoHelper.getDateCode2());

                            if (dateCode.getWait() >= quantity) {
                                dateCode.setPass(dateCode.getPass() + quantity);
                                dateCode.setWait(dateCode.getWait() - quantity);
                                quantity = 0;
                                break;
                            } else {
                                dateCode.setPass(dateCode.getPass() + dateCode.getWait());
                                dateCode.setWait(0);
                                quantity -= dateCode.getWait();
                            }
                        }
                    }
                }
            } else {
                for (DateCodeInfo dateCode : noHelper.getDateCodeInfo()) {
                    if (dateCode.getDateCode().equals(processInfoHelper.getDateCode1()) && dateCode.getWait() > 0) {
                        if (!processInfoHelper.getDateCode1().equals(processInfoHelper.getDateCode2()))
                            dateCode.setDateCode(processInfoHelper.getDateCode2());

                        dateCode.setPass(dateCode.getPass() + quantity);
                        dateCode.setWait(dateCode.getWait() - quantity);
                        break;
                    }
                }
            }
        }
    }

    private void checkReturnAndSetView() {
        if (item.getWait() == 0) {
            if (item.getQty() > 0)
                Toast.makeText(getApplicationContext(), getString(R.string.sku) + item.getItemID() + getString(R.string.chk_ok), Toast.LENGTH_LONG).show();

            setResult(RESULT_OK);
            finish();
        } else if (noHelper != null && noHelper.getItemInfo().getWait() == 0) {
            if (noHelper.getItemInfo().getQty() > 0)
                Toast.makeText(getApplicationContext(), getString(R.string.label_warehousing_number)+":" + noHelper.getNo() + getString(R.string.chk_ok), Toast.LENGTH_LONG).show();

            returnPage();
        } else if (mode == LayoutType.DC_PN) {
            if (isMerge) {
                for (StockInNoHelper stockInNo : noListHelper.getStockInNoList()) {
                    for (DateCodeInfo dateCode : stockInNo.getDateCodeInfo()) {
                        if (dateCode.getWait() > 0) {
                            txtPnDC.setText(dateCode.getDateCode());
                            return;
                        }
                    }
                }
            } else {
                for (DateCodeInfo dateCode : noHelper.getDateCodeInfo()) {
                    if (dateCode.getWait() > 0) {
                        txtPnDC.setText(dateCode.getDateCode());
                        break;
                    }
                }
            }
        }
    }

    public void onBackPressed() {
        // do something here and don't write super.onBackPressed()
        Log.d(TAG, "onClick Hardware Back Button");
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

    enum LayoutType {
        NONE, STOCK_INFO, QUESTION, RECEIPT_NO, DC_PN, DC_REEL_ID, NO, SN_PALLET, SN_BOX, SN_SN, SN_QR_CODE, DC_QR_CODE
    }

    private class GetStockInNoList extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Get StockNo List from " + AppController.getServerInfo()
                    + AppController.getProperties("GetStockNoList"));
            publishProgress(getString(R.string.downloading_data));
            return stockInHandler.getStockNoList(noListHelper);
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
            AppController.debug("GetStockInNoList result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    noListHelper = (StockInNoListHelper) result;
                    isMerge = true;
                    refreshQuantity();
                    setQuantityTitle();
                    setStockNoData();
                    setView();
                    checkReturnAndSetView();
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
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class GetCustomerID extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
//            AppController.debug("Get StockInfo from " + AppController.getServerInfo()
//                    + AppController.getProperties("GetSubinventory"));
            AppController.debug("GetCustomerID " + AppController.getServerInfo()
                    + AppController.getProperties("GetCustomerID"));
            publishProgress(getString(R.string.downloading_data));
            return stockInHandler.getSubinventory(subinventoryHelper);
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
                subinventoryHelper = (SubinventoryInfoHelper) result;

                if (result.getIntRetCode() == ReturnCode.OK) {
                    setInventoryData(subinventoryHelper);
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
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class GetSubInventory extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
//            AppController.debug("Get StockInfo from " + AppController.getServerInfo()
//                    + AppController.getProperties("GetSubinventory"));
            AppController.debug("Get StockInfo from " + AppController.getServerInfo()
                    + AppController.getProperties("GetSubinventoryNew"));
            publishProgress(getString(R.string.downloading_data));
            return stockInHandler.getSubinventory(subinventoryHelper);
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
                subinventoryHelper = (SubinventoryInfoHelper) result;

                if (result.getIntRetCode() == ReturnCode.OK) {
                    setInventoryData(subinventoryHelper);
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
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class GetStockInNo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Get StockInfo from " + AppController.getServerInfo()
                    + AppController.getProperties("GetStockInNo"));
            publishProgress(getString(R.string.downloading_data));
            return stockInHandler.getStockInNo(noHelper);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            //AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }

        @Override
        protected void onPostExecute(BasicHelper result) {
            dialog.dismiss();
            AppController.debug("GetStockInNo result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    noHelper = (StockInNoHelper) result;
                    isMerge = false;
                    setQuantityTitle();
                    setStockNoData();
                    setView();
                    checkReturnAndSetView();
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
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class CheckPalletBoxNo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check Box Number from " + AppController.getServerInfo()
                    + AppController.getProperties("CheckPalletBoxNo"));
            publishProgress(getString(R.string.downloading_data));
            return stockInHandler.checkPalletBoxNo(serialNoHelper);
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
                    serialNoHelper = (SerialNoHelper) result;
                    txtPalletSN.setText(serialNoHelper.getSerialNo());
                    txtPreStockQty.setText(getString(R.string.label_prestocking_quantity) + " " + serialNoHelper.getQtyOnPallet());
                    rbPalletCN.setChecked(true);
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    Toast.makeText(getApplicationContext(), getString(R.string.box_no_ok), Toast.LENGTH_LONG).show();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    txtBoxCN.requestFocus();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class CheckSN extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Get StockInfo from " + AppController.getServerInfo()
                    + AppController.getProperties("CheckSN"));
            publishProgress(getString(R.string.downloading_data));
            return stockInHandler.checkSN(serialNoHelper);
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

    private class CheckPallet extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Get StockInfo from " + AppController.getServerInfo()
                    + AppController.getProperties("CheckPallet"));
            publishProgress(getString(R.string.downloading_data));
            return stockInHandler.checkPallet(serialNoHelper);
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
            AppController.debug("CheckPallet result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    serialNoHelper = (SerialNoHelper) result;
                    txtPreStockQty.setText(getString(R.string.label_prestocking_quantity) + " " + serialNoHelper.getQtyOnPallet());
                    rbPalletSN.setChecked(true);
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    Toast.makeText(getApplicationContext(), getString(R.string.sn_correct), Toast.LENGTH_LONG).show();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    txtPalletSN.requestFocus();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class CheckSnBox extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Get StockInfo from " + AppController.getServerInfo()
                    + AppController.getProperties("CheckSnBox"));
            publishProgress(getString(R.string.downloading_data));
            return stockInHandler.checkSnBox(serialNoHelper);
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
            AppController.debug("CheckSnBox result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    serialNoHelper = (SerialNoHelper) result;
                    // txtBoxCN.setText(serialNoHelper.getBoxNo());
                    txtPreStockQty.setText(getString(R.string.label_prestocking_quantity) + " " + serialNoHelper.getQtyInBox());
                    rbBoxSN.setChecked(true);
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    Toast.makeText(getApplicationContext(), getString(R.string.sn_correct), Toast.LENGTH_LONG).show();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    txtBoxSN.requestFocus();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class CheckBoxNo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check Box Number from " + AppController.getServerInfo()
                    + AppController.getProperties("CheckBoxNo"));
            publishProgress(getString(R.string.downloading_data));
            return stockInHandler.checkBoxNo(serialNoHelper);
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
            AppController.debug("CheckBoxNo result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    serialNoHelper = (SerialNoHelper) result;
                    // txtBoxSN.setText(serialNoHelper.getValue());
                    txtPreStockQty.setText(getString(R.string.label_prestocking_quantity) + " " + serialNoHelper.getQtyInBox());
                    rbBoxCN.setChecked(true);
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    Toast.makeText(getApplicationContext(), getString(R.string.box_no_ok), Toast.LENGTH_LONG).show();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    txtBoxCN.requestFocus();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
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
                    processInfoHelper.setSubinventory(spinnerSubs.getSelectedItem().toString().trim());
                    processInfoHelper.setLocator(txtLocator.getText().toString().trim());
                    showLayout(LayoutType.QUESTION);
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
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class GetDateCode extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Get DateCode from " + AppController.getServerInfo()
                    + AppController.getProperties("GetDC"));
            publishProgress(getString(R.string.downloading_data));
            return stockInHandler.getDateCode(noHelper);
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
            AppController.debug("GetDateCode result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    noHelper = (StockInNoHelper) result;
                    setDateCode();
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
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class DoStockInProcess extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Do stock in process from " + AppController.getServerInfo()
                    + AppController.getProperties("DoStockInProcess"));
            publishProgress(getString(R.string.stock_proccessing));

            if (isMerge) {
                switch (mode) {
                    case DC_PN:
                        return stockInHandler.doDcMergePn(processInfoHelper);
                    case DC_QR_CODE:

                    case DC_REEL_ID:
                        return stockInHandler.doDCMergeReelID(processInfoHelper);
                    case NO:
                        return stockInHandler.doNoMerge(processInfoHelper);
                    case SN_PALLET:
                        return stockInHandler.doSnMergePallet(processInfoHelper);
                    case SN_BOX:
                        return stockInHandler.doSnMergeBox(processInfoHelper);
                    case SN_SN:
                        return stockInHandler.doSnMergeSn(processInfoHelper);
                    case SN_QR_CODE:
                        return stockInHandler.doSnMergeQRCode(processInfoHelper);
                    default:
                        return null;
                }
            } else {
                switch (mode) {
                    case DC_PN:
                        return stockInHandler.doDcPn(processInfoHelper);
                    case DC_QR_CODE:

                    case DC_REEL_ID:
                        return stockInHandler.doDCReelID(processInfoHelper);
                    case NO:
                        return stockInHandler.doNoNotMerge(processInfoHelper);
                    case SN_PALLET:
                        return stockInHandler.doSnPallet(processInfoHelper);
                    case SN_BOX:
                        return stockInHandler.doSnBox(processInfoHelper);
                    case SN_SN:
                        return stockInHandler.doSnSn(processInfoHelper);
                    case SN_QR_CODE:
                        return stockInHandler.doSnQRCode(processInfoHelper);
                    default:
                        return null;
                }
            }
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
            AppController.debug("DoStockInProcess result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    processInfoHelper = (StockInProcessInfoHelper) result;
                    needRefresh = true;
                    txtPreStockQty.setText("");
                    setQuantity();
                    setQuantityTitle();
                    cleanData();
                    Toast.makeText(getApplicationContext(), getString(R.string.checkOK), Toast.LENGTH_SHORT).show();
                    printPalletLabel(processInfoHelper.getPalletNo());

                    if (isMerge)
                        doQueryStockNoList();
                    else
                        doQueryStockInNo();
                } else {
                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    Toast.makeText(getApplicationContext(), errorInfo, Toast.LENGTH_SHORT).show();

                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                        // if (isMerge)
                        // doQueryStockNoList();
                        // else
                        // doQueryStockInNo();
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

    private void getFortinetControl() {//流程序號: SENAO10100005668      有個WEB頁面提供給Q維護
        // 使用基础URL初始化ApiManager
//        ApiManager apiManager = new ApiManager("https://api.example.com/");
//        ApiManager apiManager = new ApiManager(AppController.getAPIService()+"FortinetControl");
//        ApiManager apiManager = new ApiManager(AppController.getAPIService());
        ApiManager apiManager = new ApiManager(AppController.getSfcIp() + "/invoke" + AppController.getApi_2() + "?sCode=");

        // 发送POST请求到不同的动态URL
        DataRequest dataRequest = new DataRequest("token", 123);
        String dynamicUrl = "FortinetControl";

        apiManager.sendData(dynamicUrl, dataRequest, new ApiClient.ApiCallback() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onSuccess(ApiResponse result) {
                // 处理成功响应
//                AppController.debug( "ApiClient Status: " + result.getStatus());
//                AppController.debug( "ApiClient Message: " + result.getMessage());
                try {
                    JSONArray jsonArray = new JSONArray(result.getMessage());
                    fortinetSkuHelpers.clear();

                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String SKU = jsonObject.getString("SKU");
                        AppController.debug( "ApiClient SKU: " + SKU);
                        String chk = jsonObject.getString("CHK");
                        fortinetSkuHelpers.add(new FortinetSkuHelper(SKU,chk));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mConnection.setText("FortinetControl JSONException");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.RED);
                    errorInfo = "";
                }
            }

            @Override
            public void onError(String error) {
                // 处理错误响应
                AppController.debug("ApiClient Error: " + error);
            }
        });
    }

    private void updateRadioButtonsVisibility() {
        boolean isOnlyShowQrcode=false;

        for (FortinetSkuHelper data: fortinetSkuHelpers) {
            if(subinventoryHelper.getPartNo().equals(data.getSKU())  && "Y".equals(data.getChk().toUpperCase())){
                isOnlyShowQrcode=true;
            }
        }

        if (isOnlyShowQrcode) {
            // 隱藏所有 RadioButton，僅顯示 radio_sn_qrcode
            rbPallet.setVisibility(View.GONE);
            rbBox.setVisibility(View.GONE);
            rbSN.setVisibility(View.GONE);
            rbSnQRCode.setVisibility(View.VISIBLE);
        } else {
            // 顯示所有 RadioButton
            rbPallet.setVisibility(View.VISIBLE);
            rbBox.setVisibility(View.VISIBLE);
            rbSN.setVisibility(View.VISIBLE);
            rbSnQRCode.setVisibility(View.VISIBLE);
        }
    }
}
