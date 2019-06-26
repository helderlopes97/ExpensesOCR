package com.ipt.expensesocr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import Utils.ImageUtils;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Ativa a toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if(findViewById(R.id.fragment_container)!=null){
            if(savedInstanceState!= null){
                return;
            }
            SharedPreferences sharedPref = (SharedPreferences) PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor mEditor = sharedPref.edit();
            Bundle bundle = new Bundle();
            bundle.putString("nif", sharedPref.getString("valorNIF",""));
            SettingsFragment frag=new SettingsFragment();
            frag.setArguments(bundle);
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
