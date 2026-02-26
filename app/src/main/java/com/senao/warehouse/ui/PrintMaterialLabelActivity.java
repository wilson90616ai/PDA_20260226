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
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tscdll.TscWifiActivity;
import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.ITEM_LABEL_QR_CODE_FORMAT;
import com.senao.warehouse.database.MaterialLabelHelper;
import com.senao.warehouse.database.NewPrintData;
import com.senao.warehouse.database.PnItemInfoHelper;
import com.senao.warehouse.handler.PrintLabelHandler;
import com.senao.warehouse.print.BtPrintLabel;
import com.senao.warehouse.util.CharUtil;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.Preferences;
import com.senao.warehouse.util.QrCodeUtil;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class PrintMaterialLabelActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PRINTER_SETTING = 2;
    private static final int REQUEST_BLUETOOTH_SETTINGS = 3;
    private static final String TAG = PrintMaterialLabelActivity.class.getSimpleName();
    private final List<String> poWithQtyListReelId = new ArrayList<>();
    private final List<String> poWithQtyListPn = new ArrayList<>();
    private TextView mConnection, labelListSummary, labelListSummaryReelId,lblTitle;
    private EditText txtImportQty, txtImportPartNo, txtImportDC, txtImportVC, txtImportPackQty,
            txtImportPoWithQty, txtImportQrCode, txtImportReelId, txtImportReelIdQty, txtImportReelIdPackQty,
            txtImportReelIdPoWithQty, txtImportQrCodePackQty;
    private EditText txtImportCOO, txtImportReelIdCOO; //20260108 Ann Add
    private Button btnReturn, btnConfim, btnCancel, btnInputReelId, btnInputPn;
    private ProgressDialog dialog;
    private String errorInfo = "",orgPrint="";
    private PrintLabelHandler print;
    private PnItemInfoHelper pnInfo;
    private MaterialLabelHelper mlInfo;
    private TscWifiActivity TscEthernetDll;
    private RadioGroup rgDC, rgPrinter;
    private RadioButton rbQrCode, rbReelID, rbPartNo, rbBluetooth, rbWifi,rbCodesoft;
    private LinearLayout qrcodeLayout, pnLayout, reelIdLayout;
    private List<NewPrintData> PrintDataList;

    private final RadioGroup.OnCheckedChangeListener dcListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio_dc_qrcode:
                    if (rbQrCode.isChecked()) {
                        pnLayout.setVisibility(View.GONE);
                        reelIdLayout.setVisibility(View.GONE);
                        qrcodeLayout.setVisibility(View.VISIBLE);
                        txtImportQrCode.requestFocus();
                    }
                    break;
                case R.id.radio_reel_id:
                    if (rbReelID.isChecked()) {
                        pnLayout.setVisibility(View.GONE);
                        reelIdLayout.setVisibility(View.VISIBLE);
                        qrcodeLayout.setVisibility(View.GONE);
                        setListSummary(poWithQtyListReelId, labelListSummaryReelId);
                        txtImportReelId.requestFocus();
                    }
                    break;
                case R.id.radio_part_number:
                    if (rbPartNo.isChecked()) {
                        pnLayout.setVisibility(View.VISIBLE);
                        reelIdLayout.setVisibility(View.GONE);
                        qrcodeLayout.setVisibility(View.GONE);
                        setListSummary(poWithQtyListPn, labelListSummary);
                        txtImportPartNo.requestFocus();
                    }
                    break;
            }
        }
    };

    private ListView lvReelId, lvPn;
    private ArrayAdapter<String> mAdapterReelId, mAdapterPn;
    private int selectedIndex;
    private boolean isBluetoothPrintSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_material_label);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(PrintMaterialLabelActivity.this);
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
            }
        });

        rgDC = findViewById(R.id.radio_group_dc);
        rbQrCode = findViewById(R.id.radio_dc_qrcode);
        rbReelID = findViewById(R.id.radio_reel_id);
        rbPartNo = findViewById(R.id.radio_part_number);
        rgDC.setOnCheckedChangeListener(dcListener);

        rgPrinter = findViewById(R.id.radio_printer);
        rbBluetooth = findViewById(R.id.radio_bluetooth);
        rbBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !isBluetoothPrintSet) {
                    isBluetoothPrintSet = true;
                    checkPrinterSetting();
                }
            }
        });
        rbWifi = findViewById(R.id.radio_wifi);
        rbCodesoft=findViewById(R.id.radio_codesoft);
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
                        txtImportQrCodePackQty.requestFocus();
                        return true;
                    }
                }

                return false;
            }
        });

        txtImportQrCodePackQty = findViewById(R.id.edittext_qrcode_pack_qty);
        txtImportQrCodePackQty.setSelectAllOnFocus(true);

        reelIdLayout = findViewById(R.id.reelIdLayout);
        txtImportReelId = findViewById(R.id.edittext_reelid);
        txtImportReelId.setFilters(new InputFilter[]{new InputFilter.LengthFilter(34)});
        txtImportReelId.setSelectAllOnFocus(true);
        txtImportReelIdQty = findViewById(R.id.edittext_reelid_qty);
        txtImportReelIdQty.setSelectAllOnFocus(true);
        txtImportReelIdPackQty = findViewById(R.id.edittext_reelid_pack_qty);
        txtImportReelIdPackQty.setSelectAllOnFocus(true);
        txtImportReelIdCOO = findViewById(R.id.edittext_reelid_coo);
        txtImportReelIdCOO.setSelectAllOnFocus(true);
        labelListSummaryReelId = findViewById(R.id.label_list_summary_reelid);

        txtImportReelIdPoWithQty = findViewById(R.id.edittext_reelid_po_qty);
        txtImportReelIdPoWithQty.setSelectAllOnFocus(true);
        txtImportReelIdPoWithQty.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down " + v.getId());
                    EditText et = (EditText) v;

                    if (TextUtils.isEmpty(et.getText())) {
                        return false;
                    } else {
                        insertPoWithQty();
                        return true;
                    }
                }

                return false;
            }
        });

        btnInputReelId = findViewById(R.id.button_input_reelid);
        btnInputReelId.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtImportReelIdPoWithQty.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.plz_enter_po_num_and_qty), Toast.LENGTH_SHORT).show();
                    txtImportReelIdPoWithQty.requestFocus();
                } else {
                    insertPoWithQty();
                }
            }
        });

        lvReelId = findViewById(R.id.listViewReelId);
        lvReelId.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            // Yes button clicked
                            mAdapterReelId.remove(poWithQtyListReelId.get(selectedIndex));
                            setListSummary(poWithQtyListReelId, labelListSummaryReelId);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(PrintMaterialLabelActivity.this);
                builder.setTitle( getString(R.string.btn_ok)).setMessage(getString(R.string.is_del_data))
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show();
                return true;
            }
        });

        mAdapterReelId = new ArrayAdapter<>(this, R.layout.simple_list_item, poWithQtyListReelId);
        lvReelId.setAdapter(mAdapterReelId);

        pnLayout = findViewById(R.id.pnLayout);
        txtImportPartNo = findViewById(R.id.edittext_pn);
        // 限制長度為12
        txtImportPartNo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        txtImportPartNo.setSelectAllOnFocus(true);
        txtImportQty = findViewById(R.id.edittext_qty);
        txtImportQty.setSelectAllOnFocus(true);
        txtImportDC = findViewById(R.id.edittext_dc);
        txtImportDC.setSelectAllOnFocus(true);
        txtImportVC = findViewById(R.id.edittext_vendor_code);
        txtImportVC.setSelectAllOnFocus(true);
        txtImportPackQty = findViewById(R.id.edittext_pack_qty);
        txtImportPackQty.setSelectAllOnFocus(true);
        txtImportCOO = findViewById(R.id.edittext_coo);
        txtImportCOO.setSelectAllOnFocus(true);
        labelListSummary = findViewById(R.id.label_list_summary);

        txtImportPoWithQty = findViewById(R.id.edittext_pn_po_qty);
        txtImportPoWithQty.setSelectAllOnFocus(true);
        txtImportPoWithQty.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down " + v.getId());
                    EditText et = (EditText) v;

                    if (TextUtils.isEmpty(et.getText())) {
                        return false;
                    } else {
                        insertPoWithQty();
                        return true;
                    }
                }

                return false;
            }
        });

        btnInputPn = findViewById(R.id.button_input_pn);
        btnInputPn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtImportPoWithQty.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.plz_enter_po_num_and_qty), Toast.LENGTH_SHORT).show();
                    txtImportPoWithQty.requestFocus();
                } else {
                    insertPoWithQty();
                }
            }
        });

        lvPn = findViewById(R.id.listViewPn);
        lvPn.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            // Yes button clicked
                            mAdapterPn.remove(poWithQtyListPn.get(selectedIndex));
                            setListSummary(poWithQtyListPn, labelListSummary);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(PrintMaterialLabelActivity.this);
                builder.setTitle( getString(R.string.btn_ok)).setMessage(getString(R.string.is_del_data))
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show();
                return true;
            }
        });

        mAdapterPn = new ArrayAdapter<>(this, R.layout.simple_list_item, poWithQtyListPn);
        lvPn.setAdapter(mAdapterPn);

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
                //onCreateChoosePrinterDialog().show();
                if (checkFields())
                    doQueryPN();
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

        lblTitle = findViewById(R.id.ap_title);
        setTitle();

        rbCodesoft.setChecked(true);
        rbPartNo.setChecked(true);
        print = new PrintLabelHandler();
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

    private void doQueryPN() {
        pnInfo = new PnItemInfoHelper();
        pnInfo.setPartNo(getPartNo());
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetPNInfo().execute(0);
    }

    private void setListSummary(List<String> list, TextView tv) {
        int total = 0;

        for (String item : list) {
            total += Integer.parseInt(item.split(",")[1]);
        }

        tv.setText(getString(R.string.label_list_summary, list.size(), total));
    }

    private void insertPoWithQty() {
        if (rbReelID.isChecked()) {
            if (txtImportReelIdQty.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_orig_qty), Toast.LENGTH_SHORT).show();
                txtImportReelIdQty.requestFocus();
            } else if (txtImportReelIdPoWithQty.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_be_null), Toast.LENGTH_SHORT).show();
                txtImportReelIdPoWithQty.requestFocus();
            } else {
                String[] array = txtImportReelIdPoWithQty.getText().toString().trim().split(",");

                if (array.length == 2) {
                    int totalQty = Integer.valueOf(txtImportReelIdQty.getText().toString().trim());
                    String po = array[0].trim();
                    String qty = array[1].trim();
                    String key = po + "," + qty;

                    try {
                        if (Integer.valueOf(qty) < 1) {
                            txtImportPackQty.requestFocus();
                            Toast.makeText(getApplicationContext(), getString(R.string.num_has_greater_0), Toast.LENGTH_SHORT).show();
                            txtImportReelIdPoWithQty.requestFocus();
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        Toast.makeText(getApplicationContext(), getString(R.string.format_not_match), Toast.LENGTH_SHORT).show();
                        txtImportReelIdPoWithQty.requestFocus();
                        return;
                    }

                    int inputQty = Integer.valueOf(qty);
                    int tmpQty = 0;

                    for (String temp : poWithQtyListReelId) {
                        if (temp.split(",")[0].equals(po)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.repeat_po), Toast.LENGTH_SHORT).show();
                            txtImportReelIdPoWithQty.requestFocus();
                            return;
                        } else {
                            tmpQty += Integer.valueOf(temp.split(",")[1]);
                        }
                    }

                    if (inputQty > totalQty - tmpQty) {
                        Toast.makeText(getApplicationContext(), getString(R.string.enter_po_qty_greater_than_total), Toast.LENGTH_SHORT).show();
                        txtImportReelIdPoWithQty.requestFocus();
                        return;
                    }

                    poWithQtyListReelId.add(key);
                    mAdapterReelId.notifyDataSetChanged();
                    setListSummary(poWithQtyListReelId, labelListSummaryReelId);
                    Toast.makeText(getApplicationContext(), getString(R.string.po_num_has_inputed), Toast.LENGTH_SHORT).show();
                    txtImportReelIdPoWithQty.setText("");
                    txtImportReelIdPoWithQty.requestFocus();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.format_not_match), Toast.LENGTH_SHORT).show();
                    txtImportReelIdPoWithQty.requestFocus();
                }
            }
        } else if (rbPartNo.isChecked()) {
            if (txtImportQty.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_orig_qty), Toast.LENGTH_SHORT).show();
                txtImportQty.requestFocus();
            } else if (txtImportPoWithQty.getText().toString().trim().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_be_null), Toast.LENGTH_SHORT).show();
                txtImportPoWithQty.requestFocus();
            } else {
                String[] array = txtImportPoWithQty.getText().toString().trim().split(",");

                if (array.length == 2) {
                    int totalQty = Integer.valueOf(txtImportQty.getText().toString().trim());
                    String po = array[0].trim();
                    String qty = array[1].trim();
                    String key = po + "," + qty;

                    try {
                        if (Integer.valueOf(qty) < 1) {
                            txtImportPackQty.requestFocus();
                            Toast.makeText(getApplicationContext(), getString(R.string.num_has_greater_0), Toast.LENGTH_SHORT).show();
                            txtImportPoWithQty.requestFocus();
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        Toast.makeText(getApplicationContext(), getString(R.string.format_not_match), Toast.LENGTH_SHORT).show();
                        txtImportPoWithQty.requestFocus();
                        return;
                    }

                    int inputQty = Integer.valueOf(qty);
                    int tmpQty = 0;

                    for (String temp : poWithQtyListPn) {
                        if (temp.split(",")[0].equals(po)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.repeat_po), Toast.LENGTH_SHORT).show();
                            txtImportPoWithQty.requestFocus();
                            return;
                        } else {
                            tmpQty += Integer.valueOf(temp.split(",")[1]);
                        }
                    }

                    if (inputQty > totalQty - tmpQty) {
                        Toast.makeText(getApplicationContext(), getString(R.string.enter_po_qty_greater_than_total), Toast.LENGTH_SHORT).show();
                        txtImportPoWithQty.requestFocus();
                        return;
                    }

                    poWithQtyListPn.add(key);
                    mAdapterPn.notifyDataSetChanged();
                    setListSummary(poWithQtyListPn, labelListSummary);
                    Toast.makeText(getApplicationContext(), getString(R.string.po_num_has_inputed), Toast.LENGTH_SHORT).show();
                    txtImportPoWithQty.setText("");
                    txtImportPoWithQty.requestFocus();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.format_not_match), Toast.LENGTH_SHORT).show();
                    txtImportPoWithQty.requestFocus();
                }
            }
        }
    }

    private boolean checkFields() {
        if (rgPrinter.getCheckedRadioButtonId() == -1) {
            rbWifi.requestFocus();
            Toast.makeText(getApplicationContext(), getString(R.string.choose_printer), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (pnLayout.getVisibility() == View.VISIBLE) {
            orgPrint="";
            txtImportPartNo.setText(txtImportPartNo.getText().toString().trim());
            txtImportQty.setText(txtImportQty.getText().toString().trim());
            txtImportDC.setText(txtImportDC.getText().toString().trim());
            txtImportVC.setText(txtImportVC.getText().toString().trim());
            txtImportPackQty.setText(txtImportPackQty.getText().toString().trim());
            txtImportCOO.setText(txtImportCOO.getText().toString().trim());
            txtImportPoWithQty.setText(txtImportPoWithQty.getText().toString().trim());

            if (TextUtils.isEmpty(txtImportPartNo.getText())) {
                txtImportPartNo.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_sku), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (txtImportPartNo.getText().length() != 12) {
                txtImportPartNo.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.sku_len_must_be_12), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportQty.getText())) {
                txtImportQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.input_printer_qty), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (Integer.parseInt(txtImportQty.getText().toString()) < 1) {
                txtImportQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.num_has_greater_0), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportDC.getText())) {
                txtImportDC.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_dc), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (txtImportDC.getText().length() != 10) {
                txtImportDC.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.dc_len_greater_10), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (!Util.isDateCodeValid(getDateCode())) {
                txtImportDC.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.dc_incorrect), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportVC.getText())) {
                txtImportVC.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_vc), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (txtImportVC.getText().length() != 6) {
                txtImportVC.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.vc_len_must_be_six), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportPackQty.getText())) {
                txtImportPackQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_packing_base), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (Integer.parseInt(txtImportPackQty.getText().toString()) < 1) {
                txtImportPackQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.num_has_greater_0), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportCOO.getText())) {
                txtImportCOO.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_coo), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (poWithQtyListPn.isEmpty()) {
//                txtImportPoWithQty.requestFocus();
//                Toast.makeText(getApplicationContext(), "請輸入PO及數量", Toast.LENGTH_SHORT)
//                        .show();
//                return false;
            } else {
                int totalQty = Integer.parseInt(txtImportQty.getText().toString());
                int currentQty = 0;

                for (String item : poWithQtyListPn) {
                    currentQty += Integer.parseInt(item.split(",")[1]);
                }

                if (totalQty != currentQty) {
                    txtImportPoWithQty.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.Qty_in_PO_list_does_not_match_total_qty), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            orgPrint = "";
            txtImportReelId.setText(txtImportReelId.getText().toString().trim());
            txtImportReelIdQty.setText(txtImportReelIdQty.getText().toString().trim());
            txtImportReelIdPackQty.setText(txtImportReelIdPackQty.getText().toString().trim());
            txtImportReelIdCOO.setText(txtImportReelIdCOO.getText().toString().trim());
            txtImportReelIdPoWithQty.setText(txtImportReelIdPoWithQty.getText().toString().trim());

            if (txtImportReelId.getText().length() != 28 && txtImportReelId.getText().length() != 34) {
                txtImportReelId.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.reelid_len_must_be_28_24), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (!Util.isDateCodeValid(getDateCode())) {
                txtImportReelId.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.dc_incorrect), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportReelIdQty.getText())) {
                txtImportReelIdQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.input_printer_qty), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (Integer.parseInt(txtImportReelIdQty.getText().toString()) < 1) {
                txtImportReelIdQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.num_has_greater_0), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportReelIdPackQty.getText())) {
                txtImportReelIdPackQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_packing_base), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (Integer.parseInt(txtImportReelIdPackQty.getText().toString()) < 1) {
                txtImportReelIdPackQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.num_has_greater_0), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportReelIdCOO.getText())) {
                txtImportReelIdCOO.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_coo), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (poWithQtyListReelId.isEmpty()) {
//                txtImportReelIdPoWithQty.requestFocus();
//                Toast.makeText(getApplicationContext(), "請輸入PO及數量", Toast.LENGTH_SHORT)
//                        .show();
//                return false;
            } else {
                int totalQty = Integer.parseInt(txtImportReelIdQty.getText().toString());
                int currentQty = 0;
                for (String item : poWithQtyListReelId) {
                    currentQty += Integer.parseInt(item.split(",")[1]);
                }

                if (totalQty != currentQty) {
                    txtImportReelIdPoWithQty.requestFocus();
                    Toast.makeText(getApplicationContext(), getString(R.string.Qty_in_PO_list_does_not_match_total_qty), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        } else {
            txtImportQrCode.setText(txtImportQrCode.getText().toString().trim());
            if (TextUtils.isEmpty(txtImportQrCode.getText())) {
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_qrcode), Toast.LENGTH_SHORT).show();
                return false;
            }

            String reelId = QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID);
            if (reelId.length() != 34) {
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.reelid_incorrect_len_must_be_34), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (!Util.isDateCodeValid(getDateCode())) {
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.dc_incorrect), Toast.LENGTH_SHORT).show();
                return false;
            }

            String qty = QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.QTY);
            if (TextUtils.isEmpty(qty)) {
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.qr_qty_is_not_null), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (Integer.parseInt(qty) < 1) {
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.num_has_greater_0), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (TextUtils.isEmpty(txtImportQrCodePackQty.getText())) {
                txtImportQrCodePackQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.enter_packing_base), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (Integer.parseInt(txtImportQrCodePackQty.getText().toString()) < 1) {
                txtImportQrCodePackQty.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.num_has_greater_0), Toast.LENGTH_SHORT).show();
                return false;
            }

            orgPrint = QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.ORG);
            try{
                int num=Integer.parseInt(orgPrint);
            }catch (NumberFormatException e){
                txtImportQrCode.requestFocus();
                Toast.makeText(getApplicationContext(), getString(R.string.org_must_input_num), Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    private void cleanData() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            txtImportPartNo.setText("");
            txtImportQty.setText("");
            txtImportDC.setText("");
            txtImportVC.setText("");
            txtImportPackQty.setText("");
            txtImportPoWithQty.setText("");
            txtImportCOO.setText(""); //20260108 Ann Add
            mAdapterPn.clear();
            labelListSummary.setText(getString(R.string.label_list_summary, 0, 0));
            txtImportPartNo.requestFocus();
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            txtImportReelId.setText("");
            txtImportReelIdQty.setText("");
            txtImportReelIdPackQty.setText("");
            txtImportReelIdPoWithQty.setText("");
            txtImportReelIdCOO.setText(""); //20260108 Ann Add
            mAdapterReelId.clear();
            labelListSummaryReelId.setText(getString(R.string.label_list_summary, 0, 0));
            txtImportReelId.requestFocus();
        } else {
            txtImportQrCode.setText("");
            txtImportQrCodePackQty.setText("");
            txtImportQrCode.requestFocus();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtImportReelId.getWindowToken(), 0);
    }

    private Dialog onCreateChoosePrinterDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer, null);
        final EditText etIP = item.findViewById(R.id.editTextIP);
        String ip = Preferences.getSharedPreferences(this).getString(
                Preferences.MATERIAL_PRINTER_IP, AppController.getProperties("MaterialPrinterIP"));
        etIP.setText(ip);
        final EditText etPort = item.findViewById(R.id.editTextPort);
        String port = Preferences.getSharedPreferences(this).getString(
                Preferences.MATERIAL_PRINTER_PORT, AppController.getProperties("MaterialPrinterPort"));
        etPort.setText(port);
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.MATERIAL_PRINTER_QTY, AppController.getProperties("MaterialPrinterQty"));
        etQty.setText(qty);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
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

                        if (TextUtils.isEmpty(etQty.getText().toString())) {
                            Toast.makeText(getApplicationContext(), R.string.input_printer_qty, Toast.LENGTH_LONG).show();
                            etQty.requestFocus();
                            return;
                        }

                        if (savePrinterInfo(etIP.getText().toString().trim(), etPort.getText().toString().trim(), etQty.getText().toString().trim())) {
                            alertDialog.dismiss();
                            dialog = ProgressDialog.show(PrintMaterialLabelActivity.this,
                                    getString(R.string.holdon), getString(R.string.printingLabel), true);

                            if (!printOpen(etIP.getText().toString().trim(), etPort.getText().toString().trim())) {
                                Toast.makeText(getApplicationContext(), getString(R.string.cant_conect_printer), Toast.LENGTH_LONG).show();
                                view.setEnabled(true);
                                dialog.dismiss();
                                return;
                            }

                            int printTimes = Integer.parseInt(etQty.getText().toString());
                            for (int j = 0; j < printTimes; j++) {
                                //printTest();
                                int importQty = Integer.parseInt(getQty());
                                int packQty = Integer.parseInt(getPackQty());
                                int times = (int) Math.ceil((double) importQty / packQty);
                                int qty;

                                for (int i = 0; i < times; i++) {
                                    if (importQty > packQty) {
                                        importQty -= packQty;
                                        qty = packQty;
                                    } else {
                                        qty = importQty;
                                    }

                                    String reelId = mlInfo.getReelIds()[i];
                                    String qrCode = reelId + "@" + getPoWithQtyList() + "@" + qty;
                                    String coo = getCOO(); //20260108 Ann Add

                                    if(Constant.ISORG){
                                        if(!TextUtils.isEmpty(orgPrint)){
                                            if(coo.isEmpty()){ //20260108 Ann Add:coo
                                                qrCode = reelId + "@" + getPoWithQtyList() + "@" + qty + "@" + orgPrint;
                                            }else{
                                                qrCode = reelId + "@" + getPoWithQtyList() + "@" + qty + "@" + orgPrint + "@@" + coo;
                                            }
                                        }else{
                                            if(coo.isEmpty()){ //20260108 Ann Add:coo
                                                qrCode = reelId + "@" + getPoWithQtyList() + "@" + qty + "@" + AppController.getOrg();
                                            }else{
                                                qrCode = reelId + "@" + getPoWithQtyList() + "@" + qty + "@" + AppController.getOrg() + "@@" + coo;
                                            }
                                        }
                                    }

                                    if (!printLabel(reelId, getPartNo(), pnInfo.getPartDescription(), String.valueOf(qty), getDateCode(), getVendorCode(), getPo(), qrCode, coo)) {
                                        printClose();
                                        Toast.makeText(getApplicationContext(), getString(R.string.print_error), Toast.LENGTH_LONG).show();
                                        dialog.dismiss();
                                        return;
                                    }
                                }
                            }

                            printClose();
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private Dialog onCreateChoosePrinterCodesoftDialog() {
        final View item = LayoutInflater.from(this).inflate(R.layout.dialog_printer_new, null);
        final Spinner printerIP = item.findViewById(R.id.printspinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,    //對應的Context
                R.array.printersIP,                             //資料選項內容
                android.R.layout.simple_spinner_item);  //預設Spinner未展開時的View(預設及選取後樣式)
        printerIP.setAdapter(adapter);
        printerIP.setSelection(adapter.getPosition("10.0.191.219"));
        final EditText etQty = item.findViewById(R.id.editTextQty);
        String qty = Preferences.getSharedPreferences(this).getString(
                Preferences.MATERIAL_PRINTER_QTY, AppController.getProperties("MaterialPrinterQty"));
        etQty.setText(qty);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
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
                        dialog = ProgressDialog.show(PrintMaterialLabelActivity.this,
                                getString(R.string.holdon), getString(R.string.printingLabel), true);

                        int printTimes = Integer.parseInt(etQty.getText().toString());
                        for (int j = 0; j < printTimes; j++) {
                            int importQty = Integer.parseInt(getQty());
                            int packQty = Integer.parseInt(getPackQty());
                            int times = (int) Math.ceil((double) importQty / packQty);
                            int qty;

                            for (int i = 0; i < times; i++) {
                                if (importQty > packQty) {
                                    importQty -= packQty;
                                    qty = packQty;
                                } else {
                                    qty = importQty;
                                }

                                String reelId = mlInfo.getReelIds()[i];
                                String qrCode = reelId + "@" + getPoWithQtyList() + "@" + qty;
                                String coo = getCOO(); //20260108 Ann Add

                                if(!TextUtils.isEmpty(orgPrint)){
                                    if(coo.isEmpty()){ //20260108 Ann Add:coo
                                        qrCode = reelId + "@" + getPoWithQtyList() + "@" + qty + "@" + orgPrint;
                                    }else{
                                        qrCode = reelId + "@" + getPoWithQtyList() + "@" + qty + "@" + orgPrint + "@@" + coo;
                                    }
                                }else{
                                    if(coo.isEmpty()) { //20260108 Ann Add:coo
                                        qrCode = reelId + "@" + getPoWithQtyList() + "@" + qty + "@" + AppController.getOrg();
                                    }else{
                                        qrCode = reelId + "@" + getPoWithQtyList() + "@" + qty + "@" + AppController.getOrg() + "@@" + coo;
                                    }
                                }
                                /*if (!printLabel(reelId, getPartNo(), pnInfo.getPartDescription(), String.valueOf(qty), getDateCode(), getVendorCode(), getPo(), qrCode)) {
                                    printClose();
                                    Toast.makeText(getApplicationContext(), getString(R.string.print_error), Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                    return;
                                }*/

                                HashMap<String, String> printInfo=new HashMap<>();
                                NewPrintData PrintData= new NewPrintData();
                                printInfo.put("printname" ,printerIP.getSelectedItem().toString());
                                printInfo.put("count" ,etQty.getText().toString().trim());//print 4 piece
                                printInfo.put("label",AppController.getProperties("MerakiLabelName"));
//                                PrintData.setPrinter(printInfo);
                                HashMap<String, String> var=new HashMap<>();
                                var.put("Description",pnInfo.getPartDescription());
                                var.put("ReelID",reelId);
                                var.put("PN",getPartNo());
                                var.put("Quantity",String.valueOf(qty));
                                var.put("DateCode",getDateCode());
                                var.put("VendorCode",getVendorCode());
                                var.put("Qrcode",qrCode);

                                if(!TextUtils.isEmpty(orgPrint)){
                                    var.put("Org",AppController.getOrgName(Integer.parseInt(orgPrint)));
                                    var.put("OrgID",orgPrint);
                                }else{
                                    var.put("Org",AppController.getOrgName());
                                    var.put("OrgID",String.valueOf(AppController.getOrg()));
                                }

                                if (!TextUtils.isEmpty(getPo()) && !getPo().toUpperCase().equals("NULL")) {
                                    var.put("PONo","PO NO:");
                                    var.put("Po",getPo());
                                }else{
                                    var.put("PONo","");
                                    var.put("Po","");
                                }

                                PrintData.setVariables(var);
                                PrintDataList.add(PrintData);
                            }
                        }

//                        new PrintWithNewType().execute(0);
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        return alertDialog;
    }

    public String getReelId() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportPartNo.getText().toString().trim() + txtImportVC.getText().toString().trim() + txtImportDC.getText().toString().trim();
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            return txtImportReelId.getText().toString().trim();
        } else {
            return QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.REEL_ID);
        }
    }

    public String getPartNo() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportPartNo.getText().toString().trim();
        } else {
            return getReelId().substring(0, 12);
        }
    }

    public String getDateCode() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportDC.getText().toString().trim();
        } else {
            return getReelId().substring(18, 28);
        }
    }

    public String getPoWithQtyList() {
        StringBuilder sb = new StringBuilder();

        if (pnLayout.getVisibility() == View.VISIBLE) {
            for (String item : poWithQtyListPn) {
                sb.append(item);
                sb.append("@");
            }

            return sb.toString().trim().replace("@", ",");
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            for (String item : poWithQtyListReelId) {
                sb.append(item);
                sb.append(";");
            }

            return sb.toString().trim().replace(";", ",");
        } else {
            return QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.PO_QTY_LIST);
        }
    }

    //20260108 Ann Add
    public String getCOO(){
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportCOO.getText().toString().trim();
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            return txtImportReelIdCOO.getText().toString().trim();
        } else {
            return QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.COO);
        }
    }

    public String getQty() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportQty.getText().toString().trim();
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            return txtImportReelIdQty.getText().toString().trim();
        } else {
            return QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.QTY);
        }
    }

    public int getLabelCount() {
        int importQty = Integer.parseInt(getQty());
        int packQty = Integer.parseInt(getPackQty());
        return (int) Math.ceil((double) importQty / packQty);
    }

    public String getPo() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            if (poWithQtyListPn.isEmpty()) {
                return "";
            } else {
                return poWithQtyListPn.get(0).split(",")[0];
            }
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            if (poWithQtyListReelId.isEmpty()) {
                return "";
            } else {
                return poWithQtyListReelId.get(0).split(",")[0];
            }
        } else {
            return QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.PO_QTY_LIST).split(",")[0];
        }
    }

    public String getPackQty() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportPackQty.getText().toString().trim();
        } else if (reelIdLayout.getVisibility() == View.VISIBLE) {
            return txtImportReelIdPackQty.getText().toString().trim();
        } else {
            return txtImportQrCodePackQty.getText().toString().trim();
        }
    }

    public String getVendorCode() {
        if (pnLayout.getVisibility() == View.VISIBLE) {
            return txtImportVC.getText().toString().trim();
        } else {
            return getReelId().substring(12, 18);
        }
    }

    private boolean printOpen(String ip, String port) {
        try {
            if (TscEthernetDll == null) {
                TscEthernetDll = new TscWifiActivity();
            }

            TscEthernetDll.openport(ip, Integer.parseInt(port));
            TscEthernetDll.setup(70, 50, 3, 10, 0, 3, 0);
            return true;
        } catch (Exception ex) {
            //Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void printClose() {
        try {
            TscEthernetDll.closeport(5000);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean printLabel(String PN, String Description, String Qty, String DC, String VC) {
        try {
            TscEthernetDll.clearbuffer();
            TscEthernetDll.sendcommand("SET TEAR ON\n");
            TscEthernetDll.sendcommand("CODEPAGE UTF-8\n");
            Description = "Description:  " + Description;
            byte[] des = null;

            if (CharUtil.isChineseOrGreek(Description)) {
                des = Description.getBytes("Big5");
            }

            int x, y;
            x = mm2dot(1);
            y = mm2dot(4);
            TscEthernetDll.printerfont(x, y, "0", 0, 10, 10, "Reel ID:");
            x = mm2dot(20);
            y = mm2dot(2);
            TscEthernetDll.barcode(x, y, "128", 55, 0, 0, 2, 5, PN + VC + DC);
            x = mm2dot(20);
            y = mm2dot(7);
            TscEthernetDll.printerfont(x, y, "0", 0, 9, 9, PN + VC + DC);
            x = mm2dot(1);
            y = mm2dot(13);
            TscEthernetDll.printerfont(x, y, "0", 0, 10, 10, "P/N:");
            x = mm2dot(20);
            y = mm2dot(11);
            TscEthernetDll.barcode(x, y, "128", 55, 0, 0, 2, 5, PN);
            x = mm2dot(20);
            y = mm2dot(16);
            TscEthernetDll.printerfont(x, y, "0", 0, 9, 9, PN);
            x = mm2dot(1);
            y = mm2dot(20);
            if (des == null) {
                TscEthernetDll.printerfont(x, y, "0", 0, 9, 9, Description);
            } else {
                TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
                TscEthernetDll.sendcommand(des);
                TscEthernetDll.sendcommand("\"\n");
            }
            y = mm2dot(26);
            TscEthernetDll.printerfont(x, y, "0", 0, 10, 10, "Quantity:");
            x = mm2dot(20);
            y = mm2dot(24);
            TscEthernetDll.barcode(x, y, "128", 55, 0, 0, 2, 5, Qty);
            x = mm2dot(20);
            y = mm2dot(29);
            TscEthernetDll.printerfont(x, y, "0", 0, 9, 9, Qty);
            x = mm2dot(1);
            y = mm2dot(35);
            TscEthernetDll.printerfont(x, y, "0", 0, 10, 10, "Date Code:");
            x = mm2dot(20);
            y = mm2dot(33);
            TscEthernetDll.barcode(x, y, "128", 55, 0, 0, 2, 5, DC);
            x = mm2dot(20);
            y = mm2dot(38);
            TscEthernetDll.printerfont(x, y, "0", 0, 9, 9, DC);
            x = mm2dot(1);
            y = mm2dot(44);
            TscEthernetDll.printerfont(x, y, "0", 0, 10, 10, "Vendor Code:");
            x = mm2dot(20);
            y = mm2dot(42);
            TscEthernetDll.barcode(x, y, "128", 55, 0, 0, 2, 5, VC);
            x = mm2dot(20);
            y = mm2dot(47);
            TscEthernetDll.printerfont(x, y, "0", 0, 9, 9, VC);
            // 前五個字元任一字元為英文字母時，列印無鉛圖形
            if (!TextUtils.isEmpty(PN)
                    && PN.length() > 4
                    && Pattern.compile("[a-zA-Z]").matcher(PN.substring(0, 5))
                    .find()) {
                x = mm2dot(59);
                y = mm2dot(40);
                TscEthernetDll.sendcommand("PUTBMP " + x + "," + y + ",\"rohs.BMP\"\n");
            }
            TscEthernetDll.printlabel(1, 1);
            return true;
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void printTest() {
        errorInfo = "";

        String reelId = "72E0348004001111201922111120000100";
        String partNo = "72E034800400";
        String description = "R384RF LFP";
        String qty = "999";
        String dateCode = "1922111120";
        String vendorCode = "111120";
        String po = "999999-99";
        String coo = "TWN";
        //String poWithQty = "123456-11,100000,123456-12,200000,123456-13,300000,123456-14,400000,123456-15,500000,123456-16,600000,123456-17,700000";
        String poWithQty = "123456-11,1000,123456-12,2000,123456-13,3000,123456-14,4000,123456-15,5000,123456-16,6000,123456-17,7000";
        String qrCode = reelId + "@" + poWithQty + "@" + qty;

        if (!printLabel(reelId, partNo, description, qty, dateCode, vendorCode, po, qrCode, coo)) {
            errorInfo = getString(R.string.printLabalFailed);
            mConnection.setText(getString(R.string.printer_connect_error));
            mConnection.setTextColor(Color.RED);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }
    }

    //20260108 Ann Add:COO
    private boolean printLabel(String reelId, String partNo, String Description, String qty,
                               String dateCode, String vendorCode, String po, String qrCode, String coo) {
        try {
            TscEthernetDll.clearbuffer();
            TscEthernetDll.sendcommand("SET TEAR ON\n");
            TscEthernetDll.sendcommand("CODEPAGE UTF-8\n");
            Description = "Description: " + Description;
            byte[] des = null;
            AppController.debug("CharUtil.isChineseOrGreek(Description)= "  + CharUtil.isChineseOrGreek(Description));

            if (CharUtil.isChineseOrGreek(Description)) {
                //des = new byte[1024];
                des = Description.getBytes("Big5");
            }

            int x, y;
            x = mm2dot(1);
            y = mm2dot(3);
            TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "Reel ID:");
            x = mm2dot(9);
            y = mm2dot(2);
            TscEthernetDll.barcode(x, y, "128", 30, 1, 0, 2, 2, reelId);
            x = mm2dot(1);
            y = mm2dot(10);
            TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "P/N:");
            x = mm2dot(8);
            y = mm2dot(8);
            TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 2, partNo);
            x = mm2dot(8);
            y = mm2dot(11);
            TscEthernetDll.printerfont(x, y, "0", 0, 12, 12, partNo);
            x = mm2dot(1);
            y = mm2dot(16);
            if (des == null) {
                TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, Description);
            } else {
                TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"FONT001\",0,2,2,\"");
                TscEthernetDll.sendcommand(des);
                TscEthernetDll.sendcommand("\"\n");
            }
            y = mm2dot(22);
            TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "Quantity:");
            x = mm2dot(13);
            y = mm2dot(20);
            TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 2, qty);
            x = mm2dot(13);
            y = mm2dot(23);
            TscEthernetDll.printerfont(x, y, "0", 0, 12, 12, qty);
            x = mm2dot(1);
            y = mm2dot(29);
            TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "Date Code:");
            x = mm2dot(13);
            y = mm2dot(27);
            TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 2, dateCode);
            x = mm2dot(13);
            y = mm2dot(30);
            TscEthernetDll.printerfont(x, y, "0", 0, 12, 12, dateCode);
            x = mm2dot(1);
            y = mm2dot(36);
            TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "Vendor Code:");
            x = mm2dot(14);
            y = mm2dot(34);
            TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 2, vendorCode);
            x = mm2dot(14);
            y = mm2dot(37);
            TscEthernetDll.printerfont(x, y, "0", 0, 12, 12, vendorCode);
            if (!TextUtils.isEmpty(po) && !po.toUpperCase().equals("NULL")) {
                x = mm2dot(1);
                y = mm2dot(43);
                TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "PO NO:");
                x = mm2dot(9);
                y = mm2dot(41);
                TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 2, po);
                x = mm2dot(9);
                y = mm2dot(44);
                TscEthernetDll.printerfont(x, y, "0", 0, 12, 12, po);
            }
            //x = mm2dot(36);
            //y = mm2dot(20);

            if(Constant.ISORG){//6606An7530011428572229142857000300@@10@ENT
                x = mm2dot(43);
                y = mm2dot(8);

                if(!TextUtils.isEmpty(orgPrint)){
                    TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"4\",0,2,2,\"" + AppController.getOrgName(Integer.parseInt(orgPrint)) + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(9);
                    TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 2, orgPrint);
                }else{
                    TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"4\",0,2,2,\"" + AppController.getOrgName() + "\"\n");
                    x = mm2dot(55);
                    y = mm2dot(9);
                    TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 2, AppController.getOrg()+"");
                }
                //TscEthernetDll.sendcommand("TEXT " + x + "," + y + ",\"5\",0,3,3,\"" + st1 + "\"\n");
            }

            //COO 20260108 Ann Add
            if(!coo.isEmpty()){
                x = mm2dot(40);
                y = mm2dot(20);
                TscEthernetDll.printerfont(x, y, "0", 0, 7, 7, "COO:");
                x = mm2dot(45);
                y = mm2dot(19);
                TscEthernetDll.barcode(x, y, "128", 30, 0, 0, 2, 2, coo);
                x = mm2dot(45);
                y = mm2dot(23);
                TscEthernetDll.printerfont(x, y, "0", 0, 12, 12, coo);
            }

            x = mm2dot(45);
            y = mm2dot(27); //20260108 Ann Edit:QRCode從23下移至27
            TscEthernetDll.qrcode(x, y, "M", "5", "A", "0", "M2", "S7", qrCode);
            TscEthernetDll.printlabel(1, 1);
            return true;
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            return false;
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

    private boolean savePrinterInfo(String ip, String port, String qty) {
        try {
            Preferences.setString(this, Preferences.MATERIAL_PRINTER_IP, ip);
            Preferences.setString(this, Preferences.MATERIAL_PRINTER_PORT, port);
            Preferences.setString(this, Preferences.MATERIAL_PRINTER_QTY, qty);
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

    private void returnPage() {
        finish();
    }

    public void onBackPressed() {
        // do something here and don't write super.onBackPressed()
        returnPage();
    }

    private void getReelIdList() {
        mlInfo = new MaterialLabelHelper();
        mlInfo.setPartNo(getPartNo());
        mlInfo.setVendorCode(getVendorCode());
        mlInfo.setDateCode(getDateCode());
        mlInfo.setLabelSize("S");
        mlInfo.setLabelCount(getLabelCount());
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        new GetReelIdList().execute(0);
    }

    private void printLabel() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.printingLabel), true);
        int importQty = Integer.parseInt(getQty());
        int packQty = Integer.parseInt(getPackQty());
        int times = (int) Math.ceil((double) importQty / packQty);
        int qty;

        for (int i = 0; i < times; i++) {
            if (importQty > packQty) {
                importQty -= packQty;
                qty = packQty;
            } else {
                qty = importQty;
            }

            String reelId = mlInfo.getReelIds()[i];
            String qrCode = reelId + "@" + getPoWithQtyList() + "@" + qty;
            String coo = getCOO(); //20260108 Ann Add

            if(Constant.ISORG){
                if(!TextUtils.isEmpty(orgPrint)){
                    if(coo.isEmpty()) { //20260108 Ann Add:coo
                        qrCode = reelId + "@" + getPoWithQtyList() + "@" + qty + "@" + orgPrint;
                    }else{
                        qrCode = reelId + "@" + getPoWithQtyList() + "@" + qty + "@" + orgPrint + "@@" + coo;
                    }
                }else{
                    if(coo.isEmpty()) { //20260108 Ann Add:coo
                        qrCode = reelId + "@" + getPoWithQtyList() + "@" + qty + "@" + AppController.getOrg();
                    }else{
                        qrCode = reelId + "@" + getPoWithQtyList() + "@" + qty + "@" + AppController.getOrg() + "@@" + coo;
                    }
                }
            }

            //if (!BtPrintLabel.printMaterialLabel2(reelId, getPartNo(), pnInfo.getPartDescription(), String.valueOf(qty), getDateCode(), getVendorCode(), getPo(), qrCode, orgPrint)) {
            if (!BtPrintLabel.printMaterialLabel2(reelId, getPartNo(), pnInfo.getPartDescription(), String.valueOf(qty), getDateCode(), getVendorCode(), getPo(), qrCode, orgPrint, coo)) { //20260108 Ann Add:coo
                Toast.makeText(getApplicationContext(), getString(R.string.print_error), Toast.LENGTH_LONG).show();
                dialog.dismiss();
                return;
            }
        }

        dialog.dismiss();
        Toast.makeText(getApplicationContext(), getString(R.string.Print_OK), Toast.LENGTH_LONG).show();
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
            AppController.debug("GetPNInfo onProgressUpdate() " + text[0]);
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
            AppController.debug("GetPNInfo onPostExecute() = " + new Gson().toJson(result));

            if (result != null) {
                pnInfo = result;
                if (pnInfo.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    getReelIdList();
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
                    orgPrint = QrCodeUtil.getValueFromItemLabelQrCode(txtImportQrCode.getText().toString(), ITEM_LABEL_QR_CODE_FORMAT.ORG);
                    AppController.debug("pnInfo.getPartDescription()= "  + pnInfo.getPartDescription());

                    if (rbWifi.isChecked()) {
                        onCreateChoosePrinterDialog().show();
                    } if (rbCodesoft.isChecked()) { //20260108 Ann Mark:沒看到有呼叫任何一台Codesoft列印
                        onCreateChoosePrinterCodesoftDialog().show();
                    } else {
                        printLabel();
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

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.label_print_material_label, AppController.getOrgName()));
        text.setSpan(new RelativeSizeSpan(1f), 0, AppController.getOrgName().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }
}
