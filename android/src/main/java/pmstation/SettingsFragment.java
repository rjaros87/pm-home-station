/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */

package pmstation;

import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    Preference.OnPreferenceChangeListener numberCheckListener = (preference, newValue) -> {
        //Check that the string is an integer.
        if (!newValue.toString().equals("") && newValue.toString().matches("\\d*")) {
            return true;
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.invalid_number), Toast.LENGTH_SHORT).show();
            return false;
        }
    };

    Preference.OnPreferenceChangeListener macCheckListener = (preference, newValue) -> {
        if (!BluetoothAdapter.checkBluetoothAddress(newValue.toString())) {
            Toast.makeText(getActivity(), getResources().getString(R.string.invalid_mac), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            view.setBackgroundColor(getResources().getColor(android.R.color.background_light));
        }
        Preference pm25norm = findPreference("pm_25_norm");
        pm25norm.setOnPreferenceChangeListener(numberCheckListener);
        Preference pm10norm = findPreference("pm_10_norm");
        pm10norm.setOnPreferenceChangeListener(numberCheckListener);
        Preference btMac = findPreference("bt_mac");
        btMac.setOnPreferenceChangeListener(macCheckListener);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                             .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        ValuesFragment valuesFragment =
                (ValuesFragment) getActivity().getSupportFragmentManager()
                                              .findFragmentByTag(MainActivity.VALUES_FRAGMENT);

        if (key.equals("sampling_interval")) {
            ((MainActivity) getActivity()).wakeConnection();
            return;
        }
        if (valuesFragment == null) {
            return;
        }

        switch (key) {
            case "main_value":
                valuesFragment.changeMainValue();
                break;
            case "pm_25_norm":
                valuesFragment.changePM25Norm();
                break;
            case "pm_10_norm":
                valuesFragment.changePM10Norm();
                break;
        }
    }
}
