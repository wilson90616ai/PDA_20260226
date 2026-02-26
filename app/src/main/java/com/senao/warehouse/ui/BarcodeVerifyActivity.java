package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.R;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.RmaSnInfoHelper;
import com.senao.warehouse.database.ShipmentPalletSnInfoHelper;
import com.senao.warehouse.handler.PrintLabelHandler;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BarcodeVerifyActivity extends Activity implements View.OnClickListener {
    public static final String LICENSE = "L5JSZNFX-J7UG3GHR-XSML2A6F-7RQCODEL-H5VGTTAE-SLDRJRYU-Y4KMOFGH-CTDUL5IR";
    private static final String TAG = BarcodeVerifyActivity.class.getSimpleName();
    private static final int RC_BARCODE_CAPTURE = 9001;
    private final String MERAKI_SN_PATTERN = "[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}";
    private String errorInfo = "";
    private TextView mConnection,lblTitle;
    private Button btnReturn;
    private Button btnConfim;
    private Button btnCancel;
    private List<ShipmentPalletSnInfoHelper> infoList = new ArrayList<ShipmentPalletSnInfoHelper>();
    private ShipmentPalletSnInfoHelper palletSnInfoHelper;
    private EditText txtInputSn, txtSnQrCode;
    private ArrayAdapter<String> mAdapter;
    private ListView listView;
    private List<String> stringList = new ArrayList<String>();
    private int selectedIndex;
    private ProgressDialog dialog;
    private PrintLabelHandler print;
    private RmaSnInfoHelper rmaSnInfoHelper;
    private Switch switchLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_verify);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(BarcodeVerifyActivity.this);
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

        txtInputSn = findViewById(R.id.input_sn_no);
        txtInputSn.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s))
                    resetStatus();
            }
        });

        txtInputSn.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    //Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down" + String.valueOf(v.getId()));

                    if (txtInputSn.getText().toString().trim().equals("")) {
                        Toast.makeText(BarcodeVerifyActivity.this, getString(R.string.enter_sn), Toast.LENGTH_SHORT).show();
                        txtInputSn.requestFocus();
                    } else {
                        txtInputSn.setText(txtInputSn.getText().toString().trim());
                        txtSnQrCode.requestFocus();
                        hideKeyboard();
                    }

                    return true;
                }

                return false;
            }
        });

        switchLight = findViewById(R.id.switchLight);

        txtSnQrCode = findViewById(R.id.edittext_import_sn_qrcode);
        txtSnQrCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    //Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down" + String.valueOf(v.getId()));

                    if (txtSnQrCode.getText().toString().trim().equals("")) {
                        Toast.makeText(BarcodeVerifyActivity.this, getString(R.string.enter_barcode), Toast.LENGTH_SHORT).show();
                        txtSnQrCode.requestFocus();
                    } else {
                        if (parsePDF417Content(txtSnQrCode.getText().toString().trim())) {
                            mConnection.setText(R.string.barcode_success);
                            mConnection.setTextColor(Color.WHITE);
                            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                            Log.d(TAG, "Barcode read: " + txtSnQrCode.getText().toString().trim());
                        } else {
                            mConnection.setText(R.string.barcode_error);
                            mConnection.setTextColor(Color.RED);
                            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                        }

                        txtSnQrCode.setText("");
                        txtSnQrCode.requestFocus();
                    }

                    hideKeyboard();
                    return true;
                }

                return false;
            }
        });

        btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        btnConfim = findViewById(R.id.button_confirm);
        btnConfim.setOnClickListener(this);

        btnCancel = findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(this);

        listView = findViewById(R.id.list_item);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onCreateSnDialog(infoList.get(position)).show();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            infoList.remove(selectedIndex);
                            mAdapter.remove(stringList.get(selectedIndex));
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedIndex = position;
                AlertDialog.Builder builder = new AlertDialog.Builder(BarcodeVerifyActivity.this);
                builder.setTitle( getString(R.string.btn_ok)).setMessage(getString(R.string.is_del_data))
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show();
                return true;
            }
        });

        lblTitle = findViewById(R.id.ap_title);
        setTitle();
        mAdapter = new ArrayAdapter<String>(this, R.layout.simple_list_item, stringList);
        listView.setAdapter(mAdapter);
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.label_barcode_verify1, AppController.getOrgName()));
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private boolean parsePDF417Content(String content) {
        if (TextUtils.isEmpty(content))
            return false;

        String[] ary = content.split(";");
        ShipmentPalletSnInfoHelper item = new ShipmentPalletSnInfoHelper();
        List<String> snList = new ArrayList<String>();

        for (int i = 0; i < ary.length; i++) {
            if (i == 0) {
                item.setModelName(ary[i]);
            } else if (i == 1) {
                try {
                    item.setQty(Integer.parseInt(ary[i]));

                    if (item.getQty() != (ary.length - 2)) {
                        errorInfo = getString(R.string.barcode_qty) + ary[i] + getString(R.string.sn_qty_incorrect);
                        return false;
                    }
                } catch (NumberFormatException e) {
                    Log.d("Debug", "Exception:" + e.toString());
                    errorInfo = getString(R.string.barcode_qty) + ary[i] + getString(R.string.format_incorrect);
                    return false;
                }
            } else {
                if (ary[i].matches(MERAKI_SN_PATTERN)) {
                    if (snList.contains(ary[i])) {
                        errorInfo = getString(R.string.barcode_sn) + ary[i] + getString(R.string.repeat);
                        return false;
                    } else if (existsInOtherLabels(ary[i])) {
                        errorInfo = getString(R.string.sn_name) + ary[i] + getString(R.string.already_exist_other_swiped_barcode);
                        return false;
                    } else {
                        snList.add(ary[i]);
                    }
                } else {
                    errorInfo = getString(R.string.barcode_sn) + ary[i] + getString(R.string.format_incorrect);
                    return false;
                }
            }
        }

        item.setSnInfo(snList);
        infoList.add(item);
        mAdapter.add(content);
        mAdapter.notifyDataSetChanged();
        return true;
    }

    private boolean parsePDF417Content2(String content) {
        if (TextUtils.isEmpty(content))
            return false;

        String[] ary = content.split(";");
        ShipmentPalletSnInfoHelper item = new ShipmentPalletSnInfoHelper();
        List<String> snList = new ArrayList<String>();

        for (int i = 0; i < ary.length; i++) {
            if (i == 0) {
                item.setModelName(ary[i]);
            } else if (i == 1) {
                try {
                    item.setQty(Integer.parseInt(ary[i]));
                } catch (NumberFormatException e) {
                    Log.d("Debug", "Exception:" + e.toString());
                    return false;
                }
            } else {
                if (snList.contains(ary[i])) {
                    errorInfo = getString(R.string.barcode_sn) + ary[i] + getString(R.string.repeat);
                    return false;
                } else if (existsInOtherLabels(ary[i])) {
                    errorInfo = getString(R.string.sn_name) + ary[i] + getString(R.string.already_exist_other_swiped_barcode);
                    return false;
                } else {
                    snList.add(ary[i]);
                }
            }
        }

        item.setSnInfo(snList);
        infoList.add(item);
        mAdapter.add(content);
        mAdapter.notifyDataSetChanged();
        return true;
    }

    private boolean existsInOtherLabels(String sn) {
        if (infoList == null || infoList.isEmpty())
            return false;

        for (ShipmentPalletSnInfoHelper item : infoList) {
            if (item.getSnInfo() != null && item.getSnInfo().length > 0) {
                for (String info : item.getSnInfo()) {
                    if (info.equals(sn))
                        return true;
                }
            }
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button_return) {
            Log.d("onClick Return", String.valueOf(v.getId()));
            returnPage();
        } else if (id == R.id.button_confirm) {
            if (checkData() && existInBarcode(txtInputSn.getText().toString().trim()))
                //doConfirmLabel();
                doConfirmRmaLabel();
        } else if (id == R.id.button_cancel) {
            clearContent();
        }
    }

    private void clearContent() {
        infoList.clear();
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
        txtInputSn.setText("");
        txtInputSn.requestFocus();
        resetStatus();
    }

    private Dialog onCreateSnDialog(ShipmentPalletSnInfoHelper item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        //Inflate and set the layout for the dialog
        //Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.serial_number_list, null);
        builder.setView(view).setTitle(R.string.sn_barcode)
                //Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //sign in the user ...
                    }
                });

        TextView modelName = view.findViewById(R.id.textViewModelName);
        modelName.setText(getString(R.string.model_name, item.getModelName()));
        TextView modelQty = view.findViewById(R.id.textViewModelQty);
        modelQty.setText(getString(R.string.model_quantity, String.valueOf(item.getQty())));
        ListView tempListView = view.findViewById(R.id.listView);
        ArrayAdapter<String> tempAdapter;
        tempAdapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_b, item.getSnInfo());
        tempListView.setAdapter(tempAdapter);
        return builder.create();
    }

    private boolean checkSn() {
        if (TextUtils.isEmpty(txtInputSn.getText().toString().trim())) {
            txtInputSn.requestFocus();
            Toast.makeText(getApplicationContext(), getString(R.string.enter_sn), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private boolean checkData() {
        if (!checkSn()) {
            return false;
        }

        if (stringList == null || stringList.isEmpty()) {
            txtSnQrCode.requestFocus();
            Toast.makeText(getApplicationContext(), getString(R.string.read_pallet_pdf417_barcode), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void returnPage() {
        finish();
    }

    private void doConfirmLabel() {
        palletSnInfoHelper = new ShipmentPalletSnInfoHelper();
        palletSnInfoHelper.setSearchText(txtInputSn.getText().toString());
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        print = new PrintLabelHandler();
        new GetPalletSnInfo().execute(0);
    }

    private void doConfirmRmaLabel() {
        rmaSnInfoHelper = new RmaSnInfoHelper();
        rmaSnInfoHelper.setSn(txtInputSn.getText().toString());
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        print = new PrintLabelHandler();
        new GetRmaSnInfo().execute(0);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtInputSn.getWindowToken(), 0);
    }

    private boolean compareBarcode() {
        List<String> tempList = Arrays.asList(palletSnInfoHelper.getSnInfo());

        for (ShipmentPalletSnInfoHelper item : infoList) {
            for (String string : item.getSnInfo()) {
                if (!tempList.contains(string)) {
                    errorInfo = getString(R.string.barcode_2d_sn) + string + getString(R.string.not_exist_in_this_pallet_data);
                    return false;
                }
            }
        }

        for (String sn : tempList) {
            boolean exist = false;

            for (ShipmentPalletSnInfoHelper item : infoList) {
                for (String string : item.getSnInfo()) {
                    if (sn.equals(string)) {
                        exist = true;
                        break;
                    }
                }

                if (exist)
                    break;
            }

            if (!exist) {
                errorInfo = getString(R.string.sn_name) + sn + getString(R.string.not_exist_in_2d_barcode_missed_brush);
                return false;
            }
        }

        return true;
    }

    private boolean compareRmaBarcode() {
        if (rmaSnInfoHelper.getSnInfo() == null)
            return false;

        List<String> tempList = Arrays.asList(rmaSnInfoHelper.getSnInfo());

        for (ShipmentPalletSnInfoHelper item : infoList) {
            for (String string : item.getSnInfo()) {
                if (!tempList.contains(string)) {
                    errorInfo = getString(R.string.barcode_2d_sn) + string + getString(R.string.not_exist_in_this_pallet_data);
                    return false;
                }
            }
        }

        for (String sn : tempList) {
            boolean exist = false;

            for (ShipmentPalletSnInfoHelper item : infoList) {
                for (String string : item.getSnInfo()) {
                    if (sn.equals(string)) {
                        exist = true;
                        break;
                    }
                }

                if (exist)
                    break;
            }

            if (!exist) {
                errorInfo = getString(R.string.sn_name) + sn + getString(R.string.not_exist_in_2d_barcode_missed_brush);
                return false;
            }
        }

        return true;
    }

    private void resetStatus() {
        mConnection.setText("");
        mConnection.setTextColor(Color.WHITE);
        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        errorInfo = "";
    }

    private boolean existInBarcode(String sn) {
        for (ShipmentPalletSnInfoHelper item : infoList) {
            for (String string : item.getSnInfo()) {
                if (string.equals(sn))
                    return true;
            }
        }

        txtInputSn.requestFocus();
        mConnection.setText(R.string.barcode_verification_error);
        mConnection.setTextColor(Color.RED);
        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        errorInfo = getString(R.string.sn_name) + sn + getString(R.string.not_exist_in_2d_barcode);
        Toast.makeText(getApplicationContext(), getString(R.string.sn_name) + sn + getString(R.string.not_exist_in_2d_barcode), Toast.LENGTH_LONG).show();
        return false;
    }

    /*private boolean compareRmaBarcode() {
        List<String> tempList = Arrays.asList(rmaSnInfoHelper.getSnInfo());

        if (rmaSnInfoHelper.getType() == 1) {
            for (ShipmentPalletSnInfoHelper item : infoList) {
                for (String string : item.getSnInfo()) {
                    if (!tempList.contains(string)) {
                        errorInfo = "條碼序號" + string + "不存在此棧板資料中";
                        return false;
                    }
                }
            }
        } else {
            boolean exist;

            for (String sn : tempList) {
                exist = false;

                for (ShipmentPalletSnInfoHelper item : infoList) {
                    for (String string : item.getSnInfo()) {
                        if (string.equals(sn)) {
                            exist = true;
                            break;
                        }
                    }

                    if (exist) {
                        break;
                    }
                }

                if (!exist) {
                    errorInfo = "序號" + sn + "不存在此棧板條碼中";
                    return false;
                }
            }
        }

        return true;
    }*/

    private class GetPalletSnInfo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Get Pallet Serial Number Info from " + AppController.getServerInfo() + AppController.getProperties("GetPalletSnInfo"));
            publishProgress(getString(R.string.downloading_data));
            return print.getPalletSnInfo(palletSnInfoHelper);
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
                palletSnInfoHelper = (ShipmentPalletSnInfoHelper) result;

                if (result.getIntRetCode() == ReturnCode.OK) {
                    if (compareBarcode()) {
                        mConnection.setText(R.string.barcode_verification_ok);
                        mConnection.setTextColor(Color.WHITE);
                        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                        errorInfo = "";
                    } else {
                        mConnection.setText(R.string.barcode_verification_error);
                        mConnection.setTextColor(Color.RED);
                        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
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

    private class GetRmaSnInfo extends AsyncTask<Integer, String, BasicHelper> {
        @Override
        protected BasicHelper doInBackground(Integer... params) {
            AppController.debug("Get Rma Serial Number Info from " + AppController.getServerInfo() + AppController.getProperties("GetRmaSnInfo"));
            publishProgress(getString(R.string.downloading_data));
            return print.getRmaSnInfo(rmaSnInfoHelper);
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
                rmaSnInfoHelper = (RmaSnInfoHelper) result;

                if (result.getIntRetCode() == ReturnCode.OK) {
                    if (compareRmaBarcode()) {
                        mConnection.setText(R.string.barcode_verification_ok);
                        mConnection.setTextColor(Color.WHITE);
                        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                        errorInfo = "";
                    } else {
                        mConnection.setText(R.string.barcode_verification_error);
                        mConnection.setTextColor(Color.RED);
                        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    }
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                        errorInfo = result.getStrErrorBuf();
                        mConnection.setTextColor(Color.RED);
                        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    } else {
                        //mConnection.setText(getString(R.string.db_return_error));
                        doConfirmLabel();
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
}
