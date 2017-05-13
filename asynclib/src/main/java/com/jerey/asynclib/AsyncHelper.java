package com.jerey.asynclib;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import java.util.concurrent.Callable;

/**
 * 这是一个异步帮助类, 实现是用的AsyncTask, 不过我不喜欢Async的调用方式, 因此封装成了此类的链式调用方式.<br>
 * 不过,若你工程中有RxJava,那也没这类什么事情了.可惜公司代码不用RxJava啊.
 * <p>
 * // demo代码:
 * //        AsyncHelper.callable(new Callable<String>() {   //子线程执行
 * //            @Override
 * //            public String call() throws Exception {
 * //                Log.w("xiamin call", "thread id" + Thread.currentThread().getId());
 * //                return "hello world";
 * //            }
 * //        }).callback(new Callback<String>() {            //回调UI线程执行
 * //            @Override
 * //            public void onCallback(String pCallbackValue) {
 * //                Log.i("xiamin onCallback", "thread id" + Thread.currentThread().getId());
 * //                Log.i("xiamin onCallback", pCallbackValue);
 * //            }
 * //        }).execute();                                    // 执行
 * </p>
 */
public class AsyncHelper {
    private static final String TAG = "AsyncHelper";

    /**
     * 有处理进度条的AsnycTask
     *
     * @param pContext           进度dialog持有的context对象
     * @param pTitle             进度dialog标题
     * @param pMessage
     * @param pCallable
     * @param pCallback
     * @param pExceptionCallback
     * @param pCancelable
     * @param <T>
     */
    public static <T> void doAsyncWithProgress(final Context pContext,
                                               final CharSequence pTitle,
                                               final CharSequence pMessage,
                                               final Callable<T> pCallable,
                                               final Callback<T> pCallback,
                                               final Callback<Exception> pExceptionCallback,
                                               final boolean pCancelable) {
        new AsyncTask<Void, Void, T>() {
            private ProgressDialog mPD;
            private Exception mException = null;

            @Override
            public void onPreExecute() {
                this.mPD = ProgressDialog.show(pContext, pTitle, pMessage, true, pCancelable);
                if (pCancelable) {
                    Log.i(TAG, "pCancelable: " + pCancelable);
                    this.mPD.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(final DialogInterface pDialogInterface) {
                            if (pExceptionCallback != null) {
                                pExceptionCallback.onCallback(new CancelledException());
                            }
                            pDialogInterface.dismiss();
                        }
                    });
                }
                super.onPreExecute();
            }

            @Override
            public T doInBackground(final Void... params) {
                try {
                    return pCallable.call();
                } catch (final Exception e) {
                    this.mException = e;
                }
                return null;
            }

