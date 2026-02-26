package com.senao.warehouse.asynctask;

import com.senao.warehouse.database.BasicHelper;

public interface AsyncResponse<T> {
    public void onSuccess(BasicHelper result);
    public void onError(BasicHelper result);
    public void onFailure();
}
