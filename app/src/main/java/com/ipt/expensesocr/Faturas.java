package com.ipt.expensesocr;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
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

public class Faturas extends AppCompatActivity {

    // Códigos das permissões
    static final int CAMARA = 100;
    static final int READ_EXTERNAL_STORAGE = 101;
    static  final int WRITE_EXTERNAL_STORAGE= 102;
    static final int GALLERY_REQUEST_CODE = 103;

    // Variáveis globais
    ImageView imageView;
    TextView textView;
    Bitmap fatura_original;
    Bitmap fatura_atual;
    Bitmap fatura_transformada;
    Uri image;
    String mCameraFileName;
    String path = Environment.getExternalStorageDirectory()+"/ExpensesOCR/fatura.jpg";
    String email;
    String despesaId;
    String token;
    String api_key;
    String tipo;
    String perc;

    // Dados
    Double valor = 0.0;
    String data = "";
    String nif = "";
    Date dataTeste;
    String nifPretendido;

    // Opções
    boolean multipleImage = false;
    boolean cropped = false;
    boolean transformed = false;

    // Dados partilhados
    SharedPreferences sharedPref;
    SharedPreferences.Editor mEditor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicia o OpenCV
        OpenCVLoader.initDebug();
        // Mostra o ecrã das faturas
        setContentView(R.layout.activity_faturas);
        // Inicia o Firebase
        FirebaseApp.initializeApp(this);

        // Ativa a toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Receber dados partilhados
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = sharedPref.edit();

        // Coloca o Nif recebido na variavel
        nifPretendido=sharedPref.getString("valorNIF","");

        // Pedido de autorização - Permissões da aplicação

