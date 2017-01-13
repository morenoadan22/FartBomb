package com.moreno.fartbomb.widget;

import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import com.loopj.android.http.*;
import com.moreno.fartbomb.R;
import com.moreno.fartbomb.data.*;
import com.moreno.fartbomb.network.*;
import com.moreno.fartbomb.provider.FartBomb;

public class FriendsAdapter extends CursorAdapter {
    @SuppressWarnings("unused")
    private static final String LOG_TAG = FriendsAdapter.class.getSimpleName();
    public static final String JSON_TYPE = "type";
    public static final String JSON_FRIEND_ID = "friendId";
    private static LayoutInflater inflater = null;
    private int selectedId;
    protected FartBombDb mFbHelper = FartBombDb.getInstance();

    @SuppressWarnings("deprecation")
    public FriendsAdapter(Context context, Cursor c) {
        super(context, c);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final Friend friend = new Friend(cursor);
        TextView txtName = (TextView) view.findViewById(R.id.txtFriendName);
        TextView txtBombCount = (TextView) view.findViewById(R.id.txtFartCount);
        TextView txtRatingSum = (TextView) view.findViewById(R.id.txtRatingSum);
        ImageView btnAccept = (ImageView) view.findViewById(R.id.btnAcceptRequest);
        ImageView btnDeny = (ImageView) view.findViewById(R.id.btnDenyRequest);

        if (friend.getId() == getSelected()) {
            view.setBackgroundResource(R.drawable.rounded_row_selected);
        } else {
            view.setBackgroundResource(R.drawable.rounded_row);
        }
        if (friend.isAccepted()) {
            btnAccept.setVisibility(View.GONE);
            btnDeny.setVisibility(View.GONE);
            txtBombCount.setVisibility(View.VISIBLE);
            txtRatingSum.setVisibility(View.VISIBLE);
            txtName.setText(friend.getName());
            txtBombCount.setText("" + friend.getBombCount());
            txtRatingSum.setText(String.format("%.1f", friend.getRatingSum()));
        } else if (!friend.isAccepted()) {
            btnAccept.setVisibility(View.VISIBLE);
            btnDeny.setVisibility(View.VISIBLE);
            txtName.setText(friend.getName());
            txtBombCount.setVisibility(View.GONE);
            txtRatingSum.setVisibility(View.GONE);

            btnDeny.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    int userId = FartBombDb.getInstance().getActiveUser(context.getContentResolver()).getUserId();
                    friend.setAccepted(false);
                    RequestParams params = new RequestParams();
                    params.put(JSON_TYPE, "DENY");
                    params.put(JSON_FRIEND_ID, String.valueOf(friend.getId()));
                    params.put(FartBomb.Friends.FIELD_USER_ID, String.valueOf(userId));
                    mFbHelper.denyFriend(context.getContentResolver(), friend);
                    FartBombRestClient.post(Servlet.FRIEND_REQUEST.toString(), params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(JSONObject jo) {
                            mFbHelper.removeFriendRequest(context.getContentResolver(), friend);
                        }
                    });
                }

            });

            btnAccept.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    int userId = FartBombDb.getInstance().getActiveUser(context.getContentResolver()).getUserId();
                    friend.setAccepted(true);
                    mFbHelper.acceptFriend(context.getContentResolver(), friend);
                    RequestParams params = new RequestParams();
                    params.put(JSON_TYPE, "ACCEPT");
                    params.put(JSON_FRIEND_ID, String.valueOf(friend.getId()));
                    params.put(FartBomb.Friends.FIELD_USER_ID, String.valueOf(userId));
                    FartBombRestClient.post(Servlet.FRIEND_REQUEST.toString(), params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(JSONObject jo) {
                            mFbHelper.removeFriendRequest(context.getContentResolver(), friend);
                        }
                    });
                }

            });

        }

    }

    public int getSelected() {
        return selectedId;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.friend_row, parent, false);
    }

    public void setSelected(int friendId) {
        selectedId = friendId;
        this.notifyDataSetChanged();
    }

}
