package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

public class PhoneProfilesDashClockExtension extends DashClockExtension {

    private DataWrapper dataWrapper;
    private static volatile PhoneProfilesDashClockExtension instance;

    /** @noinspection unused*/
    public PhoneProfilesDashClockExtension()
    {
        //instance = this;
    }

    public static PhoneProfilesDashClockExtension getInstance()
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PhoneProfilesDashClockExtension constructor", "PPApplication.dashClockWidgetMutex");
        synchronized (PPApplication.dashClockWidgetMutex) {
            return instance;
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onInitialize(boolean isReconnect) {
        super.onInitialize(isReconnect);

//        PPApplicationStatic.logE("[IN_LISTENER] DashClockExtension.onInitialize", "xxx");

//        PPApplicationStatic.logE("[SYNCHRONIZED] PhoneProfilesDashClockExtension.onInitialize", "PPApplication.dashClockWidgetMutex");
        synchronized (PPApplication.dashClockWidgetMutex) {
            instance = this;

            //GlobalGUIRoutines.setLanguage(this);

            if (dataWrapper == null)
                dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_WIDGET, 0, 0f);

        }

        setUpdateWhenScreenOn(true);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

//        PPApplicationStatic.logE("[IN_LISTENER] DashClockExtension.onDestroy", "xxx");

//        PPApplicationStatic.logE("[SYNCHRONIZED] PhoneProfilesDashClockExtension.onDestroy", "PPApplication.dashClockWidgetMutex");
        synchronized (PPApplication.dashClockWidgetMutex) {
            instance = null;
            /*if (dataWrapper != null)
                dataWrapper.invalidateDataWrapper();
            dataWrapper = null;*/
        }
    }

    @Override
    protected void onUpdateData(int reason) {
//        PPApplicationStatic.logE("[IN_LISTENER] DashClockExtension.onUpdateData", "xxx");

        if (instance == null)
            return;

        if (dataWrapper == null)
            return;

        //final PhoneProfilesDashClockExtension _instance = instance;
        //final DataWrapper _dataWrapper = dataWrapper;

        Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWidget", "START run - from=PhoneProfilesDashClockExtension.onUpdateData");

            //Context appContext= appContextWeakRef.get();
            //DataWrapper dataWrapper = dataWrapperWeakRef.get();

//            PPApplicationStatic.logE("[SYNCHRONIZED] PhoneProfilesDashClockExtension.onUpdateData", "PPApplication.dashClockWidgetMutex");
            synchronized (PPApplication.dashClockWidgetMutex) {

                if ((instance != null) && (instance.dataWrapper != null)) {

                    //noinspection ExtractMethodRecommender
                    try {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PhoneProfilesDashClockExtension.onUpdateData", "do it");

                        Context appContext = instance.dataWrapper.context;
                        LocaleHelper.setApplicationLocale(appContext);

                        //profile = Profile.getMappedProfile(
                        //                            _dataWrapper.getActivatedProfile(true, false), this);
                        Profile profile = instance.dataWrapper.getActivatedProfile(true, false);

                        boolean isIconResourceID;
                        String iconIdentifier;
                        String profileName;
                        if (profile != null) {
                            isIconResourceID = profile.getIsIconResourceID();
                            iconIdentifier = profile.getIconIdentifier();
                            profileName = DataWrapperStatic.getProfileNameWithManualIndicatorAsString(profile, true, "", false, false, false, false, instance.dataWrapper);
                        } else {
                            isIconResourceID = true;
                            iconIdentifier = StringConstants.PROFILE_ICON_DEFAULT;
                            profileName = appContext.getString(R.string.profiles_header_profile_name_no_activated);
                        }
                        int iconResource;
                        if (isIconResourceID)
                            //iconResource = getResources().getIdentifier(iconIdentifier, "drawable", PPApplication.PACKAGE_NAME);
                            iconResource = ProfileStatic.getIconResource(iconIdentifier);
                        else
                            //iconResource = getResources().getIdentifier(Profile.PROFILE_ICON_DEFAULT, "drawable", PPApplication.PACKAGE_NAME);
                            iconResource = ProfileStatic.getIconResource(StringConstants.PROFILE_ICON_DEFAULT);

                        // intent
                        //Intent intent = GlobalGUIRoutines.getIntentForStartupSource(this, PPApplication.STARTUP_SOURCE_WIDGET);
                        Intent intent;
                        if (ApplicationPreferences.applicationWidgetDashClockLauncher.equals(StringConstants.EXTRA_ACTIVATOR))
                            intent = new Intent(this.getApplicationContext(), ActivatorActivity.class);
                        else
                            intent = new Intent(this.getApplicationContext(), EditorActivity.class);
                        // clear all opened activities
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_WIDGET);

                        String status = "";
                        //if (ApplicationPreferences.prefEventsBlocked) {
                        if (EventStatic.getEventsBlocked(appContext)) {
                            if (EventStatic.getForceRunEventRunning(appContext)) {
                                status = StringConstants.STR_ARROW_INDICATOR;
                            } else {
                                /*if (android.os.Build.VERSION.SDK_INT >= 16)
                                    status = "\uD83D\uDC46";
                                else */
                                status = StringConstants.STR_MANUAL;
                            }
                        }

                        ProfilePreferencesIndicator indicators = new ProfilePreferencesIndicator();

                        // Publish the extension data update.
                        instance.publishUpdate(new ExtensionData()
                                .visible(true)
                                .icon(iconResource)
                                .status(status)
                                .expandedTitle(profileName)
                                .expandedBody(indicators.getString(profile, /*0,*/ appContext))
                                .contentDescription(StringConstants.PHONE_PROFILES_PLUS +" - " + profileName)
                                .clickIntent(intent));
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            }
        };
        PPApplicationStatic.createDelayedGuiExecutor();
        PPApplication.delayedGuiExecutor.submit(runnable);
    }

    public void updateExtension()
    {
        onUpdateData(UPDATE_REASON_CONTENT_CHANGED);
    }

/*    private static abstract class PPHandlerThreadRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;
        final WeakReference<DataWrapper> dataWrapperWeakRef;

        PPHandlerThreadRunnable(Context appContext,
                                       DataWrapper dataWrapper) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.dataWrapperWeakRef = new WeakReference<>(dataWrapper);
        }

    }*/

}
