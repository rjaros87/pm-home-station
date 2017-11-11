package sanchin.pmstation;


import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import pl.radoslawjaros.plantower.ParticulateMatterSample;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ValuesFragment extends Fragment {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy HH:mm:ss", Locale.getDefault());
    private CardView pm1Card;
    private CardView pm25Card;
    private CardView pm10Card;
    private TextView pm1;
    private TextView pm25;
    private TextView pm10;
    private TextView time;
    private TextView status;

    private long lastClickTime = 0;

    public ValuesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
        status = view.findViewById(R.id.status);

        view.setOnClickListener(view1 -> {
            // Preventing multiple clicks, using threshold of 1 second
            if (SystemClock.elapsedRealtime() - lastClickTime < 500) {
                return;
            }
            lastClickTime = SystemClock.elapsedRealtime();
            ChartFragment chartFragment = new ChartFragment();
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, chartFragment, "chartFragment").addToBackStack(null)
                               .commit();
        });
    }

    void updateValues(final ParticulateMatterSample sample) {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(() -> {
            pm1.setText(String.format(Locale.getDefault(), "%d", sample.getPm1_0()));
            pm25.setText(String.format(Locale.getDefault(), "%d", sample.getPm2_5()));
            pm25Card.setCardBackgroundColor(
                    ColorUtils.setAlphaComponent(AQIColor.fromPM25Level(sample.getPm2_5()).getColor(), 136));
            pm10.setText(String.format(Locale.getDefault(), "%d", sample.getPm10()));
            pm10Card.setCardBackgroundColor(
                    ColorUtils.setAlphaComponent(AQIColor.fromPM10Level(sample.getPm10()).getColor(), 136));
            time.setText(dateFormat.format(sample.getDate()));
        });
    }

    void setStatus(@StringRes int resId) {
        status.setText(resId);
    }
}
