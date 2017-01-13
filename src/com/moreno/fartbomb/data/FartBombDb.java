package com.moreno.fartbomb.data;

import android.app.*;
import android.content.*;
import android.database.*;

import com.moreno.fartbomb.*;
import com.moreno.fartbomb.network.SyncHandler;
import com.moreno.fartbomb.provider.FartBomb;

public class FartBombDb {
    private static final FartBombDb instance = new FartBombDb();

    public static FartBombDb getInstance() {
        return instance;
    }

    private FartBombDb() {
    }

    public void acceptFriend(ContentResolver contentResolver, Friend friend) {
        String where = FartBomb.Friends.FIELD_FRIEND_ID + "=" + friend.getId();
        contentResolver.update(FartBomb.Friends.CONTENT_URI, friend.getContentValues(), where, null);
    }

    public void addBomb(ContentResolver contentResolver, Bomb bomb) {
        contentResolver.insert(FartBomb.Bombs.CONTENT_URI, bomb.getContentValues());
    }

    public void addFriend(ContentResolver contentResolver, Friend friend) {
        contentResolver.insert(FartBomb.Friends.CONTENT_URI, friend.getContentValues());
    }

    public void addFriendRequest(ContentResolver contentResolver, int friendId) {
        ContentValues values = new ContentValues();
        values.put(FartBomb.FriendRequests.FIELD_FRIEND_ID, getActiveUser(contentResolver).getUserId());
        values.put(FartBomb.FriendRequests.FIELD_USER_ID, friendId);
        values.put(FartBomb.FriendRequests.FIELD_NOTIFIED, 1);
        try {
            contentResolver.insert(FartBomb.FriendRequests.CONTENT_URI, values);
        } catch (SQLException sqe) {

        }
    }

    public void addFriendRequest(Context context, ContentResolver contentResolver, Friend friend) {
        ContentValues values = new ContentValues();
        values.put(FartBomb.FriendRequests.FIELD_FRIEND_ID, friend.getId());
        values.put(FartBomb.FriendRequests.FIELD_USER_ID, friend.getUserId());
        values.put(FartBomb.FriendRequests.FIELD_NOTIFIED, 1);
        try {
            contentResolver.insert(FartBomb.FriendRequests.CONTENT_URI, values);
            Intent intent = new Intent();
            intent.setAction(SyncHandler.BROADCAST_FRIEND_REQUEST_RECEIVED);
            context.sendBroadcast(intent);
            this.pushMessageNotification(context);
        } catch (SQLException sqe) {

        }
    }

    /**
     * Add a user to the db
     * 
     * @param contentResolver the content resolver to use.
     * @param user the user to insert into the db.
     * @return the user id of the new user
     */
    public int addUser(ContentResolver contentResolver, User user) {
        ContentValues values = user.getContentValues();
        contentResolver.insert(FartBomb.Users.CONTENT_URI, values);
        return authenticateUser(contentResolver, user);
    }

    /**
     * Validates the user
     * 
     * @param contentResolver
     * @param user
     * @return the userId of the user
     */
    public int authenticateUser(ContentResolver contentResolver, User user) {
        int userId = -1;
        String where = FartBomb.Users.FIELD_USERNAME + " = '" + user.getUserName() + "' AND " //
                + FartBomb.Users.FIELD_PASSWORD + " = '" + user.getPassword() + "'" //
        ;
        Cursor c = contentResolver.query(FartBomb.Users.CONTENT_URI, null, where, null, null);
        if (c != null && c.moveToFirst()) {
            userId = c.getInt(c.getColumnIndexOrThrow(FartBomb.Users.FIELD_USER_ID));
        }
        c.close();

        return userId;
    }

