package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.InputFilter;
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
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.R;
import com.senao.warehouse.handler.LotcodePermissionHelper;
import com.senao.warehouse.print.BtPrintLabel;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.ChkDeliveryInfoHelper;
import com.senao.warehouse.database.DcNoItemInfoHelper;
import com.senao.warehouse.database.ITEM_LABEL_QR_CODE_FORMAT;
import com.senao.warehouse.database.ItemInfoHelper;
import com.senao.warehouse.database.PackingQtyInfoHelper;
import com.senao.warehouse.database.ShipmentPalletInfoHelper;
import com.senao.warehouse.handler.ShipmentCreatePalletHandler;
import com.senao.warehouse.handler.ShippingVerifyMainHandler;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.QrCodeUtil;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;

import java.util.Locale;

public class ShipmentCreatePalletActivity extends Activity {

    private static final String TAG = ShipmentCreatePalletActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PRINTER_SETTING = 2;
    private static final int REQUEST_BLUETOOTH_SETTINGS = 3;
    private final String CUSTOMER_EXTREME = "EXTREME";
    private final String CUSTOMER_VERKADA = "VERKADA";
    private TextView mConnection, mOrderStatus, mRev, mOrigin, mPalletNo, mPartNo,
            mImprotPartNo, mImportCN, mImportQty, mImprotFracWeight,mLotcode,
            mShipmentQuantity, mPalletNo2, mPackQty,lblTitle;
    private EditText txtRev, txtOrigin, txtPalletNo, txtImportPartNo,mLotcode_txt,
            txtImportCN, txtImportQty, txtImportFracWeight, txtImportQrCode, txtImportReelId, txtImportPartNoForLotCtrl, txtImportDC;
    private Button btnReturn, btnConfim, btnCancel;
    private boolean isUpdatePalletUnmber;
    private ItemInfoHelper item;
    // private ItemQtyInfoHelper itemQty;
    private DcNoItemInfoHelper dcItem = new DcNoItemInfoHelper();
    private DcNoItemInfoHelper tempDCNOItem = new DcNoItemInfoHelper();
    private PackingQtyInfoHelper packingQtyInfo;
    private ShipmentCreatePalletHandler shipment;
    private ProgressDialog dialog;
    private String errorInfo = "";
    private int intPackQty;
    private ShipmentPalletInfoHelper printInfo;
    private ChkDeliveryInfoHelper dnInfo;
    private ShippingVerifyMainHandler verifyHandler;
    private RadioGroup rgDC;
    private RadioButton rbQrCode, rbReelID, rbPartNo;
    private LinearLayout lotCtrlLayout, qrcodeLayout, pnLayout, reelIdLayout;
    private LotcodePermissionHelper permissionHelper;

    private RadioGroup.OnCheckedChangeListener dcListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio_dc_qrcode:
                    if (rbQrCode.isChecked()) {
                        pnLayout.setVisibility(View.GONE);
                        reelIdLayout.setVisibility(View.GONE);
                        qrcodeLayout.setVisibility(View.VISIBLE);
                        mImportQty.setVisibility(View.GONE);
                        txtImportQty.setVisibility(View.GONE);
                        txtImportQrCode.requestFocus();
                    }

                    break;

                case R.id.radio_reel_id:
                    if (rbReelID.isChecked()) {
                        pnLayout.setVisibility(View.GONE);
                        reelIdLayout.setVisibility(View.VISIBLE);
                        qrcodeLayout.setVisibility(View.GONE);
                        mImportQty.setVisibility(View.VISIBLE);
                        txtImportQty.setVisibility(View.VISIBLE);
                        txtImportReelId.requestFocus();
                    }

                    break;

