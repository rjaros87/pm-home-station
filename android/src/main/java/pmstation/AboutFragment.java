/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */

package pmstation;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AboutFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = getContext();
        if (context == null) {
            return;
        }

        view.findViewById(R.id.author_rg)
            .setOnClickListener(view1 -> Utils.launchWebIntent(context, "https://github.com/sanchin"));
        view.findViewById(R.id.author_rj)
            .setOnClickListener(view1 -> Utils.launchWebIntent(context, "https://github.com/rjaros87"));
        view.findViewById(R.id.author_ps)
            .setOnClickListener(view1 -> Utils.launchWebIntent(context, "https://github.com/pskowronek"));
        view.findViewById(R.id.project_desc).setOnClickListener(
                view1 -> Utils.launchWebIntent(context, "https://github.com/rjaros87/pm-home-station"));
        view.findViewById(R.id.policy_desc).setOnClickListener(
                view1 -> Utils.launchWebIntent(context, "https://github.com/rjaros87/pm-home-station/blob/master/PRIVACY.md"));
        view.findViewById(R.id.lgpl3).setOnClickListener(
                view1 -> Utils.launchWebIntent(context, "https://www.gnu.org/licenses/gpl-3.0.en.html"));
        view.findViewById(R.id.third_party).setOnClickListener(view1 -> showAttribution());
    }

    private void showAttribution() {
        final LicensesDialogFragment fragment = new LicensesDialogFragment();
        fragment.show(getFragmentManager(), null);
    }

}
