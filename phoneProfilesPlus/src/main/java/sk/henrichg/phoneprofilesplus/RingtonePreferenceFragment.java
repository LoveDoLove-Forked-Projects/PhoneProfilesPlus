package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class RingtonePreferenceFragment extends PreferenceDialogFragmentCompat {

    RingtonePreference preference;

    private RingtonePreferenceAdapter listAdapter;
    private LinearLayout linlaProgress;
    private ListView listView;

    private Context prefContext;

    RingtonePreferenceRefreshListViewAsyncTask asyncTask = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        prefContext = context;
        preference = (RingtonePreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_ringtone_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view)
    {
        super.onBindDialogView(view);

        linlaProgress = view.findViewById(R.id.ringtone_pref_dlg_linla_progress);

        listView = view.findViewById(R.id.ringtone_pref_dlg_listview);

        //noinspection DataFlowIssue
        listView.setOnItemClickListener((parent, item, position, id) -> {
            RingtonePreferenceViewHolder viewHolder = (RingtonePreferenceViewHolder) item.getTag();
            preference.setRingtone((String)listAdapter.getItem(position), false);
            viewHolder.radioBtn.setChecked(true);
            preference.playRingtone();
        });

        listAdapter = new RingtonePreferenceAdapter(this, prefContext, preference.toneList);
        listView.setAdapter(listAdapter);

        if (Permissions.grantRingtonePreferenceDialogPermissions(prefContext)) {
            //if (preference != null) {
                if (!preference.toneList.isEmpty())
                    hideProgress();
            //}

            final Handler handler = new Handler(prefContext.getMainLooper());
            final WeakReference<RingtonePreference> preferenceWeakRef = new WeakReference<>(preference);
            handler.postDelayed(() -> {
//                    PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=RingtonePreferenceFragment.onBindDialogView");
                //_preference.oldRingtoneUri = preference.ringtoneUri;
                RingtonePreference preference = preferenceWeakRef.get();
                if (preference != null)
                    preference.refreshListView();
            }, 200);
        }

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        }
        else {
            preference.resetSummary();
        }

        if ((asyncTask != null) && asyncTask.getStatus().equals(AsyncTask.Status.RUNNING)){
            asyncTask.cancel(true);
        }
        asyncTask = null;

        preference.stopPlayRingtone();

        preference.fragment = null;
    }

//    void showProgress() {
//        listView.setVisibility(View.GONE);
//        linlaProgress.setVisibility(View.VISIBLE);
//    }

    void hideProgress() {
        linlaProgress.setVisibility(View.GONE);
        //listView.setVisibility(View.GONE);
    }

    void updateListView(boolean alsoSelection) {
        linlaProgress.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);

        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();

            if (alsoSelection) {
                List<String> uris = new ArrayList<>(listAdapter.toneList.keySet());
                int position = uris.indexOf(preference.ringtoneUri);
                if (position == -1)
                    position = 0;
                listView.setSelection(position);
            }
        }
    }

    int getRingtonePosition() {
        List<String> uris = new ArrayList<>(listAdapter.toneList.keySet());
        return uris.indexOf(preference.ringtoneUri);
    }

    void refreshListView() {
        if (Permissions.checkRingtonePreference(prefContext)) {
            asyncTask = new RingtonePreferenceRefreshListViewAsyncTask(preference, prefContext);
            asyncTask.execute();
        }
    }

}
