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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.CartonInfoHelper;
import com.senao.warehouse.database.ChkDeliveryInfoHelper;
import com.senao.warehouse.database.ItemInfoHelper;
import com.senao.warehouse.database.ItemQtyInfoHelper;
import com.senao.warehouse.database.MerakiPoInfoHelper;
import com.senao.warehouse.database.OeInfo;
import com.senao.warehouse.database.OobaItem;
import com.senao.warehouse.database.OobaListHelper;
import com.senao.warehouse.database.PackingQtyInfoHelper;
import com.senao.warehouse.database.SamsaraPoInfoHelper;
import com.senao.warehouse.database.ShipmentPalletInfoHelper;
import com.senao.warehouse.database.SnItemInfoHelper;
import com.senao.warehouse.handler.LotcodePermissionHelper;
import com.senao.warehouse.handler.ShipmentPickingHandler;
import com.senao.warehouse.handler.ShippingVerifyMainHandler;
import com.senao.warehouse.print.BtPrintLabel;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.ReturnCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShipmentPickingActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PRINTER_SETTING = 2;
    private static final int REQUEST_BLUETOOTH_SETTINGS = 3;
    public final String TAG = ShipmentPickingActivity.class.getSimpleName();
    private final String CUSTOMER_MERAKI = "MERAKI";
    private final String CUSTOMER_CISCO = "CISCO";
    private final String CUSTOMER_SONICWALL = "SONICWALL";
    private final String CUSTOMER_SAMSARA = "SAMSARA";
    private final String CUSTOMER_EXTREME = "EXTREME";
    private final String CUSTOMER_FORTINET = "FORTINET";
    private final String CUSTOMER_ENGENIUS = "ENGENIUS";
    private final String CUSTOMER_SEASONIC = "海韻";
    private final String CUSTOMER_VERKADA = "VERKADA";

    private boolean isMerakiProduct = false;
    private TextView mConnection, mOrderStatus, mPartNo, mOePo, mCheckingQty,
            mPalletRemark, mImportSN2, mPalletNo, mPalletNo2, mImportSN3,
            mImportCN2, mImportQty, mCartonWeight, mPreCheckQty, mCheckingSN, mPalletVersion,
            mBoxVersion, mQrCodeVersion, mSnVersion,lblTitle;
    private EditText txtPalletNo, txtPalletNo2, txtImportSN, txtImportCN,
            txtImportCN2, txtImportFracWeight, txtImportSN2, txtImportSN3,
            txtImportQty, txtCartonWeight, txtImportFracWeightQrCode,
            txtImportCNQrCode, txtQrCode, txtPalletVersion, txtBoxVersion, txtQrCodeVersion, txtSnVersion,mLotcode_txt,org_edtxt;
    private Button btnReturn, btnConfim, btnCancel, btnYes, btnNo;
    private RadioButton rbPallet, rbBox, rbSN, rbSnQRCode, rbImportSn2, rbImportCN3, rbImportSN, rbImportCN;
    private LinearLayout lUpdatePalletLayout, lBoxLayout, lSNLayout, lSnQRCodeLayout;
    private RadioGroup rbGroup;
    private int intCheckedID, intCheckedBoxID, intPackQty, intSNCheckedCount;
    private boolean isUpdatePalletNumber = false;
    private ItemInfoHelper itemInfo;
    private ItemQtyInfoHelper itemQty;
    private PackingQtyInfoHelper packingQtyInfo;
    private SamsaraPoInfoHelper samsaraPoInfoHelper;
    private MerakiPoInfoHelper merakiPoInfoHelper  = new MerakiPoInfoHelper();
    private SnItemInfoHelper snItem = new SnItemInfoHelper();
    private SnItemInfoHelper tempSNItem;
    private LotcodePermissionHelper permissionHelper;
    private OobaListHelper oobaList;
    private ShipmentPickingHandler shipment;
    private ProgressDialog dialog;
    private String errorInfo = "";
    private ShipmentPalletInfoHelper printInfo;
    private LinearLayout palletSnLayout,checklotcode,checkorg;
    private TextView mImportCn3;
    private LinearLayout palletCartonLayout;
    private EditText txtImportCN3;
    private boolean isBoxEditTextEnabled = false;
    private boolean isSnEditTextEnabled = false;
    private boolean isBoxWeightEditeTextEnabled = false;
    private boolean isBoxEditTextEnabledQrCode = false;
    private boolean isBoxWeightEditeTextEnabledQrCode = false;
    private String samsaraSn;
    private String merakiSn;
    private OeInfo oeInfo;
    private boolean needRefresh;
    private ChkDeliveryInfoHelper dnInfo;
    private ShippingVerifyMainHandler verifyHandler;
    private boolean isOutSourcing = false;
    private String total_sn = "";

    private RadioGroup.OnCheckedChangeListener listener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            intCheckedID = checkedId;
            intSNCheckedCount = 0;
            mConnection.setText("");
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));

            switch (checkedId) {
                case R.id.radio_pallet:
                    Log.d("onClick Radio Pallet", String.valueOf(checkedId));
                    showAskUpdatePallet();
                    txtPalletVersion.setText("");
                    break;
                case R.id.radio_box:
                    Log.d("onClick Radio Box", String.valueOf(checkedId));
                    showAskUpdatePallet();
                    txtBoxVersion.setText("");
                    ShowLotCode(false);
                    break;
                case R.id.radio_sn:
                    Log.d("onClick Radio SN", String.valueOf(checkedId));
                    showSNDetail(false);
                    txtSnVersion.setText("");
                    ShowLotCode(true);
                    break;
                case R.id.radio_sn_qrcode:
                    Log.d("onClick Radio QR Code", String.valueOf(checkedId));
                    showAskUpdatePallet();
                    txtQrCodeVersion.setText("");
                    ShowLotCode(false);
                    break;
            }

            cleanData();
        }
    };

    private CompoundButton.OnCheckedChangeListener palletSnListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbImportCN3.setChecked(false);
                txtImportSN2.setEnabled(true);
                txtImportCN3.setText("");
                // txtImportCN3.setFocusable(false);
                txtImportCN3.setEnabled(false);
                txtImportSN2.requestFocus();
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener palletCnListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                rbImportSn2.setChecked(false);
                txtImportSN2.setText("");
                txtImportSN2.setEnabled(false);
                // txtImportCN3.setFocusable(true);
                txtImportCN3.setEnabled(true);
                txtImportCN3.requestFocus();
            }
        }
    };

    private OnCheckedChangeListener cbListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            txtImportSN.setText("");
            txtImportCN.setText("");

            if (isChecked) {
                intCheckedBoxID = buttonView.getId();

                switch (buttonView.getId()) {
                    case R.id.radiobutton_import_sn:
                        txtImportSN.setEnabled(true);
                        txtImportCN.setEnabled(false);
                        //20231115 Milla 改為focus Lotcode
                        //txtImportSN.requestFocus();
                        rbImportSN.setChecked(true);
                        rbImportCN.setChecked(false);
                        break;
                    case R.id.radiobutton_import_cn:
                        txtImportSN.setEnabled(false);
                        txtImportCN.setEnabled(true);
                        txtImportCN.requestFocus();
                        rbImportSN.setChecked(false);
                        rbImportCN.setChecked(true);
                        break;
                }
            } else {
                switch (buttonView.getId()) {
                    case R.id.radiobutton_import_sn:
                        txtImportSN.setEnabled(false);
                        rbImportSN.setChecked(false);
                        break;
                    case R.id.radiobutton_import_cn:
                        txtImportCN.setEnabled(false);
                        rbImportCN.setChecked(false);
                        break;
                }
            }
        }
    };

    private TextView.OnEditorActionListener revEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
            final boolean isEnterEvent = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
            final boolean isEnterUpEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_UP;
            final boolean isEnterDownEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_DOWN;

            if (actionId == EditorInfo.IME_ACTION_NEXT || isEnterUpEvent) {
                // Do your action here
                EditText et = (EditText) textView;

                if (et.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.version_is_not_null), Toast.LENGTH_SHORT).show();
                    return true;
                }

                return false;
            } else if (isEnterDownEvent) {
                // Capture this event to receive ACTION_UP
                return true;
            } else {
                // We do not care on other actions
                return false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AppController.getUser() == null) {
            finish();
            return;
        }

        setContentView(R.layout.activity_shipping_picking_way);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            itemInfo = new Gson().fromJson(extras.getString("ITEM_INFO"), ItemInfoHelper.class);

            if (itemInfo == null || AppController.getDnInfo() == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_single_box), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            snItem.setDeliveryID(AppController.getDnInfo().getDeliveryID());
            snItem.setItemID(itemInfo.getId());
            snItem.setItemNo(itemInfo.getItemID());
            snItem.setCustomer(getCustomerName());
            shipment = new ShipmentPickingHandler();
            intPackQty = extras.getInt("PACK_QTY");
            oeInfo = new Gson().fromJson(extras.getString("OE_INFO"), OeInfo.class);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_single_box),Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        new GetOutSourcing().execute(0);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ShipmentPickingActivity.this);
                    dialog.setTitle("Msg");
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

        mOrderStatus = findViewById(R.id.textview_order_status);
        mPartNo = findViewById(R.id.label_part_no);
        mOePo = findViewById(R.id.label_oe_po);
        mCheckingQty = findViewById(R.id.label_checking_quantity);
        mPalletNo = findViewById(R.id.label_pallet_number);
        mPreCheckQty = findViewById(R.id.label_prechecking_quantity);

        rbPallet = findViewById(R.id.radio_pallet);
        rbBox = findViewById(R.id.radio_box);
        rbSN = findViewById(R.id.radio_sn);
        rbSnQRCode = findViewById(R.id.radio_sn_qrcode);
        rbGroup = findViewById(R.id.radio_group_sn);

        lUpdatePalletLayout = findViewById(R.id.updatepalletLayout);
        mImportSN2 = findViewById(R.id.label_import_sn2);
        palletSnLayout = findViewById(R.id.palletSnLayout);
        rbImportSn2 = findViewById(R.id.radiobutton_import_sn2);
        rbImportSn2.setOnCheckedChangeListener(palletSnListener);
        txtImportSN2 = findViewById(R.id.edittext_import_sn2);
        txtImportSN2.setSelectAllOnFocus(true);

