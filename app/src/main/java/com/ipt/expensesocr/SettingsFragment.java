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

        // Adicionar as preferences ao fragment
        addPreferencesFromResource(R.xml.preferences);

        // Verifica se é recebido algum argumento
        if (getArguments() != null) {
            nif = getArguments().getString("nif");
        }

        // Procura a preference valorNIF
        edit = findPreference("valorNIF");
        // Altera o summary
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
        // Receber a preference que foi alterada
        Preference preference= findPreference(key);
        // Se for um editTextPreference
        if (preference instanceof EditTextPreference) {
            // Atualiza summary
            updateSummary((EditTextPreference) preference);
        }
    }

    private void updateSummary(EditTextPreference preference) {
        // Altera o summary para o valor atual da preference
        preference.setSummary(preference.getText());
    }
}
