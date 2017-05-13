package com.jerey.asyncutil;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.jerey.asynclib.AsyncHelper;
import com.jerey.asynclib.Callback;
import com.jerey.asynclib.IProgressListener;
import com.jerey.asynclib.ProgressCallable;

import java.util.concurrent.Callable;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AsyncHelper.callable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Log.w(TAG, "call" + "thread id" + Thread.currentThread().getId());
                return "hello world";
            }
        }).callback(new Callback<String>() {
            @Override
            public void onCallback(String pCallbackValue) {
                Log.i(TAG, " onCallback " + "thread id" + Thread.currentThread().getId());
                Log.i(TAG, " onCallback " + pCallbackValue);
            }
        }).execute();


        AsyncHelper.callable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Log.w(TAG, " call " + "thread id" + Thread.currentThread().getId());
                Thread.sleep(3000);
                return 100;
            }
        }).callback(new Callback<Integer>() {
            @Override
            public void onCallback(Integer callbackValue) {
                Log.i(TAG, " onCallback " + "thread id" + Thread.currentThread().getId());
                Log.i(TAG, " onCallback " + callbackValue.toString());

            }
        }).setCancelable(true)
                .setDialogMode(MainActivity.this, "测试", "this is a test")
                .execute();

        AsyncHelper.doProgressAsync(MainActivity.this, R.string.app_name, new
                        ProgressCallable<String>() {
                            @Override
                            public String call(IProgressListener pProgressListener) throws Exception {
                                pProgressListener.onProgressChanged(5);
                                Log.w(TAG, "call " + "thread id" + Thread.currentThread()
                                        .getId()
                                        + "  ");
                                Thread.sleep(3000);
                                return "hello workddsf";
                            }
                        },
                new Callback<String>() {
                    @Override
                    public void onCallback(String callbackValue) {
                        Log.w(TAG, "onCallback " + "thread id" + Thread.currentThread().getId());
                        Log.w(TAG, "onCallback " + callbackValue);
                    }
                }, new Callback<Exception>() {
                    @Override
                    public void onCallback(Exception callbackValue) {

                    }
                }
        );
    }
}
