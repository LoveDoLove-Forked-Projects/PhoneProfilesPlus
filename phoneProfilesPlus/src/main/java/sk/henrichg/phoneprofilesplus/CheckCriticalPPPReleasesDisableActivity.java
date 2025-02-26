package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
//import me.drakeet.support.toast.ToastCompat;

public class CheckCriticalPPPReleasesDisableActivity extends AppCompatActivity
{
    private boolean activityStarted = false;
    private boolean criticalRelease = true;
    private int versionCode = 0;

    static final String EXTRA_PPP_RELEASE_CRITICAL = "github_release_critical";
    static final String EXTRA_PPP_RELEASE_CODE = "github_release_code";

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.countScreenOrientationLocks = 0;

        EditorActivity.itemDragPerformed = false;

        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

//        PPApplicationStatic.logE("[BACKGROUND_ACTIVITY] CheckCriticalGitHubReleasesDisableActivity.onCreate", "xxx");

//        if (showNotStartedToast()) {
//            finish();
//            return;
//        }

        Intent intent = getIntent();
        criticalRelease = intent.getBooleanExtra(EXTRA_PPP_RELEASE_CRITICAL, true);
        versionCode = intent.getIntExtra(EXTRA_PPP_RELEASE_CODE, 0);

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

//        if (showNotStartedToast()) {
//            if (!isFinishing())
//                finish();
//            return;
//        }

        if (activityStarted) {
            // set theme and language for dialog alert ;-)
            GlobalGUIRoutines.setTheme(this, true, false, false, false, false, false);
            //GlobalGUIRoutines.setLanguage(this);

            String title;
            String message;
            if (criticalRelease) {
                title = getString(R.string.critical_github_release);
                message = getString(R.string.critical_github_release_confirm_notification_disable);
            } else {
                title = getString(R.string.normal_github_release);
                message = getString(R.string.normal_github_release_confirm_notification_disable);
            }
            PPAlertDialog dialog = new PPAlertDialog(title, message,
                    getString(R.string.alert_button_yes), getString(R.string.alert_button_no), null, null,
                    (dialog1, which) -> {
                        CheckCriticalPPPReleasesBroadcastReceiver.setShowCriticalGitHubReleasesNotification(CheckCriticalPPPReleasesDisableActivity.this.getApplicationContext(), versionCode);
                        CheckCriticalPPPReleasesBroadcastReceiver.removeNotification(CheckCriticalPPPReleasesDisableActivity.this.getApplicationContext());
                        CheckCriticalPPPReleasesDisableActivity.this.finish();
                    },
                    (dialog12, which) -> {
                        CheckCriticalPPPReleasesBroadcastReceiver.setShowCriticalGitHubReleasesNotification(CheckCriticalPPPReleasesDisableActivity.this.getApplicationContext(), 0);
                        CheckCriticalPPPReleasesBroadcastReceiver.removeNotification(CheckCriticalPPPReleasesDisableActivity.this.getApplicationContext());
                        CheckCriticalPPPReleasesDisableActivity.this.finish();
                    },
                    null,
                    dialog13 -> {
                        CheckCriticalPPPReleasesBroadcastReceiver.removeNotification(CheckCriticalPPPReleasesDisableActivity.this.getApplicationContext());
                        CheckCriticalPPPReleasesDisableActivity.this.finish();
                    },
                    null,
                    null,
                    true, true,
                    false, false,
                    false,
                    false,
                    this
            );

            if (!isFinishing())
                dialog.showDialog();
        }
        else {
            if (isFinishing())
                finish();
        }
    }

//    private boolean showNotStartedToast() {
//        boolean applicationStarted = PPApplicationStatic.getApplicationStarted(true);
//        boolean fullyStarted = PPApplication.applicationFullyStarted /*&& (!PPApplication.applicationPackageReplaced)*/;
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

}
