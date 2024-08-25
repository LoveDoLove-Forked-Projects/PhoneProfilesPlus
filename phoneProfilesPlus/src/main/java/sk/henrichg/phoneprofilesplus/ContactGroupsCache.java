package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

/** @noinspection ExtractMethodRecommender*/
class ContactGroupsCache {

    private final ArrayList<ContactGroup> contactGroupList;

    private boolean cached;
    private boolean caching;
    //private boolean cancelled;

    ContactGroupsCache()
    {
        contactGroupList = new ArrayList<>();
        cached = false;
        caching = false;
    }

    static String translateContactGroup(String name, Context context) {
        if (name.equals("My Contacts"))
            name = context.getString(R.string.contact_group_name_myContacts);
        if (name.equals("Family"))
            name = context.getString(R.string.contact_group_name_family);
        if (name.equals("Friends"))
            name = context.getString(R.string.contact_group_name_friends);
        if (name.equals("Coworkers"))
            name = context.getString(R.string.contact_group_name_coworkers);
        if (name.equals("Starred in Android"))
            name = context.getString(R.string.contact_group_name_starred);
        if (name.equals("Starred"))
            name = context.getString(R.string.contact_group_name_starred);
        return name;
    }

    void getContactGroupList(Context context/*, boolean fixEvents*//*, boolean forceCache*/) {
        //if ((cached || caching) && (!forceCache)) return;

        caching = true;
        //cancelled = false;

        ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
        if (contactsCache == null) {
            caching = false;
            return;
        }

        ArrayList<ContactGroup> _contactGroupList = new ArrayList<>();
        //ArrayList<ContactGroupsInEvent> _contactGroupInEventsCall = new ArrayList<>();
        //ArrayList<ContactGroupsInEvent> _contactGroupInEventsSMS = new ArrayList<>();
        //ArrayList<ContactGroupsInEvent> _contactGroupInEventsNotification = new ArrayList<>();
        //ArrayList<ContactGroup> _oldContactGroupList = new ArrayList<>();

        List<Contact> _contactList;
//        PPApplicationStatic.logE("[SYNCHRONIZED] ContactGroupsCache.getContactGroupList", "(1) PPApplication.contactsCacheMutex");
        synchronized (PPApplication.contactsCacheMutex) {
            _contactList = contactsCache.getList(/*false*/);
            if (_contactList == null)
                _contactList = new ArrayList<>();
        }

//        long kolegoviaGroupId = 0;

        //DataWrapper dataWrapper = null;

        try {
            if (Permissions.checkContacts(context)) {

                /*
                if (fixEvents && (contactGroupList.size() != 0)) {
//                    Log.e("ContactGroupsCache.getContactGroupList", "contactGroupList.size() != 0");

                    dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, 0, 0, 0f);
                    dataWrapper.fillEventList();

                    // fill array with events, which uses group cache
                    for (Event _event : dataWrapper.eventList) {
                        if (_event._eventPreferencesCall._enabled) {
                            ContactGroupsInEvent contactGroupsInEvent = new ContactGroupsInEvent();
                            contactGroupsInEvent.event = _event;
                            contactGroupsInEvent.groups = _event._eventPreferencesCall._contactGroups;
                            //contactsInEvent.sensorType = EventsHandler.SENSOR_TYPE_PHONE_CALL;
                            _contactGroupInEventsCall.add(contactGroupsInEvent);
                        }
                    }
                    for (Event _event : dataWrapper.eventList) {
                        if (_event._eventPreferencesSMS._enabled) {
                            ContactGroupsInEvent contactGroupsInEvent = new ContactGroupsInEvent();
                            contactGroupsInEvent.event = _event;
                            contactGroupsInEvent.groups = _event._eventPreferencesSMS._contactGroups;
                            //contactsInEvent.sensorType = EventsHandler.SENSOR_TYPE_SMS;
                            _contactGroupInEventsSMS.add(contactGroupsInEvent);
                        }
                    }
                    for (Event _event : dataWrapper.eventList) {
                        if (_event._eventPreferencesNotification._enabled) {
                            ContactGroupsInEvent contactGroupsInEvent = new ContactGroupsInEvent();
                            contactGroupsInEvent.event = _event;
                            contactGroupsInEvent.groups = _event._eventPreferencesNotification._contactGroups;
                            //contactsInEvent.sensorType = EventsHandler.SENSOR_TYPE_NOTIFICATION;
                            _contactGroupInEventsNotification.add(contactGroupsInEvent);
                        }
                    }
//                    Log.e("ContactGroupsCache.getContactGroupList", "_contactGroupInEventsCall.size()="+_contactGroupInEventsCall.size());
//                    Log.e("ContactGroupsCache.getContactGroupList", "_contactGroupInEventsSMS.size()="+_contactGroupInEventsSMS.size());
//                    Log.e("ContactGroupsCache.getContactGroupList", "_contactGroupInEventsNotification.size()="+_contactGroupInEventsNotification.size());
                } //else
//                    Log.e("ContactGroupsCache.getContactGroupList", "contactGroupList.size() == 0");
                */

                clearGroups(_contactList);
                //contactsCache.clearGroups(_contactListWithoutNumber);

                List<Long> contactGroupIds = new ArrayList<>();

                String[] projection = new String[]{
                        ContactsContract.Groups._ID,
                        ContactsContract.Groups.TITLE,
                        ContactsContract.Groups.SUMMARY_COUNT,
                        ContactsContract.Groups.ACCOUNT_TYPE
                };
                String selection = ContactsContract.Groups.DELETED + "=0"; //' + " AND " +
                //ContactsContract.Groups.GROUP_VISIBLE+"=1 ";
                //ContactsContract.Groups.ACCOUNT_TYPE + "<>'vnd.sec.contact.phone'";
                //String order = ContactsContract.Groups.TITLE + " ASC";

                Cursor mCursor = context.getContentResolver().query(ContactsContract.Groups.CONTENT_SUMMARY_URI, projection, selection, null, null);

                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        long contactGroupId = mCursor.getLong(mCursor.getColumnIndexOrThrow(ContactsContract.Groups._ID));

                        String name = mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Groups.TITLE));
                        if (name != null) {
                            name = translateContactGroup(name, context);

                            String accountType = mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Groups.ACCOUNT_TYPE));

