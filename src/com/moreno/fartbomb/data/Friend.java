package com.moreno.fartbomb.data;

import org.json.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.moreno.fartbomb.provider.FartBomb;

public class Friend {
    public static final String JSON_USER_ID = "userId";
    public static final String JSON_FRIEND_NAME = "friendName";
    public static final String JSON_FRIEND_ID = "friendId";
    public static final String JSON_FRIEND_EMAIL = "friendEmail";
    public static final String JSON_FRIEND_PHONE = "friendPhone";
    public static final String JSON_BOMB_COUNT = "bombCount";
    public static final String JSON_RATING_SUM = "ratingSum";
    private static final String LOG_TAG = "Friend";

    public static Friend fromJSON(JSONObject jsonRow) {
        Friend friend = new Friend();
        try {
            friend.setId(jsonRow.getInt(JSON_FRIEND_ID));
            friend.setUserId(jsonRow.getInt(JSON_USER_ID));
            friend.setName(jsonRow.getString(JSON_FRIEND_NAME));
            friend.setEmail(jsonRow.getString(JSON_FRIEND_EMAIL));
            friend.setPhone(jsonRow.getLong(JSON_FRIEND_PHONE));
            friend.setBombCount(jsonRow.getInt(JSON_BOMB_COUNT));
            friend.setRatingSum(jsonRow.getDouble(JSON_RATING_SUM));
        } catch (JSONException je) {
            friend = new Friend();
        }
        return friend;
    }

    public static Friend getEmpty() {
        return new Friend();
    }

    private String name;

    private int id;

    private int userId;
    private String email;
    private long phone;
    private int bombCount;
    private double ratingSum;
    private boolean isAccepted;

    private Friend() {

    }

    public Friend(Cursor c) {
        setId(c.getInt(c.getColumnIndexOrThrow(FartBomb.Friends.FIELD_FRIEND_ID)));
        setName(c.getString(c.getColumnIndexOrThrow(FartBomb.Friends.FIELD_FRIEND_NAME)));
        setUserId(c.getInt(c.getColumnIndexOrThrow(FartBomb.Friends.FIELD_USER_ID)));
        setEmail(c.getString(c.getColumnIndexOrThrow(FartBomb.Friends.FIELD_FRIEND_EMAIL)));
        setPhone(c.getLong(c.getColumnIndexOrThrow(FartBomb.Friends.FIELD_FRIEND_PHONE)));
        setBombCount(c.getInt(c.getColumnIndexOrThrow(FartBomb.Friends.FIELD_BOMB_COUNT)));
        setRatingSum(c.getDouble(c.getColumnIndexOrThrow(FartBomb.Friends.FIELD_RATING_SUM)));
        setAccepted(c.getInt(c.getColumnIndexOrThrow(FartBomb.Friends.FIELD_IS_ACCEPTED)) == 1 ? true : false);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Friend) {
            Friend f = (Friend) o;

            return getId() == f.getId() //
                    && this.getEmail().equals(f.getEmail()) //
                    && this.getName().equals(f.getName())//
                    && this.getPhone() == f.getPhone()//
                    && this.getRatingSum() == f.getRatingSum()//
                    && this.getBombCount() == f.getBombCount()//
            ;
        }
        return false;
    }

    public int getBombCount() {
        return bombCount;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(FartBomb.Friends.FIELD_FRIEND_ID, getId());
        values.put(FartBomb.Friends.FIELD_FRIEND_EMAIL, getEmail());
        values.put(FartBomb.Friends.FIELD_FRIEND_NAME, getName());
        values.put(FartBomb.Friends.FIELD_USER_ID, getUserId());
        values.put(FartBomb.Friends.FIELD_FRIEND_PHONE, getPhone());
        values.put(FartBomb.Friends.FIELD_IS_ACCEPTED, isAccepted());
        values.put(FartBomb.Friends.FIELD_BOMB_COUNT, getBombCount());
        values.put(FartBomb.Friends.FIELD_RATING_SUM, getRatingSum());
        values.put(FartBomb.Friends.FIELD_IS_ACCEPTED, isAccepted() ? 1 : 0);
        return values;
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public long getPhone() {
        return phone;
    }

    public double getRatingSum() {
        return ratingSum;
    }

    public int getUserId() {
        return userId;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean isAccepted) {
        this.isAccepted = isAccepted;
    }

    public void setBombCount(int bombCount) {
        this.bombCount = bombCount;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(long phone) {
        this.phone = phone;
    }

    public void setRatingSum(double ratingSum) {
        this.ratingSum = ratingSum;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public JSONObject toJSON() {
        JSONObject jo = new JSONObject();
        try {
            jo.put(JSON_USER_ID, getUserId());
            jo.put(JSON_FRIEND_NAME, getName());
            jo.put(JSON_FRIEND_ID, getId());
            jo.put(JSON_FRIEND_EMAIL, getEmail());
            jo.put(JSON_FRIEND_PHONE, getPhone());
            jo.put(JSON_BOMB_COUNT, getBombCount());
            jo.put(JSON_RATING_SUM, getRatingSum());

        } catch (JSONException je) {
            Log.e(LOG_TAG, "Friend.toJSON()", je);
        }

        return jo;
    }
}
