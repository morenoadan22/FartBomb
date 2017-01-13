package com.moreno.fartbomb.widget;

import java.util.ArrayList;

import android.content.Context;
import android.view.*;
import android.widget.*;

import com.moreno.fartbomb.R;
import com.moreno.fartbomb.data.Friend;

public class MutualFriendsAdapter extends BaseAdapter {
    private ArrayList<Friend> friendList = new ArrayList<Friend>();
    private final LayoutInflater inflater;
    private int selectedId;

    public MutualFriendsAdapter(Context context) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return friendList.size();
    }

    @Override
    public Friend getItem(int position) {
        return friendList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getSelected() {
        return selectedId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Friend friend = getItem(position);
        RelativeLayout view;
        if (convertView == null) {
            view = (RelativeLayout) inflater.inflate(R.layout.friend_row, parent, false);
        } else {
            view = (RelativeLayout) convertView;
        }
        TextView txtName = (TextView) view.findViewById(R.id.txtFriendName);
        TextView txtBombCount = (TextView) view.findViewById(R.id.txtFartCount);
        TextView txtRatingSum = (TextView) view.findViewById(R.id.txtRatingSum);

        if (friend.getId() == getSelected()) {
            view.setBackgroundResource(R.drawable.rounded_row_selected);
        } else {
            view.setBackgroundResource(R.drawable.rounded_row);
        }

        txtName.setText(friend.getName());
        txtBombCount.setText("" + friend.getBombCount());
        txtRatingSum.setText(String.format("%.1f", friend.getRatingSum()));

        return view;
    }

    public void setSelected(int friendId) {
        selectedId = friendId;
        this.notifyDataSetChanged();
    }

    public void updateEntries(ArrayList<Friend> entries) {
        friendList = entries;
        notifyDataSetChanged();
    }

}