        // Ler memória externa
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            // Pede permissão
            ActivityCompat.requestPermissions(Faturas.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},READ_EXTERNAL_STORAGE);
        } else {
            // Permissão já autorizada
        }
        // Utilizar câmara
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            // Pede permissão
            ActivityCompat.requestPermissions(Faturas.this,new String[]{Manifest.permission.CAMERA},CAMARA);
        } else {
            // Permissão já autorizada
        }
        // Escrever na memória externa
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            // Pede permissão
            ActivityCompat.requestPermissions(Faturas.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_EXTERNAL_STORAGE);
        } else {
            // Permissão já autorizada
        }

        // Data para comparar com a encontrada por OCR
        try{
            dataTeste = new SimpleDateFormat("yyyy-MM-dd").parse("2000-1-1");
        }catch (Exception e){

        }

        // Referencia elementos gráficos
        imageView = (ImageView) findViewById(R.id.img);
        textView = (TextView) findViewById(R.id.txt);

        // Recebe o id do deslocamento, email e token
        final Bundle intent=getIntent().getExtras();
        despesaId=intent.getString("deslocamentoId");
        token=intent.getString("token");
        email=intent.getString("email");

        // Define a chave da API de reconhecimento do tipo de fatura
        api_key = "b05c81093b4371c50f3aa142184974149d4411b2";

        // Inicia as variáveis
        fatura_original = BitmapFactory.decodeFile(path);
        fatura_atual = fatura_original;
        fatura_transformada = fatura_original;

        // Mostra a última fatura usada
        imageView.setImageBitmap(fatura_original);

        // Programa a barra de opções
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    // Botão camara
                    case R.id.action_camera:
                        // Verifica se tem permissão
                        if (ContextCompat.checkSelfPermission(Faturas.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            // Se não tiver, volta a pedir
                            ActivityCompat.requestPermissions(Faturas.this, new String[]{Manifest.permission.CAMERA}, CAMARA);
                        } else {
                            // Se tiver, usa a camera
                            camera();
                        }
                        break;
                    // Botão galeria
                    case R.id.action_gallery:
                        // Escolhe foto através da galeria
                        pickFromGallery();
                        break;
                    // Botão para fazer crop
                    case R.id.action_crop:
                        // Referencia o butão
                        BottomNavigationView navView = (BottomNavigationView) findViewById(R.id.bar);
                        Menu menu = navView.getMenu();
                        MenuItem crop = menu.findItem(R.id.action_crop);
                        // Se não estiver cropped
                        if (!cropped){
                            try {
                                // Transforma a imagem
                                fatura_atual = ImageUtils.transform(fatura_atual);
                                // Mostra a imagem transformada - cropped
                                imageView.setImageBitmap(fatura_atual);
                                // Atualiza o estado para cropped = true
                                cropped = true;
                                // Altera o titulo do botão para "Normal"
                                crop.setTitle("Normal");
                            // Em caso de erro
                            } catch (Exception e) {
                                // Log do erro
                                e.printStackTrace();
                                // Se estiver transformada
                                if(transformed){
                                    fatura_atual = fatura_transformada;
                                } else {
                                    // Altera a fatura para a original
                                    fatura_atual = fatura_original;
                                }
                                // Mostra a fatura original
                                imageView.setImageBitmap(fatura_atual);
                                // Atualiza o estado para cropped = false
                                cropped = false;
                                // Altera o titulo do botão para "Cortar"
                                crop.setTitle("Cortar");
                            }
                        // Se já estiver cropped
                        } else {
                            // Caso tenha sido transformada
                            if(transformed){
                                // Volta para a fatura anterior - transformada
                                fatura_atual = fatura_transformada;
                            // Se não estiver transformada
                            } else {
                                // Volta para a fatura anterior - original
                                fatura_atual = fatura_original;
                            }
                            // Mostra a fatura anterior
                            imageView.setImageBitmap(fatura_atual);
                            // Atualiza o estado para cropped = false
                            cropped = false;
                            // Altera o titulo do botão para "Cortar"
                            crop.setTitle("Cortar");
                        }
                        break;
                    // Botão para detetar (OCR)
                    case R.id.action_detect:
                        try {
                            // Faz reconhecimento na fatura atual
                            runTextRecognition(fatura_atual);
                        } catch (Exception e) {
                            // Log do erro
                            e.printStackTrace();
                            // Em caso de erro - Mostra e faz reconhecimento da fatura original
                            imageView.setImageBitmap(fatura_original);
                            runTextRecognition(fatura_original);
                        }
                        break;
                    // Botão Avançar
                    case R.id.action_next:


                        // Inicia o RequestQueue
                        RequestQueue queue = Volley.newRequestQueue(Faturas.this);

                        // Link para a API do classificador
                        String url ="https://api.monkeylearn.com/v3/classifiers/cl_ZSPc2GNb/classify/";

                        // Pede uma String de resposta ao URL
                        StringRequest req = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String res) {
                                        try{
                                            // Cria um Array JSON para a resposta
                                            JSONArray array= new JSONArray(res);
                                            // Cria um Objeto JSON para o conteúdo do Array
                                            JSONObject obj = (JSONObject) array.get(0);
                                            // Se o objeto conter a tag "classifications"
                                            if(obj.has("classifications")) {
                                                // Cria um Array JSON para as "classifications"
                                                JSONArray classifArray = (JSONArray) obj.get("classifications");
                                                // Cria um Objeto JSON para o conteúdo do Array
                                                JSONObject classifcObj = (JSONObject) classifArray.get(0);
                                                // Recebe o "tag_name" com o tipo de fatura reconhecido
                                                String tag = classifcObj.get("tag_name").toString();
                                                // Recebe a "confidence" com a certeza do classificador
                                                String conf = classifcObj.get("confidence").toString();
                                                // Define o tipo pela tag
                                                tipo = tag;
                                                // Define a percentagem da certeza pela confiança
                                                perc = Double.valueOf(conf) * 100 + "%";
                                                // Log do tipo e percentagem
                                                Log.e("TIPO: ", tipo);
                                                Log.e("PERCENTAGEM: ", perc);
                                                // Prepara a atividade do formulario de envio
                                                Intent intent = new Intent(Faturas.this, Formulario.class);
                                                // Envia o id do deslocamento, token, email, valor, data, nif, tipo e percentagem
                                                intent.putExtra("deslocamentoId",(String) despesaId+"");
                                                intent.putExtra("token",(String) token);
                                                intent.putExtra("email",(String) email);
                                                intent.putExtra("valor",(String) ""+valor);
                                                intent.putExtra("data",(String) data);
                                                intent.putExtra("nif",(String) nif);
                                                intent.putExtra("tipo",(String) tipo);
                                                intent.putExtra("perc",(String) perc);
                                                intent.putExtra("texto", textView.getText().toString());
                                                // Inicia a atividade
                                                startActivity(intent);
                                                // Termina a atividade das faturas
                                                 finish();
                                            }
                                        } catch (Exception e){
                                            // Log do erro
                                            e.printStackTrace();
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("erro", error.networkResponse.allHeaders.get(1)+"");
                                    }
                                }){

                            @Override
                            public String getBodyContentType() {
                                // Define o tipo de informação enviada no body
                                return "application/json; charset=utf-8";
                            }

                            @Override
                            public byte[] getBody() throws AuthFailureError {
                                try{
                                    // Define o body da mensagem a enviar - texto obtido por OCR
                                    JSONObject jsonBody = new JSONObject();
                                    JSONArray bodyArray = new JSONArray();
                                    bodyArray.put(textView.getText().toString());
                                    jsonBody.put("data",bodyArray);
                                    return jsonBody.toString().getBytes("utf-8");
                                } catch (Exception e){
                                    // Log do erro
                                    e.printStackTrace();
                                    return null;
                                }
                            }
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                // Define o cabeçalho de autenticação no pedido
                                final Map<String, String> headers = new HashMap<String, String>();
                                headers.put("Authorization", "Token " + api_key);
                                return headers;
                            }
                        };
                        // Adiciona o pedido ao RequestQueue
                        queue.add(req);
                }
                return true;
            }
        });
    }

    /**
     * Faz o reconhecimento de caracteres (OCR) de uma imagem
     *
     * @param fatura Bitmap da fatura para OCR
     */
    private void runTextRecognition(Bitmap fatura) {
        // Cria os objetos do Firebase para a imagem e detetor
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(fatura);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        // Cria a tarefa a fazer com o resultado do OCR
        Task<FirebaseVisionText> result = detector.processImage(image)
                // Em caso de sucesso
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                // String que guarda o texto reconhecido
                                String texto;
                                // Caso - opção de imagem múltipla
                                if (multipleImage) {
                                    // O texto é igual ao que já estiver na TextView
                                    texto = textView.getText().toString();
                                    // Caso - imagem única
                                } else {
                                    // O texto ainda não tem caracteres
                                    texto = "";
                                    // Limpa o texto da TextView
                                    textView.setText("");
                                }
                                // Percorre os blocos de texto decifrados
                                for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                                    // Adiciona um ENTER no texto
                                    texto += "\n";
                                    // Percorre as linhas do bloco
                                    for (FirebaseVisionText.Line line : block.getLines()) {
                                        // Adiciona um ENTER no texto
                                        texto += "\n";

                                        /* ############ PROCURA DO VALOR ############*/

                                        // String com a linha do bloco
                                        String lineText = line.getText();
                                        // Mete o texto em minúsculas
                                        lineText = lineText.toLowerCase();
                                        // Pontos da linha
                                        Point[] lineCornerPoints = line.getCornerPoints();
                                        // Se a linha tiver uma das seguintes expressões
                                        if (lineText.contains("valor pago") ||
                                                lineText.contains("eur") ||
                                                lineText.contains("valor total") ||
                                                lineText.contains("valor") ||
                                                lineText.contains("total a pagar") ||
                                                lineText.contains("preco") ||
                                                lineText.contains("prego")) {

                                            // Margem de procura
                                            int y1, y2;
                                            y1 = lineCornerPoints[0].y - 100;
                                            y2 = lineCornerPoints[1].y + 100;
                                            // Percorre os blocos
                                            for (FirebaseVisionText.TextBlock block2 : firebaseVisionText.getTextBlocks()) {
                                                // Percorre as linhas
                                                for (FirebaseVisionText.Line line2 : block2.getLines()) {
                                                    // Percorre os elementos
                                                    for (FirebaseVisionText.Element element : line2.getElements()) {
                                                        // Texto do elemento
                                                        String elementText = element.getText();
                                                        // Pontos do elemento
                                                        Point[] elementCornerPoints = element.getCornerPoints();
                                                        // Posição do elemento
                                                        int elementY1, elementY2;
                                                        elementY1 = elementCornerPoints[0].y;
                                                        elementY2 = elementCornerPoints[2].y;
                                                        // Verifica se um dos pontos está dentro da margem de procura
                                                        if ((y1 < elementY1 && elementY1 < y2) || (y1 < elementY2 && elementY2 < y2)) {
                                                            // Expressão regular para o valor
                                                            Pattern p = Pattern.compile("[0-9]+\\,[0-9]{2}");
                                                            // Compara com o elemento
                                                            Matcher m = p.matcher(elementText);
                                                            // Se reconhecer um valor
                                                            if (m.matches()) {
                                                                // Troca a "," do valor por um "."
                                                                elementText = elementText.replace(",", ".");
                                                                // Transforma a String com o valor em double
                                                                Double valorElemento = Double.valueOf(elementText);
                                                                // Verifica se é o maior valor
                                                                if (valorElemento > valor) {
                                                                    // Atualiza o valor
                                                                    valor = valorElemento;
                                                                }
                                                                // Log dos valores
                                                                Log.e("VALOR GLOBAL: ", valor + "");
                                                                Log.e("VALOR ELEMENTO: ", valorElemento + "");
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        /* ######### FIM DA PROCURA DO VALOR #########*/


                                        // Percorre os elementos das linhas
                                        for (FirebaseVisionText.Element element : line.getElements()) {
                                            // String com o elemento da linha
                                            String elementText = element.getText();
                                            // Adiciona o elemento ao texto
                                            texto += elementText + " ";

                                            /* ############ PROCURA DA DATA ############*/

                                            // Expressões regulares para a data
                                            Pattern p = Pattern.compile(".*?([0-9]{1,4}\\-[0-9]{1,2}\\-[0-9]{1,4})");
                                            Pattern p2 = Pattern.compile(".*?([0-9]{1,4}\\/[0-9]{1,2}\\/[0-9]{1,4})");
                                            // Compara com o elemento
                                            Matcher m = p.matcher(elementText);
                                            Matcher m2 = p2.matcher(elementText);
                                            // Se reconhecer uma data
                                            if (m.matches() || m2.matches()) {
                                                Log.e("true","true");
                                                try {
                                                    elementText=m.group(0);
                                                }catch (Exception e){
                                                    elementText=m2.group(0);
                                                }


                                                try {
                                                    // Transforma a data
                                                    Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(elementText);
                                                    // Compara qual a data mais recente
                                                    if (date1.after(dataTeste)) {
                                                        // Atualiza a data para comparar
                                                        dataTeste = date1;
                                                        // Atualiza a String da data
                                                        data = elementText;
                                                    }
                                                } catch (Exception e) {
                                                    // Log do erro
                                                    e.printStackTrace();
                                                    try {
                                                        // Transforma a data
                                                        Date date1 = new SimpleDateFormat("yyyy/MM/dd").parse(elementText);
                                                        // Compara qual a data mais recente
                                                        if (date1.after(dataTeste)) {
                                                            // Atualiza a data para comparar
                                                            dataTeste = date1;
                                                            // Atualiza a String da data
                                                            data = elementText;
                                                        }
                                                    } catch (Exception er) {
                                                        // Log do erro
                                                        er.printStackTrace();

                                                    }
                                                }
                                                // Log da data
                                                Log.e("DATA: ", data);
                                            }

                                            /* ############ PROCURA DO NIF ############*/

                                            // Expressão regular para o NIF
                                            p = Pattern.compile("[0-9]{9}");
                                            // Compara com o elemento
                                            m = p.matcher(elementText);
                                            // Compara o NIF com o definido
                                            if (m.matches() && elementText.equals(nifPretendido)) {
                                                // Atualiza o NIF
                                                nif = elementText;
                                                // Log do NIF
                                                Log.e("NIF: ", nif);
                                            }

                                            /* ######################################## */
                                        }
                                    }
                                }
                                // Mostra o texto final na TextView
                                textView.setText(texto);
                                // Log do texto reconehcido
                                Log.e("TEXTO: ", textView.getText().toString());
                            }
                        }
                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // O reconhecimento falhou
                                textView.setText("Fail");
                            }
                        }
                );
    }

    /**
     * Utiliza a camara para tirar fotografia à fatura
     */
    private void camera() {
        // StrictMode
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        // Prepara a atividade
        Intent intent = new Intent();
        // Define a ação de captura de imagem
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        // Cria um ficheiro no path
        File outFile = new File(path);
        // String do ficheiro
        mCameraFileName = outFile.toString();
        // Cria um Uri do ficheiro
        Uri outuri = Uri.fromFile(outFile);
        // Envia o Uri do ficheiro
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
        // Inicia a atividade da camera
        startActivityForResult(intent, CAMARA);
    }

    /**
     * Escolhe uma fatura da galeria
     */
    private void pickFromGallery() {
        // Prepara a atividade da galeria
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Inicia a atividade da galeria
        startActivityForResult(intent,GALLERY_REQUEST_CODE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Ficheiro
        File file;
        // Se a atividade terminou com sucesso
        if (resultCode == Activity.RESULT_OK)
            // Verifica qual a atividade
            switch (requestCode) {
                // Atividade camara
                case CAMARA:
                    // Define o bitmap da fatura
                    fatura_original = BitmapFactory.decodeFile(path);
                    // Define a imagem atual como a original
                    fatura_atual = fatura_original;
                    // Define o Uri da imagem através do ficheiro
                    image = Uri.fromFile(new File(mCameraFileName));
                    // Remove a imagem anterior
                    imageView.setImageDrawable(null);
                    // Define a nova imagem
                    imageView.setImageURI(image);
                    // Mostra a imagem
                    imageView.setVisibility(View.VISIBLE);
                    // Cria o ficheiro
                    file = new File(mCameraFileName);
                    // Se o ficheiro ainda não existir
                    if (!file.exists()) {
                        // Cria a diretoria
                        file.mkdir();
                    }
                    break;
                case GALLERY_REQUEST_CODE:
                    try {
                        // Cria um Uri da imagem selecionada
                        Uri selectedImage = data.getData();
                        // Bitmap da imagem selecionada
                        Bitmap bit = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        // Guarda o Bitmap da imagem
                        fatura_original = bit;
                        // Define a imagem atual como a original
                        fatura_atual = fatura_original;
                        // Guarda o Uri
                        image = selectedImage;
                        // Remove a imagem anterior
                        imageView.setImageDrawable(null);
                        // Define a nova imagem
                        imageView.setImageURI(image);
                        // Mostra a imagem
                        imageView.setVisibility(View.VISIBLE);
                        // File para guardar a imagem
                        File fileSelected = new File(path);
                        try{
                            // Stream de dados
                            OutputStream stream = null;
                            // Inicia a stream
                            stream = new FileOutputStream(fileSelected);
                            // Comprime a imagem
                            bit.compress(Bitmap.CompressFormat.JPEG,100,stream);
                            // Envia os dados
                            stream.flush();
                            // Fecha a stream
                            stream.close();
                        }catch (IOException e)
                        {
                            // Log do erro
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        // Log do erro
                        e.printStackTrace();
                    }
                    break;
            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Adiciona os items à topbar
        getMenuInflater().inflate(R.menu.topbar2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Referencia o botão crop
        BottomNavigationView navView = (BottomNavigationView) findViewById(R.id.bar);
        Menu menu = navView.getMenu();
        MenuItem crop = menu.findItem(R.id.action_crop);
        // Programa os botões da barra de navegação
        switch (item.getItemId()) {
            // Botão para voltar atrás
            case R.id.action_back:
                // Prepara a atividade anterior
                Intent intent = new Intent(Faturas.this, Despesas.class);
                // Envia o id do deslocamento, email e token
                intent.putExtra("deslocamentoId", (String) despesaId + "");
                intent.putExtra("token", (String) token);
                intent.putExtra("email", (String) email);
                // Inicia a atividade
                startActivity(intent);
                // Termina a atividade das faturas
                finish();
                return true;
            // Botão imagem múltipla
            case R.id.action_addmore:
                // Ativa ou desativa Imagem Múltipla
                multipleImage = !multipleImage;
                return true;
            // Botão imagem original
            case R.id.action_original:
                // A fatua atual é a original
                fatura_atual = fatura_original;
                // Mostra a fatura original
                imageView.setImageBitmap(fatura_atual);
                // Altera o titulo do botão crop para "Cortar"
                crop.setTitle("Cortar");
                // Define o estado cropped = false
                cropped = false;
                // Define o estado transformed = false
                transformed = false;
                return true;
            case R.id.action_transform:
                // Se não estiver transformada
                if(!transformed) {
                    try {
                        // Transforma a imagem - threshold
                        fatura_transformada = ImageUtils.threshold(fatura_atual);
                        // A atual é a transformada
                        fatura_atual = fatura_transformada;
                        // Mostra a imagem transformada
                        imageView.setImageBitmap(fatura_atual);
                        // Altera o estado transformed = true
                        transformed = true;
                        // Se estava cropped
                        if(cropped){
                            // Altera o titulo do botão crop para "Cortar"
                            crop.setTitle("Cortar");
                            // Altera o estado cropped = false
                            cropped = false;
                        }
                    } catch (Exception e) {
                        // Log do erro
                        e.printStackTrace();
                        // A fatura atual é a original
                        fatura_atual = fatura_original;
                        // Mostra a fatura original
                        imageView.setImageBitmap(fatura_atual);
                        // Altera o estado transformed = false
                        transformed = false;
                        // Se estava cropped
                        if(cropped) {
                            // Altera o titulo do botão crop para "Cortar"
                            crop.setTitle("Cortar");
                            // Altera o estado cropped = false
                            cropped = false;
                        }
                    }
                }
                return true;
            // Botão logout
            case R.id.action_logout:
                // Acede às shared preferences
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor mEditor = sharedPref.edit();
                // Remove o token
                mEditor.remove("token");
                // Guarda as alterações
                mEditor.commit();
                // Prepara a atividade login
                intent = new Intent(Faturas.this, Login.class);
                // Inicia a atividade
                startActivity(intent);
                // Termina a atividade das faturas
                finish();
                return true;
        }
        return false;
    }
}
