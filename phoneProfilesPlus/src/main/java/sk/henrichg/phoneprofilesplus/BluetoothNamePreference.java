package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;

import java.util.ArrayList;
import java.util.List;

public class BluetoothNamePreference extends DialogPreference {

    BluetoothNamePreferenceFragment fragment;

    private final Context context;

    String value;
    private String defaultValue;
    private boolean savedInstanceState;

    List<BluetoothDeviceData> bluetoothList;
    final List<BluetoothDeviceData> customBluetoothList;

    public BluetoothNamePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        bluetoothList = new ArrayList<>();
        customBluetoothList = new ArrayList<>();
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        value = getPersistedString((String)defaultValue);
        this.defaultValue = (String)defaultValue;
        setSummary();
    }

    void addBluetoothName(String bluetoothName) {
        String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
        boolean found = false;
        for (String _bluetoothName : splits) {
            if (_bluetoothName.equals(bluetoothName)) {
                found = true;
                break;
            }
        }
        if (!found) {
            if (!value.isEmpty())
                value = value + "|";
            value = value + bluetoothName;
        }
    }

    void removeBluetoothName(String bluetoothName) {
        String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
        value = "";
        StringBuilder _value = new StringBuilder();
        for (String _bluetoothName : splits) {
            if (!_bluetoothName.isEmpty()) {
                if (!_bluetoothName.equals(bluetoothName)) {
                    //if (!value.isEmpty())
                    //    value = value + "|";
                    //value = value + _bluetoothName;
                    if (_value.length() > 0)
                        _value.append("|");
                    _value.append(_bluetoothName);
                }
            }
        }
        value = _value.toString();
    }

    boolean isBluetoothNameSelected(String bluetoothName) {
        String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
        for (String _bluetoothName : splits) {
            if (_bluetoothName.equals(bluetoothName))
                return true;
        }
        return false;
    }

    void setLocationEnableStatus() {
        if (fragment != null)
            fragment.setLocationEnableStatus();
    }

    void refreshListView(@SuppressWarnings("SameParameterValue") boolean forRescan,
                         @SuppressWarnings("SameParameterValue") final String scrollToBTName)
    {
        if (fragment != null)
            fragment.refreshListView(forRescan, scrollToBTName);
    }

    void showEditMenu(View view, BluetoothDeviceData bluetoothDevice) {
        if (fragment != null)
            fragment.showEditMenu(view, bluetoothDevice);
    }

    private void setSummary() {
        String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
        for (String _bluetoothName : splits) {
            if (_bluetoothName.isEmpty()) {
                setSummary(R.string.applications_multiselect_summary_text_not_selected);
            } else if (splits.length == 1) {
                switch (value) {
                    case EventPreferencesBluetooth.ALL_BLUETOOTH_NAMES_VALUE:
                        setSummary("[" + StringConstants.CHAR_HARD_SPACE + context.getString(R.string.bluetooth_name_pref_dlg_all_bt_names_chb) + StringConstants.CHAR_HARD_SPACE + "]");
                        break;
                    case EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE:
                        setSummary("[" + StringConstants.CHAR_HARD_SPACE + context.getString(R.string.bluetooth_name_pref_dlg_configured_bt_names_chb) + StringConstants.CHAR_HARD_SPACE + "]");
                        break;
                    default:
                        setSummary(_bluetoothName);
                        break;
                }
            } else {
                String selectedBluetoothNames = context.getString(R.string.applications_multiselect_summary_text_selected);
                selectedBluetoothNames = selectedBluetoothNames + " " + splits.length;
                setSummary(selectedBluetoothNames);
                break;
            }
        }
    }

    void persistValue() {
        if (shouldPersist()) {
            if (callChangeListener(value))
            {
                setSummary();
                persistString(value);
            }
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            value = getPersistedString(defaultValue);
            setSummary();
        }
        savedInstanceState = false;
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final BluetoothNamePreference.SavedState myState = new BluetoothNamePreference.SavedState(superState);
        myState.value = value;
        myState.defaultValue = defaultValue;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if ((state == null) || (!state.getClass().equals(BluetoothNamePreference.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // restore instance state
        BluetoothNamePreference.SavedState myState = (BluetoothNamePreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;

        setSummary();
        //notifyChanged();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;
        String defaultValue;

        SavedState(Parcel source)
        {
            super(source);

            value = source.readString();
            defaultValue = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeString(value);
            dest.writeString(defaultValue);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<BluetoothNamePreference.SavedState> CREATOR =
                new Creator<>() {
                    public BluetoothNamePreference.SavedState createFromParcel(Parcel in)
                    {
                        return new BluetoothNamePreference.SavedState(in);
                    }
                    public BluetoothNamePreference.SavedState[] newArray(int size)
                    {
                        return new BluetoothNamePreference.SavedState[size];
                    }

                };

    }

}