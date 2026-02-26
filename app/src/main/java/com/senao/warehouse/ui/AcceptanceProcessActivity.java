package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.inputmethod.InputMethodManager;
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
import com.senao.warehouse.database.MaterialLabelHelper;
import com.senao.warehouse.database.SubinventoryInfoHelper;
import com.senao.warehouse.handler.OrgHandler;
import com.senao.warehouse.handler.OrgHelper;
import com.senao.warehouse.handler.StockInHandler;
import com.senao.warehouse.print.BtPrintLabel;
import com.senao.warehouse.database.AcceptanceConditionHelper;
import com.senao.warehouse.database.AcceptanceInfoHelper;
import com.senao.warehouse.database.AcceptanceProcessInfoHelper;
import com.senao.warehouse.database.AcceptanceSerialNoHelper;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.ITEM_LABEL_QR_CODE_FORMAT;
import com.senao.warehouse.handler.AcceptanceHandler;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.QrCodeUtil;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.util.Util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//驗收入庫作業
public class AcceptanceProcessActivity extends Activity {
    private static final String TAG = AcceptanceProcessActivity.class.getSimpleName();
    private static String[] NOT_ALLOWED_SUBS = {"304", "320", "Stage", "310", "318", "322", "326", "327"};
    private List<String> LIST_NOT_ALLOWED_SUBS = new ArrayList<>();
    private final String PREFIX_ITEM_NO_PCB = "7016";
    private TextView mConnection;
    private Button btnReturn, btnConfim, btnCancel, btnYes, btnNo;
    private AcceptanceInfoHelper item;
    private AcceptanceInfoHelper tempItem;
    private AcceptanceConditionHelper conditionHelper;
    private AcceptanceConditionHelper tempConditionHelper;
    private ProgressDialog dialog;
    private String errorInfo = "";
    private TextView txtPartNo, txtStockQty, txtXbQty, txtPreStockQty, lblReceiptNo, txtTitle,
            lblXboardQtyDcQrCode, lblXboardQty, lblXboardQtyDcPn;
    private LinearLayout lPalletLayout, lNoLayout, lQrCodeLayout, lReelIDLayout, lQuestionLayout,
            lReceiptNoLayout, lPNLayout, lBoxLayout, lSNLayout, lInfo;
    private RadioGroup rgDC, rgSN;
    private RadioButton rbQrCode, rbReelID, rbPartNo, rbPalletSN, rbBoxSN, rbPalletCN,
            rbBoxCN, rbPallet, rbBox, rbSN, rbReceiptNo, rbTrxId;
    private EditText txtReelID, txtPalletCN, txtBoxCN,
            txtSNStart, txtSNEnd, txtBoxSN, txtPalletSN, txtNoPN, txtNoAddend,
            txtNoMultiplier, txtNoMultiplicand, txtReelIdMultiplicand,
            txtReelIdAddend, txtReelIdMultiplier, txtReceiptNo, txtTrxId,
            txtPnPN, txtPnDC, txtPnMultiplier, txtPnMultiplicand, txtPnAddend,
            txtPnSubinventory, //20230202 刪除
            txtReelIdSubinventory,
            txtDcQrCodeSubinventory,
            txtPnLocator,  txtReelIdLocator,
            txtDcQrCodeLocator,
            txtNoSubinventory, txtNoLocator,
            txtPalletSubinventory, txtPalletLocator,
            txtBoxSubinventory, txtBoxLocator,
            txtSnSubinventory, txtSnLocator, txtXboardQty, txtXboardQtyDcPn,
            txtDcQrCode, txtXboardQtyDcQrCode;
    private Spinner spinnerSubs,spinnerSubs01,spinnerSubs02; //20230202 新增
    private ArrayAdapter adapterSubs; //倉別 //20230202 新增
    private SubinventoryInfoHelper subinventoryHelper; //20230202 新增
    private LayoutType mode = LayoutType.NONE;
    private boolean isMerge = true;
    private AcceptanceHandler handler;
    private AcceptanceSerialNoHelper serialNoHelper;
    private AcceptanceProcessInfoHelper processInfoHelper;
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
    private List<String> palletNoList = new ArrayList<>();
    private int accType;
    private OrgHandler orgHandler;

