package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.CompoundButton;
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
import com.senao.warehouse.database.CONTAINER_LABEL_QR_CODE_FORMAT;
import com.senao.warehouse.database.ChkDeliveryInfoHelper;
import com.senao.warehouse.database.ChkSnItemInfoHelper;
import com.senao.warehouse.database.DnShipWayHelper;
import com.senao.warehouse.database.PALLET_LABEL_QR_CODE_FORMAT;
import com.senao.warehouse.database.PalletCartonInfoHelper;
import com.senao.warehouse.database.PrintShipDocHelper;
import com.senao.warehouse.handler.ShippingVerifyMainHandler;
import com.senao.warehouse.util.QrCodeUtil;
import com.senao.warehouse.util.ReturnCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShippingVerifyMainActivity extends Activity {

    private static final String TAG = ShippingVerifyMainActivity.class.getSimpleName();
    private final String CUSTOMER_MERAKI = "MERAKI";
    private final String CUSTOMER_SONICWALL = "SONICWALL";
    private final String CUSTOMER_SAMSARA = "SAMSARA";
    private final String CUSTOMER_EXTREME = "EXTREME";
    private final String CUSTOMER_FORTINET = "FORTINET";
    private final String CUSTOMER_ENGENIUS = "ENGENIUS";
    private final String CUSTOMER_SEASONIC = "海韻";
    private final String CUSTOMER_VERKADA = "VERKADA";

    private TextView mConnection, mCustomer, mPlanningShippingDate,
            mInventoryNo, mOrderStatus, mContainerNum, mContainerSize,
            mSealNum, mCargoNum, mCarSize, mPickupName, mChecking, mShipRemark,
            mPrintDoc, mTotalPalletQty, mTotalCartonQty,lblTitle;
    private EditText txtDNNumber, txtContainerNum, txtContainerSize,
            txtSealNum, txtCargoNum, txtCarSize, txtPickupName, txtChecking,
            txtShipRemark, txtImportSn, txtImportCn, txtSnQrCode, txtContainerQrCode;
    private RadioButton rbContainer, rbPallet, rbCarton, rbPickup,
            rbPalletChecking, rbBoxChecking, rbSNChecking, rbSnQRCodeChecking, rbContainerQrCode, rbContainerCondition;
    private LinearLayout lShippingLayout, lMainLayout, lCheckingLayout,
            lRemarkLayout, lQrCodeLayout, lContainerQrCodeLayout;
    private RadioGroup rgShippingWay, rgChecking, rgContainer;
    private Button btnReturn, btnConfirm, btnCancel, btnYes, btnNo;
    private ChkDeliveryInfoHelper dnInfo;
    private DnShipWayHelper dnShipWay;
    private ChkSnItemInfoHelper snItem;
    private PrintShipDocHelper shipDoc;
    private ShippingVerifyMainHandler verify;
    private ProgressDialog dialog;
    private int intCheckedID;
    private String strCheckingNumber;
    private String errorInfo = "";
    private LinearLayout lSummaryLayout;
    private LinearLayout lSnCnLayout;
    private RadioButton rbImportSn;
    private RadioButton rbImportCn;
    private PalletCartonInfoHelper palletCartonInfo;

    private CompoundButton.OnCheckedChangeListener rbListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                if (buttonView.equals(rbImportSn)) {
                    txtImportSn.setEnabled(true);
                    rbImportCn.setChecked(false);
                    txtImportCn.setEnabled(false);
                } else {
                    txtImportCn.setEnabled(true);
                    rbImportSn.setChecked(false);
                    txtImportSn.setEnabled(false);
                }
            }
        }

    };

    private RadioGroup.OnCheckedChangeListener listener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            intCheckedID = checkedId;

            switch (checkedId) {
                case R.id.radio_container:
                    Log.d("onClick Radio Container", String.valueOf(checkedId));

                    if (rbContainer.isChecked()) {
                        setShippingLayout(View.VISIBLE, true, View.GONE, View.GONE);
                    }

                    break;

                case R.id.radio_pallet:

                case R.id.radio_carton:
                    Log.d("onClick Radio Pallet", String.valueOf(checkedId));

                    if (rbPallet.isChecked() || rbCarton.isChecked()) {
                        setShippingLayout(View.GONE, false, View.VISIBLE, View.GONE);
                    }

                    break;

                case R.id.radio_pickup:
                    Log.d("onClick Radio Pickup", String.valueOf(checkedId));

                    if (rbPickup.isChecked()) {
                        setShippingLayout(View.GONE, false, View.GONE, View.VISIBLE);
                    }

                    break;

                case R.id.radio_pallet_checking:
                    Log.d(TAG, "onClick Radio Pallet Checking" + String.valueOf(checkedId));

                    if (rbPalletChecking.isChecked()) {
                        setCheckingValue(R.string.label_pallet_num);
                    }

                    break;

                case R.id.radio_box_checking:
                    Log.d(TAG, "onClick Radio Box Checking" + String.valueOf(checkedId));

                    if (rbBoxChecking.isChecked()) {
                        setCheckingValue(R.string.label_carton_num);
                    }

                    break;

                case R.id.radio_sn_checking:
                    Log.d(TAG, "onClick Radio SN Checking" + String.valueOf(checkedId));

                    if (rbSNChecking.isChecked()) {
                        setCheckingValue(R.string.label_serial_num);
                    }

                    break;

                case R.id.radio_sn_qrcode_checking:
                    Log.d(TAG, "onClick Radio QR Code Checking" + String.valueOf(checkedId));

                    if (rbSnQRCodeChecking.isChecked()) {
                        setCheckingValue(R.string.label_qrcode);
                    }

                    break;
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener rgContainerListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio_container_qrcode:
                    if (rbContainerQrCode.isChecked()) {
                        setShippingLayout(View.VISIBLE, true, View.GONE, View.GONE);
                    }

                    break;
                case R.id.radio_container_condition:
                    if (rbContainerCondition.isChecked()) {
                        setShippingLayout(View.VISIBLE, false, View.GONE, View.GONE);
                    }

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_verify_main);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ShippingVerifyMainActivity.this);
                    dialog.setTitle("Error Msg");
                    dialog.setMessage(errorInfo);
                    dialog.setIcon(android.R.drawable.ic_dialog_alert);
                    dialog.setCancelable(false);
                    //20220701
                    dialog.setPositiveButton(getString(R.string.label_Detail),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int arg1) {
                                    String dn = txtDNNumber.getText().toString().trim();
                                    Intent intent = new Intent();
                                    intent.setClass(ShippingVerifyMainActivity.this, ShipmentActivity.class);
                                    intent.putExtra("ERRORDN", dn);
                                    startActivity(intent);
//                                    finish();
                                }

                            });
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

        mCustomer = findViewById(R.id.textview_customer);
        mOrderStatus = findViewById(R.id.textview_order_status);
        mPlanningShippingDate = findViewById(R.id.textview_planning_shipping_date);
        mInventoryNo = findViewById(R.id.textview_inventory_no);

        txtDNNumber = findViewById(R.id.input_data_DN);
        lMainLayout = findViewById(R.id.main_layout);

        rbContainer = findViewById(R.id.radio_container);
        rbPallet = findViewById(R.id.radio_pallet);
        rbCarton = findViewById(R.id.radio_carton);
        rbPickup = findViewById(R.id.radio_pickup);
        rgShippingWay = findViewById(R.id.radio_group_shipping_way);

        lSummaryLayout = findViewById(R.id.summary_layout);
        mTotalPalletQty = findViewById(R.id.textViewTotalPalletQty);
        mTotalCartonQty = findViewById(R.id.textViewTotalCartonQty);

        lShippingLayout = findViewById(R.id.shipping_layout);
        txtContainerQrCode = findViewById(R.id.edittext_import_container_qrcode);

        rgContainer = findViewById(R.id.radio_group_container);
        rgContainer.setOnCheckedChangeListener(rgContainerListener);
        rbContainerQrCode = findViewById(R.id.radio_container_qrcode);
        rbContainerCondition = findViewById(R.id.radio_container_condition);
        lContainerQrCodeLayout = findViewById(R.id.containerQrcodeLayout);

        mContainerNum = findViewById(R.id.label_container_num);
        mContainerSize = findViewById(R.id.label_container_size);
        mSealNum = findViewById(R.id.label_seal_num);
        mCargoNum = findViewById(R.id.label_cargo_num);
        mCarSize = findViewById(R.id.label_car_size);
        mPickupName = findViewById(R.id.label_pickup_name);

        txtContainerNum = findViewById(R.id.edittext_container_num);
        txtContainerSize = findViewById(R.id.edittext_container_size);
        txtSealNum = findViewById(R.id.edittext_seal_num);
        txtCargoNum = findViewById(R.id.edittext_cargo_num);
        txtCarSize = findViewById(R.id.edittext_car_size);
        txtPickupName = findViewById(R.id.edittext_pickup_name);

        lCheckingLayout = findViewById(R.id.checking_layout);

        rbPalletChecking = findViewById(R.id.radio_pallet_checking);
        rbBoxChecking = findViewById(R.id.radio_box_checking);
        rbSNChecking = findViewById(R.id.radio_sn_checking);
        rbSnQRCodeChecking = findViewById(R.id.radio_sn_qrcode_checking);
        rgChecking = findViewById(R.id.radio_group_checking);
        mChecking = findViewById(R.id.label_checking);
        txtChecking = findViewById(R.id.edittext_checking);

        lSnCnLayout = findViewById(R.id.sncn_layout);
        rbImportSn = findViewById(R.id.radiobutton_import_sn);
        rbImportCn = findViewById(R.id.radiobutton_import_cn);
        txtImportSn = findViewById(R.id.edittext_import_sn);
        txtImportCn = findViewById(R.id.edittext_import_cn);
        rbImportSn.setOnCheckedChangeListener(rbListener);
        rbImportCn.setOnCheckedChangeListener(rbListener);

        lQrCodeLayout = findViewById(R.id.qrcode_layout);
        txtSnQrCode = findViewById(R.id.edittext_import_sn_qrcode);

        lRemarkLayout = findViewById(R.id.remark_layout);

        mShipRemark = findViewById(R.id.label_ship_remark);
        mPrintDoc = findViewById(R.id.label_print_shipping_doc);

        txtShipRemark = findViewById(R.id.edittext_ship_remark);

        btnReturn = findViewById(R.id.button_return);
        btnConfirm = findViewById(R.id.button_confirm);
        btnCancel = findViewById(R.id.button_cancel);
        btnYes = findViewById(R.id.button_yes);
        btnNo = findViewById(R.id.button_no);

        txtChecking.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    hideKeyboard(v);
                    Log.d(TAG, "onKeyListener Enter or Action Down" + v.getId());
                    String palletNo = txtChecking.getText().toString().trim();

                    if (!TextUtils.isEmpty(palletNo) && (rbSnQRCodeChecking.isChecked() || rbPalletChecking.isChecked())) {
                        setPalletNo(palletNo);

                        if (lSnCnLayout.getVisibility() == View.VISIBLE) {
                            if (rbImportSn.isChecked()) {
                                txtImportSn.requestFocus();
                            } else {
                                txtImportCn.requestFocus();
                            }
                        }

                        if (lQrCodeLayout.getVisibility() == View.VISIBLE) {
                            txtSnQrCode.requestFocus();
                        }
                    }

                    return true;
                }

                return false;
            }
        });

        txtDNNumber.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    hideKeyboard(v);
                    Log.d(TAG, "onKeyListener Enter or Action Down" + v.getId());
                    String dn = txtDNNumber.getText().toString().trim();

                    if (TextUtils.isEmpty(dn)) {
                        Toast.makeText(ShippingVerifyMainActivity.this, getString(R.string.dn_num_is_not_null), Toast.LENGTH_SHORT).show();
                        return true;
                    } else {
                        String result = parseDn(dn);

                        if (TextUtils.isEmpty(result)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.cant_resolve_dn), Toast.LENGTH_LONG).show();
                            return true;
                        } else {
                            txtDNNumber.setText(result);
                            dialog = ProgressDialog.show(ShippingVerifyMainActivity.this, getString(R.string.holdon),
                                    getString(R.string.downloading_data), true);
                            dnInfo = new ChkDeliveryInfoHelper();
                            dnInfo.setDeliveryID(Integer.parseInt(txtDNNumber.getText().toString().trim()));
                            verify = new ShippingVerifyMainHandler(dnInfo);
                            new ChkDNInfo().execute(0);
                        }
                    }
                }

                return false;
            }
        });

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Return", String.valueOf(v.getId()));

                if (lShippingLayout.getVisibility() == View.VISIBLE) {
                    cleanShippingLayoutData();
                    setMainLayout();
                    Log.d("onClick Return", "setMainLayout " + lShippingLayout.getVisibility());
                } else if (lCheckingLayout.getVisibility() == View.VISIBLE) {
                    Log.d("onClick Return", "setShippingLayout " + lCheckingLayout.getVisibility());
                    rgChecking.clearCheck();
                    listener.onCheckedChanged(rgShippingWay, rgShippingWay.getCheckedRadioButtonId());
                } else if (lRemarkLayout.getVisibility() == View.VISIBLE) {
                    if (mPrintDoc.getVisibility() == View.VISIBLE) {
                        Log.d("onClick Return", "setRemarkLayout " + mPrintDoc.getVisibility());
                        setRemarkLayout(false, false);
                    } else {
                        Log.d("onClick Return", "setCheckingLayout " + lRemarkLayout.getVisibility());
                        setCheckingLayout();
                        listener.onCheckedChanged(rgChecking, rgChecking.getCheckedRadioButtonId());
                        txtChecking.setText(strCheckingNumber);
                    }
                } else {
                    if (txtDNNumber.isEnabled()) {
                        returnPage();
                    } else {
                        setMainLayout();
                        clearDNInfo();
                    }
                }
            }

        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Confim", String.valueOf(v.getId()));
                hideKeyboard();

                if (lShippingLayout.getVisibility() == View.VISIBLE) {
                    Log.d("onClick Confirm", "setCheckingLayout " + lShippingLayout.getVisibility());

                    if (checkFields()) {
                        dialog = ProgressDialog.show(ShippingVerifyMainActivity.this, getString(R.string.holdon),
                                getString(R.string.data_uploading), true);
                        setDNShipWayData();
                        new SetDNShipWay().execute(0);
                    }
                } else if (lCheckingLayout.getVisibility() == View.VISIBLE) {
                    Log.d("onClick Confirm", "setRemarkLayout " + lCheckingLayout.getVisibility());

                    if (checkFields()) {
                        dialog = ProgressDialog.show(ShippingVerifyMainActivity.this, getString(R.string.holdon),
                                getString(R.string.data_chking), true);
                        setSNItemInfoData();
                        new ChkSNItemInfo().execute(0);
                    }
                } else if (lRemarkLayout.getVisibility() == View.VISIBLE) {
                    Log.d("onClick Confirm", "setRemarkLayout Print " + lRemarkLayout.getVisibility());
                    setRemarkLayout(true, false);
                }
            }

        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Cancel", String.valueOf(v.getId()));

                if (lShippingLayout.getVisibility() == View.VISIBLE) {
                    cleanShippingLayoutData();
                    Log.d("onClick Cancel", "setMainLayout " + lShippingLayout.getVisibility());
                } else if (lCheckingLayout.getVisibility() == View.VISIBLE) {
                    Log.d("onClick Cancel", "setShippingLayout " + lCheckingLayout.getVisibility());
                    txtChecking.setText("");
                    txtImportSn.setText("");
                    txtImportCn.setText("");
                    txtSnQrCode.setText("");
                    txtChecking.requestFocus();
                } else if (lRemarkLayout.getVisibility() == View.VISIBLE) {
                    Log.d("onClick Cancel", "setCheckingLayout " + lRemarkLayout.getVisibility());
                    txtShipRemark.setText("");
                }
            }

        });

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Yes", String.valueOf(v.getId()));
                dialog = ProgressDialog.show(ShippingVerifyMainActivity.this,
                        getString(R.string.holdon), getString(R.string.data_printing), true);
                setShipDoc(true);
                new PrintShipDoc().execute(0);
            }

        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick No", String.valueOf(v.getId()));
                dialog = ProgressDialog.show(ShippingVerifyMainActivity.this,getString(R.string.holdon), getString(R.string.data_printing), true);
                setShipDoc(false);
                new SaveShipRemark().execute(0);
            }

        });

        lblTitle = findViewById(R.id.ap_title);
        setTitle();
        clearDNInfo();
        rgShippingWay.setOnCheckedChangeListener(listener);
        rgChecking.setOnCheckedChangeListener(listener);
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.label_shipment1, AppController.getOrgName()));
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private void setPalletNo(String palletNo) {
        if (!TextUtils.isEmpty(palletNo) && palletNo.contains("@")) {
            txtChecking.setText(String.format("%s%s", QrCodeUtil.getValueFromPalletLabelQrCode(txtChecking.getText().toString()
                    , PALLET_LABEL_QR_CODE_FORMAT.DN), QrCodeUtil.getValueFromPalletLabelQrCode(txtChecking.getText().toString(), PALLET_LABEL_QR_CODE_FORMAT.P_NO)));
        }
    }

    private void hideKeyboard(View edit) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtChecking.getWindowToken(), 0);
    }

    private String parseDn(String dn) {
        String result = null;

        if (!TextUtils.isEmpty(dn)) {
            if (dn.contains("@")) {
                String[] list = dn.split("@");

                if (list.length > 1) {
                    result = list[1];
                }
            } else {
                result = dn;
            }
        }

        return result;
    }

    private void setPalletCartonInfo() {
        mTotalPalletQty.setText(getString(R.string.label_total_pallet_quantity,
                palletCartonInfo.getPalletTotalQty(),
                palletCartonInfo.getPalletNonCheckQty()));
        mTotalCartonQty.setText(getString(R.string.label_total_carton_quantity,
                palletCartonInfo.getCartonTotalQty(),
                palletCartonInfo.getCartonNonCheckQty()));
    }

    private void resetPalletCartonInfo() {
        mTotalPalletQty.setText(getString(R.string.label_total_pallet_quantity,
                0, 0));
        mTotalCartonQty.setText(getString(R.string.label_total_carton_quantity,
                0, 0));
    }

    private boolean checkFields() {
        if (lShippingLayout.getVisibility() == View.VISIBLE) {
            int checkedId = rgShippingWay.getCheckedRadioButtonId();

            switch (checkedId) {
                case R.id.radio_container:
                    if (rbContainerCondition.isChecked()) {
                        if (txtContainerNum.getText().toString().trim().equals("")) {
                            txtContainerNum.requestFocus();
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_container_num), Toast.LENGTH_LONG).show();
                            return false;
                        }

                        if (txtContainerSize.getText().toString().trim().equals("")) {
                            txtContainerSize.requestFocus();
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_container_size), Toast.LENGTH_LONG).show();
                            return false;
                        }

                        if (txtSealNum.getText().toString().trim().equals("")) {
                            txtSealNum.requestFocus();
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_seal_num), Toast.LENGTH_LONG).show();
                            return false;
                        }

                        if (txtCargoNum.getText().toString().trim().equals("")) {
                            txtCargoNum.requestFocus();
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_freight_num), Toast.LENGTH_LONG).show();
                            return false;
                        }
                    } else {
                        String containerQrCode = txtContainerQrCode.getText().toString().trim();

                        if (containerQrCode.equals("")) {
                            txtContainerQrCode.requestFocus();
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_qrcode), Toast.LENGTH_LONG).show();
                            return false;
                        }

                        if (!parseContainerQrCode(containerQrCode)) {
                            txtContainerQrCode.requestFocus();
                            Toast.makeText(getApplicationContext(), getString(R.string.qrcode_format_incorrect), Toast.LENGTH_LONG).show();
                            return false;
                        }

                        if (getContainerNo().equals("")) {
                            txtContainerQrCode.requestFocus();
                            Toast.makeText(getApplicationContext(), getString(R.string.QR_Code_container_num_is_empty), Toast.LENGTH_LONG).show();
                            return false;
                        }

                        if (getContainerSize().equals("")) {
                            txtContainerQrCode.requestFocus();
                            Toast.makeText(getApplicationContext(), getString(R.string.QR_Code_container_size_is_empty), Toast.LENGTH_LONG).show();
                            return false;
                        }

                        if (getSealNo().equals("")) {
                            txtContainerQrCode.requestFocus();
                            Toast.makeText(getApplicationContext(),getString(R.string.QR_Code_seal_num_is_empty), Toast.LENGTH_LONG).show();
                            return false;
                        }

                        if (getCargoNo().equals("")) {
                            txtContainerQrCode.requestFocus();
                            Toast.makeText(getApplicationContext(), getString(R.string.QR_Code_shipping_line_num_is_empty), Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }

                    break;
                case R.id.radio_pallet:

                case R.id.radio_carton:
                    if (txtCargoNum.getText().toString().trim().equals("")) {
                        txtCargoNum.requestFocus();
                        Toast.makeText(getApplicationContext(), getString(R.string.enter_freight_num), Toast.LENGTH_LONG).show();
                        return false;
                    }
                    if (txtCarSize.getText().toString().trim().equals("")) {
                        txtCarSize.requestFocus();
                        Toast.makeText(getApplicationContext(),getString( R.string.enter_truck_tonnage), Toast.LENGTH_LONG).show();
                        return false;
                    }

                    break;
                case R.id.radio_pickup:
                    if (txtPickupName.getText().toString().trim().equals("")) {
                        txtPickupName.requestFocus();
                        Toast.makeText(getApplicationContext(), getString(R.string.enter_customs_broker_or_consignor), Toast.LENGTH_LONG).show();
                        return false;
                    }

                    break;
            }
        } else if (lCheckingLayout.getVisibility() == View.VISIBLE) {
            if (intCheckedID == R.id.radio_sn_qrcode_checking) {
                if (txtChecking.getText().toString().trim().equals("")) {
                    txtChecking.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_pallet_nu), Toast.LENGTH_LONG).show();
                    return false;
                }

                if (txtSnQrCode.getText().toString().trim().equals("")) {
                    txtSnQrCode.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_qrcode), Toast.LENGTH_LONG).show();
                    return false;
                }

                List<String> snList = getSnList(txtSnQrCode.getText().toString().trim());
                if (snList.isEmpty()) {
                    txtSnQrCode.requestFocus();
                    return false;
                }
            } else {
                if (txtChecking.getText().toString().trim().equals("")) {
                    txtChecking.requestFocus();
                    String text = "";

                    switch (intCheckedID) {
                        case R.id.radio_pallet_checking:
                            text = getString(R.string.enter_pallet_nu);
                            break;
                        case R.id.radio_box_checking:
                            text = getString(R.string.enter_box_nu); //"請填寫輸入箱號號碼"
                            break;
                        case R.id.radio_sn_checking:
                            text = getString(R.string.enter_sn); //"請填寫輸入序號號碼"
                            break;
                        case R.id.radio_sn_qrcode_checking:
                            text = getString(R.string.enter_pallet_nu);
                            break;
                    }

                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                    return false;
                }

                if (intCheckedID == R.id.radio_pallet_checking && !TextUtils.isEmpty(dnShipWay.getPalletCode())) {
                    if (!exsitInPalletCode(txtChecking.getText().toString().trim(), dnShipWay.getPalletCode())) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(ShippingVerifyMainActivity.this);
                        dialog.setTitle("Error Msg");
                        dialog.setMessage(getString(R.string.pallet_num_is_not) + dnShipWay.getPalletCode() + getString(R.string.check));
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
                        return false;
                    }
                }
            }

            if (lSnCnLayout.getVisibility() == View.VISIBLE) {
                if (rbImportSn.isChecked()) {
                    if (txtImportSn.getText().toString().trim().equals("")) {
                        txtImportSn.requestFocus();
                        Toast.makeText(getApplicationContext(), getString(R.string.enter_sn), Toast.LENGTH_LONG).show();
                        return false;
                    }
                } else {
                    if (txtImportCn.getText().toString().trim().equals("")) {
                        txtImportCn.requestFocus();
                        Toast.makeText(getApplicationContext(), getString(R.string.enter_box_no), Toast.LENGTH_LONG).show();
                        return false;
                    } else if (rbBoxChecking.isChecked()
                            && !txtImportCn.getText().toString().trim().equals(txtChecking.getText().toString().trim())) {
                        txtImportCn.requestFocus();
                        Toast.makeText(getApplicationContext(), getString(R.string.Carton_number_mismatch), Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean parseContainerQrCode(String qrCode) {
        if (TextUtils.isEmpty(qrCode)) {
            return false;
        } else {
            String[] items = qrCode.split("@");
            return items.length == CONTAINER_LABEL_QR_CODE_FORMAT.values().length;
        }
    }

    private String getCustomerName() {
        return dnInfo.getCustomer().split(" ")[0].toUpperCase();
    }

    private boolean exsitInPalletCode(String palletNo, String palletCode) {
        String[] list = palletCode.split("/");

        for (String item : list) {
            if (palletNo.contains(item)) {
                return true;
            }
        }

        return false;
    }

    private void setCheckingValue(int id) {
        mChecking.setText(getResources().getString(id));
        mChecking.setVisibility(View.VISIBLE);
        txtChecking.setText("");
        txtChecking.setVisibility(View.VISIBLE);
        txtChecking.requestFocus();
        lQrCodeLayout.setVisibility(View.GONE);
        txtSnQrCode.setVisibility(View.GONE);
        btnConfirm.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);

        if (id == R.string.label_serial_num) {
            txtImportSn.setVisibility(View.INVISIBLE);
            txtImportSn.setVisibility(View.INVISIBLE);
            lSnCnLayout.setVisibility(View.INVISIBLE);
        } else if (id == R.string.label_qrcode) {
            mChecking.setText(R.string.label_pallet_num);
            txtImportSn.setVisibility(View.GONE);
            txtImportSn.setVisibility(View.GONE);
            lSnCnLayout.setVisibility(View.GONE);
            //txtChecking.setVisibility(View.GONE);
            lQrCodeLayout.setVisibility(View.VISIBLE);
            txtSnQrCode.setVisibility(View.VISIBLE);
            txtSnQrCode.setText("");
            //txtSnQrCode.requestFocus();
        } else {
            lSnCnLayout.setVisibility(View.VISIBLE);
            txtImportSn.setVisibility(View.VISIBLE);
            txtImportSn.setVisibility(View.VISIBLE);
            rbImportSn.setChecked(true);
            txtImportSn.setText("");
            txtImportCn.setText("");
        }
    }

    private void setMainLayout() {
        rgShippingWay.clearCheck();
        lShippingLayout.setVisibility(View.GONE);
        lCheckingLayout.setVisibility(View.GONE);
        lSummaryLayout.setVisibility(View.GONE);
        lRemarkLayout.setVisibility(View.GONE);
        btnConfirm.setVisibility(View.INVISIBLE);
        btnCancel.setVisibility(View.INVISIBLE);
        btnYes.setVisibility(View.GONE);
        btnNo.setVisibility(View.GONE);
        lMainLayout.setVisibility(View.VISIBLE);
    }

    private void setShippingLayout(int isContainer, boolean isQrCode, int isPalletCarton, int isPickup) {
        lMainLayout.setVisibility(View.GONE);
        lShippingLayout.setVisibility(View.VISIBLE);
        lCheckingLayout.setVisibility(View.GONE);
        lSummaryLayout.setVisibility(View.GONE);
        lRemarkLayout.setVisibility(View.GONE);

        rgContainer.setVisibility(isContainer);
        lContainerQrCodeLayout.setVisibility(isContainer);

        mContainerNum.setVisibility(isContainer);
        mContainerSize.setVisibility(isContainer);
        mSealNum.setVisibility(isContainer);
        txtContainerNum.setVisibility(isContainer);
        txtContainerSize.setVisibility(isContainer);
        txtSealNum.setVisibility(isContainer);

        if (View.VISIBLE == isContainer || View.VISIBLE == isPalletCarton) {
            mCargoNum.setVisibility(View.VISIBLE);
            txtCargoNum.setVisibility(View.VISIBLE);
        } else {
            mCargoNum.setVisibility(View.GONE);
            txtCargoNum.setVisibility(View.GONE);
        }

        mCarSize.setVisibility(isPalletCarton);
        txtCarSize.setVisibility(isPalletCarton);

        mPickupName.setVisibility(isPickup);
        txtPickupName.setVisibility(isPickup);

        btnConfirm.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);
        btnYes.setVisibility(View.GONE);
        btnNo.setVisibility(View.GONE);

        if (txtContainerNum.getVisibility() == View.VISIBLE) {
            txtContainerNum.requestFocus();
        } else if (txtCargoNum.getVisibility() == View.VISIBLE) {
            txtCargoNum.requestFocus();
        } else if (txtPickupName.getVisibility() == View.VISIBLE) {
            txtPickupName.requestFocus();
        }

        if (View.VISIBLE == isContainer) {
            if (isQrCode) {
                rbContainerQrCode.setChecked(true);
                lContainerQrCodeLayout.setVisibility(View.VISIBLE);
                mContainerNum.setVisibility(View.GONE);
                mContainerSize.setVisibility(View.GONE);
                mSealNum.setVisibility(View.GONE);
                txtContainerNum.setVisibility(View.GONE);
                txtContainerSize.setVisibility(View.GONE);
                txtSealNum.setVisibility(View.GONE);
                mCargoNum.setVisibility(View.GONE);
                txtCargoNum.setVisibility(View.GONE);
                txtContainerQrCode.requestFocus();
            } else {
                rbContainerCondition.setChecked(true);
                lContainerQrCodeLayout.setVisibility(View.GONE);
                txtContainerNum.requestFocus();
            }
        }
    }

    private void setCheckingLayout() {
        lMainLayout.setVisibility(View.GONE);
        lShippingLayout.setVisibility(View.GONE);
        lCheckingLayout.setVisibility(View.VISIBLE);
        lQrCodeLayout.setVisibility(View.GONE);
        lSummaryLayout.setVisibility(View.VISIBLE);
        lRemarkLayout.setVisibility(View.GONE);

        mChecking.setVisibility(View.INVISIBLE);
        txtChecking.setVisibility(View.INVISIBLE);
        lSnCnLayout.setVisibility(View.INVISIBLE);

        txtSnQrCode.setVisibility(View.GONE);

        btnConfirm.setVisibility(View.INVISIBLE);
        btnCancel.setVisibility(View.INVISIBLE);
        btnYes.setVisibility(View.GONE);
        btnNo.setVisibility(View.GONE);
    }

    private void setRemarkLayout(boolean isPrint, boolean setDefaultRemark) {
        lMainLayout.setVisibility(View.GONE);
        lShippingLayout.setVisibility(View.GONE);
        lCheckingLayout.setVisibility(View.GONE);
        lSummaryLayout.setVisibility(View.GONE);
        lRemarkLayout.setVisibility(View.VISIBLE);

        if (isPrint) {
            mShipRemark.setVisibility(View.GONE);
            txtShipRemark.setVisibility(View.GONE);
            mPrintDoc.setVisibility(View.VISIBLE);
            btnConfirm.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
            btnYes.setVisibility(View.VISIBLE);
            btnNo.setVisibility(View.VISIBLE);
        } else {
            mShipRemark.setVisibility(View.VISIBLE);

            if (setDefaultRemark) {
                // txtShipRemark
                // .setText("1.煩請客戶（收件人）於\"派車單\"與\"銷貨單\"上簽名（請簽全名）及蓋章（收發章），正本再送回神準公司。謝謝！\n"
                // + "2.下貨完畢後煩請通知報關行貨物擺放位置（艙門或碼頭代碼）。謝謝！\n");
            }

            txtShipRemark.setVisibility(View.VISIBLE);
            mPrintDoc.setVisibility(View.GONE);
            btnConfirm.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
            btnYes.setVisibility(View.GONE);
            btnNo.setVisibility(View.GONE);
        }
    }

    private void clearDNInfo() {
        txtDNNumber.setEnabled(true);
        mCustomer.setText("");
        mPlanningShippingDate.setText("");
        mInventoryNo.setText("");
        mOrderStatus.setText("");
        lMainLayout.setVisibility(View.INVISIBLE);
        lShippingLayout.setVisibility(View.GONE);
        lCheckingLayout.setVisibility(View.GONE);
        lSummaryLayout.setVisibility(View.GONE);
        lRemarkLayout.setVisibility(View.GONE);
        btnConfirm.setVisibility(View.INVISIBLE);
        btnCancel.setVisibility(View.INVISIBLE);
        btnYes.setVisibility(View.GONE);
        btnNo.setVisibility(View.GONE);
    }

    private void cleanShippingLayoutData() {
        txtContainerNum.setText("");
        txtContainerSize.setText("");
        txtSealNum.setText("");
        txtCargoNum.setText("");
        txtCarSize.setText("");
        txtPickupName.setText("");
        txtContainerQrCode.setText("");
    }

    private void setListData() {
        txtDNNumber.setEnabled(false);
        mCustomer.setText(dnInfo.getCustomer());
        mPlanningShippingDate.setText(dnInfo.getShipDate());
        mInventoryNo.setText(dnInfo.getInvoiceNo());
        mOrderStatus.setText(dnInfo.getOpStatus());

        if (dnInfo.getOpStatus() != null && dnInfo.getOpStatus().equals("HOLD"))
            mOrderStatus.setTextColor(getResources().getColor(R.color.red));
        else
            mOrderStatus.setTextColor(getResources().getColor(R.color.blue));

        lMainLayout.setVisibility(View.VISIBLE);
    }

    private void returnPage() {
        // Intent intent = new Intent();
        // intent.setClass(ShippingVerifyMainActivity.this, MenuActivity.class);
        // startActivity(intent);
        finish();
    }

    private String getContainerNo() {
        if (rbContainerCondition.isChecked()) {
            return txtContainerNum.getText().toString().trim();
        } else {
            return QrCodeUtil.getValueFromContainerLabelQrCode(txtContainerQrCode.getText().toString().trim(), CONTAINER_LABEL_QR_CODE_FORMAT.CONTAINER_NO);
        }
    }

    private String getContainerSize() {
        if (rbContainerCondition.isChecked()) {
            return txtContainerSize.getText().toString().trim();
        } else {
            return QrCodeUtil.getValueFromContainerLabelQrCode(txtContainerQrCode.getText().toString().trim(), CONTAINER_LABEL_QR_CODE_FORMAT.SIZE);
        }
    }

    private String getSealNo() {
        if (rbContainerCondition.isChecked()) {
            return txtSealNum.getText().toString().trim();
        } else {
            return QrCodeUtil.getValueFromContainerLabelQrCode(txtContainerQrCode.getText().toString().trim(), CONTAINER_LABEL_QR_CODE_FORMAT.SEAL_NO);
        }
    }

    private String getCargoNo() {
        if (rbContainerCondition.isChecked()) {
            return txtCargoNum.getText().toString().trim();
        } else {
            return QrCodeUtil.getValueFromContainerLabelQrCode(txtContainerQrCode.getText().toString().trim(), CONTAINER_LABEL_QR_CODE_FORMAT.CARGO_NO);
        }
    }

    private void setDNShipWayData() {
        dnShipWay = new DnShipWayHelper();

        switch (intCheckedID) {
            case R.id.radio_container:
                dnShipWay.setShipWay(1);
                dnShipWay.setDeliveryID(Integer.parseInt(txtDNNumber.getText().toString()));
                dnShipWay.setContainerNo(getContainerNo());
                dnShipWay.setContainerSize(getContainerSize());
                dnShipWay.setSealNo(getSealNo());
                dnShipWay.setCargoNo(getCargoNo());
                break;
            case R.id.radio_pallet:
                dnShipWay.setShipWay(2);
                dnShipWay.setDeliveryID(Integer.parseInt(txtDNNumber.getText().toString()));
                dnShipWay.setCargoNo(txtCargoNum.getText().toString().trim());
                dnShipWay.setCarSize(txtCarSize.getText().toString().trim());
                break;
            case R.id.radio_carton:
                dnShipWay.setShipWay(3);
                dnShipWay.setDeliveryID(Integer.parseInt(txtDNNumber.getText().toString()));
                dnShipWay.setCargoNo(txtCargoNum.getText().toString().trim());
                dnShipWay.setCarSize(txtCarSize.getText().toString().trim());
                break;
            case R.id.radio_pickup:
                dnShipWay.setShipWay(4);
                dnShipWay.setDeliveryID(Integer.parseInt(txtDNNumber.getText().toString()));
                dnShipWay.setBrokerName(txtPickupName.getText().toString().trim());
                break;
        }
    }

    private void setSNItemInfoData() {
        snItem = new ChkSnItemInfoHelper();
        snItem.setDeliveryID(dnInfo.getDeliveryID());

        switch (intCheckedID) {
            case R.id.radio_pallet_checking:
                snItem.setTypeFlag(3);
                snItem.setPalletNo(txtChecking.getText().toString().trim());
                strCheckingNumber = txtChecking.getText().toString().trim();

                if (rbImportSn.isChecked())
                    snItem.setSerialNo(txtImportSn.getText().toString().trim());
                else
                    snItem.setCartonNo(txtImportCn.getText().toString().trim());

                break;
            case R.id.radio_box_checking:
                snItem.setTypeFlag(2);
                snItem.setCartonNo(txtChecking.getText().toString().trim());
                strCheckingNumber = txtChecking.getText().toString().trim();

                if (rbImportSn.isChecked())
                    snItem.setSerialNo(txtImportSn.getText().toString().trim());
                else
                    snItem.setCartonNo(txtImportCn.getText().toString().trim());

                break;
            case R.id.radio_sn_checking:
                snItem.setTypeFlag(1);
                snItem.setSerialNo(txtChecking.getText().toString().trim());
                strCheckingNumber = txtChecking.getText().toString().trim();
                break;
            case R.id.radio_sn_qrcode_checking:
                snItem.setTypeFlag(3);
                snItem.setPalletNo(txtChecking.getText().toString().trim());
                List<String> snList = getSnList(txtSnQrCode.getText().toString().trim());
                //snItem.setSnList(snList);
                snItem.setSerialNo(snList.get(0));
                //strCheckingNumber = snList.get(snList.size() - 1);
                strCheckingNumber = snList.get(0);
                break;
        }
    }

    private List<String> getSnList(String qrCode) {
        AppController.debug("getSnList" );
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
            snList = qrCode.split("[CR]");
        } else if (qrCode.indexOf(";") > 0) {
            snList = qrCode.split(";");
        } else {
            snList = new String[1];
            snList[0] = qrCode;
        }

        List<String> list = new ArrayList<>();

        if (getCustomerName().contains(CUSTOMER_FORTINET)) {
            list.addAll(Arrays.asList(snList));
        } else if (getCustomerName().contains(CUSTOMER_EXTREME)) {
            try {
                list.add(snList[0].split(":")[1]);
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), getString(R.string.qr_code_format_incorrect_cant_resolve_sn), Toast.LENGTH_LONG).show();
            }
        } else if (getCustomerName().contains(CUSTOMER_ENGENIUS)) {
            try {
                for (String s : snList) {
                    list.add(s.split(";")[0]);
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
                        }
                    }
                }
            }
        } else if (getCustomerName().contains(CUSTOMER_VERKADA)) {
            AppController.debug("getSnList>>>>>VERKADA");
            AppController.debug("getSnList>>>>>" + snList[0]);
            snList = snList[0].split(",");

            try {
                list.addAll(Arrays.asList(snList));
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
                    }
                }
            }
        }

        return list;
    }

    private void setShipDoc(boolean isPrint) {
        shipDoc = new PrintShipDocHelper();
        shipDoc.setDeliveryID(dnInfo.getDeliveryID());
        shipDoc.setRemark(txtShipRemark.getText().toString().trim());
        shipDoc.setPrint(isPrint);
    }

    public void onBackPressed() {
        // do something here and don't write super.onBackPressed()
        Log.d(TAG, "onClick Hardware Back Button");
        returnPage();
    }

    private void doGetPalletCartonInfo() {
        dialog = ProgressDialog.show(ShippingVerifyMainActivity.this, getString(R.string.holdon),
                getString(R.string.downloading_data), true);
        palletCartonInfo = new PalletCartonInfoHelper();
        palletCartonInfo.setDeliveryID(dnInfo.getDeliveryID());
        new GetPalletCartonInfo().execute(0);
    }

    public class ChkDNInfo extends AsyncTask<Integer, String, ChkDeliveryInfoHelper> {
        @Override
        protected ChkDeliveryInfoHelper doInBackground(Integer... params) {
            AppController.debug("Get DeliveryInfo from " + AppController.getServerInfo()
                    + AppController.getProperties("ChkDNInfo"));
            publishProgress(getString(R.string.downloading_data));
            return verify.getDNInfo();
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

                if (dnInfo.getIntRetCode() == ReturnCode.OK) {
                    setListData();
                    errorInfo = "";
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else if (result.getIntRetCode() == ReturnCode.SYSTEM_INFO) {
                        setListData();
                        mConnection.setText(R.string.sever_return_info);
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = dnInfo.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    mConnection.performClick();
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class GetPalletCartonInfo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Get PalletCartonInfo from " + AppController.getServerInfo()
                    + AppController.getProperties("GetPalletCartonInfo"));
            // publishProgress("資料下載中...");
            return verify.getPalletCartonInfo(palletCartonInfo);
        }

        /*
         * @Override protected void onProgressUpdate(String... text) {
         *
         * AppController.debug("onProgressUpdate() " + text[0]);
         *
         * mConnection.setText(text[0]); mConnection.setTextColor(Color.WHITE);
         * mConnection.setBackgroundColor(Color.rgb(164, 199, 57)); // Things to
         * be done while execution of long running operation is in // progress.
         * For example updating ProgessDialog }
         */

        @Override
        protected void onPostExecute(BasicHelper result) {
            // execution of result of Long time consuming operation //
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    // errorInfo = "";
                    // mConnection.setText("");
                    // mConnection.setTextColor(Color.WHITE);
                    // mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    palletCartonInfo = (PalletCartonInfoHelper) result;
                    setPalletCartonInfo();

                    if (lCheckingLayout.getVisibility() == View.GONE || lCheckingLayout.getVisibility() == View.INVISIBLE) {
                        setCheckingLayout();
                    } else {
                        if (palletCartonInfo.getPalletNonCheckQty() == 0 && palletCartonInfo.getCartonNonCheckQty() == 0)
                            setRemarkLayout(false, true);
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
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class SetDNShipWay extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Send DNShipWay to " + AppController.getServerInfo()
                    + AppController.getProperties("SetDNShipWay"));
            publishProgress(getString(R.string.data_uploading));
            return verify.setDNShipWay(dnShipWay);
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
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText(getString(R.string.data_updated_successfully));
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    dnShipWay = (DnShipWayHelper) result;
                    doGetPalletCartonInfo();
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

    private class ChkSNItemInfo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Send SNItemInfo to " + AppController.getServerInfo()
                    + AppController.getProperties("ChkSNItemInfo"));
            publishProgress(getString(R.string.data_chking));
            return verify.chkSNItemInfo(snItem);
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
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText(R.string.data_chk_success);
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    doGetPalletCartonInfo();
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else if (result.getIntRetCode() == ReturnCode.SYSTEM_INFO) {
                        txtChecking.setText("");
                        txtImportSn.setText("");
                        txtImportCn.setText("");
                        txtSnQrCode.setText("");
                        txtChecking.requestFocus();
                        mConnection.setText(R.string.sever_return_info);
                        doGetPalletCartonInfo();
                    } else if (result.getIntRetCode() == ReturnCode.SYSTEM_OTHER) {
                        txtChecking.setText("");
                        txtImportSn.setText("");
                        txtImportCn.setText("");
                        txtSnQrCode.setText("");
                        txtChecking.requestFocus();
                        mConnection.setText(getString(R.string.db_return_error));
                        resetPalletCartonInfo();
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

    private class PrintShipDoc extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Send PrintShipDoc to " + AppController.getServerInfo()
                    + AppController.getProperties("PrintShipDoc"));
            publishProgress(getString(R.string.data_printing));
            return verify.printShipDoc(shipDoc);
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
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));
            dialog.dismiss();

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText(getString(R.string.data_printed_successfully));
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    returnPage();
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

    private class SaveShipRemark extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Send SaveShipRemark to " + AppController.getServerInfo()
                    + AppController.getProperties("PrintShipDoc"));
            publishProgress("儲存備註中...");
            return verify.printShipDoc(shipDoc);
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
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));
            dialog.dismiss();
            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText(getString(R.string.saved_successfully));
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    returnPage();
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
}
