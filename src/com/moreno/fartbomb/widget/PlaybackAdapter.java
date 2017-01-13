package com.moreno.fartbomb.widget;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.json.*;

import android.content.*;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import com.moreno.fartbomb.*;

public class PlaybackAdapter extends BaseAdapter {
    public static final String JSON_USER_ID = "userId";
    public static final String JSON_USER_NAME = "userName";
    public static final String JSON_NAME = "name";
    public static final String JSON_LENGHT = "length";
    public static final String JSON_BOMB_ID = "bombId";
    public static final String JSON_RATING = "rating";
    public static final String JSON_IS_COMMUNITY = "isCommunity";

    private int selectedId;

    private static final String LOG_TAG = PlaybackAdapter.class.getSimpleName();
    private final Context context;
    private final LayoutInflater inflater;
    private ArrayList<JSONObject> bombs = new ArrayList<JSONObject>();

    public PlaybackAdapter(Context context) {
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getBombId(int position) {
        JSONObject jBomb = getItem(position);
        try {
            return jBomb.getInt(JSON_BOMB_ID);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        return -1;
    }

    @Override
    public int getCount() {
        return bombs.size();
    }

    @Override
    public JSONObject getItem(int position) {
        return bombs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getSelected() {
        return selectedId;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        JSONObject bomb = getItem(position);
        RelativeLayout itemView;
        if (view == null) {
            itemView = (RelativeLayout) inflater.inflate(R.layout.playback_row, parent, false);
        } else {
            itemView = (RelativeLayout) view;
        }

        TextView bombName = (TextView) itemView.findViewById(R.id.txtName);
        TextView length = (TextView) itemView.findViewById(R.id.txtLenght);
        TextView creator = (TextView) itemView.findViewById(R.id.txtCreator);
        TextView rating = (TextView) itemView.findViewById(R.id.txtRating);
        try {
            ImageView play = (ImageView) itemView.findViewById(R.id.btnPlay);
            play.setBackgroundResource(R.drawable.btn_play);

            if (bomb.getInt(JSON_BOMB_ID) == getSelected()) {
                play.setVisibility(View.VISIBLE);
                itemView.setBackgroundResource(R.drawable.rounded_row_selected);
            } else {
                play.setVisibility(View.GONE);
                itemView.setBackgroundResource(R.drawable.rounded_row);
            }
            bombName.setText(bomb.getString(JSON_NAME));
            length.setText(TimeUnit.MILLISECONDS.toSeconds(bomb.getLong(JSON_LENGHT)) + "sec");
            creator.setText("" + bomb.getString(JSON_USER_NAME));
            final int friendId = bomb.getInt(JSON_USER_ID);
            final String friendName = bomb.getString(JSON_USER_NAME);
            if (bomb.getBoolean(JSON_IS_COMMUNITY)) {
                creator.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context, ViewFriendActivity.class);
                        i.putExtra("friendId", friendId);
                        i.putExtra("friendName", friendName);
                        context.startActivity(i);
                    }

                });
            }
            rating.setText(String.format("%.1f", bomb.getDouble(JSON_RATING)));
        } catch (JSONException je) {
            Log.e(LOG_TAG, "getView()", je);
        }
        return itemView;
    }

    public void setSelected(int bombId) {
        selectedId = bombId;
        this.notifyDataSetChanged();
    }

    public void updateDisplay(ArrayList<JSONObject> bombs) {
        this.bombs = bombs;
        this.notifyDataSetChanged();
    }

}
