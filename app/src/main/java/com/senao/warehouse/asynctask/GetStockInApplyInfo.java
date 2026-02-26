package com.senao.warehouse.asynctask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.senao.warehouse.R;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.handler.StockInHandler;
import com.senao.warehouse.util.ReturnCode;

import java.lang.ref.WeakReference;

public class GetStockInApplyInfo extends AsyncTask<BasicHelper, Void, BasicHelper> {
    private AsyncResponse<BasicHelper> mCallBack;
    private WeakReference<Activity> mWeakActivity;
    private ProgressDialog mProgressDialog;
    private StockInHandler mHandler;

    public GetStockInApplyInfo(Activity activity, AsyncResponse<BasicHelper> callback) {
        mWeakActivity = new WeakReference<>(activity);
        mCallBack = callback;
        mHandler = new StockInHandler();
    }

    @Override
    protected void onPreExecute() {
        final Activity activity = mWeakActivity.get();

        if (activity != null) {
            mProgressDialog = ProgressDialog.show(activity, activity.getString(R.string.downloading_data), activity.getString(R.string.holdon), true);
        }
    }

    @Override
    protected BasicHelper doInBackground(BasicHelper... params) {
        return mHandler.getStockInfo(params[0]);
    }

    @Override
    protected void onPostExecute(BasicHelper result) {
        Activity activity = mWeakActivity.get();

        if (activity == null) {
            return;
        }

        if (mProgressDialog != null) {
            mProgressDialog.cancel();
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
