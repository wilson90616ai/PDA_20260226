package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.senao.warehouse.R;

public class BakeActivity extends Activity {
    private TextView mConnection;
    private String errorInfo = "";
    private Button btnReturn, label_backstart, btnCancel,label_complete,label_reprint,label_inquire;
    private EditText txtImportReelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bake);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(BakeActivity.this);
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

        btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Return", String.valueOf(v.getId()));
                returnPage();
            }

        });

        label_backstart = findViewById(R.id.label_backstart);
        label_backstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(BakeActivity.this, BakeStartActivity.class);
                startActivity(intent);
            }
        });

        label_complete = findViewById(R.id.label_complete);
        label_complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(BakeActivity.this, BakeCompleteActivity.class);
                startActivity(intent);
            }
        });

        label_reprint = findViewById(R.id.label_reprint);
        label_reprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(BakeActivity.this, BakeReprintActivity.class);
                startActivity(intent);
            }
        });

        label_inquire = findViewById(R.id.label_inquire);
        label_inquire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(BakeActivity.this, BakeQueryActivity.class);
                startActivity(intent);
            }
        });
    }

    private void returnPage() {
        finish();
    }
 }
