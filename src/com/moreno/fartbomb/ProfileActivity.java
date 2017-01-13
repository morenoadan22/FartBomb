package com.moreno.fartbomb;

import java.io.*;

import android.app.ProgressDialog;
import android.content.*;
import android.database.Cursor;
import android.graphics.drawable.TransitionDrawable;
import android.media.*;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
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

public class ProfileActivity extends FragmentActivity implements OnClickListener {
    private static final String LOG_TAG = ProfileActivity.class.getSimpleName();
    private final FartBombDb mFbHelper = FartBombDb.getInstance();
    private User activeUser;
    private TextView txtFriends, txtBombs, txtRating, txtFragTitle;
    private ListView listFriends, listBombs;
    private ImageView btnAddFriends;
    private ProgressDialog progressDialog;
    protected FriendsAdapter friendAdapter;
    protected BombsAdapter bombAdapter;
    private MediaPlayer mPlayer;
    private AudioManager audio;
    private ViewFlipper flipper;
    private Animation slideLeftOut, slideLeftIn, slideRightIn, slideRightOut;
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

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SyncHandler.BROADCAST_SYNC_FINISHED)) {
                txtFriends.setText(String.valueOf(mFbHelper.getFriendCount(getContentResolver(), activeUser.getUserId())));
                txtBombs.setText(String.valueOf(mFbHelper.getBombsCount(getContentResolver(), activeUser.getUserId())));
            }
        }
    };

    OnItemClickListener friendClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cursor = (Cursor) friendAdapter.getItem(position);
            final Friend friend = new Friend(cursor);
            friendAdapter.setSelected(friend.getId());
            if (friend.isAccepted()) {
                Intent intent = new Intent(ProfileActivity.this, ViewFriendActivity.class);
                intent.putExtra("friendId", friend.getId());
                startActivity(intent);
            }
        }
    };

    OnItemClickListener bombClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            Cursor cursor = (Cursor) bombAdapter.getItem(position);
            final Bomb bomb = new Bomb(cursor);
            bombAdapter.setSelected(bomb.getId());

            ImageView btnPlay = (ImageView) view.findViewById(R.id.btnPlay);

            btnPlay.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {
                    if (v.getId() == R.id.btnPlay) {
                        progressDialog = new ProgressDialog(ProfileActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
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

    private void addBroadcastReciever() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SyncHandler.BROADCAST_SYNC_FINISHED);
        registerReceiver(receiver, filter);
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
            updateListVisibility(v.getId());
            break;
        case R.id.txtBombs:
            updateListVisibility(v.getId());
            break;
        case R.id.txtRating:
            highlight = (TransitionDrawable) v.getBackground();
            highlight.startTransition(500);
            highlight.reverseTransition(500);
            startActivity(new Intent(this, FartHallActivity.class));
            break;
        case R.id.btnAddFriends:
            startActivity(new Intent(this, FindFriendsActivity.class));
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        activeUser = mFbHelper.getActiveUser(getContentResolver());
        FartBombTools.syncServerData(this, activeUser);

        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        txtFriends = (TextView) findViewById(R.id.txtFriends);
        txtBombs = (TextView) findViewById(R.id.txtBombs);
        txtRating = (TextView) findViewById(R.id.txtRating);
        txtFragTitle = (TextView) findViewById(R.id.txtFragmentTitle);
        listFriends = (ListView) findViewById(R.id.friendList);
        listBombs = (ListView) findViewById(R.id.bombList);
        btnAddFriends = (ImageView) findViewById(R.id.btnAddFriends);

        TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
        ImageView btnHome = (ImageView) findViewById(R.id.btnHome);

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

        txtTitle.setText(activeUser.getUserName());
        txtFriends.setText(String.valueOf(mFbHelper.getFriendCount(getContentResolver(), activeUser.getUserId())));
        int bombCount = mFbHelper.getBombsCount(getContentResolver(), activeUser.getUserId());
        txtBombs.setText(String.valueOf(bombCount));
        txtRating.setText("" + activeUser.getRank());

        txtFriends.setOnClickListener(this);
        txtBombs.setOnClickListener(this);
        txtRating.setOnClickListener(this);
        btnAddFriends.setOnClickListener(this);
        btnHome.setOnClickListener(this);

        friendAdapter = new FriendsAdapter(this, mFbHelper.getFriends(getContentResolver(), activeUser.getUserId()));
        bombAdapter = new BombsAdapter(this, mFbHelper.getBombs(getContentResolver(), activeUser.getUserId(), null));
        listFriends.setAdapter(friendAdapter);
        listBombs.setAdapter(bombAdapter);

        listBombs.setOnItemClickListener(bombClickListener);
        listFriends.setOnItemClickListener(friendClickListener);

        updateListVisibility(R.id.txtBombs);
        addBroadcastReciever();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected Bomb parseBomb(String response) {
        String[] elements = TextUtils.split(response, "\\|");
        Bomb bomb = new Bomb(elements[4]);
        bomb.setId(Integer.parseInt(elements[0]));
        bomb.setName(elements[1]);
        bomb.setLength(Long.parseLong(elements[2]));
        bomb.setUserId(Integer.parseInt(elements[3]));
        bomb.setRating(Double.parseDouble(elements[5]));
        bomb.setTimeStamp(Long.parseLong(elements[6]));
        String encoded = elements[7];
        try {
            byte[] decoded = Base64.decode(encoded);
            bomb.setAudioBytes(decoded);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error decoding the audio string " + encoded, e);
        }
        return bomb;
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

    protected void replayBomb(byte[] buffer) {
        try {
            File tmp = File.createTempFile("fartbomb", "3gp", getCacheDir());
            tmp.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tmp);
            fos.write(buffer);
            fos.close();
            mPlayer = new MediaPlayer();
            FileInputStream fis = new FileInputStream(tmp);
            mPlayer.setDataSource(fis.getFD());
            mPlayer.prepare();
            mPlayer.start();
            fis.close();
        } catch (IOException ex) {
            Log.e(LOG_TAG, "replayBomb()", ex);
        }
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
            btnAddFriends.setVisibility(View.VISIBLE);
            break;
        case R.id.txtBombs:
            txtFriends.setLayoutParams(friendsSmall);
            txtBombs.setLayoutParams(bombsBig);
            txtFragTitle.setText(getString(R.string.title_bombs));
            flipper.setInAnimation(slideLeftIn);
            flipper.setOutAnimation(slideLeftOut);
            if (flipper.getDisplayedChild() != 1) {
                flipper.setDisplayedChild(flipper.indexOfChild(listBombs));
            }
            btnAddFriends.setVisibility(View.GONE);
            break;
        default:
            break;
        }
    }
}
