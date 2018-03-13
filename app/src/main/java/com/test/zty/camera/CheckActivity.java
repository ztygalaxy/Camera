package com.test.zty.camera;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

public class CheckActivity extends AppCompatActivity {

    private Button btn_cg_register;
    private Button btn_cp_register;
    private Button btn_cg_check;
    private EditText et_mail;
    private View lo_check = null;
    private View lo_register = null;
    private String url = "http://192.168.0.12/CheckTrue/deviceLogin?IMEI=0000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectAll().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        setContentView(R.layout.activity_check);

        lo_check = findViewById(R.id.lo_check);
        lo_register = findViewById(R.id.lo_register);
        btn_cg_register = (Button) findViewById(R.id.btn_cg_register);
        btn_cp_register = (Button) findViewById(R.id.btn_cp_register);
        btn_cg_check = (Button) findViewById(R.id.btn_cg_check);
        et_mail = (EditText) findViewById(R.id.et_mail);

        btn_cp_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.check_success);
            }
        });

        btn_cg_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lo_check.setVisibility(View.GONE);
                lo_register.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), mData.getIMEI(), Toast.LENGTH_SHORT).show();
            }
        });

        btn_cg_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tmp_email = et_mail.getText().toString().trim();
                if (tmp_email == null || tmp_email.equals("")) {
                    Toast.makeText(getApplicationContext(), "序列号不能为空！", Toast.LENGTH_SHORT).show();
                } else {
                    String tm_url = gotoURL(url);
                    //验证接口
                    if (tm_url.equals("true")) {
                        mData.setB_status(true);
                        Toast.makeText(getApplicationContext(), "验证成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CheckActivity.this, MainActivity.class);
                        startActivity(intent);
                        CheckActivity.this.finish();
                    } else {
                        Toast.makeText(getApplicationContext(), tm_url, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private String gotoURL(String url) {
        String outputString = "";
        // DefaultHttpClient
        BasicHttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 2000);
        HttpConnectionParams.setSoTimeout(httpParams, 2000);
        HttpClient httpclient = new DefaultHttpClient(httpParams);
        // HttpGet
        HttpGet httpget = new HttpGet(url);
        // ResponseHandler
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            outputString = httpclient.execute(httpget, responseHandler);
            //outputString = new String(outputString.getBytes("ISO-8859-1"), "utf-8");    // 解决中文乱码？？  （貌似有问题）
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
            return "连接超时";
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
        httpclient.getConnectionManager().shutdown();
        return outputString;
    }
}
