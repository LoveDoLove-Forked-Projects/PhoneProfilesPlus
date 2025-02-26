package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;

public class OpaquenessLightingPreferenceFragment extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    OpaquenessLightingPreference preference;

    @Override
    protected void onPrepareDialogBuilder(@NonNull AlertDialog.Builder builder) {
        GlobalGUIRoutines.setCustomDialogTitle(preference.getContext(), builder, false,
                preference.getDialogTitle(), null);
    }

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        prefContext = context;
        preference = (OpaquenessLightingPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_opaqueness_lighting_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view)
    {
        super.onBindDialogView(view);

        ListView listView = view.findViewById(R.id.opaqueness_lighting_pref_dlg_listview);

        //noinspection DataFlowIssue
        listView.setOnItemClickListener((parent, item, position, id) -> doOnItemSelected(position));

        OpaquenessLightingPreferenceAdapter opaquenessLightingPreferenceAdapter = new OpaquenessLightingPreferenceAdapter(preference.fragment, prefContext, preference.value);
        listView.setAdapter(opaquenessLightingPreferenceAdapter);
        int position = preference.getPosition(preference.value);
        if (position != -1)
            listView.setSelection(position);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        preference.fragment = null;
    }

    void doOnItemSelected(int position)
    {
        if (preference.showLighting)
            preference.setValue(String.valueOf(preference.lightingValues[position]));
        else
            preference.setValue(String.valueOf(preference.opaquenessValues[position]));
        dismiss();
    }

}
