package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.TooltipCompat;
import androidx.preference.PreferenceDialogFragmentCompat;

import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialog;

public class TimeDialogPreferenceFragment extends PreferenceDialogFragmentCompat
                                implements SeekBar.OnSeekBarChangeListener {

    private TextView mValue;
    private SeekBar mSeekBarHours;
    private SeekBar mSeekBarMinutes;
    private TimeDurationPickerDialog mValueDialog;

    private Context prefContext;
    private TimeDialogPreference preference;

    @Override
    protected void onPrepareDialogBuilder(@NonNull AlertDialog.Builder builder) {
        GlobalGUIRoutines.setCustomDialogTitle(preference.getContext(), builder, false,
                preference.getDialogTitle(), null);
    }

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        this.prefContext = context;
        preference = (TimeDialogPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_time_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view)
    {
        super.onBindDialogView(view);

        mValue = view.findViewById(R.id.time_pref_dlg_value);
        //noinspection DataFlowIssue
        TooltipCompat.setTooltipText(mValue, getString(R.string.time_pref_dlg_edit_time_tooltip));
        mSeekBarHours = view.findViewById(R.id.time_pref_dlg_hours);
        mSeekBarMinutes = view.findViewById(R.id.time_pref_dlg_minutes);

        //mSeekBarHours.setRotation(180);
        //mSeekBarMinutes.setRotation(180);

        // Initialize state
        int hours;
        int minutes;
        hours = preference.mMax / 60;
        minutes = (preference.mMax % 60);
        mSeekBarHours.setMax(hours);
        if (hours == 0)
            mSeekBarMinutes.setMax(minutes);
        else
            mSeekBarMinutes.setMax(59);
        int iValue = preference.value;
        hours = iValue / 60;
        minutes = (iValue % 60);
        mSeekBarHours.setProgress(hours);
        mSeekBarMinutes.setProgress(minutes);

        mValue.setText(StringFormatUtils.getTimeString(iValue));

        mValueDialog = new TimeDurationPickerDialog(prefContext, (view1, duration) -> {
            int iValue1 = (int) duration / 1000 / 60;

            if (iValue1 < preference.mMin)
                iValue1 = preference.mMin;
            if (iValue1 > preference.mMax)
                iValue1 = preference.mMax;

            preference.value = iValue1;

            mValue.setText(StringFormatUtils.getTimeString(iValue1));

            int hours1 = iValue1 / 60;
            int minutes1 = (iValue1 % 60);

            mSeekBarHours.setProgress(hours1);
            mSeekBarMinutes.setProgress(minutes1);
        }, (long) iValue * 60 * 1000, TimeDurationPicker.HH_MM);
        GlobalGUIRoutines.setThemeTimeDurationPickerDisplay(mValueDialog.getDurationInput(), prefContext);
        mValue.setOnClickListener(view12 -> {
                int hours12 = mSeekBarHours.getProgress();
                int minutes12 = mSeekBarMinutes.getProgress();

                int iValue12 = (hours12 * 60 + minutes12);
                if (iValue12 < preference.mMin) iValue12 = preference.mMin;
                if (iValue12 > preference.mMax) iValue12 = preference.mMax;

                mValueDialog.setDuration((long) iValue12 * 60 * 1000);
                if (getActivity() != null)
                    if (!getActivity().isFinishing())
                        mValueDialog.show();
            }
        );

        mSeekBarHours.setOnSeekBarChangeListener(this);
        mSeekBarMinutes.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            int hours = mSeekBarHours.getProgress();
            int minutes = mSeekBarMinutes.getProgress();

            int iValue = (hours * 60 + minutes);
            if (iValue < preference.mMin) iValue = preference.mMin;
            if (iValue > preference.mMax) iValue = preference.mMax;

            preference.value = iValue;

            if (preference.callChangeListener(preference.value)) {
                preference.persistValue(preference.value);
                preference.setSummaryTDP();
            }
        }
        else {
            preference.resetSummary();
        }

        preference.fragment = null;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            int hours = mSeekBarHours.getProgress();
            int minutes = mSeekBarMinutes.getProgress();

            int iValue = (hours * 60 + minutes);
            if (iValue < preference.mMin) iValue = preference.mMin;
            if (iValue > preference.mMax) iValue = preference.mMax;

            preference.value = iValue;

            mValue.setText(StringFormatUtils.getTimeString(iValue));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
