package com.senao.warehouse.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.widget.Button;
import android.widget.TextView;

import com.senao.warehouse.AppController;
import com.senao.warehouse.R;

public class TransferRolloverActivity  extends Activity {

    private TextView lblTitle;
    private Button button_select,button_manual,button_sign,button_form;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_transfer_rollover);
        lblTitle = findViewById(R.id.ap_title);
        setTitle();
        button_select = findViewById(R.id.button_select);
        button_manual = findViewById(R.id.button_manual);
        button_sign = findViewById(R.id.button_sign);
        button_form = findViewById(R.id.button_form);




    }

    private void setTitle() {
        final SpannableString text;

        text = new SpannableString(getString(R.string.label_transfer_rollover, AppController.getOrgName()));
        text.setSpan(new RelativeSizeSpan(1f), 0, AppController.getOrgName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }
}
