package com.test.zty.camera;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


public class Main2Activity extends AppCompatActivity {
    protected static final int SHORT = 0;
    private final String IMAGE_TYPE = "image/*";

    private final int IMAGE_CODE = 0;

    private Button addPic = null;
    private Uri originalUri = null;

    private ImageView imgShow = null;

    private Button folder=null;
    private TextView imgPath = null;
    private TextView tv_tips=null;
    private TextView num_show = null;
    private TextView sus_show = null;
    private TextView test = null;
    private SeekBar seek = null;
    int[] v = new int[100001];
    Bitmap bm = null;

    private int[] labelmap;
    private boolean over = false;


    private int i_number = 0;
    private int i_sus = 0;//疑似粘连的种子数目

    static int NeighborDirection[][] = {{0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}};
    static int NeighborDirection2[][] = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main2);
        init();
    }

    private void init() {
        addPic = (Button) findViewById(R.id.btn_add);
        imgPath = (TextView) findViewById(R.id.img_path);
        imgShow = (ImageView) findViewById(R.id.imgShow);
        num_show = (TextView) findViewById(R.id.num_show);
        sus_show = (TextView) findViewById(R.id.sus_show);
        test = (TextView) findViewById(R.id.myTextView);
        seek = (SeekBar) findViewById(R.id.mySeekBar);
        folder= (Button) findViewById(R.id.folder);
        tv_tips= (TextView) findViewById(R.id.tv_tips);
        seek.setMax(100);
        seek.setProgress(39);
        i_number = (int) (seek.getProgress() * 2.55);
        addPic.setOnClickListener(listener);
        folder.setOnClickListener(listener);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub

                if (imgShow.getDrawable() == null) {

					/*  bm=bitmapt;
                      Bitmap bitmapTmp=bm;
		                bitmapTmp= averageFilter(3,3,bm);
		                // 将图片转化成黑白图片
		                bitmapTmp = convertToBMW(i_number,bitmapTmp);
		                // 显得到bitmap图片
		                imgShow.setImageBitmap(bitmapTmp);
		                imgPath.setText("当前所设置的阈值为："+i_number);*/

                } else {
                    i_number = (int) (progress * 2.55);

                    Bitmap bitmapTmp = bm;
                    //中值滤波
                    bitmapTmp = averageFilter(3, 3, bm);
                    // 将图片转化成黑白图片
                    bitmapTmp = convertToBMW(i_number, bitmapTmp);
                    // 显得到bitmap图片
                    imgShow.setImageBitmap(bitmapTmp);
                    int i = ConnectedComponentLabeling(bitmapTmp);
                    while (over == false) {
                        bitmapTmp = Cut(bitmapTmp);
                        i = ConnectedComponentLabeling(bitmapTmp);
                    }
                    imgPath.setText("图片路径：" + originalUri.toString());
                    num_show.setText("种子个数为 " + i + "个");
                    sus_show.setText("当前阈值为：" + i_number);
                }
            }
        });
    }

    private OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Button btn = (Button) v;
            switch (btn.getId()) {
                case R.id.btn_add:
                    setImage();
                    break;
                case R.id.folder:
                    setImage();
                    break;
            }
        }
        private void setImage() {
            // 使用intent调用系统提供的相册功能，使用startActivityForResult是为了获取用户选择的图片的地址
            Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
            getAlbum.setType(IMAGE_TYPE);
            startActivityForResult(getAlbum, IMAGE_CODE);
        }
    };
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Toast.makeText(getApplicationContext(),"处理中",Toast.LENGTH_SHORT).show();
        // RESULT_OK 是系统自定义得一个常量
        if (resultCode != RESULT_OK) {
            Log.e("onActivityResult", "返回的resultCode出错");
            return;
        }

        // 外界的程序访问ContentProvider所提供数据 可以通过ContentResolver接口
        ContentResolver resolver = getContentResolver();
        // 判断接收的Activity是不是选择图片的
        if (requestCode == IMAGE_CODE) {
            try {
                // 获得图片的地址Uri
                originalUri = data.getData();
                // 新建一个字符串数组用于存储图片地址数据。
                String[] proj = {MediaStore.Images.Media.DATA};
                // android系统提供的接口，用于根据uri获取数据
                Cursor cursor = managedQuery(originalUri, proj, null, null,
                        null);
                // 获得用户选择图片的索引值
                int column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                // 将游标移至开头 ，防止引起队列越界
                cursor.moveToFirst();
                // 根据图片的URi生成bitmap
                bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);

                Bitmap bitmapTmp = bm;
                //中值滤波
                bitmapTmp = averageFilter(3, 3, bitmapTmp);
                // 将图片转化成黑白图片
                bitmapTmp = convertToBMW(i_number, bitmapTmp);
                // 显得到bitmap图片
                imgShow.setImageBitmap(bitmapTmp);
                test.setVisibility(View.VISIBLE);
                seek.setVisibility(View.VISIBLE);
                folder.setVisibility(View.GONE);
                tv_tips.setVisibility(View.GONE);


                int i = ConnectedComponentLabeling(bitmapTmp);

                while (over == false) {
                    bitmapTmp = Cut(bitmapTmp);
                    i = ConnectedComponentLabeling(bitmapTmp);
                }
                imgPath.setText("图片路径：" + originalUri.toString());
                num_show.setText("种子个数为 " + i + "个");
                sus_show.setText("当前阈值为：" + i_number);
                scorolShow(bm);
                i_number = 100;
            } catch (IOException e) {
                Log.e("getImg", e.toString());
            }
        }
    }

    //滚动条提示
    private void scorolShow(Bitmap bitmap) {
        WindowManager wm1 = this.getWindowManager();
        int height1 = wm1.getDefaultDisplay().getHeight();

        int[] location = new int[2];
        addPic.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        int imgP_h = imgPath.getHeight();
        int numS_h = num_show.getHeight();
        int susS_h = sus_show.getHeight();
        int myT_h = test.getHeight();
        int seB_h = seek.getHeight();
        int H = imgP_h + numS_h + seB_h + susS_h + myT_h ;

        int i = bitmap.getHeight();
        if (i > height1 - y - H) {
            Toast.makeText(getApplicationContext(), "上滑查看全部", Toast.LENGTH_SHORT).show();
        }
    }

        /**
     * 转为二值图像
     */
    public Bitmap convertToBMW(int intmp, Bitmap bmp) {
        int width = bmp.getWidth(); // 获取位图的宽
        int height = bmp.getHeight(); // 获取位图的高
        int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组
        // 设定二值化的域值，默认值为100
        int tmp = intmp;
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                // 分离三原色
                alpha = ((grey & 0xFF000000) >> 24);
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);
                if (red > tmp) {
                    red = 255;
                } else {
                    red = 0;
                }
                if (blue > tmp) {
                    blue = 255;
                } else {
                    blue = 0;
                }
                if (green > tmp) {
                    green = 255;
                } else {
                    green = 0;
                }
                pixels[width * i + j] = alpha << 24 | red << 16 | green << 8
                        | blue;
                if (pixels[width * i + j] == -1) {
                    pixels[width * i + j] = -1;
                } else {
                    pixels[width * i + j] = -16777216;
                }
            }
        }
        // 新建图片
        Bitmap newBmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        // 设置图片数据
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(newBmp, width, height);
        return resizeBmp;
    }

    public Bitmap averageFilter(int filterWidth, int filterHeight, Bitmap myBitmap) {
        // Create new array
        int width = myBitmap.getWidth();
        int height = myBitmap.getHeight();
        int[] pixNew = new int[width * height];
        int[] pixOld = new int[width * height];
        myBitmap.getPixels(pixNew, 0, width, 0, 0, width, height);
        myBitmap.getPixels(pixOld, 0, width, 0, 0, width, height);

        // Apply pixel-by-pixel change
        int filterHalfWidth = filterWidth / 2;
        int filterHalfHeight = filterHeight / 2;
        int filterArea = filterWidth * filterHeight;
        for (int y = filterHalfHeight; y < height - filterHalfHeight; y++) {
            for (int x = filterHalfWidth; x < width - filterHalfWidth; x++) {
                // Accumulate values in neighborhood
                int accumR = 0, accumG = 0, accumB = 0;
                for (int dy = -filterHalfHeight; dy <= filterHalfHeight; dy++) {
                    for (int dx = -filterHalfWidth; dx <= filterHalfWidth; dx++) {
                        int index = (y + dy) * width + (x + dx);
                        accumR += (pixOld[index] >> 16) & 0xff;
                        accumG += (pixOld[index] >> 8) & 0xff;
                        accumB += pixOld[index] & 0xff;
                    } // dx
                } // dy

                // Normalize
                accumR /= filterArea;
                accumG /= filterArea;
                accumB /= filterArea;
                int index = y * width + x;
                pixNew[index] = 0xff000000 | (accumR << 16) | (accumG << 8) | accumB;
            } // x
        } // y

        // Change bitmap to use new array
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bitmap.setPixels(pixNew, 0, width, 0, 0, width, height);
        myBitmap = null;
        pixOld = null;
        pixNew = null;
        return bitmap;
    }

    //显示粘连警示
    private String s_sus() {
        if (i_sus == 0) {
            return "";
        } else {
            return "疑似有" + i_sus + "个粘连组织，请注意检查！";
        }
    }

    private void SearchNeighbor(int bitmap[], int width, int height, int labelmap[],
                                int labelIndex, int pixelIndex, Queue<Integer> queue) {
        int searchIndex, i, length;
        labelmap[pixelIndex] = labelIndex;
        length = width * height;
        for (i = 0; i < 8; i++) {
            searchIndex = pixelIndex + NeighborDirection[i][0] * width + NeighborDirection[i][1];
            if (searchIndex > 0 && searchIndex < length &&
                    bitmap[searchIndex] == -16777216 && labelmap[searchIndex] == 0) {
                labelmap[searchIndex] = labelIndex;
                queue.offer(searchIndex);
            }
        }
    }

    private int ConnectedComponentLabeling(Bitmap bmp) {
        int cx, cy, index, popIndex, labelIndex = 0, avg = 0, unitNum = 0;
        i_sus = 0;
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] bitmap = new int[width * height];
        labelmap = new int[width * height];
        bmp.getPixels(bitmap, 0, width, 0, 0, width, height);
        Queue<Integer> queue = new LinkedList();
        ArrayList<Integer> count = new ArrayList();
        for (cy = 0; cy < height; cy++) {
            for (cx = 0; cx < width; cx++) {
                index = cy * width + cx;
                if (bitmap[index] == -16777216 && labelmap[index] == 0) {
                    labelIndex++;
                    ++unitNum;
                    SearchNeighbor(bitmap, width, height, labelmap, labelIndex, index, queue);
                    while (queue.peek() != null) {
                        popIndex = queue.remove();
                        ++unitNum;
                        SearchNeighbor(bitmap, width, height, labelmap, labelIndex, popIndex, queue);
                    }
                    count.add(unitNum);
                    v[labelIndex] = unitNum;
                    avg += unitNum;
                    unitNum = 0;
                }
            }
        }
        if (labelIndex == 0) {
            i_sus = 0;
            return labelIndex;
        }
        avg = avg / labelIndex;
        for (int i = 0; i < count.size(); ++i)
            if (count.get(i) > 1.7 * avg)
                ++i_sus;
        return labelIndex;
    }

    private boolean JudgeCut(int width, int height, int labelIndex, int pixelIndex, int[] bm2) {
        int searchIndex, i, length;
        boolean jud_1 = false, jud_2 = false;
        length = width * height;
        for (i = 0; i < 4; i++) {
            searchIndex = pixelIndex + NeighborDirection2[i][0] * width + NeighborDirection2[i][1];
            if (searchIndex > 0 && searchIndex < length && bm2[searchIndex] == -1) {
                jud_1 = true;
                break;
            }
        }
        for (i = 0; i < 8; i++) {
            searchIndex = pixelIndex + NeighborDirection[i][0] * width + NeighborDirection[i][1];
            if (searchIndex > 0 && searchIndex < length && labelmap[searchIndex] == labelIndex) {
                jud_2 = true;
                break;
            }
        }
        if (jud_1 && jud_2)
            return true;
        else
            return false;
    }

    private Bitmap Cut(Bitmap bitmapTmp) {
        int index;
        boolean temp = false;
        int width = bitmapTmp.getWidth();
        int height = bitmapTmp.getHeight();
        int[] bm2 = new int[width * height];
        int[] labelmap2 = new int[width * height];
        bitmapTmp.getPixels(bm2, 0, width, 0, 0, width, height);
        for (int cy = 0; cy < height; cy++)
            for (int cx = 0; cx < width; cx++) {
                index = cy * width + cx;
                if (bm2[index] == -16777216) {
                    if (JudgeCut(width, height, bm2[index], index, bm2)) {
                        labelmap2[index] = -1;
                        temp = true;
                    }
                }
            }
        if (temp == false)
            over = true;
        else {
            for (int cy = 0; cy < height; cy++)
                for (int cx = 0; cx < width; cx++) {
                    index = cy * width + cx;
                    if (labelmap2[index] == -1 && v[labelmap[index]] > 1) {
                        bm2[index] = -1;
                        --v[labelmap[index]];
                    }
                }
        }
        bitmapTmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bitmapTmp.setPixels(bm2, 0, width, 0, 0, width, height);
        return bitmapTmp;
    }
}
