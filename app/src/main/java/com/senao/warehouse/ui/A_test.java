package com.senao.warehouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.ApiAuthHelper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class A_test extends Activity {
    //TEST
    private EditText text_value, text_value2; //資料_Barcode, 資料_QRCode
    private Button button_next_page, button_alert_value; //下一頁, 顯示資料
    private String text_value_nextpage;

    //產線測試用
    private EditText text_value3; //取得的資料
    private Button button_get_data, button_alert_value2; //取得資料, 顯示資料
    private ApiAuthHelper apiAuthHelper;
    public static String apiToken;
    private String errorInfo = "";

    private static final int REQUEST_CODE_A_TEST2 = 123456; // 自定义请求代码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_test);

        //TEST
        text_value = findViewById(R.id.text_value);
        text_value2 = findViewById(R.id.text_value2);
        button_next_page = findViewById(R.id.button_last_page);
        button_alert_value = findViewById(R.id.button_alert_value);

        button_next_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick TEST:", v.getResources().getResourceEntryName(v.getId()));

                Intent intent = new Intent(A_test.this, A_test2.class);
                intent.putExtra("text_value_lastpage", text_value.getText().toString().trim());
                startActivityForResult(intent, REQUEST_CODE_A_TEST2);
            }
        });

        button_alert_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick TEST:", v.getResources().getResourceEntryName(v.getId()));

                new AlertDialog.Builder(A_test.this)
                        .setTitle("TEST")
                        .setMessage("資料 A:" + text_value.getText().toString().trim())
                        .setPositiveButton("確定", null)
                        .show();
            }
        });


        //產線測試用
        apiAuthHelper = new ApiAuthHelper("user1","u1111","test","","");
        text_value3 = findViewById(R.id.text_value3);
        button_get_data = findViewById(R.id.button_get_data);
        button_alert_value2 = findViewById(R.id.button_alert_value2);

        button_get_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick 產線測試用:", v.getResources().getResourceEntryName(v.getId()));
                new A_test.GetApiAuth01().execute(0);
            }
        });

        button_alert_value2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick 產線測試用:", v.getResources().getResourceEntryName(v.getId()));

                new AlertDialog.Builder(A_test.this)
                        .setTitle("產線測試用")
                        .setMessage("取得的資料:" + text_value3.getText().toString().trim())
                        .setPositiveButton("確定", null)
                        .show();
            }
        });
    }

    //接收 A_test2 返回的数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       if (requestCode == REQUEST_CODE_A_TEST2 && resultCode == RESULT_OK && data != null) {
            text_value_nextpage = data.getStringExtra("text_value_nextpage");
            text_value2.setText(text_value_nextpage); //下一頁資料
        }
    }

    //取得TOKEN
    private class GetApiAuth01 extends AsyncTask<Integer, String, String> {
        @Override
        protected String doInBackground(Integer... params) {

            AppController.debug("apiportal GetAPIAuth:"  + AppController.getProperties("GetAPIAuth"));
            publishProgress("APIAuth "+getString(R.string.processing));

            String result = "";
            // 創建 HttpClient 對象
            HttpClient httpClient = new DefaultHttpClient();
            httpClient.getConnectionManager().getSchemeRegistry().register(
                    new Scheme("https", SSLSocketFactory.getSocketFactory(), 443)
            );

            String jason =new Gson().toJson(apiAuthHelper);
            AppController.debug("jason: "+ jason);
            // 創建 HttpClient 對象
            try {
                // 創建 HttpPost 對象並設置 URL
                HttpPost httpPost = new HttpPost(AppController.getProperties("GetAPIAuth"));
                // 設置請求的 JSON 數據
                StringEntity stringEntity = new StringEntity(jason);
                httpPost.setEntity(stringEntity);
                // 設置 Content-Type 頭部
                httpPost.addHeader("Accept","application/json");
                httpPost.addHeader("Content-Type","application/json; charset=utf8");
                // 執行 POST 請求
                HttpResponse response = httpClient.execute(httpPost);
                // 獲取響應
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // 讀取響應內容
                    result = EntityUtils.toString(entity);
                    // 在這裡處理返回的結果
                }
                Thread.sleep(1000);
                AppController.debug("apiportal GetAPIAuth:: "+ result);
            } catch (Exception e) {
                e.printStackTrace();
                AppController.debug("apiportal GetAPIAuth:: "+ e.getMessage());
                return e.getMessage();
            } finally {
                httpClient.getConnectionManager().shutdown();
            }

            return result;
        }

        @Override
        protected void onProgressUpdate(String... text) {

        }

        @Override
        protected void onPostExecute(String result) {
            AppController.debug("GetApiAuth() result json = "  + new Gson().toJson(result));

            if (result != null  ) {
               try {
                   apiToken = new JSONObject(result).getString("token");
                   text_value3.setText(apiToken);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                AppController.debug("apiToken = " + apiToken);
                errorInfo = "apiToken = " + apiToken;
            } else {
                errorInfo = "APIAUTH ERROR API";
            }
        }
    }
}
