package sk.henrichg.phoneprofilesplus;

import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;
import androidx.recyclerview.widget.RecyclerView;

class RunApplicationEditorDialogViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final ImageView imageViewIcon;
    private final TextView textViewAppName;
    private final RadioButton radioBtn;
    private final AppCompatImageButton imageViewMenu;

    private final RunApplicationEditorDialog dialog;

    private Application application;

    RunApplicationEditorDialogViewHolder(View itemView, /*Context context,*/ RunApplicationEditorDialog d)
    {
        super(itemView);

        this.dialog = d;

        if (dialog.selectedFilter != 2)
            imageViewIcon = itemView.findViewById(R.id.run_applications_editor_dialog_item_icon);
        else
            imageViewIcon = null;
        textViewAppName = itemView.findViewById(R.id.run_applications_editor_dialog_item_app_name);
        radioBtn = itemView.findViewById(R.id.run_applications_editor_dialog_item_radiobutton);
        if (dialog.selectedFilter == 2)
            imageViewMenu = itemView.findViewById(R.id.run_applications_editor_dlg_item_edit_menu);
        else
            imageViewMenu = null;

        //noinspection DataFlowIssue
        radioBtn.setOnClickListener(v -> {
            RadioButton rb = (RadioButton) v;
            rb.setChecked(true);
            dialog.doOnItemSelected((Integer)rb.getTag());
        });

        itemView.setOnClickListener(this);
    }

    void bindApplication(Application application, int position) {
        this.application = application;

        // Display Application data
        if (dialog.selectedFilter != 2) {
            if (PPApplicationStatic.getApplicationsCache() != null)
                imageViewIcon.setImageBitmap(PPApplicationStatic.getApplicationsCache().getApplicationIcon(application/*, false*/));
        }
        textViewAppName.setText(application.appLabel);

        radioBtn.setChecked(dialog.selectedPosition == position);
        radioBtn.setTag(position);

        if (imageViewMenu != null) {
            TooltipCompat.setTooltipText(imageViewMenu, dialog.activity.getString(R.string.tooltip_options_menu));
            imageViewMenu.setTag(position);
            imageViewMenu.setOnClickListener(v -> dialog.showEditMenu(imageViewMenu));
        }
    }

    @Override
    public void onClick(View v) {
        int position = dialog.applicationList.indexOf(application);
        if (position != -1) {
            dialog.doOnItemSelected(position);
            radioBtn.setChecked(true);
        }
    }

}
