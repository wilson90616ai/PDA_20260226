package com.senao.warehouse.asynctask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;

import com.senao.warehouse.AppController;
import com.senao.warehouse.R;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.util.ReturnCode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadApk extends AsyncTask<String, Integer, BasicHelper> {
    private AsyncResponse<BasicHelper> mCallBack;
    private WeakReference<Activity> mWeakActivity;
    private ProgressDialog mProgressDialog;
    private PowerManager.WakeLock mWakeLock;

    public DownloadApk(Activity activity, AsyncResponse<BasicHelper> callback) {
        mWeakActivity = new WeakReference<>(activity);
        mCallBack = callback;
    }

    @Override
    protected void onPreExecute() {
        final Activity activity = mWeakActivity.get();

        if (activity != null) {
            PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire(3000);
            mProgressDialog = new ProgressDialog(activity);
            mProgressDialog.setMessage(activity.getString(R.string.download_new_version)); //下載新版apk中
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消(Cancel)", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DownloadApk.this.cancel(true);
                }
            });
            mProgressDialog.show();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        //if we get here, length is known, now set indeterminate to false
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgress(progress[0]);
    }

    @Override
    protected BasicHelper doInBackground(String... sUrl) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        BasicHelper helper = new BasicHelper();

        try {
            URL url = new URL(sUrl[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            //expect HTTP 200 OK, so we don't mistakenly save error report
            //instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                helper.setIntRetCode(ReturnCode.CONNECTION_ERROR);
                helper.setStrErrorBuf("Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
                return helper;
            }

            //this will be useful to display download percentage
            //might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            //download the file
            input = connection.getInputStream();
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppController.getProperties("ApkName"));
            //sUrl[0].substring(sUrl[0].lastIndexOf('=') + 1)
            output = new FileOutputStream(file, false);
            byte[] data = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                //allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return null;
                }

                total += count;

                //publishing the progress....
                if (fileLength > 0) // only if total length is known
                    publishProgress((int) (total * 100 / fileLength));

                output.write(data, 0, count);
            }
        } catch (Exception e) {
            helper.setIntRetCode(ReturnCode.SYSTEM_ERROR);
            helper.setStrErrorBuf(e.toString());
        } finally {
            try {
                if (output != null)
                    output.close();

                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }

        return helper;
    }

    @Override
    protected void onPostExecute(BasicHelper result) {
        Activity activity = mWeakActivity.get();

        if (activity == null) {
            return;
        }

        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        if (mCallBack != null) {
            if (result == null) {
                mCallBack.onFailure();
            } else {
                if (result.getIntRetCode() == ReturnCode.OK) {
                    mCallBack.onSuccess(result);
                } else {
                    mCallBack.onError(result);
                }
            }
        }
    }
}