                            int count = mCursor.getInt(mCursor.getColumnIndexOrThrow(ContactsContract.Groups.SUMMARY_COUNT));

                            //if (count > 0) {
                            contactGroupIds.add(contactGroupId);

                            ContactGroup aContactGroup = new ContactGroup();
                            aContactGroup.groupId = contactGroupId;
                            aContactGroup.name = name;
                            aContactGroup.count = count;
                            aContactGroup.accountType = accountType;

                            _contactGroupList.add(aContactGroup);

                        }

                        //if (cancelled)
                        //    break;

                    }
                    mCursor.close();
                }

                //if (cancelled)
                //    return;

                String[] projectionGroup = new String[]{
                        ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID,
                        ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID//,
                        //ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME
                };
                String selectionGroup = ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='"
                        + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";

                Cursor mCursorGroup = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, projectionGroup, selectionGroup, null, null);
                if (mCursorGroup != null) {
                    while (mCursorGroup.moveToNext()) {
                        long groupRowId = mCursorGroup.getLong(mCursorGroup.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID));
                        for (long contactGroupId : contactGroupIds) {
                            if (groupRowId == contactGroupId) {
                                // contact is in contactGroupId group

                                long contactId = mCursorGroup.getLong(mCursorGroup.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID));
                                addGroupToContact(contactId, contactGroupId, _contactList);
                                //contactsCache.addGroup(contactId, contactGroupId, _contactListWithoutNumber);
                            }
                        }
                    }
                    mCursorGroup.close();
                }

                //_contactGroupList.sort(new ContactGroupsComparator());

