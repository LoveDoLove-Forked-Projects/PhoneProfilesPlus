package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.preference.Preference;

import java.io.File;

public class WallpaperFolderPreference extends Preference {

    private String wallpaperFolder;
    //private Bitmap bitmap;

    private final Context prefContext;

    static final int RESULT_GET_FOLDER = 1980;

    public WallpaperFolderPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        wallpaperFolder = "-";

        prefContext = context;
    }

    @Override
    protected void onClick()
    {
        if (Permissions.grantWallpaperFolderPermissions(prefContext))
            startGallery();
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index)
    {
        super.onGetDefaultValue(a, index);

        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        wallpaperFolder = getPersistedString((String)defaultValue);
        if ((wallpaperFolder != null) &&
                (!wallpaperFolder.isEmpty()) &&
                (!wallpaperFolder.equals("-"))) {
            Uri folderUri = Uri.parse(wallpaperFolder);

            try {
                String path;
                path = GlobalUtils.getRealPath(folderUri);
                setSummary(path);
            } catch (Exception e) {
                setSummary(R.string.preference_profile_no_change);
            }
        }
        else {
            setSummary(R.string.preference_profile_no_change);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.folder = wallpaperFolder;
        return myState;

    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if ((state == null) || (!state.getClass().equals(SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // restore instance state
        SavedState myState = (SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        wallpaperFolder = myState.folder;
        if ((wallpaperFolder != null) &&
                (!wallpaperFolder.isEmpty()) &&
                (!wallpaperFolder.equals("-"))) {
            Uri folderUri = Uri.parse(wallpaperFolder);

            try {
                String path;
                path = GlobalUtils.getRealPath(folderUri);
                setSummary(path);
            } catch (Exception e) {
                setSummary(R.string.preference_profile_no_change);
            }
        }

        //notifyChanged();
    }

    void setWallpaperFolder(String newWallpaperFolder)
    {
        if (!callChangeListener(newWallpaperFolder)) {
            return;
        }

        wallpaperFolder = newWallpaperFolder;
        if ((wallpaperFolder != null) &&
                (!wallpaperFolder.isEmpty()) &&
                (!wallpaperFolder.equals("-"))) {
            Uri folderUri = Uri.parse(wallpaperFolder);
            try {
                String path;
                path = GlobalUtils.getRealPath(folderUri);
                setSummary(path);
            } catch (Exception e) {
                setSummary(R.string.preference_profile_no_change);
            }
        }

        // save to preferences
        persistString(newWallpaperFolder);

        // and notify
        notifyChanged();

    }

    void startGallery()
    {
        Intent intent;
        boolean _ok = false;
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                StorageManager sm = (StorageManager) prefContext.getSystemService(Context.STORAGE_SERVICE);
                intent = sm.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
            }
            else {
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            }
            // not supported by ACTION_OPEN_DOCUMENT_TREE
            //intent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);

            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                boolean ok = false;
                if (!(wallpaperFolder.isEmpty() || wallpaperFolder.equals("-"))) {
                    try {
                        Uri picturesUri = Uri.parse(wallpaperFolder);
                        if (picturesUri != null)
                            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, picturesUri);
                        ok = true;
                    } catch (Exception ignored) {
                    }
                }
                if (!ok) {
                    try {
                        File pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                        String fileName = pictures.getName();
                        Uri picturesUri = Uri.parse("content://com.android.externalstorage.documents/document/primary:" + fileName);
                        if (picturesUri != null)
                            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, picturesUri);
                    } catch (Exception ignored) {
                    }
                }

            ((Activity)prefContext).startActivityForResult(intent, RESULT_GET_FOLDER);
            _ok = true;
        } catch (Exception e) {
            //PPApplicationStatic.recordException(e);
        }
        if (!_ok) {
            try {
                PPAlertDialog _dialog = new PPAlertDialog(
                        prefContext.getString(R.string.profile_preferences_deviceWallpaperFolder),
                        prefContext.getString(R.string.directory_tree_activity_not_found_alert),
                        prefContext.getString(android.R.string.ok),
                        null,
                        null, null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        true, true,
                        false, false,
                        true,
                        false,
                        (Activity) prefContext
                );

                //if (!activity.isFinishing())
                _dialog.show();
            } catch (Exception e) {
                //PPApplicationStatic.recordException(e);
            }
        }
    }


    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String folder;

        SavedState(Parcel source)
        {
            super(source);

            // restore image identifier
            folder = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save image identifier and type
            dest.writeString(folder);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<SavedState> CREATOR =
                new Creator<WallpaperFolderPreference.SavedState>() {
            public WallpaperFolderPreference.SavedState createFromParcel(Parcel in)
            {
                return new WallpaperFolderPreference.SavedState(in);
            }
            public WallpaperFolderPreference.SavedState[] newArray(int size)
            {
                return new WallpaperFolderPreference.SavedState[size];
            }

        };

    }

}
