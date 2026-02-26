package com.senao.warehouse.util;

public interface AsyncTaskCompleteListener<T> {
    void onTaskComplete(T result);
}
