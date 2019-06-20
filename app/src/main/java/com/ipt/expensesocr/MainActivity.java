package com.ipt.expensesocr;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final int CAMARA = 100;
    static final int READ_EXTERNAL_STORAGE = 101;
    static  final int WRITE_EXTERNAL_STORAGE= 102;
    ImageView imageView;
    TextView textView;
    String fatura = Environment.getExternalStorageDirectory()+"/ExpensesOCR/fatura.jpg";
    Uri image;
    Bitmap teste;
    String mCameraFileName;
    String path = Environment.getExternalStorageDirectory()+"/ExpensesOCR/fatura.jpg";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCVLoader.initDebug();
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        //PERMISSIONS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},READ_EXTERNAL_STORAGE);
        } else {
            // Permission has already been granted
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},CAMARA);
        } else {
            // Permission has already been granted
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_EXTERNAL_STORAGE);
        } else {
            // Permission has already been granted

        }


        imageView = (ImageView) findViewById(R.id.img);
        imageView.setImageBitmap(BitmapFactory.decodeFile(fatura));
        textView = (TextView) findViewById(R.id.txt);

        Button next = (Button) findViewById(R.id.btNext);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,Formulario.class);
                startActivity(intent);
                finish();
            }
        });


        // Butão da câmara
        Button cam = (Button) findViewById(R.id.btCam);
        cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},CAMARA);
                } else {
                    // Permission has already been granted
                    camera();
                }
            }
        });

        // Butão para detetar
        Button detect = (Button) findViewById(R.id.btDetect);
        detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTextRecognition();
            }
        });


        // Butão para teste
        Button btTeste = (Button) findViewById(R.id.btTeste);
        btTeste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this,teste.class);
                intent.putExtra("path", path);
                startActivity(intent);
            }
        });
    }

    private void runTextRecognition() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(BitmapFactory.decodeFile(fatura));
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        Task<FirebaseVisionText> result = detector.processImage(image)
            .addOnSuccessListener(
                new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        // Task completed successfully
                        // ...
                        textView.setText("");
                        for (FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks()) {
                            String blockText = block.getText();
                            textView.append(blockText);
                            Float blockConfidence = block.getConfidence();
                            List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
                            Point[] blockCornerPoints = block.getCornerPoints();
                            Rect blockFrame = block.getBoundingBox();
                            for (FirebaseVisionText.Line line: block.getLines()) {
                                String lineText = line.getText();
                                Float lineConfidence = line.getConfidence();
                                List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                                Point[] lineCornerPoints = line.getCornerPoints();
                                Rect lineFrame = line.getBoundingBox();
                                for (FirebaseVisionText.Element element: line.getElements()) {
                                    String elementText = element.getText();
                                    Float elementConfidence = element.getConfidence();
                                    List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                                    Point[] elementCornerPoints = element.getCornerPoints();
                                    Rect elementFrame = element.getBoundingBox();
                                }
                            }
                        }
                    }
                }
            )
        .addOnFailureListener(
            new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Task failed with an exception
                    // ...
                    textView.setText("Fail");
                }
            }
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case CAMARA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }


            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private void camera() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        File outFile = new File(fatura);

        mCameraFileName = outFile.toString();
        Uri outuri = Uri.fromFile(outFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
        startActivityForResult(intent, 2);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 2) {
                image = Uri.fromFile(new File(mCameraFileName));
                imageView.setImageDrawable(null);
                imageView.setImageURI(image);
                imageView.setVisibility(View.VISIBLE);

                File file = new File(mCameraFileName);
                if (!file.exists()) {
                    file.mkdir();
                }
                image=null;
            }
        }
    }


}

