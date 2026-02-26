package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.ClosedDateSetHelper;
import com.senao.warehouse.handler.StockInHandler;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.util.Util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class StockClosedDateSetActivity extends Activity implements View.OnClickListener {

    private final String TAG = StockClosedDateSetActivity.class.getSimpleName();
    private TextView mConnection,lblTitle;
    private EditText txtDate;
    private String errorInfo;
    private ProgressDialog dialog;
    private StockInHandler stockIn;
    private ClosedDateSetHelper setHelper;
    private int mYear, mMonth, mDay;

    DialogInterface.OnClickListener dialogResetClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    resetClosedDate();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    break;
            }
        }
    };

    DialogInterface.OnClickListener dialogSetClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    setClosedDate(txtDate.getText().toString().trim());
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closed_date_set);

        Button btnDateReset = findViewById(R.id.button_date_reset);
        btnDateReset.setOnClickListener(this);
        Button btnDateSet = findViewById(R.id.button_date_set);
        btnDateSet.setOnClickListener(this);

        mConnection = findViewById(R.id.label_status);
        mConnection.setOnClickListener(this);

        Button btnReturn = findViewById(R.id.button_return);
        btnReturn.setOnClickListener(this);

        Button btnDatePicker = findViewById(R.id.btn_date);
        txtDate = findViewById(R.id.in_date);

        btnDatePicker.setOnClickListener(this);

        stockIn = new StockInHandler();
        lblTitle = findViewById(R.id.ap_title);
        setTitle();
        getSetDate();
    }

    private void setTitle() {
        final SpannableString text;
        text = new SpannableString(getString(R.string.label_closed_date_set1, AppController.getOrgName()));
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, AppController.getOrgName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTitle.setText(text);
    }

    private void resetClosedDate() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.processing), true);
        setHelper = new ClosedDateSetHelper();
        new SetClosedDate().execute(0);
    }

    private void setClosedDate(String date) {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.processing), true);
        setHelper = new ClosedDateSetHelper();
        setHelper.setClosedDate(date);
        new SetClosedDate().execute(0);
    }

    private boolean checkCondition() {
        if (txtDate.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.select_a_date_first), Toast.LENGTH_LONG).show();
            txtDate.requestFocus();
            return false;
        }

        return Util.parseDate(txtDate.getText().toString().trim());
    }

    @Override
    public void onClick(View view) {
        AlertDialog.Builder builder;
        int id = view.getId();

        if (id == R.id.btn_date) {// Get Current Date
            Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(year, monthOfYear, dayOfMonth);

                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN);
                            txtDate.setText(dateFormat.format(calendar.getTime()));
                            txtDate.requestFocus();
                        }
                    }, mYear, mMonth, mDay);

            datePickerDialog.show();
        } else if (id == R.id.button_return) {
            finish();
        } else if (id == R.id.button_date_reset) {
            builder = new AlertDialog.Builder(this);
            builder.setTitle( getString(R.string.btn_ok)).setMessage(R.string.Determine_the_reset_closing_date)
                    .setPositiveButton(getString(R.string.yes), dialogResetClickListener)
                    .setNegativeButton(getString(R.string.no), dialogResetClickListener).show();
        } else if (id == R.id.button_date_set) {
            if (checkCondition()) {
                builder = new AlertDialog.Builder(this);
                builder.setTitle( getString(R.string.btn_ok)).setMessage(R.string.set_a_closing_date)
                        .setPositiveButton(getString(R.string.yes), dialogSetClickListener)
                        .setNegativeButton(getString(R.string.no), dialogSetClickListener).show();
            }
        } else if (id == R.id.label_status) {
            if (!TextUtils.isEmpty(errorInfo)) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
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
        }
    }

    private void getSetDate() {
        dialog = ProgressDialog.show(this, getString(R.string.holdon), getString(R.string.data_reading), true);
        setHelper = new ClosedDateSetHelper();
        new GetClosedDate().execute(0);
    }

    private class GetClosedDate extends AsyncTask<Integer, String, ClosedDateSetHelper> {
        @Override
        protected ClosedDateSetHelper doInBackground(Integer... params) {
            AppController.debug("Get Set Time from " + AppController.getServerInfo()
                    + AppController.getProperties("GetClosedDate"));
            publishProgress(getString(R.string.downloading_data));
            return stockIn.getClosedDate(setHelper);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }

        @Override
        protected void onPostExecute(ClosedDateSetHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                setHelper = result;

                if (setHelper.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    txtDate.setText(setHelper.getClosedDate());
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = setHelper.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class SetClosedDate extends AsyncTask<Integer, String, ClosedDateSetHelper> {
        @Override
        protected ClosedDateSetHelper doInBackground(Integer... params) {
            AppController.debug("Set Close Time from " + AppController.getServerInfo()
                    + AppController.getProperties("SetClosedDate"));
            publishProgress(getString(R.string.processing));
            return stockIn.setClosedDate(setHelper);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }

        @Override
        protected void onPostExecute(ClosedDateSetHelper result) {
            // execution of result of Long time consuming operation //
            // finalResult.setText(result);
            // txtResponseArea.setText(result);
            dialog.dismiss();
            AppController.debug("onPostExecute() result json = " + new Gson().toJson(result));

            if (result != null) {
                setHelper = result;

                if (setHelper.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                    txtDate.setText(setHelper.getClosedDate());
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = setHelper.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                }
            } else {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }
}
