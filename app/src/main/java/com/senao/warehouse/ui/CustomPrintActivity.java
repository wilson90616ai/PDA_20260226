package com.senao.warehouse.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.print.BtPrintLabel;

public class CustomPrintActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PRINTER_SETTING = 2;
    private static final int REQUEST_BLUETOOTH_SETTINGS = 3;
    private RadioButton rb6010, rb7050, rbBluetooth, rbWifi,rbCodesoft;
    private boolean isBluetoothPrintSet = false;
    private Button btnReturn, btnConfim, btnCancel;
    private EditText txt_data;
    int x, y;
    private RadioGroup sizedata;

    private final RadioGroup.OnCheckedChangeListener dcListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio_size6010:
                    x=60;
                    y=10;
                    break;
                case R.id.radio_size7050:
                    x=70;
                    y=50;
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_custom);

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

        sizedata=findViewById(R.id.radio_sizedata);
        sizedata.setOnCheckedChangeListener(dcListener);

        rbWifi = findViewById(R.id.radio_wifi);
        rbCodesoft = findViewById(R.id.radio_codesoft);

        txt_data = findViewById(R.id.edittext_dn_no);

        rb6010 = findViewById(R.id.radio_size6010);
        rb6010.requestFocus();

        rb7050 = findViewById(R.id.radio_size7050);

        btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Return", String.valueOf(v.getId()));
                returnPage();
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

        btnConfim = findViewById(R.id.button_confirm);
        btnConfim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Confim", String.valueOf(v.getId()));
                AppController.debug("btnConfim() x:"+x+",y="+y);
                //onCreateChoosePrinterDialog().show();

                if (!TextUtils.isEmpty(txt_data.getText().toString().trim())){
                    if (rbWifi.isChecked()) {
                        AppController.debug("rbWifi.isChecked()");
                        Toast.makeText(getApplicationContext(),getString(R.string.Wifi_not_yet_developed), Toast.LENGTH_SHORT).show();
                        //onCreateChoosePrinterDialog().show();
                    } else if (rbCodesoft.isChecked()) {
                        AppController.debug("rbCodesoft.isChecked()");
                        Toast.makeText(getApplicationContext(),getString(R.string.Codesoft_not_yet_developed), Toast.LENGTH_SHORT).show();
                        //onCreateChoosePrinterCodesoftDialog().show();
                    } else if (rbBluetooth.isChecked()){
                        AppController.debug("rbBluetooth.isChecked()");
                        if (!BtPrintLabel.printCustomPrint(txt_data.getText().toString().trim(),x,y)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.Label_printing_error_occurred), Toast.LENGTH_LONG).show();
                            //return;
                        }

                        //printLabel();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_data), Toast.LENGTH_SHORT).show();
                }
            }

        });
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

    private void returnPage() {
        finish();
    }

    private void cleanData() {
        txt_data.setText("");
    }
}
