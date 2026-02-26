package com.senao.warehouse.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.senao.warehouse.R;

public class TransferLocatorTriggerActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_locator_trigger);

        Button btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        Button btnManual = findViewById(R.id.button_manual);
        btnManual.setOnClickListener(this);

        Button btnPick = findViewById(R.id.button_pick);
        btnPick.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_return) {
            finish();
        } else if (id == R.id.button_manual) {
            Intent intent = new Intent(this, TransferLocatorTriggerManualActivity.class);
            startActivity(intent);
        } else if (id == R.id.button_pick) {
            Intent intent2 = new Intent(this, TransferLocatorTriggerPickActivity.class);
            startActivity(intent2);
        }
    }
}