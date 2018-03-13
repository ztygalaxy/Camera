package com.test.zty.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //自定义变量
    public static final int TAKE_PHOTO = 1;
    public static final int CROP_PHOTO = 2;
    private Button fab;
    private Button takePhotoBn;
    private Button dealPhotoBn;
    private Button takePhotoBn_h;
    private Button dealPhotoBn_h;
    private ImageView showImage;
    private TextView tips;
    private Uri imageUri; //图片路径
    private String filename; //图片名称
    private View lo_hor = null;
    private View lo_ver = null;
    private String url = "http://192.168.0.12/CheckTrue/inforShow";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectAll().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        setContentView(R.layout.activity_main);

        tips = (TextView) findViewById(R.id.tips);
        takePhotoBn = (Button) findViewById(R.id.button1);
        showImage = (ImageView) findViewById(R.id.imageView1);
        dealPhotoBn = (Button) findViewById(R.id.button2);

        takePhotoBn_h = (Button) findViewById(R.id.button1_h);
        dealPhotoBn_h = (Button) findViewById(R.id.button2_h);

        fab = (Button) findViewById(R.id.fab);
        lo_hor = findViewById(R.id.lo_hor);
        lo_ver = findViewById(R.id.lo_ver);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHelpDialog();
            }
        });
        //点击"Photo Button"按钮照相
        takePhotoBn.setOnClickListener(mOnClickListener);
        takePhotoBn_h.setOnClickListener(mOnClickListener);
        //点击按钮进行二值化
        dealPhotoBn.setOnClickListener(mOnClickListener);
        dealPhotoBn_h.setOnClickListener(mOnClickListener);
        showInforDialog();
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Button btn = (Button) view;
            switch (btn.getId()) {
                case R.id.button1:
                    takePhoto();
                    break;
                case R.id.button1_h:
                    takePhoto();
                    break;
                case R.id.button2:
                    dealPhoto();
                    break;
                case R.id.button2_h:
                    dealPhoto();
                    break;
            }
        }

        private void dealPhoto() {
            Intent intent = new Intent(MainActivity.this, Main2Activity.class);
//                intent.putExtra("number", s_number);
            startActivity(intent);
            //关闭当前Activity
//                finish();
            overridePendingTransition(0, 0);
        }

        private void takePhoto() {
            //图片名称 时间命名
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            Date date = new Date(System.currentTimeMillis());
            filename = format.format(date);
            //创建File对象用于存储拍照的图片 SD卡根目录
            //File outputImage = new File(Environment.getExternalStorageDirectory(),"test.jpg");
            //存储至DCIM文件夹
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File outputImage = new File(path, filename + ".jpg");
            try {
                if (outputImage.exists()) {
                    outputImage.delete();
                }
                outputImage.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //将File对象转换为Uri并启动照相程序
            imageUri = Uri.fromFile(outputImage);
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE"); //照相
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); //指定图片输出地址
            startActivityForResult(intent, TAKE_PHOTO); //启动照相
            //拍完照startActivityForResult() 结果返回onActivityResult()函数
        }
    };

    private void showHelpDialog() {
        android.support.v7.app.AlertDialog.Builder builder =
                new android.support.v7.app.AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.logo);
        builder.setTitle("开发团队");
        builder.setMessage("超平和Busters\n\n张天宇  高嵩  王伟英\n\nEmail:zhangty1996@163.com");
        builder.setPositiveButton("确定", null);
        builder.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            Toast.makeText(MainActivity.this, "未采集到照片", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (requestCode) {
            case TAKE_PHOTO:
                Intent intent = new Intent("com.android.camera.action.CROP"); //剪裁
                intent.setDataAndType(imageUri, "image/*");
                intent.putExtra("scale", true);
                //设置宽高比例
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                //设置裁剪图片宽高
                intent.putExtra("outputX", 800);
                intent.putExtra("outputY", 800);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                Toast.makeText(MainActivity.this, "剪裁图片，请将种子全部放在框内", Toast.LENGTH_SHORT).show();
                //广播刷新相册
                Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intentBc.setData(imageUri);
                this.sendBroadcast(intentBc);
                startActivityForResult(intent, CROP_PHOTO); //设置裁剪参数显示图片至ImageView
                break;
            case CROP_PHOTO:
                try {
                    //图片解析成Bitmap对象
                    Bitmap bitmap = BitmapFactory.decodeStream(
                            getContentResolver().openInputStream(imageUri));
//                    Toast.makeText(MainActivity.this, imageUri.toString(), Toast.LENGTH_SHORT).show();

//                    tips.setVisibility(View.GONE);
                    lo_ver.setVisibility(View.GONE);
                    lo_hor.setVisibility(View.VISIBLE);

                    showImage.setImageBitmap(bitmap); //将剪裁后照片显示出来
                    Toast.makeText(MainActivity.this, "裁剪完毕，点击右侧按钮进行处理", Toast.LENGTH_LONG).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    private void showInforDialog() {
        String tmp_warning = gotoURL(url);
        if (!tmp_warning.equals(mData.getWarning())) {
            android.support.v7.app.AlertDialog.Builder builder =
                    new android.support.v7.app.AlertDialog.Builder(this);
            builder.setIcon(R.mipmap.logo);
            builder.setTitle("提示");
            builder.setMessage(tmp_warning);
            builder.setPositiveButton("确定", null);
            builder.show();
            mData.setWarning(tmp_warning);
        }
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

