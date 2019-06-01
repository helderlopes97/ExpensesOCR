package com.ipt.expensesocr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.clearcut.ClearcutLogger;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class teste  extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teste);

        byte[] byteArray;
        Bitmap bit;

                Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String path = extras.getString("path");
            bit = BitmapFactory.decodeFile(path);

            //Bitmap bitBlack = black(bit, path);
            Bitmap bitBlack = edges(bit, path);

            ImageView imgTeste = findViewById(R.id.imgTeste);
            imgTeste.setImageBitmap(bitBlack);

        }

    }


    public Bitmap black(Bitmap bit, String path) {
        Mat src = Imgcodecs.imread(path,Imgcodecs.IMREAD_GRAYSCALE);

        // simple thresholding
        //Imgproc.threshold(tmp,tmp,100,255,Imgproc.THRESH_BINARY);

        // adaptive threshold
        Imgproc.adaptiveThreshold(src,src,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY, 31,2);
        //Imgproc.adaptiveThreshold(tmp,tmp,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY, 31,2);

        // otsu's threshold
        //Imgproc.threshold(tmp,tmp,0,255,Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        Utils.matToBitmap(src, bit);
        return bit;
    }

    public Bitmap edges(Bitmap bit, String path) {
        Mat src = Imgcodecs.imread(path, Imgcodecs.IMREAD_GRAYSCALE);
        Size size = new Size(5, 5);
        Imgproc.GaussianBlur(src, src, size, 0);
        Imgproc.Canny(src, src, 75, 20);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(src, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat d = new Mat();
        d.create(src.rows(),src.cols(),CvType.CV_8UC3);
        Imgproc.drawContours(d,contours,-1,new Scalar(0,255,0),1);

        Utils.matToBitmap(d, bit);
        return bit;
    }


    public Bitmap calcSkew(Bitmap bit, String path){
        // le a imagem em grayscale
        Mat src = Imgcodecs.imread(path,Imgcodecs.IMREAD_GRAYSCALE);

        //Imgproc.adaptiveThreshold(src,src,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY_INV, 31,2);

        // guarda o tamanho da imagem
        Size size = src.size();
        // inverte os bits da imagem
        Core.bitwise_not(src,src);

        Mat lines = new Mat();
        //size.width/2, 20
        Imgproc.HoughLinesP(src, lines, 1, Math.PI/180, 100, 50, 30);

        Mat disp_lines = new Mat(size, CvType.CV_8UC1, new Scalar(0,0,0));
        double angle = 0;
        for (int i = 0; i < lines.rows(); i++) {
            double[] val = lines.get(i,0);
            Imgproc.line(disp_lines, new Point(val[0], val[1]), new Point(val[2], val[3]), new Scalar(255,0,0),1);
            angle += Math.atan2(val[3] - val[1], val[2] - val[0]);
        }
        angle /= lines.rows();

        Log.e("RESULT", "calcSkew: "+ angle * 180 / Math.PI );
        // tranforma em bitmap
        Utils.matToBitmap(disp_lines, bit);
        // devolve o bitmap
        return bit;
    }

}
