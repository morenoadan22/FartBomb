package com.moreno.fartbomb;

import java.io.*;
import java.util.ArrayList;

import org.json.*;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.media.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

import com.loopj.android.http.*;
import com.moreno.fartbomb.data.*;
import com.moreno.fartbomb.network.*;
import com.moreno.fartbomb.provider.FartBomb;
import com.moreno.fartbomb.util.*;
import com.moreno.fartbomb.widget.*;

public class PlaybackActivity extends Activity implements OnClickListener, OnTouchListener, OnTabChangeListener {
    class BombClickListener implements OnItemClickListener {
        private final PlaybackAdapter adapter;

        public BombClickListener(PlaybackAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            adapter.setSelected(adapter.getBombId(position));
            ImageView btnPlay = (ImageView) view.findViewById(R.id.btnPlay);

            btnPlay.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (v.getId() == R.id.btnPlay) {
                        try {
                            progressDialog = new ProgressDialog(PlaybackActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setMessage("Downloading Bomb...");
                            progressDialog.setIndeterminate(true);
                            progressDialog.show();
                            JSONObject jBomb = adapter.getItem(position);
                            RequestParams params = new RequestParams();
                            params.put(FartBomb.Bombs.FIELD_BOMB_ID, String.valueOf(jBomb.getInt(PlaybackAdapter.JSON_BOMB_ID)));
                            FartBombRestClient.get(Servlet.FETCH_AUDIO.toString(), params, new TextHttpResponseHandler() {

                                @Override
                                public void onSuccess(String response) {
                                    progressDialog.dismiss();
                                    String fileName = response.substring(9, response.indexOf("|"));
                                    String encoded = response.substring(response.indexOf("|") + 7);
                                    byte[] audioBytes = null;
                                    try {
                                        audioBytes = Base64.decode(encoded);
                                    } catch (IOException e) {
                                        Log.e(LOG_TAG, "Error decoding response" + encoded);
                                    }
                                    File tmp = createAudioFile(fileName, audioBytes);
                                    playBomb(fileName, tmp);
                                }
                            });
                        } catch (JSONException je) {
                            Log.e(LOG_TAG, je.getMessage(), je);
                        }
                    }

                }

            });
        }
    }

    TextView txtRequestCount;

    private static final String LOG_TAG = PlaybackActivity.class.getSimpleName();

    protected static final String EXTRA_FILE_NAME = "extraFileName";
    private final FartBombDb mFbHelper = FartBombDb.getInstance();
    private MediaRecorder mRecorder;
    private static String mFileName;
    private User activeUser;
    private boolean isRecording;
    private TabHost tabHost;
    private MediaPlayer mPlayer;
    private PlaybackAdapter playAdapter;
    private PlaybackAdapter playAdapterCommunity;
    private ImageView ratingBar;
    private TextView btnRate;
    private TextView txtRatingResult;
    private static final int TAB_FRIENDS = 0;
    private static final int TAB_COMMUNITY = 1;
    private AudioManager audio;
    protected ProgressDialog progressDialog;

    @SuppressLint("ClickableViewAccessibility")
    private void addRatingListener() {
        ratingBar = (ImageView) findViewById(R.id.fartRatingBar);
        txtRatingResult = (TextView) findViewById(R.id.ratingResult);

        ratingBar.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                float height = view.getHeight();
                float position = height - event.getY();
                float base = 10.0f;
                float desired = (base * position) / height;
                if (desired > base) {
                    desired = base;
                }
                if (desired < 0.0f) {
                    desired = 0.0f;
                }
                txtRatingResult.setText(String.format("%.1f", desired));
                return true;
            }

        });

        txtRatingResult.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {
                if (view.getId() == R.id.ratingResult) {
                    TextView t = (TextView) view;
                    String s = t.getText().toString();
                    Toast.makeText(PlaybackActivity.this, "Long click on view: " + s, Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }

        });

    }

    public void beep() {
        final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500);
    }

    /**
     * Checks db for any existing user who has logged in and redirects as necessary
     */
    private void checkForUser() {
        User user = mFbHelper.getActiveUser(getContentResolver());
        Intent i;
        if (user.equals(User.newNull())) {
            Log.e(LOG_TAG, "User is non existent, redirecting to Signup");
            i = new Intent(this, SignupActivity.class);
            startActivity(i);
            finish();
        } else if (!user.isActive()) {
            Log.e(LOG_TAG, "User is not active, redirecting to Login: " + user.getJSON());
            i = new Intent(this, LoginActivity.class);
            i.putExtra(FartBomb.Users.FIELD_USERNAME, user.getUserName());
            startActivity(i);
            finish();
        } else if (user.isActive()) {
            Log.e(LOG_TAG, "User is active: " + user.getJSON());
            i = new Intent(this, NotificationService.class);
            startService(i);
            FartBombTools.syncServerData(this, user);
        }
    }

    protected File createAudioFile(String fileName, byte[] buffer) {
        File tmp = null;
        try {
            tmp = File.createTempFile("fartbomb", "3gp", getCacheDir());
            if (!tmp.exists()) {
                tmp.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(tmp);
            fos.write(buffer);
            fos.close();
        } catch (IOException ex) {
            Log.e(LOG_TAG, "createAudioFile()", ex);
        }

        return tmp;
    }

    /**
     * 
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.settingsButton:
            if (!isRecording) {
                startActivity(new Intent(this, SettingsActivity.class));
            }
            break;
        case R.id.friendButton:
            if (!isRecording) {
                startActivity(new Intent(this, ProfileActivity.class));
            }
            break;
        case R.id.btnRate:
            if (!isRecording) {
                RequestParams params = new RequestParams();
                if (tabHost.getCurrentTab() == TAB_FRIENDS) {
                    params.put(FartBomb.Bombs.FIELD_BOMB_ID, "" + playAdapter.getSelected());
                } else if (tabHost.getCurrentTab() == TAB_COMMUNITY) {
                    params.put(FartBomb.Bombs.FIELD_BOMB_ID, "" + playAdapterCommunity.getSelected());
                }
                params.put(FartBomb.Bombs.FIELD_RATING, txtRatingResult.getText().toString());
                params.put(FartBomb.Bombs.FIELD_USER_ID, "" + activeUser.getUserId());

                FartBombRestClient.post(Servlet.RATE_BOMB.toString(), params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(JSONObject jo) {
                        try {
                            if (jo.getBoolean("status")) {
                                populateLists(false);
                                FartBombTools.syncServerData(PlaybackActivity.this, activeUser);
                            }
                        } catch (JSONException je) {

                        }
                    }
                });

            }
            break;
        default:
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFartbombLayout(R.layout.activity_playback);
        checkForUser();
        activeUser = mFbHelper.getActiveUser(getContentResolver());
        btnRate = (TextView) findViewById(R.id.btnRate);

        ImageButton btnFriends = (ImageButton) findViewById(R.id.friendButton);
        ImageButton btnSettings = (ImageButton) findViewById(R.id.settingsButton);
        ImageButton btnRecord = (ImageButton) findViewById(R.id.recordButton);
        txtRequestCount = (TextView) findViewById(R.id.requestCircle);
        ListView friendList = (ListView) findViewById(R.id.friendList);
        ListView communityList = (ListView) findViewById(R.id.fartBombList);

        btnRecord.setOnTouchListener(this);
        btnSettings.setOnClickListener(this);
        btnFriends.setOnClickListener(this);
        btnRate.setOnClickListener(this);

        updateFriendRequests();

        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        tabHost = (TabHost) findViewById(R.id.tabhost);
        tabHost.setup();

        TabSpec specFriends = tabHost.newTabSpec("FRIENDS");
        specFriends.setIndicator("FRIENDS");
        specFriends.setContent(R.id.tab1);

        TabSpec specCommunity = tabHost.newTabSpec("COMMUNITY");
        specCommunity.setIndicator("COMMUNITY");
        specCommunity.setContent(R.id.tab2);
        tabHost.addTab(specFriends);
        tabHost.addTab(specCommunity);

        tabHost.setOnTabChangedListener(this);

        View tabFriends = tabHost.getTabWidget().getChildAt(0);
        View tabCommunity = tabHost.getTabWidget().getChildAt(1);
        tabFriends.getLayoutParams().height = 80;
        tabFriends.setBackgroundColor(Color.WHITE);

        tabCommunity.getLayoutParams().height = 80;
        tabCommunity.setBackgroundColor(Color.WHITE);
        tabFriends.setBackgroundResource(R.drawable.tab_selector);

        playAdapter = new PlaybackAdapter(this);
        playAdapterCommunity = new PlaybackAdapter(this);
        friendList.setAdapter(playAdapter);
        communityList.setAdapter(playAdapterCommunity);
        friendList.setOnItemClickListener(new BombClickListener(playAdapter));
        communityList.setOnItemClickListener(new BombClickListener(playAdapterCommunity));
        populateLists(true);
        addRatingListener();

        friendList.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                tabHost.setCurrentTab(1);
            }
        });

        communityList.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeRight() {
                tabHost.setCurrentTab(0);
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_UP:
            audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
            return true;
        case KeyEvent.KEYCODE_VOLUME_DOWN:
            audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
            return true;
        case KeyEvent.KEYCODE_BACK:
            finish();
            return true;
        default:
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkForUser();
        updateFriendRequests();
    }

    @Override
    public void onTabChanged(String tabId) {
        if (tabId.equals("FRIENDS")) {
            tabHost.getTabWidget().getChildAt(0).setBackgroundResource(R.drawable.tab_selector);
            tabHost.getTabWidget().getChildAt(1).setBackgroundColor(Color.WHITE);
        } else if (tabId.equals("COMMUNITY")) {
            tabHost.getTabWidget().getChildAt(0).setBackgroundColor(Color.WHITE);
            tabHost.getTabWidget().getChildAt(1).setBackgroundResource(R.drawable.tab_selector);
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (view.getId()) {
        case R.id.recordButton:
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                view.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                view.invalidate();
                beep();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Log.e(LOG_TAG, "Cannot wait ", e);
                }
                startRecording();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                view.getBackground().clearColorFilter();
                view.invalidate();
                stopRecording();
                Intent intent = new Intent(this, SaveFileDialog.class);
                intent.putExtra(EXTRA_FILE_NAME, mFileName);
                mFileName = "";
                startActivity(intent);
            }
            break;
        }
        return false;
    }

    protected void playBomb(String fileName, File tmp) {
        try {
            mPlayer = new MediaPlayer();
            mPlayer.setOnCompletionListener(new BombPlaybackListener(tmp));
            mPlayer.setDataSource(tmp.getAbsolutePath());
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "playBomb() " + tmp.getAbsolutePath(), ioe);
        }
    }

    public void populateLists(final boolean toggleTabs) {
        final ProgressBar progFriends, progCommunity;
        progFriends = (ProgressBar) findViewById(R.id.progressBarFriends);
        progCommunity = (ProgressBar) findViewById(R.id.progressBarCommunity);

        RequestParams params = new RequestParams();
        params.put("userId", "" + activeUser.getUserId());
        params.put("filter", "friends");
        FartBombRestClient.get(Servlet.SPLASH.toString(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject jo) {
                progFriends.setVisibility(View.GONE);
                try {
                    if (jo.getBoolean("status")) {
                        ArrayList<JSONObject> bombs = processJArray(jo.getJSONArray("results"));
                        playAdapter.updateDisplay(bombs);
                        if (toggleTabs) {
                            tabHost.setCurrentTab(1);
                            tabHost.setCurrentTab(0);
                        }
                    }
                } catch (JSONException je) {
                    Log.e(LOG_TAG, je.getMessage(), je);
                }
            }
        });

        params.put("filter", "none");
        FartBombRestClient.get(Servlet.SPLASH.toString(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject jo) {
                progCommunity.setVisibility(View.GONE);
                try {
                    if (jo.getBoolean("status")) {
                        ArrayList<JSONObject> bombs = processJArray(jo.getJSONArray("results"));
                        for (JSONObject bomb : bombs) {
                            bomb.put(PlaybackAdapter.JSON_IS_COMMUNITY, true);
                        }
                        playAdapterCommunity.updateDisplay(bombs);
                    }
                } catch (JSONException je) {
                    Log.e(LOG_TAG, je.getMessage(), je);
                }
            }
        });
    }

    protected ArrayList<JSONObject> processJArray(JSONArray jsonArray) throws JSONException {
        ArrayList<JSONObject> bombs = new ArrayList<JSONObject>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject bomb = (JSONObject) jsonArray.get(i);
            if (bomb.getInt("bombId") != 0) {
                bombs.add((JSONObject) jsonArray.get(i));
            }
        }
        return bombs;
    }

    private void setFartbombLayout(int layoutReference) {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(layoutReference);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
    }

    private void startRecording() {
        isRecording = true;
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/" + activeUser.getUserName();
        mFileName += "_" + System.currentTimeMillis() + ".3gp";
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed " + mFileName, e);
        }

    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        isRecording = false;
    }

    public void updateFriendRequests() {
        int requestCount = mFbHelper.getFriendRequestCount(getContentResolver(), activeUser.getUserId());
        if (requestCount > 0) {
            txtRequestCount.setText("" + requestCount);
            txtRequestCount.setVisibility(View.VISIBLE);
        } else {
            txtRequestCount.setVisibility(View.GONE);
        }
    }
}
