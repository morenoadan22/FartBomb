package com.moreno.fartbomb.widget;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.database.Cursor;
import android.view.*;
import android.widget.*;

import com.moreno.fartbomb.R;
import com.moreno.fartbomb.data.Bomb;

public class BombsAdapter extends CursorAdapter {
    private final LayoutInflater inflater;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
    @SuppressWarnings("unused")
    private static final String LOG_TAG = BombsAdapter.class.getSimpleName();
    private int selectedId;

    @SuppressWarnings("deprecation")
    public BombsAdapter(Context context, Cursor c) {
        super(context, c);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Bomb bomb = new Bomb(cursor);
        TextView txtName = (TextView) view.findViewById(R.id.txtName);
        TextView txtLenght = (TextView) view.findViewById(R.id.txtLenght);
        TextView txtDate = (TextView) view.findViewById(R.id.txtDate);
        TextView txtRating = (TextView) view.findViewById(R.id.txtRating);
        ImageView btnPlay = (ImageView) view.findViewById(R.id.btnPlay);
        if (bomb.getId() == getSelected()) {
            btnPlay.setVisibility(View.VISIBLE);
            view.setBackgroundResource(R.drawable.rounded_row_selected);
        } else {
            btnPlay.setVisibility(View.GONE);
            view.setBackgroundResource(R.drawable.rounded_row);
        }

        txtName.setText(bomb.getName());
        txtLenght.setText("" + TimeUnit.MILLISECONDS.toSeconds(bomb.getLength()) + "sec");
        txtRating.setText(String.format("%.1f", bomb.getRating()));
        txtDate.setText("" + sdf.format(bomb.getDate()));
    }

    public int getSelected() {
        return selectedId;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.bomb_row, parent, false);
    }

    public void setSelected(int bombId) {
        selectedId = bombId;
        this.notifyDataSetChanged();
    }

}
