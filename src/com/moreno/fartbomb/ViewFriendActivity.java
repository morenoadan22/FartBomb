package com.moreno.fartbomb;

import java.io.*;
import java.util.ArrayList;

import org.json.*;

import android.app.*;
import android.content.*;
import android.graphics.drawable.TransitionDrawable;
import android.media.*;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.animation.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.loopj.android.http.*;
import com.moreno.fartbomb.data.*;
import com.moreno.fartbomb.network.*;
import com.moreno.fartbomb.provider.FartBomb;
import com.moreno.fartbomb.util.*;
import com.moreno.fartbomb.widget.*;

public class ViewFriendActivity extends Activity implements OnClickListener {
    private static final String LOG_TAG = ViewFriendActivity.class.getSimpleName();
    private static final int TYPE_FRIEND = 1;
    private static final int TYPE_BOMB = 2;
    private final FartBombDb mFbHelper = FartBombDb.getInstance();
    private TextView txtFriends, txtBombs, txtRating, txtFragTitle;
    private MutualFriendsAdapter friendAdapter;
    private MutualBombAdapter bombAdapter;
    private ListView listFriends, listBombs;
    private ViewFlipper flipper;
    private AudioManager audio;
    private ProgressDialog progress, progressDialog;
    private Animation slideLeftOut, slideLeftIn, slideRightIn, slideRightOut;
    private MediaPlayer mPlayer;
    static final RelativeLayout.LayoutParams rankSmall = new RelativeLayout.LayoutParams(120, 120);
    static final RelativeLayout.LayoutParams friendsSmall = new RelativeLayout.LayoutParams(120, 120);
    static final RelativeLayout.LayoutParams friendsBig = new RelativeLayout.LayoutParams(130, 130);
    static final RelativeLayout.LayoutParams bombsSmall = new RelativeLayout.LayoutParams(120, 120);
    static final RelativeLayout.LayoutParams bombsBig = new RelativeLayout.LayoutParams(130, 130);

    static {

        rankSmall.addRule(RelativeLayout.CENTER_VERTICAL);
        rankSmall.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rankSmall.setMargins(20, 20, 20, 20);

        friendsSmall.addRule(RelativeLayout.CENTER_VERTICAL);
        friendsSmall.setMargins(20, 20, 20, 20);

        friendsBig.addRule(RelativeLayout.CENTER_VERTICAL);
        friendsBig.setMargins(20, 20, 20, 20);

        bombsSmall.addRule(RelativeLayout.CENTER_HORIZONTAL);
        bombsSmall.addRule(RelativeLayout.CENTER_VERTICAL);

        bombsBig.addRule(RelativeLayout.CENTER_HORIZONTAL);
        bombsBig.addRule(RelativeLayout.CENTER_VERTICAL);
    }

    OnItemClickListener bombClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Bomb bomb = bombAdapter.getItem(position);
            bombAdapter.setSelected(bomb.getId());

