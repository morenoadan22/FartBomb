package com.moreno.fartbomb.network;

import org.json.*;

import android.content.*;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.moreno.fartbomb.data.*;

public class SyncHandler extends JsonHttpResponseHandler {
    private static final String LOG_TAG = SyncHandler.class.getSimpleName();
    private static final String JSON_RECORD_TYPE = "type";
    public static final String BROADCAST_SYNC_FINISHED = "com.moreno.fartbomb.SYNC_DATA";
    public static final String BROADCAST_FRIEND_REQUEST_RECEIVED = "com.moreno.fartbomb.REQUEST_RECEIVED";

    public static final int TYPE_FRIEND = 1;
    public static final int TYPE_BOMB = 2;
    public static final int TYPE_FRIEND_REQUEST = 3;

    private final Context context;

    private final FartBombDb mFbHelper = FartBombDb.getInstance();

    public SyncHandler(Context context, User user) {
        this.context = context;
    }

    @Override
    public void onFinish() {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_SYNC_FINISHED);
        context.sendBroadcast(intent);
    }

    @Override
    public void onSuccess(JSONObject jObject) {
        try {
            if (jObject.getBoolean("status")) {
                mFbHelper.clearSyncTables(context.getContentResolver());
                parseResponse(jObject.getJSONArray("data"));
                setUserRank(jObject.getInt("rank"));
            } else {
                Log.e(LOG_TAG, "Error while synchronizing device and server data" + jObject.toString());
            }
        } catch (JSONException je) {
            try {
                JSONObject jsonRow = jObject.getJSONObject("data");
                parseResponseRow(jsonRow, jsonRow.getInt(JSON_RECORD_TYPE));
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error while syncing data : " + je.getMessage());
            }

        }
    }

    private void parseResponse(JSONArray jArray) {
        for (int i = 0; i < jArray.length(); i++) {
            try {
                JSONObject jsonRow = jArray.getJSONObject(i);
                parseResponseRow(jsonRow, jsonRow.getInt(JSON_RECORD_TYPE));
            } catch (JSONException je) {
                Log.e(LOG_TAG, "Error parsing response: " + je.getMessage());
            }
        }
    }

    private void parseResponseRow(JSONObject jObject, int type) {
        try {
            switch (type) {
            case TYPE_FRIEND:
                processFriend(jObject);
                break;
            case TYPE_BOMB:
                processBomb(jObject);
                break;
            case TYPE_FRIEND_REQUEST:
                processFriendRequest(jObject);
                break;
            }
        } catch (JSONException je) {
            Log.e(LOG_TAG, "Error parsing response: " + je.getMessage());
        }
    }

    private void processBomb(JSONObject jsonRow) throws JSONException {
        Bomb bomb = Bomb.fromJSON(jsonRow);
        mFbHelper.addBomb(context.getContentResolver(), bomb);
    }

    private void processFriend(JSONObject jsonRow) throws JSONException {
        Friend friend = Friend.fromJSON(jsonRow);
        friend.setAccepted(true);
        mFbHelper.addFriend(context.getContentResolver(), friend);
    }

    private void processFriendRequest(JSONObject jsonRow) throws JSONException {
        Friend friend = Friend.fromJSON(jsonRow);
        friend.setUserId(mFbHelper.getActiveUser(context.getContentResolver()).getUserId());
        friend.setAccepted(false);
        Intent i = new Intent();
        i.setAction(BROADCAST_FRIEND_REQUEST_RECEIVED);
        context.sendBroadcast(i);
        mFbHelper.addFriend(context.getContentResolver(), friend);
        mFbHelper.addFriendRequest(context, context.getContentResolver(), friend);
    }

    private void setUserRank(int rank) {
        mFbHelper.updateRanking(context.getContentResolver(), rank);
    }
}
