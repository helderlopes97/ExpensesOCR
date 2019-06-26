package com.ipt.expensesocr;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Mostrar o ecrã das definições
        setContentView(R.layout.activity_settings);

        // Ativa a toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if(findViewById(R.id.fragment_container)!=null){
            if(savedInstanceState!= null){
                return;
            }
            // Acede às shared preferences
            SharedPreferences sharedPref = (SharedPreferences) PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor mEditor = sharedPref.edit();
            // cria um bundle
            Bundle bundle = new Bundle();
            //coloca o valor do nif no bundle
            bundle.putString("nif", sharedPref.getString("valorNIF",""));
            //cria o fragment
            SettingsFragment frag=new SettingsFragment();
            //adiciona o bundle ao fragment
            frag.setArguments(bundle);
            // Inicia o SettingsFragment
            getFragmentManager().beginTransaction().add(R.id.fragment_container,frag).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Adiciona os items à topbar
        getMenuInflater().inflate(R.menu.topbar3, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Programa os botões da barra de navegação
        switch (item.getItemId()) {
            // Botão para voltar atrás
            case R.id.action_back:
                finish();
                return true;
        }
        return false;
    }
}