//        AppController.debug("S========>>>>>getCustomer():" +getCustomer());
//        AppController.debug("S========>>>>>itemInfo.getId():" +itemInfo.getId());
//        if (getCustomer().equals(CUSTOMER_VERKADA)) {
//            AppController.debug("S========>>>>>1");
//            permissionHelper = new LotcodePermissionHelper();
//            permissionHelper.setItemId(itemInfo.getId());
//            new GetIsLotcode().execute(0);
//        }

        txtImportSN2.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if (event.getAction() == KeyEvent.ACTION_UP
                        && (event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, String.valueOf(v.getId()));

                    if (txtImportSN2.getText().toString().trim().equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.sn_is_not_null), Toast.LENGTH_SHORT).show();
                        txtPalletNo.setFocusable(false);
                    } else {
                        txtPalletNo.setFocusable(true);
                        itemQty = new ItemQtyInfoHelper();
                        itemQty.setItemID(itemInfo.getId());
                        itemQty.setItemNo(itemInfo.getItemID());
                        itemQty.setDeliveryID(AppController.getDnInfo().getDeliveryID());
                        itemQty.setCustomer(getCustomerName());
                        itemQty.setTypeFlag(3);
                        itemQty.setSerialNo(txtImportSN2.getText().toString().trim());
                        itemQty.setSnOnHand(0);
                        itemQty.setSnBoxPltNo(null);
                        itemQty.setBoxQty(0);

                        if (oeInfo != null) {
                            itemQty.setOeID(oeInfo.getId());
                        }

                        dialog = ProgressDialog.show(ShipmentPickingActivity.this, getString(R.string.holdon),
                                getString(R.string.data_reading), true);
                        hideKeyboard(v);
                        new GetItemQtyInfo().execute(0);
                    }
                    // return true;
                }

                return false;
            }
        });

        mImportCn3 = findViewById(R.id.label_import_cn3);
        palletCartonLayout = findViewById(R.id.palletCartonLayout);
        rbImportCN3 = findViewById(R.id.radiobutton_import_cn3);
        rbImportCN3.setOnCheckedChangeListener(palletCnListener);
        txtImportCN3 = findViewById(R.id.edittext_import_cn3);
        txtImportCN3.setSelectAllOnFocus(true);

        txtImportCN3.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if (event.getAction() == KeyEvent.ACTION_UP
                        && (event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, String.valueOf(v.getId()));

                    if (txtImportCN3.getText().toString().trim().equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.box_num_is_not_null), Toast.LENGTH_SHORT).show();
                        txtPalletNo.setFocusable(false);
                    } else {
                        txtPalletNo.setFocusable(true);
                        itemQty = new ItemQtyInfoHelper();
                        itemQty.setItemID(itemInfo.getId());
                        itemQty.setItemNo(itemInfo.getItemID());
                        itemQty.setDeliveryID(AppController.getDnInfo().getDeliveryID());
                        itemQty.setTypeFlag(7);
                        itemQty.setSerialNo(txtImportCN3.getText().toString().trim());
                        itemQty.setSnOnHand(0);
                        itemQty.setSnBoxPltNo(null);
                        itemQty.setBoxQty(0);
                        itemQty.setCustomer(getCustomerName());

                        if (oeInfo != null) {
                            itemQty.setOeID(oeInfo.getId());
                        }

                        dialog = ProgressDialog.show(ShipmentPickingActivity.this, getString(R.string.holdon),
                                getString(R.string.data_reading), true);
                        hideKeyboard(v);
                        new GetItemQtyInfo().execute(0);
                    }
                    // return true;
                }

                return false;
            }
        });

        mPalletVersion = findViewById(R.id.label_import_pallet_ver);
        txtPalletVersion = findViewById(R.id.edittext_pallet_ver);
        txtPalletVersion.setSelectAllOnFocus(true);
        txtPalletVersion.setOnEditorActionListener(revEditorActionListener);

        mPalletRemark = findViewById(R.id.pallet_remark);
        txtPalletNo = findViewById(R.id.edittext_pallet_no);
        txtPalletNo.setSelectAllOnFocus(true);

        lBoxLayout = findViewById(R.id.boxLayout);
        mBoxVersion = findViewById(R.id.label_import_box_ver);
        txtBoxVersion = findViewById(R.id.edittext_box_ver);
        txtBoxVersion.setSelectAllOnFocus(true);
        txtBoxVersion.setOnEditorActionListener(revEditorActionListener);

        rbImportSN = findViewById(R.id.radiobutton_import_sn);
        rbImportSN.setOnCheckedChangeListener(cbListener);
        txtImportSN = findViewById(R.id.edittext_import_sn);
        txtImportSN.setSelectAllOnFocus(true);
        txtImportSN.setEnabled(false);

        txtImportSN.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if (event.getAction() == KeyEvent.ACTION_UP
                        && (event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, String.valueOf(v.getId()));

                    if (txtImportSN.getText().toString().trim().equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.sn_is_not_null), Toast.LENGTH_SHORT).show();
                    } else {
                        itemQty = new ItemQtyInfoHelper();
                        itemQty.setItemID(itemInfo.getId());
                        itemQty.setItemNo(itemInfo.getItemID());
                        itemQty.setDeliveryID(AppController.getDnInfo().getDeliveryID());
                        itemQty.setTypeFlag(2);
                        itemQty.setSerialNo(txtImportSN.getText().toString().trim());
                        itemQty.setSnOnHand(0);
                        itemQty.setSnBoxPltNo(null);
                        itemQty.setBoxQty(0);
                        itemQty.setCustomer(getCustomerName());

                        if (oeInfo != null) {
                            itemQty.setOeID(oeInfo.getId());
                        }

                        dialog = ProgressDialog.show(ShipmentPickingActivity.this, getString(R.string.holdon),
                                getString(R.string.data_reading), true);
                        new GetItemQtyInfo().execute(0);
                    }
                    // return true;
                }

                return false;
            }
        });

        rbImportCN = findViewById(R.id.radiobutton_import_cn);
        rbImportCN.setOnCheckedChangeListener(cbListener);
        txtImportCN = findViewById(R.id.edittext_import_cn);
        txtImportCN.setSelectAllOnFocus(true);
        txtImportCN.setEnabled(false);

        txtImportCN.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if (btnConfim.getVisibility() != View.VISIBLE) {
                    if (event.getAction() == KeyEvent.ACTION_UP
                            && (event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                        // Perform action on key press
                        Log.d(TAG, String.valueOf(v.getId()));

                        if (txtImportCN.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_box_nu), Toast.LENGTH_SHORT).show();
                        } else {
                            itemQty = new ItemQtyInfoHelper();
                            itemQty.setItemID(itemInfo.getId());
                            itemQty.setItemNo(itemInfo.getItemID());
                            itemQty.setDeliveryID(AppController.getDnInfo().getDeliveryID());
                            itemQty.setTypeFlag(6);
                            itemQty.setSerialNo(txtImportCN.getText().toString().trim());
                            itemQty.setSnOnHand(0);
                            itemQty.setSnBoxPltNo(null);
                            itemQty.setBoxQty(0);
                            itemQty.setCustomer(getCustomerName());

                            if (oeInfo != null) {
                                itemQty.setOeID(oeInfo.getId());
                            }

                            dialog = ProgressDialog.show(ShipmentPickingActivity.this, getString(R.string.holdon),
                                    getString(R.string.data_reading), true);
                            new GetItemQtyInfo().execute(0);
                        }
                        // return true;
                    }
                }

                return false;
            }
        });

        txtImportFracWeight = findViewById(R.id.edittext_import_frac_weight);
        txtImportFracWeight.setSelectAllOnFocus(true);
        txtImportFracWeight.setEnabled(false);

        mLotcode_txt = findViewById(R.id.lotcode_edtxt);
        mLotcode_txt.setEnabled(true);
        checklotcode = findViewById(R.id.checklotcode);
        checklotcode.setVisibility(View.GONE);

        org_edtxt = findViewById(R.id.org_edtxt);

        checkorg = findViewById(R.id.checkorg);

        if(Constant.ISORG){
            checkorg.setVisibility(View.GONE);
        }else{
            checkorg.setVisibility(View.GONE);
        }

        lSNLayout = findViewById(R.id.snLayout);
        mSnVersion = findViewById(R.id.label_import_sn_ver);
        txtSnVersion = findViewById(R.id.edittext_sn_ver);
        txtSnVersion.setSelectAllOnFocus(true);
        txtSnVersion.setOnEditorActionListener(revEditorActionListener);
        mPalletNo2 = findViewById(R.id.Label_pallet_no);
        txtPalletNo2 = findViewById(R.id.edittext_pallet_no2);
        txtPalletNo2.setSelectAllOnFocus(true);
        mImportSN3 = findViewById(R.id.label_import_sn3);
        txtImportSN3 = findViewById(R.id.edittext_import_sn3);
        txtImportSN3.setSelectAllOnFocus(true);

        txtImportSN3.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if (event.getAction() == KeyEvent.ACTION_UP
                        && (event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, String.valueOf(v.getId()));

                    if(checklotcode.getVisibility()==View.VISIBLE){
                        if(TextUtils.isEmpty(mLotcode_txt.getText().toString().trim())){
                            Toast.makeText(getApplicationContext(), getString(R.string.lotcode), Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }

//                    if(Constant.ISORG){
//                        if(TextUtils.isEmpty(org_edtxt.getText().toString().trim())){
//                            Toast.makeText(getApplicationContext(), "請輸入ORG", Toast.LENGTH_SHORT).show();
//                            return false;
//                        }else if(AppController.getOrg()!=Integer.parseInt(org_edtxt.getText().toString().trim())){
//                            Toast.makeText(getApplicationContext(), "與當前ORG不符!",Toast.LENGTH_SHORT).show();
//                            return false;
//                        }
//                    }

                    if (txtImportSN3.getText().toString().trim().equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.sn_is_not_null), Toast.LENGTH_SHORT).show();
                    } else {
                        tempSNItem.setDeliveryID(AppController.getDnInfo().getDeliveryID());
                        tempSNItem.setItemID(itemInfo.getId());
                        tempSNItem.setItemNo(itemInfo.getItemID());
                        tempSNItem.setCustomer(getCustomerName());
                        tempSNItem.setTypeFlag(1);
                        tempSNItem.setPalletNo(txtPalletNo2.getText().toString().trim());
                        tempSNItem.setCartonNo(txtImportCN2.getText().toString().trim());
                        tempSNItem.setBoxWeight(Double.parseDouble(txtCartonWeight.getText().toString()));
                        tempSNItem.setSerialNo(txtImportSN3.getText().toString().trim());
                        tempSNItem.setLotcode(mLotcode_txt.getText().toString().trim());

                        if (getCustomerName().contains(CUSTOMER_EXTREME) && itemInfo.getItemID().substring(0, 2).equals("09")) {
                            String rev = txtSnVersion.getText().toString().trim();
                            String[] values = rev.split("-");
                            rev = values[values.length - 1];
                            tempSNItem.setRev(rev);
                        } else {
                            tempSNItem.setRev(null);
                        }

                        if (oeInfo != null) {
                            tempSNItem.setOeID(oeInfo.getId());
                        }

                        dialog = ProgressDialog.show(ShipmentPickingActivity.this, getString(R.string.holdon),
                                getString(R.string.data_uploading), true);
                        new SetSNItemInfo().execute(0);
                    }
                    // return true;
                }

                return false;
            }
        });

        mImportCN2 = findViewById(R.id.label_import_cn2);
        txtImportCN2 = findViewById(R.id.edittext_import_cn2);
        txtImportCN2.setSelectAllOnFocus(true);
        mImportQty = findViewById(R.id.label_import_qty);
        txtImportQty = findViewById(R.id.edittext_import_qty);
        txtImportQty.setSelectAllOnFocus(true);
        mCartonWeight = findViewById(R.id.label_carton_weight);
        txtCartonWeight = findViewById(R.id.edittext_carton_weight);
        txtCartonWeight.setSelectAllOnFocus(true);
        mCheckingSN = findViewById(R.id.label_checking_sn);

        lSnQRCodeLayout = findViewById(R.id.snQrCodeLayout);

        txtQrCode = findViewById(R.id.edittext_import_qrcode);
        mQrCodeVersion = findViewById(R.id.label_import_qrcode_ver);
        txtQrCodeVersion = findViewById(R.id.edittext_qrcode_ver);
        txtQrCodeVersion.setSelectAllOnFocus(true);
        txtQrCodeVersion.setOnEditorActionListener(revEditorActionListener);

        txtImportFracWeightQrCode = findViewById(R.id.edittext_import_frac_weight_qrcode);
        txtImportCNQrCode = findViewById(R.id.edittext_import_cn_qrcode);
        txtImportCNQrCode.setEnabled(false);
        txtImportFracWeightQrCode.setSelectAllOnFocus(true);
        txtImportFracWeightQrCode.setEnabled(false);

        txtQrCode.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if (event.getAction() == KeyEvent.ACTION_UP
                        && (event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, String.valueOf(v.getId()));

                    if(checklotcode.getVisibility() == View.VISIBLE) {
                        if (TextUtils.isEmpty(mLotcode_txt.getText())) {
                            mLotcode_txt.requestFocus();
                            Toast.makeText(getApplicationContext(), getString(R.string.lotcode), Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }

                    if (txtQrCode.getText().toString().trim().equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.QR_CODE_is_not_null), Toast.LENGTH_SHORT).show();
                        return true;
                    } else {
                        String sn = getSN(txtQrCode.getText().toString().trim());

                        if (TextUtils.isEmpty(sn)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.Unable_parse_SN_from_QR_Code), Toast.LENGTH_LONG).show();
                            return true;
                       // } else if(TextUtils.isEmpty(sn)){
                        } else {
                            itemQty = new ItemQtyInfoHelper();
                            itemQty.setItemID(itemInfo.getId());
                            itemQty.setItemNo(itemInfo.getItemID());
                            itemQty.setDeliveryID(AppController.getDnInfo().getDeliveryID());
                            itemQty.setTypeFlag(2);
                            itemQty.setSerialNo(sn);
                            itemQty.setSnOnHand(0);
                            itemQty.setSnBoxPltNo(null);
                            itemQty.setBoxQty(0);
                            itemQty.setCustomer(getCustomerName());

                            if (oeInfo != null) {
                                itemQty.setOeID(oeInfo.getId());
                            }

                            dialog = ProgressDialog.show(ShipmentPickingActivity.this, getString(R.string.holdon),
                                    getString(R.string.data_reading), true);
                            new GetItemQtyInfo().execute(0);
                        }
                    }
                }

                return false;
            }
        });

        txtImportCNQrCode.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if (event.getAction() == KeyEvent.ACTION_UP
                        && (event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, String.valueOf(v.getId()));

                    if (txtImportCNQrCode.getText().toString().trim().equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.enter_box_nu), Toast.LENGTH_SHORT).show();
                        txtPalletNo.setFocusable(false);
                    }
                    // return true;
                }

                return false;
            }
        });

        lblTitle = findViewById(R.id.ap_title);
        setTitle();
        btnReturn = findViewById(R.id.button_return);
        btnConfim = findViewById(R.id.button_confirm);
        btnCancel = findViewById(R.id.button_cancel);
        btnYes = findViewById(R.id.button_yes);
        btnNo = findViewById(R.id.button_no);

        mOrderStatus.setText(AppController.getDnInfo().getOpStatus());

        if (AppController.getDnInfo().getOpStatus().toUpperCase().equals("HOLD")) {
            mOrderStatus.setTextColor(Color.RED);
        } else {
            mOrderStatus.setTextColor(Color.BLUE);
        }

        mPartNo.setText(getString(R.string.label_part_no_2, itemInfo.getItemID()));

        if (oeInfo == null) {
            mOePo.setVisibility(View.GONE);
            mCheckingQty.setText(getString(R.string.label_picking_qty, itemInfo.getQty(), itemInfo.getPass(), itemInfo.getWait()));
        } else {
            mOePo.setVisibility(View.VISIBLE);
            mOePo.setText(getString(R.string.label_oe_po, oeInfo.getOe(), oeInfo.getPo()));
            mCheckingQty.setText(getString(R.string.label_picking_qty, oeInfo.getQty(), oeInfo.getPass(), oeInfo.getWait()));
        }

        mPalletRemark.setText("");

        btnReturn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Return", String.valueOf(v.getId()));
                returnPage();
            }

        });

        btnConfim.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Confim", String.valueOf(v.getId()));

                if (txtSnVersion.getVisibility() == View.VISIBLE) {

                }

                if (isUpdatePalletNumber) {
                    tempSNItem.setOldPallet(snItem.getPalletNo());
                    tempSNItem.setPalletNo(txtPalletNo.getText().toString().trim());
                    mPalletNo.setText(getString(R.string.label_pallet_number3, tempSNItem.getPalletNo()));
                    itemQty = new ItemQtyInfoHelper();

                    itemQty.setItemID(itemInfo.getId());
                    itemQty.setItemNo(itemInfo.getItemID());
                    itemQty.setDeliveryID(AppController.getDnInfo().getDeliveryID());
                    itemQty.setCustomer(getCustomerName());
                    itemQty.setTypeFlag(2);

                    doQueryDeliveryInfo(AppController.getDnInfo().getDeliveryID(), tempSNItem.getPalletNo());
                    ShowLotCode(true);
                } else {
                    String rev = null;

                    if(checklotcode.getVisibility()==View.VISIBLE){
                        if(TextUtils.isEmpty(mLotcode_txt.getText().toString().trim())){
                            Toast.makeText(getApplicationContext(), getString(R.string.lotcode), Toast.LENGTH_SHORT).show();
                            return ;
                        }
                    }

                    if (intCheckedID == R.id.radio_sn) {
                        if (txtSnVersion.getVisibility() == View.VISIBLE) {
                            rev = txtSnVersion.getText().toString().trim();
                            String[] values = rev.split("-");
                            rev = values[values.length - 1];
                        }

                        if (txtSnVersion.getVisibility() == View.VISIBLE && txtSnVersion.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_version), Toast.LENGTH_SHORT).show();
                            txtSnVersion.requestFocus();
                        } else if (txtPalletNo2.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_pallet_nu), Toast.LENGTH_SHORT).show();
                            txtPalletNo2.requestFocus();
                        } else if (txtImportCN2.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_box_no), Toast.LENGTH_SHORT).show();
                            txtImportCN2.requestFocus();
                        } else if (txtImportQty.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_qty), Toast.LENGTH_SHORT).show();
                            txtImportQty.requestFocus();
                        } else if (oeInfo != null && Integer.parseInt(txtImportQty.getText().toString().trim()) > oeInfo.getWait()) {
                            Toast.makeText(getApplicationContext(), getString(R.string.qty_cant_more_than_unchk), Toast.LENGTH_SHORT).show();
                            txtImportQty.requestFocus();
                        } else if (oeInfo == null && Integer.parseInt(txtImportQty.getText().toString().trim()) > itemInfo.getWait()) {
                            Toast.makeText(getApplicationContext(), getString(R.string.qty_cant_more_than_unchk), Toast.LENGTH_SHORT).show();
                            txtImportQty.requestFocus();
                        } else if (txtCartonWeight.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_box_weight), Toast.LENGTH_SHORT).show();
                            txtCartonWeight.requestFocus();
                        } else {
                            doQueryDeliveryInfo(AppController.getDnInfo().getDeliveryID(), txtPalletNo2.getText().toString().trim());
                        }
                    } else if (intCheckedID == R.id.radio_box) {
                        if (txtBoxVersion.getVisibility() == View.VISIBLE) {
                            rev = txtBoxVersion.getText().toString().trim();
                            String[] values = rev.split("-");
                            rev = values[values.length - 1];
                        }

                        if (txtBoxVersion.getVisibility() == View.VISIBLE && txtBoxVersion.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_version), Toast.LENGTH_SHORT).show();
                            txtBoxVersion.requestFocus();
                        } else if (checkCheckingQty()) {
                            if (itemQty.getSnOnHand() < itemQty.getBoxQty() && txtImportFracWeight.getText().toString().trim().equals("")) {
                                Toast.makeText(getApplicationContext(), getString(R.string.enter_zero_carton_weight), Toast.LENGTH_SHORT).show();
                                txtImportFracWeight.requestFocus();
                            } else {
                                tempSNItem.setDeliveryID(AppController.getDnInfo().getDeliveryID());
                                tempSNItem.setItemID(itemInfo.getId());
                                tempSNItem.setItemNo(itemInfo.getItemID());
                                tempSNItem.setCustomer(getCustomerName());
                                tempSNItem.setTypeFlag(2);
                                tempSNItem.setPalletNo(txtPalletNo.getText().toString().trim());
                                tempSNItem.setSerialNo(txtImportSN.getText().toString().trim());
                                tempSNItem.setCartonNo(txtImportCN.getText().toString().trim());
                                tempSNItem.setLotcode(mLotcode_txt.getText().toString().trim());

                                if (!txtImportFracWeight.getText().toString().trim().equals("")) {
                                    tempSNItem.setBoxWeight(Double.parseDouble(txtImportFracWeight.getText().toString()));
                                }

                                if (txtBoxVersion.getVisibility() == View.VISIBLE) {
                                    rev = txtBoxVersion.getText().toString().trim();
                                    String[] values = rev.split("-");
                                    rev = values[values.length - 1];
                                    tempSNItem.setRev(rev);
                                } else {
                                    tempSNItem.setRev(null);
                                }

                                if (oeInfo != null) {
                                    tempSNItem.setOeID(oeInfo.getId());
                                }

                                setBoxControlStatus(false);
                                dialog = ProgressDialog.show(ShipmentPickingActivity.this, getString(R.string.holdon),
                                        getString(R.string.data_uploading), true);
                                new SetSNItemInfo().execute(0);
                            }
                        }
                    } else if (intCheckedID == R.id.radio_pallet) {
                        if (txtPalletVersion.getVisibility() == View.VISIBLE) {
                            rev = txtPalletVersion.getText().toString().trim();
                            String[] values = rev.split("-");
                            rev = values[values.length - 1];
                        }

                        if (txtPalletVersion.getVisibility() == View.VISIBLE && txtPalletVersion.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_version), Toast.LENGTH_SHORT).show();
                            txtPalletVersion.requestFocus();
                        } else if (checkCheckingQty()) {
                            tempSNItem.setDeliveryID(AppController.getDnInfo().getDeliveryID());
                            tempSNItem.setItemID(itemInfo.getId());
                            tempSNItem.setItemNo(itemInfo.getItemID());
                            tempSNItem.setCustomer(getCustomerName());

                            if (rbImportSn2.isChecked()) {
                                tempSNItem.setTypeFlag(3);
                                tempSNItem.setSerialNo(txtImportSN2.getText().toString().trim());
                            } else {
                                tempSNItem.setTypeFlag(4);
                                tempSNItem.setSerialNo(txtImportCN3.getText().toString().trim());
                            }

                            if (txtPalletVersion.getVisibility() == View.VISIBLE) {
                                rev = txtPalletVersion.getText().toString().trim();
                                String[] values = rev.split("-");
                                rev = values[values.length - 1];
                                tempSNItem.setRev(rev);
                            } else {
                                tempSNItem.setRev(null);
                            }

                            if (oeInfo != null) {
                                tempSNItem.setOeID(oeInfo.getId());
                            }

                            tempSNItem.setLotcode(mLotcode_txt.getText().toString().trim());

                            if (txtPalletNo.getVisibility() == View.VISIBLE) {
                                tempSNItem.setPalletNo(txtPalletNo.getText().toString().trim());
                                doQueryDeliveryInfo(AppController.getDnInfo().getDeliveryID(), txtPalletNo.getText().toString().trim());
                            } else {
                                dialog = ProgressDialog.show(ShipmentPickingActivity.this, getString(R.string.holdon),
                                        getString(R.string.data_uploading), true);
                                new SetSNItemInfo().execute(0);
                            }
                        }
                    } else if (intCheckedID == R.id.radio_sn_qrcode) {
                        if (txtQrCodeVersion.getVisibility() == View.VISIBLE) {
                            rev = txtQrCodeVersion.getText().toString().trim();
                            String[] values = rev.split("-");
                            rev = values[values.length - 1];
                        }

                        if (txtQrCodeVersion.getVisibility() == View.VISIBLE && txtQrCodeVersion.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_version), Toast.LENGTH_SHORT).show();
                            txtQrCodeVersion.requestFocus();
                        } else if (checkCheckingQty()) {
                            if (itemQty.getSnOnHand() < itemQty.getBoxQty()
                                    && txtImportFracWeightQrCode.getText().toString().trim().equals("")) {
                                Toast.makeText(getApplicationContext(), getString(R.string.enter_zero_carton_weight), Toast.LENGTH_SHORT).show();
                                txtImportFracWeightQrCode.requestFocus();
                            } else {
                                tempSNItem.setDeliveryID(AppController.getDnInfo().getDeliveryID());
                                tempSNItem.setItemID(itemInfo.getId());
                                tempSNItem.setItemNo(itemInfo.getItemID());
                                tempSNItem.setCustomer(getCustomerName());
                                tempSNItem.setTypeFlag(2);
                                tempSNItem.setPalletNo(txtPalletNo.getText().toString().trim());
                                String sn = getSN(txtQrCode.getText().toString().trim());

                                if (TextUtils.isEmpty(sn)) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.Unable_parse_QR_Code), Toast.LENGTH_LONG).show();
                                    return;
                                } else {
                                    tempSNItem.setSerialNo(sn);
                                }

                                tempSNItem.setCartonNo(txtImportCNQrCode.getText().toString().trim());

                                if (!txtImportFracWeightQrCode.getText().toString().trim().equals("")) {
                                    tempSNItem.setBoxWeight(Double.parseDouble(txtImportFracWeightQrCode.getText().toString()));
                                }

                                if (txtQrCodeVersion.getVisibility() == View.VISIBLE) {
                                    rev = txtQrCodeVersion.getText().toString().trim();
                                    String[] values = rev.split("-");
                                    rev = values[values.length - 1];
                                    tempSNItem.setRev(rev);
                                } else {
                                    tempSNItem.setRev(null);
                                }

                                if (oeInfo != null) {
                                    tempSNItem.setOeID(oeInfo.getId());
                                }

                                tempSNItem.setLotcode(mLotcode_txt.getText().toString().trim());
                                setBoxControlStatus(false);
                                dialog = ProgressDialog.show(ShipmentPickingActivity.this, getString(R.string.holdon),
                                        getString(R.string.data_chking_plt_carton), true);
                                tempSNItem.setTotal_sn(total_sn);

                                //2347Y-10425,2347Y-10635,2347Y-10638,2347Y-10639,2347Y-10647,2347Y-10667,2347Y-10685,2347Y-10690,2347Y-10721,2347Y-10740
