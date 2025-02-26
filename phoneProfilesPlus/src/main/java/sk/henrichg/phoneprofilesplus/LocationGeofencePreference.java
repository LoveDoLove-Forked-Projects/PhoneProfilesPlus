package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;

public class LocationGeofencePreference extends DialogPreference {

    LocationGeofencePreferenceFragment fragment;

    private final Context context;

    final int onlyEdit;

    private String defaultValue;
    private boolean savedInstanceState;

    //private LinearLayout progressLinearLayout;
    //private RelativeLayout dataRelativeLayout;
    //private TextView geofenceName;

    //final DataWrapper dataWrapper;

    static final String EXTRA_GEOFENCE_ID = "geofence_id";
    static final int RESULT_GEOFENCE_EDITOR = 2100;

    public LocationGeofencePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        //noinspection resource
        TypedArray locationGeofenceType = context.obtainStyledAttributes(attrs,
                R.styleable.PPLocationGeofencePreference, 0, 0);

        onlyEdit = locationGeofenceType.getInt(R.styleable.PPLocationGeofencePreference_onlyEdit, 0);

        locationGeofenceType.recycle();

        this.context = context;

        //dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);

        if (onlyEdit != 0)
            setNegativeButtonText(null);
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        if (onlyEdit == 0) {
            String value = getPersistedString((String) defaultValue);
            this.defaultValue = (String)defaultValue;

            // check by value
            DatabaseHandler.getInstance(context.getApplicationContext()).checkGeofence(value, 1, true);
            setSummary();
        }
    }

    /*
    String getPersistedGeofence() {
        return getPersistedString("");
    }
    */

    void persistGeofence(boolean reset) {
        if (onlyEdit == 0) {
            if (shouldPersist()) {
                // get value for checked - checked are only with KEY_G_CHECKED = 1
                String value = DatabaseHandler.getInstance(context.getApplicationContext()).getCheckedGeofences();
                if (callChangeListener(value)) {
                    if (reset)
                        persistString("");
                    persistString(value);
                }
            }
            setSummary();
        }
    }

    void resetSummary() {
        if ((onlyEdit == 0) && (!savedInstanceState)) {
            String value = getPersistedString(defaultValue);
            // check by value
            DatabaseHandler.getInstance(context.getApplicationContext()).checkGeofence(value, 1, true);
            setSummary();
        }
        savedInstanceState = false;
    }

    /*
    public void updateGUIWithGeofence(long geofenceId)
    {
        String name = "";
        if (onlyEdit == 0) {
            name = dataWrapper.getDatabaseHandler().getGeofenceName(geofenceId);
            if (name.isEmpty())
                name = "[" + context.getString(R.string.event_preferences_locations_location_not_selected) + "]";
        }

        this.geofenceName.setText(name);
    }
    */

    void setLocationEnableStatus() {
        if (fragment != null)
            fragment.setLocationEnableStatus();
    }

    void refreshListView()
    {
        if (fragment != null)
            fragment.refreshListView();
    }

    void setGeofenceFromEditor(/*long geofenceId*/) {
        persistGeofence(true);
        refreshListView();
        //updateGUIWithGeofence(geofenceId);
    }

    private void setSummary() {
        if (onlyEdit == 0) {
            if (!GlobalUtils.isLocationEnabled(context.getApplicationContext())) {
                setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                        StringConstants.STR_COLON_WITH_SPACE + context.getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings));
            }
            /*else
            if (!ApplicationPreferences.applicationEventLocationEnableScanning(context.getApplicationContext())) {
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+context.getString(R.string.preference_not_allowed_reason_not_enabled_scanning));
            }*/
            else {
                String value = DatabaseHandler.getInstance(context.getApplicationContext()).getCheckedGeofences();
                String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
                for (String _geofence : splits) {
                    if (_geofence.isEmpty()) {
                        setSummary(R.string.applications_multiselect_summary_text_not_selected);
                    } else if (splits.length == 1) {
                        setSummary(EventPreferencesLocation.getGeofenceName(Long.parseLong(_geofence), context));
                    } else {
                        String selectedLocations = context.getString(R.string.applications_multiselect_summary_text_selected);
                        selectedLocations = selectedLocations + " " + splits.length;
                        setSummary(selectedLocations);
                        break;
                    }
                }
            }
            //GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, true, false, true);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final LocationGeofencePreference.SavedState myState = new LocationGeofencePreference.SavedState(superState);
        if (onlyEdit == 0) {
            myState.value = DatabaseHandler.getInstance(context.getApplicationContext()).getCheckedGeofences();
            myState.defaultValue = defaultValue;
        }
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if ((state == null) || (!state.getClass().equals(LocationGeofencePreference.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummary();
            return;
        }

        // restore instance state
        LocationGeofencePreference.SavedState myState = (LocationGeofencePreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        if (onlyEdit == 0) {
            String value = myState.value;
            defaultValue = myState.defaultValue;

            // check by value
            DatabaseHandler.getInstance(context.getApplicationContext()).checkGeofence(value, 1, true);
            refreshListView();
            setSummary();
        }
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;
        String defaultValue;

        SavedState(Parcel source)
        {
            super(source);

            value = source.readString();
            defaultValue = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save profileId
            dest.writeString(value);
            dest.writeString(defaultValue);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<LocationGeofencePreference.SavedState> CREATOR =
                new Creator<>() {
                    public LocationGeofencePreference.SavedState createFromParcel(Parcel in)
                    {
                        return new LocationGeofencePreference.SavedState(in);
                    }
                    public LocationGeofencePreference.SavedState[] newArray(int size)
                    {
                        return new LocationGeofencePreference.SavedState[size];
                    }

                };

    }

}