package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;

public class PPMultiSelectListPreferenceFragment extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private PPMultiSelectListPreference preference;

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
        preference = (PPMultiSelectListPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_pp_list_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        ListView listView = view.findViewById(R.id.pp_list_pref_dlg_listview);

        PPMultiSelectListPreferenceAdapter listAdapter = new PPMultiSelectListPreferenceAdapter(prefContext, preference);

        //noinspection DataFlowIssue
        listView.setOnItemClickListener((parent, item, position, id) -> {
            CheckBox chb = item.findViewById(R.id.pp_multiselect_list_pref_dlg_item_checkbox);

            String _valueFromPos = preference.entryValues[position].toString();

            // search for value from position in preference.value
            boolean _found = false;
            for (String value : preference.value) {
                if (value.equals(_valueFromPos)) {
                    _found = true;
                    break;
                }
            }
            if (_found) {
                // value form position exists in value
                preference.value.remove(_valueFromPos);
                _found = false;
            } else {
                // value form position not exists in value
                preference.value.add(_valueFromPos);
                _found = true;
            }
            //noinspection DataFlowIssue
            chb.setChecked(_found);
            //preference.persistValue();
        });

        listView.setAdapter(listAdapter);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
//        Log.e("PPMultiSelectListPreferenceFragment.onDialogClosed", "positiveResult="+positiveResult);
        if (positiveResult) {
            preference.persistValue();
        }
        else {
            preference.resetSummary();
        }

        preference.fragment = null;
    }

}