//                                tempSNItem.setSerialNo("2347Y-10425,2347Y-10635,2347Y-10638,2347Y-10639,2347Y-10647,2347Y-10667,2347Y-10685,2347Y-10690,2347Y-10721,2347Y-10740");
//                                tempSNItem.setSerialNo("2347Y-10425,2347Y-10685,2347Y-10721,2347Y-10740");

                                new ChkPalletCarton().execute(0);

//                                new SetSNItemInfo().execute(0);
                            }
                        }
                    }
                }
            }

        });

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Cancel", String.valueOf(v.getId()));

                if (isUpdatePalletNumber) {
                    isUpdatePalletNumber = false;

                    if (intCheckedID == R.id.radio_box) {
                        showBoxDetail();
                    } else if (intCheckedID == R.id.radio_sn_qrcode) {
                        showQrCodeDetail();
                    }

                    mPalletNo.setText(getString(R.string.label_pallet_number3, snItem.getPalletNo()));
                } else {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));

                    cleanData();

                    mPreCheckQty.setText(getString(R.string.label_prechecking_quantity));
                    mPreCheckQty.setVisibility(View.INVISIBLE);

                    if (intCheckedID == R.id.radio_pallet) {
                        txtPalletVersion.setText("");
                        txtPalletNo.setEnabled(false);
                    } else if (intCheckedID == R.id.radio_box) {
                        txtBoxVersion.setText("");
                    } else if (intCheckedID == R.id.radio_sn) {
                        txtSnVersion.setText("");
                        btnConfim.setVisibility(View.INVISIBLE);
                        btnCancel.setVisibility(View.INVISIBLE);
                    } else {
                        txtQrCodeVersion.setText("");
                    }

                    setInputFocus();
                }
            }

        });

        btnYes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Yes", String.valueOf(v.getId()));

                if (intCheckedID == R.id.radio_pallet) {
                    showPalletDetail(View.VISIBLE);
                } else {
                    showUpdatePallet();
                }
            }

        });

        btnNo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick No", String.valueOf(v.getId()));
                isUpdatePalletNumber = false;

                if (intCheckedID == R.id.radio_pallet) {
                    showPalletDetail(View.INVISIBLE);
                } else if (intCheckedID == R.id.radio_box) {
                    showBoxDetail();
                    ShowLotCode(true);
                } else if (intCheckedID == R.id.radio_sn_qrcode) {
                    showQrCodeDetail();
                    ShowLotCode(true);
                }
            }

        });

        lUpdatePalletLayout.setVisibility(View.GONE);
        lBoxLayout.setVisibility(View.GONE);
        lSNLayout.setVisibility(View.GONE);
        lSnQRCodeLayout.setVisibility(View.GONE);
        btnConfim.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        btnYes.setVisibility(View.GONE);
        btnNo.setVisibility(View.GONE);
        mPalletNo.setVisibility(View.GONE);
        mPreCheckQty.setVisibility(View.INVISIBLE);

        if (AppController.getUser().getPalletYN() != null && AppController.getUser().getPalletYN().toUpperCase().equals("N")) {
            rbPallet.setVisibility(View.GONE);
        }

        rbGroup.setOnCheckedChangeListener(listener);

        if (getCustomerName().contains(CUSTOMER_MERAKI) || getCustomerName().contains(CUSTOMER_CISCO)) {
            isMerakiProduct = true;
//            rbImportCN.setEnabled(false);
//            rbImportCN.setClickable(false);
//            rbImportCN.setOnCheckedChangeListener(null);
//            txtImportCN.setEnabled(false);
            rbImportCN3.setEnabled(false);
            rbImportCN3.setClickable(false);
            rbImportCN3.setOnCheckedChangeListener(null);
            txtImportCN3.setEnabled(false);
        }

        checkPrinterSetting();
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.label_picking, AppController.getOrgName()));
        text.setSpan(new RelativeSizeSpan(1f), 7, 7 + AppController.getOrgName().length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private void checkOoba() {
        if (getCustomerName().contains(CUSTOMER_SONICWALL)) {
            oobaList = new OobaListHelper();
            oobaList.setItemId(itemInfo.getId());
            oobaList.setItemNo(itemInfo.getItemID());
            dialog = ProgressDialog.show(this, getString(R.string.holdon), "OOBA"+getString(R.string.data_querying), true);
            new CheckOoba().execute(0);
        }
    }

    private String getCustomerName() {
        return AppController.getDnInfo().getCustomer().split(" ")[0].toUpperCase();
    }

    private void getInvoiceNo(int dn) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.downloading_data), true);
        dnInfo = new ChkDeliveryInfoHelper();
        dnInfo.setDeliveryID(dn);
        verifyHandler = new ShippingVerifyMainHandler(dnInfo);
        new ChkDNInfo().execute(0);
    }

    private String getSN(String qrCode) {
        total_sn="";
        String[] snList;

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
        } else if (qrCode.indexOf(";") > 0) {
            snList = qrCode.split(";");
        } else if (qrCode.indexOf(" ") > 0) {
            snList = qrCode.split(" ");
        } else {
            snList = new String[1];
            snList[0] = qrCode;
        }

        List<String> list = new ArrayList<>();
        List<String> list_all = new ArrayList<>();

        if (getCustomerName().contains(CUSTOMER_FORTINET)) {
            list.addAll(Arrays.asList(snList));
            list_all.addAll(Arrays.asList(snList));
        } else if (getCustomerName().contains(CUSTOMER_EXTREME)) {
            try {
                list.add(snList[0].split(":")[1]);
                list_all.addAll(Arrays.asList(snList[0].split(":")));
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), getString(R.string.qr_code_format_incorrect_cant_resolve_sn), Toast.LENGTH_LONG).show();
            }
        } else if (getCustomerName().contains(CUSTOMER_ENGENIUS)) {
            try {
                for (String s : snList) {
                    list.add(s.split(";")[0]);
                    list_all.add(s.split(";")[0]);
//                    list_all.addAll(Arrays.asList(snList[0].split(":")));
                }
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), getString(R.string.qr_code_format_incorrect_cant_resolve_sn), Toast.LENGTH_LONG).show();
            }
        } else if (getCustomerName().contains(CUSTOMER_SEASONIC)) {
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
                            list_all.add(s);
                        }
                    }
                }
            }
        } else if (getCustomerName().contains(CUSTOMER_VERKADA)) {
            AppController.debug("getSN>>>>>VERKADA");
            AppController.debug("getSN>>>>>" + snList[0]);
            snList = snList[0].split(",");

            try {
                list.addAll(Arrays.asList(snList));
                list_all.addAll(Arrays.asList(snList));
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), getString(R.string.qr_code_format_incorrect_cant_resolve_sn), Toast.LENGTH_LONG).show();
            }
        } else {
            if (snList.length < 4) {
                Toast.makeText(getApplicationContext(), getString(R.string.qr_code_format_incorrect_cant_resolve_sn), Toast.LENGTH_LONG).show();
            } else {
                for (int i = 0; i < snList.length; i++) {
                    if (i > 2) {
                        list.add(snList[i]);
                        list_all.add(snList[i]);
                    }
                }
            }
        }

        //PDA新增QR Code序號檢核
        Set<String> set = new HashSet();

        for (String st:list) {
//            total_sn+=st+",";
            AppController.debug("st = " +  st);
            set.add(st);
        }

        //20240325 old
