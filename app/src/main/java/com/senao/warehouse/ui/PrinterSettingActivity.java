package com.senao.warehouse.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.senao.warehouse.R;
import com.senao.warehouse.print.BtPrintLabel;
import com.senao.warehouse.util.Preferences;

public class PrinterSettingActivity extends Activity implements OnClickListener {

    private static final int REQUEST_BLUETOOTH_SETTINGS = 3;
    private EditText txtPrinterName;
    private CheckBox chkEnableRotatePrint;
    private Button btnConfirmSetting, btnCancelSetting, btnPrtinerTest;
    private TextView statusbar;
    private String printerName = null;
    private boolean enableRotatePrint = false;
    private BtPrintLabel bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_setting);
        chkEnableRotatePrint =  findViewById(R.id.input_enter_enable_rotate);
        txtPrinterName =  findViewById(R.id.input_printer_name);
        btnConfirmSetting =  findViewById(R.id.button_confirm_printer_setting);
        btnCancelSetting =  findViewById(R.id.button_cancel_printer_setting);
        btnPrtinerTest =  findViewById(R.id.button_printer_test);
        statusbar =  findViewById(R.id.label_printer_status);
        // chkEnableRotatePrint
        // .setOnCheckedChangeListener(new
        // CompoundButton.OnCheckedChangeListener() {
        //
        // @Override
        // public void onCheckedChanged(CompoundButton buttonView,
        // boolean isChecked) {
        // Log.d("onClick Enable rotate printMaterialLabel", String.valueOf(isChecked));
        // }
        // });
        //
        setViewValue();
    }

    private void setViewValue() {
        printerName = Preferences.getSharedPreferences(this).getString(Preferences.PRINTER_NAME, null);
        Log.d("Debug", "Get Prtiner Name:" + printerName);

        if (printerName != null) {
            try {
                txtPrinterName.setText(printerName);
            } catch (Exception e) {
                Log.e("ERROR", e.toString());
            }
        }

        enableRotatePrint = Preferences.getSharedPreferences(this).getBoolean(Preferences.ROTATE_ENABLED, false);
        Log.d("Debug", "Get ROTATE_ENABLED:" + enableRotatePrint);

        if (enableRotatePrint) {
            chkEnableRotatePrint.setChecked(true);
        } else {
            chkEnableRotatePrint.setChecked(false);
        }

        statusbar.setText(printerName + ":" + enableRotatePrint);
        btnConfirmSetting.setOnClickListener(this);
        btnCancelSetting.setOnClickListener(this);
        btnPrtinerTest.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        Intent intent;

        switch (viewId) {
            case R.id.button_cancel_printer_setting:
                Log.d("Debug", "Click cancel button");
                intent = new Intent();
                intent.setClass(PrinterSettingActivity.this, LoginActivity.class);
                startActivity(intent);
                // select(txtFeederId);

                break;
            case R.id.button_confirm_printer_setting:
                Log.d("Debug", "Click confirm button ");

                if (txtPrinterName.getText().toString().equals("")) {
                    Toast.makeText(this, getString(R.string.name_of_printer_is_not_null), Toast.LENGTH_SHORT).show();

                    if (txtPrinterName.getText().toString().equals("")) {
                        txtPrinterName.requestFocus();
                    }
                } else {
                    if (savePrinterInfo()) {
//					intent = new Intent();
//					intent.setClass(PrinterSettingActivity.this,
//							LoginActivity.class);
//					startActivity(intent);
                        setResult(RESULT_OK);
                        finish();
                    }
                }

                break;
            case R.id.button_printer_test:
                Log.d("Debug", "Click printer test button");
                String printerName = txtPrinterName.getText().toString().trim();
                Log.d("Debug", "PrinterName=" + printerName + ":" + chkEnableRotatePrint.isChecked());

                if (printerName.equals("")) {
                    Toast.makeText(this, getString(R.string.name_of_printer_is_not_null), Toast.LENGTH_SHORT).show();
                    txtPrinterName.requestFocus();
                } else {
                    hideKeyboard();
                    PrintTest();
                }

                break;
        }
    }

    private boolean savePrinterInfo() {
        try {
            printerName = txtPrinterName.getText().toString();
            enableRotatePrint = chkEnableRotatePrint.isChecked();
            Preferences.setString(this, Preferences.PRINTER_NAME, printerName);
            Preferences.setBoolean(this, Preferences.ROTATE_ENABLED, enableRotatePrint);

            Log.d("savePrinterInfo: ", printerName + ":" + enableRotatePrint);

            BtPrintLabel.setPrinterInfo(printerName, enableRotatePrint);
            // statusbar.setText("ServerInfo saved");
            // serverInfo = host + ":" + port;

            // txtResponseArea.setText("Server IP:PORT saved");
            return true;
        } catch (Exception e) {
            // txtResponseArea
            // .setText("ERROR: Plese veriry Server IP:PORT setting");
            Log.e("ERROR", e.toString());
            statusbar.setText("ERROR occurs");
            return false;
        }
    }

    private void PrintTest() {
        printerName = txtPrinterName.getText().toString();
        enableRotatePrint = chkEnableRotatePrint.isChecked();

        if (bt == null) {
            bt = new BtPrintLabel();
        }

        statusbar.setText("");

        Toast.makeText(this, getString(R.string.label_printer_name)+":" + printerName, Toast.LENGTH_SHORT).show();

        if (!bt.discover(printerName)) {
            //Toast.makeText(this, "找不到藍芽印表機裝置:" + printerName,
            //        Toast.LENGTH_SHORT).show();
            //statusbar.setText("找不到藍芽印表機裝置:" + printerName);
            Intent intentOpenBluetoothSettings = new Intent();
            intentOpenBluetoothSettings.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivityForResult(intentOpenBluetoothSettings, REQUEST_BLUETOOTH_SETTINGS);
//            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_bt_printer) + ":" + printerName + "請設定", Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_bt_printer) + ":" + printerName, Toast.LENGTH_LONG).show();
        } else {
            statusbar.setText(getString(R.string.result_of_print) + ":" + bt.printTest(enableRotatePrint));
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtPrinterName.getWindowToken(), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BLUETOOTH_SETTINGS) {
            if (bt == null) {
                bt = new BtPrintLabel();
            }

            if (bt.discover(printerName)) {
                statusbar.setText(getString(R.string.result_of_print) + ":" + bt.printTest(enableRotatePrint));
            } else {
                statusbar.setText(getString(R.string.cant_find_bt_printer) + ":" + printerName);
            }
        }
    }
}
