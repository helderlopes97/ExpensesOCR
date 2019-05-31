package com.ipt.expensesocr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class teste  extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teste);

        byte[] byteArray;
        Bitmap bit;

                Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String way = extras.getString("path");

            Bitmap bitGray;
            bitGray = BitmapFactory.decodeFile(way);
            bitGray = gray(bitGray);

            Bitmap bitBlack = black(bitGray);

            ImageView imgTeste = findViewById(R.id.imgTeste);
            imgTeste.setImageBitmap(bitBlack);

            //The key argument here must match that used in the other activity
        }

    }


    public Bitmap black(Bitmap origin) {
        Mat tmp = new Mat(origin.getWidth(), origin.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(origin, tmp);
        Imgproc.threshold(tmp,tmp,100,255,Imgproc.THRESH_BINARY);
        Utils.matToBitmap(tmp, origin);
        return origin;
    }

    public Bitmap gray(Bitmap origin) {
        Mat tmp = new Mat(origin.getWidth(), origin.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(origin, tmp);
        Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_RGB2GRAY);
        Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_GRAY2RGB, 4);
        Utils.matToBitmap(tmp, origin);
        return origin;
    }

}
