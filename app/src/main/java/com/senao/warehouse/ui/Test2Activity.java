package com.senao.warehouse.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.senao.warehouse.R;

import java.util.ArrayList;
import java.util.List;

public class Test2Activity extends Activity {

    private static final String TAG = Test2Activity.class.getSimpleName();
    private EditText txtQrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);

        txtQrCode = findViewById(R.id.edittext_import_qrcode);

        txtQrCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                // If the event is a key-down event on the "enter" button
                if (event.getAction() == KeyEvent.ACTION_UP
                        && (event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER || event
                        .getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, String.valueOf(v.getId()));

                    if (txtQrCode.getText().toString().trim().equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.sn_is_not_null),
                                Toast.LENGTH_SHORT).show();

                    } else {


                        String sn = getSN(txtQrCode.getText().toString().trim());
                        if (TextUtils.isEmpty(sn)) {
                            Toast.makeText(getApplicationContext(), "QRCode無法解析", Toast.LENGTH_LONG).show();
                            return true;
                        } else {
                            Toast.makeText(getApplicationContext(), sn, Toast.LENGTH_LONG).show();
                        }

                    }

                    // return true;
                }
                return false;
            }
        });
    }

    private String getSN(String qrCode) {
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

        if (list.size() > 0)
            return list.get(0);
        else
            return null;
    }
}