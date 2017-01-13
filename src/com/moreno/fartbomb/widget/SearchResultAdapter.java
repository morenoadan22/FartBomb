package com.moreno.fartbomb.widget;

import java.util.ArrayList;

import org.json.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import com.loopj.android.http.*;
import com.moreno.fartbomb.R;
import com.moreno.fartbomb.data.*;
import com.moreno.fartbomb.network.*;
import com.moreno.fartbomb.provider.FartBomb;

public class SearchResultAdapter extends BaseAdapter {
    public static final String JSON_TYPE = "type";
    public static final String JSON_FRIEND_ID = "friendId";
    public static final String JSON_IS_FRIEND = "isFriend";
    public static final String JSON_IS_PENDING = "isPending";
    public static final String JSON_FRIEND_NAME = "friendName";
    public static final String JSON_IS_FRIEND_REQUEST = "isFriendRequest";

    private static final String LOG_TAG = SearchResultAdapter.class.getSimpleName();
    private final LayoutInflater inflater;
    private ArrayList<JSONObject> friendResults = new ArrayList<JSONObject>();
    private final Drawable dwWhite, dwGreen, dwCheck;
    private final Context context;
    private final FartBombDb mFbHelper = FartBombDb.getInstance();

    public SearchResultAdapter(Context context) {
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dwWhite = context.getResources().getDrawable(R.drawable.btn_add_white);
        dwGreen = context.getResources().getDrawable(R.drawable.btn_add_green);
        dwCheck = context.getResources().getDrawable(R.drawable.btn_check);
    }

    @Override
    public int getCount() {
        return friendResults.size();
    }

    @Override
    public JSONObject getItem(int position) {
        return friendResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("NewApi")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final JSONObject friend = getItem(position);
        RelativeLayout itemView;
        if (convertView == null) {
            itemView = (RelativeLayout) inflater.inflate(R.layout.add_friend_row, parent, false);
        } else {
            itemView = (RelativeLayout) convertView;
        }

        TextView friendName = (TextView) itemView.findViewById(R.id.txtFriendName);
        ImageView btnAdd = (ImageView) itemView.findViewById(R.id.btnAddFriend);
        ImageView btnDeny = (ImageView) itemView.findViewById(R.id.btnDenyRequest);
        ImageView btnAccept = (ImageView) itemView.findViewById(R.id.btnAcceptRequest);
        btnAdd.setVisibility(View.GONE);
        btnDeny.setVisibility(View.GONE);
        btnAccept.setVisibility(View.GONE);

        try {
            friendName.setText(friend.getString(JSON_FRIEND_NAME));
            if (friend.getBoolean(JSON_IS_FRIEND)) {
                btnAdd.setVisibility(View.VISIBLE);
                friendName.setText(friend.getString(JSON_FRIEND_NAME));
                if (android.os.Build.VERSION.SDK_INT >= 16) {
                    btnAdd.setBackground(dwCheck);
                } else {
                    btnAdd.setBackgroundResource(R.drawable.btn_check);
                }
                btnAdd.setClickable(false);
            } else if (friend.getBoolean(JSON_IS_PENDING)) {
                btnAdd.setVisibility(View.VISIBLE);
                friendName.setText(friend.getString(JSON_FRIEND_NAME));
                if (android.os.Build.VERSION.SDK_INT >= 16) {
                    btnAdd.setBackground(dwGreen);
                } else {
                    btnAdd.setBackgroundResource(R.drawable.btn_add_green);
                }
                btnAdd.setClickable(false);
            } else if (friend.getBoolean(JSON_IS_FRIEND_REQUEST)) {
                btnDeny.setVisibility(View.VISIBLE);
                btnAccept.setVisibility(View.VISIBLE);
                btnDeny.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        final int userId = FartBombDb.getInstance().getActiveUser(context.getContentResolver()).getUserId();
                        try {
                            friend.put(JSON_IS_FRIEND_REQUEST, false);
                            notifyDataSetChanged();
                            RequestParams params = new RequestParams();
                            params.put(JSON_TYPE, "DENY");
                            params.put(JSON_FRIEND_ID, String.valueOf(friend.getInt(JSON_FRIEND_ID)));
                            params.put(FartBomb.Friends.FIELD_USER_ID, String.valueOf(userId));
                            FartBombRestClient.post(Servlet.FRIEND_REQUEST.toString(), params, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(JSONObject jo) {
                                    Friend f = Friend.fromJSON(friend);
                                    f.setUserId(userId);
                                    mFbHelper.removeFriendRequest(context.getContentResolver(), f);
                                }
                            });
                        } catch (JSONException je) {
                            Log.e(LOG_TAG, "Cannot parse friend: " + friend.toString(), je);
                        }
                    }

                });

                btnAccept.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        int userId = FartBombDb.getInstance().getActiveUser(context.getContentResolver()).getUserId();
                        try {
                            friend.put(JSON_IS_FRIEND, true);
                            notifyDataSetChanged();
                            RequestParams params = new RequestParams();
                            params.put(JSON_TYPE, "ACCEPT");
                            params.put(JSON_FRIEND_ID, String.valueOf(friend.getInt(JSON_FRIEND_ID)));
                            params.put(FartBomb.Friends.FIELD_USER_ID, String.valueOf(userId));
                            FartBombRestClient.post(Servlet.FRIEND_REQUEST.toString(), params, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(JSONObject jo) {
                                    mFbHelper.removeFriendRequest(context.getContentResolver(), Friend.fromJSON(friend));
                                }
                            });
                        } catch (JSONException je) {
                            Log.e(LOG_TAG, "Cannot parse friend: " + friend.toString(), je);
                        }
                    }

                });

            } else {
                btnAdd.setVisibility(View.VISIBLE);
                if (android.os.Build.VERSION.SDK_INT >= 16) {
                    btnAdd.setBackground(dwWhite);
                } else {
                    btnAdd.setBackgroundResource(R.drawable.btn_add_white);
                }
                btnAdd.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        int userId = FartBombDb.getInstance().getActiveUser(context.getContentResolver()).getUserId();
                        try {
                            friend.put(JSON_IS_PENDING, true);
                            notifyDataSetChanged();
                            RequestParams params = new RequestParams();
                            params.put(JSON_TYPE, "REQUEST");
                            params.put(JSON_FRIEND_ID, String.valueOf(friend.getInt(JSON_FRIEND_ID)));
                            params.put(FartBomb.Friends.FIELD_USER_ID, String.valueOf(userId));
                            FartBombRestClient.post(Servlet.FRIEND_REQUEST.toString(), params, new JsonHttpResponseHandler() {

                                @Override
                                public void onSuccess(JSONObject jo) {
                                    try {
                                        if (jo.getBoolean("status")) {
                                            v.setClickable(false);
                                        }
                                    } catch (JSONException je) {

                                    }
                                }
                            });
                        } catch (JSONException je) {
                            Log.e(LOG_TAG, "Cannot parse friend: " + friend.toString(), je);
                        }
                    }

                });
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Parsing friend error " + friend.toString(), e);
        }

        return itemView;
    }

    public void updateEntries(ArrayList<JSONObject> entries) {
        friendResults = entries;
        notifyDataSetChanged();
    }
}
