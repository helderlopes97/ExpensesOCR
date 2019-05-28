package com.ipt.expensesocr;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.List;

public class Formulario extends AppCompatActivity{

    Spinner spinner;
    Spinner spinnerRefeicao;
    View viewRefeicao;
    View idk;


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
        start();
    }

    public void start(){
        spinner = (Spinner) findViewById(R.id.spinnerTipo);
        spinnerRefeicao = (Spinner) findViewById(R.id.spinnerRefeição);
        viewRefeicao = findViewById(R.id.refeição);
        idk=findViewById(R.id.idk);
        viewRefeicao.setVisibility(View.GONE);
        idk.setVisibility(View.VISIBLE);

        addItemsToSpinners();
        addListenertiSpinner();
    }

    public void addItemsToSpinners(){
        List<String> list = new ArrayList<String>();
        list.add("Transport");
        list.add("Alojamento");
        list.add("Refeições");
        list.add("Despesas Diversas");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        List<String> list2 = new ArrayList<String>();
        list2.add("Almoço");
        list2.add("Jantar");
        list2.add("Refeição com clientes");
        list2.add("Refeição de grupo");
        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list2);
        dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRefeicao.setAdapter(dataAdapter2);
    }

    public void addListenertiSpinner(){
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(spinner.getSelectedItem().equals("Refeições")){
                    Log.e("spinner",spinner.getSelectedItem().toString());
                    viewRefeicao.setVisibility(View.VISIBLE);
                    idk.setVisibility(View.GONE);
                }else{
                    viewRefeicao.setVisibility(View.GONE);
                    idk.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

}
