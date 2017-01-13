package com.moreno.fartbomb;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.*;
import android.content.Context;
import android.media.*;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.*;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.moreno.fartbomb.data.*;
import com.moreno.fartbomb.network.*;
import com.moreno.fartbomb.provider.FartBomb;
import com.moreno.fartbomb.util.*;

public class SaveFileDialog extends Activity implements OnClickListener, OnCompletionListener {

    class AudioByteHandler extends FileAsyncHttpResponseHandler {

        public AudioByteHandler(Context c) {
            super(c);
        }

    };

    class SendAudioBytesTask extends AsyncTask<Bomb, Void, String> {
        @Override
        protected String doInBackground(Bomb... bombs) {
            Bomb bomb = bombs[0];
            String responseBody = "";
            try {
                ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair(FartBomb.Bombs.FIELD_FILE_NAME, bomb.getFileName()));
                params.add(new BasicNameValuePair(FartBomb.Bombs.FIELD_NAME, bomb.getName()));
                params.add(new BasicNameValuePair(FartBomb.Bombs.FIELD_LENGTH, "" + bomb.getLength()));
                params.add(new BasicNameValuePair(FartBomb.Bombs.FIELD_RATING, "" + bomb.getRating()));
                params.add(new BasicNameValuePair(FartBomb.Bombs.FIELD_TIME_STAMP, "" + bomb.getTimeStamp()));
                params.add(new BasicNameValuePair(FartBomb.Bombs.FIELD_USER_ID, "" + bomb.getUserId()));
                params.add(new BasicNameValuePair(FartBomb.Bombs.FIELD_BYTE_ARRAY, bomb.getEncodedBytes()));

                HttpClient httpClient = new DefaultHttpClient();
                HttpPost post = new HttpPost(FartBombRestClient.getAbsoluteUrl(Servlet.SAVE_BOMB.toString()));
                post.setEntity(new UrlEncodedFormEntity(params));
                HttpResponse response = httpClient.execute(post);
                HttpEntity entity = response.getEntity();
                responseBody = EntityUtils.toString(entity);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return responseBody;
        }

        @Override
        protected void onPostExecute(String response) {
            progress.dismiss();
            Bomb bomb = parseBomb(response);
            mFbHelper.addBomb(getContentResolver(), bomb);
            File f = new File(bomb.getFileName());
            if (f.exists()) {
                f.delete();
            }
            finish();
        }

    }

    private ProgressDialog progress;

    private static final String LOG_TAG = SaveFileDialog.class.getSimpleName();

    public static int snoop(short[] outData, int kind) {
        try {
            Class<?> c = MediaPlayer.class;
            Method m = c.getMethod("snoop", outData.getClass(), Integer.TYPE);
            m.setAccessible(true);
            m.invoke(c, outData, kind);
            return Integer.parseInt((m.invoke(c, outData, kind)).toString());
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    private AudioManager audio;
    private MediaPlayer mPlayer = null;
    protected FartBombDb mFbHelper = FartBombDb.getInstance();
    private String fileName;
    private EditText etxBombName;

    protected short[] mAudioData = new short[1024];

    private void commitBomb() {
        if (!validateBombName()) {
            progress.dismiss();
            return;
        }
        Bomb bomb = new Bomb(getFileName());
        User user = mFbHelper.getActiveUser(getContentResolver());
        bomb.setUserId(user.getUserId());
        bomb.setName(etxBombName.getText().toString().trim());
        bomb.setLength(mPlayer.getDuration());
        bomb.setRating(0);
        bomb.setTimeStamp(System.currentTimeMillis());
        bomb.setId(0);
        bomb.setAudioBytes(readAduioFileData(bomb.getFileName()));

        SendAudioBytesTask task = new SendAudioBytesTask();
        task.execute(bomb);
    }

    public String getFileName() {
        return fileName != null ? fileName : "";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btnPlayBomb:
            startPlaying(getFileName());
            break;
        case R.id.btnSubmit:
            progress = FartBombTools.createProgressDialog(this, getString(R.string.prgUploading), true);
            progress.show();
            commitBomb();
            break;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mPlayer.release();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();

        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        setFileName(extras.getString(PlaybackActivity.EXTRA_FILE_NAME));
        setContentView(R.layout.dialog_save_file);
        ImageView btnPlay = (ImageView) findViewById(R.id.btnPlayBomb);
        ImageView btnSubmit = (ImageView) findViewById(R.id.btnSubmit);
        etxBombName = (EditText) findViewById(R.id.etxBombName);

        btnPlay.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);

        /** Set up the Media Player data source **/
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(getFileName());
            mPlayer.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Could not find file " + getFileName(), e);
        }

    }

    @Override
    protected void onDestroy() {
        File f = new File(getFileName());
        if (f.exists()) {
            f.delete();
        }
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

    private byte[] readAduioFileData(String filePath) {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        File file = new File(filePath);
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            byte[] buff = new byte[10240];
            int i = Integer.MAX_VALUE;
            while ((i = is.read(buff, 0, buff.length)) > 0) {
                baout.write(buff, 0, i);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return baout.toByteArray();
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void startPlaying(String fileName) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(fileName);
            mPlayer.prepare();
            mPlayer.start();
            while (mPlayer.isPlaying()) {
                Log.w(LOG_TAG, "RES: " + snoop(mAudioData, 0));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    private boolean validateBombName() {
        etxBombName.setError(null);
        String bombName = etxBombName.getText().toString().trim();
        if (TextUtils.isEmpty(bombName) || bombName.equals("")) {
            etxBombName.setError("Give your fartbomb a name");
            return false;
        }
        return true;
    }
}
