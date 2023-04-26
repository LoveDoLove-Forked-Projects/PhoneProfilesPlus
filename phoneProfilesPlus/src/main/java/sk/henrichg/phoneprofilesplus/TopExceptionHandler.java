package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.RemoteServiceException;
import android.content.Context;
import android.os.DeadSystemException;
import android.provider.Settings;

import androidx.annotation.NonNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeoutException;

class TopExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Thread.UncaughtExceptionHandler defaultUEH;
    private final Context applicationContext;
    private final int actualVersionCode;

    static final String CRASH_FILENAME = "crash.txt";

    TopExceptionHandler(Context applicationContext, int actualVersionCode) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.applicationContext = applicationContext;
        this.actualVersionCode = actualVersionCode;
    }

    public void uncaughtException(@NonNull Thread _thread, @NonNull Throwable _exception)
    {
//        Log.e("TopExceptionHandler.uncaughtException", "xxx");

        try {
            //if (PPApplication.lockDeviceActivity != null) {
            if (PPApplication.lockDeviceActivityDisplayed) {
                boolean canWriteSettings;// = true;
                canWriteSettings = Settings.System.canWrite(applicationContext);
                if (canWriteSettings) {
                    /*if (PPApplication.deviceIsOppo || PPApplication.deviceIsRealme) {
                        if (PPApplication.screenTimeoutHandler != null) {
                            PPApplication.screenTimeoutHandler.post(() -> {
                                synchronized (PPApplication.rootMutex) {
                                    String command1 = "settings put system " + Settings.System.SCREEN_OFF_TIMEOUT + " " + PPApplication.screenTimeoutBeforeDeviceLock;
                                    //if (PPApplication.isSELinuxEnforcing())
                                    //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                    Command command = new Command(0, false, command1); //, command2);
                                    try {
                                        RootTools.getShell(false, Shell.ShellContext.SYSTEM_APP).add(command);
                                        PPApplication.commandWait(command, "TopExceptionHandler.uncaughtException");
                                    } catch (Exception ee) {
                                        // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                        //Log.e("TopExceptionHandler.uncaughtException", Log.getStackTraceString(e));
                                        //PPApplicationStatic.recordException(e);
                                    }
                                }
                            });
                        }
                    } else*/
                    //if ((PPApplication.lockDeviceActivity != null) &&
                    //        (PPApplication.screenTimeoutWhenLockDeviceActivityIsDisplayed != 0))
                    if (PPApplication.lockDeviceActivityDisplayed &&
                            (PPApplication.screenTimeoutWhenLockDeviceActivityIsDisplayed != 0))
                        Settings.System.putInt(applicationContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, PPApplication.screenTimeoutWhenLockDeviceActivityIsDisplayed);
                }
            }
        } catch (Exception ee) {
            //Log.e("TopExceptionHandler.uncaughtException", Log.getStackTraceString(ee));
        }

        try {
            if (PPApplication.crashIntoFile) {
                StackTraceElement[] arr = _exception.getStackTrace();
                StringBuilder report = new StringBuilder(_exception.toString());

                report.append("\n\n");

                report.append("----- App version code: ").append(actualVersionCode).append("\n\n");

                for (StackTraceElement anArr : arr) {
                    report.append("    ").append(anArr.toString()).append("\n");
                }
                report.append("-------------------------------\n\n");

                report.append("--------- Stack trace ---------\n\n");
                for (StackTraceElement anArr : arr) {
                    report.append("    ").append(anArr.toString()).append("\n");
                }
                report.append("-------------------------------\n\n");

                // If the exception was thrown in a background thread inside
                // AsyncTask, then the actual exception can be found with getCause
                report.append("--------- Cause ---------------\n\n");
                Throwable cause = _exception.getCause();
                if (cause != null) {
                    report.append(cause.toString()).append("\n\n");
                    arr = cause.getStackTrace();
                    for (StackTraceElement anArr : arr) {
                        report.append("    ").append(anArr.toString()).append("\n");
                    }
                }
                report.append("-------------------------------\n\n");

                logIntoFile("E", "TopExceptionHandler", report.toString());
            }
        } catch (Exception ee) {
            //Log.e("TopExceptionHandler.uncaughtException", Log.getStackTraceString(ee));
        }

//        Log.e("TopExceptionHandler.uncaughtException", "defaultUEH=" + defaultUEH);

        if (defaultUEH != null) {
//            Log.e("TopExceptionHandler.uncaughtException", "(2)");

            //TODO must add these filtered exceptions also into CustomACRAReportingAdministrator

            boolean ignore = false;
            if (_thread.getName().equals("FinalizerWatchdogDaemon") && (_exception instanceof TimeoutException)) {
                // ignore these exceptions
                // java.util.concurrent.TimeoutException: com.android.internal.os.BinderInternal$GcWatcher.finalize() timed out after 10 seconds
                // https://stackoverflow.com/a/55999687/2863059
                ignore = true;
            }
//            Log.e("TopExceptionHandler.uncaughtException", "(2x)");
            if (_exception instanceof DeadSystemException) {
                // ignore these exceptions
                // these are from dead of system for example:
                // java.lang.RuntimeException: Unable to create service
                // androidx.work.impl.background.systemjob.SystemJobService:
                // java.lang.RuntimeException: android.os.DeadSystemException
                ignore = true;
            }
//            Log.e("TopExceptionHandler.uncaughtException", "(2y)");
            if (_exception.getClass().getSimpleName().equals("CannotDeliverBroadcastException") &&
                    (_exception instanceof RemoteServiceException)) {
                // ignore but not exist exception
                // android.app.RemoteServiceException$CannotDeliverBroadcastException: can't deliver broadcast
                // https://stackoverflow.com/questions/72902856/cannotdeliverbroadcastexception-only-on-pixel-devices-running-android-12
                ignore = true;
            }
//            Log.e("TopExceptionHandler.uncaughtException", "(2z)");

            // this is only for debuging, how is handled ignored exceptions
//            if (_exception instanceof java.lang.RuntimeException) {
//                if (_exception.getMessage() != null) {
//                    if (_exception.getMessage().equals("Test Crash"))
//                        ignore = true;
//                    else
//                    if (_exception.getMessage().equals("Test non-fatal exception"))
//                        ignore = true;
//                }
//            }

//            Log.e("TopExceptionHandler.uncaughtException", "ignore="+ignore);

            if (!ignore) {
                //Delegates to Android's error handling
//                Log.e("TopExceptionHandler.uncaughtException", "(3)");
                defaultUEH.uncaughtException(_thread, _exception);
//                Log.e("TopExceptionHandler.uncaughtException", "(4)");
            } else
                //Prevents the service/app from freezing
                System.exit(2);
        }
        else
            //Prevents the service/app from freezing
            System.exit(2);

//        Log.e("TopExceptionHandler.uncaughtException", "end");
    }

    private void logIntoFile(@SuppressWarnings("SameParameterValue") String type,
                             @SuppressWarnings("SameParameterValue") String tag,
                             String text)
    {
        try {
            /*File sd = Environment.getExternalStorageDirectory();
            File exportDir = new File(sd, PPApplication.EXPORT_PATH);
            if (!(exportDir.exists() && exportDir.isDirectory()))
                exportDir.mkdirs();

            File logFile = new File(sd, PPApplication.EXPORT_PATH + "/" + CRASH_FILENAME);*/

            File path = applicationContext.getExternalFilesDir(null);
            File logFile = new File(path, CRASH_FILENAME);

            if (logFile.length() > 1024 * 10000)
                resetLog();

            if (!logFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                logFile.createNewFile();
            }

            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
            String time = sdf.format(Calendar.getInstance().getTimeInMillis());
            String log = time + "--" + type + "-----" + tag + "------" + text;
            buf.append(log);
            buf.newLine();
            buf.flush();
            buf.close();
        } catch (IOException ee) {
            //Log.e("TopExceptionHandler.logIntoFile", Log.getStackTraceString(ee));
        }
    }

    private void resetLog()
    {
        /*File sd = Environment.getExternalStorageDirectory();
        File exportDir = new File(sd, PPApplication.EXPORT_PATH);
        if (!(exportDir.exists() && exportDir.isDirectory()))
            exportDir.mkdirs();

        File logFile = new File(sd, PPApplication.EXPORT_PATH + "/" + CRASH_FILENAME);*/

        File path = applicationContext.getExternalFilesDir(null);
        File logFile = new File(path, CRASH_FILENAME);

        //noinspection ResultOfMethodCallIgnored
        logFile.delete();
    }

}
