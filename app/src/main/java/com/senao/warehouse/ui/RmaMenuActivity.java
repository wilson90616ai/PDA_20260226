package com.senao.warehouse.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.senao.warehouse.R;

public class RmaMenuActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rma_menu);

        Button btnPick = findViewById(R.id.btn_return);
        btnPick.setOnClickListener(this);

        Button btnReturn = findViewById(R.id.btn_pick);
        btnReturn.setOnClickListener(this);

        Button btnConfirm = findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_return) {
            finish();
        } else if (id == R.id.btn_pick) {
            Intent intent = new Intent(this, RmaPickingActivity.class);
            startActivity(intent);
        } else if (id == R.id.btn_confirm) {
            Intent intent2 = new Intent(this, RmaVerifyActivity.class);
            startActivity(intent2);
        }
    }
}
