package sanchin.pmstation;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import pl.radoslawjaros.plantower.ParticulateMatterSample;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChartFragment extends Fragment {
    List<Entry> entries = new ArrayList<>();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private String pm1Label;
    private String pm25Label;
    private String pm10Label;
    private LineChart chart;
    private boolean ready;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chart = view.findViewById(R.id.chart);
        chart.getDescription();
        ready = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MainActivity) context).chartFragment = this;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        pm1Label = getString(R.string.pm1);
        pm25Label = getString(R.string.pm25);
        pm10Label = getString(R.string.pm10);
        final List<ParticulateMatterSample> values = ((MainActivity) getActivity()).getValues();

        IAxisValueFormatter formatter =
                (value, axis) -> dateFormat.format(values.get((int) value).getDate());

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(formatter);
        xAxis.setLabelRotationAngle(-45);

        YAxis yAxisL = chart.getAxisLeft();
        YAxis yAxisR = chart.getAxisRight();

        yAxisL.setAxisMinimum(0);
        yAxisR.setAxisMinimum(0);
        chart.getDescription().setEnabled(false);

        LineData data = new LineData();

        // add empty data
        chart.setData(data);

        for (ParticulateMatterSample value : values) {
            addEntry(pm1Label, value.getPm1_0());
            addEntry(pm25Label, value.getPm2_5());
            addEntry(pm10Label, value.getPm10());
        }
        chart.invalidate();
        ready = true;
    }

    private void addEntry(String label, int value) {
        LineData data = chart.getData();
        if (data != null) {
            int index;
            if (label.equals(pm1Label)) {
                index = 0;
            } else if (label.equals(pm25Label)) {
                index = 1;
            } else {
                index = 2;
            }
            ILineDataSet set = data.getDataSetByIndex(index);

            if (set == null) {
                set = createSet(label);
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), value), index);
        }
    }

    private LineDataSet createSet(String label) {
        LineDataSet set = new LineDataSet(null, label);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        int color;
        if (label.equals(pm1Label)) {
            color = Color.BLUE;
        } else if (label.equals(pm25Label)) {
            color = Color.RED;
        } else {
            color = Color.BLACK;
        }
        set.setColor(color);
        set.setLineWidth(2f);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        return set;
    }

    void updateValues(ParticulateMatterSample sample) {
        if (!ready) {
            return;
        }
        LineData data = chart.getData();

        addEntry(pm1Label, sample.getPm1_0());
        addEntry(pm25Label, sample.getPm2_5());
        addEntry(pm10Label, sample.getPm10());

        data.notifyDataChanged();

        // let the chart know it's data has changed
        chart.notifyDataSetChanged();

        // limit the number of visible entries
        chart.setVisibleXRangeMaximum(120);

        // move to the latest entry
        chart.moveViewToX(data.getEntryCount());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_status, menu);
        boolean connected = ((MainActivity) getActivity()).isConnected();

        MenuItem item = menu.getItem(0);
        MainActivity.tintMenuItem(item);
        item = menu.getItem(1);
        MainActivity.tintMenuItem(item);
        item = menu.getItem(2);
        item.setVisible(false);
        menu.getItem(0).setVisible(connected);
        menu.getItem(1).setVisible(!connected);
    }
}
