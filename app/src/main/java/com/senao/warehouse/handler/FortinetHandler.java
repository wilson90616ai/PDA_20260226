package com.senao.warehouse.handler;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.database.ApiAuthHelper;
import com.senao.warehouse.http.HttpClient;
import com.senao.warehouse.http.ServerResponse;
import com.senao.warehouse.util.ReturnCode;

public class FortinetHandler {
    private HttpClient httpClient;
    private Gson gson = new Gson();

    public String getFortinetQcApproval(String helper) {
        try {
            httpClient = new HttpClient();
            String response = httpClient.doGetByFortinet("", AppController.getProperties("FortinetQcApproval")+helper);

            return response;
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            AppController.debug("Error to connect the server. " + e);
            String result = e.getMessage();
            return result;
        }
    }
}
