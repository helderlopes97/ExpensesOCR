package com.ipt.expensesocr;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.vision.L;

import java.util.Map;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    Preference edit;
    String nif;
    SharedPreferences sharedPreferences;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //adicionar as preferences ao fragment
        addPreferencesFromResource(R.xml.preferences);

        //verifica se é recebido algum argumento
        if (getArguments() != null) {
            nif = getArguments().getString("nif");
        }
        //procura a preference valorNIF
        edit=findPreference("valorNIF");
        //altera o summary
        edit.setSummary(nif);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // receber a preference que foi alterada
        Preference preference= findPreference(key);
        Log.e("key", key);

        // se for um editTextPreference
        if (preference instanceof EditTextPreference) {
            //atualiza summary
            updateSummary((EditTextPreference) preference);
        }
    }

    private void updateSummary(EditTextPreference preference) {
        //altera o summary para o valor atual da preference
        preference.setSummary(preference.getText());
    }
}
