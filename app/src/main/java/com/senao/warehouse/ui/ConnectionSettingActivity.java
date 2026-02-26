package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.SERVER;
import com.senao.warehouse.http.HttpClient;
import com.senao.warehouse.http.ServerResponse;
import com.senao.warehouse.util.Preferences;

public class ConnectionSettingActivity extends Activity implements OnClickListener {
    private int serverType = 0;
    private String host = null;
    private int port = 0;
    private Button btnConfirm, btnCancel, btnConnectTest;
    private EditText txtServerIP, txtServerPort;
    private TextView statusbar;
    private String serverInfo,serverIp,serverPort;//20230517 新增
    private HttpClient httpClient;
    private String errorInfo = "";
    private Spinner spinnerServer;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_setting);

        btnConfirm = findViewById(R.id.button_confirm_setting);
        btnCancel = findViewById(R.id.button_cancel_setting);
        btnConnectTest = findViewById(R.id.button_connect_test);
        mContext = this;

        ArrayAdapter adapterServer = new ArrayAdapter<>(this, R.layout.spinner_item, getResources().getStringArray(R.array.servers));
        adapterServer.setDropDownViewResource(R.layout.spinner_item);

        spinnerServer = findViewById(R.id.spinneServer);
        spinnerServer.setAdapter(adapterServer);
        spinnerServer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //20230517 新增
                txtServerIP.setEnabled(true);
                txtServerPort.setEnabled(true);

                if (i == SERVER.PROD.ordinal()) {
                    serverIp = Preferences.getSharedPreferences(mContext).getString(Preferences.SERVER_IP_FORMAL, null);
                    serverPort = Preferences.getSharedPreferences(mContext).getString(Preferences.SERVER_PORT_FORMAL, null);
                } else if (i == SERVER.TEST.ordinal()) {
                    serverIp = Preferences.getSharedPreferences(mContext).getString(Preferences.SERVER_IP_TEST, null);
                    serverPort = Preferences.getSharedPreferences(mContext).getString(Preferences.SERVER_PORT_TEST, null);
                } else {
                    serverIp = Preferences.getSharedPreferences(mContext).getString(Preferences.SERVER_IP_OTHER, null);
                    serverPort = Preferences.getSharedPreferences(mContext).getString(Preferences.SERVER_PORT_OTHER, null);
                }

                txtServerIP.setText(serverIp);
                txtServerPort.setText(serverPort);
                Log.d("onItemSelected", serverIp+':'+serverPort);
                //String serverInfo;

                /*if (i == SERVER.PROD.ordinal()) {
                    serverInfo = AppController.getProperties("ProdServer");
                    txtServerIP.setText(serverInfo.split(":")[0]);
                    txtServerPort.setText(serverInfo.split(":")[1]);
                    txtServerIP.setEnabled(false);
                    txtServerPort.setEnabled(false);
                } else if (i == SERVER.TEST.ordinal()) {
                    serverInfo = AppController.getProperties("TestServer");
                    txtServerIP.setText(serverInfo.split(":")[0]);
                    txtServerPort.setText(serverInfo.split(":")[1]);
                    txtServerIP.setEnabled(false);
                    txtServerPort.setEnabled(false);
                } else {
                    txtServerIP.setEnabled(true);
                    txtServerPort.setEnabled(true);
                    if (serverType != SERVER.OTHER.ordinal() || TextUtils.isEmpty(host) || TextUtils.isEmpty(String.valueOf(port))) {
                        txtServerIP.setText("");
                        txtServerPort.setText("");
                        txtServerIP.requestFocus();
                    } else {
                        txtServerIP.setText(host);
                        txtServerPort.setText(String.valueOf(port));
                        int pos = txtServerIP.getText().length();
                        txtServerIP.setSelection(pos);
                    }
                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        txtServerIP = findViewById(R.id.input_server_ip);
        txtServerIP.setSelectAllOnFocus(true);

        txtServerPort = findViewById(R.id.input_server_port);
        txtServerPort.setSelectAllOnFocus(true);

        statusbar = findViewById(R.id.label_connection_status);
        statusbar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ConnectionSettingActivity.this);
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
        });

        setViewValue();
    }

    private void setViewValue() {
        //20230517 新增
        txtServerIP.setEnabled(true);
        txtServerPort.setEnabled(true);
        serverType = Preferences.getInt(this, Preferences.SERVER_TYPE);
        spinnerServer.setSelection(serverType);

        if (serverType == SERVER.PROD.ordinal()) {
            serverIp = Preferences.getSharedPreferences(mContext).getString(Preferences.SERVER_IP_FORMAL, null);
            serverPort = Preferences.getSharedPreferences(mContext).getString(Preferences.SERVER_PORT_FORMAL, null);
        } else if (serverType == SERVER.TEST.ordinal()) {
            serverIp = Preferences.getSharedPreferences(mContext).getString(Preferences.SERVER_IP_TEST, null);
            serverPort = Preferences.getSharedPreferences(mContext).getString(Preferences.SERVER_PORT_TEST, null);
        } else {
            serverIp = Preferences.getSharedPreferences(mContext).getString(Preferences.SERVER_IP_OTHER, null);
            serverPort = Preferences.getSharedPreferences(mContext).getString(Preferences.SERVER_PORT_OTHER, null);
        }

        txtServerIP.setText(serverIp);
        txtServerPort.setText(serverPort);
        Log.d("setViewValue", serverIp+':'+serverPort);

        //Get Server Type setting  //20230517 註解掉
        /*serverType = Preferences.getInt(this, Preferences.SERVER_TYPE);
        spinnerServer.setSelection(serverType);

        serverInfo = Preferences.getSharedPreferences(this).getString(Preferences.PREFERENCE_NAME, null);
        Log.d("Debug", "Get Saved Server Info:" + serverInfo);

        if (serverInfo != null) {//20230517 註解掉
            try {
                parseHostPort(serverInfo);
                txtServerIP.setText(host);
                txtServerPort.setText(String.valueOf(port));
            } catch (Exception e) {
                Log.e("ERROR", e.toString());
                statusbar.setText("Please specify correct Server IP:PORT");
            }
        } else {
            txtServerIP.setText("");
            txtServerPort.setText("");
            statusbar.setText("Please set Server IP:PORT");
        }*/

        btnConfirm.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnConnectTest.setOnClickListener(this);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtServerPort.getWindowToken(), 0);
    }

    private void getServerInfo() {
        host = txtServerIP.getText().toString();
        port = Integer.parseInt(txtServerPort.getText().toString());
        statusbar.setText(host + ":" + port);
    }

    private boolean saveServerInfo() {
        try {
            Preferences.setInt(this, Preferences.SERVER_TYPE, spinnerServer.getSelectedItemPosition());
            host = txtServerIP.getText().toString();
            port = Integer.parseInt(txtServerPort.getText().toString());

            switch (spinnerServer.getSelectedItemPosition()) {
                case 0:
                    Preferences.setInt(this, Preferences.SERVER_IP_API, 0);//SERVER_IP_API
                    AppController.setAPIService(0);
                    Preferences.setString(this, Preferences.SERVER_IP_FORMAL, host);//
                    Preferences.setString(this, Preferences.SERVER_PORT_FORMAL, String.valueOf(port));
                    break;
                case 1:
                    Preferences.setInt(this, Preferences.SERVER_IP_API, 1);//SERVER_IP_API
                    AppController.setAPIService(1);
                    Preferences.setString(this, Preferences.SERVER_IP_TEST, host);
                    Preferences.setString(this, Preferences.SERVER_PORT_TEST, String.valueOf(port));
                    break;
                case 2:
                    Preferences.setString(this, Preferences.SERVER_IP_OTHER, host);
                    Preferences.setString(this, Preferences.SERVER_PORT_OTHER, String.valueOf(port));
                    break;
                default:
                    System.out.println("Invalid day of the week");
                    break;
            }

            Preferences.setString(this, Preferences.PREFERENCE_NAME, host + ":" + port);
            statusbar.setText("ServerInfo saved");
            serverInfo = host + ":" + port;

            //20230518 註解
            /*host = txtServerIP.getText().toString();
            port = Integer.parseInt(txtServerPort.getText().toString());
            Preferences.setString(this, Preferences.PREFERENCE_NAME, host + ":"+ port);
            Log.d("saveServerInfo: ", host + ":" + port);
            statusbar.setText("ServerInfo saved");
            serverInfo = host + ":" + port;
            parseHostPort(host + ":" + port);
            txtResponseArea.setText("Server IP:PORT saved");*/
            return true;
        } catch (Exception e) {
            //txtResponseArea.setText("ERROR: Please veriry Server IP:PORT setting");
            Log.e("ERROR", e.toString());
            statusbar.setText("ERROR occurs");
            return false;
        }
    }

    private void parseHostPort(String ipport) {
        host = ipport.split(":")[0];
        port = Integer.parseInt(ipport.split(":")[1]);
        Log.d("Debug", "host:port is " + host + ":" + port);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.connection_setting, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        Intent intent;

        switch (viewId) {
            case R.id.button_cancel_setting:
                Log.d("Debug", "Click cancel button");
                //intent = new Intent();
                //intent.setClass(ConnectionSettingActivity.this,
                //LoginActivity.class);
                //startActivity(intent);
                //select(txtFeederId);

                if (serverInfo == null) {
                    hideKeyboard();
                    statusbar.setText(R.string.setting_connect_server_first);
                } else
                    finish();
                break;
            case R.id.button_confirm_setting:
                Log.d("Debug", "Click confirm button ");

                if (txtServerIP.getText().toString().equals("") || txtServerPort.getText().toString().equals("")) {
                    Toast.makeText(this, getString(R.string.ip_or_port_is_not_null), Toast.LENGTH_SHORT).show();

                    if (txtServerIP.getText().toString().equals("")) {
                        txtServerIP.requestFocus();
                    } else {
                        txtServerPort.requestFocus();
                    }
                } else {
                    if (saveServerInfo()) {
                        //intent = new Intent();
                        //intent.setClass(ConnectionSettingActivity.this,
                        //LoginActivity.class);
                        //startActivity(intent);
                        setResult(RESULT_OK);
                        finish();
                    }
                }
                break;
            case R.id.button_connect_test:
                Log.d("Debug", "Click connect_test button");
                Log.d("Debug", "txtServerIP=" + txtServerIP.getText().toString() + ":" + txtServerPort.getText().toString());

                if (txtServerIP.getText().toString().equals("") || txtServerPort.getText().toString().equals("")) {
                    Toast.makeText(this, getString(R.string.ip_or_port_is_not_null), Toast.LENGTH_SHORT).show();

                    if (txtServerIP.getText().toString().equals("")) {
                        txtServerIP.requestFocus();
                    } else {
                        txtServerPort.requestFocus();
                    }
                } else {
                    hideKeyboard();
                    getServerInfo();
                    connectTest();
                }
                break;
        }
    }

    private void connectTest() {
        new AsyncTaskRunner().execute(0);
    }

    @Override
    public void onBackPressed() {
        if (serverInfo == null)
            statusbar.setText(R.string.setting_connect_server_first);
        else
            finish();
    }

    // AsyncTask<Params, Progress, Result>
    private class AsyncTaskRunner extends AsyncTask<Integer, String, String> {
        String retCode = "unknown";

        @Override
        protected String doInBackground(Integer... params) {
            publishProgress("Connecting..."); // Calls onProgressUpdate()

            try {
                //render json
                //http post
                //get http response
                //parse json
                publishProgress("Connection establishing...");
                String request = "";

                switch (params[0]) {
                    case 0:
                        Log.d("Debug", "REQUEST:" + request);
                        AppController.debug("Test connection to " + host + ":" + port + AppController.getProperties("Connection_Test"));

                        try {
                            httpClient = new HttpClient(host, port);
                            ServerResponse response = httpClient.doPost("", AppController.getProperties("Connection_Test"), Integer.parseInt(AppController.getProperties("Connection_Test_Timeout")));
                            AppController.debug("Server response:" + response.getCode());

                            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                                AppController.debug("Server connected");
                                errorInfo = "";
                                publishProgress("Connection success."+host + ":" + port);
                            } else {
                                AppController.debug("Server connect failed");
                                errorInfo = "";
                                publishProgress("Connection failed.");
                            }
                        } catch (Exception e) {
                            AppController.debug("Error to connect the server. " + e.getMessage());
                            errorInfo = e.getMessage();
                            publishProgress("Connection failed.");
                        }
                        break;
                    default:
                        break;
                }

                //echoSocket.close();
                return retCode;
            } catch (Exception e) {
                Log.d("Debug", "Exception:" + e.toString());
                publishProgress("Connection failed.");
                return e.toString();
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            //execution of result of Long time consuming operation //
            //finalResult.setText(result);
            //txtResponseArea.setText(result);
            Log.d("Debug", "onPostExecute(),result=" + result);

            if (retCode.contains("success")) {
                statusbar.setBackgroundColor(Color.GREEN);
            } else {
                statusbar.setBackgroundColor(Color.RED);
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            //Things to be done before execution of long running operation. For
            //example showing ProgessDialog
            Log.d("Debug", "onPreExecute()");
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onProgressUpdate(Progress[])
         */
        @Override
        protected void onProgressUpdate(String... text) {
            Log.d("Debug", "onProgressUpdate(),text[] length=" + text.length);
            StringBuffer sb = new StringBuffer();
            int i;

            for (i = 0; i < text.length; i++) {
                Log.d("Debug", "text[" + i + "]=" + text[i]);
                sb.append(text[i] + " ");
            }

            statusbar.setText(sb.toString());
            retCode = sb.toString();
            sb = null;
            //Things to be done while execution of long running operation is in
            //progress. For example updating ProgessDialog
        }
    }
}
