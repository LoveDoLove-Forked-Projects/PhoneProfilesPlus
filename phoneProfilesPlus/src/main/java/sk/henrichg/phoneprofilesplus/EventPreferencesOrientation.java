package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** @noinspection ExtractMethodRecommender*/
class EventPreferencesOrientation extends EventPreferences {

    String _display;
    String _sides;
    int _distance;
    boolean _checkLight;
    String _lightMin;
    String _lightMax;
    String _ignoredApplications;

    static final String PREF_EVENT_ORIENTATION_ENABLED = "eventOrientationEnabled";
    private static final String PREF_EVENT_ORIENTATION_DISPLAY = "eventOrientationDisplay";
    private static final String PREF_EVENT_ORIENTATION_SIDES = "eventOrientationSides";
    private static final String PREF_EVENT_ORIENTATION_DISTANCE = "eventOrientationDistance";
    private static final String PREF_EVENT_ORIENTATION_CHECK_LIGHT = "eventOrientationCheckLight";
    static final String PREF_EVENT_ORIENTATION_LIGHT_CURRENT_VALUE = "eventOrientationCurrentLightValue";
    private static final String PREF_EVENT_ORIENTATION_LIGHT_MIN = "eventOrientationLightMin";
    private static final String PREF_EVENT_ORIENTATION_LIGHT_MAX = "eventOrientationLightMax";
    static final String PREF_EVENT_ORIENTATION_EXTENDER = "eventOrientationExtender";
    //static final String PREF_EVENT_ORIENTATION_INSTALL_EXTENDER = "eventOrientationInstallExtender";
    //static final String PREF_EVENT_ORIENTATION_ACCESSIBILITY_SETTINGS = "eventOrientationAccessibilitySettings";
    private static final String PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS = "eventOrientationIgnoredApplications";
    private static final String PREF_EVENT_ORIENTATION_APP_SETTINGS = "eventEnableOrientationScanningAppSettings";
    //static final String PREF_EVENT_ORIENTATION_LAUNCH_EXTENDER = "eventOrientationLaunchExtender";

    static final String PREF_EVENT_ORIENTATION_CATEGORY = "eventOrientationCategoryRoot";


    EventPreferencesOrientation(Event event,
                                       boolean enabled,
                                       String display,
                                       String sides,
                                       int distance,
                                       boolean _checkLight,
                                       String lightMin,
                                       String lightMax,
                                       String ignoredApplications)
    {
        super(event, enabled);

        this._display = display;
        this._sides = sides;
        this._distance = distance;
        this._checkLight = _checkLight;
        this._lightMin = lightMin;
        this._lightMax = lightMax;
        this._ignoredApplications = ignoredApplications;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesOrientation._enabled;
        this._display = fromEvent._eventPreferencesOrientation._display;
        this._sides = fromEvent._eventPreferencesOrientation._sides;
        this._distance = fromEvent._eventPreferencesOrientation._distance;
        this._checkLight = fromEvent._eventPreferencesOrientation._checkLight;
        this._lightMin = fromEvent._eventPreferencesOrientation._lightMin;
        this._lightMax = fromEvent._eventPreferencesOrientation._lightMax;
        this._ignoredApplications = fromEvent._eventPreferencesOrientation._ignoredApplications;
        this.setSensorPassed(fromEvent._eventPreferencesOrientation.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_ORIENTATION_ENABLED, _enabled);

        String[] splits = this._display.split(StringConstants.STR_SPLIT_REGEX);
        Set<String> set = new HashSet<>(Arrays.asList(splits));
        editor.putStringSet(PREF_EVENT_ORIENTATION_DISPLAY, set);

        splits = this._sides.split(StringConstants.STR_SPLIT_REGEX);
        set = new HashSet<>(Arrays.asList(splits));
        editor.putStringSet(PREF_EVENT_ORIENTATION_SIDES, set);

        editor.putString(PREF_EVENT_ORIENTATION_DISTANCE, String.valueOf(this._distance));

        editor.putBoolean(PREF_EVENT_ORIENTATION_CHECK_LIGHT, this._checkLight);
        editor.putString(PREF_EVENT_ORIENTATION_LIGHT_MIN, this._lightMin);
        editor.putString(PREF_EVENT_ORIENTATION_LIGHT_MAX, this._lightMax);

        editor.putString(PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS, this._ignoredApplications);

        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_ORIENTATION_ENABLED, false);

        Set<String> set = preferences.getStringSet(PREF_EVENT_ORIENTATION_DISPLAY, null);
        StringBuilder values = new StringBuilder();
        if (set != null) {
            for (String s : set) {
                if (values.length() > 0)
                    values.append("|");
                values.append(s);
            }
        }
        this._display = values.toString();

        set = preferences.getStringSet(PREF_EVENT_ORIENTATION_SIDES, null);
        values = new StringBuilder();
        if (set != null) {
            for (String s : set) {
                if (values.length() > 0)
                    values.append("|");
                values.append(s);
            }
        }
        this._sides = values.toString();

        this._distance = Integer.parseInt(preferences.getString(PREF_EVENT_ORIENTATION_DISTANCE, "0"));

        this._checkLight = preferences.getBoolean(PREF_EVENT_ORIENTATION_CHECK_LIGHT, false);
        this._lightMin = preferences.getString(PREF_EVENT_ORIENTATION_LIGHT_MIN, "0");
        this._lightMax = preferences.getString(PREF_EVENT_ORIENTATION_LIGHT_MAX, "0");

