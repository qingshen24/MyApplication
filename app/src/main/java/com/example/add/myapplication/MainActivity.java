package com.example.add.myapplication;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.Utils;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
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
    private File mCascadefile,eyeCascadefile;
    private CascadeClassifier faceDetection,eyeDetection;
    private static final Scalar   FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    private static final Scalar   EYE_RECT_COLOR =new Scalar(0,0,255,255);

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
                        //load facedetection resourece file

                        InputStream face_is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
                        File cascadeDir =getDir("cascade",Context.MODE_PRIVATE);
                        mCascadefile =new File(cascadeDir,"haarcascade_frontalface_alt2.xml");
                        FileOutputStream face_os =new FileOutputStream(mCascadefile);
                        byte[] buffer = new byte[4096];
                        //将文件的数据读取到byte数组中
                        int bytesRead;
                        while ((bytesRead = face_is.read(buffer)) != -1)
                        {
                            face_os.write(buffer, 0, bytesRead);
                        }
                        face_os.close();
                        face_os.close();
                        faceDetection =new CascadeClassifier(mCascadefile.getAbsolutePath());
                        if(faceDetection.empty()){
                            Log.e(TAG,"Failed to load cascade classfier");
                            faceDetection =null;
                        }else {
                            Log.i(TAG,"Loaded cascade classifier form"+mCascadefile.getAbsolutePath());
                        }
                        cascadeDir.delete();

                        //load eyedetection resourece file

                        InputStream eye_is =getResources().openRawResource(R.raw.haarcascade_eye);
                        File eyeCascadeDir =getDir("eyeCascade",Context.MODE_PRIVATE);
                        eyeCascadeDir =new File(eyeCascadeDir,"haarcascade_eye.xml");
                        FileOutputStream eye_os = new FileOutputStream(eyeCascadefile);
                        byte [] buffer_eye =new byte[4096];
                        int eye_bytesRead;
                        while ((eye_bytesRead = eye_is.read(buffer_eye)) != -1)
                        {
                            eye_os.write(buffer_eye,0,eye_bytesRead);
                        }
                        eye_is.close();
                        eye_os.close();
                        eyeDetection =new CascadeClassifier(eyeCascadefile.getAbsolutePath());
                        eyeCascadeDir.delete();


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

    //the processing function
    public void procSrc2Gray()
    {
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        srcBitmap_orgin = BitmapFactory.decodeResource(getResources(), R.drawable.man);
        srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.man);
        grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Utils.bitmapToMat(srcBitmap, rgbMat);//convert the bitMap to Mat
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);//using the function of OpneCV to convert to the RGB to gray
        Imgproc.equalizeHist(grayMat, grayMat);//直方图均衡化
        //进行人脸检测

        MatOfRect faces = new MatOfRect();
        MatOfRect eyes =new MatOfRect();
        faceDetection.detectMultiScale(grayMat,faces,1.1,2,2,new Size(0,0),new Size());
        Rect [] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
            Imgproc.rectangle(rgbMat,facesArray[i].tl(),facesArray[i].br(),FACE_RECT_COLOR,3);

        if(facesArray.length>0)
        {
            Log.i(TAG,"start to detect eyes");
            Rect roi = new Rect((int)facesArray[0].tl().x,(int)(facesArray[0].tl().y),facesArray[0].width,(int)(facesArray[0].height));
            Mat cropped = new Mat();
            // set the ROI area
            cropped = grayMat.submat(roi);
            if(eyeDetection != null)
                eyeDetection.detectMultiScale(cropped,eyes,1.1,2,2,new Size(0,0),new Size());
            else
                Log.i(TAG,"Failed to detect eyes");
            Rect [] eyesArray =eyes.toArray();
            Point x1 =new Point();
            for(int j=0;j<eyesArray.length;j++)
            {
                x1.x=facesArray[0].x + eyesArray[j].x + eyesArray[j].width*0.5;
                x1.y=facesArray[0].y + eyesArray[j].y + eyesArray[j].height*0.5;
                int Radius=(int)((eyesArray[j].width + eyesArray[j].height)*0.25 );
                Imgproc.circle(rgbMat, x1, Radius, EYE_RECT_COLOR);
            }
        }

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