//                PPApplicationStatic.logE("[SYNCHRONIZED] ContactGroupsCache.getContactGroupList", "(2) PPApplication.contactsCacheMutex");
                synchronized (PPApplication.contactsCacheMutex) {
                    contactsCache.updateContacts(_contactList/*, false*/);
                    //contactsCache.updateContacts(_contactListWithoutNumber, true);

                    /*
                    if (fixEvents && (contactGroupList.size() != 0)) {
//                        Log.e("ContactGroupsCache.getContactGroupList", "contactGroupList.size() != 0");

                        // do copy of old contactGroupList
                        for (ContactGroup _contactGroup : contactGroupList) {
                            ContactGroup dGroup = new ContactGroup();
                            dGroup.groupId = _contactGroup.groupId;
                            dGroup.name = _contactGroup.name;
                            dGroup.accountType = _contactGroup.accountType;
                            _oldContactGroupList.add(dGroup);
                        }
//                        Log.e("ContactGroupsCache.getContactGroupList", "_oldContactGroupList.size()="+_oldContactGroupList.size());
                    } //else
//                        Log.e("ContactGroupsCache.getContactGroupList", "contactGroupList.size() == 0");
                    */

                    updateContactGroups(_contactGroupList);

                    /*
                    if (fixEvents && (_oldContactGroupList.size() != 0)) {
//                        Log.e("ContactGroupsCache.getContactGroupList", "_oldContactGroupList.size() != 0");

                        for (ContactGroupsInEvent contactGroupsInEvent : _contactGroupInEventsCall) {
                            // for each contactGroupsInEvent for call sensor
//                            Log.e("ContactGroupsCache.getContactGroupList", "(1) contactGroupsInEvent.event._eventPreferencesCall._contactGroups="+contactGroupsInEvent.event._eventPreferencesCall._contactGroups);
                            contactGroupsInEvent.event._eventPreferencesCall._contactGroups =
                                    covertOldGroupToNewGroup(contactGroupsInEvent, _oldContactGroupList);
                            DatabaseHandler.getInstance(context.getApplicationContext()).updateEvent(contactGroupsInEvent.event);
//                            Log.e("ContactGroupsCache.getContactGroupList", "(2) contactGroupsInEvent.event._eventPreferencesCall._contactGroups="+contactGroupsInEvent.event._eventPreferencesCall._contactGroups);
                        }
                        for (ContactGroupsInEvent contactGroupsInEvent : _contactGroupInEventsSMS) {
                            // for each contactGroupsInEvent for sms sensor
//                            Log.e("ContactGroupsCache.getContactGroupList", "(1) contactGroupsInEvent.event._eventPreferencesSMS._contactGroups="+contactGroupsInEvent.event._eventPreferencesSMS._contactGroups);
                            contactGroupsInEvent.event._eventPreferencesSMS._contactGroups =
                                    covertOldGroupToNewGroup(contactGroupsInEvent, _oldContactGroupList);
                            DatabaseHandler.getInstance(context.getApplicationContext()).updateEvent(contactGroupsInEvent.event);
//                            Log.e("ContactGroupsCache.getContactGroupList", "(2) contactGroupsInEvent.event._eventPreferencesSMS._contactGroups="+contactGroupsInEvent.event._eventPreferencesSMS._contactGroups);
                        }
                        for (ContactGroupsInEvent contactGroupsInEvent : _contactGroupInEventsNotification) {
                            // for each contactGroupsInEvent for notification sensor
//                            Log.e("ContactGroupsCache.getContactGroupList", "(1) contactGroupsInEvent.event._eventPreferencesNotification._contactGroups="+contactGroupsInEvent.event._eventPreferencesNotification._contactGroups);
                            contactGroupsInEvent.event._eventPreferencesNotification._contactGroups =
                                    covertOldGroupToNewGroup(contactGroupsInEvent, _oldContactGroupList);
                            DatabaseHandler.getInstance(context.getApplicationContext()).updateEvent(contactGroupsInEvent.event);
//                            Log.e("ContactGroupsCache.getContactGroupList", "(2) contactGroupsInEvent.event._eventPreferencesNotification._contactGroups="+contactGroupsInEvent.event._eventPreferencesNotification._contactGroups);
                        }
                    } //else
//                        Log.e("ContactGroupsCache.getContactGroupList", "_oldContactGroupList.size() == 0");
                     */

                }

                _contactGroupList.clear();
                _contactList.clear();

                cached = true;
            }
        } catch (SecurityException e) {
//            Log.e("ContactGroupsCache.getContactGroupListX", Log.getStackTraceString(e));
            //PPApplicationStatic.recordException(e);

            _contactGroupList.clear();
            clearGroups(_contactList);
            //contactsCache.clearGroups(_contactListWithoutNumber);

//            PPApplicationStatic.logE("[SYNCHRONIZED] ContactGroupsCache.getContactGroupList", "(3) PPApplication.contactsCacheMutex");
            synchronized (PPApplication.contactsCacheMutex) {
                contactsCache.updateContacts(_contactList/*, false*/);
                //contactsCache.updateContacts(_contactListWithoutNumber, true);

                updateContactGroups(_contactGroupList);
            }
            _contactList.clear();

            cached = false;
        } catch (Exception e) {
//            Log.e("ContactGroupsCache.getContactGroupListX", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);

            _contactGroupList.clear();
            clearGroups(_contactList);
            //contactsCache.clearGroups(_contactListWithoutNumber);

//            PPApplicationStatic.logE("[SYNCHRONIZED] ContactGroupsCache.getContactGroupList", "(4) PPApplication.contactsCacheMutex");
            synchronized (PPApplication.contactsCacheMutex) {
                contactsCache.updateContacts(_contactList/*, false*/);
                //contactsCache.updateContacts(_contactListWithoutNumber, true);

                updateContactGroups(_contactGroupList);
            }
            _contactList.clear();

            cached = false;
        }

        //if (dataWrapper != null)
        //    dataWrapper.invalidateDataWrapper();

        caching = false;
    }

    /*
    int getLength()
    {
        synchronized (PPApplication.contactsCacheMutex) {
            if (cached)
                return contactGroupList.size();
            else
                return 0;
        }
    }
    */

    void updateContactGroups(List<ContactGroup> _contactGroupList) {
        contactGroupList.clear();
        contactGroupList.addAll(_contactGroupList);
    }

    List<ContactGroup> getList()
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] ContactGroupsCache.getList", "PPApplication.contactsCacheMutex");
        synchronized (PPApplication.contactsCacheMutex) {
            if (cached) {
                //return contactGroupList;
                ArrayList<ContactGroup> copyOfList = new ArrayList<>();
                for (ContactGroup contactGroup : contactGroupList) {
                    ContactGroup copOfGroup = new ContactGroup();
                    copOfGroup.groupId = contactGroup.groupId;
                    copOfGroup.name = contactGroup.name;
                    copOfGroup.count = contactGroup.count;
                    copOfGroup.accountType = contactGroup.accountType;
                    copyOfList.add(copOfGroup);
                }
                return copyOfList;
            }
            else
                return null;
        }
    }

