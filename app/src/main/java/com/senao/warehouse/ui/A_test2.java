package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.senao.warehouse.R;

public class A_test2 extends Activity {
    private EditText text_value, text_value2; //資料_Barcode, 資料_QRCode
    private Button button_last_page, button_alert_value; //下一頁, 顯示資料

    private String text_value_lastpage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_test2);

        text_value = findViewById(R.id.text_value);
        text_value2 = findViewById(R.id.text_value2);
        button_last_page = findViewById(R.id.button_last_page);
        button_alert_value = findViewById(R.id.button_alert_value);

        // 取得傳遞過來的 text_value_lastpage
        text_value_lastpage = getIntent().getStringExtra("text_value_lastpage");
        text_value.setText(text_value_lastpage); //上一頁資料

        button_last_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text_value2_nextpage = text_value2.getText().toString().trim();
                //String resultData = text_value_lastpage + " + " + text_value2_nextpage;

                Intent resultIntent = new Intent();
                resultIntent.putExtra("text_value_nextpage", text_value_lastpage + " + " + text_value2_nextpage);
                setResult(RESULT_OK, resultIntent);
                finish(); // 关闭当前页面
            }
        });

        button_alert_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick TEST", String.valueOf(v.getId()));

                new AlertDialog.Builder(A_test2.this)
                        .setTitle("確認資料")
                        .setMessage("資料 B: " + text_value_lastpage + ", " + text_value2.getText().toString().trim())
                        .setPositiveButton("確定", null)
                        .show();
            }
        });
    }
}
