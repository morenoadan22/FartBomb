package com.moreno.fartbomb;

import java.util.Locale;

import org.json.*;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;

import com.loopj.android.http.*;
import com.moreno.fartbomb.network.*;
import com.moreno.fartbomb.provider.FartBomb;
import com.moreno.fartbomb.util.FartBombTools;

public class ForgottenUserActivity extends Activity {
    protected static final String JSON_STATUS = "status";
    protected static final String JSON_MESSAGE = "message";

    private ImageButton btnSubmit;
    private EditText etxEmail;
    private ProgressDialog progress;

    private boolean isValidEntry() {
        boolean valid = true;
        String email = etxEmail.getText().toString().toUpperCase(Locale.ENGLISH);
        etxEmail.setError(null);
        etxEmail.setBackgroundResource(R.drawable.rounded_corner);

        if (TextUtils.isEmpty(email)) {
            etxEmail.requestFocus();
            etxEmail.setBackgroundResource(R.drawable.rounded_corner_error);
            etxEmail.setError("This field cannot be blank");
            valid = false;
        } else if (!FartBombTools.validateEmail(email)) {
            etxEmail.requestFocus();
            etxEmail.setBackgroundResource(R.drawable.rounded_corner_error);
            etxEmail.setError("Please enter a valid email address");
            valid = false;
        }
        return valid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFartbombLayout(R.layout.activity_forgotten_user);

        btnSubmit = (ImageButton) findViewById(R.id.btnSubmit);
        etxEmail = (EditText) findViewById(R.id.etxEmail);

        etxEmail.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    retreiveUserPassword();
                }
                return false;
            }
        });

        btnSubmit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                retreiveUserPassword();
            }
        });

    }

    private void retreiveUserPassword() {
        if (isValidEntry()) {
            progress = FartBombTools.createProgressDialog(ForgottenUserActivity.this, getString(R.string.prgSendingEmail), true);
            progress.show();
            String email = etxEmail.getText().toString().toUpperCase(Locale.ENGLISH);
            etxEmail.setText("");
            RequestParams params = new RequestParams();
            params.put(FartBomb.Users.FIELD_EMAIL, email);
            if (FartBombTools.isConnected(ForgottenUserActivity.this)) {
                FartBombRestClient.post(Servlet.FORGOTTEN.toString(), params, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(JSONObject json) {
                        try {
                            if (json.getBoolean(JSON_STATUS)) {
                                progress.dismiss();
                                AlertDialog.Builder dialog = FartBombTools.buildDialog(ForgottenUserActivity.this, "E-mail Notification",
                                        "e-mail was sussesfully sent to " + json.getString(FartBomb.Users.FIELD_EMAIL));
                                dialog.show();
                            } else {
                                progress.dismiss();
                                AlertDialog.Builder dialog = FartBombTools.buildDialog(ForgottenUserActivity.this, "FART!",
                                        json.getString("message"));
                                dialog.show();
                            }

                        } catch (JSONException je) {
                            AlertDialog.Builder dialog = FartBombTools.buildDialog(ForgottenUserActivity.this, "FART!", je.getMessage());
                            dialog.show();
                        }
                    }
                });
            } else {
                progress.dismiss();
                AlertDialog.Builder dialog = FartBombTools.buildDialog(ForgottenUserActivity.this, "Network Down",
                        "Network is not available, please enable mobile network.");
                dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                        dialog.cancel();
                    }
                });
                dialog.setCancelable(true);
                dialog.show();
            }
        }
    }

    private void setFartbombLayout(int layoutReference) {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(layoutReference);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
    }
}
