package com.senao.warehouse.handler;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.http.HttpClient;
import com.senao.warehouse.http.ServerResponse;
import com.senao.warehouse.util.ReturnCode;

public class BasicHandler {
    protected HttpClient httpClient;
    protected Gson gson;

    public BasicHandler() {
        httpClient = new HttpClient();
        gson = new Gson();
    }

    public BasicHelper getResult(BasicHelper helper, String route, Class<BasicHelper> classOfT) {
        try {
            AppController.debug("Call " + AppController.getServerInfo() + AppController.getProperties(route));
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties(route));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), classOfT);
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
