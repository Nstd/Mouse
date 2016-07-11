package com.nstd.tools.test;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;

/**
 * Created by Nstd on 2016/7/11 0011.
 */
public class MyApp extends Application implements Thread.UncaughtExceptionHandler{
    public static final String TAG = "CrashHandler";
    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();

        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, final Throwable throwable) {
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(mContext).setTitle("提示").setCancelable(false)
                                .setMessage("程序崩溃了...\n" + getMyStackTrace(throwable)).setNeutralButton("我知道了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                System.exit(0);
                            }
                        })
                                .create().show();
                    }
                }, 1000);
                Looper.loop();
            }
        }.start();
    }

    public String getMyStackTrace(Throwable throwable) {
        String msg = "";
        if(throwable != null) {
            msg += throwable.getMessage();
            StackTraceElement[] e = throwable.getStackTrace();
            for(int i=0; i<e.length; i++) {
                msg += "\n" + e[i].getMethodName() + ":" + e[i].getLineNumber();
            }
        }
        return msg;
    }
}