//        for(String st:list_all){
//            total_sn+=st+",";
//            AppController.debug("list_all = " +  st);
//        }

        //20240325 new
        if (list_all != null) {
            AppController.debug("for");

            if (list_all.size() > 0) {
                for (String st:list_all) {
                    AppController.debug("total_sn start");
                    total_sn += st + ",";
                    AppController.debug("total_sn +=" + st + ",");
                }
            } else {
                AppController.debug("list_all.size() = 0");

                for (String st:set) {
                    AppController.debug("total_sn start");
                    total_sn += st + ",";
                    AppController.debug("total_sn +=" + st + ",");
                }
            }
        } else {
            AppController.debug("list_all null");

            for (String st:set) {
                AppController.debug("total_sn start");
                total_sn += st + ",";
                AppController.debug("total_sn +=" + st + ",");
            }
        }

        total_sn = total_sn.substring(0,total_sn.length()-1);

        AppController.debug("total_sn = " +  total_sn);
        AppController.debug("set.size() = " + set.size());
        AppController.debug("list.size() = " + list.size());
//        AppController.debug("sn1  = " + sn );

        if(list.size()!=set.size()){
            Toast.makeText(getApplicationContext(), getString(R.string.sn_repeat_check), Toast.LENGTH_LONG).show();
            return null;
        }

        if (list.size() > 0)
            return list.get(0);
        else
            return null;
    }

    private String getCartonNo(String qrCode) {
        String[] list;

        if (qrCode.indexOf("\r\n") > 0) {
            list = qrCode.split("\r\n");
        } else if (qrCode.indexOf("\r") > 0) {
            list = qrCode.split("\r");
        } else if (qrCode.indexOf("\n") > 0) {
            list = qrCode.split("\n");
        } else if (qrCode.indexOf("[CR][LF]") > 0) {
            list = qrCode.split("[CR][LF]");
        } else if (qrCode.indexOf("[CR]") > 0) {
            list = qrCode.split("[CR]");
        } else if (qrCode.indexOf(";") > 0) {
            list = qrCode.split(";");
        } else {
            list = new String[1];
            list[0] = qrCode;
        }

        for (String item : list) {
            if (item.contains("C/No.")) {
                return item.substring(item.indexOf("C/No."));
            }
        }

        return null;
    }

    private void showBoxDetail() {
        if (itemQty == null) {
            itemQty = new ItemQtyInfoHelper();
        }

        lUpdatePalletLayout.setVisibility(View.GONE);
        lSNLayout.setVisibility(View.GONE);
        lSnQRCodeLayout.setVisibility(View.GONE);
        lBoxLayout.setVisibility(View.VISIBLE);

        intCheckedBoxID = 0;
        if (rbImportSN.isChecked()) {
            txtImportSN.setEnabled(true);
            txtImportCN.setEnabled(false);
            txtImportSN.requestFocus();
            //cbImportSN.setChecked(true);
            //cbImportCN.setChecked(false);
        } else
            rbImportSN.setChecked(true);

        //cbImportCN.setChecked(false);

        mPalletNo.setText(getString(R.string.label_pallet_number4, (tempSNItem.getPalletNo() == null ? "" : tempSNItem.getPalletNo())));
        mPalletNo.setVisibility(View.VISIBLE);

        if (getCustomerName().contains(CUSTOMER_EXTREME) && itemInfo.getItemID().substring(0, 2).equals("09")) {
            mBoxVersion.setVisibility(View.VISIBLE);
            txtBoxVersion.setVisibility(View.VISIBLE);
            txtBoxVersion.requestFocus();
        } else {
            mBoxVersion.setVisibility(View.GONE);
            txtBoxVersion.setVisibility(View.GONE);
        }

        btnConfim.setVisibility(View.INVISIBLE);
        btnCancel.setVisibility(View.INVISIBLE);
        btnYes.setVisibility(View.GONE);
        btnNo.setVisibility(View.GONE);
    }

    private void hideKeyboard(View edit) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
    }

    private void doQueryDeliveryInfo(int deliveryID, String palletNo) {
        printInfo = new ShipmentPalletInfoHelper();
        printInfo.setDnNo(deliveryID);
        printInfo.setPalletNo(palletNo);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), "DeliveryInfo"+getString(R.string.data_querying), true);
        new GetPrintInfo().execute(0);
    }

    private void printLabel() {
        dialog = ProgressDialog.show(ShipmentPickingActivity.this, getString(R.string.holdon),
                getString(R.string.printingLabel), true);
        errorInfo = "";
        String qrCode = dnInfo.getInvoiceNo() + "@" + printInfo.getDnNo() + "@" + printInfo.getPalletNo() + "@" + printInfo.getBoxQty();

        if(Constant.ISORG){
            qrCode = dnInfo.getInvoiceNo() + "@" + printInfo.getDnNo() + "@" + printInfo.getPalletNo() + "@" + printInfo.getBoxQty() + "@" + AppController.getOrg();
        }

//        if (!BtPrintLabel.printShipmentPalletLabel(printInfo.getMark(),printInfo.getShippingWay(), printInfo.getDnNo(),printInfo.getPalletNo(), printInfo.getBoxQty(), qrCode)) {
        if (!BtPrintLabel.printShipmentPalletLabel1(printInfo.getMark(),printInfo.getShippingWay(), printInfo.getDnNo(),printInfo.getPalletNo(), printInfo.getBoxQty(), qrCode,AppController.getOrg()+"")) {
            errorInfo = getString(R.string.printLabalFailed)+" ShipmentPickingActivity1";
            mConnection.setText(getString(R.string.printer_connect_error));
            mConnection.setTextColor(Color.RED);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        dialog.dismiss();

//        merakiPoInfoHelper = new MerakiPoInfoHelper();
//        merakiPoInfoHelper.setDn(AppController.getDnInfo().getDeliveryID());
//        merakiPoInfoHelper.setCarton_no(txtImportCN2.getText().toString());
//        merakiPoInfoHelper.setSn("");
//        new PrintMerakiPoInfo().execute(0);
    }

    private void showAskUpdatePallet() {
        mPalletRemark.setVisibility(View.VISIBLE);
        mPalletRemark.setText(getString(R.string.label_revise_pallet_no));
        lUpdatePalletLayout.setVisibility(View.VISIBLE);
        mPalletVersion.setVisibility(View.GONE);
        txtPalletVersion.setVisibility(View.GONE);
        mImportSN2.setVisibility(View.GONE);
        // txtImportSN2.setVisibility(View.GONE);
        palletSnLayout.setVisibility(View.GONE);
        mImportCn3.setVisibility(View.GONE);
        palletCartonLayout.setVisibility(View.GONE);
        txtPalletNo.setVisibility(View.INVISIBLE);
        btnConfim.setVisibility(View.INVISIBLE);
        btnCancel.setVisibility(View.INVISIBLE);
        btnYes.setVisibility(View.VISIBLE);
        btnNo.setVisibility(View.VISIBLE);
        lBoxLayout.setVisibility(View.GONE);
        lSNLayout.setVisibility(View.GONE);
        lSnQRCodeLayout.setVisibility(View.GONE);
        mPalletNo.setVisibility(View.GONE);
        mPreCheckQty.setVisibility(View.INVISIBLE);
        isUpdatePalletNumber = false;
    }

    private void showUpdatePallet() {
        lUpdatePalletLayout.setVisibility(View.VISIBLE);
        mPalletRemark.setText(getString(R.string.label_input_pallet_no));
        mImportSN2.setVisibility(View.GONE);
        //txtImportSN2.setVisibility(View.GONE);
        palletSnLayout.setVisibility(View.GONE);
        mImportCn3.setVisibility(View.GONE);
        palletCartonLayout.setVisibility(View.GONE);
        txtPalletNo.setVisibility(View.VISIBLE);
        txtPalletNo.setText(snItem.getPalletNo());
        txtPalletNo.setEnabled(true);
        txtPalletNo.requestFocus();
        btnConfim.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);
        btnYes.setVisibility(View.GONE);
        btnNo.setVisibility(View.GONE);
        lBoxLayout.setVisibility(View.GONE);
        lSNLayout.setVisibility(View.GONE);
        lSnQRCodeLayout.setVisibility(View.GONE);
        mPalletNo.setVisibility(View.GONE);
        mPreCheckQty.setVisibility(View.INVISIBLE);
        isUpdatePalletNumber = true;
    }

    private void cleanData() {
        if (btnYes.getVisibility() == View.VISIBLE || lUpdatePalletLayout.getVisibility() == View.VISIBLE) {
            txtPalletNo.setText("");
        } else {
            /*
             * if (tempSNItem != null) { if
             * (!tempSNItem.getPalletNo().equals(""))
             * txtPalletNo.setText(tempSNItem.getPalletNo()); }
             */
        }

        mLotcode_txt.setText("");
        tempSNItem = new SnItemInfoHelper();

        if (intCheckedID == R.id.radio_pallet) {
            rbImportSn2.setEnabled(true);
            txtImportSN2.setEnabled(true);
            rbImportSn2.setChecked(true);
            txtImportSN2.setText("");
            rbImportCN3.setEnabled(false);
            txtImportCN3.setEnabled(false);
            txtPalletNo.setEnabled(false);
        } else if (intCheckedID == R.id.radio_box) {
            rbImportSN.setEnabled(true);
            txtImportSN.setText("");
            txtImportSN.setEnabled(false);
            rbImportCN.setEnabled(true);
            txtImportCN.setText("");
            txtImportCN.setEnabled(false);
            txtImportFracWeight.setText("");
            txtImportFracWeight.setEnabled(false);
        } else if (intCheckedID == R.id.radio_sn) {
            txtImportSN3.setText("");

            if (txtImportSN3.getVisibility() != View.VISIBLE) {
                txtPalletNo2.setText("");
                txtImportCN2.setText("");
                txtImportQty.setText("");
                txtCartonWeight.setText("");
                mCheckingSN.setText("");
            }
        } else {
            txtQrCode.setText("");
            txtImportCNQrCode.setText("");
            txtImportCNQrCode.setEnabled(false);
            txtImportFracWeightQrCode.setText("");
            txtImportFracWeightQrCode.setEnabled(false);
        }
    }

    private void showPalletDetail(int isPalletNoVisible) {
        if (View.VISIBLE == isPalletNoVisible) {
            mPalletRemark.setText(getString(R.string.label_input_pallet_no));
            txtPalletNo.setVisibility(View.VISIBLE);
            txtPalletNo.setEnabled(false);
        } else {
            mPalletRemark.setVisibility(View.INVISIBLE);
            txtPalletNo.setVisibility(View.INVISIBLE);
        }

        lUpdatePalletLayout.setVisibility(View.VISIBLE);
        mImportSN2.setVisibility(View.VISIBLE);
        palletSnLayout.setVisibility(View.VISIBLE);
        // txtImportSN2.setVisibility(View.VISIBLE);
        // txtImportSN2.requestFocus();

        if (rbImportSn2.isChecked()) {
            txtImportSN2.setEnabled(true);
            txtImportCN3.setText("");
            // txtImportCN3.setFocusable(false);
            txtImportCN3.setEnabled(false);
            txtImportSN2.requestFocus();
        } else {
            rbImportSn2.setChecked(true);
        }

        if (getCustomerName().contains(CUSTOMER_EXTREME) && itemInfo.getItemID().substring(0, 2).equals("09")) {
            mPalletVersion.setVisibility(View.VISIBLE);
            txtPalletVersion.setVisibility(View.VISIBLE);
            txtPalletVersion.requestFocus();
        } else {
            if (isOutSourcing) {
                mPalletVersion.setVisibility(View.VISIBLE);
                txtPalletVersion.setVisibility(View.VISIBLE);
                txtPalletVersion.requestFocus();
            } else {
                mPalletVersion.setVisibility(View.GONE);
                txtPalletVersion.setVisibility(View.GONE);
            }

//            mPalletVersion.setVisibility(View.GONE);
//            txtPalletVersion.setVisibility(View.GONE);
        }

        //mImportCn3.setVisibility(View.VISIBLE);
        //palletCartonLayout.setVisibility(View.VISIBLE);
        btnConfim.setVisibility(View.INVISIBLE);
        btnCancel.setVisibility(View.INVISIBLE);
        btnYes.setVisibility(View.GONE);
        btnNo.setVisibility(View.GONE);
        lBoxLayout.setVisibility(View.GONE);
        lSNLayout.setVisibility(View.GONE);
        lSnQRCodeLayout.setVisibility(View.GONE);
    }

    private void showQrCodeDetail() {
        if (itemQty == null) {
            itemQty = new ItemQtyInfoHelper();
        }

        lUpdatePalletLayout.setVisibility(View.GONE);
        lSNLayout.setVisibility(View.GONE);
        lSnQRCodeLayout.setVisibility(View.VISIBLE);
        lBoxLayout.setVisibility(View.GONE);

        mPalletNo.setText(getString(R.string.label_pallet_number4, (tempSNItem.getPalletNo() == null ? "" : tempSNItem.getPalletNo())));

        txtQrCode.requestFocus();

        mPalletNo.setVisibility(View.VISIBLE);

        if (getCustomerName().contains(CUSTOMER_EXTREME) && itemInfo.getItemID().substring(0, 2).equals("09")) {
            mQrCodeVersion.setVisibility(View.VISIBLE);
            txtQrCodeVersion.setVisibility(View.VISIBLE);
            txtQrCodeVersion.requestFocus();
        } else {
            if (isOutSourcing) {
                mQrCodeVersion.setVisibility(View.VISIBLE);
                txtQrCodeVersion.setVisibility(View.VISIBLE);
                txtQrCodeVersion.requestFocus();
            } else {
                mQrCodeVersion.setVisibility(View.GONE);
                txtQrCodeVersion.setVisibility(View.GONE);
            }

//            mQrCodeVersion.setVisibility(View.GONE);
//            txtQrCodeVersion.setVisibility(View.GONE);
        }

        btnConfim.setVisibility(View.INVISIBLE);
        btnCancel.setVisibility(View.INVISIBLE);
        btnYes.setVisibility(View.GONE);
        btnNo.setVisibility(View.GONE);
    }

    private void showSNDetail(boolean isCheckingSN) {
        if (itemQty == null) {
            itemQty = new ItemQtyInfoHelper();
        }

        lSnQRCodeLayout.setVisibility(View.GONE);
        lSNLayout.setVisibility(View.VISIBLE);

        if (isCheckingSN) {
            mPalletNo.setText(getString(R.string.label_pallet_number4, txtPalletNo2.getText().toString().trim()));
            mPalletNo.setVisibility(View.VISIBLE);
            mPreCheckQty.setText(getString(R.string.label_prechecking_quantity2, 0));
            mPreCheckQty.setVisibility(View.VISIBLE);
            mPalletNo2.setVisibility(View.GONE);
            txtPalletNo2.setVisibility(View.GONE);
            mImportSN3.setVisibility(View.VISIBLE);
            txtImportSN3.setVisibility(View.VISIBLE);
            txtImportSN3.requestFocus();
            mImportCN2.setVisibility(View.GONE);
            txtImportCN2.setVisibility(View.GONE);
            mImportQty.setVisibility(View.GONE);
            txtImportQty.setVisibility(View.GONE);
            mCartonWeight.setVisibility(View.GONE);
            txtCartonWeight.setVisibility(View.GONE);
            mCheckingSN.setText(getString(R.string.label_sn_seq, intSNCheckedCount, txtImportQty.getText().toString().trim()));
            mCheckingSN.setVisibility(View.VISIBLE);
            mSnVersion.setVisibility(View.GONE);
            txtSnVersion.setVisibility(View.GONE);
            btnConfim.setVisibility(View.INVISIBLE);
            btnCancel.setVisibility(View.INVISIBLE);
            btnYes.setVisibility(View.GONE);
            btnNo.setVisibility(View.GONE);
        } else {
            mPalletNo2.setVisibility(View.VISIBLE);
            txtPalletNo2.setVisibility(View.VISIBLE);
            txtPalletNo2.requestFocus();
            mImportSN3.setVisibility(View.GONE);
            txtImportSN3.setVisibility(View.GONE);
            mImportCN2.setVisibility(View.VISIBLE);
            txtImportCN2.setVisibility(View.VISIBLE);
            mImportQty.setVisibility(View.VISIBLE);
            txtImportQty.setVisibility(View.VISIBLE);
            mCartonWeight.setVisibility(View.VISIBLE);
            txtCartonWeight.setVisibility(View.VISIBLE);
            mCheckingSN.setVisibility(View.GONE);
            mPreCheckQty.setVisibility(View.INVISIBLE);

            if (getCustomerName().contains(CUSTOMER_EXTREME) && itemInfo.getItemID().substring(0, 2).equals("09")) {
                mSnVersion.setVisibility(View.VISIBLE);
                txtSnVersion.setVisibility(View.VISIBLE);
                txtSnVersion.requestFocus();
            } else {
                if (isOutSourcing) {
                    mSnVersion.setVisibility(View.VISIBLE);
                    txtSnVersion.setVisibility(View.VISIBLE);
                    txtSnVersion.requestFocus();
                } else {
                    mSnVersion.setVisibility(View.GONE);
                    txtSnVersion.setVisibility(View.GONE);
                }

//                mSnVersion.setVisibility(View.GONE);
//                txtSnVersion.setVisibility(View.GONE);
            }

            btnConfim.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
            btnYes.setVisibility(View.GONE);
            btnNo.setVisibility(View.GONE);
        }
    }

    private void setInputFocus() {
        if (intCheckedID == R.id.radio_pallet) {
            if (txtPalletNo.isEnabled()) {
                txtPalletNo.requestFocus();
            } else {
                if (rbImportSn2.isChecked())
                    txtImportSN2.requestFocus();
                else
                    rbImportCN3.requestFocus();

                if (txtPalletVersion.getVisibility() == View.VISIBLE && TextUtils.isEmpty(txtPalletVersion.getText().toString().trim())) {
                    txtPalletVersion.requestFocus();
                }
            }
        } else if (intCheckedID == R.id.radio_box) {
            if (txtImportFracWeight.isEnabled()) {
                txtImportFracWeight.requestFocus();
            } else {
                if (intCheckedBoxID == R.id.radiobutton_import_sn) {
                    rbImportSN.setChecked(true);
                    txtImportSN.setEnabled(true);
                    txtImportSN.requestFocus();
                } else if (intCheckedBoxID == R.id.radiobutton_import_cn) {
                    rbImportCN.setChecked(true);
                    txtImportCN.setEnabled(true);
                    txtImportCN.requestFocus();
                }

                if (txtBoxVersion.getVisibility() == View.VISIBLE && TextUtils.isEmpty(txtBoxVersion.getText().toString().trim())) {
                    txtBoxVersion.requestFocus();
                }
            }
        } else if (intCheckedID == R.id.radio_sn) {
            if (txtImportSN3.getVisibility() == View.VISIBLE) {
                txtImportSN3.requestFocus();
            } else {
                txtPalletNo2.requestFocus();
            }

            if (txtSnVersion.getVisibility() == View.VISIBLE && TextUtils.isEmpty(txtSnVersion.getText().toString().trim())) {
                txtSnVersion.requestFocus();
            }
        } else if (intCheckedID == R.id.radio_sn_qrcode) {
            txtQrCode.requestFocus();

            if (txtImportFracWeightQrCode.isEnabled()) {
                txtImportFracWeightQrCode.requestFocus();
            } else {
                if (txtImportCNQrCode.isEnabled()) {
                    txtImportCNQrCode.requestFocus();
                } else {
                    txtQrCode.requestFocus();
                }

                if (txtQrCodeVersion.getVisibility() == View.VISIBLE && TextUtils.isEmpty(txtQrCodeVersion.getText().toString().trim())) {
                    txtQrCodeVersion.requestFocus();
                }
            }
        }
    }

    private void setBoxControlStatus(boolean enabled) {
        if (intCheckedID == R.id.radio_box) {
            if (enabled) {
                txtImportSN.setEnabled(isBoxEditTextEnabled);
                txtImportCN.setEnabled(isSnEditTextEnabled);
                txtImportFracWeight.setEnabled(isBoxWeightEditeTextEnabled);
            } else {
                isSnEditTextEnabled = txtImportSN.isEnabled();
                isBoxEditTextEnabled = txtImportCN.isEnabled();
                isBoxWeightEditeTextEnabled = txtImportFracWeight.isEnabled();
                txtImportSN.setEnabled(enabled);
                txtImportCN.setEnabled(enabled);
                txtImportFracWeight.setEnabled(enabled);
            }
        } else if (intCheckedID == R.id.radio_sn_qrcode) {
            if (enabled) {
                txtImportCN.setEnabled(isBoxEditTextEnabledQrCode);
                txtImportFracWeight.setEnabled(isBoxWeightEditeTextEnabledQrCode);
            } else {
                isBoxEditTextEnabledQrCode = txtImportCNQrCode.isEnabled();
                isBoxWeightEditeTextEnabledQrCode = txtImportFracWeightQrCode.isEnabled();
                txtImportCNQrCode.setEnabled(false);
                txtImportFracWeightQrCode.setEnabled(false);
            }
        }
    }

    private void updateItemQtyInfo() {
        mPreCheckQty.setText(getString(R.string.label_prechecking_quantity2, itemQty.getSnOnHand()));
        mPreCheckQty.setVisibility(View.VISIBLE);
        btnConfim.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);

        if (intCheckedID == R.id.radio_pallet) {
            rbImportSn2.setEnabled(false);
            txtImportSN2.setEnabled(false);
            rbImportCN3.setEnabled(false);
            txtImportCN3.setEnabled(false);
            txtPalletNo.setEnabled(true);
            txtPalletNo.requestFocus();
        } else if (intCheckedID == R.id.radio_box) {
            txtImportCN.clearFocus();

            if (rbImportSN.isChecked()) {
                txtImportSN.setText(itemQty.getSerialNo());
                txtImportCN.setText(itemQty.getSnBoxPltNo());
            } else {
                txtImportSN.setText(itemQty.getSnBoxPltNo());
                txtImportCN.setText(itemQty.getSerialNo());
            }

            rbImportSN.setEnabled(false);
            txtImportSN.setEnabled(false);
            rbImportCN.setEnabled(false);
            txtImportFracWeight.setEnabled(true);

            if (isMerakiProduct) {
                txtImportFracWeight.requestFocus();
            } else {
                txtImportCN.setEnabled(true);
                txtImportCN.requestFocus();
            }
        } else if (intCheckedID == R.id.radio_sn_qrcode) {
            txtImportCNQrCode.clearFocus();

            if (txtImportCNQrCode.isEnabled()) {
                txtImportCNQrCode.setText(itemQty.getSerialNo());
            } else {
                txtImportCNQrCode.setText(itemQty.getSnBoxPltNo());
                if (getCustomerName().contains(CUSTOMER_SEASONIC)) {
                    String cn = getCartonNo(txtQrCode.getText().toString().trim());

                    if (!TextUtils.isEmpty(cn)) {
                        txtImportCNQrCode.setText(cn);
                    }
                }
            }

            txtImportCNQrCode.setEnabled(false);
            txtImportFracWeightQrCode.setEnabled(true);

            if (isMerakiProduct) {
                txtImportFracWeightQrCode.requestFocus();
            } else {
                txtImportCNQrCode.setEnabled(true);
                txtImportCNQrCode.requestFocus();
            }
        }
    }

    private boolean checkCheckingQty() {
        if (oeInfo == null) {
            if (itemQty.getSnOnHand() + itemInfo.getPass() > itemInfo.getQty()) {
                Toast.makeText(getApplicationContext(), getString(R.string.exceeded_qty_to_be_picked) + " 1", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            if (itemQty.getSnOnHand() + oeInfo.getPass() > oeInfo.getQty()) {
                Toast.makeText(getApplicationContext(), getString(R.string.exceeded_qty_to_be_picked) + " 2", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    private void updateItemInfo() {
        btnConfim.setVisibility(View.INVISIBLE);
        btnCancel.setVisibility(View.INVISIBLE);

        if (oeInfo == null) {
            itemInfo.setQty(packingQtyInfo.getQty());
            itemInfo.setPass(packingQtyInfo.getPickQty());
            itemInfo.setWait(packingQtyInfo.getNonPickQty());
            mCheckingQty.setText(getString(R.string.label_picking_qty, itemInfo.getQty(), itemInfo.getPass(), itemInfo.getWait()));

            if (itemInfo.getPass() == itemInfo.getQty()) {
                returnPage();
            } else {
                setInputFocus();
            }
        } else {
            oeInfo.setQty(packingQtyInfo.getQty());
            oeInfo.setPass(packingQtyInfo.getPickQty());
            oeInfo.setWait(packingQtyInfo.getNonPickQty());
            mCheckingQty.setText(getString(R.string.label_picking_qty, oeInfo.getQty(), oeInfo.getPass(), oeInfo.getWait()));

            if (oeInfo.getPass() == oeInfo.getQty()) {
                returnPage();
            } else {
                setInputFocus();
            }
        }

        if (intCheckedID == R.id.radio_sn) {
            intSNCheckedCount++;
            mCheckingSN.setText(getString(R.string.label_sn_seq, intSNCheckedCount, txtImportQty.getText().toString().trim()));

            if (intSNCheckedCount == Integer.parseInt(txtImportQty.getText().toString().trim())) {
                returnPage();
            }
        } else {
            mPreCheckQty.setText(getString(R.string.label_prechecking_quantity));
            mPreCheckQty.setVisibility(View.INVISIBLE);

            if (intCheckedID == R.id.radio_pallet) {
                txtPalletNo.setEnabled(false);
                //showAskUpdatePallet();
            }
        }
    }

    private void returnPage() {
        if (needRefresh) {
            Intent resultData = new Intent();

            if (oeInfo == null) {
                resultData.putExtra("ITEM_INFO", new Gson().toJson(itemInfo));

                if (itemInfo.getWait() == 0) {
                    setResult(RESULT_OK, resultData);
                } else {
                    setResult(RESULT_FIRST_USER, resultData);
                }
            } else {
                resultData.putExtra("OE_INFO", new Gson().toJson(oeInfo));

                if (oeInfo.getWait() == 0) {
                    setResult(RESULT_OK, resultData);
                } else {
                    setResult(RESULT_FIRST_USER, resultData);
                }
            }
        }

        finish();
    }

    public void onBackPressed() {
        // do something here and don't write super.onBackPressed()
        returnPage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.shipment_detail, menu);
        return true;
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

        checkOoba();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ENABLE_BT || requestCode == REQUEST_PRINTER_SETTING) {
                checkPrinterSetting();
            }
        } else {
            if (requestCode == REQUEST_ENABLE_BT || requestCode == REQUEST_BLUETOOTH_SETTINGS) {
                if (BtPrintLabel.instance(getApplicationContext()) && BtPrintLabel.connect()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.bt_connect_ok), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.not_connect_btprinter), Toast.LENGTH_LONG).show();
                }

                checkOoba();
            }
        }
    }

    private void printPoLabel(SamsaraPoInfoHelper helper) {
        dialog = ProgressDialog.show(ShipmentPickingActivity.this, getString(R.string.holdon),
                getString(R.string.printingLabel), true);
        errorInfo = "";

        for (String po : helper.getPoList()) {
            if (!BtPrintLabel.printPo(po)) {
                errorInfo = getString(R.string.printLabalFailed)+" ShipmentPickingActivity2";
                mConnection.setText(getString(R.string.printer_connect_error));
                mConnection.setTextColor(Color.RED);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                break;
            }
        }

        dialog.dismiss();
    }

    private void printPoLabel(String po, String oe) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.printingLabel), true);
        errorInfo = "";

        if (!BtPrintLabel.printPoAndOe(po, oe)) {
            errorInfo = getString(R.string.printLabalFailed)+" ShipmentPickingActivity3";
            mConnection.setText(getString(R.string.printer_connect_error));
            mConnection.setTextColor(Color.RED);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        dialog.dismiss();
    }

    private void showAskDialog(final String po, final String oe) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("列印");
        dialog.setMessage(getString(R.string.wrong_po_print_oe_po_question, oe, po));
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setCancelable(false);

        dialog.setNegativeButton(getString(R.string.no),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int arg1) {
                        dialog.dismiss();
                    }

                });
        dialog.setPositiveButton(getString(R.string.yes),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int arg1) {
                        dialog.dismiss();
                        printPoLabel(po, oe);
                    }
                });

        dialog.show();
    }

    private class CheckOoba extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Check OOBA from " + AppController.getServerInfo()
                    + AppController.getProperties("CheckOoba"));
            publishProgress("OOBA"+getString(R.string.data_querying));
            return shipment.checkOoba(oobaList);
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
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    oobaList = (OobaListHelper) result;
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    dialog.dismiss();
                    showOobaStatus();
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
                    dialog.dismiss();
                    setInputFocus();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                dialog.dismiss();
            }
        }

        private void showOobaStatus() {
            if (oobaList != null && oobaList.getOobaList() != null) {
                if (oobaList.getOobaList().length == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.SonicWALL_has_not_completed_OOBA), Toast.LENGTH_LONG).show();
                } else {
                    int notInQty = 0;
                    int nonPicyQty = 0;
                    String msg = "";
                    String nonPicyMsg = "";
                    String notInMsg = "";

                    for (OobaItem item : oobaList.getOobaList()) {
                        if (item.getPickDate() == null) {
                            nonPicyQty++;
                            nonPicyMsg += getString(R.string.label_SN) + ":" + item.getSn() + "\n" + getString(R.string.label_Box) + ":" + item.getBoxNo() + "\n" + getString(R.string.label_pallet2) + ":" + item.getPalletNo() + ";\n";
                            //C:代表入庫
                            if (item.getState() == null || !item.getState().equals("C")) {
                                notInQty++;
                                notInMsg += getString(R.string.label_SN) + ":" + item.getSn() + ";\n";
                            }
                            break;
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.OOBA_already_picked), Toast.LENGTH_LONG).show();
                            break;
                        }
                    }

                    if (nonPicyQty > 0) {
                        msg += getString(R.string.OOBA_not_picked) + nonPicyQty + getString(R.string.OOBA_not_picked2) + "\n" + nonPicyMsg;

                        if (notInQty > 0) {
                            msg += getString(R.string.OOBA_not_stocked_in) + notInQty + getString(R.string.OOBA_not_stocked_in2) + "\n" + notInMsg;
                        }

                        msg = msg.substring(0, msg.length() - 2);
                    }

                    if (!TextUtils.isEmpty(msg)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ShipmentPickingActivity.this);
                        builder.setMessage(msg).setTitle(getString(R.string.warn));
                        builder.setCancelable(false);
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK button
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            }
        }
    }

    private class GetPrintInfo extends AsyncTask<Integer, String, ShipmentPalletInfoHelper> {
        @Override
        protected ShipmentPalletInfoHelper doInBackground(Integer... params) {
            AppController.debug("Get DN Info from " + AppController.getServerInfo()
                    + AppController.getProperties("GetDNInfoPrint"));
            publishProgress(getString(R.string.downloading_data));
            return shipment.getShipmentDeliveryInfo(printInfo);
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
        protected void onPostExecute(ShipmentPalletInfoHelper result) {
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
                    printInfo = result;
                    getInvoiceNo(printInfo.getDnNo());
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

    private class GetOutSourcing extends  AsyncTask<Integer, String, ItemInfoHelper> {
        @Override
        protected ItemInfoHelper doInBackground(Integer... params) {
            AppController.debug("Query OutSourcing from " + AppController.getServerInfo()
                    + AppController.getProperties("GetOutSourcing"));
            publishProgress(getString(R.string.OutSourcing) + getString(R.string.data_querying));
            return shipment.getOutSourcing(itemInfo);
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
        protected void onPostExecute(ItemInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
//                    mQrCodeVersion.setVisibility(View.VISIBLE);
//                    txtQrCodeVersion.setVisibility(View.VISIBLE);
//                    txtQrCodeVersion.requestFocus();
                    isOutSourcing=true;
                    Toast.makeText(getApplicationContext(), getString(R.string.isOutSourcing), Toast.LENGTH_SHORT).show();
                } else {
//                    mQrCodeVersion.setVisibility(View.GONE);
//                    txtQrCodeVersion.setVisibility(View.GONE);
                    isOutSourcing=false;
                    Toast.makeText(getApplicationContext(), getString(R.string.is_not_OutSourcing), Toast.LENGTH_SHORT).show();
                }

                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
            } else {
                mConnection.setText(getString(R.string.OutSourcing_info) + getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                dialog.dismiss();
            }
        }
    }

    private class GetItemQtyInfo extends  AsyncTask<Integer, String, ItemQtyInfoHelper> {
        @Override
        protected ItemQtyInfoHelper doInBackground(Integer... params) {
            AppController.debug("Query ItemQtyInfo from " + AppController.getServerInfo()
                    + AppController.getProperties("GetItemQtyInfo"));
            publishProgress("ItemQtyInfo"+getString(R.string.data_querying));
            return shipment.getItemQtyInfo(itemQty);
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
        protected void onPostExecute(ItemQtyInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                itemQty = result;

                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText(R.string.hassubloc);
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    dialog.dismiss();
                    errorInfo = result.getSubLoc();
                    merakiPoInfoHelper.setCarton_no(result.getSnBoxPltNo());
                    updateItemQtyInfo();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    dialog.dismiss();
                    setInputFocus();

                    if (!TextUtils.isEmpty(itemQty.getPo()) && !itemQty.getPo().equals("-1")
                            && !TextUtils.isEmpty(itemQty.getOe()) && !itemQty.getOe().equals("-1")) {
                        showAskDialog(itemQty.getPo(), itemQty.getOe());
                    }
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                dialog.dismiss();
            }
        }
    }

    private class ChkPalletCarton extends AsyncTask<Integer, String, SnItemInfoHelper> {
        @Override
        protected SnItemInfoHelper doInBackground(Integer... params) {
            AppController.debug("Send SNItemInfo to " + AppController.getServerInfo()
                    + AppController.getProperties("ChkPalletCarton"));
//            publishProgress(getString(R.string.data_uploading));
            return shipment.chkPalletCarton(tempSNItem);
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
        protected void onPostExecute(SnItemInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            AppController.debug("ChkPalletCarton result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                tempSNItem = result;
                if (TextUtils.isEmpty(tempSNItem.getStrErrorBuf())) {
                    mConnection.setText(getString(R.string.data_updated_successfully));
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    dialog.dismiss();

                    dialog = ProgressDialog.show(
                            ShipmentPickingActivity.this, getString(R.string.holdon),
                            getString(R.string.data_uploading), true);
                    new SetSNItemInfo().execute(0);

//                    dialog = ProgressDialog.show(ShipmentPickingActivity.this,
//                            getString(R.string.holdon), getString(R.string.data_updating), true);
//                    new GetPackingQtyInfo().execute(0);
                } else {
                    mConnection.setText(tempSNItem.getStrErrorBuf());
                    errorInfo = result.getStrErrorBuf();//+"(sn_pda_upi_pkg.sn_chk_pallet_carton_info)"
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    dialog.dismiss();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                dialog.dismiss();
            }

//            setBoxControlStatus(true);

//            if (result != null) {
//                tempSNItem = result;

//                if (result.getIntRetCode() == ReturnCode.OK) {
//                    mConnection.setText(getString(R.string.data_updated_successfully));
//                    mConnection.setTextColor(Color.WHITE);
//                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
//                    errorInfo = "";
//                    dialog.dismiss();

//                    if (!TextUtils.isEmpty(result.getStrErrorBuf())) {
//                        Toast.makeText(getApplicationContext(), result.getStrErrorBuf(), Toast.LENGTH_LONG).show();
//                    }
//                    if (getCustomerName().contains(CUSTOMER_SAMSARA) && intCheckedID == R.id.radio_sn_qrcode) {
//                        samsaraSn = tempSNItem.getSerialNo();
//                    }

//                    cleanData();
//                    needRefresh = true;
//                    dialog = ProgressDialog.show(ShipmentPickingActivity.this,
//                            getString(R.string.holdon), getString(R.string.data_updating), true);
//                    new GetPackingQtyInfo().execute(0);
//                } else {
//                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
//                        mConnection.setText(getString(R.string.connect_error));
//                    } else {
//                        mConnection.setText(getString(R.string.db_return_error));
//                        setInputFocus();
//                    }

//                    errorInfo = result.getStrErrorBuf();
//                    mConnection.setTextColor(Color.RED);
//                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
//                    dialog.dismiss();

//                    if (!TextUtils.isEmpty(tempSNItem.getPo()) && !tempSNItem.getPo().equals("-1")
//                            && !TextUtils.isEmpty(tempSNItem.getOe()) && !tempSNItem.getOe().equals("-1")) {
//                        showAskDialog(tempSNItem.getPo(), tempSNItem.getOe());
//                    }
//                }
//            } else {
//                mConnection.setText(getString(R.string.can_not_connection));
//                mConnection.setTextColor(Color.WHITE);
//                mConnection.setBackgroundColor(Color.RED);
//                errorInfo = "";
//                dialog.dismiss();
//            }
        }
    }

    private class SetSNItemInfo extends AsyncTask<Integer, String, SnItemInfoHelper> {
        @Override
        protected SnItemInfoHelper doInBackground(Integer... params) {
            AppController.debug("Send SNItemInfo to " + AppController.getServerInfo()
                    + AppController.getProperties("SetSNItem"));
            publishProgress(getString(R.string.data_uploading));
            return shipment.setSNItemInfo(tempSNItem);
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
        protected void onPostExecute(SnItemInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);

            AppController.debug("SetSNItemInfo onPostExecute() result json = " + new Gson().toJson(result));
            setBoxControlStatus(true);

            if (result != null) {
                tempSNItem = result;

                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText(getString(R.string.data_updated_successfully));
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    dialog.dismiss();

                    if (!TextUtils.isEmpty(result.getStrErrorBuf())) {
                        Toast.makeText(getApplicationContext(), result.getStrErrorBuf(), Toast.LENGTH_LONG).show();
                    }

                    if (getCustomerName().contains(CUSTOMER_SAMSARA) && intCheckedID == R.id.radio_sn_qrcode) {
                        samsaraSn = tempSNItem.getSerialNo();
                    }

                    if (getCustomerName().contains(CUSTOMER_MERAKI) || getCustomerName().contains(CUSTOMER_CISCO)) {
                        merakiSn = tempSNItem.getSerialNo();
                        merakiPoInfoHelper.setSn(merakiSn);
                        merakiPoInfoHelper.setCarton_no(tempSNItem.getCartonNo());
                    }

                    cleanData();
                    needRefresh = true;
                    dialog = ProgressDialog.show(ShipmentPickingActivity.this,
                            getString(R.string.holdon), getString(R.string.data_updating), true);
                    new GetPackingQtyInfo().execute(0);
                    //new PrintMerakiPoInfo().execute(0); //Mark By Ann 20250328 (SENAO101-202503-0180)改由線上列印出此四項資訊,不需經由倉庫幫忙列印
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                        setInputFocus();
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    dialog.dismiss();

                    if (!TextUtils.isEmpty(tempSNItem.getPo()) && !tempSNItem.getPo().equals("-1")
                            && !TextUtils.isEmpty(tempSNItem.getOe()) && !tempSNItem.getOe().equals("-1")) {
                        showAskDialog(tempSNItem.getPo(), tempSNItem.getOe());
                    }
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                dialog.dismiss();
            }
        }
    }

    private class GetPackingQtyInfo extends AsyncTask<Integer, String, PackingQtyInfoHelper> {
        @Override
        protected PackingQtyInfoHelper doInBackground(Integer... params) {
            AppController.debug("Query PackingQtyInfo from " + AppController.getServerInfo()
                    + AppController.getProperties("GetPackingQtyInfo"));
            publishProgress(getString(R.string.data_updating));
            packingQtyInfo = new PackingQtyInfoHelper();
            packingQtyInfo.setDeliveryID(AppController.getDnInfo().getDeliveryID());
            packingQtyInfo.setItemID(itemInfo.getId());

            if (oeInfo != null) {
                packingQtyInfo.setOeID(oeInfo.getId());
            }

            return shipment.getPackingQtyInfo(packingQtyInfo);
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
        protected void onPostExecute(PackingQtyInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);

            AppController.debug("GetPackingQtyInfo onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    packingQtyInfo = result;
                    mConnection.setText(getString(R.string.data_updated_successfully));
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    dialog.dismiss();
                    errorInfo = "";

                    if (getCustomerName().contains(CUSTOMER_SAMSARA) && intCheckedID == R.id.radio_sn_qrcode) {
                        dialog = ProgressDialog.show(ShipmentPickingActivity.this,
                                getString(R.string.holdon), getString(R.string.data_updating), true);
                        new GetPoInfo().execute(0);
                    } else {
                        updateItemInfo();
                    }
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    dialog.dismiss();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                dialog.dismiss();
            }
        }
    }

    private class PrintMerakiPoInfo extends AsyncTask<Integer, String, MerakiPoInfoHelper> {
        @Override
        protected MerakiPoInfoHelper doInBackground(Integer... params) {
            AppController.debug("Get MerakiPoInfo from " + AppController.getServerInfo()
                    + AppController.getProperties("MerakiPoInfo"));
//            publishProgress(getString(R.string.data_reading));
//            merakiPoInfoHelper = new MerakiPoInfoHelper();
            merakiPoInfoHelper.setDn(AppController.getDnInfo().getDeliveryID());
//            merakiPoInfoHelper.setPartNo(itemInfo.getItemID());
//            merakiPoInfoHelper.setSn(merakiSn);
            return shipment.getMerakiPoInfo(merakiPoInfoHelper);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        @Override
        protected void onPostExecute(MerakiPoInfoHelper result) {
            AppController.debug("PrintMerakiPoInfo onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
//                    samsaraPoInfoHelper = result;
                    mConnection.setText(getString(R.string.data_updated_successfully));
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    dialog.dismiss();
                    errorInfo = "";

                    if (getCustomerName().contains(CUSTOMER_MERAKI) || getCustomerName().contains(CUSTOMER_CISCO)) {
//                        BtPrintLabel.printMerakiPO(result);

                        String[] poArray = result.getPoList();
                        Map<String, Integer> poCountMap = result.getPoCount();

                        for (String po : poArray) {
                            int count = 0;

                            if (poCountMap.containsKey(po)) {
                                count = poCountMap.get(po);
                            }

                            System.out.println("PO: " + po + " 的數量為: " + count);
//                            BtPrintLabel.printMerakiPO(po,"TW",count+"",result.getCarton_weight());

                            if (! BtPrintLabel.printMerakiPO(po,"TW",count+"",result.getCarton_weight())) {//printShipmentPalletLabel1()  printTESTPO()  printTest
                                errorInfo = getString(R.string.printLabalFailed);
                                mConnection.setText(getString(R.string.printer_connect_error));
                                mConnection.setTextColor(Color.RED);
                                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                            }
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.Can_not_find_MERAKI_PO), Toast.LENGTH_LONG).show();
                    }
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    dialog.dismiss();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                dialog.dismiss();
            }
//            updateItemInfo();
        }
    }

    private class GetPoInfo extends AsyncTask<Integer, String, SamsaraPoInfoHelper> {
        @Override
        protected SamsaraPoInfoHelper doInBackground(Integer... params) {
            AppController.debug("Get Samsara Po Info from " + AppController.getServerInfo()
                    + AppController.getProperties("SamsaraPoInfo"));
            publishProgress(getString(R.string.data_reading));
            samsaraPoInfoHelper = new SamsaraPoInfoHelper();
            samsaraPoInfoHelper.setDn(AppController.getDnInfo().getDeliveryID());
            samsaraPoInfoHelper.setPartNo(itemInfo.getItemID());
            samsaraPoInfoHelper.setSn(samsaraSn);
            return shipment.getSamsaraPoInfo(samsaraPoInfoHelper);
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
        protected void onPostExecute(SamsaraPoInfoHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    samsaraPoInfoHelper = result;
                    mConnection.setText(getString(R.string.data_updated_successfully));
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    dialog.dismiss();
                    errorInfo = "";

                    if (samsaraPoInfoHelper.getPoList() != null && samsaraPoInfoHelper.getPoList().length > 0) {
                        printPoLabel(samsaraPoInfoHelper);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.Can_not_find_Samsara_PO), Toast.LENGTH_LONG).show();
                    }
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    dialog.dismiss();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
                dialog.dismiss();
            }

            updateItemInfo();
        }
    }

    private class GetIsLotcode extends AsyncTask<Integer, String, LotcodePermissionHelper> {
        @Override
        protected LotcodePermissionHelper doInBackground(Integer... params) {
            AppController.debug("Get Lotcode = " + AppController.getServerInfo()
                    + AppController.getProperties("GetIsLotcode"));
//            publishProgress("資料下載中...");
            return shipment.getLotcodePermission(permissionHelper);
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
        protected void onPostExecute(LotcodePermissionHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);

            AppController.debug("onPostExecute() Lotcode result json = " + new Gson().toJson(result));

            if (result != null) {
                AppController.debug("result.getPermission() = " + result.getPermission());

                if (result.getPermission()==1) {
                    checklotcode.setVisibility(View.VISIBLE);
                    mLotcode_txt.requestFocus();
                } else if(result.getPermission()==0) {
                    checklotcode.setVisibility(View.GONE);
                } else {
                    mConnection.setText("無法判斷Lotcode權限P:"+result.getPermission());
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.RED);
                }
            } else {
                mConnection.setText("無法判斷Lotcode權限");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
            }
        }
    }

    public class ChkDNInfo extends AsyncTask<Integer, String, ChkDeliveryInfoHelper> {
        @Override
        protected ChkDeliveryInfoHelper doInBackground(Integer... params) {
            AppController.debug("Get DeliveryInfo from " + AppController.getServerInfo()
                    + AppController.getProperties("ChkDNInfo"));
            publishProgress(getString(R.string.downloading_data));
            return verifyHandler.getDNInfo();
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
        protected void onPostExecute(ChkDeliveryInfoHelper result) {
            // execution of result of Long time consuming operation //
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                dnInfo = result;
                errorInfo = "";
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                printLabel();

                if (isUpdatePalletNumber) {
                    isUpdatePalletNumber = false;

                    if (intCheckedID == R.id.radio_box) {
                        showBoxDetail();
                    } else if (intCheckedID == R.id.radio_sn_qrcode) {
                        showQrCodeDetail();
                    }
                } else {
                    if (intCheckedID == R.id.radio_sn) {
                        showSNDetail(true);
                    } else if (intCheckedID == R.id.radio_pallet) {
                        tempSNItem.setDeliveryID(AppController.getDnInfo().getDeliveryID());
                        tempSNItem.setItemID(itemInfo.getId());
                        tempSNItem.setItemNo(itemInfo.getItemID());
                        tempSNItem.setCustomer(getCustomerName());
                        tempSNItem.setLotcode(mLotcode_txt.getText().toString().trim());

                        if (txtPalletVersion.getVisibility() == View.VISIBLE) {
                            String rev = txtPalletVersion.getText().toString().trim();
                            String[] values = rev.split("-");
                            rev = values[values.length - 1];
                            tempSNItem.setRev(rev);
                        } else {
                            tempSNItem.setRev(null);
                        }

                        if (oeInfo != null) {
                            tempSNItem.setOeID(oeInfo.getId());
                        }

                        dialog = ProgressDialog.show(ShipmentPickingActivity.this, getString(R.string.holdon),
                                getString(R.string.data_uploading), true);
                        new SetSNItemInfo().execute(0);
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

    private String getCustomer() {
        return AppController.getDnInfo().getCustomer().split(" ")[0].toUpperCase();
    }

    /*20231115 Milla
     *ShowLotCode 設定是否要顥示LotCode
     * */
    private void ShowLotCode(boolean bool_visible){
        if (bool_visible) {
            AppController.debug("S========>>>>>getCustomer():" +getCustomer());
            AppController.debug("S========>>>>>itemInfo.getId():" +itemInfo.getId());

            if (getCustomer().equals(CUSTOMER_VERKADA)) {
                AppController.debug("S========>>>>>1");
                permissionHelper = new LotcodePermissionHelper();
                permissionHelper.setItemId(itemInfo.getId());
                new GetIsLotcode().execute(0);
            }
        } else {
            checklotcode.setVisibility(View.GONE);
        }
    }
}
