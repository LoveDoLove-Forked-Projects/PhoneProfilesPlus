package sk.henrichg.phoneprofilesplus;

import androidx.appcompat.app.AppCompatActivity;

public class AutostartPermissionActivity extends AppCompatActivity
{
/*
    private boolean activityStarted = false;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

//        PPApplicationStatic.logE("[BACKGROUND_ACTIVITY] AutostartPermissionActivity.onCreate", "xxx");

//        if (showNotStartedToast()) {
//            finish();
//            return;
//        }

        activityStarted = true;

        // close notification drawer - broadcast pending intent not close it :-/
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        sendBroadcast(it);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onStart()
    {
        super.onStart();
//        PPApplicationStatic.logE("[BACKGROUND_ACTIVITY] AutostartPermissionActivity.onStart", "xxx");

//        if (showNotStartedToast()) {
//            if (!isFinishing())
//                finish();
//            return;
//        }

        if (activityStarted) {
            // set theme and language for dialog alert ;-)
            GlobalGUIRoutines.setTheme(this, true, false, false, false, false, false);
            //GlobalGUIRoutines.setLanguage(this);

//            PPApplicationStatic.logE("[BACKGROUND_ACTIVITY] AutostartPermissionActivity.onStart", "do AutoStartPermissionHelper.getAutoStartPermission()");

            final AutoStartPermissionHelper autoStartPermissionHelper = AutoStartPermissionHelper.getInstance();
            if (autoStartPermissionHelper.isAutoStartPermissionAvailable(getApplicationContext())) {
                boolean success;
                try {
                    success = autoStartPermissionHelper.getAutoStartPermission(this);
                    finish();
                }catch (Exception e) {
                    success = false;
                }
                if (!success) {
                    final AppCompatActivity activity = this;

                    CharSequence message;
                    if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI)
                        message = activity.getString(R.string.phone_profiles_pref_systemAutoStartManager_settingScreenNotFound_huawei_alert);
                    else
                        message = activity.getString(R.string.phone_profiles_pref_systemAutoStartManager_settingScreenNotFound_alert);

                    PPAlertDialog dialog = new PPAlertDialog(
                            activity.getString(R.string.phone_profiles_pref_systemAutoStartManager),
                            message,
                            getString(android.R.string.cancel), null, null, null,
                            (dialog1, which) -> activity.finish(),
                            null,
                            null,
                            null,
                            null,
                            false, false,
                            false, false,
                            false,
                            false,
                            this
                    );

                    if (!isFinishing())
                        dialog.show();
                }
            }

        }
        else {
            if (isFinishing())
                finish();
        }
    }

//    private boolean showNotStartedToast() {
//        boolean applicationStarted = PPApplicationStatic.getApplicationStarted(true);
//        boolean fullyStarted = PPApplication.applicationFullyStarted /*&& (!PPApplication.applicationPackageReplaced);
//        if (!applicationStarted) {
//            String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_not_started);
//            PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
//            return true;
//        }
//        if (!fullyStarted) {
//            if ((PPApplication.startTimeOfApplicationStart > 0) &&
//                    ((Calendar.getInstance().getTimeInMillis() - PPApplication.startTimeOfApplicationStart) > PPApplication.APPLICATION_START_DELAY)) {
//                Intent activityIntent = new Intent(this, WorkManagerNotWorkingActivity.class);
//                // clear all opened activities
//                activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(activityIntent);
//            }
//            else {
//                String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_starting_toast);
//                PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
//            }
//            return true;
//        }
//        return false;
//    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }
*/
}
