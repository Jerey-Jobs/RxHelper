package com.jerey.asynclib;

public interface Observer<T> {

    /**
     * 当加载完成后回调，加载完毕的事后处理
     */
    void onObserve(final T callbackValue);

    void onError(final Exception e);
}  