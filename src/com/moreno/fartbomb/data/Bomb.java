package com.moreno.fartbomb.data;

import java.io.IOException;
import java.util.Date;

import org.json.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.moreno.fartbomb.provider.FartBomb;
import com.moreno.fartbomb.util.Base64;

public class Bomb {
    public static final String JSON_USER_ID = "userId";
    public static final String JSON_BOMB_ID = "bombId";
    public static final String JSON_NAME = "name";
    public static final String JSON_LENGTH = "length";
    public static final String JSON_FILE_NAME = "fileName";
    public static final String JSON_RATING = "rating";
    public static final String JSON_TIME_STAMP = "timeStamp";
    public static final String JSON_BYTE_ARRAY = "bytes";

    public static Bomb fromJSON(JSONObject jo) {
        Bomb bomb = new Bomb();
        try {
            bomb.setId(jo.getInt(JSON_BOMB_ID));
            bomb.setUserId(jo.getInt(JSON_USER_ID));
            bomb.setName(jo.getString(JSON_NAME));
            bomb.setLength(jo.getLong(JSON_LENGTH));
            bomb.setFileName(jo.getString(JSON_FILE_NAME));
            bomb.setRating(jo.getDouble(JSON_RATING));
            bomb.setTimeStamp(jo.getLong(JSON_TIME_STAMP));
        } catch (JSONException je) {
            bomb = new Bomb();
            Log.e("Bomb.fromJSON()", jo.toString(), je);
        }

        return bomb;
    }

    private int id;
    private int userId;
    private String name;
    private long length;
    private String fileName;
    private double rating;
    private long timeStamp;
    private byte[] audioBytes;

    private Bomb() {

    }

    public Bomb(Cursor c) {
        setId(c.getInt(c.getColumnIndexOrThrow(FartBomb.Bombs.FIELD_BOMB_ID)));
        setUserId(c.getInt(c.getColumnIndexOrThrow(FartBomb.Bombs.FIELD_USER_ID)));
        setName(c.getString(c.getColumnIndexOrThrow(FartBomb.Bombs.FIELD_NAME)));
        setLength(c.getLong(c.getColumnIndexOrThrow(FartBomb.Bombs.FIELD_LENGTH)));
        setFileName(c.getString(c.getColumnIndexOrThrow(JSON_FILE_NAME)));
        setRating(c.getDouble(c.getColumnIndexOrThrow(FartBomb.Bombs.FIELD_RATING)));
        setTimeStamp(c.getLong(c.getColumnIndexOrThrow(FartBomb.Bombs.FIELD_TIME_STAMP)));
        setAudioBytes(c.getBlob(c.getColumnIndexOrThrow(FartBomb.Bombs.FIELD_BYTE_ARRAY)));
    }

    public Bomb(String filePath) {
        setFileName(filePath);
    }

    public byte[] getAudioBytes() {
        return audioBytes != null ? audioBytes : new byte[1024];
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(FartBomb.Bombs.FIELD_BOMB_ID, getId());
        values.put(FartBomb.Bombs.FIELD_USER_ID, getUserId());
        values.put(FartBomb.Bombs.FIELD_NAME, getName());
        values.put(FartBomb.Bombs.FIELD_LENGTH, getLength());
        values.put(FartBomb.Bombs.FIELD_FILE_NAME, getFileName());
        values.put(FartBomb.Bombs.FIELD_RATING, getRating());
        values.put(FartBomb.Bombs.FIELD_TIME_STAMP, getTimeStamp());
        values.put(FartBomb.Bombs.FIELD_BYTE_ARRAY, getAudioBytes());
        return values;
    }

    public Date getDate() {
        return new Date(getTimeStamp());
    }

    public String getEncodedBytes() {
        return Base64.encodeBytes(getAudioBytes());
    }

    public String getFileName() {
        return fileName != null ? fileName : "";
    }

    public int getId() {
        return id;
    }

    public long getLength() {
        return length;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public double getRating() {
        return rating;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getUserId() {
        return userId;
    }

    public void setAudioBytes(byte[] audioBytes) {
        this.audioBytes = audioBytes;
    }

    public void setAudioBytes(String encoded) {
        try {
            setAudioBytes(Base64.decode(encoded));
        } catch (IOException e) {
            Log.e("Bomb", "Error decoding bytes " + encoded, e);
        }
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put(JSON_USER_ID, getUserId());
            json.put(JSON_BOMB_ID, getId());
            json.put(JSON_LENGTH, getLength());
            json.put(JSON_RATING, getRating());
            json.put(JSON_TIME_STAMP, getTimeStamp());
            json.put(JSON_FILE_NAME, getFileName());
            json.put(JSON_NAME, getName());
            json.put(JSON_BYTE_ARRAY, getAudioBytes());
        } catch (JSONException je) {
            Log.e("Bomb", "Error getting json", je);
            json = new JSONObject();
        }
        return json;
    }
}
