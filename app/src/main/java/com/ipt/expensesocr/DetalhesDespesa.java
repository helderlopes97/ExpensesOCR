package com.ipt.expensesocr;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DetalhesDespesa extends AppCompatActivity {
    String email;
    String token;
    String despesaId;
    LinearLayout myLayout;

    //views
    TextView descricao;
    TextView intervalo;
    TextView valorEsperado;
    TextView valorReal;
    TextView numeroDespesas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_despesa);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        final Bundle intent=getIntent().getExtras();
        despesaId=intent.getString("despesaId");
        token=intent.getString("token");
        email=intent.getString("email");

        myLayout=findViewById(R.id.myLayout);
        //textView=findViewById(R.id.despesaId);
        //textView.setText(despesaId);

        descricao=findViewById(R.id.descricao);
        intervalo=findViewById(R.id.intervalo);
        valorEsperado=findViewById(R.id.valorEsperado);
        valorReal=findViewById(R.id.valorReal);
        numeroDespesas=findViewById(R.id.numeroDespesas);


        //button=findViewById(R.id.button);
        /*button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetalhesDespesa.this, MainActivity.class);
                intent.putExtra("despesaId",(String) despesaId+"");
                intent.putExtra("token",(String) token);
                startActivity(intent);
                finish();
            }
        });*/
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_back:
                        Intent intent = new Intent(DetalhesDespesa.this, DespesasPendentes.class);
                        intent.putExtra("token",(String) token);
                        intent.putExtra("email",(String) email);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.action_add:
                        Intent intent2 = new Intent(DetalhesDespesa.this, MainActivity.class);
                        intent2.putExtra("despesaId",(String) despesaId+"");
                        intent2.putExtra("token",(String) token);
                        intent2.putExtra("email",(String) email);
                        startActivity(intent2);
                        finish();
                        break;
                    case R.id.action_refresh:
                        getDespesas();
                        break;

                }
                return true;
            }
        });

        getDespesas();
    }

    public void getDespesas(){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(DetalhesDespesa.this);
        String url ="https://davidnoob.herokuapp.com/api/v1/requests/"+despesaId;

        // Request a string response from the provided URL.
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject res) {
                        try {
                            descricao.setText( res.get("description").toString());
                            intervalo.setText(res.get("start_date").toString()+" até "+res.get("end_date").toString());
                            valorEsperado.setText(res.get("estimated_value").toString());
                            valorReal.setText(res.get("real_value").toString());
                            numeroDespesas.setText(res.get("expenses_count").toString());
                            JSONArray despesas= (JSONArray) res.get("expenses");
                            myLayout.removeAllViews();
                            for (int i = 0; i < despesas.length(); i++) {
                                JSONObject despesa=(JSONObject) despesas.get(i);
                                JSONObject categoria=(JSONObject) despesa.get("expense_category");
                                JSONObject tipo=(JSONObject) categoria.get("expense_type");

                                myLayout.addView(criarDespesa(
                                        despesa.get("expense_date").toString(),
                                        despesa.get("expense_value").toString(),
                                        categoria.get("name").toString(),
                                        tipo.get("name").toString()
                                ));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("erro", e.toString());
                            myLayout.removeAllViews();
                            TextView text= new TextView(DetalhesDespesa.this);
                            text.setLayoutParams(new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.WRAP_CONTENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                            );
                            text.setText("Ocoreu um erro");
                            myLayout.addView(text);
                        }
                        /*myLayout.removeAllViews();
                        for (int i = 0; i <  ; i++) {
                            try{
                               // JSONObject obj = res.getJSONObject(i);
                               // JSONObject user =(JSONObject) obj.get("user");
                               // JSONObject estado=(JSONObject) obj.get("request_state");
                               // if((user.get("email")).equals(email)){
                                    myLayout.addView(criarDespesa(
                                            /*obj.get("id").toString(),
                                            obj.get("start_date").toString(),
                                            obj.get("end_date").toString(),
                                            estado.get("name").toString(),
                                            obj.get("expenses_count").toString(),
                                            obj.get("expenses_sum").toString(),
                                            obj.get("description").toString()
                                    ));
                                //}


                            } catch (Exception e){
                                e.printStackTrace();
                                Log.e("erro", e.toString());
                                myLayout.removeAllViews();
                                TextView text= new TextView(DetalhesDespesa.this);
                                text.setLayoutParams(new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        )
                                );
                                text.setText("Ocoreu um erro");
                                myLayout.addView(text);
                                break;
                            }
                        }*/


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //viewDespesas.addView(despesaPendente.despesTextView(getApplicationContext(),"Erro ao receber dados."));
                    }
                }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                final Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(req);
    }

    public View criarDespesa(
            String dataDespesa,
            String valorDespesa,
            String categoriaDespesa,
            String tipoDespesa
    ){
        LinearLayout despesa = new LinearLayout(this);
        despesa.setId(R.id.despesa);
        despesa.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layout_725 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        layout_725.bottomMargin = 10;
        layout_725.topMargin = 10;
        despesa.setLayoutParams(layout_725);

        LinearLayout layoutDescricao = new LinearLayout(this);
        layoutDescricao.setId(R.id.layoutDescricao);
        layoutDescricao.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layout_130 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layout_130.leftMargin = 45;
        layout_130.topMargin = 6;
        layout_130.rightMargin = 45;
        layout_130.bottomMargin = 6;
        layoutDescricao.setLayoutParams(layout_130);

        TextView textView_145 = new TextView(this);
        textView_145.setText("Data: ");
        textView_145.setAllCaps(false);
        textView_145.setTextSize((15/getApplicationContext().getResources().getDisplayMetrics().scaledDensity));
        LinearLayout.LayoutParams layout_501 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView_145.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        textView_145.setLayoutParams(layout_501);
        layoutDescricao.addView(textView_145);

        TextView data = new TextView(this);
        data.setId(R.id.data);
        data.setText(dataDespesa);
        LinearLayout.LayoutParams layout_454 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            data.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        layout_454.rightMargin = 15;
        data.setLayoutParams(layout_454);
        layoutDescricao.addView(data);
        despesa.addView(layoutDescricao);

        LinearLayout layoutEstado = new LinearLayout(this);
        layoutEstado.setId(R.id.layoutEstado);
        layoutEstado.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layout_574 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layout_574.leftMargin = 45;
        layout_574.topMargin = 6;
        layout_574.rightMargin =45;
        layout_574.bottomMargin = 2;
        layoutEstado.setLayoutParams(layout_574);

        TextView textView_53 = new TextView(this);
        textView_53.setText("Valor: ");
        LinearLayout.LayoutParams layout_123 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView_53.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        textView_53.setLayoutParams(layout_123);
        layoutEstado.addView(textView_53);

        TextView valor = new TextView(this);
        valor.setId(R.id.valorDespesa);
        valor.setText(valorDespesa);
        LinearLayout.LayoutParams layout_563 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            valor.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        valor.setLayoutParams(layout_563);
        layoutEstado.addView(valor);
        despesa.addView(layoutEstado);

        LinearLayout layoutIntervalo = new LinearLayout(this);
        layoutIntervalo.setId(R.id.layoutIntervalo);
        layoutIntervalo.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layout_185 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layout_185.leftMargin = 45;
        layout_185.topMargin = 6;
        layout_185.rightMargin = 45;
        layout_185.bottomMargin = 6;
        layoutIntervalo.setLayoutParams(layout_185);

        TextView textView_392 = new TextView(this);
        textView_392.setText("Categoria: ");
        textView_392.setAllCaps(false);
        textView_392.setTextSize((15/getApplicationContext().getResources().getDisplayMetrics().scaledDensity));
        LinearLayout.LayoutParams layout_425 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView_392.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        textView_392.setLayoutParams(layout_425);
        layoutIntervalo.addView(textView_392);

        TextView categoria = new TextView(this);
        categoria.setText(categoriaDespesa);
        LinearLayout.LayoutParams layout_483 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            categoria.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        layout_483.rightMargin = 15;
        categoria.setLayoutParams(layout_483);
        layoutIntervalo.addView(categoria);
        despesa.addView(layoutIntervalo);

        LinearLayout layoutDespesas = new LinearLayout(this);
        layoutDespesas.setId(R.id.layoutDespesas);
        layoutDespesas.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layout_290 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layout_290.leftMargin = 45;
        layout_290.rightMargin = 45;
        layout_290.bottomMargin = 6;
        layout_290.topMargin = 6;
        layoutDespesas.setLayoutParams(layout_290);

        TextView numDespesas = new TextView(this);
        numDespesas.setId(R.id.numDespesas);
        numDespesas.setText("Tipo: ");
        numDespesas.setAllCaps(false);
        numDespesas.setTextSize((15/getApplicationContext().getResources().getDisplayMetrics().scaledDensity));
        LinearLayout.LayoutParams layout_71 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            numDespesas.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        numDespesas.setLayoutParams(layout_71);
        layoutDespesas.addView(numDespesas);

        TextView tipo = new TextView(this);
        tipo.setId(R.id.tipo);
        tipo.setText(tipoDespesa);
        LinearLayout.LayoutParams layout_296 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tipo.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        layout_296.rightMargin = 60;
        tipo.setLayoutParams(layout_296);
        layoutDespesas.addView(tipo);
        despesa.addView(layoutDespesas);

        TextView textView3 = new TextView(this);
        textView3.setId(R.id.textView3);
        textView3.setBackgroundResource(R.drawable.linebottom);
        LinearLayout.LayoutParams layout_973 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,4);
        textView3.setLayoutParams(layout_973);
        despesa.addView(textView3);

        return despesa;
    }
}
