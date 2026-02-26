package com.senao.warehouse.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.adapter.SimpleExpandableListAdapterWithEmptyGroups;
import com.senao.warehouse.asynctask.AsyncResponse;
import com.senao.warehouse.asynctask.DownloadApk;
import com.senao.warehouse.asynctask.Upgrade;
import com.senao.warehouse.database.ApkHelper;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.UserInfoHelper;
import com.senao.warehouse.handler.OrgHandler;
import com.senao.warehouse.handler.OrgHelper;
import com.senao.warehouse.util.Constant;
import com.senao.warehouse.util.Preferences;
import com.senao.warehouse.util.ReturnCode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//登入後的頁面
public class MenuActivity extends Activity {


//    private static final String[] ext_enuText = AppController.getProperties("ExtMenu").split(",");
    private String[] ext_enuText = new String[]{};


//    private static final String[][] ext_sub_enuText =
//            {AppController.getProperties("SubMenu1").split(","), AppController.getProperties("SubMenu2").split(",")
//                    , AppController.getProperties("SubMenu3").split(","), AppController.getProperties("SubMenu4").split(",")
//                    , AppController.getProperties("SubMenu5").split(","), AppController.getProperties("SubMenu6").split(",")
//                    , AppController.getProperties("SubMenu7").split(","),{}, {}};
    private static String[][] ext_sub_enuText =  new String[][]{};


    private static final String[] ext_enuText_enrack = AppController.getProperties("EnrackExtMenu").split(",");
    private static final String[][] ext_sub_enuText_enrack =
            {AppController.getProperties("EnrackSubMenu1").split(","), AppController.getProperties("EnrackSubMenu2").split(","), AppController.getProperties("EnrackSubMenu3").split(","), {}};



    private static final String[] ext_enuText_smt = AppController.getProperties("SmtExtMenu").split(",");
    private static final String[][] ext_sub_enuText_smt =
            {AppController.getProperties("SmtSubMenu1").split(","), {}};



    private static final String[] ext_enuText_eng = AppController.getProperties("EngMenu").split(",");
    private static final String[][] ext_sub_enuText_eng =
            {AppController.getProperties("EngMenu1").split(","), {}};




    private static final String NAME = "NAME";
    private static final int REQUEST_CODE_ASK_WRITE_EXTERNAL_STORAGE_PERMISSIONS = 101;
    SimpleExpandableListAdapterWithEmptyGroups mAdapter;
    private int versionCode;
    private String serverInfo;
    private ExpandableListView listView;
    private String[] mainMenu;
    private String[][] subMenu;
    private String downloadUrl;
    private ApkHelper apk;
    private OrgHelper orgHelper;
    private OrgHandler orgHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        PackageManager manager = this.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            versionCode = info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        // Get saved server setting
        serverInfo = Preferences.getSharedPreferences(this).getString(
                Preferences.PREFERENCE_NAME, null);

        listView = findViewById(R.id.expandlist);
        ext_enuText = getResources().getStringArray(R.array.ext_enuText);
        ext_sub_enuText = new String[][]{getResources().getStringArray(R.array.SubMenu1), getResources().getStringArray(R.array.SubMenu2), getResources().getStringArray(R.array.SubMenu3)
                , getResources().getStringArray(R.array.SubMenu4), getResources().getStringArray(R.array.SubMenu5), getResources().getStringArray(R.array.SubMenu6)
                , getResources().getStringArray(R.array.SubMenu7), getResources().getStringArray(R.array.SubMenu8), {}, {}};
                //, getResources().getStringArray(R.array.SubMenu7), getResources().getStringArray(R.array.SubMenu8), {}, {}, getResources().getStringArray(R.array.SubMenu11)}; //XXX測試

