package com.senao.warehouse.asynctask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.senao.warehouse.R;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.handler.UpgradeHandler;
import com.senao.warehouse.util.ReturnCode;

import java.lang.ref.WeakReference;

public class Upgrade extends AsyncTask<BasicHelper, Void, BasicHelper> {
    private AsyncResponse<BasicHelper> mCallBack;
    private WeakReference<Activity> mWeakActivity;
    private ProgressDialog mProgressDialog;
    private UpgradeHandler mUpgradeHandler;

    public Upgrade(Activity activity, AsyncResponse<BasicHelper> callback) {
        mWeakActivity = new WeakReference<>(activity);
        mCallBack = callback;
        mUpgradeHandler = new UpgradeHandler();
    }

    @Override
    protected void onPreExecute() {
        final Activity activity = mWeakActivity.get();

        if (activity != null) {
            mProgressDialog = ProgressDialog.show(activity, activity.getString(R.string.chk_update), activity.getString(R.string.holdon), true);
        }
    }

    @Override
    protected BasicHelper doInBackground(BasicHelper... params) {
        return mUpgradeHandler.doCheckNewVersion(params[0]);
    }

    @Override
    protected void onPostExecute(BasicHelper result) {
        Activity activity = mWeakActivity.get();

        if (activity == null) {
            return;
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