            ImageView btnPlay = (ImageView) view.findViewById(R.id.btnPlay);
            btnPlay.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {
                    if (v.getId() == R.id.btnPlay) {
                        progressDialog = new ProgressDialog(ViewFriendActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progressDialog.setMessage("Downloading Bomb...");
                        progressDialog.setIndeterminate(true);
                        progressDialog.show();
                        RequestParams params = new RequestParams();
                        params.put(FartBomb.Bombs.FIELD_BOMB_ID, "" + bomb.getId());
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
                    }
                }
            });
        }

    };

    OnItemClickListener friendClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Friend friend = friendAdapter.getItem(position);
            friendAdapter.setSelected(friend.getId());
        }
    };

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

    private void displayRequestDialog(final int friendId) {
        if (mFbHelper.isFriendRequest(getContentResolver(), friendId)) {
            AlertDialog.Builder d = FartBombTools.buildDialog(this, null, "This person has yet to accept your friend request..");
            d.setCancelable(true);
            d.show();
        } else {
            AlertDialog.Builder d = FartBombTools.buildDialog(this, null, "Would you like to add this person to your friends?");
            d.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });

            d.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    mFbHelper.addFriendRequest(getContentResolver(), friendId);
                    RequestParams params = new RequestParams();
                    params.put("type", "REQUEST");
                    params.put("userId", "" + friendId);
                    params.put("friendId", "" + mFbHelper.getActiveUser(getContentResolver()).getUserId());
                    FartBombRestClient.post(Servlet.FRIEND_REQUEST.toString(), params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(JSONObject jo) {
                            try {
                                if (jo.getBoolean("status")) {
                                    flipper.setVisibility(View.VISIBLE);
                                }
                            } catch (JSONException je) {

                            }
                        }
                    });
                }
            });
            d.show();
        }
    }

    @Override
    public void onClick(View v) {
        TransitionDrawable highlight;
        switch (v.getId()) {
        case R.id.btnHome:
            Intent intent = new Intent(this, PlaybackActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            break;
        case R.id.txtFriends:
            // highlight = (TransitionDrawable) v.getBackground();
            // highlight.startTransition(500);
            // highlight.reverseTransition(500);
            updateListVisibility(v.getId());
            break;
        case R.id.txtBombs:
            // highlight = (TransitionDrawable) v.getBackground();
            // highlight.startTransition(500);
            // highlight.reverseTransition(500);
            updateListVisibility(v.getId());
            break;
        case R.id.txtRating:
            highlight = (TransitionDrawable) v.getBackground();
            highlight.startTransition(500);
            highlight.reverseTransition(500);
            startActivity(new Intent(this, FartHallActivity.class));
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        int friendId = getIntent().getIntExtra("friendId", -1);
        Friend friend = mFbHelper.getFriend(getContentResolver(), friendId);
        progress = FartBombTools.createProgressDialog(this, "Loading friend info, please wait..", true);
        progress.show();
        requestFriendInfo(friendId);
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        ImageView btnHome = (ImageView) findViewById(R.id.btnHome);
        btnHome.setOnClickListener(this);

        TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText(friend.getName());

        txtFriends = (TextView) findViewById(R.id.txtFriends);
        txtBombs = (TextView) findViewById(R.id.txtBombs);
        txtRating = (TextView) findViewById(R.id.txtRating);
        txtFragTitle = (TextView) findViewById(R.id.txtFragmentTitle);

        listFriends = (ListView) findViewById(R.id.friendList);
        friendAdapter = new MutualFriendsAdapter(this);
        listFriends.setAdapter(friendAdapter);
        listFriends.setOnItemClickListener(friendClickListener);

        listBombs = (ListView) findViewById(R.id.bombList);
        bombAdapter = new MutualBombAdapter(this);
        listBombs.setAdapter(bombAdapter);
        listBombs.setOnItemClickListener(bombClickListener);

        flipper = (ViewFlipper) findViewById(R.id.viewflipper);
        slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
        listFriends.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                updateListVisibility(R.id.txtBombs);
            }
        });

        listBombs.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                txtRating.performClick();
            }

            @Override
            public void onSwipeRight() {
                updateListVisibility(R.id.txtFriends);
            }
        });

        if (friend.equals(Friend.getEmpty())) {
            flipper.setVisibility(View.INVISIBLE);
            txtTitle.setText(getIntent().getStringExtra("friendName"));
            displayRequestDialog(friendId);
        }

        txtFriends.setOnClickListener(this);
        txtBombs.setOnClickListener(this);
        txtRating.setOnClickListener(this);

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

    public void requestFriendInfo(int friendId) {
        RequestParams params = new RequestParams();
        params.put("userId", "" + friendId);
        FartBombRestClient.post(Servlet.USER_INFO.toString(), params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONObject jo) {
                progress.dismiss();
                try {
                    if (jo.getBoolean("status")) {
                        TextView txtRating = (TextView) findViewById(R.id.txtRating);
                        txtRating.setText("" + jo.getInt("rank"));
                        ArrayList<Friend> friendList = new ArrayList<Friend>();
                        ArrayList<Bomb> bombList = new ArrayList<Bomb>();
                        JSONArray jData = jo.getJSONArray("data");
                        for (int i = 0; i < jData.length(); i++) {
                            JSONObject obj = jData.getJSONObject(i);
                            if (obj.getInt("type") == TYPE_FRIEND) {
                                Friend tmpFriend = Friend.fromJSON(obj);
                                friendList.add(tmpFriend);
                            } else if (obj.getInt("type") == TYPE_BOMB) {
                                Bomb tmpBomb = Bomb.fromJSON(obj);
                                bombList.add(tmpBomb);
                            }
                        }
                        friendAdapter.updateEntries(friendList);
                        txtFriends.setText(String.valueOf(friendAdapter.getCount()));

                        bombAdapter.updateEntries(bombList);
                        txtBombs.setText(String.valueOf(bombList.size()));

                        updateListVisibility(R.id.txtBombs);

                    }
                } catch (JSONException je) {
                    Log.e(LOG_TAG, je.getMessage(), je);
                }
            }
        });
    }

    /**
     * 
     * @param id
     */
    private void updateListVisibility(int id) {
        txtRating.setLayoutParams(rankSmall);
        switch (id) {
        case R.id.txtFriends:
            txtFriends.setLayoutParams(friendsBig);
            txtBombs.setLayoutParams(bombsSmall);
            txtFragTitle.setText(getString(R.string.title_friends));
            flipper.setInAnimation(slideRightIn);
            flipper.setOutAnimation(slideRightOut);
            if (flipper.getDisplayedChild() != 0) {
                flipper.setDisplayedChild(flipper.indexOfChild(listFriends));
            }
            break;
        case R.id.txtBombs:
            txtFriends.setLayoutParams(friendsSmall);
            txtBombs.setLayoutParams(bombsBig);
            flipper.setInAnimation(slideLeftIn);
            flipper.setOutAnimation(slideLeftOut);
            txtFragTitle.setText("FART JOURNAL");
            if (flipper.getDisplayedChild() != 1) {
                flipper.setDisplayedChild(flipper.indexOfChild(listBombs));
            }
            break;
        default:
            break;
        }
    }
}
