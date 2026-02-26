package com.senao.warehouse.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.senao.warehouse.AppController;
import com.senao.warehouse.R;

public class OutsourcingPackingMethodActivity extends Activity {

    private RadioButton rbPLT, rbBox;
    private LinearLayout PalletLayout,BoxLayout;
    private Button btnConfirm, btnCancel,btnReturn;
    private EditText editPallet,editCNo;

    private RadioGroup.OnCheckedChangeListener rgListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio_PLT:
                    if (rbPLT.isChecked()) {
                        BoxLayout.setVisibility(View.GONE);
                    }
                    break;
                case R.id.radio_Box:
                    if (rbBox.isChecked()) {
                        BoxLayout.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outsourcing_packing_method);

        RadioGroup rg = findViewById(R.id.radio_group);
        rg.setOnCheckedChangeListener(rgListener);

        rbPLT = findViewById(R.id.radio_PLT);
        rbPLT.setChecked(true);
        rbBox = findViewById(R.id.radio_Box);

        BoxLayout = findViewById(R.id.layout_box);
        PalletLayout = findViewById(R.id.layout_pallet);

        editPallet = findViewById(R.id.edittext_pallet_no);
        editCNo = findViewById(R.id.edittext_c_no);

        btnConfirm = findViewById(R.id.button_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Confirm", String.valueOf(v.getId()));
                Intent intent = getIntent();
                String MergeNumber = intent.getStringExtra("MergeNum");
                AppController.debug("MergeNumber:" + MergeNumber);
                Bundle bundle = new Bundle();

                if (rbPLT.isChecked()) {
                    if (editPallet.getText().toString().trim().equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.merge_no_not_null), Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intenttoNext= new Intent(OutsourcingPackingMethodActivity.this, OutsourcingPackingListPalletActivity.class);
                        bundle.putString("MergeNum", MergeNumber);
                        bundle.putString("Pallet", editPallet.getText().toString());
                        intenttoNext.putExtras(bundle);
                        startActivity(intenttoNext);
                    }
                } else if (rbBox.isChecked()) {
                    if (editPallet.getText().toString().trim().equals("") || editCNo.getText().toString().trim().equals("") ) {
                        Toast.makeText(getApplicationContext(), getString(R.string.cant_be_null), Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intenttoNext = new Intent(OutsourcingPackingMethodActivity.this, OutsourcingPackingListPalletActivity.class);
                        bundle.putString("MergeNum", MergeNumber);
                        bundle.putString("Pallet", editPallet.getText().toString());
                        bundle.putString("CNo", editCNo.getText().toString());
                        intenttoNext.putExtras(bundle);
                        startActivity(intenttoNext);
                    }
                }
            }
        });

        btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnPage();
            }
        });

        btnCancel = findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cleanData();
            }
        });
    }

    private void returnPage() {
        hideKeyboard(editPallet);
        finish();
    }

    private void hideKeyboard(View edit) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
    }

    private void cleanData() {
        editCNo.setText("");
        editPallet.setText("");
    }
}
