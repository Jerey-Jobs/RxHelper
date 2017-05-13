package com.jerey.asynclib;

public interface IProgressListener {
    /**
     * @param pProgress between 0 and 100.
     */
    public void onProgressChanged(final int pProgress);
}  