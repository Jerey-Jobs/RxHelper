package com.jerey.asyncutil;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.jerey.asynclib.IProgressListener;
import com.jerey.asynclib.Observer;
import com.jerey.asynclib.ProgressSubscriber;
import com.jerey.asynclib.RxHelper;
import com.jerey.asynclib.Subscriber;


public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.test1).setOnClickListener(this);
        findViewById(R.id.test2).setOnClickListener(this);
        findViewById(R.id.test3).setOnClickListener(this);
    }

    /**
     * 测试普通异步
     */
    private void testAsync() {
        RxHelper.fromSubsciber(new Subscriber<String>() {
            @Override
            public String call() throws Exception {
                Log.w(TAG, "call" + "thread id" + Thread.currentThread().getId());
                return "hello world";
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onObserve(String callbackValue) {

            }

            @Override
            public void onError(Exception e) {

            }
        });

    }

    /**
     * 测试处理时带dialog
     */
    private void testDialog() {
        RxHelper.fromSubsciber(new Subscriber<Integer>() {
            @Override
            public Integer call() throws Exception {
                Log.w(TAG, " call " + "thread id" + Thread.currentThread().getId());
                Thread.sleep(3000);
                return 100;
            }
        })
                .setCancelable(true)
                .setDialogMode(MainActivity.this, "测试", "this is a test")
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onObserve(Integer callbackValue) {

                        Log.i(TAG, " onCallback " + "thread id" + Thread.currentThread().getId());
                        Log.i(TAG, " onCallback " + callbackValue.toString());

                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });

    }

    /**
     * 测试进度条
     */
    private void testProgress() {
        RxHelper.doProgressAsync(MainActivity.this, R.string.app_name, new
                        ProgressSubscriber<Object>() {
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
                new Observer<Object>() {
                    @Override
                    public void onObserve(Object callbackValue) {
                        Log.w(TAG, "onCallback " + "thread id" + Thread.currentThread().getId());
                        Log.w(TAG, "onCallback " + callbackValue);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.w(TAG, "onError ");
                    }
                }
        );
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test1:
                testAsync();
                break;
            case R.id.test2:
                testDialog();
                break;
            case R.id.test3:
                testProgress();
                break;
        }
    }
}
