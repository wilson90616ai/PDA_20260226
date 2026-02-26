package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.senao.warehouse.R;

public class OutsourcingPackingListBoxActivity extends Activity {
    private TextView textViewMergenum,textViewPalletNum,textViewCartonQTY,LabelPartNo,LabelReelId;
    private String errorInfo = "";
    private EditText EditQrcode;
    private static final String TAG = OutsourcingPackingListBoxActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outsourcing_packing_method_list_box);


        textViewMergenum=findViewById(R.id.textview_merge_number);
        textViewCartonQTY=findViewById(R.id.textview_carton_qty);
        textViewPalletNum=findViewById(R.id.textview_ban_num);
        LabelPartNo=findViewById(R.id.label_part_no);
        LabelReelId=findViewById(R.id.label_reel_id);
        LabelPartNo.setText(getString(R.string.label_part_no2,"0"));
        LabelReelId.setText(getString(R.string.label_reel_id2,"0"));

        Bundle bundle = getIntent().getExtras();
        String MergeNum = bundle.getString("MergeNum");
        String Pallet = bundle.getString("Pallet");

        textViewCartonQTY.setText("0");
        textViewMergenum.setText(MergeNum);
        textViewPalletNum.setText(Pallet);

        EditQrcode = findViewById(R.id.edittext_qr_code);//DN號碼
        EditQrcode.setText("");
        EditQrcode.setSelectAllOnFocus(true);
        EditQrcode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_UP)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down" + v.getId());
                    hideKeyboard(v);
                    String ReelID = EditQrcode.getText().toString().trim();
                    if (TextUtils.isEmpty(ReelID)) {
                        Toast.makeText(OutsourcingPackingListBoxActivity.this, getString(R.string.enter_qrcode),
                                Toast.LENGTH_SHORT).show();
                        return true;
                    } else {
                        String result = parseDn(ReelID);
                        if (TextUtils.isEmpty(result)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.cant_resolve_dn), Toast.LENGTH_LONG).show();
                            return true;
                        } else {
//                            txtDNNumber.setText(result);
//                            doQueryDN();
                        }
                    }
                }
                return false;
            }
        });


    }

    private void hideKeyboard(View edit) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
    }

    private String parseDn(String ReelID) {
        String result = null;
        if (!TextUtils.isEmpty(ReelID)) {
            if (ReelID.contains("@")) {
                String[] list = ReelID.split("@");
                if (list.length > 1) {
                    result = list[1];
                }
            } else {
                result = ReelID;
            }
        }
        return result;
    }


}
