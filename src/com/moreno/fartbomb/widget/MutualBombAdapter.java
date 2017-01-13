package com.moreno.fartbomb.widget;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.view.*;
import android.widget.*;

import com.moreno.fartbomb.R;
import com.moreno.fartbomb.data.Bomb;

public class MutualBombAdapter extends BaseAdapter {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
    private ArrayList<Bomb> bombList = new ArrayList<Bomb>();
    private final LayoutInflater inflater;
    private int selectedId;

    public MutualBombAdapter(Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return bombList.size();
    }

    @Override
    public Bomb getItem(int position) {
        return bombList.get(position);
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
        Bomb bomb = getItem(position);
        RelativeLayout view;
        if (convertView == null) {
            view = (RelativeLayout) inflater.inflate(R.layout.bomb_row, parent, false);
        } else {
            view = (RelativeLayout) convertView;
        }

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

        return view;
    }

    public void setSelected(int bombId) {
        selectedId = bombId;
        this.notifyDataSetChanged();
    }

    public void updateEntries(ArrayList<Bomb> bombList) {
        this.bombList = bombList;
        this.notifyDataSetChanged();
    }

}
