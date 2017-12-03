package pmstation;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Sanchin on 03.12.2017.
 */

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            view.setBackgroundColor(getResources().getColor(android.R.color.background_light));
        }
        Preference attributionPref = findPreference("attribution");
        attributionPref.setOnPreferenceClickListener(preference -> {
            showAttribution();
            return true;
        });
        return view;
    }

    private void showAttribution() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        final LicensesDialogFragment fragment = new LicensesDialogFragment();
        fragment.show(activity.getSupportFragmentManager(), null);
    }
}
