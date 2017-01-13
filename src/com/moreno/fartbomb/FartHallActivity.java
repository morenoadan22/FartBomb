package com.moreno.fartbomb;

import java.util.ArrayList;

import org.apache.http.Header;
import org.json.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import com.loopj.android.http.*;
import com.moreno.fartbomb.data.*;
import com.moreno.fartbomb.network.*;
import com.moreno.fartbomb.provider.FartBomb;
import com.moreno.fartbomb.widget.FartHallAdapter;

public class FartHallActivity extends Activity implements OnClickListener {
    private static final String LOG_TAG = FartHallActivity.class.getSimpleName();
    private User activeUser;
    private ListView listFartHall;
    private TextView txtLoading;
    private final FartBombDb mFbHelper = FartBombDb.getInstance();

    private void getHallOfFarts() {
        RequestParams params = new RequestParams();
        params.put(FartBomb.Users.FIELD_USER_ID, activeUser.getUserId());
        FartBombRestClient.post(Servlet.FART_HALL.toString(), params, new JsonHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
                txtLoading.setText("UNABLE TO LOAD DATA");
            }

            @Override
            public void onSuccess(JSONObject jObject) {
                updateHeader();
                listFartHall.setVisibility(View.VISIBLE);
                try {
                    if (jObject.getBoolean("status")) {
                        ArrayList<JSONObject> entries = parseJSONArray(jObject.getJSONArray("hallBombs"));
                        FartHallAdapter adapter = new FartHallAdapter(FartHallActivity.this);
                        adapter.updateEntries(entries);
                        listFartHall.setAdapter(adapter);
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error loading the hall of farts data", e);
                }
            }

            private ArrayList<JSONObject> parseJSONArray(JSONArray jsonArray) {
                ArrayList<JSONObject> entries = new ArrayList<JSONObject>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        entries.add((JSONObject) jsonArray.get(i));
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Error parsing entry: " + jsonArray.toString() + " @ index: " + i, e);
                    }
                }
                return entries;
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btnHome:
            startActivity(new Intent(this, PlaybackActivity.class));
            break;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fart_hall);
        activeUser = mFbHelper.getActiveUser(getContentResolver());
        getHallOfFarts();

        ImageView btnHome = (ImageView) findViewById(R.id.btnHome);
        txtLoading = (TextView) findViewById(R.id.txtEmpty);
        listFartHall = (ListView) findViewById(R.id.listFartHall);

        btnHome.setOnClickListener(this);
    }

    private void updateHeader() {
        txtLoading.setVisibility(View.GONE);
        TextView headerName = (TextView) findViewById(R.id.headerName);
        TextView headerBombs = (TextView) findViewById(R.id.headerBombs);
        TextView headerRating = (TextView) findViewById(R.id.headerRating);

        headerName.setVisibility(View.VISIBLE);
        headerBombs.setVisibility(View.VISIBLE);
        headerRating.setVisibility(View.VISIBLE);
    }
}