                case R.id.radio_part_number:
                    if (rbPartNo.isChecked()) {
                        pnLayout.setVisibility(View.VISIBLE);
                        reelIdLayout.setVisibility(View.GONE);
                        qrcodeLayout.setVisibility(View.GONE);
                        mImportQty.setVisibility(View.VISIBLE);
                        txtImportQty.setVisibility(View.VISIBLE);
                        txtImportPartNoForLotCtrl.requestFocus();
                    }

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_picking_create_pallet);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            item = new Gson().fromJson(extras.getString("ITEM_INFO"), ItemInfoHelper.class);

            if (item == null || AppController.getDnInfo() == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_single_box), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            dcItem.setDeliveryID(AppController.getDnInfo().getDeliveryID());
            dcItem.setItemID(item.getId());
            shipment = new ShipmentCreatePalletHandler(item);
            intPackQty = extras.getInt("PACK_QTY");
            isUpdatePalletUnmber = true;
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_single_box), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ShipmentCreatePalletActivity.this);
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

        mOrderStatus = findViewById(R.id.textview_order_status);
        mPartNo = findViewById(R.id.label_part_no);
        mShipmentQuantity = findViewById(R.id.label_checking_quantity);
        mRev = findViewById(R.id.label_rev);
        mOrigin = findViewById(R.id.label_origin);
        mPalletNo = findViewById(R.id.label_pallet_num);
        mLotcode  = findViewById(R.id.lotcode_txt);
        mLotcode_txt  = findViewById(R.id.lotcode_edt);

        rgDC = findViewById(R.id.radio_group_dc);
        rbQrCode = findViewById(R.id.radio_dc_qrcode);
        rbReelID = findViewById(R.id.radio_reel_id);
        rbPartNo = findViewById(R.id.radio_part_number);
        rgDC.setOnCheckedChangeListener(dcListener);

        lotCtrlLayout = findViewById(R.id.ll_dc);

        qrcodeLayout = findViewById(R.id.qrcodeLayout);
        txtImportQrCode = findViewById(R.id.edittext_import_dc_qrcode);
        txtImportQrCode.setSelectAllOnFocus(true);

        txtImportQrCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down " + v.getId());
                    EditText et = (EditText) v;

                    if (TextUtils.isEmpty(et.getText())) {
                        return false;
                    } else {
                        txtImportCN.requestFocus();
                        return true;
                    }
                }

                return false;
            }
        });

        reelIdLayout = findViewById(R.id.reelIdLayout);
        txtImportReelId = findViewById(R.id.edittext_reelid);
        txtImportReelId.setFilters(new InputFilter[]{new InputFilter.LengthFilter(34)});
        txtImportReelId.setSelectAllOnFocus(true);

        pnLayout = findViewById(R.id.pnLayout);
        txtImportPartNoForLotCtrl = findViewById(R.id.edittext_pn);
        // 限制長度為12
        txtImportPartNoForLotCtrl.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        txtImportPartNoForLotCtrl.setSelectAllOnFocus(true);
        txtImportDC = findViewById(R.id.edittext_dc);