    private View.OnKeyListener keyListener = new View.OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                //Perform action on key press
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
                /*}
                else if (id == R.id.edittext_pn_sub) {
                    txtPnSubinventory.selectAll();
                }
                else if (id == R.id.edittext_dc_qrcode_sub) {
                    txtDcQrCodeSubinventory.selectAll();
                } else if (id == R.id.edittext_reelid_sub) {
                    txtReelIdSubinventory.selectAll();*/
                } else if (id == R.id.edittext_no_sub) {
                    txtNoSubinventory.selectAll();
                } else if (id == R.id.edittext_pallet_sub) {
                    txtPalletSubinventory.selectAll();
                } else if (id == R.id.edittext_box_sub) {
                    txtBoxSubinventory.selectAll();
                } else if (id == R.id.edittext_sn_sub) {
                    txtSnSubinventory.selectAll();
                } else if (id == R.id.edittext_pn_addend) {
                    if (txtXboardQtyDcPn.getVisibility() == View.VISIBLE) {
                        txtXboardQtyDcPn.requestFocus();
                    } else {
                        hideKeyboard();
                    }
                } else if (id == R.id.edittext_reelid_addend) {
                    if (txtXboardQty.getVisibility() == View.VISIBLE) {
                        txtXboardQty.requestFocus();
                    } else {
                        hideKeyboard();
                    }
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
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener dcListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio_dc_qrcode:
                    if (rbQrCode.isChecked())
                        showLayout(LayoutType.DC_QR_CODE);
                    break;
                case R.id.radio_reel_id:
                    if (rbReelID.isChecked())
                        showLayout(LayoutType.DC_REEL_ID);
                    break;
                case R.id.radio_part_number:
                    if (rbPartNo.isChecked()) {
                        showLayout(LayoutType.DC_PN);
                    }
                    break;
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener rbListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if (isChecked) {
                if (buttonView.equals(rbReceiptNo)) {
                    rbTrxId.setChecked(false);
                    txtTrxId.setEnabled(false);
                    //txtTrxId.setText("");
                    txtReceiptNo.setEnabled(true);
                    txtReceiptNo.requestFocus();
                    txtReceiptNo.selectAll();
                } else {
                    rbReceiptNo.setChecked(false);
                    txtReceiptNo.setEnabled(false);
                    //txtReceiptNo.setText("");
                    txtTrxId.setEnabled(true);
                    txtTrxId.requestFocus();
                    txtTrxId.selectAll();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceptance_process);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            conditionHelper = new Gson().fromJson(extras.getString("CONDITION_INFO"), AcceptanceConditionHelper.class);
            if (conditionHelper == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_acceptance_stock_data), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            item = new Gson().fromJson(extras.getString("ITEM_INFO"), AcceptanceInfoHelper.class);
            if (item == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_acceptance_stock_data), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            handler = new AcceptanceHandler();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_acceptance_stock_data), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtTitle = findViewById(R.id.ap_title);

        SpannableString text;
        text = new SpannableString(getString(R.string.label_acceptance1, AppController.getOrgName()));
        text.setSpan(new RelativeSizeSpan(1f), 0, AppController.getOrgName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //text.setSpan(new ForegroundColorSpan(Color.parseColor("#fa160a")), 0, AppController.getOrgName().length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtTitle.setText(text);

        accType = getIntent().getIntExtra("TYPE", 0);
        if (accType == AcceptanceActivity.OUTSOURCING) {
            //txtTitle.setText(R.string.label_acceptance_outsourcing);
            text = new SpannableString(getString(R.string.label_acceptance_outsourcing1, AppController.getOrgName()));
            text.setSpan(new RelativeSizeSpan(1f), 0, AppController.getOrgName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //text.setSpan(new ForegroundColorSpan(Color.parseColor("#fa160a")), 0, AppController.getOrgName().length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            txtTitle.setText(text);
        }

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(AcceptanceProcessActivity.this);
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

        lInfo = findViewById(R.id.ll_info);
        txtStockQty = findViewById(R.id.textview_stock_quantity);

        txtXbQty = findViewById(R.id.textview_xb_quantity);
        if (item.getPartNo() == null || !item.getPartNo().substring(0, 4).equals(PREFIX_ITEM_NO_PCB)) {
            txtXbQty.setVisibility(View.GONE);
        }

        txtPreStockQty = findViewById(R.id.label_prestocking_quantity);
        lQuestionLayout = findViewById(R.id.questionLayout);
        lReceiptNoLayout = findViewById(R.id.receiptNoLayout);
        rbReceiptNo = findViewById(R.id.rb_receipt_no);
        rbReceiptNo.setOnCheckedChangeListener(rbListener);
        txtReceiptNo = findViewById(R.id.edittext_receipt_no);
        rbTrxId = findViewById(R.id.rb_trx_id);
        rbTrxId.setOnCheckedChangeListener(rbListener);
        txtTrxId = findViewById(R.id.edittext_trx_id);
        txtPnSubinventory = findViewById(R.id.edittext_pn_sub);
        subinventoryHelper = new SubinventoryInfoHelper();

        adapterSubs = new ArrayAdapter<>(this, R.layout.spinner_item, getResources().getStringArray(R.array.subs));
        adapterSubs.setDropDownViewResource(R.layout.spinner_item);

        spinnerSubs = findViewById(R.id.spinnerSubs);
        spinnerSubs.setAdapter(adapterSubs);
        spinnerSubs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppController.debug(parent.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(AcceptanceProcessActivity.this,"onNothingSelected"/*這行可直接取得選中內容*/,Toast.LENGTH_SHORT).show();
            }
        });

        spinnerSubs01 = findViewById(R.id.spinnerSubs01);
        spinnerSubs01.setAdapter(adapterSubs);
        spinnerSubs01.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppController.debug(parent.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(AcceptanceProcessActivity.this,"onNothingSelected"/*這行可直接取得選中內容*/,Toast.LENGTH_SHORT).show();
            }
        });

        spinnerSubs02 = findViewById(R.id.spinnerSubs02);
        spinnerSubs02.setAdapter(adapterSubs);
        spinnerSubs02.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppController.debug(parent.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(AcceptanceProcessActivity.this,"onNothingSelected"/*這行可直接取得選中內容*/,Toast.LENGTH_SHORT).show();
            }
        });

        doQuerySubInventory();

        txtPnLocator = findViewById(R.id.edittext_pn_loc);
        //txtPnSubinventory.setOnKeyListener(keyListener);
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

        lblXboardQtyDcPn = findViewById(R.id.label_import_x_board_qty_dc_pn);
        txtXboardQtyDcPn = findViewById(R.id.edittext_import_x_board_qty_dc_pn);

        txtPnPN.setOnKeyListener(keyListener);

        lQrCodeLayout = findViewById(R.id.dcQrCodeLayout);
        txtDcQrCodeSubinventory = findViewById(R.id.edittext_dc_qrcode_sub);
        txtDcQrCodeLocator = findViewById(R.id.edittext_dc_qrcode_loc);

        txtDcQrCode = findViewById(R.id.edittext_import_dc_qrcode);
        txtDcQrCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    //Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down " + v.getId());
                    EditText et = (EditText) v;

                    if (TextUtils.isEmpty(et.getText())) {
                        return false;
                    } else {
                        txtXboardQtyDcQrCode.requestFocus();
                        return true;
                    }
                }

                return false;
            }
        });

        lblXboardQtyDcQrCode = findViewById(R.id.label_import_x_board_qty_dc_qrcode);
        txtXboardQtyDcQrCode = findViewById(R.id.edittext_import_x_board_qty_dc_qrcode);

        lReelIDLayout = findViewById(R.id.reelIdLayout);
        txtReelIdSubinventory = findViewById(R.id.edittext_reelid_sub);
        txtReelIdLocator = findViewById(R.id.edittext_reelid_loc);
        //txtReelIdSubinventory.setOnKeyListener(keyListener);
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
        lblXboardQty = findViewById(R.id.label_import_x_board_qty);
        txtXboardQty = findViewById(R.id.edittext_import_x_board_qty);
        lNoLayout = findViewById(R.id.noLayout);
        txtNoSubinventory = findViewById(R.id.edittext_no_sub);
        txtNoLocator = findViewById(R.id.edittext_no_loc);
        txtNoSubinventory.setOnKeyListener(keyListener);
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
        txtPalletSubinventory = findViewById(R.id.edittext_pallet_sub);
        txtPalletLocator = findViewById(R.id.edittext_pallet_loc);
        txtPalletSubinventory.setOnKeyListener(keyListener);

        rbPalletSN = findViewById(R.id.radio_pallet_serial_no);
        rbPalletSN.setOnCheckedChangeListener(palletSnListener);
        txtPalletSN = findViewById(R.id.edittext_import_pallet_sn);
        txtPalletSN.setOnKeyListener(keyListener);

        rbPalletCN = findViewById(R.id.radio_pallet_cn);
        rbPalletCN.setOnCheckedChangeListener(palletCnListener);
        txtPalletCN = findViewById(R.id.edittext_import_pallet_cn);
        txtPalletCN.setOnKeyListener(keyListener);

        lBoxLayout = findViewById(R.id.boxLayout);
        txtBoxSubinventory = findViewById(R.id.edittext_box_sub);
        txtBoxLocator = findViewById(R.id.edittext_box_loc);
        txtBoxSubinventory.setOnKeyListener(keyListener);
        rbBoxSN = findViewById(R.id.radio_box_serial_no);
        rbBoxSN.setOnCheckedChangeListener(boxSnListener);
        txtBoxSN = findViewById(R.id.edittext_import_box_sn);
        txtBoxSN.setOnKeyListener(keyListener);

        rbBoxCN = findViewById(R.id.radio_box_cn);
        rbBoxCN.setOnCheckedChangeListener(boxCnListener);
        txtBoxCN = findViewById(R.id.edittext_import_box_cn);
        txtBoxCN.setOnKeyListener(keyListener);

        lSNLayout = findViewById(R.id.snLayout);
        txtSnSubinventory = findViewById(R.id.edittext_sn_sub);
        txtSnLocator = findViewById(R.id.edittext_sn_loc);
        txtSnSubinventory.setOnKeyListener(keyListener);
        txtSNStart = findViewById(R.id.edittext_import_sn_start);
        txtSNEnd = findViewById(R.id.edittext_import_sn_end);

        rgDC = findViewById(R.id.radio_group_dc);
        rbQrCode = findViewById(R.id.radio_dc_qrcode);
        rbReelID = findViewById(R.id.radio_reel_id);
        rbPartNo = findViewById(R.id.radio_part_number);
        rgDC.setOnCheckedChangeListener(dcListener);

        rgSN = findViewById(R.id.radio_group_sn);
        rbPallet = findViewById(R.id.radio_pallet);
        rbBox = findViewById(R.id.radio_box);
        rbSN = findViewById(R.id.radio_sn);
        rgSN.setOnCheckedChangeListener(snListener);

        txtPartNo = findViewById(R.id.label_part_no);
        txtPartNo.setText(getResources().getString(R.string.label_part_no) + " " + item.getPartNo());
        //setQuantityTitle();
        //setXbQtyTitle();
        lblReceiptNo = findViewById(R.id.textview_receipt_number);
        lblReceiptNo.setText("");

        btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Return", String.valueOf(v.getId()));
                returnPage();
            }
        });

        btnConfim = findViewById(R.id.button_confirm);
        btnConfim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Confim", String.valueOf(v.getId()));

                switch (mode) {
                    case RECEIPT_NO:
                        if (rbReceiptNo.isChecked() && txtReceiptNo.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_stock_form), Toast.LENGTH_SHORT).show();
                            txtReceiptNo.requestFocus();
                        } else if (rbTrxId.isChecked() && txtTrxId.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_stock_form), Toast.LENGTH_SHORT).show();
                            txtTrxId.requestFocus();
                        } else {
                            hideKeyboard();
                            doQueryReceiptNo();
                        }
                        break;
                    case DC_PN:
                        /*if (txtPnSubinventory.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_sub), Toast.LENGTH_SHORT).show();
                            txtPnSubinventory.requestFocus();
                        //} else if (Arrays.asList(NOT_ALLOWED_SUBS).contains(txtPnSubinventory.getText().toString().trim())) {
                        } else if (LIST_NOT_ALLOWED_SUBS.contains(txtPnSubinventory.getText().toString().trim())) {
                            Toast.makeText(getApplicationContext(), getString(R.string.this_sub_is_controlled_cant_used), Toast.LENGTH_SHORT).show();
                            txtPnSubinventory.requestFocus();
                        } else*/

                        if (txtPnLocator.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_loc), Toast.LENGTH_SHORT).show();
                            txtPnLocator.requestFocus();
                        } else if (txtPnLocator.getText().toString().trim().equals(Constant.LOCATOR_P023)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_other_loc), Toast.LENGTH_SHORT).show();
                            txtPnLocator.requestFocus();
                        } else if (getPartNo().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_sku), Toast.LENGTH_SHORT).show();
                            txtPnPN.requestFocus();
                        } else if (!getPartNo().equals(Util.getSenaoPartNo(item.getPartNo()))) {
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
                            } else if (txtXboardQtyDcPn.getVisibility() == View.VISIBLE && txtXboardQtyDcPn.getText().toString().trim().equals("")) {
                                Toast.makeText(getApplicationContext(), getString(R.string.enter_x_board_qty), Toast.LENGTH_SHORT).show();
                                txtXboardQtyDcPn.requestFocus();
                            } else {
                                hideKeyboard();

                                if (txtXboardQtyDcPn.getVisibility() == View.VISIBLE) {
                                    if (getInputXbQty(txtXboardQtyDcPn) > getAvailableXbQty()) {
                                        showOverConfirmDialog();
                                        return;
                                    }

                                    if (getInputQty(txtPnMultiplier, txtPnMultiplicand, txtPnAddend) == getAvailableQty()
                                            && getInputXbQty(txtXboardQtyDcPn) < getAvailableXbQty()) {
                                        showLessConfirmDialog();
                                        return;
                                    }
                                }

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
                            } else if (txtXboardQtyDcPn.getVisibility() == View.VISIBLE && txtXboardQtyDcPn.getText().toString().trim().equals("")) {
                                Toast.makeText(getApplicationContext(), getString(R.string.enter_x_board_qty), Toast.LENGTH_SHORT).show();
                                txtXboardQtyDcPn.requestFocus();
                            } else {
                                hideKeyboard();

                                if (txtXboardQtyDcPn.getVisibility() == View.VISIBLE) {
                                    if (getInputXbQty(txtXboardQtyDcPn) > getAvailableXbQty()) {
                                        showOverConfirmDialog();
                                        return;
                                    }

                                    if (getInputQty(txtPnQty) == getAvailableQty() && getInputXbQty(txtXboardQtyDcPn) < getAvailableXbQty()) {
                                        showLessConfirmDialog();
                                        return;
                                    }
                                }

                                doStockInProcess();
                            }
                        }
                        break;
                    case DC_QR_CODE:
                        /*if (txtDcQrCodeSubinventory.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_sub), Toast.LENGTH_SHORT).show();
                            txtDcQrCodeSubinventory.requestFocus();
                        //} else if (Arrays.asList(NOT_ALLOWED_SUBS).contains(txtDcQrCodeSubinventory.getText().toString().trim())) {
                        } else if (LIST_NOT_ALLOWED_SUBS.contains(txtDcQrCodeSubinventory.getText().toString().trim())) {
                            Toast.makeText(getApplicationContext(), getString(R.string.this_sub_is_controlled_cant_used), Toast.LENGTH_SHORT).show();
                            txtDcQrCodeSubinventory.requestFocus();
                        } else*/

                        if (txtDcQrCodeLocator.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_loc), Toast.LENGTH_SHORT).show();
                            txtDcQrCodeLocator.requestFocus();
                        } else if (txtDcQrCodeLocator.getText().toString().trim().equals(Constant.LOCATOR_P023)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_other_loc), Toast.LENGTH_SHORT).show();
                            txtDcQrCodeLocator.requestFocus();
                        } else if (txtDcQrCode.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_qrcode), Toast.LENGTH_SHORT).show();
                            txtDcQrCode.requestFocus();
                        } else if (getReelID().length() != 34) {
                            Toast.makeText(getApplicationContext(), getString(R.string.reelid_incorrect_len_must_be_34), Toast.LENGTH_SHORT).show();
                            txtDcQrCode.requestFocus();
                        } else if (!getPartNo().equals(Util.getSenaoPartNo(item.getPartNo()))) {
                            Toast.makeText(getApplicationContext(), getString(R.string.sku_incorrect), Toast.LENGTH_SHORT).show();
                            txtDcQrCode.requestFocus();
                        } else if (getInputQty(txtDcQrCode) > getAvailableQty()) {
                            Toast.makeText(getApplicationContext(), getString(R.string.qty_greater_than_stock_qty), Toast.LENGTH_SHORT).show();
                            txtDcQrCode.requestFocus();
                        } else if (getInputQty(txtDcQrCode) <= 0) {
                            Toast.makeText(getApplicationContext(), getString(R.string.qty_cant_less_or_equals_0), Toast.LENGTH_SHORT).show();
                            txtDcQrCode.requestFocus();
                        } else if (txtXboardQtyDcQrCode.getVisibility() == View.VISIBLE && txtXboardQtyDcQrCode.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_x_board_qty), Toast.LENGTH_SHORT).show();
                            txtXboardQtyDcQrCode.requestFocus();
                        } else if(TextUtils.isEmpty(getOrg())) {
                            txtDcQrCode.requestFocus();
                        } else {
                            hideKeyboard();

                            if (txtXboardQtyDcQrCode.getVisibility() == View.VISIBLE) {
                                if (getInputXbQty(txtXboardQtyDcQrCode) > getAvailableXbQty()) {
                                    showOverConfirmDialog();
                                    return;
                                }

                                if (getInputQty(txtDcQrCode) == getAvailableQty() && getInputXbQty(txtXboardQtyDcQrCode) < getAvailableXbQty()) {
                                    showLessConfirmDialog();
                                    return;
                                }
                            }

                            doStockInProcess();
                        }
                        break;
                    case DC_REEL_ID:
                        /*if (txtReelIdSubinventory.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_sub), Toast.LENGTH_SHORT).show();
                            txtReelIdSubinventory.requestFocus();
                        //} else if (Arrays.asList(NOT_ALLOWED_SUBS).contains(txtReelIdSubinventory.getText().toString().trim())) {
                        } else if (LIST_NOT_ALLOWED_SUBS.contains(txtReelIdSubinventory.getText().toString().trim())) {
                            Toast.makeText(getApplicationContext(), getString(R.string.this_sub_is_controlled_cant_used), Toast.LENGTH_SHORT).show();
                            txtReelIdSubinventory.requestFocus();
                        } else*/

                        if (txtReelIdLocator.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_loc), Toast.LENGTH_SHORT).show();
                            txtReelIdLocator.requestFocus();
                        } else if (txtReelIdLocator.getText().toString().trim().equals(Constant.LOCATOR_P023)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_other_loc), Toast.LENGTH_SHORT).show();
                            txtReelIdLocator.requestFocus();
                        } else if (getReelID().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_reelid), Toast.LENGTH_SHORT).show();
                            txtReelID.requestFocus();
                        } else if (getReelID().length() != 28 && getReelID().length() != 34) {
                            Toast.makeText(getApplicationContext(), getString(R.string.reelid_incorrect_len_must_be_34_or_28), Toast.LENGTH_SHORT).show();
                            txtReelID.requestFocus();
                        } else if (!getPartNo().equals(Util.getSenaoPartNo(item.getPartNo()))) {
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
                            } else if (txtXboardQty.getVisibility() == View.VISIBLE && txtXboardQty.getText().toString().trim().equals("")) {
                                Toast.makeText(getApplicationContext(), getString(R.string.enter_x_board_qty), Toast.LENGTH_SHORT).show();
                                txtXboardQty.requestFocus();
                            } else {
                                hideKeyboard();

                                if (txtXboardQty.getVisibility() == View.VISIBLE) {
                                    if (getInputXbQty(txtXboardQty) > getAvailableXbQty()) {
                                        showOverConfirmDialog();
                                        return;
                                    }

                                    if (getInputQty(txtReelIdMultiplier, txtReelIdMultiplicand, txtReelIdAddend) == getAvailableQty() && getInputXbQty(txtXboardQty) < getAvailableXbQty()) {
                                        showLessConfirmDialog();
                                        return;
                                    }
                                }

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
                            } else if (txtXboardQty.getVisibility() == View.VISIBLE && txtXboardQty.getText().toString().trim().equals("")) {
                                Toast.makeText(getApplicationContext(), getString(R.string.enter_x_board_qty), Toast.LENGTH_SHORT).show();
                                txtXboardQty.requestFocus();
                            } else {
                                hideKeyboard();

                                if (txtXboardQty.getVisibility() == View.VISIBLE) {
                                    if (getInputXbQty(txtXboardQty) > getAvailableXbQty()) {
                                        showOverConfirmDialog();
                                        return;
                                    }

                                    if (getInputQty(txtReelIdQty) == getAvailableQty()
                                            && getInputXbQty(txtXboardQty) < getAvailableXbQty()) {
                                        showLessConfirmDialog();
                                        return;
                                    }
                                }

                                doStockInProcess();
                            }
                        }
                        break;
                    case NO:
                        if (txtNoSubinventory.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_sub), Toast.LENGTH_SHORT).show();
                            txtNoSubinventory.requestFocus();
                        //} else if (Arrays.asList(NOT_ALLOWED_SUBS).contains(txtNoSubinventory.getText().toString().trim())) {
                        } else if (LIST_NOT_ALLOWED_SUBS.contains(txtNoSubinventory.getText().toString().trim())) {
                            Toast.makeText(getApplicationContext(), getString(R.string.this_sub_is_controlled_cant_used), Toast.LENGTH_SHORT).show();
                            txtNoSubinventory.requestFocus();
                        } else if (txtNoLocator.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_loc), Toast.LENGTH_SHORT).show();
                            txtNoLocator.requestFocus();
                        } else if (txtNoLocator.getText().toString().trim().equals(Constant.LOCATOR_P023)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_other_loc), Toast.LENGTH_SHORT).show();
                            txtNoLocator.requestFocus();
                        } else if (txtNoPN.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_sku), Toast.LENGTH_SHORT).show();
                            txtNoPN.requestFocus();
                        } else if (!txtNoPN.getText().toString().trim().equals(Util.getSenaoPartNo(item.getPartNo()))) {
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
                        if (txtPalletSubinventory.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_sub), Toast.LENGTH_SHORT).show();
                            txtPalletSubinventory.requestFocus();
                        //} else if (Arrays.asList(NOT_ALLOWED_SUBS).contains(txtPalletSubinventory.getText().toString().trim())) {
                        } else if (LIST_NOT_ALLOWED_SUBS.contains(txtPalletSubinventory.getText().toString().trim())) {
                            Toast.makeText(getApplicationContext(), getString(R.string.this_sub_is_controlled_cant_used), Toast.LENGTH_SHORT).show();
                            txtPalletSubinventory.requestFocus();
                        } else if (txtPalletLocator.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_loc), Toast.LENGTH_SHORT).show();
                            txtPalletLocator.requestFocus();
                        } else if (txtPalletLocator.getText().toString().trim().equals(Constant.LOCATOR_P023)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_other_loc), Toast.LENGTH_SHORT).show();
                            txtPalletLocator.requestFocus();
                        } else if (rbPalletSN.isChecked() && txtPalletSN.getText().toString().trim().equals("")) {
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
                        if (txtBoxSubinventory.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_sub), Toast.LENGTH_SHORT).show();
                            txtBoxSubinventory.requestFocus();
                        //} else if (Arrays.asList(NOT_ALLOWED_SUBS).contains(txtBoxSubinventory.getText().toString().trim())) {
                        } else if (LIST_NOT_ALLOWED_SUBS.contains(txtBoxSubinventory.getText().toString().trim())) {
                            Toast.makeText(getApplicationContext(), getString(R.string.this_sub_is_controlled_cant_used), Toast.LENGTH_SHORT).show();
                            txtBoxSubinventory.requestFocus();
                        } else if (txtBoxLocator.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_loc), Toast.LENGTH_SHORT).show();
                            txtBoxLocator.requestFocus();
                        } else if (txtBoxLocator.getText().toString().trim().equals(Constant.LOCATOR_P023)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_other_loc), Toast.LENGTH_SHORT).show();
                            txtBoxLocator.requestFocus();
                        } else if (rbBoxSN.isChecked() && txtBoxSN.getText().toString().trim().equals("")) {
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
                        if (txtSnSubinventory.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_sub), Toast.LENGTH_SHORT).show();
                            txtSnSubinventory.requestFocus();
                        //} else if (Arrays.asList(NOT_ALLOWED_SUBS).contains(txtSnSubinventory.getText().toString().trim())) {
                        } else if (LIST_NOT_ALLOWED_SUBS.contains(txtSnSubinventory.getText().toString().trim())) {
                            Toast.makeText(getApplicationContext(), getString(R.string.this_sub_is_controlled_cant_used), Toast.LENGTH_SHORT).show();
                            txtSnSubinventory.requestFocus();
                        } else if (txtSnLocator.getText().toString().trim().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_loc), Toast.LENGTH_SHORT).show();
                            txtSnLocator.requestFocus();
                        } else if (txtSnLocator.getText().toString().trim().equals(Constant.LOCATOR_P023)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.enter_other_loc), Toast.LENGTH_SHORT).show();
                            txtSnLocator.requestFocus();
                        } else if (txtSNStart.getText().toString().trim().equals("")) {
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
                    default:
                        break;
                }
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

        btnYes = findViewById(R.id.button_yes);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Yes", String.valueOf(v.getId()));
                //isMerge = true;
                doQueryReceiptNoList();
            }
        });

        btnNo = findViewById(R.id.button_no);
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick No", String.valueOf(v.getId()));
                //isMerge = false;
                showLayout(LayoutType.RECEIPT_NO);
            }
        });

        showLayout(LayoutType.QUESTION);

        if(Constant.ISORG){
            getSubs(); //ORGOU專案
        }

        //doQuerySubInventory();
    }

    private void getSubs() {
        orgHandler = new OrgHandler();
        new GetNotAllowedSubs().execute(0);
    }

    private class GetNotAllowedSubs extends AsyncTask<Integer, String, OrgHelper> {
        @Override
        protected OrgHelper doInBackground(Integer... params) {
            AppController.debug("Get Not Allowed Subs " + AppController.getServerInfo() + AppController.getProperties("GetNotAllowedSubs"));
            //publishProgress("資料下載中...");
            return orgHandler.getNotAllowedSubs(new OrgHelper());
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("GetOrgInfo onProgressUpdate() " + text[0]);
            //mConnection.setText(text[0]);
            //mConnection.setTextColor(Color.WHITE);
            //mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        @Override
        protected void onPostExecute(OrgHelper result) {
            //execution of result of Long time consuming operation //ORGOU專案
            //finalResult.setText(result);
            //txtResponseArea.setText(result);
            AppController.debug("AcceptanceProcessActivity GetNotAllowedSubs result json = "  + new Gson().toJson(result));

            if (TextUtils.isEmpty(result.getStrErrorBuf())) {
                LIST_NOT_ALLOWED_SUBS.clear();
                LIST_NOT_ALLOWED_SUBS = result.getSubs();
            } else {
                mConnection.setText(getString(R.string.Controlled_subinventory) + getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private void showLessConfirmDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Confirm");
        dialog.setMessage(R.string.label_xb_qty_smaller_than_db);
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setCancelable(false);
        dialog.setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.dismiss();
            }
        });
        dialog.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
                doStockInProcess();
            }
        });
        dialog.show();
    }

    private void showOverConfirmDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Confirm");
        dialog.setMessage(R.string.label_xb_qty_larger_than_db);
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setCancelable(false);
        dialog.setNegativeButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private String getReelID() {
        if (rbQrCode.isChecked()) {
            return QrCodeUtil.getValueFromItemLabelQrCode(txtDcQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID);
        } else if (rbReelID.isChecked()) {
            return txtReelID.getText().toString().trim();
        } else {
            return getPartNo() + getVendorCode() + getDateCode();
        }
    }

    private String getOrg() {
        if (rbQrCode.isChecked()) {
            String org = QrCodeUtil.getValueFromItemLabelQrCode(txtDcQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.ORG);

            try{
                int num=Integer.parseInt(org);
            }catch (NumberFormatException e){
                Toast.makeText(getApplicationContext(), getString(R.string.org_must_input_num),
                        Toast.LENGTH_SHORT).show();
                return "";
            }

            if (org.trim().isEmpty()) {
                Toast.makeText(getApplicationContext(), getString(R.string.org_is_not_null), Toast.LENGTH_SHORT).show();
                return "";
                //return org; //暫時讓沒輸入org的部分先通過 2022 1/1之後要拿掉  todo
            } else if (AppController.getOrg()!=Integer.parseInt(org)) {
                Toast.makeText(getApplicationContext(), getString(R.string.Does_not_match_current_ORG),Toast.LENGTH_LONG).show();
                return "";
            }

            return org;
        }

        return "";
    }

    public String getPartNo() {
        if (mode == LayoutType.DC_QR_CODE || mode == LayoutType.DC_REEL_ID)
            return getReelID().substring(0, 12);
        else
            return txtPnPN.getText().toString().trim();
    }

    public String getVendorCode() {
        if (mode == LayoutType.DC_QR_CODE || mode == LayoutType.DC_REEL_ID)
            return getReelID().substring(12, 18);
        else
            return getDateCode().substring(4);
    }

    public String getDateCode() {
        if (mode == LayoutType.DC_QR_CODE || mode == LayoutType.DC_REEL_ID)
            return getReelID().substring(18, 28);
        else
            return txtPnDC.getText().toString().trim();
    }

    protected double getInputQty(EditText edit) {
        if (mode == LayoutType.DC_QR_CODE)
            return Double.parseDouble(QrCodeUtil.getValueFromItemLabelQrCode(edit.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.QTY));
        else
            return Double.parseDouble(edit.getText().toString().trim());
    }

    private void setQuantityTitle() {
        if (tempItem == null) {
            txtStockQty.setText(getResources().getString(R.string.label_acceptance_stockin_quantity, "0", "0", "0"));
        } else {
            txtStockQty.setText(getResources().getString(R.string.label_acceptance_stockin_quantity,
                    Util.fmt(Util.getDoubleValue(tempItem.getCheckedNotInQty())),
                    Util.fmt(Util.getDoubleValue(tempItem.getInQty())), Util.fmt(Util.getDoubleValue(tempItem.getNotInQty()))));
        }
    }

    private void setXbQtyTitle() {
        if (tempItem != null)
            txtXbQty.setText(getResources().getString(R.string.label_acceptance_xb_quantity,
                    Util.fmt(Util.getDoubleValue(tempItem.getCheckedNotInXbQty())),
                    Util.fmt(Util.getDoubleValue(tempItem.getInXbQty())), Util.fmt(Util.getDoubleValue(tempItem.getNotInXbQty()))));
        else
            txtXbQty.setText(getResources().getString(R.string.label_acceptance_xb_quantity, "0", "0", "0"));
    }


    protected double getAvailableQty() {
        return Util.getDoubleValue(tempItem.getNotInQty());
    }

    protected double getAvailableXbQty() {
        return Util.getDoubleValue(tempItem.getNotInXbQty());
    }

    private double getInputQty(EditText multiplier, EditText multiplicand, EditText addEnd) {
        try {
            return Double.parseDouble(multiplier.getText().toString().trim())
                    * Double.parseDouble(multiplicand.getText().toString().trim())
                    + Double.parseDouble(addEnd.getText().toString().trim());
        } catch (Exception ex) {
            return 0;
        }
    }

    private double getInputXbQty(EditText et) {
        try {
            return Double.parseDouble(et.getText().toString().trim());
        } catch (Exception ex) {
            return 0;
        }
    }

    protected void doQueryReceiptNo() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.chking_form), true);
        tempConditionHelper = new AcceptanceConditionHelper();
        tempConditionHelper.setWareHouseNo(conditionHelper.getWareHouseNo());
        tempConditionHelper.setPartNo(item.getPartNo());

        if (rbReceiptNo.isChecked()) {
            tempConditionHelper.setReceiptsNo(txtReceiptNo.getText().toString().trim());
        } else {
            tempConditionHelper.setTrxIdNo(txtTrxId.getText().toString().trim());
        }

        new GetReceiptNo().execute(0);
    }

    private void doQueryReceiptNoList() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.reading_form_no_data), true);
        tempItem = new AcceptanceInfoHelper();
        tempItem.setPartNo(item.getPartNo());
        tempItem.setWareHouseNo(conditionHelper.getWareHouseNo());
        tempItem.setReceiptNo(conditionHelper.getReceiptsNo());
        tempItem.setTrxId(conditionHelper.getTrxIdNo());
        new GetReceiptNoList().execute(0);
    }

    private void doCheckSN(String sn) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.sn_checking), true);
        txtPreStockQty.setText(R.string.label_prestocking_quantity);
        serialNoHelper = new AcceptanceSerialNoHelper();
        serialNoHelper.setPartNo(tempItem.getPartNo());

        if (!isMerge)
            serialNoHelper.setStockInNo(tempItem.getReceiptNo());

        serialNoHelper.setSerialNo(sn);
        new CheckSN().execute(0);
    }

    private void doCheckPalletBoxNo(String boxNo) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.chk_box_no), true);
        txtPreStockQty.setText(R.string.label_prestocking_quantity);
        serialNoHelper = new AcceptanceSerialNoHelper();
        serialNoHelper.setPartNo(tempItem.getPartNo());

        if (!isMerge)
            serialNoHelper.setStockInNo(tempItem.getReceiptNo());

        serialNoHelper.setBoxNo(boxNo);
        new CheckPalletBoxNo().execute(0);
    }

    private void doCheckPallet(String sn) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.sn_checking), true);
        txtPreStockQty.setText(R.string.label_prestocking_quantity);
        serialNoHelper = new AcceptanceSerialNoHelper();
        serialNoHelper.setPartNo(tempItem.getPartNo());

        if (!isMerge)
            serialNoHelper.setStockInNo(tempItem.getReceiptNo());

        serialNoHelper.setSerialNo(sn);
        new CheckPallet().execute(0);
    }

    private void doCheckSnBox(String sn) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.sn_checking), true);
        txtPreStockQty.setText(R.string.label_prestocking_quantity);
        serialNoHelper = new AcceptanceSerialNoHelper();
        serialNoHelper.setPartNo(tempItem.getPartNo());

        if (!isMerge)
            serialNoHelper.setStockInNo(tempItem.getReceiptNo());

        serialNoHelper.setSerialNo(sn);
        new CheckSnBox().execute(0);
    }

    private void doCheckBoxNo(String boxNo) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.chk_box_no), true);
        serialNoHelper = new AcceptanceSerialNoHelper();
        serialNoHelper.setPartNo(tempItem.getPartNo());

        if (!isMerge)
            serialNoHelper.setStockInNo(tempItem.getReceiptNo());

        serialNoHelper.setBoxNo(boxNo);
        new CheckBoxNo().execute(0);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.hideSoftInputFromWindow(txtPnSubinventory.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(txtPnLocator.getWindowToken(), 0);
    }

    private void setReceiptNoData() {
        /*String applyNo = "";

        if (isMerge) {
            AcceptanceNoHelper[] stockList = noListHelper.getNoList();

            for (AcceptanceNoHelper item : stockList) {
                applyNo += item.getNo() + " ";
            }

            applyNo = applyNo.trim();
            applyNo = applyNo.replace(" ", "\n");
        } else {
            applyNo = txtReceiptNo.getText().toString();
        }

        lblReceiptNo.setText(applyNo);*/

        lblReceiptNo.setText(tempItem.getReceiptNo());
    }

    private void setView() {
        if (item.getControl().equals("SN")) {
            if (rgSN.getCheckedRadioButtonId() == -1)
                rgSN.check(R.id.radio_pallet);
        } else if (item.getControl().equals("DC")) {
            if (rgDC.getCheckedRadioButtonId() == -1)
                rgDC.check(R.id.radio_dc_qrcode);
        } else
            showLayout(LayoutType.NO);
    }

    private void cleanData() {
        switch (mode) {
            case RECEIPT_NO:
                txtReceiptNo.setText("");
                txtTrxId.setText("");

                if (rbReceiptNo.isChecked()) {
                    txtReceiptNo.requestFocus();
                } else {
                    txtTrxId.requestFocus();
                }
                break;
            case DC_PN:
                //txtPnSubinventory.setText("");
                txtPnLocator.setText("");
                //txtPnSubinventory.requestFocus();
                txtPnPN.setText("");
                txtPnDC.setText("");
                txtPnMultiplier.setText("");
                txtPnMultiplicand.setText("");
                txtPnAddend.setText("");
                txtPnQty.setText("");
                txtXboardQtyDcPn.setText("");
                //txtPnPN.requestFocus();
                break;
            case DC_QR_CODE:
                //txtDcQrCodeSubinventory.setText("");
                txtDcQrCodeLocator.setText("");
                //txtDcQrCodeSubinventory.requestFocus();
                txtDcQrCode.setText("");
                txtXboardQtyDcQrCode.setText("");
                //txtDcQrCode.requestFocus();
                break;
            case DC_REEL_ID:
                //txtReelIdSubinventory.setText("");
                txtReelIdLocator.setText("");
                //txtReelIdSubinventory.requestFocus();
                txtReelID.setText("");
                txtReelIdMultiplier.setText("");
                txtReelIdMultiplicand.setText("");
                txtReelIdAddend.setText("");
                txtReelIdQty.setText("");
                txtXboardQty.setText("");
                //txtReelID.requestFocus();
                break;
            case NO:
                txtNoSubinventory.setText("");
                txtNoLocator.setText("");
                txtNoSubinventory.requestFocus();
                txtNoPN.setText("");
                txtNoMultiplier.setText("");
                txtNoMultiplicand.setText("");
                txtNoAddend.setText("");
                txtNoQty.setText("");
                //txtNoPN.requestFocus();
                break;
            case SN_PALLET:
                txtPalletSubinventory.setText("");
                txtPalletLocator.setText("");
                txtPalletSubinventory.requestFocus();
                txtPalletSN.setText("");
                //txtPalletSN.requestFocus();
                break;
            case SN_BOX:
                txtBoxSubinventory.setText("");
                txtBoxLocator.setText("");
                txtBoxSubinventory.requestFocus();
                txtBoxSN.setText("");
                txtBoxCN.setText("");
                //txtBoxSN.requestFocus();
                break;
            case SN_SN:
                txtSnSubinventory.setText("");
                txtSnLocator.setText("");
                txtSnSubinventory.requestFocus();
                txtSNStart.setText("");
                txtSNEnd.setText("");
                //txtSNStart.requestFocus();
                break;
            default:
                break;
        }
    }

    private void cleanDataAfterProcess() {
        switch (mode) {
            case RECEIPT_NO:
                txtReceiptNo.setText("");
                txtTrxId.setText("");

                if (rbReceiptNo.isChecked()) {
                    txtReceiptNo.requestFocus();
                } else {
                    txtTrxId.requestFocus();
                }
                break;
            case DC_PN:
                txtPnPN.setText("");
                txtPnDC.setText("");
                txtPnMultiplier.setText("");
                txtPnMultiplicand.setText("");
                txtPnAddend.setText("");
                txtPnQty.setText("");
                txtXboardQtyDcPn.setText("");
                txtPnPN.requestFocus();
                break;
            case DC_QR_CODE:
                txtDcQrCode.setText("");
                txtDcQrCode.requestFocus();
                txtXboardQtyDcQrCode.setText("");
                break;
            case DC_REEL_ID:
                txtReelID.setText("");
                txtReelID.requestFocus();
                txtReelIdMultiplier.setText("");
                txtReelIdMultiplicand.setText("");
                txtReelIdAddend.setText("");
                txtReelIdQty.setText("");
                txtXboardQty.setText("");
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
            default:
                break;
        }
    }

    protected void doStockInProcess() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.stock_proccessing), true);
        processInfoHelper = new AcceptanceProcessInfoHelper();
        processInfoHelper.setPartNo(tempItem.getPartNo());

        if (isMerge) {
            processInfoHelper.setReceiptNo(null);
        } else {
            processInfoHelper.setReceiptNo(this.txtReceiptNo.getText().toString().trim());
        }

        String sn;
        switch (mode) {
            case DC_PN:
                //processInfoHelper.setSubinventory(txtPnSubinventory.getText().toString().trim());
                processInfoHelper.setSubinventory(spinnerSubs.getSelectedItem().toString().trim());
                processInfoHelper.setLocator(txtPnLocator.getText().toString().trim());
                //processInfoHelper.setReelID(getReelID().substring(0, 28));
                processInfoHelper.setDateCode(getDateCode());

                if (rbPnEquation.isChecked())
                    processInfoHelper.setQty(BigDecimal.valueOf(getInputQty(txtPnMultiplier, txtPnMultiplicand, txtPnAddend)));
                else
                    processInfoHelper.setQty(BigDecimal.valueOf(getInputQty(txtPnQty)));

                if (txtXboardQtyDcPn.getVisibility() == View.VISIBLE) {
                    processInfoHelper.setXbQty(BigDecimal.valueOf(Double.parseDouble(txtXboardQtyDcPn.getText().toString().trim())));
                } else {
                    processInfoHelper.setXbQty(BigDecimal.valueOf(0));
                }
                break;
            case DC_QR_CODE:
                //processInfoHelper.setSubinventory(txtDcQrCodeSubinventory.getText().toString().trim());
                processInfoHelper.setSubinventory(spinnerSubs01.getSelectedItem().toString().trim());
                processInfoHelper.setLocator(txtDcQrCodeLocator.getText().toString().trim());
                processInfoHelper.setReelID(getReelID().substring(0, 28));
                processInfoHelper.setDateCode(getDateCode());
                processInfoHelper.setQty(BigDecimal.valueOf(getInputQty(txtDcQrCode)));

                if (txtXboardQtyDcQrCode.getVisibility() == View.VISIBLE) {
                    processInfoHelper.setXbQty(BigDecimal.valueOf(Double.parseDouble(txtXboardQtyDcQrCode.getText().toString().trim())));
                } else {
                    processInfoHelper.setXbQty(BigDecimal.valueOf(0));
                }
                break;
            case DC_REEL_ID:
                //processInfoHelper.setSubinventory(txtReelIdSubinventory.getText().toString().trim());
                processInfoHelper.setSubinventory(spinnerSubs02.getSelectedItem().toString().trim());
                processInfoHelper.setLocator(txtReelIdLocator.getText().toString().trim());
                processInfoHelper.setReelID(getReelID().substring(0, 28));
                processInfoHelper.setDateCode(getDateCode());

                if (rbReelIdEquation.isChecked())
                    processInfoHelper.setQty(BigDecimal.valueOf(getInputQty(txtReelIdMultiplier, txtReelIdMultiplicand, txtReelIdAddend)));
                else
                    processInfoHelper.setQty(BigDecimal.valueOf(getInputQty(txtReelIdQty)));

                if (txtXboardQty.getVisibility() == View.VISIBLE) {
                    processInfoHelper.setXbQty(BigDecimal.valueOf(Double.parseDouble(txtXboardQty.getText().toString().trim())));
                } else {
                    processInfoHelper.setXbQty(BigDecimal.valueOf(0));
                }
                break;
            case NO:
                processInfoHelper.setSubinventory(txtNoSubinventory.getText().toString().trim());
                processInfoHelper.setLocator(txtNoLocator.getText().toString().trim());

                if (rbNoEquation.isChecked())
                    processInfoHelper.setQty(BigDecimal.valueOf(getInputQty(txtNoMultiplier, txtNoMultiplicand, txtNoAddend)));
                else
                    processInfoHelper.setQty(BigDecimal.valueOf(getInputQty(txtNoQty)));
                break;
            case SN_PALLET:
                processInfoHelper.setSubinventory(txtPalletSubinventory.getText().toString().trim());
                processInfoHelper.setLocator(txtPalletLocator.getText().toString().trim());
                sn = txtPalletSN.getText().toString().trim();
                processInfoHelper.setSerialNoStart(sn);
                processInfoHelper.setSerialNoEnd(sn);
                processInfoHelper.setBoxNo(txtPalletCN.getText().toString().trim());
                break;
            case SN_BOX:
                processInfoHelper.setSubinventory(txtBoxSubinventory.getText().toString().trim());
                processInfoHelper.setLocator(txtBoxLocator.getText().toString().trim());
                sn = txtBoxSN.getText().toString().trim();
                processInfoHelper.setSerialNoStart(sn);
                processInfoHelper.setSerialNoEnd(sn);
                processInfoHelper.setBoxNo(txtBoxCN.getText().toString().trim());
                break;
            case SN_SN:
                //processInfoHelper.setSubinventory(txtPnSubinventory.getText().toString().trim());
                processInfoHelper.setSubinventory(spinnerSubs.getSelectedItem().toString().trim());
                processInfoHelper.setLocator(txtPnLocator.getText().toString().trim());
                processInfoHelper.setSerialNoStart(txtSNStart.getText().toString().trim());
                processInfoHelper.setSerialNoEnd(txtSNEnd.getText().toString().trim());
                break;
            default:
                return;
        }

        new DoStockInProcess().execute(0);
    }

    private void showLayout(LayoutType type) {
        mode = type;
        lInfo.setVisibility(View.GONE);
        btnYes.setVisibility(View.GONE);
        btnNo.setVisibility(View.GONE);
        rgDC.setVisibility(View.INVISIBLE);
        rgSN.setVisibility(View.INVISIBLE);
        lPalletLayout.setVisibility(View.GONE);
        lNoLayout.setVisibility(View.GONE);
        lQrCodeLayout.setVisibility(View.GONE);
        lReelIDLayout.setVisibility(View.GONE);
        lQuestionLayout.setVisibility(View.GONE);
        lReceiptNoLayout.setVisibility(View.GONE);
        lPNLayout.setVisibility(View.GONE);
        lBoxLayout.setVisibility(View.GONE);
        lSNLayout.setVisibility(View.GONE);

        switch (type) {
            case QUESTION:
                lQuestionLayout.setVisibility(View.VISIBLE);
                btnYes.setVisibility(View.VISIBLE);
                btnNo.setVisibility(View.VISIBLE);
                break;
            case RECEIPT_NO:
                lReceiptNoLayout.setVisibility(View.VISIBLE);
                if (tempItem != null) {
                    txtReceiptNo.setText(tempItem.getReceiptNo());
                    txtTrxId.setText(tempItem.getTrxId());

                    if (rbReceiptNo.isChecked()) {
                        txtReceiptNo.requestFocus();
                        txtReceiptNo.selectAll();
                    } else {
                        txtTrxId.requestFocus();
                        txtTrxId.selectAll();
                    }
                } else {
                    txtReceiptNo.setText(item.getReceiptNo());
                    txtTrxId.setText(item.getTrxId());

                    if (rbReceiptNo.isChecked()) {
                        txtReceiptNo.requestFocus();
                        txtReceiptNo.selectAll();
                    } else if (rbTrxId.isChecked()) {
                        txtTrxId.requestFocus();
                        txtTrxId.selectAll();
                    } else {
                        rbReceiptNo.setChecked(true);
                    }
                }
                break;
            case DC_PN:
                lInfo.setVisibility(View.VISIBLE);
                rgDC.setVisibility(View.VISIBLE);
                lPNLayout.setVisibility(View.VISIBLE);

                if (item.getPartNo() != null && item.getPartNo().substring(0, 4).equals(PREFIX_ITEM_NO_PCB)) {
                    lblXboardQtyDcPn.setVisibility(View.VISIBLE);
                    txtXboardQtyDcPn.setVisibility(View.VISIBLE);
                } else {
                    lblXboardQtyDcPn.setVisibility(View.GONE);
                    txtXboardQtyDcPn.setVisibility(View.GONE);
                }

                //rbPnEquation.setChecked(true);
                rbPnQty.setChecked(true);
                //txtPnSubinventory.requestFocus();
                //txtPnPN.requestFocus();
                break;
            case DC_QR_CODE:
                lInfo.setVisibility(View.VISIBLE);
                rgDC.setVisibility(View.VISIBLE);
                lQrCodeLayout.setVisibility(View.VISIBLE);

                if (item.getPartNo() != null && item.getPartNo().substring(0, 4).equals(PREFIX_ITEM_NO_PCB)) {
                    lblXboardQtyDcQrCode.setVisibility(View.VISIBLE);
                    txtXboardQtyDcQrCode.setVisibility(View.VISIBLE);
                } else {
                    lblXboardQtyDcQrCode.setVisibility(View.GONE);
                    txtXboardQtyDcQrCode.setVisibility(View.GONE);
                }

                //txtDcQrCode.requestFocus();
                //txtDcQrCodeSubinventory.requestFocus();
                txtDcQrCodeLocator.requestFocus();
                break;
            case DC_REEL_ID:
                lInfo.setVisibility(View.VISIBLE);
                rgDC.setVisibility(View.VISIBLE);
                lReelIDLayout.setVisibility(View.VISIBLE);

                if (item.getPartNo() != null && item.getPartNo().substring(0, 4).equals(PREFIX_ITEM_NO_PCB)) {
                    lblXboardQty.setVisibility(View.VISIBLE);
                    txtXboardQty.setVisibility(View.VISIBLE);
                } else {
                    lblXboardQty.setVisibility(View.GONE);
                    txtXboardQty.setVisibility(View.GONE);
                }

                //rbReelIdEquation.setChecked(true);
                rbReelIdQty.setChecked(true);
                //txtReelIdSubinventory.requestFocus();
                txtReelIdLocator.requestFocus();
                //txtReelID.requestFocus();
                break;
            case NO:
                lInfo.setVisibility(View.VISIBLE);
                lNoLayout.setVisibility(View.VISIBLE);
                //rbNoEquation.setChecked(true);
                rbNoQty.setChecked(true);
                txtNoSubinventory.requestFocus();
                //txtNoPN.requestFocus();
                break;
            case SN_PALLET:
                lInfo.setVisibility(View.VISIBLE);
                rgSN.setVisibility(View.VISIBLE);
                lPalletLayout.setVisibility(View.VISIBLE);
                rbPalletSN.setChecked(true);
                txtPalletSubinventory.requestFocus();
                break;
            case SN_BOX:
                lInfo.setVisibility(View.VISIBLE);
                rgSN.setVisibility(View.VISIBLE);
                lBoxLayout.setVisibility(View.VISIBLE);
                rbBoxSN.setChecked(true);
                txtBoxSubinventory.requestFocus();
                break;
            case SN_SN:
                lInfo.setVisibility(View.VISIBLE);
                rgSN.setVisibility(View.VISIBLE);
                lSNLayout.setVisibility(View.VISIBLE);
                //txtSNStart.requestFocus();
                txtSnSubinventory.requestFocus();
                break;
            default:
                break;
        }
    }

    private void returnPage() {
        hideKeyboard();

        switch (mode) {
            case QUESTION:
                if (needRefresh) {
                    setResult(RESULT_OK);
                }

                finish();
                break;
            case RECEIPT_NO:
                tempItem = null;
                txtReceiptNo.setText("");
                showLayout(LayoutType.QUESTION);
                break;
            case DC_PN:
                lblReceiptNo.setText("");
                rgDC.clearCheck();
                txtPnPN.setText("");
                txtPnDC.setText("");
                rbPnEquation.setChecked(false);
                txtPnMultiplier.setText("");
                txtPnMultiplicand.setText("");
                txtPnAddend.setText("");
                rbPnQty.setChecked(false);
                txtPnQty.setText("");
                txtXboardQtyDcPn.setText("");

                if (isMerge) {
                    tempItem = null;
                    showLayout(LayoutType.QUESTION);
                } else {
                    tempConditionHelper = null;
                    showLayout(LayoutType.RECEIPT_NO);
                }
                break;
            case DC_QR_CODE:
                lblReceiptNo.setText("");
                rgDC.clearCheck();
                rbQrCode.setChecked(false);
                txtDcQrCode.setText("");
                txtXboardQtyDcQrCode.setText("");

                if (isMerge) {
                    tempItem = null;
                    showLayout(LayoutType.QUESTION);
                } else {
                    tempConditionHelper = null;
                    showLayout(LayoutType.RECEIPT_NO);
                }
            case DC_REEL_ID:
                lblReceiptNo.setText("");
                rgDC.clearCheck();
                rbReelIdEquation.setChecked(false);
                txtReelID.setText("");
                txtReelIdMultiplier.setText("");
                txtReelIdMultiplicand.setText("");
                txtReelIdAddend.setText("");
                rbReelIdQty.setChecked(false);
                txtReelIdQty.setText("");
                txtXboardQty.setText("");

                if (isMerge) {
                    tempItem = null;
                    showLayout(LayoutType.QUESTION);
                } else {
                    tempConditionHelper = null;
                    showLayout(LayoutType.RECEIPT_NO);
                }
            case NO:
                lblReceiptNo.setText("");
                txtNoPN.setText("");
                rbNoEquation.setChecked(false);
                txtNoMultiplier.setText("");
                txtNoMultiplicand.setText("");
                txtNoAddend.setText("");
                rbNoQty.setChecked(false);
                txtNoQty.setText("");

                if (isMerge) {
                    tempItem = null;
                    showLayout(LayoutType.QUESTION);
                } else {
                    tempConditionHelper = null;
                    showLayout(LayoutType.RECEIPT_NO);
                }
                break;
            case SN_PALLET:
                rgSN.clearCheck();
                rbPalletSN.setChecked(false);
                txtPalletSN.setText("");
                rbPalletCN.setChecked(false);
                txtPalletCN.setText("");
                lblReceiptNo.setText("");
                txtPreStockQty.setText(getString(R.string.label_prestocking_quantity));

                if (isMerge) {
                    tempItem = null;
                    showLayout(LayoutType.QUESTION);
                } else {
                    tempConditionHelper = null;
                    showLayout(LayoutType.RECEIPT_NO);
                }
                break;
            case SN_BOX:
                lblReceiptNo.setText("");
                rgSN.clearCheck();
                rbBoxSN.setChecked(false);
                txtBoxSN.setText("");
                rbBoxCN.setChecked(false);
                txtBoxCN.setText("");
                txtPreStockQty.setText(getString(R.string.label_prestocking_quantity));

                if (isMerge) {
                    tempItem = null;
                    showLayout(LayoutType.QUESTION);
                } else {
                    tempConditionHelper = null;
                    showLayout(LayoutType.RECEIPT_NO);
                }
                break;
            case SN_SN:
                lblReceiptNo.setText("");
                rgSN.clearCheck();
                txtSNStart.setText("");
                txtSNEnd.setText("");

                if (isMerge) {
                    tempItem = null;
                    showLayout(LayoutType.QUESTION);
                } else {
                    tempConditionHelper = null;
                    showLayout(LayoutType.RECEIPT_NO);
                }
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
            //setQuantityTitle();
            //setXbQtyTitle();
        }
    }

    private void printPalletLabel(String palletNo) {
        if (!TextUtils.isEmpty(palletNo)) {
            String[] AryPallet = palletNo.split(",");

            for (String no : AryPallet) {
                if (!palletNoList.contains(no)) {
                    palletNoList.add(no);
                    dialog = ProgressDialog.show(AcceptanceProcessActivity.this, getString(R.string.holdon), getString(R.string.printingLabel), true);
                    errorInfo = "";

                    if (!BtPrintLabel.printPalletReceived(Util.getCurrentTimeStamp(), no)) {
                        errorInfo = getString(R.string.printLabalFailed)+" AcceptanceProcessActivity";
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

    private void setQuantity() {
        double quantity = Util.getDoubleValue(processInfoHelper.getQty());
        tempItem.setInQty(tempItem.getInQty().add(BigDecimal.valueOf(quantity)));
        tempItem.setNotInQty(tempItem.getNotInQty().subtract(BigDecimal.valueOf(quantity)));
    }

    private void setXbQty() {
        double quantity = Util.getDoubleValue(processInfoHelper.getXbQty());
        tempItem.setInXbQty(BigDecimal.valueOf(Util.getDoubleValue(tempItem.getInXbQty()) + quantity));
        tempItem.setNotInXbQty(BigDecimal.valueOf(Util.getDoubleValue(tempItem.getNotInXbQty()) - quantity));
    }

    private void checkReturnAndSetView() {
        if (Util.getDoubleValue(tempItem.getNotInQty()) == 0) {
            if (Util.getDoubleValue(tempItem.getInQty()) > 0)
                Toast.makeText(getApplication(), getString(R.string.sku) + tempItem.getPartNo() + getString(R.string.chk_ok), Toast.LENGTH_LONG).show();

            setResult(RESULT_OK);
            finish();
        }
    }

    public void onBackPressed() {
        //do something here and don't write super.onBackPressed()
        Log.d(TAG, "onClick Hardware Back Button");
        returnPage();
    }

    private void setAccData() {
        if (tempConditionHelper != null && tempConditionHelper.getInfoList() != null) {
            double checkedNotInQty = 0;
            double inQty = 0;
            double notInQty = 0;
            double checkedNotInXbQty = 0;
            double inXbQty = 0;
            double notInXbQty = 0;

            for (AcceptanceInfoHelper item : tempConditionHelper.getInfoList()) {
                checkedNotInQty += item.getCheckedNotInQty().doubleValue();
                inQty += item.getInQty().doubleValue();
                notInQty += item.getNotInQty().doubleValue();
                checkedNotInXbQty += Util.getDoubleValue(item.getCheckedNotInXbQty());
                inXbQty += Util.getDoubleValue(item.getInXbQty());
                notInXbQty += Util.getDoubleValue(item.getNotInXbQty());
            }

            tempItem = new AcceptanceInfoHelper();
            tempItem.setPartNo(tempConditionHelper.getPartNo());
            tempItem.setWareHouseNo(tempConditionHelper.getWareHouseNo());
            tempItem.setReceiptNo(tempConditionHelper.getReceiptsNo());
            tempItem.setTrxId(tempConditionHelper.getTrxIdNo());
            tempItem.setCheckedNotInQty(BigDecimal.valueOf(checkedNotInQty));
            tempItem.setInQty(BigDecimal.valueOf(inQty));
            tempItem.setNotInQty(BigDecimal.valueOf(notInQty));
            tempItem.setCheckedNotInXbQty(BigDecimal.valueOf(checkedNotInXbQty));
            tempItem.setInXbQty(BigDecimal.valueOf(inXbQty));
            tempItem.setNotInXbQty(BigDecimal.valueOf(notInXbQty));
        }
    }

    private void setQuantityTitlea() {
        if (tempItem == null) {
            txtStockQty.setText(getResources().getString(R.string.label_acceptance_stockin_quantity, "0", "0", "0"));
        } else {
            txtStockQty.setText(getResources().getString(R.string.label_acceptance_stockin_quantity,
                    Util.fmt(Util.getDoubleValue(tempItem.getCheckedNotInQty())),
                    Util.fmt(Util.getDoubleValue(tempItem.getInQty())), Util.fmt(Util.getDoubleValue(tempItem.getNotInQty()))));
        }
    }

    private void setXbQtyTitleb() {
        if (tempItem != null)
            txtXbQty.setText(getResources().getString(R.string.label_acceptance_xb_quantity,
                    Util.fmt(Util.getDoubleValue(tempItem.getCheckedNotInXbQty())),
                    Util.fmt(Util.getDoubleValue(tempItem.getInXbQty())), Util.fmt(Util.getDoubleValue(tempItem.getNotInXbQty()))));
        else
            txtXbQty.setText(getResources().getString(R.string.label_acceptance_xb_quantity, "0", "0", "0"));
    }

    enum LayoutType {
        NONE, QUESTION, RECEIPT_NO, DC_PN, DC_REEL_ID, NO, SN_PALLET, SN_BOX, SN_SN, DC_QR_CODE
    }

    private class GetReceiptNoList extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            publishProgress(getString(R.string.downloading_data));

            if (accType == AcceptanceActivity.OUTSOURCING) {
                AppController.debug("Get StockNo List Osp from " + AppController.getServerInfo() + AppController.getProperties("GetReceiptNoListOsp"));
                return handler.getReceiptNoListOsp(tempItem);
            } else {
                AppController.debug("Get StockNo List from " + AppController.getServerInfo() + AppController.getProperties("GetReceiptNoList"));
                return handler.getReceiptNoList(tempItem);
            }
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            //Things to be done while execution of long running operation is in
            //progress. For example updating ProgessDialog
        }

        @Override
        protected void onPostExecute(BasicHelper result) {
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    tempItem = (AcceptanceInfoHelper) result;
                    isMerge = true;
                    setQuantityTitle();
                    setXbQtyTitle();
                    setReceiptNoData();
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

    private class GetReceiptNo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            publishProgress(getString(R.string.downloading_data));

            if (accType == AcceptanceActivity.OUTSOURCING) {
                AppController.debug("Get StockInfo Osp from " + AppController.getServerInfo() + AppController.getProperties("GetReceiptNoOsp"));
                return handler.getReceiptNoOsp(tempConditionHelper);
            } else {
                AppController.debug("Get StockInfo from " + AppController.getServerInfo() + AppController.getProperties("GetReceiptNo"));
                return handler.getReceiptNo(tempConditionHelper);
            }
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            //Things to be done while execution of long running operation is in
            //progress. For example updating ProgessDialog
        }

        @Override
        protected void onPostExecute(BasicHelper result) {
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    tempConditionHelper = (AcceptanceConditionHelper) result;
                    isMerge = false;
                    setAccData();
                    setQuantityTitle();
                    setXbQtyTitle();
                    setReceiptNoData();
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
            AppController.debug("Check Box Number from " + AppController.getServerInfo() + AppController.getProperties("AcceptanceCheckPalletBoxNo"));
            publishProgress(getString(R.string.downloading_data));
            return handler.checkPalletBoxNo(serialNoHelper);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            //Things to be done while execution of long running operation is in
            //progress. For example updating ProgessDialog
        }

        @Override
        protected void onPostExecute(BasicHelper result) {
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    serialNoHelper = (AcceptanceSerialNoHelper) result;
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
            AppController.debug("Check SN from " + AppController.getServerInfo() + AppController.getProperties("AcceptanceCheckSN"));
            publishProgress(getString(R.string.downloading_data));
            return handler.checkSN(serialNoHelper);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            //Things to be done while execution of long running operation is in
            //progress. For example updating ProgessDialog
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
            AppController.debug("Check Pallet  from " + AppController.getServerInfo() + AppController.getProperties("AcceptanceCheckPallet"));
            publishProgress(getString(R.string.downloading_data));
            return handler.checkPallet(serialNoHelper);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            //Things to be done while execution of long running operation is in
            //progress. For example updating ProgessDialog
        }

        @Override
        protected void onPostExecute(BasicHelper result) {
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    serialNoHelper = (AcceptanceSerialNoHelper) result;
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
            AppController.debug("Check BOX from " + AppController.getServerInfo() + AppController.getProperties("AcceptanceCheckSnBox"));
            publishProgress(getString(R.string.downloading_data));
            return handler.checkSnBox(serialNoHelper);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            //Things to be done while execution of long running operation is in
            //progress. For example updating ProgessDialog
        }

        @Override
        protected void onPostExecute(BasicHelper result) {
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    serialNoHelper = (AcceptanceSerialNoHelper) result;
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
            AppController.debug("Check Box Number from " + AppController.getServerInfo() + AppController.getProperties("AcceptanceCheckBoxNo"));
            publishProgress(getString(R.string.downloading_data));
            return handler.checkBoxNo(serialNoHelper);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            //Things to be done while execution of long running operation is in
            //progress. For example updating ProgessDialog
        }

        @Override
        protected void onPostExecute(BasicHelper result) {
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    serialNoHelper = (AcceptanceSerialNoHelper) result;
                    //txtBoxSN.setText(serialNoHelper.getValue());
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

    private class DoStockInProcess extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Do acceptance stock in process from ");
            publishProgress(getString(R.string.stock_proccessing));

            if (isMerge) {
                switch (mode) {
                    case DC_PN:
                        return handler.doDcMergePn(processInfoHelper);
                    case DC_QR_CODE:

                    case DC_REEL_ID:
                        return handler.doDCMergeReelID(processInfoHelper);
                    case NO:
                        return handler.doNoMerge(processInfoHelper);
                    case SN_PALLET:
                        return handler.doSnMergePallet(processInfoHelper);
                    case SN_BOX:
                        return handler.doSnMergeBox(processInfoHelper);
                    case SN_SN:
                        return handler.doSnMergeSn(processInfoHelper);
                    default:
                        return null;
                }
            } else {
                switch (mode) {
                    case DC_PN:
                        return handler.doDcPn(processInfoHelper);
                    case DC_QR_CODE:

                    case DC_REEL_ID:
                        return handler.doDCReelID(processInfoHelper);
                    case NO:
                        return handler.doNoNotMerge(processInfoHelper);
                    case SN_PALLET:
                        return handler.doSnPallet(processInfoHelper);
                    case SN_BOX:
                        return handler.doSnBox(processInfoHelper);
                    case SN_SN:
                        return handler.doSnSn(processInfoHelper);
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
            //Things to be done while execution of long running operation is in
            //progress. For example updating ProgessDialog
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
                    processInfoHelper = (AcceptanceProcessInfoHelper) result;
                    needRefresh = true;
                    txtPreStockQty.setText(R.string.label_prestocking_quantity);
                    setQuantity();
                    setQuantityTitle();
                    setXbQty();
                    setXbQtyTitle();
                    cleanDataAfterProcess();
                    Toast.makeText(getApplicationContext(), getString(R.string.checkOK), Toast.LENGTH_SHORT).show();
                    checkReturnAndSetView();
                } else {
                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    Toast.makeText(getApplicationContext(), errorInfo, Toast.LENGTH_SHORT).show();

                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));

                        /*if (isMerge)
                            doQueryStockNoList();
                        else
                            doQueryStockInNo();*/
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

    private void doQuerySubInventory() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.reading_sub_lot), true);
        orgHandler = new OrgHandler();
        new GetInStockSubs().execute(0);
    }
    private class GetInStockSubs extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            publishProgress(getString(R.string.downloading_data));
            return orgHandler.getInStockSubs(subinventoryHelper);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            //Things to be done while execution of long running operation is in
            //progress. For example updating ProgessDialog
        }

        @Override
        protected void onPostExecute(BasicHelper result) {
            dialog.dismiss();
            AppController.debug("GetInStockSubs result json = " + new Gson().toJson(result));

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

    public void setInventoryData(SubinventoryInfoHelper item) {
            adapterSubs= new ArrayAdapter<>(this, R.layout.spinner_item, item.getSubinventories());
            spinnerSubs.setAdapter(adapterSubs);
            spinnerSubs01.setAdapter(adapterSubs);
            spinnerSubs02.setAdapter(adapterSubs);
            adapterSubs.notifyDataSetChanged();
    }
}
