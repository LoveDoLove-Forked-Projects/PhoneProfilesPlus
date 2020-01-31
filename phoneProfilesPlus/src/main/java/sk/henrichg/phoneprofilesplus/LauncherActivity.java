package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import me.drakeet.support.toast.ToastCompat;

public class LauncherActivity extends AppCompatActivity {

    private boolean activityStarted = false;
    private int startupSource;
    private DataWrapper dataWrapper;

    private static final int REQUEST_CODE_IMPORTANT_INFO = 1620;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //overridePendingTransition(0, 0);

        PhoneProfilesService instance = PhoneProfilesService.getInstance();
        if (instance == null) {
            return;
        }

        if (instance.getWaitForEndOfStart()) {
            return;
        }

        activityStarted = true;

        dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);

        Intent intent = getIntent();
        startupSource = intent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, 0);
        //PPApplication.logE("LauncherActivity.onCreate", "startupSource="+startupSource);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        boolean applicationStarted = PPApplication.getApplicationStarted(true);
        applicationStarted = applicationStarted && (!PhoneProfilesService.getInstance().getWaitForEndOfStart());
        if (!applicationStarted) {
            Toast msg = ToastCompat.makeText(getApplicationContext(),
                    getResources().getString(R.string.activate_profile_application_not_started),
                    Toast.LENGTH_LONG);
            msg.show();
        }

        if (!PPApplication.getApplicationStarted(true))
        {
            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("LauncherActivity.onStart", "application is not started");
                PPApplication.logE("LauncherActivity.onStart", "service instance=" + PhoneProfilesService.getInstance());
                if (PhoneProfilesService.getInstance() != null)
                    PPApplication.logE("LauncherActivity.onStart", "service hasFirstStart=" + PhoneProfilesService.getInstance().getServiceHasFirstStart());
            }*/
            // start PhoneProfilesService
            //PPApplication.firstStartServiceStarted = false;
            PPApplication.setApplicationStarted(getApplicationContext(), true);
            Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
            PPApplication.startPPService(this, serviceIntent);
            finish();
            return;
        }
        else
        {
            if ((PhoneProfilesService.getInstance() == null) || (!PhoneProfilesService.getInstance().getServiceHasFirstStart())) {
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("LauncherActivity.onStart", "application is started");
                    PPApplication.logE("LauncherActivity.onStart", "service instance=" + PhoneProfilesService.getInstance());
                    if (PhoneProfilesService.getInstance() != null)
                        PPApplication.logE("LauncherActivity.onStart", "service hasFirstStart=" + PhoneProfilesService.getInstance().getServiceHasFirstStart());
                }*/
                // start PhoneProfilesService
                //PPApplication.firstStartServiceStarted = false;
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
                PPApplication.startPPService(this, serviceIntent);
                finish();
                return;
            }
            /*else {
                PPApplication.logE("LauncherActivity.onStart", "application and service is started");
            }*/
        }

        if (activityStarted) {
            if (startupSource == 0) {
                // activity was not started from notification, widget

                PPApplication.showProfileNotification(/*getApplicationContext()*/true, false);
                //PPApplication.logE("ActivateProfileHelper.updateGUI", "from LauncherActivity.onStart");
                ActivateProfileHelper.updateGUI(dataWrapper.context, true, true);
                startupSource = PPApplication.STARTUP_SOURCE_LAUNCHER;
            }
        }

        endOnStart();
    }

    private void endOnStart()
    {
        //  application is already started - is in PhoneProfilesService
        //PPApplication.setApplicationStarted(getBaseContext(), true);

        if (!activityStarted)
            return;

        PhoneProfilesService instance = PhoneProfilesService.getInstance();
        if (instance == null) {
            finish();
            return;
        }

        if (instance.getWaitForEndOfStart()) {
            /*AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setMessage(R.string.application_is_initialized);
            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            dialogBuilder.setPositiveButton(android.R.string.ok, null);
            dialogBuilder.show();*/

            finish();
            return;
        }

        Intent intentLaunch;

        //PPApplication.logE("LauncherActivity.endOnStart", "startupSource="+startupSource);
        switch (startupSource) {
            case PPApplication.STARTUP_SOURCE_NOTIFICATION:
                //PPApplication.logE("LauncherActivity.endOnStart", "STARTUP_SOURCE_NOTIFICATION");
                //PPApplication.logE("LauncherActivity.endOnStart", "ApplicationPreferences.applicationNotificationLauncher="+ApplicationPreferences.applicationNotificationLauncher);
                if (ApplicationPreferences.applicationNotificationLauncher.equals("activator"))
                    intentLaunch = new Intent(getApplicationContext(), ActivateProfileActivity.class);
                else
                    intentLaunch = new Intent(getApplicationContext(), EditorProfilesActivity.class);
                break;
            case PPApplication.STARTUP_SOURCE_WIDGET:
                //PPApplication.logE("LauncherActivity.endOnStart", "STARTUP_SOURCE_WIDGET");
                //PPApplication.logE("LauncherActivity.endOnStart", "ApplicationPreferences.applicationWidgetLauncher="+ApplicationPreferences.applicationWidgetLauncher);
                if (ApplicationPreferences.applicationWidgetLauncher.equals("activator"))
                    intentLaunch = new Intent(getApplicationContext(), ActivateProfileActivity.class);
                else
                    intentLaunch = new Intent(getApplicationContext(), EditorProfilesActivity.class);
                break;
            default:
                //PPApplication.logE("LauncherActivity.endOnStart", "default");
                //PPApplication.logE("LauncherActivity.endOnStart", "ApplicationPreferences.applicationHomeLauncher="+ApplicationPreferences.applicationHomeLauncher);
                if (ApplicationPreferences.applicationHomeLauncher.equals("activator"))
                    intentLaunch = new Intent(getApplicationContext(), ActivateProfileActivity.class);
                else
                    intentLaunch = new Intent(getApplicationContext(), EditorProfilesActivity.class);
                break;
        }

        //PPApplication.logE("LauncherActivity.endOnStart", "applicationFirstStart="+ApplicationPreferences.applicationFirstStart(getApplicationContext()));
        if (ApplicationPreferences.applicationFirstStart(getApplicationContext())) {
            SharedPreferences sharedPreferences = ApplicationPreferences.getSharedPreferences(getApplicationContext());
            if (sharedPreferences != null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_FIRST_START, false);
                editor.apply();
            }
            intentLaunch = new Intent(getApplicationContext(), ImportantInfoActivity.class);
            intentLaunch.putExtra(ImportantInfoActivity.EXTRA_SHOW_QUICK_GUIDE, true);
            startActivityForResult(intentLaunch, REQUEST_CODE_IMPORTANT_INFO);
        }
        else {
            finish();

            if (startupSource == PPApplication.STARTUP_SOURCE_NOTIFICATION)
                intentLaunch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK /*| Intent.FLAG_ACTIVITY_NO_ANIMATION*/);
            else
                intentLaunch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intentLaunch.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
            getApplicationContext().startActivity(intentLaunch);
            // reset startupSource
            startupSource = 0;
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        //dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMPORTANT_INFO)
        {
            endOnStart();
        }
    }

    /*
    @Override
    public void finish()
    {
        super.finish();
        //overridePendingTransition(0, 0);
    }
    */

}
