package com.jerey.asynclib;

public interface Callback<T> {

    /**
     * 当加载完成后回调，加载完毕的事后处理
     */
    public void onCallback(final T callbackValue);
}  