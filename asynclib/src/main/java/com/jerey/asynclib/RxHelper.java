package com.jerey.asynclib;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

/**
 * 这是一个异步帮助类, 实现是用的AsyncTask, 不过我不喜欢Async的调用方式, 因此封装成了此类的链式调用方式.<br>
 * 不过,若你工程中有RxJava,那也没这类什么事情了.可惜公司代码不用RxJava啊.
 * <p>
 * Q:RxHelper是什么:
 * A:RxHelper是一个帮助Android异步的类.由于不喜AsyncTask的写法,进行了二次封装.
 * 将其修改成了时下最流行的观察者模式的提交方式. Subsciber将会在子线程运行,Observer在主线程"观察"结果.
 * <p>
 * // demo代码:
 * //        RxHelper.fromSubsciber(new Subscriber<String>() {     //子线程执行
 * //            @Override
 * //            public String call() throws Exception {
 * //                Log.w(TAG, "call" + "thread id" + Thread.currentThread().getId());
 * //                return "hello world";
 * //            }
 * //        }).subscribe(new Observer<String>() {                 //UI回调
 * //            @Override
 * //            public void onObserve(String callbackValue) {
 * //
 * //            }
 * //
 * //            @Override
 * //            public void onError(Exception e) {
 * //
 * //            }
 * //        });
 * </p>
 */
public class RxHelper {
    private static final String TAG = "AsyncHelper";

    /**
     * 有处理进度条的AsnycTask
     *
     * @param pContext    进度dialog持有的context对象
     * @param pTitle      进度dialog标题
     * @param pMessage
     * @param pSubscriber
     * @param pObserver
     * @param pCancelable
     * @param <T>
     */
    public static <T> void doAsyncWithProgress(final Context pContext,
                                               final CharSequence pTitle,
                                               final CharSequence pMessage,
                                               final Subscriber<T> pSubscriber,
                                               final Observer<T> pObserver,
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
                            if (pObserver != null) {
                                pObserver.onError(new CancelledException());
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
                    return pSubscriber.call();
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
                    pObserver.onObserve(result);
                } else {
                    if (pObserver == null) {
                        if (this.mException != null)
                            Log.e("Error", this.mException.toString());
                    } else {
                        pObserver.onError(this.mException);
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
     * @param <T>
     */
    public static <T> void doProgressAsync(final Context pContext,
                                           final int pTitleResID,
                                           final ProgressSubscriber<T> pCallable,
                                           final Observer<T> pCallback) {
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
                    pCallback.onObserve(result);
                } else {
                    if (pCallback == null) {
                        Log.e("Error", this.mException.getLocalizedMessage());
                    } else {
                        pCallback.onError(this.mException);
                    }
                }
                super.onPostExecute(result);
            }
        }.execute((Void[]) null);
    }


    /**
     * 最简单的回调.实现链式调用的效果
     */
    public static <T> void doAsync(final Subscriber<T> subscriber,
                                   final Observer<T> callback) {
        new AsyncTask<Object, Object, T>() {
            @Override
            protected T doInBackground(Object... params) {
                try {
                    return subscriber.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(T r) {
                super.onPostExecute(r);
                callback.onObserve(r);
            }
        }.execute((Void[]) null);
    }

    /**
     * 子线程执行部分
     *
     * @param subscriber
     * @param <T>
     * @return
     */
    public static <T> AsyncBuilder<T> fromSubsciber(final Subscriber<T> subscriber) {
        return new AsyncBuilder<T>(subscriber);
    }


    public static class AsyncBuilder<T extends Object> {
        private Subscriber<T> subscriber;
        private Context context;
        private String title;
        private String hintMessage;
        private boolean cancelable = false;


        public AsyncBuilder(Subscriber<T> subscriber) {
            this.subscriber = subscriber;
        }

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
         * 设置进度dialog是否可以点击其他地方时自动消失,默认为false, 若设置了, 同时设置了异常回调时, 异常回调将会被回调
         *
         * @param cancelable
         * @return
         */
        public AsyncBuilder<T> setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

//        /**
//         * 子线程代码
//         *
//         * @param subscriber
//         * @return
//         */
//        public AsyncBuilder<T> subscribe(Subscriber<T> subscriber) {
//            this.subscriber = subscriber;
//            return this;
//        }

        /**
         * 执行,callable将会在一个线程池执行, callback将会在主线程回调.
         */
        public AsyncBuilder<T> subscribe(Observer<T> observer) {
            if (context == null) {
                RxHelper.doAsync(subscriber, observer);
            } else if (context != null && title != null) {
                doAsyncWithProgress(context,
                        title,
                        hintMessage,
                        subscriber,
                        observer,
                        cancelable);
            }
            return this;
        }

    }

    public static class CancelledException extends Exception {
        private static final long serialVersionUID = -78123211381435595L;
    }
}  