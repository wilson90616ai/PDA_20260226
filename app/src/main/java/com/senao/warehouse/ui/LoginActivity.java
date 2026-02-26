package com.senao.warehouse.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.apiservice.ApiClient;
import com.senao.warehouse.apiservice.ApiManager;
import com.senao.warehouse.apiservice.ApiClient.ApiResponse;
import com.senao.warehouse.apiservice.ApiManager.DataRequest;
import com.senao.warehouse.asynctask.AsyncResponse;
import com.senao.warehouse.asynctask.DownloadApk;
import com.senao.warehouse.asynctask.Login;
import com.senao.warehouse.asynctask.Upgrade;
import com.senao.warehouse.database.ApiAuthHelper;
import com.senao.warehouse.database.ApkHelper;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.COMPANY;
import com.senao.warehouse.database.ConnectionDetails;
import com.senao.warehouse.database.FortinetSkuHelper;
import com.senao.warehouse.database.UserInfoHelper;
import com.senao.warehouse.handler.APIHandler;
import com.senao.warehouse.handler.OrgHandler;
import com.senao.warehouse.handler.OrgHelper;
import com.senao.warehouse.handler.OuOrgHelper;
import com.senao.warehouse.print.BtPrintLabel;
import com.senao.warehouse.util.ConnectionUtils;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.Preferences;
import com.senao.warehouse.util.ReturnCode;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class LoginActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PRINTER_SETTING = 2;
    private static final int REQUEST_BLUETOOTH_SETTINGS = 3;

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int REQUEST_CODE = 1234;
    private static final int REQUEST_CODE_ASK_WRITE_EXTERNAL_STORAGE_PERMISSIONS = 101;
    public static String apiToken;
    public static String apiToken_scm;
    public static String apiToken_scm_vn;
    public static String apiToken_chose;

    private Button btnLogin, btnLoginCancel, btnConnectionSetting,
            btnPrinterSetting, btnBTConnect,
            btnBTDisconnect,btnChangeLang;
    private EditText txtName, txtPWD;
    private TextView mConnection, textViewVersion,textViewIp;
    private String serverInfo;
    private String errorInfo = "";
    private int versionCode;
    private Spinner spinnerCompany;
    private Spinner spinnerFacotry;
    private Spinner spinnerLanguage;
    private ArrayAdapter adapterCompany; //公司別
    private ArrayAdapter adapterFactory; //工廠別
    private ArrayAdapter adapterLanguage; //語系
    private String downloadUrl;
    private UserInfoHelper userInfoHelper;
    private ApkHelper apk;

    private OrgHelper orgHelper;
    private ApiAuthHelper apiAuthHelper;

    private OrgHandler orgHandler;
    private APIHandler apiHandler;
    public static Map<Integer,String> orgMap ;

    private String[] companies={"神準1","恩睿","EnGenius Networks"};
    private int[] senao_factories={86};
    private List<String> com = new ArrayList<>();
    private List<String> comOrgName = new ArrayList<>();
    private List<Integer> comOrg = new ArrayList<>();

    List<OuOrgHelper> ouOrgHelperList = null;
    Set set = null;
    List<String> orgNameList = new ArrayList<>();;

    public static Map<Integer,List<Map<Integer,String>>> ouOrgMap = null;
    private boolean isFirstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isTaskRoot()) {
            final Intent intent = getIntent();
            final String intentAction = intent.getAction();

            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null && intentAction.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }

        comOrg.add(86);
        comOrgName.add("IC");
        comOrgName.add("IC");
        comOrgName.add("IC");

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);
        orgHandler = new OrgHandler();
        apiHandler = new APIHandler();

        spinnerCompany = findViewById(R.id.spinnerCompany);
        spinnerFacotry = findViewById(R.id.spinnerFactory);

        if(Constant.ISOUORG){
            com.add("神準C");
            com.add("恩睿");
            com.add("EnGenius Networks");
            adapterCompany = new ArrayAdapter<>(this, R.layout.spinner_item, com); //ORGOU專案 com companies
        }else{
            adapterCompany = new ArrayAdapter<>(this, R.layout.spinner_item, getResources().getStringArray(R.array.companies));
        }

        //AppController.debug("adapterCompany.getCount() = "+adapterCompany.getCount());
        adapterCompany.setDropDownViewResource(R.layout.spinner_item);
        spinnerCompany.setAdapter(adapterCompany);
        spinnerCompany.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                AppController.debug("onItemSelected = "+i);

                if(Constant.ISOUORG){
                        orgNameList.clear();

                        if(ouOrgHelperList !=null){
                            for(OuOrgHelper ouOrgHelper: ouOrgHelperList){
                                if(ouOrgHelper.getOuName().equals(com.get(i))){
                                    orgNameList.add(ouOrgHelper.getOrgEName());
                                }
                            }

                            adapterFactory = new ArrayAdapter<>(LoginActivity.this, R.layout.spinner_item,orgNameList);
                            //adapterFactory.notifyDataSetChanged();
                        }else{
                            orgNameList.add("IC");
                            orgNameList.add("CN1");
                            orgNameList.add("ENT1");
                            adapterFactory = new ArrayAdapter<>(LoginActivity.this, R.layout.spinner_item,orgNameList);
                            //adapterFactory.notifyDataSetChanged();
                        }
                }else {
                    //原來
                    if (i == COMPANY.SENAO_NETWORKS.ordinal()) {
                        if(Constant.ISORG){
                            adapterFactory = new ArrayAdapter<>(LoginActivity.this, R.layout.spinner_item, comOrgName);//ORGOU專案
                        }else{
                            adapterFactory = new ArrayAdapter<>(LoginActivity.this, R.layout.spinner_item, getResources().getStringArray(R.array.senao_factories));
                        }
                    } else if (i == COMPANY.ENRACK.ordinal()) {
                        adapterFactory = new ArrayAdapter<>(LoginActivity.this, R.layout.spinner_item, getResources().getStringArray(R.array.enrack_factories));
                    } else {
                        adapterFactory = new ArrayAdapter<>(LoginActivity.this, R.layout.spinner_item, getResources().getStringArray(R.array.eng_factories));
                    }
                }

                adapterFactory.setDropDownViewResource(R.layout.spinner_item);
                spinnerFacotry.setAdapter(adapterFactory);
                AppController.debug("adapterFactory.getCount() = "+adapterFactory.getCount());
                AppController.debug("AppController.getCompany() = "+AppController.getCompany());
                AppController.debug("AppController.getFactory() = "+AppController.getFactory());

                /*if (i == AppController.getCompany()) {
                    spinnerFacotry.setSelection(AppController.getFactory());//有機會發生IndexOutOfBoundsException
                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                AppController.debug("spinnerCompany.setOnItemSelectedListener onNothingSelected");
            }
        });

        //btnConnect =  findViewById(R.id.button_conect);
        //btnScannerSetting =  findViewById(R.id.button_scanner_setting);

        txtName = findViewById(R.id.input_login_name);

        textViewVersion = findViewById(R.id.textViewVersion);
        textViewIp = findViewById(R.id.textViewIp);

        PackageManager manager = this.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            textViewVersion.setText("Version:"+info.versionName); //版本名
            versionCode = info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        mConnection = findViewById(R.id.label_main_status);
        mConnection.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
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

        //checkPrinterSetting();

        btnLogin = findViewById(R.id.button_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Login", String.valueOf(v.getId()));
                //txtName.setText("WHFG01");
                //txtPWD.setText("100236");

                if (false) {
                    UserInfoHelper user = new UserInfoHelper();

                    if (txtName.getText().toString().toUpperCase().contains("SMT")) {
                        user.setUserName("SMT01");
                    } else {
                        user.setUserName("WHFG01");
                    }

                    user.setPassword("100236");
                    user.setPalletYN("Y");
                    AppController.setUser(user);
                    /*DeliveryInfoHelper dn = new DeliveryInfoHelper();
                    dn.setDeliveryID(123456);
                    dn.setOpStatus("OPEN");
                    dn.setCustomer("Fortinet");
                    AppController.setDnInfo(dn);
                    Intent intent = new Intent();
                    ItemInfoHelper item = new ItemInfoHelper();
                    item.setControl("SN");
                    item.setId(123456);
                    item.setItemID("1102A1234567");
                    item.setDescription("test");
                    item.setQty(275);
                    item.setPass(0);
                    item.setWait(275);
                    intent.putExtra("ITEM_INFO", new Gson().toJson(item));
                    intent.putExtra("PACK_QTY", 10);
                    intent.setClass(LoginActivity.this, MenuActivity.class);
                    startActivityForResult(intent, 0);

                    Intent intent = new Intent(LoginActivity.this, AcceptanceProcessActivity.class);
                    intent.putExtra("CONDITION_INFO", new Gson().toJson(new AcceptanceConditionHelper()));
                    intent.putExtra("ITEM_INFO", new Gson().toJson(new AcceptanceInfoHelper()));
                    intent.putExtra("TYPE", 0);
                    startActivityForResult(intent, 0);

                    MaterialReceivingModeActivity
                    intent.setClass(LoginActivity.this,MaterialReceivingModeActivity.class);
                    intent.putExtra("TYPE",MaterialReceivingActivity.REGULAR);
                    intent.setClass(LoginActivity.this, PrintLabelActivity.class);
                     startActivity(intent);
                    intent.setClass(LoginActivity.this, MaterialSendingActivity.class);
                    intent.setClass(LoginActivity.this,
                            ShipmentPalletLabelPrintActivity.class);
                    intent.putExtra("TYPE", MaterialSendingActivity.QUERY_TYPE_SEND);
                    intent.setClass(LoginActivity.this,TransferSubinventoryLocatorActivity.class);
                    startActivity(intent);*/
                    return;
                }

                if (txtName.getText().toString().trim().equals("") || txtPWD.getText().toString().trim().equals("")) {
                    Toast.makeText(LoginActivity.this, getString(R.string.account_password_is_not_null), Toast.LENGTH_SHORT).show();

                    if (txtName.getText().toString().equals("")) {
                        txtName.requestFocus();
                    } else {
                        txtPWD.requestFocus();
                    }

                    /*if (!BtPrintLabel.printTESTPO()) {//printShipmentPalletLabel1()  printTESTPO()  printTest
                        //errorInfo = getString(R.string.printLabalFailed);
                        //mConnection.setText(getString(R.string.printer_connect_error));
                        //mConnection.setTextColor(Color.RED);
                        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    }*/
                } else {
                    hideKeyboard();
                    String tMsg = "";

                    if(TextUtils.isEmpty(AppController.getSfcIp())){
                        tMsg += getString(R.string.set_SFC_IP) + "\n";
                    }

                    if(TextUtils.isEmpty(AppController.getApi_1())){
                        tMsg += getString(R.string.set_API_1) + "\n";
                    }

                    if(TextUtils.isEmpty(AppController.getApi_2())){
                        tMsg += getString(R.string.set_API_2) + "\n";
                    }

                    if(TextUtils.isEmpty(AppController.getApi_3())){
                        tMsg += getString(R.string.set_API_3);
                    }

                    if(!tMsg.isEmpty()){
                        Toast.makeText(getApplicationContext(), tMsg, Toast.LENGTH_LONG).show();
                        return;
                    }

                    login();
                }
            }
        });


        txtPWD = findViewById(R.id.input_login_password);
        txtPWD.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    //Perform action on key press
                    Log.d(TAG, "onKeyListener Enter or Action Down" + String.valueOf(v.getId()));

                    if (!TextUtils.isEmpty(txtName.getText().toString().trim()) && !TextUtils.isEmpty(txtPWD.getText().toString().trim())) {
                        hideKeyboard();
                        String tMsg = "";

                        if(TextUtils.isEmpty(AppController.getSfcIp())){
                            tMsg += getString(R.string.set_SFC_IP) + "\n";
                        }

                        if(TextUtils.isEmpty(AppController.getApi_1())){
                            tMsg += getString(R.string.set_API_1) + "\n";
                        }

                        if(TextUtils.isEmpty(AppController.getApi_2())){
                            tMsg += getString(R.string.set_API_2) + "\n";
                        }

                        if(TextUtils.isEmpty(AppController.getApi_3())){
                            tMsg += getString(R.string.set_API_3);
                        }

                        if(!tMsg.isEmpty()){
                            Toast.makeText(getApplicationContext(), tMsg, Toast.LENGTH_LONG).show();
                            return false;
                        }

                        login();
                        return true;
                    }
                }

                return false;
            }
        });

        btnLoginCancel = findViewById(R.id.button_login_cancel);
        btnLoginCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick Login Cancel", String.valueOf(v.getId()));
                txtName.setText(null);
                txtPWD.setText(null);
                txtName.requestFocus();
            }
        });

        btnConnectionSetting = findViewById(R.id.button_connection_setting);
        btnConnectionSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick ConnectionSetting " + String.valueOf(v.getId()));
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this,ConnectionSettingMActivity.class);
                //intent.setClass(LoginActivity.this,ConnectionSettingActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        btnPrinterSetting = findViewById(R.id.button_bt_printer_setting);
        btnPrinterSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick ConnectionSetting " + String.valueOf(v.getId()));
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, PrinterSettingActivity.class);
                startActivity(intent);
            }
        });

        btnBTConnect = findViewById(R.id.button_bt_connect);
        btnBTConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick btnBTConnect", String.valueOf(v.getId()));

                if (!BtPrintLabel.instance(getApplicationContext())) {
                    mConnection.setText(R.string.b_t_error);
                } else {
                    if (BtPrintLabel.connect()) {
                        Toast.makeText(getApplicationContext(), "Success！", Toast.LENGTH_SHORT).show();//"連線成功"
                        mConnection.setText("");
                    } else {
                        Toast.makeText(getApplicationContext(), "Connection Failed", Toast.LENGTH_SHORT).show();//"連線失敗"
                        mConnection.setText(R.string.b_t_error);
                    }
                }
            }
        });

        final Resources resources = this.getResources();

        btnChangeLang = findViewById(R.id.language_change);
        btnChangeLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constant.lan++;
                DisplayMetrics dm = resources.getDisplayMetrics();
                Configuration config = resources.getConfiguration();

                //应用用户选择语言
                if(Constant.lan%2==0){
                    AppController.debug("CHINESE");
                    config.locale = Locale.CHINESE;
                }else if(Constant.lan%2==1){
                    AppController.debug("ENGLISH");
                    config.locale = Locale.ENGLISH;
                }/*else if(Constant.lan%3==2){
                    AppController.debug("越南文");
                    config.locale = new Locale("vi"); // 越南文
                }*/

                resources.updateConfiguration(config, dm);
                Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        adapterLanguage = ArrayAdapter.createFromResource(LoginActivity.this, R.array.Language, R.layout.spinner_item);
        spinnerLanguage.setAdapter(adapterLanguage);
        adapterLanguage.setDropDownViewResource(R.layout.spinner_item);

        //根據目前語系設定 spinner 預設選項
        Locale currentLocale = resources.getConfiguration().locale;
        if (currentLocale.getLanguage().equals("zh")) {
            spinnerLanguage.setSelection(0);
        } else if (currentLocale.getLanguage().equals("en")) {
            spinnerLanguage.setSelection(1);
        } else if (currentLocale.getLanguage().equals("vi")) {
            spinnerLanguage.setSelection(2);
        }

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isFirstLoad) {
                    isFirstLoad = false;
                    return;
                }

                DisplayMetrics dm = resources.getDisplayMetrics();
                Configuration config = resources.getConfiguration();

                if(position == 0){ //中文
                    config.locale = Locale.CHINESE;
                }else if(position == 1){ //英文
                    config.locale = Locale.ENGLISH;
                }else if(position == 2){ //越文
                    config.locale = new Locale("vi");
                }

                resources.updateConfiguration(config, dm);
                Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                AppController.debug("spinnerLanguage.setOnItemSelectedListener onNothingSelected");
            }
        });

        btnBTDisconnect = findViewById(R.id.button_bt_disconnect);
        btnBTDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick btnBTConnect", String.valueOf(v.getId()));

                if (BtPrintLabel.disconnect()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.Disconnected_successfully), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.Failed_to_disconnect), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Get saved server setting
        /*serverInfo = Preferences.getSharedPreferences(this).getString(Preferences.PREFERENCE_NAME, null);
        AppController.debug("Get saved server setting.");

        if (serverInfo == null) {
            serverInfo = AppController.getProperties("ProdServer");
            Preferences.setString(this, Preferences.PREFERENCE_NAME, serverInfo);
        }*/

        //AppController.setServerInfo(serverInfo);

        //Get COMPANY setting
        //spinnerCompany.setSelection(Preferences.getInt(this, Preferences.COMPANY));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(Constant.ISORG){
            apiAuthHelper = new ApiAuthHelper("user1","u1111","test","","");
            connection();
            new GetApiAuth().execute(0); //GetApiAuth
            //new GetAPIAuth_SCM().execute(0); //GetAPIAuth_SCM
            //new GetAPIAuth_SCM_VN().execute(0); //GetAPIAuth_SCM_VN

            try {
                getAuthtoken();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), getString(R.string.set_SFC_IP), Toast.LENGTH_LONG).show();
            }

            addOrg(); //ORGOU專案

            /*if(TextUtils.isEmpty(AppController.getAPIService())){
                int serverip_api = Preferences.getSharedPreferences(getApplicationContext()).getInt(Preferences.SERVER_IP_API, 2);

                if(serverip_api==1 ||serverip_api==0 ){
                    AppController.setAPIService(serverip_api);
                    AppController.debug("已設定好:"+AppController.getAPIService());
                }else{
                    Toast.makeText(getApplicationContext(), "請先設定連線", Toast.LENGTH_LONG).show();
                }
            }else{
                AppController.debug("已設定好1:"+AppController.getAPIService());
                //Toast.makeText(getApplicationContext(), "請先設定連線", Toast.LENGTH_LONG).show();
            }*/

            getFortinetControl();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!TextUtils.isEmpty(serverInfo)) {
            long interval = Long.parseLong(AppController.getProperties("Update_Interval"));
            long millis = System.currentTimeMillis();
            if (millis - AppController.getLastAskTime() > interval) {
                AppController.setLastAskTime(millis);
                checkUpgrade();
            }
        }
    }

    private void connection(){
        ConnectionDetails currentConnection = ConnectionUtils.getCurrentConnectionDetails(getApplicationContext());

        if (currentConnection != null) {
            //使用當前連線資訊
            String name = currentConnection.getName();
            String ip = currentConnection.getIp();
            String port = currentConnection.getPort();
            String sfcIp = currentConnection.getSfcIp();
            String api_1 = currentConnection.getApi_1();
            String api_2 = currentConnection.getApi_2();
            String api_3 = currentConnection.getApi_3();
            String erpIp = currentConnection.getErpIp();
            AppController.debug("LoginActivity ConnectionDetails:" + currentConnection.toString());
            serverInfo=ip+":"+port;
            AppController.setServerInfo(serverInfo);
            AppController.setSfcIp(sfcIp);
            AppController.setApi_1(api_1);
            AppController.setApi_2(api_2);
            AppController.setApi_3(api_3);
            //errorInfo = AppController.getServerInfo();

            String tMsg = "";

            if(TextUtils.isEmpty(sfcIp)){
                tMsg += getString(R.string.set_SFC_IP) + "\n";
            }

            if(TextUtils.isEmpty(api_1)){
                tMsg += getString(R.string.set_API_1) + "\n";
            }

            if(TextUtils.isEmpty(api_2)){
                tMsg += getString(R.string.set_API_2) + "\n";
            }

            if(TextUtils.isEmpty(api_3)){
                tMsg += getString(R.string.set_API_3);
            }

            if(!tMsg.isEmpty()){
                Toast.makeText(this, tMsg, Toast.LENGTH_LONG).show();
            }
        }

        //textViewIp.setText("IP:"+AppController.getServerInfo());
        //textViewIp.setText("IP:"+AppController.getServerInfo()+"\n"+"sfcIp:"+AppController.getSfcIp().substring(25));

        if(!TextUtils.isEmpty(AppController.getSfcIp())&&AppController.getSfcIp().length()>=25){
            textViewIp.setText("IP:"+AppController.getServerInfo()+"\n"+"sfcIp:"+AppController.getSfcIp().substring(0,25)+"...");
        }else{
            textViewIp.setText("IP:"+AppController.getServerInfo());
        }

        //textViewIp.setText();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        AppController.debug("===onActivityResult=====");

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            serverInfo = Preferences.getSharedPreferences(this).getString(Preferences.PREFERENCE_NAME, null);
            AppController.setServerInfo(serverInfo);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtPWD.getWindowToken(), 0);
    }

    public void onBackPressed() {
        //do something here and don't write super.onBackPressed()
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    private void addOrg(){
        orgHelper = new OrgHelper();//可以塞不同OU
        //orgMap.put(86,"IC");
        //orgMap.put(287,"CN");
        //orgMap.put(288,"ENt");

        if(Constant.ISOUORG){
            new GetOuOrgInfo().execute(0);//
        }else{
            new GetOrgInfo().execute(0);
        }
    }

    private class GetOrgInfo extends AsyncTask<Integer, String, OrgHelper> {
        @Override
        protected OrgHelper doInBackground(Integer... params) {
            AppController.debug("Get Org from " + AppController.getServerInfo() + AppController.getProperties("GetOrg"));
            //publishProgress("資料下載中...");
            return orgHandler.getOrgData(orgHelper);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            AppController.debug("GetOrgInfo onProgressUpdate() " + text[0]);
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            //Things to be done while execution of long running operation is in
            //progress. For example updating ProgessDialog
        }

        @Override
        protected void onPostExecute(OrgHelper result) {
            //execution of result of Long time consuming operation //ORGOU專案
            //finalResult.setText(result);
            //txtResponseArea.setText(result);
            AppController.debug("GetOrgInfo() result json = "  + new Gson().toJson(result));

            if (result != null && result.getOrMap() != null ) {
                comOrgName.clear();
                comOrg.clear();
                orgHelper = result;
                orgMap = result.getOrMap();
                AppController.setOrgMap(result.getOrMap());

                for (Map.Entry<Integer, String> entry : AppController.getOrgMap().entrySet()) {
                    AppController.debug("登入成功後取得的 orgMap = " +entry.getKey()+" ,val = "+entry.getValue());
                    comOrgName.add(entry.getValue());
                    comOrg.add(entry.getKey());
                }

                adapterCompany.notifyDataSetChanged();

                if(Preferences.getInt(LoginActivity.this, Preferences.COMPANY) == 0){
                    //spinnerFacotry.setSelection(Preferences.getInt(LoginActivity.this,Preferences.FACTORY));
                    //如果orgMap的長度縮短成三以下，可使用以下程式
                    AppController.setOrgName(Preferences.getString(LoginActivity.this,Preferences.ORGNAME));

                    if(AppController.getOrgMap()!=null){
                        int i = 0;

                        for (Map.Entry<Integer, String> entry : AppController.getOrgMap().entrySet()) {
                            AppController.debug("=====> orgMap = " +entry.getKey()+" ,val = "+entry.getValue());

                            if(AppController.getOrgName().trim().equals(entry.getValue().trim())){
                                spinnerFacotry.setSelection(i);
                                break;
                            }

                            i++;
                        }
                    }

                    if(adapterFactory==null){
                        if(Constant.ISORG){
                            adapterFactory = new ArrayAdapter<>(LoginActivity.this, R.layout.spinner_item,comOrgName);//ORGOU專案
                        }else{
                            adapterFactory = new ArrayAdapter<>(LoginActivity.this, R.layout.spinner_item, getResources().getStringArray(R.array.senao_factories));
                        }
                    }

                    adapterFactory.notifyDataSetChanged();
                }

                /*if (orgHelper.getIntRetCode() == ReturnCode.OK) {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                } else {
                    if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                        mConnection.setText(getString(R.string.connect_error));
                    } else {
                        mConnection.setText(getString(R.string.db_return_error));
                    }

                    errorInfo = orgHelper.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                }*/
            } else {
                mConnection.setText("ORG"+getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class GetOuOrgInfo extends AsyncTask<Integer, String, OrgHelper> {
        @Override
        protected OrgHelper doInBackground(Integer... params) {
            AppController.debug("Get Ou Org from " + AppController.getServerInfo() + AppController.getProperties("GetOuOrg"));
            //publishProgress("資料下載中...");
            return orgHandler.getOuOrgData(orgHelper);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        @Override
        protected void onPostExecute(OrgHelper result) {
            AppController.debug("GetOuOrgInfo() result json = "  + new Gson().toJson(result));

            if (result != null && result.getList() != null ) {
                com.clear();
                orgNameList.clear();
                ouOrgHelperList = result.getList();
                set = new LinkedHashSet();

                ouOrgMap = new LinkedHashMap<>();
                Map<Integer,String> mapData = null;
                List<Map<Integer,String>> listData = null;

                for(OuOrgHelper helper : ouOrgHelperList){
                    mapData = new LinkedHashMap<>();

                    if(ouOrgMap.get(helper.getOuId())==null){
                        mapData.put(helper.getOrgId(), helper.getOrgEName());
                        listData = new ArrayList<>();
                        listData.add(mapData);
                        ouOrgMap.put(helper.getOuId(), listData);
                    }else{
                        mapData.put(helper.getOrgId(), helper.getOrgEName());
                        listData = ouOrgMap.get(helper.getOuId());
                        listData.add(mapData);
                        ouOrgMap.put(helper.getOuId(), listData);
                    }
                }

                for (Map.Entry<Integer, List<Map<Integer,String>>> entry : ouOrgMap.entrySet()) {
                    AppController.debug("==>初始畫面取得的 ou = " +entry.getKey()+" ,val = "+entry.getValue());
                }

                for(OuOrgHelper helper : ouOrgHelperList){
                    set.add(helper.getOuName());
                }

                com.addAll(set);
                adapterCompany.notifyDataSetChanged();

                //取第一個公司別的名稱
                for(OuOrgHelper ouOrgHelper: ouOrgHelperList){
                    if(ouOrgHelper.getOuName().equals(com.get(0))){
                        orgNameList.add(ouOrgHelper.getOrgEName());
                    }
                }

                adapterFactory = new ArrayAdapter<>(LoginActivity.this, R.layout.spinner_item,orgNameList);
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
            } else {
                mConnection.setText("OUORG"+getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        }
    }

    private class GetApiAuth extends AsyncTask<Integer, String, String> {
        @Override
        protected String doInBackground(Integer... params) {
            AppController.debug("GetAPIAuth:"  + AppController.getSfcIp() + "/auth");
            publishProgress("APIAuth "+getString(R.string.processing));
            String result = "";

            //創建 HttpClient 對象
            HttpClient httpClient = new DefaultHttpClient();
            httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

            String jason =new Gson().toJson(apiAuthHelper);
            AppController.debug("GetApiAuth jason: "+ jason);

            //創建 HttpClient 對象
            try {
                //創建 HttpPost 對象並設置 URL
                HttpPost httpPost = new HttpPost(AppController.getSfcIp() + "/auth");
                //設置請求的 JSON 數據
                StringEntity stringEntity = new StringEntity(jason);
                httpPost.setEntity(stringEntity);
                //設置 Content-Type 頭部
                httpPost.addHeader("Accept","application/json");
                httpPost.addHeader("Content-Type","application/json; charset=utf8");
                //執行 POST 請求
                HttpResponse response = httpClient.execute(httpPost);
                //獲取響應
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    //讀取響應內容
                    result = EntityUtils.toString(entity);
                    //在這裡處理返回的結果
                }

                Thread.sleep(1000);
                AppController.debug("GetAPIAuth: "+ result);
            } catch (Exception e) {
                e.printStackTrace();
                AppController.debug("GetAPIAuth: "+ e.getMessage());
                return e.getMessage();
            }

            httpClient.getConnectionManager().shutdown();
            return result;
        }

        @Override
        protected void onProgressUpdate(String... text) {
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        @Override
        protected void onPostExecute(String result) {
            AppController.debug("GetApiAuth() result json = "  + new Gson().toJson(result));

            if (result != null  ) {
                //apiToken = new Gson().toJson(result);;

                try {
                    apiToken = new JSONObject(result).getString("token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                AppController.debug("apiToken = "  + apiToken);
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
            } else {
                mConnection.setText("APIAUTH"+getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "APIAUTH ERROR API";
            }
        }
    }

    private class GetAPIAuth_SCM extends AsyncTask<Integer, String, String> {
        @Override
        protected String doInBackground(Integer... params) {
            AppController.debug("scmportal GetAPIAuth:"  + AppController.getProperties("GetAPIAuth_SCM"));
            publishProgress("APIAuth "+getString(R.string.processing));
            String result = "";

            //創建 HttpClient 對象
            HttpClient httpClient = new DefaultHttpClient();
            httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

            String jason =new Gson().toJson(apiAuthHelper);
            AppController.debug("GetAPIAuth_SCM jason: "+ jason);

            //創建 HttpClient 對象
            try {
                //創建 HttpPost 對象並設置 URL
                HttpPost httpPost = new HttpPost(AppController.getProperties("GetAPIAuth_SCM"));
                //設置請求的 JSON 數據
                StringEntity stringEntity = new StringEntity(jason);
                httpPost.setEntity(stringEntity);
                //設置 Content-Type 頭部
                httpPost.addHeader("Accept","application/json");
                httpPost.addHeader("Content-Type","application/json; charset=utf8");
                //執行 POST 請求
                HttpResponse response = httpClient.execute(httpPost);
                //獲取響應
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    //讀取響應內容
                    result = EntityUtils.toString(entity);
                    //在這裡處理返回的結果
                }

                Thread.sleep(1000);
                AppController.debug("scmportal GetAPIAuth: "+ result);
            } catch (Exception e) {
                e.printStackTrace();
                AppController.debug("scmportal GetAPIAuth: "+ e.getMessage());
                return e.getMessage();
            }

            httpClient.getConnectionManager().shutdown();
            return result;
        }

        @Override
        protected void onProgressUpdate(String... text) {
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        @Override
        protected void onPostExecute(String result) {
            AppController.debug("GetAPIAuth_SCM() result json = "  + new Gson().toJson(result));

            if (result != null  ) {
                try {
                    apiToken_scm = new JSONObject(result).getString("token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                AppController.debug("apiToken_scm = "  + apiToken_scm);
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
            } else {
                mConnection.setText("APIAUTH"+getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "APIAUTH ERROR SCM";
            }
        }
    }

    private class GetAPIAuth_SCM_VN extends AsyncTask<Integer, String, String> {
        @Override
        protected String doInBackground(Integer... params) {
            AppController.debug("VN scmportal GetAPIAuth:"  + AppController.getProperties("GetAPIAuth_SCM_VN"));
            publishProgress("APIAuth "+getString(R.string.processing));
            String result = "";

            //創建 HttpClient 對象
            HttpClient httpClient = new DefaultHttpClient();
            httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

            String jason =new Gson().toJson(apiAuthHelper);
            AppController.debug("GetAPIAuth_SCM_VN jason: "+ jason);

            //創建 HttpClient 對象
            try {
                //創建 HttpPost 對象並設置 URL
                HttpPost httpPost = new HttpPost(AppController.getProperties("GetAPIAuth_SCM_VN"));
                //設置請求的 JSON 數據
                StringEntity stringEntity = new StringEntity(jason);
                httpPost.setEntity(stringEntity);
                //設置 Content-Type 頭部
                httpPost.addHeader("Accept","application/json");
                httpPost.addHeader("Content-Type","application/json; charset=utf8");
                //執行 POST 請求
                HttpResponse response = httpClient.execute(httpPost);
                //獲取響應
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    //讀取響應內容
                    result = EntityUtils.toString(entity);
                    //在這裡處理返回的結果
                }

                Thread.sleep(1000);
                AppController.debug("VN scmportal GetAPIAuth: "+ result);
            } catch (Exception e) {
                e.printStackTrace();
                AppController.debug("scmportal GetAPIAuth: "+ e.getMessage());
                return e.getMessage();
            }

            httpClient.getConnectionManager().shutdown();
            return result;
        }

        @Override
        protected void onProgressUpdate(String... text) {
            mConnection.setText(text[0]);
            mConnection.setTextColor(Color.WHITE);
            mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        }

        @Override
        protected void onPostExecute(String result) {
            AppController.debug("GetAPIAuth_SCM_VN() result json = "  + new Gson().toJson(result));

            if (result != null  ) {
                try {
                    apiToken_scm_vn = new JSONObject(result).getString("token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                AppController.debug("apiToken_scm_vn = "  + apiToken_scm_vn);
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
            } else {
                mConnection.setText("APIAUTH"+getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "APIAUTH ERROR SCM VN";
            }
        }
    }

    private void checkUpgrade() {
        apk = new ApkHelper();
        apk.setStrVersionCode(String.valueOf(versionCode));

        Upgrade task = new Upgrade(this, new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                apk = (ApkHelper) result;
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";
                downloadUrl = "http://" + serverInfo
                        + AppController.getProperties("Download")
                        //+ apk.getStrFileName(); //20260121 Ann Mark
                        + AppController.getProperties("ApkName"); //20260121 Ann Add
                AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
                dialog.setCancelable(false);
                dialog.setTitle(R.string.Update);
                dialog.setMessage(getString(R.string.New_version) + apk.getStrVersionName() + getString(R.string.do_u_want_to_download));
                dialog.setIcon(android.R.drawable.ic_dialog_alert);
                dialog.setPositiveButton(getString(R.string.button_confirm_setting),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.dismiss();

                                if (hasExternalStoragePermission()) {
                                    downloadApk(downloadUrl);
                                } else {
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                        new AlertDialog.Builder(LoginActivity.this)
                                                .setMessage(R.string.we_need_permission)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        requestExternalStoragePermission();
                                                    }
                                                })
                                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                })
                                                .show();
                                    } else {
                                        requestExternalStoragePermission();
                                    }
                                }
                            }
                        });
                dialog.setNegativeButton(R.string.later,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.dismiss();
                            }
                        });
                dialog.show();
            }

            @Override
            public void onError(BasicHelper result) {
                if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                    mConnection.setText(getString(R.string.connect_error));
                    errorInfo = result.getStrErrorBuf();
                    mConnection.setTextColor(Color.RED);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                } else {
                    mConnection.setText("");
                    mConnection.setTextColor(Color.WHITE);
                    mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                    errorInfo = "";
                }
            }

            @Override
            public void onFailure() {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        });

        task.execute(apk);
    }

    private void downloadApk(String url) {
        DownloadApk task = new DownloadApk(this, new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                Toast.makeText(getApplicationContext(), getString(R.string.file_downloaded), Toast.LENGTH_SHORT).show();
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppController.getProperties("ApkName"));
                installApk(file);
            }

            @Override
            public void onError(BasicHelper result) {
                Toast.makeText(getApplicationContext(), getString(R.string.download_error) + result.getStrErrorBuf(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure() {

            }
        });

        task.execute(url);
    }

    private void login() {
        AppController.debug("loginActivity => login" );
        userInfoHelper = new UserInfoHelper();
        userInfoHelper.setUserName(txtName.getText().toString().trim());
        userInfoHelper.setPassword(txtPWD.getText().toString().trim());

        if(Constant.ISOUORG){
            if(ouOrgHelperList !=null){
                Map<Integer,String> tmpMap = new LinkedHashMap<>();

                for(OuOrgHelper ouOrgHelper: ouOrgHelperList){
                    if(ouOrgHelper.getOuName().equals(com.get(spinnerCompany.getSelectedItemPosition()))){
                        tmpMap.put(ouOrgHelper.getOrgId(),ouOrgHelper.getOrgEName());
                        userInfoHelper.setOu(ouOrgHelper.getOuId());
                        AppController.debug("loginActivity ouOrgMap=> " +ouOrgMap.get(ouOrgHelper.getOuId()).get(spinnerFacotry.getSelectedItemPosition()).keySet().toArray()[0]);

                        userInfoHelper.setOrg((int)ouOrgMap.get(ouOrgHelper.getOuId()).get(spinnerFacotry.getSelectedItemPosition()).keySet().toArray()[0]);
                        userInfoHelper.setOrgName("");
                        //userInfoHelper.setOrg(comOrg.get(spinnerFacotry.getSelectedItemPosition()));

                        //if(ouOrgHelper.getOuId()==307){
                            //userInfoHelper.setOu(306);
                        //}
                    }
                }

                AppController.setOrgMap(tmpMap);

                for (Map.Entry<Integer, String> entry : AppController.getOrgMap().entrySet()) {
                    AppController.debug("==> orgMap = " +entry.getKey()+" ,val = "+entry.getValue());
                    comOrgName.add(entry.getValue());
                    comOrg.add(entry.getKey());
                }
            }
        }else {
            userInfoHelper.setOu(Integer.parseInt(getResources().getStringArray(R.array.companies_ou)[spinnerCompany.getSelectedItemPosition()]));

            if (spinnerCompany.getSelectedItemPosition() == COMPANY.SENAO_NETWORKS.ordinal()) {
                if(Constant.ISORG){
                    userInfoHelper.setOrg(comOrg.get(spinnerFacotry.getSelectedItemPosition()));//ORGOU專案  //{"288":"ENT","86":"IC","287":"CN"}
                }else{
                    userInfoHelper.setOrg(Integer.parseInt(getResources().getStringArray(R.array.senao_org)[spinnerFacotry.getSelectedItemPosition()]));
                }
            } else if (spinnerCompany.getSelectedItemPosition() == COMPANY.ENRACK.ordinal()) {
                userInfoHelper.setOrg(Integer.parseInt(getResources().getStringArray(R.array.enrack_org)[spinnerFacotry.getSelectedItemPosition()]));
            } else {
                userInfoHelper.setOrg(Integer.parseInt(getResources().getStringArray(R.array.eng_org)[spinnerFacotry.getSelectedItemPosition()]));
            }
        }

        Login task = new Login(this, new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                userInfoHelper = (UserInfoHelper) result;
                mConnection.setText("");
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
                errorInfo = "";

                AppController.setCompany(spinnerCompany.getSelectedItemPosition());
                AppController.setFactory(spinnerFacotry.getSelectedItemPosition());

                if(Constant.ISOUORG){
                    AppController.setOu(userInfoHelper.getOu());
                    AppController.setOrg(userInfoHelper.getOrg());
                    AppController.setOrgName(AppController.getOrgName(userInfoHelper.getOrg()));
                }else{
                    AppController.setOu(Integer.parseInt(getResources().getStringArray(R.array.companies_ou)[spinnerCompany.getSelectedItemPosition()]));

                    if (spinnerCompany.getSelectedItemPosition() == COMPANY.SENAO_NETWORKS.ordinal()) {
                        if(Constant.ISORG){
                            AppController.setOrg(comOrg.get(spinnerFacotry.getSelectedItemPosition()));//ORGOU專案
                            AppController.setOrgName(AppController.getOrgName(comOrg.get(spinnerFacotry.getSelectedItemPosition())));
                        }else{
                            AppController.setOrg(Integer.parseInt(getResources().getStringArray(R.array.senao_org)[spinnerFacotry.getSelectedItemPosition()]));
                        }
                    } else if (spinnerCompany.getSelectedItemPosition() == COMPANY.ENRACK.ordinal()) {
                        AppController.setOrg(Integer.parseInt(getResources().getStringArray(R.array.enrack_org)[spinnerFacotry.getSelectedItemPosition()]));
                    } else {
                        AppController.setOrg(Integer.parseInt(getResources().getStringArray(R.array.eng_org)[spinnerFacotry.getSelectedItemPosition()]));
                    }
                }

                AppController.setUser(userInfoHelper);
                AppController.debug("userInfoHelper=>"+userInfoHelper);

                //儲存到Preferences
                Preferences.setInt(LoginActivity.this, Preferences.COMPANY, AppController.getCompany());
                Preferences.setInt(LoginActivity.this, Preferences.FACTORY, AppController.getFactory());
                Preferences.setInt(LoginActivity.this, Preferences.ORG, AppController.getOrg());
                Preferences.setInt(LoginActivity.this, Preferences.OU, AppController.getOu());
                Preferences.setString(LoginActivity.this, Preferences.ORGNAME, AppController.getOrgName());

                AppController.debug("AppController.getOrgName() NOW=>"+AppController.getOrgName()+" ,AppController.getOrg()=>"+AppController.getOrg());
                //AppController.debug("AppController.getOrgName() CN=>"+AppController.getOrgName(287));
                //AppController.debug("AppController.getOrgName() ENT=>"+AppController.getOrgName(288));

                Preferences.setString(LoginActivity.this, Preferences.USER, userInfoHelper.getUserName());
                Preferences.setString(LoginActivity.this, Preferences.PWD, userInfoHelper.getPassword());
                Preferences.setString(LoginActivity.this, Preferences.PALLET_YN, userInfoHelper.getPalletYN());

                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, MenuActivity.class);
                startActivity(intent);
            }

            @Override
            public void onError(BasicHelper result) {
               //testModel();//要測試且要撇開燈入畫面用

                if (result.getIntRetCode() == ReturnCode.CONNECTION_ERROR) {
                    mConnection.setText(getString(R.string.connect_error));
                } else {
                    mConnection.setText(getString(R.string.db_return_error));
                }

                errorInfo = result.getStrErrorBuf();
                mConnection.setTextColor(Color.RED);
                mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
            }

            @Override
            public void onFailure() {
                mConnection.setText(getString(R.string.can_not_connection));
                mConnection.setTextColor(Color.WHITE);
                mConnection.setBackgroundColor(Color.RED);
                errorInfo = "";
            }
        });

        task.execute(userInfoHelper);
    }

    private void testModel(){
        //userInfoHelper = (UserInfoHelper) result;
        mConnection.setText("");
        mConnection.setTextColor(Color.WHITE);
        mConnection.setBackgroundColor(Color.rgb(164, 199, 57));
        errorInfo = "";
        AppController.setCompany(spinnerCompany.getSelectedItemPosition());
        AppController.setFactory(spinnerFacotry.getSelectedItemPosition());
        AppController.setOu(Integer.parseInt(getResources().getStringArray(R.array.companies_ou)[spinnerCompany.getSelectedItemPosition()]));

        if (spinnerCompany.getSelectedItemPosition() == COMPANY.SENAO_NETWORKS.ordinal()) {
            AppController.setOrg(Integer.parseInt(getResources().getStringArray(R.array.senao_org)[spinnerFacotry.getSelectedItemPosition()]));
        } else if (spinnerCompany.getSelectedItemPosition() == COMPANY.ENRACK.ordinal()) {
            AppController.setOrg(Integer.parseInt(getResources().getStringArray(R.array.enrack_org)[spinnerFacotry.getSelectedItemPosition()]));
        } else {
            AppController.setOrg(Integer.parseInt(getResources().getStringArray(R.array.eng_org)[spinnerFacotry.getSelectedItemPosition()]));
        }

        AppController.setUser(userInfoHelper);

        Preferences.setInt(LoginActivity.this, Preferences.COMPANY, AppController.getCompany());
        Preferences.setInt(LoginActivity.this, Preferences.FACTORY, AppController.getFactory());
        Preferences.setInt(LoginActivity.this, Preferences.ORG, AppController.getOrg());
        Preferences.setInt(LoginActivity.this, Preferences.OU, AppController.getOu());

        Preferences.setString(LoginActivity.this, Preferences.USER, userInfoHelper.getUserName());
        Preferences.setString(LoginActivity.this, Preferences.PWD, userInfoHelper.getPassword());
        Preferences.setString(LoginActivity.this, Preferences.PALLET_YN, userInfoHelper.getPalletYN());

        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, MenuActivity.class);
        startActivity(intent);
    }

    //20260121 Ann Mark
    /*private void installApk(File file) {
        if (file != null && file.exists()) {
            try {
                Intent promptInstall = new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.fromFile(file),
                                "application/vnd.android.package-archive");
                startActivity(promptInstall);
            } catch (Exception ex) {
                Intent promptInstall = new Intent(Intent.ACTION_VIEW);
                promptInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                promptInstall.setDataAndType(FileProvider.getUriForFile(this,
                        this.getPackageName() + ".provider",
                        file), "application/vnd.android.package-archive");
                startActivity(promptInstall);
            }
        }
    }*/

    //20260121 Ann Add
    private void installApk(File file) {
        if (file == null || !file.exists()) return;

        Uri apkUri;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apkUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    file
            );
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            apkUri = Uri.fromFile(file);
        }

        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        startActivity(intent);
    }

    private boolean hasExternalStoragePermission() {
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestExternalStoragePermission() {
        //requests WRITE_EXTERNAL_STORAGE permission
        ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_WRITE_EXTERNAL_STORAGE_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ASK_WRITE_EXTERNAL_STORAGE_PERMISSIONS) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission was granted!
                downloadApk(downloadUrl);
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(LoginActivity.this)
                            .setMessage("Go to App Setting to grant permission?")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", getPackageName(), null));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void getAuthtoken() {
        AppController.debug( "=========== getAuthtoken Start ===========" );
        ApiManager apiManager = null;

        try {
            apiManager = new ApiManager(AppController.getSfcIp());
            AppController.debug( "=========== getAuthtoken Domain ===========" +AppController.getSfcIp());
        } catch (Exception e) {
            AppController.debug( "=========== getAuthtoken Exception ===========" +e);
            e.printStackTrace();
        }

        //发送POST请求到不同的动态URL
        //DataRequest dataRequest = new DataRequest("token", 123);
        String dynamicUrl = "/auth";

        apiManager.sendAuth(dynamicUrl, apiAuthHelper, new ApiClient.ApiCallback() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onSuccess(ApiResponse result) {
                // 处理成功响应
                AppController.debug( "getAuthtoken Status: " + result.getStatus());
                AppController.debug( "getAuthtoken Message: " + result.getMessage());

                try {
                    JSONObject jsonObject = new JSONObject(result.getMessage());
                    String token = jsonObject.getString("token");
                    apiToken_chose = token;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String error) {
                //处理错误响应
                AppController.debug("getAuthtoken Error: " + error);
            }
        });
    }

    private void getFortinetControl() {
//        // 使用基础URL初始化ApiManager
//        ApiManager apiManager = new ApiManager("https://api.example.com/");
//
//        // 发送POST请求到不同的动态URL
//        DataRequest dataRequest = new DataRequest("example", 123);
//        String dynamicUrl = AppController.getAPIService()+"FortinetControl";
//        apiManager.sendData01(dynamicUrl, dataRequest);
//---------------------------------------------------------------------------------------------------
//        // 使用基础URL初始化ApiManager
//        ApiManager apiManager = new ApiManager("https://api.example.com/");
//
//        // 发送POST请求到不同的动态URL
//        DataRequest dataRequest = new DataRequest("example", 123);
//        String dynamicUrl = "https://api.example.com/data";
//
//        apiManager.sendData02(dynamicUrl, dataRequest, new ApiCallback<ApiResponse>() {
//            @Override
//            public void onSuccess(ApiResponse result) {
//                // 处理成功响应
//                Log.d("MainActivity", "Status: " + result.getStatus());
//                Log.d("MainActivity", "Message: " + result.getMessage());
//            }
//
//            @Override
//            public void onError(String error) {
//                // 处理错误响应
//                Log.e("MainActivity", "Error: " + error);
//            }
//        });

//---------------------------------------------------------------------------------------------------

//        // 使用基础URL初始化ApiManager
//        ApiManager apiManager = new ApiManager("https://api.example.com/");
//
//        // 发送POST请求到不同的动态相对路径
//        DataRequest dataRequest = new DataRequest("example", 123);
//        String relativeUrl = "data"; // 相对路径
//
//        apiManager.sendData(relativeUrl, dataRequest, new ApiCallback<ApiResponse>() {
//            @Override
//            public void onSuccess(ApiResponse result) {
//                // 处理成功响应
//                Log.d("MainActivity", "Status: " + result.getStatus());
//                Log.d("MainActivity", "Message: " + result.getMessage());
//            }
//
//            @Override
//            public void onError(String error) {
//                // 处理错误响应
//                Log.e("MainActivity", "Error: " + error);
//            }
//        });

//---------------------------------------------------------------------------------------------------

//        // 使用基础URL初始化ApiManager
////        ApiManager apiManager = new ApiManager("https://api.example.com/");
////        ApiManager apiManager = new ApiManager(AppController.getAPIService()+"FortinetControl");
//        ApiManager apiManager = new ApiManager(AppController.getAPIService());
//
//        // 发送POST请求到不同的动态URL
//        DataRequest dataRequest = new DataRequest("token", 123);
//        String dynamicUrl = "FortinetControl";
//
//        apiManager.sendData(dynamicUrl, dataRequest, new ApiClient.ApiCallback() {
//            @Override
//            public void onSuccess(ApiResponse result) {
//                // 处理成功响应
//                AppController.debug( "ApiClient Status: " + result.getStatus());
//                AppController.debug( "ApiClient Message: " + result.getMessage());
//            }
//
//            @Override
//            public void onError(String error) {
//                // 处理错误响应
//                AppController.debug("ApiClient Error: " + error);
//            }
//        });
    }

    private void checkPrinterSetting() {
        if (!BtPrintLabel.isPrintNameSet(this)) {
            Intent intent = new Intent(this, PrinterSettingActivity.class);
            startActivityForResult(intent, REQUEST_PRINTER_SETTING);
            Toast.makeText(getApplicationContext(), getString(R.string.set_printer_name), Toast.LENGTH_LONG).show();
            return;
        }

        if (!BtPrintLabel.isBtEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Toast.makeText(getApplicationContext(), getString(R.string.open_bt), Toast.LENGTH_LONG).show();
            return;
        }

        if (!BtPrintLabel.instance(getApplicationContext())) {
            Intent intentOpenBluetoothSettings = new Intent();
            intentOpenBluetoothSettings.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivityForResult(intentOpenBluetoothSettings, REQUEST_BLUETOOTH_SETTINGS);
            Toast.makeText(getApplicationContext(), getString(R.string.cant_find_printer), Toast.LENGTH_LONG).show();
            return;
        }

        if (BtPrintLabel.connect()) {
            Toast.makeText(getApplicationContext(), getString(R.string.bt_connect_ok), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.not_connect_btprinter), Toast.LENGTH_LONG).show();
        }
    }
}
