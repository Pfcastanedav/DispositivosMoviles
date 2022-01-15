package edu.unal.reto9maps;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class settings extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final EditTextPreference radioPref = (EditTextPreference) findPreference("Radio");
        String radioMessage = prefs.getString("Radio", getResources().getString(R.string.radio));
        radioPref.setSummary(radioMessage);

        radioPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                radioPref.setSummary((CharSequence) newValue);
                // Since we are handling the pref, we must save it
                SharedPreferences.Editor ed = prefs.edit();
                ed.putString("Radio", newValue.toString());
                ed.commit();
                MapsActivity.radius = Integer.parseInt(newValue.toString());
                MapsActivity.updateCircle();
                return true;
            }
        });
    }
}
