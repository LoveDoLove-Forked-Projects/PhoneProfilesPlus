package sk.henrichg.phoneprofilesplus;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

class CApplication implements Parcelable {
    int type = TYPE_APPLICATION;
    String appLabel = "";
    String packageName = "";
    String activityName = "";
    Bitmap icon = null;
    long shortcutId = 0;
    long intentId = 0;
    boolean checked = false;
    int startApplicationDelay;

    static final int TYPE_APPLICATION = 1;
    static final int TYPE_SHORTCUT = 2;
    static final int TYPE_INTENT = 3;

    CApplication() {
    }

    @NonNull
    public String toString() {
        if (type == TYPE_INTENT)
            return String.valueOf(intentId);
        else
            return appLabel;
    }

    void toggleChecked() {
        checked = !checked;
    }

    static boolean isShortcut(String value) {
        if (value.length() > 2) {
            String shortcut = value.substring(0, 3);
            return shortcut.equals(StringConstants.SHORTCUT_ID);
        }
        return false;
    }

    static boolean isIntent(String value) {
        if (value.length() > 2) {
            String intent = value.substring(0, 3);
            return intent.equals(StringConstants.INTENT_ID);
        }
        return false;
    }

    static String getPackageName(String value) {
        if (value.length() > 2) {
            String packageName = "";
            String shortcutIntent = "";
            String[] packageNameActivity = value.split("/");
            if (packageNameActivity.length == 2) {
                // activity exists
                if (packageNameActivity[0].length() > 2)
                    shortcutIntent = packageNameActivity[0].substring(0, 3);
                packageName = packageNameActivity[0];
                if (shortcutIntent.equals(StringConstants.SHORTCUT_ID)) {
                    // shortcut
                    if (packageNameActivity[0].length() > 3)
                        packageName = packageNameActivity[0].substring(3);
                }
                else
                if (shortcutIntent.equals(StringConstants.INTENT_ID)) {
                    // intent
                    packageName = "";
                }
            }
            else {
                // activity not exists
                shortcutIntent = value.substring(0, 3);
                if (!shortcutIntent.equals(StringConstants.SHORTCUT_ID) && !shortcutIntent.equals(StringConstants.INTENT_ID))
                    // application
                    packageName = value;
            }
            return packageName;
        }
        else
            return "";
    }

    static String getActivityName(String value) {
        if (value.length() > 2) {
            String activityName = "";
            String[] packageNameActivity = value.split("/");
            if (packageNameActivity.length == 2) {
                // activity exists
                if (packageNameActivity[0].length() > 2) {
                    String shortcutIntent = packageNameActivity[0].substring(0, 3);
                    if (!shortcutIntent.equals(StringConstants.INTENT_ID)) {
                        // application, shortcut
                        String[] activityShortcutIdDelay = packageNameActivity[1].split("#");
                        activityName = activityShortcutIdDelay[0];
                    }
                }
                else
                    return "";
            }
            return activityName;
        }
        else
            return "";
    }

    static long getShortcutId(String value) {
        if (value.length() > 2) {
            long shortcutId = 0;
            String[] packageNameActivity = value.split("/");
            if (packageNameActivity.length == 2) {
                // activity exists
                if (packageNameActivity[0].length() > 2) {
                    String shortcut = packageNameActivity[0].substring(0, 3);
                    String[] activityShortcutIdDelay = packageNameActivity[1].split("#");
                    if (shortcut.equals(StringConstants.SHORTCUT_ID)) {
                        // shortcut
                        if (activityShortcutIdDelay.length >= 2)
                            try {
                                shortcutId = Long.parseLong(activityShortcutIdDelay[1]);
                            } catch (Exception e) {
                                //PPApplicationStatic.recordException(e);
                            }
                    }
                }
            }
            return shortcutId;
        }
        else
            return 0;
    }

