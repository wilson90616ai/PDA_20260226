package com.senao.warehouse.apiservice;

import com.senao.warehouse.database.ApiAuthHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class ApiManager {
    private ApiClient apiClient;

    public ApiManager(String baseUrl) {
        this.apiClient = new ApiClient(baseUrl);
    }

    public void sendData(String endpoint, DataRequest dataRequest, final ApiClient.ApiCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("key1", dataRequest.getKey1());
            json.put("key2", dataRequest.getKey2());
            apiClient.post(endpoint, json, callback);
        } catch (JSONException e) {
            callback.onError(e.getMessage());
        }
    }

    public void sendAuth(String endpoint, ApiAuthHelper apiAuthHelper, final ApiClient.ApiCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("username", apiAuthHelper.getUsername());
            json.put("password", apiAuthHelper.getPassword());
            json.put("company", apiAuthHelper.getCompany());
            json.put("authType", apiAuthHelper.getAuthType());
            apiClient.post(endpoint, json, callback);
        } catch (JSONException e) {
            callback.onError(e.getMessage());
        }
    }

    public static class DataRequest {
        private String key1;
        private int key2;

        public DataRequest(String key1, int key2) {
            this.key1 = key1;
            this.key2 = key2;
        }

        public String getKey1() {
            return key1;
        }

        public int getKey2() {
            return key2;
        }
    }

    public static String getDomain(String urlString) throws MalformedURLException {
        URL url = new URL(urlString);
        return url.getProtocol() + "://" + url.getHost() + "/";
    }
}
