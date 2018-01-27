/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */

package pmstation;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import pmstation.core.plantower.IPlanTowerObserver;
import pmstation.core.plantower.ParticulateMatterSample;
import pmstation.plantower.Settings;

public class ValuesFragment extends Fragment implements IPlanTowerObserver {
    private static final String TAG = "ValuesFragment";
    SharedPreferences.OnSharedPreferenceChangeListener spChanged = (sharedPreferences, key) -> {

    };
    private CardView pm1Card;
    private CardView pm25Card;
    private CardView pm10Card;
    private TextView pm1;
    private TextView pm1Secondary;
    private TextView pm25;
    private TextView pm25Secondary;
    private TextView pm25Unit;
    private TextView pm25SecondaryUnit;
    private TextView pm10;
    private TextView pm10Secondary;
    private TextView pm10Unit;
    private TextView pm10SecondaryUnit;

    private TextView time;
    private ParticulateMatterSample currentValue;
    private SharedPreferences preferences;

    public ValuesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_values, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pm1Card = view.findViewById(R.id.pm1_card);
        pm25Card = view.findViewById(R.id.pm25_card);
        pm10Card = view.findViewById(R.id.pm10_card);
        pm1 = pm1Card.findViewById(R.id.pm_main_value);
        pm1Secondary = pm1Card.findViewById(R.id.pm_secondary_value);
        pm10SecondaryUnit = pm1Card.findViewById(R.id.pm_secondary_unit);
        pm1Secondary.setVisibility(View.GONE);
        pm10SecondaryUnit.setVisibility(View.GONE);
        pm25 = pm25Card.findViewById(R.id.pm_main_value);
        pm25Unit = pm25Card.findViewById(R.id.pm_main_unit);
        pm25SecondaryUnit = pm25Card.findViewById(R.id.pm_secondary_unit);
        pm25Secondary = pm25Card.findViewById(R.id.pm_secondary_value);
        pm10 = pm10Card.findViewById(R.id.pm_main_value);
        pm10Secondary = pm10Card.findViewById(R.id.pm_secondary_value);
        pm10Unit = pm10Card.findViewById(R.id.pm_main_unit);
        pm10SecondaryUnit = pm10Card.findViewById(R.id.pm_secondary_unit);
        ((TextView) pm1Card.findViewById(R.id.pm_label)).setText(R.string.pm1);
        ((TextView) pm25Card.findViewById(R.id.pm_label)).setText(R.string.pm25);
        ((TextView) pm10Card.findViewById(R.id.pm_label)).setText(R.string.pm10);

        time = view.findViewById(R.id.time);

        if (savedInstanceState != null) {
            currentValue = (ParticulateMatterSample) savedInstanceState.getSerializable("value");
        } else {
            List<ParticulateMatterSample> values = ((MainActivity) getActivity()).getValues();
            if (!values.isEmpty()) {
                currentValue = values.get(values.size() - 1);
            }
        }

        update(currentValue);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivity activity = (MainActivity) getActivity();
        activity.getSensor().addValueObserver(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        changeMainValue();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MainActivity activity = (MainActivity) getActivity();
        activity.getSensor().removeValueObserver(this);
    }

    @Override
    public void update(final ParticulateMatterSample sample) {
        FragmentActivity activity = getActivity();
        currentValue = sample;
        if (activity == null || sample == null) {
            return;
        }
        activity.runOnUiThread(() -> {
            pm1.setText(String.format(Locale.getDefault(), "%d", sample.getPm1_0()));
            pm25.setText(String.format(Locale.getDefault(), "%d", sample.getPm2_5()));
            pm25Secondary.setText(String.format(Locale.getDefault(), "%d", Math.round(sample.getPm2_5() / 25f * 100)));
            AQIColor pm25Color = AQIColor.fromPM25Level(sample.getPm2_5());
            pm1Card.setCardBackgroundColor(
                    ColorUtils.setAlphaComponent(pm25Color.getColor(), 136));
            pm25Card.setCardBackgroundColor(
                    ColorUtils.setAlphaComponent(pm25Color.getColor(), 136));
            pm10.setText(String.format(Locale.getDefault(), "%d", sample.getPm10()));
            pm10Secondary.setText(String.format(Locale.getDefault(), "%d", Math.round(sample.getPm10() / 25f * 100)));
            pm10Card.setCardBackgroundColor(
                    ColorUtils.setAlphaComponent(AQIColor.fromPM10Level(sample.getPm10()).getColor(), 136));
            time.setText(Settings.dateFormat.format(sample.getDate()));
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable("value", currentValue);
        super.onSaveInstanceState(outState);
    }

    public void changeMainValue() {
        int main_value = Integer.parseInt(preferences.getString("main_value", "1"));
        TextView tmpView = pm25;
        pm25 = pm25Secondary;
        pm25Secondary = tmpView;
        tmpView = pm10;
        pm10 = pm10Secondary;
        pm10Secondary = tmpView;
        switch (main_value) {
            case 2:
                pm25Unit.setText(R.string.percent);
                pm25SecondaryUnit.setText(R.string.unit);
                pm10Unit.setText(R.string.percent);
                pm10SecondaryUnit.setText(R.string.unit);
                break;
            case 1:
                pm25Unit.setText(R.string.unit);
                pm25SecondaryUnit.setText(R.string.percent);
                pm10Unit.setText(R.string.unit);
                pm10SecondaryUnit.setText(R.string.percent);
            default:
                break;
        }
    }
}
