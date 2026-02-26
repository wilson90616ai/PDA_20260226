package com.senao.warehouse.handler;

import com.senao.warehouse.AppController;
import com.senao.warehouse.database.ApiAuthHelper;
import com.senao.warehouse.database.BakeHelper;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.UserInfoHelper;
import com.senao.warehouse.http.HttpClient;
import com.senao.warehouse.http.ServerResponse;
import com.senao.warehouse.util.ReturnCode;

public class APIHandler extends  BasicHandler{

    public ApiAuthHelper getApiAuth(ApiAuthHelper user) {
        try {
            //AppController.debug("Login from " + AppController.getServerInfo() + AppController.getProperties("GetAPIAuth"));
            HttpClient httpClient = new HttpClient();
            ServerResponse response = httpClient.doPostByOtherHost(gson.toJson(user), AppController.getProperties("GetAPIAuth"));
            AppController.debug("Server response:" + response.getCode());
            AppController.debug("Server response:" + response.getMessage());
            AppController.debug("Server response:" + response.getData());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getMessage(), ApiAuthHelper.class); //這裡有改
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            BasicHelper result = new BasicHelper();
            result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            result.setStrErrorBuf(e.getMessage());
            return user;
        }

        return null;
    }

    public BakeHelper startBake(BakeHelper bake, String method) {
        try {
            //AppController.debug("Login from " + AppController.getServerInfo() + AppController.getProperties("GetAPIAuth"));
            HttpClient httpClient = new HttpClient();
            ServerResponse response = httpClient.doPostByOtherHost(gson.toJson(bake),
                    //AppController.getProperties("wmsportal2") + method);
                    AppController.getSfcIp() + "/invoke" + AppController.getApi_1() + "?sCode=" + method); //20260213 Ann Edit
            AppController.debug("Server response:" + response.getCode());
            AppController.debug("Server response:" + response.getMessage());
            AppController.debug("Server response:" + response.getData());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getMessage(), BakeHelper.class); //這裡有改
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            BasicHelper result = new BasicHelper();
            result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            result.setStrErrorBuf(e.getMessage());
            return bake;
        }

        return null;
    }
}
