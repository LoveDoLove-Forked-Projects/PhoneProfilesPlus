package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class RingtonePreference extends DialogPreference {

    RingtonePreferenceFragment fragment;

    String ringtoneUri;
    private String defaultValue;
    private boolean savedInstanceState;

    //String oldRingtoneUri;

    final String ringtoneType;
    final boolean showSilent;
    final boolean showDefault;
    final int simCard;

    final Map<String, String> toneList = new LinkedHashMap<>();

    private final Context prefContext;

    static volatile MediaPlayer mediaPlayer = null;
    private static volatile int oldMediaVolume = -1;
    private static volatile boolean oldMediaMuted = false;
    private static volatile Timer playTimer = null;
    private static volatile boolean ringtoneIsPlayed = false;

    private SetRingtoneAsyncTask setRingtoneAsyncTask = null;

    static final String RINGTONE_TYPE_RINGTONE = "ringtone";
    static final String RINGTONE_TYPE_NOTIFICATION = "notification";
    static final String RINGTONE_TYPE_ALARM = "alarm";

    public RingtonePreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        //noinspection resource
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PPRingtonePreference);

        ringtoneType = typedArray.getString(R.styleable.PPRingtonePreference_ringtoneType);
        showSilent = typedArray.getBoolean(R.styleable.PPRingtonePreference_showSilent, false);
        showDefault = typedArray.getBoolean(R.styleable.PPRingtonePreference_showDefault, false);
        simCard = typedArray.getInt(R.styleable.PPRingtonePreference_simCard, 0);

        // set ringtoneUri to default
        ringtoneUri = "";
        if (!showSilent && showDefault) {
            if (ringtoneType != null) {
                switch (ringtoneType) {
                    case RINGTONE_TYPE_RINGTONE:
                        ringtoneUri = Settings.System.DEFAULT_RINGTONE_URI.toString();
                        break;
                    case RINGTONE_TYPE_NOTIFICATION:
                        ringtoneUri = Settings.System.DEFAULT_NOTIFICATION_URI.toString();
                        break;
                    case RINGTONE_TYPE_ALARM:
                        ringtoneUri = Settings.System.DEFAULT_ALARM_ALERT_URI.toString();
                        break;
                }
            }
        }

        prefContext = context;

        typedArray.recycle();
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        // set ringtone uri from preference value
        String value = getPersistedString((String) defaultValue);
        String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
        ringtoneUri = splits[0];
        this.defaultValue = (String)defaultValue;
        setSummary("");
        setRingtone("", true);
    }

    void refreshListView() {
        if ((fragment != null) && (fragment.getDialog() != null) && fragment.getDialog().isShowing()) {
            fragment.refreshListView();
        }
    }

    void setRingtone(String newRingtoneUri, boolean onlySetName)
    {
        if (!onlySetName)
            ringtoneUri = newRingtoneUri;

        setRingtoneAsyncTask = new SetRingtoneAsyncTask(this, prefContext);
        setRingtoneAsyncTask.execute();

        if (!onlySetName) {
            //View positive =
            //        getButton(DialogInterface.BUTTON_POSITIVE);
            //positive.setEnabled(true);
            setPositiveButtonText(android.R.string.ok);

            if (fragment != null)
                fragment.updateListView(false);
        }
    }

    void stopPlayRingtone() {
        AudioManager _audioManager = (AudioManager) prefContext.getSystemService(Context.AUDIO_SERVICE);
        if (_audioManager != null) {
            final Context appContext = prefContext.getApplicationContext();
            final String _ringtoneUri = ringtoneUri;
            final WeakReference<AudioManager> audioManagerWeakRef = new WeakReference<>(_audioManager);
            Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadPlayTone", "START run - from=RingtonePreferenceFragment.stopPlayRingtone");

                //Context appContext = appContextWeakRef.get();
                AudioManager audioManager = audioManagerWeakRef.get();

                if ((audioManager != null) && (_ringtoneUri != null)) {
                    if (playTimer != null) {
                        playTimer.cancel();
                        playTimer = null;
                    }
                    if ((mediaPlayer != null) && ringtoneIsPlayed) {
                        try {
                            if (mediaPlayer.isPlaying())
                                mediaPlayer.stop();
                        } catch (Exception e) {
                            //PPApplicationStatic.recordException(e);
                        }
                        try {
                            mediaPlayer.release();
                        } catch (Exception e) {
                            //PPApplicationStatic.recordException(e);
                        }
                        ringtoneIsPlayed = false;
                        mediaPlayer = null;

                        if (oldMediaVolume > -1)
                            ActivateProfileHelper.setMediaVolume(appContext, audioManager,
                                    audioManager.getStreamVolume(AudioManager.STREAM_MUSIC),
                                    oldMediaVolume, true, false);
                        if (oldMediaMuted) {
                            PPApplication.volumesInternalChange = true;
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

                            PPExecutors.scheduleDisableVolumesInternalChangeExecutor();
                        }
                    }
                }
            };
            PPApplicationStatic.createPlayToneExecutor();
            PPApplication.playToneExecutor.submit(runnable);
        }
    }

    void playRingtone() {
        if ((ringtoneUri == null) || ringtoneUri.isEmpty())
            return;

        stopPlayRingtone();

        final AudioManager _audioManager = (AudioManager)prefContext.getSystemService(Context.AUDIO_SERVICE);
        if (_audioManager != null) {

            final Uri __ringtoneUri = Uri.parse(ringtoneUri);

            final Context appContext = prefContext.getApplicationContext();
            final String _ringtoneType = ringtoneType;

            final WeakReference<AudioManager> audioManagerWeakRef = new WeakReference<>(_audioManager);
            final WeakReference<Uri> uriWeakRef = new WeakReference<>(__ringtoneUri);
            final WeakReference<RingtonePreference> preferenceWeakRef = new WeakReference<>(this);
            Runnable runnable = () -> {
                //Context appContext = appContextWeakRef.get();
                AudioManager audioManager = audioManagerWeakRef.get();
                Uri _ringtoneUri = uriWeakRef.get();
                RingtonePreference preference = preferenceWeakRef.get();

                if ((preference != null) && (audioManager != null) && (_ringtoneUri != null)) {

                    try {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadPlayTone", "START run - from=RingtonePreference.playRingtone");

                        /*if (TonesHandler.isPhoneProfilesSilent(ringtoneUri, appContext)) {
                            //String filename = appContext.getResources().getResourceEntryName(TonesHandler.TONE_ID) + ".ogg";
                            //File soundFile = new File(appContext.getFilesDir(), filename);
                            // /data/user/0/sk.henrichg.phoneprofilesplus/files
                            //mediaPlayer.setDataSource(soundFile.getAbsolutePath());
                            return;
                        }
                        else*/
                        {
                            if (mediaPlayer == null)
                                mediaPlayer = new MediaPlayer();

                            mediaPlayer.setDataSource(appContext, _ringtoneUri);
                        }

                        PPApplication.volumesInternalChange = true;
                        PPApplication.ringerModeInternalChange = true;

                        AudioAttributes attrs = new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build();
                        mediaPlayer.setAudioAttributes(attrs);
                        //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                        mediaPlayer.prepare();
                        mediaPlayer.setLooping(false);

                        oldMediaMuted = audioManager.isStreamMute(AudioManager.STREAM_MUSIC);
                        if (!oldMediaMuted)
                            oldMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                        else
                            oldMediaVolume = -1;

                        int ringtoneVolume = 0;
                        int maximumRingtoneValue = 0;

                        switch (_ringtoneType) {
                            case RINGTONE_TYPE_RINGTONE:
                                maximumRingtoneValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
                                if (!oldMediaMuted)
                                    ringtoneVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                                else
                                    ringtoneVolume = Math.round(maximumRingtoneValue * 0.75f);
                                break;
                            case RINGTONE_TYPE_NOTIFICATION:
                                maximumRingtoneValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
                                if (!oldMediaMuted)
                                    ringtoneVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
                                else
                                    ringtoneVolume = Math.round(maximumRingtoneValue * 0.75f);
                                break;
                            case RINGTONE_TYPE_ALARM:
                                maximumRingtoneValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                                if (!oldMediaMuted)
                                    ringtoneVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                                else
                                    ringtoneVolume = Math.round(maximumRingtoneValue * 0.75f);
                                break;
                        }

                        int maximumMediaValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                        float percentage = (float) ringtoneVolume / maximumRingtoneValue * 100.0f;
                        int mediaVolume = Math.round(maximumMediaValue / 100.0f * percentage);

                        if (oldMediaMuted) {
                            PPApplication.volumesInternalChange = true;
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                        }
                        ActivateProfileHelper.setMediaVolume(appContext, audioManager,
                                audioManager.getStreamVolume(AudioManager.STREAM_MUSIC),
                                mediaVolume, true, false);

                        mediaPlayer.start();
                        ringtoneIsPlayed = true;

                        playTimer = new Timer();
                        playTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                //Context _appContext = appContextWeakRef.get();
                                //AudioManager _audioManager = audioManagerWeakRef.get();

                                //if ((_appContext != null) && (_audioManager != null)) {
                                    if (mediaPlayer != null) {
                                        try {
                                            if (mediaPlayer.isPlaying())
                                                mediaPlayer.stop();
                                        } catch (Exception e) {
                                            //PPApplicationStatic.recordException(e);
                                        }
                                        try {
                                            mediaPlayer.release();
                                        } catch (Exception e) {
                                            //PPApplicationStatic.recordException(e);
                                        }

                                        if (oldMediaVolume > -1)
                                            ActivateProfileHelper.setMediaVolume(appContext, audioManager,
                                                    audioManager.getStreamVolume(AudioManager.STREAM_MUSIC),
                                                    oldMediaVolume, true, false);
                                        if (oldMediaMuted) {
                                            PPApplication.volumesInternalChange = true;
                                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                        }
                                    }

                                    ringtoneIsPlayed = false;
                                    mediaPlayer = null;

                                    PPExecutors.scheduleDisableRingerModeInternalChangeExecutor();
                                    PPExecutors.scheduleDisableVolumesInternalChangeExecutor();

                                    playTimer = null;
                                //}
                            }
                        }, mediaPlayer.getDuration());

                    } catch (Exception e) {
                        //Log.e("RingtonePreference.playRingtone", Log.getStackTraceString(e));
                        //PPApplicationStatic.recordException(e);
                        try {
                            preference.stopPlayRingtone();
                        } catch (Exception ignored) {}

                        PPExecutors.scheduleDisableRingerModeInternalChangeExecutor();
                        PPExecutors.scheduleDisableVolumesInternalChangeExecutor();
                    }
                }
            };
            PPApplicationStatic.createPlayToneExecutor();
            PPApplication.playToneExecutor.submit(runnable);

        }
    }

    void persistValue() {
        if (shouldPersist())
        {
            if (fragment != null) {
                final int position = fragment.getRingtonePosition();
                if (position != -1) {
                    // save to preferences
                    persistString(ringtoneUri);

                    // and notify
                    notifyChanged();
                }
            }
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            String value = getPersistedString(defaultValue);
            String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
            ringtoneUri = splits[0];
            setSummary("");
            setRingtone("", true);
        }
        savedInstanceState = false;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        savedInstanceState = true;

        stopPlayRingtone();

        final Parcelable superState = super.onSaveInstanceState();

        final SavedState myState = new SavedState(superState);
        myState.ringtoneUri = ringtoneUri;
        myState.defaultValue = defaultValue;
        //myState.oldRingtoneUri = oldRingtoneUri;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        stopPlayRingtone();

        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setRingtone("", true);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        ringtoneUri = myState.ringtoneUri;
        defaultValue = myState.defaultValue;
        //oldRingtoneUri = myState.oldRingtoneUri;

        setRingtone("", true);
    }

    @Override
    public void onDetached() {
        super.onDetached();
        if ((setRingtoneAsyncTask != null) &&
                setRingtoneAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            setRingtoneAsyncTask.cancel(true);
        setRingtoneAsyncTask = null;
    }

    // From DialogPreference
    private static class SavedState extends BaseSavedState {

        public static final Creator<SavedState> CREATOR =
                new Creator<RingtonePreference.SavedState>() {
                    public RingtonePreference.SavedState createFromParcel(Parcel in) {
                        return new RingtonePreference.SavedState(in);
                    }

                    public RingtonePreference.SavedState[] newArray(int size) {
                        return new RingtonePreference.SavedState[size];
                    }
                };

        String ringtoneUri;
        String defaultValue;

        //String oldRingtoneUri;

        SavedState(Parcel source) {
            super(source);
            ringtoneUri = source.readString();
            defaultValue = source.readString();
            //oldRingtoneUri = source.readString();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(ringtoneUri);
            dest.writeString(defaultValue);
            //dest.writeString(oldRingtoneUri);
        }
    }

/*
    private static class RefreshListViewAsyncTask extends AsyncTask<Void, Integer, Void> {

        //Ringtone defaultRingtone;
        private final Map<String, String> _toneList = new LinkedHashMap<>();

        private final WeakReference<RingtonePreference> preferenceWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListViewAsyncTask(RingtonePreference preference,
                                        Context prefContext) {
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected Void doInBackground(Void... params) {
            RingtonePreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((preference != null) && (prefContext != null)) {

                RingtoneManager manager = new RingtoneManager(prefContext);

                Uri uri;// = null;
                //switch (ringtoneType) {
                //    case "ringtone":
                //        uri = Settings.System.DEFAULT_RINGTONE_URI;
                //        break;
                //    case "notification":
                //        uri = Settings.System.DEFAULT_NOTIFICATION_URI;
                //        break;
                //    case "alarm":
                //        uri = Settings.System.DEFAULT_ALARM_ALERT_URI;
                //        break;
                //}

                //defaultRingtone = RingtoneManager.getRingtone(prefContext, uri);

                if (preference.showSilent) {
                    _toneList.put("", prefContext.getString(R.string.ringtone_preference_none));
                }

                Ringtone _ringtone;
                boolean typeIsSet = false;

                switch (preference.ringtoneType) {
                    case "ringtone":
                        manager.setType(RingtoneManager.TYPE_RINGTONE);
                        typeIsSet = true;
                        if (preference.showDefault) {
                            uri = Settings.System.DEFAULT_RINGTONE_URI;
                            _ringtone = RingtoneManager.getRingtone(prefContext, uri);
                            String ringtoneName;
                            try {
                                ringtoneName = _ringtone.getTitle(prefContext);
                            } catch (Exception e) {
                                ringtoneName = prefContext.getString(R.string.ringtone_preference_default_ringtone);
                            }
                            _toneList.put(Settings.System.DEFAULT_RINGTONE_URI.toString(), ringtoneName);
                        }
                        break;
                    case "notification":
                        manager.setType(RingtoneManager.TYPE_NOTIFICATION);
                        typeIsSet = true;
                        if (preference.showDefault) {
                            uri = Settings.System.DEFAULT_NOTIFICATION_URI;
                            _ringtone = RingtoneManager.getRingtone(prefContext, uri);
                            String ringtoneName;
                            try {
                                ringtoneName = _ringtone.getTitle(prefContext);
                            } catch (Exception e) {
                                ringtoneName = prefContext.getString(R.string.ringtone_preference_default_notification);
                            }
                            _toneList.put(Settings.System.DEFAULT_NOTIFICATION_URI.toString(), ringtoneName);
                        }
                        break;
                    case "alarm":
                        manager.setType(RingtoneManager.TYPE_ALARM);
                        typeIsSet = true;
                        if (preference.showDefault) {
                            uri = Settings.System.DEFAULT_ALARM_ALERT_URI;
                            _ringtone = RingtoneManager.getRingtone(prefContext, uri);
                            String ringtoneName;
                            try {
                                ringtoneName = _ringtone.getTitle(prefContext);
                            } catch (Exception e) {
                                ringtoneName = prefContext.getString(R.string.ringtone_preference_default_alarm);
                            }
                            _toneList.put(Settings.System.DEFAULT_ALARM_ALERT_URI.toString(), ringtoneName);
                        }
                        break;
                }

                if (typeIsSet) {
                    try {
                        Cursor cursor = manager.getCursor();

                        //profile._soundRingtone=content://settings/system/ringtone
                        //profile._soundNotification=content://settings/system/notification_sound
                        //profile._soundAlarm=content://settings/system/alarm_alert

                        while (cursor.moveToNext()) {
                            String _uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
                            String _title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
                            String _id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);

                            // for Samsung do not allow external tones
                            boolean add = true;
                            if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) {
                                if (preference.ringtoneType.equals("ringtone") && (preference.simCard != 0) && (!_uri.contains("content://media/internal")))
                                    add = false;
                                if (preference.ringtoneType.equals("notification") && (preference.simCard != 0) && (!_uri.contains("content://media/internal")))
                                    add = false;
                            }

                            if (add)
                                _toneList.put(_uri + "/" + _id, _title);
                        }
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            RingtonePreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((preference != null) && (prefContext != null)) {
                preference.toneList.clear();
                preference.toneList.putAll(_toneList);

                //if (defaultRingtone == null) {
                //    // ringtone not found
                //    //View positive = getButton(DialogInterface.BUTTON_POSITIVE);
                //    //positive.setEnabled(false);
                //    setPositiveButtonText(null);
                //}

                if (preference.fragment != null)
                    preference.fragment.updateListView(true);
            }
        }

    }
*/

    private static class SetRingtoneAsyncTask extends AsyncTask<Void, Integer, Void> {

        private String ringtoneName;

        private final WeakReference<RingtonePreference> preferenceWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public SetRingtoneAsyncTask(RingtonePreference preference,
                                    Context prefContext) {
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected Void doInBackground(Void... params) {
            RingtonePreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((preference != null) && (prefContext != null)) {
                if ((preference.ringtoneUri == null) || preference.ringtoneUri.isEmpty())
                    ringtoneName = prefContext.getString(R.string.ringtone_preference_none);
                else {
                    Uri uri = Uri.parse(preference.ringtoneUri);
                    Ringtone ringtone = RingtoneManager.getRingtone(prefContext, uri);
                    try {
                        ringtoneName = ringtone.getTitle(prefContext);
                    } catch (Exception e) {
                        ringtoneName = prefContext.getString(R.string.ringtone_preference_not_set);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            RingtonePreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((preference != null) && (prefContext != null)) {
                preference.setSummary(ringtoneName);
            }
        }

    }

/*    private static abstract class PlayRingtoneRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;
        final WeakReference<AudioManager> audioManagerWeakRef;
        final WeakReference<Uri> ringtoneUriWeakRef;

        PlayRingtoneRunnable(Context appContext,
                                       AudioManager audioManager,
                                       Uri ringtoneUri) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.audioManagerWeakRef = new WeakReference<>(audioManager);
            this.ringtoneUriWeakRef = new WeakReference<>(ringtoneUri);
        }

    }*/

/*    private static abstract class PlayRingtoneTimerTask extends TimerTask {
        public final WeakReference<Context> appContextWeakRef;
        public final WeakReference<AudioManager> audioManagerWeakRef;

        public PlayRingtoneTimerTask(Context appContext,
                                    AudioManager audioManager) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.audioManagerWeakRef = new WeakReference<>(audioManager);
        }
    }*/

/*    private static abstract class StopPlayRingtoneRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;
        final WeakReference<AudioManager> audioManagerWeakRef;

        StopPlayRingtoneRunnable(Context appContext,
                                    AudioManager audioManager) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.audioManagerWeakRef = new WeakReference<>(audioManager);
        }

    }*/

}
