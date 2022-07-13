package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

public class VPNNetworkCallback extends ConnectivityManager.NetworkCallback {

    private final Context context;

    static boolean connected = false;

    VPNNetworkCallback(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void onLost(Network network) {
        //record vpn disconnect event
        PPApplication.logE("[IN_LISTENER] ----------- VPNNetworkCallback.onLost", "xxx");
        connected = false;
        doConnection();
    }

    @Override
    public void onUnavailable() {
        PPApplication.logE("[IN_LISTENER] ----------- VPNNetworkCallback.onUnavailable", "xxx");
        doConnection();
    }

    @Override
    public void onLosing(Network network, int maxMsToLive) {
        PPApplication.logE("[IN_LISTENER] ----------- VPNNetworkCallback.onLosing", "xxx");
        doConnection();
    }

    @Override
    public void onAvailable(Network network) {
        //record vpn connect event
        PPApplication.logE("[IN_LISTENER] ----------- VPNNetworkCallback.onAvailable", "xxx");
        connected = true;
        doConnection();
    }

/*
    private void doConnection() {
//        PPApplication.logE("[TEST BATTERY] VPNNetworkCallback.doConnection", "xxx");
//        PPApplication.logE("[TEST BATTERY] VPNNetworkCallback.doConnection", "current thread="+Thread.currentThread());

        //final Context appContext = getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        //PPApplication.logE("[TEST BATTERY] VPNNetworkCallback.doConnection", "connected or disconnected");

        PPApplication.startHandlerThreadBroadcast();
        final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        handler.postDelayed(() -> {
//            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=VPNNetworkCallback.doConnection");

            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":VPNNetworkCallback_doConnection");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                _doConnection();

//                PPApplication.logE("PPApplication.startHandlerThread", "END run - from=VPNNetworkCallback.doConnection");

            } catch (Exception e) {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            } finally {
                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {
                    }
                }
            }
        }, 5000);
    }
*/

    private void doConnection() {
//        PPApplication.logE("[TEST BATTERY] VPNNetworkCallback.doConnection", "xxx");
//        PPApplication.logE("[TEST BATTERY] VPNNetworkCallback.doConnection", "current thread="+Thread.currentThread());

        //final Context appContext = getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        //PPApplication.logE("[TEST BATTERY] VPNNetworkCallback.doConnection", "connected or disconnected");

        if (Build.VERSION.SDK_INT >= 26) {
            // configured is PPApplication.handlerThreadBroadcast handler (see PhoneProfilesService.registerCallbacks()

//            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=VPNNetworkCallback.doConnection");

            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":VPNNetworkCallback_doConnection_1");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                _doConnection(context);

//               PPApplication.logE("PPApplication.startHandlerThread", "END run - from=VPNNetworkCallback.doConnection");

            } catch (Exception e) {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            } finally {
                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        else {
            final Context appContext = context;
            PPApplication.startHandlerThreadBroadcast();
            final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(
            //        appContext) {
            __handler.post(() -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=VPNNetworkCallback.doConnection");

                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":VPNNetworkCallback_doConnection_2");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        VPNNetworkCallback.this._doConnection(appContext);

//                    PPApplication.logE("PPApplication.startHandlerThread", "END run - from=VPNNetworkCallback.doConnection");

                    } catch (Exception e) {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplication.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                //}
            });
        }
    }

    private void _doConnection(Context appContext) {
//        PPApplication.logE("$$$ VPNNetworkCallback._doConnection", "connected=" + connected);

        if (Event.getGlobalEventsRunning()) {
            //if ((info.getState() == NetworkInfo.State.CONNECTED) ||
            //        (info.getState() == NetworkInfo.State.DISCONNECTED)) {

            //PPApplication.logE("$$$ VPNNetworkCallback._doConnection", "start HandleEvents - SENSOR_TYPE_VPN");

            // start events handler
            //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=VPNNetworkCallback._doConnection");

//            PPApplication.logE("[EVENTS_HANDLER_CALL] VPNNetworkCallback._doConnection", "sensorType=SENSOR_TYPE_RADIO_VPN");
            EventsHandler eventsHandler = new EventsHandler(appContext);
            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_VPN);

            //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=VPNNetworkCallback._doConnection");
        }
    }

}