    public void clearSyncTables(ContentResolver contentResolver) {
        int userId = getActiveUser(contentResolver).getUserId();
        String fWhere = FartBomb.Friends.FIELD_USER_ID + "=" + userId;
        String bWhere = FartBomb.Bombs.FIELD_USER_ID + "=" + userId;
        contentResolver.delete(FartBomb.Friends.CONTENT_URI, fWhere, null);
        contentResolver.delete(FartBomb.Bombs.CONTENT_URI, bWhere, null);
    }

    public void denyFriend(ContentResolver contentResolver, Friend friend) {
        String where = FartBomb.Friends.FIELD_FRIEND_ID + "=" + friend.getId();
        contentResolver.delete(FartBomb.Friends.CONTENT_URI, where, null);

    }

    public User getActiveUser(ContentResolver contentResolver) {
        User user = User.newNull();

        String where = FartBomb.Users.FIELD_IS_ACTIVE + " = " + 1;

        Cursor c = contentResolver.query(FartBomb.Users.CONTENT_URI, null, where, null, null);

        if (c != null && c.moveToFirst()) {
            user = new User(c);
        }
        c.close();

        return user;

    }

    public Cursor getBombs(ContentResolver contentResolver, int userId, String orderBy) {
        String where = FartBomb.Bombs.FIELD_USER_ID + "=" + userId;
        return contentResolver.query(FartBomb.Bombs.CONTENT_URI, null, where, null, null);
    }

    public int getBombsCount(ContentResolver contentResolver, int userId) {
        String[] projection = new String[] { "count(*) AS count" };
        String where = FartBomb.Bombs.FIELD_USER_ID + " = " + userId;
        Cursor countCursor = contentResolver.query(FartBomb.Bombs.CONTENT_URI, projection, where, null, null);
        countCursor.moveToFirst();
        int count = countCursor.getInt(0);
        countCursor.close();
        return count;
    }

    public Friend getFriend(ContentResolver contentResolver, int friendId) {
        String where = FartBomb.Friends.FIELD_FRIEND_ID + " = " + friendId;
        Cursor friendCursor = contentResolver.query(FartBomb.Friends.CONTENT_URI, null, where, null, null);
        Friend friend;
        if (friendCursor != null && friendCursor.moveToNext()) {
            friend = new Friend(friendCursor);
            friendCursor.close();
        } else {
            friend = Friend.getEmpty();
        }
        return friend;
    }

    public int getFriendCount(ContentResolver contentResolver, int userId) {
        String[] projection = new String[] { "count(*) AS count" };
        String where = FartBomb.Friends.FIELD_USER_ID + " = " + userId;
        Cursor countCursor = contentResolver.query(FartBomb.Friends.CONTENT_URI, projection, where, null, null);
        countCursor.moveToFirst();
        int count = countCursor.getInt(0);
        countCursor.close();
        return count;
    }

    public int getFriendRequestCount(ContentResolver contentResolver, int userId) {
        int numOfRequests = 0;
        String[] projection = new String[] { "count(*) AS count" };
        String where = FartBomb.FriendRequests.FIELD_USER_ID + "=" + userId;
        Cursor countCursor = contentResolver.query(FartBomb.FriendRequests.CONTENT_URI, projection, where, null, null);
        countCursor.moveToFirst();
        numOfRequests = countCursor.getInt(0);
        countCursor.close();
        return numOfRequests;
    }

    public Cursor getFriends(ContentResolver contentResolver, int userId) {
        String where = FartBomb.Friends.FIELD_USER_ID + " = " + userId;
        String orderBy = FartBomb.Friends.FIELD_IS_ACCEPTED + " ASC, " + FartBomb.Friends.FIELD_FRIEND_NAME + " ASC";
        return contentResolver.query(FartBomb.Friends.CONTENT_URI, null, where, null, orderBy);
    }

    public double getRatingSum(ContentResolver contentResolver, int userId) {
        String[] projection = new String[] { "SUM( " + FartBomb.Bombs.FIELD_RATING + ")" };
        String where = FartBomb.Bombs.FIELD_USER_ID + " = " + userId;
        Cursor sumCursor = contentResolver.query(FartBomb.Bombs.CONTENT_URI, projection, where, null, null);
        sumCursor.moveToFirst();
        double sum = sumCursor.getDouble(0);
        sumCursor.close();
        return sum;
    }

