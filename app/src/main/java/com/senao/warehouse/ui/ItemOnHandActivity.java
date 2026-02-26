package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.ItemOnHandInfoHelper;

public class ItemOnHandActivity extends Activity implements View.OnClickListener {
    private static final String TAG = ItemOnHandActivity.class.getSimpleName();
    private RadioButton rbAll, rbWaitInspect, rbOnHand;
    private CheckBox cbPn, cbSubinventory, cbLocator;
    private EditText etPn, etSubinventory, etLocator;
    private TextView lblTitle;

    private CompoundButton.OnCheckedChangeListener rbListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                /*if (buttonView.equals(cbPn)) {
                    etPn.setEnabled(true);
                    etPn.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    etPn.requestFocus();

                    if (cbSubinventory.isChecked()) {
                        etPn.setNextFocusDownId(R.id.edittext_subinventory);
                    } else if (cbLocator.isChecked()) {
                        etPn.setNextFocusDownId(R.id.edittext_locator);
                    } else {
                        etPn.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        etPn.setNextFocusDownId(R.id.button_confirm);
                    }
                } else*/

                if (buttonView.equals(cbSubinventory)) {
                    etSubinventory.setEnabled(true);
                    etSubinventory.requestFocus();
                    etSubinventory.setImeOptions(EditorInfo.IME_ACTION_NEXT);

                    /*if (cbPn.isChecked()) {
                        etPn.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        etPn.setNextFocusDownId(R.id.edittext_subinventory);
                    }*/

                    if (cbLocator.isChecked()) {
                        etSubinventory.setNextFocusDownId(R.id.edittext_locator);
                    } else {
                        etSubinventory.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        etSubinventory.setNextFocusDownId(R.id.button_confirm);
                    }
                } else {
                    etLocator.setEnabled(true);
                    etLocator.requestFocus();
                    etLocator.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    etLocator.setNextFocusDownId(R.id.button_confirm);

                    /*if (cbPn.isChecked() && !cbSubinventory.isChecked()) {
                        etPn.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        etPn.setNextFocusDownId(R.id.edittext_locator);
                    }*/

                    if (cbSubinventory.isChecked()) {
                        etSubinventory.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        etSubinventory.setNextFocusDownId(R.id.edittext_locator);
                    }
                }
            } else {
                /*if (buttonView.equals(cbPn)) {
                    etPn.setText("");
                    etPn.setEnabled(false);
                } else*/

                if (buttonView.equals(cbSubinventory)) {
                    etSubinventory.setText("");
                    etSubinventory.setEnabled(false);

                    /*if (cbPn.isChecked()) {
                        if (cbLocator.isChecked()) {
                            etPn.setNextFocusDownId(R.id.edittext_locator);
                        }
                        etPn.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        etPn.setNextFocusDownId(R.id.button_confirm);
                    }*/
                } else {
                    etLocator.setText("");
                    etLocator.setEnabled(false);

                    /*if (cbPn.isChecked() && !cbSubinventory.isChecked()) {
                        etPn.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        etPn.setNextFocusDownId(R.id.button_confirm);
                    }*/

                    if (cbSubinventory.isChecked()) {
                        etSubinventory.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        etSubinventory.setNextFocusDownId(R.id.button_confirm);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_on_hand);

        Button btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        Button btnConfirm = findViewById(R.id.button_confirm);
        btnConfirm.setOnClickListener(this);

        Button btnCancel = findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(this);

        TextView mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(this);

        rbAll = findViewById(R.id.radio_all);
        rbWaitInspect = findViewById(R.id.radio_wait_inspect);
        rbOnHand = findViewById(R.id.radio_on_hand);

        //cbPn = findViewById(R.id.cb_pn);
        //cbPn.setOnCheckedChangeListener(rbListener);

        cbSubinventory = findViewById(R.id.cb_subinventory);
        cbSubinventory.setOnCheckedChangeListener(rbListener);
        cbLocator = findViewById(R.id.cb_locator);
        cbLocator.setOnCheckedChangeListener(rbListener);

        etPn = findViewById(R.id.edittext_pn);
        etSubinventory = findViewById(R.id.edittext_subinventory);
        etLocator = findViewById(R.id.edittext_locator);

        rbAll.setChecked(true);
        //cbPn.setChecked(true);

        etLocator.setEnabled(false);
        etSubinventory.setEnabled(false);
        lblTitle = findViewById(R.id.ap_title);
        setTitle();
        cleanData();
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.label_item_on_hand1, AppController.getOrgName()));
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private boolean checkInputConditions() {
        if (TextUtils.isEmpty((etPn.getText().toString().trim()))) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_sku), Toast.LENGTH_LONG).show();
            etPn.requestFocus();
            return false;
        }

        if (etPn.getText().toString().trim().length() != 12) {
            Toast.makeText(getApplicationContext(), getString(R.string.sku_len_incorrect), Toast.LENGTH_LONG).show();
            etPn.requestFocus();
            return false;
        }

        if (cbSubinventory.isChecked() && TextUtils.isEmpty((etSubinventory.getText().toString().trim()))) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_sub), Toast.LENGTH_LONG).show();
            etSubinventory.requestFocus();
            return false;
        }

        if (cbLocator.isChecked() && TextUtils.isEmpty((etLocator.getText().toString().trim()))) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_loc), Toast.LENGTH_LONG).show();
            etLocator.requestFocus();
            return false;
        }

        return true;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.button_return) {
            returnPage();
        } else if (id == R.id.button_confirm) {
            if (checkInputConditions()) {
                goToList();
            }
        } else if (id == R.id.button_cancel) {
            cleanData();
        } else if (id == R.id.label_status) {
            showStatus();
        }
    }

    //查詢條件
    private void goToList() {
        ItemOnHandInfoHelper conditionHelper = new ItemOnHandInfoHelper();

        if (rbAll.isChecked()) {
            conditionHelper.setType(0);
        } else if (rbWaitInspect.isChecked()) {
            conditionHelper.setType(1);
        } else {
            conditionHelper.setType(2);
        }

        //if (cbPn.isChecked())
        conditionHelper.setItemNo(etPn.getText().toString().trim());
        //else
            //conditionHelper.setItemNo(null);

        if (cbSubinventory.isChecked())
            conditionHelper.setSubinventory(etSubinventory.getText().toString().trim());
        else
            conditionHelper.setSubinventory(null);

        if (cbLocator.isChecked())
            conditionHelper.setLocator(etLocator.getText().toString().trim());
        else
            conditionHelper.setLocator(null);

        Intent intent = new Intent(this, ItemOnHandListActivity.class);
        intent.putExtra("CONDITION_INFO",new Gson().toJson(conditionHelper));
        AppController.debug("查詢條件 INFO = "+ new Gson().toJson(conditionHelper));
        startActivity(intent);
    }

    private void showStatus() {
        String errorInfo = "";

        if (!TextUtils.isEmpty(errorInfo)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
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

    private void returnPage() {
        hideKeyboard();
        finish();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etPn.getWindowToken(), 0);
    }

    private void cleanData() {
        etPn.setText("");
        etSubinventory.setText("");
        etLocator.setText("");
    }
}
