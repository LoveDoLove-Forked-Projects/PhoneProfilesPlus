package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;

// Disable action button
public class StartLauncherFromNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("##### StartLauncherFromNotificationReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "StartLauncherFromNotificationReceiver.onReceive", "StartLauncherFromNotificationReceiver_onReceive");

        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                //PPApplication.logE("StartLauncherFromNotificationReceiver.onReceive", "action="+action);

                if (action.equals(PhoneProfilesService.ACTION_START_LAUNCHER_FROM_NOTIFICATION)) {
                    final Context appContext = context.getApplicationContext();
                    Handler _handler = new Handler(appContext.getMainLooper());
                    Runnable r = new Runnable() {
                        public void run() {
                            //PPApplication.logE("StartLauncherFromNotificationReceiver.onReceive", "start activity");

                            // intent to LauncherActivity, for click on notification
                            Intent launcherIntent = new Intent(appContext, LauncherActivity.class);
                            // clear all opened activities
                            launcherIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK/*|Intent.FLAG_ACTIVITY_NO_ANIMATION*/);
                            // setup startupSource
                            launcherIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_NOTIFICATION);
                            appContext.startActivity(launcherIntent);
                        }
                    };
                    // TODO maybe will be fixed in next Samsung update, because also in another
                    // application (not One Ui2) is the same bug
                    if (PPApplication.deviceIsSamsung && (Build.VERSION.SDK_INT >= 29) &&
                            ApplicationPreferences.applicationWidgetLauncher.equals("activator"))
                        _handler.postDelayed(r, 1000);
                    else
                        _handler.post(r);
                }
            }
        }
    }

}
