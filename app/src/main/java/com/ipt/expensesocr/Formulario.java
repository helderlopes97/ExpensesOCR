package com.ipt.expensesocr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Formulario extends AppCompatActivity{

    // Variáveis Globais
    Spinner spinner;
    Spinner spinnerRefeicao;
    Spinner spinnerTranporte;
    Spinner spinnerDiversas;
    Spinner spinnerAlojamento;
    View viewRefeicao;
    View viewDiversas;
    View viewTransporte;
    View viewAlojamento;
    View viewViatura;
    EditText dataDespesa;
    EditText nifDespesa;
    EditText valorDespesa;
    String email;
    String token;
    String despesaId;
    String valor;
    String data;
    String nif;
    String tipo;
    String perc;
    String texto;
    String tag;
    String api_key;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Mostra o ecrã do formulário
        setContentView(R.layout.activity_formulario);

        // Ativa a toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // I'm fabulous
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Recebe o id do deslocamento, email, token e valores da fatura
        final Bundle intent=getIntent().getExtras();
        despesaId = intent.getString("deslocamentoId");
        token = intent.getString("token");
        email = intent.getString("email");
        valor = intent.getString("valor");
        data = intent.getString("data");
        nif = intent.getString("nif");
        tipo = intent.getString("tipo");
        perc = intent.getString("perc");
        texto = intent.getString("texto");

        // Referencia os elementos gráficos
        dataDespesa=findViewById(R.id.dataDespesa);
        nifDespesa=findViewById(R.id.nifDespesa);
        valorDespesa=findViewById(R.id.valorDespesa);

        // Preenche a data, NIF e valor
        dataDespesa.setText(data);
        nifDespesa.setText(nif);
        valorDespesa.setText(valor);

        // Define a chave da API de reconhecimento do tipo de fatura
        api_key = "b05c81093b4371c50f3aa142184974149d4411b2";

        // Programa a barra de opções
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    // Botão voltar
                    case R.id.action_back:
                        // Prepara a atividade faturas
                        Intent intent = new Intent(Formulario.this, Faturas.class);
                        // Envia o id do deslocamento, email e token
                        intent.putExtra("token",(String) token);
                        intent.putExtra("email",(String) email);
                        intent.putExtra("deslocamentoId",(String)despesaId);
                        // Inicia a atividade
                        startActivity(intent);
                        // Termina a atividade formulário
                        finish();
                        break;
                    // Botão enviar
                    case R.id.action_send:

                        // Inicia o RequestQueue
                        RequestQueue queue = Volley.newRequestQueue(Formulario.this);

                        // Link para a API do classificador
                        String url ="https://api.monkeylearn.com/v3/classifiers/cl_ZSPc2GNb/classify/";

                        // Pede uma String de resposta ao URL
                        StringRequest req = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String res) {

                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
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
                                    JSONObject dataObj = new JSONObject();
                                    dataObj.put("text",texto);
                                    JSONArray tagArray = new JSONArray();
                                    tagArray.put(tag);
                                    dataObj.put("tags",tagArray);
                                    bodyArray.put(dataObj);
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

                        Toast.makeText(Formulario.this, "Envio não é possivel", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
        // Log do tipo de fatura
        Log.e("TIPO: ",tipo);
        // Prepara os elementos do ecrã
        start();
        switch(tipo){
            // Comboio
            case "Comboio":
                // Define o tipo de fatura como transporte
                spinner.setSelection(0);
                // Define o tipo de transporte como comboio
                spinnerTranporte.setSelection(4);
                break;
            // Metro
            case "Metro":
                // Define o tipo de fatura como transporte
                spinner.setSelection(0);
                // Define o tipo de transporte como metro
                spinnerTranporte.setSelection(5);
                break;
            // Taxi
            case "Taxi":
                // Define o tipo de fatura como transporte
                spinner.setSelection(0);
                // Define o tipo de transporte como taxi
                spinnerTranporte.setSelection(6);
                break;
            // Autocarro
            case "Autocarro":
                // Define o tipo de fatura como transporte
                spinner.setSelection(0);
                // Define o tipo de transporte como autocarro
                spinnerTranporte.setSelection(3);
                break;
        }
    }

    /**
     * Prepara os elementos do ecrã
     */
    public void start(){

        // Referencia os Spinners
        spinner = (Spinner) findViewById(R.id.spinnerTipo);
        spinnerRefeicao = (Spinner) findViewById(R.id.spinnerRefeição);
        spinnerTranporte = (Spinner) findViewById(R.id.spinnerTransporte);
        spinnerDiversas = (Spinner) findViewById(R.id.spinnerDiversas);
        spinnerAlojamento = (Spinner) findViewById(R.id.spinnerAlojamento);

        // Referencia as Views
        viewRefeicao = findViewById(R.id.refeição);
        viewTransporte = findViewById(R.id.transport);
        viewDiversas = findViewById(R.id.diversas);
        viewAlojamento = findViewById(R.id.alojamento);
        viewViatura = findViewById(R.id.viatura);

        // Define a View por defeito e esconde as outras
        viewRefeicao.setVisibility(View.GONE);
        viewTransporte.setVisibility(View.VISIBLE);
        viewDiversas.setVisibility(View.GONE);
        viewAlojamento.setVisibility(View.GONE);
        viewViatura.setVisibility(View.GONE);

        // Adiciona os itens aos spinners
        addItemsToSpinners();
        // Adiciona os listeners aos spinners
        addListenerToSpinner();
    }

    /**
     * Adiciona as opções aos spinners
     */
    public void addItemsToSpinners(){
        // Itens do spinner para o tipo de despesa
        List<String> list = new ArrayList<String>();
        list.add("Transporte");
        list.add("Alojamento");
        list.add("Refeições");
        list.add("Despesas Diversas");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list);
        dataAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
        spinner.setAdapter(dataAdapter);
        // Itens do spinner para o tipo de refeição
        List<String> list2 = new ArrayList<String>();
        list2.add("Almoço");
        list2.add("Jantar");
        list2.add("Refeição com clientes");
        list2.add("Refeição de grupo");
        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list2);
        dataAdapter2.setDropDownViewResource(R.layout.my_spinner_textview);
        spinnerRefeicao.setAdapter(dataAdapter2);
        // Itens do spinner para o tipo de transporte
        List<String> list3 = new ArrayList<String>();
        list3.add("Viatura Própria");
        list3.add("Viatura Empresa");
        list3.add("Viatura Alugada");
        list3.add("Autocarro");
        list3.add("Comboio");
        list3.add("Metro");
        list3.add("Taxi");
        ArrayAdapter<String> dataAdapter3 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list3);
        dataAdapter3.setDropDownViewResource(R.layout.my_spinner_textview);
        spinnerTranporte.setAdapter(dataAdapter3);
        // Itens do spinner para a área da despesa diversa
        List<String> list4 = new ArrayList<String>();
        list4.add("Marketing & Comunicação");
        list4.add("Recursos Humanos");
        list4.add("Comissões /IS Bancários");
        list4.add("Passaportes /Vistos");
        list4.add("Outras Despesas");
        ArrayAdapter<String> dataAdapter4 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list4);
        dataAdapter4.setDropDownViewResource(R.layout.my_spinner_textview);
        spinnerDiversas.setAdapter(dataAdapter4);
        // Itens do spinner para o tipo de despesa
        List<String> list5 = new ArrayList<String>();
        list5.add("Hotel");
        list5.add("Motel");
        list5.add("Pensão");
        list5.add("Apartamento");
        list5.add("Outro");
        ArrayAdapter<String> dataAdapter5 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list5);
        dataAdapter5.setDropDownViewResource(R.layout.my_spinner_textview);
        spinnerAlojamento.setAdapter(dataAdapter5);
    }

    /**
     * Adiciona os listeners de eventos aos spinners
     */
    public void addListenerToSpinner(){
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Se o tipo de fatura selecionado for Refeição
                if(spinner.getSelectedItem().equals("Refeições")){
                    // Ativa a View de refeição e esconde as outras
                    viewRefeicao.setVisibility(View.VISIBLE);
                    viewTransporte.setVisibility(View.GONE);
                    viewDiversas.setVisibility(View.GONE);
                    viewAlojamento.setVisibility(View.GONE);
                    viewViatura.setVisibility(View.GONE);
                // Se o tipo de fatura selecionado for Alojamento
                }else if(spinner.getSelectedItem().equals("Alojamento")){
                    // Ativa a View de alojamento e esconde as outras
                    viewRefeicao.setVisibility(View.GONE);
                    viewTransporte.setVisibility(View.GONE);
                    viewDiversas.setVisibility(View.GONE);
                    viewAlojamento.setVisibility(View.VISIBLE);
                    viewViatura.setVisibility(View.GONE);
                // Se o tipo de fatura selecionado for Transporte
                }else if(spinner.getSelectedItem().equals("Transporte")){
                    // Ativa a View de transporte e esconde as outras
                    viewRefeicao.setVisibility(View.GONE);
                    viewTransporte.setVisibility(View.VISIBLE);
                    viewDiversas.setVisibility(View.GONE);
                    viewAlojamento.setVisibility(View.GONE);
                    viewViatura.setVisibility(View.GONE);
                    if(spinnerTranporte.getSelectedItem().equals("Viatura Própria")){
                        viewViatura.setVisibility(View.VISIBLE);
                    }
                // Se o tipo de fatura selecionado for Despesas Diversas
                }else if(spinner.getSelectedItem().equals("Despesas Diversas")){
                    // Ativa a View de despesas diversas e esconde as outras
                    viewRefeicao.setVisibility(View.GONE);
                    viewTransporte.setVisibility(View.GONE);
                    viewDiversas.setVisibility(View.VISIBLE);
                    viewAlojamento.setVisibility(View.GONE);
                    viewViatura.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Adiciona evento ao spinner de tipo de transporte
        spinnerTranporte.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Se o tipo de transporte for viatura própria, da empresa ou alugada
                if( spinnerTranporte.getSelectedItem().equals("Viatura Própria") ||
                    spinnerTranporte.getSelectedItem().equals("Viatura Empresa") ||
                    spinnerTranporte.getSelectedItem().equals("Viatura Alugada") )
                {
                    // Mostra a View de viatura
                    viewViatura.setVisibility(View.VISIBLE);
                }else {
                    // Esconde a View de viatura
                    viewViatura.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
            // Botão logout
            case R.id.action_logout:
                // Acede as shared preferences
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor mEditor = sharedPref.edit();
                // Remove o token
                mEditor.remove("token");
                // Guarda as alterações
                mEditor.commit();
                // Prepara a atividade login
                Intent intent = new Intent(Formulario.this, Login.class);
                // Inicia a atividade
                startActivity(intent);
                // Termina a atividade do formulario
                finish();
                return true;
        }
        return false;

    }

}
