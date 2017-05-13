package com.jerey.asynclib;

public interface ProgressCallable<T> {
    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @param pProgressListener
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    public T call(final IProgressListener pProgressListener) throws Exception;
}