package com.ipt.expensesocr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class login extends AppCompatActivity {

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

        final Button login = (Button) findViewById(R.id.btLogin);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userTextView = findViewById(R.id.username);
                username = userTextView.getText().toString();
                pwTextView = findViewById(R.id.password);
                password = userTextView.getText().toString();

                err = (TextView) findViewById(R.id.loginErr);

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
                queue.add(req);
            }
        });

    }

}
