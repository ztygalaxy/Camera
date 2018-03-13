package com.test.zty.camera;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;


public class StartActivity extends AppCompatActivity {

    public static final String data_pre = "data_pre";    //Preferences文件的名称
    private Handler mHandler;
    private String status = "yes";
    private String s_imei = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_start);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View rootView = LayoutInflater.from(this).inflate(R.layout.activity_start, null);

        setContentView(rootView);
        TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        s_imei = tm.getDeviceId();


        mHandler = new Handler();
        //初始化渐变动画
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.jianbian);
        //设置动画监听器
        animation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
                if (s_imei == null || s_imei.equals("")) {
                    status = "no";
                    showHelpDialog();
                }
                mData.setIMEI(s_imei);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //当监听到动画结束时，开始跳转到MainActivity中去
                TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
                if (status.equals("no"))
                    android.os.Process.killProcess(android.os.Process.myPid());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        loadData();
                    }
                });
            }
        });

        //开始播放动画
        rootView.startAnimation(animation);
    }

    private void showHelpDialog() {
        android.support.v7.app.AlertDialog.Builder builder =
                new android.support.v7.app.AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.logo);
        builder.setTitle("提示");
        builder.setMessage("请授予相应权限后重新启动App");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        builder.show();
    }

    private void loadData() {
        boolean b_status = mData.isB_status();
        if (!b_status) {
            Intent i = new Intent(StartActivity.this, CheckActivity.class);
            startActivity(i);
            StartActivity.this.finish();
        } else {
            Intent i = new Intent(StartActivity.this, MainActivity.class);
            startActivity(i);
            StartActivity.this.finish();
        }
    }
}
