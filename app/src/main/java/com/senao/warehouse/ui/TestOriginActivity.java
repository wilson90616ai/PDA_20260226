package com.senao.warehouse.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.senao.warehouse.R;

import java.util.Locale;

public class TestOriginActivity extends Activity {


    private EditText txtOrigin, txtImportCN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_origin);

        txtOrigin = findViewById(R.id.editText);
        txtOrigin.setSelectAllOnFocus(true);
        txtOrigin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {

                final boolean isEnterEvent = event != null
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
                final boolean isEnterUpEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_UP;
                final boolean isEnterDownEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_DOWN;

                if (actionId == EditorInfo.IME_ACTION_NEXT || isEnterUpEvent) {
                    // Do your action here
                    EditText et = (EditText) textView;
                    if (et.getText().toString().trim().equals("")) {
                        Toast.makeText(TestOriginActivity.this, getString(R.string.location_is_not_null),
                                Toast.LENGTH_SHORT).show();
                        return true;
                    } else {
                        if (getCountryCode(et.getText().toString()) == null) {
                            et.setSelectAllOnFocus(true);
                            Toast.makeText(TestOriginActivity.this, getString(R.string.origin_has_problem),
                                    Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    }
                    return false;
                } else if (isEnterDownEvent) {
                    // Capture this event to receive ACTION_UP
                    return true;
                } else {
                    // We do not care on other actions
                    return false;
                }
            }
        });
        txtImportCN = findViewById(R.id.editText2);

        Button button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkField())
                    Toast.makeText(getApplicationContext(), "OK", Toast.LENGTH_LONG).show();
            }
        });

    }

    private boolean checkField() {
        if (txtOrigin.getVisibility() == View.VISIBLE) {
            if (txtOrigin.getText().toString().trim().equals("")) {
                Toast.makeText(this, getString(R.string.enter_origin),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            if (getCountryCode(txtOrigin.getText().toString()) == null) {
                Toast.makeText(this, getString(R.string.origin_has_problem),
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (txtImportCN.getText().toString().trim().equals("")) {
            Toast.makeText(this, getString(R.string.enter_box_no),
                    Toast.LENGTH_SHORT).show();
            txtImportCN.requestFocus();
            return false;
        }
        return true;
    }

    @Nullable
    private String getCountryCode(String origin) {
        origin = origin.trim().toUpperCase();
        String[] locales = Locale.getISOCountries();
        for (String countryCode : locales) {

            Locale locale = new Locale("", countryCode);
            String iso = locale.getISO3Country();
            String code = locale.getCountry();
            String chName = locale.getDisplayCountry(Locale.TAIWAN);
            String enName = locale.getDisplayCountry(Locale.US);

            if (code.toUpperCase().equals(origin)
                    || iso.toUpperCase().equals(origin)
                    || enName.toUpperCase().contains(origin)
                    || chName.contains(origin)) {
                return code;
            }
        }
        String[] origins = getResources().getStringArray(R.array.origin_of_china);
        for (String item : origins) {
            if (item.toUpperCase().equals(origin)) {
                return "CN";
            }
        }
        origins = getResources().getStringArray(R.array.origin_of_tw);
        for (String item : origins) {
            if (item.toUpperCase().equals(origin)) {
                return "TW";
            }
        }
        return null;
    }
}
