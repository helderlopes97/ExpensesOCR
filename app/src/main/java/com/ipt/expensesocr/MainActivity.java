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
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    String api_key;
    String tipo;
    String perc;


    ///DADOS
    Double valor=0.0;
    String data="";
    String nif="";
    Date dataTeste;
    boolean more=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCVLoader.initDebug();
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        try{
            dataTeste=new SimpleDateFormat("yyyy-MM-dd").parse("2000-1-1");
        }catch (Exception e){

        }

        final Bundle intent=getIntent().getExtras();
        despesaId=intent.getString("despesaId");
        token=intent.getString("token");
        email=intent.getString("email");
        api_key = "b05c81093b4371c50f3aa142184974149d4411b2";

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


        Button detectMore = (Button) findViewById(R.id.addmore);
        detectMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                more=true;
                try {
                    runTextRecognition(fatura_transformada);
                } catch (Exception e) {
                    imageView.setImageBitmap(fatura_original);
                    runTextRecognition(fatura_original);
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
                        more=false;
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
                        // Instantiate the RequestQueue.
                        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

                        String url ="https://api.monkeylearn.com/v3/classifiers/cl_ZSPc2GNb/classify/";

                        // Request a string response from the provided URL.
                        StringRequest req = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String res) {
                                        try{
                                            Log.e("CLASS", "aqui");
                                            JSONArray array= new JSONArray(res);
                                            JSONObject obj = (JSONObject) array.get(0);
                                            if(obj.has("classifications")) {
                                                JSONArray classifArray = (JSONArray) obj.get("classifications");
                                                JSONObject classifcObj = (JSONObject) classifArray.get(0);
                                                String tag = classifcObj.get("tag_name").toString();
                                                String conf = classifcObj.get("confidence").toString();
                                                tipo = tag;
                                                perc = Double.valueOf(conf) * 100 + "%";
                                                Log.e("YYYYYYYYYYYYYYYY", tipo);
                                                Log.e("YYYYYYYYYYYYYYYY", perc);
                                                Intent intent;
                                                intent = new Intent(MainActivity.this, Formulario.class);
                                                intent.putExtra("despesaId",(String) despesaId+"");
                                                intent.putExtra("token",(String) token);
                                                intent.putExtra("email",(String) email);
                                                intent.putExtra("valor",(String)""+valor);
                                                intent.putExtra("data",(String) data);
                                                intent.putExtra("nif",(String) nif);
                                                intent.putExtra("tipo",(String) tipo);
                                                intent.putExtra("perc",(String) perc);
                                                startActivity(intent);
                                                finish();
                                            }
                                        } catch (Exception e){
                                            Log.e("CLASS", "erro");
                                            e.printStackTrace();
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("ERR", "API FAIL" );
                                    }
                                }){

                            @Override
                            public String getBodyContentType() {
                                return "application/json; charset=utf-8";
                            }

                            @Override
                            public byte[] getBody() throws AuthFailureError {
                                try{
                                    JSONObject jsonBody = new JSONObject();
                                    JSONArray bodyArray = new JSONArray();
                                    bodyArray.put(textView.getText().toString());
                                    jsonBody.put("data",bodyArray);
                                    return jsonBody.toString().getBytes("utf-8");
                                } catch (Exception e){
                                    e.printStackTrace();
                                    return null;
                                }
                            }
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                final Map<String, String> headers = new HashMap<String, String>();
                                headers.put("Authorization", "Token " + api_key);
                                return headers;
                            }
                        };

                        // Add the request to the RequestQueue.
                        queue.add(req);
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
                                String texto;
                                if(more==true){
                                    texto=textView.getText().toString();
                                }else{
                                    texto = "";
                                    textView.setText("");
                                }
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

                                for (FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks()) {
                                    for (FirebaseVisionText.Line line: block.getLines()) {
                                        String lineText = line.getText();
                                        lineText=lineText.toLowerCase();
                                        Point[] lineCornerPoints = line.getCornerPoints();
                                        if(lineText.contains("valor pago")||lineText.contains("eur")||lineText.contains("valor total")||lineText.contains("valor")||lineText.contains("total a pagar")||lineText.contains("preco")||lineText.contains("prego")){
                                            //////valor////////////
                                            int y1, y2;
                                            y1=lineCornerPoints[0].y-100;
                                            y2=lineCornerPoints[1].y+100;
                                            for (FirebaseVisionText.TextBlock block2: firebaseVisionText.getTextBlocks()) {
                                                for (FirebaseVisionText.Line line2 : block2.getLines()) {
                                                    for (FirebaseVisionText.Element element : line2.getElements()) {
                                                        String elementText = element.getText();
                                                        Point[] elementCornerPoints = element.getCornerPoints();
                                                        int elementY1, elementY2;
                                                        elementY1=elementCornerPoints[0].y;
                                                        elementY2=elementCornerPoints[2].y;
                                                        if((y1<elementY1 && elementY1<y2)||(y1<elementY2 && elementY2<y2)){
                                                            Pattern p = Pattern.compile("[0-9]+\\,[0-9]{2}");
                                                            Matcher m = p.matcher(elementText);
                                                            boolean b = m.matches();
                                                            Log.e("elementos",elementText+" "+b);
                                                            if(m.matches()){
                                                                elementText=elementText.replace(",",".");
                                                                Double valorElemento=Double.valueOf(elementText);
                                                                if(valorElemento>valor){
                                                                    valor=valorElemento;
                                                                }
                                                                Log.e("eeeeeeeeeeeeeeeeeeeeee", valor+"");
                                                                Log.e("eeeeeeeeeeeeeeeeeeeeee2", valorElemento+"");
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        /////////////////DATA///////////////////
                                        for (FirebaseVisionText.Element element: line.getElements()) {
                                            String elementText = element.getText();
                                            Pattern p = Pattern.compile("[0-9]{1,4}\\-[0-9]{1,2}\\-[0-9]{1,4}");
                                            Matcher m = p.matcher(elementText);
                                            boolean b = m.matches();
                                            if(m.matches()){
                                                try {
                                                    Date date1=new SimpleDateFormat("yyyy-MM-dd").parse(elementText);
                                                    if (date1.after(dataTeste)){
                                                        dataTeste=date1;
                                                        data=elementText;
                                                        Log.e("dddddddddddddddd", data);
                                                    }

                                                }catch (Exception e){
                                                    data=elementText;
                                                    Log.e("dddddddddddddddd", data);
                                                }

                                            }
                                            p = Pattern.compile("[0-9]{1,4}\\/[0-9]{1,2}\\/[0-9]{1,4}");
                                            m = p.matcher(elementText);
                                            b = m.matches();
                                            if(m.matches()){
                                                try {
                                                    Date date1=new SimpleDateFormat("yyyy-MM-dd").parse(elementText);
                                                    if (date1.after(dataTeste)){
                                                        dataTeste=date1;
                                                        data=elementText;
                                                        Log.e("dddddddddddddddd", data);
                                                    }

                                                }catch (Exception e){
                                                    data=elementText;
                                                    Log.e("dddddddddddddddd", data);
                                                }
                                            }
                                        }
                                        ////////////////NIF//////////////////
                                        for (FirebaseVisionText.Element element: line.getElements()) {
                                            String elementText = element.getText();
                                            Pattern p = Pattern.compile("[0-9]{9}");
                                            Matcher m = p.matcher(elementText);
                                            boolean b = m.matches();
                                            if(m.matches()&&elementText.equals("508207908")){
                                                nif=elementText;
                                                Log.e("nnnnnnnnnnnnnnnn", nif);
                                            }
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

        File outFile = new File(path);

        mCameraFileName = outFile.toString();
        Uri outuri = Uri.fromFile(outFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
        startActivityForResult(intent, CAMARA);
    }

    private void pickFromGallery() {
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
                  /*  Uri selectedImage = data.getData();

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

                        imageView.setImageBitmap(bitmap);
                        fatura_original=bitmap;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    try {
                    Uri selectedImage = data.getData();
                    Bitmap bitmap2;

                        bitmap2 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

                    //////https://android--code.blogspot.com/2015/09/android-how-to-save-image-to-file-in.html
                    /////////////////////////////////////////////////////////////////////
                    File fileSelected;
                    String path = Environment.getExternalStorageDirectory()+"/ExpensesOCR/fatura.jpg";

                    fileSelected = new File(path);

                    try{

                        OutputStream stream = null;

                        stream = new FileOutputStream(fileSelected);

                        bitmap2.compress(Bitmap.CompressFormat.JPEG,100,stream);

                        stream.flush();

                        stream.close();

                    }catch (IOException e) // Catch the exception
                    {
                        e.printStackTrace();
                    }

                    // Parse the saved image path to uri
                    Uri savedImageURI = Uri.parse(fileSelected.getAbsolutePath());
                    Log.e("path", savedImageURI.getPath());
                    File file6=new File(savedImageURI.getPath());

                        image = Uri.fromFile(file6);
                        imageView.setImageDrawable(null);
                        imageView.setImageURI(image);
                        imageView.setVisibility(View.VISIBLE);
                        fatura_original=bitmap2;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ////////////////////////////////////////////////////////////////////
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.topbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_logout:
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
                finish();
                return true;
        }
        return false;

    }
}
