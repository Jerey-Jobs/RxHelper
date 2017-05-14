package com.jerey.asynclib;

/**
 * 山寨callable,哈哈
 *
 * @param <V>
 */
public interface Subscriber<V> {
    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    V call() throws Exception;
}