package com.senao.warehouse.ui;

import com.senao.warehouse.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.senao.warehouse.AppController;

public class ScannerSettingActivity extends Activity {

    private static final String TAG = ScannerSettingActivity.class.getSimpleName();
    private TextView mConnectionStatus, mConnection, mScoundSetting;
    private CheckBox chkEnterEnable;
    private Button btnConfirmSetting, btnCancelSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_setting);

        chkEnterEnable =  findViewById(R.id.input_enter_enable_sound);
        mScoundSetting =  findViewById(R.id.label_sound_setting);
        btnConfirmSetting =  findViewById(R.id.button_confirm_setting);
        btnCancelSetting =  findViewById(R.id.button_cancel_setting);

        chkEnterEnable
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        Log.d(TAG, "onClick Enter Enable " + String.valueOf(isChecked));
                    }
                });

        mScoundSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick Sound Setting " + String.valueOf(v.getId()));

				/*
                 * Intent intent = new Intent();
				 * intent.setClass(LoginActivity.this,
				 * ConnectionSettingActivity.class); startActivity(intent);
				 */
            }

        });

        btnConfirmSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick Confirm Setting " + String.valueOf(v.getId()));

//                Intent intent = new Intent();
//                if (AppController.getUser() != null) {
//                    intent.setClass(ScannerSettingActivity.this,
//                            MenuActivity.class);
//                } else {
//                    intent.setClass(ScannerSettingActivity.this,
//                            LoginActivity.class);
//                }
//                startActivity(intent);
                finish();
            }

        });

        btnCancelSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick Cancel Setting " + String.valueOf(v.getId()));

//                Intent intent = new Intent();
//                if (AppController.getUser() != null) {
//                    intent.setClass(ScannerSettingActivity.this,
//                            MenuActivity.class);
//                } else {
//                    intent.setClass(ScannerSettingActivity.this,
//                            LoginActivity.class);
//                }
//                startActivity(intent);
                finish();
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.scanner_setting, menu);
        return true;
    }

}