    static long getIntentId(String value) {
        if (value.length() > 2) {
            long intentId = 0;
            String[] intentIdDelay = value.split("#");
            if (intentIdDelay[0].length() > 2) {
                String intent = intentIdDelay[0].substring(0, 3);
                if (intent.equals(StringConstants.INTENT_ID)) {
                    // intent
                    try {
                        intentId = Long.parseLong(intentIdDelay[0].substring(3));
                    } catch (Exception e) {
                        //PPApplicationStatic.recordException(e);
                    }
                }
            }
            return intentId;
        }
        else
            return 0;
    }

    static int getStartApplicationDelay(String value) {
        if (value.length() > 2) {
            String shortcutIntent = "";
            int startApplicationDelay = 0;
            String[] packageNameActivity = value.split("/");
            if (packageNameActivity.length == 2) {
                // activity exists
                if (packageNameActivity[0].length() > 2)
                    shortcutIntent = packageNameActivity[0].substring(0, 3);
                String[] activityShortcutIdDelay = packageNameActivity[1].split("#");
                if (shortcutIntent.equals(StringConstants.SHORTCUT_ID)) {
                    // shortcut
                    if (activityShortcutIdDelay.length >= 3) {
                        try {
                            startApplicationDelay = Integer.parseInt(activityShortcutIdDelay[2]);
                        } catch (Exception e) {
                            //PPApplicationStatic.recordException(e);
                        }
                    }
                    else
                        //noinspection NonStrictComparisonCanBeEquality
                        if (activityShortcutIdDelay.length >= 2) {
                        try {
                            startApplicationDelay = Integer.parseInt(activityShortcutIdDelay[1]);
                        } catch (Exception e) {
                            //PPApplicationStatic.recordException(e);
                        }
                    }
                }
                else
                if (!shortcutIntent.equals(StringConstants.INTENT_ID)) {
                    // application
                    if (activityShortcutIdDelay.length >= 2) {
                        try {
                            startApplicationDelay = Integer.parseInt(activityShortcutIdDelay[1]);
                        } catch (Exception e) {
                            //PPApplicationStatic.recordException(e);
                        }
                    }
                }
            } else {
                // activity not exists
                //Log.e("Application.getStartApplicationDelay", "packageNameActivity.length <> 2");
                //shortcutIntent = value.substring(0, 3);
                //Log.e("Application.getStartApplicationDelay", "shortcutIntent="+shortcutIntent);
                //if (shortcutIntent.equals("(i)")) {
                    String[] packageNameDelay = value.split("#");
                    if (packageNameDelay.length >= 2) {
                        //Log.e("Application.getStartApplicationDelay", "packageNameDelay[1]="+packageNameDelay[1]);
                        try {
                            startApplicationDelay = Integer.parseInt(packageNameDelay[1]);
                        } catch (Exception e) {
                            //PPApplicationStatic.recordException(e);
                        }
                    }
                //}
                /*else {
                    String[] packageNameDelay = value.split("#");
                    if (packageNameDelay.length >= 2) {
                        try {
                            startApplicationDelay = Integer.parseInt(packageNameDelay[1]);
                        } catch (Exception e) {
                            //PPApplicationStatic.recordException(e);
                        }
                    }
                }*/
            }
            return startApplicationDelay;
        }
        else
            return 0;
    }

    private CApplication(Parcel in) {
        this.type = in.readInt();
        this.appLabel = in.readString();
        this.packageName = in.readString();
        this.activityName = in.readString();
        this.shortcutId = in.readLong();
        this.intentId = in.readLong();
        //this.checked = in.readBoolean();
        this.checked = in.readByte() != 0;
        this.startApplicationDelay = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeString(this.appLabel);
        dest.writeString(this.packageName);
        dest.writeString(this.activityName);
        dest.writeLong(this.shortcutId);
        dest.writeLong(this.intentId);
        //dest.writeBoolean(this.checked);
        dest.writeByte((byte) (this.checked ? 1 : 0));
        dest.writeInt(this.startApplicationDelay);
    }

    public static final Parcelable.Creator<CApplication> CREATOR = new Parcelable.Creator<>() {
        public CApplication createFromParcel(Parcel source) {
            return new CApplication(source);
        }

        public CApplication[] newArray(int size) {
            return new CApplication[size];
        }
    };

}