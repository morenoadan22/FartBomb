package com.moreno.fartbomb.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.HashMap;

public class FartBombProvider extends ContentProvider {
	private static class DatabaseHelper extends SQLiteOpenHelper {
		private DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		public void dropDatabase(SQLiteDatabase db) {
			db.execSQL("DROP TABLE IF EXISTS " + FartBomb.Users.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + FartBomb.Friends.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + FartBomb.Bombs.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + FartBomb.FriendRequests.TABLE_NAME);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database
		 * .sqlite.SQLiteDatabase)
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(FartBomb.Users.getCreateQuery());
			db.execSQL(FartBomb.Friends.getCreateQuery());
			db.execSQL(FartBomb.Bombs.getCreateQuery());
			db.execSQL(FartBomb.FriendRequests.getCreateQuery());
		}

		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			dropDatabase(db);
			onCreate(db);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database
		 * .sqlite.SQLiteDatabase, int, int)
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			dropDatabase(db);
			onCreate(db);
		}

	}

	@SuppressWarnings("unused")
	private static final String LOG_TAG = FartBombProvider.class.getSimpleName();

	private static final String DATABASE_NAME = "fartbomb.db";
	private static final int DATABASE_VERSION = 3;

	private static final int USERS = 1;
	private static final int FRIENDS = 2;
	private static final int BOMBS = 3;
	private static final int FRIEND_REQUESTS = 4;

	private DatabaseHelper mFbHelper;

	private static final UriMatcher uriMatcher;
	private static final HashMap<String, String> sUsersProjectionMap;
	private static final HashMap<String, String> sFriendsProjectionMap;
	private static final HashMap<String, String> sBombsProjectionMap;
	private static final HashMap<String, String> sRequestsProjectionMap;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(FartBomb.AUTHORITY, FartBomb.Users.TABLE_NAME, USERS);
		uriMatcher.addURI(FartBomb.AUTHORITY, FartBomb.Friends.TABLE_NAME, FRIENDS);
		uriMatcher.addURI(FartBomb.AUTHORITY, FartBomb.Bombs.TABLE_NAME, BOMBS);
		uriMatcher.addURI(FartBomb.AUTHORITY, FartBomb.FriendRequests.TABLE_NAME, FRIEND_REQUESTS);

		sUsersProjectionMap = new HashMap<String, String>();
		sUsersProjectionMap.put(FartBomb.Users._ID, FartBomb.Users._ID);
		sUsersProjectionMap.put(FartBomb.Users.FIELD_USER_ID, FartBomb.Users.FIELD_USER_ID);
		sUsersProjectionMap.put(FartBomb.Users.FIELD_USERNAME, FartBomb.Users.FIELD_USERNAME);
		sUsersProjectionMap.put(FartBomb.Users.FIELD_EMAIL, FartBomb.Users.FIELD_EMAIL);
		sUsersProjectionMap.put(FartBomb.Users.FIELD_PASSWORD, FartBomb.Users.FIELD_PASSWORD);
		sUsersProjectionMap.put(FartBomb.Users.FIELD_PHONE_NUMBER, FartBomb.Users.FIELD_PHONE_NUMBER);
		sUsersProjectionMap.put(FartBomb.Users.FIELD_USER_RANK, FartBomb.Users.FIELD_USER_RANK);
		sUsersProjectionMap.put(FartBomb.Users.FIELD_IS_ACTIVE, FartBomb.Users.FIELD_IS_ACTIVE);

		sFriendsProjectionMap = new HashMap<String, String>();
		sFriendsProjectionMap.put(FartBomb.Friends._ID, FartBomb.Friends._ID);
		sFriendsProjectionMap.put(FartBomb.Friends.FIELD_FRIEND_ID, FartBomb.Friends.FIELD_FRIEND_ID);
		sFriendsProjectionMap.put(FartBomb.Friends.FIELD_USER_ID, FartBomb.Friends.FIELD_USER_ID);
		sFriendsProjectionMap.put(FartBomb.Friends.FIELD_FRIEND_NAME, FartBomb.Friends.FIELD_FRIEND_NAME);
		sFriendsProjectionMap.put(FartBomb.Friends.FIELD_FRIEND_EMAIL, FartBomb.Friends.FIELD_FRIEND_EMAIL);
		sFriendsProjectionMap.put(FartBomb.Friends.FIELD_FRIEND_PHONE, FartBomb.Friends.FIELD_FRIEND_PHONE);
		sFriendsProjectionMap.put(FartBomb.Friends.FIELD_BOMB_COUNT, FartBomb.Friends.FIELD_BOMB_COUNT);
		sFriendsProjectionMap.put(FartBomb.Friends.FIELD_RATING_SUM, FartBomb.Friends.FIELD_RATING_SUM);
		sFriendsProjectionMap.put(FartBomb.Friends.FIELD_IS_ACCEPTED, FartBomb.Friends.FIELD_IS_ACCEPTED);

		sBombsProjectionMap = new HashMap<String, String>();
		sBombsProjectionMap.put(FartBomb.Bombs._ID, FartBomb.Bombs._ID);
		sBombsProjectionMap.put(FartBomb.Bombs.FIELD_BOMB_ID, FartBomb.Bombs.FIELD_BOMB_ID);
		sBombsProjectionMap.put(FartBomb.Bombs.FIELD_NAME, FartBomb.Bombs.FIELD_NAME);
		sBombsProjectionMap.put(FartBomb.Bombs.FIELD_USER_ID, FartBomb.Bombs.FIELD_USER_ID);
		sBombsProjectionMap.put(FartBomb.Bombs.FIELD_RATING, FartBomb.Bombs.FIELD_RATING);
		sBombsProjectionMap.put(FartBomb.Bombs.FIELD_FILE_NAME, FartBomb.Bombs.FIELD_FILE_NAME);
		sBombsProjectionMap.put(FartBomb.Bombs.FIELD_LENGTH, FartBomb.Bombs.FIELD_LENGTH);
		sBombsProjectionMap.put(FartBomb.Bombs.FIELD_TIME_STAMP, FartBomb.Bombs.FIELD_TIME_STAMP);
		sBombsProjectionMap.put(FartBomb.Bombs.FIELD_BYTE_ARRAY, FartBomb.Bombs.FIELD_BYTE_ARRAY);

		sRequestsProjectionMap = new HashMap<String, String>();
		sRequestsProjectionMap.put(FartBomb.FriendRequests._ID, FartBomb.FriendRequests._ID);
		sRequestsProjectionMap.put(FartBomb.FriendRequests.FIELD_USER_ID, FartBomb.FriendRequests.FIELD_USER_ID);
		sRequestsProjectionMap.put(FartBomb.FriendRequests.FIELD_FRIEND_ID, FartBomb.FriendRequests.FIELD_FRIEND_ID);
		sRequestsProjectionMap.put(FartBomb.FriendRequests.FIELD_NOTIFIED, FartBomb.FriendRequests.FIELD_NOTIFIED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#delete(android.net.Uri,
	 * java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mFbHelper.getWritableDatabase();
		int count;

		switch (uriMatcher.match(uri)) {
		case USERS:
			count = db.delete(FartBomb.Users.TABLE_NAME, selection, selectionArgs);
			break;
		case FRIENDS:
			count = db.delete(FartBomb.Friends.TABLE_NAME, selection, selectionArgs);
			break;
		case BOMBS:
			count = db.delete(FartBomb.Bombs.TABLE_NAME, selection, selectionArgs);
			break;
		case FRIEND_REQUESTS:
			count = db.delete(FartBomb.FriendRequests.TABLE_NAME, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case USERS:
			return FartBomb.Users.CONTENT_TYPE;
		case FRIENDS:
			return FartBomb.Friends.CONTENT_TYPE;
		case BOMBS:
			return FartBomb.Bombs.CONTENT_TYPE;
		case FRIEND_REQUESTS:
			return FartBomb.FriendRequests.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#insert(android.net.Uri,
	 * android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues contentValues) {
		SQLiteDatabase db = mFbHelper.getWritableDatabase();

		ContentValues values;
		if (contentValues != null) {
			values = new ContentValues(contentValues);
		} else {
			values = new ContentValues();
		}

		long rowId;

		switch (uriMatcher.match(uri)) {
		case USERS:
			rowId = db.insert(FartBomb.Users.TABLE_NAME, FartBomb.Users.FIELD_USERNAME, values);
			break;
		case FRIENDS:
			rowId = db.insert(FartBomb.Friends.TABLE_NAME, FartBomb.Friends.FIELD_FRIEND_NAME, values);
			break;
		case BOMBS:
			rowId = db.insert(FartBomb.Bombs.TABLE_NAME, FartBomb.Bombs.FIELD_USER_ID, values);
			break;
		case FRIEND_REQUESTS:
			rowId = db.insert(FartBomb.FriendRequests.TABLE_NAME, FartBomb.FriendRequests.FIELD_USER_ID, values);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

		if (rowId > 0) {
			uri = ContentUris.withAppendedId(uri, rowId);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		} else {
			throw new SQLException("Failed to insert row into " + uri);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		mFbHelper = new DatabaseHelper(getContext());
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#query(android.net.Uri,
	 * java.lang.String[], java.lang.String, java.lang.String[],
	 * java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (uriMatcher.match(uri)) {
		case USERS:
			qb.setTables(FartBomb.Users.TABLE_NAME);
			break;
		case FRIENDS:
			qb.setTables(FartBomb.Friends.TABLE_NAME);
			break;
		case BOMBS:
			qb.setTables(FartBomb.Bombs.TABLE_NAME);
			break;
		case FRIEND_REQUESTS:
			qb.setTables(FartBomb.FriendRequests.TABLE_NAME);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

		SQLiteDatabase db = mFbHelper.getReadableDatabase();

		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);

		return c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#update(android.net.Uri,
	 * android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mFbHelper.getWritableDatabase();
		int count;

		switch (uriMatcher.match(uri)) {
		case USERS:
			count = db.update(FartBomb.Users.TABLE_NAME, contentValues, selection, selectionArgs);
			break;
		case FRIENDS:
			count = db.update(FartBomb.Friends.TABLE_NAME, contentValues, selection, selectionArgs);
			break;
		case BOMBS:
			count = db.update(FartBomb.Bombs.TABLE_NAME, contentValues, selection, selectionArgs);
			break;
		case FRIEND_REQUESTS:
			count = db.update(FartBomb.FriendRequests.TABLE_NAME, contentValues, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