            @Override
            public void onPostExecute(final T result) {
                try {
                    this.mPD.dismiss();
                } catch (final Exception e) {
                    Log.e("Error", e.toString());
                }
                if (this.isCancelled()) {
                    this.mException = new CancelledException();
                }
                if (this.mException == null) {
                    pCallback.onCallback(result);
                } else {
                    if (pExceptionCallback == null) {
                        if (this.mException != null)
                            Log.e("Error", this.mException.toString());
                    } else {
                        pExceptionCallback.onCallback(this.mException);
                    }
                }
                super.onPostExecute(result);
            }
        }.execute((Void[]) null);
    }


    /**
     * @param pContext
     * @param pTitleResID
     * @param pCallable
     * @param pCallback
     * @param pExceptionCallback
     * @param <T>
     */
    public static <T> void doProgressAsync(final Context pContext,
                                           final int pTitleResID,
                                           final ProgressCallable<T> pCallable,
                                           final Callback<T> pCallback,
                                           final Callback<Exception> pExceptionCallback) {
        new AsyncTask<Void, Integer, T>() {
            private ProgressDialog mPD;
            private Exception mException = null;

            @Override
            public void onPreExecute() {
                this.mPD = new ProgressDialog(pContext);
                this.mPD.setTitle(pTitleResID);
                this.mPD.setIcon(android.R.drawable.ic_menu_save);
                this.mPD.setIndeterminate(false);
                this.mPD.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                this.mPD.show();
                super.onPreExecute();
            }

            @Override
            public T doInBackground(final Void... params) {
                try {
                    return pCallable.call(new IProgressListener() {
                        @Override
                        public void onProgressChanged(final int pProgress) {
                            onProgressUpdate(pProgress);
                        }
                    });
                } catch (final Exception e) {
                    this.mException = e;
                }
                return null;
            }

            @Override
            public void onProgressUpdate(final Integer... values) {
                this.mPD.setProgress(values[0]);
            }

            @Override
            public void onPostExecute(final T result) {
                try {
                    this.mPD.dismiss();
                } catch (final Exception e) {
                    Log.e("Error", e.getLocalizedMessage());  
                    /* Nothing. */
                }
                if (this.isCancelled()) {
                    this.mException = new CancelledException();
                }
                if (this.mException == null) {
                    pCallback.onCallback(result);
                } else {
                    if (pExceptionCallback == null) {
                        Log.e("Error", this.mException.getLocalizedMessage());
                    } else {
                        pExceptionCallback.onCallback(this.mException);
                    }
                }
                super.onPostExecute(result);
            }
        }.execute((Void[]) null);
    }


    /**
     * 最简单的回调.实现链式调用的效果
     *
     * @param callable
     * @param callback
     * @param <T>
     */
    public static <T> void doAsync(final Callable<T> callable, final Callback<T> callback) {
        new AsyncTask<Void, Void, T>() {
            @Override
            protected T doInBackground(Void... params) {
                try {
                    return callable.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(T t) {
                super.onPostExecute(t);
                callback.onCallback(t);
            }
        }.execute((Void[]) null);
    }

    /**
     * 子线程执行部分
     *
     * @param callable
     * @param <T>
     * @return
     */
    public static <T> AsyncBuilder<T> callable(final Callable<T> callable) {
        return new AsyncBuilder<T>(callable);
    }


    public static class AsyncBuilder<T> {
        private Callable<T> callable;
        private Callback<T> callback;
        private Context context;
        private String title;
        private String hintMessage;
        private Callback<Exception> exceptionCallback = null;
        private boolean cancelable = false;

        /**
         * 设置有进度界面显示模式
         *
         * @param context
         * @param title
         * @param hintMessage
         * @return
         */
        public AsyncBuilder<T> setDialogMode(Context context, String title, String hintMessage) {
            this.context = context;
            this.title = title;
            this.hintMessage = hintMessage;
            return this;
        }

        /**
         * 在发生用户点击取消时或者其他异常时的异常回调
         *
         * @param exceptionCallback
         * @return回调
         */
        public AsyncBuilder<T> setExceptionCallback(Callback<Exception> exceptionCallback) {
            this.exceptionCallback = exceptionCallback;
            return this;
        }

        /**
         * 设置进度dialog是否可以点击其他地方时自动消失,默认为false, 若设置了, 同时设置了异常回调时, 异常回调将会被回调
         *
         * @param cancelable
         * @return
         */
        public AsyncBuilder<T> setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        /**
         * 子线程代码
         *
         * @param callable
         * @return
         */
        public AsyncBuilder<T> callable(Callable<T> callable) {
            this.callable = callable;
            return this;
        }

        /**
         * 主线程回调
         *
         * @param callback
         * @return
         */
        public AsyncBuilder<T> callback(Callback<T> callback) {
            this.callback = callback;
            return this;
        }

        public AsyncBuilder(Callable<T> callable) {
            this.callable = callable;
        }

        public AsyncBuilder() {
        }

        /**
         * 执行,callable将会在一个线程池执行, callback将会在主线程回调.
         *
         * @param <T>
         */
        public <T> void execute() {
            if (context == null) {
                AsyncHelper.doAsync(callable, callback);
            } else if (context != null && title != null) {
                doAsyncWithProgress(context,
                        title, hintMessage, callable,
                        callback, exceptionCallback,
                        cancelable
                );
            }
        }

    }

    public static class CancelledException extends Exception {
        private static final long serialVersionUID = -78123211381435595L;
    }
}  