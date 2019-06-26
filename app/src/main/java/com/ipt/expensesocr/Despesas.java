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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Despesas extends AppCompatActivity {

    // Variáveis globais
    String email;
    String token;
    String deslocamentoId;
    LinearLayout myLayout;
    // TextViews
    TextView descricao;
    TextView intervalo;
    TextView valorEsperado;
    TextView valorReal;
    TextView numeroDespesas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Mostrar o ecrã das despesas
        setContentView(R.layout.activity_despesas);

        // Ativa a toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Recebe o id do deslocamento e as variáveis de login
        final Bundle intent = getIntent().getExtras();
        deslocamentoId = intent.getString("deslocamentoId");
        token = intent.getString("token");
        email = intent.getString("email");

        // Referencia os elementos gráficos
        myLayout = findViewById(R.id.myLayout);
        descricao = findViewById(R.id.descricao);
        intervalo = findViewById(R.id.intervalo);
        valorEsperado = findViewById(R.id.valorEsperado);
        valorReal = findViewById(R.id.valorReal);
        numeroDespesas = findViewById(R.id.numeroDespesas);

        // Programa a barra de opções
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    // Botão voltar
                    case R.id.action_back:
                        // Prepara a atividade dos deslocamentos
                        Intent intent = new Intent(Despesas.this, Deslocamentos.class);
                        // Envia o email e o token para a nova atividade
                        intent.putExtra("token",(String) token);
                        intent.putExtra("email",(String) email);
                        // Inicia a atividade
                        startActivity(intent);
                        // Termina a atividade das despesas
                        finish();
                        break;

                    // Botão adicionar
                    case R.id.action_add:
                        // Prepara a atividade para inserir uma despesa
                        Intent intent2 = new Intent(Despesas.this, Faturas.class);
                        // Envia o id do deslocamento, email e token para a nova atividade
                        intent2.putExtra("deslocamentoId",(String) deslocamentoId +"");
                        intent2.putExtra("token",(String) token);
                        intent2.putExtra("email",(String) email);
                        // Inicia a atividade
                        startActivity(intent2);
                        // Termina a atividade das despesas
                        finish();
                        break;

                    // Botão atualizar
                    case R.id.action_refresh:
                        // Volta a pedir as despesas
                        getDespesas();
                        break;

                }
                return true;
            }
        });
        // Pede as despesas
        getDespesas();
    }

    /**
     * Pede as despesas do deslocamento através da API
     */
    public void getDespesas(){

        // Inicia o RequestQueue
        RequestQueue queue = Volley.newRequestQueue(Despesas.this);

        // Link para a API
        String url ="https://davidnoob.herokuapp.com/api/v1/requests/"+deslocamentoId;

        // Pede uma String de resposta ao URL
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject res) {
                        try {
                            // Mostra os detalhes do deslocamento
                            descricao.setText( res.get("description").toString());
                            intervalo.setText(res.get("start_date").toString()+" até "+res.get("end_date").toString());
                            valorEsperado.setText(res.get("estimated_value").toString());
                            valorReal.setText(res.get("real_value").toString());
                            numeroDespesas.setText(res.get("expenses_count").toString());
                            // Cria um objeto para as despesas do deslocamento
                            JSONArray despesas= (JSONArray) res.get("expenses");
                            // Limpa a lista de despesas
                            myLayout.removeAllViews();
                            // Percorre as despesas
                            for (int i = 0; i < despesas.length(); i++) {
                                // Cria um objeto para a despesa
                                JSONObject despesa=(JSONObject) despesas.get(i);
                                // Cria um objeto para a categoria da despesa
                                JSONObject categoria=(JSONObject) despesa.get("expense_category");
                                // Cria um objeto para o tipo de despesa
                                JSONObject tipo=(JSONObject) categoria.get("expense_type");
                                // Cria a despesa
                                myLayout.addView(createDespesa(
                                        despesa.get("expense_date").toString(),
                                        despesa.get("expense_value").toString(),
                                        categoria.get("name").toString(),
                                        tipo.get("name").toString()
                                ));
                            }
                        } catch (JSONException e) {
                            // Log do erro
                            e.printStackTrace();
                            Log.e("erro", e.toString());
                            // Limpa a lista de despesas
                            myLayout.removeAllViews();
                            // Cria uma TextView para mostrar o erro
                            TextView text= new TextView(Despesas.this);
                            // Define o layout da TextView
                            text.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                            );
                            // Define a mensagem de erro
                            text.setText("Ocoreu um erro");
                            // Mostra o erro na lista de despesas
                            myLayout.addView(text);
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
     * Cria uma despesa na lista de despesas
     *
     * @param dataDespesa - data da despesa
     * @param valorDespesa - valor da despesa
     * @param categoriaDespesa - categoria da despesa
     * @param tipoDespesa - tipo da despesa
     * @return Layout da despesa
     */
    public LinearLayout createDespesa(
            // Detalhes da despesa
            String dataDespesa,
            String valorDespesa,
            String categoriaDespesa,
            String tipoDespesa
    ){
        // Cria o layout para a despesa
        LinearLayout despesa = new LinearLayout(this);
        despesa.setId(R.id.deslocamento);
        despesa.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layout_725 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        layout_725.bottomMargin = 10;
        layout_725.topMargin = 10;
        despesa.setLayoutParams(layout_725);

        // Cria o layout para a data da despesa
        LinearLayout layoutData = new LinearLayout(this);
        layoutData.setId(R.id.layoutDescricao);
        layoutData.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layout_130 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layout_130.leftMargin = 45;
        layout_130.topMargin = 6;
        layout_130.rightMargin = 45;
        layout_130.bottomMargin = 6;
        layoutData.setLayoutParams(layout_130);

        // TextView para a tag da data
        TextView dateDespesa = new TextView(this);
        dateDespesa.setText("Data: ");
        dateDespesa.setAllCaps(false);
        dateDespesa.setTextSize((15/getApplicationContext().getResources().getDisplayMetrics().scaledDensity));
        LinearLayout.LayoutParams layout_501 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dateDespesa.setTextAppearance(R.style.TextAppearance_AppCompat_Body2);
        }
        dateDespesa.setLayoutParams(layout_501);
        layoutData.addView(dateDespesa);

        // TextView para o campo data da despesa
        TextView data = new TextView(this);
        data.setId(R.id.data);
        data.setText(dataDespesa);
        LinearLayout.LayoutParams layout_454 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            data.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        layout_454.rightMargin = 15;
        data.setLayoutParams(layout_454);
        layoutData.addView(data);
        despesa.addView(layoutData);

        // Cria o layout para o valor da despesa
        LinearLayout layoutValor = new LinearLayout(this);
        layoutValor.setId(R.id.layoutEstado);
        layoutValor.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layout_574 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layout_574.leftMargin = 45;
        layout_574.topMargin = 6;
        layout_574.rightMargin =45;
        layout_574.bottomMargin = 2;
        layoutValor.setLayoutParams(layout_574);

        // TextView para a tag do valor
        TextView tagValor = new TextView(this);
        tagValor.setText("Valor: ");
        LinearLayout.LayoutParams layout_123 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tagValor.setTextAppearance(R.style.TextAppearance_AppCompat_Body2);
        }
        tagValor.setLayoutParams(layout_123);
        layoutValor.addView(tagValor);

        // TextView para o campo valor da despesa
        TextView valor = new TextView(this);
        valor.setId(R.id.valorDespesa);
        valor.setText(valorDespesa);
        LinearLayout.LayoutParams layout_563 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            valor.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        valor.setLayoutParams(layout_563);
        layoutValor.addView(valor);
        despesa.addView(layoutValor);

        // Cria o layout para a categoria da despesa
        LinearLayout layoutCategoria = new LinearLayout(this);
        layoutCategoria.setId(R.id.layoutIntervalo);
        layoutCategoria.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layout_185 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layout_185.leftMargin = 45;
        layout_185.topMargin = 6;
        layout_185.rightMargin = 45;
        layout_185.bottomMargin = 6;
        layoutCategoria.setLayoutParams(layout_185);

        // TextView para a tag da categoria
        TextView tagCategoria = new TextView(this);
        tagCategoria.setText("Categoria: ");
        tagCategoria.setAllCaps(false);
        tagCategoria.setTextSize((15/getApplicationContext().getResources().getDisplayMetrics().scaledDensity));
        LinearLayout.LayoutParams layout_425 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tagCategoria.setTextAppearance(R.style.TextAppearance_AppCompat_Body2);
        }
        tagCategoria.setLayoutParams(layout_425);
        layoutCategoria.addView(tagCategoria);

        // TextView para o campo categoria da despesa
        TextView categoria = new TextView(this);
        categoria.setText(categoriaDespesa);
        LinearLayout.LayoutParams layout_483 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            categoria.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        layout_483.rightMargin = 15;
        categoria.setLayoutParams(layout_483);
        layoutCategoria.addView(categoria);
        despesa.addView(layoutCategoria);

        // Cria o layout para o tipo da despesa
        LinearLayout layoutTipo = new LinearLayout(this);
        layoutTipo.setId(R.id.layoutDespesas);
        layoutTipo.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layout_290 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layout_290.leftMargin = 45;
        layout_290.rightMargin = 45;
        layout_290.bottomMargin = 6;
        layout_290.topMargin = 6;
        layoutTipo.setLayoutParams(layout_290);

        // TextView para a tag do tipo
        TextView tagTipo = new TextView(this);
        tagTipo.setId(R.id.numDespesas);
        tagTipo.setText("Tipo: ");
        tagTipo.setAllCaps(false);
        tagTipo.setTextSize((15/getApplicationContext().getResources().getDisplayMetrics().scaledDensity));
        LinearLayout.LayoutParams layout_71 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tagTipo.setTextAppearance(R.style.TextAppearance_AppCompat_Body2);
        }
        tagTipo.setLayoutParams(layout_71);
        layoutTipo.addView(tagTipo);

        // TextView para o campo tipo da despesa
        TextView tipo = new TextView(this);
        tipo.setId(R.id.tipo);
        tipo.setText(tipoDespesa);
        LinearLayout.LayoutParams layout_296 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tipo.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        }
        layout_296.rightMargin = 60;
        tipo.setLayoutParams(layout_296);
        layoutTipo.addView(tipo);
        despesa.addView(layoutTipo);

        // TextView de separação de despesas
        TextView separador = new TextView(this);
        separador.setId(R.id.textView3);
        separador.setBackgroundResource(R.drawable.linebottom);
        LinearLayout.LayoutParams layout_973 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,4);
        separador.setLayoutParams(layout_973);
        despesa.addView(separador);

        // Devolve o layout da despesa
        return despesa;
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
                Intent intent = new Intent(Despesas.this, Login.class);
                startActivity(intent);
                finish();
                return true;
        }
        return false;

    }
}