//        if (getCustomer().equals(CUSTOMER_EXTREME)) {
//            txtImportDC.setInputType(InputType.TYPE_CLASS_TEXT);
//        }
        txtImportDC.setSelectAllOnFocus(true);

        mImprotPartNo = findViewById(R.id.textview_part_no);
        mImportCN = findViewById(R.id.textview_import_cn);
        mImportQty = findViewById(R.id.textview_import_qty);
        mImprotFracWeight = findViewById(R.id.textview_import_frac_weight);
        mPalletNo2 = findViewById(R.id.label_pallet_number);
        mPackQty = findViewById(R.id.label_pack_qty);

        txtOrigin = findViewById(R.id.edittext_origin);
        txtOrigin.setSelectAllOnFocus(true);
        txtOrigin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                final boolean isEnterEvent = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
                final boolean isEnterUpEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_UP;
                final boolean isEnterDownEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_DOWN;

                // Capture this event to receive ACTION_UP
                // We do not care on other actions
                if (actionId == EditorInfo.IME_ACTION_NEXT || isEnterUpEvent) {
                    // Do your action here
                    EditText et = (EditText) textView;

                    if (et.getText().toString().trim().equals("")) {
                        Toast.makeText(ShipmentCreatePalletActivity.this, getString(R.string.location_is_not_null), Toast.LENGTH_SHORT).show();
                        return true;
                    } else {
                        if (getCountryCode(et.getText().toString()) == null) {
                            et.setSelectAllOnFocus(true);
                            Toast.makeText(ShipmentCreatePalletActivity.this, getString(R.string.origin_has_problem), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    }

                    return false;
                } else return isEnterDownEvent;
            }
        });

        txtRev = findViewById(R.id.edittext_rev);
        txtRev.setSelectAllOnFocus(true);

        txtRev.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                final boolean isEnterEvent = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
                final boolean isEnterUpEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_UP;
                final boolean isEnterDownEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_DOWN;

                // Capture this event to receive ACTION_UP
                // We do not care on other actions
                if (actionId == EditorInfo.IME_ACTION_NEXT || isEnterUpEvent) {
                    // Do your action here
                    EditText et = (EditText) textView;

                    if (et.getText().toString().trim().equals("")) {
                        Toast.makeText(ShipmentCreatePalletActivity.this, getString(R.string.version_is_not_null), Toast.LENGTH_SHORT).show();
                        return true;
                    } else {
                        String rev = et.getText().toString().trim();
                        String[] values = rev.split("-");
                        rev = values[values.length - 1];

                        if (!rev.matches(".*[A-Z]+.*")) {
                            et.setSelectAllOnFocus(true);
                            Toast.makeText(ShipmentCreatePalletActivity.this, getString(R.string.format_error_atleast_1_Eng_word), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    }

                    return false;
                } else return isEnterDownEvent;
            }
        });

        txtPalletNo = findViewById(R.id.edittext_pallet_no);
        txtPalletNo.setSelectAllOnFocus(true);
        txtImportPartNo = findViewById(R.id.edittext_part_no);
        txtImportPartNo.setSelectAllOnFocus(true);
        txtImportCN = findViewById(R.id.edittext_import_cn);
        txtImportCN.setSelectAllOnFocus(true);
        txtImportQty = findViewById(R.id.edittext_import_qty);
        txtImportQty.setSelectAllOnFocus(true);
        txtImportFracWeight = findViewById(R.id.edittext_import_frac_weight);
        txtImportFracWeight.setSelectAllOnFocus(true);

        btnReturn = findViewById(R.id.button_return);
        btnConfim = findViewById(R.id.button_confirm);
        btnCancel = findViewById(R.id.button_cancel);

        mOrderStatus.setText(AppController.getDnInfo().getOpStatus());

        if (AppController.getDnInfo().getOpStatus().toUpperCase().equals("HOLD")) {
            mOrderStatus.setTextColor(Color.RED);
        } else {
            mOrderStatus.setTextColor(Color.BLUE);
        }

        mPartNo.setText(getString(R.string.label_part_no2, item.getItemID()));
        mShipmentQuantity.setText(getString(R.string.label_picking_qty, item.getQty(), item.getPass(), item.getWait()));
        mPackQty.setText(getString(R.string.label_pack_qty3, intPackQty));
        txtPalletNo.setText(dcItem.getPalletNo());

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

                if (isUpdatePalletUnmber) {
                    if (checkPalletNumber()) {
                        doQueryDeliveryInfo();
                    }
                } else {
                    if (checkInput()) {
                        tempDCNOItem = new DcNoItemInfoHelper();
                        tempDCNOItem.setDeliveryID(AppController.getDnInfo().getDeliveryID());
                        tempDCNOItem.setItemID(item.getId());
                        tempDCNOItem.setPalletNo(txtPalletNo.getText().toString().trim());
                        tempDCNOItem.setCartonNo(txtImportCN.getText().toString().trim());
                        tempDCNOItem.setShipQty(Integer.parseInt(getQty()));

                        if(mLotcode_txt.getVisibility() == View.VISIBLE) {
                            tempDCNOItem.setLotcode(mLotcode_txt.getText().toString().trim());
                        }

                        if (txtRev.getVisibility() == View.VISIBLE) {
                            String rev = txtRev.getText().toString().trim();
                            String[] values = rev.split("-");
                            rev = values[values.length - 1];
                            tempDCNOItem.setRev(rev);
                        }

                        if (txtOrigin.getVisibility() == View.VISIBLE) {
 /*                           String origin = txtOrigin.getText().toString().trim();
                            origin = origin.toUpperCase();
                            if (origin.contains("CHINA") || origin.contains("CN")) {
                                origin = "CN";
                            } else if (origin.contains("TAIWAN") || origin.contains("TW")) {
                                origin = "TW";
                            } else {
                                String[] values = txtOrigin.getText().toString().trim().split(" ");
                                origin = values[values.length - 1];
                                if (origin.length() > 1) {
                                    origin = origin.substring(0, 2);
                                }
                            }*/
                            tempDCNOItem.setOrigin(getCountryCode(txtOrigin.getText().toString()));
                        }

                        if (!txtImportFracWeight.getText().toString().trim().equals("")) {
                            tempDCNOItem.setBoxWeight(Double.parseDouble(txtImportFracWeight.getText().toString().trim()));
                        }

                        if (item.getControl().toUpperCase().equals("DC")) {
                            if (pnLayout.getVisibility() == View.VISIBLE) {
                                tempDCNOItem.setNo(getReelId());
                            } else {
                                tempDCNOItem.setNo(getReelId().substring(0, 28));
                            }
                        } else {
                            tempDCNOItem.setNo(txtImportPartNo.getText().toString().trim());
                        }

                        dialog = ProgressDialog.show(ShipmentCreatePalletActivity.this, getString(R.string.holdon),
                                getString(R.string.data_uploading), true);
                        new SetDCItemInfo().execute(0);
                    }
                }
            }

        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Cancel", "ShipmentCreatePalletActivity "+String.valueOf(v.getId()));

                if (isUpdatePalletUnmber) {
                    txtPalletNo.setText("");
                    txtPalletNo.requestFocus();
                } else {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    cleanData();
                    txtOrigin.setText("");
                    txtRev.setText("");
                    mLotcode_txt.setText("");

                    if (txtOrigin.getVisibility() == View.VISIBLE) {
                        txtOrigin.requestFocus();
                    }
                }
            }

        });

        mPalletNo2.setVisibility(View.INVISIBLE);
        AppController.debug("ShipmentCreatePalletActivity>>>getCustomer():" +getCustomer());

        if (getCustomer().equals(CUSTOMER_VERKADA)) {
            AppController.debug("ShipmentCreatePalletActivity>>>>CUSTOMER_VERKADA");
            permissionHelper = new LotcodePermissionHelper();
            permissionHelper.setItemId(item.getId());

            new GetIsLotcode().execute(0);
        }

        mOrigin.setVisibility(View.GONE);
        mRev.setVisibility(View.GONE);
        rgDC.setVisibility(View.GONE);
        lotCtrlLayout.setVisibility(View.GONE);
        mImprotPartNo.setVisibility(View.GONE);
        mImportCN.setVisibility(View.GONE);
        mImportQty.setVisibility(View.GONE);
        mImprotFracWeight.setVisibility(View.GONE);

        mLotcode.setVisibility(View.GONE);
        mLotcode_txt.setVisibility(View.GONE);

        txtOrigin.setVisibility(View.GONE);
        txtRev.setVisibility(View.GONE);
        txtImportPartNo.setVisibility(View.GONE);
        txtImportCN.setVisibility(View.GONE);
        txtImportQty.setVisibility(View.GONE);
        txtImportFracWeight.setVisibility(View.GONE);

        mPalletNo.setVisibility(View.VISIBLE);
        txtPalletNo.setVisibility(View.VISIBLE);
        lblTitle = findViewById(R.id.ap_title);

        setTitle();
        checkPrinterSetting();
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.label_picking, AppController.getOrgName()));
        text.setSpan(new RelativeSizeSpan(1f), 0, AppController.getOrgName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(Color.parseColor("#fa160a")), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private String getReelId() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return getPartNo() + getVendorCode() + getDateCode();
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            return txtImportReelId.getText().toString().trim();
        } else {
            return QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID);
        }
    }

    private String getPartNo() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportPartNoForLotCtrl.getText().toString().trim();
        } else {
            return getReelId().substring(0, 12);
        }
    }

    private String getDateCode() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportDC.getText().toString().trim();
        } else {
            return getReelId().substring(18, 28);
        }
    }

    private String getQty() {
        if (item.getControl().toUpperCase().equals("DC") && qrcodeLayout.getVisibility() == View.VISIBLE) {
            return QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.QTY);
        } else {
            return txtImportQty.getText().toString().trim();
        }
    }

    private String getVendorCode() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            if (getCustomer().equals(CUSTOMER_EXTREME) && txtImportDC.getText().length() != 10) {
                return "000000";
            } else {
                return getDateCode().substring(4);
            }
        } else {
            return getReelId().substring(12, 18);
        }
    }

    private String getCustomer() {
        return AppController.getDnInfo().getCustomer().split(" ")[0].toUpperCase();
    }

    private void getInvoiceNo(int dn) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.downloading_data), true);
        dnInfo = new ChkDeliveryInfoHelper();
        dnInfo.setDeliveryID(dn);
        verifyHandler = new ShippingVerifyMainHandler(dnInfo);
        new ChkDNInfo().execute(0);
    }

    private void doQueryDeliveryInfo(int deliveryId, String palletNo) {
        printInfo = new ShipmentPalletInfoHelper();
        printInfo.setDnNo(deliveryId);
        printInfo.setPalletNo(palletNo);
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetPrintInfo().execute(0);
    }

    private void printLabel() {
        dialog = ProgressDialog.show(ShipmentCreatePalletActivity.this, getString(R.string.holdon),
                getString(R.string.printingLabel), true);

        errorInfo = "";
        String qrCode = dnInfo.getInvoiceNo() + "@" + printInfo.getDnNo() + "@" + printInfo.getPalletNo() + "@" + printInfo.getBoxQty();

        if(Constant.ISORG){
            qrCode = dnInfo.getInvoiceNo() + "@" + printInfo.getDnNo() + "@" + printInfo.getPalletNo() + "@" + printInfo.getBoxQty() + "@" + AppController.getOrg();
        }

        AppController.debug("qrCode:"+qrCode);

//        if (!BtPrintLabel.printShipmentPalletLabel(printInfo.getMark(),printInfo.getShippingWay(), printInfo.getDnNo(),printInfo.getPalletNo(), printInfo.getBoxQty(), qrCode)) {
        if (!BtPrintLabel.printShipmentPalletLabel2(printInfo.getMark(),printInfo.getShippingWay(), printInfo.getDnNo(),printInfo.getPalletNo(), printInfo.getBoxQty(), qrCode,AppController.getOrgName())) {
            errorInfo = getString(R.string.printLabalFailed)+" ShipmentCreatePalletActivity";
            mConnection.setText(getString(R.string.printer_connect_error));
            mConnection.setTextColor(Color.RED);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        dialog.dismiss();
    }

    private boolean checkPalletNumber() {
        if (txtPalletNo.getText().toString().trim().equals("")) {
            Toast.makeText(ShipmentCreatePalletActivity.this, getString(R.string.pallet_nu_uncorrect), Toast.LENGTH_SHORT).show();
            txtPalletNo.requestFocus();
            return false;
        }

        return true;
    }

    private boolean checkInput() {
        if(mLotcode_txt.getVisibility() == View.VISIBLE) {
            if (TextUtils.isEmpty(mLotcode_txt.getText())) {
                mLotcode_txt.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.lotcode), Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (item.getControl().toUpperCase().equals("DC")) {
            if (pnLayout.getVisibility() == View.VISIBLE) {
                txtImportPartNoForLotCtrl.setText(txtImportPartNoForLotCtrl.getText().toString().trim());
                txtImportDC.setText(txtImportDC.getText().toString().trim());

                if (TextUtils.isEmpty(txtImportPartNoForLotCtrl.getText())) {
                    txtImportPartNoForLotCtrl.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_sku), Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (txtImportPartNoForLotCtrl.getText().length() != 12) {
                    txtImportPartNoForLotCtrl.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.sku_len_must_be_12), Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (TextUtils.isEmpty(txtImportDC.getText())) {
                    txtImportDC.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_dc), Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (!getCustomer().equals(CUSTOMER_EXTREME) && txtImportDC.getText().length() != 10) {
                    txtImportDC.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.dc_len_greater_10), Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
                txtImportReelId.setText(txtImportReelId.getText().toString().trim());

                if (txtImportReelId.getText().length() != 28 && txtImportReelId.getText().length() != 34) {
                    txtImportReelId.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.reelid_len_must_be_28_24), Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else {
                txtImportQrCode.setText(txtImportQrCode.getText().toString().trim());

                if (TextUtils.isEmpty(txtImportQrCode.getText())) {
                    txtImportQrCode.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_qrcode), Toast.LENGTH_SHORT).show();
                    return false;
                }

                String reelId = getReelId();
                if (reelId.length() != 34) {
                    txtImportQrCode.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.reelid_incorrect_len_must_be_34), Toast.LENGTH_SHORT).show();
                    return false;
                }

                String qty = getQty();
                if (TextUtils.isEmpty(qty)) {
                    txtImportQrCode.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.qr_qty_is_not_null), Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (Integer.valueOf(qty) < 1) {
                    txtImportQrCode.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.num_has_greater_0), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        } else {
            if (txtImportPartNo.getText().toString().trim().equals("")) {
                Toast.makeText(ShipmentCreatePalletActivity.this, getString(R.string.enter_sku), Toast.LENGTH_SHORT).show();
                txtImportPartNo.requestFocus();
                return false;
            }
        }

        if (txtRev.getVisibility() == View.VISIBLE) {
            if (txtRev.getText().toString().trim().equals("")) {
                Toast.makeText(ShipmentCreatePalletActivity.this, getString(R.string.enter_version), Toast.LENGTH_SHORT).show();
                txtRev.requestFocus();
                return false;
            }

            String rev = txtRev.getText().toString().trim();
            String[] values = rev.split("-");
            rev = values[values.length - 1];

            if (!rev.matches(".*[A-Z]+.*")) {
                Toast.makeText(ShipmentCreatePalletActivity.this, getString(R.string.format_error_atleast_1_Eng_word), Toast.LENGTH_SHORT).show();
                txtRev.requestFocus();
                txtRev.setSelectAllOnFocus(true);
                return false;
            }
        }

        if (txtOrigin.getVisibility() == View.VISIBLE) {
            if (txtOrigin.getText().toString().trim().equals("")) {
                Toast.makeText(ShipmentCreatePalletActivity.this, getString(R.string.enter_origin), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (getCountryCode(txtOrigin.getText().toString()) == null) {
                Toast.makeText(ShipmentCreatePalletActivity.this, getString(R.string.origin_has_problem), Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (txtImportCN.getText().toString().trim().equals("")) {
            Toast.makeText(ShipmentCreatePalletActivity.this, getString(R.string.enter_box_no), Toast.LENGTH_SHORT).show();
            txtImportCN.requestFocus();
            return false;
        }

        if (getQty().equals("")) {
            Toast.makeText(ShipmentCreatePalletActivity.this, getString(R.string.enter_qty), Toast.LENGTH_SHORT).show();

            if (txtImportQty.getVisibility() == View.VISIBLE) {
                txtImportQty.requestFocus();
            } else {
                txtImportQrCode.requestFocus();
            }

            return false;
        } else {
            int intImportQty = Integer.parseInt(getQty());

            if (intImportQty + item.getPass() > item.getQty()) {
                Toast.makeText(ShipmentCreatePalletActivity.this, getString(R.string.exceeded_qty_to_be_picked), Toast.LENGTH_SHORT).show();

                if (txtImportQty.getVisibility() == View.VISIBLE) {
                    txtImportQty.requestFocus();
                } else {
                    txtImportQrCode.requestFocus();
                }

                return false;
            }

            if (intImportQty > intPackQty) {
                Toast.makeText(ShipmentCreatePalletActivity.this, getString(R.string.qty_cant_more_than_box_num), Toast.LENGTH_SHORT).show();

                if (txtImportQty.getVisibility() == View.VISIBLE) {
                    txtImportQty.requestFocus();
                } else {
                    txtImportQrCode.requestFocus();
                }

                return false;
            }

            if (intImportQty < intPackQty && txtImportFracWeight.getText().toString().trim().equals("")) {
                Toast.makeText(ShipmentCreatePalletActivity.this, getString(R.string.enter_zero_carton_weight), Toast.LENGTH_SHORT).show();
                txtImportFracWeight.requestFocus();
                return false;
            }
        }

        return true;
    }

    @Nullable
    private String getCountryCode(String origin) {
        origin = origin.trim().toUpperCase();
        String[] locales = Locale.getISOCountries();
        for (String countryCode : locales) {
            Locale locale = new Locale("", countryCode);
            String iso = locale.getISO3Country();
            String code = locale.getCountry();
            String chName = locale.getDisplayCountry(Locale.TAIWAN);
            String enName = locale.getDisplayCountry(Locale.US);

            if (code.toUpperCase().equals(origin)
                    || iso.toUpperCase().equals(origin)
                    || enName.toUpperCase().contains(origin)
                    || chName.contains(origin)) {
                return code;
            }
        }

        String[] origins = getResources().getStringArray(R.array.origin_of_china);
        for (String item : origins) {
            if (item.toUpperCase().equals(origin)) {
                return "CN";
            }
        }

        origins = getResources().getStringArray(R.array.origin_of_tw);
        for (String item : origins) {
            if (item.toUpperCase().equals(origin)) {
                return "TW";
            }
        }

        return null;
    }

    private void returnPage() {
        finish();
    }

    public void onBackPressed() {
        // do something here and don't write super.onBackPressed()
        Log.d(TAG, "onClick Hardware Back Button");
        returnPage();
    }

    private void cleanData() {
        // txtPalletNo.setText(tempDCNOItem.getPalletNo());
        txtImportQrCode.setText("");
        txtImportReelId.setText("");
        txtImportPartNoForLotCtrl.setText("");
        txtImportDC.setText("");
        txtImportPartNo.setText("");
        txtImportCN.setText("");
        txtImportQty.setText("");
        txtImportFracWeight.setText("");

        if (item.getControl().toUpperCase().equals("DC")) {
            if (rbQrCode.isChecked()) {
                txtImportQrCode.requestFocus();
            } else if (rbReelID.isChecked()) {
                txtImportReelId.requestFocus();
            } else {
                txtImportPartNoForLotCtrl.requestFocus();
            }
        } else {
            txtImportPartNo.requestFocus();
        }

        tempDCNOItem = new DcNoItemInfoHelper();
    }

    private void showDetail() {
        mPalletNo2.setText(getString(R.string.label_pallet_number4, txtPalletNo.getText().toString().trim()));
        mPalletNo2.setVisibility(View.VISIBLE);

        if (item.getControl().toUpperCase().equals("DC")) {
            rgDC.setVisibility(View.VISIBLE);
            lotCtrlLayout.setVisibility(View.VISIBLE);
            rbReelID.setChecked(true);
            mImprotPartNo.setVisibility(View.GONE);
            txtImportPartNo.setVisibility(View.GONE);
        } else {
            rgDC.setVisibility(View.GONE);
            lotCtrlLayout.setVisibility(View.GONE);
            mImprotPartNo.setVisibility(View.VISIBLE);
            txtImportPartNo.setVisibility(View.VISIBLE);
            txtImportPartNo.requestFocus();
        }

        if (getCustomer().equals(CUSTOMER_EXTREME)) {
            mRev.setVisibility(View.VISIBLE);
            txtRev.setVisibility(View.VISIBLE);
            mOrigin.setVisibility(View.VISIBLE);
            txtOrigin.setVisibility(View.VISIBLE);
            txtOrigin.requestFocus();
        }

        mImportCN.setVisibility(View.VISIBLE);

        if (item.getControl().toUpperCase().equals("DC") && rbQrCode.isChecked()) {
            mImportQty.setVisibility(View.GONE);
            txtImportQty.setVisibility(View.GONE);
        } else {
            mImportQty.setVisibility(View.VISIBLE);
            txtImportQty.setVisibility(View.VISIBLE);
        }

        mImprotFracWeight.setVisibility(View.VISIBLE);
        txtImportCN.setVisibility(View.VISIBLE);
        txtImportFracWeight.setVisibility(View.VISIBLE);

        mPalletNo.setVisibility(View.GONE);
        txtPalletNo.setVisibility(View.GONE);
    }

    private void updateItemInfo() {
        item.setQty(packingQtyInfo.getQty());
        item.setPass(packingQtyInfo.getPickQty());
        item.setWait(packingQtyInfo.getNonPickQty());

        mShipmentQuantity.setText(getString(R.string.label_picking_qty, item.getQty(), item.getPass(), item.getWait()));

        if (item.getPass() == item.getQty()) {
            setResult(RESULT_OK);
            finish();
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

    private void doQueryDeliveryInfo() {
        tempDCNOItem = new DcNoItemInfoHelper();
        tempDCNOItem.setDeliveryID(AppController.getDnInfo().getDeliveryID());
        tempDCNOItem.setPalletNo(txtPalletNo.getText().toString().trim());

        doQueryDeliveryInfo(AppController.getDnInfo().getDeliveryID(), txtPalletNo.getText().toString().trim());
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
                    isUpdatePalletUnmber = false;
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

    private class SetDCItemInfo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            publishProgress(getString(R.string.data_uploading));

            if (item.getControl().toUpperCase().equals("DC")) {
                AppController.debug("Send DCItemInfo to " + AppController.getServerInfo()
                        + AppController.getProperties("SetDCItem"));

                return shipment.setDCItemInfo(tempDCNOItem);
            } else {
                AppController.debug("Send DCItemInfo to " + AppController.getServerInfo()
                        + AppController.getProperties("SetNOItem"));

                return shipment.setNOItemInfo(tempDCNOItem);
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
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText(R.string.data_updated_successfully);
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    dialog.dismiss();
                    cleanData();
                    setResult(RESULT_FIRST_USER);
                    dialog = ProgressDialog.show(ShipmentCreatePalletActivity.this, getString(R.string.holdon),
                            getString(R.string.data_updating), true);

                    new GetPackingQtyInfo().execute(0);
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

    private class GetPackingQtyInfo extends AsyncTask<Integer, String, PackingQtyInfoHelper> {
        @Override
        protected PackingQtyInfoHelper doInBackground(Integer... params) {
            AppController.debug("Query PackingQtyInfo from " + AppController.getServerInfo()
                    + AppController.getProperties("GetPackingQtyInfo"));
            publishProgress(getString(R.string.data_updating));
            packingQtyInfo = new PackingQtyInfoHelper();
            packingQtyInfo.setDeliveryID(AppController.getDnInfo().getDeliveryID());
            packingQtyInfo.setItemID(item.getId());
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
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    packingQtyInfo = result;
                    mConnection.setText(getString(R.string.data_updated_successfully));
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    dialog.dismiss();
                    errorInfo = "";
                    updateItemInfo();
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
                if (result.getPermission()==1) {
                    mLotcode.setVisibility(View.VISIBLE);
                    mLotcode_txt.setVisibility(View.VISIBLE);
                } else if(result.getPermission()==0) {
                    mLotcode.setVisibility(View.GONE);
                    mLotcode_txt.setVisibility(View.GONE);
                } else {
                    mConnection.setText(getString(R.string.label_unable_determine_lotcode_permissionC) + ":" + result.getPermission());
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.RED);
                }
            } else {
                mConnection.setText(getString(R.string.label_unable_determine_lotcode_permission));
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
                showDetail();
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }
}
