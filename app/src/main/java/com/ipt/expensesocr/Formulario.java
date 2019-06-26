package com.ipt.expensesocr;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

public class Formulario extends AppCompatActivity{

    Spinner spinner;
    Spinner spinnerRefeicao;
    Spinner spinnerTranporte;
    Spinner spinnerDiversas;
    Spinner spinnerAlojamento;
    View viewRefeicao;
    View viewDiversas;
    View viewTransport;
    View viewAlojamento;
    View viewTransporteProprio;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final Bundle intent=getIntent().getExtras();
        despesaId=intent.getString("deslocamentoId");
        token=intent.getString("token");
        email=intent.getString("email");
        valor=intent.getString("valor");
        data=intent.getString("data");
        nif=intent.getString("nif");
        tipo=intent.getString("tipo");
        tipo=tipo.toLowerCase();
        perc=intent.getString("perc");

        dataDespesa=findViewById(R.id.dataDespesa);
        nifDespesa=findViewById(R.id.nifDespesa);
        valorDespesa=findViewById(R.id.valorDespesa);

        dataDespesa.setText(data);
        nifDespesa.setText(nif);
        valorDespesa.setText(valor);
        /*
        if(data.equals("") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            dataDespesa.setBackground(getDrawable(R.drawable.rounded_edittext_red));
        }
        if(valor.equals("0")&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            valorDespesa.setBackground(getDrawable(R.drawable.rounded_edittext_red));
        }
        if(nif.equals("")&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            nifDespesa.setBackground(getDrawable(R.drawable.rounded_edittext_red));
        }*/

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_back:
                        Intent intent = new Intent(Formulario.this, Faturas.class);
                        intent.putExtra("token",(String) token);
                        intent.putExtra("email",(String) email);
                        intent.putExtra("deslocamentoId",(String)despesaId);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.action_send:
                        Toast.makeText(Formulario.this, "Envio não é possivel", Toast.LENGTH_SHORT).show();
                        break;


                }
                return true;
            }
        });

        Log.e("tranport",tipo);
        start();
        switch(tipo){
            case "comboio":
                spinner.setSelection(0);
                spinnerTranporte.setSelection(4);
                break;
            case "metro":
                spinner.setSelection(0);
                spinnerTranporte.setSelection(5);
                break;
            case "taxi":
                spinner.setSelection(0);
                spinnerTranporte.setSelection(6);
                break;
            case "autocarro":
                spinner.setSelection(0);
                spinnerTranporte.setSelection(3);
                break;
        }
    }

    public void start(){
        spinner = (Spinner) findViewById(R.id.spinnerTipo);
        spinnerRefeicao = (Spinner) findViewById(R.id.spinnerRefeição);
        spinnerTranporte = (Spinner) findViewById(R.id.spinnerTransporte);
        spinnerDiversas =(Spinner) findViewById(R.id.spinnerDiversas);
        spinnerAlojamento = (Spinner) findViewById(R.id.spinnerAlojamento);

        viewRefeicao = findViewById(R.id.refeição);
        viewTransport=findViewById(R.id.transport);
        viewDiversas=findViewById(R.id.diversas);
        viewAlojamento = findViewById(R.id.alojamento);
        viewTransporteProprio=findViewById(R.id.transportProprio);

        viewRefeicao.setVisibility(View.GONE);
        viewTransport.setVisibility(View.GONE);
        viewDiversas.setVisibility(View.VISIBLE);
        viewAlojamento.setVisibility(View.GONE);
        viewTransporteProprio.setVisibility(View.GONE);

        addItemsToSpinners();
        addListenertiSpinner();
    }

    public void addItemsToSpinners(){
        List<String> list = new ArrayList<String>();
        list.add("Transporte");
        list.add("Alojamento");
        list.add("Refeições");
        list.add("Despesas Diversas");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list);
        dataAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
        spinner.setAdapter(dataAdapter);

        List<String> list2 = new ArrayList<String>();
        list2.add("Almoço");
        list2.add("Jantar");
        list2.add("Refeição com clientes");
        list2.add("Refeição de grupo");
        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list2);
        dataAdapter2.setDropDownViewResource(R.layout.my_spinner_textview);
        spinnerRefeicao.setAdapter(dataAdapter2);

        List<String> list3 = new ArrayList<String>();
        list3.add("Viatura Própria");
        list3.add("Viatura Empresa");
        list3.add("Viatura Alugada");
        list3.add("Autocarro");
        list3.add("Comboio");
        list3.add("Metro");
        list3.add("Taxi e outros");
        ArrayAdapter<String> dataAdapter3 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list3);
        dataAdapter3.setDropDownViewResource(R.layout.my_spinner_textview);
        spinnerTranporte.setAdapter(dataAdapter3);

        List<String> list4 = new ArrayList<String>();
        list4.add("Marketing & Comunicação");
        list4.add("Recursos Humanos");
        list4.add("Comissões /IS Bancários");
        list4.add("Passaportes /Vistos");
        list4.add("Outras Despesas");
        ArrayAdapter<String> dataAdapter4 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list4);
        dataAdapter4.setDropDownViewResource(R.layout.my_spinner_textview);
        spinnerDiversas.setAdapter(dataAdapter4);

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

    public void addListenertiSpinner(){
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(spinner.getSelectedItem().equals("Refeições")){
                    Log.e("spinner",spinner.getSelectedItem().toString());
                    viewRefeicao.setVisibility(View.VISIBLE);
                    viewTransport.setVisibility(View.GONE);
                    viewDiversas.setVisibility(View.GONE);
                    viewAlojamento.setVisibility(View.GONE);
                    viewTransporteProprio.setVisibility(View.GONE);
                }else if(spinner.getSelectedItem().equals("Alojamento")){
                    viewRefeicao.setVisibility(View.GONE);
                    viewTransport.setVisibility(View.GONE);
                    viewDiversas.setVisibility(View.GONE);
                    viewAlojamento.setVisibility(View.VISIBLE);
                    viewTransporteProprio.setVisibility(View.GONE);
                }else if(spinner.getSelectedItem().equals("Transporte")){
                    viewRefeicao.setVisibility(View.GONE);
                    viewTransport.setVisibility(View.VISIBLE);
                    viewDiversas.setVisibility(View.GONE);
                    viewAlojamento.setVisibility(View.GONE);
                    viewTransporteProprio.setVisibility(View.GONE);
                    if(spinnerTranporte.getSelectedItem().equals("Viatura Própria")){
                        viewTransporteProprio.setVisibility(View.VISIBLE);
                    }
                }else if(spinner.getSelectedItem().equals("Despesas Diversas")){
                    viewRefeicao.setVisibility(View.GONE);
                    viewTransport.setVisibility(View.GONE);
                    viewDiversas.setVisibility(View.VISIBLE);
                    viewAlojamento.setVisibility(View.GONE);
                    viewTransporteProprio.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerTranporte.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(spinnerTranporte.getSelectedItem().equals("Viatura Própria")){
                    viewTransporteProprio.setVisibility(View.VISIBLE);
                }else if(spinnerTranporte.getSelectedItem().equals("Viatura Empresa")){
                    viewTransporteProprio.setVisibility(View.VISIBLE);
                }else if(spinnerTranporte.getSelectedItem().equals("Viatura Alugada")){
                    viewTransporteProprio.setVisibility(View.VISIBLE);
                }else {
                    viewTransporteProprio.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.topbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_logout:
                Intent intent = new Intent(Formulario.this, Login.class);
                startActivity(intent);
                finish();
                return true;
        }
        return false;

    }

}
