package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import java.util.List;

import androidx.preference.DialogPreference;

public class ContactsMultiSelectDialogPreferenceX extends DialogPreference
{
    ContactsMultiSelectDialogPreferenceFragmentX fragment;

    private final Context _context;
    String value = "";

    List<Contact> contactList;

    public ContactsMultiSelectDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        if (EditorProfilesActivity.getContactsCache() == null)
            EditorProfilesActivity.createContactsCache();

    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        // Get the persistent value
        value = getPersistedString(value);
        getValueCMSDP();
        setSummaryCMSDP();
    }

    @SuppressLint("StaticFieldLeak")
    void refreshListView(final boolean notForUnselect) {
        if (fragment != null)
            fragment.refreshListView(notForUnselect);
    }

    void getValueCMSDP()
    {
        // change checked state by value
        contactList = EditorProfilesActivity.getContactsCache().getList();
        if (contactList != null)
        {
            String[] splits = value.split("\\|");
            for (Contact contact : contactList)
            {
                contact.checked = false;
                for (String split : splits) {
                    try {
                        String[] splits2 = split.split("#");
                        long contactId = Long.parseLong(splits2[0]);
                        long phoneId = Long.parseLong(splits2[1]);
                        if ((contact.contactId == contactId) && (contact.phoneId == phoneId))
                            contact.checked = true;
                    } catch (Exception ignored) {
                    }
                }
            }
            // move checked on top
            int i = 0;
            int ich = 0;
            while (i < contactList.size()) {
                Contact contact = contactList.get(i);
                if (contact.checked) {
                    contactList.remove(i);
                    contactList.add(ich, contact);
                    ich++;
                }
                i++;
            }
        }
    }

    private void setSummaryCMSDP()
    {
        String prefVolumeDataSummary = _context.getString(R.string.contacts_multiselect_summary_text_not_selected);
        if (Permissions.checkContacts(_context)) {
            if (!value.isEmpty()) {
                String[] splits = value.split("\\|");
                if (splits.length == 1) {
                    boolean found = false;
                    String[] projection = new String[]{
                            ContactsContract.Contacts._ID,
                            ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.Contacts.PHOTO_ID};
                    String[] splits2 = splits[0].split("#");
                    String selection = ContactsContract.Contacts._ID + "=" + splits2[0];
                    Cursor mCursor = _context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, selection, null, null);

                    if (mCursor != null) {
                        while (mCursor.moveToNext()) {
                            selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + splits2[0] + " AND " +
                                    ContactsContract.CommonDataKinds.Phone._ID + "=" + splits2[1];
                            Cursor phones = _context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, selection, null, null);
                            if (phones != null) {
                                //while (phones.moveToNext()) {
                                if (phones.moveToFirst()) {
                                    found = true;
                                    prefVolumeDataSummary = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)) + '\n' +
                                            phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    //break;
                                }
                                phones.close();
                            }
                            if (found)
                                break;
                        }
                        mCursor.close();
                    }
                    if (!found)
                        prefVolumeDataSummary = _context.getString(R.string.contacts_multiselect_summary_text_selected) + ": " + splits.length;
                } else
                    prefVolumeDataSummary = _context.getString(R.string.contacts_multiselect_summary_text_selected) + ": " + splits.length;
            }
        }
        setSummary(prefVolumeDataSummary);
    }

    @SuppressWarnings("StringConcatenationInLoop")
    void persistValue() {
        if (shouldPersist())
        {
            // fill with strings of contacts separated with |
            value = "";
            if (contactList != null)
            {
                for (Contact contact : contactList)
                {
                    if (contact.checked)
                    {
                        if (!value.isEmpty())
                            value = value + "|";
                        value = value + contact.contactId + "#" + contact.phoneId;
                    }
                }
            }
            persistString(value);

            setSummaryCMSDP();
        }
    }


    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final ContactsMultiSelectDialogPreferenceX.SavedState myState = new ContactsMultiSelectDialogPreferenceX.SavedState(superState);
        myState.value = value;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if (!state.getClass().equals(ContactsMultiSelectDialogPreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryCMSDP();
            return;
        }

        // restore instance state
        ContactsMultiSelectDialogPreferenceX.SavedState myState = (ContactsMultiSelectDialogPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;

        setSummaryCMSDP();
        //notifyChanged();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;

        SavedState(Parcel source)
        {
            super(source);

            value = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeString(value);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<ContactsMultiSelectDialogPreferenceX.SavedState> CREATOR =
                new Creator<ContactsMultiSelectDialogPreferenceX.SavedState>() {
                    public ContactsMultiSelectDialogPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new ContactsMultiSelectDialogPreferenceX.SavedState(in);
                    }
                    public ContactsMultiSelectDialogPreferenceX.SavedState[] newArray(int size)
                    {
                        return new ContactsMultiSelectDialogPreferenceX.SavedState[size];
                    }

                };

    }

}
