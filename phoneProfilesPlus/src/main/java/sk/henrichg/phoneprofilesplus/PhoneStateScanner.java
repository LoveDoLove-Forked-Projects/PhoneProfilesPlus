package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.POWER_SERVICE;

class PhoneStateScanner extends PhoneStateListener {

    private final Context context;
    private final TelephonyManager telephonyManager;
    //private TelephonyManager telephonyManager2 = null;

    int registeredCell = Integer.MAX_VALUE;
    long lastConnectedTime = 0;

    static boolean forceStart = false;

    static boolean enabledAutoRegistration = false;
    static int durationForAutoRegistration = 0;
    static String cellsNameForAutoRegistration = "";

    static MobileCellsRegistrationService autoRegistrationService = null;

    //static String ACTION_PHONE_STATE_CHANGED = "sk.henrichg.phoneprofilesplus.ACTION_PHONE_STATE_CHANGED";

    PhoneStateScanner(Context context) {
        PPApplication.logE("PhoneStateScanner.constructor", "xxx");
        this.context = context;
        /*if (Build.VERSION.SDK_INT >= 24) {
            TelephonyManager telephonyManager = (TelephonyManager) this.context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                SubscriptionManager mSubscriptionManager = SubscriptionManager.from(context);
                // Loop through the subscription list i.e. SIM list.
                List<SubscriptionInfo> subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                if (subscriptionList != null) {
                    for (int i = 0; i < mSubscriptionManager.getActiveSubscriptionInfoCountMax(); i++) {
                        SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                        int subscriptionId = subscriptionInfo.getSubscriptionId();
                        if (telephonyManager1 == null)
                            telephonyManager1 = telephonyManager.createForSubscriptionId(subscriptionId);
                        if (telephonyManager2 == null)
                            telephonyManager2 = telephonyManager.createForSubscriptionId(subscriptionId);
                    }
                } else
                    telephonyManager1 = (TelephonyManager) this.context.getSystemService(Context.TELEPHONY_SERVICE);
            }
        }
        else {*/
            telephonyManager = (TelephonyManager) this.context.getSystemService(Context.TELEPHONY_SERVICE);
        //}
        MobileCellsRegistrationService.getMobileCellsAutoRegistration(context);
    }

