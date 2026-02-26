package com.senao.warehouse.handler;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.database.AcceptanceConditionHelper;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.OverTakeHelper;
import com.senao.warehouse.http.HttpClient;
import com.senao.warehouse.http.ServerResponse;
import com.senao.warehouse.util.ReturnCode;

public class OverTakeHandler {
    private HttpClient httpClient;
    private Gson gson = new Gson();

    public BasicHelper getOvertakeData(OverTakeHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("GetOvertakeData"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), OverTakeHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            AppController.debug("Error to connect the server. " + e);
            BasicHelper result = new BasicHelper();
            result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            result.setStrErrorBuf(e.getMessage());
            return result;
        }

        return null;
    }
}
