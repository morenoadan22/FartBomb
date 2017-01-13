package com.moreno.fartbomb;

import java.util.Locale;

import org.json.*;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.text.*;
import android.util.Log;
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

public class SignupActivity extends Activity implements OnClickListener {
    private static final String LOG_TAG = SignupActivity.class.getSimpleName();
    private ProgressDialog progress;
    private ImageButton btnSubmit;
    private TextView txtExistingUser;
    private EditText etxUserName, etxEmail, etxPassword, etxConfirmPass, etxPhone;
    private final FartBombDb mFbHelper = FartBombDb.getInstance();

    /**
     * Remove the text from the text fields in the {@link Activity}
     */
    private void clearTextFields() {
        etxUserName.setText("");
        etxEmail.setText("");
        etxPassword.setText("");
        etxConfirmPass.setText("");
        etxPhone.setText("");
    }

    /**
     * Checks if the input is empty and valid against regular expressions
     * 
     * @return the {@link User} object populated with the user's input.
     */
    private User isValid() {
        User user = new User();
        user.setUserName(etxUserName.getText().toString().trim());
        user.setEmail(etxEmail.getText().toString().trim().toUpperCase(Locale.ENGLISH));
        user.setPassword(etxPassword.getText().toString().trim());
        String phoneNumber = etxPhone.getText().toString().replaceAll("[^\\d]", "");
        String confirmPass = etxConfirmPass.getText().toString().trim();

        etxUserName.setError(null);
        etxEmail.setError(null);
        etxPassword.setError(null);
        etxConfirmPass.setError(null);
        etxPhone.setError(null);

        etxUserName.setBackgroundResource(R.drawable.rounded_corner);
        etxEmail.setBackgroundResource(R.drawable.rounded_corner);
        etxPassword.setBackgroundResource(R.drawable.rounded_corner);
        etxConfirmPass.setBackgroundResource(R.drawable.rounded_corner);
        etxPhone.setBackgroundResource(R.drawable.rounded_corner);

        if (TextUtils.isEmpty(user.getUserName())) {
            etxUserName.requestFocus();
            etxUserName.setBackgroundResource(R.drawable.rounded_corner_error);
            etxUserName.setError("This field cannot be blank");
            return user;
        } else if (TextUtils.isEmpty(user.getEmail()) || !FartBombTools.validateEmail(user.getEmail())) {
            etxEmail.requestFocus();
            etxEmail.setBackgroundResource(R.drawable.rounded_corner_error);
            etxEmail.setError("Please enter a valid email address.");
            return user;
        } else if (TextUtils.isEmpty(user.getPassword())) {
            etxPassword.requestFocus();
            etxPassword.setBackgroundResource(R.drawable.rounded_corner_error);
            etxPassword.setError("This field cannot be blank");
            return user;
        } else if (TextUtils.isEmpty(confirmPass)) {
            etxConfirmPass.requestFocus();
            etxConfirmPass.setBackgroundResource(R.drawable.rounded_corner_error);
            etxConfirmPass.setError("This field cannot be blank");
            return user;
        } else if (TextUtils.isEmpty(phoneNumber)) {
            etxPhone.requestFocus();
            etxPhone.setBackgroundResource(R.drawable.rounded_corner_error);
            etxPhone.setError("This field cannot be blank");
            return user;
        } else if (phoneNumber.length() < 10) {
            etxPhone.requestFocus();
            etxPhone.setBackgroundResource(R.drawable.rounded_corner_error);
            etxPhone.setError("Enter a 10 digit phone number, area code first");
            return user;
        }

        if (!user.getPassword().equals(confirmPass)) {
            etxPassword.requestFocus();
            etxPassword.setBackgroundResource(R.drawable.rounded_corner_error);
            etxConfirmPass.setBackgroundResource(R.drawable.rounded_corner_error);
            etxPassword.setError("Passwords do not match");
            etxConfirmPass.setError("Passwords do not match");
            return user;
        }

        user.setPhoneNumber(Long.parseLong(phoneNumber));
        user.setActive(true);

        return user;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.btnSubmit:
            startSignupProcess();
            break;
        case R.id.txtExistingUser:
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
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
        setFartbombLayout(R.layout.activity_signup);

        btnSubmit = (ImageButton) findViewById(R.id.btnSubmit);
        txtExistingUser = (TextView) findViewById(R.id.txtExistingUser);
        etxUserName = (EditText) findViewById(R.id.etxUserName);
        etxEmail = (EditText) findViewById(R.id.etxEmail);
        etxPassword = (EditText) findViewById(R.id.etxPassword);
        etxConfirmPass = (EditText) findViewById(R.id.etxConfirmPass);
        etxPhone = (EditText) findViewById(R.id.etxPhone);

        btnSubmit.setOnClickListener(this);
        txtExistingUser.setOnClickListener(this);

        etxUserName.setFilters(new InputFilter[] { new AlphaNumericFilter(), new InputFilter.LengthFilter(15) });
        etxPhone.setFilters(new InputFilter[] { new PhoneInputFilter(), new InputFilter.LengthFilter(10) });
        etxPhone.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    startSignupProcess();
                }
                return false;
            }
        });

        // hide the keyboard until the user touches an EditText field
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void performSignup(User user) {
        user.setActive(true);
        if (mFbHelper.addUser(getContentResolver(), user) != -1) {
            Intent intent = new Intent(this, PlaybackActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    private void setFartbombLayout(int layoutReference) {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(layoutReference);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
    }

    private void startSignupProcess() {
        User user = isValid();
        if (user.isActive()) {
            progress = FartBombTools.createProgressDialog(this, "Signing up, please wait...", true);
            progress.show();
            clearTextFields();
            RequestParams params = new RequestParams();
            params.put(FartBomb.Users.FIELD_USERNAME, user.getUserName());
            params.put(FartBomb.Users.FIELD_PASSWORD, user.getPassword());
            params.put(FartBomb.Users.FIELD_EMAIL, user.getEmail());
            params.put(FartBomb.Users.FIELD_PHONE_NUMBER, String.valueOf(user.getPhoneNumber()));

            if (FartBombTools.isConnected(this)) {
                FartBombRestClient.get(Servlet.SIGNUP.toString(), params, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(JSONObject jo) {
                        try {
                            if (jo.getBoolean("status")) {
                                Log.e(LOG_TAG, "RESPONSE: " + jo.toString());
                                performSignup(User.fromJSON(jo.getJSONObject("user")));
                            } else {
                                progress.dismiss();
                                AlertDialog.Builder dialog = FartBombTools.buildDialog(SignupActivity.this, "FART!",
                                        jo.getString("message"));
                                dialog.show();
                            }
                        } catch (JSONException e) {
                            progress.dismiss();
                            AlertDialog.Builder dialog = FartBombTools.buildDialog(SignupActivity.this, "FART!", e.getMessage());
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
                dialog.setCancelable(true);
                dialog.show();
            }
        }
    }

}
