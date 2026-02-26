package com.senao.warehouse.apiservice;

import static com.senao.warehouse.ui.LoginActivity.apiToken;
import static com.senao.warehouse.ui.LoginActivity.apiToken_chose;

import com.senao.warehouse.AppController;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class ApiClient {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client;
    private String baseUrl;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;

        //設置 HttpLoggingInterceptor 來打印請求詳細信息
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        //添加 Authorization 攔截器
        Interceptor authInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                //Request.Builder builder = originalRequest.newBuilder().header("Authorization", "Bearer " + apiToken);
                Request.Builder builder = originalRequest.newBuilder().header("Authorization", "Bearer " + apiToken_chose);
                Request newRequest = builder.build();
                AppController.debug("ApiClient request headers: " + newRequest.headers());
                return chain.proceed(newRequest);
            }
        };

        this.client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .build();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void post(String url, JSONObject json, final ApiCallback callback) {
        AppController.debug("ApiClient post: " + baseUrl + url);
        AppController.debug("ApiClient json: " + json.toString());
        RequestBody body = RequestBody.create(JSON, json.toString());
        Request request = new Request.Builder()
                .url(baseUrl + url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                AppController.debug("ApiClient onFailure: " + e.getMessage());
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                AppController.debug("ApiClient onResponse: " + response.code());

                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    AppController.debug("ApiClient responseBody: " + responseBody);

                    try {
                        //JSONObject responseJson = new JSONObject(responseBody);
                        String status = "OK";
                        //String message = responseJson.getString("message");
                        //String message = responseBody;
                        ApiResponse apiResponse = new ApiResponse(status, responseBody);
                        callback.onSuccess(apiResponse);
                    } catch (Exception e) {
                        AppController.debug("ApiClient JSON parse error: " + e.getMessage());
                        callback.onError(e.getMessage());
                    }
                } else {
                    AppController.debug("ApiClient onResponse error: " + response.message());
                    callback.onError(response.message());
                }
            }
        });
    }

    public interface ApiCallback {
        void onSuccess(ApiResponse result);
        void onError(String error);
    }

    public static class ApiResponse {
        private String status;
        private String message;

        public ApiResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}
