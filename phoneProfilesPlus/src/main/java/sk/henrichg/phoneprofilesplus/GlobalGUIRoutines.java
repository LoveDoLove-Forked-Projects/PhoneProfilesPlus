package sk.henrichg.phoneprofilesplus;

import static android.os.Looper.getMainLooper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.DynamicColors;

import java.lang.ref.WeakReference;
import java.util.List;

import mobi.upod.timedurationpicker.TimeDurationPicker;

class GlobalGUIRoutines {

    static final int ICON_SIZE_DP = 50;

    static final String OPAQUENESS_LIGHTNESS_0 = "0";
    static final String OPAQUENESS_LIGHTNESS_12 = "12";
    static final String OPAQUENESS_LIGHTNESS_25 = "25";
    static final String OPAQUENESS_LIGHTNESS_37 = "37";
    static final String OPAQUENESS_LIGHTNESS_50 = "50";
    static final String OPAQUENESS_LIGHTNESS_62 = "62";
    static final String OPAQUENESS_LIGHTNESS_75 = "75";
    static final String OPAQUENESS_LIGHTNESS_87 = "87";
    static final String OPAQUENESS_LIGHTNESS_100 = "100";

    private GlobalGUIRoutines() {
        // private constructor to prevent instantiation
    }

    static void setTheme(Activity activity, boolean forPopup,
                                boolean withToolbar,
                                /*boolean forEditor,*/ boolean forActivator, boolean forDialog,
                                boolean forLocationEditor, boolean forPreference)
    {
        int theme = getTheme(forPopup, withToolbar,
                /*forEditor,*/ forActivator,
                forDialog, forLocationEditor, forPreference,
                activity);
        if (theme != 0)
            activity.setTheme(theme);
    }

