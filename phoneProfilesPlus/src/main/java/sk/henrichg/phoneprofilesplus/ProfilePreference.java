package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

import java.lang.ref.WeakReference;

public class ProfilePreference extends DialogPreference {

    ProfilePreferenceFragment fragment;

    String profileId;

    final int addNoActivateItem;
    final int noActivateAsDoNotApply;
    final int showDuration;

    private final Context prefContext;

    final DataWrapper dataWrapper;

    public ProfilePreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        //noinspection resource
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PPProfilePreference);

        addNoActivateItem = typedArray.getInt(R.styleable.PPProfilePreference_addNoActivateItem, 0);
        noActivateAsDoNotApply = typedArray.getInt(R.styleable.PPProfilePreference_noActivateAsDoNotApply, 0);
        showDuration = typedArray.getInt(R.styleable.PPProfilePreference_showDuration, 0);

        profileId = "0";
        prefContext = context;
        //preferenceTitle = getTitle();

        dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);

        setWidgetLayoutResource(R.layout.preference_widget_profile_preference); // resource na layout custom preference - TextView-ImageView
        typedArray.recycle();

        setPositiveButtonText(null);
    }

    // this is caled also for setEnbaled()
    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);

        //Log.e("ProfilePreference.onBindViewHolder", "isEnabled="+isEnabled());

        //preferenceTitleView = view.findViewById(R.id.applications_pref_label);  // resource na title
        //preferenceTitleView.setText(preferenceTitle);

        ImageView profileIcon = (ImageView) holder.findViewById(R.id.profile_pref_icon);

        if (profileIcon != null)
        {
            Profile profile = dataWrapper.getProfileById(Long.parseLong(profileId), true, false, false);
            if (profile != null)
            {
                //int disabledColor = ContextCompat.getColor(prefContext, R.color.activityDisabledTextColor);

                if (profile.getIsIconResourceID())
                {
                    Bitmap bitmap = profile.increaseProfileIconBrightnessForContext(prefContext, profile._iconBitmap);
                    if (bitmap != null)
                        profileIcon.setImageBitmap(bitmap);
                    else {
                        if (profile._iconBitmap != null)
                            profileIcon.setImageBitmap(profile._iconBitmap);
                        else {
                            //profileIcon.setImageBitmap(null);
                            //int res = prefContext.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                            //        prefContext.PPApplication.PACKAGE_NAME);
                            int res = ProfileStatic.getIconResource(profile.getIconIdentifier());
                            profileIcon.setImageResource(res); // icon resource
                        }
                    }
                }
                else
                {
                    //Bitmap bitmap = profile.increaseProfileIconBrightnessForContext(prefContext, profile._iconBitmap);
                    //Bitmap bitmap = profile._iconBitmap;
                    //if (bitmap != null)
                    //    profileIcon.setImageBitmap(bitmap);
                    //else
                        profileIcon.setImageBitmap(profile._iconBitmap);
                }
                if (!isEnabled())
                    profileIcon.setAlpha(0.35f);
                else
                    profileIcon.setAlpha(1f);
            }
            else
            {
                //if ((addNoActivateItem == 1) && (Long.parseLong(profileId) == PPApplication.PROFILE_NO_ACTIVATE))
                //    profileIcon.setImageResource(R.drawable.ic_profile_default); // icon resource
                //else
                    profileIcon.setImageResource(R.drawable.ic_empty); // icon resource
                profileIcon.setAlpha(1f);
            }

            final Handler handler = new Handler(prefContext.getMainLooper());
            final WeakReference<ProfilePreference> preferenceWeakRef
                    = new WeakReference<>(this);
            handler.postDelayed(() -> {
//                    PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ProfilePreference.onBindViewHolder");
                ProfilePreference preference = preferenceWeakRef.get();
                if (preference != null)
                    preference.setSummary(Long.parseLong(preference.profileId));
            }, 200);
        }
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index)
    {
        super.onGetDefaultValue(a, index);
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        String value;
        try {
            value = getPersistedString((String) defaultValue);
        } catch  (Exception e) {
            value = (String) defaultValue;
        }
        profileId = value;
    }

    /*
    @Override
    protected void onPrepareForRemoval()
    {
        super.onPrepareForRemoval();
        //dataWrapper.invalidateDataWrapper();
        //dataWrapper = null;
    }
    */

    /*
    public String getProfileId()
    {
        return profileId;
    }
    */

    void setProfileId(long newProfileId)
    {
        String newValue = String.valueOf(newProfileId);

        if (!callChangeListener(newValue)) {
            // no save new value
            return;
        }

        profileId = newValue;

        // set summary
        setSummary(Long.parseLong(profileId));

        // save to preferences
        persistString(newValue);

        // and notify
        // this rewrite preference, by me, calls also onBindViewHolder() to change icon in widgetLayout,
        notifyChanged();

    }

    void setSummary(long profileId)
    {
        Profile profile = dataWrapper.getProfileById(profileId, false, false, false);
        if (profile != null)
        {
            if (showDuration == 1)
                setSummary(profile.getProfileNameWithDuration("", "", false, false, prefContext.getApplicationContext()));
            else
                setSummary(profile._name);
        }
        else
        {
            if ((addNoActivateItem == 1) && (profileId == Profile.PROFILE_NO_ACTIVATE))
                if (noActivateAsDoNotApply == 1)
                    setSummary(prefContext.getString(R.string.profile_preference_do_not_apply));
                else
                    setSummary(prefContext.getString(R.string.profile_preference_profile_end_no_activate));
            else
                setSummary(prefContext.getString(R.string.profile_preference_profile_not_set));
        }
    }


    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final SavedState myState = new SavedState(superState);
        myState.profileId = profileId;
        /*myState.addNoActivateItem = addNoActivateItem;
        myState.noActivateAsDoNotApply = noActivateAsDoNotApply;
        myState.showDuration = showDuration;*/
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if ((state == null) || (!state.getClass().equals(SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummary(Long.parseLong(profileId));
            return;
        }

        // restore instance state
        SavedState myState = (SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        profileId = myState.profileId;
        /*addNoActivateItem = myState.addNoActivateItem;
        noActivateAsDoNotApply = myState.noActivateAsDoNotApply;
        showDuration = myState.showDuration;*/

        setSummary(Long.parseLong(profileId));
        //notifyChanged();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String profileId;
        /*int addNoActivateItem;
        int noActivateAsDoNotApply;
        int showDuration;*/

        SavedState(Parcel source)
        {
            super(source);

            // restore profileId
            profileId = source.readString();
            /*addNoActivateItem = source.readInt();
            noActivateAsDoNotApply = source.readInt();
            showDuration = source.readInt();*/
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save profileId
            dest.writeString(profileId);
            /*dest.writeInt(addNoActivateItem);
            dest.writeInt(noActivateAsDoNotApply);
            dest.writeInt(showDuration);*/
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<ProfilePreference.SavedState> CREATOR =
                new Creator<>() {
            public ProfilePreference.SavedState createFromParcel(Parcel in)
            {
                return new ProfilePreference.SavedState(in);
            }
            public ProfilePreference.SavedState[] newArray(int size)
            {
                return new ProfilePreference.SavedState[size];
            }

        };

    }

}
