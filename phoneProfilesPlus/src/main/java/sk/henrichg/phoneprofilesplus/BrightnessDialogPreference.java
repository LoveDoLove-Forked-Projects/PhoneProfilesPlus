package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class BrightnessDialogPreference extends DialogPreference {

    BrightnessDialogPreferenceFragment fragment;

    private final Context _context;

    // Custom xml attributes.
    int noChange;
    int automatic;
    //int sharedProfile;
    //int disableSharedProfile;
    int changeLevel;
    final int forBrightnessSensor;

    //private final int defaultValue = 50;
    final int maximumValue = 100;
    //final int minimumValue = 0;
    //final int stepSize = 1;

    private String sValue = "";
    private String defaultValue;
    private boolean savedInstanceState;

    int value = 0;

    //final boolean adaptiveAllowed;
    //final Profile _sharedProfile;

    //private float savedLayoutParamsBrightness;

    public BrightnessDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        //noinspection resource
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.PPBrightnessDialogPreference);

        forBrightnessSensor = typedArray.getInteger(
                R.styleable.PPBrightnessDialogPreference_bForBrightnessSensor, 0);

        noChange = typedArray.getInteger(
                R.styleable.PPBrightnessDialogPreference_bNoChange, (forBrightnessSensor == 0) ? 1 : 0);
        automatic = typedArray.getInteger(
                R.styleable.PPBrightnessDialogPreference_bAutomatic, (forBrightnessSensor == 0) ? 1 : 0);
        /*sharedProfile = typedArray.getInteger(
                R.styleable.BrightnessDialogPreference_bSharedProfile, 0);
        disableSharedProfile = typedArray.getInteger(
                R.styleable.BrightnessDialogPreference_bDisableSharedProfile, 0);*/
        changeLevel = typedArray.getInteger(
                R.styleable.PPBrightnessDialogPreference_bChangeLevel, 1);

        typedArray.recycle();

        //_sharedProfile = Profile.getProfileFromSharedPreferences(_context, PPApplication.SHARED_PROFILE_PREFS_NAME);

        //SharedPreferences sharedPreferences = getSharedPreferences();
        //adaptiveAllowed =  (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS, null, sharedPreferences, true, _context.getApplicationContext()).allowed
        //                == PreferenceAllowed.PREFERENCE_ALLOWED);

        /*if (Build.VERSION.SDK_INT >= 28) {
            defaultValue = 24;
            maximumValue = 255;
        }*/

        /*savedBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                Profile.convertPercentsToBrightnessManualValue(50, context));
        savedBrightnessMode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        savedAdaptiveBrightness = Settings.System.getFloat(context.getContentResolver(), ActivateProfileHelper.ADAPTIVE_BRIGHTNESS_SETTING_NAME, 0f);*/
        //Window win = ((Activity)context).getWindow();
        //WindowManager.LayoutParams layoutParams = win.getAttributes();
        //savedLayoutParamsBrightness = layoutParams.screenBrightness;
    }

    void enableViews() {
        if (fragment != null)
            fragment.enableViews();
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        // Get the persistent value and correct it for the minimum value.
        sValue = getPersistedString((String) defaultValue);
        this.defaultValue = (String)defaultValue;

        getValueBDP();
        setSummaryBDP();
    }

    private void getValueBDP()
    {
        String[] splits = sValue.split(StringConstants.STR_SPLIT_REGEX);
        try {
            value = Integer.parseInt(splits[0]);
            /*if (value == Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET) {
                // brightness is not set, change it to default adaptive brightness value
                int halfValue = maximumValue / 2;
                value = Math.round(SettingsContentObserver.savedAdaptiveBrightness * halfValue + halfValue);
            }*/
            if ((value < 0) || (value > maximumValue)) {
                value = 50;
            }
        } catch (Exception e) {
            value = 50;
        }
        //value = value - minimumValue;
        if (forBrightnessSensor == 0) {
            try {
                noChange = Integer.parseInt(splits[1]);
            } catch (Exception e) {
                noChange = 1;
            }
            try {
                automatic = Integer.parseInt(splits[2]);
            } catch (Exception e) {
                automatic = 1;
            }
            /*try {
                sharedProfile = Integer.parseInt(splits[3]);
            } catch (Exception e) {
                sharedProfile = 0;
            }*/
            try {
                changeLevel = Integer.parseInt(splits[4]);
            } catch (Exception e) {
                changeLevel = 1;
            }
        } else {
            noChange = 0;
            automatic = 0;
            changeLevel = 1;
        }

        //value = getPersistedInt(minimumValue) - minimumValue;

        // You're never know...
        if (value < 0) {
            value = 0;
        }
    }

    private void setSummaryBDP()
    {
        String prefVolumeDataSummary;
        if (forBrightnessSensor == 0) {
            if (noChange == 1)
                prefVolumeDataSummary = _context.getString(R.string.preference_profile_no_change);
            /*else
            if (sharedProfile == 1)
            prefVolumeDataSummary = _context.getString(R.string.preference_profile_default_profile);*/
            else {
                if (automatic == 1) {
                    prefVolumeDataSummary = _context.getString(R.string.preference_profile_adaptiveBrightness);
                } else
                    prefVolumeDataSummary = _context.getString(R.string.preference_profile_manual_brightness);

                if ((changeLevel == 1) /*&& (adaptiveAllowed || automatic == 0)*/) {
                    String _value = value + " / " + maximumValue;
                    prefVolumeDataSummary = prefVolumeDataSummary + "; " + _value;
                }
            }
        } else {
            prefVolumeDataSummary = value + " / " + maximumValue;
        }
        setSummary(prefVolumeDataSummary);
    }

    String getSValue() {
        //int _value = value + minimumValue;
        return value
                + "|" + noChange
                + "|" + automatic
                + "|" + "0"
                + "|" + changeLevel;
    }

    void persistValue() {
        if (shouldPersist()) {
            persistString(getSValue());
            setSummaryBDP();
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            sValue = getPersistedString(defaultValue);
            getValueBDP();
            setSummaryBDP();
        }
        savedInstanceState = false;
    }

    static boolean changeEnabled(String value) {
        String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
        if (splits.length > 1) {
            try {
                return Integer.parseInt(splits[1]) == 0;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return false;
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            // save is not needed, is already saved persistent
            return superState;
        }*/

        final BrightnessDialogPreference.SavedState myState = new BrightnessDialogPreference.SavedState(superState);
        myState.sValue = sValue;
        myState.defaultValue = defaultValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if ((state == null) || (!state.getClass().equals(SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            getValueBDP();
            setSummaryBDP();
            return;
        }

        // restore instance state
        BrightnessDialogPreference.SavedState myState = (BrightnessDialogPreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        sValue = myState.sValue;
        defaultValue = myState.defaultValue;

        getValueBDP();
        setSummaryBDP();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String sValue;
        String defaultValue;

        SavedState(Parcel source)
        {
            super(source);

            sValue = source.readString();
            defaultValue = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeString(sValue);
            dest.writeString(defaultValue);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<BrightnessDialogPreference.SavedState> CREATOR =
                new Creator<>() {
                    public BrightnessDialogPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new BrightnessDialogPreference.SavedState(in);
                    }
                    public BrightnessDialogPreference.SavedState[] newArray(int size)
                    {
                        return new BrightnessDialogPreference.SavedState[size];
                    }

                };

    }

}
