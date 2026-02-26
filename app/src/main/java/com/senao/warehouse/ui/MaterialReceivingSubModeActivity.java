package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.RECEIVING_TYPE;
import com.senao.warehouse.database.VendorInfoHelper;

public class MaterialReceivingSubModeActivity extends Activity implements View.OnClickListener {

    private Button btnReturn, btnCombine, btnInvoice, btnPo, btnCombineInvoice;
    private TextView lblTitle, txtVendorName, lblStatus, txtVedorSiteCode, lblRemittance;
    private String errorInfo;
    private RECEIVING_TYPE funType;
    private VendorInfoHelper vendorInfo;
    private int accType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            vendorInfo = new Gson().fromJson(extras.getString("VENDOR_INFO"), VendorInfoHelper.class);

            if (vendorInfo == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.cant_find_vc_data), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_vc_data), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_material_receiving_sub_mode);

        lblTitle = findViewById(R.id.ap_title);

        accType = vendorInfo.isOutSourcing() ? MaterialReceivingActivity.OUTSOURCING : MaterialReceivingActivity.REGULAR;

        btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        txtVendorName = findViewById(R.id.txt_vendor_info);
        //txtVendorName.setText(getString(R.string.label_vendor_info, vendorInfo.getNum(), vendorInfo.getName()));
        txtVendorName.setText(vendorInfo.getName());

        lblRemittance = findViewById(R.id.label_remittance_yn);
        lblRemittance.setText(getString(R.string.label_remittance_yn, vendorInfo.getIsRemittance()));

        txtVedorSiteCode = findViewById(R.id.label_vendor_site_code);
        txtVedorSiteCode.setText(vendorInfo.getSiteCode());

        lblStatus = findViewById(R.id.label_status);
        lblStatus.setOnClickListener(this);

        btnCombine = findViewById(R.id.btn_combine);
        btnCombine.setOnClickListener(this);

        btnInvoice = findViewById(R.id.btn_invoice);
        btnInvoice.setOnClickListener(this);

        btnPo = findViewById(R.id.btn_po);
        btnPo.setOnClickListener(this);

        btnCombineInvoice = findViewById(R.id.btn_combine_invoice);
        btnCombineInvoice.setOnClickListener(this);

        funType = RECEIVING_TYPE.valueOf(vendorInfo.getReceivingType());
        setTitle(funType);
    }

    private void setTitle(RECEIVING_TYPE type) {
        SpannableString text;

        if (accType == MaterialReceivingActivity.OUTSOURCING) {
            if (type == RECEIVING_TYPE.RECEIPTS) {
                text = new SpannableString(getString(R.string.label_receiving_outsourcing1, AppController.getOrgName(),getString(R.string.Receiving)));
                text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                lblTitle.setText(text);
//                lblTitle.setText(getString(R.string.label_receiving_outsourcing, "收料"));
                btnInvoice.setVisibility(View.GONE);
                btnCombine.setVisibility(View.GONE);
                btnCombineInvoice.setVisibility(View.GONE);
            } else{
                text = new SpannableString(getString(R.string.label_receiving_outsourcing1, AppController.getOrgName(),getString(R.string.Pending_inspection)));
                text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                lblTitle.setText(text);
//                lblTitle.setText(getString(R.string.label_receiving_outsourcing, "暫收"));
            }
        } else {
            if (type == RECEIVING_TYPE.RECEIPTS){
                text = new SpannableString(getString(R.string.label_receiving1, AppController.getOrgName(),getString(R.string.Receiving)));
                text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                lblTitle.setText(text);
//                lblTitle.setText(getString(R.string.label_receiving, "收料"));
            }
            else{
                text = new SpannableString(getString(R.string.label_receiving1, AppController.getOrgName(),getString(R.string.Pending_inspection)));
                text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                lblTitle.setText(text);
//                lblTitle.setText(getString(R.string.label_receiving, "暫收"));
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button_return) {
            finish();
        } else if (id == R.id.label_status) {
            if (!TextUtils.isEmpty(errorInfo)) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MaterialReceivingSubModeActivity.this);
                dialog.setTitle("Error Msg");
                dialog.setMessage(errorInfo);
                dialog.setIcon(android.R.drawable.ic_dialog_alert);
                dialog.setCancelable(false);
                dialog.setNegativeButton( getString(R.string.btn_ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.dismiss();
                            }
                        });

                dialog.show();
            }
        } else if (id == R.id.btn_combine) {
            if (funType == RECEIVING_TYPE.RECEIPTS)
                goToInvoiceLocator(RECEIVING_TYPE.REC_COMBINE);
            else
                goToInvoiceLocator(RECEIVING_TYPE.TEMP_COMBINE);
        } else if (id == R.id.btn_invoice) {
            if (funType == RECEIVING_TYPE.RECEIPTS)
                goToNextPage(RECEIVING_TYPE.REC_INVOICE);
            else
                goToNextPage(RECEIVING_TYPE.TEMP_INVOICE);
        } else if (id == R.id.btn_po) {
            if (funType == RECEIVING_TYPE.RECEIPTS)
                goToInvoiceLocator(RECEIVING_TYPE.REC_PO);
            else
                goToInvoiceLocator(RECEIVING_TYPE.TEMP_PO);
        } else if (id == R.id.btn_combine_invoice) {
            if (funType == RECEIVING_TYPE.RECEIPTS)
                goToInvoiceNo(RECEIVING_TYPE.REC_COMBINE_INVOICE);
            else
                goToInvoiceNo(RECEIVING_TYPE.TEMP_COMBINE_INVOICE);
        }
    }

    private void goToNextPage(RECEIVING_TYPE type) {
        Intent intent = new Intent(this, MaterialReceivingListActivity.class);
        vendorInfo.setOutSourcing(accType == 1);
        vendorInfo.setReceivingType(type.name());
        intent.putExtra("VENDOR_INFO", new Gson().toJson(vendorInfo));
        startActivityForResult(intent, type.ordinal());
    }

    private void goToInvoiceLocator(RECEIVING_TYPE type) {
        Intent intent = new Intent(this, MaterialReceivingInvoiceLocatorActivity.class);
        vendorInfo.setOutSourcing(accType == 1);
        vendorInfo.setReceivingType(type.name());
        intent.putExtra("VENDOR_INFO", new Gson().toJson(vendorInfo));
        startActivityForResult(intent, type.ordinal());
    }

    private void goToInvoiceNo(RECEIVING_TYPE type) {
        Intent intent = new Intent(this, MaterialReceivingInvoiceActivity.class);
        vendorInfo.setOutSourcing(accType == 1);
        vendorInfo.setReceivingType(type.name());
        intent.putExtra("VENDOR_INFO", new Gson().toJson(vendorInfo));
        startActivityForResult(intent, type.ordinal());
    }
}
