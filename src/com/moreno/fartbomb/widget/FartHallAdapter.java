package com.moreno.fartbomb.widget;

import java.util.ArrayList;

import org.json.*;

import android.content.Context;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.moreno.fartbomb.R;

public class FartHallAdapter extends BaseAdapter {
    public static final String JSON_NAME = "name";
    public static final String JSON_BOMBS = "bombs";
    public static final String JSON_RATING = "rating";

    private static final String LOG_TAG = FartHallAdapter.class.getSimpleName();
    private final LayoutInflater inflater;
    private ArrayList<JSONObject> fartBombs = new ArrayList<JSONObject>();

    public FartHallAdapter(Context context) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return fartBombs.size();
    }

    @Override
    public JSONObject getItem(int position) {
        return fartBombs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        JSONObject bomb = getItem(position);
        RelativeLayout itemView;
        if (convertView == null) {
            itemView = (RelativeLayout) inflater.inflate(R.layout.fart_hall_row, parent, false);
        } else {
            itemView = (RelativeLayout) convertView;
        }

        TextView txtName = (TextView) itemView.findViewById(R.id.txtName);
        TextView txtBombCount = (TextView) itemView.findViewById(R.id.txtBombCount);
        TextView txtRatingSum = (TextView) itemView.findViewById(R.id.txtRatingSum);

        try {
            txtName.setText(bomb.getString(JSON_NAME));
            txtBombCount.setText("" + bomb.getInt(JSON_BOMBS));
            txtRatingSum.setText(String.format("%.1f", bomb.getDouble(JSON_RATING)));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Exception rendering list view row: " + position, e);
        }

        return itemView;
    }

    public void updateEntries(ArrayList<JSONObject> entries) {
        fartBombs = entries;
        notifyDataSetChanged();
    }

}
