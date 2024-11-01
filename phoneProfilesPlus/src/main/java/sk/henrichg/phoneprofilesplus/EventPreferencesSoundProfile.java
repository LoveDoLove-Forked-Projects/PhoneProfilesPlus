package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class EventPreferencesSoundProfile extends EventPreferences {

    String _ringerModes;
    String _zenModes;

    static final String PREF_EVENT_SOUND_PROFILE_ENABLED = "eventSoundProfileEnabled";
    private static final String PREF_EVENT_SOUND_PROFILE_RINGER_MODES = "eventSoundProfileRingerModes";
    private static final String PREF_EVENT_SOUND_PROFILE_ZEN_MODES = "eventSoundProfileZenModes";

    static final String PREF_EVENT_SOUND_PROFILE_CATEGORY = "eventSoundProfileCategoryRoot";

    // it must be same as array/eventSoundProfileRingerModeValues for "array_pref_event_ringerMode_zenMode".
    static final String RINGER_MODE_DO_NOT_DISTURB_VALUE = "4";

    EventPreferencesSoundProfile(Event event,
                                 boolean enabled,
                                 String ringerModes,
                                 String zenModes)
    {
        super(event, enabled);

        this._ringerModes = ringerModes;
        this._zenModes = zenModes;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesSoundProfile._enabled;
        this._ringerModes = fromEvent._eventPreferencesSoundProfile._ringerModes;
        this._zenModes = fromEvent._eventPreferencesSoundProfile._zenModes;
        this.setSensorPassed(fromEvent._eventPreferencesSoundProfile.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_SOUND_PROFILE_ENABLED, _enabled);

        String[] splits = this._ringerModes.split(StringConstants.STR_SPLIT_REGEX);
        Set<String> set = new HashSet<>(Arrays.asList(splits));
        editor.putStringSet(PREF_EVENT_SOUND_PROFILE_RINGER_MODES, set);

        splits = this._zenModes.split(StringConstants.STR_SPLIT_REGEX);
        set = new HashSet<>(Arrays.asList(splits));
        editor.putStringSet(PREF_EVENT_SOUND_PROFILE_ZEN_MODES, set);

        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_SOUND_PROFILE_ENABLED, false);

        Set<String> set = preferences.getStringSet(PREF_EVENT_SOUND_PROFILE_RINGER_MODES, null);
        StringBuilder values = new StringBuilder();
        if (set != null) {
            for (String s : set) {
                if (values.length() > 0)
                    values.append("|");
                values.append(s);
            }
        }
        this._ringerModes = values.toString();

        set = preferences.getStringSet(PREF_EVENT_SOUND_PROFILE_ZEN_MODES, null);
        values = new StringBuilder();
        if (set != null) {
            for (String s : set) {
                if (values.length() > 0)
                    values.append("|");
                values.append(s);
            }
        }
        this._zenModes = values.toString();
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_soundProfile_summary));
        } else {
            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_SOUND_PROFILE_ENABLED, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML);
                    _value.append(getPassStatusString(context.getString(R.string.event_type_soundProfile), addPassStatus, DatabaseHandler.ETYPE_SOUND_PROFILE, context));
                    _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
                }

                boolean dndChecked = false;
                String selectedValues = context.getString(R.string.applications_multiselect_summary_text_not_selected);
                if (!this._ringerModes.isEmpty() && !this._ringerModes.equals("-")) {
                    String[] splits = this._ringerModes.split(StringConstants.STR_SPLIT_REGEX);
                    String[] values = context.getResources().getStringArray(R.array.eventSoundProfileRingerModeValues);
                    String[] names = context.getResources().getStringArray(R.array.eventSoundProfileRingerModeArray);
                    //selectedValues = "";
                    StringBuilder __value = new StringBuilder();
                    for (String s : splits) {
                        int idx = Arrays.asList(values).indexOf(s);
                        if (idx != -1) {
                            if (__value.length() > 0)
                                __value.append(", ");
                            if (values[idx].equals(RINGER_MODE_DO_NOT_DISTURB_VALUE))
                                dndChecked = true;
                            __value.append(names[idx]);
                        }
                    }
                    selectedValues = __value.toString();
                }
                _value.append(context.getString(R.string.event_preferences_soundProfile_ringerModes)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(selectedValues, disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);

                if (dndChecked) {
                    selectedValues = context.getString(R.string.applications_multiselect_summary_text_not_selected);
                    if (!this._zenModes.isEmpty() && !this._zenModes.equals("-")) {
                        String[] splits = this._zenModes.split(StringConstants.STR_SPLIT_REGEX);
                        String[] values = context.getResources().getStringArray(R.array.eventSoundProfileZenModeValues);
                        String[] names = context.getResources().getStringArray(R.array.eventSoundProfileZenModeArray);
                        //selectedValues = "";
                        StringBuilder __value = new StringBuilder();
                        for (String s : splits) {
                            int idx = Arrays.asList(values).indexOf(s);
                            if (idx != -1) {
                                if (__value.length() > 0)
                                    __value.append(", ");
                                __value.append(names[idx]);
                            }
                        }
                        selectedValues = __value.toString();
                    }
                    _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.event_preferences_soundProfile_zenModes)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(selectedValues, disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                }
            }
        }

        return _value.toString();
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_SOUND_PROFILE_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_SOUND_PROFILE_RINGER_MODES) ||
                key.equals(PREF_EVENT_SOUND_PROFILE_ZEN_MODES))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value);
            }
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesSoundProfile.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesSoundProfile.isRunnable(context);
        //boolean isAllConfigured = event._eventPreferencesSoundProfile.isAllConfigured(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_SOUND_PROFILE_ENABLED, false);

        Preference preference = prefMng.findPreference(PREF_EVENT_SOUND_PROFILE_RINGER_MODES);
        if (preference != null) {
            Set<String> set = prefMng.getSharedPreferences().getStringSet(PREF_EVENT_SOUND_PROFILE_RINGER_MODES, null);
            StringBuilder values = new StringBuilder();
            if (set != null) {
                for (String s : set) {
                    if (values.length() > 0)
                        values.append("|");
                    values.append(s);
                }
            }
            boolean bold = values.length() > 0;
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_SOUND_PROFILE_ZEN_MODES);
        if (preference != null) {
            Set<String> set = prefMng.getSharedPreferences().getStringSet(PREF_EVENT_SOUND_PROFILE_ZEN_MODES, null);
            StringBuilder values = new StringBuilder();
            if (set != null) {
                for (String s : set) {
                    if (values.length() > 0)
                        values.append("|");
                    values.append(s);
                }
            }
            boolean bold = values.length() > 0;
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, false, false, false);
        }
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_SOUND_PROFILE_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING, context);
        }

        if (key.equals(PREF_EVENT_SOUND_PROFILE_RINGER_MODES)) {
            Set<String> set = preferences.getStringSet(key, null);
            String values;// = "";
            if (set != null) {
                String[] sValues = context.getResources().getStringArray(R.array.eventSoundProfileRingerModeValues);
                String[] sNames = context.getResources().getStringArray(R.array.eventSoundProfileRingerModeArray);
                StringBuilder _value = new StringBuilder();
                for (String s : set) {
                    if (!s.isEmpty()) {
                        int pos = Arrays.asList(sValues).indexOf(s);
                        if (pos != -1) {
                            //if (!values.isEmpty())
                            //    values = values + ", ";
                            //values = values + sNames[pos];
                            if (_value.length() > 0)
                                _value.append(", ");
                            _value.append(sNames[pos]);
                        }
                    }
                }
                values = _value.toString();
                if (values.isEmpty())
                    values = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            }
            else
                values = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            setSummary(prefMng, key, values, context);
        }
        if (key.equals(PREF_EVENT_SOUND_PROFILE_ZEN_MODES)) {
            Set<String> set = preferences.getStringSet(key, null);
            String values;// = "";
            if (set != null) {
                String[] sValues = context.getResources().getStringArray(R.array.eventSoundProfileZenModeValues);
                String[] sNames = context.getResources().getStringArray(R.array.eventSoundProfileZenModeArray);
                StringBuilder _value = new StringBuilder();
                for (String s : set) {
                    if (!s.isEmpty()) {
                        int pos = Arrays.asList(sValues).indexOf(s);
                        if (pos != -1) {
                            //if (!values.isEmpty())
                            //    values = values + ", ";
                            //values = values + sNames[pos];
                            if (_value.length() > 0)
                                _value.append(", ");
                            _value.append(sNames[pos]);
                        }
                    }
                }
                values = _value.toString();
                if (values.isEmpty())
                    values = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            }
            else
                values = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            setSummary(prefMng, key, values, context);
        }

    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_SOUND_PROFILE_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_SOUND_PROFILE_RINGER_MODES, preferences, context);
        setSummary(prefMng, PREF_EVENT_SOUND_PROFILE_ZEN_MODES, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_SOUND_PROFILE_ENABLED, false, context);
        if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesSoundProfile tmp = new EventPreferencesSoundProfile(this._event, this._enabled,
                    this._ringerModes, this._zenModes);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_SOUND_PROFILE_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_SOUND_PROFILE_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_SOUND_PROFILE).isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(tmp.isRunnable(context) && tmp.isAllConfigured(context) && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false,  false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_SOUND_PROFILE_CATEGORY);
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

        runnable = runnable && (!_ringerModes.isEmpty());

        return runnable;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context)
    {
        super.checkPreferences(prefMng, onlyCategory, context);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_SOUND_PROFILE_ENABLED) != null) {
                boolean enabled = EventStatic.isEventPreferenceAllowed(PREF_EVENT_SOUND_PROFILE_ENABLED, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED;

                PPMultiSelectListPreference ringerModesPreference = prefMng.findPreference(PREF_EVENT_SOUND_PROFILE_RINGER_MODES);
                if (ringerModesPreference != null)
                    ringerModesPreference.setEnabled(enabled);

                PPMultiSelectListPreference zenModesPreference = prefMng.findPreference(PREF_EVENT_SOUND_PROFILE_ZEN_MODES);
                if (zenModesPreference != null)
                    zenModesPreference.setEnabled(enabled);

                if (enabled) {
                    if (ringerModesPreference != null) {
                        boolean checked = false;
                        Set<String> set = ringerModesPreference.getValues();
                        if (set != null) {
                            String[] sValues = context.getResources().getStringArray(R.array.eventSoundProfileRingerModeValues);
                            for (String s : set) {
                                if (!s.isEmpty()) {
                                    int pos = Arrays.asList(sValues).indexOf(s);
                                    if (pos != -1) {
                                        String value = sValues[pos];
                                        if (value.equals(RINGER_MODE_DO_NOT_DISTURB_VALUE)) {
                                            // checked is "Do not disturb"
                                            checked = true;
                                        }
                                    }
                                }
                            }
                        }
                        if (zenModesPreference != null)
                            zenModesPreference.setEnabled(checked);
                    }
                }
                setSummary(prefMng, PREF_EVENT_SOUND_PROFILE_ENABLED, preferences, context);
            }
        }
        setCategorySummary(prefMng, preferences, context);
    }

    /*
    @Override
    void setSystemEventForStart(Context context)
    {
    }

    @Override
    void setSystemEventForPause(Context context)
    {
    }

    @Override
    void removeSystemEvent(Context context)
    {
    }
    */

    void doHandleEvent(EventsHandler eventsHandler/*, boolean forRestartEvents*/) {
        if (_enabled) {

            int oldSensorPassed = getSensorPassed();
            if ((EventStatic.isEventPreferenceAllowed(EventPreferencesSoundProfile.PREF_EVENT_SOUND_PROFILE_ENABLED, false, eventsHandler.context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                eventsHandler.soundProfilePassed = false;
                //boolean tested = false;

                boolean dndChecked = false;
                if (!this._ringerModes.isEmpty() && !this._ringerModes.equals("-")) {
                    ActivateProfileHelper.getRingerMode(eventsHandler.context);

                    String[] splits = this._ringerModes.split(StringConstants.STR_SPLIT_REGEX);
                    String[] values = eventsHandler.context.getResources().getStringArray(R.array.eventSoundProfileRingerModeValues);
                    for (String s : splits) {
                        int idx = Arrays.asList(values).indexOf(s);
                        if (idx != -1) {
                            if (values[idx].equals(RINGER_MODE_DO_NOT_DISTURB_VALUE))
                                dndChecked = true;

                            // check ringer modes
                            switch (values[idx]) {
                                case "1":
                                    // ring
                                    if (ApplicationPreferences.prefRingerMode == Profile.RINGERMODE_RING)
                                        eventsHandler.soundProfilePassed = true;
                                    //tested = true;
                                    break;
                                case "2":
                                    // vibrate
                                    if (ApplicationPreferences.prefRingerMode == Profile.RINGERMODE_VIBRATE)
                                        eventsHandler.soundProfilePassed = true;
                                    //tested = true;
                                    break;
                                case "3":
                                    // silent
                                    if (ApplicationPreferences.prefRingerMode == Profile.RINGERMODE_SILENT)
                                        eventsHandler.soundProfilePassed = true;
                                    //tested = true;
                                    break;
                                /*
                                case "4":
                                    // dnd - must be checked dnd type
                                    if (ApplicationPreferences.prefRingerMode == Profile.RINGERMODE_ZENMODE)
                                        eventsHandler.soundProfilePassed = true;
                                    //tested = true;
                                    break;
                                */
                            }
                        }
                    }
                }

                if (dndChecked && (ApplicationPreferences.prefRingerMode == Profile.RINGERMODE_ZENMODE)) {
                    // DND ringer mode is configured and current ringer mode is DND
                    if (!this._zenModes.isEmpty() && !this._zenModes.equals("-")) {
                        ActivateProfileHelper.getZenMode(eventsHandler.context);

                        String[] splits = this._zenModes.split(StringConstants.STR_SPLIT_REGEX);
                        String[] values = eventsHandler.context.getResources().getStringArray(R.array.eventSoundProfileZenModeValues);
                        for (String s : splits) {
                            int idx = Arrays.asList(values).indexOf(s);
                            if (idx != -1) {
                                // check zen modes

                                switch (values[idx]) {
                                    case "1":
                                        // off
                                        if (ApplicationPreferences.prefZenMode == Profile.ZENMODE_ALL)
                                            eventsHandler.soundProfilePassed = true;
                                        //tested = true;
                                        break;
                                    case "2":
                                        // off with vibration
                                        if (ApplicationPreferences.prefZenMode == Profile.ZENMODE_ALL_AND_VIBRATE)
                                            eventsHandler.soundProfilePassed = true;
                                        //tested = true;
                                        break;
                                    case "3":
                                        // priority
                                        if (ApplicationPreferences.prefZenMode == Profile.ZENMODE_PRIORITY)
                                            eventsHandler.soundProfilePassed = true;
                                        //tested = true;
                                        break;
                                    case "4":
                                        // priority with vibration
                                        if (ApplicationPreferences.prefZenMode == Profile.ZENMODE_PRIORITY_AND_VIBRATE)
                                            eventsHandler.soundProfilePassed = true;
                                        //tested = true;
                                        break;
                                    case "5":
                                        // alarms only
                                        if (ApplicationPreferences.prefZenMode == Profile.ZENMODE_ALARMS)
                                            eventsHandler.soundProfilePassed = true;
                                        //tested = true;
                                        break;
                                    case "6":
                                        // total silence
                                        if (ApplicationPreferences.prefZenMode == Profile.ZENMODE_NONE)
                                            eventsHandler.soundProfilePassed = true;
                                        //tested = true;
                                        break;
                                }
                            }
                        }
                    }
                    else {
                        // dnd type is not configured, check if system ringer mode is set to DND
                        // dnd type is ignored in this situation
                        eventsHandler.soundProfilePassed = true;
                        //tested = true;
                    }
                }

                //eventsHandler.soundProfilePassed = eventsHandler.soundProfilePassed && tested;

                if (!eventsHandler.notAllowedSoundProfile) {
                    if (eventsHandler.soundProfilePassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedSoundProfile = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_SOUND_PROFILE);
            }
        }
    }

}
