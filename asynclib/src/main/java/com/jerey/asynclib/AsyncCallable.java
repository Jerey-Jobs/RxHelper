package com.jerey.asynclib;

public interface AsyncCallable<T> {

    /**
     * Computes a result asynchronously, return values and exceptions are to be handled through the callbacks.
     * This method is expected to return almost immediately, after starting a {@link Thread} or similar.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    public void call(final Callback<T> pCallback, final Callback<Exception> pExceptionCallback);
}  