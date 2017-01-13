package com.moreno.fartbomb;

import java.util.Locale;

import org.json.*;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;

import com.loopj.android.http.*;
import com.moreno.fartbomb.data.*;
import com.moreno.fartbomb.network.*;
import com.moreno.fartbomb.provider.FartBomb;
import com.moreno.fartbomb.util.*;

/**
 * 
 * LoginActivity
 * 
 * This activity logs in the user and syncs the db with the server user table.
 * 
 * @author adan
 */
public class LoginActivity extends Activity implements OnClickListener {
    @SuppressWarnings("unused")
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    private ProgressDialog progress;
    private TextView txtForgotPass;
    private EditText etxUserName, etxPassword;
    private ImageButton btnSubmit;
    private final FartBombDb mFbHelper = FartBombDb.getInstance();

    /**
     * Removes the text from the fields
     */
    private void clearTextFields() {
        etxUserName.setText("");
        etxPassword.setText("");
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
        case R.id.txtForgot:
            intent = new Intent(this, ForgottenUserActivity.class);
            startActivity(intent);
            break;
        case R.id.btnSubmit:
            startLoginProcess();
            break;
        case R.id.btnBack:
            intent = new Intent(this, SignupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
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
        setFartbombLayout(R.layout.activity_login);

        etxUserName = (EditText) findViewById(R.id.etxUserName);
        etxPassword = (EditText) findViewById(R.id.etxPassword);
        txtForgotPass = (TextView) findViewById(R.id.txtForgot);
        btnSubmit = (ImageButton) findViewById(R.id.btnSubmit);
        ImageView btnBack = (ImageView) findViewById(R.id.btnBack);

        btnBack.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        txtForgotPass.setOnClickListener(this);

        etxUserName.setFilters(new InputFilter[] { new InputFilter.LengthFilter(15), new AlphaNumericFilter() });

        etxPassword.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    startLoginProcess();
                }
                return false;
            }
        });

        // hide the keyboard until the user touches an EditText field
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /**
     * Syncs android database with server db and re-directs to {@link PlaybackActivity}
     * 
     * @param user User created from the JSON response
     */
    private void performLogin(User user) {
        progress.dismiss();
        mFbHelper.setUserActive(getContentResolver(), user);
        Intent intent = new Intent(this, PlaybackActivity.class);
        intent.putExtra(FartBomb.Users.FIELD_USER_ID, user.getUserId());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void setFartbombLayout(int layoutReference) {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(layoutReference);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
    }

    /**
     * Validates the text input from user and calls the {@link http://fartbomb.net/FartbombServer/Authentication} Servlet
     */
    private void startLoginProcess() {
        if (validateUser()) {
            progress = FartBombTools.createProgressDialog(LoginActivity.this, getString(R.string.prgLogin), true);
            progress.show();
            String username = etxUserName.getText().toString().trim().toUpperCase(Locale.ENGLISH);
            String password = etxPassword.getText().toString().trim();
            clearTextFields();
            RequestParams params = new RequestParams();
            params.put(FartBomb.Users.FIELD_USERNAME, username);
            params.put(FartBomb.Users.FIELD_PASSWORD, password);
            if (FartBombTools.isConnected(this)) {
                FartBombRestClient.get(Servlet.AUTHENTICATE.toString(), params, new JsonHttpResponseHandler() {

                    /*
                     * (non-Javadoc)
                     * 
                     * @see com.loopj.android.http.JsonHttpResponseHandler#onSuccess(org.json.JSONObject)
                     */
                    @Override
                    public void onSuccess(JSONObject jo) {
                        try {
                            if (jo.getBoolean("status")) {
                                performLogin(User.fromJSON(jo.getJSONObject("user")));
                            } else {
                                progress.dismiss();
                                AlertDialog.Builder dialog = FartBombTools.buildDialog(LoginActivity.this, "FART!", jo.getString("message"));
                                dialog.show();
                            }
                        } catch (JSONException e) {
                            AlertDialog.Builder dialog = FartBombTools.buildDialog(LoginActivity.this, "FART!", e.getMessage());
                            dialog.show();
                        }
                    }
                });
            } else {
                progress.dismiss();
                AlertDialog.Builder dialog = FartBombTools.buildDialog(this, "Network Down",
                        "Network is not available, please enable mobile network.");
                dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        }
    }

    /**
     * Validates the input from the user
     * 
     * @return {@code true} if valid {@code false} otherwise
     */
    private boolean validateUser() {
        boolean valid = true;
        String username = etxUserName.getText().toString().trim().toUpperCase(Locale.ENGLISH);
        String password = etxPassword.getText().toString().trim();

        etxUserName.setError(null);
        etxPassword.setError(null);

        etxUserName.setBackgroundResource(R.drawable.rounded_corner);
        etxPassword.setBackgroundResource(R.drawable.rounded_corner);

        if (TextUtils.isEmpty(username)) {
            etxUserName.requestFocus();
            etxUserName.setBackgroundResource(R.drawable.rounded_corner_error);
            etxUserName.setError("This field cannot be blank");
            valid = false;
        } else if (TextUtils.isEmpty(password)) {
            etxPassword.requestFocus();
            etxPassword.setBackgroundResource(R.drawable.rounded_corner_error);
            etxPassword.setError("This field cannot be blank");
            valid = false;
        }
        return valid;
    }

}