    @SuppressLint("InlinedApi")
    void connect() {
        PPApplication.logE("PhoneStateScanner.connect", "xxx");
        boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
        if (/*PPApplication.*/isPowerSaveMode && ApplicationPreferences.applicationEventMobileCellsScanInPowerSaveMode(context).equals("2"))
            // start scanning in power save mode is not allowed
            return;

        if ((telephonyManager != null) &&
                PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY) &&
                Permissions.checkLocation(context.getApplicationContext())) {
            PPApplication.logE("PhoneStateScanner.connect", "telelphonyManager.listen");
            telephonyManager.listen(this,
                    //  PhoneStateListener.LISTEN_CALL_STATE
                    PhoneStateListener.LISTEN_CELL_INFO // Requires API 17
                            | PhoneStateListener.LISTEN_CELL_LOCATION
                            //| PhoneStateListener.LISTEN_DATA_ACTIVITY
                            //| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                            | PhoneStateListener.LISTEN_SERVICE_STATE
                    //| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                    //| PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
                    //| PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR
            );
            /*if ((telephonyManager2 != null) &&
                    context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY) &&
                    Permissions.checkLocation(context.getApplicationContext()))
                telephonyManager2.listen(this,
                    //  PhoneStateListener.LISTEN_CALL_STATE
                        PhoneStateListener.LISTEN_CELL_INFO // Requires API 17
                      | PhoneStateListener.LISTEN_CELL_LOCATION
                    //| PhoneStateListener.LISTEN_DATA_ACTIVITY
                    //| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                      | PhoneStateListener.LISTEN_SERVICE_STATE
                    //| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                    //| PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
                    //| PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR
                );*/
        }
        startAutoRegistration(context);
    }

    void disconnect() {
        PPApplication.logE("PhoneStateScanner.disconnect", "xxx");
        if ((telephonyManager != null) && PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY))
            telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);
        /*if ((telephonyManager2 != null) && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
            telephonyManager2.listen(this, PhoneStateListener.LISTEN_NONE);*/
        stopAutoRegistration(context);
    }

    /*
    void resetListening(boolean oldPowerSaveMode, boolean forceReset) {
        if ((forceReset) || (PPApplication.isPowerSaveMode != oldPowerSaveMode)) {
            disconnect();
            connect();
        }
    }
    */

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void getAllCellInfo(List<CellInfo> cellInfo) {
        // only for registered cells is returned identify
        // SlimKat in Galaxy Nexus - returns null :-/
        // Honor 7 - returns empty list (not null), Dual SIM?

        PPApplication.logE("PhoneStateScanner.getAllCellInfo", "cellInfo="+cellInfo);

        if (cellInfo!=null) {

            if (Permissions.checkLocation(context.getApplicationContext())) {

                PPApplication.logE("PhoneStateScanner.getAllCellInfo", "---- start ----------------------------");

                for (CellInfo _cellInfo : cellInfo) {
                    PPApplication.logE("PhoneStateScanner.getAllCellInfo", "registered="+_cellInfo.isRegistered());

                    if (_cellInfo instanceof CellInfoGsm) {
                        PPApplication.logE("PhoneStateScanner.getAllCellInfo", "gsm info="+_cellInfo);
                        CellIdentityGsm identityGsm = ((CellInfoGsm) _cellInfo).getCellIdentity();
                        if (identityGsm.getCid() != Integer.MAX_VALUE) {
                            PPApplication.logE("PhoneStateScanner.getAllCellInfo", "gsm mCid="+identityGsm.getCid());
                            if (_cellInfo.isRegistered()) {
                                registeredCell = identityGsm.getCid();
                                lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                            }
                            doAutoRegistration(identityGsm.getCid());
                        }
                    } else if (_cellInfo instanceof CellInfoLte) {
                        PPApplication.logE("PhoneStateScanner.getAllCellInfo", "lte info="+_cellInfo);
                        CellIdentityLte identityLte = ((CellInfoLte) _cellInfo).getCellIdentity();
                        if (identityLte.getCi() != Integer.MAX_VALUE) {
                            PPApplication.logE("PhoneStateScanner.getAllCellInfo", "lte mCi="+identityLte.getCi());
                            if (_cellInfo.isRegistered()) {
                                registeredCell = identityLte.getCi();
                                lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                            }
                            doAutoRegistration(identityLte.getCi());
                        }
                    } else if (_cellInfo instanceof CellInfoWcdma) {
                        PPApplication.logE("PhoneStateScanner.getAllCellInfo", "wcdma info="+_cellInfo);
                        CellIdentityWcdma identityWcdma = ((CellInfoWcdma) _cellInfo).getCellIdentity();
                        if (identityWcdma.getCid() != Integer.MAX_VALUE) {
                            PPApplication.logE("PhoneStateScanner.getAllCellInfo", "wcdma mCid=" + identityWcdma.getCid());
                            if (_cellInfo.isRegistered()) {
                                registeredCell = identityWcdma.getCid();
                                lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                            }
                            doAutoRegistration(identityWcdma.getCid());
                        }
                    } else if (_cellInfo instanceof CellInfoCdma) {
                        PPApplication.logE("PhoneStateScanner.getAllCellInfo", "cdma info="+_cellInfo);
                        CellIdentityCdma identityCdma = ((CellInfoCdma) _cellInfo).getCellIdentity();
                        if (identityCdma.getBasestationId() != Integer.MAX_VALUE) {
                            PPApplication.logE("PhoneStateScanner.getAllCellInfo", "wcdma mCid="+identityCdma.getBasestationId());
                            if (_cellInfo.isRegistered()) {
                                registeredCell = identityCdma.getBasestationId();
                                lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                            }
                            doAutoRegistration(identityCdma.getBasestationId());
                        }
                    }
                    else {
                        PPApplication.logE("PhoneStateScanner.getAllCellInfo", "unknown info="+_cellInfo);
                    }
                }

                PPApplication.logE("PhoneStateScanner.getAllCellInfo", "---- end ----------------------------");

                PPApplication.logE("PhoneStateScanner.getAllCellInfo", "registeredCell=" + registeredCell);
            }

        }
        else
            PPApplication.logE("PhoneStateScanner.getAllCellInfo", "cell info is null");
    }

    @SuppressLint("MissingPermission")
    private void getAllCellInfo() {
        if (telephonyManager != null) {
            List<CellInfo> cellInfo = null;
            if (Permissions.checkLocation(context.getApplicationContext()))
                cellInfo = telephonyManager.getAllCellInfo();
            PPApplication.logE("PhoneStateScanner.getAllCellInfo.2", "cellInfo="+cellInfo);
            getAllCellInfo(cellInfo);
        }
    }

    @Override
    public void onCellInfoChanged(List<CellInfo> cellInfo)
    {
        super.onCellInfoChanged(cellInfo);

        PPApplication.logE("PhoneStateScanner.onCellInfoChanged", "telephonyManager="+telephonyManager);
        CallsCounter.logCounter(context, "PhoneStateScanner.onCellInfoChanged", "PhoneStateScanner_onCellInfoChanged");

        if (cellInfo == null)
            getAllCellInfo();
        else
            getAllCellInfo(cellInfo);

        if (registeredCell != Integer.MAX_VALUE) {
            DatabaseHandler db = DatabaseHandler.getInstance(context);
            db.updateMobileCellLastConnectedTime(registeredCell, lastConnectedTime);
        }

        doAutoRegistration(registeredCell);
        handleEvents();
    }

    @Override
    public void onServiceStateChanged (ServiceState serviceState) {
        super.onServiceStateChanged(serviceState);

        PPApplication.logE("PhoneStateScanner.onServiceStateChanged", "telephonyManager=" + telephonyManager);
        CallsCounter.logCounter(context, "PhoneStateScanner.onServiceStateChanged", "PhoneStateScanner_onServiceStateChanged");

        getRegisteredCell();
        PPApplication.logE("PhoneStateScanner.onServiceStateChanged", "registeredCell=" + registeredCell);

        if (registeredCell != Integer.MAX_VALUE) {
            DatabaseHandler db = DatabaseHandler.getInstance(context);
            db.updateMobileCellLastConnectedTime(registeredCell, lastConnectedTime);
        }

        doAutoRegistration(registeredCell);
        handleEvents();
    }

    private void getCellLocation(CellLocation location) {
        PPApplication.logE("PhoneStateScanner.getCellLocation", "location="+location);

        if (location!=null) {

            if (Permissions.checkLocation(context.getApplicationContext())) {

                if (location instanceof GsmCellLocation) {
                    GsmCellLocation gcLoc = (GsmCellLocation) location;
                    PPApplication.logE("PhoneStateScanner.getCellLocation", "gsm location="+gcLoc);
                    if (gcLoc.getCid() != -1) {
                        PPApplication.logE("PhoneStateScanner.getCellLocation", "gsm mCid="+gcLoc.getCid());
                        registeredCell = gcLoc.getCid();
                        lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                    }
                } else if (location instanceof CdmaCellLocation) {
                    CdmaCellLocation ccLoc = (CdmaCellLocation) location;
                    PPApplication.logE("PhoneStateScanner.getCellLocation", "cdma location="+ccLoc);
                    if (ccLoc.getBaseStationId() != -1) {
                        PPApplication.logE("PhoneStateScanner.getCellLocation", "cdma mCid="+ccLoc.getBaseStationId());
                        registeredCell = ccLoc.getBaseStationId();
                        lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                    }
                }
                else {
                    PPApplication.logE("PhoneStateScanner.getCellLocation", "unknown location="+location);
                }

                PPApplication.logE("PhoneStateScanner.getCellLocation", "registeredCell=" + registeredCell);

            }

        }
        else
            PPApplication.logE("PhoneStateScanner.getCellLocation", "location is null");
    }

    @SuppressLint("MissingPermission")
    private void getCellLocation() {
        if (telephonyManager != null) {
            CellLocation location = null;
            if (Permissions.checkLocation(context.getApplicationContext()))
                location = telephonyManager.getCellLocation();
            PPApplication.logE("PhoneStateScanner.getCellLocation.2", "location="+location);
            getCellLocation(location);
        }
    }

    @Override
    public void onCellLocationChanged (CellLocation location) {
        super.onCellLocationChanged(location);

        PPApplication.logE("PhoneStateScanner.onCellLocationChanged", "telephonyManager="+telephonyManager);
        CallsCounter.logCounter(context, "PhoneStateScanner.onCellLocationChanged", "PhoneStateScanner_onCellLocationChanged");

        if (location == null)
            getCellLocation();
        else
            getCellLocation(location);

        PPApplication.logE("PhoneStateScanner.onCellLocationChanged", "location="+location);
        PPApplication.logE("PhoneStateScanner.onCellLocationChanged", "registeredCell="+registeredCell);

        if (registeredCell != Integer.MAX_VALUE) {
            DatabaseHandler db = DatabaseHandler.getInstance(context);
            db.updateMobileCellLastConnectedTime(registeredCell, lastConnectedTime);
        }

        doAutoRegistration(registeredCell);
        handleEvents();
    }

    /*
    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength)
    {
        super.onSignalStrengthsChanged(signalStrength);

        signal = signalStrength.getGsmSignalStrength()*2-113;
    }
    */

    void getRegisteredCell() {
        PPApplication.logE("PhoneStateScanner.getRegisteredCell", "xxx");
        getAllCellInfo();
        getCellLocation();
    }

    void rescanMobileCells() {
        PPApplication.logE("PhoneStateScanner.rescanMobileCells", "xxx");
        if (ApplicationPreferences.applicationEventMobileCellEnableScannig(context.getApplicationContext()) || PhoneStateScanner.forceStart) {
            PPApplication.logE("PhoneStateScanner.rescanMobileCells", "-----");
            getRegisteredCell();
            doAutoRegistration(registeredCell);
            handleEvents();
        }
    }

    private void handleEvents() {
        PPApplication.logE("PhoneStateScanner.handleEvents", "xxx");
        //PhoneStateJob.start(context);
        if (Event.getGlobalEventsRunning(context))
        {
            final Context appContext = context.getApplicationContext();
            PPApplication.startHandlerThread("PhoneStateScanner.sendBroadcast");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PhoneStateScanner.sendBroadcast");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    DataWrapper dataWrapper = new DataWrapper(appContext, false, 0);
                    if (DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS, false) > 0) {
                        PPApplication.logE("PhoneStateScanner.handleEvents", "start events handler");
                        // start events handler
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_PHONE_STATE/*, false*/);
                    }
                    dataWrapper.invalidateDataWrapper();

                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            });
        }

        /*
        // broadcast for cells editor
        Intent intent = new Intent("PhoneStateChangedBroadcastReceiver_preference");
        //intent.putExtra("state", mode);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        */
    }

    private void doAutoRegistration(final int cellId) {
        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        PPApplication.logE("PhoneStateScanner.doAutoRegistration", "enabledAutoRegistration="+enabledAutoRegistration);
        if (enabledAutoRegistration) {
            PPApplication.startHandlerThreadMobileCells();
            final Handler handler = new Handler(PPApplication.handlerThreadMobileCells.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PhoneStateScanner.doAutoRegistration");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    //Log.d("PhoneStateScanner.doAutoRegistration", "xxx");
                    List<MobileCellsData> localCellsList = new ArrayList<>();
                    if (cellId != Integer.MAX_VALUE)
                        localCellsList.add(new MobileCellsData(registeredCell, cellsNameForAutoRegistration, true, false, lastConnectedTime));
                    DatabaseHandler db = DatabaseHandler.getInstance(context);
                    db.saveMobileCellsList(localCellsList, true, true);

                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            });
        }
    }

    static void startAutoRegistration(Context context) {
        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        MobileCellsRegistrationService.getMobileCellsAutoRegistration(context);

        PPApplication.logE("PhoneStateScanner.startAutoRegistration", "enabledAutoRegistration="+enabledAutoRegistration);

        if (enabledAutoRegistration) {
            //Log.d("PhoneStateScanner.startAutoRegistration","xxx");
            stopAutoRegistration(context);
            try {
                Intent serviceIntent = new Intent(context.getApplicationContext(), MobileCellsRegistrationService.class);
                PPApplication.startPPService(context, serviceIntent);
            } catch (Exception ignored) {}
        }
    }

    static void stopAutoRegistration(Context context) {
        if (autoRegistrationService != null) {
            PPApplication.logE("PhoneStateScanner.stopAutoRegistration", "xxx");
            context.stopService(new Intent(context.getApplicationContext(), MobileCellsRegistrationService.class));
            autoRegistrationService = null;
        }
    }
}
