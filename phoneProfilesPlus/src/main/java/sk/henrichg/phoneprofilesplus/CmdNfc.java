package sk.henrichg.phoneprofilesplus;

import android.nfc.INfcAdapter;
import android.os.ServiceManager;
import android.util.Log;

/**
 * A shell executable for NTC toggle.
 */

public class CmdNfc {

    public static void main(String[] args) {
        if (!(run(Boolean.parseBoolean(args[0])))) {
            System.exit(1);
        }
    }

    private static boolean run(boolean enable) {
        return setNFC(enable);
    }

    // requires android.permission.WRITE_SECURE_SETTINGS
    static boolean setNFC(boolean enable) {
        try {
            INfcAdapter adapter = INfcAdapter.Stub.asInterface(ServiceManager.getService("nfc")); // service list | grep INfcAdapter
            return enable ? adapter.enable(PPApplication.PACKAGE_NAME) : adapter.disable(true, PPApplication.PACKAGE_NAME);
        } catch (Throwable e) {
            PPApplicationStatic.logException("CmdNfc.setNFC", Log.getStackTraceString(e));
            //PPApplicationStatic.recordException(e);
            return false;
        }
    }

}
