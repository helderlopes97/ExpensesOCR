package com.ipt.expensesocr;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;
import static java.lang.Boolean.FALSE;

public class DespesasPendentes extends AppCompatActivity {


    String email;
    String token;

    ScrollView viewDespesas;
    Button buttonRefresh;
    //DespesaPendente despesaPendente;
    LinearLayout myLayout;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_despesas_pendentes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Bundle intent=getIntent().getExtras();
        token=intent.getString("token");
        email=intent.getString("email");


        viewDespesas = findViewById(R.id.scrollViewDespesas);
        buttonRefresh = findViewById(R.id.buttonGetDespesas);
        myLayout = (LinearLayout) findViewById(R.id.myLayout);
        getDespesas();
    }


    protected void getDespesas(){
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*// Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(com.ipt.expensesocr.DespesasPendentes.this);
                String url ="https://my-json-server.typicode.com/helderfoca/ExpensesOCR/despesas";
                // Request a string response from the provided URL.
                JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url,null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray res) {
                            for (int i = 0; i < res.length() ; i++) {
                                try{
                                    JSONObject obj = res.getJSONObject(i);
                                    TextView text= new TextView(DespesasPendentes.this);
                                    text.setLayoutParams(new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.WRAP_CONTENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                            )
                                    );
                                    text.setText(""+res.length());
                                    myLayout.addView(text);
                                    Log.e("here","here");
                                } catch (Exception e){
                                   e.printStackTrace();
                                    myLayout.removeAllViews();
                                    TextView text= new TextView(DespesasPendentes.this);
                                    text.setLayoutParams(new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                            )
                                    );
                                    text.setText("Ocoreu um erro");
                                    myLayout.addView(text);
                                }
                            }

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //viewDespesas.addView(despesaPendente.despesTextView(getApplicationContext(),"Erro ao receber dados."));
                                Log.e("here2","here2");
                            }
                        });

                // Add the request to the RequestQueue.
                queue.add(req);*/

                RequestQueue queue = Volley.newRequestQueue(com.ipt.expensesocr.DespesasPendentes.this);
                String url ="https://davidnoob.herokuapp.com/api/v1/requests";
                // Request a string response from the provided URL.
                JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url,null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray res) {
                                myLayout.removeAllViews();
                                for (int i = 0; i < res.length() ; i++) {
                                    try{
                                        JSONObject obj = res.getJSONObject(i);
                                        JSONObject user =(JSONObject) obj.get("user");
                                        JSONObject estado=(JSONObject) obj.get("request_state");
                                        if((user.get("email")).equals(email)){
                                            myLayout.addView(createDespesa(
                                                    (double) obj.get("id"),
                                                    (String) obj.get("created_at"),
                                                    (String) obj.get("start_date"),
                                                    (String) obj.get("end_date"),
                                                    (String) estado.get("name"),
                                                    (double) obj.get("expenses_count"),
                                                    (double) obj.get("expenses_sum")
                                            ));
                                        }


                                    } catch (Exception e){
                                        e.printStackTrace();
                                        Log.e("erro", e.toString());
                                        myLayout.removeAllViews();
                                        TextView text= new TextView(DespesasPendentes.this);
                                        text.setLayoutParams(new LinearLayout.LayoutParams(
                                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                                )
                                        );
                                        text.setText("Ocoreu um erro");
                                        myLayout.addView(text);
                                        break;
                                    }
                                }


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
        });
    }


    public LinearLayout createDespesa(
            double id,
            String dataPedido,
            String dataStart,
            String dataEnd,
            String estadoDespesa,
            double numeroDespesas,
            double totalDespesas
    ){


        LinearLayout linearLayout_126 = new LinearLayout(this);
        linearLayout_126.setOrientation(VERTICAL);
        linearLayout_126.setId(Integer.parseInt(Double.toString(id)));
        linearLayout_126.setBackgroundResource(R.drawable.rounded_edittext);
        LinearLayout.LayoutParams layout_1000 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        layout_1000.bottomMargin=15;
        linearLayout_126.setLayoutParams(layout_1000);


        LinearLayout linearLayout_808 = new LinearLayout(this);
        linearLayout_808.setOrientation(HORIZONTAL);
        LinearLayout.LayoutParams layout_139 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        layout_139.leftMargin = 15;
        layout_139.topMargin = 2;
        layout_139.rightMargin = 15;
        layout_139.bottomMargin = 2;
        linearLayout_808.setLayoutParams(layout_139);

        TextView textView_173 = new TextView(this);
        textView_173.setText("Data Pedido: ");
        textView_173.setAllCaps(FALSE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView_173.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        LinearLayout.LayoutParams layout_474 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        textView_173.setLayoutParams(layout_474);
        linearLayout_808.addView(textView_173);

        TextView dataCriacao = new TextView(this);
        dataCriacao.setId(R.id.dataCriacao);
        dataCriacao.setText(dataPedido);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dataCriacao.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        LinearLayout.LayoutParams layout_553 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout_553.rightMargin = 5;
        dataCriacao.setLayoutParams(layout_553);
        linearLayout_808.addView(dataCriacao);

        TextView textView_298 = new TextView(this);
        textView_298.setText("Estado: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView_298.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        LinearLayout.LayoutParams layout_962 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        textView_298.setLayoutParams(layout_962);
        linearLayout_808.addView(textView_298);

        TextView estado = new TextView(this);
        estado.setId(R.id.estado);
        estado.setText(estadoDespesa);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            estado.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        LinearLayout.LayoutParams layout_465 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        estado.setLayoutParams(layout_465);
        linearLayout_808.addView(estado);
        linearLayout_126.addView(linearLayout_808);

        LinearLayout linearLayout_491 = new LinearLayout(this);
        linearLayout_491.setOrientation(HORIZONTAL);
        LinearLayout.LayoutParams layout_119 = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layout_119.leftMargin = 15;
        layout_119.topMargin = 2;
        layout_119.rightMargin = 15;
        layout_119.bottomMargin = 2;
        linearLayout_491.setLayoutParams(layout_119);
        TextView textView_878 = new TextView(this);
        textView_878.setText("Data Partida: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView_878.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        textView_878.setAllCaps(FALSE);
        LinearLayout.LayoutParams layout_415 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textView_878.setLayoutParams(layout_415);
        linearLayout_491.addView(textView_878);

        TextView dataPartida = new TextView(this);
        dataPartida.setId(R.id.dataPartida);
        dataPartida.setText(dataStart);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dataPartida.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        LinearLayout.LayoutParams layout_473 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout_473.rightMargin = 5;
        dataPartida.setLayoutParams(layout_473);
        linearLayout_491.addView(dataPartida);

        TextView numDespesas = new TextView(this);
        numDespesas.setId(R.id.numDespesas);
        numDespesas.setText("Nº de despesas: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            numDespesas.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        numDespesas.setAllCaps(FALSE);
        LinearLayout.LayoutParams layout_930 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        numDespesas.setLayoutParams(layout_930);
        linearLayout_491.addView(numDespesas);

        TextView textView_308 = new TextView(this);
        textView_308.setText(Double.toString(numeroDespesas));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView_308.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        LinearLayout.LayoutParams layout_357 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textView_308.setLayoutParams(layout_357);
        linearLayout_491.addView(textView_308);
        linearLayout_126.addView(linearLayout_491);

        LinearLayout linearLayout_47 = new LinearLayout(this);
        linearLayout_47.setOrientation(HORIZONTAL);
        LinearLayout.LayoutParams layout_558 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout_558.leftMargin = 15;
        layout_558.rightMargin = 15;
        layout_558.bottomMargin = 2;
        layout_558.topMargin = 2;
        linearLayout_47.setLayoutParams(layout_558);

        TextView textView_64 = new TextView(this);
        textView_64.setText("Data Chegada: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView_64.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        LinearLayout.LayoutParams layout_634 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textView_64.setLayoutParams(layout_634);
        linearLayout_47.addView(textView_64);

        TextView dataChegada = new TextView(this);
        dataChegada.setId(R.id.dataChegada);
        dataChegada.setText(dataEnd);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dataChegada.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        LinearLayout.LayoutParams layout_260 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout_260.rightMargin = 5;
        dataChegada.setLayoutParams(layout_260);
        linearLayout_47.addView(dataChegada);

        TextView textView_1000 = new TextView(this);
        textView_1000.setText("Valor Total: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView_1000.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        LinearLayout.LayoutParams layout_50 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textView_1000.setLayoutParams(layout_50);
        linearLayout_47.addView(textView_1000);

        TextView valorTotal = new TextView(this);
        valorTotal.setId(R.id.valorTotal);
        valorTotal.setText(Double.toString(totalDespesas));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            valorTotal.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        LinearLayout.LayoutParams layout_33 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        valorTotal.setLayoutParams(layout_33);
        linearLayout_47.addView(valorTotal);
        linearLayout_126.addView(linearLayout_47);

        return linearLayout_126;


    }
}
