package com.ipt.expensesocr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class login extends AppCompatActivity {

    static final int CAMARA = 100;
    static final int READ_EXTERNAL_STORAGE = 101;
    static  final int WRITE_EXTERNAL_STORAGE= 102;

    TextView userTextView;
    String username;
    TextView pwTextView;
    String password;
    TextView err;
    boolean loggedin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //PERMISSIONS
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(login.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},READ_EXTERNAL_STORAGE);
        } else {
            // Permission has already been granted
        }
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(login.this,new String[]{Manifest.permission.CAMERA},CAMARA);

                   /* if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED){
                        if(!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)){
                            requestPermissions(new String[]{permission}),
                                    SMS_PERMISSION);
                        }
                    }*/
        } else {
            // Permission has already been granted
        }
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(login.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_EXTERNAL_STORAGE);
        } else {
            // Permission has already been granted
        }


        final Button login = (Button) findViewById(R.id.btLogin);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userTextView = findViewById(R.id.username);
                username = userTextView.getText().toString().trim();
                pwTextView = findViewById(R.id.password);
                password = pwTextView.getText().toString().trim();

                err = (TextView) findViewById(R.id.loginErr);


                /*
                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(com.ipt.expensesocr.login.this);
                String url ="https://my-json-server.typicode.com/helderfoca/ExpensesOCR/users";

                // Request a string response from the provided URL.
                JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url,null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray res) {
                                for (int i = 0; i < res.length() ; i++) {
                                    try{
                                        JSONObject obj = res.getJSONObject(i);
                                        String user = obj.getString("username");
                                        String pw = obj.getString("password");
                                        Log.d("asd", "username: " + user);
                                        Log.d("asd", "password: " + pw);
                                        Log.d("asd", "username: " + username);
                                        Log.d("asd", "password: " + password);
                                        if(user.equals(username) && pw.equals(password)){
                                            loggedin = true;
                                            Intent intent = new Intent(login.this,Formulario.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    } catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                                if(!loggedin){
                                    err.setText("Username ou Password incorretos!");
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                err.setText("Erro! Tente novamente");
                            }
                        });

                // Add the request to the RequestQueue.
                queue.add(req);*/



                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(com.ipt.expensesocr.login.this);
                String url ="https://davidnoob.herokuapp.com/api/v1/login/";

                // Request a string response from the provided URL.
                StringRequest req = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String res) {
                                    try{
                                        JSONObject obj= new JSONObject(res);
                                        Log.e("obj",obj.toString());
                                        Log.e("er", ""+obj.has("token"));
                                        if(obj.has("token")) {
                                            loggedin = true;
                                            Intent intent = new Intent(login.this, DespesasPendentes.class);
                                            Log.e("token",(String) obj.get("token"));
                                            intent.putExtra("token",(String) obj.get("token"));
                                            intent.putExtra("email", username);
                                            startActivity(intent);
                                            finish();
                                        }
                                    } catch (Exception e){
                                        e.printStackTrace();
                                    }

                                if(!loggedin){
                                    err.setText("Username ou Password incorretos!");
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                err.setText("Erro! Tente novamente");
                                Log.e("erroidk", error.toString());
                            }
                        }){

                            @Override
                            protected Map<String,String> getParams(){
                                Map<String ,String> params=new HashMap<String, String>();
                                params.put("email",username);
                                params.put("password",password);
                                return params;
                            }

                         };

                // Add the request to the RequestQueue.
                queue.add(req);

            }
        });

    }

}
