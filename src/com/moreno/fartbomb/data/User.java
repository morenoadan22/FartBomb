package com.moreno.fartbomb.data;

import org.json.*;

import android.content.ContentValues;
import android.database.Cursor;

import com.moreno.fartbomb.provider.FartBomb;

/**
 * 
 * User
 * 
 * @author adan
 */
public class User {
    public static User fromJSON(JSONObject json) {
        User user = new User();
        try {
            user.setUserId(json.getInt(FartBomb.Users.FIELD_USER_ID));
            user.setUserName(json.getString(FartBomb.Users.FIELD_USERNAME));
            user.setEmail(json.getString(FartBomb.Users.FIELD_EMAIL));
            user.setPassword(json.getString(FartBomb.Users.FIELD_PASSWORD));
            user.setPhoneNumber(json.getLong(FartBomb.Users.FIELD_PHONE_NUMBER));
            if (json.has(FartBomb.Users.FIELD_IS_ACTIVE)) {
                user.setActive(json.getInt(FartBomb.Users.FIELD_IS_ACTIVE) == 1 ? true : false);
            }
            if (json.has(FartBomb.Users.FIELD_USER_RANK)) {
                user.setRank(json.getInt(FartBomb.Users.FIELD_USER_RANK));
            }

        } catch (JSONException je) {
            je.printStackTrace();
            user = new User();
        }
        return user;
    }

    public static User fromJSON(String json) {
        User user;
        try {
            JSONObject jo = new JSONObject(json);
            user = User.fromJSON(jo);
        } catch (JSONException je) {
            user = new User();
        }

        return user;
    }

    public static User newNull() {
        return new User();
    }

    private int userId;
    private String userName;
    private String email;
    private String password;
    private long phoneNumber;
    private boolean isActive;
    private int rank;

    public User() {

    }

    public User(Cursor c) {
        setUserId(c.getInt(c.getColumnIndexOrThrow(FartBomb.Users.FIELD_USER_ID)));
        setUserName(c.getString(c.getColumnIndexOrThrow(FartBomb.Users.FIELD_USERNAME)));
        setEmail(c.getString(c.getColumnIndexOrThrow(FartBomb.Users.FIELD_EMAIL)));
        setPassword(c.getString(c.getColumnIndexOrThrow(FartBomb.Users.FIELD_PASSWORD)));
        setPhoneNumber(c.getLong(c.getColumnIndexOrThrow(FartBomb.Users.FIELD_PHONE_NUMBER)));
        setActive(c.getInt(c.getColumnIndexOrThrow(FartBomb.Users.FIELD_IS_ACTIVE)) == 1 ? true : false);
        setRank(c.getInt(c.getColumnIndexOrThrow(FartBomb.Users.FIELD_USER_RANK)));
    }

    public User(String userName, String email, String password, long phoneNumber, boolean isActive) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.isActive = isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof User) {
            User u = (User) o;
            return getUserId() == u.getUserId() //
                    && getUserName().equals(u.getUserName()) //
                    && getEmail().equals(u.getEmail())//
                    && getPassword().equals(u.getPassword())//
                    && getPhoneNumber() == u.getPhoneNumber() //
                    && isActive() == u.isActive() //
                    && getRank() == u.getRank()//
            ;
        }

        return false;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(FartBomb.Users.FIELD_USER_ID, getUserId());
        values.put(FartBomb.Users.FIELD_USERNAME, getUserName());
        values.put(FartBomb.Users.FIELD_EMAIL, getEmail());
        values.put(FartBomb.Users.FIELD_PASSWORD, getPassword());
        values.put(FartBomb.Users.FIELD_PHONE_NUMBER, getPhoneNumber());
        values.put(FartBomb.Users.FIELD_IS_ACTIVE, isActive() ? 1 : 0);
        values.put(FartBomb.Users.FIELD_USER_RANK, getRank());

        return values;
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public JSONObject getJSON() {
        JSONObject jo = new JSONObject();
        try {
            jo.put(FartBomb.Users.FIELD_USER_ID, getUserId());
            jo.put(FartBomb.Users.FIELD_USERNAME, getUserName());
            jo.put(FartBomb.Users.FIELD_EMAIL, getEmail());
            jo.put(FartBomb.Users.FIELD_PASSWORD, getPassword());
            jo.put(FartBomb.Users.FIELD_PHONE_NUMBER, getPhoneNumber());
            jo.put(FartBomb.Users.FIELD_IS_ACTIVE, isActive() ? 1 : 0);
            jo.put(FartBomb.Users.FIELD_USER_RANK, getRank());
        } catch (JSONException je) {
            jo = new JSONObject();
        }

        return jo;
    }

    public String getPassword() {
        return password != null ? password : "";
    }

    public long getPhoneNumber() {
        return phoneNumber;
    }

    public int getRank() {
        return rank;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName != null ? userName : "";
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhoneNumber(long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setUserId(int id) {
        this.userId = id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
