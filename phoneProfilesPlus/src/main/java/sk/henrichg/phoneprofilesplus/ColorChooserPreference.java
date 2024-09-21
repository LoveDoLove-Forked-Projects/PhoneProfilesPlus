package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.graphics.ColorUtils;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

import com.kunzisoft.androidclearchroma.ChromaUtil;

public class ColorChooserPreference extends DialogPreference {

    ColorChooserPreferenceFragment fragment;

    //private FrameLayout widgetLayout;
    private AppCompatImageView colorPreview;

    String value;

    final Context context;

    final int[] mColors;

    public ColorChooserPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        //noinspection resource
        final TypedArray ta = context.getResources().obtainTypedArray(R.array.colorChooserDialog_colors);
        int length = ta.length();
        mColors = new int[length];
        for (int i = 0; i < length; i++) {
            mColors[i] = ta.getColor(i, 0);
        }
        ta.recycle();

        setWidgetLayoutResource(R.layout.preference_widget_color_chooser_preference); // resource na layout custom preference - TextView-ImageView

        setPositiveButtonText(null);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);

        colorPreview = (AppCompatImageView)holder.findViewById(R.id.dialog_color_chooser_pref_color);

        setColorInWidget();
    }

    void setBackgroundCompat(View view, Drawable d) {
        view.setBackground(d);
    }

    private void setColorInWidget() {

        int color = parseValue(value);

        colorPreview.setImageResource(R.drawable.acch_circle);

        // Update color
        String applicationTheme = ApplicationPreferences.applicationTheme(context, true);
        boolean nightModeOn = !applicationTheme.equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_WHITE);
        if (!isEnabled()) {
            color = ColorUtils.setAlphaComponent(color, 89);
        }
        if (nightModeOn) {
            colorPreview.getDrawable()
                    .setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.ADD));
        }
        else {
            colorPreview.getDrawable()
                    .setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
        }

        colorPreview.invalidate();
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        value = getPersistedString((String) defaultValue);
        setSummaryCCHP(value);
    }

    void persistValue() {
        if (callChangeListener(value))
        {
            persistString(value);
            setColorInWidget();
            setSummaryCCHP(value);
        }
    }

    private void setSummaryCCHP(String value)
    {
        int color = parseValue(value);
        setSummary(ChromaUtil.getFormattedColorString(color, false));
    }

    int shiftColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.9f; // value component
        return Color.HSVToColor(hsv);
    }

    Drawable createSelector(int color) {
        /*int position = -1;
        for (int i = 0; i < mColors.length; i++) {
            if (mColors[i] == color) {
                position = i;
                break;
            }
        }*/

        String applicationTheme = ApplicationPreferences.applicationTheme(context, true);
        /*if (GlobalGUIRoutines.isNightModeEnabled(context.getApplicationContext()))
            applicationTheme = "dark";
        else
            applicationTheme = "white";*/
        /*int nightModeFlags =
                context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                applicationTheme = "dark";
                break;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                applicationTheme = "white";
                break;
        }*/

//        final String COLOR1 = "#6E6E6E";
//        final String COLOR2 = "#AEAEAE";

        GradientDrawable coloredCircle = new GradientDrawable();
        coloredCircle.setColor(color);
        coloredCircle.setShape(GradientDrawable.OVAL);
        //noinspection IfStatementWithIdenticalBranches
        if (applicationTheme.equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_WHITE)) {
            //if (position == 2) // dark gray color
            //    coloredCircle.setStroke(2, Color.parseColor("#6E6E6E"));
            //else
//                coloredCircle.setStroke(1, Color.parseColor(COLOR1));
            coloredCircle.setStroke(1, context.getColor(R.color.pppColorChooserColor1));
        }
        else {
            //if (position == 0) // white color
            //    coloredCircle.setStroke(2, Color.parseColor("#AEAEAE"));
            //else
//                coloredCircle.setStroke(1, Color.parseColor(COLOR1));
            coloredCircle.setStroke(1, context.getColor(R.color.pppColorChooserColor1));
        }

        GradientDrawable darkerCircle = new GradientDrawable();
        darkerCircle.setColor(shiftColor(color));
        darkerCircle.setShape(GradientDrawable.OVAL);
        if (applicationTheme.equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_WHITE)) {
            //if (position == 2) // dark gray color
            //    coloredCircle.setStroke(2, Color.parseColor("#6E6E6E"));
            //else
//                coloredCircle.setStroke(2, Color.parseColor(COLOR1));
            coloredCircle.setStroke(2, context.getColor(R.color.pppColorChooserColor1));
        }
        else {
            //if (position == 0) // white color
            //    darkerCircle.setStroke(2, Color.parseColor("#AEAEAE"));
            //else
//                darkerCircle.setStroke(2, Color.parseColor(COLOR2));
            darkerCircle.setStroke(2, context.getColor(R.color.pppColorChooserColor2));
        }

        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{-android.R.attr.state_pressed}, coloredCircle);
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, darkerCircle);
        return stateListDrawable;
    }


    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final ColorChooserPreference.SavedState myState = new ColorChooserPreference.SavedState(superState);
        myState.value = value;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if ((state == null) || (!state.getClass().equals(ColorChooserPreference.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // restore instance state
        ColorChooserPreference.SavedState myState = (ColorChooserPreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        setSummaryCCHP(value);
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;

        SavedState(Parcel source)
        {
            super(source);

            // restore profileId
            value = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save profileId
            dest.writeString(value);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<ColorChooserPreference.SavedState> CREATOR =
                new Creator<ColorChooserPreference.SavedState>() {
                    public ColorChooserPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new ColorChooserPreference.SavedState(in);
                    }
                    public ColorChooserPreference.SavedState[] newArray(int size)
                    {
                        return new ColorChooserPreference.SavedState[size];
                    }

                };

    }

    static int parseValue(String value) {
        long color;
        if (value.startsWith("#"))
            color = Long.decode("0x" + value.substring(1));
        else
            color = Long.parseLong(value);
        return (int) color;
    }
}