        this._ignoredApplications = preferences.getString(PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS, "");
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_orientation_summary));
        } else {
            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_ORIENTATION_ENABLED, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML);
                    _value.append(getPassStatusString(context.getString(R.string.event_type_orientation), addPassStatus, DatabaseHandler.ETYPE_ORIENTATION, context));
                    _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
                }

                if (!ApplicationPreferences.applicationEventOrientationEnableScanning) {
//                    PPApplicationStatic.logE("[TEST BATTERY] EventPreferencesOrientation.getPreferencesDescription", "******** ### *******");
                    if (!ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile)
                        _value.append("* ").append(context.getString(R.string.array_pref_applicationDisableScanning_disabled)).append("! *").append(StringConstants.TAG_BREAK_HTML);
                    else
                        _value.append(context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile)).append(StringConstants.TAG_BREAK_HTML);
                } else {
                    boolean scanningPaused = ApplicationPreferences.applicationEventOrientationScanInTimeMultiply.equals("2") &&
                            GlobalUtils.isNowTimeBetweenTimes(
                                    ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyFrom,
                                    ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyTo);
                    if (scanningPaused) {
                        _value.append(context.getString(R.string.phone_profiles_pref_applicationEventScanningPaused)).append(StringConstants.TAG_BREAK_HTML);
                    }
                }

                String selectedValues = context.getString(R.string.applications_multiselect_summary_text_not_selected);
                if (!this._display.isEmpty() && !this._display.equals("-")) {
                    String[] splits = this._display.split(StringConstants.STR_SPLIT_REGEX);
                    String[] sideValues = context.getResources().getStringArray(R.array.eventOrientationDisplayValues);
                    String[] sideNames = context.getResources().getStringArray(R.array.eventOrientationDisplayArray);
                    //selectedValues = "";
                    StringBuilder __value = new StringBuilder();
                    for (String s : splits) {
                        int sideIdx = Arrays.asList(sideValues).indexOf(s);
                        if (sideIdx != -1) {
                            if (__value.length() > 0)
                                __value.append(", ");
                            __value.append(sideNames[sideIdx]);
                        }
                    }
                    selectedValues = __value.toString();
                }
                _value.append(context.getString(R.string.event_preferences_orientation_display)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(selectedValues, disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);

                //SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

                if (PPApplication.magneticFieldSensor != null) {
                    selectedValues = context.getString(R.string.applications_multiselect_summary_text_not_selected);
                    if (!this._sides.isEmpty() && !this._sides.equals("-")) {
                        String[] splits = this._sides.split(StringConstants.STR_SPLIT_REGEX);
                        String[] sideValues = context.getResources().getStringArray(R.array.eventOrientationSidesValues);
                        String[] sideNames = context.getResources().getStringArray(R.array.eventOrientationSidesArray);
                        //selectedValues = "";
                        StringBuilder __value = new StringBuilder();
                        for (String s : splits) {
                            int pos = Arrays.asList(sideValues).indexOf(s);
                            if (pos != -1) {
                                if (__value.length() > 0)
                                    __value.append(", ");
                                __value.append(sideNames[pos]);
                            }
                        }
                        selectedValues = __value.toString();
                    }
                    _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.event_preferences_orientation_sides)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(selectedValues, disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                }

                String[] distanceValues = context.getResources().getStringArray(R.array.eventOrientationDistanceTypeValues);
                String[] distanceNames = context.getResources().getStringArray(R.array.eventOrientationDistanceTypeArray);
                int i = Arrays.asList(distanceValues).indexOf(String.valueOf(this._distance));
                if (i != -1)
                    _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.event_preferences_orientation_distance)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(distanceNames[i], disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);

                if (this._checkLight) {
                    _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.event_preferences_orientation_light)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML)
                            .append(getColorForChangedPreferenceValue(this._lightMin + "-" + this._lightMax, disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                }
                else {
                    _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.event_preferences_orientation_light)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML)
                            .append(getColorForChangedPreferenceValue(context.getString(R.string.event_preferences_orientation_light_not_enabled), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                }

                String selectedApplications = context.getString(R.string.applications_multiselect_summary_text_not_selected);
                int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(context.getApplicationContext());
                if (extenderVersion == 0) {
                    selectedApplications = context.getString(R.string.profile_preferences_device_not_allowed) +
                            StringConstants.STR_COLON_WITH_SPACE + context.getString(R.string.preference_not_allowed_reason_not_extender_installed);
                } else if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_REQUIRED) {
                    selectedApplications = context.getString(R.string.profile_preferences_device_not_allowed) +
                            StringConstants.STR_COLON_WITH_SPACE + context.getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                } else if (!PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context.getApplicationContext(), false, true
                        /*, "EventPreferencesOrientation.getPreferencesDescription"*/)) {
                    selectedApplications = context.getString(R.string.profile_preferences_device_not_allowed) +
                            StringConstants.STR_COLON_WITH_SPACE + context.getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
                } else if (PPApplication.accessibilityServiceForPPPExtenderConnected == 0) {
                    selectedApplications = context.getString(R.string.profile_preferences_device_not_allowed) +
                            StringConstants.STR_COLON_WITH_SPACE + context.getString(R.string.preference_not_allowed_reason_state_of_accessibility_setting_for_extender_is_determined);
                } else if (!this._ignoredApplications.isEmpty() && !this._ignoredApplications.equals("-")) {
                    String[] splits = this._ignoredApplications.split(StringConstants.STR_SPLIT_REGEX);
                    if (splits.length == 1) {
                        String packageName = CApplication.getPackageName(splits[0]);
                        String activityName = CApplication.getActivityName(splits[0]);
                        PackageManager packageManager = context.getPackageManager();
                        if (activityName.isEmpty()) {
                            ApplicationInfo app;
                            try {
                                app = packageManager.getApplicationInfo(packageName, PackageManager.MATCH_ALL);
                                //if (app != null)
                                    selectedApplications = packageManager.getApplicationLabel(app).toString();
                            } catch (Exception e) {
                                selectedApplications = context.getString(R.string.applications_multiselect_summary_text_selected) + StringConstants.STR_COLON_WITH_SPACE + splits.length;
                            }
                        } else {
                            Intent intent = new Intent();
                            intent.setClassName(packageName, activityName);
                            ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                            if (info != null)
                                selectedApplications = info.loadLabel(packageManager).toString();
                        }
                    } else
                        selectedApplications = context.getString(R.string.applications_multiselect_summary_text_selected) + StringConstants.STR_COLON_WITH_SPACE + splits.length;
                }
                _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.event_preferences_orientation_ignoreForApplications)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(selectedApplications, disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
            }
        }

        return _value.toString();
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_ORIENTATION_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_ORIENTATION_ENABLED) ||
            key.equals(PREF_EVENT_ORIENTATION_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_APP_SETTINGS);
            String summary;
            int titleColor;
            if (preference != null) {
                if (!ApplicationPreferences.applicationEventOrientationEnableScanning) {
//                    PPApplicationStatic.logE("[TEST BATTERY] EventPreferencesOrientation.setSummary", "******** ### *******");
                    if (!ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile) {
                        summary = "* " + context.getString(R.string.array_pref_applicationDisableScanning_disabled) + "! *"+StringConstants.STR_SEPARATOR_LINE +
                                context.getString(R.string.phone_profiles_pref_eventOrientationAppSettings_summary);
                        titleColor = ContextCompat.getColor(context, R.color.errorColor);
                    }
                    else {
                        summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + StringConstants.STR_SEPARATOR_LINE +
                                context.getString(R.string.phone_profiles_pref_eventOrientationAppSettings_summary);
                        titleColor = 0;
                    }
                }
                else {
                    boolean scanningPaused = ApplicationPreferences.applicationEventOrientationScanInTimeMultiply.equals("2") &&
                            GlobalUtils.isNowTimeBetweenTimes(
                                    ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyFrom,
                                    ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyTo);
                    if (scanningPaused) {
                        summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningPaused) + StringConstants.STR_SEPARATOR_WITH_DOT +
                                context.getString(R.string.phone_profiles_pref_eventOrientationAppSettings_summary);
                    } else {
                        summary = context.getString(R.string.array_pref_applicationDisableScanning_enabled) + StringConstants.STR_SEPARATOR_WITH_DOT +
                                context.getString(R.string.phone_profiles_pref_eventOrientationAppSettings_summary);
                    }
                    titleColor = 0;
                }
                CharSequence sTitle = preference.getTitle();
                int titleLenght = 0;
                if (sTitle != null)
                    titleLenght = sTitle.length();
                Spannable sbt = new SpannableString(sTitle);
                Object[] spansToRemove = sbt.getSpans(0, titleLenght, Object.class);
                for(Object span: spansToRemove){
                    if(span instanceof CharacterStyle)
                        sbt.removeSpan(span);
                }
                if (preferences.getBoolean(PREF_EVENT_ORIENTATION_ENABLED, false)) {
                    if (titleColor != 0)
                        sbt.setSpan(new ForegroundColorSpan(titleColor), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                preference.setTitle(sbt);
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_ORIENTATION_DISPLAY)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value);
            }
        }
        if (key.equals(PREF_EVENT_ORIENTATION_SIDES)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value);
            }
        }
        if (key.equals(PREF_EVENT_ORIENTATION_DISTANCE))
        {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }

        if (key.equals(PREF_EVENT_ORIENTATION_ENABLED) || // added due to call setEnabled also when this preference is changed
                key.equals(PREF_EVENT_ORIENTATION_CHECK_LIGHT) ||
                key.equals(PREF_EVENT_ORIENTATION_LIGHT_CURRENT_VALUE) ||
                key.equals(PREF_EVENT_ORIENTATION_LIGHT_MIN) ||
                key.equals(PREF_EVENT_ORIENTATION_LIGHT_MAX))
        {
//            PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] EventPreferencesOrientation.setSummary", "xxx");
            Intent intent = new Intent(PPApplication.ACTION_REFRESH_EVENTS_PREFS_GUI_BROADCAST_RECEIVER);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            boolean hasLight = (sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null);
            if (preferences.getBoolean(PREF_EVENT_ORIENTATION_CHECK_LIGHT, false)) {
                Preference currentLightValuePreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_LIGHT_CURRENT_VALUE);
                if (currentLightValuePreference != null) {
                    currentLightValuePreference.setEnabled(hasLight);
                    //GlobalGUIRoutines.setPreferenceTitleStyleX(currentLightValuePreference, true, false, false, false, false);
                }
                BetterNumberPickerPreference minMaxPreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_LIGHT_MIN);
                if (minMaxPreference != null) {
                    minMaxPreference.setEnabled(hasLight);
                    minMaxPreference.setSummary(minMaxPreference.value);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(minMaxPreference, true, true, false, false, false, false);
                }
                minMaxPreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_LIGHT_MAX);
                if (minMaxPreference != null) {
                    minMaxPreference.setEnabled(hasLight);
                    minMaxPreference.setSummary(minMaxPreference.value);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(minMaxPreference, true, true, false, false, false, false);
                }
            }
            else {
                Preference currentLightValuePreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_LIGHT_CURRENT_VALUE);
                if (currentLightValuePreference != null) {
                    currentLightValuePreference.setEnabled(false);
                    //GlobalGUIRoutines.setPreferenceTitleStyleX(currentLightValuePreference, true, false, false, false, false);
                }
                BetterNumberPickerPreference minMaxPreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_LIGHT_MIN);
                if (minMaxPreference != null) {
                    minMaxPreference.setEnabled(false);
                    minMaxPreference.setSummary(minMaxPreference.value);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(minMaxPreference, true, false, false, false, false, false);
                }
                minMaxPreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_LIGHT_MAX);
                if (minMaxPreference != null) {
                    minMaxPreference.setEnabled(false);
                    minMaxPreference.setSummary(minMaxPreference.value);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(minMaxPreference, true, false, false, false, false, false);
                }
            }
        }
        /*
        if (key.equals(PREF_EVENT_ORIENTATION_INSTALL_EXTENDER)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(context.getApplicationContext());
                if (extenderVersion == 0) {
                    String summary = context.getString(R.string.profile_preferences_PPPExtender_not_installed_summary);// +
                            //"\n\n" + context.getString(R.string.event_preferences_orientation_PPPExtender_install_summary);
                    preference.setSummary(summary);
                }
                else {
                    String extenderVersionName = PPExtenderBroadcastReceiver.getExtenderVersionName(context);
                    String summary = context.getString(R.string.profile_preferences_PPPExtender_installed_summary) +
                            " " + extenderVersionName + " (" + extenderVersion + ")\n\n";
                    if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST)
                        summary = summary + context.getString(R.string.event_preferences_applications_PPPExtender_new_version_summary);
                    else
                        summary = summary + context.getString(R.string.pppextender_pref_dialog_PPPExtender_upgrade_summary);
                    preference.setSummary(summary);
                }
            }
        }
        */

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesOrientation.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesOrientation.isRunnable(context.getApplicationContext());
        //boolean isAllConfigured = event._eventPreferencesOrientation.isAllConfigured(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_ORIENTATION_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_DISPLAY);
        if (preference != null) {
            Set<String> set = prefMng.getSharedPreferences().getStringSet(PREF_EVENT_ORIENTATION_DISPLAY, null);
            StringBuilder sides = new StringBuilder();
            if (set != null) {
                for (String s : set) {
                    if (sides.length() > 0)
                        sides.append("|");
                    sides.append(s);
                }
            }
            boolean bold = sides.length() > 0;
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_SIDES);
        if (preference != null) {
            Set<String> set = prefMng.getSharedPreferences().getStringSet(PREF_EVENT_ORIENTATION_SIDES, null);
            StringBuilder sides = new StringBuilder();
            if (set != null) {
                for (String s : set) {
                    if (sides.length() > 0)
                        sides.append("|");
                    sides.append(s);
                }
            }
            boolean bold = sides.length() > 0;
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
        PPListPreference distancePreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_DISTANCE);
        if (distancePreference != null) {
            int index = distancePreference.findIndexOfValue(distancePreference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(distancePreference, enabled, index > 0, false, true, !isRunnable, false);
        }

        SwitchPreferenceCompat checkLightPreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_CHECK_LIGHT);
        if (checkLightPreference != null) {
            boolean bold = checkLightPreference.isChecked();
            GlobalGUIRoutines.setPreferenceTitleStyleX(checkLightPreference, enabled, bold, false, true, !isRunnable, false);
        }
        int _isAccessibilityEnabled = event._eventPreferencesOrientation.isAccessibilityServiceEnabled(context, false);
        boolean isAccessibilityEnabled = _isAccessibilityEnabled == 1;

        ExtenderDialogPreference extenderPreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_EXTENDER);
        if (extenderPreference != null) {
            extenderPreference.setSummaryEDP();
        }
        /*
        preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_ACCESSIBILITY_SETTINGS);
        if (preference != null) {

            String summary;
            if (isAccessibilityEnabled && (PPApplication.accessibilityServiceForPPPExtenderConnected == 1))
                summary = context.getString(R.string.accessibility_service_enabled);
            else {
                if (_isAccessibilityEnabled == -1) {
                    summary = context.getString(R.string.accessibility_service_not_used);
                    summary = summary + "\n\n" + context.getString(R.string.preference_not_used_extender_reason) + " " +
                            context.getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                } else {
                    summary = context.getString(R.string.accessibility_service_disabled);
                    summary = summary + "\n\n" + context.getString(R.string.event_preferences_orientation_AccessibilitySettingsForExtender_summary);
                }
            }
            preference.setSummary(summary);

            //GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, true, !isAccessibilityEnabled, false);
        }
        */
        preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS);
        if (preference != null) {
            String _value = prefMng.getSharedPreferences().getString(PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS, "");
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, !_value.isEmpty(), false, false,
                    !(isAccessibilityEnabled && (PPApplication.accessibilityServiceForPPPExtenderConnected == 1)), false);
        }

    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_ORIENTATION_ENABLED) ||
            key.equals(PREF_EVENT_ORIENTATION_CHECK_LIGHT)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING, context);
        }

        if (key.equals(PREF_EVENT_ORIENTATION_DISPLAY)) {
            Set<String> set = preferences.getStringSet(key, null);
            String sides;// = "";
            if (set != null) {
                String[] sideValues = context.getResources().getStringArray(R.array.eventOrientationDisplayValues);
                String[] sideNames = context.getResources().getStringArray(R.array.eventOrientationDisplayArray);
                StringBuilder _sides = new StringBuilder();
                for (String s : set) {
                    if (!s.isEmpty()) {
                        int pos = Arrays.asList(sideValues).indexOf(s);
                        if (pos != -1) {
                            //if (!sides.isEmpty())
                            //    sides = sides + ", ";
                            //sides = sides + sideNames[pos];
                            if (_sides.length() > 0)
                                _sides.append(", ");
                            _sides.append(sideNames[pos]);
                        }
                    }
                }
                sides = _sides.toString();
                if (sides.isEmpty())
                    sides = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            }
            else
                sides = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            setSummary(prefMng, key, sides, context);
        }

        if (key.equals(PREF_EVENT_ORIENTATION_SIDES)) {
            Set<String> set = preferences.getStringSet(key, null);
            String sides;// = "";
            if (set != null) {
                String[] sideValues = context.getResources().getStringArray(R.array.eventOrientationSidesValues);
                String[] sideNames = context.getResources().getStringArray(R.array.eventOrientationSidesArray);
                StringBuilder _sides = new StringBuilder();
                for (String s : set) {
                    if (!s.isEmpty()) {
                        int pos = Arrays.asList(sideValues).indexOf(s);
                        if (pos != -1) {
                            //if (!sides.isEmpty())
                            //    sides = sides + ", ";
                            //sides = sides + sideNames[pos];
                            if (_sides.length() > 0)
                                _sides.append(", ");
                            _sides.append(sideNames[pos]);
                        }
                    }
                }
                sides = _sides.toString();
                if (sides.isEmpty())
                    sides = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            }
            else
                sides = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            setSummary(prefMng, key, sides, context);
        }

        if (key.equals(PREF_EVENT_ORIENTATION_DISTANCE))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
        if (key.equals(PREF_EVENT_ORIENTATION_LIGHT_CURRENT_VALUE) ||
                key.equals(PREF_EVENT_ORIENTATION_LIGHT_MIN) ||
                key.equals(PREF_EVENT_ORIENTATION_LIGHT_MAX) )
        {
            //int value = preferences.getInt(key, 0);
            //setSummary(prefMng, key, String.valueOf(value), context);
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
        if (key.equals(PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS) ||
            key.equals(PREF_EVENT_ORIENTATION_EXTENDER) ||
            //key.equals(PREF_EVENT_ORIENTATION_INSTALL_EXTENDER) ||
            key.equals(PREF_EVENT_ORIENTATION_APP_SETTINGS))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_ORIENTATION_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_DISPLAY, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_SIDES, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_DISTANCE, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_CHECK_LIGHT, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_LIGHT_CURRENT_VALUE, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_LIGHT_MIN, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_LIGHT_MAX, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_EXTENDER, preferences, context);
        //setSummary(prefMng, PREF_EVENT_ORIENTATION_INSTALL_EXTENDER, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_APP_SETTINGS, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_ORIENTATION_ENABLED, false, context);
        if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesOrientation tmp = new EventPreferencesOrientation(this._event, this._enabled, this._display, this._sides, this._distance, this._checkLight, this._lightMin, this._lightMax, this._ignoredApplications);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_ORIENTATION_ENABLED, false);
                boolean runnable = tmp.isRunnable(context) && tmp.isAllConfigured(context) &&
                        (tmp.isAccessibilityServiceEnabled(context, false) == 1) &&
                        (PPApplication.accessibilityServiceForPPPExtenderConnected == 1);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_DEVICE_ORIENTATION).isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(runnable && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false,  false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        StringConstants.STR_COLON_WITH_SPACE+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    boolean isRunnable(Context context)
    {

        boolean runnable = super.isRunnable(context);

        boolean magneticSensor = false;
        boolean lightSensor = false;
        if (PPApplication.magneticFieldSensor != null)
            magneticSensor = true;
        if (PPApplication.lightSensor != null)
            lightSensor = true;
        boolean lightEnabled = _checkLight && lightSensor;

        if (magneticSensor)
            runnable = runnable && (!_display.isEmpty() || !_sides.isEmpty() || (_distance != 0) || lightEnabled);
        else
            runnable = runnable && (!_display.isEmpty() || (_distance != 0) || lightEnabled);
        /*
        if (_checkLight) {
            if ((sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) == null))
                runnable = false;
        }
       */

        return runnable;
    }

    @Override
    boolean isAllConfigured(Context context)
    {
        boolean allConfigured = super.isAllConfigured(context);

        allConfigured = allConfigured &&
                (ApplicationPreferences.applicationEventOrientationEnableScanning ||
                        ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile);

        return allConfigured;
    }

    @Override
    int isAccessibilityServiceEnabled(Context context, boolean againCheckInDelay)
    {
        int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(context);
        if (extenderVersion == 0)
            return -2;
        if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_REQUIRED)
            return -1;
        if ((_event.getStatus() != Event.ESTATUS_STOP) && this._enabled &&
                isRunnable(context) && isAllConfigured(context)) {
            if (PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context, againCheckInDelay, true
                        /*, "EventPreferencesOrientation.isAccessibilityServiceEnabled"*/))
                return 1;
        } else
            return 1;
        return 0;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        super.checkPreferences(prefMng, onlyCategory, context);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_ORIENTATION_ENABLED) != null) {
                SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
                boolean hasAccelerometer = (sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null);
                boolean hasMagneticField = (sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null);
                boolean hasProximity = (sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null);
                boolean hasLight = (sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null);
                boolean enabledAll = (hasAccelerometer) && (hasMagneticField);
                Preference preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_DISPLAY);

                if (preference != null) {
                    if (!hasAccelerometer)
                        preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                StringConstants.STR_COLON_WITH_SPACE + context.getString(R.string.preference_not_allowed_reason_no_hardware));
                    preference.setEnabled(hasAccelerometer);
                }
                preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_SIDES);
                if (preference != null) {
                    if (!enabledAll)
                        preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                StringConstants.STR_COLON_WITH_SPACE + context.getString(R.string.preference_not_allowed_reason_no_hardware));
                    preference.setEnabled(enabledAll);
                }
                boolean enabled = hasProximity;
                preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_DISTANCE);
                if (preference != null) {
                    if (!enabled)
                        preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                StringConstants.STR_COLON_WITH_SPACE + context.getString(R.string.preference_not_allowed_reason_no_hardware));
                    preference.setEnabled(enabled);
                }

                enabled = hasLight;
                Preference currentValuePreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_LIGHT_CURRENT_VALUE);
                if (currentValuePreference != null) {
                    if (!enabled)
                        currentValuePreference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                StringConstants.STR_COLON_WITH_SPACE + context.getString(R.string.preference_not_allowed_reason_no_hardware));
                    currentValuePreference.setEnabled(enabled);
                }
                Preference minLightPreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_LIGHT_MIN);
                if (minLightPreference != null) {
                    if (!enabled)
                        minLightPreference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                StringConstants.STR_COLON_WITH_SPACE + context.getString(R.string.preference_not_allowed_reason_no_hardware));
                    minLightPreference.setEnabled(enabled);
                }
                Preference maxLightPreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_LIGHT_MAX);
                if (maxLightPreference != null) {
                    if (!enabled)
                        maxLightPreference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                StringConstants.STR_COLON_WITH_SPACE + context.getString(R.string.preference_not_allowed_reason_no_hardware));
                    maxLightPreference.setEnabled(enabled);
                }
                if (enabled) {
                    final PreferenceManager _prefMng = prefMng;
                    final Context _context = context.getApplicationContext();

                    if (minLightPreference != null) {
                        minLightPreference.setOnPreferenceChangeListener((preference1, newValue) -> {
                            String sNewValue = (String) newValue;
                            int iNewValue;
                            if (sNewValue.isEmpty())
                                iNewValue = 0;
                            else
                                iNewValue = Integer.parseInt(sNewValue);

                            String sHightLevelValue = "2147483647";
                            if (_prefMng.getSharedPreferences() != null)
                                sHightLevelValue = _prefMng.getSharedPreferences().getString(PREF_EVENT_ORIENTATION_LIGHT_MAX, "2147483647");
                            int iHightLevelValue;
                            if (sHightLevelValue.isEmpty())
                                iHightLevelValue = 2147483647;
                            else
                                iHightLevelValue = Integer.parseInt(sHightLevelValue);

                            boolean OK = ((iNewValue >= 0) && (iNewValue <= iHightLevelValue));

                            if (!OK) {
                                PPApplication.showToast(_context.getApplicationContext(),
                                        _context.getString(R.string.event_preferences_orientation_light_level_min) + StringConstants.STR_COLON_WITH_SPACE +
                                                _context.getString(R.string.event_preferences_orientation_light_level_bad_value),
                                        Toast.LENGTH_SHORT);
                            }

                            return OK;
                        });
                    }
                    if (maxLightPreference != null) {
                        maxLightPreference.setOnPreferenceChangeListener((preference12, newValue) -> {
                            String sNewValue = (String) newValue;
                            int iNewValue;
                            if (sNewValue.isEmpty())
                                iNewValue = 2147483647;
                            else
                                iNewValue = Integer.parseInt(sNewValue);

                            String sLowLevelValue = "0";
                            if (_prefMng.getSharedPreferences() != null)
                                sLowLevelValue = _prefMng.getSharedPreferences().getString(PREF_EVENT_ORIENTATION_LIGHT_MIN, "0");
                            int iLowLevelValue;
                            if (sLowLevelValue.isEmpty())
                                iLowLevelValue = 0;
                            else
                                iLowLevelValue = Integer.parseInt(sLowLevelValue);

                            //noinspection ConstantConditions
                            boolean OK = ((iNewValue >= iLowLevelValue) && (iNewValue <= 2147483647));

                            if (!OK) {
                                PPApplication.showToast(_context.getApplicationContext(),
                                        _context.getString(R.string.event_preferences_orientation_light_level_max) + StringConstants.STR_COLON_WITH_SPACE +
                                                _context.getString(R.string.event_preferences_orientation_light_level_bad_value),
                                        Toast.LENGTH_SHORT);
                            }

                            return OK;
                        });
                    }
                }

                //enabled = PPExtenderBroadcastReceiver.isEnabled(context.getApplicationContext()/*, PPApplication.VERSION_CODE_EXTENDER_7_0*/, true, false
                //        /*, "EventPreferencesOrientation.checkPreferences"*/);
                ApplicationsMultiSelectDialogPreference applicationsPreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS);
                if (applicationsPreference != null) {
                    //applicationsPreference.setEnabled(enabled);
                    applicationsPreference.setSummaryAMSDP();
                }
                setSummary(prefMng, PREF_EVENT_ORIENTATION_APP_SETTINGS, preferences, context);
            }
        }
        setCategorySummary(prefMng, preferences, context);
    }

    /*long computeAlarm(Context context)
    {
        Calendar calEndTime = Calendar.getInstance();

        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();

        String applicationEventOrientationScanInPowerSaveMode = ApplicationPreferences.applicationEventOrientationScanInPowerSaveMode;

        boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
        if (isPowerSaveMode && applicationEventOrientationScanInPowerSaveMode.equals("2"))
            // start scanning in power save mode is not allowed
            return 0;

        int interval = ApplicationPreferences.applicationEventOrientationScanInterval;
        if (isPowerSaveMode && applicationEventOrientationScanInPowerSaveMode.equals("1"))
            interval *= 2;

        calEndTime.setTimeInMillis((calEndTime.getTimeInMillis() - gmtOffset) + (interval * 1000));
        //calEndTime.set(Calendar.SECOND, 0);
        //calEndTime.set(Calendar.MILLISECOND, 0);

        long alarmTime;
        alarmTime = calEndTime.getTimeInMillis();

        return alarmTime;
    }

    @Override
    void setSystemEventForStart(Context context)
    {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that change state into RUNNING;
        // from broadcast will by called EventsHandler

        removeAlarm(context);
    }

    @Override
    void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsHandler

        removeAlarm(context);

        if (!(isRunnable(context) && _enabled))
            return;

        long alarmTime = computeAlarm(context);

        if (alarmTime > 0)
            setAlarm(alarmTime, context);
    }

    @Override
    void removeSystemEvent(Context context)
    {
        removeAlarm(context);

    }
    */

    /*static int convertLightToSensor(float light, float maxLight) {
        return (int)Math.round(light / maxLight * 10000.0);
    }

    static float convertPercentsToLight(long percentage, float maxLight) {
        return Math.round(maxLight / 100 * percentage);
    }*/

    void doHandleEvent(EventsHandler eventsHandler, boolean forRestartEvents) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if ((EventStatic.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, false, eventsHandler.context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                boolean inCall = false;
                TelephonyManager telephony = (TelephonyManager) eventsHandler.context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephony != null) {
                    /*int callState = TelephonyManager.CALL_STATE_IDLE; //telephony.getCallState();
                    int simCount = telephony.getPhoneCount();
                    if (simCount > 1) {
                        if (PPApplication.phoneCallsListenerSIM1 != null)
                            callState = PPApplication.phoneCallsListenerSIM1.lastState;
                        if ((callState != TelephonyManager.CALL_STATE_RINGING) && (callState != TelephonyManager.CALL_STATE_OFFHOOK)){
                            if (PPApplication.phoneCallsListenerSIM2 != null)
                                callState = PPApplication.phoneCallsListenerSIM2.lastState;
                        }
                    }
                    else {
                        if (PPApplication.phoneCallsListenerDefaul != null)
                            callState = PPApplication.phoneCallsListenerDefaul.lastState;
                    }*/

                    int callState = GlobalUtils.getCallState(eventsHandler.context);

                    inCall = (callState == TelephonyManager.CALL_STATE_RINGING) || (callState == TelephonyManager.CALL_STATE_OFFHOOK);
                }
                if (inCall) {
                    // not allowed changes during call
                    eventsHandler.notAllowedOrientation = true;
                } else if (!ApplicationPreferences.applicationEventOrientationEnableScanning) {
                    //if (forRestartEvents)
                    //    orientationPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesOrientation.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                    //else
                    // not allowed for disabled orientation scanner
                    //    notAllowedOrientation = true;
                    eventsHandler.orientationPassed = false;
                } else if (!PPApplication.isScreenOn && ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn) {
//                    PPApplicationStatic.logE("[TEST BATTERY] EventPreferencesOrientation.doHandleEvent", "******** ### ******* (1)");
                    if (forRestartEvents)
                        eventsHandler.orientationPassed = (EventPreferences.SENSOR_PASSED_PASSED & getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                    else
                        // not allowed for screen Off
                        eventsHandler.notAllowedOrientation = true;
                } else {
//                    PPApplicationStatic.logE("[TEST BATTERY] EventPreferencesOrientation.doHandleEvent", "******** ### ******* (2)");

                    boolean scanningPaused = ApplicationPreferences.applicationEventOrientationScanInTimeMultiply.equals("2") &&
                            GlobalUtils.isNowTimeBetweenTimes(
                                    ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyFrom,
                                    ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyTo);

                    if (!scanningPaused) {

//                        PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesOrientation.doHandleEvent", "PPApplication.orientationScannerMutex");
                        synchronized (PPApplication.orientationScannerMutex) {
                            if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesServiceStatic.isOrientationScannerStarted()) {
                                PPApplicationStatic.startHandlerThreadOrientationScanner();
                                boolean lApplicationPassed = false;
                                if (!_ignoredApplications.isEmpty()) {
                                    if (PPExtenderBroadcastReceiver.isEnabled(eventsHandler.context.getApplicationContext(), PPApplication.VERSION_CODE_EXTENDER_REQUIRED, true, true
                                            /*, "EventPreferencesOrientation.doHandleEvent"*/)) {
                                        String foregroundApplication = ApplicationPreferences.prefApplicationInForeground;
//                                    PPApplicationStatic.logE("EventPreferencesOrientation.doHandleEvent", "foregroundApplication="+foregroundApplication);
                                        if (!foregroundApplication.isEmpty()) {
                                            String[] splits = _ignoredApplications.split(StringConstants.STR_SPLIT_REGEX);
                                            for (String split : splits) {
                                                if (!split.isEmpty()) {
                                                    String packageName = CApplication.getPackageName(split);
//                                                PPApplicationStatic.logE("EventPreferencesOrientation.doHandleEvent", "packageName="+packageName);

                                                    if (foregroundApplication.equals(packageName)) {
//                                                    PPApplicationStatic.logE("EventPreferencesOrientation.doHandleEvent", "lApplicationPassed=true");
                                                        lApplicationPassed = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    } else
                                        eventsHandler.notAllowedOrientation = true;
                                }
                                if (!lApplicationPassed) {
                                    boolean lDisplayPassed = false;
                                    boolean lSidePassed = false;

                                    boolean hasAccelerometer = PPApplication.accelerometerSensor != null;
                                    boolean hasMagneticField = PPApplication.magneticFieldSensor != null;
                                    boolean hasProximity = PPApplication.proximitySensor != null;
                                    boolean hasLight = PPApplication.lightSensor != null;

                                    boolean enabledAll = (hasAccelerometer) && (hasMagneticField);

                                    boolean configuredDisplay = false;
                                    if (hasAccelerometer) {
                                        if (!_display.isEmpty()) {
                                            String[] splits = _display.split(StringConstants.STR_SPLIT_REGEX);
                                            if (splits.length > 0) {
                                                configuredDisplay = true;
                                                //lDisplayPassed = false;
                                                for (String split : splits) {
                                                    if (!split.isEmpty()) {
                                                        try {
                                                            int side = -1;
                                                            try {
                                                                side = Integer.parseInt(split);
                                                            } catch (
                                                                    NumberFormatException ignored) {
                                                            }
                                                            if (side > -1) {
                                                                if (side == PPApplication.handlerThreadOrientationScanner.resultDisplayUp) {
                                                                    lDisplayPassed = true;
                                                                    break;
                                                                }
                                                            }
                                                        } catch (Exception e) {
                                                            PPApplicationStatic.recordException(e);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    boolean configuredSide = false;
                                    if (enabledAll) {
                                        if (!_sides.isEmpty()) {
                                            String[] splits = _sides.split(StringConstants.STR_SPLIT_REGEX);
                                            if (splits.length > 0) {
                                                configuredSide = true;
                                                //lSidePassed = false;
                                                for (String split : splits) {
                                                    if (!split.isEmpty()) {
                                                        try {
                                                            int side = -1;
                                                            try {
                                                                side = Integer.parseInt(split);
                                                            } catch (
                                                                    NumberFormatException ignored) {
                                                            }
                                                            if (side > -1) {
                                                                if (side == OrientationScannerHandlerThread.DEVICE_ORIENTATION_HORIZONTAL) {
                                                                    if (PPApplication.handlerThreadOrientationScanner.resultSideUp == PPApplication.handlerThreadOrientationScanner.resultDisplayUp) {
                                                                        lSidePassed = true;
                                                                        break;
                                                                    }
                                                                } else {
                                                                    if (side == PPApplication.handlerThreadOrientationScanner.resultSideUp) {
                                                                        lSidePassed = true;
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                        } catch (Exception e) {
                                                            PPApplicationStatic.recordException(e);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    boolean lDistancePassed = false;
                                    boolean configuredDistance = false;
                                    if (hasProximity) {
                                        if (_distance != 0) {
                                            configuredDistance = true;
                                            lDistancePassed = _distance == PPApplication.handlerThreadOrientationScanner.resultDeviceDistance;
                                        }
                                    }

                                    boolean lLightPassed = false;
                                    boolean configuredLight = false;
                                    if (hasLight) {
                                        if (_checkLight) {
                                            configuredLight = true;
                                            int light = PPApplication.handlerThreadOrientationScanner.resultLight;
                                            int min = Integer.parseInt(_lightMin);
                                            int max = Integer.parseInt(_lightMax);
                                            lLightPassed = (light >= min) && (light <= max);
                                        }
                                    }

                                    if (configuredDisplay || configuredSide || configuredDistance || configuredLight) {
                                        eventsHandler.orientationPassed = true;
                                        if (configuredDisplay)
                                            //noinspection ConstantConditions
                                            eventsHandler.orientationPassed = eventsHandler.orientationPassed && lDisplayPassed;
                                        if (configuredSide)
                                            eventsHandler.orientationPassed = eventsHandler.orientationPassed && lSidePassed;
                                        if (configuredDistance)
                                            eventsHandler.orientationPassed = eventsHandler.orientationPassed && lDistancePassed;
                                        if (configuredLight)
                                            eventsHandler.orientationPassed = eventsHandler.orientationPassed && lLightPassed;
                                    } else
                                        eventsHandler.notAllowedOrientation = true;
                                    //orientationPassed = lDisplayPassed || lSidePassed || lDistancePassed || lLightPassed;
                                } else
                                    eventsHandler.notAllowedOrientation = true;
                            } else {
                                eventsHandler.notAllowedOrientation = true;
                            }
                        }
                    } else
                        eventsHandler.orientationPassed = false;
                }

                if (!eventsHandler.notAllowedOrientation) {
                    if (eventsHandler.orientationPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedOrientation = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_ORIENTATION);
            }
        }
    }

}
