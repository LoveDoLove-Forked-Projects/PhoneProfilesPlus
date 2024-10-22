package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import java.io.File;
import java.lang.ref.WeakReference;

public class WallpaperViewPreference extends Preference {

    private String imageIdentifier;
    //private Bitmap bitmap;
    private final boolean forLockScreen;

    private final Context prefContext;
    private ImageView imageView;

    private BindViewAsyncTask bindViewAsyncTask = null;

    static final int RESULT_LOAD_IMAGE = 1970;
    static final int RESULT_LOAD_IMAGE_LOCKSCREEN = 1972;

    public WallpaperViewPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        imageIdentifier = "-";

        prefContext = context;

        //noinspection resource
        TypedArray wallpaperType = context.obtainStyledAttributes(attrs,
                R.styleable.WallpaperViewPreference, 0, 0);
        forLockScreen = wallpaperType.getBoolean(R.styleable.WallpaperViewPreference_forLockScreen, false);

        wallpaperType.recycle();

        setWidgetLayoutResource(R.layout.preference_widget_imageview_preference);
    }

    //@Override
    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);

        imageView = (ImageView) holder.findViewById(R.id.imageview_pref_imageview);

        bindViewAsyncTask = new BindViewAsyncTask(this);
        bindViewAsyncTask.execute();
    }

    @Override
    public void onDetached() {
        super.onDetached();
        if ((bindViewAsyncTask != null) &&
                bindViewAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            bindViewAsyncTask.cancel(true);
        bindViewAsyncTask = null;
    }

    @Override
    protected void onClick()
    {
        if (Permissions.grantImageWallpaperPermissions(prefContext, forLockScreen))
            startGallery();
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index)
    {
        super.onGetDefaultValue(a, index);

        return a.getString(index);  // icon is returned as string
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        imageIdentifier = getPersistedString((String)defaultValue);
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.imageIdentifier = imageIdentifier;
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
        imageIdentifier = myState.imageIdentifier;
        //notifyChanged();
    }

    private Bitmap getBitmap() {
        if (!imageIdentifier.startsWith("-")) {
            int height = GlobalGUIRoutines.dpToPx(GlobalGUIRoutines.ICON_SIZE_DP);
            int width = GlobalGUIRoutines.dpToPx(GlobalGUIRoutines.ICON_SIZE_DP);
            return BitmapManipulator.resampleBitmapUri(imageIdentifier, width, height, false, true, prefContext);
        }
        else
            return null;
    }

    void setImageIdentifier(String newImageIdentifier)
    {
        if (!callChangeListener(newImageIdentifier)) {
            return;
        }

        imageIdentifier = newImageIdentifier;
        //Log.d("---- WallpaperViewPreference.setImageIdentifier","getBitmap");
        //getBitmap();

        // save to preferences
        persistString(newImageIdentifier);

        // and notify
        notifyChanged();

    }

    void startGallery()
    {
        Intent intent;
        try {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType(StringConstants.MIME_TYPE_IMAGE);

                boolean ok = false;
                if (!(imageIdentifier.isEmpty() || imageIdentifier.equals("-"))) {
                    try {
                        Uri picturesUri = Uri.parse(imageIdentifier);
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

            // is not possible to get activity from preference, used is static method
            //ProfilesPrefsFragment.setChangedWallpaperViewPreference(this);
            if (forLockScreen)
                ((Activity)prefContext).startActivityForResult(intent, RESULT_LOAD_IMAGE_LOCKSCREEN);
            else
                ((Activity)prefContext).startActivityForResult(intent, RESULT_LOAD_IMAGE);
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }


    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String imageIdentifier;

        SavedState(Parcel source)
        {
            super(source);

            // restore image identifier
            imageIdentifier = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save image identifier and type
            dest.writeString(imageIdentifier);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<WallpaperViewPreference.SavedState> CREATOR =
                new Creator<>() {
            public WallpaperViewPreference.SavedState createFromParcel(Parcel in)
            {
                return new WallpaperViewPreference.SavedState(in);
            }
            public WallpaperViewPreference.SavedState[] newArray(int size)
            {
                return new WallpaperViewPreference.SavedState[size];
            }

        };

    }

    private static class BindViewAsyncTask extends AsyncTask<Void, Integer, Void> {

        Bitmap bitmap;

        private final WeakReference<WallpaperViewPreference> preferenceWeakRef;

        public BindViewAsyncTask(WallpaperViewPreference preference) {
            this.preferenceWeakRef = new WeakReference<>(preference);
        }

        @Override
        protected Void doInBackground(Void... params) {
            WallpaperViewPreference preference = preferenceWeakRef.get();
            if (preference != null) {
                bitmap = preference.getBitmap();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            WallpaperViewPreference preference = preferenceWeakRef.get();
            if (preference != null) {
                if (preference.imageView != null) {
                    if (bitmap != null) {
                        preference.imageView.setImageBitmap(bitmap);
                        if (!preference.isEnabled())
                            preference.imageView.setAlpha(0.35f);
                        else
                            preference.imageView.setAlpha(1f);
                    }
                    else {
                        preference.imageView.setImageResource(R.drawable.ic_empty);
                        preference.imageView.setAlpha(1f);
                    }
                }
            }
        }

    }

//---------------------------------------------------------------------------------------------

    /*
    static Uri getImageContentUri(Context context, String imageFile) {
        Cursor cursor = context.getApplicationContext().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { imageFile }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
            cursor.close();
            Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
            return uri;
        } else {
            if (cursor != null)
                cursor.close();
            File file = new File(imageFile);
            if (file.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, imageFile);
                Uri uri = context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                //    final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                //    ContentResolver resolver = context.getApplicationContext().getContentResolver();
                //    resolver.takePersistableUriPermission(uri, takeFlags);
                //}
                return uri;
            } else {
                return null;
            }
        }
    }
    */

}
