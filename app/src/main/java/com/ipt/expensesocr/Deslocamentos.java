package com.ipt.expensesocr;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;

public class Deslocamentos extends AppCompatActivity {

    // Variáveis globais
    String email;
    String token;
    ScrollView viewDeslocamentos;
    LinearLayout myLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Mostra o ecrã dos deslocamentos
        setContentView(R.layout.activity_deslocamentos);

        // Ativa a toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Recebe as variáveis do login
        Bundle intent=getIntent().getExtras();
        token=intent.getString("token");
        email=intent.getString("email");

        // Referencia elementos gráficos
        viewDeslocamentos = findViewById(R.id.scrollViewDeslocamentos);
        myLayout = (LinearLayout) findViewById(R.id.myLayout);

        // Programa a barra de opções
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    // Botão atualizar
                    case R.id.action_refresh:
                        // Volta a pedir os deslocamentos
                        getDeslocamentos();
                        break;

                    // Botão pesquisar
                    case R.id.action_search:
                        // Não faz nada
                        Toast.makeText(Deslocamentos.this, "Search", Toast.LENGTH_SHORT).show();
                        break;

                    // Botão definições
                    case R.id.action_settings:
                        // Não faz nada
                        Toast.makeText(Deslocamentos.this, "Setting", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });

        // Pede os deslocamentos
        getDeslocamentos();
    }

    /**
     * Pede os deslocamentos do utilizador através da API
     */
    protected void getDeslocamentos(){

        // Inicia o RequestQueue
        RequestQueue queue = Volley.newRequestQueue(Deslocamentos.this);

        // Link para a API
        String url ="https://davidnoob.herokuapp.com/api/v1/requests";

        // Pede uma String de resposta ao URL
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray res) {
                        // Limpa a lista de deslocamentos
                        myLayout.removeAllViews();
                        // Percorre os deslocamentos da resposta
                        for (int i = 0; i < res.length() ; i++) {
                            try{
                                // Cria um objeto JSON de um deslocamento
                                JSONObject obj = res.getJSONObject(i);
                                // Cria um objeto para o utilizador
                                JSONObject user =(JSONObject) obj.get("user");
                                // Cria um objeto para o estado do deslocamento
                                JSONObject estado=(JSONObject) obj.get("request_state");
                                // Cria a View do deslocamento
                                myLayout.addView(createDeslocamento(
                                         obj.get("id").toString(),
                                         obj.get("start_date").toString(),
                                         obj.get("end_date").toString(),
                                         estado.get("name").toString(),
                                         obj.get("expenses_count").toString(),
                                         obj.get("expenses_sum").toString(),
                                         obj.get("description").toString()
                                ));
                            } catch (Exception e){
                                // Log do erro
                                e.printStackTrace();
                                Log.e("erro", e.toString());
                                // Limpa a lista de deslocamentos
                                myLayout.removeAllViews();
                                // Cria uma TextView para mostrar o erro
                                TextView text = new TextView(Deslocamentos.this);
                                // Define o layout da TextView
                                text.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                );
                                // Define a mensagem de erro
                                text.setText("Ocoreu um erro");
                                // Mostra o erro na lista de deslocamentos
                                myLayout.addView(text);
                                break;
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // Define o cabeçalho de autenticação no pedido
                final Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        // Adiciona o pedido ao RequestQueue
        queue.add(req);
    }

    /**
     * Cria um deslocamento na lista de deslocamentos
     *
     * @param id - id do deslocamento
     * @param dataStart - data ida
     * @param dataEnd - data volta
     * @param estadoDeslocamento - estado do deslocamento
     * @param numeroDespesas - número de despesas
     * @param totalDespesas - total das despesas
     * @param descricaoDeslocamento - descrição do deslocamento
     * @return Layout do deslocamento
     */
    public LinearLayout createDeslocamento(
            // Detalhes do deslocamento
            final String id,
            String dataStart,
            String dataEnd,
            String estadoDeslocamento,
            String numeroDespesas,
            String totalDespesas,
            String descricaoDeslocamento
    ){
        // String para o intervalo do deslocamento
        dataEnd = dataEnd.substring(0,10);
        dataStart = dataStart.substring(0,10);
        String datas = dataStart+" até "+dataEnd;

        // Cria o layout para o deslocamento
        LinearLayout deslocamento = new LinearLayout(this);
        deslocamento.setId(R.id.deslocamento);
        deslocamento.setOrientation(VERTICAL);
        deslocamento.setBackgroundResource(R.drawable.rounded_edittext);

        // Cria o layout para os detalhes do deslocamento
        LinearLayout.LayoutParams layout_750 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layout_750.bottomMargin = 15;
        deslocamento.setLayoutParams(layout_750);

        // Cria o layout para a descrição do deslocamento
        LinearLayout layoutDescricao = new LinearLayout(this);
        layoutDescricao.setId(R.id.layoutDescricao);
        layoutDescricao.setOrientation(HORIZONTAL);
        LinearLayout.LayoutParams layout_155 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout_155.leftMargin = 30;
        layout_155.topMargin = 6;
        layout_155.rightMargin = 30;
        layout_155.bottomMargin = 6;
        layoutDescricao.setLayoutParams(layout_155);

        // Cria a TextView para a tag descrição
        TextView descricao = new TextView(this);
        descricao.setId(R.id.descricao);
        descricao.setText("Descrição: ");
        descricao.setTextSize((15/getApplicationContext().getResources().getDisplayMetrics().scaledDensity));
        LinearLayout.LayoutParams layout_587 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            descricao.setTextAppearance(R.style.TextAppearance_AppCompat_Body2);
        }
        descricao.setLayoutParams(layout_587);
        layoutDescricao.addView(descricao);

        // Cria a TextView para o campo descrição
        TextView valorDescricao = new TextView(this);
        valorDescricao.setId(R.id.valorDescricao);
        valorDescricao.setText(descricaoDeslocamento);
        LinearLayout.LayoutParams layout_591 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            valorDescricao.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        layout_591.rightMargin = 5;
        valorDescricao.setLayoutParams(layout_591);
        layoutDescricao.addView(valorDescricao);
        deslocamento.addView(layoutDescricao);

        // Cria o layout para a estado do deslocamento
        LinearLayout layoutEstado = new LinearLayout(this);
        layoutEstado.setId(R.id.layoutEstado);
        layoutEstado.setOrientation(HORIZONTAL);
        LinearLayout.LayoutParams layout_700 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout_700.leftMargin = 30;
        layout_700.topMargin = 6;
        layout_700.rightMargin = 30;
        layout_700.bottomMargin = 6;
        layoutEstado.setLayoutParams(layout_700);

        // Cria a TextView para a tag estado
        TextView estado = new TextView(this);
        estado.setId(R.id.estado);
        estado.setText("Estado: ");
        LinearLayout.LayoutParams layout_311 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            estado.setTextAppearance(R.style.TextAppearance_AppCompat_Body2);
        }
        estado.setLayoutParams(layout_311);
        layoutEstado.addView(estado);

        // Cria a TextView para o campo estado
        TextView valorEstado = new TextView(this);
        valorEstado.setId(R.id.valorEstado);
        valorEstado.setText(estadoDeslocamento);
        LinearLayout.LayoutParams layout_761 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            valorEstado.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        valorEstado.setLayoutParams(layout_761);
        layoutEstado.addView(valorEstado);
        deslocamento.addView(layoutEstado);

        // Cria o layout para o intervalo do deslocamento
        LinearLayout layoutIntervalo = new LinearLayout(this);
        layoutIntervalo.setId(R.id.layoutIntervalo);
        layoutIntervalo.setOrientation(HORIZONTAL);
        LinearLayout.LayoutParams layout_895 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout_895.leftMargin = 30;
        layout_895.topMargin = 6;
        layout_895.rightMargin = 30;
        layout_895.bottomMargin = 6;
        layoutIntervalo.setLayoutParams(layout_895);

        // Cria a TextView para a tag intervalo
        TextView intervalo = new TextView(this);
        intervalo.setId(R.id.intervalo);
        intervalo.setText("Intervalo: ");
        intervalo.setTextSize((15/getApplicationContext().getResources().getDisplayMetrics().scaledDensity));
        LinearLayout.LayoutParams layout_993 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intervalo.setTextAppearance(R.style.TextAppearance_AppCompat_Body2);
        }
        intervalo.setLayoutParams(layout_993);
        layoutIntervalo.addView(intervalo);

        // Cria a TextView para o campo intervalo
        TextView valorIntervalo = new TextView(this);
        valorIntervalo.setId(R.id.valorIntervalo);
        valorIntervalo.setText(datas);
        LinearLayout.LayoutParams layout_379 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            valorIntervalo.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        layout_379.rightMargin = 5;
        valorIntervalo.setLayoutParams(layout_379);
        layoutIntervalo.addView(valorIntervalo);
        deslocamento.addView(layoutIntervalo);

        // Cria o layout para as despesas do deslocamento
        LinearLayout layoutNumDespesas = new LinearLayout(this);
        layoutNumDespesas.setId(R.id.layoutDespesas);
        layoutNumDespesas.setOrientation(HORIZONTAL);
        LinearLayout.LayoutParams layout_346 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout_346.leftMargin = 30;
        layout_346.rightMargin = 30;
        layout_346.bottomMargin = 6;
        layout_346.topMargin = 6;
        layoutNumDespesas.setLayoutParams(layout_346);

        // Cria a TextView para a tag número de despesas
        TextView numDespesas = new TextView(this);
        numDespesas.setId(R.id.numDespesas);
        numDespesas.setText("Nº de despesas: ");
        numDespesas.setTextSize((15/getApplicationContext().getResources().getDisplayMetrics().scaledDensity));
        LinearLayout.LayoutParams layout_172 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            numDespesas.setTextAppearance(R.style.TextAppearance_AppCompat_Body2);
        }
        numDespesas.setLayoutParams(layout_172);
        layoutNumDespesas.addView(numDespesas);

        // Cria a TextView para o campo número de despesas
        TextView ValorNumDespesas = new TextView(this);
        ValorNumDespesas.setText(numeroDespesas);
        LinearLayout.LayoutParams layout_283 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ValorNumDespesas.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        layout_283.rightMargin = 50;
        ValorNumDespesas.setLayoutParams(layout_283);
        layoutNumDespesas.addView(ValorNumDespesas);

        // Cria a TextView para a tag valor total
        TextView total = new TextView(this);
        total.setText("Valor Total: ");
        LinearLayout.LayoutParams layout_125 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            total.setTextAppearance(R.style.TextAppearance_AppCompat_Body2);
        }
        total.setLayoutParams(layout_125);
        layoutNumDespesas.addView(total);

        // Cria a TextView para o campo valor total
        TextView valorTotal = new TextView(this);
        valorTotal.setText(totalDespesas);
        LinearLayout.LayoutParams layout_83 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            valorTotal.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        valorTotal.setLayoutParams(layout_83);
        layoutNumDespesas.addView(valorTotal);
        deslocamento.addView(layoutNumDespesas);

        // Função de click num deslocamento
        deslocamento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Prepara a atividade dos detalhes e despesas do deslocamento
                Intent intent = new Intent(Deslocamentos.this, Despesas.class);
                // Envia o id, token e email para a nova atividade
                intent.putExtra("deslocamentoId",(String) id+"");
                intent.putExtra("token",(String) token);
                intent.putExtra("email",(String) email);
                // Inicia a atividade
                startActivity(intent);
                // Termina a atividade Deslocamentos
                finish();
            }
        });
        // Devolve o layout do deslocamento
        return deslocamento;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Adiciona os items à topbar
        getMenuInflater().inflate(R.menu.topbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Programa os botões da barra de navegação
        switch (item.getItemId()) {
            case R.id.action_logout:
                Intent intent = new Intent(Deslocamentos.this, Login.class);
                startActivity(intent);
                finish();
                return true;
        }
        return false;
    }
}
