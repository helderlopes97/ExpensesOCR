package com.ipt.expensesocr;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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

import java.io.File;
import java.io.IOException;
import java.util.List;

import Utils.ImageUtils;

public class MainActivity extends AppCompatActivity {

    static final int CAMARA = 100;
    static final int READ_EXTERNAL_STORAGE = 101;
    static  final int WRITE_EXTERNAL_STORAGE= 102;
    static final int GALLERY_REQUEST_CODE = 103;
    ImageView imageView;
    TextView textView;
    Bitmap fatura_original;
    Bitmap fatura_transformada;
    Uri image;
    Bitmap teste;
    String mCameraFileName;
    String path = Environment.getExternalStorageDirectory()+"/ExpensesOCR/fatura.jpg";
    String email;
    String despesaId;
    String token;


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

        final Bundle intent=getIntent().getExtras();
        despesaId=intent.getString("despesaId");
        token=intent.getString("token");
        email=intent.getString("email");

        imageView = (ImageView) findViewById(R.id.img);
        fatura_original = BitmapFactory.decodeFile(path);
        imageView.setImageBitmap(fatura_original);
        textView = (TextView) findViewById(R.id.txt);

        // Butão para detetar
        Button but1 = (Button) findViewById(R.id.button4);
        but1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Log.e("YYYYYYYYYYYYYYYY", "TRANSFORM11111111 TRANSFORM");
                    fatura_transformada = ImageUtils.transform(fatura_original);
                    imageView.setImageBitmap(fatura_transformada);
                } catch (Exception e){
                    Log.e("YYYYYYYYYYYYYYYY", "ERRO ERRO ERRO ERRO 1111111111");
                    e.printStackTrace();
                    imageView.setImageBitmap(fatura_original);
                }
            }
        });

        // Butão para detetar
        Button but2 = (Button) findViewById(R.id.button5);
        but2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Log.e("YYYYYYYYYYYYYYYY", "TRANSFORM222222222222 TRANSFORM");
                    fatura_transformada = ImageUtils.threshold(fatura_transformada);
                    imageView.setImageBitmap(fatura_transformada);
                } catch (Exception e){
                    Log.e("YYYYYYYYYYYYYYYY", "ERRO ERRO ERRO ERRO 22222222222");
                    e.printStackTrace();
                    imageView.setImageBitmap(fatura_original);
                }
            }
        });

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.action_back:

                        intent = new Intent(MainActivity.this, DetalhesDespesa.class);
                        intent.putExtra("despesaId",(String) despesaId+"");
                        intent.putExtra("token",(String) token);
                        intent.putExtra("email",(String) email);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.action_detect:
                        try {
                            runTextRecognition(fatura_transformada);
                        } catch (Exception e) {
                            imageView.setImageBitmap(fatura_original);
                            runTextRecognition(fatura_original);
                        }

                        break;
                    case R.id.action_camera:
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMARA);
                        } else {
                            // Permission has already been granted
                            camera();
                        }
                        break;
                    case R.id.action_gallery:
                        pickFromGallery();
                        break;
                    case R.id.action_next:
                        intent = new Intent(MainActivity.this, Formulario.class);
                        intent.putExtra("despesaId",(String) despesaId+"");
                        intent.putExtra("token",(String) token);
                        intent.putExtra("email",(String) email);
                        startActivity(intent);
                        finish();
                        break;

                }
                return true;
            }
        });
    }

    private void runTextRecognition(Bitmap fatura) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(fatura);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        Task<FirebaseVisionText> result = detector.processImage(image)
            .addOnSuccessListener(
                new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        // Task completed successfully
                        // ...
                        textView.setText("");
                        String texto = "";
                        for (FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks()) {
                            String blockText = block.getText();
                            //textView.append(blockText);
                            Float blockConfidence = block.getConfidence();
                            List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
                            Point[] blockCornerPoints = block.getCornerPoints();
                            Rect blockFrame = block.getBoundingBox();
                            texto += "\n";
                            for (FirebaseVisionText.Line line: block.getLines()) {
                                String lineText = line.getText();
                                Float lineConfidence = line.getConfidence();
                                List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                                Point[] lineCornerPoints = line.getCornerPoints();
                                Rect lineFrame = line.getBoundingBox();
                                texto += "\n";
                                for (FirebaseVisionText.Element element: line.getElements()) {
                                    String elementText = element.getText();
                                    Float elementConfidence = element.getConfidence();
                                    List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                                    Point[] elementCornerPoints = element.getCornerPoints();
                                    Rect elementFrame = element.getBoundingBox();
                                    texto += elementText + " ";
                                }
                            }
                        }
                        textView.setText(texto);
                        Log.e("TEXTO", textView.getText().toString() );
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

        File outFile = new File(path);

        mCameraFileName = outFile.toString();
        Uri outuri = Uri.fromFile(outFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
        startActivityForResult(intent, CAMARA);
    }

    private void pickFromGallery() {
       /* File outFile = new File(path);
        mCameraFileName = outFile.toString();
        Uri outuri = Uri.fromFile(outFile);
        //Create an Intent with action as ACTION_PICK
        Intent intent = new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
        // Launching the Intent
        startActivityForResult(intent, GALLERY_REQUEST_CODE);*/
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,GALLERY_REQUEST_CODE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        File file;
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case GALLERY_REQUEST_CODE:
                    //data.getData returns the content URI for the selected Image
                   /* Uri selectedImage = data.getData();
                    image=selectedImage;
                    imageView.setImageDrawable(null);
                    imageView.setImageURI(image);
                    imageView.setVisibility(View.VISIBLE);

                    file = new File(mCameraFileName);
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    image = null;*/
                    Uri selectedImage = data.getData();

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

                        imageView.setImageBitmap(bitmap);
                        fatura_original=bitmap;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case CAMARA:
                    image = Uri.fromFile(new File(mCameraFileName));
                    imageView.setImageDrawable(null);
                    imageView.setImageURI(image);
                    imageView.setVisibility(View.VISIBLE);


                    file = new File(mCameraFileName);
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    image = null;
                    fatura_original=BitmapFactory.decodeFile(path);
            }
    }


}