/*
    ContactGroup getContactGroup(int position)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] ContactGroupsCache.getContactGroup", "PPApplication.contactsCacheMutex");
        synchronized (PPApplication.contactsCacheMutex) {
            if (cached)
                return contactGroupList.get(position);
            else
                return null;
        }
    }
 */

    // called only from ContactGroupsCache
    void clearGroups(List<Contact> _contactList) {
        if (_contactList == null)
            return;

        for (Contact contact : _contactList) {
            if (contact.groups != null) {
                //synchronized (PPApplication.contactsCacheMutex) {
                    contact.groups.clear();
                    contact.groups = null;
                //}
            }
        }
    }

    // called only from ContactGroupsCache
    void addGroupToContact(long contactId, long contactGroupId, List<Contact> _contactList) {
        if (_contactList == null)
            return;

        for (Contact contact : _contactList) {
            boolean contactFound = false;

            if (contact.contactId == contactId) {
                contactFound = true;

//                PPApplicationStatic.logE("[SYNCHRONIZED] ContactGroupsCache.addGroupToContact", "PPApplication.contactsCacheMutex");
                synchronized (PPApplication.contactsCacheMutex) {
                    if (contact.groups == null)
                        contact.groups = new ArrayList<>();

                    // search group in contact
                    boolean groupFound = false;
                    for (long groupId : contact.groups) {
                        if (groupId == contactGroupId) {
                            groupFound = true;
                            break;
                        }
                    }
                    if (!groupFound) {
                        // group not found, add it
                        contact.groups.add(contactGroupId);
                    }
                }
            }

            if (contactFound)
                break;
        }
    }

    void clearCache()
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] ContactGroupsCache.clearCache", "PPApplication.contactsCacheMutex");
        synchronized (PPApplication.contactsCacheMutex) {
            contactGroupList.clear();
            cached = false;
            caching = false;
        }
    }

    boolean getCaching() {
        return caching;
    }

    /*
    private String covertOldGroupToNewGroup(ContactGroupsInEvent groupsInEvent, List<ContactGroup> _oldContactGroupList) {
        if (groupsInEvent.groups == null)
            return "";

        StringBuilder newGroups = new StringBuilder();

        String[] splits = groupsInEvent.groups.split(StringConstants.STR_SPLIT_REGEX);
        for (String split : splits) {
            // for each group in groupsInEvent.groups
            if (split.isEmpty())
                continue;

            long _groupId = Long.parseLong(split);

//            Log.e("ContactGroupsCache.covertOldGroupToNewGroup", "_groupId="+_groupId);

            boolean foundInNew = false;
            // search one group from groupsInEvent.groups
            for (ContactGroup oldGroup : _oldContactGroupList) {
                boolean foundInOld = false;
                if (oldGroup.groupId == _groupId)
                    foundInOld = true;
//                Log.e("ContactGroupsCache.covertOldGroupToNewGroup", "foundInOld="+foundInOld);
                if (foundInOld) {
                    // found contact in old list

                    // search it in new list
                    for (ContactGroup newGroup : contactGroupList) {
                        // search these fields in new contactGroupList
//                        Log.e("ContactGroupsCache.covertOldGroupToNewGroup", "oldGroup.name="+oldGroup.name);
//                        Log.e("ContactGroupsCache.covertOldGroupToNewGroup", "oldGroup.accountType="+oldGroup.accountType);
                        if (newGroup.name.equals(oldGroup.name) &&
                                newGroup.accountType.equals(oldGroup.accountType)) {
                            foundInNew = true;
//                            Log.e("ContactGroupsCache.covertOldGroupToNewGroup", "newGroup.groupId="+newGroup.groupId);
                            // update group to new group in event
                            if (newGroups.length() > 0)
                                newGroups.append("|");
                            newGroups.append(newGroup.groupId);
                            break;
                        }
                    }
                    break;
                }
            }
//            Log.e("ContactGroupsCache.covertOldGroupToNewGroup", "foundInNew="+foundInNew);
            if (!foundInNew) {
                // get back old contact
                if (newGroups.length() > 0)
                    newGroups.append("|");
                newGroups.append(split);
            }
        }

        return newGroups.toString();
    }
    */

    /*
    private static class ContactGroupsComparator implements Comparator<ContactGroup> {

        public int compare(ContactGroup lhs, ContactGroup rhs) {
            if (PPApplication.collator != null)
                return PPApplication.collator.compare(lhs.name, rhs.name);
            else
                return 0;
        }
    }
    */

    /*
    private static class ContactGroupsInEvent {
        Event event = null;
        String groups = null;
        //int sensorType = -1;
    }
    */

}
