package com.senao.warehouse.handler;

import com.google.gson.Gson;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.RmaBoxListInfoHelper;
import com.senao.warehouse.database.RmaPalletInfoHelper;
import com.senao.warehouse.database.RmaPalletListHelper;
import com.senao.warehouse.http.HttpClient;
import com.senao.warehouse.http.ServerResponse;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;

public class RmaHandler {
    private HttpClient httpClient;
    private Gson gson;

    public RmaHandler() {
        httpClient = new HttpClient();
        gson = new Gson();
    }

    public RmaBoxListInfoHelper getPalletInfo(RmaBoxListInfoHelper info) {
        try {
            ServerResponse response = httpClient.doPost(gson.toJson(info), AppController.getProperties("GetRmaPalletInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), RmaBoxListInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            info = new RmaBoxListInfoHelper();
            info.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            info.setStrErrorBuf(e.getMessage());
            return info;
        }

        return null;
    }

    public RmaBoxListInfoHelper getRmaCartonInfo(RmaBoxListInfoHelper info) {
        try {
            ServerResponse response = httpClient.doPost(gson.toJson(info), AppController.getProperties("GetRmaCartonInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), RmaBoxListInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            info = new RmaBoxListInfoHelper();
            info.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            info.setStrErrorBuf(e.getMessage());
            return info;
        }

        return null;
    }

    public BasicHelper doDeleteTempInfo(RmaPalletInfoHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("ClearPalletInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), RmaPalletInfoHelper.class);
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

    public RmaPalletListHelper getPalletList(RmaPalletListHelper info) {
        try {
            ServerResponse response = httpClient.doPost(gson.toJson(info), AppController.getProperties("GetRmaPalletList"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), RmaPalletListHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            info = new RmaPalletListHelper();
            info.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            info.setStrErrorBuf(e.getMessage());
            return info;
        }

        return null;
    }

    public BasicHelper doConfirmShipDate(RmaPalletInfoHelper info) {
        try {
            ServerResponse response = httpClient.doPost(gson.toJson(info), AppController.getProperties("ConfirmShipDate"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), RmaPalletListHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            BasicHelper result = new BasicHelper();
            result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            result.setStrErrorBuf(e.getMessage());
            return info;
        }

        return null;
    }
}