        setData();

        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            public boolean onGroupClick(ExpandableListView elv, View view, int g, long l) {
                Log.d("onGroupClick onClick ", ext_enuText[g]);
                Log.d("onGroupClick onClick ", "g = "+g);
//                if ((AppController.getCompany() == 0 && !AppController.getUser().getUserName().toUpperCase().contains("SMT") && g == 6)
//                        || (AppController.getCompany() == 0 && AppController.getUser().getUserName().toUpperCase().contains("SMT") && g == 1)
//                        || (AppController.getCompany() == 1 && g == 3)
//                        || (AppController.getCompany() == 2 && g == 1)
//                        ) {
                if (((AppController.getOu() == 82 ||AppController.getOu() == 307)&& !AppController.getUser().getUserName().toUpperCase().contains("SMT") && g == 6)
                        || ((AppController.getOu() == 82 ||AppController.getOu() == 307)&& AppController.getUser().getUserName().toUpperCase().contains("SMT") && g == 1)
                        || (AppController.getOu() == 263 && g == 3)
                        || (AppController.getOu() == 185 && g == 1)  || (AppController.getOu() == 368)
                ) {
                    if(true){//20231214 Tim:烘烤
                        if (!listView.isGroupExpanded(g)) {
                            AppController.debug("expandGroup ");//展開List
                            listView.expandGroup(g);
                        } else {
                            AppController.debug("collapseGroup ");//關閉List
                            listView.collapseGroup(g);
                        }
                    }else{
                        AppController.setUser(null);
                        Intent intent = new Intent();
                        intent.setClass(MenuActivity.this,LoginActivity.class);
                        startActivity(intent);
                    }
                }else if(g==8&&"09.Change Organization".equals(ext_enuText[g])&&Constant.ISORG){
                    Toast.makeText(getApplicationContext(), "選擇組織(Select Org)", Toast.LENGTH_SHORT).show();

                    final String[] org = {"IC","CN","ENT"};
                    final String[] org1 = AppController.getOrgNameList().toArray(new String[0]);

                    AlertDialog.Builder dialog_list = new AlertDialog.Builder(MenuActivity.this);
                    dialog_list.setTitle("Current ORG : "+AppController.getOrgName());
                    dialog_list.setItems(org1, new DialogInterface.OnClickListener(){
                        @Override
                        //只要你在onClick處理事件內，使用which參數，就可以知道按下陣列裡的哪一個了
                        public void onClick(DialogInterface dialog, int which) {

                            AppController.setOrgName(org1[which]);
                            UserInfoHelper userInfoHelper = AppController.getUser();
                            userInfoHelper.setOrg(AppController.getOrg(AppController.getOrgName()));
                            AppController.setUser(userInfoHelper);
                            AppController.setOrg(AppController.getOrg(AppController.getOrgName()));

                            Preferences.setString(MenuActivity.this,Preferences.ORGNAME,AppController.getOrgName());
                            Preferences.setInt(MenuActivity.this, Preferences.ORG, AppController.getOrg());

                            Toast.makeText(MenuActivity.this, "當前ORG是"+org1[which] , Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog_list.show();
                }
                else {
                    if (!listView.isGroupExpanded(g)) {
                        AppController.debug("expandGroup ");//展開List
                        listView.expandGroup(g);
                    } else {
                        AppController.debug("collapseGroup ");//關閉List
                        listView.collapseGroup(g);
                    }
                }
                return true;
            }
        });

        listView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                for (int i = 0; i < mAdapter.getGroupCount(); i++) {
                    if (groupPosition != i && groupPosition != 2) {
                        listView.collapseGroup(i);
                    }
                }
            }
        });

        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            public boolean onChildClick(ExpandableListView elv, View v, int g, int c, long l) {
                Log.d("onClick ", subMenu[g][c]);
                Log.d("onClick ","g = "+g+" ,c = "+c);
                Intent intent = new Intent();
//                if (AppController.getCompany() == 0) {
                if (AppController.getOu() == 82 ||AppController.getOu() == 307 || (AppController.getOu() == 368)) {//senao=82
                    if (AppController.getUser().getUserName().toUpperCase().contains("SMT")) {
                        if (g == 0) { //SMT相關作業
                            switch (c) {
                                case 0:
                                    //材料拆包作業(Material split packing label)
                                    intent.setClass(MenuActivity.this, SmtMaterialSplitPackingActivity.class);
                                    startActivity(intent);
                                    break;
                                case 1:
                                    //材料標籤補標作業(Material reprint label)
                                    intent.setClass(MenuActivity.this, SmtPrintMaterialLabelActivity.class);
                                    startActivity(intent);
                                    break;

                                case 2:
                                    //濕敏元件狀態管制作業
//                                    Toast.makeText(getApplicationContext(), "濕敏元件狀態管制作業正在施工", Toast.LENGTH_SHORT).show();
                                    intent.setClass(MenuActivity.this, HumiditySensorStatusControlActivity.class);
                                    startActivity(intent);
                                    break;
                                case 3:
                                    //序號狀態查詢作業(含OOBA查詢作業)
//                                    Toast.makeText(getApplicationContext(), "烘烤作業正在施工", Toast.LENGTH_SHORT).show();
                                    intent.setClass(MenuActivity.this, OobaSnActivity.class);
                                    startActivity(intent);
                                    break;

                                default:
                            }
                        }
                    } else {
                        if (g == 0) { //01.出貨相關作業
                            switch (c) {
                                case 0:
                                    //出貨撿料作業
                                    intent.setClass(MenuActivity.this, ShipmentActivity.class);
                                    startActivity(intent);
                                    break;
                                case 1:
                                    //出貨核對作業
                                    intent.setClass(MenuActivity.this, ShippingVerifyMainActivity.class);
                                    startActivity(intent);
                                    break;
                                case 2:
                                    //出貨棧板標籤列印
                                    intent.setClass(MenuActivity.this, ShipmentPalletLabelPrintActivity.class);
//                                    intent.setClass(MenuActivity.this, ShipmentPalletLabelPrintTestActivity.class);
                                    startActivity(intent);
                                    break;
                                case 3:
                                    //出貨刷退作業
                                    intent.setClass(MenuActivity.this, ShipmentReturnActivity.class);
                                    startActivity(intent);
                                    break;
                                case 4:
                                    //條碼驗證作業
                                    intent.setClass(MenuActivity.this, BarcodeVerifyActivity.class);
                                    startActivity(intent);
                                    break;
                                case 5:
                                    //RMA出貨撿貨作業
                                    intent.setClass(MenuActivity.this, RmaPickingActivity.class);
                                    startActivity(intent);
                                    break;
                                case 6:
                                    //RMA出貨確認作業
                                    intent.setClass(MenuActivity.this, RmaVerifyActivity.class);
                                    startActivity(intent);
                                    break;
                                case 7:
                                    //Samsara的PO標籤列印
//                                    intent.setClass(MenuActivity.this, SamsaraPoTestActivity.class);
//                                    startActivity(intent);
                                    intent.setClass(MenuActivity.this, SamsaraPoActivity.class);
                                    startActivity(intent);
                                    break;
                                default:
                                    break;
                            }
                        } else if (g == 1) { //02.製令相關作業
                            switch (c) {
                                case 0:
                                    //製令入庫作業
                                    intent.setClass(MenuActivity.this, StockInActivity.class);
                                    startActivity(intent);
                                    break;
                                case 1:
                                    //關帳日期設定
                                    Resources res = getResources();
                                    String[] users = res.getStringArray(R.array.closed_date_users);
                                    boolean find = false;

                                    for (String user : users) {
                                        if (user.equals(AppController.getUser().getPassword())) {
                                            find = true;
                                            break;
                                        }
                                    }

                                    if (find) {
                                        intent.setClass(MenuActivity.this, StockClosedDateSetActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(getApplicationContext(), getString(R.string.You_do_not_have_permission), Toast.LENGTH_LONG).show();
                                    }

                                    break;
                                case 2:
                                    //製令發料扣帳作業
                                    intent.setClass(MenuActivity.this, MaterialSendingActivity.class);
                                    intent.putExtra("TYPE", MaterialSendingActivity.QUERY_TYPE_SEND);
                                    startActivity(intent);
                                    break;
                                case 3:
                                    //製令發料轉倉作業
                                    intent.setClass(MenuActivity.this, MaterialSendingActivity.class);
                                    intent.putExtra("TYPE", MaterialSendingActivity.QUERY_TYPE_TRANSFER);
                                    startActivity(intent);
                                    break;
//                                case 4:
//                                    //製令欠料查詢作業
//                                    intent.setClass(MenuActivity.this, MaterialSendingActivity.class);
//                                    intent.putExtra("TYPE", MaterialSendingActivity.QUERY_TYPE_LACK);
//                                    startActivity(intent);
//                                    break;
                                case 4:
                                    //製令發料檢核作業
                                    intent.setClass(MenuActivity.this, MaterialSendingVerifyActivity.class);
                                    startActivity(intent);
                                    break;
                                case 5:
                                    //轉倉轉儲位作業
                                    intent.setClass(MenuActivity.this, TransferSubinventoryLocatorActivity.class);
                                    startActivity(intent);
                                    break;
                                default:
                                    break;
                            }
                        } else if (g == 2) { //03.收料入庫相關作業
                            switch (c) {
                                case 0:
                                    //收料作業
                                    intent.setClass(MenuActivity.this, MaterialReceivingModeActivity.class); //742A59121000
                                    intent.putExtra("TYPE", MaterialReceivingActivity.REGULAR);
                                    startActivity(intent);
                                    break;
                                case 1:
                                    //外包收料作業
                                    intent.setClass(MenuActivity.this, MaterialReceivingModeActivity.class);
                                    intent.putExtra("TYPE", MaterialReceivingActivity.OUTSOURCING);
                                    startActivity(intent);
                                    break;
                                case 2:
                                    //驗收入庫作業
                                    intent.setClass(MenuActivity.this, AcceptanceActivity.class);
                                    intent.putExtra("TYPE", AcceptanceActivity.REGULAR);
                                    startActivity(intent);
                                    break;
                                case 3:
                                    //外包驗收入庫作業
                                    intent.setClass(MenuActivity.this, AcceptanceActivity.class);
                                    intent.putExtra("TYPE", AcceptanceActivity.OUTSOURCING);
                                    startActivity(intent);
                                    break;
                                case 4:
                                    //驗收入庫批次作業
                                    Toast.makeText(getApplicationContext(), getString(R.string.Incoming_inspection_and_stocking_batch_process_is_under_construction), Toast.LENGTH_SHORT).show();
                                    break;
//                                case 5:
//                                    //待驗查詢作業
//                                    Toast.makeText(getApplicationContext(), "待驗查詢作業正在施工", Toast.LENGTH_SHORT).show();
////                            intent.setClass(MenuActivity.this, WaitCheckInquiryActivity.class);
////                            startActivity(intent);
//                                    break;
                                default:
                                    break;
                            }
                        } else if (g == 3) { //04.查詢相關作業
                            switch (c) {
                                case 0:
                                    //進貨超交查詢作業
                                    intent.setClass(MenuActivity.this, OverTakeActivity.class);
                                    startActivity(intent);
                                    //Toast.makeText(getApplicationContext(), "進貨超交查詢作業正在施工\"", Toast.LENGTH_SHORT).show();
                                    break;
                                case 1:
                                    //庫存查詢作業
//                                    Toast.makeText(getApplicationContext(), "庫存查詢作業\"", Toast.LENGTH_SHORT).show();
                                    intent.setClass(MenuActivity.this, ItemOnHandActivity.class);
                                    startActivity(intent);
                                    break;
                                case 2:
                                    //製令欠料查詢作業
                                    intent.setClass(MenuActivity.this, MaterialSendingActivity.class);
                                    intent.putExtra("TYPE", MaterialSendingActivity.QUERY_TYPE_LACK);
                                    startActivity(intent);
                                    break;
                                case 3:
                                    //PO查詢及列印作業
                                    intent.setClass(MenuActivity.this, ShipmentQueryPoActivity.class);
                                    startActivity(intent);
                                    break;
                                case 4:
                                    //序號狀態查詢作業(OOBA查詢作業)
                                    intent.setClass(MenuActivity.this, OobaSnActivity.class);
                                    startActivity(intent);
//                                    Toast.makeText(getApplicationContext(), "OOBA查詢作業正在施工\"", Toast.LENGTH_SHORT).show();
//                                    intent.setClass(MenuActivity.this, OobaQueryActivity.class);
//                                    startActivity(intent);
                                    break;
                                case 5:
                                    //序號狀態查詢作業
//                                    Toast.makeText(getApplicationContext(), "序號狀態查詢作業正在施工\"", Toast.LENGTH_SHORT).show();
//                                    intent.setClass(MenuActivity.this, OobaSnActivity.class);
//                                    startActivity(intent);
                                    break;
                                default:
                                    break;
                            }
                        } else if (g == 4) {
                            switch (c) {
                                case 0:
                                    //客供材料進貨標籤列印作業
                                    intent.setClass(MenuActivity.this, CustomerMaterialReceiptsLabelActivity.class);
                                    startActivity(intent);
                                    break;
                                case 1:
                                    //進貨標籤列印作業
                                    intent.setClass(MenuActivity.this, PrintMaterialLabelActivity.class);
                                    startActivity(intent);
                                    break;
                                case 2:
                                    //客供材料拆包作業
                                    intent.setClass(MenuActivity.this, CustomerMaterialSplitPackingActivity.class);
                                    startActivity(intent);
                                    break;
                                case 3:
                                    //材料拆包作業
                                    intent.setClass(MenuActivity.this, MaterialSplitPackingActivity.class);
                                    startActivity(intent);
                                    break;
                                case 4:
                                    //客供材料標籤補標作業
                                    intent.setClass(MenuActivity.this, CustomerMaterialLabelReprintActivity.class);
                                    startActivity(intent);
                                    break;
                                case 5:
                                    //材料標籤補標作業
                                    intent.setClass(MenuActivity.this, PrintLabelActivity.class);
                                    startActivity(intent);
                                    break;
                                case 6:
                                    //發票流水號補標作業
                                    intent.setClass(MenuActivity.this, PrintInvoiceSeqActivity.class);
                                    startActivity(intent);
                                    break;
                                case 7:
                                    //製令標籤補標作業
                                    intent.setClass(MenuActivity.this, WoLabelPrintActivity.class);
                                    startActivity(intent);
                                    break;

                                case 8:
                                    //自訂標籤列印作業
                                    intent.setClass(MenuActivity.this, CustomPrintActivity.class);
                                    startActivity(intent);
                                    break;
                                default:
                                    break;
                            }
                        } else if (g == 5) {
                            switch (c) {
                                case 0:
                                    //轉倉/呆料責任歸屬單 //還在施工
                                    Toast.makeText(getApplicationContext(), getString(R.string.Transfer_Waste_material_responsibility_form_under_construction), Toast.LENGTH_SHORT).show();
//                                    intent.setClass(MenuActivity.this, TransferLocatorFormActivity.class);
//                                    startActivity(intent);
                                    break;
                                case 1:
                                    //材料耗損 //還在施工
                                    Toast.makeText(getApplicationContext(), getString(R.string.Material_wear_under_construction), Toast.LENGTH_SHORT).show();
//                                    intent.setClass(MenuActivity.this,ShipmentActivity.class);
                                    //startActivity(intent);
                                    break;
                                case 2:
                                    //一般領(退)料單 //還在施工
                                    Toast.makeText(getApplicationContext(), getString(R.string.General_receipt_return_material_list_under_construction), Toast.LENGTH_SHORT).show();
                                    //intent.setClass(MenuActivity.this,ShipmentActivity.class);
                                    //startActivity(intent);
                                    break;
                                case 3:
                                    //表單查詢 //還在施工
                                    Toast.makeText(getApplicationContext(), getString(R.string.Form_query_under_construction), Toast.LENGTH_SHORT).show();
                                    //intent.setClass(MenuActivity.this,ShipmentActivity.class);
                                    //startActivity(intent);
                                    break;
                                case 4:
                                    //簽核作業 //還在施工
                                    Toast.makeText(getApplicationContext(), getString(R.string.Sign_off_under_construction), Toast.LENGTH_SHORT).show();
                                    //intent.setClass(MenuActivity.this,ShipmentActivity.class);
                                    //startActivity(intent);
                                    break;
                                case 5:
                                    //轉倉表單觸發作業 //還在施工
                                    Toast.makeText(getApplicationContext(), getString(R.string.Rollover_form_triggers_job_under_construction), Toast.LENGTH_SHORT).show();
//                                    intent.setClass(MenuActivity.this, TransferLocatorTriggerActivity.class);
//                                    startActivity(intent);
                                    break;
                                case 6:
                                    //轉倉表單撤銷作業 //還在施工
                                    Toast.makeText(getApplicationContext(), getString(R.string.Rollover_form_cancels_job_under_construction), Toast.LENGTH_SHORT).show();
//                                    intent.setClass(MenuActivity.this, TransferLocatorRevokeActivity.class);
//                                    startActivity(intent);
                                    break;
                                case 7:
                                    //表單標籤補標作業等作業功能 //還在施工
                                    Toast.makeText(getApplicationContext(), getString(R.string.Form_label_labeling_jobs_under_construction), Toast.LENGTH_SHORT).show();
                                    //intent.setClass(MenuActivity.this,ShipmentActivity.class);
                                    //startActivity(intent);
                                    break;
                                case 8:
                                    //表單作業(領料/退料/調撥/轉倉) //還在施工
                                    Toast.makeText(getApplicationContext(), getString(R.string.Form_jobs_pick_return_transfer_move_under_construction), Toast.LENGTH_SHORT).show();
//                                    intent.setClass(MenuActivity.this,TransferRolloverActivity.class);
//                                    startActivity(intent);

                                    if ("100236".equals(AppController.getUser().getPassword())&&"WHFG01".equals(AppController.getUser().getUserName())) {
                                        //inflate目的是把自己設計xml的Layout轉成View，作用類似於findViewById，它用於一個沒有被載入或者想要動態
                                        //載入的介面，當被載入Activity後才可以用findViewById來獲得其中界面的元素
                                        LayoutInflater inflater = LayoutInflater.from(MenuActivity.this);
                                        final View view = inflater.inflate(R.layout.dialog_password, null);

                                        //語法一：new AlertDialog.Builder(主程式類別).XXX.XXX.XXX;
                                        new AlertDialog.Builder(MenuActivity.this)
                                                .setTitle("password")
                                                .setView(view)
                                                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        EditText editText = (EditText) (view.findViewById(R.id.password));

                                                        if("print".equals(editText.getText().toString())){
                                                            //印標籤
                                                            Intent intent = new Intent();
                                                            intent.setClass(MenuActivity.this, ShipmentPalletLabelPrintTestActivity.class);
                                                            startActivity(intent);
                                                        }else if("test1".equals(editText.getText().toString())){

                                                        }
//                                                        Toast.makeText(getApplicationContext(), "你的id是" +editText.getText().toString(), Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .show();
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } else if(g==6){
                            switch (c) {
                                case 0:
                                    //材料拆包作業
                                    intent.setClass(MenuActivity.this, SmtMaterialSplitPackingActivity.class);
                                    startActivity(intent);
                                    break;
                                case 1:
                                    //材料標籤補標作業
                                    intent.setClass(MenuActivity.this, SmtPrintMaterialLabelActivity.class);
                                    startActivity(intent);
                                    break;
                                case 2:
                                    //濕敏元件狀態管制作業
//                                    Toast.makeText(getApplicationContext(), "濕敏元件狀態管制作業正在施工", Toast.LENGTH_SHORT).show();
                                    intent.setClass(MenuActivity.this, HumiditySensorStatusControlActivity.class);
                                    startActivity(intent);
                                    break;
                                case 3:
                                    //烘烤作業
//                                    Toast.makeText(getApplicationContext(), "烘烤作業正在施工", Toast.LENGTH_SHORT).show();
                                    intent.setClass(MenuActivity.this, BakeActivity.class);
                                    startActivity(intent);
                                    break;
                                default:
                                    break;
                            }
                        } else if(g==7){
                            switch (c) {
                                case 0:
                                    //外包材料裝箱作業 //還在施工
                                    Toast.makeText(getApplicationContext(), getString(R.string.Outsourcing_Material_Packing_Work_under_construction), Toast.LENGTH_SHORT).show();
//                                    intent.setClass(MenuActivity.this, TransferLocatorFormActivity.class);
//                                    startActivity(intent);
                                    break;
                                case 1:
                                    //外包裝箱刷退作業 //還在施工
                                    Toast.makeText(getApplicationContext(), getString(R.string.Outsourcing_packing_return_under_construction), Toast.LENGTH_SHORT).show();
//                                    intent.setClass(MenuActivity.this,ShipmentActivity.class);
                                    //startActivity(intent);
                                    break;
                                case 2:
                                    //外包出貨撿料作業 //還在施工
                                    Toast.makeText(getApplicationContext(), getString(R.string.Outsourcing_Shipment_picking_operations_under_construction), Toast.LENGTH_SHORT).show();
                                    //intent.setClass(MenuActivity.this,ShipmentActivity.class);
                                    //startActivity(intent);
                                    break;
                                case 3:
                                    //外包出貨減料刷退作業 //還在施工
                                    Toast.makeText(getApplicationContext(), getString(R.string.Outsourcing_Shipment_picking_return_under_construction), Toast.LENGTH_SHORT).show();
                                    //intent.setClass(MenuActivity.this,ShipmentActivity.class);
                                    //startActivity(intent);
                                    break;
                                case 4:
                                    //外包棧板及外箱標籤列印 //還在施工
                                    Toast.makeText(getApplicationContext(), getString(R.string.Outsourcing_Pallet_Box_label_printing_under_construction), Toast.LENGTH_SHORT).show();
                                    //intent.setClass(MenuActivity.this,ShipmentActivity.class);
                                    //startActivity(intent);
                                    break;
                                case 5:
                                    //外包收料核對作業 //還在施工
                                    Toast.makeText(getApplicationContext(), getString(R.string.Outsourcing_Check_Receiving_operations_under_construction), Toast.LENGTH_SHORT).show();
//                                    intent.setClass(MenuActivity.this, TransferLocatorTriggerActivity.class);
//                                    startActivity(intent);
                                    break;
                                case 6:
                                    //外包材料裝箱及出貨撿料離線作業
                                    intent.setClass(MenuActivity.this, OutsourcingMaterialShipmentPickingActivity.class);
                                    startActivity(intent);
                                    break;
                                case 7:
                                    //外包裝箱及出貨撿料明細查詢 //還在施工
                                    Toast.makeText(getApplicationContext(), getString(R.string.Outsourcing_Boxing_Shipment_picking_detail_Search_under_construction), Toast.LENGTH_SHORT).show();
                                    //intent.setClass(MenuActivity.this,ShipmentActivity.class);
                                    //startActivity(intent);
                                    break;
                                case 8:
                                    //外包裝箱及出貨撿料明細下載 //還在施工
                                    Toast.makeText(getApplicationContext(), getString(R.string.Outsourcing_Boxing_Shipment_picking_detail_download_under_construction), Toast.LENGTH_SHORT).show();
                                    //intent.setClass(MenuActivity.this,ShipmentActivity.class);
                                    //startActivity(intent);
                                    break;
                            }
                        }else if(g==8) {

                        }/*XXX測試
                        else if(g==10) {
                            switch (c) {
                                case 0:
                                    //TEST
                                    intent.setClass(MenuActivity.this,
                                            A_test.class);
                                    startActivity(intent);
                                    break;
                            }
                        }*/
                        else {
                            switch (c) {
                                default:
                                    break;
                            }
                        }
                    }
//                } else if (AppController.getCompany() == 1) {
                } else if (AppController.getOu() == 263) { //恩睿
                    if (g == 0) {
                        switch (c) {
                            case 0: //出貨撿料作業
                                intent.setClass(MenuActivity.this, ShipmentActivity.class);
                                startActivity(intent);
                                break;
                            case 1: //出貨核對作業
                                intent.setClass(MenuActivity.this, ShippingVerifyMainActivity.class);
                                startActivity(intent);
                                break;
                            case 2: //出貨棧板標籤列印
                                intent.setClass(MenuActivity.this, ShipmentPalletLabelPrintActivity.class);
                                startActivity(intent);
                                break;
                            case 3: //出貨刷退作業
                                intent.setClass(MenuActivity.this, ShipmentReturnActivity.class);
                                startActivity(intent);
                                break;
                            case 4: //條碼驗證作業
                                intent.setClass(MenuActivity.this, BarcodeVerifyActivity.class);
                                startActivity(intent);
                                break;
                            case 5: //RMA出貨撿貨作業
                                intent.setClass(MenuActivity.this, RmaPickingActivity.class);
                                startActivity(intent);
                                break;
                            case 6: //RMA出貨確認作業
                                intent.setClass(MenuActivity.this, RmaVerifyActivity.class);
                                startActivity(intent);
                                break;
                            case 7: //Samsara的PO標籤列印
                                intent.setClass(MenuActivity.this, SamsaraPoActivity.class);
                                startActivity(intent);
                                break;
                            case 8: //PO查詢及列印作業
                                intent.setClass(MenuActivity.this, ShipmentQueryPoActivity.class);
                                startActivity(intent);
                                break;
                            default:
                        }
                    } else if (g == 1) {
                        switch (c) {
                            case 0: //材料標籤補標作業
                                intent.setClass(MenuActivity.this, PrintLabelActivity.class);
                                startActivity(intent);
                                break;
                            case 1: //關帳日期設定
                                Resources res = getResources();
                                String[] users = res.getStringArray(R.array.closed_date_users);
                                boolean find = false;

                                for (String user : users) {
                                    if (user.equals(AppController.getUser().getPassword())) {
                                        find = true;
                                        break;
                                    }
                                }
                                if (find) {
                                    intent.setClass(MenuActivity.this, StockClosedDateSetActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(getApplicationContext(), "您無此權限使用此功能", Toast.LENGTH_LONG).show();
                                }
                                break;
                            case 2: //製令發料扣帳作業
                                intent.setClass(MenuActivity.this, MaterialSendingActivity.class);
                                intent.putExtra("TYPE", MaterialSendingActivity.QUERY_TYPE_SEND);
                                startActivity(intent);
                                break;
                            case 3: //製令發料轉倉作業
                                intent.setClass(MenuActivity.this, MaterialSendingActivity.class);
                                intent.putExtra("TYPE", MaterialSendingActivity.QUERY_TYPE_TRANSFER);
                                startActivity(intent);
                                break;
                            case 4: //製令欠料查詢作業
                                intent.setClass(MenuActivity.this, MaterialSendingActivity.class);
                                intent.putExtra("TYPE", MaterialSendingActivity.QUERY_TYPE_LACK);
                                startActivity(intent);
                                break;
                            case 5: //製令標籤補標作業
                                intent.setClass(MenuActivity.this, WoLabelPrintActivity.class);
                                startActivity(intent);
                                break;
                            case 6: //製令發料檢核作業
                                intent.setClass(MenuActivity.this, MaterialSendingVerifyActivity.class);
                                startActivity(intent);
                                break;
                            case 7: //轉倉轉儲位作業
                                intent.setClass(MenuActivity.this, TransferSubinventoryLocatorActivity.class);
                                startActivity(intent);
                                break;
                            default:
                                break;
                        }
                    } else if (g == 2) {
                        switch (c) {
                            case 0: //進貨標籤列印作業
                                intent.setClass(MenuActivity.this, PrintMaterialLabelActivity.class);
                                startActivity(intent);
                                break;
                            case 1: //發票流水號補標作業
                                intent.setClass(MenuActivity.this, PrintInvoiceSeqActivity.class);
                                startActivity(intent);
                                break;
                            case 2: //收料作業
                                intent.setClass(MenuActivity.this, MaterialReceivingModeActivity.class);
                                intent.putExtra("TYPE", MaterialReceivingActivity.REGULAR);
                                startActivity(intent);
                                break;
                            case 3: //外包收料作業
                                intent.setClass(MenuActivity.this, MaterialReceivingModeActivity.class);
                                intent.putExtra("TYPE", MaterialReceivingActivity.OUTSOURCING);
                                startActivity(intent);
                                break;
                            case 4: //驗收入庫作業
                                intent.setClass(MenuActivity.this, AcceptanceActivity.class);
                                intent.putExtra("TYPE", AcceptanceActivity.REGULAR);
                                startActivity(intent);
                                break;
                            case 5: //外包驗收入庫作業
                                intent.setClass(MenuActivity.this, AcceptanceActivity.class);
                                intent.putExtra("TYPE", AcceptanceActivity.OUTSOURCING);
                                startActivity(intent);
                                break;
                            case 6: //驗收入庫批次作業
                                Toast.makeText(getApplicationContext(), "正在施工", Toast.LENGTH_SHORT).show();
//                                intent.setClass(MenuActivity.this, AcceptanceBatchActivity.class);
//                                intent.putExtra("TYPE", AcceptanceBatchActivity.REGULAR);
//                                startActivity(intent);
                                break;
                            case 7: //待驗查詢作業
                                Toast.makeText(getApplicationContext(), "正在施工", Toast.LENGTH_SHORT).show();
//                            intent.setClass(MenuActivity.this, WaitCheckInquiryActivity.class);
//                            startActivity(intent);
                                break;
                            default:
                                break;
                        }
                    }
//                } else {//EnGenius Networks
                } else if (AppController.getOu() == 185) {//EnGenius Networks
                    if (g == 0) {
                        switch (c) {
                            case 0: //出貨撿料作業
                                intent.setClass(MenuActivity.this, ShipmentActivity.class);
                                startActivity(intent);
                                break;
                            case 1: //出貨核對作業
                                intent.setClass(MenuActivity.this, ShippingVerifyMainActivity.class);
                                startActivity(intent);
                                break;
                            case 2: //出貨棧板標籤列印
                                intent.setClass(MenuActivity.this, ShipmentPalletLabelPrintActivity.class);
                                startActivity(intent);
                                break;
                            case 3: //出貨刷退作業
                                intent.setClass(MenuActivity.this, ShipmentReturnActivity.class);
                                startActivity(intent);
                                break;
                            case 4: //條碼驗證作業
                                intent.setClass(MenuActivity.this, BarcodeVerifyActivity.class);
                                startActivity(intent);
                                break;
                            case 5: //RMA出貨撿貨作業
                                intent.setClass(MenuActivity.this, RmaPickingActivity.class);
                                startActivity(intent);
                                break;
                            case 6: //RMA出貨確認作業
                                intent.setClass(MenuActivity.this, RmaVerifyActivity.class);
                                startActivity(intent);
                                break;
                            case 7: //Samsara的PO標籤列印
                                intent.setClass(MenuActivity.this, SamsaraPoActivity.class);
                                startActivity(intent);
                                break;
                            case 8: //PO查詢及列印作業
                                intent.setClass(MenuActivity.this, ShipmentQueryPoActivity.class);
                                startActivity(intent);
                                break;
                            default:
                                break;
                        }
                    }
                }

                return false;
            }
        });

        listView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
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

    private void setData() {
        List<Map<String, String>> groupData = new ArrayList<>();
        List<List<Map<String, String>>> childData = new ArrayList<>();

//        if (AppController.getCompany() == 0) {
        if (AppController.getOu() == 82 ||AppController.getOu() == 307) {
            if (AppController.getUser().getUserName().toUpperCase().contains("SMT")) {
                mainMenu = ext_enuText_smt;
                subMenu = ext_sub_enuText_smt;
            } else {
                mainMenu = ext_enuText;
                subMenu = ext_sub_enuText;
            }
//        } else if (AppController.getCompany() == 1) {
        } else if (AppController.getOu() == 263) {
            mainMenu = ext_enuText_enrack;
            subMenu = ext_sub_enuText_enrack;
//        } else {
        } else if (AppController.getOu() == 185) {
            mainMenu = ext_enuText_eng;
            subMenu = ext_sub_enuText_eng;
        } else{
            mainMenu = ext_enuText;
            subMenu = ext_sub_enuText;
        }

        for (int i = 0; i < mainMenu.length; i++) {
            HashMap<String, String> h = new HashMap<>();
            h.put(NAME, mainMenu[i]);
            groupData.add(h);

            if (subMenu.length > i) {
                List<Map<String, String>> list = new ArrayList<Map<String, String>>();
                for (int j = 0; j < subMenu[i].length; j++) {
                    HashMap<String, String> h2 = new HashMap<String, String>();
                    h2.put(NAME, subMenu[i][j]);
                    list.add(h2);
                }
                childData.add(list);
            }
        }

        mAdapter = new SimpleExpandableListAdapterWithEmptyGroups(
                this,
                groupData,    // groupData describes the first-level entries
                R.layout.mylistitem,    // Layout for the first-level entries
                new String[]{NAME},    // Key in the groupData maps to display
                new int[]{android.R.id.text1},        // Data under "colorName" key goes into this TextView
                childData,    // childData describes second-level entries
                R.layout.submenu,    // Layout for second-level entries
                new String[]{NAME},    // Keys in childData maps to display
                new int[]{android.R.id.text1}    // Data under the keys above go into these TextViews
        );
    }

    private void checkUpgrade() {
        apk = new ApkHelper();
        apk.setStrVersionCode(String.valueOf(versionCode));

        Upgrade task = new Upgrade(this, new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                apk = (ApkHelper) result;
                downloadUrl = "http://" + serverInfo
                        + AppController.getProperties("Download")
                        + apk.getStrFileName();
                AlertDialog.Builder dialog = new AlertDialog.Builder(MenuActivity.this);
                dialog.setCancelable(false);
                dialog.setTitle(R.string.Update);
                dialog.setMessage(getString(R.string.New_version) + apk.getStrVersionName() + getString(R.string.do_u_want_to_download));
                dialog.setIcon(android.R.drawable.ic_dialog_alert);
                dialog.setPositiveButton(getString(R.string.button_confirm_setting), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.dismiss();

                                if (hasExternalStoragePermission()) {
                                    downloadApk(downloadUrl);
                                } else {
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(MenuActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                        new AlertDialog.Builder(MenuActivity.this)
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

                dialog.setNegativeButton(R.string.later, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.dismiss();
                            }
                        });

                dialog.show();
            }

            @Override
            public void onError(BasicHelper result) {

            }

            @Override
            public void onFailure() {

            }
        });

        task.execute(apk);
    }

    private void downloadApk(String url) {
        DownloadApk task = new DownloadApk(this, new AsyncResponse<BasicHelper>() {
            @Override
            public void onSuccess(BasicHelper result) {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    Toast.makeText(getApplicationContext(),  getString(R.string.file_downloaded), Toast.LENGTH_SHORT).show();
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            AppController.getProperties("ApkName"));
                    installApk(file);
                }
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

    private void installApk(File file) {
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
    }

    private boolean hasExternalStoragePermission() {
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestExternalStoragePermission() {
        // requests WRITE_EXTERNAL_STORAGE permission
        ActivityCompat.requestPermissions(MenuActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_CODE_ASK_WRITE_EXTERNAL_STORAGE_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_WRITE_EXTERNAL_STORAGE_PERMISSIONS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted!
                    downloadApk(downloadUrl);
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(MenuActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        new AlertDialog.Builder(MenuActivity.this)
                                .setMessage("Go to App Setting to grant permission?")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
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
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }
}
