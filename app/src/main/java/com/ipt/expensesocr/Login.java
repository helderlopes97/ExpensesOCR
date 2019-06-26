package com.ipt.expensesocr;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    // Códigos das permissões
    static final int CAMARA = 100;
    static final int READ_EXTERNAL_STORAGE = 101;
    static  final int WRITE_EXTERNAL_STORAGE= 102;

    // Variáveis globais
    TextView userTextView;
    String email;
    TextView pwTextView;
    String password;
    TextView err;
    CheckBox credenciais;
    boolean loggedin = false;

    SharedPreferences sharedPref;
    SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Mostra o ecrã de login
        setContentView(R.layout.activity_login);

        // Pedido de autorização - Permissões da aplicação

        // Ler memória externa
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            // Pede permissão
            ActivityCompat.requestPermissions(Login.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},READ_EXTERNAL_STORAGE);
        } else {
            // Permissão já autorizada
        }
        // Utilizar câmara
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            // Pede permissão
            ActivityCompat.requestPermissions(Login.this,new String[]{Manifest.permission.CAMERA},CAMARA);
        } else {
            // Permissão já autorizada
        }
        // Escrever na memória externa
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            // Pede permissão
            ActivityCompat.requestPermissions(Login.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_EXTERNAL_STORAGE);
        } else {
            // Permissão já autorizada
        }

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = sharedPref.edit();

        credenciais= findViewById(R.id.checkBox);
        userTextView = findViewById(R.id.email);
        pwTextView = findViewById(R.id.password);

        if(sharedPref.contains("token")&&sharedPref.contains("email")){
            // Prepara a atividade dos deslocamentos
            Intent intent = new Intent(Login.this, Deslocamentos.class);
            // Envia o Token e o email para a nova atividade
            intent.putExtra("token", sharedPref.getString("token",""));
            intent.putExtra("email", sharedPref.getString("email",""));
            // Inicia a atividade
            startActivity(intent);
            // Termina a atividade Login
            finish();
        }
        if(sharedPref.contains("password")){
            userTextView.setText(sharedPref.getString("email",""));
            pwTextView.setText(sharedPref.getString("password",""));
        }

        if(!userTextView.getText().toString().equals("")){
            credenciais.setChecked(true);
        }

        // Botão de login
        final Button login = (Button) findViewById(R.id.btLogin);
        // Função do botão
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login.setEnabled(false);
                // limpa o log de erros
                err = (TextView) findViewById(R.id.loginErr);
                err.setText("");
                // desativa o botão
                login.setActivated(false);
                // encontra o email
                email = userTextView.getText().toString().trim();
                // encontra a password
                password = pwTextView.getText().toString().trim();

                // verifica se os campos estão preenchidos
                if( email.equals("") || password.equals("")){
                    err.setText("Dados incompletos!");
                    login.setEnabled(true);
                    return;
                }

                // Inicia o RequestQueue
                RequestQueue queue = Volley.newRequestQueue(Login.this);

                // Link para a API
                String url ="https://davidnoob.herokuapp.com/api/v1/login/";

                // Pede uma String de resposta ao URL
                StringRequest req = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String res) {
                                try{
                                    // Cria um objeto JSON da resposta
                                    JSONObject obj= new JSONObject(res);
                                    // Verifica o Token
                                    if(obj.has("token")) {
                                        if(credenciais.isChecked()){
                                            mEditor.putString("email",email);
                                            mEditor.putString("password",password);
                                            mEditor.putString("token",(String) obj.get("token"));
                                            mEditor.commit();
                                        }else{
                                            mEditor.putString("email",email);
                                            mEditor.putString("token",(String) obj.get("token"));
                                            if(sharedPref.contains("password")){
                                                mEditor.remove("password");
                                            }
                                            mEditor.commit();
                                        }

                                        // Define o user com loggedin
                                        loggedin = true;
                                        // Prepara a atividade dos deslocamentos
                                        Intent intent = new Intent(Login.this, Deslocamentos.class);
                                        // Envia o Token e o email para a nova atividade
                                        intent.putExtra("token",(String) obj.get("token"));
                                        intent.putExtra("email", email);
                                        // Inicia a atividade
                                        startActivity(intent);
                                        // Termina a atividade Login
                                        finish();
                                    }
                                } catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // Tratamento de erros
                                if(error.networkResponse != null ){
                                    if(error.networkResponse.statusCode == 401) {
                                        // Erro de autenticação
                                        err.setText("Email/password incorretos!");
                                        login.setEnabled(true);
                                    } else {
                                        // Erro inesperado
                                        Log.e("ERROR",  error.networkResponse.statusCode +"");
                                        login.setEnabled(true);
                                        err.setText("Erro! Tente novamente");
                                    }
                                } else {
                                    // Erro inesperado
                                    Log.e("ERROR",  error +"");
                                    err.setText("Erro! Tente novamente");
                                    login.setEnabled(true);
                                }
                            }
                        }){
                    @Override
                    protected Map<String,String> getParams(){
                        // Envia o email e password
                        Map<String ,String> params=new HashMap<String, String>();
                        params.put("email",email);
                        params.put("password",password);
                        return params;
                    }
                };
                // Adiciona o pedido ao RequestQueue
                queue.add(req);
            }
        });
    }


}
