package com.ipt.expensesocr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

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
            getFragmentManager().beginTransaction().add(R.id.fragment_container, new SettingsFragment()).commit();
        }
    }
}