    static int getTheme(boolean forPopup, boolean withToolbar,
                        /*boolean forEditor,*/ boolean forActivator, boolean forDialog,
                        boolean forLocationEditor, boolean forPreferences,
                        Context context) {
        // !!! this must be called
        /*String applicationTheme =*/ ApplicationPreferences.applicationTheme(context, true);
//        if (forEditor)
//            Log.e("GlobalGUIRoutines.getTheme", "applicationTheme="+applicationTheme);

        int miuiVersion = -1;
        if (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) {
//            Log.e("GlobalGUIRoutines.getTheme", "Build.VERSION.INCREMENTAL="+Build.VERSION.INCREMENTAL);
            String[] splits = Build.VERSION.INCREMENTAL.split("\\.");
//            Log.e("GlobalGUIRoutines.getTheme", "splits[0]="+splits[0]);
            try {
                miuiVersion = Integer.parseInt(splits[0].substring(1));
//                Log.e("GlobalGUIRoutines.getTheme", "miuiVersion="+miuiVersion);
            }
            catch (Exception ignored) {}
        }

        if (forActivator) {
            if (PPApplication.deviceIsOnePlus) {
                if (Build.VERSION.SDK_INT >= 33)
                    return R.style.ActivatorTheme_dayNight;
                else
                    return R.style.ActivatorTheme_dayNight_noRipple;
            }
            else
            if (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI && miuiVersion >= 14) {
                    return R.style.ActivatorTheme_dayNight_noRipple;
            }
            else {
                if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy &&
                    (Build.VERSION.SDK_INT >= 33))
                    return R.style.ActivatorTheme_dayNight_samsung;
                else
                    return R.style.ActivatorTheme_dayNight;
            }
        }
        else
        if (forDialog) {
            if (PPApplication.deviceIsOnePlus) {
                if (Build.VERSION.SDK_INT >= 33)
                    return R.style.DialogTheme_dayNight;
                else
                    return R.style.DialogTheme_dayNight_noRipple;
            }
            else
            if (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI && miuiVersion >= 14) {
                return R.style.DialogTheme_dayNight_noRipple;
            }
            else {
                if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy &&
                        (Build.VERSION.SDK_INT >= 33))
                    return R.style.DialogTheme_dayNight_samsung;
                else
                    return R.style.DialogTheme_dayNight;
            }
        }
        else
        if (forLocationEditor) {
            if (PPApplication.deviceIsOnePlus) {
                if (Build.VERSION.SDK_INT >= 33)
                    return R.style.Theme_PhoneProfilesTheme_locationeditor_dayNight;
                else
                    return R.style.Theme_PhoneProfilesTheme_locationeditor_dayNight_noRipple;
            }
            else
            if (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI && miuiVersion >= 14) {
                return R.style.Theme_PhoneProfilesTheme_locationeditor_dayNight_noRipple;
            }
            else
                return R.style.Theme_PhoneProfilesTheme_locationeditor_dayNight;
        }
        else
        if (forPreferences) {
            if (PPApplication.deviceIsOnePlus) {
                if (Build.VERSION.SDK_INT >= 33)
                    return R.style.Theme_PhoneProfilesTheme_preferences_dayNight;
                else
                    return R.style.Theme_PhoneProfilesTheme_preferences_dayNight_noRipple;
            }
            else
            if (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI && miuiVersion >= 14) {
                return R.style.Theme_PhoneProfilesTheme_preferences_dayNight_noRipple;
            }
            else
                return R.style.Theme_PhoneProfilesTheme_preferences_dayNight;
        }
        else
        if (forPopup) {
            if (PPApplication.deviceIsOnePlus) {
                if (Build.VERSION.SDK_INT >= 33) {
                    if (withToolbar)
                        return R.style.PopupTheme_withToolbar_dayNight;
                    else
                        return R.style.PopupTheme_dayNight;
                } else {
                    if (withToolbar)
                        return R.style.PopupTheme_withToolbar_dayNight_noRipple;
                    else
                        return R.style.PopupTheme_dayNight_noRipple;
                }
            }
            else
            if (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI && miuiVersion >= 14) {
                if (withToolbar)
                    return R.style.PopupTheme_withToolbar_dayNight_noRipple;
                else
                    return R.style.PopupTheme_dayNight_noRipple;
            }
            else {
                if (withToolbar)
                    return R.style.PopupTheme_withToolbar_dayNight;
                else
                    return R.style.PopupTheme_dayNight;
            }
        }
        else {
            if (PPApplication.deviceIsOnePlus) {
                if (Build.VERSION.SDK_INT >= 33) {
                    if (withToolbar) {
                        return R.style.Theme_PhoneProfilesTheme_withToolbar_dayNight;
                    } else
                        return R.style.Theme_PhoneProfilesTheme_dayNight;
                } else {
                    if (withToolbar) {
                        return R.style.Theme_PhoneProfilesTheme_withToolbar_dayNight_noRipple;
                    } else
                        return R.style.Theme_PhoneProfilesTheme_dayNight_noRipple;
                }
            }
            else
            if (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI && miuiVersion >= 14) {
                if (withToolbar) {
                    return R.style.Theme_PhoneProfilesTheme_withToolbar_dayNight_noRipple;
                } else
                    return R.style.Theme_PhoneProfilesTheme_dayNight_noRipple;
            }
            else {
                if (withToolbar) {
                    return R.style.Theme_PhoneProfilesTheme_withToolbar_dayNight;
                } else
                    return R.style.Theme_PhoneProfilesTheme_dayNight;
            }
        }
    }

    static boolean isNightModeEnabled(Context appContext) {
        if (Build.VERSION.SDK_INT >= 30)
            return appContext.getResources().getConfiguration().isNightModeActive();

        int nightModeFlags =
                appContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            return true;
        }
        return false;
    }

    private static void switchNightMode(Context appContext) {
        switch (ApplicationPreferences.applicationTheme(appContext, false)) {
            case ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_WHITE:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_NIGHT_MODE:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    static void switchNightMode(final Context appContext, boolean useMainLooperHandler) {
        if (useMainLooperHandler) {
            final Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
//                PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=GlobalGUIRoutines.switchNightMode");
                try {
                    switchNightMode(appContext);
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            });
        }
        else
            switchNightMode(appContext);
    }

    static void reloadActivity(Activity activity, boolean newIntent)
    {
        if (activity == null)
            return;

        if (newIntent)
        {
            final Handler handler = new Handler(activity.getMainLooper());
            final WeakReference<Activity> activityWeakRef = new WeakReference<>(activity);
            handler.post(() -> {
                try {
//                        PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=GlobalGUIRoutines.reloadActivity");
                    Activity _activity = activityWeakRef.get();
                    if (_activity == null)
                        return;

                    Context context = _activity.getApplicationContext();

                    Intent intent = _activity.getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);

                    _activity.finish();
                    _activity.overridePendingTransition(0, 0);

                    context.startActivity(intent);
                    //activity.overridePendingTransition(0, 0);
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            });
        }
        else
            activity.recreate();
    }

    static void setPreferenceTitleStyleX(androidx.preference.Preference preference, boolean enabled,
                                         boolean bold, boolean addArrows,
                                         boolean underline, boolean errorColor, boolean forceErrorColor)
    {
        if (preference != null) {
            CharSequence title = preference.getTitle();
            if (title != null) {
                // remove bullet
                String s = title.toString();
                title = s;
                if (s.startsWith(StringConstants.CHAR_BULLET +" "))
                    title = TextUtils.replace(title, new String[]{StringConstants.CHAR_BULLET +" "}, new CharSequence[]{""});

                // remove arrows
                if (s.startsWith(StringConstants.CHAR_ARROW +" "))
                    title = TextUtils.replace(title, new String[]{StringConstants.CHAR_ARROW +" "}, new CharSequence[]{""});

                // remove underline
                //s = title.toString();
                //title = s;
                //if (s.startsWith("[!] "))
                //    title = TextUtils.replace(title, new String[]{"[!] "}, new CharSequence[]{""});

                //if (underline)
                //    title = TextUtils.concat("[!] ", title);
                if (bold)
                    title = TextUtils.concat(StringConstants.CHAR_BULLET +" ", title);
                else
                if (addArrows)
                    title = TextUtils.concat(StringConstants.CHAR_ARROW +" ", title);

                //}
                Spannable sbt = new SpannableString(title);
                /*Object[] spansToRemove = sbt.getSpans(0, title.length(), Object.class);
                for (Object span : spansToRemove) {
                    if (span instanceof CharacterStyle)
                        sbt.removeSpan(span);
                }*/
                if (bold || underline || forceErrorColor) {
                    if (bold) {
                        sbt.setSpan(new StyleSpan(Typeface.BOLD), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        //sbt.setSpan(new RelativeSizeSpan(1.05f), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    else {
                        sbt.setSpan(new StyleSpan(Typeface.NORMAL), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    if (underline) {
                        if (bold)
                            sbt.setSpan(new UnderlineSpan(), 2, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        else
                            sbt.setSpan(new UnderlineSpan(), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
//                    if (forceErrorColor) {
//                        Log.e("GlobalGUIRoutines.setPreferenceTitleStyleX", "errorColor="+errorColor);
//                        Log.e("GlobalGUIRoutines.setPreferenceTitleStyleX", "enabled="+enabled);
//                    }
                    if (errorColor && enabled)
                        sbt.setSpan(new ForegroundColorSpan(ContextCompat.getColor(preference.getContext(), R.color.error_color)), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                preference.setTitle(sbt);
            }
        }
    }

    static void setImageButtonEnabled(boolean enabled, AppCompatImageButton item, /*int iconResId,*/ Context context) {
        item.setEnabled(enabled);
        //Drawable originalIcon = ContextCompat.getDrawable(context, iconResId);
        //Drawable icon = enabled ? originalIcon : convertDrawableToGrayScale(originalIcon);
        //item.setImageDrawable(icon);
        if (enabled)
            item.setColorFilter(null);
        else
            item.setColorFilter(context.getColor(R.color.activityDisabledTextColor), PorterDuff.Mode.SRC_IN);
    }

/*    private static Drawable convertDrawableToGrayScale(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Drawable res = drawable.mutate();
        res.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        return res;
    } */

    /*
    static float pixelsToSp(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().scaledDensity;
    }
    */

    /*
    private static float spToPixels(Context context, float sp) {
        return sp * context.getResources().getDisplayMetrics().scaledDensity;
    }
    */

    static int dpToPx(float dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    /*
    static int dpToPx2(float dp, Context context)
    {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
    */

    static int dip(float dp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics()));
    }

    static int sip(float sp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, Resources.getSystem().getDisplayMetrics()));
    }

    /*
    static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    } */

    /*
    static Float getRawDimensionInDp(Resources resources, @DimenRes int dimenResId) {
        TypedValue value = new TypedValue();
        resources.getValue(dimenResId, value, true);
        return TypedValue.complexToFloat(value.data);
    }
    */

    /**
     * Uses reflection to access divider private attribute and override its color
     * Use Color.Transparent if you wish to hide them
     */
    /*static void setSeparatorColorForNumberPicker(NumberPicker picker, int separatorColor) {
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    pf.set(picker, new ColorDrawable(separatorColor));
                } catch (IllegalAccessException | IllegalArgumentException ignored) {
                }
                break;
            }
        }
    }*/
    /*
    static void updateTextAttributesForNumberPicker(NumberPicker picker, int textSizeSP) {
        for (int i = 0; i < picker.getChildCount(); i++){
            View child = picker.getChildAt(i);
            if (child instanceof EditText) {
                try {
                    Field selectorWheelPaintField = NumberPicker.class.getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);

                    Paint wheelPaint = ((Paint)selectorWheelPaintField.get(picker));
                    //wheelPaint.setColor(textColor);
                    wheelPaint.setTextSize(spToPixels(picker.getContext(), textSizeSP));

                    EditText editText = ((EditText) child);
                    //editText.setTextColor(textColor);
                    editText.setTextSize(textSizeSP);

                    picker.invalidate();
                    break;
                }
                catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException ignored) {
                }
            }
        }
    }
    */

    /*
    static Point getNavigationBarSize(Context context) {
        Point appUsableSize = getAppUsableScreenSize(context);
        Point realScreenSize = getRealScreenSize(context);

        if ((appUsableSize != null) && (realScreenSize != null)) {
            // navigation bar on the right
            if (appUsableSize.x < realScreenSize.x) {
                return new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y);
            }

            // navigation bar at the bottom
            if (appUsableSize.y < realScreenSize.y) {
                return new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y);
            }

            // navigation bar is not present
            return new Point();
        }
        else
            return null;
    }
    */

    /*
    private static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            return size;
        }
        else
            return null;
    }

    static Point getRealScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();

            display.getRealSize(size);

            return size;
        }
        else
            return null;
    }
    static int getStatusBarHeight(Context context) {
        //int result = 0;
        //int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        //if (resourceId > 0) {
        //    result = context.getResources().getDimensionPixelSize(resourceId);
        //}
        //return result;
        final Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            return resources.getDimensionPixelSize(resourceId);
        else
            return (int) Math.ceil((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 24 : 25) * resources.getDisplayMetrics().density);
    }
    static int getNavigationBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    */

    /*
    static int getThemeAccentColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        return value.data;
    }
    */

    /*
    static int getThemeWhiteTextColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.activityWhiteTextColor, value, true);
        return value.data;
    }
    */
    /*
    static int getThemeNormalTextColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.activityNormalTextColor, value, true);
        return value.data;
    }
    */
    /*
    static int getThemeDisabledTextColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.activityDisabledTextColor, value, true);
        return value.data;
    }
    */
    /*
    static int getThemeCommandBackgroundColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.activityCommandBackgroundColor, value, true);
        return value.data;
    }
    */

    /*
    static int getThemeColorControlHighlight(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorControlHighlight, value, true);
        return value.data;
    }
    */

    /*
    static int getThemeEventPauseColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.eventPauseTextColor, value, true);
        return value.data;
    }
    */
    /*
    static int getThemeEventStopColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.eventStopTextColor, value, true);
        return value.data;
    }
    */

    /*
    static int getThemeEventStopStatusIndicator(final Context context) {
        //final TypedValue value = new TypedValue();
        //context.getTheme().resolveAttribute(R.attr.eventStopStatusIndicator, value, true);
        //return value.data;
        int theme = GlobalGUIRoutines.getTheme(false, false, false, context);
        if (theme != 0) {
            TypedArray a = context.getTheme().obtainStyledAttributes(theme, new int[]{R.attr.eventStopStatusIndicator});
            return a.getResourceId(0, 0);
        }
        else
            return 0;
    }
    */

    /*
    static int getThemeEventInDelayColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.eventInDelayTextColor, value, true);
        return value.data;
    }
    */

    static int getThemeSensorPassStatusColor(final int passStatus, final Context context) {
        /*final TypedValue value = new TypedValue();
        if (passStatus == EventPreferences.SENSOR_PASSED_PASSED)
            context.getTheme().resolveAttribute(R.attr.sensorPassStatusPassed, value, true);
        else
        if (passStatus == EventPreferences.SENSOR_PASSED_NOT_PASSED)
            context.getTheme().resolveAttribute(R.attr.sensorPassStatusNotPassed, value, true);
        else
            context.getTheme().resolveAttribute(R.attr.sensorPassStatusWaiting, value, true);
        return value.data;*/
        if (passStatus == EventPreferences.SENSOR_PASSED_PASSED)
            return ContextCompat.getColor(context, R.color.sensor_pass_status_passed);
        else if (passStatus == EventPreferences.SENSOR_PASSED_NOT_PASSED)
            return ContextCompat.getColor(context, R.color.sensor_pass_status_not_passed);
        else
            return ContextCompat.getColor(context, R.color.sensor_pass_status_waiting);
    }

    /*
    static int getThemeActivatorGridDividerColor(final boolean show, final Context context) {
        final TypedValue value = new TypedValue();
        if (show)
            context.getTheme().resolveAttribute(android.R.attr.listDivider, value, false);
        else
            context.getTheme().resolveAttribute(R.attr.activityBackgroundColor, value, false);
        return value.data;
    }
    */

    /*
    static int getThemeActivityLogTypeOtherColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.activityLogTypeOther, value, true);
        return value.data;
    }
    */
    /*
    static private int getThemeEditorSpinnerDropDownTextColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.editorSpinnerDropDownTextColor, value, true);
        return value.data;
    }
    */
    /*
    static int getThemeDialogDividerColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.dialogDivider, value, true);
        return value.data;
    }
    */

    /*
    static private int getThemeEditorFilterBackgroundColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.editorFilterBackgroundColor, value, true);
        return value.data;
    }
    */
    /*
    static private int getThemeDialogBackgroundColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.activityBackgroundColor, value, true);
        return value.data;
    }
    */

    static void setThemeTimeDurationPickerDisplay(TimeDurationPicker timeDurationPicker, final Context context) {
        //boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(context.getApplicationContext());
//                (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
//                                    == Configuration.UI_MODE_NIGHT_YES;

        if (ApplicationPreferences.applicationTheme(context, true).equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_WHITE)/*!nightModeOn*/) {
            timeDurationPicker.setDisplayTextAppearance(R.style.TextAppearance_TimeDurationPicker_Display);
            timeDurationPicker.setUnitTextAppearance(R.style.TextAppearance_TimeDurationPicker_Unit);
        } else {
            timeDurationPicker.setDisplayTextAppearance(R.style.TextAppearance_TimeDurationPicker_Display_Dark);
            timeDurationPicker.setUnitTextAppearance(R.style.TextAppearance_TimeDurationPicker_Unit_Dark);
        }
        timeDurationPicker.setBackspaceIcon(ContextCompat.getDrawable(context, R.drawable.ic_backspace));
        timeDurationPicker.setClearIcon(ContextCompat.getDrawable(context, R.drawable.ic_clear));

        //timeDurationPicker.setDurationDisplayBackgroundColor(getThemeDialogBackgroundColor(context));
        timeDurationPicker.setDurationDisplayBackgroundColor(ContextCompat.getColor(context, R.color.activityBackgroundColor));
        //timeDurationPicker.setSeparatorColor(GlobalGUIRoutines.getThemeDialogDividerColor(context));
        timeDurationPicker.setDisplaySeparatorColor(ContextCompat.getColor(context, R.color.time_duration_picker_display_divider));
        timeDurationPicker.setButtonsSeparatorColor(ContextCompat.getColor(context, R.color.dialog_divider));
    }
    /*
    static int getThemeSecondaryTextColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.activitySecondaryTextColor, value, true);
        return value.data;
    }
    */

    /*
    static int getResourceId(String pVariableName, String pResourceName, Context context)
    {
        try {
            return context.getResources().getIdentifier(pVariableName, pResourceName, context.PPApplication.PACKAGE_NAME);
        } catch (Exception e) {
            return -1;
        }
    }
    */

    static boolean activityActionExists(String action, Context context) {
        try {
            final Intent intent = new Intent(action);
            List<ResolveInfo> activities = context.getApplicationContext().getPackageManager().queryIntentActivities(intent, 0);
            return !activities.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    static int getDynamicColor(int colorAttr, Context context) {
        if (Build.VERSION.SDK_INT >= 31) {
            if (DynamicColors.isDynamicColorAvailable()) {
                Context dynamicColorContext = DynamicColors.wrapContextIfAvailable(context, R.style.ThemeOverlay_Material3_DynamicColors_DayNight);
                /*int[] attrsToResolve = {
                        R.attr.colorPrimary,    // 0
                        R.attr.colorOnPrimary,  // 1
                        R.attr.colorSecondary,  // 2
                        R.attr.colorAccent,      // 3
                };*/
                int[] attrsToResolve = { colorAttr };
                // now resolve them
                //noinspection resource
                TypedArray ta = dynamicColorContext.obtainStyledAttributes(attrsToResolve);
                /*int color = ta.getColor(0, 0);
                int onPrimary = ta.getColor(1, 0);
                int secondary = ta.getColor(2, 0);
                int accent = ta.getColor(3, 0);*/
                int color = ta.getColor(0, 0);
                ta.recycle();   // recycle TypedArray

                return color;
            }
        }
        return 0;
    }

    static boolean activityIntentExists(Intent intent, Context context) {
        try {
            List<ResolveInfo> activities = context.getApplicationContext().getPackageManager().queryIntentActivities(intent, 0);
            return !activities.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /*
    static void registerOnActivityDestroyListener(@NonNull Preference preference, @NonNull PreferenceManager.OnActivityDestroyListener listener) {
        try {
            PreferenceManager pm = preference.getPreferenceManager();
            Method method = pm.getClass().getDeclaredMethod(
                    "registerOnActivityDestroyListener",
                    PreferenceManager.OnActivityDestroyListener.class);
            method.setAccessible(true);
            method.invoke(pm, listener);
        } catch (Exception ignored) {
        }
    }

    static void unregisterOnActivityDestroyListener(@NonNull Preference preference, @NonNull PreferenceManager.OnActivityDestroyListener listener) {
        try {
            PreferenceManager pm = preference.getPreferenceManager();
            Method method = pm.getClass().getDeclaredMethod(
                    "unregisterOnActivityDestroyListener",
                    PreferenceManager.OnActivityDestroyListener.class);
            method.setAccessible(true);
            method.invoke(pm, listener);
        } catch (Exception ignored) {
        }
    }
    */

    @SuppressLint("SourceLockedOrientationActivity")
    static void lockScreenOrientation(Activity activity, boolean toDefault) {
        try {
            if ((Build.VERSION.SDK_INT != 26) && (!toDefault)) {
                int currentOrientation = activity.getResources().getConfiguration().orientation;
                if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                } else {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                }
            }
            else
                // this set device to default orientation (for mobile to portrait, for 10' tablets to landscape)
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        } catch (Exception e) {
            // FC in API 26 (A8) - Google bug: java.lang.IllegalStateException: Only fullscreen activities can request orientation
            PPApplicationStatic.recordException(e);
        }
    }

    static void unlockScreenOrientation(Activity activity) {
        try {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        } catch (Exception e) {
            // FC in API 26 (A8) - Google bug: java.lang.IllegalStateException: Only fullscreen activities can request orientation
            PPApplicationStatic.recordException(e);
        }
    }

    static boolean areSystemAnimationsEnabled(Context context) {
        float duration, transition;
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            duration = Settings.Global.getFloat(
                    context.getContentResolver(),
                    Settings.Global.ANIMATOR_DURATION_SCALE, 1);
            transition = Settings.Global.getFloat(
                    context.getContentResolver(),
                    Settings.Global.TRANSITION_ANIMATION_SCALE, 1);
        /*} else {
            duration = Settings.System.getFloat(
                    context.getContentResolver(),
                    Settings.System.ANIMATOR_DURATION_SCALE, 1);
            transition = Settings.System.getFloat(
                    context.getContentResolver(),
                    Settings.System.TRANSITION_ANIMATION_SCALE, 1);
        }*/
        return (duration != 0 && transition != 0);
    }

    static void showDialogAboutRedText(final Profile profile, final Event event,
                                       final boolean forProfile,
                                       final boolean forActivator,
                                       final boolean forShowInActivator,
                                       final boolean forRunStopEvent,
                                       final Activity activity) {
        if (activity == null)
            return;

        if (forActivator) {
            Intent intent;
            if (forProfile && (profile != null)) {
                intent = new Intent(activity.getBaseContext(), ProfilesPrefsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                intent.putExtra(PPApplication.EXTRA_NEW_PROFILE_MODE, PPApplication.EDIT_MODE_EDIT);
                intent.putExtra(PPApplication.EXTRA_PREDEFINED_PROFILE_INDEX, 0);
                activity.startActivity(intent);
                /*try {
                    // close Activator
                    activity.finish();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }*/
            }
            /*if ((!forProfile) && (event != null)) {
                intent = new Intent(activity.getBaseContext(), EventsPrefsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra(PPApplication.EXTRA_EVENT_ID, event._id);
                intent.putExtra(PPApplication.EXTRA_NEW_EVENT_MODE, PPApplication.EDIT_MODE_EDIT);
                intent.putExtra(PPApplication.EXTRA_PREDEFINED_EVENT_INDEX, 0);
                activity.startActivity(intent);
                //try {
                    // close Activator
                //    activity.finish();
                //} catch (Exception e) {
                //    PPApplicationStatic.recordException(e);
                //}
            }*/
        } else {
            String nTitle = "";
            String nText = "";

            if (profile != null) {
                nTitle = activity.getString(R.string.profile_preferences_red_texts_title);
                nText = activity.getString(R.string.profile_preferences_red_texts_text_1) + " " +
                        "\"" + profile._name + "\" " +
                        activity.getString(R.string.preferences_red_texts_text_2);
                if (forShowInActivator)
                    nText = nText + " " + activity.getString(R.string.profile_preferences_red_texts_text_3_new);
                else
                    nText = nText + " " + activity.getString(R.string.profile_preferences_red_texts_text_2);

                nText = nText + StringConstants.STR_DOUBLE_NEWLINE + activity.getString(R.string.profile_preferences_red_texts_text_4);
            }

            if (event != null) {
                nTitle = activity.getString(R.string.event_preferences_red_texts_title);
                nText = activity.getString(R.string.event_preferences_red_texts_text_1) + " " +
                        "\"" + event._name + "\" " +
                        activity.getString(R.string.preferences_red_texts_text_2);
                if (forRunStopEvent)
                    nText = nText + " " + activity.getString(R.string.event_preferences_red_texts_text_2);
                else
                    nText = nText + " " + activity.getString(R.string.profile_preferences_red_texts_text_2);

                nText = nText + StringConstants.STR_DOUBLE_NEWLINE + activity.getString(R.string.event_preferences_red_texts_text_4);
            }

            String positiveText;
            DialogInterface.OnClickListener positiveClick;
            //String negativeText = null;
            //DialogInterface.OnClickListener negativeClick = null;

            if ((profile != null) || (event != null)) {
                if (forProfile) {
                    positiveText = activity.getString(R.string.show_dialog_about_red_text_show_profile_preferences);
                    positiveClick = (dialog, which) -> {
                        Intent intent;
                        if (profile != null) {
                            intent = new Intent(activity.getBaseContext(), ProfilesPrefsActivity.class);
                            //if (forActivator)
                            //    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                            intent.putExtra(PPApplication.EXTRA_NEW_PROFILE_MODE, PPApplication.EDIT_MODE_EDIT);
                            intent.putExtra(PPApplication.EXTRA_PREDEFINED_PROFILE_INDEX, 0);
                        } else {
                            intent = new Intent(activity.getBaseContext(), EditorActivity.class);
                            //if (forActivator)
                            //    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_EDITOR_SHOW_IN_ACTIVATOR_FILTER);
                        }
                        activity.startActivity(intent);

                        /*
                        try {
                            // close Activator
                            if (forActivator)
                                activity.finish();
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                        */
                    };
                    /*if (forActivator) {
                        negativeText = activity.getString(R.string.show_dialog_about_red_text_show_editor);
                        negativeClick = (dialog, which) -> {
                            Intent intent = new Intent(activity.getBaseContext(), EditorActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_EDITOR_SHOW_IN_ACTIVATOR_FILTER);
                            activity.startActivity(intent);

                            try {
                                // close Activator
                                activity.finish();
                            } catch (Exception e) {
                                PPApplicationStatic.recordException(e);
                            }
                        };
                    }*/
                } else {
                    positiveText = activity.getString(R.string.show_dialog_about_red_text_show_event_preferences);
                    positiveClick = (dialog, which) -> {
                        Intent intent;
                        if (event != null) {
                            intent = new Intent(activity.getBaseContext(), EventsPrefsActivity.class);
                            //if (forActivator)
                            //    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra(PPApplication.EXTRA_EVENT_ID, event._id);
                            intent.putExtra(PPApplication.EXTRA_NEW_EVENT_MODE, PPApplication.EDIT_MODE_EDIT);
                            intent.putExtra(PPApplication.EXTRA_PREDEFINED_EVENT_INDEX, 0);
                        } else {
                            intent = new Intent(activity.getBaseContext(), EditorActivity.class);
                            //if (forActivator)
                            //    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_EDITOR_SHOW_IN_EDITOR_FILTER);
                        }
                        activity.startActivity(intent);

                        /*
                        try {
                            // close Activator
                            if (forActivator)
                                activity.finish();
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                        */
                    };
                }

                PPAlertDialog dialog = new PPAlertDialog(nTitle, nText,
                        positiveText, null/*negativeText*/, null, null,
                        positiveClick,
                        null/*negativeClick*/,
                        null,
                        null,
                        null,
                        true/*!forActivator*/, true/*!forActivator*/,
                        false, false,
                        false,
                        false,
                        activity
                );

                if (!activity.isFinishing())
                    dialog.show();
            }
        }
    }

    /*
    static Intent getIntentForStartupSource(Context context, int startupSource) {
        Intent intentLaunch;

        switch (startupSource) {
            case PPApplication.STARTUP_SOURCE_NOTIFICATION:
                if (ApplicationPreferences.applicationNotificationLauncher.equals(StringConstants.EXTRA_ACTIVATOR))
                    intentLaunch = new Intent(context.getApplicationContext(), ActivatorActivity.class);
                else
                    intentLaunch = new Intent(context.getApplicationContext(), EditorActivity.class);
                break;
            case PPApplication.STARTUP_SOURCE_WIDGET:
                if (ApplicationPreferences.applicationWidgetLauncher.equals(StringConstants.EXTRA_ACTIVATOR))
                    intentLaunch = new Intent(context.getApplicationContext(), ActivatorActivity.class);
                else
                    intentLaunch = new Intent(context.getApplicationContext(), EditorActivity.class);
                break;
//            case PPApplication.STARTUP_SOURCE_EDITOR_WIDGET_HEADER:
//                intentLaunch = new Intent(context.getApplicationContext(), EditorActivity.class);
//                //startupSource = PPApplication.STARTUP_SOURCE_WIDGET;
//                break;
            default:
                //if (ApplicationPreferences.applicationHomeLauncher.equals("activator"))
                    intentLaunch = new Intent(context.getApplicationContext(), ActivatorActivity.class);
                //else
                //    intentLaunch = new Intent(context.getApplicationContext(), EditorActivity.class);
                break;
        }
        return intentLaunch;
    }
    */

}
