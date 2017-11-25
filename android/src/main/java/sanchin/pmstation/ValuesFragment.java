package sanchin.pmstation;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import pmstation.plantower.ParticulateMatterSample;

public class ValuesFragment extends Fragment implements ValueObserver {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy HH:mm:ss", Locale.getDefault());
    private static final String TAG = "ValuesFragment";
    private CardView pm1Card;
    private CardView pm25Card;
    private CardView pm10Card;
    private TextView pm1;
    private TextView pm25;
    private TextView pm10;
    private TextView time;
    private ImageView smog;

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
        pm1 = pm1Card.findViewById(R.id.pm_value);
        pm25 = pm25Card.findViewById(R.id.pm_value);
        pm10 = pm10Card.findViewById(R.id.pm_value);
        ((TextView) pm1Card.findViewById(R.id.pm_label)).setText(R.string.pm1);
        ((TextView) pm25Card.findViewById(R.id.pm_label)).setText(R.string.pm25);
        ((TextView) pm10Card.findViewById(R.id.pm_label)).setText(R.string.pm10);

        time = view.findViewById(R.id.time);
        smog = view.findViewById(R.id.smog);
        smog.setAlpha(0f);

        List<ParticulateMatterSample> values = ((MainActivity) getActivity()).getValues();
        if (!values.isEmpty()) {
            ParticulateMatterSample sample = values.get(values.size() - 1);
            onNewValue(sample);
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MainActivity activity = (MainActivity) getActivity();
        activity.addValueObserver(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MainActivity activity = (MainActivity) getActivity();
        activity.removeValueObserver(this);
    }

    @Override
    public void onNewValue(final ParticulateMatterSample sample) {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(() -> {
            pm1.setText(String.format(Locale.getDefault(), "%d", sample.getPm1_0()));
            pm25.setText(String.format(Locale.getDefault(), "%d", sample.getPm2_5()));
            AQIColor pm25Color = AQIColor.fromPM25Level(sample.getPm2_5());
            pm1Card.setCardBackgroundColor(
                    ColorUtils.setAlphaComponent(pm25Color.getColor(), 136));
            pm25Card.setCardBackgroundColor(
                    ColorUtils.setAlphaComponent(pm25Color.getColor(), 136));
            pm10.setText(String.format(Locale.getDefault(), "%d", sample.getPm10()));
            pm10Card.setCardBackgroundColor(
                    ColorUtils.setAlphaComponent(AQIColor.fromPM10Level(sample.getPm10()).getColor(), 136));
            smog.animate().alpha(pm25Color.getAlpha());
            time.setText(dateFormat.format(sample.getDate()));
        });
    }
}
