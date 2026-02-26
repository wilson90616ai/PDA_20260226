package com.senao.warehouse.util;

import android.os.AsyncTask;

import com.senao.warehouse.AppController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyAsyncTask extends AsyncTask<String, Void, String> {


    private AsyncTaskCompleteListener<String> callback;

    public MyAsyncTask(AsyncTaskCompleteListener<String> callback) {
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... params) {
        String urlString = params[0];
        String jsonPayload = params[1];
        return postData(urlString, jsonPayload);
    }

    @Override
    protected void onPostExecute(String result) {
        if (callback != null) {
            callback.onTaskComplete(result);
        }
    }

    private String postData(String urlString, String jsonPayload) {
        // 同样的POST请求代码，省略...
        String response = HttpUtil.postData(urlString, jsonPayload);



        return response;
    }
}

