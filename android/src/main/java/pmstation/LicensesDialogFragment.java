/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */

package pmstation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LicensesDialogFragment extends DialogFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_licenses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.plug_icons_notice_url_1)
            .setOnClickListener(view1 -> Utils.launchWebIntent(getContext(), ((TextView) view1).getText().toString()));
        view.findViewById(R.id.plug_icons_notice_url_2)
            .setOnClickListener(view1 -> Utils.launchWebIntent(getContext(), ((TextView) view1).getText().toString()));
        view.findViewById(R.id.background_notice_url)
            .setOnClickListener(view1 -> Utils.launchWebIntent(getContext(), ((TextView) view1).getText().toString()));
    }
}
