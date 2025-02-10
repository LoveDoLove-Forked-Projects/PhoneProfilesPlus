package sk.henrichg.phoneprofilesplus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

class ApplicationsMultiSelectPreferenceAdapter extends RecyclerView.Adapter<ApplicationsMultiSelectDialogPreferenceViewHolder>
                                                implements FastScrollRecyclerView.SectionedAdapter
{
    private final ApplicationsMultiSelectDialogPreference preference;

    ApplicationsMultiSelectPreferenceAdapter(ApplicationsMultiSelectDialogPreference preference)
    {
        this.preference = preference;
    }

    @NonNull
    @Override
    public ApplicationsMultiSelectDialogPreferenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int resource;
        resource = R.layout.listitem_applications_multiselect_preference;

        View view = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
        return new ApplicationsMultiSelectDialogPreferenceViewHolder(view/*, preference.getContext()*/);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicationsMultiSelectDialogPreferenceViewHolder holder, int position) {
        // Application to display
        CApplication application = preference.applicationList.get(position);
        //System.out.println(String.valueOf(position));

        holder.bindApplication(application);
    }

    /** @noinspection unused*/
    @NonNull
    @Override
    public String getSectionName(int position) {
        CApplication application = preference.applicationList.get(position);
        if (application.checked)
            return "*";
        else {
            if (application.appLabel.isEmpty())
                return "?";
            else
                return application.appLabel.substring(0, 1);
        }
    }

    @Override
    public int getItemCount() {
        if (preference.applicationList == null)
            return 0;
        else
            return preference.applicationList.size();
    }

}
