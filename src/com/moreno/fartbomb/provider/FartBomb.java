package com.moreno.fartbomb.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class FartBomb {

	public static final class Bombs implements BaseColumns {
		public static final String TABLE_NAME = "bombs";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_TYPE_PREFIX + TABLE_NAME;

		public static final String FIELD_BOMB_ID = "bombId";
		public static final String FIELD_NAME = "name";
		public static final String FIELD_USER_ID = "userId";
		public static final String FIELD_LENGTH = "length";
		public static final String FIELD_FILE_NAME = "fileName";
		public static final String FIELD_RATING = "rating";
		public static final String FIELD_TIME_STAMP = "timeStamp";
		public static final String FIELD_BYTE_ARRAY = "bytes";

		public static String getCreateQuery() {
			return "CREATE TABLE " + TABLE_NAME + " (" //
					+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT" //
					+ "," + FIELD_BOMB_ID + " INTEGER" //
					+ "," + FIELD_NAME + " TEXT" //
					+ "," + FIELD_LENGTH + " LONG" //
					+ "," + FIELD_USER_ID + " INTEGER" //
					+ "," + FIELD_FILE_NAME + " TEXT" //
					+ "," + FIELD_RATING + " DOUBLE" //
					+ "," + FIELD_TIME_STAMP + " LONG" //
					+ "," + FIELD_BYTE_ARRAY + " BLOB"//
					+ ", CONSTRAINT bombidCK UNIQUE (" + FIELD_BOMB_ID + ") ON CONFLICT REPLACE" //
					+ ");";
		}

		private Bombs() {
		}
	}

	public static final class FriendRequests implements BaseColumns {
		public static final String TABLE_NAME = "friendRequests";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_TYPE_PREFIX + TABLE_NAME;

		public static final String FIELD_USER_ID = "userId";
		public static final String FIELD_FRIEND_ID = "friendId";
		public static final String FIELD_NOTIFIED = "notified";

		public static String getCreateQuery() {
			return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" //
					+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT" //
					+ "," + FIELD_USER_ID + " INTEGER" //
					+ "," + FIELD_FRIEND_ID + " INTEGER" //
					+ "," + FIELD_NOTIFIED + " INTEGER" //
					+ ", CONSTRAINT requestCK UNIQUE (" + FIELD_USER_ID + "," + FIELD_FRIEND_ID + ") ON CONFLICT IGNORE"//
					+ ");";
		}

		private FriendRequests() {

		}
	}

	public static final class Friends implements BaseColumns {
		public static final String TABLE_NAME = "friends";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_TYPE_PREFIX + TABLE_NAME;

		public static final String FIELD_FRIEND_NAME = "friendName";
		public static final String FIELD_FRIEND_ID = "friendId";
		public static final String FIELD_USER_ID = "userId";
		public static final String FIELD_FRIEND_EMAIL = "friendEmail";
		public static final String FIELD_FRIEND_PHONE = "friendPhone";
		public static final String FIELD_BOMB_COUNT = "bombCount";
		public static final String FIELD_RATING_SUM = "ratingSum";
		public static final String FIELD_IS_ACCEPTED = "isAccepted";

		public static String getCreateQuery() {
			return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" //
					+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT" //
					+ "," + FIELD_FRIEND_ID + " INTEGER" //
					+ "," + FIELD_USER_ID + " INTEGER" //
					+ "," + FIELD_FRIEND_NAME + " TEXT" //
					+ "," + FIELD_FRIEND_EMAIL + " TEXT" //
					+ "," + FIELD_FRIEND_PHONE + " LONG" //
					+ "," + FIELD_BOMB_COUNT + " INTEGER"//
					+ "," + FIELD_RATING_SUM + " REAL" //
					+ "," + FIELD_IS_ACCEPTED + " INTEGER"//
					+ ", CONSTRAINT friendCK UNIQUE (" + FIELD_FRIEND_NAME + ") ON CONFLICT REPLACE" //
					+ ");";
		}

		private Friends() {

		}

	}

	public static final class Users implements BaseColumns {
		public static final String TABLE_NAME = "users";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_TYPE_PREFIX + TABLE_NAME;

		public static final String FIELD_USER_ID = "id";
		public static final String FIELD_USERNAME = "userName";
		public static final String FIELD_EMAIL = "email";
		public static final String FIELD_PASSWORD = "password";
		public static final String FIELD_PHONE_NUMBER = "phoneNumber";
		public static final String FIELD_IS_ACTIVE = "isActive";
		public static final String FIELD_USER_RANK = "userRank";

		public static String getCreateQuery() {
			return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" //
					+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT" //
					+ "," + FIELD_USER_ID + " INTEGER" //
					+ "," + FIELD_USERNAME + " TEXT" //
					+ "," + FIELD_EMAIL + " TEXT" //
					+ "," + FIELD_PASSWORD + " TEXT" //
					+ "," + FIELD_PHONE_NUMBER + " LONG" //
					+ "," + FIELD_USER_RANK + " INTEGER" //
					+ "," + FIELD_IS_ACTIVE + " INTEGER" //
					+ ", CONSTRAINT usernameCK UNIQUE (" + FIELD_USERNAME + ") ON CONFLICT REPLACE"//
					+ ");";
		}

		private Users() {

		}
	}

	private static final String CONTENT_TYPE_PREFIX = "/vnd.fartbomb.";

	public static final String AUTHORITY = "com.moreno.fartbomb.provider.FartBombProvider";

	private FartBomb() {

	}
}