    public boolean isFriendRequest(ContentResolver contentResolver, int friendId) {
        boolean isFriendRequest = false;
        String where = FartBomb.FriendRequests.FIELD_FRIEND_ID + " = " + friendId //
                + " AND " + FartBomb.FriendRequests.FIELD_USER_ID + " = " + getActiveUser(contentResolver).getUserId()//
        ;
        Cursor c = contentResolver.query(FartBomb.FriendRequests.CONTENT_URI, null, where, null, null);

        if (c != null && c.moveToNext()) {
            isFriendRequest = true;
        }
        c.close();

        return isFriendRequest;
    }

    public void logoff(ContentResolver contentResolver) {
        ContentValues values = new ContentValues();
        values.put(FartBomb.Users.FIELD_IS_ACTIVE, 0);
        String where = FartBomb.Users.FIELD_IS_ACTIVE + " = 1";
        contentResolver.update(FartBomb.Users.CONTENT_URI, values, where, null);
        contentResolver.delete(FartBomb.Friends.CONTENT_URI, null, null);
        contentResolver.delete(FartBomb.FriendRequests.CONTENT_URI, null, null);
        contentResolver.delete(FartBomb.Bombs.CONTENT_URI, null, null);
    }

    @SuppressWarnings("deprecation")
    private void pushMessageNotification(Context context) {
        final SharedPreferences sharedPreferences = SettingsActivity.getSharedPreference();
        boolean allowNotification = sharedPreferences.getBoolean(SettingsActivity.PREFERENCE_NOTIFICATIONS, true);
        if (allowNotification) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification note = new Notification(R.drawable.fartbomb_icon, "You have a new friend request!", System.currentTimeMillis());
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, ProfileActivity.class), 0);
            note.setLatestEventInfo(context, "FartBomb Message", "You have a new friend request!", pendingIntent);
            notificationManager.notify(6, note);
        }
    }

    public void removeFriendRequest(ContentResolver contentResolver, Friend friend) {
        String where = FartBomb.FriendRequests.FIELD_FRIEND_ID + "=" + friend.getId() //
                + " AND " + FartBomb.FriendRequests.FIELD_USER_ID + "=" + friend.getUserId()//
        ;

        contentResolver.delete(FartBomb.FriendRequests.CONTENT_URI, where, null);
    }

    public void setUser(ContentResolver contentResolver, User user) {
        User activeUser = getActiveUser(contentResolver);
        if (!user.equals(activeUser)) {
            activeUser = user;
            activeUser.setActive(true);
            String where = FartBomb.Users.FIELD_USER_ID + "=" + activeUser.getUserId();
            contentResolver.update(FartBomb.Users.CONTENT_URI, activeUser.getContentValues(), where, null);
        }
    }

    public void setUserActive(ContentResolver contentResolver, User user) {
        String where = FartBomb.Users.FIELD_USER_ID + " = " + user.getUserId();
        Cursor c = contentResolver.query(FartBomb.Users.CONTENT_URI, null, where, null, null);
        if (c == null || !c.moveToFirst()) {
            addUser(contentResolver, user);
        }
        c.close();

        ContentValues values = new ContentValues();
        values.put(FartBomb.Users.FIELD_IS_ACTIVE, 1);
        contentResolver.update(FartBomb.Users.CONTENT_URI, values, where, null);
    }

    public void updateRanking(ContentResolver contentResolver, int rank) {
        User user = getActiveUser(contentResolver);
        user.setRank(rank);
        String where = FartBomb.Users.FIELD_USER_ID + "=" + user.getUserId();
        contentResolver.update(FartBomb.Users.CONTENT_URI, user.getContentValues(), where, null);
    }
}
