package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.RECEIVING_TYPE;

public class MaterialReceivingModeActivity extends Activity implements View.OnClickListener {

    private final int MAIN = 0;
    private final int SUB = 1;
    private Button btnReturn, btnReceipts, btnTempReceipts, btnTempEmploy, btnDel, btnError, btnDelivery;
    private TextView lblTitle, lblStatus;
    private LinearLayout llMenu, llSubMenu;
    private String errorInfo;
    private int menuType;
    private RECEIVING_TYPE funType;
    private int accType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_receiving_mode);

        lblTitle =  findViewById(R.id.ap_title);
        llMenu =  findViewById(R.id.list_layout);
        llSubMenu =  findViewById(R.id.submenu_layout);

        accType = getIntent().getIntExtra("TYPE", 0);

        btnReturn =  findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        btnReceipts =  findViewById(R.id.btn_receipts);
        btnReceipts.setOnClickListener(this);

        btnTempReceipts =  findViewById(R.id.btn_temp_receipts);
        btnTempReceipts.setOnClickListener(this);

        btnTempEmploy =  findViewById(R.id.btn_temp_employ);
        btnTempEmploy.setOnClickListener(this);

        btnDel =  findViewById(R.id.btn_del);
        btnDel.setOnClickListener(this);

        btnError =  findViewById(R.id.btn_error);
        btnError.setOnClickListener(this);

        btnDelivery =  findViewById(R.id.btn_delivery);
        btnDelivery.setOnClickListener(this);

        lblStatus =  findViewById(R.id.label_status);
        lblStatus.setOnClickListener(this);

        setView(MAIN);
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.label_picking, AppController.getOrgName()));
//        text.setSpan(new RelativeSizeSpan(1f), 7, 7 + AppController.getOrgName().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(Color.parseColor("#fa160a")), 7, 7 + AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private void setView(int type) {
        SpannableString text;
        this.menuType = type;

        if (type == MAIN) {
            if (accType == MaterialReceivingActivity.OUTSOURCING) {
                text = new SpannableString(getString(R.string.label_material_receiving_outsourcing1, AppController.getOrgName()));
                text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                lblTitle.setText(text);
//                lblTitle.setText(R.string.label_material_receiving_outsourcing);
                btnTempReceipts.setEnabled(false);
                btnTempEmploy.setEnabled(false);
            } else {
                text = new SpannableString(getString(R.string.label_material_receiving1, AppController.getOrgName()));
                text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                lblTitle.setText(text);
//                lblTitle.setText(R.string.label_material_receiving);
                btnTempReceipts.setEnabled(true);
                btnTempEmploy.setEnabled(true);
            }

            llMenu.setVisibility(View.VISIBLE);
            llSubMenu.setVisibility(View.GONE);
        } else {
            if (accType == MaterialReceivingActivity.OUTSOURCING) {
                text = new SpannableString(getString(R.string.label_receiving_outsourcing1, AppController.getOrgName(),getString(R.string.label_delete3)));
                text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                lblTitle.setText(text);
//                lblTitle.setText(getString(R.string.label_receiving_outsourcing, "刪除 "));
            } else {
                text = new SpannableString(getString(R.string.label_receiving1, AppController.getOrgName(),getString(R.string.label_delete3)));
                text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                lblTitle.setText(text);
//                lblTitle.setText(getString(R.string.label_receiving, "刪除 "));
            }

            llMenu.setVisibility(View.GONE);
            llSubMenu.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button_return) {
            returnPage();
        } else if (id == R.id.label_status) {
            if (!TextUtils.isEmpty(errorInfo)) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MaterialReceivingModeActivity.this);
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
        } else if (id == R.id.btn_receipts) {
            funType = RECEIVING_TYPE.RECEIPTS;
            goToNextPage(funType);
        } else if (id == R.id.btn_temp_receipts) {
            funType = RECEIVING_TYPE.TEMP_RECEIPTS;
            goToNextPage(funType);
        } else if (id == R.id.btn_temp_employ) {
            funType = RECEIVING_TYPE.TEMP_EMPLOY;
            goToNextPage(funType);
        } else if (id == R.id.btn_del) {
            funType = RECEIVING_TYPE.DELETE;
            setView(SUB);
        } else if (id == R.id.btn_error) {
            funType = RECEIVING_TYPE.DEL_ERROR;
            goToNextPage(funType);
        } else if (id == R.id.btn_delivery) {
            funType = RECEIVING_TYPE.DEL_PREDELIVER;
            goToNextPage(funType);
        }
    }

    private void goToNextPage(RECEIVING_TYPE type) {
        Intent intent = new Intent();

        switch (type) {
            case RECEIPTS:

            case TEMP_RECEIPTS:
                intent.setClass(this, MaterialReceivingActivity.class);//btn_receipts 收料
                break;
            case DEL_ERROR:

            case DEL_PREDELIVER:
                intent.setClass(this, MaterialReceivingDelActivity.class);
                break;
            case TEMP_EMPLOY:
                intent.setClass(this, MaterialReceivingTempActivity.class);
                break;
        }

        intent.putExtra("RECEIVING_TYPE", type.name());
        intent.putExtra("TYPE", accType);
        startActivityForResult(intent, type.ordinal());
    }

    @Override
    public void onBackPressed() {
        returnPage();
    }

    private void returnPage() {
        if (menuType == MAIN)
            finish();
        else
            setView(MAIN);
    }
}
