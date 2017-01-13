package com.moreno.fartbomb;

import java.util.Locale;

import org.json.*;

import android.app.*;
import android.content.*;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.*;
import android.util.Log;
import android.widget.*;

import com.loopj.android.http.*;
import com.moreno.fartbomb.data.*;
import com.moreno.fartbomb.network.*;
import com.moreno.fartbomb.provider.FartBomb;
import com.moreno.fartbomb.util.*;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener {
    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();
    private static final String PARAM_TYPE = "type";
    private static final String PREF_FILE = "com.fartbomb.android_preferences";
    private static final SharedPreferences prfSettings = FartbombApplication.getAppContext().getSharedPreferences(PREF_FILE,
            Activity.MODE_PRIVATE | Activity.MODE_MULTI_PROCESS);

    protected final FartBombDb mFbHelper = FartBombDb.getInstance();

    public static final String PREFERENCE_USERNAME = "user_name";
    public static final String PREFERENCE_LOGOFF = "logoff";
    public static final String PREFERENCE_TERMS = "terms";
    public static final String PREFERENCE_CONTACT = "contact";
    public static final String PREFERENCE_NOTIFICATIONS = "notifications";
    public static final String PREFERENCE_EMAIL = "email";
    public static final String PREFERENCE_PHONE = "phone";

    /**
     * Get a copy of the shared preferences file for the application settings
     * 
     * @return The SharedPreferences file
     */
    public static SharedPreferences getSharedPreference() {
        return prfSettings;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onApplyThemeResource(android.content.res.Resources.Theme, int, boolean)
     */
    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resId, boolean first) {
        theme.applyStyle(R.style.FartbombSettings, true);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        User activeUser = mFbHelper.getActiveUser(getContentResolver());

        Preference username = findPreference(PREFERENCE_USERNAME);
        Preference logoff = findPreference(PREFERENCE_LOGOFF);
        Preference email = findPreference(PREFERENCE_EMAIL);
        Preference phone = findPreference(PREFERENCE_PHONE);

        username.setSummary(activeUser.getUserName().toLowerCase(Locale.ENGLISH));
        email.setSummary(activeUser.getEmail().toLowerCase(Locale.ENGLISH));
        phone.setSummary("" + activeUser.getPhoneNumber());
        logoff.setOnPreferenceClickListener(this);
        phone.setOnPreferenceClickListener(this);
        email.setOnPreferenceClickListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.preference.Preference.OnPreferenceClickListener#onPreferenceClick(android.preference.Preference)
     */
    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(PREFERENCE_LOGOFF)) {
            AlertDialog.Builder dialog = FartBombTools.buildDialog(this, "Logoff", "Are you sure you want to log off?");
            dialog.setPositiveButton("Logout", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int id) {
                    mFbHelper.logoff(getContentResolver());
                    dialog.dismiss();
                    finish();
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            dialog.show();

        } else if (preference.getKey().equals(PREFERENCE_PHONE)) {
            final Preference phonePreference = preference;
            AlertDialog.Builder phoneDialog = FartBombTools.buildDialog(this, "Phone Number", "Enter Phone Number");
            final EditText input = new EditText(this);
            input.setBackgroundResource(R.drawable.rounded_corner);
            User activeUser = mFbHelper.getActiveUser(getContentResolver());
            input.setFilters(new InputFilter[] { new PhoneInputFilter(), new InputFilter.LengthFilter(10) });
            input.setText(String.valueOf(activeUser.getPhoneNumber()));
            phoneDialog.setView(input);

            phoneDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int id) {
                    String phoneNumber = input.getText().toString();
                    if (TextUtils.isEmpty(phoneNumber)) {
                        Toast.makeText(SettingsActivity.this, "Phone number cannot be blank", Toast.LENGTH_SHORT).show();
                    } else {
                        updatePhoneNumber(phonePreference, phoneNumber);
                    }
                }

            });

            phoneDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            phoneDialog.show();
        } else if (preference.getKey().equals(PREFERENCE_EMAIL)) {
            final Preference emailPreference = preference;
            AlertDialog.Builder emailDialog = FartBombTools.buildDialog(this, "E-mail", "Enter e-mail address");
            final EditText input = new EditText(this);
            input.setBackgroundResource(R.drawable.rounded_corner);
            final User activeUser = mFbHelper.getActiveUser(getContentResolver());
            input.setText(activeUser.getEmail().toLowerCase(Locale.ENGLISH));
            emailDialog.setView(input);
            emailDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    String email = input.getText().toString();
                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(SettingsActivity.this, "Phone number cannot be blank", Toast.LENGTH_SHORT).show();
                    } else if (!email.equals(activeUser.getEmail())) {
                        updateEmail(emailPreference, email);
                    }
                }
            });

            emailDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            emailDialog.show();

        }
        return false;
    }

    protected void updateEmail(final Preference emailPref, String email) {
        int userId = mFbHelper.getActiveUser(getContentResolver()).getUserId();
        RequestParams params = new RequestParams();
        params.put(PARAM_TYPE, "email");
        params.put(FartBomb.Users.FIELD_USER_ID, String.valueOf(userId));
        params.put(FartBomb.Users.FIELD_EMAIL, email);

        if (FartBombTools.isConnected(this)) {
            FartBombRestClient.post(Servlet.CHANGE_ACCOUNT.toString(), params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(JSONObject jo) {
                    try {
                        if (jo.getBoolean("status")) {
                            JSONObject jUser = jo.getJSONObject("user");
                            mFbHelper.setUser(getContentResolver(), User.fromJSON(jUser));
                            User activeUser = mFbHelper.getActiveUser(getContentResolver());
                            emailPref.setSummary("" + activeUser.getEmail());
                        } else {
                            AlertDialog.Builder dialog = FartBombTools.buildDialog(SettingsActivity.this, "FART!", jo.getString("message"));
                            dialog.show();
                        }
                    } catch (JSONException je) {
                        AlertDialog.Builder dialog = FartBombTools.buildDialog(SettingsActivity.this, "FART!", je.getMessage());
                        dialog.show();
                    }
                }

            });
        } else {
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

    protected void updatePhoneNumber(final Preference phonePref, String newPhoneNumber) {
        long phone = 0;
        try {
            phone = Long.parseLong(newPhoneNumber);
        } catch (NumberFormatException nfe) {
            Log.e(LOG_TAG, "Error parsing new phone number", nfe);
            return;
        }
        int userId = mFbHelper.getActiveUser(getContentResolver()).getUserId();
        RequestParams params = new RequestParams();
        params.put(PARAM_TYPE, "phone");
        params.put(FartBomb.Users.FIELD_USER_ID, String.valueOf(userId));
        params.put(FartBomb.Users.FIELD_PHONE_NUMBER, String.valueOf(phone));

        if (FartBombTools.isConnected(this)) {
            FartBombRestClient.post(Servlet.CHANGE_ACCOUNT.toString(), params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(JSONObject jo) {
                    try {
                        if (jo.getBoolean("status")) {
                            JSONObject jUser = jo.getJSONObject("user");
                            mFbHelper.setUser(getContentResolver(), User.fromJSON(jUser));
                            User activeUser = mFbHelper.getActiveUser(getContentResolver());
                            phonePref.setSummary("" + activeUser.getPhoneNumber());
                        } else {
                            AlertDialog.Builder dialog = FartBombTools.buildDialog(SettingsActivity.this, "FART!", jo.getString("message"));
                            dialog.show();
                        }
                    } catch (JSONException je) {
                        AlertDialog.Builder dialog = FartBombTools.buildDialog(SettingsActivity.this, "FART!", je.getMessage());
                        dialog.show();
                    }
                }
            });
        } else {
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
