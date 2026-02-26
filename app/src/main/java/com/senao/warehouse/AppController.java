package com.senao.warehouse;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;

import com.senao.warehouse.database.ConnectionDetails;
import com.senao.warehouse.database.DeliveryInfoHelper;
import com.senao.warehouse.database.UserInfoHelper;
import com.senao.warehouse.util.ConnectionUtils;
import com.senao.warehouse.util.Preferences;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class AppController extends Application {

    private static final String PROPERTIES_FILE = "warehouse.properties";
    private static final String APISERVICE_TEST = "https://apiportal02.senao.com/MES/invoke9?sCode="; //20260213 Ann Check:沒有使用
    private static final String APISERVICE_FORMAL = "https://scmportal.senao.com/MES/invoke3?sCode="; //20260213 Ann Check:沒有使用

    private static int where=2;//0 正式區， 1 測試區

    private static long lastAskTime;
    private static int org;
    private static String orgName;
    private static int ou;
    private static int company;
    private static int factory;
    private static Properties prop;
    private static UserInfoHelper user;
    private static String serverInfo;
    private static DeliveryInfoHelper dnInfo;
    private static AssetManager assetManager;
    private static List<Integer> orgList;
    private static List<String> orgListS;
    private static Map<Integer,String> orgMap = null ;

    private static String name = "";
    private static String ip = "";
    private static String port = "";
    private static String sfcIp = "";
    private static String api_1 = "";
    private static String api_2 = "";
    private static String api_3 = "";
    private static String erpIp = "";
    private static String receivingOrg = "";


    public static long getLastAskTime() {
        return lastAskTime;
    }

    public static void setLastAskTime(long lastAskTime) {
        AppController.lastAskTime = lastAskTime;
    }

    public static String getSfcIp() {
        return sfcIp;
    }

    public static void setSfcIp(String sfcIp) {
        AppController.sfcIp = sfcIp;
    }

    public static String getApi_1() {
        return api_1;
    }

    public static void setApi_1(String api_1) {
        AppController.api_1 = api_1;
    }

    public static String getApi_2() {
        return api_2;
    }

    public static void setApi_2(String api_2) {
        AppController.api_2 = api_2;
    }

    public static String getApi_3() {
        return api_3;
    }

    public static void setApi_3(String api_3) {
        AppController.api_3 = api_3;
    }

    public static String getAPIService() {

//        Preferences.getSharedPreferences(this).getString(Preferences.SERVER_PORT_FORMAL, null);

        if(where==0){
            return APISERVICE_FORMAL;

        }else if(where==1){
            return APISERVICE_TEST;

        }

        return "";
    }

    public static void setAPIService(int where) {
        AppController.where=where;



    }



    public static int getOrg() {
        return org;
    }


    public static int getOrg(String name) {
        for (Map.Entry<Integer, String> entry : orgMap.entrySet()) {
            if(name.equals(entry.getValue())){
                return entry.getKey();
            }
        }
        return 0;
    }

    public static void setOrg(int org) {
        AppController.org = org;
    }

    public static String getOrgName(int org) {
        if(orgMap == null)
            return "";
        else
            return orgMap.get(org);


    }
    public static String getOrgName() {
        if(orgName == null)
            return "";
        else
            return orgName;
    }

    public static void setOrgName(String orgName) {
        AppController.orgName = orgName;
    }

    public static int getOu() {
        return ou;
    }

    public static void setOu(int ou) {
        AppController.ou = ou;
    }

    public static int getCompany() {
        return company;
    }

    public static void setCompany(int company) {
        AppController.company = company;
    }

    public static int getFactory() {
        return factory;
    }

    public static void setFactory(int factory) {
        AppController.factory = factory;
    }

    public static String getServerInfo() {


//        if(!TextUtils.isEmpty(ip)&&!TextUtils.isEmpty(port)){
//
//            return ip+":"+port;
//        }


        return serverInfo;
    }

    public static void setServerInfo(String serverInfo) {
        AppController.serverInfo = serverInfo;
    }

    public static Map<Integer, String> getOrgMap() {
        return orgMap;
    }

    public static List<Integer> getOrgList() {
        orgList = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : AppController.getOrgMap().entrySet()) {
            orgList.add(entry.getKey());
        }

        return orgList;
    }

    public static List<String> getOrgNameList() {
        orgListS = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : AppController.getOrgMap().entrySet()) {
            orgListS.add(entry.getValue());
        }

        return orgListS;
    }

    public static void setOrgMap(Map<Integer, String> orgMap) {
        AppController.orgMap = orgMap;
    }

    public static String getProperties(String name) {
        if (prop == null) {
            prop = new Properties();
            try {
                prop.load(new InputStreamReader(assetManager.open(PROPERTIES_FILE), "UTF-8"));
            } catch (Exception e) {
                debug("Load properties file error! " + e.getMessage());
                e.printStackTrace();
            }
        }
        return prop.getProperty(name);
    }

    public static DeliveryInfoHelper getDnInfo() {
        return dnInfo;
    }

    public static void setDnInfo(DeliveryInfoHelper dnInfo) {
        AppController.dnInfo = dnInfo;
    }

    public static UserInfoHelper getUser() {
        return user;
    }

    public static void setUser(UserInfoHelper user) {
        AppController.user = user;
    }

    public static void debug(String str) {
        if (getProperties("Test_Mode").toLowerCase().equals("true")) {
            final String TAG = "com.senao.warehouse";
            Log.d(TAG, str);
        }
    }

    public static void setReceivingOrg(String orgString) {
        receivingOrg = orgString;
    }

    public static String getReceivingOrg() {
        return receivingOrg;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        assetManager = getAssets();
        setCompany(Preferences.getInt(this, Preferences.COMPANY));
        setFactory(Preferences.getInt(this, Preferences.FACTORY));
        setOrg(Preferences.getInt(this, Preferences.ORG));
        setOu(Preferences.getInt(this, Preferences.OU));
        setOrgName(Preferences.getString(this, Preferences.ORGNAME));
        // Get saved server setting
//        String serverInfo = Preferences.getSharedPreferences(this).getString(
//                Preferences.PREFERENCE_NAME, null);
//        if (serverInfo == null) {
//            serverInfo = getProperties("ProdServer");
//            Preferences.setString(this, Preferences.PREFERENCE_NAME, serverInfo);
//        }
//        setServerInfo(serverInfo);

        if (!TextUtils.isEmpty(Preferences.getString(AppController.this, Preferences.USER))) {
            UserInfoHelper userInfoHelper = new UserInfoHelper();
            userInfoHelper.setUserName(Preferences.getString(AppController.this, Preferences.USER));
            userInfoHelper.setPassword(Preferences.getString(AppController.this, Preferences.PWD));
            userInfoHelper.setPalletYN(Preferences.getString(AppController.this, Preferences.PALLET_YN));
            setUser(userInfoHelper);
        }



        ConnectionDetails currentConnection = ConnectionUtils.getCurrentConnectionDetails(getApplicationContext());
        if (currentConnection != null) {
            // 使用當前連線資訊
            name = currentConnection.getName();
            ip = currentConnection.getIp();
            port = currentConnection.getPort();
            sfcIp = currentConnection.getSfcIp();
            api_1 = currentConnection.getApi_1();
            api_2 = currentConnection.getApi_2();
            api_3 = currentConnection.getApi_3();
            erpIp = currentConnection.getErpIp();

            debug("AppController ConnectionDetails:"+currentConnection.toString());
            String serverInfo=ip+":"+port;
            setServerInfo(serverInfo);
        }else{
            debug("AppController ConnectionDetails(null)");
            serverInfo = getProperties("ProdServer");
            setServerInfo(serverInfo);
        }

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }



}
