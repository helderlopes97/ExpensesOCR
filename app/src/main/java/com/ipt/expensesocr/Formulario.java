package com.ipt.expensesocr;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Formulario extends AppCompatActivity{

    // Variáveis Globais
    RelativeLayout layoutSpinner;
    RelativeLayout layoutSpinnerTransporte;
    RelativeLayout layoutSpinnerTipoDespesa;
    Spinner spinner;
    Spinner spinnerRefeicao;
    Spinner spinnerTranporte;
    Spinner spinnerDiversas;
    Spinner spinnerAlojamento;
    Spinner spinnerTipoViatura;
    Spinner spinnerTipoDespesa;
    View viewRefeicao;
    View viewDiversas;
    View viewTransporte;
    View viewAlojamento;
    View viewViatura;
    EditText dataDespesa;
    EditText nifDespesa;
    EditText valorDespesa;
    TextView confidence;
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
    String nifPretendido;
    Date dataInicio;
    Date dataFim;
    int year,monthOfYear,dayOfMonth;


    // Dados partilhados
    SharedPreferences sharedPref;
    SharedPreferences.Editor mEditor;

    final Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Mostra o ecrã do formulário
        setContentView(R.layout.activity_formulario);

        // Ativa a toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        try{
            dataInicio= new SimpleDateFormat("yyyy-MM-dd").parse(intent.getString("dataInicio"));
            Log.e("datainicio",dataInicio.toString());
        }catch (Exception e){
            try{
                dataInicio= new SimpleDateFormat("yyyy/MM/dd").parse(intent.getString("dataInicio"));
                Log.e("datainicio",dataInicio.toString());
            }catch (Exception i){
                e.printStackTrace();
            }
        }

        try{
            dataFim= new SimpleDateFormat("yyyy-MM-dd").parse(intent.getString("dataFim"));
            Log.e("dataFim",dataFim.toString());
        }catch (Exception e){
            try{
                dataFim= new SimpleDateFormat("yyyy/MM/dd").parse(intent.getString("dataFim"));
                Log.e("dataFim",dataFim.toString());
            }catch (Exception i){
                e.printStackTrace();
            }
        }

        try {
            Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(data);
            myCalendar.setTime(date1);
        } catch (ParseException e) {
            try {
                Date date1 = new SimpleDateFormat("yyyy/MM/dd").parse(data);
                myCalendar.setTime(date1);
            } catch (ParseException i) {
                i.getErrorOffset();
            }
        }



        // Receber dados partilhados
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = sharedPref.edit();

        // Coloca o Nif recebido na variavel
        nifPretendido=sharedPref.getString("valorNIF","");

        // Define a tag
        tag = tipo;

        // Referencia os elementos gráficos
        dataDespesa = findViewById(R.id.dataDespesa);
        nifDespesa = findViewById(R.id.nifDespesa);
        valorDespesa = findViewById(R.id.valorDespesa);
        confidence = findViewById(R.id.confidence);
        layoutSpinner = findViewById(R.id.layoutSpinner);
        layoutSpinnerTransporte = findViewById(R.id.layoutSpinnerTransporte);
        spinnerTipoDespesa=findViewById(R.id.spinnerTipoDespesa);
        layoutSpinnerTipoDespesa=findViewById(R.id.layoutSpinnerTipoDespesa);
        spinnerTipoViatura=findViewById(R.id.spinnerTipoViatura);

        confidence.setText(tipo+" - "+perc);

        // Preenche a data, NIF e valor

        // Expressões regulares para a data
        Pattern p = Pattern.compile("[0-9]{1,4}\\-[0-9]{1,2}\\-[0-9]{1,4}");
        Pattern p2 = Pattern.compile("[0-9]{1,4}\\/[0-9]{1,2}\\/[0-9]{1,4}");


        Log.e("data", data+"hi");
        // Compara com o elemento
        Matcher m = p.matcher(data);
        Matcher m2 = p2.matcher(data);
        // Se reconhecer uma data
        if (m.matches() || m2.matches()) {
            try {
                if(myCalendar.getTime().before(dataInicio)||myCalendar.getTime().after(dataFim)){
                    // Muda cor do edtiText
                    dataDespesa.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_red));
                    // Bloqueia a textView dataDespesa
                    dataDespesa.setEnabled(true);
                }else {
                    // Muda cor do edtiText
                    dataDespesa.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                    // Bloqueia a textView dataDespesa
                    dataDespesa.setEnabled(false);
                }
            }catch (Exception e){
                // Muda cor do edtiText
                dataDespesa.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                // Bloqueia a textView dataDespesa
                dataDespesa.setEnabled(false);
            }
            // Define a data
            dataDespesa.setText(data);
            // Definir cor do texto
            dataDespesa.setTextColor(Color.BLACK);
        }else{
            // Muda cor do edtiText
            dataDespesa.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_red));
            dataDespesa.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    // Muda cor do edtiText
                    dataDespesa.setBackground(ContextCompat.getDrawable(Formulario.this,R.drawable.rounded_edittext));
                }

            });

            /*dataDespesa.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    dataDespesa.setBackground(ContextCompat.getDrawable(Formulario.this,R.drawable.rounded_edittext));
                    return false;
                }
            });*/
        }

        // Expressão regular para o NIF
        p = Pattern.compile("[0-9]{9}");
        // Compara com o elemento
        m = p.matcher(nif);
        // Compara o NIF com o definido
        if (m.matches() && nif.equals(nifPretendido)) {
            // Muda cor do edtiText
            nifDespesa.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
            // Define o nif
            nifDespesa.setText(nif);
            // Bloqueia a textView nifDespesa
            nifDespesa.setEnabled(false);
            // Definir cor do texto
            nifDespesa.setTextColor(Color.BLACK);
        }else{
            // Muda cor do edtiText
            nifDespesa.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_red));
           /*nifDespesa.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    // Muda cor do edtiText
                    nifDespesa.setBackground(ContextCompat.getDrawable(Formulario.this,R.drawable.rounded_edittext));
                }
            });*/
            nifDespesa.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    nifDespesa.setBackground(ContextCompat.getDrawable(Formulario.this,R.drawable.rounded_edittext));
                    return false;
                }
            });
        }

        if(!valor.equals("0.0")){
            // Muda cor do edtiText
            valorDespesa.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
            // Define o valor
            valorDespesa.setText(valor);
            // Bloqueia a textView valorDespesa
            valorDespesa.setEnabled(false);
            // Definir cor do texto
            valorDespesa.setTextColor(Color.BLACK);
        }else{
            // Muda cor do edtiText
            valorDespesa.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_red));
          /*  valorDespesa.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    // Muda cor do edtiText
                    valorDespesa.setBackground(ContextCompat.getDrawable(Formulario.this,R.drawable.rounded_edittext));
                }
            });*/
            valorDespesa.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    valorDespesa.setBackground(ContextCompat.getDrawable(Formulario.this,R.drawable.rounded_edittext));
                    return false;
                }
            });

        }

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
                        intent.putExtra("texto",(String) texto);
                        intent.putExtra("deslocamentoId",(String)despesaId);
                        // Inicia a atividade
                        startActivity(intent);
                        // Termina a atividade formulário
                        finish();
                        break;
                    // Botão editar
                    case R.id.action_edit:
                        // Desbloquear todas os spinners e editText
                        spinner.setEnabled(true);
                        spinnerTranporte.setEnabled(true);
                        dataDespesa.setEnabled(true);
                        nifDespesa.setEnabled(true);
                        valorDespesa.setEnabled(true);
                        spinnerTipoDespesa.setEnabled(true);
                        // Mudar cor dos spinners e dos editText
                        try{
                            layoutSpinnerTransporte.setBackground(ContextCompat.getDrawable(Formulario.this,R.drawable.rounded_edittext));
                        }catch (Exception e){
                        }
                        try{
                            layoutSpinnerTipoDespesa.setBackground(ContextCompat.getDrawable(Formulario.this,R.drawable.rounded_edittext));
                        }catch (Exception e){
                        }
                        layoutSpinner.setBackground(ContextCompat.getDrawable(Formulario.this,R.drawable.rounded_edittext));
                        dataDespesa.setBackground(ContextCompat.getDrawable(Formulario.this,R.drawable.rounded_edittext));
                        valorDespesa.setBackground(ContextCompat.getDrawable(Formulario.this,R.drawable.rounded_edittext));
                        nifDespesa.setBackground(ContextCompat.getDrawable(Formulario.this,R.drawable.rounded_edittext));
                        break;
                    // Botão enviar
                    case R.id.action_send:
                        // Verifica valores selecionados nos spinners
                        if(spinner.getSelectedItem().equals("Transporte")){
                            String atual=spinnerTranporte.getSelectedItem().toString();
                            if(atual.equals("Viatura")){
                                atual=spinnerTipoDespesa.getSelectedItem().toString();
                                if (!atual.equals(tipo) || tipo.equals("")){
                                    // Atualiza a tag
                                    tag = atual;
                                }
                            }else {
                                if (!atual.equals(tipo) || tipo.equals("")){
                                    // Atualiza a tag
                                    tag = atual;
                                }
                            }
                        }else{
                            String atual= spinner.getSelectedItem().toString();
                            if(!atual.equals(tipo) || tipo.equals("")){
                                // Atualiza a tag
                                tag = atual;
                            }
                        }
                        //Define tag
                        defineTag();
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
                // Bloqueio do spinner
                spinner.setEnabled(false);
                // Muda cor do spinner
                layoutSpinner.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                // Define o tipo de transporte como comboio
                spinnerTranporte.setSelection(2);
                // Bloqueio do spinnerTransporte
                spinnerTranporte.setEnabled(false);
                // Muda cor do spinner
                layoutSpinnerTransporte.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                break;
            // Metro
            case "Metro":
                // Define o tipo de fatura como transporte
                spinner.setSelection(0);
                // Bloqueio do spinner
                spinner.setEnabled(false);
                // Muda cor do spinner
                layoutSpinner.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                // Define o tipo de transporte como metro
                spinnerTranporte.setSelection(3);
                // Bloqueio do spinnerTransporte
                spinnerTranporte.setEnabled(false);
                // Muda cor do spinner
                layoutSpinnerTransporte.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                break;
            // Taxi
            case "Taxi":
                // Define o tipo de fatura como transporte
                spinner.setSelection(0);
                // Bloqueio do spinner
                spinner.setEnabled(false);
                // Muda cor do spinner
                layoutSpinner.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                // Define o tipo de transporte como taxi
                spinnerTranporte.setSelection(4);
                // Bloqueio do spinnerTransporte
                spinnerTranporte.setEnabled(false);
                // Muda cor do spinner
                layoutSpinnerTransporte.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                break;
            // Autocarro
            case "Autocarro":
                // Define o tipo de fatura como transporte
                spinner.setSelection(0);
                // Bloqueio do spinner
                spinner.setEnabled(false);
                // Muda cor do spinner
                layoutSpinner.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                // Define o tipo de transporte como autocarro
                spinnerTranporte.setSelection(1);
                // Bloqueio do spinnerTransporte
                spinnerTranporte.setEnabled(false);
                // Muda cor do spinner
                layoutSpinnerTransporte.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                break;
            // Combustíveis
            case "Combustíveis":
                // Define o tipo de fatura como transporte
                spinner.setSelection(0);
                // Bloqueio do spinner
                spinner.setEnabled(false);
                // Muda cor do spinner
                layoutSpinner.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                // Define o tipo de transporte como Viatura
                spinnerTranporte.setSelection(0);
                // Bloqueio do spinnerTransporte
                spinnerTranporte.setEnabled(false);
                // Muda cor do spinner
                layoutSpinnerTransporte.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                // Define o tipo de despesa como Combustíveis
                spinnerTipoDespesa.setSelection(0);
                // Bloqueio do spinnerTransporte
                spinnerTipoDespesa.setEnabled(false);
                // Muda cor do spinner
                layoutSpinnerTipoDespesa.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                break;
            // Portagens
            case "Portagens":
                // Define o tipo de fatura como transporte
                spinner.setSelection(0);
                // Bloqueio do spinner
                spinner.setEnabled(false);
                // Muda cor do spinner
                layoutSpinner.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                // Define o tipo de transporte como Viatura
                spinnerTranporte.setSelection(0);
                // Bloqueio do spinnerTransporte
                spinnerTranporte.setEnabled(false);
                // Muda cor do spinner
                layoutSpinnerTransporte.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                // Define o tipo de despesa como Portagens
                spinnerTipoDespesa.setSelection(1);
                // Bloqueio do spinnerTransporte
                spinnerTipoDespesa.setEnabled(false);
                // Muda cor do spinner
                layoutSpinnerTipoDespesa.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                break;
            // Estacionamento
            case "Estacionamento":
                // Define o tipo de fatura como transporte
                spinner.setSelection(0);
                // Bloqueio do spinner
                spinner.setEnabled(false);
                // Muda cor do spinner
                layoutSpinner.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                // Define o tipo de transporte como Viatura
                spinnerTranporte.setSelection(0);
                // Bloqueio do spinnerTransporte
                spinnerTranporte.setEnabled(false);
                // Muda cor do spinner
                layoutSpinnerTransporte.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                // Define o tipo de despesa como Estacionamento
                spinnerTipoDespesa.setSelection(2);
                // Bloqueio do spinnerTransporte
                spinnerTipoDespesa.setEnabled(false);
                // Muda cor do spinner
                layoutSpinnerTipoDespesa.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                break;
            // Aluguer
            case "Aluguer":
                // Define o tipo de fatura como transporte
                spinner.setSelection(0);
                // Bloqueio do spinner
                spinner.setEnabled(false);
                // Muda cor do spinner
                layoutSpinner.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                // Define o tipo de transporte como Viatura
                spinnerTranporte.setSelection(0);
                // Bloqueio do spinnerTransporte
                spinnerTranporte.setEnabled(false);
                // Muda cor do spinner
                layoutSpinnerTransporte.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                // Define o tipo de despesa como Aluguer
                spinnerTipoDespesa.setSelection(3);
                // Bloqueio do spinnerTransporte
                spinnerTipoDespesa.setEnabled(false);
                // Muda cor do spinner
                layoutSpinnerTipoDespesa.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                break;
            case "Refeições":
                // Define o tipo de fatura como Refeição
                spinner.setSelection(2);
                // Bloqueio do spinner
                spinner.setEnabled(false);
                // Muda cor do spinner
                layoutSpinner.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                break;
            case "Alojamento":
                // Define o tipo de fatura como Alojamento
                spinner.setSelection(1);
                // Bloqueio do spinner
                spinner.setEnabled(false);
                // Muda cor do spinner
                layoutSpinner.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                break;
            case "Outros":
                // Define o tipo de fatura como Alojamento
                spinner.setSelection(3);
                // Bloqueio do spinner
                spinner.setEnabled(false);
                // Muda cor do spinner
                layoutSpinner.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_green));
                break;

        }
        // Definir cor do texto
        spinner.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ((TextView) spinner.getSelectedView()).setTextColor(Color.BLACK); //change to your color
            }
        });
        // Definir cor do texto
        spinnerTranporte.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    ((TextView) spinnerTranporte.getSelectedView()).setTextColor(Color.BLACK); //change to your color
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        // Definir cor do texto
        spinnerTipoDespesa.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try{
                    ((TextView) spinnerTipoDespesa.getSelectedView()).setTextColor(Color.BLACK); //change to your color
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });


        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                    myCalendar.set(Calendar.YEAR, year);
                    myCalendar.set(Calendar.MONTH, monthOfYear);
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateLabel();
            }
        };


        dataDespesa.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(Formulario.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);

        dataDespesa.setText(sdf.format(myCalendar.getTime()));
        try {

            if(myCalendar.getTime().before(dataInicio)||myCalendar.getTime().after(dataFim)){
                // Muda cor do edtiText
                dataDespesa.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext_red));
            }else {
                // Muda cor do edtiText
                dataDespesa.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext));
            }
        }catch (Exception e){
            // Muda cor do edtiText
            dataDespesa.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_edittext));
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
        list.add("Outros");
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
        list3.add("Viatura");
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
        // Itens do spinner para o tipo de viatura
        List<String> list6 = new ArrayList<String>();
        list6.add("Própria");
        list6.add("Empresa");
        list6.add("Alugada");
        ArrayAdapter<String> dataAdapter6 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list6);
        dataAdapter6.setDropDownViewResource(R.layout.my_spinner_textview);
        spinnerTipoViatura.setAdapter(dataAdapter6);
        // Itens do spinner para o tipo de despesa
        List<String> list7 = new ArrayList<String>();
        list7.add("Combustível");
        list7.add("Portagens");
        list7.add("Estacionamento");
        list7.add("Aluguer");
        ArrayAdapter<String> dataAdapter7 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list7);
        dataAdapter7.setDropDownViewResource(R.layout.my_spinner_textview);
        spinnerTipoDespesa.setAdapter(dataAdapter7);
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
                    if(spinnerTranporte.getSelectedItem().equals("Viatura")){
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
                // Se o tipo de transporte for viatura
                if( spinnerTranporte.getSelectedItem().equals("Viatura") && spinner.getSelectedItem().equals("Transporte")){
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

    /**
     * Classifica o texto com a tag
     */
    public void defineTag(){
        // Inicia o RequestQueue
        RequestQueue queue = Volley.newRequestQueue(Formulario.this);

        // Link para a API do classificador
        String url = "https://api.monkeylearn.com/v3/classifiers/cl_ZSPc2GNb/data/";

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
                    jsonBody.put("existing_duplicates_strategy","overwrite");
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
