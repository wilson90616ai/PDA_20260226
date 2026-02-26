package com.senao.warehouse.handler;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.OverTakeHelper;
import com.senao.warehouse.database.StockInInfoHelper;
import com.senao.warehouse.database.SubinventoryInfoHelper;
import com.senao.warehouse.http.HttpClient;
import com.senao.warehouse.http.ServerResponse;
import com.senao.warehouse.util.ReturnCode;

public class OrgHandler {
    private HttpClient httpClient;
    private Gson gson = new Gson();

    public OrgHelper getOuOrgData(OrgHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("GetOuOrg"));
            AppController.debug("Server getOrgData response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), OrgHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            AppController.debug("Error to connect the server. " + e);
            OrgHelper result = new OrgHelper();
            result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            result.setStrErrorBuf(e.getMessage());
            return result;
        }

        return null;
    }

    public OrgHelper getOrgData(OrgHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("GetOrg"));
            AppController.debug("Server getOrgData response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), OrgHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            AppController.debug("Error to connect the server. " + e);
            OrgHelper result = new OrgHelper();
            result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            result.setStrErrorBuf(e.getMessage());
            return result;
        }

        return null;
    }

    public OrgHelper getNotAllowedSubs(OrgHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("GetNotAllowedSubs"));
            AppController.debug("Server getOrgData response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), OrgHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            AppController.debug("Error to connect the server. " + e);
            OrgHelper result = new OrgHelper();
            result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            result.setStrErrorBuf(e.getMessage());
            return result;
        }

        return null;
    }

    public OrgHelper getNotAllowedSubs02(OrgHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("GetNotAllowedSubs02"));
            AppController.debug("Server getOrgData response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), OrgHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            AppController.debug("Error to connect the server. " + e);
            OrgHelper result = new OrgHelper();
            result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            result.setStrErrorBuf(e.getMessage());
            return result;
        }

        return null;
    }

    public OrgHelper getInStockSubs(OrgHelper stockInfo) {
        try {
            AppController.debug("Get StockInfo from " + AppController.getServerInfo() + AppController.getProperties("GetInStockSubs"));
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(stockInfo), AppController.getProperties("GetInStockSubs"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), OrgHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            OrgHelper result = new OrgHelper();
            result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            result.setStrErrorBuf(e.getMessage());
            return result;
        }

        return null;
    }

    public BasicHelper getInStockSubs(BasicHelper stockInfo) {
        try {
            AppController.debug("Get StockInfo from " + AppController.getServerInfo() + AppController.getProperties("GetInStockSubs")); //select secondary_inventory_name from INV.mtl_secondary_inventories s where attribute1 like'%01%' and disable_date is null and ORGANIZATION_ID = ?
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(stockInfo), AppController.getProperties("GetInStockSubs"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), SubinventoryInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            BasicHelper result = new BasicHelper();
            result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            result.setStrErrorBuf(e.getMessage());
            return result;
        }

        return null;
    }
}
