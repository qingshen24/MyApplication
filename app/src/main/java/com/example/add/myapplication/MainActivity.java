package com.example.add.myapplication;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.Utils;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Xml;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Object.*;


public class MainActivity extends AppCompatActivity
{
    private ImageView img;
    private Button btn;
    private File mCascadefile;
    private CascadeClassifier faceDetection;
    private static final Scalar   FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);

    private Bitmap srcBitmap;
    private Bitmap grayBitmap;
    private Bitmap srcBitmap_orgin;
    private static boolean flag = true;
    private static boolean isFirst = true;
    private static final String TAG = "qing_shen";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        img = (ImageView) findViewById(R.id.img);
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new ProcessClickListener());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, getApplicationContext(), mLoaderCallback);
        Log.i(TAG, "onResume sucess load OpenCV...");
    }


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    Log.i(TAG, "加载成功");
                    //load the opencv_source file

                    try
                    {
                        InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
                        //获取文件的字节数
                        //int length = is.available();
                        //创建byte数组
                        File cascadeDir =getDir("cascade",Context.MODE_PRIVATE);
                        mCascadefile =new File(cascadeDir,"haarcascade_frontalface_alt2.xml");
                        FileOutputStream os =new FileOutputStream(mCascadefile);
                        byte[] buffer = new byte[4096];
                        //将文件的数据读取到byte数组中
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1)
                        {
                            os.write(buffer,0,bytesRead);
                        }
                        is.close();
                        os.close();
                        faceDetection =new CascadeClassifier(mCascadefile.getAbsolutePath());
                        if(faceDetection.empty()){
                            Log.e(TAG,"Failed to load cascade classfier");
                            faceDetection =null;
                        }else {
                            Log.i(TAG,"Loaded cascade classifier form"+mCascadefile.getAbsolutePath());
                        }
                        cascadeDir.delete();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.e(TAG,"Failed to load cascade. Exception throem:"+e);
                    }

                    break;
                default:
                    super.onManagerConnected(status);
                    Log.i(TAG, "加载失败");
                    break;
            }
        }
    };

    /*public static String getString(InputStream inputStream) {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream,"utf-8");
        } catch (UnsupportedOperationException e1) {
            e1.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuilder sb = new StringBuilder("");
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    */

    public void procSrc2Gray()
    {
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        srcBitmap_orgin = BitmapFactory.decodeResource(getResources(), R.drawable.genie);
        srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.genie);
        grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Utils.bitmapToMat(srcBitmap, rgbMat);//convert the bitMap to Mat
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);//using the function of OpneCV to convert to the RGB to gray
        //进行人脸检测
        MatOfRect faces = new MatOfRect();
        faceDetection.detectMultiScale(grayMat,faces,1.1,2,2,new Size(0,0),new Size());
        Rect [] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
            Imgproc.rectangle(rgbMat,facesArray[i].tl(),facesArray[i].br(),FACE_RECT_COLOR,3);
            //Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

        //Imgproc.equalizeHist(grayMat, grayMat);//直方图均衡化

        //Imgproc.Canny(grayMat,grayMat,100.0,2.0);//边缘检测
        Utils.matToBitmap(rgbMat, srcBitmap);//convert Mat to bitMap
        Log.i(TAG, "procSrc2Gray sucess...");
    }

    public class ProcessClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            if (isFirst) {
                procSrc2Gray();
                isFirst = false;
            }
            if (flag) {
                img.setImageBitmap(srcBitmap);
                btn.setText("查看原图");
                flag = false;
            } else {
                img.setImageBitmap(srcBitmap_orgin);
                btn.setText("识别人脸");
                flag = true;
            }
        }
    }
}





