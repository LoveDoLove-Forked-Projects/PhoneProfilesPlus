package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.text.Spannable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

class EditorProfileListViewHolder extends RecyclerView.ViewHolder
                    implements View.OnClickListener, View.OnLongClickListener

{

    final DragHandle dragHandle;
    //private RelativeLayout listItemRoot;
    private final ImageView profileIcon;
    private final TextView profileName;
    private ImageView profileIndicator;
    private final AppCompatImageButton profileItemEditMenu;
    private final AppCompatImageButton showInActivatorButton;

    private Profile profile;
    final EditorProfileListFragment editorFragment;

    private final Context context;

    EditorProfileListViewHolder(View itemView, EditorProfileListFragment editorFragment, Context context, int filterType) {
        super(itemView);

        this.context = context;
        this.editorFragment = editorFragment;

        if (filterType == EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR)
            dragHandle = itemView.findViewById(R.id.profile_list_drag_handle);
        else
            dragHandle = null;
        //if (filterType == EditorProfileListFragment.FILTER_TYPE_ALL)
        showInActivatorButton = itemView.findViewById(R.id.profile_list_item_show_in_activator);
        /*else
            showInActivatorButton = null;*/

        profileName = itemView.findViewById(R.id.profile_list_item_profile_name);
        profileIcon = itemView.findViewById(R.id.profile_list_item_profile_icon);
        profileItemEditMenu = itemView.findViewById(R.id.profile_list_item_edit_menu);
        if (ApplicationPreferences.applicationEditorPrefIndicator)
            profileIndicator = itemView.findViewById(R.id.profile_list_profile_pref_indicator);

        // don't delete this - it is workaround for set this LinearLayout non-clickable
        LinearLayout buttonsLayout = itemView.findViewById(R.id.profile_list_item_buttons_root);
        //noinspection DataFlowIssue
        buttonsLayout.setOnClickListener(v -> {});

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

    }

    void bindProfile(Profile profile) {

        this.profile = profile;

        //boolean isPermissionGranted = Permissions.checkProfilePermissions(context, profile).size() == 0;

        //boolean applicationEditorHeader = ApplicationPreferences.applicationEditorHeader(context);

        /*if (profile._checked && (!applicationEditorHeader))
        {
            profileName.setTypeface(null, Typeface.BOLD);
            profileName.setTextSize(16);
            profileName.setTextColor(GlobalGUIRoutines.getThemeAccentColor(editorFragment.getActivity()));
        }
        else*/
        /*if ((!isPermissionGranted) ||
            (Profile.isProfilePreferenceAllowed("-", profile, null, true, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) ||
            ((profile._volumeRingerMode != 0) && (!ActivateProfileHelper.canChangeZenMode(context, false))) ||
            (profile.isAccessibilityServiceEnabled(context) != 1)
           )*/
        if (ProfileStatic.isRedTextNotificationRequired(profile, false, context)){
            profileName.setTypeface(null, Typeface.BOLD);
            //profileName.setTextSize(15);
            profileName.setTextColor(ContextCompat.getColor(context, R.color.errorColor));
        }
        else {
            profileName.setTypeface(null, Typeface.BOLD);
            //profileName.setTextSize(15);
            //profileName.setTextColor(GlobalGUIRoutines.getThemeNormalTextColor(editorFragment.getActivity()));
            //noinspection ConstantConditions
            profileName.setTextColor(ContextCompat.getColor(editorFragment.getActivity(), R.color.activityNormalTextColor));
        }

        String indicators = "";
        //if (profile._showInActivator)
        //    indicators = "[A]";
        Spannable _profileName = DataWrapperStatic.getProfileNameWithManualIndicator(profile,
                                    false, indicators, true, true, false,
                                    false, editorFragment.activityDataWrapper);

        profileName.setText(_profileName);

        if (profile.getIsIconResourceID())
        {
            Bitmap bitmap = profile.increaseProfileIconBrightnessForActivity(editorFragment.getActivity(), profile._iconBitmap);
            if (bitmap != null)
                profileIcon.setImageBitmap(bitmap);
            else {
                if (profile._iconBitmap != null)
                    profileIcon.setImageBitmap(profile._iconBitmap);
                else {
                    //holder.profileIcon.setImageBitmap(null);
                    //int res = context.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                    //        context.PPApplication.PACKAGE_NAME);
                    int res = ProfileStatic.getIconResource(profile.getIconIdentifier());
                    profileIcon.setImageResource(res); // icon resource
                }
            }
        }
        else
        {
            //Bitmap bitmap = profile.increaseProfileIconBrightnessForActivity(editorFragment.getActivity(), profile._iconBitmap);
            //Bitmap bitmap = profile._iconBitmap;
            //if (bitmap != null)
            //    profileIcon.setImageBitmap(bitmap);
            //else
                profileIcon.setImageBitmap(profile._iconBitmap);
        }

        if (ApplicationPreferences.applicationEditorPrefIndicator)
        {
            //profilePrefIndicatorImageView.setImageBitmap(null);
            //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
            //profilePrefIndicatorImageView.setImageBitmap(bitmap);
            if (profileIndicator != null)
                profileIndicator.setImageBitmap(profile._preferencesIndicator);
        }

        TooltipCompat.setTooltipText(profileItemEditMenu, context.getString(R.string.tooltip_options_menu));
        profileItemEditMenu.setTag(profile);
        profileItemEditMenu.setOnClickListener(v -> editorFragment.showEditMenu(profileItemEditMenu));

        //if (showInActivatorButton != null) {
            if (profile._showInActivator)
                showInActivatorButton.setImageResource(R.drawable.ic_show_in_activator);
            else
                showInActivatorButton.setImageResource(R.drawable.ic_not_show_in_activator);
            TooltipCompat.setTooltipText(showInActivatorButton, context.getString(R.string.profile_preferences_showInActivator));
            showInActivatorButton.setTag(profile);
            showInActivatorButton.setOnClickListener(v -> {
                editorFragment.showShowInActivatorMenu(showInActivatorButton);
                /*final Profile profile = (Profile)v.getTag();
                if (profile != null) {
                    editorFragment.changeShowInActivator(profile);
                }*/
            });
        //}
    }

    @Override
    public void onClick(View v) {
        editorFragment.startProfilePreferencesActivity(profile, 0);
    }

    @Override
    public boolean onLongClick(View v) {
        editorFragment.activateProfile(profile);
        return true;
    }

}
