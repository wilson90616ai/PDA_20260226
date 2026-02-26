package com.senao.warehouse.util;

import com.senao.warehouse.ui.LoginActivity;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;

public class OkHttpUtil {

    private static final OkHttpClient client = new OkHttpClient();

    // GET请求
    public static void getRequest(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(callback);
    }

    // POST请求
    public static void postRequest(String url, String json, Callback callback) {
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), json);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer "+ LoginActivity.apiToken)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }
}

